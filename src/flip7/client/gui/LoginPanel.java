package flip7.client.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {
    private JTextField userField, hostField, portField;
    private JPasswordField passField;
    private JButton loginBtn, registerBtn;
    private LoginListener listener;
    
    private static final Color BLUE_DARK = new Color(66, 133, 244);
    private static final Color BLUE_LIGHT = new Color(135, 206, 250);
    private static final Color GREEN = new Color(74, 222, 128);
    
    public interface LoginListener {
        void onLogin(String username, String password, String host, int port);
        void onRegister(String username, String password, String host, int port);
    }
    
    public LoginPanel(LoginListener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setOpaque(false);
        
        JPanel loginBox = createLoginBox();
        add(loginBox);
    }
    
    private JPanel createLoginBox() {
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(5, 5, getWidth() - 5, getHeight() - 5, 25, 25));
                
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 25, 25));
                
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(BLUE_DARK);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 7, getHeight() - 7, 24, 24));
            }
        };
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(40, 50, 40, 50));
        box.setPreferredSize(new Dimension(420, 520));
        
        // Logo
        JLabel logo = new JLabel("FLIP 7");
        logo.setFont(new Font("Arial", Font.BOLD, 48));
        logo.setForeground(BLUE_DARK);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("Inicia sesion o registrate para jugar");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Campos
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);
        fieldsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        userField = createTextField("Usuario");
        passField = new JPasswordField();
        passField.setFont(new Font("Arial", Font.PLAIN, 15));
        passField.setMaximumSize(new Dimension(300, 45));
        passField.setPreferredSize(new Dimension(300, 45));
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        hostField = createTextField("localhost");
        portField = createTextField("5555");
        
        fieldsPanel.add(createFieldPanel("Usuario:", userField));
        fieldsPanel.add(Box.createVerticalStrut(12));
        fieldsPanel.add(createFieldPanel("Password:", passField));
        fieldsPanel.add(Box.createVerticalStrut(12));
        fieldsPanel.add(createFieldPanel("Servidor:", hostField));
        fieldsPanel.add(Box.createVerticalStrut(12));
        fieldsPanel.add(createFieldPanel("Puerto:", portField));
        
        // Botones
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setMaximumSize(new Dimension(320, 55));
        
        loginBtn = createButton("ENTRAR", BLUE_DARK);
        registerBtn = createButton("REGISTRAR", GREEN);
        
        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> doRegister());
        
        passField.addActionListener(e -> doLogin());
        
        buttonsPanel.add(loginBtn);
        buttonsPanel.add(registerBtn);
        
        box.add(logo);
        box.add(Box.createVerticalStrut(5));
        box.add(subtitle);
        box.add(Box.createVerticalStrut(30));
        box.add(fieldsPanel);
        box.add(Box.createVerticalStrut(25));
        box.add(buttonsPanel);
        
        return box;
    }
    
    private JTextField createTextField(String defaultValue) {
        JTextField field = new JTextField(defaultValue);
        field.setFont(new Font("Arial", Font.PLAIN, 15));
        field.setMaximumSize(new Dimension(300, 45));
        field.setPreferredSize(new Dimension(300, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        return field;
    }
    
    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(300, 70));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));
        
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(140, 45));
        btn.setMinimumSize(new Dimension(140, 45));
        btn.setMaximumSize(new Dimension(140, 45));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }
    
    private void doLogin() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        String host = hostField.getText().trim();
        int port = 5555;
        try { port = Integer.parseInt(portField.getText().trim()); } catch (Exception e) {}
        
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu usuario", "Error", JOptionPane.WARNING_MESSAGE);
            userField.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu password", "Error", JOptionPane.WARNING_MESSAGE);
            passField.requestFocus();
            return;
        }
        
        loginBtn.setEnabled(false);
        registerBtn.setEnabled(false);
        loginBtn.setText("...");
        
        if (listener != null) {
            listener.onLogin(user, pass, host.isEmpty() ? "localhost" : host, port);
        }
    }
    
    private void doRegister() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        String host = hostField.getText().trim();
        int port = 5555;
        try { port = Integer.parseInt(portField.getText().trim()); } catch (Exception e) {}
        
        if (user.length() < 3) {
            JOptionPane.showMessageDialog(this, "Usuario: minimo 3 caracteres", "Error", JOptionPane.WARNING_MESSAGE);
            userField.requestFocus();
            return;
        }
        if (pass.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password: minimo 4 caracteres", "Error", JOptionPane.WARNING_MESSAGE);
            passField.requestFocus();
            return;
        }
        
        loginBtn.setEnabled(false);
        registerBtn.setEnabled(false);
        registerBtn.setText("...");
        
        if (listener != null) {
            listener.onRegister(user, pass, host.isEmpty() ? "localhost" : host, port);
        }
    }
    
    public void onLoginFailed(String reason) {
        SwingUtilities.invokeLater(() -> {
            loginBtn.setText("ENTRAR");
            registerBtn.setText("REGISTRAR");
            loginBtn.setEnabled(true);
            registerBtn.setEnabled(true);
            JOptionPane.showMessageDialog(this, reason, "Error de Login", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void resetButtons() {
        SwingUtilities.invokeLater(() -> {
            loginBtn.setText("ENTRAR");
            registerBtn.setText("REGISTRAR");
            loginBtn.setEnabled(true);
            registerBtn.setEnabled(true);
        });
    }
    
    public void onRegisterFailed(String reason) {
        SwingUtilities.invokeLater(() -> {
            loginBtn.setText("ENTRAR");
            registerBtn.setText("REGISTRAR");
            loginBtn.setEnabled(true);
            registerBtn.setEnabled(true);
            JOptionPane.showMessageDialog(this, reason, "Error de Registro", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void onConnectionFailed() {
        SwingUtilities.invokeLater(() -> {
            loginBtn.setText("ENTRAR");
            registerBtn.setText("REGISTRAR");
            loginBtn.setEnabled(true);
            registerBtn.setEnabled(true);
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor", "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public String getPlayerName() {
        return userField.getText().trim();
    }
}
