package com.casabaca.prime.cxc.mantenimiento.controller.cml;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.cxc.cml.dao.CmlTipoFactorRiesgoDaoLocal;
import com.casabaca.cxc.cml.model.CmlTipoFactorRiesgo;
import com.casabaca.cxc.cml.model.CmlTipoFactorRiesgoPK;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.Compania;


/**
 * @author dv_ramirez
 *
 */
@ManagedBean(name = "cmlTipoFactorRiesgoController")
@ViewScoped
public class CmlTipoFactorRiesgoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 643800580113537058L;

	private static final Logger LOG = Logger.getLogger(CmlTipoFactorRiesgoController.class);


	@EJB(lookup = "java:global/cuentas-cobrar-ejb/CmlTipoFactorRiesgoDaoBean")
	private CmlTipoFactorRiesgoDaoLocal cmlTipoFactorRiesgoDaoLocal;
	
	private List<CmlTipoFactorRiesgo> tipoFactorRiesgoLst;
	private CmlTipoFactorRiesgo tipoFactorRiesgoSel;
	private Compania noCia;
	Long codigoCon=0L;
	
	@PostConstruct
	public void init() {
		try {
			this.noCia=getCompania();
			tipoFactorRiesgoLst = cmlTipoFactorRiesgoDaoLocal.listar(noCia.getNoCia());
		} catch (Exception e) {
			LOG.error("Error al cargar datos iniciales", e);
		}

	}


	public void inactivarTipoFactorRiesgo(CmlTipoFactorRiesgo tipoFactorRiesgo){
		try{
			tipoFactorRiesgo.setEstado("I");
			cmlTipoFactorRiesgoDaoLocal.edit(tipoFactorRiesgo);
			tipoFactorRiesgoLst = cmlTipoFactorRiesgoDaoLocal.listar(noCia.getNoCia());
			addInfoMessage("INFO: ", "Se ha inactivado el tipo de factor riesgo correctamente");
		}catch( UpdateException d){
			LOG.error("Error al inactivar la tipo factor riesgo", d);
			addErrorMessage("ERROR: ", "Al inactivar la tipoFactorRiesgo");
		}
	}
	

	
	public void editarTipoFactorRiesgo(CmlTipoFactorRiesgo tipoFactorRiesgo){
		tipoFactorRiesgoSel = tipoFactorRiesgo;

		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgNewTipoFactorRiesgo').show();");
	}
	
	
	
	public void guardar(){
		RequestContext context = RequestContext.getCurrentInstance();
		try{ 
			if(tipoFactorRiesgoSel.getId().getCodigo()== null) {
				Long codigo = cmlTipoFactorRiesgoDaoLocal.generarSecuencia(noCia.getNoCia());
				tipoFactorRiesgoSel.getId().setCodigo(codigo);
				cmlTipoFactorRiesgoDaoLocal.create(tipoFactorRiesgoSel);
			}else {
				cmlTipoFactorRiesgoDaoLocal.edit(tipoFactorRiesgoSel);
			}
			context.execute("PF('dlgNewTipoFactorRiesgo').hide();");
			tipoFactorRiesgoLst = cmlTipoFactorRiesgoDaoLocal.listar(noCia.getNoCia());
			addInfoMessage("INFO: ", "Se ha guardado el registro correctamente");
		}catch( InsertException | UpdateException e){
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
			LOG.error(mensaje);
			addErrorMessage("ERROR: ", "Al guardar el registro");
		}
	}

	
	
	
	public void nuevoTipoFactorRiesgo(){
		tipoFactorRiesgoSel = new CmlTipoFactorRiesgo();
		CmlTipoFactorRiesgoPK id=new CmlTipoFactorRiesgoPK();
		id.setNoCia(noCia.getNoCia());
		tipoFactorRiesgoSel.setId(id);
		tipoFactorRiesgoSel.setEstado("A");
	}

	
	protected void redirect(String pageRedirect) throws IOException{
		FacesContext.getCurrentInstance().getExternalContext().redirect(pageRedirect);
	}


	public List<CmlTipoFactorRiesgo> getTipoFactorRiesgoLst() {
		return tipoFactorRiesgoLst;
	}


	public void setTipoFactorRiesgoLst(List<CmlTipoFactorRiesgo> tipoFactorRiesgoLst) {
		this.tipoFactorRiesgoLst = tipoFactorRiesgoLst;
	}


	public CmlTipoFactorRiesgo getTipoFactorRiesgoSel() {
		return tipoFactorRiesgoSel;
	}


	public void setTipoFactorRiesgoSel(CmlTipoFactorRiesgo tipoFactorRiesgoSel) {
		this.tipoFactorRiesgoSel = tipoFactorRiesgoSel;
	}

	public Compania getNoCia() {
		return noCia;
	}


	public void setNoCia(Compania noCia) {
		this.noCia = noCia;
	}


	public Long getCodigoCon() {
		return codigoCon;
	}


	public void setCodigoCon(Long codigoCon) {
		this.codigoCon = codigoCon;
	}
	
}
