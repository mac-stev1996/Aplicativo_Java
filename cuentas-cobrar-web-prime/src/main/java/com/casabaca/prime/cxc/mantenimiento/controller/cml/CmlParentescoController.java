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
import com.casabaca.cxc.cml.dao.CmlParentescoDaoLocal;
import com.casabaca.cxc.cml.model.CmlParentesco;
import com.casabaca.cxc.cml.model.CmlParentescoPK;
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
@ManagedBean(name = "cmlParentescoController")
public class CmlParentescoController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlParentescoDaoLocal cmlParentescoDaoLocal;

	private String noCia;
	private List<CmlParentesco> listaParentesco;
	private CmlParentesco cmlParentescoSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;

	private boolean editar;


	public CmlParentescoController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlParentescoDaoLocal = (CmlParentescoDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlParentescoDaoBean");
			this.setListaParentesco(cmlParentescoDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoParentesco() {
		editar=false;
		setCodigo(null);
		setDescripcion(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgParentescoWV').show();");
	
	}
	

	public void mostrarDialogoParentescoEditar() {
		if(cmlParentescoSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlParentescoSeleccionado.getId().getCodigo());
			setDescripcion(cmlParentescoSeleccionado.getDescripcion());
			setEstado(cmlParentescoSeleccionado.getEstado());
			setValor(cmlParentescoSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgParentescoWV').show();");
		}
		
	}

	public void guardarParentesco() {
		try {
			
			CmlParentesco cmlParentescoEditar;
			if(editar) {
				cmlParentescoSeleccionado.setDescripcion(descripcion);
				cmlParentescoSeleccionado.setEstado(estado);
				cmlParentescoSeleccionado.setValor(valor);
				cmlParentescoEditar=cmlParentescoSeleccionado;
				
				cmlParentescoDaoLocal.edit(cmlParentescoEditar);
			}else {
				CmlParentescoPK id = new CmlParentescoPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlParentescoEditar=new CmlParentesco();
				cmlParentescoEditar.setDescripcion(descripcion);
				cmlParentescoEditar.setEstado(estado);
				cmlParentescoEditar.setValor(valor);
				cmlParentescoEditar.setId(id);
				cmlParentescoDaoLocal.create(cmlParentescoEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaParentesco(cmlParentescoDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgParentescoWV').hide();");
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

	
	public List<CmlParentesco> getListaParentesco() {
		return listaParentesco;
	}

	public void setListaParentesco(List<CmlParentesco> listaParentesco) {
		this.listaParentesco = listaParentesco;
	}

	public CmlParentesco getCmlParentescoSeleccionado() {
		return cmlParentescoSeleccionado;
	}

	public void setCmlParentescoSeleccionado(CmlParentesco cmlParentescoSeleccionado) {
		this.cmlParentescoSeleccionado = cmlParentescoSeleccionado;
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

