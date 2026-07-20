package com.jobportal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

public class UpdateJobServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Map<String, String> response = new HashMap<>();
        Gson gson = new Gson();

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE jobs SET title=?, company=?, location=?, salary=?, description=?, status=?, category=?, experience_level=?, job_type=? WHERE id=?");

            ps.setString(1, req.getParameter("title"));
            ps.setString(2, req.getParameter("company"));
            ps.setString(3, req.getParameter("location"));
            ps.setString(4, req.getParameter("salary"));
            ps.setString(5, req.getParameter("description"));
            ps.setString(6, req.getParameter("status"));
            ps.setString(7, req.getParameter("category"));
            ps.setString(8, req.getParameter("experience"));
            ps.setString(9, req.getParameter("jobType"));
            ps.setInt(10, Integer.parseInt(req.getParameter("id")));

            ps.executeUpdate();

            response.put("status", "success");
            response.put("message", "Job updated successfully");

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }
        
        out.print(gson.toJson(response));
        out.flush();
    }
}
