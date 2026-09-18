/*
 * CommonController.java
 *
 * Created on May 10, 2007, 10:07 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.casabaca.compras.web.common.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.common.util.SessionUtil;
import com.casabaca.exception.FindException;
import com.casabaca.exception.RequeridoException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.s3s.ejb.model.Compania;
import com.casabaca.s3s.ejb.model.UsuarioCentro;
import com.casabaca.s3s.ejb.model.UsuarioCentroPK;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.CompaniaServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioCentroServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UsuarioSisServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;

/**
 * 
 * @author Administrador
 */
public class CommonController implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 658971635663542620L;
	
	private HtmlInputHidden reportPath;
	private UsuarioCentro usuarioCentro;
	@SuppressWarnings("unused")
	private UsuarioCentro usuarioCentroConectado;
	UsuarioSisServiceDelegate usuarioServiceDelegate;

	
	@EJB(lookup = NombreJNDI.COMPANIA_SERVICE_BEAN)
	private CompaniaServiceLocal companyService;
	
	@EJB(lookup = NombreJNDI.USUARIO_CENTRO_SERVICE)
	private UsuarioCentroServiceLocal usuarioCentroService; 
	
	@ManagedProperty(value = "#{userDataManager}")
	private UserDataManager userDataManager;
	
	/** Creates a new instance of CommonController */
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
			
			} catch (FindException e) {
				addErrorMessage("ERROR","Al cargar datos de la Compania");
			}
			return this.userDataManager.getCompania();
		}
		HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
				.getSession();
		String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		return (Compania) SessionUtil.getPropiedadServletContext(session, userName,
				CommonConstants.COMPANY_SESSION_NAME);
	}

	protected void addErrorMessage(String summary, String detail) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail);
		FacesContext fc = FacesContext.getCurrentInstance();
		fc.addMessage(null, facesMsg);
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

	public String getXlsFileName(String sourceReportPath) {
		String[] folders = sourceReportPath.split("/");
		String reportFile = folders[folders.length - 1];

		String[] names = reportFile.split("\\.");
		String xlsName = names[0].concat(".xls");

		return xlsName;
	}

	protected void addInfoMessage(String summary, String detail) {
		FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
		FacesContext fc = FacesContext.getCurrentInstance();
		fc.addMessage(null, facesMsg);
	}

	public boolean getShowMessagesPanel() {
		return FacesContext.getCurrentInstance().getMessages().hasNext();
	}

	public String getLoggedUsername() {
		String remoteUser = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		if (remoteUser != null)
			return remoteUser.toUpperCase();
		return remoteUser;
	}

	protected ServletContext getServletContext() {
		return (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
	}

	public HtmlInputHidden getReportPath() {
		return reportPath;
	}

	public void setReportPath(HtmlInputHidden reportPath) {
		this.reportPath = reportPath;
	}

	public int getBatchSize() {
		return CommonConstants.BATCH_SIZE;
	}

	public UsuarioCentro getUsuarioCentro() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		HttpSession session = request.getSession();
		session.getServletContext().setAttribute("usuarioCentro", this.usuarioCentro);
		return usuarioCentro;
	}

	public UsuarioCentro getUsuarioCentroConectado() {
		if(this.userDataManager.getUsuarioCentro()!= null){
			return this.userDataManager.getUsuarioCentro();
		}
		if(getRequestParameter(CommonConstants.REQUEST_PARAM_CENTRO)!=null){
			try {
				this.userDataManager.setUsuarioCentro(usuarioCentroService
						.findByPK(new UsuarioCentroPK(getLoggedUsername(), getRequestParameter(CommonConstants.REQUEST_PARAM_CENTRO), getCompania().getNoCia())));
			} catch (FindException e) {
				addErrorMessage("ERROR"," Al cargar datos de la agencia conectada");
			}
			return this.userDataManager.getUsuarioCentro();
		}
		HttpSession session = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest()).getSession();
		String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		return (UsuarioCentro) SessionUtil.getPropiedadServletContext(session, userName,
				CommonConstants.USUARIO_CENTRO_SESSION_NAME);
	}

	public void setUsuarioCentro(UsuarioCentro usuarioCentro) {
		this.usuarioCentro = usuarioCentro;
	}

	public void setUsuarioCentroConectado(UsuarioCentro usuarioCentroConectado) {
		this.usuarioCentroConectado = usuarioCentroConectado;
	}

	public UsuarioSis getUsuario() {
		UsuarioSisServiceDelegate usuarioService = new UsuarioSisServiceDelegate();
		try {
			return usuarioService.getUsuarioSisByUsername(getLoggedUsername());
		} catch (FindException e) {
			addErrorMessage(e.getSummary(), e.getDetail());
		}
		return null;
	}
	
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
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_IGNORE_CELL_BORDER, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_COLLAPSE_ROW_SPAN, CommonConstants.TRUE_VALUE);
		exporter.exportReport();
		byte[] bytes = byteArrayOutputStream.toByteArray();

		StringBuffer header = new StringBuffer();
		header.append("inline; filename=\"");
		header.append(getXlsFileName());
		header.append("\"");
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(bytes.length);
		ServletOutputStream outputStream = response.getOutputStream();
		outputStream.write(bytes, 0, bytes.length);
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * Metodo que devuelve el id del modulo activo.
	 * 
	 * @return id del modulo activo.
	 */
	public String getActiveModuleId() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		ServletContext servletContext = session.getServletContext().getContext(CommonConstants.PRINCIPAL_CONTEXT_PATH);
		Object object = servletContext.getAttribute(CommonConstants.ACTIVE_MODULE_ID + session.getId());
		return (String) object;
	}

	/**
	 * Metodo que devuelve el nombre del modulo activo.
	 * 
	 * @return nombre del modulo activo
	 */
	public String getActiveModuleName() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		ServletContext servletContext = session.getServletContext().getContext(CommonConstants.PRINCIPAL_CONTEXT_PATH);
		Object object = servletContext.getAttribute(CommonConstants.ACTIVE_MODULE_NAME + session.getId());
		return (String) object;
	}

	/**
	 * Metodo que devuelve el nombre del menu activo.
	 * 
	 * @return nombre del modulo activo
	 */
	public String getActiveMenuId() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		ServletContext servletContext = session.getServletContext().getContext(CommonConstants.PRINCIPAL_CONTEXT_PATH);
		Object object = servletContext.getAttribute(CommonConstants.ACTIVE_MENU_ID + session.getId());
		return (String) object;
	}

	protected String getPathReal() {
		FacesContext fctx = FacesContext.getCurrentInstance();
		ServletContext context = (ServletContext) fctx.getExternalContext().getContext();
		String realPath = context.getRealPath(File.separator + "reportes") + File.separator;
		return realPath;
	}

	protected String getPathImagen() {
		FacesContext fctx = FacesContext.getCurrentInstance();
		ServletContext context = (ServletContext) fctx.getExternalContext().getContext();
		String realPath = context.getRealPath(File.separator + "common") + File.separator + "imgs" + File.separator
				+ "imagenes" + File.separator;
		return realPath;

		//
	}

	/**
	 * Metodo que devuelve el nombre del menu activo.
	 * 
	 * @return nombre del modulo activo
	 */
	public String getActiveLink() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		ServletContext servletContext = session.getServletContext().getContext(CommonConstants.PRINCIPAL_CONTEXT_PATH);
		Object object = servletContext.getAttribute(CommonConstants.ACTIVE_LINK + session.getId());
		return (String) object;
	}

	public void validarRequeridos(Map<String, Object> map) throws RequeridoException {
		String mensaje = "";
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() == "" || entry.getValue() == null) {
				mensaje += " " + entry.getKey() + ",";
			}
		}
		if (!mensaje.trim().equals("")) {
			mensaje = mensaje.substring(0, mensaje.length() - 1);
			throw new RequeridoException(mensaje);
		}
	}

	protected String getRequestParameter(String name) {
		return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
	}

	public String getActiveModuleOption() {
		HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		ServletContext servletContext = session.getServletContext().getContext(CommonConstants.PRINCIPAL_CONTEXT_PATH);
		Object object = servletContext.getAttribute(CommonConstants.ACTIVE_MODULE_OPTION + session.getId());
		return (String) object;
	}

	public UserDataManager getUserDataManager() {
		return userDataManager;
	}

	public void setUserDataManager(UserDataManager userDataManager) {
		this.userDataManager = userDataManager;
	}
	
	
}