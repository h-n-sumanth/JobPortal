package com.jobportal.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ApiResponseUtil {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void sendSuccess(HttpServletResponse resp, int status, String message, Object data) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        if (message != null) {
            map.put("message", message);
        }
        if (data != null) {
            map.put("data", data);
        }

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
    }

    public static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> map = new HashMap<>();
        map.put("status", "error");
        map.put("message", message);

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(map));
        out.flush();
    }

    public static <T> T readJsonBody(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return gson.fromJson(sb.toString(), clazz);
    }

    public static JsonObject readJsonObject(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        if (sb.length() == 0) {
            return new JsonObject();
        }
        return gson.fromJson(sb.toString(), JsonObject.class);
    }

    public static String getJsonString(JsonObject json, String key) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }
}
