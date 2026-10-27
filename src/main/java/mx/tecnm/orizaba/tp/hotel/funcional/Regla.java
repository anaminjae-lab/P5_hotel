package mx.tecnm.orizaba.tp.hotel.funcional;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Interfaz funcional propia (un solo método abstracto) con métodos por defecto de composición
 * (véase {@code java_5_lambdas/b_interfaces_funcionales} y {@code d_orden_superior/Superior2}).
 *
 * @param <T> tipo evaluado
 */
@FunctionalInterface
public interface Regla<T> {

    boolean evaluar(T t);

    default Regla<T> y(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) && otra.evaluar(t) (validar otra no nula).
        // Comprobar que recibimos una segunda regla.
        Objects.requireNonNull(otra, "La otra regla no puede ser nula");
        return t -> evaluar(t) && otra.evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }

    default Regla<T> o(Regla<? super T> otra) {
        // TODO E11: Devolver la lambda t -> evaluar(t) || otra.evaluar(t).
        Objects.requireNonNull(otra, "La otra regla no puede ser nula");
        // Devolver una nueva regla que acepta el objeto
        // cuando cumple la regla actual o la segunda regla.
        return t -> evaluar(t) || otra.evaluar(t);
       // throw new UnsupportedOperationException("TODO E11");
    }

    default Regla<T> negar() {
        // TODO E11: Devolver la lambda t -> !evaluar(t).
        // Crear una regla que devuelva lo contrario de la regla actual.
        return t -> !evaluar(t);
        //throw new UnsupportedOperationException("TODO E11");
    }

    /** Adaptador hacia {@link Predicate} mediante referencia a método de instancia particular. */
    default Predicate<T> comoPredicado() {
        return this::evaluar;
    }

    static <T> Regla<T> todas() {
        return t -> true;
    }

    /** Adaptador desde {@link Predicate}. */
    static <T> Regla<T> de(Predicate<? super T> p) {
        return p::test;
    }
}
