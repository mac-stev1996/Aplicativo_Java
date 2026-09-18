package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.jfree.util.Log;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.caja.ejb.service.NativeDmlServiceLocal;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.dto.Arcjsec;
import com.casabaca.common.ejb.dto.DatosMemorandoDto;
import com.casabaca.common.ejb.dto.DireccionesMail;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.InventarioMaster;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ArcjsecServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.DatosMemorandoServiceLocal;
import com.casabaca.common.ejb.service.InventarioMasterServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxCdatosAnticiposDto;
import com.casabaca.cxc.ejb.dto.CxCdatosComprobantesDiariosDto;
import com.casabaca.cxc.ejb.dto.CxCdatosCtasBancariasDto;
import com.casabaca.cxc.ejb.modelo.CxcCabDevAnticipo;
import com.casabaca.cxc.ejb.modelo.CxcCabDevAnticipoPK;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiarios;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiariosPK;
import com.casabaca.cxc.ejb.servicio.CabDevAnticipoClienteServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxCdatosComprobantesDiariosDtoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcAnticiposServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcDevolucionSolAnticiposServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcRevisionDiariosServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcdatosCtasBancariasServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * @author fb_fabara
 * @version $Revision: 1.50 $
 */

@ViewScoped
@ManagedBean(name = "cxcAutomatizacionDiariosAController")
public class CxcAutomatizacionDiariosAController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	private static Logger logger = Logger.getLogger(CxcAutomatizacionDiariosAController.class);

	/**
	 * VARIABLES
	 */
	private String noCia;
	private String centro;
	private String usuarioConectado;

	private String cedula;
	private String nombres;
	private String cedulaBeneficiario;
	private String nombresBeneficiario;
	private BigDecimal diferencia;
	private String ObservacionCabecera;
	private String emiteCheque;
	private String ctasBancarias;
	private String usuarioSolicita;
	private String comentarioEmisionChequeCabecera;
	private String comentarioEmisionCheque;
	private String comentarioCancelacionAnticipos;
	private String documentoCabeceraCancelacion;
	private String documentosYvaloresAcancelar;
	private String documentosTomados;
	private BigDecimal valoresCancelar;
	private String numero;
	private String lineaNegocio;
	private String descripcionLineaNegocio;
	private String rutaPdfMail;
	private String noFisicoGeneracion;
	private String numeroDocumentoSeleccionado;
	private String codigoU;
	private String noClienteArchivo;
	private String placaRecuperada;
	private String variable;
	private String pOrigen;
	private String pnoSolicitud;
	private Long pnoCliente;
	private String pnoCia;
	private String pCedula;
	private String pnombres;
	private Date fechaAnticipo;
	private static final String GRUPO_CORREO = "GMDAC";

	private BigDecimal valor;
	private BigDecimal sumaTotalesXbeneficiarios;
	private BigDecimal sumaTotalAnticiposSeleccionados;

	private boolean uafAplica;
	private boolean aplicaDiferencia;
	private boolean verificarMontosTomar;
	private boolean desactivarBtnProcesar;
	public boolean noEsAdministrador;

	private Cliente cliente;
	private Cliente clienteBeneficiarios;

	private List<CxCdatosAnticiposDto> listaAnticipos;
	private List<CxCdatosAnticiposDto> listaAnticiposSeleccionados;
	private List<DatosMemorandoDto> listaMemorandos;
	private List<CxCdatosCtasBancariasDto> listaCtasBancarias;
	private List<SelectItem> usuariosSelectItems;
	private List<SelectItem> ctasBancariasSelectItems;
	private List<Cliente> listarClientes;
	private List<Cliente> listarClientesBeneficiarios;
	private List<String> listaNumerosFisicos;
	private List<String> listaNumerosDocumentos;

	private UploadedFile file;
	private List<String> listaAdjuntos;
	private Map<String, String> archivosAdjuntos;
	private CxcRevisionDiarios revision;
	private Map<String, List<String>> mapaDiariosCliente;
	private Map<String, List<String>> mapaArchivosCliente;

	private InventarioMaster vehiculo;

	public static final String DOC_FILE = ".doc";
	public static final String PDF_FILE = ".pdf";
	public static final String XLS_FILE = ".xls";
	public static final String XLS_APPLICATION = "application/vnd.ms-excel";
	public static final String PDF_APPLICATION = "application/pdf";
	public static final String DOC_APPLICATION = "application/msword";

	private static final String ESTADO_MEMO = "E";

	private List<Object[]> codigos;
	private Object[] codigoUSel;
	private DatosMemorandoDto beneficiarioSelected;
	private Boolean mostrarDatos= Boolean.FALSE;

	/**
	 * VARIABLES DE SERVICIOS
	 */
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	@EJB(lookup = NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN)
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;

	@EJB(lookup = NombreJNDI.CXC_DATOS_ANTICIPOS)
	private CxcAnticiposServiceLocal anticiposService;

	@EJB(lookup = NombreJNDI.DATOS_MEMORANDO_SERVICE)
	private DatosMemorandoServiceLocal memorandoService;

	@EJB(lookup = NombreJNDI.CXC_DATOS_CTAS_BANCARIAS)
	private CxcdatosCtasBancariasServiceLocal ctasbancariasService;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@EJB(lookup = NombreJNDI.CXC_DATOS_COMPROBANTES_DIARIOS)
	private CxCdatosComprobantesDiariosDtoServiceLocal cxcDatosComprobatesDiariosService;

	@EJB(lookup = NombreJNDI.CXC_REVISION_DIARIOS_SERVICE_BEAN)
	private CxcRevisionDiariosServiceLocal revisionServiceLocal;

	@EJB(lookup = NombreJNDI.INVENTARIO_MASTER_SERVICE)
	private InventarioMasterServiceLocal inventarioMasterService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	@EJB(lookup = NombreJNDI.NATIVE_DML_CAJAS_SERVICE)
	private NativeDmlServiceLocal nativeDmlCajasService;

	@EJB(lookup = NombreJNDI.CXC_DEVOLUCIONES_SOLICITUDES_SERVICE)
	private CxcDevolucionSolAnticiposServiceLocal solicitudesService;

	@EJB(lookup = NombreJNDI.CXC_CAB_DEV_ANTICIPO_SERVICE)
	private CabDevAnticipoClienteServiceLocal devAnticipoService;
	
	@EJB(lookup = NombreJNDI.ARCJSEC_SERVICE_BEAN)
	private ArcjsecServiceLocal arcjsecService;

	@PostConstruct
	public void init() {
		if (getRequestParameter("param") != null && !getRequestParameter("param").equals("")) {
			this.setVariable(getRequestParameter("param"));
		}
		this.noCia = this.getCompania().getNoCia();
		this.centro = this.getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		this.usuarioConectado = this.getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario();
		this.cliente = new Cliente();
		this.clienteBeneficiarios = new Cliente();
		this.listaAnticipos = new ArrayList<>();
		this.listaMemorandos = new ArrayList<>();
		this.listaAnticiposSeleccionados = new ArrayList<>();
		this.listaNumerosFisicos = new ArrayList<>();
		this.listaNumerosDocumentos = new ArrayList<>();
		this.usuariosSelectItems = new ArrayList<SelectItem>();
		this.setEmiteCheque(CommonConstants.YES_STRING_VALUE);
		this.setRutaPdfMail(null);
		this.setSumaTotalesXbeneficiarios(null);
		this.numeroDocumentoSeleccionado = null;
		this.revision = new CxcRevisionDiarios();
		this.mapaDiariosCliente = new HashMap<>();
		this.archivosAdjuntos = new HashMap<>();
		this.listaAdjuntos = new ArrayList<>();
		this.archivosAdjuntos = new HashMap<>();
		this.mapaArchivosCliente = new HashMap<>();
		this.desactivarBtnProcesar = true;
		this.vehiculo = new InventarioMaster();
		this.noEsAdministrador = this.getVariable() != null ? (this.getVariable().equals("noAdmin") ? true : false)
				: false;
		this.setEmiteCheque(
				this.noEsAdministrador ? CommonConstants.NO_STRING_VALUE : CommonConstants.YES_STRING_VALUE);
		cargarCtasBancarias();
		cargarUsuarios();

		if (!"".equals(getRequestParameter("origen")) && getRequestParameter("origen") != null) {
			this.pOrigen = getRequestParameter("origen");
		}
		if (!"".equals(getRequestParameter("solicitud")) && getRequestParameter("solicitud") != null) {
			this.pnoSolicitud = getRequestParameter("solicitud");
		}
		if (!"".equals(getRequestParameter("noCia")) && getRequestParameter("noCia") != null) {
			this.pnoCia = getRequestParameter("noCia");
		}
		if (!"".equals(getRequestParameter("cliente")) && getRequestParameter("cliente") != null) {
			this.pnoCliente = Long.parseLong(getRequestParameter("cliente"));
		}
		if (!"".equals(getRequestParameter("cedula")) && getRequestParameter("cedula") != null) {
			this.pCedula = getRequestParameter("cedula");
		}
		if (!"".equals(getRequestParameter("nombres")) && getRequestParameter("nombres") != null) {
			//this.pnombres = getRequestParameter("nombres");
			this.pnombres = "";
		}

		if (("D").equals(this.pOrigen)) {
			try {
				cargarCientes();
				obtenerAnticipos();
				agregarAnticiposSeleccionados();
				this.mostrarDatos= activarSegunSecuencia(getCompania().getNoCia(), getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), "M"); 
				if(!this.mostrarDatos) {
					 FacesContext fc = FacesContext.getCurrentInstance();
		                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
		                        "Error",
		                        "El proceso no se encuentra habilitado para su usuario, por favor comunicarse con el administrador del sistema."));

		                fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
		                        "",
		                        "No se encontró cliente con los datos ingresados"));

		                desactivarBtnProcesar = Boolean.FALSE;
		                return;
				}
			} catch (Exception e) {
				Log.error(e);
	            FacesContext.getCurrentInstance().addMessage(null,
	                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
			}
		}

	}

	public List<SelectItem> cargarUsuarios() {
		List<UsuarioSis> usuariosList = usuarioSisService.getUsuarioList(getCompania().getNoCia());
		this.usuariosSelectItems = new ArrayList<SelectItem>();
		if (usuariosList != null && usuariosList.size() > 0) {
			for (UsuarioSis usuario : usuariosList) {
				this.usuariosSelectItems.add(new SelectItem(usuario.getUsuario(), usuario.getNombre()));
			}
		}
		return this.usuariosSelectItems;
	}

	public void cargarCientes() {
		try {
			if (("D").equals(this.pOrigen)) {
				this.listarClientes = clienteService.buscarClientesPorParametros(0, 25, getCompania().getNoCia(),
						this.pCedula, this.pnombres);
				for (Cliente cli : listarClientes) {
					cliente = cli;

				}

			} else {
				this.listarClientes = clienteService.buscarClientesPorParametros(0, 25, getCompania().getNoCia(),
						this.getCedula(), this.getNombres());
			}

		} catch (Exception e) {
			this.listarClientes = new ArrayList<>();
			e.printStackTrace();

		}
	}

	public void seleccionarFilaCliente(SelectEvent event) {
		this.cliente = new Cliente();
		this.cliente = (Cliente) event.getObject();
	}

	public void cargarCientesBeneficiarios() {
		try {
			this.listarClientesBeneficiarios = clienteService.buscarClientesPorParametros(0, 25,
					getCompania().getNoCia(), this.getCedulaBeneficiario(), this.getNombresBeneficiario());
		} catch (Exception e) {
			addInfoMessage("Verificar ", " No se encontro cliente con los datos ingresados");
			this.listarClientesBeneficiarios = new ArrayList<>();
			e.printStackTrace();
		}
	}

	public void seleccionarFilaClienteBeneficiarios(SelectEvent event) {
		this.clienteBeneficiarios = new Cliente();
		this.clienteBeneficiarios = (Cliente) event.getObject();

		DatosMemorandoDto datoMemo = new DatosMemorandoDto();
		datoMemo.setNoCliente(clienteBeneficiarios.getClientePK().getNoCliente());
		datoMemo.setCedula(clienteBeneficiarios.getCedula());
		datoMemo.setBeneficiario(clienteBeneficiarios.getNombre());
		this.listaMemorandos.add(datoMemo);
		obtenerDiferencia(datoMemo);
	}

	public void obtenerAnticipos() {
		try {
			if (("D").equals(this.pOrigen)) {
				this.listaAnticipos = devAnticipoService.anticiposSeleccionados(this.pnoCia, this.pCedula,
						this.pnoSolicitud);
			} else {
				this.listaAnticipos = anticiposService.buscarAnticiposClientes(getCompania().getNoCia(),
						getCliente().getCedula());
			}

			if (this.listaAnticipos.size() > 0) {
				info("Datos Recuperados con exito");
			} else {
				warn("Verificar Si el anticipo esta bloqueado o si ya fue ocupado");
			}
		} catch (Exception e) {
			error("Error: No se pudieron recuperar anticipos");
			e.printStackTrace();
		}

	}

	public void recuperaAtnicipo(SelectEvent event) {
		this.setListaMemorandos(new ArrayList<>());
		CxCdatosAnticiposDto itemAnticipos = (CxCdatosAnticiposDto) event.getObject();
		this.setLineaNegocio(itemAnticipos.getNoLinea());
		this.setDescripcionLineaNegocio(itemAnticipos.getDescripcion());
		this.setFechaAnticipo(itemAnticipos.getFecha());
		this.setNumero(itemAnticipos.getNoFisico());
		this.setValor(itemAnticipos.getSaldo());
		this.setDiferencia(itemAnticipos.getSaldo().setScale(2, RoundingMode.HALF_UP));
		this.setNumeroDocumentoSeleccionado(itemAnticipos.getNoDocu());
		obtenerObservacionCabecera();

	}

	public void agregarAnticiposSeleccionados() throws Exception {
		this.setListaNumerosFisicos(new ArrayList<>());
		this.setListaNumerosDocumentos(new ArrayList<>());
		this.numero = "";
		this.numeroDocumentoSeleccionado = "";
		this.documentosTomados = "";
		BigDecimal sumaValorAnticpos = BigDecimal.valueOf(0D);
		BigDecimal sumaValorDiferencias = BigDecimal.valueOf(0D);

		if (("D").equals(pOrigen)) {
			this.listaAnticiposSeleccionados = devAnticipoService.anticiposSeleccionados(this.pnoCia, this.pCedula,
					this.pnoSolicitud);
		}

		if (!validarValoresOcupar(this.listaAnticiposSeleccionados)) {
			for (CxCdatosAnticiposDto itemRecuperado : this.listaAnticiposSeleccionados) {
				setLineaNegocio(itemRecuperado.getNoLinea());
				setDescripcionLineaNegocio(itemRecuperado.getDescripcion());
				setFechaAnticipo(itemRecuperado.getFecha());
				this.numero += itemRecuperado.getNoFisico() + ",";
				this.documentosTomados += itemRecuperado.getTipoDoc() + " - " + itemRecuperado.getNoFisico() + ",";
				sumaValorAnticpos = sumaValorAnticpos
						.add(itemRecuperado.getSaldo() == null ? BigDecimal.valueOf(0D) : itemRecuperado.getSaldo());
				sumaValorDiferencias = sumaValorDiferencias
						.add(itemRecuperado.getValorTomar() == null ? BigDecimal.valueOf(0D)
								: itemRecuperado.getValorTomar());
				this.numeroDocumentoSeleccionado += itemRecuperado.getNoDocu() + ",";
			}
			setValor(sumaValorDiferencias.setScale(2, RoundingMode.HALF_UP));
			setDiferencia(sumaValorDiferencias.setScale(2, RoundingMode.HALF_UP));

			obtenerObservacionCabecera();
			recuperarArchivosXcodigoU(this.numero, this.cliente.getClientePK().getNoCliente().toString());
		} else {
			warn("Verificar Los montos a ocupar no deben ser mayores a los de los Anticipos");
		}
	}

	public boolean validarValoresOcupar(List<CxCdatosAnticiposDto> listaAnticiposRecuperados) throws Exception {
		try {
			for (CxCdatosAnticiposDto valorTomar : listaAnticiposRecuperados) {

				if (("D").equals(this.pOrigen)) {
					valorTomar.setValorTomar(valorTomar.getMontoUtilizado());
				}
				if (valorTomar.getSaldo() != null && valorTomar.getValorTomar() != null) {
					if (valorTomar.getSaldo().doubleValue() < valorTomar.getValorTomar().doubleValue()) {
						this.setVerificarMontosTomar(true);
						throw new Exception("Verificar - El Valor a ocupar no puede ser mayor al Valor del anticipo");

					} else {
						this.setVerificarMontosTomar(false);
					}
				}
			}
			return this.verificarMontosTomar;
		} catch (Exception e) {
			Log.error(e);
			return true;
		}
	}

	private String obtenerObservacionCabecera() {

		StringBuilder mensajeCabecera = new StringBuilder();
		mensajeCabecera.append(" EMISION CHEQUE CLIENTE POR INTERMEDIACION GARANTIA ");
		mensajeCabecera.append(getNumero());
		this.setComentarioEmisionChequeCabecera(mensajeCabecera.toString());
		return mensajeCabecera.toString();
	}

	public void obtenerComentarios(String valor) {
		StringBuilder mensajeCambioDinero = new StringBuilder();
		StringBuilder mensajeEmisionCheque = new StringBuilder();

		for (DatosMemorandoDto item : listaMemorandos) {
			mensajeCambioDinero.setLength(0);
			mensajeEmisionCheque.setLength(0);
			if (!item.getEmitirCheque().equals(CommonConstants.NO_STRING_VALUE)) {
				mensajeCambioDinero.append("CAMBIO DE ");
				mensajeCambioDinero.append(this.getNumero());
				mensajeCambioDinero.append(" POR ");
				mensajeCambioDinero.append(valor);
				mensajeCambioDinero.append(" A ");
				mensajeCambioDinero.append(item.getBeneficiario().toString());
				mensajeCambioDinero.append(" DE ");
				mensajeCambioDinero.append(this.cliente.getNombre());
				mensajeCambioDinero.append(" POR ");
				this.setComentarioCancelacionAnticipos(mensajeCambioDinero.toString());

				mensajeEmisionCheque.append("EMISION CHEQUE POR ");
				mensajeEmisionCheque.append(valor + " DE ");
				mensajeEmisionCheque.append(this.getCliente().getNombre());
				mensajeEmisionCheque.append(" NUMERO DE DIARIO ");
				mensajeEmisionCheque.append(this.getNoFisicoGeneracion());
				this.setComentarioEmisionCheque(mensajeEmisionCheque.toString());

			} else if (item.getEmitirCheque().equals(CommonConstants.NO_STRING_VALUE)) {
				mensajeCambioDinero.setLength(0);
				mensajeCambioDinero.append("CAMBIO DE ");
				mensajeCambioDinero.append(this.getNumero());
				mensajeCambioDinero.append(" POR ");
				mensajeCambioDinero.append(valor);
				mensajeCambioDinero.append(" A ");
				mensajeCambioDinero.append(item.getBeneficiario());
				mensajeCambioDinero.append(" DE ");
				mensajeCambioDinero.append(this.cliente.getNombre());
				mensajeCambioDinero.append(" POR ");
				this.setComentarioCancelacionAnticipos(mensajeCambioDinero.toString());
			}
		}

	}

	@SuppressWarnings({ "rawtypes" })
	public void obtenerMemorandos() {
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		String codigoU = "";
		sql.setLength(0);
		parametros.clear();

		for (CxCdatosAnticiposDto det : this.listaAnticiposSeleccionados) {
			codigoU += det.getNoFisico() + ",";
		}
		this.setCodigoU(codigoU);
		try {
			this.placaRecuperada = inventarioMasterService.buscarPlacaPorU(this.noCia, this.getCodigoU());
			Optional<String> placa = Optional.ofNullable(this.placaRecuperada);
			if (!placa.isPresent()) {
				warn("El vehiculo con codigo de U: " + this.getCodigoU() + " no tiene Placa.");
			}
		} catch (Exception e) {
			logger.error(e);
		}

		try {
			this.listaMemorandos = memorandoService.buscarDatosMemornadoXnoCiaXplacaXestado(this.noCia,
					this.placaRecuperada, ESTADO_MEMO);
			if (!this.listaMemorandos.isEmpty()) {
				for (DatosMemorandoDto item : this.listaMemorandos) {
					obtenerDiferencia(item);
				}

			} else {
				info("No se obtuvieron memorandos para el numero: " + this.codigoU);
			}

		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void cargarCtasBancarias() {
		try {
			this.setCtasBancariasSelectItems(new ArrayList<>());
			this.listaCtasBancarias = ctasbancariasService.buscarCtasBancariasPorCia(this.noCia);
			Optional<List<CxCdatosCtasBancariasDto>> cuentasBancarias = Optional.ofNullable(this.listaCtasBancarias);
			if (cuentasBancarias.isPresent()) {
				for (CxCdatosCtasBancariasDto item : this.listaCtasBancarias) {
					getCtasBancariasSelectItems().add(new SelectItem(item.getNoCuenta().trim().toUpperCase().toString(),
							item.getDescripcion().trim().toUpperCase().toString()));
				}
			}
		} catch (Exception e) {
			warn("No se recuperaron Cuentas Bancarias");
			logger.error(e);
		}
	}

	public void aniadirBeneficiarios() {
		if (this.getListaMemorandos() == null) {
			this.setListaMemorandos(new ArrayList<>());
			this.getListaMemorandos().add(new DatosMemorandoDto());
		} else {
			this.getListaMemorandos().add(new DatosMemorandoDto());
		}

	}

	public void removerBeneficiario(DatosMemorandoDto datoSeleccionado) {
		try {
			revertirDiferencia(datoSeleccionado);
			this.listaMemorandos.remove(datoSeleccionado);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro removido con Exito", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);

		} catch (Exception e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problemas para remover registro", "");
			FacesContext.getCurrentInstance().addMessage(null, msg);
			logger.error(e);
		}
	}

	public void procesar() {
		try {
			this.setDocumentoCabeceraCancelacion(null);
			this.documentosYvaloresAcancelar = "";
			this.valoresCancelar = null;

			if (controlCampos() == true) {
				// 'CUENTA9=5|CUENTA6=10|CUENTA7=15'

				for (CxCdatosAnticiposDto det : this.listaAnticiposSeleccionados) {
					this.documentosYvaloresAcancelar += det.getNoDocu() + "=" + det.getValorTomar() + "|";
					// this.valoresCancelar +=det.getValorTomar()+",";
				}
				if (!this.emiteCheque.equals(CommonConstants.NO_STRING_VALUE)
						&& !this.diferencia.equals(BigDecimal.valueOf(0D))) {
					obtenerComentarios(this.getDiferencia().toString());
					System.out.println(this.getDiferencia());

					String secuenciaChkAnticipos = generaCreacionCheque(getCompania().getNoCia(),
							getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(),
							this.cliente.getClientePK().getNoCliente(), this.cliente.getNombre(), this.lineaNegocio,
							this.getCtasBancarias(), this.getDiferencia().setScale(2, RoundingMode.HALF_UP),
							this.getDiferencia().setScale(2, RoundingMode.HALF_UP),
							"CREACION - " + " EMISION - " + this.emiteCheque + " " + this.getObservacionCabecera()
									+ " POR EL VALOR DE " + this.getDiferencia().setScale(2, RoundingMode.HALF_UP)
									+ " A LA " + this.getDocumentosTomados()// + this.getNumero().toString()
									+ " SOLIICTADO POR " + this.getUsuarioSolicita() + " GENERADO POR "
									+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
							this.usuarioSolicita, this.numeroDocumentoSeleccionado, this.emiteCheque);

					addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + secuenciaChkAnticipos);
					// CREAR ARCHIVO

					System.out.println(this.getDiferencia());
					String numeroCancelacionAnticipos = generarCancelacionAnticipos(getCompania().getNoCia(),
							getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(),
							this.cliente.getClientePK().getNoCliente(),
							this.getDiferencia().setScale(2, RoundingMode.HALF_UP), "EMISION - " + this.getEmiteCheque()
									+ " " + this.getObservacionCabecera() + " " + this.getDocumentosTomados()
									// + this.getNumero()
									+ " POR EL VALOR DE " + this.getDiferencia().setScale(2, RoundingMode.HALF_UP)
									+ " SOLIICTADO POR " + this.getUsuarioSolicita() + " GENERADO POR "
									+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
							// this.getDiferencia(),
							this.getValor().setScale(2, RoundingMode.HALF_UP), this.lineaNegocio,
							this.documentosYvaloresAcancelar, this.getDocumentoCabeceraCancelacion(),
							this.getCtasBancarias(), secuenciaChkAnticipos, this.uafAplica,
							this.cliente.getClientePK().getNoCliente().intValue(), null, null, null);
					addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + numeroCancelacionAnticipos);
					this.setDocumentoCabeceraCancelacion(numeroCancelacionAnticipos);
				} else if (this.emiteCheque.equals(CommonConstants.NO_STRING_VALUE)) {

					String numeroDocumento = generarCancelacionAnticipos(getCompania().getNoCia(),
							getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(),
							this.getCliente().getClientePK().getNoCliente(),
							this.getValor().setScale(2, RoundingMode.HALF_UP),
							"CANCELACION - " + "CAMBIO DE " + this.getDocumentosTomados() // + this.getNumero()
									+ " POR " + this.getValor() + " DE " + this.cliente.getNombre() + " POR "
									+ this.getObservacionCabecera() + " SOLIICTADO POR " + this.getUsuarioSolicita()
									+ " GENERADO POR " + getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
							this.getValor().setScale(2, RoundingMode.HALF_UP), this.lineaNegocio,
							this.documentosYvaloresAcancelar, // this.numeroDocumentoSeleccionado,
							this.getDocumentoCabeceraCancelacion(), null, null, this.uafAplica,
							this.cliente.getClientePK().getNoCliente().intValue(), null, null, null);
					addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + numeroDocumento);
					if (this.getDocumentoCabeceraCancelacion() == null) {
						this.setDocumentoCabeceraCancelacion(numeroDocumento);
					}
				}

				for (DatosMemorandoDto item : listaMemorandos) {
					if (item.getEmitirCheque().equals(CommonConstants.NO_STRING_VALUE)) {
						obtenerComentarios(item.getValor().toString());
						obtenerComentarios(item.getValor().toString());
						System.out.println(item.getValor());
						String nofisico = generarCreacionDiarioA(getCompania().getNoCia(),
								getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), item.getNoCliente(),
								item.getValor(), "CREACION - " + "CAMBIO DE " + this.getDocumentosTomados()// +
								// this.getNumero()
										+ " POR " + item.getValor() + " A " + item.getBeneficiario() + " DE "
										+ this.cliente.getNombre() + " POR " + item.getComentarioCambioDinero()
										+ " SOLIICTADO POR " + this.getUsuarioSolicita() + " GENERADO POR "
										+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
								item.getValor(), this.lineaNegocio, this.getDocumentoCabeceraCancelacion(), // this.numeroDocumentoSeleccionado,
								this.getDocumentoCabeceraCancelacion(), item.isAplicaUAF(),
								this.cliente.getClientePK().getNoCliente().intValue(), null, item.getCentroCosto(),
								item.getCodigoU());
						addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + nofisico);

					} else if (item.getEmitirCheque().equals("TR") || item.getEmitirCheque().equals("CK")) {
						System.out.println(item.getValor());

						System.out.println(item.getValor());
						String numeroDocumentoDiario = generarCreacionDiarioA(getCompania().getNoCia(),
								getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), item.getNoCliente(),
								item.getValor(), "CREACION - " + "CAMBIO DE " + this.getDocumentosTomados() // +
																											// this.getNumero()
										+ " POR " + item.getValor().setScale(2, RoundingMode.HALF_UP) + " A "
										+ item.getBeneficiario() + " DE " + this.cliente.getNombre() + " POR "
										+ item.getComentarioCambioDinero() + " SOLIICTADO POR "
										+ this.getUsuarioSolicita() + " GENERADO POR "
										+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
								item.getValor().setScale(2, RoundingMode.HALF_UP), this.lineaNegocio,
								this.getDocumentoCabeceraCancelacion(), // this.numeroDocumentoSeleccionado,
								this.getDocumentoCabeceraCancelacion(), item.isAplicaUAF(),
								this.cliente.getClientePK().getNoCliente().intValue(), null, item.getCentroCosto(),
								item.getCodigoU());
						System.out.println(item.getValor());
						addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + numeroDocumentoDiario);

						System.out.println(item.getValor());
						String secuenciaChk = generaCreacionCheque(getCompania().getNoCia(),
								getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), item.getNoCliente(),
								item.getBeneficiario(), this.lineaNegocio, item.getCtaBanco(), item.getValor(),
								item.getValor(), "CREACION - " + "EMISION - " + item.getEmitirCheque() + " "
										+ this.getDocumentosTomados()// +
										// this.getNumero()
										+ " POR " + item.getValor() + " DE " + this.getNumero() + " A "
										+ item.getBeneficiario() + " " + item.getComentarioEmisionCheque()
										+ " SOLIICTADO POR " + this.getUsuarioSolicita() + " GENERADO POR "
										+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
								this.usuarioSolicita, // this.getDocumentoCabeceraCancelacion()
								numeroDocumentoDiario, item.getEmitirCheque());
						System.out.println(item.getValor());

						addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + secuenciaChk);
						System.out.println(item.getValor());
						// CAMBIO DINERO PARA CANCELAR CHEQUE
						String numeroCancelacion = generarCancelacionAnticipos(getCompania().getNoCia(),
								getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), item.getNoCliente(),
								item.getValor().setScale(2, RoundingMode.HALF_UP),
								"CREACION - " + "EMISION - " + item.getEmitirCheque() + " " + numeroDocumentoDiario // +
																													// this.getNumero()
										+ " POR " + item.getValor() + " DE " + this.cliente.getNombre() + " A "
										+ item.getBeneficiario() + " " + item.getComentarioCambioDinero()
										+ " NUMERO DE DIARIO " + this.getNoFisicoGeneracion() + " SOLIICTADO POR "
										+ this.getUsuarioSolicita() + " GENERADO POR "
										+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario()
										+ this.getDocumentosTomados(),
								item.getValor().setScale(2, RoundingMode.HALF_UP), this.lineaNegocio,
								numeroDocumentoDiario, this.getDocumentoCabeceraCancelacion(), item.getCtaBanco(),
								secuenciaChk, item.isAplicaUAF(), this.cliente.getClientePK().getNoCliente().intValue(),
								null, null, item.getCodigoU());
						System.out.println(item.getValor());

						addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + numeroCancelacion);
					} else if (item.getEmitirCheque().equals("CT")) {
						String nofisicoDiario = generarCreacionDiarioA(getCompania().getNoCia(),
								getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), item.getNoCliente(),
								item.getValor().setScale(2, RoundingMode.HALF_UP),
								"CREACION - " + "UTILIZACION DINERO DE " + this.getDocumentosTomados()// +
										+ " POR " + item.getValor().setScale(2, RoundingMode.HALF_UP) + " POR "
										+ item.getConcepto() + " DEL CLIENTE " + this.cliente.getNombre() + " POR "
										+ item.getComentarioCambioDinero() + " SOLIICTADO POR "
										+ this.getUsuarioSolicita() + " GENERADO POR "
										+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
								item.getValor().setScale(2, RoundingMode.HALF_UP), this.lineaNegocio,
								this.getDocumentoCabeceraCancelacion(), // this.numeroDocumentoSeleccionado,
								this.getDocumentoCabeceraCancelacion(), item.isAplicaUAF(),
								this.cliente.getClientePK().getNoCliente().intValue(), null, null, item.getCodigoU());
						addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + nofisicoDiario);

						String numeroDocCancelacion = generarCancelacionAnticipos(getCompania().getNoCia(),
								getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), item.getNoCliente(),
								item.getValor().setScale(2, RoundingMode.HALF_UP),
								"CANCELACION - " + "UTILIZACION DINERO DEL " + nofisicoDiario + " DEL CLIENTE "
										+ this.cliente.getNombre() + " POR " + item.getComentarioCambioDinero()
										+ " SOLIICTADO POR " + this.getUsuarioSolicita() + " GENERADO POR "
										+ getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario(),
								item.getValor().setScale(2, RoundingMode.HALF_UP), this.lineaNegocio, nofisicoDiario,
								this.getDocumentoCabeceraCancelacion(), null, null, item.isAplicaUAF(),
								this.cliente.getClientePK().getNoCliente().intValue(), item.getCuentaContable(),
								item.getCentroCosto(), item.getCodigoU());
						addInfoMessage("INFO:", "NUMERO DOCUMENTO: " + numeroDocCancelacion);

					}
				}

				addInfoMessage("INFO:", " DATOS PROCESADOS CON EXITO");
				generarArchivoDiariosA(getCompania().getNoCia(), this.getDocumentoCabeceraCancelacion(), "disco");
				if ("D".equals(this.pOrigen)) {
					solicitudesService.actualizarNoDocuDiarioA(this.pnoCia, this.pnoSolicitud, this.pnoCliente,
							this.getDocumentoCabeceraCancelacion());
				}

				enviaMailInformativo(this.cliente.getClientePK().getNoCliente().toString(),
						this.getDocumentoCabeceraCancelacion());
				enviarMailDiariosChk(this.cliente.getClientePK().getNoCliente().toString(),
						this.getDocumentoCabeceraCancelacion());
				if (("D").equals(this.pOrigen)) {
					enviarCorreo(pnoCliente,pnoSolicitud,pnoCia);
				}		
				limpiarPantalla();

			}

		} catch (Exception e) {
			obtainException(e);
			logger.error(e);
			addErrorMessage("ERROR:", " NO SE PUDIERON PROCESAR DATOS" + obtainException(e));
		}

	}

	private String generarCancelacionAnticipos(String noCia, String centro, Long noCliente, BigDecimal sumaSaldo,
			String comentario, BigDecimal montoUtilizar, String lineaNegocio, String noDocumento, String noDocuOriginal,
			String ctaBancaria, String secuenciaCHK, boolean aplicaUAF, Integer clienteOriginal, String ctaContable,
			String centroCostos, String codigoUSeleccionado) throws EJBTransactionRolledbackException, Exception {

		String error = null;
		String respuesta = null;
		Connection conn = null;
		CallableStatement cstmnt = null;
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();

		try {
			conn = utilServiceDelegate.getDataSource().getConnection();

			String storeCall = "{call GENERACION_DIARIOS_A.GENERA_DIARIOA_CANCELAANT(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			cstmnt = conn.prepareCall(storeCall);
			cstmnt.setString(1, noCia);
			cstmnt.setString(2, centro);
			cstmnt.setLong(3, noCliente);
			cstmnt.setBigDecimal(4, sumaSaldo);
			cstmnt.setString(5, comentario);
			cstmnt.setBigDecimal(6, montoUtilizar);
			cstmnt.setString(7, lineaNegocio);
			cstmnt.setString(8, noDocumento);
			cstmnt.setString(9, noDocuOriginal);
			cstmnt.setString(10, ctaBancaria);
			cstmnt.setString(11, secuenciaCHK);
			cstmnt.setString(12, aplicaUAF ? CommonConstants.YES_STRING_VALUE : CommonConstants.NO_STRING_VALUE);
			cstmnt.setInt(13, clienteOriginal);
			cstmnt.setString(14, ctaContable);
			cstmnt.setString(15, centroCostos);
			cstmnt.setString(16, codigoUSeleccionado);
			cstmnt.registerOutParameter(17, java.sql.Types.VARCHAR);
			cstmnt.registerOutParameter(18, java.sql.Types.VARCHAR);
			cstmnt.execute();
			error = cstmnt.getString(17);
			respuesta = cstmnt.getString(18);
			if (error != null && error.length() > 0) {
				throw new GeneralException("ERROR: ", error.toString());
			}

			return respuesta;
		} catch (EJBTransactionRolledbackException e) {
			obtainException(e);
			e.printStackTrace();
		} finally {
			try {
				if (cstmnt != null)
					cstmnt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				obtainException(e);
				e.printStackTrace();
			}
		}
		return respuesta;

	}

	private String generarCreacionDiarioA(String noCia, String centro, Long noCliente, BigDecimal sumaSaldo,
			String comentario, BigDecimal montoUtilizar, String lineaNegocio, String noDocumento, String noDocuOriginal,
			boolean aplicaUAF, Integer clienteOriginal, String ctaContable, String centroCostos,
			String codigoUSeleccionado) throws EJBTransactionRolledbackException, Exception {

		String error = null;
		String respuesta = null;
		String noFisico = null;
		Connection conn = null;
		CallableStatement cstmnt = null;
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		try {
			conn = utilServiceDelegate.getDataSource().getConnection();

			String storeCall = "{call GENERACION_DIARIOS_A.genera_diarioa_creaant(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			cstmnt = conn.prepareCall(storeCall);
			cstmnt.setString(1, noCia);
			cstmnt.setString(2, centro);
			cstmnt.setLong(3, noCliente);
			cstmnt.setBigDecimal(4, sumaSaldo);
			cstmnt.setString(5, comentario);
			cstmnt.setBigDecimal(6, montoUtilizar);
			cstmnt.setString(7, lineaNegocio);
			cstmnt.setString(8, noDocumento);
			cstmnt.setString(9, noDocuOriginal);
			cstmnt.setString(10, aplicaUAF ? CommonConstants.YES_STRING_VALUE : CommonConstants.NO_STRING_VALUE);
			cstmnt.setInt(11, clienteOriginal);
			cstmnt.setString(12, ctaContable);
			cstmnt.setString(13, centroCostos);
			cstmnt.setString(14, codigoUSeleccionado);
			cstmnt.registerOutParameter(15, java.sql.Types.VARCHAR);
			cstmnt.registerOutParameter(16, java.sql.Types.VARCHAR);
			cstmnt.registerOutParameter(17, java.sql.Types.VARCHAR);
			cstmnt.execute();
			error = cstmnt.getString(15);
			respuesta = cstmnt.getString(16);
			noFisico = cstmnt.getString(17);
			this.setNoFisicoGeneracion(noFisico);

			if (error != null && error.length() > 0) {
				throw new GeneralException("ERROR: ", error);
			}

			return respuesta;
		} catch (EJBTransactionRolledbackException e) {
			obtainException(e);
			e.printStackTrace();
		} finally {
			try {
				if (cstmnt != null)
					cstmnt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				obtainException(e);
				e.printStackTrace();
			}
		}
		return noFisico;

	}

	@SuppressWarnings("unused")
	public String generaCreacionCheque(String noCia, String centro, Long noCliente, String beneficiario,
			String lineaNegocio, String ctaBancaria, BigDecimal valorAnticipo, BigDecimal montoUtilizar,
			String comentario, String usuarioSolicita, String noDocumento, String tipoTransaccion)
			throws EJBTransactionRolledbackException, Exception {

		String error = null;
		String respuesta = null;
		String secuenciaChk = null;
		Connection conn = null;
		CallableStatement cstmnt = null;
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();

		try {
			conn = utilServiceDelegate.getDataSource().getConnection();

			String storeCall = "{call GENERACION_DIARIOS_A.GRABAR_CHEQUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
			cstmnt = conn.prepareCall(storeCall);
			cstmnt.setString(1, noCia);
			cstmnt.setString(2, centro);
			cstmnt.setLong(3, noCliente);
			cstmnt.setString(4, beneficiario);
			cstmnt.setString(5, lineaNegocio);
			cstmnt.setString(6, ctaBancaria);
			cstmnt.setBigDecimal(7, valorAnticipo);
			cstmnt.setBigDecimal(8, montoUtilizar);
			cstmnt.setString(9, comentario);
			cstmnt.setString(10, usuarioSolicita);
			cstmnt.setString(11, noDocumento);
			cstmnt.setString(12, tipoTransaccion);
			cstmnt.registerOutParameter(13, java.sql.Types.VARCHAR);
			cstmnt.registerOutParameter(14, java.sql.Types.VARCHAR);
			cstmnt.registerOutParameter(15, java.sql.Types.VARCHAR);
			cstmnt.execute();
			error = cstmnt.getString(13);
			respuesta = cstmnt.getString(14);
			secuenciaChk = cstmnt.getString(15);

			if (error != null && error.length() > 0) {
				throw new GeneralException("ERROR: ", error.toString());
			}

			return secuenciaChk;
		} catch (EJBTransactionRolledbackException e) {
			obtainException(e);
			e.printStackTrace();
		} finally {
			try {
				if (cstmnt != null)
					cstmnt.close();
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				obtainException(e);
				e.printStackTrace();
			}
		}
		return secuenciaChk;

	}

	public void obtenerDiferencia(DatosMemorandoDto cxcDatosMemorandoDto) {
		BigDecimal sumaValor = BigDecimal.valueOf(0D);
		BigDecimal valorDiferencia = BigDecimal.valueOf(0D);

		for (DatosMemorandoDto item : listaMemorandos) {
			if (item.getValor() == null) {
				sumaValor = sumaValor.add(BigDecimal.valueOf(0D));
			} else {
				sumaValor = sumaValor.add(item.getValor().setScale(2, RoundingMode.HALF_UP));
			}

			sumaValor = sumaValor.setScale(2, RoundingMode.HALF_UP);

			valorDiferencia = this.valor.subtract(sumaValor.setScale(2, RoundingMode.HALF_UP));
			valorDiferencia = valorDiferencia.setScale(2, RoundingMode.HALF_UP);

			if (valorDiferencia.compareTo(BigDecimal.valueOf(0D)) > 0) {
				warn("VERIFICAR NO DEBE QUEDAR SALDO A FAVOR DEL CLIENTE PARA PROCESAR");
			}

			if (valorDiferencia.compareTo(BigDecimal.valueOf(0D)) < 0) {
				// warn("VERIFICAR LA SUMA DE LOS VALORES NO PUEDE SER MAYOR QUE EL SALDO");
				this.setDiferencia(BigDecimal.valueOf(0D));
				this.aplicaDiferencia = true;
			} else {
				this.aplicaDiferencia = false;
				this.setDiferencia(valorDiferencia.setScale(2, RoundingMode.HALF_UP));
			}
		}

		if (this.valor.setScale(2, RoundingMode.HALF_UP).equals(sumaValor.setScale(2, RoundingMode.HALF_UP))) {
			info("LOS VALORES SE ENCUENTRAN CUADRADOS PUEDE PROCESAR LOS DIARIOS");
			this.desactivarBtnProcesar = true;
		} else {
			warn("VERIFICAR LA SUMA DE LOS VALORES NO PUEDE SER MAYOR O MENOR QUE EL SALDO A FAVOR DEL CLIENTE");
			this.desactivarBtnProcesar = false;
		}

		int indice = this.listaMemorandos.indexOf(cxcDatosMemorandoDto);

		super.updateComponentFromId("form:idValorDiferencia");
		super.updateComponentFromId("form:idBeneficiariosTable:" + indice + ":idOTsumaTotalXBeneficiarios");

	}

	public void revertirDiferencia(DatosMemorandoDto cxcDatosMemorandoDto) {
		BigDecimal sumaValor = BigDecimal.valueOf(0D);
		BigDecimal valorDiferencia = BigDecimal.valueOf(0D);
		obtenerDiferencia(cxcDatosMemorandoDto);
		if (cxcDatosMemorandoDto.getValor() == null) {
			sumaValor = sumaValor.add(BigDecimal.valueOf(0D));
		} else {
			sumaValor = sumaValor.add(cxcDatosMemorandoDto.getValor().setScale(2, RoundingMode.HALF_UP));
		}

		sumaValor = sumaValor.setScale(2, RoundingMode.HALF_UP);

		if (this.aplicaDiferencia) {
			this.setDiferencia(BigDecimal.valueOf(0D));
		} else {
			valorDiferencia = this.diferencia.add(sumaValor.setScale(2, RoundingMode.HALF_UP));
			valorDiferencia = valorDiferencia.setScale(2, RoundingMode.HALF_UP);
			this.setDiferencia(valorDiferencia.setScale(2, RoundingMode.HALF_UP));
		}

		int indice = this.listaMemorandos.indexOf(cxcDatosMemorandoDto);

		super.updateComponentFromId("form:idValorDiferencia");
		super.updateComponentFromId("form:idBeneficiariosTable:" + indice + ":idOTsumaTotalXBeneficiarios");

	}

	public BigDecimal getSumaTotalesXbeneficiarios() {
		BigDecimal sumaValor = BigDecimal.valueOf(0D);

		for (DatosMemorandoDto item : listaMemorandos) {
			if (item.getValor() == null) {
				sumaValor = sumaValor.add(BigDecimal.valueOf(0D));
			} else {
				sumaValor = sumaValor.add(item.getValor().setScale(2, RoundingMode.HALF_UP));
			}

			sumaValor = sumaValor.setScale(2, RoundingMode.HALF_UP);
		}
		this.setSumaTotalesXbeneficiarios(sumaValor.setScale(2, RoundingMode.HALF_UP));
		return sumaTotalesXbeneficiarios;
	}

	public BigDecimal getSumaTotalAnticiposSeleccionados() {
		BigDecimal sumaValorAnticiposSeleccionados = BigDecimal.valueOf(0D);

		for (CxCdatosAnticiposDto item : listaAnticiposSeleccionados) {
			if (item.getSaldo() == null) {
				sumaValorAnticiposSeleccionados = sumaValorAnticiposSeleccionados.add(BigDecimal.valueOf(0D));
			} else {
				sumaValorAnticiposSeleccionados = sumaValorAnticiposSeleccionados
						.add(item.getSaldo().setScale(2, RoundingMode.HALF_UP));
			}

			sumaValorAnticiposSeleccionados = sumaValorAnticiposSeleccionados.setScale(2, RoundingMode.HALF_UP);
		}
		this.setSumaTotalAnticiposSeleccionados(sumaValorAnticiposSeleccionados.setScale(2, RoundingMode.HALF_UP));
		return sumaTotalAnticiposSeleccionados;
	}

	public BigDecimal getTotalValorATomar() {
		BigDecimal sumaValorAocupar = BigDecimal.valueOf(0D);

		for (CxCdatosAnticiposDto item : listaAnticiposSeleccionados) {
			if (item.getValorTomar() == null) {
				sumaValorAocupar = sumaValorAocupar.add(BigDecimal.valueOf(0D));
			} else {
				sumaValorAocupar = sumaValorAocupar.add(item.getValorTomar().setScale(2, RoundingMode.HALF_UP));
			}

			sumaValorAocupar = sumaValorAocupar.setScale(2, RoundingMode.HALF_UP);
		}
		return sumaValorAocupar;

	}

	@SuppressWarnings("rawtypes")
	public void enviaMailInformativo(String noCliente, String nofisico) {
		HashMap<String, String> parametros = new HashMap<>();
		StringBuffer sql = new StringBuffer();
		String emailAdministrador = null;
		String nombreAdministrador = null;
		StringBuffer mensaje = new StringBuffer();
		List<String> gruposJefes = new ArrayList<String>();

		mensaje.append("<div style='font-size:12px;'>");
		mensaje.append("Le informamos que se realizo la creacion de Diario A del siguiente cliente: <br><br>");

		mensaje.append(
				"<table border='1' cellspacing='0' cellpadding='1' style='font-size:11px; border-collapse:collapse;' >");
		mensaje.append("<tr style='background: #A9A9A9' >");
		mensaje.append("<td><b>No. Documento1</b></td>");
		mensaje.append("<td><b>No. Cliente</b></td>");
		mensaje.append("<td><b>Cedula</b></td>");
		mensaje.append("<td><b>Beneficiarios</b></td>");
		mensaje.append("<td><b>Monto</b></td>");
		mensaje.append("<td><b>Comentario</b></td>");
		mensaje.append("<td><b>Docu.Refer</b></td>");
		mensaje.append("</tr>");

		List<CxCdatosComprobantesDiariosDto> listaComprobantes;
		try {
			listaComprobantes = cxcDatosComprobatesDiariosService.buscarComprobantesDiarios(getCompania().getNoCia(),
					nofisico.toString());

			for (CxCdatosComprobantesDiariosDto item : listaComprobantes) {

				// Llenamos mapa de diarios por cliente
				if (mapaDiariosCliente.containsKey(item.getNoCliente())) {
					List<String> diarios = mapaDiariosCliente.get(item.getNoCliente());
					diarios.add(item.getNoDocumento());
					mapaDiariosCliente.replace(item.getNoCliente(), diarios);
				} else {
					List<String> diarios = new ArrayList<>();
					diarios.add(item.getNoDocumento());
					mapaDiariosCliente.put(item.getNoCliente(), diarios);
				}

				mensaje.append("<tr ");

				if (listaComprobantes.indexOf(item) % 2 == 0) {
					mensaje.append("style='background: #e2e2e2'");
				}

				mensaje.append(">");
				mensaje.append("<td> ");
				mensaje.append(item.getNoFisico().trim().toUpperCase());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getNoCliente().trim().toUpperCase());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getCedula().toString().toUpperCase().trim());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getNombre().toString().toUpperCase().trim());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getMontoOriginal().toString().toUpperCase().trim());
				mensaje.append("</td>");

				if (item.getComentario() != null) {
					mensaje.append("<td> ");
					mensaje.append(item.getComentario().toString().toUpperCase().trim());
					mensaje.append("</td>");
				} else {
					mensaje.append("<td> ");
					mensaje.append(" - ");
					mensaje.append("</td>");
				}
				mensaje.append("<td> ");
				mensaje.append(item.getNoDocumento().trim().toUpperCase());
				mensaje.append("</td>");
				mensaje.append("</tr>");

			}

			mensaje.append("</table>");

			mensaje.append("<br><br><br>Saludos Cordiales.");
			mensaje.append("</div>");

			// Creamos estructura revision de diarios A
			almacenarAdjuntosDiarios();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try

		{

			parametros.clear();
			sql.append("SELECT U.EMAIL, U.NOMBRE");
			sql.append(" FROM USUARIOS_SIS U");
			sql.append(" WHERE U.USUARIO =:usuario");
			parametros.put("usuario", getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario().trim());
			List usuariosList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null, 0, 0);
			if (!usuariosList.isEmpty()) {

				emailAdministrador = (String) ((Object[]) usuariosList.get(0))[0];
				nombreAdministrador = (String) ((Object[]) usuariosList.get(0))[1];
			}

			String emailNotificaciones = null;
			List<ParamDet> remitente = paramDetService.consultarPorCodigoCab(this.getCompania().getNoCia(),
					CommonConstants.CODIGO_CATALOGO_MAIL_NOTIFICACIONES);
			emailNotificaciones = remitente != null && !remitente.isEmpty() ? remitente.get(0).getTexto1() : null;
			if (emailNotificaciones == null) {
				warn("No cuenta con um mail de notificaciones parametrizado en ParamDet Codigo "
						+ CommonConstants.CODIGO_CATALOGO_MAIL_NOTIFICACIONES);
			} else {
				// ENVIO MAIL USUARIO QUE GNERO DIARIO
				mailService.sendEmail(emailNotificaciones, emailAdministrador, nombreAdministrador,
						"GENERACION DE COMPROBANTES DIARIOS " + getCompania().getNombre(), mensaje, true);
			}
			// GRUPO DE MAIL CREDITO - CONTABILIDAD - COBRANZAS
			gruposJefes.add("GDA");
			envioMail("GENERACION DE COMPROBANTES DIARIOS " + getCompania().getNombre(), mensaje, gruposJefes, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("rawtypes")
	public void enviarMailDiariosChk(String noCliente, String nofisico) {
		HashMap<String, String> parametros = new HashMap<>();
		StringBuffer sql = new StringBuffer();
		String emailAdministrador = null;
		String nombreAdministrador = null;
		StringBuffer mensaje = new StringBuffer();
		List<String> gruposJefes = new ArrayList<String>();

		mensaje.append("<div style='font-size:12px;'>");
		mensaje.append(
				"Le informamos que se requiere realizar la siguiente transacci�n que se detalla a continuacion: <br><br>");

		mensaje.append(
				"<table border='1' cellspacing='0' cellpadding='1' style='font-size:11px; border-collapse:collapse;' >");
		mensaje.append("<tr style='background: #A9A9A9' >");
		mensaje.append("<td><b>No. Documento1</b></td>");
		mensaje.append("<td><b>No. Cliente</b></td>");
		mensaje.append("<td><b>Cedula</b></td>");
		mensaje.append("<td><b>Beneficiarios</b></td>");
		mensaje.append("<td><b>Monto</b></td>");
		mensaje.append("<td><b>Comentario</b></td>");
		mensaje.append("<td><b>Docu.Refer</b></td>");
		mensaje.append("</tr>");

		List<CxCdatosComprobantesDiariosDto> listaComprobantesChk;
		try {
			listaComprobantesChk = cxcDatosComprobatesDiariosService
					.buscarComprobantesDiariosChk(getCompania().getNoCia(), nofisico.toString());

			for (CxCdatosComprobantesDiariosDto item : listaComprobantesChk) {

				mensaje.append("<tr ");

				if (listaComprobantesChk.indexOf(item) % 2 == 0) {
					mensaje.append("style='background: #e2e2e2'");
				}

				mensaje.append(">");
				mensaje.append("<td> ");
				mensaje.append(item.getNoFisico().trim().toUpperCase());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getNoCliente().trim().toUpperCase());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getCedula().toString().toUpperCase().trim());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getNombre().toString().toUpperCase().trim());
				mensaje.append("</td>");

				mensaje.append("<td> ");
				mensaje.append(item.getMontoOriginal().toString().toUpperCase().trim());
				mensaje.append("</td>");

				if (item.getComentario() != null) {
					mensaje.append("<td> ");
					mensaje.append(item.getComentario().toString().toUpperCase().trim());
					mensaje.append("</td>");
				} else {
					mensaje.append("<td> ");
					mensaje.append(" - ");
					mensaje.append("</td>");
				}
				mensaje.append("<td> ");
				mensaje.append(item.getNoDocumento().trim().toUpperCase());
				mensaje.append("</td>");
				mensaje.append("</tr>");

			}

			mensaje.append("</table>");

			mensaje.append("<br><br><br>Saludos Cordiales.");
			mensaje.append("</div>");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try

		{

			parametros.clear();
			sql.append("SELECT U.EMAIL, U.NOMBRE");
			sql.append(" FROM USUARIOS_SIS U");
			sql.append(" WHERE U.USUARIO =:usuario");
			parametros.put("usuario", getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario().trim());
			List usuariosList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null, 0, 0);
			if (usuariosList.size() > 0) {

				emailAdministrador = (String) ((Object[]) usuariosList.get(0))[0];
				nombreAdministrador = (String) ((Object[]) usuariosList.get(0))[1];
			}

			String emailNotificaciones = null;
			List<ParamDet> remitente = paramDetService.consultarPorCodigoCab(this.getCompania().getNoCia(),
					CommonConstants.CODIGO_CATALOGO_MAIL_NOTIFICACIONES);
			emailNotificaciones = remitente != null && !remitente.isEmpty() ? remitente.get(0).getTexto1() : null;
			if (emailNotificaciones == null) {
				warn("No cuenta con um mail de notificaciones parametrizado en ParamDet Codigo "
						+ CommonConstants.CODIGO_CATALOGO_MAIL_NOTIFICACIONES);
			} else {
				// ENVIO MAIL USUARIO QUE GNERO DIARIO
				mailService.sendEmail(emailNotificaciones, emailAdministrador, nombreAdministrador,
						"DIARIOS EMISION GENERACION CHEQUE O TRANSFERENCIA " + getCompania().getNombre(), mensaje,
						true);
			}

			// GRUPO DE MAIL CREDITO - TESORERIA
			if (CommonConstants.CASABACA.equals(getCompania().getNoCia())) {
				gruposJefes.add("CKDA");
				envioMail("DIARIOS EMISION GENERACION CHEQUE O TRANSFERENCIA " + getCompania().getNombre(), mensaje,
						gruposJefes, null);
			} else {
				gruposJefes.add("CKD" + getCompania().getNoCia());
				envioMail("DIARIOS EMISION GENERACION CHEQUE O TRANSFERENCIA " + getCompania().getNombre(), mensaje,
						gruposJefes, null);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void almacenarAdjuntosDiarios() {
		Set<String> clientes = this.mapaDiariosCliente.keySet();
		for (String item : clientes) {
			for (String docu : this.mapaDiariosCliente.get(item)) {
				CxcRevisionDiarios revision = new CxcRevisionDiarios();
				CxcRevisionDiariosPK primaria = new CxcRevisionDiariosPK(getCompania().getNoCia(), docu);
				revision.setRevisionDiarioPk(primaria);
				String adjuntos = new String();
				if (this.mapaArchivosCliente.containsKey(item) && this.mapaArchivosCliente.get(item) != null
						&& !this.mapaArchivosCliente.get(item).isEmpty()) {
					for (String ruta : this.mapaArchivosCliente.get(item)) {
						if (adjuntos.isEmpty()) {
							adjuntos = ruta;
						} else {
							adjuntos = adjuntos + ";" + ruta;
						}
					}
				}
				revision.setAdjuntos(adjuntos);
				revision.setRevAprobada(CommonConstants.NO_STRING_VALUE);
				revision.setUsuario(getUsuario().getUsuario());
				revision.setFecha(new Date());
				try {
					revisionServiceLocal.crear(revision);
				} catch (InsertException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void envioMail(String subject, StringBuffer mensaje, List<String> gruposJefes,
			List<DireccionesMail> mailsIndividuales) throws Exception, Exception {
		if (gruposJefes != null && gruposJefes.size() > 0) {
			for (int i = 0; i < gruposJefes.size(); i++) {
				mailService.sendMailToGroup(gruposJefes.get(i), getUsuario().getEmail(), subject, mensaje, true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void generarArchivoDiariosA(String noCia, String noDocu, String tipoDestino) {

		List<CxCdatosComprobantesDiariosDto> listaComprobantes;
		try {
			listaComprobantes = cxcDatosComprobatesDiariosService.buscarComprobantesDiarios(getCompania().getNoCia(),
					noDocu.toString());

			for (CxCdatosComprobantesDiariosDto item : listaComprobantes) {
				String reportPath = "/reportes/cxcComprobanteDiariosA.jasper";
				UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
				Connection connection = null;
				JasperPrint jasperPrint = null;

				try {
					Map<String, Object> parameters = new HashMap<String, Object>();
					setParametersDiariosA(parameters, noCia, item.getNoDocumento());
					connection = utilServiceDelegate.getDataSource().getConnection();
					String ctxPath = getServletContext().getRealPath("/");

					String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance()
							.getExternalContext().getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
							+ System.getProperty("file.separator");

					JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
					parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

					jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);
					StringBuffer header = new StringBuffer();
					HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
							.getExternalContext().getResponse();

					if (tipoDestino.compareTo("stream") == 0) {// Mostrar en la pagina web
						header.append("inline; filename=\"");
						header.append("comprobantesDiarioA.pdf");
						header.append("\"");
						JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

						FacesContext.getCurrentInstance().getApplication().getStateManager()
								.saveView(FacesContext.getCurrentInstance());
						FacesContext.getCurrentInstance().responseComplete();
						response.getOutputStream().flush();
						response.getOutputStream().close();
					} else if (tipoDestino.compareTo("disco") == 0) {// guardar en el disco duro
						this.setRutaPdfMail(System.getProperty("file.separator") + "opt"
								+ System.getProperty("file.separator") + "diariosA"
								+ System.getProperty("file.separator") + item.getNoFisico() + "_" + item.getNombre()
								+ "_" + getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario() + ".pdf");
						JasperExportManager.exportReportToPdfFile(jasperPrint, this.getRutaPdfMail());
					}
					virtualizer.cleanup();
				} catch (SQLException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (JRException ex) {
					ex.printStackTrace();
				} finally {
					try {
						if (connection != null) {
							connection.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void setParametersDiariosA(Map<String, Object> parameters, String noCia, String noDocu) {
		parameters.put("P_NO_CIA", noCia);
		parameters.put("P_NO_DOCU", noDocu);
		parameters.put("USUARIO", getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
		parameters.put("EMPRESA", getCompania().getNombre());
		parameters.put("SUBREPORT_DIR", getPathReal());

	}

	public boolean controlCampos() {
		boolean valida = true;
		if (this.getEmiteCheque().equals(CommonConstants.YES_STRING_VALUE)) {
			if (this.ctasBancarias == null) {
				addErrorMessage("ERROR:", "LA CUENTA BANCARIA ES REQUERIDA");
				valida = false;
			}
			if (this.usuarioSolicita == null) {
				addErrorMessage("ERROR:", "EL USUARIO QUE SOLICITA ES REQUERIDO");
				valida = false;
			}
		}

		if (listaMemorandos.size() > 0) {
			for (DatosMemorandoDto item : listaMemorandos) {
				if (!item.getEmitirCheque().equals(CommonConstants.NO_STRING_VALUE)
						&& !item.getEmitirCheque().equals("CT")) {
					if (item.getCtaBanco() == null) {
						addErrorMessage("ERROR:", "LA CUENTA BANCARIA ES REQUERIDA");
						valida = false;
					}
					if (this.usuarioSolicita == null) {
						addErrorMessage("ERROR:", "EL USUARIO QUE SOLICITA ES REQUERIDO");
						valida = false;
					}

				}
			}
		}

		if (this.cliente.getCedula() == null || this.cliente.getNombre() == null || this.lineaNegocio == null
				|| this.descripcionLineaNegocio == null || this.fechaAnticipo == null || this.numero == null
				|| this.valor == null) {
			addErrorMessage("ERROR:", "PARA PROCESAR LLENE LOS CAMPOS");
			valida = false;
		}

		if (!("D").equals(this.pOrigen)) {
			if (mapaArchivosCliente == null || mapaArchivosCliente.isEmpty()) {
				addErrorMessage("ERROR: ", "LOS ADJUNTOS SON OBLIGATORIOS");
				valida = false;
			}
		}
		return valida;

	}

	public void abriRevisar(String noCliente) {
		this.noClienteArchivo = noCliente;

		this.listaAdjuntos = new ArrayList<>();
		this.archivosAdjuntos = new HashMap<>();

		recuperarArchivosXcodigoU(this.codigoU, this.noClienteArchivo);
		// Cargarmos los archivos subidos por cliente
		if (this.mapaArchivosCliente != null && this.mapaArchivosCliente.containsKey(this.noClienteArchivo)) {
			List<String> a = this.mapaArchivosCliente.get(this.noClienteArchivo);
			for (String arch : a) {
				String nombreArchivo = arch;
				if (arch.contains("/")) {
					nombreArchivo = arch.substring(arch.lastIndexOf("/"));
				} else if (arch.contains("\\")) {
					nombreArchivo = arch.substring(arch.lastIndexOf("\\"));
				}
				this.archivosAdjuntos.put(nombreArchivo, arch);
				this.listaAdjuntos.add(nombreArchivo);
			}
		}

		accionesDialog("DlgAdjuntos", true);
	}

	public void cargarArchivo(FileUploadEvent event) {
		try {
			UploadedFile uf = event.getFile();
			byte[] buffer = IOUtils.toByteArray(uf.getInputstream());
			InputStream is1 = new ByteArrayInputStream(buffer);
			procesarArchivo(is1, uf);
		} catch (IOException e) {
			e.printStackTrace();
			error(this.obtainException(e));
		}
	}

	public void procesarDocumentos() {
		try {
			InputStream is1;
			is1 = file.getInputstream();
			procesarArchivo(is1, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Alamacenamos el archivo subido en el mapa por cliente
	 * 
	 * @param is1
	 * @param uf
	 */
	private void procesarArchivo(InputStream is1, UploadedFile uf) {
		try {
			// Para el servidor Linux
			String raizDiarios = System.getProperty("file.separator") + "opt" + System.getProperty("file.separator")
					+ "diariosA" + System.getProperty("file.separator");

			File folderGeneral = new File(
					raizDiarios + getCompania().getNoCia() + System.getProperty("file.separator") + "Adjuntos");
			if (!folderGeneral.exists()) {
				folderGeneral.mkdir();
			}
			raizDiarios = raizDiarios + getCompania().getNoCia() + System.getProperty("file.separator") + "Adjuntos"
					+ System.getProperty("file.separator");
			File folder = new File(raizDiarios + this.numeroDocumentoSeleccionado);
			if (!folder.exists()) {
				folder.mkdir();
			}

			raizDiarios = raizDiarios + this.numeroDocumentoSeleccionado + System.getProperty("file.separator");

			File archivo = new File(raizDiarios + this.noClienteArchivo + "_" + uf.getFileName());
			String urlArchivo = raizDiarios + this.noClienteArchivo + "_" + uf.getFileName();
			copyInputStreamToFile(is1, archivo);
			if (this.mapaArchivosCliente == null) {
				this.mapaArchivosCliente = new HashMap<>();
			}

			if (this.mapaArchivosCliente.containsKey(this.noClienteArchivo)) {
				List<String> a = this.mapaArchivosCliente.get(this.noClienteArchivo);
				a.add(urlArchivo);
				this.mapaArchivosCliente.replace(this.noClienteArchivo, a);
			} else {
				List<String> a = new ArrayList<>();
				a.add(urlArchivo);
				this.mapaArchivosCliente.put(this.noClienteArchivo, a);
			}
			this.archivosAdjuntos.put(uf.getFileName(), urlArchivo);
			this.listaAdjuntos.add(uf.getFileName());

			System.out.println(">>>>>>MAPA ARCHIVO " + this.mapaArchivosCliente);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

		try (FileOutputStream outputStream = new FileOutputStream(file)) {

			int read;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			// commons-io
			IOUtils.copy(inputStream, outputStream);
		}
	}

	public void limpiarPanelClientes() {
		this.setCedula(null);
		this.setNombres(null);
		this.listarClientes = new ArrayList<>();
	}

	public void limpiarPanelClientesBeneficiarios() {
		this.setCedulaBeneficiario(null);
		this.setNombresBeneficiario(null);
		this.listarClientesBeneficiarios = new ArrayList<>();
	}

	public void verAdjunto(String nombreArchivo) {
		try {
			String destPath = null;
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			destPath = archivosAdjuntos.get(nombreArchivo);

			File file = new File(destPath);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				byte[] fileBytes = file.getName().getBytes();

				StringBuffer header = new StringBuffer();
				header.append("filename=\"");
				header.append(nombreArchivo);
				header.append("\"");
				response.setHeader("Content-Disposition", header.toString());
				response.setContentType(getTipoAplicacion(nombreArchivo));
				response.setContentLength(fileBytes.length);
				response.setContentLength((int) length);
				while ((in != null) && ((length = in.read(buf)) != -1)) {
					response.getOutputStream().write(buf, 0, (int) length);
				}
				in.close();
				response.getOutputStream().close();

				ctx.responseComplete();
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getTipoAplicacion(String fileName) {
		if (fileName != null) {
			if (fileName.indexOf(DOC_FILE) > 0) {
				return DOC_APPLICATION;
			}
			if (fileName.indexOf(XLS_FILE) > 0) {
				return XLS_APPLICATION;
			}
			if (fileName.indexOf(PDF_FILE) > 0) {
				return PDF_APPLICATION;
			}
		}
		return null;
	}

	public void quitarArchivo(String nombreArchivo) {

		if (mapaArchivosCliente != null && mapaArchivosCliente.containsKey(noClienteArchivo)) {
			List<String> a = mapaArchivosCliente.get(noClienteArchivo);
			a.remove(archivosAdjuntos.get(nombreArchivo));
			mapaArchivosCliente.replace(noClienteArchivo, a);
		}

		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos.remove(nombreArchivo);
	}

	public void limpiarPantalla() {
		this.setCedula(null);
		this.setNombres(null);
		this.setCedulaBeneficiario(null);
		this.setNombresBeneficiario(null);
		this.cliente = new Cliente();
		this.clienteBeneficiarios = new Cliente();
		this.setLineaNegocio(null);
		this.setDescripcionLineaNegocio(null);
		this.setFechaAnticipo(null);
		this.setNumero(null);
		this.setValor(null);
		this.setSumaTotalesXbeneficiarios(BigDecimal.valueOf(0D));
		this.setDiferencia(null);
		this.setObservacionCabecera(null);
		this.setEmiteCheque(null);
		this.setCtasBancarias(null);
		this.setUsuarioSolicita(null);
		this.setUsuarioSolicita(null);
		this.setComentarioEmisionChequeCabecera(null);
		this.setComentarioEmisionCheque(null);
		this.setComentarioCancelacionAnticipos(null);
		this.uafAplica = false;
		this.setListaAnticipos(new ArrayList<>());
		this.setListaAnticiposSeleccionados(new ArrayList<>());
		this.setListaNumerosFisicos(new ArrayList<>());
		this.setListaNumerosDocumentos(new ArrayList<>());
		this.setListaMemorandos(new ArrayList<>());
		this.setListaCtasBancarias(new ArrayList<>());
		this.setUsuariosSelectItems(new ArrayList<>());
		this.setCtasBancariasSelectItems(new ArrayList<>());
		this.setListarClientes(new ArrayList<>());
		this.setListarClientesBeneficiarios(new ArrayList<>());
		this.setRutaPdfMail(null);
		this.setNoFisicoGeneracion(null);
		this.setDocumentosTomados(null);
		this.revision = new CxcRevisionDiarios();
		this.mapaDiariosCliente = new HashMap<>();
		this.archivosAdjuntos = new HashMap<>();
		this.listaAdjuntos = new ArrayList<>();
		this.archivosAdjuntos = new HashMap<>();
		this.mapaArchivosCliente = new HashMap<>();
		this.desactivarBtnProcesar = true;
		this.usuariosSelectItems = new ArrayList<SelectItem>();
		this.noClienteArchivo = "";
		this.codigoU = "";
		cargarCientes();
		cargarCientesBeneficiarios();
		cargarCtasBancarias();
		cargarUsuarios();

	}

	public void habilitarBotonAgregarBeneficiarios() {
		if (this.emiteCheque != null && !this.emiteCheque.isEmpty()
				&& this.emiteCheque.equals(CommonConstants.NO_STRING_VALUE)) {
			this.desactivarBtnProcesar = true;
			info("Agregar Beneficiarios");
		} else if (this.emiteCheque != null && !this.emiteCheque.isEmpty() && this.emiteCheque.equals("CK")) {
			this.desactivarBtnProcesar = true;
			warn("Emision Cheque o Transferencia debe ser directo a cliente");
		} else if (this.emiteCheque != null && !this.emiteCheque.isEmpty() && this.emiteCheque.equals("TR")) {
			if (this.cliente.getTipoCuenta() == null || this.cliente.getBanco() == null
					|| this.cliente.getNoCtaCliente() == null) {
				this.desactivarBtnProcesar = false;
				warn("Emision Cheque o Transferencia debe ser directo a cliente");
				warn("Verificar que el cliente tenga registrada la Cta. Bancaria");
			}
		}
	}

	public void desHabilitarBotonProcesar(DatosMemorandoDto datosCliente) {
		Cliente clienteRecuperado = new Cliente();
		try {
			if (datosCliente.getEmitirCheque() != null && !datosCliente.getEmitirCheque().isEmpty()
					&& datosCliente.getEmitirCheque().equals("TR")) {
				clienteRecuperado = clienteService.buscarPorCedulaNoCia(datosCliente.getCedula(),
						this.getCompania().getNoCia());
				if (clienteRecuperado.getTipoCuenta() == null || clienteRecuperado.getBanco() == null
						|| clienteRecuperado.getNoCtaCliente() == null) {
					this.desactivarBtnProcesar = false;
					warn("El cliente no tiene registrado los datos Bancarios Actualizar los datos en mantenimiento de Clientes");
				} else {
					info("El cliente cuenta con todos los datos registrados");
				}
			}
			// else {
			// this.desactivarBtnProcesar = true;
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @author fb_fabara
	 * @Descripcion: Metodo que permite recuperar los documentos por Codigo U
	 * @param CodigoU
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void recuperarArchivosXcodigoU(String CodigoU, String noCliente) {
		try {
			HashMap parametros = new HashMap();
			StringBuffer sql = new StringBuffer();
			sql.setLength(0);
			parametros.clear();
			sql.append(" SELECT a.NO_CIA,a.NO_CLIENTE,d.PATH FROM USD_VEHICLE_AVALUO a, USD_VEHICLE_DOCUMENTO_DIG d  ");
			sql.append(" WHERE a.NO_CIA = :noCia ");
			sql.append(
					" AND a.CODIGO_INGRESO IN (SELECT REGEXP_SUBSTR(:CodigoU ,'[^\\\",\\\"]+', 1, LEVEL) CAMPO FROM DUAL CONNECT BY REGEXP_SUBSTR(:CodigoU , '[^\\\",\\\",]+', 1, LEVEL) IS NOT NULL)");
			sql.append(" AND a.CODE = d.VEHICLE_AVALUO_CODE");
			parametros.put("noCia", getCompania().getNoCia());
			parametros.put("CodigoU", CodigoU);
			List<Object[]> listarArchivosU = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null, 0, 0);
			if (!listarArchivosU.isEmpty()) {
				for (Object[] archivoRecuperado : listarArchivosU) {
					try {
						String clienteArch = noCliente;
						String urlArchivo = (String) archivoRecuperado[2];
						if (this.mapaArchivosCliente == null) {
							this.mapaArchivosCliente = new HashMap<>();
						}
						if (this.mapaArchivosCliente.containsKey(clienteArch)) {
							List<String> a = this.mapaArchivosCliente.get(clienteArch);
							a.add(urlArchivo);
							this.mapaArchivosCliente.replace(clienteArch, a);
						} else {
							List<String> a = new ArrayList<>();
							a.add(urlArchivo);
							this.mapaArchivosCliente.put(clienteArch, a);
						}
						String[] nombres = new String[0];
						if (urlArchivo.contains("/")) {
							nombres = urlArchivo.split("/");
						} else if (urlArchivo.contains("\\")) {
							nombres = urlArchivo.split("\\");
						}
						String nombreArchivo = nombres[nombres.length - 1];
						if (this.archivosAdjuntos == null) {
							this.archivosAdjuntos = new HashMap<>();
						}
						if (this.listaAdjuntos == null) {
							this.listaAdjuntos = new ArrayList<>();
						}
						this.archivosAdjuntos.put(nombreArchivo, urlArchivo);
						this.listaAdjuntos.add(nombreArchivo);

						// byte[] buffer = IOUtils.toByteArray(uf.getInputstream());
						// InputStream is1 = new ByteArrayInputStream(buffer);
						// procesarArchivo(is1, uf);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String reportReimpresionIngresoU() throws FindException {
		String format = "pdf";
		String reportPath = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("reportPath");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();

			String ctxPath = getServletContext().getRealPath("/");

			setParametersReimpresionU(parameters, false);
			connection = utilServiceDelegate.getDataSource().getConnection();

			String[] codigoU = this.codigoU.split(",");
			StringBuffer header = new StringBuffer();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);

			if ("pdf".equals(format)) {

				header.append("inline; filename=\"");
				header.append("ReimpresionU " + String.valueOf(codigoU[0]) + ".pdf");
				header.append("\"");
				response.setHeader("Content-Disposition", header.toString());
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

				FacesContext.getCurrentInstance().getApplication().getStateManager()
						.saveView(FacesContext.getCurrentInstance());
			}

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
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	private void setParametersReimpresionU(Map<String, Object> parameters, boolean esHojaPrecios) throws FindException {
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		obtenerMemorandos();
		String[] codeU = this.codigoU.split(",");
		String reportPath = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("reportPath");

		try {
			InventarioMaster inventarioMaster = inventarioMasterService.buscarInventarioPorU(this.noCia,
					String.valueOf(codeU[0]));

			sql.setLength(0);
			parametros.clear();
			sql.append(" SELECT * FROM USD_VEHICLE_ALISTAMIENTO ");
			sql.append(" WHERE NO_CIA =:noCia ");
			sql.append(" AND SVINNOTAUS IN (:codigoU) ");
			sql.append(
					" AND CODE = (SELECT MAX(CODE) FROM USD_VEHICLE_ALISTAMIENTO WHERE NO_CIA =:noCia AND SVINNOTAUS IN (:codigoU)) ");
			parametros.put("noCia", this.noCia);
			parametros.put("codigoU", inventarioMaster.getSvinnotaus().trim().toUpperCase());
			List<Object[]> usdVehicleAlistamiento = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null,
					0, 0);

			if (inventarioMaster != null) {

				parameters.put("pChasis", inventarioMaster.getInventarioMasterPK().getSvincodi());
				parameters.put("pNoCia", inventarioMaster.getInventarioMasterPK().getNoCia());
				parameters.put("pCodigoIngreso", inventarioMaster.getSvinnotaus());
				parameters.put("USUARIO", getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
			}
			if (usdVehicleAlistamiento != null && !usdVehicleAlistamiento.isEmpty()) {
				parameters.put("pAlistamientoCode", String.valueOf(usdVehicleAlistamiento.get(0)[0]));
				parameters.put("pOrdenTrabajo",
						obtenerNumeroOrden(Long.parseLong(String.valueOf(usdVehicleAlistamiento.get(0)[0]))));
			}
			parameters.put("pImage1", getServletContext().getRealPath("/common/imgs/imagenes/autoVistaUp.jpg"));
			parameters.put("pImage2", getServletContext().getRealPath("/common/imgs/imagenes/carpeta.png"));
			if (getCompania().getNoCia().equals(CommonConstants.CASABACA)) {
				parameters.put("pImage3", getServletContext().getRealPath("/common/imgs/imagenes/logoCasabaca.jpg"));

				if (usdVehicleAlistamiento != null && !usdVehicleAlistamiento.isEmpty()) {
					parameters.put("P_ESTADO_GENERAL",
							String.valueOf(usdVehicleAlistamiento.get(0)[22]).equals("I") ? "SOLICITUD INGRESADA"
									: String.valueOf(usdVehicleAlistamiento.get(0)[22]).equals("E")
											? "SOLICITUD ENVIADA"
											: "SOLICITUD APROBADA");
				}
				if (usdVehicleAlistamiento != null && !usdVehicleAlistamiento.isEmpty()) {
					parameters.put("P_TIPO_ALISTAMIENTO", String.valueOf(usdVehicleAlistamiento.get(0)[21]).equals("N")
							? "NORMAL"
							: String.valueOf(usdVehicleAlistamiento.get(0)[21]).equals("R") ? "REPROCESO" : "GARANTIA");
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (getCompania().getNoCia().equals("L2")) {
			parameters.put("pImage3", getServletContext().getRealPath("/common/imgs/imagenes/logoCarlosLarrea.png"));
		}
		if (getCompania().getNoCia().equals("M2")) {
			parameters.put("pImage4", getServletContext().getRealPath("/common/imgs/imagenes/ambandine.jpg"));
		}
		if (getCompania().getNoCia().equals(CommonConstants.NEXUMCORP)) {
			parameters.put("pImage4", getServletContext().getRealPath("/common/imgs/imagenes/nexumcorp.png"));
		}
		if (getCompania().getNoCia().equals(CommonConstants.TOYOCOSTAS)) {
			parameters.put("pImage4", getServletContext().getRealPath("/common/imgs/imagenes/toyoCosta.png"));
		}
		String subreport = getPathReal();
		if (esHojaPrecios) {
			subreport = subreport.replace("reportes", "");
		}
		parameters.put("SUBREPORT_DIR", subreport);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String obtenerNumeroOrden(Long orden) {

		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		String ordenRepId = null;
		parametros.clear();
		sql.append("SELECT ORDENREP_ID FROM se_ordenrep WHERE CODE_VEH_ALISMIENTO= :code");
		parametros.put("code", orden);
		List resultadoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null, 0, 0);
		if (resultadoList.size() > 0) {
			ordenRepId = (String) (resultadoList.get(0));
		}
		return ordenRepId;
	}

	public void cargarCodigosU(DatosMemorandoDto datoMemo) {
		if (Objects.nonNull(cliente.getClientePK().getNoCliente())) {
			codigos = nativeDmlCajasService.consultarCodigosUCliente(getCompania().getNoCia(), datoMemo.getNoCliente());
			beneficiarioSelected = datoMemo;
			accionesDialog("dlgCodigosU", Boolean.TRUE);
		} else {
			warn("Verificar si el cliente seleccionado existe");
			return;
		}
	}

	public void seleccionarCodigoU() {
		if (!nativeDmlCajasService.existeCodigoU(getCompania().getNoCia(), String.valueOf(codigoUSel[0]))) {
			error("No existe una U creada para el usuario, por favor verificar...");
			codigoUSel = null;
		} else {
			int index = getListaMemorandos().indexOf(beneficiarioSelected);
			beneficiarioSelected.setCodigoU(codigoUSel[0].toString());
			RequestContext.getCurrentInstance().update("form:idBeneficiariosTable:" + index + ":txtCodigoU");
			RequestContext.getCurrentInstance().update("form:idBeneficiariosTable:" + index + ":lblCodigoU");
		}
		accionesDialog("dlgCodigosU", Boolean.FALSE);
	}

	public void pantallaDevolucion() {
		String url = "/cxc-web-prime/jsf/procesos/cxcAprobacionDevSaldos.jsf?noCia=" + this.noCia;
		try {
			super.redirect(url);
		} catch (IOException e) {
			logger.error(e);
		}

	}

	@SuppressWarnings("unused")
	private void enviarCorreo(Long  pnoCliente, String pnoSolicitud, String pnoCia) {
		try {
			CxcCabDevAnticipo cabAnticipo= new CxcCabDevAnticipo(new CxcCabDevAnticipoPK());
			cabAnticipo=devAnticipoService.datosCabecera(pnoCia, pnoCliente, pnoSolicitud);			
			StringBuffer mensajeEnvio = new StringBuffer("SE HA PROCESADO EXITOSAMENTE LA SOLICITUD DE DEVOLUCION DE ANTICIPOS: ");
			String email = usuarioSisService.mailUsuario(cabAnticipo.getUsuarioProceso());
			String emailAsesor=usuarioSisService.mailUsuario(cabAnticipo.getAsesor());
			mensajeEnvio.append(generarDetalleEnvio(cabAnticipo));
			if (null != email) {
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), email, email,
						"SOLICITUD DE DEVOLUCION DE ANTICIPO PROCESADA", mensajeEnvio, Boolean.FALSE);
			}
			if (null != emailAsesor) {
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), emailAsesor, emailAsesor,
						"SOLICITUD DE DEVOLUCION DE ANTICIPO PROCESADA", mensajeEnvio, Boolean.FALSE);
			}
			mailService.sendMailToGroupAttachments(getSisMailServidores(getCompania().getNoCia()), GRUPO_CORREO,
					"SOLICITUD DE DEVOLUCION DE ANTICIPO PROCESADA", "SOLICITUD DE DEVOLUCION DE ANTICIPO PROCESADA", mensajeEnvio,
					Boolean.FALSE, null);

		} catch (GeneralException | FindException e) {
			logger.error(e);
			addErrorMessage("Error", " Ocurrio un error al enviar el correo al grupo" + e.getMessage());
			return;
		}

	}

	private StringBuffer generarDetalleEnvio(CxcCabDevAnticipo cabAnticipo) {
		StringBuffer cadena = new StringBuffer();
		cadena.append(
				" <table border='1' width='100%'> <td  style='color: blue;' align=center><b> SOLICITUD DEVOLUCION DE  ANTICIPOS GENERADO CON EXITO </b> </td>  <table  width='100%'>  <td  style='color: black;' align=center> </td> </table> <table border='1' width='100%'> <tr> <td align=center><b> CI. CLIENTE</b></td> <td align=center><b> CLIENTE</b></td> <td align=center><b>  FECHA DE SOLICITUD  </b> </td>  <td align=center><b> # SOLICITUD</b> </td> <td align=center><b> ASESOR COMERCIAL </b> <td align=center><b> MOTIVO DEVOLUCION </b> </td> <td align=center> <b> VALOR </b> </td> <tr> <td align=center>"
						+ pCedula+ "</td>  <td align=center> " + cliente.getNombres()
						+ "<td align=center> " + cabAnticipo.getFechaProceso()  + "</td> <td td align=center> "
						+ cabAnticipo.getId().getNoSolicitud() + "  </td> <td align=center> " + cabAnticipo.getAsesor()
						+ "  </td> <td align=center> " + cabAnticipo.getMotivoDevolucion() + " </td> <td align=right> "
						+ cabAnticipo.getValorTotal() + " </td> </tr> </table> ");
		return cadena;
	}
	
	
	private Boolean activarSegunSecuencia(String noCia, String centro, String tipoSec) {

		Arcjsec datos = arcjsecService.obtenerSiguienteSecuencia(noCia, centro, tipoSec);

		if (datos == null || datos.getSecuencia() == null) {
			return false; // botón deshabilitado
		}

		// Si devuelve datos → habilitar botón
		return true;

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

	public String getUsuarioConectado() {
		return usuarioConectado;
	}

	public void setUsuarioConectado(String usuarioConectado) {
		this.usuarioConectado = usuarioConectado;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getCedulaBeneficiario() {
		return cedulaBeneficiario;
	}

	public void setCedulaBeneficiario(String cedulaBeneficiario) {
		this.cedulaBeneficiario = cedulaBeneficiario;
	}

	public String getNombresBeneficiario() {
		return nombresBeneficiario;
	}

	public void setNombresBeneficiario(String nombresBeneficiario) {
		this.nombresBeneficiario = nombresBeneficiario;
	}

	public BigDecimal getDiferencia() {
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
	}

	public String getObservacionCabecera() {
		return ObservacionCabecera;
	}

	public void setObservacionCabecera(String observacionCabecera) {
		ObservacionCabecera = observacionCabecera;
	}

	public String getEmiteCheque() {
		return emiteCheque;
	}

	public void setEmiteCheque(String emiteCheque) {
		this.emiteCheque = emiteCheque;
	}

	public String getCtasBancarias() {
		return ctasBancarias;
	}

	public void setCtasBancarias(String ctasBancarias) {
		this.ctasBancarias = ctasBancarias;
	}

	public String getUsuarioSolicita() {
		return usuarioSolicita;
	}

	public void setUsuarioSolicita(String usuarioSolicita) {
		this.usuarioSolicita = usuarioSolicita;
	}

	public String getComentarioEmisionChequeCabecera() {
		return comentarioEmisionChequeCabecera;
	}

	public void setComentarioEmisionChequeCabecera(String comentarioEmisionChequeCabecera) {
		this.comentarioEmisionChequeCabecera = comentarioEmisionChequeCabecera;
	}

	public String getComentarioEmisionCheque() {
		return comentarioEmisionCheque;
	}

	public void setComentarioEmisionCheque(String comentarioEmisionCheque) {
		this.comentarioEmisionCheque = comentarioEmisionCheque;
	}

	public String getComentarioCancelacionAnticipos() {
		return comentarioCancelacionAnticipos;
	}

	public void setComentarioCancelacionAnticipos(String comentarioCancelacionAnticipos) {
		this.comentarioCancelacionAnticipos = comentarioCancelacionAnticipos;
	}

	public String getDocumentoCabeceraCancelacion() {
		return documentoCabeceraCancelacion;
	}

	public void setDocumentoCabeceraCancelacion(String documentoCabeceraCancelacion) {
		this.documentoCabeceraCancelacion = documentoCabeceraCancelacion;
	}

	public String getDocumentosYvaloresAcancelar() {
		return documentosYvaloresAcancelar;
	}

	public void setDocumentosYvaloresAcancelar(String documentosYvaloresAcancelar) {
		this.documentosYvaloresAcancelar = documentosYvaloresAcancelar;
	}

	public String getDocumentosTomados() {
		return documentosTomados;
	}

	public void setDocumentosTomados(String documentosTomados) {
		this.documentosTomados = documentosTomados;
	}

	public BigDecimal getValoresCancelar() {
		return valoresCancelar;
	}

	public void setValoresCancelar(BigDecimal valoresCancelar) {
		this.valoresCancelar = valoresCancelar;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getLineaNegocio() {
		return lineaNegocio;
	}

	public void setLineaNegocio(String lineaNegocio) {
		this.lineaNegocio = lineaNegocio;
	}

	public String getDescripcionLineaNegocio() {
		return descripcionLineaNegocio;
	}

	public void setDescripcionLineaNegocio(String descripcionLineaNegocio) {
		this.descripcionLineaNegocio = descripcionLineaNegocio;
	}

	public String getRutaPdfMail() {
		return rutaPdfMail;
	}

	public void setRutaPdfMail(String rutaPdfMail) {
		this.rutaPdfMail = rutaPdfMail;
	}

	public String getNoFisicoGeneracion() {
		return noFisicoGeneracion;
	}

	public void setNoFisicoGeneracion(String noFisicoGeneracion) {
		this.noFisicoGeneracion = noFisicoGeneracion;
	}

	public String getNumeroDocumentoSeleccionado() {
		return numeroDocumentoSeleccionado;
	}

	public void setNumeroDocumentoSeleccionado(String numeroDocumentoSeleccionado) {
		this.numeroDocumentoSeleccionado = numeroDocumentoSeleccionado;
	}

	public String getCodigoU() {
		return codigoU;
	}

	public void setCodigoU(String codigoU) {
		this.codigoU = codigoU;
	}

	public String getNoClienteArchivo() {
		return noClienteArchivo;
	}

	public void setNoClienteArchivo(String noClienteArchivo) {
		this.noClienteArchivo = noClienteArchivo;
	}

	public String getPlacaRecuperada() {
		return placaRecuperada;
	}

	public void setPlacaRecuperada(String placaRecuperada) {
		this.placaRecuperada = placaRecuperada;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public Date getFechaAnticipo() {
		return fechaAnticipo;
	}

	public void setFechaAnticipo(Date fechaAnticipo) {
		this.fechaAnticipo = fechaAnticipo;
	}

	public BigDecimal getValor() {
		return valor;
	}

	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public boolean isUafAplica() {
		return uafAplica;
	}

	public void setUafAplica(boolean uafAplica) {
		this.uafAplica = uafAplica;
	}

	public boolean isAplicaDiferencia() {
		return aplicaDiferencia;
	}

	public void setAplicaDiferencia(boolean aplicaDiferencia) {
		this.aplicaDiferencia = aplicaDiferencia;
	}

	public boolean isVerificarMontosTomar() {
		return verificarMontosTomar;
	}

	public void setVerificarMontosTomar(boolean verificarMontosTomar) {
		this.verificarMontosTomar = verificarMontosTomar;
	}

	public boolean isDesactivarBtnProcesar() {
		return desactivarBtnProcesar;
	}

	public void setDesactivarBtnProcesar(boolean desactivarBtnProcesar) {
		this.desactivarBtnProcesar = desactivarBtnProcesar;
	}

	public boolean isNoEsAdministrador() {
		return noEsAdministrador;
	}

	public void setNoEsAdministrador(boolean noEsAdministrador) {
		this.noEsAdministrador = noEsAdministrador;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Cliente getClienteBeneficiarios() {
		return clienteBeneficiarios;
	}

	public void setClienteBeneficiarios(Cliente clienteBeneficiarios) {
		this.clienteBeneficiarios = clienteBeneficiarios;
	}

	public List<CxCdatosAnticiposDto> getListaAnticipos() {
		return listaAnticipos;
	}

	public void setListaAnticipos(List<CxCdatosAnticiposDto> listaAnticipos) {
		this.listaAnticipos = listaAnticipos;
	}

	public List<CxCdatosAnticiposDto> getListaAnticiposSeleccionados() {
		return listaAnticiposSeleccionados;
	}

	public void setListaAnticiposSeleccionados(List<CxCdatosAnticiposDto> listaAnticiposSeleccionados) {
		this.listaAnticiposSeleccionados = listaAnticiposSeleccionados;
	}

	public List<DatosMemorandoDto> getListaMemorandos() {
		return listaMemorandos;
	}

	public void setListaMemorandos(List<DatosMemorandoDto> listaMemorandos) {
		this.listaMemorandos = listaMemorandos;
	}

	public List<CxCdatosCtasBancariasDto> getListaCtasBancarias() {
		return listaCtasBancarias;
	}

	public void setListaCtasBancarias(List<CxCdatosCtasBancariasDto> listaCtasBancarias) {
		this.listaCtasBancarias = listaCtasBancarias;
	}

	public List<SelectItem> getUsuariosSelectItems() {
		return usuariosSelectItems;
	}

	public void setUsuariosSelectItems(List<SelectItem> usuariosSelectItems) {
		this.usuariosSelectItems = usuariosSelectItems;
	}

	public List<SelectItem> getCtasBancariasSelectItems() {
		return ctasBancariasSelectItems;
	}

	public void setCtasBancariasSelectItems(List<SelectItem> ctasBancariasSelectItems) {
		this.ctasBancariasSelectItems = ctasBancariasSelectItems;
	}

	public List<Cliente> getListarClientes() {
		return listarClientes;
	}

	public void setListarClientes(List<Cliente> listarClientes) {
		this.listarClientes = listarClientes;
	}

	public List<Cliente> getListarClientesBeneficiarios() {
		return listarClientesBeneficiarios;
	}

	public void setListarClientesBeneficiarios(List<Cliente> listarClientesBeneficiarios) {
		this.listarClientesBeneficiarios = listarClientesBeneficiarios;
	}

	public List<String> getListaNumerosFisicos() {
		return listaNumerosFisicos;
	}

	public void setListaNumerosFisicos(List<String> listaNumerosFisicos) {
		this.listaNumerosFisicos = listaNumerosFisicos;
	}

	public List<String> getListaNumerosDocumentos() {
		return listaNumerosDocumentos;
	}

	public void setListaNumerosDocumentos(List<String> listaNumerosDocumentos) {
		this.listaNumerosDocumentos = listaNumerosDocumentos;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	public Map<String, String> getArchivosAdjuntos() {
		return archivosAdjuntos;
	}

	public void setArchivosAdjuntos(Map<String, String> archivosAdjuntos) {
		this.archivosAdjuntos = archivosAdjuntos;
	}

	public CxcRevisionDiarios getRevision() {
		return revision;
	}

	public void setRevision(CxcRevisionDiarios revision) {
		this.revision = revision;
	}

	public Map<String, List<String>> getMapaDiariosCliente() {
		return mapaDiariosCliente;
	}

	public void setMapaDiariosCliente(Map<String, List<String>> mapaDiariosCliente) {
		this.mapaDiariosCliente = mapaDiariosCliente;
	}

	public Map<String, List<String>> getMapaArchivosCliente() {
		return mapaArchivosCliente;
	}

	public void setMapaArchivosCliente(Map<String, List<String>> mapaArchivosCliente) {
		this.mapaArchivosCliente = mapaArchivosCliente;
	}

	public InventarioMaster getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(InventarioMaster vehiculo) {
		this.vehiculo = vehiculo;
	}

	public NativeDmlDatabaseServiceLocal getNativeDmlDatabaseService() {
		return nativeDmlDatabaseService;
	}

	public void setNativeDmlDatabaseService(NativeDmlDatabaseServiceLocal nativeDmlDatabaseService) {
		this.nativeDmlDatabaseService = nativeDmlDatabaseService;
	}

	public MailServiceLocal getMailService() {
		return mailService;
	}

	public void setMailService(MailServiceLocal mailService) {
		this.mailService = mailService;
	}

	public ClienteServiceLocal getClienteService() {
		return clienteService;
	}

	public void setClienteService(ClienteServiceLocal clienteService) {
		this.clienteService = clienteService;
	}

	public DatosMemorandoServiceLocal getMemorandoService() {
		return memorandoService;
	}

	public void setMemorandoService(DatosMemorandoServiceLocal memorandoService) {
		this.memorandoService = memorandoService;
	}

	public static String getEstadoMemo() {
		return ESTADO_MEMO;
	}

	public void setSumaTotalesXbeneficiarios(BigDecimal sumaTotalesXbeneficiarios) {
		this.sumaTotalesXbeneficiarios = sumaTotalesXbeneficiarios;
	}

	public void setSumaTotalAnticiposSeleccionados(BigDecimal sumaTotalAnticiposSeleccionados) {
		this.sumaTotalAnticiposSeleccionados = sumaTotalAnticiposSeleccionados;
	}

	public List<Object[]> getCodigos() {
		return codigos;
	}

	public void setCodigos(List<Object[]> codigos) {
		this.codigos = codigos;
	}

	public Object[] getCodigoUSel() {
		return codigoUSel;
	}

	public void setCodigoUSel(Object[] codigoUSel) {
		this.codigoUSel = codigoUSel;
	}

	public DatosMemorandoDto getBeneficiarioSelected() {
		return beneficiarioSelected;
	}

	public void setBeneficiarioSelected(DatosMemorandoDto beneficiarioSelected) {
		this.beneficiarioSelected = beneficiarioSelected;
	}

	/**
	 * @return the pOrigen
	 */
	public String getpOrigen() {
		return pOrigen;
	}

	/**
	 * @param pOrigen the pOrigen to set
	 */
	public void setpOrigen(String pOrigen) {
		this.pOrigen = pOrigen;
	}

}