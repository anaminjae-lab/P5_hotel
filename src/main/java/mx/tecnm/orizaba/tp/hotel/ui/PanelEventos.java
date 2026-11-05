package mx.tecnm.orizaba.tp.hotel.ui;

import mx.tecnm.orizaba.tp.hotel.eventos.Evento;
import mx.tecnm.orizaba.tp.hotel.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/** Panel que muestra los eventos generados por el hotel. */
public class PanelEventos extends JPanel {

    private final Hotel hotel;
    private final JTable tablaEventos;
    private final DefaultTableModel modeloTabla;

    // Conservamos el observador para desuscribirlo al cerrar el panel.
    private final OyenteEvento oyente;

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Color FONDO = new Color(34, 39, 46);
    private static final Color SUPERFICIE = new Color(43, 52, 64);
    private static final Color CABECERA = new Color(37, 47, 61);
    private static final Color AZUL = new Color(54, 123, 203);
    private static final Color GRIS = new Color(78, 80, 83);
    private static final Color SECUNDARIO = new Color(169, 199, 235);

    /**
     * Construye el panel de eventos.
     *
     * @param hotel servicio del hotel
     */
    public PanelEventos(Hotel hotel) {
        this.hotel = hotel;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(35, 35, 25, 35));
        setBackground(FONDO);

        // ===== ENCABEZADO =====
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);

        JLabel titulo = new JLabel("Eventos del Hotel");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Consulta la actividad registrada en el sistema");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitulo.setForeground(SECUNDARIO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(12));
        encabezado.add(subtitulo);
        add(encabezado, BorderLayout.NORTH);

        // ===== TABLA =====
        String[] columnas = {"Fecha y hora", "Tipo", "Mensaje"};
        modeloTabla = new DefaultTableModel(columnas, 0) {

            /**
             * Impide editar directamente las celdas de la tabla.
             *
             * @param fila índice de la fila
             * @param columna índice de la columna
             * @return false porque las celdas no son editables
             */
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaEventos = new JTable(modeloTabla);
        tablaEventos.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tablaEventos.setForeground(Color.WHITE);
        tablaEventos.setBackground(SUPERFICIE);
        tablaEventos.setSelectionBackground(AZUL);
        tablaEventos.setSelectionForeground(Color.WHITE);
        tablaEventos.setRowHeight(48);
        tablaEventos.setShowGrid(false);
        tablaEventos.setIntercellSpacing(new Dimension(0, 0));
        tablaEventos.setFillsViewportHeight(true);
        tablaEventos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader cabecera = tablaEventos.getTableHeader();
        cabecera.setFont(new Font("Segoe UI", Font.BOLD, 17));
        cabecera.setForeground(SECUNDARIO);
        cabecera.setBackground(CABECERA);
        cabecera.setPreferredSize(new Dimension(0, 60));
        cabecera.setReorderingAllowed(false);

        // Evita que el aspecto visual de la cabecera dependa del tema de Windows.
        DefaultTableCellRenderer renderCabecera = new DefaultTableCellRenderer();
        renderCabecera.setHorizontalAlignment(SwingConstants.CENTER);
        renderCabecera.setBackground(CABECERA);
        renderCabecera.setForeground(SECUNDARIO);
        renderCabecera.setFont(new Font("Segoe UI", Font.BOLD, 17));
        renderCabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, GRIS));
        for (int i = 0; i < tablaEventos.getColumnCount(); i++) {
            tablaEventos.getColumnModel().getColumn(i).setHeaderRenderer(renderCabecera);
        }

        tablaEventos.getColumnModel().getColumn(0).setPreferredWidth(220);
        tablaEventos.getColumnModel().getColumn(1).setPreferredWidth(180);
        tablaEventos.getColumnModel().getColumn(2).setPreferredWidth(580);

        JScrollPane desplazamiento = new JScrollPane(tablaEventos);
        desplazamiento.setBorder(BorderFactory.createLineBorder(GRIS));
        desplazamiento.getViewport().setBackground(SUPERFICIE);
        add(desplazamiento, BorderLayout.CENTER);

        // ===== BOTÓN =====
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBotones.setOpaque(false);

        JButton btnLimpiar = new JButton("Limpiar pantalla");
        btnLimpiar.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setBackground(GRIS);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setOpaque(true);
        btnLimpiar.setPreferredSize(new Dimension(190, 52));
        btnLimpiar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panelBotones.add(btnLimpiar);
        add(panelBotones, BorderLayout.SOUTH);

        // Solo borra las filas mostradas, como en el código original.
        btnLimpiar.addActionListener(e -> modeloTabla.setRowCount(0));

        // ===== SUSCRIPCIÓN A EVENTOS =====
        oyente = this::recibirEvento;
        hotel.suscribir(oyente);
    }

    // ===== RECIBIR EVENTOS =====
    private void recibirEvento(Evento evento) {
        // Los eventos pueden llegar desde otro hilo: Swing se actualiza en el EDT.
        SwingUtilities.invokeLater(() -> {
            modeloTabla.addRow(new Object[]{
                    evento.instante().format(FORMATO),
                    evento.tipo(),
                    evento.mensaje()
            });
        });
    }

    // ===== DESUSCRIBIR =====
    /** Libera los recursos y suscripciones utilizados por el panel. */
    public void cerrar() {
        hotel.desuscribir(oyente);
    }
}
