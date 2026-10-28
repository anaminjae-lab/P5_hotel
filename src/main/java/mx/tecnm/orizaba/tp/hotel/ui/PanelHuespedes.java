package mx.tecnm.orizaba.tp.hotel.ui;

import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Nivel;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelHuespedes extends JPanel {

    // Servicio compartido con la ventana principal.
    private final Hotel hotel;

    // Componentes de la tabla.
    private final JTable tablaHuespedes;
    private final DefaultTableModel modeloTabla;

    public PanelHuespedes(Hotel hotel) {

        this.hotel = hotel;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ===== TÍTULO =====

        JLabel titulo = new JLabel("Gestión de Huéspedes");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));

        add(titulo, BorderLayout.NORTH);

        // ===== TABLA =====

        String[] columnas = {
                "ID", "Nombre", "Correo electrónico", "Nivel"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaHuespedes = new JTable(modeloTabla);
        tablaHuespedes.setRowHeight(30);
        tablaHuespedes.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        add(new JScrollPane(tablaHuespedes), BorderLayout.CENTER);

        // ===== BOTONES =====

        JPanel panelBotones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        JButton btnAgregar = new JButton("Agregar");
        JButton btnCambiarNivel = new JButton("Cambiar nivel");
        JButton btnActualizar = new JButton("Actualizar");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnCambiarNivel);
        panelBotones.add(btnActualizar);

        add(panelBotones, BorderLayout.SOUTH);

        // ===== EVENTOS =====

        btnAgregar.addActionListener(
                e -> mostrarFormularioAgregar()
        );

        btnCambiarNivel.addActionListener(
                e -> mostrarFormularioNivel()
        );

        btnActualizar.addActionListener(
                e -> cargarHuespedes()
        );

        // Cargamos los huéspedes existentes.
        cargarHuespedes();
    }

    // ===== CONSULTAR HUÉSPEDES =====

    private void cargarHuespedes() {

        // Limpiamos la tabla para evitar duplicados.
        modeloTabla.setRowCount(0);

        // Consultamos los huéspedes registrados.
        for (Huesped huesped : hotel.huespeds()) {

            modeloTabla.addRow(new Object[]{
                    huesped.id(),
                    huesped.nombre(),
                    huesped.correo(),
                    huesped.nivel()
            });
        }
    }

    // ===== AGREGAR HUÉSPED =====

    private void mostrarFormularioAgregar() {

        JTextField campoNombre = new JTextField();
        JTextField campoCorreo = new JTextField();

        // El enum Nivel contiene las opciones permitidas.
        JComboBox<Nivel> campoNivel = new JComboBox<>(
                Nivel.values()
        );

        JPanel formulario = new JPanel(
                new GridLayout(3, 2, 10, 10)
        );

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Correo electrónico:"));
        formulario.add(campoCorreo);

        formulario.add(new JLabel("Nivel:"));
        formulario.add(campoNivel);

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Registrar huésped",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String nombre = campoNombre.getText().trim();
            String correo = campoCorreo.getText().trim();
            Nivel nivel = (Nivel) campoNivel.getSelectedItem();

            // Hotel valida los datos y guarda en SQLite.
            Huesped nuevo = hotel.registrarHuesped(
                    nombre,
                    correo,
                    nivel
            );

            cargarHuespedes();

            JOptionPane.showMessageDialog(
                    this,
                    "Huésped registrado correctamente.\n"
                            + "ID asignado: " + nuevo.id()
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al registrar huésped",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ===== CAMBIAR NIVEL =====

    private void mostrarFormularioNivel() {

        int fila = tablaHuespedes.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un huésped para cambiar su nivel."
            );
            return;
        }

        // Obtenemos el ID de la fila seleccionada.
        int filaModelo = tablaHuespedes.convertRowIndexToModel(fila);

        int id = (Integer) modeloTabla.getValueAt(
                filaModelo, 0
        );

        Huesped huesped = hotel.huesped(id);

        JComboBox<Nivel> campoNivel = new JComboBox<>(
                Nivel.values()
        );

        campoNivel.setSelectedItem(huesped.nivel());

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                campoNivel,
                "Cambiar nivel de " + huesped.nombre(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Nivel nuevoNivel = (Nivel) campoNivel.getSelectedItem();

            // Actualizamos mediante el servicio Hotel.
            hotel.cambiarNivel(id, nuevoNivel);

            cargarHuespedes();

            JOptionPane.showMessageDialog(
                    this,
                    "Nivel actualizado correctamente."
            );

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al cambiar nivel",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}