package mx.tecnm.orizaba.tp.hotel.modelo;

/**
 * Nivel del huesped; cada constante encapsula su etiqueta y su descuento.
 * <p>Tipificación fuerte: un {@code enum} es un tipo cerrado con estado y comportamiento.</p>
 */
public enum Nivel {
    ESTANDAR("Estándar", 0.0),
    FRECUENTE("Frecuente", 0.1),
    CONVENIO("Convenio", 0.2);

    private final String etiqueta;
    private final double descuento;

    Nivel(String etiqueta, double descuento) {
        this.etiqueta = etiqueta;
        this.descuento = descuento;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public double descuento() {
        return descuento;
    }

    /** Aplica el descuento del nivel a un monto, redondeando a 2 decimales. */
    public double aplicar(double monto) {
        // TODO E06: Restar el porcentaje de descuento al monto y redondear a 2 decimales.
        throw new UnsupportedOperationException("TODO E06");
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
