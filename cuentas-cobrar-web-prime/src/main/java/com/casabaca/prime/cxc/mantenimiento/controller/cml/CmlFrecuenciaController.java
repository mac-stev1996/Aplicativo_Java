package com.casabaca.prime.cxc.mantenimiento.controller.cml;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.cml.dao.CmlFrecuenciaVentaDaoLocal;
import com.casabaca.cxc.cml.model.CmlFrecuenciaVenta;
import com.casabaca.cxc.cml.model.CmlFrecuenciaVentaPK;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;

import org.apache.log4j.Logger;

/**
 * @author rr_reyes
 */
@ManagedBean(name = "cmlFrecuenciaController")
@ViewScoped
public class CmlFrecuenciaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(CmlFrecuenciaController.class);

	@EJB(lookup = NombreJNDI.CXC_FRECUENCIA_VENTAS_SERVICE)
	private CmlFrecuenciaVentaDaoLocal cmlFrecuenciaVentaDaoLocal;

	private List<CmlFrecuenciaVenta> tipoFrecuenciaLst;
	private CmlFrecuenciaVenta tipoFrecuenciaSel;
	private Boolean estadoCampoCodigo;

	@PostConstruct
	public void init() {
		try {
			tipoFrecuenciaLst = cmlFrecuenciaVentaDaoLocal.listar(getCompania().getNoCia());
		} catch (Exception e) {
			LOG.error("Error al cargar datos iniciales", e);
		}
	}

	public void inactivarFrecuenciaDeVenta(CmlFrecuenciaVenta tipoFrecuencia) {
		try {
			tipoFrecuencia.setCampoEstado(CommonConstants.INACTIVO);
			cmlFrecuenciaVentaDaoLocal.edit(tipoFrecuencia);
			init();
			info("Se ha inactivado la frecuencia de venta, correctamente");
		} catch (UpdateException d) {
			LOG.error("Error al inactivar la tipo factor riesgo", d);
			error("Al inactivar la tipoFrecuenciaLst");
		}
	}

	public void editarFrecuenciaDeVenta(CmlFrecuenciaVenta tipoFrecuencia) {
		tipoFrecuenciaSel = tipoFrecuencia;
		accionesDialog("dlgNewTipoFactorRiesgo", CommonConstants.TRUE_VALUE);
		setEstadoCampoCodigo(CommonConstants.TRUE_VALUE);
	}

	public void guardar() {
		try {		
			if(validaCampos()){
				if(estadoCampoCodigo) {
					cmlFrecuenciaVentaDaoLocal.edit(tipoFrecuenciaSel);
					info("Se actualizo el registro correctamente");
					accionesDialog("dlgNewTipoFactorRiesgo", Boolean.FALSE);
				}
				else {
					if(cmlFrecuenciaVentaDaoLocal.validaCodigo(getCompania().getNoCia(), tipoFrecuenciaSel.getId().getCodigo())) {
						info("El codigo de frecuencia: ".concat(tipoFrecuenciaSel.getId().getCodigo()).concat(", ya existe en la base."));
					}
					else {
						cmlFrecuenciaVentaDaoLocal.create(tipoFrecuenciaSel);
						info("Se ha guardado el registro correctamente");
						accionesDialog("dlgNewTipoFactorRiesgo", Boolean.FALSE);
					}
				}
				init();
			}
		} catch (InsertException | UpdateException e) {
			LOG.error(obtainException(e));
			error("Al guardar el registro");
		}
	}

	public void nuevaFrecuenciaDeVenta() {
		tipoFrecuenciaSel = new CmlFrecuenciaVenta(new CmlFrecuenciaVentaPK(getCompania().getNoCia(), null));
		tipoFrecuenciaSel.setCampoEstado(CommonConstants.ACTIVO);
		setEstadoCampoCodigo(CommonConstants.FALSE_VALUE);
	}
	
	public Boolean validaCampos() {
		if("".equals(tipoFrecuenciaSel.getId().getCodigo())){
			info("Codigo: Error de validación: se necesita un valor.");
			return false;
		}
		if(tipoFrecuenciaSel.getCampoInicio() == null){
			info("Inicio: Error de validación: se necesita un valor.");
			return false;
		}
		if(tipoFrecuenciaSel.getCampoFin() == null){
			info("Fin: Error de validación: se necesita un valor.");
			return false;
		}
		if(tipoFrecuenciaSel.getCampoValor() == null){
			info("Valor: Error de validación: se necesita un valor.");
			return false;
		}
		if(tipoFrecuenciaSel.getCampoInicio().signum() <= 0 || tipoFrecuenciaSel.getCampoFin().signum() <= 0 || tipoFrecuenciaSel.getCampoValor().signum() <= 0 ) {
			info("El valor del campo Inicio, Fin, Valor debe ser mayor a 0");
			return false;
		}
		if(tipoFrecuenciaSel.getCampoInicio().compareTo(tipoFrecuenciaSel.getCampoFin()) == 1) {
			info("El valor del campo Fin debe ser mayor o igual al valor del campo Inicio.");
			return false;
		}
		
		return true;
	}
	
	public List<CmlFrecuenciaVenta> getTipoFrecuenciaLst() {
		return tipoFrecuenciaLst;
	}

	public void setTipoFrecuenciaLst(List<CmlFrecuenciaVenta> tipoFrecuenciaLst) {
		this.tipoFrecuenciaLst = tipoFrecuenciaLst;
	}

	public CmlFrecuenciaVenta getTipoFrecuenciaSel() {
		return tipoFrecuenciaSel;
	}

	public void setTipoFrecuenciaSel(CmlFrecuenciaVenta tipoFrecuenciaSel) {
		this.tipoFrecuenciaSel = tipoFrecuenciaSel;
	}

	public Boolean getEstadoCampoCodigo() {
		return estadoCampoCodigo;
	}

	public void setEstadoCampoCodigo(Boolean estadoCampoCodigo) {
		this.estadoCampoCodigo = estadoCampoCodigo;
	}
	
}
