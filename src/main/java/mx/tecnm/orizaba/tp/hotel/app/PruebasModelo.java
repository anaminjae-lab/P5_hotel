package mx.tecnm.orizaba.tp.hotel.app;

import mx.tecnm.orizaba.tp.hotel.eventos.Evento;
import mx.tecnm.orizaba.tp.hotel.eventos.TipoEvento;
import mx.tecnm.orizaba.tp.hotel.funcional.FabricaElemento;
import mx.tecnm.orizaba.tp.hotel.funcional.Regla;
import mx.tecnm.orizaba.tp.hotel.funcional.Reglas;
import mx.tecnm.orizaba.tp.hotel.modelo.*;
import mx.tecnm.orizaba.tp.hotel.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.hotel.persistencia.Respaldo;
import mx.tecnm.orizaba.tp.hotel.servicio.Hotel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pruebas de aceptación del modelo (sin dependencias externas). Deben pasar al 100 %
 * antes de comenzar la UI. Ejecutar: {@code java ... mx.tecnm.orizaba.tp.hotel.app.PruebasModelo}
 */
public final class PruebasModelo {

    private static int aprobadas;
    private static final List<String> fallidas = new ArrayList<>();

    @FunctionalInterface
    private interface Caso {
        void ejecutar() throws Exception;
    }

    private static void prueba(String nombre, Caso caso) {
        try {
            caso.ejecutar();
            aprobadas++;
            System.out.println("  [OK]    " + nombre);
        } catch (Throwable t) {
            fallidas.add(nombre);
            System.out.println("  [FALLA] " + nombre + " -> " + t);
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void cerca(double esperado, double real) {
        verificar(Math.abs(esperado - real) < 0.01, "se esperaba " + esperado + " y se obtuvo " + real);
    }

    private static void lanza(Class<? extends Throwable> tipo, Caso caso) {
        try {
            caso.ejecutar();
        } catch (Throwable t) {
            verificar(tipo.isInstance(t), "se esperaba " + tipo.getSimpleName() + " y se obtuvo " + t);
            return;
        }
        throw new AssertionError("se esperaba " + tipo.getSimpleName());
    }

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempDirectory("pruebas-hotel");
        System.out.println("Pruebas del modelo – Hotel");

        prueba("T01 NumeroHabitacion: formato, parse y validación", () -> {
            NumeroHabitacion c = NumeroHabitacion.parse("ABC-0042");
            verificar(c.toString().equals("ABC-0042"), "toString");
            verificar(c.compareTo(new NumeroHabitacion("ABC", 43)) < 0, "orden");
            lanza(IllegalArgumentException.class, () -> new NumeroHabitacion("ab", 1));
            lanza(IllegalArgumentException.class, () -> NumeroHabitacion.parse("ABC_1"));
        });

        prueba("T02 Invariantes de Habitacion (encapsulamiento)", () -> {
            lanza(ReglaNegocioException.class, () -> new Habitacion.Sencilla("x", -1, 1));
            lanza(ReglaNegocioException.class, () -> new Habitacion.Sencilla(" ", 10, 1));
            lanza(ReglaNegocioException.class, () -> new Habitacion.Doble("x", 10, 0));
            lanza(ReglaNegocioException.class, () -> new Habitacion.Sencilla("x", 10, 1).tarifa(0));
        });

        prueba("T03 Polimorfismo: tarifas por subtipo (Template Method)", () -> {
            Reservable[] v = {
                new Habitacion.Sencilla("a", 900.0, 1),
                new Habitacion.Doble("b", 1300.0, 7),
                new Habitacion.Suite("c", 3000.0, 85) };
            cerca(2700.0, v[0].tarifa(3));
            cerca(3120.0, v[1].tarifa(2));
            cerca(13500.0, v[2].tarifa(5));
            cerca(Math.round(2700.0 * 1.16 * 100.0) / 100.0, v[0].tarifaConImpuesto(3));
        });

        prueba("T04 Clasificación múltiple y colisión de métodos default", () -> {
            Object s1 = new Habitacion.Sencilla("a", 1, 1);
            Object s2 = new Habitacion.Doble("b", 1, 7);
            Habitacion.Suite s3 = new Habitacion.Suite("c", 1, 85);
            verificar(s1 instanceof ConVista && !(s1 instanceof Accesible), "Sencilla sólo ConVista");
            verificar(s2 instanceof Accesible && !(s2 instanceof ConVista), "Doble sólo Accesible");
            verificar(s3 instanceof ConVista && s3 instanceof Accesible, "Suite ambas");
            String d = s3.descripcionCorta();
            verificar(d.contains("Vista") && d.contains("Accesibilidad"), "colisión resuelta: " + d);
        });

        prueba("T05 Retorno covariante en copiar()", () -> {
            Habitacion.Sencilla a = new Habitacion.Sencilla("a", 5, 1);
            Habitacion.Sencilla b = a.copiar();   // sin conversión: retorno covariante
            verificar(!a.equals(b) && a.nombre().equals(b.nombre()), "copia con clave nueva");
        });

        prueba("T06 Máquina de estados de Reservacion", () -> {
            Huesped x = new Huesped(900, "Prueba", "prueba@ito.mx");
            Reservacion t = new Reservacion(900, x, LocalDate.now());
            lanza(ReglaNegocioException.class, t::iniciar);
            t.agregar(new Habitacion.Sencilla("a", 10, 1), 1);
            t.iniciar();
            lanza(ReglaNegocioException.class, () -> t.agregar(new Habitacion.Sencilla("b", 1, 1), 1));
            t.cerrar();
            lanza(ReglaNegocioException.class, t::cancelar);
            verificar(t.estado() == Estado.CERRADA, "estado final");
        });

        prueba("T07 Composición: la línea usa el nivel de la envolvente", () -> {
            Nivel n = Nivel.values()[Nivel.values().length - 1];
            Huesped x = new Huesped(901, "Prueba", "p2@ito.mx", n);
            Reservacion t = new Reservacion(901, x, LocalDate.now());
            Reservacion.Linea l = t.agregar(new Habitacion.Sencilla("a", 900.0, 1), 3);
            cerca(n.aplicar(2700.0), l.importe());
            verificar(l.contenedor() == t, "Reservacion.this");
            lanza(ReglaNegocioException.class, () -> t.agregar(l.elemento(), 2));
        });

        prueba("T08 Agregación, iterador oculto, clase anónima y clase local", () -> {
            Habitacion a = new Habitacion.Sencilla("Alfa", 300, 1);
            Habitacion b = new Habitacion.Doble("Beta", 100, 7);
            Inventario cat = new Inventario(a, b);
            lanza(ReglaNegocioException.class, () -> cat.agregar(a));
            Iterator<Habitacion> it = cat.iterator();
            verificar(!it.getClass().getSimpleName().isEmpty() && it.hasNext(), "iterador");
            lanza(UnsupportedOperationException.class, it::remove);
            verificar(cat.ordenados(Inventario.porTarifa(1)).get(0) == b, "comparador anónimo");
            verificar(cat.resumen().startsWith("2 "), "clase local: " + cat.resumen());
            verificar(cat.deTipo(Habitacion.Sencilla.class).size() == 1, "genéricos acotados");
        });

        prueba("T09 Interfaz funcional propia y composición de reglas", () -> {
            Habitacion a = new Habitacion.Sencilla("Alfa", 300, 1);
            Habitacion b = new Habitacion.Suite("Gamma", 100, 85);
            Inventario cat = new Inventario(a, b);
            Regla<Habitacion> r = Reglas.precioHasta(200).y(Reglas.conCapacidad(Accesible.class));
            verificar(cat.filtrar(r).equals(List.of(b)), "y");
            verificar(cat.filtrar(r.negar()).equals(List.of(a)), "negar");
            verificar(cat.filtrar(Reglas.nombreContiene("ALF").o(r)).size() == 2, "o");
            FabricaElemento<Habitacion.Sencilla, Integer> f = Habitacion.Sencilla::new;
            verificar(f.crear("Delta", 1, 1).tipo().equals("Sencilla"), "referencia a constructor");
        });

        prueba("T10 Serialización: transient y Externalizable", () -> {
            Habitacion a = new Habitacion.Sencilla("Alfa", 10, 1);
            a.tarifa(1);
            Huesped x = new Huesped(7, "Ada", "ada@ito.mx", Nivel.values()[1]);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
                oos.writeObject(a);
                oos.writeObject(x);
            }
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                Habitacion a2 = (Habitacion) ois.readObject();
                Huesped x2 = (Huesped) ois.readObject();
                verificar(a2.equals(a) && a2.cotizaciones() == 0 && a.cotizaciones() == 1, "transient");
                verificar(x2.correo().equals("ada@ito.mx") && x2.nivel() == Nivel.values()[1], "externalizable");
            }
        });

        Path bd = tmp.resolve("pruebas.db");
        AtomicReference<LocalDate> hoy = new AtomicReference<>(LocalDate.now());
        int[] ids = new int[2];

        prueba("T11 Servicio + SQLite: alta, flujo y reportes", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Hotel g = new Hotel(cx, hoy::get);
                List<Evento> eventos = new CopyOnWriteArrayList<>();
                g.suscribir(eventos::add);
                Habitacion e1 = g.registrar(Habitacion.Sencilla::new, "Uno", 900.0, 1);
                Habitacion e3 = g.registrar(Habitacion.Suite::new, "Tres", 3000.0, 85);
                Huesped a = g.registrarHuesped("Grace Hopper", "grace@ito.mx", Nivel.values()[0]);
                lanza(ReglaNegocioException.class, () -> g.registrarHuesped("Otra", "GRACE@ito.mx", Nivel.values()[0]));
                Reservacion t = g.abrir(a.id());
                g.agregarLinea(t.id(), e1.clave().toString(), 3);
                g.agregarLinea(t.id(), e3.clave().toString(), 5);
                g.iniciar(t.id());
                g.cerrar(t.id());
                ids[0] = t.id();
                cerca(2700.0 + 13500.0, g.estadisticas().getSum());
                verificar(g.ingresosPorTipo().size() == 2, "ingresos por tipo");
                verificar(g.mejores(1).equals(List.of(a)), "mejores");
                verificar(g.masDemandado().isPresent(), "más demandado");
                verificar(g.describir(e1).contains("Sencilla"), "describir");
                verificar(g.proyectar(Habitacion::nombre).containsAll(List.of("Uno", "Tres")), "proyectar");
                verificar(eventos.stream().anyMatch(ev -> ev.tipo() == TipoEvento.ALTA), "eventos");
                lanza(ReglaNegocioException.class, () -> g.retirar(e1.clave()));
                ids[1] = g.abrir(a.id()).id();
            }
        });

        prueba("T12 Persistencia: la información sobrevive al reinicio", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Hotel g = new Hotel(cx, hoy::get);
                verificar(g.catalogo().tamanio() == 2, "catálogo");
                verificar(g.huespeds().size() == 1, "huéspedes");
                Reservacion t = g.registro(ids[0]);
                verificar(t.estado() == Estado.CERRADA && t.lineas().size() == 2, "registro reconstruido");
                cerca(2700.0 + 13500.0, t.total());
            }
        });

        prueba("T13 Monitor: reglas, hilo y detención", () -> {
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Hotel g = new Hotel(cx, hoy::get);
                List<Evento> eventos = new CopyOnWriteArrayList<>();
                g.suscribir(eventos::add);
                Reservacion t = g.abrir(g.huespeds().get(0).id());
                g.agregarLinea(t.id(), g.catalogo().iterator().next().clave().toString(), 1);
                g.iniciar(t.id());
                hoy.set(hoy.get().plusDays(Hotel.PLAZO_DIAS + 1));
                verificar(g.revisar() == 2, "cancelación + alerta");
                verificar(g.registro(ids[1]).estado() == Estado.CANCELADA, "abierta sin líneas cancelada");
                verificar(g.revisar() == 0, "la alerta no se repite");
                g.iniciarMonitor(20);
                Thread.sleep(120);
                verificar(g.monitorActivo(), "hilo activo");
                g.detenerMonitor();
                verificar(!g.monitorActivo(), "hilo detenido");
                long alertas = eventos.stream().filter(ev -> ev.tipo() == TipoEvento.ALERTA).count();
                verificar(alertas == 1, "alertas: " + alertas);
            }
        });

        prueba("T14 Respaldo binario (exportar/importar)", () -> {
            Path archivo = tmp.resolve("respaldo.bin");
            try (ConexionSQLite cx = new ConexionSQLite(bd.toString())) {
                Hotel g = new Hotel(cx, hoy::get);
                g.exportarRespaldo(archivo);
                Respaldo.Contenido c = Respaldo.importar(archivo);
                verificar(c.catalogo().tamanio() == g.catalogo().tamanio(), "catálogo");
                verificar(c.registros().size() == g.registros().size(), "registros");
                Reservacion.Linea l = c.registros().get(0).lineas().get(0);
                verificar(l.contenedor() == c.registros().get(0), "clase interna serializada con su envolvente");
            }
        });

        System.out.printf("%nResultado: %d aprobadas, %d fallidas%n", aprobadas, fallidas.size());
        if (!fallidas.isEmpty()) {
            System.exit(1);
        }
    }
}
