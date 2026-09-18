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
import com.casabaca.cxc.cml.dao.CmlTitularCuentaDaoLocal;
import com.casabaca.cxc.cml.model.CmlTitularCuenta;
import com.casabaca.cxc.cml.model.CmlTitularCuentaPK;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.mantenimiento.controller.ControlOficialCumplimientoController;

/**
 * @author Diego Ramirez
 */
@ViewScoped
@ManagedBean(name = "cmlTitularCuentaController")
public class CmlTitularCuentaController extends CommonController implements Serializable {

	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlTitularCuentaDaoLocal cmlTitularCuentaDaoLocal;

	private String noCia;
	private List<CmlTitularCuenta> listaTitularCuenta;
	private CmlTitularCuenta cmlTitularCuentaSeleccionado;

	// Dialog
	private String tipo;
	private Integer valor;

	private boolean editar;

	public CmlTitularCuentaController() {
		editar = false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlTitularCuentaDaoLocal = (CmlTitularCuentaDaoLocal) ServiceLocator
					.getService("java:global/cuentas-cobrar-ejb/CmlTitularCuentaDaoBean");
			this.setListaTitularCuenta(cmlTitularCuentaDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}

	}

	public void mostrarDialogoTitularCuentaEditar() {
		if (cmlTitularCuentaSeleccionado == null) {
			warn("Seleccione un registro");
			editar = false;
		} else {
			editar = true;
			setTipo(cmlTitularCuentaSeleccionado.getId().getTipo());
			setValor(cmlTitularCuentaSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgTitularCuentaWV').show();");
		}

	}

	public void guardarTitularCuenta() {
		try {

			CmlTitularCuenta cmlTitularCuentaEditar;
			if (editar) {
				cmlTitularCuentaSeleccionado.setValor(valor);
				cmlTitularCuentaEditar = cmlTitularCuentaSeleccionado;

				cmlTitularCuentaDaoLocal.edit(cmlTitularCuentaEditar);
			} else {
				CmlTitularCuentaPK id = new CmlTitularCuentaPK();
				id.setTipo(tipo);
				id.setNoCia(noCia);

				cmlTitularCuentaEditar = new CmlTitularCuenta();
				cmlTitularCuentaEditar.setValor(valor);
				cmlTitularCuentaEditar.setId(id);
				cmlTitularCuentaDaoLocal.create(cmlTitularCuentaEditar);
			}
			info("se guardó exitosamente.");

			this.setListaTitularCuenta(cmlTitularCuentaDaoLocal.listar(noCia));

			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgTitularCuentaWV').hide();");
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

	public List<CmlTitularCuenta> getListaTitularCuenta() {
		return listaTitularCuenta;
	}

	public void setListaTitularCuenta(List<CmlTitularCuenta> listaTitularCuenta) {
		this.listaTitularCuenta = listaTitularCuenta;
	}

	public CmlTitularCuenta getCmlTitularCuentaSeleccionado() {
		return cmlTitularCuentaSeleccionado;
	}

	public void setCmlTitularCuentaSeleccionado(CmlTitularCuenta cmlTitularCuentaSeleccionado) {
		this.cmlTitularCuentaSeleccionado = cmlTitularCuentaSeleccionado;
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
