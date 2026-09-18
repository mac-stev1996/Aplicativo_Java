package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.Vendedor;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.VendedorServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.FiltrosConServicioDto;
import com.casabaca.cxc.ejb.dto.SeguimientoVehNuevosDto;
import com.casabaca.cxc.ejb.modelo.CxcContactoServicio;
import com.casabaca.cxc.ejb.servicio.CxcContactoServicioServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.ContactoServicioCommonController;
import com.casabaca.prime.cxc.common.EnumEntregaVeh;
import com.casabaca.prime.cxc.common.lazy.LazyClientesDataModel;
import com.casabaca.prime.cxc.lazy.LDMSeguimientoVehNuevos;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.servicios.ejb.modelo.SeTecniAsesor;
import com.casabaca.servicios.ejb.service.SeTecniAsesorServiceLocal;

/**
 * <b> Clase controlador para obtener politica de proteccion de datos de
 * cotizacion de clientes. </b>
 * 
 * @author luis.soria
 * @version $1.0$
 */
@ViewScoped
@ManagedBean(name = "cxcAsignacionVehiculosController")
public class CxcAsignacionVehiculosController extends ContactoServicioCommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(CxcAsignacionVehiculosController.class);
	private static final String VAR_LISTADO = "LISTA_ASIGNACION";
	private static final String VAR_CONTACTOS = "CONTACTOS";
	private static final String VAR_ASIGNACION_VEH = "ASIGNACION";
	private static final String VAR_NOTIFICA_ARRIBO="ARRIBO_CLIENTE";
	private static final String VAR_NOTIFICA_ENTRGA_MANUALES="ENTREGA_MANUALES";
	private static final String VAL_NO_REGISTRA="NO_REGISTRA";
	private static final String ORIGEN_ASIGNACION_VEHICULOS = "asignacionVehiculosNuevos";
	private static final String TIPO_CONTACTO_CLIENTE_VEHICULO="V";
	private static final String TIPO_CONTACTO_CLIENTE_SERVICIO="S";
	private static final String VAL_OPT_OTRA_AGENCIA="O";
	private boolean verBotonContactos;	
	private static final String CADENA_VACIA = null;

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
	
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;

	private String usuarioSesion;
	private CxcContactoServicio contactoSer;
	private CxcContactoServicio contactoSeleccionado;	
	private SeguimientoVehNuevosDto seguimientoVeh;
	private List<SeguimientoVehNuevosDto> listaSegVehNuevos;
	private List<CxcContactoServicio> listaContactos;
	private List<SeTecniAsesor> listaAsesoresServicio;
	private LDMSeguimientoVehNuevos model;
	private List<String> lineasVeh;
	private String noCia;
	private String centro;
	private String pantalla;
	private String noLinea;

	// Controles
	private boolean actGuardar;
	private boolean mostrarCamposPlaca;
	private Date fechaDesde;
	private Date fechaHasta;
	// CLientes
	private LazyDataModel<Cliente> lazyModel;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private String cedulaCliente;

	// Otra Agencia
	private List<Agencia> listaAgencias;
	private Vendedor vendedor;
	private boolean actAgencia;

	@PostConstruct
	public void init() {
		logger.info("init...");
		try {
			this.pantalla = getRequestParameter("pantalla") != null ? getRequestParameter("pantalla") : VAR_LISTADO;
			this.noCia = getCompania().getNoCia();
			this.centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
			this.usuarioSesion = getUsuario().getUsuario();
			setContactoSer(new CxcContactoServicio());
			setSeguimientoVehBusqueda(new SeguimientoVehNuevosDto());
			getSeguimientoVehBusqueda().setNoCia(noCia);
			getSeguimientoVehBusqueda().setNoLinea(EnumEntregaVeh.LINEA_VEH_NUEVOS.getCodigo());
			this.fechaDesde = new Date();
			this.fechaHasta = null;
			this.listaContactos = new ArrayList<>();
			this.listaContactosServicio = new ArrayList<>();
			this.listaContactosVehiculos = new ArrayList<>();
			this.listaAsesoresServicio = new ArrayList<>();
			this.listaAgencias = new ArrayList<>();
			this.vendedor = buscarVendedor();
			this.cargarLineaNegocio();
			this.consultarSeguimientos();
			this.actGuardar = true;		
			verBotonContactos=false;
		} catch (Exception e) {
			error("Error al cargar información inicial de vehiculos");
		}
	}

	private Vendedor buscarVendedor() {
		Vendedor objVendedor;
		try {
			objVendedor = vendedorService.findByUsuarioConectado(getCompania().getNoCia(),
					getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
		} catch (FindException e) {
			logger.log(Level.ERROR, "No es posible ejecutar metodo:buscarVendedor()" + e.getMessage());
			return null;
		}
		return objVendedor;
	}
 
	public void inicializarContactoServicio() {
		this.actGuardar = false;
		this.contactoSer = new CxcContactoServicio();
		getContactoSer().setNoCia(this.noCia);
		getContactoSer().setCentro(this.centro);
		getContactoSer().setEstado("A");
		getContactoSer().setUsuarioCrea(usuarioSesion);
		getContactoSer().setFechaCrea(new Date());
	}
	
	public void inicializarContactoPropietario() {
		this.actGuardar = false;
		this.contactoSer = new CxcContactoServicio();
		getContactoSer().setNoCia(this.noCia);
		getContactoSer().setCentro(this.centro);
		getContactoSer().setEstado("A");
		getContactoSer().setUsuarioCrea(usuarioSesion);
		getContactoSer().setFechaCrea(new Date());
		getContactoSer().setNoCliente(this.seguimientoVeh.getNoCliente());	
		getContactoSer().setNombreContacto(this.seguimientoVeh.getNombreCliente());
		getContactoSer().setTelefono(this.getSeguimientoVeh().getFonoCliente());
		getContactoSer().setEmail(this.getSeguimientoVeh().getEmailCliente());
		getContactoSer().setTipoContacto(EnumEntregaVeh.TIPO_VEH.getCodigo());
		getContactoSer().setEstado("A");
	}
 
	public void guardarContacto() {
		try {
			if (validarCampos()) {	
				if(contactoSer.getCodContacto()==null) {
					getContactoSer().setNoCliente(this.seguimientoVeh.getNoCliente());						 
					if (EnumEntregaVeh.TIPO_VEH.getCodigo().equals(getContactoSer().getTipoContacto())) {
						getContactoSer().setPlaca(seguimientoVeh.getPlaca());
					}				 
					contactoService.create(contactoSer);
					info("El contacto ha sido guardado exitosamente");
					RequestContext context = RequestContext.getCurrentInstance();
					context.execute("PF('diaContactosVeh').hide();");
				}else {
					getContactoSer().setFechaMod(new Date());
					getContactoSer().setUsuarioMod(usuarioSesion);
					if (EnumEntregaVeh.TIPO_VEH.getCodigo().equals(getContactoSer().getTipoContacto())) {
						getContactoSer().setPlaca(seguimientoVeh.getPlaca());
					}
					contactoService.update(contactoSer);
					info("El contacto ha sido actualizado exitosamente");
					RequestContext context = RequestContext.getCurrentInstance();
					context.execute("PF('diaContactosVeh').hide();");
				}
				verBotonContactos=true;
				actualizarListasContactos();
				limpiar();			
			} 			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			error("Error al guardar el contacto de servicio");
		}
	}

	 
	public void editar(SeguimientoVehNuevosDto itemSel) {
		try {
			if (itemSel != null) {
				setSeguimientoVeh(itemSel);
				this.pantalla = getRequestParameter("pantalla") != null ? getRequestParameter("pantalla")
						: VAR_CONTACTOS;
				cargarContactosRegistrados(seguimientoVeh);
				if (this.listaContactos != null) {
					getListaContactosServicio();
					getListaContactosVehiculos();
				}
			} else {
				error("Se debe seleccionar un registro");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void asignarEntregaVehiculos(SeguimientoVehNuevosDto itemSel) {
		if (itemSel != null) {
			setSeguimientoVeh(itemSel);
			this.pantalla = getRequestParameter("pantalla") != null ? getRequestParameter("pantalla")
					: VAR_ASIGNACION_VEH;
			this.consultarEntregasVehiculos(seguimientoVeh.getNoCia(), seguimientoVeh.getNoCliente(),
					seguimientoVeh.getPlaca());
			this.obtenerAsesores();
			this.cargarAgencias();
			this.actAgencia = VAL_OPT_OTRA_AGENCIA.equals(this.contactoSer.getTipoEntrega());
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
					.append(seguimientoVeh.getPlaca()).append("&origen=").append(ORIGEN_ASIGNACION_VEHICULOS)
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

	private void cargarAgencias() {
		try {
			String[] order = { "nombre" };
			this.listaAgencias = agenciaService.findByCompania(getCompania().getNoCia(), order);
			if (this.listaAgencias.isEmpty()) {
				error("No se puede cargar las agencias de la empresa");
				listaAgencias.clear();
			}
		} catch (Exception e) {
			error("Error al cargar las agencias que posee la empresa");
		}
	}

	public void habilitarCmbAgencia() {
		this.actAgencia = VAL_OPT_OTRA_AGENCIA.equals(this.contactoSer.getTipoEntrega());
		if(!this.actAgencia) {
			this.contactoSer.setCentroEntrega(null);
		}
	}

	private void consultarEntregasVehiculos(String noCia, Long noCliente, String placa) {
		try {
			this.contactoSer = contactoService.consultarEntregasVehiculos(noCia, noCliente, placa);
			if (contactoSer == null) {
				cargarEntregaDesdeSeguimiento();
			}
		} catch (FindException e) {
			logger.error("No se encontro informacion del contacto" + e);
		}
	}

	private void cargarEntregaDesdeSeguimiento() {
		setContactoSer(new CxcContactoServicio());
		if (this.seguimientoVeh != null) {
			try {
				if (this.seguimientoVeh.getCodContacto() != null) {
					CxcContactoServicio entregaRegistrada = contactoService.findByPk(this.seguimientoVeh.getCodContacto());
					if (entregaRegistrada != null && correspondeVehiculoSeleccionado(entregaRegistrada)) {
						setContactoSer(entregaRegistrada);
						return;
					}
					logger.warn("El codContacto " + this.seguimientoVeh.getCodContacto()
							+ " no corresponde al vehiculo seleccionado " + this.seguimientoVeh.getChasis());
				}
			} catch (FindException e) {
				logger.error("No se encontro entrega por codContacto " + this.seguimientoVeh.getCodContacto(), e);
			}
			getContactoSer().setNoCia(this.seguimientoVeh.getNoCia());
			getContactoSer().setCentro(this.centro);
			getContactoSer().setNoCliente(this.seguimientoVeh.getNoCliente());
			getContactoSer().setNombreContacto(this.seguimientoVeh.getNombreCliente());
			getContactoSer().setPlaca(this.seguimientoVeh.getPlaca());
			getContactoSer().setChasis(this.seguimientoVeh.getChasis());
			getContactoSer().setFechaEntregaVeh(this.seguimientoVeh.getFechaEntregaVeh());
			getContactoSer().setUsuarioEntregaVeh(this.seguimientoVeh.getUsuarioEntregaVeh());
			getContactoSer().setTipoContacto(EnumEntregaVeh.TIPO_ENTREGA.getCodigo());
		}
	}

	private boolean correspondeVehiculoSeleccionado(CxcContactoServicio entregaRegistrada) {
		if (!this.seguimientoVeh.getNoCia().equals(entregaRegistrada.getNoCia())
				|| !this.seguimientoVeh.getNoCliente().equals(entregaRegistrada.getNoCliente())) {
			return false;
		}
		String chasisSeleccionado = this.seguimientoVeh.getChasis();
		String chasisRegistrado = entregaRegistrada.getChasis();
		if (chasisSeleccionado != null && !chasisSeleccionado.trim().isEmpty()
				&& chasisRegistrado != null && !chasisRegistrado.trim().isEmpty()) {
			return chasisSeleccionado.trim().equalsIgnoreCase(chasisRegistrado.trim());
		}
		String placaSeleccionada = this.seguimientoVeh.getPlaca();
		String placaRegistrada = entregaRegistrada.getPlaca();
		return placaSeleccionada != null && placaRegistrada != null
				&& placaSeleccionada.trim().equalsIgnoreCase(placaRegistrada.trim());
	}

	public void obtenerAsesores() {
		this.listaAsesoresServicio = seTecniAsesorService.obtenerAsesoresPorCentro(this.noCia, this.centro);
	}

	private void limpiar() {
		this.contactoSer = new CxcContactoServicio();
	}
 
	public void consultarSeguimientos() {
		try {
			this.obtenerLineasVehiculos();
			this.cargarModeloSeguimientos();
		} catch (Exception e) {
			addInfoMessage("No se han encontrado vehiculos para esta empresa", "");
		}
	}

	 
	public void buscarVehiculos() {	
		try {
			if(validarRangoFechas()) {
				this.obtenerLineasVehiculos();
				this.cargarModeloSeguimientos();
			}
		} catch (Exception e) {
			logger.error("No se encontro vehiculos con los criterios de busqueda" + e);
			info("No se han encontrado Vehiculos con los criterios de busqueda");
		}
	}

	public void actualizarFechaDesde() {
		if (this.fechaDesde != null && this.fechaHasta != null && this.fechaHasta.before(this.fechaDesde)) {
			this.fechaHasta = null;
		}
	}

	private void cargarModeloSeguimientos() {
		FiltrosConServicioDto objFiltroServicio = new FiltrosConServicioDto();
		objFiltroServicio.setUsuAsesor(null);
		objFiltroServicio.setCedCliente(this.cedulaCliente);
		objFiltroServicio.setFechaIinicio(this.fechaDesde);
		objFiltroServicio.setFechaFin(this.fechaHasta);
		objFiltroServicio.setAplicaContacto(true);
		this.model = new LDMSeguimientoVehNuevos(0, getBatchSize(), this.noCia, this.centro, this.lineasVeh,
				objFiltroServicio);
	}
	
	private boolean validarRangoFechas() {
		boolean esRangoAceptable = true;
		Date fechaActual = new Date();
		if ((this.fechaDesde!=null && this.fechaHasta!=null) && (this.fechaDesde.compareTo(this.fechaHasta) > 0)) {
			esRangoAceptable = false;
			addErrorMessage("Error: ", "La fecha (desde) no debe ser mayor a la fecha (hasta)");			
		}
		if (this.fechaDesde != null && this.fechaDesde.compareTo(fechaActual) > 0) {
			esRangoAceptable = false;
			addErrorMessage("Error: ", "La fecha (desde) no debe ser mayor a la fecha actual");
		}
		return esRangoAceptable;
	}
	
	public void activarCargaContactoManual() {
		verBotonContactos=true;
	}

	public void seleccionarTipo() {
		String tipoContacto = contactoSer.getTipoContacto();
		this.mostrarCamposPlaca = EnumEntregaVeh.TIPO_VEH.getCodigo().equals(tipoContacto);
	}

	public void ejecutarConsultaClientes() {
		this.lazyModel = new LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(),
				getCedulaClienteConsulta(), getNombreClienteConsulta());
	}

	public void seleccionarCliente(Cliente cliente) {
		this.cedulaCliente = cliente.getCedula();
	}

	public void editarContacto() {
		CxcContactoServicio contactoEdit = getContactoSeleccionado();
		try {
			if (contactoEdit != null) {
				this.contactoSer = contactoEdit;
				this.seleccionarTipo();
			} else {
				error("Se debe seleccionar un contacto");
			}
		} catch (Exception e) {
			error(" No se pudo cargar registro:" + e.getMessage());
		}
	}
	
	public void eliminarContacto() {
		CxcContactoServicio regEliminado = getContactoSeleccionado();
		try {
			if (regEliminado != null) {
				contactoService.eliminar(regEliminado);
				info("Contacto eliminado con exito");
				init();
			} else {
				error("No se puede eliminar el contacto de servicio");
			}
		} catch (Exception e) {
			error("Ocurrio un problema al eliminar el registro");
		}
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

	private void cargarContactosRegistrados(SeguimientoVehNuevosDto vehSeguimiento) {
		try {
			if (vehSeguimiento != null) {
				this.listaContactos = contactoService.obtenerContactosPorCliente(noCia, vehSeguimiento.getNoCliente());
			}
		} catch (FindException e) {
			logger.error("No se encontro contactos de servicio registrados" + e);
		}

	}

	public void guardarEntregaVehiculo() {
		try {
			if (validarProcesoFinalizado()) {
				return;
			}
			if (validarEntregaVehiculoAsignacion() && validarTipoEntregaAgencia()) {
				getContactoSer().setUsuarioMod(usuarioSesion);
				getContactoSer().setFechaMod(new Date());
				getContactoSer().setFlujoProceso(EnumEntregaVeh.ESTADO_ASIGNADO.getCodigo());
				contactoService.update(contactoSer);
				info("Entrega de vehiculo asignada con exito");
			}
		} catch (UpdateException e) {
			logger.error("No es posible ejecutar metodo:guardarEntregaVehiculo()" + e);
		}
	}

	private boolean validarEntregaVehiculoAsignacion() {
		boolean entregaValida = true;
		if (getContactoSer() == null || getContactoSer().getCodContacto() == null) {
			entregaValida = false;
			addErrorMessage("Error: ", "No se encontro una entrega de vehiculo registrada para asignar");
		}
		if (getContactoSer() == null || getContactoSer().getFechaEntregaVeh() == null) {
			entregaValida = false;
			addErrorMessage("Error: ", "No existe fecha de entrega de vehiculo registrada");
		}
		if (getContactoSer() == null || getContactoSer().getUsuarioEntregaVeh() == null
				|| getContactoSer().getUsuarioEntregaVeh().isEmpty()) {
			entregaValida = false;
			addErrorMessage("Error: ", "Debe asignar un asesor para entrega de vehiculo");
		}
		if (getContactoSer() == null || getContactoSer().getTipoContacto() == null
				|| !EnumEntregaVeh.TIPO_ENTREGA.getCodigo().equals(getContactoSer().getTipoContacto())) {
			entregaValida = false;
			addErrorMessage("Error: ", "El registro seleccionado no corresponde a una entrega de vehiculo");
		}
		return entregaValida;
	}
	
	private boolean validarTipoEntregaAgencia() {
		boolean entregaValida = true;
		if (getContactoSer().getTipoEntrega() == null || getContactoSer().getTipoEntrega().isEmpty()) {
			entregaValida=false;
			addWarnMessage("Recuerde:", "Es necesario seleccionar el tipo de entrega");
		}
		if ((getContactoSer().getTipoEntrega() != null && !getContactoSer().getTipoEntrega().isEmpty() && getContactoSer().getTipoEntrega().equals(VAL_OPT_OTRA_AGENCIA) ) 
				&& (getContactoSer().getCentroEntrega()==null || getContactoSer().getCentroEntrega().isEmpty())) {
			entregaValida=false;
			addWarnMessage("Ha seleccionado tipo entrega:Otra Agencia", " Es necesario seleccionar la agencia");
		}
		return entregaValida;
	}
	
	private boolean validarProcesoSeguimientoVehiculos(String noCiaParam, Long noClienteParam) {
		boolean contactosRegistrados = false;
		try {
			List<CxcContactoServicio> listContactoClienteServicio = contactoService.obtenerContactosPorCliente(noCiaParam,
					noClienteParam);
			if (listContactoClienteServicio != null && !listContactoClienteServicio.isEmpty()) {
				CxcContactoServicio objContactosValidos = listContactoClienteServicio.stream()
						.filter(obj -> (TIPO_CONTACTO_CLIENTE_VEHICULO).equals(obj.getTipoContacto())
								|| (TIPO_CONTACTO_CLIENTE_SERVICIO).equals(obj.getTipoContacto()))
						.findAny().orElse(null);
				if (objContactosValidos != null) {
					contactosRegistrados = true;
				}
			}
			if (!contactosRegistrados) {
				addErrorMessage("Recuerde:", "Debe registrar al menos un contacto (servicio o  vehículo-usuario)");
			}
		} catch (FindException e) {
			logger.log(Level.ERROR,
					"No es posible retornar ejecutar metodo:validarProcesoSeguimientoVehiculos" + e.getMessage());
		}
		return contactosRegistrados;
	}
	
	private boolean validarArriboCliente(String arriboCliente) {
		boolean registraArriboCliente=false;
		if(EnumEntregaVeh.OPCION_SI.getCodigo().equals(arriboCliente)) {
			registraArriboCliente=true;
		}else {
			addErrorMessage("Recuerde:", "Debe haber notificado el arribo del cliente antes de confirmar su acercamiento");
		}		
		return registraArriboCliente;
	}
	
	private boolean camposAcercamientoObligatorio() {
		boolean registroCampos=false;
		if(getContactoSer().getOpEntrInfoCli()!=null && !getContactoSer().getOpEntrInfoCli().isEmpty() &&
			getContactoSer().getObservacionEntrInfo()!=null && !getContactoSer().getObservacionEntrInfo().isEmpty()	) {
			registroCampos=true;
		}else {
			addErrorMessage("Recuerde:", "Es obligatorio registrar un comentario y la confirmación de acercamiento");
		}
		return registroCampos;
	}

	public void confirmarEntregaInformacion() {
		try {
			if (validarProcesoFinalizado()) {
				return;
			}
			if (validarArriboCliente(getContactoSer().getOpArriboCliente()) && validarProcesoSeguimientoVehiculos(getContactoSer().getNoCia(),
					getContactoSer().getNoCliente()) && camposAcercamientoObligatorio()) {
				getContactoSer().setUsuarioConfirmaEntr(usuarioSesion);
				getContactoSer().setUsuarioMod(usuarioSesion);
				getContactoSer().setFechaMod(new Date());
				getContactoSer().setFechaEntrInfo(new Date());
				getContactoSer().setEstado(EnumEntregaVeh.CONFIRMADA.getCodigo());
				getContactoSer().setFlujoProceso(EnumEntregaVeh.ESTADO_CONFIRMADO.getCodigo());
				contactoService.update(contactoSer);
				info("Confirmación de Entrega de Informacion a Clientes realizadas con exito");
			}
		} catch (UpdateException e) {
			logger.error("No es posible ejecutar metodo:confirmarEntregaInformacion()" + e);
		}
	}

	public void notificarArriboCLiente() {
		SeguimientoVehNuevosDto veh = getSeguimientoVeh();
		try {
			this.consultarEntregasVehiculos(veh.getNoCia(), veh.getNoCliente(), veh.getPlaca());
			if (validarProcesoFinalizado()) {
				return;
			}
			if (getContactoSer().getUsuarioEntregaVeh() != null
					&& EnumEntregaVeh.TIPO_ENTREGA.getCodigo().equals(getContactoSer().getTipoContacto())) {
				getContactoSer().setUsuarioMod(usuarioSesion);
				getContactoSer().setFechaMod(new Date());
				getContactoSer().setFlujoProceso(EnumEntregaVeh.ESTADO_ARRIBO.getCodigo());
				getContactoSer().setOpArriboCliente(EnumEntregaVeh.OPCION_SI.getCodigo());
				getContactoSer().setUsuarioRegArribo(usuarioSesion);
				getContactoSer().setFechaArriboCliente(new Date());
				contactoService.update(contactoSer);
				info("Entrega de vehiculo actualizada con exito");
				enviarNotificacion(VAR_NOTIFICA_ARRIBO);
			} else {
				error("Debe asignar un Asesor para entrega de vehiculo.");
			}
		} catch (UpdateException e) {
			logger.error("No es posible ejecutar metodo:notificarArriboCLiente()" + e);
		}
	}
	
	public void notificarEntregaManualesGarantia() {
		SeguimientoVehNuevosDto veh = getSeguimientoVeh();
		this.consultarEntregasVehiculos(veh.getNoCia(), veh.getNoCliente(), veh.getPlaca());
		if (validarProcesoFinalizado()) {
			return;
		}
		if (getContactoSer().getUsuarioEntregaVeh() != null) {
			enviarNotificacion(VAR_NOTIFICA_ENTRGA_MANUALES);
		} else {
			error("Debe asignar un Asesor para entrega de vehiculo.");
		}
	}

	private boolean validarProcesoFinalizado() {
		boolean procesoFinalizado = getContactoSer() != null
				&& EnumEntregaVeh.CONFIRMADA.getCodigo().equals(getContactoSer().getEstado());
		if (procesoFinalizado) {
			addWarnMessage("Proceso finalizado:",
					"La entrega de información al cliente ya fue confirmada y no admite nuevas modificaciones o notificaciones");
		}
		return procesoFinalizado;
	}

	private void enviarNotificacion(String tipoNotificacion) {
		String nomAgencia = agenciaService.buscarNombreAgencia(this.noCia, this.centro);
		StringBuffer bodyMail = new StringBuffer();
		String mensajeNotifcaEnvio="";
		String subjectMail="";
		if (VAR_NOTIFICA_ARRIBO.equals(tipoNotificacion)) {
			bodyMail = mensajeMailArribo(nomAgencia);
			subjectMail="Proceso: Seguimiento de vehículos nuevos: Arribo de cliente ";
			mensajeNotifcaEnvio="Notificación de arribo cliente, enviado con éxito";
		} else {
			bodyMail = mensajeMailEntregaManual(nomAgencia);
			subjectMail="Proceso: Seguimiento de vehículos nuevos: entrega (manuales de garantía) ";
			mensajeNotifcaEnvio="Notificación entrega (manuales de garantia), enviado con éxito";
		}
		String mailJefePostVenta = VAL_NO_REGISTRA;
		String mailRespTaller = VAL_NO_REGISTRA;
		String mailAsesorServicio = VAL_NO_REGISTRA;
		String mailUsuarioRemitente = VAL_NO_REGISTRA;
		List<String> mailDestinatariosList = new ArrayList<String>();
		try {
			SisMailServidores sisMailServidores = getSisMailServidores(getCompania().getNoCia());
			Object[] datosUsuariosRespTallerJefePostVenta = contactoService.consultarDatosUsuariosMail(this.noCia,
					this.centro);
			if (datosUsuariosRespTallerJefePostVenta != null && datosUsuariosRespTallerJefePostVenta.length > 0) {
				if (datosUsuariosRespTallerJefePostVenta[0] != null
						&& !((String) datosUsuariosRespTallerJefePostVenta[0]).equals("")) {
					UsuarioSis responsableTaller = usuarioSisService
							.findByPk((String) datosUsuariosRespTallerJefePostVenta[0]);
					mailRespTaller = responsableTaller.getEmail();
					if(!VAL_NO_REGISTRA.equals(mailRespTaller)) {
						mailDestinatariosList.add(mailRespTaller);	
					}					
				}
				if (datosUsuariosRespTallerJefePostVenta[1] != null
						&& !((String) datosUsuariosRespTallerJefePostVenta[1]).equals("")) {
					UsuarioSis jefePostventa = usuarioSisService
							.findByPk((String) datosUsuariosRespTallerJefePostVenta[1]);
					mailJefePostVenta = jefePostventa.getEmail();
					if(!VAL_NO_REGISTRA.equals(mailJefePostVenta)) {
						mailDestinatariosList.add(mailJefePostVenta);
					}					
				}
			}
			if (getContactoSer().getUsuarioEntregaVeh() != null && !getContactoSer().getUsuarioEntregaVeh().isEmpty()) {
				UsuarioSis asesorServicio = usuarioSisService.findByPk(getContactoSer().getUsuarioEntregaVeh());
				mailAsesorServicio = asesorServicio.getEmail();
				if(!VAL_NO_REGISTRA.equals(mailAsesorServicio)) {
					mailDestinatariosList.add(mailAsesorServicio);
				}				
			}
			UsuarioSis objRemitente = usuarioSisServiceLocal.consultarDatosUsuario(getUsuario().getUsuario());
			mailUsuarioRemitente = objRemitente.getEmail();
			Stream<String> mailConcatena = mailDestinatariosList.stream();
			String mailDestinatariosConcatena = mailConcatena.collect(Collectors.joining(","));
			mailService.sendEmailInHtmlCC(sisMailServidores, sisMailServidores.getCuenta(), mailDestinatariosConcatena,
					mailUsuarioRemitente, subjectMail, bodyMail);
			info(mensajeNotifcaEnvio);
		} catch (FindException e) {
			error("Error al enviar el correo.");
		} catch (GeneralException e) {
			warn("Las notificaciones por correo no se realizaron.");
		}
	}

	private StringBuffer mensajeMailArribo(String nomAgencia) {
		StringBuffer mensaje = new StringBuffer();
				mensaje.append("Estimado(a), <br> Se informa que el cliente acaba de llegar a la agencia, prepararse para realizar la entrega del ").append(remplazarCaracteresEspeciales("vehículo")).append(" con los siguientes datos: <br><br><table border='1'><tr><td><b>").append(remplazarCaracteresEspeciales("Cédula: ")).
				append("Cliente: </b></td><td>")				 
				.append(getSeguimientoVeh().getCedulaCliente())
				.append("</td></tr><tr><td><b>Nombre Cliente </b></td> <td> ")
				.append(getSeguimientoVeh().getNombreCliente()).append("</td></tr><tr><td><b>Fecha: </b></td> <td> ")
				.append(FechaUtils.formatearFecha(getContactoSer().getFechaEntregaVeh(), FechaUtils.patronFechaTiempo24))
				.append("</td></tr><tr><td><b>Agencia: </b></td> <td> ").append(nomAgencia)
				.append("</td></tr><tr><td><b>Placa ").append(remplazarCaracteresEspeciales("Vehículo: ")).append("</b></td> <td> ").append(getSeguimientoVeh().getPlaca())
				.append("</td></tr><tr><td><b>Notificado por: </b></td> <td> ").append(getContactoSer().getUsuarioMod())
				.append("</td></tr></table><br><br> Por favor acercarse al punto de encuentro para realizar la entrega del " ).append(remplazarCaracteresEspeciales("vehículo")).append(", gracias.<br><br>");
		return mensaje;
	}
	
	private StringBuffer mensajeMailEntregaManual(String nomAgencia) {
		StringBuffer mensaje = new StringBuffer();
		mensaje.append("Estimado(a), <br> Se informa que el cliente acaba de llegar a la agencia, prepararse para realizar la entrega del ").append(remplazarCaracteresEspeciales("vehículo")).append(" con los siguientes datos: <br><br><table border='1'><tr><td><b>").append(remplazarCaracteresEspeciales("Cédula: ")).
		append("Cliente: </b></td><td>")				 
		.append(getSeguimientoVeh().getCedulaCliente())
		.append("</td></tr><tr><td><b>Nombre Cliente </b></td> <td> ")
		.append(getSeguimientoVeh().getNombreCliente()).append("</td></tr><tr><td><b>Fecha: </b></td> <td> ")
		.append(FechaUtils.formatearFecha(getContactoSer().getFechaEntregaVeh(), FechaUtils.patronFechaTiempo24))
		.append("</td></tr><tr><td><b>Agencia: </b></td> <td> ").append(nomAgencia)
		.append("</td></tr><tr><td><b>Placa ").append(remplazarCaracteresEspeciales("Vehículo: ")).append("</b></td> <td> ").append(getSeguimientoVeh().getPlaca())
		.append("</td></tr><tr><td><b>Notificado por: </b></td> <td> ").append(getContactoSer().getUsuarioMod())
		.append("</td></tr></table><br><br> Por favor acercarse al punto de encuentro para realizar la entrega de manuales de ").append(remplazarCaracteresEspeciales("garantía")).append(", gracias.<br><br>");
		return mensaje;
	}

	public void limpiarFiltros() {
		this.seguimientoVehBusqueda = new SeguimientoVehNuevosDto();
		this.cedulaCliente = CADENA_VACIA;
		setVendedor(null);		
		this.noLinea = CADENA_VACIA;
		setFechaDesde(new Date());
		setFechaHasta(null);
		this.consultarSeguimientos();
	}
	

	private void actualizarListasContactos() {
		this.obtenerContactosRegistrados();
		getListaContactosServicio();
		getListaContactosVehiculos();
	}

	private void obtenerContactosRegistrados() {
		try {
			this.listaContactos = contactoService.obtenerContactosPorCliente(noCia, contactoSer.getNoCliente());
		} catch (FindException e) {
			logger.error("No se encontro contactos de servicio registrados" + e);
		}

	}

	public List<CxcContactoServicio> getListaContactosServicio() {
		if (this.listaContactos != null) {
			// Asignar el resultado del filtro a la lista específica
			this.listaContactosServicio = this.listaContactos.stream()
					.filter(item -> EnumEntregaVeh.TIPO_SER.getCodigo().equals(item.getTipoContacto()))
					.collect(Collectors.toList());
		} else {
			this.listaContactosServicio = new ArrayList<>(); // Manejar caso de lista nula
		}
		return listaContactosServicio;
	}

	public List<CxcContactoServicio> getListaContactosVehiculos() {
		if (this.listaContactos != null) {
			// Asignar el resultado del filtro a la lista específica
			this.listaContactosVehiculos = this.listaContactos.stream()
					.filter(item -> EnumEntregaVeh.TIPO_VEH.getCodigo().equals(item.getTipoContacto()))
					.collect(Collectors.toList());
		} else {
			this.listaContactosVehiculos = new ArrayList<>(); // Manejar caso de lista nula
		}
		return listaContactosVehiculos;
	}

	/***********************
	 * GETTERS AND SETTERS
	 ***********************/
	@Override
	public CxcContactoServicio getContactoSer() {
		return contactoSer;
	}
	@Override
	public void setContactoSer(CxcContactoServicio contactoSer) {
		this.contactoSer = contactoSer;
	}	
	
	@Override
	public String getPantalla() {
		return pantalla;
	}
	@Override
	public void setPantalla(String pantalla) {
		this.pantalla = pantalla;
	}

	public boolean isActGuardar() {
		return actGuardar;
	}

	public void setActGuardar(boolean actGuardar) {
		this.actGuardar = actGuardar;
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
	public List<CxcContactoServicio> getListaContactos() {
		return listaContactos;
	}
	@Override
	public void setListaContactos(List<CxcContactoServicio> listaContactos) {
		this.listaContactos = listaContactos;
	}
   
	@Override
	public String getNoLinea() {
		return noLinea;
	}
	@Override	
	public void setNoLinea(String noLinea) {
		this.noLinea = noLinea;
	}

	public boolean isMostrarCamposPlaca() {
		return mostrarCamposPlaca;
	}

	public void setMostrarCamposPlaca(boolean mostrarCamposPlaca) {
		this.mostrarCamposPlaca = mostrarCamposPlaca;
	}

	public String getCedulaClienteConsulta() {
		return cedulaClienteConsulta;
	}

	public void setCedulaClienteConsulta(String cedulaClienteConsulta) {
		this.cedulaClienteConsulta = cedulaClienteConsulta;
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
	@Override
	public CxcContactoServicio getContactoSeleccionado() {
		return contactoSeleccionado;
	}
	@Override
	public void setContactoSeleccionado(CxcContactoServicio contactoSeleccionado) {
		this.contactoSeleccionado = contactoSeleccionado;
	}
	@Override
	public List<SeTecniAsesor> getListaAsesoresServicio() {
		return listaAsesoresServicio;
	}
	@Override
	public void setListaAsesoresServicio(List<SeTecniAsesor> listaAsesoresServicio) {
		this.listaAsesoresServicio = listaAsesoresServicio;
	}
 
	public List<Agencia> getListaAgencias() {
		return listaAgencias;
	}

	public void setListaAgencias(List<Agencia> listaAgencias) {
		this.listaAgencias = listaAgencias;
	} 

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	public boolean isActAgencia() {
		return actAgencia;
	}

	public void setActAgencia(boolean actAgencia) {
		this.actAgencia = actAgencia;
	}

	public boolean isVerBotonContactos() {
		return verBotonContactos;
	}

	public void setVerBotonContactos(boolean verBotonContactos) {
		this.verBotonContactos = verBotonContactos;
	}
	

}
