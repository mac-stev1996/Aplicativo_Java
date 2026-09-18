package com.casabaca.prime.cxc.procesos.controller;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.FacVentasCab;
import com.casabaca.common.ejb.model.NomEmpleados;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.FacVentasServiceLocal;
import com.casabaca.common.ejb.service.NominaEmpleadosServiceLocal;
import com.casabaca.common.ejb.util.IdentificacionEnum;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.HistoricoDocEntradaDto;
import com.casabaca.cxc.ejb.dto.HistoricoDocumentoDto;
import com.casabaca.cxc.ejb.modelo.CxcCreditoDigital;
import com.casabaca.cxc.ejb.modelo.CxcHistorialSolCobranza;
import com.casabaca.cxc.ejb.modelo.CxcParametrosDet;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCobranza;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCobranzaArchivo;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCobranzaPK;
import com.casabaca.cxc.ejb.servicio.CreditoDigitalServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcHistorialSolCobranzaServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcSolicitudCobranzaArchivoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcSolicitudCobranzaServiceLocal;
import com.casabaca.cxc.ejb.servicio.HistoricoDocumentosServiceLocal;
import com.casabaca.cxc.ejb.servicio.ParametrosDetServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;
import com.casabaca.prime.cxc.common.lazy.LazyClientesDataModel;
import com.casabaca.prime.cxc.common.lazy.LazyCxcSolicitudCobranzaDataModel;
import com.casabaca.s3s.ejb.model.SisMailUsuario;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.ModuloServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;

/**
 * 
 * @author edwin.amaguaya
 * @version <$ version 1.0 $>
 * 
 */

@ViewScoped
@ManagedBean
public class CxcSolicitudCobranzaF2Controller extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CxcSolicitudCobranzaF2Controller.class);

	private static final String CONDITION = "C";
	private static final String FINALIZADO = "DV";
	private static final String RUC = "R";
	private static final String SOLICITUD_CREDITO_TIPOS_ARCHIVOS = "SCTA";
	private static final String ETAPAS_DEMANDA_F2 = "EDF2";
	private static final String RUTA_ARCHIVOS = "/opt/archivosSolicitudCobranzas/";

	@EJB(lookup = "java:global/cuentas-cobrar-ejb/CxcSolicitudCobranzaServiceBean")
	private CxcSolicitudCobranzaServiceLocal solicitudCobranzaServiceLocal;

	@EJB(lookup = "java:global/cuentas-cobrar-ejb/CxcSolicitudCobranzaArchivoServiceBean")
	private CxcSolicitudCobranzaArchivoServiceLocal solicitudCobranzaArchivoServiceLocal;

	@EJB(lookup = NombreJNDI.NOMINA_EMPLEADOS_SERVICE)
	private NominaEmpleadosServiceLocal nominaEmpleadosServiceLocal;

	// @EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	// private ParamDetServiceLocal paramDetServiceLocal;

	@EJB(lookup = NombreJNDI.PARAMETROS_DET_SERVICE_LOCAL)
	private ParametrosDetServiceLocal parametrosDetServiceLocal;

	@EJB(lookup = NombreJNDI.FAC_VENTAS_CAB_SERVICE)
	private FacVentasServiceLocal facVentasServiceLocal;

	@EJB(lookup = NombreJNDI.MODULO_SERVICE_BEAN)
	private ModuloServiceLocal moduloServiceLocal;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioLocal;

	@EJB(lookup = NombreJNDI.JNDI_CXC + "HistoricoDocumentosServiceBean")
	private HistoricoDocumentosServiceLocal historicoDocumentosService;

	@EJB(lookup = NombreJNDI.CREDITO_DIGITAL_SERVICE)
	private CreditoDigitalServiceLocal creditoDigitalServiceLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_HISTORIAL_SOL_COBRANZA_SERVICE)
	private CxcHistorialSolCobranzaServiceLocal historialService;

	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	private CxcSolicitudCobranza solicitudCobranza;
	private LazyCxcSolicitudCobranzaDataModel lazySolicitudCobranza;
	private List<FacVentasCab> lfacturas;
	// private List<ParamDet> lestados;
	private List<CxcParametrosDet> lestados;
	private List<CxcSolicitudCobranzaArchivo> lsolicitudCobranzaArchivo;
	private CxcSolicitudCobranzaArchivo cxcSolicitudCobranzaArchivo;
	private List<HistoricoDocumentoDto> documentosHistoricos;
	private List<CxcCreditoDigital> lcentroDigital;
	private List<String> usuariosCrean;
	private List<SisMailUsuario> usuariosAprueban;
	private HistoricoDocumentoDto facturaSelect;
	private String facturaABuscar;
	private String cedulaABuscar;
	private String nombreABuscar;
	private String urlArchivo;
	private String noCia;
	private boolean edicion;
	// Mejoras Req - nuevos campos
	private List<CxcHistorialSolCobranza> listaHistoriales;
	// Filtros Busqueda
	private Date fechaDesde;
	private Date fechaHasta;
	private String estadoABuscar;
	// CLientes
	private LazyDataModel<Cliente> lazyModel;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;

	@PostConstruct
	public void init() {

		this.noCia = getCompania().getNoCia();

		this.construct();
		try {
			this.lazySolicitudCobranza = new LazyCxcSolicitudCobranzaDataModel(this.noCia, null);
		} catch (ServiceLocatorException e) {
			log.error(e);
		}
	}

	public void construct() {

//		this.lestados = this.paramDetServiceLocal.buscarDetalleCodigo(this.noCia, ETAPAS_DEMANDA_F2);

		this.lestados = this.parametrosDetServiceLocal.findNoCiaAndCodigo(getCompania().getNoCia(), ETAPAS_DEMANDA_F2);
		CxcParametrosDet estadoInicial = this.estadoInicial();
		this.usuariosCrean = this.usuariosEtapa(estadoInicial.getTexto2());
		this.fillUsuariosAprueban();
		// Mejoras Req - inicializar controller
		if (this.dialogClientesController != null && this.dialogClientesController.getCliente() == null) {
	        this.dialogClientesController.setCliente(new Cliente());
	    }
	}

	private void fillUsuariosAprueban() {
		this.usuariosAprueban = this.mailUsuarioLocal.findByUsuariosLikeGrupoMail(getUsuario().getUsuario(),
				ETAPAS_DEMANDA_F2);
	}

	public boolean habilitaSiguienteEtapa(CxcSolicitudCobranza solicitudCobranza) {

		String etapa = ETAPAS_DEMANDA_F2.concat(solicitudCobranza.getEstado());
		List<SisMailUsuario> usuariosEtapa = this.usuariosAprueban.stream()
				.filter(item -> item.getSisMailUsuarioPK().getGrupoMail().equals(etapa)).collect(Collectors.toList())
				.stream().filter(item -> getUsuario().getUsuario().equals(item.getUsuarioSis().getUsuario()))
				.collect(Collectors.toList());

		return usuariosEtapa.size() > 0;
	}

	private CxcParametrosDet estadoInicial() {
		CxcParametrosDet estadoInicial = this.lestados.stream()
				.filter(item -> item.getCxcParametrosDetPk().getCodigoDetalle().equals("1")).findFirst().get();
		return estadoInicial;
	}

	public String getLabelSiguienteEtapa(String etapaAcutal) {
		return etapaAcutal != null ? this.obtenerEtapaSiguiente(etapaAcutal).getDescripcion() : "";
	}

	private List<String> usuariosEtapa(String etapa) {
		return this.mailUsuarioLocal.findByCodigoGrupo(ETAPAS_DEMANDA_F2 + etapa).stream()
				.map(SisMailUsuario::getUsuarioSis).collect(Collectors.toList()).stream().map(UsuarioSis::getUsuario)
				.collect(Collectors.toList());
	}

	public void nuevaSolicitud() {

		try {
			if (this.dialogClientesController != null && this.dialogClientesController.getCliente() == null) {
		        this.dialogClientesController.setCliente(new Cliente());
		    }
			CxcParametrosDet estadoInicial = this.estadoInicial();

			this.edicion = false;
			CxcSolicitudCobranzaPK pk = new CxcSolicitudCobranzaPK();
			pk.setNoCia(this.noCia);

			this.solicitudCobranza = new CxcSolicitudCobranza();
			this.solicitudCobranza.setSolicitudCobranzaPK(pk);

			this.solicitudCobranza.setEmpleado(obtenerSolicitante());
			this.solicitudCobranza.setNoEmple(this.solicitudCobranza.getEmpleado().getNomEmpleadosPK().getNoEmple());
			this.solicitudCobranza
					.setArea(this.moduloServiceLocal.getModuloByPK(super.getActiveModuleId()).getNombre());
			this.solicitudCobranza.setEstado(estadoInicial.getTexto2());

			this.solicitudCobranza.setUsuarioCrea(getUsuario().getUsuario());
			this.solicitudCobranza.setFechaCrea(new Date());

			cargarDocumentos();

		} catch (FindException e) {
			log.error(e);
		}

	}

	/**
	 * Permite cargar el dialogo de clientes
	 */

	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);
	}

	private void cargarDocumentos() {

		this.lsolicitudCobranzaArchivo = new ArrayList<>();
		List<CxcParametrosDet> lta = this.parametrosDetServiceLocal.findNoCiaAndCodigo(this.noCia,
				SOLICITUD_CREDITO_TIPOS_ARCHIVOS);
		lta.forEach(item -> {

			CxcSolicitudCobranzaArchivo sca = new CxcSolicitudCobranzaArchivo();
			sca.setNombreDocumento(item.getDescripcion());
			sca.setTipoDocumento(item.getTexto1());
			this.lsolicitudCobranzaArchivo.add(sca);

		});

		if (this.solicitudCobranza.getSolicitudCobranzaPK().getNoSolicitud() != null) {
			List<CxcSolicitudCobranzaArchivo> larchivos = this.solicitudCobranzaArchivoServiceLocal
					.lsolicitudCobranzaArchivos(this.solicitudCobranza);

			for (CxcSolicitudCobranzaArchivo archivo : larchivos) {

				for (CxcSolicitudCobranzaArchivo sca : this.lsolicitudCobranzaArchivo) {
					if (archivo.getTipoDocumento().equals(sca.getTipoDocumento())) {
						sca.setNoSolicitudArchivo(archivo.getNoSolicitudArchivo());
						sca.setNoSolicitud(archivo.getNoSolicitud());
						sca.setNoCia(archivo.getNoCia());
						sca.setTipoDocumento(archivo.getTipoDocumento());
						sca.setDescripcion(archivo.getDescripcion());
						sca.setNombreArchivo(archivo.getNombreArchivo());
						sca.setTipoArchivo(archivo.getTipoArchivo());
						sca.setRuta(archivo.getRuta());
						sca.setUsuario(archivo.getUsuario());
						sca.setFecha(archivo.getFecha());
						sca.setOriginal(archivo.getOriginal());
						sca.setCopia(archivo.getCopia());
						sca.setExist(true);
						break;
					}
				}

			}

			this.cargarDocumentosCredito(this.noCia, this.solicitudCobranza.getNoFactu(),
					this.solicitudCobranza.getSerieFisico(), this.solicitudCobranza.getNoCliente().toString());
		}

	}

	public void mostrarDialogSubirArchivo(CxcSolicitudCobranzaArchivo archivo) {
		this.cxcSolicitudCobranzaArchivo = archivo;
	}

	public void subirArchivo(FileUploadEvent event) {

		SimpleDateFormat sdf = new SimpleDateFormat("ddMMYY_hhmmss");
		String ruta = RUTA_ARCHIVOS + event.getFile().getFileName();

		StringBuffer filePN = new StringBuffer();
		filePN.append(FilenameUtils.getFullPath(ruta));
		filePN.append(sdf.format(new Date()));
		filePN.append(FilenameUtils.getBaseName(ruta));
		filePN.append(FilenameUtils.EXTENSION_SEPARATOR_STR);
		filePN.append(FilenameUtils.getExtension(ruta));

		this.cxcSolicitudCobranzaArchivo.setNombreArchivo(FilenameUtils.getName(filePN.toString()));
		this.cxcSolicitudCobranzaArchivo.setTipoArchivo(event.getFile().getContentType());
		this.cxcSolicitudCobranzaArchivo.setRuta(filePN.toString());
		this.cxcSolicitudCobranzaArchivo.setArchivo(event.getFile().getContents());

		super.addInfoMessage("Carga", "exitosa");
	}

	private NomEmpleados obtenerSolicitante() {
		NomEmpleados empleado = null;
		try {
			empleado = this.nominaEmpleadosServiceLocal.getEmpleadoByPK(this.noCia, getUsuario().getNoEmple());
		} catch (Exception e) {
			log.error("No se pudo encontrar un empleado ", e);
		}
		return empleado;
	}

	public void editarSolicitud(CxcSolicitudCobranza sc) {
		this.edicion = true;
		this.solicitudCobranza = sc;
		this.solicitudCobranza.setEmpleado(obtenerSolicitante());

		cargarDocumentos();
	}

	public void guardarNuevaSolicitud() {

		if (this.solicitudCobranza.getNoCliente() == null) {
			addWarnMessage("Atención", "Debe seleccionar un cliente");
			return;
		}

		if (this.solicitudCobranza.getNoFactu() == null) {
			addWarnMessage("Atención", "Debe seleccionar una factura");
			return;
		}

		List<CxcSolicitudCobranzaArchivo> larchivos = new ArrayList<>();
		for (CxcSolicitudCobranzaArchivo sa : this.lsolicitudCobranzaArchivo) {

			if (sa.getOriginal().booleanValue() || sa.getCopia().booleanValue()) {

				if (sa.getNombreArchivo() == null || sa.getNombreArchivo().length() <= 0) {
					addWarnMessage(sa.getNombreDocumento() + " Debe ingresar un archivo", "");
					return;
				}

				sa.setUsuario(getUsuario().getUsuario());
				sa.setFecha(new Date());
				larchivos.add(sa);

			} else if (sa.getExist()) {
				sa.setUsuario(getUsuario().getUsuario());
				sa.setFecha(new Date());
				larchivos.add(sa);
			}

		}
		// Mejoras Req - Validar creacion de solicitudes con estado INGRESADO
		if (larchivos.isEmpty() && !"IN".equals(solicitudCobranza.getEstado())) {
			addWarnMessage("Debe subir al menos 1 documento", "Para generar el formulario");
			return;
		}

		this.solicitudCobranza.setFecha(new Date());
		this.solicitudCobranza.setFechaCrea(new Date());
		this.solicitudCobranza.setUsuarioCrea(getUsuario().getUsuario());
		if (this.solicitudCobranza.getSolicitudCobranzaPK().getNoSolicitud() != null) {
			this.solicitudCobranza.setFechaActualiza(new Date());
			this.solicitudCobranza.setUsuarioActualiza(getUsuario().getUsuario());
		}

		try {
			File file = new File(RUTA_ARCHIVOS);
			if (!file.exists()) {
				file.mkdir();
			}
		} catch (Exception e) {
			log.error("Error al crear ruta formulario demanda F2 " + e.getMessage());
		}

		try {
			this.solicitudCobranza = this.solicitudCobranzaServiceLocal.save(this.solicitudCobranza, larchivos);
			this.lazySolicitudCobranza = new LazyCxcSolicitudCobranzaDataModel(this.noCia, null);
		} catch (ServiceLocatorException e) {
			addWarnMessage("Error", e.toString());
			log.error("Error", e);
		} catch (Exception e) {
			log.error("Error", e);
		}

		// Mejoras Req - Agregar funcionalidad para guardar historial
		historialService.registrarHistorialSolicitud(this.solicitudCobranza.getSolicitudCobranzaPK().getNoCia(),
				this.solicitudCobranza.getSolicitudCobranzaPK().getNoSolicitud(), this.solicitudCobranza.getEstado(),
				this.solicitudCobranza.getUsuarioCrea(), this.solicitudCobranza.getObservacion());
		super.accionesDialog("dlgNuevaSolicitudComprobante", false);
	}

	public void buscarFacturaCliente() {

		if (this.solicitudCobranza.getNoCliente() != null) {
			super.accionesDialog("dlgDlgBuscarFactura", true);

			HistoricoDocEntradaDto entrada = new HistoricoDocEntradaDto();
			entrada.setNoCia(this.noCia);
			entrada.setNoCliente(this.solicitudCobranza.getNoCliente());
			entrada.setFechaInicio(FechaUtils.convertirStrFecha("01/01/2000", FechaUtils.patronDiaMesAnio));
			entrada.setFechaFin(new Date());
			entrada.setLineaNegocio("%");
			entrada.setTipoDoc("%");
			entrada.setAnulado("%");
			entrada.setIncluyeAlistamiento(true);
			entrada.setDocumento(String.valueOf(2));
			this.documentosHistoricos = this.historicoDocumentosService.consultarDocumentosCliente(entrada);

			this.documentosHistoricos = this.documentosHistoricos.stream().filter(item -> item.getSaldo() > 0)
					.collect(Collectors.toList());

		} else {
			super.addInfoMessage("Debe seleccionar un cliente", "");
		}

	}

	public void selectFactura() {
		this.solicitudCobranza.setNoFactu(this.facturaSelect.getNoFisico());
		this.solicitudCobranza.setSerieFisico(this.facturaSelect.getSerieFisico());

		this.cargarDocumentosCredito(this.noCia, this.solicitudCobranza.getNoFactu(),
				this.solicitudCobranza.getSerieFisico(), this.solicitudCobranza.getNoCliente().toString());

	}

	private void cargarDocumentosCredito(String noCia, String noFisico, String serieFisico, String noCliente) {
		try {
			this.lcentroDigital = this.creditoDigitalServiceLocal.findCreditoDigitalFrom(noCia, noFisico, serieFisico,
					noCliente);
		} catch (Exception e) {
			super.addWarnMessage("Factura sin documentos de credito", "");
		}
	}

	public String obtenerTipoIdentificacion(String tipoIdentificacion) {
		if (tipoIdentificacion != null && tipoIdentificacion.length() > 0) {
			return IdentificacionEnum.valueOf(tipoIdentificacion).getValue().toUpperCase();
		} else {
			return null;
		}
	}

	public String obtenerEstado(String estado) {
		if (estado != null && estado.length() > 0) {
			return this.lestados.stream().filter(item -> item.getTexto2().equals(estado)).findFirst().get()
					.getDescripcion();
		} else {
			return "";
		}

	}

	public void enviar(CxcSolicitudCobranza sc) {

		String value = null;
		if (sc == null) {
			value = getRequestParameter("validacionEtapa");
			sc = this.solicitudCobranza;
			super.accionesDialog("dlgEnviarFormulario", false);
		} else {
			this.solicitudCobranza = null;
		}

		CxcParametrosDet etapaSiguiente = obtenerEtapaSiguiente(sc.getEstado());

		if (value == null && etapaSiguiente.getTexto1().equals(CONDITION)) {
			this.solicitudCobranza = sc;
			super.accionesDialog("dlgEnviarFormulario", true);
			return;
		}

		if (value != null) {

			switch (value) {
			case CommonConstants.YES_STRING_VALUE:
				sc.setEstado(etapaSiguiente.getTexto3());
				break;
			case CommonConstants.NO_STRING_VALUE:
				sc.setEstado(etapaSiguiente.getTexto4());
				break;
			default:

				break;
			}

		} else {
			sc.setEstado(etapaSiguiente.getTexto2());
		}

		sc.setUsuarioActualiza(getUsuario().getUsuario());
		sc.setFechaActualiza(new Date());

		this.solicitudCobranzaServiceLocal.actualizarEstado(sc);
		try {
			this.lazySolicitudCobranza = new LazyCxcSolicitudCobranzaDataModel(this.noCia, null);
			this.fillUsuariosAprueban();
			enviarMail(sc);
			super.addInfoMessage("Enviado con", "Exito");

			// Mejoras Req - Agregar funcionalidad para guardar historial
			historialService.registrarHistorialSolicitud(sc.getSolicitudCobranzaPK().getNoCia(),
					sc.getSolicitudCobranzaPK().getNoSolicitud(), sc.getEstado(), sc.getUsuarioActualiza(),
					sc.getEstado());

		} catch (ServiceLocatorException e) {
			log.error(e);
		}

	}

	private void enviarMail(CxcSolicitudCobranza sc) {

		if (!sc.getEstado().equals(FINALIZADO)) {
			try {
				String grupoMail = ETAPAS_DEMANDA_F2.concat(this.obtenerEtapaSiguiente(sc.getEstado()).getTexto2());
				String asunto = "FORUMARIO DEMANDA F2 Nro. - ".concat(sc.getSolicitudCobranzaPK().getNoSolicitud().toString());
				StringBuffer mensaje = new StringBuffer("Se ha enviado el formulario demanda numero ");
				mensaje.append(sc.getSolicitudCobranzaPK().getNoSolicitud());
				mensaje.append(" para verificar su proceso.");
				this.mailServiceLocal.sendMailToGroup(grupoMail, getUsuario().getEmail(), asunto, mensaje, false);
			} catch (GeneralException | FindException e) {
				super.addWarnMessage("No se pudo enviar ", "la notificacion");
			}
		} else {
			super.addInfoMessage("La solicitud " + sc.getSolicitudCobranzaPK().getNoSolicitud(), "ha finalizado.");
		}
	}

	private CxcParametrosDet obtenerEtapaSiguiente(String estadoActual) {
		CxcParametrosDet etapaActual = this.lestados.stream().filter(item -> item.getTexto2().equals(estadoActual))
				.findFirst().get();
		CxcParametrosDet etapaSiguiente = this.lestados.stream()
				.filter(item -> item.getTexto2().equals(etapaActual.getTexto4())).findFirst().get();
		return etapaSiguiente;
	}

	public void ver(CxcSolicitudCobranzaArchivo archivo) {

		if (archivo.getNoSolicitudArchivo() != null) {
			this.verArchivo(archivo.getRuta());
		}

	}

	public void verDocumentoCredito(String ruta) {

		if (ruta != null) {
			this.verArchivo(ruta);
		}

	}

	private void verArchivo(String path) {
		String url = "openTab('/cxc-web-prime/images/dynamic?file=" + path + "')";
		super.ejecutarJavascript(url);
	}

	public void borrar(CxcSolicitudCobranzaArchivo archivo) {

		if (archivo.getNoSolicitudArchivo() != null) {
			this.solicitudCobranzaArchivoServiceLocal.eliminarArchivo(archivo);
		}
		cargarDocumentos();
		super.addInfoMessage("Exito", "");

	}

	public LazyCxcSolicitudCobranzaDataModel getLazySolicitudCobranza() {
		return lazySolicitudCobranza;
	}

	public void setLazySolicitudCobranza(LazyCxcSolicitudCobranzaDataModel lazySolicitudCobranza) {
		this.lazySolicitudCobranza = lazySolicitudCobranza;
	}

	public CxcSolicitudCobranza getSolicitudCobranza() {

		if (this.solicitudCobranza != null) {

			if (Objects.nonNull(dialogClientesController.getCliente())
					&& Objects.nonNull(dialogClientesController.getCliente().getClientePK())
					&& Objects.nonNull(dialogClientesController.getCliente().getClientePK().getNoCliente())) {

				Cliente cliente = dialogClientesController.getCliente();
				this.solicitudCobranza
						.setNombreCliente(cliente.getTipoIdentificacion().equals(RUC) ? cliente.getNombreComercial()
								: cliente.getNombre());
				this.solicitudCobranza.setTipoIdentificacion(cliente.getTipoIdentificacion());
				this.solicitudCobranza.setIdentificacion(cliente.getCedula());
				this.solicitudCobranza.setNoCliente(cliente.getClientePK().getNoCliente());
				dialogClientesController.setCliente(null);
				this.lcentroDigital = new ArrayList<>();
				super.updateComponentFromId("idfrmNuevaSolicitud");
			}

		}

		return solicitudCobranza;
	}

	// Mejoras Req - Nuevas funcionalidades para implementacion de filtro de busqueda
	public List<CxcHistorialSolCobranza> cargarHistorialporSoicitud(CxcSolicitudCobranza sc) {
		try {
			this.listaHistoriales = historialService.obtenerHistorialSolicitud(sc.getSolicitudCobranzaPK().getNoCia(),
					sc.getSolicitudCobranzaPK().getNoSolicitud());

		} catch (Exception e) {
			addInfoMessage(
					"No se ha encontrado información para la solicitud " + sc.getSolicitudCobranzaPK().getNoSolicitud(),
					"");
		}
		return listaHistoriales;
	}
	
	private boolean isNotEmpty(String s) {
	    return s != null && !s.trim().isEmpty();
	}

	/**
	 * Metodo para ejecutar filtros de busqueda
	 */
	public void busquedaSolicitudes() {
	    try {
	        if (!validarRangoFechas()) {
	            return;
	        }

	        Map<String, Object> filtros = new HashMap<>();

	        if (isNotEmpty(this.cedulaClienteConsulta)) {
	            filtros.put("identificacion", this.cedulaClienteConsulta.trim());
	        }
	        if (isNotEmpty(this.nombreClienteConsulta)) {
	            filtros.put("nombreCliente", this.nombreClienteConsulta.trim());
	        }
	        if (this.fechaDesde != null) {
	            filtros.put("fechaInicio", this.fechaDesde);
	        }
	        if (this.fechaHasta != null) {
	            filtros.put("fechaFin", this.fechaHasta);
	        }
	        if (isNotEmpty(this.estadoABuscar)) {
	            filtros.put("estado", this.estadoABuscar.trim());
	        }
	        if (isNotEmpty(this.facturaABuscar)) {
	            filtros.put("noFactu", this.facturaABuscar.trim());
	        }

	        this.lazySolicitudCobranza = new LazyCxcSolicitudCobranzaDataModel(this.noCia, filtros);
	    } catch (Exception e) {
	        log.error("Error en búsqueda de Solicitudes de Cobranza: " + e.getMessage(), e);
	        info("No se han encontrado Solicitudes de Cobranza con los criterios de búsqueda");
	    }
	}

	/**
	 * Valida fechas validas en buscador
	 * @return
	 */
	private boolean validarRangoFechas() {
		boolean esRangoAceptable = true;
		if ((this.fechaDesde != null && this.fechaHasta != null) && (this.fechaDesde.compareTo(this.fechaHasta) > 0)) {
			esRangoAceptable = false;
			addErrorMessage("Error: ", "La fecha (desde) no debe ser mayor a la fecha (hasta)");
		}
		return esRangoAceptable;
	}

	/**
	 * Metodo para encerar campos de filtros de busqueda
	 */
	public void limpiar() {
		this.nombreClienteConsulta = null;
		this.cedulaClienteConsulta = null;
	    this.facturaABuscar = null;
	    this.fechaDesde = null;
	    this.fechaHasta = null;
	    this.construct();
	    this.init();
	}

	public void ejecutarConsultaClientes() {
		this.lazyModel = new LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(),
				getCedulaClienteConsulta(), getNombreClienteConsulta());
	}
	
	public void seleccionarCliente(Cliente cliente) {
		this.cedulaClienteConsulta = cliente.getCedula();
		this.nombreClienteConsulta = cliente.getNombre();
	}

	
	public int getBatchSize() {
		return CommonConstants.BATCH_SIZE;
	}

	public void setSolicitudCobranza(CxcSolicitudCobranza solicitudCobranza) {
		this.solicitudCobranza = solicitudCobranza;
	}

	public List<FacVentasCab> getLfacturas() {
		return lfacturas;
	}

	public void setLfacturas(List<FacVentasCab> lfacturas) {
		this.lfacturas = lfacturas;
	}

	public String getFacturaABuscar() {
		return facturaABuscar;
	}

	public void setFacturaABuscar(String facturaABuscar) {
		this.facturaABuscar = facturaABuscar;
	}

	public boolean isEdicion() {
		return edicion;
	}

	public void setEdicion(boolean edicion) {
		this.edicion = edicion;
	}

	public List<CxcSolicitudCobranzaArchivo> getLsolicitudCobranzaArchivo() {
		return lsolicitudCobranzaArchivo;
	}

	public void setLsolicitudCobranzaArchivo(List<CxcSolicitudCobranzaArchivo> lsolicitudCobranzaArchivo) {
		this.lsolicitudCobranzaArchivo = lsolicitudCobranzaArchivo;
	}

	public String getCedulaABuscar() {
		return cedulaABuscar;
	}

	public void setCedulaABuscar(String cedulaABuscar) {
		this.cedulaABuscar = cedulaABuscar;
	}

	public String getNombreABuscar() {
		return nombreABuscar;
	}

	public void setNombreABuscar(String nombreABuscar) {
		this.nombreABuscar = nombreABuscar;
	}

	public String getUrlArchivo() {
		return urlArchivo;
	}

	public void setUrlArchivo(String urlArchivo) {
		this.urlArchivo = urlArchivo;
	}

	public List<CxcParametrosDet> getLestados() {
		return lestados;
	}

	public void setLestados(List<CxcParametrosDet> lestados) {
		this.lestados = lestados;
	}

	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}

	public void setDialogClientesController(DialogClientesController dialogClientesController) {
		this.dialogClientesController = dialogClientesController;
	}

	public List<HistoricoDocumentoDto> getDocumentosHistoricos() {
		return documentosHistoricos;
	}

	public void setDocumentosHistoricos(List<HistoricoDocumentoDto> documentosHistoricos) {
		this.documentosHistoricos = documentosHistoricos;
	}

	public HistoricoDocumentoDto getFacturaSelect() {
		return facturaSelect;
	}

	public void setFacturaSelect(HistoricoDocumentoDto facturaSelect) {
		this.facturaSelect = facturaSelect;
	}

	public List<String> getUsuariosCrean() {
		return usuariosCrean;
	}

	public void setUsuariosCrean(List<String> usuariosCrean) {
		this.usuariosCrean = usuariosCrean;
	}

	public List<CxcCreditoDigital> getLcentroDigital() {
		return lcentroDigital;
	}

	public void setLcentroDigital(List<CxcCreditoDigital> lcentroDigital) {
		this.lcentroDigital = lcentroDigital;
	}

	public List<CxcHistorialSolCobranza> getListaHistoriales() {
		return listaHistoriales;
	}

	public void setListaHistoriales(List<CxcHistorialSolCobranza> listaHistoriales) {
		this.listaHistoriales = listaHistoriales;
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

	public String getEstadoABuscar() {
		return estadoABuscar;
	}

	public void setEstadoABuscar(String estadoABuscar) {
		this.estadoABuscar = estadoABuscar;
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

}