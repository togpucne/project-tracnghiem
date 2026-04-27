<?php
if (!class_exists('Database')) {
    class Database
    {
        private static $host = "localhost";
        private static $username = "root";
        private static $password = "";
        private static $dbname = "tracnghiem";
        private static $port = 3307;

        private static $instance = null;

        public static function connect()
        {
            $needsConnect = false;
            if (self::$instance === null) {
                $needsConnect = true;
            } else {
                try {
                    if (!@self::$instance->ping()) {
                        $needsConnect = true;
                    }
                } catch (Exception $e) {
                    $needsConnect = true;
                }
            }

            if ($needsConnect) {
                self::$instance = new mysqli(
                    self::$host,
                    self::$username,
                    self::$password,
                    self::$dbname,
                    self::$port
                );

                if (self::$instance->connect_error) {
                    http_response_code(500);
                    echo json_encode(["error" => "Lỗi kết nối database: " . self::$instance->connect_error]);
                    exit;
                }
                self::$instance->set_charset("utf8mb4");
            }

            return self::$instance;
        }
    }
}
