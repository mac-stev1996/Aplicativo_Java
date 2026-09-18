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
import com.casabaca.cxc.cml.dao.CmlAgenciaDaoLocal;
import com.casabaca.cxc.cml.model.CmlAgencia;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.mantenimiento.controller.ControlOficialCumplimientoController;

/**
 * @author Diego Ramirez
 *
 */
@ViewScoped
@ManagedBean(name = "cmlAgenciaController")
public class CmlAgenciaController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlAgenciaDaoLocal cmlAgenciaDaoLocal;

	private String noCia;
	private List<CmlAgencia> listaAgencia;
	private CmlAgencia cmlAgenciaSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;



	public CmlAgenciaController() {
		this.noCia = getCompania().getNoCia();
		try {
			cmlAgenciaDaoLocal = (CmlAgenciaDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlAgenciaDaoBean");
			this.setListaAgencia(cmlAgenciaDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}


	public void mostrarDialogoAgenciaEditar() {
		if(cmlAgenciaSeleccionado==null) {
			warn("Seleccione un registro");
		}else {
			setCodigo(cmlAgenciaSeleccionado.getId().getCodigo());
			setDescripcion(cmlAgenciaSeleccionado.getDescripcion());
			setEstado(cmlAgenciaSeleccionado.getEstado());
			setValor(cmlAgenciaSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgAgenciaWV').show();");
		}
		
	}

	public void guardarAgencia() {
		try {
			
			CmlAgencia cmlAgenciaEditar;
			cmlAgenciaSeleccionado.setEstado(estado);
			cmlAgenciaSeleccionado.setValor(valor);
			cmlAgenciaEditar=cmlAgenciaSeleccionado;
			
			cmlAgenciaDaoLocal.edit(cmlAgenciaEditar);
			
			info("se guardó exitosamente.");
			
			this.setListaAgencia(cmlAgenciaDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgAgenciaWV').hide();");
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

	
	public List<CmlAgencia> getListaAgencia() {
		return listaAgencia;
	}

	public void setListaAgencia(List<CmlAgencia> listaAgencia) {
		this.listaAgencia = listaAgencia;
	}

	public CmlAgencia getCmlAgenciaSeleccionado() {
		return cmlAgenciaSeleccionado;
	}

	public void setCmlAgenciaSeleccionado(CmlAgencia cmlAgenciaSeleccionado) {
		this.cmlAgenciaSeleccionado = cmlAgenciaSeleccionado;
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

