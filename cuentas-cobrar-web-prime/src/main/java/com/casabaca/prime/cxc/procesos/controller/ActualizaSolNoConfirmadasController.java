package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import com.casabaca.common.ejb.dto.DigDocRepositorioDto;
import com.casabaca.common.ejb.service.DigDocRepositorioRegistradorServiceLocal;
import com.casabaca.common.ejb.util.ReferenciaDocRepositorio;
import com.casabaca.common.ejb.util.RepositorioDoc;
import com.casabaca.common.ejb.util.TipoEntidadDocRepositorio;
import com.casabaca.common.ejb.util.type.CommonEnums.EstadoAI;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.DigDocumentos;
import com.casabaca.common.ejb.model.MaestroCuenta;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.DigDocumentoServiceLocal;
import com.casabaca.common.ejb.service.MaestroCuentaServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.ConDepositos;
import com.casabaca.cxc.ejb.servicio.ConDepositosServiceLocal;
import com.casabaca.cxc.ejb.util.TipoTransaccion;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyClientesDataModel;
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
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean
public class ActualizaSolNoConfirmadasController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ActualizaSolNoConfirmadasController.class);

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
	private ConDepositosServiceLocal conDepositosService;
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.DIG_DOCUMENTO_SERVICE)
	private DigDocumentoServiceLocal digDocumentoService;
	@EJB(lookup = NombreJNDI.DIG_DOC_REPOSITORIO_REGISTRADOR_SERVICE_BEAN)
	private DigDocRepositorioRegistradorServiceLocal digDocRegistrador;
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;

	private List<ConDepositos> listConDepositos;
	private LazyDataModel<ConDepositos> conDepositosLazyDataModel;
	private boolean consultaLista = false;

	private ConDepositos conDepositosBusqueda;
	private ConDepositos conDepositos;
	private Object[] archivo;
	private UploadedFile file;
	private List<SelectItem> listaTipoTransaccion;
	private List<MaestroCuenta> listMaestroCuenta;
	private List<SelectItem> agenciasSelectItems;
	private List<SelectItem> listAreas;
	private String urlImagen;

	private LazyDataModel<Cliente> lazyModel;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private boolean puedeEditar;
	private List<ConDepositos> solicitudesSel;
	private String codBanco;
	private String urlReport;
	private String mailBanco;
	private UploadedFile uploadedFile;
	private String fileName;
	private Map<String, String> archivosAdjuntos;
	private List<String> listaAdjuntos;
	private String filePath;
	private Object[] archivoEnvio;
	private UploadedFile fileEnvio;

	@PostConstruct
	public void init() throws FindException, ServiceLocatorException {
		this.listConDepositos = new ArrayList<>();
		this.solicitudesSel = new ArrayList<>();
		this.conDepositosBusqueda = new ConDepositos();
		this.conDepositosBusqueda.setNoCia(getCompania().getNoCia());
		this.conDepositosBusqueda.setEstado("B");
		puedeEditar = false;
		this.conDepositosBusqueda.setFechaDesde(FechaUtils.sumarNDias(new Date(), -30));
		this.conDepositosBusqueda.setFechaHasta(new Date());

		this.cargarMaestroCuentas();

		this.consultaSolicitudesNoConfirmadas();
		archivosAdjuntos = new HashMap<String, String>();
		listaAdjuntos = new ArrayList<>();
	}

	private void cargarMaestroCuentas() {
		this.listMaestroCuenta = this.maestroCuentaService
				.consultaMaestrosCuentaConfirmacionDeposito(getCompania().getNoCia());
		if (listMaestroCuenta != null && !listMaestroCuenta.isEmpty()) {
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
	 * Carga la lista de Solicitudes de confirmación de depositos registradas por el
	 * usuario conectado
	 */
	public void consultaSolicitudesNoConfirmadas() {
		logger.info("consultaSolicitudesConfirmacion...");
		try {
			if (this.conDepositosBusqueda.getFechaDesde() == null
					&& this.conDepositosBusqueda.getFechaHasta() != null) {
				addErrorMessage("", "Ingrese la fecha desde.");
				consultaLista = true;
				return;
			}
			this.obtenerDepositos(0, getBatchSize());
			// Si no hay resultados se agrega un mensaje informativo
			if (getListConDepositos() == null || getListConDepositos().isEmpty()) {
				if (conDepositosLazyDataModel != null) {
					conDepositosLazyDataModel.setRowCount(0);
				}
				addInfoMessage("", "No existen registros.");

			} else {
				consultaLista = true;
				@SuppressWarnings("serial")
				LazyDataModel<ConDepositos> lazyConDepositos = new LazyDataModel<ConDepositos>() {
					@Override
					public List<ConDepositos> load(int first, int pageSize, String sortField, SortOrder sortOrder,
							Map<String, Object> filters) {
						// control para que consulte solo cuando sea necesario
						if (!consultaLista && FacesContext.getCurrentInstance().getCurrentPhaseId()
								.equals(PhaseId.RENDER_RESPONSE)) {
							logger.info(":buscar solicitudes desde: " + first);
							// Se selecciona el tipo de orden
							try {
								// Nueva consulta
								obtenerDepositos(first, pageSize);
							} catch (Exception e) {
								logger.error(e);
								addErrorMessage("", "No existen registros.");
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

	public void obtenerDepositos(int firstResult, int maxResults) {
		logger.info("obtieneDepositos..." + getCompania().getNoCia());
		try {
			this.listConDepositos = conDepositosService.consultarListConDepositos(conDepositosBusqueda, firstResult,
					maxResults);
			if (listConDepositos != null && !listConDepositos.isEmpty()) {
				HashMap<String, String> tiposTransaccion = obtenerTiposTransaccion();

				for (ConDepositos conDepositos : listConDepositos) {
					if (listMaestroCuenta != null) {
						Optional<MaestroCuenta> result = listMaestroCuenta.stream()
								.filter(x -> x.getBanco().equals(conDepositos.getBanco())).findFirst();
						if (result.isPresent()) {
							conDepositos.setNombreBanco(result.get().getNomBanco());
						}
					}
					conDepositos.setNombreTipoTransaccion(tiposTransaccion.get(conDepositos.getTipoTransaccion()));
					if (conDepositos.getCedula() != null) {
						String nombre = null;
						try {
							nombre = clienteServiceLocal
									.buscarNombrePorCedulaNoCia(conDepositos.getCedula(), getCompania().getNoCia())
									.getNombre();
							conDepositos.setNombreCliente(nombre);
						} catch (FindException e) {
							addErrorMessage("Error al consultar cliente", e.getMessage());
						}

					}
					if (conDepositos.getAgenciaCajero() != null) {
						String nombreAgencia = agenciaService.buscarNombreAgencia(getCompania().getNoCia(),
								conDepositos.getAgenciaCajero());
						conDepositos.setNombreAgenciaCajero(nombreAgencia);
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
		tiposTransaccion.put(TipoTransaccion.DEPOSITO_EFECTIVO.getTipo(),
				TipoTransaccion.DEPOSITO_EFECTIVO.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.DEPOSITO_CHEQUE.getTipo(),
				TipoTransaccion.DEPOSITO_CHEQUE.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.TRANSF_MISMO_BANCO.getTipo(),
				TipoTransaccion.TRANSF_MISMO_BANCO.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.TRANSF_OTROS_BANCOS.getTipo(),
				TipoTransaccion.TRANSF_OTROS_BANCOS.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.TRANSF_APP.getTipo(), TipoTransaccion.TRANSF_APP.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.DEPOSITO_AGENCIA.getTipo(),
				TipoTransaccion.DEPOSITO_AGENCIA.getDescripcion());
		tiposTransaccion.put(TipoTransaccion.RECAUDACION.getTipo(), TipoTransaccion.RECAUDACION.getDescripcion());
		return tiposTransaccion;
	}

	/**
	 * Para desplegar en la edición
	 * 
	 * @return
	 */
	public List<SelectItem> getListaTipoTransaccion() {

		listaTipoTransaccion = new ArrayList<SelectItem>();
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.DEPOSITO_EFECTIVO.getTipo(),
				TipoTransaccion.DEPOSITO_EFECTIVO.getDescripcion()));
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.DEPOSITO_CHEQUE.getTipo(),
				TipoTransaccion.DEPOSITO_CHEQUE.getDescripcion()));
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.TRANSF_MISMO_BANCO.getTipo(),
				TipoTransaccion.TRANSF_MISMO_BANCO.getDescripcion()));
		listaTipoTransaccion.add(new SelectItem(TipoTransaccion.TRANSF_OTROS_BANCOS.getTipo(),
				TipoTransaccion.TRANSF_OTROS_BANCOS.getDescripcion()));
		listaTipoTransaccion
				.add(new SelectItem(TipoTransaccion.TRANSF_APP.getTipo(), TipoTransaccion.TRANSF_APP.getDescripcion()));
		if (CommonConstants.CASABACA.equals(getCompania().getNoCia())) {
			listaTipoTransaccion.add(new SelectItem(TipoTransaccion.RECAUDACION.getTipo(),
					TipoTransaccion.RECAUDACION.getDescripcion()));
		}

		return listaTipoTransaccion;
	}

	public void editar(ConDepositos item) {
		logger.info("editar...");
		this.conDepositos = item;
		this.archivo = null;

		this.cargarAgencias();
		accionesDialog("dlgEditarValores", true);
	}

	public void actualizar() {
		logger.info("actualizar...");
		try {
			String mensajeValidacion = validarCamposRequeridos();

			if (mensajeValidacion.trim().isEmpty()) {
				String path = null;
				if (this.getArchivo() != null) {
					// Guarda archivo subido
					path = guardarArchivo();
				}
				if (path != null) {
					this.conDepositos.setPathArchivoBanco(path);
				}
				this.conDepositos.setEstado("S");
				Integer envios = this.conDepositos.getEnvioNo() != null ? this.conDepositos.getEnvioNo() : 0;
				this.conDepositos.setEnvioNo(envios + 1);
				this.conDepositos.setUsuarioNoIdentificada(getLoggedUsername());
				this.conDepositos.setFechaNoIdentificada(new Date());
				this.conDepositos.setEnvioBanco(null);

				conDepositosService.actualizarConDepositos(this.conDepositos);

				addInfoMessage("Exito", "Guardado exitoso, solicitud " + this.conDepositos.getId());

				envioMail(this.conDepositos);

				accionesDialog("dlgEditarValores", false);

				this.consultaSolicitudesNoConfirmadas();
			} else {
				addErrorMessage("Error", mensajeValidacion);
			}
		} catch (Exception e) {
			addErrorMessage("Error", "Error en el guardado " + e.getMessage());
			logger.error(e.getMessage() + " " + e.getCause());
		}
	}

	private String validarCamposRequeridos() {
		StringBuffer mensaje = new StringBuffer();

		if (getCompania().getNoCia().equals(CommonConstants.CASABACA)) {
			if ("".equals(this.conDepositos.getObservacionNoIdentificada())) {
				mensaje.append(" Debe ingresar una observacion de respaldo.");
			}

			if (this.conDepositos.getPathArchivoBanco() == null) {
				mensaje.append(" Debe seleccionar un archivo de respaldo.");
			}

		}
		return mensaje.toString();

	}

	public void asignarArchivo(FileUploadEvent event) {
		this.fileEnvio = event.getFile();
		this.conDepositos.setPathArchivoBanco(this.fileEnvio.getFileName());
		cargarArchivo();
	}

	private void cargarArchivo() {

		try {
			UploadedFile uf = this.fileEnvio;
			Object[] file = new Object[3];
			file[0] = uf.getFileName();
			file[1] = uf.getFileName().substring(uf.getFileName().lastIndexOf("."), uf.getFileName().length());
			file[2] = uf.getInputstream();
			this.setArchivo(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String guardarArchivo() throws IOException {
		String pathDestino = "";

		if (null != this.getArchivo()) {
			pathDestino = getCompania().getPathFileServer();
			try {
				Object[] file = this.getArchivo();
				InputStream inputStream = (InputStream) file[2];
				// Subir Archivo e inserta en tabla DIG_DOCUMENTO
				DigDocumentos digDocumento = new DigDocumentos();
				digDocumento.setNoCia(getCompania().getNoCia());
				digDocumento.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
				digDocumento.setLineaNegocio("CXC");
				digDocumento.setEntidad("CLIENTE");
				digDocumento.setFechaIngreso(new Date());
				digDocumento.setUsuarioIngreso(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				digDocumento.setDocumento(this.conDepositos.getCedula());
				digDocumento.setNombreArchivo((String) file[0]);// this.conDepositos.getPathArchivo());
				digDocumento.setArchivo(inputStream);
				digDocumento.setPathFileServer(getCompania().getPathFileServer());
				digDocumento.setRucEmpresa(getCompania().getIdTributario());
				digDocumento.setCategoria("CONFIRMACION BANCOS");
				digDocumento.setObservacion("INGRESO CONFIRMACION BANCOS");
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
				repoDto.setReferenciaDoc(ReferenciaDocRepositorio.CONFIRMACION_BANCO.getValor());
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

	public void verImagenes(ConDepositos conDepositos) {
		this.urlImagen = conDepositos.getPathArchivo();
	}

	public void descargarImagen(String imgUrl) {
		String path = System.getProperty("file.separator") + "TEMP" + System.getProperty("file.separator") + imgUrl;
		try {
			URL url = new URL(path);
			ByteArrayOutputStream out;
			try (InputStream in = new BufferedInputStream(url.openStream())) {
				out = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int n = 0;
				while (-1 != (n = in.read(buf))) {
					out.write(buf, 0, n);
				}
				out.close();
			}

		} catch (IOException ex) {
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
		} catch (Exception e) {
			addErrorMessage("No se ha encontrado el archivo en el servidor", "");
		}
	}

	private void envioMail(ConDepositos conDepositos) throws GeneralException {
		// MAIL al grupo de Solicitudes de confirmación
		SisMailServidores sisMailServidores = getSisMailServidores(getCompania().getNoCia());
		StringBuffer mensaje = new StringBuffer();
		mensaje.append("Se ha actualizado la solicitud de confirmación de depósito o transferencia.<br><br>");
		mensaje.append(
				"<table border='1'><tr><td><b>Datos Depósito </b></td><td>" + conDepositos.getId() + "</td></tr>");
		mensaje.append("<tr><td><b>Fecha de depósito</b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy").format(conDepositos.getFechaDocumento()) + "</td></tr>");
		mensaje.append("<tr><td><b>Documento</b></td> <td> " + conDepositos.getNumeroDocumento() + "</td></tr>");
		mensaje.append("<tr><td><b>Valor</b></td> <td> " + conDepositos.getValorDocumento() + "</td></tr>");
		mensaje.append("<tr><td><b>Cédula</b></td> <td> " + conDepositos.getCedula() + "</td></tr>");
		mensaje.append("<tr><td><b>Nombre</b></td> <td> " + conDepositos.getNombreCliente() + "</td></tr>");
		mensaje.append("<tr><td><b>Agencia</b></td> <td> "
				+ agenciaService.buscarNombreAgencia(getCompania().getNoCia(), conDepositos.getAgencia())
				+ "</td></tr>");
		mensaje.append("<tr><td><b>Observación No Identificada</b></td> <td> "
				+ conDepositos.getObservacionNoIdentificada() + "</td></tr>");
		mensaje.append("<tr><td><b>Fecha No Identificada</b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy").format(conDepositos.getFechaNoIdentificada()) + "</td></tr>");
		mensaje.append("</table>");
		mensaje.append("<br><br>Por favor revisar la solicitud en el sistema S3S .<br><br>");

		try {
			// MAIL al grupo configurado de Solicitudes de confirmación para la empresa
			// conectada
			// SI no tiene Area(Si no es Casabaca) o el area es Contabilidad, se envia al
			// grupo SCxx
			if (conDepositos.getAreaConfirma() == null || conDepositos.getAreaConfirma().equals("CONTABILIDAD")) {
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SC" + getCompania().getNoCia());
				if (usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {
						mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),
								(String) objects[1],
								"Se ha actualizado la solicitud de confirmación de depósito " + conDepositos.getId(),
								mensaje);
					}
				}
			} else {// SI el area es Credito, se envia al grupo SCDxx
				List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("SCD" + getCompania().getNoCia());
				if (usuariosMail != null && !usuariosMail.isEmpty()) {
					for (Object[] objects : usuariosMail) {
						mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),
								(String) objects[1],
								"Se ha actualizado la solicitud de confirmación de depósito " + conDepositos.getId(),
								mensaje);
					}
				}
			}
			// Mail al asesor quien registra la solicitud
			mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(), getUsuario().getEmail(),
					"Se ha actualiado la solicitud de confirmación de depósito " + conDepositos.getId(), mensaje);
		} catch (GeneralException e) {
			e.printStackTrace();
		}
	}

	public void ejecutarConsultaClientes() {
		lazyModel = new LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(), getCedulaClienteConsulta(),
				getNombreClienteConsulta());
	}

	public void seleccionarCliente(Cliente cliente) {
		this.conDepositos.setCedula(cliente.getCedula());
		this.conDepositos.setNombreCliente(cliente.getNombre());
	}

	public int getBatchSize() {
		return CommonConstants.BATCH_SIZE;
	}

	public void rowSelectCheckbox(SelectEvent use) {
		if (solicitudesSel == null) {
			solicitudesSel = new ArrayList<>();
			solicitudesSel.add((ConDepositos) use.getObject());
		} else {
			solicitudesSel.add((ConDepositos) use.getObject());
		}
		for (ConDepositos con : solicitudesSel) {
			con.setEnvioBanco("S");
			try {
				conDepositosService.actualizarConDepositos(con);
			} catch (UpdateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void rowUnselectCheckbox(UnselectEvent use) {
		solicitudesSel.remove((ConDepositos) use.getObject());

	}

	protected String getStrReportPath() {
		urlReport = "/reportes/cxcTransferenciasNoIdentificadas.jasper";
		return urlReport;
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("noCia", getCompania().getNoCia());
		parameters.put("idBanco", this.codBanco);
		parameters.put("envioBanco", "S");
	}

	public void reporte() {
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

			exportXls(jasperPrint, response);
			virtualizer.cleanup();
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
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

	/**
	 * Metodo para exportar reporte en formato EXCEL
	 * 
	 * @param jasperPrint
	 * @param response
	 * @throws JRException
	 * @throws IOException
	 */
	public void exportXls(JasperPrint jasperPrint, HttpServletResponse response) throws JRException, IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		JRXlsExporter exporter = new JRXlsExporter();
		exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, true);
		exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.setParameter(JExcelApiExporterParameter.IS_DETECT_CELL_TYPE, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND, CommonConstants.FALSE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_IGNORE_CELL_BORDER, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JExcelApiExporterParameter.IS_COLLAPSE_ROW_SPAN, CommonConstants.TRUE_VALUE);

		exporter.exportReport();
		byte[] bytes = byteArrayOutputStream.toByteArray();

		StringBuffer header = new StringBuffer();
		header.append("attachment; filename=\"");
		header.append(getXlsFileName());
		header.append("\"");
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(bytes.length);
		// ServletOutputStream outputStream = response.getOutputStream();
		response.getOutputStream().write(bytes, 0, bytes.length);
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	/**
	 * Metodo para concatenar extension .xls
	 * 
	 * @return
	 */
	public String getXlsFileName() {

		String[] folders = getStrReportPath().split("/");
		String reportFile = folders[folders.length - 1];

		String[] names = reportFile.split("\\.");

		String nombreBancoSelec = "";
		Date myDate = new Date();
		for (MaestroCuenta ba : this.listMaestroCuenta) {
			if (ba.getBanco().equals(this.codBanco)) {
				nombreBancoSelec = ba.getNomBanco();
			}
		}
		String result = nombreBancoSelec.replaceAll("\\s", "");
		String xlsName = result.concat("_" + new SimpleDateFormat("dd-MM-yyyy").format(myDate) + ".xls");

		RequestContext.getCurrentInstance().execute("PF('idDlgGeneraReporte').hide();");
		return xlsName;
	}

	public void enviarSolicitudBanco(ConDepositos item) {
		boolean envioConfirma = true;
		if (item.getDocEstadoCuenta() == null || item.getDocEstadoCuenta().trim().isEmpty()) {
			addErrorMessage("", "Debe ingresar un Documento de Estado de Cuenta para enviar la solicitud.");
			envioConfirma = false;
		}
		try {
			if (envioConfirma) {
				item.setEnvioBanco("S");
				conDepositosService.actualizarConDepositos(item);
				info("Solicitud confirmada con exito para envio a Bancos");
			}

		} catch (UpdateException e) {
			e.printStackTrace();
		}
	}

	public void quitarEnvioBanco(ConDepositos item) {
		boolean noConfirma = true;
		if (item.getDocEstadoCuenta() == null || item.getDocEstadoCuenta().trim().isEmpty()) {
			addErrorMessage("", "No se puede realizar el envio a Bancos.");
			noConfirma = false;
		}
		try {
			if (noConfirma && item.getEnvioBanco().equals("S")) {
				item.setEnvioBanco("N");
				conDepositosService.actualizarConDepositos(item);
				info("Solicitud NO se envia a Bancos");
			}
		} catch (UpdateException e) {
			e.printStackTrace();
		}
	}

	public void enviarRequerimientoBanco() {
		boolean enviaBanco = true;
		String nombreBancoSelec = "";
		for (MaestroCuenta ba : this.listMaestroCuenta) {
			if (ba.getBanco().equals(this.codBanco)) {
				nombreBancoSelec = ba.getNomBanco();
			}
		}

		if (!this.fileName.contains(nombreBancoSelec.replaceAll("\\s", ""))) {
			addErrorMessage("",
					"No coinciden el banco seleccionado con el nombre del archivo adjunto, por favor verificar.");
			this.codBanco = "";
			this.mailBanco = "";
			this.fileName = "";
		} else {
			try {
				UsuarioSis usuarioSolicita = usuarioSisServiceLocal
						.findByPk(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				SisMailServidores sisMailServidores = getSisMailServidores(getCompania().getNoCia());

				// mensaje
				StringBuffer mensaje = new StringBuffer();
				this.preparaMensaje(mensaje);

				List<File> archivos = new ArrayList<File>();

				// Create a temporary file
				File outputFile;
				outputFile = File.createTempFile(uploadedFile.getFileName(), ".xls");

				try (InputStream inputStream = this.uploadedFile.getInputstream();
						OutputStream outputStream = new FileOutputStream(outputFile)) {

					// Read from the input stream and write to the output stream
					byte[] buffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				archivos.add(outputFile);

				if (validarCorreo(mailBanco)) {
					List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo("RTNC");
					if (usuariosMail != null && !usuariosMail.isEmpty()) {
						for (Object[] objects : usuariosMail) {

							mailService.sendEmailAttachmentsImgEmbebida(sisMailServidores, usuarioSolicita.getEmail(),
									usuarioSolicita.getUsuario(), (String) objects[1], usuarioSolicita.getNombre(),
									getCompania().getNombre() + " " + getCompania().getIdTributario()
											+ " - SOLICITUD DE ORDENANTES ",
									mensaje, true, archivos, true, new ArrayList<String>(), null);
						}
					}
					mailService.sendEmailAttachmentsImgEmbebida(sisMailServidores, usuarioSolicita.getEmail(),
							usuarioSolicita.getUsuario(), mailBanco, usuarioSolicita.getNombre(),
							getCompania().getNombre() + " " + getCompania().getIdTributario()
									+ " - SOLICITUD DE ORDENANTES ",
							mensaje, true, archivos, true, new ArrayList<String>(), null);
				}
				addInfoMessage("", "La notificacion por correo ha sido enviado exitosamente al Banco seleccionado");
				this.codBanco = "";
				this.mailBanco = "";
				this.fileName = "";
			} catch (FindException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (GeneralException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void upload(FileUploadEvent event) {
		this.uploadedFile = event.getFile();
		this.fileName = uploadedFile.getFileName();
	}

	private boolean validarCorreo(String email) {
		if (email != null) {
			String regx = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
			Pattern pattern = Pattern.compile(regx);
			Matcher matcher = pattern.matcher(email);
			return matcher.matches();
		} else {
			return false;
		}
	}

	private void preparaMensaje(StringBuffer mensaje) {

		String nombreBancoSelec = "";
		for (MaestroCuenta ba : this.listMaestroCuenta) {
			if (ba.getBanco().equals(this.codBanco)) {
				nombreBancoSelec = ba.getNomBanco();
			}
		}
		Date myDate = new Date();
		mensaje.append("Estimado(a), <br><br>");
		mensaje.append(
				"Reciba un cordial saludo, por favor su ayuda con el formulario de requerimiento que se encuentra como archivo adjunto a esta notificación.<br>");
		mensaje.append(
				"Información requerida para identificar los nombres de los ordenantes de las transferencias para el Banco: <br><br>");
		mensaje.append("<table border='1'><tr><td><b>Banco </b></td><td>" + nombreBancoSelec + "</td></tr>");
		mensaje.append("<tr><td><b>Fecha de Envio</b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(myDate) + "</td></tr>");
		mensaje.append("</table>");

		mensaje.append("<br><br>Gracias por su atención.<br><br>");
		mensaje.append("<br><br>Atentamente,");
		mensaje.append("<br>" + getCompania().getNombre());
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

	public List<ConDepositos> getSolicitudesSel() {
		return solicitudesSel;
	}

	public void setSolicitudesSel(List<ConDepositos> solicitudesSel) {
		this.solicitudesSel = solicitudesSel;
	}

	public String getCodBanco() {
		return codBanco;
	}

	public void setCodBanco(String codBanco) {
		this.codBanco = codBanco;
	}

	public String getMailBanco() {
		return mailBanco;
	}

	public void setMailBanco(String mailBanco) {
		this.mailBanco = mailBanco;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public Map<String, String> getArchivosAdjuntos() {
		return archivosAdjuntos;
	}

	public void setArchivosAdjuntos(Map<String, String> archivosAdjuntos) {
		this.archivosAdjuntos = archivosAdjuntos;
	}

	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Object[] getArchivoEnvio() {
		return archivoEnvio;
	}

	public void setArchivoEnvio(Object[] archivoEnvio) {
		this.archivoEnvio = archivoEnvio;
	}

	public UploadedFile getFileEnvio() {
		return fileEnvio;
	}

	public void setFileEnvio(UploadedFile fileEnvio) {
		this.fileEnvio = fileEnvio;
	}

}