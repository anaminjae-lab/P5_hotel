package mx.tecnm.orizaba.tp.hotel.funcional;

import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;

/**
 * Fábrica genérica acotada, diseñada para recibir <strong>referencias a constructor</strong>:
 * {@code FabricaElemento<Habitacion.Sencilla, Integer> f = Habitacion.Sencilla::new;}
 * (véase {@code java_5_lambdas/c_referencias_metodos/Referencias4}).
 *
 * @param <E> subtipo concreto de Habitacion
 * @param <A> tipo del atributo específico del subtipo
 */
@FunctionalInterface
public interface FabricaElemento<E extends Habitacion, A> {

    E crear(String nombre, double precioBase, A atributo);
}
