package mx.tecnm.orizaba.tp.hotel.eventos;

/**
 * Observador funcional de eventos. La UI se suscribe con una lambda, una referencia a método
 * o una clase anónima.
 * <p><strong>Importante:</strong> el monitor notifica desde un hilo de fondo; la UI debe
 * reenviar la actualización a su propio hilo ({@code Platform.runLater},
 * {@code SwingUtilities.invokeLater}, SSE/WebSocket, etc.).</p>
 */
@FunctionalInterface
public interface OyenteEvento {

    /**
     * Recibe una notificación del hotel.
     *
     * @param evento evento publicado
     */
    void notificar(Evento evento);

    /**
     * Combina dos oyentes para notificarlos secuencialmente.
     *
     * @param siguiente oyente que recibirá la segunda notificación
     * @return oyente compuesto
     */
    default OyenteEvento luego(OyenteEvento siguiente) {
        // TODO E11: Devolver una lambda que notifique primero a this y después a siguiente.
        return evento -> {

            // Primero notificamos al observador actual.
            this.notificar(evento);

            // Después notificamos al siguiente observador.
            siguiente.notificar(evento);
        };
        //throw new UnsupportedOperationException("TODO E11");
    }
}
