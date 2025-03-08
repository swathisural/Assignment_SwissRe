package com.bigcompany.employee_analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EmployeeAnalysis {
	public static void main(String[] args) {
		// Path to your CSV file
		String filePath = "C:\\Users\\Swathi\\Desktop\\swissRe\\employees.csv";
		Map<Integer, Employee> employees = new HashMap<Integer, Employee>();
		Map<Integer, List<Employee>> managerToDirectReportees = new HashMap<Integer, List<Employee>>();
		//List to salary checks
		List<String> salaryResults = new ArrayList();
		//List to hold reporting chain checks
		List<String> reportingChainResults = new ArrayList();
		// Read all employees from CSV
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
			br.readLine();
			String line;
			while ((line = br.readLine()) != null) {
				String[] employeerows = line.split(",");
				int id = Integer.parseInt(employeerows[0]);
				String firstName = employeerows[1];
				String lastName = employeerows[2];
				int salary = Integer.parseInt(employeerows[3]);
				Integer managerId = employeerows.length > 4 && !employeerows[4].isEmpty()
						? Integer.parseInt(employeerows[4])
						: null;

				Employee employee = new Employee(id, firstName, lastName, salary, managerId);
				employees.put(id, employee);

				if (managerId != null) {
					if (!managerToDirectReportees.containsKey(managerId)) {
						managerToDirectReportees.put(managerId, new ArrayList<Employee>());
					}
					managerToDirectReportees.get(managerId).add(employee);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			// return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		analyzeManagerSalaries(employees, managerToDirectReportees, salaryResults);

		findLongReportingLine(employees, reportingChainResults);
	}

	static void analyzeManagerSalaries(Map<Integer, Employee> employees,
			Map<Integer, List<Employee>> managerToSubordinates, List<String> salaryResults) {
		for (Map.Entry<Integer, List<Employee>> entry : managerToSubordinates.entrySet()) {
			int managerId = entry.getKey();
			Employee manager = employees.get(managerId);
			List<Employee> directReportees = entry.getValue();

			// Calculate average salary of directReportees
			double avgDirectReporteeSalary = directReportees.stream().mapToInt(e -> e.salary).average().orElse(0);
			double minRequiredSalary = avgDirectReporteeSalary * 1.2;
			double maxAllowedSalary = avgDirectReporteeSalary * 1.5;

			if (manager.salary < minRequiredSalary) {
				salaryResults.add(manager.firstName + " " + manager.lastName + " earns "
						+ (minRequiredSalary - manager.salary) + " less than required.");
			} else if (manager.salary > maxAllowedSalary) {
				salaryResults.add(manager.firstName + " " + manager.lastName + " earns "
						+ (manager.salary - maxAllowedSalary) + " more than allowed.");
			}
		}
		salaryResults.stream().forEach(System.out::println);

	}

	static void findLongReportingLine(Map<Integer, Employee> employees, List<String> reportingChainResults) {
		Map<Integer, Integer> depths = new HashMap<>();

		// Find the CEO (the employee with no manager)
		Employee ceo = null;
		for (Employee e : employees.values()) {
			if (e.managerId == null) {
				ceo = e;
				break;
			}
		}

		if (ceo == null) {
			reportingChainResults.add("No CEO found in data.");
			return;
		}

		calculateDepth(ceo, employees, depths, 0);

		for (Map.Entry<Integer, Integer> entry : depths.entrySet()) {
			if (entry.getValue() > 4) {
				Employee e = employees.get(entry.getKey());
				reportingChainResults.add(
						e.firstName + " " + e.lastName + " has a reporting line too long by " + (entry.getValue() - 4));
			}
		}

		reportingChainResults.stream().forEach(System.out::println);
	}

	private static void calculateDepth(Employee employee, Map<Integer, Employee> employees,
			Map<Integer, Integer> depths, int depth) {
		depths.put(employee.id, depth);
		for (Employee e : employees.values()) {
			if (e.managerId != null && e.managerId.equals(employee.id)) {
				calculateDepth(e, employees, depths, depth + 1);
			}
		}
	}

}
