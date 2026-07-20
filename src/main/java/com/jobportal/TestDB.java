package com.jobportal;

import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SHOW TABLES");
            System.out.println("Tables in jobportal:");
            while (rs.next()) {
                System.out.println("- " + rs.getString(1));
            }
            
            System.out.println("\nChecking applications table...");
            rs = st.executeQuery("SHOW COLUMNS FROM applications");
            while (rs.next()) {
                System.out.println(rs.getString(1) + " | " + rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
