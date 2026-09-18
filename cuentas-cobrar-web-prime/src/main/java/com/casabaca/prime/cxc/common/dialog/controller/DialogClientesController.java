package com.casabaca.prime.cxc.common.dialog.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;

/**
 * @author Roberto Guizado
 */
@ViewScoped
@ManagedBean
public class DialogClientesController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -44333600875555112L;
	private LazyDataModelClientes lazyDataModelClientes;
	private Cliente cliente;
	private String nombreDialog;

	@PostConstruct
	public void init() {
		cliente = new Cliente();
	}

	public void cargarClientes() {
		lazyDataModelClientes = new LazyDataModelClientes(0, 10, getCompania().getNoCia());
	}

	public void seleccionarCliente() {
		accionesDialog(nombreDialog, false);
	}

	public LazyDataModelClientes getLazyDataModelClientes() {
		return lazyDataModelClientes;
	}

	public void setLazyDataModelClientes(LazyDataModelClientes lazyDataModelClientes) {
		this.lazyDataModelClientes = lazyDataModelClientes;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}

}