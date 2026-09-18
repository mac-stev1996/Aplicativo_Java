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
import com.casabaca.cxc.cml.dao.CmlPaisDaoLocal;
import com.casabaca.cxc.cml.model.CmlPais;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.mantenimiento.controller.ControlOficialCumplimientoController;

/**
 * @author Diego Ramirez
 *
 */
@ViewScoped
@ManagedBean(name = "cmlPaisController")
public class CmlPaisController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlPaisDaoLocal cmlPaisDaoLocal;

	private String noCia;
	private List<CmlPais> listaPais;
	private CmlPais cmlPaisSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;



	public CmlPaisController() {
		this.noCia = getCompania().getNoCia();
		try {
			cmlPaisDaoLocal = (CmlPaisDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlPaisDaoBean");
			this.setListaPais(cmlPaisDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}


	public void mostrarDialogoPaisEditar() {
		if(cmlPaisSeleccionado==null) {
			warn("Seleccione un registro");
		}else {
			setCodigo(cmlPaisSeleccionado.getId().getCodigo());
			setDescripcion(cmlPaisSeleccionado.getDescripcion());
			setEstado(cmlPaisSeleccionado.getEstado());
			setValor(cmlPaisSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgPaisWV').show();");
		}
		
	}

	public void guardarPais() {
		try {
			CmlPais cmlPaisEditar;

			cmlPaisSeleccionado.setEstado(estado);
			cmlPaisSeleccionado.setValor(valor);
			cmlPaisEditar=cmlPaisSeleccionado;
			cmlPaisDaoLocal.edit(cmlPaisEditar);
			
			info("se guardó exitosamente.");
			
			this.setListaPais(cmlPaisDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgPaisWV').hide();");
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

	
	public List<CmlPais> getListaPais() {
		return listaPais;
	}

	public void setListaPais(List<CmlPais> listaPais) {
		this.listaPais = listaPais;
	}

	public CmlPais getCmlPaisSeleccionado() {
		return cmlPaisSeleccionado;
	}

	public void setCmlPaisSeleccionado(CmlPais cmlPaisSeleccionado) {
		this.cmlPaisSeleccionado = cmlPaisSeleccionado;
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

