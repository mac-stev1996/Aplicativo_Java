package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.model.ParamDetPK;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.DeleteException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * Controlador para parametrizar los analistas de Credito
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcParametrizacionAnalistasCreditoController")
public class CxcParametrizacionAnalistasCreditoController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;
	private static final Logger log = Logger.getLogger(CxcParametrizacionAnalistasCreditoController.class);

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuariosService;

	private List<UsuarioSis> usuarios;
	private List<ParamDet> parametros;
	private ParamDet parametro;
	private static final String CODIGO_PARAM = "ASCRE";
	private boolean esNuevo;
	
	@PostConstruct
	public void init() {
		esNuevo = false;
		usuarios= usuariosService.obtenerPorDepartamento("CXC");
		List<UsuarioSis> usuariosVeh= usuariosService.obtenerPorDepartamento("VEH");
		usuarios.addAll(usuariosVeh);
		parametros = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CODIGO_PARAM);
	}

	public void abrirAnalista() {
		parametro = new ParamDet(new ParamDetPK(getCompania().getNoCia(), CODIGO_PARAM, null));
		esNuevo = true;
		accionesDialog("DlgResult", true);
	}

	public void editarAnalista(ParamDet entrada) {
		parametro = entrada;
		esNuevo = false;
		accionesDialog("DlgResult", true);
	}

	public void eliminarAnalista(ParamDet entrada) {
		try {
			paramDetServiceLocal.destroy(entrada);
			parametros = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CODIGO_PARAM);
			addInfoMessage("", "Se ha eliminado el registro exitosamente");
		} catch (DeleteException e) {
			addErrorMessage("No se puede eliminar el registro", e.getMessage());
			log.error(e);
		}
	}

	public void guardarAnalista() {
		try {
			if(parametro.getTexto1() == null){
				addErrorMessage("", "Debe seleccionar un analista");
			}
			UsuarioSis user = usuariosService.getUsuarioSisByUsername(parametro.getTexto1());
			parametro.getParamDetPK().setCodigoDet(user.getUsuario());
			parametro.setDescripcion(user.getNombre());
			parametro.setTexto2(user.getEmail());
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			if("A".equals(parametro.getEstado1())){
				parametro.setTexto3(sdf.format(new Date()));
			}else{
				parametro.setTexto4(sdf.format(new Date()));
			}
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

	public List<UsuarioSis> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<UsuarioSis> usuarios) {
		this.usuarios = usuarios;
	}

}