package com.sport.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private String message;
    @ToString.Exclude
    private byte[] photo;
    private int step = 0;
}