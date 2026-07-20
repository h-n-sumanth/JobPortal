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

public class ViewApplicantsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Gson gson = new Gson();
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> applicants = new ArrayList<>();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                "SELECT a.*, j.title FROM applications a JOIN jobs j ON a.job_id=j.id");

            while (rs.next()) {
                Map<String, Object> applicant = new HashMap<>();
                applicant.put("id", rs.getInt("id"));
                applicant.put("user_email", rs.getString("user_email"));
                applicant.put("job_id", rs.getInt("job_id"));
                applicant.put("job_title", rs.getString("title"));
                applicant.put("resume", rs.getString("resume"));
                applicants.add(applicant);
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
