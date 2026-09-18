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
import com.casabaca.cxc.cml.dao.CmlOrigenFondoDaoLocal;
import com.casabaca.cxc.cml.model.CmlOrigenFondo;
import com.casabaca.cxc.cml.model.CmlOrigenFondoPK;
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
@ManagedBean(name = "cmlOrigenFondoController")
public class CmlOrigenFondoController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlOrigenFondoDaoLocal cmlOrigenFondoDaoLocal;

	private String noCia;
	private List<CmlOrigenFondo> listaOrigenFondo;
	private CmlOrigenFondo cmlOrigenFondoSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String abreviado;
	private String estado;
	private Integer valor;

	private boolean editar;


	public CmlOrigenFondoController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlOrigenFondoDaoLocal = (CmlOrigenFondoDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlOrigenFondoDaoBean");
			this.setListaOrigenFondo(cmlOrigenFondoDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoOrigenFondo() {
		editar=false;
		setCodigo(null);
		setDescripcion(null);
		setAbreviado(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgOrigenFondoWV').show();");
	
	}
	

	public void mostrarDialogoOrigenFondoEditar() {
		if(cmlOrigenFondoSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlOrigenFondoSeleccionado.getId().getCodigo());
			setDescripcion(cmlOrigenFondoSeleccionado.getDescripcion());
			setAbreviado(cmlOrigenFondoSeleccionado.getAbreviado());
			setEstado(cmlOrigenFondoSeleccionado.getEstado());
			setValor(cmlOrigenFondoSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgOrigenFondoWV').show();");
		}
		
	}

	public void guardarOrigenFondo() {
		try {
			
			CmlOrigenFondo cmlOrigenFondoEditar;
			if(editar) {
				cmlOrigenFondoSeleccionado.setDescripcion(descripcion);
				cmlOrigenFondoSeleccionado.setAbreviado(abreviado);
				cmlOrigenFondoSeleccionado.setEstado(estado);
				cmlOrigenFondoSeleccionado.setValor(valor);
				cmlOrigenFondoEditar=cmlOrigenFondoSeleccionado;
				
				cmlOrigenFondoDaoLocal.edit(cmlOrigenFondoEditar);
			}else {
				CmlOrigenFondoPK id = new CmlOrigenFondoPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlOrigenFondoEditar=new CmlOrigenFondo();
				cmlOrigenFondoEditar.setDescripcion(descripcion);
				cmlOrigenFondoEditar.setAbreviado(abreviado);
				cmlOrigenFondoEditar.setEstado(estado);
				cmlOrigenFondoEditar.setValor(valor);
				cmlOrigenFondoEditar.setId(id);
				cmlOrigenFondoDaoLocal.create(cmlOrigenFondoEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaOrigenFondo(cmlOrigenFondoDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgOrigenFondoWV').hide();");
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

	
	public List<CmlOrigenFondo> getListaOrigenFondo() {
		return listaOrigenFondo;
	}

	public void setListaOrigenFondo(List<CmlOrigenFondo> listaOrigenFondo) {
		this.listaOrigenFondo = listaOrigenFondo;
	}

	public CmlOrigenFondo getCmlOrigenFondoSeleccionado() {
		return cmlOrigenFondoSeleccionado;
	}

	public void setCmlOrigenFondoSeleccionado(CmlOrigenFondo cmlOrigenFondoSeleccionado) {
		this.cmlOrigenFondoSeleccionado = cmlOrigenFondoSeleccionado;
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


	public String getAbreviado() {
		return abreviado;
	}


	public void setAbreviado(String abreviado) {
		this.abreviado = abreviado;
	}


	
}

