package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.service.CompaniaServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean(name = "cxcAnticiposReportController")
public class CxcAnticiposReportController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcAnticiposReportController.class);

	private ClienteServiceLocal clienteService;
	private CompaniaServiceLocal companyService = null;
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;
	private LineaNegocioServicioLocal lineaNegocioService;

	private String cedula;
	private String nombres;
	private Cliente cliente;
	private String lineaNegocio;
	private List<Cliente> listarClientes;
	private List<LineaNegocio> listarLineasNegocio;
	private List<SelectItem> lineasNegocioSelectItems;

	public CxcAnticiposReportController() {
		try {
			clienteService = (ClienteServiceLocal) ServiceLocator.getService(NombreJNDI.CLIENTE_SERVICE);
			nativeDmlDatabaseService = (NativeDmlDatabaseServiceLocal) ServiceLocator
					.getService(NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN);
			companyService = (CompaniaServiceLocal) ServiceLocator.getService(NombreJNDI.COMPANIA_SERVICE_BEAN);
			lineaNegocioService = (LineaNegocioServicioLocal) ServiceLocator
					.getService(NombreJNDI.LINEA_NEGOCIO_SERVICIO);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}

	@PostConstruct
	public void init() {
		this.cedula = null;
		this.nombres = null;
		this.listarClientes = new ArrayList<>();
		this.listarLineasNegocio = new ArrayList<>();
		this.lineasNegocioSelectItems = new ArrayList<>();
		this.cliente = new Cliente();
		cargarCientes();
		cargarLineasNrgocio();
	}

	@SuppressWarnings("deprecation")
	public String report() {
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		Map<String, Object> parameters = new HashMap<String, Object>();
		try {

			String ctxPath = getServletContext().getRealPath("/");
			setParameters(parameters);
			connection = utilServiceDelegate.getDataSource().getConnection();
			
			// VIRTUALIZAR
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

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
	}

	@SuppressWarnings("unchecked")
	private void setParameters(Map parameters) {

		parameters.put("PNO_CIA", getCompania().getNoCia());
		parameters.put("PCEDULA", this.cliente.getCedula());
		parameters.put("PLINEA", getLineaNegocio());
		parameters.put("SUBREPORT_DIR", getPathReal());
	}

	private String getStrReportPagare() {
		String reportPath = null;
		reportPath = "cxcAnticipos.jasper";

		return reportPath;
	}

	public void cargarCientes() {
		try {
			listarClientes = clienteService.buscarClientesPorParametros(0, 25, getCompania().getNoCia(),
					this.getCedula(), this.getNombres());
		} catch (Exception e) {
			listarClientes = new ArrayList<>();
			logger.error(e);
			addInfoMessage("Info. ", " No se encontro al cliente ingresado");

		}
	}

	public void cargarLineasNrgocio() {
		try {
			listarLineasNegocio = lineaNegocioService.buscarLineaNegocioLista(getCompania().getNoCia());
			for (LineaNegocio item : listarLineasNegocio) {
				lineasNegocioSelectItems
						.add(new SelectItem(item.getLineaNegocioPK().getNoLinea().toString(), item.getDescripcion()));
			}

		} catch (Exception e) {
			listarLineasNegocio = new ArrayList<>();
			logger.error(e);
			addInfoMessage("Info. ", " No se encontro al cliente ingresado");

		}
	}

	public void seleccionarFilaCliente(SelectEvent event) {
		this.cliente = new Cliente();
		this.cliente = (Cliente) event.getObject();
	}

	public void limpiarPanelClientes() {
		this.setCedula(null);
		this.setNombres(null);
		this.listarClientes = new ArrayList<>();
	}

	public ClienteServiceLocal getClienteService() {
		return clienteService;
	}

	public void setClienteService(ClienteServiceLocal clienteService) {
		this.clienteService = clienteService;
	}

	public CompaniaServiceLocal getCompanyService() {
		return companyService;
	}

	public void setCompanyService(CompaniaServiceLocal companyService) {
		this.companyService = companyService;
	}

	public NativeDmlDatabaseServiceLocal getNativeDmlDatabaseService() {
		return nativeDmlDatabaseService;
	}

	public void setNativeDmlDatabaseService(NativeDmlDatabaseServiceLocal nativeDmlDatabaseService) {
		this.nativeDmlDatabaseService = nativeDmlDatabaseService;
	}

	public LineaNegocioServicioLocal getLineaNegocioService() {
		return lineaNegocioService;
	}

	public void setLineaNegocioService(LineaNegocioServicioLocal lineaNegocioService) {
		this.lineaNegocioService = lineaNegocioService;
	}

	public List<Cliente> getListarClientes() {
		return listarClientes;
	}

	public void setListarClientes(List<Cliente> listarClientes) {
		this.listarClientes = listarClientes;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<LineaNegocio> getListarLineasNegocio() {
		return listarLineasNegocio;
	}

	public void setListarLineasNegocio(List<LineaNegocio> listarLineasNegocio) {
		this.listarLineasNegocio = listarLineasNegocio;
	}

	public List<SelectItem> getLineasNegocioSelectItems() {
		return lineasNegocioSelectItems;
	}

	public void setLineasNegocioSelectItems(List<SelectItem> lineasNegocioSelectItems) {
		this.lineasNegocioSelectItems = lineasNegocioSelectItems;
	}

	public String getLineaNegocio() {
		return lineaNegocio;
	}

	public void setLineaNegocio(String lineaNegocio) {
		this.lineaNegocio = lineaNegocio;
	}

}
