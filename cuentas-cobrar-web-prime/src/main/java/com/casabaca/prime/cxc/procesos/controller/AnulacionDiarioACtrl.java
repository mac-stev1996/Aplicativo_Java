package com.casabaca.prime.cxc.procesos.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.caja.ejb.modelo.Arcjad;
import com.casabaca.caja.ejb.service.ArcjadServiceLocal;
import com.casabaca.common.ejb.model.Arccda;
import com.casabaca.common.ejb.model.ArccdaPK;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.service.ArccdaServiceLocal;
import com.casabaca.common.ejb.service.ArccrdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;
import com.casabaca.s3s.ejb.model.UsuarioCentroPK;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ManagedBean
@ViewScoped
public class AnulacionDiarioACtrl extends CommonController {
	
	private static final Logger LOGGER = Logger.getLogger(AnulacionDiarioACtrl.class);
	
	private static final String PATH_REPORTE_DOC_ANULACION = "/reportes/cxcrepDiariosA.jasper";
	private static final String JASPER_PARAM_NO_CIA = "noCia";
	private static final String JASPER_PARAM_NO_DOCU = "noDocu";
	private static final String JASPER_PARAM_TOTAL_INTERES = "totalInteres";
	private static final String JASPER_PARAM_TOTAL_DESCUENTO = "totalDescuento";
	
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;
	@EJB(lookup = NombreJNDI.ARCJAD_SERVICE)
	private ArcjadServiceLocal arcjadService;
	@EJB(lookup = NombreJNDI.ARCCDA_SERVICE)
	private ArccdaServiceLocal arccdaService;
	@EJB(lookup = NombreJNDI.ARCCRD_SERVICE)
	private ArccrdServiceLocal arccrdService;
	
	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	private String noCia;
	private String centro;
	private String usuario;
	
	private List<Arccda> diariosAParaAnular;
	private Cliente cliente;
	private String docReciboPago;
	private Arccda diarioASeleccionado;
	
	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		
		Arcjad arcjad = arcjadService.findByNocia(noCia);
		if (arcjad == null) {
			error("ERROR: No se ha definido la asignacion de documentos de pago.");
			return;
		} else {
			docReciboPago = arcjad.getrPago();
		}
		
		UsuarioCentroPK usuarioCentroPK = getUsuarioCentroConectado().getUsuarioCentroPK();
		this.centro = usuarioCentroPK.getCentro();
		this.usuario = usuarioCentroPK.getUsuario();
		this.cliente = new Cliente(new ClientePK(this.noCia));
		this.diarioASeleccionado = new Arccda();
		this.diarioASeleccionado.setArccdaPk(new ArccdaPK());
	}
	
	
	
	
	public void seleccionarClienteListener () {
		this.limpiar();
		
		Cliente clienteSeleccionado = this.dialogClientesController.getCliente();
		if (clienteSeleccionado == null || clienteSeleccionado.getClientePK() == null|| clienteSeleccionado.getClientePK().getNoCliente() == null) {
			error("Debe seleccionar un cliente");
			return;
		}
		try {
			buscarCliente(clienteSeleccionado.getClientePK());			
		} catch (IllegalArgumentException e) {
			error(e.getMessage());
			RequestContext.getCurrentInstance().execute("PF('dialogClientesWV').hide();");
			return;
		}
		
		buscarDiariosAParaAnulacion(clienteSeleccionado.getClientePK(), this.centro);
		RequestContext.getCurrentInstance().update(Arrays.asList("frmPrincipal:pgDatosDiario", "frmDiarios"));
		RequestContext.getCurrentInstance().execute("PF('dialogClientesWV').hide();PF('wvDiagDiariosA').show();");
	}
	
	private void buscarCliente (ClientePK clientePK) throws IllegalArgumentException {
		List<Cliente>clientes = clienteService.buscarClienteFechaCierre(clientePK, null);
		
		if (clientes.isEmpty()) {
			throw new IllegalArgumentException("ERROR: La cedula del cliente no es valido o no esta autorizado en el centro.");
		} else if (clientes.size() > 1) {
			throw new IllegalArgumentException("ERROR: La búsqueda devolvió demasiados resultados. Borre la cédula o el no cliente.");
		} else {
			cliente = clientes.get(0);
		}
	}
	
	private void buscarDiariosAParaAnulacion (ClientePK clientePK, String centro) {
		this.diariosAParaAnular = arccdaService.diarioADelDiaParaAnular(clientePK, centro);
	}
	
	private void limpiar() {
		this.cliente = new Cliente(new ClientePK(this.noCia));
		this.diariosAParaAnular = new ArrayList<Arccda>();
		this.diarioASeleccionado = new Arccda();
	}
	
	public void anularDiarioA () {
		if (this.diarioASeleccionado.getArccdaPk() == null) {
			error("Debe seleccionar un Diario A para anular");
			return;
		}
		try {
			String resultadoAnulacion = arccdaService.anularDiarioA(diarioASeleccionado);
			info(resultadoAnulacion);
		} catch (UpdateException e) {
			e.printStackTrace();
			error(e.getDetail());
		}
	}
	
	public void generarReportePdf () {
		
		BigDecimal [] totalInteresYDescuento = arccrdService.totalInteresYDescuento(diarioASeleccionado.getArccdaPk().getNoCia(), diarioASeleccionado.getArccdaPk().getNoDocu());
		
		Connection connection = prepararConexion();
		Map<String, Object> jasperParams = buildJasperParams(totalInteresYDescuento[0], totalInteresYDescuento[1]);
		String ctxPath = getServletContext().getRealPath("/");
		try {
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			jasperParams.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + PATH_REPORTE_DOC_ANULACION,
					jasperParams, connection);
			virtualizer.cleanup();
			sendPdfResponse(jasperPrint);
		} catch (JRException e) {
			error("Problemas al intentar llenar el reporte.");
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
			}
		}
	}
	
	public void sendPdfResponse(JasperPrint jasperPrint) {
		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append(buildNombreReporte());
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
			LOGGER.error("No se pudo obtener el response PDF para el reporte: "+PATH_REPORTE_DOC_ANULACION);
		} catch (JRException e) {
			LOGGER.error(e.getMessage());
			LOGGER.error("No se pudo contruir el PDF para el reporte: "+PATH_REPORTE_DOC_ANULACION);
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
	
	public Map<String, Object> buildJasperParams (BigDecimal totalInteres, BigDecimal totalDescuento) {
		Map<String, Object> jasperParams = new HashMap<>();
		jasperParams.put(JRParameter.REPORT_VIRTUALIZER, buildVirtualizer());
		jasperParams.put(JASPER_PARAM_NO_CIA, diarioASeleccionado.getArccdaPk().getNoCia());
		jasperParams.put(JASPER_PARAM_NO_DOCU, diarioASeleccionado.getArccdaPk().getNoDocu());
		jasperParams.put(JASPER_PARAM_TOTAL_INTERES, totalInteres);
		jasperParams.put(JASPER_PARAM_TOTAL_DESCUENTO, totalDescuento);
		return jasperParams;
	}
	
	
	public JRFileVirtualizer buildVirtualizer () {
		String path = System.getProperty("file.separator") 
				+ FacesContext.getCurrentInstance().getExternalContext().getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
				+ System.getProperty("file.separator");
		
		File[] drives = File.listRoots();
		for (File fileDrives : drives) {
			if (fileDrives.getPath().length() > 1 && fileDrives.getPath().substring(0,1).equals("C")) {
				path = fileDrives.getPath() + path;
			}
		}
		return new JRFileVirtualizer(10, path);
	}
	

	public String buildNombreReporte () {
		StringBuilder nombreReporte = new StringBuilder();
		nombreReporte
		.append("diarioAAnulado-")
		.append(diarioASeleccionado.getArccdaPk().getNoDocu());
		return nombreReporte.toString();
	}
	
	/**
	 * Permite cargar el dialogo de clientes
	 */
	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);
	}
	
	// GETTERS Y SETTERS
	public String getCentro() {
		return centro;
	}


	public void setCentro(String centro) {
		this.centro = centro;
	}


	public Cliente getCliente() {
		return cliente;
	}


	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}


	public List<Arccda> getDiariosAParaAnular() {
		return diariosAParaAnular;
	}


	public void setDiariosAParaAnular(List<Arccda> diariosAParaAnular) {
		this.diariosAParaAnular = diariosAParaAnular;
	}


	public Arccda getDiarioASeleccionado() {
		return diarioASeleccionado;
	}


	public void setDiarioASeleccionado(Arccda diarioASeleccionado) {
		this.diarioASeleccionado = diarioASeleccionado;
	}


	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}


	public void setDialogClientesController(DialogClientesController dialogClientesController) {
		this.dialogClientesController = dialogClientesController;
	}


	
}
