package com.ptquiz.ui.admin;

import com.ptquiz.core.APIHelper;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;

public class SecurityMonitoringPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbMethodFilter;
    private JTextField txtSearch;
    private JSpinner spinDate;
    private JButton btnFilter, btnRefresh;
    private Timer refreshTimer;

    public SecurityMonitoringPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Giám sát bảo mật API");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.BLACK);
        
        JLabel subTitleLabel = new JLabel("Theo dõi nhật ký truy cập. Bấm 'Khóa' để chặn tài khoản vi phạm.");
        subTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subTitleLabel.setForeground(new Color(100, 100, 100));
        
        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 2, 2));
        titleGroup.setBackground(Color.WHITE);
        titleGroup.add(titleLabel);
        titleGroup.add(subTitleLabel);
        headerPanel.add(titleGroup, BorderLayout.WEST);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.setBackground(new Color(248, 249, 250));
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        filterBar.add(new JLabel("METHOD:"));
        cbMethodFilter = new JComboBox<>(new String[]{"Tất cả", "GET", "POST", "PUT", "PATCH", "DELETE"});
        cbMethodFilter.setPreferredSize(new Dimension(80, 25));
        filterBar.add(cbMethodFilter);

        filterBar.add(new JLabel("NGÀY:"));
        // Date Spinner chuyên nghiệp
        spinDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinDate, "yyyy-MM-dd");
        spinDate.setEditor(dateEditor);
        spinDate.setValue(new Date());
        spinDate.setPreferredSize(new Dimension(110, 25));
        filterBar.add(spinDate);

        filterBar.add(new JLabel("TÌM KIẾM:"));
        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(150, 25));
        filterBar.add(txtSearch);

        btnFilter = new JButton("LỌC");
        btnFilter.setPreferredSize(new Dimension(60, 25));
        btnFilter.setBackground(Color.WHITE);
        btnFilter.setForeground(Color.BLACK);
        filterBar.add(btnFilter);

        btnRefresh = new JButton("RESET");
        btnRefresh.setPreferredSize(new Dimension(70, 25));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setForeground(Color.BLACK);
        filterBar.add(btnRefresh);

        add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(filterBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // Table
        String[] cols = {"ID", "Thời gian", "Người dùng", "Trạng thái", "Method", "Endpoint", "IP Address", "Thao tác", "ID_User", "VaiTro"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return c == 7; }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        int[] hideCols = {0, 8, 9};
        for (int cIdx : hideCols) {
            table.getColumnModel().getColumn(cIdx).setMinWidth(0);
            table.getColumnModel().getColumn(cIdx).setMaxWidth(0);
            table.getColumnModel().getColumn(cIdx).setPreferredWidth(0);
        }

        table.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        btnFilter.addActionListener(e -> loadData());
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            spinDate.setValue(new Date());
            cbMethodFilter.setSelectedIndex(0);
            loadData();
        });

        refreshTimer = new Timer(30000, e -> loadData());
        refreshTimer.start();

        loadData();
    }

    public void loadData() {
        new Thread(() -> {
            try {
                String method = cbMethodFilter.getSelectedIndex() == 0 ? "" : cbMethodFilter.getSelectedItem().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format((Date)spinDate.getValue());
                String kw = txtSearch.getText().trim();
                String url = String.format("admin/logs/list?method=%s&date=%s&keyword=%s", 
                        method, date, APIHelper.escapeJSON(kw));
                
                String json = APIHelper.sendGet(url);
                
                SwingUtilities.invokeLater(() -> {
                    if (json == null || json.isEmpty()) {
                        model.setRowCount(0);
                        return;
                    }

                    String dataPart = APIHelper.extractJsonValue(json, "data");
                    List<String> items = APIHelper.splitJsonArray(dataPart);
                    
                    model.setRowCount(0);
                    for (String item : items) {
                        String idLog = APIHelper.extractJsonValue(item, "id_log");
                        String time = APIHelper.extractJsonValue(item, "created_at");
                        if (time.isEmpty()) time = APIHelper.extractJsonValue(item, "thoigian");
                        
                        String user = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(item, "ten"));
                        if (user.isEmpty() || user.equals("null")) user = "Khách vãng lai";
                        
                        String statusStr = "";
                        String trangthai = APIHelper.extractJsonValue(item, "trangthai");
                        if (trangthai.equals("active")) statusStr = "Đang hoạt động";
                        else if (trangthai.equals("locked")) statusStr = "Đã khóa";
                        else if (!trangthai.isEmpty()) statusStr = trangthai;

                        String mth = APIHelper.extractJsonValue(item, "method");
                        String endpoint = APIHelper.extractJsonValue(item, "endpoint");
                        String ip = APIHelper.extractJsonValue(item, "ip_address");
                        String idUser = APIHelper.extractJsonValue(item, "id_nguoidung");
                        String vaitro = APIHelper.extractJsonValue(item, "vaitro");
                        
                        String action = "Khóa";
                        if (idUser == null || idUser.isEmpty() || idUser.equals("null") || (vaitro != null && vaitro.equals("admin"))) {
                            action = "";
                        }
                        
                        model.addRow(new Object[]{idLog, time, user, statusStr, mth, endpoint, ip, action, idUser, vaitro});
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    class ActionButtonRenderer extends JButton implements TableCellRenderer {
        public ActionButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String label = (value == null) ? "" : value.toString();
            if (label.isEmpty()) {
                return new JLabel("");
            }
            
            setText(label);
            setBackground(new Color(255, 193, 7));
            setForeground(Color.BLACK);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            
            String trangthai = table.getModel().getValueAt(row, 3).toString();
            if (trangthai.equals("Đã khóa")) {
                setEnabled(false);
                setBackground(Color.LIGHT_GRAY);
            } else {
                setEnabled(true);
            }
            
            return this;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow;

        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row;
            label = (value == null) ? "" : value.toString();
            if (label.isEmpty()) {
                isPushed = false;
                return new JLabel(""); 
            }
            button.setText(label);
            button.setBackground(new Color(255, 193, 7));
            button.setForeground(Color.BLACK);
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                String idUser = model.getValueAt(selectedRow, 8).toString();
                String userName = model.getValueAt(selectedRow, 2).toString();
                
                int confirm = JOptionPane.showConfirmDialog(button, 
                    "Bạn có chắc muốn KHÓA tài khoản của '" + userName + "' không?", 
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    String jsonInput = String.format("{\"id_nguoidung\":%s, \"trangthai\":\"locked\"}", idUser);
                    APIHelper.APIResponse res = APIHelper.sendPost("admin/users/toggle-status", jsonInput);
                    if (res.success) {
                        JOptionPane.showMessageDialog(button, "Đã khóa tài khoản thành công!");
                        // Sử dụng invokeLater để tránh crash bảng khi đang editing
                        SwingUtilities.invokeLater(() -> loadData());
                    } else {
                        JOptionPane.showMessageDialog(button, "Lỗi: " + res.message);
                    }
                }
            }
            isPushed = false;
            return label;
        }
    }
}
