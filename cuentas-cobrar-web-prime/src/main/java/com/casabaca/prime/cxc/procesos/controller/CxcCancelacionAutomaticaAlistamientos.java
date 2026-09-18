package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.common.ejb.model.Arccda;
import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CommonNativeServiceLocal;
import com.casabaca.common.ejb.util.ConstantesVehiculos;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcDetVentaVehiculoUsadoDto;
import com.casabaca.cxc.ejb.modelo.FacVentasCabCp;
import com.casabaca.cxc.ejb.servicio.CancelacionAlistamientosMandatoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.servicio.FacVentasCabCpServicioLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * Controlador para realizar la cancelacion automatica de alistamientos para
 * usados
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcCancelacionAutomaticaAlistamientos")
public class CxcCancelacionAutomaticaAlistamientos extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;
	private static final Logger LOGGER = Logger.getLogger(CxcCancelacionAutomaticaAlistamientos.class);
	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_CANCELAMIENTO_MANDATO_SERVICE_BEAN)
	private CancelacionAlistamientosMandatoServiceLocal cancelacionService;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;
	
	@EJB(lookup = NombreJNDI.FAC_VENTAS_CAB_CP_SERVICIO)
	private FacVentasCabCpServicioLocal facturaService;

	@EJB(lookup = NombreJNDI.COMMON_NATIVE_SERVICE_LOCAL)
	protected CommonNativeServiceLocal commonNativeService;
	
	private Date fechaInicial;
	private Date fechaFinal;
	private String codigoU;

	private List<Arccmd> facturaComision;
	private List<Arccmd> facturasAlistamientos;
	private String comentario;
	private Cliente noCliente;

	List<CxcDetVentaVehiculoUsadoDto> listDetallesUsados;

	CxcDetVentaVehiculoUsadoDto detalleSeleccionado;

	private BigDecimal valorComision;
	private BigDecimal valorAlistamientos;
	private BigDecimal valorTotal;
	private BigDecimal iva;

	private BigDecimal valorComparar;
	private boolean esPerdida;
	private boolean diferenciaMayor;

	private boolean esCobranzas;
	private boolean esContabilidad;
	private List<Arccmd> facturasCanceladas;
	public static final String PDF_PATH_NC = "gnvoiceCasabaca/descargarDocumento/obtenerFacturaPDF/";

	@PostConstruct
	public void init() {
		LOGGER.info("init...");
		// CXC Cobranzas, CON Contabilidad
		esCobranzas = "CXC".equals(getRequestParameter("modulo"));
		esContabilidad = !esCobranzas;
		listDetallesUsados = new ArrayList<>();
		detalleSeleccionado = new CxcDetVentaVehiculoUsadoDto();
		facturasCanceladas= new ArrayList<>();
		this.iva=new BigDecimal(commonNativeService.getPorcentajeIva(getCompania().getNoCia()));
	}

	public void realizarCancelacion() {
		LOGGER.info("realizarCancelacion...");
		try {
			Arccmd comision = null;
			if (facturaComision != null && !facturaComision.isEmpty()) {
				comision = facturaComision.get(0);
			}
			List<Arccmd> facturasAlistmaientoSaldo = null;
			if (facturasAlistamientos != null) {
				facturasAlistmaientoSaldo = facturasAlistamientos.stream()
						.filter(a -> BigDecimal.ZERO.compareTo(a.getSaldo()) < 0).collect(Collectors.toList());
			}
			Arccda diarioR = cancelacionService.cancelarComisionAlistamientos(getCompania().getNoCia(), comision,
					facturasAlistmaientoSaldo, valorTotal, noCliente, comentario, getUsuario().getUsuario(),
					getUsuarioCentroConectado().getAgencia().getAgenciaPK().getCodigo(),
					detalleSeleccionado.getCentrod());

			if (diarioR != null) {
				accionesDialog("DlgPrevDiario", false);
				info("Se ha generado el Diario A Nro: " + diarioR.getNoFisico());
			} else {
				error("No se genero el diaro A");

			}

		} catch (Exception e) {
			error("ERROR: " + e);
			e.printStackTrace();
		}
	}
	
	public void imprimirRide(Arccmd fact) {
		FacVentasCabCp cab = null;
		try {
			System.out.println("NULL "+facturaService+" "+fact.getCentro()+" CLiente "+fact.getNoCliente());
			cab = facturaService.getFacturaNoCiaCentroNoFisicoNoCliente(getCompania().getNoCia(), fact.getCentro(),
					fact.getNoFisico(), Integer.parseInt(fact.getNoCliente()));
		} catch (FindException e) {
			super.warn("Factura no existe");
			
		}

		String clave = cab != null ? cab.getClaveAcceso() : null;

		if (clave != null) {
			String ip = getCompania().getIpWebServer();
			if (ip.length() > 0) {
				ip = ip.replace("8080", "18080");
			}
			String pathNC = ip + PDF_PATH_NC + clave;
			System.out.println("PathNC "+pathNC);
			RequestContext requestContext = RequestContext.getCurrentInstance();
			requestContext.execute("openDuplicatedTab('" + pathNC + "')");
			
		} else {
			super.warn("Documento no existe");
		}
		
	}

	public void verDiarioAGenerado(CxcDetVentaVehiculoUsadoDto factura) {
		try {
			// Obtengo las facturas canceladas por el diario A
			facturasCanceladas = arccmdService
					.consultarFacturasCanceladasXNoDocuDiario(getCompania().getNoCia(), factura.getNoDocuDiario());
			accionesDialog("DlgDiarioA", true);
			detalleSeleccionado = factura;
		} catch (Exception e) {
			error("Error al obtener las facturas canceladas " + e);
		}
	}
	
	public void verPreviaDiarioA(CxcDetVentaVehiculoUsadoDto factura) {
		LOGGER.info("verPreviaDiarioA...");
		try {
			detalleSeleccionado = factura;

			esPerdida = detalleSeleccionado.getUtilidadBruta().subtract(detalleSeleccionado.getValorAlistamientos())
					.compareTo(BigDecimal.ZERO) < 0;
			comentario = new String();
			if (esPerdida) {
				valorComparar = detalleSeleccionado.getValorAlistamientos();
				comentario = "CANC.FACT ALISTAMIENTO X PERDIDA (" + detalleSeleccionado.getAgencia() + ") "
						+ detalleSeleccionado.getCeduldaDueno();
			} else {
				valorComparar = detalleSeleccionado.getUtilidadBruta();
				comentario = detalleSeleccionado.getChasisComision() + " Y FACT ALISTAMIENTO Y SE.";
			}

			String[] arreglo = detalleSeleccionado.getClienteUsado().split(" ");
			String cedula = arreglo[0];
			noCliente = clienteService.buscarPorCedulaNoCiaGeneral(cedula, getCompania().getNoCia());

			if (noCliente == null || noCliente.getClientePK() == null) {
				warn("No existe cliente Usado " + cedula);
				return;
			}
			facturaComision = new ArrayList<>();
			valorComision = BigDecimal.ZERO;
			if (detalleSeleccionado.getFacturaComision() != null) {
				Arccmd comision = arccmdService.consultaArccmdXcliente(getCompania().getNoCia(),
						String.valueOf(noCliente.getClientePK().getNoCliente()),
						detalleSeleccionado.getFacturaComision(), ConstantesVehiculos.COMISION_MANDATO);
				if(comision != null) {
					facturaComision = new ArrayList<>();
					facturaComision.add(comision);
					valorComision = comision.getSaldo();
				}
			}

			facturasAlistamientos = arccmdService.consultarFacturasAlistamientosCodigoU(getCompania().getNoCia(),
					detalleSeleccionado.getSvincodi(), detalleSeleccionado.getCeduldaDueno());
			valorAlistamientos = BigDecimal.ZERO;
			if (facturasAlistamientos != null && !facturasAlistamientos.isEmpty()) {
				valorAlistamientos = new BigDecimal(
						facturasAlistamientos.stream().mapToDouble(a -> a.getSaldo().doubleValue()).sum());
				valorAlistamientos = valorAlistamientos.divide(BigDecimal.ONE, 2, BigDecimal.ROUND_HALF_UP);
			}

			valorTotal = valorAlistamientos.add(valorComision);

			diferenciaMayor = valorTotal.subtract(valorComparar).abs().compareTo(new BigDecimal(0.02)) > 0;
			accionesDialog("DlgPrevDiario", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void buscar() {
		if (fechaInicial == null || fechaFinal == null) {
			warn("Por favor seleccione un rango de fechas");
			return;
		}
		if (esCobranzas) {
			listDetallesUsados = nativeServiceLocal.obtenerDetalleVentaUsados(getCompania().getNoCia(), fechaInicial,
					fechaFinal, codigoU);
		} else {
			listDetallesUsados = nativeServiceLocal.obtenerDetalleVenUsadosCancelacion(getCompania().getNoCia(),
					fechaInicial, fechaFinal, codigoU);
		}
	}

	public boolean getValidarDiferencia() {
		if (valorTotal != null && valorComparar != null) {
			return valorTotal.compareTo(valorComparar) != 0;
		}
		return false;
	}

	public BigDecimal obtenerBase(CxcDetVentaVehiculoUsadoDto item) {
		// (item.utilidadBruta - item.valorAlistamientos)/(1+
		// (item.claveIva/100)
		BigDecimal valorBase = item.getUtilidadBruta().subtract(item.getValorAlistamientos())
				.divide(BigDecimal.ONE.add(this.iva.divide(new BigDecimal(100))), 2, BigDecimal.ROUND_UP);
		return valorBase.divide(BigDecimal.ONE, 2, BigDecimal.ROUND_HALF_UP);
	}

	public BigDecimal obtenerIva(CxcDetVentaVehiculoUsadoDto item) {
		// ((item.utilidadBruta - item.valorAlistamientos)/(1+
		// (item.claveIva/100)))* (item.claveIva/100)
		BigDecimal iva = obtenerBase(item).multiply(this.iva.divide(new BigDecimal(100)));
		return iva.divide(BigDecimal.ONE, 2, BigDecimal.ROUND_HALF_UP);

	}



	public void generarReporteJasper() {
		String reportPath = "/reportes/cxcComprobanteDiariosA.jasper";
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		JasperPrint jasperPrint = null;

		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParametersDiariosA(parameters, getCompania().getNoCia(), detalleSeleccionado.getNoDocuDiario(),
					detalleSeleccionado.getUsuarioDiario());
			connection = utilServiceDelegate.getDataSource().getConnection();
			String ctxPath = getServletContext().getRealPath("/");
			
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);

			StringBuffer header = new StringBuffer();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			header.append("inline; filename=\"");
			header.append("DIARIO_A" + detalleSeleccionado.getNumDiario() + "-" + detalleSeleccionado.getNoDocuDiario() + ".pdf");
			header.append("\"");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			virtualizer.cleanup();
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void setParametersDiariosA(Map<String, Object> parameters, String noCia, String noDocu, String usuario) {
		parameters.put("P_NO_CIA", noCia);
		parameters.put("P_NO_DOCU", noDocu);
		parameters.put("USUARIO", usuario);
		parameters.put("EMPRESA", getCompania().getNombre());
		parameters.put("SUBREPORT_DIR", getPathReal());
	}

	public boolean isEsPerdida(CxcDetVentaVehiculoUsadoDto item) {
		return item.getUtilidadBruta().subtract(item.getValorAlistamientos()).compareTo(BigDecimal.ZERO) < 0;
	}

	public void seleccionarDetalle(CxcDetVentaVehiculoUsadoDto detSeleccionado) {
		detalleSeleccionado = detSeleccionado;
	}

	public List<CxcDetVentaVehiculoUsadoDto> getListDetallesUsados() {
		return listDetallesUsados;
	}

	public void setListDetallesUsados(List<CxcDetVentaVehiculoUsadoDto> listDetallesUsados) {
		this.listDetallesUsados = listDetallesUsados;
	}

	public CxcDetVentaVehiculoUsadoDto getDetalleSeleccionado() {
		return detalleSeleccionado;
	}

	public void setDetalleSeleccionado(CxcDetVentaVehiculoUsadoDto detalleSeleccionado) {
		this.detalleSeleccionado = detalleSeleccionado;
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	public List<Arccmd> getFacturaComision() {
		return facturaComision;
	}

	public void setFacturaComision(List<Arccmd> facturaComision) {
		this.facturaComision = facturaComision;
	}

	public List<Arccmd> getFacturasAlistamientos() {
		return facturasAlistamientos;
	}

	public void setFacturasAlistamientos(List<Arccmd> facturasAlistamientos) {
		this.facturasAlistamientos = facturasAlistamientos;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public BigDecimal getValorComision() {
		return valorComision;
	}

	public void setValorComision(BigDecimal valorComision) {
		this.valorComision = valorComision;
	}

	public BigDecimal getValorAlistamientos() {
		return valorAlistamientos;
	}

	public void setValorAlistamientos(BigDecimal valorAlistamientos) {
		this.valorAlistamientos = valorAlistamientos;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public BigDecimal getValorComparar() {
		return valorComparar;
	}

	public void setValorComparar(BigDecimal valorComparar) {
		this.valorComparar = valorComparar;
	}

	public boolean isEsPerdida() {
		return esPerdida;
	}

	public void setEsPerdida(boolean esPerdida) {
		this.esPerdida = esPerdida;
	}

	public boolean isDiferenciaMayor() {
		return diferenciaMayor;
	}

	public void setDiferenciaMayor(boolean diferenciaMayor) {
		this.diferenciaMayor = diferenciaMayor;
	}

	public String getCodigoU() {
		return codigoU;
	}

	public void setCodigoU(String codigoU) {
		this.codigoU = codigoU;
	}

	public boolean isEsCobranzas() {
		return esCobranzas;
	}

	public void setEsCobranzas(boolean esCobranzas) {
		this.esCobranzas = esCobranzas;
	}

	public boolean isEsContabilidad() {
		return esContabilidad;
	}

	public void setEsContabilidad(boolean esContabilidad) {
		this.esContabilidad = esContabilidad;
	}

	public List<Arccmd> getFacturasCanceladas() {
		return facturasCanceladas;
	}

	public void setFacturasCanceladas(List<Arccmd> facturasCanceladas) {
		this.facturasCanceladas = facturasCanceladas;
	}

}