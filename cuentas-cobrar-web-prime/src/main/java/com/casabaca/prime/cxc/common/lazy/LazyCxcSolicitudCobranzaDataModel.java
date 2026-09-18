package com.casabaca.prime.cxc.common.lazy;

import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCobranza;
import com.casabaca.cxc.ejb.servicio.CxcSolicitudCobranzaServiceLocal;
import com.casabaca.exception.ServiceLocatorException;

public class LazyCxcSolicitudCobranzaDataModel extends LazyDataModel<CxcSolicitudCobranza> {

	private static final long serialVersionUID = 1L;
	
	private List<CxcSolicitudCobranza> lsolicitudesCobranzas;
	private String noCia;
	
	private CxcSolicitudCobranzaServiceLocal solicitudCobranzaServiceLocal;
	
	private Map<String, Object> filtros;

	public LazyCxcSolicitudCobranzaDataModel(String noCia, Map<String, Object> filtros) throws ServiceLocatorException {
		this.noCia = noCia;
		this.filtros = filtros;
		this.solicitudCobranzaServiceLocal = ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CxcSolicitudCobranzaServiceBean");
	}
	
//	public LazyCxcSolicitudCobranzaDataModel(String noCia) throws ServiceLocatorException {
//		this.noCia = noCia;
//		this.solicitudCobranzaServiceLocal = ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CxcSolicitudCobranzaServiceBean");
//	}

	@Override
	public CxcSolicitudCobranza getRowData(String rowkey) {
		
		for (CxcSolicitudCobranza sc : this.lsolicitudesCobranzas) {
			if(sc.getSolicitudCobranzaPK().toString().equals(rowkey)) {
				return sc;
			}
		}
		return null;
		
	}
	
	@Override
	public Object getRowKey(CxcSolicitudCobranza object) {
		return object.getSolicitudCobranzaPK();
	}
		
	@Override
	public List<CxcSolicitudCobranza> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		if (this.filtros != null) {
			filters.putAll(this.filtros); // Inyectar los filtros personalizados
		}
		
		this.lsolicitudesCobranzas =this.solicitudCobranzaServiceLocal.getSolicitudes(this.noCia, filters, first, pageSize); 
		this.setRowCount(this.lsolicitudesCobranzas!=null?this.lsolicitudesCobranzas.size():0);
		return this.lsolicitudesCobranzas;
	}
	
	@Override
	public List<CxcSolicitudCobranza> load(int first, int pageSize, List<SortMeta> multiSortMeta,
			Map<String, Object> filters) {
		// TODO Auto-generated method stub
		return super.load(first, pageSize, multiSortMeta, filters);
	}

	public List<CxcSolicitudCobranza> getLsolicitudesCobranzas() {
		return lsolicitudesCobranzas;
	}

	public void setLsolicitudesCobranzas(List<CxcSolicitudCobranza> lsolicitudesCobranzas) {
		this.lsolicitudesCobranzas = lsolicitudesCobranzas;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

}