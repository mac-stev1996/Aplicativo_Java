/**
 * 
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.LazyDataModel;

import com.casabaca.common.ejb.dto.RetencionDto;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;
import com.casabaca.prime.cxc.lazy.RetencionDtoLazyDataModel;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class ConsultaRetencionController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3724954260136729934L;

	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	private LazyDataModel<RetencionDto> retencionesLazyDataModel;
	private Long noCliente;
	private String numeroRet;
	private Double monto;
	private String numeroFact;

	public void limpiarDatos() {
		noCliente = null;
		numeroRet = null;
		monto = null;
		numeroFact = null;
		retencionesLazyDataModel = null;
	}

	public void consultar() {
		Map<String, Object> filtros = new HashMap<>();
		if (noCliente != null) {
			filtros.put("NO_CLIENTE", noCliente);
		}
		if (numeroRet != null && !numeroRet.trim().isEmpty()) {
			filtros.put("NUMERO", numeroRet);
		}
		if (monto != null) {
			filtros.put("MONTO", monto);
		}
		if (numeroFact != null && !numeroFact.trim().isEmpty()) {
			filtros.put("NO_REFE", numeroFact);
		}
		if (filtros.size() > 0) {
			retencionesLazyDataModel = new RetencionDtoLazyDataModel(getCompania().getNoCia(), filtros, Boolean.TRUE);
		} else {
			warn("Por favor ingrese al menos un campo de consulta");
		}
	}

	/**
	 * Permite cargar el dialogo de clientes
	 */

	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);
	}

	public LazyDataModel<RetencionDto> getRetencionesLazyDataModel() {
		return retencionesLazyDataModel;
	}

	public void setRetencionesLazyDataModel(LazyDataModel<RetencionDto> retencionesLazyDataModel) {
		this.retencionesLazyDataModel = retencionesLazyDataModel;
	}

	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}

	public void setDialogClientesController(DialogClientesController dialogClientesController) {
		this.dialogClientesController = dialogClientesController;
	}

	public Long getNoCliente() {
		if (Objects.nonNull(dialogClientesController.getCliente())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK().getNoCliente())) {
			noCliente = dialogClientesController.getCliente().getClientePK().getNoCliente();
			dialogClientesController.setCliente(null);
		}
		return noCliente;
	}

	public void setNoCliente(Long noCliente) {
		this.noCliente = noCliente;
	}

	public String getNumeroRet() {
		return numeroRet;
	}

	public void setNumeroRet(String numeroRet) {
		this.numeroRet = numeroRet;
	}

	public Double getMonto() {
		return monto;
	}

	public void setMonto(Double monto) {
		this.monto = monto;
	}

	public String getNumeroFact() {
		return numeroFact;
	}

	public void setNumeroFact(String numeroFact) {
		this.numeroFact = numeroFact;
	}

}
