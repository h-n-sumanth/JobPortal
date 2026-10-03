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
import java.util.HashMap;
import java.util.Map;

public class DeleteApplicationServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");

        if (adminEmail == null) {
            write(res, 401, error("Admin session required"));
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null) {
            write(res, 400, error("Application ID is required"));
            return;
        }

        try {
            int appId = Integer.parseInt(idStr);

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "DELETE a FROM applications a JOIN jobs j ON a.job_id = j.id "
                         + "WHERE a.id=? AND (j.created_by_admin=? OR ( ? <> '' AND LOWER(j.company)=LOWER(?) ))")) {

                String comp = adminCompany != null ? adminCompany.trim() : "";
                ps.setInt(1, appId);
                ps.setString(2, adminEmail);
                ps.setString(3, comp);
                ps.setString(4, comp);

                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Application #" + appId + " removed successfully!");
                    write(res, 200, response);
                } else {
                    write(res, 403, error("Application not found or unauthorized"));
                }
            }

        } catch (NumberFormatException e) {
            write(res, 400, error("Invalid application ID"));
        } catch (Exception e) {
            write(res, 500, error("Server error: " + e.getMessage()));
        }
    }

    private Map<String, Object> error(String message) {
        Map<String, Object> r = new HashMap<>(); r.put("status", "error"); r.put("message", message); return r;
    }
    private void write(HttpServletResponse res, int status, Map<String, Object> body) throws IOException {
        res.setStatus(status); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8");
        try (PrintWriter out = res.getWriter()) { out.print(GSON.toJson(body)); }
    }
}
