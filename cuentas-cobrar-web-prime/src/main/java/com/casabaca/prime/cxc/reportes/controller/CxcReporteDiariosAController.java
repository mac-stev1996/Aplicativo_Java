package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
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

@ViewScoped
@ManagedBean(name = "cxcReporteDiariosAController")
public class CxcReporteDiariosAController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcReporteDiariosAController.class);

	private Date fechaDesde;
	private Date fechaHasta;

	public CxcReporteDiariosAController() {

	}

	@PostConstruct
	public void init() {
	}

	@SuppressWarnings("deprecation")
	public String report() {
		if (fechaDesde != null && fechaHasta != null) {
			String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("format");
			UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
			Connection connection = null;
			Map<String, Object> parameters = new HashMap<String, Object>();
			try {

				String ctxPath = getServletContext().getRealPath("/");
				String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance()
						.getExternalContext().getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
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
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
						.getExternalContext().getResponse();

				if ("pdf".equals(format)) {
					response.setContentType("application/pdf");
					JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
				}

				if ("excel".equals(format)) {
					exportXls(jasperPrint, response);
				}

				if ("html".equals(format)) {
					exportHTML(jasperPrint, response);
				}
				virtualizer.cleanup();
				FacesContext.getCurrentInstance().getApplication().getStateManager()
						.saveView(FacesContext.getCurrentInstance());
				FacesContext.getCurrentInstance().responseComplete();
				response.getOutputStream().flush();
				response.getOutputStream().close();
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
		} else {
			error("Es necesario seleccionar la fecha desde y la fecha hasta para la generación del reporte");
			return null;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setParameters(Map parameters) {

		parameters.put("P_NOCIA", getCompania().getNoCia());
		parameters.put("P_NOMBRE_EMPRESA", getCompania().getNombre());
		parameters.put("P_FECHA_INI", this.getFechaDesde());
		parameters.put("P_FECHA_FIN", this.getFechaHasta());
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
