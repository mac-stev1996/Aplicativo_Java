package com.casabaca.prime.cxc.consultas.avalburo.dto;

public class CxcAvalBuroJson {
    
    
    private String cedula;       
    private String nombre;        
    private String fechaInicio;        
    private String fechaFin;    
    private String usuario;
    private String json;
    private String jsonV2;
    
	public CxcAvalBuroJson() {
		super();
	}

	public CxcAvalBuroJson(String cedula, String nombre, String fechaInicio, String fechaFin, String usuario, String json) {
		super();
		this.cedula = cedula;
		this.nombre = nombre;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.usuario = usuario;		
		this.json = json;
	}

	public CxcAvalBuroJson(String cedula, String nombre, String fechaInicio, String fechaFin, String usuario,
			String json, String jsonV2) {
		super();
		this.cedula = cedula;
		this.nombre = nombre;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.usuario = usuario;
		this.json = json;
		this.jsonV2 = jsonV2;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}
		
	public String getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(String fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public String getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(String fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}	

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public String getJsonV2() {
		return jsonV2;
	}

	public void setJsonV2(String jsonV2) {
		this.jsonV2 = jsonV2;
	}

	@Override
	public String toString() {
		return "CxcAvalBuro [cedula=" + cedula + ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin
				+ ", usuario=" + usuario + ", nombre=" + nombre + ", json=" + json + ", jsonV2(length)="
				+ (jsonV2 != null ? jsonV2.length() : 0) + "]";
	}
    
    
}