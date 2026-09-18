package com.casabaca.prime.cxc.reportes.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean(name = "cxcReporteCreditoPostventaController")
public class CxcReporteCreditoPostventaController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcReporteCreditoPostventaController.class);

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	private List<Agencia> agencias;

	private Date fechaDesde;
	private Date fechaHasta;

	private String agencia;
	private String estado;
	private String asesor;

	public CxcReporteCreditoPostventaController() {

	}

	@PostConstruct
	public void init() {
		String[] order = { "nombre" };
		agencias = agenciaService.findByCompania(getCompania().getNoCia(), order);
	}

	public String report() {
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		Map<String, Object> parameters = new HashMap<String, Object>();
		try {

			String ctxPath = getServletContext().getRealPath("/");
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			setParameters(parameters);
			if ("excel".equals(format)) {
				parameters.put("IMPRIME_CABECERA", false);
			}

			connection = utilServiceDelegate.getDataSource().getConnection();

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + getStrReportPath(), parameters,
					connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			}

			if ("excel".equals(format)) {

				exportXlsT(jasperPrint, response, getStrReportPath());
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
	}

	protected void exportXlsT(JasperPrint jasperPrint, HttpServletResponse response, String sourceReportPath)
			throws JRException, IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JRXlsExporter exporter = new JRXlsExporter();
		exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND, CommonConstants.FALSE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_IGNORE_CELL_BORDER, CommonConstants.FALSE_VALUE);
		exporter.exportReport();
		byte[] bytes = byteArrayOutputStream.toByteArray();

		StringBuffer header = new StringBuffer();
		header.append("inline; filename=\"");
		header.append("ReporteCreditoExpress.xls");
		header.append("\"");
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(bytes.length);
		ServletOutputStream outputStream = response.getOutputStream();
		outputStream.write(bytes, 0, bytes.length);
		outputStream.flush();
		outputStream.close();
	}

	@SuppressWarnings("unchecked")
	private void setParameters(Map parameters) {

		parameters.put("noCia", getCompania().getNoCia());
		parameters.put("NombreCia", getCompania().getNombre());

		parameters.put("agencia", agencia);
		parameters.put("fechaIni", fechaDesde);

		parameters.put("fechaFin", fechaHasta);
		parameters.put("estado", estado);
		parameters.put("asesor", asesor);

	}

	protected String getStrReportPath() {
		String reportPath = null;
		reportPath = "/reportes/RepCreditosPostventaExpress.jasper";
		return reportPath;
	}

//	private String getStrReportCredito(boolean esTotalizado) {
//		String reportPath = null;
//		if (esTotalizado) {
//			reportPath = "/reportes/RepCreditosPostventaExpress.jasper";
//		} else {
//			reportPath = "/reportes/RepCreditosPostventaExpress.jasper";
//		}
//		return reportPath;
//	}

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

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public void setAgencias(List<Agencia> agencias) {
		this.agencias = agencias;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getAsesor() {
		return asesor;
	}

	public void setAsesor(String asesor) {
		this.asesor = asesor;
	}

}
