package com.ptquiz.ui.auth;

import com.ptquiz.core.*;
import com.ptquiz.ui.main.Home;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Login extends JFrame {
    private JTextField textfieldEmail;
    private JPasswordField passwordfieldPassword;
    private JCheckBox checkboxShowPassword;
    private JButton buttonLogin;
    private JButton buttonQuit;
    private JButton buttonRegister;

    public Login() {
        initComponents();
        setTitle("Đăng nhập Hệ thống");
        setSize(420, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Header
        JLabel labelTitle = new JLabel("ĐĂNG NHẬP", SwingConstants.CENTER);
        labelTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        labelTitle.setForeground(new Color(31, 41, 55)); // Dark gray
        labelTitle.setBorder(new EmptyBorder(0, 0, 25, 0));
        mainPanel.add(labelTitle, BorderLayout.NORTH);

        // Center Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel labelEmail = new JLabel("Email:");
        labelEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(labelEmail, gbc);

        textfieldEmail = new JTextField(18);
        textfieldEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textfieldEmail.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(textfieldEmail, gbc);

        JLabel labelPassword = new JLabel("Mật khẩu:");
        labelPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 8, 15);
        formPanel.add(labelPassword, gbc);

        passwordfieldPassword = new JPasswordField(18);
        passwordfieldPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordfieldPassword.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(passwordfieldPassword, gbc);

        checkboxShowPassword = new JCheckBox("Hiện mật khẩu");
        checkboxShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(checkboxShowPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Footer Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        buttonLogin = new JButton("Đăng nhập");
        stylePrimaryButton(buttonLogin);

        buttonQuit = new JButton("Thoát");
        styleSecondaryButton(buttonQuit);

        topButtons.add(buttonLogin);
        topButtons.add(buttonQuit);

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel labelRegister = new JLabel("Chưa có tài khoản? ");
        labelRegister.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        buttonRegister = new JButton("Đăng ký ngay");
        styleTextButton(buttonRegister);

        bottomButtons.add(labelRegister);
        bottomButtons.add(buttonRegister);

        buttonPanel.add(topButtons);
        buttonPanel.add(bottomButtons);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Actions
        checkboxShowPassword.addActionListener(e -> {
            if (checkboxShowPassword.isSelected()) {
                passwordfieldPassword.setEchoChar((char) 0);
            } else {
                passwordfieldPassword.setEchoChar('*');
            }
        });

        buttonQuit.addActionListener(e -> System.exit(0));

        buttonRegister.addActionListener(e -> {
            new Register();
            dispose();
        });

        buttonLogin.addActionListener(e -> {
            String email = textfieldEmail.getText().trim();
            String password = new String(passwordfieldPassword.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String jsonRequest = String.format("{\"email\":\"%s\",\"password\":\"%s\"}",
                    APIHelper.escapeJSON(email),
                    APIHelper.escapeJSON(password));

            APIHelper.APIResponse response = APIHelper.sendPost("auth/login", jsonRequest);

            if (response.success) {
                // Parse returned info and save it globally in UserSession
                if (response.rawData != null) {
                    // Smart ID extraction
                    String idStr = APIHelper.extractJsonValue(response.rawData, "id");
                    if (idStr.isEmpty()) idStr = APIHelper.extractJsonValue(response.rawData, "id_nguoidung");
                    if (idStr.isEmpty()) idStr = APIHelper.extractJsonValue(response.rawData, "userId");
                    
                    if (!idStr.isEmpty()) {
                        UserSession.userId = Integer.parseInt(idStr);
                    }
                    
                    UserSession.ten = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(response.rawData, "ten"));
                    UserSession.email = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(response.rawData, "email"));
                    UserSession.ngaythamgia = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(response.rawData, "ngaytao"));
                    UserSession.role = APIHelper.extractJsonValue(response.rawData, "role");
                    UserSession.avatar = APIHelper.extractJsonValue(response.rawData, "avatar");
                    UserSession.token = APIHelper.extractJsonValue(response.rawData, "token");
                    UserSession.matkhau = password;
                    
                    if (UserSession.ngaythamgia.contains(" ")) {
                        UserSession.ngaythamgia = UserSession.ngaythamgia.split(" ")[0];
                    }
                }
                JOptionPane.showMessageDialog(this, response.message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                new Home();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.message, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(mainPanel);
        getRootPane().setDefaultButton(buttonLogin);
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(new Color(37, 99, 235)); // Blue-600
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(130, 36));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(229, 231, 235)); // Gray-200
        button.setForeground(new Color(31, 41, 55)); // Gray-800
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(130, 36));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTextButton(JButton button) {
        button.setBackground(UIManager.getColor("Panel.background"));
        button.setForeground(new Color(37, 99, 235));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        // Cài đặt Look and Feel hệ thống để giao diện đẹp và mượt mà hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Login());
    }
}