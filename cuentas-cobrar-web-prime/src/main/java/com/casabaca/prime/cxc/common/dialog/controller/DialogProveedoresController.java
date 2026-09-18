/**
 * 
 */
package com.casabaca.prime.cxc.common.dialog.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.model.Proveedor;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelProveedores;


/**
 * @author is_delacruz
 *
 */
@ViewScoped
@ManagedBean(name="dialogProveedoresController")
public class DialogProveedoresController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private LazyDataModelProveedores LazyDataModelProveedores;
	private String nombreDialog;
	private Proveedor proveedor;
	
	@PostConstruct
	public void init() {
		LazyDataModelProveedores = new LazyDataModelProveedores(0, 10, getCompania().getNoCia());
	}
	
	public void seleccionarProveedor() {
		accionesDialog(this.nombreDialog, false);
	}

	public LazyDataModelProveedores getLazyDataModelProveedores() {
		return LazyDataModelProveedores;
	}

	public void setLazyDataModelProveedores(LazyDataModelProveedores lazyDataModelProveedores) {
		LazyDataModelProveedores = lazyDataModelProveedores;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}
	
	

}
