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

public class CurrentAdminServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Map<String, Object> response = new HashMap<>();

        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");

        if (adminEmail == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.put("status", "error");
            response.put("message", "Admin login required");
            out.print(GSON.toJson(response));
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT email, name, company_name FROM admin WHERE email=?")) {

            ps.setString(1, adminEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String company = rs.getString("company_name");
                    if (company == null || company.trim().isEmpty()) {
                        company = "Company";
                    }

                    // Ensure session holds fresh company name
                    session.setAttribute("adminName", name != null ? name : "Admin");
                    session.setAttribute("adminCompany", company);

                    response.put("status", "success");
                    response.put("email", adminEmail);
                    response.put("name", name != null ? name : "Admin");
                    response.put("company", company);
                } else {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.put("status", "error");
                    response.put("message", "Admin account not found");
                }
            }

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }

        out.print(GSON.toJson(response));
        out.flush();
    }
}
