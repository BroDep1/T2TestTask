package org.example.t2testtask.mapper;

import org.example.t2testtask.dto.response.UploadStatusResponse;
import org.example.t2testtask.entity.UploadStatus;
import org.springframework.stereotype.Component;

@Component
public class UploadStatusMapper {
    public UploadStatusResponse toUploadStatusResponse(UploadStatus uploadStatus){
        return new UploadStatusResponse(
                uploadStatus.getId(),
                uploadStatus.getUploadStatusType().getName()
        );
    }
}
