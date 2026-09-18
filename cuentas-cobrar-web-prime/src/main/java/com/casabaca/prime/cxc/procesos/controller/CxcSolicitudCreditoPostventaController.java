package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Canton;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.DigDocumentos;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.CantonServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.DigDocumentoServiceLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
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
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean
public class CxcSolicitudCreditoPostventaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CxcSolicitudCreditoPostventaController.class);

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

	private CxcCreditoPostventa creditoPostventa;
	private CxcCreditoPostventa creditoPostventaSel;
	private List<CxcCreDigPostventa> listaArchivosDigitales = new ArrayList<>();
	private String idTipoVivienda;
	private String idBanco;
	private String idTipoCedula;

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
	private String pathReservacion;
	private Object[] archivo;
	private UploadedFile file;

	private List<Cliente> listarClientes;
	private List<ParamDet> listaTiposVivienda = new ArrayList<>();
	private List<SelectItem> listaCmbTipoVivienda = new ArrayList<>();
	private List<Canton> cantonesList = new ArrayList<Canton>();
	private List<Canton> cantonesListGarante = new ArrayList<Canton>();
	private List<ParamDet> listaTiposCtaBancaria = new ArrayList<>();
	private List<SelectItem> listaCmbTipoCuenta = new ArrayList<>();
	private List<SelectItem> listarBancos = new ArrayList<>();
	private List<SelectItem> estadoCivilList = new ArrayList<SelectItem>();
	private List<CxcCreditoPostventa> listaPreCreditos;

	private static final String CODIGO_CATALOGO = "EMTCTA";
	private static final String CODIGO_VIVIENDA = "CLIVIE";

	private String urlReport;
	private String criterioCedula;
	private String criterioNombre;
	private boolean activaTab;
	private boolean edit;
	private boolean nuevo;
	private int numero = 0;
	private String urlArchivo;
	private int idTmpArchivoL;
	private boolean actBtnEnviar = true;
	private Double totalGastos = 0.00;
	// NUEVO REQUERIMIENTO
	private CxcCreDigPostventa archivoSel = new CxcCreDigPostventa();
	private boolean habCampo;
	private List<ParamDet> listaTiposCliente = new ArrayList<>();
	private List<SelectItem> listaCmbTiposCliente = new ArrayList<>();
	private String idTipoCliente;
	private String claseVehiculos;
	private String claseRepuestos;
	private String claseServicio;
	// Req-37713 ReqCambios Solicitud Credito
	private boolean activaReport;
	private boolean actBtnGuardar;
	private List<CxcCreDigPostventa> listaArchivos;
	private int SIZE = 10;
	private List <String> estado;
	private Date fechaDesde;
	private Date fechaHasta;
	private LDMSolicitudCreditoPostventa model;
	private boolean convenio;
	private String estadoBusqueda;

	@PostConstruct
	public void init() throws Exception {
		if (getRequestParameter("nuevo") != null) {
			crearNuevaSolicitud();
		} else if (getRequestParameter("noSolicitud") != null) {
			validarSolicitudSeleccionada();
			editarSolicitud(creditoPostventaSel);
		} else if (getRequestParameter("regresa") != null) {
			regresaListadoSolicitudes();
		} else {
			cargaListaSolicitudes();
		}
	}

	public void seleccionarClientePorCodigo(AjaxBehaviorEvent event) {
		if (event != null) {
			try {
				Cliente cliente = clienteServiceLocal.findByCedulaNoCia(this.creditoPostventa.getCedula(),
						getCompania().getNoCia());

				if (cliente != null) {
					setCliente(cliente);
					this.creditoPostventa.setCedula(cliente.getCedula());
					this.creditoPostventa.setNombres(cliente.getNombre());
				} else {
					cedulaCliente = null;
					nombreCliente = null; // this.conDepositos.setCedula(null);
					this.creditoPostventa.setNombres(null);
					setCliente(null);
					addWarnMessage("Alerta",
							"El cliente no está creado aún en el maestro de clientes. Si se permitirá grabar");
				}
			} catch (Exception e) {

				nombreCliente = null;
				setCliente(null);
				this.creditoPostventa.setCedula(cliente.getCedula());
				this.creditoPostventa.setNombres(cliente.getNombre());
				addWarnMessage("Alerta", "No existen datos del cliente ingresado");
			}
		}
	}

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
					// NUEVO REQUERIMIENTO
					this.idTipoCedula = cliente.getTipoIdentificacion();
					this.idTipoCliente = cliente.getTipoCliente();
					this.claseVehiculos = cliente.getClaseVehiculos();
					this.claseServicio = cliente.getClaseServicio();
					this.claseRepuestos = cliente.getClaseRepuestos();
				} else {
					addInfoMessage("Info. ",
							"No se encontro el número de identificación, por favor proceda a registrar el nombre del cliente");
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
					this.idTipoCliente = null;
					this.idTipoCedula = null;
					this.claseVehiculos = null;
					this.claseServicio = null;
					this.claseRepuestos = null;
					this.cliente = new Cliente();
					this.creditoPostventa = new CxcCreditoPostventa();
				}
			} catch (FindException e) {
				addErrorMessage(e.getSummary(), e.getDetail());
			}
		} else {
			addErrorMessage("", "Por favor ingrese el número de identificación del cliente");
		}

	}

	public void buscarSolicitud() {
		this.noCia = getCompania().getNoCia();
		this.centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		this.estado = new ArrayList<>();
		this.estado.add(estadoBusqueda);
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.centro, this.estado,
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
			archivo.setNoSolicitud(creditoPostventa.getNoSolicitud());
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

	public String getPathReservacion() {
		return pathReservacion;
	}

	public void setPathReservacion(String pathReservacion) {
		this.pathReservacion = pathReservacion;
	}

	/**
	 * Permite enviar la solicitud para revision.
	 */
	public void enviarSolicitud() {
		this.creditoPostventa.setEstado(EnumEstSolicitud.ENVIADO.getCodigo());
		this.creditoPostventa.setUsuarioEnvia(usuarioSesion);
		this.creditoPostventa.setFechaEstEnviado(new Date());
		this.actBtnEnviar = true;
		this.actBtnGuardar = true;
		this.activaTab = true;
		try {
			creditoPostventaService.update(creditoPostventa);
			this.envioMail();
		} catch (UpdateException e) {
			error("La solicitud de credito no pudo ser enviada para revisión");
		}
	}

	public void envioMail() throws UpdateException { // MAIL al grupo deSolicitudes de confirmación
		StringBuffer mensaje = new StringBuffer();
		String nomAgencia = agenciaService.buscarNombreAgencia(creditoPostventa.getNoCia(),
				creditoPostventa.getCentro());
		mensaje.append(
				"Se ha enviado la Solicitud de Credito Express, con los siguientes datos para su revisión: <br><br>");
		mensaje.append("<table border='1'><tr><td><b>Numero Solicitud</b></td><td>" + creditoPostventa.getNoSolicitud()
				+ "</td></tr>");
		mensaje.append("<tr><td><b>Asesor Envia </b></td> <td> " + creditoPostventa.getUsuarioEnvia() + "</td></tr>");
		mensaje.append("<tr><td><b>Fecha </b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy").format(creditoPostventa.getFecha()) + "</td></tr>");
		if (creditoPostventa.getEstado().equals(EnumEstSolicitud.ENVIADO.getCodigo())) {
			mensaje.append("<tr><td><b>Estado </b></td> <td> " + " ENVIADO " + "</td></tr>");
		}
		mensaje.append("<tr><td><b>Cédula Cliente </b></td> <td> " + creditoPostventa.getCedula() + "</td></tr>");
		mensaje.append("<tr><td><b>Nombre Cliente </b></td> <td> " + creditoPostventa.getNombres() + "</td></tr>");
		mensaje.append("<tr><td><b>Centro </b></td> <td> " + nomAgencia + "</td></tr>");
		mensaje.append("<tr><td><b>Codigo Centro </b></td> <td> " + creditoPostventa.getCentro() + "</td></tr>");
		mensaje.append(
				"<tr><td><b>Cupo Sugerido </b></td> <td> $ " + creditoPostventa.getCupoSugerido() + "</td></tr>");
		mensaje.append("</table>");
		mensaje.append(
				"<br><br>Por favor revisar la solicitud de Credito Postventa Express en el sistema S3S.<br><br>");

		try {
			SisMailServidores sisMailServidores = getSisMailServidores(creditoPostventa.getNoCia());
			if (getUsuarioCentroConectado().getUsuarioCentroPK().getNoCia().equals(CommonConstants.CASABACA)) {
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SCPE");
				if (usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {
						mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
								(String) objects[1],
								"NOTIFICACION DE ENVIO DE SOLICITUD EXPRESS ".concat(getCompania().getNombre())
										.concat(" - NUMERO: ")
										.concat(String.valueOf(creditoPostventa.getNoSolicitud())),
								mensaje);
					}
				}

			} else if (getUsuarioCentroConectado() != null
					&& getUsuarioCentroConectado().getUsuarioCentroPK().getNoCia() != null) {
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SCP" + getCompania().getNoCia());
				if (usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {
						mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
								(String) objects[1],
								"NOTIFICACION DE ENVIO DE SOLICITUD EXPRESS ".concat(getCompania().getNombre())
										.concat(" - NUMERO: ")
										.concat(String.valueOf(creditoPostventa.getNoSolicitud())),
								mensaje);
					}
				}
			} // Mail al asesor quien registra la solicitud
			mailService
					.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(), getUsuario().getEmail(),
							"NOTIFICACION DE ENVIO DE SOLICITUD EXPRESS ".concat(getCompania().getNombre())
									.concat(" - NUMERO: ").concat(String.valueOf(creditoPostventa.getNoSolicitud())),
							mensaje);
			super.info("La notificación ha sido enviada por correo eletrónico");
		} catch (GeneralException e) {
			super.error("La notificación no pudo ser enviada por correo eletrónico");
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
				this.creditoPostventa = itemSel;
				this.cedulaCliente = creditoPostventa.getCedula();
				this.nombreCliente = creditoPostventa.getNombres();
				this.domicilioCliente = creditoPostventa.getDomicilioCalle();
				this.refDomicilioCliente = creditoPostventa.getDomicilioReferencia();
				this.telefonoCliente = creditoPostventa.getTelefono();
				this.celularCliente = creditoPostventa.getTelefonoCel();
				this.estCivilCliente = creditoPostventa.getEstadoCivil();
				this.correoCliente = creditoPostventa.getEmail();
				this.bancoCliente = creditoPostventa.getBanco();
				this.noCuentaCliente = creditoPostventa.getNoCuenta();
				this.tipoCuentaCliente = creditoPostventa.getTipoCuenta();
				this.idTipoCedula = creditoPostventa.getTipoIdentificacion();
				this.idTipoCliente = creditoPostventa.getTipoCliente();
				cargarArchivosPorSolicitud();
				if (this.listaArchivos == null) {
					info("No existen archivos registrados para esta solcitud");
				} else {
					this.listaArchivosDigitales = this.listaArchivos;
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
			if (this.creditoPostventa.getNoSolicitud() == null) {
				if (this.cliente.getClientePK().getNoCliente() != null) {
					this.creditoPostventa.setCedula(cliente.getCedula().toUpperCase());
					this.creditoPostventa.setNombres(cliente.getNombre().toUpperCase());
					if (cliente.getDomicilioCalle() == null) {
						this.creditoPostventa.setDomicilioCalle(domicilioCliente.toUpperCase());
						this.cliente.setDomicilioCalle(domicilioCliente.toUpperCase());
					} else {
						this.creditoPostventa.setDomicilioCalle(cliente.getDomicilioCalle().toUpperCase());
					}
					if (cliente.getDomicilioReferencia() == null) {
						this.creditoPostventa.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
						this.cliente.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
					} else {
						this.creditoPostventa.setDomicilioReferencia(cliente.getDomicilioReferencia().toUpperCase());
					}
					if (cliente.getDomicilioTelefono() == null) {
						this.creditoPostventa.setTelefono(telefonoCliente.toUpperCase());
						this.cliente.setDomicilioTelefono(telefonoCliente.toUpperCase());
					} else {
						this.creditoPostventa.setTelefono(cliente.getDomicilioTelefono().toUpperCase());
					}
					if (cliente.getTelefono() == null) {
						this.creditoPostventa.setTelefonoCel(celularCliente.toUpperCase());
						this.cliente.setTelefono(celularCliente.toUpperCase());
					} else {
						this.creditoPostventa.setTelefonoCel(cliente.getTelefono().toUpperCase());
					}
					if (cliente.getNoCtaCliente() == null) {
						this.creditoPostventa.setNoCuenta(noCuentaCliente.toUpperCase());
						this.cliente.setNoCtaCliente(noCuentaCliente.toUpperCase());
					} else {
						this.creditoPostventa.setNoCuenta(cliente.getNoCtaCliente().toUpperCase());
					}
					if (cliente.getTipoCuenta() == null) {
						this.creditoPostventa.setTipoCuenta(tipoCuentaCliente.toUpperCase());
						this.cliente.setTipoCuenta(tipoCuentaCliente.toUpperCase());
					} else {
						this.creditoPostventa.setTipoCuenta(cliente.getTipoCuenta().toUpperCase());
					}
					if (cliente.getBanco() == null) {
						this.creditoPostventa.setBanco(bancoCliente.toUpperCase());
						this.cliente.setBanco(bancoCliente.toUpperCase());
					} else {
						this.creditoPostventa.setBanco(cliente.getBanco().toUpperCase());
					}
					if (cliente.getEstadoCivil() == null) {
						this.creditoPostventa.setEstadoCivil(estCivilCliente.toUpperCase());
						this.cliente.setEstadoCivil(estCivilCliente.toUpperCase());
					} else {
						this.creditoPostventa.setEstadoCivil(cliente.getEstadoCivil().toUpperCase());
					}
					if (cliente.getEmail1() == null) {
						this.creditoPostventa.setEmail(correoCliente);
						this.cliente.setEmail1(correoCliente);
					} else {
						this.creditoPostventa.setEmail(cliente.getEmail1());
					}
					if (cliente.getTipoIdentificacion() == null) {
						this.creditoPostventa.setTipoIdentificacion(idTipoCedula.toUpperCase());
						this.cliente.setTipoIdentificacion(idTipoCedula.toUpperCase());
					} else {
						this.creditoPostventa.setTipoIdentificacion(cliente.getTipoIdentificacion().toUpperCase());
					}
					if (cliente.getTipoCliente() == null) {
						this.creditoPostventa.setTipoCliente(idTipoCliente.toUpperCase());
						this.cliente.setTipoCliente(idTipoCliente.toUpperCase());
						;
					} else {
						this.creditoPostventa.setTipoCliente(cliente.getTipoCliente().toUpperCase());
					}
					if (cliente.getClaseVehiculos() != null) {
						this.creditoPostventa.setClaseVehiculos(claseVehiculos.toUpperCase());
					}
					if (cliente.getClaseRepuestos() != null) {
						this.creditoPostventa.setClaseRepuestos(claseRepuestos.toUpperCase());
					}
					if (cliente.getClaseServicio() != null) {
						this.creditoPostventa.setClaseServicio(claseServicio.toUpperCase());
					}
					clienteServiceLocal.update(cliente);
				} else { // GUARDAR CLIENTES NUEVOS EN MAESTRO DE CLIENTES
					if (idTipoCedula.equals("C") && cedulaCliente.length() == 10) {
						this.creditoPostventa.setTipoIdentificacion(idTipoCedula.toUpperCase());
						this.creditoPostventa.setTipoCliente(idTipoCliente.toUpperCase());
						this.creditoPostventa.setCedula(cedulaCliente.toUpperCase());
						this.creditoPostventa.setNombres(nombreCliente.toUpperCase());
						this.creditoPostventa.setDomicilioCalle(domicilioCliente.toUpperCase());
						this.creditoPostventa.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
						this.creditoPostventa.setTelefonoCel(celularCliente.toUpperCase());
						this.creditoPostventa.setTelefono(telefonoCliente.toUpperCase());
						this.creditoPostventa.setBanco(bancoCliente.toUpperCase());
						this.creditoPostventa.setNoCuenta(noCuentaCliente.toUpperCase());
						this.creditoPostventa.setTipoCuenta(tipoCuentaCliente.toUpperCase());
						this.creditoPostventa.setEstadoCivil(estCivilCliente.toUpperCase());
						this.creditoPostventa.setEmail(correoCliente);
						guardarNuevoCliente();
					} else if (idTipoCedula.equals("R") && cedulaCliente.length() == 13) {
						this.creditoPostventa.setTipoIdentificacion(idTipoCedula.toUpperCase());
						this.creditoPostventa.setTipoCliente(idTipoCliente.toUpperCase());
						this.creditoPostventa.setCedula(cedulaCliente.toUpperCase());
						this.creditoPostventa.setNombres(nombreCliente.toUpperCase());
						this.creditoPostventa.setDomicilioCalle(domicilioCliente.toUpperCase());
						this.creditoPostventa.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
						this.creditoPostventa.setTelefonoCel(celularCliente.toUpperCase());
						this.creditoPostventa.setTelefono(telefonoCliente.toUpperCase());
						this.creditoPostventa.setBanco(bancoCliente.toUpperCase());
						this.creditoPostventa.setNoCuenta(noCuentaCliente.toUpperCase());
						this.creditoPostventa.setTipoCuenta(tipoCuentaCliente.toUpperCase());
						this.creditoPostventa.setEstadoCivil(estCivilCliente.toUpperCase());
						this.creditoPostventa.setEmail(correoCliente);
						guardarNuevoCliente();
					} else if (idTipoCedula.equals("P")) {
						this.creditoPostventa.setTipoIdentificacion(idTipoCedula.toUpperCase());
						this.creditoPostventa.setTipoCliente(idTipoCliente.toUpperCase());
						this.creditoPostventa.setCedula(cedulaCliente.toUpperCase());
						this.creditoPostventa.setNombres(nombreCliente.toUpperCase());
						this.creditoPostventa.setDomicilioCalle(domicilioCliente.toUpperCase());
						this.creditoPostventa.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
						this.creditoPostventa.setTelefonoCel(celularCliente.toUpperCase());
						this.creditoPostventa.setTelefono(telefonoCliente.toUpperCase());
						this.creditoPostventa.setBanco(bancoCliente.toUpperCase());
						this.creditoPostventa.setNoCuenta(noCuentaCliente.toUpperCase());
						this.creditoPostventa.setTipoCuenta(tipoCuentaCliente.toUpperCase());
						this.creditoPostventa.setEstadoCivil(estCivilCliente.toUpperCase());
						this.creditoPostventa.setEmail(correoCliente);
						guardarNuevoCliente();
					} else {
						addErrorMessage("ERROR", "El número de identificación no es correcto por favor revisar");
						return;
					}
				}
				this.creditoPostventa.setNoCia(getCompania().getNoCia());
				this.creditoPostventa.setUsuario(usuarioSesion);
				this.creditoPostventa.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
				this.creditoPostventa.setEstado(EnumEstSolicitud.INGRESADO.getCodigo());
				this.creditoPostventa.setFecha(new Date());
				this.creditoPostventa.setNoCia(getCompania().getNoCia());
				creditoPostventaService.create(creditoPostventa);
				this.activaTab = false;
				this.activaReport = false;
				this.actBtnEnviar = false;
				addInfoMessage("Info", "La solicitud ha sido guardada exitosamente");
				getUrlRegresa();
				// regresaListadoSolicitudes();
			} else {
				// clienteServiceLocal.update(cliente);
				this.creditoPostventa.setDomicilioCalle(domicilioCliente.toUpperCase());
				this.creditoPostventa.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
				this.creditoPostventa.setTelefonoCel(celularCliente.toUpperCase());
				this.creditoPostventa.setTelefono(telefonoCliente.toUpperCase());
				this.creditoPostventa.setBanco(bancoCliente.toUpperCase());
				this.creditoPostventa.setNoCuenta(noCuentaCliente.toUpperCase());
				this.creditoPostventa.setTipoCuenta(tipoCuentaCliente.toUpperCase());
				this.creditoPostventa.setEstadoCivil(estCivilCliente.toUpperCase());
				this.creditoPostventa.setEmail(correoCliente);
				creditoPostventaService.update(creditoPostventa);
				super.info("La solicitud ha sido actualizada");
			}

		} catch (Exception e) {
			super.error("Error al guardar la solicitud");
			logger.error("Error al guardar la solicitud", e);
		}
	}

	public void guardarNuevoCliente() {
		try {
			if (this.nuevo == false && this.cliente.getClientePK().getNoCliente() == null) {
				if (idTipoCedula.equals("C") && cedulaCliente.length() == 10) {
					this.cliente.getClientePK().setNoCia(getCompania().getNoCia());
					this.cliente.setTipoIdentificacion(idTipoCedula.toUpperCase());
					this.cliente.setTipoCliente(idTipoCliente.toUpperCase());
					this.cliente.setCedula(cedulaCliente.toUpperCase());
					this.cliente.setNombre(nombreCliente.toUpperCase());
					this.cliente.setNombreComercial(nombreCliente.toUpperCase());
					this.cliente.setDomicilioCalle(domicilioCliente.toUpperCase());
					this.cliente.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
					this.cliente.setTelefono(celularCliente.toUpperCase());
					this.cliente.setDomicilioTelefono(telefonoCliente.toUpperCase());
					this.cliente.setBanco(bancoCliente.toUpperCase());
					this.cliente.setNoCtaCliente(noCuentaCliente.toUpperCase());
					this.cliente.setTipoCuenta(tipoCuentaCliente.toUpperCase());
					this.cliente.setEstadoCivil(estCivilCliente.toUpperCase());
					this.cliente.setEmail1(correoCliente);
					clienteServiceLocal.save(cliente);
					super.info("El Nuevo Cliente ha sido creado");
					this.nuevo = true;
					return;
				}
				if (idTipoCedula.equals("R") && cedulaCliente.length() == 13) {
					this.cliente.getClientePK().setNoCia(getCompania().getNoCia());
					this.cliente.setTipoIdentificacion(idTipoCedula.toUpperCase());
					this.cliente.setTipoCliente(idTipoCliente.toUpperCase());
					this.cliente.setCedula(cedulaCliente.toUpperCase());
					this.cliente.setNombre(nombreCliente.toUpperCase());
					this.cliente.setNombreComercial(nombreCliente.toUpperCase());
					this.cliente.setDomicilioCalle(domicilioCliente.toUpperCase());
					this.cliente.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
					this.cliente.setTelefono(celularCliente.toUpperCase());
					this.cliente.setDomicilioTelefono(telefonoCliente.toUpperCase());
					this.cliente.setBanco(bancoCliente.toUpperCase());
					this.cliente.setNoCtaCliente(noCuentaCliente.toUpperCase());
					this.cliente.setTipoCuenta(tipoCuentaCliente.toUpperCase());
					this.cliente.setEstadoCivil(estCivilCliente.toUpperCase());
					this.cliente.setEmail1(correoCliente);
					clienteServiceLocal.save(cliente);
					super.info("El Nuevo Cliente ha sido creado");
					this.nuevo = true;
					return;
				}
				if (idTipoCedula.equals("P")) {
					this.cliente.getClientePK().setNoCia(getCompania().getNoCia());
					this.cliente.setTipoIdentificacion(idTipoCedula.toUpperCase());
					this.cliente.setTipoCliente(idTipoCliente.toUpperCase());
					this.cliente.setCedula(cedulaCliente.toUpperCase());
					this.cliente.setNombre(nombreCliente.toUpperCase());
					this.cliente.setNombreComercial(nombreCliente.toUpperCase());
					this.cliente.setDomicilioCalle(domicilioCliente.toUpperCase());
					this.cliente.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
					this.cliente.setTelefono(celularCliente.toUpperCase());
					this.cliente.setDomicilioTelefono(telefonoCliente.toUpperCase());
					this.cliente.setBanco(bancoCliente.toUpperCase());
					this.cliente.setNoCtaCliente(noCuentaCliente.toUpperCase());
					this.cliente.setTipoCuenta(tipoCuentaCliente.toUpperCase());
					this.cliente.setEstadoCivil(estCivilCliente.toUpperCase());
					this.cliente.setEmail1(correoCliente);
					clienteServiceLocal.save(cliente);
					super.info("El Nuevo Cliente ha sido creado");
					this.nuevo = true;
					return;
				}
			}
		} catch (InsertException e) {
			super.error("Error al guardar el cliente");
			logger.error("Error al guardar el cliente desde Solicitud Postventa Express", e);
		}
	}

	public void resetearFormulario() {
		this.creditoPostventa = new CxcCreditoPostventa();
		this.cliente = new Cliente();
		this.creditoPostventaSel = null;
		this.idTipoVivienda = null;
	}

	public void crearNuevaSolicitud() {
		this.creditoPostventa = new CxcCreditoPostventa();
		this.cliente = new Cliente();
		this.creditoPostventaSel = null;
		this.idTipoVivienda = null;
		this.nuevo = true;
		this.activaTab = true;
		this.activaReport = true;
		this.actBtnGuardar = false;
		this.usuarioSesion = getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario();
		obtenerTodosCantones();
		obtenerCantonesGarante();
		cargarTipoCtasBancarias();
		cargarTipoVivienda();
		cargarListaBancos();
		cargarEstadoCivilList();
		// NUEVO REQUERIMIENTO
		cargarTipoClientes();
		this.validarConvenio();
	}

	public void crearNuevoCliente() {
		this.cliente = new Cliente();
	}

	private void validarSolicitudSeleccionada() {
		String noSolicitud = super.getRequestParameter("noSolicitud");
		if (noSolicitud != null) {

			this.creditoPostventaSel = consultarSolicitud(getCompania().getNoCia(),
					getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(),
					Long.parseLong(super.getRequestParameter("noSolicitud")));
			validarBotonesSolicitud();
			obtenerTodosCantones();
			obtenerCantonesGarante();
			cargarTipoCtasBancarias();
			cargarTipoVivienda();
			cargarEstadoCivilList();
			cargarListaBancos();
			// NUEVO REQUERIMIENTO
			cargarTipoClientes();
			this.usuarioSesion = getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario();
			this.listaArchivosDigitales = new ArrayList<>();
			this.listaArchivos = new ArrayList<>();
			this.validarConvenio();
		}

	}

	private CxcCreditoPostventa consultarSolicitud(String noCia, String centro, Long noSolicitud) {
		edit = true;
		try {
			this.creditoPostventa = creditoPostventaService.buscarSolicitud(noCia, centro, noSolicitud);
		} catch (Exception e) {
			super.error("No se encontraron solicitudes de credito postventa");
		}
		return this.creditoPostventa;
	}

	public void getUrlConsulta(CxcCreditoPostventa item) {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/form.jsf?noSolicitud="
					+ item.getNoSolicitud();
			redirect(url);
			this.creditoPostventaSel = item;
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void getUrlNuevo() {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/form.jsf?nuevo=true";
			redirect(url);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void getUrlRegresa() {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/list.jsf?regresa=true";
			redirect(url);
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
		this.centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.centro, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			addInfoMessage("No se han encontrado soicitudes de credito postventa para esta empresa", "");
		}
	}

	public void cargarArchivosPorSolicitud() {
		try {
			if (this.creditoPostventa != null) {
				this.listaArchivos = archivoCrePostventaService.obtenerArchivosPorSolicitud(creditoPostventa.getNoCia(),
						creditoPostventa.getNoSolicitud());
			}
		} catch (Exception e) {
			addInfoMessage("No se han encontrado archivos digitales para la solicitud", "");
		}
	}

	public void validarBotonesSolicitud() {
		if (this.creditoPostventa.getEstado().equals(EnumEstSolicitud.INGRESADO.getCodigo()) || this.creditoPostventa.getEstado().equals(EnumEstSolicitud.DEVUELTO.getCodigo())) {
			this.actBtnEnviar = false;
			this.activaTab = false;
			this.actBtnGuardar = false;
		} else {
			this.actBtnEnviar = true;
			this.activaTab = true;
			this.actBtnGuardar = true;
		}
	}

	public void calcularTotalDisponible() {
		totalGastos = 0.00;
		Double ingreso = (creditoPostventa.getIngreso() == null ? 0.00 : creditoPostventa.getIngreso());
		Double egreso = (creditoPostventa.getEgreso() == null ? 0.00 : creditoPostventa.getEgreso());

		if (egreso <= ingreso) {
			totalGastos = ingreso - egreso;
			creditoPostventa.setMontoDisponible(totalGastos);
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
		parameters.put("NO_SOLICITUD", new Long(creditoPostventa.getNoSolicitud()));
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
		centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.centro, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			addInfoMessage("No se han encontrado solicitudes para esta empresa", "");
		}
	}

	public void deleteFile() throws DeleteException, Exception {
		CxcCreDigPostventa regEliminar = getArchivoSel();
		if (this.creditoPostventa.getNoSolicitud() == null) {
			this.listaArchivosDigitales.remove(regEliminar);
		} else {
			archivoCrePostventaService.delete(regEliminar.getNoRegistro());
		}
		cargarArchivosPorSolicitud();
		if (this.listaArchivos == null) {
			info("No existen archivos registrados para esta solcitud");
			this.listaArchivosDigitales.clear();
		} else {
			this.listaArchivosDigitales = this.listaArchivos;
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
			logger.error("No se encontrado registros para obtener el tipo de clientes");
		}
	}

	protected String getStrConvenioPath() {
		urlReport = "/reportes/cxcConvenioCreditoComercialEmpresas.jasper";
		return urlReport;
	}

	private void setParametersConvenio(Map<String, Object> parameters) {
		parameters.put("NO_SOLICITUD", new Long(creditoPostventa.getNoSolicitud()));
		parameters.put("NO_CIA", getCompania().getNoCia());
		parameters.put("P_LOGO", obtenerLogoEmpresa());
	}

	private String obtenerLogoEmpresa() {
		String rutaLogoEmpresa = null;
		if (CommonConstants.CASABACA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoCasabaca.jpg");
		} else if (CommonConstants.TOYOCOSTAS.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoToyocosta.jpg");
		} else if (CommonConstants.NEXUMCORP.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoNexumcorp.jpg");
		} else if (CommonConstants.CARLOS_LARREA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logoCarloslarrea.jpg");
		} else if (CommonConstants.TOYOSERVICIOS.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/toyoser.jpg");
		} else if (CommonConstants.AMBAMAZDA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/ambamazda.jpg");
		} else if (CommonConstants.MANSUERA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logoMansuera.jpg");
		}
		return rutaLogoEmpresa;
	}

	@SuppressWarnings("unchecked")
	public void reportConvenioPdf() {
		Map parameters = new HashMap();
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

	public void validarConvenio() {
		this.convenio = false;
		if ("A".equals(this.creditoPostventa.getEstado())) {
			this.convenio = true;
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

	public CxcCreditoPostventaServiceLocal getCreditoPostventaService() {
		return creditoPostventaService;
	}

	public void setCreditoPostventaService(CxcCreditoPostventaServiceLocal creditoPostventaService) {
		this.creditoPostventaService = creditoPostventaService;
	}

	public CxcCreditoPostventa getCreditoPostventa() {
		return creditoPostventa;
	}

	public void setCreditoPostventa(CxcCreditoPostventa creditoPostventa) {
		this.creditoPostventa = creditoPostventa;
	}

	public CxcCreditoPostventa getCreditoPostventaSel() {
		return creditoPostventaSel;
	}

	public void setCreditoPostventaSel(CxcCreditoPostventa creditoPostventaSel) {
		this.creditoPostventaSel = creditoPostventaSel;
	}

	public List<CxcCreDigPostventa> getListaArchivosDigitales() {
		return listaArchivosDigitales;
	}

	public void setListaArchivosDigitales(List<CxcCreDigPostventa> listaArchivosDigitales) {
		this.listaArchivosDigitales = listaArchivosDigitales;
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

	public String getIdTipoCedula() {
		return idTipoCedula;
	}

	public void setIdTipoCedula(String idTipoCedula) {
		this.idTipoCedula = idTipoCedula;
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

	public String getDomicilioCliente() {
		return domicilioCliente;
	}

	public void setDomicilioCliente(String domicilioCliente) {
		this.domicilioCliente = domicilioCliente;
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

	public List<SelectItem> getEstadoCivilList() {
		return estadoCivilList;
	}

	public void setEstadoCivilList(List<SelectItem> estadoCivilList) {
		this.estadoCivilList = estadoCivilList;
	}

	public String getUrlReport() {
		return urlReport;
	}

	public void setUrlReport(String urlReport) {
		this.urlReport = urlReport;
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

	public boolean isActBtnEnviar() {
		return actBtnEnviar;
	}

	public void setActBtnEnviar(boolean actBtnEnviar) {
		this.actBtnEnviar = actBtnEnviar;
	}

	public Double getTotalGastos() {
		return totalGastos;
	}

	public void setTotalGastos(Double totalGastos) {
		this.totalGastos = totalGastos;
	}

	public List<CxcCreditoPostventa> getListaPreCreditos() {
		return listaPreCreditos;
	}

	public void setListaPreCreditos(List<CxcCreditoPostventa> listaPreCreditos) {
		this.listaPreCreditos = listaPreCreditos;
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

	public boolean isConvenio() {
		return convenio;
	}

	public void setConvenio(boolean convenio) {
		this.convenio = convenio;
	}

	public String getEstadoBusqueda() {
		return estadoBusqueda;
	}

	public void setEstadoBusqueda(String estadoBusqueda) {
		this.estadoBusqueda = estadoBusqueda;
	}

}