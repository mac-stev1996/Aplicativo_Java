/**
 * 
 */
package com.casabaca.prime.cxc.mantenimiento.controller.cml;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.cml.dao.CmlRangosRiesgoDaoLocal;
import com.casabaca.cxc.cml.model.CmlRangosRiesgo;
import com.casabaca.cxc.cml.model.CmlRangosRiesgoPK;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.mantenimiento.controller.ControlOficialCumplimientoController;

/**
 * @author Diego Ramirez
 *
 */
@ViewScoped
@ManagedBean(name = "cmlRangosRiesgoController")
public class CmlRangosRiesgoController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlRangosRiesgoDaoLocal cmlRangosRiesgoDaoLocal;

	private String noCia;
	private List<CmlRangosRiesgo> listaRangosRiesgo;
	private CmlRangosRiesgo cmlRangosRiesgoSeleccionado;
	
	//Dialog
	
	private String codigo;
	private String estado;
	private BigDecimal desde;
	private BigDecimal hasta;
	private Integer valor;


	private boolean editar;


	public CmlRangosRiesgoController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlRangosRiesgoDaoLocal = (CmlRangosRiesgoDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlRangosRiesgoDaoBean");
			this.setListaRangosRiesgo(cmlRangosRiesgoDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}



	public void mostrarDialogoRangosRiesgoEditar() {
		if(cmlRangosRiesgoSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlRangosRiesgoSeleccionado.getId().getCodigo());
			setDesde(cmlRangosRiesgoSeleccionado.getDesde());
			setHasta(cmlRangosRiesgoSeleccionado.getHasta());
			setEstado(cmlRangosRiesgoSeleccionado.getEstado());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgRangosRiesgoWV').show();");
		}
		
	}

	public void guardarRangosRiesgo() {
		try {
			
			CmlRangosRiesgo cmlRangosRiesgoEditar;
			if(editar) {
				cmlRangosRiesgoSeleccionado.setDesde(desde);
				cmlRangosRiesgoSeleccionado.setHasta(hasta);
				cmlRangosRiesgoSeleccionado.setEstado(estado);
				cmlRangosRiesgoEditar=cmlRangosRiesgoSeleccionado;
				
				cmlRangosRiesgoDaoLocal.edit(cmlRangosRiesgoEditar);
			}else {
				CmlRangosRiesgoPK id = new CmlRangosRiesgoPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlRangosRiesgoEditar=new CmlRangosRiesgo();
				cmlRangosRiesgoEditar.setDesde(desde);
				cmlRangosRiesgoEditar.setHasta(hasta);
				cmlRangosRiesgoEditar.setEstado(estado);
				cmlRangosRiesgoEditar.setId(id);
				cmlRangosRiesgoDaoLocal.create(cmlRangosRiesgoEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaRangosRiesgo(cmlRangosRiesgoDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgRangosRiesgoWV').hide();");
		} catch (InsertException | UpdateException e) {
			String mensaje="Error al guardar: ";
			if(e.getMessage()!=null) {
				mensaje=mensaje+e.getMessage();
			}
			if(e.getCause()!=null) {
				Throwable causa = e.getCause();
				mensaje=mensaje+" -> "+causa.getMessage();
				if(causa.getCause()!=null) {
					mensaje=mensaje+" -> "+causa.getCause().getMessage();
				}
			}
			error(mensaje);
			logger.error(mensaje);
		}
	}
	
	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	
	public List<CmlRangosRiesgo> getListaRangosRiesgo() {
		return listaRangosRiesgo;
	}

	public void setListaRangosRiesgo(List<CmlRangosRiesgo> listaRangosRiesgo) {
		this.listaRangosRiesgo = listaRangosRiesgo;
	}

	public CmlRangosRiesgo getCmlRangosRiesgoSeleccionado() {
		return cmlRangosRiesgoSeleccionado;
	}

	public void setCmlRangosRiesgoSeleccionado(CmlRangosRiesgo cmlRangosRiesgoSeleccionado) {
		this.cmlRangosRiesgoSeleccionado = cmlRangosRiesgoSeleccionado;
	}


	public String getCodigo() {
		return codigo;
	}


	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}


	public String getEstado() {
		return estado;
	}


	public void setEstado(String estado) {
		this.estado = estado;
	}


	public BigDecimal getDesde() {
		return desde;
	}


	public void setDesde(BigDecimal desde) {
		this.desde = desde;
	}


	public BigDecimal getHasta() {
		return hasta;
	}


	public void setHasta(BigDecimal hasta) {
		this.hasta = hasta;
	}


	public Integer getValor() {
		return valor;
	}


	public void setValor(Integer valor) {
		this.valor = valor;
	}



	
}

