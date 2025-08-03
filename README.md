# ACME Medical RESTful API System

CST8277 Java Enterprise Project – Summer 2025  
Developed using Jakarta EE, Payara Server, JPA, and JAX-RS.

# Group Members and Task Distribution
| Member          | Contributions                                                                                                                           |  Average Peer Grade (Grade Provided by Group #27) |
| --------------- |-----------------------------------------------------------------------------------------------------------------------------------------| ------------------------------------------------|
| Ruchen Ding     | Completed T1 (Entity Class JPA Mapping), T2 (User Authentication Logic Implementation), T3 (Establish User-Physician Relationship)      |             100 %                               | 
| Ryan Xu         | Completed T4 (REST API Implementation), T5 (Security Annotations), T7 (Postman Validation)                                              |             100 %                  	      | 
| Yizhen Xu       | Completed T6 (JUnit Testing), T8 (Documentation and Evaluation)                                                                         |             100%				      | 


---

## 1. Overview

This project models the medical training and certification system of ACME Medical Corp.  
It exposes RESTful APIs for managing Physicians, Patients, Medical Certificates, Prescriptions, Medicines, and more.

---

## 2. Technologies Used

- Java 21.0.1 2023-10-17 LTS
- Jakarta EE 10
- Payara Server 6.2024.4
- JPA / Hibernate
- MySQL 8.0.42
- Postman for Windows 11.55.1
- Eclipse IDE (with Maven support) 2025-03 (4.35.0)
- JUnit 5 + Maven Surefire

---

## 3. Setup & Deployment Instructions

### 3.1. Prerequisites

- Java JDK 17+
- Payara Server 6.2024+
- MySQL Server (or local MySQL Workbench)
- Eclipse IDE with Maven plugin
- Postman

### 3.2. Database Setup

The database schema undergoes a "drop-and-create" operation each time the application is deployed or restarted on Payara Server.

Required in persistence.xml by this property:
<property name="jakarta.persistence.schema-generation.database.action" value="drop-and-create"/>

The three SQL were provided in the project skeleton; don't modify them:
acmemedical-create.sql
acmemedical-data.sql
acmemedical-drop.sql

### 3.3. Payara Configuration
1. Run "localhost:4848" in browser
2. JDBC -> JDBC Connection Pools, create a new pool named "acmemedicalPool"
	- Resource Type: javax.sql.DataSource
	- Datasource Classname: com.mysql.cj.jdbc.MysqlDataSource
	- Additional Properties:
		- password: 8277
		- databaseName: acmemedical
		- serverName: 127.0.0.1
		- user: cst8277
		- networkProtocal: tcp
		- portNumber:3306
		- url: jdbc:mysql://localhost:3306/acmemedical?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

3. Save
4. Ping test
![Payara Ping](src/main/resources/PayaraPing.png)


### 3.4. Security Roles
| Role         | Username  | Password |
| ------------ | --------- | -------- |
| `ADMIN_ROLE` | `admin`   | `admin`  |
| `USER_ROLE`  | `cst8277` | `8277`   |


### 3.5. REST Endpoints
Base URL: http://localhost:8080/rest-acmemedical/api/v1/

## 4 Testing with Postman
### 4.1 Steps
1. Import the REST-ACMEMedical-Sample.postman_collection.json file in POSTMAN.

2. Basic Auth can be changed during testing
	- Admin: admin:admin
	- User: cst8277:8277

3. Ensure correct port and endpoint paths


### 4.2 Special Notes
1. MedicalTraining Entity**: embedded "DurationAndStatus"
2.  POST of Prescription**:  The `Prescription` entity uses a composite primary key** (Physician + Patient).
By default, the database contains:
- Physician ID = 1
- Patient IDs = 1 and 2
- Prescriptions for combinations (1,1) and (1,2)
Any attempt to create duplicate combinations will result in a primary key violation.

To ensure POST of Prescription testing succeeds, our tests first create a new Physician (`ID = 2`),  
then use the combination `Physician = 2`, `Patient = 1` for the new Prescription.
Please run the requests **in order**, starting with the `POST Physician` request.

3.  POST of MedicalCertificate**: The MedicalCertificate POST API implements business validation to prevent duplicate certificates for the same physician-training combination. Due to existing seed data, POST requests typically return 409 Conflict with a JSON error message, which is the expected and correct behavior. This demonstrates proper data integrity protection and structured error handling.

##JUNIT Test
ACME Medical System - Testing Guide
## Overview
This document provides a comprehensive guide for the testing implementation of the ACME Medical System REST API. The testing suite consists of multiple test layers that validate entity functionality, REST API endpoints, business logic, and system integration.
Test Suite Architecture
Test Classes Structure
acmemedical/
├── EntityTests.java           - Entity layer unit tests (24 tests)
├── ServiceTest.java           - Service layer integration tests (10 tests)  
├── ResourceTest.java          - REST API endpoint tests (56 tests)
└── TestACMEMedicalSystem.java - System integration test (1 test)
Total Test Count: 91 tests covering all major functionality


## Test Categories
### 1. Entity Layer Tests (EntityTests.java)
Purpose: Validates entity classes, relationships, and JPA annotations
Test Count: 24 tests
Coverage:

--Entity creation and property validation
--Inheritance relationships (PublicSchool, PrivateSchool)
--Embedded objects (DurationAndStatus)
--Composite primary keys (PrescriptionPK)
--Bidirectional relationships
--Equals and hashCode implementations

### Key Test Methods:

---test01_PhysicianEntityCreation() - Basic entity creation
---test04_PublicSchoolInheritance() - Inheritance validation
---test06_DurationAndStatusEmbeddable() - Embedded object testing
---test09_PrescriptionPK_Creation() - Composite key validation

### 2. Service Layer Tests (ServiceTest.java)
Purpose: Direct testing of business logic using EntityManager
Test Count: 10 tests
Approach: Uses JPA EntityManager directly for persistence operations
Coverage:
---CRUD operations on all major entities
---Business logic validation
---Database transaction handling
---Direct persistence layer testing

### Key Test Methods:

---test01_GetAllPhysicians_notEmpty() - Retrieve operations
---test02_PersistPhysician_createsNew() - Create operations
---test04_UpdatePatientById_changesFields() - Update operations
---test10_DeletePhysicianById() - Delete operations

### 3. Resource Layer Tests (ResourceTest.java)
Purpose: REST API endpoint and security testing
Test Count: 56 tests
Coverage:

---HTTP method validation (GET, POST, PUT, DELETE)
---Security role testing (ADMIN_ROLE, USER_ROLE)
---Error handling and status codes
---Content negotiation
---Authentication and authorization

## Security Test Categories:

---Admin Role Tests: Full CRUD access validation
---User Role Tests: Limited access validation
---Unauthorized Tests: 401 error validation
---Forbidden Tests: 403 error validation

## Key Test Methods:

---test01_PhysicianResource_GetAll_AdminRole_Success() - Admin access
---test02_PhysicianResource_GetAll_UserRole_Forbidden() - User restrictions
---test46_UnauthorizedAccess_Returns401() - Security validation
---test49_InvalidJSON_Returns400() - Error handling

## 4. System Integration Test (TestACMEMedicalSystem.java)
Purpose: End-to-end system validation
Test Count: 1 test
Coverage:

---Complete system functionality
---Integration between all layers
---Final validation of system health

### Testing Technologies Used
Frameworks and Libraries

---JUnit 5: Primary testing framework
---Hamcrest: Assertion library for readable test validation
---JAX-RS Client API: REST endpoint testing
---JPA/EntityManager: Direct database testing
---Jersey Client: HTTP client for API testing

### Test Annotations

---@TestMethodOrder(MethodOrderer.OrderAnnotation.class) - Ensures test execution order
---@Order(n) - Specifies individual test execution sequence
---@BeforeAll / @BeforeEach - Test setup methods
---@AfterAll / @AfterEach - Test cleanup methods

### Authentication and Security Testing
## Test Users
Admin User: admin / admin
Regular User: cst8277 / 8277
Security Roles Tested

---ADMIN_ROLE: Full system access
---USER_ROLE: Limited access based on ownership
---No Authentication: 401 Unauthorized responses
---Insufficient Permissions: 403 Forbidden responses

### Security Test Scenarios

---Admin Access: Validates full CRUD operations
---User Restrictions: Ensures users can only access owned resources
---Authentication Required: Tests endpoints requiring login
---Permission Enforcement: Validates role-based access control

## Test Data Management
### Entity Relationships Tested

---Physician ↔ MedicalCertificate (One-to-Many)
---Physician ↔ Prescription (One-to-Many)
---Patient ↔ Prescription (One-to-Many)
---Medicine ↔ Prescription (One-to-Many)
---MedicalSchool ↔ MedicalTraining (One-to-Many)
---MedicalTraining ↔ MedicalCertificate (One-to-Many)

### Composite Key Testing
The Prescription entity uses a composite primary key consisting of:

---physicianId (Integer)
---patientId (Integer)

Tests validate proper creation, equality, and hash code implementation.

## Error Handling Coverage
### HTTP Status Codes Tested

---200 OK: Successful operations
---201 Created: Successful creation
---400 Bad Request: Invalid data/JSON
---401 Unauthorized: Missing authentication
---403 Forbidden: Insufficient permissions
---404 Not Found: Resource not found
---405 Method Not Allowed: Unsupported HTTP methods
---409 Conflict: Duplicate resource creation
---415 Unsupported Media Type: Invalid content type

### Test Execution Requirements
Prerequisites

---Payara Server: Must be running on localhost:8080
---Database: MySQL/PostgreSQL with ACME Medical schema
---Application Deployment: REST API deployed and accessible
---Test Data: Basic seed data for entities

### Execution Order
The test suite is designed to run in a specific order to ensure data dependencies:

---Entity Tests (independent unit tests)
---Service Tests (creates initial data)
---Resource Tests (tests API with created data)
---Integration Test (validates complete system)

## Maven Surefire Integration
Tests can be executed using Maven Surefire plugin:
bashmvn clean install test surefire-report:report site -DgenerateReports=true
This generates a comprehensive test report at: target/site/surefire-report.html
### Test Coverage Summary
### Entity Coverage
- All 7 main entities tested
- Inheritance relationships validated
- Composite keys verified
- Embedded objects tested
### API Endpoint Coverage
- All CRUD operations tested
- Security roles validated
- Error scenarios covered
- Content negotiation verified
### Business Logic Coverage
- Service layer operations tested
- Transaction handling validated
- Data persistence verified
- Relationship management tested


## Key Testing Patterns
### 1. Arrange-Act-Assert Pattern
All tests follow the standard testing pattern:

Arrange: Set up test data and preconditions
Act: Execute the operation being tested
Assert: Verify the expected outcomes

### 2. Security Testing Pattern
Security tests validate both positive and negative scenarios:

Authorized access succeeds with expected data
Unauthorized access fails with appropriate error codes

### 3. Error Handling Pattern
Error tests ensure proper system behavior:

Invalid inputs return appropriate error codes
Error responses include meaningful messages
System remains stable after errors

## Contributing to Tests
Adding New Tests
When adding new functionality, ensure:

Entity tests for any new entities or relationships
Service tests for new business logic
Resource tests for new API endpoints
Security tests for protected operations

### Test Naming Convention

Use descriptive method names: test01_EntityOperation_Scenario_ExpectedResult()
Include order numbers for execution sequence
Clearly indicate the test purpose and expected outcome

## Assertion Guidelines

Use Hamcrest matchers for readability
Include descriptive assertion messages
Test both positive and negative scenarios
Validate all relevant response attributes

Test Report Interpretation
Surefire Report Sections

Summary: Overall pass/fail statistics
Package Results: Results organized by package
Test Cases: Individual test results with timing
Failure Details: Complete stack traces for failed tests

## Success Criteria

All critical path tests must pass
Security tests must demonstrate proper access control
Error handling tests must show appropriate responses
Performance should be reasonable for test environment


This testing guide ensures comprehensive validation of the ACME Medical System across all architectural layers while maintaining high code quality and security standards.
