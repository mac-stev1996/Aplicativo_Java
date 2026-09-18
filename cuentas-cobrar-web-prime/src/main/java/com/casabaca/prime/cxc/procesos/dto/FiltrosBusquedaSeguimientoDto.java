/* 
 * FiltrosBusquedaExcepcionesDto.java 
 * 12 jul. 2024
 * Copyright 2024 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * <b> Clase de almacenamiento de variables (filtros de búsqueda) para la visualización de datos en la gestión de
 * excepciones. </b>
 * 
 * @author darwin.aldas
 * @version $1.0$
 */
public class FiltrosBusquedaSeguimientoDto implements Serializable{
	private static final long serialVersionUID = 1L;
	private Date fechaIni;
	private Date fechaFin;
	 
	private boolean actGuardar;
	private boolean mostrarCamposPlaca;
	
	public Date getFechaIni() {
		return fechaIni;
	}
	public void setFechaIni(Date fechaIni) {
		this.fechaIni = fechaIni;
	}
	public Date getFechaFin() {
		return fechaFin;
	}
	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}
	public boolean isActGuardar() {
		return actGuardar;
	}
	public void setActGuardar(boolean actGuardar) {
		this.actGuardar = actGuardar;
	}
	public boolean isMostrarCamposPlaca() {
		return mostrarCamposPlaca;
	}
	public void setMostrarCamposPlaca(boolean mostrarCamposPlaca) {
		this.mostrarCamposPlaca = mostrarCamposPlaca;
	}
	 
	
}
