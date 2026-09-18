/**
 * 
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.administration.ejb.dto.UsuarioDto;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.EjecutaComandoUtils;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.NumericUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.model.FacturasVehiculo;
import com.casabaca.common.ejb.model.FacturasVehiculoPK;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccckfServiceLocal;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CotizacionRepuestoServicesLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.HistoricoDocEntradaDto;
import com.casabaca.cxc.ejb.dto.HistoricoDocumentoDto;
import com.casabaca.cxc.ejb.modelo.Cotizacion;
import com.casabaca.cxc.ejb.servicio.CotizacionServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.servicio.HistoricoDocumentosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogUsuariosController;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * @author Roberto Guizado
 */
@ManagedBean
@ViewScoped
public class ConsultaHistDocumentosController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3621683819519757653L;
	private static final Logger LOG = Logger.getLogger(ConsultaHistDocumentosController.class);
	private static final String REPORTE_HIST_DOC = "/reportes/historicoDocumentos.jasper";
	private static final String NOMBRE_REPORTE_HIST_DOC = "historicoDocumentos.pdf";

	private static final String REPORTE_CERTIFICADO_RESERV = "/reportes/certReservaDom.jasper";
	private static final String NOMBRE_REPORTE_CERTIFICADO_RESERV = "certReservaDom.pdf";

	private static final String REPORTE_CERTIFICADO = "/reportes/cxcCertificado.jasper";
	private static final String NOMBRE_REPORTE_CERTIFICADO = "cxcCertificado.pdf";

	private static final String REPORTE_ANT = "/reportes/cxcJefatura.jasper";
	private static final String NOMBRE_REPORTE_ANT = "cxcAnt.pdf";

	private static final String REPORTE_RM = "/reportes/cxcRegistroMercantil.jasper";
	private static final String NOMBRE_REPORTE_RM = "cxcRegistroMercantil.pdf";

	private static final String PDF_PATH_FC = "gnvoiceCasabaca/descargarDocumento/obtenerFacturaPDF/";
	
	private static final String VALOR_NULO = null;
	private static final UsuarioDto USUARIO_NULO = null;
	
	private static final String VAL_MODULO_COBRANZAS="CXC";

	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	@ManagedProperty("#{dialogUsuariosController}")
	private DialogUsuariosController dialogUsuariosController;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeService;

	@EJB(lookup = NombreJNDI.JNDI_CXC + "HistoricoDocumentosServiceBean")
	private HistoricoDocumentosServiceLocal historicoDocumentosService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;

	@EJB(lookup = NombreJNDI.COTIZACION_SERVICE)
	private CotizacionServiceLocal cotizacionService;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	
	@EJB(lookup = NombreJNDI.ARCCCKF_SERVICE)
	private ArccckfServiceLocal arccckfService;
	
	@EJB(lookup = NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN)
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;
	
	@EJB(lookup = NombreJNDI.COTIZACION_REPUESTOS_SERVICES_BEAN)
	private CotizacionRepuestoServicesLocal cotizacionRepuestosService;

	private String noCia;

	private Cliente cliente;
	private List<LineaNegocio> lineasNegocio;
	private String noLineaNeg;
	private List<Object[]> tiposDocumento;
	private String tipoDoc;
	private String tipoAnulado;
	private Integer tipoDocumento;
	private Date fechaInicio;
	private Date fechaFin;
	private List<HistoricoDocumentoDto> documentosHistoricos;
	private HistoricoDocumentoDto documentoSeleccionado;
	private Double totalDebitoLocal;
	private Double totalCreditoLocal;
	private Double totalDebitoInt;
	private Double totalCreditoInt;
	private Cotizacion cotizacion;
	private String noInscripcion;
	private String tomo;
	private Integer anio;
	private Date fecha;
	private boolean aplicaDesbloqueo;
	private String prenda;
	private String seguro;
	private String observacionDesbloqueo;
	private UploadedFile file;
	private UsuarioDto usuarioDto;
	private String zona;
	private String canal;
	private String tipoFinanciamiento;
	private String lineaNegocio;
	private String rangoAntiguedad;
	private String origen;
	private String agencia;
	private String smsCheqFuturos;
	private String smsCheqProtestados;
	private String comprobante;
	private String noFactu;
	private BigDecimal noCoti;
	private String placaVehPagosMemo;
	//Req39875 BloqueoAnticipoPorCliente
	private boolean aplicaBloqueo;
	private String observacionBloqueo;
	private Boolean incluyeAlistamiento;

	private Double valorCoutaMantPrepagados;
	private boolean aplicaInteresMantPrepagados;

	public ConsultaHistDocumentosController() {
		this.noCia = getCompania().getNoCia();
		this.aplicaDesbloqueo = Boolean.FALSE;
	}

	@PostConstruct
	public void init() {
		limpiarDatos();
		try {
			lineasNegocio = lineaNegocioService.buscarLineaNegocioLista(this.noCia);
		} catch (FindException e) {
			warn(e.getDetail() + ". Por favor recargue la página");
		}
		placaVehPagosMemo = getRequestParameter("placaVehiculo") != null ? getRequestParameter("placaVehiculo") : null;
		origen = getRequestParameter("origen") != null ? getRequestParameter("origen") : null;
		
		if (origen != null) {
			zona = getRequestParameter("zona") != null ? getRequestParameter("zona") : null;
			canal = getRequestParameter("canal") != null ? getRequestParameter("canal") : null;
			tipoFinanciamiento = getRequestParameter("tipoFinanciamiento") != null
					? getRequestParameter("tipoFinanciamiento")
					: null;
			lineaNegocio = getRequestParameter("lineaNegocio") != null ? getRequestParameter("lineaNegocio") : null;
			rangoAntiguedad = getRequestParameter("rangoAntiguedad") != null ? getRequestParameter("rangoAntiguedad")
					: null;
			agencia = getRequestParameter("agenciaSelected") != null ? getRequestParameter("agenciaSelected") : null;

		}
		tiposDocumento = nativeService.consultarTiposDocumentos(this.noCia);
		Long noCliente = getRequestParameter("noCliente") != null ? Long.valueOf(getRequestParameter("noCliente"))
				: null;
		if (noCliente != null) {
			try {
				cliente = clienteService.findByPk(new ClientePK(this.noCia, "01", noCliente));
				noLineaNeg = getRequestParameter("lineaNeg") != null ? getRequestParameter("lineaNeg").replace('T', '%')
						: null;
				tipoDoc = getRequestParameter("tipoDoc") != null ? getRequestParameter("tipoDoc").replace('T', '%')
						: null;
				tipoAnulado = getRequestParameter("anulado") != null ? getRequestParameter("anulado").replace('T', '%')
						: null;
				tipoDocumento = getRequestParameter("documento") != null
						? Integer.parseInt(getRequestParameter("documento"))
						: null;
				fechaInicio = getRequestParameter("fechaIni") != null && !"".equals(getRequestParameter("fechaIni"))
						? FechaUtils.convertirStrFecha(getRequestParameter("fechaIni"), FechaUtils.patronDiaMesAnio)
						: null;
				fechaFin = getRequestParameter("fechaFin") != null && !"".equals(getRequestParameter("fechaFin"))
						? FechaUtils.convertirStrFecha(getRequestParameter("fechaFin"), FechaUtils.patronDiaMesAnio)
						: null;
				ejecutarJavascript("document.getElementById(\"frmBotones:btnConsultar\").click()");
			} catch (FindException e) {
				LOG.error(e.getMessage(), e);
				error("Se presento un problema al cargar los datos del cliente");
			}
		}
		aplicaDesbloqueo = "S".equals(getRequestParameter("APLDESB"));
	}

	public void limpiarDatos() {
		totalDebitoLocal = 0D;
		totalCreditoLocal = 0D;
		totalDebitoInt = 0D;
		totalCreditoInt = 0D;
		cliente = new Cliente();
		documentosHistoricos = new ArrayList<>();
		documentoSeleccionado = null;
		fechaInicio = FechaUtils.convertirStrFecha("01/01/2000", FechaUtils.patronDiaMesAnio);
		fechaFin = new Date();
		noLineaNeg = "%";
		tipoDoc = "%";
		tipoAnulado = "%";
		tipoDocumento = 2;
		smsCheqFuturos = null;
		smsCheqProtestados = null;
	}

	public void consultar() {	
		HistoricoDocEntradaDto entrada = new HistoricoDocEntradaDto();
		entrada.setNoCia(this.noCia);
		entrada.setNoCliente(cliente.getClientePK().getNoCliente());
		entrada.setFechaInicio(fechaInicio);
		entrada.setFechaFin(fechaFin);
		entrada.setLineaNegocio(noLineaNeg);
		entrada.setTipoDoc(tipoDoc);
		entrada.setAnulado(tipoAnulado);
		entrada.setDocumento(String.valueOf(tipoDocumento));
		entrada.setIncluyeAlistamiento(incluyeAlistamiento==null?true:incluyeAlistamiento);
		documentosHistoricos = historicoDocumentosService.consultarDocumentosCliente(entrada);
		documentoSeleccionado = !documentosHistoricos.isEmpty() ? documentosHistoricos.get(0) : null;
		calcularTotales();
		smsCheqFuturos = arccckfService.tieneChequesFuturos(noCia, cliente.getClientePK().getNoCliente())
				? "Cliente registra cheques futuros."
				: null;
		smsCheqProtestados = nativeService.tieneChequesProtestados(noCia, cliente.getClientePK().getNoCliente())
				? "Cliente tiene cheques protestados."
				: null;
		/*
		HashMap<String, Object> parametros = new HashMap<>();
		StringBuffer sqlFac = new StringBuffer();
		documentosHistoricos.stream().forEach(item -> {
			parametros.put("noCia", getCompania().getNoCia());
			parametros.put("noCliente", cliente.getClientePK().getNoCliente().toString());
			parametros.put("noFisico", item.getNoFisico());
			sqlFac.append(" select no_factu from fac_ventas_cab where no_cia = :noCia and no_cliente = to_number(:noCliente) and no_fisico = :noFisico ");
			List<Object> noFactur = nativeDmlDatabaseService.nativeQueryAdvanced(sqlFac, parametros, null, 0, 0);
			if(!noFactur.isEmpty()) {
				parametros.clear();
				sqlFac.setLength(0);
				noFactu = (String) noFactur.get(0);
				parametros.put("noCia", getCompania().getNoCia());
				parametros.put("noFactu", noFactu );
				sqlFac.append("select numero_cotizacion from proformas_cab where no_cia= :noCia and refer= :noFactu ");
				List<Object> noCotizacion = nativeDmlDatabaseService.nativeQueryAdvanced(sqlFac, parametros, null, 0, 0);
				if(!noCotizacion.isEmpty()) {
					noCoti = (BigDecimal) noCotizacion.get(0);
					parametros.clear();
					parametros.put("noCia", getCompania().getNoCia());
					parametros.put("codigo", item.getAgencia());
					parametros.put("noCoti", noCoti );
					sqlFac.setLength(0);
					sqlFac.append("select PATH_COMPROBANTE from SRFCOTI where no_cia = :noCia and centro = :codigo and numero_cotizacion = to_number(:noCoti)");				
					List<Object> pathComprobante = nativeDmlDatabaseService.nativeQueryAdvanced(sqlFac, parametros, null, 0, 0);
					comprobante = (String) pathComprobante.get(0);
					item.setPathComprobante(comprobante);
					if(!pathComprobante.isEmpty()) {
						LOG.error("comprobante"+comprobante);
						LOG.error(" agencia " + item.getAgencia() + " noProforma " + item.getNoFisico());
					}
				}
			}
			sqlFac.setLength(0);
			parametros.clear();
			LOG.error(" noCliente " + item.getAgencia() + " noFisico " + item.getNoFisico());
		});
	*/
	}
	
	public String verDetalleFac() {
		if (documentoSeleccionado != null && !lineaNegocioService.verificarLineaExoneracionPorCompania(this.noCia,
				documentoSeleccionado.getSerieFisico())) {
			return "datosFactura?faces-redirect=true?" + construirParametros();
		} else {
			warn("Debe seleccionar un documento para ver el detalle de la factura");
			return "";
		}
	}

	public String verHistorialPagos() {
		if (documentoSeleccionado != null) {
			return "consultaHistoricoPagos?faces-redirect=true?" + construirParametros();
		} else {
			warn("Debe seleccionar un documento para ver el historial de Pagos");
			return "";
		}
	}

	/**
	 * Permite imprimir el RIDE de la factura
	 */
	public void visualizarFactura() {
		documentoSeleccionado.setNoCia(this.noCia);
		documentoSeleccionado.setNoCliente(cliente.getClientePK().getNoCliente());
		if ("6".equals(documentoSeleccionado.getSerieFisico())) {
			ejecutarJavascript("document.getElementById(\"frmBotones:printMandatoUsado\").click()");
		} else {
			Object[] datosFac = nativeService.consultarDatosFactura(documentoSeleccionado);
			String clave = datosFac != null && datosFac[1] != null ? String.valueOf(datosFac[1]) : null;
			if (clave != null && clave.length() >= 49) {
				String ip = getCompania().getIpWebServer();
				if (ip.length() > 0) {
					ip = ip.replace("8080", "18080");
				}
				String pathNC = ip + PDF_PATH_FC + clave;
				ejecutarJavascript("openDuplicatedTab('" + pathNC + "')");
			} else {
				super.warn("La factura aun no ha sido autorizada");
			}
		}
	}

	public void generarPdfMandatoUsado() {
		String pathReporte = obtenerRuta("mandatoUsado.jasper");
		if (pathReporte != null) {
			Object[] datosFac = nativeService.consultarDatosFactura(documentoSeleccionado);
			Double valor = (datosFac[4] != null ? convertirADouble(String.valueOf(datosFac[4])) : 0D)
					- (datosFac[5] != null ? convertirADouble(String.valueOf(datosFac[5])) : 0D);
			String valorLetras = String.valueOf(valor);
			String enteros = NumericUtils.cantidadConLetra(valorLetras.substring(0, valorLetras.indexOf(".")));
			String decimales = NumericUtils
					.cantidadConLetra(valorLetras.substring(valorLetras.indexOf(".") + 1, valorLetras.length()));
			Map<String, Object> parametros = new HashMap<>();
			parametros.put("pNoCia", this.noCia);
			parametros.put("pNombreEmpresa", getCompania().getNombre());
			parametros.put("pNombreAgencia", documentoSeleccionado.getAgencia());
			parametros.put("pNoFactu", datosFac[0]);
			parametros.put("pNoCliente", cliente.getClientePK().getNoCliente());
			parametros.put("pDescripcionVeh", datosFac[2]);
			parametros.put("pNotaUsado", datosFac[3]);
			parametros.put("pUsuario", getLoggedUsername());
			parametros.put("pNumeroLetras", enteros.concat("dolares con ").concat(decimales).concat(" ctvs"));
			parametros.put("pObservaciones2", datosFac[6]);
			parametros.put("SUBREPORT_DIR", pathReporte.split("mandatoUsado")[0]);
			UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
			Connection connection = null;
			try {
				Map<String, Object> parameters = new HashMap<String, Object>();
				connection = utilServiceDelegate.getDataSource().getConnection();
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
						.getExternalContext().getResponse();
				String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance()
						.getExternalContext().getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
						+ System.getProperty("file.separator");

				JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
				parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

				JasperPrint jasperPrint = JasperFillManager.fillReport(pathReporte, parametros, connection);
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment;filename=\"mandatoUsado.pdf\"");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
				virtualizer.cleanup();
				FacesContext.getCurrentInstance().responseComplete();
				response.getOutputStream().flush();
				response.getOutputStream().close();
			} catch (IOException | JRException e) {
				LOG.error(e.getMessage() + ".EE. ", e);
			} catch (SQLException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				cerrarConexion(connection);
			}
		} else {
			error("No se encontro el reporte mandatoUsado");
		}

	}

	/**
	 * Permite convertir a double
	 * 
	 * @param valor
	 * @return
	 */
	private Double convertirADouble(String valor) {
		Double valorRes = null;
		try {
			valorRes = Double.valueOf(valor);
		} catch (Exception e) {
			LOG.error("Error de formato de numero: " + valor);
			valorRes = Double.valueOf(valor.replace(',', '.'));
		}
		return valorRes;
	}

	public String verPagosAnticipados() {
		if (documentoSeleccionado != null) {
			return "consultaPagosAnticipados?faces-redirect=true?" + construirParametros();
		} else {
			warn("Debe seleccionar un documento para ver el historial de Pagos");
			return "";
		}
	}

	public void verPantallaCliente() {
		if (cliente != null && cliente.getClientePK() != null && cliente.getClientePK().getNoCliente() != null) {
			StringBuilder parametros = new StringBuilder();
			parametros.append("lineaNeg=").append(noLineaNeg.replace('%', 'T'));
			parametros.append("&tipoDoc=").append(tipoDoc.replace('%', 'T'));
			parametros.append("&anulado=").append(tipoAnulado.replace('%', 'T'));
			parametros.append("&documento=").append(tipoDocumento);
			parametros.append("&fechaIni=").append(
					fechaInicio != null ? FechaUtils.formatearFecha(fechaInicio, FechaUtils.patronDiaMesAnio) : "");
			parametros.append("&fechaFin=")
					.append(fechaFin != null ? FechaUtils.formatearFecha(fechaFin, FechaUtils.patronDiaMesAnio) : "");
			parametros.append("&codigoCliente=").append(cliente.getClientePK().getNoCliente());
			parametros.append("&origen=consultaHistDocumentoClientes");
			parametros.append("&accion=EDIT");
			parametros.append(aplicaDesbloqueo ? "&APLDESB=S" : "");
			try {
				super.redirect("/cxc/faces/jsp/mantenimiento/cliente/clienteForm.jsp?" + parametros.toString());
			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				error("Se presento un error al redireccionar página, comuníquese con sistemas");
			}
		} else {
			warn("Primero debe seleccionar un cliente");
		}
	}

	public void regresarPantalla() throws IOException {

		String url = "/cxc-web-prime/jsf/procesos/seguimientoDetalleDeuda.jsf?origen=D"
				.concat(cliente != null ? "&noCLiente=".concat(cliente.getClientePK().getNoCliente().toString()) : "")
				.concat(zona != null ? "&zonasSelected=".concat(zona) : "").concat(canal != null ? "&canalSelected=".concat(canal) : "")
				.concat(tipoFinanciamiento != null ? "&tipoFinanciamiento=".concat(tipoFinanciamiento) : "")
				.concat(lineaNegocio != null ? "&lineaSeleccionada=".concat(lineaNegocio) : "")
				.concat(rangoAntiguedad != null ? "&rangoAntiguedadSelected=".concat(rangoAntiguedad) : "")
				.concat(agencia != null ? "agenciaSelected=".concat(agencia) : "");
		super.redirect(url);
	}

	public void validarGenDocRefinanciamiento() {
		if (documentoSeleccionado != null && documentoSeleccionado.getCantProrroga() != null
				&& documentoSeleccionado.getCantProrroga() > 1) {
			documentoSeleccionado.setNoCia(this.noCia);
			documentoSeleccionado.setNoCliente(cliente.getClientePK().getNoCliente());
			ejecutarJavascript("document.getElementById(\"frmBotones:printRefin\").click()");
		} else {
			warn("No puede imprimir estos documentos ya que no es un refinanciamiento");
		}
	}

	public void generarRepRefinanciamiento() {
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			connection = utilServiceDelegate.getDataSource().getConnection();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("p_no_cia", this.noCia);
			parameters.put("p_no_docu", nativeService.consultarNoDocuDocumento(documentoSeleccionado));
			parameters.put("p_nombre_empresa", getCompania().getNombre());
			parameters.put("p_agencia", documentoSeleccionado.getAgencia());
			parameters.put("p_no_fisico", documentoSeleccionado.getNoFisico());
			parameters.put("p_serie_fisico", documentoSeleccionado.getSerieFisico());
			parameters.put("p_cant_prorrogas", documentoSeleccionado.getCantProrroga());
			parameters.put("p_tipo", "R");
			parameters.put("p_no_cliente", cliente.getClientePK().getNoCliente());
			parameters.put("p_fecha", documentoSeleccionado.getFecha());
			List<JasperPrint> jaspers = new ArrayList<>();
			List<String> reportes = new ArrayList<>();
			reportes.add("/reportes/cxcNotaCredito.jasper");
			reportes.add("/reportes/cxcNotaDebito2.jasper");
			reportes.add(obtenerRuta("VehPagare.jasper"));
			reportes.add("/reportes/cxcTablaAmortizacion.jasper");
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			for (String reporte : reportes) {
				String pathReporte = !reporte.contains("VehPagare") ? getServletContext().getRealPath("/") + reporte
						: reporte;
				jaspers.add(JasperFillManager.fillReport(pathReporte,
						!reporte.contains("VehPagare") ? parameters : construirParametrosPagare(), connection));
				virtualizer.cleanup();
			}
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment;filename=\"documentosRefinanciamiento.pdf\"");
			JRPdfExporter exporter = new JRPdfExporter();
			exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jaspers);
			exporter.setParameter(JRPdfExporterParameter.IS_CREATING_BATCH_MODE_BOOKMARKS, Boolean.TRUE);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, response.getOutputStream());
			exporter.exportReport();

			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			LOG.error(e.getMessage(), e.getCause());
		} catch (Exception e) {
			LOG.error(e.getMessage(), e.getCause());
		} finally {
			cerrarConexion(connection);
		}
	}

	private Map<String, Object> construirParametrosPagare() {
		Map<String, Object> parameters = new HashMap<String, Object>();

		String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
				.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH") + System.getProperty("file.separator");

		JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
		parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

		parameters.put("NO_CIA", this.noCia);
		parameters.put("CENTRO", documentoSeleccionado.getAgencia());
		parameters.put("NO_FISICO", documentoSeleccionado.getNoFisico());
		parameters.put("SERIE_FISICO", documentoSeleccionado.getSerieFisico());
		parameters.put("CANT_PRORROGAS", documentoSeleccionado.getCantProrroga());
		parameters.put("CIUDAD",
				agenciaService.consultarCiudadPorAgencia(getCompania().getNoCia(), documentoSeleccionado.getAgencia()));
		parameters.put("NO_CLIENTE", cliente.getClientePK().getNoCliente());
		parameters.put("pTotalFinanciar", 0D);
		FacturasVehiculo facVentasCab = new FacturasVehiculo();
		facVentasCab.setFacVentasCabPK(new FacturasVehiculoPK(this.noCia, null, null, null));
		facVentasCab.setCentroD(documentoSeleccionado.getAgencia());
		facVentasCab.setNoCliente(cliente.getClientePK().getNoCliente().intValue());
		facVentasCab.setNoFisico(documentoSeleccionado.getNoFisico());
		facVentasCab.setSerieFisico(documentoSeleccionado.getSerieFisico());
		facVentasCab.setCantProrrogas(documentoSeleccionado.getCantProrroga().toString());
		List<Object[]> totales = arccmdService.obtenerTotales(facVentasCab);
		if (totales.size() > 0) {
			parameters.put("pTotalFinanciar", Double.valueOf(String.valueOf(totales.get(0)[1])));
		}
		return parameters;
	}

	/**
	 * Permite obtener la ruta del reporte en otro contexto
	 * 
	 * @param nombreReporte
	 * @return
	 */
	private String obtenerRuta(String nombreReporte) {
		EjecutaComandoUtils ejecutar = new EjecutaComandoUtils();
		ejecutar.executeCommand("updatedb");
		StringBuilder comando = new StringBuilder("locate ").append(nombreReporte);
		List<String> listaResultado = ejecutar.executeCommand(comando.toString());
		String ctxPath = null;
		for (String directorio : listaResultado) {
			if (directorio.contains("jboss-EAP-7.4")) {
				ctxPath = directorio;
			}
		}
		return ctxPath;
	}

	/**
	 * Pemite fabricar los parametros request a enviar
	 * 
	 * @return
	 */
	private String construirParametros() {
		StringBuilder parametros = new StringBuilder();
		parametros.append("&lineaNeg=").append(noLineaNeg.replace('%', 'T'));
		parametros.append("&tipoDoc=").append(tipoDoc.replace('%', 'T'));
		parametros.append("&anulado=").append(tipoAnulado.replace('%', 'T'));
		parametros.append("&documento=").append(tipoDocumento);
		parametros.append("&fechaIni=")
				.append(fechaInicio != null ? FechaUtils.formatearFecha(fechaInicio, FechaUtils.patronDiaMesAnio) : "");
		parametros.append("&fechaFin=")
				.append(fechaFin != null ? FechaUtils.formatearFecha(fechaFin, FechaUtils.patronDiaMesAnio) : "");
		parametros.append("&agencia=").append(documentoSeleccionado.getAgencia());
		parametros.append("&serieFisico=").append(documentoSeleccionado.getSerieFisico());
		parametros.append("&noFisico=").append(documentoSeleccionado.getNoFisico());
		parametros.append("&noCliente=").append(cliente.getClientePK().getNoCliente());
		parametros.append("&cantProrrogas=")
				.append(documentoSeleccionado.getCantProrroga() == null ? "" : documentoSeleccionado.getCantProrroga());
		parametros.append("&fecha=")
				.append(FechaUtils.formatearFecha(documentoSeleccionado.getFecha(), FechaUtils.patronDiaMesAnio));
		parametros.append(aplicaDesbloqueo ? "&APLDESB=S" : "");
		return parametros.toString();

	}

	/**
	 * Permite generar el reporte de historico de pagos
	 */
	public void generarReporte() {

		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			connection = utilServiceDelegate.getDataSource().getConnection();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			String ctxPath = getServletContext().getRealPath("/") + REPORTE_HIST_DOC;
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters = obtenerParametros();
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath, parameters, connection);
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + NOMBRE_REPORTE_HIST_DOC + "\"");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			virtualizer.cleanup();
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException | JRException e) {
			LOG.error(e.getMessage() + ".EE. ", e);
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			cerrarConexion(connection);
		}

	}

	/**
	 * Permite asignar los parametros del comprobante
	 * 
	 * @return
	 */
	private Map<String, Object> obtenerParametros() {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("p_no_cia", this.noCia);
		parametros.put("p_nombre_empresa", getCompania().getNombre().toUpperCase());
		parametros.put("p_usuario", getLoggedUsername());
		parametros.put("p_no_cliente", cliente.getClientePK().getNoCliente());
		parametros.put("p_linea_negocio", noLineaNeg);
		parametros.put("p_tipo_doc", tipoDoc);
		parametros.put("p_anulados", tipoAnulado);
		parametros.put("p_fecha_inicio",
				fechaInicio == null ? FechaUtils.convertirStrFecha("01/01/2000", FechaUtils.patronDiaMesAnio)
						: fechaInicio);
		parametros.put("p_fecha_fin", fechaFin == null ? new Date() : fechaFin);
		parametros.put("p_documento", tipoDocumento);
		parametros.put("p_sql_adicional0", tipoDocumento == 2 ? " and nvl(saldo,0) <> 0  " : "  ");
		parametros.put("p_sql_adicional1", tipoDocumento == 2 ? " and DOCUMENTO = 2  " : "  ");
		return parametros;
	}

	/**
	 * Permite cerrar la conexion a la base de datos
	 * 
	 * @param con
	 */
	private void cerrarConexion(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	/**
	 * Permite calcular los totales
	 */
	private void calcularTotales() {
		totalDebitoLocal = 0D;
		totalCreditoLocal = 0D;
		totalDebitoInt = 0D;
		totalCreditoInt = 0D;
		String idMonedaLocal = nativeService.obtenerCodMonedaLocal(this.noCia);
		if (documentosHistoricos != null && !documentosHistoricos.isEmpty()) {
			totalDebitoLocal = documentosHistoricos.stream()
					.mapToDouble(p -> idMonedaLocal.equals(p.getMonedaId()) && !"S".equals(p.getAnulado())
							&& "D".equals(p.getTipo()) && p.getSaldo() != null ? p.getSaldo() : 0D)
					.sum();
			totalCreditoLocal = documentosHistoricos.stream()
					.mapToDouble(p -> idMonedaLocal.equals(p.getMonedaId()) && !"S".equals(p.getAnulado())
							&& !"D".equals(p.getTipo()) && p.getSaldo() != null ? p.getSaldo() : 0D)
					.sum();
			totalDebitoInt = documentosHistoricos.stream()
					.mapToDouble(p -> !idMonedaLocal.equals(p.getMonedaId()) && !"S".equals(p.getAnulado())
							&& "D".equals(p.getTipo()) && p.getSaldo() != null ? p.getSaldo() : 0D)
					.sum();
			totalCreditoInt = documentosHistoricos.stream()
					.mapToDouble(p -> !idMonedaLocal.equals(p.getMonedaId()) && !"S".equals(p.getAnulado())
							&& !"D".equals(p.getTipo()) && p.getSaldo() != null ? p.getSaldo() : 0D)
					.sum();
		}
		totalDebitoLocal = CommonUtils.redondear(totalDebitoLocal, 2);
		totalCreditoLocal = CommonUtils.redondear(totalCreditoLocal, 2);
		totalDebitoInt = CommonUtils.redondear(totalDebitoInt, 2);
		totalCreditoInt = CommonUtils.redondear(totalCreditoInt, 2);
	}

	/**
	 * Permite cargar el dialogo de clientes
	 */

	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);
	}

	public void cargarDatosAprobacion(HistoricoDocumentoDto documentoDto) {
		documentoSeleccionado = documentoDto;
		documentoSeleccionado.setNoCia(this.noCia);
		documentoSeleccionado.setNoCliente(cliente.getClientePK().getNoCliente());
		cotizacion = historicoDocumentosService.consultarDatosAprobacion(documentoSeleccionado);
		if (cotizacion == null) {
			error("No existen datos a mostrar");
		} else {
			aplicaInteresMantPrepagados = false;
			if(cotizacion.getValorMantPrepagados() != null && cotizacion.getValorMantPrepagados() > 0D) {
				//Si tiene saldo a financiar del vehículo, valida
				if (cotizacion.getSaldoFinanciar() > 0 ) {//|| cotizacion.getCuotaDeAlcance() > 0) {
					Object[] resultados = this.valorOrdenRepPrepagados();
					if(resultados[0] != null && Double.valueOf(resultados[0].toString()) > 0) {
						aplicaInteresMantPrepagados = true;
					}
					valorCoutaMantPrepagados = Double.valueOf(resultados[1].toString());
				}else {
					valorCoutaMantPrepagados = 0D;
				}
			} else {
				valorCoutaMantPrepagados = 0D;
			}
			accionesDialog("dialogDatosAprob", Boolean.TRUE);
		}
	}
	
	public Object[] valorOrdenRepPrepagados() {
		Object[] datosFac = nativeService.consultarDatosFactura(documentoSeleccionado);
		
		Object[] datosFinanciaOrdenMpp = nativeDmlDatabaseService.datosFinanciaOrdenRepPrepagados(cotizacion.getCotizacionPK().getNoCia(), (String)datosFac[0]);
		return datosFinanciaOrdenMpp;
	}
	/**
	 * Permite validar los datos antes de enviar a generar el reporte de certificados
	 * 
	 * @param documentoDto
	 */
	public void validarGenRep(HistoricoDocumentoDto documentoDto, String idBoton) {
		documentoSeleccionado = documentoDto;
		documentoSeleccionado.setNoCia(this.noCia);
		if (documentoSeleccionado.getLineaFacturacion() != null
				&& (documentoSeleccionado.getSaldo() == null || documentoSeleccionado.getSaldo() == 0)
				&& "N".equals(documentoSeleccionado.getAnulado())) {
			ejecutarJavascript("document.getElementById(\"frmBotones:" + idBoton + "\").click()");
		} else {
			warn("NO PUEDE IMPRIMIR ESTE TIPO DE REGISTRO ...");
		}
	}

	/**
	 * Permite generar el reporte de certificados
	 */
	public void generarRepCertificado() {
		boolean exiCotObsReserv = cotizacionService.exiteCotizacionObsReserv(documentoSeleccionado);
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("p_no_cia", this.noCia);
		parametros.put("p_agencia", documentoSeleccionado.getAgencia());
		parametros.put("p_no_fisico", documentoSeleccionado.getNoFisico());
		parametros.put("p_serie_fisico", documentoSeleccionado.getSerieFisico());
		parametros.put("p_cant_prorrogas", documentoSeleccionado.getCantProrroga());
		parametros.put("p_no_cliente", cliente.getClientePK().getNoCliente());
		generarReportes(exiCotObsReserv ? REPORTE_CERTIFICADO_RESERV : REPORTE_CERTIFICADO,
				exiCotObsReserv ? NOMBRE_REPORTE_CERTIFICADO_RESERV : NOMBRE_REPORTE_CERTIFICADO, parametros);
	}

	/**
	 * Permite generar el reporte ANT
	 */
	public void generarRepAnt() {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("p_no_cia", this.noCia);
		parametros.put("p_agencia", documentoSeleccionado.getAgencia());
		parametros.put("p_no_fisico", documentoSeleccionado.getNoFisico());
		parametros.put("p_serie_fisico", documentoSeleccionado.getSerieFisico());
		parametros.put("p_cant_prorrogas", documentoSeleccionado.getCantProrroga());
		parametros.put("p_no_cliente", cliente.getClientePK().getNoCliente());
		generarReportes(REPORTE_ANT, NOMBRE_REPORTE_ANT, parametros);
	}

	public void abrirDialogDatAdicional(HistoricoDocumentoDto documentoDto) {
		documentoSeleccionado = documentoDto;
		documentoSeleccionado.setNoCia(this.noCia);
		noInscripcion = null;
		tomo = null;
		anio = null;
		fecha = null;
		accionesDialog("dialogDatAdicional", Boolean.TRUE);

	}

	public void generarRepRm() {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("p_no_cia", this.noCia);
		parametros.put("p_agencia", documentoSeleccionado.getAgencia());
		parametros.put("p_no_fisico", documentoSeleccionado.getNoFisico());
		parametros.put("p_serie_fisico", documentoSeleccionado.getSerieFisico());
		parametros.put("p_cant_prorrogas", documentoSeleccionado.getCantProrroga());
		parametros.put("p_no_cliente", cliente.getClientePK().getNoCliente());
		parametros.put("p_no_inscripcion", noInscripcion);
		parametros.put("p_tomo", tomo);
		parametros.put("p_anio", anio);
		parametros.put("p_fecha", fecha);
		generarReportes(REPORTE_RM, NOMBRE_REPORTE_RM, parametros);

	}

	public void abrirDialogDesbAnt(HistoricoDocumentoDto documentoDto) {
		documentoSeleccionado = documentoDto;
		documentoSeleccionado.setNoCia(this.noCia);
		documentoSeleccionado.setNombreArcDesbloqueo(null);
		documentoSeleccionado.setPathArcDesbAnticipo(null);
		prenda = CommonConstants.NO_STRING_VALUE;
		seguro = CommonConstants.NO_STRING_VALUE;
		observacionDesbloqueo = null;
		usuarioDto = null;
		accionesDialog("dialogDesbAnt", Boolean.TRUE);
	}

	/**
	 * Permite cargar el dialogo de usuarios
	 */

	public void cargarUsuariosSistema() {
		this.dialogUsuariosController.setNombreDialog("dialogUsuariosWV");
		this.dialogUsuariosController.cargarUsuarios();
		this.dialogUsuariosController.setUsuarioDto(null);
		accionesDialog("dialogUsuariosWV", Boolean.TRUE);
	}

	/**
	 * Permite desbloquear los anticipos
	 */
	public void desbloquearAnticipos() {
		String usuarioLoggin = getLoggedUsername();
		if (prenda == null || seguro == null) {
			warn("Debe seleccionar la prenda y seguro");
			accionesDialog("dialogDesbAnt", Boolean.TRUE);
		} else if (observacionDesbloqueo == null || observacionDesbloqueo.trim().length() == 0) {
			warn("Se debe ingresar la Observación para desbloquear el anticipo");
			accionesDialog("dialogDesbAnt", Boolean.TRUE);
		} else if (usuarioDto == null) {
			warn("Se debe seleccionar un usuario para envio de correo");
			accionesDialog("dialogDesbAnt", Boolean.TRUE);
		} else if (usuarioLoggin == null) {
			warn("No se logró obtener el usuario logeado, salir y entrar nuevamente a la pantalla");
			accionesDialog("dialogDesbAnt", Boolean.TRUE);
		} else if (guardarArchivoEnServidor()) {
			try {
				documentoSeleccionado
						.setComentario(observacionDesbloqueo.length() > 200 ? observacionDesbloqueo.substring(0, 200)
								: observacionDesbloqueo);
				documentoSeleccionado.setNoCliente(cliente.getClientePK().getNoCliente());
				documentoSeleccionado.setPrenda(prenda);
				documentoSeleccionado.setSeguro(seguro);
				historicoDocumentosService.desbloquearAnticipoArccmd(documentoSeleccionado, usuarioLoggin);
				info("Anticipo desbloqueado de forma exitosa");
				enviarMailUsuario();
				consultar();
				accionesDialog("dialogDesbAnt", Boolean.FALSE);
			} catch (GeneralException e) {
				error(e.getDetail());
			}
		}
	}

	/**
	 * Permite descargar el documento respaldo de desbloqueo
	 * 
	 * @param documentoDto
	 */
	public void descargarDoc(HistoricoDocumentoDto documentoDto) {
		try {
			documentoSeleccionado = documentoDto;
			FileUpload fileUpload = new FileUpload();
			fileUpload.seeFile(FacesContext.getCurrentInstance(), documentoSeleccionado.getPathArcDesbAnticipo());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	/**
	 * Permite guardar el documento respaldo desbloqueo en la ruta local
	 * 
	 * @return
	 */
	private boolean guardarArchivoEnServidor() {
		Boolean aceptar = Boolean.TRUE;
		if (file != null && file.getFileName().length() > 0) {
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.prime.cxc.web.prime.DOC_DESB_ANT")
					+ System.getProperty("file.separator");
			String fileName = file.getFileName().length() > 100 ? file.getFileName().substring(0, 100)
					: file.getFileName();
			try {
				File[] drives = File.listRoots();
				for (File fileDrives : drives) {
					// Si es windows
					if (fileDrives.getPath().length() > 1 && fileDrives.getPath().substring(0, 1).equals("C")) {
						path = fileDrives.getPath() + path;
					}
				}
				// Create the directory if it doesn't exist
				File dirPath = new File(path);
				if (!dirPath.exists()) {
					dirPath.mkdirs();
				}
				path = path + System.getProperty("file.separator") + FileUpload.cambioNombre(fileName).toLowerCase()
						.trim().replace(".", FechaUtils.formatearFecha(new Date(), "_dd_MM_yyyy_hh_mm_ss") + ".");
				InputStream in = file.getInputstream();
				// write the inputStream to a FileOutputStream
				OutputStream out = new FileOutputStream(path);

				int read = 0;
				byte[] bytes = new byte[10240];

				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				in.close();
				out.flush();
				out.close();
				documentoSeleccionado.setNombreArcDesbloqueo(fileName);
				documentoSeleccionado.setPathArcDesbAnticipo(path);
			} catch (Exception e) {
				aceptar = Boolean.FALSE;
				error(e.getMessage());
			}
		}
		return aceptar;
	}

	/**
	 * Permite enviar el correo de desbloqueo al usuario
	 */
	private void enviarMailUsuario() {
		String remitenteCorreo = consultarRemitenteCorreo();
		if (remitenteCorreo != null) {
			try {
				StringBuilder message = new StringBuilder();
				message.append("Estimado(a) ");
				message.append(usuarioDto.getNombre());
				message.append(",<br><br>");
				message.append("Se ha desbloqueado el anticipo número ");
				message.append(documentoSeleccionado.getNoFisico());
				message.append(" de ");
				message.append(documentoSeleccionado.getDesLineaAnticipos());
				message.append(" de la fecha ");
				message.append(
						FechaUtils.formatearFecha(documentoSeleccionado.getFecha(), FechaUtils.patronDiaMesAnio));
				message.append(" con el valor USD ");
				message.append(documentoSeleccionado.getSaldo());
				message.append(" del cliente ");
				message.append(cliente.getCedula());
				message.append(" ");
				message.append(cliente.getNombre());
				message.append(".");
				message.append("<br><br>");
				message.append("OBSERVACIÓN: ");
				message.append(observacionDesbloqueo);
				mailService.sendEmailAttachments(remitenteCorreo, new String[] { usuarioDto.getEmail().toLowerCase() },
						new String[] { getUsuario().getEmail() }, usuarioDto.getNombre(), "DESBLOQUEO DE ANTICIPO",
						new StringBuffer(message), Boolean.TRUE,
						file != null && file.getFileName().length() > 0
								? Arrays.asList(new File(documentoSeleccionado.getPathArcDesbAnticipo()))
								: null,
						Boolean.FALSE);
				info("Correo enviado con exito a correo del usuario");
			} catch (GeneralException e) {
				LOG.error("Error al enviar el correo ", e);
				warn("Error al enviar el correo al usuario");
			}

		}

	}

	/**
	 * Remitente por empresa
	 * 
	 * @return
	 */
	private String consultarRemitenteCorreo() {
		String emailNotifica = null;
		try {
			List<ParamDet> resultado = paramDetService.consultarPorCodigoCab(this.noCia,
					CommonConstants.CODIGO_CATALOGO_MAIL_NOTIFICACIONES);
			emailNotifica = resultado != null && !resultado.isEmpty() ? resultado.get(0).getTexto1() : null;
		} catch (FindException e) {
			emailNotifica = null;
		}
		return emailNotifica;
	}

	/**
	 * Permite generar los reportes de los documentos historicos
	 * 
	 * @param reporte
	 * @param nomReporte
	 */
	private void generarReportes(String reporte, String nomReporte, Map<String, Object> parametros) {
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			connection = utilServiceDelegate.getDataSource().getConnection();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			String ctxPath = getServletContext().getRealPath("/") + reporte;
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas

			parametros.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath, parametros, connection);
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + nomReporte + "\"");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			virtualizer.cleanup();
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException | JRException e) {
			LOG.error(e.getMessage() + ".EE. ", e);
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			cerrarConexion(connection);
		}
	}
	
	public void descargarArchivo(String path) {
		try {
			FileUpload fileUpload = new FileUpload();
			fileUpload.seeFile(FacesContext.getCurrentInstance(), path);
		} catch (Exception e) {
			error("No se pudo descargar el adjunto, intente nuevamente");
		}
	}

	//Req39875 BloqueoAnticipoPorCliente
	/**
	 * Metodo que permite abrir cuadro de dialogo e instanciar variables del mismo
	 * @param documentoDto
	 */
	public void abrirDialogBloquearAnt(HistoricoDocumentoDto documentoDto) {
		documentoSeleccionado = documentoDto;
		documentoSeleccionado.setNoCia(this.noCia);
		observacionDesbloqueo = VALOR_NULO;
		usuarioDto = USUARIO_NULO;
		observacionBloqueo = VALOR_NULO;
		accionesDialog("dialogBloqueoAnt", Boolean.TRUE);
	}

	//Req39875 BloqueoAnticipoPorCliente
	/**
	 * Metodo que permite bloquear anticipos en ARCCMD campos: fechaBloqueado, anticipoBloqueado y anticipoObservacionBloqueo
	 */
	public void bloquearAnticipos() {
		String usuarioLoggin = getLoggedUsername();
		if (checkTrimEmpty (observacionBloqueo)) {
			warn("Se debe ingresar la Observación para desbloquear el anticipo");
			accionesDialog("dialogBloqueoAnt", Boolean.TRUE);
		} else if (usuarioDto == null) {
			warn("Se debe seleccionar un usuario para envio de correo");
			accionesDialog("dialogBloqueoAnt", Boolean.TRUE);
		} else {
			try {
				documentoSeleccionado
				.setComentario(observacionBloqueo.length() > 200 ? observacionBloqueo.substring(0, 200)
						: observacionBloqueo);
				documentoSeleccionado.setNoCliente(cliente.getClientePK().getNoCliente());
				historicoDocumentosService.bloquearAnticipoArccmd(documentoSeleccionado, usuarioLoggin);
				info("Anticipo bloqueado de forma exitosa");
				enviarNotificacionBloqueoAnticipo();
				consultar();
				accionesDialog("dialogBloqueoAnt", Boolean.FALSE);
			} catch (GeneralException e) {
				error(e.getDetail());
			}
		}
	}

	//Req39875 BloqueoAnticipoPorCliente
	/**
	 * Metodo que genera envio de notificacion a usuario que solicita bloqueo de anticipo
	 */
	private void enviarNotificacionBloqueoAnticipo() {
		String remitenteCorreo = consultarRemitenteCorreo();
		if (remitenteCorreo != null) {
			try {
				StringBuffer message = new StringBuffer("Se ha bloqueado el anticipo número ")
						.append(documentoSeleccionado.getNoFisico()).append(" de ").append(documentoSeleccionado.getDesLineaAnticipos())
						.append(" de la fecha ").append(FechaUtils.formatearFecha(documentoSeleccionado.getFecha(), FechaUtils.patronDiaMesAnio))
						.append(" con el valor USD ").append(documentoSeleccionado.getSaldo()).append(" del cliente ").append(cliente.getCedula())
						.append(", ").append(cliente.getNombre()).append(".<br><br>").append("OBSERVACIÓN: ").append(observacionBloqueo);
				mailService.sendEmailCCLogo(remitenteCorreo, usuarioDto.getEmail(), getUsuario().getEmail(), usuarioDto.getNombre(), "NOTIFICACION BLOQUEO DE ANTICIPO CLIENTE", new StringBuffer(remplazarCaracteresEspeciales(message.toString())), Boolean.TRUE);
				info("Correo de notificación enviado exitosamente");
			} catch (GeneralException e) {
				LOG.error("Error al enviar el correo ", e);
				warn("Error al enviar el correo al usuario");
			}
		}
	}
	
	public void regresarPantallaMemoDiario() {
		try {
			if (placaVehPagosMemo != null) {
				super.redirect("/vehiculosWebPrime/jsf/proceso/matriculacion/vehGestionMemoDiario.jsf?placaVehiculo="
						+ placaVehPagosMemo);
			} else {
				addWarnMessage("Error:", "No es posible regresar a la pantalla de generación de diarios (pagos memo)");
			}
		} catch (IOException e) {
			LOG.log(Level.ERROR, "No es posible retornar a pantalla inicial" + e.getMessage());
		}
	}
	
	/**
	 * Permite asignar el valor seleccionado en la tabla
	 * 
	 * @param event
	 */
	public void onRowSelect(SelectEvent event) {
		documentoSeleccionado = (HistoricoDocumentoDto) event.getObject();
	}

	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}

	public void setDialogClientesController(DialogClientesController dialogClientesController) {
		this.dialogClientesController = dialogClientesController;
	}

	public Cliente getCliente() {
		if (Objects.nonNull(dialogClientesController.getCliente())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK().getNoCliente())) {
			cliente = dialogClientesController.getCliente();
			dialogClientesController.setCliente(null);
		}
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<LineaNegocio> getLineasNegocio() {
		return lineasNegocio;
	}

	public void setLineasNegocio(List<LineaNegocio> lineasNegocio) {
		this.lineasNegocio = lineasNegocio;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getNoLineaNeg() {
		return noLineaNeg;
	}

	public void setNoLineaNeg(String noLineaNeg) {
		this.noLineaNeg = noLineaNeg;
	}

	public List<Object[]> getTiposDocumento() {
		return tiposDocumento;
	}

	public void setTiposDocumento(List<Object[]> tiposDocumento) {
		this.tiposDocumento = tiposDocumento;
	}

	public String getTipoDoc() {
		return tipoDoc;
	}

	public void setTipoDoc(String tipoDoc) {
		this.tipoDoc = tipoDoc;
	}

	public String getTipoAnulado() {
		return tipoAnulado;
	}

	public void setTipoAnulado(String tipoAnulado) {
		this.tipoAnulado = tipoAnulado;
	}

	public Integer getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(Integer tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
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

	public List<HistoricoDocumentoDto> getDocumentosHistoricos() {
		return documentosHistoricos;
	}

	public void setDocumentosHistoricos(List<HistoricoDocumentoDto> documentosHistoricos) {
		this.documentosHistoricos = documentosHistoricos;
	}

	public HistoricoDocumentoDto getDocumentoSeleccionado() {
		return documentoSeleccionado;
	}

	public void setDocumentoSeleccionado(HistoricoDocumentoDto documentoSeleccionado) {
		this.documentoSeleccionado = documentoSeleccionado;
	}

	public Double getTotalDebitoLocal() {
		return totalDebitoLocal;
	}

	public void setTotalDebitoLocal(Double totalDebitoLocal) {
		this.totalDebitoLocal = totalDebitoLocal;
	}

	public Double getTotalCreditoLocal() {
		return totalCreditoLocal;
	}

	public void setTotalCreditoLocal(Double totalCreditoLocal) {
		this.totalCreditoLocal = totalCreditoLocal;
	}

	public Double getTotalDebitoInt() {
		return totalDebitoInt;
	}

	public void setTotalDebitoInt(Double totalDebitoInt) {
		this.totalDebitoInt = totalDebitoInt;
	}

	public Double getTotalCreditoInt() {
		return totalCreditoInt;
	}

	public void setTotalCreditoInt(Double totalCreditoInt) {
		this.totalCreditoInt = totalCreditoInt;
	}

	public Double getTotalLocal() {
		return CommonUtils.redondearToDouble(totalDebitoLocal + totalCreditoLocal, 2);
	}

	public Double getTotalInt() {
		return CommonUtils.redondearToDouble(totalDebitoInt + totalCreditoInt, 2);
	}

	public Cotizacion getCotizacion() {
		return cotizacion;
	}

	public void setCotizacion(Cotizacion cotizacion) {
		this.cotizacion = cotizacion;
	}

	public String getNoInscripcion() {
		return noInscripcion;
	}

	public void setNoInscripcion(String noInscripcion) {
		this.noInscripcion = noInscripcion;
	}

	public String getTomo() {
		return tomo;
	}

	public void setTomo(String tomo) {
		this.tomo = tomo;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public boolean isAplicaDesbloqueo() {
		return aplicaDesbloqueo;
	}

	public void setAplicaDesbloqueo(boolean aplicaDesbloqueo) {
		this.aplicaDesbloqueo = aplicaDesbloqueo;
	}

	public String getObservacionDesbloqueo() {
		return observacionDesbloqueo;
	}

	public void setObservacionDesbloqueo(String observacionDesbloqueo) {
		this.observacionDesbloqueo = observacionDesbloqueo;
	}

	public String getSeguro() {
		return seguro;
	}

	public void setSeguro(String seguro) {
		this.seguro = seguro;
	}

	public String getPrenda() {
		return prenda;
	}

	public void setPrenda(String prenda) {
		this.prenda = prenda;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public DialogUsuariosController getDialogUsuariosController() {
		return dialogUsuariosController;
	}

	public void setDialogUsuariosController(DialogUsuariosController dialogUsuariosController) {
		this.dialogUsuariosController = dialogUsuariosController;
	}

	public UsuarioDto getUsuarioDto() {
		if (dialogUsuariosController != null && dialogUsuariosController.getUsuarioDto() != null
				&& dialogUsuariosController.getUsuarioDto().getUsuario() != null) {
			usuarioDto = dialogUsuariosController.getUsuarioDto();
			dialogUsuariosController.setUsuarioDto(null);
		}
		return usuarioDto;
	}

	public void setUsuarioDto(UsuarioDto usuarioDto) {
		this.usuarioDto = usuarioDto;
	}

	public ParamDetServiceLocal getParamDetService() {
		return paramDetService;
	}

	public void setParamDetService(ParamDetServiceLocal paramDetService) {
		this.paramDetService = paramDetService;
	}

	public String getZona() {
		return zona;
	}

	public void setZona(String zona) {
		this.zona = zona;
	}

	public String getTipoFinanciamiento() {
		return tipoFinanciamiento;
	}

	public void setTipoFinanciamiento(String tipoFinanciamiento) {
		this.tipoFinanciamiento = tipoFinanciamiento;
	}

	public String getRangoAntiguedad() {
		return rangoAntiguedad;
	}

	public void setRangoAntiguedad(String rangoAntiguedad) {
		this.rangoAntiguedad = rangoAntiguedad;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getSmsCheqFuturos() {
		return smsCheqFuturos;
	}

	public void setSmsCheqFuturos(String smsCheqFuturos) {
		this.smsCheqFuturos = smsCheqFuturos;
	}

	public String getSmsCheqProtestados() {
		return smsCheqProtestados;
	}

	public void setSmsCheqProtestados(String smsCheqProtestados) {
		this.smsCheqProtestados = smsCheqProtestados;
	}

	public String getComprobante() {
		return comprobante;
	}

	public void setComprobante(String comprobante) {
		this.comprobante = comprobante;
	}

	public String getNoFactu() {
		return noFactu;
	}

	public void setNoFactu(String noFactu) {
		this.noFactu = noFactu;
	}

	public BigDecimal getNoCoti() {
		return noCoti;
	}

	public void setNoCoti(BigDecimal noCoti) {
		this.noCoti = noCoti;
	}

	public boolean isAplicaBloqueo() {
		return aplicaBloqueo;
	}

	public void setAplicaBloqueo(boolean aplicaBloqueo) {
		this.aplicaBloqueo = aplicaBloqueo;
	}

	public String getObservacionBloqueo() {
		return observacionBloqueo;
	}

	public void setObservacionBloqueo(String observacionBloqueo) {
		this.observacionBloqueo = observacionBloqueo;
	}
	
	public Boolean getIncluyeAlistamiento() {
		return incluyeAlistamiento;
	}

	public void setIncluyeAlistamiento(Boolean incluyeAlistamiento) {
		this.incluyeAlistamiento = incluyeAlistamiento;
	}

	public String getPlacaVehPagosMemo() {
		return placaVehPagosMemo;
	}

	public void setPlacaVehPagosMemo(String placaVehPagosMemo) {
		this.placaVehPagosMemo = placaVehPagosMemo;
	}

	public Double getValorCoutaMantPrepagados() {
		return valorCoutaMantPrepagados;
	}

	public void setValorCoutaMantPrepagados(Double valorCoutaMantPrepagados) {
		this.valorCoutaMantPrepagados = valorCoutaMantPrepagados;
	}

	public boolean isAplicaInteresMantPrepagados() {
		return aplicaInteresMantPrepagados;
	}

	public void setAplicaInteresMantPrepagados(boolean aplicaInteresMantPrepagados) {
		this.aplicaInteresMantPrepagados = aplicaInteresMantPrepagados;
	}
	
}
