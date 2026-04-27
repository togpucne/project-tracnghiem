package com.ptquiz.ui.admin;

import com.ptquiz.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class UserManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cbRoleFilter, cbStatusFilter;
    private JTextField txtSearch;
    private JButton btnFilter, btnReset, btnAdd;

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));
        
        initComponents();
        loadData();
    }

    private void initComponents() {
        // --- Header Section ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titlePanel = new JPanel(new GridLayout(0, 1));
        titlePanel.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("Quản lý người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.BLACK);
        titlePanel.add(lblTitle);
        
        JLabel lblSub = new JLabel("Quản lý và phân quyền tài khoản 'thí sinh' và 'giảng viên' qua API bảo mật.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(Color.BLACK);
        titlePanel.add(lblSub);

        btnAdd = new JButton("THÊM NGƯỜI DÙNG");
        styleStandardButton(btnAdd, new Color(219, 234, 254)); 
        btnAdd.addActionListener(e -> showAddEditDialog(-1, "", "", "thisinh", "active"));

        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(btnAdd, BorderLayout.EAST);

        // --- Filter Section ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(new Color(243, 244, 246));
        filterPanel.setBorder(new LineBorder(Color.BLACK, 1));

        filterPanel.add(createBoldLabel("VAI TRÒ:"));
        cbRoleFilter = new JComboBox<>(new String[]{"Tất cả", "Giảng viên", "Thí sinh"});
        cbRoleFilter.setForeground(Color.BLACK);
        filterPanel.add(cbRoleFilter);

        filterPanel.add(createBoldLabel("TRẠNG THÁI:"));
        cbStatusFilter = new JComboBox<>(new String[]{"Tất cả", "Hoạt động", "Khóa"});
        cbStatusFilter.setForeground(Color.BLACK);
        filterPanel.add(cbStatusFilter);

        filterPanel.add(createBoldLabel("TÌM KIẾM:"));
        txtSearch = new JTextField(15);
        txtSearch.setForeground(Color.BLACK);
        filterPanel.add(txtSearch);

        btnFilter = new JButton("LỌC");
        styleStandardButton(btnFilter, Color.WHITE);
        btnFilter.addActionListener(e -> loadData());

        btnReset = new JButton("LÀM MỚI");
        styleStandardButton(btnReset, Color.WHITE);
        btnReset.addActionListener(e -> {
            cbRoleFilter.setSelectedIndex(0);
            cbStatusFilter.setSelectedIndex(0);
            txtSearch.setText("");
            loadData();
        });

        filterPanel.add(btnFilter);
        filterPanel.add(btnReset);

        JPanel headerContainer = new JPanel(new BorderLayout(0, 15));
        headerContainer.setBackground(Color.WHITE);
        headerContainer.add(topPanel, BorderLayout.NORTH);
        headerContainer.add(filterPanel, BorderLayout.CENTER);
        add(headerContainer, BorderLayout.NORTH);

        // --- Table Section ---
        String[] cols = {"STT", "ID", "Họ tên", "Email", "Vai trò", "Trạng thái", "Ngày tạo", "Thao tác"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 7; }
        };

        table = new JTable(model);
        table.setRowHeight(55);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setGridColor(Color.BLACK);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        center.setForeground(Color.BLACK);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        
        table.getColumnModel().getColumn(4).setCellRenderer(new RoleBadgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());
        
        table.getColumnModel().getColumn(7).setMinWidth(220);
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionPanelRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new ActionPanelEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(Color.BLACK, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel createBoldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(Color.BLACK);
        return l;
    }

    public void loadData() {
        new Thread(() -> {
            String role = cbRoleFilter.getSelectedIndex() == 1 ? "giangvien" : (cbRoleFilter.getSelectedIndex() == 2 ? "thisinh" : "");
            String status = cbStatusFilter.getSelectedIndex() == 1 ? "active" : (cbStatusFilter.getSelectedIndex() == 2 ? "inactive" : "");
            String kw = txtSearch.getText().trim();

            String url = "admin/users/list?vaitro=" + role + "&trangthai=" + status + "&keyword=" + APIHelper.escapeJSON(kw);
            String json = APIHelper.sendGet(url);

            SwingUtilities.invokeLater(() -> {
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
                if (json == null || json.isEmpty()) return;
                model.setRowCount(0);
                try {
                    String dataStr = APIHelper.extractJsonValue(json, "data");
                    List<String> items = APIHelper.splitJsonArray(dataStr);
                    int stt = 1;
                    for (String item : items) {
                        String id = APIHelper.extractJsonValue(item, "id_nguoidung");
                        String ten = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(item, "ten"));
                        String email = APIHelper.extractJsonValue(item, "email");
                        String vt = APIHelper.extractJsonValue(item, "vaitro");
                        String tt = APIHelper.extractJsonValue(item, "trangthai");
                        String date = APIHelper.extractJsonValue(item, "ngaytao");
                        model.addRow(new Object[]{stt++, id, ten, email, vt, tt, date, ""});
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });
        }).start();
    }

    private void styleStandardButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.BLACK); 
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(new LineBorder(Color.BLACK, 1));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(160, 35));
    }

    // --- Badge Renderers ---
    class RoleBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Color.BLACK);
            if ("giangvien".equals(v)) { lbl.setText("Giảng viên"); }
            else { lbl.setText("Thí sinh"); }
            return lbl;
        }
    }

    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            lbl.setHorizontalAlignment(JLabel.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if ("active".equals(v)) {
                lbl.setText("Hoạt động");
                lbl.setBackground(new Color(187, 247, 208)); // Light Green
                lbl.setForeground(new Color(21, 128, 61)); // Dark Green
            } else {
                lbl.setText("Bị khóa");
                lbl.setBackground(new Color(254, 202, 202)); // Light Red
                lbl.setForeground(new Color(185, 28, 28)); // Dark Red
            }
            if (sel) lbl.setBackground(lbl.getBackground().darker());
            return lbl;
        }
    }

    // --- Action Panel Renderer/Editor ---
    class ActionPanelRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return buildActionPanel(r);
        }
    }
    class ActionPanelEditor extends DefaultCellEditor {
        public ActionPanelEditor() { super(new JCheckBox()); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return buildActionPanel(r); }
        @Override public Object getCellEditorValue() { return ""; }
    }

    private JPanel buildActionPanel(int row) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        p.setBackground(Color.WHITE);
        
        JButton btnSua = new JButton("SỬA");
        styleTableButton(btnSua);
        btnSua.addActionListener(e -> {
            int id = Integer.parseInt(model.getValueAt(row, 1).toString());
            String ten = model.getValueAt(row, 2).toString();
            String email = model.getValueAt(row, 3).toString();
            String role = model.getValueAt(row, 4).toString();
            String status = model.getValueAt(row, 5).toString();
            showAddEditDialog(id, ten, email, role, status);
        });

        JButton btnLock = new JButton("KHÓA");
        styleTableButton(btnLock);
        btnLock.addActionListener(e -> {
            String id = model.getValueAt(row, 1).toString();
            new Thread(() -> {
                APIHelper.sendPost("admin/users/toggle-status", "{\"id_nguoidung\":" + id + "}");
                loadData();
            }).start();
        });

        p.add(btnSua); p.add(btnLock);
        return p;
    }

    private void styleTableButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.BLACK);
        b.setBackground(Color.WHITE);
        b.setBorder(new LineBorder(Color.BLACK, 1));
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(80, 30));
    }

    // --- Dialog Thêm/Sửa ---
    private void showAddEditDialog(int id, String initTen, String initEmail, String initRole, String initStatus) {
        boolean isEdit = id > 0;
        JDialog dialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), isEdit ? "Cập nhật" : "Thêm mới", true);
        dialog.setSize(550, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel pnlHeader = new JPanel(new GridLayout(0, 1, 0, 5));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(25, 30, 10, 30));
        JLabel lblTitle = new JLabel(isEdit ? "Cập nhật người dùng" : "Thêm người dùng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.BLACK);
        pnlHeader.add(lblTitle);
        JLabel lblDesc = new JLabel("Chỉ tạo và quản lý tài khoản thí sinh, giảng viên qua API bảo mật.");
        lblDesc.setForeground(new Color(107, 114, 128));
        pnlHeader.add(lblDesc);
        dialog.add(pnlHeader, BorderLayout.NORTH);

        // Body
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        JTextField txtTen = new JTextField(initTen);
        JTextField txtEmail = new JTextField(initEmail);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"thisinh", "giangvien"});
        cbRole.setSelectedItem(initRole);
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"active", "inactive"});
        cbStatus.setSelectedItem(initStatus);
        JPasswordField txtPass = new JPasswordField();

        gbc.gridy = 0; gbc.gridx = 0; pnlBody.add(createInputGroup("Họ tên", txtTen), gbc);
        gbc.gridx = 1; pnlBody.add(createInputGroup("Email", txtEmail), gbc);
        gbc.gridy = 1; gbc.gridx = 0; pnlBody.add(createComboGroup("Vai trò", cbRole), gbc);
        gbc.gridx = 1; pnlBody.add(createComboGroup("Trạng thái", cbStatus), gbc);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel pnlP = new JPanel(new BorderLayout(0, 5));
        pnlP.setBackground(Color.WHITE);
        pnlP.add(createBoldLabel("Mật khẩu"), BorderLayout.NORTH);
        txtPass.setPreferredSize(new Dimension(0, 38));
        pnlP.add(txtPass, BorderLayout.CENTER);
        JLabel lblH = new JLabel(isEdit ? "Để trống nếu giữ nguyên mật khẩu cũ." : "Bắt buộc khi tạo mới. Ít nhất 6 ký tự.");
        lblH.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblH.setForeground(Color.BLACK);
        pnlP.add(lblH, BorderLayout.SOUTH);
        pnlBody.add(pnlP, gbc);
        dialog.add(pnlBody, BorderLayout.CENTER);

        // Footer
        JPanel pnlF = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlF.setBackground(new Color(249, 250, 251));
        pnlF.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        
        JButton btnClose = new JButton("ĐÓNG");
        styleStandardButton(btnClose, Color.WHITE);
        btnClose.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton("LƯU NGƯỜI DÙNG");
        styleStandardButton(btnSave, new Color(187, 247, 208)); 
        btnSave.addActionListener(e -> {
            String ten = txtTen.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());
            if (ten.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Họ tên không được để trống!"); return; }
            if (!Pattern.compile("^(.+)@(.+)$").matcher(email).matches()) { 
                JOptionPane.showMessageDialog(dialog, "Email không hợp lệ!"); return; 
            }
            if (!isEdit && pass.length() < 6) { 
                JOptionPane.showMessageDialog(dialog, "Mật khẩu mới phải từ 6 ký tự!"); return; 
            }
            if (isEdit && !pass.isEmpty() && pass.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu mới (nếu đổi) phải từ 6 ký tự!"); return;
            }
            String payload = String.format("{\"id_nguoidung\":%d, \"ten\":\"%s\", \"email\":\"%s\", \"vaitro\":\"%s\", \"trangthai\":\"%s\", \"matkhau\":\"%s\"}",
                id, APIHelper.escapeJSON(ten), APIHelper.escapeJSON(email), cbRole.getSelectedItem(), cbStatus.getSelectedItem(), APIHelper.escapeJSON(pass));
            new Thread(() -> {
                APIHelper.APIResponse res = APIHelper.sendPost("admin/users/save", payload);
                SwingUtilities.invokeLater(() -> {
                    if (res.success) { dialog.dispose(); loadData(); }
                    else JOptionPane.showMessageDialog(dialog, res.message);
                });
            }).start();
        });

        pnlF.add(btnClose); pnlF.add(btnSave);
        dialog.add(pnlF, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createInputGroup(String l, JTextField t) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Color.WHITE);
        p.add(createBoldLabel(l), BorderLayout.NORTH);
        t.setPreferredSize(new Dimension(0, 38));
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    private JPanel createComboGroup(String l, JComboBox<String> c) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Color.WHITE);
        p.add(createBoldLabel(l), BorderLayout.NORTH);
        c.setPreferredSize(new Dimension(0, 38));
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
