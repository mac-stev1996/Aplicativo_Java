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
import com.casabaca.cxc.cml.dao.CmlMedioTransaccionalDaoLocal;
import com.casabaca.cxc.cml.model.CmlMedioTransaccional;
import com.casabaca.cxc.cml.model.CmlMedioTransaccionalPK;
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
@ManagedBean(name = "cmlMedioTransaccionalController")
public class CmlMedioTransaccionalController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlMedioTransaccionalDaoLocal cmlMedioTransaccionalDaoLocal;

	private String noCia;
	private List<CmlMedioTransaccional> listaMedioTransaccional;
	private CmlMedioTransaccional cmlMedioTransaccionalSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;

	private boolean editar;


	public CmlMedioTransaccionalController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlMedioTransaccionalDaoLocal = (CmlMedioTransaccionalDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlMedioTransaccionalDaoBean");
			this.setListaMedioTransaccional(cmlMedioTransaccionalDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoMedioTransaccional() {
		editar=false;
		setCodigo(null);
		setDescripcion(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgMedioTransaccionalWV').show();");
	
	}
	

	public void mostrarDialogoMedioTransaccionalEditar() {
		if(cmlMedioTransaccionalSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlMedioTransaccionalSeleccionado.getId().getCodigo());
			setDescripcion(cmlMedioTransaccionalSeleccionado.getDescripcion());
			setEstado(cmlMedioTransaccionalSeleccionado.getEstado());
			setValor(cmlMedioTransaccionalSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgMedioTransaccionalWV').show();");
		}
		
	}

	public void guardarMedioTransaccional() {
		try {
			
			CmlMedioTransaccional cmlMedioTransaccionalEditar;
			if(editar) {
				cmlMedioTransaccionalSeleccionado.setDescripcion(descripcion);
				cmlMedioTransaccionalSeleccionado.setEstado(estado);
				cmlMedioTransaccionalSeleccionado.setValor(valor);
				cmlMedioTransaccionalEditar=cmlMedioTransaccionalSeleccionado;
				
				cmlMedioTransaccionalDaoLocal.edit(cmlMedioTransaccionalEditar);
			}else {
				CmlMedioTransaccionalPK id = new CmlMedioTransaccionalPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlMedioTransaccionalEditar=new CmlMedioTransaccional();
				cmlMedioTransaccionalEditar.setDescripcion(descripcion);
				cmlMedioTransaccionalEditar.setEstado(estado);
				cmlMedioTransaccionalEditar.setValor(valor);
				cmlMedioTransaccionalEditar.setId(id);
				cmlMedioTransaccionalDaoLocal.create(cmlMedioTransaccionalEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaMedioTransaccional(cmlMedioTransaccionalDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgMedioTransaccionalWV').hide();");
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

	
	public List<CmlMedioTransaccional> getListaMedioTransaccional() {
		return listaMedioTransaccional;
	}

	public void setListaMedioTransaccional(List<CmlMedioTransaccional> listaMedioTransaccional) {
		this.listaMedioTransaccional = listaMedioTransaccional;
	}

	public CmlMedioTransaccional getCmlMedioTransaccionalSeleccionado() {
		return cmlMedioTransaccionalSeleccionado;
	}

	public void setCmlMedioTransaccionalSeleccionado(CmlMedioTransaccional cmlMedioTransaccionalSeleccionado) {
		this.cmlMedioTransaccionalSeleccionado = cmlMedioTransaccionalSeleccionado;
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

