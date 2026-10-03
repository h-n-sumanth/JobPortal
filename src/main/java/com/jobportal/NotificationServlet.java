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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session != null ? (String) session.getAttribute("user") : null;
        String adminEmail = session != null ? (String) session.getAttribute("admin") : null;

        String recipientEmail = null;
        String recipientRole = null;

        if (userEmail != null) {
            recipientEmail = userEmail;
            recipientRole = "USER";
        } else if (adminEmail != null) {
            recipientEmail = adminEmail;
            recipientRole = "ADMIN";
        } else {
            write(res, 401, error("Session required to view notifications"));
            return;
        }

        List<Map<String, Object>> notifications = new ArrayList<>();
        int unreadCount = 0;

        String sql = "SELECT id, title, message, is_read, created_at FROM notifications "
                + "WHERE recipient_email=? AND recipient_role=? ORDER BY created_at DESC LIMIT 30";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, recipientEmail);
            ps.setString(2, recipientRole);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> notif = new HashMap<>();
                    boolean isRead = rs.getBoolean("is_read");
                    if (!isRead) unreadCount++;

                    notif.put("id", rs.getInt("id"));
                    notif.put("title", rs.getString("title"));
                    notif.put("message", rs.getString("message"));
                    notif.put("isRead", isRead);
                    notif.put("createdAt", rs.getString("created_at"));
                    notifications.add(notif);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", notifications);
            response.put("unreadCount", unreadCount);
            write(res, 200, response);

        } catch (Exception e) {
            write(res, 500, error("Unable to load notifications: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String userEmail = session != null ? (String) session.getAttribute("user") : null;
        String adminEmail = session != null ? (String) session.getAttribute("admin") : null;

        String recipientEmail = userEmail != null ? userEmail : adminEmail;
        String recipientRole = userEmail != null ? "USER" : (adminEmail != null ? "ADMIN" : null);

        if (recipientEmail == null) {
            write(res, 401, error("Session required"));
            return;
        }

        String action = req.getParameter("action");
        try (Connection con = DBConnection.getConnection()) {
            if ("markRead".equalsIgnoreCase(action)) {
                String idStr = req.getParameter("id");
                if (idStr != null) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE notifications SET is_read=TRUE WHERE id=? AND recipient_email=? AND recipient_role=?")) {
                        ps.setInt(1, Integer.parseInt(idStr));
                        ps.setString(2, recipientEmail);
                        ps.setString(3, recipientRole);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE notifications SET is_read=TRUE WHERE recipient_email=? AND recipient_role=?")) {
                        ps.setString(1, recipientEmail);
                        ps.setString(2, recipientRole);
                        ps.executeUpdate();
                    }
                }
            } else if ("clear".equalsIgnoreCase(action)) {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM notifications WHERE recipient_email=? AND recipient_role=?")) {
                    ps.setString(1, recipientEmail);
                    ps.setString(2, recipientRole);
                    ps.executeUpdate();
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification status updated");
            write(res, 200, response);

        } catch (Exception e) {
            write(res, 500, error("Unable to update notification: " + e.getMessage()));
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
