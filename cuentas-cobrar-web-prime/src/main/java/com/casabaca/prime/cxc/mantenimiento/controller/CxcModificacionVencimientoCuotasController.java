package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.ParamCab;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcFacturaDeudaClienteDto;
import com.casabaca.cxc.ejb.servicio.FacVentasCabCpServicioLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;

/**
 * Controlador para modificar las fechas de vencimiento de las cuotas, aplicar
 * meses de gracia
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcModificacionVencimientoCuotasController")
public class CxcModificacionVencimientoCuotasController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	private static final String PARAM_JEFE_COBRANZAS = "JEFCOB";
	// private static final String JEFE_DE_COBRANZAS = "6120"; // LG_BOADA

	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabServiceLocal;

	@EJB(lookup = NombreJNDI.FAC_VENTAS_CAB_CP_SERVICIO)
	private FacVentasCabCpServicioLocal facVentasServicio;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	private LazyDataModelClientes clientes;
	private Cliente clienteSeleccionado;
	private List<Arccmd> cuotasAAfectar;
	private List<CxcFacturaDeudaClienteDto> facturasDeudas;
	private List<CxcFacturaDeudaClienteDto> facturasSeleccionadas;										   
	private List<LineaNegocio> lineasDeNegocio;
	private String lineaSeleccionada;
	private List<Arccmd> letrasDeuda;
	private Integer mesesGracia;

	@PostConstruct
	public void init() {
		boolean usuarioAdministrador = false;
		try {
			limpiar();
			ParamCab paramJefeCobranzas = this.paramCabServiceLocal.consultarParametro(getCompania().getNoCia(),
					PARAM_JEFE_COBRANZAS);

			String usuario = getUsuario().getUsuario();

			usuarioAdministrador = (usuario.equals(paramJefeCobranzas.getTexto1())
					|| usuario.equals(paramJefeCobranzas.getTexto2()) || usuario.equals(paramJefeCobranzas.getTexto3())
					|| usuario.equals(paramJefeCobranzas.getTexto4()));

		} catch (FindException| NullPointerException  e) {
			e.printStackTrace();
		}

		if (usuarioAdministrador) {
			clientes = new LazyDataModelClientes(0, 10, getCompania().getNoCia());
			clienteSeleccionado = new Cliente();
			try {
				lineasDeNegocio = lineaNegocioService.buscarLineaNegocioLista(getCompania().getNoCia());
			} catch (FindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			error("Esta opcion esta habilitada unicamente para el Jefe de Cobranzas");
		}
	}

	public void limpiar() {
		lineaSeleccionada = null;
		clienteSeleccionado = new Cliente();
		facturasDeudas = new ArrayList<>();
		cuotasAAfectar = new ArrayList<>();
		letrasDeuda = new ArrayList<>();
		facturasSeleccionadas = new ArrayList<>();
	}

								   
								  
	public void abrirRevisarCuotas() {
		letrasDeuda = new ArrayList<>();
		for (CxcFacturaDeudaClienteDto facSelecciona : facturasSeleccionadas) {
			letrasDeuda.addAll(arccmdService.consultarListadoCuotasVencer(getCompania().getNoCia(), facSelecciona.getCentro(),
					facSelecciona.getNoFisico(), facSelecciona.getTipoDoc(), facSelecciona.getNoCliente(),
					facSelecciona.getFecha(), lineaSeleccionada, facSelecciona.getCantProrrogas()));
		}
		mesesGracia = 0;
		accionesDialog("DlgCuotasRev", true);
	}

	public void agregarMesesGracia() {
		try {
			if (letrasDeuda != null && !letrasDeuda.isEmpty() && mesesGracia > 0) {
				for (Arccmd letra : letrasDeuda) {
					letra.setFechaVence(FechaUtils.sumarNMeses(letra.getFechaVence(), mesesGracia));
					letra.setFechaVenceOriginal(letra.getFechaVence());
					arccmdService.updateArccmd(letra);
				}
				buscarDeudas();
				info("Se ha agregado los " + mesesGracia + " meses de gracia exitosamente");
			}

		} catch (Exception e) {
			error("Error al agregar meses de gracia " + e);
		}
	}

	public void buscarDeudas() {
		if (clienteSeleccionado.getClientePK() == null || clienteSeleccionado.getClientePK().getNoCliente() == null) {
			warn("Debe seleccionar un cliente para buscar");
		}else if (lineaSeleccionada == null) {
			warn("Debe seleccionar una linea de negocio para buscar");
		} else {
			facturasDeudas = facVentasServicio.getFacturasDeudasClientes(getCompania().getNoCia(),
					clienteSeleccionado.getClientePK().getNoCliente(), lineaSeleccionada);																  
		}
	}

	public void seleccionarCliente() {
		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null) {
			accionesDialog("DlgClientes", false);
		} else {
			warn("Debe seleccionar un cliente");
		}
	}

	public LazyDataModelClientes getClientes() {
		return clientes;
	}

	public void setClientes(LazyDataModelClientes clientes) {
		this.clientes = clientes;
	}

	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}

	public void setClienteSeleccionado(Cliente clienteSeleccionado) {
		this.clienteSeleccionado = clienteSeleccionado;
	}

	public List<Arccmd> getCuotasAAfectar() {
		return cuotasAAfectar;
	}

	public void setCuotasAAfectar(List<Arccmd> cuotasAAfectar) {
		this.cuotasAAfectar = cuotasAAfectar;
	}

	public List<CxcFacturaDeudaClienteDto> getFacturasDeudas() {
		return facturasDeudas;
	}

	public void setFacturasDeudas(List<CxcFacturaDeudaClienteDto> facturasDeudas) {
		this.facturasDeudas = facturasDeudas;
	}

	public List<LineaNegocio> getLineasDeNegocio() {
		return lineasDeNegocio;
	}

	public void setLineasDeNegocio(List<LineaNegocio> lineasDeNegocio) {
		this.lineasDeNegocio = lineasDeNegocio;
	}

	public String getLineaSeleccionada() {
		return lineaSeleccionada;
	}

	public void setLineaSeleccionada(String lineaSeleccionada) {
		this.lineaSeleccionada = lineaSeleccionada;
	}

	public List<Arccmd> getLetrasDeuda() {
		return letrasDeuda;
	}

	public void setLetrasDeuda(List<Arccmd> letrasDeuda) {
		this.letrasDeuda = letrasDeuda;
	}

	public Integer getMesesGracia() {
		return mesesGracia;
	}

	public void setMesesGracia(Integer mesesGracia) {
		this.mesesGracia = mesesGracia;
	}

	public List<CxcFacturaDeudaClienteDto> getFacturasSeleccionadas() {
		return facturasSeleccionadas;
	}

	public void setFacturasSeleccionadas(List<CxcFacturaDeudaClienteDto> facturasSeleccionadas) {
		this.facturasSeleccionadas = facturasSeleccionadas;
	}

 
 
}