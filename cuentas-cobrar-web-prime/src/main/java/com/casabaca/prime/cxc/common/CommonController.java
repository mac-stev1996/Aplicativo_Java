/*
 * CommonController.java
 *
 * Created on Sep 13, 2018, 10:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.casabaca.prime.cxc.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.context.RequestContext;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.UsuarioCentroBodega;
import com.casabaca.common.ejb.model.UsuarioCentroBodegaPK;
import com.casabaca.common.ejb.service.UsuarioCentroBodegaServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.common.util.SessionUtil;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.s3s.ejb.model.Compania;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.model.UsuarioCentro;
import com.casabaca.s3s.ejb.model.UsuarioCentroPK;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.CompaniaServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailServidoresServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioCentroServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UsuarioSisServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

public class CommonController {
	
	public Severity SEVERITY_ERROR = FacesMessage.SEVERITY_ERROR;
	public Severity SEVERITY_FATAL = FacesMessage.SEVERITY_FATAL;
	public Severity SEVERITY_INFO  = FacesMessage.SEVERITY_INFO;
	public Severity SEVERITY_WARN  = FacesMessage.SEVERITY_WARN;
	public static final String SPACE_IN_URL = "%20";
	
	private static final Logger logger = Logger.getLogger(CommonController.class);

	@EJB(lookup = NombreJNDI.COMPANIA_SERVICE_BEAN)
	private CompaniaServiceLocal companyService;
	
	@EJB(lookup = NombreJNDI.USUARIO_CENTRO_SERVICE)
	private UsuarioCentroServiceLocal usuarioCentroService; 
	
	@EJB(lookup = NombreJNDI.USUARIO_CENTRO_BODEGA_SERVICE_LOCAL)
	private UsuarioCentroBodegaServiceLocal usuarioCentroBodegaService;
	
	@EJB(lookup = NombreJNDI.JNDI_S3S+"SisMailServidoresServiceBean")
	private SisMailServidoresServiceLocal sisMailServidoresService;

	@ManagedProperty(value = "#{userDataManager}")
	private UserDataManager userDataManager;

	private HtmlInputHidden reportPath;	

	
	public CommonController() {
		if(this.userDataManager == null){
			this.userDataManager = new UserDataManager();
		}
		if (companyService == null) {
			try {
				companyService = (CompaniaServiceLocal) ServiceLocator
						.getService(com.casabaca.common.ejb.util.NombreJNDI.COMPANIA_SERVICE_BEAN);
				usuarioCentroService = (UsuarioCentroServiceLocal) ServiceLocator
						.getService(NombreJNDI.USUARIO_CENTRO_SERVICE);
				usuarioCentroBodegaService = (UsuarioCentroBodegaServiceLocal) ServiceLocator
						.getService(NombreJNDI.USUARIO_CENTRO_BODEGA_SERVICE_LOCAL);
			} catch (ServiceLocatorException e) {	}
		}
	}

	public Compania getCompania() {
		if (this.userDataManager.getCompania() != null) {
			return this.userDataManager.getCompania();
		}

		if (getRequestParameter(CommonConstants.REQUEST_PARAM_NOCIA) != null) {
			try {
				this.userDataManager.setCompania(companyService
						.findCompaniaByPK((String) getRequestParameter(CommonConstants.REQUEST_PARAM_NOCIA)));
				getUsuarioCentroConectado();
				getUsuarioCentroBodegaConectado();
				getLineaPorModulo();
			} catch (FindException e) {
				addErrorMessage("ERROR", "Al cargar datos de la Compania");
			}
			return this.userDataManager.getCompania();
		}
		HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
				.getSession();
		String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		return (Compania) SessionUtil.getPropiedadServletContext(session, userName,
				CommonConstants.COMPANY_SESSION_NAME);
	}

	public String getLineaPorModulo() {
		if (this.userDataManager.getLineaNegocio() != null) {
			return this.userDataManager.getLineaNegocio();
		}
		if (getRequestParameter(CommonConstants.REQUEST_PARAM_LINEA) != null) {

			this.userDataManager.setLineaNegocio(getRequestParameter(CommonConstants.REQUEST_PARAM_LINEA));
			return this.userDataManager.getLineaNegocio();
		}
		HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
				.getSession();
		String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		return (String) SessionUtil.getPropiedadServletContext(session, userName, CommonConstants.ACTIVE_MENU_LINEA);
	}

	protected void addErrorMessage(String summary, String detail) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail);
		FacesContext fc = FacesContext.getCurrentInstance();
		fc.addMessage(null, facesMsg);
	}

	protected void addInfoMessage(String summary, String detail) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
		FacesContext fc = FacesContext.getCurrentInstance();
		fc.addMessage(null, facesMsg);
	}

	protected void addWarnMessage(String summary, String detail) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail);
		FacesContext fc = FacesContext.getCurrentInstance();
		fc.addMessage(null, facesMsg);
	}
	public boolean getShowMessagesPanel() {
		return FacesContext.getCurrentInstance().getMessages().hasNext();
	}

	public UsuarioCentro getUsuarioCentroConectado() {
		if(this.userDataManager.getUsuarioCentro()!= null){
			if(this.userDataManager.getUsuarioCentroBodega() != null){
			getUsuarioCentroBodegaConectado();
			}
			return this.userDataManager.getUsuarioCentro();
		}
		if(getRequestParameter(CommonConstants.REQUEST_PARAM_CENTRO)!=null){
			try {
				this.userDataManager.setUsuarioCentro(usuarioCentroService
						.findByPK(new UsuarioCentroPK(getLoggedUsername(), getRequestParameter(CommonConstants.REQUEST_PARAM_CENTRO), getCompania().getNoCia())));
			} catch (FindException e) {
				addErrorMessage("ERROR", "Al cargar datos de la agencia conectada");
			}
			return this.userDataManager.getUsuarioCentro();
		}
		HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest()).getSession();
		String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		return (UsuarioCentro) SessionUtil.getPropiedadServletContext(session, userName,
				CommonConstants.USUARIO_CENTRO_SESSION_NAME);
	}

	public UsuarioSis getUsuario() {
		UsuarioSisServiceDelegate usuarioService = new UsuarioSisServiceDelegate();
		try {
			if (getLoggedUsername()!= null) {
				return usuarioService.getUsuarioSisByUsername(getLoggedUsername());
			}
		} catch (FindException e) {
			addErrorMessage(e.getSummary(), e.getDetail());
		}
		return null;
	}

	public String getLoggedUsername() {
		String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		if (remoteUser != null)
			return remoteUser.toUpperCase();
		return remoteUser;
	}

	protected String getRequestParameter(String name) {
		return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
	}
	
	protected ServletContext getServletContext() {
		return (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	}
	
	/**
	 * FacesMessage.SEVERITY_WARN | FacesMessage.SEVERITY_INFO | FacesMessage.SEVERITY_ERROR 
	 * @param header
	 * @param message
	 */
	public void setMessageGrowl(Severity severity, String header, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, header, message) );
    }
	
	public Locale getLocale(){
		Locale browserLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		return browserLocale;
	}
	
	/**
	 * @param idComponent
	 */
	public void updateComponentFromId(String idComponent){
		RequestContext.getCurrentInstance().update(idComponent);
	}
	
	protected void info(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }
     
	protected void warn(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", message));
    }
     
	protected void error(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", message));
    }
     
	protected void fatal(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fatal!", message));
    }
	
	/**
	 * Abre o cierra un dialog
	 * @param nombreDialog
	 * @param accion
	 */
	public void accionesDialog(String nombreDialog, boolean accion) {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('" + nombreDialog + "')." + (accion ? "show" : "hide") + "();");
	}

	/**
	 * Ejecuta funciones javascript que se encuentren en la pagina Por ejemplo
	 * ejecutarJavascript("openDuplicatedTab();");
	 * 
	 * @param texto
	 *            es el nombre de la funcion
	 */
	public void ejecutarJavascript(String texto) {
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute(texto);
	}
	
	public String getPathReal() {
		FacesContext fctx = FacesContext.getCurrentInstance();
		ServletContext context = (ServletContext) fctx.getExternalContext()
				.getContext();
		String realPath = context.getRealPath(File.separator + "reportes")
				+ File.separator;
		return realPath;
	}
	
	public List<Object[]> leerExcel(InputStream excelStream, boolean omitirCabecera) {

		List<Object[]> dataExcel = new ArrayList<>();

		try {
//			InputStream excelStream = null;
			try {
//	            excelStream = new FileInputStream(excelFile);
//	        	excelStream = new InputStream(stream);
				// High level representation of a workbook.
				// Representación del más alto nivel de la hoja excel.

//        	Workbook workbook = WorkbookFactory.create(stream);

				XSSFWorkbook hssfWorkbook = new XSSFWorkbook(excelStream);
				// We chose the sheet is passed as parameter.
				// Elegimos la hoja que se pasa por parámetro.
				XSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
				// An object that allows us to read a row of the excel sheet, and extract from
				// it the cell contents.
				// Objeto que nos permite leer un fila de la hoja excel, y de aquí extraer el
				// contenido de las celdas.
				XSSFRow hssfRow;
				// Initialize the object to read the value of the cell
				// Inicializo el objeto que leerá el valor de la celda
				XSSFCell cell;
				// I get the number of rows occupied on the sheet
				// Obtengo el número de filas ocupadas en la hoja
				int rows = hssfSheet.getLastRowNum();
				// I get the number of columns occupied on the sheet
				// Obtengo el número de columnas ocupadas en la hoja
				int cols = 0;
				// A string used to store the reading cell
				// Cadena que usamos para almacenar la lectura de la celda
				String cellValue;
				// For this example we'll loop through the rows getting the data we want
				// Para este ejemplo vamos a recorrer las filas obteniendo los datos que
				// queremos
				Object[] data = null;

				int iniciarDesde = omitirCabecera ? 1 : 0;

				for (int r = iniciarDesde; r <= rows; r++) {
					hssfRow = hssfSheet.getRow(r);
					if (hssfRow == null) {
						break;
					} else {
//	                    System.out.print("Row: " + r + " -> ");
						cols = hssfRow.getLastCellNum();
						data = new Object[cols];

						for (int c = 0; c < (cols); c++) {
							/*
							 * We have those cell types (tenemos estos tipos de celda): CELL_TYPE_BLANK,
							 * CELL_TYPE_NUMERIC, CELL_TYPE_BLANK, CELL_TYPE_FORMULA, CELL_TYPE_BOOLEAN,
							 * CELL_TYPE_ERROR
							 */

							cellValue = hssfRow.getCell(c) == null ? ""
									: (hssfRow.getCell(c).getCellType() == Cell.CELL_TYPE_STRING)
											? hssfRow.getCell(c).getStringCellValue()
											: (hssfRow.getCell(c).getCellType() == Cell.CELL_TYPE_NUMERIC)
													? "" + hssfRow.getCell(c).getNumericCellValue()
													: (hssfRow.getCell(c).getCellType() == Cell.CELL_TYPE_BOOLEAN)
															? "" + hssfRow.getCell(c).getBooleanCellValue()
															: (hssfRow.getCell(c)
																	.getCellType() == Cell.CELL_TYPE_BOOLEAN)
																			? "" + hssfRow.getCell(c).getDateCellValue()
																			: (hssfRow.getCell(c)
																					.getCellType() == Cell.CELL_TYPE_BLANK)
																							? "BLANK"
																							: (hssfRow.getCell(c)
																									.getCellType() == Cell.CELL_TYPE_FORMULA)
																											? "FORMULA"
																											: (hssfRow
																													.getCell(
																															c)
																													.getCellType() == Cell.CELL_TYPE_ERROR)
																															? "ERROR"
																															: "";
							data[c] = cellValue;
//	                        System.out.print("[Column " + c + ": " + cellValue + "] ");
						}
						dataExcel.add(data);
//	                    System.out.println();
					}
				}
			} catch (FileNotFoundException fileNotFoundException) {
				System.out.println("The file not exists (No se encontró el fichero): " + fileNotFoundException);
			} catch (IOException ex) {
				System.out.println("Error in file procesing (Error al procesar el fichero): " + ex);
			} finally {
				try {
					excelStream.close();
				} catch (IOException ex) {
					System.out.println(
							"Error in file processing after close it (Error al procesar el fichero después de cerrarlo): "
									+ ex);
				}
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return dataExcel;
	}
	
	protected String obtainException(Exception ex) {

		String error = null;
		Throwable cause = (Throwable) ex;
		do {
			error = cause.getMessage();
			if (ex.getMessage() != null) {
				error = error + " | " + ex.getMessage();
			}
			cause = cause.getCause();
		} while (cause != null);

		return error;
	}
	
	public UsuarioCentroBodega getUsuarioCentroBodegaConectado() {
		if(this.userDataManager.getUsuarioCentroBodega()!= null){
			return this.userDataManager.getUsuarioCentroBodega();
		}
		if(getRequestParameter(CommonConstants.REQUEST_PARAM_BODEGA)!=null){
			try {
				this.userDataManager.setUsuarioCentroBodega(usuarioCentroBodegaService
						.consultarUsuarioCentroBodegaxPK(new UsuarioCentroBodegaPK(getCompania().getNoCia(),
								getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(),
								getUsuario().getUsuario(), getRequestParameter(CommonConstants.REQUEST_PARAM_BODEGA))));
			} catch (FindException e) {
				addErrorMessage("ERROR", "Al cargar datos de la bodega conectada");
			}
			return this.userDataManager.getUsuarioCentroBodega();
		}
		HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
				.getSession();
		String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		return (UsuarioCentroBodega) SessionUtil.getPropiedadServletContext(session, userName,
				CommonConstants.USUARIO_CENTRO_BODEGA_SESSION_NAME);
	}

	public UserDataManager getUserDataManager() {
		return userDataManager;
	}

	public void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}	

	public String callJasperReport(String reportDir, Map<String, Object> parameters, String format) {
		StringBuffer str = new StringBuffer(CommonUtils.isValid(getCompania().getUrlServidorJasper()) ? getCompania().getUrlServidorJasper() : CommonConstants.URL_JASPERSERVER);
		str.append("&reportUnit=/reports" + reportDir);
		str.append("&standAlone=true");
		str.append("&p_no_cia=" + getCompania().getNoCia());
		str.append("&p_usuario=" + getUsuario().getUsuario());
		str.append("&p_centro=" + getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
		if(getUsuarioCentroBodegaConectado() != null)
			str.append("&p_bodega=" + getUsuarioCentroBodegaConectado().getUsuarioCentroBodegaPK().getBodega());
		if(getLineaPorModulo() != null)
			str.append("&p_linea=" + getLineaPorModulo());
		if (parameters != null && !parameters.isEmpty()) {
			parameters.forEach((key, value) -> {	
				if (value instanceof java.util.Date || value instanceof java.sql.Date
						|| value instanceof java.sql.Timestamp) {
					SimpleDateFormat sdf = new SimpleDateFormat(CommonConstants.jasperserverURLDateFormat);
					str.append("&" + key + "=" + sdf.format(value));
				} else
					str.append("&" + key + "=" + value);
			});
		}
		str.append("&output=" + format);
		System.out.println("URL "+str.toString());
		return str.toString();	
	}
	
	/**
	 * Recupera el reporte en byte[] pdf el reporte de jasper
	 * @param jasperPrint
	 * @return
	 */
	public byte[] recuperarPdfByte(JasperPrint jasperPrint) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
		} catch (JRException e) {
			logger.error("Error en recuperarPdfByte ", e);
		}
		return byteArrayOutputStream.toByteArray();
	}

	protected void redirect(String pageRedirect) throws IOException {
		FacesContext.getCurrentInstance().getExternalContext().redirect(pageRedirect);
	}

	/**
	 * Metodo que devuelve el id del modulo activo.
	 * 
	 * @return id del modulo activo.
	 */
	protected String getActiveModuleId() {
		HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
				.getSession();
		String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		return (String) SessionUtil.getPropiedadServletContext(session, userName, CommonConstants.ACTIVE_MODULE_ID);
	}
	/**
	 * Permite cargar el archivo excel, devolviendo una lista de objetos
	 * 
	 * @param stream
	 * @param hasHeaders
	 * @param numSheet
	 * @return
	 * @throws EncryptedDocumentException
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	protected List<Object[]> cargarArchivoExcel(InputStream stream, boolean hasHeaders, int numSheet)
			throws EncryptedDocumentException, IOException, InvalidFormatException {
		Workbook workbook = WorkbookFactory.create(stream);
		return readSheetExcel(workbook.getSheetAt(numSheet), hasHeaders);
	}
	
	protected List<Object[]> readSheetExcel(Sheet sheet, boolean hasHeaders) {

		List<Object[]> lista = new ArrayList<>();
		int numColumnas = sheet.getRow(0).getPhysicalNumberOfCells();
		sheet.rowIterator().forEachRemaining(row -> {
			if (hasHeaders && row.getRowNum() == 0) {
				return;
			}
			Object[] array = new Object[numColumnas];
			for (int i = 0; i < numColumnas; i++) {
				array[i] = row.getCell(i) == null ? null : examinarTipoColumna(row.getCell(i));
			}
			lista.add(array);
		});
		return lista;
	}
	
	/**
	 * Permite establecer obtener el valor de acuerdo al formato de cada celda
	 * 
	 * @param cell
	 * @return
	 */
	private Object examinarTipoColumna(Cell cell) {
		Object object = null;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				object = cell.getDateCellValue();

			} else {
				DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
				simbolos.setDecimalSeparator('.');
				DecimalFormat formatter = new DecimalFormat("###.##", simbolos);
				formatter.setGroupingUsed(false);
				object = formatter.format(cell.getNumericCellValue());
			}
			break;
		case Cell.CELL_TYPE_STRING:
			object = Objects.nonNull(cell.getStringCellValue()) ? cell.getStringCellValue().toUpperCase()
					: cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			object = Objects.nonNull(cell.getBooleanCellValue()) ? cell.getBooleanCellValue()
					: Boolean.FALSE;
			break;
		default:
			object = cell.getDateCellValue();
			break;
		}
		return object;
	}
	
	/**
	 * Permite obtener la causa de la excepcion
	 * 
	 * @param cause
	 * @return
	 */
	public String obtenerCausaException(Exception cause) {
		String error = "";
		if (null != cause) {
			while (null != cause) {
				error = error + " - " + cause.toString();
				if (null != cause.getCause()) {
					cause = (Exception) cause.getCause();
				} else {
					break;
				}
			}
			;
		} else {
			error = "error en obtenerCausaException";
		}

		if (null != error && error.contains("ORA")) {
			error = error.substring(error.indexOf("ORA"));
		}
		return error;
	}
	
	/**
	 * Permite descargar el archivo
	 * 
	 */
	public void descargarArchivo(String path) {
		try {
			FileUpload fileUpload = new FileUpload();
			fileUpload.seeFile(FacesContext.getCurrentInstance(), path);
		} catch (IOException e) {
			logger.error(e.getCause());
		}
	}
	
	protected void exportXls(JasperPrint jasperPrint,
			HttpServletResponse response) throws JRException, IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		//JRXlsExporter exporter = new JRXlsExporter();
		 JRXlsxExporter exporter = new JRXlsxExporter();
		//exporter
		//		.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, true);
		exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM,
				byteArrayOutputStream);
		exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(
				JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND,
				CommonConstants.FALSE_VALUE);
		exporter
				.setParameter(
						JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
						CommonConstants.TRUE_VALUE);
		exporter.setParameter(
				JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
				CommonConstants.TRUE_VALUE);
		//exporter.setParameter(JExcelApiExporterParameter.IS_IGNORE_CELL_BORDER,
		//		CommonConstants.TRUE_VALUE);
		//exporter.setParameter(JExcelApiExporterParameter.IS_COLLAPSE_ROW_SPAN,
		//		CommonConstants.TRUE_VALUE);

		exporter.exportReport();
		byte[] bytes = byteArrayOutputStream.toByteArray();

		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append(getXlsXFileName());
		header.append("\"");
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setContentLength(bytes.length);
		// ServletOutputStream outputStream = response.getOutputStream();
		response.getOutputStream().write(bytes, 0, bytes.length);
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	public String getXlsXFileName() {
		String[] folders = getStrReportPath().split("/");
		String reportFile = folders[folders.length - 1];

		String[] names = reportFile.split("\\.");
		String xlsName = names[0].concat(".xlsx");

		return xlsName;
	}
	
	protected String getStrReportPath() {
		String reportPath = (String) getReportPath().getValue();
		return reportPath;
	}


	public SisMailServidores getSisMailServidores(String noCia) throws GeneralException {
		return sisMailServidoresService.getbyNocia(noCia);
	}

	public HtmlInputHidden getReportPath() {
		return reportPath;
	}

	public void setReportPath(HtmlInputHidden reportPath) {
		this.reportPath = reportPath;
	}
	
	protected boolean checkTrimEmpty(String str) {
	    for(int i = 0; i < str.length(); i++) {
	        if(!Character.isWhitespace(str.charAt(i))) {
	            return false;
	        }
	    }
	    return true;
	}
	
	protected String remplazarCaracteresEspeciales(String mensaje) {
		String nuevoMensaje = mensaje.replaceAll("á", "&aacute;").replaceAll("é", "&eacute;").replaceAll("í", "&iacute;")
				.replaceAll("ó", "&oacute;").replaceAll("ú", "&uacute;").replaceAll("Á", "A").replaceAll("É", "E")
				.replaceAll("Í", "I").replaceAll("Ó", "O").replaceAll("Ú", "U").replaceAll("¿", "&iquest;")
				.replaceAll("¡", "&iexcl;").replaceAll("ñ", "&ntilde;").replaceAll("Ñ", "&Ntilde;");
		return nuevoMensaje;
	}
	
}