package mx.tecnm.orizaba.tp.hotel.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;
import mx.tecnm.orizaba.tp.hotel.persistencia.ConexionSQLite;
/** Ventana principal de la interfaz gráfica del sistema hotelero. */
public class VentanaPrincipal extends JFrame {

    // ===== PALETA DE COLORES DEL HOTEL =====

    // Fondo oscuro del menú lateral.
    private static final Color COLOR_MENU = new Color(28, 34, 43);

    // Fondo principal de la aplicación.
    private static final Color COLOR_FONDO = new Color(34, 40, 49);

    // Azul principal del sistema.
    private static final Color COLOR_AZUL = new Color(65, 145, 245);

    // Fondo de los botones.
    private static final Color COLOR_BOTON = new Color(45, 53, 65);

    // Color del texto.
    private static final Color COLOR_TEXTO = new Color(235, 240, 248);

    // Servicio que administra las operaciones del hotel.
    private final Hotel hotel;

    /** Construye la ventana principal y sus paneles de gestión. */
    public VentanaPrincipal() {

        // ===== CONEXIÓN CON EL SERVICIO =====

        // Creamos la conexión con SQLite.
        ConexionSQLite conexion = new ConexionSQLite("datos.db");

        // Creamos el servicio del hotel.
        hotel = new Hotel(conexion);

        // ===== CONFIGURACIÓN DE LA VENTANA =====

        setTitle("Sistema de Reservaciones de un Hotel");
        setSize(1100, 700);
        setLocationRelativeTo(null);

        // Controlamos el cierre para detener correctamente el servicio.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {

            /**
             * Atiende el cierre de la ventana y libera los recursos del sistema.
             *
             * @param e evento de cierre de ventana
             */
            @Override
            public void windowClosing(WindowEvent e) {
                hotel.close();
                dispose();
            }
        });

        // Panel principal de la aplicación.
        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(COLOR_FONDO);

        // ===== MENÚ LATERAL =====

        JPanel menu = new JPanel();
        menu.setBackground(COLOR_MENU);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(220, 0));

        menu.setBorder(
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        );

        // ===== TÍTULO DEL MENÚ =====

        JLabel logo = new JLabel("HOTEL", SwingConstants.CENTER);

        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(COLOR_AZUL);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ===== CREACIÓN DE BOTONES =====

        JButton btnHabitaciones = new JButton("Habitaciones");
        JButton btnHuespedes = new JButton("Huéspedes");
        JButton btnReservaciones = new JButton("Reservaciones");
        JButton btnReportes = new JButton("Reportes");
        JButton btnEventos = new JButton("Eventos");

        // ===== CONFIGURACIÓN DE BOTONES =====

        Dimension tamanoBoton = new Dimension(190, 45);

        JButton[] botones = {
                btnHabitaciones,
                btnHuespedes,
                btnReservaciones,
                btnReportes,
                btnEventos
        };

        for (JButton boton : botones) {

            boton.setMaximumSize(tamanoBoton);
            boton.setPreferredSize(tamanoBoton);
            boton.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Colores del botón.
            boton.setBackground(COLOR_BOTON);
            boton.setForeground(COLOR_TEXTO);

            // Tipografía.
            boton.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            // Bordes redondeados mediante FlatLaf.
            boton.putClientProperty("JButton.buttonType", "roundRect");

            // Evitamos que el botón conserve el foco visual.
            boton.setFocusPainted(false);

            // Cambiamos el cursor al pasar el mouse.
            boton.setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );
        }

        // ===== IDENTIFICAR LA SECCIÓN SELECCIONADA =====

// Cuando seleccionamos una sección, su botón cambia a azul.
        for (JButton boton : botones) {

            boton.addActionListener(e -> {

                // Primero regresamos todos los botones a su color normal.
                for (JButton otro : botones) {
                    otro.setBackground(COLOR_BOTON);
                    otro.setForeground(COLOR_TEXTO);
                }

                // Resaltamos únicamente el botón seleccionado.
                boton.setBackground(COLOR_AZUL);
                boton.setForeground(Color.WHITE);
            });
        }

        // ===== AGREGAMOS LOS BOTONES AL MENÚ =====

        menu.add(logo);
        menu.add(Box.createVerticalStrut(35));

        for (JButton boton : botones) {
            menu.add(boton);
            menu.add(Box.createVerticalStrut(12));
        }

        // ===== ÁREA DE CONTENIDO =====

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(COLOR_FONDO);

        // ===== PANTALLA DE BIENVENIDA =====

// Panel que contiene toda la información de inicio.
        JPanel inicio = new JPanel(new BorderLayout(20, 30));
        inicio.setBackground(COLOR_FONDO);

        inicio.setBorder(
                BorderFactory.createEmptyBorder(60, 60, 60, 60)
        );

// ===== ENCABEZADO =====

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 10));
        encabezado.setBackground(COLOR_FONDO);

        JLabel bienvenida = new JLabel("Bienvenido al Hotel");
        bienvenida.setFont(new Font("Segoe UI", Font.BOLD, 34));
        bienvenida.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel(
                "Sistema de gestión de habitaciones, huéspedes y reservaciones"
        );

        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitulo.setForeground(new Color(160, 175, 195));

        encabezado.add(bienvenida);
        encabezado.add(subtitulo);

        inicio.add(encabezado, BorderLayout.NORTH);

// ===== TARJETAS DE ESTADÍSTICAS =====

        JPanel tarjetas = new JPanel(new GridLayout(2, 2, 20, 20));
        tarjetas.setBackground(COLOR_FONDO);

// Contamos las habitaciones registradas.
        int totalHabitaciones = 0;

        for (var habitacion : hotel.catalogo()) {
            totalHabitaciones++;
        }

// Consultamos la información del hotel.
        int totalHuespedes = hotel.huespeds().size();
        int totalReservaciones = hotel.registros().size();
        double ingresos = hotel.estadisticas().getSum();

// Creamos las cuatro tarjetas.
        tarjetas.add(crearTarjeta(
                "HABITACIONES",
                String.valueOf(totalHabitaciones),
                new Color(65, 145, 245)
        ));

        tarjetas.add(crearTarjeta(
                "HUÉSPEDES",
                String.valueOf(totalHuespedes),
                new Color(65, 190, 140)
        ));

        tarjetas.add(crearTarjeta(
                "RESERVACIONES",
                String.valueOf(totalReservaciones),
                new Color(180, 125, 245)
        ));

        tarjetas.add(crearTarjeta(
                "INGRESOS TOTALES",
                String.format("$%.2f", ingresos),
                new Color(245, 185, 85)
        ));

        inicio.add(tarjetas, BorderLayout.CENTER);

// Mostramos la pantalla de inicio.
        contenido.add(inicio, BorderLayout.CENTER);

        // ===== ACCIÓN DEL BOTÓN HABITACIONES =====

        btnHabitaciones.addActionListener(e -> {

            // Quitamos el contenido anterior.
            contenido.removeAll();

            // Mostramos el panel y le compartimos el servicio Hotel.
            contenido.add(
                    new PanelHabitaciones(hotel),
                    BorderLayout.CENTER
            );

            // Actualizamos la interfaz.
            contenido.revalidate();
            contenido.repaint();
        });

        // ===== ACCIÓN DEL BOTÓN HUÉSPEDES =====

        btnHuespedes.addActionListener(e -> {

            // Eliminamos el contenido anterior.
            contenido.removeAll();

            // Mostramos el panel de huéspedes.
            contenido.add(
                    new PanelHuespedes(hotel),
                    BorderLayout.CENTER
            );

            // Actualizamos la ventana.
            contenido.revalidate();
            contenido.repaint();
        });

        // ===== BOTÓN RESERVACIONES =====

        btnReservaciones.addActionListener(e -> {

            contenido.removeAll();

            contenido.add(
                    new PanelReservaciones(hotel),
                    BorderLayout.CENTER
            );

            contenido.revalidate();
            contenido.repaint();
        });

        // ===== BOTÓN REPORTES =====

        btnReportes.addActionListener(e -> {

            contenido.removeAll();

            contenido.add(
                    new PanelReportes(hotel),
                    BorderLayout.CENTER
            );

            contenido.revalidate();
            contenido.repaint();
        });

        // ===== BOTÓN EVENTOS =====

// Creamos el panel una sola vez para no acumular observadores.
        PanelEventos panelEventos = new PanelEventos(hotel);

        btnEventos.addActionListener(e -> {

            contenido.removeAll();

            contenido.add(panelEventos, BorderLayout.CENTER);

            contenido.revalidate();
            contenido.repaint();
        });

        // ===== ORGANIZACIÓN DE LA VENTANA =====

        principal.add(menu, BorderLayout.WEST);
        principal.add(contenido, BorderLayout.CENTER);

        setContentPane(principal);
    }

    // ===== CREAR TARJETAS DEL PANEL DE INICIO =====

    private JPanel crearTarjeta(String titulo, String valor, Color color) {

        JPanel tarjeta = new JPanel(new BorderLayout(10, 15));

        tarjeta.setBackground(new Color(45, 53, 65));

        tarjeta.setBorder(
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        // Nombre de la estadística.
        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        etiqueta.setForeground(new Color(180, 195, 210));

        // Valor de la estadística.
        JLabel numero = new JLabel(valor);
        numero.setFont(new Font("Segoe UI", Font.BOLD, 36));
        numero.setForeground(color);

        tarjeta.add(etiqueta, BorderLayout.NORTH);
        tarjeta.add(numero, BorderLayout.CENTER);

        return tarjeta;
    }

    // ===== MÉTODO PRINCIPAL =====

    /**
     * Inicia la aplicación gráfica del hotel.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {

        // Activamos el tema oscuro de FlatLaf.
        FlatDarkLaf.setup();

        // Ejecutamos Swing en su hilo de eventos.
        SwingUtilities.invokeLater(() -> {

            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}