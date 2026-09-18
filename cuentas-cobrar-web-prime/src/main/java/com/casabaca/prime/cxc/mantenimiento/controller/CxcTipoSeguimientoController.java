package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcTipoSeguimiento;
import com.casabaca.cxc.ejb.modelo.CxcTipoSeguimientoPK;
import com.casabaca.cxc.ejb.servicio.CxcTipoSeguimientoServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;

import org.apache.log4j.Logger;

/**
 * author: jl_reyes
 */
@ManagedBean
@ViewScoped
public class CxcTipoSeguimientoController extends CommonController implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5619452697193283059L;

	private static final Logger LOG = Logger.getLogger(CxcTipoSeguimientoController.class);

	@EJB(lookup = NombreJNDI.CXC_TIPO_SEGUIMIENTO_SERVICE)
	private CxcTipoSeguimientoServiceLocal cxcTipoSeguimientoServiceLocal;

	private List<CxcTipoSeguimiento> listadoTipoSeguimiento;
	private CxcTipoSeguimiento cxcTipoSeguimientoNuevo;

	@PostConstruct
	public void init() {
		listadoTipoSeguimiento = new ArrayList<CxcTipoSeguimiento>();
		cxcTipoSeguimientoNuevo = new CxcTipoSeguimiento(new CxcTipoSeguimientoPK(0L, getCompania().getNoCia()));
		listadoTipoSeguimiento = cxcTipoSeguimientoServiceLocal.obtenerPorNocia(getCompania().getNoCia());

	}

	public void nuevo() {
		cxcTipoSeguimientoNuevo = new CxcTipoSeguimiento(new CxcTipoSeguimientoPK(0L, getCompania().getNoCia()));
		accionesDialog("dlgNuevo", Boolean.TRUE);

	}

	public void actualizar(CxcTipoSeguimiento cxcTipoSeguimientoSeleted) {
		try {
			cxcTipoSeguimientoServiceLocal.actualizar(cxcTipoSeguimientoSeleted);
			info("Registro Actualizado con Exito !!!");
			init();
		} catch (UpdateException e) {
			error("Ocurrio un error al Actualizar Registro!!!");
			LOG.error(e);
		}
	}

	public void grabar() {
		try {
			cxcTipoSeguimientoServiceLocal.insertar(cxcTipoSeguimientoNuevo);
			accionesDialog("dlgNuevo", Boolean.FALSE);
			info("Registro Insertado con Exito !!!");
			init();
		} catch (InsertException e) {
			error("Ocurrio un error al Insertar Registro!!!");
			LOG.error(e);
		}

	}

	public List<CxcTipoSeguimiento> getListadoTipoSeguimiento() {
		return listadoTipoSeguimiento;
	}

	public void setListadoTipoSeguimiento(List<CxcTipoSeguimiento> listadoTipoSeguimiento) {
		this.listadoTipoSeguimiento = listadoTipoSeguimiento;
	}

	public CxcTipoSeguimiento getCxcTipoSeguimientoNuevo() {
		return cxcTipoSeguimientoNuevo;
	}

	public void setCxcTipoSeguimientoNuevo(CxcTipoSeguimiento cxcTipoSeguimientoNuevo) {
		this.cxcTipoSeguimientoNuevo = cxcTipoSeguimientoNuevo;
	}

}
