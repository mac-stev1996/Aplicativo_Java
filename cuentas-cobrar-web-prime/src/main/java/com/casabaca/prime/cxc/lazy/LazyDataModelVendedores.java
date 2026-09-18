/**
 * 
 */
package com.casabaca.prime.cxc.lazy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.model.Vendedor;
import com.casabaca.common.ejb.service.VendedorServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceLocatorException;

/**
 * @author is_delacruz
 *
 */
public class LazyDataModelVendedores extends LazyDataModel<Vendedor> {

	private static final long serialVersionUID = 1L;

	private List<Vendedor> vendedorList;

	private VendedorServicioLocal vendedorService;

	private String noCia;
	private String agencia;
	private Map<String, Object> filtrosLazy = new HashMap<String, Object>();
	private boolean ventaEmpleados;

	public LazyDataModelVendedores(String noCia, String agencia, boolean ventaEmpleados) {
		try {
			this.noCia = noCia;
			vendedorService = (VendedorServicioLocal) ServiceLocator.getService(NombreJNDI.VENDEDOR_SERVICIO_BEAN);
			this.filtrosLazy.put("svagcodi", agencia);
			this.filtrosLazy.put("svvestat", "A");
			this.ventaEmpleados = ventaEmpleados;
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Vendedor> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		filters.putAll(this.filtrosLazy);
		setVendedorList(cargarVendedores(first, pageSize, filters, null));
		if (getVendedorList().size() > 0) {
			this.setRowCount(contarVendedores(filters));
		} else {
			this.setRowCount(0);
		}
		return this.vendedorList;
	}

	@Override
	public Vendedor getRowData(String rowKey) {
		for (Vendedor vendedor : getVendedorList()) {
			if (vendedor.getSvfvendPK().toString().equals(rowKey)) {
				return vendedor;
			}
		}

		return null;
	}

	@Override
	public Object getRowKey(Vendedor vendedor) {
		return vendedor.getSvfvendPK();
	}

	private List<Vendedor> cargarVendedores(int first, int max, Map<String, Object> filters, String orden) {
		try {
			return vendedorService.consultarVendedoresLazy(this.noCia, first, max, filters, orden, this.ventaEmpleados);
		} catch (FindException e) {
			e.printStackTrace();
			return null;
		}
	}

	private int contarVendedores(Map<String, Object> filters) {
		return vendedorService.contarVendedoresLazy(this.noCia, filters, this.ventaEmpleados);
	}

	public List<Vendedor> getVendedorList() {
		return vendedorList;
	}

	public void setVendedorList(List<Vendedor> vendedorList) {
		this.vendedorList = vendedorList;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public boolean isVentaEmpleados() {
		return ventaEmpleados;
	}

	public void setVentaEmpleados(boolean ventaEmpleados) {
		this.ventaEmpleados = ventaEmpleados;
	}

	public Map<String, Object> getFiltrosLazy() {
		return filtrosLazy;
	}

	public void setFiltrosLazy(Map<String, Object> filtrosLazy) {
		this.filtrosLazy = filtrosLazy;
	}

}
