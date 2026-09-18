package com.casabaca.prime.cxc.common.lazy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.service.ArcjcaServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.modelo.CxcComprobanteExterno;
import com.casabaca.cxc.ejb.servicio.CxcComprobanteExternoServiceLocal;
import com.casabaca.exception.ServiceLocatorException;

public class LazyDataModelCxcComprobanteExterno extends LazyDataModel<CxcComprobanteExterno>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String noCia;
	
	private CxcComprobanteExternoServiceLocal cxcComprobanteExternoServiceLocal;
	
	private List<CxcComprobanteExterno> lcomprobantesExternosList;
	
	private CxcComprobanteExterno comprobanteExterno;
	
	private void initService() throws ServiceLocatorException {
		
		this.cxcComprobanteExternoServiceLocal = (CxcComprobanteExternoServiceLocal) ServiceLocator.getService(NombreJNDI.CXC_COMPROBANTE_EXTERNO_SERVICE_BEAN);
	}
	
	public LazyDataModelCxcComprobanteExterno() {
		
		try {
			initService();
		} catch (ServiceLocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public LazyDataModelCxcComprobanteExterno(int first, int pageSize, String noCia, CxcComprobanteExterno comprobanteExterno) {
		
		try {
			initService();
		
			this.noCia = noCia;
			this.comprobanteExterno = comprobanteExterno;
			HashMap<String, Object> filters = new HashMap<>();
			setLcomprobantesExternosList(cargarComprobantesExternos(first, pageSize, filters));
			this.setRowCount(cxcComprobanteExternoServiceLocal.contarCxcComprobanteExternoxNocia(this.noCia, filters).intValue());
			
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public List<CxcComprobanteExterno> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {	
			Map<String, Object> filtrosCustomize = ObjectUtils.clone(filters);
			
			if(this.comprobanteExterno != null) {

				if(validarnulosString(this.comprobanteExterno.getComprobante())) {
					filtrosCustomize.put("comprobante", this.comprobanteExterno.getComprobante());
				}
				
				if(validarnulosString(this.comprobanteExterno.getSerieComprobante())) {
					filtrosCustomize.put("serieComprobante", this.comprobanteExterno.getSerieComprobante());
				}
				
				if(validarnulosString(this.comprobanteExterno.getRucEmisor())) {
					filtrosCustomize.put("rucEmisor", this.comprobanteExterno.getRucEmisor());
				}
				
				if(this.comprobanteExterno.getFechaDesde()!=null) {
					filtrosCustomize.put("customFromDate", this.comprobanteExterno.getFechaDesde());
				}
				
				if(this.comprobanteExterno.getFechaHasta()!=null) {
					filtrosCustomize.put("customFromTo", this.comprobanteExterno.getFechaHasta());
				}
				
				if(this.comprobanteExterno.getFechaCaja()!=null) {
					filtrosCustomize.put("customFechaCaja", this.comprobanteExterno.getFechaCaja());
				}
				
				if(this.comprobanteExterno.getFechaRevision()!=null) {
					//Me liste los que tengan fecha de revision
					filtrosCustomize.put("customFechaRevision", Boolean.TRUE);
				}
				
				
			}
			
			setLcomprobantesExternosList(cargarComprobantesExternos(first, pageSize, filtrosCustomize));		
			this.setRowCount(cxcComprobanteExternoServiceLocal.contarCxcComprobanteExternoxNocia(this.noCia, filtrosCustomize).intValue());
		return this.lcomprobantesExternosList;
	}
	
	
	private boolean validarnulosString(String dato) {
		return dato != null ? (dato.trim().equals("")? false : true ) : false;
	}
	
	@Override
    public CxcComprobanteExterno getRowData(String rowKey) {
        for(CxcComprobanteExterno comprobante :this.lcomprobantesExternosList) {
            if(comprobante.getCode().toString().equals(rowKey)) {
                return comprobante;
            }
        }
 
        return null;
    }
	
	private List<CxcComprobanteExterno> cargarComprobantesExternos(int first, int size, Map<String, Object> filters){
		
		List<CxcComprobanteExterno> lcomprobantes = cxcComprobanteExternoServiceLocal.getCxcComprobanteExternoxNocia(this.noCia, first, size, filters);

		return lcomprobantes;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<CxcComprobanteExterno> getLcomprobantesExternosList() {
		return lcomprobantesExternosList;
	}

	public void setLcomprobantesExternosList(List<CxcComprobanteExterno> lcomprobantesExternosList) {
		this.lcomprobantesExternosList = lcomprobantesExternosList;
	}

	public CxcComprobanteExterno getComprobanteExterno() {
		return comprobanteExterno;
	}

	public void setComprobanteExterno(CxcComprobanteExterno comprobanteExterno) {
		this.comprobanteExterno = comprobanteExterno;
	}

}
