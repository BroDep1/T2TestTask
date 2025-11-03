package org.example.t2testtask.repository;

import org.example.t2testtask.entity.Employee;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
}
