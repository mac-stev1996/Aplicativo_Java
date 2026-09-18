package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
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
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import com.casabaca.common.ejb.dto.DigDocRepositorioDto;
import com.casabaca.common.ejb.service.DigDocRepositorioRegistradorServiceLocal;
import com.casabaca.common.ejb.util.ReferenciaDocRepositorio;
import com.casabaca.common.ejb.util.RepositorioDoc;
import com.casabaca.common.ejb.util.TipoEntidadDocRepositorio;
import com.casabaca.common.ejb.util.type.CommonEnums.EstadoAI;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.StringUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ConfirmarDeposito;
import com.casabaca.common.ejb.model.DigDocumentos;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.MaestroCuenta;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.ConfirmarDepositoServiceLocal;
import com.casabaca.common.ejb.service.DigDocumentoServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.MaestroCuentaServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
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
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;

@ViewScoped
@ManagedBean
public class ActualizaAnticiposController extends CommonController implements Serializable{

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ActualizaAnticiposController.class);
	
	private static final String PARAM_USUARIOS_CONSULTA_CONFIRMACIONES_DEP = "USRCCD";
	
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;
	@EJB(lookup = NombreJNDI.MAESTRO_CUENTA_SERVICE_LOCAL)
	private MaestroCuentaServiceLocal maestroCuentaService;
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;
	@EJB(lookup = NombreJNDI.CON_DEPOSITOS_SERVICE_BEAN)	
	private  ConDepositosServiceLocal conDepositosService;
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.DIG_DOCUMENTO_SERVICE)
	private DigDocumentoServiceLocal digDocumentoService;
	@EJB(lookup = NombreJNDI.DIG_DOC_REPOSITORIO_REGISTRADOR_SERVICE_BEAN)
	private DigDocRepositorioRegistradorServiceLocal digDocRegistrador;
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;
	//Req-35779 CON AUTOMAT DEPOSITO Y TRANSFER
	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaService;
	@EJB(lookup = NombreJNDI.CONFIRMAR_DEPOSITO_SERVICE)
	private ConfirmarDepositoServiceLocal confirmarDepositoServiceLocal;

	private List<ConDepositos> listConDepositos;
	private LazyDataModel<ConDepositos> conDepositosLazyDataModel;
	private boolean consultaLista = false;

	private ConDepositos conDepositosBusqueda;
	private ConDepositos conDepositos;
	private Object[] archivo;
	private String pathArchivoAnterior;
	private UploadedFile file;
	private List<SelectItem> listaTipoTransaccion;
	private List<MaestroCuenta> listMaestroCuenta;
	private List<SelectItem> agenciasSelectItems;
	private List<SelectItem> listAreas;
	private String urlImagen;
	private String urlPathArchivoTercero;

	private LazyDataModel<Cliente> lazyModel;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private boolean puedeEditar;
	//Req-35779 CON AUTOMAT DEPOSITO Y TRANSFER
	private List<SelectItem> lineaSelectItems;
		
	
	@PostConstruct
	public void init() throws FindException, ServiceLocatorException {
		boolean usuarioAutorizado = buscaUsuarioAutorizadoConsulta();
		this.listConDepositos = new ArrayList<>();
		this.conDepositosBusqueda = new ConDepositos();
		this.conDepositosBusqueda.setNoCia(getCompania().getNoCia());
		//Si no es usuario autorizado, consulta solo los registrados por el usuario conectado(asesor)
		puedeEditar = false;
		if(!usuarioAutorizado) {
			puedeEditar = true;//Solo los asesores pueden editar sus solicitudes
			this.conDepositosBusqueda.setUsuario(getLoggedUsername());
		}
		this.conDepositosBusqueda.setFechaDesde(FechaUtils.sumarNDias(new Date(), -30));
		this.conDepositosBusqueda.setFechaHasta(new Date());
		
		this.cargarMaestroCuentas();
		
		this.consultaSolicitudesConfirmacion();
	}
	private boolean buscaUsuarioAutorizadoConsulta() {
		boolean usuarioAutorizado = false;
		try {
			List<ParamDet> resultado = paramDetService.consultarPorCodigoCab(getCompania().getNoCia(),
																			PARAM_USUARIOS_CONSULTA_CONFIRMACIONES_DEP);
			if(resultado != null && !resultado.isEmpty()) {
				usuarioAutorizado = resultado.stream().filter(x -> x.getTexto1().equals(getLoggedUsername())).findFirst().isPresent();
			}
		} catch (FindException e) {
			usuarioAutorizado = false;
		}
		return usuarioAutorizado;
		
	}
	private void cargarMaestroCuentas() {
		this.listMaestroCuenta = this.maestroCuentaService.consultaMaestrosCuentaConfirmacionDeposito(getCompania().getNoCia());
		if(listMaestroCuenta != null && !listMaestroCuenta.isEmpty()) {
			LinkedHashMap<String, MaestroCuenta> list = new LinkedHashMap<String, MaestroCuenta>();
			for (MaestroCuenta cuenta : listMaestroCuenta) {
				list.put(cuenta.getBanco(), cuenta);
			}
			this.listMaestroCuenta = list.values().stream().collect(Collectors.toList());
		}
	}
	public List<SelectItem> cargarAgencias() {
		String[] orderStrings = { "nombre" };
		List<Agencia> agenciasList = agenciaService.findByCompaniaAct(getCompania().getNoCia(), orderStrings);
		agenciasSelectItems = new ArrayList<SelectItem>();
		if (agenciasList != null && agenciasList.size() > 0) {
			for (Agencia agencia : agenciasList) {
				agenciasSelectItems.add(new SelectItem(agencia.getAgenciaPK().getCodigo(), agencia.getNombre()));
			}
		}
		return agenciasSelectItems;
	}
	/**
	 * Carga la lista de Solicitudes de confirmación de depositos registradas por el usuario conectado
	 */
	public void consultaSolicitudesConfirmacion() {
		logger.info("consultaSolicitudesConfirmacion...");
		try {
			if(this.conDepositosBusqueda.getFechaDesde() == null && this.conDepositosBusqueda.getFechaHasta() != null) {
				addErrorMessage("", "Ingrese la fecha desde.");
				consultaLista = true;
				return;
			}
			this.obtenerDepositos(0, getBatchSize());
			//Si no hay resultados se agrega un mensaje informativo
			if (getListConDepositos() == null || getListConDepositos().isEmpty()) {
				if (conDepositosLazyDataModel != null) {
					conDepositosLazyDataModel.setRowCount(0);
				}
				addInfoMessage("", "No existen registros.");
				
			}else {
				consultaLista = true;
				@SuppressWarnings("serial")
				LazyDataModel<ConDepositos> lazyConDepositos = new LazyDataModel<ConDepositos>() {
					@Override
					public List<ConDepositos> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
						// control para que consulte solo cuando sea necesario
						if (!consultaLista && FacesContext.getCurrentInstance().getCurrentPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
							logger.info(":buscar solicitudes desde: " + first);
							// Se selecciona el tipo de orden
							try {
								//Nueva consulta
								obtenerDepositos(first, pageSize);
							} catch (Exception e) {
								logger.error(e);
								addErrorMessage("","No existen registros.");
							} finally {
								
							}
						}
						consultaLista = false;
						return getListConDepositos();
					} 
				    @Override  
				    public Object getRowKey(ConDepositos dep) {  
				         return dep.getId();
				    } 
				    @Override  
				    public ConDepositos getRowData(String rowKey) {
				    	logger.info(":rowKey = " + rowKey);
				        return getListConDepositos().get(Integer.valueOf(rowKey) - 1);
				    } 
				};
				lazyConDepositos.setRowCount(this.consultarTotalRegistros());
				setConDepositosLazyDataModel(lazyConDepositos);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	public void obtenerDepositos(int firstResult, int maxResults){
		logger.info("obtieneDepositos..."+getCompania().getNoCia());
		try {
			this.listConDepositos = conDepositosService.consultarListConDepositos(conDepositosBusqueda, firstResult, maxResults);
			if(listConDepositos != null && !listConDepositos.isEmpty()) {
				HashMap<String, String> tiposTransaccion = obtenerTiposTransaccion();			
				
				for (ConDepositos conDepositos : listConDepositos) {
					if(listMaestroCuenta != null) {
						Optional<MaestroCuenta> result = listMaestroCuenta.stream().filter(x -> x.getBanco().equals(conDepositos.getBanco())).findFirst();
						if(result.isPresent()) {
							conDepositos.setNombreBanco(result.get().getNomBanco());
						}
					}
					conDepositos.setNombreTipoTransaccion(tiposTransaccion.get(conDepositos.getTipoTransaccion()));
					if(conDepositos.getCedula() != null){
						String nombre = null;
						try {
							nombre = clienteServiceLocal.buscarNombrePorCedulaNoCia(conDepositos.getCedula(), getCompania().getNoCia()).getNombre();
							conDepositos.setNombreCliente(nombre);
						} catch (FindException e) {
							addErrorMessage("Error al consultar cliente", e.getMessage());				
						}
					
					}
					if(conDepositos.getAgenciaCajero() != null) {
						String nombreAgencia = agenciaService.buscarNombreAgencia(getCompania().getNoCia(), conDepositos.getAgenciaCajero());
						conDepositos.setNombreAgenciaCajero(nombreAgencia);
					}
					//Req-35779 CON AUTOMAT DEPOSITO Y TRANSFER
					conDepositos.setNombreLinea("".equals(conDepositos.getNoLinea()) || conDepositos.getNoLinea() == null ? "N/A" : 
						lineaService.getNombreLineaNegocio(getCompania().getNoCia(),conDepositos.getNoLinea()));
					
					ConfirmarDeposito confirmarDep = new ConfirmarDeposito();
					try {
						confirmarDep = confirmarDepositoServiceLocal.obtenerConfirmacionDeposito(Long.valueOf(conDepositos.getId()).intValue());
						if (confirmarDep.getUsuarioUtiliza() != null) {
							String usuarioUtiliza = confirmarDep.getUsuarioUtiliza();
							conDepositos.setUsuarioUtiliza(usuarioUtiliza);
						} else {
							conDepositos.setUsuarioUtiliza(" ");
						}
						} catch (FindException e) {
							confirmarDep = null;
						}
				}
			}
		} catch (FindException e) {
			logger.error(e.getDetail(), e);
		}		
	}
	public int consultarTotalRegistros() {
		try {
			Long total = conDepositosService.consultarTotalConDepositos(conDepositosBusqueda);
			return total.intValue();
		} catch (FindException e) {
			logger.error(e.getDetail(), e);
		}
		return 0;
	}

	private HashMap<String, String> obtenerTiposTransaccion() {
		HashMap<String, String> tiposTransaccion = new HashMap<>();			
		tiposTransaccion.put(TipoTransaccion.DEPOSITO_EFECTIVO.getTipo(), TipoTransaccion.DEPOSITO_EFECTIVO.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.DEPOSITO_CHEQUE.getTipo(), TipoTransaccion.DEPOSITO_CHEQUE.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.TRANSF_MISMO_BANCO.getTipo(), TipoTransaccion.TRANSF_MISMO_BANCO.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.TRANSF_OTROS_BANCOS.getTipo(), TipoTransaccion.TRANSF_OTROS_BANCOS.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.TRANSF_APP.getTipo(), TipoTransaccion.TRANSF_APP.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.DEPOSITO_AGENCIA.getTipo(), TipoTransaccion.DEPOSITO_AGENCIA.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.RECAUDACION.getTipo(), TipoTransaccion.RECAUDACION.getDescripcion());
		return tiposTransaccion;
	}
	/**
	 * Para desplegar en la edición
	 * @return
	 */
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
	
	public void editar(ConDepositos item) {
		logger.info("editar...");
		this.conDepositos = item;
		this.pathArchivoAnterior = item.getPathArchivo();
		this.archivo = null;
		
		this.cargarAgencias();
		this.cargarLineasAnticipo();
		accionesDialog("dlgEditarValores", true);
	}

	public void actualizar() {
		logger.info("actualizar...");
		try {			
			String mensajeValidacion = validarCamposRequeridos();
			
			if (mensajeValidacion.trim().isEmpty()){
				String path = null;
				if(this.getArchivo() != null) {
					//Guarda archivo subido
					path = guardarImagen();
				}
				if(path!=null) {			
					this.conDepositos.setPathArchivo(path);
				}
				this.conDepositos.setEstado("S");
				Integer envios = this.conDepositos.getEnvioNo() != null?this.conDepositos.getEnvioNo():0;
				this.conDepositos.setEnvioNo(envios + 1);
				//this.conDepositos.setCondicion("Actualizado");
				
				conDepositosService.actualizarConDepositos(this.conDepositos);
			
				addInfoMessage("Exito", "Guardado exitoso, solicitud "+ this.conDepositos.getId());	
			    
				envioMail(this.conDepositos);
				
				accionesDialog("dlgEditarValores", false);
				
				this.consultaSolicitudesConfirmacion();
			}
			else{
				addErrorMessage("Error", mensajeValidacion);
			}
		} catch (Exception e) {
			addErrorMessage("Error", "Error en el guardado " + e.getMessage() );
			logger.error(e.getMessage()+ " " + e.getCause());
		}		
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
		if (this.conDepositos.getBanco() == null || "".equals(this.conDepositos.getBanco())){ 
			 mensaje.append(" Debe seleccionar un banco.");
		}
		
		if (this.conDepositos.getTipoTransaccion() == null || "".equals(this.conDepositos.getTipoTransaccion())){ 
			 mensaje.append(" Debe seleccionar un tipo de transaccion.");
		}
		
		if (this.conDepositos.getFechaDocumento()==null){ 
			 mensaje.append(" Debe registrar una fecha de documento.");
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
					//SI el codigo de la solicitud es diferente
					if(this.conDepositos.getId() != lsolDeposito.get(0).getId()) {
						String estado = "SOLICITADO";
						if(lsolDeposito.get(0).getEstado().equals("R")) {
							estado = "RECHAZADO. No. Solicitud: " + lsolDeposito.get(0).getId();
						}else if(lsolDeposito.get(0).getEstado().equals("P")) {
							estado = "CONFIRMADO";
						}	
						mensaje.append(" Ya existe ese número de documento para este banco en estado " + estado);
					}
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
				|| getCompania().getNoCia().equals(CommonConstants.EPICENTRO) ){			
			if (this.conDepositos.getAgenciaCajero() == null ){			
				 mensaje.append(" Debe seleccionar una agencia.");
			}
		}
		
		if(this.conDepositos.getPathArchivo() == null) {
			 mensaje.append(" Debe seleccionar un archivo.");
		}
		
		return mensaje.toString();
		
	}
	public void asignarArchivo(FileUploadEvent event) {
		this.file = event.getFile();
		this.conDepositos.setPathArchivo(this.file.getFileName());
		cargarArchivo();
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
			e.printStackTrace();
		}
	
	}
	private String guardarImagen() throws IOException {
		String pathDestino ="";

		if (null != this.getArchivo()) {
			pathDestino = getCompania().getPathFileServer();
			try {
				String pathAnterior = this.pathArchivoAnterior;
				Object[] file = this.getArchivo();
				InputStream inputStream = (InputStream) file[2];
				//Subir Archivo e inserta en tabla DIG_DOCUMENTO
				DigDocumentos digDocumento = new DigDocumentos();
				digDocumento.setNoCia(getCompania().getNoCia());
				digDocumento.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
				digDocumento.setLineaNegocio("CXC");
				digDocumento.setEntidad("CLIENTE");
				digDocumento.setFechaIngreso(new Date());
				digDocumento.setUsuarioIngreso(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				digDocumento.setDocumento(this.conDepositos.getCedula());
				digDocumento.setNombreArchivo((String)file[0]);//this.conDepositos.getPathArchivo());
				digDocumento.setArchivo(inputStream);
				digDocumento.setPathFileServer(getCompania().getPathFileServer());
				digDocumento.setRucEmpresa(getCompania().getIdTributario());
				digDocumento.setCategoria("CONFIRMACION DEPOSITOS");
				digDocumento.setObservacion("INGRESO SOICITUD CONFIRMACION DEPOSITOS");
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
				repoDto.setReferenciaDoc(ReferenciaDocRepositorio.ACTUALIZACION_ANTICIPOS.getValor());
				repoDto.setEstado(EstadoAI.ACTIVO.getValue());
				repoDto.setUsuarioIngreso(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				repoDto.setFechaIngreso(new Date());
				digDocRegistrador.registrarAsync(repoDto);
				if (pathAnterior != null && !pathAnterior.isEmpty()) {
					digDocRegistrador.inactivarAsync(
						getCompania().getNoCia(),
						RepositorioDoc.DIG_DOCUMENTOS.getValor(),
						pathAnterior,
						getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario()
					);
				}
			} catch (Exception e) {
				addErrorMessage(e.getMessage(), "");
			}
		}
		return pathDestino;
	}
	
    public void verImagenes(ConDepositos conDepositos) {
    	this.urlImagen = conDepositos.getPathArchivo();		
	}    
    
    public void verImagenesAutorizacion(ConDepositos conDepositos) {
    	this.urlPathArchivoTercero = //"/images/dynamic?file="+
    			conDepositos.getPathArchivoTercero();		
	}  
    
	public void descargarImagen(String imgUrl){
    	String path = System.getProperty("file.separator")+"TEMP"
				+System.getProperty("file.separator")
				+imgUrl; 
    	try{
            URL url = new URL(path);
            ByteArrayOutputStream out;
            try (InputStream in = new BufferedInputStream(url.openStream())) {
	            out = new ByteArrayOutputStream();
	            byte[] buf = new byte[1024];
	            int n = 0;
	            while (-1!=(n=in.read(buf))) {
	                out.write(buf, 0, n);
	            }
	            out.close();
            }           

        } catch(IOException ex){
            ex.printStackTrace(System.out);
        }
	 } 

	 public void seeFile() throws IOException {
		 try {
			 FileUpload fileUpload = new FileUpload();
			 if (null != urlImagen) {
				 fileUpload.seeFile(FacesContext.getCurrentInstance(), urlImagen);
			 } else {
				 addErrorMessage("No se ha subido ningún archivo al servidor", "");
			 }
		 }catch (Exception e) {
			addErrorMessage("No se ha encontrado el archivo en el servidor", "");
		 }
	 }	 

		public void seeFileAutorizacionTerceros() throws IOException {
			FileUpload fileUpload = new FileUpload();
			if (null != urlPathArchivoTercero) {
				fileUpload.seeFile(FacesContext.getCurrentInstance(), urlPathArchivoTercero);
			} else {
				addErrorMessage("No se ha subido ningún archivo al servidor", "");
			}
		}
	 
	 private void envioMail(ConDepositos conDepositos) throws GeneralException{	 
		//MAIL al grupo de Solicitudes de confirmación
		SisMailServidores sisMailServidores = getSisMailServidores(getCompania().getNoCia());
		StringBuffer mensaje = new StringBuffer();
		mensaje.append("Se ha actualizado la solicitud de confirmación de depósito o transferencia.<br><br>");
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
		mensaje.append("</table>");
		mensaje.append("<br><br>Por favor revisar la solicitud en el sistema S3S .<br><br>");
		
		try {
			// MAIL al grupo configurado de Solicitudes de confirmación para la empresa conectada
			//SI no tiene Area(Si no es Casabaca) o el area es Contabilidad, se envia al grupo SCxx
			if(conDepositos.getAreaConfirma() == null || conDepositos.getAreaConfirma().equals("CONTABILIDAD")) {
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SC"+getCompania().getNoCia());
				if(usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {				    
						mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),(String)objects[1],
								"Se ha actualizado la solicitud de confirmación de depósito "+conDepositos.getId(), mensaje);
					}
				}
			}else {//SI el area es Credito, se envia al grupo SCDxx
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SCD"+getCompania().getNoCia());
				if(usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {				    
						mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),(String)objects[1],
								"Se ha actualizado la solicitud de confirmación de depósito "+conDepositos.getId(), mensaje);
					}
				}
			}
			//Mail al asesor quien registra la solicitud
			mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),getUsuario().getEmail(),
					"Se ha actualiado la solicitud de confirmación de depósito "+conDepositos.getId(), mensaje);
		} catch (GeneralException e) {
			e.printStackTrace();
		}			
	}
	public void ejecutarConsultaClientes() {
		lazyModel = new LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(),
											  getCedulaClienteConsulta(),
											  getNombreClienteConsulta());
	}
	public void seleccionarCliente(Cliente cliente) {
		this.conDepositosBusqueda.setCedula(cliente.getCedula());
		//this.conDepositos.setNombreCliente(cliente.getNombre());		
	}
	
	public List<SelectItem> cargarLineasAnticipo() {
		List<LineaNegocio> lineasList;
		try {
			lineasList = lineaService.buscarLineaNegocioAnticipoCaja(getCompania().getNoCia());
			lineaSelectItems = new ArrayList<SelectItem>();
			if (lineasList != null && lineasList.size() > 0) {
				for (LineaNegocio linea : lineasList) {
					lineaSelectItems.add(new SelectItem(linea.getLineaNegocioPK().getNoLinea(), linea.getDescripcion()));
				}
			}
		} catch (FindException e) {
			e.printStackTrace();
		}
		return lineaSelectItems;
	}
	
	public int getBatchSize() {
		return CommonConstants.BATCH_SIZE;
	}
	public List<ConDepositos> getListConDepositos() {
		return listConDepositos;
	}
	public void setListConDepositos(List<ConDepositos> listConDepositos) {
		this.listConDepositos = listConDepositos;
	}
	public String getUrlImagen() {		
		return urlImagen;
	}

	public void setUrlImagen(String urlImagen) {
		this.urlImagen = urlImagen;
	}

	public ConDepositos getConDepositos() {
		return conDepositos;
	}

	public void setConDepositos(ConDepositos conDepositos) {
		this.conDepositos = conDepositos;
	}

	public Object[] getArchivo() {
		return archivo;
	}

	public void setArchivo(Object[] archivo) {
		this.archivo = archivo;
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
	public LazyDataModel<ConDepositos> getConDepositosLazyDataModel() {
		return conDepositosLazyDataModel;
	}
	public void setConDepositosLazyDataModel(LazyDataModel<ConDepositos> conDepositosLazyDataModel) {
		this.conDepositosLazyDataModel = conDepositosLazyDataModel;
	}
	public boolean isConsultaLista() {
		return consultaLista;
	}
	public void setConsultaLista(boolean consultaLista) {
		this.consultaLista = consultaLista;
	}
	public ConDepositos getConDepositosBusqueda() {
		return conDepositosBusqueda;
	}
	public void setConDepositosBusqueda(ConDepositos conDepositosBusqueda) {
		this.conDepositosBusqueda = conDepositosBusqueda;
	}
	public UploadedFile getFile() {
		return file;
	}
	public void setFile(UploadedFile file) {
		this.file = file;
	}
	public LazyDataModel<Cliente> getLazyModel() {
		return lazyModel;
	}
	public void setLazyModel(LazyDataModel<Cliente> lazyModel) {
		this.lazyModel = lazyModel;
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
	public boolean isPuedeEditar() {
		return puedeEditar;
	}
	public void setPuedeEditar(boolean puedeEditar) {
		this.puedeEditar = puedeEditar;
	}
	public List<SelectItem> getLineaSelectItems() {
		return lineaSelectItems;
	}
	public void setLineaSelectItems(List<SelectItem> lineaSelectItems) {
		this.lineaSelectItems = lineaSelectItems;
	}
	
	public String getUrlPathArchivoTercero() {
		return urlPathArchivoTercero;
	}

	public void setUrlPathArchivoTercero(String urlPathArchivoTercero) {
		this.urlPathArchivoTercero = urlPathArchivoTercero;
	}
}
