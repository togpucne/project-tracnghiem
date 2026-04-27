<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/Database.php";
require_once __DIR__ . "/../model/giangvien/cauhoi.model.php";

$user = Api::requireLogin();

$id_baithi = isset($_POST["id_baithi"]) ? (int) $_POST["id_baithi"] : 0;
if ($id_baithi <= 0) {
    Api::json(["error" => "Thiếu ID bài thi"], 400);
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

$conn = Database::connect();
$role = $user["vaitro"] ?? "";
$ownerId = (int) ($user["id_nguoidung"] ?? 0);
$sql = "SELECT bt.id_baithi
    FROM baithi bt
    JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc
    WHERE bt.id_baithi = ? AND (mh.id_nguoidung = ? OR ? = 'admin')";
$stmt = $conn->prepare($sql);
$stmt->bind_param("iis", $id_baithi, $ownerId, $role);
$stmt->execute();
if ($stmt->get_result()->num_rows === 0) {
    // $conn->close();
    Api::json(["error" => "Bạn không có quyền import vào bài thi này"], 403);
}
// $conn->close();

function parse_docx_text($path)
{
    $content = false;

    if (class_exists('ZipArchive')) {
        $zip = new ZipArchive();
        if ($zip->open($path) === true) {
            $content = $zip->getFromName("word/document.xml");
            $zip->close();
        }
    } elseif (function_exists('shell_exec')) {
        $tempDir = sys_get_temp_dir() . DIRECTORY_SEPARATOR . 'docx_' . uniqid('', true);
        if (!mkdir($tempDir, 0777, true) && !is_dir($tempDir)) {
            throw new Exception("Không tạo được thư mục tạm để đọc file Word");
        }

        $docxPath = realpath($path);
        if ($docxPath === false) {
            throw new Exception("Không tìm thấy file Word đã upload");
        }

        $powerShell = 'powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -Command ';
        $command = $powerShell . escapeshellarg(
            "Expand-Archive -LiteralPath '" . str_replace("'", "''", $docxPath) . "' -DestinationPath '" . str_replace("'", "''", $tempDir) . "' -Force"
        );
        @shell_exec($command);

        $documentXmlPath = $tempDir . DIRECTORY_SEPARATOR . 'word' . DIRECTORY_SEPARATOR . 'document.xml';
        if (is_file($documentXmlPath)) {
            $content = file_get_contents($documentXmlPath);
        }

        $iterator = new RecursiveIteratorIterator(
            new RecursiveDirectoryIterator($tempDir, FilesystemIterator::SKIP_DOTS),
            RecursiveIteratorIterator::CHILD_FIRST
        );
        foreach ($iterator as $item) {
            $item->isDir() ? @rmdir($item->getPathname()) : @unlink($item->getPathname());
        }
        @rmdir($tempDir);
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
    if (!function_exists('gzinflate')) {
        return false;
    }

    $fp = @fopen($path, 'rb');
    if (!$fp) {
        return false;
    }

    while (!feof($fp)) {
        $signature = fread($fp, 4);
        if ($signature === false || strlen($signature) < 4) {
            break;
        }

        if ($signature !== "PK\x03\x04") {
            break;
        }

        $header = fread($fp, 26);
        if ($header === false || strlen($header) < 26) {
            break;
        }

        $parts = unpack('vversion/vflags/vmethod/vtime/vdate/Vcrc/Vcompressed/Vuncompressed/vnameLen/vextraLen', $header);
        $name = fread($fp, (int) $parts['nameLen']);
        if ($name === false || strlen($name) < (int) $parts['nameLen']) {
            break;
        }

        if (!empty($parts['extraLen'])) {
            fseek($fp, (int) $parts['extraLen'], SEEK_CUR);
        }

        $data = fread($fp, (int) $parts['compressed']);
        if ($data === false || strlen($data) < (int) $parts['compressed']) {
            break;
        }

        if ($name === $targetName) {
            fclose($fp);

            if ((int) $parts['method'] === 0) {
                return $data;
            }

            if ((int) $parts['method'] === 8) {
                $inflated = @gzinflate($data);
                return $inflated === false ? false : $inflated;
            }

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
        if (!$current) {
            return;
        }

        if (empty($current['noidungcauhoi']) || count($current['options']) < 2 || empty($current['answer_letter'])) {
            throw new Exception("Định dạng file Word chưa đúng ở câu: " . ($current['noidungcauhoi'] ?? ''));
        }

        $answerIndex = ord($current['answer_letter']) - 65;
        if (!isset($current['options'][$answerIndex])) {
            throw new Exception("Đáp án đúng không khớp với danh sách A/B/C/D của câu: " . $current['noidungcauhoi']);
        }

        $dapan_list = [];
        foreach ($current['options'] as $index => $optionText) {
            $dapan_list[] = [
                'noidung' => $optionText,
                'dapandung' => $index === $answerIndex ? 1 : 0,
            ];
        }

        $questions[] = [
            'noidungcauhoi' => $current['noidungcauhoi'],
            'dokho' => $current['dokho'] ?: 'Dễ',
            'dapan_list' => $dapan_list,
        ];

        $current = null;
    };

    foreach ($lines as $line) {
        if (preg_match('/^Câu\s*\d+\s*:\s*(.+)$/iu', $line, $matches)) {
            $flushCurrent($current, $questions);
            $current = [
                'noidungcauhoi' => trim($matches[1]),
                'options' => [],
                'answer_letter' => '',
                'dokho' => 'Dễ',
            ];
            continue;
        }

        if (!$current) {
            continue;
        }

        if (preg_match('/^([A-D])\.\s*(.+)$/u', $line, $matches)) {
            $index = ord($matches[1]) - 65;
            $current['options'][$index] = trim($matches[2]);
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
} catch (Exception $e) {
    Api::json(["error" => $e->getMessage()], 400);
}

if (!$questions) {
    Api::json(["error" => "Không tìm thấy câu hỏi hợp lệ trong file Word"], 400);
}

$model = new CauHoiModel();
$result = $model->createMany($id_baithi, $questions);

if (!($result["success"] ?? false)) {
    Api::json(["error" => $result["message"] ?? "Không thể import câu hỏi"], 400);
}

Api::json([
    "success" => true,
    "message" => "Import thành công " . (int) ($result["count"] ?? count($questions)) . " câu hỏi từ file Word",
]);
