-- ============================================================
--  NexJob – Insert 20 Sample Jobs with Detailed Descriptions
--  Run this file in your MySQL client or Workbench:
--    mysql -u root -pRoot jobportal < jobs_seed.sql
--
--  Description format:  SECTION: value | SECTION: value;item;item
-- ============================================================

USE jobportal;

INSERT INTO jobs (title, company, location, salary, description, status) VALUES

('Senior Java Developer',
 'Infosys', 'Bengaluru, Karnataka', '18-24 LPA',
 'Job Summary: Build and maintain enterprise-grade Java applications for banking clients. |
Key Responsibilities: Design microservices using Spring Boot; Write unit and integration tests; Review code and mentor juniors; Collaborate with DevOps for CI/CD pipelines |
Required Skills: Java 17; Spring Boot; Hibernate; REST APIs; Docker; Kubernetes |
Qualifications: B.E./B.Tech in Computer Science or related field |
Experience: 5-8 years in Java backend development |
Employment Type: Full-Time |
How to Apply: Send resume to careers@infosys.com with subject "Senior Java Developer"',
 'Open'),

('React Frontend Developer',
 'Wipro', 'Hyderabad, Telangana', '12-18 LPA',
 'Job Summary: Develop responsive, high-performance user interfaces for SaaS products. |
Key Responsibilities: Build reusable React components; Integrate RESTful APIs; Write Jest/RTL tests; Participate in Agile sprints |
Required Skills: React.js; Redux Toolkit; TypeScript; HTML5; CSS3; Git |
Qualifications: B.Sc./B.E. in CS or equivalent |
Experience: 3-6 years in frontend development |
Employment Type: Full-Time |
How to Apply: Apply via wipro.com/careers or email to frontend.jobs@wipro.com',
 'Open'),

('Data Scientist',
 'TCS', 'Mumbai, Maharashtra', '20-28 LPA',
 'Job Summary: Extract insights from large datasets to drive business decisions at scale. |
Key Responsibilities: Build and deploy ML models; Perform EDA; Present findings to stakeholders; Collaborate with data engineers |
Required Skills: Python; Pandas; Scikit-Learn; TensorFlow; SQL; Power BI |
Qualifications: M.Sc./M.Tech in Data Science, Statistics, or CS |
Experience: 4-7 years in data science or analytics |
Employment Type: Full-Time |
How to Apply: Upload resume at tcs.com/careers — search "Data Scientist"',
 'Open'),

('DevOps Engineer',
 'HCL Technologies', 'Noida, Uttar Pradesh', '15-22 LPA',
 'Job Summary: Automate infrastructure and streamline delivery pipelines for cloud-native products. |
Key Responsibilities: Manage CI/CD with Jenkins and GitHub Actions; Maintain AWS/Azure infrastructure; Monitor systems with Prometheus & Grafana; Handle incident response |
Required Skills: Docker; Kubernetes; Terraform; AWS; Linux; Bash scripting |
Qualifications: B.E. in CS/IT or equivalent |
Experience: 3-6 years in DevOps/Cloud Engineering |
Employment Type: Full-Time |
How to Apply: careers@hcl.com – Subject: DevOps Engineer Application',
 'Open'),

('Full Stack Developer (.NET)',
 'Accenture', 'Pune, Maharashtra', '14-20 LPA',
 'Job Summary: Design and develop full-stack enterprise applications using .NET and Angular. |
Key Responsibilities: Develop APIs with ASP.NET Core; Build Angular 16+ frontends; Write SQL queries and stored procedures; Participate in code reviews |
Required Skills: C#; ASP.NET Core; Angular; MS SQL Server; Azure DevOps |
Qualifications: B.E./MCA in CS or related |
Experience: 4-7 years full-stack .NET development |
Employment Type: Full-Time |
How to Apply: Apply via accenture.com/careers and search "Full Stack .NET Developer"',
 'Open'),

('Android Developer',
 'Tech Mahindra', 'Chennai, Tamil Nadu', '10-16 LPA',
 'Job Summary: Develop feature-rich Android applications for enterprise mobility solutions. |
Key Responsibilities: Develop in Kotlin; Integrate Firebase & REST APIs; Optimize app performance; Publish and manage Play Store deployments |
Required Skills: Kotlin; Android SDK; Jetpack Compose; Retrofit; Room DB; Git |
Qualifications: B.E./B.Sc. in CS, IT, or Electronics |
Experience: 2-5 years in Android development |
Employment Type: Full-Time |
How to Apply: Email resume to android.hiring@techmahindra.com',
 'Open'),

('Cloud Architect',
 'Amazon (AWS)', 'Bengaluru, Karnataka', '35-50 LPA',
 'Job Summary: Architect multi-cloud solutions for Fortune 500 clients migrating to AWS. |
Key Responsibilities: Design cloud reference architectures; Lead cloud assessments; Define security and governance policies; Mentor engineering teams |
Required Skills: AWS (Certified Solutions Architect Pro); Terraform; Multi-account strategy; Networking; Cost Optimization |
Qualifications: B.E./M.Tech + AWS/GCP certification required |
Experience: 8-12 years with 4+ years in cloud architecture |
Employment Type: Full-Time |
How to Apply: Apply at amazon.jobs – search "Cloud Architect India"',
 'Open'),

('UI/UX Designer',
 'Zoho Corporation', 'Chennai, Tamil Nadu', '8-14 LPA',
 'Job Summary: Create beautiful, accessible user experiences for Zoho''s suite of SaaS products. |
Key Responsibilities: Wireframing & prototyping in Figma; Conduct user research; Define design systems; Work with engineering to ensure pixel-perfect delivery |
Required Skills: Figma; Adobe XD; User Research; Accessibility (WCAG); HTML/CSS basics |
Qualifications: B.Des. or equivalent in Design/HCI |
Experience: 2-5 years in product UX design |
Employment Type: Full-Time |
How to Apply: Portfolio + CV to design.careers@zoho.com',
 'Open'),

('Cybersecurity Analyst',
 'IBM India', 'Delhi NCR', '16-24 LPA',
 'Job Summary: Protect enterprise clients from cyber threats through monitoring, analysis, and incident response. |
Key Responsibilities: Operate SIEM (QRadar); Perform vulnerability assessments; Handle incident response; Write threat intelligence reports |
Required Skills: SIEM; Kali Linux; Metasploit; CEH/CISSP preferred; Networking fundamentals |
Qualifications: B.E. in CS/IT + Security certification preferred |
Experience: 3-6 years in information security |
Employment Type: Full-Time |
How to Apply: ibm.com/jobs – search "Cybersecurity Analyst India"',
 'Open'),

('Machine Learning Engineer',
 'Google India', 'Hyderabad, Telangana', '40-60 LPA',
 'Job Summary: Build large-scale ML systems powering Google Search and Ads products. |
Key Responsibilities: Train and deploy ML models at scale; Write production-quality Python/C++; Optimize model latency & accuracy; Participate in ML research projects |
Required Skills: Python; TensorFlow; PyTorch; Distributed Systems; Strong CS fundamentals; LLMs |
Qualifications: M.Tech/Ph.D. in CS, AI, or related (preferred) |
Experience: 5-10 years in ML engineering |
Employment Type: Full-Time |
How to Apply: careers.google.com – search "Machine Learning Engineer Hyderabad"',
 'Open'),

('Business Analyst',
 'Capgemini', 'Bengaluru, Karnataka', '10-15 LPA',
 'Job Summary: Bridge the gap between business stakeholders and technology teams for digital transformation projects. |
Key Responsibilities: Gather and document requirements; Facilitate workshops; Write BRD/FRD; Perform UAT; Create process flow diagrams |
Required Skills: Business Analysis; JIRA; Confluence; SQL basics; Agile/Scrum; Stakeholder Management |
Qualifications: MBA or B.E. with relevant certifications (CBAP preferred) |
Experience: 3-5 years in business analysis |
Employment Type: Full-Time |
How to Apply: capgemini.com/careers and filter for Business Analyst',
 'Open'),

('Python Backend Developer',
 'Freshworks', 'Chennai, Tamil Nadu', '15-22 LPA',
 'Job Summary: Build scalable backend services for Freshdesk and CRM products used by 65,000+ businesses. |
Key Responsibilities: Develop Django/FastAPI services; Design PostgreSQL schemas; Write async APIs; Build integrations with third-party tools |
Required Skills: Python; Django; FastAPI; PostgreSQL; Redis; Celery; AWS |
Qualifications: B.E./B.Sc. in CS or equivalent |
Experience: 3-6 years in Python backend development |
Employment Type: Full-Time |
How to Apply: freshworks.com/company/jobs – search "Python Backend"',
 'Open'),

('Salesforce Developer',
 'Deloitte India', 'Mumbai, Maharashtra', '14-20 LPA',
 'Job Summary: Develop and customize Salesforce CRM for large-scale enterprise clients across BFSI and retail. |
Key Responsibilities: Develop Apex classes & Triggers; Build Lightning Web Components; Configure Flows & Process Builder; Integrate via REST/SOAP APIs |
Required Skills: Salesforce Platform Developer I/II; Apex; LWC; SOQL; Integration Patterns |
Qualifications: B.E./MCA + Salesforce Certified Developer |
Experience: 3-6 years Salesforce development |
Employment Type: Full-Time |
How to Apply: deloitte.com/careers – search "Salesforce Developer"',
 'Open'),

('iOS Developer (Swift)',
 'Swiggy', 'Bengaluru, Karnataka', '18-26 LPA',
 'Job Summary: Build and maintain Swiggy''s consumer iOS app serving millions of daily food orders. |
Key Responsibilities: Develop in Swift/SwiftUI; Build real-time order tracking features; Work on performance and crash optimization; A/B test new features |
Required Skills: Swift; SwiftUI; UIKit; CoreData; Xcode; Instruments profiler; REST APIs |
Qualifications: B.E./B.Sc. in CS or equivalent |
Experience: 4-7 years in iOS development |
Employment Type: Full-Time |
How to Apply: careers.swiggy.com – search "iOS Developer"',
 'Open'),

('Data Engineer',
 'Flipkart', 'Bengaluru, Karnataka', '20-30 LPA',
 'Job Summary: Design and build data pipelines for Flipkart''s petabyte-scale data infrastructure. |
Key Responsibilities: Build Spark/Flink pipelines; Manage Kafka streams; Design data lake architecture on S3; Monitor pipeline health |
Required Skills: Apache Spark; Kafka; Airflow; Python; SQL; AWS/GCP; Hadoop |
Qualifications: B.E./M.Tech in CS or Data Engineering |
Experience: 4-8 years in data engineering |
Employment Type: Full-Time |
How to Apply: flipkartcareers.com – search "Data Engineer"',
 'Open'),

('SAP ABAP Consultant',
 'L&T Infotech (LTIMindtree)', 'Pune, Maharashtra', '12-18 LPA',
 'Job Summary: Develop and support SAP ABAP solutions for manufacturing and logistics clients. |
Key Responsibilities: Develop Reports; BAPIs; User Exits; Enhancement Frameworks; work on SAP S/4HANA migration |
Required Skills: SAP ABAP; OOPs ABAP; ALV Reports; BAPIs/BAdIs; S/4HANA Fiori basics |
Qualifications: B.E./MCA with SAP certification preferred |
Experience: 3-6 years SAP ABAP development |
Employment Type: Full-Time |
How to Apply: ltimindtree.com/careers – search "SAP ABAP"',
 'Open'),

('QA Automation Engineer',
 'Persistent Systems', 'Nagpur, Maharashtra', '10-16 LPA',
 'Job Summary: Ensure software quality through robust automated testing frameworks. |
Key Responsibilities: Design Selenium/Playwright test suites; Integrate into CI/CD; Perform API testing with Postman/RestAssured; Generate test reports |
Required Skills: Selenium; Java/Python; TestNG; Postman; REST Assured; Jenkins; Git |
Qualifications: B.E. in CS/IT |
Experience: 3-5 years in QA automation |
Employment Type: Full-Time |
How to Apply: persistent.com/careers – filter "QA Automation"',
 'Open'),

('Product Manager',
 'Razorpay', 'Bengaluru, Karnataka', '25-40 LPA',
 'Job Summary: Own the product roadmap for Razorpay''s Payment Gateway used by 8M+ businesses. |
Key Responsibilities: Define product strategy and OKRs; Write detailed PRDs; Work with design and engineering; Analyze metrics and drive growth |
Required Skills: Product Strategy; Data Analytics; SQL basics; Figma; Agile; Stakeholder Management |
Qualifications: MBA from top institution OR 5+ years in technical PM role |
Experience: 4-8 years with 2+ years in fintech product management |
Employment Type: Full-Time |
How to Apply: razorpay.com/jobs – search "Product Manager"',
 'Open'),

('Embedded Systems Engineer',
 'Bosch India', 'Coimbatore, Tamil Nadu', '10-15 LPA',
 'Job Summary: Develop firmware and embedded software for automotive control units. |
Key Responsibilities: Write firmware in C/C++ for ECUs; Implement AUTOSAR stack; Debug using oscilloscopes and logic analyzers; Support HILS and validation |
Required Skills: C; C++; AUTOSAR; CAN/LIN protocols; RTOS; MATLAB/Simulink basics |
Qualifications: B.E. in Electronics, Electrical, or Embedded Systems |
Experience: 2-5 years in embedded engineering |
Employment Type: Full-Time |
How to Apply: bosch.in/careers – filter "Embedded Systems"',
 'Open'),

('Technical Writer',
 'Adobe India', 'Noida, Uttar Pradesh', '9-14 LPA',
 'Job Summary: Create clear, concise documentation for Adobe Creative Cloud developer APIs. |
Key Responsibilities: Write API reference guides and tutorials; Maintain docs portal; Work with product and engineering; Gather feedback from developer community |
Required Skills: Technical Writing; Markdown/RST; Git; REST API concepts; Swagger/OpenAPI; Adobe CC tools |
Qualifications: B.E. in CS or BA/MA in English/Journalism with technical exposure |
Experience: 2-5 years in technical writing for software products |
Employment Type: Full-Time |
How to Apply: adobe.com/careers – search "Technical Writer India"',
 'Open');

-- Categorise the original jobs so that the new filters work with existing data.
UPDATE jobs
SET category = CASE
    WHEN title IN ('Senior Java Developer', 'React Frontend Developer', 'Full Stack Developer (.NET)', 'Python Backend Developer') THEN 'Software Development'
    WHEN title IN ('QA Automation Engineer') THEN 'QA & Testing'
    WHEN title IN ('Data Scientist', 'Machine Learning Engineer', 'Data Engineer') THEN 'Data & AI'
    WHEN title IN ('DevOps Engineer', 'Cloud Architect', 'Cybersecurity Analyst') THEN 'Cloud & DevOps'
    WHEN title IN ('UI/UX Designer', 'Product Manager', 'Technical Writer') THEN 'Design & Product'
    WHEN title IN ('Business Analyst') THEN 'Business & Operations'
    WHEN title IN ('Android Developer', 'iOS Developer (Swift)') THEN 'Mobile Development'
    WHEN title IN ('SAP ABAP Consultant', 'Embedded Systems Engineer') THEN 'Enterprise Systems'
    ELSE 'Software Development'
END,
experience_level = CASE
    WHEN title IN ('Cloud Architect', 'Machine Learning Engineer') THEN '9+ years'
    WHEN title IN ('Senior Java Developer', 'Data Scientist', 'Full Stack Developer (.NET)', 'Data Engineer', 'Product Manager') THEN '6-8 years'
    WHEN title IN ('React Frontend Developer', 'DevOps Engineer', 'Python Backend Developer', 'QA Automation Engineer', 'SAP ABAP Consultant') THEN '3-5 years'
    ELSE '0-2 years'
END,
job_type = 'Full-Time';

-- Additional roles for every filter category.
INSERT INTO jobs (title, company, location, salary, description, status, category, experience_level, job_type) VALUES
('Junior Java Developer', 'Mindtree', 'Bengaluru, Karnataka', '5-8 LPA',
 'Job Summary: Build Spring Boot services with a supportive engineering team. | Key Responsibilities: Implement REST APIs; Fix defects; Write unit tests | Required Skills: Java; Spring Boot; SQL; Git | Qualifications: B.E./B.Tech or MCA | Experience: 0-2 years | Employment Type: Full-Time',
 'Open', 'Software Development', '0-2 years', 'Full-Time'),
('Frontend Engineer', 'Razorpay', 'Bengaluru, Karnataka', '14-20 LPA',
 'Job Summary: Create reliable payment experiences using React and TypeScript. | Key Responsibilities: Build accessible components; Integrate APIs; Review pull requests | Required Skills: React; TypeScript; CSS; Testing Library | Qualifications: Degree or equivalent experience | Experience: 3-5 years | Employment Type: Full-Time',
 'Open', 'Software Development', '3-5 years', 'Full-Time'),
('Manual QA Analyst', 'Cognizant', 'Chennai, Tamil Nadu', '4-7 LPA',
 'Job Summary: Validate web and mobile releases before they reach customers. | Key Responsibilities: Create test cases; Execute regression testing; Report defects | Required Skills: Manual Testing; JIRA; SQL basics; Postman | Qualifications: B.E./B.Sc. or equivalent | Experience: 0-2 years | Employment Type: Full-Time',
 'Open', 'QA & Testing', '0-2 years', 'Full-Time'),
('SDET - API Automation', 'Paytm', 'Noida, Uttar Pradesh', '16-24 LPA',
 'Job Summary: Build high-value automated API and platform test suites. | Key Responsibilities: Develop automation frameworks; Test microservices; Improve release quality | Required Skills: Java; Rest Assured; Selenium; CI/CD | Qualifications: B.E./MCA | Experience: 3-5 years | Employment Type: Full-Time',
 'Open', 'QA & Testing', '3-5 years', 'Full-Time'),
('Data Analyst', 'Meesho', 'Bengaluru, Karnataka', '7-12 LPA',
 'Job Summary: Turn marketplace data into clear business insights. | Key Responsibilities: Build dashboards; Analyse metrics; Present findings | Required Skills: SQL; Excel; Power BI; Python basics | Qualifications: Degree in Analytics, Statistics, or CS | Experience: 0-2 years | Employment Type: Full-Time',
 'Open', 'Data & AI', '0-2 years', 'Full-Time'),
('MLOps Engineer', 'Fractal Analytics', 'Mumbai, Maharashtra', '22-32 LPA',
 'Job Summary: Operate reliable machine learning platforms and model delivery pipelines. | Key Responsibilities: Deploy models; Monitor model quality; Automate training workflows | Required Skills: Python; MLflow; Docker; Kubernetes; AWS | Qualifications: B.E./M.Tech | Experience: 6-8 years | Employment Type: Full-Time',
 'Open', 'Data & AI', '6-8 years', 'Full-Time'),
('AWS Cloud Engineer', 'LTI Mindtree', 'Pune, Maharashtra', '13-19 LPA',
 'Job Summary: Build secure cloud infrastructure for enterprise applications. | Key Responsibilities: Automate infrastructure; Support deployments; Monitor cloud services | Required Skills: AWS; Terraform; Linux; Jenkins | Qualifications: B.E./MCA | Experience: 3-5 years | Employment Type: Full-Time',
 'Open', 'Cloud & DevOps', '3-5 years', 'Full-Time'),
('Site Reliability Engineer', 'PhonePe', 'Bengaluru, Karnataka', '24-36 LPA',
 'Job Summary: Improve availability and performance for high-scale financial systems. | Key Responsibilities: Define SLOs; Lead incident response; Optimise observability | Required Skills: Kubernetes; Go or Java; Prometheus; AWS | Qualifications: B.E./M.Tech | Experience: 6-8 years | Employment Type: Full-Time',
 'Open', 'Cloud & DevOps', '6-8 years', 'Full-Time'),
('Product Designer', 'CRED', 'Bengaluru, Karnataka', '15-24 LPA',
 'Job Summary: Shape end-to-end experiences for consumer financial products. | Key Responsibilities: Research users; Prototype flows; Maintain design systems | Required Skills: Figma; User Research; Prototyping; Accessibility | Qualifications: Design degree or strong portfolio | Experience: 3-5 years | Employment Type: Full-Time',
 'Open', 'Design & Product', '3-5 years', 'Full-Time'),
('Operations Analyst', 'Zomato', 'Gurugram, Haryana', '6-10 LPA',
 'Job Summary: Improve service operations through reporting and process improvement. | Key Responsibilities: Track KPIs; Build reports; Coordinate stakeholders | Required Skills: Excel; SQL; Communication; Problem Solving | Qualifications: Graduate degree | Experience: 0-2 years | Employment Type: Full-Time',
 'Open', 'Business & Operations', '0-2 years', 'Full-Time'),
('Flutter Developer', 'Dream11', 'Mumbai, Maharashtra', '12-18 LPA',
 'Job Summary: Build polished, performant experiences across Android and iOS. | Key Responsibilities: Develop Flutter features; Integrate APIs; Improve app performance | Required Skills: Flutter; Dart; REST APIs; Git | Qualifications: B.E./B.Sc. or equivalent | Experience: 3-5 years | Employment Type: Full-Time',
 'Open', 'Mobile Development', '3-5 years', 'Full-Time'),
('SAP Fiori Consultant', 'EY', 'Hyderabad, Telangana', '14-21 LPA',
 'Job Summary: Deliver modern SAP Fiori applications for transformation programmes. | Key Responsibilities: Build UI5 apps; Integrate OData services; Support SAP rollouts | Required Skills: SAP Fiori; UI5; OData; JavaScript | Qualifications: B.E./MCA with SAP exposure | Experience: 3-5 years | Employment Type: Full-Time',
 'Open', 'Enterprise Systems', '3-5 years', 'Full-Time');

-- Verification
SELECT id, title, category, experience_level, salary FROM jobs ORDER BY id DESC LIMIT 32;
