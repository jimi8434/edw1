package com.example.demo.service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.EmployeeModel;
import com.example.demo.repository.EmployeeRepository;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    public List<EmployeeModel> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public void exportToFile() throws IOException {
        List<EmployeeModel> employees = getAllEmployees();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("employees.csv"))) {
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
        }
    }
} 