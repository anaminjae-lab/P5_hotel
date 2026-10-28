package mx.tecnm.orizaba.tp.hotel.ui;

import mx.tecnm.orizaba.tp.hotel.eventos.Evento;
import mx.tecnm.orizaba.tp.hotel.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class PanelEventos extends JPanel {

    private final Hotel hotel;

    private final JTable tablaEventos;
    private final DefaultTableModel modeloTabla;

    // Guardamos el observador para poder desuscribirlo.
    private final OyenteEvento oyente;

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public PanelEventos(Hotel hotel) {

        this.hotel = hotel;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ===== TÍTULO =====

        JLabel titulo = new JLabel("Eventos del Hotel");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));

        add(titulo, BorderLayout.NORTH);

        // ===== TABLA =====

        String[] columnas = {
                "Fecha y hora", "Tipo", "Mensaje"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaEventos = new JTable(modeloTabla);
        tablaEventos.setRowHeight(30);

        add(new JScrollPane(tablaEventos), BorderLayout.CENTER);

        // ===== BOTONES =====

        JPanel panelBotones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        JButton btnLimpiar = new JButton("Limpiar pantalla");

        panelBotones.add(btnLimpiar);

        add(panelBotones, BorderLayout.SOUTH);

        btnLimpiar.addActionListener(
                e -> modeloTabla.setRowCount(0)
        );

        // ===== SUSCRIPCIÓN A EVENTOS =====

        // El observador recibe los eventos del sistema.
        oyente = this::recibirEvento;

        // Registramos el observador en el servicio.
        hotel.suscribir(oyente);
    }

    // ===== RECIBIR EVENTOS =====

    private void recibirEvento(Evento evento) {

        // Los eventos pueden llegar desde un hilo diferente.
        // Swing exige actualizar sus componentes desde el EDT.
        SwingUtilities.invokeLater(() -> {

            modeloTabla.addRow(new Object[]{
                    evento.instante().format(FORMATO),
                    evento.tipo(),
                    evento.mensaje()
            });
        });
    }

    // ===== DESUSCRIBIR =====

    public void cerrar() {

        // Evitamos que el hotel siga notificando a un panel cerrado.
        hotel.desuscribir(oyente);
    }
}