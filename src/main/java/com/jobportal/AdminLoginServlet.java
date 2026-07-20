package com.jobportal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

public class AdminLoginServlet extends HttpServlet {

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
                    "SELECT * FROM admin WHERE email=? AND password=?");

            ps.setString(1, req.getParameter("email"));
            ps.setString(2, req.getParameter("password"));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                req.getSession().setAttribute("admin", rs.getString("email"));
                response.put("status", "success");
                response.put("message", "Admin login successful");
            } else {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("status", "error");
                response.put("message", "Invalid admin credentials");
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
