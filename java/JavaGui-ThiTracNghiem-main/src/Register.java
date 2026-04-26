import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Register extends JFrame {
    private JTextField textfieldFullname;
    private JTextField textfieldEmail;
    private JPasswordField passwordfieldPassword;
    private JCheckBox checkboxShowPassword;
    private JButton buttonRegister;
    private JButton buttonLogin;

    public Register() {
        initComponents();
        setTitle("Đăng ký Tài khoản");
        setSize(420, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(25, 40, 25, 40));

        // Header
        JLabel labelTitle = new JLabel("ĐĂNG KÝ", SwingConstants.CENTER);
        labelTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        labelTitle.setForeground(new Color(31, 41, 55));
        labelTitle.setBorder(new EmptyBorder(0, 0, 25, 0));
        mainPanel.add(labelTitle, BorderLayout.NORTH);

        // Center Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel labelFullname = new JLabel("Họ tên:");
        labelFullname.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(labelFullname, gbc);

        textfieldFullname = new JTextField(18);
        textfieldFullname.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textfieldFullname.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(textfieldFullname, gbc);

        JLabel labelEmail = new JLabel("Email:");
        labelEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 8, 15);
        formPanel.add(labelEmail, gbc);

        textfieldEmail = new JTextField(18);
        textfieldEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textfieldEmail.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(textfieldEmail, gbc);

        JLabel labelPassword = new JLabel("Mật khẩu:");
        labelPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 0, 8, 15);
        formPanel.add(labelPassword, gbc);

        passwordfieldPassword = new JPasswordField(18);
        passwordfieldPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordfieldPassword.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 0, 8, 0);
        formPanel.add(passwordfieldPassword, gbc);

        checkboxShowPassword = new JCheckBox("Hiện mật khẩu");
        checkboxShowPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(checkboxShowPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Footer Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));

        buttonRegister = new JButton("Đăng ký");
        stylePrimaryButton(buttonRegister);
        buttonRegister.setPreferredSize(new Dimension(200, 36));
        topButtons.add(buttonRegister);

        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel labelLogin = new JLabel("Đã có tài khoản? ");
        labelLogin.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        buttonLogin = new JButton("Đăng nhập");
        styleTextButton(buttonLogin);

        bottomButtons.add(labelLogin);
        bottomButtons.add(buttonLogin);

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

        buttonLogin.addActionListener(e -> {
            new Login();
            dispose();
        });

        buttonRegister.addActionListener(e -> {
            String fullname = textfieldFullname.getText().trim();
            String email = textfieldEmail.getText().trim();
            String password = new String(passwordfieldPassword.getPassword());

            if (fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String jsonRequest = String.format("{\"fullname\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    APIHelper.escapeJSON(fullname),
                    APIHelper.escapeJSON(email),
                    APIHelper.escapeJSON(password));

            APIHelper.APIResponse response = APIHelper.sendPost("auth/register", jsonRequest);

            if (response.success) {
                JOptionPane.showMessageDialog(this, response.message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                new Login();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.message, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(mainPanel);
        getRootPane().setDefaultButton(buttonRegister);
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(new Color(37, 99, 235)); // Blue-600
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
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
}
