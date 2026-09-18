package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.Compania;
import com.casabaca.s3s.ejb.service.CompaniaServiceLocal;

/**
 * Mantenimiento de Compania - control de campos:
 * CODIGO_UAF, VALOR_MAX_PAGO_EFECTIVO, CODIGO_EMPRESA, CODIGO_DINARDAP, MONTO_MINIMO_UAF, VALOR_MIN_EFECTIVO, VALOR_MIN_TARJETA
 * @author mm_rivera
 *
 */
@ViewScoped
@ManagedBean
public class ControlOficialCumplimientoController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);

	@EJB(lookup = NombreJNDI.COMPANIA_SERVICE_BEAN)
	private CompaniaServiceLocal companiaServiceLocal;
	
	private List<Compania> companiaList;

	private Compania companiaEditar;
	private boolean editar;
	

	@PostConstruct
	public void init() {
		logger.info("init...");
		companiaList = null;
		companiaEditar = null;
		
		consultarCompanias();
	}
	public void consultarCompanias() {
		logger.info("consultarCompanias...");
		try {
			companiaList = companiaServiceLocal.getAllCias();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	public void editar(Compania item) {
		logger.info("editar...");
		editar = true;
		companiaEditar = item;
		
		accionesDialog("dlgCompania", true);
	}
	
	public void guardar() {
		logger.info("guardar...");
		try {
			companiaServiceLocal.edit(companiaEditar);
			info("Actualización realizada con éxito.");				
			accionesDialog("dlgCompania", false);
		} catch (UpdateException e) {
			error("Error al actualizar la compañia");
		}
	}

	public int getBatchSize() {
		return CommonConstants.BATCH_SIZE;
	}
	public List<Compania> getCompaniaList() {
		return companiaList;
	}
	public void setCompaniaList(List<Compania> companiaList) {
		this.companiaList = companiaList;
	}
	public Compania getCompaniaEditar() {
		return companiaEditar;
	}
	public void setCompaniaEditar(Compania companiaEditar) {
		this.companiaEditar = companiaEditar;
	}
	public boolean isEditar() {
		return editar;
	}
	public void setEditar(boolean editar) {
		this.editar = editar;
	}
		
}