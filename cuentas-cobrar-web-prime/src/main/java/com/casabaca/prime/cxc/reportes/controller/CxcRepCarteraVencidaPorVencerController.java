package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
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
@ManagedBean(name = "cxcRepCarteraVencidaPorVencerController")
@ViewScoped
public class CxcRepCarteraVencidaPorVencerController extends ReportCommonController implements Serializable {
	static Logger logger = Logger.getLogger(CxcRepCarteraVencidaPorVencerController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -7900641687719651629L;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaServiceLocal;
	
	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;
	
	private String agencia;
	private String codigoAgenciaSelect;
	private Date fechaAnticipo;
	private String lineaNegocio;
	private List<LineaNegocio> listarLineasNegocio;
	private List<SelectItem> lineasNegocioSelectItems;

	@PostConstruct
	public void init() {
		this.listarLineasNegocio = new ArrayList<>();
		this.lineasNegocioSelectItems = new ArrayList<>();
		cargarLineasNrgocio();
	}
	
	public void cargarLineasNrgocio() {
		try {
			listarLineasNegocio = lineaNegocioService.obtenerLineasNoAplicaAnticipos(getCompania().getNoCia());
			for (LineaNegocio item : listarLineasNegocio) {
				lineasNegocioSelectItems
						.add(new SelectItem(item.getLineaNegocioPK().getNoLinea().toString(), item.getDescripcion()));
			}

		} catch (Exception e) {
			listarLineasNegocio = new ArrayList<>();
			logger.error(e);
			addInfoMessage("Info. ", " No se encontro al cliente ingresado");

		}
	}

	public String report() {
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		Map<String, Object> parameters = new HashMap<String, Object>();
		try {
			String ctxPath = getServletContext().getRealPath("/");
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			setParameters(parameters);
			connection = utilServiceDelegate.getDataSource().getConnection();
			if ("excel".equals(format)) {
				parameters.put("IMPRIME_CABECERA", CommonConstants.FALSE_VALUE);
			}else {
				parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
			}
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
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy");
	 	SimpleDateFormat format2 = new SimpleDateFormat("MM");
        String anioString = format1.format(fechaAnticipo);
        String mesString = format2.format(fechaAnticipo);
		parameters.put("PMES", new Integer(mesString));
		parameters.put("PANIO",new Integer(anioString));
		parameters.put("NOMBREEMPRESA", getCompania().getNombre());
		parameters.put("USUARIO", getUsuario().getUsuario());
		parameters.put("LINEA_NEGOCIO", lineaNegocio);
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public String getCodigoAgenciaSelect() {
		return codigoAgenciaSelect;
	}

	public void setCodigoAgenciaSelect(String codigoAgenciaSelect) {
		this.codigoAgenciaSelect = codigoAgenciaSelect;
	}

	public Date getFechaAnticipo() {
		return fechaAnticipo;
	}

	public void setFechaAnticipo(Date fechaAnticipo) {
		this.fechaAnticipo = fechaAnticipo;
	}

	public String getLineaNegocio() {
		return lineaNegocio;
	}

	public void setLineaNegocio(String lineaNegocio) {
		this.lineaNegocio = lineaNegocio;
	}

	public List<LineaNegocio> getListarLineasNegocio() {
		return listarLineasNegocio;
	}

	public void setListarLineasNegocio(List<LineaNegocio> listarLineasNegocio) {
		this.listarLineasNegocio = listarLineasNegocio;
	}

	public List<SelectItem> getLineasNegocioSelectItems() {
		return lineasNegocioSelectItems;
	}

	public void setLineasNegocioSelectItems(List<SelectItem> lineasNegocioSelectItems) {
		this.lineasNegocioSelectItems = lineasNegocioSelectItems;
	}
	
}
