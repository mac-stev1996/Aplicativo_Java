package com.casabaca.prime.cxc.common;

public enum EnumEstSolicitud {

	INGRESADO("I"), 
	DEVUELTO("D"), 
	ENVIADO("E"), 
	REVISADO("R"),
	APROBADO("A"),
	NEGADO("N"),
	DEVUELTO_REVISION("V");
	
private final String codigo;
	
EnumEstSolicitud(String codigo){
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}
	
}
