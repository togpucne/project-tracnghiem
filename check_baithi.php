<?php
require_once 'server/model/Database.php';
$conn = Database::connect();
$res = $conn->query("DESC baithi");
while($row = $res->fetch_assoc()) {
    echo $row['Field'] . " (" . $row['Type'] . ")\n";
}
$conn->close();
