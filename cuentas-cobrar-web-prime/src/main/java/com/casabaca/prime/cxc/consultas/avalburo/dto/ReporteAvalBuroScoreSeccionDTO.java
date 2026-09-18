package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class ReporteAvalBuroScoreSeccionDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String titulo;
	private String contenido;

	public ReporteAvalBuroScoreSeccionDTO() {
	}

	public ReporteAvalBuroScoreSeccionDTO(String titulo, String contenido) {
		this.titulo = titulo;
		this.contenido = contenido;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}
}
