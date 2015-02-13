//Created by MyEclipse Struts
//XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.8.3/xslt/JavaClass.xsl
package co.edu.icesi.notas.action;

import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.struts.action.*;

import co.edu.icesi.notas.*;
import co.edu.icesi.notas.control.*;

/**
 * MyEclipse Struts Creation date: 06-12-2006 Acci�n que env�a al cierre del
 * curso en cuesti�n. XDoclet definition:
 * 
 * @struts:action validate="true"
 */
public class CerrarCursoAction extends Action {

    // --------------------------------------------------------- Instance
    // Variables
    // --------------------------------------------------------- Methods
    /**
     * Method execute
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        String idUsuario = "";
        Connection conexion = null;
        try {
            HttpSession sesion = ControlSesion.obtenerSesion(request);
            if (sesion != null) {
                idUsuario = ControlSesion.getUsuario(request);
                String forward = "IrEsquema";
                if (sesion.getAttribute("profesores") == null
                        || sesion.getAttribute("fechaLimite") == null) {
                    request.setAttribute("mensajeError",
                            "La aplicaci�n ha recibido par�metros inv�lidos.");
                    return mapping.findForward("errorAplicacion");
                }

                if (sesion.getAttribute("curso") == null) {
                    request.setAttribute(
                            "mensajeError",
                            "Ha vuelto a la p�gina de listado de cursos pero no ha escogido un curso, por favor escoja un curso del listado inicial.");
                    return mapping.findForward("errorAplicacion");
                }
                conexion = ControlRecursos.obtenerConexion();
                ActionErrors errores = new ActionErrors();
                ControlProfesores profesores = (ControlProfesores) sesion.getAttribute("profesores");
                Profesor profesor = profesores.getProfesor();
                String cerrar = request.getParameter("opcion");
                forward = request.getParameter("pagina");

                Curso curso;

                // En caso de que se invoque el cierre desde
                // listaCursos.jsp.
                if (forward == null) {
                    if (request.getParameter("curso") == null
                            || request.getParameter("curso").equals("")) {
                        request.setAttribute("mensajeError",
                                "La aplicaci�n ha recibido par�metros inv�lidos.");
                        ControlRecursos.liberarRecursos(conexion);
                        return mapping.findForward("errorAplicacion");
                    }
                    String cursoId = request.getParameter("curso");
                    curso = profesores.getCurso(cursoId);
                    sesion.setAttribute("curso", curso);
                    ControlRecursos.liberarRecursos(conexion);
                    return mapping.findForward("IrCierre");
                }

                if (!forward.equals("IrCierre")) {
                    ControlRecursos.liberarRecursos(conexion);
                    return mapping.findForward("IrCierre");
                }

                curso = (Curso) sesion.getAttribute("curso");

                // Bloque para realizar el cierre del curso.
                if (cerrar.equals("cerrar")) {
                    int resultadoOperacionCierre = cerrarCurso(curso, conexion,
                            sesion, profesor.getDeptoAcademico());
                    if (resultadoOperacionCierre < 0) {
                        request.setAttribute("error", "S");
                        switch (resultadoOperacionCierre) {
                            case -1:
                                errores.add("noCerrarCurso", new ActionError(
                                        "noCerrarCurso-1.error"));
                                break;
                            case -2:
                                errores.add("noCerrarCurso", new ActionError(
                                        "noCerrarCurso-2.error"));
                                break;
                            case -3:
                                errores.add("noCerrarCurso", new ActionError(
                                        "noCerrarCurso-3.error"));
                                break;
                        }
                    }
                    saveErrors(request, errores);
                    forward = "IrCierre";
                }
                ControlRecursos.liberarRecursos(conexion);
                return mapping.findForward(forward);
            }
            System.out.println("Sesi�n inactiva: " + this.getClass().getName());
            return mapping.findForward("sesionInactiva");
        } catch (Exception e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Usuario: " + request.getRemoteUser());
            System.out.println("Descripci�n:");
            e.printStackTrace();
        } finally {
            ControlRecursos.liberarRecursos(conexion);
        }
        return mapping.findForward("errorAplicacion");
    }

    public int cerrarCurso(Curso curso, Connection conexion,
            HttpSession sesion, String deptoAcadProfesor) {
        int resultadoOperacionCierre = curso.cerrarCurso(conexion,
                deptoAcadProfesor);
        if (resultadoOperacionCierre >= 0) {

            sesion.setAttribute("cerrado", "S");
            // return "IrCierre";
        }
        // return "NoCierre";
        return resultadoOperacionCierre;
    }
}
