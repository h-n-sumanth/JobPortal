-- For an existing NexJob database: run schema.sql first, then run this file once.
USE jobportal;

UPDATE jobs
SET category = CASE
    WHEN title IN ('Senior Java Developer', 'React Frontend Developer', 'Full Stack Developer (.NET)', 'Python Backend Developer') THEN 'Software Development'
    WHEN title = 'QA Automation Engineer' THEN 'QA & Testing'
    WHEN title IN ('Data Scientist', 'Machine Learning Engineer', 'Data Engineer') THEN 'Data & AI'
    WHEN title IN ('DevOps Engineer', 'Cloud Architect', 'Cybersecurity Analyst') THEN 'Cloud & DevOps'
    WHEN title IN ('UI/UX Designer', 'Product Manager', 'Technical Writer') THEN 'Design & Product'
    WHEN title = 'Business Analyst' THEN 'Business & Operations'
    WHEN title IN ('Android Developer', 'iOS Developer (Swift)') THEN 'Mobile Development'
    WHEN title IN ('SAP ABAP Consultant', 'Embedded Systems Engineer') THEN 'Enterprise Systems'
    ELSE category
END,
experience_level = CASE
    WHEN title IN ('Cloud Architect', 'Machine Learning Engineer') THEN '9+ years'
    WHEN title IN ('Senior Java Developer', 'Data Scientist', 'Full Stack Developer (.NET)', 'Data Engineer', 'Product Manager') THEN '6-8 years'
    WHEN title IN ('React Frontend Developer', 'DevOps Engineer', 'Python Backend Developer', 'QA Automation Engineer', 'SAP ABAP Consultant') THEN '3-5 years'
    ELSE experience_level
END;

-- Adds only missing roles; safe to run again.
INSERT INTO jobs (title, company, location, salary, description, status, category, experience_level, job_type)
SELECT n.title, n.company, n.location, n.salary, n.description, 'Open', n.category, n.experience_level, 'Full-Time'
FROM (
    SELECT 'Junior Java Developer' title, 'Mindtree' company, 'Bengaluru, Karnataka' location, '5-8 LPA' salary, 'Job Summary: Build Spring Boot services. | Required Skills: Java; Spring Boot; SQL; Git | Experience: 0-2 years' description, 'Software Development' category, '0-2 years' experience_level
    UNION ALL SELECT 'Frontend Engineer', 'Razorpay', 'Bengaluru, Karnataka', '14-20 LPA', 'Job Summary: Create payment experiences. | Required Skills: React; TypeScript; CSS; Testing Library | Experience: 3-5 years', 'Software Development', '3-5 years'
    UNION ALL SELECT 'Manual QA Analyst', 'Cognizant', 'Chennai, Tamil Nadu', '4-7 LPA', 'Job Summary: Validate web and mobile releases. | Required Skills: Manual Testing; JIRA; SQL; Postman | Experience: 0-2 years', 'QA & Testing', '0-2 years'
    UNION ALL SELECT 'SDET - API Automation', 'Paytm', 'Noida, Uttar Pradesh', '16-24 LPA', 'Job Summary: Build API automation suites. | Required Skills: Java; Rest Assured; Selenium; CI/CD | Experience: 3-5 years', 'QA & Testing', '3-5 years'
    UNION ALL SELECT 'Data Analyst', 'Meesho', 'Bengaluru, Karnataka', '7-12 LPA', 'Job Summary: Turn marketplace data into insights. | Required Skills: SQL; Excel; Power BI; Python | Experience: 0-2 years', 'Data & AI', '0-2 years'
    UNION ALL SELECT 'MLOps Engineer', 'Fractal Analytics', 'Mumbai, Maharashtra', '22-32 LPA', 'Job Summary: Operate ML platforms and delivery pipelines. | Required Skills: Python; MLflow; Docker; Kubernetes; AWS | Experience: 6-8 years', 'Data & AI', '6-8 years'
    UNION ALL SELECT 'AWS Cloud Engineer', 'LTI Mindtree', 'Pune, Maharashtra', '13-19 LPA', 'Job Summary: Build secure cloud infrastructure. | Required Skills: AWS; Terraform; Linux; Jenkins | Experience: 3-5 years', 'Cloud & DevOps', '3-5 years'
    UNION ALL SELECT 'Site Reliability Engineer', 'PhonePe', 'Bengaluru, Karnataka', '24-36 LPA', 'Job Summary: Improve availability for financial systems. | Required Skills: Kubernetes; Java; Prometheus; AWS | Experience: 6-8 years', 'Cloud & DevOps', '6-8 years'
    UNION ALL SELECT 'Product Designer', 'CRED', 'Bengaluru, Karnataka', '15-24 LPA', 'Job Summary: Shape consumer financial experiences. | Required Skills: Figma; User Research; Prototyping | Experience: 3-5 years', 'Design & Product', '3-5 years'
    UNION ALL SELECT 'Operations Analyst', 'Zomato', 'Gurugram, Haryana', '6-10 LPA', 'Job Summary: Improve operations through reporting. | Required Skills: Excel; SQL; Communication | Experience: 0-2 years', 'Business & Operations', '0-2 years'
    UNION ALL SELECT 'Flutter Developer', 'Dream11', 'Mumbai, Maharashtra', '12-18 LPA', 'Job Summary: Build mobile experiences across platforms. | Required Skills: Flutter; Dart; REST APIs | Experience: 3-5 years', 'Mobile Development', '3-5 years'
    UNION ALL SELECT 'SAP Fiori Consultant', 'EY', 'Hyderabad, Telangana', '14-21 LPA', 'Job Summary: Deliver modern SAP Fiori apps. | Required Skills: SAP Fiori; UI5; OData; JavaScript | Experience: 3-5 years', 'Enterprise Systems', '3-5 years'
) n
LEFT JOIN jobs j ON j.title = n.title AND j.company = n.company
WHERE j.id IS NULL;
