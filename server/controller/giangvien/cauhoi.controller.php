<?php
// controller/giangvien/cauhoi.controller.php
require_once __DIR__ . '/../../model/giangvien/cauhoi.model.php';

$cauhoiModel = new CauHoiModel();

// Lấy action từ URL
$action = isset($_GET['action']) ? $_GET['action'] : 'list';
$id_baithi = isset($_GET['id_baithi']) ? (int)$_GET['id_baithi'] : 0;
$ten_baithi = isset($_GET['ten_baithi']) ? urldecode($_GET['ten_baithi']) : '';

switch ($action) {
    case 'list':
        // Hiển thị danh sách câu hỏi
        if (!$id_baithi) {
            $_SESSION['error'] = 'Không tìm thấy bài thi';
            header('Location: index.php?act=quanly-baithi');
            exit;
        }

        $baithi = $cauhoiModel->getBaiThiInfo($id_baithi);

        // TẠM THỜI BỎ QUA KIỂM TRA QUYỀN (sau này thêm cột id_nguoidung vào bảng monhoc thì bỏ comment)
        /*
        $user_id = $_SESSION['user']['id_nguoidung'];
        if ($baithi['id_nguoidung'] != $user_id) {
            $_SESSION['error'] = 'Bạn không có quyền xem bài thi này';
            header('Location: index.php?act=quanly-baithi');
            exit;
        }
        */

        $list_cauhoi = $cauhoiModel->getByBaiThi($id_baithi);

        // Gán biến cho view
        $title = "Quản lý câu hỏi: " . htmlspecialchars($ten_baithi);
        $view = "views/giangvien/cauhoi/list.php";
        break;

    case 'add':
        // Thêm câu hỏi
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $id_baithi = (int)$_POST['id_baithi'];
            $noidungcauhoi = trim($_POST['noidungcauhoi']);
            $dokho = $_POST['dokho'];

            // Xử lý đáp án
            $dapan_list = [];
            $options = $_POST['option'] ?? [];
            $is_correct = $_POST['is_correct'] ?? [];

            foreach ($options as $index => $noidung) {
                if (!empty(trim($noidung))) {
                    $dapan_list[] = [
                        'noidung' => trim($noidung),
                        'dapandung' => isset($is_correct[$index]) ? 1 : 0
                    ];
                }
            }

            // Kiểm tra có ít nhất 2 đáp án và 1 đáp án đúng
            if (count($dapan_list) < 2) {
                $_SESSION['error'] = 'Vui lòng nhập ít nhất 2 đáp án';
                header("Location: index.php?act=cauhoi-list&id_baithi=$id_baithi&ten_baithi=" . urlencode($ten_baithi));
                exit;
            }

            $hasCorrect = false;
            foreach ($dapan_list as $d) {
                if ($d['dapandung'] == 1) $hasCorrect = true;
            }

            if (!$hasCorrect) {
                $_SESSION['error'] = 'Vui lòng chọn ít nhất 1 đáp án đúng';
                header("Location: index.php?act=cauhoi-list&id_baithi=$id_baithi&ten_baithi=" . urlencode($ten_baithi));
                exit;
            }

            $result = $cauhoiModel->create($id_baithi, $noidungcauhoi, $dokho, $dapan_list);

            if ($result['success']) {
                $_SESSION['success'] = 'Thêm câu hỏi thành công';
            } else {
                $_SESSION['error'] = 'Lỗi: ' . $result['message'];
            }

            // Lấy lại tên bài thi để redirect
            $baithi_info = $cauhoiModel->getBaiThiInfo($id_baithi);
            $ten_baithi = $baithi_info['ten_baithi'];
            header("Location: index.php?act=cauhoi-list&id_baithi=$id_baithi&ten_baithi=" . urlencode($ten_baithi));
            exit;
        }
        break;

    case 'edit':
        // Sửa câu hỏi
        $id_cauhoi = isset($_GET['id']) ? (int)$_GET['id'] : 0;

        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $id_cauhoi = (int)$_POST['id_cauhoi'];
            $id_baithi = (int)$_POST['id_baithi'];
            $noidungcauhoi = trim($_POST['noidungcauhoi']);
            $dokho = $_POST['dokho'];

            $dapan_list = [];
            $options = $_POST['option'] ?? [];
            $is_correct = $_POST['is_correct'] ?? [];

            foreach ($options as $index => $noidung) {
                if (!empty(trim($noidung))) {
                    $dapan_list[] = [
                        'noidung' => trim($noidung),
                        'dapandung' => isset($is_correct[$index]) ? 1 : 0
                    ];
                }
            }

            $result = $cauhoiModel->update($id_cauhoi, $noidungcauhoi, $dokho, $dapan_list);

            if ($result['success']) {
                $_SESSION['success'] = 'Cập nhật câu hỏi thành công';
            } else {
                $_SESSION['error'] = 'Lỗi: ' . $result['message'];
            }

            // Lấy lại tên bài thi để redirect
            $baithi_info = $cauhoiModel->getBaiThiInfo($id_baithi);
            $ten_baithi = $baithi_info['ten_baithi'];
            header("Location: index.php?act=cauhoi-list&id_baithi=$id_baithi&ten_baithi=" . urlencode($ten_baithi));
            exit;
        }

        // GET: hiển thị form sửa
        $cauhoi = $cauhoiModel->getById($id_cauhoi);
        if (!$cauhoi) {
            $_SESSION['error'] = 'Không tìm thấy câu hỏi';
            header('Location: index.php?act=quanly-baithi');
            exit;
        }

        // TẠM THỜI BỎ QUA KIỂM TRA QUYỀN
        /*
        $baithi = $cauhoiModel->getBaiThiInfo($cauhoi['id_baithi']);
        $user_id = $_SESSION['user']['id_nguoidung'];
        if ($baithi['id_nguoidung'] != $user_id) {
            $_SESSION['error'] = 'Bạn không có quyền sửa câu hỏi này';
            header('Location: index.php?act=quanly-baithi');
            exit;
        }
        */

        $baithi = $cauhoiModel->getBaiThiInfo($cauhoi['id_baithi']);
        $ten_baithi = $baithi['ten_baithi'];
        $id_baithi = $cauhoi['id_baithi'];

        $title = "Sửa câu hỏi";
        $view = "views/giangvien/cauhoi/edit.php";
        break;

    case 'delete':
        // Xóa câu hỏi
        $id_cauhoi = isset($_GET['id']) ? (int)$_GET['id'] : 0;
        $id_baithi = isset($_GET['id_baithi']) ? (int)$_GET['id_baithi'] : 0;

        if ($id_cauhoi) {
            // TẠM THỜI BỎ QUA KIỂM TRA QUYỀN
            $result = $cauhoiModel->delete($id_cauhoi);
            if ($result['success']) {
                $_SESSION['success'] = 'Xóa câu hỏi thành công';
            } else {
                $_SESSION['error'] = 'Lỗi xóa: ' . $result['message'];
            }
        }

        // Lấy lại tên bài thi để redirect
        $baithi_info = $cauhoiModel->getBaiThiInfo($id_baithi);
        $ten_baithi = $baithi_info['ten_baithi'];
        header("Location: index.php?act=cauhoi-list&id_baithi=$id_baithi&ten_baithi=" . urlencode($ten_baithi));
        exit;
        break;

    default:
        header('Location: index.php?act=quanly-baithi');
        exit;
}
