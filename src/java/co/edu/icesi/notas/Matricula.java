package co.edu.icesi.notas;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import co.edu.icesi.email.Correo;
import co.edu.icesi.notas.control.ControlRecursos;

/**
 * Representa la relaci�n entre un alumno y una actividad del curso. En esta
 * relaci�n se encuentran atributos como la nota del estudiante. OJO: Esta clase
 * NO tiene una correspondencia l�gica con la tabla TNTP_MATRICULAS del modelo
 * entidad relaci�n de la aplicaci�n.
 * 
 * @author lmdiaz, mzapata
 */
public class Matricula implements Serializable {

    private Curso curso;
    private Alumno alumno;
    private double calificacion;
    private double copiaCalificacion = -1;
    private boolean existeBd;
    private String comentario;
    private Actividad actividad;

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public boolean isExisteBd() {
        return existeBd;
    }

    public void setExisteBd(boolean existeBd) {
        this.existeBd = existeBd;
    }

    /**
     * Envia correo al estudiante indicandole una modificaci�n o una inserci�n
     * de una nota.
     *
     * @param asunto
     *            Asunto del correo electr�nico
     * @param mensaje
     *            Mensaje que contendr� el correo electr�nico
     */
    public void enviarCorreoEstudiante(String asunto, String mensaje) {
        if (alumno.isNotificable()) {
            Correo correo = new Correo();
            String[] para = new String[2];
            // Si el alumno tiene correo electr�nico definido (correo_electr)
            if (alumno.getMail() != null) {
                para[0] = alumno.getMail();
                if (alumno.getMailAlternativo() != null) {
                    // Si el alumno tiene correo institucional definido
                    // (correo_inst)
                    para[1] = alumno.getMailAlternativo();
                } else {
                    String temp = para[0];
                    para = new String[1];
                    para[0] = temp;
                }
            } else if (alumno.getMailAlternativo() != null) {
                // Si no tiene correo electr�nico definido (correo_electr) pero
                // si
                // institucional (correo_inst)
                para = new String[1];
                para[0] = alumno.getMailAlternativo();
            }
            correo.setDe("siaepre@icesi.edu.co");
            correo.setPara(para);
            correo.setAsunto(asunto);
            correo.setMensaje(mensaje);
            correo.enviarMSG();
        }
    }

    /**
     * Determina si hubo cambio en la calificaci�n.
     *
     * @return true si no hubo cambio en la calificaci�n
     */
    public boolean cambioCalificacion() {
        return calificacion == copiaCalificacion;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public double getCopiaCalificacion() {
        return copiaCalificacion;
    }

    public void setCopiaCalificacion(double copiaCalificacion) {
        this.copiaCalificacion = copiaCalificacion;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Matricula(Curso curso, Alumno alumno) {
        super();
        this.curso = curso;
        this.alumno = alumno;
        existeBd = false;
    }

    /**
     * Almacena la calificaci�n en la base de datos.
     *
     * @param conexion
     *            Conexi�n a la base de datos
     */
    public synchronized int guardarBd(Connection conexion) {
        int cambios = 0;
        String comentarioInsercion = this.comentario != null ? "'"
                + comentario.replaceAll("\\'", "''") + "'" : "''";
        // Esta primera inserci�n es para guardar la nota ingresada.
        String sql = "insert into tntp_calificaciones (codigo_alumno, consec_actividad, consec_curso, calificacion, comentarios)"
                + "values('"
                + alumno.getCodigo()
                + "',"
                + actividad.getConsecutivo()
                + ","
                + curso.getConsecutivo()
                + "," + calificacion + "," + comentarioInsercion + ")";
        Statement stm = null;
        try {
            stm = conexion.createStatement();
            cambios = stm.executeUpdate(sql);
            copiaCalificacion = calificacion;
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripci�n:");
            System.out.println(e.getMessage()
                    + " - Error guardando la calificaci�n  del alumno "
                    + alumno.getCodigo() + " en la actividad "
                    + actividad.getNombre() + " de la materia "
                    + curso.getCodigoMateria() + " - " + curso.getNombre()
                    + " grupo " + curso.getGrupo() + ".");
            // e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
        return cambios;
    }

    /**
     * Carga la calificaci�n del estudiante para la actividad respectiva.
     *
     * @param conexion
     *            Conexi�n a la base de datos
     */
    public void cargarCalificacion(Connection conexion) {
        String sql = "select calificacion,comentarios "
                + "from tntp_calificaciones " + "where codigo_alumno='"
                + alumno.getCodigo() + "' " + "and consec_curso="
                + curso.getConsecutivo() + " " + "and consec_actividad="
                + actividad.getConsecutivo();

        Statement stm = null;
        ResultSet rs = null;
        try {
            stm = conexion.createStatement();
            rs = stm.executeQuery(sql);
            if (rs.next()) {
                calificacion = rs.getDouble("calificacion");
                copiaCalificacion = calificacion;
                comentario = rs.getString("comentarios");
                existeBd = true;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripci�n:");
            System.out.println("Error cargando la calificaci�n "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(rs, stm);
    }

    /**
     * Actualiza la calificaci�n del estudiante para la actividad respectiva en
     * la base de datos.
     *
     * @param conexion
     *            Conexi�n a la base de datos
     */
    public synchronized int actualizarBd(Connection conexion) {
        int cambios = 0;
        String comentarioInsercion = this.comentario != null ? "'"
                + comentario.replaceAll("\\'", "''") + "'" : "''";
        String sql = "update tntp_calificaciones  set calificacion="
                + this.calificacion + ", comentarios=" + comentarioInsercion
                + " " + "where codigo_alumno='" + alumno.getCodigo() + "' "
                + "and consec_curso=" + curso.getConsecutivo() + " "
                + "and consec_actividad=" + actividad.getConsecutivo();

        Statement stm = null;
        try {
            stm = conexion.createStatement();
            cambios = stm.executeUpdate(sql);
            ControlRecursos.liberarRecursos(null, stm);
            // A continuaci�n registramos el cambio en la tabla de
            // auditor�as
            String accion = "Modificaci�n de nota del estudiante en la actividad ";

            sql = "insert into tntp_auditorias(ACCION,ALUMNO_CODIGO,CONSEC_CURSO,DATO_ANTERIOR, DATO_NUEVO, FECHA_HORA, CONSECUTIVO) "
                    + "values ('"
                    + accion
                    + actividad.getConsecutivo()
                    + "','"
                    + alumno.getCodigo()
                    + "',"
                    + curso.getConsecutivo()
                    + ",'"
                    + copiaCalificacion
                    + "','"
                    + calificacion
                    + "',sysdate, (select (nvl(max(consecutivo), 0) + 1) from tntp_auditorias))";
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
            copiaCalificacion = calificacion;
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripci�n:");
            System.out.println(e.getMessage()
                    + " - Error actualizando la calificaci�n  del alumno "
                    + alumno.getCodigo() + " en la actividad "
                    + actividad.getNombre() + " de la materia "
                    + curso.getCodigoMateria() + " - " + curso.getNombre()
                    + " grupo " + curso.getGrupo() + ".");
            // e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
        return cambios;
    }

    /**
     * Elimina la calificaci�n f�sicamente de la base de datos. Deve guardar
     * esto en las auditor�as
     *
     * @param conexion
     *            Conexi�n a la base de datos
     */
    public void eliminarBD(Connection conexion) {
        String sql = "delete from tntp_calificaciones "
                + "where codigo_alumno='" + alumno.getCodigo() + "' "
                + "and consec_curso=" + curso.getConsecutivo() + " "
                + "and consec_actividad=" + actividad.getConsecutivo();

        Statement stm = null;
        try {
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
            // A continuaci�n registramos el cambio en la tabla de
            // auditor�as
            String accion = "Eliminaci�n de nota en la actividad ";
            sql = "insert into tntp_auditorias(ACCION,ALUMNO_CODIGO,CONSEC_CURSO,DATO_ANTERIOR,FECHA_HORA, consecutivo) "
                    + "values ('"
                    + accion
                    + actividad.getConsecutivo()
                    + "','"
                    + alumno.getCodigo()
                    + "',"
                    + curso.getConsecutivo()
                    + ",'"
                    + copiaCalificacion
                    + "',sysdate, (select (nvl(max(consecutivo), 0) + 1) from tntp_auditorias))";
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
            this.calificacion = 0;
            copiaCalificacion = -1;
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripci�n:");
            System.out.println("Error eliminando la calificaci�n "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
    }

    public Actividad getActividad() {
        return actividad;
    }

    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }
}
