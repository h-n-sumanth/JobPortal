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
import java.util.HashMap;
import java.util.Map;

public class AddJobServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Map<String, String> response = new HashMap<>();
        Gson gson = new Gson();

        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");

        if (adminEmail == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.put("status", "error");
            response.put("message", "Admin session expired or unauthorized");
            out.print(gson.toJson(response));
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            // Fetch actual company name from DB if not in session
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

            if (adminCompany == null || adminCompany.trim().isEmpty()) {
                adminCompany = "Company";
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO jobs(title, company, location, salary, description, status, category, experience_level, job_type, work_mode, required_skills, created_by_admin) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");

            ps.setString(1, req.getParameter("title"));
            ps.setString(2, adminCompany.trim()); // Enforce logged-in admin's company
            ps.setString(3, req.getParameter("location"));
            ps.setString(4, req.getParameter("salary"));
            ps.setString(5, req.getParameter("description"));
            ps.setString(6, req.getParameter("status"));
            ps.setString(7, req.getParameter("category"));
            ps.setString(8, req.getParameter("experience"));
            ps.setString(9, req.getParameter("jobType"));
            ps.setString(10, req.getParameter("workMode") != null ? req.getParameter("workMode") : "On-site");
            ps.setString(11, req.getParameter("skills") != null ? req.getParameter("skills") : "");
            ps.setString(12, adminEmail);

            ps.executeUpdate();

            response.put("status", "success");
            response.put("message", "Job published successfully for " + adminCompany.trim());

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }
        
        out.print(gson.toJson(response));
        out.flush();
    }
}
