package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class RequestConsultaExterna implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String tipoCliente;
	private String identificacion;
	
	public String getTipoCliente() {
		return tipoCliente;
	}
	public void setTipoCliente(String tipoCliente) {
		this.tipoCliente = tipoCliente;
	}
	public String getIdentificacion() {
		return identificacion;
	}
	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

}
