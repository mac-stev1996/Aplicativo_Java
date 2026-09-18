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
@ManagedBean(name = "cxcRepDiarioRecaudacionController")
public class CxcRepDiarioRecaudacionController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcRepDiarioRecaudacionController.class);


	private Date fechaDesde;
	private Date fechaHasta;

	
	public CxcRepDiarioRecaudacionController() {

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
//		if ("S".equals(getCompania().getServidorJasper())) {
			try {
				// LLamar al servidor Jasper
				reportJasper("/cxc/cxcRepDiarioRecaudacion/cxcRepDiarioRecaudacion"+CommonUtils.getCiaReportUnit(getCompania().getNoCia()));
				
			} catch (Exception e) {
				logger.error("ERROR en reporte Jasper" + e);
			}
			
			
//		}else{
//			error("La compania no tiene configurado el servidor Jasper");
//			
//		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void setParameters(Map parameters) {
		parameters.put("p_fecha_desde", fechaDesde);
		parameters.put("p_fecha_hasta", fechaHasta);
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

	
}
