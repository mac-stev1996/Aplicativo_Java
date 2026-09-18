package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.model.ParamDetPK;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.DeleteException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * Controlador para parametrizar paramDet
 * 
 * @author we_romero
 *
 */
@ViewScoped
@ManagedBean(name = "cxcParametrizacionDetalleController")
public class CxcParametrizacionDetalleController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetServiceLocal;

	private List<ParamDet> parametros;
	private ParamDet parametro;
	private static final String CODIGO_PARAM = "TFGRUP";
	private boolean esNuevo;
	
	@PostConstruct
	public void init() {
		esNuevo = false;
		parametros = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CODIGO_PARAM);
	}

	public void abrirItem() {
		parametro = new ParamDet(new ParamDetPK(getCompania().getNoCia(), CODIGO_PARAM, null));
		esNuevo = true;
		accionesDialog("DlgResult", true);
	}

	public void editarItem(ParamDet entrada) {
		parametro = entrada;
		esNuevo = false;
		accionesDialog("DlgResult", true);
	}

	public void eliminarItem(ParamDet entrada) {
		try {
			paramDetServiceLocal.destroy(entrada);
			parametros = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CODIGO_PARAM);
			addInfoMessage("", "Se ha eliminado el registro exitosamente");
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void guardarItem() {
		try {
			parametro.getParamDetPK().setCodigo(CODIGO_PARAM);
			parametro.getParamDetPK().setNoCia(getCompania().getNoCia());
			
			if (parametro.getParamDetPK().getCodigoDet().equals("") || parametro.getParamDetPK().getCodigoDet().isEmpty()) {
				addErrorMessage("","El campo codigo detalle no puede estar vacio o nulo.");
			}
			parametro.getParamDetPK().setCodigoDet(parametro.getParamDetPK().getCodigoDet());
			
			if (parametro.getDescripcion().equals("") || parametro.getDescripcion().isEmpty()) {
				addErrorMessage("","El campo descripcion detalle no puede estar vacio o nulo.");
			}
			parametro.setDescripcion(parametro.getDescripcion());
			parametro.setEstado1(parametro.getEstado1());
			if (esNuevo) {
				
				paramDetServiceLocal.crearDetalleParametro(parametro);
			} else {
				paramDetServiceLocal.actualizarParametro(parametro);
			}
			parametros = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CODIGO_PARAM);
			addInfoMessage("", "Se ha guardado la informacion exitosamente");
			accionesDialog("DlgResult", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<ParamDet> getParametros() {
		return parametros;
	}

	public void setParametros(List<ParamDet> parametros) {
		this.parametros = parametros;
	}

	public ParamDet getParametro() {
		return parametro;
	}

	public void setParametro(ParamDet parametro) {
		this.parametro = parametro;
	}

	public boolean isEsNuevo() {
		return esNuevo;
	}

	public void setEsNuevo(boolean esNuevo) {
		this.esNuevo = esNuevo;
	}

}