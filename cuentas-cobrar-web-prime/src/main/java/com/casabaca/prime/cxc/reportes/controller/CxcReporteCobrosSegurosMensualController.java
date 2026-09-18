/**
 * 
 */
package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * @author fb_fabara
 *
 */
@ManagedBean
@ViewScoped
public class CxcReporteCobrosSegurosMensualController extends ReportCommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3779333905227124970L;

	private static final Logger LOG = Logger.getLogger(CxcReporteCobrosSegurosMensualController.class);

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeService;

	private String noCia;
	String reporte = "/reportes/cxcRepCobrosSegurosMensual.jasper";

	public CxcReporteCobrosSegurosMensualController() {
		this.noCia = getCompania().getNoCia();
	}

	@PostConstruct
	public void init() {
		limpiarDatos();
	}

	/**
	 * Permite inicializar la variables
	 */
	public void limpiarDatos() {
		setFechaDesde(new Date());
		setFechaHasta(new Date());

	}

	public void generarReporte() {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("p_no_cia", this.noCia);
		parametros.put("p_nombre_empresa", getCompania().getNombre());
		parametros.put("p_usuario", getLoggedUsername());
		parametros.put("p_fecha_inicio", getFechaDesde());
		parametros.put("p_fecha_fin", getFechaHasta());
		parametros.put("p_modulo", getActiveModuleId());
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("format");

			if ("pdf".equals(format)) {
				parametros.put("p_imprime_cabecera", CommonConstants.TRUE_VALUE);
			} else {
				parametros.put("p_imprime_cabecera", CommonConstants.FALSE_VALUE);
			}

			parametros.put(JRParameter.IS_IGNORE_PAGINATION, "excel".equals(format));
			connection = utilServiceDelegate.getDataSource().getConnection();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

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
						"attachment;filename=\"" + "cxcRepCobrosSegurosMensual.pdf" + "\"");
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

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getReporte() {
		return reporte;
	}

	public void setReporte(String reporte) {
		this.reporte = reporte;
	}

}
