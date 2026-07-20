package com.jobportal;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 7 * 1024 * 1024)
public class ProfileServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String, Object> response = new HashMap<>();
        String email = currentUser(req);
        if (email == null) {
            write(res, HttpServletResponse.SC_UNAUTHORIZED, error("User not logged in"));
            return;
        }

        String sql = "SELECT u.name, u.email, p.gender, p.phone, p.profile_picture "
                + "FROM users u LEFT JOIN user_profiles p ON u.email=p.user_email WHERE u.email=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    write(res, HttpServletResponse.SC_UNAUTHORIZED, error("User not found"));
                    return;
                }
                response.put("status", "success");
                response.put("name", rs.getString("name"));
                response.put("email", rs.getString("email"));
                response.put("gender", rs.getString("gender"));
                response.put("phone", rs.getString("phone"));
                response.put("profilePicture", rs.getString("profile_picture"));
            }
            write(res, HttpServletResponse.SC_OK, response);
        } catch (Exception e) {
            write(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error("Unable to load profile"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String email = currentUser(req);
        if (email == null) {
            write(res, HttpServletResponse.SC_UNAUTHORIZED, error("User not logged in"));
            return;
        }

        String name = safe(req.getParameter("name"));
        String gender = safe(req.getParameter("gender"));
        String phone = safe(req.getParameter("phone"));
        if (name.isEmpty() || phone.isEmpty()) {
            write(res, HttpServletResponse.SC_BAD_REQUEST, error("Name and phone number are required"));
            return;
        }

        try {
            String picture = null;
            Part photo = req.getPart("profilePicture");
            if (photo != null && photo.getSize() > 0) {
                picture = FileStorageUtil.save(photo, getServletContext(), "profile-pictures",
                        Set.of(".jpg", ".jpeg", ".png", ".webp"), 5 * 1024 * 1024);
            }

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement updateUser = con.prepareStatement("UPDATE users SET name=? WHERE email=?");
                 PreparedStatement upsert = con.prepareStatement(
                         "INSERT INTO user_profiles(user_email, gender, phone, profile_picture) VALUES(?,?,?,?) "
                         + "ON DUPLICATE KEY UPDATE gender=VALUES(gender), phone=VALUES(phone), "
                         + "profile_picture=COALESCE(VALUES(profile_picture), profile_picture)")) {
                updateUser.setString(1, name);
                updateUser.setString(2, email);
                updateUser.executeUpdate();
                upsert.setString(1, email);
                upsert.setString(2, gender);
                upsert.setString(3, phone);
                upsert.setString(4, picture);
                upsert.executeUpdate();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Profile saved successfully");
            write(res, HttpServletResponse.SC_OK, response);
        } catch (IllegalArgumentException e) {
            write(res, HttpServletResponse.SC_BAD_REQUEST, error(e.getMessage()));
        } catch (Exception e) {
            write(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error("Unable to save profile"));
        }
    }

    private String currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (String) session.getAttribute("user");
    }

    private String safe(String value) { return value == null ? "" : value.trim(); }

    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }

    private void write(HttpServletResponse res, int status, Map<String, Object> body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        try (PrintWriter out = res.getWriter()) { out.print(GSON.toJson(body)); }
    }
}
