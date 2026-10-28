package mx.tecnm.orizaba.tp.hotel.ui;

import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Reservacion;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelReservaciones extends JPanel {

    private final Hotel hotel;
    private final JTable tabla;
    private final DefaultTableModel modeloTabla;

    public PanelReservaciones(Hotel hotel) {
        this.hotel = hotel;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ===== TÍTULO =====

        JLabel titulo = new JLabel("Gestión de Reservaciones");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        add(titulo, BorderLayout.NORTH);

        // ===== TABLA =====

        String[] columnas = {
                "ID", "Huésped", "Fecha", "Estado",
                "Habitaciones", "Total"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(30);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ===== BOTONES =====

        JPanel botones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        JButton btnAbrir = new JButton("Abrir");
        JButton btnAgregar = new JButton("Agregar habitación");
        JButton btnQuitar = new JButton("Quitar habitación");
        JButton btnIniciar = new JButton("Iniciar");
        JButton btnCerrar = new JButton("Cerrar");
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnActualizar = new JButton("Actualizar");

        botones.add(btnAbrir);
        botones.add(btnAgregar);
        botones.add(btnQuitar);
        botones.add(btnIniciar);
        botones.add(btnCerrar);
        botones.add(btnCancelar);
        botones.add(btnActualizar);

        // Varias filas de botones si la ventana es estrecha.
        JPanel zonaInferior = new JPanel(new BorderLayout());
        zonaInferior.add(botones, BorderLayout.CENTER);
        add(zonaInferior, BorderLayout.SOUTH);

        // ===== EVENTOS =====

        btnAbrir.addActionListener(e -> abrirReservacion());
        btnAgregar.addActionListener(e -> agregarHabitacion());
        btnQuitar.addActionListener(e -> quitarHabitacion());

        btnIniciar.addActionListener(
                e -> cambiarEstado("Iniciar")
        );

        btnCerrar.addActionListener(
                e -> cambiarEstado("Cerrar")
        );

        btnCancelar.addActionListener(
                e -> cambiarEstado("Cancelar")
        );

        btnActualizar.addActionListener(
                e -> cargarReservaciones()
        );

        cargarReservaciones();
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