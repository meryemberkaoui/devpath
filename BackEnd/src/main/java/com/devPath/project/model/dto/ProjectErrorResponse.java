package com.devPath.project.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectErrorResponse {

    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String path;
}
