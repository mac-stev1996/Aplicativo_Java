package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.ejb.model.ArccdaPK;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.CommonNativeServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.Arccaux1;
import com.casabaca.cxc.ejb.servicio.Arccaux1ServiceLocal;
import com.casabaca.cxc.ejb.servicio.BajaSaldosMenoresServiceLocal;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;

@ViewScoped
@ManagedBean
public class CxcManBajaSaldosController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -186960203661665077L;

	static final Logger LOG = Logger.getLogger(CxcManBajaSaldosController.class);

	// EJB
	@EJB(lookup = NombreJNDI.BAJA_SALDOS_SERVICE)
	private BajaSaldosMenoresServiceLocal bajaSaldosService;

	@EJB(lookup = NombreJNDI.ARCCAUX1_SERVICE)
	private Arccaux1ServiceLocal arccauxService;

	@EJB(lookup = NombreJNDI.COMMON_NATIVE_SERVICE_LOCAL)
	private CommonNativeServiceLocal commonNativeService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal servicesParamDet;

	private static final String REPORTE = "/cxc/cxcBajaGeneradaPorTipoProceso";
	private String noCia;
	private Double valor = 0D;
	private String tipoProceso;
	private String tipoBaja;
	private Date fechaIni;
	private Date fechaFin;
	private String canal;
	private String anio;
	private String usuario;
	private Boolean activaB;
	private Boolean activaM;
	private Boolean activaConta;
	private List<Arccaux1> listArccaux1;
	private BigDecimal total;
	private String centro;
	private String reporte;
	private String formato;
	private ParamDet paramDet;

	public CxcManBajaSaldosController() {
		this.noCia = getCompania().getNoCia();
		this.usuario = getUsuario().getUsuario();
		this.centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
	}

	@PostConstruct
	public void init() {
		limpiarDatos();

	}

	public void limpiarDatos() {

		valor = 0D;
		tipoBaja = "N";
		fechaIni = null;
		fechaFin = null;
		canal = null;
		anio = null;
		activaB = Boolean.FALSE;
		activaM = Boolean.FALSE;
		activaConta = Boolean.FALSE;
		listArccaux1 = new ArrayList<>();
	}

	public void detalleConsulta() {
		String url = "/cxc-web-prime/jsf/procesos/cxcConsultaBajaSaldosPorAnio.jsf?origen=D";
		try {
			super.redirect(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void agregar() {

		try {
			if (!validarDatos()) {

				String resultado = bajaSaldosService.procesarBaja(noCia, fechaIni, fechaFin, anio, valor, tipoBaja,
						tipoProceso, canal, usuario);
				if (resultado != null && !resultado.contains("Error:")) {
					consultarBajas();
				} else {

					addErrorMessage("Error", resultado);
				}

			}

		} catch (Exception e) {
			LOG.error(e.getMessage());
			addErrorMessage("Error", e.getMessage());
		}

	}

	public Boolean validarDatos() {

		if (tipoBaja.equals("N")) {
			if (valor.equals(null) || "".equals(valor) || valor == 0) {
				addErrorMessage("Error", "Por favor ingresar Valor ");
				return true;
			}
		} else if (tipoBaja.equals("A")) {
			if (valor.equals(null) || "".equals(valor) || valor == 0) {
				addErrorMessage("Error", "Por favor ingresar Valor");
				return true;
			} else if (anio.equals(null) || anio.isEmpty()) {
				addErrorMessage("Error", "Por favor ingresar Año");
				return true;
			}
		} else if (tipoBaja.equals("F")) {
			if (valor.equals(null) || "".equals(valor) || valor == 0) {
				addErrorMessage("Error", "Por favor ingresar Valor");
				return true;
			}
			if (fechaIni == null || "".equals(fechaIni)) {
				addErrorMessage("Error", "Por favor ingresar fecha inicial");
				return true;
			}
			if (fechaFin == null || "".equals(fechaFin)) {
				addErrorMessage("Error", "Por favor ingresar fecha final");
				return true;
			}

		}
		return false;

	}

	public void consultarBajas() {

		listArccaux1 = bajaSaldosService.documentosBajaSaldo(noCia, usuario, tipoBaja, anio);

		if (listArccaux1.isEmpty()) {
			addWarnMessage("Warning", "No existe informacion para el Valor ingresado" + " " + valor);
			limpiarDatos();
			activaConta = Boolean.FALSE;
			return;
		} else {
			activaConta = Boolean.TRUE;
		}

	}

	public void procesarConta() {
		try {
			if (!listArccaux1.isEmpty()) {
				String resultado = bajaSaldosService.contabilizacionBajaSaldos(noCia, centro, tipoProceso, anio,
						usuario);
				if (resultado != null && !resultado.contains("Error:") && !resultado.contains("ORA-")) {
					addInfoMessage("Exito", resultado);
					init();
				} else {
					addErrorMessage("Error", resultado);
				}

			} else {
				addErrorMessage("Error", "No se puede generar Contabilización.. No a PROCESADO los Datos....");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void eliminar(Arccaux1 tablaAux) {

		try {
			int eli = arccauxService.eliminar(tablaAux.getId().getNoCia(), tablaAux.getUsuarioProceso(),
					tablaAux.getFechaProceso(), tablaAux.getId().getNoDocu());
			if (eli == 0) {
				warn("No existe datos para eliminar con los parámetros ingresados");
				return;
			}
			info("Datos eliminados con éxito. NoFisico: " + " " + tablaAux.getId().getNoDocu());
			consultarBajas();
		} catch (DeleteException e) {
			e.getMessage();
			addErrorMessage("Error", e.getMessage());
			LOG.error(e.getMessage());
		}

	}

	public void activarCamposReporte() {

		listArccaux1 = new ArrayList<>();
		valor = 0D;
		if ("N".equalsIgnoreCase(tipoBaja)) {

			fechaIni = null;
			fechaFin = null;
			anio = null;

			activaB = Boolean.FALSE;
			activaM = Boolean.FALSE;

		}
		if ("A".equalsIgnoreCase(tipoBaja)) {
			fechaIni = null;
			fechaFin = null;
			activaB = Boolean.TRUE;
			activaM = Boolean.FALSE;

		}

		if ("F".equalsIgnoreCase(tipoBaja)) {
			anio = null;
			activaB = Boolean.FALSE;
			activaM = Boolean.TRUE;

		}

	}

	public void excel() {
		if (!listArccaux1.isEmpty()) {
			this.formato = CommonConstants.OUTPUT_XLSX_NO_PAG;
			reporte = REPORTE + CommonUtils.getCiaReportUnit(getCompania().getNoCia());
			reporte();
		} else {
			
			addErrorMessage("Error", "No a PROCESADO los Datos....");
		}
	}

	public void reporte() {
		Map<String, Object> parameters = new HashMap<>();
		setParameters(parameters);
		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(super.callJasperReport(this.reporte, parameters, this.formato));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("p_tipo_proceso", tipoProceso);
		parameters.put("p_fecha_proceso", new Date());
		parameters.put("p_procesado", 'P');

	}

	// GETTER Y SETTER

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public Date getFechaIni() {
		return fechaIni;
	}

	public void setFechaIni(Date fechaIni) {
		this.fechaIni = fechaIni;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getCanal() {
		return canal;
	}

	public void setCanal(String canal) {
		this.canal = canal;
	}

	public String getAnio() {
		return anio;
	}

	public void setAnio(String anio) {
		this.anio = anio;
	}

	public Boolean getActivaB() {
		return activaB;
	}

	public void setActivaB(Boolean activaB) {
		this.activaB = activaB;
	}

	public Boolean getActivaM() {
		return activaM;
	}

	public void setActivaM(Boolean activaM) {
		this.activaM = activaM;
	}

	public List<Arccaux1> getListArccaux1() {
		return listArccaux1;
	}

	public void setListArccaux1(List<Arccaux1> listArccaux1) {
		this.listArccaux1 = listArccaux1;
	}

	public BigDecimal getTotal() {
		total = BigDecimal.ZERO;
		if (listArccaux1 != null && !listArccaux1.isEmpty()) {
			total = listArccaux1.stream().map(Arccaux1::getSaldo).reduce(BigDecimal::add).get();
		}
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getTipoBaja() {
		return tipoBaja;
	}

	public void setTipoBaja(String tipoBaja) {
		this.tipoBaja = tipoBaja;
	}

	public String getTipoProceso() {
		return tipoProceso;
	}

	public void setTipoProceso(String tipoProceso) {
		this.tipoProceso = tipoProceso;
	}

	public Boolean getActivaConta() {
		return activaConta;
	}

	public void setActivaConta(Boolean activaConta) {
		this.activaConta = activaConta;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public ParamDet getParamDet() {
		return paramDet;
	}

	public void setParamDet(ParamDet paramDet) {
		this.paramDet = paramDet;
	}

}
