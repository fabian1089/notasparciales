/*
 * Created on Dec 14, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package co.edu.icesi.notas.form;

import java.io.Serializable;

/** joha prueba!!
 * @author rescobar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Asistencia implements Serializable{
	private String porcentajeAsistencia;
	/**
	 * @return Returns the porcentajeAsistencia.
	 */
	public String getPorcentajeAsistencia() {
		return porcentajeAsistencia;
	}
	/**
	 * @param porcentajeAsistencia The porcentajeAsistencia to set.
	 */
	public void setPorcentajeAsistencia(String porcentajeAsistencia) {
		this.porcentajeAsistencia = porcentajeAsistencia;
	}
	
	public boolean isError(){
		try{
			if(Integer.parseInt(porcentajeAsistencia)<0 || Integer.parseInt(porcentajeAsistencia) > 100){
				return true;
			}
			return false;
		}catch(NumberFormatException exc){
			return true;
		}
	}
	
}
