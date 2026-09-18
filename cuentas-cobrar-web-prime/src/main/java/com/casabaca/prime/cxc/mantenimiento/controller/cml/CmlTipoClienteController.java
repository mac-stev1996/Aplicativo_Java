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
import com.casabaca.cxc.cml.dao.CmlTipoClienteDaoLocal;
import com.casabaca.cxc.cml.model.CmlTipoCliente;
import com.casabaca.cxc.cml.model.CmlTipoClientePK;
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
@ManagedBean(name = "cmlTipoClienteController")
public class CmlTipoClienteController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlTipoClienteDaoLocal cmlTipoClienteDaoLocal;

	private String noCia;
	private List<CmlTipoCliente> listaTipoCliente;
	private CmlTipoCliente cmlTipoClienteSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private String tipo;
	private Integer valor;

	private boolean editar;


	public CmlTipoClienteController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlTipoClienteDaoLocal = (CmlTipoClienteDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlTipoClienteDaoBean");
			this.setListaTipoCliente(cmlTipoClienteDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoTipoCliente() {
		editar=false;
		setCodigo(null);
		setDescripcion(null);
		setEstado("A");
		setTipo(null);
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgTipoClienteWV').show();");
	
	}
	

	public void mostrarDialogoTipoClienteEditar() {
		if(cmlTipoClienteSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlTipoClienteSeleccionado.getId().getCodigo());
			setDescripcion(cmlTipoClienteSeleccionado.getDescripcion());
			setEstado(cmlTipoClienteSeleccionado.getEstado());
			setTipo(cmlTipoClienteSeleccionado.getTipo());
			setValor(cmlTipoClienteSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgTipoClienteWV').show();");
		}
		
	}

	public void guardarTipoCliente() {
		try {
			
			CmlTipoCliente cmlTipoClienteEditar;
			if(editar) {
				cmlTipoClienteSeleccionado.setDescripcion(descripcion);
				cmlTipoClienteSeleccionado.setEstado(estado);
				cmlTipoClienteSeleccionado.setTipo(tipo);
				cmlTipoClienteSeleccionado.setValor(valor);
				cmlTipoClienteEditar=cmlTipoClienteSeleccionado;
				
				cmlTipoClienteDaoLocal.edit(cmlTipoClienteEditar);
			}else {
				CmlTipoClientePK id = new CmlTipoClientePK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlTipoClienteEditar=new CmlTipoCliente();
				cmlTipoClienteEditar.setDescripcion(descripcion);
				cmlTipoClienteEditar.setEstado(estado);
				cmlTipoClienteEditar.setTipo(tipo);
				cmlTipoClienteEditar.setValor(valor);
				cmlTipoClienteEditar.setId(id);
				cmlTipoClienteDaoLocal.create(cmlTipoClienteEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaTipoCliente(cmlTipoClienteDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgTipoClienteWV').hide();");
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

	
	public List<CmlTipoCliente> getListaTipoCliente() {
		return listaTipoCliente;
	}

	public void setListaTipoCliente(List<CmlTipoCliente> listaTipoCliente) {
		this.listaTipoCliente = listaTipoCliente;
	}

	public CmlTipoCliente getCmlTipoClienteSeleccionado() {
		return cmlTipoClienteSeleccionado;
	}

	public void setCmlTipoClienteSeleccionado(CmlTipoCliente cmlTipoClienteSeleccionado) {
		this.cmlTipoClienteSeleccionado = cmlTipoClienteSeleccionado;
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

