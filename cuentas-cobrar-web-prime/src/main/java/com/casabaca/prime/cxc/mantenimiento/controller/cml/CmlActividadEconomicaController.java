/**
 * 
 */
package com.casabaca.prime.cxc.mantenimiento.controller.cml;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.cml.dao.CmlActividadEconomicaDaoLocal;
import com.casabaca.cxc.cml.model.CmlActividadEconomica;
import com.casabaca.cxc.cml.model.CmlActividadEconomicaPK;
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
@ManagedBean(name = "cmlActividadEconomicaController")
public class CmlActividadEconomicaController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlActividadEconomicaDaoLocal cmlActividadEconomicaDaoLocal;

	private String noCia;
	private List<CmlActividadEconomica> listaActividadEconomica;
	private CmlActividadEconomica cmlActividadEconomicaSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;

	private boolean editar;


	public CmlActividadEconomicaController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlActividadEconomicaDaoLocal = (CmlActividadEconomicaDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlActividadEconomicaDaoBean");
			this.setListaActividadEconomica(cmlActividadEconomicaDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoActividadEconomica() {
		editar=false;
		setCodigo(null);
		setDescripcion(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgActividadEconomicaWV').show();");
	
	}
	

	public void mostrarDialogoActividadEconomicaEditar() {
		if(cmlActividadEconomicaSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlActividadEconomicaSeleccionado.getId().getCodigo());
			setDescripcion(cmlActividadEconomicaSeleccionado.getDescripcion());
			setEstado(cmlActividadEconomicaSeleccionado.getEstado());
			setValor(cmlActividadEconomicaSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgActividadEconomicaWV').show();");
		}
		
	}

	public void guardarActividadEconomica() {
		try {
			
			CmlActividadEconomica cmlActividadEconomicaEditar;
			if(editar) {
				cmlActividadEconomicaSeleccionado.setDescripcion(descripcion);
				cmlActividadEconomicaSeleccionado.setEstado(estado);
				cmlActividadEconomicaSeleccionado.setValor(valor);
				cmlActividadEconomicaEditar=cmlActividadEconomicaSeleccionado;
				
				cmlActividadEconomicaDaoLocal.edit(cmlActividadEconomicaEditar);
			}else {
				CmlActividadEconomicaPK id = new CmlActividadEconomicaPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlActividadEconomicaEditar=new CmlActividadEconomica();
				cmlActividadEconomicaEditar.setDescripcion(descripcion);
				cmlActividadEconomicaEditar.setEstado(estado);
				cmlActividadEconomicaEditar.setValor(valor);
				cmlActividadEconomicaEditar.setId(id);
				cmlActividadEconomicaDaoLocal.create(cmlActividadEconomicaEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaActividadEconomica(cmlActividadEconomicaDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgActividadEconomicaWV').hide();");
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

	
	public List<CmlActividadEconomica> getListaActividadEconomica() {
		return listaActividadEconomica;
	}

	public void setListaActividadEconomica(List<CmlActividadEconomica> listaActividadEconomica) {
		this.listaActividadEconomica = listaActividadEconomica;
	}

	public CmlActividadEconomica getCmlActividadEconomicaSeleccionado() {
		return cmlActividadEconomicaSeleccionado;
	}

	public void setCmlActividadEconomicaSeleccionado(CmlActividadEconomica cmlActividadEconomicaSeleccionado) {
		this.cmlActividadEconomicaSeleccionado = cmlActividadEconomicaSeleccionado;
	}


	public String getCodigo() {
		return codigo;
	}


	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}


	public String getDescripcion() {
		return descripcion;
	}


	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}


	public String getEstado() {
		return estado;
	}


	public void setEstado(String estado) {
		this.estado = estado;
	}


	public Integer getValor() {
		return valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}


	
}

