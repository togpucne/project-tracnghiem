<?php
require_once __DIR__ . '/../core/Api.php';
require_once __DIR__ . '/../model/Database.php';
require_once __DIR__ . '/../model/admin/goipremium.model.php';

// Đảm bảo không có output thừa làm hỏng JSON
if (ob_get_level() > 0) ob_clean();

$db = Database::connect();
$model = new GoiPremiumModel($db);

$method = $_SERVER['REQUEST_METHOD'];

try {
    if ($method === 'GET') {
        $id = isset($_GET['id']) ? (int)$_GET['id'] : 0;
        if ($id > 0) {
            $data = $model->getById($id);
            Api::json(['success' => true, 'data' => $data]);
        } else {
            $data = $model->getAll();
            // DEBUG: Nếu mảng rỗng, ta vẫn trả về success true nhưng data rỗng
            Api::json(['success' => true, 'data' => $data]);
        }
    } 
    elseif ($method === 'POST') {
        $input = Api::jsonInput();
        if (isset($input['copy_id']) && $input['copy_id'] > 0) {
            $success = $model->copy($input['copy_id']);
            Api::json(['success' => $success, 'message' => $success ? 'Sao chép gói thành công' : 'Không thể sao chép gói']);
        } else {
            // Validation cho thêm mới
            if (empty($input['ten_goi']) || empty($input['gia']) || empty($input['thoihan_ngay']) || empty($input['mieuta'])) {
                Api::json(['success' => false, 'message' => 'Vui lòng điền đầy đủ thông tin (Tên, Giá, Thời hạn, Miêu tả)']);
            }
            if ($input['gia'] <= 10000) {
                Api::json(['success' => false, 'message' => 'Giá gói phải lớn hơn 10.000 VNĐ']);
            }
            if ($input['thoihan_ngay'] <= 0) {
                Api::json(['success' => false, 'message' => 'Thời hạn phải lớn hơn 0 ngày']);
            }
            if (!$model->checkUniqueName($input['ten_goi'])) {
                Api::json(['success' => false, 'message' => 'Tên gói này đã tồn tại, vui lòng chọn tên khác']);
            }

            $success = $model->save($input);
            Api::json(['success' => $success, 'message' => $success ? 'Lưu gói thành công' : 'Không thể lưu gói']);
        }
    } 
    elseif ($method === 'PATCH') {
        $data = Api::jsonInput();
        if (empty($data['ten_goi']) || empty($data['gia']) || empty($data['thoihan_ngay']) || empty($data['mieuta'])) {
            Api::json(['success' => false, 'message' => 'Vui lòng điền đầy đủ thông tin (Tên, Giá, Thời hạn, Miêu tả)']);
        }

        if ($data['gia'] <= 10000) {
            Api::json(['success' => false, 'message' => 'Giá gói phải lớn hơn 10.000 VNĐ']);
        }

        if ($data['thoihan_ngay'] <= 0) {
            Api::json(['success' => false, 'message' => 'Thời hạn phải lớn hơn 0 ngày']);
        }

        $id = isset($data['id_goi']) ? (int)$data['id_goi'] : 0;
        if (!$model->checkUniqueName($data['ten_goi'], $id)) {
            Api::json(['success' => false, 'message' => 'Tên gói này đã tồn tại, vui lòng chọn tên khác']);
        }

        $success = $model->save($data);
        Api::json(['success' => $success, 'message' => $success ? 'Cập nhật thành công' : 'Không thể cập nhật']);
    } 
    elseif ($method === 'DELETE') {
        $input = Api::jsonInput();
        $id = isset($input['id_goi']) ? (int)$input['id_goi'] : 0;
        $success = $model->delete($id);
        Api::json(['success' => $success, 'message' => $success ? 'Xóa thành công' : 'Không thể xóa']);
    } 
} catch (Exception $e) {
    Api::json(['success' => false, 'error' => $e->getMessage()], 500);
}
?>
