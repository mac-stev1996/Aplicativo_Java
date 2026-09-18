/**
 * 
 */
package com.casabaca.prime.cxc.common.lazy;

import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.model.ConPlantas;
import com.casabaca.common.ejb.service.ConPlantasServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;

public class LazyDataModelPlanCuentas extends LazyDataModel<ConPlantas> {


	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6432333683256722303L;


	private String noCia;

	private ConPlantasServiceLocal conPlantasServiceLocal;

	private List<ConPlantas> planCuentas;

	public LazyDataModelPlanCuentas(int first, int pageSize, String noCia) {
		try {
			this.noCia = noCia;
			conPlantasServiceLocal = (ConPlantasServiceLocal) ServiceLocator.getService(NombreJNDI.CON_PLANTAS_SERVICE);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<ConPlantas> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		planCuentas = cargarPlanCuentas(first, pageSize, filters);
		this.setRowCount(conPlantasServiceLocal.contarPlanCuentas(this.noCia, filters, Boolean.FALSE).intValue());
		return this.planCuentas;
	}

	@Override
	public ConPlantas getRowData(String rowKey) {
		for (ConPlantas conPlantas : this.planCuentas) {
			if (conPlantas.getConPlatasPk().toString().equals(rowKey)) {
				return conPlantas;
			}
		}
		return null;
	}

	private List<ConPlantas> cargarPlanCuentas(int first, int size, Map<String, Object> filters) {
		return conPlantasServiceLocal.consultarConPlantasNoCiaFiltros(this.noCia, filters, first, size, Boolean.FALSE);
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<ConPlantas> getPlanCuentas() {
		return planCuentas;
	}

	public void setPlanCuentas(List<ConPlantas> planCuentas) {
		this.planCuentas = planCuentas;
	}
}
