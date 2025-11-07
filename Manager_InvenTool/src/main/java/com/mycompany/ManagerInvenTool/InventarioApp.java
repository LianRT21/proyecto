package com.mycompany.managerinventool;
// NOTA: No borrar los comentarios
// Ventana Inventario - Muestra productos directamente desde PostgreSQL

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel; // 🔹 Import necesario
import java.awt.*;
import java.sql.*;

public class InventarioApp extends JFrame {

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla; // 🔹 Ahora reconocido correctamente

    // Colores y fuentes coherentes con IndexApp
    private final Color primaryColor = new Color(0, 0, 139);
    private final Color secondaryColor = new Color(255, 215, 0);
    private final Color bgColor = new Color(244, 244, 244);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 20);
    private final Font bodyFont = new Font("Segoe UI", Font.PLAIN, 14);

    public InventarioApp() {
        setTitle("Inventario - Manager Inventool");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior
        JLabel titulo = new JLabel("📦 Inventario de Productos", SwingConstants.CENTER);
        titulo.setFont(headerFont);
        titulo.setForeground(primaryColor);
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(titulo, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripción", "Precio", "Stock"}, 0);
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setFont(bodyFont);
        tablaProductos.setRowHeight(25);
        tablaProductos.setGridColor(Color.LIGHT_GRAY);
        tablaProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaProductos.getTableHeader().setBackground(primaryColor);
        tablaProductos.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        add(scrollPane, BorderLayout.CENTER);

        // Botones CRUD
        JPanel panelBotones = new JPanel();
        panelBotones.setBackground(bgColor);
        JButton btnRefrescar = new JButton("🔄 Actualizar");
        JButton btnCerrar = new JButton("⬅️ Volver");

        styleButton(btnRefrescar);
        styleButton(btnCerrar);

        panelBotones.add(btnRefrescar);
        panelBotones.add(btnCerrar);
        add(panelBotones, BorderLayout.SOUTH);

        // Acciones
        btnRefrescar.addActionListener(e -> cargarProductos());
        btnCerrar.addActionListener(e -> dispose());

        // Cargar datos iniciales
        cargarProductos();
    }

    private void cargarProductos() {
        modeloTabla.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/Manager_InvenTool_DB",
                "postgres", "qwerty123")) {

            String query = "SELECT id_producto, nombre, descripcion, precio_unitario, stock FROM productos ORDER BY id_producto ASC";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modeloTabla.addRow(new Object[]{
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    "$" + rs.getBigDecimal("precio_unitario"),
                    rs.getInt("stock")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar productos:\n" + e.getMessage(),
                "Error de conexión",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleButton(JButton btn) {
        btn.setBackground(secondaryColor);
        btn.setForeground(primaryColor);
        btn.setFont(bodyFont);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(primaryColor, 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventarioApp().setVisible(true));
    }
}
