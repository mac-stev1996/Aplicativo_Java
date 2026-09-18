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
import com.casabaca.cxc.cml.dao.CmlProvinciaDaoLocal;
import com.casabaca.cxc.cml.model.CmlProvincia;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.mantenimiento.controller.ControlOficialCumplimientoController;

/**
 * @author Diego Ramirez
 *
 */
@ViewScoped
@ManagedBean(name = "cmlProvinciaController")
public class CmlProvinciaController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlProvinciaDaoLocal cmlProvinciaDaoLocal;

	private String noCia;
	private List<CmlProvincia> listaProvincia;
	private CmlProvincia cmlProvinciaSeleccionado;
	
	//Dialog
	private String codigo;
	private String descripcion;
	private String estado;
	private Integer valor;

	public CmlProvinciaController() {
		this.noCia = getCompania().getNoCia();
		try {
			cmlProvinciaDaoLocal = (CmlProvinciaDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlProvinciaDaoBean");
			this.setListaProvincia(cmlProvinciaDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}


	public void mostrarDialogoProvinciaEditar() {
		if(cmlProvinciaSeleccionado==null) {
			warn("Seleccione un registro");
		}else {
			setCodigo(cmlProvinciaSeleccionado.getId().getCodigo());
			setDescripcion(cmlProvinciaSeleccionado.getDescripcion());
			setEstado(cmlProvinciaSeleccionado.getEstado());
			setValor(cmlProvinciaSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgProvinciaWV').show();");
		}
		
	}

	public void guardarProvincia() {
		try {
			
			CmlProvincia cmlProvinciaEditar;
			cmlProvinciaSeleccionado.setEstado(estado);
			cmlProvinciaSeleccionado.setValor(valor);
			cmlProvinciaEditar=cmlProvinciaSeleccionado;
			
			cmlProvinciaDaoLocal.edit(cmlProvinciaEditar);

			info("se guardó exitosamente.");
			
			this.setListaProvincia(cmlProvinciaDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgProvinciaWV').hide();");
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

	
	public List<CmlProvincia> getListaProvincia() {
		return listaProvincia;
	}

	public void setListaProvincia(List<CmlProvincia> listaProvincia) {
		this.listaProvincia = listaProvincia;
	}

	public CmlProvincia getCmlProvinciaSeleccionado() {
		return cmlProvinciaSeleccionado;
	}

	public void setCmlProvinciaSeleccionado(CmlProvincia cmlProvinciaSeleccionado) {
		this.cmlProvinciaSeleccionado = cmlProvinciaSeleccionado;
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

