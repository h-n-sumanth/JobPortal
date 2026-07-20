package com.jobportal;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

public class CurrentUserServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Gson gson = new Gson();
        Map<String, Object> response = new HashMap<>();

        try {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                String email = (String) session.getAttribute("user");
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT name, email FROM users WHERE email=?");
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    response.put("status", "success");
                    response.put("name", rs.getString("name"));
                    response.put("email", rs.getString("email"));
                } else {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.put("status", "error");
                    response.put("message", "User not found in db");
                }
            } else {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("status", "error");
                response.put("message", "User not logged in");
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }
        
        out.print(gson.toJson(response));
        out.flush();
    }
}
