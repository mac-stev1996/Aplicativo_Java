/**
 * 
 */
package com.casabaca.prime.cxc.reportes.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.casabaca.common.FechaUtils;
import com.casabaca.prime.cxc.common.ReportCommonController;

/**
 * @author rr_reyes
 *
 */
@ViewScoped
@ManagedBean(name = "cxcCarteraVenxVenSinEmpRelaController")
public class CxcCarteraVenxVenSinEmpRelaController extends ReportCommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(CxcCarteraVenxVenSinEmpRelaController.class);
	private static final int valorAnios = 4;
	
	@PostConstruct
	public void init() {
		try {
			setAnoHasta(FechaUtils.getAno(new Date()));
			setMesHasta(FechaUtils.getMes(new Date()));
		} catch (Exception e) {
			LOG.error(e);
			error("Error: init(): " + e.getMessage());
		}
	}
	
	private void setParameters(Map<String, Object> parameters, Date fechaCierre) {
		parameters.put("NO_CIA", getCompania().getNoCia());
		parameters.put("ANO_CIERRE", getAnoHasta());
		parameters.put("MES_CIERRE", getMesHasta());
    	parameters.put("FECHA_CIERRE", fechaCierre);
	}
	
	public void report(String formato) {
		try
		{
			if(getAnoHasta() == null ) {
				error("¡Info: " + "Ingrese el año de cierre.");
				return;
			}
			else if(getAnoHasta() > FechaUtils.getAno(new Date())) {
				error("¡Info: " + "El año de cierre no puede ser mayor al actual.");
				return;
			}
			else if(Integer.toString(getAnoHasta()).length() < valorAnios) {
				error("¡Info: " + "El formato del año de cierre debe ser: YYYY");
				return;
			}
			else if(getMesHasta() == null ) {
				error("¡Info: " + "Ingrese el mes de cierre.");
				return;
			}
			else if((getMesHasta() > FechaUtils.getMes(new Date()) && getAnoHasta() == FechaUtils.getAno(new Date()))) {
				error("¡Info: " + "El mes de cierre no puede ser mayor al actual.");
				return;
			}
			else if (!(getMesHasta() >= 1 && getMesHasta() <= 12)) {
				error("¡Info: " + "El mes de cierre debe ser entre 1 y 12");
				return;
			}
			
			SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");	 
			Integer ultimoDiaMes = (Integer) FechaUtils.obtenerUltimoDiaMes(getAnoHasta(), getMesHasta());
			Date fechaCierre = formatoFecha.parse(ultimoDiaMes.toString().concat("/").concat(getMesHasta().toString()).concat("/").concat(getAnoHasta().toString()));
			
			String reportJasper =  "/cxc/cxcCarteraAnualControlContaGen".concat(getCompania().getNoCia());
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			setParameters(parameters, fechaCierre);
			
			if ("S".equals(getCompania().getServidorJasper())) {
				FacesContext.getCurrentInstance().getExternalContext().redirect(super.callJasperReport(reportJasper, parameters, formato));	
			}
		} catch (Exception e) {
			LOG.error(e);
			error("Error: report(): " + e.getMessage());
		} 
	}
}
