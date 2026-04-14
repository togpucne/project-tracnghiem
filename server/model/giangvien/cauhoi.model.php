<?php
require_once __DIR__ . '/../Database.php';

class CauHoiModel
{
    private $conn;

    public function __construct()
    {
        $this->conn = Database::connect();
    }

    public function isBaiThiLocked($id_baithi)
    {
        $sql = "SELECT COUNT(*) as total FROM lanthi WHERE id_baithi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_baithi);
        $stmt->execute();
        $result = $stmt->get_result()->fetch_assoc();

        return (int) ($result['total'] ?? 0) > 0;
    }

    public function checkDuplicate($id_baithi, $noidungcauhoi, $exclude_id = null)
    {
        $sql = "SELECT COUNT(*) as count FROM cauhoi WHERE id_baithi = ? AND noidungcauhoi = ?";
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
        $row = $stmt->get_result()->fetch_assoc();
        return (int) ($row['count'] ?? 0) > 0;
    }

    public function create($id_baithi, $noidungcauhoi, $dokho, $dapan_list)
    {
        if ($this->isBaiThiLocked($id_baithi)) {
            return ['success' => false, 'message' => 'Bài thi này đã có thí sinh làm, không được phép thêm câu hỏi mới.'];
        }

        if ($this->checkDuplicate($id_baithi, $noidungcauhoi)) {
            return ['success' => false, 'message' => 'Câu hỏi này đã tồn tại trong bài thi!'];
        }

        $this->conn->begin_transaction();
        try {
            $sql = "INSERT INTO cauhoi (id_baithi, noidungcauhoi, dokho, ngaytao) VALUES (?, ?, ?, NOW())";
            $stmt = $this->conn->prepare($sql);
            $stmt->bind_param("iss", $id_baithi, $noidungcauhoi, $dokho);
            $stmt->execute();

            $id_cauhoi = $this->conn->insert_id;

            foreach ($dapan_list as $dapan) {
                $sql_d = "INSERT INTO dapan (id_cauhoi, noidungdapan, dapandung) VALUES (?, ?, ?)";
                $stmt_d = $this->conn->prepare($sql_d);
                $stmt_d->bind_param("isi", $id_cauhoi, $dapan['noidung'], $dapan['dapandung']);
                $stmt_d->execute();
            }

            $this->conn->commit();
            return ['success' => true, 'id' => $id_cauhoi];
        } catch (Exception $e) {
            $this->conn->rollback();
            return ['success' => false, 'message' => "Lỗi: " . $e->getMessage()];
        }
    }

    public function update($id_cauhoi, $noidungcauhoi, $dokho, $dapan_list)
    {
        $cauhoi = $this->getById($id_cauhoi);
        if (!$cauhoi) {
            return ['success' => false, 'message' => 'Không tìm thấy câu hỏi'];
        }

        if ($this->isBaiThiLocked($cauhoi['id_baithi'])) {
            return ['success' => false, 'message' => 'Không thể sửa vì bài thi này đã có thí sinh làm.'];
        }

        if ($this->checkDuplicate($cauhoi['id_baithi'], $noidungcauhoi, $id_cauhoi)) {
            return ['success' => false, 'message' => 'Câu hỏi này đã tồn tại trong bài thi!'];
        }

        $this->conn->begin_transaction();
        try {
            $sql = "UPDATE cauhoi SET noidungcauhoi = ?, dokho = ? WHERE id_cauhoi = ?";
            $stmt = $this->conn->prepare($sql);
            $stmt->bind_param("ssi", $noidungcauhoi, $dokho, $id_cauhoi);
            $stmt->execute();

            $stmt_del = $this->conn->prepare("DELETE FROM dapan WHERE id_cauhoi = ?");
            $stmt_del->bind_param("i", $id_cauhoi);
            $stmt_del->execute();

            foreach ($dapan_list as $dapan) {
                $stmt_ins = $this->conn->prepare("INSERT INTO dapan (id_cauhoi, noidungdapan, dapandung) VALUES (?, ?, ?)");
                $stmt_ins->bind_param("isi", $id_cauhoi, $dapan['noidung'], $dapan['dapandung']);
                $stmt_ins->execute();
            }

            $this->conn->commit();
            return ['success' => true];
        } catch (Exception $e) {
            $this->conn->rollback();
            return ['success' => false, 'message' => $e->getMessage()];
        }
    }

    public function delete($id_cauhoi)
    {
        $cauhoi = $this->getById($id_cauhoi);
        if (!$cauhoi) {
            return ['success' => false, 'message' => 'Không tìm thấy câu hỏi'];
        }

        if ($this->isBaiThiLocked($cauhoi['id_baithi'])) {
            return ['success' => false, 'message' => 'Không thể xóa vì bài thi này đã có dữ liệu làm bài của thí sinh.'];
        }

        $this->conn->begin_transaction();
        try {
            $stmt1 = $this->conn->prepare("DELETE FROM dapan WHERE id_cauhoi = ?");
            $stmt1->bind_param("i", $id_cauhoi);
            $stmt1->execute();

            $stmt2 = $this->conn->prepare("DELETE FROM cauhoi WHERE id_cauhoi = ?");
            $stmt2->bind_param("i", $id_cauhoi);
            $stmt2->execute();

            $this->conn->commit();
            return ['success' => true];
        } catch (Exception $e) {
            $this->conn->rollback();
            return ['success' => false, 'message' => "Lỗi hệ thống: " . $e->getMessage()];
        }
    }

    public function getByBaiThi($id_baithi)
    {
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

    public function getDapAnByCauHoi($id_cauhoi)
    {
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

    public function getById($id_cauhoi)
    {
        $sql = "SELECT * FROM cauhoi WHERE id_cauhoi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_cauhoi);
        $stmt->execute();
        $cauhoi = $stmt->get_result()->fetch_assoc();
        if ($cauhoi) {
            $cauhoi['dapan'] = $this->getDapAnByCauHoi($id_cauhoi);
        }
        return $cauhoi;
    }

    public function getBaiThiInfo($id_baithi)
    {
        $sql = "SELECT bt.*, mh.tenmonhoc
                FROM baithi bt
                LEFT JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc
                WHERE bt.id_baithi = ?";
        $stmt = $this->conn->prepare($sql);
        $stmt->bind_param("i", $id_baithi);
        $stmt->execute();
        return $stmt->get_result()->fetch_assoc();
    }
}
