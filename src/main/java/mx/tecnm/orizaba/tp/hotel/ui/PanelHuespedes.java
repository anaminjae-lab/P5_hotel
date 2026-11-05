package mx.tecnm.orizaba.tp.hotel.ui;

import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Nivel;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
/** Panel para registrar y consultar huéspedes del hotel. */
public class PanelHuespedes extends JPanel {

    // Servicio compartido con la ventana principal.
    private final Hotel hotel;

    // Componentes de la tabla.
    private final JTable tablaHuespedes;
    private final DefaultTableModel modeloTabla;

    /**
     * Construye el panel de gestión de huéspedes.
     *
     * @param hotel servicio utilizado para administrar huéspedes
     */
    public PanelHuespedes(Hotel hotel) {

        this.hotel = hotel;

        // ===== ESTILO GENERAL =====
        setBackground(new Color(34, 39, 46));
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(35, 35, 30, 35));

        // ===== TÍTULO =====

        // ===== ENCABEZADO =====

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBackground(new Color(34, 39, 46));

        JLabel titulo = new JLabel("Gestión de Huéspedes");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel(
                "Administra los datos y niveles de tus huéspedes"
        );
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitulo.setForeground(new Color(170, 190, 215));
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(10));
        encabezado.add(subtitulo);

        add(encabezado, BorderLayout.NORTH);

        // ===== TABLA =====

        String[] columnas = {
                "ID", "Nombre", "Correo electrónico", "Nivel"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {

            /**
             * Indica que las celdas de la tabla no pueden editarse.
             *
             * @param fila índice de la fila
             * @param columna índice de la columna
             * @return false porque la tabla es de solo lectura
             */
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
// ===== ESTILO DE LA TABLA =====

        tablaHuespedes = new JTable(modeloTabla);

// Colores de las filas.
        tablaHuespedes.setBackground(new Color(44, 52, 64));
        tablaHuespedes.setForeground(Color.WHITE);

// Color de la fila seleccionada.
        tablaHuespedes.setSelectionBackground(new Color(55, 105, 170));
        tablaHuespedes.setSelectionForeground(Color.WHITE);

// Altura de las filas.
        tablaHuespedes.setRowHeight(42);

// Ocultamos las líneas de la tabla.
        tablaHuespedes.setShowGrid(false);

// Encabezados.
        tablaHuespedes.getTableHeader().setBackground(
                new Color(36, 45, 58)
        );

        tablaHuespedes.getTableHeader().setForeground(
                new Color(170, 200, 240)
        );

        tablaHuespedes.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 14)
        );

        tablaHuespedes.getTableHeader().setPreferredSize(
                new Dimension(0, 45)
        );
        tablaHuespedes.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        add(new JScrollPane(tablaHuespedes), BorderLayout.CENTER);

        // ===== BOTONES =====

        JPanel panelBotones = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10)
        );

        panelBotones.setBackground(new Color(34, 39, 46));

        JButton btnAgregar = new JButton("Agregar");
        JButton btnCambiarNivel = new JButton("Cambiar nivel");
        JButton btnActualizar = new JButton("Actualizar");

        // ===== ESTILO DE BOTONES =====

        JButton[] botones = {
                btnAgregar, btnCambiarNivel, btnActualizar
        };

        for (JButton boton : botones) {
            boton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            boton.setPreferredSize(new Dimension(145, 40));
            boton.setFocusPainted(false);
        }

// Botón principal.
        btnAgregar.setBackground(new Color(50, 115, 200));
        btnAgregar.setForeground(Color.WHITE);

// Botón para modificar el nivel.
        btnCambiarNivel.setBackground(new Color(65, 95, 145));
        btnCambiarNivel.setForeground(Color.WHITE);

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