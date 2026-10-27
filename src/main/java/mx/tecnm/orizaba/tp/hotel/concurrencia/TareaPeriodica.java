package mx.tecnm.orizaba.tp.hotel.concurrencia;

/**
 * Tarea que se ejecuta periódicamente en su propio hilo.
 * <ul>
 *   <li>Implementa {@link Runnable} en lugar de extender {@link Thread}
 *       (véase {@code java_4_modelo/t6_concurrencia/EjemploHilos}).</li>
 *   <li>Detención cooperativa: bandera {@code volatile} + {@code interrupt()} + {@code join()}.</li>
 *   <li>Template Method: {@link #run()} es final; la variación está en {@link #ejecutarCiclo()}.</li>
 * </ul>
 */
public abstract class TareaPeriodica implements Runnable {

    private final long periodoMs;
    private volatile boolean activa;
    private Thread hilo;
    private volatile long ciclos;

    protected TareaPeriodica(long periodoMs) {
        if (periodoMs <= 0) {
            throw new IllegalArgumentException("Periodo inválido: " + periodoMs);
        }
        this.periodoMs = periodoMs;
    }

    /** Arranca el hilo (demonio) con el nombre indicado. */
    public final synchronized void iniciar(String nombreHilo) {
        // TODO E12: Si ya está activa lanzar IllegalStateException; crear new Thread(this, nombreHilo), marcarlo demonio, activar y start().
        // Impedir que iniciemos otra vez una tarea marcada como activa.
        if (activa) {
            throw new IllegalStateException("La tarea ya está activa");
        }
        // Crear un hilo que ejecutará el run() de este objeto.
        // El segundo argumento establece el nombre del hilo.
        hilo = new Thread(this, nombreHilo);
        // Configurarlo como demonio antes de arrancarlo.
        hilo.setDaemon(true);
        // Permitir que la tarea trabaje cuando comience su run().
        activa = true;
        // Arrancar el nuevo hilo.
        hilo.start();
        //throw new UnsupportedOperationException("TODO E12");
    }

    /** Solicita la detención y espera a que el hilo termine. */
    public final void detener() throws InterruptedException {
        // TODO E12: Bajar la bandera, interrumpir el hilo y esperar con join() (si existe).
        // Solicitar que la tarea deje de repetir su trabajo.
        activa = false;
        // Si existe un hilo, avisarle y esperar a que termine.
        if (hilo != null) {
            // Si está dormido, interrumpir su espera.
            hilo.interrupt();
            // Esperar a que el hilo termine de ejecutar su tarea.
            hilo.join();
        }
        //throw new UnsupportedOperationException("TODO E12");
    }

    public final boolean activa() {
        return activa;
    }

    public final long ciclos() {
        return ciclos;
    }

    @Override
    public final void run() {
        // TODO E12: Mientras activa: ejecutarCiclo(), incrementar ciclos y dormir periodoMs; ante InterruptedException restaurar la interrupción y salir.
        try {
            // Repetir mientras no se haya solicitado la detención.
            while (activa) {
                // Realizar el trabajo definido por la subclase.
                ejecutarCiclo();
                // Registrar que terminó un ciclo de trabajo.
                ciclos++;
                // Pausar este hilo durante el período configurado.
                Thread.sleep(periodoMs);
            }
        } catch (InterruptedException e) {
            // Restaurar la señal de interrupción del hilo actual.
            Thread.currentThread().interrupt();
        } finally {
            // Indicar que la tarea terminó, cualquiera que sea la causa.
            activa = false;
        }
       // throw new UnsupportedOperationException("TODO E12");
    }

    /** Trabajo de un ciclo. */
    protected abstract void ejecutarCiclo();
}
