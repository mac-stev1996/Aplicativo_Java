package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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

import com.casabaca.common.CommonConstants;
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
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcCreDigPostventa;
import com.casabaca.cxc.ejb.modelo.CxcCreditoPostventa;
import com.casabaca.cxc.ejb.servicio.CxcCreDigPostventaServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcCreditoPostventaServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.EnumEstSolicitud;
import com.casabaca.prime.cxc.lazy.LDMSolicitudCreditoPostventa;
import com.casabaca.s3s.ejb.model.Agencia;
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
public class CxcConsultaCreditoPostventaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CxcConsultaCreditoPostventaController.class);

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

	private List<CxcCreditoPostventa> listaCreditos;
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
	private boolean actBtnEnviar = true;
	private Double totalGastos = 0.00;
	// NUEVO REQUERIMIENTO
	private CxcCreDigPostventa archivoSel = new CxcCreDigPostventa();
	private boolean habCampo;
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
	private String idTipoArchRev;
	// NUEVO REQUERIMIENTO
	private List<ParamDet> listaTipoBloqueo = new ArrayList<>();
	private List<SelectItem> listaCmbTipoBloqueo = new ArrayList<>();
	private Integer tipoBloqueo;
	// Req-37713 ReqCambios Solicitud Credito
	private boolean activaReport;
	private boolean actBtnGuardar;
	private List<CxcCreDigPostventa> listaArchivos;
	private List<CxcCreDigPostventa> listaArchivosRev;
	private List<Agencia> listaAgencias;
	private String codAgencia;
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
	private boolean convenio;
	private String estadoBusqueda;

	@PostConstruct
	public void init() throws Exception {
		estadoBusqueda="";
		if (getRequestParameter("noSolicitud") != null) {
			validarSolicitudSeleccionada();
			editarSolicitud(creditoPostventaSel);
		} else if (getRequestParameter("regresa") != null) {
			regresaListadoSolicitudes();
		} else {
			cargaListaSolicitudes();
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

	public void cargarClientes() {
		try {
			this.listarClientes = new ArrayList<>();
			listarClientes = clienteServiceLocal.buscarClientesPorParametros(0, 25, getCompania().getNoCia(),
					this.cedulaCliente, this.nombreCliente.toUpperCase());
		} catch (Exception e) {
			listarClientes = new ArrayList<>();
			logger.error(e);
			addInfoMessage("Error. ", " No se encontro la cedula " + cliente.getCedula()
					+ " cliente ingresado, por favor proceda a registrar los campos del cliente");
			RequestContext.getCurrentInstance().execute("PF('dlgClientes').hide();");
			this.cliente = new Cliente();
			this.nuevo = false;
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
					this.cliente = new Cliente();
				}
			} catch (FindException e) {
				addErrorMessage(e.getSummary(), e.getDetail());
			}
		} else {
			addErrorMessage("", "Por favor ingrese la cedula o RUC del cliente");
		}

	}

	public void buscarSolicitud() {
		this.noCia = getCompania().getNoCia();
		cargarAgencias();
		this.estado = new ArrayList<>();
		if (estadoBusqueda!=null) {
			this.estado.add(estadoBusqueda);
		}
		
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.codAgencia, this.estado,
					this.fechaDesde, this.fechaHasta, this.cedulaCliente);
		} catch (Exception e) {
			info("No se han encontrado Solicitudes de Credito con los criterios de busqueda seleccionados");
		}
	}

	public void guardarArchivoDigital(FileUploadEvent event) throws Exception {
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
				this.activaTab = true;
				this.creditoPostventa = itemSel;
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
					clienteServiceLocal.update(cliente);
				} else {
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
//					clienteServiceLocal.save(cliente);
				}
				this.creditoPostventa.setNoCia(getCompania().getNoCia());
				this.creditoPostventa.setUsuario(usuarioSesion);
				this.creditoPostventa.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
				this.creditoPostventa.setEstado(EnumEstSolicitud.INGRESADO.getCodigo());
				this.creditoPostventa.setFecha(new Date());
				this.creditoPostventa.setNoCia(getCompania().getNoCia());
				creditoPostventaService.create(creditoPostventa);
				this.activaTab = false;
				this.actBtnEnviar = false;
				addInfoMessage("Info", "La solicitud ha sido guardada exitosamente");
			} else {
//				clienteServiceLocal.update(cliente);
				this.creditoPostventa.setDomicilioCalle(domicilioCliente.toUpperCase());
				this.creditoPostventa.setDomicilioReferencia(refDomicilioCliente.toUpperCase());
				this.creditoPostventa.setTelefonoCel(celularCliente.toUpperCase());
				this.creditoPostventa.setTelefono(telefonoCliente.toUpperCase());
				this.creditoPostventa.setBanco(bancoCliente.toUpperCase());
				this.creditoPostventa.setNoCuenta(noCuentaCliente.toUpperCase());
				this.creditoPostventa.setTipoCuenta(tipoCuentaCliente.toUpperCase());
				this.creditoPostventa.setEstadoCivil(estCivilCliente.toUpperCase());
				this.creditoPostventa.setEmail(correoCliente.toUpperCase());
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
				if (idTipoCedula.equals("C") && cliente.getCedula().length() == 10) {
					this.cliente.getClientePK().setNoCia(getCompania().getNoCia());
					this.cliente.setTipoIdentificacion(idTipoCedula);
					this.cliente.setCedula(cedulaCliente);
					this.cliente.setNombres(nombreCliente);
					clienteServiceLocal.save(cliente);
					super.info("El usuario ha sido creado");
					this.nuevo = true;
				} else {
					addInfoMessage("Info", "La cedula del cliente debe tener 10 digitos, actualmente tiene "
							+ cliente.getCedula().length() + " (" + cliente.getCedula() + ")");
				}
				if (idTipoCedula.equals("R") && cliente.getCedula().length() == 13) {
					this.cliente.getClientePK().setNoCia(getCompania().getNoCia());
					this.cliente.setTipoIdentificacion(idTipoCedula);
					this.cliente.setCedula(cedulaCliente);
					this.cliente.setNombres(nombreCliente);
					clienteServiceLocal.save(cliente);
					super.info("El usuario ha sido creado");
					this.nuevo = true;
				} else {
					addInfoMessage("Info", "El RUC del cliente debe tener 13 digitos, actualmente tiene "
							+ cliente.getCedula().length() + " (" + cliente.getCedula() + ")");
				}
				if (idTipoCedula.equals("P")) {
					this.cliente.getClientePK().setNoCia(getCompania().getNoCia());
					this.cliente.setTipoIdentificacion(idTipoCedula);
					this.cliente.setCedula(cedulaCliente);
					this.cliente.setNombres(nombreCliente);
					clienteServiceLocal.save(cliente);
					super.info("El usuario ha sido creado");
					this.nuevo = true;
				} else {
					addInfoMessage("Info", "El PASAPORTE del cliente no es válido " + cliente.getCedula() + ")");
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
		obtenerTodosCantones();
		obtenerCantonesGarante();
		cargarTipoCtasBancarias();
		cargarTipoVivienda();
		cargarListaBancos();
		cargarEstadoCivilList();
	}

	public void crearNuevoCliente() {
		this.cliente = new Cliente();
	}

	private void validarSolicitudSeleccionada() {
		String noSolicitud = super.getRequestParameter("noSolicitud");
		if (noSolicitud != null) {

			this.creditoPostventaSel = consultarSolicitudConsulta(getCompania().getNoCia(),
					Long.parseLong(super.getRequestParameter("noSolicitud")));
			validarBotonesSolicitud();
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
			this.validarConvenio();
		}

	}

	private CxcCreditoPostventa consultarSolicitudConsulta(String noCia, Long noSolicitud) {
		edit = true;
		try {
			creditoPostventa = creditoPostventaService.buscarSolicitudRevision(noCia, noSolicitud);
		} catch (Exception e) {
			logger.error("No se pudo cargar la informacion de las solicitudes de credito");
		}
		return creditoPostventa;
	}

	public void getUrlConsulta(CxcCreditoPostventa item) {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/formConsulta.jsf?noSolicitud="
					+ item.getNoSolicitud();
			redirect(url);
			creditoPostventaSel = item;
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void getUrlRegresa() {
		try {
			String url = "/cxc-web-prime/jsf/procesos/cxcSolicitudCreditoPostventa/listConsulta.jsf?regresa=true";
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
		cargarAgencias();
		try {
			this.model = new LDMSolicitudCreditoPostventa(0, SIZE, this.noCia, this.codAgencia, this.estado,
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
		if (!this.creditoPostventa.getEstado().equals(EnumEstSolicitud.INGRESADO.getCodigo())) {
			this.actBtnEnviar = true;
			this.activaTab = true;
			this.actBtnGuardar = true;
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
		cargarAgencias();
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
			logger.error("No se puede cargar tipos de clientes");
		}
	}

	public void cargarArchivosRevisionPorSolicitud() {
		String tipo = "R";
		try {
			if (this.creditoPostventa != null) {
				this.listaArchivosRev = archivoCrePostventaService.obtenerArchivosPorSolicitudRevisada(
						creditoPostventa.getNoCia(), creditoPostventa.getNoSolicitud(), tipo);
			}
		} catch (Exception e) {
			addInfoMessage("No se han encontrado archivos digitales de revision para la solicitud", "");
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
			logger.error("No se puede cargar tipo de bloqueo de clientes");
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
			logger.error("No se puede cargar las agencias de la empresa");
		}
	}

	protected String getStrConvenioPath() {
		urlReport = "/reportes/cxcConvenioCreditoComercialEmpresas.jasper";
		return urlReport;
	}

	private void setParametersConvenio(Map<String, Object> parameters) {
		parameters.put("NO_SOLICITUD", Long.valueOf(creditoPostventa.getNoSolicitud()));
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
		Map<String, Object> parameters = new HashMap<>();
		setParametersConvenio(parameters);
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
		String ctxPath = getServletContext().getRealPath("/");
		String path = getStrConvenioPath();
		String path2 = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
				.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH") + System.getProperty("file.separator");
		try (Connection connection = utilServiceDelegate.getDataSource().getConnection();) {

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path2);
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
		} catch (SQLException | IOException | JRException e) {
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void obtenerCliente() {
		try {
			Cliente cliente = clienteServiceLocal.findByCedulaNoCia(this.creditoPostventa.getCedula(),
					getCompania().getNoCia());

			if (cliente != null) {
				setCliente(cliente);
				this.creditoPostventa.setCedula(cliente.getCedula());
				this.creditoPostventa.setNombres(cliente.getNombre());
			}
		} catch (Exception e) {
			addWarnMessage("Alerta", "No existen datos del cliente ingresado");
		}
	}

	public void consultarCuposCliente() {
		this.cupoDisponible = BigDecimal.ZERO;
		this.monto = BigDecimal.ZERO;
		this.creditoSolicitado = BigDecimal.ZERO;
		this.nuevoCupoDisponible = BigDecimal.ZERO;
		this.saldoVencido = BigDecimal.ZERO;
		try {
			List<CotizacionRepuesto> listCotiByCliente = cotizacionRepuestosServices
					.findByClienteAndEstadoPediente(getCompania().getNoCia(), this.cliente.getCedula(), null);

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
				creditoSolicitado = (creditoPostventa.getCupoSugerido());
				nuevoCupoDisponible = cupoDisponible.subtract(creditoSolicitado);
			} else {
				cupoDisponible = new BigDecimal((double) this.cliente.getLimiteCredi());
				creditoSolicitado = (creditoPostventa.getCupoSugerido());
				nuevoCupoDisponible = cupoDisponible.subtract(creditoSolicitado);
			}
			this.saldoVencido = creditoPostventaService.obtenerSaldosVencidosCliente(getCompania().getNoCia(),
					creditoPostventa.getCedula());
		} catch (Exception e) {
			logger.error("No se pudo consultar cupos de cliente", e.getCause());
		}

	}

	public void validarConvenio() {
		this.convenio = false;
		if (EnumEstSolicitud.APROBADO.getCodigo().equals(this.creditoPostventa.getEstado())) {
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

	public List<CxcCreditoPostventa> getListaCreditos() {
		return listaCreditos;
	}

	public void setListaCreditos(List<CxcCreditoPostventa> listaCreditos) {
		this.listaCreditos = listaCreditos;
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

	public CotizacionRepuesto getCotizacionRepuesto() {
		return cotizacionRepuesto;
	}

	public void setCotizacionRepuesto(CotizacionRepuesto cotizacionRepuesto) {
		this.cotizacionRepuesto = cotizacionRepuesto;
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