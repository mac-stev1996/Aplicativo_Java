package com.casabaca.prime.cxc.common.dialog.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;


import com.casabaca.common.ejb.dto.LineaNegocioLOVDto;
import com.casabaca.prime.cxc.common.CommonController;

@ManagedBean
@ViewScoped
public class dialogLineasNegocioController extends CommonController implements Serializable {
	
	
	private static final long serialVersionUID = -954621387752863876L;
	private LazyDataModelLineasNegocioLOV lazyDataModelLineasNegocioLOV;
	private LineaNegocioLOVDto lineaNegocioLOVDto;
	private String nombreDialog;
	private String aplicaDocTributario;
	private String noLineaAdicional;

	@PostConstruct
	public void init() {
		lineaNegocioLOVDto = new LineaNegocioLOVDto();
	}

	public void cargarLineasNegocioLOV() {
		setLazyDataModelLineasNegocioLOV(
				new LazyDataModelLineasNegocioLOV(getCompania().getNoCia(), aplicaDocTributario, noLineaAdicional));
	}

	public void seleccionarLineaNegocioLOV() {
		accionesDialog(this.nombreDialog, false);
	}

	public LazyDataModelLineasNegocioLOV getLazyDataModelLineasNegocioLOV() {
		return lazyDataModelLineasNegocioLOV;
	}

	public void setLazyDataModelLineasNegocioLOV(LazyDataModelLineasNegocioLOV lazyDataModelLineasNegocioLOV) {
		this.lazyDataModelLineasNegocioLOV = lazyDataModelLineasNegocioLOV;
	}

	public LineaNegocioLOVDto getLineaNegocioLOVDto() {
		return lineaNegocioLOVDto;
	}

	public void setLineaNegocioLOVDto(LineaNegocioLOVDto lineaNegocioLOVDto) {
		this.lineaNegocioLOVDto = lineaNegocioLOVDto;
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}

	public String getAplicaDocTributario() {
		return aplicaDocTributario;
	}

	public void setAplicaDocTributario(String aplicaDocTributario) {
		this.aplicaDocTributario = aplicaDocTributario;
	}

	public String getNoLineaAdicional() {
		return noLineaAdicional;
	}

	public void setNoLineaAdicional(String noLineaAdicional) {
		this.noLineaAdicional = noLineaAdicional;
	}

}
