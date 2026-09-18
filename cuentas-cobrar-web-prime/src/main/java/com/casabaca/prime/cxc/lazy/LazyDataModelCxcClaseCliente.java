package com.casabaca.prime.cxc.lazy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.model.CxcClaseCliente;
import com.casabaca.common.ejb.service.CxcClaseClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;

public class LazyDataModelCxcClaseCliente extends LazyDataModel<CxcClaseCliente> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3669312074587731630L;
	
	private static Logger logger = Logger.getLogger(LazyDataModelCxcClaseCliente.class);

	private CxcClaseClienteServiceLocal repTipoVisitaServiceLocal;

	private List<CxcClaseCliente> listaCxcClaseCliente;
	private Map<String, Object> filters;
	private int first;
	private String noCia;

	public LazyDataModelCxcClaseCliente(int first, int pageSize, String noCia) {
		HashMap<String, Object> filtros = new HashMap<>();
		this.noCia = noCia;
		try {
			repTipoVisitaServiceLocal = (CxcClaseClienteServiceLocal) ServiceLocator
					.getService(NombreJNDI.CXC_CLASE_CLIENTE_SERVICE);
		} catch (ServiceLocatorException e) {
			logger.error(e);
		}
		setRowCount(repTipoVisitaServiceLocal.countByNoCia(noCia, filtros));
		this.listaCxcClaseCliente = loadCxcClaseCliente(noCia, first, pageSize, filtros);
	}

	@Override
	public List<CxcClaseCliente> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		this.setRowCount(repTipoVisitaServiceLocal.countByNoCia(noCia, filters));
		setListaCxcClaseCliente(loadCxcClaseCliente(this.noCia, first, pageSize, filters));
		return this.listaCxcClaseCliente;
	}

	@Override
	public CxcClaseCliente getRowData(String rowKey) {
		for (CxcClaseCliente repTipoVisita : this.listaCxcClaseCliente) {
			if (repTipoVisita.getCxcClaseClientePK().toString().equals(rowKey)) {
				return repTipoVisita;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(CxcClaseCliente repTipoVisita) {
		return repTipoVisita.getCxcClaseClientePK();
	}

	private List<CxcClaseCliente> loadCxcClaseCliente(String noCia, int first, int maxSize,
			Map<String, Object> filters) {
		this.first = first;
		this.filters = filters;
		return repTipoVisitaServiceLocal.findByNoCia(noCia, first, maxSize, filters);
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public Map<String, Object> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}

	public List<CxcClaseCliente> getListaCxcClaseCliente() {
		return listaCxcClaseCliente;
	}

	public void setListaCxcClaseCliente(List<CxcClaseCliente> listaCxcClaseCliente) {
		this.listaCxcClaseCliente = listaCxcClaseCliente;
	}

}
