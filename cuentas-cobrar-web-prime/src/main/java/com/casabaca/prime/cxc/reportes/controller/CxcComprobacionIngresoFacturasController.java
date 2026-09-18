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
public class CxcComprobacionIngresoFacturasController extends ReportCommonController {
	private static String REPORTE_URL= "/cxc/repComprobacionIngresoFacturas/repComprobacionIngresoFacturas";
	private String reporteCia;
	
	private String formato;
	private Integer noCliente;
	

	@PostConstruct
	public void init() {
	}
		
	public void html() {
		this.formato = CommonConstants.OUTPUT_PDF;
		reporteCia = REPORTE_URL+CommonUtils.getCiaReportUnit(getCompania().getNoCia());
		reporte();
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
		if(validar()) {
			Map<String, Object> parameters = new HashMap<>();
			setParameters(parameters);
			try {
				super.ejecutarJavascript("openDuplicatedTab('"
						+super.callJasperReport(reporteCia, parameters, this.formato) + "');");
			} catch (Exception e) {
				addErrorMessage("Error al presentar el reporte", e.getMessage());
			}
		}
	}
	
	private boolean validar() {
		if(getFechaDesde()==null) {
			warn("La fecha desde debe ser ingresada");
			return false;
		}
		if(getFechaHasta()==null) {
			warn("La fecha hasta debe ser ingresada");
			return false;
		}
		if(getFechaDesde().after(getFechaHasta())) {
			warn("La fecha desde debe ser anterior a fecha hasta");
			return false;
		}
		return true;
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("p_fecha_desde", getFechaDesde());
		parameters.put("p_fecha_hasta", getFechaHasta());
		parameters.put("S3S_NOM_EMPRESA", this.getCompania().getNombre());
		parameters.put("p_no_cliente", getNoCliente());
	}


	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}

	public Integer getNoCliente() {
		return noCliente;
	}

	public void setNoCliente(Integer noCliente) {
		this.noCliente = noCliente;
	}

	
	
}
