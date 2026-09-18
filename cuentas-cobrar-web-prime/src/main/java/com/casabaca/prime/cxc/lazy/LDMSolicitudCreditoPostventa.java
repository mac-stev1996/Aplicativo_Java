package com.casabaca.prime.cxc.lazy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.modelo.CxcCreditoPostventa;
import com.casabaca.cxc.ejb.servicio.CxcCreditoPostventaServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceLocatorException;

/**
 * @author la_soria
 */
public class LDMSolicitudCreditoPostventa extends LazyDataModel<CxcCreditoPostventa> {

	private static final long serialVersionUID = 1L;

	private CxcCreditoPostventaServiceLocal service;

	private List<CxcCreditoPostventa> lista;
	private Map<String, Object> filters;
	private int first;
	private String noCia;
	private String centro;
	private List <String> estado;
	private Date fechaDesde;
	private Date fechaHasta;
	private String cedula;
	private boolean refresh;

	public LDMSolicitudCreditoPostventa(int first, int pageSize, String noCia, String centro, List <String> estado,
			Date fechaDesde, Date fechaHasta, String cedula) {
		this.lista = new ArrayList<CxcCreditoPostventa>();
		this.noCia = noCia;
		this.centro = centro;
		this.estado = estado;
		this.fechaDesde = fechaDesde;
		this.fechaHasta = fechaHasta;
		this.cedula = cedula;

		try {
			service = (CxcCreditoPostventaServiceLocal) ServiceLocator
					.getService(NombreJNDI.CXC_CREDITO_POSTVENTA_SERVICE);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}

	}

	@Override
	public List<CxcCreditoPostventa> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			if (!this.refresh) {
				this.setRowCount(service.countSolicitudesCredito(this.noCia, this.centro, this.estado, this.fechaDesde,
						this.fechaHasta, this.cedula, filters).intValue());
				this.setLista(loadData(this.noCia, this.centro, filters, first, pageSize));
			}
			this.refresh = false;
		} catch (FindException e) {
			e.printStackTrace();
		}
		return this.lista;
	}

	@Override
	public CxcCreditoPostventa getRowData(String rowKey) {
		for (CxcCreditoPostventa objeto : this.lista) {
			if (objeto.getNoSolicitud().toString().equals(rowKey)) {
				return objeto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(CxcCreditoPostventa objeto) {
		return objeto.getNoSolicitud();
	}

	private List<CxcCreditoPostventa> loadData(String noCia, String centro, Map<String, Object> filters, int first,
			int maxSize) throws FindException {
		this.first = first;
		List<CxcCreditoPostventa> lista = service.getSolicitudesCredito(noCia, centro, estado, fechaDesde, fechaHasta,
				cedula, first, maxSize, filters);
		return lista;
	}

	/********
	 * GETERS AND SETERS
	 ********/

	public List<CxcCreditoPostventa> getLista() {
		return lista;
	}

	public void setLista(List<CxcCreditoPostventa> lista) {
		this.lista = lista;
	}

	public Map<String, Object> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}

	public int getFirst() {
		return first;
	}

	public void setFirst(int first) {
		this.first = first;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public List<String> getEstado() {
		return estado;
	}

	public void setEstado(List<String> estado) {
		this.estado = estado;
	}

	public Date getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

}
