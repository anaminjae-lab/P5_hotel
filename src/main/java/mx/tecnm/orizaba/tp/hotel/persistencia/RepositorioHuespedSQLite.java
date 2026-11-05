package mx.tecnm.orizaba.tp.hotel.persistencia;

import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Nivel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Repositorio JDBC de huéspedes. */
public class RepositorioHuespedSQLite implements Repositorio<Huesped, Integer> {

    private final ConexionSQLite bd;
    private final MapeadorFila<Huesped> mapeador = rs -> new Huesped(rs.getInt("id"),
            rs.getString("nombre"), rs.getString("correo"), Nivel.valueOf(rs.getString("nivel")));

    /**
     * Construye el repositorio de huéspedes.
     *
     * @param bd conexión SQLite utilizada para las operaciones
     */
    public RepositorioHuespedSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    /**
     * Inserta o actualiza los datos de un huésped en SQLite.
     *
     * @param a huésped que se desea guardar
     */
    @Override
    public void guardar(Huesped a) {
        // TODO E13: Upsert por id con PreparedStatement.
        // Inserta al huésped o actualiza sus datos si el id ya existe.
        String sql = """
            INSERT INTO huesped (id, nombre, correo, nivel)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                nombre = excluded.nombre,
                correo = excluded.correo,
                nivel = excluded.nivel
            """;
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            ps.setInt(1, a.id());
            ps.setString(2, a.nombre());
            ps.setString(3, a.correo());
            ps.setString(4, a.nivel().name());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al guardar el huésped", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    /**
     * Busca un huésped mediante su identificador.
     *
     * @param id identificador del huésped
     * @return Optional con el huésped, si existe
     */
    @Override
    public Optional<Huesped> buscar(Integer id) {
        // TODO E13: SELECT por id.
        // Consulta al huésped que tenga el id indicado.
        String sql = "SELECT * FROM huesped WHERE id = ?";
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapeador.mapear(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al buscar el huésped", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    /**
     * Recupera todos los huéspedes ordenados por identificador.
     *
     * @return lista de huéspedes almacenados
     */
    @Override
    public List<Huesped> todos() {
        // TODO E13: SELECT ordenado por id.
        List<Huesped> huespedes = new ArrayList<>();
        String sql = "SELECT * FROM huesped ORDER BY id";

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                huespedes.add(mapeador.mapear(rs));
            }
            // Si no hay huéspedes, devuelve la lista vacía.
            return huespedes;

        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al consultar los huéspedes", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    /**
     * Elimina un huésped de la base de datos.
     *
     * @param id identificador del huésped
     * @return true si se eliminó el huésped
     */
    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE por id.
        // Elimina únicamente al huésped con el id indicado.
        String sql = "DELETE FROM huesped WHERE id = ?";
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filasEliminadas = ps.executeUpdate();
            return filasEliminadas > 0;
        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al eliminar el huésped", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }
}
