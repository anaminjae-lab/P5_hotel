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
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Reservacion> buscar(Integer id) {
        // TODO E13: Leer el encabezado, resolver el actor y los elementos con sus repositorios, agregar líneas y restaurar el estado.
        throw new UnsupportedOperationException("TODO E13");
    }

    private Reservacion reconstruir(Connection c, ResultSet rs) throws SQLException {
        // TODO E13: Construir el objeto en estado ABIERTA, agregar las líneas y al final restaurarEstado(estado leído).
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Reservacion> todos() {
        // TODO E13: Recorrer los ids ordenados y reconstruir cada registro.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE del encabezado (las líneas se eliminan en cascada).
        throw new UnsupportedOperationException("TODO E13");
    }
}
