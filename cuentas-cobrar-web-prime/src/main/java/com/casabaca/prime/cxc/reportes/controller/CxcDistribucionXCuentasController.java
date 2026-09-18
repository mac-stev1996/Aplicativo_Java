package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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

import com.casabaca.common.ejb.model.ConPlantas;
import com.casabaca.common.ejb.service.ConPlantasServiceLocal;
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

@ViewScoped
@ManagedBean(name = "cxcDistribucionXCuentasController")
public class CxcDistribucionXCuentasController extends ReportCommonController {
	static Logger logger = Logger.getLogger(CxcDistribucionXCuentasController.class);

	@EJB(lookup = NombreJNDI.CON_PLANTAS_SERVICE)
	private ConPlantasServiceLocal planctasService;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	private List<ConPlantas> planCtas;
	private List<Agencia> agencias;

	private Date fechaDesde;
	private Date fechaHasta;

	private String ctaDesde;
	private String ctaHasta;

	private String agencia;

	public CxcDistribucionXCuentasController() {

	}

	@PostConstruct
	public void init() {
		planCtas = planctasService.obtenerListadoPorNoCiaTipCta(getCompania().getNoCia(), "1");
		String[] order = { "nombre" };
		agencias = agenciaService.findByCompania(getCompania().getNoCia(), order);
	}

	public String report() {
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		Map<String, Object> parameters = new HashMap<String, Object>();
		try {

			String ctxPath = getServletContext().getRealPath("/");
			// VIRTUALIZAR
						String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
								.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
								+ System.getProperty("file.separator");
						JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
						parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
						
			setParameters(parameters);
			connection = utilServiceDelegate.getDataSource().getConnection();

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
			virtualizer.cleanup();
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e.getCause());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e.getCause());
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e.getCause());
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void setParameters(Map parameters) {

		parameters.put("P_NO_CIA", getCompania().getNoCia());
		parameters.put("P_NOM_CIA", getCompania().getNombre());
		parameters.put("P_CTA_DESDE", ctaDesde);
		parameters.put("P_CTA_HASTA", ctaHasta);
		parameters.put("P_CENTRO", agencia);
		parameters.put("P_FECHA_DESDE", fechaDesde);
		parameters.put("P_FECHA_HASTA", fechaHasta);

	}

	private String getStrReportPagare() {
		String reportPath = null;
		reportPath = "cxcRepDistribucionCuentas.jasper";
		return reportPath;
	}

	public List<ConPlantas> getPlanCtas() {
		return planCtas;
	}

	public void setPlanCtas(List<ConPlantas> planCtas) {
		this.planCtas = planCtas;
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

	public String getCtaDesde() {
		return ctaDesde;
	}

	public void setCtaDesde(String ctaDesde) {
		this.ctaDesde = ctaDesde;
	}

	public String getCtaHasta() {
		return ctaHasta;
	}

	public void setCtaHasta(String ctaHasta) {
		this.ctaHasta = ctaHasta;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public void setAgencias(List<Agencia> agencias) {
		this.agencias = agencias;
	}

}
