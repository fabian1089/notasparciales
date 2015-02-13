/*
 * Created on 12/12/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package co.edu.icesi.notas.builder;

import co.edu.icesi.notas.Curso;

/**
 * Clase que define la estructura que tendr�n los cursos que se empleen para las distintas vistas que
 * podr�a tener la aplicaci�n en futuras versiones. Por ejemplo, la clase Subject de la vista b�sica.
 * @author mzapata
 */
public abstract class CursoAbstracto {
	
	private Curso curso;
	
	public Curso getCurso() {
		return curso;
	}
	
	public void setCurso(Curso curso) {
		this.curso = curso;
	}
	
	public CursoAbstracto(){
		super();
	}

}
