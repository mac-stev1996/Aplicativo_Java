package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.InvControl;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcGeneracionAsientoServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceException;
import com.casabaca.prime.cxc.common.CommonController;

@ViewScoped
@ManagedBean
public class CxcGeneracionAsientoController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CxcGeneracionAsientoController.class);

	@EJB(lookup = NombreJNDI.CXC_GENERACION_ASIENTO_SERVICE)
	private CxcGeneracionAsientoServiceLocal service;

	private String noCia;
	private String centro;
	private BigDecimal anio;
	private BigDecimal mes;
	private Date fecha;
	private Date diaProceso;
	private Date diaProcesoCaja;
	private String noAsiento;
	private String noAsientoScb;
	private String codDiario;
	private String estado;

	@PostConstruct
	public void init() {
		if (null != getRequestParameter("noCia") && getRequestParameter("noCia").compareTo("") != 0
				&& null != getRequestParameter("centro") && getRequestParameter("centro").compareTo("") != 0) {
			noCia = getRequestParameter("noCia");
			centro = getRequestParameter("centro");
		} else {
			noCia = getCompania().getNoCia();
			centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		}
		cargarDatos();
	}

	private void cargarDatos() {
		Object[] arccct;
		try {
			arccct = service.getArccct(noCia);
			if (arccct != null) {
				codDiario = arccct[2] != null ? (String) arccct[2] : null;
			}
		} catch (FindException e) {
			super.error("La compañía no se encuentra definida");
			logger.error("La compañía no se encuentra definida", e);
			return;
		}

		InvControl control = service.getInvControl(noCia, centro);
		if (control.getId() != null && control.getId().getNoCia() != null) {
			anio = control.getAnoProceCxc();
			mes = control.getMesProceCxc();
			diaProceso = control.getDiaProcesoCxc();
			diaProcesoCaja = control.getDiaProcesoCaja();

			this.fecha = diaProceso;
			Calendar fechaTmp = new GregorianCalendar();
			fechaTmp.setTime(this.fecha);

			Integer anoMesProce = (anio.intValue() * 100) + mes.intValue();
			Integer fecha = (fechaTmp.get(Calendar.YEAR) * 100) + (fechaTmp.get(Calendar.MONDAY) + 1);

			if (anoMesProce < fecha) {
				int dia = FechaUtils.obtenerUltimoDiaMes(anio.intValue(), mes.intValue());
				fechaTmp.set(Calendar.DAY_OF_MONTH, dia);
				fechaTmp.set(Calendar.MONTH, (mes.intValue() - 1));
				fechaTmp.set(Calendar.YEAR, anio.intValue());
				this.fecha = fechaTmp.getTime();
			} else if (anoMesProce > fecha) {
				fechaTmp.set(Calendar.DAY_OF_MONTH, 1);
				fechaTmp.set(Calendar.MONTH, (mes.intValue() - 1));
				fechaTmp.set(Calendar.YEAR, anio.intValue());
				this.fecha = fechaTmp.getTime();
			}
		} else {
			super.error("El centro de distribución no se encuentra definido");
			logger.error("El centro de distribución no se encuentra definido");
			return;
		}

		Short anioCia = getCompania().getAnoProce();
		Short mesCia = getCompania().getMesProce();

		estado = (((anioCia * 100) + mesCia) <= ((anio.shortValue() * 100) + mes.shortValue())) ? "P" : "O";
	}

	public void generar() {
		try {
			String[] resultado = service.generar(noCia, centro, fecha, diaProceso, diaProcesoCaja, estado, anio, mes,
					codDiario);
			noAsiento = resultado[0];
			noAsientoScb = resultado[1];
			if (resultado[2] != null) {
				String[] mensajeError = resultado[2].split("\\|");
				for (String mensaje : mensajeError) {
					super.error(mensaje);
				}
			}
			if (resultado[3] != null)
				super.error(resultado[3]);
			if (resultado[4] != null)
				super.error(resultado[4]);
			if (resultado[5] != null)
				super.error(resultado[5]);
			if (resultado[2] == null && resultado[3] == null && resultado[4] == null && resultado[5] == null)
				super.info("Asiento generado");
		} catch (ServiceException e) {
			super.error("Error al generar asiento");
			super.error(e.getMessage());
			logger.error("Error al generar asiento", e);
		}
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public BigDecimal getAnio() {
		return anio;
	}

	public void setAnio(BigDecimal anio) {
		this.anio = anio;
	}

	public BigDecimal getMes() {
		return mes;
	}

	public void setMes(BigDecimal mes) {
		this.mes = mes;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getDiaProceso() {
		return diaProceso;
	}

	public void setDiaProceso(Date diaProceso) {
		this.diaProceso = diaProceso;
	}

	public Date getDiaProcesoCaja() {
		return diaProcesoCaja;
	}

	public void setDiaProcesoCaja(Date diaProcesoCaja) {
		this.diaProcesoCaja = diaProcesoCaja;
	}

	public String getNoAsiento() {
		return noAsiento;
	}

	public void setNoAsiento(String noAsiento) {
		this.noAsiento = noAsiento;
	}

	public String getNoAsientoScb() {
		return noAsientoScb;
	}

	public void setNoAsientoScb(String noAsientoScb) {
		this.noAsientoScb = noAsientoScb;
	}
}
