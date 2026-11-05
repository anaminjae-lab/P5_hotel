package mx.tecnm.orizaba.tp.hotel.modelo;

/**
 * Interfaz de capacidad (clasificación múltiple, véase {@code java_4_modelo/t4_jerarquia/t1_clasificacion}).
 * Declara un método por defecto {@code descripcionCorta()} que colisiona deliberadamente con el de la otra
 * capacidad; quien implemente ambas debe resolver la colisión con {@code Interfaz.super.descripcionCorta()}
 * (véase {@code java_5_lambdas/a_interfaces_default/Default3}).
 */
public interface ConVista {

    /**
     * Obtiene la vista que ofrece la habitación.
     *
     * @return descripción de la vista
     */
    String vista();

    /**
     * Devuelve una descripción breve de la vista.
     *
     * @return texto con la vista de la habitación
     */
    default String descripcionCorta() {
        // TODO E05: Devolver "Vista: " concatenado con el valor de vista().
        return "Vista: " + vista();
        //throw new UnsupportedOperationException("TODO E05");
    }
}
