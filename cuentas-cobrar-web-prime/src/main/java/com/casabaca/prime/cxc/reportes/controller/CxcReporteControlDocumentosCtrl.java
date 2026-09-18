package com.casabaca.prime.cxc.reportes.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
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
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ManagedBean
@ViewScoped
public class CxcReporteControlDocumentosCtrl extends CommonController {
	private static final Logger LOGGER = Logger.getLogger(CxcReporteControlDocumentosCtrl.class);
	
	private static final String PATH_REPORTE_ACTIVIDADES_VENDEDORES_APP ="/reportes/control-documentos-entrega.jasper";
	private static final String JASPER_PARAM_NO_CIA = "noCia";
	private static final String JASPER_PARAM_QUERY = "query";
	private static final String JASPER_PARAM_CEDULA = "cedulaCliente";
	private static final String JASPER_PARAM_FECHA_INICIO = "fechaInicio";
	private static final String JASPER_PARAM_FECHA_FIN = "fechaFin";
	
	private Date fechaInicio;
	private Date fechaFin;
	private String cedulaCliente;
	private String noCia;
	
	@PostConstruct
	public void init () {
		noCia = getCompania().getNoCia();
	}
	
	public void generarReportePdf () {
		Connection connection = prepararConexion();
		Map<String, Object> jasperParams = buildJasperParams();
		String ctxPath = getServletContext().getRealPath("/");
		try {
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			jasperParams.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + PATH_REPORTE_ACTIVIDADES_VENDEDORES_APP,
					jasperParams, connection);
			virtualizer.cleanup();
			sendPdfResponse(jasperPrint);
		} catch (JRException e) {
			error("Problemas al intentar llenar el reporte.");
			e.printStackTrace();
		}
		 finally {
				try {
					connection.close();
				} catch (SQLException e) {
					error("Problemas al intentar llenar el reporte.");
				}
			}
	}
	
	public void sendPdfResponse(JasperPrint jasperPrint) {
		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append("control-documentos");
		header.append(".pdf");
		header.append("\"");

		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		try {
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			LOGGER.error("No se pudo obtener el response PDF para el reporte: "+PATH_REPORTE_ACTIVIDADES_VENDEDORES_APP);
		} catch (JRException e) {
			LOGGER.error(e.getMessage());
			LOGGER.error("No se pudo contruir el PDF para el reporte: "+PATH_REPORTE_ACTIVIDADES_VENDEDORES_APP);
		}
	}
	
	public void generarReporteExcel() {
		Connection connection = prepararConexion();
		Map<String, Object> jasperParams = buildJasperParams();
		String ctxPath = getServletContext().getRealPath("/");
		try {
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			jasperParams.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + PATH_REPORTE_ACTIVIDADES_VENDEDORES_APP,
					jasperParams, connection);
			byte[] excelBytes = buildExcelByteArray(jasperPrint);
			sendExcelResponse(excelBytes);
		} catch (JRException e) {
			error("Problemas al intentar llenar el reporte.");
			e.printStackTrace();
		}
		finally {
			try {
				connection.close();
			} catch (SQLException e) {
				error("Problemas al intentar llenar el reporte.");
			}
		}
	}
	
	public byte[] buildExcelByteArray (JasperPrint jasperPrint) {
		JRXlsxExporter exporter = new JRXlsxExporter();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND, CommonConstants.FALSE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
				CommonConstants.TRUE_VALUE);
		try {
			exporter.exportReport();
			return byteArrayOutputStream.toByteArray();
		} catch (JRException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException("No se pudo exportar el reporte con Jasper");
		}
	}
	
	public void sendExcelResponse (byte[] content) {
		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append("control-documentos");
		header.append(".xlsx");
		header.append("\"");

		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(content.length);
		try {
			response.getOutputStream().write(content, 0, content.length);
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException("No se puede generar el response del Excel.");
		}
	}
	
	private Connection prepararConexion () {
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		try {
			return utilServiceDelegate.getDataSource().getConnection();
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException("No se puede conectar a la base para crear el reporte.");
		}
	}
	
	public Map<String, Object> buildJasperParams () {
		Map<String, Object> jasperParams = new HashMap<>();
		jasperParams.put(JASPER_PARAM_NO_CIA, noCia);
		String query = "";
		if (cedulaCliente != null && cedulaCliente.length() > 0) {
			query += " and a.cedula_deudor = $P{cedulaCliente} ";
			jasperParams.put(JASPER_PARAM_CEDULA, cedulaCliente);
		} else {
			if (fechaInicio == null && fechaFin != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fechaFin);
				cal.add(Calendar.DATE, -14);
				fechaInicio = cal.getTime();
			} else if (fechaInicio != null && fechaFin == null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(fechaInicio);
				cal.add(Calendar.DATE, 14);
				fechaFin = cal.getTime();
			} else if(fechaInicio == null && fechaFin == null) {
				Calendar cal = Calendar.getInstance();
				fechaFin = cal.getTime();
				cal.add(Calendar.DATE, -14);
				fechaInicio = cal.getTime();
			}
			query += " and b.fecha_entrega >= $P{fechaInicio}  and b.fecha_entrega <= $P{fechaFin} ";
			jasperParams.put(JASPER_PARAM_FECHA_INICIO, fechaInicio);
			jasperParams.put(JASPER_PARAM_FECHA_FIN, fechaFin);
		}
		jasperParams.put(JASPER_PARAM_QUERY, query);
		return jasperParams;
	}
	
	public Date getFechaInicio() {
		return fechaInicio;
	}
	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}
	public Date getFechaFin() {
		return fechaFin;
	}
	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}
	public String getCedulaCliente() {
		return cedulaCliente;
	}
	public void setCedulaCliente(String cedulaCliente) {
		this.cedulaCliente = cedulaCliente;
	}	
}
