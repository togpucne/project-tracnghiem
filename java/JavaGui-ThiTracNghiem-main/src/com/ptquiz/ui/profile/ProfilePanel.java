package com.ptquiz.ui.profile;

import com.ptquiz.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ProfilePanel extends JPanel {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passField;
    private JTextField dateField;
    private JLabel avatarLabel;
    private JLabel imgLabel;
    private java.io.File selectedAvatarFile;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        centerWrapper.setBackground(new Color(249, 250, 251));
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(600, 650));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(0, 0, 30, 0)
        ));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(37, 99, 235)); // Blue-600
        header.setPreferredSize(new Dimension(600, 60));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Stretch horizontally
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel headerLabel = new JLabel("Thông tin cá nhân", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        header.add(headerLabel, BorderLayout.CENTER);
        card.add(header);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(0, 50, 40, 50)); 
        
        // Avatar info / User Title
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarPanel.setBorder(new EmptyBorder(40, 0, 30, 0));

        imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(80, 80));
        try {
            String avatarUrl = "http://localhost/project-tracnghiem/server/public/imgs/avatars/" + UserSession.avatar;
            java.net.URL url = new java.net.URI(avatarUrl).toURL();
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imgLabel.setText("N/A");
        }
        avatarPanel.add(imgLabel);
        avatarPanel.add(Box.createHorizontalStrut(20));

        JLabel welcomeLabel = new JLabel(UserSession.ten);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(31, 41, 55));
        avatarPanel.add(welcomeLabel);

        avatarLabel = welcomeLabel;

        JButton changeAvatarBtn = new JButton("Chọn ảnh");
        changeAvatarBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeAvatarBtn.setMargin(new Insets(2, 5, 2, 5));
        changeAvatarBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedAvatarFile = chooser.getSelectedFile();
                try {
                    ImageIcon icon = new ImageIcon(selectedAvatarFile.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(img));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        JPanel avatarFull = new JPanel(new BorderLayout(0, 10));
        avatarFull.setBackground(Color.WHITE);
        avatarFull.add(avatarPanel, BorderLayout.CENTER);
        avatarFull.add(changeAvatarBtn, BorderLayout.SOUTH);
        avatarFull.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(avatarFull);

        nameField = createInput("Họ và tên", UserSession.ten, formPanel);
        emailField = createInput("Email", UserSession.email, formPanel);
        passField = createPasswordInput("Mật khẩu", UserSession.matkhau, formPanel);
        dateField = createInput("Ngày tham gia", UserSession.ngaythamgia, formPanel);
        dateField.setEditable(false);
        dateField.setBackground(new Color(243, 244, 246));

        JButton updateBtn = new JButton("Cập nhật thông tin");
        updateBtn.setBackground(new Color(16, 185, 129)); // Emerald-500
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        updateBtn.setFocusPainted(false);
        updateBtn.setBorderPainted(false);
        updateBtn.setContentAreaFilled(false);
        updateBtn.setOpaque(true);
        updateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateBtn.addActionListener(e -> updateProfile());
        
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(updateBtn);
        
        card.add(formPanel);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);
    }
    
    private JTextField createInput(String labelText, String value, JPanel parent) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(75, 85, 99));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(8));
        
        JTextField tf = new JTextField(value);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setPreferredSize(new Dimension(500, 40));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(209, 213, 219), 1),
            new EmptyBorder(5, 12, 5, 12)
        ));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(tf);
        parent.add(Box.createVerticalStrut(15));
        return tf;
    }

    private JPasswordField createPasswordInput(String labelText, String value, JPanel parent) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(75, 85, 99));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(8));
        
        JPasswordField tf = new JPasswordField(value);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tf.setPreferredSize(new Dimension(500, 40));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(209, 213, 219), 1),
            new EmptyBorder(5, 12, 5, 12)
        ));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(tf);
        
        JCheckBox toggleBtn = new JCheckBox("Hiện mật khẩu");
        toggleBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toggleBtn.setBackground(Color.WHITE);
        toggleBtn.setForeground(new Color(107, 114, 128));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        toggleBtn.addActionListener(e -> {
            if (toggleBtn.isSelected()) {
                tf.setEchoChar((char) 0);
            } else {
                tf.setEchoChar('•');
            }
        });
        
        parent.add(Box.createVerticalStrut(5));
        parent.add(toggleBtn);
        parent.add(Box.createVerticalStrut(15));
        
        return tf;
    }

    private void updateProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        
        if (!pass.isEmpty() && pass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới phải có tối thiểu 6 ký tự", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        java.util.Map<String, String> fields = new java.util.HashMap<>();
        fields.put("ten", name);
        fields.put("email", email);
        if (!pass.isEmpty()) {
            fields.put("matkhau", pass);
        }

        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendMultipartPost("profile/update", fields, "avatar", selectedAvatarFile);
            
            SwingUtilities.invokeLater(() -> {
                if(res.success) {
                    UserSession.ten = name;
                    UserSession.email = email;
                    if (!pass.isEmpty()) {
                        UserSession.matkhau = pass;
                    }
                    avatarLabel.setText(UserSession.ten);
                    
                    // Update global session with new avatar if returned
                    String newAvatar = APIHelper.extractJsonValue(res.rawData, "avatar");
                    if (!newAvatar.isEmpty()) {
                        UserSession.avatar = newAvatar;
                    }

                    JOptionPane.showMessageDialog(this, res.message, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, res.message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }
}
