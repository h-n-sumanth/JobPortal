# Job Portal Web Application

A Java-based Job Portal web application that allows users to register, log in, search and apply for jobs, upload resumes, manage their profiles, and track their applications. Administrators can manage jobs, view applicants, and update application statuses.

## 🚀 Features

### 👤 User Features
- User registration and login
- User dashboard
- Search jobs by title, company, and location
- View available jobs
- Apply for jobs
- Upload resume while applying
- View submitted applications
- Download uploaded resumes
- Manage user profile
- Logout functionality
- Application status tracking
- Notifications

### 🔐 Admin Features
- Admin registration and login
- Admin dashboard
- Add new jobs
- View and manage jobs
- Edit job details
- Delete jobs
- View applicants
- Download applicant resumes
- Update application status
- Delete applications
- Admin logout

### 🔗 REST API
The project also contains REST API functionality for handling job portal data.

## 🛠️ Technologies Used

- Java
- Java Servlets
- JSP
- HTML5
- CSS3
- JavaScript
- Bootstrap
- MySQL
- JDBC
- Apache Tomcat
- Maven
- REST API
- JSON

## 🏗️ Project Architecture

```text
JobPortal
│
├── src
│   └── main
│       ├── java
│       │   └── com.jobportal
│       │       ├── AddJobServlet.java
│       │       ├── AdminLoginServlet.java
│       │       ├── AdminRegisterServlet.java
│       │       ├── ApplyJobServlet.java
│       │       ├── CurrentAdminServlet.java
│       │       ├── DBConnection.java
│       │       ├── DeleteApplicationServlet.java
│       │       ├── DeleteJobServlet.java
│       │       ├── NotificationServlet.java
│       │       ├── ProfileServlet.java
│       │       ├── ResumeServlet.java
│       │       ├── UpdateApplicationStatusServlet.java
│       │       ├── UpdateJobServlet.java
│       │       ├── ViewApplicantsServlet.java
│       │       ├── ViewJobsServlet.java
│       │       └── api
│       │           ├── ApiResponseUtil.java
│       │           └── RestApiServlet.java
│       │
│       └── webapp
│           ├── css
│           ├── html
│           ├── js
│           └── WEB-INF
│               └── web.xml
│
├── schema.sql
├── pom.xml
└── .gitignore