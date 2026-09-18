/**
 * 
 */
package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class ClientesTiempoExternoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7047472324327564495L;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal cxcNativeServiceLocal;

	private String noCia;
	private String identificacion;
	private String nombre;
	private List<Object[]> clientes;

	public ClientesTiempoExternoController() {
		this.noCia = getCompania().getNoCia();
	}

	public void limpiarDatos() {
		identificacion = null;
		nombre = null;
		clientes = new ArrayList<>();
	}

	public void consultar() {
		clientes = new ArrayList<>();
		if ((identificacion == null || identificacion.trim().isEmpty())
				&& (nombre == null || nombre.trim().isEmpty())) {
			error("Es obligatorio el ingreso de identificación o nombre para realizar la consulta.");
			return;
		}
		clientes = cxcNativeServiceLocal.consultarCliente(this.noCia, identificacion, nombre);
		if (clientes.isEmpty()) {
			info("No existe clientes con los datos ingresados");
		}
	}

	public void actualizarDias(Object[] item) {
		String msjError = "No se actualizó el registro por favor intentar nuevamente.";
		try {
			int cantActualizado = cxcNativeServiceLocal.actualizarDiasPagoU(noCia, item);
			if (cantActualizado == 0) {
				error(msjError);
			} else {
				info("Dato actualizado correctamente");
			}
		} catch (Exception e) {
			e.printStackTrace();
			error(msjError);
		}
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getIdentificacion() {
		return identificacion;
	}

	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Object[]> getClientes() {
		return clientes;
	}

	public void setClientes(List<Object[]> clientes) {
		this.clientes = clientes;
	}

}
