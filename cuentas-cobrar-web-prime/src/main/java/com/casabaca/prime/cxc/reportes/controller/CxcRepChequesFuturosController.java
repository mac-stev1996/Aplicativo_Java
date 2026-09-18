package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonUtils;
import com.casabaca.prime.cxc.common.ReportCommonController;

@ViewScoped
@ManagedBean(name = "cxcRepChequesFuturosController")
public class CxcRepChequesFuturosController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcRepChequesFuturosController.class);


	private Date fechaDesde;
	private Date fechaHasta;
	private String estado;

	
	public CxcRepChequesFuturosController() {

	}

	@PostConstruct
	public void init() {
	}

	
	private void reportJasper(String rutaReporte) throws IOException {
		Map<String, Object> parameters = new HashMap<>();
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		setParameters(parameters);
		if ("excel".equals(format)) {
			parameters.put("IMPRIME_CABECERA","N");
		}
		FacesContext.getCurrentInstance().getExternalContext()
				.redirect(super.callJasperReport(rutaReporte, parameters, format));
	}
	
	public String report() {
		if(fechaDesde == null || fechaHasta == null){
			error("Debe seleccionar un rango de fechas");
			return null;
		}
		if ("S".equals(getCompania().getServidorJasper())) {
			try {
				String reporte="/cxc/cxcRepChequesFuturos".concat(CommonUtils.getCiaReportUnit(getCompania().getNoCia()));
				// LLamar al servidor Jasper
				reportJasper(reporte);
				
			} catch (Exception e) {
				logger.error("ERROR en reporte Jasper" + e);
			}
			
			
		}else{
			error("La compania no tiene configurado el servidor Jasper");
			
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void setParameters(Map parameters) {
		parameters.put("p_fecha_desde", fechaDesde);
		parameters.put("p_fecha_hasta", fechaHasta);
		if(estado != null){
			parameters.put("p_estado", estado);	
		}
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

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

}
