package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.StringUtils;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ManagedBean
@ViewScoped
public class CxcCarteraConsolidadoController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcCarteraConsolidadoController.class);

	private Integer anio;

	@PostConstruct
	public void init() {
		anio = FechaUtils.getAno(new Date());
	}

	public void generarReporte() {
		if (anio == null || getMes() == null) {
			error("El año y mes son campos obligatorios.");
			return;
		}
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			connection = utilServiceDelegate.getDataSource().getConnection();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			Map<String, Object> parametros = new HashMap<String, Object>();
			setParameters(parametros);
			String ctxPath = getServletContext().getRealPath("/") + getStrReportPath();
			String path2 = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			// VIRTUALIZAR
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path2);// 50paginas
			parametros.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath, parametros, connection);
			exportXls(jasperPrint, response);
			virtualizer.cleanup();
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException | JRException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			cerrarConexion(connection);
		}
	}

	/**
	 * Permite cerrar la conexion a la base de datos
	 * 
	 * @param con
	 */
	private void cerrarConexion(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setParameters(Map<String, Object> parameters) {

		parameters.put("P_NOCIA", getCompania().getNoCia());
		parameters.put("NOMBRE_CIA", getCompania().getNombre());
		parameters.put("P_ANIO", anio);
		parameters.put("P_MES", super.getMes());

	}

	/**
	 * Carga los meses
	 * 
	 * @return
	 */
	public List<SelectItem> getMeses() {
		List<SelectItem> items = new ArrayList<SelectItem>();
		HashMap<Long, String> meses = FechaUtils.getListaMesesMinus();
		meses.forEach((k, v) -> items
				.add(new SelectItem(StringUtils.rellenarConCaracteresALaIzquierda(String.valueOf(k), "0", 2), v)));
		return items;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

}