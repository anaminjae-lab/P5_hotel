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
        return switch (e) {
            // Si es Sencilla, obtiene sus camas y las convierte a texto.
            case Habitacion.Sencilla x -> Integer.toString(x.camas());
            // Si es Doble, obtiene su piso y lo convierte a texto.
            case Habitacion.Doble x -> Integer.toString(x.piso());
            // Si es Suite, obtiene sus metros y los convierte a texto.
            case Habitacion.Suite x -> Integer.toString(x.metros());
        };

        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public void guardar(Habitacion e) {
        // TODO E13: INSERT ... ON CONFLICT(clave) DO UPDATE ... con PreparedStatement.
        String sql = """
            INSERT INTO habitacion
                (clave, tipo, nombre, precio_base, atributo)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(clave) DO UPDATE SET
                tipo = excluded.tipo,
                nombre = excluded.nombre,
                precio_base = excluded.precio_base,
                atributo = excluded.atributo
            """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {

            // Asigna los valores a los cinco signos ?, en orden.
            ps.setString(1, e.clave().toString());
            ps.setString(2, e.tipo());
            ps.setString(3, e.nombre());
            ps.setDouble(4, e.precioBase());
            ps.setString(5, atributoDe(e));
            // Ejecuta la inserción o actualización.
            ps.executeUpdate();
        } catch (SQLException ex) {
            // Conserva el error SQL como causa.
            throw new PersistenciaException(
                    "Error al guardar la habitación", ex);
        }

       // throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Habitacion> buscar(NumeroHabitacion clave) {
        // TODO E13: SELECT por clave; mapear con mapeador si hay fila.
        // Selecciona la fila cuya clave coincida con el parámetro.
        String sql = "SELECT * FROM habitacion WHERE clave = ?";

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            ps.setString(1, clave.toString());
            try (ResultSet rs = ps.executeQuery()) {
                // Avanza a la primera fila, si existe.
                if (rs.next()) {
                    // Convierte la fila en una habitación
                    // y la devuelve dentro de un Optional.
                    return Optional.of(mapeador.mapear(rs));
                }
                // La consulta no encontró ninguna habitación.
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al buscar la habitación", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Habitacion> todos() {
        // TODO E13: SELECT ordenado por clave; mapear cada fila.
        List<Habitacion> habitaciones = new ArrayList<>();

        String sql = "SELECT * FROM habitacion ORDER BY clave";

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                habitaciones.add(mapeador.mapear(rs));
            }
            return habitaciones;

        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al consultar las habitaciones", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(NumeroHabitacion clave) {
        // TODO E13: DELETE por clave; devolver si se afectó alguna fila.
        String sql = "DELETE FROM habitacion WHERE clave = ?";

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            ps.setString(1, clave.toString());
            int filasEliminadas = ps.executeUpdate();
            return filasEliminadas > 0;
        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al eliminar la habitación", ex);
        }

        //throw new UnsupportedOperationException("TODO E13");
    }
}
