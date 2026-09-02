package com.ecopickup.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String,Object>> notFound(NoSuchElementException ex){return error(HttpStatus.NOT_FOUND,ex.getMessage());}
    @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class})
    ResponseEntity<Map<String,Object>> badRequest(RuntimeException ex){return error(HttpStatus.BAD_REQUEST,ex.getMessage());}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex){
        Map<String,String> fields=new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(e->fields.put(e.getField(),e.getDefaultMessage()));
        Map<String,Object> body=new LinkedHashMap<>(); body.put("timestamp",LocalDateTime.now());body.put("status",400);body.put("message","Validation failed");body.put("fields",fields);
        return ResponseEntity.badRequest().body(body);
    }
    private ResponseEntity<Map<String,Object>> error(HttpStatus status,String message){return ResponseEntity.status(status).body(Map.of("timestamp",LocalDateTime.now(),"status",status.value(),"message",message));}
}
