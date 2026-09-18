package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcResultadoGestion;
import com.casabaca.cxc.ejb.modelo.CxcResultadoGestionPK;
import com.casabaca.cxc.ejb.servicio.CxcResultadoGestionServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author jreyes
 *
 */
@ViewScoped
@ManagedBean
public class CxcResultadoGestionController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8006964925134274835L;
	private static final Logger LOG = Logger.getLogger(CxcResultadoGestionController.class);

	@EJB(lookup = NombreJNDI.CXC_RESULTADO_GESTION_SERVICE)
	private CxcResultadoGestionServiceLocal cxcResultadoGestionServiceLocal;

	private List<CxcResultadoGestion> listadoResultadoGestion;
	private CxcResultadoGestion cxcResultadoGestionNuevo;

	@PostConstruct
	private void init() {
		listadoResultadoGestion = new ArrayList<CxcResultadoGestion>();

		listadoResultadoGestion = cxcResultadoGestionServiceLocal.obtenerNocia(getCompania().getNoCia());

	}

	public void abrirNuevo() {
		cxcResultadoGestionNuevo = new CxcResultadoGestion(new CxcResultadoGestionPK(getCompania().getNoCia(), null));

		accionesDialog("dlgNuevo", Boolean.TRUE);

	}

	public void editar(CxcResultadoGestion cxcResultadoGestionEditado) {
		try {
			cxcResultadoGestionServiceLocal.actualizar(cxcResultadoGestionEditado);
			info("Registro Actualizado con Exito");
			init();
		} catch (UpdateException e) {
			error("Ocurrio un error al Actualizar Registro" + e.getCause());
			LOG.error(e);
		}

	}

	public void grabar() {

		try {
			cxcResultadoGestionServiceLocal.insertar(cxcResultadoGestionNuevo);
			info("Registro Insertado con Exito");
			accionesDialog("dlgNuevo", Boolean.FALSE);
			init();
		} catch (InsertException e) {
			error("Ocurrio un error al insertar Registro" + e.getCause());
			LOG.error(e);
		}

	}

	public List<CxcResultadoGestion> getListadoResultadoGestion() {
		return listadoResultadoGestion;
	}

	public void setListadoResultadoGestion(List<CxcResultadoGestion> listadoResultadoGestion) {
		this.listadoResultadoGestion = listadoResultadoGestion;
	}

	public CxcResultadoGestion getCxcResultadoGestionNuevo() {
		return cxcResultadoGestionNuevo;
	}

	public void setCxcResultadoGestionNuevo(CxcResultadoGestion cxcResultadoGestionNuevo) {
		this.cxcResultadoGestionNuevo = cxcResultadoGestionNuevo;
	}

}
