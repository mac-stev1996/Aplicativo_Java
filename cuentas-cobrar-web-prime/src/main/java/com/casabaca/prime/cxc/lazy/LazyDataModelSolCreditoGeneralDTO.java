package com.casabaca.prime.cxc.lazy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCreditoGlobal;
import com.casabaca.cxc.ejb.servicio.CxcSolicitudCreditoGlobalServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.seminuevos.ejb.modelo.UsdCatalogueDetail;
import com.casabaca.seminuevos.ejb.servicio.UsdCatalogueDetailServiceLocal;

public class LazyDataModelSolCreditoGeneralDTO extends LazyDataModel<CxcSolicitudCreditoGlobal> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LazyDataModelSolCreditoGeneralDTO.class);
	private static final String VAL_COD_CATALOGO_NOM_EMPRESAS="NEBCG";
	private static final String VAL_NO_REGISTRA="NO_REGISTRA";
	private CxcSolicitudCreditoGlobalServiceLocal solicitudesServiceLocal;
	
	private List<CxcSolicitudCreditoGlobal> solicitudes;
	private CxcSolicitudCreditoGlobal solicitud; 
	private UsdCatalogueDetailServiceLocal usdCatalogoService;
	
	private Map<String, Object> filtros;
	private List<UsdCatalogueDetail> listCatalogoNomEmpresas;
	
	public LazyDataModelSolCreditoGeneralDTO(CxcSolicitudCreditoGlobal solicitud, Map<String, Object> filtros) {
		this.solicitudes = new ArrayList<>();
		this.solicitud = solicitud;
		this.filtros = filtros;
		try {
			solicitudesServiceLocal = (CxcSolicitudCreditoGlobalServiceLocal) ServiceLocator.getService(NombreJNDI.CXC_CREDITO_GLOBAL_SERVICE);
			usdCatalogoService=(UsdCatalogueDetailServiceLocal) ServiceLocator.getService(NombreJNDI.USD_CATALOGUE_DETAIL_SERVICE);
			cargarCatalogoNombreEmpresas();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public List<CxcSolicitudCreditoGlobal> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {			
			logger.info(":buscar carga solicitudes desde: " + first + " Filtros: " + this.filtros);
			this.setSolicitudes(loadData(solicitud, this.filtros, first, pageSize));
			if (getSolicitudes() != null && !getSolicitudes().isEmpty()) {
				this.setRowCount(solicitudesServiceLocal.obtenerTotalSolicitudes(solicitud, this.filtros).intValue());
			}else {
				this.setRowCount(0);
			}
		} catch (FindException e) {
			logger.error(e);
		}
		return this.solicitudes;
	}
	
	public void cargarCatalogoNombreEmpresas() {
		try {
			listCatalogoNomEmpresas = new ArrayList<UsdCatalogueDetail>();
			listCatalogoNomEmpresas = usdCatalogoService.consultarCatalogoActivoPorNoCiaAndSiglaCab(CommonConstants.CENTRIC,
					VAL_COD_CATALOGO_NOM_EMPRESAS);
			if (listCatalogoNomEmpresas == null) {
				logger.error("Error:No se ha encontrado el catalogo para el nombre de las empresas");
			}
		} catch (Exception e) {
			logger.error("Error:No se ha encontrado el catalogo para el nombre de las empresas");
		}
	}


	private List<CxcSolicitudCreditoGlobal> loadData(CxcSolicitudCreditoGlobal solicitud,
			Map<String, Object> filters, int first, int maxSize) throws FindException {
		this.solicitudes = solicitudesServiceLocal.obtenerListaSolicitudes(solicitud, first, maxSize, filters);
		this.cargaInformacionAdicionalList();
	    this.solicitudes.sort(Comparator
	        .comparing(CxcSolicitudCreditoGlobal::getPuesto, Comparator.nullsLast(Comparator.naturalOrder()))
	        .thenComparing(CxcSolicitudCreditoGlobal::getFechaEnviada));
		return this.solicitudes;
	}

	private void cargaInformacionAdicionalList() {
		if (solicitudes != null && !solicitudes.isEmpty()) {
			for (CxcSolicitudCreditoGlobal item : solicitudes) {
				if (item.getEstado() != null) {
					asignarCompania(item);
					asignarEstado(item);
					asignarFechaEntrega(item);
				}
			}
		}
	}

	private void asignarFechaEntrega(CxcSolicitudCreditoGlobal item) {
		if(null!=item.getFechaUsuarioRevision())
			item.setFechaEntrega(FechaUtils.sumarNMinutos(item.getFechaUsuarioRevision(), 25));
		
	}

	private void asignarCompania(CxcSolicitudCreditoGlobal item) {
		item.setNombreEmpresa(VAL_NO_REGISTRA);
		if (this.listCatalogoNomEmpresas != null && !this.listCatalogoNomEmpresas.isEmpty()) {
			this.listCatalogoNomEmpresas.stream().forEach(objItem -> {
				if (objItem.getSigla() != null && !objItem.getSigla().isEmpty() && item.getPk().getNoCia().equals(objItem.getSigla())) {
					item.setNombreEmpresa(objItem.getDescripcion());
				}
			});
		}		
	}

	private void asignarEstado(CxcSolicitudCreditoGlobal item) {
		if ("A".equals(item.getEstado())) {
			item.setNombreEstado("Aprobado");
		} else if ("E".equals(item.getEstado())) {
			item.setNombreEstado("Enviado");
		} else if ("D".equals(item.getEstado())) {
			item.setNombreEstado("Devuelto");
		} else if ("N".equals(item.getEstado())) {
			item.setNombreEstado("Negado");
		} else if ("R".equals(item.getEstado())) {
			item.setNombreEstado("En aprobación");
		} else if ("P".equals(item.getEstado())) {
			item.setNombreEstado("En Confirmación");
		} else if ("L".equals(item.getEstado())) {
			item.setNombreEstado("Asignado");
		}
	}
	
	public List<CxcSolicitudCreditoGlobal> getSolicitudes() {
		return solicitudes;
	}

	public void setSolicitudes(List<CxcSolicitudCreditoGlobal> solicitudes) {
		this.solicitudes = solicitudes;
	}

	public CxcSolicitudCreditoGlobal getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(CxcSolicitudCreditoGlobal solicitud) {
		this.solicitud = solicitud;
	}

	public Map<String, Object> getFiltros() {
		return filtros;
	}

	public void setFiltros(Map<String, Object> filtros) {
		this.filtros = filtros;
	}
}