package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;
import java.util.Date;

public class RespuestaWsAbalBuro implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String codigoMensaje;	
	private String nomMensaje;	
	private String descripcionMensaje;
	private String cedula;       
    private String nombre;        
    private String fechaInicio;        
    private String fechaFin;    
    private String usuario;
    private String json;
    private String resumen;
    
	public String getCodigoMensaje() {
		return codigoMensaje;
	}
	public void setCodigoMensaje(String codigoMensaje) {
		this.codigoMensaje = codigoMensaje;
	}
	public String getNomMensaje() {
		return nomMensaje;
	}
	public void setNomMensaje(String nomMensaje) {
		this.nomMensaje = nomMensaje;
	}
	public String getDescripcionMensaje() {
		return descripcionMensaje;
	}
	public void setDescripcionMensaje(String descripcionMensaje) {
		this.descripcionMensaje = descripcionMensaje;
	}
	public String getCedula() {
		return cedula;
	}
	public void setCedula(String cedula) {
		this.cedula = cedula;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}	
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
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
	public String getResumen() {
		return resumen;
	}
	public void setResumen(String resumen) {
		this.resumen = resumen;
	}
	 
	
}
