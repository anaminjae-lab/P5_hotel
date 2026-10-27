package mx.tecnm.orizaba.tp.hotel.persistencia;

import mx.tecnm.orizaba.tp.hotel.modelo.Huesped;
import mx.tecnm.orizaba.tp.hotel.modelo.Habitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.NumeroHabitacion;
import mx.tecnm.orizaba.tp.hotel.modelo.Estado;
import mx.tecnm.orizaba.tp.hotel.modelo.Reservacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JDBC de reservaciones y sus líneas. Guardar es <strong>atómico</strong>:
 * encabezado y líneas se escriben en una sola transacción (commit/rollback).
 */
public class RepositorioReservacionSQLite implements Repositorio<Reservacion, Integer> {

    private final ConexionSQLite bd;
    private final Repositorio<Huesped, Integer> huespeds;
    private final Repositorio<Habitacion, NumeroHabitacion> elementos;

    public RepositorioReservacionSQLite(ConexionSQLite bd, Repositorio<Huesped, Integer> huespeds,
                         Repositorio<Habitacion, NumeroHabitacion> elementos) {
        this.bd = bd;
        this.huespeds = huespeds;
        this.elementos = elementos;
    }

    @Override
    public void guardar(Reservacion t) {
        // TODO E13: En bd.enTransaccion: upsert del encabezado, borrar sus líneas e insertarlas en lote (addBatch/executeBatch).
        // Ejecuta las tres etapas dentro de la misma transacción.
        bd.enTransaccion(c -> {
            // 1. Inserta o actualiza el encabezado de la reservación.
            String sqlEncabezado = """
                INSERT INTO reservacion (id, huesped_id, fecha, estado)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    huesped_id = excluded.huesped_id,
                    fecha = excluded.fecha,
                    estado = excluded.estado
                """;
            try (PreparedStatement ps = c.prepareStatement(sqlEncabezado)) {
                ps.setInt(1, t.id());
                ps.setInt(2, t.huesped().id());
                ps.setString(3, t.fecha().toString());
                ps.setString(4, t.estado().name());
                ps.executeUpdate();
            }
            // 2. Borra únicamente las líneas anteriores de esta reservación.
            String sqlBorrar = """
                DELETE FROM reservacion_linea
                WHERE reservacion_id = ?
                """;
            try (PreparedStatement ps = c.prepareStatement(sqlBorrar)) {
                ps.setInt(1, t.id());
                ps.executeUpdate();
            }
            // 3. Prepara la inserción de las líneas actuales.
            String sqlLinea = """
                INSERT INTO reservacion_linea
                    (reservacion_id, clave_habitacion, unidades)
                VALUES (?, ?, ?)
                """;
            try (PreparedStatement ps = c.prepareStatement(sqlLinea)) {
                // t es Iterable: permite recorrer sus líneas con for-each.
                for (Reservacion.Linea linea : t) {
                    ps.setInt(1, t.id());
                    ps.setString(2, linea.elemento().clave().toString());
                    ps.setInt(3, linea.unidades());
                    // Agrega estos parámetros al lote, sin ejecutarlos aún.
                    ps.addBatch();
                }
                // Ejecuta todas las inserciones acumuladas.
                ps.executeBatch();
            }
            return null;
        });
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Reservacion> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        String sql = "SELECT * FROM reservacion WHERE id = ?";
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    return Optional.of(
                            reconstruir(bd.conexion(), rs));
                }
                return Optional.empty();
            }

        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al buscar la reservación", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }

    private Reservacion reconstruir(Connection c, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int idHuesped = rs.getInt("huesped_id");
        LocalDate fecha = LocalDate.parse(rs.getString("fecha"));
        Estado estado = Estado.valueOf(rs.getString("estado"));
        Huesped huesped = huespeds.buscar(idHuesped)
                .orElseThrow(() -> new SQLException(
                        "No existe el huésped con id " + idHuesped));

        // El constructor deja la reservación en estado ABIERTA.
        Reservacion reservacion = new Reservacion(id, huesped, fecha);

        // Consulta las líneas que pertenecen a esta reservación.
        String sql = """
            SELECT clave_habitacion, unidades
            FROM reservacion_linea
            WHERE reservacion_id = ?
            ORDER BY clave_habitacion
            """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rsLineas = ps.executeQuery()) {
                while (rsLineas.next()) {
                    // Convierte la clave guardada como texto a NumeroHabitacion.
                    NumeroHabitacion clave = NumeroHabitacion.parse(
                            rsLineas.getString("clave_habitacion"));

                    // Recupera la habitación con su subtipo correspondiente.
                    Habitacion habitacion = elementos.buscar(clave)
                            .orElseThrow(() -> new SQLException(
                                    "No existe la habitación " + clave));

                    int unidades = rsLineas.getInt("unidades");
                    reservacion.agregar(habitacion, unidades);
                }
            }
        }
        reservacion.restaurarEstado(estado);
        return reservacion;

        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).
       // throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Reservacion> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        List<Integer> ids = new ArrayList<>();

        String sql = "SELECT id FROM reservacion ORDER BY id";

        try (PreparedStatement ps = bd.conexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }

        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al consultar las reservaciones", ex);
        }
        List<Reservacion> reservaciones = new ArrayList<>();

        for (Integer id : ids) {
            Reservacion reservacion = buscar(id)
                    .orElseThrow(() -> new PersistenciaException(
                            "No se encontró la reservación con id " + id,
                            null));

            reservaciones.add(reservacion);
        }

        return reservaciones;
        //throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        String sql = "DELETE FROM reservacion WHERE id = ?";
        try (PreparedStatement ps = bd.conexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            int filasEliminadas = ps.executeUpdate();
            return filasEliminadas > 0;

        } catch (SQLException ex) {
            throw new PersistenciaException(
                    "Error al eliminar la reservación", ex);
        }
        //throw new UnsupportedOperationException("TODO E13");
    }
}
