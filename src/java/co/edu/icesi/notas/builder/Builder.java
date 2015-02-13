package co.edu.icesi.notas.builder;

import java.sql.Connection;
import java.util.List;

import co.edu.icesi.notas.Actividad;
import co.edu.icesi.notas.Clasificacion;
import co.edu.icesi.notas.Curso;
import co.edu.icesi.notas.TipoCategoria;
import co.edu.icesi.notas.basica.Activity;

/**
 * Interfaz que define el comportamiento de las clases encargadas de crear los objetos Curso (y su contenido).
 * Esta interfaz implementa el patr�n de dise�o de creaci�n orientado a objetos llamado Builder.
 * @author mzapata
 */
public interface Builder {
	
	/**
	 * Este m�todo se encarga de crear un objeto Curso a partir de un objeto que herede de la 
	 * clase CursoAbstracto. 
	 * @param curso CursoAbstracto a partir del cual se crear� el objeto Curso.
	 * @param tipoCategorias Colecci�n con los tipos de categor�a asociados al departamento acad�mico 
	 * al cual pertecene el curso.
	 * @param conexion Conexi�n a la base de datos.
	 * @return Curso creado a partir del CursoAbstracto por par�metro.
	 */
	public Curso crearCurso(CursoAbstracto curso,List tipoCategorias,Connection conexion);
	
	/**
	 * Este m�todo se encarga de actualizar el objeto Curso y todos los objetos que los contiene.
	 * @param curso Curso al acual se 
	 * @param cursoAbstracto CursoAbstracto
	 * @param tipoCategorias Colecci�n con los tipos de categor�a asociados al departamento acad�mico 
	 * al cual pertecene el curso.
	 * @param conexion Conexi�n a la base de datos.
	 */
	public void modificarCurso(Curso curso, CursoAbstracto cursoAbstracto,List tipoCategorias,Connection conexion);
	
	/**
	 * Elimina la actividad del Curso cuyo consecutivo coincida con el par�metro.
	 * @param curso Curso del cual se eliminar� la Actividad.
	 * @param cursoAbstracto CursoAbstracto
	 * @param conexion Conexi�n a la base de datos.
	 * @param consecutivoActividad Consecutivo de la Actividad a Eliminar.
	 * @return true si la Actividad fue eliminada.
	 */
	public boolean eliminarActividad(Curso curso,CursoAbstracto cursoAbstracto,Connection conexion,int consecutivoActividad);
	
	/**
	 * Crea al curso con sus respectivas clasificaciones.
	 * @param curso Curso al cual se le crear�n las clasificaciones.
	 */
	void construir(Curso curso);
	
	/**
	 * Crea las categor�as asociadas a una clasificaci�n.
	 * @param curso
	 * @param cla
	 * @param tipoCategorias
	 * @param conexion
	 */
	void crearCategorias(Curso curso,Clasificacion cla,List tipoCategorias,Connection conexion);
	
	/**
	 * Determina si el tipo de categor�a (representada por el par�metro nombre)
	 * est� en la colecci�n de tipos de categor�as.
	 * @param tipoCategorias Colecci�n con los tipos de categor�a asociados al departamento acad�mico 
	 * al cual pertecene el curso.
	 * @param nombre Nombre del tipo de categor�a a buscar.
	 * @return En caso de que la categor�a est�, se retorna. En caso contrario, se
	 * devuelve null.
	 */
	TipoCategoria estaEnLista(List tipoCategorias,String nombre);
	/**
	 * Crea un nuevo tipo de categor�a y la guarda en la base de datos.
	 * @param nombre Nombre del objeto TipoCategoria a crear.
	 * @param tipoCategorias Colecci�n con los tipos de categor�a asociados al departamento acad�mico 
	 * al cual pertecene el curso.
	 * @param conexion Conexi�n a la base de datos.
	 * @return Objeto TipoCategoria creado y guardado en la base de datos
	 */
	TipoCategoria guardarNuevoTipoCateg(String nombre,List tipoCategorias, Connection conexion);
	
	/**
	 * Determina si la categor�a (representada por tipoCat) est� contenida en la Clasificaci�n.
	 * @param cla Clasificaci�n
	 * @param tipoCat TipoCategoria a buscar en el objeto Clasificaci�n
	 * @return true si el tipo de categor�a existe en el objeto Clasificaci�n
	 */
	boolean existeCategoria(Clasificacion cla,TipoCategoria tipoCat);
	
	/**
	 * Calcula el porcentaje que le corresponde a cada categor�a, en base a los objetos Activity respectivos.
	 * @param cla Objeto Clasificaci�n que contiene las categor�as.
	 * **/
	void porcentajeCategorias(Clasificacion cla);
	
	/**
	 * Crea las actividades y las asocia a su respectiva categor�a, dentro de la Clasificaci�n 
	 * por par�metro.
	 * @param cla Clasificacion
	 */
	void crearActividades(Clasificacion cla);
	
	/**
	 * Almacena los objetos creados en la BD.
	 * @param curso Curso cuyas clasificaciones, categorias y actividades se guardar�n en la base de datos.
	 * @param conexion Conexi�n a la base de datos.
	 */
	void guardarBD(Curso curso,Connection conexion);
	
	/**
	 * Retorna el objeto Activity asociado con la Actividad por par�metro.
	 * @param actividad Actividad
	 * @return Activity
	 */
	Activity obtenerActivity(Actividad actividad);
	
	/**
	 * Crea una nueva Actividad con la informaci�n del objeto Activity por par�metro.
	 * @param actual Activity  para la cual se crear� una Actividad.
	 * @param tipoCategorias Colecci�n con los tipos de categor�a asociados al departamento acad�mico 
	 * al cual pertecene el curso.
	 * @param conexion Conexi�n a la base de datos.
	 * @param curso Curso al cual se agregar� una nueva actividad.
	 */
	void agregarActivity(Activity actual,List tipoCategorias,Connection conexion,Curso curso);
	
	/**
	 * Este m�todo llama a los m�todos respectivos encargados de calcular los porcentajes para
	 * las clasificaciones, las categorias y los porcentajes.
	 * @param curso Curso al que se le recalcular�n los porcentajes.
	 */
	void recalcularPorcentajes(Curso curso);
	
	/**
	 * Como su nombre lo indica, modifica el porcentaje de los actividades.
	 * @param cla Clasificaci�n a la que se modificaran los porcentajes de sus actividades.
	 */
	void modificarPorcentajesActividades(Clasificacion cla);
	
	/**
	 * Recorre la colecci�n de Activities del Subject, y para cada una actualiza su objeto Actividad
	 * hom�logo, el cual est� contenido en el curso.
	 * @param curso Curso al cual se actualizar�n sus actividades.
	 */
	void actualizarInfoActividades(Curso curso);
	
	/**
	 * Actualiza la informaci�n del objeto Actividad equivalente al objeto Activity por par�metro. 
	 * @param actual Objeto tipo Activity
	 * @param curso Curso al cual pertenece la Actividad a actualizar.
	 */
	void actualizarActividad(Activity actual,Curso curso);
	
	/**
	 * Este m�todo se invoca luego de haber modificado los porcentajes de la actividades.
	 * Realiza las actualizaciones respectivas en la base de datos para los objetos Clasificacion,
	 * Categoria y Actividad, del curso.
	 * @param curso Curso al cual se actualizar�n sus �tems.
	 * @param conexion Conexi�n a la base de datos.
	 */
	void actualizarBD(Curso curso, Connection conexion);
}
