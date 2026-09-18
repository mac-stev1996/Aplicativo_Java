package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class OfertaSugeridaReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String plazo;
	private String montoSugerido;
	private String cuotaSugerida;

	public OfertaSugeridaReporteDTO() {
	}

	public OfertaSugeridaReporteDTO(String plazo, String montoSugerido, String cuotaSugerida) {
		this.plazo = plazo;
		this.montoSugerido = montoSugerido;
		this.cuotaSugerida = cuotaSugerida;
	}

	public String getPlazo() {
		return plazo;
	}

	public void setPlazo(String plazo) {
		this.plazo = plazo;
	}

	public String getMontoSugerido() {
		return montoSugerido;
	}

	public void setMontoSugerido(String montoSugerido) {
		this.montoSugerido = montoSugerido;
	}

	public String getCuotaSugerida() {
		return cuotaSugerida;
	}

	public void setCuotaSugerida(String cuotaSugerida) {
		this.cuotaSugerida = cuotaSugerida;
	}
}
