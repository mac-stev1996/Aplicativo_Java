package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.RangosAntiguedad;
import com.casabaca.cxc.ejb.modelo.RangosAntiguedadPK;
import com.casabaca.cxc.ejb.servicio.RangoAntiguedadServicesLocal;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

@ViewScoped
@ManagedBean
public class RangoAntiguedadController extends CommonController implements Serializable {
	private static final long serialVersionUID = 1L;

	@EJB(lookup = NombreJNDI.CXC_RANGOS_ANTIGUEDAD)
	private RangoAntiguedadServicesLocal rangoAntiguedadServicesLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;

	private List<RangosAntiguedad> listaRangosAntiguedad;
	private RangosAntiguedad rangosAntiguedad;
	private List<UsuarioSis> listaUsuariosSis;

	@PostConstruct
	public void init() throws FindException {
		rangosAntiguedad = new RangosAntiguedad();
		listaDeUsuarios();
		cargarListaRangosAntiguedad();
	}

	public List<UsuarioSis> listaDeUsuarios() {
		return this.listaUsuariosSis = this.usuarioSisServiceLocal.getUsuarioList(getCompania().getNoCia());
	}

	public List<RangosAntiguedad> cargarListaRangosAntiguedad() throws FindException {
		this.listaRangosAntiguedad = this.rangoAntiguedadServicesLocal.findByNoCia(getCompania().getNoCia());
		Collections.sort(this.listaRangosAntiguedad,
				(obj1, obj2) -> obj1.getId().getDesde().compareTo(obj2.getId().getDesde()));
		return this.listaRangosAntiguedad;
	}

	public void cargarRangoAntiguedad() {
		this.rangosAntiguedad = null;
		RangosAntiguedadPK pk = new RangosAntiguedadPK();
		pk.setNoCia(getCompania().getNoCia());
		RangosAntiguedad rangosAntiguedad = new RangosAntiguedad();
		rangosAntiguedad.setId(pk);
		this.rangosAntiguedad = rangosAntiguedad;
	}

	public boolean validaciones() {
		if (this.rangosAntiguedad.getId().getDesde().isEmpty()) {
			super.addWarnMessage("Advertencia !!", "La campo 'Desde' no puede estar vacio");
			return false;
		}
		if (this.rangosAntiguedad.getId().getHasta().isEmpty()) {
			super.addWarnMessage("Advertencia !!", "La campo 'Hasta' no puede estar vacio");
			return false;
		}
		if (this.rangosAntiguedad.getDescripcion().isEmpty()) {
			super.addWarnMessage("Advertencia !!", "La campo 'Descripción' no puede estar vacio");
			return false;
		}
		return true;
	}

	public void guardarAgenciaSupervisor() throws FindException, InsertException {
		if (validaciones()) {
			for (RangosAntiguedad rangosAntiguedad : listaRangosAntiguedad) {
				if (rangosAntiguedad.getId().getDesde().equals(this.rangosAntiguedad.getId().getDesde())
						&& rangosAntiguedad.getId().getHasta().equals(this.rangosAntiguedad.getId().getHasta())) {
					super.addWarnMessage("Advertencia", "El rango ingresado ya existe.");
					this.rangosAntiguedad = null;
					return;
				}
			}
			Calendar calendar = Calendar.getInstance();
			Date dateObj = calendar.getTime();
			this.rangosAntiguedad.setFechaCrea(dateObj);
			this.rangosAntiguedad.setUsuarioCrea(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
			this.rangoAntiguedadServicesLocal.create(this.rangosAntiguedad);
			super.addInfoMessage("Exito", "Registro ingresado exitosamente");
		}
		cargarListaRangosAntiguedad();
	}

	public void editarRangosAntiguedad(RangosAntiguedad rangosAntiguedad) throws UpdateException {
		this.rangoAntiguedadServicesLocal.update(rangosAntiguedad);
		super.addInfoMessage("Exito", "Registro editado exitosamente");
	}

	public void eliminarRangosAntiguedad(RangosAntiguedad rangosAntiguedad) throws DeleteException, FindException {
		this.rangoAntiguedadServicesLocal.delete(rangosAntiguedad);
		super.addInfoMessage("Exito", "Registro eliminado exitosamente");
		cargarListaRangosAntiguedad();
	}

	public String changeUserNameByName(String usuario) {
		String nombre = "";
		for (UsuarioSis usuarioSis : listaUsuariosSis) {
			if (usuarioSis.getUsuario().equals(usuario)) {
				nombre = usuarioSis.getNombre();
			}
		}
		return nombre;
	}

	public String changeNameByUserName(String nombre) {
		String usuario = "";
		for (UsuarioSis usuarioSis : listaUsuariosSis) {
			if (usuarioSis.getNombre().equals(nombre)) {
				usuario = usuarioSis.getUsuario();
			}
		}
		return usuario;
	}

	public List<UsuarioSis> getListaUsuariosSis() {
		return listaUsuariosSis;
	}

	public void setListaUsuariosSis(List<UsuarioSis> listaUsuariosSis) {
		this.listaUsuariosSis = listaUsuariosSis;
	}

	public List<RangosAntiguedad> getListaRangosAntiguedad() {
		return listaRangosAntiguedad;
	}

	public void setListaRangosAntiguedad(List<RangosAntiguedad> listaRangosAntiguedad) {
		this.listaRangosAntiguedad = listaRangosAntiguedad;
	}

	public RangosAntiguedad getRangosAntiguedad() {
		return rangosAntiguedad;
	}

	public void setRangosAntiguedad(RangosAntiguedad rangosAntiguedad) {
		this.rangosAntiguedad = rangosAntiguedad;
	}

}
