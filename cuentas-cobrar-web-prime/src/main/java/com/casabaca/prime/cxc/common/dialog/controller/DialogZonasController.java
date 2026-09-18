package com.casabaca.prime.cxc.common.dialog.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.cxc.ejb.dto.ZonasDto;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyZonasDataModel;

/**
 * @author jreyes
 *
 */
@ViewScoped
@ManagedBean
public class DialogZonasController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -44333600875555112L;
	private LazyZonasDataModel lazyZonasDataModel;
	private ZonasDto zonasDto;
	private String nombreDialog;

	@PostConstruct
	public void init() {
		zonasDto = new ZonasDto();
	}

	public void cargarZonas() {
		lazyZonasDataModel = new LazyZonasDataModel(0, 10, getCompania().getNoCia());
	}

	public void seleccionarZona() {
		accionesDialog(nombreDialog, false);
	}

	public LazyZonasDataModel getLazyZonasDataModel() {
		return lazyZonasDataModel;
	}

	public void setLazyZonasDataModel(LazyZonasDataModel lazyZonasDataModel) {
		this.lazyZonasDataModel = lazyZonasDataModel;
	}

	public ZonasDto getZonasDto() {
		return zonasDto;
	}

	public void setZonasDto(ZonasDto zonasDto) {
		this.zonasDto = zonasDto;
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}

}