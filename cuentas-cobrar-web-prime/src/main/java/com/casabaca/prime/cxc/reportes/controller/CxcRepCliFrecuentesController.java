package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * 
 *
 */
@ManagedBean(name = "cxcRepCliFrecuentesController")
@ViewScoped
public class CxcRepCliFrecuentesController extends ReportCommonController implements Serializable {
	static Logger logger = Logger.getLogger(CxcRepCliFrecuentesController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -7900641687719651629L;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaServiceLocal;

	private List<Agencia> listadoAgencias;
	private String agencia;
	private String codigoAgenciaSelect;
	private Date fechaInicio;
	private Date fechaFin;

	@PostConstruct
	public void init() {
		listadoAgencias = new ArrayList<Agencia>();
		listadoAgencias = agenciaServiceLocal.getAgenciasServicios(getCompania().getNoCia());
		Calendar c = Calendar.getInstance();
		c.add(Calendar.YEAR, -1);
		fechaFin=new Date();
		fechaInicio=c.getTime();
	}

	public String report() {
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		Map<String, Object> parameters = new HashMap<String, Object>();
		try {
			String ctxPath = getServletContext().getRealPath("/");
			setParameters(parameters);
			connection = utilServiceDelegate.getDataSource().getConnection();
			if ("excel".equals(format)) {
				parameters.put("IMPRIME_CABECERA", CommonConstants.FALSE_VALUE);
			}else {
				parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
			}
			
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + getStrReportPath(), parameters,
					connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			}
			if ("excel".equals(format)) {
				exportXls(jasperPrint, response);
			}
			if ("html".equals(format)) {
				exportHTML(jasperPrint, response);
			}
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
			
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
		return null;
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("NOCIA", getCompania().getNoCia());
		//parameters.put("CODIGOAGENCIA", this.codigoAgenciaSelect);
		parameters.put("FECHADESDE", this.fechaInicio);
		parameters.put("FECHAHASTA", this.fechaFin);
		parameters.put("NOMBREEMPRESA", getCompania().getNombre());
		parameters.put("USUARIO", getUsuario().getUsuario());
		//parameters.put("NOMBREAGENCIA", getCompania().getNombreIdTributario());// this.getAgencia());
	}

	public List<Agencia> getListadoAgencias() {
		return listadoAgencias;
	}

	public void setListadoAgencias(List<Agencia> listadoAgencias) {
		this.listadoAgencias = listadoAgencias;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
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

	public String getCodigoAgenciaSelect() {
		return codigoAgenciaSelect;
	}

	public void setCodigoAgenciaSelect(String codigoAgenciaSelect) {
		this.codigoAgenciaSelect = codigoAgenciaSelect;
	}

}
