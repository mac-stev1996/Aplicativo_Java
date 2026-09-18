package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean(name = "cxcAnticiposMensualesCB")
public class CxcAnticiposMensualesCB extends ReportCommonController implements Serializable {
	static Logger logger = Logger.getLogger(CxcAnticiposMensualesCB.class);

	private static final long serialVersionUID = -7900641687719651629L;

	private String anio;
	private String mes;

	@PostConstruct
	public void init() {

	}

	public String report() {
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
			} else {
				parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
			}
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + getStrReportPath(), parameters,
					connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			}
			if ("excel".equals(format)) {
				exportXls(jasperPrint, response);
			}

			virtualizer.cleanup();
			FacesContext.getCurrentInstance().getApplication().getStateManager().saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (IOException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			logger.error(e.getMessage(), e.getCause());
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e.getCause());
			}
		}
		return null;
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("NOCIA", getCompania().getNoCia());
		parameters.put("MES", Integer.valueOf(mes));
		parameters.put("ANIO", Integer.valueOf(anio));
		parameters.put("NOMBREEMPRESA", getCompania().getNombre());
	}
	
	
	public SelectItem[] popularAnos(int anoInicial) {
		int anoActual = FechaUtils.getAno(new Date());
		int rango = anoActual - anoInicial;
		SelectItem[] anos = new SelectItem[rango + 1];
		int j = 0;
		for (int i = 2000; i < anoActual + 1; i++) {
			anos[j] = new SelectItem(new Integer(i), new Integer(i).toString());
			j++;
		}
		return anos;
	}
	
	public SelectItem[] getAnios() {
		return popularAnos(2000);
	}

	public SelectItem[] getMeses() {
		return popularMeses();
	}

	public String getAnio() {
		return anio;
	}

	public void setAnio(String anio) {
		this.anio = anio;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}
	
}
