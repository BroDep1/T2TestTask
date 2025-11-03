package org.example.t2testtask.service;

import lombok.RequiredArgsConstructor;
import org.example.t2testtask.entity.Employee;
import org.example.t2testtask.exeption.ResourceNotFoundException;
import org.example.t2testtask.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public Employee findById(Long id){
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сотрудник с id %s не найден".formatted(id)));
    }

    public boolean existsById(Long id){
        return employeeRepository.existsById(id);
    }
}
