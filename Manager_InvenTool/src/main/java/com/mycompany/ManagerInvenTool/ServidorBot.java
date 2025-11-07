package com.mycompany.managerinventool;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServidorBot {
    private static final int PORT = 12345;
    private JFrame frame;
    private JTextArea logArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServidorBot().start()); 
    }
    
    public void start() {
        frame = new JFrame("Servidor Bot - Soporte");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setMinimumSize(new Dimension(300, 200));

        logArea = new JTextArea();
        logArea.setEditable(false);
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);

        frame.setVisible(true);
        
        startServer();
    }

    private void startServer() {
        logArea.append("Servidor iniciado en puerto " + PORT + ".\n");
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                while (true) {
                    new ClientHandler(serverSocket.accept()).start();
                }
            } catch (IOException e) {
                logArea.append("Error fatal en el servidor: " + e.getMessage() + "\n");
                Logger.getLogger(ServidorBot.class.getName()).log(Level.SEVERE, null, e);
            }
        }).start();
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                logArea.append("Nuevo cliente conectado desde: " + socket.getInetAddress().getHostAddress() + "\n");

                String message;
                while ((message = in.readLine()) != null) {
                    logArea.append(">> Cliente: " + message + "\n"); 
                    String botResponse = getBotResponse(message);
                    out.println("🤖 Bot: " + botResponse);
                    logArea.append("<< Bot responde: " + botResponse + "\n");
                }
            } catch (IOException e) {
            } finally {
                try {
                    socket.close();
                    logArea.append("Cliente desconectado.\n");
                } catch (IOException e) {
                }
            }
        }
    }

    private String getBotResponse(String message) {
        switch (message) {
            case "¿CUÁL ES EL HORARIO DE ATENCIÓN?":
                return "Nuestro horario de atención es de Lunes a Sábado de 8:00 AM a 6:00 PM.";
            case "¿CÓMO PUEDO RASTREAR MI PEDIDO?":
                return "Puedes rastrear tu pedido ingresando a la sección 'Rastreo' en nuestra web y usando tu número de factura.";
            case "¿CUÁLES SON LOS MÉTODOS DE PAGO?":
                return "Aceptamos todos los métodos de pago habituales: tarjetas de crédito/débito, pagos PSE y transferencias bancarias.";
            case "TENGO UN PROBLEMA CON UN PRODUCTO":
                return "Lamentamos tu inconveniente. Un asesor humano ha sido notificado y se unirá al chat en breve para gestionar tu caso.";
            default:
                return "Lo siento, solo puedo responder a las preguntas predeterminadas. Por favor, selecciona una opción de la lista.";
        }
    }

}

