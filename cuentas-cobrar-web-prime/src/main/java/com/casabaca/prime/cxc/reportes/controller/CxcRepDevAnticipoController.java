/* 
 * CxcRepDevAnticipoController.java 
 * 24 jun. 2024
 * Copyright 2024 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * <b> Descripcion de la clase, interface o enumeracion. </b>
 * @author laura.llangari
 *
 * @version $1.0$
 */

@ViewScoped
@ManagedBean
public class CxcRepDevAnticipoController  extends CommonController implements Serializable{

	
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Logger LOG = Logger.getLogger(CxcRepDevAnticipoController.class);
	private static final String NOMBREREPORTE = "/cxc/CxcRepSolicitudDevolucionAnticipos";
	private static final String TITULOREPORTE="REPORTE DE SOLICITUDES DE DEVOLUCIONES DE ANTICIPOS";
	private String noCia;
	private String formato;
	private Date   fechaInicio;
	private Date   fechaFin;
	private String reporteRuta;
	private String nombreEmpresa;

	/**
	 * 
	 */
	public CxcRepDevAnticipoController() {
		// TODO Auto-generated constructor stub
	}
	
	
	@PostConstruct
	public void init() {
		this.nombreEmpresa=getCompania().getNombre();

	}
	
	public void limpiarDatos() {
		setFechaFin(new Date());
		setFechaInicio(new Date());
		
	}
	
	public void excel() {
		if (validarCamposObligatorios()) {
			
			this.formato = CommonConstants.OUTPUT_XLSX_NO_PAG;
			reporteRuta = NOMBREREPORTE + CommonUtils.getCiaReportUnit(getCompania().getNoCia());
			reporte();
			
		}		
	}

	public void reporte() {
		Map<String, Object> parameters = new HashMap<>();
		setParameters(parameters);
		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(super.callJasperReport(this.reporteRuta, parameters, this.formato));
		} catch (IOException e) {
			LOG.error(e);
		}
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("p_fecha_hasta", this.fechaFin);
		parameters.put("p_fecha_desde",this.fechaInicio);
		parameters.put("p_nombreEmpresa", this.nombreEmpresa);
		parameters.put("p_titulo", TITULOREPORTE);

	}

	private Boolean validarCamposObligatorios() {
		
		if (this.fechaInicio == null ) {
			addErrorMessage(" Error ", " Debe ingresar informacion de Fecha Desde ");
			return false;
			
			
		}else if (this.fechaFin == null ) {
			addErrorMessage(" Error ", " Debe ingresar informacion de Fecha Hasta ");
			return false;
		}
		
		return true;		
	} 
	

	// Getter y Setter
	
	
	/**
	 * @return the noCia
	 */
	public String getNoCia() {
		return noCia;
	}


	/**
	 * @param noCia the noCia to set
	 */
	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}


	/**
	 * @return the formato
	 */
	public String getFormato() {
		return formato;
	}


	/**
	 * @param formato the formato to set
	 */
	public void setFormato(String formato) {
		this.formato = formato;
	}


	/**
	 * @return the fechaInicio
	 */
	public Date getFechaInicio() {
		return fechaInicio;
	}


	/**
	 * @param fechaInicio the fechaInicio to set
	 */
	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}


	/**
	 * @return the fechaFin
	 */
	public Date getFechaFin() {
		return fechaFin;
	}


	/**
	 * @param fechaFin the fechaFin to set
	 */
	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}


	

	/**
	 * @return the reporteRuta
	 */
	public String getReporteRuta() {
		return reporteRuta;
	}


	/**
	 * @param reporteRuta the reporteRuta to set
	 */
	public void setReporteRuta(String reporteRuta) {
		this.reporteRuta = reporteRuta;
	}


	/**
	 * @return the nombreEmpresa
	 */
	public String getNombreEmpresa() {
		return nombreEmpresa;
	}


	/**
	 * @param nombreEmpresa the nombreEmpresa to set
	 */
	public void setNombreEmpresa(String nombreEmpresa) {
		this.nombreEmpresa = nombreEmpresa;
	}

	
	
	
	
	
	
	
	

}
