<?php
require_once __DIR__ . "/../Database.php";

/**
 * Lấy danh sách các bài thi của giảng viên kèm theo thống kê kết quả
 */
function get_exam_results_summary($id_giangvien) {
    $conn = Database::connect();
    $sql = "SELECT 
                b.id_baithi, 
                b.ten_baithi, 
                b.tongcauhoi,
                m.tenmonhoc,
                COUNT(l.id_lanthi) as so_luot_lam,
                AVG(l.diem) as diem_trung_binh,
                MAX(l.diem) as diem_cao_nhat,
                MIN(l.diem) as diem_thap_nhat
            FROM baithi b
            JOIN monhoc m ON b.id_monhoc = m.id_monhoc
            LEFT JOIN lanthi l ON b.id_baithi = l.id_baithi AND l.trangthai = 'done'
            WHERE m.id_nguoidung = ?
            GROUP BY b.id_baithi
            ORDER BY b.id_baithi DESC";
            
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id_giangvien);
    $stmt->execute();
    $result = $stmt->get_result();
    $data = $result->fetch_all(MYSQLI_ASSOC);
    
    $stmt->close();
    $conn->close();
    return $data;
}

/**
 * Lấy danh sách thí sinh đã làm một bài thi cụ thể
 */
function get_exam_submissions($id_baithi) {
    $conn = Database::connect();
    $sql = "SELECT 
                l.id_lanthi,
                l.id_nguoidung,
                n.ten as ten_thisinh,
                n.email as email_thisinh,
                l.diem,
                l.socaudung,
                l.thoigianbatdau,
                l.thoigiannop,
                TIMESTAMPDIFF(SECOND, l.thoigianbatdau, l.thoigiannop) as thoi_gian_lam_giay
            FROM lanthi l
            JOIN nguoidung n ON l.id_nguoidung = n.id_nguoidung
            WHERE l.id_baithi = ? AND l.trangthai = 'done'
            ORDER BY l.thoigiannop DESC";
            
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id_baithi);
    $stmt->execute();
    $result = $stmt->get_result();
    $data = $result->fetch_all(MYSQLI_ASSOC);
    
    $stmt->close();
    $conn->close();
    return $data;
}

/**
 * Lấy chi tiết bài làm của một thí sinh
 */
function get_submission_detail($id_lanthi) {
    $conn = Database::connect();
    
    // Thông tin chung lần thi
    $sql_info = "SELECT l.*, n.ten, b.ten_baithi, b.tongcauhoi 
                 FROM lanthi l
                 JOIN nguoidung n ON l.id_nguoidung = n.id_nguoidung
                 JOIN baithi b ON l.id_baithi = b.id_baithi
                 WHERE l.id_lanthi = ?";
    $stmt = $conn->prepare($sql_info);
    $stmt->bind_param("i", $id_lanthi);
    $stmt->execute();
    $info = $stmt->get_result()->fetch_assoc();
    
    // Danh sách câu hỏi và câu trả lời
    $sql_answers = "SELECT 
                        ch.id_cauhoi, 
                        ch.noidungcauhoi, 
                        ch.loai_cauhoi,
                        ts.cautraloichon,
                        ts.noidungtraloi as noidung_thisinh,
                        d.id_dapan,
                        d.noidungdapan,
                        d.dapandung
                    FROM cauhoi ch
                    LEFT JOIN traloithisinh ts ON ch.id_cauhoi = ts.id_cauhoi AND ts.id_lanthi = ?
                    JOIN dapan d ON ch.id_cauhoi = d.id_cauhoi
                    WHERE ch.id_baithi = ?
                    ORDER BY ch.id_cauhoi, d.id_dapan";
                    
    $stmt = $conn->prepare($sql_answers);
    $stmt->bind_param("ii", $id_lanthi, $info['id_baithi']);
    $stmt->execute();
    $answers_raw = $stmt->get_result()->fetch_all(MYSQLI_ASSOC);
    
    // Group answers by question
    $questions = [];
    foreach ($answers_raw as $row) {
        $qid = $row['id_cauhoi'];
        if (!isset($questions[$qid])) {
            $questions[$qid] = [
                'id_cauhoi' => $qid,
                'noidungcauhoi' => $row['noidungcauhoi'],
                'loai_cauhoi' => $row['loai_cauhoi'],
                'cautraloichon' => $row['cautraloichon'],
                'noidung_thisinh' => $row['noidung_thisinh'],
                'options' => []
            ];
        }
        $questions[$qid]['options'][] = [
            'id_dapan' => $row['id_dapan'],
            'noidungdapan' => $row['noidungdapan'],
            'dapandung' => $row['dapandung']
        ];
    }
    
    $stmt->close();
    $conn->close();
    
    return [
        'info' => $info,
        'questions' => array_values($questions)
    ];
}
