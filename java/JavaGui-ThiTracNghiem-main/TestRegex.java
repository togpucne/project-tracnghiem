public class TestRegex {
    public static void main(String[] args) {
        String json = "{\"success":true,"diem":9.0,"socaudung":8,"id_lanthi":12}";
        System.out.println("diem = " + extractBasic(json, "diem"));
        System.out.println("socaudung = " + extractBasic(json, "socaudung"));
    }
    private static String extractBasic(String json, String key) {
        java.util.regex.Matcher ms = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (ms.find()) return ms.group(1);
        java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)").matcher(json);
        if (mn.find()) return mn.group(1).replaceAll("[\\]\\}]", "").trim();
        return "N/A";
    }
}
