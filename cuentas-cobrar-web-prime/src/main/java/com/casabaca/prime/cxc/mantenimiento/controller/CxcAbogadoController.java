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
import com.casabaca.cxc.ejb.modelo.CxcAbogado;
import com.casabaca.cxc.ejb.modelo.CxcAbogadoPK;
import com.casabaca.cxc.ejb.servicio.CxcAbogadoServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author jl_reyes
 *
 */
@ManagedBean
@ViewScoped
public class CxcAbogadoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2317754047097877562L;

	private static final Logger LOG = Logger.getLogger(CxcAbogadoController.class);

	@EJB(lookup = NombreJNDI.CXC_ABOGADO_SERVICE)
	private CxcAbogadoServiceLocal cxcAbogadoServiceLocal;

	private List<CxcAbogado> listadoAbogado;
	private CxcAbogado cxcAbogadoNuevo;
	private CxcAbogado cxcAbogadoSelected;

	@PostConstruct
	public void init() {

		listadoAbogado = new ArrayList<CxcAbogado>();
		cxcAbogadoNuevo = new CxcAbogado(new CxcAbogadoPK(getCompania().getNoCia(), null));
		cxcAbogadoSelected = new CxcAbogado();

		listadoAbogado = cxcAbogadoServiceLocal.buscarPorNoCia(getCompania().getNoCia());

	}

	public void grabar() {

		try {

			if (cxcAbogadoServiceLocal.validarExite(cxcAbogadoNuevo.getId().getNoCia(),
					cxcAbogadoNuevo.getId().getCodigo()) == BigDecimal.ZERO) {

				cxcAbogadoServiceLocal.insertar(cxcAbogadoNuevo);

				info("Registro Almacenado Con Exito");

				init();

			} else {

				error("Datos ingresados para la Compania ya Existen, Codigo Abogado: "
						+ cxcAbogadoNuevo.getId().getCodigo());
				init();
			}

		} catch (Exception e) {
			LOG.error(e);
			error("Ocurrio un Error al Grabar Registro");
		}

	}

	public void actualizar(CxcAbogado cxcAbogado) {

		try {

			cxcAbogadoServiceLocal.actualizar(cxcAbogado);
			init();

			info("Registro Actualizado Con Exito");

		} catch (Exception e) {
			error("Ocurrio un Error al Actualizar Registro");
		}

	}

	public void eliminar(CxcAbogado cxcAbogado) {

		try {

			cxcAbogadoServiceLocal.borrar(cxcAbogado);

			info("Registro Borrado Con Exito");

			init();

		} catch (Exception e) {
			error("Ocurrio un Error al Borrar Registro");
		}

	}

	public List<CxcAbogado> getListadoAbogado() {
		return listadoAbogado;
	}

	public void setListadoAbogado(List<CxcAbogado> listadoAbogado) {
		this.listadoAbogado = listadoAbogado;
	}

	public CxcAbogado getCxcAbogadoNuevo() {
		return cxcAbogadoNuevo;
	}

	public void setCxcAbogadoNuevo(CxcAbogado cxcAbogadoNuevo) {
		this.cxcAbogadoNuevo = cxcAbogadoNuevo;
	}

	public CxcAbogado getCxcAbogadoSelected() {
		return cxcAbogadoSelected;
	}

	public void setCxcAbogadoSelected(CxcAbogado cxcAbogadoSelected) {
		this.cxcAbogadoSelected = cxcAbogadoSelected;
	}
}
