package com.casabaca.prime.cxc.common.lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.model.Arccck;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.servicio.CxcEntradaChequesDevueltosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceLocatorException;

public class LDMEntradaChequesDevueltos extends LazyDataModel<Arccck> {

	private static final long serialVersionUID = 1L;

	private CxcEntradaChequesDevueltosServiceLocal service;

	private List<Arccck> lista;
	private Map<String, Object> filters;
	private int first;
	private Arccck filtro;

	public LDMEntradaChequesDevueltos(int first, int pageSize, Arccck arccck) {
		this.lista = new ArrayList<>();
		this.filtro = arccck;
		try {
			service = (CxcEntradaChequesDevueltosServiceLocal) ServiceLocator
					.getService(NombreJNDI.CXC_ENTRADA_CHEQUES_DEVUELTOS_SERVICE);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Arccck> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			this.filters = filters;
			this.setRowCount(service.countCheques(filtro).intValue());
			this.setLista(loadData(filtro, first, pageSize));
		} catch (FindException e) {
			e.printStackTrace();
		}
		return this.lista;
	}

	@Override
	public Arccck getRowData(String rowKey) {
		for (Arccck objeto : lista) {
			if (objeto.getId().toString().equals(rowKey)) {
				return objeto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(Arccck objeto) {
		return objeto.getId();
	}

	private List<Arccck> loadData(Arccck filtro, Integer first, Integer maxSize) throws FindException {
		this.first = first;
		List<Arccck> lista = service.getEntradas(filtro, first, maxSize);
		lista.forEach(l -> {
		});
		return lista;
	}

	public List<Arccck> getLista() {
		return lista;
	}

	public void setLista(List<Arccck> lista) {
		this.lista = lista;
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

	public Arccck getFiltro() {
		return filtro;
	}

	public void setFiltro(Arccck filtro) {
		this.filtro = filtro;
	}

}
