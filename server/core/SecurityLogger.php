<?php
require_once __DIR__ . '/../model/Database.php';

class SecurityLogger
{
    /**
     * Log an API request to the database
     */
    public static function logRequest($id_nguoidung = null, $response_code = 200)
    {
        $conn = Database::connect();
        
        $endpoint = $_SERVER['REQUEST_URI'] ?? 'unknown';
        $method = $_SERVER['REQUEST_METHOD'] ?? 'unknown';
        $ip_address = self::getIpAddress();
        $user_agent = $_SERVER['HTTP_USER_AGENT'] ?? 'unknown';
        
        // Collect request parameters (GET and POST)
        $params = array_merge($_GET, $_POST);
        // Remove sensitive data
        if (isset($params['matkhau'])) $params['matkhau'] = '********';
        if (isset($params['password'])) $params['password'] = '********';
        
        $request_params = json_encode($params);
        
        $stmt = $conn->prepare("INSERT INTO api_logs (id_nguoidung, endpoint, method, ip_address, request_params, response_code, user_agent) VALUES (?, ?, ?, ?, ?, ?, ?)");
        $stmt->bind_param("issssis", $id_nguoidung, $endpoint, $method, $ip_address, $request_params, $response_code, $user_agent);
        $stmt->execute();
        $stmt->close();
        $conn->close();
    }

    /**
     * Kiểm tra xem IP có đang bị khóa do thử sai quá nhiều không
     */
    public static function checkBruteForce($ip)
    {
        $conn = Database::connect();
        // Đếm số lần login sai (401) trong 15 phút qua từ IP này
        $stmt = $conn->prepare("SELECT COUNT(*) as attempts FROM api_logs 
                                WHERE ip_address = ? 
                                AND response_code = 401 
                                AND endpoint LIKE '%login.php%'
                                AND created_at > DATE_SUB(NOW(), INTERVAL 15 MINUTE)");
        $stmt->bind_param("s", $ip);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        $conn->close();

        return ($result['attempts'] ?? 0) >= 5; // Khóa nếu sai từ 5 lần trở lên
    }

    /**
     * Get the real IP address of the client
     */
    public static function getIpAddress()
    {
        if (!empty($_SERVER['HTTP_CLIENT_IP'])) {
            return $_SERVER['HTTP_CLIENT_IP'];
        } elseif (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
            return $_SERVER['HTTP_X_FORWARDED_FOR'];
        } else {
            return $_SERVER['REMOTE_ADDR'] ?? '0.0.0.0';
        }
    }
}
