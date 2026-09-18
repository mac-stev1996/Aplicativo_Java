package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class ModeloCentricReglaReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fechaEvaluacion;
	private String reglaPadre;
	private String reglaUsada;
	private String valorObtenido;
	private String decisionRegla;

	public ModeloCentricReglaReporteDTO(String fechaEvaluacion, String reglaPadre, String reglaUsada,
			String valorObtenido, String decisionRegla) {
		this.fechaEvaluacion = fechaEvaluacion;
		this.reglaPadre = reglaPadre;
		this.reglaUsada = reglaUsada;
		this.valorObtenido = valorObtenido;
		this.decisionRegla = decisionRegla;
	}

	public String getFechaEvaluacion() {
		return fechaEvaluacion;
	}

	public String getReglaPadre() {
		return reglaPadre;
	}

	public String getReglaUsada() {
		return reglaUsada;
	}

	public String getValorObtenido() {
		return valorObtenido;
	}

	public String getDecisionRegla() {
		return decisionRegla;
	}
}
