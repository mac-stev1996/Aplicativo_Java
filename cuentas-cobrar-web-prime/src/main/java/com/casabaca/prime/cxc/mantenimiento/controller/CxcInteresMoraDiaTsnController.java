package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcInteresMoraDiaTsn;
import com.casabaca.cxc.ejb.modelo.CxcInteresMoraDiaTsnPK;
import com.casabaca.cxc.ejb.servicio.CxcInteresMoraDiaTsnServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.vehiculos.ejb.modelo.TipoFinanciamiento;
import com.casabaca.vehiculos.ejb.servicio.TipoFinanciamientoServicioLocal;

/**
 * Controlador CXC_INTERES_MORA_DIAS_TSN
 * 
 * @author we_romero
 *
 */
@ManagedBean
@ViewScoped
public class CxcInteresMoraDiaTsnController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6821702734881082442L;
	static final Logger LOG = Logger.getLogger(CxcInteresMoraDiaTsnController.class);

	@EJB(lookup = NombreJNDI.CXC_INTERES_MORA_DIA_TSN_SERVICE)
	private CxcInteresMoraDiaTsnServiceLocal cxcInteresMoraDiaTsnServiceLocal;

	@EJB(lookup = NombreJNDI.TIPO_FINANCIAMIENTO_SERVICIO_BEAN)
	private TipoFinanciamientoServicioLocal tipoFinanciamientoService;
	
	private List<CxcInteresMoraDiaTsn> listadoTsn;
	private CxcInteresMoraDiaTsn cxcInteresMoraDiaTsnNuevo;
	private CxcInteresMoraDiaTsn cxcInteresMoraDiaTsnSeleted;
	
	private List<TipoFinanciamiento> tipoFinanciamientoList;
	private String tipoFinanciamiento;
	
	@PostConstruct
	public void init() {
		tipoFinanciamientoList = new ArrayList<TipoFinanciamiento>();
		listadoTsn = new ArrayList<CxcInteresMoraDiaTsn>();
		cxcInteresMoraDiaTsnNuevo = new CxcInteresMoraDiaTsn(new CxcInteresMoraDiaTsnPK(getCompania().getNoCia(), this.tipoFinanciamiento));
		cxcInteresMoraDiaTsnSeleted = new CxcInteresMoraDiaTsn();
		tipoFinanciamientoList = tipoFinanciamientoService.buscarTiposFinanciamientoPorParams(getCompania().getNoCia(), "3");
	}


	public void grabar() {
		try {
			if (cxcInteresMoraDiaTsnNuevo.getPk().getTipo() != null
					&& cxcInteresMoraDiaTsnNuevo.getPk().getDiasInicio() > 0L
					&& cxcInteresMoraDiaTsnNuevo.getPk().getDiasFin() > 0L
					&& cxcInteresMoraDiaTsnNuevo.getPorcentaje().intValue() > 0) {
				if (cxcInteresMoraDiaTsnServiceLocal.validarExite(cxcInteresMoraDiaTsnNuevo.getPk().getNoCia(),
						cxcInteresMoraDiaTsnNuevo.getPk().getTipo(), cxcInteresMoraDiaTsnNuevo.getPk().getDiasInicio(),
						cxcInteresMoraDiaTsnNuevo.getPk().getDiasFin()).intValue() == 0) {

					cxcInteresMoraDiaTsnNuevo.setUsuarioControl(getUsuario().getUsuario());
					cxcInteresMoraDiaTsnNuevo.setFechaControl(new Date());
					cxcInteresMoraDiaTsnServiceLocal.insertar(cxcInteresMoraDiaTsnNuevo);
					info("Registro Almacenado con Exito");
				} else {
					error("Registro Ingresado ya existe en la Compania para el tipo");
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
		this.tipoFinanciamiento = cxcInteresMoraDiaTsnNuevo.getPk().getTipo();
		buscarxNoCiaTipo(cxcInteresMoraDiaTsnNuevo.getPk().getNoCia(), cxcInteresMoraDiaTsnNuevo.getPk().getTipo());
	}

	public void editar(CxcInteresMoraDiaTsn cxcInteresMoraDiaTsn) {
		try {
			cxcInteresMoraDiaTsn.setUsuarioControl(getUsuario().getUsuario());
			cxcInteresMoraDiaTsn.setFechaControl(new Date());
			cxcInteresMoraDiaTsnServiceLocal.actualizar(cxcInteresMoraDiaTsn);
			info("Registro Actualizado con Exito");
		} catch (Exception e) {
			error("Ocurrio un Error al Actualizar Registro");
		}
		this.tipoFinanciamiento = cxcInteresMoraDiaTsn.getPk().getNoCia();
		buscarxNoCiaTipo(cxcInteresMoraDiaTsn.getPk().getNoCia(), cxcInteresMoraDiaTsn.getPk().getTipo());
	}

	public void eliminar(CxcInteresMoraDiaTsn cxcInteresMoraDiaTsn) {
		try {
			cxcInteresMoraDiaTsnServiceLocal.eliminar(cxcInteresMoraDiaTsn);
			info("Registro Eliminado con Exito");
			init();
		} catch (Exception e) {
			error("Ocurrio un Error al Eliminar el registro");
		}
		this.tipoFinanciamiento = cxcInteresMoraDiaTsn.getPk().getNoCia();
		buscarxNoCiaTipo(cxcInteresMoraDiaTsn.getPk().getNoCia(), cxcInteresMoraDiaTsn.getPk().getTipo());

	}
	

	public void buscar() {
		listadoTsn = new ArrayList<CxcInteresMoraDiaTsn>();
		if (tipoFinanciamiento != null) {
			listadoTsn = cxcInteresMoraDiaTsnServiceLocal.buscarPorNoCiaTipo(getCompania().getNoCia(),tipoFinanciamiento);
		}else
			warn("Por favor ingrese el tipo de Financiamiento.");

	}
	
	public void buscarxNoCiaTipo(String noCia, String tipo) {
		listadoTsn = new ArrayList<CxcInteresMoraDiaTsn>();
		if (tipoFinanciamiento != null) {
			listadoTsn = cxcInteresMoraDiaTsnServiceLocal.buscarPorNoCiaTipo(noCia,tipo);
		}
	}
	
	public void validarFechas() {
		
		if (cxcInteresMoraDiaTsnServiceLocal.validarExite(getCompania().getNoCia(),
				cxcInteresMoraDiaTsnNuevo.getPk().getTipo(), cxcInteresMoraDiaTsnNuevo.getPk().getDiasInicio(),
				cxcInteresMoraDiaTsnNuevo.getPk().getDiasFin()).intValue() == 0) {
			cxcInteresMoraDiaTsnNuevo.getPk().setNoCia(getCompania().getNoCia());
			cxcInteresMoraDiaTsnNuevo.getPk().setTipo(cxcInteresMoraDiaTsnNuevo.getPk().getTipo());
			cxcInteresMoraDiaTsnNuevo.getPk().setDiasInicio(cxcInteresMoraDiaTsnNuevo.getPk().getDiasInicio());
			cxcInteresMoraDiaTsnNuevo.getPk().setDiasFin(cxcInteresMoraDiaTsnNuevo.getPk().getDiasFin());
		} else {
			error("Ya existen datos para la linea: " + cxcInteresMoraDiaTsnNuevo.getPk().getTipo());
		}

	}
	
	public List<CxcInteresMoraDiaTsn> getListadoTsn() {
		return listadoTsn;
	}

	public void setListadoTsn(List<CxcInteresMoraDiaTsn> listadoTsn) {
		this.listadoTsn = listadoTsn;
	}

	public CxcInteresMoraDiaTsn getCxcInteresMoraDiaTsnNuevo() {
		return cxcInteresMoraDiaTsnNuevo;
	}

	public void setCxcInteresMoraDiaTsnNuevo(CxcInteresMoraDiaTsn cxcInteresMoraDiaTsnNuevo) {
		this.cxcInteresMoraDiaTsnNuevo = cxcInteresMoraDiaTsnNuevo;
	}

	public CxcInteresMoraDiaTsn getCxcInteresMoraDiaTsnSeleted() {
		return cxcInteresMoraDiaTsnSeleted;
	}

	public void setCxcInteresMoraDiaTsnSeleted(CxcInteresMoraDiaTsn cxcInteresMoraDiaTsnSeleted) {
		this.cxcInteresMoraDiaTsnSeleted = cxcInteresMoraDiaTsnSeleted;
	}


	public String getTipoFinanciamiento() {
		return tipoFinanciamiento;
	}

	public void setTipoFinanciamiento(String tipoFinanciamiento) {
		this.tipoFinanciamiento = tipoFinanciamiento;
	}


	public List<TipoFinanciamiento> getTipoFinanciamientoList() {
		return tipoFinanciamientoList;
	}


	public void setTipoFinanciamientoList(List<TipoFinanciamiento> tipoFinanciamientoList) {
		this.tipoFinanciamientoList = tipoFinanciamientoList;
	}
	
}
