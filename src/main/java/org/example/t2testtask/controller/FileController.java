package org.example.t2testtask.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.t2testtask.dto.response.UploadStatusResponse;
import org.example.t2testtask.mapper.UploadStatusMapper;
import org.example.t2testtask.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;
    private final UploadStatusMapper uploadStatusMapper;

    @Operation(summary = "Загрузка файла")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadStatusResponse uploadFile(@RequestParam("file") MultipartFile file) {
        return uploadStatusMapper.toUploadStatusResponse(fileService.uploadFile(file));
    }

    @Operation(summary = "Получение статуса загрузки файла")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/upload-status")
    public UploadStatusResponse getUploadStatus(@PathVariable Long id) {
        return uploadStatusMapper.toUploadStatusResponse(fileService.getUploadStatus(id));
    }

}
