package com.example.demo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.model.EmployeeModel;
import com.example.demo.repository.EmployeeRepository;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    public List<EmployeeModel> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public void exportToFile() throws IOException {
        List<EmployeeModel> employees = getAllEmployees();
        
        // 파일 경로 설정 및 디렉토리 생성
        String filePath = System.getProperty("user.home") + "/employees.csv";
        File file = new File(filePath);
        
        // 파일 경로 로깅
        System.out.println("파일 생성 경로: " + filePath);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // CSV 헤더 작성
            writer.write("ID,이름,이메일,부서\n");
            
            // 직원 데이터 작성
            for (EmployeeModel employee : employees) {
                writer.write(String.format("%d,%s,%s,%s%n",
                    employee.getId(),
                    employee.getName(),
                    employee.getEmail(),
                    employee.getDepartment()
                ));
            }
            System.out.println("파일 생성 완료: " + filePath);
        } catch (IOException e) {
            System.err.println("파일 생성 중 오류 발생: " + e.getMessage());
            System.err.println("파일 경로: " + filePath);
            throw e;
        }
    }
    
    public void exportSqlToCSV(String sqlFilePath, String csvFilePath) throws IOException {
        if (csvFilePath == null || csvFilePath.trim().isEmpty()) {
            csvFilePath = System.getProperty("user.home") + "/employees.csv";

        }

        log.info("SQL 파일 경로: {}", sqlFilePath);
        log.info("CSV 파일 경로: {}", csvFilePath);
        
        List<String> sqlQueries = readSqlFile(sqlFilePath);
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            // SQL 실행 및 결과 저장
            for (String sql : sqlQueries) {
                if (!sql.trim().isEmpty()) {
                    List<Map<String, Object>> queryResult = executeSQL(sql);
                    results.addAll(queryResult);
                }
            }

            // CSV 파일로 저장
            writeToCsv(results, csvFilePath);

        } catch (Exception e) {
            System.err.println("SQL 실행 중 오류 발생: " + e.getMessage());
            throw new IOException("SQL 처리 중 오류 발생", e);
        }
    }

    private List<String> readSqlFile(String sqlFilePath) throws IOException {
        log.info("readSqlFile");
        List<String> queries = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath));
            StringBuilder queryBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("line:{}",line);
                // 주석 제거
                if (line == null || line.trim().isEmpty() || line.trim().startsWith("--")) {
                    continue;
                }
                
                queryBuilder.append(line).append(" ");
                if (line.trim().endsWith(";")) {
                    log.info("queryBuilder:{}",queryBuilder.toString().trim().substring(0, queryBuilder.toString().length() - 1));
                    queries.add(queryBuilder.toString().trim().substring(0, queryBuilder.toString().length() - 1));
                    queryBuilder = new StringBuilder();
                }
            }
        }catch(Exception e){
            throw new IOException("SQL 파일읽기 오류");
        }
        log.info("readSqlFile:{}", queries);
        return queries;
    }

    private List<Map<String, Object>> executeSQL(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    private void writeToCsv(List<Map<String, Object>> results, String csvFilePath) throws IOException {
        if (results.isEmpty()) {
            throw new IOException("결과 데이터가 없습니다.");
        }

        File file = new File(csvFilePath);
        File directory = file.getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // 헤더 작성
            Set<String> headers = results.get(0).keySet();
            writer.writeNext(headers.toArray(new String[0]));

            // 데이터 작성
            for (Map<String, Object> row : results) {
                String[] csvRow = headers.stream()
                    .map(header -> row.get(header))
                    .map(value -> value != null ? value.toString() : "")
                    .toArray(String[]::new);
                writer.writeNext(csvRow);
            }
        }
    }
} 