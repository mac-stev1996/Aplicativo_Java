package com.casabaca.prime.cxc.common.dialog.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.model.ConPlantas;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelPlanCuentas;

/**
 * 
 * @author Roberto Guizado
 *
 */
@ViewScoped
@ManagedBean(name = "dialogPlanCuentasController")
public class DialogPlanCuentasController extends CommonController implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4627569399974490953L;
	
	private LazyDataModelPlanCuentas lazyDataModelPlanCuentas;
	private ConPlantas conPlantas;
	private String nombreDialog;

	@PostConstruct
	public void init() {
		lazyDataModelPlanCuentas = new LazyDataModelPlanCuentas(0, 10, getCompania().getNoCia());
		conPlantas = new ConPlantas();
	}

	public void seleccionarCuenta() {
		accionesDialog(this.nombreDialog, false);
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}

	public LazyDataModelPlanCuentas getLazyDataModelPlanCuentas() {
		return lazyDataModelPlanCuentas;
	}

	public void setLazyDataModelPlanCuentas(LazyDataModelPlanCuentas lazyDataModelPlanCuentas) {
		this.lazyDataModelPlanCuentas = lazyDataModelPlanCuentas;
	}

	public ConPlantas getConPlantas() {
		return conPlantas;
	}

	public void setConPlantas(ConPlantas conPlantas) {
		this.conPlantas = conPlantas;
	}

}