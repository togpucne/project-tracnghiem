# PT Quiz — Secure Online Examination System

A research and development project on RESTful API security, applied to an online multiple-choice examination platform combining a Web Application, a Desktop Application, and a centralized API backend.

**Live Demo:**
- Student portal: https://ptquizz.onrender.com/client/
- Admin / Lecturer portal: https://ptquizz.onrender.com/server/
- Desktop application (.exe): https://drive.google.com/drive/folders/11VJWaiHnWW4qAy2azdyf_3HjznmvD5CL?usp=sharing

## 1. Project Overview

PT Quiz is an academic project in the domain of Information Security. The system simulates a real-world online examination environment and serves as a research platform for studying secure API design, authentication mechanisms, and data protection techniques.

The architecture consists of three components — a PHP-based web client for students, a PHP-based admin portal for lecturers and administrators, and a Java desktop application — all communicating through a single secured RESTful API layer. The database is never accessed directly by any client; all data operations are mediated through the API.

## Test Accounts

The following accounts are available for testing different user roles:

| Role | Email | Password |
|---|---|---|
| Teacher | giangvien@gmail.com | Giangvien@123 |
| Admin | admin@gmail.com | Admin@123 |
| Student | nguyenvana@gmail.com | Nguyenvana@123 |
## 2. Objectives

- Study the structure and programming model of RESTful APIs.
- Analyze common security vulnerabilities targeting API-based systems.
- Apply information security techniques to a functional, end-to-end system.
- Build an examination platform covering both web and desktop environments.
- Ensure the system upholds the principles of confidentiality, integrity, and access control.

## 3. System Architecture

The system follows a Client–Server architecture with the API as the central security boundary.

```
[Web Client]         [Desktop Client]
      \                    /
       \                  /
        [RESTful API (PHP)]
               |
          [MySQL Database]
```

| Component | Technology | Role |
|---|---|---|
| Web Client | PHP, HTML, CSS, JavaScript, Bootstrap | Student examination interface |
| Admin Portal | PHP, HTML, CSS, JavaScript, Bootstrap | Exam and user management for lecturers and admins |
| Desktop Client | Java (Swing), NetBeans | Standalone exam-taking application |
| API Backend | PHP, JWT, PDO, Composer | Business logic, authentication, and security enforcement |
| Database | MySQL | Persistent storage for users, questions, exams, and results |

## 4. Technology Stack

**Backend (API Server)**
- PHP with a custom MVC structure
- JWT (JSON Web Token) for stateless authentication
- PDO with prepared statements for all database operations
- Composer for dependency management

**Web Frontend (Client and Admin)**
- PHP for server-side rendering and API communication
- Bootstrap, HTML5, CSS3, and vanilla JavaScript

**Desktop Application**
- Java with Swing for the graphical interface
- Compiled and packaged as a standalone `.exe` using NetBeans; no Java installation required on the target machine

**Infrastructure**
- XAMPP (Apache + MySQL) for local development
- Render.com for cloud hosting

## 5. Features

**Student Web Portal**
- User registration and login
- Browse and search available examinations
- Take multiple-choice exams with automatic scoring
- View personal exam history and results

**Admin and Lecturer Portal**
- Manage subjects, question banks, and exam papers
- Create and edit questions individually or via Word document import
- Manage user accounts and toggle account status
- View, filter, and export exam results to CSV
- Premium account management via SePay payment gateway integration

**Desktop Application**
- Authenticate against the live API using JWT
- Browse available exams and complete them in a desktop environment
- Submit results directly to the server upon completion

## 6. Security Implementation

The following security measures have been researched and applied across the system:

| Security Mechanism | Implementation Detail |
|---|---|
| Authentication | JWT-based stateless token authentication with expiry validation |
| Authorization | Role-based access control (admin, giangvien, thisinh) enforced per endpoint |
| Input Validation | Server-side sanitization of all user-supplied data to prevent XSS and injection |
| SQL Injection Prevention | PDO with parameterized queries used throughout the entire data layer |
| API Endpoint Protection | Apache `.htaccess` rules combined with `ApiSecurityValidator.php` middleware |
| Security Logging | `SecurityLogger.php` records suspicious requests and access violations |
| CSRF Protection | Anti-CSRF token generation and validation on all state-changing operations |
| Cache-Control Headers | Sensitive API responses served with `Cache-Control: no-store, no-cache` |
| Token Management | `TokenManager.php` handles JWT signing, verification, and revocation |

## 7. Project Structure

```
project-tracnghiem/
├── client/                        # Web Application — Student Portal
│   ├── api/                       # API call handlers
│   ├── core/                      # Core routing and utility classes
│   ├── public/                    # Static assets (CSS, JS, images)
│   ├── views/                     # Page templates
│   └── index.php                  # Entry point
│
├── server/                        # RESTful API Backend and Admin Portal
│   ├── api/                       # API endpoint handlers (41 endpoints)
│   ├── core/                      # Security core: JWT, Validator, Logger, TokenManager
│   ├── controller/                # Business logic controllers
│   ├── model/                     # Database model layer
│   ├── database/                  # Database connection
│   ├── routes/                    # API and page routing
│   └── index.php                  # Entry point
│
├── java/                          # Desktop Application
│   └── JavaGui-ThiTracNghiem-main/
│
├── sepay_webhook.php              # SePay payment gateway webhook handler
└── README.md
```

## 8. Getting Started

### Option A — Hosted Version

The system is deployed and accessible without any local setup.

| Access Point | URL | Role |
|---|---|---|
| Student Web Portal | https://ptquizz.onrender.com/client/ | thisinh |
| Admin / Lecturer Portal | https://ptquizz.onrender.com/server/ | giangvien, admin |
| Desktop Application | https://drive.google.com/drive/folders/11VJWaiHnWW4qAy2azdyf_3HjznmvD5CL?usp=sharing | thisinh |

Download the `.exe` file from the Google Drive link above, run it directly, and it will connect to the hosted API automatically.

### Option B — Local Setup

**Prerequisites:** XAMPP (Apache + MySQL), Composer, Java JDK 8+, NetBeans IDE

1. Place the project folder inside the XAMPP `htdocs` directory.
2. Import the database schema into MySQL via phpMyAdmin.
3. Install PHP dependencies by running `composer install` inside the `server/` directory.
4. Start Apache and MySQL from the XAMPP control panel.
5. Access the student portal at `http://localhost/project-tracnghiem/client/`.
6. Access the admin portal at `http://localhost/project-tracnghiem/server/`.
7. Open and run the Java project in NetBeans, or use the compiled `.exe` from Google Drive.

## 9. Authors

Developed as an academic research project in the field of Information Security, with a focus on secure API design and implementation practices.
