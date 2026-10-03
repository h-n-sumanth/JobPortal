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

public class DeleteJobServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Map<String, String> response = new HashMap<>();
        Gson gson = new Gson();

        HttpSession session = req.getSession(false);
        String adminEmail = session == null ? null : (String) session.getAttribute("admin");
        String adminCompany = session == null ? null : (String) session.getAttribute("adminCompany");

        if (adminEmail == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.put("status", "error");
            response.put("message", "Admin session required");
            out.print(gson.toJson(response));
            return;
        }

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM jobs WHERE id=? AND (created_by_admin=? OR ( ? <> '' AND LOWER(company)=LOWER(?) ))");

            String comp = adminCompany != null ? adminCompany.trim() : "";
            ps.setInt(1, id);
            ps.setString(2, adminEmail);
            ps.setString(3, comp);
            ps.setString(4, comp);

            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                response.put("status", "success");
                response.put("message", "Job deleted successfully");
            } else {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.put("status", "error");
                response.put("message", "You can delete only your company's posted jobs");
            }

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }

        out.print(gson.toJson(response));
        out.flush();
    }
}
