package com.casabaca.prime.cxc.reportes.controller;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import com.casabaca.common.CommonUtils;

@ManagedBean(name = "cxcConfirmacionDepositoController")
@RequestScoped
public class CxcConfirmacionDepositoController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcConfirmacionDepositoController.class);

	private String resultado;

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
		if(getFechaDesde() == null || getFechaHasta() == null ) {
			error("Los valores de fechas son requeridos.");
			return null;
		}
		if ("S".equals(getCompania().getServidorJasper())) {

			try {
				// LLamar al servidor Jasper
				if(CommonConstants.SUZUKI.equals(getCompania().getNoCia())) {
					reportJasper("/cxc/cxcConfirmacionDepositosSZK");
					return null;
				}else {
					reportJasper("/cxc/CxcConfirmacionDepositos/cxcConfirmacionDepositos"+CommonUtils.getCiaReportUnit(getCompania().getNoCia()));
					return null;
				}				
			} catch (Exception e) {
				logger.error("ERROR en reporte Jasper" + e);
			}
			
			return null;
		}
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		Map<String, Object> parameters = new HashMap<String, Object>();
		try {

			String ctxPath = getServletContext().getRealPath("/");
			
			// VIRTUALIZAR
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
						
			setParameters(parameters);
			connection = utilServiceDelegate.getDataSource().getConnection();

			if ("excel".equals(format)) {
				parameters.put("IMPRIME_CABECERA", CommonConstants.FALSE_VALUE);
			}
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + getStrReportPath(), parameters,
					connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}else if ("excel".equals(format)) {
				exportXls(jasperPrint, response);
			} else{
				exportHTML(jasperPrint, response);
			}
			
			FacesContext.getCurrentInstance().renderResponse();
			FacesContext.getCurrentInstance().responseComplete();
			virtualizer.cleanup();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e.getCause());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e.getCause());
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e.getCause());
			}
		}
		return null;
	}


	private void setParameters(Map<String, Object> parameters) {
		parameters.put("NO_CIA", getCompania().getNoCia());
		parameters.put("VFECHAINI", getFechaDesde());
		parameters.put("VFECHAFIN", getFechaHasta());
		
	}


	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

}
