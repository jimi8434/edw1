package com.example.demo.controller;

import java.io.IOException;
import java.util.List;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.example.demo.model.EmployeeModel;
import com.example.demo.service.EmployeeService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @GetMapping
    public ResponseEntity<List<EmployeeModel>> getAllEmployees() {
        try {
            List<EmployeeModel> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("직원 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    
    @GetMapping("/export")
    public ResponseEntity<?> exportToFile() {
        try {
            employeeService.exportToFile();
            
            File exportedFile = new File("생성된 파일 경로");
            
            if (!exportedFile.exists()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 생성에 실패했습니다.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + exportedFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            Resource resource = new FileSystemResource(exportedFile);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
        } catch (IOException e) {
            log.error("파일 내보내기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("파일 생성 실패: " + e.getMessage());
        }
    }
    
    @GetMapping("/export/sql-to-csv")
    public ResponseEntity<Map<String, String>> exportSqlToCSV(
            @RequestParam String sqlFilePath,
            @RequestParam(required = false) String csvFilePath) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 파일 존재 여부 확인
            if (!new File(sqlFilePath).exists()) {
                response.put("status", "error");
                response.put("message", "SQL 파일을 찾을 수 없습니다: " + sqlFilePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            log.info("SQL 파일  여부: {}", new File(sqlFilePath).exists());
            employeeService.exportSqlToCSV(sqlFilePath, csvFilePath);
            
            response.put("status", "success");
            response.put("message", "SQL 파일이 CSV로 성공적으로 변환되었습니다.");
            response.put("sqlFilePath", sqlFilePath);
            response.put("csvFilePath", csvFilePath);
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
         } finally {
            log.info("SQL 파일 처리 완료: {}", sqlFilePath);
         }
    }
} 