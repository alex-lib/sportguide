package com.sport.service.entities.training_program;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "training_program_documents")
public class TrainingProgramDocument {
    @Id
    private String id;
    private byte[] content;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private LocalDateTime lastModifiedAt;
}