package com.casabaca.prime.cxc.lazy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.caja.ejb.modelo.Arcctd;
import com.casabaca.caja.ejb.modelo.ArcctdPK;
import com.casabaca.caja.ejb.service.ArcctdServiceLocal;
import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.servicio.CxcEntradaMovimientosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceLocatorException;

/**
 * @author hj_garcia
 * @Comments Lazy Data Model by Arccmd Items for Paginator in Data Table
 */

public class LDMEntradasMovimientos extends LazyDataModel<Arccmd> {

	private static final long serialVersionUID = 1L;

	private CxcEntradaMovimientosServiceLocal service;
	private ArcctdServiceLocal arcctdService;

	private List<Arccmd> lista;
	private Map<String, Object> filters;
	private int first;
	private Arccmd filtro;

	public LDMEntradasMovimientos(int first, int pageSize, Arccmd arccmd) {
		this.lista = new ArrayList<>();
		this.filtro = arccmd;
		try {
			service = (CxcEntradaMovimientosServiceLocal) ServiceLocator
					.getService(NombreJNDI.CXC_ENTRADA_MOVIMIENTOS_SERVICE);
			arcctdService = (ArcctdServiceLocal) ServiceLocator.getService(NombreJNDI.ARCCTD_SERVICE);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Arccmd> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			this.filters = filters;
			this.setRowCount(service.countEntradas(filtro).intValue());
			this.setLista(loadData(filtro, first, pageSize));
		} catch (FindException e) {
			e.printStackTrace();
		}
		return this.lista;
	}

	@Override
	public Arccmd getRowData(String rowKey) {
		for (Arccmd objeto : lista) {
			if (objeto.getId().toString().equals(rowKey)) {
				return objeto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(Arccmd objeto) {
		return objeto.getId();
	}

	private List<Arccmd> loadData(Arccmd filtro, Integer first, Integer maxSize) throws FindException {
		this.first = first;
		List<Arccmd> lista = service.getEntradas(filtro, first, maxSize);
		lista.forEach(l -> {
			if (l.getTipoDoc() != null) {
				try {
					Arcctd tipoDoc = arcctdService.findByPK(new ArcctdPK(l.getId().getNoCia(), l.getTipoDoc()));
					l.setDescTipoDoc(tipoDoc.getDescripcion());
				} catch (FindException e) {
				}
			}
		});
		return lista;
	}

	public List<Arccmd> getLista() {
		return lista;
	}

	public void setLista(List<Arccmd> lista) {
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

	public Arccmd getFiltro() {
		return filtro;
	}

	public void setFiltro(Arccmd filtro) {
		this.filtro = filtro;
	}

}