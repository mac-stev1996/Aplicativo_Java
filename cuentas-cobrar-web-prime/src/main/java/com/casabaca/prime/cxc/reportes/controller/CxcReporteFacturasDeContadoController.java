
package com.casabaca.prime.cxc.reportes.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.prime.cxc.common.ReportCommonController;

/**
 * 
 * @author rr_reyes
 */

@ManagedBean(name = "cxcReporteFacturasDeContadoController")
@RequestScoped
public class CxcReporteFacturasDeContadoController extends ReportCommonController{
			
	private static final String REPORTE_PATH = "/cxc/CxcFacturasDeContado";


	
	@PostConstruct
	public void init() {
		setFechaDesde(FechaUtils.truncarFecha(FechaUtils.sumarNDias(new Date(), -30)));
		setFechaHasta(FechaUtils.truncarFecha(new Date()));
	}	

	public void reporte(String formato) {
		try
		{	
			if(validar()) {
				Map<String, Object> parameters = new HashMap<>();
				setParameters(parameters);
				FacesContext.getCurrentInstance().getExternalContext().redirect(super.callJasperReport(REPORTE_PATH, parameters, "excel".equals(formato) ? CommonConstants.OUTPUT_XLSX : CommonConstants.OUTPUT_PDF));
				
			}
			
		} catch (Exception e) {
			error("Se presento un error al generar el reporte, comuníquese con sistemas: " + e.getMessage());
		}
	}
	
	private boolean validar() {
		if(getFechaDesde() == null || getFechaHasta() == null) {
			error("Las fechas Desde y Hasta son requeridas.");
			return false;
		}
		if(getFechaDesde().after(getFechaHasta())) {
			warn("La fecha desde debe ser anterior a fecha hasta");
			return false;
		}
		return true;
	}
	
	private void setParameters(Map<String, Object> parameters) {
		parameters.put("p_NoCia", getCompania().getNoCia());
		parameters.put("p_fechaInicio", getFechaDesde());
		parameters.put("p_fechaFin", getFechaHasta());
	}

}
