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

    public RepositorioHuespedSQLite(ConexionSQLite bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Huesped a) {
        // TODO E13: Upsert por id con PreparedStatement.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public Optional<Huesped> buscar(Integer id) {
        // TODO E13: SELECT por id.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public List<Huesped> todos() {
        // TODO E13: SELECT ordenado por id.
        throw new UnsupportedOperationException("TODO E13");
    }

    @Override
    public boolean eliminar(Integer id) {
        // TODO E13: DELETE por id.
        throw new UnsupportedOperationException("TODO E13");
    }
}
