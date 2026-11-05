package mx.tecnm.orizaba.tp.hotel.persistencia;

import java.io.Serial;

/** Envuelve los errores de acceso a datos en una excepción no comprobada del dominio. */
public class PersistenciaException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Construye una excepción de persistencia con su causa original.
     *
     * @param mensaje descripción del error
     * @param causa excepción que originó el problema
     */
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
