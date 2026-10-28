package mx.tecnm.orizaba.tp.hotel.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;
import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;

public class PanelHabitaciones extends JPanel {
    private final Hotel hotel;

    // Tabla donde mostraremos las habitaciones.
    private final JTable tablaHabitaciones;

    // Modelo que administra las filas y columnas de la tabla.
    private final DefaultTableModel modeloTabla;

    public PanelHabitaciones(Hotel hotel) {
        this.hotel = hotel;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ===== TÍTULO =====

        JLabel titulo = new JLabel("Gestión de Habitaciones");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));

        add(titulo, BorderLayout.NORTH);

        // ===== TABLA =====
        String[] columnas = {
                "Número",
                "Nombre",
                "Tipo",
                "Precio base"
        };

        // Creamos una tabla que no permita editar directamente las celdas.
        modeloTabla = new DefaultTableModel(columnas, 0) {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaHabitaciones = new JTable(modeloTabla);
        // Ajusta la altura de las filas.
        tablaHabitaciones.setRowHeight(30);
        // selecciona una fila completa.
        tablaHabitaciones.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        JScrollPane scroll = new JScrollPane(tablaHabitaciones);

        add(scroll, BorderLayout.CENTER);

        // ===== BOTONES =====

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAgregar = new JButton("Agregar");
        // Cuando el usuario presione Agregar, abriremos un formulario.
        btnAgregar.addActionListener(e -> mostrarFormularioAgregar());
        JButton btnEditar = new JButton("Editar");
        btnEditar.addActionListener(e -> mostrarFormularioEditar());
        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarHabitacion());
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarHabitaciones());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);

        add(panelBotones, BorderLayout.SOUTH);

        cargarHabitaciones();
    }
    // Consulta las habitaciones del hotel y las muestra en la tabla.
    private void cargarHabitaciones() {

        // 1. Limpiamos la tabla para evitar filas duplicadas.
        modeloTabla.setRowCount(0);

        // 2. Recorremos las habitaciones del inventario.
        for (Habitacion habitacion : hotel.catalogo()) {

            // 3. Agregamos una fila por cada habitación.
            modeloTabla.addRow(new Object[]{
                    habitacion.clave(),
                    habitacion.nombre(),
                    habitacion.getClass().getSimpleName(),
                    habitacion.precioBase()
            });
        }
    }

    // Muestra un formulario para registrar una habitación.
    private void mostrarFormularioAgregar() {

        // Campo para escribir el nombre de la habitación.
        JTextField campoNombre = new JTextField();

        // Campo para escribir el precio.
        JTextField campoPrecio = new JTextField();

        // Lista para seleccionar el tipo de habitación.
        JComboBox<String> campoTipo = new JComboBox<>(
                new String[]{"Sencilla", "Doble", "Suite"}
        );
        JTextField campoAtributo = new JTextField();

        // Creamos el formulario.
        JPanel formulario = new JPanel(new GridLayout(4, 2, 10, 10));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Precio base:"));
        formulario.add(campoPrecio);

        formulario.add(new JLabel("Tipo:"));
        formulario.add(campoTipo);

        // Campo para camas, piso o metros cuadrados.
        JLabel etiquetaAtributo = new JLabel("Número de camas:");

        formulario.add(etiquetaAtributo);
        formulario.add(campoAtributo);

        // Mostramos el formulario en una ventana emergente.
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Agregar habitación",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta == JOptionPane.OK_OPTION) {

            try {
                // Obtenemos los datos del formulario.
                String nombre = campoNombre.getText().trim();
                double precio = Double.parseDouble(campoPrecio.getText().trim());
                int atributo = Integer.parseInt(campoAtributo.getText().trim());

                // Identificamos el tipo de habitación.
                String tipo = (String) campoTipo.getSelectedItem();

                // Registramos la habitación mediante el servicio Hotel.
                switch (tipo) {

                    case "Sencilla":
                        hotel.registrar(
                                Habitacion.Sencilla::new,
                                nombre,
                                precio,
                                atributo
                        );
                        break;

                    case "Doble":
                        hotel.registrar(
                                Habitacion.Doble::new,
                                nombre,
                                precio,
                                atributo
                        );
                        break;

                    case "Suite":
                        hotel.registrar(
                                Habitacion.Suite::new,
                                nombre,
                                precio,
                                atributo
                        );
                        break;
                }

                // Actualizamos la tabla.
                cargarHabitaciones();

                JOptionPane.showMessageDialog(
                        this,
                        "Habitación registrada correctamente."
                );

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "El precio y el atributo deben ser números válidos.",
                        "Datos incorrectos",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (RuntimeException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Error al registrar",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // Permite modificar el nombre y precio de una habitación existente.
    private void mostrarFormularioEditar() {

        // Obtenemos la fila seleccionada en la tabla.
        int fila = tablaHabitaciones.getSelectedRow();

        // Verificamos que el usuario haya seleccionado una habitación.
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una habitación para editar."
            );
            return;
        }

        // Obtenemos la clave de la habitación seleccionada.
        String clave = modeloTabla.getValueAt(
                tablaHabitaciones.convertRowIndexToModel(fila), 0
        ).toString();

        Habitacion habitacion = hotel.elemento(clave);

        JTextField campoNombre = new JTextField(habitacion.nombre());

        JTextField campoPrecio = new JTextField(
                String.valueOf(habitacion.precioBase())
        );
        JPanel formulario = new JPanel(new GridLayout(2, 2, 10, 10));

        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        formulario.add(new JLabel("Precio base:"));
        formulario.add(campoPrecio);

        // Mostramos el formulario.
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Editar habitación " + clave,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (respuesta == JOptionPane.OK_OPTION) {

            try {
                // Leemos los nuevos valores.
                String nombre = campoNombre.getText().trim();

                double precio = Double.parseDouble(
                        campoPrecio.getText().trim()
                );

                // Validamos ambos datos antes de modificar la habitación.
                if (nombre.isBlank() || precio < 0 || !Double.isFinite(precio)) {
                    throw new IllegalArgumentException(
                            "Escribe un nombre válido y un precio mayor o igual a cero."
                    );
                }

                hotel.actualizar(habitacion, nombre, precio);

                // Recargamos la tabla.
                cargarHabitaciones();

                JOptionPane.showMessageDialog(
                        this,
                        "Habitación actualizada correctamente."
                );

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "El precio debe ser un número válido.",
                        "Datos incorrectos",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (RuntimeException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Error al actualizar",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    // Elimina una habitación seleccionada de la tabla.
    private void eliminarHabitacion() {

        // Obtenemos la fila seleccionada.
        int fila = tablaHabitaciones.getSelectedRow();

        // Verificamos que se haya seleccionado una habitación.
        if (fila == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona una habitación para eliminar."
            );
            return;
        }

        // Obtenemos la clave de la habitación seleccionada.
        String clave = modeloTabla.getValueAt(
                tablaHabitaciones.convertRowIndexToModel(fila), 0
        ).toString();

        // Pedimos confirmación antes de eliminar.
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Deseas eliminar la habitación " + clave + "?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        // Si el usuario selecciona No, cancelamos.
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Buscamos la habitación utilizando su clave.
            Habitacion habitacion = hotel.elemento(clave);

            if (habitacion == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "La habitación ya no existe."
                );
                cargarHabitaciones();
                return;
            }

            // Retiramos la habitación mediante el servicio Hotel.
            boolean eliminada = hotel.retirar(habitacion.clave());

            if (eliminada) {

                // Actualizamos la tabla.
                cargarHabitaciones();

                JOptionPane.showMessageDialog(
                        this,
                        "Habitación eliminada correctamente."
                );

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "No fue posible eliminar la habitación."
                );
            }

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al eliminar",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}