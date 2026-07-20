package com.jobportal;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewJobsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<String> filters = new ArrayList<>();
        List<String> values = new ArrayList<>();
        addFilter(req, "category", "category", filters, values);
        addFilter(req, "experience", "experience_level", filters, values);
        addFilter(req, "location", "location", filters, values);
        String keyword = value(req.getParameter("keyword"));
        if (keyword != null) { filters.add("(title LIKE ? OR company LIKE ? OR description LIKE ?)"); values.add("%" + keyword + "%"); values.add("%" + keyword + "%"); values.add("%" + keyword + "%"); }

        String sql = "SELECT id,title,company,location,salary,description,status,category,experience_level,job_type FROM jobs"
                + (filters.isEmpty() ? "" : " WHERE " + String.join(" AND ", filters)) + " ORDER BY id DESC";
        List<Map<String, Object>> jobs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) ps.setString(i + 1, values.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> job = new HashMap<>();
                    job.put("id", rs.getInt("id")); job.put("title", rs.getString("title"));
                    job.put("company", rs.getString("company")); job.put("location", rs.getString("location"));
                    job.put("salary", rs.getString("salary")); job.put("description", rs.getString("description"));
                    job.put("status", rs.getString("status")); job.put("category", rs.getString("category"));
                    job.put("experience", rs.getString("experience_level")); job.put("jobType", rs.getString("job_type"));
                    jobs.add(job);
                }
            }
            Map<String, Object> response = new HashMap<>(); response.put("status", "success"); response.put("data", jobs);
            write(res, 200, response);
        } catch (Exception e) { write(res, 500, error("Unable to load jobs. Run schema.sql to add job filters.")); }
    }

    private void addFilter(HttpServletRequest req, String parameter, String column, List<String> filters, List<String> values) {
        String value = value(req.getParameter(parameter));
        if (value != null && !"all".equalsIgnoreCase(value)) { filters.add(column + "=?"); values.add(value); }
    }
    private String value(String input) { return input == null || input.trim().isEmpty() ? null : input.trim(); }
    private Map<String, Object> error(String message) { Map<String, Object> r = new HashMap<>(); r.put("status", "error"); r.put("message", message); return r; }
    private void write(HttpServletResponse res, int status, Map<String, Object> body) throws IOException { res.setStatus(status); res.setContentType("application/json"); res.setCharacterEncoding("UTF-8"); try (PrintWriter out = res.getWriter()) { out.print(new Gson().toJson(body)); } }
}
