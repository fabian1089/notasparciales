//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.8.4/xslt/JavaClass.xsl
package co.edu.icesi.notas.action;

import java.util.ArrayList;

import javax.servlet.http.*;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.*;

import co.edu.icesi.notas.basica.Activity;
import co.edu.icesi.notas.control.*;
import co.edu.icesi.notas.estudiante.control.ControlEstudiante;
import co.edu.icesi.notas.form.*;

/**
 * MyEclipse Struts Creation date: 12-14-2006 Esta acci�n prepara el formulario
 * para una actividad espec�fica que se planea actualizar. El formulario tendr�
 * los datos de la actividad en cuesti�n. XDoclet definition:
 * 
 * @struts:action validate="true"
 */
public class ModificarActividadAction extends Action {

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
        try {
            HttpSession session = ControlSesion.obtenerSesion(request);
            if (session != null) {
                idUsuario = ControlSesion.getUsuario(request);
                ActualizarActividadForm actualizarActividadForm = new ActualizarActividadForm();

                String indice1 = (String) request.getAttribute("index1");
                String indice2 = (String) request.getAttribute("index2");

                if (session.getAttribute("curso") == null) {
                    request.setAttribute(
                            "mensajeError",
                            "Ha vuelto a la p�gina de listado de cursos pero no ha escogido un curso, por favor escoja un curso del listado inicial.");
                    return mapping.findForward("errorAplicacion");
                }

                Activity act = (Activity) session.getAttribute("Actividad");
                ControlEstudiante estudiante = (ControlEstudiante) session.getAttribute("estudiante");
                String origen = (String) request.getAttribute("origenPorcentaje");

                /*
                 * Si el objeto act es null, esta acci�n fue invocada desde la
                 * p�gina '/basica/esquema.jsp'. Adem�s, esto implica que index1
                 * e index2 no est�n en el request como atributos, sino como
                 * par�metros del mismo.
                 */
                if (act == null) {
                    ArrayList listaCat = (ArrayList) session.getAttribute("listaCategorias");
                    indice1 = request.getParameter("index1");
                    indice2 = request.getParameter("index2");
                    Category cat = (Category) listaCat.get(Integer.parseInt(indice1));
                    act = cat.getActivity(Integer.parseInt(indice2));
                    origen = "esquema";
                }
                try {
                    BeanUtils.copyProperties(actualizarActividadForm, act);
                } catch (Exception e) {
                    System.out.println("Error al crear el formulario de la actividad");
                    e.printStackTrace();
                }
                request.setAttribute("actualizarActividadForm",
                        actualizarActividadForm);
                session.setAttribute("Actividad", act);
                session.setAttribute("origen", origen);
                if (estudiante != null) {
                    return mapping.findForward("exitoEstud");
                } else {
                    return mapping.findForward("exito");
                }
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
}
