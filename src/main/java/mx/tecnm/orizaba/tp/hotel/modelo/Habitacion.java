package mx.tecnm.orizaba.tp.hotel.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase abstracta <strong>sellada</strong> que factoriza el estado y el comportamiento común de todo
 * habitación. Sus tres subtipos se declaran como clases anidadas estáticas finales
 * (véase {@code java_4_modelo/t1_abstraccion/Abstraccion04_anidadas}).
 * <ul>
 *   <li>Abstracción + Jerarquía: patrón <em>Template Method</em> ({@link #tarifa(int)} es final y delega en
 *       {@link #calcularTarifa(int)}).</li>
 *   <li>Proceso: bloque estático, bloque de instancia y encadenamiento {@code this(...)}/{@code super(...)}
 *       (véase {@code java_2_proceso/Proceso03}, {@code Proceso09}).</li>
 *   <li>Persistencia: {@code Serializable} con un campo {@code transient}
 *       (véase {@code java_4_modelo/t7_persistencia/a_serializacion/NoSerializar_09}).</li>
 * </ul>
 */
public abstract sealed class Habitacion implements Reservable, Serializable
        permits Habitacion.Sencilla, Habitacion.Doble, Habitacion.Suite {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final AtomicInteger INSTANCIAS_CREADAS;

    static {
        INSTANCIAS_CREADAS = new AtomicInteger();
    }

    private final NumeroHabitacion clave;
    private String nombre;
    private double precioBase;
    private final LocalDateTime creadoEn;
    /** No se serializa: tras recuperar un respaldo vuelve a 0. */
    private transient int cotizaciones;

    {
        // Bloque de inicialización de instancia: se ejecuta antes del cuerpo de cualquier constructor.
        creadoEn = LocalDateTime.now();
        INSTANCIAS_CREADAS.incrementAndGet();
    }

    /** Constructor para objetos nuevos: genera la clave y encadena con {@code this(...)}. */
    protected Habitacion(String prefijo, String nombre, double precioBase) {
        this(NumeroHabitacion.siguiente(prefijo), nombre, precioBase);
    }

    /** Constructor para objetos reconstruidos (persistencia) con clave conocida. */
    protected Habitacion(NumeroHabitacion clave, String nombre, double precioBase) {
        this.clave = Objects.requireNonNull(
                clave, "La clave no puede ser nula");
        NumeroHabitacion.registrar(clave);
        setNombre(nombre);
        setPrecioBase(precioBase);

        // TODO E04: Asignar la clave (no nula), registrarla en NumeroHabitacion.registrar y validar nombre y precio mediante los setters.
        //throw new UnsupportedOperationException("TODO E04");
    }

    public static int instanciasCreadas() {
        return INSTANCIAS_CREADAS.get();
    }

    @Override
    public final NumeroHabitacion clave() {
        return clave;
    }

    @Override
    public final String nombre() {
        return nombre;
    }

    @Override
    public final double precioBase() {
        return precioBase;
    }

    public final LocalDateTime creadoEn() {
        return creadoEn;
    }

    public final int cotizaciones() {
        return cotizaciones;
    }

    public final void setNombre(String nombre) {
        // TODO E04: Rechazar nombres nulos o en blanco (ReglaNegocioException); guardar el nombre sin espacios extremos.
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.strip();
        //throw new UnsupportedOperationException("TODO E04");
    }

    public final void setPrecioBase(double precioBase) {
        // TODO E04: Rechazar precios negativos, NaN o infinitos (ReglaNegocioException).
        if (precioBase < 0 || !Double.isFinite(precioBase)) {
            throw new ReglaNegocioException(
                    "El precio base debe ser mayor o igual a cero"
            );
        }
        // Guarda el precio válido en el campo de esta habitación.
        this.precioBase = precioBase;

        //throw new UnsupportedOperationException("TODO E04");
    }

    /** Método plantilla: valida, contabiliza y redondea; la variación la aporta cada subtipo. */
    @Override
    public final double tarifa(int unidades) {
        // TODO E04: Validar unidades > 0 (ReglaNegocioException), incrementar cotizaciones y devolver calcularTarifa redondeada a 2 decimales.
        if (unidades <= 0) {
            throw new ReglaNegocioException(
                    "El número de noches debe ser mayor que cero"
            );
        }
        cotizaciones++;
        double importe = calcularTarifa(unidades);
        // Redondea el importe a dos decimales y lo devuelve.
        return Math.round(importe * 100.0) / 100.0;


       // throw new UnsupportedOperationException("TODO E04");
    }

    /** Punto de variación del Template Method. */
    protected abstract double calcularTarifa(int unidades);

    /** Copia con clave nueva; los subtipos la redefinen con <strong>retorno covariante</strong>. */
    public abstract Habitacion copiar();

    /** Nombre corto del subtipo (útil para la UI y la persistencia). */
    public final String tipo() {
        return getClass().getSimpleName();
    }

    @Override
    public final boolean equals(Object o) {
        return this == o || (o instanceof Habitacion otro && clave.equals(otro.clave));
    }

    @Override
    public final int hashCode() {
        return clave.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[%s, %s, $%.2f]", tipo(), clave, nombre, precioBase);
    }

    /**
     * Sencilla: tarifa lineal por noche.
     * <p>Subtipo de Habitacion declarado como clase anidada estática final; implementa ConVista.</p>
     */
    public static final class Sencilla extends Habitacion implements ConVista {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "SEN";

        private final int camas;

        public Sencilla(String nombre, double precioBase, int camas) {
            super(PREFIJO, nombre, precioBase);
            this.camas = validar(camas);
        }

        public Sencilla(NumeroHabitacion clave, String nombre, double precioBase, int camas) {
            super(clave, nombre, precioBase);
            this.camas = validar(camas);
        }

        private static int validar(int camas) {
            // TODO E05: Validar: camas >= 1 && camas <= 2 (si no se cumple: ReglaNegocioException "Una habitación sencilla tiene 1 o 2 camas").
            if (camas < 1 || camas > 2) {
                throw new ReglaNegocioException(
                        "Una habitación sencilla tiene 1 o 2 camas"
                );
            }
            return camas;
            //throw new UnsupportedOperationException("TODO E05");
        }

        public int camas() {
            return camas;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Sencilla: tarifa lineal por noche. Fórmula: precioBase() * unidades
            return precioBase() * unidades;
            //throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Sencilla, no Habitacion. */
        @Override
        public Sencilla copiar() {
            // TODO E05: Crear un nuevo Sencilla con el mismo nombre, precio y atributo (clave nueva).
            return new Sencilla(nombre(), precioBase(), camas);
            //throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String vista() {
            return "Jardín";
        }

        @Override
        public String toString() {
            return super.toString() + " {camas=" + camas + "}";
        }
    }

    /**
     * Doble: 20 % adicional en pisos superiores al quinto.
     * <p>Subtipo de Habitacion declarado como clase anidada estática final; implementa Accesible.</p>
     */
    public static final class Doble extends Habitacion implements Accesible {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "DOB";

        private final int piso;

        public Doble(String nombre, double precioBase, int piso) {
            super(PREFIJO, nombre, precioBase);
            this.piso = validar(piso);
        }

        public Doble(NumeroHabitacion clave, String nombre, double precioBase, int piso) {
            super(clave, nombre, precioBase);
            this.piso = validar(piso);
        }

        private static int validar(int piso) {
            // TODO E05: Validar: piso >= 1 && piso <= 10 (si no se cumple: ReglaNegocioException "El piso debe estar entre 1 y 10").

            if (piso < 1 || piso > 10) {
                throw new ReglaNegocioException(
                        "El piso debe estar entre 1 y 10"
                );
            }
            return piso;

            //throw new UnsupportedOperationException("TODO E05");
        }

        public int piso() {
            return piso;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            // TODO E05: Doble: 20 % adicional en pisos superiores al quinto. Fórmula: precioBase() * unidades * (piso > 5 ? 1.2 : 1.0)

            double importe = precioBase() * unidades;
            if (piso > 5) {
                importe = importe * 1.2;
            }
            return importe;
           // throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Doble, no Habitacion. */
        @Override
        public Doble copiar() {
            // TODO E05: Crear un nuevo Doble con el mismo nombre, precio y atributo (clave nueva).

            return new Doble(nombre(), precioBase(), piso());
           // throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String adaptaciones() {
            return piso == 1 ? "Planta baja accesible" : "Acceso por elevador";
        }

        @Override
        public String toString() {
            return super.toString() + " {piso=" + piso + "}";
        }
    }

    /**
     * Suite: 10 % de descuento a partir de 5 noches.
     * <p>Subtipo de Habitacion declarado como clase anidada estática final; implementa ConVista, Accesible.</p>
     */
    public static final class Suite extends Habitacion implements ConVista, Accesible {
        @Serial
        private static final long serialVersionUID = 1L;

        public static final String PREFIJO = "SUI";

        private final int metros;

        public Suite(String nombre, double precioBase, int metros) {
            super(PREFIJO, nombre, precioBase);
            this.metros = validar(metros);
        }

        public Suite(NumeroHabitacion clave, String nombre, double precioBase, int metros) {
            super(clave, nombre, precioBase);
            this.metros = validar(metros);
        }

        private static int validar(int metros) {
            // TODO E05: Validar: metros > 0 (si no se cumple: ReglaNegocioException "La superficie debe ser positiva").
            if (metros <= 0) {
                throw new ReglaNegocioException(
                        "La superficie debe ser positiva");
                // throw new UnsupportedOperationException("TODO E05");
            }
            return metros;
        }

        public int metros() {
            return metros;
        }

        @Override
        protected double calcularTarifa(int unidades) {
            double importe = precioBase() * unidades;
            if (unidades >= 5) {
                importe = importe * 0.9;
            }
            return importe;
            // TODO E05: Suite: 10 % de descuento a partir de 5 noches. Fórmula: unidades >= 5 ? precioBase() * unidades * 0.9 : precioBase() * unidades
            //throw new UnsupportedOperationException("TODO E05");
        }

        /** Retorno covariante: devuelve Suite, no Habitacion. */
        @Override
        public Suite copiar() {
            // TODO E05: Crear un nuevo Suite con el mismo nombre, precio y atributo (clave nueva).
            return new Suite(nombre(), precioBase(), metros());
            //throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String vista() {
            return "Pico de Orizaba";
        }

        @Override
        public String adaptaciones() {
            return "Accesibilidad total";
        }

        /** Resolución explícita de la colisión entre métodos por defecto. */
        @Override
        public String descripcionCorta() {
            // TODO E05: Combinar ConVista.super.descripcionCorta() y Accesible.super.descripcionCorta() separados por " | ".
            return ConVista.super.descripcionCorta() + " | "
                    + Accesible.super.descripcionCorta();

           // throw new UnsupportedOperationException("TODO E05");
        }

        @Override
        public String toString() {
            return super.toString() + " {metros cuadrados=" + metros + "}";
        }
    }
}
