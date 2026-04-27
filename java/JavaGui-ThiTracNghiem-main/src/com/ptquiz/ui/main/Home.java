package com.ptquiz.ui.main;

import com.ptquiz.core.*;
import com.ptquiz.ui.auth.Login;
import com.ptquiz.ui.lecturer.*;
import com.ptquiz.ui.profile.ProfilePanel;
import com.ptquiz.ui.student.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Home extends JFrame {
    private JPanel cards;
    private CardLayout cardLayout;
    private JPanel gridPanel;
    private java.util.Map<String, JButton> menuButtons = new java.util.HashMap<>();
    
    // Lecturer Panels
    private SubjectManagementPanel subjectPanel;
    private ExamManagementPanel examPanel;
    private BankManagementPanel bankPanel;
    private ResultViewPanel resultPanel;
    private LecturerDashboard dashboardPanel;
    private HistoryPanel historyPanel;

    public Home() {
        setTitle("Trang chủ - Trắc Nghiệm");
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();

        setVisible(true);
    }

    private void initComponents() {
        JPanel wrapper = new JPanel(new BorderLayout());

        // WEST: Sidebar Navigation
        wrapper.add(createSidebar(), BorderLayout.WEST);

        // CENTER: Main Content via CardLayout
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // ------------- HOME CARD -------------
        cards.add(createHomePanel(), "HOME");

        // ------------- EXAM LIBRARY CARD -------------
        cards.add(new ExamLibraryPanel(), "LIBRARY");

        // ------------- LECTURER PANELS -------------
        if ("giangvien".equals(UserSession.role)) {
            dashboardPanel = new LecturerDashboard();
            subjectPanel = new SubjectManagementPanel();
            examPanel = new ExamManagementPanel();
            bankPanel = new BankManagementPanel();
            resultPanel = new ResultViewPanel();
            
            cards.add(dashboardPanel, "LECTURER_DASHBOARD");
            cards.add(subjectPanel, "MANAGE_SUBJECTS");
            cards.add(examPanel, "MANAGE_EXAMS");
            cards.add(bankPanel, "MANAGE_BANKS");
            cards.add(resultPanel, "VIEW_RESULTS");
        }

        // ------------- PROFILE CARD -------------
        cards.add(new ProfilePanel(), "PROFILE");

        // ------------- HISTORY CARD -------------
        historyPanel = new HistoryPanel();
        cards.add(historyPanel, "HISTORY");

        wrapper.add(cards, BorderLayout.CENTER);

        // Set default view based on role
        if ("giangvien".equals(UserSession.role)) {
            switchView("LECTURER_DASHBOARD");
        } else {
            switchView("HOME");
        }

        add(wrapper);
    }

    public void switchView(String viewName) {
        cardLayout.show(cards, viewName);
        
        // Update sidebar highlights
        for (java.util.Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            boolean active = entry.getKey().equals(viewName);
            JButton btn = entry.getValue();
            btn.setForeground(active ? Color.WHITE : new Color(209, 213, 219));
            btn.setBackground(active ? new Color(239, 68, 68) : new Color(31, 41, 55));
        }

        // Refresh data based on viewName
        switch (viewName) {
            case "LECTURER_DASHBOARD": if(dashboardPanel != null) dashboardPanel.loadStats(); break;
            case "MANAGE_SUBJECTS": if(subjectPanel != null) subjectPanel.loadData(); break;
            case "MANAGE_EXAMS": if(examPanel != null) examPanel.loadData(); break;
            case "MANAGE_BANKS": if(bankPanel != null) bankPanel.loadBanks(); break;
            case "HISTORY": if(historyPanel != null) historyPanel.refresh(); break;
            case "HOME": refreshExams(); break;
        }
    }

    public void refreshExams() {
        if (gridPanel != null) {
            gridPanel.removeAll();
            loadLatest8Exams(gridPanel);
            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.WHITE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);

        // --- 1. HERO SECTION ---
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(new Color(37, 99, 235));
        hero.setPreferredSize(new Dimension(0, 180));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel heroText = new JLabel("KIỂM TRA NĂNG LỰC - LUYỆN ĐỀ ĐỈNH CAO", SwingConstants.CENTER);
        heroText.setFont(new Font("Segoe UI", Font.BOLD, 32));
        heroText.setForeground(Color.WHITE);
        hero.add(heroText, BorderLayout.CENTER);
        content.add(hero);

        // --- 2. LATEST EXAMS SECTION ---
        JPanel examsSection = new JPanel(new BorderLayout());
        examsSection.setBackground(Color.WHITE);
        examsSection.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel sectionTitle = new JLabel("8 bài thi mới nhất");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sectionTitle.setBorder(new EmptyBorder(0, 0, 30, 0));
        examsSection.add(sectionTitle, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        gridPanel.setBackground(Color.WHITE);
        examsSection.add(gridPanel, BorderLayout.CENTER);
        
        loadLatest8Exams(gridPanel);
        content.add(examsSection);

        // --- 3. MARKETING SECTION ---
        JPanel marketPanel = new JPanel();
        marketPanel.setLayout(new BoxLayout(marketPanel, BoxLayout.Y_AXIS));
        marketPanel.setBackground(new Color(248, 250, 252));
        marketPanel.setBorder(new EmptyBorder(60, 100, 60, 100));

        JLabel mTitle = new JLabel("Phần mềm luyện thi online — PT QUIZ");
        mTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        mTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextArea mDesc = new JTextArea(
            "PT QUIZ là nền tảng luyện thi trắc nghiệm online giúp người học ôn tập hiệu quả với nhiều chủ đề như TOEIC, IELTS, Lập trình, Toán học và nhiều lĩnh vực khác.\n\n" +
            "Hệ thống mô phỏng đề thi thật, cung cấp ngân hàng câu hỏi đa dạng, luyện tập theo chủ đề hoặc làm đề full test.\n\n" +
            "Theo dõi tiến độ học tập, thống kê kết quả chi tiết và gợi ý lộ trình phù hợp.\n\n" +
            "Luyện tập miễn phí ngay hôm nay cùng PT QUIZ!"
        );
        mDesc.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        mDesc.setLineWrap(true);
        mDesc.setWrapStyleWord(true);
        mDesc.setEditable(false);
        mDesc.setBackground(new Color(248, 250, 252));
        mDesc.setBorder(new EmptyBorder(30, 0, 40, 0));
        mDesc.setMaximumSize(new Dimension(900, 300));

        marketPanel.add(mTitle);
        marketPanel.add(mDesc);
        content.add(marketPanel);

        // --- 4. CONSULTATION FORM ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(60, 100, 80, 100));
        formPanel.setMaximumSize(new Dimension(1000, 700));

        JLabel fTitle = new JLabel("Tư vấn lộ trình học");
        fTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        fTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(fTitle);
        formPanel.add(Box.createVerticalStrut(30));

        addFormField("Họ tên *", formPanel);
        addFormField("Số điện thoại *", formPanel);
        addFormField("Khu vực học *", formPanel);
        addFormField("Môn học bạn quan tâm", formPanel);

        JButton submitForm = new JButton("Gửi yêu cầu tư vấn");
        submitForm.setBackground(new Color(16, 185, 129));
        submitForm.setForeground(Color.BLACK); // Changed to BLACK for visibility
        submitForm.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitForm.setFocusPainted(false);
        submitForm.setPreferredSize(new Dimension(300, 50));
        submitForm.setMaximumSize(new Dimension(300, 50));
        submitForm.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitForm.setOpaque(true);
        submitForm.setContentAreaFilled(true); // Ensure background is painted
        submitForm.setBorderPainted(false);
        submitForm.setCursor(new Cursor(Cursor.HAND_CURSOR));

        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(submitForm);

        content.add(formPanel);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        homePanel.add(scroll, BorderLayout.CENTER);

        return homePanel;
    }

    private void addFormField(String label, JPanel parent) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(8));
        
        JTextField tf = new JTextField();
        tf.setMaximumSize(new Dimension(500, 40));
        tf.setPreferredSize(new Dimension(500, 40));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(tf);
        parent.add(Box.createVerticalStrut(20));
    }

    private void loadLatest8Exams(JPanel gridPanel) {
        new Thread(() -> {
            String jsonResponse = APIHelper.sendGet("exam/list");
            if (jsonResponse == null || jsonResponse.isEmpty()) return;

            SwingUtilities.invokeLater(() -> {
                gridPanel.removeAll();
                int dataStart = jsonResponse.indexOf("\"data\":[");
                if (dataStart != -1) {
                    String dataPart = jsonResponse.substring(dataStart);
                    String[] items = dataPart.split("\\{");
                    for (int i = 1; i < Math.min(items.length, 9); i++) {
                        String raw = "{" + items[i];
                        String id = APIHelper.extractJsonValue(raw, "id_baithi");
                        String title = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "ten_baithi"));
                        String sub = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "tenmonhoc"));
                        boolean ongoing = "1".equals(APIHelper.extractJsonValue(raw, "is_ongoing"));
                        gridPanel.add(createExamCard(id, title, sub, "60 phút", "10 câu", ongoing));
                    }
                }
                gridPanel.revalidate();
                gridPanel.repaint();
            });
        }).start();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(31, 41, 55)); // Gray-800
        sidebar.setPreferredSize(new Dimension(260, 0));

        // Logo / Title
        JLabel logo = new JLabel("PT QUIZ", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(new EmptyBorder(40, 0, 60, 0));

        sidebar.add(logo);

        // Menu Buttons
        if ("giangvien".equals(UserSession.role)) {
            sidebar.add(createMenuButton("Tổng quan", true, "LECTURER_DASHBOARD"));
            sidebar.add(createMenuButton("Quản lý môn học", false, "MANAGE_SUBJECTS"));
            sidebar.add(createMenuButton("Quản lý đề thi", false, "MANAGE_EXAMS"));
            sidebar.add(createMenuButton("Ngân hàng câu hỏi", false, "MANAGE_BANKS"));
            sidebar.add(createMenuButton("Kết quả thi", false, "VIEW_RESULTS"));
            
            sidebar.add(Box.createVerticalStrut(20));
        } else {
            sidebar.add(createMenuButton("Trang chủ", true, "HOME"));
            sidebar.add(createMenuButton("Đề bài", false, "LIBRARY"));
            sidebar.add(createMenuButton("Lịch sử làm bài", false, "HISTORY"));
        }
        
        sidebar.add(createMenuButton("Thông tin cá nhân", false, "PROFILE"));



        sidebar.add(Box.createVerticalGlue()); // Push logout to bottom

        // Logout
        JButton logoutBtn = createMenuButton("Đăng xuất", false, null);
        logoutBtn.setForeground(new Color(248, 113, 113)); // Red-400
        logoutBtn.addActionListener(e -> {
            new Login();
            dispose();
        });
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(30));

        return sidebar;
    }

    private JButton createMenuButton(String text, boolean active, String viewName) {
        JButton btn = new JButton(text);
        if (viewName != null) menuButtons.put(viewName, btn);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        if (viewName != null && !viewName.isEmpty()) {
            btn.addActionListener(e -> switchView(viewName));
        }
        btn.setForeground(active ? Color.WHITE : new Color(209, 213, 219));
        btn.setBackground(active ? new Color(239, 68, 68) : new Color(31, 41, 55));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setBorder(new EmptyBorder(0, 40, 0, 0)); // Indent the text beautifully
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                // Only hover if not already active
                if (btn.getBackground().equals(new Color(31, 41, 55))) {
                    btn.setBackground(new Color(55, 65, 81));
                }
            }

            public void mouseExited(MouseEvent e) {
                // If it's not the active red, return to normal gray
                if (!btn.getBackground().equals(new Color(239, 68, 68))) {
                    btn.setBackground(new Color(31, 41, 55));
                }
            }
        });
        return btn;
    }

    private JPanel createExamCard(String idBaithi, String title, String category, String time, String questions, boolean isOngoing) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(25, 20, 25, 20)));

        JLabel titleLabel = new JLabel("<html><div style='width: 150px;'><b>" + title + "</b></div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(31, 41, 55));

        JLabel infoLabel = new JLabel("<html>" + time + " | " + questions + "</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(107, 114, 128));

        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        categoryLabel.setForeground(new Color(37, 99, 235));
        categoryLabel.setBackground(new Color(239, 246, 255));
        categoryLabel.setOpaque(true);
        categoryLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        JButton button = new JButton(isOngoing ? "Làm tiếp" : "Làm bài");
        if (isOngoing) {
            button.setBackground(new Color(255, 235, 59)); // Vibrant Yellow
            button.setForeground(new Color(31, 41, 55)); // Dark text for contrast
            button.setBorder(new LineBorder(new Color(234, 179, 8), 1, true)); // Subtle darker yellow border
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setBorder(new LineBorder(Color.BLACK, 1, true));
        }
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(150, 40));

        button.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ExamScreen(topFrame, idBaithi, title).setVisible(true);
        });

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(infoLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(categoryLabel);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(20));
        card.add(button);

        return card;
    }

    // Main method removed to ensure app starts from Login.java
}
