/**
 * 
 */
package com.casabaca.prime.cxc.reportes.controller;

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
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.model.CxcClaseCliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.CxcClaseClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.util.TipoRepCuadre;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class CxcReportesCuadreController extends ReportCommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8699374167437171253L;
	private static final Logger LOG = Logger.getLogger(CxcReportesCuadreController.class);

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	@EJB(lookup = NombreJNDI.CXC_CLASE_CLIENTE_SERVICE)
	private CxcClaseClienteServiceLocal claseClienteService;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeService;

	private TipoRepCuadre[] tiposNegNuevos;
	private TipoRepCuadre[] tiposCancelacionesNC;
	private TipoRepCuadre[] tiposDetalleCobros;
	private TipoRepCuadre tipoRepCuadre;
	private TipoRepCuadre tipoRepCuadreSeleccionado;
	private List<LineaNegocio> lineasNegocio;
	private String noCia;
	private String noLineaNeg;
	private List<CxcClaseCliente> clasesClientes;
	private String claseCliente;
	private List<Object[]> centros;
	private String tipoPago;
	private String ventaBanco;
	private String filtroRelacion;
	private boolean resumenFact;
	private List<String> lineasResumen;

	public CxcReportesCuadreController() {
		this.noCia = getCompania().getNoCia();
		this.tipoRepCuadre = TipoRepCuadre.FAC_NUEVAS;
	}

	@PostConstruct
	public void init() {
		limpiarDatos();
		lineasNegocio = lineaNegocioService.lineasNegocio(this.noCia);
		clasesClientes = claseClienteService.findClaseDeClientesByNoCia(noCia);
		centros = nativeService.consultarCentros(this.noCia);
	}

	/**
	 * Permite inicializar la variables
	 */
	public void limpiarDatos() {
		resumenFact = Boolean.FALSE;
		lineasResumen = new ArrayList<>();
		noLineaNeg = null;
		claseCliente = null;
		setFechaDesde(new Date());
		setFechaHasta(new Date());
		tipoRepCuadreSeleccionado = tipoRepCuadre;
	}

	public void generarReporte() {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("p_no_cia", this.noCia);
		parametros.put("p_nombre_empresa", getCompania().getNombre());
		parametros.put("p_usuario", getLoggedUsername());
		parametros.put("p_linea_negocio", noLineaNeg);
		parametros.put("p_clase_cliente", claseCliente);
		parametros.put("p_fecha_inicio", getFechaDesde());
		parametros.put("p_fecha_fin", getFechaHasta());
		parametros.put("p_modulo", getActiveModuleId());
		parametros.put("p_tipo_pago", tipoPago);
		parametros.put("p_banco", ventaBanco);
		parametros.put("p_tipo", tipoRepCuadreSeleccionado.equals(TipoRepCuadre.SIN_EMP_RELACIONADAS) ? "SER" : "PCL");
		if (tipoRepCuadreSeleccionado.equals(TipoRepCuadre.SIN_EMP_RELACIONADAS)) {
			parametros.put("p_clase_cliente", "SER");
		}
		parametros.put("p_lineas_negocio", lineasResumen);
		parametros.put("p_sql_emp_relacionadas",
				"1".equals(filtroRelacion) ? " and nvl(c.relacion,'EX')<>'ER' " : CommonConstants.VACIO);
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("format");
			parametros.put("p_imprime_cabecera", "pdf".equals(format));
			parametros.put(JRParameter.IS_IGNORE_PAGINATION, "excel".equals(format));
			connection = utilServiceDelegate.getDataSource().getConnection();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			String reporte = resumenFact ? "/reportes/cxcFacturasNuevasResumenPorLinea.jasper"
					: tipoRepCuadreSeleccionado.getReporte();
			String ctxPath = getServletContext().getRealPath("/") + reporte;
			// VIRTUALIZAR
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parametros.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath, parametros, connection);
			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition",
						"attachment;filename=\"" + tipoRepCuadreSeleccionado.getNombreArchivoPdf() + "\"");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			} else if ("excel".equals(format)) {
				exportXls(jasperPrint, response);
			}
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
			virtualizer.cleanup();
		} catch (IOException | JRException e) {
			LOG.error(e.getMessage() + ".EE. ", e);
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			cerrarConexion(connection);
		}
	}

	/**
	 * Permite cerrar la conexion
	 * 
	 * @param con
	 */
	private void cerrarConexion(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public TipoRepCuadre[] getTiposNegNuevos() {
		if (tiposNegNuevos == null) {
			tiposNegNuevos = TipoRepCuadre.getNegociosNuevos();
		}
		return tiposNegNuevos;
	}

	public void setTiposNegNuevos(TipoRepCuadre[] tiposNegNuevos) {
		this.tiposNegNuevos = tiposNegNuevos;
	}

	public TipoRepCuadre[] getTiposCancelacionesNC() {
		if (tiposCancelacionesNC == null) {
			tiposCancelacionesNC = TipoRepCuadre.getCancelacionPorNC();
		}
		return tiposCancelacionesNC;
	}

	public void setTiposCancelacionesNC(TipoRepCuadre[] tiposCancelacionesNC) {
		this.tiposCancelacionesNC = tiposCancelacionesNC;
	}

	public TipoRepCuadre[] getTiposDetalleCobros() {
		if (tiposDetalleCobros == null) {
			tiposDetalleCobros = TipoRepCuadre.getDetalleCobros();
		}
		return tiposDetalleCobros;
	}

	public void setTiposDetalleCobros(TipoRepCuadre[] tiposDetalleCobros) {
		this.tiposDetalleCobros = tiposDetalleCobros;
	}

	public List<LineaNegocio> getLineasNegocio() {
		return lineasNegocio;
	}

	public void setLineasNegocio(List<LineaNegocio> lineasNegocio) {
		this.lineasNegocio = lineasNegocio;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getNoLineaNeg() {
		return noLineaNeg;
	}

	public void setNoLineaNeg(String noLineaNeg) {
		this.noLineaNeg = noLineaNeg;
	}

	public List<CxcClaseCliente> getClasesClientes() {
		return clasesClientes;
	}

	public void setClasesClientes(List<CxcClaseCliente> clasesClientes) {
		this.clasesClientes = clasesClientes;
	}

	public String getClaseCliente() {
		return claseCliente;
	}

	public void setClaseCliente(String claseCliente) {
		this.claseCliente = claseCliente;
	}

	public TipoRepCuadre getTipoRepCuadreSeleccionado() {
		return tipoRepCuadreSeleccionado;
	}

	public void setTipoRepCuadreSeleccionado(TipoRepCuadre tipoRepCuadreSeleccionado) {
		this.tipoRepCuadreSeleccionado = tipoRepCuadreSeleccionado;
	}

	public TipoRepCuadre getTipoRepCuadre() {
		return tipoRepCuadre;
	}

	public void setTipoRepCuadre(TipoRepCuadre tipoRepCuadre) {
		this.tipoRepCuadre = tipoRepCuadre;
	}

	public List<Object[]> getCentros() {
		return centros;
	}

	public void setCentros(List<Object[]> centros) {
		this.centros = centros;
	}

	public String getTipoPago() {
		return tipoPago;
	}

	public void setTipoPago(String tipoPago) {
		this.tipoPago = tipoPago;
	}

	public String getVentaBanco() {
		return ventaBanco;
	}

	public void setVentaBanco(String ventaBanco) {
		this.ventaBanco = ventaBanco;
	}

	public String getFiltroRelacion() {
		return filtroRelacion;
	}

	public void setFiltroRelacion(String filtroRelacion) {
		this.filtroRelacion = filtroRelacion;
	}

	public boolean isResumenFact() {
		return resumenFact;
	}

	public void setResumenFact(boolean resumenFact) {
		this.resumenFact = resumenFact;
	}

	public List<String> getLineasResumen() {
		return lineasResumen;
	}

	public void setLineasResumen(List<String> lineasResumen) {
		this.lineasResumen = lineasResumen;
	}

}
