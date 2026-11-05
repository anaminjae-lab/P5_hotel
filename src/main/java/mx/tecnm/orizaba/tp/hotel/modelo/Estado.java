package mx.tecnm.orizaba.tp.hotel.modelo;

/**
 * Ciclo de vida de un(a) reservación: máquina de estados finita.
 * <pre>ABIERTA → EN_CURSO → CERRADA;  ABIERTA|EN_CURSO → CANCELADA</pre>
 */
public enum Estado {
    ABIERTA("Solicitada"), EN_CURSO("Hospedado"), CERRADA("Finalizada"), CANCELADA("Cancelada");

    private final String etiqueta;

    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** @return nombre descriptivo del estado */
    public String etiqueta() {
        return etiqueta;
    }

    /** Indica si la transición hacia {@code destino} es válida (switch como expresión). */
    public boolean permiteTransicionA(Estado destino) {
        // TODO E07: Implementar con switch-expresión las transiciones del diagrama; los estados terminales no permiten ninguna.
        return switch (this) {
            case ABIERTA ->
                    destino == EN_CURSO || destino == CANCELADA;
            case EN_CURSO ->
                    destino == CERRADA || destino == CANCELADA;
            case CERRADA, CANCELADA -> false;
        };


        //throw new UnsupportedOperationException("TODO E07");
    }

    /**
     * Indica si el estado es final.
     *
     * @return true si la reservación está cerrada o cancelada
     */
    public boolean esTerminal() {
        return this == CERRADA || this == CANCELADA;
    }

    /**
     * Devuelve la etiqueta del estado.
     *
     * @return nombre descriptivo del estado
     */
    @Override
    public String toString() {
        return etiqueta;
    }
}
