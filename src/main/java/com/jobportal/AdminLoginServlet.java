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

public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Map<String, Object> response = new HashMap<>();
        Gson gson = new Gson();

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT email, name, company_name FROM admin WHERE email=? AND password=?");

            ps.setString(1, req.getParameter("email"));
            ps.setString(2, req.getParameter("password"));

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String email = rs.getString("email");
                String name = rs.getString("name");
                String company = rs.getString("company_name");

                HttpSession session = req.getSession(true);
                session.setAttribute("admin", email);
                session.setAttribute("adminName", name != null ? name : "Admin");
                session.setAttribute("adminCompany", company != null ? company : "Company");

                response.put("status", "success");
                response.put("message", "Admin login successful");
                response.put("email", email);
                response.put("name", name);
                response.put("company", company);
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
