<?php
if (!class_exists('Database')) {
    class Database
    {
        private static $host = "localhost";
        private static $username = "root";
        private static $password = "";
        private static $dbname = "tracnghiem";
        private static $port = 3307;

        public static function connect()
        {
            $conn = new mysqli(
                self::$host,
                self::$username,
                self::$password,
                self::$dbname,
                self::$port
            );

            if ($conn->connect_error) {
                http_response_code(500);
                echo json_encode(["error" => "Lỗi kết nối database"]);
                exit;
            }

            $conn->set_charset("utf8mb4");

            return $conn;
        }
    }
}