<?php
$c = new mysqli('localhost', 'root', '', 'tracnghiem', 3307);
$r = $c->query('SHOW COLUMNS FROM api_logs');
while($row = $r->fetch_assoc()) echo $row['Field'] . " | ";
