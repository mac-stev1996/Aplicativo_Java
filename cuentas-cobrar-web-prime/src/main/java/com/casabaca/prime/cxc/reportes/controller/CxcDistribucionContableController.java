/* 
 * CxcDistribucionContableController.java 
 * 21 may. 2020
 * Copyright 2020 CASABACA.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.reportes.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogPlanCuentasController;
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
 * <b> Clase con la logica necesaria para emitir el reporte Distribucion Contable por Cuenta. </b>
 * 
 * @author Jorge Lucas
 * @version $1.0$
 */
@ManagedBean(name = "cxcDistribucionContableController")
@ViewScoped
public class CxcDistribucionContableController extends ReportCommonController implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3195838549741347104L;

	private static final String FLAG_CUENTA_INICIO = "D";

	private static final String FLAG_CUENTA_HASTA = "H";

	static Logger logger = Logger.getLogger(CxcDistribucionContableController.class);

	/**
	 * Servicio ejb para transaccionar contra la base de datos
	 */
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaServiceLocal;

	/**
	 * Dialog de cuentas contables
	 */
	@ManagedProperty("#{dialogPlanCuentasController}")
	private DialogPlanCuentasController dialogPlanCuentas;

	/**
	 * Variables para captura de informacion
	 */
	private List<Agencia> agenciaList;

	private String cuentaCodigoInicio;

	private String cuentaNombreInicio;

	private String cuentaCodigoHasta;

	private String cuentaNombreHasta;

	private String origen;

	/**
	 * <b> Metodo para llenar el combo de agencias. </b>
	 * <p>
	 * [Author Jorge Lucas, 22 may. 2020]
	 * </p>
	 *
	 * @return
	 */
	public List<SelectItem> getAgencias() {
		this.agenciaList = agenciaServiceLocal.findByCompania(getCompania().getNoCia(), new String[] { "nombre" });
		List<SelectItem> items = new ArrayList<>();
		items.add(new SelectItem(null, "TODAS"));
		agenciaList.forEach((agencias) -> {
			items.add(new SelectItem(agencias.getAgenciaPK().getCodigo(), agencias.getNombre()));
		});
		return items;
	}

	/**
	 * <b> Metodo para instaciar el dialog de la cuenta de inicio. </b>
	 * <p>
	 * [Author Jorge Lucas, 22 may. 2020]
	 * </p>
	 */
	public void cargarPlanCuentaDesde() {
		getDialogPlanCuentas().setNombreDialog("dialogPlanCuentasDesde");
		accionesDialog("dialogPlanCuentasDesde", Boolean.TRUE);
		origen = FLAG_CUENTA_INICIO;
	}

	/**
	 * <b> Metodo para instaciar el dialog de la cuenta de fin. </b>
	 * <p>
	 * [Author Jorge Lucas, 22 may. 2020]
	 * </p>
	 */
	public void cargarPlanCuentaHasta() {
		getDialogPlanCuentas().setNombreDialog("dialogPlanCuentasHasta");
		accionesDialog("dialogPlanCuentasHasta", Boolean.TRUE);
		origen = FLAG_CUENTA_HASTA;
	}

	/**
	 * <b> Metodo que captura los parametros de entrada y genera el reporte excel o pdf. </b>
	 * <p>
	 * [Author Jorge Lucas, 22 may. 2020]
	 * </p>
	 *
	 * @return
	 */
	public String report() {
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		Map<String, Object> parameters = new HashMap<>();
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
			if ("excel".equals(format)) {
				parameters.put("P_IMPRIME_CABECERA", CommonConstants.FALSE_VALUE);
			} else {
				parameters.put("P_IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
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

	/**
	 * <b> Metodo que se setea los valores ingresados en pantalla para generar el reporte. </b>
	 * <p>
	 * [Author Jorge Lucas, 22 may. 2020]
	 * </p>
	 *
	 * @param parameters
	 */
	private void setParameters(Map<String, Object> parameters) {
		parameters.put("P_DESC_COMPANIA", getCompania().getNombre());
		parameters.put("TITULO", "DISTRIBUCION CONTABLE POR CUENTA");
		parameters.put("P_NOCIA", getCompania().getNoCia());
		parameters.put("P_FECHA_DESDE", getFechaDesde());
		parameters.put("P_FECHA_HASTA", getFechaHasta());
		parameters.put("P_CENTRO", getAgencia());
		Agencia agenciaSeleccionada = getAgencia() == null ? null
				: agenciaList.stream().filter(agencia -> getAgencia().equals(agencia.getAgenciaPK().getCodigo()))
						.findAny().orElse(null);
		parameters.put("NOMBRE_AGENCIA", agenciaSeleccionada == null ? "TODAS" : agenciaSeleccionada.getNombre());
		parameters.put("P_CUENTA_DESDE", getCuentaCodigoInicio());
		parameters.put("P_CUENTA_HASTA", getCuentaCodigoHasta());
	}

	public DialogPlanCuentasController getDialogPlanCuentas() {
		return dialogPlanCuentas;
	}

	public void setDialogPlanCuentas(DialogPlanCuentasController dialogPlanCuentas) {
		this.dialogPlanCuentas = dialogPlanCuentas;
	}

	public List<Agencia> getAgenciaList() {
		return agenciaList;
	}

	public void setAgenciaList(List<Agencia> agenciaList) {
		this.agenciaList = agenciaList;
	}

	public String getCuentaCodigoInicio() {
		if (FLAG_CUENTA_INICIO.equals(origen) && this.dialogPlanCuentas.getConPlantas() != null
				&& this.dialogPlanCuentas.getConPlantas().getConPlatasPk() != null) {
			this.cuentaCodigoInicio = this.dialogPlanCuentas.getConPlantas().getConPlatasPk().getCtaconId();
			this.cuentaNombreInicio = this.dialogPlanCuentas.getConPlantas().getNombre();
		}
		return cuentaCodigoInicio;
	}

	public void setCuentaCodigoInicio(String cuentaCodigoInicio) {
		this.cuentaCodigoInicio = cuentaCodigoInicio;
	}

	public String getCuentaNombreInicio() {
		return cuentaNombreInicio;
	}

	public void setCuentaNombreInicio(String cuentaNombreInicio) {
		this.cuentaNombreInicio = cuentaNombreInicio;
	}

	public String getCuentaCodigoHasta() {
		if (FLAG_CUENTA_HASTA.equals(origen) && this.dialogPlanCuentas.getConPlantas() != null
				&& this.dialogPlanCuentas.getConPlantas().getConPlatasPk() != null) {
			this.cuentaCodigoHasta = this.dialogPlanCuentas.getConPlantas().getConPlatasPk().getCtaconId();
			this.cuentaNombreHasta = this.dialogPlanCuentas.getConPlantas().getNombre();
		}
		return cuentaCodigoHasta;
	}

	public void setCuentaCodigoHasta(String cuentaCodigoHasta) {
		this.cuentaCodigoHasta = cuentaCodigoHasta;
	}

	public String getCuentaNombreHasta() {
		return cuentaNombreHasta;
	}

	public void setCuentaNombreHasta(String cuentaNombreHasta) {
		this.cuentaNombreHasta = cuentaNombreHasta;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}
}
