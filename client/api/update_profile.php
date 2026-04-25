<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

$conn = Database::connect();

$isMultipart = strpos($_SERVER["CONTENT_TYPE"] ?? "", "multipart/form-data") !== false;
$data = $isMultipart ? $_POST : Api::jsonInput();

$user_id = $_SESSION["user"]["id"];
$ten = trim($data["ten"] ?? "");
$email = trim($data["email"] ?? "");
$matkhau = $data["matkhau"] ?? "";

if ($ten === "" || $email === "") {
    Response::json(["success" => false, "error" => "Ho ten va Email khong duoc de trong"], 400);
}

// Xử lý upload avatar
$avatarPath = null;
if (isset($_FILES['avatar']) && $_FILES['avatar']['error'] === UPLOAD_ERR_OK) {
    $allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/jpg'];
    $fileType = mime_content_type($_FILES['avatar']['tmp_name']);
    $fileSize = $_FILES['avatar']['size'];
    
    if (!in_array($fileType, $allowedTypes)) {
        Response::json(["success" => false, "error" => "Định dạng ảnh không hợp lệ. Chỉ chấp nhận JPG, PNG, GIF"], 400);
    }
    
    if ($fileSize > 2 * 1024 * 1024) { // 2MB limit
        Response::json(["success" => false, "error" => "Dung lượng ảnh tối đa là 2MB"], 400);
    }
    
    $extension = pathinfo($_FILES['avatar']['name'], PATHINFO_EXTENSION);
    $newFileName = 'avatar_' . $user_id . '_' . time() . '.' . $extension;
    $uploadDir = __DIR__ . '/../../server/public/imgs/avatars/';
    
    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0777, true);
    }
    
    if (move_uploaded_file($_FILES['avatar']['tmp_name'], $uploadDir . $newFileName)) {
        $avatarPath = $newFileName;
    } else {
        Response::json(["success" => false, "error" => "Không thể tải lên ảnh đại diện"], 500);
    }
}

$stmt = $conn->prepare("SELECT id_nguoidung FROM nguoidung WHERE email = ? AND id_nguoidung != ?");
$stmt->bind_param("si", $email, $user_id);
$stmt->execute();

if ($stmt->get_result()->num_rows > 0) {
    if ($avatarPath && file_exists(__DIR__ . '/../../server/public/imgs/avatars/' . $avatarPath)) {
        unlink(__DIR__ . '/../../server/public/imgs/avatars/' . $avatarPath);
    }
    Response::json(["success" => false, "error" => "Email nay da duoc su dung boi nguoi khac"], 400);
}

if ($matkhau !== "" && strlen($matkhau) < 6) {
    if ($avatarPath && file_exists(__DIR__ . '/../../server/public/imgs/avatars/' . $avatarPath)) {
        unlink(__DIR__ . '/../../server/public/imgs/avatars/' . $avatarPath);
    }
    Response::json(["success" => false, "error" => "Mat khau moi phai co toi thieu 6 ky tu"], 400);
}

// Lấy avatar cũ để xóa sau khi update thành công
$stmtOld = $conn->prepare("SELECT avatar FROM nguoidung WHERE id_nguoidung = ?");
$stmtOld->bind_param("i", $user_id);
$stmtOld->execute();
$oldAvatar = $stmtOld->get_result()->fetch_assoc()['avatar'] ?? 'default.jpg';
$stmtOld->close();

$updateFields = ["ten = ?", "email = ?"];
$types = "ss";
$params = [$ten, $email];

if ($matkhau !== "") {
    $hashedPassword = password_hash($matkhau, PASSWORD_DEFAULT);
    $updateFields[] = "matkhau = ?";
    $types .= "s";
    $params[] = $hashedPassword;
}

if ($avatarPath !== null) {
    $updateFields[] = "avatar = ?";
    $types .= "s";
    $params[] = $avatarPath;
}

$types .= "i";
$params[] = $user_id;

$sql = "UPDATE nguoidung SET " . implode(", ", $updateFields) . " WHERE id_nguoidung = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param($types, ...$params);

if ($stmt->execute()) {
    // Nếu update thành công và có avatar mới, xóa avatar cũ (nếu không phải mặc định)
    if ($avatarPath !== null && $oldAvatar !== 'default.jpg' && $oldAvatar !== $avatarPath) {
        $oldFilePath = __DIR__ . '/../../server/public/imgs/avatars/' . $oldAvatar;
        if (file_exists($oldFilePath)) {
            unlink($oldFilePath);
        }
    }

    $_SESSION["user"]["name"] = $ten;
    if ($avatarPath !== null) {
        $_SESSION["user"]["avatar"] = $avatarPath;
    }
    Response::json(["success" => true, "message" => "Cap nhat thong tin thanh cong", "avatar" => $avatarPath]);
}

if ($avatarPath && file_exists(__DIR__ . '/../../server/public/imgs/avatars/' . $avatarPath)) {
    unlink(__DIR__ . '/../../server/public/imgs/avatars/' . $avatarPath);
}
Response::json(["success" => false, "error" => "Loi he thong khi cap nhat co so du lieu"], 500);
