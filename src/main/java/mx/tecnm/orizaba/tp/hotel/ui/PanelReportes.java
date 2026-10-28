package mx.tecnm.orizaba.tp.hotel.ui;

import mx.tecnm.orizaba.tp.hotel.modelo.Estado;
import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.DoubleSummaryStatistics;
import java.util.Map;

public class PanelReportes extends JPanel {

    private final Hotel hotel;

    // Panel donde colocaremos todas las tarjetas y reportes.
    private final JPanel contenido;

    // Colores del dashboard.
    private static final Color FONDO = new Color(35, 39, 45);
    private static final Color TARJETA = new Color(47, 53, 61);
    private static final Color AZUL = new Color(65, 145, 230);
    private static final Color VERDE = new Color(75, 190, 145);
    private static final Color TEXTO = new Color(235, 238, 242);
    private static final Color SECUNDARIO = new Color(165, 175, 185);

    public PanelReportes(Hotel hotel) {

        this.hotel = hotel;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setBackground(FONDO);

        // ===== ENCABEZADO =====

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        JLabel titulo = new JLabel("Panel de Control");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(TEXTO);

        JLabel subtitulo = new JLabel(
                "Resumen de actividad y estadísticas del hotel"
        );
        subtitulo.setForeground(SECUNDARIO);

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 5));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        encabezado.add(textos, BorderLayout.WEST);

        JButton btnActualizar = new JButton("Actualizar datos");
        btnActualizar.addActionListener(e -> cargarReportes());

        encabezado.add(btnActualizar, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);

        // ===== CONTENIDO DESPLAZABLE =====

        contenido = new JPanel();
        contenido.setLayout(
                new BoxLayout(contenido, BoxLayout.Y_AXIS)
        );
        contenido.setBackground(FONDO);

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        add(scroll, BorderLayout.CENTER);

        cargarReportes();
    }

    // ===== CREAR TARJETA =====

    private JPanel crearTarjeta(String titulo, String valor, Color color) {

        JPanel tarjeta = new JPanel(new BorderLayout(10, 10));

        tarjeta.setBackground(TARJETA);
        tarjeta.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        etiqueta.setForeground(SECUNDARIO);

        JLabel numero = new JLabel(valor);
        numero.setFont(new Font("Segoe UI", Font.BOLD, 30));
        numero.setForeground(color);

        tarjeta.add(etiqueta, BorderLayout.NORTH);
        tarjeta.add(numero, BorderLayout.CENTER);

        return tarjeta;
    }

    // ===== CREAR TÍTULO DE SECCIÓN =====

    private JLabel crearTitulo(String texto) {

        JLabel titulo = new JLabel(texto);

        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(TEXTO);
        titulo.setBorder(new EmptyBorder(20, 0, 12, 0));

        return titulo;
    }

    // ===== CREAR FILA INFORMATIVA =====

    private JPanel crearFila(String etiqueta, String valor) {

        JPanel fila = new JPanel(new BorderLayout());

        fila.setBackground(TARJETA);
        fila.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel izquierda = new JLabel(etiqueta);
        izquierda.setForeground(TEXTO);

        JLabel derecha = new JLabel(valor);
        derecha.setForeground(AZUL);
        derecha.setFont(new Font("Segoe UI", Font.BOLD, 15));

        fila.add(izquierda, BorderLayout.WEST);
        fila.add(derecha, BorderLayout.EAST);

        return fila;
    }

    // ===== GENERAR DASHBOARD =====

    private void cargarReportes() {

        try {

            contenido.removeAll();

            // ===== RESUMEN GENERAL =====

            int habitaciones = 0;

            for (Habitacion habitacion : hotel.catalogo()) {
                habitaciones++;
            }

            int huespedes = hotel.huespeds().size();
            int reservaciones = hotel.registros().size();

            DoubleSummaryStatistics estadisticas =
                    hotel.estadisticas();

            // Cuatro tarjetas en una cuadrícula.
            JPanel resumen = new JPanel(
                    new GridLayout(2, 2, 15, 15)
            );

            resumen.setOpaque(false);
            resumen.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 210)
            );

            resumen.add(crearTarjeta(
                    "Habitaciones registradas",
                    String.valueOf(habitaciones),
                    AZUL
            ));

            resumen.add(crearTarjeta(
                    "Huéspedes registrados",
                    String.valueOf(huespedes),
                    VERDE
            ));

            resumen.add(crearTarjeta(
                    "Reservaciones",
                    String.valueOf(reservaciones),
                    new Color(190, 150, 245)
            ));

            resumen.add(crearTarjeta(
                    "Ingresos totales",
                    String.format("$%,.2f", estadisticas.getSum()),
                    new Color(240, 190, 100)
            ));

            contenido.add(resumen);

            // ===== RESERVACIONES POR ESTADO =====

            contenido.add(
                    crearTitulo("Reservaciones por estado")
            );

            JPanel panelEstados = new JPanel();
            panelEstados.setLayout(
                    new BoxLayout(panelEstados, BoxLayout.Y_AXIS)
            );
            panelEstados.setBackground(TARJETA);

            Map<Estado, Long> estados =
                    hotel.conteoPorEstado();

            for (Estado estado : Estado.values()) {

                long cantidad =
                        estados.getOrDefault(estado, 0L);

                panelEstados.add(
                        crearFila(
                                estado.toString(),
                                String.valueOf(cantidad)
                        )
                );
            }

            panelEstados.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 220)
            );

            contenido.add(panelEstados);

            // ===== ESTADÍSTICAS =====

            contenido.add(
                    crearTitulo("Estadísticas de ingresos")
            );

            JPanel panelEstadisticas = new JPanel();
            panelEstadisticas.setLayout(
                    new BoxLayout(
                            panelEstadisticas,
                            BoxLayout.Y_AXIS
                    )
            );
            panelEstadisticas.setBackground(TARJETA);

            panelEstadisticas.add(crearFila(
                    "Reservaciones finalizadas",
                    String.valueOf(estadisticas.getCount())
            ));

            panelEstadisticas.add(crearFila(
                    "Ingreso promedio",
                    String.format(
                            "$%,.2f",
                            estadisticas.getAverage()
                    )
            ));

            if (estadisticas.getCount() > 0) {

                panelEstadisticas.add(crearFila(
                        "Ingreso mínimo",
                        String.format(
                                "$%,.2f",
                                estadisticas.getMin()
                        )
                ));

                panelEstadisticas.add(crearFila(
                        "Ingreso máximo",
                        String.format(
                                "$%,.2f",
                                estadisticas.getMax()
                        )
                ));
            }

            panelEstadisticas.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 220)
            );

            contenido.add(panelEstadisticas);

            // ===== INGRESOS POR TIPO =====

            contenido.add(
                    crearTitulo("Ingresos por tipo de habitación")
            );

            JPanel panelIngresos = new JPanel();
            panelIngresos.setLayout(
                    new BoxLayout(
                            panelIngresos,
                            BoxLayout.Y_AXIS
                    )
            );
            panelIngresos.setBackground(TARJETA);

            Map<String, Double> ingresos =
                    hotel.ingresosPorTipo();

            if (ingresos.isEmpty()) {

                panelIngresos.add(
                        crearFila(
                                "Sin ingresos registrados",
                                "$0.00"
                        )
                );

            } else {

                ingresos.forEach((tipo, total) -> {

                    panelIngresos.add(
                            crearFila(
                                    tipo,
                                    String.format("$%,.2f", total)
                            )
                    );
                });
            }

            panelIngresos.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 200)
            );

            contenido.add(panelIngresos);

            // ===== HABITACIÓN MÁS DEMANDADA =====

            contenido.add(
                    crearTitulo("Habitación más demandada")
            );

            JPanel panelDemandada = new JPanel();
            panelDemandada.setLayout(
                    new BoxLayout(
                            panelDemandada,
                            BoxLayout.Y_AXIS
                    )
            );
            panelDemandada.setBackground(TARJETA);

            hotel.masDemandado().ifPresentOrElse(

                    habitacion -> panelDemandada.add(
                            crearFila(
                                    habitacion.clave().toString(),
                                    habitacion.nombre()
                            )
                    ),

                    () -> panelDemandada.add(
                            crearFila(
                                    "Sin demanda registrada",
                                    "—"
                            )
                    )
            );

            panelDemandada.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 100)
            );

            contenido.add(panelDemandada);

            // ===== TOP 5 HUÉSPEDES =====

            contenido.add(
                    crearTitulo("Top 5 huéspedes")
            );

            JPanel panelMejores = new JPanel();
            panelMejores.setLayout(
                    new BoxLayout(
                            panelMejores,
                            BoxLayout.Y_AXIS
                    )
            );
            panelMejores.setBackground(TARJETA);

            var mejores = hotel.mejores(5);

            if (mejores.isEmpty()) {

                panelMejores.add(
                        crearFila(
                                "Sin huéspedes en el ranking",
                                "—"
                        )
                );

            } else {

                int posicion = 1;

                for (Huesped huesped : mejores) {

                    panelMejores.add(
                            crearFila(
                                    posicion++ + ". " + huesped.nombre(),
                                    huesped.nivel().toString()
                            )
                    );
                }
            }

            panelMejores.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 250)
            );

            contenido.add(panelMejores);

            // Actualizamos la pantalla.
            contenido.revalidate();
            contenido.repaint();

        } catch (RuntimeException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Error al generar reportes",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}