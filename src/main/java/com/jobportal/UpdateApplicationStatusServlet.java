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

public class UpdateApplicationStatusServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");

        if (adminEmail == null) {
            write(res, 401, error("Admin session required"));
            return;
        }

        String idStr = req.getParameter("id");
        String newStatus = req.getParameter("status");

        if (idStr == null || newStatus == null || newStatus.trim().isEmpty()) {
            write(res, 400, error("Application ID and status are required"));
            return;
        }

        try {
            int appId = Integer.parseInt(idStr);

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement check = con.prepareStatement(
                         "SELECT a.user_email, j.title, j.company FROM applications a JOIN jobs j ON a.job_id=j.id "
                         + "WHERE a.id=? AND (j.created_by_admin=? OR ( ? <> '' AND LOWER(j.company)=LOWER(?) ))");
                 PreparedStatement update = con.prepareStatement("UPDATE applications SET status=? WHERE id=?");
                 PreparedStatement notif = con.prepareStatement(
                         "INSERT INTO notifications(recipient_email, recipient_role, title, message) VALUES(?,'USER',?,?)")) {

                String comp = adminCompany != null ? adminCompany.trim() : "";
                check.setInt(1, appId);
                check.setString(2, adminEmail);
                check.setString(3, comp);
                check.setString(4, comp);

                String userEmail, jobTitle, company;
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        write(res, 403, error("Application not found or unauthorized"));
                        return;
                    }
                    userEmail = rs.getString("user_email");
                    jobTitle = rs.getString("title");
                    company = rs.getString("company");
                }

                update.setString(1, newStatus.trim());
                update.setInt(2, appId);
                update.executeUpdate();

                // Notify User
                notif.setString(1, userEmail);
                notif.setString(2, "Application Status Updated: " + newStatus.trim());
                notif.setString(3, "Your application for '" + jobTitle + "' at " + company + " status was updated to: " + newStatus.trim());
                notif.executeUpdate();

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Application status updated to '" + newStatus.trim() + "' and candidate notified!");
                write(res, 200, response);
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
