package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class FactorScoreReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String factor;
	private String valor;
	private String efecto;

	public FactorScoreReporteDTO() {
	}

	public FactorScoreReporteDTO(String factor, String valor, String efecto) {
		this.factor = factor;
		this.valor = valor;
		this.efecto = efecto;
	}

	public String getFactor() {
		return factor;
	}

	public void setFactor(String factor) {
		this.factor = factor;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getEfecto() {
		return efecto;
	}

	public void setEfecto(String efecto) {
		this.efecto = efecto;
	}
}
