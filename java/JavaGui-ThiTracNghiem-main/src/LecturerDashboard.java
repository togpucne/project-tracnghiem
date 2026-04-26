import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LecturerDashboard extends JPanel {
    private JPanel statsGrid;

    public LecturerDashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(249, 250, 251));
        headerPanel.setBorder(new EmptyBorder(0, 0, 40, 0));

        JLabel welcomeLabel = new JLabel("Chào mừng, " + UserSession.ten + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(15, 23, 42));

        JLabel subLabel = new JLabel("Theo dõi hiệu suất giảng dạy và kết quả thi của sinh viên.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(new Color(100, 116, 139));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(249, 250, 251));
        textPanel.add(welcomeLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(subLabel);

        headerPanel.add(textPanel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Stats Grid
        statsGrid = new JPanel(new GridLayout(1, 4, 25, 0));
        statsGrid.setBackground(new Color(249, 250, 251));
        add(statsGrid, BorderLayout.CENTER);

        loadStats();
    }

    public void loadStats() {
        new Thread(() -> {
            String jsonResponse = APIHelper.sendGet("lecturer/stats");
            if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.contains("\"error\"")) {
                return;
            }

            try {
                String subjects = extractBasic(jsonResponse, "subjects");
                String exams = extractBasic(jsonResponse, "exams");
                String questions = extractBasic(jsonResponse, "questions");
                String attempts = extractBasic(jsonResponse, "attempts");

                SwingUtilities.invokeLater(() -> {
                    statsGrid.removeAll();
                    statsGrid.add(createStatCard("Môn học", subjects, new Color(59, 130, 246), new Color(239, 246, 255)));
                    statsGrid.add(createStatCard("Bài thi", exams, new Color(8, 145, 178), new Color(236, 254, 255)));
                    statsGrid.add(createStatCard("Câu hỏi", questions, new Color(245, 158, 11), new Color(255, 247, 237)));
                    statsGrid.add(createStatCard("Lượt thi", attempts, new Color(16, 185, 129), new Color(240, 253, 244)));
                    statsGrid.revalidate();
                    statsGrid.repaint();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JPanel createStatCard(String title, String value, Color iconColor, Color bgColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(241, 245, 249), 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setPreferredSize(new Dimension(52, 52));
        iconPanel.setBackground(bgColor);
        iconPanel.setBorder(BorderFactory.createEmptyBorder());
        // In a real app we'd use FontAwesome or similar, here we just use background color
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(100, 116, 139));

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(new Color(30, 41, 59));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valLabel);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private String extractBasic(String json, String key) {
        // Simple search for "key": value or "key": "value"
        java.util.regex.Matcher ms = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (ms.find()) return ms.group(1);

        java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)").matcher(json);
        if (mn.find()) return mn.group(1).replaceAll("[\\]\\}]", "").trim();

        return "0";
    }
}
