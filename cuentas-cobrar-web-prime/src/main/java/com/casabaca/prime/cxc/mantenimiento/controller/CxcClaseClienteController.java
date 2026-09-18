package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.common.ejb.model.CxcClaseCliente;
import com.casabaca.common.ejb.model.CxcClaseClientePK;
import com.casabaca.common.ejb.service.CxcClaseClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.lazy.LazyDataModelCxcClaseCliente;


@ManagedBean
@ViewScoped
public class CxcClaseClienteController extends CommonController implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7131436108498670782L;

	private static final Logger log = Logger.getLogger(CxcClaseClienteController.class);

	private static final int PAGE_SIZE = 10;

	@EJB(lookup = NombreJNDI.CXC_CLASE_CLIENTE_SERVICE)
	private CxcClaseClienteServiceLocal servicioClaseCliente;
	

	private LazyDataModelCxcClaseCliente lazyDataModelCxcClaseCliente;

	private List<CxcClaseCliente> listadoClaseClientes;
	private List<CxcClaseCliente> listadoClaseClientesSelected;
	private List<String> listadoNegocios;
	private CxcClaseCliente cxcClaseClienteSeleccionado;
	private boolean esNuevo;


	@PostConstruct
	public void init() {
		listadoClaseClientes = new ArrayList<CxcClaseCliente>();
		listadoClaseClientesSelected = new ArrayList<CxcClaseCliente>();
		lazyDataModelCxcClaseCliente = new LazyDataModelCxcClaseCliente(0, PAGE_SIZE, getCompania().getNoCia());
		
		try {
			listadoNegocios = servicioClaseCliente.listarNegocios(getCompania().getNoCia());
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void nuevoItem() {
		esNuevo=true;
		cxcClaseClienteSeleccionado = new CxcClaseCliente();
		CxcClaseClientePK pk = new CxcClaseClientePK();
		pk.setNoCia(getCompania().getNoCia());
		cxcClaseClienteSeleccionado.setCxcClaseClientePK(pk);
	}
	

	public void seleccionar(CxcClaseCliente CxcClaseClientes) {
		esNuevo=false;
		cxcClaseClienteSeleccionado=CxcClaseClientes;
	}
	


	public void grabar() {
		
		try {
			if(cxcClaseClienteSeleccionado==null) {
				addErrorMessage("Advertencia", "Seleccione un tipo de visita");
				return;
			}
			
			if(esNuevo) {
				CxcClaseClientePK pk = cxcClaseClienteSeleccionado.getCxcClaseClientePK();
				boolean existe;
				try {
					servicioClaseCliente.findClaseClientesByNegocio( pk.getNegocio(), pk.getClase(), pk.getNoCia());
					existe=true;
				}catch (Exception e) {
					existe=false;
				}
				if(existe) {
					addErrorMessage("Advertencia", "Ya existe un registro con la clave primaria");
					return;
				}
				servicioClaseCliente.insertar(cxcClaseClienteSeleccionado);
				addInfoMessage("Exito", "Registro creado con Exito");
			}else {
				servicioClaseCliente.actualizar(cxcClaseClienteSeleccionado);
				addInfoMessage("Exito", "Registro actualizado con Exito");
			}
			RequestContext.getCurrentInstance().execute("PF('dlgClase').hide();");
			init();

		}catch (Exception e) {
			addErrorMessage("Error", "Error al Grabar la información: "+(e.getCause()==null?e.getMessage():e.getCause().getMessage()));
			log.error(e.getMessage());
		}
	}

	public List<CxcClaseCliente> getListadoClaseClientes() {
		return listadoClaseClientes;
	}

	public void setListadoClaseClientes(List<CxcClaseCliente> listadoClaseClientes) {
		this.listadoClaseClientes = listadoClaseClientes;
	}

	public CxcClaseCliente getCxcClaseClienteSeleccionado() {
		return cxcClaseClienteSeleccionado;
	}

	public void setCxcClaseClienteSeleccionado(CxcClaseCliente CxcClaseClienteSeleccionado) {
		this.cxcClaseClienteSeleccionado = CxcClaseClienteSeleccionado;
	}

	public boolean isEsNuevo() {
		return esNuevo;
	}

	public List<CxcClaseCliente> getListadoClaseClientesSelected() {
		return listadoClaseClientesSelected;
	}

	public void setListadoClaseClientesSelected(List<CxcClaseCliente> listadoClaseClientesSelected) {
		this.listadoClaseClientesSelected = listadoClaseClientesSelected;
	}

	public LazyDataModelCxcClaseCliente getLazyDataModelCxcClaseCliente() {
		return lazyDataModelCxcClaseCliente;
	}

	public List<String> getListadoNegocios() {
		return listadoNegocios;
	}

	public int getPageSize() {
		return PAGE_SIZE;
	}
	
	

}
