package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.model.ParamCab;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.model.ParamDetPK;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;

@ViewScoped
@ManagedBean
public class CxcParametrosCalificacionController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CxcParametrosCalificacionController.class);

	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	private String noCia;
	private String codigo;
	private String descripcion;
	private List<ParamCab> listaCabecera;
	private ParamCab seleccionado;
	private List<ParamDet> listaDetalle;

	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		listaCabecera = new ArrayList<ParamCab>();
		listaDetalle = new ArrayList<ParamDet>();
		seleccionado = null;
	}

	public void buscarCabecera() {
		try {
			this.listaCabecera = paramCabService.getByNoCiaEstado2Codigo(noCia, "ESTADO", codigo, descripcion);
			seleccionado = null;
			listaDetalle = new ArrayList<ParamDet>();
		} catch (FindException e) {
			super.error("Error al buscar cabeceras");
			log.error("Error al buscar cabeceras", e);
		}
	}

	public void seleccionarCabecera() {
		listaDetalle = paramDetService.consultarPorCodigoNocia(seleccionado.getPk().getNoCia(),
				seleccionado.getPk().getCodigo());
		listaDetalle.sort((a, b) -> a.getParamDetPK().getCodigoDet().compareTo(b.getParamDetPK().getCodigoDet()));
	}

	public void creaNuevoDetalle() {
		if (seleccionado == null) {
			super.error("Seleccione una cuenta");
			return;
		}

		if (listaDetalle == null)
			listaDetalle = new ArrayList<ParamDet>();
		ParamDet param = new ParamDet(new ParamDetPK(null, null, null));
		listaDetalle.add(param);
	}

	public void guardar() {
		if (seleccionado == null) {
			super.error("Seleccione una cuenta");
			return;
		}

		if (listaDetalle == null || listaDetalle.isEmpty()) {
			super.warn("No hay nada para guardar");
			return;
		}

		for (ParamDet det : listaDetalle) {
			try {
				if (det.getParamDetPK().getNoCia() == null) {
					det.getParamDetPK().setNoCia(seleccionado.getPk().getNoCia());
					det.getParamDetPK().setCodigo(seleccionado.getPk().getCodigo());
				}
				paramDetService.actualizarParametro(det);
				super.info("Grabado correctamente");
			} catch (InsertException e) {
				super.error("Error al guardar parametro " + det.getParamDetPK().getCodigoDet());
				log.error("Error al guardar parametro " + det.getParamDetPK().getCodigoDet(), e);
			}
		}

	}

	public void eliminarDetalle(ParamDet param) {
		try {
			if (param.getParamDetPK().getNoCia() == null)
				listaDetalle.remove(param);
			else
				paramDetService.destroy(param);
			super.info("Eliminado correctamente");
		} catch (DeleteException e) {
			super.error("Error al eliminar registro");
			log.error("Error al eliminar registro", e);
		}
		seleccionarCabecera();
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public List<ParamCab> getListaCabecera() {
		return listaCabecera;
	}

	public void setListaCabecera(List<ParamCab> listaCabecera) {
		this.listaCabecera = listaCabecera;
	}

	public ParamCab getSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(ParamCab seleccionado) {
		this.seleccionado = seleccionado;
	}

	public List<ParamDet> getListaDetalle() {
		return listaDetalle;
	}

	public void setListaDetalle(List<ParamDet> listaDetalle) {
		this.listaDetalle = listaDetalle;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

}
