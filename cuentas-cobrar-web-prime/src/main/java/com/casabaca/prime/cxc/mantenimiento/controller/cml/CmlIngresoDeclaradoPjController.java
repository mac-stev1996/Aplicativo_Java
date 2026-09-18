/**
 * 
 */
package com.casabaca.prime.cxc.mantenimiento.controller.cml;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.cml.dao.CmlIngresoDeclaradoPjDaoLocal;
import com.casabaca.cxc.cml.model.CmlIngresoDeclaradoPj;
import com.casabaca.cxc.cml.model.CmlIngresoDeclaradoPjPK;
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
@ManagedBean(name = "cmlIngresoDeclaradoPjController")
public class CmlIngresoDeclaradoPjController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlIngresoDeclaradoPjDaoLocal cmlIngresoDeclaradoPjDaoLocal;

	private String noCia;
	private List<CmlIngresoDeclaradoPj> listaIngresoDeclaradoPj;
	private CmlIngresoDeclaradoPj cmlIngresoDeclaradoPjSeleccionado;
	
	//Dialog
	
	private String codigo;
	private String estado;
	private BigDecimal desde;
	private BigDecimal hasta;
	private Integer valor;


	private boolean editar;


	public CmlIngresoDeclaradoPjController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlIngresoDeclaradoPjDaoLocal = (CmlIngresoDeclaradoPjDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlIngresoDeclaradoPjDaoBean");
			this.setListaIngresoDeclaradoPj(cmlIngresoDeclaradoPjDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoIngresoDeclaradoPj() {
		editar=false;
		setCodigo(null);
		setDesde(null);
		setHasta(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgIngresoDeclaradoPjWV').show();");
	
	}
	

	public void mostrarDialogoIngresoDeclaradoPjEditar() {
		if(cmlIngresoDeclaradoPjSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlIngresoDeclaradoPjSeleccionado.getId().getCodigo());
			setDesde(cmlIngresoDeclaradoPjSeleccionado.getDesde());
			setHasta(cmlIngresoDeclaradoPjSeleccionado.getHasta());
			setEstado(cmlIngresoDeclaradoPjSeleccionado.getEstado());
			setValor(cmlIngresoDeclaradoPjSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgIngresoDeclaradoPjWV').show();");
		}
		
	}

	public void guardarIngresoDeclaradoPj() {
		try {
			
			CmlIngresoDeclaradoPj cmlIngresoDeclaradoPjEditar;
			if(editar) {
				cmlIngresoDeclaradoPjSeleccionado.setValor(valor);
				cmlIngresoDeclaradoPjSeleccionado.setDesde(desde);
				cmlIngresoDeclaradoPjSeleccionado.setHasta(hasta);
				cmlIngresoDeclaradoPjSeleccionado.setEstado(estado);
				cmlIngresoDeclaradoPjEditar=cmlIngresoDeclaradoPjSeleccionado;
				
				cmlIngresoDeclaradoPjDaoLocal.edit(cmlIngresoDeclaradoPjEditar);
			}else {
				CmlIngresoDeclaradoPjPK id = new CmlIngresoDeclaradoPjPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlIngresoDeclaradoPjEditar=new CmlIngresoDeclaradoPj();
				cmlIngresoDeclaradoPjEditar.setDesde(desde);
				cmlIngresoDeclaradoPjEditar.setHasta(hasta);
				cmlIngresoDeclaradoPjEditar.setEstado(estado);
				cmlIngresoDeclaradoPjEditar.setValor(valor);
				cmlIngresoDeclaradoPjEditar.setId(id);
				cmlIngresoDeclaradoPjDaoLocal.create(cmlIngresoDeclaradoPjEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaIngresoDeclaradoPj(cmlIngresoDeclaradoPjDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgIngresoDeclaradoPjWV').hide();");
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

	
	public List<CmlIngresoDeclaradoPj> getListaIngresoDeclaradoPj() {
		return listaIngresoDeclaradoPj;
	}

	public void setListaIngresoDeclaradoPj(List<CmlIngresoDeclaradoPj> listaIngresoDeclaradoPj) {
		this.listaIngresoDeclaradoPj = listaIngresoDeclaradoPj;
	}

	public CmlIngresoDeclaradoPj getCmlIngresoDeclaradoPjSeleccionado() {
		return cmlIngresoDeclaradoPjSeleccionado;
	}

	public void setCmlIngresoDeclaradoPjSeleccionado(CmlIngresoDeclaradoPj cmlIngresoDeclaradoPjSeleccionado) {
		this.cmlIngresoDeclaradoPjSeleccionado = cmlIngresoDeclaradoPjSeleccionado;
	}


	public String getCodigo() {
		return codigo;
	}


	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}


	public String getEstado() {
		return estado;
	}


	public void setEstado(String estado) {
		this.estado = estado;
	}


	public BigDecimal getDesde() {
		return desde;
	}


	public void setDesde(BigDecimal desde) {
		this.desde = desde;
	}


	public BigDecimal getHasta() {
		return hasta;
	}


	public void setHasta(BigDecimal hasta) {
		this.hasta = hasta;
	}


	public Integer getValor() {
		return valor;
	}


	public void setValor(Integer valor) {
		this.valor = valor;
	}


	
}

