-- Run once before starting the enhanced application.
CREATE DATABASE IF NOT EXISTS jobportal;
USE jobportal;

CREATE TABLE IF NOT EXISTS users (
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS admin (
    email VARCHAR(150) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS jobs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    company VARCHAR(180) NOT NULL,
    location VARCHAR(180) NOT NULL,
    salary VARCHAR(80) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Open',
    category VARCHAR(80) NOT NULL DEFAULT 'Software Development',
    experience_level VARCHAR(40) NOT NULL DEFAULT '0-2 years',
    job_type VARCHAR(40) NOT NULL DEFAULT 'Full-Time'
);

CREATE TABLE IF NOT EXISTS applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(150) NOT NULL,
    job_id INT NOT NULL,
    resume VARCHAR(255) NOT NULL,
    applied_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_applicant FOREIGN KEY (user_email) REFERENCES users(email),
    CONSTRAINT fk_application_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    UNIQUE KEY uq_application (user_email, job_id)
);

CREATE TABLE IF NOT EXISTS user_profiles (
    user_email VARCHAR(150) PRIMARY KEY,
    gender VARCHAR(30),
    phone VARCHAR(30) NOT NULL,
    profile_picture VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_resumes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(150) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_user FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- Safe for MySQL 8.0.29+; these make an existing installation compatible.
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS category VARCHAR(80) NOT NULL DEFAULT 'Software Development';
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS experience_level VARCHAR(40) NOT NULL DEFAULT '0-2 years';
ALTER TABLE jobs ADD COLUMN IF NOT EXISTS job_type VARCHAR(40) NOT NULL DEFAULT 'Full-Time';
ALTER TABLE applications ADD COLUMN IF NOT EXISTS applied_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
