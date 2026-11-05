package mx.tecnm.orizaba.tp.hotel.persistencia;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrato genérico de repositorio (patrón Repository).
 * Contiene un <strong>tipo miembro de interfaz</strong> ({@link MapeadorFila}), implícitamente
 * {@code public static} (véase {@code java_3_tipos_internos/t2_jerarquias/J05_Tipos_Miembro_Interfaz}).
 *
 * @param <T> entidad
 * @param <K> clave
 */
public interface Repositorio<T, K> {

    /** Guarda o actualiza una entidad. */
    void guardar(T entidad);

    /** @return entidad encontrada, si existe */
    Optional<T> buscar(K clave);

    /** @return lista de todas las entidades almacenadas */
    List<T> todos();

    /** @return true si la entidad fue eliminada */
    boolean eliminar(K clave);

    /**
     * Comprueba si existe una entidad con la clave indicada.
     *
     * @param clave identificador de la entidad
     * @return true si la entidad existe
     */
    default boolean existe(K clave) {
        return buscar(clave).isPresent();
    }

    /**
     * Guarda todas las entidades de una colección.
     *
     * @param entidades entidades que se desean guardar
     */
    default void guardarTodos(Iterable<? extends T> entidades) {
        entidades.forEach(this::guardar);
    }

    /** Convierte la fila actual de un {@link ResultSet} en un objeto. */
    @FunctionalInterface
    interface MapeadorFila<R> {
        /**
         * Convierte una fila SQL en un objeto.
         *
         * @param rs fila del resultado SQL
         * @return objeto reconstruido
         * @throws SQLException si ocurre un error SQL
         */
        R mapear(ResultSet rs) throws SQLException;
    }
}
