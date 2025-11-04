package org.example.t2testtask.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.t2testtask.entity.UploadStatus;
import org.example.t2testtask.entity.UploadStatusType;
import org.example.t2testtask.exeption.FileParsingException;
import org.example.t2testtask.exeption.ResourceNotFoundException;
import org.example.t2testtask.repository.UploadStatusRepository;
import org.example.t2testtask.repository.UploadStatusTypeRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ISO_DATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final UploadStatusRepository uploadStatusRepository;
    private final UploadStatusTypeRepository uploadStatusTypeRepository;
    private final EmployeeService employeeService;
    private final EmployeeAwardService employeeAwardService;
    private final AwardService awardService;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationContext context;

    @Transactional
    public UploadStatus uploadFile(MultipartFile file) {
        log.info("Received file {}", file.getOriginalFilename());
        UploadStatus uploadStatus = initFileUpload();
        context.getBean(FileService.class).parseFile(file, uploadStatus);
        return uploadStatus;
    }

    @Async("taskExecutor")
    protected void parseFile(MultipartFile file, UploadStatus uploadStatus) {
        log.info("Parsing file {} initialised", file.getOriginalFilename());
        try (InputStream stream = file.getInputStream()) {
            Workbook workbook;
            if (file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".csv")) {
                workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("CSV");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    int rowNum = 0;
                    while ((line = reader.readLine()) != null) {
                        Row row = sheet.createRow(rowNum++);
                        String[] parts = line.split("[,;]");
                        for (int i = 0; i < parts.length; i++) {
                            row.createCell(i).setCellValue(parts[i].trim());
                        }
                    }
                }
            }
            else {
                workbook = new XSSFWorkbook(stream);
            }
            Sheet sheet = workbook.getSheetAt(0);
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            for (Row row : sheet) {
                txTemplate.executeWithoutResult((status) -> {
                    try {
                        long employeeId, awardId;
                        String awardName;
                        LocalDate receivedDate;
                        employeeId = getLongValue(row.getCell(0));
                        awardId = getLongValue(row.getCell(2));
                        awardName = getStringValue(row.getCell(3));
                        receivedDate = parseDate(row.getCell(4));
                        if (employeeService.existsById(employeeId)) {
                            employeeAwardService.save(
                                    employeeService.findById(employeeId),
                                    awardService.save(awardId, awardName, receivedDate)
                            );
                        }
                    }
                    catch (Exception e) {
                        log.error("Parsing row {} in file {} failed", row.getRowNum(), file.getOriginalFilename());
                        status.setRollbackOnly();
                    }
                });
            }
            workbook.close();
        } catch (Exception e) {
            changeUploadStatus(uploadStatus, "failed");
            log.error("Parsing file {} failed", file.getOriginalFilename());
            throw new FileParsingException("Ошибка при чтении Excel-файла");
        }
        changeUploadStatus(uploadStatus, "finished");
        log.info("Parsing file {} finished", file.getOriginalFilename());
    }

    private Long getLongValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        String s = cell.getStringCellValue().trim();
        return s.isEmpty() ? null : Long.parseLong(s);
    }

    private String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return cell.getStringCellValue().trim();
    }

    private LocalDate parseDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        String str = cell.getStringCellValue().trim();
        return str.isEmpty() ? null : LocalDate.parse(str, ISO_DATE);
    }

    private UploadStatus initFileUpload() {
        UploadStatus uploadStatus = new UploadStatus();
        uploadStatus.setUploadStatusType(
                findByName("initialised")
        );
        return uploadStatusRepository.save(uploadStatus);
    }

    private void changeUploadStatus(UploadStatus uploadStatus, String name) {
        uploadStatus.setUploadStatusType(
                findByName(name)
        );
        uploadStatusRepository.save(uploadStatus);
    }

    private UploadStatusType findByName(String name) {
        return uploadStatusTypeRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Тип статуса загрузки %s не найден".formatted(name)));
    }

    public UploadStatus getUploadStatus(Long uploadStatusId) {
        return uploadStatusRepository.findById(uploadStatusId)
                .orElseThrow(() -> new ResourceNotFoundException("Статус загрузки с id %s не найден".formatted(uploadStatusId)));
    }
}
