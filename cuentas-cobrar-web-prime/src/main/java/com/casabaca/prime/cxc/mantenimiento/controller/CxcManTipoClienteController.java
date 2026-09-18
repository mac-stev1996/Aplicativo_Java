package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.StringUtils;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.cml.model.CxcTipoCliente;
import com.casabaca.cxc.cml.model.CxcTipoClientePK;
import com.casabaca.cxc.ejb.servicio.CxcTipoClienteMantenimientoServiceLocal;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * Controlador para mantenimiento de Tipos de Clientes que pertenecen a flotas.
 */
@ViewScoped
@ManagedBean
public class CxcManTipoClienteController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	static final Logger logger = Logger.getLogger(CxcManTipoClienteController.class);

	@EJB(lookup = NombreJNDI.CXC_MAN_TIPO_CLIENTE_SERVICE)
	private CxcTipoClienteMantenimientoServiceLocal tipoClienteService;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	private List<CxcTipoCliente> listaTipoCliente;
	private List<LineaNegocio> listaLineaNegocio;
	private CxcTipoCliente cxcTipoCliente;
	private String noCia;
	private String esEdicion;
	private Date fechaMinFinal;

	public CxcManTipoClienteController() {
		this.noCia = getCompania().getNoCia();
	}

	/**
	 * Inicializa el controlador cargando los datos necesarios.
	 */
	@PostConstruct
	private void init() {
		cxcTipoCliente = new CxcTipoCliente(new CxcTipoClientePK());
		consultarLineasNegocio();
		consultarTiposCliente();
	}

	public void crear() {
		this.esEdicion = CommonConstants.NO_STRING_VALUE;
		inicializaObjetoCarga();
		accionesDialog("dlgNuevo", Boolean.TRUE);
	}

	private void inicializaObjetoCarga() {
		cxcTipoCliente = new CxcTipoCliente(new CxcTipoClientePK());
		cxcTipoCliente.getId().setNoCia(this.noCia);
		fechaMinFinal = null;
	}

	public void consultarLineasNegocio() {
		try {
			listaLineaNegocio = lineaNegocioService.buscarLineaNegocioLista(this.noCia);
		} catch (FindException e) {
			logger.error("No se cargaron las lineas de negocio");
		}
	}

	public void consultarTiposCliente() {
		listaTipoCliente = tipoClienteService.consultarPorNoCia(this.noCia);
		for (CxcTipoCliente tipoCliente : listaTipoCliente) {
			tipoCliente.setNombreLinea(obtenerNombreLinea(tipoCliente));
		}
	}

	private String obtenerNombreLinea(CxcTipoCliente tipoCliente) {
		return lineaNegocioService.obtenerDescripcionLineaNeg(noCia, tipoCliente.getId().getNoLinea().toString());
	}

	public void abrirEditar(CxcTipoCliente itemSeleccionado) {
		this.esEdicion = CommonConstants.YES_STRING_VALUE;
		cxcTipoCliente = itemSeleccionado;
		fechaMinFinal = itemSeleccionado.getFechaInicial();
		accionesDialog("dlgNuevo", Boolean.TRUE);
	}

	public void eliminar(CxcTipoCliente cxcTipoCliente) {
		try {
			tipoClienteService.eliminar(cxcTipoCliente);
			info("Registro eliminado correctamente.");
		} catch (DeleteException e) {
			String detalle = StringUtils.nvl(e.getDetail());
			if (detalle.contains("ORA-02292")
					|| (e.getCause() != null && e.getCause().toString().contains("ORA-02292"))) {
				error("El Tipo de Cliente tiene registros asociados en el sistema.");
			} else {
				error("No se puede eliminar: " + detalle);
			}
			logger.error(e.getCause());
		}
		consultarTiposCliente();
	}

	public void onChangeFechaInicial() {
		fechaMinFinal = cxcTipoCliente.getFechaInicial();
		cxcTipoCliente.setFechaFinal(null);
	}

	public void guardar() {
		if (cxcTipoCliente.getFechaInicial() != null && cxcTipoCliente.getFechaFinal() != null
				&& cxcTipoCliente.getFechaInicial().after(cxcTipoCliente.getFechaFinal())) {
			error("La Fecha Inicial no puede ser mayor a la Fecha Final.");
			return;
		}

		try {
			if (CommonConstants.NO_STRING_VALUE.equals(esEdicion)) {
				if (tipoClienteService.consultarPorPk(new CxcTipoClientePK(this.noCia,
						cxcTipoCliente.getId().getTipoCliente(), cxcTipoCliente.getId().getNoLinea())) != null) {
					error("El Tipo de Cliente '" + cxcTipoCliente.getId().getTipoCliente()
							+ "' ya existe. No se puede crear un registro duplicado.");
					return;
				}
				cxcTipoCliente.getId().setNoCia(noCia);
				cxcTipoCliente.setFechaCrea(new Date());
				tipoClienteService.crear(cxcTipoCliente);
			} else {
				tipoClienteService.actualizar(cxcTipoCliente);
			}
			info("Datos guardados corectamente");
			consultarTiposCliente();
			accionesDialog("dlgNuevo", Boolean.FALSE);
		} catch (InsertException e) {
			error("Ocurrió un problema al guardar el registro.");
			logger.error(e.getCause());
		} catch (UpdateException e) {
			error("Ocurrió un problema al actualizar el registro.");
			logger.error(e.getCause());
		}
	}

	public void onChangeTipoCliente() {
		if (cxcTipoCliente.getId().getTipoCliente() != null) {
			cxcTipoCliente.getId().setTipoCliente(cxcTipoCliente.getId().getTipoCliente().toUpperCase());
		}
	}

	public void onChangeDescripcion() {
		if (cxcTipoCliente.getDescripcion() != null) {
			cxcTipoCliente.setDescripcion(cxcTipoCliente.getDescripcion().toUpperCase());
		}
	}

	// Getters y Setters
	public List<LineaNegocio> getListaLineaNegocio() {
		return listaLineaNegocio;
	}

	public void setListaLineaNegocio(List<LineaNegocio> l) {
		this.listaLineaNegocio = l;
	}

	public List<CxcTipoCliente> getListaTipoCliente() {
		return listaTipoCliente;
	}

	public void setListaTipoCliente(List<CxcTipoCliente> l) {
		this.listaTipoCliente = l;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public CxcTipoCliente getCxcTipoCliente() {
		return cxcTipoCliente;
	}

	public void setCxcTipoCliente(CxcTipoCliente c) {
		this.cxcTipoCliente = c;
	}

	public String getEsEdicion() {
		return esEdicion;
	}

	public void setEsEdicion(String esEdicion) {
		this.esEdicion = esEdicion;
	}

	public Date getFechaMinFinal() {
		return fechaMinFinal;
	}

	public void setFechaMinFinal(Date fechaMinFinal) {
		this.fechaMinFinal = fechaMinFinal;
	}
}