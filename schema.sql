-- ============================================================
--  JobPortal – Master Database Setup Script
--  Copy and execute this script in MySQL Workbench (⚡ Execute)
-- ============================================================

DROP DATABASE IF EXISTS jobportal;
CREATE DATABASE jobportal;
USE jobportal;

-- 1. Users Table (Job Seekers / Candidates)
CREATE TABLE users (
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- 2. Admin Table (Company Representatives)
CREATE TABLE admin (
    email VARCHAR(150) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL DEFAULT 'Admin',
    company_name VARCHAR(180) NOT NULL DEFAULT 'General Company'
);

-- 3. Jobs Table
CREATE TABLE jobs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    company VARCHAR(180) NOT NULL,
    location VARCHAR(180) NOT NULL,
    salary VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Open',
    category VARCHAR(80) NOT NULL DEFAULT 'Software Development',
    experience_level VARCHAR(40) NOT NULL DEFAULT '0-2 years',
    job_type VARCHAR(40) NOT NULL DEFAULT 'Full-Time',
    work_mode VARCHAR(40) NOT NULL DEFAULT 'On-site',
    required_skills VARCHAR(255) NOT NULL DEFAULT '',
    created_by_admin VARCHAR(150)
);

-- 4. User Profiles Table (Candidate Personal Details)
CREATE TABLE user_profiles (
    user_email VARCHAR(150) PRIMARY KEY,
    gender VARCHAR(30),
    phone VARCHAR(30) NOT NULL DEFAULT '',
    profile_picture VARCHAR(255),
    dob VARCHAR(30),
    address TEXT,
    skills TEXT,
    experience TEXT,
    education TEXT,
    preferred_role VARCHAR(150),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- 5. User Resumes Table (Custom-labeled Candidate Resumes)
CREATE TABLE user_resumes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(150) NOT NULL,
    custom_name VARCHAR(150) NOT NULL DEFAULT 'My Resume',
    file_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_user FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- 6. Applications Table
CREATE TABLE applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(150) NOT NULL,
    job_id INT NOT NULL,
    resume VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'Submitted',
    applied_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_applicant FOREIGN KEY (user_email) REFERENCES users(email),
    CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    UNIQUE KEY uq_application (user_email, job_id)
);

-- 7. Notifications Table (In-App Notifications)
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(150) NOT NULL,
    recipient_role VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Initial Sample Admins & Scoped Job Data
-- ============================================================

INSERT INTO admin (email, password, name, company_name) VALUES
('google@gmail.com', 'google123', 'Sumanth', 'Google'),
('infosys@gmail.com', 'infosys123', 'Rajesh Sharma', 'Infosys'),
('wipro@gmail.com', 'wipro123', 'Priya Patel', 'Wipro'),
('tcs@gmail.com', 'tcs123', 'Amit Kumar', 'TCS');

INSERT INTO jobs (title, company, location, salary, description, status, category, experience_level, job_type, work_mode, required_skills, created_by_admin) VALUES
('Senior Java Developer', 'Google', 'Bengaluru, Karnataka', '25-35 LPA',
 'Job Summary: Build and scale microservices for Google Cloud. | Company Details: Google Cloud Infrastructure. | Eligibility Criteria: B.E./B.Tech in CS/IT | Required Skills: Java; Spring Boot; Microservices; SQL; Kubernetes | Experience Required: 3+ Years',
 'Open', 'Software Development', '3+ Years', 'Full-Time', 'Hybrid', 'Java, Spring Boot, SQL, Microservices', 'google@gmail.com'),

('Backend Software Engineer', 'Google', 'Hyderabad, Telangana', '20-30 LPA',
 'Job Summary: High throughput REST API and distributed services. | Company Details: Google Payments. | Eligibility Criteria: B.E./B.Tech in CS | Required Skills: Java; Python; SQL; Redis; Docker | Experience Required: 1-3 Years',
 'Open', 'Software Development', '1-3 Years', 'Full-Time', 'Remote', 'Java, Python, SQL, Docker', 'google@gmail.com'),

('Senior Java Developer', 'Infosys', 'Bengaluru, Karnataka', '18-24 LPA',
 'Job Summary: Build enterprise Java applications for banking clients. | Company Details: Infosys Financial Systems. | Eligibility Criteria: B.E./B.Tech/MCA | Required Skills: Java 17; Spring Boot; Hibernate; SQL | Experience Required: 3+ Years',
 'Open', 'Software Development', '3+ Years', 'Full-Time', 'Hybrid', 'Java, Spring Boot, Hibernate, SQL', 'infosys@gmail.com'),

('QA Automation Specialist', 'Infosys', 'Pune, Maharashtra', '8-12 LPA',
 'Job Summary: Test automation framework creation and API testing. | Company Details: Infosys Quality Engineering. | Eligibility Criteria: B.E./B.Tech in CS/ECE | Required Skills: Selenium; Java; TestNG; Postman; JIRA | Experience Required: 1-3 Years',
 'Open', 'QA & Testing', '1-3 Years', 'Full-Time', 'On-site', 'Selenium, Java, TestNG, JIRA', 'infosys@gmail.com'),

('React Frontend Developer', 'Wipro', 'Hyderabad, Telangana', '12-18 LPA',
 'Job Summary: Craft modern responsive web applications using React. | Company Details: Wipro Digital Practice. | Eligibility Criteria: B.Sc/B.E. in CS | Required Skills: React; TypeScript; CSS3; HTML5; Redux | Experience Required: 1-3 Years',
 'Open', 'Software Development', '1-3 Years', 'Full-Time', 'Remote', 'React, TypeScript, CSS, Redux', 'wipro@gmail.com'),

('Full Stack Developer', 'TCS', 'Mumbai, Maharashtra', '14-20 LPA',
 'Job Summary: End-to-end full stack web application development. | Company Details: TCS Digital Transformation. | Eligibility Criteria: B.E./MCA | Required Skills: React; Java; Spring Boot; MySQL; Node.js | Experience Required: 3+ Years',
 'Open', 'Software Development', '3+ Years', 'Full-Time', 'Hybrid', 'React, Java, Spring Boot, MySQL', 'tcs@gmail.com');
