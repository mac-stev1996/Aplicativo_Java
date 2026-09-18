package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class IndiceFinancieroReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String tipo;
	private String nombre;
	private String anioMenos5;
	private String anioMenos4;
	private String anioMenos3;
	private String anioMenos2;
	private String anioMenos1;

	public IndiceFinancieroReporteDTO(String tipo, String nombre, String anioMenos5, String anioMenos4,
			String anioMenos3, String anioMenos2, String anioMenos1) {
		this.tipo = tipo;
		this.nombre = nombre;
		this.anioMenos5 = anioMenos5;
		this.anioMenos4 = anioMenos4;
		this.anioMenos3 = anioMenos3;
		this.anioMenos2 = anioMenos2;
		this.anioMenos1 = anioMenos1;
	}

	public String getTipo() {
		return tipo;
	}

	public String getNombre() {
		return nombre;
	}

	public String getAnioMenos5() {
		return anioMenos5;
	}

	public String getAnioMenos4() {
		return anioMenos4;
	}

	public String getAnioMenos3() {
		return anioMenos3;
	}

	public String getAnioMenos2() {
		return anioMenos2;
	}

	public String getAnioMenos1() {
		return anioMenos1;
	}
}
