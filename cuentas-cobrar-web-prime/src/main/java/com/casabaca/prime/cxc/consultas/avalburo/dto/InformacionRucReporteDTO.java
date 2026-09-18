package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class InformacionRucReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String identificacion;
	private String razonSocial;
	private String tipoRelacion;

	public InformacionRucReporteDTO(String identificacion, String razonSocial, String tipoRelacion) {
		this.identificacion = identificacion;
		this.razonSocial = razonSocial;
		this.tipoRelacion = tipoRelacion;
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public String getTipoRelacion() {
		return tipoRelacion;
	}
}
