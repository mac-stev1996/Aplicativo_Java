package com.casabaca.prime.cxc.reportes.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.prime.cxc.common.ReportCommonController;


@ManagedBean
@ViewScoped
public class CxcAceptaLOPDPController extends ReportCommonController {
	private static String REPORTE_URL= "/cxc/cxcAceptaLOPDP/cxcAceptaLOPDP";
	private String reporteCia;
	
	private String formato;
	

	@PostConstruct
	public void init() {
	}
		
	
	public void excel() {
		this.formato = CommonConstants.OUTPUT_XLSX;
		reporteCia = REPORTE_URL+CommonUtils.getCiaReportUnit(getCompania().getNoCia());
		reporte();
	}

	public void pdf() {
		this.formato = CommonConstants.OUTPUT_PDF;
		reporteCia = REPORTE_URL+CommonUtils.getCiaReportUnit(getCompania().getNoCia());
		reporte();
	}
	
	public void reporte() {
		Map<String, Object> parameters = new HashMap<>();
		setParameters(parameters);
		try {
			super.ejecutarJavascript("openDuplicatedTab('"
					+super.callJasperReport(reporteCia, parameters, this.formato) + "');");
		} catch (Exception e) {
			addErrorMessage("Error al presentar el reporte", e.getMessage());
		}
	}
	

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("S3S_NOM_EMPRESA", this.getCompania().getNombre());
	}


	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}

	
	
}
