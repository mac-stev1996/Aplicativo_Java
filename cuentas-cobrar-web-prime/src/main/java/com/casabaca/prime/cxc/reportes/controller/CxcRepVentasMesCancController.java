package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.casabaca.common.CommonUtils;
import com.casabaca.prime.cxc.common.ReportCommonController;

/**
 * 
 * @author er_amaguaya
 * @date 16-06-2020
 *
 */
@ManagedBean
@ViewScoped
public class CxcRepVentasMesCancController extends ReportCommonController implements Serializable{

	private static final long serialVersionUID = 1L;

	private String pserieFisico;
	
	@PostConstruct
	private void init() {
		
	}
	
	public void reportJasper() throws IOException {
		Map<String, Object> parameters = new HashMap<>();
		String format = (String) FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("format");
		setParameters(parameters);
		
		FacesContext.getCurrentInstance().getExternalContext().redirect(super.callJasperReport("/cxc/cxcRepVentasMesCanc", parameters, format));
	}
	
	private void setParameters(Map<String, Object> parameters) {
		parameters.put("p_fecha_desde", super.getFechaDesde());
		parameters.put("p_fecha_hasta", super.getFechaHasta());
		if(CommonUtils.isValid(this.pserieFisico))
			parameters.put("p_serieFisico", this.pserieFisico);
	}

	public String getPserieFisico() {
		return pserieFisico;
	}

	public void setPserieFisico(String pserieFisico) {
		this.pserieFisico = pserieFisico;
	}
	
}