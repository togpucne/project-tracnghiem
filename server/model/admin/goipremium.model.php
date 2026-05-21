<?php
class GoiPremiumModel {
    private $db;

    public function __construct($db) {
        $this->db = $db;
    }

    public function getAll() {
        $sql = "SELECT * FROM goi_premium ORDER BY gia ASC";
        $result = $this->db->query($sql);
        $data = [];
        if ($result) {
            while ($row = $result->fetch_assoc()) {
                $data[] = $row;
            }
        }
        return $data;
    }

    public function getById($id) {
        $stmt = $this->db->prepare("SELECT * FROM goi_premium WHERE id_goi = ?");
        $stmt->bind_param("i", $id);
        $stmt->execute();
        $result = $stmt->get_result();
        return $result->fetch_assoc();
    }

    public function save($data) {
        if (isset($data['id_goi']) && $data['id_goi'] > 0) {
            // Update
            $stmt = $this->db->prepare("UPDATE goi_premium SET ten_goi = ?, gia = ?, thoihan_ngay = ?, mieuta = ? WHERE id_goi = ?");
            $stmt->bind_param("sdisi", $data['ten_goi'], $data['gia'], $data['thoihan_ngay'], $data['mieuta'], $data['id_goi']);
        } else {
            // Insert
            $stmt = $this->db->prepare("INSERT INTO goi_premium (ten_goi, gia, thoihan_ngay, mieuta) VALUES (?, ?, ?, ?)");
            $stmt->bind_param("sdis", $data['ten_goi'], $data['gia'], $data['thoihan_ngay'], $data['mieuta']);
        }
        $success = $stmt->execute();
        $stmt->close();
        return $success;
    }

    public function delete($id) {
        $stmt = $this->db->prepare("DELETE FROM goi_premium WHERE id_goi = ?");
        $stmt->bind_param("i", $id);
        $success = $stmt->execute();
        $stmt->close();
        return $success;
    }

    public function copy($id) {
        $goi = $this->getById($id);
        if (!$goi) return false;

        $new_name = $goi['ten_goi'] . " (Copy)";
        $stmt = $this->db->prepare("INSERT INTO goi_premium (ten_goi, gia, thoihan_ngay, mieuta) VALUES (?, ?, ?, ?)");
        $stmt->bind_param("sdis", $new_name, $goi['gia'], $goi['thoihan_ngay'], $goi['mieuta']);
        $success = $stmt->execute();
        $stmt->close();
        return $success;
    }

    public function checkUniqueName($name, $excludeId = 0) {
        $sql = "SELECT id_goi FROM goi_premium WHERE ten_goi = ? AND id_goi != ?";
        $stmt = $this->db->prepare($sql);
        $stmt->bind_param("si", $name, $excludeId);
        $stmt->execute();
        $result = $stmt->get_result();
        return $result->num_rows === 0;
    }
}
?>
