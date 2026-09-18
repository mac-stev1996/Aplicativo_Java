/*
 * ReportCommonControl.java
 *
 * Created on Jun 21, 2007, 3:57:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.casabaca.prime.cxc.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.service.delegate.ClienteServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;

/**
 * Jsf backing bean del cual heredan todos los reportes, contiene los metodos y
 * propiedades comunes
 * 
 * 
 * @author er_amaguaya
 * @version $Revision: 1.0 $
 */
public class ReportCommonController extends CommonController {
	private String ano = new Integer(FechaUtils.getAno(new Date())).toString();
	private String mes = new Integer(FechaUtils.getMes(new Date())).toString();

	private Date fechaDesde;

	private Date fechaHasta;

	private Integer mesDesde;

	private Integer anoDesde;

	private Integer mesHasta;

	private Integer anoHasta;

	private HtmlInputHidden reportPath = new HtmlInputHidden();

	private HtmlInputHidden concesionario;

	private List<SelectItem> monthList;

	private String franquicia;

	private Date fecha;

	private String consecionario;

	private String agencia;

	private String bodega;

	private String ubicacion;

	private ClienteServiceDelegate clienteServiceDelegate;

	/**
	 * Crea una nueva instancia de ReportCommonController.
	 */
	public ReportCommonController() {
		clienteServiceDelegate = new ClienteServiceDelegate();

		// Inicializar meses
		monthList = new ArrayList<SelectItem>();
		monthList.add(new SelectItem("0", "Enero"));
		monthList.add(new SelectItem("1", "Febrero"));
		monthList.add(new SelectItem("2", "Marzo"));
		monthList.add(new SelectItem("3", "Abril"));
		monthList.add(new SelectItem("4", "Mayo"));
		monthList.add(new SelectItem("5", "Junio"));
		monthList.add(new SelectItem("6", "Julio"));
		monthList.add(new SelectItem("7", "Agosto"));
		monthList.add(new SelectItem("8", "Septiembre"));
		monthList.add(new SelectItem("9", "Octubre"));
		monthList.add(new SelectItem("10", "Noviembre"));
		monthList.add(new SelectItem("11", "Diciembre"));
		// Inicializar valores por defecto
		Calendar now = new GregorianCalendar();
		setMesDesde(now.get(Calendar.MONTH));
		setMesHasta(now.get(Calendar.MONTH));
		setAnoDesde(now.get(Calendar.YEAR));
		setAnoHasta(now.get(Calendar.YEAR));

//		this.consecionario = getUsuario().getCedulaConcesionario();
//		this.agencia = getUsuario().getAgenciaConcesionario();
	}

	/**
	 * Metodo que devuelve el valor de la propiedad FechaDesde.
	 * 
	 * @return el valor de la propiedad FechaDesde
	 */
	/**
	 * Exporta a formato xls.
	 * 
	 * @param jasperPrint el valor de jasper print
	 * @param response    el valor de response
	 * 
	 * @throws JRException the JR exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void exportXls(JasperPrint jasperPrint, HttpServletResponse response) throws JRException, IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JRXlsExporter exporter = new JRXlsExporter();
		exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, true);
		exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND, CommonConstants.FALSE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_IGNORE_CELL_BORDER, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_COLLAPSE_ROW_SPAN, CommonConstants.TRUE_VALUE);

		exporter.exportReport();
		byte[] bytes = byteArrayOutputStream.toByteArray();

		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append(getXlsFileName());
		header.append("\"");
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(bytes.length);
		// ServletOutputStream outputStream = response.getOutputStream();
		response.getOutputStream().write(bytes, 0, bytes.length);
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	/**
	 * Exporta a formato html.
	 * 
	 * @param jasperPrint el valor de jasper print
	 * @param response    el valor de response
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JRException the JR exception
	 */
	protected void exportHTML(JasperPrint jasperPrint, HttpServletResponse response) throws IOException, JRException {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();

		response.setContentType("text/html");
		JRExporter exporter = new JRHtmlExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_WRITER, response.getWriter());
		// Make images available for the HTML output

		request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
		exporter.setParameter(JRHtmlExporterParameter.IMAGES_MAP, new HashMap());

		exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, request.getContextPath() + "/image?image=");
		exporter.exportReport();
	}
	
	
	protected void exportXlsx(JasperPrint jasperPrint, HttpServletResponse response, String nameReport)
			throws JRException, IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, true);
		exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND, CommonConstants.FALSE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_IGNORE_CELL_BORDER, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_COLLAPSE_ROW_SPAN, CommonConstants.TRUE_VALUE);

		exporter.exportReport();
		byte[] bytes = byteArrayOutputStream.toByteArray();

		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append(nameReport);
		header.append("\"");
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setContentLength(bytes.length);

		response.getOutputStream().write(bytes, 0, bytes.length);
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	/**
	 * Metodo que devuelve el valor de la propiedad MesDesde.
	 * 
	 * @return el valor de la propiedad MesDesde
	 */
	public Integer getMesDesde() {
		return mesDesde;
	}

	/**
	 * Metodo que define el valor de la propiedad mesDesde.
	 * 
	 * @param mesDesde el nuevo valor de mesDesde
	 */
	public void setMesDesde(Integer mesDesde) {
		this.mesDesde = mesDesde;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad AnoDesde.
	 * 
	 * @return el valor de la propiedad AnoDesde
	 */
	public Integer getAnoDesde() {
		return anoDesde;
	}

	/**
	 * Metodo que define el valor de la propiedad anoDesde.
	 * 
	 * @param anoDesde el nuevo valor de anoDesde
	 */
	public void setAnoDesde(Integer anoDesde) {
		this.anoDesde = anoDesde;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad MesHasta.
	 * 
	 * @return el valor de la propiedad MesHasta
	 */
	public Integer getMesHasta() {
		return mesHasta;
	}

	/**
	 * Metodo que define el valor de la propiedad mesHasta.
	 * 
	 * @param mesHasta el nuevo valor de mesHasta
	 */
	public void setMesHasta(Integer mesHasta) {
		this.mesHasta = mesHasta;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad AnoHasta.
	 * 
	 * @return el valor de la propiedad AnoHasta
	 */
	public Integer getAnoHasta() {
		return anoHasta;
	}

	/**
	 * Metodo que define el valor de la propiedad anoHasta.
	 * 
	 * @param anoHasta el nuevo valor de anoHasta
	 */
	public void setAnoHasta(Integer anoHasta) {
		this.anoHasta = anoHasta;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad MonthList.
	 * 
	 * @return el valor de la propiedad MonthList
	 */
	public List<SelectItem> getMonthList() {
		return monthList;
	}

	/**
	 * Metodo que define el valor de la propiedad monthList.
	 * 
	 * @param monthList el nuevo valor de monthList
	 */
	public void setMonthList(List<SelectItem> monthList) {
		this.monthList = monthList;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad ReportPath.
	 * 
	 * @return el valor de la propiedad ReportPath
	 */
	public HtmlInputHidden getReportPath() {
		return reportPath;
	}

	/**
	 * Metodo que define el valor de la propiedad reportPath.
	 * 
	 * @param reportPath el nuevo valor de reportPath
	 */
	public void setReportPath(HtmlInputHidden reportPath) {
		this.reportPath = reportPath;
	}

	protected String getStrReportPath() {
		String reportPath = (String) getReportPath().getValue();
		return reportPath;
	}

	public String getXlsFileName() {

		String[] folders = getStrReportPath().split("/");
		String reportFile = folders[folders.length - 1];

		String[] names = reportFile.split("\\.");
		String xlsName = names[0].concat(".xls");

		return xlsName;
	}

	public String getCsvFileName() {
		String[] folders = getStrReportPath().split("/");
		String reportFile = folders[folders.length - 1];

		String[] names = reportFile.split("\\.");
		String xlsName = names[0].concat(".csv");

		return xlsName;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad Fecha.
	 * 
	 * @return el valor de la propiedad Fecha
	 */
	public Date getFecha() {
		return fecha;
	}

	/**
	 * Metodo que define el valor de la propiedad fecha.
	 * 
	 * @param fecha el nuevo valor de fecha
	 */
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad Consecionario.
	 * 
	 * @return el valor de la propiedad Consecionario
	 */
	public String getConsecionario() {
		return consecionario;
	}

	/**
	 * Metodo que define el valor de la propiedad consecionario.
	 * 
	 * @param consecionario el nuevo valor de consecionario
	 */
	public void setConsecionario(String consecionario) {
		this.consecionario = consecionario;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad Concesionario.
	 * 
	 * @return el valor de la propiedad Concesionario
	 */
	public HtmlInputHidden getConcesionario() {
		return concesionario;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad Bodega.
	 * 
	 * @return el valor de la propiedad Bodega
	 */
	public String getBodega() {
		return bodega;
	}

	/**
	 * Metodo que define el valor de la propiedad bodega.
	 * 
	 * @param bodega el nuevo valor de bodega
	 */
	public void setBodega(String bodega) {
		this.bodega = bodega;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad Ubicacion.
	 * 
	 * @return el valor de la propiedad Ubicacion
	 */
	public String getUbicacion() {
		return ubicacion;
	}

	/**
	 * Metodo que define el valor de la propiedad ubicacion.
	 * 
	 * @param ubicacion el nuevo valor de ubicacion
	 */
	public void setUbicacion(String ubicacion) {
		this.ubicacion = toUpperCase(ubicacion);
	}

	/**
	 * To upper case.
	 * 
	 * @param valor el valor de valor
	 * 
	 * @return el valor de string
	 */
	private String toUpperCase(String valor) {
		String v = new String();
		if (valor != null) {
			v = valor.trim().toUpperCase();
		}
		return v;
	}

	/**
	 * @return the agencia
	 */
	public String getAgencia() {
		return agencia;
	}

	/**
	 * @param agencia the agencia to set
	 */
	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	/**
	 * @return the franquicia
	 */
	public String getFranquicia() {
		return franquicia;
	}

	/**
	 * @param franquicia the franquicia to set
	 */
	public void setFranquicia(String franquicia) {
		this.franquicia = franquicia;
	}

	/**
	 * @return the fechaDesde
	 */
	public Date getFechaDesde() {
		return fechaDesde;
	}

	/**
	 * @param fechaDesde the fechaDesde to set
	 */
	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	/**
	 * @return the fechaHasta
	 */
	public Date getFechaHasta() {
		return fechaHasta;
	}

	/**
	 * @param fechaHasta the fechaHasta to set
	 */
	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

	/**
	 * @param ano mes the ano mes to set
	 */
	public String getAno() {
		return ano;
	}

	public void setAno(String ano) {
		this.ano = ano;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		this.mes = mes;
	}

	/**
	 * @param concesionario the concesionario to set
	 */
	public void setConcesionario(HtmlInputHidden concesionario) {
		this.concesionario = concesionario;
	}

	protected String getStrConcesionario() {
		return (String) getConcesionario().getValue();
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

	public SelectItem[] popularMeses() {
		SelectItem[] mesesParaSeleccion = { new SelectItem(new Integer(1), "Enero"),
				new SelectItem(new Integer(2), "Febrero"), new SelectItem(new Integer(3), "Marzo"),
				new SelectItem(new Integer(4), "Abril"), new SelectItem(new Integer(5), "Mayo"),
				new SelectItem(new Integer(6), "Junio"), new SelectItem(new Integer(7), "Julio"),
				new SelectItem(new Integer(8), "Agosto"), new SelectItem(new Integer(9), "Septiembre"),
				new SelectItem(new Integer(10), "Octubre"), new SelectItem(new Integer(11), "Noviembre"),
				new SelectItem(new Integer(12), "Diciembre"), };
		return mesesParaSeleccion;
	}
}