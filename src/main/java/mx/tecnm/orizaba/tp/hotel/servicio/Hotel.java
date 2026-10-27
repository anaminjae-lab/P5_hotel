package mx.tecnm.orizaba.tp.hotel.servicio;

import mx.tecnm.orizaba.tp.hotel.concurrencia.TareaPeriodica;
import mx.tecnm.orizaba.tp.hotel.eventos.Evento;
import mx.tecnm.orizaba.tp.hotel.eventos.OyenteEvento;
import mx.tecnm.orizaba.tp.hotel.eventos.TipoEvento;
import mx.tecnm.orizaba.tp.hotel.funcional.FabricaElemento;
import mx.tecnm.orizaba.tp.hotel.funcional.Regla;
import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.Inventario;
import mx.tecnm.orizaba.tp.hotel.modelo.NumeroHabitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.Estado;
import mx.tecnm.orizaba.tp.hotel.modelo.Nivel;
import mx.tecnm.orizaba.tp.hotel.modelo.ReglaNegocioException;
import mx.tecnm.orizaba.tp.hotel.modelo.Reservacion;
import mx.tecnm.orizaba.tp.hotel.persistencia.ConexionSQLite;
import mx.tecnm.orizaba.tp.hotel.persistencia.Repositorio;
import mx.tecnm.orizaba.tp.hotel.persistencia.RepositorioHuespedSQLite;
import mx.tecnm.orizaba.tp.hotel.persistencia.RepositorioHabitacionSQLite;
import mx.tecnm.orizaba.tp.hotel.persistencia.RepositorioReservacionSQLite;
import mx.tecnm.orizaba.tp.hotel.persistencia.Respaldo;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * Fachada del dominio (capa de servicio). <strong>La UI sólo debe depender de esta clase</strong>
 * y de los tipos del paquete {@code modelo}/{@code eventos}; nunca de JDBC.
 * <ul>
 *   <li>Concurrencia: {@link MonitorSalidas} es una clase interna que <em>es una</em>
 *       {@link TareaPeriodica} y <em>tiene acceso</em> a {@code Hotel.this}: herencia múltiple de
 *       implementación mediante clases internas (véase {@code J04_Multiple_De_Implementacion}).</li>
 *   <li>Programación funcional: reportes con streams y funciones de orden superior.</li>
 * </ul>
 */
public class Hotel implements AutoCloseable {

    /** Días tras los cuales un(a) reservación en curso genera una alerta. */
    public static final int PLAZO_DIAS = 4;

    private final Inventario catalogo = new Inventario();
    private final Map<Integer, Huesped> huespeds = new ConcurrentHashMap<>();
    private final Map<Integer, Reservacion> registros = new ConcurrentHashMap<>();
    private final Set<Integer> alertados = ConcurrentHashMap.newKeySet();
    private final List<OyenteEvento> oyentes = new CopyOnWriteArrayList<>();

    private final Repositorio<Habitacion, NumeroHabitacion> repoElementos;
    private final Repositorio<Huesped, Integer> repoHuespeds;
    private final Repositorio<Reservacion, Integer> repoRegistros;
    private final Supplier<LocalDate> reloj;
    private MonitorSalidas monitor;

    public Hotel(ConexionSQLite bd) {
        this(bd, LocalDate::now);
    }

    /** @param reloj proveedor de la fecha actual (inyectable para pruebas y simulación) */
    public Hotel(ConexionSQLite bd, Supplier<LocalDate> reloj) {
        // TODO E15: Crear los tres repositorios SQLite, asignar el reloj y cargar los datos existentes en memoria.
        this.reloj = java.util.Objects.requireNonNull(reloj);
        this.repoElementos = new RepositorioHabitacionSQLite(bd);
        this.repoHuespeds = new RepositorioHuespedSQLite(bd);
        this.repoRegistros = new RepositorioReservacionSQLite(
                bd, repoHuespeds, repoElementos
        );

        // Recuperamos los datos almacenados en SQLite.
        cargar();

       // throw new UnsupportedOperationException("TODO E15");
    }

    private void cargar() {
        // TODO E15: Usar referencias a método: repoElementos.todos().forEach(catalogo::agregar), etc.
        repoElementos.todos().forEach(catalogo::agregar);
        repoHuespeds.todos().forEach(
                huesped -> huespeds.put(huesped.id(), huesped)
        );
        // Recuperamos las reservaciones y las guardamos en el mapa.
        repoRegistros.todos().forEach(
                reservacion -> registros.put(reservacion.id(), reservacion)
        );
      //  throw new UnsupportedOperationException("TODO E15");
    }

    // ------------------------------------------------------------------ eventos

    public void suscribir(OyenteEvento oyente) {
        oyentes.add(oyente);
    }

    public void desuscribir(OyenteEvento oyente) {
        oyentes.remove(oyente);
    }

    private void publicar(TipoEvento tipo, String mensaje) {
        Evento e = Evento.de(tipo, mensaje);
        oyentes.forEach(o -> o.notificar(e));
    }

    // ------------------------------------------------------------------ catálogo

    /**
     * Alta genérica de cualquier subtipo mediante una fábrica (función de orden superior).
     * Ejemplo: {@code registrar(Habitacion.Sencilla::new, "...", 100.0, 1)}.
     */
    public <E extends Habitacion, A> E registrar(FabricaElemento<E, A> fabrica, String nombre, double precio, A atributo) {
        // TODO E15: Crear con la fábrica, persistir, agregar al catálogo, publicar ALTA y devolver el elemento.
        E elemento = fabrica.crear(nombre, precio, atributo);
        repoElementos.guardar(elemento);
        catalogo.agregar(elemento);
        publicar(TipoEvento.ALTA,
                "Habitación registrada: " + elemento.clave());
        return elemento;

        //throw new UnsupportedOperationException("TODO E15");
    }

    public void actualizar(Habitacion elemento, String nombre, double precio) {
        elemento.setNombre(nombre);
        elemento.setPrecioBase(precio);
        repoElementos.guardar(elemento);
    }

    public boolean retirar(NumeroHabitacion clave) {
        // TODO E15: Impedir retirar elementos referenciados por algún registro; eliminar de la BD y del catálogo.
        boolean enUso = registros.values().stream()
                .anyMatch(reservacion ->
                        reservacion.lineas().stream()
                                .anyMatch(linea ->
                                        linea.elemento().clave().equals(clave)
                                )
                );
        if (enUso) {
            throw new ReglaNegocioException(
                    "No se puede eliminar una habitación asociada a una reservación"
            );
        }
        boolean eliminado = repoElementos.eliminar(clave);

        // 4. Si se eliminó de SQLite, también la retiramos del catálogo.
        if (eliminado) {
            catalogo.retirar(clave);
        }

        // 5. Devolvemos si la eliminación fue exitosa.
        return eliminado;
        //throw new UnsupportedOperationException("TODO E15");
    }

    public Inventario catalogo() {
        return catalogo;
    }

    public Habitacion elemento(String clave) {
        return catalogo.buscar(NumeroHabitacion.parse(clave))
                .orElseThrow(() -> new ReglaNegocioException("No existe " + clave));
    }

    // ------------------------------------------------------------------ huéspedes

    public Huesped registrarHuesped(String nombre, String correo, Nivel nivel) {
        // TODO E15: Rechazar correos repetidos; id = máximo actual + 1 (stream mapToInt/max); persistir y publicar ALTA.
        boolean repetido = huespeds.values().stream()
                .anyMatch(h -> h.correo().equalsIgnoreCase(correo));
        if (repetido) {
            throw new ReglaNegocioException(
                    "Ya existe un huésped con ese correo"
            );
        }
        int nuevoId = huespeds.values().stream()
                .mapToInt(Huesped::id)
                .max()
                .orElse(0) + 1;
        Huesped nuevo = new Huesped(nuevoId, nombre, correo, nivel);
        repoHuespeds.guardar(nuevo);
        huespeds.put(nuevo.id(), nuevo);
        publicar(TipoEvento.ALTA,
                "Huésped registrado: " + nuevo.nombre());
        return nuevo;


        //throw new UnsupportedOperationException("TODO E15");
    }

    public void cambiarNivel(int idHuesped, Nivel nivel) {
        Huesped a = huesped(idHuesped);
        a.setNivel(nivel);
        repoHuespeds.guardar(a);
    }

    public Huesped huesped(int id) {
        return Optional.ofNullable(huespeds.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<Huesped> huespeds() {
        return huespeds.values().stream().sorted(Comparator.comparingInt(Huesped::id)).toList();
    }

    // ------------------------------------------------------------------ reservaciones

    public Reservacion abrir(int idHuesped) {
        // TODO E15: Crear el registro con la fecha del reloj, persistir, guardar en el mapa y publicar CAMBIO_ESTADO.
        // 1. Buscamos al huésped.
        Huesped h = huesped(idHuesped);

        // 2. Calculamos el siguiente identificador disponible.
        int nuevoId = registros.values().stream()
                .mapToInt(Reservacion::id)
                .max()
                .orElse(0) + 1;

        // 3. Creamos la reservación utilizando el reloj inyectado.
        Reservacion nueva = new Reservacion(nuevoId, h, reloj.get());

        // 4. Guardamos la reservación en SQLite.
        repoRegistros.guardar(nueva);

        // 5. La agregamos al mapa en memoria.
        registros.put(nueva.id(), nueva);

        // 6. Publicamos el evento de cambio de estado.
        publicar(TipoEvento.CAMBIO_ESTADO,
                "Reservación abierta: " + nueva.id());

        // 7. Devolvemos la reservación creada.
        return nueva;

        //throw new UnsupportedOperationException("TODO E15");
    }

    public Reservacion.Linea agregarLinea(int idReservacion, String clave, int unidades) {
        // TODO E15: Localizar registro y elemento, agregar la línea y persistir.
        Reservacion reservacion = registro(idReservacion);
        Habitacion habitacion = elemento(clave);
        // El método agregar() devuelve un objeto de la clase interna Linea.
        Reservacion.Linea nuevaLinea = reservacion.agregar(habitacion, unidades);
        repoRegistros.guardar(reservacion);
        return nuevaLinea;
        //throw new UnsupportedOperationException("TODO E15");
    }

    public boolean quitarLinea(int idReservacion, String clave) {
        Reservacion t = registro(idReservacion);
        boolean ok = t.quitar(NumeroHabitacion.parse(clave));
        repoRegistros.guardar(t);
        return ok;
    }

    public void iniciar(int idReservacion) {
        transitar(idReservacion, Reservacion::iniciar);
    }

    public void cerrar(int idReservacion) {
        transitar(idReservacion, Reservacion::cerrar);
    }

    public void cancelar(int idReservacion) {
        transitar(idReservacion, Reservacion::cancelar);
    }

    /** Orden superior: la transición concreta llega como {@link Consumer} (referencia a método). */
    private void transitar(int id, Consumer<Reservacion> transicion) {
        // TODO E15: Aplicar la transición, persistir y publicar CAMBIO_ESTADO.
        Reservacion reservacion = registro(id);
        // Puede ser iniciar(), cerrar() o cancelar().
        transicion.accept(reservacion);
        repoRegistros.guardar(reservacion);
        publicar(TipoEvento.CAMBIO_ESTADO,
                "Reservación " + id + ": " + reservacion.estado());
        //throw new UnsupportedOperationException("TODO E15");
    }

    public Reservacion registro(int id) {
        return Optional.ofNullable(registros.get(id))
                .orElseThrow(() -> new ReglaNegocioException("No existe el registro " + id));
    }

    public List<Reservacion> registros() {
        return registros.values().stream().sorted(Comparator.comparingInt(Reservacion::id)).toList();
    }

    public List<Reservacion> registrosEn(Estado estado) {
        return registros().stream().filter(t -> t.estado() == estado).toList();
    }

    // ------------------------------------------------------------------ reportes (streams)

    private Stream<Reservacion> cerrados() {
        return registros.values().stream().filter(t -> t.estado() == Estado.CERRADA);
    }

    /** Ingresos por subtipo (sólo registros cerrados). */
    public Map<String, Double> ingresosPorTipo() {
        // TODO E16: flatMap de líneas; groupingBy(tipo, TreeMap::new, summingDouble(importe)).
        return cerrados()
                // flatMap une todas las líneas en un solo Stream.
                .flatMap(reservacion -> reservacion.lineas().stream())
                .collect(Collectors.groupingBy(
                        linea -> linea.elemento().getClass().getSimpleName(),
                        TreeMap::new,
                        Collectors.summingDouble(Reservacion.Linea::importe)
                ));

       // throw new UnsupportedOperationException("TODO E16");
    }

    /** El habitación con más unidades en registros cerrados. */
    public Optional<Habitacion> masDemandado() {
        // TODO E16: groupingBy(elemento, summingInt(unidades)) y máximo por valor con Map.Entry.comparingByValue().
        return cerrados()
                .flatMap(reservacion -> reservacion.lineas().stream())
                .collect(Collectors.groupingBy(
                        Reservacion.Linea::elemento,
                        Collectors.summingInt(Reservacion.Linea::unidades)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);


        //throw new UnsupportedOperationException("TODO E16");
    }

    /** Los {@code n} huéspedes con mayor importe acumulado. */
    public List<Huesped> mejores(int n) {
        // TODO E16: groupingBy(actor, summingDouble(total)); ordenar descendentemente por valor; limit(n).
        Map<Huesped, Double> importes = registros.values().stream()
                .collect(Collectors.groupingBy(
                        Reservacion::huesped,
                        Collectors.summingDouble(Reservacion::total)
                ));
        return importes.entrySet().stream()
                .sorted(Map.Entry.<Huesped, Double>comparingByValue().reversed())

                .limit(n)

                .map(Map.Entry::getKey)

                .toList();


        //throw new UnsupportedOperationException("TODO E16");
    }

    public DoubleSummaryStatistics estadisticas() {
        // TODO E16: mapToDouble(Reservacion::total).summaryStatistics() sobre los cerrados.
        return registros.values().stream()
                .filter(reservacion ->
                        reservacion.estado() == Estado.CERRADA)
                .mapToDouble(Reservacion::total)
                .summaryStatistics();

        //throw new UnsupportedOperationException("TODO E16");
    }

    public Map<Boolean, List<Habitacion>> particionarPorPrecio(double umbral) {
        // TODO E16: partitioningBy(e -> e.precioBase() >= umbral).
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                catalogo.iterator(), Spliterator.ORDERED
                        ),
                        false
                )
                .collect(Collectors.partitioningBy(
                        habitacion -> habitacion.precioBase() >= umbral
                ));

        //throw new UnsupportedOperationException("TODO E16");
    }

    public Map<Estado, Long> conteoPorEstado() {
        return registros.values().stream()
                .collect(Collectors.groupingBy(Reservacion::estado, () -> new TreeMap<>(), Collectors.counting()));
    }

    public List<Habitacion> buscar(Regla<? super Habitacion> regla) {
        return catalogo.filtrar(regla);
    }

    /** Proyección genérica (orden superior). */
    public <R> List<R> proyectar(Function<? super Habitacion, ? extends R> funcion) {
        // TODO E16: Recorrer el catálogo ordenado por clave y aplicar map(funcion).
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                catalogo.iterator(), Spliterator.ORDERED
                        ),
                        false
                )

                // 2. Ordenamos las habitaciones por su clave.
                .sorted(Comparator.comparing(
                        habitacion -> habitacion.clave().toString()
                ))

                // 3. Aplicamos la función recibida a cada habitación.
                .<R>map(funcion)

                // 4. Guardamos los resultados en una lista.
                .toList();

       // throw new UnsupportedOperationException("TODO E16");
    }

    /** Descripción con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada (sin default). */
    public String describir(Habitacion e) {
        // TODO E16: switch (e) con un caso por subtipo y al menos un caso con guarda "when".
        // Identificamos el subtipo de habitación recibido.
        return switch (e) {
            case Habitacion.Sencilla s when s.precioBase() > 1000 ->
                    "Sencilla premium: " + s.nombre();

            case Habitacion.Sencilla s ->
                    "Sencilla: " + s.nombre();

            case Habitacion.Doble d ->
                    "Doble: " + d.nombre();

            case Habitacion.Suite s ->
                    "Suite: " + s.nombre();
        };

        //throw new UnsupportedOperationException("TODO E16");
    }

    // ------------------------------------------------------------------ monitor

    /**
     * Un ciclo de revisión (síncrono y probado de forma determinista):
     * cancela registros ABIERTOS sin líneas con al menos un día de antigüedad y emite una
     * ALERTA (una sola vez) por cada registro EN_CURSO con más de {@link #PLAZO_DIAS} días.
     *
     * @return número de acciones realizadas
     */
    public synchronized int revisar() {
        // TODO E12: Recorrer los registros y aplicar las dos reglas con un switch sobre el estado.
        // Consultar una sola vez la fecha proporcionada por el reloj.
        LocalDate hoy = reloj.get();
        int acciones = 0;
        for (Reservacion reservacion : registros.values()) {
            long dias = reservacion.diasDesde(hoy);

            switch (reservacion.estado()) {
                case ABIERTA -> {
                    // Cancelar únicamente si está vacía y tiene al menos un día.
                    if (reservacion.lineas().isEmpty() && dias >= 1) {
                        // Utilizar el método del servicio para cambiar el estado.
                        cancelar(reservacion.id());
                        acciones++;

                    }
                }
                case EN_CURSO -> {
                    // Alertar si supera el plazo y todavía no había sido alertada.
                    if (dias > PLAZO_DIAS
                            && alertados.add(reservacion.id())) {
                        publicar(
                                TipoEvento.ALERTA,
                                "Reservación " + reservacion.id()
                                        + " excedió el plazo de "
                                        + PLAZO_DIAS + " días"
                        );
                        acciones++;
                    }
                }
                // Las reservaciones terminadas no requieren ninguna acción.
                case CERRADA, CANCELADA -> {
                }
            }
        }

        //System.out.println("DEBUG: acciones del monitor = " + acciones);
        return acciones;
        //throw new UnsupportedOperationException("TODO E12");
    }

    public synchronized void iniciarMonitor(long periodoMs) {
        // TODO E12: Crear el monitor interno (new MonitorSalidas(periodoMs)) e iniciarlo con un nombre de hilo descriptivo.
        // Evitar crear otro monitor si ya hay uno activo.
        if (monitor != null && monitor.activa()) {
            throw new IllegalStateException("El monitor ya está activo");
        }
        // Crear el monitor con el tiempo de pausa entre revisiones.
        monitor = new MonitorSalidas(periodoMs);
        // Arrancar su hilo con un nombre que permita identificarlo.
        monitor.iniciar("MonitorSalidas-Hotel");
       // throw new UnsupportedOperationException("TODO E12");
    }

    public void detenerMonitor() throws InterruptedException {
        MonitorSalidas m;
        synchronized (this) {
            m = monitor;
        }
        if (m != null) {
            m.detener();
        }
    }

    public synchronized boolean monitorActivo() {
        return monitor != null && monitor.activa();
    }

    /**
     * Clase interna: ES UNA {@link TareaPeriodica} y TIENE ACCESO a la envolvente.
     */
    public final class MonitorSalidas extends TareaPeriodica {

        private MonitorSalidas(long periodoMs) {
            super(periodoMs);
        }

        @Override
        protected void ejecutarCiclo() {
            // TODO E12: Delegar en la envolvente: Hotel.this.revisar().
            Hotel.this.revisar();
            //throw new UnsupportedOperationException("TODO E12");
        }
    }

    // ------------------------------------------------------------------ respaldo

    public void exportarRespaldo(Path archivo) throws IOException {
        Respaldo.exportar(archivo, catalogo, huespeds(), registros());
    }

    /** Detiene el monitor (si está activo). Invocar al cerrar la aplicación. */
    @Override
    public void close() {
        try {
            detenerMonitor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
