<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";


$conn = Database::connect();
$data = Api::jsonInput();

$user_id = $_SESSION["user"]["id"];
$ten = trim($data["ten"] ?? "");
$email = trim($data["email"] ?? "");
$matkhau = $data["matkhau"] ?? "";

if ($ten === "" || $email === "") {
    Response::json(["success" => false, "error" => "Ho ten va Email khong duoc de trong"], 400);
}

$stmt = $conn->prepare("SELECT id_nguoidung FROM nguoidung WHERE email = ? AND id_nguoidung != ?");
$stmt->bind_param("si", $email, $user_id);
$stmt->execute();

if ($stmt->get_result()->num_rows > 0) {
    Response::json(["success" => false, "error" => "Email nay da duoc su dung boi nguoi khac"], 400);
}

if ($matkhau !== "") {
    if (strlen($matkhau) < 6) {
        Response::json(["success" => false, "error" => "Mat khau moi phai co toi thieu 6 ky tu"], 400);
    }

    $hashedPassword = password_hash($matkhau, PASSWORD_DEFAULT);
    $stmt = $conn->prepare("UPDATE nguoidung SET ten = ?, email = ?, matkhau = ? WHERE id_nguoidung = ?");
    $stmt->bind_param("sssi", $ten, $email, $hashedPassword, $user_id);
} else {
    $stmt = $conn->prepare("UPDATE nguoidung SET ten = ?, email = ? WHERE id_nguoidung = ?");
    $stmt->bind_param("ssi", $ten, $email, $user_id);
}

if ($stmt->execute()) {
    $_SESSION["user"]["name"] = $ten;
    Response::json(["success" => true, "message" => "Cap nhat thong tin thanh cong"]);
}

Response::json(["success" => false, "error" => "Loi he thong khi cap nhat co so du lieu"], 500);

