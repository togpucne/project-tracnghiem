<?php 
$data = ['content' => 'CHUYEN TIEN DH1779378017842', 'amount' => 20000, 'reference_number' => '130160154939'];
$options = ['http' => ['method' => 'POST', 'header' => 'Content-type: application/json', 'content' => json_encode($data)]];
$context = stream_context_create($options);
$result = file_get_contents('http://localhost/project-tracnghiem/client/api/premium/webhook', false, $context);
echo "Result: " . $result;
