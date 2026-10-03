package com.jobportal;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewJobsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<String> filters = new ArrayList<>();
        List<String> values = new ArrayList<>();

        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");
        boolean adminOnly = "true".equalsIgnoreCase(req.getParameter("adminOnly"));

        try (Connection con = DBConnection.getConnection()) {
            if (adminOnly) {
                if (adminEmail == null) {
                    write(res, 401, error("Admin session required"));
                    return;
                }

                if (adminCompany == null || adminCompany.trim().isEmpty()) {
                    try (PreparedStatement adminPs = con.prepareStatement("SELECT company_name FROM admin WHERE email=?")) {
                        adminPs.setString(1, adminEmail);
                        try (ResultSet rs = adminPs.executeQuery()) {
                            if (rs.next() && rs.getString("company_name") != null) {
                                adminCompany = rs.getString("company_name");
                            }
                        }
                    }
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
            addLikeFilter(req, "location", "location", filters, values);
            addLikeFilter(req, "company", "company", filters, values);
            addLikeFilter(req, "salaryRange", "salary", filters, values);

            String skills = value(req.getParameter("skills"));
            if (skills != null) {
                filters.add("(required_skills LIKE ? OR description LIKE ?)");
                values.add("%" + skills + "%");
                values.add("%" + skills + "%");
            }

            String keyword = value(req.getParameter("keyword"));
            if (keyword != null) {
                filters.add("(title LIKE ? OR company LIKE ? OR description LIKE ?)");
                values.add("%" + keyword + "%");
                values.add("%" + keyword + "%");
                values.add("%" + keyword + "%");
            }

            String fullSql = "SELECT id, title, company, location, salary, description, status, category, experience_level, job_type, work_mode, required_skills, created_by_admin FROM jobs"
                    + (filters.isEmpty() ? "" : " WHERE " + String.join(" AND ", filters)) + " ORDER BY id DESC";

            String basicSql = "SELECT id, title, company, location, salary, description, status FROM jobs"
                    + (filters.isEmpty() ? "" : " WHERE " + String.join(" AND ", filters)) + " ORDER BY id DESC";

            List<Map<String, Object>> jobs = new ArrayList<>();
            try (PreparedStatement ps = con.prepareStatement(fullSql)) {
                for (int i = 0; i < values.size(); i++) {
                    ps.setString(i + 1, values.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> job = new HashMap<>();
                        job.put("id", rs.getInt("id"));
                        job.put("title", safeStr(rs.getString("title")));
                        job.put("company", safeStr(rs.getString("company")));
                        job.put("location", safeStr(rs.getString("location")));
                        job.put("salary", safeStr(rs.getString("salary")));
                        job.put("description", safeStr(rs.getString("description")));
                        job.put("status", safeStr(rs.getString("status")));
                        job.put("category", safeStr(rs.getString("category")));
                        job.put("experience", safeStr(rs.getString("experience_level")));
                        job.put("jobType", safeStr(rs.getString("job_type")));
                        job.put("workMode", safeStr(rs.getString("work_mode")));
                        job.put("skills", safeStr(rs.getString("required_skills")));
                        job.put("createdByAdmin", safeStr(rs.getString("created_by_admin")));
                        jobs.add(job);
                    }
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("data", jobs);
                    write(res, 200, response);
                    return;
                }
            } catch (SQLException ex) {
                // Fallback for older schemas
                try (PreparedStatement basicPs = con.prepareStatement(basicSql)) {
                    for (int i = 0; i < values.size(); i++) {
                        basicPs.setString(i + 1, values.get(i));
                    }
                    try (ResultSet rs = basicPs.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> job = new HashMap<>();
                            job.put("id", rs.getInt("id"));
                            job.put("title", safeStr(rs.getString("title")));
                            job.put("company", safeStr(rs.getString("company")));
                            job.put("location", safeStr(rs.getString("location")));
                            job.put("salary", safeStr(rs.getString("salary")));
                            job.put("description", safeStr(rs.getString("description")));
                            job.put("status", safeStr(rs.getString("status")));
                            job.put("category", "Software Development");
                            job.put("experience", "0-2 years");
                            job.put("jobType", "Full-Time");
                            job.put("workMode", "On-site");
                            job.put("skills", "Java, SQL");
                            job.put("createdByAdmin", "");
                            jobs.add(job);
                        }
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "success");
                        response.put("data", jobs);
                        write(res, 200, response);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            write(res, 500, error("Unable to load jobs: " + e.getMessage()));
        }
    }

    private void addExactFilter(HttpServletRequest req, String param, String column, List<String> filters, List<String> values) {
        String val = value(req.getParameter(param));
        if (val != null && !"all".equalsIgnoreCase(val)) {
            filters.add(column + "=?");
            values.add(val);
        }
    }

    private void addLikeFilter(HttpServletRequest req, String param, String column, List<String> filters, List<String> values) {
        String val = value(req.getParameter(param));
        if (val != null && !"all".equalsIgnoreCase(val)) {
            filters.add(column + " LIKE ?");
            values.add("%" + val + "%");
        }
    }

    private String value(String input) { return input == null || input.trim().isEmpty() ? null : input.trim(); }
    private String safeStr(String val) { return val == null ? "" : val; }
    private Map<String, Object> error(String message) { Map<String, Object> r = new HashMap<>(); r.put("status", "error"); r.put("message", message); return r; }
    private void write(HttpServletResponse res, int status, Map<String, Object> body) throws IOException { res.setStatus(status); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8"); try (PrintWriter out = res.getWriter()) { out.print(new Gson().toJson(body)); } }
}
