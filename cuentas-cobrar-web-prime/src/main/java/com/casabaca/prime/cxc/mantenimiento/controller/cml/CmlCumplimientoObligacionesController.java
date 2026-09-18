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
import com.casabaca.cxc.cml.dao.CmlCumplimientoObligacionesDaoLocal;
import com.casabaca.cxc.cml.model.CmlCumplimientoObligaciones;
import com.casabaca.cxc.cml.model.CmlCumplimientoObligacionesPK;
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
@ManagedBean(name = "cmlCumplimientoObligacionesController")
public class CmlCumplimientoObligacionesController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlCumplimientoObligacionesDaoLocal cmlCumplimientoObligacionesDaoLocal;

	private String noCia;
	private List<CmlCumplimientoObligaciones> listaCumplimientoObligaciones;
	private CmlCumplimientoObligaciones cmlCumplimientoObligacionesSeleccionado;
	
	//Dialog
	private String tipo;
	private Integer valor;

	private boolean editar;


	public CmlCumplimientoObligacionesController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlCumplimientoObligacionesDaoLocal = (CmlCumplimientoObligacionesDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlCumplimientoObligacionesDaoBean");
			this.setListaCumplimientoObligaciones(cmlCumplimientoObligacionesDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}



	public void mostrarDialogoCumplimientoObligacionesEditar() {
		if(cmlCumplimientoObligacionesSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setTipo(cmlCumplimientoObligacionesSeleccionado.getId().getTipo());
			setValor(cmlCumplimientoObligacionesSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgCumplimientoObligacionesWV').show();");
		}
		
	}

	public void guardarCumplimientoObligaciones() {
		try {
			
			CmlCumplimientoObligaciones cmlCumplimientoObligacionesEditar;
			if(editar) {
				cmlCumplimientoObligacionesSeleccionado.setValor(valor);
				cmlCumplimientoObligacionesEditar=cmlCumplimientoObligacionesSeleccionado;
				
				cmlCumplimientoObligacionesDaoLocal.edit(cmlCumplimientoObligacionesEditar);
			}else {
				CmlCumplimientoObligacionesPK id = new CmlCumplimientoObligacionesPK();
				id.setTipo(tipo);
				id.setNoCia(noCia);
				
				cmlCumplimientoObligacionesEditar=new CmlCumplimientoObligaciones();
				cmlCumplimientoObligacionesEditar.setValor(valor);
				cmlCumplimientoObligacionesEditar.setId(id);
				cmlCumplimientoObligacionesDaoLocal.create(cmlCumplimientoObligacionesEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaCumplimientoObligaciones(cmlCumplimientoObligacionesDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgCumplimientoObligacionesWV').hide();");
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

	
	public List<CmlCumplimientoObligaciones> getListaCumplimientoObligaciones() {
		return listaCumplimientoObligaciones;
	}

	public void setListaCumplimientoObligaciones(List<CmlCumplimientoObligaciones> listaCumplimientoObligaciones) {
		this.listaCumplimientoObligaciones = listaCumplimientoObligaciones;
	}

	public CmlCumplimientoObligaciones getCmlCumplimientoObligacionesSeleccionado() {
		return cmlCumplimientoObligacionesSeleccionado;
	}

	public void setCmlCumplimientoObligacionesSeleccionado(CmlCumplimientoObligaciones cmlCumplimientoObligacionesSeleccionado) {
		this.cmlCumplimientoObligacionesSeleccionado = cmlCumplimientoObligacionesSeleccionado;
	}

	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}



	public Integer getValor() {
		return valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}


	
}

