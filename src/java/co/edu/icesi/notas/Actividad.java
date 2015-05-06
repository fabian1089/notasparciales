package co.edu.icesi.notas;

import java.io.*;
import java.sql.*;
import java.util.*;

import co.edu.icesi.notas.control.ControlRecursos;
import co.edu.icesi.notas.utilidades.OperacionesMatematicas;

/**
 * Esta clase describe el objeto Actividad, el cual consiste en el último
 * elemento de la jerarquía en el esquema de un curso. Cada estructura pertenece
 * a una Categoría y representa una unidad dentro de este. Por ejemplo, si se
 * tiene una Categoría que corresponda al tipo "Parciales", una actividad sería
 * un elemento dentro de este grupo. Igualmente la suma de los porcentajes de
 * las actividades de la Categoría debe ser 100%.
 * 
 * @author lmdiaz, mzapata
 */
public class Actividad implements Serializable {

    private int consecutivo;
    private String nombre;
    private double porcentaje;
    private String fechaRealizacion;
    private String temas;
    private String descripcion;
    private List matriculas;
    // Copias para auditoria//
    private double copiaPorcentaje;
    private String copiaTemas;
    private String copiaDescripcion;
    private String copiaFecha;
    private Categoria categoria;
    
    //ffceballos
    private java.util.Date fechaIngreso;
    
    public java.util.Date getFechaIngreso(){
        return fechaIngreso;
    }
    public void setFechaIngreso(java.util.Date fecha){
        this.fechaIngreso=fecha;
    }
    
    //ffceballos

    /**
     * Se emplea al crear una nueva actividad, para asignar las matriculas
     * respectivas al objeto de acuerdo a los alumnos que se matriculados en el
     * curso.
     */
    public void agregarNuevasMatriculas() {
        ArrayList alumnos = this.getCategoria().getClasificacion().getCurso().getAlumnos();
        for (int i = 0; i < alumnos.size(); i++) {
            co.edu.icesi.notas.Alumno alumno = (co.edu.icesi.notas.Alumno) alumnos.get(i);
            Matricula mat = new Matricula(this.getCategoria().getClasificacion().getCurso(), alumno);
            this.getMatriculas().add(mat);
        }
    }

    public void setMatriculas(ArrayList matriculas) {
        this.matriculas = matriculas;
    }

    public List getMatriculas() {
        return matriculas;
    }

    public Actividad(int consecutivo, String nombre, double porcentaje,
            String fechaRealizacion, String temas, String descripcion) {
        super();
        matriculas = new ArrayList();
        this.consecutivo = consecutivo;
        this.nombre = nombre;
        this.porcentaje = porcentaje;
        this.fechaRealizacion = fechaRealizacion;
        this.temas = temas;
        this.descripcion = descripcion;
    }

    public Actividad() {
        super();
        matriculas = new ArrayList();
    }

    public int getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(int consecutivo) {
        this.consecutivo = consecutivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFechaRealizacion() {
        return fechaRealizacion;
    }

    public void setFechaRealizacion(String fechaRealizacion) {
        this.fechaRealizacion = fechaRealizacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getTemas() {
        return temas;
    }

    public void setTemas(String temas) {
        this.temas = temas;
    }

    /**
     * Guarda la actividad en la base de datos.
     *
     * @param conexion
     *            Conexión a la base de datos
     */
    public void guardarBd(Connection conexion) {
        String fRealizacion = (fechaRealizacion == null) ? "null" : "to_date('"
                + fechaRealizacion + "', 'yyyy-mm-dd')";
        String sql = "insert into tntp_actividades (consec_categoria, nombre, porcentaje, fecha_realizacion, temas, descripcion) "
                + "values ("
                + this.categoria.getConsecutivo()
                + ", '"
                + nombre
                + "', "
                + porcentaje
                + ", "
                + fRealizacion
                + ", '"
                + (temas != null ? temas.replaceAll("\\'", "''") : "")
                + "', '"
                + (descripcion != null ? descripcion.replaceAll("\\'", "''")
                : "") + "')";
        Statement stm = null;
        try {
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
            consecutivo = getConsecutivoActividad(conexion);
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error guardando la actividad " + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
    }

    /**
     * Este método se encarga de actualizar las modificaciones realizadas al
     * objeto Actividad en la BD.
     *
     * @param conexion
     *            Conexión a la base de datos
     */
    public void actualizarBd(Connection conexion) {

        String fRealizacion = (fechaRealizacion == null) ? "null" : "to_date('"
                + fechaRealizacion + "', 'yyyy-mm-dd')";
        String sql = "update tntp_actividades set porcentaje="
                + porcentaje
                + " , fecha_realizacion="
                + fRealizacion
                + ", temas='"
                + (temas != null ? temas.replaceAll("\\'", "''") : "")
                + "', descripcion='"
                + (descripcion != null ? descripcion.replaceAll("\\'", "''")
                : "") + "' " + "where consecutivo=" + consecutivo;
        Statement stm = null;
        try {
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error actualizando la actividad "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
    }

    /**
     * Llama a un procedimiento que se encarga de eliminar 'lógicamente' una
     * actividad de la BD, asignandole una fecha de cancelación.
     *
     * @param conexion
     *            Conexión a la base de datos
     * @return Número que indica si la actividad fue o no eliminada de la BD.
     */
    public int eliminarBd(Connection conexion) {

        int resp = 0;
        // String sql="declare vresultado number; begin
        // PBORRAITEM_TNTP("+consecutivo+", vresultado); end;";
        String sql = "update tntp_actividades set fecha_cancelacion = sysdate where consecutivo = "
                + this.consecutivo;
        Statement stm = null;
        try {
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
            resp = 2;
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error cancelando la actividad "
                    + e.getMessage());
            e.printStackTrace();
            resp = 1;
        }
        ControlRecursos.liberarRecursos(null, stm);
        return resp;
    }

    public int hashCode() {
        return consecutivo;
    }

    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    /**
     * Obtiene una matrícula a partir del código de un alumno.
     *
     * @param codigoAlumno
     *            Código del alumno
     * @return La matrícula asociada al alumno para esa actividad.
     */
    public Matricula getMatricula(String codigoAlumno) {
        for (int i = 0; i < matriculas.size(); i++) {
            Matricula m = (Matricula) matriculas.get(i);
            if (m.getAlumno().getCodigo().equals(codigoAlumno)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Obtiene el promedio de todas las calificaciones de los estudiantes para
     * la actividad actual.
     *
     * @return Promedio de las calificaciones.
     */
    public double getPromedio() {
        double promedio = 0;
        Matricula m;
        int contador = 0;
        for (int i = 0; i < matriculas.size(); i++) {
            m = (Matricula) matriculas.get(i);
            if (m.getAlumno().getEstado().equals("A")) {
                promedio += m.getCalificacion();
                contador++;
            }
        }
        if (contador == 0) {
            return 0;
        }
        double prom = OperacionesMatematicas.dividir(promedio, contador);
        return OperacionesMatematicas.redondear(prom, 1);
    }
    
    
    
    //ffceballos 20/09/2013
    
    
    /**
     * Obtiene el la desviación estándar de todas las calificaciones de los estudiantes para
     * la actividad actual.
     *
     * @return Desviación estándar de todas las calificaciones.
     */
   public double getDesviacion() {
        
        double promedio=this.getPromedio();
        double var=0;
        double desviacion=0;
        double n=0;
       
        Matricula m;
        
        for (int i = 0; i < matriculas.size(); i++) {
            m = (Matricula) matriculas.get(i);           
            if (m.getAlumno().getEstado().equals("A")) {
               
              var+=Math.pow(m.getCalificacion()-promedio,2);  
              n++;
            }
        }
        
        desviacion=Math.sqrt((1/n)*var);
        
        return OperacionesMatematicas.redondear(desviacion, 2);
    }
    
    //fin ffceballos
    
    
    
    

    /**
     * Calcula el consecutivo de una actividad guardada en la BD, de acuerdo al
     * nombre al nombre y al consecutivo de la categorìa a la cual pertenece.
     *
     * @param conexion
     *            Conexión a la base de datos
     * @return El consecutivo que retorna la BD para la actividad actual.
     */
    public int getConsecutivoActividad(Connection conexion) {
        int cons = 0;
        String sql = "select consecutivo from tntp_actividades where nombre='"
                + this.nombre + "' and consec_categoria="
                + this.categoria.getConsecutivo();
        Statement stm = null;
        ResultSet rs = null;
        try {
            stm = conexion.createStatement();
            rs = stm.executeQuery(sql);
            if (rs.next()) {
                cons = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error cargando consecutivo de la actividad "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(rs, stm);
        return cons;
    }

    /**
     * Método que sirve para indicar qué tipo de cambios se van a registrar.
     *
     * @param conexion
     *            Conexión a la base de datos
     * @param curso
     *            Curso al que pertenece la actividad.
     */
    public void registrarCambios(Connection conexion, Curso curso) {

        if (this.getFechaRealizacion() != null
                && (this.copiaFecha != null && !this.copiaFecha.equals(""))
                && !this.getFechaRealizacion().equals(copiaFecha)) {
            actualizarCambioBd(conexion, curso, copiaFecha, this.getFechaRealizacion(), "La fecha de la actividad "
                    + this.getConsecutivo() + " fue modificada");
        }
        /*
         * Si copiaPorcentaje es diferente de cero, es porque efectivamente el
         * porcentaje fue modificado, y no que se está creando por primera vez.
         */
        if (this.getPorcentaje() != copiaPorcentaje && copiaPorcentaje != 0) {
            actualizarCambioBd(conexion, curso, "" + copiaPorcentaje, ""
                    + this.getPorcentaje(), "El porcentaje de la actividad "
                    + this.getConsecutivo() + " fue modificado");
        }
        if (this.getTemas() != null
                && (this.copiaTemas != null && !this.copiaTemas.equals(""))
                && !this.getTemas().equals(copiaTemas)) {
            actualizarCambioBd(conexion, curso, copiaTemas, this.getTemas(),
                    "Los temas de la actividad " + this.getConsecutivo()
                    + " fueron modificados");
        }
        if (this.getDescripcion() != null
                && (this.copiaDescripcion != null && !this.copiaDescripcion.equals(""))
                && !this.getDescripcion().equals(copiaDescripcion)) {
            actualizarCambioBd(conexion, curso, copiaDescripcion, this.getDescripcion(), "La descripción de la actividad "
                    + this.getConsecutivo() + " fue modificada");
        }
    }

    /**
     * Inserta un registro en la tabla de auditorias, indicando que se modificó
     * de la actividad.
     *
     * @param conexion
     *            Conexión a la base de datos
     * @param curso
     *            Curso al cual pertenece la actividad.
     * @param datoViejo
     *            Dato anterior que tenía alguno de los atributos del objeto.
     * @param datoNuevo
     *            Dato que tiene alguno de los atributos de la variable.
     * @param accion
     *            Texto descriptivo explicando el cambio realizado al objeto.
     */
    public void actualizarCambioBd(Connection conexion, Curso curso,
            String datoViejo, String datoNuevo, String accion) {
        Statement stm = null;
        try {
            String sql = "insert into tntp_auditorias (fecha_hora,dato_anterior,dato_nuevo,accion,consec_curso, consecutivo)";
            sql += "values(sysdate,'"
                    + (datoViejo != null ? datoViejo.replaceAll("\\'", "''")
                    : "")
                    + "','"
                    + (datoNuevo != null ? datoNuevo.replaceAll("\\'", "''")
                    : "")
                    + "','"
                    + accion
                    + "',"
                    + curso.getConsecutivo()
                    + ", (select (nvl(max(consecutivo), 0) + 1) from tntp_auditorias))";
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error auditando cambios de la actividad "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
    }

    /**
     * Método que sirve para registrar el ingreso de una actividad en la tabla
     * de auditorías.
     *
     * @param conexion
     *            Conexión a la base de datos
     * @param curso
     *            Curso al que pertenece la actividad.
     */
    public void registrarInsercion(Connection conexion, Curso curso) {
        actualizarCambioBd(conexion, curso, "", "",
                "Inserción de una nueva actividad con consecutivo "
                + consecutivo);
    }

    /**
     * Método que sirve para registrar la eliminación de una actividad en la
     * base de datos.
     *
     * @param conexion
     *            Conexión a la base de datos
     * @param curso
     *            Curso al que pertenece la actividad.
     */
    public void registrarEliminacion(Connection conexion, Curso curso) {
        actualizarCambioBd(conexion, curso, "", "",
                "Eliminación de la actividad con consecutivo " + consecutivo);
    }

    /**
     * Actualiza los atributos que se emplean como copia de los atributos
     * principales de la Actividad.
     */
    public void crearCopia() {
        copiaFecha = fechaRealizacion;
        copiaTemas = temas;
        copiaDescripcion = descripcion;
        copiaPorcentaje = porcentaje;
    }

    /**
     * Elimina una estructura de la base de datos de forma física, es decir,
     * elimina el registro.
     *
     * @param conexion
     *            Conexión a la base de datos
     */
    public void eliminarFisicaBd(Connection conexion) {
        String sql = "delete from tntp_actividades where consecutivo="
                + this.consecutivo;
        Statement stm = null;
        try {
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error eliminando la actividad "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    /**
     * Reactiva una actividad que estaba cancelada en la BD.
     *
     * @param conexion
     *            - Conexión a la base de datos
     */
    public void reactivarBd(Connection conexion) {
        String sqlBorrado = "delete " + "from tntp_calificaciones "
                + "where consec_actividad=" + this.consecutivo;
        String sqlActualizado = "update tntp_actividades "
                + "set fecha_cancelacion='', porcentaje=" + this.porcentaje
                + ", descripcion='" + this.descripcion + "', temas='"
                + this.temas + "' " + "where consecutivo=" + this.consecutivo;

        Statement stm = null;

        try {
            stm = conexion.createStatement();
            stm.executeUpdate(sqlBorrado);
            ControlRecursos.liberarRecursos(null, stm);
            stm = conexion.createStatement();
            stm.executeUpdate(sqlActualizado);
            ControlRecursos.liberarRecursos(null, stm);
        } catch (Exception e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error reactivando la actividad "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
    }
    
    
    //ffceballos
    
     /**
     * Cambia la fecha de ingreso de la primera nota para validar el cambio de nota de parciales
     *
     * @param conexion
     *            - Conexión a la base de datos
     */
    
    
    public void fechaIngreso(Connection conexion) {
        Statement stm = null;
        try {
           // GregorianCalendar fechaActual=new GregorianCalendar();
           // String fecha=fechaActual.get(Calendar.DATE)+"/"+fechaActual.get(Calendar.MONTH)+"/"+fechaActual.get(Calendar.YEAR);
            //to_date('"+fecha+"','dd/mm/yyyy')
            String sql = "update tntp_actividades set fecha_ingreso_nota= sysdate  where consecutivo="+this.consecutivo;
          //  System.err.println("update: "+sql);
            stm = conexion.createStatement();
            stm.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Descripción:");
            System.out.println("Error guardando la fecha de ingreso "
                    + e.getMessage());
            e.printStackTrace();
        }
        ControlRecursos.liberarRecursos(null, stm);
    }
    
    
    
    //fin ffceballos
    
    
}
