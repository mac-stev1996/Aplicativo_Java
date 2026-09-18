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
import com.casabaca.cxc.cml.dao.CmlLineaNegocioDaoLocal;
import com.casabaca.cxc.cml.model.CmlLineaNegocio;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.mantenimiento.controller.ControlOficialCumplimientoController;

/**
 * @author Diego Ramirez
 *
 */
@ViewScoped
@ManagedBean(name = "cmlLineaNegocioController")
public class CmlLineaNegocioController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlLineaNegocioDaoLocal cmlLineaNegocioDaoLocal;

	private String noCia;
	private List<CmlLineaNegocio> listaLineaNegocio;
	private CmlLineaNegocio cmlLineaNegocioSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;

	public CmlLineaNegocioController() {
		this.noCia = getCompania().getNoCia();
		try {
			cmlLineaNegocioDaoLocal = (CmlLineaNegocioDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlLineaNegocioDaoBean");
			this.setListaLineaNegocio(cmlLineaNegocioDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}


	public void mostrarDialogoLineaNegocioEditar() {
		if(cmlLineaNegocioSeleccionado==null) {
			warn("Seleccione un registro");
		}else {
			setCodigo(cmlLineaNegocioSeleccionado.getId().getCodigo());
			setDescripcion(cmlLineaNegocioSeleccionado.getDescripcion());
			setEstado(cmlLineaNegocioSeleccionado.getEstado());
			setValor(cmlLineaNegocioSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgLineaNegocioWV').show();");
		}
		
	}

	public void guardarLineaNegocio() {
		try {
			
			CmlLineaNegocio cmlLineaNegocioEditar;
			cmlLineaNegocioSeleccionado.setEstado(estado);
			cmlLineaNegocioSeleccionado.setValor(valor);
			cmlLineaNegocioEditar=cmlLineaNegocioSeleccionado;
			
			cmlLineaNegocioDaoLocal.edit(cmlLineaNegocioEditar);

			info("se guardó exitosamente.");
			
			this.setListaLineaNegocio(cmlLineaNegocioDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgLineaNegocioWV').hide();");
		} catch (UpdateException e) {
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

	
	public List<CmlLineaNegocio> getListaLineaNegocio() {
		return listaLineaNegocio;
	}

	public void setListaLineaNegocio(List<CmlLineaNegocio> listaLineaNegocio) {
		this.listaLineaNegocio = listaLineaNegocio;
	}

	public CmlLineaNegocio getCmlLineaNegocioSeleccionado() {
		return cmlLineaNegocioSeleccionado;
	}

	public void setCmlLineaNegocioSeleccionado(CmlLineaNegocio cmlLineaNegocioSeleccionado) {
		this.cmlLineaNegocioSeleccionado = cmlLineaNegocioSeleccionado;
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

