package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.VendedorServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CitaEntregaSugarDto;
import com.casabaca.cxc.ejb.dto.FiltrosConServicioDto;
import com.casabaca.cxc.ejb.dto.SeguimientoVehNuevosDto;
import com.casabaca.cxc.ejb.modelo.CxcContactoServicio;
import com.casabaca.cxc.ejb.servicio.CxcContactoServicioServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.ContactoServicioCommonController;
import com.casabaca.prime.cxc.common.EnumEntregaVeh;
import com.casabaca.prime.cxc.common.lazy.LazyClientesDataModel;
import com.casabaca.prime.cxc.lazy.LDMSeguimientoVehNuevos;
import com.casabaca.prime.cxc.lazy.LazyDataModelVendedores;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.servicios.ejb.service.SeTecniAsesorServiceLocal;
import com.casabaca.rest.wsIntegracionCRM.entidad.BodyInCitaEntregaCRM;
import com.casabaca.rest.wsIntegracionCRM.entidad.CitaEntrega;
import com.casabaca.rest.wsIntegracionCRM.entidad.CitasEntregaList;
import com.casabaca.webservice.wsIntegracionCRM.CitaEntregaCrm;
import com.casabaca.webservice.wsIntegracionCRM.impl.CitaEntregaCrmImpl;

/**
 * <b> Clase controlador para obtener politica de proteccion de datos de
 * cotizacion de clientes. </b>
 * 
 * @author luis.soria
 * @version $1.0$
 */
@ViewScoped
@ManagedBean(name = "cxcContactoServicioController")
public class CxcContactoServicioController extends ContactoServicioCommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CxcContactoServicioController.class);
	private static final String VAR_LISTADO = "LISTADO";	
	private static final String VAR_ENTREGAS = "ENTREGAS";
	private static final String INIT_TIPO_EVENTO_PROCESO="SEGUIMIENTO";
	private static final String CADENA_VACIA = null;
	private static final String FORMATO_FECHA_CRM = "yyyy-MM-dd HH:mm:ss";
	private static final String ORIGEN_SEGUIMIENTO_VEHICULOS = "seguimientoVehiculosNuevos";

	@EJB(lookup = NombreJNDI.CXC_CONTACTO_SERVICIO_SERVICE)
	private CxcContactoServicioServiceLocal contactoService;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN)
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaService;

	@EJB(lookup = NombreJNDI.SETECNIASESOR_SERVICE_LOCAL)
	private SeTecniAsesorServiceLocal seTecniAsesorService;

	@EJB(lookup = NombreJNDI.VENDEDOR_SERVICIO_BEAN)
	private VendedorServicioLocal vendedorService;

	//private String usuarioSesion;
	 
	private List<String> lineasVeh;
	// Datos Generales
	
	private String agencia;
	// Controles
	
	private Date fechaDesde;
	private Date fechaHasta;

	// CLientes
	private LazyDataModel<Cliente> lazyModel;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private String cedulaCliente;
	private String nombreCliente;

	// Vendedores
	private LazyDataModelVendedores lazyDataModelVendedores;
	private boolean entregaVehiculoGuardada;

	@PostConstruct
	public void init() {
		logger.info("init...");
		try {
			this.pantalla = getRequestParameter("pantalla") != null ? getRequestParameter("pantalla") : VAR_LISTADO;			
			this.noCia = getCompania().getNoCia();
			this.agencia = getUsuarioCentroConectado().getAgencia().getAgenciaPK().getCodigo();
			this.usuarioSesion = getUsuario().getUsuario();
			this.fechaDesde=FechaUtils.sumarNDias(new Date(), -90);
			this.fechaHasta=new Date();
			this.entregaVehiculoGuardada = false;
			setContactoSer(new CxcContactoServicio());
			setSeguimientoVehBusqueda(new SeguimientoVehNuevosDto());
			getSeguimientoVehBusqueda().setNoCia(noCia);
			getSeguimientoVehBusqueda().setNoLinea(EnumEntregaVeh.LINEA_VEH_NUEVOS.getCodigo());		  
			this.listaContactos = new ArrayList<>();
			this.listaContactosServicio = new ArrayList<>();
			this.listaContactosVehiculos = new ArrayList<>();
			this.listaAsesoresServicio = new ArrayList<>();
			this.cargarLineaNegocio();
			this.consultarSeguimientos();			
		} catch (Exception e) {
			error("Error al cargar información inicial de vehiculos");
		}
	}
	

	public void asignarEntregaVehiculos(SeguimientoVehNuevosDto itemSel) {
		if (itemSel != null) {
			this.entregaVehiculoGuardada = false;
			setSeguimientoVeh(itemSel);
			this.pantalla = getRequestParameter("pantalla") != null ? getRequestParameter("pantalla") : VAR_ENTREGAS;			
			this.consultarEntregasVehiculos(seguimientoVeh.getNoCia(), seguimientoVeh.getNoCliente(),
					seguimientoVeh.getPlaca());
		} else {
			error("Se debe seleccionar un registro");
		}
	}

	public void verPantallaCliente(SeguimientoVehNuevosDto itemSel) {
		Date fechaInicio = FechaUtils.convertirStrFecha("01/01/2000", FechaUtils.patronDiaMesAnio);
		Date fechaFin = new Date();
		seguimientoVeh = itemSel;
		if (seguimientoVeh != null && seguimientoVeh.getNoCliente() != null) {
			StringBuilder parametros = new StringBuilder("noCia=").append(seguimientoVeh.getNoCia())
					.append("&fechaIni=")
					.append(fechaInicio != null ? FechaUtils.formatearFecha(fechaInicio, FechaUtils.patronDiaMesAnio)
							: "")
					.append("&fechaFin=")
					.append(fechaFin != null ? FechaUtils.formatearFecha(fechaFin, FechaUtils.patronDiaMesAnio) : "")
					.append("&codigoCliente=").append(seguimientoVeh.getNoCliente()).append("&placa=")
					.append(seguimientoVeh.getPlaca()).append("&origen=").append(ORIGEN_SEGUIMIENTO_VEHICULOS)
					.append("&accion=EDIT");
			try {
				super.redirect("/cxc/faces/jsp/mantenimiento/cliente/clienteForm.jsp?" + parametros.toString());
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				error("Se presento un error al redireccionar página");
			}
		} else {
			warn("Primero debe seleccionar un registro");
		}
	}
	

	public void regresar() {
		logger.info("regresar...");
		this.init();
	}

	private void consultarEntregasVehiculos(String noCia, Long noCliente, String placa) {
		try {
			this.contactoSer = contactoService.consultarEntregasVehiculos(noCia, noCliente, placa);
			if (contactoSer == null) {
				setContactoSer(new CxcContactoServicio());
			}
		} catch (FindException e) {
			logger.error("No se encontro informacion del contacto" + e);
		}
	}

		
	public void consultarSeguimientos() {
		this.obtenerLineasVehiculos();
		try {
			FiltrosConServicioDto objFiltroServicio = new FiltrosConServicioDto();
			objFiltroServicio.setUsuAsesor(null);
			objFiltroServicio.setCedCliente(this.cedulaCliente!=null?this.cedulaCliente:null);
			objFiltroServicio.setFechaIinicio(this.fechaDesde);
			objFiltroServicio.setFechaFin(this.fechaHasta);
			objFiltroServicio.setAplicaContacto(false);
			objFiltroServicio.setRolEvento(INIT_TIPO_EVENTO_PROCESO);
			this.model = new LDMSeguimientoVehNuevos(0, getBatchSize(), this.noCia, this.agencia, this.lineasVeh,
					objFiltroServicio);
		} catch (Exception e) {
			addInfoMessage("No se han encontrado vehiculos para esta empresa", "");
		}
	}

	
	public void buscarVehiculos() {		
		try {
			if(validarRangoFechas()) {
				this.construirCondicionesBusqueda(this.noLinea, this.fechaDesde);
				FiltrosConServicioDto objFiltroServicio = new FiltrosConServicioDto();
				objFiltroServicio.setUsuAsesor(null);
				objFiltroServicio.setCedCliente(this.cedulaCliente!=null?this.cedulaCliente:null);
				objFiltroServicio.setFechaIinicio(this.fechaDesde);
				objFiltroServicio.setFechaFin(this.fechaHasta);
				objFiltroServicio.setAplicaContacto(false);
				objFiltroServicio.setRolEvento(INIT_TIPO_EVENTO_PROCESO);
				this.model = new LDMSeguimientoVehNuevos(0, getBatchSize(), this.noCia, this.agencia, this.lineasVeh,
						objFiltroServicio);
			}		
		} catch (Exception e) {
			logger.error("No se encontro vehiculos con los criterios de busqueda" + e);
			info("No se han encontrado Vehiculos con los criterios de busqueda");
		}
	}
	
	private boolean validarRangoFechas() {
		boolean esRangoAceptable = true;
		if (this.fechaDesde == null || this.fechaHasta == null) {
			addErrorMessage("Error: ", "Debe ingresar las fechas de factura desde y hasta");
			return false;
		}
		if (this.fechaDesde.after(this.fechaHasta)) {
			esRangoAceptable = false;
			addErrorMessage("Error: ", "La fecha (desde) no debe ser mayor a la fecha (hasta)");			
		}
		Date fechaActual = new Date();
		if (this.fechaDesde.after(fechaActual) || this.fechaHasta.after(fechaActual)) {
			esRangoAceptable = false;
			addErrorMessage("Error: ", "Las fechas de factura no deben ser mayores a la fecha actual");
		}
		return esRangoAceptable;
	}

	public void actualizarFechaDesde() {
		if (this.fechaDesde != null && this.fechaHasta != null && this.fechaHasta.before(this.fechaDesde)) {
			this.fechaHasta = null;
		}
	}

	public void ejecutarConsultaClientes() {
		this.lazyModel = new LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(),
				getCedulaClienteConsulta(), getNombreClienteConsulta());
	}

	public void seleccionarCliente(Cliente cliente) {
		this.cedulaCliente = cliente.getCedula();
		this.nombreCliente = cliente.getNombre();
	}


	public void cargarLineaNegocio() {
		try {
			this.listaLineasNegocio = lineaService.buscarLineaNegocioVehListaInventario(noCia);
		} catch (Exception e) {
			info("No se han encontrado Lineas de Negocio para esta empresa");
		}
	}

	private void obtenerLineasVehiculos() {
		this.lineasVeh = new ArrayList<>();
		for (LineaNegocio linea : this.listaLineasNegocio) {
			String noLinea = linea.getLineaNegocioPK().getNoLinea();
			this.lineasVeh.add(noLinea);
		}
	}

	public void registrarEntregaVehiculo() {
		try {
			if (entregaVehiculoGuardada) {
				warn("La entrega de vehiculo ya fue guardada");
				return;
			}
			if (getContactoSer().getTipoContacto() != null
					&& EnumEntregaVeh.TIPO_ENTREGA.getCodigo().equals(getContactoSer().getTipoContacto())
					&& validarFechaEntregaVehiculo()) {
				getContactoSer().setUsuarioMod(usuarioSesion);
				getContactoSer().setFechaMod(new Date());
				contactoService.update(contactoSer);
				boolean citaEnviadaSugar = enviarCitaEntregaSugar(getContactoSer().getCodContacto());
				this.entregaVehiculoGuardada = true;
				info(construirMensajeEntregaGuardada("actualizada", citaEnviadaSugar));
			} else {
				if(validarFechaEntregaVehiculo()) {
					getContactoSer().setNoCia(this.noCia);
					getContactoSer().setCentro(this.agencia);
					getContactoSer().setNoCliente(seguimientoVeh.getNoCliente());
					getContactoSer().setNombreContacto(seguimientoVeh.getNombreCliente());
					getContactoSer().setPlaca(seguimientoVeh.getPlaca());
					getContactoSer().setChasis(seguimientoVeh.getChasis());
					getContactoSer().setFechaCrea(new Date());
					getContactoSer().setTipoContacto(EnumEntregaVeh.TIPO_ENTREGA.getCodigo());
					getContactoSer().setEstado(EnumEntregaVeh.INGRESADA.getCodigo());
					getContactoSer().setUsuarioCrea(usuarioSesion);
					contactoService.create(contactoSer);
					boolean citaEnviadaSugar = enviarCitaEntregaSugar(getContactoSer().getCodContacto());
					this.entregaVehiculoGuardada = true;
					info(construirMensajeEntregaGuardada("creada", citaEnviadaSugar));
				}				
			}
		} catch (InsertException e) {
			logger.error("No es posible ejecutar el metodo crear:registrarEntregaVehiculo()", e);
		} catch (UpdateException e) {
			logger.error("No es posible ejecutar el metodo actualizar:registrarEntregaVehiculo()", e);
		}
	}

	/**
	 * Metodo para enviar a Sugar CRM la cita de entrega registrada en S3S.
	 * Consulta los parametros PARCRM, valida si el flag de ejecucion esta activo,
	 * obtiene el token de la empresa y consume el servicio REST de citas de entrega.
	 *
	 * @param codContacto identificador de la entrega en CXC_CONTACTO_SERVICIO
	 */
	private boolean enviarCitaEntregaSugar(Long codContacto) {
		try {
			if (codContacto == null) {
				logger.warn("No se envia cita de entrega Sugar: codContacto nulo");
				return false;
			}
			CitaEntregaSugarDto datosCita = contactoService.consultarDatosCitaEntregaSugar(getCompania().getNoCia(),
					codContacto);
			if (datosCita == null) {
				logger.warn("No se envia cita de entrega Sugar: no se encontraron datos para codContacto "
						+ codContacto);
				return false;
			}
			BodyInCitaEntregaCRM body = construirBodyCitaEntrega(datosCita);
			CitaEntregaCrm citaEntregaCrm = new CitaEntregaCrmImpl();
			citaEntregaCrm.agendarCitaEntregaCrm(getCompania().getNoCia(), body);
			return true;
		} catch (FindException e) {
			logger.error("Ocurrio un error al cargar datos para el consumo del servicio de citas entrega CRM", e);
			warn("Entrega guardada, pero no se pudo consultar datos para enviar a Sugar");
		} catch (WebServiceException e) {
			logger.error("Ocurrio un error al consumir el servicio web de citas entrega CRM", e);
			warn("Entrega guardada, pero no se pudo enviar a Sugar");
		} catch (Exception e) {
			logger.error("No se ejecuto el servicio de citas entrega CRM", e);
			warn("Entrega guardada, pero no se ejecuto el envio a Sugar");
		}
		return false;
	}

	private String construirMensajeEntregaGuardada(String accion, boolean citaEnviadaSugar) {
		String mensaje = "Entrega de vehiculo " + accion + " con exito";
		if (citaEnviadaSugar) {
			mensaje = mensaje + ". Cita enviada a Sugar CRM";
		}
		return mensaje;
	}

	/**
	 * Metodo para construir el body JSON requerido por el endpoint de Sugar CRM:
	 * api/postCitasEntrega/agendar.
	 *
	 * @param datosCita datos recuperados desde CXC_CONTACTO_SERVICIO y tablas
	 *                  relacionadas
	 * @return trama de entrada para el servicio REST de citas de entrega CRM
	 */
	private BodyInCitaEntregaCRM construirBodyCitaEntrega(CitaEntregaSugarDto datosCita) {
		CitaEntrega citaEntrega = new CitaEntrega();
		citaEntrega.setIdentrega(datosCita.getIdentrega());
		citaEntrega.setIdpct(datosCita.getIdpct());
		citaEntrega.setIdCotizacion(datosCita.getIdCotizacion());
		citaEntrega.setFecha_entrega(formatearFechaCrm(datosCita.getFechaEntrega()));
		citaEntrega.setAgencia_entrega(datosCita.getAgenciaEntrega());
		citaEntrega.setTipoIdentificacionId(datosCita.getTipoIdentificacionId());
		citaEntrega.setCedula(datosCita.getCedula());
		citaEntrega.setNombres(datosCita.getNombres());
		citaEntrega.setApellidos(datosCita.getApellidos());
		citaEntrega.setRazonSocial(datosCita.getRazonSocial());
		citaEntrega.setAsesor_servicio(datosCita.getAsesorServicio());
		citaEntrega.setVehiculo_descripcion(datosCita.getVehiculoDescripcion());
		citaEntrega.setVehiculo_placa(datosCita.getVehiculoPlaca());
		citaEntrega.setCreadoPorUsername(datosCita.getCreadoPorUsername());
		citaEntrega.setCreadoPorNombres(datosCita.getCreadoPorNombres());
		citaEntrega.setFecha_creacion(formatearFechaCrm(datosCita.getFechaCreacion()));

		CitasEntregaList citasEntregaList = new CitasEntregaList();
		citasEntregaList.setCitaEntrega(Arrays.asList(citaEntrega));

		BodyInCitaEntregaCRM body = new BodyInCitaEntregaCRM();
		body.setCitasEntregaList(citasEntregaList);
		return body;
	}

	/**
	 * Metodo para formatear fechas con el patron requerido por Sugar CRM.
	 *
	 * @param fecha fecha a convertir
	 * @return fecha en formato yyyy-MM-dd HH:mm:ss
	 */
	private String formatearFechaCrm(Date fecha) {
		return fecha != null ? FechaUtils.formatearFecha(fecha, FORMATO_FECHA_CRM) : null;
	}
	
	
	private boolean validarFechaEntregaVehiculo() {
		boolean esFechaValida=true;
		SimpleDateFormat sdfo = new SimpleDateFormat(FechaUtils.patronFechaTiempo24);  
		try {		 
			if (getContactoSer().getFechaEntregaVeh() == null
					|| getContactoSer().getFechaEntregaVeh().toString().isEmpty()) {
				esFechaValida = false;
				error("La fecha (entrega vehículo) es obligatoria");
			} else {
				Date date1 = sdfo.parse(FechaUtils.formatearFecha(getContactoSer().getFechaEntregaVeh(), FechaUtils.patronFechaTiempo24));
				Date date2 = sdfo.parse(FechaUtils.formatearFecha(new Date(), FechaUtils.patronFechaTiempo24));  		
				if (date1.before(date2)) {
					esFechaValida = false;
					error("La fecha (entrega vehículo) no debe ser menor a la fecha actual");
				}
			}
		} catch (ParseException e) {
			logger.error("No es posible castear fechas metodo:validarFechaEntregaVehiculo()" + e);
		}   	
		return esFechaValida;
	}

	public void confirmarEntregaInformacion() {
		try {
			if (EnumEntregaVeh.OPCION_SI.getCodigo().equals(getContactoSer().getOpArriboCliente())) {
				getContactoSer().setUsuarioConfirmaEntr(usuarioSesion);
				getContactoSer().setUsuarioMod(usuarioSesion);
				getContactoSer().setFechaMod(new Date());
				getContactoSer().setEstado(EnumEntregaVeh.CONFIRMADA.getCodigo());
				contactoService.update(contactoSer);
				info("Confirmación de Entrega de Informacion a Clientes realizadas con exito");
			}
		} catch (UpdateException e) {
			logger.error("No es posible ejecutar el metodo actualizar:confirmarEntregaInformacion()" + e);
		}
	}

	public void limpiarFiltros() {
		this.fechaDesde=FechaUtils.sumarNDias(new Date(), -90);
		this.fechaHasta=new Date();
		this.seguimientoVehBusqueda = new SeguimientoVehNuevosDto();
		this.cedulaCliente = CADENA_VACIA;		
		this.nombreCliente=CADENA_VACIA;		
		this.noLinea = CADENA_VACIA;		 	
		this.consultarSeguimientos();
	}	

	private void construirCondicionesBusqueda(String noLinea, Date fecha) {
		if (noLinea != null && !noLinea.isEmpty()) {
			this.lineasVeh = new ArrayList<>();
			this.lineasVeh.add(noLinea);
		} else {
			this.obtenerLineasVehiculos();
		}
		if (fecha == null) {
			this.fechaDesde = getSeguimientoVehBusqueda().getFechaFactura();
		}
	}

	/***********************
	 * GETTERS AND SETTERS
	 ***********************/

	public LazyDataModelVendedores getLazyDataModelVendedores() {
		return lazyDataModelVendedores;
	}

	public void setLazyDataModelVendedores(LazyDataModelVendedores lazyDataModelVendedores) {
		this.lazyDataModelVendedores = lazyDataModelVendedores;
	}
	
	@Override
	public SeguimientoVehNuevosDto getSeguimientoVehBusqueda() {
		return seguimientoVehBusqueda;
	}
	@Override
	public void setSeguimientoVehBusqueda(SeguimientoVehNuevosDto seguimientoVehBusqueda) {
		this.seguimientoVehBusqueda = seguimientoVehBusqueda;
	}
	
	@Override
	public String getPantalla() {
		return pantalla;
	}
	@Override
	public void setPantalla(String pantalla) {
		this.pantalla = pantalla;
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

	public Date getFechaActual() {
		return new Date();
	}
	@Override
	public List<SeguimientoVehNuevosDto> getListaSegVehNuevos() {
		return listaSegVehNuevos;
	}
	@Override
	public void setListaSegVehNuevos(List<SeguimientoVehNuevosDto> listaSegVehNuevos) {
		this.listaSegVehNuevos = listaSegVehNuevos;
	}
	@Override
	public LDMSeguimientoVehNuevos getModel() {
		return model;
	}
	@Override
	public void setModel(LDMSeguimientoVehNuevos model) {
		this.model = model;
	}
	@Override
	public SeguimientoVehNuevosDto getSeguimientoVeh() {
		return seguimientoVeh;
	}
	@Override
	public void setSeguimientoVeh(SeguimientoVehNuevosDto seguimientoVeh) {
		this.seguimientoVeh = seguimientoVeh;
	}
	
	@Override
	public List<LineaNegocio> getListaLineasNegocio() {
		return listaLineasNegocio;
	}
	@Override
	public void setListaLineasNegocio(List<LineaNegocio> listaLineasNegocio) {
		this.listaLineasNegocio = listaLineasNegocio;
	}
	@Override
	public String getNoLinea() {
		return noLinea;
	}
	@Override
	public void setNoLinea(String noLinea) {
		this.noLinea = noLinea;
	}	

	public String getCedulaClienteConsulta() {
		return cedulaClienteConsulta;
	}

	public void setCedulaClienteConsulta(String cedulaClienteConsulta) {
		this.cedulaClienteConsulta = cedulaClienteConsulta;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getNombreClienteConsulta() {
		return nombreClienteConsulta;
	}

	public void setNombreClienteConsulta(String nombreClienteConsulta) {
		this.nombreClienteConsulta = nombreClienteConsulta;
	}

	public String getCedulaCliente() {
		return cedulaCliente;
	}

	public void setCedulaCliente(String cedulaCliente) {
		this.cedulaCliente = cedulaCliente;
	}

	public LazyDataModel<Cliente> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Cliente> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public boolean isEntregaVehiculoGuardada() {
		return entregaVehiculoGuardada;
	}

	public void setEntregaVehiculoGuardada(boolean entregaVehiculoGuardada) {
		this.entregaVehiculoGuardada = entregaVehiculoGuardada;
	}
	
}
