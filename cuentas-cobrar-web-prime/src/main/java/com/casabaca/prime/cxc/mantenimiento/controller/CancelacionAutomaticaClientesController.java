package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.event.SelectEvent;

import com.casabaca.common.CommonUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ParamCab;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcLiqAnticipoPago;
import com.casabaca.cxc.ejb.modelo.CxcLiqDetallePago;
import com.casabaca.cxc.ejb.modelo.CxcLiqPago;
import com.casabaca.cxc.ejb.servicio.CxcLiqAnticipoPagoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcLiqDetallePagoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcLiqPagoServiceLocal;
import com.casabaca.cxc.ejb.util.EstadoSolicitudCredito;
import com.casabaca.exception.FindException;
import com.casabaca.nomina.ejb.servicio.NomCargoSevicioLocal;
import com.casabaca.prime.cxc.common.CommonController;

@ManagedBean
@ViewScoped
public class CancelacionAutomaticaClientesController extends CommonController implements Serializable {

	private static final long serialVersionUID = 695492081437376219L;
	
	static Logger log = Logger.getLogger(CancelacionAutomaticaClientesController.class.getName());

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_LIQ_PAGO_SERVICE_BEAN)
	private CxcLiqPagoServiceLocal cxcLiqPagoServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_LIQ_DETALLE_PAGO_SERVICE_BEAN)
	private CxcLiqDetallePagoServiceLocal CxcLiqDetallePagoServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_LIQ_ANTICIPO_PAGO_SERVICE_BEAN)
	private CxcLiqAnticipoPagoServiceLocal cxcLiqAnticipoPagoServiceLocal;

	@EJB(lookup = NombreJNDI.NOM_CARGO_SERVICIO_LOCAL)
	private NomCargoSevicioLocal nomCargoSevicioLocal;

	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabServiceLocal;

	private static final String PARAM_JEFE_COBRANZAS = "JEFCOB";
	// private static final String JEFE_DE_COBRANZAS = "6120"; // LG_BOADA
	// private static final String JEFE_DE_COBRANZAS = "5269";
	// private static final String JEFE_CREDITO_Y_COBRANZA_N1 = "5267";
	private static final String PAGINA_LIST = "/jsf/consultas/listCancelacionAutomaticaDiarios.xhtml";

	private Cliente cliente;
	private CxcLiqPago liquidacionPago;
	private List<CxcLiqPago> listLiquidacionPago;
	private List<CxcLiqDetallePago> listLiquidacionDetallePago;
	private List<CxcLiqAnticipoPago> listAnticipoPago;
	private List<Cliente> listClientesDisponibles;
	private boolean usuarioAdministrador;
	private boolean seleccionarTodasFacturas;
	private boolean ingesarMemo;
	private Double totalDetalle;
	private Double totalAnticipos;
	private Double totalDetalleIngresado;
	private Double totalAnticiposIngresado;

	@PostConstruct
	public void init() {
		boolean nuevo = getRequestParameter("new") != null ? true : false;
		if (nuevo) {
			nuevoRegistro();
		} else {
			validarUsuarioAdministrador();
			listarLiquidacionesPago();
		}
	}

	private void validarUsuarioAdministrador() {

		try {
			ParamCab paramJefeCobranzas = this.paramCabServiceLocal.consultarParametro(getCompania().getNoCia(),
					PARAM_JEFE_COBRANZAS);

			String usuario = getUsuario().getUsuario();
			
			this.usuarioAdministrador = (usuario.equals(paramJefeCobranzas.getTexto1())
										|| usuario.equals(paramJefeCobranzas.getTexto2())
										|| usuario.equals(paramJefeCobranzas.getTexto3())
										|| usuario.equals(paramJefeCobranzas.getTexto4()));

		} catch (FindException e) {
			e.printStackTrace();
		}
	}

	private void listarLiquidacionesPago() {
		if (this.usuarioAdministrador) {
			this.listLiquidacionPago = this.cxcLiqPagoServiceLocal
					.listarLiquidacionesPago(EstadoSolicitudCredito.INGRESADO, EstadoSolicitudCredito.ENVIADO);
		} else {
			this.listLiquidacionPago = this.cxcLiqPagoServiceLocal
					.listarLiquidacionesPago(EstadoSolicitudCredito.INGRESADO);
		}
	}

	public void enviarAprobacion() {
		try {

			for (CxcLiqPago item : this.listLiquidacionPago) {
				if (item.isSeleccionado()
						&& !item.getEstado().equals(EstadoSolicitudCredito.INGRESADO.getAbreviacion())) {
					throw new Exception(
							"Solo se puede enviar registros en estado " + EstadoSolicitudCredito.INGRESADO.name());
				}
			}

			this.listLiquidacionPago.forEach(item -> {
				if (item.isSeleccionado()) {
					item.setEstado(EstadoSolicitudCredito.ENVIADO.getAbreviacion());
					this.cxcLiqPagoServiceLocal.merge(item);
				}
			});
			listarLiquidacionesPago();
			super.setMessageGrowl(FacesMessage.SEVERITY_INFO, "Exito", "Transaccion correcta.");
		} catch (Exception e) {
			super.setMessageGrowl(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
		}
	}

	public void nuevoRegistro() {
		this.liquidacionPago = new CxcLiqPago();
		this.liquidacionPago.setFecha(new Date());
		this.liquidacionPago.setNoCia(getCompania().getNoCia());
		this.liquidacionPago.setCentro(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
		this.liquidacionPago.setEstado(EstadoSolicitudCredito.INGRESADO.getAbreviacion());
		this.liquidacionPago.setUsuario(getUsuario().getUsuario());
		this.listLiquidacionDetallePago = new ArrayList<>();
		this.listAnticipoPago = new ArrayList<>();
	}

	public void listarClientes() {
		try {
			this.listClientesDisponibles = this.clienteServiceLocal.buscarClientesPorParametros(0, 25,
					getCompania().getNoCia(), this.cliente.getCedula(), this.cliente.getNombre());
		} catch (FindException e) {
			this.listClientesDisponibles = new ArrayList<>();
		}
	}

	public void listarLiquidacionesDetallePago() {
		this.listLiquidacionDetallePago = new ArrayList<>();
		this.listAnticipoPago = new ArrayList<>();
		this.liquidacionPago.setValorMemo(null);
		this.liquidacionPago.setCuentaContable(null);

		String noCia = getCompania().getNoCia();
		if (this.cliente != null) {
			this.listLiquidacionDetallePago.addAll(this.CxcLiqDetallePagoServiceLocal.listDetallePago(noCia,
					this.cliente.getClientePK().getNoCliente().toString()));
			this.listAnticipoPago.addAll(this.cxcLiqAnticipoPagoServiceLocal.listarAnticipos(noCia,
					this.cliente.getClientePK().getNoCliente().toString()));
		} else {
			super.setMessageGrowl(SEVERITY_INFO, "Atencion", "Debe seleccionar un cliente");
		}
		if (this.listLiquidacionDetallePago.isEmpty() && this.listAnticipoPago.isEmpty()) {
			super.setMessageGrowl(FacesMessage.SEVERITY_WARN, "Atencion", "La consulta no devolvio datos.");
		} else {
			calcularTotalDetalles(null);
			calcularTotalAnticipos(null);
		}
	}

	public String guardar() {
		Double totalEvaluar = null;
		calcularTotalDetalles(null);
		calcularTotalAnticipos(null);
		if (this.cliente == null) {
			super.setMessageGrowl(SEVERITY_INFO, "Atencion", "Debe seleccionar un cliente");
			return "#";
		}
		if (this.liquidacionPago.getValorMemo() != null && this.liquidacionPago.getValorMemo().doubleValue() > 0) {
			if (this.liquidacionPago.getCuentaContable() != null
					&& !this.liquidacionPago.getCuentaContable().equals("")) {
				totalEvaluar = this.totalAnticiposIngresado + this.liquidacionPago.getValorMemo().doubleValue();
			} else {
				super.setMessageGrowl(SEVERITY_WARN, "Advertencia", "Debe ingresar una cuenta contable.");
				return "#";
			}
		} else {
			totalEvaluar = this.totalAnticiposIngresado;
		}
		if ((this.totalDetalleIngresado != null && totalEvaluar != null) && (CommonUtils
				.redondear(this.totalDetalleIngresado, 2).equals(CommonUtils.redondear(totalEvaluar, 2)))) {
			try {
				this.liquidacionPago.setNoCliente(new BigDecimal(this.cliente.getClientePK().getNoCliente()));
				this.liquidacionPago.setCxcLiqDetallePagos(this.listLiquidacionDetallePago);
				this.liquidacionPago.setCxcLiqAnticipoPagos(this.listAnticipoPago);
				this.liquidacionPago = this.cxcLiqPagoServiceLocal.ejecutarCancelacion(this.liquidacionPago);
				super.setMessageGrowl(FacesMessage.SEVERITY_INFO, "Exito",
						"Numero de liquidacion generada [" + this.liquidacionPago.getSecuencia() + "]");
			} catch (Exception e) {
				super.setMessageGrowl(FacesMessage.SEVERITY_ERROR, "Advertencia",
						"Ocurrio un problema al ejecutar la transaccion [" + e.getMessage() + "]");
			}
		} else {
			super.setMessageGrowl(FacesMessage.SEVERITY_WARN, "Advertencia",
					"El total de los anticipos no coinciden con el total de los documentos.");
			return "#";
		}
		return PAGINA_LIST;
	}

	public void realizarAprobacion() {
		try {
			for (CxcLiqPago item : this.listLiquidacionPago) {
				if (item.isSeleccionado()
						&& !item.getEstado().equals(EstadoSolicitudCredito.ENVIADO.getAbreviacion())) {
					throw new Exception(
							"Solo se puede aprobar registros en estado " + EstadoSolicitudCredito.ENVIADO.name());
				}
			}

			boolean haySeleccion = false;
			for (CxcLiqPago item : this.listLiquidacionPago) {
				if (item.isSeleccionado()) {
					item.setEstado(EstadoSolicitudCredito.APROBADO.getAbreviacion());
					haySeleccion = true;
					this.cxcLiqPagoServiceLocal.generaDiarioAutomatico(item);
					this.cxcLiqPagoServiceLocal.merge(item);
				}
			}
			if (haySeleccion) {
				super.setMessageGrowl(FacesMessage.SEVERITY_INFO, "Exito", "Ejecucion correcta");
				listarLiquidacionesPago();
			} else
				super.setMessageGrowl(FacesMessage.SEVERITY_WARN, "Advertencia",
						"No ha seleccionado ninguna liquidacion");

		} catch (Exception e) {
			super.setMessageGrowl(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
		}
	}

	public void calcularTotalDetalles(CxcLiqDetallePago cxcLiqDetallePago) {
		int indiceDetalle = this.listLiquidacionDetallePago.indexOf(cxcLiqDetallePago);
		if (cxcLiqDetallePago != null && cxcLiqDetallePago.getTotalPago() != null) {
			if (cxcLiqDetallePago.getTotalPago().doubleValue() == 0.0D) {
				super.setMessageGrowl(SEVERITY_WARN, "Atencion", "El valor debe de ser mayor a cero");
				cxcLiqDetallePago.setTotalPago(null);
			} else if (cxcLiqDetallePago.getTotalPago().doubleValue() > cxcLiqDetallePago.getMonto().doubleValue()) {
				super.setMessageGrowl(SEVERITY_WARN, "Atencion",
						"El valor ingresado [" + cxcLiqDetallePago.getTotalPago().doubleValue()
								+ "] es mayor al esperado [" + cxcLiqDetallePago.getMonto().doubleValue() + "]");
				cxcLiqDetallePago.setTotalPago(null);
			}
			super.updateComponentFromId("growl");
		}
		this.totalDetalle = 0D;
		this.totalDetalleIngresado = 0D;
		this.listLiquidacionDetallePago.forEach(item -> {
			this.totalDetalle = this.totalDetalle + item.getMonto().doubleValue();
			this.totalDetalleIngresado = this.totalDetalleIngresado
					+ (item.getTotalPago() != null ? item.getTotalPago().doubleValue() : 0D);
		});
		// this.totalDetalleIngresado = 0D;
		// this.listLiquidacionDetallePago.forEach(item -> {
		// this.totalDetalleIngresado = this.totalDetalleIngresado
		// + (item.getTotalPago() != null ? item.getTotalPago().doubleValue() :
		// 0D);
		// });

		this.seleccionarTodasFacturas = (this.totalDetalle.equals(this.totalDetalleIngresado));

		int totFilasDetalle = (this.listLiquidacionDetallePago.size() - 1);
		super.updateComponentFromId("formPrincipal:idTblDetalleLiquidacionPago:" + indiceDetalle + ":idINValorDetalle");
		super.updateComponentFromId(
				"formPrincipal:idTblDetalleLiquidacionPago:" + totFilasDetalle + ":idOTtotalDetalle");
		super.updateComponentFromId(
				"formPrincipal:idTblDetalleLiquidacionPago:" + totFilasDetalle + ":idOTtotalDetalleIngresado");
		super.updateComponentFromId("formPrincipal:idTblDetalleLiquidacionPago:idSBCSeleccionTodasFacturas");
	}

	public void calcularTotalAnticipos(CxcLiqAnticipoPago cxcLiqAnticipoPago) {
		int indiceAnticipo = this.listAnticipoPago.indexOf(cxcLiqAnticipoPago);
		if (cxcLiqAnticipoPago != null && cxcLiqAnticipoPago.getMontoUtilizable() != null) {
			if (cxcLiqAnticipoPago.getMontoUtilizable().doubleValue() == 0.0D) {
				super.setMessageGrowl(SEVERITY_WARN, "Atencion", "El valor debe de ser mayor a cero");
				cxcLiqAnticipoPago.setMontoUtilizable(null);
			} else if (cxcLiqAnticipoPago.getMontoUtilizable().doubleValue() > cxcLiqAnticipoPago.getMonto()
					.doubleValue()) {
				super.setMessageGrowl(SEVERITY_WARN, "Atencion",
						"El valor ingresado [" + cxcLiqAnticipoPago.getMontoUtilizable().doubleValue()
								+ "] es mayor al esperado [" + cxcLiqAnticipoPago.getMonto().doubleValue() + "]");
				cxcLiqAnticipoPago.setMontoUtilizable(null);
			}
			super.updateComponentFromId("growl");
		}
		this.totalAnticipos = 0D;
		this.listAnticipoPago.forEach(item -> {
			this.totalAnticipos = this.totalAnticipos + item.getMonto().doubleValue();
		});
		this.totalAnticiposIngresado = 0D;
		this.listAnticipoPago.forEach(item -> {
			this.totalAnticiposIngresado = this.totalAnticiposIngresado
					+ (item.getMontoUtilizable() != null ? item.getMontoUtilizable().doubleValue() : 0D);
		});
		int totFilasAnticipo = (this.listAnticipoPago.size() - 1);
		super.updateComponentFromId("formPrincipal:idTblDTAnticipoPago:" + indiceAnticipo + ":idINmontoUtilizable");
		super.updateComponentFromId("formPrincipal:idTblDTAnticipoPago:" + totFilasAnticipo + ":idOTtotalAnticipos");
		super.updateComponentFromId(
				"formPrincipal:idTblDTAnticipoPago:" + totFilasAnticipo + ":idOTtotalAnticiposIngresado");
		super.updateComponentFromId("formPrincipal:idOLTotalMemoMasDetalle");

	}

	public void seleccionTodasFacturas() {
		for (CxcLiqDetallePago detalle : this.listLiquidacionDetallePago) {
			detalle.setTotalPago(this.seleccionarTodasFacturas ? detalle.getMonto() : null);
		}
		calcularTotalDetalles(null);
	}

	public void passSaldoAlValorFactura(CxcLiqDetallePago cxcLiqDetallePago) {
		int indice = this.listLiquidacionDetallePago.indexOf(cxcLiqDetallePago);
		cxcLiqDetallePago.setTotalPago(cxcLiqDetallePago.getTotalPago() == null ? cxcLiqDetallePago.getMonto() : null);
		calcularTotalDetalles(null);
		super.updateComponentFromId("formPrincipal:idTblDetalleLiquidacionPago:" + indice + ":idINValorDetalle");
	}

	public void passSaldoAlValorAnticipos(CxcLiqAnticipoPago cxcLiqDetallePago) {
		int indice = this.listAnticipoPago.indexOf(cxcLiqDetallePago);
		cxcLiqDetallePago.setMontoUtilizable(
				cxcLiqDetallePago.getMontoUtilizable() == null ? cxcLiqDetallePago.getMonto() : null);
		calcularTotalAnticipos(null);
		super.updateComponentFromId("formPrincipal:idTblDTAnticipoPago:" + indice + ":idINmontoUtilizable");
	}

	public void seleccionarFilaCliente(SelectEvent event) {
		this.cliente = (Cliente) event.getObject();
	}

	public void accionesClientes() {
		this.cliente = new Cliente();
	}

	public CxcLiqPago getLiquidacionPago() {
		return liquidacionPago;
	}

	public void setLiquidacionPago(CxcLiqPago liquidacionPago) {
		this.liquidacionPago = liquidacionPago;
	}

	public List<CxcLiqPago> getListLiquidacionPago() {
		return listLiquidacionPago;
	}

	public void setListLiquidacionPago(List<CxcLiqPago> listLiquidacionPago) {
		this.listLiquidacionPago = listLiquidacionPago;
	}

	public List<Cliente> getListClientesDisponibles() {
		return listClientesDisponibles;
	}

	public void setListClientesDisponibles(List<Cliente> listClientesDisponibles) {
		this.listClientesDisponibles = listClientesDisponibles;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<CxcLiqDetallePago> getListLiquidacionDetallePago() {
		return listLiquidacionDetallePago;
	}

	public void setListLiquidacionDetallePago(List<CxcLiqDetallePago> listLiquidacionDetallePago) {
		this.listLiquidacionDetallePago = listLiquidacionDetallePago;
	}

	public List<CxcLiqAnticipoPago> getListAnticipoPago() {
		return listAnticipoPago;
	}

	public void setListAnticipoPago(List<CxcLiqAnticipoPago> listAnticipoPago) {
		this.listAnticipoPago = listAnticipoPago;
	}

	public Double getTotalDetalle() {
		return totalDetalle;
	}

	public void setTotalDetalle(Double totalDetalle) {
		this.totalDetalle = totalDetalle;
	}

	public Double getTotalAnticipos() {
		return totalAnticipos;
	}

	public void setTotalAnticipos(Double totalAnticipos) {
		this.totalAnticipos = totalAnticipos;
	}

	public Double getTotalDetalleIngresado() {
		return totalDetalleIngresado;
	}

	public void setTotalDetalleIngresado(Double totalDetalleIngresado) {
		this.totalDetalleIngresado = totalDetalleIngresado;
	}

	public Double getTotalAnticiposIngresado() {
		return totalAnticiposIngresado;
	}

	public void setTotalAnticiposIngresado(Double totalAnticiposIngresado) {
		this.totalAnticiposIngresado = totalAnticiposIngresado;
	}

	public boolean isUsuarioAdministrador() {
		return usuarioAdministrador;
	}

	public void setUsuarioAdministrador(boolean usuarioAdministrador) {
		this.usuarioAdministrador = usuarioAdministrador;
	}

	public boolean isSeleccionarTodasFacturas() {
		return seleccionarTodasFacturas;
	}

	public void setSeleccionarTodasFacturas(boolean seleccionarTodasFacturas) {
		this.seleccionarTodasFacturas = seleccionarTodasFacturas;
	}

	public boolean isIngesarMemo() {
		return ingesarMemo;
	}

	public void setIngesarMemo(boolean ingesarMemo) {
		this.ingesarMemo = ingesarMemo;
	}

}