package org.example.t2testtask.service;

import lombok.RequiredArgsConstructor;
import org.example.t2testtask.entity.Award;
import org.example.t2testtask.entity.Employee;
import org.example.t2testtask.entity.EmployeeAward;
import org.example.t2testtask.repository.EmployeeAwardRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeAwardService {
    private final EmployeeAwardRepository employeeAwardRepository;

    public void save(Employee employee, Award award){
        EmployeeAward employeeAward = new EmployeeAward();
        employeeAward.setEmployee(employee);
        employeeAward.setAward(award);
        employeeAwardRepository.save(employeeAward);
    }
}
