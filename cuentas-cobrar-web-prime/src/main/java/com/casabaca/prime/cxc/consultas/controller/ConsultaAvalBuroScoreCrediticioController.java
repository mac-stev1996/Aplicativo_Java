package com.casabaca.prime.cxc.consultas.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.model.SisParametrosWebServices;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.service.SisParametrosWebServicesServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcAvalBuro;
import com.casabaca.cxc.ejb.modelo.CxcCredito;
import com.casabaca.cxc.ejb.modelo.CxcCreditoPK;
import com.casabaca.cxc.ejb.servicio.CreditoDigitalServiceLocal;
import com.casabaca.cxc.ejb.servicio.CreditoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcAvalBuroServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.consultas.avalburo.dto.Consulta12MesesReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.ContactabilidadReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.ContactoReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.CuentaFinancieraReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.CxcAvalBuroJson;
import com.casabaca.prime.cxc.consultas.avalburo.dto.DetalleEmpresaReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.DeudaVigenteReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.FactorScoreReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.IndicadoresDeudaReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.IndiceFinancieroReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.InformacionRucReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.ModeloCentricReglaReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.OfertaSugeridaReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.OperacionCreditoReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.PersonaEmpresaReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.ReporteAvalBuroScoreSeccionDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.SelectItemDto;
import com.casabaca.prime.cxc.consultas.avalburo.dto.SerieRiesgoReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.ServicioHistoricoReporteDTO;
import com.casabaca.prime.cxc.consultas.avalburo.dto.TarjetaReporteDTO;
import com.casabaca.rest.wsNexumCredito.entidad.SubirDocumentoBuroResponse;
import com.casabaca.rest.wsNexumCredito.util.ConstantesNexumCredito;
import com.casabaca.rest.wsScoreCrediticio.entidad.ConsultaResponse;
import com.casabaca.rest.wsScoreCrediticio.util.AvalBuroResponseUtils;
import com.casabaca.webservice.wsNexumCredito.ServicioWebNexumCredito;
import com.casabaca.webservice.wsNexumCredito.impl.ServicioWebNexumCreditoImpl;
import com.casabaca.webservice.wsScoreCrediticio.impl.ServicioConsultaScoreCrediticio;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@ManagedBean
@ViewScoped
public class ConsultaAvalBuroScoreCrediticioController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(ConsultaAvalBuroScoreCrediticioController.class);

	private static final String POSITIVO = "POSITIVO";
	private static final String NEGATIVO = "NEGATIVO";

	private static final String LABEL_PREAPROBADO = "PREAPROBADO";
	private static final String LABEL_NEGADO = "NO PREAPROBADO";
	private static final String LABEL_ANALISIS = "ANÁLISIS";

	private static final int LONGITUD_MAXIMA_IDENTIFICACION = 20;
	private static final int LONGITUD_MAXIMA_INGRESOS = 7;
	private static final Pattern DATE_YYYY_MM_DD = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
	private static final SimpleDateFormat IN_DATE = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat OUT_DATE = new SimpleDateFormat("dd/MM/yyyy");

	private static final SimpleDateFormat CHART_IN_DATE = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat CHART_OUT_DATE = new SimpleDateFormat("MMM.yy", new Locale("es", "EC"));
	private static final String ORIGEN_SOLICITUD_CREDITO = "SOLICITUD_CREDITO";
	private static final String TIPO_DOCUMENTO_BURO_CREDITO = "BURO DE CREDITO";
	private static final String TIPO_DOCUMENTO_URL_FAB_CREDITO = "URL_FAB_CREDITO";
	private static final String TIPO_DOC_API_APPROVAL = "approval";
	private static final String MIME_TYPE_TEXT = "text/plain";
	private static final String TIPO_CREDITO_CLIENTE = "C";
	private static final String REPORTE_JASPER_AVAL_BURO = "/cxc/repAvalBuroScoreCrediticio";
	private static final String PERSONA_NATURAL = "PERSONA_NATURAL";
	private static final String PERSONA_JURIDICA = "PERSONA_JURIDICA";
	private static final String SECCION_MODELO_CENTRIC = "modeloCentric";
	private static final String SECCION_MODELO_CENTRIC_JURIDICO = "modeloCentricJuridico";
	private static final String SECCION_MODELO_CENTRIC_RESUMEN = "modeloCentricResumen";
	private static final String SECCION_MODELO_CENTRIC_DETALLE = "modeloCentricDetalle";
	private static final String NODO_MODELO_RESUMEN = "resumen";
	private static final String NODO_MODELO_DETALLE = "detalle";
	private static final String SECCION_OPERACIONES_VIGENTES_TARJETA = "operacionesVigentesTarjeta";
	private static final String SECCION_DETALLE_TARJETA_SALDO_VIGENTE = "detalleTarjetaSaldoVigente";
	private static final String SECCION_DETALLE_TARJETA_CREDITO = "detalleTarjetaCredito";
	private static final String COLUMNA_DETALLE_TARJETA = "__detalleTarjeta";
	private static final int LIMITE_FILAS_REPORTE = 10;
	private static final Map<String, String> TITULOS_SECCIONES = crearTitulosSecciones();
	private static final List<String> SECCIONES_NATURAL = Arrays.asList("identificacionTitular", SECCION_MODELO_CENTRIC,
			"parametrosEntrada", "capacidadPago", "ofertaSugerida", "ofertaSugeridaCapacidadCalculada",
			"scoreFinanciero", "scoreFinancieroV2", "factoresScore", "informacionComoRUC", "relacionEmpresas",
			"datosContacto");
	private static final List<String> SECCIONES_JURIDICA = Arrays.asList("datosGeneralesEmpresa",
			SECCION_MODELO_CENTRIC_JURIDICO, "scoreEmpresa", "detalleEmpresa", "resumenPrincipalesCuentasFinancieras",
			"cuentasEstadosFinancieros", "indicesFinancieros", "principalesAccionistas", "representantesLegales",
			"contactabilidad");
	private static final String PARAM_DIAS_AVAL_BURO = "DIASAB";

	@EJB(lookup = NombreJNDI.CXC_AVAL_BURO_SERVICE)
	private CxcAvalBuroServiceLocal cxcAvalBuroService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	@EJB(lookup = NombreJNDI.CREDITO_DIGITAL_SERVICE)
	private CreditoDigitalServiceLocal creditoDigitalService;

	@EJB(lookup = NombreJNDI.CREDITO_SERVICE)
	private CreditoServiceLocal creditoService;

	@EJB(lookup = NombreJNDI.SIS_PARAMETRO_WEB_SERVICES_SERVICE)
	private SisParametrosWebServicesServiceLocal sisParametrosWebServicesServiceLocal;

	// Campos busqueda
	private String tipoCliente;
	private String identificacionCliente;
	private BigDecimal ingresos;
	private String tipoModelo;
	private String marca;
	private boolean mostrarFiltrosScoreVehiculo = true;

	// Mostrar pantalla
	private boolean mostrarPantallasConsulta = false;
	private String logo = "/avalburo.png";

	// Rspuesta
	private ConsultaResponse consultaResponse;

	// Resultado dinamico
	private Map<String, List<Map<String, Object>>> secciones = new LinkedHashMap<>();
	private Map<String, List<String>> columnasPorSeccion = new LinkedHashMap<>();
	private List<String> seccionesOrdenadas = new ArrayList<>();

	private final Gson gson = new GsonBuilder().serializeNulls().create();

	private String noCia;
	private boolean solicitudCredito;
	private boolean generarFileCrediticioBloqueado;
	private Long solicitudCreditoNumero;
	private String noCiaSolicitudCredito;
	private String cedulaClienteSolicitudCredito;
	private Long creditRequestIdSolicitudCredito;
	private int numeroDiasValidoAvalBuro;
	private Date fechaConsulta;
	private String nombreUsuarioConsulta;
	private String nombreEmpresa;

	private String evolucionScoreChartJson;
	private Integer evolucionScoreMin;
	private Integer evolucionScoreMax;

	private String semaforoChartJson;
	private String tendenciaDeudaChartJson;

	private List<SelectItemDto> listaMarcas;

	private CxcAvalBuro existeCedula;
	private Date fechaInicio = new Date();
	private Date fechaFin = new Date();
	private boolean existe;
	private Date fechaIniPantalla;
	private Date fechaFinPantalla;
	private String mensajeNuevaBusqueda;

	// Filtros para UI
	private String tipoDeudorSeleccionado = "Titular";
	private String tipoDeudorSemaforo = "Titular";
	private String tipoCreditoSemaforo = "Comercial";
	private String sistemaCrediticioSemaforo = "Comercial";

	private String tipoDeudorTendencia = "Titular";
	private String tipoCreditoTendencia = "Comercial";
	private String sistemaCrediticioTendencia = "Comercial";
	private Integer mesesTendencia = 24;

	private String tipoDeudorHistoricoComercial = "Titular";
	private Integer mesesHistoricoComercial = 12;

	private String tipoDeudorHistoricoBanco = "Titular";
	private Integer mesesHistoricoBanco = 24;

	private String tipoDeudorHistoricoCooperativa = "Titular";
	private Integer mesesHistoricoCooperativa = 24;

	private String tipoDeudorVigenteBanco = "Titular";
	private String tipoDeudorVigenteCooperativa = "Titular";
	private String tipoDeudorVigenteComercial = "Titular";
	private String tipoDeudorVigenteCobranza = "Titular";
	private String gastoFinancieroChartJson;
	private String tipoTramaResultado;
	private Map<String, Object> operacionTarjetaSeleccionada = new LinkedHashMap<String, Object>();
	private List<Map<String, Object>> detalleTarjetaCreditoSeleccionado = new ArrayList<Map<String, Object>>();

	private static Map<String, String> crearTitulosSecciones() {
		Map<String, String> titulos = new LinkedHashMap<String, String>();
		titulos.put("identificacionTitular", "Identificación del Titular");
		titulos.put("datosGeneralesEmpresa", "Datos Generales Empresa");
		titulos.put(SECCION_MODELO_CENTRIC_RESUMEN, "Resumen Modelo Centric");
		titulos.put(SECCION_MODELO_CENTRIC_DETALLE, "Detalle Reglas Modelo Centric");
		titulos.put(SECCION_MODELO_CENTRIC_JURIDICO, "Modelo Centric Jurídico");
		titulos.put("tipoSujeto", "Tipo de Sujeto");
		titulos.put("parametrosEntrada", "Parámetros de Entrada");
		titulos.put("capacidadPago", "Capacidad de Pago");
		titulos.put("ofertaSugeridaCapacidadCalculada", "Oferta Sugerida con Capacidad Calculada");
		titulos.put("scoreFinancieroV2", "Score Financiero");
		titulos.put("scoreEmpresa", "Score Empresa");
		titulos.put("factoresScore", "Factores que influyen en el Score");
		titulos.put("manejoCuentasCorrientes", "Manejo de Cuentas Corrientes");
		titulos.put("deudaVigenteTotal", "Deuda Vigente Total");
		titulos.put("gastoFinanciero", "Gasto Financiero");
		titulos.put("operacionesCodeudorGarante", "Operaciones en las que es Codeudor");
		titulos.put("informacionComoRUC", "Información como RUC");
		titulos.put(SECCION_OPERACIONES_VIGENTES_TARJETA, "Operaciones Vigentes de Tarjetas de Crédito");
		titulos.put(SECCION_DETALLE_TARJETA_SALDO_VIGENTE, "Detalle adicional de Tarjetas de Crédito");
		titulos.put("indicadoresTarjeta", "Indicadores Tarjeta");
		titulos.put(SECCION_DETALLE_TARJETA_CREDITO, "Detalle Tarjeta Crédito");
		titulos.put("operacionesVigentesBanco", "Operaciones Vigentes Banco");
		titulos.put("estructuraOperacionBancosDetalle", "Estructura Operación Bancos Detalle");
		titulos.put("operacionesVigentesCooperativa", "Operaciones Vigentes Cooperativa");
		titulos.put("estructuraOperacionCooperativaDetalle", "Estructura Operación Cooperativa Detalle");
		titulos.put("operacionesVigentesEmpresa", "Operaciones Vigentes Empresa");
		titulos.put("operacionesVigentesServicio", "Operaciones Vigentes Servicio");
		titulos.put("operacionesVigentesCobranza", "Operaciones Vigentes Cobranza");
		titulos.put("evolucionScoreFinancieroV2", "Evolución Score Financiero");
		titulos.put("evolucionScoreFinanciero", "Evolución Score Financiero");
		titulos.put("semaforoMaximoDiasVencido", "Semáforo Máximo Días Vencido");
		titulos.put("tendenciaDeuda", "Tendencia Deuda");
		titulos.put("indicadoresDeuda", "Indicadores Deuda");
		titulos.put("operacionesHistoricasTarjeta", "Operaciones Históricas Tarjeta");
		titulos.put("operacionesHistoricasBanco", "Operaciones Históricas Banco");
		titulos.put("operacionesHistoricasCooperativa", "Operaciones Históricas Cooperativa");
		titulos.put("operacionesHistoricasEmpresa", "Operaciones Históricas Empresa");
		titulos.put("operacionesHistoricasServicio", "Operaciones Históricas Servicio");
		titulos.put("operacionesHistoricasCobranza", "Operaciones Históricas Cobranza");
		titulos.put("detalleEmpresa", "Detalle Empresa");
		titulos.put("resumenPrincipalesCuentasFinancieras", "Resumen Principales Cuentas Financieras");
		titulos.put("cuentasEstadosFinancieros", "Cuentas Estados Financieros");
		titulos.put("indicesFinancieros", "Índices Financieros");
		titulos.put("principalesAccionistas", "Principales Accionistas");
		titulos.put("representantesLegales", "Representantes Legales");
		titulos.put("relacionEmpresas", "Relación Empresas");
		titulos.put("datosContacto", "Datos de Contacto");
		titulos.put("contactabilidad", "Contactabilidad");
		titulos.put("titularConsultado12Meses", "Titular Consultado en los Últimos 12 Meses");
		return Collections.unmodifiableMap(titulos);
	}

	@PostConstruct
	public void init() {
		this.ingresos = null;
		this.mostrarPantallasConsulta = false;
		this.logo = "/logo-aval-buro.png";
		this.nombreUsuarioConsulta = getUsuario().getUsuario();
		this.nombreEmpresa = getCompania().getNombre();
		this.fechaConsulta = new Date();
		this.listaMarcas = new ArrayList<SelectItemDto>();
		this.noCia = getCompania().getNoCia();
		this.cargarParametroScoreVehiculo();
		this.cargarParametrosSolicitudCredito();
		this.cargarDiasValidosAvalBuro();
		this.limpiarResultados();
	}

	private void cargarParametroScoreVehiculo() {
		String scoreVehiculo = getRequestParameter("scoreVehiculo");
		if (scoreVehiculo != null) {
			scoreVehiculo = scoreVehiculo.trim().replace("'", "").replace("\"", "");
		}
		this.mostrarFiltrosScoreVehiculo = !"N".equalsIgnoreCase(scoreVehiculo);
		aplicarParametrosScoreVehiculo();
	}

	private void aplicarParametrosScoreVehiculo() {
		if (mostrarFiltrosScoreVehiculo) {
			return;
		}
		this.ingresos = new BigDecimal("2000");
		this.tipoModelo = "semiNuevo";
		this.marca = "-";
	}

	private void cargarParametrosSolicitudCredito() {
		String origen = getRequestParameter("origen");
		this.solicitudCredito = ORIGEN_SOLICITUD_CREDITO.equals(origen);
		if (!this.solicitudCredito) {
			return;
		}

		this.noCiaSolicitudCredito = getRequestParameter("noCiaSolicitudCredito");
		this.cedulaClienteSolicitudCredito = getRequestParameter("cedulaCliente");
		// Nuevo parametro para Fabrica Credito Nexumcorp
		this.creditRequestIdSolicitudCredito = parseLong(getRequestParameter("creditRequestId"));
		String solicitud = getRequestParameter("solicitud");
		if (!isBlank(solicitud)) {
			try {
				this.solicitudCreditoNumero = Long.valueOf(solicitud);
			} catch (NumberFormatException e) {
				LOG.warn("Parametro solicitud invalido para Aval Buro: " + solicitud, e);
				this.solicitudCreditoNumero = null;
			}
		}
	}

	/**
	 * Metodo para recuperar consumo de servicio web
	 */
	public void consultarScoreCrediticio() {
		try {
			aplicarParametrosScoreVehiculo();
			if (!validarParametrosBusqueda()) {
				return;
			}

			limpiarResultados();

			if (!CommonConstants.CENTRIC.equals(noCia) && cargarRespuestaDesdeWsLocalSiExiste()) {
				addInfoMessage("Info",
						"La consulta ya existe en la base de datos central y se recuperó desde el servicio interno.");
				return;
			}

			existeHistorialCrediticioPorCedula();

			if (existeCedula != null) {

				if (tieneJsonV2Disponible(existeCedula) && registroVigente(existeCedula)) {
					LOG.debug("Consulta vigente en BD para identificacion.");
					cargarRespuestaDesdeBd();
					addInfoMessage("Info", "La consulta ya existe y se recuperó desde la base de datos.");
					return;
				}

				LOG.debug("Existe registro pero no aplica JSON_V2 vigente. Se consultara WS.");
				consultarWSModeloCentricYGuardar(true);
				return;
			}

			LOG.debug("No existe historial en CXC_AVAL_BURO. Se consultara WS.");
			consultarWSModeloCentricYGuardar(false);

		} catch (Exception e) {
			LOG.error("Error consultando Modelo Centric", e);
			this.mostrarPantallasConsulta = false;
			limpiarResultados();
			addErrorMessage("Error", "Error consultando Modelo Centric: " + e.getMessage());
		}
	}

	private boolean cargarRespuestaDesdeWsLocalSiExiste() {
		HttpURLConnection connection = null;
		try {
			List<ParamDet> urlWSlocal = paramDetService.buscarTiposIdentificacion(noCia, "URLWSA");
			if (urlWSlocal == null || urlWSlocal.isEmpty() || isBlank(urlWSlocal.get(0).getTexto1())) {
				LOG.warn("No se encontró parámetro URLWSA para noCia=" + noCia);
				return false;
			}

			String urlConsulta = urlWSlocal.get(0).getTexto1() + "consultaravalburo?tipoIdentificacion=" + tipoCliente
					+ "&cedula=" + identificacionCliente;
			LOG.debug("Consultando WS local para score.");

			connection = (HttpURLConnection) new URL(urlConsulta).openConnection();
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				LOG.debug("WS local no devolvio 200 para score. responseCode=" + responseCode);
				return false;
			}

			StringBuilder response = new StringBuilder();
			try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}

			CxcAvalBuroJson consultaLocal = gson.fromJson(response.toString(), CxcAvalBuroJson.class);
			String jsonLocalDisponible = obtenerJsonLocalV2(consultaLocal);
			if (consultaLocal == null || isBlank(jsonLocalDisponible)) {
				LOG.debug("WS local no contiene JSON_V2 para score. Se continuara con flujo normal.");
				return false;
			}

			Date fechaFinLocal = parseFechaWsLocal(consultaLocal.getFechaFin());
			if (!esConsultaVigente(fechaFinLocal)) {
				LOG.debug("WS local tiene JSON_V2 vencido para score. Se continuara con flujo normal.");
				return false;
			}

			String jsonLocal = normalizarJson(jsonLocalDisponible);
			ConsultaResponse respuestaLocal = gson.fromJson(jsonLocal, ConsultaResponse.class);

			if (respuestaLocal == null || respuestaLocal.getResult() == null) {
				LOG.debug("JSON devuelto por WS local no corresponde a score crediticio.");
				return false;
			}

			this.consultaResponse = respuestaLocal;
			this.fechaConsulta = parseFechaWsLocal(consultaLocal.getFechaInicio());
			this.fechaIniPantalla = parseFechaWsLocal(consultaLocal.getFechaInicio());
			this.fechaFinPantalla = parseFechaWsLocal(consultaLocal.getFechaFin());
			procesarRespuestaModeloCentric(respuestaLocal);

			return this.mostrarPantallasConsulta;
		} catch (Exception e) {
			LOG.warn("No fue posible recuperar score desde WS local. Se continuará con flujo normal.", e);
			return false;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String obtenerJsonLocalV2(CxcAvalBuroJson consultaLocal) {
		if (consultaLocal == null) {
			return null;
		}

		if (!isBlank(consultaLocal.getJsonV2())) {
			return consultaLocal.getJsonV2();
		}
		return null;
	}

	private Date parseFechaWsLocal(String fecha) {
		if (isBlank(fecha)) {
			return null;
		}
		try {
			return IN_DATE.parse(fecha);
		} catch (Exception e) {
			try {
				return OUT_DATE.parse(fecha);
			} catch (Exception e2) {
				LOG.warn("No se pudo parsear fecha del WS local: " + fecha);
				return null;
			}
		}
	}

	public void limpiarPantalla() {
		this.tipoCliente = null;
		this.identificacionCliente = null;
		this.ingresos = null;
		this.tipoModelo = null;
		this.marca = null;
		aplicarParametrosScoreVehiculo();
		this.generarFileCrediticioBloqueado = false;
		this.mostrarPantallasConsulta = false;
		this.consultaResponse = null;
		limpiarResultados();
	}

	private void limpiarResultados() {
		limpiarResultados(true);
	}

	private void limpiarResultados(boolean limpiarTipoTrama) {
		this.secciones.clear();
		this.columnasPorSeccion.clear();
		this.seccionesOrdenadas.clear();
		this.evolucionScoreChartJson = null;
		this.evolucionScoreMin = null;
		this.evolucionScoreMax = null;
		this.semaforoChartJson = null;
		this.tendenciaDeudaChartJson = null;
		this.gastoFinancieroChartJson = null;
		this.operacionTarjetaSeleccionada = new LinkedHashMap<String, Object>();
		this.detalleTarjetaCreditoSeleccionado = new ArrayList<Map<String, Object>>();
		if (limpiarTipoTrama) {
			this.tipoTramaResultado = null;
		}
	}

	private boolean esConsultaVigente(Date fechaFin) {
		if (fechaFin == null) {
			return false;
		}

		Date hoy = new Date();

		return !fechaFin.before(hoy);
	}

	private String normalizarJson(String json) {
		if (isBlank(json)) {
			return json;
		}

		String jsonNormalizado = json.trim();

		// Si vino serializado como string entre comillas, lo desescapa
		if (jsonNormalizado.startsWith("\"") && jsonNormalizado.endsWith("\"")) {
			jsonNormalizado = jsonNormalizado.substring(1, jsonNormalizado.length() - 1).replace("\\\"", "\"")
					.replace("\\\\", "\\");
		}

		return jsonNormalizado;
	}

	private void cargarRespuestaDesdeBd() {
		try {
			String jsonBd = normalizarJson(existeCedula.getJsonV2());

			if (isBlank(jsonBd)) {
				addWarnMessage("Advertencia", "No existe contenido en JSON_V2 para el cliente consultado.");
				this.mostrarPantallasConsulta = false;
				return;
			}

			ConsultaResponse respBd = gson.fromJson(jsonBd, ConsultaResponse.class);

			if (respBd == null) {
				addWarnMessage("Advertencia", "No se pudo reconstruir la respuesta desde JSON_V2.");
				this.mostrarPantallasConsulta = false;
				return;
			}

			this.consultaResponse = respBd;
			this.fechaConsulta = existeCedula.getFechaInicio();
			this.fechaIniPantalla = existeCedula.getFechaInicio();
			this.fechaFinPantalla = existeCedula.getFechaFin();

			procesarRespuestaModeloCentric(respBd);

		} catch (Exception e) {
			LOG.error("Error cargando respuesta desde JSON_V2", e);
			this.mostrarPantallasConsulta = false;
			limpiarResultados();
			addErrorMessage("Error", "No se pudo recuperar la consulta almacenada.");
		}
	}

	private void consultarWSModeloCentricYGuardar(boolean actualizarRegistroExistente) {
		try {
			String tipoModeloNorm = tipoModelo.trim().toLowerCase();
			String marcaEnviar = "seminuevo".equals(tipoModeloNorm) ? "-" : marca.trim();

			ServicioConsultaScoreCrediticio ws = new ServicioConsultaScoreCrediticio();
			ConsultaResponse resp = ws.consultarAvalModeloCentric(noCia, tipoCliente.trim(),
					identificacionCliente.trim(), obtenerIngresosEnteros(), tipoModeloNorm, marcaEnviar);

			this.consultaResponse = resp;

			String code = resp != null ? resp.getResponseCode() : null;
			String msg = resp != null ? resp.getMessage() : null;
			String normalizedCode = AvalBuroResponseUtils.getNormalizedCode(resp);

			LOG.debug("ModeloCentric statusRaw=" + code + " statusNorm=" + normalizedCode + " msg=" + msg);

			if (!AvalBuroResponseUtils.isSuccessResponse(resp)) {
				this.mostrarPantallasConsulta = false;
				limpiarResultados();
				mostrarMensajeErrorServicio(code, msg);
				return;
			}

			if (resp == null || resp.getResult() == null) {
				this.mostrarPantallasConsulta = false;
				limpiarResultados();
				addWarnMessage("Servicio", "La respuesta no contiene información de result.");
				return;
			}

			Map<String, Object> resultMap = toMap(resp.getResult());
			if (resultMap == null || resultMap.isEmpty()) {
				this.mostrarPantallasConsulta = false;
				limpiarResultados();
				addWarnMessage("Servicio", "No se pudo interpretar el contenido de result.");
				return;
			}
			agregarParametrosEntradaResult(resultMap, tipoModeloNorm, marcaEnviar, obtenerIngresosEnteros());

			// Mostrar pantalla
			procesarRespuestaModeloCentric(resp);

			// Guardar o actualizar BD
			guardarConsultaModeloCentric(resp);

			if (actualizarRegistroExistente) {
				addInfoMessage("Info", "La consulta estaba vencida y fue actualizada desde el servicio web.");
			} else {
				addInfoMessage("Info", "La consulta fue obtenida desde el servicio web y guardada en base local.");
			}

		} catch (Exception e) {
			LOG.error("Error consultando y guardando Modelo Centric", e);
			this.mostrarPantallasConsulta = false;
			limpiarResultados();
			addErrorMessage("Error", "Error consultando Modelo Centric: " + e.getMessage());
		}
	}

	/**
	 * Metodo para construir secciones en pantalla
	 * 
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	private void buildSeccionesFromResult(Map<String, Object> result) {
		limpiarResultados(false);
		if (result == null || result.isEmpty()) {
			return;
		}

		addModeloCentricSecciones(result.get(SECCION_MODELO_CENTRIC));
		boolean modeloCentricJuridicoNormalizado = addModeloCentricSecciones(
				result.get(SECCION_MODELO_CENTRIC_JURIDICO));

		for (Map.Entry<String, Object> entry : result.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();

			if (SECCION_MODELO_CENTRIC.equals(key)
					|| (SECCION_MODELO_CENTRIC_JURIDICO.equals(key) && modeloCentricJuridicoNormalizado)) {
				continue;
			}

			if (val instanceof List) {
				List<?> lst = (List<?>) val;
				List<Map<String, Object>> rows = new ArrayList<>();
				for (Object item : lst) {
					if (item instanceof Map) {
						rows.add((Map<String, Object>) item);
					}
				}
				addSeccion(key, rows);
			} else if (val instanceof Map) {
				addSeccion(key, Arrays.asList((Map<String, Object>) val));
			}
		}

		normalizarSeccionesPorTipoTrama();
	}

	private void addSeccion(String key, List<Map<String, Object>> rows) {
		if (key == null) {
			return;
		}
		if (rows == null) {
			rows = new ArrayList<>();
		}
		secciones.put(key, rows);
		columnasPorSeccion.put(key, inferColumns(rows));
	}

	private List<String> inferColumns(List<Map<String, Object>> rows) {
		LinkedHashSet<String> cols = new LinkedHashSet<>();
		if (rows != null) {
			for (Map<String, Object> r : rows) {
				if (r != null) {
					cols.addAll(r.keySet());
				}
			}
		}
		return new ArrayList<>(cols);
	}

	/**
	 * Metotod para obtener Titulos y orden de las secciones
	 * 
	 * @param keys
	 * @return
	 */
	private List<String> ordenarSecciones(List<String> keys) {
		List<String> priority = new ArrayList<String>(TITULOS_SECCIONES.keySet());

		List<String> ordered = new ArrayList<>();
		for (String p : priority) {
			if (keys.contains(p)) {
				ordered.add(p);
			}
		}

		for (String k : keys) {
			if (!ordered.contains(k)) {
				ordered.add(k);
			}
		}
		return ordered;
	}

	public String tituloSeccion(String key) {
		if (key == null) {
			return "";
		}

		String titulo = TITULOS_SECCIONES.get(key);
		return titulo != null ? titulo : humanize(key);
	}

	private List<Map<String, Object>> filtrarPorCampo(List<Map<String, Object>> rows, String campo, String valor) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (rows == null || rows.isEmpty()) {
			return result;
		}

		for (Map<String, Object> row : rows) {
			Object v = row.get(campo);
			if (v == null) {
				result.add(row);
				continue;
			}
			if (v != null && valor != null && valor.equalsIgnoreCase(String.valueOf(v).trim())) {
				result.add(row);
			}
		}
		return result;
	}

	private List<Map<String, Object>> filtrarPorMeses(List<Map<String, Object>> rows, String campoFecha, int meses) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (rows == null || rows.isEmpty()) {
			return result;
		}

		int total = rows.size();
		int desde = Math.max(0, total - meses);

		for (int i = desde; i < total; i++) {
			result.add(rows.get(i));
		}
		return result;
	}

	private boolean equalsIgnoreCaseSafe(Object value, String expected) {
		return value != null && expected != null && expected.equalsIgnoreCase(String.valueOf(value).trim());
	}

	/**
	 * Getter Filtrados para tablas vigentes
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getOperacionesVigentesBancoFiltradas() {
		return filtrarPorCampo(getFilas("operacionesVigentesBanco"), "tipoDeudor", tipoDeudorVigenteBanco);
	}

	public List<Map<String, Object>> getOperacionesVigentesCooperativaFiltradas() {
		return filtrarPorCampo(getFilas("operacionesVigentesCooperativa"), "tipoDeudor", tipoDeudorVigenteCooperativa);
	}

	public List<Map<String, Object>> getOperacionesVigentesComercialFiltradas() {
		return filtrarPorCampo(getFilas("operacionesVigentesComercial"), "tipoDeudor", tipoDeudorVigenteComercial);
	}

	public List<Map<String, Object>> getOperacionesVigentesCobranzaFiltradas() {
		return filtrarPorCampo(getFilas("operacionesVigentesCobranza"), "tipoDeudor", tipoDeudorVigenteCobranza);
	}

	/**
	 * Lista filtrada para historico comercial
	 * 
	 * @return
	 */
	public List<Map<String, Object>> getOperacionesHistoricasComercialFiltradas() {
		List<Map<String, Object>> rows = getFilas("operacionesHistoricasComercial");
		rows = filtrarPorCampo(rows, "tipoDeudor", tipoDeudorHistoricoComercial);
		rows = filtrarPorMeses(rows, "fechaCorte", mesesHistoricoComercial != null ? mesesHistoricoComercial : 12);
		return rows;
	}

	public void seleccionarTipoDeudorVigenteComercialTitular() {
		this.tipoDeudorVigenteComercial = "Titular";
	}

	public void seleccionarTipoDeudorVigenteComercialCodeudor() {
		this.tipoDeudorVigenteComercial = "Codeudor";
	}

	/**
	 * Metodo para las columnas y formatos
	 * 
	 * @param col
	 * @return
	 */
	public String labelColumna(String col) {
		if (col == null) {
			return "";
		}

		switch (col) {
		case "tipoIdentificacionSujetoDescripcion":
			return "Tipo Documento";
		case "identificacionSujeto":
			return "Identificación";
		case "nombreRazonSocial":
			return "Nombre / Razón Social";
		case "tipoSujeto":
			return "Tipo Sujeto";
		case "fechaEvaluacion":
			return "Fecha Evaluación";
		case "decisionModelo":
			return "Decisión";
		case "tipoDecision":
			return "Tipo Decisión";
		case "reglaPadre":
			return "Regla Padre";
		case "reglaUsada":
			return "Regla";
		case "valorObtenido":
			return "Valor";
		case "estadoResultado":
			return "Resultado";
		case "clientesPeorScore":
			return "% Personas con peor score";
		case "tasaMalos":
			return "% Probabilidad de caer en mora";
		default:
			return humanize(col);
		}
	}

	public String labelColumna(String seccion, String col) {
		if (esSeccionFinancieraAnual(seccion)) {
			String etiquetaFinancieraAnual = labelColumnaFinancieraAnual(col);
			if (etiquetaFinancieraAnual != null) {
				return etiquetaFinancieraAnual;
			}
		}
		return labelColumna(col);
	}

	private boolean esSeccionFinancieraAnual(String seccion) {
		return "resumenPrincipalesCuentasFinancieras".equals(seccion) || "cuentasEstadosFinancieros".equals(seccion)
				|| "indicesFinancieros".equals(seccion);
	}

	private String labelColumnaFinancieraAnual(String col) {
		if ("cuenta".equals(col)) {
			return "Cuenta Contable";
		}
		if ("anioActualMenos5".equals(col)) {
			return anioActualMenos(5);
		}
		if ("anioActualMenos4".equals(col)) {
			return anioActualMenos(4);
		}
		if ("anioActualMenos3".equals(col)) {
			return anioActualMenos(3);
		}
		if ("anioActualMenos2".equals(col)) {
			return anioActualMenos(2);
		}
		if ("anioActualMenos1".equals(col)) {
			return anioActualMenos(1);
		}
		return null;
	}

	private String anioActualMenos(int anios) {
		Calendar calendario = Calendar.getInstance();
		return String.valueOf(calendario.get(Calendar.YEAR) - anios);
	}

	public boolean colOculta(String col) {
		if (col == null) {
			return false;
		}

		return "color".equalsIgnoreCase(col) || "radio".equalsIgnoreCase(col) || "ejeX".equalsIgnoreCase(col);
	}

	public String colStyleClass(String col) {
		if (col == null) {
			return "";
		}

		String c = col.toLowerCase();

		if (c.contains("fecha")) {
			return "centrarTexto";
		}

		if (c.contains("monto") || c.contains("saldo") || c.contains("cuota") || c.contains("valor")
				|| c.contains("score") || c.contains("ingreso") || c.contains("tasa") || c.contains("capacidad")
				|| c.contains("clientespeorscore") || c.contains("ejey")) {
			return "numero";
		}

		return "";
	}

	public String formatCell(Object value, String col) {
		if (value == null) {
			return "";
		}

		if (value instanceof Map || value instanceof List) {
			return gson.toJson(value);
		}

		if (value instanceof String) {
			String s = ((String) value).trim();

			if (DATE_YYYY_MM_DD.matcher(s).matches()) {
				try {
					return OUT_DATE.format(IN_DATE.parse(s));
				} catch (Exception e) {
					return s;
				}
			}

			if (esColumnaTelefono(col)) {
				return normalizarTelefono(s);
			}

			// No formatear como número campo de identificacion o codigos
			if (esColumnaIdentificador(col)) {
				return s;
			}

			if (isNumericString(s)) {
				try {
					BigDecimal bd = new BigDecimal(s);
					return formatNumberByColumn(bd, col);
				} catch (Exception e) {
					return s;
				}
			}

			return s;
		}

		if (value instanceof Number) {
			if (esColumnaTelefono(col)) {
				return new BigDecimal(value.toString()).stripTrailingZeros().toPlainString();
			}

			if (esColumnaIdentificador(col)) {
				return String.valueOf(value);
			}

			BigDecimal bd = new BigDecimal(value.toString());
			return formatNumberByColumn(bd, col);
		}

		return String.valueOf(value);
	}

	private boolean esColumnaTelefono(String col) {
		if (col == null) {
			return false;
		}
		String c = col.toLowerCase();
		return c.contains("telefono") || c.contains("celular");
	}

	private String normalizarTelefono(String telefono) {
		if (telefono == null) {
			return "";
		}
		return telefono.replaceFirst("^(\\d+)\\.0+$", "$1");
	}

	private boolean esColumnaIdentificador(String col) {
		if (col == null) {
			return false;
		}

		String c = col.toLowerCase();

		return "identificacionsujeto".equals(c) || "identificacioncliente".equals(c) || "ruc".equals(c)
				|| c.contains("identificacion") || c.contains("cedula") || c.contains("documento")
				|| c.contains("codigo") || c.contains("usuario");
	}

	private String formatNumberByColumn(BigDecimal bd, String col) {
		String c = col == null ? "" : col.toLowerCase();

		if (c.contains("plazo") || c.contains("dias") || c.contains("eje") || "valor".equals(c)) {
			return new DecimalFormat("#,##0").format(bd);
		}

		return new DecimalFormat("#,##0.00").format(bd);
	}

	private boolean isNumericString(String s) {
		if (s == null || s.trim().isEmpty()) {
			return false;
		}
		return s.matches("^-?\\d+(\\.\\d+)?$");
	}

	/**
	 * Helpers para formulario XHTML
	 * 
	 * @param key
	 * @return
	 */
	public boolean tieneDatos(String key) {
		List<Map<String, Object>> rows = secciones.get(key);
		return rows != null && !rows.isEmpty();
	}

	public List<Map<String, Object>> getFilas(String key) {
		List<Map<String, Object>> rows = secciones.get(key);
		return rows != null ? rows : new ArrayList<Map<String, Object>>();
	}

	/**
	 * Agrega la accion de detalle al modelo dinamico de columnas de tarjetas.
	 */
	public List<String> getColumnasDetalleTarjetaSaldoVigente() {
		List<String> columnas = new ArrayList<String>();
		List<String> columnasSeccion = columnasPorSeccion.get(SECCION_DETALLE_TARJETA_SALDO_VIGENTE);
		if (columnasSeccion != null) {
			columnas.addAll(columnasSeccion);
		}
		columnas.add(COLUMNA_DETALLE_TARJETA);
		return columnas;
	}

	/**
	 * Identifica la columna que abre el dialogo con los movimientos de la tarjeta.
	 */
	public boolean isColumnaDetalleTarjeta(String columna) {
		return COLUMNA_DETALLE_TARJETA.equals(columna);
	}

	/**
	 * Resuelve la cabecera de las columnas de la tabla adicional de tarjetas.
	 */
	public String getLabelColumnaDetalleTarjeta(String columna) {
		return isColumnaDetalleTarjeta(columna) ? "Detalle" : labelColumna(columna);
	}

	public Map<String, Object> getPrimeraFila(String key) {
		List<Map<String, Object>> rows = getFilas(key);
		if (rows.isEmpty()) {
			return new LinkedHashMap<String, Object>();
		}
		return rows.get(0);
	}

	public Object getValor(String key, String columna) {
		Map<String, Object> fila = getPrimeraFila(key);
		return fila.get(columna);
	}

	public String getValorFormateado(String key, String columna) {
		Object value = getValor(key, columna);
		return formatCell(value, columna);
	}

	public String getPorcentaje(String key, String columna) {
		Object value = getValor(key, columna);
		if (value == null) {
			return "";
		}

		try {
			BigDecimal bd = new BigDecimal(value.toString());

			// Si viene 0.4 => 40%
			if (bd.compareTo(BigDecimal.ONE) <= 0) {
				bd = bd.multiply(new BigDecimal("100"));
			}

			return new DecimalFormat("#,##0.00").format(bd) + " %";
		} catch (Exception e) {
			return String.valueOf(value);
		}
	}

	/**
	 * Secciones restantes de formulario
	 */
	public List<String> getSeccionesRestantes() {
		List<String> fijas = Arrays.asList("identificacionTitular", "datosGeneralesEmpresa",
				SECCION_MODELO_CENTRIC_RESUMEN, SECCION_MODELO_CENTRIC_DETALLE, "tipoSujeto", "parametrosEntrada",
				"capacidadPago", "ofertaSugeridaCapacidadCalculada", "scoreFinancieroV2", "scoreEmpresa",
				"factoresScore", "informacionComoRUC", "evolucionScoreFinancieroV2", "semaforoMaximoDiasVencido",
				"tendenciaDeuda", "indicadoresDeuda", "datosContacto", "contactabilidad", "titularConsultado12Meses",
				"operacionesVigentesComercial", "operacionesVigentesEmpresa", SECCION_OPERACIONES_VIGENTES_TARJETA,
				SECCION_DETALLE_TARJETA_SALDO_VIGENTE, SECCION_DETALLE_TARJETA_CREDITO, "gastoFinanciero");
		List<String> restantes = new ArrayList<String>();
		for (String key : seccionesOrdenadas) {
			if (!fijas.contains(key) && tieneDatos(key)) {
				restantes.add(key);
			}
		}
		return restantes;
	}

	/**
	 * Selecciona una operacion vigente y obtiene las filas que componen el detalle
	 * de esa tarjeta.
	 */
	public void verDetalleTarjetaCredito(Map<String, Object> operacion) {
		this.operacionTarjetaSeleccionada = new LinkedHashMap<String, Object>();
		this.detalleTarjetaCreditoSeleccionado = new ArrayList<Map<String, Object>>();

		if (operacion == null) {
			return;
		}

		Map<String, Object> operacionReferencia = operacion;
		for (Map<String, Object> operacionVigente : getFilas(SECCION_OPERACIONES_VIGENTES_TARJETA)) {
			if (correspondeResumenTarjeta(operacion, operacionVigente)) {
				operacionReferencia = operacionVigente;
				break;
			}
		}
		this.operacionTarjetaSeleccionada = new LinkedHashMap<String, Object>(operacionReferencia);

		for (Map<String, Object> detalle : getFilas(SECCION_DETALLE_TARJETA_CREDITO)) {
			if (correspondeDetalleTarjeta(operacionReferencia, detalle)) {
				this.detalleTarjetaCreditoSeleccionado.add(detalle);
			}
		}
	}

	/**
	 * Relaciona la fila resumida visible con la operacion vigente completa.
	 */
	private boolean correspondeResumenTarjeta(Map<String, Object> resumen, Map<String, Object> operacion) {
		String entidadResumen = valorClaveTarjeta(resumen, "razonSocial", "nombreComercial");
		String entidadOperacion = valorClaveTarjeta(operacion, "razonSocial", "nombreComercial");
		String marcaResumen = valorClaveTarjeta(resumen, "marcaTarjetaDescripcion", "marcaTarjeta");
		String marcaOperacion = valorClaveTarjeta(operacion, "marcaTarjetaDescripcion", "marcaTarjeta");

		return !isBlank(entidadResumen) && entidadResumen.equalsIgnoreCase(entidadOperacion) && !isBlank(marcaResumen)
				&& marcaResumen.equalsIgnoreCase(marcaOperacion);
	}

	/**
	 * Verifica si una fila de detalle pertenece a la operacion seleccionada,
	 * comparando principalmente la institucion financiera y la marca.
	 */
	private boolean correspondeDetalleTarjeta(Map<String, Object> operacion, Map<String, Object> detalle) {
		if (detalle == null) {
			return false;
		}

		String institucionOperacion = valorClaveTarjeta(operacion, "codigoInstitucionFinanciera", "codigoInstitucion",
				"codigoEntidad");
		String institucionDetalle = valorClaveTarjeta(detalle, "codigoInstitucionFinanciera", "codigoInstitucion",
				"codigoEntidad");
		String marcaOperacion = valorClaveTarjeta(operacion, "marcaTarjeta");
		String marcaDetalle = valorClaveTarjeta(detalle, "marcaTarjeta");
		String descripcionMarcaOperacion = valorClaveTarjeta(operacion, "marcaTarjetaDescripcion");
		String descripcionMarcaDetalle = valorClaveTarjeta(detalle, "marcaTarjetaDescripcion");

		boolean tieneInstitucion = !isBlank(institucionOperacion) && !isBlank(institucionDetalle);
		boolean tieneMarca = !isBlank(marcaOperacion) && !isBlank(marcaDetalle);
		boolean tieneDescripcionMarca = !isBlank(descripcionMarcaOperacion) && !isBlank(descripcionMarcaDetalle);

		if (tieneInstitucion && !institucionOperacion.equalsIgnoreCase(institucionDetalle)) {
			return false;
		}
		if (tieneMarca && !marcaOperacion.equalsIgnoreCase(marcaDetalle)) {
			return false;
		}
		if (tieneDescripcionMarca && !descripcionMarcaOperacion.equalsIgnoreCase(descripcionMarcaDetalle)) {
			return false;
		}
		if (tieneInstitucion || tieneMarca || tieneDescripcionMarca) {
			return true;
		}

		String entidadOperacion = valorClaveTarjeta(operacion, "razonSocial", "nombreComercial");
		String entidadDetalle = valorClaveTarjeta(detalle, "razonSocial", "nombreComercial");
		return !isBlank(entidadOperacion) && entidadOperacion.equalsIgnoreCase(entidadDetalle);
	}

	/**
	 * Obtiene la primera clave disponible que permita relacionar la operacion
	 * vigente con su detalle.
	 */
	private String valorClaveTarjeta(Map<String, Object> fila, String... claves) {
		Object valor = primerValor(fila, claves);
		return valor == null ? "" : String.valueOf(valor).trim();
	}

	/**
	 * Suma un campo numerico de las filas mostradas en el detalle de la tarjeta.
	 */
	public String getTotalDetalleTarjeta(String campo) {
		BigDecimal total = BigDecimal.ZERO;
		for (Map<String, Object> fila : detalleTarjetaCreditoSeleccionado) {
			Object valor = fila.get(campo);
			if (valor != null) {
				try {
					total = total.add(new BigDecimal(String.valueOf(valor)));
				} catch (NumberFormatException e) {
					LOG.debug("Valor no numerico en detalle de tarjeta. campo=" + campo + ", valor=" + valor);
				}
			}
		}
		return new DecimalFormat("#,##0.00").format(total);
	}

	/**
	 * Retorna la operacion de tarjeta seleccionada desde la tabla principal.
	 */
	public Map<String, Object> getOperacionTarjetaSeleccionada() {
		return operacionTarjetaSeleccionada;
	}

	/**
	 * Retorna las filas de detalle asociadas a la tarjeta seleccionada.
	 */
	public List<Map<String, Object>> getDetalleTarjetaCreditoSeleccionado() {
		return detalleTarjetaCreditoSeleccionado;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> toMap(Object obj) {
		if (obj == null) {
			return null;
		}

		if (obj instanceof Map) {
			return (Map<String, Object>) obj;
		}

		try {
			String json = gson.toJson(obj);
			return gson.fromJson(json, Map.class);
		} catch (Exception e) {
			LOG.warn("No se pudo convertir result a Map: " + e.getMessage());
			return null;
		}
	}

	private boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	// Metodo Helper para validar campo creditRequestId
	private Long parseLong(String valor) {
		if (isBlank(valor)) {
			return null;
		}
		try {
			return Long.valueOf(valor.trim());
		} catch (NumberFormatException e) {
			LOG.warn("Parametro numerico invalido: " + valor, e);
			return null;
		}
	}

	private boolean validarParametrosBusqueda() {
		boolean valido = true;

		if (isBlank(tipoCliente)) {
			addWarnMessage("Advertencia", "Debe seleccionar el Tipo Identificación.");
			valido = false;
		}

		if (isBlank(identificacionCliente)) {
			addWarnMessage("Advertencia", "Debe ingresar la Identificación.");
			valido = false;
		} else if (identificacionCliente.trim().length() > LONGITUD_MAXIMA_IDENTIFICACION) {
			addWarnMessage("Advertencia", "La Identificación debe tener máximo 20 caracteres.");
			valido = false;
		}

		if (ingresos == null) {
			addWarnMessage("Advertencia", "Debe ingresar los Ingresos.");
			valido = false;
		} else {
			String ingresosEnteros = obtenerIngresosEnteros();

			if (ingresos.signum() < 0) {
				addWarnMessage("Advertencia", "Los Ingresos deben ser un valor positivo.");
				valido = false;
			} else if (ingresosEnteros == null) {
				addWarnMessage("Advertencia", "Los Ingresos solo aceptan números enteros sin decimales.");
				valido = false;
			} else if (ingresosEnteros.length() > LONGITUD_MAXIMA_INGRESOS) {
				addWarnMessage("Advertencia", "Los Ingresos deben tener máximo 7 dígitos.");
				valido = false;
			}
		}

		if (isBlank(tipoModelo)) {
			addWarnMessage("Advertencia", "Debe seleccionar el Tipo Modelo.");
			valido = false;
		}

		if (isBlank(marca)) {
			addWarnMessage("Advertencia", "Debe seleccionar la Marca.");
			valido = false;
		}

		return valido;
	}

	private String obtenerIngresosEnteros() {
		if (ingresos == null) {
			return null;
		}

		BigDecimal valor = ingresos.stripTrailingZeros();
		if (valor.scale() > 0) {
			return null;
		}

		return valor.toPlainString();
	}

	private String humanize(String s) {
		if (s == null) {
			return "";
		}
		String t = s.replace("_", " ");
		t = t.replaceAll("([a-z])([A-Z])", "$1 $2");
		t = t.trim();
		return t.isEmpty() ? "" : (t.substring(0, 1).toUpperCase() + t.substring(1));
	}

	public String getColumnStyle(String col) {
		if (col == null) {
			return "";
		}

		if ("reglaUsada".equals(col) || "reglaPadre".equals(col)) {
			return "min-width:220px;";
		}
		if ("estadoResultado".equals(col)) {
			return "min-width:180px;";
		}
		if ("fechaEvaluacionRegla".equals(col)) {
			return "min-width:120px; text-align:center;";
		}
		if ("valorObtenido".equals(col)) {
			return "min-width:100px; text-align:right;";
		}
		return "";
	}

	/********************************
	 * Metodos para generar graficas
	 ********************************/
	public String getEvolucionScoreChartJson() {
		if (evolucionScoreChartJson != null) {
			return evolucionScoreChartJson;
		}

		List<Map<String, Object>> rows = getFilas("evolucionScoreFinancieroV2");
		List<List<Object>> data = new ArrayList<List<Object>>();

		for (Map<String, Object> row : rows) {
			String fecha = row.get("fechaCorte") != null ? String.valueOf(row.get("fechaCorte")) : "";
			String fechaLabel = fecha;

			try {
				fechaLabel = CHART_OUT_DATE.format(CHART_IN_DATE.parse(fecha)).toLowerCase();
			} catch (Exception e) {
				// si falla, deja la fecha como venga
			}

			BigDecimal score = BigDecimal.ZERO;
			if (row.get("ejeY") != null) {
				try {
					score = new BigDecimal(String.valueOf(row.get("ejeY")));
				} catch (Exception e) {
					score = BigDecimal.ZERO;
				}
			}

			String annotation = score.stripTrailingZeros().scale() <= 0 ? String.valueOf(score.intValue())
					: score.toPlainString();

			List<Object> item = new ArrayList<Object>();
			item.add(fechaLabel);
			item.add(score.doubleValue());
			item.add(annotation);

			data.add(item);
		}

		evolucionScoreChartJson = gson.toJson(data);
		return evolucionScoreChartJson;
	}

	public boolean isMostrarGraficaEvolucionScore() {
		return tieneDatos("evolucionScoreFinancieroV2");
	}

	public void setEvolucionScoreChartJson(String evolucionScoreChartJson) {
		this.evolucionScoreChartJson = evolucionScoreChartJson;
	}

	public Integer getEvolucionScoreMin() {
		if (evolucionScoreMin != null) {
			return evolucionScoreMin;
		}

		List<Map<String, Object>> rows = getFilas("evolucionScoreFinancieroV2");
		if (rows == null || rows.isEmpty()) {
			evolucionScoreMin = 0;
			return evolucionScoreMin;
		}

		BigDecimal min = null;
		for (Map<String, Object> row : rows) {
			if (row.get("ejeY") != null) {
				try {
					BigDecimal val = new BigDecimal(String.valueOf(row.get("ejeY")));
					if (min == null || val.compareTo(min) < 0) {
						min = val;
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}

		if (min == null) {
			evolucionScoreMin = 0;
			return evolucionScoreMin;
		}

		int margen = obtenerMargenEvolucionScore();
		int value = min.intValue() - margen;
		evolucionScoreMin = value < 0 ? 0 : value;
		return evolucionScoreMin;
	}

	public void setEvolucionScoreMin(Integer evolucionScoreMin) {
		this.evolucionScoreMin = evolucionScoreMin;
	}

	public Integer getEvolucionScoreMax() {
		if (evolucionScoreMax != null) {
			return evolucionScoreMax;
		}

		List<Map<String, Object>> rows = getFilas("evolucionScoreFinancieroV2");
		if (rows == null || rows.isEmpty()) {
			evolucionScoreMax = 1000;
			return evolucionScoreMax;
		}

		BigDecimal max = null;
		for (Map<String, Object> row : rows) {
			if (row.get("ejeY") != null) {
				try {
					BigDecimal val = new BigDecimal(String.valueOf(row.get("ejeY")));
					if (max == null || val.compareTo(max) > 0) {
						max = val;
					}
				} catch (Exception e) {
					// ignore
				}
			}
		}

		if (max == null) {
			evolucionScoreMax = 1000;
			return evolucionScoreMax;
		}

		evolucionScoreMax = max.intValue() + obtenerMargenEvolucionScore();
		return evolucionScoreMax;
	}

	public void setEvolucionScoreMax(Integer evolucionScoreMax) {
		this.evolucionScoreMax = evolucionScoreMax;
	}

	/**
	 * Metodo para obtener el margen de la seccion evolucion score
	 */
	private int obtenerMargenEvolucionScore() {
		BigDecimal min = null;
		BigDecimal max = null;
		for (Map<String, Object> row : getFilas("evolucionScoreFinancieroV2")) {
			if (row.get("ejeY") == null) {
				continue;
			}
			try {
				BigDecimal valor = new BigDecimal(String.valueOf(row.get("ejeY")));
				min = min == null || valor.compareTo(min) < 0 ? valor : min;
				max = max == null || valor.compareTo(max) > 0 ? valor : max;
			} catch (Exception e) {
				// Ignora valores no numericos.
			}
		}
		if (min == null || max == null) {
			return 10;
		}
		int rango = max.subtract(min).abs().intValue();
		return Math.max(2, (int) Math.ceil(rango * 0.15d));
	}

	/**
	 * Metodo para obtener chart de Json de semaforo
	 */
	public String getSemaforoChartJson() {
		if (semaforoChartJson != null) {
			return semaforoChartJson;
		}

		List<Map<String, Object>> rows = getFilas("semaforoMaximoDiasVencido");
		List<List<Object>> data = new ArrayList<List<Object>>();

		for (Map<String, Object> row : rows) {
			String fecha = row.get("fechaCorte") != null ? String.valueOf(row.get("fechaCorte")) : "";
			String fechaLabel = fecha;

			try {
				fechaLabel = CHART_OUT_DATE.format(CHART_IN_DATE.parse(fecha)).toLowerCase();
			} catch (Exception e) {
				// deja fecha original
			}

			String diasVencido = row.get("diasVencido") != null ? String.valueOf(row.get("diasVencido")) : "";
			double nivelSemaforo = obtenerNivelSemaforo(diasVencido);
			String color = row.get("color") != null ? String.valueOf(row.get("color")) : "#00FF00";

			int radio = 10;
			if (row.get("radio") != null) {
				try {
					radio = new BigDecimal(String.valueOf(row.get("radio"))).intValue();
				} catch (Exception e) {
					radio = 10;
				}
			}

			String tooltip = "Fecha: " + fechaLabel + " - Días vencido: " + diasVencido;

			List<Object> item = new ArrayList<Object>();
			item.add(fechaLabel);
			item.add(nivelSemaforo);
			item.add("point { size: " + radio + "; fill-color: " + color + "; stroke-color: " + color + "; }");
			item.add(tooltip);

			data.add(item);
		}

		semaforoChartJson = gson.toJson(data);
		return semaforoChartJson;
	}

	/**
	 * Metodo para obtener nivel de semaforo
	 */
	private double obtenerNivelSemaforo(String diasVencido) {
		if (isBlank(diasVencido) || "CC".equalsIgnoreCase(diasVencido.trim())) {
			return 1d;
		}
		try {
			int dias = new BigDecimal(diasVencido.trim()).intValue();
			if (dias <= 0) {
				return 2d;
			}
			return dias < 60 ? 3d : 4d;
		} catch (Exception e) {
			return 1d;
		}
	}

	public String getTendenciaDeudaChartJson() {
		if (tendenciaDeudaChartJson != null) {
			return tendenciaDeudaChartJson;
		}

		List<Map<String, Object>> rows = getFilas("tendenciaDeuda");
		List<List<Object>> data = new ArrayList<List<Object>>();

		for (Map<String, Object> row : rows) {
			String fecha = row.get("fechaCorte") != null ? String.valueOf(row.get("fechaCorte")) : "";
			String fechaLabel = fecha;

			try {
				fechaLabel = CHART_OUT_DATE.format(CHART_IN_DATE.parse(fecha)).toLowerCase();
			} catch (Exception e) {
				// deja fecha original
			}

			double totalDeuda = toDouble(row.get("totalDeuda"));
			double valorVencidoTotal = toDouble(row.get("valorVencidoTotal"));

			List<Object> item = new ArrayList<Object>();
			item.add(fechaLabel);
			item.add(totalDeuda);
			item.add(valorVencidoTotal);

			data.add(item);
		}

		tendenciaDeudaChartJson = gson.toJson(data);
		return tendenciaDeudaChartJson;
	}

	public boolean isMostrarGraficaTendenciaDeuda() {
		return tieneDatos("tendenciaDeuda");
	}

	private double toDouble(Object value) {
		if (value == null) {
			return 0d;
		}
		try {
			return new BigDecimal(String.valueOf(value)).doubleValue();
		} catch (Exception e) {
			return 0d;
		}
	}

	public boolean isMostrarGraficaSemaforo() {
		return tieneDatos("semaforoMaximoDiasVencido");
	}

	public String getScoreActual() {
		Object value = getValor("scoreFinancieroV2", "score");
		if (value == null) {
			return "0";
		}
		try {
			return new BigDecimal(String.valueOf(value)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
		} catch (Exception e) {
			return String.valueOf(value);
		}
	}

	public String getScoreMarkerStyle() {
		Object value = getValor("scoreFinancieroV2", "score");
		if (value == null) {
			return "left:0%;";
		}
		try {
			BigDecimal score = new BigDecimal(String.valueOf(value));
			BigDecimal min = new BigDecimal("1");
			BigDecimal max = new BigDecimal("999");

			if (score.compareTo(min) < 0) {
				score = min;
			}
			if (score.compareTo(max) > 0) {
				score = max;
			}

			BigDecimal percent = score.subtract(min).multiply(new BigDecimal("100")).divide(max.subtract(min), 2,
					BigDecimal.ROUND_HALF_UP);

			return "left:" + percent.toPlainString() + "%;";
		} catch (Exception e) {
			return "left:0%;";
		}
	}

	public String getTextoProbabilidadMora() {
		String score = getScoreActual();
		String prob = getPorcentaje("scoreFinancieroV2", "tasaMalos");
		String sujeto = isPersonaJuridicaConsulta() ? "empresa" : "persona";
		return "Una " + sujeto + " con puntaje de " + score + " tiene una probabilidad de " + prob
				+ " de caer en mora.";
	}

	public String getClaseEfecto(Object efecto) {
		if (efecto == null) {
			return "efecto-neutro";
		}

		String v = String.valueOf(efecto).trim();
		if ("+".equals(v)) {
			return "efecto-positivo";
		}
		if ("-".equals(v)) {
			return "efecto-negativo";
		}
		return "efecto-neutro";
	}

	private String formatCurrency(BigDecimal value) {
		if (value == null) {
			value = BigDecimal.ZERO;
		}
		return "$" + new DecimalFormat("#,##0.00").format(value);
	}

	private BigDecimal sumarCampo(String seccion, String campo) {
		List<Map<String, Object>> rows = getFilas(seccion);
		BigDecimal total = BigDecimal.ZERO;

		for (Map<String, Object> row : rows) {
			Object v = row.get(campo);
			if (v != null) {
				try {
					total = total.add(new BigDecimal(String.valueOf(v)));
				} catch (Exception e) {
					// ignora
				}
			}
		}
		return total;
	}

	public void cargarMarcasPorTipoModelo() {
		marca = null;
		listaMarcas = new ArrayList<SelectItemDto>();

		if (tipoModelo == null || tipoModelo.trim().isEmpty()) {
			return;
		}

		switch (tipoModelo) {
		case "nuevo":
			cargarMarcasNuevo();
			break;
		case "semiNuevo":
			listaMarcas.add(new SelectItemDto("-", "-"));
			break;
		default:
			break;
		}
	}

	private void cargarMarcasNuevo() {
		listaMarcas.add(new SelectItemDto("changan", "CHANGAN"));
		listaMarcas.add(new SelectItemDto("opel", "OPEL"));
		listaMarcas.add(new SelectItemDto("peugeot", "PEUGEOT"));
		listaMarcas.add(new SelectItemDto("suzuki", "SUZUKI"));
		listaMarcas.add(new SelectItemDto("toyota", "TOYOTA"));
	}

	public String getTipoDecisionResumen() {
		return getValorFormateado(SECCION_MODELO_CENTRIC_RESUMEN, "tipoDecision");
	}

	public String getDynamicColumnClass(String col) {
		if (col == null) {
			return "";
		}

		String c = col.toLowerCase();

		if (c.contains("fecha")) {
			return "col-fecha";
		}

		if (c.contains("monto") || c.contains("saldo") || c.contains("cuota") || c.contains("valor")
				|| c.contains("score") || c.contains("ingreso") || c.contains("tasa") || c.contains("capacidad")
				|| c.contains("dias") || c.contains("interes") || c.contains("capital")) {
			return "col-numero";
		}

		if (c.contains("descripcion") || c.contains("observacion") || c.contains("institucion") || c.contains("tipo")
				|| c.contains("nombre") || c.contains("direccion") || c.contains("estado") || c.contains("segmento")) {
			return "col-texto-medio";
		}

		if (c.contains("detalle") || c.contains("regla") || c.contains("comentario")) {
			return "col-texto-largo";
		}

		return "";
	}

	public String getDynamicColumnStyle(String col) {
		if (col == null) {
			return "min-width:120px;";
		}

		String c = col.toLowerCase();

		if (c.contains("fecha")) {
			return "min-width:110px; text-align:center;";
		}

		if (c.contains("monto") || c.contains("saldo") || c.contains("cuota") || c.contains("valor")
				|| c.contains("score") || c.contains("ingreso") || c.contains("tasa") || c.contains("capacidad")
				|| c.contains("dias") || c.contains("interes") || c.contains("capital")) {
			return "min-width:95px; text-align:right;";
		}

		if (c.contains("descripcion") || c.contains("observacion") || c.contains("institucion") || c.contains("tipo")
				|| c.contains("nombre") || c.contains("direccion") || c.contains("estado") || c.contains("segmento")) {
			return "min-width:160px;";
		}

		if (c.contains("detalle") || c.contains("regla") || c.contains("comentario")) {
			return "min-width:220px;";
		}

		return "min-width:120px;";
	}

	public String getTipoDecisionLabel() {
		String valor = getTipoDecisionResumen();

		if (POSITIVO.equalsIgnoreCase(valor)) {
			return LABEL_PREAPROBADO;
		} else if (NEGATIVO.equalsIgnoreCase(valor)) {
			return LABEL_NEGADO;
		} else if (valor != null && !valor.isEmpty()) {
			return LABEL_ANALISIS;
		}
		return "-";
	}

	public String getTipoDecisionCss() {
		String valor = getTipoDecisionResumen();

		if (POSITIVO.equalsIgnoreCase(valor)) {
			return "badge-decision badge-positivo";
		} else if (NEGATIVO.equalsIgnoreCase(valor)) {
			return "badge-decision badge-negativo";
		} else if (valor != null && !valor.isEmpty()) {
			return "badge-decision badge-warning";
		}
		return "";
	}

	public String getTipoDecisionIcon() {
		String valor = getTipoDecisionResumen();

		if (POSITIVO.equalsIgnoreCase(valor)) {
			return "/common/imgs/icons/aprobar.png";
		} else if (NEGATIVO.equalsIgnoreCase(valor)) {
			return "/common/imgs/icons/negar.png";
		} else if (valor != null && !valor.isEmpty()) {
			return "/common/imgs/icons/warning.png";
		}
		return null;
	}

	/***********************************
	 * Implementar Control de Consultas
	 ***********************************/

	private boolean existeHistorialCrediticioPorCedula() {
		this.existe = false;
		this.existeCedula = null;
		this.fechaIniPantalla = null;
		this.fechaFinPantalla = null;

		try {
			existeCedula = cxcAvalBuroService.consultarJsonAvalBuroV2(identificacionCliente);
			if (existeCedula != null && existeCedula.tieneJsonV2()) {
				fechaIniPantalla = existeCedula.getFechaInicio();
				fechaFinPantalla = existeCedula.getFechaFin();
				this.existe = true;
			} else {
				mensajeNuevaBusqueda = "Es la primera vez que se consulta al cliente";
			}
		} catch (FindException e) {
			LOG.error("Error consultando historial de aval buró para la cédula: " + identificacionCliente, e);
		}

		return this.existe;
	}

	private void procesarRespuestaModeloCentric(ConsultaResponse resp) {
		String code = resp != null ? resp.getResponseCode() : null;
		String msg = resp != null ? resp.getMessage() : null;
		String normalizedCode = AvalBuroResponseUtils.getNormalizedCode(resp);

		LOG.debug("ModeloCentric statusRaw=" + code + " statusNorm=" + normalizedCode + " msg=" + msg);

		if (!AvalBuroResponseUtils.isSuccessResponse(resp)) {
			this.mostrarPantallasConsulta = false;
			limpiarResultados();
			addWarnMessage("Servicio", "Respuesta: " + AvalBuroResponseUtils.buildMessage(resp));
			return;
		}

		if (resp.getResult() == null) {
			this.mostrarPantallasConsulta = false;
			limpiarResultados();
			addWarnMessage("Servicio", "La respuesta no contiene result.");
			return;
		}

		Map<String, Object> resultMap = toMap(resp.getResult());

		if (resultMap == null || resultMap.isEmpty()) {
			this.mostrarPantallasConsulta = false;
			limpiarResultados();
			addWarnMessage("Servicio", "No se pudo interpretar el contenido de result.");
			return;
		}

		this.tipoTramaResultado = detectarTipoTrama(resultMap);
		LOG.debug("Aval Buro Score tipoTrama=" + this.tipoTramaResultado + " seccionesResult=" + resultMap.keySet());

		this.buildSeccionesFromResult(resultMap);
		logColumnasSeccionesPrincipales();

		this.seccionesOrdenadas = filtrarSeccionesPorTipoPersona(
				ordenarSecciones(new ArrayList<String>(secciones.keySet())));
		this.mostrarPantallasConsulta = !this.seccionesOrdenadas.isEmpty();

		this.evolucionScoreChartJson = getEvolucionScoreChartJson();
		this.evolucionScoreMin = getEvolucionScoreMin();
		this.evolucionScoreMax = getEvolucionScoreMax();
		this.semaforoChartJson = getSemaforoChartJson();
		this.tendenciaDeudaChartJson = getTendenciaDeudaChartJson();
		this.gastoFinancieroChartJson = getGastoFinancieroChartJson();

		addInfoMessage("Info", msg);
	}

	public void cargarDiasValidosAvalBuro() {
		try {
			List<ParamDet> diasValidos = paramDetService.consultarPorCodigoCab(noCia, PARAM_DIAS_AVAL_BURO);

			if (diasValidos != null && !diasValidos.isEmpty() && diasValidos.get(0).getTexto2() != null
					&& !diasValidos.get(0).getTexto2().trim().isEmpty()) {
				numeroDiasValidoAvalBuro = Integer.parseInt(diasValidos.get(0).getTexto2().trim());
			} else {
				numeroDiasValidoAvalBuro = 0;
				LOG.warn("No se encontro parametro DIASAB para calcular vigencia Aval Buro.");
			}

			LOG.debug("DIAS VALIDOS DATA AVAL BURO: " + numeroDiasValidoAvalBuro);

		} catch (Exception e) {
			numeroDiasValidoAvalBuro = 0;
			LOG.error("Error cargando parametro DIASAB para calcular vigencia Aval Buro.", e);
		}
	}

	private Date obtenerFechaFinDatosValidos() {
		if (numeroDiasValidoAvalBuro <= 0) {
			cargarDiasValidosAvalBuro();
		}

		if (numeroDiasValidoAvalBuro <= 0) {
			throw new IllegalStateException("No se encontro un valor valido para el parametro DIASAB.");
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, numeroDiasValidoAvalBuro);
		return cal.getTime();
	}

	/**
	 * Metodo para guardar consulta del servicio web
	 * 
	 * @param resp
	 */
	private void guardarConsultaModeloCentric(ConsultaResponse resp) {
		try {
			Date fechaInicioDatosValidos = new Date();
			Date fechaFinDatosValidos = obtenerFechaFinDatosValidos();

			String nombreSujeto = obtenerNombreSujetoConsulta(resp);
			String jsonRespuesta = gson.toJson(resp);

			SimpleDateFormat sdfJson = new SimpleDateFormat("yyyy-MM-dd");
			CxcAvalBuroJson payload = new CxcAvalBuroJson();
			payload.setCedula(identificacionCliente);
			payload.setNombre(nombreSujeto);
			payload.setFechaInicio(sdfJson.format(fechaInicioDatosValidos));
			payload.setFechaFin(sdfJson.format(fechaFinDatosValidos));
			payload.setUsuario(getUsuario().getUsuario());
			payload.setJsonV2(jsonRespuesta);

			if (!guardarConsultaModeloCentricEnServicio(payload)) {
				throw new Exception("No fue posible guardar en el servicio remoto de Aval Buró.");
			}

			CxcAvalBuro registroPantalla = this.existeCedula != null ? this.existeCedula : new CxcAvalBuro();
			registroPantalla.setCedula(identificacionCliente);
			registroPantalla.setNombre(nombreSujeto);
			registroPantalla.setFechaInicio(fechaInicioDatosValidos);
			registroPantalla.setFechaFin(fechaFinDatosValidos);
			registroPantalla.setUsuario(getUsuario().getUsuario());
			registroPantalla.setJsonV2(jsonRespuesta);

			this.existeCedula = registroPantalla;
			this.fechaIniPantalla = fechaInicioDatosValidos;
			this.fechaFinPantalla = fechaFinDatosValidos;
			this.nombreUsuarioConsulta = getUsuario().getUsuario();

			LOG.info("JSON_V2 guardado correctamente en servicio remoto para: " + identificacionCliente
					+ " | fechaInicio=" + fechaInicioDatosValidos + " | fechaFin=" + fechaFinDatosValidos);

		} catch (Exception e) {
			LOG.error("Error guardando JSON_V2 en servicio remoto de Aval Buró", e);
			addErrorMessage("Error", "No se pudo guardar la consulta en el servicio remoto: " + e.getMessage());
		}
	}

	/**
	 * Metodo para guardar informacion de Modelo Centric en servicio web interno
	 */
	private boolean guardarConsultaModeloCentricEnServicio(CxcAvalBuroJson payload) {
		HttpURLConnection connection = null;
		try {
			List<ParamDet> urlWSlocalGuardar = paramDetService.buscarTiposIdentificacion(noCia, "URLWSA");
			if (urlWSlocalGuardar == null || urlWSlocalGuardar.isEmpty()
					|| isBlank(urlWSlocalGuardar.get(0).getTexto1())) {
				LOG.warn("No se encontró parámetro URLWSA para guardar score. noCia=" + noCia);
				return false;
			}

			String baseUrl = urlWSlocalGuardar.get(0).getTexto1().trim();
			if (!baseUrl.endsWith("/")) {
				baseUrl += "/";
			}

			String urlGuardar = baseUrl + "guardarclienteavalburo";
			LOG.debug("Guardando score en WS local.");

			connection = (HttpURLConnection) new URL(urlGuardar).openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setRequestProperty("Accept", "application/json");

			String jsonBody = gson.toJson(payload);
			try (OutputStream datosEntrada = connection.getOutputStream()) {
				datosEntrada.write(jsonBody.getBytes("UTF-8"));
				datosEntrada.flush();
			}

			int responseCode = connection.getResponseCode();
			LOG.debug("guardarclienteavalburo responseCode=" + responseCode);

			BufferedReader reader = null;
			if (responseCode >= 200 && responseCode < 300) {
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			} else if (connection.getErrorStream() != null) {
				reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}

			if (reader != null) {
				try (BufferedReader r = reader) {
					StringBuilder body = new StringBuilder();
					String line;
					while ((line = r.readLine()) != null) {
						body.append(line);
					}
					LOG.debug("guardarclienteavalburo responseBody=" + body.toString());
				}
			}

			return responseCode >= 200 && responseCode < 300;
		} catch (Exception e) {
			LOG.error("Error consumiendo guardarclienteavalburo para score", e);
			return false;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String obtenerNombreSujetoConsulta(ConsultaResponse resp) {
		try {
			if (resp == null || resp.getResult() == null) {
				return identificacionCliente;
			}

			Map<String, Object> resultMap = toMap(resp.getResult());
			if (resultMap == null || resultMap.isEmpty()) {
				return identificacionCliente;
			}

			Object identificacionTitularObj = resultMap.get("identificacionTitular");
			if (identificacionTitularObj instanceof List) {
				List<?> lista = (List<?>) identificacionTitularObj;
				if (!lista.isEmpty() && lista.get(0) instanceof Map) {
					Object nombre = ((Map<?, ?>) lista.get(0)).get("nombreRazonSocial");
					if (nombre != null && !String.valueOf(nombre).trim().isEmpty()) {
						return String.valueOf(nombre).trim();
					}
				}
			}

			Object datosEmpresaObj = resultMap.get("datosGeneralesEmpresa");
			if (datosEmpresaObj instanceof List) {
				List<?> lista = (List<?>) datosEmpresaObj;
				if (!lista.isEmpty() && lista.get(0) instanceof Map) {
					Object nombre = ((Map<?, ?>) lista.get(0)).get("nombreRazonSocial");
					if (nombre != null && !String.valueOf(nombre).trim().isEmpty()) {
						return String.valueOf(nombre).trim();
					}
				}
			}

		} catch (Exception e) {
			LOG.warn("No se pudo obtener nombre del sujeto desde result: " + e.getMessage());
		}

		return identificacionCliente;
	}

	private void mostrarMensajeErrorServicio(String code, String msg) {
		if ("A401".equals(code)) {
			addErrorMessage("Aval Buro", "No se envió la cabecera de autorización");
		} else if ("A402".equals(code)) {
			addErrorMessage("Aval Buro", "El usuario/clave ingresados son incorrectos");
		} else if ("A410".equals(code)) {
			addErrorMessage("Aval Buro",
					"Se debe enviar la identificación y el tipo de identificación en el formato requerido para realizar la consulta");
		} else if ("A405".equals(code)) {
			addErrorMessage("Aval Buro", "No se encuentra información crediticia en la base de Aval Buró");
		} else if ("A406".equals(code)) {
			addErrorMessage("Aval Buro", "Cliente al que pertenece no se encuentra activo");
		} else if ("A407".equals(code)) {
			addErrorMessage("Aval Buro", "El producto no se encuentra activo");
		} else if ("A408".equals(code)) {
			addErrorMessage("Aval Buro",
					"No está autorizado para usar este producto o su contrato/plan no está vigente");
		} else {
			addWarnMessage("Servicio", "Respuesta: " + (msg != null ? msg : "No fue posible consultar el servicio"));
		}
	}

	private boolean tieneJsonV2Disponible(CxcAvalBuro registro) {
		return registro != null && registro.getJsonV2() != null && !registro.getJsonV2().trim().isEmpty()
				&& !"(null)".equalsIgnoreCase(registro.getJsonV2().trim());
	}

	private boolean registroVigente(CxcAvalBuro registro) {
		if (registro == null || registro.getFechaFin() == null) {
			return false;
		}
		Date hoy = new Date();
		return !registro.getFechaFin().before(hoy);
	}

	// Metodos para mostrar chart de Gastos Financieros
	public String getGastoFinancieroChartJson() {
		if (gastoFinancieroChartJson != null) {
			return gastoFinancieroChartJson;
		}

		List<List<Object>> data = new ArrayList<List<Object>>();

		addGastoChartItem(data, "Operaciones", getValor("gastoFinanciero", "cuotaTotalOperaciones"));
		addGastoChartItem(data, "Tarjetas", getValor("gastoFinanciero", "cuotaTotalTarjeta"));
		addGastoChartItem(data, "Servicios", getValor("gastoFinanciero", "cuotaTotalServicios"));
		addGastoChartItem(data, "Vencidos", getValor("gastoFinanciero", "cuotaVencidos"));

		gastoFinancieroChartJson = gson.toJson(data);
		return gastoFinancieroChartJson;
	}

	private void addGastoChartItem(List<List<Object>> data, String label, Object value) {
		double monto = toDouble(value);

		if (monto > 0) {
			List<Object> item = new ArrayList<Object>();
			item.add(label);
			item.add(monto);
			data.add(item);
		}
	}

	public boolean isMostrarGraficaGastoFinanciero() {
		return tieneDatos("gastoFinanciero") && getGastoFinancieroChartJson() != null
				&& !"[]".equals(getGastoFinancieroChartJson());
	}

	public String getCuotaMensualTotalGasto() {
		return formatCurrency(BigDecimal.valueOf(toDouble(getValor("gastoFinanciero", "cuotaEstimadaTitular"))));
	}

	public String getCuotaOperacionesGasto() {
		return formatCurrency(BigDecimal.valueOf(toDouble(getValor("gastoFinanciero", "cuotaTotalOperaciones"))));
	}

	public String getCuotaTarjetaGasto() {
		return formatCurrency(BigDecimal.valueOf(toDouble(getValor("gastoFinanciero", "cuotaTotalTarjeta"))));
	}

	public String getCuotaServiciosGasto() {
		return formatCurrency(BigDecimal.valueOf(toDouble(getValor("gastoFinanciero", "cuotaTotalServicios"))));
	}

	public String getCuotaVencidosGasto() {
		return formatCurrency(BigDecimal.valueOf(toDouble(getValor("gastoFinanciero", "cuotaVencidos"))));
	}

	public String getPorcentajeGasto(String campo) {
		BigDecimal total = BigDecimal.valueOf(toDouble(getValor("gastoFinanciero", "cuotaEstimadaTitular")));

		if (total.compareTo(BigDecimal.ZERO) <= 0) {
			return "0%";
		}

		BigDecimal valor = BigDecimal.valueOf(toDouble(getValor("gastoFinanciero", campo)));
		BigDecimal porcentaje = valor.multiply(new BigDecimal("100")).divide(total, 2, BigDecimal.ROUND_HALF_UP)
				.stripTrailingZeros();

		return porcentaje.toPlainString() + "%";
	}

	public void setGastoFinancieroChartJson(String gastoFinancieroChartJson) {
		this.gastoFinancieroChartJson = gastoFinancieroChartJson;
	}

	/**
	 * Genera un PDF resumen desde la informacion cargada en JSON_V2.
	 */
	public void generarReporteScoreCrediticio() {
		try {
			if (!validarReporteScoreCrediticio()) {
				return;
			}

			descargarReporteScoreCrediticio(generarPdfReporteScoreCrediticio());
		} catch (IOException e) {
			LOG.error("Error generando reporte PDF Aval Buro", e);
			addErrorMessage("Error", "No fue posible generar el reporte PDF Aval Buro.");
		}
	}

	public void generarReporteScoreCrediticioSolicitud() {
		try {
			if (!validarReporteScoreCrediticio() || !validarParametrosSolicitudCredito()) {
				return;
			}

			this.generarFileCrediticioBloqueado = true;
			CxcCreditoPK creditoPK = new CxcCreditoPK(noCiaSolicitudCredito, solicitudCreditoNumero);
			byte[] pdf = generarPdfReporteScoreCrediticio();
			creditoDigitalService.registrarFileCrediticio(creditoPK, TIPO_CREDITO_CLIENTE,
					cedulaClienteSolicitudCredito, TIPO_DOCUMENTO_BURO_CREDITO, construirNombreArchivoReporteScore(),
					new ByteArrayInputStream(pdf), Long.valueOf(pdf.length),
					getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), getUsuario().getUsuario(),
					getCompania().getPathFileServer(), getCompania().getIdTributario());
			// Metodo para registrar URL de fabrica credito Nexum
			registrarUrlFabricaCreditoNexum(creditoPK, pdf);
			addInfoMessage("Info", "Reporte Aval Buro registrado en el File Crediticio.");
		} catch (IOException e) {
			LOG.error("Error generando reporte PDF Aval Buro", e);
			addErrorMessage("Error", "No fue posible generar el reporte PDF Aval Buro.");
		} catch (Exception e) {
			LOG.error("Error guardando reporte PDF Aval Buro en File Crediticio", e);
			addErrorMessage("Error", "No fue posible guardar el reporte PDF Aval Buro en el File Crediticio.");
		}
	}

	/**
	 * Metodo para obtener Url de Documento Buro Credito aplica solo para Nexumcorp
	 */
	private void registrarUrlFabricaCreditoNexum(CxcCreditoPK creditoPK, byte[] pdf) {
		if (!CommonConstants.NEXUMCORP.equals(noCiaSolicitudCredito)) {
			return;
		}

		File archivoTemporal = null;
		try {
			Long creditRequestId = obtenerCreditRequestId(creditoPK);
			if (creditRequestId == null) {
				LOG.warn("Nexum: creditRequestId nulo. No se consume WS externo para reporte Aval Buro.");
				addWarnMessage("Advertencia",
						"El reporte se guardó localmente, pero no se envió a Nexum porque la solicitud no tiene creditRequestId.");
				return;
			}

			Long financialInstitutionId = obtenerFinancialInstitutionId(noCiaSolicitudCredito);
			if (financialInstitutionId == null) {
				LOG.warn(
						"Nexum: falta financial_institution_id en UP_WS_CREDITO. No se consume WS externo para reporte Aval Buro.");
				addWarnMessage("Advertencia",
						"El reporte se guardó localmente, pero falta configurar financial_institution_id para Nexum.");
				return;
			}

			String nombreArchivo = construirNombreArchivoReporteScore();
			archivoTemporal = crearArchivoTemporalPdf(pdf);
			ServicioWebNexumCredito ws = new ServicioWebNexumCreditoImpl();
			SubirDocumentoBuroResponse resp = ws.subirDocumentoBuroCredito(noCiaSolicitudCredito, creditRequestId,
					financialInstitutionId, archivoTemporal, nombreArchivo, TIPO_DOC_API_APPROVAL);

			if (resp == null || !Boolean.TRUE.equals(resp.getSuccess())) {
				String msg = resp != null ? resp.getMessage() : "Sin respuesta del servicio";
				LOG.warn("Nexum: carga reporte Aval Buro no exitosa. " + msg);
				addWarnMessage("Advertencia",
						"El reporte se guardó localmente, pero Nexum no registró la URL externa. Detalle: " + msg);
				return;
			}

			String urlFabCredito = resp.obtenerUrlDocumento();
			if (isBlank(urlFabCredito)) {
				String batchId = resp.getData() != null ? resp.getData().getBatch_id() : null;
				LOG.warn("Nexum: respuesta exitosa sin URL final para reporte Aval Buro. batchId=" + batchId);
				addWarnMessage("Advertencia", "El reporte se guardó localmente, pero Nexum no devolvió URL final"
						+ (!isBlank(batchId) ? ". batch_id=" + batchId : "."));
				return;
			}

			creditoDigitalService.registrarUrlFileCrediticio(creditoPK, TIPO_CREDITO_CLIENTE,
					cedulaClienteSolicitudCredito, TIPO_DOCUMENTO_URL_FAB_CREDITO, urlFabCredito.trim(),
					MIME_TYPE_TEXT);
			LOG.debug("Nexum: URL externa de reporte Aval Buro guardada en CXC_CREDITO_DIGITAL.");
		} catch (Exception e) {
			LOG.error("Nexum: error cargando reporte Aval Buro a servicio externo", e);
			addWarnMessage("Advertencia",
					"El reporte se guardó localmente, pero ocurrió un error al registrar la URL externa en Nexum: "
							+ e.getMessage());
		} finally {
			if (archivoTemporal != null && archivoTemporal.exists() && !archivoTemporal.delete()) {
				LOG.warn("No se pudo eliminar archivo temporal: " + archivoTemporal.getAbsolutePath());
			}
		}
	}

	private Long obtenerCreditRequestId(CxcCreditoPK creditoPK) throws FindException {
		if (creditRequestIdSolicitudCredito != null) {
			return creditRequestIdSolicitudCredito;
		}
		CxcCredito credito = creditoService.findByPk(creditoPK);
		return credito != null ? credito.getCreditRequestId() : null;
	}

	private Long obtenerFinancialInstitutionId(String noCiaParametro) {
		List<SisParametrosWebServices> conf = sisParametrosWebServicesServiceLocal
				.obtenerParametrosWebServices(noCiaParametro, ConstantesNexumCredito.PARAMETRO_WS_CREDITO);
		String financialInstitutionId = getParam(conf, ConstantesNexumCredito.PARAM_FINANCIAL_INSTITUTION_ID);
		if (isBlank(financialInstitutionId)) {
			return null;
		}
		try {
			return Long.valueOf(financialInstitutionId.trim());
		} catch (NumberFormatException e) {
			LOG.warn("Nexum: no se pudo convertir financial_institution_id=" + financialInstitutionId, e);
			return null;
		}
	}

	private String getParam(List<SisParametrosWebServices> conf, String nombreEtiqueta) {
		if (conf == null) {
			return null;
		}
		for (SisParametrosWebServices parametro : conf) {
			if (parametro != null && nombreEtiqueta.equalsIgnoreCase(parametro.getNombreEtiqueta())) {
				return parametro.getValorEtiqueta();
			}
		}
		return null;
	}

	private File crearArchivoTemporalPdf(byte[] contenido) throws IOException {
		File temp = File.createTempFile("aval_buro_", ".pdf");
		try (FileOutputStream out = new FileOutputStream(temp)) {
			out.write(contenido);
			out.flush();
		}
		return temp;
	}

	private boolean validarReporteScoreCrediticio() {
		if (consultaResponse == null || consultaResponse.getResult() == null || !mostrarPantallasConsulta) {
			addWarnMessage("Advertencia", "Debe consultar el score crediticio antes de generar el PDF.");
			return false;
		}
		return true;
	}

	private boolean validarParametrosSolicitudCredito() {
		if (!solicitudCredito || solicitudCreditoNumero == null || isBlank(noCiaSolicitudCredito)
				|| isBlank(cedulaClienteSolicitudCredito)) {
			addWarnMessage("Advertencia",
					"No existen datos de solicitud de credito para guardar el reporte en el File Crediticio.");
			return false;
		}
		return true;
	}

	/**
	 * Metodo para generar reporte para ajuste de trama persona natural y juridica
	 */
	private byte[] generarPdfReporteScoreCrediticio() throws IOException {
		Map<String, Object> parametros = construirParametrosReporteScore();
		LOG.debug("POST Jasper Report Aval Buro: reportUnit=" + REPORTE_JASPER_AVAL_BURO + ", tipoTrama="
				+ parametros.get("TIPO_TRAMA") + ", parametros=" + parametros.size());
		return ejecutarReporteJasperServer(REPORTE_JASPER_AVAL_BURO, parametros);
	}

	private byte[] ejecutarReporteJasperServer(String reportDir, Map<String, Object> parametros) throws IOException {
		String urlServidorJasper = getCompania() != null ? getCompania().getUrlServidorJasper() : null;
		if (isBlank(urlServidorJasper)) {
			urlServidorJasper = CommonConstants.URL_JASPERSERVER;
		}

		Map<String, Object> parametrosJasper = new LinkedHashMap<String, Object>();
		parametrosJasper.put("p_no_cia", getCompania().getNoCia());
		parametrosJasper.put("p_usuario", getUsuario().getUsuario());
		parametrosJasper.put("p_centro", getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
		if (getUsuarioCentroBodegaConectado() != null) {
			parametrosJasper.put("p_bodega", getUsuarioCentroBodegaConectado().getUsuarioCentroBodegaPK().getBodega());
		}
		parametrosJasper.put("p_linea", getLineaPorModulo());
		if (parametros != null) {
			parametrosJasper.putAll(parametros);
		}

		String baseJasper = obtenerBaseJasperServer(urlServidorJasper);
		String usuario = decodeUrlValue(obtenerParametroUrl(urlServidorJasper, "j_username", "invitado"));
		String clave = decodeUrlValue(obtenerParametroUrl(urlServidorJasper, "j_password", "invitado"));
		String locale = decodeUrlValue(obtenerParametroUrl(urlServidorJasper, "userLocale", "es_ES"));
		String requestUrl = baseJasper + "/rest_v2/reportExecutions";
		String requestBody = construirSolicitudEjecucionJasper(reportDir, parametrosJasper, locale);

		RespuestaEjecucionJasper respuestaEjecucion = enviarSolicitudEjecucionJasper(requestUrl, requestBody, usuario,
				clave);
		String respuesta = respuestaEjecucion.getContenido();
		Map<?, ?> ejecucion = gson.fromJson(respuesta, Map.class);
		String estado = valorMapa(ejecucion, "status");
		String requestId = valorMapa(ejecucion, "requestId");
		String exportId = obtenerExportId(ejecucion);
		if (!"ready".equalsIgnoreCase(estado) || isBlank(requestId) || isBlank(exportId)) {
			throw new IOException("Jasper Server no completo el reporte. status=" + estado + ", detalle="
					+ limitarDetalle(respuesta));
		}

		String outputUrl = baseJasper + "/rest_v2/reportExecutions/" + encodePathSegment(requestId) + "/exports/"
				+ encodePathSegment(exportId) + "/outputResource";
		return descargarPdfJasperServer(outputUrl, usuario, clave, respuestaEjecucion.getCookies());
	}

	private String obtenerBaseJasperServer(String urlServidorJasper) {
		String base = urlServidorJasper;
		int indiceFlow = base.indexOf("/flow.html");
		if (indiceFlow >= 0) {
			base = base.substring(0, indiceFlow);
		}
		int indiceQuery = base.indexOf("?");
		if (indiceQuery >= 0) {
			base = base.substring(0, indiceQuery);
		}
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base;
	}

	private String obtenerParametroUrl(String url, String nombre, String valorDefault) {
		if (isBlank(url)) {
			return valorDefault;
		}
		int indiceQuery = url.indexOf("?");
		if (indiceQuery < 0 || indiceQuery == url.length() - 1) {
			return valorDefault;
		}
		String[] parametros = url.substring(indiceQuery + 1).split("&");
		for (String parametro : parametros) {
			int indiceValor = parametro.indexOf("=");
			if (indiceValor > 0 && nombre.equals(parametro.substring(0, indiceValor))) {
				return parametro.substring(indiceValor + 1);
			}
		}
		return valorDefault;
	}

	private String encodeUrlValue(String valor) throws IOException {
		return URLEncoder.encode(valor != null ? valor : "", "UTF-8");
	}

	private String decodeUrlValue(String valor) throws IOException {
		return URLDecoder.decode(valor != null ? valor : "", "UTF-8");
	}

	private String construirSolicitudEjecucionJasper(String reportDir, Map<String, Object> parametros, String locale) {
		StringBuilder xml = new StringBuilder(4096);
		xml.append("<reportExecutionRequest>");
		xml.append("<reportUnitUri>/reports").append(escapeXml(reportDir)).append("</reportUnitUri>");
		xml.append("<async>false</async>");
		xml.append("<freshData>true</freshData>");
		xml.append("<saveDataSnapshot>false</saveDataSnapshot>");
		xml.append("<outputFormat>pdf</outputFormat>");
		xml.append("<interactive>false</interactive>");
		xml.append("<ignorePagination>false</ignorePagination>");
		xml.append("<parameters>");
		agregarParametroXml(xml, "userLocale", locale);
		for (Map.Entry<String, Object> parametro : parametros.entrySet()) {
			agregarParametroXml(xml, parametro.getKey(), parametro.getValue());
		}
		xml.append("</parameters>");
		xml.append("</reportExecutionRequest>");
		return xml.toString();
	}

	private void agregarParametroXml(StringBuilder xml, String nombre, Object valor) {
		xml.append("<reportParameter name=\"").append(escapeXml(nombre)).append("\"><value>")
				.append(escapeXml(valor != null ? valor.toString() : "")).append("</value></reportParameter>");
	}

	private RespuestaEjecucionJasper enviarSolicitudEjecucionJasper(String requestUrl, String requestBody,
			String usuario, String clave) throws IOException {
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) new URL(requestUrl).openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", construirBasicAuth(usuario, clave));
			connection.setRequestProperty("Content-Type", "application/xml; charset=UTF-8");
			connection.setRequestProperty("Accept", "application/json");
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(120000);

			byte[] contenido = requestBody.getBytes("UTF-8");
			connection.setFixedLengthStreamingMode(contenido.length);
			try (OutputStream output = connection.getOutputStream()) {
				output.write(contenido);
			}

			int responseCode = connection.getResponseCode();
			String respuesta = leerRespuesta(connection,
					responseCode >= HttpURLConnection.HTTP_BAD_REQUEST ? connection.getErrorStream()
							: connection.getInputStream());
			if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
				throw new IOException("Jasper Server rechazo la ejecucion. responseCode=" + responseCode + ", detalle="
						+ limitarDetalle(respuesta));
			}
			return new RespuestaEjecucionJasper(respuesta, obtenerCookies(connection));
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private byte[] descargarPdfJasperServer(String urlReporte, String usuario, String clave, String cookies)
			throws IOException {
		HttpURLConnection connection = null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			connection = (HttpURLConnection) new URL(urlReporte).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", construirBasicAuth(usuario, clave));
			connection.setRequestProperty("Accept", "application/pdf");
			if (!isBlank(cookies)) {
				connection.setRequestProperty("Cookie", cookies);
			}
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(120000);

			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				String detalleError = leerRespuestaError(connection);
				throw new IOException("Jasper Server no devolvio PDF. responseCode=" + responseCode
						+ (!isBlank(detalleError) ? ", detalle=" + detalleError : ""));
			}

			try (InputStream inputStream = connection.getInputStream()) {
				byte[] data = new byte[4096];
				int read;
				while ((read = inputStream.read(data)) != -1) {
					buffer.write(data, 0, read);
				}
			}
			byte[] pdf = buffer.toByteArray();
			if (pdf.length < 4 || pdf[0] != '%' || pdf[1] != 'P' || pdf[2] != 'D' || pdf[3] != 'F') {
				throw new IOException("Jasper Server no devolvio un archivo PDF valido.");
			}
			return pdf;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String leerRespuestaError(HttpURLConnection connection) {
		InputStream errorStream = connection != null ? connection.getErrorStream() : null;
		if (errorStream == null) {
			return "";
		}
		StringBuilder detalle = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (detalle.length() > 0) {
					detalle.append(" ");
				}
				detalle.append(line);
			}
		} catch (IOException e) {
			LOG.warn("No fue posible leer el detalle de error de Jasper Server", e);
		}
		return detalle.toString();
	}

	private void descargarReporteScoreCrediticio(byte[] pdf) throws IOException {
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition",
				"attachment;filename=\"" + construirNombreArchivoReporteScore() + "\"");
		response.setContentLength(pdf.length);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(pdf.length);
		buffer.write(pdf);
		buffer.writeTo(response.getOutputStream());
		FacesContext.getCurrentInstance().responseComplete();
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	private Map<String, Object> construirParametrosReporteScore() throws IOException {
		Map<String, Object> parametros = new HashMap<String, Object>();
		String identificacionReporte = resolverIdentificacionReporte();
		String tipoTramaReporte = resolverTipoTramaReporte();
		parametros.put("NOMBRE_EMPRESA", valorSeguro(nombreEmpresa));
		parametros.put("USUARIO", valorSeguro(nombreUsuarioConsulta));
		parametros.put("FECHA_REPORTE", OUT_DATE.format(new Date()));
		parametros.put("TIPO_TRAMA", tipoTramaReporte);
		parametros.put("ES_PERSONA_JURIDICA", Boolean.valueOf(PERSONA_JURIDICA.equals(tipoTramaReporte)));
		parametros.put("NOMBRE_TITULAR", valorSeguro(getValorFormateado("identificacionTitular", "nombreRazonSocial")));
		parametros.put("IDENTIFICACION_TITULAR", identificacionReporte);
		parametros.put("TIPO_IDENTIFICACION",
				valorSeguro(getValorFormateado("identificacionTitular", "tipoIdentificacionSujetoDescripcion")));
		parametros.put("DECISION_MODELO",
				valorSeguro(getValorFormateado(SECCION_MODELO_CENTRIC_RESUMEN, "decisionModelo")));
		parametros.put("TIPO_DECISION", valorSeguro(getTipoDecisionLabel()));
		parametros.put("SCORE", valorSeguro(getValorFormateado("scoreFinancieroV2", "score")));
		parametros.put("EVOLUCION_SCORE_MIN", getEvolucionScoreMin());
		parametros.put("EVOLUCION_SCORE_MAX", getEvolucionScoreMax());
		parametros.put("CAPACIDAD_PAGO", valorSeguro(obtenerCapacidadPagoReporte()));
		parametros.put("CUOTA_ESTIMADA_MENSUAL",
				valorSeguro(getValorFormateado("capacidadPago", "cuotaEstimadaMensual")));
		parametros.put("INGRESO_DIGITADO", valorSeguro(getValorFormateado("parametrosEntrada", "ingresoDigitado")));
		parametros.put("VALIDADOR_INGRESO", valorSeguro(getValorFormateado("parametrosEntrada", "validadorIngreso")));
		parametros.put("RANGO_INGRESOS", valorSeguro(getValorFormateado("parametrosEntrada", "rangoIngresos")));
		parametros.put("CUOTA_ESTIMADA_TITULAR",
				valorSeguro(getValorFormateado("gastoFinanciero", "cuotaEstimadaTitular")));
		parametros.put("CUOTA_TOTAL_OPERACIONES",
				valorSeguro(getValorFormateado("gastoFinanciero", "cuotaTotalOperaciones")));
		parametros.put("CUOTA_TOTAL_TARJETA", valorSeguro(getValorFormateado("gastoFinanciero", "cuotaTotalTarjeta")));
		parametros.put("CUOTA_TOTAL_SERVICIOS",
				valorSeguro(getValorFormateado("gastoFinanciero", "cuotaTotalServicios")));
		parametros.put("CUOTA_VENCIDOS", valorSeguro(getValorFormateado("gastoFinanciero", "cuotaVencidos")));
		parametros.put("PORCENTAJE_OPERACIONES", getPorcentajeGasto("cuotaTotalOperaciones"));
		parametros.put("PORCENTAJE_TARJETAS", getPorcentajeGasto("cuotaTotalTarjeta"));
		parametros.put("PORCENTAJE_SERVICIOS", getPorcentajeGasto("cuotaTotalServicios"));
		parametros.put("PORCENTAJE_VENCIDOS", getPorcentajeGasto("cuotaVencidos"));
		agregarParametrosDetalleDeudaVigente(parametros);
		parametros.put("GASTO_FINANCIERO_JSON", comprimirJsonParaReporte(construirGastoFinancieroReporte()));
		agregarParametrosOfertaSugerida(parametros, construirOfertaSugeridaReporte());
		agregarParametrosDeudaVigente(parametros, construirDeudaVigenteReporte());
		agregarParametrosFactoresScore(parametros, construirFactoresScoreReporte());
		agregarParametrosDetalleNatural(parametros);
		agregarParametrosRiesgoReporte(parametros);
		agregarParametrosJuridicosReporte(parametros);
		agregarParametrosSeccionesReporte(parametros,
				isPersonaJuridicaConsulta() ? construirSeccionesReporteScoreJuridico()
						: Collections.<ReporteAvalBuroScoreSeccionDTO>emptyList());
		return parametros;
	}

	private List<Map<String, Object>> construirGastoFinancieroReporte() {
		List<Map<String, Object>> resultado = new ArrayList<Map<String, Object>>();
		agregarGastoFinancieroReporte(resultado, "Operaciones", getValor("gastoFinanciero", "cuotaTotalOperaciones"));
		agregarGastoFinancieroReporte(resultado, "Tarjetas", getValor("gastoFinanciero", "cuotaTotalTarjeta"));
		agregarGastoFinancieroReporte(resultado, "Servicios", getValor("gastoFinanciero", "cuotaTotalServicios"));
		agregarGastoFinancieroReporte(resultado, "Vencidos", getValor("gastoFinanciero", "cuotaVencidos"));
		return resultado;
	}

	private void agregarParametrosDetalleDeudaVigente(Map<String, Object> parametros) {
		agregarParametroDetalleDeuda(parametros, "BANCOS", "operacionesVigentesBanco", "banco");
		agregarParametroDetalleDeuda(parametros, "COOPERATIVAS", "operacionesVigentesCooperativa", "cooper");
		agregarParametroDetalleDeuda(parametros, "TARJETAS", SECCION_OPERACIONES_VIGENTES_TARJETA, "tarjet");
		agregarParametroDetalleDeuda(parametros, "COMERCIAL", "operacionesVigentesEmpresa", "comercial");
		agregarParametroDetalleDeuda(parametros, "SERVICIOS", "operacionesVigentesServicio", "servici");
		agregarParametroDetalleDeuda(parametros, "COBRANZA", "operacionesVigentesCobranza", "cobran");
	}

	private void agregarParametroDetalleDeuda(Map<String, Object> parametros, String categoria, String seccion,
			String... coincidencias) {
		parametros.put("DEUDA_" + categoria, obtenerMontoDeudaVigente(coincidencias));
		parametros.put("NUM_" + categoria, String.valueOf(getFilas(seccion).size()));
	}

	private String obtenerMontoDeudaVigente(String... coincidencias) {
		BigDecimal total = BigDecimal.ZERO;
		for (Map<String, Object> fila : getFilas("deudaVigenteTotal")) {
			String sistema = String.valueOf(fila.get("sistemaCrediticio")).toLowerCase(Locale.ROOT);
			if (contieneAlgunaCoincidencia(sistema, coincidencias)) {
				total = total.add(BigDecimal.valueOf(toDouble(fila.get("totalDeuda"))));
			}
		}
		return formatCurrency(total);
	}

	private boolean contieneAlgunaCoincidencia(String valor, String... coincidencias) {
		for (String coincidencia : coincidencias) {
			if (valor.contains(coincidencia)) {
				return true;
			}
		}
		return false;
	}

	private void agregarGastoFinancieroReporte(List<Map<String, Object>> resultado, String categoria, Object valor) {
		double monto = toDouble(valor);
		if (monto <= 0d) {
			return;
		}
		Map<String, Object> item = new LinkedHashMap<String, Object>();
		item.put("categoria", categoria);
		item.put("monto", Double.valueOf(monto));
		resultado.add(item);
	}

	private String obtenerCapacidadPagoReporte() {
		String capacidadPago = getValorFormateado("capacidadPago", "capacidadPago");
		if (!isBlank(capacidadPago) || !isPersonaJuridicaConsulta()) {
			return capacidadPago;
		}

		for (Map<String, Object> fila : getFilas("indicesFinancieros")) {
			Object tipoIndice = fila.get("tipoIndice");
			if (tipoIndice == null || !"Capacidad de pago".equalsIgnoreCase(String.valueOf(tipoIndice).trim())) {
				continue;
			}

			Object valor = primerValor(fila, "anioActualMenos1", "anioActualMenos2", "anioActualMenos3",
					"anioActualMenos4", "anioActualMenos5");
			return formatCell(valor, "capacidadPago");
		}

		return "";
	}

	private void agregarParametrosDetalleNatural(Map<String, Object> parametros) throws IOException {
		if (isPersonaJuridicaConsulta()) {
			parametros.put("MODELO_DETALLE_JSON", comprimirJsonParaReporte(construirModeloCentricDetalleReporte()));
			parametros.put("INFORMACION_RUC_JSON", "[]");
			parametros.put("CONTACTOS_JSON", "[]");
			parametros.put("CONSULTAS_12_MESES_JSON", comprimirJsonParaReporte(construirConsultas12MesesReporte()));
			parametros.put("SECCIONES_NATURAL_JSON", "[]");
			return;
		}

		parametros.put("MODELO_DETALLE_JSON", comprimirJsonParaReporte(construirModeloCentricDetalleReporte()));
		parametros.put("INFORMACION_RUC_JSON", comprimirJsonParaReporte(construirInformacionRucReporte()));
		parametros.put("CONTACTOS_JSON", comprimirJsonParaReporte(construirContactosReporte()));
		parametros.put("CONSULTAS_12_MESES_JSON", comprimirJsonParaReporte(construirConsultas12MesesReporte()));
		parametros.put("SECCIONES_NATURAL_JSON",
				comprimirJsonParaReporte(Collections.<ReporteAvalBuroScoreSeccionDTO>emptyList()));
	}

	private void agregarParametrosRiesgoReporte(Map<String, Object> parametros) throws IOException {
		List<SerieRiesgoReporteDTO> evolucionScore = construirSerieRiesgoReporte("evolucionScoreFinancieroV2", "ejeY",
				null);
		List<SerieRiesgoReporteDTO> tendenciaDeuda = construirSerieRiesgoReporte("tendenciaDeuda", "totalDeuda",
				"valorVencidoTotal");
		parametros.put("TARJETAS_VIGENTES_JSON", comprimirJsonParaReporte(
				construirTarjetasReporte(SECCION_OPERACIONES_VIGENTES_TARJETA, LIMITE_FILAS_REPORTE)));
		parametros.put("OPERACIONES_VIGENTES_JSON", comprimirJsonParaReporte(construirOperacionesVigentesReporte()));
		parametros.put("EVOLUCION_SCORE_JSON", comprimirJsonParaReporte(evolucionScore));
		parametros.put("EVOLUCION_SCORE_IMAGEN", construirGraficaEvolucionScore(evolucionScore));
		parametros.put("SEMAFORO_MORA_JSON",
				comprimirJsonParaReporte(construirSerieRiesgoReporte("semaforoMaximoDiasVencido", "ejeY", null)));
		parametros.put("TENDENCIA_DEUDA_JSON", comprimirJsonParaReporte(construirTendenciaDeudaJasper(tendenciaDeuda)));
		parametros.put("TENDENCIA_DEUDA_IMAGEN", construirGraficaTendenciaDeuda(tendenciaDeuda));
		parametros.put("INDICADORES_DEUDA_JSON", comprimirJsonParaReporte(construirIndicadoresDeudaReporte()));
		parametros.put("HISTORICO_TARJETAS_JSON",
				comprimirJsonParaReporte(construirTarjetasReporte("operacionesHistoricasTarjeta", 12)));
		parametros.put("HISTORICO_OPERACIONES_JSON",
				comprimirJsonParaReporte(construirOperacionesReporte("operacionesHistoricasBanco", 12)));
		parametros.put("HISTORICO_SERVICIOS_JSON", comprimirJsonParaReporte(construirServiciosHistoricosReporte()));
	}

	private String construirGraficaEvolucionScore(List<SerieRiesgoReporteDTO> puntos) throws IOException {
		if (puntos == null || puntos.isEmpty()) {
			return "";
		}

		final int ancho = 1600;
		final int alto = 430;
		final int izquierda = 90;
		final int derecha = 35;
		final int superior = 45;
		final int inferior = 75;
		final int anchoGrafica = ancho - izquierda - derecha;
		final int altoGrafica = alto - superior - inferior;
		int minimo = getEvolucionScoreMin();
		int maximo = getEvolucionScoreMax();
		if (maximo <= minimo) {
			maximo = minimo + 10;
		}

		BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D grafico = imagen.createGraphics();
		try {
			grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			grafico.setColor(Color.WHITE);
			grafico.fillRect(0, 0, ancho, alto);

			grafico.setFont(new Font("Arial", Font.PLAIN, 16));
			grafico.setStroke(new BasicStroke(1f));
			for (int i = 0; i <= 5; i++) {
				int y = superior + (altoGrafica * i / 5);
				int valor = maximo - ((maximo - minimo) * i / 5);
				grafico.setColor(new Color(220, 225, 230));
				grafico.drawLine(izquierda, y, ancho - derecha, y);
				grafico.setColor(new Color(55, 70, 85));
				grafico.drawString(String.valueOf(valor), 35, y + 6);
			}

			int cantidad = puntos.size();
			int[] posicionesX = new int[cantidad];
			int[] posicionesY = new int[cantidad];
			for (int i = 0; i < cantidad; i++) {
				SerieRiesgoReporteDTO punto = puntos.get(i);
				double valor = punto.getValorPrincipalNumerico() != null
						? punto.getValorPrincipalNumerico().doubleValue()
						: 0d;
				posicionesX[i] = izquierda + (cantidad == 1 ? anchoGrafica / 2 : (anchoGrafica * i / (cantidad - 1)));
				posicionesY[i] = superior + (int) Math.round((maximo - valor) * altoGrafica / (maximo - minimo));
			}

			grafico.setColor(new Color(245, 166, 35));
			grafico.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			for (int i = 1; i < cantidad; i++) {
				grafico.drawLine(posicionesX[i - 1], posicionesY[i - 1], posicionesX[i], posicionesY[i]);
			}

			grafico.setFont(new Font("Arial", Font.BOLD, 17));
			for (int i = 0; i < cantidad; i++) {
				SerieRiesgoReporteDTO punto = puntos.get(i);
				int x = posicionesX[i];
				int y = posicionesY[i];
				grafico.setColor(new Color(245, 166, 35));
				grafico.fillOval(x - 9, y - 9, 18, 18);
				grafico.setColor(Color.BLACK);
				String valor = punto.getValorPrincipalNumerico() == null ? ""
						: new DecimalFormat("0").format(punto.getValorPrincipalNumerico());
				grafico.drawString(valor, x - grafico.getFontMetrics().stringWidth(valor) / 2, y - 15);

				grafico.setFont(new Font("Arial", Font.PLAIN, 14));
				String fecha = punto.getFechaCorte() != null ? punto.getFechaCorte() : "";
				grafico.drawString(fecha, x - grafico.getFontMetrics().stringWidth(fecha) / 2, alto - inferior + 30);
				grafico.setFont(new Font("Arial", Font.BOLD, 17));
			}
		} finally {
			grafico.dispose();
		}

		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		ImageIO.write(imagen, "png", salida);
		return Base64.getEncoder().encodeToString(salida.toByteArray());
	}

	private List<Map<String, Object>> construirTendenciaDeudaJasper(List<SerieRiesgoReporteDTO> puntos) {
		List<Map<String, Object>> resultado = new ArrayList<Map<String, Object>>();
		if (puntos == null) {
			return resultado;
		}
		for (SerieRiesgoReporteDTO punto : puntos) {
			Map<String, Object> fila = new LinkedHashMap<String, Object>();
			fila.put("fechaCorte", punto.getFechaCorte());
			fila.put("valorPrincipalNumerico", Long.valueOf(redondearMontoGrafica(punto.getValorPrincipalNumerico())));
			fila.put("valorSecundarioNumerico",
					Long.valueOf(redondearMontoGrafica(punto.getValorSecundarioNumerico())));
			resultado.add(fila);
		}
		return resultado;
	}

	private long redondearMontoGrafica(Double valor) {
		return valor != null ? Math.round(valor.doubleValue()) : 0L;
	}

	private String construirGraficaTendenciaDeuda(List<SerieRiesgoReporteDTO> puntos) throws IOException {
		if (puntos == null || puntos.isEmpty()) {
			return "";
		}

		final int ancho = 1600;
		final int alto = 500;
		final int izquierda = 125;
		final int derecha = 35;
		final int superior = 75;
		final int inferior = 75;
		final int anchoGrafica = ancho - izquierda - derecha;
		final int altoGrafica = alto - superior - inferior;
		final Color colorDeuda = new Color(74, 144, 226);
		final Color colorVencido = new Color(91, 192, 248);

		double maximoValor = 0d;
		for (SerieRiesgoReporteDTO punto : puntos) {
			maximoValor = Math.max(maximoValor,
					punto.getValorPrincipalNumerico() != null ? punto.getValorPrincipalNumerico().doubleValue() : 0d);
			maximoValor = Math.max(maximoValor,
					punto.getValorSecundarioNumerico() != null ? punto.getValorSecundarioNumerico().doubleValue() : 0d);
		}
		double maximoEje = calcularMaximoEjeDeuda(maximoValor);

		BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D grafico = imagen.createGraphics();
		try {
			grafico.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			grafico.setColor(Color.WHITE);
			grafico.fillRect(0, 0, ancho, alto);

			grafico.setFont(new Font("Arial", Font.PLAIN, 16));
			grafico.setStroke(new BasicStroke(1f));
			DecimalFormat formatoEje = new DecimalFormat("#,##0");
			for (int i = 0; i <= 5; i++) {
				int y = superior + (altoGrafica * i / 5);
				double valor = maximoEje - (maximoEje * i / 5d);
				grafico.setColor(new Color(210, 218, 225));
				grafico.drawLine(izquierda, y, ancho - derecha, y);
				grafico.setColor(new Color(55, 70, 85));
				String etiqueta = formatoEje.format(valor);
				grafico.drawString(etiqueta, izquierda - 15 - grafico.getFontMetrics().stringWidth(etiqueta), y + 6);
			}

			int cantidad = puntos.size();
			int[] posicionesX = new int[cantidad];
			int[] posicionesDeuda = new int[cantidad];
			int[] posicionesVencido = new int[cantidad];
			for (int i = 0; i < cantidad; i++) {
				SerieRiesgoReporteDTO punto = puntos.get(i);
				double deuda = punto.getValorPrincipalNumerico() != null
						? punto.getValorPrincipalNumerico().doubleValue()
						: 0d;
				double vencido = punto.getValorSecundarioNumerico() != null
						? punto.getValorSecundarioNumerico().doubleValue()
						: 0d;
				posicionesX[i] = izquierda + (cantidad == 1 ? anchoGrafica / 2 : anchoGrafica * i / (cantidad - 1));
				posicionesDeuda[i] = superior + (int) Math.round((maximoEje - deuda) * altoGrafica / maximoEje);
				posicionesVencido[i] = superior + (int) Math.round((maximoEje - vencido) * altoGrafica / maximoEje);
			}

			dibujarSerieDeuda(grafico, posicionesX, posicionesDeuda, colorDeuda);
			dibujarSerieDeuda(grafico, posicionesX, posicionesVencido, colorVencido);

			grafico.setFont(new Font("Arial", Font.PLAIN, 14));
			for (int i = 0; i < cantidad; i++) {
				String fecha = puntos.get(i).getFechaCorte() != null ? puntos.get(i).getFechaCorte() : "";
				int x = posicionesX[i] - grafico.getFontMetrics().stringWidth(fecha) / 2;
				grafico.setColor(new Color(45, 55, 65));
				grafico.drawString(fecha, x, alto - inferior + 32);
			}

			grafico.setFont(new Font("Arial", Font.PLAIN, 17));
			int leyendaX = ancho / 2 - 175;
			grafico.setColor(colorDeuda);
			grafico.setStroke(new BasicStroke(4f));
			grafico.drawLine(leyendaX, 35, leyendaX + 35, 35);
			grafico.fillOval(leyendaX + 12, 28, 14, 14);
			grafico.setColor(Color.BLACK);
			grafico.drawString("Total Deuda", leyendaX + 45, 41);
			grafico.setColor(colorVencido);
			grafico.drawLine(leyendaX + 190, 35, leyendaX + 225, 35);
			grafico.fillOval(leyendaX + 202, 28, 14, 14);
			grafico.setColor(Color.BLACK);
			grafico.drawString("Valor Vencido Total", leyendaX + 235, 41);
		} finally {
			grafico.dispose();
		}

		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		ImageIO.write(imagen, "png", salida);
		return Base64.getEncoder().encodeToString(salida.toByteArray());
	}

	private double calcularMaximoEjeDeuda(double maximoValor) {
		if (maximoValor <= 0d) {
			return 100d;
		}
		double valorConMargen = maximoValor * 1.03d;
		double magnitud = Math.pow(10d, Math.floor(Math.log10(valorConMargen)));
		double paso = magnitud / 5d;
		return Math.ceil(valorConMargen / paso) * paso;
	}

	private void dibujarSerieDeuda(Graphics2D grafico, int[] posicionesX, int[] posicionesY, Color color) {
		grafico.setColor(color);
		grafico.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int i = 1; i < posicionesX.length; i++) {
			grafico.drawLine(posicionesX[i - 1], posicionesY[i - 1], posicionesX[i], posicionesY[i]);
		}
		for (int i = 0; i < posicionesX.length; i++) {
			grafico.fillOval(posicionesX[i] - 6, posicionesY[i] - 6, 12, 12);
		}
	}

	private void agregarParametrosJuridicosReporte(Map<String, Object> parametros) throws IOException {
		if (!isPersonaJuridicaConsulta()) {
			String jsonVacio = comprimirJsonParaReporte(Collections.emptyList());
			parametros.put("DETALLE_EMPRESA_JSON", jsonVacio);
			parametros.put("RESUMEN_FINANCIERO_JSON", jsonVacio);
			parametros.put("ESTADOS_FINANCIEROS_JSON", jsonVacio);
			parametros.put("INDICES_FINANCIEROS_JSON", jsonVacio);
			parametros.put("ACCIONISTAS_JSON", jsonVacio);
			parametros.put("REPRESENTANTES_JSON", jsonVacio);
			parametros.put("CONTACTABILIDAD_JSON", jsonVacio);
			return;
		}

		parametros.put("DETALLE_EMPRESA_JSON", comprimirJsonParaReporte(construirDetalleEmpresaReporte()));
		parametros.put("RESUMEN_FINANCIERO_JSON",
				comprimirJsonParaReporte(construirCuentasFinancierasReporte("resumenPrincipalesCuentasFinancieras")));
		parametros.put("ESTADOS_FINANCIEROS_JSON",
				comprimirJsonParaReporte(construirCuentasFinancierasReporte("cuentasEstadosFinancieros")));
		parametros.put("INDICES_FINANCIEROS_JSON", comprimirJsonParaReporte(construirIndicesFinancierosReporte()));
		parametros.put("ACCIONISTAS_JSON",
				comprimirJsonParaReporte(construirPersonasEmpresaReporte("principalesAccionistas", "participacion")));
		parametros.put("REPRESENTANTES_JSON",
				comprimirJsonParaReporte(construirPersonasEmpresaReporte("representantesLegales", "cargo")));
		parametros.put("CONTACTABILIDAD_JSON", comprimirJsonParaReporte(construirContactabilidadReporte()));
	}

	private void agregarParametrosSeccionesReporte(Map<String, Object> parametros,
			List<ReporteAvalBuroScoreSeccionDTO> seccionesReporte) {
		for (int i = 0; i < 10; i++) {
			ReporteAvalBuroScoreSeccionDTO seccion = seccionesReporte != null && seccionesReporte.size() > i
					? seccionesReporte.get(i)
					: null;
			int fila = i + 1;
			parametros.put("SECCION_TITULO_" + fila, seccion != null ? valorSeguro(seccion.getTitulo()) : "");
			parametros.put("SECCION_CONTENIDO_" + fila, seccion != null ? valorSeguro(seccion.getContenido()) : "");
		}
	}

	private void agregarParametrosOfertaSugerida(Map<String, Object> parametros,
			List<OfertaSugeridaReporteDTO> ofertasReporte) {
		for (int i = 0; i < 4; i++) {
			OfertaSugeridaReporteDTO oferta = ofertasReporte != null && ofertasReporte.size() > i
					? ofertasReporte.get(i)
					: null;
			int fila = i + 1;
			parametros.put("OFERTA_PLAZO_" + fila, oferta != null ? valorSeguro(oferta.getPlazo()) : "");
			parametros.put("OFERTA_MONTO_" + fila, oferta != null ? valorSeguro(oferta.getMontoSugerido()) : "");
			parametros.put("OFERTA_CUOTA_" + fila, oferta != null ? valorSeguro(oferta.getCuotaSugerida()) : "");
		}
	}

	private void agregarParametrosDeudaVigente(Map<String, Object> parametros,
			List<DeudaVigenteReporteDTO> deudasReporte) {
		for (int i = 0; i < 4; i++) {
			DeudaVigenteReporteDTO deuda = deudasReporte != null && deudasReporte.size() > i ? deudasReporte.get(i)
					: null;
			int fila = i + 1;
			parametros.put("DEUDA_SISTEMA_" + fila, deuda != null ? valorSeguro(deuda.getSistemaCrediticio()) : "");
			parametros.put("DEUDA_POR_VENCER_" + fila, deuda != null ? valorSeguro(deuda.getValorPorVencer()) : "");
			parametros.put("DEUDA_VENCIDO_" + fila, deuda != null ? valorSeguro(deuda.getValorVencido()) : "");
			parametros.put("DEUDA_CASTIGADA_" + fila, deuda != null ? valorSeguro(deuda.getCarteraCastigada()) : "");
			parametros.put("DEUDA_TOTAL_" + fila, deuda != null ? valorSeguro(deuda.getTotalDeuda()) : "");
		}
	}

	private void agregarParametrosFactoresScore(Map<String, Object> parametros,
			List<FactorScoreReporteDTO> factoresReporte) {
		for (int i = 0; i < 4; i++) {
			FactorScoreReporteDTO factor = factoresReporte != null && factoresReporte.size() > i
					? factoresReporte.get(i)
					: null;
			int fila = i + 1;
			parametros.put("FACTOR_NOMBRE_" + fila, factor != null ? valorSeguro(factor.getFactor()) : "");
			parametros.put("FACTOR_VALOR_" + fila, factor != null ? valorSeguro(factor.getValor()) : "");
			parametros.put("FACTOR_EFECTO_" + fila, factor != null ? valorSeguro(factor.getEfecto()) : "");
		}
	}

	private List<ReporteAvalBuroScoreSeccionDTO> construirSeccionesReporteScore() {
		if (isPersonaJuridicaConsulta()) {
			return construirSeccionesReporteScoreJuridico();
		}

		List<ReporteAvalBuroScoreSeccionDTO> seccionesReporte = new ArrayList<ReporteAvalBuroScoreSeccionDTO>();

		agregarSeccionReporte(seccionesReporte, "1. Titular consultado",
				linea("Tipo identificacion",
						getValorFormateado("identificacionTitular", "tipoIdentificacionSujetoDescripcion"))
						+ linea("Identificacion", getValorFormateado("identificacionTitular", "identificacionSujeto"))
						+ linea("Nombre", getValorFormateado("identificacionTitular", "nombreRazonSocial"))
						+ linea("Tipo sujeto", getValorFormateado("tipoSujeto", "tipoSujeto")));

		agregarSeccionReporte(seccionesReporte, "2. Decision del modelo",
				linea("Fecha evaluacion", getValorFormateado(SECCION_MODELO_CENTRIC_RESUMEN, "fechaEvaluacion"))
						+ linea("Decision modelo", getValorFormateado(SECCION_MODELO_CENTRIC_RESUMEN, "decisionModelo"))
						+ linea("Tipo decision", getTipoDecisionLabel()));

		agregarSeccionReporte(seccionesReporte, "3. Score financiero",
				linea("Score", getValorFormateado("scoreFinancieroV2", "score"))
						+ linea("Personas con peor score", getPorcentaje("scoreFinancieroV2", "clientesPeorScore"))
						+ linea("Probabilidad de caer en mora", getPorcentaje("scoreFinancieroV2", "tasaMalos")));

		agregarSeccionReporte(seccionesReporte, "4. Parametros de entrada",
				linea("Tipo modelo", getValorFormateado("parametrosEntrada", "tipoModelo"))
						+ linea("Marca", getValorFormateado("parametrosEntrada", "marca"))
						+ linea("Ingreso digitado", getValorFormateado("parametrosEntrada", "ingresoDigitado"))
						+ linea("Validador ingreso", getValorFormateado("parametrosEntrada", "validadorIngreso"))
						+ linea("Rango ingresos", getValorFormateado("parametrosEntrada", "rangoIngresos")));

		agregarSeccionReporte(seccionesReporte, "5. Gasto financiero",
				linea("Cuota estimada titular", getValorFormateado("gastoFinanciero", "cuotaEstimadaTitular"))
						+ linea("Cuota total operaciones",
								getValorFormateado("gastoFinanciero", "cuotaTotalOperaciones"))
						+ linea("Cuota total tarjeta", getValorFormateado("gastoFinanciero", "cuotaTotalTarjeta"))
						+ linea("Cuota total servicios", getValorFormateado("gastoFinanciero", "cuotaTotalServicios"))
						+ linea("Cuota vencidos", getValorFormateado("gastoFinanciero", "cuotaVencidos")));

		agregarSeccionReporte(seccionesReporte, "6. Detalle reglas Modelo Centric",
				filasReporte(SECCION_MODELO_CENTRIC_DETALLE,
						Arrays.asList("fechaEvaluacionRegla", "reglaUsada", "valorObtenido", "estadoResultado"), 10));

		agregarSeccionReporte(seccionesReporte, "7. Operaciones vigentes principales", seccionOperacionesPrincipales());

		agregarSeccionReporte(seccionesReporte, "8. Datos de contacto", filasReporte("datosContacto",
				Arrays.asList("telefonoCelular", "ciudadDescripcion", "sector", "direccionCompleta", "numeracion"), 3));

		agregarSeccionReporte(seccionesReporte, "9. Consultas ultimos 12 meses", filasReporte(
				"titularConsultado12Meses", Arrays.asList("fechaConsulta", "nombreComercial", "nombreUsuario"), 3));

		agregarSeccionReporte(seccionesReporte, "10. Datos de la consulta",
				linea("Codigo respuesta", consultaResponse.getResponseCode())
						+ linea("Mensaje", consultaResponse.getMessage())
						+ linea("Numero transaccion", consultaResponse.getTransactionNumber())
						+ linea("Fecha consulta", fechaConsulta != null ? OUT_DATE.format(fechaConsulta) : "")
						+ linea("Vigencia desde", fechaIniPantalla != null ? OUT_DATE.format(fechaIniPantalla) : "")
						+ linea("Vigencia hasta", fechaFinPantalla != null ? OUT_DATE.format(fechaFinPantalla) : ""));

		return seccionesReporte;
	}

	private List<ReporteAvalBuroScoreSeccionDTO> construirSeccionesReporteScoreJuridico() {
		List<ReporteAvalBuroScoreSeccionDTO> seccionesReporte = new ArrayList<ReporteAvalBuroScoreSeccionDTO>();

		agregarSeccionReporte(seccionesReporte, "1. Empresa consultada",
				linea("Tipo identificacion",
						getValorFormateado("identificacionTitular", "tipoIdentificacionSujetoDescripcion"))
						+ linea("Identificacion", getValorFormateado("identificacionTitular", "identificacionSujeto"))
						+ linea("Razon social", getValorFormateado("identificacionTitular", "nombreRazonSocial"))
						+ linea("Tipo compania", getValorFormateado("identificacionTitular", "tipoCompania"))
						+ linea("Fecha constitucion", getValorFormateado("identificacionTitular", "fechaConstitucion"))
						+ linea("Estado", getValorFormateado("identificacionTitular", "estadoSocial"))
						+ linea("Objeto social", getValorFormateado("identificacionTitular", "objetoSocial")));

		agregarSeccionReporte(seccionesReporte, "2. Decision del modelo juridico",
				linea("Fecha evaluacion", getValorFormateado(SECCION_MODELO_CENTRIC_RESUMEN, "fechaEvaluacion"))
						+ linea("Decision modelo", getValorFormateado(SECCION_MODELO_CENTRIC_RESUMEN, "decisionModelo"))
						+ linea("Tipo decision", getTipoDecisionLabel()));

		agregarSeccionReporte(seccionesReporte, "3. Score empresa",
				linea("Score", getValorFormateado("scoreFinancieroV2", "score"))
						+ linea("Empresas con peor score", getPorcentaje("scoreFinancieroV2", "clientesPeorScore"))
						+ linea("Probabilidad de caer en mora", getPorcentaje("scoreFinancieroV2", "tasaMalos")));

		agregarSeccionReporte(seccionesReporte, "4. Detalle empresa", filasReporteDinamico("detalleEmpresa", 3, 10));

		agregarSeccionReporte(seccionesReporte, "5. Resumen principales cuentas financieras",
				filasReporteDinamico("resumenPrincipalesCuentasFinancieras", 4, 10));

		agregarSeccionReporte(seccionesReporte, "6. Cuentas de estados financieros",
				filasReporteDinamico("cuentasEstadosFinancieros", 4, 10));

		agregarSeccionReporte(seccionesReporte, "7. Indices financieros",
				filasReporteDinamico("indicesFinancieros", 4, 10));

		agregarSeccionReporte(seccionesReporte, "8. Accionistas y representantes legales",
				"Principales accionistas\n" + filasReporteDinamico("principalesAccionistas", 4, 8)
						+ "\nRepresentantes legales\n" + filasReporteDinamico("representantesLegales", 4, 8));

		agregarSeccionReporte(seccionesReporte, "9. Contactabilidad", filasReporteDinamico("contactabilidad", 3, 10));

		agregarSeccionReporte(seccionesReporte, "10. Datos de la consulta",
				linea("Codigo respuesta", consultaResponse.getResponseCode())
						+ linea("Mensaje", consultaResponse.getMessage())
						+ linea("Numero transaccion", consultaResponse.getTransactionNumber())
						+ linea("Fecha consulta", fechaConsulta != null ? OUT_DATE.format(fechaConsulta) : "")
						+ linea("Vigencia desde", fechaIniPantalla != null ? OUT_DATE.format(fechaIniPantalla) : "")
						+ linea("Vigencia hasta", fechaFinPantalla != null ? OUT_DATE.format(fechaFinPantalla) : ""));

		return seccionesReporte;
	}

	private List<OfertaSugeridaReporteDTO> construirOfertaSugeridaReporte() {
		List<OfertaSugeridaReporteDTO> ofertasReporte = new ArrayList<OfertaSugeridaReporteDTO>();
		List<Map<String, Object>> filas = getFilas("ofertaSugeridaCapacidadCalculada");
		if (filas == null || filas.isEmpty()) {
			return ofertasReporte;
		}
		for (Map<String, Object> fila : filas) {
			if (fila == null) {
				continue;
			}
			ofertasReporte.add(new OfertaSugeridaReporteDTO(formatCell(fila.get("plazo"), "plazo"),
					formatCell(fila.get("montoSugerido"), "montoSugerido"),
					formatCell(fila.get("cuotaSugerida"), "cuotaSugerida")));
		}
		return ofertasReporte;
	}

	private List<DeudaVigenteReporteDTO> construirDeudaVigenteReporte() {
		List<DeudaVigenteReporteDTO> deudasReporte = new ArrayList<DeudaVigenteReporteDTO>();
		List<Map<String, Object>> filas = getFilas("deudaVigenteTotal");
		if (filas == null || filas.isEmpty()) {
			return deudasReporte;
		}
		for (Map<String, Object> fila : filas) {
			if (fila == null) {
				continue;
			}
			deudasReporte.add(new DeudaVigenteReporteDTO(formatCell(fila.get("sistemaCrediticio"), "sistemaCrediticio"),
					formatCell(fila.get("valorPorVencer"), "valorPorVencer"),
					formatCell(fila.get("valorVencido"), "valorVencido"),
					formatCell(fila.get("carteraCastigada"), "carteraCastigada"),
					formatCell(fila.get("totalDeuda"), "totalDeuda")));
		}
		return deudasReporte;
	}

	private List<FactorScoreReporteDTO> construirFactoresScoreReporte() {
		List<FactorScoreReporteDTO> factoresReporte = new ArrayList<FactorScoreReporteDTO>();
		List<Map<String, Object>> filas = getFilas("factoresScore");
		if (filas == null || filas.isEmpty()) {
			return factoresReporte;
		}
		for (Map<String, Object> fila : filas) {
			if (fila == null) {
				continue;
			}
			factoresReporte.add(new FactorScoreReporteDTO(formatCell(fila.get("factor"), "factor"),
					formatCell(fila.get("valor"), "valor"), formatCell(fila.get("efecto"), "efecto")));
		}
		return factoresReporte;
	}

	private List<ModeloCentricReglaReporteDTO> construirModeloCentricDetalleReporte() {
		List<ModeloCentricReglaReporteDTO> resultado = new ArrayList<ModeloCentricReglaReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte(SECCION_MODELO_CENTRIC_DETALLE)) {
			resultado.add(new ModeloCentricReglaReporteDTO(
					formatCell(fila.get("fechaEvaluacionRegla"), "fechaEvaluacionRegla"),
					formatCell(fila.get("reglaPadre"), "reglaPadre"), formatCell(fila.get("reglaUsada"), "reglaUsada"),
					formatCell(fila.get("valorObtenido"), "valorObtenido"),
					formatCell(fila.get("estadoResultado"), "estadoResultado")));
		}
		return resultado;
	}

	private List<InformacionRucReporteDTO> construirInformacionRucReporte() {
		List<InformacionRucReporteDTO> resultado = new ArrayList<InformacionRucReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte("informacionComoRUC", 3)) {
			resultado.add(
					new InformacionRucReporteDTO(formatCell(fila.get("identificacionSujeto"), "identificacionSujeto"),
							formatCell(fila.get("nombreRazonSocial"), "nombreRazonSocial"),
							formatCell(fila.get("tipoRelacion"), "tipoRelacion")));
		}
		return resultado;
	}

	private List<ContactoReporteDTO> construirContactosReporte() {
		List<ContactoReporteDTO> resultado = new ArrayList<ContactoReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte("datosContacto", 3)) {
			resultado.add(new ContactoReporteDTO(formatCell(fila.get("telefonoCelular"), "telefonoCelular"),
					formatCell(fila.get("ciudadDescripcion"), "ciudadDescripcion"),
					formatCell(fila.get("direccionCompleta"), "direccionCompleta")));
		}
		return resultado;
	}

	private List<Consulta12MesesReporteDTO> construirConsultas12MesesReporte() {
		List<Consulta12MesesReporteDTO> resultado = new ArrayList<Consulta12MesesReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte("titularConsultado12Meses")) {
			resultado.add(new Consulta12MesesReporteDTO(formatCell(fila.get("fechaConsulta"), "fechaConsulta"),
					formatCell(fila.get("nombreComercial"), "nombreComercial"),
					formatCell(fila.get("nombreUsuario"), "nombreUsuario")));
		}
		return resultado;
	}

	private List<TarjetaReporteDTO> construirTarjetasReporte(String seccion, int limite) {
		List<TarjetaReporteDTO> resultado = new ArrayList<TarjetaReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte(seccion, limite)) {
			resultado.add(new TarjetaReporteDTO(formatCell(fila.get("fechaCorte"), "fechaCorte"),
					formatCell(fila.get("razonSocial"), "razonSocial"),
					formatCell(fila.get("marcaTarjetaDescripcion"), "marcaTarjetaDescripcion"),
					formatCell(fila.get("cupoTarjeta"), "cupoTarjeta"),
					formatCell(fila.get("saldoTotal"), "saldoTotal"),
					formatCell(fila.get("saldoVencido"), "saldoVencido"),
					formatCell(fila.get("diasMorosidad"), "diasMorosidad"),
					formatCell(fila.get("cuotaEstimadaTarjetas"), "cuotaEstimadaTarjetas")));
		}
		return resultado;
	}

	private List<OperacionCreditoReporteDTO> construirOperacionesVigentesReporte() {
		List<OperacionCreditoReporteDTO> resultado = new ArrayList<OperacionCreditoReporteDTO>();
		agregarOperacionesReporte(resultado, "operacionesVigentesBanco", 5);
		agregarOperacionesReporte(resultado, "operacionesVigentesCooperativa", 5);
		agregarOperacionesReporte(resultado, "operacionesVigentesEmpresa", 5);
		agregarOperacionesReporte(resultado, "operacionesVigentesServicio", 5);
		agregarOperacionesReporte(resultado, "operacionesVigentesCobranza", 5);
		return resultado;
	}

	private List<OperacionCreditoReporteDTO> construirOperacionesReporte(String seccion, int limite) {
		List<OperacionCreditoReporteDTO> resultado = new ArrayList<OperacionCreditoReporteDTO>();
		agregarOperacionesReporte(resultado, seccion, limite);
		return resultado;
	}

	private void agregarOperacionesReporte(List<OperacionCreditoReporteDTO> resultado, String seccion, int limite) {
		for (Map<String, Object> fila : ultimasFilasReporte(seccion, limite)) {
			resultado
					.add(new OperacionCreditoReporteDTO(formatCell(fila.get("fechaCorte"), "fechaCorte"),
							formatCell(primerValor(fila, "razonSocial", "nombreCasaCobranza"), "razonSocial"),
							formatCell(primerValor(fila, "tipoCreditoDescripcion", "tipoServicioDescripcion"),
									"tipoCredito"),
							formatCell(fila.get("numeroOperacion"), "numeroOperacion"),
							formatCell(primerValor(fila, "saldoTotalCalculado", "saldoTotal"), "saldoTotal"),
							formatCell(primerValor(fila, "valorVencidoTotal", "saldoVencido"), "valorVencidoTotal"),
							formatCell(fila.get("diasMorosidad"), "diasMorosidad"),
							formatCell(primerValor(fila, "cuotaEstimadaOperacion", "cuotaEstimada"), "cuotaEstimada")));
		}
	}

	private List<SerieRiesgoReporteDTO> construirSerieRiesgoReporte(String seccion, String campoPrincipal,
			String campoSecundario) {

		List<SerieRiesgoReporteDTO> resultado = new ArrayList<SerieRiesgoReporteDTO>();

		boolean evolucionScore = "evolucionScoreFinancieroV2".equals(seccion);
		boolean tendenciaDeuda = "tendenciaDeuda".equals(seccion);

		List<Map<String, Object>> filas = evolucionScore || tendenciaDeuda
				? new ArrayList<Map<String, Object>>(getFilas(seccion))
				: ultimasFilasReporte(seccion, 12);

		if (!evolucionScore && !tendenciaDeuda) {
			Collections.reverse(filas);
		}

		for (Map<String, Object> fila : filas) {
			Object valorPrincipal = obtenerValorSerieRiesgo(fila, seccion, campoPrincipal);

			Object valorSecundario = campoSecundario != null ? obtenerValorSerieRiesgo(fila, seccion, campoSecundario)
					: null;

			Double valorPrincipalNumerico = Double.valueOf(toDouble(valorPrincipal));

			if (evolucionScore) {
				valorPrincipalNumerico = normalizarScoreReporte(valorPrincipalNumerico);
			}

			resultado.add(new SerieRiesgoReporteDTO(formatearFechaGraficaReporte(fila.get("fechaCorte")),
					formatCell(valorPrincipal, campoPrincipal),
					campoSecundario != null ? formatCell(valorSecundario, campoSecundario) : "", valorPrincipalNumerico,
					campoSecundario != null ? Double.valueOf(toDouble(valorSecundario)) : Double.valueOf(0d),
					formatCell(fila.get("diasVencido"), "diasVencido"), formatCell(fila.get("color"), "color")));
		}

		return resultado;
	}

	private Object obtenerValorSerieRiesgo(Map<String, Object> fila, String seccion, String campo) {
		if ("evolucionScoreFinancieroV2".equals(seccion)) {
			return primerValor(fila, campo, "ejeY", "score", "scoreFinanciero", "scoreEmpresa", "puntaje", "valorScore",
					"valor");
		}
		if ("totalDeuda".equals(campo)) {
			return primerValor(fila, campo, "saldoTotal", "saldoTotalCalculado", "deudaTotal", "valorTotal");
		}
		if ("valorVencidoTotal".equals(campo)) {
			return primerValor(fila, campo, "valorVencido", "saldoVencido", "saldoVencidoTotal", "deudaVencida");
		}
		return fila.get(campo);
	}

	private Double normalizarScoreReporte(Double score) {
		double valor = score.doubleValue();
		while (valor > 999d) {
			valor = valor / 10d;
		}
		if (valor > 0d && valor <= 10d) {
			valor = valor * 100d;
		}
		return Double.valueOf(valor);
	}

	private String formatearFechaGraficaReporte(Object valor) {
		if (valor == null) {
			return "";
		}
		String fecha = String.valueOf(valor);
		try {
			return formatearMesAnioCorto(CHART_IN_DATE.parse(fecha));
		} catch (Exception e) {
			try {
				return formatearMesAnioCorto(OUT_DATE.parse(fecha));
			} catch (Exception ignored) {
				return fecha;
			}
		}
	}

	private String formatearMesAnioCorto(Date fecha) {
		String[] meses = { "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic" };
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(fecha);
		return meses[calendario.get(Calendar.MONTH)]
				+ String.format(Locale.ROOT, "%02d", calendario.get(Calendar.YEAR) % 100);
	}

	private List<IndicadoresDeudaReporteDTO> construirIndicadoresDeudaReporte() {
		List<IndicadoresDeudaReporteDTO> resultado = new ArrayList<IndicadoresDeudaReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte("indicadoresDeuda", 1)) {
			resultado.add(new IndicadoresDeudaReporteDTO(formatCell(fila.get("saldoPromedio36M"), "saldoPromedio36M"),
					formatCell(fila.get("saldoPromedioTarjetas36M"), "saldoPromedioTarjetas36M"),
					formatCell(fila.get("maxMontoDeuda"), "maxMontoDeuda"),
					formatCell(fila.get("peorEdadVencidoDirecta36M"), "peorEdadVencidoDirecta36M"),
					formatCell(fila.get("maySaldoVencDirecta36M"), "maySaldoVencDirecta36M"),
					formatCell(fila.get("fechaUltimoVencido"), "fechaUltimoVencido")));
		}
		return resultado;
	}

	private List<ServicioHistoricoReporteDTO> construirServiciosHistoricosReporte() {
		List<ServicioHistoricoReporteDTO> resultado = new ArrayList<ServicioHistoricoReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte("operacionesHistoricasServicio", 12)) {
			resultado.add(new ServicioHistoricoReporteDTO(formatCell(fila.get("fechaCorte"), "fechaCorte"),
					formatCell(fila.get("razonSocial"), "razonSocial"),
					formatCell(fila.get("tipoServicioDescripcion"), "tipoServicioDescripcion"),
					formatCell(fila.get("codigoServicioContrato"), "codigoServicioContrato"),
					formatCell(fila.get("estadoServicioDescripcion"), "estadoServicioDescripcion"),
					formatCell(primerValor(fila, "cuotaEstimadaOperacion", "cuotaEstimada"), "cuotaEstimadaOperacion"),
					formatCell(primerValor(fila, "totalDeuda", "saldoDeuda"), "totalDeuda"),
					formatCell(fila.get("valorVencido"), "valorVencido"),
					formatCell(fila.get("numeroDiasVencido"), "numeroDiasVencido")));
		}
		return resultado;
	}

	private List<DetalleEmpresaReporteDTO> construirDetalleEmpresaReporte() {
		List<DetalleEmpresaReporteDTO> resultado = new ArrayList<DetalleEmpresaReporteDTO>();
		for (Map<String, Object> fila : ultimasFilasReporte("detalleEmpresa", 1)) {
			agregarDetalleEmpresa(resultado, "Actividad económica",
					formatCell(fila.get("actividadEconomicaN3Descripcion"), "actividadEconomicaN3Descripcion"));
			agregarDetalleEmpresa(resultado, "Capital suscrito",
					formatCell(fila.get("capitalSuscrito"), "capitalSuscrito"));
			agregarDetalleEmpresa(resultado, "Antigüedad (años)", formatCell(fila.get("antiguedad"), "antiguedad"));
			agregarDetalleEmpresa(resultado, "Grupo económico",
					formatCell(fila.get("nombreGrupoEconomico"), "nombreGrupoEconomico"));
			agregarDetalleEmpresa(resultado, "Empleados directivos",
					formatCell(fila.get("numeroEmpleadosDirectivos"), "numeroEmpleadosDirectivos"));
			agregarDetalleEmpresa(resultado, "Empleados administrativos",
					formatCell(fila.get("numeroEmpleadosAdministrativos"), "numeroEmpleadosAdministrativos"));
			agregarDetalleEmpresa(resultado, "Empleados producción",
					formatCell(fila.get("numeroEmpleadosProduccion"), "numeroEmpleadosProduccion"));
			agregarDetalleEmpresa(resultado, "Empleados otros",
					formatCell(fila.get("numeroEmpleadosOtros"), "numeroEmpleadosOtros"));
			agregarDetalleEmpresa(resultado, "Total empleados",
					formatCell(primerValor(fila, "numeroTotalEmpleados", "numeroEmpleados"), "numeroTotalEmpleados"));
			agregarDetalleEmpresa(resultado, "Inversión extranjera", formatCell(
					fila.get("companiaConInversionExtrangeraDirecta"), "companiaConInversionExtrangeraDirecta"));
			agregarDetalleEmpresa(resultado, "Mercado de valores",
					formatCell(fila.get("perteneceMercadoValores"), "perteneceMercadoValores"));
		}
		return resultado;
	}

	private void agregarDetalleEmpresa(List<DetalleEmpresaReporteDTO> resultado, String campo, String valor) {
		resultado.add(new DetalleEmpresaReporteDTO(campo, valorSeguro(valor)));
	}

	private List<CuentaFinancieraReporteDTO> construirCuentasFinancierasReporte(String seccion) {
		List<CuentaFinancieraReporteDTO> resultado = new ArrayList<CuentaFinancieraReporteDTO>();
		for (Map<String, Object> fila : getFilas(seccion)) {
			resultado.add(new CuentaFinancieraReporteDTO(formatCell(fila.get("cuenta"), "cuenta"),
					formatCell(fila.get("anioActualMenos5"), "anioActualMenos5"),
					formatCell(fila.get("anioActualMenos4"), "anioActualMenos4"),
					formatCell(fila.get("anioActualMenos3"), "anioActualMenos3"),
					formatCell(fila.get("anioActualMenos2"), "anioActualMenos2"),
					formatCell(fila.get("anioActualMenos1"), "anioActualMenos1")));
		}
		return resultado;
	}

	private List<IndiceFinancieroReporteDTO> construirIndicesFinancierosReporte() {
		List<IndiceFinancieroReporteDTO> resultado = new ArrayList<IndiceFinancieroReporteDTO>();
		for (Map<String, Object> fila : getFilas("indicesFinancieros")) {
			resultado.add(new IndiceFinancieroReporteDTO(formatCell(fila.get("tipoIndice"), "tipoIndice"),
					formatCell(fila.get("nombreIndice"), "nombreIndice"),
					formatCell(fila.get("anioActualMenos5"), "anioActualMenos5"),
					formatCell(fila.get("anioActualMenos4"), "anioActualMenos4"),
					formatCell(fila.get("anioActualMenos3"), "anioActualMenos3"),
					formatCell(fila.get("anioActualMenos2"), "anioActualMenos2"),
					formatCell(fila.get("anioActualMenos1"), "anioActualMenos1")));
		}
		return resultado;
	}

	private List<PersonaEmpresaReporteDTO> construirPersonasEmpresaReporte(String seccion, String campoDetalle) {
		List<PersonaEmpresaReporteDTO> resultado = new ArrayList<PersonaEmpresaReporteDTO>();
		for (Map<String, Object> fila : getFilas(seccion)) {
			resultado.add(new PersonaEmpresaReporteDTO(
					formatCell(fila.get("identificacionSujeto"), "identificacionSujeto"),
					formatCell(fila.get("nombre"), "nombre"), formatCell(fila.get(campoDetalle), campoDetalle)));
		}
		return resultado;
	}

	private List<ContactabilidadReporteDTO> construirContactabilidadReporte() {
		List<ContactabilidadReporteDTO> resultado = new ArrayList<ContactabilidadReporteDTO>();
		for (Map<String, Object> fila : getFilas("contactabilidad")) {
			Object telefono = primerValor(fila, "telefono1", "telefonoCelular", "telefono");
			Object email = primerValor(fila, "email1", "email");
			Object paginaWeb = primerValor(fila, "paginaWeb", "web");
			String direccion = construirDireccionContacto(fila);
			if (telefono == null && email == null && paginaWeb == null && isBlank(direccion)) {
				continue;
			}
			resultado.add(new ContactabilidadReporteDTO(
					formatCell(primerValor(fila, "provinciaDescripcion", "provincia"), "provinciaDescripcion"),
					formatCell(primerValor(fila, "ciudadDescripcion", "ciudad"), "ciudadDescripcion"),
					formatCell(telefono, "telefono1"), formatCell(email, "email1"), valorSeguro(direccion),
					formatCell(paginaWeb, "paginaWeb")));
		}
		return resultado;
	}

	private List<Map<String, Object>> ultimasFilasReporte(String seccion) {
		return ultimasFilasReporte(seccion, LIMITE_FILAS_REPORTE);
	}

	private List<Map<String, Object>> ultimasFilasReporte(String seccion, int limite) {
		List<Map<String, Object>> filas = getFilas(seccion);
		if (filas == null || filas.isEmpty()) {
			return Collections.emptyList();
		}
		return new ArrayList<Map<String, Object>>(filas.subList(0, Math.min(filas.size(), limite)));
	}

	private String seccionOperacionesPrincipales() {
		StringBuilder contenido = new StringBuilder();
		contenido.append("Bancos").append("\n");
		contenido.append(filasReporte("operacionesVigentesBanco", Arrays.asList("fechaCorte", "razonSocial",
				"tipoCreditoDescripcion", "saldoTotalCalculado", "valorVencidoTotal", "diasMorosidad"), 3));
		contenido.append("\nEmpresas").append("\n");
		contenido.append(filasReporte("operacionesVigentesEmpresa",
				Arrays.asList("fechaCorte", "razonSocial", "saldoTotalCalculado", "valorVencidoTotal", "diasVencido"),
				3));
		contenido.append("\nServicios").append("\n");
		contenido.append(filasReporte("operacionesVigentesServicio",
				Arrays.asList("fechaCorte", "razonSocial", "totalDeuda", "valorVencido", "numeroDiasVencido"), 3));
		contenido.append("\nCobranza").append("\n");
		contenido.append(filasReporte("operacionesVigentesCobranza", Arrays.asList("fechaCorte", "nombreCasaCobranza",
				"saldoDeuda", "carteraCastigada", "numeroDiasVencido"), 3));
		return contenido.toString();
	}

	private void agregarSeccionReporte(List<ReporteAvalBuroScoreSeccionDTO> seccionesReporte, String titulo,
			String contenido) {
		if (contenido == null || "".equals(contenido.trim())) {
			contenido = "Sin informacion disponible.";
		}
		seccionesReporte.add(new ReporteAvalBuroScoreSeccionDTO(titulo, contenido));
	}

	private String filasReporte(String key, List<String> columnas, int limite) {
		List<Map<String, Object>> filas = getFilas(key);
		if (filas == null || filas.isEmpty()) {
			return "Sin informacion disponible.\n";
		}

		StringBuilder contenido = new StringBuilder();
		int contador = 0;
		for (Map<String, Object> fila : filas) {
			if (contador >= limite) {
				contenido.append("... ").append(filas.size() - limite).append(" registro(s) adicional(es).\n");
				break;
			}
			contador++;
			contenido.append("Registro ").append(contador).append("\n");
			for (String columna : columnas) {
				Object valor = fila != null ? fila.get(columna) : null;
				String valorFormateado = formatCell(valor, columna);
				if (valorFormateado != null && !"".equals(valorFormateado.trim())) {
					contenido.append("  ").append(labelColumna(columna)).append(": ").append(valorFormateado)
							.append("\n");
				}
			}
			contenido.append("\n");
		}
		return contenido.toString();
	}

	private String filasReporteDinamico(String key, int limiteFilas, int limiteColumnas) {
		List<String> columnas = columnasPorSeccion.get(key);
		if (columnas == null || columnas.isEmpty()) {
			return "Sin informacion disponible.\n";
		}

		List<String> columnasVisibles = new ArrayList<String>();
		for (String columna : columnas) {
			if (!colOculta(columna)) {
				columnasVisibles.add(columna);
			}
			if (columnasVisibles.size() >= limiteColumnas) {
				break;
			}
		}
		return filasReporte(key, columnasVisibles, limiteFilas);
	}

	private String linea(String etiqueta, Object valor) {
		return etiqueta + ": " + valorSeguro(valor) + "\n";
	}

	private String valorSeguro(Object valor) {
		return valor != null ? String.valueOf(valor).trim() : "";
	}

	private String construirNombreArchivoReporteScore() {
		String identificacion = getValorFormateado("identificacionTitular", "identificacionSujeto");
		if (identificacion == null || "".equals(identificacion.trim())) {
			identificacion = identificacionCliente;
		}
		if (identificacion == null || "".equals(identificacion.trim())) {
			identificacion = "cliente";
		}
		return "AvalBuroScore_" + identificacion.trim() + ".pdf";
	}

	/**
	 * Detecta si el result corresponde a persona natural o juridica segun las
	 * secciones propias de cada trama.
	 */
	private String detectarTipoTrama(Map<String, Object> resultMap) {
		if (resultMap == null || resultMap.isEmpty()) {
			return "SIN_RESULT";
		}

		if (tieneContenidoResult(resultMap.get("datosGeneralesEmpresa"))
				|| tieneContenidoResult(resultMap.get(SECCION_MODELO_CENTRIC_JURIDICO))
				|| tieneContenidoResult(resultMap.get("scoreEmpresa"))) {
			return PERSONA_JURIDICA;
		}
		if (tieneContenidoResult(resultMap.get("identificacionTitular"))
				|| tieneContenidoResult(resultMap.get(SECCION_MODELO_CENTRIC))
				|| tieneContenidoResult(resultMap.get("scoreFinancieroV2"))) {
			return PERSONA_NATURAL;
		}
		String tipoPorIdentificacion = detectarTipoPersonaPorIdentificacion();
		if (tipoPorIdentificacion != null) {
			return tipoPorIdentificacion;
		}
		return "NO_IDENTIFICADA";
	}

	private String resolverTipoTramaReporte() {
		if (PERSONA_NATURAL.equals(tipoTramaResultado) || PERSONA_JURIDICA.equals(tipoTramaResultado)) {
			return tipoTramaResultado;
		}
		String tipoRespaldo = detectarTipoPersonaPorIdentificacion(resolverIdentificacionReporte());
		if (tipoRespaldo != null) {
			tipoTramaResultado = tipoRespaldo;
		}
		return valorSeguro(tipoTramaResultado);
	}

	private String resolverIdentificacionReporte() {
		if (!isBlank(identificacionCliente)) {
			return identificacionCliente.trim();
		}
		String identificacionSeccion = getValorFormateado("identificacionTitular", "identificacionSujeto");
		if (!isBlank(identificacionSeccion)) {
			return identificacionSeccion.trim();
		}
		return valorSeguro(cedulaClienteSolicitudCredito);
	}

	private boolean tieneContenidoResult(Object valor) {
		if (valor == null) {
			return false;
		}
		if (valor instanceof Map) {
			for (Object contenido : ((Map<?, ?>) valor).values()) {
				if (tieneContenidoResult(contenido)) {
					return true;
				}
			}
			return false;
		}
		if (valor instanceof List) {
			for (Object contenido : (List<?>) valor) {
				if (tieneContenidoResult(contenido)) {
					return true;
				}
			}
			return false;
		}
		return !String.valueOf(valor).trim().isEmpty();
	}

	private String detectarTipoPersonaPorIdentificacion() {
		return detectarTipoPersonaPorIdentificacion(identificacionCliente);
	}

	private String detectarTipoPersonaPorIdentificacion(String identificacionOrigen) {
		if (isBlank(identificacionOrigen)) {
			return null;
		}
		String tipoIdentificacion = isBlank(tipoCliente) ? "" : tipoCliente.trim().toUpperCase();
		String identificacion = identificacionOrigen.replaceAll("\\D", "");

		if ("C".equals(tipoIdentificacion) || "CEDULA".equals(tipoIdentificacion) || "CÉDULA".equals(tipoIdentificacion)
				|| identificacion.length() == 10) {
			return PERSONA_NATURAL;
		}
		if ((!"R".equals(tipoIdentificacion) && !"RUC".equals(tipoIdentificacion)) && identificacion.length() != 13) {
			return null;
		}
		if (identificacion.length() < 3) {
			return null;
		}

		char tercerDigito = identificacion.charAt(2);
		if (tercerDigito >= '0' && tercerDigito <= '5') {
			return PERSONA_NATURAL;
		}
		if (tercerDigito == '6' || tercerDigito == '9') {
			return PERSONA_JURIDICA;
		}
		return null;
	}

	private List<String> filtrarSeccionesPorTipoPersona(List<String> seccionesOrdenadas) {
		List<String> filtradas = new ArrayList<String>();
		for (String seccion : seccionesOrdenadas) {
			if (seccionVisiblePorTipoPersona(seccion)) {
				filtradas.add(seccion);
			}
		}
		return filtradas;
	}

	private boolean seccionVisiblePorTipoPersona(String seccion) {
		if (seccion == null) {
			return false;
		}
		if (SECCIONES_NATURAL.contains(seccion)) {
			return !isPersonaJuridicaConsulta();
		}
		if (SECCIONES_JURIDICA.contains(seccion)) {
			return isPersonaJuridicaConsulta();
		}
		return true;
	}

	/**
	 * Registra columnas clave para diagnosticar diferencias entre tramas.
	 */
	private void logColumnasSeccionesPrincipales() {
		logColumnasSeccion("identificacionTitular");
		logColumnasSeccion("scoreFinancieroV2");
		logColumnasSeccion(SECCION_MODELO_CENTRIC_RESUMEN);
		logColumnasSeccion("tipoSujeto");
		logColumnasSeccion("parametrosEntrada");
	}

	private void logColumnasSeccion(String seccion) {
		List<String> columnas = columnasPorSeccion.get(seccion);
		if (columnas != null && !columnas.isEmpty()) {
			LOG.debug("Aval Buro Score columnas " + seccion + "=" + columnas);
		}
	}

	private String construirBasicAuth(String usuario, String clave) throws IOException {
		String credenciales = (usuario != null ? usuario : "") + ":" + (clave != null ? clave : "");
		return "Basic " + Base64.getEncoder().encodeToString(credenciales.getBytes("UTF-8"));
	}

	private String obtenerCookies(HttpURLConnection connection) {
		List<String> setCookies = null;
		if (connection != null) {
			for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
				if (header.getKey() != null && "Set-Cookie".equalsIgnoreCase(header.getKey())) {
					setCookies = header.getValue();
					break;
				}
			}
		}
		if (setCookies == null || setCookies.isEmpty()) {
			return "";
		}
		StringBuilder cookies = new StringBuilder();
		for (String setCookie : setCookies) {
			if (isBlank(setCookie)) {
				continue;
			}
			String cookie = setCookie.split(";", 2)[0];
			if (cookies.length() > 0) {
				cookies.append("; ");
			}
			cookies.append(cookie);
		}
		return cookies.toString();
	}

	private static class RespuestaEjecucionJasper {
		private final String contenido;
		private final String cookies;

		private RespuestaEjecucionJasper(String contenido, String cookies) {
			this.contenido = contenido;
			this.cookies = cookies;
		}

		private String getContenido() {
			return contenido;
		}

		private String getCookies() {
			return cookies;
		}
	}

	private String escapeXml(Object valor) {
		String texto = valor != null ? valor.toString() : "";
		return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	private String encodePathSegment(String valor) throws IOException {
		return encodeUrlValue(valor).replace("+", "%20");
	}

	private String valorMapa(Map<?, ?> mapa, String clave) {
		Object valor = mapa != null ? mapa.get(clave) : null;
		return valor != null ? valor.toString() : "";
	}

	private String obtenerExportId(Map<?, ?> ejecucion) {
		Object exports = ejecucion != null ? ejecucion.get("exports") : null;
		if (!(exports instanceof List) || ((List<?>) exports).isEmpty()) {
			return "";
		}
		Object export = ((List<?>) exports).get(0);
		return export instanceof Map ? valorMapa((Map<?, ?>) export, "id") : "";
	}

	private String leerRespuesta(HttpURLConnection connection, InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return "";
		}
		StringBuilder respuesta = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
			String line;
			while ((line = reader.readLine()) != null) {
				respuesta.append(line);
			}
		}
		return respuesta.toString();
	}

	private String limitarDetalle(String detalle) {
		if (detalle == null) {
			return "";
		}
		return detalle.length() <= 1000 ? detalle : detalle.substring(0, 1000) + "...";
	}

	private String comprimirJsonParaReporte(Object valor) throws IOException {
		byte[] json = gson.toJson(valor).getBytes("UTF-8");
		ByteArrayOutputStream salida = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(salida)) {
			gzip.write(json);
		}
		return Base64.getEncoder().encodeToString(salida.toByteArray());
	}

	/**
	 * Helper JSF para controlar bloques que solo aplican a persona juridica.
	 */
	public boolean isPersonaJuridicaConsulta() {
		return PERSONA_JURIDICA.equals(tipoTramaResultado);
	}

	public String getLabelBotonGenerarPdf() {
		return isPersonaJuridicaConsulta() ? "Generar PDF Jurídico" : "Generar PDF";
	}

	public String getTituloIdentificacionPrincipal() {
		return isPersonaJuridicaConsulta() ? "Datos Generales Empresa" : "Identificacion del Titular";
	}

	public String getLabelNombrePrincipal() {
		return isPersonaJuridicaConsulta() ? "Razon Social" : "Apellidos y Nombres";
	}

	public String getLabelScorePrincipal() {
		return isPersonaJuridicaConsulta() ? "Score Empresa" : "Score Financiero";
	}

	public String getLabelPeorScorePrincipal() {
		return isPersonaJuridicaConsulta() ? "% empresas con peor score" : "% personas con peor score";
	}

	/**
	 * Helpers JSF para mostrar secciones segun disponibilidad real de datos.
	 */
	public boolean isMostrarResultadosCapacidadPago() {
		return tieneDatos("capacidadPago");
	}

	public String getClaseParametrosEntradaCapacidad() {
		return isMostrarResultadosCapacidadPago() ? "ui-g-6" : "ui-g-12";
	}

	public boolean isMostrarOfertaSugerida() {
		return tieneDatos("ofertaSugeridaCapacidadCalculada") || !isPersonaJuridicaConsulta();
	}

	/**
	 * Extrae las secciones resumen/detalle del nodo modeloCentric sin importar si
	 * la trama viene como persona natural o juridica.
	 */
	@SuppressWarnings("unchecked")
	private boolean addModeloCentricSecciones(Object modeloCentricObj) {
		if (modeloCentricObj instanceof List) {
			List<?> mcList = (List<?>) modeloCentricObj;
			if (!mcList.isEmpty() && mcList.get(0) instanceof Map) {
				return addModeloCentricSeccionesDesdeMap((Map<String, Object>) mcList.get(0));
			}
		} else if (modeloCentricObj instanceof Map) {
			return addModeloCentricSeccionesDesdeMap((Map<String, Object>) modeloCentricObj);
		}
		return false;
	}

	/**
	 * Normaliza el resumen y detalle de reglas del modelo para que el XHTML use
	 * siempre las secciones modeloCentricResumen y modeloCentricDetalle.
	 */
	@SuppressWarnings("unchecked")
	private boolean addModeloCentricSeccionesDesdeMap(Map<String, Object> modeloCentric) {
		boolean normalizado = false;
		if (modeloCentric == null || modeloCentric.isEmpty()) {
			return false;
		}

		Object resumen = modeloCentric.get(NODO_MODELO_RESUMEN);
		if (resumen instanceof Map) {
			addSeccion(SECCION_MODELO_CENTRIC_RESUMEN, Arrays.asList((Map<String, Object>) resumen));
			normalizado = true;
		}

		Object detalle = modeloCentric.get(NODO_MODELO_DETALLE);
		if (detalle instanceof List) {
			List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
			for (Object item : (List<?>) detalle) {
				if (item instanceof Map) {
					rows.add((Map<String, Object>) item);
				}
			}
			if (!rows.isEmpty()) {
				addSeccion(SECCION_MODELO_CENTRIC_DETALLE, rows);
				normalizado = true;
			}
		}
		return normalizado;
	}

	/**
	 * Orquesta la normalizacion por tipo de trama: comun, juridica y presentacion.
	 */
	private void normalizarSeccionesPorTipoTrama() {
		normalizarSeccionesComunes();

		if (isPersonaJuridicaConsulta()) {
			normalizarSeccionesPersonaJuridica();
		}

		normalizarSeccionesPresentacion();
	}

	/**
	 * Homologa secciones que pueden venir en natural o juridica.
	 */
	private void normalizarSeccionesComunes() {
		if (tieneDatos("identificacionTitular")) {
			addSeccion("identificacionTitular", normalizarIdentificacionTitular(getFilas("identificacionTitular")));
		}

		if (tieneDatos("scoreFinancieroV2")) {
			addSeccion("scoreFinancieroV2", normalizarScoreFinanciero(getFilas("scoreFinancieroV2")));
		}
	}

	/**
	 * Adapta la trama juridica a los nombres comunes usados por la pantalla.
	 */
	private void normalizarSeccionesPersonaJuridica() {
		if (!tieneDatos("identificacionTitular") && tieneDatos("datosGeneralesEmpresa")) {
			addSeccion("identificacionTitular", normalizarDatosGeneralesEmpresa(getFilas("datosGeneralesEmpresa")));
		}

		if (!tieneDatos("scoreFinancieroV2") && tieneDatos("scoreEmpresa")) {
			addSeccion("scoreFinancieroV2", normalizarScoreEmpresa(getFilas("scoreEmpresa")));
		}

		if (!tieneDatos(SECCION_MODELO_CENTRIC_RESUMEN) && tieneDatos(SECCION_MODELO_CENTRIC_JURIDICO)) {
			addSeccion(SECCION_MODELO_CENTRIC_RESUMEN,
					Arrays.asList(copiarFila(getPrimeraFila(SECCION_MODELO_CENTRIC_JURIDICO))));
		}

		if (!tieneDatos("tipoSujeto") && tieneDatos("datosGeneralesEmpresa")) {
			Map<String, Object> tipoSujeto = new LinkedHashMap<String, Object>();
			tipoSujeto.put("tipoSujeto", "PERSONA JURIDICA");
			addSeccion("tipoSujeto", Arrays.asList(tipoSujeto));
		}

		if (!tieneDatos("datosContacto") && tieneDatos("contactabilidad")) {
			addSeccion("datosContacto", normalizarContactabilidad(getFilas("contactabilidad")));
		}

		if (!tieneDatos("evolucionScoreFinancieroV2") && tieneDatos("evolucionScoreFinanciero")) {
			addSeccion("evolucionScoreFinancieroV2", getFilas("evolucionScoreFinanciero"));
		}

		if (!tieneDatos("operacionesVigentesComercial") && tieneDatos("operacionesVigentesEmpresa")) {
			addSeccion("operacionesVigentesComercial", getFilas("operacionesVigentesEmpresa"));
		}

		if (!tieneDatos("factoresScore") && tieneDatos(SECCION_MODELO_CENTRIC_DETALLE)) {
			addSeccion("factoresScore", normalizarFactoresDesdeModeloCentric(getFilas(SECCION_MODELO_CENTRIC_DETALLE)));
		}
	}

	/**
	 * Completa datos que pertenecen a la presentacion y no siempre regresan en la
	 * respuesta del servicio.
	 */
	private void normalizarSeccionesPresentacion() {
		if (!tieneDatos("parametrosEntrada") && !isBlank(tipoModelo)) {
			String tipoModeloNorm = tipoModelo.trim().toLowerCase();
			String marcaConsulta = "seminuevo".equals(tipoModeloNorm) ? "-" : marca;
			addSeccion("parametrosEntrada",
					Arrays.asList(crearParametrosEntrada(tipoModeloNorm, marcaConsulta, obtenerIngresosEnteros())));
		}
	}

	/**
	 * Homologa los nombres de columnas de identificacion natural/juridica.
	 */
	private List<Map<String, Object>> normalizarIdentificacionTitular(List<Map<String, Object>> rows) {
		List<Map<String, Object>> normalizadas = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> row : rows) {
			Map<String, Object> copia = copiarFila(row);
			putSiAusente(copia, "tipoIdentificacionSujetoDescripcion",
					primerValor(row, "tipoIdentificacionSujetoDescripcion", "tipoIdentificacionDescripcion",
							"tipoIdentificacion", "tipoDocumento", "tipoDocumentoDescripcion", "tipoDoc"));
			putSiAusente(copia, "identificacionSujeto",
					primerValor(row, "identificacionSujeto", "identificacionTitular", "identificacion", "cedula", "ruc",
							"numeroDocumento", "documento", "numeroIdentificacion"));
			putSiAusente(copia, "nombreRazonSocial", primerValor(row, "nombreRazonSocial", "razonSocial",
					"nombreSujeto", "nombreTitular", "apellidosNombres", "nombres", "nombre"));
			normalizadas.add(copia);
		}
		return normalizadas;
	}

	/**
	 * Homologa los campos de score financiero de persona natural.
	 */
	private List<Map<String, Object>> normalizarScoreFinanciero(List<Map<String, Object>> rows) {
		List<Map<String, Object>> normalizadas = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> row : rows) {
			Map<String, Object> copia = copiarFila(row);
			putSiAusente(copia, "score", primerValor(row, "score", "scoreFinanciero", "puntaje", "valorScore"));
			putSiAusente(copia, "clientesPeorScore", primerValor(row, "clientesPeorScore", "personasPeorScore",
					"porcentajePeorScore", "clientesConPeorScore"));
			putSiAusente(copia, "tasaMalos", primerValor(row, "tasaMalos", "probabilidadMora", "probabilidadCaerMora",
					"probabilidadIncumplimiento"));
			normalizadas.add(copia);
		}
		return normalizadas;
	}

	/**
	 * Mapea datosGeneralesEmpresa a la seccion comun identificacionTitular.
	 */
	private List<Map<String, Object>> normalizarDatosGeneralesEmpresa(List<Map<String, Object>> rows) {
		List<Map<String, Object>> normalizadas = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> row : rows) {
			Map<String, Object> copia = copiarFila(row);
			putSiAusente(copia, "tipoIdentificacionSujetoDescripcion", "RUC");
			putSiAusente(copia, "identificacionSujeto",
					primerValor(row, "identificacionSujeto", "identificacion", "ruc", "numeroRuc", "numeroRUC"));
			putSiAusente(copia, "nombreRazonSocial",
					primerValor(row, "nombreRazonSocial", "razonSocial", "nombreComercial", "nombre"));
			putSiAusente(copia, "tipoCompania", primerValor(row, "tipoCompania", "tipoCompaniaDescripcion"));
			putSiAusente(copia, "fechaConstitucion", primerValor(row, "fechaConstitucion"));
			putSiAusente(copia, "objetoSocial", primerValor(row, "objetoSocial", "actividadEconomica"));
			putSiAusente(copia, "estadoSocial", primerValor(row, "estadoSocial", "estado"));
			normalizadas.add(copia);
		}
		return normalizadas;
	}

	/**
	 * Mapea scoreEmpresa a la seccion comun scoreFinancieroV2.
	 */
	private List<Map<String, Object>> normalizarScoreEmpresa(List<Map<String, Object>> rows) {
		List<Map<String, Object>> normalizadas = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> row : rows) {
			Map<String, Object> copia = copiarFila(row);
			putSiAusente(copia, "score", primerValor(row, "score", "scoreEmpresa", "puntaje", "valorScore"));
			putSiAusente(copia, "clientesPeorScore",
					primerValor(row, "clientesPeorScore", "empresasPeorScore", "porcentajePeorScore"));
			putSiAusente(copia, "tasaMalos",
					primerValor(row, "tasaMalos", "probabilidadMora", "probabilidadIncumplimiento"));
			normalizadas.add(copia);
		}
		return normalizadas;
	}

	/**
	 * Reutiliza el detalle de reglas del modelo como factores visibles en el
	 * resumen de score cuando la trama juridica no trae factoresScore.
	 */
	private List<Map<String, Object>> normalizarFactoresDesdeModeloCentric(List<Map<String, Object>> rows) {
		List<Map<String, Object>> normalizadas = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> row : rows) {
			Map<String, Object> factor = new LinkedHashMap<String, Object>();
			factor.put("factor", primerValor(row, "reglaUsada", "regla", "reglaPadre"));
			factor.put("valor", primerValor(row, "valorObtenido", "valor", "valorRegla"));
			factor.put("efecto", primerValor(row, "estadoResultado", "resultado", "efecto"));
			normalizadas.add(factor);
		}
		return normalizadas;
	}

	/**
	 * Mapea contactabilidad juridica a datosContacto para usar la tabla comun.
	 */
	private List<Map<String, Object>> normalizarContactabilidad(List<Map<String, Object>> rows) {
		List<Map<String, Object>> normalizadas = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> row : rows) {
			Map<String, Object> contacto = copiarFila(row);
			putSiAusente(contacto, "telefonoCelular", primerValor(row, "telefonoCelular", "telefono1", "telefono"));
			putSiAusente(contacto, "ciudadDescripcion", primerValor(row, "ciudadDescripcion", "ciudad"));
			putSiAusente(contacto, "sector", primerValor(row, "sector"));
			putSiAusente(contacto, "direccionCompleta", construirDireccionContacto(row));
			putSiAusente(contacto, "numeracion", primerValor(row, "numeracion", "numero"));
			if (tieneValorContacto(contacto)) {
				normalizadas.add(contacto);
			}
		}
		return normalizadas;
	}

	/**
	 * Evita mostrar filas de contacto sin datos utiles.
	 */
	private boolean tieneValorContacto(Map<String, Object> contacto) {
		return primerValor(contacto, "telefonoCelular", "ciudadDescripcion", "sector", "direccionCompleta",
				"numeracion", "email1", "paginaWeb") != null;
	}

	/**
	 * Construye una direccion legible a partir de las columnas de contactabilidad.
	 */
	private String construirDireccionContacto(Map<String, Object> row) {
		List<String> partes = new ArrayList<String>();
		agregarParteDireccion(partes, primerValor(row, "callePrincipal", "direccionCompleta", "direccion"));
		agregarParteDireccion(partes, primerValor(row, "calleSecundaria"));
		agregarParteDireccion(partes, primerValor(row, "referencia"));
		return partes.isEmpty() ? null : join(partes, " / ");
	}

	private void agregarParteDireccion(List<String> partes, Object valor) {
		if (valor != null && !String.valueOf(valor).trim().isEmpty()) {
			partes.add(String.valueOf(valor).trim());
		}
	}

	private String join(List<String> valores, String separador) {
		StringBuilder sb = new StringBuilder();
		for (String valor : valores) {
			if (sb.length() > 0) {
				sb.append(separador);
			}
			sb.append(valor);
		}
		return sb.toString();
	}

	/**
	 * Agrega los parametros enviados a Aval Buro cuando el servicio no los retorna.
	 */
	private void agregarParametrosEntradaResult(Map<String, Object> resultMap, String tipoModeloConsulta,
			String marcaConsulta, String ingresosConsulta) {
		if (resultMap == null || resultMap.get("parametrosEntrada") != null) {
			return;
		}

		resultMap.put("parametrosEntrada",
				Arrays.asList(crearParametrosEntrada(tipoModeloConsulta, marcaConsulta, ingresosConsulta)));
	}

	private Map<String, Object> crearParametrosEntrada(String tipoModeloConsulta, String marcaConsulta,
			String ingresosConsulta) {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		parametros.put("tipoModelo", tipoModeloConsulta);
		parametros.put("marca", marcaConsulta);
		parametros.put("ingresoDigitado", ingresosConsulta);
		return parametros;
	}

	private Map<String, Object> copiarFila(Map<String, Object> row) {
		Map<String, Object> copia = new LinkedHashMap<String, Object>();
		if (row != null) {
			copia.putAll(row);
		}
		return copia;
	}

	private Object primerValor(Map<String, Object> row, String... columnas) {
		if (row == null || columnas == null) {
			return null;
		}
		for (String columna : columnas) {
			Object valor = row.get(columna);
			if (valor != null && !String.valueOf(valor).trim().isEmpty()) {
				return valor;
			}
		}
		return null;
	}

	private void putSiAusente(Map<String, Object> row, String columna, Object valor) {
		if (row == null || columna == null || valor == null) {
			return;
		}
		Object actual = row.get(columna);
		if (actual == null || String.valueOf(actual).trim().isEmpty()) {
			row.put(columna, valor);
		}
	}

	// =========================
	// GETTERS / SETTERS
	// =========================
	public String getTipoCliente() {
		return tipoCliente;
	}

	public boolean isSolicitudCredito() {
		return solicitudCredito;
	}

	public boolean isGenerarFileCrediticioBloqueado() {
		return generarFileCrediticioBloqueado;
	}

	public void setGenerarFileCrediticioBloqueado(boolean generarFileCrediticioBloqueado) {
		this.generarFileCrediticioBloqueado = generarFileCrediticioBloqueado;
	}

	public void setTipoCliente(String tipoCliente) {
		this.tipoCliente = tipoCliente;
	}

	public String getIdentificacionCliente() {
		return identificacionCliente;
	}

	public void setIdentificacionCliente(String identificacionCliente) {
		this.identificacionCliente = identificacionCliente;
	}

	public BigDecimal getIngresos() {
		return ingresos;
	}

	public void setIngresos(BigDecimal ingresos) {
		this.ingresos = ingresos;
	}

	public String getTipoModelo() {
		return tipoModelo;
	}

	public void setTipoModelo(String tipoModelo) {
		this.tipoModelo = tipoModelo;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public boolean isMostrarFiltrosScoreVehiculo() {
		return mostrarFiltrosScoreVehiculo;
	}

	public boolean isMostrarPantallasConsulta() {
		return mostrarPantallasConsulta;
	}

	public boolean getMostrarPantallasConsulta() {
		return mostrarPantallasConsulta;
	}

	public void setMostrarPantallasConsulta(boolean mostrarPantallasConsulta) {
		this.mostrarPantallasConsulta = mostrarPantallasConsulta;
	}

	public String getLogo() {
		return logo;
	}

	public String getNombreEmpresa() {
		return nombreEmpresa;
	}

	public void setNombreEmpresa(String nombreEmpresa) {
		this.nombreEmpresa = nombreEmpresa;
	}

	public String getNombreUsuarioConsulta() {
		return nombreUsuarioConsulta;
	}

	public void setNombreUsuarioConsulta(String nombreUsuarioConsulta) {
		this.nombreUsuarioConsulta = nombreUsuarioConsulta;
	}

	public Date getFechaConsulta() {
		return fechaConsulta;
	}

	public void setFechaConsulta(Date fechaConsulta) {
		this.fechaConsulta = fechaConsulta;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public ConsultaResponse getConsultaResponse() {
		return consultaResponse;
	}

	public void setConsultaResponse(ConsultaResponse consultaResponse) {
		this.consultaResponse = consultaResponse;
	}

	public Map<String, List<Map<String, Object>>> getSecciones() {
		return secciones;
	}

	public void setSecciones(Map<String, List<Map<String, Object>>> secciones) {
		this.secciones = secciones;
	}

	public Map<String, List<String>> getColumnasPorSeccion() {
		return columnasPorSeccion;
	}

	public void setColumnasPorSeccion(Map<String, List<String>> columnasPorSeccion) {
		this.columnasPorSeccion = columnasPorSeccion;
	}

	public List<String> getSeccionesOrdenadas() {
		return seccionesOrdenadas;
	}

	public void setSeccionesOrdenadas(List<String> seccionesOrdenadas) {
		this.seccionesOrdenadas = seccionesOrdenadas;
	}

	public void setSemaforoChartJson(String semaforoChartJson) {
		this.semaforoChartJson = semaforoChartJson;
	}

	public void setTendenciaDeudaChartJson(String tendenciaDeudaChartJson) {
		this.tendenciaDeudaChartJson = tendenciaDeudaChartJson;
	}

	public String getMontoDeudaBancos() {
		return obtenerMontoDeudaVigente("banco");
	}

	public String getMontoDeudaCooperativas() {
		return obtenerMontoDeudaVigente("cooper");
	}

	public String getMontoDeudaTarjetas() {
		return obtenerMontoDeudaVigente("tarjet");
	}

	public String getMontoDeudaComercial() {
		return obtenerMontoDeudaVigente("comercial");
	}

	public String getMontoDeudaServicios() {
		return obtenerMontoDeudaVigente("servici");
	}

	public String getMontoDeudaCobranza() {
		return obtenerMontoDeudaVigente("cobran");
	}

	public List<SelectItemDto> getListaMarcas() {
		return listaMarcas;
	}

	public void setListaMarcas(List<SelectItemDto> listaMarcas) {
		this.listaMarcas = listaMarcas;
	}

	public CxcAvalBuro getExisteCedula() {
		return existeCedula;
	}

	public void setExisteCedula(CxcAvalBuro existeCedula) {
		this.existeCedula = existeCedula;
	}

	public boolean isExiste() {
		return existe;
	}

	public void setExiste(boolean existe) {
		this.existe = existe;
	}

	public Date getFechaIniPantalla() {
		return fechaIniPantalla;
	}

	public void setFechaIniPantalla(Date fechaIniPantalla) {
		this.fechaIniPantalla = fechaIniPantalla;
	}

	public Date getFechaFinPantalla() {
		return fechaFinPantalla;
	}

	public void setFechaFinPantalla(Date fechaFinPantalla) {
		this.fechaFinPantalla = fechaFinPantalla;
	}

	public String getMensajeNuevaBusqueda() {
		return mensajeNuevaBusqueda;
	}

	public void setMensajeNuevaBusqueda(String mensajeNuevaBusqueda) {
		this.mensajeNuevaBusqueda = mensajeNuevaBusqueda;
	}

	public ParamDetServiceLocal getParamDetService() {
		return paramDetService;
	}

	public void setParamDetService(ParamDetServiceLocal paramDetService) {
		this.paramDetService = paramDetService;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public int getNumeroDiasValidoAvalBuro() {
		return numeroDiasValidoAvalBuro;
	}

	public void setNumeroDiasValidoAvalBuro(int numeroDiasValidoAvalBuro) {
		this.numeroDiasValidoAvalBuro = numeroDiasValidoAvalBuro;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getTipoDeudorSeleccionado() {
		return tipoDeudorSeleccionado;
	}

	public void setTipoDeudorSeleccionado(String tipoDeudorSeleccionado) {
		this.tipoDeudorSeleccionado = tipoDeudorSeleccionado;
	}

	public String getTipoDeudorSemaforo() {
		return tipoDeudorSemaforo;
	}

	public void setTipoDeudorSemaforo(String tipoDeudorSemaforo) {
		this.tipoDeudorSemaforo = tipoDeudorSemaforo;
	}

	public String getTipoCreditoSemaforo() {
		return tipoCreditoSemaforo;
	}

	public void setTipoCreditoSemaforo(String tipoCreditoSemaforo) {
		this.tipoCreditoSemaforo = tipoCreditoSemaforo;
	}

	public String getSistemaCrediticioSemaforo() {
		return sistemaCrediticioSemaforo;
	}

	public void setSistemaCrediticioSemaforo(String sistemaCrediticioSemaforo) {
		this.sistemaCrediticioSemaforo = sistemaCrediticioSemaforo;
	}

	public String getTipoDeudorTendencia() {
		return tipoDeudorTendencia;
	}

	public void setTipoDeudorTendencia(String tipoDeudorTendencia) {
		this.tipoDeudorTendencia = tipoDeudorTendencia;
	}

	public String getTipoCreditoTendencia() {
		return tipoCreditoTendencia;
	}

	public void setTipoCreditoTendencia(String tipoCreditoTendencia) {
		this.tipoCreditoTendencia = tipoCreditoTendencia;
	}

	public String getSistemaCrediticioTendencia() {
		return sistemaCrediticioTendencia;
	}

	public void setSistemaCrediticioTendencia(String sistemaCrediticioTendencia) {
		this.sistemaCrediticioTendencia = sistemaCrediticioTendencia;
	}

	public Integer getMesesTendencia() {
		return mesesTendencia;
	}

	public void setMesesTendencia(Integer mesesTendencia) {
		this.mesesTendencia = mesesTendencia;
	}

	public String getTipoDeudorHistoricoComercial() {
		return tipoDeudorHistoricoComercial;
	}

	public void setTipoDeudorHistoricoComercial(String tipoDeudorHistoricoComercial) {
		this.tipoDeudorHistoricoComercial = tipoDeudorHistoricoComercial;
	}

	public Integer getMesesHistoricoComercial() {
		return mesesHistoricoComercial;
	}

	public void setMesesHistoricoComercial(Integer mesesHistoricoComercial) {
		this.mesesHistoricoComercial = mesesHistoricoComercial;
	}

	public String getTipoDeudorHistoricoBanco() {
		return tipoDeudorHistoricoBanco;
	}

	public void setTipoDeudorHistoricoBanco(String tipoDeudorHistoricoBanco) {
		this.tipoDeudorHistoricoBanco = tipoDeudorHistoricoBanco;
	}

	public Integer getMesesHistoricoBanco() {
		return mesesHistoricoBanco;
	}

	public void setMesesHistoricoBanco(Integer mesesHistoricoBanco) {
		this.mesesHistoricoBanco = mesesHistoricoBanco;
	}

	public String getTipoDeudorHistoricoCooperativa() {
		return tipoDeudorHistoricoCooperativa;
	}

	public void setTipoDeudorHistoricoCooperativa(String tipoDeudorHistoricoCooperativa) {
		this.tipoDeudorHistoricoCooperativa = tipoDeudorHistoricoCooperativa;
	}

	public Integer getMesesHistoricoCooperativa() {
		return mesesHistoricoCooperativa;
	}

	public void setMesesHistoricoCooperativa(Integer mesesHistoricoCooperativa) {
		this.mesesHistoricoCooperativa = mesesHistoricoCooperativa;
	}

	public String getTipoDeudorVigenteBanco() {
		return tipoDeudorVigenteBanco;
	}

	public void setTipoDeudorVigenteBanco(String tipoDeudorVigenteBanco) {
		this.tipoDeudorVigenteBanco = tipoDeudorVigenteBanco;
	}

	public String getTipoDeudorVigenteCooperativa() {
		return tipoDeudorVigenteCooperativa;
	}

	public void setTipoDeudorVigenteCooperativa(String tipoDeudorVigenteCooperativa) {
		this.tipoDeudorVigenteCooperativa = tipoDeudorVigenteCooperativa;
	}

	public String getTipoDeudorVigenteComercial() {
		return tipoDeudorVigenteComercial;
	}

	public void setTipoDeudorVigenteComercial(String tipoDeudorVigenteComercial) {
		this.tipoDeudorVigenteComercial = tipoDeudorVigenteComercial;
	}

	public String getTipoDeudorVigenteCobranza() {
		return tipoDeudorVigenteCobranza;
	}

	public void setTipoDeudorVigenteCobranza(String tipoDeudorVigenteCobranza) {
		this.tipoDeudorVigenteCobranza = tipoDeudorVigenteCobranza;
	}

	public Gson getGson() {
		return gson;
	}

}
