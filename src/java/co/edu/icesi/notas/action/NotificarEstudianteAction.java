/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package co.edu.icesi.notas.action;

import java.util.Iterator;

import javax.servlet.http.*;

import org.apache.struts.action.*;
import co.edu.icesi.email.*;
import co.edu.icesi.notas.*;
import co.edu.icesi.notas.control.*;

/**
 * MyEclipse Struts Creation date: 11-20-2009
 * 
 * XDoclet definition:
 * 
 * @struts.action path="/notificarEstudiante" name="notificacionForm"
 *                input="/basica/calificaciones.jsp" scope="request"
 *                validate="true"
 */
public class NotificarEstudianteAction extends Action {
    /*
     * Generated Methods
     */

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
        try {
            HttpSession session = ControlSesion.obtenerSesion(request);
            if (session != null) {
                idUsuario = ControlSesion.getUsuario(request);
                if (session.getAttribute("curso") == null) {
                    request.setAttribute(
                            "mensajeError",
                            "Ha vuelto a la p�gina de listado de cursos pero no ha escogido un curso, por favor escoja un curso del listado inicial.");
                    return mapping.findForward("errorAplicacion");
                }
                DynaActionForm notificacionForm = (DynaActionForm) form;
                String mensaje = "";
                String datos[] = notificacionForm.get("datos").toString().split(";");
                String codigo = datos[0];
                String inasistencia = datos[1];
                Curso curso = (Curso) session.getAttribute("curso");
                double maximaInasistenciaPermitida = curso.getHorasPerdida(Integer.parseInt(session.getAttribute(
                        "porcentajeMinimoAsistencia").toString()));
                Correo correo = new Correo();
                String[] para = obtenerCorreosEstudiante(curso, codigo);
                if (para != null) {
                    correo.setDe("siaepre@icesi.edu.co");
                    correo.setPara(para);
                    correo.setAsunto("Notificaci�n de inasistencia");
                    correo.setMensaje("Cordial saludo,\n\nSe le notifica que usted ha faltado a "
                            + inasistencia
                            + " horas de las "
                            + maximaInasistenciaPermitida
                            + " horas de ausencia permitidas del curso "
                            + curso.getNombre()
                            + " - "
                            + curso.getCodigoMateria()
                            + ".\n\nEste correo es informativo, por favor no lo responda.");
                    if (correo.enviarMSG()) {
                        mensaje = "Mensaje enviado correctamente al estudiante "
                                + codigo;
                    } else {
                        mensaje = "El mensaje no fue enviado correctamente al estudiante "
                                + codigo;
                    }
                } else {
                    mensaje = "El estudiante con c�digo " + codigo
                            + " no tiene ning�n correo registrado";
                }
                request.setAttribute("email", mensaje);
                return mapping.findForward("continuar");
            }
            System.out.println("Sesi�n inactiva: " + this.getClass().getName());
            return mapping.findForward("sesionInactiva");
        } catch (Exception e) {
            System.out.println("Error: " + this.getClass().getName());
            System.out.println("Usuario: " + request.getRemoteUser());
            System.out.println("Descripci�n:");
            e.printStackTrace();
        }
        return mapping.findForward("errorAplicacion");
    }

    private String[] obtenerCorreosEstudiante(Curso curso, String codigo) {
        boolean siga = true;
        String[] correos = new String[2];
        for (Iterator iter = curso.getAlumnos().iterator(); siga
                && iter.hasNext();) {
            Alumno alumno = (Alumno) iter.next();
            if (alumno.getCodigo().equals(codigo)) {
                if (alumno.isNotificable()) {
                    // Si el alumno tiene correo electr�nico definido
                    // (correo_electr)
                    if (alumno.getMail() != null) {
                        correos[0] = alumno.getMail();
                        if (alumno.getMailAlternativo() != null) {
                            // Si el alumno tiene correo institucional definido
                            // (correo_inst)
                            correos[1] = alumno.getMailAlternativo();
                        } else {
                            String temp = correos[0];
                            correos = new String[1];
                            correos[0] = temp;
                        }
                    } else if (alumno.getMailAlternativo() != null) {
                        // Si no tiene correo electr�nico definido
                        // (correo_electr) pero
                        // si
                        // institucional (correo_inst)
                        correos = new String[1];
                        correos[0] = alumno.getMailAlternativo();
                    }
                } else {
                    correos = null;
                }
                siga = false;
            }
        }
        return correos;
    }
}
