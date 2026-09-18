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
 * Controlador para parametrizar a los comite credito controller
 * 
 * @author ed_picuasi
 *
 */
@ViewScoped
@ManagedBean(name = "cxcParamComiteCreditoController")
public class CxcParamComiteCreditoController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/***
	 * 
	 * VARIABLES DE SERVICIOS
	 * 
	 ***/
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuariosService;

	private List<UsuarioSis> usuarios;
	private List<ParamDet> parametros;
	private ParamDet parametro;
	private static final String CODIGO_PARAM = "CCAPR";
	private boolean esNuevo;
	private String observacion;
	
	@PostConstruct
	public void init() {
		esNuevo = false;
		usuarios= usuariosService.obtenerPorDepartamento("CXC");
		List<UsuarioSis> usuariosVeh= usuariosService.obtenerPorDepartamento("VEH");
		usuarios.addAll(usuariosVeh);
		parametros = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CODIGO_PARAM);
	}

	/***
	 * Metodo para abrir un comite de la parametrizacion de aprobacion comite
	 */
	public void abrirComite() {
		List<UsuarioSis> usuariosVeh= usuariosService.obtenerPorDepartamento("FIN");
		usuarios.addAll(usuariosVeh);
		usuariosVeh= usuariosService.obtenerPorDepartamento("GER");
		usuarios.addAll(usuariosVeh);
		usuariosVeh= usuariosService.obtenerPorDepartamento("CON");
		usuarios.addAll(usuariosVeh);
		parametro = new ParamDet(new ParamDetPK(getCompania().getNoCia(), CODIGO_PARAM, null));
		esNuevo = true;
		accionesDialog("DlgResult", true);
	}

	/***
	 * Metodo para editar un calaborador de la parametrizacion de aprobacion comite
	 * @param entrada
	 */
	public void editarComite(ParamDet entrada) {
		parametro = entrada;
		setObservacion(parametro.getTexto5());
		esNuevo = false;
		accionesDialog("DlgResult", true);
	}

	/***
	 * Metodo para eliminar un colaborador de la parametrizacion de aprobacion comite
	 * @param entrada
	 */
	public void eliminarComite(ParamDet entrada) {
		try {
			paramDetServiceLocal.destroy(entrada);
			parametros = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CODIGO_PARAM);
			addInfoMessage("", "Se ha eliminado el registro exitosamente");
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	/***
	 * Metodo para guardar el colaborador parte del comite
	 */
	public void guardarComite() {
		try {
			if(parametro.getTexto1() == null){
				addErrorMessage("", "Debe seleccionar un encargado");
			}
			UsuarioSis user = usuariosService.getUsuarioSisByUsername(parametro.getTexto1());
			parametro.getParamDetPK().setCodigoDet(user.getNoEmple());
			parametro.setDescripcion(user.getNombre());
			parametro.setTexto2(user.getEmail());
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			if("A".equals(parametro.getEstado1())){
				parametro.setTexto3(sdf.format(new Date()));
			}else{
				parametro.setTexto4(sdf.format(new Date()));
				parametro.setTexto6(parametro.getTexto5());
			}
			parametro.setTexto5(getObservacion());
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
	
	
	/***
	 * 
	 * GETTERS Y SETTERS
	 * 
	 */	
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

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

}