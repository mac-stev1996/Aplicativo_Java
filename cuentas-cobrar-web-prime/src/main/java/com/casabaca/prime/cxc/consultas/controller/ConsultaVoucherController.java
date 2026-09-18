/* 
 * ConsultaVoucherController.java 
 * Dec 5, 2023
 * Copyright 2023 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;

/**
 * <b> Controlador para manejo de consulta de vouchers </b>
 * 
 * @author jorge.reyes
 * @version $1.0$
 */
@ViewScoped
@ManagedBean
public class ConsultaVoucherController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3664782399413972602L;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal cxcNativeServiceLocal;

	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	private List<Object[]> listadoVouchers;
	private Cliente cliente;
	private String cedula;
	private String noRecap;
	private String noVoucher;
	private Date fechaInicio;
	private Date fechaFin;

	@PostConstruct
	public void init() {
		listadoVouchers = new ArrayList<Object[]>();
		cedula = null;
		noRecap = null; 
		noVoucher = null;
		cliente = null;
		fechaInicio = null;
		fechaFin = null;

	}

	public void buscar() {

		listadoVouchers = cxcNativeServiceLocal.consultarVoucher(getCompania().getNoCia(), fechaInicio, fechaFin,
				noVoucher, cedula, noRecap);

	}

	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);
	}

	public List<Object[]> getListadoVouchers() {
		return listadoVouchers;
	}

	public void setListadoVouchers(List<Object[]> listadoVouchers) {
		this.listadoVouchers = listadoVouchers;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public String getNoRecap() {
		return noRecap;
	}

	public void setNoRecap(String noRecap) {
		this.noRecap = noRecap;
	}

	public String getNoVoucher() {
		return noVoucher;
	}

	public void setNoVoucher(String noVoucher) {
		this.noVoucher = noVoucher;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Cliente getCliente() {
		if (dialogClientesController.getCliente() != null
				&& dialogClientesController.getCliente().getClientePK() != null
				&& dialogClientesController.getCliente().getClientePK().getNoCliente() != null) {

			cliente = dialogClientesController.getCliente();
			cedula = cliente.getCedula();
			dialogClientesController.setCliente(null);
		}
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}

	public void setDialogClientesController(DialogClientesController dialogClientesController) {
		this.dialogClientesController = dialogClientesController;
	}

}
