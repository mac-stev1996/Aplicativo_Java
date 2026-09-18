package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.Arcctc;
import com.casabaca.cxc.ejb.modelo.ArcctcPK;
import com.casabaca.cxc.ejb.servicio.ArcctcServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

@ManagedBean
@ViewScoped
public class CxcTipoClienteController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2317754047097877562L;

	private static final Logger LOG = Logger.getLogger(CxcTipoClienteController.class);

	@EJB(lookup = NombreJNDI.CXC_ARCCTC_SERVICE)
	private ArcctcServiceLocal arcctcServiceLocal;

	private List<Arcctc> listadoArcctc;
	private Arcctc arcctcNuevo;
	private Arcctc arcctcSelected;

	@PostConstruct
	public void init() {

		listadoArcctc = new ArrayList<Arcctc>();
		arcctcNuevo = new Arcctc(new ArcctcPK(getCompania().getNoCia(), null));
		arcctcSelected = new Arcctc();

		listadoArcctc = arcctcServiceLocal.buscarPorNoCia(getCompania().getNoCia());

	}

	public void grabar() {

		try {

			if (arcctcServiceLocal.validarExite(arcctcNuevo.getId().getNoCia(),
					arcctcNuevo.getId().getTipoCliente()) == BigDecimal.ZERO) {

				arcctcServiceLocal.insertar(arcctcNuevo);

				info("Registro Almacenado Con Exito");

				init();

			} else {

				error("Datos ingresados para la Compania ya Existen, Tipo Cliente: "
						+ arcctcNuevo.getId().getTipoCliente());
				init();
			}

		} catch (Exception e) {
			LOG.error(e);
			error("Ocurrio un Error al Grabar Registro");
		}

	}

	public void actualizar(Arcctc arcctc) {

		try {

			arcctcServiceLocal.actualizar(arcctc);
			init();

			info("Registro Actualizado Con Exito");

		} catch (Exception e) {
			error("Ocurrio un Error al Actualizar Registro");
		}

	}

	public void eliminar(Arcctc arcctc) {

		try {

			arcctcServiceLocal.borrar(arcctc);

			info("Registro Borrado Con Exito");
			arcctcSelected = new Arcctc();

			init();

		} catch (Exception e) {
			error("Ocurrio un Error al Borrar Registro");
		}

	}

	public List<Arcctc> getListadoArcctc() {
		return listadoArcctc;
	}

	public void setListadoArcctc(List<Arcctc> listadoArcctc) {
		this.listadoArcctc = listadoArcctc;
	}

	public Arcctc getArcctcNuevo() {
		return arcctcNuevo;
	}

	public void setArcctcNuevo(Arcctc arcctcNuevo) {
		this.arcctcNuevo = arcctcNuevo;
	}

	public Arcctc getArcctcSelected() {
		return arcctcSelected;
	}

	public void setArcctcSelected(Arcctc arcctcSelected) {
		this.arcctcSelected = arcctcSelected;
	}

}
