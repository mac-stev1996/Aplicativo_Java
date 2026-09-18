/**
 * 
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.Arccaux1;
import com.casabaca.cxc.ejb.servicio.Arccaux1ServiceLocal;
import com.casabaca.cxc.ejb.servicio.BajaSaldosMenoresServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.vehiculos.ejb.modelo.VehOrdenCompraDetAgrupado;

/**
 * @author laura.llangari
 *
 */
@ViewScoped
@ManagedBean
public class CxcConsultaBajaSaldosPorAnioController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7177093116010041919L;
	private String anio;
	private List<Arccaux1> listSaldos;
	private Boolean activaCampo;
	private String noCia;
	private String reporte;
	private String formato;
	private Arccaux1    arccaux1;
	private static final String REPORTE = "/cxc/cxcBajaGeneradaPorTipoProceso";

	// EJB
	@EJB(lookup = NombreJNDI.BAJA_SALDOS_SERVICE)
	private BajaSaldosMenoresServiceLocal bajaSaldosService;

	@EJB(lookup = NombreJNDI.ARCCAUX1_SERVICE)
	private Arccaux1ServiceLocal arccauxService;

	@PostConstruct
	public void init() {

		activaCampo = Boolean.FALSE;
		noCia=getCompania().getNoCia();

	}

	public void cargar() {
		activaCampo = Boolean.TRUE;
		listSaldos= bajaSaldosService.listaBajaSaldos(noCia, anio);
		
	}
	
	public Integer getCantidadTotalSaldos() {
		
		Double total = 0D;
		if(this.listSaldos!=null) {
			
			for (Arccaux1 ocda : listSaldos) {
				total = total + ocda.getSaldo().intValue();
			}
			
		}
		return total.intValue();
		
	}
	
	public void regresar() {

		// deudaSeleccionada = objetoSeleccionado;

		String url = "/cxc-web-prime/jsf/procesos/cxcManBajaSaldosMenores.jsf?origen=D";

		try {
			super.redirect(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void excel(Arccaux1 tablaAux) {
		arccaux1=tablaAux;
		this.formato = CommonConstants.OUTPUT_XLSX_NO_PAG;
		reporte = REPORTE + CommonUtils.getCiaReportUnit(getCompania().getNoCia());
		reporte();
	}

	public void reporte() {
		Map<String, Object> parameters = new HashMap<>();
		setParameters(parameters);
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(super.callJasperReport(this.reporte, parameters, this.formato));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void setParameters(Map<String, Object> parameters) {
			parameters.put("p_tipo_proceso",arccaux1.getTipoProceso());
			parameters.put("p_fecha_proceso", arccaux1.getFechaProceso());
			parameters.put("p_procesado", 'G');			

	}
	
	

	// GETTER Y SETTER

	public String getAnio() {
		return anio;
	}

	public void setAnio(String anio) {
		this.anio = anio;
	}

	public List<Arccaux1> getListSaldos() {
		return listSaldos;
	}

	public void setListSaldos(List<Arccaux1> listSaldos) {
		this.listSaldos = listSaldos;
	}

	public Boolean getActivaCampo() {
		return activaCampo;
	}

	public void setActivaCampo(Boolean activaCampo) {
		this.activaCampo = activaCampo;
	}
	
	

}
