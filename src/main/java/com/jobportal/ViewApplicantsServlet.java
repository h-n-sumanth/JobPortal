package com.jobportal;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewApplicantsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Gson gson = new Gson();
        Map<String, Object> response = new HashMap<>();

        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");

        if (adminEmail == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.put("status", "error");
            response.put("message", "Admin login required");
            out.print(gson.toJson(response));
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
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
            List<Map<String, Object>> applicants = new ArrayList<>();

            String sql = "SELECT a.id AS app_id, a.user_email, a.resume, a.status AS app_status, a.applied_on, "
                    + "j.id AS job_id, j.title AS job_title, j.company, "
                    + "u.name AS user_name, p.gender, p.phone, p.profile_picture, p.dob, p.address, p.skills, p.experience, p.education, p.preferred_role, "
                    + "r.custom_name AS resume_custom_name "
                    + "FROM applications a "
                    + "JOIN jobs j ON a.job_id = j.id "
                    + "LEFT JOIN users u ON a.user_email = u.email "
                    + "LEFT JOIN user_profiles p ON a.user_email = p.user_email "
                    + "LEFT JOIN user_resumes r ON (a.user_email = r.user_email AND a.resume = r.stored_name) "
                    + "WHERE (j.created_by_admin = ? OR ( ? <> '' AND LOWER(j.company) = LOWER(?) )) "
                    + "ORDER BY a.applied_on DESC";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, adminEmail);
                ps.setString(2, comp);
                ps.setString(3, comp);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> applicant = new HashMap<>();
                        applicant.put("id", rs.getInt("app_id"));
                        applicant.put("user_email", rs.getString("user_email"));
                        applicant.put("name", rs.getString("user_name"));
                        applicant.put("phone", rs.getString("phone"));
                        applicant.put("gender", rs.getString("gender"));
                        applicant.put("dob", rs.getString("dob"));
                        applicant.put("address", rs.getString("address"));
                        applicant.put("skills", rs.getString("skills"));
                        applicant.put("experience", rs.getString("experience"));
                        applicant.put("education", rs.getString("education"));
                        applicant.put("preferred_role", rs.getString("preferred_role"));
                        applicant.put("profile_picture", rs.getString("profile_picture"));
                        applicant.put("job_id", rs.getInt("job_id"));
                        applicant.put("job_title", rs.getString("job_title"));
                        applicant.put("company", rs.getString("company"));
                        applicant.put("resume", rs.getString("resume"));
                        applicant.put("resume_custom_name", rs.getString("resume_custom_name") != null ? rs.getString("resume_custom_name") : "Candidate Resume");
                        applicant.put("status", rs.getString("app_status"));
                        applicant.put("applied_on", rs.getString("applied_on"));
                        applicants.add(applicant);
                    }
                }
            }

            response.put("status", "success");
            response.put("data", applicants);

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }
        
        out.print(gson.toJson(response));
        out.flush();
    }
}
