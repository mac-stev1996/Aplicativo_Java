package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class Consulta12MesesReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fechaConsulta;
	private String institucion;
	private String usuario;

	public Consulta12MesesReporteDTO(String fechaConsulta, String institucion, String usuario) {
		this.fechaConsulta = fechaConsulta;
		this.institucion = institucion;
		this.usuario = usuario;
	}

	public String getFechaConsulta() {
		return fechaConsulta;
	}

	public String getInstitucion() {
		return institucion;
	}

	public String getUsuario() {
		return usuario;
	}
}
