package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.prime.cxc.common.CommonController;

@ManagedBean
@ViewScoped
public class CxcProBajaSaldosController extends CommonController implements Serializable{

	/**/

	private static final long serialVersionUID = -3775467433516137094L;
	static final Logger LOG = Logger.getLogger(CxcProBajaSaldosController.class);
	
	
	
	
	
	
	
	

	private String valor;
	private String tipoProceso;
	
	
	

	

	
	
	
	
	
	
	
	
	
	
	
	
	
	// Getter & Setter
	
	
	public String getTipoProceso() {
		return tipoProceso;
	}

	public void setTipoProceso(String tipoProceso) {
		this.tipoProceso = tipoProceso;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
	
	
	
	
	
}
