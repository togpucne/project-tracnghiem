<?php
// controller/giangvien/cauhoi.controller.php
require_once __DIR__ . '/../../model/giangvien/cauhoi.model.php';
$cauhoiModel = new CauHoiModel();

// Sửa lại cách lấy action để khớp với URL act=cauhoi-list
$act = isset($_GET['act']) ? $_GET['act'] : '';
$parts = explode('-', $act);
$action = isset($parts[1]) ? $parts[1] : 'list';

$id_baithi = isset($_GET['id_baithi']) ? (int)$_GET['id_baithi'] : (isset($_POST['id_baithi']) ? (int)$_POST['id_baithi'] : 0);
$ten_baithi = isset($_GET['ten_baithi']) ? urldecode($_GET['ten_baithi']) : '';

switch ($action) {
    case 'list':
        $baithi = $cauhoiModel->getBaiThiInfo($id_baithi);
        $list_cauhoi = $cauhoiModel->getByBaiThi($id_baithi);
        $view = "views/giangvien/cauhoi/list.php";
        break;

    case 'add':
    case 'edit':
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            $id_cauhoi = isset($_POST['id_cauhoi']) ? (int)$_POST['id_cauhoi'] : 0;
            $noidungcauhoi = trim($_POST['noidungcauhoi']);
            $dokho = $_POST['dokho'];
            $options = $_POST['option'] ?? [];
            $is_correct_indexes = $_POST['is_correct'] ?? []; // Mảng chứa các index: [0, 2]

            $dapan_list = [];
            foreach ($options as $index => $noidung) {
                if (!empty(trim($noidung))) {
                    $dapan_list[] = [
                        'noidung' => trim($noidung),
                        // Quan trọng: Kiểm tra xem index này có được tích không
                        'dapandung' => in_array($index, $is_correct_indexes) ? 1 : 0
                    ];
                }
            }

            if ($action == 'add') {
                $result = $cauhoiModel->create($id_baithi, $noidungcauhoi, $dokho, $dapan_list);
            } else {
                $result = $cauhoiModel->update($id_cauhoi, $noidungcauhoi, $dokho, $dapan_list);
            }

            if ($result['success']) $_SESSION['success'] = ($action == 'add' ? 'Thêm' : 'Sửa') . ' thành công';
            else $_SESSION['error'] = $result['message'];

            header("Location: index.php?act=cauhoi-list&id_baithi=$id_baithi");
            exit;
        }
        break;

    case 'delete':
        $id_cauhoi = (int)$_GET['id'];
        $result = $cauhoiModel->delete($id_cauhoi);
        $_SESSION[$result['success'] ? 'success' : 'error'] = $result['success'] ? 'Xóa thành công' : $result['message'];
        header("Location: index.php?act=cauhoi-list&id_baithi=$id_baithi");
        exit;
}
