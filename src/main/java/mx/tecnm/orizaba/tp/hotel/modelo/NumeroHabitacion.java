package mx.tecnm.orizaba.tp.hotel.modelo;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Objeto valor inmutable que identifica a un habitación (formato {@code ABC-0001}).
 * <ul>
 *   <li>Encapsulamiento: {@code record} con constructor compacto que valida invariantes.</li>
 *   <li>Proceso: bloque de inicialización estática (véase {@code java_2_proceso/Proceso03}).</li>
 *   <li>Concurrencia: secuencias atómicas seguras entre hilos.</li>
 * </ul>
 */
public record NumeroHabitacion(String prefijo, int numero) implements Comparable<NumeroHabitacion>, Serializable {

    private static final Map<String, AtomicInteger> SECUENCIAS;

    static {
        // Inicialización estática: se ejecuta una sola vez, al primer uso activo de la clase.
        SECUENCIAS = new ConcurrentHashMap<>();
    }
    /**
     * Valida los componentes de la clave de habitación.
     *
     * @throws IllegalArgumentException si el prefijo no tiene
     *         tres letras mayúsculas o el número no es positivo
     */
    public NumeroHabitacion {
        if(prefijo == null ||!prefijo.matches("[A-Z]{3}") || numero<=0 ) { //AGREGADO
            throw new IllegalArgumentException("NO VALIDO");//AGREGADO
        }
        // TODO E03: Validar que el prefijo tenga exactamente 3 letras mayúsculas y que el número sea positivo (IllegalArgumentException).
        //throw new UnsupportedOperationException("TODO E03");
    }

    /**
     * Genera la siguiente clave disponible para un prefijo.
     *
     * @param prefijo tres letras mayúsculas del tipo de habitación
     * @return nueva clave con número consecutivo
     */
    public static NumeroHabitacion siguiente(String prefijo) {
        AtomicInteger secuencia = SECUENCIAS.computeIfAbsent(prefijo, p -> new AtomicInteger(0));

        int numero = secuencia.incrementAndGet();
        return new NumeroHabitacion(prefijo, numero);

        // TODO E03: Obtener (o crear) la secuencia del prefijo con computeIfAbsent e incrementarla atómicamente.
        //throw new UnsupportedOperationException("TODO E03");
    }

    /**
     * Actualiza la secuencia para evitar repetir claves existentes.
     *
     * @param clave clave que debe registrarse
     */
    public static void registrar(NumeroHabitacion clave) {
        String prefijo = clave.prefijo();
        int numero = clave.numero();

        AtomicInteger secuencia = SECUENCIAS.computeIfAbsent(prefijo, p -> new AtomicInteger(0));
        secuencia.accumulateAndGet(numero, Math::max);

        // TODO E03: Actualizar la secuencia del prefijo con accumulateAndGet(numero, Math::max).
        //throw new UnsupportedOperationException("TODO E03");
    }

    /**
     * Convierte una cadena de texto en una clave de habitación.
     *
     * @param texto clave con formato ABC-0001
     * @return clave de habitación
     * @throws IllegalArgumentException si el formato es inválido
     */
    public static NumeroHabitacion parse(String texto) {
        if (texto == null || !texto.matches("[A-Z]{3}-\\d{4,}")) {
            throw new IllegalArgumentException("Formato de habitación inválido");
        }
        String[] partes = texto.split("-");

        String prefijo = partes[0];
        int numero = Integer.parseInt(partes[1]);
        return new NumeroHabitacion(prefijo, numero);

        // TODO E03: Validar el formato con la expresión [A-Z]{3}-\d{4,} y construir la clave.


       // throw new UnsupportedOperationException("TODO E03");
    }
    /**
     * Compara dos claves, primero por prefijo y después por número.
     *
     * @param otra clave con la que se compara
     * @return valor negativo, cero o positivo según el orden
     */
    @Override
    public int compareTo(NumeroHabitacion otra) {
        int comparacionPrefijos = prefijo.compareTo(otra.prefijo());
        if (comparacionPrefijos != 0) {
            return comparacionPrefijos;
        }
        return Integer.compare(numero, otra.numero());

        // TODO E03: Ordenar primero por prefijo y después por número.
        //throw new UnsupportedOperationException("TODO E03");
    }
    /**
     * Devuelve la clave con el formato ABC-0001.
     *
     * @return representación textual de la clave
     */
    @Override
    public String toString() {
        return String.format("%s-%04d", prefijo, numero);
    }
}
