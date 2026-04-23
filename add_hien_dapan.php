<?php
require_once 'server/model/Database.php';
$conn = Database::connect();
$ok = $conn->query("ALTER TABLE baithi ADD COLUMN hien_dapan TINYINT(1) DEFAULT 0 AFTER xao_tron");
if ($ok) {
    echo "Added hien_dapan column successfully.\n";
} else {
    echo "Error adding column: " . $conn->error . "\n";
}
$conn->close();
