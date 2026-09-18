package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcInteresMoraDia;
import com.casabaca.cxc.ejb.modelo.CxcInteresMoraDiaPK;
import com.casabaca.cxc.ejb.servicio.CxcInteresMoraDiaServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * Controlador para CRUD CXC_INTERES_MORA_DIAS
 * 
 * @author jl_reyes
 *
 */
@ManagedBean
@ViewScoped
public class CxcInteresMoraDiaController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6821702734881082442L;
	static final Logger LOG = Logger.getLogger(CxcInteresMoraDiaController.class);

	@EJB(lookup = NombreJNDI.CXC_INTERES_MORA_DIA_SERVICE)
	private CxcInteresMoraDiaServiceLocal cxcInteresMoraDiaServiceLocal;

	private List<CxcInteresMoraDia> listado;
	private CxcInteresMoraDia cxcInteresMoraDiaNuevo;
	private CxcInteresMoraDia cxcInteresMoraDiaSeleted;

	@PostConstruct
	public void init() {

		listado = new ArrayList<CxcInteresMoraDia>();
		cxcInteresMoraDiaNuevo = new CxcInteresMoraDia(new CxcInteresMoraDiaPK(getCompania().getNoCia(), 0l, 0l));
		cxcInteresMoraDiaNuevo.setPorcentaje(BigDecimal.ZERO);
		cxcInteresMoraDiaSeleted = new CxcInteresMoraDia();

		listado = cxcInteresMoraDiaServiceLocal.buscarPorNoCia(getCompania().getNoCia());

	}

	public void grabar() {
		try {

			if (cxcInteresMoraDiaNuevo.getId().getDiasInicio() > 0L
					&& cxcInteresMoraDiaNuevo.getId().getDiasFin() > 0L && cxcInteresMoraDiaNuevo.getPorcentaje().intValue() > 0) {

				if (cxcInteresMoraDiaServiceLocal.validarExite(cxcInteresMoraDiaNuevo.getId().getNoCia(),
						cxcInteresMoraDiaNuevo.getId().getDiasInicio(), cxcInteresMoraDiaNuevo.getId().getDiasFin())
						.intValue() == 0) {
					cxcInteresMoraDiaNuevo.setUsuarioControl(getUsuario().getUsuario());
					cxcInteresMoraDiaNuevo.setFechaControl(new Date());
					cxcInteresMoraDiaServiceLocal.insertar(cxcInteresMoraDiaNuevo);
					info("Registro Almacenado con Exito");
					init();

				} else {
					error("Registro Ingresado ya Existe para la Compania");
					init();
				}

			} else {

				error("Favor ingresar los campos con (*), son Obligatorios");
				init();

			}

		} catch (Exception e) {
			LOG.error(e);
			error("Ocurrio un Error al Grabar nuevo Registro");
		}

	}

	public void editar(CxcInteresMoraDia cxcInteresMoraDia) {

		try {

			cxcInteresMoraDia.setUsuarioControl(getUsuario().getUsuario());
			cxcInteresMoraDia.setFechaControl(new Date());
			cxcInteresMoraDiaServiceLocal.actualizar(cxcInteresMoraDia);
			info("Registro Actualizado con Exito");
			init();
		} catch (Exception e) {
			error("Ocurrio un Error al Actualizar Registro");
			LOG.error("Ocurrio un Error al Actualizar Registro", e);
		}

	}

	public void eliminar(CxcInteresMoraDia cxcInteresMoraDia) {

		try {

			cxcInteresMoraDiaServiceLocal.eliminar(cxcInteresMoraDia);
			info("Registro Eliminado con Exito");
			init();
		} catch (Exception e) {
			error("Ocurrio un Error al Eliminar el registro");
			LOG.error("Ocurrio un Error al Eliminar el registro", e);
		}

	}

	public List<CxcInteresMoraDia> getListado() {
		return listado;
	}

	public void setListado(List<CxcInteresMoraDia> listado) {
		this.listado = listado;
	}

	public CxcInteresMoraDia getCxcInteresMoraDiaNuevo() {
		return cxcInteresMoraDiaNuevo;
	}

	public void setCxcInteresMoraDiaNuevo(CxcInteresMoraDia cxcInteresMoraDiaNuevo) {
		this.cxcInteresMoraDiaNuevo = cxcInteresMoraDiaNuevo;
	}

	public CxcInteresMoraDia getCxcInteresMoraDiaSeleted() {
		return cxcInteresMoraDiaSeleted;
	}

	public void setCxcInteresMoraDiaSeleted(CxcInteresMoraDia cxcInteresMoraDiaSeleted) {
		this.cxcInteresMoraDiaSeleted = cxcInteresMoraDiaSeleted;
	}

}
