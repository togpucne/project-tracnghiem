<?php

return [
    "auth/login" => [
        "handler" => __DIR__ . "/../api/login.php",
        "methods" => ["POST"],
    ],
    "auth/register" => [
        "handler" => __DIR__ . "/../api/register.php",
        "methods" => ["POST"],
    ],
    "auth/google_callback.php" => [
        "handler" => __DIR__ . "/../api/auth/google_callback.php",
        "methods" => ["POST"],
    ],
    "auth/forgot-password" => [
        "handler" => __DIR__ . "/../api/auth/forgot_password.php",
        "methods" => ["POST"],
    ],
    "auth/verify-otp" => [
        "handler" => __DIR__ . "/../api/auth/verify_otp.php",
        "methods" => ["POST"],
    ],
    "auth/reset-password" => [
        "handler" => __DIR__ . "/../api/auth/reset_password.php",
        "methods" => ["POST"],
    ],
    "exam/list" => [
        "handler" => __DIR__ . "/../api/get_exams.php",
        "methods" => ["GET"],
    ],
    "exam/subjects" => [
        "handler" => __DIR__ . "/../api/get_subjects.php",
        "methods" => ["GET"],
    ],
    "exam/questions" => [
        "handler" => __DIR__ . "/../api/get_exam_questions.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["thisinh"],
    ],
    "exam/submit" => [
        "handler" => __DIR__ . "/../api/submit.php",
        "methods" => ["POST"],
        "auth" => true,
        "roles" => ["thisinh"],
    ],
    "exam/sync-draft" => [
        "handler" => __DIR__ . "/../api/sync_draft.php",
        "methods" => ["PUT"],
        "auth" => true,
        "roles" => ["thisinh"],
    ],
    "result/detail" => [
        "handler" => __DIR__ . "/../api/get_result.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["thisinh"],
    ],
    "history/list" => [
        "handler" => __DIR__ . "/../api/get_history.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["thisinh"],
    ],
    "profile/detail" => [
        "handler" => __DIR__ . "/../api/get_profile.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["thisinh"],
    ],
    "profile/update" => [
        "handler" => __DIR__ . "/../api/update_profile.php",
        "methods" => ["POST", "PATCH"],
        "auth" => true,
        "roles" => ["thisinh"],
    ],
];
