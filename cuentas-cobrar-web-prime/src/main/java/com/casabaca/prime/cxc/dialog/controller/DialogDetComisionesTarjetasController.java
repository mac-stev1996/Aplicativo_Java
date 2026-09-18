/**
 * 
 */
package com.casabaca.prime.cxc.dialog.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.cxc.ejb.dto.CxcDetComisionTarjetaDto;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.lazy.LazyDataModelComisionesTc;

/**
 * 
 * @author Roberto Guizado
 *
 */
@ViewScoped
@ManagedBean
public class DialogDetComisionesTarjetasController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4700955433650123223L;
	private LazyDataModelComisionesTc lazyDataModelComisionesTc;
	private CxcDetComisionTarjetaDto cxcDetComisionTarjetaDto;
	private String nombreDialog;

	@PostConstruct
	public void init() {
		setLazyDataModelComisionesTc(new LazyDataModelComisionesTc(0, 10, getCompania().getNoCia()));
		setCxcDetComisionTarjetaDto(new CxcDetComisionTarjetaDto());
	}

	public void seleccionarUsuario() {
		accionesDialog(this.nombreDialog, false);
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}

	public LazyDataModelComisionesTc getLazyDataModelComisionesTc() {
		return lazyDataModelComisionesTc;
	}

	public void setLazyDataModelComisionesTc(LazyDataModelComisionesTc lazyDataModelComisionesTc) {
		this.lazyDataModelComisionesTc = lazyDataModelComisionesTc;
	}

	public CxcDetComisionTarjetaDto getCxcDetComisionTarjetaDto() {
		return cxcDetComisionTarjetaDto;
	}

	public void setCxcDetComisionTarjetaDto(CxcDetComisionTarjetaDto cxcDetComisionTarjetaDto) {
		this.cxcDetComisionTarjetaDto = cxcDetComisionTarjetaDto;
	}

}
