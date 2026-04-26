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

        // ------------- PROFILE CARD -------------
        cards.add(new ProfilePanel(), "PROFILE");

        // ------------- HISTORY CARD -------------
        HistoryPanel historyPanel = new HistoryPanel();
        cards.add(historyPanel, "HISTORY");

        wrapper.add(cards, BorderLayout.CENTER);

        add(wrapper);
    }

    public void switchView(String viewName) {
        cardLayout.show(cards, viewName);
        if ("HISTORY".equals(viewName)) {
            // Find and refresh history panel
            for (Component c : cards.getComponents()) {
                if (c instanceof HistoryPanel) {
                    ((HistoryPanel) c).refresh();
                }
            }
        }
    }

    public void refreshExams() {
        if (gridPanel != null) {
            gridPanel.removeAll();
            loadExamsFromAPI(gridPanel);
            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        class ScrollablePanel extends JPanel implements javax.swing.Scrollable {
            public ScrollablePanel() {
                super();
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(java.awt.Rectangle r, int o, int d) {
                return 16;
            }

            @Override
            public int getScrollableBlockIncrement(java.awt.Rectangle r, int o, int d) {
                return 50;
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }

            @Override
            public boolean getScrollableTracksViewportHeight() {
                return false;
            }
        }

        // Content Area
        ScrollablePanel homeContent = new ScrollablePanel();
        homeContent.setLayout(new BoxLayout(homeContent, BoxLayout.Y_AXIS));
        homeContent.setBackground(new Color(249, 250, 251));

        // 1. TOP BANNER
        homeContent.add(createBannerSection());

        // 2. EXAM SECTION
        JPanel examWrapper = new JPanel(new BorderLayout());
        examWrapper.setBackground(new Color(249, 250, 251));

        JLabel titleLabel = new JLabel("Danh sách toàn bộ đề thi", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setBorder(new EmptyBorder(40, 0, 30, 0));
        examWrapper.add(titleLabel, BorderLayout.NORTH);
        // Cards Grid with Dynamic Auto-Fit Layout
        GridLayout gridLayout = new GridLayout(0, 4, 25, 25);
        gridPanel = new JPanel(gridLayout);
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(new EmptyBorder(0, 40, 40, 40));

        gridPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int availableWidth = gridPanel.getWidth() - 80; // Minus L/R borders
                if (availableWidth <= 0)
                    return;
                int cols = Math.max(1, availableWidth / 280);
                if (gridLayout.getColumns() != cols) {
                    gridLayout.setColumns(cols);
                    gridPanel.revalidate();
                }
            }
        });

        loadExamsFromAPI(gridPanel);

        examWrapper.add(gridPanel, BorderLayout.CENTER);
        homeContent.add(examWrapper);

        JScrollPane scrollPane = new JScrollPane(homeContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
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
        sidebar.add(createMenuButton("Trang chủ", true, "HOME"));
        sidebar.add(createMenuButton("Thông tin cá nhân", false, "PROFILE"));
        sidebar.add(createMenuButton("Lịch sử làm bài", false, "HISTORY"));

        JButton refreshBtn = createMenuButton("Làm mới dữ liệu", false, null);
        refreshBtn.addActionListener(e -> refreshExams());
        sidebar.add(refreshBtn);

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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        if (viewName != null && !viewName.isEmpty()) {
            btn.addActionListener(e -> switchView(viewName));
        }
        btn.setForeground(active ? Color.WHITE : new Color(209, 213, 219));
        btn.setBackground(active ? new Color(55, 65, 81) : new Color(31, 41, 55));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setBorder(new EmptyBorder(0, 40, 0, 0)); // Indent the text beautifully
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (!active) {
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(55, 65, 81));
                    btn.setForeground(Color.WHITE);
                }

                public void mouseExited(MouseEvent e) {
                    btn.setBackground(new Color(31, 41, 55));
                    btn.setForeground(new Color(209, 213, 219));
                }
            });
        }
        return btn;
    }

    private JPanel createBannerSection() {
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setPreferredSize(new Dimension(10, 150));
        bannerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        bannerPanel.setBackground(new Color(37, 99, 235)); // Blue-600

        JLabel bannerText = new JLabel("KIỂM TRA NĂNG LỰC - LUYỆN ĐỀ ĐỈNH CAO", SwingConstants.CENTER);
        bannerText.setFont(new Font("Segoe UI", Font.BOLD, 28));
        bannerText.setForeground(Color.WHITE);

        bannerPanel.add(bannerText, BorderLayout.CENTER);
        return bannerPanel;
    }

    private void loadExamsFromAPI(JPanel gridPanel) {
        String jsonResponse = APIHelper.sendGet("get_exams.php");
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            gridPanel.add(new JLabel("Không thể lấy dữ liệu từ máy chủ."));
            return;
        }

        try {
            int dataIndex = jsonResponse.indexOf("\"data\":[");
            if (dataIndex != -1) {
                int startArr = jsonResponse.indexOf("[", dataIndex);
                int endArr = jsonResponse.lastIndexOf("]");
                if (startArr != -1 && endArr != -1 && endArr > startArr) {
                    String arrStr = jsonResponse.substring(startArr + 1, endArr);
                    if (arrStr.trim().isEmpty()) {
                        gridPanel.add(new JLabel("Chưa có đề thi nào."));
                        return;
                    }
                    String[] objects = arrStr.split("\\}\\s*,\\s*\\{");
                    for (String obj : objects) {
                        String idBaithi = extractBasic(obj, "id_baithi");
                        String ten = APIHelper.unescapeUnicode(extractBasic(obj, "ten_baithi"));
                        String thoiGian = extractBasic(obj, "thoigianlam");
                        String cauHoi = extractBasic(obj, "tongcauhoi");
                        String monHoc = APIHelper.unescapeUnicode(extractBasic(obj, "tenmonhoc"));
                        String isOngoing = extractBasic(obj, "is_ongoing");
                        String conLai = extractBasic(obj, "thoigianconlai");

                        gridPanel.add(createExamCard(idBaithi, ten, thoiGian + " phút", cauHoi + " câu hỏi", monHoc,
                                "1".equals(isOngoing), conLai));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            gridPanel.add(new JLabel("Lỗi phân tích dữ liệu."));
        }
    }

    private String extractBasic(String json, String key) {
        String val = APIHelper.extractJsonValue(json, key);
        if (val.isEmpty() && json.contains("\"" + key + "\"")) {
            // Fallback for non-string values (numbers/null)
            java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)")
                    .matcher(json);
            if (mn.find())
                return mn.group(1).replaceAll("[\\]\\}]", "").trim();
        }
        return val.isEmpty() ? "N/A" : val;
    }

    private JPanel createExamCard(String idBaithi, String title, String time, String questions, String category,
            boolean isOngoing, String conLai) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(25, 20, 25, 20)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(new Color(31, 41, 55));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel("Thời gian: " + time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timeLabel.setForeground(new Color(107, 114, 128));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel questionLabel = new JLabel("Số câu: " + questions);
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        questionLabel.setForeground(new Color(107, 114, 128));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryLabel.setForeground(new Color(37, 99, 235));
        categoryLabel.setBackground(new Color(239, 246, 255));
        categoryLabel.setOpaque(true);
        categoryLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String btnText = "Làm bài";
        if (isOngoing) {
            if (!conLai.isEmpty() && !conLai.equals("null")) {
                int sec = Integer.parseInt(conLai);
                btnText = "Làm tiếp (" + (sec / 60) + ":" + String.format("%02d", sec % 60) + ")";
            } else {
                btnText = "Làm tiếp";
            }
        }
        JButton button = new JButton(btnText);
        Color mainColor = isOngoing ? new Color(245, 158, 11) : new Color(59, 130, 246); // Amber for ongoing, Blue for
                                                                                         // new
        button.setBackground(isOngoing ? mainColor : Color.WHITE);
        button.setForeground(isOngoing ? Color.WHITE : mainColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(mainColor, 1, true),
                new EmptyBorder(10, 0, 10, 0)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        button.addActionListener(e -> {
            new ExamScreen(idBaithi, this);
            this.setVisible(false);
        });

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(timeLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(questionLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(categoryLabel);
        card.add(Box.createVerticalStrut(25));
        card.add(Box.createVerticalGlue());
        card.add(button);

        return card;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Home());
    }
}
