package org.example.t2testtask.repository;

import org.example.t2testtask.entity.UploadStatusType;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UploadStatusTypeRepository extends CrudRepository<UploadStatusType, Long> {
    Optional<UploadStatusType> findByName(String name);
}
