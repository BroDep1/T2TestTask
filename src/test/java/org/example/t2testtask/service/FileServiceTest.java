package org.example.t2testtask.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.t2testtask.entity.Award;
import org.example.t2testtask.entity.Employee;
import org.example.t2testtask.entity.UploadStatus;
import org.example.t2testtask.entity.UploadStatusType;
import org.example.t2testtask.repository.UploadStatusRepository;
import org.example.t2testtask.repository.UploadStatusTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private UploadStatusRepository uploadStatusRepository;
    @Mock
    private UploadStatusTypeRepository uploadStatusTypeRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private EmployeeAwardService employeeAwardService;
    @Mock
    private AwardService awardService;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private ApplicationContext context;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    FileService fileService;

    @BeforeEach
    void setUp() {
        UploadStatusType type = mock(UploadStatusType.class);
        when(uploadStatusTypeRepository.findByName(any(String.class))).thenReturn(Optional.of(type));
    }

    FileInputStream createMockInputStream() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test Sheet");

        var dataRow = sheet.createRow(0);
        dataRow.createCell(0).setCellValue("1");
        dataRow.createCell(1).setCellValue("John Johnovich");
        dataRow.createCell(2).setCellValue("100");
        dataRow.createCell(3).setCellValue("Money");
        dataRow.createCell(4).setCellValue("2023-08-01");

        File file = File.createTempFile("test", ".xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            workbook.write(outputStream);
        }
        workbook.close();
        return new FileInputStream(file);
    }


    @Test
    void uploadFile() {
        UploadStatus mockUploadStatus = mock(UploadStatus.class);
        FileService mockFileService = mock(FileService.class);

        when(uploadStatusRepository.save(any(UploadStatus.class))).thenReturn(mockUploadStatus);
        when(context.getBean(FileService.class)).thenReturn(mockFileService);
        var result = fileService.uploadFile(multipartFile);

        verify(mockFileService).parseFile(multipartFile, mockUploadStatus);
        assertNotNull(result);
        assertEquals(result, mockUploadStatus);
    }

    @Test
    void parseXlsxFile() throws Exception {
        UploadStatus mockUploadStatus = mock(UploadStatus.class);
        FileInputStream mockStream = createMockInputStream();
        Employee mockEmployee = mock(Employee.class);
        Award mockAward = mock(Award.class);

        when(multipartFile.getInputStream()).thenReturn(mockStream);
        when(multipartFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(employeeService.existsById(1L)).thenReturn(true);
        when(employeeService.findById(1L)).thenReturn(mockEmployee);
        when(awardService.save(100L, "Money", LocalDate.parse("2023-08-01", ISO_DATE))).thenReturn(mockAward);

        fileService.parseFile(multipartFile, mockUploadStatus);

        verify(uploadStatusRepository).save(mockUploadStatus);
        verify(employeeAwardService, atLeastOnce()).save(mockEmployee, mockAward);

    }
}