package com.jobportal;

import com.google.gson.Gson;
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

public class ApplyJobServlet extends HttpServlet {
    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        String email = session == null ? null : (String) session.getAttribute("user");
        if (email == null) { write(res, 401, error("User not logged in")); return; }

        try {
            int jobId = Integer.parseInt(req.getParameter("jobId"));
            int resumeId = Integer.parseInt(req.getParameter("resumeId"));
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement resume = con.prepareStatement(
                         "SELECT stored_name FROM user_resumes WHERE id=? AND user_email=?");
                 PreparedStatement job = con.prepareStatement("SELECT status FROM jobs WHERE id=?");
                 PreparedStatement existing = con.prepareStatement(
                         "SELECT id FROM applications WHERE user_email=? AND job_id=?");
                 PreparedStatement apply = con.prepareStatement(
                         "INSERT INTO applications(user_email, job_id, resume) VALUES(?,?,?)")) {
                resume.setInt(1, resumeId); resume.setString(2, email);
                String storedResume;
                try (ResultSet rs = resume.executeQuery()) {
                    if (!rs.next()) { write(res, 400, error("Choose a resume from your profile")); return; }
                    storedResume = rs.getString(1);
                }
                job.setInt(1, jobId);
                try (ResultSet rs = job.executeQuery()) {
                    if (!rs.next() || !"open".equalsIgnoreCase(rs.getString("status"))) {
                        write(res, 400, error("This job is no longer open for applications")); return;
                    }
                }
                existing.setString(1, email); existing.setInt(2, jobId);
                try (ResultSet rs = existing.executeQuery()) {
                    if (rs.next()) { write(res, 409, error("You have already applied for this job")); return; }
                }
                apply.setString(1, email); apply.setInt(2, jobId); apply.setString(3, storedResume);
                apply.executeUpdate();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success"); response.put("message", "Application submitted successfully");
            write(res, 200, response);
        } catch (NumberFormatException e) {
            write(res, 400, error("Choose a valid job and resume"));
        } catch (Exception e) {
            write(res, 500, error("Unable to submit application"));
        }
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
