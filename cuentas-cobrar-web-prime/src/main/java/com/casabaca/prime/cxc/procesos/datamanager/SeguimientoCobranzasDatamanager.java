package com.casabaca.prime.cxc.procesos.datamanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * 
 */
@ManagedBean(name = "seguimientoCobranzasDatamanager")
@SessionScoped
public class SeguimientoCobranzasDatamanager implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private List<Object[]> listadoCarteraConsulta;

	public SeguimientoCobranzasDatamanager() {
		listadoCarteraConsulta = new ArrayList<Object[]>();

	}

	public List<Object[]> getListadoCarteraConsulta() {
		return listadoCarteraConsulta;
	}

	public void setListadoCarteraConsulta(List<Object[]> listadoCarteraConsulta) {
		this.listadoCarteraConsulta = listadoCarteraConsulta;
	}

}
