package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class PersonaEmpresaReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String identificacion;
	private String nombre;
	private String detalle;

	public PersonaEmpresaReporteDTO(String identificacion, String nombre, String detalle) {
		this.identificacion = identificacion;
		this.nombre = nombre;
		this.detalle = detalle;
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDetalle() {
		return detalle;
	}
}
