<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/Database.php";
require_once __DIR__ . "/../model/giangvien/nganhang.model.php";

$user = Api::requireRole(["admin", "giangvien"]);

$id_nhch = isset($_POST["id_nhch"]) ? (int) $_POST["id_nhch"] : 0;
if ($id_nhch <= 0) {
    Api::json(["error" => "Thiếu ID ngân hàng câu hỏi"], 400);
}

if (!isset($_FILES["word_file"]) || !is_array($_FILES["word_file"])) {
    Api::json(["error" => "Vui lòng chọn file Word .docx"], 400);
}

$file = $_FILES["word_file"];
if (($file["error"] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK) {
    Api::json(["error" => "Upload file Word thất bại"], 400);
}

$extension = strtolower(pathinfo((string) ($file["name"] ?? ""), PATHINFO_EXTENSION));
if ($extension !== "docx") {
    Api::json(["error" => "Chỉ hỗ trợ file Word định dạng .docx"], 400);
}

// Validate ownership
$bank = getQuestionBankById($id_nhch, (int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "");
if (!$bank) {
    Api::json(["error" => "Bạn không có quyền import vào ngân hàng này"], 403);
}

// Word parsing logic (copied from cauhoi_import_word.php)
function parse_docx_text($path)
{
    $content = false;
    if (class_exists('ZipArchive')) {
        $zip = new ZipArchive();
        if ($zip->open($path) === true) {
            $content = $zip->getFromName("word/document.xml");
            $zip->close();
        }
    }

    if ($content === false || $content === null) {
        $content = read_docx_entry_without_zip($path, 'word/document.xml');
    }

    if ($content === false || $content === null) {
        throw new Exception("Server chưa đọc được file .docx.");
    }

    $content = preg_replace('/<w:p[^>]*>/', "\n", $content);
    $content = preg_replace('/<w:tab[^>]*\/>/', "\t", $content);
    $content = preg_replace('/<w:br[^>]*\/>/', "\n", $content);
    $text = strip_tags($content);
    $text = html_entity_decode($text, ENT_QUOTES | ENT_XML1, 'UTF-8');
    $lines = preg_split("/\r\n|\n|\r/u", $text);

    return array_values(array_filter(array_map(static function ($line) {
        $line = trim(preg_replace('/\s+/u', ' ', (string) $line));
        return $line;
    }, $lines), static fn($line) => $line !== ''));
}

function read_docx_entry_without_zip($path, $targetName)
{
    if (!function_exists('gzinflate')) return false;
    $fp = @fopen($path, 'rb');
    if (!$fp) return false;
    while (!feof($fp)) {
        $signature = fread($fp, 4);
        if ($signature !== "PK\x03\x04") break;
        $header = fread($fp, 26);
        if (strlen($header) < 26) break;
        $parts = unpack('vversion/vflags/vmethod/vtime/vdate/Vcrc/Vcompressed/Vuncompressed/vnameLen/vextraLen', $header);
        $name = fread($fp, (int) $parts['nameLen']);
        if (!empty($parts['extraLen'])) fseek($fp, (int) $parts['extraLen'], SEEK_CUR);
        $data = fread($fp, (int) $parts['compressed']);
        if ($name === $targetName) {
            fclose($fp);
            if ((int) $parts['method'] === 0) return $data;
            if ((int) $parts['method'] === 8) return @gzinflate($data);
            return false;
        }
    }
    fclose($fp);
    return false;
}

function parse_questions_from_lines($lines)
{
    $questions = [];
    $current = null;
    $flushCurrent = static function (&$current, &$questions) {
        if (!$current) return;
        if (empty($current['noidungcauhoi']) || count($current['options']) < 2 || empty($current['answer_letter'])) {
            throw new Exception("Định dạng file Word chưa đúng ở câu: " . ($current['noidungcauhoi'] ?? ''));
        }
        $answerIndex = ord($current['answer_letter']) - 65;
        if (!isset($current['options'][$answerIndex])) {
            throw new Exception("Đáp án đúng không khớp với danh sách A/B/C/D của câu: " . $current['noidungcauhoi']);
        }
        $dapan_list = [];
        foreach ($current['options'] as $index => $optionText) {
            $dapan_list[] = ['noidung' => $optionText, 'dapandung' => $index === $answerIndex ? 1 : 0];
        }
        $questions[] = ['noidungcauhoi' => $current['noidungcauhoi'], 'dokho' => $current['dokho'] ?: 'Dễ', 'dapan_list' => $dapan_list];
        $current = null;
    };

    foreach ($lines as $line) {
        if (preg_match('/^Câu\s*\d+\s*:\s*(.+)$/iu', $line, $matches)) {
            $flushCurrent($current, $questions);
            $current = ['noidungcauhoi' => trim($matches[1]), 'options' => [], 'answer_letter' => '', 'dokho' => 'Dễ'];
            continue;
        }
        if (!$current) continue;
        if (preg_match('/^([A-D])\.\s*(.+)$/u', $line, $matches)) {
            $current['options'][ord($matches[1]) - 65] = trim($matches[2]);
            continue;
        }
        if (preg_match('/^Đáp án\s*:\s*([A-D])$/iu', $line, $matches)) {
            $current['answer_letter'] = strtoupper($matches[1]);
            continue;
        }
        if (preg_match('/^Độ khó\s*:\s*(Dễ|Trung bình|Khó)$/iu', $line, $matches)) {
            $current['dokho'] = $matches[1];
            continue;
        }
        $current['noidungcauhoi'] .= ' ' . trim($line);
    }
    $flushCurrent($current, $questions);
    return $questions;
}

try {
    $lines = parse_docx_text($file["tmp_name"]);
    $questions = parse_questions_from_lines($lines);
    if (!$questions) throw new Exception("Không tìm thấy câu hỏi hợp lệ trong file Word");
    
    $result = createManyInBank($id_nhch, $questions);
    if (!($result["success"] ?? false)) throw new Exception($result["message"] ?? "Không thể import câu hỏi");
    
    Api::json(["success" => true, "message" => "Import thành công " . (int) ($result["count"] ?? count($questions)) . " câu hỏi vào ngân hàng"]);
} catch (Exception $e) {
    Api::json(["error" => $e->getMessage()], 400);
}
