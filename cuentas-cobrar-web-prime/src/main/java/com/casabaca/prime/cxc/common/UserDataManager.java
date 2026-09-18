/*
 * UserDataManager.java
 *
 * Created on April 19, 2007, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.casabaca.prime.cxc.common;

import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.casabaca.common.ejb.model.NomEmpleados;
import com.casabaca.common.ejb.model.UsuarioCentroBodega;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.s3s.ejb.model.Compania;
import com.casabaca.s3s.ejb.model.GenEmpresas;
import com.casabaca.s3s.ejb.model.UsuarioCentro;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.GenEmpresasServiceLocal;

/**
 * Data manager usado para administrar los datos de usuario
 * 
 * @author Administrador
 */
@ViewScoped
@ManagedBean
public class UserDataManager {
	
	static Logger logger = Logger.getLogger(UserDataManager.class.getName());

	private UsuarioSis usuario;
	private Compania compania;
	private NomEmpleados nomEmpleado;
	private UsuarioCentro usuarioCentro;
	private GenEmpresas empresa;
	private UsuarioCentroBodega usuarioCentroBodega;

	private String codigoModulo;
	private String codigoMenu;
	private String lineaNegocio;
	private String layout;
	private String theme;
	
	private GenEmpresasServiceLocal genEmpresasService;
	
	public UserDataManager(){
		
		try {
			genEmpresasService = (GenEmpresasServiceLocal) ServiceLocator
					.getService(com.casabaca.common.ejb.util.NombreJNDI.GEN_EMPRESAS_SERVICE);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
		llenarGenEmpresa(getRequestParameter("noCia"));
	}
	
	public void llenarGenEmpresa(String noCia) {	
		if(noCia!=null){
			GenEmpresas geEmpresas = genEmpresasService.consultarLayoutS3S(noCia);
			setEmpresa(geEmpresas);
			setLayout(this.empresa.getLayout());
			setTheme(this.empresa.getTheme());
		}
	}
	
	private String getRequestParameter(String name) {
		return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
	}

	/**
	 * Metodo que devuelve el valor de la propiedad Compania.
	 * 
	 * @return el valor de la propiedad Compania
	 */
	public Compania getCompania() {
		return compania;
	}

	/**
	 * Metodo que define el valor de la propiedad compania.
	 * 
	 * @param compania
	 *            el nuevo valor de compania
	 */
	public void setCompania(Compania compania) {
		this.compania = compania;
	}

	/**
	 * Metodo que devuelve el valor de la propiedad UsuarioCentro.
	 * 
	 * @return el valor de la propiedad UsuarioCentro
	 */
	public UsuarioCentro getUsuarioCentro() {
		return usuarioCentro;
	}

	/**
	 * Metodo que define el valor de la propiedad usuarioCentro.
	 * 
	 * @param usuarioCentro
	 *            el nuevo valor de usuarioCentro
	 */
	public void setUsuarioCentro(UsuarioCentro usuarioCentro) {
		this.usuarioCentro = usuarioCentro;

	}

	/**
	 * Metodo que devuelve el valor de la propiedad Usuario.
	 * 
	 * @return el valor de la propiedad Usuario
	 */
	public UsuarioSis getUsuario() {
		return usuario;
	}

	/**
	 * Metodo que define el valor de la propiedad usuario.
	 * 
	 * @param usuario
	 *            el nuevo valor de usuario
	 */
	public void setUsuario(UsuarioSis usuario) {
		this.usuario = usuario;
	}

	/**
	 * @return the empresa
	 */
	public GenEmpresas getEmpresa() {
		return empresa;
	}

	/**
	 * @param empresa
	 *            the empresa to set
	 */
	public void setEmpresa(GenEmpresas empresa) {
		this.empresa = empresa;
	}

	public NomEmpleados getNomEmpleado() {
		return nomEmpleado;
	}

	public void setNomEmpleado(NomEmpleados nomEmpleado) {
		this.nomEmpleado = nomEmpleado;
	}

	public String getCodigoModulo() {
		return codigoModulo;
	}

	public void setCodigoModulo(String codigoModulo) {
		this.codigoModulo = codigoModulo;
	}

	public String getCodigoMenu() {
		return codigoMenu;
	}

	public void setCodigoMenu(String codigoMenu) {
		this.codigoMenu = codigoMenu;
	}

	public UsuarioCentroBodega getUsuarioCentroBodega() {
		return usuarioCentroBodega;
	}

	public void setUsuarioCentroBodega(UsuarioCentroBodega usuarioCentroBodega) {
		this.usuarioCentroBodega = usuarioCentroBodega;
	}

	public String getLineaNegocio() {
		return lineaNegocio;
	}

	public void setLineaNegocio(String lineaNegocio) {
		this.lineaNegocio = lineaNegocio;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

}
	
