package com.mycompany.managerinventool;
//Librerias
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Random;

public class PSEApp extends JFrame {

    private final Color primaryColor = new Color(0, 0, 139);
    private final Color secondaryColor = new Color(255, 215, 0);
    private final Color bgGradientTop = new Color(230, 233, 255);
    private final Color bgGradientBottom = new Color(250, 250, 250);
    private final Color successColor = new Color(0, 120, 0);
    private final Color errorColor = new Color(200, 0, 0);

    private JComboBox<String> campoImpuesto, campoBanco, campoCuenta;
    private JTextField campoPeriodo, campoNIT, campoMonto, campoCorreo;
    private JButton botonPagar, botonVolver;
    private JTextArea areaResultado;

    public PSEApp() {
        setTitle("💳 Pagos PSE - Manager Inventool");
        setSize(650, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        // Fondo con gradiente
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, bgGradientTop, 0, getHeight(), bgGradientBottom);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Encabezado
        JLabel header = new JLabel("🛠️ Manager Inventool - Pagos PSE", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.setForeground(primaryColor);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, secondaryColor));
        header.setOpaque(true);
        header.setBackground(Color.WHITE);

        // Tarjeta del formulario
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));
        formCard.setPreferredSize(new Dimension(540, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("💰 Pago de Impuestos DIAN", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(primaryColor);
        titulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formCard.add(titulo, gbc);

        // Campos del formulario
        campoImpuesto = new JComboBox<>(new String[]{"IVA", "Renta", "ICA"});
        campoPeriodo = new JTextField();
        campoNIT = new JTextField();
        campoMonto = new JTextField();
        campoBanco = new JComboBox<>(new String[]{"Bancolombia", "Banco de Bogotá", "Davivienda", "BBVA"});
        campoCuenta = new JComboBox<>(new String[]{"Ahorros", "Corriente"});
        campoCorreo = new JTextField();

        agregarCampo(formCard, gbc, 1, "Tipo de Impuesto:", campoImpuesto);
        agregarCampo(formCard, gbc, 2, "Período (YYYY-MM):", campoPeriodo);
        agregarCampo(formCard, gbc, 3, "NIT de la Ferretería:", campoNIT);
        agregarCampo(formCard, gbc, 4, "Monto a Pagar:", campoMonto);
        agregarCampo(formCard, gbc, 5, "Banco:", campoBanco);
        agregarCampo(formCard, gbc, 6, "Tipo de Cuenta:", campoCuenta);
        agregarCampo(formCard, gbc, 7, "Correo Electrónico:", campoCorreo);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        botonPagar = new JButton("💳 Pagar Ahora");
        botonVolver = new JButton("⬅ Volver");
        styleButton(botonPagar, secondaryColor, primaryColor);
        styleButton(botonVolver, primaryColor, Color.WHITE);

        botonPagar.addActionListener(e -> procesarPago());
        botonVolver.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Volviendo a la página de inicio...")
        );

        buttonPanel.add(botonPagar);
        buttonPanel.add(botonVolver);

        gbc.gridy = 8;
        gbc.gridwidth = 2;
        formCard.add(buttonPanel, gbc);

        // Área de resultado
        areaResultado = new JTextArea(5, 25);
        areaResultado.setEditable(false);
        areaResultado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210), 1));
        areaResultado.setBackground(new Color(250, 250, 250));
        areaResultado.setMargin(new Insets(10, 10, 10, 10));

        gbc.gridy = 9;
        gbc.insets = new Insets(20, 0, 0, 0);
        formCard.add(new JScrollPane(areaResultado), gbc);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(formCard, BorderLayout.CENTER);
        add(mainPanel);

        setVisible(true);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String texto, JComponent campo) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(60, 60, 60));
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        panel.add(label, gbc);

        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(200, 35));
        if (campo instanceof JTextField) {
            ((JTextField) campo).setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        } else if (campo instanceof JComboBox) {
            ((JComboBox<?>) campo).setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        }

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(campo, gbc);
    }

    private void styleButton(JButton boton, Color bg, Color fg) {
        boton.setBackground(bg);
        boton.setForeground(fg);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBorder(new EmptyBorder(10, 20, 10, 20));
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { boton.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { boton.setBackground(bg); }
        });
    }

    private void procesarPago() {
        String impuesto = (String) campoImpuesto.getSelectedItem();
        String periodo = campoPeriodo.getText();
        String nit = campoNIT.getText();
        String montoTexto = campoMonto.getText();
        String banco = (String) campoBanco.getSelectedItem();
        String tipoCuenta = (String) campoCuenta.getSelectedItem();
        String correo = campoCorreo.getText();

        if (periodo.isEmpty() || nit.isEmpty() || montoTexto.isEmpty() || correo.isEmpty()) {
            mostrarMensaje("⚠️ Complete todos los campos obligatorios.", errorColor, new Color(255, 230, 230));
            return;
        }

        try {
            double monto = Double.parseDouble(montoTexto);
            areaResultado.setText("⏳ Procesando pago...");
            areaResultado.setForeground(primaryColor);
            areaResultado.setBackground(new Color(240, 248, 255));

            new javax.swing.Timer(2000, e -> {
                if (new Random().nextDouble() > 0.1) {
                    mostrarMensaje(
                            "✅ Pago exitoso de $" + String.format("%,.2f", monto) +
                                    "\nImpuesto: " + impuesto +
                                    "\nPeríodo: " + periodo +
                                    "\nBanco: " + banco + " (" + tipoCuenta + ")" +
                                    "\nNIT: " + nit +
                                    "\nRecibo enviado a: " + correo,
                            successColor, new Color(235, 255, 235));
                } else {
                    mostrarMensaje("❌ Error en el pago. Inténtalo de nuevo.", errorColor, new Color(255, 230, 230));
                }
                ((javax.swing.Timer)e.getSource()).stop();
            }).start();
        } catch (NumberFormatException ex) {
            mostrarMensaje("⚠️ Ingrese un monto válido.", errorColor, new Color(255, 230, 230));
        }
    }

    private void mostrarMensaje(String texto, Color fg, Color bg) {
        areaResultado.setText(texto);
        areaResultado.setForeground(fg);
        areaResultado.setBackground(bg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PSEApp::new);
    }
}
