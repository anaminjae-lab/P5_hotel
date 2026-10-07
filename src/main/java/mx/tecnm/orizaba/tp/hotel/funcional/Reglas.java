package mx.tecnm.orizaba.tp.hotel.funcional;

import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;

/**
 * Biblioteca de reglas reutilizables (clase utilitaria no instanciable).
 */
public final class Reglas {

    private Reglas() {
        throw new AssertionError("No instanciable");
    }

    public static Regla<Habitacion> precioHasta(double maximo) {
        // TODO E11: Lambda que acepta los elementos cuyo precioBase() sea menor o igual que maximo.
        throw new UnsupportedOperationException("TODO E11");
    }

    public static Regla<Habitacion> nombreContiene(String texto) {
        // TODO E11: Lambda insensible a mayúsculas/minúsculas sobre nombre().
        throw new UnsupportedOperationException("TODO E11");
    }

    /** Elementos que implementan una capacidad (p. ej. {@code ConVista.class}). */
    public static Regla<Habitacion> conCapacidad(Class<?> capacidad) {
        // TODO E11: Referencia a método de instancia particular: capacidad::isInstance.
        throw new UnsupportedOperationException("TODO E11");
    }
}
