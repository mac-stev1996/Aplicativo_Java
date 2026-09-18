package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class ContactabilidadReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String provincia;
	private String ciudad;
	private String telefono;
	private String email;
	private String direccion;
	private String paginaWeb;

	public ContactabilidadReporteDTO(String provincia, String ciudad, String telefono, String email, String direccion,
			String paginaWeb) {
		this.provincia = provincia;
		this.ciudad = ciudad;
		this.telefono = telefono;
		this.email = email;
		this.direccion = direccion;
		this.paginaWeb = paginaWeb;
	}

	public String getProvincia() {
		return provincia;
	}

	public String getCiudad() {
		return ciudad;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getEmail() {
		return email;
	}

	public String getDireccion() {
		return direccion;
	}

	public String getPaginaWeb() {
		return paginaWeb;
	}
}
