package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
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
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;

import com.casabaca.administration.ejb.dto.UsuarioDto;
import com.casabaca.caja.ejb.service.PagoCajaServiceLocal;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.DescargaArchivoFacturacion;
import com.casabaca.common.EjecutaComandoUtils;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.NumericUtils;
import com.casabaca.common.ejb.dao.FacturasVehiculoDaoLocal;
import com.casabaca.common.ejb.dto.DeudaClientesDto;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.CotizacionVehiculo;
import com.casabaca.common.ejb.model.CxcClaseCliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CotizacionVehiculoServicioLocal;
import com.casabaca.common.ejb.service.CxcClaseClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.ZonasDto;
import com.casabaca.cxc.ejb.modelo.CxcCanalContactado;
import com.casabaca.cxc.ejb.modelo.CxcCanalGestion;
import com.casabaca.cxc.ejb.modelo.CxcResultadoGestion;
import com.casabaca.cxc.ejb.modelo.CxcSeguimientosCartera;
import com.casabaca.cxc.ejb.modelo.CxcSeguimientosCarteraPK;
import com.casabaca.cxc.ejb.modelo.CxcTipoSeguimiento;
import com.casabaca.cxc.ejb.servicio.CxcCanalContactadoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcCanalGestionServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcResultadoGestionServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcSeguimientosCarteraServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcTipoSeguimientoServiceLocal;
import com.casabaca.cxc.ejb.servicio.RangoAntiguedadServicesLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogUsuariosController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogZonasController;
import com.casabaca.prime.cxc.procesos.datamanager.SeguimientoCobranzasDatamanager;
import com.casabaca.s3s.ejb.dto.AgenciaDTO;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;
import com.casabaca.vehiculos.ejb.modelo.TipoFinanciamiento;
import com.casabaca.vehiculos.ejb.servicio.TipoFinanciamientoServicioLocal;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * Author: jl_reyes Pantalla para seguimiento de cobranzas en las empresas
 */
@ManagedBean
@ViewScoped
public class SeguimientoCobranzasController extends CommonController implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3758032821153962288L;

	static Logger logger = Logger.getLogger(SeguimientoCobranzasController.class);

	private static String CANALES = "CANALR";
	private static String VEH = "VEH";
	private static String EXCEL = "ESTADOCUENTA.xls";
	private static String RESUMEN = "/reportes/EstadoCuentaClienteResumido.jasper";
	private static String DETALLE = "/reportes/EstadoCuentaClienteDetallado.jasper";
	private static final String PDF_PATH_FC = "gnvoiceCasabaca/descargarDocumento/obtenerFacturaPDF/";
	private static final String XML_PATH_FC = "gnvoiceCasabaca/descargarDocumento/obtenerFacturaXML/";
	private static final String PDF_PATH_NC = "gnvoiceCasabaca/descargarDocumento/obtenerNotaCreditoPDF/";
	private static final String XML_PATH_NC = "gnvoiceCasabaca/descargarDocumento/obtenerNotaCreditoXML/";
	private static final String RUTA_PRINCIPAL_ARC_FISICO = "/opt/";

	@EJB(lookup = NombreJNDI.CXC_RANGOS_ANTIGUEDAD)
	private RangoAntiguedadServicesLocal rangoAntiguedadServicesLocal;

	@EJB(lookup = NombreJNDI.TIPO_FINANCIAMIENTO_SERVICIO_BEAN)
	private TipoFinanciamientoServicioLocal tipoFinanciamientoServicioLocal;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioServicioLocal;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal cxcNativeServiceLocal;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;

	@EJB(lookup = NombreJNDI.CXC_SEGUIMIENTO_CARTERA_SERVICE)
	private CxcSeguimientosCarteraServiceLocal cxcSeguimientosCarteraServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_CLASE_CLIENTE_SERVICE)
	private CxcClaseClienteServiceLocal cxcClaseClienteServiceLocal;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_TIPO_SEGUIMIENTO_SERVICE)
	private CxcTipoSeguimientoServiceLocal cxcTipoSeguimientoServiceLocal;

	@EJB(lookup = NombreJNDI.PAGO_CAJA_SERVICE)
	private PagoCajaServiceLocal pagoCajaServiceLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.COTIZACION_VEHICULO_SERVICIO_BEAN)
	private CotizacionVehiculoServicioLocal cotizacionVehiculoServicio;

	@EJB(lookup = NombreJNDI.FACTURAS_VEHICULOS_DAO)
	private FacturasVehiculoDaoLocal facVentasCabDao;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	@EJB(lookup = NombreJNDI.CXC_RESULTADO_GESTION_SERVICE)
	private CxcResultadoGestionServiceLocal cxcResultadoServiceLocal;
	
	@EJB(lookup = NombreJNDI.CXC_CANAL_GESTION_SERVICE)
	private CxcCanalGestionServiceLocal cxcCanalGestionServiceLocal;
	@EJB(lookup = NombreJNDI.CXC_CANAL_CONTACTADO_SERVICE)
	private CxcCanalContactadoServiceLocal cxcCanalContactadoServiceLocal;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	@ManagedProperty("#{dialogUsuariosController}")
	private DialogUsuariosController dialogUsuariosController;

	@ManagedProperty("#{dialogZonasController}")
	private DialogZonasController dialogZonasController;

	@ManagedProperty(value = "#{seguimientoCobranzasDataManager}")
	private SeguimientoCobranzasDatamanager seguimientoCobranzasDataManager;

	private List<Object[]> listadoRangoAntiguedad;
	private List<TipoFinanciamiento> listadoTipoFinanciamiento;
	private List<ParamDet> listadoCanales;
	private List<LineaNegocio> listadoLineasNegocio;
	private List<Object[]> listadoCartera;
	private List<Object[]> listadoDetalleCartera;
	private List<Object[]> listadoDetalleCarteraSelected;
	private List<CxcClaseCliente> listaClaseClientes;
	private List<AgenciaDTO> listadoAgencias;
	private List<CxcTipoSeguimiento> listadoTipoSeguimiento;
	private List<CxcSeguimientosCartera> listadoSeguimientoCartera;
	private List<Object[]> listadoSegClientes;
	private List<CxcResultadoGestion> listadoResultadoGestion;
	private List<CxcCanalGestion> listadoCanalGestion;
	private List<CxcCanalContactado> listadoContactado;
	private Cliente cliente;
	private UsuarioDto usuarioDto;
	private ZonasDto zonasDto;
	private CxcSeguimientosCartera cxcSeguimientosCartera;
	private Object[] deudaSeleccionada;
	private Object[] docSeleccionado;
	private String canal;
	private String canalSelected;
	private String rangoAntiguedad;
	private String rangoAntiguedadSelect;
	private String tipoFinanciamiento;
	private String tipoFinanciamientoSelect;
	private String lineaNegocioSelected;
	private String lineaNegocio;
	private String zona;
	private String zonaSelected;
	private String noClienteSelected;
	private Cliente clienteSeleccionado;
	private String origen;
	private String clase;
	private String agencia;
	private String tipoReporte;
	private String urlReporte;
	private String lineaDeuda;
	private String tipoArchivo;
	private CotizacionVehiculo cotizacionVeh;
	private Boolean incluyeAlistamiento;

	@PostConstruct
	public void init() {
		try {
			origen = getRequestParameter("origen");
			if ("D".equalsIgnoreCase(origen)) {
				rangoAntiguedadSelect = getRequestParameter("rangoAntiguedad") == null
						? getRequestParameter("rangoAntiguedadSelected")
						: getRequestParameter("rangoAntiguedad");
				tipoFinanciamientoSelect = getRequestParameter("tipoFinanciamiento");
				noClienteSelected = getRequestParameter("noCLiente");
				lineaNegocio = getRequestParameter("lineaSeleccionada");
				canalSelected = getRequestParameter("canalSelected");
				zonaSelected = getRequestParameter("zonasSelected");
				clienteSeleccionado = clienteService.getClienteByNoCLienteAndCia(getCompania().getNoCia(),
						noClienteSelected != null ? new Long(noClienteSelected)
								: new Long(getRequestParameter("codigoCliente")));
				cxcSeguimientosCartera = new CxcSeguimientosCartera(
						new CxcSeguimientosCarteraPK(0L, getCompania().getNoCia(), null));
				listadoTipoSeguimiento = cxcTipoSeguimientoServiceLocal.obtenerPorNocia(getCompania().getNoCia());
				usuarioDto = new UsuarioDto();
				usuarioDto.setUsuario(getUsuario().getUsuario());
				usuarioDto.setNombre(getUsuario().getNombre());
				cargarDetalle();

			} else if ("C".equalsIgnoreCase(origen)) {
				lineaNegocioSelected = getRequestParameter("lineaSeleccionada") == null ? lineaNegocio
						: getRequestParameter("lineaSeleccionada");
				zona = getRequestParameter("zonasSelected") == null ? zonaSelected
						: getRequestParameter("zonasSelected");
				canal = getRequestParameter("canalSelected") == null ? canalSelected
						: getRequestParameter("canalSelected");
				rangoAntiguedad = getRequestParameter("rangoAntiguedadSelected") == null ? rangoAntiguedadSelect
						: getRequestParameter("rangoAntiguedadSelected");
				tipoFinanciamiento = getRequestParameter("tipoFinanciamiento");
				agencia = getRequestParameter("agenciaSelected");
				clase = getRequestParameter("claseSelected");
				zonasDto = new ZonasDto();
				zonasDto.setZona(zona);
				noClienteSelected = getRequestParameter("noCLienteSelected");
				clienteSeleccionado = noClienteSelected == null ? null
						: clienteService.getClienteByNoCLienteAndCia(getCompania().getNoCia(),
								new Long(noClienteSelected));
				cargarDataMenus();
				ejecutarJavascript("document.getElementById(\"frmBotones:buscar\").click()");

			} else {
				listadoRangoAntiguedad = new ArrayList<Object[]>();
				listadoDetalleCartera = new ArrayList<Object[]>();
				listadoTipoFinanciamiento = new ArrayList<TipoFinanciamiento>();
				listadoCanales = new ArrayList<ParamDet>();
				listadoCartera = new ArrayList<Object[]>();
				listadoDetalleCarteraSelected = new ArrayList<Object[]>();
				deudaSeleccionada = null;
				canal = null;
				zona = null;
				zonasDto = new ZonasDto();
				tipoFinanciamiento = null;
				rangoAntiguedad = null;
				lineaNegocioSelected = null;
				tipoReporte = null;
				urlReporte = null;
				docSeleccionado = null;
				tipoArchivo = null;
				seguimientoCobranzasDataManager = new SeguimientoCobranzasDatamanager();
				seguimientoCobranzasDataManager.setListadoCarteraConsulta(null);
				cxcSeguimientosCartera = new CxcSeguimientosCartera(
						new CxcSeguimientosCarteraPK(0L, getCompania().getNoCia(), null));

				cargarDataMenus();

			}

		} catch (Exception e) {
			logger.error(e);
		}

	}

	public void cargarDataMenus() {

		try {
			listadoRangoAntiguedad = rangoAntiguedadServicesLocal.obtenerRangosxNocia(getCompania().getNoCia());
			listadoLineasNegocio = lineaNegocioServicioLocal.buscarLineaNegocioLista(getCompania().getNoCia());

			listadoTipoFinanciamiento = tipoFinanciamientoServicioLocal
					.findByTipoFinanciaXnoCia(getCompania().getNoCia());

			listaClaseClientes = cxcClaseClienteServiceLocal.findByNoCiaNegocio(getCompania().getNoCia(), VEH);

			listadoAgencias = agenciaServiceLocal.buscarAgenciasByCompania(getCompania().getNoCia());
			listadoCanales = paramDetServiceLocal.consultarPorCodigoCabecera(getCompania().getNoCia(), CANALES);

		} catch (FindException e) {
			logger.error(e);
		}

	}

	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);

	}

	public void cargarZonas() {
		this.dialogZonasController.setNombreDialog("dialogZonasSW");
		this.dialogZonasController.cargarZonas();
		accionesDialog("dialogZonasSW", Boolean.TRUE);

	}

	private void cargarDetalle() {

		listadoCanalGestion = cxcCanalGestionServiceLocal.obtenerNocia(getCompania().getNoCia());
		listadoContactado = cxcCanalContactadoServiceLocal.obtenerNocia(getCompania().getNoCia());
		listadoResultadoGestion = cxcResultadoServiceLocal.obtenerNocia(getCompania().getNoCia());

		listadoDetalleCartera = cxcNativeServiceLocal.obtenerDetalleCartera(getCompania().getNoCia(), lineaNegocio,
				clienteSeleccionado.getClientePK().getNoCliente(), zona, canalSelected,
				rangoAntiguedadSelect == null ? null : rangoAntiguedadSelect.replace("%", " "),
				tipoFinanciamientoSelect, agencia, clase);
		for (Object[] obj : listadoDetalleCartera) {
			DeudaClientesDto duedaClientesDto = new DeudaClientesDto();
			duedaClientesDto.setNoDocu(obj[11].toString());
			duedaClientesDto.setSerieFisico(obj[16].toString());
			duedaClientesDto.setClaseDocumento(obj[9].toString());
			duedaClientesDto.setFechaVence((Date) obj[3]);
			duedaClientesDto.setTasaFinancia(obj[15] == null ? 0D : new Double(obj[15].toString()));
			duedaClientesDto.setCodF(obj[14].toString());
			duedaClientesDto.setNoFisico(obj[2].toString());
			duedaClientesDto.setMontoOriginal(new Double(obj[4].toString()));
			duedaClientesDto.setSaldo(new Double(obj[5].toString()));

			obj[18] = pagoCajaServiceLocal.obtenerDescInteres(getCompania().getNoCia(), new Date(),
					new Long(obj[17].toString()), duedaClientesDto);
			obj[19] = obtenerEstiloFila(obj[12] == null ? null : (Date) obj[12]);

		}

	}

	private String obtenerEstiloFila(Date fechaProxSeg) {
		String estilo = null;
		if (fechaProxSeg != null) {
			int resultSeg = FechaUtils.truncarFecha(fechaProxSeg).compareTo(FechaUtils.truncarFecha(new Date()));
			estilo = resultSeg > 0 ? "menorFecha" : (resultSeg < 0 ? "mayorfecha" : "fechaDia");
		}else {
			
			estilo ="noseguimiento";
		}
		return estilo;
	}

	public void regresarPantalla() throws IOException {

		String url = "/cxc-web-prime/jsf/procesos/seguimientoCobranzas.jsf?origen=C"
				.concat(cliente != null ? "&noCLienteSelected=".concat(cliente.getClientePK().getNoCliente().toString())
						: "")
				.concat(zona != null ? "&zona=".concat(zona) : "").concat(canal != null ? "&canal=".concat(canal) : "")
				.concat(tipoFinanciamiento != null ? "&tipoFinanciamiento=".concat(tipoFinanciamiento) : "")
				.concat(lineaNegocio != null ? "&lineaSeleccionada=".concat(lineaNegocio) : "")
				.concat(rangoAntiguedadSelect != null ? "&rangoAntiguedadSelected=".concat(rangoAntiguedadSelect) : "")
				.concat(canalSelected != null ? "&canalSelected=".concat(canalSelected) : "")
				.concat(zonaSelected != null ? "&zonasSelected=".concat(zonaSelected) : "");
		super.redirect(url);
	}

	public void refrescar() {
		listadoRangoAntiguedad = new ArrayList<Object[]>();
		listadoDetalleCartera = new ArrayList<Object[]>();
		listadoTipoFinanciamiento = new ArrayList<TipoFinanciamiento>();
		listadoCanales = new ArrayList<ParamDet>();
		listadoCartera = new ArrayList<Object[]>();
		deudaSeleccionada = null;
		canal = null;
		zona = null;
		tipoFinanciamiento = null;
		rangoAntiguedad = null;
		lineaNegocioSelected = null;
		rangoAntiguedadSelect = null;
		tipoFinanciamientoSelect = null;
		lineaNegocioSelected = null;
		noClienteSelected = null;
		lineaNegocio = null;
		listadoSeguimientoCartera = new ArrayList<CxcSeguimientosCartera>();

	}

	public void detalleDeuda(Object[] objetoSeleccionado) throws IOException {
		deudaSeleccionada = objetoSeleccionado;

		String url = "/cxc-web-prime/jsf/procesos/seguimientoDetalleDeuda.jsf?origen=D".concat("&noCLiente=")
				.concat(String.valueOf(deudaSeleccionada[0])).concat("&lineaDedua=");
		url = url.concat(zona != null ? "&zona=".concat(zona) : "").concat(canal != null ? "&canal=".concat(canal) : "")
				.concat(tipoFinanciamiento != null ? "&tipoFinanciamiento=".concat(tipoFinanciamiento) : "")
				.concat(lineaNegocioSelected != null ? "&lineaSeleccionada=".concat(lineaNegocioSelected) : "")
				.concat(rangoAntiguedad != null ? "&rangoAntiguedad=".concat(rangoAntiguedad) : "")
				.concat(canal != null ? "&canalSelected=".concat(canal) : "")
				.concat(zona != null ? "&zonasSelected=".concat(zona) : "")
				.concat(agencia != null ? "agenciaSelected=".concat(agencia) : "");
		super.redirect(url);

	}

	public void buscar() {

		listadoCartera = cxcNativeServiceLocal.obtenerCarteraClientes(getCompania().getNoCia(), lineaNegocioSelected,
				cliente == null ? null : cliente.getClientePK().getNoCliente(), zona, canal, rangoAntiguedad,
				tipoFinanciamiento, agencia, clase,
				this.incluyeAlistamiento == null ? true : this.incluyeAlistamiento);

	}

	public void actualizarSms() {

		try {
			clienteService.update(clienteSeleccionado);

			info("Cliente Actualizado con exito");
		} catch (UpdateException e) {
			error("Ocurrio un error al Actualizar Cliente ");
			logger.error(e);
		}

	}

	public void grabarSeguimiento() throws IOException {

		try {
			if (listadoDetalleCarteraSelected != null && listadoDetalleCarteraSelected.size() > 0) {
				String smsValida = validarCamposSeg();
				if (smsValida != null) {
					error(smsValida);
					return;
				}

				StringBuilder msgTabla = new StringBuilder();
				msgTabla.append("<table style='border-color: black;' border='1' >");
				msgTabla.append("<tr><td style='background-color: lightblue;' ><b> Factura </b></td> ");
				msgTabla.append("<td style='background-color: lightblue;' ><b>Fecha<b></td>");
				msgTabla.append(
						"<td style='background-color: lightblue;' ><b>Saldo<b></td> <td style='background-color: lightblue;'><b>Observacion<b></td> </tr>");
				for (Object[] obj : listadoDetalleCarteraSelected) {

					msgTabla.append("<tr><td>").append(obj[2]).append("</td> <td>")
							.append(FechaUtils.formatearFecha((Date) obj[1], FechaUtils.patronFechaTiempo))
							.append("</td> <td>").append(obj[5]).append("</td> <td>")
							.append(cxcSeguimientosCartera.getComentario()).append("</td> </tr>");

					cxcSeguimientosCartera.setFechaCreacion(new Date());
					cxcSeguimientosCartera.setUsuarioCreacion(getUsuario().getUsuario());
					cxcSeguimientosCartera.setFechaSeguimiento(new Date());
					cxcSeguimientosCartera.setUsuarioCobro(usuarioDto.getUsuario());
					cxcSeguimientosCartera.setNoCliente(clienteSeleccionado.getClientePK().getNoCliente());
					cxcSeguimientosCartera.setSaldoGestion(NumericUtils.toBigDecimal(obj[5]));
					cxcSeguimientosCartera
							.setId(new CxcSeguimientosCarteraPK(0L, getCompania().getNoCia(), obj[11].toString()));
					cxcSeguimientosCarteraServiceLocal.insertar(cxcSeguimientosCartera);
				}
				// envio mail

				if (!usuarioDto.getUsuario().equals(getUsuario().getUsuario())) {
					String mailTo = usuarioSisService.mailUsuario(usuarioDto.getUsuario());
					String mailFrom = usuarioSisService.mailUsuario(getUsuario().getUsuario());
					String subject = "Asignacion de Seguimiento Cobranzas " + " Cliente: "
							+ clienteSeleccionado.getNombre();
					StringBuffer mensaje = new StringBuffer();
					mensaje = new StringBuffer();
					mensaje.append(usuarioDto.getNombre() + ", " + "<br><br>");
					mensaje.append("El usuario " + getUsuario().getNombre());
					mensaje.append(" le asign&oacute; el seguimiento de cobranzas del Cliente: "
							+ clienteSeleccionado.getNombre()).append(", RUC: " + clienteSeleccionado.getCedula())
							.append("<br><br>");
					mensaje.append(" Con el siguiente detalle : " + "<br><br>");
					mensaje.append(msgTabla);
					mensaje.append("</table>" + "<br><br>");
					mensaje.append("Saludos Cordiales." + "<br><br>");

					mailService.sendEmail(getSisMailServidores(getCompania().getNoCia()), mailFrom, mailTo, subject,
							mensaje.toString());

				}

				info("Seguimiento almacenado con exito");
				limpiarDatSeg();
			} else {

				error("Debe Seleccionar al menos un registro para grabar el seguimiento");

			}
		} catch (InsertException e) {
			error("Ocurrio un error al Almancenar Seguimiento de Cartera");
			logger.error(e);

		} catch (FindException e) {
			logger.error(e);
		} catch (GeneralException e) {
			logger.error(e);
		}

	}

	/**
	 * Comprobar datos obligatorios del seguimiento
	 * 
	 * @return
	 */
	private String validarCamposSeg() {
		String mensaje = null;
		if (cxcSeguimientosCartera.getSecuencialSeguimiento() == null) {
			mensaje = "Debe seleccionar el tipo de seguimiento";
		} else if (cxcSeguimientosCartera.getCanalGestion() == null) {
			mensaje = "Debe seleccionar el canal de gestion";
		} else if (cxcSeguimientosCartera.getContactado() == null) {
			mensaje = "Debe seleccionar el contactado";
		} else if (cxcSeguimientosCartera.getCodigoResultado() == null) {
			mensaje = "Debe seleccionar el resultado de la gestion";
		} else if (cxcSeguimientosCartera.getComentario() == null
				|| cxcSeguimientosCartera.getComentario().trim().isEmpty()) {
			mensaje = "Debe ingresar un comentario para grabar un seguimiento";
		}
		return mensaje;
	}

	/**
	 * Permite inicializar los campos de seguimiento
	 */
	private void limpiarDatSeg() {
		cxcSeguimientosCartera.setSecuencialSeguimiento(null);
		cxcSeguimientosCartera.setCanalGestion(null);
		cxcSeguimientosCartera.setContactado(null);
		cxcSeguimientosCartera.setCodigoResultado(null);
		cxcSeguimientosCartera.setComentario(null);
	}

	/**
	 * Permite asignar la fila seleccionada y ejecutar el boton de descarga
	 * 
	 * @param facVentasCab
	 * @param tipo
	 */
	public void asignarFilaSeleccionada(Object[] facVentasCab, String tipo) {
		this.docSeleccionado = facVentasCab;

		if (this.docSeleccionado[13] != null && this.docSeleccionado[13].toString().length() >= 49) {
			this.tipoArchivo = tipo;
			ejecutarJavascript("document.getElementById(\"frmBotones:descargar\").click()");
		} else {
			super.warn("No se ha generado la clave de acceso");
		}

	}

	/**
	 * Permite descargar la factura en pdf o xml
	 * 
	 * @param facVentasCab
	 * @param tipo         pdf o xml
	 */
	public void descargarDocumento() {
		String ip = getCompania().getIpWebServer();
		if (ip.length() > 0) {
			ip = ip.replace("8080", "18080");
			ip = ip.replace("/", "");
		}
		String ruta = "pdf".equals(tipoArchivo)
				? "FC".equals(this.docSeleccionado[0].toString()) ? PDF_PATH_FC : PDF_PATH_NC
				: "FC".equals(this.docSeleccionado[0].toString()) ? XML_PATH_FC : XML_PATH_NC;

		String[] dat = ip.split(":");
		Map<String, String> mapaDatos = new HashMap<>();
		mapaDatos.put("IP_SERVIDOR_FAC", dat[1]);
		mapaDatos.put("PUERTO_SERVIDOR_FAC", dat[2]);
		mapaDatos.put("DIRECTORIO_PDF", "/".concat(ruta));
		mapaDatos.put("DIRECTORIO_XML", "/".concat(ruta));
		mapaDatos.put("RUTA_FISICA", RUTA_PRINCIPAL_ARC_FISICO);
		DescargaArchivoFacturacion utilitarioFacturas = new DescargaArchivoFacturacion();
		try {
			utilitarioFacturas.obtenerArchivo(mapaDatos, this.docSeleccionado[13].toString(), tipoArchivo);
			String rutaFisica = RUTA_PRINCIPAL_ARC_FISICO.concat(this.docSeleccionado[13].toString()).concat(".")
					.concat(tipoArchivo);
			downloadFile(rutaFisica, tipoArchivo);
			utilitarioFacturas.eliminarArchivo(rutaFisica, this.docSeleccionado[13].toString());
		} catch (Exception e) {
			error(e.getMessage());
			return;
		}
	}

	/**
	 * Permite descargar el archivo
	 * 
	 * @param filePath
	 * @param extension
	 */
	private void downloadFile(String filePath, String extension) {
		try {

			FacesContext ctx = FacesContext.getCurrentInstance();
			File file = new File(filePath);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				byte[] fileBytes = file.getName().getBytes();
				HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();
				StringBuffer header = new StringBuffer();
				header.append("filename=\"");
				header.append(file.getName());
				header.append("\"");
				response.setHeader("Content-Disposition", header.toString());
				response.setContentType("application/" + extension);
				response.setContentLength(fileBytes.length);
				ServletOutputStream out = response.getOutputStream();
				response.setContentLength((int) length);
				while ((in != null) && ((length = in.read(buf)) != -1)) {
					out.write(buf, 0, (int) length);
				}
				in.close();
				out.close();
				ctx.responseComplete();
			}
		} catch (Exception ex) {
			error("No se pudo descargar archivo. " + ex.getMessage());
		}
	}

	/**
	 * Permite cargar el dialogo de usuarios
	 */

	public void cargarUsuariosSistema() {
		this.dialogUsuariosController.setNombreDialog("dialogUsuariosWV");
		this.dialogUsuariosController.cargarUsuarios();
		this.dialogUsuariosController.setUsuarioDto(null);
		accionesDialog("dialogUsuariosWV", Boolean.TRUE);
	}

	public void selectAllCheckboxes(ToggleSelectEvent event) {
		if (listadoDetalleCarteraSelected == null || listadoDetalleCarteraSelected.isEmpty()) {
			listadoDetalleCarteraSelected = new ArrayList<Object[]>();
			if (event.isSelected()) {
				listadoDetalleCarteraSelected.addAll(listadoDetalleCartera); // Add all the elements from getSomeList()

			}
		} else {
			listadoDetalleCarteraSelected = new ArrayList<Object[]>();
		}

	}

	public void verPantallaCliente() {

		try {
			String url = "/cxc/faces/jsp/mantenimiento/cliente/clienteForm.jsp?origen=seguimientoCredito&accion=EDIT"
					.concat("&codigoCliente=")
					.concat(String.valueOf(clienteSeleccionado.getClientePK().getNoCliente()));
			url = url.concat(zona != null ? "&zona=".concat(zona) : "")
					.concat(canal != null ? "&canal=".concat(canal) : "")
					.concat(tipoFinanciamiento != null ? "&tipoFinanciamiento=".concat(tipoFinanciamiento) : "")
					.concat(lineaNegocioSelected != null ? "&lineaNegocio=".concat(lineaNegocioSelected) : "")
					.concat(rangoAntiguedadSelect != null ? "&rangoAntiguedad=".concat(rangoAntiguedadSelect) : "")
					.concat(canal != null ? "&canal=".concat(canal) : "")
					.concat(zona != null ? "&zona=".concat(zona) : "");

			super.redirect(url);
		} catch (IOException e) {
			logger.error(e);
			error("Se presento un error al redireccionar página, comuníquese con sistemas");
		}

	}

	public void verPantallaReqCartera() {
		if (listadoDetalleCarteraSelected == null || listadoDetalleCarteraSelected.isEmpty()) {
			error("Debe seleccionar una factura...");
		} else if (listadoDetalleCarteraSelected.size() > 1) {
			error("Debe seleccionar una sola factura...");
			logger.info("redirigeTramiteCredito...");
		} else if (listadoDetalleCarteraSelected.get(0)[21] == null) {
			error("La factura no puede ser generada como inscripcion de contrato de cartera vencida");
		} else {

			try {
				cotizacionVeh = cotizacionVehiculoServicio.buscarCotizacion(getCompania().getNoCia(),
						NumericUtils.toBigDecimal(listadoDetalleCarteraSelected.get(0)[21]).intValue());
				String paginaTramiteCredito = "/vehiculosWebPrime/jsf/proceso/tramiteCredito/tramiteCreditoMain.jsf?nocia="
						+ cotizacionVeh.getSvfcotiPK().getNoCia() + "&centro="
						+ cotizacionVeh.getSvfcotiPK().getCentro() + "&numeroCotizacion="
						+ cotizacionVeh.getSvfcotiPK().getNumeroCotizacion() + "&areaUsuario=VEH" + "&idVendedorSugar="
						+ cotizacionVeh.getUsuario() + "&areaProceso=COB" ;
				super.redirect(paginaTramiteCredito);

			} catch (FindException e) {
				error("Error al Buscar Cotizacion" + e.getMessage());
			} catch (IOException e) {
				error("Error al verificar url" + e.getMessage());
			}
		}
	}

	

	public void verPantallaHistoricoDoc() {
		try {
			String url = "/cxc-web-prime/jsf/consultas/consultaHistDocumentoClientes.jsf?origen=seguimientoCredito"
					.concat("&noCliente=").concat(String.valueOf(clienteSeleccionado.getClientePK().getNoCliente()));
			url = url.concat(zonaSelected != null ? "&zona=".concat(zonaSelected) : "")
					.concat(canalSelected != null ? "&canal=".concat(canalSelected) : "")
					.concat(tipoFinanciamientoSelect != null ? "&tipoFinanciamiento=".concat(tipoFinanciamientoSelect)
							: "")
					.concat(lineaNegocio != null ? "&lineaNegocio=".concat(lineaNegocio) : "")
					.concat(rangoAntiguedadSelect != null ? "&rangoAntiguedad=".concat(rangoAntiguedadSelect) : "")
					.concat(agencia != null ? "agenciaSelected=".concat(agencia) : "");

			super.redirect(url);
		} catch (IOException e) {
			logger.error(e);
			error("Se presento un error al redireccionar página, comuníquese con sistemas");
		}

	}

	public String generarReporte(String tipo) {

		try {

			if (validarCampoReporte()) {

				error("Campo Tipo Reporte Requerido!!");
				return null;
			}

			UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
			Connection connection = null;
			try {
				Map<String, Object> parameters = new HashMap<String, Object>();

				String ctxPath = getServletContext().getRealPath("/");

				String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance()
						.getExternalContext().getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
						+ System.getProperty("file.separator");

				File[] drives = File.listRoots();
				for (File fileDrives : drives) {
					// Si es windows
					if (fileDrives.getPath().length() > 1 && fileDrives.getPath().substring(0, 1).equals("C")) {
						path = fileDrives.getPath() + path;
					}
				}
				JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
				parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
				setParameters(parameters);
				if ("excel".equals(tipo)) {
					parameters.put("IMPRIME_CABECERA", CommonConstants.FALSE_VALUE);
				} else {
					parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
				}
				connection = utilServiceDelegate.getDataSource().getConnection();

				JasperPrint jasperPrint = JasperFillManager.fillReport(
						"D".equalsIgnoreCase(tipoReporte) ? ctxPath + DETALLE : ctxPath + RESUMEN, parameters,
						connection);
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
						.getExternalContext().getResponse();

				if ("pdf".equals(tipo)) {
					response.setContentType("application/pdf");
					JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
				}

				if ("excel".equals(tipo)) {
					exportXls(jasperPrint, response);
				}

				FacesContext.getCurrentInstance().getApplication().getStateManager()
						.saveView(FacesContext.getCurrentInstance());
				FacesContext.getCurrentInstance().responseComplete();
				response.getOutputStream().flush();
				response.getOutputStream().close();
				accionesDialog("dlgReporte", Boolean.FALSE);

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

		} catch (Exception e1) {
			addErrorMessage(e1.getCause().toString(), e1.getMessage());
		}

		return null;
	}

	protected void exportXls(JasperPrint jasperPrint, HttpServletResponse response) throws JRException, IOException {
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
		header.append(EXCEL);
		header.append("\"");
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(bytes.length);
		response.getOutputStream().write(bytes, 0, bytes.length);
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	private Boolean validarCampoReporte() {
		if (tipoReporte != null) {
			return Boolean.FALSE;
		} else {

			return Boolean.TRUE;
		}

	}

	/**
	 * Este Metodo abre el detalle del seguimeinto de la cartera por Cliente
	 */
	public void abrirDialogoSeguimientoCliente(Object[] obj) {

		listadoSegClientes = cxcSeguimientosCarteraServiceLocal.obtenerPorCliente(getCompania().getNoCia(),
				new Long(obj[0].toString()));

		accionesDialog("dlgDetalle", Boolean.TRUE);

	}

	/**
	 * Este Metodo abre el detalle del seguimeinto de la cartera por no_docu
	 */
	public void abrirDialogoSeguimientoNoDocu(Object[] obj) {

		listadoSeguimientoCartera = cxcSeguimientosCarteraServiceLocal.obtenerPorNoDocu(getCompania().getNoCia(),
				obj[11].toString());

		accionesDialog("dlgDetalle", Boolean.TRUE);

	}

	public void abrirDialogReporte() {
		accionesDialog("dlgReporte", Boolean.TRUE);

	}

	public void cerrarDialogReporte() {
		accionesDialog("dlgReporte", Boolean.FALSE);
		generarReporte(tipoFinanciamiento);

	}

	private void setParameters(Map<String, Object> parameters) {

		parameters.put("NO_CIA", getCompania().getNoCia());
		parameters.put("CIA", getCompania().getNombre());
		parameters.put("NO_CLIENTE", noClienteSelected != null ? new Long(noClienteSelected)
				: clienteSeleccionado.getClientePK().getNoCliente());

	}

	private String obtenerRuta(String nombreReporte) {
		EjecutaComandoUtils ejecutar = new EjecutaComandoUtils();
		ejecutar.executeCommand("updatedb");
		StringBuilder comando = new StringBuilder("locate ").append(nombreReporte);
		List<String> listaResultado = ejecutar.executeCommand(comando.toString());
		String ctxPath = null;
		for (String directorio : listaResultado) {
			if (directorio.contains("jboss-EAP-7.4")) {
				ctxPath = directorio;
			}
		}
		return ctxPath;
	}

	public void rowSelectCheckbox(SelectEvent se) {
		if (listadoDetalleCarteraSelected == null) {
			listadoDetalleCarteraSelected = new ArrayList<Object[]>();
			listadoDetalleCarteraSelected.add((Object[]) se.getObject());
		} else {
			listadoDetalleCarteraSelected.add((Object[]) se.getObject());
		}
	}

	public void rowUnselectCheckbox(UnselectEvent use) {

		listadoDetalleCarteraSelected.remove((Object[]) use.getObject());
	}

	public List<Object[]> getListadoRangoAntiguedad() {
		return listadoRangoAntiguedad;
	}

	public void setListadoRangoAntiguedad(List<Object[]> listadoRangoAntiguedad) {
		this.listadoRangoAntiguedad = listadoRangoAntiguedad;
	}

	public String getZona() {
		return zona;
	}

	public void setZona(String zona) {
		this.zona = zona;
	}

	public List<TipoFinanciamiento> getListadoTipoFinanciamiento() {
		return listadoTipoFinanciamiento;
	}

	public void setListadoTipoFinanciamiento(List<TipoFinanciamiento> listadoTipoFinanciamiento) {
		this.listadoTipoFinanciamiento = listadoTipoFinanciamiento;
	}

	public List<ParamDet> getListadoCanales() {
		return listadoCanales;
	}

	public void setListadoCanales(List<ParamDet> listadoCanales) {
		this.listadoCanales = listadoCanales;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public String getRangoAntiguedad() {
		return rangoAntiguedad;
	}

	public void setRangoAntiguedad(String rangoAntiguedad) {
		this.rangoAntiguedad = rangoAntiguedad;
	}

	public String getTipoFinanciamiento() {
		return tipoFinanciamiento;
	}

	public void setTipoFinanciamiento(String tipoFinanciamiento) {
		this.tipoFinanciamiento = tipoFinanciamiento;
	}

	public List<LineaNegocio> getListadoLineasNegocio() {
		return listadoLineasNegocio;
	}

	public void setListadoLineasNegocio(List<LineaNegocio> listadoLineasNegocio) {
		this.listadoLineasNegocio = listadoLineasNegocio;
	}

	public String getLineaNegocioSelected() {
		return lineaNegocioSelected;
	}

	public void setLineaNegocioSelected(String lineaNegocioSelected) {
		this.lineaNegocioSelected = lineaNegocioSelected;
	}

	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}

	public void setDialogClientesController(DialogClientesController dialogClientesController) {
		this.dialogClientesController = dialogClientesController;
	}

	public Cliente getCliente() {
		if (dialogClientesController.getCliente() != null
				&& dialogClientesController.getCliente().getClientePK() != null
				&& dialogClientesController.getCliente().getClientePK().getNoCliente() != null) {

			cliente = dialogClientesController.getCliente();
			dialogClientesController.setCliente(null);
		}

		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<Object[]> getListadoCartera() {
		return listadoCartera;
	}

	public void setListadoCartera(List<Object[]> listadoCartera) {
		this.listadoCartera = listadoCartera;
	}

	public List<Object[]> getListadoDetalleCartera() {
		return listadoDetalleCartera;
	}

	public void setListadoDetalleCartera(List<Object[]> listadoDetalleCartera) {
		this.listadoDetalleCartera = listadoDetalleCartera;
	}

	public Object[] getDeudaSeleccionada() {
		return deudaSeleccionada;
	}

	public void setDeudaSeleccionada(Object[] deudaSeleccionada) {
		this.deudaSeleccionada = deudaSeleccionada;
	}

	public String getRangoAntiguedadSelect() {
		return rangoAntiguedadSelect;
	}

	public void setRangoAntiguedadSelect(String rangoAntiguedadSelect) {
		this.rangoAntiguedadSelect = rangoAntiguedadSelect;
	}

	public String getTipoFinanciamientoSelect() {
		return tipoFinanciamientoSelect;
	}

	public void setTipoFinanciamientoSelect(String tipoFinanciamientoSelect) {
		this.tipoFinanciamientoSelect = tipoFinanciamientoSelect;
	}

	public String getNoClienteSelected() {
		return noClienteSelected;
	}

	public void setNoClienteSelected(String noClienteSelected) {
		this.noClienteSelected = noClienteSelected;
	}

	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}

	public void setClienteSeleccionado(Cliente clienteSeleccionado) {
		this.clienteSeleccionado = clienteSeleccionado;
	}

	public String getLineaNegocio() {
		return lineaNegocio;
	}

	public void setLineaNegocio(String lineaNegocio) {
		this.lineaNegocio = lineaNegocio;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public List<CxcClaseCliente> getListaClaseClientes() {
		return listaClaseClientes;
	}

	public void setListaClaseClientes(List<CxcClaseCliente> listaClaseClientes) {
		this.listaClaseClientes = listaClaseClientes;
	}

	public List<AgenciaDTO> getListadoAgencias() {
		return listadoAgencias;
	}

	public void setListadoAgencias(List<AgenciaDTO> listadoAgencias) {
		this.listadoAgencias = listadoAgencias;
	}

	public DialogUsuariosController getDialogUsuariosController() {
		return dialogUsuariosController;
	}

	public void setDialogUsuariosController(DialogUsuariosController dialogUsuariosController) {
		this.dialogUsuariosController = dialogUsuariosController;
	}

	public UsuarioDto getUsuarioDto() {
		if (dialogUsuariosController != null && dialogUsuariosController.getUsuarioDto() != null
				&& dialogUsuariosController.getUsuarioDto().getUsuario() != null) {
			usuarioDto = dialogUsuariosController.getUsuarioDto();
			dialogUsuariosController.setUsuarioDto(null);
		}
		return usuarioDto;
	}

	public void setUsuarioDto(UsuarioDto usuarioDto) {
		this.usuarioDto = usuarioDto;
	}

	public CxcSeguimientosCartera getCxcSeguimientosCartera() {
		return cxcSeguimientosCartera;
	}

	public void setCxcSeguimientosCartera(CxcSeguimientosCartera cxcSeguimientosCartera) {
		this.cxcSeguimientosCartera = cxcSeguimientosCartera;
	}

	public List<Object[]> getListadoDetalleCarteraSelected() {
		return listadoDetalleCarteraSelected;
	}

	public void setListadoDetalleCarteraSelected(List<Object[]> listadoDetalleCarteraSelected) {
		this.listadoDetalleCarteraSelected = listadoDetalleCarteraSelected;
	}

	public DialogZonasController getDialogZonasController() {
		return dialogZonasController;
	}

	public void setDialogZonasController(DialogZonasController dialogZonasController) {
		this.dialogZonasController = dialogZonasController;
	}

	public ZonasDto getZonasDto() {
		if (dialogZonasController != null && dialogZonasController.getZonasDto() != null
				&& dialogZonasController.getZonasDto().getId() != null) {
			zonasDto = dialogZonasController.getZonasDto();
			zona = zonasDto.getZona();
			dialogZonasController.setZonasDto(null);
		}

		return zonasDto;
	}

	public void setZonasDto(ZonasDto zonasDto) {
		this.zonasDto = zonasDto;
	}

	public String getCanalSelected() {
		return canalSelected;
	}

	public void setCanalSelected(String canalSelected) {
		this.canalSelected = canalSelected;
	}

	public String getZonaSelected() {
		return zonaSelected;
	}

	public void setZonaSelected(String zonaSelected) {
		this.zonaSelected = zonaSelected;
	}

	public String getTipoReporte() {
		return tipoReporte;
	}

	public void setTipoReporte(String tipoReporte) {
		this.tipoReporte = tipoReporte;
	}

	public List<CxcTipoSeguimiento> getListadoTipoSeguimiento() {
		return listadoTipoSeguimiento;
	}

	public void setListadoTipoSeguimiento(List<CxcTipoSeguimiento> listadoTipoSeguimiento) {
		this.listadoTipoSeguimiento = listadoTipoSeguimiento;
	}

	public String getUrlReporte() {
		return urlReporte;
	}

	public void setUrlReporte(String urlReporte) {
		this.urlReporte = urlReporte;
	}

	public String getLineaDeuda() {
		return lineaDeuda;
	}

	public void setLineaDeuda(String lineaDeuda) {
		this.lineaDeuda = lineaDeuda;
	}

	public List<CxcSeguimientosCartera> getListadoSeguimientoCartera() {
		return listadoSeguimientoCartera;
	}

	public void setListadoSeguimientoCartera(List<CxcSeguimientosCartera> listadoSeguimientoCartera) {
		this.listadoSeguimientoCartera = listadoSeguimientoCartera;
	}

	public SeguimientoCobranzasDatamanager getSeguimientoCobranzasDataManager() {
		return seguimientoCobranzasDataManager;
	}

	public void setSeguimientoCobranzasDataManager(SeguimientoCobranzasDatamanager seguimientoCobranzasDataManager) {
		this.seguimientoCobranzasDataManager = seguimientoCobranzasDataManager;
	}

	public Object[] getDocSeleccionado() {
		return docSeleccionado;
	}

	public void setDocSeleccionado(Object[] docSeleccionado) {
		this.docSeleccionado = docSeleccionado;
	}

	public String getTipoArchivo() {
		return tipoArchivo;
	}

	public void setTipoArchivo(String tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

	public List<Object[]> getListadoSegClientes() {
		return listadoSegClientes;
	}

	public void setListadoSegClientes(List<Object[]> listadoSegClientes) {
		this.listadoSegClientes = listadoSegClientes;
	}

	public List<CxcResultadoGestion> getListadoResultadoGestion() {
		return listadoResultadoGestion;
	}

	public void setListadoResultadoGestion(List<CxcResultadoGestion> listadoResultadoGestion) {
		this.listadoResultadoGestion = listadoResultadoGestion;
	}

	public List<CxcCanalGestion> getListadoCanalGestion() {
		return listadoCanalGestion;
	}

	public void setListadoCanalGestion(List<CxcCanalGestion> listadoCanalGestion) {
		this.listadoCanalGestion = listadoCanalGestion;
	}

	public List<CxcCanalContactado> getListadoContactado() {
		return listadoContactado;
	}

	public void setListadoContactado(List<CxcCanalContactado> listadoContactado) {
		this.listadoContactado = listadoContactado;
	}

	public Boolean getIncluyeAlistamiento() {
		return incluyeAlistamiento;
	}

	public void setIncluyeAlistamiento(Boolean incluyeAlistamiento) {
		this.incluyeAlistamiento = incluyeAlistamiento;
	}

}
