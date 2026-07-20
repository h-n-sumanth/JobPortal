package com.jobportal;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import com.google.gson.Gson;

public class DeleteJobServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        Map<String, String> response = new HashMap<>();
        Gson gson = new Gson();

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Connection con = DBConnection.getConnection();
            
            // Delete job
            PreparedStatement ps = con.prepareStatement("DELETE FROM jobs WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();

            response.put("status", "success");
            response.put("message", "Job deleted successfully");

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("status", "error");
            response.put("message", "Server error: " + e.getMessage());
        }
        
        out.print(gson.toJson(response));
        out.flush();
    }
}
