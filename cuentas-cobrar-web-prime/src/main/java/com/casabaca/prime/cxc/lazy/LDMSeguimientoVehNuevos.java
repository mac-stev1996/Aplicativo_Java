package com.casabaca.prime.cxc.lazy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.dto.FiltrosConServicioDto;
import com.casabaca.cxc.ejb.dto.SeguimientoVehNuevosDto;
import com.casabaca.cxc.ejb.servicio.CxcContactoServicioServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.prime.cxc.procesos.controller.CxcContactoServicioController;

/**
 * @author la_soria
 */
public class LDMSeguimientoVehNuevos extends LazyDataModel<SeguimientoVehNuevosDto> {

	private static final long serialVersionUID = 1L;
	private static final Logger Log = Logger.getLogger(CxcContactoServicioController.class);
	private CxcContactoServicioServiceLocal service;

	private List<SeguimientoVehNuevosDto> lista;
	private Map<String, Object> filters;
	private int first;
	private String noCia;
	private String centro;
	private List<String> noLinea;
	private String asesor;
	private String cedulaCliente;
	private Date fechaDesde;
	private Date fechaHasta;
	private boolean aplicaQuery;
	private boolean refresh;
	private final int firstGlobal;
	private final int pageSizeGlobal;
	private final FiltrosConServicioDto paramFiltroConServicioGlobal;

	public LDMSeguimientoVehNuevos(int first, int pageSize, String noCia, String centro, List<String> noLinea,
			//String asesor, String cedulaCliente, Date fechaDesde, Date fechaHasta, boolean aplicaQuery,
			FiltrosConServicioDto objFiltroConServicioParam) {
		this.firstGlobal=first;
		this.pageSizeGlobal=pageSize;
		this.noCia = noCia;
		this.centro = centro;
		this.noLinea = noLinea;
		//this.asesor = asesor;
		//this.cedulaCliente = cedulaCliente;
		//this.fechaDesde = fechaDesde;
		//this.fechaHasta = fechaHasta;
		//this.aplicaQuery = aplicaQuery;
		this.paramFiltroConServicioGlobal=objFiltroConServicioParam;
		this.lista = new ArrayList<SeguimientoVehNuevosDto>();		
		try {
			service = (CxcContactoServicioServiceLocal) ServiceLocator
					.getService(NombreJNDI.CXC_CONTACTO_SERVICIO_SERVICE);
		} catch (ServiceLocatorException e) {
			Log.error("Error en inicializar contructor:LDMSeguimientoVehNuevos()" + e);
		}

	}

	@Override
	public List<SeguimientoVehNuevosDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			if (!this.refresh) {
				//FiltrosConServicioDto objFiltroServicio = new FiltrosConServicioDto();
				/*objFiltroServicio.setUsuAsesor(this.asesor);
				objFiltroServicio.setCedCliente(this.cedulaCliente);
				objFiltroServicio.setFechaIinicio(this.fechaDesde);
				objFiltroServicio.setFechaFin(this.fechaHasta);
				objFiltroServicio.setAplicaContacto(this.aplicaQuery);*/
				this.setRowCount(service
						.countDetalleSegVehiculosNuevos(this.noCia, this.centro, this.noLinea,this.paramFiltroConServicioGlobal,0,0, filters)
						.intValue());
				this.setLista(loadData(this.noCia, this.centro, this.noLinea, this.paramFiltroConServicioGlobal, filters, this.firstGlobal, this.pageSizeGlobal));
			}
			this.refresh = false;
		} catch (FindException e) {
			Log.error("Error en inicializar contructor:LDMSeguimientoVehNuevos()" + e);
		}
		return this.lista;
	}

	@Override
	public SeguimientoVehNuevosDto getRowData(String rowKey) {
		for (SeguimientoVehNuevosDto objeto : this.lista) {
			if (objeto.getChasis() != null && objeto.getChasis().equals(rowKey)) {
				return objeto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(SeguimientoVehNuevosDto objeto) {
		return objeto.getChasis();
	}

	private List<SeguimientoVehNuevosDto> loadData(String noCia, String centro, List<String> noLinea, FiltrosConServicioDto objFiltroConServicioParam, Map<String, Object> filters, int first, int maxSize)
			throws FindException {
		this.first = first;
		/*FiltrosConServicioDto objFiltroServicio = new FiltrosConServicioDto();
		objFiltroServicio.setUsuAsesor(asesor);
		objFiltroServicio.setCedCliente(cedulaCliente);
		objFiltroServicio.setFechaIinicio(fechaDesde);
		objFiltroServicio.setFechaFin(fechaHasta);
		objFiltroServicio.setAplicaContacto(aplicaQuery);*/
		List<SeguimientoVehNuevosDto> lista = service.consultarDetalleSegVehiculosNuevos(noCia, centro, noLinea,objFiltroConServicioParam, first, maxSize, filters);
		return lista;
	}

	public CxcContactoServicioServiceLocal getService() {
		return service;
	}

	public void setService(CxcContactoServicioServiceLocal service) {
		this.service = service;
	}

	public List<SeguimientoVehNuevosDto> getLista() {
		return lista;
	}

	public void setLista(List<SeguimientoVehNuevosDto> lista) {
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

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public List<String> getNoLinea() {
		return noLinea;
	}

	public void setNoLinea(List<String> noLinea) {
		this.noLinea = noLinea;
	}

	public String getCedulaCliente() {
		return cedulaCliente;
	}

	public void setCedulaCliente(String cedulaCliente) {
		this.cedulaCliente = cedulaCliente;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	public String getAsesor() {
		return asesor;
	}

	public void setAsesor(String asesor) {
		this.asesor = asesor;
	}

	public boolean isAplicaQuery() {
		return aplicaQuery;
	}

	public void setAplicaQuery(boolean aplicaQuery) {
		this.aplicaQuery = aplicaQuery;
	}

}