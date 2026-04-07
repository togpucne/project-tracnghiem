<?php

class Api
{
    public static function boot()
    {
        if (session_status() === PHP_SESSION_NONE) {
            session_start();
        }

        header("Content-Type: application/json");
        header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
        header("Pragma: no-cache");
    }

    public static function json($data, $status = 200)
    {
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
