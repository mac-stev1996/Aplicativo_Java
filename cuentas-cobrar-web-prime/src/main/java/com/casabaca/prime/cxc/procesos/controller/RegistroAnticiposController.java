package com.casabaca.prime.cxc.procesos.controller;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.StringUtils;
import com.casabaca.common.ejb.dto.DigDocRepositorioDto;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.DigDocumentos;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.LineaNegocioPK;
import com.casabaca.common.ejb.model.MaestroCuenta;
import com.casabaca.common.ejb.service.BancoServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.DigDocRepositorioRegistradorServiceLocal;
import com.casabaca.common.ejb.service.DigDocumentoServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.MaestroCuentaServiceLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ReferenciaDocRepositorio;
import com.casabaca.common.ejb.util.RepositorioDoc;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.common.ejb.util.TipoEntidadDocRepositorio;
import com.casabaca.common.ejb.util.type.CommonEnums.EstadoAI;
import com.casabaca.cxc.cml.dao.CmlParentescoDaoLocal;
import com.casabaca.cxc.cml.model.CmlParentesco;
import com.casabaca.cxc.ejb.modelo.ConDepositos;
import com.casabaca.cxc.ejb.servicio.ConDepositosServiceLocal;
import com.casabaca.cxc.ejb.util.TipoTransaccion;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyClientesDataModel;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.vehiculos.ejb.modelo.ParametrosLinea;
import com.casabaca.vehiculos.ejb.modelo.TipoFinanciamiento;
import com.casabaca.vehiculos.ejb.servicio.ParametrosLineaServiceLocal;
import com.casabaca.vehiculos.ejb.servicio.TipoFinanciamientoServicioLocal;

@ViewScoped
@ManagedBean(name = "registroAnticiposController")
public class RegistroAnticiposController extends CommonController implements Serializable {
	
	private static final String VALOR_NO_APLICA = "No Aplica";
	private static final String VALOR_NA = "NA";
	private static final String FORMULARIO_TERCERO = "FORMULARIO AUTORIZADO DEPOSITO TERCERO";
	private static final String AUTORIZACION_DEPOSITO = "AUTORIZACION DEPOSITO TERCERO";
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(RegistroAnticiposController.class);
	
	private static final String AUTORIZACION = "/cxc/autorizacionDepositoTercero";
	
	@EJB(lookup = NombreJNDI.BANCO_SERVICE)
	private BancoServiceLocal bancoServiceLocal;
	@EJB(lookup = NombreJNDI.MAESTRO_CUENTA_SERVICE_LOCAL)
	private MaestroCuentaServiceLocal maestroCuentaService;
	@EJB(lookup = NombreJNDI.CON_DEPOSITOS_SERVICE_BEAN)
	private  ConDepositosServiceLocal conDepositosService;
	@EJB(lookup = NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN)
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.DIG_DOCUMENTO_SERVICE)
	private DigDocumentoServiceLocal digDocumentoService;
	@EJB(lookup = NombreJNDI.DIG_DOC_REPOSITORIO_REGISTRADOR_SERVICE_BEAN)
	private DigDocRepositorioRegistradorServiceLocal digDocRegistrador;
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;
	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaService;
	@EJB(lookup = NombreJNDI.TIPO_FINANCIAMIENTO_SERVICIO_BEAN)
	private TipoFinanciamientoServicioLocal tipoFinanciamientoServicioLocal;
	@EJB(lookup = com.casabaca.common.ejb.util.NombreJNDI.PARAMETROS_LINEA_SERVICE_BEAN)
	private ParametrosLineaServiceLocal parametroLineaService;
	private CmlParentescoDaoLocal cmlParentescoDaoLocal;
	
	private Long codCliente;
	private String nombreCliente;
	private Cliente cliente;
	private LazyDataModel<Cliente> lazyModel;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private String pathReservacion;
	private Object[] archivo;
	private Object[] archivoTercero;
	private UploadedFile file;
	
	private ConDepositos conDepositos;
	private List<ConDepositos> lconDepositos;
	private List<SelectItem> listaTipoTransaccion;
	private List<MaestroCuenta> listMaestroCuenta;
	
	private List<UsuarioSis> listCajeros;
	private String CedulaCliente;
	private List<SelectItem> agenciasSelectItems;
	private List<SelectItem> listAreas;
	private List<LineaNegocio> listLineasNegocio;
	private List<CmlParentesco> listaParentescos;
	private boolean areaConfirmaDesbloqueada;
	private boolean terceroEncontrado;
	private boolean aplicaTerceros;
	private boolean terceroEsArcctf;
	private boolean areaConfirmaBloqueada;

	
	@PostConstruct
	public void init() throws ServiceLocatorException {
		try {
			cmlParentescoDaoLocal = (CmlParentescoDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlParentescoDaoBean");
		} catch (ServiceLocatorException e) {
			logger.error("Error al cargar servicio de parentescos");
		}
		this.conDepositos = new ConDepositos();
		this.conDepositos.setAreaConfirma("CONTABILIDAD");
		if (getCompania().getNoCia().equals(CommonConstants.CASABACA) 
				|| getCompania().getNoCia().equals(CommonConstants.SUZUKI)
				|| getCompania().getNoCia().equals(CommonConstants.COSTA1001)
				|| getCompania().getNoCia().equals(CommonConstants.EPICENTRO)){			
			if (this.conDepositos.getAgenciaCajero() == null ){
				this.conDepositos.setAgenciaCajero(super.getUsuarioCentroConectado().getAgencia().getAgenciaPK().getCodigo());
			}
		}
		this.archivo = null;
		this.archivoTercero = null;
		this.listCajeros = new ArrayList<>();
		pathReservacion = null;
		this.areaConfirmaBloqueada = false;
					
		this.listMaestroCuenta = this.maestroCuentaService.consultaMaestrosCuentaConfirmacionDeposito(getCompania().getNoCia());
		if(listMaestroCuenta != null && !listMaestroCuenta.isEmpty()) {
			LinkedHashMap<String, MaestroCuenta> list = new LinkedHashMap<String, MaestroCuenta>();
			for (MaestroCuenta cuenta : listMaestroCuenta) {
				list.put(cuenta.getBanco(), cuenta);
			}
			this.listMaestroCuenta = list.values().stream().collect(Collectors.toList());
		}
		
		cargarAgencias();
		cargarLineasNegocio();
		cargarParentesco();
		this.areaConfirmaDesbloqueada = false;
		this.aplicaTerceros = false;
		this.terceroEsArcctf = false;
		this.terceroEncontrado = false;
	}	
	
	private void cargarParentesco() {
		listaParentescos = new ArrayList<>();
		setListaParentescos(cmlParentescoDaoLocal.listarActivas(getCompania().getNoCia()));
	}

	public void seleccionarConDepositos(ConDepositos conDepositos){
		this.conDepositos = conDepositos;
	}
	
	public void seleccionarLineaNegocio() {
		if (this.conDepositos.getNoLinea() == null || "".equals(this.conDepositos.getNoLinea())) {
			this.conDepositos.setAreaConfirma(null);
			this.areaConfirmaDesbloqueada = false;
			this.areaConfirmaBloqueada = false;
			return;
		}

		Optional<LineaNegocio> lineaOpt = this.listLineasNegocio.stream()
				.filter(l -> l.getLineaNegocioPK().getNoLinea().equals(this.conDepositos.getNoLinea())).findFirst();

		if (lineaOpt.isPresent() && lineaOpt.get().getAreaConfirma() != null
				&& !lineaOpt.get().getAreaConfirma().trim().isEmpty()) {
			this.conDepositos.setAreaConfirma(lineaOpt.get().getAreaConfirma());
			this.areaConfirmaDesbloqueada = false;
			this.areaConfirmaBloqueada = esSuzuki();
		} else {
			this.conDepositos.setAreaConfirma(null);
			this.areaConfirmaDesbloqueada = true;
			this.areaConfirmaBloqueada = false;
			warn("La línea seleccionada no tiene área de confirmación parametrizada. Por favor seleccione manualmente.");
		}
	}
	
    public void guardar() {
		logger.info("guardar...");
		try {	
			String mensajeValidacion = validarCamposRequeridos();
			if (mensajeValidacion.trim().isEmpty()){
				String path = guardarDeposito();
				if (path != null) {
				    this.conDepositos.setPathArchivo(path);
				}
				if (this.aplicaTerceros && this.archivoTercero != null) {
					String pathTercero = guardarArchivoEnDigDoc(this.archivoTercero, this.conDepositos.getCedula(), AUTORIZACION_DEPOSITO, FORMULARIO_TERCERO);
					if (pathTercero != null) {
						this.conDepositos.setPathArchivoTercero(pathTercero);
					}
				}
				this.conDepositos.setNoCia(getCompania().getNoCia());				
				this.conDepositos.setFechaSolicitud(new Date());
				this.conDepositos.setUsuario(getUsuario().getUsuario());
				this.conDepositos.setEstado("S");
				this.conDepositos.setAgencia(super.getUsuarioCentroConectado().getAgencia().getAgenciaPK().getCodigo());
				this.conDepositos.setEnvioNo(1);
				this.conDepositosService.crearConDepositos(this.conDepositos);
				addInfoMessage("Exito", "Guardado exitoso, solicitud "+ this.conDepositos.getId());	
				envioMail(this.conDepositos);
				
				init();
			}
			else{
				addErrorMessage("Error", mensajeValidacion);
			}
		} catch (Exception e) {
			addErrorMessage("Error", "Error en el guardado " + e.getMessage() );
			logger.error(e.getMessage()+ " " + e.getCause());
		}		
	}	
    
    public void nuevoDeposito (){
		this.conDepositos = new ConDepositos();
	}
      
	public List<SelectItem> getListaTipoTransaccion() {
		
		listaTipoTransaccion = new ArrayList<SelectItem>();		
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.DEPOSITO_EFECTIVO.getTipo(), TipoTransaccion.DEPOSITO_EFECTIVO.getDescripcion()));
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.DEPOSITO_CHEQUE.getTipo(), TipoTransaccion.DEPOSITO_CHEQUE.getDescripcion()));
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.TRANSF_MISMO_BANCO.getTipo(), TipoTransaccion.TRANSF_MISMO_BANCO.getDescripcion()));
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.TRANSF_OTROS_BANCOS.getTipo(), TipoTransaccion.TRANSF_OTROS_BANCOS.getDescripcion()));
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.TRANSF_APP.getTipo(), TipoTransaccion.TRANSF_APP.getDescripcion()));
		if (CommonConstants.CASABACA.equals(getCompania().getNoCia())) {
			listaTipoTransaccion.add(new SelectItem(TipoTransaccion.RECAUDACION.getTipo(), TipoTransaccion.RECAUDACION.getDescripcion()));
		}

		return listaTipoTransaccion;
	}
	
	public void seleccionarClientePorCodigo(AjaxBehaviorEvent event) {
		if (event != null) {
			if (this.conDepositos.getNoLinea() == null || this.conDepositos.getNoLinea().trim().isEmpty()) {
				this.conDepositos.setCedula(null);
				warn("Debe seleccionar primero la Línea de Anticipo.");
				return;
			}
			try {
				Cliente clienteDeposito = clienteServiceLocal.findByCedulaNoCia(this.conDepositos.getCedula(), getCompania().getNoCia());
				if (clienteDeposito != null) {
					if (validarDireccionCliente(clienteDeposito)) {
						setCliente(clienteDeposito);
						this.conDepositos.setCedula(clienteDeposito.getCedula());
						this.conDepositos.setNombreCliente(clienteDeposito.getNombre());
					} else {
						this.conDepositos.setNombreCliente(null);
						setCliente(null);
						error("Los datos del cliente no estan completos, validar en mantenimiento de cliente");
					}
				} else {
					codCliente = null;
					nombreCliente = null;
					this.conDepositos.setNombreCliente(null);
					setCliente(null);
					warn("El cliente no está creado aún en el maestro de clientes.");
				}
			} catch (Exception e) {
				codCliente = null;
				nombreCliente = null;
				setCliente(null);
				warn("No existen datos del cliente ingresado");
			}
		}
	}

	private boolean validarDireccionCliente(Cliente clienteDeposito) {
		try {
			ParametrosLinea vehLinea = parametroLineaService.obtenerParametrosPorLinea(getCompania().getNoCia(), this.conDepositos.getNoLinea());
			if (vehLinea == null || !CommonConstants.YES_STRING_VALUE.equalsIgnoreCase(vehLinea.getAplicaControlCsc())) {
				return true;
			}
			if (clienteDeposito.getDireccion() == null || clienteDeposito.getDireccion().trim().isEmpty()) {
				return false;
			}
			return true;
		} catch (FindException e) {
			logger.error("No se encontraron parametros para la linea: " + this.conDepositos.getNoLinea());
			return true;
		}
	}

	public void seleccionarTerceroPorCodigo(AjaxBehaviorEvent event) {
		String cedula = this.conDepositos.getCedulaTerceros();
		this.conDepositos.setNombreTerceros(null);
		this.conDepositos.setApellidosTerceros(null);
		this.terceroEncontrado = false;
		this.terceroEsArcctf = false;
		
		if (cedula == null || cedula.trim().isEmpty()) {
			return;
		}
		if (cedula.length() != 10 && cedula.length() != 13) {
			warn("La cédula debe tener 10 dígitos o el RUC 13 dígitos.");
			return;
		}

		try {
			Optional<TipoFinanciamiento> tipoFinanOpt = tipoFinanciamientoServicioLocal.findByRuc(getCompania().getNoCia(), cedula);

			if (tipoFinanOpt.isPresent()) {
			    this.conDepositos.setNombreTerceros(tipoFinanOpt.get().getEntidad());
			    this.conDepositos.setApellidosTerceros(tipoFinanOpt.get().getEntidad());
			    this.terceroEncontrado = true;
			    this.terceroEsArcctf = true; 
			    return;
			}

			Cliente clienteTercero = clienteServiceLocal.findByCedulaNoCia(cedula, getCompania().getNoCia());
			if (clienteTercero != null) {
				this.conDepositos.setNombreTerceros(clienteTercero.getNombres());
				this.conDepositos.setApellidosTerceros(clienteTercero.getApellidos());
				this.terceroEncontrado = true;
			} else {
				addWarnMessage("Alerta", "No se encontró ningún registro. Ingrese los datos manualmente.");
			}
		} catch (Exception e) {
			logger.error("Error en seleccionarTerceroPorCodigo: " + e.getMessage(), e);
			addWarnMessage("Alerta", "No se encontró ningún registro. Ingrese los datos manualmente.");
		}
	}
	
	public void cambiarAplicaTerceros() {
	    if (!this.aplicaTerceros) {
	        this.conDepositos.setCedulaTerceros(null);
	        this.conDepositos.setNombreTerceros(null);
	        this.conDepositos.setApellidosTerceros(null);
	        this.conDepositos.setCodigoParentesco(null);
	        this.terceroEncontrado = false;
	        this.terceroEsArcctf = false;
	    }
	}

	public void seleccionarCliente(Cliente cliente) {
	    if (this.conDepositos.getNoLinea() == null 
	            || this.conDepositos.getNoLinea().trim().isEmpty()) {
	        warn("Debe seleccionar primero la Línea de Anticipo.");
	        return;
	    }
	    if (!validarDireccionCliente(cliente)) {
	        return;
	    }
	    logger.info("seleccionarCliente..." + cliente);
	    this.conDepositos.setCedula(null);
	    this.conDepositos.setNombreCliente(null);
	    this.conDepositos.setCedula(cliente.getCedula());
	    this.conDepositos.setNombreCliente(cliente.getNombre());
	}
	
	public void asignarArchivo(FileUploadEvent event) {
		this.file = event.getFile();
		this.conDepositos.setPathArchivo(this.file.getFileName());
		cargarArchivo();
	}	
		
	public void asignarArchivoTercero(FileUploadEvent event) {
	    this.file = event.getFile();
	    this.conDepositos.setPathArchivoTercero(this.file.getFileName());
	    try {
	        UploadedFile uf = this.file;
	        Object[] file = new Object[3];
	        file[0] = uf.getFileName();
	        file[1] = uf.getFileName().substring(uf.getFileName().lastIndexOf("."), uf.getFileName().length());
	        file[2] = uf.getInputstream();
	        this.archivoTercero = file;
	    } catch (IOException e) {
	        error("Error al cargar archivo de tercero");
	        logger.error("Error al cargar archivo de tercero: " + e.getMessage());
	    }
	}
	
	private void cargarArchivo() {
		try {
			UploadedFile uf = this.file;
			Object[] file = new Object[3];
			file[0] = uf.getFileName();
			file[1] = uf.getFileName().substring(uf.getFileName().lastIndexOf("."), uf.getFileName().length());
			file[2] = uf.getInputstream();
			this.setArchivo(file);
		} catch (IOException e) {
			error("Error al cargar archivo");
			logger.error("Error al cargar archivo:" + e.getMessage());
		}
	}

	private String guardarDeposito() throws IOException {
		return guardarArchivoEnDigDoc(this.getArchivo(), this.conDepositos.getCedula(), "CONFIRMACION DEPOSITOS", "INGRESO SOICITUD CONFIRMACION DEPOSITOS");
	}

	private String guardarArchivoEnDigDoc(Object[] file, String documento, String categoria, String observacion) throws IOException {
		String pathDestino = CommonConstants.VACIO;
		if (null != file) {
			InputStream inputStream = null;
			pathDestino = getCompania().getPathFileServer();
			try {
				inputStream = (InputStream) file[2];
				DigDocumentos digDocumento = new DigDocumentos();
				digDocumento.setNoCia(getCompania().getNoCia());
				digDocumento.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
				digDocumento.setLineaNegocio("CXC");
				digDocumento.setEntidad("CLIENTE");
				digDocumento.setFechaIngreso(new Date());
				digDocumento.setUsuarioIngreso(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				digDocumento.setDocumento(documento);
				digDocumento.setNombreArchivo((String) file[0]);
				digDocumento.setArchivo(inputStream);
				digDocumento.setPathFileServer(getCompania().getPathFileServer());
				digDocumento.setRucEmpresa(getCompania().getIdTributario());
				digDocumento.setCategoria(categoria);
				digDocumento.setObservacion(observacion);
				pathDestino = digDocumentoService.crearDocumento(digDocumento);

				DigDocRepositorioDto repoDto = new DigDocRepositorioDto();
				repoDto.setNoCia(getCompania().getNoCia());
				repoDto.setModulo("CXC");
				repoDto.setRepositorio(RepositorioDoc.DIG_DOCUMENTOS.getValor());
				repoDto.setDocumentoId(digDocumento.getCode());
				repoDto.setTipoDocumentoId(null);
				repoDto.setTipoDocumento(digDocumento.getCategoria());
				repoDto.setIdentificadorEntidadDoc(this.conDepositos.getCedula());
				repoDto.setTipoEntidadDoc(TipoEntidadDocRepositorio.IDENTIFICACION_CLIENTE.getValor());
				repoDto.setReferenciaDoc(ReferenciaDocRepositorio.REGISTRO_ANTICIPOS.getValor());
				repoDto.setEstado(EstadoAI.ACTIVO.getValue());
				repoDto.setUsuarioIngreso(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				repoDto.setFechaIngreso(new Date());
				digDocRegistrador.registrarAsync(repoDto);
			} catch (Exception e) {
				addErrorMessage(e.getMessage(), "");
			}
		}
		return pathDestino;
	}
	
	private String validarCamposRequeridos() {
		StringBuffer mensaje = new StringBuffer();
				
		if (getCompania().getNoCia().equals(CommonConstants.CASABACA) 
				|| getCompania().getNoCia().equals(CommonConstants.SUZUKI)
				|| getCompania().getNoCia().equals(CommonConstants.COSTA1001)
				|| getCompania().getNoCia().equals(CommonConstants.EPICENTRO)){			
			if (this.conDepositos.getAreaConfirma() == null ){			
				 mensaje.append(" Debe seleccionar un Area de confirmación.");
			}
		}
		
		if (this.conDepositos.getNoLinea() == null || "".equals(this.conDepositos.getNoLinea())){ 
			 mensaje.append(" Debe seleccionar una Linea de Anticipo.");
		}
		
		if (this.conDepositos.getBanco() == null || "".equals(this.conDepositos.getBanco())){ 
			 mensaje.append(" Debe seleccionar un banco.");
		}
		
		if (this.conDepositos.getTipoTransaccion() == null || "".equals(this.conDepositos.getTipoTransaccion())){ 
			 mensaje.append(" Debe seleccionar un tipo de transaccion.");
		}
		
		if (this.conDepositos.getFechaDocumento()==null){ 
			 mensaje.append(" Debe registrar una fecha de documento.");
		}		
		
		if (this.aplicaTerceros) {
		    if (this.conDepositos.getCedulaTerceros() == null 
		            || this.conDepositos.getCedulaTerceros().trim().isEmpty()) {
		        mensaje.append(" Debe ingresar la cédula del tercero.");
		    }
		    if (this.conDepositos.getNombreTerceros() == null 
		            || this.conDepositos.getNombreTerceros().trim().isEmpty()) {
		        mensaje.append(" Debe ingresar el nombre del tercero.");
		    }
		    if (this.conDepositos.getApellidosTerceros() == null 
		            || this.conDepositos.getApellidosTerceros().trim().isEmpty()) {
		        mensaje.append(" Debe ingresar los apellidos del tercero.");
		    }
		    if (!this.terceroEsArcctf 
		            && (this.conDepositos.getCodigoParentesco() == null 
		                || this.conDepositos.getCodigoParentesco().trim().isEmpty())) {
		        mensaje.append(" Debe seleccionar el parentesco del tercero.");
		    }
		}
		
		if (this.conDepositos.getTipoTransaccion() != null && this.conDepositos.getBanco() != null 
				&& this.conDepositos.getTipoTransaccion().equals(TipoTransaccion.TRANSF_OTROS_BANCOS.getTipo()) 
				&& this.conDepositos.getBanco().equals("PA")){
			if ("".equals(this.conDepositos.getNumeroDocumento())){
				this.conDepositos.setNumeroDocumento("NO IDENTIFICADO");
			}	
		}else{
			boolean validaNumero = true;
			if ("".equals(this.conDepositos.getNumeroDocumento())||this.conDepositos.getNumeroDocumento().equals("NO IDENTIFICADO")){			
				 mensaje.append(" Debe registrar un numero de documento.");
				 validaNumero = false;
			}
			else {
				if (!StringUtils.contieneSoloDigitos(this.conDepositos.getNumeroDocumento())){
					 mensaje.append(" Debe registrar un numero de documento, que contenga solo dígitos (0-9).");
					 validaNumero = false;
				}
			}		
			if(validaNumero) {
				List<ConDepositos> lsolDeposito = conDepositosService.validaConDepositos(getCompania().getNoCia(),this.conDepositos.getNumeroDocumento(), this.conDepositos.getBanco());		    
			    
				if (lsolDeposito.size() >0 ){ 
					String estado = "SOLICITADO";
					if(lsolDeposito.get(0).getEstado().equals("R")) {
						estado = "RECHAZADO, por favor revise el motivo de rechazo. No. Solicitud: " + lsolDeposito.get(0).getId();
					}else if(lsolDeposito.get(0).getEstado().equals("P")) {
						estado = "CONFIRMADO";
					}	
					mensaje.append(" Ya existe ese número de documento para este banco en estado " + estado);
				}
			}
		}
		
		if (this.conDepositos.getValorDocumento()==null){ 
			 mensaje.append(" Debe registrar un valor del documento.");
		}
		
		if ("".equals(this.conDepositos.getCedula())) {
			 mensaje.append(" Debe seleccionar un cliente.");
		}
		
		if (getCompania().getNoCia().equals(CommonConstants.CASABACA) 
				|| getCompania().getNoCia().equals(CommonConstants.SUZUKI)
				|| getCompania().getNoCia().equals(CommonConstants.COSTA1001)
				|| getCompania().getNoCia().equals(CommonConstants.EPICENTRO)){			
			if (this.conDepositos.getAgenciaCajero() == null ){			
				 mensaje.append(" Debe seleccionar una agencia.");
			}
		}
		
		if(null == this.getArchivo()) {
			 mensaje.append(" Debe seleccionar un archivo.");
		}
		
		if (null != this.conDepositos.getPathArchivo() && this.conDepositos.getPathArchivo().length()>=100) {
			mensaje.append(" El nombre del archivo no debe exceder la longitud permitida de 100 caracteres, por favor actualizar el nombre.");		
		}
		
		return mensaje.toString();
		
	}

	public void generarFormularioTercero() {
	    if (this.conDepositos.getValorDocumento() == null) {
	    	error("Debe registrar el valor antes de descargar el formulario.");
	        return;
	    } if (this.conDepositos.getNombreCliente() == null) {
	        error("Debe registrar el nombre del cliente antes de descargar el formulario.");
	        return;
	    } if (this.conDepositos.getNoLinea() == null) {
	    	error("Debe registrar la línea de negocio antes de descargar el formulario.");
	        return;
	    } if (!this.terceroEsArcctf
	            && (this.conDepositos.getCodigoParentesco() == null
	                || this.conDepositos.getCodigoParentesco().trim().isEmpty())) {
	        error("Debe seleccionar el parentesco del tercero antes de descargar el formulario.");
	        return;
	    }
	    try {
	        Map<String, Object> parametros = new HashMap<>();
	        parametros.put("p_fecha", new Date());
	        parametros.put("p_nombre_tercero", obtenerNombreTercero());
	        parametros.put("p_cedula_tercero", this.conDepositos.getCedulaTerceros());
	        parametros.put("p_valor", this.conDepositos.getValorDocumento());
	        parametros.put("p_cedula_cliente", this.conDepositos.getCedula());
	        parametros.put("p_nombre_cliente", this.conDepositos.getNombreCliente());
	        parametros.put("p_parentesco", obtenerParentesco(this.conDepositos.getCodigoParentesco()));
	        parametros.put("p_nombre_empresa", getCompania().getNombre());
	        super.ejecutarJavascript("openDuplicatedTab('" + super.callJasperReport(AUTORIZACION, parametros, CommonConstants.OUTPUT_PDF) + "');");
	    } catch (Exception e) {
	        addErrorMessage("Error al generar formulario de tercero", e.getMessage());
	        logger.error("Error generando formulario tercero: " + e.getMessage(), e);
	    }
	}

	private String obtenerNombreTercero() {
		if(this.conDepositos.getNombreTerceros().equalsIgnoreCase(this.conDepositos.getApellidosTerceros()))
			return this.conDepositos.getNombreTerceros();
		else 
			return this.conDepositos.getNombreTerceros() + " " + this.conDepositos.getApellidosTerceros();
	}

	private String obtenerParentesco(String codigoParentesco) {
		CmlParentesco parentesco = cmlParentescoDaoLocal.consultarPorId(getCompania().getNoCia(), codigoParentesco);
		return parentesco != null ? parentesco.getDescripcion() : VALOR_NA;
	}

	private void envioMail(ConDepositos conDepositos){	 
		//MAIL al grupo de Solicitudes de confirmación
		StringBuffer mensaje = new StringBuffer();
		mensaje.append("Se ha ingresado la solicitud de confirmacion de deposito o transferencia.<br><br>");
		mensaje.append("<table border='1'><tr><td><b>Datos Depósito </b></td><td>"
				+  conDepositos.getId()	+ "</td></tr>");
		mensaje.append("<tr><td><b>Fecha de depósito</b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy").format(conDepositos.getFechaDocumento()) 
				+ "</td></tr>");
		mensaje.append("<tr><td><b>Documento</b></td> <td> "
				+ conDepositos.getNumeroDocumento()	+ "</td></tr>");
		mensaje.append("<tr><td><b>Valor</b></td> <td> "
				+ conDepositos.getValorDocumento() + "</td></tr>");		
		mensaje.append("<tr><td><b>Cédula</b></td> <td> "
				+ conDepositos.getCedula() + "</td></tr>");
		mensaje.append("<tr><td><b>Nombre</b></td> <td> "
				+ conDepositos.getNombreCliente() + "</td></tr>");
		mensaje.append("<tr><td><b>Agencia</b></td> <td> "
				+ agenciaService.buscarNombreAgencia(getCompania().getNoCia(), conDepositos.getAgencia()) + "</td></tr>");
		if (conDepositos.getNoLinea().equals(VALOR_NA) || "".equals(conDepositos.getNoLinea())) {
			mensaje.append("<tr><td><b>Linea Anticipo</b></td> <td> "
					+ conDepositos.getNoLinea() + "</td></tr>");
		}
		else {
			mensaje.append("<tr><td><b>Linea Anticipo</b></td> <td> "
					+ conDepositos.getNoLinea() + " - " + lineaService.obtenerDescripcionLineaNeg(getCompania().getNoCia(), conDepositos.getNoLinea()) + "</td></tr>");
		}
		mensaje.append("</table>");
		mensaje.append("<br><br>Por favor revisar la solicitud en el sistema S3S .<br><br>");
		
		try {
			SisMailServidores sisMailServidores =  getSisMailServidores(conDepositos.getNoCia());
			if(conDepositos.getAreaConfirma() == null || conDepositos.getAreaConfirma().equals("CONTABILIDAD")) {
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SC"+getCompania().getNoCia());
				if(usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {				    
						mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),(String)objects[1],
								"Se ha ingresado la solicitud de confirmacion de deposito "+conDepositos.getId(), mensaje);
					}
				}
			}else {//SI el area es Credito, se envia al grupo SCDxx
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SCD"+getCompania().getNoCia());
				if(usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {				    
						mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),(String)objects[1],
								"Se ha ingresado la solicitud de confirmacion de deposito "+conDepositos.getId(), mensaje);
					}
				}
			}
			//Mail al asesor quien registra la solicitud
			mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),getUsuario().getEmail(),
					"Se ha ingresado la solicitud de confirmacion de deposito "+conDepositos.getId(), mensaje);
		} catch (GeneralException e) {
			error("Error al enviar correo");
			logger.error("Error al al enviar correo:" + e.getMessage());
		}
			
	}
	
	/**
	 * 
	 * <b> Metodo para cargar lineas de negocio que aplican para Anticipos . </b>
	 * <p>
	 * [Author luis.soria, 29 sep. 2023]
	 * </p>
	 *
	 */
	public void cargarLineasNegocio() {
	    try {
	        this.listLineasNegocio = lineaService.buscarLineaNegocioAnticipoCaja(getCompania().getNoCia());
	        if (!esSuzuki() && this.listLineasNegocio != null) {
	            LineaNegocio lineaNA = new LineaNegocio();
	            lineaNA.setLineaNegocioPK(new LineaNegocioPK());
	            lineaNA.getLineaNegocioPK().setNoLinea(VALOR_NA);
	            lineaNA.setDescripcion(VALOR_NO_APLICA);
	            this.listLineasNegocio.add(0, lineaNA);
	        }
	    } catch (FindException e) {
	        error("Error al cargar las lineas de negocio");
	        logger.error("Error al cargar las lineas de negocio de Anticipos de Caja");
	    }
	}
	
	public List<SelectItem> cargarAgencias() {
		String[] orderStrings = { "nombre" };
		List<Agencia> agenciasList = agenciaService.findByCompaniaAct(getCompania().getNoCia(), orderStrings);
		agenciasSelectItems = new ArrayList<>();
		if (agenciasList != null && !agenciasList.isEmpty()) {
			for (Agencia agencia : agenciasList) {
				agenciasSelectItems.add(new SelectItem(agencia.getAgenciaPK().getCodigo(), agencia.getNombre()));
			}
		}
		return agenciasSelectItems;
	}

	public List<MaestroCuenta> getListMaestroCuenta() {
		return listMaestroCuenta;
	}

	public void setListMaestroCuenta(List<MaestroCuenta> listMaestroCuenta) {
		this.listMaestroCuenta = listMaestroCuenta;
	}

	public List<SelectItem> getAgenciasSelectItems() {
		return agenciasSelectItems;
	}

	public void setAgenciasSelectItems(List<SelectItem> agenciasSelectItems) {
		this.agenciasSelectItems = agenciasSelectItems;
	}

	public List<SelectItem> getListAreas() {
		listAreas = new ArrayList<SelectItem>();
		listAreas.add(new SelectItem("CONTABILIDAD", "CONTABILIDAD"));
		listAreas.add(new SelectItem("CREDITO", "CREDITO"));
		return listAreas;
	}

	public void setListAreas(List<SelectItem> listAreas) {
		this.listAreas = listAreas;
	}

	public void setListaTipoTransaccion(List<SelectItem> listaTipoTransaccion) {
		this.listaTipoTransaccion = listaTipoTransaccion;
	}

	public List<LineaNegocio> getListLineasNegocio() {
		return listLineasNegocio;
	}

	public void setListLineasNegocio(List<LineaNegocio> listLineasNegocio) {
		this.listLineasNegocio = listLineasNegocio;
	}

	public String getPathReservacion() {
		return pathReservacion;
	}

	public void setPathReservacion(String pathReservacion) {
		this.pathReservacion = pathReservacion;
	}

	public List<UsuarioSis> getListCajeros() {
		return listCajeros;
	}

	public void setListCajeros(List<UsuarioSis> listCajeros) {
		this.listCajeros = listCajeros;
	}

	public Object[] getArchivo() {
		return archivo;
	}

	public void setArchivo(Object[] archivo) {
		this.archivo = archivo;
	}

	public List<ConDepositos> getLconDepositos() {
		return lconDepositos;
	}

	public ConDepositos getConDepositos() {
		return conDepositos;
	}

	public void setConDepositos(ConDepositos conDepositos) {
		this.conDepositos = conDepositos;
	}

	public void setLconDepositos(List<ConDepositos> lconDepositos) {
		this.lconDepositos = lconDepositos;
	}

	public void ejecutarConsultaClientes() {
		lazyModel = new LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(), getCedulaClienteConsulta(),
				getNombreClienteConsulta());
	}

	public String getCedulaCliente() {
		return CedulaCliente;
	}

	public void setCedulaCliente(String cedulaCliente) {
		CedulaCliente = cedulaCliente;
	}

	public Long getCodCliente() {
		return codCliente;
	}

	public void setCodCliente(Long codCliente) {
		this.codCliente = codCliente;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
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

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public LazyDataModel<Cliente> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Cliente> lazyModel) {
		this.lazyModel = lazyModel;
	}
	
	public boolean isAreaConfirmaDesbloqueada() {
	    return areaConfirmaDesbloqueada;
	}

	public List<CmlParentesco> getListaParentescos() {
		return listaParentescos;
	}

	public void setListaParentescos(List<CmlParentesco> listaParentescos) {
		this.listaParentescos = listaParentescos;
	}

	public boolean isTerceroEncontrado() {
		return terceroEncontrado;
	}

	public void setTerceroEncontrado(boolean terceroEncontrado) {
		this.terceroEncontrado = terceroEncontrado;
	}

	public boolean isAplicaTerceros() {
		return aplicaTerceros;
	}

	public void setAplicaTerceros(boolean aplicaTerceros) {
		this.aplicaTerceros = aplicaTerceros;
	}

	public boolean isTerceroEsArcctf() {
		return terceroEsArcctf;
	}

	public void setTerceroEsArcctf(boolean terceroEsArcctf) {
		this.terceroEsArcctf = terceroEsArcctf;
	}
	
	private boolean esSuzuki() {
	    return CommonConstants.SUZUKI.equals(getCompania().getNoCia());
	}
	
	public boolean isEsSuzuki() {
	    return esSuzuki();
	}

	public boolean isAreaConfirmaBloqueada() {
	    return areaConfirmaBloqueada;
	}

	public void setAreaConfirmaBloqueada(boolean areaConfirmaBloqueada) {
	    this.areaConfirmaBloqueada = areaConfirmaBloqueada;
	}
}