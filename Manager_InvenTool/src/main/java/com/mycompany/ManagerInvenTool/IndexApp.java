package com.mycompany.managerinventool;
//NOTA: No borrar los comentarios porfis

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class IndexApp extends JFrame {

    private ArrayList<String> carrito = new ArrayList<>();

    // Colores
    private final Color primaryColor = new Color(0, 0, 139);
    private final Color secondaryColor = new Color(255, 215, 0); // Dorado
    private final Color bgColor = new Color(244, 244, 244);
    private final Color textColor = new Color(51, 51, 51);
    private final Color hoverColor = new Color(230, 230, 255);

    // Fuentes
    private final Font logoFont = new Font("Segoe UI", Font.BOLD, 24);
    private final Font headerFont = new Font("Segoe UI", Font.BOLD, 18);
    private final Font bodyFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final int MAX_CONTENT_WIDTH = 1000;
    
    private NetworkChatDialog chatDialog; 

    public IndexApp() {
        setTitle("Manager Inventool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);
        
        JLayeredPane layeredPane = getRootPane().getLayeredPane();
        
        // Esto para iniciar ventana de chat flotante
        chatDialog = new NetworkChatDialog(this);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBounds(0, 0, getWidth(), getHeight()); 
        
        // HEADER
        contentPanel.add(createHeader(), BorderLayout.NORTH);
        
        // MAIN CONTENT (con scroll)
        JScrollPane scrollPane = new JScrollPane(createMainContentWrapper());
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(bgColor);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);
        setupFloatingChatButton(layeredPane);
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                contentPanel.setBounds(0, 0, getWidth(), getHeight());
                setupFloatingChatButton(layeredPane);
            }
        });
    }
    
    private JPanel createMainContentWrapper() {
        JPanel mainContentWrapper = new JPanel(new BorderLayout());
        mainContentWrapper.add(createMainContent(), BorderLayout.CENTER);
        return mainContentWrapper;
    }
    
    // esto para configurar y posicionar el Botón Flotante (soportee)
    private void setupFloatingChatButton(JLayeredPane layeredPane) {
        
        final int BUTTON_WIDTH = 120;
        final int BUTTON_HEIGHT = 40;
        final int MARGIN = 50; 
        Component[] components = layeredPane.getComponentsInLayer(JLayeredPane.PALETTE_LAYER);
        JButton chatButton = null;

        for (Component comp : components) {
            if (comp instanceof JButton && comp.getName() != null && comp.getName().equals("floatingChatButton")) {
                chatButton = (JButton) comp;
                break;
            }
        }
        if (chatButton == null) {
            chatButton = new JButton("💬 Soporte");
            chatButton.setName("floatingChatButton");
            chatButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            chatButton.setBackground(secondaryColor);
            chatButton.setForeground(primaryColor);
            chatButton.setFocusPainted(false);
            chatButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            chatButton.addActionListener(e -> {
                if (chatDialog.isVisible()) {
                    chatDialog.setVisible(false);
                } else {
                    int x = getLocationOnScreen().x + getWidth() - chatDialog.getWidth() - MARGIN;
                    int y = getLocationOnScreen().y + getHeight() - chatDialog.getHeight() - MARGIN - BUTTON_HEIGHT; 
                    chatDialog.setLocation(x, y);
                    chatDialog.setVisible(true);
                }
            });
            
            layeredPane.add(chatButton, JLayeredPane.PALETTE_LAYER);
        }

        // Esto para que el boton(soporyte) sea estatico
        int xPos = getWidth() - BUTTON_WIDTH - MARGIN;
        int yPos = getHeight() - BUTTON_HEIGHT - MARGIN;

        chatButton.setBounds(xPos, yPos, BUTTON_WIDTH, BUTTON_HEIGHT);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private class NetworkChatDialog extends JDialog {
        private static final String SERVER_ADDRESS = "localhost";
        private static final int SERVER_PORT = 12345;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        
        private JTextArea chatArea;
        private JComboBox<String> messageChooser;
        
        private final String[] predefinedQuestions = {
            "¿Cuál es el horario de atención?", 
            "¿Cómo puedo rastrear mi pedido?", 
            "¿Cuáles son los métodos de pago?", 
            "Tengo un problema con un producto"
        };


        public NetworkChatDialog(JFrame owner) {
            super(owner, "Soporte ManagerBot", false); 
            
            setSize(320, 450);
            setResizable(false);
            setLayout(new BorderLayout());
            
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            
            setupChatContent();
            
            connectToServer();
        }

        private void setupChatContent() {
            // HEADER
            JLabel titleLabel = new JLabel("💬 Soporte en Línea", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setBackground(primaryColor);
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setOpaque(true);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(titleLabel, BorderLayout.NORTH);

            // CHAT AREA
            chatArea = new JTextArea();
            chatArea.setEditable(false);
            chatArea.setFont(bodyFont);
            chatArea.setLineWrap(true);
            chatArea.setWrapStyleWord(true);
            
            JScrollPane scrollPane = new JScrollPane(chatArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            add(scrollPane, BorderLayout.CENTER);

            // INPUT PANEL (Selector y Botón)
            JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

            messageChooser = new JComboBox<>(predefinedQuestions);
            messageChooser.setFont(bodyFont);
            inputPanel.add(messageChooser, BorderLayout.CENTER);

            JButton sendButton = new JButton("Enviar");
            sendButton.setFont(bodyFont);
            styleButton(sendButton, secondaryColor, primaryColor, 5); 
            sendButton.setPreferredSize(new Dimension(80, 30));
            sendButton.addActionListener(e -> sendMessage());
            
            inputPanel.add(sendButton, BorderLayout.EAST);
            add(inputPanel, BorderLayout.SOUTH);
        }
        
        private void connectToServer() {
            new Thread(() -> {
                try {
                    socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    chatArea.append("Conectado al servidor de soporte.\n");

                    new Thread(new IncomingReader()).start();
                } catch (IOException e) {
                    chatArea.append("No se pudo conectar al servidor (Asegúrate de ejecutar ServidorBot.java).\n");
                }
            }).start();
        }

        private void sendMessage() {
            if (out == null) {
                chatArea.append("Error: No hay conexión con el servidor.\n");
                return;
            }
            
            String selectedQuestion = (String) messageChooser.getSelectedItem();
            if (selectedQuestion != null && !selectedQuestion.trim().isEmpty()) {
                chatArea.append("🧑 Tú: " + selectedQuestion + "\n");
                
                out.println(selectedQuestion.trim().toUpperCase()); 
                
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        }
        
        private class IncomingReader implements Runnable {
            public void run() {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        chatArea.append(message + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    
                }
            }
        }
    }
    // HEADER 
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        
        // Barra superior
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(primaryColor);
        topBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 35)); 
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel contact = new JLabel("📞 +57 123 456 7890    ✉ infomanagerit@gmail.com");
        contact.setForeground(Color.WHITE);
        contact.setFont(bodyFont);
        topBar.add(contact, BorderLayout.WEST);

        JButton pseButton = new JButton("PAGOS PSE");
        styleButton(pseButton, secondaryColor, primaryColor, 7);
        pseButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        pseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IndexApp.this.setVisible(false);
                PSEApp pseWindow = new PSEApp();
                pseWindow.setVisible(true);
            }
        });
        
        topBar.add(pseButton, BorderLayout.EAST);

        header.add(topBar, BorderLayout.NORTH);

        //  Barra de navegación principal 
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(Color.WHITE);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        
        // Logo
        JLabel logo = new JLabel("Manager Inventool");
        logo.setFont(logoFont);
        logo.setForeground(primaryColor);
        navBar.add(logo, BorderLayout.WEST);

        // Links de Navegación
        JPanel navLinks = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5)); 
        navLinks.setBackground(Color.WHITE);
        String[] links = {"Inicio", "Productos", "Servicios", "Nosotros", "Contacto"};
        for (String link : links) {
            JButton btn = createNavLinkButton(link);
            navLinks.add(btn);
        }
        navBar.add(navLinks, BorderLayout.CENTER);

        // Buscador y Carrito
        JPanel searchCart = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchCart.setBackground(Color.WHITE);
        
        JTextField searchField = new JTextField(15);
        searchField.setFont(bodyFont);
        searchField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JButton searchBtn = new JButton("🔍 Buscar");
        styleButton(searchBtn, bgColor, textColor, 5);
        
        JButton cartBtn = new JButton("🛒 Carrito (" + carrito.size() + ")");
        styleButton(cartBtn, secondaryColor, primaryColor, 5);
        cartBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Carrito: " + carrito));
        searchBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Buscando: " + searchField.getText()));

        searchCart.add(searchField);
        searchCart.add(searchBtn);
        cartBtn.setName("cartButton"); 
        searchCart.add(cartBtn);
        navBar.add(searchCart, BorderLayout.EAST);

        header.add(navBar, BorderLayout.CENTER);
        return header;
    }

    // MAIN CONTENT
    private JPanel createMainContent() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(bgColor);
        
        // HERO
        mainPanel.add(createCenteredSection(createHeroPanel(), primaryColor));
        mainPanel.add(Box.createVerticalStrut(40));

        // PRODUCTOS
        mainPanel.add(createSectionTitle("Productos Destacados 🛠️"));
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createCenteredSection(createFeaturedProductsPanel(), bgColor));
        mainPanel.add(Box.createVerticalStrut(40));

        // SERVICIOS
        mainPanel.add(createSectionTitle("Nuestros Servicios 🤝"));
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(createCenteredSection(createServicesPanel(), primaryColor));
        mainPanel.add(Box.createVerticalStrut(40));

        // FOOTER
        mainPanel.add(createCenteredSection(createFooter(), primaryColor));
        
        return mainPanel;
    }
    
    private JPanel createCenteredSection(JComponent component, Color backgroundColor) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setBackground(backgroundColor);
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        wrapper.add(Box.createHorizontalGlue());
        
        component.setPreferredSize(new Dimension(MAX_CONTENT_WIDTH, component.getPreferredSize().height));
        component.setMinimumSize(new Dimension(MAX_CONTENT_WIDTH, component.getPreferredSize().height));
        component.setMaximumSize(new Dimension(MAX_CONTENT_WIDTH, component.getMaximumSize().height));

        wrapper.add(component);
        
        wrapper.add(Box.createHorizontalGlue());
        
        return wrapper;
    }

    private JLabel createSectionTitle(String title) {
        JLabel sectionTitle = new JLabel(title, SwingConstants.CENTER);
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        sectionTitle.setForeground(primaryColor);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        return sectionTitle;
    }
    
    // FOOTER 
    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(primaryColor);
        footer.setBorder(BorderFactory.createEmptyBorder(30, 50, 10, 50));

        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 40, 0));
        infoPanel.setOpaque(false);

        infoPanel.add(createFooterColumn("Sobre Nosotros",
            "<html>Manager Inventool es tu ferretería de confianza<br>para todas tus necesidades.</html>"));
        infoPanel.add(createFooterColumn("Enlaces Rápidos",
            "<html>Inicio<br>Productos<br>Servicios<br>Contacto</html>"));
        infoPanel.add(createFooterColumn("Contacto",
            "<html>Calle Principal #123<br>Tel: +57 123 456 7890<br>Email: infomanagerit@gmail.com</html>"));

        JLabel copyright = new JLabel("© 2025 Manager Inventool - Todos los derechos reservados",
            SwingConstants.CENTER);
        copyright.setForeground(Color.WHITE);
        copyright.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        copyright.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        footer.add(infoPanel, BorderLayout.CENTER);
        footer.add(copyright, BorderLayout.SOUTH);
        return footer;
    }

    private JPanel createFooterColumn(String title, String content) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(secondaryColor);
        titleLabel.setFont(headerFont);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel contentLabel = new JLabel(content);
        contentLabel.setForeground(Color.WHITE);
        contentLabel.setFont(bodyFont);
        contentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        col.add(titleLabel);
        col.add(Box.createVerticalStrut(10));
        col.add(contentLabel);
        return col;
    }
    
    private JPanel createHeroPanel() {
    JPanel hero = new JPanel();
    hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
    hero.setBackground(primaryColor); 
    hero.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));
    hero.setAlignmentX(Component.CENTER_ALIGNMENT); 
    String colorCode = String.format("#%06x", secondaryColor.getRGB() & 0xFFFFFF); 

    JLabel heroTitle = new JLabel(
        "<html>Bienvenidos a <span style='color:" + colorCode + ";'>Manager Inventool</span></html>"
    );
    
    heroTitle.setForeground(Color.WHITE); 
    heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 55));
    heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel heroSubtitle = new JLabel("Tu socio confiable en herramientas y materiales de construcción");
    heroSubtitle.setForeground(Color.WHITE);
    heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    heroSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton heroBtn = new JButton("Ver Productos Ahora");
    styleButton(heroBtn, secondaryColor, primaryColor, 15);
    heroBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
    heroBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    heroBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    heroBtn.addActionListener(e -> {
    IndexApp.this.setVisible(false);
    InventarioApp inv = new InventarioApp();
    inv.setVisible(true);
    });



    hero.add(heroTitle);
    hero.add(Box.createVerticalStrut(15));
    hero.add(heroSubtitle);
    hero.add(Box.createVerticalStrut(30));
    hero.add(heroBtn);
    return hero;
    }

    private JPanel createFeaturedProductsPanel() {
        JPanel productosPanel = new JPanel(new GridLayout(1, 4, 30, 20));
        productosPanel.setBackground(bgColor);
        productosPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        
        String[][] productos = {
            {"Taladro Eléctrico", "$99.99"},
            {"Juego de Destornilladores", "$49.99"},
            {"Sierra Circular", "$129.99"},
            {"Caja de Herramientas", "$79.99"}
        };

        for (String[] producto : productos) {
            productosPanel.add(createProductCard(producto[0], producto[1]));
        }

        return productosPanel;
    }

    private JPanel createProductCard(String name, String price) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel imageSpace = new JLabel("🖼️", SwingConstants.CENTER);
        imageSpace.setPreferredSize(new Dimension(0, 100));
        imageSpace.setFont(new Font("Segoe UI", Font.PLAIN, 30));
        imageSpace.setOpaque(true);
        imageSpace.setBackground(new Color(230, 230, 230));
        card.add(imageSpace, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel(price);
        priceLabel.setForeground(primaryColor);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addCart = new JButton("Añadir al Carrito");
        styleButton(addCart, secondaryColor, primaryColor, 5);
        addCart.setAlignmentX(Component.CENTER_ALIGNMENT);
        addCart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addCart.addActionListener(e -> {
            carrito.add(name);
            JOptionPane.showMessageDialog(IndexApp.this, name + " ha sido añadido al carrito.");
        });

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(addCart);
        card.add(infoPanel, BorderLayout.CENTER);

        // Hover efecto
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(250, 250, 255));
                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setCursor(Cursor.getDefaultCursor());
            }
        });

        return card;
    }

    private JPanel createServicesPanel() {
        JPanel serviciosPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        serviciosPanel.setBackground(primaryColor);
        serviciosPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        serviciosPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[][] servicios = {
            {"🚚", "Entrega a Domicilio", "Entregamos tus compras directamente a tu puerta."},
            {"🧑‍🔧", "Asesoría Técnica", "Nuestros expertos te ayudan a elegir las mejores herramientas."},
            {"↩️", "Devoluciones Fáciles", "30 días para devoluciones sin complicaciones."}
        };

        for (String[] servicio : servicios) {
            serviciosPanel.add(createServiceCard(servicio[0], servicio[1], servicio[2]));
        }

        return serviciosPanel;
    }

    private JPanel createServiceCard(String icon, String title, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 20)); 
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(secondaryColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><div style='text-align: center; color: white;'>" + description + "</div></html>");
        descLabel.setFont(bodyFont);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(descLabel);
        return card;
    }
    
    // HELPERS
    private void styleButton(JButton btn, Color background, Color foreground, int padding) {
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(padding, padding * 2, padding, padding * 2));
    }

    private JButton createNavLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setFont(bodyFont);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
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
        UIManager.put("Button.arc", 999); 
        SwingUtilities.invokeLater(() -> new IndexApp().setVisible(true));
    }
}
