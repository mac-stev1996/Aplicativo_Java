package com.casabaca.prime.cxc.procesos.controller;

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
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Canton;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.CotizacionRepuesto;
import com.casabaca.common.ejb.model.CxcClaseCliente;
import com.casabaca.common.ejb.model.DigDocumentos;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.CantonServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CotizacionRepuestoServicesLocal;
import com.casabaca.common.ejb.service.CxcClaseClienteServiceLocal;
import com.casabaca.common.ejb.service.DigDocumentoServiceLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
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
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.EnumEstSolicitud;
import com.casabaca.prime.cxc.lazy.LDMSolicitudCreditoPostventa;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean
public class CxcListadoRevisionCreditoPostventaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CxcListadoRevisionCreditoPostventaController.class);
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
	@EJB(lookup = NombreJNDI.COTIZACION_REPUESTOS_SERVICES_BEAN)
	private CotizacionRepuestoServicesLocal cotizacionRepuestosServices;
	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;

	private List<CxcCreditoPostventa> listaCreditos;
	private CxcCreditoPostventa crePosRevisado;
	private CxcCreditoPostventa crePosRevisadoSel;
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
	private boolean actBtnRevisar = true;
	private Double totalGastos = 0.00;
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
	private Cliente clienteSel = new Cliente();
	private String idTipoArchRev = null;
	private String emailUsuarioEnvia;
	private boolean btnDevolver = false;
	// NUEVO REQUERIMIENTO
	private List<ParamDet> listaTipoBloqueo = new ArrayList<>();
	private List<SelectItem> listaCmbTipoBloqueo = new ArrayList<>();
	private Integer tipoBloqueo;
	// Req-37713 ReqCambios Solicitud Credito
	private boolean activaReport;
	private boolean actBtnGuardar;
	private List<Agencia> listaAgencias;
	private String codAgencia;
	private List<CxcCreDigPostventa> listaArchivos;
	private List<CxcCreDigPostventa> listaArchivosRev;
	private int SIZE = 10;
	private List<String> estado;
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
			editarSolicitud(crePosRevisadoSel);
		} else if (getRequestParameter("regresa") != null) {
			regresaListadoSolicitudes();
		} else {
			cargaListaSolicitudes();
		}
	}

	public void obtenerCliente() {
		try {
			Cliente cliente = clienteServiceLocal.findByCedulaNoCia(this.crePosRevisado.getCedula(),
					getCompania().getNoCia());

			if (cliente != null) {
				setCliente(cliente);
				this.crePosRevisado.setCedula(cliente.getCedula());
				this.crePosRevisado.setNombres(cliente.getNombre());
			}
		} catch (Exception e) {
			nombreCliente = null;
			setCliente(null);
			this.crePosRevisado.setCedula(cliente.getCedula());
			this.crePosRevisado.setNombres(cliente.getNombre());
			addWarnMessage("Alerta", "No existen datos del cliente ingresado");
		}
	}

	/*
	 * public void ejecutarConsultaClientes() { lazyModel = new
	 * LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(),
	 * getCedulaClienteConsulta(), getNombreClienteConsulta()); }
	 */

	public void seleccionarFilaCliente(SelectEvent event) {

		this.cliente = (Cliente) event.getObject();
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

	public void listarClientes() throws FindException {
		this.setCedulaCliente(null);
		this.setNombreCliente(null);
		this.listarClientes = new ArrayList<>();
		listarClientes = clienteServiceLocal.buscarClientesPorParametros(0, 25, getCompania().getNoCia(),
				this.getCedulaCliente(), this.getNombreCliente());
	}

	public void cargarClientes() {
		try {
			this.listarClientes = new ArrayList<>();
			listarClientes = clienteServiceLocal.buscarClientesPorParametros(0, 25, getCompania().getNoCia(),
					this.cedulaCliente, this.nombreCliente.toUpperCase());
		} catch (Exception e) {
			listarClientes = new ArrayList<>();
			logger.error(e);
			addInfoMessage("Info. ", " No se encontro al cliente ingresado");

		}
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
		this.estado.add(EnumEstSolicitud.ENVIADO.getCodigo());
		this.estado.add(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo());
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
			archivo.setNoSolicitud(crePosRevisado.getNoSolicitud());
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
			addErrorMessage("", "Error al cargar el archivo.");
			logger.error(e.getMessage(), e.getCause());
		} catch (InsertException e) {
			addErrorMessage("", "Error al cargar el archivo.");
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
			archivo.setNoSolicitud(crePosRevisado.getNoSolicitud());
			archivo.setNombre(nombreArchivo);
			archivo.setTipo("R");
			archivo.setTipoArchivo(this.idTipoArchRev);
			archivo.setUsuarioCrea(usuarioSesion);
			archivo.setFechaCrea(new Date());
			archivo.setArchivo(inputStream);
			archivo.setPath(pathDestino);
			archivoCrePostventaService.create(archivo);
			listaArchivosDigitalesRevision.add(archivo);
			addInfoMessage("", "El archivo de revision fue cargado correctamente.");
		} catch (IOException e) {
			addErrorMessage("", "Error al cargar el archivo.");
			logger.error(e.getMessage(), e.getCause());
		} catch (InsertException e) {
			addErrorMessage("", "Error al cargar el archivo.");
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void envioMailRevisado() throws UpdateException { // MAIL al grupo deSolicitudes de confirmación
		StringBuffer mensaje = new StringBuffer();
		String nomAgencia = agenciaService.buscarNombreAgencia(crePosRevisado.getNoCia(), crePosRevisado.getCentro());
		mensaje.append(
				"La Solicitud de Credito Express, se encuentra enviada para APROBACIÓN con los siguientes datos: <br><br>");
		mensaje.append("<table border='1'><tr><td><b>Numero Solicitud</b></td><td>" + crePosRevisado.getNoSolicitud()
				+ "</td></tr>");
		mensaje.append("<tr><td><b>Fecha Revisión </b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy").format(crePosRevisado.getFechaEstRevisado()) + "</td></tr>");
		if (crePosRevisado.getEstado().equals(EnumEstSolicitud.REVISADO.getCodigo())) {
			mensaje.append("<tr><td><b>Estado</b></td> <td> " + " REVISADO " + "</td></tr>");
		}
		mensaje.append("<tr><td><b>Cédula Cliente</b></td> <td> " + crePosRevisado.getCedula() + "</td></tr>");
		mensaje.append("<tr><td><b>Nombre Cliente</b></td> <td> " + crePosRevisado.getNombres() + "</td></tr>");
		mensaje.append("<tr><td><b>Centro</b></td> <td> " + nomAgencia + "</td></tr>");
		mensaje.append("<tr><td><b>Numero de Centro</b></td> <td> " + crePosRevisado.getCentro() + "</td></tr>");
		mensaje.append("<tr><td><b>Observación Revisión</b></td> <td> " + crePosRevisado.getObservacionEstRevisado()
				+ "</td></tr>");
		mensaje.append("</table>");
		mensaje.append(
				"<br><br>Por favor revisar la solicitud de Credito Postventa Express en el sistema S3S.<br><br>");

		try {
			UsuarioSis solicitante = usuarioSisServiceLocal
					.getUsuarioSisByUsername(this.crePosRevisado.getUsuarioEnvia());
			SisMailServidores sisMailServidores = getSisMailServidores(crePosRevisado.getNoCia());
			if (EnumEstSolicitud.REVISADO.getCodigo().equals(crePosRevisado.getEstado())) {
				mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
						solicitante.getEmail(),
						"NOTIFICACION DE SOLICITUD EXPRESS REVISADA CON NUMERO: " + crePosRevisado.getNoSolicitud(),
						mensaje);
			} // Mail al asesor quien registra la solicitud
			mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(), getUsuario().getEmail(),
					"NOTIFICACION DE SOLICITUD EXPRESS REVISADA CON NUMERO: " + crePosRevisado.getNoSolicitud(),
					mensaje);
			super.info("La notificación ha sido enviada por correo eletrónico");
		} catch (GeneralException e) {
			logger.error(e.getMessage(), e.getCause());
			error("La notificación no pudo ser enviada por correo eletrónico");
		} catch (FindException e) {
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void envioMailRegresaEstado() throws UpdateException {
		StringBuffer mensaje = new StringBuffer();
		mensaje.append(
				"La Solicitud de Credito Express, se encuentra enviada para REVISIÓN con los siguientes datos: <br><br>");
		mensaje.append("<table border='1'><tr><td><b>Numero Solicitud</b></td><td>" + crePosRevisado.getNoSolicitud()
				+ "</td></tr>");
		mensaje.append("<tr><td><b>Fecha Revisión </b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy").format(crePosRevisado.getFechaEstIngresado()) + "</td></tr>");

		if (crePosRevisado.getEstado().equals("D")) {
			mensaje.append("<tr><td><b>Estado</b></td> <td> " + " DEVUELTO " + "</td></tr>");
		}
		mensaje.append("<tr><td><b>Cédula Cliente</b></td> <td> " + crePosRevisado.getCedula() + "</td></tr>");
		mensaje.append("<tr><td><b>Nombre Cliente</b></td> <td> " + crePosRevisado.getNombres() + "</td></tr>");
		mensaje.append("<tr><td><b>Observación Revisión</b></td> <td> " + crePosRevisado.getObservacionEstRevisado()
				+ "</td></tr>");
		mensaje.append("</table>");
		mensaje.append(
				"<br><br>Por favor revisar la solicitud de Credito Postventa Express en el sistema S3S.<br><br>");

		try {
			UsuarioSis solicitante = usuarioSisServiceLocal
					.getUsuarioSisByUsername(this.crePosRevisado.getUsuarioEnvia()); // confirmación
			SisMailServidores sisMailServidores = getSisMailServidores(crePosRevisado.getNoCia());
			if ("D".equals(crePosRevisado.getEstado())) {
				mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(), solicitante.getEmail(),
						"NOTIFICACION DE SOLICITUD EXPRESS DEVUELTA CON NUMERO: " + crePosRevisado.getNoSolicitud(),
						mensaje);
			}
			mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(), getUsuario().getEmail(),
					"NOTIFICACION DE SOLICITUD EXPRESS DEVUELTA CON NUMERO: " + crePosRevisado.getNoSolicitud(),
					mensaje);
			super.info("La notificación ha sido enviada por correo eletrónico");
		} catch (GeneralException e) {
			logger.error(e.getMessage(), e.getCause());
			error("La notificación no pudo ser enviada por correo eletrónico");
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
			logger.error("No se pudo cargar la informacion sobre tipos de viviendas");
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
				this.habCampo = true;
				this.activaTab = false;
				this.crePosRevisado = itemSel;
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
			if (this.crePosRevisado.getNoSolicitud() != null) {
				// guardarParametrosCredito();
				creditoPostventaService.update(crePosRevisado);
				super.info("La solicitud ha sido actualizada");
			}

		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar la solicitud", e);
		}
	}

	public void guardarSolicitudRevisada() {
		this.guardarSolicitud();
		boolean existeObs = crePosRevisado.getObservacionEstRevisado() != null
				&& !crePosRevisado.getObservacionEstRevisado().equals("");
		try {
			if (!existeObs) {
				warn("Debe registrar una observacion para REVISAR la Solicitud");
				return;
			} else if ((this.crePosRevisado.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo()) || this.crePosRevisado.getEstado().equals(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo())) && this.crePosRevisado.getNoSolicitud() != null) {
				this.crePosRevisado.setEstado(EnumEstSolicitud.REVISADO.getCodigo());
				this.crePosRevisado.setUsuarioRevisa(usuarioSesion);
				this.crePosRevisado.setFechaEstRevisado(new Date());
				this.actBtnRevisar = true;
				creditoPostventaService.update(crePosRevisado);
				envioMailRevisado();
				getUrlRegresa();
				super.info("La solicitud ha sido revisada");
			}
		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar movimiento", e);
		}
	}

	public void resetearFormulario() {
		this.crePosRevisado = new CxcCreditoPostventa();
		this.cliente = new Cliente();
		this.crePosRevisadoSel = null;
		this.idCiudadGarante = null;
		this.idTipoVivienda = null;
		this.idCiudadTrabajo = null;
	}

	private void validarSolicitudSeleccionada() {
		String noSolicitud = super.getRequestParameter("noSolicitud");
		if (noSolicitud != null) {

			this.crePosRevisadoSel = consultarSolicitudRevision(getCompania().getNoCia(),
					Long.parseLong(super.getRequestParameter("noSolicitud")));
			verificarRevisionSolicitud();
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

	private CxcCreditoPostventa consultarSolicitudRevision(String noCia, Long noSolicitud) {
		edit = true;
		try {
			crePosRevisado = creditoPostventaService.buscarSolicitudRevision(noCia, noSolicitud);
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion sobre solicitudes de credito");
		}
		return crePosRevisado;
	}

	public void getUrlRegresa() {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/listRevision.jsf?regresa=true";
			redirect(url);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void getUrlConsultaRevisado(CxcCreditoPostventa item) {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/formRevisado.jsf?noSolicitud="
					+ item.getNoSolicitud();
			redirect(url);
			this.crePosRevisadoSel = item;
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void calcularTotalDisponible() {
		totalGastos = 0.00;
		Double ingreso = (crePosRevisado.getIngreso() == null ? 0.00 : crePosRevisado.getIngreso());
		Double egreso = (crePosRevisado.getEgreso() == null ? 0.00 : crePosRevisado.getEgreso());

		if (egreso <= ingreso) {
			totalGastos = ingreso - egreso;
			crePosRevisado.setMontoDisponible(totalGastos);
		} else {
			addErrorMessage("Los egresos no pueden ser mayores que los ingresos", "");
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
		this.estado.add(EnumEstSolicitud.ENVIADO.getCodigo());
		this.estado.add(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo());
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.codAgencia, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			addInfoMessage("No se han encontrado soicitudes de credito postventa para esta empresa", "");
		}
	}

	public void cargarArchivosPorSolicitud() {
		try {
			if (this.crePosRevisado != null) {
				this.listaArchivos = archivoCrePostventaService.obtenerArchivosPorSolicitud(crePosRevisado.getNoCia(),
						crePosRevisado.getNoSolicitud());
			}
		} catch (Exception e) {
			addInfoMessage("No se han encontrado archivos digitales para la solicitud", "");
		}
	}

	public void regresarEstadoEnviado() {
		boolean existeObs = crePosRevisado.getObservacionEstRevisado() != null
				&& !crePosRevisado.getObservacionEstRevisado().equals("");
		try {
			if (!existeObs) {
				warn("Debe registrar una observacion para DEVOLVER la Solicitud");
				return;
			} else if ((this.crePosRevisado.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo()) || this.crePosRevisado.getEstado().equals(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo())) && this.crePosRevisado.getNoSolicitud() != null) {
				this.crePosRevisado.setEstado("D");
				this.crePosRevisado.setUsuarioRevisa(usuarioSesion);
				// Se guarda FECHA_EST_INGRESADO cada vez que se devuelva la solicitud para
				// revision - fecha se envia en notificacion
				this.crePosRevisado.setFechaEstIngresado(new Date());
				this.actBtnRevisar = true;
				creditoPostventaService.update(crePosRevisado);
				envioMailRegresaEstado();
				super.info("La solicitud ha sido regresada a estado DEVUELTO para revisión de observaciones");
				getUrlRegresa();
			}
		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar la solicitud express", e);
		}
	}

	public void ingresarObservacionRev() {
		if (this.crePosRevisado.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo()) || this.crePosRevisado.getEstado().equals(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo())) {
			this.activaObservacion = false;
			this.btnDevolver = true;
		}
	}

	public void cerrarDialogo() {
		if (this.crePosRevisado.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo()) || this.crePosRevisado.getEstado().equals(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo())) {
			this.activaObservacion = true;
			this.btnDevolver = false;
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('diaRevFlujo').hide();");
		}
	}

	public void verificarRevisionSolicitud() {
		if (this.crePosRevisado.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo()) || this.crePosRevisado.getEstado().equals(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo())) {
			this.actBtnRevisar = false;
		} else {
			this.actBtnRevisar = true;
		}
	}

	protected String getStrReportPath() {
		urlReport = "/reportes/cxcSolicitudCreditoPostventaExpress.jasper";
		return urlReport;
	}

	private void setParameters(Map<String, Object> parameters) {
		// parameters.put("NOMBRE_CIA", getCompania().getNombre());
		parameters.put("NO_SOLICITUD", new Long(crePosRevisado.getNoSolicitud()));
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
		this.estado.add(EnumEstSolicitud.ENVIADO.getCodigo());
		this.estado.add(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo());
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.codAgencia, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			addInfoMessage("No se han encontrado solicitudes para esta empresa", "");
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

	public void deleteFile() throws DeleteException, Exception {

		CxcCreDigPostventa regEliminar = getArchivoSel();
		if (this.crePosRevisado.getNoSolicitud() == null) {
			this.listaArchivosDigitales.remove(regEliminar);
		} else {
			archivoCrePostventaService.delete(regEliminar.getNoRegistro());
		}

		cargarArchivosPorSolicitud();
	}

	public void deleteFileRevision() throws DeleteException, Exception {

		CxcCreDigPostventa regEliminar = getArchivoSel();
		if (this.crePosRevisado.getNoSolicitud() == null) {
			this.listaArchivosDigitalesRevision.remove(regEliminar);
		} else {
			archivoCrePostventaService.delete(regEliminar.getNoRegistro());
		}
		cargarArchivosRevisionPorSolicitud();
	}

	public void guardarParametrosCredito() {
		try {
			this.clienteSel = clienteServiceLocal.findByCedulaNoCia(cedulaCliente, getCompania().getNoCia());
			if (this.clienteSel.getClientePK().getNoCliente() != null) {
				this.clienteSel.setLimiteCredi(limiteCrediCliente);
				this.clienteSel.setLimiteFact(limiteFactCliente);
				this.clienteSel.setPlazo(plazoCliente);
				this.clienteSel.setPromo(promoCliente);
				this.clienteSel.setTasaInt(tasaIntCliente);
				this.clienteSel.setPagareCupo(pagareCupoCliente);
				this.clienteSel.setDiasGracia(diasGraciaCliente);
				this.clienteSel.setMotivo(motivoCliente.toUpperCase());
				if (clienteSel.getClaseVehiculos() == null) {
					this.clienteSel.setClaseVehiculos(claseVehiculos.toUpperCase());
				}
				if (clienteSel.getClaseRepuestos() == null) {
					this.clienteSel.setClaseRepuestos(claseRepuestos.toUpperCase());
				}
				if (clienteSel.getClaseServicio() == null) {
					this.clienteSel.setClaseServicio(claseServicio.toUpperCase());
				}
				this.clienteSel.setSegCensal(tipoBloqueo);
				clienteServiceLocal.update(clienteSel);
				super.info("Los parametros de credito del cliente han sido almacenados correctamente");
			}
		} catch (UpdateException | FindException e) {
			super.error("Error al guardar los parametros de credito en el cliente");
			logger.error("Error al guardar los parametros de credito desde Solicitud Postventa Express", e);
		}
	}

	public void cargarArchivosRevisionPorSolicitud() {
		String tipo = "R";
		try {
			if (this.crePosRevisado != null) {
				this.listaArchivosRev = archivoCrePostventaService.obtenerArchivosPorSolicitudRevisada(
						crePosRevisado.getNoCia(), crePosRevisado.getNoSolicitud(), tipo);
			}
		} catch (Exception e) {
			addInfoMessage("No se han encontrado archivos digitales de revision para la solicitud", "");
		}
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
		if (this.crePosRevisado.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo()) || this.crePosRevisado.getEstado().equals(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo())) {
			this.activaObservacion = true;
			this.btnDevolver = false;
			this.crePosRevisado.setObservacionEstRevisado(VALOR_NULO);
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

	private void cargarAgencias() {
		try {
			String[] order = { "nombre" };
			this.listaAgencias = agenciaService.findByCompania(getCompania().getNoCia(), order);
			if (this.listaAgencias.isEmpty()) {
				error("No se puede cargar las agencias de la empresa");
				listaAgencias.clear();
			}
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion de agencias de la empresa");
		}
	}

	public void consultarCuposCliente() {
		this.cupoDisponible = BigDecimal.ZERO;
		this.monto = BigDecimal.ZERO;
		this.creditoSolicitado = BigDecimal.ZERO;
		this.nuevoCupoDisponible = BigDecimal.ZERO;
		this.saldoVencido = BigDecimal.ZERO;
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
				creditoSolicitado = (crePosRevisado.getCupoSugerido());
				nuevoCupoDisponible = cupoDisponible.subtract(creditoSolicitado);
			} else {
				cupoDisponible = new BigDecimal((double) this.cliente.getLimiteCredi());
				creditoSolicitado = (crePosRevisado.getCupoSugerido());
				nuevoCupoDisponible = cupoDisponible.subtract(creditoSolicitado);
			}
			this.saldoVencido = creditoPostventaService.obtenerSaldosVencidosCliente(getCompania().getNoCia(),
					crePosRevisado.getCedula());
		} catch (Exception e) {
			logger.error("No se pudo consultar cupos de cliente", e.getCause());
		}

	}

	public void negarSolicitud() {
		boolean existeObs = crePosRevisado.getObservacionEstRevisado() != null
				&& !crePosRevisado.getObservacionEstRevisado().equals("");
		try {
			if (!existeObs) {
				warn("Debe registrar una observacion para NEGAR la Solicitud");
				return;
			} else if ((this.crePosRevisado.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo()) || this.crePosRevisado.getEstado().equals(EnumEstSolicitud.DEVUELTO_REVISION.getCodigo())) && this.crePosRevisado.getNoSolicitud() != null) {
				this.crePosRevisado.setEstado(EnumEstSolicitud.NEGADO.getCodigo());
				this.crePosRevisado.setUsuarioAprueba(usuarioSesion);
				crePosRevisado.setFechaEstAprobado(new Date());
				creditoPostventaService.update(crePosRevisado);
//				this.actBtnAprobar = true;
				this.envioMailSolictudNegada();
				super.info("La Solicitud Postventa ha sido NEGADA");
				getUrlRegresa();
			}
		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar la solicitud express", e);
		}
	}

	public void envioMailSolictudNegada() throws UpdateException, GeneralException { // MAIL al grupo deSolicitudes de
		try {
			UsuarioSis solicitante = usuarioSisServiceLocal
					.getUsuarioSisByUsername(this.crePosRevisado.getUsuarioEnvia()); // confirmación
			StringBuffer mensaje = new StringBuffer();
			mensaje.append("La Solicitud de Credito Express, se encuentra NEGADA con los siguientes datos: <br><br>");
			mensaje.append("<table border='1'><tr><td><b>Numero Solicitud</b></td><td>"
					+ crePosRevisado.getNoSolicitud() + "</td></tr>");
			mensaje.append("<tr><td><b>Fecha Negación </b></td> <td> "
					+ new SimpleDateFormat("dd/MM/yyyy").format(crePosRevisado.getFechaEstAprobado()) + "</td></tr>");
			if (crePosRevisado.getEstado().equals(EnumEstSolicitud.NEGADO.getCodigo())) {
				mensaje.append("<tr><td><b>Estado</b></td> <td> " + " NEGADA " + "</td></tr>");
			}
			mensaje.append("<tr><td><b>Cédula Cliente</b></td> <td> " + crePosRevisado.getCedula() + "</td></tr>");
			mensaje.append("<tr><td><b>Nombres Cliente</b></td> <td> " + crePosRevisado.getNombres() + "</td></tr>");
			mensaje.append("<tr><td><b>Observación Negación</b></td> <td> " + crePosRevisado.getObservacionEstRevisado()
					+ "</td></tr>");
			mensaje.append("</table>");
			mensaje.append(
					"<br><br>Por favor revisar la Solicitud de Credito Postventa Express en el sistema S3S.<br><br>");
			SisMailServidores sisMailServidores = getSisMailServidores(crePosRevisado.getNoCia());

			if (EnumEstSolicitud.NEGADO.getCodigo().equals(crePosRevisado.getEstado())) {
				mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
						solicitante.getEmail(),
						"NOTIFICACION DE NEGACIÓN DE SOLICITUD EXPRESS NUMERO: " + crePosRevisado.getNoSolicitud(),
						mensaje);
			} // Mail al asesor quien registra la solicitud
			mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(), getUsuario().getEmail(),
					"NOTIFICACION DE NEGACIÓN DE SOLICITUD EXPRESS NUMERO: " + crePosRevisado.getNoSolicitud(),
					mensaje);
			super.info("La notificación ha sido enviada por correo eletrónico");
		} catch (GeneralException e) {
			logger.error(e.getMessage(), e.getCause());
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

	public CxcCreditoPostventa getCrePosRevisado() {
		return crePosRevisado;
	}

	public void setCrePosRevisado(CxcCreditoPostventa crePosRevisado) {
		this.crePosRevisado = crePosRevisado;
	}

	public CxcCreditoPostventa getCrePosRevisadoSel() {
		return crePosRevisadoSel;
	}

	public void setCrePosRevisadoSel(CxcCreditoPostventa crePosRevisadoSel) {
		this.crePosRevisadoSel = crePosRevisadoSel;
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

	public boolean isActivaObservacion() {
		return activaObservacion;
	}

	public void setActivaObservacion(boolean activaObservacion) {
		this.activaObservacion = activaObservacion;
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

	public boolean isActBtnRevisar() {
		return actBtnRevisar;
	}

	public void setActBtnRevisar(boolean actBtnRevisar) {
		this.actBtnRevisar = actBtnRevisar;
	}

	public Double getTotalGastos() {
		return totalGastos;
	}

	public void setTotalGastos(Double totalGastos) {
		this.totalGastos = totalGastos;
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

	public void setClaseClienteList(List<CxcClaseCliente> claseClienteList) {
		this.claseClienteList = claseClienteList;
	}

	public CxcCreDigPostventa getArchivoSel() {
		return archivoSel;
	}

	public void setArchivoSel(CxcCreDigPostventa archivoSel) {
		this.archivoSel = archivoSel;
	}

	public MailServiceLocal getMailService() {
		return mailService;
	}

	public void setMailService(MailServiceLocal mailService) {
		this.mailService = mailService;
	}

	public CxcClaseClienteServiceLocal getServicioClaseCliente() {
		return servicioClaseCliente;
	}

	public void setServicioClaseCliente(CxcClaseClienteServiceLocal servicioClaseCliente) {
		this.servicioClaseCliente = servicioClaseCliente;
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

	public Cliente getClienteSel() {
		return clienteSel;
	}

	public void setClienteSel(Cliente clienteSel) {
		this.clienteSel = clienteSel;
	}

	public String getIdTipoArchRev() {
		return idTipoArchRev;
	}

	public void setIdTipoArchRev(String idTipoArchRev) {
		this.idTipoArchRev = idTipoArchRev;
	}

	public String getEmailUsuarioEnvia() {
		return emailUsuarioEnvia;
	}

	public void setEmailUsuarioEnvia(String emailUsuarioEnvia) {
		this.emailUsuarioEnvia = emailUsuarioEnvia;
	}

	public boolean isBtnDevolver() {
		return btnDevolver;
	}

	public void setBtnDevolver(boolean btnDevolver) {
		this.btnDevolver = btnDevolver;
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

	public BigDecimal getNuevoCupoDisponible() {
		return nuevoCupoDisponible;
	}

	public void setNuevoCupoDisponible(BigDecimal nuevoCupoDisponible) {
		this.nuevoCupoDisponible = nuevoCupoDisponible;
	}

	public BigDecimal getSaldoVencido() {
		return saldoVencido;
	}

	public void setSaldoVencido(BigDecimal saldoVencido) {
		this.saldoVencido = saldoVencido;
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

	public BigDecimal getGarancheck() {
		return garancheck;
	}

	public void setGarancheck(BigDecimal garancheck) {
		this.garancheck = garancheck;
	}

}