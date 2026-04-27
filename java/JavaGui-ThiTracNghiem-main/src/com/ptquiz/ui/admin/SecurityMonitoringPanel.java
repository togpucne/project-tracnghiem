package com.ptquiz.ui.admin;

import com.ptquiz.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SecurityMonitoringPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbStatusFilter;
    private JTextField txtSearch;
    private JButton btnFilter, btnReset;

    private final Color COLOR_BORDER = Color.BLACK;
    private final Color COLOR_TEXT   = Color.BLACK;

    public SecurityMonitoringPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // --- 1. Header ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new GridLayout(0, 1));
        titlePanel.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("Giám sát bảo mật API");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT);
        titlePanel.add(lblTitle);
        
        JLabel lblSub = new JLabel("Theo dõi nhật ký truy cập thời gian thực của Thí sinh, Giảng viên và Khách.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(COLOR_TEXT);
        titlePanel.add(lblSub);
        topPanel.add(titlePanel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Filter & Table Container ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(Color.WHITE);

        // Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(new Color(243, 244, 246));
        filterPanel.setBorder(new LineBorder(COLOR_BORDER, 1));

        filterPanel.add(createBoldLabel("TRẠNG THÁI:"));
        cbStatusFilter = new JComboBox<>(new String[]{"Tất cả", "Thành công (2xx)", "Lỗi Client (4xx)", "Lỗi Server (5xx)"});
        cbStatusFilter.setForeground(COLOR_TEXT);
        filterPanel.add(cbStatusFilter);

        filterPanel.add(createBoldLabel("TÌM KIẾM:"));
        txtSearch = new JTextField(15);
        txtSearch.setForeground(COLOR_TEXT);
        filterPanel.add(txtSearch);

        btnFilter = new JButton("LỌC");
        styleStandardButton(btnFilter, Color.WHITE);
        btnFilter.addActionListener(e -> loadData());

        btnReset = new JButton("LÀM MỚI");
        styleStandardButton(btnReset, Color.WHITE);
        btnReset.addActionListener(e -> {
            cbStatusFilter.setSelectedIndex(0);
            txtSearch.setText("");
            loadData();
        });

        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);
        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Thời gian", "Người dùng", "Method", "Endpoint", "IP Address", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(COLOR_TEXT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setForeground(COLOR_TEXT);
        table.setGridColor(COLOR_BORDER);

        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(COLOR_BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JLabel createBoldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(COLOR_TEXT);
        return l;
    }

    private void styleStandardButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(COLOR_TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new LineBorder(COLOR_BORDER, 1));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(120, 32));
    }

    public void loadData() {
        new Thread(() -> {
            String status = "";
            int idx = cbStatusFilter.getSelectedIndex();
            if (idx == 1) status = "200";
            else if (idx == 2) status = "400"; // API model handles >= 400
            else if (idx == 3) status = "500";

            String kw = txtSearch.getText().trim();
            String url = String.format("admin/logs/list?status_code=%s&keyword=%s", status, APIHelper.escapeJSON(kw));
            String json = APIHelper.sendGet(url);

            SwingUtilities.invokeLater(() -> {
                if (json == null || json.isEmpty()) return;
                model.setRowCount(0);
                try {
                    String dataPart = APIHelper.extractJsonValue(json, "data");
                    List<String> items = APIHelper.splitJsonArray(dataPart);
                    for (String item : items) {
                        String time = APIHelper.extractJsonValue(item, "thoigian"); // FIXED FIELD NAME
                        String user = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(item, "ten"));
                        if (user.isEmpty()) user = "Khách vãng lai";
                        
                        String method = APIHelper.extractJsonValue(item, "method");
                        String endpoint = APIHelper.extractJsonValue(item, "endpoint");
                        String ip = APIHelper.extractJsonValue(item, "ip_address");
                        String status_code = APIHelper.extractJsonValue(item, "response_code");
                        
                        model.addRow(new Object[]{time, user, method, endpoint, ip, status_code});
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });
        }).start();
    }

    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            try {
                int code = Integer.parseInt(v.toString());
                if (code >= 500) lbl.setForeground(new Color(185, 28, 28)); // Red
                else if (code >= 400) lbl.setForeground(new Color(180, 83, 9)); // Orange
                else if (code >= 200) lbl.setForeground(new Color(21, 128, 61)); // Green
            } catch (Exception e) { lbl.setForeground(COLOR_TEXT); }
            return lbl;
        }
    }
}
