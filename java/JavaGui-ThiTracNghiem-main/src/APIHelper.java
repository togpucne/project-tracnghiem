import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class APIHelper {
    public static final String BASE_URL = "http://localhost/tracnghiem-project/client/api/";

    static {
        java.net.CookieManager cookieManager = new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(cookieManager);
    }

    public static APIResponse sendPost(String endpoint, String jsonInputString) {
        try {
            URL url = new java.net.URI(BASE_URL + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

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

            // Cơ bản parse JSON thủ công cho response {"message": "..."} hoặc {"error":
            // "..."}
            if (code >= 200 && code < 300) {
                String message = extractJsonValue(responseStr, "message");
                if (message.isEmpty() && responseStr.contains("thành công"))
                    message = "Thành công!";
                return new APIResponse(true, message, responseStr);
            } else {
                String error = extractJsonValue(responseStr, "error");
                if (error.isEmpty())
                    error = "Có lỗi xảy ra, mã HTTP: " + code;
                return new APIResponse(false, error, responseStr);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new APIResponse(false, "Lỗi kết nối: " + e.getMessage(), "");
        }
    }

    public static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int index = json.indexOf(searchKey);
        if (index == -1) {
            searchKey = "\"" + key + "\" :";
            index = json.indexOf(searchKey);
            if (index == -1) {
                searchKey = "\"" + key + "\": ";
                index = json.indexOf(searchKey);
                if (index == -1) return "";
            }
        }

        int afterColon = index + searchKey.length();
        // Skip whitespace
        while (afterColon < json.length() && Character.isWhitespace(json.charAt(afterColon))) {
            afterColon++;
        }

        if (afterColon >= json.length()) return "";

        if (json.charAt(afterColon) == '"') {
            // String value
            int start = afterColon + 1;
            int end = json.indexOf("\"", start);
            if (end == -1) return "";
            String rawValue = json.substring(start, end).replace("\\\"", "\"").replace("\\/", "/");
            return unescapeUnicode(rawValue);
        } else {
            // Numeric, Boolean, or Null value
            int end = afterColon;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}' && json.charAt(end) != ']') {
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
        if (str == null)
            return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
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
}
