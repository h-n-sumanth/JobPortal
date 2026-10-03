package com.jobportal;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        String fullSql = "SELECT u.name, u.email, p.gender, p.phone, p.profile_picture, p.dob, p.address, p.skills, p.experience, p.education, p.preferred_role "
                + "FROM users u LEFT JOIN user_profiles p ON u.email=p.user_email WHERE u.email=?";

        String basicSql = "SELECT u.name, u.email, p.gender, p.phone, p.profile_picture "
                + "FROM users u LEFT JOIN user_profiles p ON u.email=p.user_email WHERE u.email=?";

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(fullSql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        response.put("status", "success");
                        response.put("name", safeStr(rs.getString("name")));
                        response.put("email", safeStr(rs.getString("email")));
                        response.put("gender", safeStr(rs.getString("gender")));
                        response.put("phone", safeStr(rs.getString("phone")));
                        response.put("profilePicture", safeStr(rs.getString("profile_picture")));
                        response.put("dob", safeStr(rs.getString("dob")));
                        response.put("address", safeStr(rs.getString("address")));
                        response.put("skills", safeStr(rs.getString("skills")));
                        response.put("experience", safeStr(rs.getString("experience")));
                        response.put("education", safeStr(rs.getString("education")));
                        response.put("preferredRole", safeStr(rs.getString("preferred_role")));
                        write(res, HttpServletResponse.SC_OK, response);
                        return;
                    }
                }
            } catch (SQLException e) {
                // Fallback for older schema missing new profile columns
                try (PreparedStatement ps = con.prepareStatement(basicSql)) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            response.put("status", "success");
                            response.put("name", safeStr(rs.getString("name")));
                            response.put("email", safeStr(rs.getString("email")));
                            response.put("gender", safeStr(rs.getString("gender")));
                            response.put("phone", safeStr(rs.getString("phone")));
                            response.put("profilePicture", safeStr(rs.getString("profile_picture")));
                            response.put("dob", "");
                            response.put("address", "");
                            response.put("skills", "");
                            response.put("experience", "");
                            response.put("education", "");
                            response.put("preferredRole", "");
                            write(res, HttpServletResponse.SC_OK, response);
                            return;
                        }
                    }
                }
            }
            write(res, HttpServletResponse.SC_UNAUTHORIZED, error("User profile not found"));
        } catch (Exception e) {
            write(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error("Unable to load profile: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String email = currentUser(req);
        if (email == null) {
            write(res, HttpServletResponse.SC_UNAUTHORIZED, error("User not logged in"));
            return;
        }

        String name = getParam(req, "name");
        String gender = getParam(req, "gender");
        String phone = getParam(req, "phone");
        String dob = getParam(req, "dob");
        String address = getParam(req, "address");
        String skills = getParam(req, "skills");
        String experience = getParam(req, "experience");
        String education = getParam(req, "education");
        String preferredRole = getParam(req, "preferredRole");

        try (Connection con = DBConnection.getConnection()) {
            // If name is blank, query fallback from users table
            if (name.isEmpty()) {
                try (PreparedStatement uPs = con.prepareStatement("SELECT name FROM users WHERE email=?")) {
                    uPs.setString(1, email);
                    try (ResultSet rs = uPs.executeQuery()) {
                        if (rs.next()) name = safeStr(rs.getString("name"));
                    }
                }
            }

            String picture = null;
            try {
                Part photo = req.getPart("profilePicture");
                if (photo != null && photo.getSize() > 0 && photo.getSubmittedFileName() != null && !photo.getSubmittedFileName().trim().isEmpty()) {
                    picture = FileStorageUtil.save(photo, getServletContext(), "profile-pictures",
                            Set.of(".jpg", ".jpeg", ".png", ".webp"), 5 * 1024 * 1024);
                }
            } catch (Exception ignored) {}

            if (!name.isEmpty()) {
                try (PreparedStatement updateUser = con.prepareStatement("UPDATE users SET name=? WHERE email=?")) {
                    updateUser.setString(1, name);
                    updateUser.setString(2, email);
                    updateUser.executeUpdate();
                }
            }

            // Full upsert
            String fullUpsert = "INSERT INTO user_profiles(user_email, gender, phone, profile_picture, dob, address, skills, experience, education, preferred_role) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?) "
                    + "ON DUPLICATE KEY UPDATE gender=VALUES(gender), phone=VALUES(phone), "
                    + "profile_picture=COALESCE(VALUES(profile_picture), profile_picture), "
                    + "dob=VALUES(dob), address=VALUES(address), skills=VALUES(skills), "
                    + "experience=VALUES(experience), education=VALUES(education), preferred_role=VALUES(preferred_role)";

            try (PreparedStatement upsert = con.prepareStatement(fullUpsert)) {
                upsert.setString(1, email);
                upsert.setString(2, gender);
                upsert.setString(3, phone);
                upsert.setString(4, picture);
                upsert.setString(5, dob);
                upsert.setString(6, address);
                upsert.setString(7, skills);
                upsert.setString(8, experience);
                upsert.setString(9, education);
                upsert.setString(10, preferredRole);
                upsert.executeUpdate();
            } catch (SQLException ex) {
                // Fallback to basic upsert for older schemas
                String basicUpsert = "INSERT INTO user_profiles(user_email, gender, phone, profile_picture) VALUES(?,?,?,?) "
                        + "ON DUPLICATE KEY UPDATE gender=VALUES(gender), phone=VALUES(phone), "
                        + "profile_picture=COALESCE(VALUES(profile_picture), profile_picture)";
                try (PreparedStatement basicPs = con.prepareStatement(basicUpsert)) {
                    basicPs.setString(1, email);
                    basicPs.setString(2, gender);
                    basicPs.setString(3, phone);
                    basicPs.setString(4, picture);
                    basicPs.executeUpdate();
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Profile saved successfully!");
            write(res, HttpServletResponse.SC_OK, response);
        } catch (IllegalArgumentException e) {
            write(res, HttpServletResponse.SC_BAD_REQUEST, error(e.getMessage()));
        } catch (Exception e) {
            write(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error("Unable to save profile: " + e.getMessage()));
        }
    }

    private String getParam(HttpServletRequest req, String paramName) {
        String val = req.getParameter(paramName);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }
        try {
            Part part = req.getPart(paramName);
            if (part != null) {
                try (InputStream is = part.getInputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    return sb.toString().trim();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (String) session.getAttribute("user");
    }

    private String safeStr(String val) { return val == null ? "" : val; }

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
