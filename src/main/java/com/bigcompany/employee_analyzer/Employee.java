package com.bigcompany.employee_analyzer;

class Employee {
    int id;
    String firstName;
    String lastName;
    int salary;
    Integer managerId;

    Employee(int id, String firstName, String lastName, int salary, Integer managerId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.managerId = managerId;
    }
}
