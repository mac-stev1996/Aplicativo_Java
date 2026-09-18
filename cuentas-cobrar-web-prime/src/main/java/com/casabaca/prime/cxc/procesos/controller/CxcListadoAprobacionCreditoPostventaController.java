package com.casabaca.prime.cxc.procesos.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;

import com.casabaca.administration.ejb.model.PlantillaMail;
import com.casabaca.administration.ejb.service.PlantillaMailServiceLocal;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Canton;
import com.casabaca.common.ejb.model.CantonPK;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.CotizacionRepuesto;
import com.casabaca.common.ejb.model.CxcClaseCliente;
import com.casabaca.common.ejb.model.DigDocumentos;
import com.casabaca.common.ejb.model.NomEmpleados;
import com.casabaca.common.ejb.model.NomEmpleadosPK;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.CantonServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CotizacionRepuestoServicesLocal;
import com.casabaca.common.ejb.service.CxcClaseClienteServiceLocal;
import com.casabaca.common.ejb.service.DigDocumentoServiceLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.NominaEmpleadosServiceLocal;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.service.S3SPropertiesServiceBean;
import com.casabaca.common.ejb.util.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcCreDigPostventa;
import com.casabaca.cxc.ejb.modelo.CxcCreditoPostventa;
import com.casabaca.cxc.ejb.servicio.CxcCreDigPostventaServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcCreditoPostventaServiceLocal;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.exception.WServiceException;
import com.casabaca.inventario.ejb.service.InvVendedorServiceLocal;
import com.casabaca.nomina.ejb.modelo.NomAreaPK;
import com.casabaca.nomina.ejb.modelo.NomDepartamentoPK;
import com.casabaca.nomina.ejb.modelo.NomDivisionPK;
import com.casabaca.nomina.ejb.modelo.NomSeccion;
import com.casabaca.nomina.ejb.modelo.NomSeccionPK;
import com.casabaca.nomina.ejb.servicio.NomAreaServicioLocal;
import com.casabaca.nomina.ejb.servicio.NomDepartamentoServicioLocal;
import com.casabaca.nomina.ejb.servicio.NomDivisionServicioLocal;
import com.casabaca.nomina.ejb.servicio.NomSeccionServicioLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.EnumEstSolicitud;
import com.casabaca.prime.cxc.lazy.LDMSolicitudCreditoPostventa;
import com.casabaca.rest.wsNpsIntegrado.entidad.BodyInDatosNps;
import com.casabaca.rest.wsNpsIntegrado.entidad.OutputDatosNps;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.AgenciaPK;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.model.SisUsuariosBitacora;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.SisUsuariosBitacoraServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;
import com.casabaca.webservice.wsTareasNps.NpsIntegradoRest;
import com.casabaca.webservice.wsTareasNps.impl.NpsIntegradoImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean
public class CxcListadoAprobacionCreditoPostventaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CxcListadoAprobacionCreditoPostventaController.class);
	private static final String VALOR_NULO = null;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;
	@EJB(lookup = NombreJNDI.DIG_DOCUMENTO_SERVICE)
	private DigDocumentoServiceLocal digDocumentoService;
	@EJB(lookup = NombreJNDI.CANTON_SERVICE)
	private CantonServiceLocal cantonService;
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetServiceLocal;
	@EJB(lookup = NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN)
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.CXC_CREDITO_POSTVENTA_SERVICE)
	private CxcCreditoPostventaServiceLocal creditoPostventaService;
	@EJB(lookup = NombreJNDI.CXC_CRE_DIG_POSTVENTA_SERVICE)
	private CxcCreDigPostventaServiceLocal archivoCrePostventaService;
	// NUEVO REQUERIMIENTO
	@EJB(lookup = NombreJNDI.CXC_CLASE_CLIENTE_SERVICE)
	private CxcClaseClienteServiceLocal servicioClaseCliente;
	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabServiceLocal;
	// IMPLEMENTACION NPS REQ-37739
	@EJB(lookup = NombreJNDI.NOMINA_EMPLEADOS_SERVICE)
	private NominaEmpleadosServiceLocal nominaEmpleadosService;
	@EJB(lookup = NombreJNDI.PLANTILLA_MAIL_BEAN)
	private PlantillaMailServiceLocal plantillaMailServiceLocal;
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;
	@EJB(lookup = NombreJNDI.S3S_PROPERTIES_SERVICE)
	private S3SPropertiesServiceBean s3sPropertiesService;
	@EJB(lookup = NombreJNDI.NOM_DEPARTAMENTO_SERVICIO_LOCAL)
	private NomDepartamentoServicioLocal nomDepartamentoServicioLocal;
	@EJB(lookup = NombreJNDI.NOM_DIVISION_SERVICIO_LOCAL)
	private NomDivisionServicioLocal nomDivisionServicioLocal;
	@EJB(lookup = NombreJNDI.NOM_SECCION_SERVICIO_LOCAL)
	private NomSeccionServicioLocal nomSeccionServicioLocal;
	@EJB(lookup = NombreJNDI.NOM_AREA_SERVICIO_LOCAL)
	private NomAreaServicioLocal nomAreaServicioLocal;
	@EJB(lookup = NombreJNDI.SIS_USUSARIOS_BITACORA_SERVICE)
	private SisUsuariosBitacoraServiceLocal sisUsuariosBitacoraService;
	@EJB(lookup = NombreJNDI.COTIZACION_REPUESTOS_SERVICES_BEAN)
	private CotizacionRepuestoServicesLocal cotizacionRepuestosServices;
	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;
	@EJB(lookup = NombreJNDI.INV_VENDEDOR_SERVICE)
	private InvVendedorServiceLocal invVendedorServiceLocal;

	private List<CxcCreditoPostventa> listaCreditos;
	private CxcCreditoPostventa crePosAprobado;
	private CxcCreditoPostventa crePosAprobadoSel;
	private List<CxcCreDigPostventa> listaArchivosDigitales = new ArrayList<>();
	private String idCiudadGarante;
	private String idTipoVivienda;
	private String idBanco;
	private String idCiudadTrabajo;

	private String noCia;
	private String centro;
	private String usuarioSesion;
	private String usuarioModulo;

	private String cedulaCliente;
	private String nombreCliente;
	private String domicilioCliente;
	private String refDomicilioCliente;
	private String bancoCliente;
	private String noCuentaCliente;
	private String estCivilCliente;
	private String correoCliente;
	private String telefonoCliente;
	private String celularCliente;
	private String tipoCuentaCliente;
	private Cliente cliente;
	private LazyDataModel<Cliente> lazyModel;
	private Object[] archivo;
	private UploadedFile file;
	private List<CxcCreditoPostventa> listaPreCreditos;

	private List<Cliente> listarClientes;
	private List<ParamDet> listaTiposVivienda = new ArrayList<>();
	private List<SelectItem> listaCmbTipoVivienda = new ArrayList<>();
	private List<Canton> cantonesList = new ArrayList<Canton>();
	private List<Canton> cantonesListGarante = new ArrayList<Canton>();
	private List<ParamDet> listaTiposCtaBancaria = new ArrayList<>();
	private List<SelectItem> listaCmbTipoCuenta = new ArrayList<>();
	private List<SelectItem> listarBancos = new ArrayList<>();
	private List<SelectItem> estadoCivilList = new ArrayList<SelectItem>();

	private static final String CODIGO_CATALOGO = "EMTCTA";
	private static final String CODIGO_VIVIENDA = "CLIVIE";
	private static final String CLASIFICACION_CLIENTES = "CLFCLI";
	private static final String BLOQUEO_CLIENTES = "BLOQUE";
	public static final String RUTA_IMAGEN_C = "/resources/images/nps/centric_cab";
	public static final String RUTA_IMAGEN_D = "/resources/images/nps/centric_det";
	public static final String EXT_IMAGEN = ".png";
	
	private String urlReport;
	private String criterioCedula;
	private String criterioNombre;
	private boolean activaTab;
	private boolean edit;
	private boolean nuevo;
	private int numero = 0;
	private String urlArchivo;
	private int idTmpArchivoL;
	private boolean activaObservacion = true;
	private boolean actBtnAprobar = true;
	private Double totalGastos = 0.00;
	// NUEVO REQUERIMIENTO
	private CxcCreDigPostventa archivoSel = new CxcCreDigPostventa();
	private boolean habCampo;
	private String idTipoCedula;
	private List<CxcClaseCliente> claseClienteList = new ArrayList<CxcClaseCliente>();
	private Float limiteCrediCliente;
	private Integer limiteFactCliente;
	private Short plazoCliente;
	private String promoCliente;
	private Float tasaIntCliente;
	private String pagareCupoCliente;
	private Short diasGraciaCliente;
	private String motivoCliente;
	private String claseVehiculos;
	private String claseRepuestos;
	private String claseServicio;
	private List<CxcCreDigPostventa> listaArchivosDigitalesRevision = new ArrayList<>();
	private List<ParamDet> listaTiposCliente = new ArrayList<>();
	private List<SelectItem> listaCmbTiposCliente = new ArrayList<>();
	private String idTipoCliente;
	private String idTipoArchRev = null;
	private boolean btnNegar = false;
	// NUEVO REQUERIMIENTO
	private List<ParamDet> listaTipoBloqueo = new ArrayList<>();
	private List<SelectItem> listaCmbTipoBloqueo = new ArrayList<>();
	private Integer tipoBloqueo;
	// IMPLEMENTACION NPS REQ-37739
	public static final String NPS_MAIL = "NOTIFICA_NPS";
	private static final String APP_NPS_BACKEND_API = "appnpsbackend.API_NPS";
	private static final String APP_NPS_FRONTEND_URL = "appnpsfrontend.URL_NPS";
	private static final int MAX_LENGTH = 600;
	// Req-37713 ReqCambios Solicitud Credito
	private boolean activaReport;
	private boolean actBtnGuardar;
	private List<CxcCreDigPostventa> listaArchivos;
	private List<CxcCreDigPostventa> listaArchivosRev;
	private List<Agencia> listaAgencias;
	private String codAgencia;
	private int SIZE = 10;
	private List <String> estado;
	private Date fechaDesde;
	private Date fechaHasta;
	private LDMSolicitudCreditoPostventa model;
	private CotizacionRepuesto cotizacionRepuesto;
	private BigDecimal monto;
	private BigDecimal cupoDisponible;
	private BigDecimal creditoSolicitado;
	private BigDecimal nuevoCupoDisponible;
	private BigDecimal saldoVencido;
	private BigDecimal garancheck;
	private String comentario;
	private String sobregiro;
	private List<CotizacionRepuesto> listCotiByCliente;

	@PostConstruct
	public void init() throws Exception {
		if (getRequestParameter("noSolicitud") != null) {
			validarSolicitudSeleccionada();
			editarSolicitud(crePosAprobadoSel);
		} else if (getRequestParameter("regresa") != null) {
			regresaListadoSolicitudes();
		} else {
			cargaListaSolicitudes();
		}
	}

	public List<Canton> obtenerTodosCantones() {
		try {
			return cantonesList = cantonService.findAll();
		} catch (Exception e) {
			error("No se puede cargar las ciudades");
		}
		return cantonesList;
	}

	public List<Canton> obtenerCantonesGarante() {
		try {
			return cantonesListGarante = cantonService.findAll();
		} catch (Exception e) {
			error("No se puede cargar las ciudades");
		}
		return cantonesListGarante;
	}

	@SuppressWarnings("unused")
	private void cargarArchivo() {

		try {
			UploadedFile uf = this.file;
			Object[] file = new Object[3];
			file[0] = uf.getFileName();
			file[1] = uf.getFileName().substring(uf.getFileName().lastIndexOf("."), uf.getFileName().length());
			file[2] = uf.getInputstream();
			this.setArchivo(file);
		} catch (IOException e) {
			logger.error(e);
		}

	}

	public void buscarSolicitud() {
		this.noCia = getCompania().getNoCia();
		this.estado = new ArrayList<>();
		this.estado.add(EnumEstSolicitud.REVISADO.getCodigo());
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.codAgencia, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			info("No se han encontrado Solicitudes de Credito con los criterios de busqueda seleccionados");
		}

	}

	public void guardarArchivoDigital(FileUploadEvent event) {
		try {

			String nombreArchivo = event.getFile().getFileName();

			InputStream inputStream = event.getFile().getInputstream();
			// Subir Archivo e inserta en tabla DIG_DOCUMENTO
			DigDocumentos digDocumento = new DigDocumentos();
			digDocumento.setNoCia(getCompania().getNoCia());
			digDocumento.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
			digDocumento.setLineaNegocio("REPUESTOS");
			digDocumento.setEntidad("CREDITO_POSTVENTA");
			digDocumento.setFechaIngreso(new Date());
			digDocumento.setUsuarioIngreso(usuarioSesion);
			digDocumento.setDocumento(null);
			digDocumento.setNombreArchivo(nombreArchivo);
			digDocumento.setArchivo(inputStream);
			digDocumento.setPathFileServer(getCompania().getPathFileServer());
			digDocumento.setRucEmpresa(getCompania().getIdTributario());
			digDocumento.setCategoria("DOCUMENTO DIGITALIZADO");
			digDocumento.setObservacion("SUBIR DOCUMENTO SOLICITUD EXPRESS");
			String pathDestino = digDocumentoService.crearDocumento(digDocumento);

			CxcCreDigPostventa archivo = new CxcCreDigPostventa();
			archivo.setNoCia(getCompania().getNoCia());
			archivo.setNoSolicitud(crePosAprobado.getNoSolicitud());
			archivo.setNombre(nombreArchivo);
			archivo.setTipo("D");
			archivo.setUsuarioCrea(usuarioSesion);
			archivo.setFechaCrea(new Date());
			archivo.setArchivo(inputStream);
			archivo.setPath(pathDestino);
			archivoCrePostventaService.create(archivo);
			listaArchivosDigitales.add(archivo);
			addInfoMessage("", "Los archivos fueron cargados correctamente.");
		} catch (IOException e) {
			addErrorMessage("", "Error al cargar los archivos.");
			logger.error(e.getMessage(), e.getCause());
		} catch (InsertException e) {
			addErrorMessage("", "Error al cargar los archivos.");
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void guardarArchivoDigitalRevision(FileUploadEvent event) {
		try {

			String nombreArchivo = event.getFile().getFileName();

			InputStream inputStream = event.getFile().getInputstream();
			// Subir Archivo e inserta en tabla DIG_DOCUMENTO
			DigDocumentos digDocumento = new DigDocumentos();
			digDocumento.setNoCia(getCompania().getNoCia());
			digDocumento.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
			digDocumento.setLineaNegocio("REPUESTOS");
			digDocumento.setEntidad("CREDITO_POSTVENTA");
			digDocumento.setFechaIngreso(new Date());
			digDocumento.setUsuarioIngreso(usuarioSesion);
			digDocumento.setDocumento(null);
			digDocumento.setNombreArchivo(nombreArchivo);
			digDocumento.setArchivo(inputStream);
			digDocumento.setPathFileServer(getCompania().getPathFileServer());
			digDocumento.setRucEmpresa(getCompania().getIdTributario());
			digDocumento.setCategoria("DOCUMENTO DIGITALIZADO");
			digDocumento.setObservacion("SUBIR DOCUMENTO SOLICITUD EXPRESS");
			String pathDestino = digDocumentoService.crearDocumento(digDocumento);

			CxcCreDigPostventa archivo = new CxcCreDigPostventa();
			archivo.setNoCia(getCompania().getNoCia());
			archivo.setNoSolicitud(crePosAprobado.getNoSolicitud());
			archivo.setNombre(nombreArchivo);
			archivo.setTipo("R");
			archivo.setTipoArchivo(idTipoArchRev);
			archivo.setUsuarioCrea(usuarioSesion);
			archivo.setFechaCrea(new Date());
			archivo.setArchivo(inputStream);
			archivo.setPath(pathDestino);
			archivoCrePostventaService.create(archivo);
			listaArchivosDigitalesRevision.add(archivo);
			addInfoMessage("", "Los archivos fueron cargados correctamente.");
		} catch (IOException e) {
			addErrorMessage("", "Error al cargar los archivos.");
			logger.error(e.getMessage(), e.getCause());
		} catch (InsertException e) {
			addErrorMessage("", "Error al cargar los archivos.");
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void envioMailSolicitudAprobada() throws UpdateException { // MAIL al grupo deSolicitudes de confirmación
		HashMap<String, byte[]> attachments = new HashMap<String, byte[]>();
		Map<String, Object> parameters = new HashMap<>();
		setParametersConvenio(parameters);
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
			String ctxPath = getServletContext().getRealPath("/");
			String path = getStrConvenioPath();
			connection = utilServiceDelegate.getDataSource().getConnection();
			String path2 = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path2);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + path, parameters, connection);
			// OBTENER ARCHIVOS REIMPRESIONES
			if (jasperPrint.getPages().size() != 0) {
				attachments.put("ConvenioCreditoComercial.pdf", recuperarPdfByte(jasperPrint));
			}
			UsuarioSis solicitante = usuarioSisServiceLocal
					.getUsuarioSisByUsername(this.crePosAprobado.getUsuarioEnvia());

			String nomAgencia = agenciaService.buscarNombreAgencia(crePosAprobado.getNoCia(),
					crePosAprobado.getCentro());
			StringBuffer mensaje = new StringBuffer(
					" La Solicitud de Credito Express, se encuentra APROBADA con los siguientes datos: <br><br>");
			mensaje.append("<table border='1'><tr><td><b>Numero Solicitud</b></td><td>"
					+ crePosAprobado.getNoSolicitud() + "</td></tr>");
			mensaje.append("<tr><td><b>Fecha Aprobación </b></td> <td> "
					+ new SimpleDateFormat("dd/MM/yyyy").format(crePosAprobado.getFechaEstAprobado()) + "</td></tr>");

			if (crePosAprobado.getEstado().equals(EnumEstSolicitud.APROBADO.getCodigo())) {
				mensaje.append("<tr><td><b>Estado</b></td> <td> " + " APROBADA " + "</td></tr>");
			}
			mensaje.append("<tr><td><b>Cédula Cliente</b></td> <td> " + crePosAprobado.getCedula() + "</td></tr>");
			mensaje.append("<tr><td><b>Nombre Cliente</b></td> <td> " + crePosAprobado.getNombres() + "</td></tr>");
			mensaje.append("<tr><td><b>Centro</b></td> <td> " + nomAgencia + "</td></tr>");
			mensaje.append("<tr><td><b>Numero de Centro</b></td> <td> " + crePosAprobado.getCentro() + "</td></tr>");
			mensaje.append("<tr><td><b>Observación Aprobación</b></td> <td> "
					+ crePosAprobado.getObservacionEstAprobado() + "</td></tr>");
			mensaje.append("</table>");
			mensaje.append(
					"<br><br>Por favor revisar la Solicitud de Credito Postventa Express en el sistema S3S.<br><br>");
			SisMailServidores sisMailServidores = null;
			sisMailServidores = getSisMailServidores(crePosAprobado.getNoCia());

			if (EnumEstSolicitud.APROBADO.getCodigo().equals(crePosAprobado.getEstado())) {
				mailService.sendEmailAttachmentsArray(sisMailServidores, sisMailServidores.getCuenta(),
						solicitante.getEmail(), solicitante.getNombre(),
						"NOTIFICACION DE APROBACIÓN DE SOLICITUD EXPRESS NUMERO: " + crePosAprobado.getNoSolicitud(),
						mensaje, true, attachments);
			} // Mail al asesor quien registra la solicitud
			mailService.sendEmailAttachmentsArray(sisMailServidores, sisMailServidores.getCuenta(),
					getUsuario().getEmail(), getUsuario().getNombre(),
					"NOTIFICACION DE APROBACIÓN DE SOLICITUD EXPRESS NUMERO: " + crePosAprobado.getNoSolicitud(),
					mensaje, true, attachments);
			super.info("La notificación ha sido enviada por correo eletrónico");
		} catch (GeneralException e) {
			logger.error(e.getMessage(), e.getCause());
			error("La notificación no pudo ser enviada por correo eletrónico");
		} catch (SQLException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (FindException e) {
			logger.error(e.getMessage(), e.getCause());
		}

	}

	public void envioMailSolictudNegada() throws UpdateException, GeneralException { // MAIL al grupo deSolicitudes de
		try {
			UsuarioSis solicitante = usuarioSisServiceLocal
					.getUsuarioSisByUsername(this.crePosAprobado.getUsuarioEnvia()); // confirmación
			StringBuffer mensaje = new StringBuffer();
			mensaje.append("La Solicitud de Credito Express, se encuentra NEGADA con los siguientes datos: <br><br>");
			mensaje.append("<table border='1'><tr><td><b>Numero Solicitud</b></td><td>"
					+ crePosAprobado.getNoSolicitud() + "</td></tr>");
			mensaje.append("<tr><td><b>Fecha Negación </b></td> <td> "
					+ new SimpleDateFormat("dd/MM/yyyy").format(crePosAprobado.getFechaEstAprobado()) + "</td></tr>");
			if (crePosAprobado.getEstado().equals(EnumEstSolicitud.NEGADO.getCodigo())) {
				mensaje.append("<tr><td><b>Estado</b></td> <td> " + " NEGADA " + "</td></tr>");
			}
			mensaje.append("<tr><td><b>Cédula Cliente</b></td> <td> " + crePosAprobado.getCedula() + "</td></tr>");
			mensaje.append("<tr><td><b>Nombres Cliente</b></td> <td> " + crePosAprobado.getNombres() + "</td></tr>");
			mensaje.append("<tr><td><b>Observación Negación</b></td> <td> " + crePosAprobado.getObservacionEstAprobado()
					+ "</td></tr>");
			mensaje.append("</table>");
			mensaje.append(
					"<br><br>Por favor revisar la Solicitud de Credito Postventa Express en el sistema S3S.<br><br>");
			SisMailServidores sisMailServidores = getSisMailServidores(crePosAprobado.getNoCia());

			if (EnumEstSolicitud.NEGADO.getCodigo().equals(crePosAprobado.getEstado())) {
				mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
						solicitante.getEmail(),
						"NOTIFICACION DE NEGACIÓN DE SOLICITUD EXPRESS NUMERO: " + crePosAprobado.getNoSolicitud(),
						mensaje);
			} // Mail al asesor quien registra la solicitud
			mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(), getUsuario().getEmail(),
					"NOTIFICACION DE NEGACIÓN DE SOLICITUD EXPRESS NUMERO: " + crePosAprobado.getNoSolicitud(),
					mensaje);
			super.info("La notificación ha sido enviada por correo eletrónico");
		} catch (GeneralException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (FindException e) {
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void cargarTipoVivienda() {
		try {
			this.listaTiposVivienda = paramDetServiceLocal.consultarPorCodigoCabecera(this.getCompania().getNoCia(),
					CODIGO_VIVIENDA);
			if (this.listaTiposVivienda.size() > 0) {
				for (ParamDet item : this.listaTiposVivienda) {
					this.listaCmbTipoVivienda
							.add(new SelectItem(item.getParamDetPK().getCodigoDet(), item.getDescripcion()));
				}
			}
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion sobre tipos de vivienda");
		}
	}

	public void cargarTipoCtasBancarias() {
		try {
			this.listaTiposCtaBancaria = paramDetServiceLocal.consultarPorCodigoCabecera(this.getCompania().getNoCia(),
					CODIGO_CATALOGO);
			if (this.listaTiposCtaBancaria.size() > 0) {
				for (ParamDet item : this.listaTiposCtaBancaria) {
					this.listaCmbTipoCuenta
							.add(new SelectItem(item.getParamDetPK().getCodigoDet(), item.getDescripcion()));
				}
			}
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion sobre tipos de cuentas bancarias");
		}
	}

	public List<SelectItem> cargarEstadoCivilList() {
		List<ParamDet> parametros = paramDetServiceLocal
				.findEstadoCivilByNoCia(getCompania() == null ? getCompania().getNoCia() : getCompania().getNoCia());

		for (ParamDet element : parametros) {
			estadoCivilList.add(new SelectItem(element.getParamDetPK().getCodigoDet(), element.getDescripcion()));
		}

		return estadoCivilList;
	}

	public void cargarListaBancos() {
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		sql.setLength(0);
		parametros.clear();
		sql.append(" SELECT DISTINCT CODIGO_BANCO, DESCRIPCION FROM CHE_BANCO_CTATRANSFERENCIA ");
		sql.append(" WHERE NO_CIA = :noCia");
		sql.append(" AND ESTADO = 'A' ");
		sql.append(" ORDER BY DESCRIPCION ");
		parametros.put("noCia", this.getCompania().getNoCia());
		List<Object[]> list = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null, 0, 150);
		if (list.size() > 0) {
			for (Object[] item : list) {
				this.listarBancos.add(new SelectItem(item[0].toString(), item[1].toString()));
			}
		}

	}

	public void editarSolicitud(CxcCreditoPostventa itemSel) {
		try {
			if (itemSel != null) {
				this.nuevo = true;
				this.activaTab = false;
				this.crePosAprobado = itemSel;
				this.cedulaCliente = crePosAprobado.getCedula();
				this.cargarArchivosPorSolicitud();
				this.obtenerCliente();
				this.consultarCuposCliente();
				if (this.listaArchivos == null) {
					info("No existen archivos registrados para esta solcitud");
				} else {
					this.listaArchivosDigitales = this.listaArchivos;
				}
				cargarArchivosRevisionPorSolicitud();
				if (this.listaArchivosRev == null) {
					info("No existen archivos de revision registrados para esta solcitud");
				} else {
					this.listaArchivosDigitalesRevision = this.listaArchivosRev;
				}
			} else {
				error("Se debe seleccionar una solicitud");
			}
		} catch (Exception e) {
			error(" No se pudo cargar registro:" + e.getMessage());
		}
	}

	public void guardarSolicitud() {
		try {
			if (this.crePosAprobado.getNoSolicitud() != null) {
				// guardarParametrosCredito();
				creditoPostventaService.update(crePosAprobado);
				super.info("La solicitud ha sido actualizada");

			}

		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar movimiento", e);
		}
	}

	public void guardarSolicitudAprobada() {
		boolean existeObs = crePosAprobado.getObservacionEstAprobado() != null
				&& !crePosAprobado.getObservacionEstAprobado().equals("");
		try {
			if (!existeObs) {
				warn("Debe registrar una observacion para APROBAR la Solicitud");
				return;
			} else if (this.crePosAprobado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo()) && this.crePosAprobado.getNoSolicitud() != null) {
				this.crePosAprobado.setEstado(EnumEstSolicitud.APROBADO.getCodigo());
				this.crePosAprobado.setUsuarioAprueba(usuarioSesion);
				this.crePosAprobado.setFechaEstAprobado(new Date());
				creditoPostventaService.update(crePosAprobado);
				this.actBtnAprobar = true;
				guardarParametrosCredito();
				envioMailSolicitudAprobada();
				super.info("La solicitud ha sido aprobada");

				// IMPLEMENTACION NPS REQ-37739
				// REGISTRO DE PROCESO NPS, LLAMADO AL MICROSERVICIO Y ENVIO DE ENCUESTA NPS
				if (EnumEstSolicitud.APROBADO.getCodigo().equals(this.crePosAprobado.getEstado()) && ("01".equals(getCompania().getNoCia())
						|| "TY".equals(getCompania().getNoCia()) || "T1".equals(getCompania().getNoCia())
						|| "08".equals(getCompania().getNoCia()) || "10".equals(getCompania().getNoCia()))) {
					try {
						String url = s3sPropertiesService.getProperty(APP_NPS_BACKEND_API) + "createTareaNps";
						String token = "pendiente";
						NpsIntegradoRest proceso = new NpsIntegradoImpl();
						BodyInDatosNps item = parcearDatos(crePosAprobado, crePosAprobado.getUsuarioEnvia(),
								crePosAprobado.getNoCia());
						OutputDatosNps resp = proceso.enviarTareaReq(item, url, token);
						Long idNps = 0l;
						String emailCliente = "";
						String nombreCliente1 = "";
						String emailAsesor = "";
						String nombreAsesor = "";
						String departamento = "";
						String usuarioAsesor = "";
						String usuarioCliente = "";
						String tituloTarea = "";
						try {
							ObjectMapper objectMapper = new ObjectMapper();
							JsonNode jsonNode = objectMapper.readTree(resp.getCode());
							logger.info(jsonNode);
							idNps = jsonNode.get("idNps").longValue();
							emailCliente = jsonNode.get("emailCliente").textValue();
							nombreCliente1 = jsonNode.get("nombresCliente").textValue();
							emailAsesor = jsonNode.get("emailAsesor").textValue();
							nombreAsesor = jsonNode.get("nombreAsesor").textValue();
							departamento = jsonNode.get("departamento").textValue();
							usuarioAsesor = jsonNode.get("usuarioAsesor").textValue();
							usuarioCliente = jsonNode.get("usuarioCliente").textValue();
							tituloTarea = jsonNode.get("tituloTarea").textValue();
							logger.info("idNps: " + idNps);
						} catch (Exception err) {
							logger.error("Exception : " + err.toString());
						}
						if (!"".equals(emailAsesor) && !"".equals(emailCliente)) {
							this.enviaMailNps(emailAsesor, emailCliente, nombreAsesor, nombreCliente1,
									"Califique nuestro servicio finalizado por: " + nombreAsesor, idNps.toString(),
									tituloTarea);
						}
						logger.info(resp.getMessage());
					} catch (WServiceException e) {
						logger.error("WServiceException, Envio datos tarea req a microservice ", e);
					}
				}
				// TERMINA NPS
				getUrlRegresa();
			}
		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar movimiento", e);
		}
	}

	public void resetearFormulario() {
		this.crePosAprobado = new CxcCreditoPostventa();
		this.cliente = new Cliente();
		this.crePosAprobadoSel = null;
		this.idCiudadGarante = null;
		this.idTipoVivienda = null;
		this.idCiudadTrabajo = null;
	}

	private void validarSolicitudSeleccionada() {
		String noSolicitud = super.getRequestParameter("noSolicitud");
		if (noSolicitud != null) {

			this.crePosAprobadoSel = consultarSolicitudAprobacion(getCompania().getNoCia(),
					Long.parseLong(super.getRequestParameter("noSolicitud")));
			verificarAprobacionSolicitud();
			obtenerTodosCantones();
			obtenerCantonesGarante();
			cargarTipoCtasBancarias();
			cargarTipoVivienda();
			cargarEstadoCivilList();
			cargarListaBancos();
			obtenerTodosCantones();
			obtenerCantonesGarante();
			// NUEVO REQUERIMIENTO
			obtenerClasificacionClientes();
			cargarTipoClientes();
			cargarTipoBloqueoCLiente();
			this.listaArchivosDigitales = new ArrayList<>();
			this.listaArchivos = new ArrayList<>();
			this.listaArchivosRev = new ArrayList<>();
			this.usuarioSesion = getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario();
		}

	}

	private CxcCreditoPostventa consultarSolicitudAprobacion(String noCia, Long noSolicitud) {
		edit = true;
		try {
			crePosAprobado = creditoPostventaService.buscarSolicitudRevision(noCia, noSolicitud);
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion sobre solicitudes de credito");
		}
		return crePosAprobado;
	}

	public void getUrlRegresa() {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/listAprobacion.jsf?regresa=true";
			redirect(url);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void getUrlConsultaAprobado(CxcCreditoPostventa item) {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/formAprobado.jsf?noSolicitud="
					+ item.getNoSolicitud();
			redirect(url);
			crePosAprobadoSel = item;
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void verArchivo() throws IOException {
		try {
			FileUpload fileUpload = new FileUpload();
			if (null != urlArchivo) {
				fileUpload.seeFile(FacesContext.getCurrentInstance(), urlArchivo);
			} else {
				addErrorMessage("No se ha subido ningún archivo al servidor", "");
			}
		} catch (Exception e) {
			addErrorMessage("No se encontrado el archivo en el servidor", "");
			logger.error(e);
		}
	}

	public void cargaListaSolicitudes() {
		this.noCia = getCompania().getNoCia();
		cargarAgencias();
		this.estado = new ArrayList<>();
		this.estado.add(EnumEstSolicitud.REVISADO.getCodigo());
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.codAgencia, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			addInfoMessage("No se han encontrado soicitudes de credito postventa para esta empresa", "");
		}
	}

	public void cargarArchivosPorSolicitud() {
		try {
			if (this.crePosAprobado != null) {
				this.listaArchivos = archivoCrePostventaService.obtenerArchivosPorSolicitud(crePosAprobado.getNoCia(),
						crePosAprobado.getNoSolicitud());
			}
		} catch (Exception e) {
			addInfoMessage("No se han encontrado archivos digitales para la solicitud", "");
		}
	}

	public void negarSolicitud() {
		boolean existeObs = crePosAprobado.getObservacionEstAprobado() != null
				&& !crePosAprobado.getObservacionEstAprobado().equals("");
		try {
			if (!existeObs) {
				warn("Debe registrar una observacion para NEGAR la Solicitud");
				return;
			} else if (this.crePosAprobado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo()) && this.crePosAprobado.getNoSolicitud() != null) {
				this.crePosAprobado.setEstado(EnumEstSolicitud.NEGADO.getCodigo());
				this.crePosAprobado.setUsuarioAprueba(usuarioSesion);
				crePosAprobado.setFechaEstAprobado(new Date());
				creditoPostventaService.update(crePosAprobado);
				this.actBtnAprobar = true;
				envioMailSolictudNegada();
				super.info("La Solicitud Postventa ha sido NEGADA");
				getUrlRegresa();
			}
		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar la solicitud express", e);
		}
	}

	public void ingresarObservacionRev() {
		if (this.crePosAprobado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo())) {
			this.activaObservacion = false;
			this.btnNegar = true;
		}
	}

	public void cerrarDialogo() {
		if (this.crePosAprobado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo())) {
			this.activaObservacion = true;
			this.btnNegar = false;
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('diaAprobFlujo').hide();");
		}
	}

	public void calcularTotalDisponible() {
		totalGastos = 0.00;
		Double ingreso = (crePosAprobado.getIngreso() == null ? 0.00 : crePosAprobado.getIngreso());
		Double egreso = (crePosAprobado.getEgreso() == null ? 0.00 : crePosAprobado.getEgreso());

		if (egreso <= ingreso) {
			totalGastos = ingreso - egreso;
			crePosAprobado.setMontoDisponible(totalGastos);
		} else {
			addErrorMessage("Los egresos no pueden ser mayores que los ingresos", "");
		}
	}

	protected String getStrReportPath() {
		urlReport = "/reportes/cxcSolicitudCreditoPostventaExpress.jasper";
		return urlReport;
	}

	private void setParameters(Map<String, Object> parameters) {
		// parameters.put("NOMBRE_CIA", getCompania().getNombre());
		parameters.put("NO_SOLICITUD", new Long(crePosAprobado.getNoSolicitud()));
		parameters.put("NO_CIA", getCompania().getNoCia());
	}

	@SuppressWarnings("unchecked")
	public void reportPdf() {
		Map parameters = new HashMap();
		setParameters(parameters);
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
			String ctxPath = getServletContext().getRealPath("/");
			String path = getStrReportPath();
			connection = utilServiceDelegate.getDataSource().getConnection();
			String path2 = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path2);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + path, parameters, connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			response.setContentType("application/pdf");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
			virtualizer.cleanup();
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
	}

	public void regresaListadoSolicitudes() {
		noCia = getCompania().getNoCia();
		cargarAgencias();
		this.estado = new ArrayList<>();
		this.estado.add(EnumEstSolicitud.REVISADO.getCodigo());
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.codAgencia, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			addInfoMessage("No se han encontrado solicitudes para esta empresa", "");
		}
	}

	public void verificarAprobacionSolicitud() {
		if (this.crePosAprobado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo())) {
			this.actBtnAprobar = false;
		} else {
			this.actBtnAprobar = true;
		}
	}

	public void deleteFile() throws DeleteException, Exception {

		CxcCreDigPostventa regEliminar = getArchivoSel();
		if (this.crePosAprobado.getNoSolicitud() == null) {
			this.listaArchivosDigitales.remove(regEliminar);
		} else {
			archivoCrePostventaService.delete(regEliminar.getNoRegistro());
		}
		cargarArchivosPorSolicitud();
	}

	public void deleteFileRevision() throws DeleteException, Exception {

		CxcCreDigPostventa regEliminar = getArchivoSel();
		if (this.crePosAprobado.getNoSolicitud() == null) {
			this.listaArchivosDigitalesRevision.remove(regEliminar);
		} else {
			archivoCrePostventaService.delete(regEliminar.getNoRegistro());
		}
		cargarArchivosRevisionPorSolicitud();
	}

	public void guardarParametrosCredito() {
		try {
			this.cliente = clienteServiceLocal.findByCedulaNoCia(cedulaCliente, getCompania().getNoCia());
			if (this.cliente.getClientePK().getNoCliente() != null) {
				this.cliente.setLimiteCredi(crePosAprobado.getLimiteCredi());
				this.cliente.setLimiteFact(crePosAprobado.getLimiteFact());
				this.cliente.setPlazo(crePosAprobado.getPlazo());
				this.cliente.setPromo(crePosAprobado.getPromo());
				this.cliente.setTasaInt(crePosAprobado.getTasaInt());
				this.cliente.setPagareCupo(crePosAprobado.getPagareCupo());
				this.cliente.setDiasGracia(crePosAprobado.getDiasGracia());
				this.cliente.setMotivo(crePosAprobado.getMotivo().toUpperCase());
				this.cliente.setClaseVehiculos(crePosAprobado.getClaseVehiculos().toUpperCase());
				this.cliente.setClaseRepuestos(crePosAprobado.getClaseRepuestos().toUpperCase());
				this.cliente.setClaseServicio(crePosAprobado.getClaseServicio().toUpperCase());
				this.cliente.setSegCensal(crePosAprobado.getSegCensal());
				clienteServiceLocal.update(cliente);
				super.info("Los parametros de credito han sido almacenados en el SCB");
			}
		} catch (UpdateException | FindException e) {
			super.error("Error al guardar los parametros de credito en el cliente");
			logger.error("Error al guardar los parametros de credito desde Solicitud Postventa Express", e);
		}
	}

	public void buscarCliente() {
		boolean existe = false;

		if (cedulaCliente != null) {
			if (!cedulaCliente.trim().equals("")) {
				existe = true;
			}
		}

		if (existe) {
			try {
				this.nuevo = true;
				this.cliente = clienteServiceLocal.findByCedulaNoCia(cedulaCliente, getCompania().getNoCia());

				if (this.cliente != null) {
					this.nombreCliente = cliente.getNombre();
					this.domicilioCliente = cliente.getDomicilioCalle();
					this.refDomicilioCliente = cliente.getDomicilioReferencia();
					this.estCivilCliente = cliente.getEstadoCivil();
					this.telefonoCliente = cliente.getDomicilioTelefono();
					this.celularCliente = cliente.getTelefono();
					this.correoCliente = cliente.getEmail1();
					this.noCuentaCliente = cliente.getNoCtaCliente();
					this.bancoCliente = cliente.getBanco();
					this.tipoCuentaCliente = cliente.getTipoCuenta();
				} else {
					addInfoMessage("Info. ",
							" No se encontro la cedula, por favor proceda a registrar el nombre del cliente");
					this.nuevo = false;
					this.nombreCliente = null;
					this.domicilioCliente = null;
					this.refDomicilioCliente = null;
					this.bancoCliente = null;
					this.noCuentaCliente = null;
					this.estCivilCliente = null;
					this.correoCliente = null;
					this.telefonoCliente = null;
					this.celularCliente = null;
					this.tipoCuentaCliente = null;
					this.cliente = new Cliente();
					this.crePosAprobado = new CxcCreditoPostventa();
				}
			} catch (FindException e) {
				addErrorMessage(e.getSummary(), e.getDetail());
			}
		} else {
			addErrorMessage("", "Por favor ingrese la cedula o RUC del cliente");
		}
	}

	public void cargarArchivosRevisionPorSolicitud() {
		String tipo = "R";
		try {
			if (this.crePosAprobado != null) {
				this.listaArchivosRev = archivoCrePostventaService.obtenerArchivosPorSolicitudRevisada(
						crePosAprobado.getNoCia(), crePosAprobado.getNoSolicitud(), tipo);
			}
		} catch (Exception e) {
			addInfoMessage("No se han encontrado archivos digitales de revision para la solicitud", "");
		}
	}

	public List<SelectItem> getClaseClienteList() {
		List<SelectItem> claseList = new ArrayList<SelectItem>();
		List<CxcClaseCliente> clases = servicioClaseCliente.findClaseDeClientesByNoCia(getCompania().getNoCia());

		claseList.add(new SelectItem("", ""));
		for (CxcClaseCliente element : clases) {
			claseList.add(new SelectItem(element.getCxcClaseClientePK().getClase(), element.getDescripcion()));
		}

		return claseList;
	}

	public List<CxcClaseCliente> obtenerClasificacionClientes() {
		try {
			return claseClienteList = servicioClaseCliente.findClaseDeClientesByNoCia(getCompania().getNoCia());
		} catch (Exception e) {
			error("No se puede cargar la clasificacion de clientes");
		}
		return claseClienteList;
	}

	public void cargarTipoClientes() {
		try {
			this.listaTiposCliente = paramDetServiceLocal.findTiposClienteByNoCia(getCompania().getNoCia());
			if (this.listaTiposCliente.size() > 0) {
				for (ParamDet item : this.listaTiposCliente) {
					this.listaCmbTiposCliente
							.add(new SelectItem(item.getParamDetPK().getCodigoDet(), item.getDescripcion()));
				}
			}
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion sobre tipos de clientes");
		}
	}

	public void verificarControles() {
		if (this.crePosAprobado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo())) {
			this.activaObservacion = true;
			this.btnNegar = false;
			this.crePosAprobado.setObserEstDevuelto(VALOR_NULO);
		}
	}

	public void cargarTipoBloqueoCLiente() {
		try {
			this.listaTipoBloqueo = paramDetServiceLocal.consultarPorCodigoCabecera(this.getCompania().getNoCia(),
					BLOQUEO_CLIENTES);
			if (this.listaTipoBloqueo.size() > 0) {
				for (ParamDet item : this.listaTipoBloqueo) {
					this.listaCmbTipoBloqueo
							.add(new SelectItem(item.getParamDetPK().getCodigoDet(), item.getDescripcion()));
				}
			}
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion sobre tipos de bloqueo de clientes");
		}
	}

	// IMPLEMENTACION NPS REQ-37739
	private BodyInDatosNps parcearDatos(CxcCreditoPostventa crePosAprobado, String usuarioCliente,
			String noCiaCliente) {
		if (crePosAprobado != null) {
			BodyInDatosNps datosEntrada = new BodyInDatosNps();
			// Datos Tarea
			datosEntrada.setCodigoTareq(String.valueOf(crePosAprobado.getNoSolicitud()));
			datosEntrada.setTituloTarea("Análisis Crediticio Solicitud Express " + crePosAprobado.getNoSolicitud());
			if (crePosAprobado.getObservacionEstAprobado().length() > MAX_LENGTH ) {
				datosEntrada.setDescripcionTarea(crePosAprobado.getObservacionEstAprobado().substring(0, MAX_LENGTH));
			} else {
				datosEntrada.setDescripcionTarea(crePosAprobado.getObservacionEstAprobado());
			}
			datosEntrada.setTipoProceso("CSE"); // Tarea CREDITO SOLICITUD EXPRESS
//			datosEntrada.setFechaRespuesta(FechaUtils.formatearFecha(new Date(), "dd/MM/yyyy HH:mm"));
			datosEntrada.setFechaAtencion(crePosAprobado.getFechaEstAprobado() != null
					? FechaUtils.formatearFecha(crePosAprobado.getFechaEstAprobado(), "dd/MM/yyyy HH:mm")
					: FechaUtils.formatearFecha(new Date(), "dd/MM/yyyy HH:mm"));

			// Datos Cliente / Solicita
			datosEntrada.setUsuarioCliente(usuarioCliente);
			NomEmpleados empleadoSol = new NomEmpleados();
			UsuarioSis client = null;
			NomEmpleados empleadosl = null;
			try {
				Optional<SisUsuariosBitacora> usuarioBitacora = sisUsuariosBitacoraService
						.findUserByCiaUser(noCiaCliente, usuarioCliente);
				client = usuarioSisServiceLocal.getUsuarioByNociaUser(noCiaCliente, usuarioCliente);
				if (usuarioBitacora.isPresent()) {
					empleadoSol.setNomEmpleadosPK(new NomEmpleadosPK());
					empleadoSol.getNomEmpleadosPK().setNoCia(usuarioBitacora.get().getId().getNoCia());
					empleadoSol.setNombre(usuarioBitacora.get().getNombre());
					empleadoSol.setMail(usuarioBitacora.get().getEmail());
					empleadoSol.setUsuarioDb(usuarioBitacora.get().getId().getUsuario());
					datosEntrada.setNoCia(empleadoSol.getNomEmpleadosPK().getNoCia());
					datosEntrada.setNombresCliente(empleadoSol.getNombre());
					datosEntrada.setEmailCliente(empleadoSol.getMail());
					if (client != null && client.getNoEmple() != null) {
						empleadosl = nominaEmpleadosService.getEmpleado(noCiaCliente, client.getNoEmple());
					}
				} else {
					if (client != null && client.getNoEmple() != null) {
						empleadosl = nominaEmpleadosService.getEmpleado(noCiaCliente, client.getNoEmple());
						empleadoSol.setNomEmpleadosPK(new NomEmpleadosPK());
						empleadoSol.getNomEmpleadosPK().setNoCia(noCiaCliente);
						empleadoSol.setNombre(client.getNombre());
						empleadoSol.setMail(client.getEmail());
						empleadoSol.setUsuarioDb(client.getUsuario());
						datosEntrada.setNoCia(empleadoSol.getNomEmpleadosPK().getNoCia());
						datosEntrada.setNombresCliente(empleadoSol.getNombre());
						datosEntrada.setEmailCliente(empleadoSol.getMail());
					}
				}
				if (empleadosl != null) {
					datosEntrada.setCedulaCliente(empleadosl.getCedula());
					datosEntrada.setCelularCliente(empleadosl.getCelular());
					datosEntrada.setTelefonoCliente(empleadosl.getTelefono());
					NomAreaPK nomEmpPk = new NomAreaPK();
					nomEmpPk.setArea(empleadosl.getArea());
					nomEmpPk.setNoCia(empleadosl.getNomEmpleadosPK().getNoCia());
					String empresa = nomAreaServicioLocal.findByPK(nomEmpPk).getDescri();
					NomDepartamentoPK depPk = new NomDepartamentoPK(empleadosl.getNomEmpleadosPK().getNoCia(),
							empleadosl.getArea(), empleadosl.getDepto());
					datosEntrada.setEmpresa(empresa);
					datosEntrada.setArea(nomDepartamentoServicioLocal.findByPK(depPk).getDescri());
					NomSeccionPK secPk = new NomSeccionPK();
					secPk.setArea(empleadosl.getArea());
					secPk.setDepa(empleadosl.getDepto());
					secPk.setDivision(empleadosl.getDivision());
					secPk.setNoCia(empleadosl.getNomEmpleadosPK().getNoCia());
					secPk.setSeccion(empleadosl.getSeccion());
					datosEntrada.setAgencia(nomSeccionServicioLocal.findByPK(secPk).getDescri());
					NomDivisionPK divPk = new NomDivisionPK(empleadosl.getNomEmpleadosPK().getNoCia(),
							empleadosl.getArea(), empleadosl.getDepto(), empleadosl.getDivision());
					datosEntrada.setDepartamento(nomDivisionServicioLocal.findByPK(divPk).getDescri());

					// Agregar correctamente la ciudad o canton
					try {
						NomSeccion nomSecc = nomSeccionServicioLocal.findByPK(secPk);
						AgenciaPK pkagencia = new AgenciaPK(nomSecc.getCodigoAgencia(),
								empleadosl.getNomEmpleadosPK().getNoCia());
						Agencia agencia = agenciaService.getAgenciaByPK(pkagencia);
						CantonPK cantpk = new CantonPK(agencia.getCiudad(), agencia.getProvincia(), "01");
						Canton cant = cantonService.getCantonByPK(cantpk);
						datosEntrada.setCiudad(cant.getDescripcion());
					} catch (Exception ex) {
						logger.error("Error ciudad: " + ex.getMessage());
					}
				}
			} catch (FindException e) {
				logger.error("Error cliente" + e.getMessage());
			}

			// Datos Asesor / Atiende
			datosEntrada.setUsuarioAsesor(usuarioSesion);
			NomEmpleados asesor = new NomEmpleados();
			UsuarioSis usuario = null;
			NomEmpleados asesorAux = null;
			List<NomEmpleados> usSol = null;
			try {
				usSol = nominaEmpleadosService.findByEmpleadoActivo(usuarioSesion);
			} catch (FindException e) {
				logger.error("Error asesor activo: " + e.getMessage());
			}
			usuario = usuarioSisServiceLocal.getUsuarioByNociaUser(getCompania().getNoCia(), usuarioSesion);
			if (usSol != null && !usSol.isEmpty()) {
				asesor = usSol.get(0);
				Optional<SisUsuariosBitacora> usuarioBitacora = sisUsuariosBitacoraService
						.findUserByCiaUser(asesor.getNomEmpleadosPK().getNoCia(), usuarioSesion);
				if (usuarioBitacora.isPresent()) {
					asesor.setNomEmpleadosPK(new NomEmpleadosPK());
					asesor.getNomEmpleadosPK().setNoCia(usuarioBitacora.get().getId().getNoCia());
					asesor.setNombre(usuarioBitacora.get().getNombre());
					asesor.setMail(usuarioBitacora.get().getEmail());
					asesor.setUsuarioDb(usuarioBitacora.get().getId().getUsuario());
					datosEntrada.setEmailAsesor(asesor.getMail());
					datosEntrada.setNombreAsesor(asesor.getNombre());
					datosEntrada.setCedulaAsesor(asesor.getCedula());
					// Departamento y Nocia del Asesor
					NomDepartamentoPK depPk = new NomDepartamentoPK(asesor.getNomEmpleadosPK().getNoCia(),
							asesor.getArea(), asesor.getDepto());
					NomSeccionPK secPk = new NomSeccionPK();
					secPk.setArea(asesor.getArea());
					secPk.setDepa(asesor.getDepto());
					secPk.setDivision(asesor.getDivision());
					secPk.setNoCia(asesor.getNomEmpleadosPK().getNoCia());
					secPk.setSeccion(asesor.getSeccion());
					// Nuevos campos
					NomDivisionPK divPk = new NomDivisionPK(asesor.getNomEmpleadosPK().getNoCia(), asesor.getArea(),
							asesor.getDepto(), asesor.getDivision());
					try {
						datosEntrada.setDepartamentoAsesor(nomDivisionServicioLocal.findByPK(divPk).getDescri());
					} catch (FindException e) {
						logger.error("Error departamento asesor activo: " + e.getMessage());
					}
					datosEntrada.setNociaAsesor(asesor.getNomEmpleadosPK().getNoCia());

				}
			} else {
				Optional<SisUsuariosBitacora> usuarioBitacora = sisUsuariosBitacoraService
						.findByUserName(usuarioSesion);
				if (usuarioBitacora.isPresent()) {
					asesor.setNomEmpleadosPK(new NomEmpleadosPK());
					asesor.getNomEmpleadosPK().setNoCia(usuarioBitacora.get().getId().getNoCia());
					asesor.setNombre(usuarioBitacora.get().getNombre());
					asesor.setMail(usuarioBitacora.get().getEmail());
					asesor.setUsuarioDb(usuarioBitacora.get().getId().getUsuario());
					datosEntrada.setEmailAsesor(asesor.getMail());
					datosEntrada.setNombreAsesor(asesor.getNombre());
					datosEntrada.setCedulaAsesor(asesor.getCedula());
					datosEntrada.setDepartamentoAsesor("CREDITO Y COBRANZAS");
					datosEntrada.setNociaAsesor("04");
				}
			}
			datosEntrada.setEstado("ENVIADO");
			return datosEntrada;
		}
		return null;
	}

	/***
	 * Metodo para envio de correos NPS
	 * 
	 * @param from
	 * @param to
	 */
	// IMPLEMENTACION NPS REQ-37739
	private void enviaMailNps(String from, String to, String fromName, String toName, String subject, String encuesta,
			String tituloTarea) {
		StringBuffer message = new StringBuffer();
		PlantillaMail plantilla = new PlantillaMail();
		plantilla = plantillaMailServiceLocal.obtenerPlantilla(NPS_MAIL);
		try {
			String mensaje = plantilla.getValor().replace("xxx", toName).replace("ooo", tituloTarea)
					.replace("yyy", fromName)
					.replace("zzz", s3sPropertiesService.getProperty(APP_NPS_FRONTEND_URL) + encuesta.toString())
					.replace("Nota de solucion:", "").replace("No_Aplica", "");
			message.append(mensaje);

			List<String> pathImgs = new ArrayList<String>();
			String imgCab=RUTA_IMAGEN_C+EXT_IMAGEN; 
			String imgDet=RUTA_IMAGEN_D+EXT_IMAGEN;
			if(CommonConstants.TOYOCOSTAS.equals(getCompania().getNoCia())){
				imgCab=RUTA_IMAGEN_C+getCompania().getNoCia()+EXT_IMAGEN; 
				imgDet=RUTA_IMAGEN_D+getCompania().getNoCia()+EXT_IMAGEN;
			}
			pathImgs.add(getServletContext().getRealPath(imgCab));
			pathImgs.add(getServletContext().getRealPath(imgDet));

			mailService.sendEmailPlantillaHTML(getSisMailServidores(getCompania().getNoCia()), from, fromName, to,
					toName, subject, message, null, false, pathImgs, null);

		} catch (GeneralException e) {
			System.out.println(e.getMessage());
		}
	}

	private void cargarAgencias() {
		try {
			String[] order = { "nombre" };
			this.listaAgencias = agenciaService.findByCompania(getCompania().getNoCia(), order);
			if (this.listaAgencias.isEmpty()) {
				error("No se puede cargar las agencias de la empresa");
				listaAgencias.clear();
			}
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion de las agencias de las empresas");
		}
	}

	// REQ ARCHIVO ADJUNTO

	protected String getStrConvenioPath() {
		urlReport = "/reportes/cxcConvenioCreditoComercialEmpresas.jasper";
		return urlReport;
	}

	private void setParametersConvenio(Map<String, Object> parameters) {
		parameters.put("NO_SOLICITUD", new Long(crePosAprobado.getNoSolicitud()));
		parameters.put("NO_CIA", getCompania().getNoCia());
		parameters.put("P_LOGO", obtenerLogoEmpresa());
	}

	private String obtenerLogoEmpresa() {
		String rutaLogoEmpresa = null;
		if (CommonConstants.CASABACA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoCasabaca.jpg");
		} else if (CommonConstants.TOYOCOSTAS.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoToyocosta.jpg");
		}
		return rutaLogoEmpresa;
	}

	/**
	 * Recupera el reporte en byte[] pdf el reporte de jasper
	 * 
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

	public void consultarCuposCliente() {
		this.cupoDisponible = BigDecimal.ZERO;
		this.monto = BigDecimal.ZERO;
		this.creditoSolicitado = BigDecimal.ZERO;
		this.nuevoCupoDisponible = BigDecimal.ZERO;
		try {
			listCotiByCliente = cotizacionRepuestosServices.findByClienteAndEstadoPediente(getCompania().getNoCia(),
					this.cliente.getCedula(), null);

			if (!listCotiByCliente.isEmpty()) {
				for (CotizacionRepuesto coti : listCotiByCliente) {
					this.comentario = coti.getComentarioAprob();
					this.sobregiro = coti.getSobregiros();
//				this.garancheck = coti.getGarancheck();
				}
			}
			monto = arccmdService.obtenerMonto(cliente.getClientePK().getNoCia(),
					cliente.getClientePK().getNoCliente().toString());
			if (monto != null) {

				cupoDisponible = new BigDecimal((double) this.cliente.getLimiteCredi()).subtract(monto);
				creditoSolicitado = (crePosAprobado.getCupoSugerido());
				nuevoCupoDisponible = cupoDisponible.subtract(creditoSolicitado);
			} else {
				cupoDisponible = new BigDecimal((double) this.cliente.getLimiteCredi());
				creditoSolicitado = (crePosAprobado.getCupoSugerido());
				nuevoCupoDisponible = cupoDisponible.subtract(creditoSolicitado);
			}
			this.saldoVencido = creditoPostventaService.obtenerSaldosVencidosCliente(getCompania().getNoCia(),
					crePosAprobado.getCedula());
		} catch (Exception e) {
			logger.error("No se pudo consultar cupos de cliente", e.getCause());
		}

	}

	public void obtenerCliente() {
		try {
			Cliente cliente = clienteServiceLocal.findByCedulaNoCia(this.crePosAprobado.getCedula(),
					getCompania().getNoCia());

			if (cliente != null) {
				setCliente(cliente);
				this.crePosAprobado.setCedula(cliente.getCedula());
				this.crePosAprobado.setNombres(cliente.getNombre());
			}
		} catch (Exception e) {
			nombreCliente = null;
			setCliente(null);
			this.crePosAprobado.setCedula(cliente.getCedula());
			this.crePosAprobado.setNombres(cliente.getNombre());
			addWarnMessage("Alerta", "No existen datos del cliente ingresado");
		}
	}
	
	public void regresarEstadoEnviado() {
		boolean existeObs = crePosAprobado.getObserEstDevuelto() != null
				&& !crePosAprobado.getObserEstDevuelto().equals("");
		try {
			if (!existeObs) {
				warn("Debe registrar una observacion para Devolver la Solicitud");
				return;
			} else if (this.crePosAprobado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo()) && this.crePosAprobado.getNoSolicitud() != null) {
				this.crePosAprobado.setEstado(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo());
				crePosAprobado.setFechaEstRevisado(new Date());
				creditoPostventaService.update(crePosAprobado);
				this.actBtnAprobar = true;
				envioMailRegresaEstado();
				super.info("La Solicitud ha sido regresada a estado DEVUELTO para revisión de observaciones");
				getUrlRegresa();
			}
		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar la solicitud express", e);
		}
	}
	
	public void envioMailRegresaEstado() throws UpdateException {
		StringBuffer mensaje = new StringBuffer(" La Solicitud de Credito Express, se DEVUELVE para REVISIÓN con los siguientes datos:<br><br><table border='1'><tr><td><b>Numero Solicitud</b></td><td>")
		        .append(crePosAprobado.getNoSolicitud()).append("</td></tr><tr><td><b>Fecha Revisión </b></td> <td>")
		        .append(new SimpleDateFormat("dd/MM/yyyy").format(crePosAprobado.getFechaEstRevisado())).append("</td></tr><tr><td><b>Estado</b></td> <td>DEVUELTO REVISION</td></tr><tr><td><b>Cédula Cliente</b></td> <td>")
		        .append(crePosAprobado.getCedula()).append("</td></tr><tr><td><b>Nombre Cliente</b></td> <td>")
		        .append(crePosAprobado.getNombres()).append("</td></tr><tr><td><b>Observación Revisión</b></td> <td>")
		        .append(crePosAprobado.getObserEstDevuelto()).append("</td></tr></table><br><br>Por favor revisar la solicitud de Credito Postventa Express en el sistema S3S.<br><br>");

		try {
			UsuarioSis solicitante = usuarioSisServiceLocal
					.getUsuarioSisByUsername(this.crePosAprobado.getUsuarioRevisa()); // confirmación
			SisMailServidores sisMailServidores = getSisMailServidores(crePosAprobado.getNoCia());
			if (EnumEstSolicitud.DEVUELTO_REVISION.getCodigo().equals(crePosAprobado.getEstado())) {
				mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(), solicitante.getEmail(),
						"NOTIFICACION DE SOLICITUD EXPRESS DEVUELTA PARA REVISION CON NUMERO: " + crePosAprobado.getNoSolicitud(),
						mensaje);
			}
			mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(), getUsuario().getEmail(),
					"NOTIFICACION DE SOLICITUD EXPRESS DEVUELTA PARA REVISION CON NUMERO: " + crePosAprobado.getNoSolicitud(),
					mensaje);
			super.info("La notificación ha sido enviada por correo eletrónico");
		} catch (GeneralException e) {
			logger.error(e.getMessage(), e.getCause());
			error("La notificación no pudo ser enviada por correo eletrónico");
		} catch (FindException e) {
			logger.error(e.getMessage(), e.getCause());
		}
	}

	/**
	 * GETTERS & SETTERS
	 * 
	 * @return
	 */
	public DigDocumentoServiceLocal getDigDocumentoService() {
		return digDocumentoService;
	}

	public void setDigDocumentoService(DigDocumentoServiceLocal digDocumentoService) {
		this.digDocumentoService = digDocumentoService;
	}

	public List<CxcCreditoPostventa> getListaCreditos() {
		return listaCreditos;
	}

	public void setListaCreditos(List<CxcCreditoPostventa> listaCreditos) {
		this.listaCreditos = listaCreditos;
	}

	public CxcCreditoPostventa getCrePosAprobado() {
		return crePosAprobado;
	}

	public void setCrePosAprobado(CxcCreditoPostventa crePosAprobado) {
		this.crePosAprobado = crePosAprobado;
	}

	public CxcCreditoPostventa getCrePosAprobadoSel() {
		return crePosAprobadoSel;
	}

	public void setCrePosAprobadoSel(CxcCreditoPostventa crePosAprobadoSel) {
		this.crePosAprobadoSel = crePosAprobadoSel;
	}

	public List<CxcCreDigPostventa> getListaArchivosDigitales() {
		return listaArchivosDigitales;
	}

	public void setListaArchivosDigitales(List<CxcCreDigPostventa> listaArchivosDigitales) {
		this.listaArchivosDigitales = listaArchivosDigitales;
	}

	public String getIdCiudadGarante() {
		return idCiudadGarante;
	}

	public void setIdCiudadGarante(String idCiudadGarante) {
		this.idCiudadGarante = idCiudadGarante;
	}

	public String getIdCiudadTrabajo() {
		return idCiudadTrabajo;
	}

	public void setIdCiudadTrabajo(String idCiudadTrabajo) {
		this.idCiudadTrabajo = idCiudadTrabajo;
	}

	public String getIdTipoVivienda() {
		return idTipoVivienda;
	}

	public void setIdTipoVivienda(String idTipoVivienda) {
		this.idTipoVivienda = idTipoVivienda;
	}

	public String getIdBanco() {
		return idBanco;
	}

	public void setIdBanco(String idBanco) {
		this.idBanco = idBanco;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public String getUsuarioSesion() {
		return usuarioSesion;
	}

	public void setUsuarioSesion(String usuarioSesion) {
		this.usuarioSesion = usuarioSesion;
	}

	public String getUsuarioModulo() {
		return usuarioModulo;
	}

	public void setUsuarioModulo(String usuarioModulo) {
		this.usuarioModulo = usuarioModulo;
	}

	public String getCedulaCliente() {
		return cedulaCliente;
	}

	public void setCedulaCliente(String cedulaCliente) {
		this.cedulaCliente = cedulaCliente;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public LazyDataModel<Cliente> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Cliente> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public Object[] getArchivo() {
		return archivo;
	}

	public void setArchivo(Object[] archivo) {
		this.archivo = archivo;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public List<Cliente> getListarClientes() {
		return listarClientes;
	}

	public void setListarClientes(List<Cliente> listarClientes) {
		this.listarClientes = listarClientes;
	}

	public List<ParamDet> getListaTiposVivienda() {
		return listaTiposVivienda;
	}

	public void setListaTiposVivienda(List<ParamDet> listaTiposVivienda) {
		this.listaTiposVivienda = listaTiposVivienda;
	}

	public List<SelectItem> getListaCmbTipoVivienda() {
		return listaCmbTipoVivienda;
	}

	public void setListaCmbTipoVivienda(List<SelectItem> listaCmbTipoVivienda) {
		this.listaCmbTipoVivienda = listaCmbTipoVivienda;
	}

	public List<Canton> getCantonesList() {
		return cantonesList;
	}

	public void setCantonesList(List<Canton> cantonesList) {
		this.cantonesList = cantonesList;
	}

	public List<Canton> getCantonesListGarante() {
		return cantonesListGarante;
	}

	public void setCantonesListGarante(List<Canton> cantonesListGarante) {
		this.cantonesListGarante = cantonesListGarante;
	}

	public List<SelectItem> getEstadoCivilList() {
		return estadoCivilList;
	}

	public void setEstadoCivilList(List<SelectItem> estadoCivilList) {
		this.estadoCivilList = estadoCivilList;
	}

	public String getCriterioCedula() {
		return criterioCedula;
	}

	public void setCriterioCedula(String criterioCedula) {
		this.criterioCedula = criterioCedula;
	}

	public String getCriterioNombre() {
		return criterioNombre;
	}

	public void setCriterioNombre(String criterioNombre) {
		this.criterioNombre = criterioNombre;
	}

	public boolean isActivaTab() {
		return activaTab;
	}

	public void setActivaTab(boolean activaTab) {
		this.activaTab = activaTab;
	}

	public boolean isActivaObservacion() {
		return activaObservacion;
	}

	public void setActivaObservacion(boolean activaObservacion) {
		this.activaObservacion = activaObservacion;
	}

	public boolean isActBtnAprobar() {
		return actBtnAprobar;
	}

	public void setActBtnAprobar(boolean actBtnAprobar) {
		this.actBtnAprobar = actBtnAprobar;
	}

	public Double getTotalGastos() {
		return totalGastos;
	}

	public void setTotalGastos(Double totalGastos) {
		this.totalGastos = totalGastos;
	}

	public List<ParamDet> getListaTiposCtaBancaria() {
		return listaTiposCtaBancaria;
	}

	public void setListaTiposCtaBancaria(List<ParamDet> listaTiposCtaBancaria) {
		this.listaTiposCtaBancaria = listaTiposCtaBancaria;
	}

	public List<SelectItem> getListaCmbTipoCuenta() {
		return listaCmbTipoCuenta;
	}

	public void setListaCmbTipoCuenta(List<SelectItem> listaCmbTipoCuenta) {
		this.listaCmbTipoCuenta = listaCmbTipoCuenta;
	}

	public List<SelectItem> getListarBancos() {
		return listarBancos;
	}

	public void setListarBancos(List<SelectItem> listarBancos) {
		this.listarBancos = listarBancos;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public boolean isNuevo() {
		return nuevo;
	}

	public void setNuevo(boolean nuevo) {
		this.nuevo = nuevo;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public String getUrlArchivo() {
		return urlArchivo;
	}

	public void setUrlArchivo(String urlArchivo) {
		this.urlArchivo = urlArchivo;
	}

	public int getIdTmpArchivoL() {
		return idTmpArchivoL;
	}

	public void setIdTmpArchivoL(int idTmpArchivoL) {
		this.idTmpArchivoL = idTmpArchivoL;
	}

	public String getDomicilioCliente() {
		return domicilioCliente;
	}

	public void setDomicilioCliente(String domicilioCliente) {
		this.domicilioCliente = domicilioCliente;
	}

	public String getRefDomicilioCliente() {
		return refDomicilioCliente;
	}

	public void setRefDomicilioCliente(String refDomicilioCliente) {
		this.refDomicilioCliente = refDomicilioCliente;
	}

	public String getBancoCliente() {
		return bancoCliente;
	}

	public void setBancoCliente(String bancoCliente) {
		this.bancoCliente = bancoCliente;
	}

	public String getNoCuentaCliente() {
		return noCuentaCliente;
	}

	public void setNoCuentaCliente(String noCuentaCliente) {
		this.noCuentaCliente = noCuentaCliente;
	}

	public String getEstCivilCliente() {
		return estCivilCliente;
	}

	public void setEstCivilCliente(String estCivilCliente) {
		this.estCivilCliente = estCivilCliente;
	}

	public String getCorreoCliente() {
		return correoCliente;
	}

	public void setCorreoCliente(String correoCliente) {
		this.correoCliente = correoCliente;
	}

	public String getTelefonoCliente() {
		return telefonoCliente;
	}

	public void setTelefonoCliente(String telefonoCliente) {
		this.telefonoCliente = telefonoCliente;
	}

	public String getCelularCliente() {
		return celularCliente;
	}

	public void setCelularCliente(String celularCliente) {
		this.celularCliente = celularCliente;
	}

	public String getTipoCuentaCliente() {
		return tipoCuentaCliente;
	}

	public void setTipoCuentaCliente(String tipoCuentaCliente) {
		this.tipoCuentaCliente = tipoCuentaCliente;
	}

	public List<CxcCreditoPostventa> getListaPreCreditos() {
		return listaPreCreditos;
	}

	public void setListaPreCreditos(List<CxcCreditoPostventa> listaPreCreditos) {
		this.listaPreCreditos = listaPreCreditos;
	}

	public String getUrlReport() {
		return urlReport;
	}

	public void setUrlReport(String urlReport) {
		this.urlReport = urlReport;
	}

	public CxcCreDigPostventa getArchivoSel() {
		return archivoSel;
	}

	public void setArchivoSel(CxcCreDigPostventa archivoSel) {
		this.archivoSel = archivoSel;
	}

	public boolean isHabCampo() {
		return habCampo;
	}

	public void setHabCampo(boolean habCampo) {
		this.habCampo = habCampo;
	}

	public String getIdTipoCedula() {
		return idTipoCedula;
	}

	public void setIdTipoCedula(String idTipoCedula) {
		this.idTipoCedula = idTipoCedula;
	}

	public void setClaseClienteList(List<CxcClaseCliente> claseClienteList) {
		this.claseClienteList = claseClienteList;
	}

	public Float getLimiteCrediCliente() {
		return limiteCrediCliente;
	}

	public void setLimiteCrediCliente(Float limiteCrediCliente) {
		this.limiteCrediCliente = limiteCrediCliente;
	}

	public Integer getLimiteFactCliente() {
		return limiteFactCliente;
	}

	public void setLimiteFactCliente(Integer limiteFactCliente) {
		this.limiteFactCliente = limiteFactCliente;
	}

	public Short getPlazoCliente() {
		return plazoCliente;
	}

	public void setPlazoCliente(Short plazoCliente) {
		this.plazoCliente = plazoCliente;
	}

	public String getPromoCliente() {
		return promoCliente;
	}

	public void setPromoCliente(String promoCliente) {
		this.promoCliente = promoCliente;
	}

	public Float getTasaIntCliente() {
		return tasaIntCliente;
	}

	public void setTasaIntCliente(Float tasaIntCliente) {
		this.tasaIntCliente = tasaIntCliente;
	}

	public String getPagareCupoCliente() {
		return pagareCupoCliente;
	}

	public void setPagareCupoCliente(String pagareCupoCliente) {
		this.pagareCupoCliente = pagareCupoCliente;
	}

	public Short getDiasGraciaCliente() {
		return diasGraciaCliente;
	}

	public void setDiasGraciaCliente(Short diasGraciaCliente) {
		this.diasGraciaCliente = diasGraciaCliente;
	}

	public String getMotivoCliente() {
		return motivoCliente;
	}

	public void setMotivoCliente(String motivoCliente) {
		this.motivoCliente = motivoCliente;
	}

	public String getClaseVehiculos() {
		return claseVehiculos;
	}

	public void setClaseVehiculos(String claseVehiculos) {
		this.claseVehiculos = claseVehiculos;
	}

	public String getClaseRepuestos() {
		return claseRepuestos;
	}

	public void setClaseRepuestos(String claseRepuestos) {
		this.claseRepuestos = claseRepuestos;
	}

	public String getClaseServicio() {
		return claseServicio;
	}

	public void setClaseServicio(String claseServicio) {
		this.claseServicio = claseServicio;
	}

	public List<CxcCreDigPostventa> getListaArchivosDigitalesRevision() {
		return listaArchivosDigitalesRevision;
	}

	public void setListaArchivosDigitalesRevision(List<CxcCreDigPostventa> listaArchivosDigitalesRevision) {
		this.listaArchivosDigitalesRevision = listaArchivosDigitalesRevision;
	}

	public List<ParamDet> getListaTiposCliente() {
		return listaTiposCliente;
	}

	public void setListaTiposCliente(List<ParamDet> listaTiposCliente) {
		this.listaTiposCliente = listaTiposCliente;
	}

	public List<SelectItem> getListaCmbTiposCliente() {
		return listaCmbTiposCliente;
	}

	public void setListaCmbTiposCliente(List<SelectItem> listaCmbTiposCliente) {
		this.listaCmbTiposCliente = listaCmbTiposCliente;
	}

	public String getIdTipoCliente() {
		return idTipoCliente;
	}

	public void setIdTipoCliente(String idTipoCliente) {
		this.idTipoCliente = idTipoCliente;
	}

	public String getIdTipoArchRev() {
		return idTipoArchRev;
	}

	public void setIdTipoArchRev(String idTipoArchRev) {
		this.idTipoArchRev = idTipoArchRev;
	}

	public boolean isBtnNegar() {
		return btnNegar;
	}

	public void setBtnNegar(boolean btnNegar) {
		this.btnNegar = btnNegar;
	}

	public List<ParamDet> getListaTipoBloqueo() {
		return listaTipoBloqueo;
	}

	public void setListaTipoBloqueo(List<ParamDet> listaTipoBloqueo) {
		this.listaTipoBloqueo = listaTipoBloqueo;
	}

	public List<SelectItem> getListaCmbTipoBloqueo() {
		return listaCmbTipoBloqueo;
	}

	public void setListaCmbTipoBloqueo(List<SelectItem> listaCmbTipoBloqueo) {
		this.listaCmbTipoBloqueo = listaCmbTipoBloqueo;
	}

	public Integer getTipoBloqueo() {
		return tipoBloqueo;
	}

	public void setTipoBloqueo(Integer tipoBloqueo) {
		this.tipoBloqueo = tipoBloqueo;
	}

	public boolean isActivaReport() {
		return activaReport;
	}

	public void setActivaReport(boolean activaReport) {
		this.activaReport = activaReport;
	}

	public boolean isActBtnGuardar() {
		return actBtnGuardar;
	}

	public void setActBtnGuardar(boolean actBtnGuardar) {
		this.actBtnGuardar = actBtnGuardar;
	}

	public List<CxcCreDigPostventa> getListaArchivos() {
		return listaArchivos;
	}

	public void setListaArchivos(List<CxcCreDigPostventa> listaArchivos) {
		this.listaArchivos = listaArchivos;
	}

	public List<CxcCreDigPostventa> getListaArchivosRev() {
		return listaArchivosRev;
	}

	public void setListaArchivosRev(List<CxcCreDigPostventa> listaArchivosRev) {
		this.listaArchivosRev = listaArchivosRev;
	}

	public int getSIZE() {
		return SIZE;
	}

	public void setSIZE(int sIZE) {
		SIZE = sIZE;
	}

	public List<String> getEstado() {
		return estado;
	}

	public void setEstado(List<String> estado) {
		this.estado = estado;
	}

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

	public LDMSolicitudCreditoPostventa getModel() {
		return model;
	}

	public void setModel(LDMSolicitudCreditoPostventa model) {
		this.model = model;
	}

	public List<Agencia> getListaAgencias() {
		return listaAgencias;
	}

	public void setListaAgencias(List<Agencia> listaAgencias) {
		this.listaAgencias = listaAgencias;
	}

	public String getCodAgencia() {
		return codAgencia;
	}

	public void setCodAgencia(String codAgencia) {
		this.codAgencia = codAgencia;
	}

	public CotizacionRepuesto getCotizacionRepuesto() {
		return cotizacionRepuesto;
	}

	public void setCotizacionRepuesto(CotizacionRepuesto cotizacionRepuesto) {
		this.cotizacionRepuesto = cotizacionRepuesto;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public BigDecimal getCupoDisponible() {
		return cupoDisponible;
	}

	public void setCupoDisponible(BigDecimal cupoDisponible) {
		this.cupoDisponible = cupoDisponible;
	}

	public BigDecimal getCreditoSolicitado() {
		return creditoSolicitado;
	}

	public void setCreditoSolicitado(BigDecimal creditoSolicitado) {
		this.creditoSolicitado = creditoSolicitado;
	}

	public BigDecimal getSaldoVencido() {
		return saldoVencido;
	}

	public void setSaldoVencido(BigDecimal saldoVencido) {
		this.saldoVencido = saldoVencido;
	}

	public BigDecimal getGarancheck() {
		return garancheck;
	}

	public void setGarancheck(BigDecimal garancheck) {
		this.garancheck = garancheck;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getSobregiro() {
		return sobregiro;
	}

	public void setSobregiro(String sobregiro) {
		this.sobregiro = sobregiro;
	}

	public BigDecimal getNuevoCupoDisponible() {
		return nuevoCupoDisponible;
	}

	public void setNuevoCupoDisponible(BigDecimal nuevoCupoDisponible) {
		this.nuevoCupoDisponible = nuevoCupoDisponible;
	}

}