package com.casabaca.prime.cxc.common.lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.ClienteServiceLocal;

public class LazyClientesDataModel extends LazyDataModel<Cliente>  {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(LazyClientesDataModel.class);

	private List<Cliente> datasource;

	private String noCia;
	private String cedula;
	private String nombres;

	private ClienteServiceLocal clienteServiceLocal;

	public LazyClientesDataModel() {
	}

	public LazyClientesDataModel(ClienteServiceLocal clienteServiceLocal, String noCia, String cedula, String nombres) {
		this.noCia = noCia;
		this.cedula = cedula;
		this.nombres = nombres;
		this.clienteServiceLocal = clienteServiceLocal;

	}

	@Override
	public Cliente getRowData(String rowKey) {
		try {
			return clienteServiceLocal.buscarPorCedulaNoCia(rowKey, noCia);
		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	@Override
	public Object getRowKey(Cliente Cliente) {
		return Cliente.getClientePK().getNoCliente();
	}

	@Override
	public List<Cliente> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		datasource = new ArrayList<Cliente>();

		try {
			datasource = clienteServiceLocal.buscarClientesPorParametros(first, pageSize, noCia, cedula != null
					&& cedula.length() > 0 ? cedula : null, nombres != null && nombres.length() > 0 ? nombres : null);

			Long dataSize = clienteServiceLocal.contarPorParametros(noCia,
					cedula != null && cedula.length() > 0 ? cedula : null,
					nombres != null && nombres.length() > 0 ? nombres : null);

			this.setRowCount(Integer.valueOf(String.valueOf(dataSize)));

		} catch (NumberFormatException e) {
			log.error(e);
		} catch (Exception e) {
			log.error(e);
		}

		return datasource;
	}

	public List<Cliente> getDatasource() {
		return datasource;
	}

	public void setDatasource(List<Cliente> datasource) {
		this.datasource = datasource;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public ClienteServiceLocal getClienteServiceLocal() {
		return clienteServiceLocal;
	}

	public void setClienteServiceLocal(ClienteServiceLocal clienteServiceLocal) {
		this.clienteServiceLocal = clienteServiceLocal;
	}

}
