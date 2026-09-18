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
import com.casabaca.cxc.cml.dao.CmlRangosEdadDaoLocal;
import com.casabaca.cxc.cml.model.CmlRangosEdad;
import com.casabaca.cxc.cml.model.CmlRangosEdadPK;
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
@ManagedBean(name = "cmlRangosEdadController")
public class CmlRangosEdadController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlRangosEdadDaoLocal cmlRangosEdadDaoLocal;

	private String noCia;
	private List<CmlRangosEdad> listaRangosEdad;
	private CmlRangosEdad cmlRangosEdadSeleccionado;
	
	//Dialog
	
	private String codigo;
	private String estado;
	private BigDecimal desde;
	private BigDecimal hasta;
	private Integer valor;


	private boolean editar;


	public CmlRangosEdadController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlRangosEdadDaoLocal = (CmlRangosEdadDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlRangosEdadDaoBean");
			this.setListaRangosEdad(cmlRangosEdadDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoRangosEdad() {
		editar=false;
		setCodigo(null);
		setDesde(null);
		setHasta(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgRangosEdadWV').show();");
	
	}
	

	public void mostrarDialogoRangosEdadEditar() {
		if(cmlRangosEdadSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlRangosEdadSeleccionado.getId().getCodigo());
			setDesde(cmlRangosEdadSeleccionado.getDesde());
			setHasta(cmlRangosEdadSeleccionado.getHasta());
			setEstado(cmlRangosEdadSeleccionado.getEstado());
			setValor(cmlRangosEdadSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgRangosEdadWV').show();");
		}
		
	}

	public void guardarRangosEdad() {
		try {
			
			CmlRangosEdad cmlRangosEdadEditar;
			if(editar) {
				cmlRangosEdadSeleccionado.setValor(valor);
				cmlRangosEdadSeleccionado.setDesde(desde);
				cmlRangosEdadSeleccionado.setHasta(hasta);
				cmlRangosEdadSeleccionado.setEstado(estado);
				cmlRangosEdadEditar=cmlRangosEdadSeleccionado;
				
				cmlRangosEdadDaoLocal.edit(cmlRangosEdadEditar);
			}else {
				CmlRangosEdadPK id = new CmlRangosEdadPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlRangosEdadEditar=new CmlRangosEdad();
				cmlRangosEdadEditar.setDesde(desde);
				cmlRangosEdadEditar.setHasta(hasta);
				cmlRangosEdadEditar.setEstado(estado);
				cmlRangosEdadEditar.setValor(valor);
				cmlRangosEdadEditar.setId(id);
				cmlRangosEdadDaoLocal.create(cmlRangosEdadEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaRangosEdad(cmlRangosEdadDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgRangosEdadWV').hide();");
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

	
	public List<CmlRangosEdad> getListaRangosEdad() {
		return listaRangosEdad;
	}

	public void setListaRangosEdad(List<CmlRangosEdad> listaRangosEdad) {
		this.listaRangosEdad = listaRangosEdad;
	}

	public CmlRangosEdad getCmlRangosEdadSeleccionado() {
		return cmlRangosEdadSeleccionado;
	}

	public void setCmlRangosEdadSeleccionado(CmlRangosEdad cmlRangosEdadSeleccionado) {
		this.cmlRangosEdadSeleccionado = cmlRangosEdadSeleccionado;
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

