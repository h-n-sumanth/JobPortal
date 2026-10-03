package com.jobportal.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jobportal.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestApiServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Enable CORS for external API clients
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        String pathInfo = req.getPathInfo(); // e.g. /jobs, /jobs/1, /auth/login
        if (pathInfo == null || pathInfo.equals("/")) {
            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "JobPortal REST API v1 is active", Map.of(
                    "version", "1.0",
                    "docs", "/html/apidocs.html",
                    "endpoints", List.of("/jobs", "/applications", "/auth/login", "/auth/register", "/auth/me", "/profile", "/notifications")
            ));
            return;
        }

        try {
            if ("PATCH".equalsIgnoreCase(req.getMethod())) {
                doPatch(req, resp);
            } else {
                super.service(req, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "API processing error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = getNormalizedPath(req);

        if (path.equals("/jobs")) {
            handleGetJobs(req, resp);
        } else if (path.matches("^/jobs/\\d+$")) {
            int jobId = Integer.parseInt(path.substring("/jobs/".length()));
            handleGetJobById(req, resp, jobId);
        } else if (path.equals("/applications")) {
            handleGetApplications(req, resp);
        } else if (path.equals("/auth/me")) {
            handleGetAuthMe(req, resp);
        } else if (path.equals("/profile")) {
            handleGetProfile(req, resp);
        } else if (path.equals("/notifications")) {
            handleGetNotifications(req, resp);
        } else {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "API Resource endpoint not found: " + path);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = getNormalizedPath(req);

        if (path.equals("/auth/login")) {
            handleAuthLogin(req, resp);
        } else if (path.equals("/auth/register")) {
            handleAuthRegister(req, resp);
        } else if (path.equals("/auth/logout")) {
            handleAuthLogout(req, resp);
        } else if (path.equals("/jobs")) {
            handleCreateJob(req, resp);
        } else if (path.equals("/applications")) {
            handleCreateApplication(req, resp);
        } else if (path.matches("^/notifications/\\d+/read$")) {
            int notifId = Integer.parseInt(path.split("/")[2]);
            handleMarkNotificationRead(req, resp, notifId);
        } else {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "API Resource endpoint not found: " + path);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = getNormalizedPath(req);

        if (path.matches("^/jobs/\\d+$")) {
            int jobId = Integer.parseInt(path.substring("/jobs/".length()));
            handleUpdateJob(req, resp, jobId);
        } else if (path.equals("/profile")) {
            handleUpdateProfile(req, resp);
        } else if (path.matches("^/applications/\\d+/status$")) {
            int appId = Integer.parseInt(path.split("/")[2]);
            handleUpdateApplicationStatus(req, resp, appId);
        } else {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "API Resource endpoint not found: " + path);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = getNormalizedPath(req);

        if (path.matches("^/applications/\\d+/status$")) {
            int appId = Integer.parseInt(path.split("/")[2]);
            handleUpdateApplicationStatus(req, resp, appId);
        } else if (path.matches("^/notifications/\\d+/read$")) {
            int notifId = Integer.parseInt(path.split("/")[2]);
            handleMarkNotificationRead(req, resp, notifId);
        } else {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "API Resource endpoint not found: " + path);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = getNormalizedPath(req);

        if (path.matches("^/jobs/\\d+$")) {
            int jobId = Integer.parseInt(path.substring("/jobs/".length()));
            handleDeleteJob(req, resp, jobId);
        } else if (path.matches("^/applications/\\d+$")) {
            int appId = Integer.parseInt(path.substring("/applications/".length()));
            handleDeleteApplication(req, resp, appId);
        } else if (path.equals("/notifications")) {
            handleClearNotifications(req, resp);
        } else {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "API Resource endpoint not found: " + path);
        }
    }

    private String getNormalizedPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty()) return "/";
        if (path.endsWith("/") && path.length() > 1) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    // ==========================================
    // AUTH HANDLERS
    // ==========================================

    private void handleAuthLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = ApiResponseUtil.readJsonObject(req);
        String email = ApiResponseUtil.getJsonString(json, "email");
        String password = ApiResponseUtil.getJsonString(json, "password");
        String role = ApiResponseUtil.getJsonString(json, "role"); // "candidate" or "admin"

        if (email == null || password == null) {
            // Check fallback request parameters
            email = req.getParameter("email");
            password = req.getParameter("password");
            if (role == null) role = req.getParameter("role");
        }

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if ("admin".equalsIgnoreCase(role)) {
                try (PreparedStatement ps = con.prepareStatement("SELECT email, name, company_name FROM admin WHERE email=? AND password=?")) {
                    ps.setString(1, email.trim());
                    ps.setString(2, password.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            HttpSession session = req.getSession(true);
                            session.setAttribute("admin", rs.getString("email"));
                            session.setAttribute("adminName", rs.getString("name"));
                            session.setAttribute("adminCompany", rs.getString("company_name"));

                            Map<String, Object> data = new HashMap<>();
                            data.put("role", "admin");
                            data.put("email", rs.getString("email"));
                            data.put("name", rs.getString("name"));
                            data.put("company", rs.getString("company_name"));
                            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Admin login successful", data);
                            return;
                        }
                    }
                }
            } else {
                // Try candidate first
                try (PreparedStatement ps = con.prepareStatement("SELECT name, email FROM users WHERE email=? AND password=?")) {
                    ps.setString(1, email.trim());
                    ps.setString(2, password.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            HttpSession session = req.getSession(true);
                            session.setAttribute("user", rs.getString("email"));

                            Map<String, Object> data = new HashMap<>();
                            data.put("role", "candidate");
                            data.put("email", rs.getString("email"));
                            data.put("name", rs.getString("name"));
                            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "User login successful", data);
                            return;
                        }
                    }
                }

                // If candidate login failed, check if admin credentials match
                try (PreparedStatement ps = con.prepareStatement("SELECT email, name, company_name FROM admin WHERE email=? AND password=?")) {
                    ps.setString(1, email.trim());
                    ps.setString(2, password.trim());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            HttpSession session = req.getSession(true);
                            session.setAttribute("admin", rs.getString("email"));
                            session.setAttribute("adminName", rs.getString("name"));
                            session.setAttribute("adminCompany", rs.getString("company_name"));

                            Map<String, Object> data = new HashMap<>();
                            data.put("role", "admin");
                            data.put("email", rs.getString("email"));
                            data.put("name", rs.getString("name"));
                            data.put("company", rs.getString("company_name"));
                            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Admin login successful", data);
                            return;
                        }
                    }
                }
            }

            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Login failed: " + e.getMessage());
        }
    }

    private void handleAuthRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = ApiResponseUtil.readJsonObject(req);
        String name = ApiResponseUtil.getJsonString(json, "name");
        String email = ApiResponseUtil.getJsonString(json, "email");
        String password = ApiResponseUtil.getJsonString(json, "password");
        String role = ApiResponseUtil.getJsonString(json, "role"); // "candidate" or "admin"
        String companyName = ApiResponseUtil.getJsonString(json, "companyName");

        if (email == null || password == null) {
            name = req.getParameter("name");
            email = req.getParameter("email");
            password = req.getParameter("password");
            role = req.getParameter("role");
            companyName = req.getParameter("companyName");
        }

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name, email and password are required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if ("admin".equalsIgnoreCase(role)) {
                String comp = (companyName == null || companyName.trim().isEmpty()) ? "General Company" : companyName.trim();
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO admin (email, password, name, company_name) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, email.trim());
                    ps.setString(2, password.trim());
                    ps.setString(3, name.trim());
                    ps.setString(4, comp);
                    ps.executeUpdate();

                    HttpSession session = req.getSession(true);
                    session.setAttribute("admin", email.trim());
                    session.setAttribute("adminName", name.trim());
                    session.setAttribute("adminCompany", comp);

                    Map<String, Object> data = new HashMap<>();
                    data.put("role", "admin");
                    data.put("email", email.trim());
                    data.put("name", name.trim());
                    data.put("company", comp);
                    ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Admin account registered successfully", data);
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO users (name, email, password) VALUES (?, ?, ?)")) {
                    ps.setString(1, name.trim());
                    ps.setString(2, email.trim());
                    ps.setString(3, password.trim());
                    ps.executeUpdate();

                    HttpSession session = req.getSession(true);
                    session.setAttribute("user", email.trim());

                    Map<String, Object> data = new HashMap<>();
                    data.put("role", "candidate");
                    data.put("email", email.trim());
                    data.put("name", name.trim());
                    ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "User account registered successfully", data);
                }
            }
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, "Registration failed or account already exists: " + e.getMessage());
        }
    }

    private void handleGetAuthMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            String userEmail = (String) session.getAttribute("user");
            Map<String, Object> data = new HashMap<>();
            data.put("authenticated", true);
            data.put("role", "candidate");
            data.put("email", userEmail);

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement("SELECT name FROM users WHERE email=?")) {
                ps.setString(1, userEmail);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.put("name", rs.getString("name"));
                    }
                }
            } catch (Exception ignored) {}

            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Authenticated as candidate", data);
        } else if (session != null && session.getAttribute("admin") != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("authenticated", true);
            data.put("role", "admin");
            data.put("email", session.getAttribute("admin"));
            data.put("name", session.getAttribute("adminName"));
            data.put("company", session.getAttribute("adminCompany"));
            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Authenticated as admin", data);
        } else {
            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Not authenticated", Map.of("authenticated", false));
        }
    }

    private void handleAuthLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Logout successful", null);
    }

    // ==========================================
    // JOBS HANDLERS
    // ==========================================

    private void handleGetJobs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> filters = new ArrayList<>();
        List<String> values = new ArrayList<>();

        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");
        boolean adminOnly = "true".equalsIgnoreCase(req.getParameter("adminOnly"));

        try (Connection con = DBConnection.getConnection()) {
            if (adminOnly) {
                if (adminEmail == null) {
                    ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Admin session required for adminOnly query");
                    return;
                }
                String comp = adminCompany != null ? adminCompany.trim() : "";
                filters.add("(created_by_admin=? OR ( ? <> '' AND LOWER(company)=LOWER(?) ))");
                values.add(adminEmail);
                values.add(comp);
                values.add(comp);
            }

            addExactFilter(req, "category", "category", filters, values);
            addExactFilter(req, "experience", "experience_level", filters, values);
            addExactFilter(req, "jobType", "job_type", filters, values);
            addExactFilter(req, "workMode", "work_mode", filters, values);

            addLikeFilter(req, "jobRole", "title", filters, values);
            addLikeFilter(req, "title", "title", filters, values);
            addLikeFilter(req, "location", "location", filters, values);
            addLikeFilter(req, "company", "company", filters, values);

            String skills = req.getParameter("skills");
            if (skills != null && !skills.trim().isEmpty()) {
                filters.add("(required_skills LIKE ? OR description LIKE ?)");
                values.add("%" + skills.trim() + "%");
                values.add("%" + skills.trim() + "%");
            }

            String keyword = req.getParameter("keyword");
            if (keyword != null && !keyword.trim().isEmpty()) {
                filters.add("(title LIKE ? OR company LIKE ? OR description LIKE ?)");
                values.add("%" + keyword.trim() + "%");
                values.add("%" + keyword.trim() + "%");
                values.add("%" + keyword.trim() + "%");
            }

            String sql = "SELECT id, title, company, location, salary, description, status, category, experience_level, job_type, work_mode, required_skills, created_by_admin FROM jobs"
                    + (filters.isEmpty() ? "" : " WHERE " + String.join(" AND ", filters)) + " ORDER BY id DESC";

            List<Map<String, Object>> jobs = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (int i = 0; i < values.size(); i++) {
                    ps.setString(i + 1, values.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> job = mapResultSetToJob(rs);
                        jobs.add(job);
                    }
                }
            }
            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, null, jobs);
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve jobs: " + e.getMessage());
        }
    }

    private void handleGetJobById(HttpServletRequest req, HttpServletResponse resp, int jobId) throws IOException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM jobs WHERE id=?")) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, null, mapResultSetToJob(rs));
                } else {
                    ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Job with ID " + jobId + " not found.");
                }
            }
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleCreateJob(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");

        JsonObject json = ApiResponseUtil.readJsonObject(req);
        String title = ApiResponseUtil.getJsonString(json, "title");
        String company = ApiResponseUtil.getJsonString(json, "company");
        String location = ApiResponseUtil.getJsonString(json, "location");
        String salary = ApiResponseUtil.getJsonString(json, "salary");
        String description = ApiResponseUtil.getJsonString(json, "description");
        String category = ApiResponseUtil.getJsonString(json, "category");
        String experienceLevel = ApiResponseUtil.getJsonString(json, "experienceLevel");
        String jobType = ApiResponseUtil.getJsonString(json, "jobType");
        String workMode = ApiResponseUtil.getJsonString(json, "workMode");
        String requiredSkills = ApiResponseUtil.getJsonString(json, "requiredSkills");

        if (title == null) {
            title = req.getParameter("title");
            company = req.getParameter("company");
            location = req.getParameter("location");
            salary = req.getParameter("salary");
            description = req.getParameter("description");
            category = req.getParameter("category");
            experienceLevel = req.getParameter("experienceLevel");
            jobType = req.getParameter("jobType");
            workMode = req.getParameter("workMode");
            requiredSkills = req.getParameter("requiredSkills");
        }

        if (company == null || company.trim().isEmpty()) {
            company = adminCompany != null ? adminCompany : "Company";
        }
        if (category == null || category.trim().isEmpty()) category = "Software Development";
        if (experienceLevel == null || experienceLevel.trim().isEmpty()) experienceLevel = "0-2 years";
        if (jobType == null || jobType.trim().isEmpty()) jobType = "Full-Time";
        if (workMode == null || workMode.trim().isEmpty()) workMode = "On-site";

        if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Job title and description are required.");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO jobs (title, company, location, salary, description, status, category, experience_level, job_type, work_mode, required_skills, created_by_admin) VALUES (?, ?, ?, ?, ?, 'Open', ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title.trim());
            ps.setString(2, company.trim());
            ps.setString(3, location != null ? location.trim() : "Remote");
            ps.setString(4, salary != null ? salary.trim() : "Disclosed on call");
            ps.setString(5, description.trim());
            ps.setString(6, category.trim());
            ps.setString(7, experienceLevel.trim());
            ps.setString(8, jobType.trim());
            ps.setString(9, workMode.trim());
            ps.setString(10, requiredSkills != null ? requiredSkills.trim() : "");
            ps.setString(11, adminEmail);

            ps.executeUpdate();

            int generatedId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", generatedId);
            data.put("title", title);
            data.put("company", company);
            data.put("location", location);
            data.put("salary", salary);
            data.put("status", "Open");

            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Job created successfully", data);
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create job: " + e.getMessage());
        }
    }

    private void handleUpdateJob(HttpServletRequest req, HttpServletResponse resp, int jobId) throws IOException {
        JsonObject json = ApiResponseUtil.readJsonObject(req);
        String title = ApiResponseUtil.getJsonString(json, "title");
        String company = ApiResponseUtil.getJsonString(json, "company");
        String location = ApiResponseUtil.getJsonString(json, "location");
        String salary = ApiResponseUtil.getJsonString(json, "salary");
        String description = ApiResponseUtil.getJsonString(json, "description");
        String status = ApiResponseUtil.getJsonString(json, "status");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE jobs SET title=?, company=?, location=?, salary=?, description=?, status=? WHERE id=?")) {
            ps.setString(1, title);
            ps.setString(2, company);
            ps.setString(3, location);
            ps.setString(4, salary);
            ps.setString(5, description);
            ps.setString(6, status != null ? status : "Open");
            ps.setInt(7, jobId);

            int updated = ps.executeUpdate();
            if (updated > 0) {
                ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Job updated successfully", Map.of("jobId", jobId));
            } else {
                ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Job with ID " + jobId + " not found.");
            }
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update job: " + e.getMessage());
        }
    }

    private void handleDeleteJob(HttpServletRequest req, HttpServletResponse resp, int jobId) throws IOException {
        try (Connection con = DBConnection.getConnection()) {
            // Delete related applications first
            try (PreparedStatement psApp = con.prepareStatement("DELETE FROM applications WHERE job_id=?")) {
                psApp.setInt(1, jobId);
                psApp.executeUpdate();
            }
            // Delete job
            try (PreparedStatement psJob = con.prepareStatement("DELETE FROM jobs WHERE id=?")) {
                psJob.setInt(1, jobId);
                int deleted = psJob.executeUpdate();
                if (deleted > 0) {
                    ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Job deleted successfully", Map.of("jobId", jobId));
                } else {
                    ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Job with ID " + jobId + " not found.");
                }
            }
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete job: " + e.getMessage());
        }
    }

    // ==========================================
    // APPLICATIONS HANDLERS
    // ==========================================

    private void handleGetApplications(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session == null ? null : (String) session.getAttribute("user");
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");

        try (Connection con = DBConnection.getConnection()) {
            if (userEmail != null) {
                // Get Candidate's applications
                String sql = "SELECT a.id, a.job_id, a.resume, a.status, a.applied_on, j.title, j.company, j.location, j.salary " +
                        "FROM applications a JOIN jobs j ON a.job_id = j.id WHERE a.user_email=? ORDER BY a.id DESC";
                List<Map<String, Object>> apps = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, userEmail);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("applicationId", rs.getInt("id"));
                            map.put("jobId", rs.getInt("job_id"));
                            map.put("jobTitle", rs.getString("title"));
                            map.put("company", rs.getString("company"));
                            map.put("location", rs.getString("location"));
                            map.put("salary", rs.getString("salary"));
                            map.put("resume", rs.getString("resume"));
                            map.put("status", rs.getString("status"));
                            map.put("appliedOn", rs.getTimestamp("applied_on") != null ? rs.getTimestamp("applied_on").toString() : "");
                            apps.add(map);
                        }
                    }
                }
                ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, null, apps);
            } else if (adminEmail != null) {
                // Get Admin's job applicants
                String sql = "SELECT a.id as app_id, a.user_email, a.resume, a.status, a.applied_on, u.name as applicant_name, j.id as job_id, j.title as job_title " +
                        "FROM applications a JOIN jobs j ON a.job_id = j.id LEFT JOIN users u ON a.user_email = u.email " +
                        "WHERE j.created_by_admin=? ORDER BY a.id DESC";
                List<Map<String, Object>> apps = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, adminEmail);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("applicationId", rs.getInt("app_id"));
                            map.put("candidateEmail", rs.getString("user_email"));
                            map.put("candidateName", rs.getString("applicant_name"));
                            map.put("resume", rs.getString("resume"));
                            map.put("status", rs.getString("status"));
                            map.put("appliedOn", rs.getTimestamp("applied_on") != null ? rs.getTimestamp("applied_on").toString() : "");
                            map.put("jobId", rs.getInt("job_id"));
                            map.put("jobTitle", rs.getString("job_title"));
                            apps.add(map);
                        }
                    }
                }
                ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, null, apps);
            } else {
                ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required to view applications");
            }
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database query failed: " + e.getMessage());
        }
    }

    private void handleCreateApplication(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session == null ? null : (String) session.getAttribute("user");

        JsonObject json = ApiResponseUtil.readJsonObject(req);
        int jobId = json.has("jobId") ? json.get("jobId").getAsInt() : 0;
        String resume = ApiResponseUtil.getJsonString(json, "resume");
        if (userEmail == null && json.has("email")) {
            userEmail = ApiResponseUtil.getJsonString(json, "email");
        }

        if (jobId <= 0 || resume == null || resume.trim().isEmpty()) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "jobId and resume file path are required.");
            return;
        }

        if (userEmail == null) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Candidate authentication required.");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO applications (user_email, job_id, resume, status) VALUES (?, ?, ?, 'Submitted')",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, userEmail);
            ps.setInt(2, jobId);
            ps.setString(3, resume);
            ps.executeUpdate();

            int appId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) appId = rs.getInt(1);
            }

            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_CREATED, "Application submitted successfully", Map.of("applicationId", appId, "jobId", jobId));
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, "Already applied or error submitting application: " + e.getMessage());
        }
    }

    private void handleUpdateApplicationStatus(HttpServletRequest req, HttpServletResponse resp, int appId) throws IOException {
        JsonObject json = ApiResponseUtil.readJsonObject(req);
        String status = ApiResponseUtil.getJsonString(json, "status");

        if (status == null || status.trim().isEmpty()) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Application status is required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String candidateEmail = null;
            String jobTitle = "Job";
            try (PreparedStatement psSel = con.prepareStatement(
                    "SELECT a.user_email, j.title FROM applications a JOIN jobs j ON a.job_id=j.id WHERE a.id=?")) {
                psSel.setInt(1, appId);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (rs.next()) {
                        candidateEmail = rs.getString("user_email");
                        jobTitle = rs.getString("title");
                    }
                }
            }

            if (candidateEmail == null) {
                ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Application ID " + appId + " not found.");
                return;
            }

            try (PreparedStatement psUpd = con.prepareStatement("UPDATE applications SET status=? WHERE id=?")) {
                psUpd.setString(1, status.trim());
                psUpd.setInt(2, appId);
                psUpd.executeUpdate();
            }

            // Create notification for candidate
            try (PreparedStatement psNotif = con.prepareStatement(
                    "INSERT INTO notifications (recipient_email, recipient_role, title, message) VALUES (?, 'candidate', ?, ?)")) {
                psNotif.setString(1, candidateEmail);
                psNotif.setString(2, "Application Update: " + jobTitle);
                psNotif.setString(3, "Your application status for " + jobTitle + " has been updated to: " + status.trim());
                psNotif.executeUpdate();
            }

            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Application status updated to " + status.trim(), Map.of("applicationId", appId, "status", status.trim()));
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update status: " + e.getMessage());
        }
    }

    private void handleDeleteApplication(HttpServletRequest req, HttpServletResponse resp, int appId) throws IOException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM applications WHERE id=?")) {
            ps.setInt(1, appId);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Application deleted successfully", Map.of("applicationId", appId));
            } else {
                ApiResponseUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Application ID " + appId + " not found.");
            }
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete application: " + e.getMessage());
        }
    }

    // ==========================================
    // PROFILE HANDLERS
    // ==========================================

    private void handleGetProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session == null ? null : (String) session.getAttribute("user");
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");

        if (userEmail == null && adminEmail == null) {
            String qEmail = req.getParameter("email");
            if (qEmail != null && !qEmail.trim().isEmpty()) {
                userEmail = qEmail.trim();
            }
        }

        try (Connection con = DBConnection.getConnection()) {
            if (userEmail != null) {
                String sql = "SELECT u.name, u.email, p.gender, p.phone, p.profile_picture, p.dob, p.address, p.skills, p.experience, p.education, p.preferred_role " +
                        "FROM users u LEFT JOIN user_profiles p ON u.email = p.user_email WHERE u.email=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, userEmail);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("role", "candidate");
                            map.put("name", rs.getString("name"));
                            map.put("email", rs.getString("email"));
                            map.put("gender", rs.getString("gender"));
                            map.put("phone", rs.getString("phone"));
                            map.put("profilePicture", rs.getString("profile_picture"));
                            map.put("dob", rs.getString("dob"));
                            map.put("address", rs.getString("address"));
                            map.put("skills", rs.getString("skills"));
                            map.put("experience", rs.getString("experience"));
                            map.put("education", rs.getString("education"));
                            map.put("preferredRole", rs.getString("preferred_role"));
                            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, null, map);
                            return;
                        }
                    }
                }
            } else if (adminEmail != null) {
                try (PreparedStatement ps = con.prepareStatement("SELECT name, email, company_name FROM admin WHERE email=?")) {
                    ps.setString(1, adminEmail);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("role", "admin");
                            map.put("name", rs.getString("name"));
                            map.put("email", rs.getString("email"));
                            map.put("companyName", rs.getString("company_name"));
                            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, null, map);
                            return;
                        }
                    }
                }
            }

            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required to view profile.");
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Profile query error: " + e.getMessage());
        }
    }

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session == null ? null : (String) session.getAttribute("user");

        JsonObject json = ApiResponseUtil.readJsonObject(req);
        if (userEmail == null && json.has("email")) {
            userEmail = ApiResponseUtil.getJsonString(json, "email");
        }

        if (userEmail == null) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Candidate authentication required to update profile.");
            return;
        }

        String phone = ApiResponseUtil.getJsonString(json, "phone");
        String gender = ApiResponseUtil.getJsonString(json, "gender");
        String dob = ApiResponseUtil.getJsonString(json, "dob");
        String address = ApiResponseUtil.getJsonString(json, "address");
        String skills = ApiResponseUtil.getJsonString(json, "skills");
        String experience = ApiResponseUtil.getJsonString(json, "experience");
        String education = ApiResponseUtil.getJsonString(json, "education");
        String preferredRole = ApiResponseUtil.getJsonString(json, "preferredRole");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO user_profiles (user_email, phone, gender, dob, address, skills, experience, education, preferred_role) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                             "phone=VALUES(phone), gender=VALUES(gender), dob=VALUES(dob), address=VALUES(address), " +
                             "skills=VALUES(skills), experience=VALUES(experience), education=VALUES(education), preferred_role=VALUES(preferred_role)")) {

            ps.setString(1, userEmail);
            ps.setString(2, phone != null ? phone : "");
            ps.setString(3, gender);
            ps.setString(4, dob);
            ps.setString(5, address);
            ps.setString(6, skills);
            ps.setString(7, experience);
            ps.setString(8, education);
            ps.setString(9, preferredRole);

            ps.executeUpdate();

            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Profile updated successfully", Map.of("userEmail", userEmail));
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update profile: " + e.getMessage());
        }
    }

    // ==========================================
    // NOTIFICATIONS HANDLERS
    // ==========================================

    private void handleGetNotifications(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session == null ? null : (String) session.getAttribute("user");
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");

        String recipient = userEmail != null ? userEmail : adminEmail;
        if (recipient == null) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, title, message, is_read, created_at FROM notifications WHERE recipient_email=? ORDER BY created_at DESC")) {
            ps.setString(1, recipient);
            List<Map<String, Object>> notifs = new ArrayList<>();
            int unreadCount = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    boolean isRead = rs.getBoolean("is_read");
                    if (!isRead) unreadCount++;

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("title", rs.getString("title"));
                    map.put("message", rs.getString("message"));
                    map.put("isRead", isRead);
                    map.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "");
                    notifs.add(map);
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("notifications", notifs);
            data.put("unreadCount", unreadCount);
            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, null, data);
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Notifications error: " + e.getMessage());
        }
    }

    private void handleMarkNotificationRead(HttpServletRequest req, HttpServletResponse resp, int notifId) throws IOException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE notifications SET is_read=TRUE WHERE id=?")) {
            ps.setInt(1, notifId);
            ps.executeUpdate();
            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Notification marked as read", Map.of("notificationId", notifId));
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update notification: " + e.getMessage());
        }
    }

    private void handleClearNotifications(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session == null ? null : (String) session.getAttribute("user");
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String recipient = userEmail != null ? userEmail : adminEmail;

        if (recipient == null) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM notifications WHERE recipient_email=?")) {
            ps.setString(1, recipient);
            int count = ps.executeUpdate();
            ApiResponseUtil.sendSuccess(resp, HttpServletResponse.SC_OK, "Cleared " + count + " notifications", null);
        } catch (Exception e) {
            ApiResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to clear notifications: " + e.getMessage());
        }
    }

    // Helper utilities

    private Map<String, Object> mapResultSetToJob(ResultSet rs) throws Exception {
        Map<String, Object> job = new HashMap<>();
        job.put("id", rs.getInt("id"));
        job.put("title", rs.getString("title"));
        job.put("company", rs.getString("company"));
        job.put("location", rs.getString("location"));
        job.put("salary", rs.getString("salary"));
        job.put("description", rs.getString("description"));
        job.put("status", rs.getString("status"));

        try {
            job.put("category", rs.getString("category"));
            job.put("experienceLevel", rs.getString("experience_level"));
            job.put("jobType", rs.getString("job_type"));
            job.put("workMode", rs.getString("work_mode"));
            job.put("requiredSkills", rs.getString("required_skills"));
            job.put("createdByAdmin", rs.getString("created_by_admin"));
        } catch (Exception ignored) {}

        return job;
    }

    private void addExactFilter(HttpServletRequest req, String param, String col, List<String> filters, List<String> values) {
        String val = req.getParameter(param);
        if (val != null && !val.trim().isEmpty()) {
            filters.add(col + "=?");
            values.add(val.trim());
        }
    }

    private void addLikeFilter(HttpServletRequest req, String param, String col, List<String> filters, List<String> values) {
        String val = req.getParameter(param);
        if (val != null && !val.trim().isEmpty()) {
            filters.add(col + " LIKE ?");
            values.add("%" + val.trim() + "%");
        }
    }
}
