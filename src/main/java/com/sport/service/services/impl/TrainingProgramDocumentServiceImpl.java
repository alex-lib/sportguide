package com.sport.service.services.impl;

import com.sport.service.entities.training_program.TrainingProgramDocument;
import com.sport.service.repositories.TrainingProgramDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrainingProgramDocumentServiceImpl {
    private final TrainingProgramDocumentRepository trainingProgramDocumentRepository;

    public String save(MultipartFile file) {
        TrainingProgramDocument trainingProgramDocument = new TrainingProgramDocument();
        try {
            trainingProgramDocument.setContent(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        trainingProgramDocument.setFileSize(file.getSize());
        trainingProgramDocument.setUploadedAt(LocalDateTime.now());
        trainingProgramDocument.setLastModifiedAt(LocalDateTime.now());
        return trainingProgramDocumentRepository.save(trainingProgramDocument).getId();
    }

    public TrainingProgramDocument findById(String programIdInMongoDB) {
        return trainingProgramDocumentRepository.findById(programIdInMongoDB).orElse(null);
    }

    public void delete(String programIdInMongoDB) {
        trainingProgramDocumentRepository.deleteById(programIdInMongoDB);
    }

    public void update(String id, MultipartFile file) {
        if (file != null) {
            TrainingProgramDocument trainingProgramDocument = findById(id);
            try {
                trainingProgramDocument.setContent(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            trainingProgramDocument.setFileSize(file.getSize());
            trainingProgramDocument.setLastModifiedAt(LocalDateTime.now());
            trainingProgramDocumentRepository.save(trainingProgramDocument);
        }
    }
}
