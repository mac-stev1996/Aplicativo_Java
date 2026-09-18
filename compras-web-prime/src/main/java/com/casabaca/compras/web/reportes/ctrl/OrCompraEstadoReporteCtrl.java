package com.casabaca.compras.web.reportes.ctrl;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import com.casabaca.compras.web.common.controller.CommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ManagedBean
@ViewScoped
public class OrCompraEstadoReporteCtrl extends CommonController {
	
	private static final Logger LOGGER = Logger.getLogger(OrCompraEstadoReporteCtrl.class);

	private static final String PATH_REPORTE_OR_COMPRA_POR_ESTADO = "/reportes/or-compras-estado.jasper";
	private static final String JASPER_PARAM_FECHA_INICIO = "fecha_inicio";
	private static final String JASPER_PARAM_FECHA_FIN = "fecha_fin";
	private static final String JASPER_PARAM_NO_CIA = "no_cia";
	
	private String noCia;
	private Date fechaInicio;
	private Date fechaFin;
	
	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		
	}
	
	public void generarReporteExcel() {
		Map<String, Object> jasperParams = buildJasperParams();
		String ctxPath = getServletContext().getRealPath("/");

		Connection connection = null;
		try {
			UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
			connection = utilServiceDelegate.getDataSource().getConnection();
			// VIRTUALIZAR
			String pathVirtualizer = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.compras.web.TMP_FILE_PATH") + System.getProperty("file.separator");
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, pathVirtualizer);// 50paginas
			jasperParams.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + PATH_REPORTE_OR_COMPRA_POR_ESTADO,
					jasperParams, connection);
			byte[] excelBytes = buildExcelByteArray(jasperPrint);
			sendExcelResponse(excelBytes);
			virtualizer.cleanup();
		} catch (JRException e) {
			addErrorMessage("Problemas al intentar llenar el reporte.", null);
			e.printStackTrace();
		} catch (SQLException e) {
			addErrorMessage("Problemas al intentar llenar el reporte.", null);
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	
	public Map<String, Object> buildJasperParams() {
		Map<String, Object> jasperParams = new HashMap<>();
		//jasperParams.put(JRParameter.REPORT_VIRTUALIZER, buildVirtualizer());
		jasperParams.put(JASPER_PARAM_NO_CIA, noCia);
		jasperParams.put(JASPER_PARAM_FECHA_INICIO, fechaInicio);
		jasperParams.put(JASPER_PARAM_FECHA_FIN, fechaFin);
		return jasperParams;
	}
	
	public JRFileVirtualizer buildVirtualizer() {

		String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
				.getInitParameter("com.casabaca.servicios.web.TMP_FILE_PATH") + System.getProperty("file.separator");

		File[] drives = File.listRoots();
		for (File fileDrives : drives) {
			if (fileDrives.getPath().length() > 1 && fileDrives.getPath().substring(0, 1).equals("C")) {
				path = fileDrives.getPath() + path;
			}
		}
		return new JRFileVirtualizer(10, path);
	}

	public byte[] buildExcelByteArray(JasperPrint jasperPrint) {
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

	public void sendExcelResponse(byte[] content) {
		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append("reporte-or-compra-estado");
		header.append(".xlsx");
		header.append("\"");

		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
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
}
