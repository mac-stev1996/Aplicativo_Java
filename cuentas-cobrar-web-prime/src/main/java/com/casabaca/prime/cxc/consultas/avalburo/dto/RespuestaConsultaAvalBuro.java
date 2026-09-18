package com.casabaca.prime.cxc.consultas.avalburo.dto;

public class RespuestaConsultaAvalBuro {
    
    
    private String cedula;       
    private String nombre;        
    private String fechaInicio;        
    private String fechaFin;    
    private String usuario;
    private String json;
    
	public RespuestaConsultaAvalBuro() {
		super();
	}

	public RespuestaConsultaAvalBuro(String cedula, String nombre, String fechaInicio, String fechaFin, String usuario, String json) {
		super();
		this.cedula = cedula;
		this.nombre = nombre;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.usuario = usuario;		
		this.json = json;
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

	@Override
	public String toString() {
		return "CxcAvalBuro [cedula=" + cedula + ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin
				+ ", usuario=" + usuario + ", nombre=" + nombre + ", json=" + json + "]";
	}
    
    
}