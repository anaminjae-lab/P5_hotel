package mx.tecnm.orizaba.tp.hotel.modelo;

import mx.tecnm.orizaba.tp.hotel.funcional.Regla;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Inventario: <strong>agregación</strong> de habitaciones (las partes existen con independencia del
 * todo; véase {@code java_4_modelo/t4_jerarquia/t2_agregacion/Jerarquia02}).
 * Reúne en un solo lugar tres variantes de tipos internos:
 * clase interna privada ({@link Iterador}), clase anónima ({@link #porTarifa(int)}) y clase local
 * ({@link #resumen()}).
 */
public class Inventario implements Iterable<Habitacion>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<NumeroHabitacion, Habitacion> elementos = new LinkedHashMap<>();

    /** Constructor con número variable de argumentos. */
    public Inventario(Habitacion... iniciales) {
        agregar(iniciales);
    }

    public final void agregar(Habitacion... nuevos) {
        // TODO E09: Recorrer los argumentos; rechazar nulos y claves duplicadas (ReglaNegocioException); usar putIfAbsent.
        if (nuevos == null) {
            throw new ReglaNegocioException(
                    "El arreglo de habitaciones no puede estar vacio");
        }
        for (Habitacion habitacion : nuevos) {
            if (habitacion == null) {
                throw new ReglaNegocioException(
                        "No se pueden agregar habitaciones vacias");
            }
            NumeroHabitacion clave = habitacion.clave();
            Habitacion anterior = elementos.putIfAbsent(clave, habitacion);
            if (anterior != null) {
                throw new ReglaNegocioException(
                        "Ya existe una habitación con la clave " + clave);
            }
        }

       // throw new UnsupportedOperationException("TODO E09");
    }

    public Optional<Habitacion> buscar(NumeroHabitacion clave) {
        return Optional.ofNullable(elementos.get(clave));
    }

    public boolean retirar(NumeroHabitacion clave) {
        return elementos.remove(clave) != null;
    }

    public int tamanio() {
        return elementos.size();
    }

    /** Filtrado con la interfaz funcional propia {@link Regla}. */
    public List<Habitacion> filtrar(Regla<? super Habitacion> regla) {
        // TODO E09: Stream de los valores filtrado con regla::evaluar y recolectado con toList().
        return elementos.values()       // Obtener las habitaciones del mapa.
                .stream()               // Crear un flujo para procesarlas.
                .filter(regla::evaluar)  // Conservar las que cumplen la regla.
                .toList();
       // throw new UnsupportedOperationException("TODO E09");
    }

    /** Filtrado por subtipo con genéricos acotados: {@code deTipo(Habitacion.Sencilla.class)}. */
    public <E extends Habitacion> List<E> deTipo(Class<E> tipo) {
        // TODO E09: Usar filter(tipo::isInstance) y map(tipo::cast).
        return elementos.values()
                .stream()
                .filter(tipo::isInstance)
                .map(tipo::cast)
                .toList();
       // throw new UnsupportedOperationException("TODO E09");
    }

    public List<Habitacion> ordenados(Comparator<? super Habitacion> criterio) {
        return elementos.values().stream().sorted(criterio).toList();
    }

    /**
     * Comparador implementado con una <strong>clase anónima</strong> que captura la variable
     * {@code unidades} (efectivamente final; véase {@code Parcel09}).
     */
    public static Comparator<Habitacion> porTarifa(final int unidades) {
        // TODO E10: Devolver new Comparator<>() { compare(a, b) → Double.compare(a.tarifa(unidades), b.tarifa(unidades)) }.
        return new Comparator<Habitacion>() {
            @Override
            public int compare(Habitacion a, Habitacion b) {
                // Calcular las tarifas y compararlas de menor a mayor.
                return Double.compare(
                        a.tarifa(unidades),
                        b.tarifa(unidades)
                );
            }
        };

       // throw new UnsupportedOperationException("TODO E10");
    }

    /** Resumen calculado con una <strong>clase local</strong> (véase {@code Parcel05}). */
    public String resumen() {
        // TODO E10: Declarar dentro del método la clase local Acumulador (cuenta, suma, mínimo, máximo) y recorrer el catálogo.
        class Acumulador {
            int cuenta = 0;
            double suma = 0;
            double minimo = Double.POSITIVE_INFINITY;
            double maximo = Double.NEGATIVE_INFINITY;
        }
        Acumulador acumulador = new Acumulador();

        // Recorrer directamente las habitaciones guardadas en el mapa.
        for (Habitacion habitacion : elementos.values()) {

            // Consultar una sola vez el precio de la habitación actual.
            double precio = habitacion.precioBase();

            acumulador.cuenta++;       // Contar una habitación más.
            acumulador.suma += precio; // Agregar su precio a la suma.

            // Conservar el menor y el mayor precio encontrados.
            acumulador.minimo = Math.min(acumulador.minimo, precio);
            acumulador.maximo = Math.max(acumulador.maximo, precio);
        }
        // Sin habitaciones, no hay precios para calcular mínimo y máximo.
        if (acumulador.cuenta == 0) {
            return "0 habitaciones: suma=0.00, mínimo=sin datos, máximo=sin datos";
        }
        // Construir y devolver el texto con los resultados.
        return String.format(
                "%d habitaciones: suma=%.2f, mínimo=%.2f, máximo=%.2f",
                acumulador.cuenta,
                acumulador.suma,
                acumulador.minimo,
                acumulador.maximo
        );
        //throw new UnsupportedOperationException("TODO E10");
    }

    @Override
    public Iterator<Habitacion> iterator() {
        return new Iterador();
    }

    /**
     * Clase interna privada que implementa una interfaz pública: el cliente sólo conoce
     * {@code Iterator<Habitacion>} y la implementación queda oculta (véase {@code Parcel04}).
     * Itera sobre una instantánea, de modo que el catálogo no puede modificarse a través de ella.
     */
    private class Iterador implements Iterator<Habitacion> {
        private final List<Habitacion> instantanea = new ArrayList<>(elementos.values());
        private int cursor;

        @Override
        public boolean hasNext() {
            return cursor < instantanea.size();
        }

        @Override
        public Habitacion next() {
            // TODO E10: Lanzar NoSuchElementException si no hay siguiente; devolver el elemento y avanzar el cursor.
            // Comprobar si todavía queda alguna habitación por entregar.
            if (!hasNext()) {
                throw new NoSuchElementException(
                        "No hay más habitaciones en el inventario");
            }
            // Obtener la habitación ubicada en la posición actual.
            Habitacion habitacion = instantanea.get(cursor);
            // Avanzar a la posición que se utilizará en la siguiente llamada.
            cursor++;
            // Entregar la habitación que acabamos de obtener.
            return habitacion;
            //throw new UnsupportedOperationException("TODO E10");
        }
    }
}
