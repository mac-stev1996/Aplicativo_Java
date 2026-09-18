/**
 * 
 */
package com.casabaca.prime.cxc.common.dialog.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.administration.ejb.dto.UsuarioDto;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelUsuarios;

/**
 * 
 * @author Roberto Guizado
 *
 */
@ViewScoped
@ManagedBean
public class DialogUsuariosController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4700955433650123223L;
	private LazyDataModelUsuarios lazyDataModelUsuarios;
	private UsuarioDto usuarioDto;
	private String nombreDialog;

	@PostConstruct
	public void init() {
		setUsuarioDto(new UsuarioDto());
	}

	public void cargarUsuarios() {
		setLazyDataModelUsuarios(new LazyDataModelUsuarios(0, 10, getCompania().getNoCia()));
	}

	public void seleccionarUsuario() {
		accionesDialog(this.nombreDialog, false);
	}

	public LazyDataModelUsuarios getLazyDataModelUsuarios() {
		return lazyDataModelUsuarios;
	}

	public void setLazyDataModelUsuarios(LazyDataModelUsuarios lazyDataModelUsuarios) {
		this.lazyDataModelUsuarios = lazyDataModelUsuarios;
	}

	public UsuarioDto getUsuarioDto() {
		return usuarioDto;
	}

	public void setUsuarioDto(UsuarioDto usuarioDto) {
		this.usuarioDto = usuarioDto;
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}

}
