/* 
 * CargaArchivoProcesoDto.java 
 * 7 jun. 2024
 * Copyright 2024 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.InputStream;

import org.primefaces.model.UploadedFile;

/**
 * <b> Descripcion de la clase, interface o enumeracion. </b>
 * @author laura.llangari
 *
 * @version $1.0$
 */
public class CargaArchivoProcesoDto {

	
	private UploadedFile uf;
	private InputStream is1;
	
	
	
	public CargaArchivoProcesoDto() {
		super();
	}

	public CargaArchivoProcesoDto(UploadedFile uf, InputStream is1) {
		super();
		this.uf = uf;
		this.is1 = is1;
	}
	
	public UploadedFile getUf() {
		return uf;
	}
	public void setUf(UploadedFile uf) {
		this.uf = uf;
	}
	public InputStream getIs1() {
		return is1;
	}
	public void setIs1(InputStream is1) {
		this.is1 = is1;
	}		

}
