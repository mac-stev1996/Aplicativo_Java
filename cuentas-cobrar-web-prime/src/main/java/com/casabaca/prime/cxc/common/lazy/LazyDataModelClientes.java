package com.casabaca.prime.cxc.common.lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;

/**
 * @author cf_yaselga
 *
 */
public class LazyDataModelClientes extends LazyDataModel<Cliente> {

	private static final long serialVersionUID = 1L;
	private String noCia;
	private ClienteServiceLocal clienteService;
	private List<Cliente> clientes;

	public LazyDataModelClientes(int first, int pageSize, String noCia) {
		try {
			clienteService = (ClienteServiceLocal) ServiceLocator.getService(NombreJNDI.CLIENTE_SERVICE);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		this.noCia = noCia;

	}

	@Override
	public List<Cliente> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		setClientes(cargarClientes(first, pageSize, filters));
		this.setRowCount((clienteService.contarClientesParametros(noCia, filters)).intValue());

		return clientes;
	}

	@Override
	public Cliente getRowData(String rowKey) {
		for (Cliente cliente : clientes) {
			if (cliente.getClientePK().toString().equals(rowKey)) {
				return cliente;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(Cliente cliente) {
		return cliente.getClientePK();
	}

	private List<Cliente> cargarClientes(int first, int size, Map<String, Object> filters) {
		List<Cliente> result = new ArrayList<Cliente>();
		result = clienteService.getClientesParametros(noCia, first, size, filters);

		return result;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}

}
