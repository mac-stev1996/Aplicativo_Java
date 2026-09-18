package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.ArcctasaMora;
import com.casabaca.cxc.ejb.modelo.ArcctasaMoraPK;
import com.casabaca.cxc.ejb.servicio.ArcctasaMoraServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcTipoCreditoTcServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * Controlador para Comisiones de Cobro por Mora
 * 
 * @author jl_reyes
 *
 */
@ManagedBean
@ViewScoped
public class CxcArcctasaMoraController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3915447405236987935L;

	static final Logger LOG = Logger.getLogger(CxcArcctasaMoraController.class);

	@EJB(lookup = NombreJNDI.CXC_ARCCTASA_MORA_SERVICE)
	private ArcctasaMoraServiceLocal arcctasaMoraServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_TIPO_CREDITO_TC_SERVICE_BEAN)
	private CxcTipoCreditoTcServiceLocal cxcTipoCreditoTcServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioServicioLocal;

	private List<ArcctasaMora> listado;
	private List<LineaNegocio> listadoLineas;
	private ArcctasaMora arccTasaMoraNuevo;
	private ArcctasaMora arccTasaMoraSelected;

	@PostConstruct
	public void init() {
		listado = new ArrayList<ArcctasaMora>();
		arccTasaMoraNuevo = new ArcctasaMora(new ArcctasaMoraPK(getCompania().getNoCia(), null));
		arccTasaMoraSelected = new ArcctasaMora();
		listado = arcctasaMoraServiceLocal.buscarPorNoCia(getCompania().getNoCia());

	}

	public void editar(ArcctasaMora arccTasaMora) {
		try {
			arcctasaMoraServiceLocal.actualizar(arccTasaMora);

			info("Registro Actualizado con Exito");

		} catch (Exception e) {
			error("Ocurrio un Error al Actualizar Comision Cobro por Mora");
			LOG.error("Ocurrio un Error al Actualizar Comision Cobro por Mora", e);
		}

	}

	public void grabar() {
		try {

			if (arccTasaMoraNuevo.getDiasGracia() != null && arccTasaMoraNuevo.getDiasGraciaInt() != null
					&& arccTasaMoraNuevo.getId().getLinea() != null && arccTasaMoraNuevo.getTasaComision() != null
					&& arccTasaMoraNuevo.getTasaMora() != null) {

				if (arcctasaMoraServiceLocal
						.validarExite(arccTasaMoraNuevo.getId().getNoCia(), arccTasaMoraNuevo.getId().getLinea())
						.intValue() == 0) {

					arcctasaMoraServiceLocal.insertar(arccTasaMoraNuevo);
					info("Registro Almacenado Con Exito");
				} else {

					error("El registro ingresado ya existe para la compania");
				}

				init();
			} else {

				error("Favor ingresar los campos con (*), son Obligatorios");

			}

		} catch (Exception e) {
			error("Ocurrio un Error al Grabar nuevo Registro");
			LOG.error("Ocurrio un Error al Grabar nuevo Registro", e);
		}

	}

	public void eliminar(ArcctasaMora arccTasaMora) {
		try {
			arcctasaMoraServiceLocal.eliminar(arccTasaMora);
			info("Registro Eliminado con Exito");
			init();
		} catch (Exception e) {
			error("Ocurrio un Error al Eliminar el registro");
			LOG.error("Ocurrio un Error al Eliminar el registro", e);
		}

	}

	public String obtenerDescripcionLinea(ArcctasaMora arccTasaMora) {

		return cxcTipoCreditoTcServiceLocal.obtenerDescripcionLineaNegocio(arccTasaMora.getId().getNoCia(),
				arccTasaMora.getId().getLinea());

	}

	public void nuevo() {
		try {
			arccTasaMoraNuevo = new ArcctasaMora(new ArcctasaMoraPK(getCompania().getNoCia(), null));
			listadoLineas = new ArrayList<LineaNegocio>();

			listadoLineas = lineaNegocioServicioLocal.lineasNegocio(getCompania().getNoCia());

		} catch (Exception e) {
			error("Error al Cargar Linea de Negocio");
			LOG.error("Error al Cargar Linea de Negocio", e);
		}

	}

	public List<LineaNegocio> getListadoLineas() {
		return listadoLineas;
	}

	public void setListadoLineas(List<LineaNegocio> listadoLineas) {
		this.listadoLineas = listadoLineas;
	}

	public List<ArcctasaMora> getListado() {
		return listado;
	}

	public void setListado(List<ArcctasaMora> listado) {
		this.listado = listado;
	}

	public ArcctasaMora getArccTasaMoraNuevo() {
		return arccTasaMoraNuevo;
	}

	public void setArccTasaMoraNuevo(ArcctasaMora arccTasaMoraNuevo) {
		this.arccTasaMoraNuevo = arccTasaMoraNuevo;
	}

	public ArcctasaMora getArccTasaMoraSelected() {
		return arccTasaMoraSelected;
	}

	public void setArccTasaMoraSelected(ArcctasaMora arccTasaMoraSelected) {
		this.arccTasaMoraSelected = arccTasaMoraSelected;
	}

}
