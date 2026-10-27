package mx.tecnm.orizaba.tp.hotel.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexión JDBC con SQLite y el esquema de la base de datos.
 * <p>Proceso: el bloque estático carga el controlador la primera vez que la clase se inicializa
 * (véase {@code java_2_proceso/Proceso07}: uso activo de una clase).</p>
 */
public final class ConexionSQLite implements AutoCloseable {

    private static final String[] ESQUEMA = {
        """
        CREATE TABLE IF NOT EXISTS habitacion (
            clave        TEXT PRIMARY KEY,
            tipo         TEXT NOT NULL,
            nombre       TEXT NOT NULL,
            precio_base  REAL NOT NULL CHECK (precio_base >= 0),
            atributo     TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS huesped (
            id      INTEGER PRIMARY KEY,
            nombre  TEXT NOT NULL,
            correo  TEXT NOT NULL UNIQUE,
            nivel   TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS reservacion (
            id        INTEGER PRIMARY KEY,
            huesped_id  INTEGER NOT NULL REFERENCES huesped(id),
            fecha     TEXT NOT NULL,
            estado    TEXT NOT NULL
        )""",
        """
        CREATE TABLE IF NOT EXISTS reservacion_linea (
            reservacion_id   INTEGER NOT NULL REFERENCES reservacion(id) ON DELETE CASCADE,
            clave_habitacion TEXT NOT NULL REFERENCES habitacion(clave),
            unidades        INTEGER NOT NULL CHECK (unidades > 0),
            PRIMARY KEY (reservacion_id, clave_habitacion)
        )"""
    };

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el controlador sqlite-jdbc en el classpath");
        }
    }

    private final Connection conexion;

    /** @param rutaArchivo archivo .db (se crea si no existe) */
    public ConexionSQLite(String rutaArchivo) {
        try {
            // Abre la conexión con el archivo SQLite indicado.
            // Si el archivo no existe, SQLite intenta crearlo.
            conexion = DriverManager.getConnection(
                    "jdbc:sqlite:" + rutaArchivo);
            // Crea un objeto para ejecutar instrucciones SQL.
            // El try con recursos cierra stmt automáticamente al terminar.
            try (Statement stmt = conexion.createStatement()) {

                // Activa la comprobación de llaves foráneas
                // para esta conexión.
                stmt.execute("PRAGMA foreign_keys = ON");

                // Recorre las instrucciones que crean las tablas.
                for (String sql : ESQUEMA) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            // Conserva el error SQL original como causa.
            throw new PersistenciaException(
                    "Error al inicializar la base de datos", e);
        }
        // TODO E13: Abrir jdbc:sqlite:<ruta>, activar PRAGMA foreign_keys = ON y crear el esquema.


        //throw new UnsupportedOperationException("TODO E13");
    }

    public Connection conexion() {
        return conexion;
    }

    /**
     * Ejecuta un trabajo dentro de una transacción: confirma si termina bien y revierte ante error.
     */
    public <R> R enTransaccion(TrabajoSQL<R> trabajo) {
        // TODO E13: setAutoCommit(false); ejecutar; commit; ante SQLException rollback y PersistenciaException; finalmente setAutoCommit(true).
        try {
            conexion.setAutoCommit(false);
            R resultado = trabajo.ejecutar(conexion);
            conexion.commit();
            return resultado;
        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException errorRollback) {
                e.addSuppressed(errorRollback);
            }
            throw new PersistenciaException(
                    "Error al ejecutar la transacción", e);

        } finally {
            try {
                conexion.setAutoCommit(true);
            } catch (SQLException e) {
                throw new PersistenciaException(
                        "Error al restaurar la confirmación automática", e);
            }
        }
       // throw new UnsupportedOperationException("TODO E13");
    }

    /** Interfaz funcional miembro: trabajo que puede lanzar {@link SQLException}. */
    @FunctionalInterface
    public interface TrabajoSQL<R> {
        R ejecutar(Connection c) throws SQLException;
    }

    @Override
    public void close() {
        try {
            conexion.close();
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cerrar la conexión", e);
        }
    }
}
