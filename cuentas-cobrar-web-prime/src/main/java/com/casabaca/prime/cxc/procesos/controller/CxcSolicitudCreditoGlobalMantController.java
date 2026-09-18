package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCreditoGlobalMant;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCreditoGlobalMantPK;
import com.casabaca.cxc.ejb.servicio.CxcCreditoGlobalMantServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

@ViewScoped
@ManagedBean(name = "cxcSolicitudCreditoGlobalMantController")
public class CxcSolicitudCreditoGlobalMantController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB(lookup = NombreJNDI.CXC_CREDITO_GLOBAL_MANT_SERVICE)
	private CxcCreditoGlobalMantServiceLocal creditoGlobalMantServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	private static Logger logger = Logger.getLogger(CxcSolicitudCreditoGlobalMantController.class);

	private List<CxcSolicitudCreditoGlobalMant> analistas;
	private List<UsuarioSis> usuarios;
	private String cia;
	private boolean esNuevo;

	private CxcSolicitudCreditoGlobalMant analista;
	private CxcSolicitudCreditoGlobalMantPK analistaPk;

	@PostConstruct
	public void init() {
		logger.info("Iniciando Servicio");
		esNuevo = false;
		analistas = new ArrayList<>();
		usuarios = new ArrayList<>();
		analista = new CxcSolicitudCreditoGlobalMant(new CxcSolicitudCreditoGlobalMantPK());
		obtenerCia();
		cargarAnalistas();
	}
	
	private void obtenerCia() {
		cia = getCompania().getNoCia();
		if (null != getRequestParameter("noCia")) {
			cia = getRequestParameter("noCia");
		}
	}

	private void cargarAnalistas() {
		analistas = creditoGlobalMantServiceLocal.obtenerLista(this.analista, 0, 0);
	}

	public void nuevoAnalista() {
		esNuevo = true;
		accionesDialog("DlgResultGrupo", true);
	}

	public void seleccionarAnalista(CxcSolicitudCreditoGlobalMant analistaIn) {
		esNuevo = false;
		analista = analistaIn;
		accionesDialog("DlgResultGrupo", true);
	}

	public void cargarUsuarios() {
		usuarios = usuarioSisService.obtenerPorDepartamento("CXC");
		accionesDialog("dialogUsuarios", true);
	}

	public void validarTelefono() {
		if(analista.getTelefono().length()<10 || analista.getTelefono().length()>11)
			addErrorMessage("Teléfono incorrecto", "Debe tener 10 caracteres");
	}
	
	public void seleccionarUsuario(UsuarioSis usuario) {
		analistaPk = new CxcSolicitudCreditoGlobalMantPK();
		analistaPk.setNoCia(getCompania().getNoCia());
		analistaPk.setUsuario(usuario.getUsuario());
		analista.setNombre(usuario.getNombre());
		analista.setCorreo(usuario.getEmail());
		analista.setUsuarioCb(usuario.getUsuario());
		analista.setUsuarioCl(usuario.getUsuario());
		analista.setUsuarioGbv(usuario.getUsuario());
		analista.setUsuarioNx(usuario.getUsuario());
		analista.setUsuarioSzk(usuario.getUsuario());
		analista.setUsuarioTy(usuario.getUsuario());
		analista.setPk(analistaPk);
	}

	public void guardar() {
		try {
			if (esNuevo) {
				creditoGlobalMantServiceLocal.crear(analista);
				addInfoMessage("Guardado", "Correctamente");
			} else {
				creditoGlobalMantServiceLocal.actualizar(analista);
				addInfoMessage("Actualizado", "Correctamente");
			}
		} catch (InsertException | UpdateException e) {
			addErrorMessage("Error al guardar", e.getCause().toString());
			logger.error("Error al guardar analista", e);
		}
		accionesDialog("DlgResultGrupo", false);
		init();
	}

	public List<CxcSolicitudCreditoGlobalMant> getAnalistas() {
		return analistas;
	}

	public void setAnalistas(List<CxcSolicitudCreditoGlobalMant> analistas) {
		this.analistas = analistas;
	}

	public List<UsuarioSis> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<UsuarioSis> usuarios) {
		this.usuarios = usuarios;
	}

	public boolean isEsNuevo() {
		return esNuevo;
	}

	public void setEsNuevo(boolean esNuevo) {
		this.esNuevo = esNuevo;
	}

	public CxcSolicitudCreditoGlobalMant getAnalista() {
		return analista;
	}

	public void setAnalista(CxcSolicitudCreditoGlobalMant analista) {
		this.analista = analista;
	}

	public CxcSolicitudCreditoGlobalMantPK getAnalistaPk() {
		return analistaPk;
	}

	public void setAnalistaPk(CxcSolicitudCreditoGlobalMantPK analistaPk) {
		this.analistaPk = analistaPk;
	}

	public String getCia() {
		return cia;
	}

	public void setCia(String cia) {
		this.cia = cia;
	}
	
}