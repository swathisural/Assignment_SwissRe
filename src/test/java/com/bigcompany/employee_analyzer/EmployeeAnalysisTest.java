package com.bigcompany.employee_analyzer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bigcompany.employee_analyzer.Employee;
import com.bigcompany.employee_analyzer.EmployeeAnalysis;

import java.util.*;

class EmployeeAnalysisTest {

	private Map<Integer, Employee> employees;
	private Map<Integer, List<Employee>> managerToSubordinates;
	private List<String> salaryAnalysisResults;
	private List<String> reportingLineResults;

	@BeforeEach
	void setUp() {
		employees = new HashMap<>();
		managerToSubordinates = new HashMap<>();
		salaryAnalysisResults = new ArrayList<>();
		reportingLineResults = new ArrayList<>();

		// Creating Employee Hierarchy (CEO -> Managers -> Employees)
		employees.put(123, new Employee(123, "Joe", "Doe", 60000, null)); // CEO
		employees.put(124, new Employee(124, "Martin", "Chekov", 45000, 123)); // Manager
		employees.put(125, new Employee(125, "Bob", "Ronstad", 47000, 123)); // Manager
		employees.put(300, new Employee(300, "Alice", "Hasacat", 50000, 124)); // Employee
		employees.put(305, new Employee(305, "Brett", "Hardleaf", 34000, 300)); // Employee
		employees.put(306, new Employee(306, "Karen", "Johnston", 34000, 305));
		employees.put(400, new Employee(400, "Charlie", "Jobs", 50000, 306));
		employees.put(405, new Employee(405, "Kim", "John", 40000, 400));

		// Setting up the manager-to-subordinates mapping
		managerToSubordinates.put(123, Arrays.asList(employees.get(124), employees.get(125))); // CEO -> Managers
		managerToSubordinates.put(124, Collections.singletonList(employees.get(300))); // Manager -> Employee
		managerToSubordinates.put(300, Collections.singletonList(employees.get(305))); // Employee -> Employee
		managerToSubordinates.put(400, Collections.singletonList(employees.get(405)));
	}

	@Test
	void testAnalyzeManagerSalariesForUnderpaidManagers() {
		EmployeeAnalysis.analyzeManagerSalaries(employees, managerToSubordinates, salaryAnalysisResults);

		// Expected: Martin (124) is underpaid
		assertTrue(salaryAnalysisResults.stream().anyMatch(msg -> msg.contains("Martin Chekov earns")));
	}

	@Test
	void testAnalyzeManagerSalariesForOverpaidManagers() {
		// Increasing a manager's salary to test overpayment case
		employees.get(124).salary = 90000;

		salaryAnalysisResults.clear();
		EmployeeAnalysis.analyzeManagerSalaries(employees, managerToSubordinates, salaryAnalysisResults);

		// Expected: Martin (124) is overpaid
		assertTrue(salaryAnalysisResults.stream().anyMatch(msg -> msg.contains("Martin Chekov earns")));
	}

	@Test
	void testAnalyzeManagerSalariesWhenSalaryIsWithinLimits() {
		// No manager should be flagged when all are within limits
		salaryAnalysisResults.clear();
		EmployeeAnalysis.analyzeManagerSalaries(employees, managerToSubordinates, salaryAnalysisResults);

		assertEquals(1, salaryAnalysisResults.size()); // Only Martin should be flagged for underpayment
	}

	@Test
	void testFindLongReportingLine_WhenEmployeesHaveDeepHierarchy_1() {
		salaryAnalysisResults.clear();

		EmployeeAnalysis.findLongReportingLine(employees, reportingLineResults);

		assertTrue(reportingLineResults.stream()
				.anyMatch(msg -> msg.contains("Charlie Jobs has a reporting line too long by 1")));
	}
	
	@Test
	void testFindLongReportingLine_WhenEmployeesHaveDeepHierarchy_2() {
		salaryAnalysisResults.clear();

		EmployeeAnalysis.findLongReportingLine(employees, reportingLineResults);

		assertTrue(reportingLineResults.stream()
				.anyMatch(msg -> msg.contains("Kim John has a reporting line too long by 2")));
	}


	@Test
	void testFindLongReportingLine_WhenNoCEOExists() {
		// Remove the CEO
		employees.remove(123);

		EmployeeAnalysis.findLongReportingLine(employees, reportingLineResults);

		assertTrue(reportingLineResults.contains("No CEO found in data."));
	}

}
