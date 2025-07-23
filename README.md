# ACME Medical RESTful API System

CST8277 Java Enterprise Project – Summer 2025  
Developed using Jakarta EE, Payara Server, JPA, and JAX-RS.

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

### 3.4. Security Roles
| Role         | Username  | Password |
| ------------ | --------- | -------- |
| `ADMIN_ROLE` | `admin`   | `admin`  |
| `USER_ROLE`  | `cst8277` | `8277`   |


### 3.5. REST Endpoints
Base URL: http://localhost:8080/rest-acmemedical/api/v1/

## 4 Testing with Postman
### 4.1 Steps
1. Import the REST-ACMEMedical-Sample.postman_collection.json file.

2. Use Basic Auth:
	- Admin: admin:admin
	- User: cst8277:8277

3. Ensure correct port and endpoint paths (adjust localhost:8080 if needed)

4. Try:
	- Adding new MedicalCertificate
	- Attempting duplicate insertion (should return 409 + JSON message)
	- Secured endpoint access test
5. POSTMAN

### 4.2 REST Endpoints Testing Summary
#### MedicalSchool `/medicalschool`
| Method | Endpoint | Status | Notes | Source |
|--------|----------|--------|-------|--------|
| GET | `/{id}` | ✅ | Returns JSON "404 Not Found" for non-existent ID | Skeleton |
| GET |  | ✅ | Returns "401 Unauthorized" for invalid credentials | Skeleton |
| POST |  | ✅ | Duplicate submissions will be rejected, with a 409 JSON response. | Skeleton |
| POST |`/{medicalSchoolId}/medicaltraining`| ✅ | Can submit multiple times, must embedded format | Skeleton |
| PUT | `/{id}` | ✅ | Returns JSON "404 Not Found" for non-existent ID | Skeleton |
| DELETE | `/{id}` | ✅ | CReturns JSON "404 Not Found" for non-existent ID | Skeleton |

#### Physician `/physician`
| Method | Endpoint | Status | Notes | Source |
|--------|----------|--------|-------|--------|
| GET | `//{id}` | ✅ | Returns JSON "404 Not Found" for non-existent ID | Skeleton |
| GET || ✅ | Returns "401 Unauthorized" for invalid credentials | Skeleton |
| POST |  | ✅ | Returns "409 Conflict" on duplicate submission but still creates new record | Skeleton |
| PUT | `/physicians/{physicianId}/patient/{patientId}/medicine` | ✅ | Has transient fields set to NULL | Skeleton |
| PUT | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| DELETE | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |

#### Medicine `/medicine`
| Method | Endpoint | Status | Notes | Source |
|--------|----------|--------|-------|--------| Added |
| GET | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| GET | | ✅ | Returns all medicines | Added |
| POST | | ✅ | Multiple submissions create multiple records | Added |
| PUT | `/medicine/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| DELETE | `/medicine/{id}` | ✅ | Returns JSON error for non-existent ID | Added |

#### Patient `/patient`
| Method | Endpoint | Status | Notes | Source |
|--------|----------|--------|-------|--------|
| GET | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| GET |  | ✅ | Returns all patients | Added |
| POST |  | ✅ | Multiple submissions create multiple records | Added |
| PUT | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| DELETE | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |

#### Prescription `/prescription`
| Method | Endpoint | Status | Notes | Source |
|--------|----------|--------|-------|--------|
| GET | `/{patientId}/{medicineId}` | ✅ | Returns JSON error for non-existent composite key | Added |
| GET |  | ✅ | Returns all prescriptions | Added |
| POST |  | ✅ | Returns JSON error on repeat submission | Added |
| PUT | `/prescriptions/{patientId}/{medicineId}` | ✅ | Returns JSON error for non-existent composite key | Added |
| DELETE | `/prescriptions/{patientId}/{medicineId}` | ✅ | Returns JSON error for non-existent composite key | Added |

#### MedicalTraining `/medicaltraining`
| Method | Endpoint | Status | Notes | Source |
|--------|----------|--------|-------|--------|
| GET | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| GET |  | ✅ | Returns all medical trainings | Added |
| POST |  | ✅ | Multiple submissions create multiple records | Added |
| PUT | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| DELETE | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |


#### MedicalCertificate `/medicalcertificate`
| Method | Endpoint | Status | Notes | Source |
|--------|----------|--------|-------|--------|
| GET | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| GET |  | ✅ | Returns "401 Unauthorized" for invalid credentials | Added |
| POST |  | ✅ | Prevents duplicate physician-training combinations | Added |
| PUT | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |
| DELETE | `/{id}` | ✅ | Returns JSON error for non-existent ID | Added |

## Legend
- ✅ **Completed**: All tests passed as expected
- ⚠️ **Warning**: Functionality works but has noted issues
- ❌ **Failed**: Tests failed or not working as expected

## Special Notes
1. **Prescription Entity**: Uses composite primary key (patientId + medicineId), embedded "PrescriptionPK"
2. **MedicalTraining Entity**: embedded "DurationAndStatus"


## JUnit Testing (50+ tests required)
Run the following Maven goal in Eclipse:
```
clean install test surefire-report:report site -DgenerateReports=true
```
Then open:
```
target/site/surefire-report.html
```
It will show a summary of all passed/failed tests.
