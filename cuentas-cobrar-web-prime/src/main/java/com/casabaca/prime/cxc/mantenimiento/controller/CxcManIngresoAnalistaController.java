/**
 * 
 */
package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcAnalistaCredito;
import com.casabaca.cxc.ejb.modelo.CxcAnalistaCreditoPK;
import com.casabaca.cxc.ejb.servicio.CxcAnalistaCreditoServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * @author laura.llangari
 *
 */
@ManagedBean
@ViewScoped
public class CxcManIngresoAnalistaController extends CommonController implements Serializable {

	@EJB(lookup = NombreJNDI.ANALISTA_CREDITO_SERVICE)
	private CxcAnalistaCreditoServiceLocal analistaService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuariosService;

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -396708177889245383L;
	static final Logger LOG = Logger.getLogger(CxcManIngresoAnalistaController.class);
	private String noCia;
	private List<UsuarioSis> usuarios;
	private List<CxcAnalistaCredito> listaAnalista;
	private CxcAnalistaCredito analistaCredito;
	private String origenDialogo;

	/**
	 * 
	 */

	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		this.listaAnalista = new ArrayList<CxcAnalistaCredito>();
		this.analistaCredito = new CxcAnalistaCredito(new CxcAnalistaCreditoPK());
		usuarios= usuariosService.obtenerPorDepartamento("CXC");
		detalleAnalistaCredito();

	}

	public CxcManIngresoAnalistaController() {
		// TODO Auto-generated constructor stub
	}

	private void inicializaObjetoAnalistaCredito() {
		analistaCredito = new CxcAnalistaCredito(new CxcAnalistaCreditoPK());
		analistaCredito.getCxcAnalistaCreditoPK().setNoCia(noCia);
	}

	private void detalleAnalistaCredito() {
		try {
			listaAnalista = analistaService.listarAnalistaCredito(this.noCia);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

	}

	public void agregar() {
		this.origenDialogo = "N";
		inicializaObjetoAnalistaCredito();
		analistaCredito.setNombreAnalista("");
		analistaCredito.getCxcAnalistaCreditoPK().setCodigoAnalista("");
		analistaCredito.setEstado("A");
		accionesDialog("dlgNuevo", Boolean.TRUE);

	}

	public void abrirEditar(CxcAnalistaCredito itemSeleccionado) throws FileNotFoundException {
		this.origenDialogo = "E";
		analistaCredito = itemSeleccionado;
		accionesDialog("dlgNuevo", Boolean.TRUE);
	}

	public void guardar() {

		try {
			analistaCredito.setUsuarioCreacion(getUsuario().getUsuario());
			analistaCredito.setFechaCreacion(new Date());

			if ("N".equals(origenDialogo)) {
				analistaCredito.getCxcAnalistaCreditoPK().setNoCia(this.noCia);
				analistaService.guardarAnalista(analistaCredito);
				info("Datos Guardados con éxito..");
			} else {
				analistaService.actualizarAnalista(analistaCredito);
				info("Datos Actualizados con éxito..");
			}
			detalleAnalistaCredito();
			accionesDialog("dlgNuevo", Boolean.FALSE);
		} catch (Exception e) {
			error("Ocurrio un problema al guardar Registro " + e.getCause());
			LOG.error(e.getCause());
		}

	}

	public void eliminar(CxcAnalistaCredito analista) {

		try {
			analistaService.eliminarAnalista(analista);
			detalleAnalistaCredito();
			accionesDialog("dlgConfirmar", Boolean.FALSE);
		} catch (Exception e) {
			error("Ocurrio un problema al Eliminar Registro " + e.getCause());
			LOG.error(e.getCause());
		}

	}

	// GETTER Y SETTER

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<CxcAnalistaCredito> getListaAnalista() {
		return listaAnalista;
	}

	public void setListaAnalista(List<CxcAnalistaCredito> listaAnalista) {
		this.listaAnalista = listaAnalista;
	}

	public CxcAnalistaCredito getAnalistaCredito() {
		return analistaCredito;
	}

	public void setAnalistaCredito(CxcAnalistaCredito analistaCredito) {
		this.analistaCredito = analistaCredito;
	}

	public String getOrigenDialogo() {
		return origenDialogo;
	}

	public void setOrigenDialogo(String origenDialogo) {
		this.origenDialogo = origenDialogo;
	}

	public List<UsuarioSis> getUsuarios() {
		return usuarios;
	}
	
	

}
