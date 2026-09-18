package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.InvControl;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcCierreDiarioServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceException;
import com.casabaca.prime.cxc.common.CommonController;

@ViewScoped
@ManagedBean
public class CxcCierreDiarioController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CxcCierreDiarioController.class);

	@EJB(lookup = NombreJNDI.CXC_CIERRE_DIARIO_SERVICE)
	private CxcCierreDiarioServiceLocal service;

	private String noCia;
	private List<InvControl> lista;

	private String centro;
	private Date diaCaja;
	private BigDecimal anio;
	private BigDecimal mes;
	private Date dia;

	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		cargarLista();
	}

	public void cargarLista() {
		try {
			lista = service.getLista(noCia, centro, diaCaja, anio, mes, dia);
		} catch (FindException e) {
			lista = new ArrayList<InvControl>();
		}
	}

	public void cerrar() {
		try {

			if (lista == null || lista.isEmpty()) {
				super.error("Nada para cerrar");
				return;
			
			}
			
			if (!validarCamposCerrar()) {
				return;
			}

			InvControl seleccionado = lista.get(0);

			BigDecimal anio = lista.get(0).getAnoProceCxc().add(BigDecimal.ONE);
			boolean existeCalendario = false;
			try {
				existeCalendario = service.verificarExisteCalendario(seleccionado.getId().getNoCia(), anio);
			} catch (FindException e) {
				super.error("Error al verificar si existe creado el calendario");
				logger.error("Error al verificar si existe creado el calendario", e);
				return;
			}
			if (!existeCalendario) {
				try {
					service.generarCalendario(seleccionado.getId().getNoCia(), anio);
				} catch (ServiceException e) {
					super.error("Error al generar calendario");
					logger.error("Error al generar calendario", e);
					return;
				}
			}

			service.cerrar(lista);
			super.info("Proceso de cierre realizado con éxito");
			cargarLista();
		} catch (ServiceException e) {
			super.error("Ocurrió un error al cerrar");
			super.error(e.getMessage());
			logger.error("Ocurrió un error al cerrar", e);
		}
	}

	/**
	 * <b> Extraccion de Metodo para PMD  </b>
	 * <p>
	 * [Author jorge.reyes, Feb 19, 2024]
	 * </p>
	 *
	 * @param fechaInicio
	 */
	private boolean validarCamposCerrar() {
		
		Date fechaCaja = FechaUtils.truncarFecha(lista.get(0).getDiaProcesoCaja());
		Date fechaCxc = FechaUtils.truncarFecha(lista.get(0).getDiaProcesoCxc());
		
		if (lista.stream().anyMatch(c -> !fechaCaja.equals(FechaUtils.truncarFecha(c.getDiaProcesoCaja())))) {
			super.error("No puede realizar el cierre, existen cajas sin cerrar... verifique");
			return false;
		}	
		if (lista.stream().anyMatch(c -> !fechaCxc.equals(FechaUtils.truncarFecha(c.getDiaProcesoCxc())))) {
			super.error("No puede realizar el cierre, fechas de CxC no pueden ser diferentes... verifique");
			return false;
		}	
		if (lista.stream().anyMatch(c -> "SI".equals(c.getFaltaContabilizar()))) {
			super.error("No puede realizar el cierre existe contabilizacion pendiente de generar... verifique");
			return false;
		}
		if (lista.stream().anyMatch(c -> c.getDiaProcesoCxc() == null)) {
			super.error("No puede realizar el Proceso de cierre.. Verifique datos");
			return false;
		}
		if (lista.stream().anyMatch(c -> FechaUtils.truncarFecha(c.getDiaProcesoCaja()).compareTo(FechaUtils.truncarFecha(c.getDiaProcesoCxc())) <= 0)) {
			super.error("No puede realizar el cierre existen cajas sin cerrar... verifique");
			return false;
		}
		return true;
	}

	public void irAGeneracionAsiento(InvControl control) {
		String url = "/cxc-web-prime/jsf/procesos/cxcGeneracionAsiento.jsf?noCia=" + control.getId().getNoCia()
				+ "&centro=" + control.getId().getCentro();
		ejecutarJavascript("openDuplicatedTab('" + url + "');");
	}

	public List<InvControl> getLista() {
		return lista;
	}

	public void setLista(List<InvControl> lista) {
		this.lista = lista;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public Date getDiaCaja() {
		return diaCaja;
	}

	public void setDiaCaja(Date diaCaja) {
		this.diaCaja = diaCaja;
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

	public Date getDia() {
		return dia;
	}

	public void setDia(Date dia) {
		this.dia = dia;
	}
}
