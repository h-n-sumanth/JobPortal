package com.jobportal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

public class RegisterServlet extends HttpServlet {

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
                    "INSERT INTO users(name,email,password) VALUES(?,?,?)");

            ps.setString(1, req.getParameter("name"));
            ps.setString(2, req.getParameter("email"));
            ps.setString(3, req.getParameter("password"));

            ps.executeUpdate();
            
            response.put("status", "success");
            response.put("message", "User registered successfully");

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            res.setStatus(HttpServletResponse.SC_CONFLICT);
            response.put("status", "error");
            response.put("message", "An account with this email already exists");
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            if (e.getMessage().contains("Duplicate")) {
                response.put("message", "An account with this email already exists");
            } else {
                response.put("message", "Server error: " + e.getMessage());
            }
        }
        
        out.print(gson.toJson(response));
        out.flush();
    }
}
