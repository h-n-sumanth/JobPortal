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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 7 * 1024 * 1024)
public class ResumeServlet extends HttpServlet {
    private static final Gson GSON = new Gson();
    private static final int MAX_RESUMES = 4;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String email = currentUser(req);
        if (email == null) { write(res, 401, error("User not logged in")); return; }

        List<Map<String, Object>> resumes = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, custom_name, file_name, stored_name, created_at FROM user_resumes WHERE user_email=? ORDER BY created_at DESC")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> resume = new HashMap<>();
                    resume.put("id", rs.getInt("id"));
                    resume.put("customName", rs.getString("custom_name"));
                    resume.put("fileName", rs.getString("file_name"));
                    resume.put("storedName", rs.getString("stored_name"));
                    resume.put("createdAt", rs.getString("created_at"));
                    resumes.add(resume);
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", resumes);
            response.put("limit", MAX_RESUMES);
            write(res, 200, response);
        } catch (Exception e) {
            write(res, 500, error("Unable to load resumes"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        String email = currentUser(req);
        if (email == null) { write(res, 401, error("User not logged in")); return; }
        if ("delete".equals(req.getParameter("action"))) { delete(req, res, email); return; }

        String customName = req.getParameter("customName");
        if (customName == null || customName.trim().isEmpty()) {
            customName = "Resume";
        } else {
            customName = customName.trim();
        }

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement count = con.prepareStatement("SELECT COUNT(*) FROM user_resumes WHERE user_email=?")) {
                count.setString(1, email);
                try (ResultSet rs = count.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) >= MAX_RESUMES) {
                        write(res, 400, error("You can keep up to " + MAX_RESUMES + " resumes. Remove one first."));
                        return;
                    }
                }
            }
            Part resume = req.getPart("resume");
            String originalName = FileStorageUtil.originalName(resume);
            String storedName = FileStorageUtil.save(resume, getServletContext(), "resume",
                    Set.of(".pdf", ".doc", ".docx"), 5 * 1024 * 1024);
            try (PreparedStatement insert = con.prepareStatement(
                    "INSERT INTO user_resumes(user_email, custom_name, file_name, stored_name) VALUES(?,?,?,?)")) {
                insert.setString(1, email);
                insert.setString(2, customName);
                insert.setString(3, originalName);
                insert.setString(4, storedName);
                insert.executeUpdate();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Resume '" + customName + "' added to your library");
            write(res, 200, response);
        } catch (IllegalArgumentException e) {
            write(res, 400, error(e.getMessage()));
        } catch (Exception e) {
            write(res, 500, error("Unable to upload resume: " + e.getMessage()));
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse res, String email) throws IOException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM user_resumes WHERE id=? AND user_email=?")) {
            ps.setInt(1, Integer.parseInt(req.getParameter("id")));
            ps.setString(2, email);
            if (ps.executeUpdate() == 0) { write(res, 404, error("Resume not found")); return; }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Resume removed");
            write(res, 200, response);
        } catch (Exception e) {
            write(res, 400, error("Unable to remove resume"));
        }
    }

    private String currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (String) session.getAttribute("user");
    }
    private Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error"); response.put("message", message); return response;
    }
    private void write(HttpServletResponse res, int status, Map<String, Object> body) throws IOException {
        res.setStatus(status); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8");
        try (PrintWriter out = res.getWriter()) { out.print(GSON.toJson(body)); }
    }
}
