package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class DetalleEmpresaReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String campo;
	private String valor;

	public DetalleEmpresaReporteDTO(String campo, String valor) {
		this.campo = campo;
		this.valor = valor;
	}

	public String getCampo() {
		return campo;
	}

	public String getValor() {
		return valor;
	}
}
