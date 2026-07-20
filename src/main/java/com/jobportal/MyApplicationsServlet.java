package com.jobportal;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

public class MyApplicationsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Gson gson = new Gson();
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> applications = new ArrayList<>();

        try {
            HttpSession session = req.getSession(false);
            String email = session != null ? (String) session.getAttribute("user") : null;

            if (email == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("status", "error");
                response.put("message", "User not logged in");
                out.print(gson.toJson(response));
                return;
            }

            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT a.id, a.resume, a.applied_on, j.title, j.company, j.location, j.salary, j.status " +
                "FROM applications a JOIN jobs j ON a.job_id = j.id " +
                "WHERE a.user_email = ? ORDER BY a.id DESC"
            );
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> app = new HashMap<>();
                app.put("id", rs.getInt("id"));
                app.put("resume", rs.getString("resume"));
                app.put("job_title", rs.getString("title"));
                app.put("company", rs.getString("company"));
                app.put("location", rs.getString("location"));
                app.put("salary", rs.getString("salary"));
                app.put("status", rs.getString("status"));
                app.put("applied_on", rs.getString("applied_on"));
                applications.add(app);
            }

            response.put("status", "success");
            response.put("data", applications);

        } catch (Exception e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }

        out.print(gson.toJson(response));
        out.flush();
    }
}
