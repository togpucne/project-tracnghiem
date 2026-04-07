<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Response.php";

Api::boot();

$routes = require __DIR__ . "/../routes/api.php";
$route = Api::detectRoute();

if ($route === "") {
    Response::json([
        "success" => true,
        "message" => "PT QUIZ API router",
        "usage" => "api/auth/login",
        "routes" => array_keys($routes),
    ]);
}

if (!isset($routes[$route])) {
    Response::json([
        "error" => "API route not found",
        "route" => $route,
    ], 404);
}

$config = $routes[$route];

if (!empty($config["methods"])) {
    Api::requireMethods($config["methods"]);
}

if (!empty($config["auth"])) {
    Api::requireLogin();
}

if (!empty($config["roles"])) {
    Api::requireRole($config["roles"]);
}

require $config["handler"];
