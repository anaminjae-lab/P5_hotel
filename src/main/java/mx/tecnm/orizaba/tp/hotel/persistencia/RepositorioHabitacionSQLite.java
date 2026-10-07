package mx.tecnm.orizaba.tp.hotel.persistencia;

import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.NumeroHabitacion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio JDBC de habitaciones (tabla de herencia única: columna {@code tipo}).
 * <ul>
 *   <li>Escritura: {@code switch} con <em>pattern matching</em> exhaustivo sobre la jerarquía sellada.</li>
 *   <li>Lectura: mapa de reconstructores (lambdas) indexado por el nombre del subtipo.</li>
 * </ul>
 */
public class RepositorioHabitacionSQLite implements Repositorio<Habitacion, NumeroHabitacion> {

    /** Interfaz funcional privada anidada. */
    @FunctionalInterface
    private interface Reconstructor {
        Habitacion crear(NumeroHabitacion clave, String nombre, double precio, String atributo);
    }

    private static final Map<String, Reconstructor> RECONSTRUCTORES = Map.of(
            "Sencilla", (c, n, p, a) -> new Habitacion.Sencilla(c, n, p, Integer.parseInt(a)),
            "Doble", (c, n, p, a) -> new Habitacion.Doble(c, n, p, Integer.parseInt(a)),
            "Suite", (c, n, p, a) -> new Habitacion.Suite(c, n, p, Integer.parseInt(a))
    );

    private final ConexionSQLite bd;
    private final MapeadorFila<Habitacion> mapeador = rs -> {
        Reconstructor r = RECONSTRUCTORES.get(rs.getString("tipo"));
        if (r == null) {
            throw new SQLException("Tipo desconocido: " + rs.getString("tipo"));
        }
        return r.crear(NumeroHabitacion.parse(rs.getString("clave")), rs.getString("nombre"),
                rs.getDouble("precio_base"), rs.getString("atributo"));
    };

    public RepositorioHabitacionSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    /** Atributo específico del subtipo, obtenido con pattern matching. */
    static String atributoDe(Habitacion e) {
        // TODO E13: switch (e) { case Habitacion.Sencilla x -> ...; ... } sin default (la jerarquía es sellada).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public void guardar(Habitacion e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Habitacion> buscar(NumeroHabitacion clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Habitacion> todos() {
        // TODO E13: SELECT ordenado por clave; mapear cada fila.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(NumeroHabitacion clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        throw new UnsupportedOperationException("TODO E13");
    }
}
