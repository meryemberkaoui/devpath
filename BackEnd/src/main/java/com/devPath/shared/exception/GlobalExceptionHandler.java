package com.devPath.shared.exception;

import com.devPath.project.exceptions.ProjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des validations @Valid -> HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Validation failed: " +
                ex.getBindingResult().getFieldErrors().stream()
                        .map(err -> err.getField() + " " + err.getDefaultMessage())
                        .reduce("", (s1, s2) -> s1 + "; " + s2));
    }

    // Gestion des projets non trouvés -> HTTP 404
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ProjectNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Gestion des autres RuntimeException -> HTTP 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}

