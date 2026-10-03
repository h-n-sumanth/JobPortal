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

public class AdminRegisterServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String name = safe(req.getParameter("name"));
        String companyName = safe(req.getParameter("companyName"));
        String email = safe(req.getParameter("email"));
        String password = safe(req.getParameter("password"));

        if (name.isEmpty() || companyName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            write(res, 400, error("All fields (Name, Company Name, Email, Password) are required"));
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement check = con.prepareStatement("SELECT email FROM admin WHERE email=?");
             PreparedStatement insert = con.prepareStatement(
                     "INSERT INTO admin(email, password, name, company_name) VALUES(?,?,?,?)")) {

            check.setString(1, email);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    write(res, 409, error("An admin account with this email already exists"));
                    return;
                }
            }

            insert.setString(1, email);
            insert.setString(2, password);
            insert.setString(3, name);
            insert.setString(4, companyName);
            insert.executeUpdate();

            HttpSession session = req.getSession(true);
            session.setAttribute("admin", email);
            session.setAttribute("adminName", name);
            session.setAttribute("adminCompany", companyName);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Admin registered successfully!");
            response.put("name", name);
            response.put("company", companyName);
            write(res, 200, response);

        } catch (Exception e) {
            write(res, 500, error("Registration failed: " + e.getMessage()));
        }
    }

    private String safe(String val) { return val == null ? "" : val.trim(); }
    private Map<String, Object> error(String message) {
        Map<String, Object> r = new HashMap<>(); r.put("status", "error"); r.put("message", message); return r;
    }
    private void write(HttpServletResponse res, int status, Map<String, Object> body) throws IOException {
        res.setStatus(status); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8");
        try (PrintWriter out = res.getWriter()) { out.print(GSON.toJson(body)); }
    }
}
