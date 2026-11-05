package mx.tecnm.orizaba.tp.hotel.eventos;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa un evento inmutable del hotel.
 *
 * @param tipo clasificación del evento
 * @param mensaje descripción del evento
 * @param instante fecha y hora de creación
 */
public record Evento(TipoEvento tipo, String mensaje, LocalDateTime instante) {

    /**
     * Valida los componentes del evento antes de construirlo.
     *
     * @throws NullPointerException si algún componente es null
     */
    public Evento {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(mensaje, "mensaje");
        Objects.requireNonNull(instante, "instante");
    }

    /** Fábrica estática (útil como referencia a método estático). */
    public static Evento de(TipoEvento tipo, String mensaje) {
        return new Evento(tipo, mensaje, LocalDateTime.now());
    }

    /**
     * Devuelve una representación textual del evento.
     *
     * @return texto con el tipo y mensaje del evento
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", tipo, mensaje);
    }
}
