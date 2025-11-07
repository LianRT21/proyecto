package com.mycompany.managerinventool;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class VentasApp extends JFrame {

    static class Producto {
        int id;
        String nombre;
        double precio;
        int stock;

        Producto(int id, String nombre, double precio, int stock) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.stock = stock;
        }

        @Override
        public String toString() {
            return nombre + " (Stock: " + stock + ")";
        }
    }

    static class Venta {
        String fecha;
        String cliente;
        String producto;
        int cantidad;
        double total;
        String metodoPago;

        Venta(String fecha, String cliente, String producto, int cantidad, double total, String metodoPago) {
            this.fecha = fecha;
            this.cliente = cliente;
            this.producto = producto;
            this.cantidad = cantidad;
            this.total = total;
            this.metodoPago = metodoPago;
        }
    }

    private ArrayList<Producto> productos = new ArrayList<>();
    private ArrayList<Venta> ventas = new ArrayList<>();

    private JTextField clienteField;
    private JComboBox<Producto> productoBox;
    private JSpinner cantidadSpinner;
    private JLabel precioLabel, stockLabel, totalLabel;
    private JComboBox<String> metodoPagoBox;
    private DefaultTableModel tableModel;

    public VentasApp() {
        setTitle("Registro de Ventas - El Martillo Dorado");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Color primaryColor = new Color(0, 0, 139);
        Color bgColor = new Color(244, 244, 244);

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        mainPanel.setBackground(bgColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ==== FORMULARIO DE REGISTRO ====
        JPanel registroPanel = new JPanel();
        registroPanel.setLayout(new BoxLayout(registroPanel, BoxLayout.Y_AXIS));
        registroPanel.setBackground(Color.WHITE);
        registroPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titulo = new JLabel("Registrar Nueva Venta");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        registroPanel.add(titulo);
        registroPanel.add(Box.createVerticalStrut(8));

        // === Campo Cliente ===
        registroPanel.add(new JLabel("Número de Identificación del Cliente:"));
        clienteField = new JTextField();
        clienteField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clienteField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        clienteField.setPreferredSize(new Dimension(200, 28));
        registroPanel.add(clienteField);
        registroPanel.add(Box.createVerticalStrut(6));


        // === Campo Producto ===
        registroPanel.add(new JLabel("Producto:"));
        productoBox = new JComboBox<>();
        productoBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        productoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        registroPanel.add(productoBox);
        registroPanel.add(Box.createVerticalStrut(6));

        // === Campo Cantidad ===
        registroPanel.add(new JLabel("Cantidad:"));
        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        ((JSpinner.DefaultEditor) cantidadSpinner.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cantidadSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        registroPanel.add(cantidadSpinner);
        registroPanel.add(Box.createVerticalStrut(6));

        // === Información de precio y stock ===
        precioLabel = new JLabel("Precio unitario: $0.00");
        stockLabel = new JLabel("Stock disponible: 0");
        totalLabel = new JLabel("Total: $0.00");
        registroPanel.add(precioLabel);
        registroPanel.add(stockLabel);
        registroPanel.add(totalLabel);
        registroPanel.add(Box.createVerticalStrut(8));

        // === Método de pago===
        registroPanel.add(new JLabel("Método de Pago:"));
        metodoPagoBox = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        metodoPagoBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        metodoPagoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        registroPanel.add(metodoPagoBox);
        registroPanel.add(Box.createVerticalStrut(10));

        // === Botón Registrar ===
        JButton registrarBtn = new JButton("Registrar Venta");
        registrarBtn.setBackground(primaryColor);
        registrarBtn.setForeground(Color.WHITE);
        registrarBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registrarBtn.setFocusPainted(false);
        registrarBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registroPanel.add(registrarBtn);

        mainPanel.add(registroPanel);

        // ==== HISTORIAL ====
        JPanel historialPanel = new JPanel(new BorderLayout());
        historialPanel.setBackground(Color.WHITE);
        historialPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel tituloHistorial = new JLabel("Historial de Ventas");
        tituloHistorial.setFont(new Font("Segoe UI", Font.BOLD, 18));
        historialPanel.add(tituloHistorial, BorderLayout.NORTH);

        String[] columnas = {"Fecha", "Cliente", "Producto", "Cantidad", "Total", "Método de Pago"};
        tableModel = new DefaultTableModel(columnas, 0);
        JTable tablaVentas = new JTable(tableModel);
        historialPanel.add(new JScrollPane(tablaVentas), BorderLayout.CENTER);

        mainPanel.add(historialPanel);
        add(mainPanel, BorderLayout.CENTER);

        // ==== EVENTOS ====
        clienteField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || clienteField.getText().length() >= 12) {
                    e.consume();
                }
            }
        });

        productoBox.addActionListener(e -> actualizarInfoProducto());
        cantidadSpinner.addChangeListener(e -> actualizarTotal());
        registrarBtn.addActionListener(e -> registrarVenta());

        // ==== CARGAR PRODUCTOS ====
        cargarProductosDemo();
        actualizarSelectProductos();
    }

    private void cargarProductosDemo() {
        productos.add(new Producto(1, "Taladro Eléctrico", 99999, 10));
        productos.add(new Producto(2, "Sierra Circular", 129999, 5));
        productos.add(new Producto(3, "Juego de Destornilladores", 49999, 20));
    }

    private void actualizarSelectProductos() {
        productoBox.removeAllItems();
        for (Producto p : productos) {
            if (p.stock > 0) productoBox.addItem(p);
        }
        actualizarInfoProducto();
    }

    private void actualizarInfoProducto() {
        Producto seleccionado = (Producto) productoBox.getSelectedItem();
        if (seleccionado != null) {
            precioLabel.setText("Precio unitario: $" + seleccionado.precio);
            stockLabel.setText("Stock disponible: " + seleccionado.stock);
            actualizarTotal();
        }
    }

    private void actualizarTotal() {
        Producto seleccionado = (Producto) productoBox.getSelectedItem();
        if (seleccionado != null) {
            int cantidad = (Integer) cantidadSpinner.getValue();
            double total = cantidad * seleccionado.precio;
            totalLabel.setText("Total: $" + total);
        }
    }

    private void registrarVenta() {
        Producto seleccionado = (Producto) productoBox.getSelectedItem();
        if (seleccionado == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto válido");
            return;
        }
        String cliente = clienteField.getText();
        if (cliente.length() < 6) {
            JOptionPane.showMessageDialog(this, "El número de identificación debe tener al menos 6 dígitos");
            return;
        }
        int cantidad = (Integer) cantidadSpinner.getValue();
        if (cantidad > seleccionado.stock) {
            JOptionPane.showMessageDialog(this, "La cantidad supera el stock disponible");
            return;
        }

        double total = cantidad * seleccionado.precio;
        String fecha = java.time.LocalDate.now().toString();
        String metodo = metodoPagoBox.getSelectedItem().toString();

        ventas.add(new Venta(fecha, cliente, seleccionado.nombre, cantidad, total, metodo));

        seleccionado.stock -= cantidad;
        actualizarSelectProductos();

        tableModel.addRow(new Object[]{fecha, cliente, seleccionado.nombre, cantidad, "$" + total, metodo});
        clienteField.setText("");
        cantidadSpinner.setValue(1);
        totalLabel.setText("Total: $0.00");

        JOptionPane.showMessageDialog(this, "Venta registrada exitosamente");
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new VentasApp().setVisible(true));
    }
}
