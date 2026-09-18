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
import com.casabaca.cxc.cml.dao.CmlReputacionRepresentanteDaoLocal;
import com.casabaca.cxc.cml.model.CmlReputacionRepresentante;
import com.casabaca.cxc.cml.model.CmlReputacionRepresentantePK;
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
@ManagedBean(name = "cmlReputacionRepresentanteController")
public class CmlReputacionRepresentanteController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlReputacionRepresentanteDaoLocal cmlReputacionRepresentanteDaoLocal;

	private String noCia;
	private List<CmlReputacionRepresentante> listaReputacionRepresentante;
	private CmlReputacionRepresentante cmlReputacionRepresentanteSeleccionado;
	
	//Dialog
	private String tipo;
	private Integer valor;

	private boolean editar;


	public CmlReputacionRepresentanteController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlReputacionRepresentanteDaoLocal = (CmlReputacionRepresentanteDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlReputacionRepresentanteDaoBean");
			this.setListaReputacionRepresentante(cmlReputacionRepresentanteDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}



	public void mostrarDialogoReputacionRepresentanteEditar() {
		if(cmlReputacionRepresentanteSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setTipo(cmlReputacionRepresentanteSeleccionado.getId().getTipo());
			setValor(cmlReputacionRepresentanteSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgReputacionRepresentanteWV').show();");
		}
		
	}

	public void guardarReputacionRepresentante() {
		try {
			
			CmlReputacionRepresentante cmlReputacionRepresentanteEditar;
			if(editar) {
				cmlReputacionRepresentanteSeleccionado.setValor(valor);
				cmlReputacionRepresentanteEditar=cmlReputacionRepresentanteSeleccionado;
				
				cmlReputacionRepresentanteDaoLocal.edit(cmlReputacionRepresentanteEditar);
			}else {
				CmlReputacionRepresentantePK id = new CmlReputacionRepresentantePK();
				id.setTipo(tipo);
				id.setNoCia(noCia);
				
				cmlReputacionRepresentanteEditar=new CmlReputacionRepresentante();
				cmlReputacionRepresentanteEditar.setValor(valor);
				cmlReputacionRepresentanteEditar.setId(id);
				cmlReputacionRepresentanteDaoLocal.create(cmlReputacionRepresentanteEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaReputacionRepresentante(cmlReputacionRepresentanteDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgReputacionRepresentanteWV').hide();");
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

	
	public List<CmlReputacionRepresentante> getListaReputacionRepresentante() {
		return listaReputacionRepresentante;
	}

	public void setListaReputacionRepresentante(List<CmlReputacionRepresentante> listaReputacionRepresentante) {
		this.listaReputacionRepresentante = listaReputacionRepresentante;
	}

	public CmlReputacionRepresentante getCmlReputacionRepresentanteSeleccionado() {
		return cmlReputacionRepresentanteSeleccionado;
	}

	public void setCmlReputacionRepresentanteSeleccionado(CmlReputacionRepresentante cmlReputacionRepresentanteSeleccionado) {
		this.cmlReputacionRepresentanteSeleccionado = cmlReputacionRepresentanteSeleccionado;
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

