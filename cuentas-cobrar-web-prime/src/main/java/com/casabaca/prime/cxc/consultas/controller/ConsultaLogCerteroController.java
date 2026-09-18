/* 
 * ConsultaLogCerteroController.java 
 * Jun 5, 2023
 * Copyright 2023 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;



import com.casabaca.common.StringUtils;
import com.casabaca.common.ejb.model.WsRegistrosPagos;
import com.casabaca.common.ejb.service.WsRegistrosPagosServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * <b> Controlador para revisar tabla WS_REGISTROS_PAGOS Certero </b>
 * 
 * @author jorge.reyes
 * @version $1.0$
 */
@ViewScoped
@ManagedBean
public class ConsultaLogCerteroController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2248697375792704396L;

	@EJB(lookup = NombreJNDI.WS_REGISTROS_PAGOS)
	private WsRegistrosPagosServiceLocal wsRegistrosPagosServiceLocal;

	private List<WsRegistrosPagos> listadoConsultado;

	private WsRegistrosPagos wsRegistroPagosConsultado;

	private String noContrato;
	private String estado;

	@PostConstruct
	public void init() {

		listadoConsultado = new ArrayList<WsRegistrosPagos>();
		noContrato = null;
		estado = null;
		wsRegistroPagosConsultado = new WsRegistrosPagos();
	}

	public void buscar() {
		if (noContrato != null && estado != null) {
			listadoConsultado = wsRegistrosPagosServiceLocal.buscarRegistrosPorContrato(getCompania().getNoCia(),
					noContrato, estado);
		} else {
			error("Debse Ingresar un contrato y Seleccionar el campo Estado ");
		}

	}

	public void detalle(WsRegistrosPagos wsRegistrosPagosEditado) {
		wsRegistroPagosConsultado = wsRegistrosPagosEditado;

		wsRegistroPagosConsultado.setPeticion(StringUtils.formatearjson(wsRegistroPagosConsultado.getPeticion()));

		accionesDialog("detRecibido", Boolean.TRUE);
	}

	public List<WsRegistrosPagos> getListadoConsultado() {
		return listadoConsultado;
	}

	public void setListadoConsultado(List<WsRegistrosPagos> listadoConsultado) {
		this.listadoConsultado = listadoConsultado;
	}

	public String getNoContrato() {
		return noContrato;
	}

	public void setNoContrato(String noContrato) {
		this.noContrato = noContrato;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public WsRegistrosPagos getWsRegistroPagosConsultado() {
		return wsRegistroPagosConsultado;
	}

	public void setWsRegistroPagosConsultado(WsRegistrosPagos wsRegistroPagosConsultado) {
		this.wsRegistroPagosConsultado = wsRegistroPagosConsultado;
	}

}
