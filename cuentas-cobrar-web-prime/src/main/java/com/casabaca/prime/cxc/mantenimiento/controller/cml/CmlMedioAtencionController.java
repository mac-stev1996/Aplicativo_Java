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
import com.casabaca.cxc.cml.dao.CmlMedioAtencionDaoLocal;
import com.casabaca.cxc.cml.model.CmlMedioAtencion;
import com.casabaca.cxc.cml.model.CmlMedioAtencionPK;
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
@ManagedBean(name = "cmlMedioAtencionController")
public class CmlMedioAtencionController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlMedioAtencionDaoLocal cmlMedioAtencionDaoLocal;

	private String noCia;
	private List<CmlMedioAtencion> listaMedioAtencion;
	private CmlMedioAtencion cmlMedioAtencionSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;

	private boolean editar;


	public CmlMedioAtencionController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlMedioAtencionDaoLocal = (CmlMedioAtencionDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlMedioAtencionDaoBean");
			this.setListaMedioAtencion(cmlMedioAtencionDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoMedioAtencion() {
		editar=false;
		setCodigo(null);
		setDescripcion(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgMedioAtencionWV').show();");
	
	}
	

	public void mostrarDialogoMedioAtencionEditar() {
		if(cmlMedioAtencionSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlMedioAtencionSeleccionado.getId().getCodigo());
			setDescripcion(cmlMedioAtencionSeleccionado.getDescripcion());
			setEstado(cmlMedioAtencionSeleccionado.getEstado());
			setValor(cmlMedioAtencionSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgMedioAtencionWV').show();");
		}
		
	}

	public void guardarMedioAtencion() {
		try {
			
			CmlMedioAtencion cmlMedioAtencionEditar;
			if(editar) {
				cmlMedioAtencionSeleccionado.setDescripcion(descripcion);
				cmlMedioAtencionSeleccionado.setEstado(estado);
				cmlMedioAtencionSeleccionado.setValor(valor);
				cmlMedioAtencionEditar=cmlMedioAtencionSeleccionado;
				
				cmlMedioAtencionDaoLocal.edit(cmlMedioAtencionEditar);
			}else {
				CmlMedioAtencionPK id = new CmlMedioAtencionPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlMedioAtencionEditar=new CmlMedioAtencion();
				cmlMedioAtencionEditar.setDescripcion(descripcion);
				cmlMedioAtencionEditar.setEstado(estado);
				cmlMedioAtencionEditar.setValor(valor);
				cmlMedioAtencionEditar.setId(id);
				cmlMedioAtencionDaoLocal.create(cmlMedioAtencionEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaMedioAtencion(cmlMedioAtencionDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgMedioAtencionWV').hide();");
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

	
	public List<CmlMedioAtencion> getListaMedioAtencion() {
		return listaMedioAtencion;
	}

	public void setListaMedioAtencion(List<CmlMedioAtencion> listaMedioAtencion) {
		this.listaMedioAtencion = listaMedioAtencion;
	}

	public CmlMedioAtencion getCmlMedioAtencionSeleccionado() {
		return cmlMedioAtencionSeleccionado;
	}

	public void setCmlMedioAtencionSeleccionado(CmlMedioAtencion cmlMedioAtencionSeleccionado) {
		this.cmlMedioAtencionSeleccionado = cmlMedioAtencionSeleccionado;
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

