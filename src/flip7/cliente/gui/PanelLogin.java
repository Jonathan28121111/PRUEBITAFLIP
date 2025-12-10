package flip7.cliente.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class PanelLogin extends JPanel {
    private JTextField campoUsuario, campoHost, campoPuerto;
    private JPasswordField campoContrasena;
    private JButton botonLogin, botonRegistro;
    private EscuchaLogin escucha;
    
    private static final Color AZUL_OSCURO = new Color(66, 133, 244);
    private static final Color VERDE = new Color(74, 222, 128);
    
    public interface EscuchaLogin {
        void alLogin(String usuario, String contrasena, String host, int puerto);
        void alRegistro(String usuario, String contrasena, String host, int puerto);
    }
    
    public PanelLogin(EscuchaLogin escucha) {
        this.escucha = escucha;
        setLayout(new GridBagLayout());
        setOpaque(false);
        add(crearCajaLogin());
    }
    
    private JPanel crearCajaLogin() {
        JPanel caja = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fill(new RoundRectangle2D.Float(5, 5, getWidth() - 5, getHeight() - 5, 25, 25));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 5, getHeight() - 5, 25, 25));
                g2.setStroke(new BasicStroke(3f));
                g2.setColor(AZUL_OSCURO);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 7, getHeight() - 7, 24, 24));
            }
        };
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.setOpaque(false);
        caja.setBorder(new EmptyBorder(40, 50, 40, 50));
        caja.setPreferredSize(new Dimension(420, 520));
        
        JLabel logo = new JLabel("VOLTEAR 7");
        logo.setFont(new Font("Arial", Font.BOLD, 48));
        logo.setForeground(AZUL_OSCURO);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitulo = new JLabel("Inicia sesion o registrate para jugar");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(100, 116, 139));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel panelCampos = new JPanel();
        panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.Y_AXIS));
        panelCampos.setOpaque(false);
        panelCampos.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        campoUsuario = crearCampoTexto("");
        campoContrasena = new JPasswordField();
        campoContrasena.setFont(new Font("Arial", Font.PLAIN, 15));
        campoContrasena.setMaximumSize(new Dimension(300, 45));
        campoContrasena.setPreferredSize(new Dimension(300, 45));
        campoContrasena.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        campoHost = crearCampoTexto("localhost");
        campoPuerto = crearCampoTexto("5555");
        
        panelCampos.add(crearPanelCampo("Usuario:", campoUsuario));
        panelCampos.add(Box.createVerticalStrut(12));
        panelCampos.add(crearPanelCampo("Contrasena:", campoContrasena));
        panelCampos.add(Box.createVerticalStrut(12));
        panelCampos.add(crearPanelCampo("Servidor:", campoHost));
        panelCampos.add(Box.createVerticalStrut(12));
        panelCampos.add(crearPanelCampo("Puerto:", campoPuerto));
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setOpaque(false);
        panelBotones.setMaximumSize(new Dimension(320, 55));
        
        botonLogin = crearBoton("ENTRAR", AZUL_OSCURO);
        botonRegistro = crearBoton("REGISTRAR", VERDE);
        
        botonLogin.addActionListener(e -> hacerLogin());
        botonRegistro.addActionListener(e -> hacerRegistro());
        campoContrasena.addActionListener(e -> hacerLogin());
        
        panelBotones.add(botonLogin);
        panelBotones.add(botonRegistro);
        
        caja.add(logo);
        caja.add(Box.createVerticalStrut(5));
        caja.add(subtitulo);
        caja.add(Box.createVerticalStrut(30));
        caja.add(panelCampos);
        caja.add(Box.createVerticalStrut(25));
        caja.add(panelBotones);
        
        return caja;
    }
    
    private JTextField crearCampoTexto(String valorDefecto) {
        JTextField campo = new JTextField(valorDefecto);
        campo.setFont(new Font("Arial", Font.PLAIN, 15));
        campo.setMaximumSize(new Dimension(300, 45));
        campo.setPreferredSize(new Dimension(300, 45));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        return campo;
    }
    
    private JPanel crearPanelCampo(String etiqueta, JComponent campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(300, 70));
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        lbl.setForeground(new Color(51, 65, 85));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
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
    
    private void hacerLogin() {
        String usuario = campoUsuario.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());
        String host = campoHost.getText().trim();
        int puerto = 5555;
        try { puerto = Integer.parseInt(campoPuerto.getText().trim()); } catch (Exception e) {}
        if (usuario.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingresa tu usuario", "Error", JOptionPane.WARNING_MESSAGE); campoUsuario.requestFocus(); return; }
        if (contrasena.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingresa tu contrasena", "Error", JOptionPane.WARNING_MESSAGE); campoContrasena.requestFocus(); return; }
        botonLogin.setEnabled(false);
        botonRegistro.setEnabled(false);
        botonLogin.setText("...");
        if (escucha != null) escucha.alLogin(usuario, contrasena, host.isEmpty() ? "localhost" : host, puerto);
    }
    
    private void hacerRegistro() {
        String usuario = campoUsuario.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());
        String host = campoHost.getText().trim();
        int puerto = 5555;
        try { puerto = Integer.parseInt(campoPuerto.getText().trim()); } catch (Exception e) {}
        if (usuario.length() < 3) { JOptionPane.showMessageDialog(this, "Usuario: minimo 3 caracteres", "Error", JOptionPane.WARNING_MESSAGE); campoUsuario.requestFocus(); return; }
        if (contrasena.length() < 4) { JOptionPane.showMessageDialog(this, "Contrasena: minimo 4 caracteres", "Error", JOptionPane.WARNING_MESSAGE); campoContrasena.requestFocus(); return; }
        botonLogin.setEnabled(false);
        botonRegistro.setEnabled(false);
        botonRegistro.setText("...");
        if (escucha != null) escucha.alRegistro(usuario, contrasena, host.isEmpty() ? "localhost" : host, puerto);
    }
    
    public void limpiarCampos() {
        SwingUtilities.invokeLater(() -> {
            campoUsuario.setText("");
            campoContrasena.setText("");
            campoHost.setText("localhost");
            campoPuerto.setText("5555");
            botonLogin.setEnabled(true);
            botonRegistro.setEnabled(true);
            botonLogin.setText("ENTRAR");
            botonRegistro.setText("REGISTRAR");
            campoUsuario.requestFocusInWindow();
        });
    }
    
    public void reiniciarBotones() {
        SwingUtilities.invokeLater(() -> {
            botonLogin.setText("ENTRAR");
            botonRegistro.setText("REGISTRAR");
            botonLogin.setEnabled(true);
            botonRegistro.setEnabled(true);
        });
    }
    
    public void alLoginFallido(String razon) {
        SwingUtilities.invokeLater(() -> {
            botonLogin.setText("ENTRAR");
            botonRegistro.setText("REGISTRAR");
            botonLogin.setEnabled(true);
            botonRegistro.setEnabled(true);
            JOptionPane.showMessageDialog(this, razon, "Error de Login", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void alRegistroFallido(String razon) {
        SwingUtilities.invokeLater(() -> {
            botonLogin.setText("ENTRAR");
            botonRegistro.setText("REGISTRAR");
            botonLogin.setEnabled(true);
            botonRegistro.setEnabled(true);
            JOptionPane.showMessageDialog(this, razon, "Error de Registro", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public void alConexionFallida() {
        SwingUtilities.invokeLater(() -> {
            botonLogin.setText("ENTRAR");
            botonRegistro.setText("REGISTRAR");
            botonLogin.setEnabled(true);
            botonRegistro.setEnabled(true);
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor", "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    public String getNombreJugador() { return campoUsuario.getText().trim(); }
}
