package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.cml.dao.CmlRangosRiesgoDaoLocal;
import com.casabaca.cxc.cml.model.CmlRangosRiesgo;
import com.casabaca.cxc.ejb.dao.MatrizRiesgoDaoLocal;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.prime.cxc.common.ReportCommonController;



@ManagedBean
@ViewScoped
public class CxcDatMatrizRiesgosController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcDatMatrizRiesgosController.class);
	
	static String REPORTE= "/cxc/cxcDatMatrizRiesgos/cxcDatMatrizRiesgos";
	private String reporte = "";
	private static final String NOMBRE_REPORTE = "Matriz de Riesgo";
	
	private CmlRangosRiesgoDaoLocal cmlRangosRiesgoDaoLocal;
	private MatrizRiesgoDaoLocal matrizRiesgoDaoLocal;
	
	private Integer imprimeCabecera;	
	private String formato;
	

	@PostConstruct
	public void init() {
		try {
			cmlRangosRiesgoDaoLocal = (CmlRangosRiesgoDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlRangosRiesgoDaoBean");
			matrizRiesgoDaoLocal = (MatrizRiesgoDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/MatrizRiesgoDaoBean");
		} catch (ServiceLocatorException e) {
			addErrorMessage("Error", "inicializar servicios "+e.getMessage());
			logger.error("error inicializar servicios "+e.getMessage());
		}
	}
	
	
	public void html() {
		this.formato = CommonConstants.OUTPUT_PDF;
		reporte = REPORTE+CommonUtils.getCiaReportUnit(getCompania().getNoCia());
		reporte();
	}

	public void excel() {
		this.formato = CommonConstants.OUTPUT_XLSX_NO_PAG;
		reporte = REPORTE+CommonUtils.getCiaReportUnit(getCompania().getNoCia());
		reporte();
	}

	public void pdf() {
		this.formato = CommonConstants.OUTPUT_PDF;
		reporte = REPORTE+CommonUtils.getCiaReportUnit(getCompania().getNoCia());
		reporte();
	}
	
	public void reporte() {
		if(validar()) {
			try {
				matrizRiesgoDaoLocal.actualizarDatos(getCompania().getNoCia(), new java.sql.Date(getFechaDesde().getTime())  , new java.sql.Date(getFechaHasta().getTime()) );
				Map<String, Object> parameters = new HashMap<>();
				setParameters(parameters);
				FacesContext.getCurrentInstance().getExternalContext().redirect(super.callJasperReport(reporte, parameters, this.formato));
			} catch (SQLException e) {
				addErrorMessage("Error", "actualizar matriz de riesgo "+e.getMessage());
				logger.error("error actualizar matriz de riesgo "+e.getMessage());
			} catch (IOException e) {
				addErrorMessage("Error", "presentar matriz de riesgo "+e.getMessage());
				logger.error("error presentar matriz de riesgo "+e.getMessage());
			}
		}
	}
	
	private boolean validar() {
		if(getFechaDesde().after(getFechaHasta())) {
			warn("La fecha desde debe ser anterior a fecha hasta");
			return false;
		}
		return true;
	}

	private void setParameters(Map<String, Object> parameters) {
		//p_cuenta
		parameters.put("p_fecha_desde", getFechaDesde());
		parameters.put("p_fecha_hasta", getFechaHasta());
		parameters.put("S3S_NOM_EMPRESA", this.getCompania().getNombre());
		parameters.put("p_titulo",NOMBRE_REPORTE );
		parameters.put("p_imprimir_cabecera",imprimeCabecera );
		
		BigDecimal rangoDesde;
		BigDecimal rangoHasta;
		List<CmlRangosRiesgo> ramgosLst = cmlRangosRiesgoDaoLocal.listarActivas(getCompania().getNoCia());
		if(!ramgosLst.isEmpty()) {
			CmlRangosRiesgo rango = ramgosLst.get(0);
			rangoDesde=rango.getDesde();
			rangoHasta=rango.getHasta();
		}else {
			rangoDesde=new BigDecimal(1.5D);
			rangoHasta=new BigDecimal(2.5D);
		}
		parameters.put("p_rango_desde", rangoDesde);
		parameters.put("p_rango_hasta", rangoHasta);

	}


	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}

	public Integer getImprimeCabecera() {
		return imprimeCabecera;
	}

	public void setImprimeCabecera(Integer imprimeCabecera) {
		this.imprimeCabecera = imprimeCabecera;
	}

	
}
