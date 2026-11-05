package mx.tecnm.orizaba.tp.hotel.modelo;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Huesped del sistema. Implementa {@link Externalizable}: el propio objeto decide qué y cómo se
 * escribe (véase {@code java_4_modelo/t7_persistencia/b_externalizacion/Cosas2}); por ello requiere un
 * constructor público sin argumentos.
 */
public class Huesped implements Externalizable {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private int id;
    private String nombre;
    private String correo;
    private Nivel nivel;

    /** Requerido por la externalización: se invoca antes de readExternal. */
    public Huesped() {
    }

    /**
     * Inicializa un huésped validando sus datos.
     *
     * @param id identificador positivo del huésped
     * @param nombre nombre del huésped
     * @param correo correo electrónico
     * @param nivel nivel asignado
     */
    public Huesped(int id, String nombre, String correo, Nivel nivel) {
        // TODO E06: Validar id > 0 y asignar nombre, correo y nivel mediante sus setters.
        if (id <= 0) {
            throw new ReglaNegocioException(
                    "La ID del huésped debe ser mayor que cero"
            );
        }
        this.id = id;
        setNombre(nombre);
        setCorreo(correo);
        setNivel(nivel);

        //throw new UnsupportedOperationException("TODO E06");
    }

    /** Encadenamiento de constructores: nivel por omisión. */
    public Huesped(int id, String nombre, String correo) {
        this(id, nombre, correo, Nivel.values()[0]);
    }

    /** @return identificador del huésped */
    public int id() {
        return id;
    }

    /** @return nombre del huésped */
    public String nombre() {
        return nombre;
    }

    /** @return correo electrónico del huésped */
    public String correo() {
        return correo;
    }

    /** @return nivel del huésped */
    public Nivel nivel() {
        return nivel;
    }

    /**
     * Actualiza el nombre del huésped.
     *
     * @param nombre nombre no nulo ni vacío
     */
    public final void setNombre(String nombre) {
        // TODO E06: Rechazar nombre nulo o en blanco.
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException(
                    "El nombre del huésped no puede estar vacío"
            );
        }
        this.nombre = nombre;
       // throw new UnsupportedOperationException("TODO E06");
    }

    /**
     * Actualiza y normaliza el correo electrónico.
     *
     * @param correo correo con formato válido
     */
    public final void setCorreo(String correo) {
        // TODO E06: Validar el correo con el patrón CORREO y guardarlo en minúsculas.
        if (correo == null || !CORREO.matcher(correo).matches()) {
            throw new ReglaNegocioException(
                    "El correo del huésped no es válido"
            );
        }
        this.correo = correo.toLowerCase(java.util.Locale.ROOT);
        //throw new UnsupportedOperationException("TODO E06");
    }

    /**
     * Asigna el nivel del huésped.
     *
     * @param nivel nivel no nulo
     */
    public final void setNivel(Nivel nivel) {
        this.nivel = Objects.requireNonNull(nivel, "nivel");
    }

    /**
     * Escribe los datos del huésped en una salida de objetos.
     *
     * @param salida destino de los datos
     * @throws IOException si ocurre un error de escritura
     */
    @Override
    public void writeExternal(ObjectOutput salida) throws IOException {
        salida.writeInt(id);
        salida.writeUTF(nombre);
        salida.writeUTF(correo);
        salida.writeUTF(nivel.name());
        // TODO E14: Escribir id, nombre, correo y el nombre del nivel (writeInt / writeUTF).
        //throw new UnsupportedOperationException("TODO E14");
    }

    /**
     * Reconstruye los datos del huésped desde una entrada de objetos.
     *
     * @param entrada origen de los datos
     * @throws IOException si ocurre un error de lectura
     */
    @Override
    public void readExternal(ObjectInput entrada) throws IOException {
        // TODO E14: Leer en el mismo orden en que se escribió y reconstruir el nivel con Nivel.valueOf.
        // Recupera el identificador entero.
        int idLeido = entrada.readInt();
        String nombreLeido = entrada.readUTF();
        String correoLeido = entrada.readUTF();
        Nivel nivelLeido = Nivel.valueOf(entrada.readUTF());
        if (idLeido <= 0) {
            throw new IOException("ID de huésped inválido");
        }
        this.id = idLeido;
        setNombre(nombreLeido);
        setCorreo(correoLeido);
        setNivel(nivelLeido);
        //throw new UnsupportedOperationException("TODO E14");
    }

    /**
     * Compara huéspedes mediante su identificador.
     *
     * @param o objeto que se compara
     * @return true si representan al mismo huésped
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Huesped otro && id == otro.id);
    }

    /**
     * Calcula el código hash del identificador.
     *
     * @return código hash del huésped
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Devuelve una representación textual del huésped.
     *
     * @return descripción del huésped
     */
    @Override
    public String toString() {
        return String.format("%s #%d %s <%s> (%s)", "Huesped", id, nombre, correo, nivel);
    }
}
