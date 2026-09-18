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
import com.casabaca.cxc.cml.dao.CmlIngresoDeclaradoPnDaoLocal;
import com.casabaca.cxc.cml.model.CmlIngresoDeclaradoPn;
import com.casabaca.cxc.cml.model.CmlIngresoDeclaradoPnPK;
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
@ManagedBean(name = "cmlIngresoDeclaradoPnController")
public class CmlIngresoDeclaradoPnController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlIngresoDeclaradoPnDaoLocal cmlIngresoDeclaradoPnDaoLocal;

	private String noCia;
	private List<CmlIngresoDeclaradoPn> listaIngresoDeclaradoPn;
	private CmlIngresoDeclaradoPn cmlIngresoDeclaradoPnSeleccionado;
	
	//Dialog
	
	private String codigo;
	private String estado;
	private BigDecimal desde;
	private BigDecimal hasta;
	private Integer valor;


	private boolean editar;


	public CmlIngresoDeclaradoPnController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlIngresoDeclaradoPnDaoLocal = (CmlIngresoDeclaradoPnDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlIngresoDeclaradoPnDaoBean");
			this.setListaIngresoDeclaradoPn(cmlIngresoDeclaradoPnDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoIngresoDeclaradoPn() {
		editar=false;
		setCodigo(null);
		setDesde(null);
		setHasta(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgIngresoDeclaradoPnWV').show();");
	
	}
	

	public void mostrarDialogoIngresoDeclaradoPnEditar() {
		if(cmlIngresoDeclaradoPnSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlIngresoDeclaradoPnSeleccionado.getId().getCodigo());
			setDesde(cmlIngresoDeclaradoPnSeleccionado.getDesde());
			setHasta(cmlIngresoDeclaradoPnSeleccionado.getHasta());
			setEstado(cmlIngresoDeclaradoPnSeleccionado.getEstado());
			setValor(cmlIngresoDeclaradoPnSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgIngresoDeclaradoPnWV').show();");
		}
		
	}

	public void guardarIngresoDeclaradoPn() {
		try {
			
			CmlIngresoDeclaradoPn cmlIngresoDeclaradoPnEditar;
			if(editar) {
				cmlIngresoDeclaradoPnSeleccionado.setValor(valor);
				cmlIngresoDeclaradoPnSeleccionado.setDesde(desde);
				cmlIngresoDeclaradoPnSeleccionado.setHasta(hasta);
				cmlIngresoDeclaradoPnSeleccionado.setEstado(estado);
				cmlIngresoDeclaradoPnEditar=cmlIngresoDeclaradoPnSeleccionado;
				
				cmlIngresoDeclaradoPnDaoLocal.edit(cmlIngresoDeclaradoPnEditar);
			}else {
				CmlIngresoDeclaradoPnPK id = new CmlIngresoDeclaradoPnPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlIngresoDeclaradoPnEditar=new CmlIngresoDeclaradoPn();
				cmlIngresoDeclaradoPnEditar.setDesde(desde);
				cmlIngresoDeclaradoPnEditar.setHasta(hasta);
				cmlIngresoDeclaradoPnEditar.setEstado(estado);
				cmlIngresoDeclaradoPnEditar.setValor(valor);
				cmlIngresoDeclaradoPnEditar.setId(id);
				cmlIngresoDeclaradoPnDaoLocal.create(cmlIngresoDeclaradoPnEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaIngresoDeclaradoPn(cmlIngresoDeclaradoPnDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgIngresoDeclaradoPnWV').hide();");
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

	
	public List<CmlIngresoDeclaradoPn> getListaIngresoDeclaradoPn() {
		return listaIngresoDeclaradoPn;
	}

	public void setListaIngresoDeclaradoPn(List<CmlIngresoDeclaradoPn> listaIngresoDeclaradoPn) {
		this.listaIngresoDeclaradoPn = listaIngresoDeclaradoPn;
	}

	public CmlIngresoDeclaradoPn getCmlIngresoDeclaradoPnSeleccionado() {
		return cmlIngresoDeclaradoPnSeleccionado;
	}

	public void setCmlIngresoDeclaradoPnSeleccionado(CmlIngresoDeclaradoPn cmlIngresoDeclaradoPnSeleccionado) {
		this.cmlIngresoDeclaradoPnSeleccionado = cmlIngresoDeclaradoPnSeleccionado;
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

