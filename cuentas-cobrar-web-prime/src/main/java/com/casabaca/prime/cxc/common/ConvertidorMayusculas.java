package com.casabaca.prime.cxc.common;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * 
 * Esta clase permite convertidor una cadena a letras mayúsculas
 *
 *
 */
@FacesConverter("convMay")
public class ConvertidorMayusculas implements Converter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext ,
	 * javax.faces.component.UIComponent, java.lang.String)
	 */
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String obj) throws ConverterException {
		String cadenaUper = null;
		if (obj != null) {
			cadenaUper = obj.toUpperCase();
		}
		return cadenaUper.replaceAll("[\n\r]", "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext ,
	 * javax.faces.component.UIComponent, java.lang.Object)
	 */
	public String getAsString(FacesContext arg0, UIComponent arg1, Object obj) throws ConverterException {
		String cadenaUper = null;
		if (obj != null) {
			cadenaUper = obj.toString().toUpperCase();
		}
		return cadenaUper.replaceAll("[\n\r]", "");
	}

}
