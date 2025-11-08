package com.mycompany.proyectopagina;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;

public class IndexApp extends JFrame {
    
    // // seguridad y cifrado
    
    private static final String HASHED_ADMIN_PASS = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"; // Este es un hash que guarda la contraseña, lo vi en un video :)
    private static final byte[] AES_KEY = "ManagerSecretKey".getBytes(StandardCharsets.UTF_8); //Esto es la clave de cifrado para el algoritmo AES
    private static final String ALGORITHM = "AES"; //Nombre el algoritmo que se usa
    private static SecretKeySpec secretKeySpec;

    //Esto se carga automaticamente e inicializa el objeto utiliando la clave (AES_KEY) y el algortimo
    static {
        try {
            secretKeySpec = new SecretKeySpec(AES_KEY, ALGORITHM);
        } catch (Exception e) {
            System.err.println("Error al inicializar la clave AES: " + e.getMessage());
        }
    }

    //Aqui se convierte el texto normal de la contraseña en el hash con el algoritmo SHA-256
    public static String hashSHA256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256"); // objeto para generar el hash
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8)); //convierte el texto en bytes con el UTF_8
            //De aqui
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            //Hasta aqui -> recorre cada byte del hash y se convierte en su equivalente hexadecimal
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: Algoritmo SHA-256 no disponible.", e);
        }
    }
    
    //Esto cifra el texto
    public static String encryptAES(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);// Inicializa el cifrado
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes); // bytes cifrados a Base64
    }

    public static String decryptAES(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec); 
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes, StandardCharsets.UTF_8);  
    }
    
    // COLORES
    
    private ArrayList<String> carrito = new ArrayList<>();
    private final Color primaryColor = new Color(0, 0, 139);
    private final Color secondaryColor = new Color(255, 215, 0);
    private final Color bgColor = new Color(244, 244, 244);
    private final Color textColor = new Color(51, 51, 51);
    private final Color hoverColor = new Color(230, 230, 255);
    
    //fuentes
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
        
        contentPanel.add(createHeader(), BorderLayout.NORTH);
        
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
            chatButton = new JButton("Soporte");
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
        
        // Esto para que el boton(soporte) sea estatico
        int xPos = getWidth() - BUTTON_WIDTH - MARGIN;
        int yPos = getHeight() - BUTTON_HEIGHT - MARGIN;

        chatButton.setBounds(xPos, yPos, BUTTON_WIDTH, BUTTON_HEIGHT);
        layeredPane.revalidate();
        layeredPane.repaint();
    }
    
    // CHAT
    
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
            JLabel titleLabel = new JLabel("Soporte en Línea", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setBackground(primaryColor);
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setOpaque(true);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            add(titleLabel, BorderLayout.NORTH);

            chatArea = new JTextArea();
            chatArea.setEditable(false);
            chatArea.setFont(bodyFont);
            chatArea.setLineWrap(true);
            chatArea.setWrapStyleWord(true);
            
            JScrollPane scrollPane = new JScrollPane(chatArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            add(scrollPane, BorderLayout.CENTER);

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
                    chatArea.append("Conectado al servidor\n");

                    new Thread(new IncomingReader()).start();
                } catch (IOException e) {
                    chatArea.append("No se pudo conectar al servidor (ejecutar primero el ServidorBot.java)\n");
                }
            }).start();
        }

        private void sendMessage() {
            if (out == null) {
                chatArea.append("No hay conexión con el servidor\n");
                return;
            }
            
            String selectedQuestion = (String) messageChooser.getSelectedItem();
            if (selectedQuestion != null && !selectedQuestion.trim().isEmpty()) {
                String fullMessage = "🧑 Tú: " + selectedQuestion.trim();
                chatArea.append(fullMessage + "\n");
                
                try {
                    String encryptedMessage = IndexApp.encryptAES(selectedQuestion.trim().toUpperCase());
                    out.println(encryptedMessage); 
                } catch (Exception e) {
                    chatArea.append("ERROR DE CIFRADO AL ENVIAR\n");
                    e.printStackTrace();
                }
                
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        }
        
        private class IncomingReader implements Runnable {
            public void run() {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        try {
                            String decryptedMessage = IndexApp.decryptAES(message);
                            chatArea.append(decryptedMessage + "\n");
                        } catch (Exception e) {
                            chatArea.append("[ERROR DE DESCIFRADO] Mensaje recibido en formato incorrecto: " + message + "\n");
                        }
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    chatArea.append("Conexión del chat cerrada\n");
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

        JLabel contact = new JLabel("📞 +57 123 456 7890     ✉ infomanagerit@gmail.com");
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
                PSEApp pseWindow = new PSEApp(IndexApp.this);
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
        
        JButton loginButton = new JButton("🔑 Iniciar Sesión");
        styleButton(loginButton, secondaryColor, primaryColor, 5); 
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        loginButton.addActionListener(e -> {
            LoginDialog login = new LoginDialog(IndexApp.this, primaryColor, secondaryColor, bodyFont);
            login.setVisible(true);
        });
        
        JButton cartBtn = new JButton("🛒 Carrito (" + carrito.size() + ")");
        styleButton(cartBtn, secondaryColor, primaryColor, 5);
        cartBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Carrito: " + carrito));
        searchBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Buscando: " + searchField.getText()));

        searchCart.add(searchField);
        searchCart.add(searchBtn);
        searchCart.add(loginButton); 
        
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
    heroBtn.addActionListener(e -> JOptionPane.showMessageDialog(IndexApp.this, "¡Redirigiendo a productos!"));

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
    
    // HELPERS -> pa no repetir codigo
    
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
    
    // LOGIN (CAPTCHA & HASH) 

    private class LoginDialog extends JDialog {
        private String captchaChallenge;
        private JLabel captchaLabel;
        private JTextField captchaField;

        public LoginDialog(JFrame owner, Color primaryColor, Color secondaryColor, Font bodyFont) {
            super(owner, "Iniciar Sesión", true);
            setSize(380, 350); 
            setLocationRelativeTo(owner);
            setResizable(false);
            setLayout(new GridBagLayout());
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 15, 8, 15);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            JLabel title = new JLabel("🔑 ACCESO AL SISTEMA", SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));
            title.setForeground(primaryColor);
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            add(title, gbc);
            
            gbc.gridwidth = 1; gbc.gridy = 1; gbc.weightx = 0.3;
            add(new JLabel("Usuario:"), gbc);
            JTextField userField = new JTextField(15);
            userField.setFont(bodyFont);
            gbc.gridx = 1; gbc.weightx = 0.7;
            add(userField, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
            add(new JLabel("Clave:"), gbc);
            JPasswordField passField = new JPasswordField(15);
            passField.setFont(bodyFont);
            gbc.gridx = 1; gbc.weightx = 0.7;
            add(passField, gbc);
            
            // CAPTCHA
            generateCaptcha();

            captchaLabel = new JLabel(captchaChallenge, SwingConstants.CENTER);
            captchaLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 26)); 
            captchaLabel.setForeground(new Color(200, 50, 50));
            captchaLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.insets = new Insets(10, 15, 5, 15);
            add(captchaLabel, gbc);

            // CAPTCHA INPUT
            JPanel captchaInputPanel = new JPanel(new BorderLayout(5, 0));
            captchaField = new JTextField(10);
            captchaField.setFont(bodyFont);
            
            JButton refreshBtn = new JButton("↻");
            refreshBtn.setToolTipText("Generar nuevo CAPTCHA");
            refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            styleButton(refreshBtn, bgColor, textColor, 2);
            refreshBtn.setPreferredSize(new Dimension(40, 30));
            refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            refreshBtn.addActionListener(e -> refreshCaptcha());

            captchaInputPanel.add(captchaField, BorderLayout.CENTER);
            captchaInputPanel.add(refreshBtn, BorderLayout.EAST);

            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.insets = new Insets(5, 15, 10, 15);
            add(captchaInputPanel, gbc);
            
            // LOGIN BUTTON
            JButton btnLogin = new JButton("Entrar");
            btnLogin.setBackground(secondaryColor);
            btnLogin.setForeground(primaryColor);
            btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnLogin.setFocusPainted(false);
            
            btnLogin.addActionListener(e -> {
                String user = userField.getText();
                String pass = new String(passField.getPassword());
                String captchaInput = captchaField.getText();
                
                if (!captchaInput.equalsIgnoreCase(this.captchaChallenge)) {
                    JOptionPane.showMessageDialog(this, "❌ El código CAPTCHA es incorrecto.", "Error de Verificación", JOptionPane.ERROR_MESSAGE);
                    refreshCaptcha();
                    return;
                }
                
                String inputPassHash = IndexApp.hashSHA256(pass);

                if (user.equals("admin") && inputPassHash.equals(HASHED_ADMIN_PASS)) {
                    JOptionPane.showMessageDialog(this, "✅ Sesión iniciada como Admin. ¡Bienvenido!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Credenciales incorrectas. (Error de usuario o hash de clave).", "Error", JOptionPane.ERROR_MESSAGE);
                    refreshCaptcha();
                }
            });
            
            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.insets = new Insets(15, 15, 15, 15);
            add(btnLogin, gbc);
        }
        
        // METODOS CAPTCHA
        private void generateCaptcha() {
            String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"; 
            StringBuilder captcha = new StringBuilder();
            Random rnd = new Random();
            for (int i = 0; i < 6; i++) {
                captcha.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            this.captchaChallenge = captcha.toString();
        }
        
        private void refreshCaptcha() {
            generateCaptcha();
            if (captchaLabel != null) {
                captchaLabel.setText(captchaChallenge);
                captchaField.setText("");
            }
        }
    }
    
    //MAIN

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
