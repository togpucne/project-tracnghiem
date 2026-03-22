<?php
// model/cauhoi.model.php
require_once __DIR__ . '/../Database.php';  // Sửa đường dẫn này

class CauHoiModel
{
    private $conn;

    public function __construct()
    {
        $this->conn = Database::connect();
    }

    /**
     * Lấy danh sách câu hỏi theo id_baithi
     */
    public function getByBaiThi($id_baithi)
    {
        $sql = "SELECT * FROM cauhoi WHERE id_baithi = ? ORDER BY id_cauhoi DESC";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_baithi);
        $stmt->execute();
        $result = $stmt->get_result();

        $list = [];
        while ($row = $result->fetch_assoc()) {
            // Lấy đáp án cho từng câu hỏi
            $row['dapan'] = $this->getDapAnByCauHoi($row['id_cauhoi']);
            $list[] = $row;
        }
        return $list;
    }

    /**
     * Lấy đáp án theo id_cauhoi
     */
    public function getDapAnByCauHoi($id_cauhoi)
    {
        $sql = "SELECT * FROM dapan WHERE id_cauhoi = ?";
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
    public function getById($id_cauhoi)
    {
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
     * Thêm câu hỏi mới (kèm đáp án)
     */
    public function create($id_baithi, $noidungcauhoi, $dokho, $dapan_list)
    {
        // Kiểm tra kết nối
        if (!$this->conn) {
            return ['success' => false, 'message' => 'Lỗi kết nối database'];
        }

        // Bắt đầu transaction
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

            // Commit transaction
            $this->conn->commit();
            return ['success' => true, 'id' => $id_cauhoi];
        } catch (Exception $e) {
            // Rollback nếu có lỗi
            $this->conn->rollback();
            return ['success' => false, 'message' => $e->getMessage()];
        }
    }

    /**
     * Cập nhật câu hỏi
     */
    public function update($id_cauhoi, $noidungcauhoi, $dokho, $dapan_list)
    {
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
    public function delete($id_cauhoi)
    {
        $sql = "DELETE FROM cauhoi WHERE id_cauhoi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_cauhoi);

        if ($stmt->execute()) {
            return ['success' => true];
        }
        return ['success' => false, 'message' => $stmt->error];
    }

    /**
     * Đếm số câu hỏi của bài thi
     */
    public function countByBaiThi($id_baithi)
    {
        $sql = "SELECT COUNT(*) as total FROM cauhoi WHERE id_baithi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_baithi);
        $stmt->execute();
        $result = $stmt->get_result();
        $row = $result->fetch_assoc();
        return $row['total'];
    }

    /**
     * Lấy thông tin bài thi
     */
    public function getBaiThiInfo($id_baithi)
    {
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
