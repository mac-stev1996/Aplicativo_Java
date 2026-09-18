package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class ContactoReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String telefono;
	private String ciudad;
	private String direccion;

	public ContactoReporteDTO(String telefono, String ciudad, String direccion) {
		this.telefono = telefono;
		this.ciudad = ciudad;
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getCiudad() {
		return ciudad;
	}

	public String getDireccion() {
		return direccion;
	}
}
