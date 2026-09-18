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
import com.casabaca.cxc.cml.dao.CmlListasPoDaoLocal;
import com.casabaca.cxc.cml.model.CmlListasPo;
import com.casabaca.cxc.cml.model.CmlListasPoPK;
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
@ManagedBean(name = "cmlListasPoController")
public class CmlListasPoController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlListasPoDaoLocal cmlListasPoDaoLocal;

	private String noCia;
	private List<CmlListasPo> listaListasPo;
	private CmlListasPo cmlListasPoSeleccionado;
	
	//Dialog
	private String tipo;
	private Integer valor;

	private boolean editar;


	public CmlListasPoController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlListasPoDaoLocal = (CmlListasPoDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlListasPoDaoBean");
			this.setListaListasPo(cmlListasPoDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}



	public void mostrarDialogoListasPoEditar() {
		if(cmlListasPoSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setTipo(cmlListasPoSeleccionado.getId().getTipo());
			setValor(cmlListasPoSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgListasPoWV').show();");
		}
		
	}

	public void guardarListasPo() {
		try {
			
			CmlListasPo cmlListasPoEditar;
			if(editar) {
				cmlListasPoSeleccionado.setValor(valor);
				cmlListasPoEditar=cmlListasPoSeleccionado;
				
				cmlListasPoDaoLocal.edit(cmlListasPoEditar);
			}else {
				CmlListasPoPK id = new CmlListasPoPK();
				id.setTipo(tipo);
				id.setNoCia(noCia);
				
				cmlListasPoEditar=new CmlListasPo();
				cmlListasPoEditar.setValor(valor);
				cmlListasPoEditar.setId(id);
				cmlListasPoDaoLocal.create(cmlListasPoEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaListasPo(cmlListasPoDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgListasPoWV').hide();");
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

	
	public List<CmlListasPo> getListaListasPo() {
		return listaListasPo;
	}

	public void setListaListasPo(List<CmlListasPo> listaListasPo) {
		this.listaListasPo = listaListasPo;
	}

	public CmlListasPo getCmlListasPoSeleccionado() {
		return cmlListasPoSeleccionado;
	}

	public void setCmlListasPoSeleccionado(CmlListasPo cmlListasPoSeleccionado) {
		this.cmlListasPoSeleccionado = cmlListasPoSeleccionado;
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

