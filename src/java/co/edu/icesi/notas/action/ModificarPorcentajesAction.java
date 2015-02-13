//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_3.8.4/xslt/JavaClass.xsl
package co.edu.icesi.notas.action;

import javax.servlet.http.*;

import org.apache.struts.action.*;

import co.edu.icesi.notas.form.*;
import co.edu.icesi.notas.utilidades.*;
import co.edu.icesi.notas.basica.*;
import co.edu.icesi.notas.control.*;
import co.edu.icesi.notas.Curso;

import java.sql.Connection;
import java.util.*;

/**
 * MyEclipse Struts Creation date: 12-13-2006 Acci�n que modifica los
 * porcentajes de las actividades del curso. En esta acci�n se hace la
 * traducci�n de los porcentajes que ve el usuario a la l�gica interna del
 * programa que los trata y organiza. Tambi�n se establece la categorizaci�n
 * interna de las actividades. XDoclet definition:
 * 
 * @struts:action path="/modificarPorcentajes" name="modificarPorcentajesForm"
 *                scope="request" validate="true"
 */
public class ModificarPorcentajesAction extends Action {

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
            HttpSession session = ControlSesion.obtenerSesion(request);
            if (session != null) {
                idUsuario = ControlSesion.getUsuario(request);
                if (session.getAttribute("curso") == null) {
                    request.setAttribute(
                            "mensajeError",
                            "Ha vuelto a la p�gina de listado de cursos pero no ha escogido un curso, por favor escoja un curso del listado inicial.");
                    return mapping.findForward("errorAplicacion");
                }

                session.removeAttribute("errores");

                ModificarPorcentajesForm modificarPorcentajesForm = (ModificarPorcentajesForm) form;
                Subject sub = (Subject) session.getAttribute("subject");
                ControlProfesores cp = (ControlProfesores) session.getAttribute("profesores");

                if (modificarPorcentajesForm == null || sub == null
                        || cp == null) {
                    request.setAttribute("mensajeError",
                            "La aplicaci�n ha recibido par�metros inv�lidos.");
                    return mapping.findForward("errorAplicacion");
                }

                ArrayList lista = modificarPorcentajesForm.getListaCategorias();

                String opcion = modificarPorcentajesForm.getOpcion();

                String index1;
                String tipoConf = ((Curso) session.getAttribute("curso")).getTipoConfiguracion();
                // Si desea modificar la actividad
                if (opcion.equals("1")) {
                    index1 = modificarPorcentajesForm.getIndiceUno();
                    String index2 = modificarPorcentajesForm.getIndiceDos();
                    Activity act = (Activity) ((Category) modificarPorcentajesForm.getListaCategorias().get(Integer.parseInt(index1))).getActivity(Integer.parseInt(index2));
                    session.setAttribute("Actividad", act);
                    request.setAttribute("index1", index1);
                    request.setAttribute("index2", index2);
                    if (tipoConf.equals("B")) {
                        request.setAttribute("origenPorcentaje",
                                "porcentajeBasico");
                    } else {
                        if (tipoConf.equals("I")) {
                            request.setAttribute("origenPorcentaje",
                                    "porcentajeIntermedio");
                        }
                    }
                    copiarActividades(sub, lista);
                    ControlRecursos.liberarRecursos(conexion);
                    return mapping.findForward("modificacion");

                }
                // En caso de que se desee regresar
                if (opcion.equals("2")) {
                    copiarActividades(sub, lista);
                    ControlRecursos.liberarRecursos(conexion);
                    return mapping.findForward("anterior");
                }

                // En caso de que se quiera redistribuir los porcentajes.
                if (opcion.equals("3")) {
                    index1 = modificarPorcentajesForm.getIndiceUno();
                    Category seleccion = (Category) modificarPorcentajesForm.getListaCategorias().get(Integer.parseInt(index1));
                    if (seleccion.getPercentage() <= 0) {
                        ActionErrors errors = new ActionErrors();
                        errors.add("errorCat", new ActionError(
                                "errores.cat.cero"));
                        session.setAttribute("errores", "S");
                        saveErrors(request, errors);
                        ControlRecursos.liberarRecursos(conexion);
                        return mapping.findForward("fracaso");
                    }
                    UtilidadCategories.redistribuirPorcentajes(seleccion);
                    ControlRecursos.liberarRecursos(conexion);
                    return mapping.findForward(ControlSecuencia.getNextModificarPorcentaje(tipoConf));
                }

                // Si desea continuar
                // Pasar de nuevo las actividades al subject
                copiarActividades(sub, lista);
                // Copiamos los porcentajes individuales y grupales asignados al
                // form
                sub.setPorcentajeIndividual(modificarPorcentajesForm.getPorcentajeIndividual());
                sub.setPorcentajeGrupal(modificarPorcentajesForm.getPorcentajeGrupal());

                // Realizar las validaciones correspondientes.
                ActionErrors errores = validar(sub, ((Curso) session.getAttribute("curso")).getTipoConfiguracion());
                if (!errores.isEmpty()) {
                    session.setAttribute("errores", "S");
                    saveErrors(request, errores);
                    ControlRecursos.liberarRecursos(conexion);
                    return mapping.findForward(ControlSecuencia.getNextFracasoPorcentaje(tipoConf));
                }
                conexion = ControlRecursos.obtenerConexion();
                /*
                 * Esta variable indica si es la primera vez que crea el
                 * esquema, o si por el contrario lo est� modificando.
                 */
                boolean primeraVez = session.getAttribute("nuevo") != null
                        && session.getAttribute("nuevo").equals("S");
                // Si es la primera vez que se crea el esquema con notas
                // parciales
                if (primeraVez) {
                    sub.crearCurso(cp.getTiposCategoria(), conexion, tipoConf);
                    session.removeAttribute("nuevo");
                } else {
                    // Si no es la primera vez se debe modificar
                    sub.modificarCurso(cp.getTiposCategoria(), conexion,
                            tipoConf);
                }
                ControlRecursos.liberarRecursos(conexion);
                return mapping.findForward("exito");
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

    /**
     * Este m�todo se encarga de manejar todas las validaciones correspondientes
     * a esta Acci�n.
     */
    private ActionErrors validar(Subject sub, String tipoConf) {

        ActionErrors errors = new ActionErrors();
        // Condici�n com�n tanto al esquema b�sico como al esquema avanzado.
        if (!sub.verificarPorcentajesMenoresCero()) {
            errors.add("diferenteCien", new ActionError("errores.dif.cien"));
        } else {
            if (tipoConf.equals("B")) {
                validarBasico(sub, errors);
            }
            if (tipoConf.equals("I")) {
                validarIntermedio(sub, errors);
            }
        }

        return errors;
    }

    /**
     * Realizar las validaciones requeridas para un curso con un esquema b�sico.
     */
    private void validarBasico(Subject sub, ActionErrors errors) {
        if (!sub.validarSuma()) {
            errors.add("errorSuma", new ActionError("errores.errorSuma"));
        }
    }

    /**
     * Realizar las validaciones requeridas para un curso con un esquema
     * intermedio.
     */
    private void validarIntermedio(Subject sub, ActionErrors errors) {

        // Validar que la suma del grupal + individual sea 100%
        double suma = sub.getPorcentajeIndividual() + sub.getPorcentajeGrupal();
        suma = OperacionesMatematicas.redondear(suma, 1);
        if (suma != 100) {
            errors.add("errorSumaTotal", new ActionError("suma.total.error"));
        }

        if (!UtilidadCategories.validarSumaClasif(sub.getActivities(), "I")) {
            errors.add("errorIndividual", new ActionError(
                    "total.individual.error"));
        }
        if (!UtilidadCategories.validarSumaClasif(sub.getActivities(), "G")) {
            errors.add("errorGrupal", new ActionError("total.grupal.error"));
        }

    }

    /**
     * copia las actividades de una lista de categorias a la lista de
     * actividades del Subject
     *
     * @param sub
     * @param categories
     */
    public void copiarActividades(Subject sub, List categories) {
        List activities = new ArrayList();
        Category cat;
        Activity act;
        for (int i = 0; i < categories.size(); i++) {
            cat = (Category) categories.get(i);
            for (int j = 0; j < cat.getListActivities().size(); j++) {
                act = (Activity) cat.getActivity(j);
                activities.add(act);
            }
        }
        sub.setActivities(activities);
    }
}
