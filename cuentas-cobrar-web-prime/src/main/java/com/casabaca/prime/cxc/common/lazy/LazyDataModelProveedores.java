/**
 * 
 */
package com.casabaca.prime.cxc.common.lazy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.model.Proveedor;
import com.casabaca.common.ejb.service.ProveedorServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;

/**
 * @author is_delacruz
 *
 */
public class LazyDataModelProveedores extends LazyDataModel<Proveedor> {
	
	private String noCia;
	
	private ProveedorServiceLocal proveedorService;
	
	private List<Proveedor> proveedorList;
	
	public LazyDataModelProveedores(int first, int pageSize, String noCia) {
		try {
			this.noCia = noCia;
			HashMap<String, Object> filters = new HashMap<>();
			proveedorService = (ProveedorServiceLocal) ServiceLocator.getService(NombreJNDI.PROVEEDOR_SERVICE);
			setProveedorList(cargarProveedores(first, pageSize, filters));
			this.setRowCount(proveedorService.contarProveedoresxNocia(this.noCia, first, pageSize, filters).intValue());
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<Proveedor> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {	
		setProveedorList(cargarProveedores(first, pageSize, filters));		
		this.setRowCount(proveedorService.contarProveedoresxNocia(this.noCia, first, pageSize, filters).intValue());						
		return this.proveedorList;
	}
	
	@Override
    public Proveedor getRowData(String rowKey) {
        for(Proveedor proveedor :this.proveedorList) {
            if(proveedor.getPk().toString().equals(rowKey)) {
                return proveedor;
            }
        }
 
        return null;
    }
	
	private List<Proveedor> cargarProveedores(int first, int size, Map<String, Object> filters){
		return proveedorService.getProveedoresxNocia(this.noCia, first, size, filters);
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<Proveedor> getProveedorList() {
		return proveedorList;
	}

	public void setProveedorList(List<Proveedor> proveedorList) {
		this.proveedorList = proveedorList;
	}

}
