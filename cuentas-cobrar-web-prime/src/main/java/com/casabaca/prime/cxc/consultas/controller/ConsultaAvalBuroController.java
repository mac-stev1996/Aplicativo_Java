/**
 * 
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BubbleChartModel;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DonutChartModel;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcAvalBuro;
import com.casabaca.cxc.ejb.servicio.CxcAvalBuroServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.consultas.avalburo.dto.CxcAvalBuroJson;
import com.casabaca.prime.cxc.consultas.avalburo.dto.SaldoNoDevengaInteresDto;
import com.casabaca.prime.cxc.consultas.avalburo.dto.SaldoPorVencerDto;
import com.casabaca.prime.cxc.consultas.avalburo.dto.SaldoVencidoDto;
import com.casabaca.prime.cxc.consultas.avalburo.dto.TarjetasCreditoVigentesDTO;
import com.casabaca.rest.wsAvalBuro.entidad.ConsultaResponse;
import com.casabaca.rest.wsAvalBuro.entidad.Contactabilidad;
import com.casabaca.rest.wsAvalBuro.entidad.CuentasEstadosFinancieros;
import com.casabaca.rest.wsAvalBuro.entidad.DatosContacto;
import com.casabaca.rest.wsAvalBuro.entidad.DatosGeneralesEmpresa;
import com.casabaca.rest.wsAvalBuro.entidad.DetalleEmpresa;
import com.casabaca.rest.wsAvalBuro.entidad.DetalleTarjetaCredito;
import com.casabaca.rest.wsAvalBuro.entidad.DetalleTarjetaSaldoVigente;
import com.casabaca.rest.wsAvalBuro.entidad.DeudaVigenteTotal;
import com.casabaca.rest.wsAvalBuro.entidad.EstructuraOperacionBancoDetalle;
import com.casabaca.rest.wsAvalBuro.entidad.EstructuraOperacionCooperativaDetalle;
import com.casabaca.rest.wsAvalBuro.entidad.EvolucionScoreFinanciero;
import com.casabaca.rest.wsAvalBuro.entidad.FactoresScore;
import com.casabaca.rest.wsAvalBuro.entidad.GastoFinanciero;
import com.casabaca.rest.wsAvalBuro.entidad.IdentificacionTitular;
import com.casabaca.rest.wsAvalBuro.entidad.IndicadorTarjeta;
import com.casabaca.rest.wsAvalBuro.entidad.IndicadoresDeuda;
import com.casabaca.rest.wsAvalBuro.entidad.IndicesFinancieros;
import com.casabaca.rest.wsAvalBuro.entidad.InformacionComoRUC;
import com.casabaca.rest.wsAvalBuro.entidad.ManejoCuentasCorrientes;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionHistoricaBanco;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionHistoricaCobranza;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionHistoricaCooperativa;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionHistoricaEmpresa;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionHistoricaTarjeta;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionVigenteBanco;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionVigenteCooperativa;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionVigenteTarjeta;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionesCodeudorGarante;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionesHistoricasServicio;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionesVigentesCobranza;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionesVigentesEmpresa;
import com.casabaca.rest.wsAvalBuro.entidad.OperacionesVigentesServicio;
import com.casabaca.rest.wsAvalBuro.entidad.PrincipalesAccionistas;
import com.casabaca.rest.wsAvalBuro.entidad.RelacionEmpresas;
import com.casabaca.rest.wsAvalBuro.entidad.RepresentantesLegales;
import com.casabaca.rest.wsAvalBuro.entidad.ResumenPrincipalesCuentasFinancieras;
import com.casabaca.rest.wsAvalBuro.entidad.ScoreEmpresa;
import com.casabaca.rest.wsAvalBuro.entidad.ScoreFinanciero;
import com.casabaca.rest.wsAvalBuro.entidad.SemaforoMaximoDiasVencido;
import com.casabaca.rest.wsAvalBuro.entidad.TendenciaDeuda;
import com.casabaca.rest.wsAvalBuro.entidad.TitularConsultado;
import com.casabaca.webservice.wsAvalBuroImpl.MainEjecutarServicioConsultaAvalBuro;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


@ManagedBean
@ViewScoped
public class ConsultaAvalBuroController extends CommonController implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;
	
	String noCia;
	ConsultaResponse consultaResponse;
	ConsultaResponse consultaResponseGuardar;
	CxcAvalBuroJson consultaResponseExternal;
	private Date fechaActual;
	private String tipoCliente;
	private String identificacionCliente;
	private String mensajeConexion;
	private String mensajeDatosVacios;
	private List<SelectItem> listaTiposIdentificacion;
	private String porcentajePeorScore;
	private String porcentajeProbalidadCaerMora;
	private String explicacion;
	private Double totalSaldoVencer;
	private Double totalSaldoNDI;
	private Double totalSaldoVenciado;
	private Double totalDemandaJudicial;
	private Double totalCarteraCastigada;
	private Double totalDeudaTotal;
	private String logoAvalBuro;
	private boolean esTitular;
	private boolean esCodeudor;
	private List<String> selectedOptionsTitularCodeudor;
	private boolean esTitularCoop;
	private boolean esCodeudorCoop;
	private String opcionUltmesesTC;
	private String opcionUltmesesBancos;
	private String opcionUltmesesCooperativas;
	private String opcionUltmesesEmpresas;
	private String opcionUltmesesServicios;
	private String opcionUltmesesCobranzas;
	private boolean esTitularHisBanco;
	private boolean esCodeudorHisBanco;
	private boolean esTitularHisCooperativa;
	private boolean esCodeudorHisCooperativa;
	private boolean esTitularHisEmpresas;
	private boolean esCodeudorHisEmpresas;
	private boolean esTitularHisServicio;
	private boolean esCodeudorHisServicio;
	
	/*Totales Tarjeta de Credito Vigentes (Modal)*/
	private double saldoTotal;
    private double capitalxVencerTotal;
    private double saldoVencido;
    private double valorNoDevengaInteresTotal;
    private double valorDemandaJudicial;
    private double carteraCastigada;
    private int diasMorosidad;
    private double valorPagado;
    private double valorMinimoPagar;
    /*Totales Saldo po Vencer (Modal)*/
    private double capitalxVencer1a30;    
    private double capitalxVencer31a90;
    private double capitalxVencer91a180;
    private double capitalxVencer181a360;
    private double capitalxVencerMas360;  
    /*Totales Saldo No Devenga Intereses (Modal)*/
    private double valorNoDevengaInteres1a30;
    private double valorNoDevengaInteres31a90;
    private double valorNoDevengaInteres91a180;
    private double valorNoDevengaInteres181a360;
    private double valorNoDevengaInteresMas360;
    /*Totales Saldo Vencido (Modal)*/
    private double capitalVencido1a30;
    private double capitalVencido31a90;
    private double capitalVencido91a180;
    private double capitalVencido181a360;
    private double capitalVencidoMas360;
    private double capitalVencido181a270;
    private double capitalVencidoMas270;
    private double interesVencido1a30;
    private double interesVencido31a60;
    private double interesVencido61a90;
    private double interesVencido91a180;
    private double interesVencido181a270;
    private double interesVencidoMas270;
    private double interesSobreMora;
    private double totalCostoOperativoVencido;
    /*Totales Operaciones Vigentes Bacos*/
    private double valorOperacion;
    private double plazoXOperacion;
    private double plazoXOpPendiente;
    private double saldoTotalCalculado;
    private double valorxVencerTotal;
    private double valorVencidoTotal;
    private double valorNoDevengaInteresTotalBancos;
    private double valorDemandaJudicialBancos;
    private double carteraCastigadaBancos;
    private int diasMorosidadBancos;
    private double cuotaEstimadaOperacion;
    private double tjcConsumoMes;
    private double tjcSaldoTotal;
    private double tjcSaldoVencer;
    private double tjcSaldoVencido;
    private double tjcSaldoNDI;
    private double tjcDemandaJudicial;
    private double tjcCarteraCastigada;
    private double tjcUltValorPagado;
    private double tjcValorMinPagar;
    private double tjcCuotaMensual;
    
    private double bcMontoOriginal;
    private double bcSaldoTotal;
    private double bcSaldoVencer;
    private double bcSaldoVencido;
    private double bcSaldoNDI;
    private double bcDemandaJudicial;
    private double bcCarteraCastigada;
    private double bcCuotaMensual;
    
    private double coopMontoOriginal;
    private double coopSaldoTotal;
    private double coopSaldoVencer;
    private double coopSaldoVencido;
    private double coopSaldoNDI;
    private double coopDemandaJudicial;
    private double coopCarteraCastigada;
    private double coopCuotaMensual;
          
    private double empSaldoActual;
    private double empCuotaMensual;
    private double empSaldoVencer;
    private double empSaldoVencido;
    private double empSaldoNDI;
    private double empSaldoDemandaJudicial;
    private double empCarteraCastigada;
    
    private double servCuotaMensual;
    private double servSaldoVencer;
    private double servSaldoActual;
    private double servSaldoVencido;
    
    private double cobzMontoOriginal;
    private double cobzSaldoTotal;
    private double cobzSaldoVencer;
    private double cobzSaldoVencido;
    private double cobzSaldoNDI;
    private double cobzDemandaJudicial;
    private double cobzCarteraCastigada;
    private double cobzCuotaEstimada;
    private int anioActual;
	
	private Boolean mostrarPantallasConsulta;
	private IdentificacionTitular identificacionTitular;
	private DatosGeneralesEmpresa datosGeneralesEmpresa;
	private InformacionComoRUC informacionComoRUC; 
	private ScoreFinanciero scoreFinanciero;
	private ScoreEmpresa scoreEmpresa; 
	private List<FactoresScore> lstFactoresScore;
	private List<ManejoCuentasCorrientes> lstManejoCuentasCorrientes; 
	private List<DeudaVigenteTotal> lstDeudaVigenteTotal;
	private List<GastoFinanciero> lstGastoFinanciero;
	private List<OperacionesCodeudorGarante> lstOperacionesCodeudorGarante;
	private List<InformacionComoRUC> lstInformacionComoRuc;
	private List<OperacionVigenteTarjeta> lstOperacionesVigentesTarjeta;
	private List<DetalleTarjetaSaldoVigente> lstDetalleTarjetaSaldoVigente; 
	private List<IndicadorTarjeta> lstIndicadorTarjeta;
	private List<OperacionVigenteBanco> lstOperacionVigenteBanco;
	private List<OperacionVigenteBanco> lstOperacionVigenteBancoFiltradoTitular;
	private List<OperacionVigenteBanco> lstOperacionVigenteBancoFiltradoCodeudor;
	private List<OperacionVigenteBanco> lstOperacionVigenteBancoFiltradoFinal;
	private List<DetalleTarjetaCredito> lstDetalleTarjetaCredito;
	private DetalleTarjetaCredito detalleTarjetaCreditoSeleccionada;
	private List<TarjetasCreditoVigentesDTO> lstTarjetasCreditoVigentesDTO;
	private List<SaldoPorVencerDto> lstSaldoPorVencerDto;
	private List<SaldoNoDevengaInteresDto> lstSaldoNoDevengaInteresDto;
	private List<SaldoVencidoDto> lstSaldoVencidoDto;
	private List<EstructuraOperacionBancoDetalle> lstEstructuraOperacionBancoDetalle;
	private EstructuraOperacionBancoDetalle estrucOperBancoDetSeleccionada;
	private List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativa;
	private List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativaFilterTitular;
	private List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativaFilterCodeudor;
	private List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativaFilterFinal;
	private List<EstructuraOperacionCooperativaDetalle> lstEstructuraOperacionCooperativaDetalle;
	private EstructuraOperacionCooperativaDetalle estructuraOperacionCooperativaDetalle;
	private List<EvolucionScoreFinanciero> lstEvolucionScoreFinanciero;
	
	private List<OperacionesVigentesCobranza> lstOperacionesVigentesCobranza;
	private List<OperacionesVigentesServicio> lstOperacionesVigentesServicio;
	private List<OperacionesVigentesEmpresa> lstOperacionesVigentesEmpresa;
	private List<IndicadoresDeuda> lstIndicadoresDeuda;
	private List<OperacionHistoricaTarjeta> lstOperacionHistoricaTarjeta;
	private List<OperacionHistoricaTarjeta> lstOperacionHistoricaTarjetaFiltrada;
	private List<OperacionHistoricaBanco> lstOperacionHistoricaBanco;
	private List<OperacionHistoricaBanco> lstOperacionHistoricaBancoFiltrada;	
	private List<OperacionHistoricaCooperativa> lstOperacionHistoricaCooperativa;
	private List<OperacionHistoricaCooperativa> lstOperacionHistoricaCooperativaFiltrada;
	private List<OperacionHistoricaEmpresa> lstOperacionHistoricaEmpresa;
	private List<OperacionHistoricaEmpresa> lstOperacionHistoricaEmpresaFiltrada;
	private List<OperacionesHistoricasServicio> lstOperacionesHistoricasServicio;
	private List<OperacionesHistoricasServicio> lstOperacionesHistoricasServicioFiltrada;
	private List<OperacionHistoricaCobranza> lstOperacionHistoricaCobranza;
	private List<OperacionHistoricaCobranza> lstOperacionHistoricaCobranzaFiltrada;
	private List<RelacionEmpresas> lstRelacionEmpresas;
	private List<DatosContacto> lstDatosContacto;
	private List<TitularConsultado> lstTitularConsultado;
	private List<SemaforoMaximoDiasVencido> lstSemaforoMaximoDiasVencido;
	private List<TendenciaDeuda> lstTendenciaDeuda;
	private List<DetalleEmpresa> lstDetalleEmpresas;
	private List<ResumenPrincipalesCuentasFinancieras> lstResumenPrincipalesCuentasFinancieras;
	private List<CuentasEstadosFinancieros> lstCuentasEstadosFinancieros;
	private List<IndicesFinancieros> lstIndicesFinancieros;
	private List<PrincipalesAccionistas> lstPrincipalesAccionistas;
	private List<RepresentantesLegales> lstRepresentantesLegales;
	private List<Contactabilidad> lstContactabilidad;
	
	private DonutChartModel score;
	private DonutChartModel scoreEmpresaDonut; 
	private BubbleChartModel evolScoreFinanciero;
	private BubbleChartModel semaforoMaxDiasVenChart;
	private LineChartModel tendenciaDeudaChat;
	private LineChartModel lineModel;
	private LineChartModel lineModelSemaforo;
	private String logo;
	private static final Logger LOG = Logger.getLogger(ConsultaAvalBuroController.class);
	private CxcAvalBuro existeCedula;
	private Date fechaIniPantalla;
	private Date fechaFinPantalla;
	private String scorePantalla;	
	int numeroDiasValidoAvalBuro;
	private boolean mostrarPantallaRuc;

	@EJB(lookup = NombreJNDI.CXC_AVAL_BURO_SERVICE)
	private CxcAvalBuroServiceLocal cxcAvalBuroServiceLocal;
	private String mensajeNuevaBusqueda;
	private Boolean existe;
	private String urlWSconsulta;
	private HttpsURLConnection connection;
	private HttpURLConnection connectionGuardar;

	private String nombreUsuarioConsulta;

	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		consultaResponse = new ConsultaResponse();	
		mostrarPantallasConsulta = Boolean.FALSE;		
		logoAvalBuro = getServletContext().getRealPath("/resources/images/logo-aval-buro.png");
		fechaActual = new Date();		
		esTitular = Boolean.TRUE;
		esCodeudor = Boolean.TRUE;
		esTitularCoop = Boolean.TRUE;
		esCodeudorCoop = Boolean.TRUE;	
		esTitularHisBanco = Boolean.TRUE;
		esCodeudorHisBanco = Boolean.TRUE;
		esTitularHisCooperativa = Boolean.TRUE;
		esCodeudorHisCooperativa= Boolean.TRUE;
		esTitularHisEmpresas = Boolean.TRUE;
		esCodeudorHisEmpresas= Boolean.TRUE;
		esTitularHisServicio = Boolean.TRUE;
		esCodeudorHisServicio= Boolean.TRUE;
		cargarDiasValidosAvalBuro() ;
		logo="/logo-aval-buro.png";
		anioActual = FechaUtils.getAno(fechaActual);
		mostrarPantallaRuc = false;
	}
		
	public void cargarDiasValidosAvalBuro() {
		List<ParamDet> diasValidos = paramDetService.buscarTiposIdentificacion(noCia, "DIASAB");
		numeroDiasValidoAvalBuro = Integer.parseInt(diasValidos.get(0).getTexto1());
		LOG.info("DIAS VALIDOS DATA AVAL BURO" + numeroDiasValidoAvalBuro);
	}
	
	public void cargarTiposIdentificaciones() {
		listaTiposIdentificacion = new ArrayList<SelectItem>();
		SelectItem item = null;
		List<ParamDet> lstParam = paramDetService.buscarTiposIdentificacion(noCia, "TIPIDE");
		for(ParamDet paramDet : lstParam) {
			item = new SelectItem();
			item.setValue(paramDet.getParamDetPK().getCodigoDet());
			item.setLabel(paramDet.getDescripcion());			
			listaTiposIdentificacion.add(item);
		}
	}
	
	public void consultarAvalBuro() throws ParseException {
		Gson gson = new Gson();	
		LocalDateTime fecActual = fechaActual.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime fecFinalDatos = fecActual.plusDays(numeroDiasValidoAvalBuro);			
		Date fechaFinDatosValidos = Date.from(fecFinalDatos.atZone(ZoneId.systemDefault()).toInstant());
		
		if(existeHistorialCrediticioPorCedula()){					
			try {
				if (!tieneJsonLegacyDisponible(existeCedula)) {
					LOG.warn("Registro existente sin JSON para cédula " + identificacionCliente
							+ ". Se realizará nueva consulta al WS.");
					if (!refrescarJsonLegacyDesdeWs(gson, fechaFinDatosValidos)) {
						addWarnMessage("Aval Buro", "No se pudo recuperar el JSON para la cédula consultada.");
						return;
					}
				}
				
				validaFechaConsultaAvalBuro(gson, fecActual, fechaFinDatosValidos);
				if (consultaResponse == null) {
					return;
				}
				
				if ("C".equals(tipoCliente)){					
					mostrarPantallaRuc = false;				
				} else if("R".equals(tipoCliente)) {
					if(consultaResponse.getResult().getDatosGeneralesEmpresa() == null || consultaResponse.getResult().getDatosGeneralesEmpresa().isEmpty()) {						
						mostrarPantallaRuc = false;
					} else {						
						mostrarPantallaRuc = true;
					}
									
				} 
								
				generacionReporte(consultaResponse, "S");	
				
			} catch (MalformedURLException e) {
				LOG.error("Error al formar la url de consulta: " + e);				
			} catch (ProtocolException e) {
				LOG.error("Error en el protocolo: " + e);
			} catch (ParseException e) {
				LOG.error("Error al cambiar la fecha: " + e);
			} catch (IOException e) {
				LOG.error("Error en la entrada o salida de datos: " + e);
			}						 
					
		} else {		
			try {
				
				consultarWSReporteAvalBuro(gson, fechaFinDatosValidos);
				
			} catch (MalformedURLException e) {
				LOG.error("Error al formar la url de consulta: " + e);				
			} catch (ProtocolException e) {
				LOG.error("Error en el protocolo: " + e);
			} catch (ParseException e) {
				LOG.error("Error al cambiar la fecha: " + e);
			} catch (IOException e) {
				LOG.error("Error en la entrada o salida de datos: " + e);
			}	
		}
	}

	private void validaFechaConsultaAvalBuro(Gson gson, LocalDateTime fecActual, Date fechaFinDatosValidos)
			throws ParseException, MalformedURLException, ProtocolException, IOException {
		if (fechaFinPantalla == null) {
			if (!refrescarJsonLegacyDesdeWs(gson, fechaFinDatosValidos)) {
				addWarnMessage("Aval Buro", "No existe una fecha de vigencia válida para la consulta almacenada.");
				return;
			}
		}

		if (fechaFinPantalla == null) {
			addWarnMessage("Aval Buro", "No se pudo determinar la vigencia de la consulta almacenada.");
			return;
		}

		LocalDateTime fecFinAvalBuroDaseDatos = fechaFinPantalla.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		
		if(fecFinAvalBuroDaseDatos.isBefore(fecActual) || fecFinAvalBuroDaseDatos.isEqual(fecActual)){
			if(fecFinAvalBuroDaseDatos.getMonth() == fecActual.getMonth()) {
				if(fecActual.getDayOfMonth() >= 15 ) {
					consultarWSReporteAvalBuro(gson, fechaFinDatosValidos);
					existeHistorialCrediticioPorCedula();
				}
			}						
		}
		
		if(fecActual.isAfter(fecFinAvalBuroDaseDatos) || fecActual.isEqual(fecFinAvalBuroDaseDatos)) {
				consultarWSReporteAvalBuro(gson, fechaFinDatosValidos);
				existeHistorialCrediticioPorCedula();		
		}

		if (!tieneJsonLegacyDisponible(existeCedula) && !refrescarJsonLegacyDesdeWs(gson, fechaFinDatosValidos)) {
			addWarnMessage("Aval Buro", "No existe JSON legado disponible para generar el reporte.");
			return;
		}

		String jsonString = normalizarJson(existeCedula != null ? existeCedula.getJson() : null);
		if (jsonString == null || jsonString.trim().isEmpty()) {
			addWarnMessage("Aval Buro", "No existe información JSON almacenada para la consulta.");
			return;
		}

		JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
		if (jsonObject == null) {
			addWarnMessage("Aval Buro", "No se pudo interpretar el JSON almacenado de la consulta.");
			return;
		}

		consultaResponse = gson.fromJson(jsonObject, ConsultaResponse.class);
	}

	private void consultarWSReporteAvalBuro(Gson gson, Date fechaFinDatosValidos) throws ParseException, MalformedURLException, ProtocolException, IOException {
		if(!"04".equals(getCompania().getNoCia())) {
			int responseCode = conexionWsConsultaLocal();

			if(responseCode == 200) {
				if (consultaResponseExternal == null || consultaResponseExternal.getFechaFin() == null) {
					consumirWsDirectoYGuardar(gson, fechaFinDatosValidos);
					return;
				}

				String jsonLegacyLocal = normalizarJson(consultaResponseExternal.getJson());
				if (jsonLegacyLocal == null || jsonLegacyLocal.trim().isEmpty()) {
					LOG.warn("Registro encontrado en consulta local sin JSON legado. Se consulta WS directo para: "
							+ identificacionCliente);
					consumirWsDirectoYGuardar(gson, fechaFinDatosValidos);
					return;
				}

				LocalDateTime fecActual = fechaActual.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				LocalDateTime fecFinAvalBuroDaseDatos = FechaUtils
						.convertirStrFecha(consultaResponseExternal.getFechaFin(), "yyyy-MM-dd")
						.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

				if(fecActual.isAfter(fecFinAvalBuroDaseDatos) || fecActual.isEqual(fecFinAvalBuroDaseDatos)) {
					consumirWsDirectoYGuardar(gson, fechaFinDatosValidos);
				} else {
					generaReporteClienteExistente(gson);
				}
			} else if(responseCode == 500) {
				addErrorMessage("Error interno del servidor", "Error al convertir la cadena JSON");
			} else if(responseCode == 404) {
				consumirWsDirectoYGuardar(gson, fechaFinDatosValidos);
			}

		} else {
			consultaWsAvalBuro(gson, fechaFinDatosValidos);
		}
	}

	private void consumirWsDirectoYGuardar(Gson gson, Date fechaFinDatosValidos)
			throws ParseException, MalformedURLException, IOException, ProtocolException {
		consultaDirectaAvaBuro();

		if (consultaResponseGuardar == null) {
			addErrorMessage("Aval Buro", "No fue posible obtener respuesta del servicio de Aval Buro.");
			return;
		}

		if("A200".equals(consultaResponseGuardar.getResponseCode())) {
			LOG.info("Conexion fuera de Centric: " +  mensajeConexion);
			if(guardarDatosBaseLocalWs(gson, fechaFinDatosValidos)) {
				generacionReporte(consultaResponseGuardar, "N");
			}
		} else if ("A401".equals(consultaResponseGuardar.getResponseCode())) {
			addErrorMessage("Aval Buro", "No se envió la cabecera de autorización");
		} else if ("A402".equals(consultaResponseGuardar.getResponseCode())) {
			addErrorMessage("Aval Buro", "El usuario/clave ingresados son incorrectos");
		} else if ("A410".equals(consultaResponseGuardar.getResponseCode())) {
			addErrorMessage("Aval Buro", "Se debe enviar la identificación y el tipo de identificación en el formato requerido para realizar la consulta");
		} else if ("A405".equals(consultaResponseGuardar.getResponseCode())) {
			addErrorMessage("Aval Buro", "No se encuentra información crediticia en la base a AVAL Buró");
		} else if ("A406".equals(consultaResponseGuardar.getResponseCode())) {
			addErrorMessage("Aval Buro", "Cliente al que pertenece no se encuentra activo");
		} else if ("A407".equals(consultaResponseGuardar.getResponseCode())) {
			addErrorMessage("Aval Buro", "El producto no se encuentra activo");
		} else if ("A408".equals(consultaResponseGuardar.getResponseCode())) {
			addErrorMessage("Aval Buro", "No está autorizado para usar este producto o su contrato/plan no está vigente\"");
		} else {
			addWarnMessage("Aval Buro", mensajeConexion != null ? mensajeConexion : "Respuesta no controlada del servicio.");
		}
	}

	private boolean guardarDatosBaseLocalWs(Gson gson, Date fechaFinDatosValidos)
			throws MalformedURLException, IOException, ProtocolException {
		boolean exiteCliente = false;
		CxcAvalBuroJson cxcAvalBuro = setearEnvioDatosGuardar(gson, fechaFinDatosValidos);
		if(cxcAvalBuro != null) {
			exiteCliente = true;
			Gson gsonAvlaBuro = new Gson();
			String jsonBody = gsonAvlaBuro.toJson(cxcAvalBuro);
			List<ParamDet> urlWSlocalGuardar = paramDetService.buscarTiposIdentificacion(noCia, "URLWSA");
			
			//Consumo servicio para guardar datos en la base de datos de centric
			String urlGuardar = urlWSlocalGuardar.get(0).getTexto1() + "guardarclienteavalburo";
			URL urlRestGuardar = new URL(urlGuardar);

			LOG.info("urlWSGuardar: " + urlGuardar);

			connectionGuardar = (HttpURLConnection) urlRestGuardar.openConnection();
			connectionGuardar.setDoOutput(true);
			connectionGuardar.setRequestMethod("POST");
			connectionGuardar.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

			try {
				OutputStream datosEntrada = connectionGuardar.getOutputStream();
				datosEntrada.write(jsonBody.getBytes("UTF-8"));
				datosEntrada.flush();
			}catch (Exception e) {
				LOG.error(e);
			}

			StringBuilder resultadoRestService = new StringBuilder();
			try {
				BufferedReader datosRecibidos = new BufferedReader(new InputStreamReader(connectionGuardar.getInputStream()));
				String output;
				resultadoRestService = new StringBuilder();
				while ((output = datosRecibidos.readLine()) != null) {
					resultadoRestService.append(output);
				}

			}catch (Exception e) {
				LOG.error(e);
			}
			LOG.info("codigo response url de guardar : " + connectionGuardar.getResponseCode());
			connectionGuardar.disconnect();
		}
		return exiteCliente;
	}

	private CxcAvalBuroJson setearEnvioDatosGuardar(Gson gson, Date fechaFinDatosValidos) {
		CxcAvalBuroJson cxcAvalBuro = null;
		Date fechaConsulta = new Date();
		LocalDateTime fecActual = fechaConsulta.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();	        			        		
		Date fechaInicioDatosValidos = Date.from(fecActual.atZone(ZoneId.systemDefault()).toInstant());		
		String formatoJson = "yyyy-MM-dd";
		SimpleDateFormat sdfJson = new SimpleDateFormat(formatoJson);
		String fechaJsonIni = sdfJson.format(fechaInicioDatosValidos);
		String fechaJsonFin = sdfJson.format(fechaFinDatosValidos);
			      
		String nombreSujeto = "";
		if ("C".equals(tipoCliente)){
			if(!consultaResponseGuardar.getResult().getIdentificacionTitular().isEmpty() && consultaResponseGuardar.getResult().getIdentificacionTitular() != null) {
				nombreSujeto = consultaResponseGuardar.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
				mostrarPantallaRuc = false;
			}				
		} else if("R".equals(tipoCliente)) {
			if(consultaResponseGuardar.getResult().getDatosGeneralesEmpresa() == null || consultaResponseGuardar.getResult().getDatosGeneralesEmpresa().isEmpty()) {
				nombreSujeto = consultaResponseGuardar.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
				mostrarPantallaRuc = false;
			} else {
				nombreSujeto = consultaResponseGuardar.getResult().getDatosGeneralesEmpresa().get(0).getNombreRazonSocial();
				mostrarPantallaRuc = true;
			}
							
		}
			        							
		cxcAvalBuro = new CxcAvalBuroJson(identificacionCliente, nombreSujeto, fechaJsonIni, fechaJsonFin, getUsuario().getUsuario(), gson.toJson(consultaResponseGuardar));
				
		return cxcAvalBuro;
	}

	private void consultaDirectaAvaBuro() {
		//Consulta directa a aval buro
		MainEjecutarServicioConsultaAvalBuro consultaAvalBuro = new MainEjecutarServicioConsultaAvalBuro();		
		consultaResponseGuardar = consultaAvalBuro.consultarAvalBuro(noCia, tipoCliente, identificacionCliente);
		mensajeConexion =  consultaResponseGuardar.getMessage() == null ? "Error en la consulta en Aval Buro" : consultaResponseGuardar.getMessage();
	}

	private void generaReporteClienteExistente(Gson gson)
			throws UnsupportedEncodingException, IOException, ParseException {
		
		fechaIniPantalla = FechaUtils.convertirStrFecha(consultaResponseExternal.getFechaInicio(), "yyyy-MM-dd");
		fechaFinPantalla = FechaUtils.convertirStrFecha(consultaResponseExternal.getFechaFin(), "yyyy-MM-dd");
		nombreUsuarioConsulta = consultaResponseExternal.getUsuario();
		
		existe = Boolean.TRUE;
		
		String jsonString = normalizarJson(consultaResponseExternal != null ? consultaResponseExternal.getJson() : null);
		if (jsonString == null || jsonString.trim().isEmpty()) {
			addWarnMessage("Aval Buro", "No existe información JSON en el registro local para generar el reporte.");
			return;
		}

		JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);		
		if (jsonObject == null) {
			addWarnMessage("Aval Buro", "No se pudo interpretar el JSON del registro local.");
			return;
		}
			
		consultaResponse = gson.fromJson(jsonObject, ConsultaResponse.class);
		
		if ("C".equals(tipoCliente)){					
			mostrarPantallaRuc = false;				
		} else if("R".equals(tipoCliente)) {
			if(consultaResponse.getResult().getDatosGeneralesEmpresa() == null || consultaResponse.getResult().getDatosGeneralesEmpresa().isEmpty()) {						
				mostrarPantallaRuc = false;
			} else {						
				mostrarPantallaRuc = true;
			}
							
		} 
		
		generacionReporte(consultaResponse, "S");
	}

	private int conexionWsConsultaLocal() throws MalformedURLException, IOException, ProtocolException {
		int responseCode = 0;
		try {
			List<ParamDet> urlWSlocal = paramDetService.buscarTiposIdentificacion(noCia, "URLWSA");
			urlWSconsulta = urlWSlocal.get(0).getTexto1() + "consultaravalburo?";
			
			String parametros = "tipoIdentificacion="+ tipoCliente + "&cedula=" + identificacionCliente;
			String urlConsulta = urlWSconsulta + parametros;

			
			URL url = new URL(urlConsulta);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			responseCode = connection.getResponseCode();
			LOG.info("Código de respuesta: " + responseCode);
			
			if (responseCode == 200) {
			    // Leer el contenido del response (el JSON)
			    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			        String inputLine;
			        StringBuilder response = new StringBuilder();
			        while ((inputLine = in.readLine()) != null) {
			            response.append(inputLine);
			        }
			        // Aquí tienes el contenido del response en formato JSON
			        String jsonResponse = response.toString();
			        LOG.info("Contenido del response: " + jsonResponse);

			        // Si quieres convertirlo a un objeto Java
			        // Ejemplo usando Gson
			        Gson gson = new Gson();
			        consultaResponseExternal = gson.fromJson(jsonResponse, CxcAvalBuroJson.class);
			        LOG.info("Objeto parseado: " + consultaResponseExternal);
			    }
			} else {
			    // Leer el error si existe
			    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
			        String inputLine;
			        StringBuilder response = new StringBuilder();
			        while ((inputLine = in.readLine()) != null) {
			            response.append(inputLine);
			        }
			        LOG.error("Error en respuesta: " + response.toString());
			    }
			}

		} catch (Exception e) {
			LOG.error("Error al consultar el WS en centric: " + e);
		}
		return responseCode;
	}

	private void consultaWsAvalBuro(Gson gson, Date fechaFinDatosValidos) throws ParseException {
		MainEjecutarServicioConsultaAvalBuro consultaAvalBuro = new MainEjecutarServicioConsultaAvalBuro();		
		consultaResponse = consultaAvalBuro.consultarAvalBuro(noCia, tipoCliente, identificacionCliente);
		mensajeConexion =  consultaResponse.getMessage() == null ? "Error en la consulta en Aval Buro" : consultaResponse.getMessage();
				
		if("A200".equals(consultaResponse.getResponseCode())) {
			
			addInfoMessage("Conexion", mensajeConexion);
			
			generacionReporte(consultaResponse, "N");
				
			
			String nombreSujeto = "";						
			if ("C".equals(tipoCliente)){
				if(!consultaResponse.getResult().getIdentificacionTitular().isEmpty()) {
					nombreSujeto = consultaResponse.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
				} else {
					addErrorMessage("Cliente", "El cliente no tiene historial crediticio");
					return;
				}
								
			} else if("R".equals(tipoCliente)) {
				if(consultaResponse.getResult().getDatosGeneralesEmpresa().isEmpty() && consultaResponse.getResult().getIdentificacionTitular().isEmpty()) {
					addErrorMessage("Cliente", "El cliente no tiene historial crediticio");
					return;
				}
				if(consultaResponse.getResult().getDatosGeneralesEmpresa() == null || consultaResponse.getResult().getDatosGeneralesEmpresa().isEmpty()) {
					nombreSujeto = consultaResponse.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
				} else {
					nombreSujeto = consultaResponse.getResult().getDatosGeneralesEmpresa().get(0).getNombreRazonSocial();
				}								
			} 
									
			CxcAvalBuro cxcAvalBuro = new CxcAvalBuro(identificacionCliente, nombreSujeto, fechaActual, fechaFinDatosValidos, getUsuario().getUsuario(), gson.toJson(consultaResponse));
			cxcAvalBuroServiceLocal.grabarJsonAvalBuro(cxcAvalBuro);
			
		} else if ("A401".equals(consultaResponse.getResponseCode())) {
			addErrorMessage("Aval Buro", "No se envió la cabecera de autorización");
			return;
		} else if ("A402".equals(consultaResponse.getResponseCode())) {
			addErrorMessage("Aval Buro", "El usuario/clave ingresados son incorrectos");
			return;
		} else if ("A410".equals(consultaResponse.getResponseCode())) {
			addErrorMessage("Aval Buro", "Se debe enviar la identificación y el tipo de identificación en el formato requerido para realizar la consulta");
			return;
		} else if ("A405".equals(consultaResponse.getResponseCode())) {
			addErrorMessage("Aval Buro", "No se encuentra información crediticia en la base a AVAL Buró");
			return;
		} else if ("A406".equals(consultaResponse.getResponseCode())) {
			addErrorMessage("Aval Buro", "Cliente al que pertenece no se encuentra activo");
			return;
		} else if ("A407".equals(consultaResponse.getResponseCode())) {
			addErrorMessage("Aval Buro", "El producto no se encuentra activo");
			return;
		} else if ("A408".equals(consultaResponse.getResponseCode())) {
			addErrorMessage("Aval Buro", "No está autorizado para usar este producto o su contrato/plan no está vigente\"");
			return;
		} else {
			addInfoMessage("Conexion", mensajeConexion);
		}		
				
		LOG.info("Codigo de conexion: " + consultaResponse.getMessage());
	}

	public void consultaComoRuc() throws ParseException {		
		
		limpiarListas();		
		if(!isNullOrEmpty(consultaResponse.getResult().getInformacionComoRUC())){
			informacionComoRUC = consultaResponse.getResult().getInformacionComoRUC().get(0);
			tipoCliente = "R";
			identificacionCliente = informacionComoRUC.getIdentificacionSujeto(); 			
						
			consultarAvalBuro();
		}		
	}

	private void limpiarListas() {
		identificacionTitular = null;
		 datosGeneralesEmpresa = null;
		 informacionComoRUC = null; 
		 scoreFinanciero = null;
		 scoreEmpresa = null;
		 lstFactoresScore = null;
		 lstManejoCuentasCorrientes = null; 
		 lstDeudaVigenteTotal = null;
		 lstGastoFinanciero = null;
		 lstOperacionesCodeudorGarante = null;
		 lstInformacionComoRuc = null;
		 lstOperacionesVigentesTarjeta = null;
		 lstDetalleTarjetaSaldoVigente = null; 
		 lstIndicadorTarjeta = null;
		 lstOperacionVigenteBanco = null;
		 lstOperacionVigenteBancoFiltradoTitular = null;
		 lstOperacionVigenteBancoFiltradoCodeudor = null;
		 lstOperacionVigenteBancoFiltradoFinal = null;
		 lstDetalleTarjetaCredito = null;
		 lstTarjetasCreditoVigentesDTO = null;
		 lstSaldoPorVencerDto = null;
		 lstSaldoNoDevengaInteresDto = null;
		 lstSaldoVencidoDto = null;
		 lstEstructuraOperacionBancoDetalle = null;
		 estrucOperBancoDetSeleccionada = null;
		 lstOperacionVigenteCooperativa = null;
		 lstOperacionVigenteCooperativaFilterTitular = null;
		 lstOperacionVigenteCooperativaFilterCodeudor = null;
		 lstOperacionVigenteCooperativaFilterFinal = null;
		 lstEstructuraOperacionCooperativaDetalle = null;
		 estructuraOperacionCooperativaDetalle = null;
		 lstEvolucionScoreFinanciero = null;		
		 lstOperacionesVigentesCobranza = null;
		 lstOperacionesVigentesServicio = null;
		 lstOperacionesVigentesEmpresa = null;
		 lstIndicadoresDeuda = null;
		 lstOperacionHistoricaTarjeta = null;
		 lstOperacionHistoricaTarjetaFiltrada = null;
		 lstOperacionHistoricaBanco = null;
		 lstOperacionHistoricaBancoFiltrada = null;	
		 lstOperacionHistoricaCooperativa = null;
		 lstOperacionHistoricaCooperativaFiltrada = null;
		 lstOperacionHistoricaEmpresa = null;
		 lstOperacionHistoricaEmpresaFiltrada = null;
		 lstOperacionesHistoricasServicio = null;
		 lstOperacionesHistoricasServicioFiltrada = null;
		 lstOperacionHistoricaCobranza = null;
		 lstOperacionHistoricaCobranzaFiltrada = null;
		 lstRelacionEmpresas = null;
		 lstDatosContacto = null;
		 lstTitularConsultado = null;
		 lstSemaforoMaximoDiasVencido = null;
		 lstTendenciaDeuda = null;
		 lstDetalleEmpresas = null;
		 lstResumenPrincipalesCuentasFinancieras = null;
		 lstCuentasEstadosFinancieros = null;
		 lstIndicesFinancieros = null;
		 lstPrincipalesAccionistas = null;
		 lstRepresentantesLegales = null;
		 lstContactabilidad = null;		
		 score = null;
		 scoreEmpresaDonut = null; 
		 evolScoreFinanciero = null;
		 semaforoMaxDiasVenChart = null;
		 tendenciaDeudaChat = null;
		 lineModel = null;
		 lineModelSemaforo = null;
	}
	
	
	private void generacionReporte(ConsultaResponse consultaResponse, String vista) throws ParseException {
		mostrarPantallasConsulta = Boolean.TRUE;
		
		consultaIdentificacionTitular(consultaResponse);		
		consultaDatosGeneralesEmpresa(consultaResponse);
		consultaRucPersonal(consultaResponse);			
		consultaScoreFinanciero(consultaResponse);
		consultaScoreFinancieroEmpresa(consultaResponse);
		consultaFactoresInfluyenScore(consultaResponse);
		consultaManejoCuentasCorriente(consultaResponse);
		consultaDeudaVigenteTotal(consultaResponse);
		consultaGastoFinanciero(consultaResponse);
		consultaOperacionesCodeudorGarante(consultaResponse);
		consultaInformacionComoRuc(consultaResponse);
		consultaOperacionesVigentesTarjetas(consultaResponse, vista);
		consultaDetalleTatjetaSaldoVigente(consultaResponse, vista);
		consultarIndicadoresTarjetaCreditoVigentes(consultaResponse);
		consultarOperacionesVigentesBancos(consultaResponse);
		consutarDetalleTarjetaCredito(consultaResponse);
		consultarOperacionesVigentesCooperativas(consultaResponse);
		consultaEvolucionScoreFinanciero(consultaResponse, vista);
		consultarOperacionesVigentesEmpresasCobranza(consultaResponse);
		consultarOperacionesVigentesServicio(consultaResponse);
		consultarOperacionesVigentesEmpresa(consultaResponse);
		consultarIndicadoresDeudaUlti36Meses(consultaResponse);
		consultarOperacionesHistoricasTarjetasCredito(consultaResponse);
		consultarOperacionesHistoricasBancos(consultaResponse);
		consultarOperacionesHistoricasCooperativas(consultaResponse);
		consultarOperacionesHistoricasEmpresas(consultaResponse);
		consultarOperacionesHistoricasServicios(consultaResponse);
		consultarOperacionesHistoricasCobranzas(consultaResponse);
		consultarDatosContacto(consultaResponse);
		consultarTitularConsultado(consultaResponse);
		consultarDetalleEmpresa(consultaResponse);
		consultarResumenPrincipalCtasFinancieras(consultaResponse);
		consultarCuentasEstadosFinancieros(consultaResponse);
		consultarIndicesFinancieros(consultaResponse);
		consultarPrincipalesAccionistas(consultaResponse);
		consultarRepresentantesLegales(consultaResponse);
		consultarContactabilidad(consultaResponse);		
		consultarRelacionConEmpresas(consultaResponse);
		consultaSemaforoMaximoDiasVencidos(consultaResponse, vista);
		consultarTendenciaDeuda(consultaResponse, vista);
	}	

	private boolean existeHistorialCrediticioPorCedula() {
		existe = Boolean.FALSE;
		try {
			existeCedula = cxcAvalBuroServiceLocal.consultarJsonAvalBuro(identificacionCliente);	
			if (existeCedula != null) {
				fechaIniPantalla = existeCedula.getFechaInicio();
				fechaFinPantalla = existeCedula.getFechaFin();
				nombreUsuarioConsulta = existeCedula.getUsuario();
				existe = Boolean.TRUE;
			} else {
				mensajeNuevaBusqueda = "Es la primera vez que se consulta al cliente";
			}			
		} catch (FindException e) {			
			e.printStackTrace();
		}		
		return existe;
	}
	
	private void consultaIdentificacionTitular(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getIdentificacionTitular())) {
			identificacionTitular = consultaResponse.getResult().getIdentificacionTitular().get(0);
		}		
	}
	
	private void consultaDatosGeneralesEmpresa(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getDatosGeneralesEmpresa())) {
			datosGeneralesEmpresa = consultaResponse.getResult().getDatosGeneralesEmpresa().get(0);
		}
	}

	private void consultaRucPersonal(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getInformacionComoRUC())){
			informacionComoRUC = consultaResponse.getResult().getInformacionComoRUC().get(0);				
		}
	}

	private void consultaScoreFinanciero(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getScoreFinanciero())) {
			DecimalFormat df = new DecimalFormat("#.##");
			DecimalFormat dfInt = new DecimalFormat("#");
			scoreFinanciero = consultaResponse.getResult().getScoreFinanciero().get(0);				
			score = new DonutChartModel();				
			Map<String, Number> datos = new LinkedHashMap<String, Number>();				
			datos.put("Score", scoreFinanciero.getScore());
			datos.put("Riesgo", 1000 - scoreFinanciero.getScore());			
			score.addCircle(datos);
			score.setLegendPosition("ne");	
			score.setSliceMargin(7);
			score.setShowDataLabels(true);
			score.setShadow(true);
			score.setLegendRows(1);		
						
			scorePantalla = dfInt.format(scoreFinanciero.getScore());
			
			final HashMap<String, String> tooltip = new HashMap<>();
	        tooltip.put("text", "value");			
					
			Double pctjPeor = Double.parseDouble(scoreFinanciero.getTasaMalos()) * 100.0;
			porcentajePeorScore = scoreFinanciero.getClientesPeorScore() * 100 + " %";				
			porcentajeProbalidadCaerMora = df.format(pctjPeor) + " %";
			explicacion = "Una persona con puntaje de " + scoreFinanciero.getScore() + " tiene una probabilidad de " 
			+ porcentajeProbalidadCaerMora + " de caer en un vencimiento de 60 días o superior en los siguientes " 
			+ "12 meses, el " + porcentajePeorScore + " de la población crediticia ecuatoriana presenta un score "
			+ "inferior al de la persona consultada.";				
		}
	}		
	
	
	private void consultaScoreFinancieroEmpresa(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getScoreEmpresa() )) {
			DecimalFormat dfInt = new DecimalFormat("#");
			DecimalFormat df = new DecimalFormat("#.##");
			scoreEmpresa = consultaResponse.getResult().getScoreEmpresa().get(0);				
			scoreEmpresaDonut = new DonutChartModel();				
			Map<String, Number> datos = new LinkedHashMap<String, Number>();				
			datos.put("Score", scoreEmpresa.getScoreEmpresa());
			datos.put("Riesgo", 1000 - scoreEmpresa.getScoreEmpresa());			

			scoreEmpresaDonut.addCircle(datos);
			scoreEmpresaDonut.setLegendPosition("ne");	
			scoreEmpresaDonut.setSliceMargin(7);
			scoreEmpresaDonut.setShowDataLabels(true);
			scoreEmpresaDonut.setShadow(true);
			scoreEmpresaDonut.setLegendRows(1);		
						
			scorePantalla = dfInt.format(scoreEmpresa.getScoreEmpresa());
			
			final HashMap<String, String> tooltip = new HashMap<>();
	        tooltip.put("text", "value");			
					
			Double pctjPeor = Double.parseDouble(scoreEmpresa.getTasaMalos()) * 100.0;
			porcentajePeorScore = scoreEmpresa.getEmpresasPeorScore() * 100 + " %";				
			porcentajeProbalidadCaerMora = df.format(pctjPeor) + " %";
			explicacion = "Una Empresa con indicador de " + dfInt.format(scoreEmpresa.getScoreEmpresa()) + " tiene una probabilidad de " 
			+ porcentajeProbalidadCaerMora + " de caer en un vencimiento de 60 días o superior en los siguientes " 
			+ "12 meses, el " + porcentajePeorScore + " de las Empresas presentan un indicador "
			+ "inferior al de la empresa consultada.";
							
		}
	}		
	
	private void consultaFactoresInfluyenScore(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getFactoresScore())) {
			lstFactoresScore = consultaResponse.getResult().getFactoresScore();
		}	
	}
	
	private void consultaDeudaVigenteTotal(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getIndicadoresDeuda())) {
			lstDeudaVigenteTotal = consultaResponse.getResult().getDeudaVigenteTotal();
			totalSaldoVencer = lstDeudaVigenteTotal.stream().mapToDouble(DeudaVigenteTotal::getValorPorVencer).sum();
			totalSaldoNDI = lstDeudaVigenteTotal.stream().mapToDouble(DeudaVigenteTotal::getNoDevengaIntereses).sum();
			totalSaldoVenciado = lstDeudaVigenteTotal.stream().mapToDouble(DeudaVigenteTotal::getValorVencido).sum();
			totalDemandaJudicial = lstDeudaVigenteTotal.stream().mapToDouble(DeudaVigenteTotal::getValorDemandaJudicial).sum();
			totalCarteraCastigada = lstDeudaVigenteTotal.stream().mapToDouble(DeudaVigenteTotal::getCarteraCastigada).sum();
			totalDeudaTotal = lstDeudaVigenteTotal.stream().mapToDouble(DeudaVigenteTotal::getTotalDeuda).sum();
		}	
	}
	
	private void consultaManejoCuentasCorriente(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getManejoCuentasCorrientes())) {
			lstManejoCuentasCorrientes = consultaResponse.getResult().getManejoCuentasCorrientes();
		}		
	}
	
	private void consultaGastoFinanciero(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getGastoFinanciero())) {
			lstGastoFinanciero = consultaResponse.getResult().getGastoFinanciero();
		}		
	}
	
	private void consultaOperacionesCodeudorGarante(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesCodeudorGarante())) {
			lstOperacionesCodeudorGarante = consultaResponse.getResult().getOperacionesCodeudorGarante();
		}		
	}
	
	private void consultaInformacionComoRuc(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getInformacionComoRUC())) {
			lstInformacionComoRuc = consultaResponse.getResult().getInformacionComoRUC();
		}		
	}
	
	private void consultaOperacionesVigentesTarjetas(ConsultaResponse consultaResponse, String vista) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesVigentesTarjeta())) {
			lstOperacionesVigentesTarjeta = consultaResponse.getResult().getOperacionesVigentesTarjeta();			
			try {	            	                  
	            for(OperacionVigenteTarjeta ovt : lstOperacionesVigentesTarjeta) {
	            	if("N".equals(vista)) {
	            		ovt.setFechaCorte(formateoFecha(ovt.getFechaCorte()));
	            	}	            		            	
	            	//ovt.setPorcentajeUso(ovt.getPorcentajeUso() * 100);	   	            	
	            }	           
	            
	            this.setTjcConsumoMes(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getCapitalConsumo).sum());
	            this.setTjcSaldoTotal(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getSaldoTotal).sum());
	            this.setTjcSaldoVencer(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getCapitalxVencerTotal).sum());
	            this.setTjcSaldoVencido(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getSaldoVencido).sum());
	            this.setTjcSaldoNDI(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getValorNoDevengaInteresTotal).sum());
	            this.setTjcDemandaJudicial(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getValorDemandaJudicial).sum());
	            this.setTjcCarteraCastigada(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getCarteraCastigada).sum());
	            this.setTjcUltValorPagado(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getValorPagado).sum());
	            this.setTjcValorMinPagar(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getValorMinimoPagar).sum());
	            this.setTjcCuotaMensual(lstOperacionesVigentesTarjeta.stream().mapToDouble(OperacionVigenteTarjeta::getCuotaEstimadaTarjetas).sum());
	            
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }			
		}		
		
	}
	
	private void consultaDetalleTatjetaSaldoVigente(ConsultaResponse consultaResponse, String vista) {
		if(!isNullOrEmpty(consultaResponse.getResult().getDetalleTarjetaSaldoVigente())) {
			lstDetalleTarjetaSaldoVigente = consultaResponse.getResult().getDetalleTarjetaSaldoVigente();			
			try {	            	                  
	            for(DetalleTarjetaSaldoVigente det : lstDetalleTarjetaSaldoVigente) {
	            	if("N".equals(vista)) {
	            		det.setFechaCorte(formateoFecha(det.getFechaCorte()));
	            	}
	            }	            
	        } catch (Exception e) {
	            e.printStackTrace();
	        }			
		}		
		
	}
	
	private void consultarIndicadoresTarjetaCreditoVigentes(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getIndicadoresTarjeta())) {
			lstIndicadorTarjeta = consultaResponse.getResult().getIndicadoresTarjeta();							
		}	
	}
	
		
	private void consutarDetalleTarjetaCredito(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getDetalleTarjetaCredito())) {
			lstDetalleTarjetaCredito = consultaResponse.getResult().getDetalleTarjetaCredito();		
		}		
	}
	
	public void verDetalleTarjetaCredito(OperacionVigenteTarjeta operacionVigenteTarjeta) {
		if(lstDetalleTarjetaCredito != null && lstDetalleTarjetaCredito.size() > 0) {			
			
			detalleTarjetaCreditoSeleccionada = lstDetalleTarjetaCredito.stream()
					.filter(d -> operacionVigenteTarjeta.getCodigoInstitucionFinanciera()
							.equals(d.getCodigoInstitucionFinanciera())
							&& operacionVigenteTarjeta.getMarcaTarjeta().equals(d.getMarcaTarjeta()))
					.findFirst().orElse(null);
						
			consultaTarjetaCreditoVigentesDto(operacionVigenteTarjeta);
			consultaSaldoPorVencerDto(operacionVigenteTarjeta);
			consultaSaldoNoDevengaIntereseDto(operacionVigenteTarjeta);
			consultaSaldoVencidoDto(operacionVigenteTarjeta);
		}
	}

	private void consultaTarjetaCreditoVigentesDto(OperacionVigenteTarjeta operacionVigenteTarjeta) {
		TarjetasCreditoVigentesDTO tarjetasCreditoVigentesDTO = null;
		lstTarjetasCreditoVigentesDTO = new ArrayList<TarjetasCreditoVigentesDTO>();
		for(DetalleTarjetaCredito dtc : lstDetalleTarjetaCredito) {
			if(operacionVigenteTarjeta.getCodigoInstitucionFinanciera().equals(dtc.getCodigoInstitucionFinanciera()) 
					&& operacionVigenteTarjeta.getMarcaTarjeta().equals(dtc.getMarcaTarjeta())) {					
				tarjetasCreditoVigentesDTO = new TarjetasCreditoVigentesDTO(dtc.getFormaPagoDescripcion(), dtc.getFechaEmisión(),
						dtc.getFechaVencimiento(), dtc.getFechaCancelacion(), dtc.getEstadoOperacionDescripcion(),
						dtc.getSaldoTotal(), dtc.getCapitalxVencerTotal(), dtc.getSaldoVencido(), 
						dtc.getValorNoDevengaInteresTotal(), dtc.getValorDemandaJudicial(), dtc.getCarteraCastigada(),
						dtc.getDiasMorosidad(), dtc.getValorPagado(), dtc.getValorMinimoPagar());
				lstTarjetasCreditoVigentesDTO.add(tarjetasCreditoVigentesDTO);
			}
		}
		if(lstTarjetasCreditoVigentesDTO.size() > 0) {
			this.setSaldoTotal(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getSaldoTotal).sum());
			this.setCapitalxVencerTotal(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getCapitalxVencerTotal).sum());
			this.setSaldoVencido(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getSaldoVencido).sum());
			this.setValorNoDevengaInteresTotal(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getValorNoDevengaInteresTotal).sum());
			this.setValorDemandaJudicial(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getValorDemandaJudicial).sum());
			this.setCarteraCastigada(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getCarteraCastigada).sum());
			this.setDiasMorosidad(lstTarjetasCreditoVigentesDTO.stream().mapToInt(TarjetasCreditoVigentesDTO::getDiasMorosidad).sum());
			this.setValorPagado(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getValorPagado).sum());
			this.setValorMinimoPagar(lstTarjetasCreditoVigentesDTO.stream().mapToDouble(TarjetasCreditoVigentesDTO::getValorMinimoPagar).sum());
		}
	}
	
	private void consultaSaldoPorVencerDto(OperacionVigenteTarjeta operacionVigenteTarjeta) {
		SaldoPorVencerDto saldoPorVencerDto = null;
		lstSaldoPorVencerDto = new ArrayList<SaldoPorVencerDto>();
		for(DetalleTarjetaCredito dtc : lstDetalleTarjetaCredito) {
			if(operacionVigenteTarjeta.getCodigoInstitucionFinanciera().equals(dtc.getCodigoInstitucionFinanciera()) 
					&& operacionVigenteTarjeta.getMarcaTarjeta().equals(dtc.getMarcaTarjeta())) {
				saldoPorVencerDto = new SaldoPorVencerDto(dtc.getFormaPagoDescripcion(), dtc.getCapitalxVencer1a30(), 
						dtc.getCapitalxVencer31a90(), dtc.getCapitalxVencer91a180(), dtc.getCapitalxVencer181a360(), 
						dtc.getCapitalxVencerMas360());
				lstSaldoPorVencerDto.add(saldoPorVencerDto);
			}
		}
		if(lstSaldoPorVencerDto.size() > 0) {
			this.setCapitalxVencer1a30(lstSaldoPorVencerDto.stream().mapToDouble(SaldoPorVencerDto::getCapitalxVencer1a30).sum());
			this.setCapitalxVencer31a90(lstSaldoPorVencerDto.stream().mapToDouble(SaldoPorVencerDto::getCapitalxVencer31a90).sum());
			this.setCapitalxVencer91a180(lstSaldoPorVencerDto.stream().mapToDouble(SaldoPorVencerDto::getCapitalxVencer91a180).sum());
			this.setCapitalxVencer181a360(lstSaldoPorVencerDto.stream().mapToDouble(SaldoPorVencerDto::getCapitalxVencer181a360).sum());
			this.setCapitalxVencerMas360(lstSaldoPorVencerDto.stream().mapToDouble(SaldoPorVencerDto::getCapitalxVencerMas360).sum());
		}
	}
	
	private void consultaSaldoNoDevengaIntereseDto(OperacionVigenteTarjeta operacionVigenteTarjeta) {
		SaldoNoDevengaInteresDto saldoNoDevengaInteresDto = null;
		lstSaldoNoDevengaInteresDto = new ArrayList<SaldoNoDevengaInteresDto>();
		for(DetalleTarjetaCredito dtc : lstDetalleTarjetaCredito) {
			if(operacionVigenteTarjeta.getCodigoInstitucionFinanciera().equals(dtc.getCodigoInstitucionFinanciera()) 
					&& operacionVigenteTarjeta.getMarcaTarjeta().equals(dtc.getMarcaTarjeta())) {
				saldoNoDevengaInteresDto = new SaldoNoDevengaInteresDto(dtc.getFormaPagoDescripcion(),
						dtc.getValorNoDevengaInteres1a30(),
						dtc.getValorNoDevengaInteres31a90(), dtc.getValorNoDevengaInteres91a180(),
						dtc.getValorNoDevengaInteres181a360(), dtc.getValorNoDevengaInteresMas360());
				lstSaldoNoDevengaInteresDto.add(saldoNoDevengaInteresDto);
			}
		}
		if(lstSaldoNoDevengaInteresDto.size() > 0) {
			this.setValorNoDevengaInteres1a30(lstSaldoNoDevengaInteresDto.stream().mapToDouble(SaldoNoDevengaInteresDto::getValorNoDevengaInteres1a30).sum());
			this.setValorNoDevengaInteres31a90(lstSaldoNoDevengaInteresDto.stream().mapToDouble(SaldoNoDevengaInteresDto::getValorNoDevengaInteres31a90).sum());
			this.setValorNoDevengaInteres91a180(lstSaldoNoDevengaInteresDto.stream().mapToDouble(SaldoNoDevengaInteresDto::getValorNoDevengaInteres91a180).sum());
			this.setValorNoDevengaInteres181a360(lstSaldoNoDevengaInteresDto.stream().mapToDouble(SaldoNoDevengaInteresDto::getValorNoDevengaInteres181a360).sum());
			this.setValorNoDevengaInteresMas360(lstSaldoNoDevengaInteresDto.stream().mapToDouble(SaldoNoDevengaInteresDto::getValorNoDevengaInteresMas360).sum());
		}
	}
	
	private void consultaSaldoVencidoDto(OperacionVigenteTarjeta operacionVigenteTarjeta) {
		SaldoVencidoDto saldoVencidoDto = null;
		lstSaldoVencidoDto = new ArrayList<SaldoVencidoDto>();
		for(DetalleTarjetaCredito dtc : lstDetalleTarjetaCredito) {
			if(operacionVigenteTarjeta.getCodigoInstitucionFinanciera().equals(dtc.getCodigoInstitucionFinanciera()) 
					&& operacionVigenteTarjeta.getMarcaTarjeta().equals(dtc.getMarcaTarjeta())) {
				saldoVencidoDto = new SaldoVencidoDto(dtc.getFormaPagoDescripcion(), dtc.getCapitalVencido1a30(),
						dtc.getCapitalVencido31a90(), dtc.getCapitalVencido91a180(), dtc.getCapitalVencido181a360(),
						dtc.getCapitalVencidoMas360(), dtc.getCapitalVencido181a270(), dtc.getCapitalVencidoMas270(),
						dtc.getInteresVencido1a30(), dtc.getInteresVencido31a60(), dtc.getInteresVencido61a90(),
						dtc.getInteresVencido91a180(), dtc.getInteresVencido181a270(), dtc.getInteresVencidoMas270(),
						dtc.getInteresSobreMora(), dtc.getTotalCostoOperativoVencido());
				lstSaldoVencidoDto.add(saldoVencidoDto);
			}
		}
		if(lstSaldoVencidoDto.size() > 0) {
			this.setValorNoDevengaInteres1a30(lstSaldoNoDevengaInteresDto.stream().mapToDouble(SaldoNoDevengaInteresDto::getValorNoDevengaInteres1a30).sum());
			this.setCapitalVencido1a30(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getCapitalVencido1a30).sum());
			this.setCapitalVencido31a90(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getCapitalVencido31a90).sum());
			this.setCapitalVencido91a180(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getCapitalVencido91a180).sum());
			this.setCapitalVencido181a360(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getCapitalVencido181a360).sum());
			this.setCapitalVencidoMas360(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getCapitalVencidoMas360).sum());
			this.setCapitalVencido181a270(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getCapitalVencido181a270).sum());
			this.setCapitalVencidoMas270(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getCapitalVencidoMas270).sum());
			this.setInteresVencido1a30(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getInteresVencido1a30).sum());
			this.setInteresVencido31a60(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getInteresVencido31a60).sum());
			this.setInteresVencido61a90(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getInteresVencido61a90).sum());
			this.setInteresVencido91a180(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getInteresVencido91a180).sum());
			this.setInteresVencido181a270(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getInteresVencido181a270).sum());
			this.setInteresVencidoMas270(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getInteresVencidoMas270).sum());
			this.setInteresSobreMora(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getInteresSobreMora).sum());
			this.setTotalCostoOperativoVencido(lstSaldoVencidoDto.stream().mapToDouble(SaldoVencidoDto::getTotalCostoOperativoVencido).sum());
		}
	}
	
	private void consultarOperacionesVigentesBancos(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesVigentesBanco())) {
			lstOperacionVigenteBanco = consultaResponse.getResult().getOperacionesVigentesBanco();
			if(lstOperacionVigenteBanco.size() > 0) {
				lstEstructuraOperacionBancoDetalle = consultaResponse.getResult().getEstructuraOperacionBancosDetalle();				
				lstOperacionVigenteBancoFiltradoFinal = new ArrayList<OperacionVigenteBanco>();
				lstOperacionVigenteBancoFiltradoTitular = new ArrayList<OperacionVigenteBanco>();
				lstOperacionVigenteBancoFiltradoCodeudor = new ArrayList<OperacionVigenteBanco>();
				if(esTitular && esCodeudor) {
					lstOperacionVigenteBancoFiltradoTitular = lstOperacionVigenteBanco.stream()
							.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
							.collect(Collectors.toList());
					lstOperacionVigenteBancoFiltradoCodeudor = lstOperacionVigenteBanco.stream()
							.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
							.collect(Collectors.toList());
				}
				lstOperacionVigenteBancoFiltradoFinal.addAll(lstOperacionVigenteBancoFiltradoTitular);
				lstOperacionVigenteBancoFiltradoFinal.addAll(lstOperacionVigenteBancoFiltradoCodeudor);
				
				sumatoriaValoresBancos();
			}					
		}	
	}
	
	public void cambiarTipoDeudorOperacionesVigentesBancos() {
		lstOperacionVigenteBancoFiltradoFinal = new ArrayList<OperacionVigenteBanco>();
		lstOperacionVigenteBancoFiltradoTitular = new ArrayList<OperacionVigenteBanco>();
		lstOperacionVigenteBancoFiltradoCodeudor = new ArrayList<OperacionVigenteBanco>();
		if(esTitular && !esCodeudor) {
			lstOperacionVigenteBancoFiltradoTitular = lstOperacionVigenteBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstOperacionVigenteBancoFiltradoFinal.addAll(lstOperacionVigenteBancoFiltradoTitular);
			encerarValoresBancos();
		}
		if(!esTitular && esCodeudor) {
			lstOperacionVigenteBancoFiltradoCodeudor = lstOperacionVigenteBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionVigenteBancoFiltradoFinal.addAll(lstOperacionVigenteBancoFiltradoCodeudor);
			encerarValoresBancos();
		}
		if(esTitular && esCodeudor) {
			lstOperacionVigenteBancoFiltradoTitular = lstOperacionVigenteBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstOperacionVigenteBancoFiltradoCodeudor = lstOperacionVigenteBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionVigenteBancoFiltradoFinal.addAll(lstOperacionVigenteBancoFiltradoTitular);
			lstOperacionVigenteBancoFiltradoFinal.addAll(lstOperacionVigenteBancoFiltradoCodeudor);
			encerarValoresBancos();
		}		
		
		sumatoriaValoresBancos();
				
		if(!esTitular && !esCodeudor) {
			addWarnMessage("Filtro", "Active al menos un filtro");
		}
	}
	
	private void encerarValoresBancos() {
		bcMontoOriginal = 0;
		bcSaldoTotal = 0;		
		bcSaldoVencer = 0;
		bcSaldoVencido = 0;
		bcSaldoNDI = 0;
		bcDemandaJudicial = 0;
		bcCarteraCastigada = 0;
		bcCuotaMensual = 0;
	}
	
	private void sumatoriaValoresBancos() {
		bcMontoOriginal = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getValorOperacion).sum();
		bcSaldoTotal = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getSaldoTotalCalculado).sum();		
		bcSaldoVencer = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getValorxVencerTotal).sum();
		bcSaldoVencido = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getValorVencidoTotal).sum();
		bcSaldoNDI = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getValorNoDevengaInteresTotal).sum();
		bcDemandaJudicial = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getValorDemandaJudicial).sum();
		bcCarteraCastigada = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getCarteraCastigada).sum();
		bcCuotaMensual = lstOperacionVigenteBancoFiltradoFinal.stream().mapToDouble(OperacionVigenteBanco::getCuotaEstimadaOperacion).sum();
	}
	
	
	public void consultarEstructuraOperacionBancoDetalle(OperacionVigenteBanco operacionVigenteBanco) {
		if(lstEstructuraOperacionBancoDetalle != null && lstEstructuraOperacionBancoDetalle.size() > 0) {
			estrucOperBancoDetSeleccionada = new EstructuraOperacionBancoDetalle();
			
			estrucOperBancoDetSeleccionada = lstEstructuraOperacionBancoDetalle.stream()
					.filter(e -> operacionVigenteBanco.getCodigoInstitucionFinanciera()
							.equals(e.getCodigoInstitucionFinanciera())
							&& operacionVigenteBanco.getSistemaCrediticio().equals(e.getSistemaCrediticio())
							&& operacionVigenteBanco.getNumeroOperacion().equals(e.getNumeroOperacion()))
					.findFirst().orElse(null);						
		}				
	}
	
	private void consultarOperacionesVigentesCooperativas(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesVigentesCooperativa())) {
			lstOperacionVigenteCooperativa = consultaResponse.getResult().getOperacionesVigentesCooperativa();
			if(lstOperacionVigenteCooperativa.size() > 0) {
				lstEstructuraOperacionCooperativaDetalle = consultaResponse.getResult().getEstructuraOperacionCooperativaDetalle();				
				lstOperacionVigenteCooperativaFilterFinal = new ArrayList<OperacionVigenteCooperativa>();
				lstOperacionVigenteCooperativaFilterTitular = new ArrayList<OperacionVigenteCooperativa>();
				lstOperacionVigenteCooperativaFilterCodeudor = new ArrayList<OperacionVigenteCooperativa>();
				if(esTitularCoop && esCodeudorCoop) {
					lstOperacionVigenteCooperativaFilterTitular = lstOperacionVigenteCooperativa.stream()
							.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
							.collect(Collectors.toList());
					lstOperacionVigenteCooperativaFilterCodeudor = lstOperacionVigenteCooperativa.stream()
							.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
							.collect(Collectors.toList());
				}
				lstOperacionVigenteCooperativaFilterFinal.addAll(lstOperacionVigenteCooperativaFilterTitular);
				lstOperacionVigenteCooperativaFilterFinal.addAll(lstOperacionVigenteCooperativaFilterCodeudor);
				
				sumatoriaValoresCooperativas();
			}				
		}
	}
	
	public void cambiarTipoDeudorOperacionesVigentesCooperativas() {
		lstOperacionVigenteCooperativaFilterFinal = new ArrayList<OperacionVigenteCooperativa>();
		lstOperacionVigenteCooperativaFilterTitular = new ArrayList<OperacionVigenteCooperativa>();
		lstOperacionVigenteCooperativaFilterCodeudor = new ArrayList<OperacionVigenteCooperativa>();
		if(esTitularCoop && !esCodeudorCoop) {
			lstOperacionVigenteCooperativaFilterTitular = lstOperacionVigenteCooperativa.stream()
					.filter(operacionVigenteCooperativa -> operacionVigenteCooperativa.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstOperacionVigenteCooperativaFilterFinal.addAll(lstOperacionVigenteCooperativaFilterTitular);
			encerarValoresCooperativas();
		}
		if(!esTitularCoop && esCodeudorCoop) {
			lstOperacionVigenteCooperativaFilterCodeudor = lstOperacionVigenteCooperativa.stream()
					.filter(operacionVigenteCooperativa -> operacionVigenteCooperativa.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionVigenteCooperativaFilterFinal.addAll(lstOperacionVigenteCooperativaFilterCodeudor);
			encerarValoresCooperativas();
		}
		if(esTitularCoop && esCodeudorCoop) {
			lstOperacionVigenteCooperativaFilterTitular = lstOperacionVigenteCooperativa.stream()
					.filter(operacionVigenteCooperativa -> operacionVigenteCooperativa.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstOperacionVigenteCooperativaFilterCodeudor = lstOperacionVigenteCooperativa.stream()
					.filter(operacionVigenteCooperativa -> operacionVigenteCooperativa.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionVigenteCooperativaFilterFinal.addAll(lstOperacionVigenteCooperativaFilterTitular);
			lstOperacionVigenteCooperativaFilterFinal.addAll(lstOperacionVigenteCooperativaFilterCodeudor);
			encerarValoresCooperativas();
		}		
		
		sumatoriaValoresCooperativas();
		
		if(!esTitularCoop && !esCodeudorCoop) {
			addWarnMessage("Filtro", "Active al menos un filtro");
		}
	}
	
	private void encerarValoresCooperativas() {
		coopMontoOriginal = 0;
		coopSaldoTotal = 0;		
		coopSaldoVencer = 0;
		coopSaldoVencido = 0;
		coopSaldoNDI = 0;
		coopDemandaJudicial = 0;
		coopCarteraCastigada = 0;
		coopCuotaMensual = 0;
	}
	
	private void sumatoriaValoresCooperativas() {
		coopMontoOriginal = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getValorOperacion).sum();
		coopSaldoTotal = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getSaldoTotalCalculado).sum();		
		coopSaldoVencer = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getValorxVencerTotal).sum();
		coopSaldoVencido = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getValorVencidoTotal).sum();
		coopSaldoNDI = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getValorNoDevengaInteresTotal).sum();
		coopDemandaJudicial = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getValorDemandaJudicial).sum();
		coopCarteraCastigada = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getCarteraCastigada).sum();
		coopCuotaMensual = lstOperacionVigenteCooperativaFilterFinal.stream().mapToDouble(OperacionVigenteCooperativa::getCuotaEstimadaOperacion).sum();
	}
	
	public void consultarEstructuraOperacionCooperativaDetalle(OperacionVigenteCooperativa operacionVigenteCooperativa) {
		if(lstEstructuraOperacionCooperativaDetalle != null && lstEstructuraOperacionCooperativaDetalle.size() > 0) {
			estructuraOperacionCooperativaDetalle = new EstructuraOperacionCooperativaDetalle();
			
			estructuraOperacionCooperativaDetalle = lstEstructuraOperacionCooperativaDetalle.stream()
					.filter(e -> operacionVigenteCooperativa.getCodigoInstitucionFinanciera()
							.equals(e.getCodigoInstitucionFinanciera())
							&& operacionVigenteCooperativa.getSistemaCrediticio().equals(e.getSistemaCrediticio())
							&& operacionVigenteCooperativa.getNumeroOperacion().equals(e.getNumeroOperacion()))
					.findFirst().orElse(null);						
		}				
	}
	
	public void consultarOperacionesVigentesEmpresa(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesVigentesEmpresa())) {
			lstOperacionesVigentesEmpresa = new ArrayList<OperacionesVigentesEmpresa>();
			lstOperacionesVigentesEmpresa.addAll(consultaResponse.getResult().getOperacionesVigentesEmpresa());
		
			empSaldoActual = lstOperacionesVigentesEmpresa.stream().mapToDouble(OperacionesVigentesEmpresa::getSaldoTotalCalculado).sum();
			empCuotaMensual = lstOperacionesVigentesEmpresa.stream().mapToDouble(OperacionesVigentesEmpresa::getCuotaEstimadaOperacion).sum();
			empSaldoVencer = lstOperacionesVigentesEmpresa.stream().mapToDouble(OperacionesVigentesEmpresa::getValorxVencerTotal).sum();
			empSaldoVencido = lstOperacionesVigentesEmpresa.stream().mapToDouble(OperacionesVigentesEmpresa::getValorVencidoTotal).sum();
			empSaldoNDI = lstOperacionesVigentesEmpresa.stream().mapToDouble(OperacionesVigentesEmpresa::getValorNoDevengaInteresTotal).sum();
			empSaldoDemandaJudicial = lstOperacionesVigentesEmpresa.stream().mapToDouble(OperacionesVigentesEmpresa::getValorDemandaJudicial).sum();
			empCarteraCastigada = lstOperacionesVigentesEmpresa.stream().mapToDouble(OperacionesVigentesEmpresa::getCarteraCastigada).sum();			
		}		
		
	}
	
	public void consultarOperacionesVigentesServicio(ConsultaResponse consultaResponse) {		
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesVigentesServicio())) {
			lstOperacionesVigentesServicio = new ArrayList<OperacionesVigentesServicio>();
			lstOperacionesVigentesServicio.addAll(consultaResponse.getResult().getOperacionesVigentesServicio());
		
			servCuotaMensual = lstOperacionesVigentesServicio.stream().mapToDouble(OperacionesVigentesServicio::getCuotaEstimadaOperacion).sum();
			servSaldoVencer = lstOperacionesVigentesServicio.stream().mapToDouble(OperacionesVigentesServicio::getValorPorVencer).sum();
			servSaldoActual = lstOperacionesVigentesServicio.stream().mapToDouble(OperacionesVigentesServicio::getTotalDeuda).sum();
			servSaldoVencido = lstOperacionesVigentesServicio.stream().mapToDouble(OperacionesVigentesServicio::getValorVencido).sum();
			
		}		
	}
	
	public void consultarOperacionesVigentesEmpresasCobranza(ConsultaResponse consultaResponse) {		
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesVigentesCobranza())) {
			lstOperacionesVigentesCobranza = new ArrayList<OperacionesVigentesCobranza>();
			lstOperacionesVigentesCobranza.addAll(consultaResponse.getResult().getOperacionesVigentesCobranza());
		
			cobzMontoOriginal = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getMontoInicialCasaCobranza).sum();
			cobzSaldoTotal = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getSaldoDeuda).sum();
			cobzSaldoVencer = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getValorPorVencer).sum();
			cobzSaldoVencido = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getValorVencido).sum();
			cobzSaldoNDI = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getValorNoDevengaInteres).sum();
			cobzDemandaJudicial = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getDemandaJudicial).sum();
			cobzCarteraCastigada = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getCarteraCastigada).sum();
			cobzCuotaEstimada = lstOperacionesVigentesCobranza.stream().mapToDouble(OperacionesVigentesCobranza::getCuotaEstimadaOperacion).sum();
		}
	}
	
				
	private void consultaEvolucionScoreFinanciero(ConsultaResponse consultaResponse, String vista) throws ParseException {
		if(!isNullOrEmpty(consultaResponse.getResult().getEvolucionScoreFinanciero())) {
			lstEvolucionScoreFinanciero = consultaResponse.getResult().getEvolucionScoreFinanciero();
			if(lstEvolucionScoreFinanciero.size() > 0) {																				 
				 createLineModel(vista);
			}
		}
	}
	
		
	private void createLineModel(String vista) throws ParseException {
        lineModel = new LineChartModel();

        LineChartSeries series = new LineChartSeries();
        series.setLabel("Evolución Score Financiero");
        
        
        for (EvolucionScoreFinanciero datos : lstEvolucionScoreFinanciero) {
        	if("N".equals(vista)) {
        		series.set(formateoFecha(datos.getFechaCorte()), datos.getEjeY());
        	} else {
        		series.set(datos.getFechaCorte(), datos.getEjeY());
        	}
        }

        lineModel.addSeries(series);
        lineModel.setShowPointLabels(true);
        lineModel.setLegendPosition("n");                
        lineModel.setAnimate(true);
        lineModel.setLegendRows(1);
        lineModel.setShadow(true);
        lineModel.setZoom(true);
        lineModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID); 
        
        
        CategoryAxis cat = new CategoryAxis("Fecha de Corte");
        cat.setTickAngle(-45);
        lineModel.getAxes().put(AxisType.X, cat);
        Axis yAxis = lineModel.getAxis(AxisType.Y);
        yAxis.setLabel("Score");
        	        
    }	
		
	public void consultaSemaforoMaximoDiasVencidos(ConsultaResponse consultaResponse, String vista) throws ParseException {
		if(!isNullOrEmpty(consultaResponse.getResult().getSemaforoMaximoDiasVencido())) {
			lstSemaforoMaximoDiasVencido = consultaResponse.getResult().getSemaforoMaximoDiasVencido();
			if(lstSemaforoMaximoDiasVencido.size() > 0) {								
				
				lineModelSemaforo = new LineChartModel();
		        LineChartSeries series = new LineChartSeries();
		        series.setLabel("Días Vencidos");
		        
		        for (SemaforoMaximoDiasVencido semaforo : lstSemaforoMaximoDiasVencido) {		        
		        	if(semaforo.getDiasVencido().equalsIgnoreCase("CC")) {
		        		if("N".equals(vista)) {
		        			series.set(formateoFecha(semaforo.getFechaCorte()), 0);
		        		} else {
		        			series.set(semaforo.getFechaCorte(), 0);
		        		}
		        	} else {
		        		BigDecimal dias = BigDecimal.ZERO;
		        		if(semaforo.getDiasVencido().matches("^\\d+$")) {
		        			dias = new BigDecimal(semaforo.getDiasVencido());
		        		} 		        
		        		if("N".equals(vista)) {
		        			series.set(formateoFecha(semaforo.getFechaCorte()), dias.intValue());
		        		} else {
		        			series.set(semaforo.getFechaCorte(), dias.intValue());
		        		}	
		        	}
		            
		        }
		        
		        lineModelSemaforo.addSeries(series);
		        lineModelSemaforo.setShowPointLabels(true);		        
		        lineModelSemaforo.setLegendPosition("n");
		        lineModelSemaforo.setAnimate(true);
		        lineModelSemaforo.setLegendRows(1);
		        lineModelSemaforo.setShadow(true);
		        lineModelSemaforo.setZoom(true);
		        lineModelSemaforo.setLegendPlacement(LegendPlacement.OUTSIDEGRID);     
		        
		        CategoryAxis cat = new CategoryAxis("Fecha de Corte");
		        cat.setTickAngle(-45);
		        lineModelSemaforo.getAxes().put(AxisType.X, cat);
		        Axis yAxis = lineModelSemaforo.getAxis(AxisType.Y);
		        yAxis.setLabel("Dias Vencidos");
		        	      							
			}
		}		
	}
	
	
	public void consultarTendenciaDeuda(ConsultaResponse consultaResponse, String vista) throws ParseException {
		if(!isNullOrEmpty(consultaResponse.getResult().getTendenciaDeuda())) {
			lstTendenciaDeuda = consultaResponse.getResult().getTendenciaDeuda();
			lstTendenciaDeuda.sort(Comparator.comparing(TendenciaDeuda::getFechaCorte));
			
			Map<String, Double> mapaTotalDeuda = lstTendenciaDeuda.stream().
					collect(Collectors.toMap(
							TendenciaDeuda::getFechaCorte, 
							TendenciaDeuda::getTotalDeuda));						
			
			Map<String, Double> mapaValorVencidoTotal = lstTendenciaDeuda.stream()
			        .collect(Collectors.toMap(
			        		TendenciaDeuda::getFechaCorte, 
			        		TendenciaDeuda::getValorVencidoTotal));
						
			tendenciaDeudaChat = new LineChartModel();
			
			ChartSeries deudaTotalSeries = new ChartSeries();
			deudaTotalSeries.setLabel("Total Deuda");	      
	        
			List<Map.Entry<String, Double>> listaTotalDeuda = new ArrayList<>(mapaTotalDeuda.entrySet());
			listaTotalDeuda.sort(Comparator.comparing(Map.Entry::getKey));
			
	       for(Map.Entry<String, Double> entry : listaTotalDeuda) {	     
	        	if("N".equals(vista)) {
	        		deudaTotalSeries.set(formateoFecha(entry.getKey()), entry.getValue());
	        	} else {
	        		deudaTotalSeries.set(entry.getKey(), entry.getValue());
	        	}
	        }	        	        	              
	        
	       			
			ChartSeries vencidoTotalSeries = new ChartSeries();
	        vencidoTotalSeries.setLabel("Total Vencido");	
	        	        
	        List<Map.Entry<String, Double>> listaVencidoTotal = new ArrayList<>(mapaValorVencidoTotal.entrySet());
	        listaVencidoTotal.sort(Comparator.comparing(Map.Entry::getKey));
	        for(Map.Entry<String, Double> entry : listaVencidoTotal) {
	        	if("N".equals(vista)) {
	        		vencidoTotalSeries.set(formateoFecha(entry.getKey()), entry.getValue());
	        	} else {
	        		vencidoTotalSeries.set(entry.getKey(), entry.getValue());
	        	}
	        }
	       
	        tendenciaDeudaChat.addSeries(deudaTotalSeries);	        
	        tendenciaDeudaChat.setShowPointLabels(false);
	        tendenciaDeudaChat.setLegendPosition("n");
	        tendenciaDeudaChat.setAnimate(true);
	        tendenciaDeudaChat.setLegendRows(1);
	        tendenciaDeudaChat.setShadow(true);
	        tendenciaDeudaChat.setZoom(true);
	        tendenciaDeudaChat.setLegendPlacement(LegendPlacement.OUTSIDEGRID);     
	        tendenciaDeudaChat.addSeries(vencidoTotalSeries);
	        
	        CategoryAxis cat = new CategoryAxis("Fecha de Corte");
	        cat.setTickAngle(-45);
	        tendenciaDeudaChat.getAxes().put(AxisType.X, cat);	
	        
	        Axis yAxis = tendenciaDeudaChat.getAxis(AxisType.Y);
	        yAxis.setLabel("Total Deuda");
	        yAxis.setMin(0);
	        
		}
	}
	
	
	public void consultarIndicadoresDeudaUlti36Meses(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getIndicadoresDeuda())){
			lstIndicadoresDeuda = new ArrayList<IndicadoresDeuda>();
			lstIndicadoresDeuda = consultaResponse.getResult().getIndicadoresDeuda();
		}		
	}
	
	public void consultarOperacionesHistoricasTarjetasCredito(ConsultaResponse consultaResponse) throws ParseException {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesHistoricasTarjeta())){
			lstOperacionHistoricaTarjeta = new ArrayList<OperacionHistoricaTarjeta>();
			lstOperacionHistoricaTarjetaFiltrada = new ArrayList<OperacionHistoricaTarjeta>();
			lstOperacionHistoricaTarjeta = consultaResponse.getResult().getOperacionesHistoricasTarjeta();
			lstOperacionHistoricaTarjetaFiltrada = lstOperacionHistoricaTarjeta;			
		}
	}
	
	public void filtrarConsultaOperacionesHistoricasTarjetasCredito() {
		lstOperacionHistoricaTarjetaFiltrada = new ArrayList<OperacionHistoricaTarjeta>();
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Los meses comienzan desde 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear objeto LocalDate
        final LocalDate fechaHoy = LocalDate.of(year, month, day);
        LocalDate fechaMesesAntes = LocalDate.now();
        
		if("6".equals(opcionUltmesesTC)) {
			fechaMesesAntes = fechaHoy.minusMonths(6);	        
		} else if("12".equals(opcionUltmesesTC)) {
			fechaMesesAntes = fechaHoy.minusMonths(12);
		} else if("24".equals(opcionUltmesesTC)) {
			fechaMesesAntes = fechaHoy.minusMonths(24);
		} else if("36".equals(opcionUltmesesTC)) {
			fechaMesesAntes = fechaHoy.minusMonths(36);
		} else {
			lstOperacionHistoricaTarjetaFiltrada = lstOperacionHistoricaTarjeta;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		for (OperacionHistoricaTarjeta operacion : lstOperacionHistoricaTarjeta) {
            LocalDate fechaCorte = LocalDate.parse(operacion.getFechaCorte(), formatter);
            if (fechaCorte.isAfter(fechaMesesAntes) && fechaCorte.isBefore(fechaHoy)) {            	
                lstOperacionHistoricaTarjetaFiltrada.add(operacion);
            }
        }
	}
	
	
	public void consultarOperacionesHistoricasBancos(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesHistoricasBanco())){
			lstOperacionHistoricaBanco = new ArrayList<OperacionHistoricaBanco>();
			lstOperacionHistoricaBancoFiltrada = new ArrayList<OperacionHistoricaBanco>();
			lstOperacionHistoricaBanco = consultaResponse.getResult().getOperacionesHistoricasBanco();
			lstOperacionHistoricaBancoFiltrada = lstOperacionHistoricaBanco;			
		}
	}
	
	public void filtrarConsultaOperacionesHistoricasBancos() {							
		lstOperacionHistoricaBancoFiltrada = new ArrayList<OperacionHistoricaBanco>();
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Los meses comienzan desde 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear objeto LocalDate
        final LocalDate fechaHoy = LocalDate.of(year, month, day);
        LocalDate fechaMesesAntes = LocalDate.now();
        
		if("6".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(6);	        
		} else if("12".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(12);
		} else if("24".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(24);
		} else if("36".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(36);
		} else {
			lstOperacionHistoricaBancoFiltrada = lstOperacionHistoricaBanco;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		for (OperacionHistoricaBanco operacion : lstOperacionHistoricaBanco) {
            LocalDate fechaCorte = LocalDate.parse(operacion.getFechaCorte(), formatter);
            if (fechaCorte.isAfter(fechaMesesAntes) && fechaCorte.isBefore(fechaHoy)) {            	
            	lstOperacionHistoricaBancoFiltrada.add(operacion);
            }
        }
		
	}
	
	public void filtrarConsultaOperacionesHistoricasBancosTitularCodeudor() {
		List<OperacionHistoricaBanco> lstTitularTmp;
		List<OperacionHistoricaBanco> lstCodeudorTmp;		
				
		if(esTitularHisBanco && !esCodeudorHisBanco) {						
			lstTitularTmp = lstOperacionHistoricaBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstOperacionHistoricaBancoFiltrada  = new ArrayList<OperacionHistoricaBanco>();
			lstOperacionHistoricaBancoFiltrada.addAll(lstTitularTmp);
		}
		if(!esTitularHisBanco && esCodeudorHisBanco) {
			
			lstCodeudorTmp = lstOperacionHistoricaBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionHistoricaBancoFiltrada  = new ArrayList<OperacionHistoricaBanco>();
			lstOperacionHistoricaBancoFiltrada.addAll(lstCodeudorTmp);
		}
		if(esTitularHisBanco && esCodeudorHisBanco) {
			lstTitularTmp = lstOperacionHistoricaBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstCodeudorTmp = lstOperacionHistoricaBanco.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionHistoricaBancoFiltrada = new ArrayList<OperacionHistoricaBanco>();
			lstOperacionHistoricaBancoFiltrada.addAll(lstTitularTmp);
			lstOperacionHistoricaBancoFiltrada.addAll(lstCodeudorTmp);						
		}
	}
		
	public void consultarOperacionesHistoricasCooperativas(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesHistoricasCooperativa())){
			lstOperacionHistoricaCooperativa = new ArrayList<OperacionHistoricaCooperativa>();
			lstOperacionHistoricaCooperativaFiltrada = new ArrayList<OperacionHistoricaCooperativa>();
			lstOperacionHistoricaCooperativa = consultaResponse.getResult().getOperacionesHistoricasCooperativa();
			lstOperacionHistoricaCooperativaFiltrada = lstOperacionHistoricaCooperativa;			
		}
	}
	
	public void filtrarConsultaOperacionesHistoricasCooperativas() {							
		lstOperacionHistoricaCooperativaFiltrada = new ArrayList<OperacionHistoricaCooperativa>();
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Los meses comienzan desde 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear objeto LocalDate
        final LocalDate fechaHoy = LocalDate.of(year, month, day);
        LocalDate fechaMesesAntes = LocalDate.now();
        
		if("6".equals(opcionUltmesesCooperativas)) {
			fechaMesesAntes = fechaHoy.minusMonths(6);	        
		} else if("12".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(12);
		} else if("24".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(24);
		} else if("36".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(36);
		} else {
			lstOperacionHistoricaCooperativaFiltrada = lstOperacionHistoricaCooperativa;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		for (OperacionHistoricaCooperativa operacion : lstOperacionHistoricaCooperativa) {
            LocalDate fechaCorte = LocalDate.parse(operacion.getFechaCorte(), formatter);
            if (fechaCorte.isAfter(fechaMesesAntes) && fechaCorte.isBefore(fechaHoy)) {            	
            	lstOperacionHistoricaCooperativaFiltrada.add(operacion);
            }
        }
		
	}
	
	public void filtrarConsultaOperacionesHistoricasCooperativasTitularCodeudor() {
		List<OperacionHistoricaCooperativa> lstTitularTmp = new ArrayList<OperacionHistoricaCooperativa>();
		List<OperacionHistoricaCooperativa> lstCodeudorTmp = new ArrayList<OperacionHistoricaCooperativa>();;		
				
		if(esTitularHisCooperativa && !esCodeudorHisCooperativa) {						
			lstTitularTmp = lstOperacionHistoricaCooperativa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstOperacionHistoricaCooperativaFiltrada  = new ArrayList<OperacionHistoricaCooperativa>();
			lstOperacionHistoricaCooperativaFiltrada.addAll(lstTitularTmp);
		}
		if(!esTitularHisCooperativa && esCodeudorHisCooperativa) {
			
			lstCodeudorTmp = lstOperacionHistoricaCooperativa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionHistoricaCooperativaFiltrada  = new ArrayList<OperacionHistoricaCooperativa>();
			lstOperacionHistoricaCooperativaFiltrada.addAll(lstCodeudorTmp);
		}
		if(esTitularHisCooperativa && esCodeudorHisCooperativa) {
			lstTitularTmp = lstOperacionHistoricaCooperativa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstCodeudorTmp = lstOperacionHistoricaCooperativa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionHistoricaBancoFiltrada = new ArrayList<OperacionHistoricaBanco>();
			lstOperacionHistoricaCooperativaFiltrada.addAll(lstTitularTmp);
			lstOperacionHistoricaCooperativaFiltrada.addAll(lstCodeudorTmp);						
		}
	}
	
	
	public void consultarOperacionesHistoricasEmpresas(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesHistoricasEmpresa())){
			lstOperacionHistoricaEmpresa = new ArrayList<OperacionHistoricaEmpresa>();
			lstOperacionHistoricaEmpresaFiltrada = new ArrayList<OperacionHistoricaEmpresa>();
			lstOperacionHistoricaEmpresa = consultaResponse.getResult().getOperacionesHistoricasEmpresa();
			lstOperacionHistoricaEmpresaFiltrada = lstOperacionHistoricaEmpresa;
		}
	}
	
	public void filtrarConsultaOperacionesHistoricasEmpresas() {							
		lstOperacionHistoricaEmpresaFiltrada = new ArrayList<OperacionHistoricaEmpresa>();
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Los meses comienzan desde 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear objeto LocalDate
        final LocalDate fechaHoy = LocalDate.of(year, month, day);
        LocalDate fechaMesesAntes = LocalDate.now();
        
		if("6".equals(opcionUltmesesEmpresas)) {
			fechaMesesAntes = fechaHoy.minusMonths(6);	        
		} else if("12".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(12);
		} else if("24".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(24);
		} else if("36".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(36);
		} else {
			lstOperacionHistoricaEmpresaFiltrada = lstOperacionHistoricaEmpresa;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		for (OperacionHistoricaEmpresa operacion : lstOperacionHistoricaEmpresa) {
            LocalDate fechaCorte = LocalDate.parse(operacion.getFechaCorte(), formatter);
            if (fechaCorte.isAfter(fechaMesesAntes) && fechaCorte.isBefore(fechaHoy)) {            	
            	lstOperacionHistoricaEmpresaFiltrada.add(operacion);
            }
        }
		
	}
	
	public void filtrarConsultaOperacionesHistoricasEmpresaTitularCodeudor() {
		List<OperacionHistoricaEmpresa> lstTitularTmp = new ArrayList<OperacionHistoricaEmpresa>();
		List<OperacionHistoricaEmpresa> lstCodeudorTmp = new ArrayList<OperacionHistoricaEmpresa>();;		
				
		if(esTitularHisEmpresas && !esCodeudorHisEmpresas) {						
			lstTitularTmp = lstOperacionHistoricaEmpresa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstOperacionHistoricaEmpresaFiltrada  = new ArrayList<OperacionHistoricaEmpresa>();
			lstOperacionHistoricaEmpresaFiltrada.addAll(lstTitularTmp);
		}
		if(!esTitularHisEmpresas && esCodeudorHisEmpresas) {
			
			lstCodeudorTmp = lstOperacionHistoricaEmpresa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionHistoricaEmpresaFiltrada  = new ArrayList<OperacionHistoricaEmpresa>();
			lstOperacionHistoricaEmpresaFiltrada.addAll(lstCodeudorTmp);
		}
		if(esTitularHisEmpresas && esCodeudorHisEmpresas) {
			lstTitularTmp = lstOperacionHistoricaEmpresa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Titular"))
					.collect(Collectors.toList());
			lstCodeudorTmp = lstOperacionHistoricaEmpresa.stream()
					.filter(operacionVigenteBanco -> operacionVigenteBanco.getTipoDeudorDescripcion().equals("Codeudor"))
					.collect(Collectors.toList());
			lstOperacionHistoricaEmpresaFiltrada = new ArrayList<OperacionHistoricaEmpresa>();
			lstOperacionHistoricaEmpresaFiltrada.addAll(lstTitularTmp);
			lstOperacionHistoricaEmpresaFiltrada.addAll(lstCodeudorTmp);						
		}
	}
	
	public void consultarOperacionesHistoricasServicios(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesHistoricasServicio())){
			lstOperacionesHistoricasServicio = new ArrayList<OperacionesHistoricasServicio>();
			lstOperacionesHistoricasServicioFiltrada = new ArrayList<OperacionesHistoricasServicio>();
			lstOperacionesHistoricasServicio = consultaResponse.getResult().getOperacionesHistoricasServicio();
			lstOperacionesHistoricasServicioFiltrada = lstOperacionesHistoricasServicio;
		}
	}
	
	public void filtrarConsultaOperacionesHistoricasServicios() {							
		lstOperacionesHistoricasServicioFiltrada = new ArrayList<OperacionesHistoricasServicio>();
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Los meses comienzan desde 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear objeto LocalDate
        final LocalDate fechaHoy = LocalDate.of(year, month, day);
        LocalDate fechaMesesAntes = LocalDate.now();
        
		if("6".equals(opcionUltmesesServicios)) {
			fechaMesesAntes = fechaHoy.minusMonths(6);	        
		} else if("12".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(12);
		} else if("24".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(24);
		} else if("36".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(36);
		} else {
			lstOperacionesHistoricasServicioFiltrada = lstOperacionesHistoricasServicio;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		for (OperacionesHistoricasServicio operacion : lstOperacionesHistoricasServicio) {
            LocalDate fechaCorte = LocalDate.parse(operacion.getFechaCorte(), formatter);
            if (fechaCorte.isAfter(fechaMesesAntes) && fechaCorte.isBefore(fechaHoy)) {            	
            	lstOperacionesHistoricasServicioFiltrada.add(operacion);
            }
        }		
	}
	
	
	public void consultarOperacionesHistoricasCobranzas(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getOperacionesHistoricasCobranza())){
			lstOperacionHistoricaCobranza = new ArrayList<OperacionHistoricaCobranza>();
			lstOperacionHistoricaCobranzaFiltrada = new ArrayList<OperacionHistoricaCobranza>();
			lstOperacionHistoricaCobranza = consultaResponse.getResult().getOperacionesHistoricasCobranza();
			lstOperacionHistoricaCobranzaFiltrada = lstOperacionHistoricaCobranza;
		}
	}
	
	public void filtrarConsultaOperacionesHistoricasCobranzas() {							
		lstOperacionHistoricaCobranzaFiltrada = new ArrayList<OperacionHistoricaCobranza>();
		
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaActual);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Los meses comienzan desde 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear objeto LocalDate
        final LocalDate fechaHoy = LocalDate.of(year, month, day);
        LocalDate fechaMesesAntes = LocalDate.now();
        
		if("6".equals(opcionUltmesesCobranzas)) {
			fechaMesesAntes = fechaHoy.minusMonths(6);	        
		} else if("12".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(12);
		} else if("24".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(24);
		} else if("36".equals(opcionUltmesesBancos)) {
			fechaMesesAntes = fechaHoy.minusMonths(36);
		} else {
			lstOperacionHistoricaCobranzaFiltrada = lstOperacionHistoricaCobranza;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
		for (OperacionHistoricaCobranza operacion : lstOperacionHistoricaCobranza) {
            LocalDate fechaCorte = LocalDate.parse(operacion.getFechaCorte(), formatter);
            if (fechaCorte.isAfter(fechaMesesAntes) && fechaCorte.isBefore(fechaHoy)) {            	
            	lstOperacionHistoricaCobranzaFiltrada.add(operacion);
            }
        }		
	}
	
	public void consultarRelacionConEmpresas(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getRelacionEmpresas())){
			lstRelacionEmpresas = new ArrayList<RelacionEmpresas>();			
			lstRelacionEmpresas = consultaResponse.getResult().getRelacionEmpresas();
			
		}
	}
	
	public void consultarDatosContacto(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getDatosContacto())){
			lstDatosContacto = new ArrayList<DatosContacto>();			
			lstDatosContacto = consultaResponse.getResult().getDatosContacto();
			
		}
	}
	
	public void consultarTitularConsultado(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getTitularConsultado12Meses())){
			lstTitularConsultado = new ArrayList<TitularConsultado>();			
			lstTitularConsultado = consultaResponse.getResult().getTitularConsultado12Meses();
			
		}
	}
		
	public void consultarDetalleEmpresa(ConsultaResponse consultaResponse){
		if(!isNullOrEmpty(consultaResponse.getResult().getDetalleEmpresa())){
			lstDetalleEmpresas = new ArrayList<DetalleEmpresa>();			
			lstDetalleEmpresas = consultaResponse.getResult().getDetalleEmpresa();			
		}
	}
	
	public void consultarResumenPrincipalCtasFinancieras(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getResumenPrincipalesCuentasFinancieras())){
			lstResumenPrincipalesCuentasFinancieras = new ArrayList<ResumenPrincipalesCuentasFinancieras>();			
			lstResumenPrincipalesCuentasFinancieras = consultaResponse.getResult().getResumenPrincipalesCuentasFinancieras();		
		}
	}
	
	public void consultarCuentasEstadosFinancieros(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getCuentasEstadosFinancieros())){
			lstCuentasEstadosFinancieros = new ArrayList<CuentasEstadosFinancieros>();			
			lstCuentasEstadosFinancieros = consultaResponse.getResult().getCuentasEstadosFinancieros();		
		}
	}
	
	public void consultarIndicesFinancieros(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getIndicesFinancieros())){
			lstIndicesFinancieros = new ArrayList<IndicesFinancieros>();			
			lstIndicesFinancieros = consultaResponse.getResult().getIndicesFinancieros();		
		}
	}
	
	public void consultarPrincipalesAccionistas(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getPrincipalesAccionistas())){
			lstPrincipalesAccionistas = new ArrayList<PrincipalesAccionistas>();			
			lstPrincipalesAccionistas = consultaResponse.getResult().getPrincipalesAccionistas();		
		}
	}
	
	public void consultarRepresentantesLegales(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getRepresentantesLegales())){
			lstRepresentantesLegales = new ArrayList<RepresentantesLegales>();			
			lstRepresentantesLegales = consultaResponse.getResult().getRepresentantesLegales();		
		}
	}
	public void consultarContactabilidad(ConsultaResponse consultaResponse) {
		if(!isNullOrEmpty(consultaResponse.getResult().getContactabilidad())){
			lstContactabilidad = new ArrayList<Contactabilidad>();			
			lstContactabilidad = consultaResponse.getResult().getContactabilidad();		
		}
	}
	
	private String formateoFecha(String Paramfecha) throws ParseException {
		SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatoDeseado = new SimpleDateFormat("MMM-yyyy");
        Date fecha  = null;                
        fecha = formatoOriginal.parse(Paramfecha);
        return formatoDeseado.format(fecha).toUpperCase();
	}
	
	/**
	 * Metodos Helper utilizados para manejo de flujo legacy de aval buro
	 */
	private boolean tieneJsonLegacyDisponible(CxcAvalBuro registro) {
		if (registro == null) {
			return false;
		}

		String json = registro.getJson();
		return json != null && !json.trim().isEmpty() && !"(null)".equalsIgnoreCase(json.trim());
	}

	private String normalizarJson(String jsonString) {
		if (jsonString == null) {
			return null;
		}

		String jsonNormalizado = jsonString.trim();
		if (jsonNormalizado.startsWith("\"") && jsonNormalizado.endsWith("\"")) {
			jsonNormalizado = jsonNormalizado.substring(1, jsonNormalizado.length() - 1).replace("\\\"", "\"")
					.replace("\\\\", "\\");
		}

		return jsonNormalizado;
	}

	private boolean refrescarJsonLegacyDesdeWs(Gson gson, Date fechaFinDatosValidos)
			throws ParseException, MalformedURLException, ProtocolException, IOException {
		consultarWSReporteAvalBuro(gson, fechaFinDatosValidos);
		existeHistorialCrediticioPorCedula();
		return tieneJsonLegacyDisponible(existeCedula);
	}
	
	public static <T> boolean isNullOrEmpty(List<T> lista) {
        return lista == null || lista.isEmpty();
    }
	
	public String getTipoCliente() {
		return tipoCliente;
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

	public String getMensajeConexion() {
		return mensajeConexion;
	}

	public void setMensajeConexion(String mensajeConexion) {
		this.mensajeConexion = mensajeConexion;
	}

	public List<SelectItem> getListaTiposIdentificacion() {
		return listaTiposIdentificacion;
	}

	public void setListaTiposIdentificacion(List<SelectItem> listaTiposIdentificacion) {
		this.listaTiposIdentificacion = listaTiposIdentificacion;
	}

	public IdentificacionTitular getIdentificacionTitular() {
		return identificacionTitular;
	}

	public void setIdentificacionTitular(IdentificacionTitular identificacionTitular) {
		this.identificacionTitular = identificacionTitular;
	}

	public Boolean getMostrarPantallasConsulta() {
		return mostrarPantallasConsulta;
	}

	public void setMostrarPantallasConsulta(Boolean mostrarPantallasConsulta) {
		this.mostrarPantallasConsulta = mostrarPantallasConsulta;
	}

	public InformacionComoRUC getInformacionComoRUC() {
		return informacionComoRUC;
	}

	public void setInformacionComoRUC(InformacionComoRUC informacionComoRUC) {
		this.informacionComoRUC = informacionComoRUC;
	}

	public String getMensajeDatosVacios() {
		return mensajeDatosVacios;
	}

	public void setMensajeDatosVacios(String mensajeDatosVacios) {
		this.mensajeDatosVacios = mensajeDatosVacios;
	}

	public DonutChartModel getScore() {
		return score;
	}

	public void setScore(DonutChartModel score) {
		this.score = score;
	}

	public ScoreFinanciero getScoreFinanciero() {
		return scoreFinanciero;
	}

	public void setScoreFinanciero(ScoreFinanciero scoreFinanciero) {
		this.scoreFinanciero = scoreFinanciero;
	}

	public String getPorcentajePeorScore() {
		return porcentajePeorScore;
	}

	public void setPorcentajePeorScore(String porcentajePeorScore) {
		this.porcentajePeorScore = porcentajePeorScore;
	}

	public String getPorcentajeProbalidadCaerMora() {
		return porcentajeProbalidadCaerMora;
	}

	public void setPorcentajeProbalidadCaerMora(String porcentajeProbalidadCaerMora) {
		this.porcentajeProbalidadCaerMora = porcentajeProbalidadCaerMora;
	}

	public String getExplicacion() {
		return explicacion;
	}

	public void setExplicacion(String explicacion) {
		this.explicacion = explicacion;
	}

	public List<FactoresScore> getLstFactoresScore() {
		return lstFactoresScore;
	}

	public void setLstFactoresScore(List<FactoresScore> LstFactoresScore) {
		this.lstFactoresScore = LstFactoresScore;
	}

	public List<ManejoCuentasCorrientes> getLstManejoCuentasCorrientes() {
		return lstManejoCuentasCorrientes;
	}

	public void setLstManejoCuentasCorrientes(List<ManejoCuentasCorrientes> lstManejoCuentasCorrientes) {
		this.lstManejoCuentasCorrientes = lstManejoCuentasCorrientes;
	}

	public Date getFechaActual() {
		return fechaActual;
	}

	public void setFechaActual(Date fechaActual) {
		this.fechaActual = fechaActual;
	}

	public List<DeudaVigenteTotal> getLstDeudaVigenteTotal() {
		return lstDeudaVigenteTotal;
	}

	public void setLstDeudaVigenteTotal(List<DeudaVigenteTotal> lstDeudaVigenteTotal) {
		this.lstDeudaVigenteTotal = lstDeudaVigenteTotal;
	}

	public ParamDetServiceLocal getParamDetService() {
		return paramDetService;
	}

	public void setParamDetService(ParamDetServiceLocal paramDetService) {
		this.paramDetService = paramDetService;
	}

	public Double getTotalSaldoVencer() {
		return totalSaldoVencer;
	}

	public void setTotalSaldoVencer(Double totalSaldoVencer) {
		this.totalSaldoVencer = totalSaldoVencer;
	}

	public Double getTotalSaldoNDI() {
		return totalSaldoNDI;
	}

	public void setTotalSaldoNDI(Double totalSaldoNDI) {
		this.totalSaldoNDI = totalSaldoNDI;
	}

	public Double getTotalSaldoVenciado() {
		return totalSaldoVenciado;
	}

	public void setTotalSaldoVenciado(Double totalSaldoVenciado) {
		this.totalSaldoVenciado = totalSaldoVenciado;
	}

	public Double getTotalDemandaJudicial() {
		return totalDemandaJudicial;
	}

	public void setTotalDemandaJudicial(Double totalDemandaJudicial) {
		this.totalDemandaJudicial = totalDemandaJudicial;
	}

	public Double getTotalCarteraCastigada() {
		return totalCarteraCastigada;
	}

	public void setTotalCarteraCastigada(Double totalCarteraCastigada) {
		this.totalCarteraCastigada = totalCarteraCastigada;
	}

	public Double getTotalDeudaTotal() {
		return totalDeudaTotal;
	}

	public void setTotalDeudaTotal(Double totalDeudaTotal) {
		this.totalDeudaTotal = totalDeudaTotal;
	}

	public List<GastoFinanciero> getLstGastoFinanciero() {
		return lstGastoFinanciero;
	}

	public void setLstGastoFinanciero(List<GastoFinanciero> lstGastoFinanciero) {
		this.lstGastoFinanciero = lstGastoFinanciero;
	}

	public List<OperacionesCodeudorGarante> getLstOperacionesCodeudorGarante() {
		return lstOperacionesCodeudorGarante;
	}

	public void setLstOperacionesCodeudorGarante(List<OperacionesCodeudorGarante> lstOperacionesCodeudorGarante) {
		this.lstOperacionesCodeudorGarante = lstOperacionesCodeudorGarante;
	}

	public List<InformacionComoRUC> getLstInformacionComoRuc() {
		return lstInformacionComoRuc;
	}

	public void setLstInformacionComoRuc(List<InformacionComoRUC> lstInformacionComoRuc) {
		this.lstInformacionComoRuc = lstInformacionComoRuc;
	}

	public String getLogoAvalBuro() {
		return logoAvalBuro;
	}

	public void setLogoAvalBuro(String logoAvalBuro) {
		this.logoAvalBuro = logoAvalBuro;
	}

	public List<OperacionVigenteTarjeta> getLstOperacionesVigentesTarjeta() {
		return lstOperacionesVigentesTarjeta;
	}

	public void setLstOperacionesVigentesTarjeta(List<OperacionVigenteTarjeta> lstOperacionesVigentesTarjeta) {
		this.lstOperacionesVigentesTarjeta = lstOperacionesVigentesTarjeta;
	}

	public List<DetalleTarjetaSaldoVigente> getLstDetalleTarjetaSaldoVigente() {
		return lstDetalleTarjetaSaldoVigente;
	}

	public void setLstDetalleTarjetaSaldoVigente(List<DetalleTarjetaSaldoVigente> lstDetalleTarjetaSaldoVigente) {
		this.lstDetalleTarjetaSaldoVigente = lstDetalleTarjetaSaldoVigente;
	}

	public List<IndicadorTarjeta> getLstIndicadorTarjeta() {
		return lstIndicadorTarjeta;
	}

	public void setLstIndicadorTarjeta(List<IndicadorTarjeta> lstIndicadorTarjeta) {
		this.lstIndicadorTarjeta = lstIndicadorTarjeta;
	}

	public List<OperacionVigenteBanco> getLstOperacionVigenteBanco() {
		return lstOperacionVigenteBanco;
	}

	public void setLstOperacionVigenteBanco(List<OperacionVigenteBanco> lstOperacionVigenteBanco) {
		this.lstOperacionVigenteBanco = lstOperacionVigenteBanco;
	}

	public List<DetalleTarjetaCredito> getLstDetalleTarjetaCredito() {
		return lstDetalleTarjetaCredito;
	}

	public void setLstDetalleTarjetaCredito(List<DetalleTarjetaCredito> lstDetalleTarjetaCredito) {
		this.lstDetalleTarjetaCredito = lstDetalleTarjetaCredito;
	}		

	public List<TarjetasCreditoVigentesDTO> getLstTarjetasCreditoVigentesDTO() {
		return lstTarjetasCreditoVigentesDTO;
	}

	public void setLstTarjetasCreditoVigentesDTO(List<TarjetasCreditoVigentesDTO> lstTarjetasCreditoVigentesDTO) {
		this.lstTarjetasCreditoVigentesDTO = lstTarjetasCreditoVigentesDTO;
	}

	public double getSaldoTotal() {
		return saldoTotal;
	}

	public void setSaldoTotal(double saldoTotal) {
		this.saldoTotal = saldoTotal;
	}

	public double getCapitalxVencerTotal() {
		return capitalxVencerTotal;
	}

	public void setCapitalxVencerTotal(double capitalxVencerTotal) {
		this.capitalxVencerTotal = capitalxVencerTotal;
	}

	public double getSaldoVencido() {
		return saldoVencido;
	}

	public void setSaldoVencido(double saldoVencido) {
		this.saldoVencido = saldoVencido;
	}

	public double getValorNoDevengaInteresTotal() {
		return valorNoDevengaInteresTotal;
	}

	public void setValorNoDevengaInteresTotal(double valorNoDevengaInteresTotal) {
		this.valorNoDevengaInteresTotal = valorNoDevengaInteresTotal;
	}

	public double getValorDemandaJudicial() {
		return valorDemandaJudicial;
	}

	public void setValorDemandaJudicial(double valorDemandaJudicial) {
		this.valorDemandaJudicial = valorDemandaJudicial;
	}

	public double getCarteraCastigada() {
		return carteraCastigada;
	}

	public void setCarteraCastigada(double carteraCastigada) {
		this.carteraCastigada = carteraCastigada;
	}

	public int getDiasMorosidad() {
		return diasMorosidad;
	}

	public void setDiasMorosidad(int diasMorosidad) {
		this.diasMorosidad = diasMorosidad;
	}

	public double getValorPagado() {
		return valorPagado;
	}

	public void setValorPagado(double valorPagado) {
		this.valorPagado = valorPagado;
	}

	public double getValorMinimoPagar() {
		return valorMinimoPagar;
	}

	public void setValorMinimoPagar(double valorMinimoPagar) {
		this.valorMinimoPagar = valorMinimoPagar;
	}

	public List<SaldoPorVencerDto> getLstSaldoPorVencerDto() {
		return lstSaldoPorVencerDto;
	}

	public void setLstSaldoPorVencerDto(List<SaldoPorVencerDto> lstSaldoPorVencerDto) {
		this.lstSaldoPorVencerDto = lstSaldoPorVencerDto;
	}

	public double getCapitalxVencer1a30() {
		return capitalxVencer1a30;
	}

	public void setCapitalxVencer1a30(double capitalxVencer1a30) {
		this.capitalxVencer1a30 = capitalxVencer1a30;
	}

	public double getCapitalxVencer31a90() {
		return capitalxVencer31a90;
	}

	public void setCapitalxVencer31a90(double capitalxVencer31a90) {
		this.capitalxVencer31a90 = capitalxVencer31a90;
	}

	public double getCapitalxVencer91a180() {
		return capitalxVencer91a180;
	}

	public void setCapitalxVencer91a180(double capitalxVencer91a180) {
		this.capitalxVencer91a180 = capitalxVencer91a180;
	}

	public double getCapitalxVencer181a360() {
		return capitalxVencer181a360;
	}

	public void setCapitalxVencer181a360(double capitalxVencer181a360) {
		this.capitalxVencer181a360 = capitalxVencer181a360;
	}

	public double getCapitalxVencerMas360() {
		return capitalxVencerMas360;
	}

	public void setCapitalxVencerMas360(double capitalxVencerMas360) {
		this.capitalxVencerMas360 = capitalxVencerMas360;
	}

	public double getValorNoDevengaInteres1a30() {
		return valorNoDevengaInteres1a30;
	}

	public void setValorNoDevengaInteres1a30(double valorNoDevengaInteres1a30) {
		this.valorNoDevengaInteres1a30 = valorNoDevengaInteres1a30;
	}

	public double getValorNoDevengaInteres31a90() {
		return valorNoDevengaInteres31a90;
	}

	public void setValorNoDevengaInteres31a90(double valorNoDevengaInteres31a90) {
		this.valorNoDevengaInteres31a90 = valorNoDevengaInteres31a90;
	}

	public double getValorNoDevengaInteres91a180() {
		return valorNoDevengaInteres91a180;
	}

	public void setValorNoDevengaInteres91a180(double valorNoDevengaInteres91a180) {
		this.valorNoDevengaInteres91a180 = valorNoDevengaInteres91a180;
	}

	public double getValorNoDevengaInteres181a360() {
		return valorNoDevengaInteres181a360;
	}

	public void setValorNoDevengaInteres181a360(double valorNoDevengaInteres181a360) {
		this.valorNoDevengaInteres181a360 = valorNoDevengaInteres181a360;
	}

	public double getValorNoDevengaInteresMas360() {
		return valorNoDevengaInteresMas360;
	}

	public void setValorNoDevengaInteresMas360(double valorNoDevengaInteresMas360) {
		this.valorNoDevengaInteresMas360 = valorNoDevengaInteresMas360;
	}

	public List<SaldoNoDevengaInteresDto> getLstSaldoNoDevengaInteresDto() {
		return lstSaldoNoDevengaInteresDto;
	}

	public void setLstSaldoNoDevengaInteresDto(List<SaldoNoDevengaInteresDto> lstSaldoNoDevengaInteresDto) {
		this.lstSaldoNoDevengaInteresDto = lstSaldoNoDevengaInteresDto;
	}

	public double getCapitalVencido1a30() {
		return capitalVencido1a30;
	}

	public void setCapitalVencido1a30(double capitalVencido1a30) {
		this.capitalVencido1a30 = capitalVencido1a30;
	}

	public double getCapitalVencido31a90() {
		return capitalVencido31a90;
	}

	public void setCapitalVencido31a90(double capitalVencido31a90) {
		this.capitalVencido31a90 = capitalVencido31a90;
	}

	public double getCapitalVencido91a180() {
		return capitalVencido91a180;
	}

	public void setCapitalVencido91a180(double capitalVencido91a180) {
		this.capitalVencido91a180 = capitalVencido91a180;
	}

	public double getCapitalVencido181a360() {
		return capitalVencido181a360;
	}

	public void setCapitalVencido181a360(double capitalVencido181a360) {
		this.capitalVencido181a360 = capitalVencido181a360;
	}

	public double getCapitalVencidoMas360() {
		return capitalVencidoMas360;
	}

	public void setCapitalVencidoMas360(double capitalVencidoMas360) {
		this.capitalVencidoMas360 = capitalVencidoMas360;
	}

	public double getCapitalVencido181a270() {
		return capitalVencido181a270;
	}

	public void setCapitalVencido181a270(double capitalVencido181a270) {
		this.capitalVencido181a270 = capitalVencido181a270;
	}

	public double getCapitalVencidoMas270() {
		return capitalVencidoMas270;
	}

	public void setCapitalVencidoMas270(double capitalVencidoMas270) {
		this.capitalVencidoMas270 = capitalVencidoMas270;
	}

	public double getInteresVencido1a30() {
		return interesVencido1a30;
	}

	public void setInteresVencido1a30(double interesVencido1a30) {
		this.interesVencido1a30 = interesVencido1a30;
	}

	public double getInteresVencido31a60() {
		return interesVencido31a60;
	}

	public void setInteresVencido31a60(double interesVencido31a60) {
		this.interesVencido31a60 = interesVencido31a60;
	}

	public double getInteresVencido61a90() {
		return interesVencido61a90;
	}

	public void setInteresVencido61a90(double interesVencido61a90) {
		this.interesVencido61a90 = interesVencido61a90;
	}

	public double getInteresVencido91a180() {
		return interesVencido91a180;
	}

	public void setInteresVencido91a180(double interesVencido91a180) {
		this.interesVencido91a180 = interesVencido91a180;
	}

	public double getInteresVencido181a270() {
		return interesVencido181a270;
	}

	public void setInteresVencido181a270(double interesVencido181a270) {
		this.interesVencido181a270 = interesVencido181a270;
	}

	public double getInteresVencidoMas270() {
		return interesVencidoMas270;
	}

	public void setInteresVencidoMas270(double interesVencidoMas270) {
		this.interesVencidoMas270 = interesVencidoMas270;
	}

	public double getInteresSobreMora() {
		return interesSobreMora;
	}

	public void setInteresSobreMora(double interesSobreMora) {
		this.interesSobreMora = interesSobreMora;
	}

	public double getTotalCostoOperativoVencido() {
		return totalCostoOperativoVencido;
	}

	public void setTotalCostoOperativoVencido(double totalCostoOperativoVencido) {
		this.totalCostoOperativoVencido = totalCostoOperativoVencido;
	}

	public List<SaldoVencidoDto> getLstSaldoVencidoDto() {
		return lstSaldoVencidoDto;
	}

	public void setLstSaldoVencidoDto(List<SaldoVencidoDto> lstSaldoVencidoDto) {
		this.lstSaldoVencidoDto = lstSaldoVencidoDto;
	}

	public List<String> getSelectedOptionsTitularCodeudor() {
		return selectedOptionsTitularCodeudor;
	}

	public void setSelectedOptionsTitularCodeudor(List<String> selectedOptionsTitularCodeudor) {
		this.selectedOptionsTitularCodeudor = selectedOptionsTitularCodeudor;
	}

	public boolean getEsTitular() {
		return esTitular;
	}

	public void setEsTitular(boolean esTitular) {
		this.esTitular = esTitular;
	}

	public boolean getEsCodeudor() {
		return esCodeudor;
	}

	public void setEsCodeudor(boolean esCodeudor) {
		this.esCodeudor = esCodeudor;
	}

	public List<OperacionVigenteBanco> getLstOperacionVigenteBancoFiltradoTitular() {
		return lstOperacionVigenteBancoFiltradoTitular;
	}

	public void setLstOperacionVigenteBancoFiltradoTitular(
			List<OperacionVigenteBanco> lstOperacionVigenteBancoFiltradoTitular) {
		this.lstOperacionVigenteBancoFiltradoTitular = lstOperacionVigenteBancoFiltradoTitular;
	}

	public List<OperacionVigenteBanco> getLstOperacionVigenteBancoFiltradoCodeudor() {
		return lstOperacionVigenteBancoFiltradoCodeudor;
	}

	public void setLstOperacionVigenteBancoFiltradoCodeudor(
			List<OperacionVigenteBanco> lstOperacionVigenteBancoFiltradoCodeudor) {
		this.lstOperacionVigenteBancoFiltradoCodeudor = lstOperacionVigenteBancoFiltradoCodeudor;
	}

	public List<OperacionVigenteBanco> getLstOperacionVigenteBancoFiltradoFinal() {
		return lstOperacionVigenteBancoFiltradoFinal;
	}

	public void setLstOperacionVigenteBancoFiltradoFinal(
			List<OperacionVigenteBanco> lstOperacionVigenteBancoFiltradoFinal) {
		this.lstOperacionVigenteBancoFiltradoFinal = lstOperacionVigenteBancoFiltradoFinal;
	}

	public double getValorOperacion() {
		return valorOperacion;
	}

	public void setValorOperacion(double valorOperacion) {
		this.valorOperacion = valorOperacion;
	}

	public double getPlazoXOperacion() {
		return plazoXOperacion;
	}

	public void setPlazoXOperacion(double plazoXOperacion) {
		this.plazoXOperacion = plazoXOperacion;
	}

	public double getPlazoXOpPendiente() {
		return plazoXOpPendiente;
	}

	public void setPlazoXOpPendiente(double plazoXOpPendiente) {
		this.plazoXOpPendiente = plazoXOpPendiente;
	}

	public double getSaldoTotalCalculado() {
		return saldoTotalCalculado;
	}

	public void setSaldoTotalCalculado(double saldoTotalCalculado) {
		this.saldoTotalCalculado = saldoTotalCalculado;
	}

	public double getValorxVencerTotal() {
		return valorxVencerTotal;
	}

	public void setValorxVencerTotal(double valorxVencerTotal) {
		this.valorxVencerTotal = valorxVencerTotal;
	}

	public double getValorVencidoTotal() {
		return valorVencidoTotal;
	}

	public void setValorVencidoTotal(double valorVencidoTotal) {
		this.valorVencidoTotal = valorVencidoTotal;
	}

	public double getValorNoDevengaInteresTotalBancos() {
		return valorNoDevengaInteresTotalBancos;
	}

	public void setValorNoDevengaInteresTotalBancos(double valorNoDevengaInteresTotalBancos) {
		this.valorNoDevengaInteresTotalBancos = valorNoDevengaInteresTotalBancos;
	}

	public double getValorDemandaJudicialBancos() {
		return valorDemandaJudicialBancos;
	}

	public void setValorDemandaJudicialBancos(double valorDemandaJudicialBancos) {
		this.valorDemandaJudicialBancos = valorDemandaJudicialBancos;
	}

	public double getCarteraCastigadaBancos() {
		return carteraCastigadaBancos;
	}

	public void setCarteraCastigadaBancos(double carteraCastigadaBancos) {
		this.carteraCastigadaBancos = carteraCastigadaBancos;
	}

	public int getDiasMorosidadBancos() {
		return diasMorosidadBancos;
	}

	public void setDiasMorosidadBancos(int diasMorosidadBancos) {
		this.diasMorosidadBancos = diasMorosidadBancos;
	}

	public double getCuotaEstimadaOperacion() {
		return cuotaEstimadaOperacion;
	}

	public void setCuotaEstimadaOperacion(double cuotaEstimadaOperacion) {
		this.cuotaEstimadaOperacion = cuotaEstimadaOperacion;
	}

	public List<EstructuraOperacionBancoDetalle> getLstEstructuraOperacionBancoDetalle() {
		return lstEstructuraOperacionBancoDetalle;
	}

	public void setLstEstructuraOperacionBancoDetalle(
			List<EstructuraOperacionBancoDetalle> lstEstructuraOperacionBancoDetalle) {
		this.lstEstructuraOperacionBancoDetalle = lstEstructuraOperacionBancoDetalle;
	}

	public EstructuraOperacionBancoDetalle getEstrucOperBancoDetSeleccionada() {
		return estrucOperBancoDetSeleccionada;
	}

	public void setEstrucOperBancoDetSeleccionada(EstructuraOperacionBancoDetalle estrucOperBancoDetSeleccionada) {
		this.estrucOperBancoDetSeleccionada = estrucOperBancoDetSeleccionada;
	}

	public List<OperacionVigenteCooperativa> getLstOperacionVigenteCooperativa() {
		return lstOperacionVigenteCooperativa;
	}

	public void setLstOperacionVigenteCooperativa(List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativa) {
		this.lstOperacionVigenteCooperativa = lstOperacionVigenteCooperativa;
	}

	public List<OperacionVigenteCooperativa> getLstOperacionVigenteCooperativaFilterTitular() {
		return lstOperacionVigenteCooperativaFilterTitular;
	}

	public void setLstOperacionVigenteCooperativaFilterTitular(
			List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativaFilterTitular) {
		this.lstOperacionVigenteCooperativaFilterTitular = lstOperacionVigenteCooperativaFilterTitular;
	}

	public List<OperacionVigenteCooperativa> getLstOperacionVigenteCooperativaFilterCodeudor() {
		return lstOperacionVigenteCooperativaFilterCodeudor;
	}

	public void setLstOperacionVigenteCooperativaFilterCodeudor(
			List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativaFilterCodeudor) {
		this.lstOperacionVigenteCooperativaFilterCodeudor = lstOperacionVigenteCooperativaFilterCodeudor;
	}

	public List<OperacionVigenteCooperativa> getLstOperacionVigenteCooperativaFilterFinal() {
		return lstOperacionVigenteCooperativaFilterFinal;
	}

	public void setLstOperacionVigenteCooperativaFilterFinal(
			List<OperacionVigenteCooperativa> lstOperacionVigenteCooperativaFilterFinal) {
		this.lstOperacionVigenteCooperativaFilterFinal = lstOperacionVigenteCooperativaFilterFinal;
	}

	public EstructuraOperacionCooperativaDetalle getEstructuraOperacionCooperativaDetalle() {
		return estructuraOperacionCooperativaDetalle;
	}

	public void setEstructuraOperacionCooperativaDetalle(
			EstructuraOperacionCooperativaDetalle estructuraOperacionCooperativaDetalle) {
		this.estructuraOperacionCooperativaDetalle = estructuraOperacionCooperativaDetalle;
	}

	public List<EstructuraOperacionCooperativaDetalle> getLstEstructuraOperacionCooperativaDetalle() {
		return lstEstructuraOperacionCooperativaDetalle;
	}

	public void setLstEstructuraOperacionCooperativaDetalle(
			List<EstructuraOperacionCooperativaDetalle> lstEstructuraOperacionCooperativaDetalle) {
		this.lstEstructuraOperacionCooperativaDetalle = lstEstructuraOperacionCooperativaDetalle;
	}

	public boolean isEsTitularCoop() {
		return esTitularCoop;
	}

	public void setEsTitularCoop(boolean esTitularCoop) {
		this.esTitularCoop = esTitularCoop;
	}

	public boolean isEsCodeudorCoop() {
		return esCodeudorCoop;
	}

	public void setEsCodeudorCoop(boolean esCodeudorCoop) {
		this.esCodeudorCoop = esCodeudorCoop;
	}

	public List<EvolucionScoreFinanciero> getLstEvolucionScoreFinanciero() {
		return lstEvolucionScoreFinanciero;
	}

	public void setLstEvolucionScoreFinanciero(List<EvolucionScoreFinanciero> lstEvolucionScoreFinanciero) {
		this.lstEvolucionScoreFinanciero = lstEvolucionScoreFinanciero;
	}

	public BubbleChartModel getEvolScoreFinanciero() {
		return evolScoreFinanciero;
	}

	public void setEvolScoreFinanciero(BubbleChartModel evolScoreFinanciero) {
		this.evolScoreFinanciero = evolScoreFinanciero;
	}
	
	public List<OperacionesVigentesCobranza> getLstOperacionesVigentesCobranza() {
		return lstOperacionesVigentesCobranza;
	}

	public void setLstOperacionesVigentesCobranza(List<OperacionesVigentesCobranza> lstOperacionesVigentesCobranza) {
		this.lstOperacionesVigentesCobranza = lstOperacionesVigentesCobranza;
	}

	public List<OperacionesVigentesServicio> getLstOperacionesVigentesServicio() {
		return lstOperacionesVigentesServicio;
	}

	public void setLstOperacionesVigentesServicio(List<OperacionesVigentesServicio> lstOperacionesVigentesServicio) {
		this.lstOperacionesVigentesServicio = lstOperacionesVigentesServicio;
	}

	public List<OperacionesVigentesEmpresa> getLstOperacionesVigentesEmpresa() {
		return lstOperacionesVigentesEmpresa;
	}

	public void setLstOperacionesVigentesEmpresa(List<OperacionesVigentesEmpresa> lstOperacionesVigentesEmpresa) {
		this.lstOperacionesVigentesEmpresa = lstOperacionesVigentesEmpresa;
	}

	public List<IndicadoresDeuda> getLstIndicadoresDeuda() {
		return lstIndicadoresDeuda;
	}

	public void setLstIndicadoresDeuda(List<IndicadoresDeuda> lstIndicadoresDeuda) {
		this.lstIndicadoresDeuda = lstIndicadoresDeuda;
	}

	public List<OperacionHistoricaTarjeta> getLstOperacionHistoricaTarjeta() {
		return lstOperacionHistoricaTarjeta;
	}

	public void setLstOperacionHistoricaTarjeta(List<OperacionHistoricaTarjeta> lstOperacionHistoricaTarjeta) {
		this.lstOperacionHistoricaTarjeta = lstOperacionHistoricaTarjeta;
	}	

	public String getOpcionUltmesesTC() {
		return opcionUltmesesTC;
	}

	public void setOpcionUltmesesTC(String opcionUltmesesTC) {
		this.opcionUltmesesTC = opcionUltmesesTC;
	}

	public List<OperacionHistoricaTarjeta> getLstOperacionHistoricaTarjetaFiltrada() {
		return lstOperacionHistoricaTarjetaFiltrada;
	}

	public void setLstOperacionHistoricaTarjetaFiltrada(
			List<OperacionHistoricaTarjeta> lstOperacionHistoricaTarjetaFiltrada) {
		this.lstOperacionHistoricaTarjetaFiltrada = lstOperacionHistoricaTarjetaFiltrada;
	}

	public List<OperacionHistoricaBanco> getLstOperacionHistoricaBanco() {
		return lstOperacionHistoricaBanco;
	}

	public void setLstOperacionHistoricaBanco(List<OperacionHistoricaBanco> lstOperacionHistoricaBanco) {
		this.lstOperacionHistoricaBanco = lstOperacionHistoricaBanco;
	}

	public List<OperacionHistoricaBanco> getLstOperacionHistoricaBancoFiltrada() {
		return lstOperacionHistoricaBancoFiltrada;
	}

	public void setLstOperacionHistoricaBancoFiltrada(List<OperacionHistoricaBanco> lstOperacionHistoricaBancoFiltrada) {
		this.lstOperacionHistoricaBancoFiltrada = lstOperacionHistoricaBancoFiltrada;
	}	

	public String getOpcionUltmesesBancos() {
		return opcionUltmesesBancos;
	}

	public void setOpcionUltmesesBancos(String opcionUltmesesBancos) {
		this.opcionUltmesesBancos = opcionUltmesesBancos;
	}

	public boolean getEsTitularHisBanco() {
		return esTitularHisBanco;
	}

	public void setEsTitularHisBanco(boolean esTitularHisBanco) {
		this.esTitularHisBanco = esTitularHisBanco;
	}

	public boolean getEsCodeudorHisBanco() {
		return esCodeudorHisBanco;
	}

	public void setEsCodeudorHisBanco(boolean esCodeudorHisBanco) {
		this.esCodeudorHisBanco = esCodeudorHisBanco;
	}

	public List<OperacionHistoricaCooperativa> getLstOperacionHistoricaCooperativa() {
		return lstOperacionHistoricaCooperativa;
	}

	public void setLstOperacionHistoricaCooperativa(List<OperacionHistoricaCooperativa> lstOperacionHistoricaCooperativa) {
		this.lstOperacionHistoricaCooperativa = lstOperacionHistoricaCooperativa;
	}

	public List<OperacionHistoricaCooperativa> getLstOperacionHistoricaCooperativaFiltrada() {
		return lstOperacionHistoricaCooperativaFiltrada;
	}

	public void setLstOperacionHistoricaCooperativaFiltrada(
			List<OperacionHistoricaCooperativa> lstOperacionHistoricaCooperativaFiltrada) {
		this.lstOperacionHistoricaCooperativaFiltrada = lstOperacionHistoricaCooperativaFiltrada;
	}

	public String getOpcionUltmesesCooperativas() {
		return opcionUltmesesCooperativas;
	}

	public void setOpcionUltmesesCooperativas(String opcionUltmesesCooperativas) {
		this.opcionUltmesesCooperativas = opcionUltmesesCooperativas;
	}

	public boolean getEsTitularHisCooperativa() {
		return esTitularHisCooperativa;
	}

	public void setEsTitularHisCooperativa(boolean esTitularHisCooperativa) {
		this.esTitularHisCooperativa = esTitularHisCooperativa;
	}

	public boolean getEsCodeudorHisCooperativa() {
		return esCodeudorHisCooperativa;
	}

	public void setEsCodeudorHisCooperativa(boolean esCodeudorHisCooperativa) {
		this.esCodeudorHisCooperativa = esCodeudorHisCooperativa;
	}

	public List<OperacionHistoricaEmpresa> getLstOperacionHistoricaEmpresa() {
		return lstOperacionHistoricaEmpresa;
	}

	public void setLstOperacionHistoricaEmpresa(List<OperacionHistoricaEmpresa> lstOperacionHistoricaEmpresa) {
		this.lstOperacionHistoricaEmpresa = lstOperacionHistoricaEmpresa;
	}

	public List<OperacionHistoricaEmpresa> getLstOperacionHistoricaEmpresaFiltrada() {
		return lstOperacionHistoricaEmpresaFiltrada;
	}

	public void setLstOperacionHistoricaEmpresaFiltrada(
			List<OperacionHistoricaEmpresa> lstOperacionHistoricaEmpresaFiltrada) {
		this.lstOperacionHistoricaEmpresaFiltrada = lstOperacionHistoricaEmpresaFiltrada;
	}

	public String getOpcionUltmesesEmpresas() {
		return opcionUltmesesEmpresas;
	}

	public void setOpcionUltmesesEmpresas(String opcionUltmesesEmpresas) {
		this.opcionUltmesesEmpresas = opcionUltmesesEmpresas;
	}

	public boolean getEsTitularHisEmpresas() {
		return esTitularHisEmpresas;
	}

	public void setEsTitularHisEmpresas(boolean esTitularHisEmpresas) {
		this.esTitularHisEmpresas = esTitularHisEmpresas;
	}

	public boolean getEsCodeudorHisEmpresas() {
		return esCodeudorHisEmpresas;
	}

	public void setEsCodeudorHisEmpresas(boolean esCodeudorHisEmpresas) {
		this.esCodeudorHisEmpresas = esCodeudorHisEmpresas;
	}

	public boolean isEsTitularHisServicio() {
		return esTitularHisServicio;
	}

	public void setEsTitularHisServicio(boolean esTitularHisServicio) {
		this.esTitularHisServicio = esTitularHisServicio;
	}

	public boolean getEsCodeudorHisServicio() {
		return esCodeudorHisServicio;
	}

	public void setEsCodeudorHisServicio(boolean esCodeudorHisServicio) {
		this.esCodeudorHisServicio = esCodeudorHisServicio;
	}

	public List<OperacionesHistoricasServicio> getLstOperacionesHistoricasServicio() {
		return lstOperacionesHistoricasServicio;
	}

	public void setLstOperacionesHistoricasServicio(List<OperacionesHistoricasServicio> lstOperacionesHistoricasServicio) {
		this.lstOperacionesHistoricasServicio = lstOperacionesHistoricasServicio;
	}

	public List<OperacionesHistoricasServicio> getLstOperacionesHistoricasServicioFiltrada() {
		return lstOperacionesHistoricasServicioFiltrada;
	}

	public void setLstOperacionesHistoricasServicioFiltrada(
			List<OperacionesHistoricasServicio> lstOperacionesHistoricasServicioFiltrada) {
		this.lstOperacionesHistoricasServicioFiltrada = lstOperacionesHistoricasServicioFiltrada;
	}

	public String getOpcionUltmesesServicios() {
		return opcionUltmesesServicios;
	}

	public void setOpcionUltmesesServicios(String opcionUltmesesServicios) {
		this.opcionUltmesesServicios = opcionUltmesesServicios;
	}

	public String getOpcionUltmesesCobranzas() {
		return opcionUltmesesCobranzas;
	}

	public void setOpcionUltmesesCobranzas(String opcionUltmesesCobranzas) {
		this.opcionUltmesesCobranzas = opcionUltmesesCobranzas;
	}

	public List<OperacionHistoricaCobranza> getLstOperacionHistoricaCobranza() {
		return lstOperacionHistoricaCobranza;
	}

	public void setLstOperacionHistoricaCobranza(List<OperacionHistoricaCobranza> lstOperacionHistoricaCobranza) {
		this.lstOperacionHistoricaCobranza = lstOperacionHistoricaCobranza;
	}

	public List<OperacionHistoricaCobranza> getLstOperacionHistoricaCobranzaFiltrada() {
		return lstOperacionHistoricaCobranzaFiltrada;
	}

	public void setLstOperacionHistoricaCobranzaFiltrada(
			List<OperacionHistoricaCobranza> lstOperacionHistoricaCobranzaFiltrada) {
		this.lstOperacionHistoricaCobranzaFiltrada = lstOperacionHistoricaCobranzaFiltrada;
	}

	public List<RelacionEmpresas> getLstRelacionEmpresas() {
		return lstRelacionEmpresas;
	}

	public void setLstRelacionEmpresas(List<RelacionEmpresas> lstRelacionEmpresas) {
		this.lstRelacionEmpresas = lstRelacionEmpresas;
	}

	public List<DatosContacto> getLstDatosContacto() {
		return lstDatosContacto;
	}

	public void setLstDatosContacto(List<DatosContacto> lstDatosContacto) {
		this.lstDatosContacto = lstDatosContacto;
	}

	public List<TitularConsultado> getLstTitularConsultado() {
		return lstTitularConsultado;
	}

	public void setLstTitularConsultado(List<TitularConsultado> lstTitularConsultado) {
		this.lstTitularConsultado = lstTitularConsultado;
	}

	public List<SemaforoMaximoDiasVencido> getLstSemaforoMaximoDiasVencido() {
		return lstSemaforoMaximoDiasVencido;
	}

	public void setLstSemaforoMaximoDiasVencido(List<SemaforoMaximoDiasVencido> lstSemaforoMaximoDiasVencido) {
		this.lstSemaforoMaximoDiasVencido = lstSemaforoMaximoDiasVencido;
	}

	public BubbleChartModel getSemaforoMaxDiasVenChart() {
		return semaforoMaxDiasVenChart;
	}

	public void setSemaforoMaxDiasVenChart(BubbleChartModel semaforoMaxDiasVenChart) {
		this.semaforoMaxDiasVenChart = semaforoMaxDiasVenChart;
	}

	public List<TendenciaDeuda> getLstTendenciaDeuda() {
		return lstTendenciaDeuda;
	}

	public void setLstTendenciaDeuda(List<TendenciaDeuda> lstTendenciaDeuda) {
		this.lstTendenciaDeuda = lstTendenciaDeuda;
	}

	public LineChartModel getTendenciaDeudaChat() {
		return tendenciaDeudaChat;
	}

	public void setTendenciaDeudaChat(LineChartModel tendenciaDeudaChat) {
		this.tendenciaDeudaChat = tendenciaDeudaChat;
	}

	public LineChartModel getLineModel() {
		return lineModel;
	}

	public void setLineModel(LineChartModel lineModel) {
		this.lineModel = lineModel;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}


	public LineChartModel getLineModelSemaforo() {
		return lineModelSemaforo;
	}


	public void setLineModelSemaforo(LineChartModel lineModelSemaforo) {
		this.lineModelSemaforo = lineModelSemaforo;
	}


	public CxcAvalBuro getExisteCedula() {
		return existeCedula;
	}


	public void setExisteCedula(CxcAvalBuro existeCedula) {
		this.existeCedula = existeCedula;
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

	public Boolean getExiste() {
		return existe;
	}

	public void setExiste(Boolean existe) {
		this.existe = existe;
	}

	public double getTjcConsumoMes() {
		return tjcConsumoMes;
	}

	public void setTjcConsumoMes(double tjcConsumoMes) {
		this.tjcConsumoMes = tjcConsumoMes;
	}

	public double getTjcSaldoTotal() {
		return tjcSaldoTotal;
	}

	public void setTjcSaldoTotal(double tjcSaldoTotal) {
		this.tjcSaldoTotal = tjcSaldoTotal;
	}

	public double getTjcSaldoVencer() {
		return tjcSaldoVencer;
	}

	public void setTjcSaldoVencer(double tjcSaldoVencer) {
		this.tjcSaldoVencer = tjcSaldoVencer;
	}

	public double getTjcSaldoVencido() {
		return tjcSaldoVencido;
	}

	public void setTjcSaldoVencido(double tjcSaldoVencido) {
		this.tjcSaldoVencido = tjcSaldoVencido;
	}

	public double getTjcSaldoNDI() {
		return tjcSaldoNDI;
	}

	public void setTjcSaldoNDI(double tjcSaldoNDI) {
		this.tjcSaldoNDI = tjcSaldoNDI;
	}

	public double getTjcDemandaJudicial() {
		return tjcDemandaJudicial;
	}

	public void setTjcDemandaJudicial(double tjcDemandaJudicial) {
		this.tjcDemandaJudicial = tjcDemandaJudicial;
	}

	public double getTjcCarteraCastigada() {
		return tjcCarteraCastigada;
	}

	public void setTjcCarteraCastigada(double tjcCarteraCastigada) {
		this.tjcCarteraCastigada = tjcCarteraCastigada;
	}

	public double getTjcUltValorPagado() {
		return tjcUltValorPagado;
	}

	public void setTjcUltValorPagado(double tjcUltValorPagado) {
		this.tjcUltValorPagado = tjcUltValorPagado;
	}

	public double getTjcValorMinPagar() {
		return tjcValorMinPagar;
	}

	public void setTjcValorMinPagar(double tjcValorMinPagar) {
		this.tjcValorMinPagar = tjcValorMinPagar;
	}

	public double getTjcCuotaMensual() {
		return tjcCuotaMensual;
	}

	public void setTjcCuotaMensual(double tjcCuotaMensual) {
		this.tjcCuotaMensual = tjcCuotaMensual;
	}

	public String getScorePantalla() {
		return scorePantalla;
	}

	public void setScorePantalla(String scorePantalla) {
		this.scorePantalla = scorePantalla;
	}

	public double getBcMontoOriginal() {
		return bcMontoOriginal;
	}

	public void setBcMontoOriginal(double bcMontoOriginal) {
		this.bcMontoOriginal = bcMontoOriginal;
	}

	public double getBcSaldoTotal() {
		return bcSaldoTotal;
	}

	public void setBcSaldoTotal(double bcSaldoTotal) {
		this.bcSaldoTotal = bcSaldoTotal;
	}

	public double getBcSaldoVencer() {
		return bcSaldoVencer;
	}

	public void setBcSaldoVencer(double bcSaldoVencer) {
		this.bcSaldoVencer = bcSaldoVencer;
	}

	public double getBcSaldoVencido() {
		return bcSaldoVencido;
	}

	public void setBcSaldoVencido(double bcSaldoVencido) {
		this.bcSaldoVencido = bcSaldoVencido;
	}

	public double getBcSaldoNDI() {
		return bcSaldoNDI;
	}

	public void setBcSaldoNDI(double bcSaldoNDI) {
		this.bcSaldoNDI = bcSaldoNDI;
	}

	public double getBcDemandaJudicial() {
		return bcDemandaJudicial;
	}

	public void setBcDemandaJudicial(double bcDemandaJudicial) {
		this.bcDemandaJudicial = bcDemandaJudicial;
	}

	public double getBcCarteraCastigada() {
		return bcCarteraCastigada;
	}

	public void setBcCarteraCastigada(double bcCarteraCastigada) {
		this.bcCarteraCastigada = bcCarteraCastigada;
	}

	public double getBcCuotaMensual() {
		return bcCuotaMensual;
	}

	public void setBcCuotaMensual(double bcCuotaMensual) {
		this.bcCuotaMensual = bcCuotaMensual;
	}

	public double getCoopMontoOriginal() {
		return coopMontoOriginal;
	}

	public void setCoopMontoOriginal(double coopMontoOriginal) {
		this.coopMontoOriginal = coopMontoOriginal;
	}

	public double getCoopSaldoTotal() {
		return coopSaldoTotal;
	}

	public void setCoopSaldoTotal(double coopSaldoTotal) {
		this.coopSaldoTotal = coopSaldoTotal;
	}

	public double getCoopSaldoVencer() {
		return coopSaldoVencer;
	}

	public void setCoopSaldoVencer(double coopSaldoVencer) {
		this.coopSaldoVencer = coopSaldoVencer;
	}

	public double getCoopSaldoVencido() {
		return coopSaldoVencido;
	}

	public void setCoopSaldoVencido(double coopSaldoVencido) {
		this.coopSaldoVencido = coopSaldoVencido;
	}

	public double getCoopSaldoNDI() {
		return coopSaldoNDI;
	}

	public void setCoopSaldoNDI(double coopSaldoNDI) {
		this.coopSaldoNDI = coopSaldoNDI;
	}

	public double getCoopDemandaJudicial() {
		return coopDemandaJudicial;
	}

	public void setCoopDemandaJudicial(double coopDemandaJudicial) {
		this.coopDemandaJudicial = coopDemandaJudicial;
	}

	public double getCoopCarteraCastigada() {
		return coopCarteraCastigada;
	}

	public void setCoopCarteraCastigada(double coopCarteraCastigada) {
		this.coopCarteraCastigada = coopCarteraCastigada;
	}

	public double getCoopCuotaMensual() {
		return coopCuotaMensual;
	}

	public void setCoopCuotaMensual(double coopCuotaMensual) {
		this.coopCuotaMensual = coopCuotaMensual;
	}

	public double getEmpSaldoActual() {
		return empSaldoActual;
	}

	public void setEmpSaldoActual(double empSaldoActual) {
		this.empSaldoActual = empSaldoActual;
	}

	public double getEmpCuotaMensual() {
		return empCuotaMensual;
	}

	public void setEmpCuotaMensual(double empCuotaMensual) {
		this.empCuotaMensual = empCuotaMensual;
	}

	public double getEmpSaldoVencer() {
		return empSaldoVencer;
	}

	public void setEmpSaldoVencer(double empSaldoVencer) {
		this.empSaldoVencer = empSaldoVencer;
	}

	public double getEmpSaldoVencido() {
		return empSaldoVencido;
	}

	public void setEmpSaldoVencido(double empSaldoVencido) {
		this.empSaldoVencido = empSaldoVencido;
	}

	public double getEmpSaldoNDI() {
		return empSaldoNDI;
	}

	public void setEmpSaldoNDI(double empSaldoNDI) {
		this.empSaldoNDI = empSaldoNDI;
	}

	public double getEmpSaldoDemandaJudicial() {
		return empSaldoDemandaJudicial;
	}

	public void setEmpSaldoDemandaJudicial(double empSaldoDemandaJudicial) {
		this.empSaldoDemandaJudicial = empSaldoDemandaJudicial;
	}

	public double getEmpCarteraCastigada() {
		return empCarteraCastigada;
	}

	public void setEmpCarteraCastigada(double empCarteraCastigada) {
		this.empCarteraCastigada = empCarteraCastigada;
	}

	public double getServCuotaMensual() {
		return servCuotaMensual;
	}

	public void setServCuotaMensual(double servCuotaMensual) {
		this.servCuotaMensual = servCuotaMensual;
	}

	public double getServSaldoVencer() {
		return servSaldoVencer;
	}

	public void setServSaldoVencer(double servSaldoVencer) {
		this.servSaldoVencer = servSaldoVencer;
	}

	public double getServSaldoActual() {
		return servSaldoActual;
	}

	public void setServSaldoActual(double servSaldoActual) {
		this.servSaldoActual = servSaldoActual;
	}

	public double getServSaldoVencido() {
		return servSaldoVencido;
	}

	public void setServSaldoVencido(double servSaldoVencido) {
		this.servSaldoVencido = servSaldoVencido;
	}

	public double getCobzMontoOriginal() {
		return cobzMontoOriginal;
	}

	public void setCobzMontoOriginal(double cobzMontoOriginal) {
		this.cobzMontoOriginal = cobzMontoOriginal;
	}

	public double getCobzSaldoTotal() {
		return cobzSaldoTotal;
	}

	public void setCobzSaldoTotal(double cobzSaldoTotal) {
		this.cobzSaldoTotal = cobzSaldoTotal;
	}

	public double getCobzSaldoVencer() {
		return cobzSaldoVencer;
	}

	public void setCobzSaldoVencer(double cobzSaldoVencer) {
		this.cobzSaldoVencer = cobzSaldoVencer;
	}

	public double getCobzSaldoVencido() {
		return cobzSaldoVencido;
	}

	public void setCobzSaldoVencido(double cobzSaldoVencido) {
		this.cobzSaldoVencido = cobzSaldoVencido;
	}

	public double getCobzSaldoNDI() {
		return cobzSaldoNDI;
	}

	public void setCobzSaldoNDI(double cobzSaldoNDI) {
		this.cobzSaldoNDI = cobzSaldoNDI;
	}

	public double getCobzDemandaJudicial() {
		return cobzDemandaJudicial;
	}

	public void setCobzDemandaJudicial(double cobzDemandaJudicial) {
		this.cobzDemandaJudicial = cobzDemandaJudicial;
	}

	public double getCobzCarteraCastigada() {
		return cobzCarteraCastigada;
	}

	public void setCobzCarteraCastigada(double cobzCarteraCastigada) {
		this.cobzCarteraCastigada = cobzCarteraCastigada;
	}

	public double getCobzCuotaEstimada() {
		return cobzCuotaEstimada;
	}

	public void setCobzCuotaEstimada(double cobzCuotaEstimada) {
		this.cobzCuotaEstimada = cobzCuotaEstimada;
	}

	public DatosGeneralesEmpresa getDatosGeneralesEmpresa() {
		return datosGeneralesEmpresa;
	}

	public void setDatosGeneralesEmpresa(DatosGeneralesEmpresa datosGeneralesEmpresa) {
		this.datosGeneralesEmpresa = datosGeneralesEmpresa;
	}

	public ScoreEmpresa getScoreEmpresa() {
		return scoreEmpresa;
	}

	public void setScoreEmpresa(ScoreEmpresa scoreEmpresa) {
		this.scoreEmpresa = scoreEmpresa;
	}

	public DonutChartModel getScoreEmpresaDonut() {
		return scoreEmpresaDonut;
	}

	public void setScoreEmpresaDonut(DonutChartModel scoreEmpresaDonut) {
		this.scoreEmpresaDonut = scoreEmpresaDonut;
	}

	public List<DetalleEmpresa> getLstDetalleEmpresas() {
		return lstDetalleEmpresas;
	}

	public void setLstDetalleEmpresas(List<DetalleEmpresa> lstDetalleEmpresas) {
		this.lstDetalleEmpresas = lstDetalleEmpresas;
	}

	public List<ResumenPrincipalesCuentasFinancieras> getLstResumenPrincipalesCuentasFinancieras() {
		return lstResumenPrincipalesCuentasFinancieras;
	}

	public void setLstResumenPrincipalesCuentasFinancieras(
			List<ResumenPrincipalesCuentasFinancieras> lstResumenPrincipalesCuentasFinancieras) {
		this.lstResumenPrincipalesCuentasFinancieras = lstResumenPrincipalesCuentasFinancieras;
	}

	public List<CuentasEstadosFinancieros> getLstCuentasEstadosFinancieros() {
		return lstCuentasEstadosFinancieros;
	}

	public void setLstCuentasEstadosFinancieros(List<CuentasEstadosFinancieros> lstCuentasEstadosFinancieros) {
		this.lstCuentasEstadosFinancieros = lstCuentasEstadosFinancieros;
	}

	public List<IndicesFinancieros> getLstIndicesFinancieros() {
		return lstIndicesFinancieros;
	}

	public void setLstIndicesFinancieros(List<IndicesFinancieros> lstIndicesFinancieros) {
		this.lstIndicesFinancieros = lstIndicesFinancieros;
	}

	public List<PrincipalesAccionistas> getLstPrincipalesAccionistas() {
		return lstPrincipalesAccionistas;
	}

	public void setLstPrincipalesAccionistas(List<PrincipalesAccionistas> lstPrincipalesAccionistas) {
		this.lstPrincipalesAccionistas = lstPrincipalesAccionistas;
	}

	public List<RepresentantesLegales> getLstRepresentantesLegales() {
		return lstRepresentantesLegales;
	}

	public void setLstRepresentantesLegales(List<RepresentantesLegales> lstRepresentantesLegales) {
		this.lstRepresentantesLegales = lstRepresentantesLegales;
	}

	public List<Contactabilidad> getLstContactabilidad() {
		return lstContactabilidad;
	}

	public void setLstContactabilidad(List<Contactabilidad> lstContactabilidad) {
		this.lstContactabilidad = lstContactabilidad;
	}

	public int getAnioActual() {
		return anioActual;
	}

	public void setAnioActual(int anioActual) {
		this.anioActual = anioActual;
	}

	public boolean isMostrarPantallaRuc() {
		return mostrarPantallaRuc;
	}

	public void setMostrarPantallaRuc(boolean mostrarPantallaRuc) {
		this.mostrarPantallaRuc = mostrarPantallaRuc;
	}

	public String getNombreUsuarioConsulta() {
		return nombreUsuarioConsulta;
	}

	public void setNombreUsuarioConsulta(String nombreUsuarioConsulta) {
		this.nombreUsuarioConsulta = nombreUsuarioConsulta;
	}

	public DetalleTarjetaCredito getDetalleTarjetaCreditoSeleccionada() {
		return detalleTarjetaCreditoSeleccionada;
	}

	public void setDetalleTarjetaCreditoSeleccionada(DetalleTarjetaCredito detalleTarjetaCreditoSeleccionada) {
		this.detalleTarjetaCreditoSeleccionada = detalleTarjetaCreditoSeleccionada;
	}

	
}
