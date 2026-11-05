package mx.tecnm.orizaba.tp.hotel.ui;

import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Reservacion;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel para gestionar las reservaciones y sus estados. */
public class PanelReservaciones extends JPanel {

    private final Hotel hotel;
    private final JTable tabla;
    private final DefaultTableModel modeloTabla;

    private static final Color FONDO = new Color(34, 39, 46);
    private static final Color SUPERFICIE = new Color(43, 52, 64);
    private static final Color CABECERA = new Color(37, 47, 61);
    private static final Color AZUL = new Color(54, 123, 203);
    private static final Color ROJO = new Color(152, 61, 72);
    private static final Color GRIS = new Color(78, 80, 83);
    private static final Color TEXTO = Color.WHITE;
    private static final Color SECUNDARIO = new Color(169, 199, 235);

    /**
     * Construye el panel de gestión de reservaciones.
     *
     * @param hotel servicio utilizado para administrar reservaciones
     */
    public PanelReservaciones(Hotel hotel) {
        this.hotel = hotel;

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(35, 35, 25, 35));
        setBackground(FONDO);

        // ===== ENCABEZADO =====
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBackground(FONDO);

        JLabel titulo = new JLabel("Gestión de Reservaciones");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titulo.setForeground(TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Administra las reservaciones y habitaciones de los huéspedes");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitulo.setForeground(SECUNDARIO);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(12));
        encabezado.add(subtitulo);
        add(encabezado, BorderLayout.NORTH);

        // ===== TABLA =====
        String[] columnas = {
                "ID", "Huésped", "Fecha", "Estado", "Habitaciones", "Total"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {

            /**
             * Impide editar directamente las celdas de reservaciones.
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

        tabla = new JTable(modeloTabla);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setRowHeight(38);
        tabla.setBackground(SUPERFICIE);
        tabla.setForeground(TEXTO);
        tabla.setSelectionBackground(AZUL);
        tabla.setSelectionForeground(TEXTO);
        tabla.setGridColor(new Color(61, 69, 79));
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabla.getTableHeader().setBackground(CABECERA);
        tabla.getTableHeader().setForeground(SECUNDARIO);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 48));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setOpaque(true);

        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setBorder(BorderFactory.createLineBorder(new Color(75, 82, 91)));
        desplazamiento.getViewport().setBackground(SUPERFICIE);
        add(desplazamiento, BorderLayout.CENTER);

        // ===== BOTONES =====
        JButton btnAbrir = crearBoton("Abrir", AZUL);
        JButton btnAgregar = crearBoton("Agregar habitación", AZUL);
        JButton btnQuitar = crearBoton("Quitar habitación", GRIS);
        JButton btnIniciar = crearBoton("Iniciar", AZUL);
        JButton btnCerrar = crearBoton("Cerrar", GRIS);
        JButton btnCancelar = crearBoton("Cancelar", ROJO);
        JButton btnActualizar = crearBoton("Actualizar", GRIS);

        // Dos filas: evita que los siete botones se compriman en una sola línea.
        JPanel botones = new JPanel(new GridLayout(2, 4, 12, 12));
        botones.setBackground(FONDO);
        botones.add(btnAbrir);
        botones.add(btnAgregar);
        botones.add(btnQuitar);
        botones.add(btnIniciar);
        botones.add(btnCerrar);
        botones.add(btnCancelar);
        botones.add(btnActualizar);
        JPanel espacio = new JPanel();
        espacio.setOpaque(false);
        botones.add(espacio);

        JPanel zonaInferior = new JPanel(new BorderLayout());
        zonaInferior.setBackground(FONDO);
        zonaInferior.add(botones, BorderLayout.CENTER);
        add(zonaInferior, BorderLayout.SOUTH);

        // ===== EVENTOS: SE CONSERVA LA LÓGICA ORIGINAL =====
        btnAbrir.addActionListener(e -> abrirReservacion());
        btnAgregar.addActionListener(e -> agregarHabitacion());
        btnQuitar.addActionListener(e -> quitarHabitacion());
        btnIniciar.addActionListener(e -> cambiarEstado("Iniciar"));
        btnCerrar.addActionListener(e -> cambiarEstado("Cerrar"));
        btnCancelar.addActionListener(e -> cambiarEstado("Cancelar"));
        btnActualizar.addActionListener(e -> cargarReservaciones());

        cargarReservaciones();
    }

    private JButton crearBoton(String texto, Color fondo) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        boton.setForeground(TEXTO);
        boton.setBackground(fondo);
        boton.setOpaque(true);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createLineBorder(new Color(99, 103, 109)));
        boton.setPreferredSize(new Dimension(160, 46));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    // ===== MOSTRAR RESERVACIONES =====

    private void cargarReservaciones() {
        modeloTabla.setRowCount(0);

        for (Reservacion r : hotel.registros()) {
            modeloTabla.addRow(new Object[]{
                    r.id(),
                    r.huesped().nombre(),
                    r.fecha(),
                    r.estado(),
                    r.lineas().size(),
                    r.total()
            });
        }
    }

    // ===== OBTENER RESERVACIÓN SELECCIONADA =====

    private Reservacion seleccionada() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una reservación."
            );
            return null;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);
        int id = (Integer) modeloTabla.getValueAt(filaModelo, 0);

        return hotel.registro(id);
    }

    // ===== ABRIR RESERVACIÓN =====

    private void abrirReservacion() {
        if (hotel.huespeds().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero debes registrar un huésped."
            );
            return;
        }

        // Mostramos huéspedes con su ID y nombre.
        JComboBox<String> combo = new JComboBox<>();

        for (Huesped h : hotel.huespeds()) {
            combo.addItem(h.id() + " - " + h.nombre());
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                combo,
                "Seleccionar huésped",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String seleccion = (String) combo.getSelectedItem();

            int idHuesped = Integer.parseInt(
                    seleccion.split(" - ", 2)[0]
            );

            Reservacion nueva = hotel.abrir(idHuesped);

            cargarReservaciones();

            JOptionPane.showMessageDialog(
                    this,
                    "Reservación abierta con ID: " + nueva.id()
            );

        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    // ===== AGREGAR HABITACIÓN =====

    private void agregarHabitacion() {
        Reservacion r = seleccionada();

        if (r == null) {
            return;
        }

        JComboBox<String> combo = new JComboBox<>();

        for (Habitacion h : hotel.catalogo()) {
            combo.addItem(
                    h.clave() + " - " + h.nombre()
            );
        }

        if (combo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No hay habitaciones registradas."
            );
            return;
        }

        JTextField campoUnidades = new JTextField("1");

        JPanel formulario = new JPanel(
                new GridLayout(2, 2, 10, 10)
        );

        formulario.add(new JLabel("Habitación:"));
        formulario.add(combo);

        formulario.add(new JLabel("Unidades:"));
        formulario.add(campoUnidades);

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Agregar habitación a reservación #" + r.id(),
                JOptionPane.OK_CANCEL_OPTION
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String seleccion = (String) combo.getSelectedItem();

            String clave = seleccion.split(" - ", 2)[0];

            int unidades = Integer.parseInt(
                    campoUnidades.getText().trim()
            );

            if (unidades <= 0) {
                throw new IllegalArgumentException(
                        "Las unidades deben ser mayores que cero."
                );
            }

            hotel.agregarLinea(r.id(), clave, unidades);

            cargarReservaciones();

            JOptionPane.showMessageDialog(
                    this,
                    "Habitación agregada correctamente."
            );

        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    // ===== QUITAR HABITACIÓN =====

    private void quitarHabitacion() {
        Reservacion r = seleccionada();

        if (r == null) {
            return;
        }

        JComboBox<String> combo = new JComboBox<>();

        for (Reservacion.Linea linea : r.lineas()) {
            Habitacion h = linea.elemento();

            combo.addItem(
                    h.clave() + " - " + h.nombre()
            );
        }

        if (combo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Esta reservación no tiene habitaciones."
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                combo,
                "Quitar habitación",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String seleccion = (String) combo.getSelectedItem();
            String clave = seleccion.split(" - ", 2)[0];

            boolean eliminada = hotel.quitarLinea(r.id(), clave);

            cargarReservaciones();

            JOptionPane.showMessageDialog(
                    this,
                    eliminada
                            ? "Habitación retirada de la reservación."
                            : "No se encontró la habitación."
            );

        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    // ===== CAMBIAR ESTADO =====

    private void cambiarEstado(String accion) {
        Reservacion r = seleccionada();

        if (r == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas " + accion.toLowerCase()
                        + " la reservación #" + r.id() + "?",
                "Confirmar operación",
                JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            switch (accion) {
                case "Iniciar":
                    hotel.iniciar(r.id());
                    break;

                case "Cerrar":
                    hotel.cerrar(r.id());
                    break;

                case "Cancelar":
                    hotel.cancelar(r.id());
                    break;
            }

            cargarReservaciones();

            JOptionPane.showMessageDialog(
                    this,
                    "Estado actualizado correctamente."
            );

        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    // ===== MOSTRAR ERRORES =====

    private void mostrarError(RuntimeException ex) {
        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Error en reservación",
                JOptionPane.ERROR_MESSAGE
        );
    }
}