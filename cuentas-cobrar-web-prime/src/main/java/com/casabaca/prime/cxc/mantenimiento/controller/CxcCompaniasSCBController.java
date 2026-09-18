package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.FacCompania;
/**
 * @author jl_reyes
 *
 */
import com.casabaca.s3s.ejb.service.FacCompaniaServiceLocal;

@ManagedBean
@ViewScoped
public class CxcCompaniasSCBController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7399772167925210962L;

	@EJB(lookup = NombreJNDI.FAC_COMPANIA_SERVICE)
	private FacCompaniaServiceLocal facCompaniaServiceLocal;

	private List<FacCompania> listadoCompanias;
	
	private FacCompania facCompaniaSelected;

	@PostConstruct
	public void init() {
		listadoCompanias = new ArrayList<FacCompania>();

		listadoCompanias = facCompaniaServiceLocal.obtenerTodas();

	}

	public void actualizar(FacCompania facCompania) {

		try {

				facCompaniaServiceLocal.actualizar(facCompania);

			info("Registro Actualizado Con Exito");

		} catch (Exception e) {
			error("Ocurrio un Error al Actualizar Registro");
		}

	}

	public List<FacCompania> getListadoCompanias() {
		return listadoCompanias;
	}

	public void setListadoCompanias(List<FacCompania> listadoCompanias) {
		this.listadoCompanias = listadoCompanias;
	}

	public FacCompania getFacCompaniaSelected() {
		return facCompaniaSelected;
	}

	public void setFacCompaniaSelected(FacCompania facCompaniaSelected) {
		this.facCompaniaSelected = facCompaniaSelected;
	}
	
	


}
