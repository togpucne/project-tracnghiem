<?php
// model/giangvien/cauhoi.model.php
require_once __DIR__ . '/../Database.php';

class CauHoiModel {
    private $conn;

    public function __construct() {
        $this->conn = Database::connect();
    }

    /**
     * Kiểm tra câu hỏi có bị trùng trong bài thi không
     */
    public function checkDuplicate($id_baithi, $noidungcauhoi, $exclude_id = null) {
        $sql = "SELECT COUNT(*) as count FROM cauhoi 
                WHERE id_baithi = ? AND noidungcauhoi = ?";
        $params = [$id_baithi, $noidungcauhoi];
        $types = "is";
        
        if ($exclude_id) {
            $sql .= " AND id_cauhoi != ?";
            $params[] = $exclude_id;
            $types .= "i";
        }
        
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param($types, ...$params);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        
        return $row['count'] > 0;
    }

    /**
     * Lấy danh sách câu hỏi theo id_baithi
     */
    public function getByBaiThi($id_baithi) {
        $sql = "SELECT * FROM cauhoi WHERE id_baithi = ? ORDER BY id_cauhoi ASC";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_baithi);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $list = [];
        while ($row = $result->fetch_assoc()) {
            $row['dapan'] = $this->getDapAnByCauHoi($row['id_cauhoi']);
            $list[] = $row;
        }
        return $list;
    }

    /**
     * Lấy đáp án theo id_cauhoi
     */
    public function getDapAnByCauHoi($id_cauhoi) {
        $sql = "SELECT * FROM dapan WHERE id_cauhoi = ? ORDER BY id_dapan ASC";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_cauhoi);
        $stmt->execute();
        $result = $stmt->get_result();
        
        $dapan = [];
        while ($row = $result->fetch_assoc()) {
            $dapan[] = $row;
        }
        return $dapan;
    }

    /**
     * Lấy chi tiết 1 câu hỏi (kèm đáp án)
     */
    public function getById($id_cauhoi) {
        $sql = "SELECT * FROM cauhoi WHERE id_cauhoi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_cauhoi);
        $stmt->execute();
        $result = $stmt->get_result();
        $cauhoi = $result->fetch_assoc();
        
        if ($cauhoi) {
            $cauhoi['dapan'] = $this->getDapAnByCauHoi($id_cauhoi);
        }
        return $cauhoi;
    }

    /**
     * Thêm câu hỏi mới (kèm đáp án) - có kiểm tra trùng
     */
    public function create($id_baithi, $noidungcauhoi, $dokho, $dapan_list) {
        // Kiểm tra trùng lặp
        if ($this->checkDuplicate($id_baithi, $noidungcauhoi)) {
            return ['success' => false, 'message' => 'Câu hỏi này đã tồn tại trong bài thi!'];
        }
        
        $this->conn->begin_transaction();

        try {
            // Thêm câu hỏi
            $sql = "INSERT INTO cauhoi (id_baithi, noidungcauhoi, dokho, ngaytao) 
                    VALUES (?, ?, ?, NOW())";
            $stmt = $this->conn->prepare($sql);
            $stmt->bind_param("iss", $id_baithi, $noidungcauhoi, $dokho);
            
            if (!$stmt->execute()) {
                throw new Exception("Lỗi thêm câu hỏi: " . $stmt->error);
            }
            
            $id_cauhoi = $this->conn->insert_id;

            // Thêm các đáp án
            foreach ($dapan_list as $dapan) {
                $sql_dapan = "INSERT INTO dapan (id_cauhoi, noidungdapan, dapandung) 
                              VALUES (?, ?, ?)";
                $stmt_dapan = $this->conn->prepare($sql_dapan);
                $stmt_dapan->bind_param("isi", $id_cauhoi, $dapan['noidung'], $dapan['dapandung']);
                
                if (!$stmt_dapan->execute()) {
                    throw new Exception("Lỗi thêm đáp án: " . $stmt_dapan->error);
                }
            }

            $this->conn->commit();
            return ['success' => true, 'id' => $id_cauhoi];

        } catch (Exception $e) {
            $this->conn->rollback();
            return ['success' => false, 'message' => $e->getMessage()];
        }
    }

    /**
     * Cập nhật câu hỏi - có kiểm tra trùng (bỏ qua chính nó)
     */
    public function update($id_cauhoi, $noidungcauhoi, $dokho, $dapan_list) {
        // Lấy id_baithi từ câu hỏi
        $cauhoi = $this->getById($id_cauhoi);
        if (!$cauhoi) {
            return ['success' => false, 'message' => 'Không tìm thấy câu hỏi'];
        }
        
        // Kiểm tra trùng lặp (bỏ qua câu hỏi hiện tại)
        if ($this->checkDuplicate($cauhoi['id_baithi'], $noidungcauhoi, $id_cauhoi)) {
            return ['success' => false, 'message' => 'Câu hỏi này đã tồn tại trong bài thi!'];
        }
        
        $this->conn->begin_transaction();

        try {
            // Cập nhật câu hỏi
            $sql = "UPDATE cauhoi SET noidungcauhoi = ?, dokho = ? WHERE id_cauhoi = ?";
            $stmt = $this->conn->prepare($sql);
            $stmt->bind_param("ssi", $noidungcauhoi, $dokho, $id_cauhoi);
            
            if (!$stmt->execute()) {
                throw new Exception("Lỗi cập nhật câu hỏi");
            }

            // Xóa đáp án cũ
            $sql_del = "DELETE FROM dapan WHERE id_cauhoi = ?";
            $stmt_del = $this->conn->prepare($sql_del);
            $stmt_del->bind_param("i", $id_cauhoi);
            $stmt_del->execute();

            // Thêm đáp án mới
            foreach ($dapan_list as $dapan) {
                $sql_dapan = "INSERT INTO dapan (id_cauhoi, noidungdapan, dapandung) 
                              VALUES (?, ?, ?)";
                $stmt_dapan = $this->conn->prepare($sql_dapan);
                $stmt_dapan->bind_param("isi", $id_cauhoi, $dapan['noidung'], $dapan['dapandung']);
                
                if (!$stmt_dapan->execute()) {
                    throw new Exception("Lỗi thêm đáp án mới");
                }
            }

            $this->conn->commit();
            return ['success' => true];

        } catch (Exception $e) {
            $this->conn->rollback();
            return ['success' => false, 'message' => $e->getMessage()];
        }
    }

    /**
     * Xóa câu hỏi (cascade sẽ tự xóa đáp án)
     */
    public function delete($id_cauhoi) {
        $sql = "DELETE FROM cauhoi WHERE id_cauhoi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_cauhoi);
        
        if ($stmt->execute()) {
            return ['success' => true];
        }
        return ['success' => false, 'message' => $stmt->error];
    }

    /**
     * Lấy thông tin bài thi
     */
    public function getBaiThiInfo($id_baithi) {
        $sql = "SELECT bt.*, mh.tenmonhoc 
                FROM baithi bt 
                LEFT JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc 
                WHERE bt.id_baithi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_baithi);
        $stmt->execute();
        $result = $stmt->get_result();
        return $result->fetch_assoc();
    }
}
?>