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
import com.casabaca.cxc.cml.dao.CmlPepDaoLocal;
import com.casabaca.cxc.cml.model.CmlPep;
import com.casabaca.cxc.cml.model.CmlPepPK;
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
@ManagedBean(name = "cmlPepController")
public class CmlPepController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlPepDaoLocal cmlPepDaoLocal;

	private String noCia;
	private List<CmlPep> listaPep;
	private CmlPep cmlPepSeleccionado;
	
	//Dialog
	private String tipo;
	private Integer valor;

	private boolean editar;


	public CmlPepController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlPepDaoLocal = (CmlPepDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlPepDaoBean");
			this.setListaPep(cmlPepDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}


	public void mostrarDialogoPepEditar() {
		if(cmlPepSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setTipo(cmlPepSeleccionado.getId().getTipo());
			setValor(cmlPepSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgPepWV').show();");
		}
		
	}

	public void guardarPep() {
		try {
			
			CmlPep cmlPepEditar;
			if(editar) {
				cmlPepSeleccionado.setValor(valor);
				cmlPepEditar=cmlPepSeleccionado;
				
				cmlPepDaoLocal.edit(cmlPepEditar);
			}else {
				CmlPepPK id = new CmlPepPK();
				id.setTipo(tipo);
				id.setNoCia(noCia);
				
				cmlPepEditar=new CmlPep();

				cmlPepEditar.setValor(valor);
				cmlPepEditar.setId(id);
				cmlPepDaoLocal.create(cmlPepEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaPep(cmlPepDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgPepWV').hide();");
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

	
	public List<CmlPep> getListaPep() {
		return listaPep;
	}

	public void setListaPep(List<CmlPep> listaPep) {
		this.listaPep = listaPep;
	}

	public CmlPep getCmlPepSeleccionado() {
		return cmlPepSeleccionado;
	}

	public void setCmlPepSeleccionado(CmlPep cmlPepSeleccionado) {
		this.cmlPepSeleccionado = cmlPepSeleccionado;
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

