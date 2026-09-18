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

import com.casabaca.common.CommonConstants;
import com.casabaca.cxc.cml.dao.CmlFactorRiesgoDaoLocal;
import com.casabaca.cxc.cml.dao.CmlTipoFactorRiesgoDaoLocal;
import com.casabaca.cxc.cml.model.CmlFactorRiesgo;
import com.casabaca.cxc.cml.model.CmlFactorRiesgoPK;
import com.casabaca.cxc.cml.model.CmlTipoFactorRiesgo;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.Compania;


/**
 * @author dv_ramirez
 *
 */
@ManagedBean(name = "cmlFactorRiesgoController")
@ViewScoped
public class CmlFactorRiesgoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 643800580113537058L;

	private static final Logger LOG = Logger.getLogger(CmlFactorRiesgoController.class);


	@EJB(lookup = "java:global/cuentas-cobrar-ejb/CmlFactorRiesgoDaoBean")
	private CmlFactorRiesgoDaoLocal cmlFactorRiesgoDaoLocal;
	
	@EJB(lookup = "java:global/cuentas-cobrar-ejb/CmlTipoFactorRiesgoDaoBean")
	private CmlTipoFactorRiesgoDaoLocal cmlTipoFactorRiesgoDaoLocal;
	
	private List<CmlFactorRiesgo> factorRiesgoLst;
	private List<CmlTipoFactorRiesgo> tipoLst;
	private CmlFactorRiesgo factorRiesgoSel;
	private Compania noCia;
	private Long codigoFRPadre;
	double sumaPonderacion;

	private Double ponderacionAnterior; 
	
	
	@PostConstruct
	public void init() {
		try {
			this.noCia=getCompania();
			String codigoStr = getRequestParameter("CODFR");
			if(codigoStr!=null) {
				codigoFRPadre=Long.valueOf(codigoStr);
			}else {
				codigoFRPadre=null;
			}
			listarFactoresRiesgo();
			tipoLst=cmlTipoFactorRiesgoDaoLocal.listarActivas(noCia.getNoCia());
		} catch (Exception e) {
			LOG.error("Error al cargar datos iniciales", e);
		}

	}


	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 27 jun. 2023]
	 * </p>
	 *
	 */
	private void listarFactoresRiesgo() {
		sumaPonderacion=0D;
		factorRiesgoLst=cmlFactorRiesgoDaoLocal.listarPorPadre(noCia.getNoCia(),codigoFRPadre);
		for (CmlFactorRiesgo cmlFactorRiesgo : factorRiesgoLst) {
			if(cmlFactorRiesgo.getEstado().compareTo("A")==0) {
				sumaPonderacion=sumaPonderacion+cmlFactorRiesgo.getPonderacion();
			}
			
		}
	}


	public void inactivarFactorRiesgo(CmlFactorRiesgo FactorRiesgo){
		try{
			FactorRiesgo.setEstado("I");
			cmlFactorRiesgoDaoLocal.edit(FactorRiesgo);
			listarFactoresRiesgo();
			addInfoMessage("INFO: ", "Se ha inactivado el  de factor riesgo correctamente");
		}catch( UpdateException d){
			LOG.error("Error al inactivar la  factor riesgo", d);
			addErrorMessage("ERROR: ", "Al inactivar la FactorRiesgo");
		}
	}
	

	
	public void editarFactorRiesgo(CmlFactorRiesgo FactorRiesgo){
		factorRiesgoSel = FactorRiesgo;
		ponderacionAnterior=FactorRiesgo.getPonderacion();
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('dlgNewFactor').show();");
	}
	
	
	public void verFactoresDescendientes(CmlFactorRiesgo factorRiesgo){
		
		try {
			redirect(
					"/cxc-web-prime/jsf/mantenimiento/cml/manFactorRiesgo.xhtml?noCia=" + noCia.getNoCia() + "&usuario="
							+ getUsuario().getUsuario() + "&tipoDoc=PC" + "&" + CommonConstants.REQUEST_PARAM_CENTRO
							+ "=" + getUsuarioCentroConectado().getUsuarioCentroPK().getCentro() + "&"
							+ CommonConstants.REQUEST_PARAM_BODEGA + "="
							+ getUsuarioCentroBodegaConectado().getUsuarioCentroBodegaPK().getBodega() + "&"
							+ CommonConstants.REQUEST_PARAM_LINEA + "=" + getLineaPorModulo()+ "&"
							+ "CODFR=" + factorRiesgo.getId().getCodigo());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	public void guardar(){
		RequestContext context = RequestContext.getCurrentInstance();
		try{ 
			factorRiesgoSel.setPadreId(codigoFRPadre);
			if(factorRiesgoSel.getId().getCodigo()== null) {
				if(sumaPonderacion+factorRiesgoSel.getPonderacion()>100) {
					addWarnMessage("Advertencia:", "La suma de las ponderaciones no puede ser superior a 100");
					return;
				}
				Long codigo = cmlFactorRiesgoDaoLocal.generarSecuencia(noCia.getNoCia());
				factorRiesgoSel.getId().setCodigo(codigo);
				cmlFactorRiesgoDaoLocal.create(factorRiesgoSel);
			}else {
				if(sumaPonderacion-ponderacionAnterior+factorRiesgoSel.getPonderacion()>100) {
					addWarnMessage("Advertencia:", "La suma de las ponderaciones no puede ser superior a 100");
					return;
				}
				cmlFactorRiesgoDaoLocal.edit(factorRiesgoSel);
			}
			context.execute("PF('dlgNewFactor').hide();");
			addInfoMessage("INFO: ", "Se ha guardado el registro correctamente");
			listarFactoresRiesgo();
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

	
	
	
	public void nuevoFactorRiesgo(){
		factorRiesgoSel = new CmlFactorRiesgo();
		CmlFactorRiesgoPK id=new CmlFactorRiesgoPK();
		id.setNoCia(noCia.getNoCia());
		factorRiesgoSel.setId(id);
		factorRiesgoSel.setEstado("A");
		factorRiesgoSel.setPonderacion(100.00-sumaPonderacion);
	}

	
	
	
	protected void redirect(String pageRedirect) throws IOException{
		FacesContext.getCurrentInstance().getExternalContext().redirect(pageRedirect);
	}


	public List<CmlFactorRiesgo> getFactorRiesgoLst() {
		return factorRiesgoLst;
	}


	public void setFactorRiesgoLst(List<CmlFactorRiesgo> FactorRiesgoLst) {
		this.factorRiesgoLst = FactorRiesgoLst;
	}


	public CmlFactorRiesgo getFactorRiesgoSel() {
		return factorRiesgoSel;
	}


	public void setFactorRiesgoSel(CmlFactorRiesgo FactorRiesgoSel) {
		this.factorRiesgoSel = FactorRiesgoSel;
	}

	public Compania getNoCia() {
		return noCia;
	}


	public void setNoCia(Compania noCia) {
		this.noCia = noCia;
	}


	public List<CmlTipoFactorRiesgo> getTipoLst() {
		return tipoLst;
	}

	
	public void setTipoLst(List<CmlTipoFactorRiesgo> tipoLst) {
		this.tipoLst = tipoLst;
	}


	public Long getCodigoFRPadre() {
		return codigoFRPadre;
	}


	public void setCodigoFRPadre(Long codigoFRPadre) {
		this.codigoFRPadre = codigoFRPadre;
	}

	

}
