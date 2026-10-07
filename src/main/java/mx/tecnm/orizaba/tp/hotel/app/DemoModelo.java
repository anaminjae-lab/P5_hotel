package mx.tecnm.orizaba.tp.hotel.app;

import mx.tecnm.orizaba.tp.hotel.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.hotel.eventos.Evento;
import mx.tecnm.orizaba.tp.hotel.funcional.Reglas;
import mx.tecnm.orizaba.tp.hotel.modelo.*;
import mx.tecnm.orizaba.tp.hotel.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Recorrido de consola por el modelo: sirve como referencia de uso para construir la UI.
 * La UI del estudiante debe ofrecer, como mínimo, lo que aquí se muestra.
 */
public final class DemoModelo {

    public static void main(String[] args) throws Exception {
        Path bd = args.length > 0 ? Path.of(args[0]) : Files.createTempFile("hotel", ".db");
        AtomicReference<LocalDate> hoy = new AtomicReference<>(LocalDate.now());

        try (ConexionSQLite cx = new ConexionSQLite(bd.toString());
             Hotel g = new Hotel(cx, hoy::get)) {

            // Suscripción con clase anónima (la UI usaría Platform.runLater / invokeLater aquí).
            g.suscribir(new OyenteEvento() {
                @Override
                public void notificar(Evento e) {
                    System.out.println("   evento " + e + " [" + Thread.currentThread().getName() + "]");
                }
            });

            System.out.println("== Alta de habitaciones (referencias a constructor)");
            g.registrar(Habitacion.Sencilla::new, "Sencilla 101", 900.0, 1);
            g.registrar(Habitacion.Sencilla::new, "Sencilla 102", 950.0, 2);
            g.registrar(Habitacion.Doble::new, "Doble 201", 1300.0, 2);
            g.registrar(Habitacion.Doble::new, "Doble 702", 1300.0, 7);
            g.registrar(Habitacion.Suite::new, "Suite Citlaltépetl", 3000.0, 85);
            g.registrar(Habitacion.Suite::new, "Suite Presidencial", 4500.0, 120);

            System.out.println("== huéspedes");
            g.registrarHuesped("Julia Ortega", "julia.ortega@gmail.com", Nivel.ESTANDAR);
            g.registrarHuesped("Kevin Ramos", "kramos@outlook.com", Nivel.FRECUENTE);
            g.registrarHuesped("TecNM Orizaba", "viajes@orizaba.tecnm.mx", Nivel.CONVENIO);

            System.out.println("== Flujo de reservación");
            Reservacion t = g.abrir(1);
            g.agregarLinea(t.id(), "SEN-0001", 2);
            g.agregarLinea(t.id(), "SUI-0001", 3);
            g.iniciar(t.id());
            t.forEach(System.out::println);
            g.cerrar(t.id());
            Reservacion t2 = g.abrir(2);
            g.agregarLinea(t2.id(), "DOB-0001", 1);
            g.iniciar(t2.id());
            g.abrir(3);

            System.out.println("== Reportes");
            System.out.println(g.catalogo().resumen());
            System.out.println("Ingresos por tipo: " + g.ingresosPorTipo());
            System.out.println("Estadísticas: " + g.estadisticas());
            System.out.println("Conteo por estado: " + g.conteoPorEstado());
            g.mejores(3).forEach(System.out::println);
            g.buscar(Reglas.conCapacidad(ConVista.class)).stream().map(g::describir).forEach(System.out::println);

            System.out.println("== Monitor en segundo plano (reloj simulado +" + (Hotel.PLAZO_DIAS + 1) + " días)");
            g.iniciarMonitor(100);
            hoy.set(hoy.get().plusDays(Hotel.PLAZO_DIAS + 1));
            Thread.sleep(400);
            g.detenerMonitor();
            System.out.println("Base de datos: " + bd.toAbsolutePath());
        }
    }
}
