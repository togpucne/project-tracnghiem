<?php
require_once __DIR__ . "/../model/Database.php";

class Api
{
    public static function boot()
    {
        if (session_status() === PHP_SESSION_NONE) {
            @session_start();
        }

        // Clear any previous output (warnings, notices) that might break JSON
        if (ob_get_level() > 0) ob_clean();
        else ob_start();

        header("Content-Type: application/json");
        header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
        header("Pragma: no-cache");
    }

    public static function json($data, $status = 200)
    {
        if (ob_get_level() > 0) ob_clean();
        http_response_code($status);
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        exit;
    }

    public static function requireMethod($method)
    {
        if (($_SERVER["REQUEST_METHOD"] ?? "GET") !== strtoupper($method)) {
            self::json(["error" => "Method Not Allowed"], 405);
        }
    }

    public static function requireMethods(array $methods)
    {
        $requestMethod = strtoupper($_SERVER["REQUEST_METHOD"] ?? "GET");
        $allowed = array_map("strtoupper", $methods);

        if (!in_array($requestMethod, $allowed, true)) {
            self::json([
                "error" => "Method Not Allowed",
                "allowed_methods" => $allowed,
            ], 405);
        }
    }

    public static function requireLogin()
    {
        if (!isset($_SESSION["user"])) {
            self::json(["error" => "Unauthorized"], 401);
        }

        // Check if user is still active
        $id = $_SESSION["user"]["id_nguoidung"];
        $conn = Database::connect();
        $stmt = $conn->prepare("SELECT trangthai FROM nguoidung WHERE id_nguoidung = ?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $result = $stmt->get_result();
        $userStatus = $result->fetch_assoc();
        $conn->close();

        if (!$userStatus || $userStatus['trangthai'] !== 'active') {
            session_destroy();
            self::json(["error" => "Tài khoản của bạn đã bị khóa hoặc không tồn tại. Vui lòng liên hệ quản trị viên."], 403);
        }

        return $_SESSION["user"];
    }

    public static function requireRole(array $roles)
    {
        $user = self::requireLogin();
        $role = $user["vaitro"] ?? "";

        if (!in_array($role, $roles, true)) {
            self::json(["error" => "Forbidden"], 403);
        }

        return $user;
    }

    public static function detectRoute()
    {
        if (!empty($_GET["route"])) {
            return trim((string) $_GET["route"], "/");
        }

        $uriPath = parse_url($_SERVER["REQUEST_URI"] ?? "", PHP_URL_PATH) ?? "";
        $scriptName = str_replace("\\", "/", $_SERVER["SCRIPT_NAME"] ?? "");
        $baseDir = rtrim(str_replace("\\", "/", dirname($scriptName)), "/");

        if ($baseDir !== "" && str_starts_with($uriPath, $baseDir)) {
            $uriPath = substr($uriPath, strlen($baseDir));
        }

        return trim($uriPath, "/");
    }

    public static function jsonInput()
    {
        $raw = file_get_contents("php://input");
        $data = json_decode($raw, true);

        if (json_last_error() !== JSON_ERROR_NONE) {
            self::json(["error" => "Invalid JSON payload"], 400);
        }

        return is_array($data) ? $data : [];
    }
}
