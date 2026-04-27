package com.ptquiz.core;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class APIHelper {
    public static final String BASE_URL = "http://localhost/project-tracnghiem/client/api/";

    static {
        java.net.CookieManager cookieManager = new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(cookieManager);
    }

    public static APIResponse sendPost(String endpoint, String jsonInputString) {
        // ... (existing code preserved)
        try {
            URL url = new java.net.URI(BASE_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            if (UserSession.token != null && !UserSession.token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + UserSession.token);
            }
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            return getResponse(conn);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIResponse(false, "Lỗi kết nối: " + e.getMessage(), "");
        }
    }

    public static APIResponse sendMultipartPost(String endpoint, java.util.Map<String, String> fields, String fileKey, java.io.File file) {
        String boundary = "---" + System.currentTimeMillis() + "---";
        String LINE_FEED = "\r\n";
        try {
            URL url = new java.net.URI(BASE_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            if (UserSession.token != null && !UserSession.token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + UserSession.token);
            }
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream();
                 java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(os, "UTF-8"), true)) {
                
                for (java.util.Map.Entry<String, String> entry : fields.entrySet()) {
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"").append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.append(entry.getValue()).append(LINE_FEED);
                }

                if (file != null && file.exists()) {
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"" + fileKey + "\"; filename=\"" + file.getName() + "\"").append(LINE_FEED);
                    writer.append("Content-Type: " + java.net.URLConnection.guessContentTypeFromName(file.getName())).append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.flush();

                    java.io.FileInputStream fis = new java.io.FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                    fis.close();
                    writer.append(LINE_FEED);
                }

                writer.append("--" + boundary + "--").append(LINE_FEED);
            }

            return getResponse(conn);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIResponse(false, "Lỗi kết nối: " + e.getMessage(), "");
        }
    }

    private static APIResponse getResponse(HttpURLConnection conn) throws java.io.IOException {
        int code = conn.getResponseCode();
        Scanner scanner;
        if (code >= 200 && code < 300) {
            scanner = new Scanner(conn.getInputStream(), "UTF-8");
        } else {
            if (conn.getErrorStream() != null) {
                scanner = new Scanner(conn.getErrorStream(), "UTF-8");
            } else {
                return new APIResponse(false, "Lỗi HTTP: " + code, "");
            }
        }

        String responseStr = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        scanner.close();

        if (code >= 200 && code < 300) {
            String message = extractJsonValue(responseStr, "message");
            if (message.isEmpty()) {
                if (responseStr.contains("\"success\":true") || responseStr.contains("thành công")) {
                    message = "Thao tác thành công!";
                } else {
                    message = "Máy chủ đã xử lý yêu cầu.";
                }
            }
            return new APIResponse(true, message, responseStr);
        } else {
            String error = extractJsonValue(responseStr, "error");
            if (error.isEmpty())
                error = "Có lỗi xảy ra, mã HTTP: " + code;
            return new APIResponse(false, error, responseStr);
        }
    }

    public static String extractJsonValue(String json, String key) {
        if (json == null || json.isEmpty()) return "";
        
        // Try multiple formats: "key":value, "key" :value, "key": value
        String[] patterns = {
            "\"" + key + "\":",
            "\"" + key + "\" :",
            "\"" + key + "\": "
        };
        
        int index = -1;
        int patternLen = 0;
        for (String p : patterns) {
            index = json.indexOf(p);
            if (index != -1) {
                patternLen = p.length();
                break;
            }
        }
        
        if (index == -1) return "";

        int afterColon = index + patternLen;
        // Skip whitespace
        while (afterColon < json.length() && (Character.isWhitespace(json.charAt(afterColon)) || json.charAt(afterColon) == ' ')) {
            afterColon++;
        }

        if (afterColon >= json.length()) return "";

        if (json.charAt(afterColon) == '"') {
            // String value
            int start = afterColon + 1;
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) {
                    sb.append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return unescapeUnicode(sb.toString().replace("\\/", "/"));
        } else if (json.charAt(afterColon) == '[') {
            // Array value: Take until the matching closing bracket
            int start = afterColon;
            int depth = 0;
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) return json.substring(start, i + 1);
                }
            }
            return "";
        } else {
            // Numeric, Boolean, or Null value
            int end = afterColon;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && !Character.isWhitespace(json.charAt(end))) {
                end++;
            }
            return json.substring(afterColon, end).trim();
        }
    }

    public static String unescapeUnicode(String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0, len = str.length();
        while (i < len) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = str.charAt(i + 1);
                if (next == 'u' && i + 5 < len) {
                    try {
                        int code = Integer.parseInt(str.substring(i + 2, i + 6), 16);
                        sb.append((char) code);
                        i += 6;
                        continue;
                    } catch (NumberFormatException e) {
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    public static String escapeJSON(String str) {
        if (str == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch <= 31) {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        for (int k = 0; k < 4 - ss.length(); k++) sb.append('0');
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    public static class APIResponse {
        public boolean success;
        public String message;
        public String rawData;

        public APIResponse(boolean success, String message, String rawData) {
            this.success = success;
            this.message = message;
            this.rawData = rawData;
        }
    }

    public static String sendGet(String endpoint) {
        try {
            URL url = new java.net.URI(BASE_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (UserSession.token != null && !UserSession.token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + UserSession.token);
            }

            int code = conn.getResponseCode();
            Scanner scanner;
            if (code >= 200 && code < 300) {
                scanner = new Scanner(conn.getInputStream(), "UTF-8");
            } else {
                if (conn.getErrorStream() != null) {
                    scanner = new Scanner(conn.getErrorStream(), "UTF-8");
                } else {
                    return "";
                }
            }
            
            String responseStr = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            scanner.close();
            return responseStr;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Split a JSON array string like [{"a":"1"},{"b":"2"}] into individual object strings.
     * Uses depth tracking to correctly handle nested objects.
     */
    public static java.util.List<String> splitJsonArray(String arrayJson) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (arrayJson == null || arrayJson.isEmpty()) return result;

        // Find opening bracket
        int start = arrayJson.indexOf('[');
        if (start == -1) return result;

        int depth = 0;
        int objStart = -1;
        boolean inString = false;
        char prev = 0;

        for (int i = start; i < arrayJson.length(); i++) {
            char c = arrayJson.charAt(i);

            if (c == '"' && prev != '\\') {
                inString = !inString;
            }

            if (!inString) {
                if (c == '{') {
                    if (depth == 0) objStart = i;
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0 && objStart != -1) {
                        result.add(arrayJson.substring(objStart, i + 1));
                        objStart = -1;
                    }
                }
            }
            prev = c;
        }
        return result;
    }
}
