package mx.tecnm.orizaba.tp.hotel.persistencia;

import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Inventario;
import mx.tecnm.orizaba.tp.hotel.modelo.Reservacion;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Respaldo binario mediante serialización de objetos (complementa a SQLite).
 * Escribe y lee en el mismo orden (véase {@code java_4_modelo/t7_persistencia/a_serializacion/Gusano_07}).
 */
public final class Respaldo {

    public static final String ENCABEZADO = "Respaldo Hotel v1";

    private Respaldo() {
    }

    /** Contenido recuperado de un respaldo. */
    public record Contenido(String encabezado, LocalDateTime fecha, Inventario catalogo,
                            List<Huesped> huespeds, List<Reservacion> registros) {
    }

    public static void exportar(Path archivo, Inventario catalogo, Collection<Huesped> huespeds,
                                Collection<Reservacion> registros) throws IOException {

        try (ObjectOutputStream salida = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(archivo)))) {
            // Guarda el encabezado para identificar el respaldo.
            salida.writeUTF(ENCABEZADO);
            // Guarda la fecha y hora de creación.
            salida.writeObject(LocalDateTime.now());
            // Guarda el catálogo de habitaciones.
            salida.writeObject(catalogo);
            // Guarda una lista con los huéspedes.
            salida.writeObject(new ArrayList<>(huespeds));
            // Guarda una lista con las reservaciones.
            salida.writeObject(new ArrayList<>(registros));
        }
        // TODO E14: ObjectOutputStream(BufferedOutputStream(Files.newOutputStream)): encabezado, fecha, catálogo, listas.

        //throw new UnsupportedOperationException("TODO E14");
    }

    @SuppressWarnings("unchecked")
    public static Contenido importar(Path archivo) throws IOException, ClassNotFoundException {
        // TODO E14: Leer en el mismo orden; validar el encabezado (IOException si no coincide).
        try (ObjectInputStream entrada = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {
            // Recupera el encabezado.
            String encabezado = entrada.readUTF();
            // Comprueba que el archivo sea un respaldo válido.
            if (!ENCABEZADO.equals(encabezado)) {
                throw new IOException("Encabezado de respaldo inválido");
            }
            // Recupera la fecha de creación.
            LocalDateTime fecha = (LocalDateTime) entrada.readObject();
            // Recupera el catálogo.
            Inventario catalogo = (Inventario) entrada.readObject();
            // Recupera los huéspedes.
            List<Huesped> huespedes =
                    (List<Huesped>) entrada.readObject();
            // Recupera las reservaciones.
            List<Reservacion> registros =
                    (List<Reservacion>) entrada.readObject();
            // Devuelve toda la información recuperada.
            return new Contenido(
                    encabezado,
                    fecha,
                    catalogo,
                    huespedes,
                    registros
            );
        }


        //throw new UnsupportedOperationException("TODO E14");
    }
}
