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
import com.casabaca.cxc.cml.dao.CmlNacionalidadDaoLocal;
import com.casabaca.cxc.cml.model.CmlNacionalidad;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.mantenimiento.controller.ControlOficialCumplimientoController;

/**
 * @author Diego Ramirez
 *
 */
@ViewScoped
@ManagedBean(name = "cmlNacionalidadController")
public class CmlNacionalidadController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlNacionalidadDaoLocal cmlNacionalidadDaoLocal;

	private String noCia;
	private List<CmlNacionalidad> listaNacionalidad;
	private CmlNacionalidad cmlNacionalidadSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;



	public CmlNacionalidadController() {
		this.noCia = getCompania().getNoCia();
		try {
			cmlNacionalidadDaoLocal = (CmlNacionalidadDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlNacionalidadDaoBean");
			this.setListaNacionalidad(cmlNacionalidadDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}


	public void mostrarDialogoNacionalidadEditar() {
		if(cmlNacionalidadSeleccionado==null) {
			warn("Seleccione un registro");
		}else {
			setCodigo(cmlNacionalidadSeleccionado.getId().getCodigo());
			setDescripcion(cmlNacionalidadSeleccionado.getDescripcion());
			setEstado(cmlNacionalidadSeleccionado.getEstado());
			setValor(cmlNacionalidadSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgNacionalidadWV').show();");
		}
		
	}

	public void guardarNacionalidad() {
		try {
			CmlNacionalidad cmlNacionalidadEditar;

			cmlNacionalidadSeleccionado.setEstado(estado);
			cmlNacionalidadSeleccionado.setValor(valor);
			cmlNacionalidadEditar=cmlNacionalidadSeleccionado;
			cmlNacionalidadDaoLocal.edit(cmlNacionalidadEditar);
			
			info("se guardó exitosamente.");
			
			this.setListaNacionalidad(cmlNacionalidadDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgNacionalidadWV').hide();");
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

	
	public List<CmlNacionalidad> getListaNacionalidad() {
		return listaNacionalidad;
	}

	public void setListaNacionalidad(List<CmlNacionalidad> listaNacionalidad) {
		this.listaNacionalidad = listaNacionalidad;
	}

	public CmlNacionalidad getCmlNacionalidadSeleccionado() {
		return cmlNacionalidadSeleccionado;
	}

	public void setCmlNacionalidadSeleccionado(CmlNacionalidad cmlNacionalidadSeleccionado) {
		this.cmlNacionalidadSeleccionado = cmlNacionalidadSeleccionado;
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

