package flip7.cliente.gui;

import flip7.comun.Jugador;
import flip7.comun.Usuario;
import flip7.comun.Carta;
import flip7.comun.SalaJuego;
import flip7.comun.EstadoJuego;
import flip7.cliente.ClienteJuego;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

public class VentanaJuego extends JFrame implements ClienteJuego.EscuchaClienteJuego {
    private ClienteJuego cliente = new ClienteJuego();
    private int miIdJugador = -1;
    private boolean esMiTurno, juegoIniciado, esEspectador, esHost = false;
    private Map<Integer, PanelJugador> panelesJugadores = new HashMap<>();
    private CardLayout cardLayout;
    private JPanel contenedorPrincipal, panelJuego, panelJugadores;
    private PanelLogin panelLogin;
    private PanelLobby panelLobby;
    private PanelSalaEspera panelSalaEspera;
    private PanelInfoJuego panelInfo;
    private PanelRankings panelRankings;
    private JButton botonPedir, botonPlantarse, botonListo;
    private JLabel indicadorTurno;
    private JTextArea areaChat;
    private JTextField entradaChat;
    private static final Color AZUL_CLARO = new Color(135, 206, 250);
    private static final Color AZUL_OSCURO = new Color(66, 133, 244);
    private static final Color VERDE = new Color(74, 222, 128);
    private static final Color NARANJA = new Color(251, 146, 60);
    private static final Color ROJO = new Color(248, 113, 113);
    
    public VentanaJuego() {
        super("Voltear 7 - Juego de Cartas");
        cliente.agregarEscucha(this);
        inicializarUI();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1250, 850);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent e) { manejarCierreVentana(); } });
    }
    
    private void manejarCierreVentana() {
        if (juegoIniciado && cliente.estaEnSala()) {
            String msg = esHost ? "Eres el HOST. Si sales, la partida TERMINARA para todos." : "Si sales, PERDERAS los puntos.";
            if (JOptionPane.showConfirmDialog(this, msg + "\n\nSalir?", "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                if (cliente.estaConectado()) { cliente.salirSala(); cliente.desconectar(); }
                dispose(); System.exit(0);
            }
        } else if (cliente.estaEnSala()) {
            String msg = esHost ? "Eres el HOST. La sala se CERRARA." : "Salir de la sala?";
            if (JOptionPane.showConfirmDialog(this, msg, "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                if (cliente.estaConectado()) { cliente.salirSala(); cliente.desconectar(); }
                dispose(); System.exit(0);
            }
        } else {
            if (cliente.estaConectado()) cliente.desconectar();
            dispose(); System.exit(0);
        }
    }
    
    private void inicializarUI() {
        cardLayout = new CardLayout();
        contenedorPrincipal = new JPanel(cardLayout) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, AZUL_CLARO, 0, getHeight(), new Color(248, 250, 252)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panelLogin = new PanelLogin(new PanelLogin.EscuchaLogin() {
            public void alLogin(String u, String c, String h, int p) { new Thread(() -> { if (!cliente.estaConectado() && !cliente.conectar(h, p, u)) { panelLogin.alConexionFallida(); return; } cliente.login(u, c); }).start(); }
            public void alRegistro(String u, String c, String h, int p) { new Thread(() -> { if (!cliente.estaConectado() && !cliente.conectar(h, p, u)) { panelLogin.alConexionFallida(); return; } cliente.registrar(u, c); }).start(); }
        });
        panelLobby = new PanelLobby(new PanelLobby.EscuchaLobby() {
            public void alSalir() { if (cliente.estaConectado()) cliente.desconectar(); esHost = false; panelLogin.limpiarCampos(); mostrarPanel("login"); }
            public void alCrearSala(String n, String j, int m) { cliente.crearSala(n, m); }
            public void alUnirseSala(String id, String j, boolean esp) { esEspectador = esp; if (esp) cliente.unirseSalaComoEspectador(id); else cliente.unirseSala(id); }
            public void alActualizar() { cliente.solicitarSalas(); }
            public void alVerRankings() { cliente.solicitarRankings(); mostrarPanel("rankings"); }
        });
        panelRankings = new PanelRankings(new PanelRankings.EscuchaRankings() {
            public void alVolver() { mostrarPanel("lobby"); }
            public void alActualizar() { cliente.solicitarRankings(); }
        });
        panelSalaEspera = new PanelSalaEspera(new PanelSalaEspera.EscuchaSalaEspera() {
            public void alListo() { cliente.listo(); }
            public void alSalirSala() {
                if (esHost) {
                    if (JOptionPane.showConfirmDialog(VentanaJuego.this, "Eres el HOST. La sala se CERRARA.", "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        cliente.salirSala(); esHost = false; mostrarPanel("lobby");
                    }
                } else { cliente.salirSala(); mostrarPanel("lobby"); }
            }
            public void alUnirseComoJugador() { String id = cliente.getIdSalaActual(); if (id != null) { cliente.salirSala(); esEspectador = false; esHost = false; cliente.unirseSala(id); } }
        });
        panelJuego = crearPanelJuego();
        contenedorPrincipal.add(panelLogin, "login");
        contenedorPrincipal.add(panelLobby, "lobby");
        contenedorPrincipal.add(panelRankings, "rankings");
        contenedorPrincipal.add(panelSalaEspera, "espera");
        contenedorPrincipal.add(panelJuego, "juego");
        setContentPane(contenedorPrincipal);
        mostrarPanel("login");
    }
    
    private void mostrarPanel(String nombre) { cardLayout.show(contenedorPrincipal, nombre); }
    
    private JPanel crearPanelJuego() {
        JPanel principal = new JPanel(new BorderLayout(0, 0));
        principal.setOpaque(false);
        JPanel cabecera = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g; g2.setColor(Color.WHITE); g2.fillRect(0, 0, getWidth(), getHeight()); g2.setColor(new Color(100, 180, 246)); g2.fillRect(0, getHeight() - 4, getWidth(), 4); }
        };
        cabecera.setBorder(new EmptyBorder(15, 25, 15, 25));
        JLabel logo = new JLabel("VOLTEAR 7");
        logo.setFont(new Font("Arial", Font.BOLD, 32));
        logo.setForeground(AZUL_OSCURO);
        indicadorTurno = new JLabel("");
        indicadorTurno.setFont(new Font("Arial", Font.BOLD, 16));
        indicadorTurno.setForeground(AZUL_OSCURO);
        cabecera.add(logo, BorderLayout.WEST);
        cabecera.add(indicadorTurno, BorderLayout.EAST);
        JPanel areaCentral = new JPanel(new BorderLayout(15, 0));
        areaCentral.setOpaque(false);
        areaCentral.setBorder(new EmptyBorder(15, 20, 15, 20));
        JPanel panelMesa = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(100, 150, 200, 30));
                g2.fill(new RoundRectangle2D.Float(4, 4, w - 4, h - 4, 28, 28));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 5, h - 5, 28, 28));
                g2.setStroke(new BasicStroke(4f));
                g2.setColor(new Color(100, 180, 246));
                g2.draw(new RoundRectangle2D.Float(2, 2, w - 9, h - 9, 26, 26));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.setColor(new Color(100, 180, 246));
                g2.fillOval(w/2 - 80, h/2 - 80, 160, 160);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                g2.setFont(new Font("Arial", Font.BOLD, 42));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(AZUL_OSCURO);
                g2.drawString("VOLTEAR", w/2 - fm.stringWidth("VOLTEAR")/2, h/2 - 8);
                g2.setFont(new Font("Arial", Font.BOLD, 56));
                fm = g2.getFontMetrics();
                g2.drawString("7", w/2 - fm.stringWidth("7")/2, h/2 + 45);
            }
        };
        panelMesa.setOpaque(false);
        panelMesa.setBorder(new EmptyBorder(25, 25, 25, 25));
        panelJugadores = new JPanel(new GridLayout(2, 3, 15, 15));
        panelJugadores.setOpaque(false);
        panelMesa.add(panelJugadores, BorderLayout.CENTER);
        JPanel panelDerecho = new JPanel(new BorderLayout(0, 12));
        panelDerecho.setOpaque(false);
        panelDerecho.setPreferredSize(new Dimension(250, 0));
        panelInfo = new PanelInfoJuego();
        JPanel panelChat = new JPanel(new BorderLayout(0, 8)) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 150, 200, 30));
                g2.fill(new RoundRectangle2D.Float(3, 3, getWidth(), getHeight(), 16, 16));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 3, getHeight() - 3, 16, 16));
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(100, 180, 246));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-5, getHeight()-5, 15, 15));
            }
        };
        panelChat.setOpaque(false);
        panelChat.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel tituloChat = new JLabel("CHAT");
        tituloChat.setFont(new Font("Arial", Font.BOLD, 12));
        tituloChat.setForeground(AZUL_OSCURO);
        areaChat = new JTextArea();
        areaChat.setEditable(false);
        areaChat.setOpaque(false);
        areaChat.setForeground(new Color(51, 65, 85));
        areaChat.setFont(new Font("Arial", Font.PLAIN, 12));
        areaChat.setLineWrap(true);
        JScrollPane scrollChat = new JScrollPane(areaChat);
        scrollChat.setOpaque(false);
        scrollChat.getViewport().setOpaque(false);
        scrollChat.setBorder(null);
        entradaChat = new JTextField();
        entradaChat.setBackground(new Color(248, 250, 252));
        entradaChat.setForeground(new Color(30, 41, 59));
        entradaChat.setCaretColor(AZUL_OSCURO);
        entradaChat.setFont(new Font("Arial", Font.PLAIN, 12));
        entradaChat.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 2), new EmptyBorder(10, 12, 10, 12)));
        entradaChat.addActionListener(e -> { String t = entradaChat.getText().trim(); if (!t.isEmpty() && cliente.estaConectado()) { cliente.enviarChat(t); entradaChat.setText(""); } });
        panelChat.add(tituloChat, BorderLayout.NORTH);
        panelChat.add(scrollChat, BorderLayout.CENTER);
        panelChat.add(entradaChat, BorderLayout.SOUTH);
        panelDerecho.add(panelInfo, BorderLayout.NORTH);
        panelDerecho.add(panelChat, BorderLayout.CENTER);
        areaCentral.add(panelMesa, BorderLayout.CENTER);
        areaCentral.add(panelDerecho, BorderLayout.EAST);
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15)) {
            protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g; g2.setColor(Color.WHITE); g2.fillRect(0, 0, getWidth(), getHeight()); g2.setColor(new Color(100, 180, 246)); g2.fillRect(0, 0, getWidth(), 4); }
        };
        controles.setBorder(new EmptyBorder(12, 0, 18, 0));
        botonListo = crearBoton("LISTO", VERDE);
        botonPedir = crearBoton("PEDIR CARTA", AZUL_OSCURO);
        botonPlantarse = crearBoton("PLANTARSE", NARANJA);
        JButton botonSalir = crearBoton("SALIR", ROJO);
        botonListo.addActionListener(e -> { cliente.listo(); botonListo.setEnabled(false); botonListo.setText("ESPERANDO..."); });
        botonPedir.addActionListener(e -> { if (esMiTurno) cliente.pedir(); });
        botonPlantarse.addActionListener(e -> { if (esMiTurno) cliente.plantarse(); });
        botonSalir.addActionListener(e -> {
            if (esHost && juegoIniciado) {
                if (JOptionPane.showConfirmDialog(this, "Eres el HOST. El juego TERMINARA.", "Salir", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    cliente.salirSala(); juegoIniciado = false; esHost = false; limpiarEstadoJuego(); mostrarPanel("lobby");
                }
            } else { cliente.salirSala(); juegoIniciado = false; limpiarEstadoJuego(); mostrarPanel("lobby"); }
        });
        botonListo.setEnabled(false);
        botonPedir.setEnabled(false);
        botonPlantarse.setEnabled(false);
        controles.add(botonSalir);
        controles.add(botonListo);
        controles.add(botonPedir);
        controles.add(botonPlantarse);
        principal.add(cabecera, BorderLayout.NORTH);
        principal.add(areaCentral, BorderLayout.CENTER);
        principal.add(controles, BorderLayout.SOUTH);
        return principal;
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto) {
            boolean hover = false;
            { addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) { hover = true; repaint(); } public void mouseExited(MouseEvent e) { hover = false; repaint(); } }); }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color c = isEnabled() ? (hover ? masBrillante(color, 1.1f) : color) : new Color(203, 213, 225);
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fill(new RoundRectangle2D.Float(3, 3, w - 3, h - 3, 14, 14));
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, w - 3, h - 3, 14, 14));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(isEnabled() ? Color.WHITE : new Color(148, 163, 184));
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            Color masBrillante(Color c, float f) { return new Color(Math.min(255,(int)(c.getRed()*f)), Math.min(255,(int)(c.getGreen()*f)), Math.min(255,(int)(c.getBlue()*f))); }
        };
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(160, 50));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    private void actualizarControles() { SwingUtilities.invokeLater(() -> { botonPedir.setEnabled(juegoIniciado && esMiTurno && !esEspectador); botonPlantarse.setEnabled(juegoIniciado && esMiTurno && !esEspectador); }); }
    
    private void actualizarPanelesJugadores(EstadoJuego e) {
        if (e == null) return;
        SwingUtilities.invokeLater(() -> {
            java.util.List<Jugador> jugadores = e.getJugadores();
            java.util.List<Jugador> conectados = new java.util.ArrayList<>();
            for (Jugador j : jugadores) if (j.estaConectado()) conectados.add(j);
            if (panelesJugadores.size() != conectados.size()) {
                panelJugadores.removeAll();
                panelesJugadores.clear();
                for (Jugador j : conectados) {
                    PanelJugador pj = new PanelJugador(j.getId() == miIdJugador);
                    pj.setJugador(j);
                    panelesJugadores.put(j.getId(), pj);
                    panelJugadores.add(pj);
                }
                for (int i = conectados.size(); i < 6; i++) { JPanel vacio = new JPanel(); vacio.setOpaque(false); panelJugadores.add(vacio); }
                panelJugadores.revalidate();
            }
            Jugador actual = e.getJugadorActual();
            for (Jugador j : conectados) {
                PanelJugador pj = panelesJugadores.get(j.getId());
                if (pj != null) { pj.setJugador(j); pj.setTurnoActual(actual != null && actual.getId() == j.getId()); }
            }
            panelInfo.actualizarEstado(e);
            panelJugadores.repaint();
        });
    }
    
    private void limpiarEstadoJuego() {
        SwingUtilities.invokeLater(() -> {
            panelJugadores.removeAll();
            panelesJugadores.clear();
            for (int i = 0; i < 6; i++) { JPanel vacio = new JPanel(); vacio.setOpaque(false); panelJugadores.add(vacio); }
            panelJugadores.revalidate();
            panelJugadores.repaint();
            areaChat.setText("");
            indicadorTurno.setText("");
        });
    }
    
    public void alConectar(int id, String nombre) {}
    public void alLoginExitoso(Usuario usuario) { SwingUtilities.invokeLater(() -> { miIdJugador = usuario.getId(); cliente.setNombreJugador(usuario.getNombreUsuario()); panelLobby.setNombreJugador(usuario.getNombreUsuario()); mostrarPanel("lobby"); panelInfo.registrar("Conectado"); panelInfo.registrar("Partidas: " + usuario.getPartidasJugadas() + " | Ganadas: " + usuario.getPartidasGanadas()); }); }
    public void alLoginFallido(String razon) { panelLogin.alLoginFallido(razon); }
    public void alRegistroExitoso(Usuario usuario) { SwingUtilities.invokeLater(() -> { panelLogin.reiniciarBotones(); JOptionPane.showMessageDialog(this, "Usuario registrado correctamente!", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE); miIdJugador = usuario.getId(); cliente.setNombreJugador(usuario.getNombreUsuario()); panelLobby.setNombreJugador(usuario.getNombreUsuario()); mostrarPanel("lobby"); panelInfo.registrar("Registro exitoso!"); }); }
    public void alRegistroFallido(String razon) { panelLogin.alRegistroFallido(razon); }
    public void alDesconectar() { SwingUtilities.invokeLater(() -> { juegoIniciado = false; esMiTurno = false; esHost = false; mostrarPanel("login"); JOptionPane.showMessageDialog(this, "Desconectado del servidor", "Aviso", JOptionPane.WARNING_MESSAGE); }); }
    public void alListaSalas(java.util.List<SalaJuego> salas) { panelLobby.actualizarListaSalas(salas); }
    public void alCrearSala(SalaJuego sala, int idJugador) { SwingUtilities.invokeLater(() -> { miIdJugador = idJugador; esHost = true; panelSalaEspera.setEspectador(false); panelSalaEspera.setEsHost(true); panelSalaEspera.actualizarSala(sala); mostrarPanel("espera"); }); }
    public void alUnirseSala(SalaJuego sala, int idJugador) { SwingUtilities.invokeLater(() -> { miIdJugador = idJugador; esEspectador = (idJugador < 0); esHost = false; panelSalaEspera.setEspectador(esEspectador); panelSalaEspera.setEsHost(false); panelSalaEspera.actualizarSala(sala); mostrarPanel("espera"); }); }
    public void alActualizarSala(SalaJuego sala) { panelSalaEspera.actualizarSala(sala); }
    public void alErrorSala(String error) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE)); }
    public void alUnirseJugador(int id, String nombre) { panelInfo.registrar("+ " + nombre + " se unio"); }
    public void alSalirJugador(int id, String nombre) { SwingUtilities.invokeLater(() -> { panelInfo.registrar("- " + nombre + " salio"); PanelJugador panel = panelesJugadores.remove(id); if (panel != null) { panelJugadores.remove(panel); JPanel vacio = new JPanel(); vacio.setOpaque(false); panelJugadores.add(vacio); panelJugadores.revalidate(); panelJugadores.repaint(); } }); }
    public void alIniciarJuego(java.util.List<Jugador> jugadores) { SwingUtilities.invokeLater(() -> { juegoIniciado = true; botonListo.setEnabled(false); botonListo.setText("EN JUEGO"); mostrarPanel("juego"); panelInfo.registrar("\n*** JUEGO INICIADO ***\n"); }); }
    public void alIniciarRonda(int ronda) { SwingUtilities.invokeLater(() -> { panelInfo.registrar("\n=== RONDA " + ronda + " ==="); panelInfo.registrar("Comienza la ronda!\n"); }); }
    public void alTuTurno(int id) { SwingUtilities.invokeLater(() -> { esMiTurno = (id == miIdJugador); actualizarControles(); if (esMiTurno && !esEspectador) { indicadorTurno.setText("TU TURNO!"); Toolkit.getDefaultToolkit().beep(); } else { EstadoJuego e = cliente.getEstadoJuegoActual(); if (e != null) { Jugador j = e.getJugadorPorId(id); if (j != null) indicadorTurno.setText("Turno: " + j.getNombre()); } } }); }
    public void alRepartirCarta(int id, Carta c) { EstadoJuego e = cliente.getEstadoJuegoActual(); if (e != null) { Jugador j = e.getJugadorPorId(id); if (j != null) panelInfo.registrar(j.getNombre() + " <- " + c); } }
    public void alEliminarJugador(int id, Carta c) { EstadoJuego e = cliente.getEstadoJuegoActual(); if (e != null) { Jugador j = e.getJugadorPorId(id); if (j != null) panelInfo.registrar("X " + j.getNombre() + " ELIMINADO con " + c); } if (id == miIdJugador) { esMiTurno = false; actualizarControles(); } }
    public void alPlantarseJugador(int id) { EstadoJuego e = cliente.getEstadoJuegoActual(); if (e != null) { Jugador j = e.getJugadorPorId(id); if (j != null) panelInfo.registrar(j.getNombre() + " se planto"); } if (id == miIdJugador) { esMiTurno = false; actualizarControles(); } }
    public void alCongelarJugador(int id) { EstadoJuego e = cliente.getEstadoJuegoActual(); if (e != null) { Jugador j = e.getJugadorPorId(id); if (j != null) panelInfo.registrar(j.getNombre() + " CONGELADO"); } }
    public void alRobarCartaAccion(int id, Carta c) { EstadoJuego e = cliente.getEstadoJuegoActual(); if (e != null) { Jugador j = e.getJugadorPorId(id); if (j != null) panelInfo.registrar("* " + j.getNombre() + " -> " + c); } }
    public void alElegirObjetivoAccion(Carta carta, java.util.List<Jugador> activos) {
        if (esEspectador) return;
        SwingUtilities.invokeLater(() -> {
            botonPedir.setEnabled(false);
            botonPlantarse.setEnabled(false);
            String[] opciones = new String[activos.size()];
            for (int i = 0; i < activos.size(); i++) opciones[i] = activos.get(i).getNombre();
            javax.swing.Timer timer = new javax.swing.Timer(2000, ev -> {
                String sel = null;
                while (sel == null) {
                    sel = (String) JOptionPane.showInputDialog(VentanaJuego.this, "A quien asignas " + carta + "?\n(Debes elegir obligatoriamente)", "Carta de Accion - OBLIGATORIO", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
                    if (sel == null) JOptionPane.showMessageDialog(VentanaJuego.this, "Debes elegir un objetivo para continuar", "Seleccion Obligatoria", JOptionPane.WARNING_MESSAGE);
                }
                for (Jugador j : activos) if (j.getNombre().equals(sel)) { cliente.asignarCartaAccion(j.getId(), carta); break; }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    public void alFinRonda(java.util.List<Jugador> jugadores, int ronda) {
        SwingUtilities.invokeLater(() -> {
            esMiTurno = false;
            indicadorTurno.setText("");
            actualizarControles();
            panelInfo.registrar("\n=== FIN RONDA " + ronda + " ===");
            for (Jugador j : jugadores) panelInfo.registrar("  " + j.getNombre() + ": +" + j.getPuntajeRonda() + " -> " + j.getPuntajeTotal());
            panelInfo.mostrarPuntuaciones(jugadores);
            boolean finJuego = false;
            for (Jugador j : jugadores) if (j.getPuntajeTotal() >= 200) { finJuego = true; break; }
            if (!finJuego) {
                final int[] cuenta = {3};
                JOptionPane pane = new JOptionPane("Ronda " + ronda + " finalizada\n\nNueva ronda en: " + cuenta[0] + " segundos", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
                JDialog dialogo = pane.createDialog(this, "Fin de Ronda");
                dialogo.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialogo.setModal(false);
                javax.swing.Timer timer = new javax.swing.Timer(1000, ev -> {
                    cuenta[0]--;
                    if (cuenta[0] > 0) pane.setMessage("Ronda " + ronda + " finalizada\n\nNueva ronda en: " + cuenta[0] + " segundos");
                    else { pane.setMessage("Comenzando!"); javax.swing.Timer cerrar = new javax.swing.Timer(300, e2 -> dialogo.dispose()); cerrar.setRepeats(false); cerrar.start(); ((javax.swing.Timer)ev.getSource()).stop(); }
                });
                timer.setRepeats(true);
                dialogo.setVisible(true);
                timer.start();
                Toolkit.getDefaultToolkit().beep();
            }
        });
    }
    public void alFinJuego(java.util.List<Jugador> jugadores, int idGanador) {
        SwingUtilities.invokeLater(() -> {
            juegoIniciado = false;
            esMiTurno = false;
            actualizarControles();
            Jugador g = null;
            for (Jugador j : jugadores) if (j.getId() == idGanador) { g = j; break; }
            String msg = g != null ? g.getNombre() + " GANA con " + g.getPuntajeTotal() + " pts!" : "Fin";
            panelInfo.registrar("\n*** " + msg + " ***\n");
            if (!esEspectador) {
                int opcion = -1;
                while (opcion != 0 && opcion != 1) {
                    opcion = JOptionPane.showOptionDialog(this, msg + "\n\nQuieres jugar otra partida?\n(Debes elegir una opcion)", "Fin del Juego!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"REVANCHA", "SALIR"}, "REVANCHA");
                    if (opcion == -1 || opcion == JOptionPane.CLOSED_OPTION) JOptionPane.showMessageDialog(this, "Debes elegir REVANCHA o SALIR para continuar", "Seleccion Obligatoria", JOptionPane.WARNING_MESSAGE);
                }
                if (opcion == 0) { panelSalaEspera.reiniciar(); limpiarEstadoJuego(); mostrarPanel("espera"); }
                else { cliente.salirSala(); limpiarEstadoJuego(); mostrarPanel("lobby"); }
            } else {
                int opcion = -1;
                while (opcion != 0 && opcion != 1) {
                    opcion = JOptionPane.showOptionDialog(this, msg + "\n\nQue quieres hacer?", "Fin del Juego!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"SEGUIR OBSERVANDO", "SALIR"}, "SEGUIR OBSERVANDO");
                    if (opcion == -1) JOptionPane.showMessageDialog(this, "Debes elegir una opcion", "Seleccion Obligatoria", JOptionPane.WARNING_MESSAGE);
                }
                if (opcion == 0) { panelSalaEspera.reiniciar(); limpiarEstadoJuego(); mostrarPanel("espera"); }
                else { cliente.salirSala(); limpiarEstadoJuego(); mostrarPanel("lobby"); }
            }
        });
    }
    public void alActualizarEstado(EstadoJuego e) { actualizarPanelesJugadores(e); }
    public void alMensajeChat(int id, String nombre, String msg) { SwingUtilities.invokeLater(() -> { areaChat.append(nombre + ": " + msg + "\n"); areaChat.setCaretPosition(areaChat.getDocument().getLength()); }); }
    public void alError(String msg) { SwingUtilities.invokeLater(() -> { panelInfo.registrar("! " + msg); JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }); }
    public void alRecibirRankings(java.util.List<Usuario> rankings) { panelRankings.actualizarRankings(rankings); }
    public void alRecibirJugadoresListos(java.util.List<String> listos) { panelSalaEspera.actualizarJugadoresListos(listos); }
    
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> { VentanaJuego v = new VentanaJuego(); v.setVisible(true); });
    }
}
