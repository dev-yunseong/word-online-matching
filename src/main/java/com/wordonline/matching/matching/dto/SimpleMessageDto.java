package com.wordonline.matching.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleMessageDto {
    private final String type = "message";
    private String message;
}
