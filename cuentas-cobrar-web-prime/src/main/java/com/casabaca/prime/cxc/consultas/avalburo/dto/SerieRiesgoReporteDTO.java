package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class SerieRiesgoReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fechaCorte;
	private String valorPrincipal;
	private String valorSecundario;
	private Double valorPrincipalNumerico;
	private Double valorSecundarioNumerico;
	private String nivel;
	private String color;

	public SerieRiesgoReporteDTO(String fechaCorte, String valorPrincipal, String valorSecundario,
			Double valorPrincipalNumerico, Double valorSecundarioNumerico, String nivel, String color) {
		this.fechaCorte = fechaCorte;
		this.valorPrincipal = valorPrincipal;
		this.valorSecundario = valorSecundario;
		this.valorPrincipalNumerico = valorPrincipalNumerico;
		this.valorSecundarioNumerico = valorSecundarioNumerico;
		this.nivel = nivel;
		this.color = color;
	}

	public String getFechaCorte() {
		return fechaCorte;
	}

	public String getValorPrincipal() {
		return valorPrincipal;
	}

	public String getValorSecundario() {
		return valorSecundario;
	}

	public Double getValorPrincipalNumerico() {
		return valorPrincipalNumerico;
	}

	public Double getValorSecundarioNumerico() {
		return valorSecundarioNumerico;
	}

	public String getNivel() {
		return nivel;
	}

	public String getColor() {
		return color;
	}
}
