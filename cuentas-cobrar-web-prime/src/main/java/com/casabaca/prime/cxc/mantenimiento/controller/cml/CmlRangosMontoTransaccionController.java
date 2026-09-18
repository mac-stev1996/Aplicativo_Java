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
import com.casabaca.cxc.cml.dao.CmlRangosMontoTransaccionDaoLocal;
import com.casabaca.cxc.cml.model.CmlRangosMontoTransaccion;
import com.casabaca.cxc.cml.model.CmlRangosMontoTransaccionPK;
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
@ManagedBean(name = "cmlRangosMontoTransaccionController")
public class CmlRangosMontoTransaccionController extends CommonController implements Serializable {

	
	private static final Logger logger = Logger.getLogger(ControlOficialCumplimientoController.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -6208489277907124648L;

	private CmlRangosMontoTransaccionDaoLocal cmlRangosMontoTransaccionDaoLocal;

	private String noCia;
	private List<CmlRangosMontoTransaccion> listaRangosMontoTransaccion;
	private CmlRangosMontoTransaccion cmlRangosMontoTransaccionSeleccionado;
	
	//Dialog
	
	private String codigo;
	private String estado;
	private BigDecimal desde;
	private BigDecimal hasta;
	private Integer valor;


	private boolean editar;


	public CmlRangosMontoTransaccionController() {
		editar=false;
		this.noCia = getCompania().getNoCia();
		try {
			cmlRangosMontoTransaccionDaoLocal = (CmlRangosMontoTransaccionDaoLocal) ServiceLocator.getService("java:global/cuentas-cobrar-ejb/CmlRangosMontoTransaccionDaoBean");
			this.setListaRangosMontoTransaccion(cmlRangosMontoTransaccionDaoLocal.listar(noCia));
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		
		
	}

	
	public void mostrarDialogoRangosMontoTransaccion() {
		editar=false;
		setCodigo(null);
		setDesde(null);
		setHasta(null);
		setEstado("A");
		setValor(null);
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgRangosMontoTransaccionWV').show();");
	
	}
	

	public void mostrarDialogoRangosMontoTransaccionEditar() {
		if(cmlRangosMontoTransaccionSeleccionado==null) {
			warn("Seleccione un registro");
			editar=false;
		}else {
			editar=true;
			setCodigo(cmlRangosMontoTransaccionSeleccionado.getId().getCodigo());
			setDesde(cmlRangosMontoTransaccionSeleccionado.getDesde());
			setHasta(cmlRangosMontoTransaccionSeleccionado.getHasta());
			setEstado(cmlRangosMontoTransaccionSeleccionado.getEstado());
			setValor(cmlRangosMontoTransaccionSeleccionado.getValor());
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgRangosMontoTransaccionWV').show();");
		}
		
	}

	public void guardarRangosMontoTransaccion() {
		try {
			
			CmlRangosMontoTransaccion cmlRangosMontoTransaccionEditar;
			if(editar) {
				cmlRangosMontoTransaccionSeleccionado.setValor(valor);
				cmlRangosMontoTransaccionSeleccionado.setDesde(desde);
				cmlRangosMontoTransaccionSeleccionado.setHasta(hasta);
				cmlRangosMontoTransaccionSeleccionado.setEstado(estado);
				cmlRangosMontoTransaccionEditar=cmlRangosMontoTransaccionSeleccionado;
				
				cmlRangosMontoTransaccionDaoLocal.edit(cmlRangosMontoTransaccionEditar);
			}else {
				CmlRangosMontoTransaccionPK id = new CmlRangosMontoTransaccionPK();
				id.setCodigo(codigo);
				id.setNoCia(noCia);
				
				cmlRangosMontoTransaccionEditar=new CmlRangosMontoTransaccion();
				cmlRangosMontoTransaccionEditar.setDesde(desde);
				cmlRangosMontoTransaccionEditar.setHasta(hasta);
				cmlRangosMontoTransaccionEditar.setEstado(estado);
				cmlRangosMontoTransaccionEditar.setValor(valor);
				cmlRangosMontoTransaccionEditar.setId(id);
				cmlRangosMontoTransaccionDaoLocal.create(cmlRangosMontoTransaccionEditar);
			}
			info("se guardó exitosamente.");
			
			this.setListaRangosMontoTransaccion(cmlRangosMontoTransaccionDaoLocal.listar(noCia));
			
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dlgRangosMontoTransaccionWV').hide();");
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

	
	public List<CmlRangosMontoTransaccion> getListaRangosMontoTransaccion() {
		return listaRangosMontoTransaccion;
	}

	public void setListaRangosMontoTransaccion(List<CmlRangosMontoTransaccion> listaRangosMontoTransaccion) {
		this.listaRangosMontoTransaccion = listaRangosMontoTransaccion;
	}

	public CmlRangosMontoTransaccion getCmlRangosMontoTransaccionSeleccionado() {
		return cmlRangosMontoTransaccionSeleccionado;
	}

	public void setCmlRangosMontoTransaccionSeleccionado(CmlRangosMontoTransaccion cmlRangosMontoTransaccionSeleccionado) {
		this.cmlRangosMontoTransaccionSeleccionado = cmlRangosMontoTransaccionSeleccionado;
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

