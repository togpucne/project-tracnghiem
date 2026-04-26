import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryPanel extends JPanel {

    private DefaultTableModel model;
    private JPanel paginationPanel;
    private List<Object[]> allHistoryData = new ArrayList<>();
    private int currentPage = 1;
    private final int ROWS_PER_PAGE = 20;

    public void refresh() {
        currentPage = 1;
        loadHistoryData();
    }

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Lịch sử làm bài thi", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(37, 99, 235)); // Blue-600
        title.setBorder(new EmptyBorder(0, 0, 30, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = { "STT", "Đề thi", "Thời gian bắt đầu", "Trạng thái", "Điểm số", "Hành động", "ID_LANTHI" };

        model = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(true);
        table.setGridColor(new Color(229, 231, 235));
        table.setBackground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(new Color(31, 41, 55));
        header.setPreferredSize(new Dimension(100, 50));

        // Custom Cell Renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setBorder(new EmptyBorder(0, 20, 0, 0));
                l.setHorizontalAlignment(JLabel.LEFT);
                l.setFont(new Font("Segoe UI", Font.BOLD, 14));
                l.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return l;
            }
        });
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        // Status Tag Renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        
        // Score Renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                if (value != null && !value.toString().equals("-")) {
                    l.setForeground(isSelected ? Color.WHITE : new Color(37, 99, 235));
                    l.setFont(new Font("Segoe UI", Font.BOLD, 14));
                }
                return l;
            }
        });

        // Action Column
        table.getColumnModel().getColumn(5).setCellRenderer(new ActionButtonRenderer());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 5) {
                    String idLanthi = (String) table.getModel().getValueAt(row, 6);
                    String tenBaithi = (String) table.getModel().getValueAt(row, 1);
                    String trangThai = (String) table.getModel().getValueAt(row, 3);
                    if ("Đã nộp bài".equals(trangThai)) {
                        JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(HistoryPanel.this);
                        new ResultDetailDialog(topFrame, idLanthi, tenBaithi).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(HistoryPanel.this, "Bài thi này chưa nộp, không thể xem chi tiết.");
                    }
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        // Hide ID_LANTHI column
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        paginationPanel.setBackground(new Color(249, 250, 251));
        paginationPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        add(paginationPanel, BorderLayout.SOUTH);

        loadHistoryData();
    }

    private void loadHistoryData() {
        new Thread(() -> {
            String jsonResponse = APIHelper.sendGet("history/list");
            if (jsonResponse == null || jsonResponse.isEmpty())
                return;

            allHistoryData.clear();
            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                model.addRow(new Object[] { "", "Đang tải dữ liệu...", "", "", "", "", "" });
            });
            try {
                int dataIndex = jsonResponse.indexOf("\"data\":[");
                if (dataIndex != -1) {
                    int startArr = jsonResponse.indexOf("[", dataIndex);
                    int endArr = jsonResponse.lastIndexOf("]");
                    if (startArr != -1 && endArr != -1 && endArr > startArr) {
                        String arrStr = jsonResponse.substring(startArr + 1, endArr);
                        if (arrStr.trim().isEmpty())
                            return;

                        String[] objects = arrStr.split("\\}\\s*,\\s*\\{");
                        int stt = 1;
                        for (String obj : objects) {
                             String idLanthi = extractBasic(obj, "id_lanthi");
                             String ten = APIHelper.unescapeUnicode(extractBasic(obj, "ten_baithi"));
                             String thoiGian = extractBasic(obj, "thoigianbatdau");
                             String trangThai = extractBasic(obj, "trangthai");
                             String diem = extractBasic(obj, "diem");
 
                             if (trangThai.equalsIgnoreCase("ongoing") || "null".equals(diem) || diem.isEmpty()) {
                                 continue; // Hide ongoing from history
                             } else {
                                 trangThai = "Đã nộp bài";
                                 if (!diem.equals("N/A") && !diem.equals("null")) {
                                     try {
                                         double d = Double.parseDouble(diem);
                                         if (d == (long) d) diem = String.format("%d", (long) d);
                                         else diem = String.format("%.1f", d);
                                     } catch (Exception e) {}
                                     diem += " / 10";
                                 } else {
                                     diem = "0 / 10";
                                 }
                             }
 
                             allHistoryData.add(new Object[] { String.valueOf(stt++), ten, thoiGian, trangThai, diem, "Xem chi tiết", idLanthi });
                        }

                        SwingUtilities.invokeLater(() -> refreshPage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void refreshPage() {
        model.setRowCount(0); // Clear table
        int start = (currentPage - 1) * ROWS_PER_PAGE;
        int end = Math.min(start + ROWS_PER_PAGE, allHistoryData.size());

        for (int i = start; i < end; i++) {
            model.addRow(allHistoryData.get(i));
        }

        updatePaginationUI();
    }

    private void updatePaginationUI() {
        paginationPanel.removeAll();

        int rawTotal = (int) Math.ceil((double) allHistoryData.size() / ROWS_PER_PAGE);
        final int totalPages = rawTotal == 0 ? 1 : rawTotal;

        JButton prevBtn = new JButton("Trước");
        stylePaginationButton(prevBtn, currentPage > 1);
        prevBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshPage();
            }
        });
        paginationPanel.add(prevBtn);

        // Page numbers
        for (int i = 1; i <= totalPages; i++) {
            JButton pageBtn = new JButton(String.valueOf(i));
            final int page = i;

            pageBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            pageBtn.setFocusPainted(false);
            pageBtn.setContentAreaFilled(false);
            pageBtn.setOpaque(true);
            pageBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            pageBtn.setPreferredSize(new Dimension(40, 35));

            if (page == currentPage) {
                pageBtn.setBackground(new Color(37, 99, 235));
                pageBtn.setForeground(Color.WHITE);
                pageBtn.setBorder(BorderFactory.createLineBorder(new Color(37, 99, 235)));
            } else {
                pageBtn.setBackground(Color.WHITE);
                pageBtn.setForeground(new Color(37, 99, 235));
                pageBtn.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
            }

            pageBtn.addActionListener(e -> {
                if (currentPage != page) {
                    currentPage = page;
                    refreshPage();
                }
            });
            paginationPanel.add(pageBtn);
        }

        JButton nextBtn = new JButton("Sau");
        stylePaginationButton(nextBtn, currentPage < totalPages);
        nextBtn.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                refreshPage();
            }
        });
        paginationPanel.add(nextBtn);

        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    private void stylePaginationButton(JButton btn, boolean enabled) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(enabled ? new Color(31, 41, 55) : new Color(156, 163, 175));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        if (enabled) {
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            btn.setEnabled(false);
        }
        btn.setPreferredSize(new Dimension(80, 35));
    }

    private String extractBasic(String json, String key) {
        String val = APIHelper.extractJsonValue(json, key);
        if (val.isEmpty() && json.contains("\"" + key + "\"")) {
            // Fallback for non-string values (numbers/null)
            java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)").matcher(json);
            if (mn.find())
                return mn.group(1).replaceAll("[\\]\\}]", "").trim();
        }
        return val.isEmpty() ? "N/A" : val;
    }

    // --- INNER CLASSES FOR RENDERERS ---

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (value != null) ? value.toString() : "";
            JPanel p = new JPanel(new GridBagLayout());
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            JLabel label = new JLabel(status);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            label.setOpaque(true);
            
            if (status.equals("Đang làm dở")) {
                label.setBackground(new Color(254, 243, 199)); // Amber 100
                label.setForeground(new Color(146, 64, 14));   // Amber 800
            } else {
                label.setBackground(new Color(209, 250, 229)); // Emerald 100
                label.setForeground(new Color(6, 78, 59));     // Emerald 800
            }
            
            p.add(label);
            return p;
        }
    }

    class ActionButtonRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            p.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            
            JButton btn = new JButton("Xem chi tiết");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setBackground(new Color(229, 231, 235)); // Light gray background
            btn.setForeground(Color.BLACK); // Black text
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(156, 163, 175)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBorderPainted(true);
            
            p.add(btn);
            return p;
        }
    }
}
