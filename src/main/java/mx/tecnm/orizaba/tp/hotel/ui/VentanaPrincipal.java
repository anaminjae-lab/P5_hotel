package mx.tecnm.orizaba.tp.hotel.ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;
import mx.tecnm.orizaba.tp.hotel.persistencia.ConexionSQLite;

public class VentanaPrincipal extends JFrame {

    // Servicio que administra las operaciones del hotel.
    private final Hotel hotel;

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
            @Override
            public void windowClosing(WindowEvent e) {
                hotel.close();
                dispose();
            }
        });

        // Panel principal de la aplicación.
        JPanel principal = new JPanel(new BorderLayout());

        // ===== MENÚ LATERAL =====

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setPreferredSize(new Dimension(220, 0));

        menu.setBorder(
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        );

        // ===== TÍTULO DEL MENÚ =====

        JLabel logo = new JLabel("HOTEL", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
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

        JLabel titulo = new JLabel(
                "BIENVENIDO AL SISTEMA DE RESERVACIONES",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));

        contenido.add(titulo, BorderLayout.CENTER);

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

    // ===== MÉTODO PRINCIPAL =====

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