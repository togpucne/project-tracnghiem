<?php
// server/api/premium_create_payment.php
header('Content-Type: application/json');
require_once __DIR__ . "/../core/Database.php";
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

$amount = isset($_POST['amount']) ? (int)$_POST['amount'] : 0;
$package_type = $_POST['package_type'] ?? 'month'; // month hoặc year
$user_id = $_SESSION['user']['id'] ?? $_SESSION['user']['id_nguoidung'] ?? 0;

if ($user_id == 0) {
    echo json_encode(['success' => false, 'message' => 'Vui lòng đăng nhập']);
    exit;
}

$conn = Database::connect();
$result = $conn->query("SELECT gia FROM goi_premium");
$valid_amounts = [];
while ($row = $result->fetch_assoc()) {
    $valid_amounts[] = (int)$row['gia'];
}

if (!in_array($amount, $valid_amounts)) {
    echo json_encode(['success' => false, 'message' => 'Gói không hợp lệ']);
    exit;
}

// Tạo mã đơn
$order_code = 'DH' . time() . rand(100, 999);

$conn = Database::connect();

// Lưu đơn thanh toán
$sql = "INSERT INTO payments 
        (user_id, order_code, amount, package_type, status, expires_at, created_at) 
        VALUES (?, ?, ?, ?, 'pending', DATE_ADD(NOW(), INTERVAL 15 MINUTE), NOW())";

$stmt = $conn->prepare($sql);
$stmt->bind_param("isss", $user_id, $order_code, $amount, $package_type);

if ($stmt->execute()) {
    echo json_encode([
        'success' => true,
        'order_code' => $order_code,
        'amount' => $amount,
        'qr_content' => "CHUYEN TIEN " . $order_code,
        'message' => 'Tạo đơn thành công'
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Lỗi server: ' . $conn->error]);
}
?>
