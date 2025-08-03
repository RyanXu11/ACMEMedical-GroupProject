# ACME Medical RESTful API System

CST8277 Java Enterprise Project – Summer 2025  
Developed using Jakarta EE, Payara Server, JPA, and JAX-RS.

# Group Members and Task Distribution
| Member          | Contributions  |  Average Peer Grade (Grade Provided by Group #27) |
| --------------- |----------------| -----------------------------------------|
| Ruchen Ding     | T1, T2, T3 and T9 |             100 %                     | 
| Ryan Xu         | T4, T5, T6 and T9 |             100 %                  	  | 
| Yizhen Xu       | T7, T8 and T9     |             100%				      | 

- T1: Finish the JPA Annotations for Entities
- T2: Finish Custom Authentication Mechanism
- T3: Relationship Between SecurityUser and Physician
- T4: Building REST API
- T5: Securing REST Endpoints
- T6: POSTMAN Based API Validation
- T7: Building JUnit Tests
- T8: Documentation
- T9: Evaluation

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

## 5 JUNIT Test
ACME Medical System - Testing Guide
### 5.1 Overview
This document provides a comprehensive guide for the testing implementation of the ACME Medical System REST API. The testing suite consists of multiple test layers that validate entity functionality, REST API endpoints, business logic, and system integration.
Test Suite Architecture
Test Classes Structure
acmemedical/
├── EntityTests.java           - Entity layer unit tests (24 tests) 
├── ServiceTest.java           - Service layer integration tests (10 tests)  
├── ResourceTest.java          - REST API endpoint tests (56 tests) 
└── TestACMEMedicalSystem.java - System integration test (1 test) 
Total Test Count: 91 tests covering all major functionality

#### 5.1.1 Test Execution Requirements
Prerequisites
- Payara Server: Must be running on localhost:8080
- Database: MySQL/PostgreSQL with ACME Medical schema
- Application Deployment: REST API deployed and accessible
- Test Data: Basic seed data for entities

#### 5.1.2 Execution Order
The test suite is designed to run in a specific order to ensure data dependencies:

---Entity Tests (independent unit tests)
---Service Tests (creates initial data)
---Resource Tests (tests API with created data)
---Integration Test (validates complete system)

### 5.2 Test Categories
#### 5.2.1. Entity Layer Tests (EntityTests.java)
Purpose: Validates entity classes, relationships, and JPA annotations
Test Count: 24 tests
Coverage:

- Entity creation and property validation 
- Inheritance relationships (PublicSchool, PrivateSchool) 
- Embedded objects (DurationAndStatus) 
- Composite primary keys (PrescriptionPK) 
- Bidirectional relationships 
- Equals and hashCode implementations 

#### 5.2.2 Service Layer Tests (ServiceTest.java)
Purpose: Direct testing of business logic using EntityManager
Test Count: 10 tests
Approach: Uses JPA EntityManager directly for persistence operations
Coverage:
- CRUD operations on all major entities
- Business logic validation
- Database transaction handling
- Direct persistence layer testing

##### 5.2.3 Resource Layer Tests (ResourceTest.java)
Purpose: REST API endpoint and security testing
Test Count: 56 tests
Coverage:
- HTTP method validation (GET, POST, PUT, DELETE)
- Security role testing (ADMIN_ROLE, USER_ROLE)
- Error handling and status codes
- Content negotiation
- Authentication and authorization

#### 5.2.4 Security Test Categories:
---Admin Role Tests: Full CRUD access validation
---User Role Tests: Limited access validation
---Unauthorized Tests: 401 error validation
---Forbidden Tests: 403 error validation

#### 5.2.5. System Integration Test (TestACMEMedicalSystem.java)
Purpose: End-to-end system validation
Test Count: 1 test
Coverage:
- Complete system functionality
- Integration between all layers
- Final validation of system health

### 5.3 Testing Technologies Used
Frameworks and Libraries

- JUnit 5: Primary testing framework
- Hamcrest: Assertion library for readable test validation
- JAX-RS Client API: REST endpoint testing
- JPA/EntityManager: Direct database testing
- Jersey Client: HTTP client for API testing

### 5.4 Test Annotations
- @TestMethodOrder(MethodOrderer.OrderAnnotation.class) - Ensures test execution order
- @Order(n) - Specifies individual test execution sequence
- @BeforeAll / @BeforeEach - Test setup methods
- @AfterAll / @AfterEach - Test cleanup methods

### 5.5 Error Handling Coverage
- 200 OK: Successful operations
- 201 Created: Successful creation
- 400 Bad Request: Invalid data/JSON
- 401 Unauthorized: Missing authentication
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 405 Method Not Allowed: Unsupported HTTP methods
- 409 Conflict: Duplicate resource creation
- 415 Unsupported Media Type: Invalid content type


### 5.6 Maven Surefire Integration
Tests can be executed using Maven Surefire plugin:
`bash: mvn clean install test surefire-report:report site -DgenerateReports=true`

This generates a comprehensive test report at: target/site/surefire-report.html


This testing guide ensures comprehensive validation of the ACME Medical System across all architectural layers while maintaining high code quality and security standards.
