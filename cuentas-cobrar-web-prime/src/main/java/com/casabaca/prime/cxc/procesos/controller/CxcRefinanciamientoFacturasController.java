package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;

import com.casabaca.common.CommonUtils;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.NumericUtils;
import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.model.Arccrefi;
import com.casabaca.common.ejb.model.ArccrefiDet;
import com.casabaca.common.ejb.model.ArccrefiDetPK;
import com.casabaca.common.ejb.model.ArccrefiPK;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.InvControl;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.FacVentasCabCp;
import com.casabaca.cxc.ejb.servicio.CxcRefinanciamientoFacturasServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;

@ViewScoped
@ManagedBean
public class CxcRefinanciamientoFacturasController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CxcRefinanciamientoFacturasController.class);
	private static final String TAB_CABECERA = "tabCabecera";
	private static final String TAB_REFINANCIACION = "tabRefinanciacion";
	private static final int SIZE = 10;

	@EJB(lookup = NombreJNDI.CXC_REFINANCIAMIENTO_FACTURAS_SERVICE)
	private CxcRefinanciamientoFacturasServiceLocal service;

	private String noCia;
	private String centro;
	private String noFisico;
	private String serieFisico;
	private String tipoDoc;
	private String filtroNoFisico;
	private String clasePrestamo;
	private String filtroCliente;
	private String usuarioSesion;

	private int tabActiveIndex;

	private BigDecimal cantProrrogas;
	private BigDecimal mOriginal;
	private BigDecimal saldo;
	private BigDecimal tasaFinanciacion;
	private BigDecimal tipoCambio;
	private BigDecimal moraDol1;
	private BigDecimal moraSuc1;
	private BigDecimal moraDol2;
	private BigDecimal moraSuc2;
	private BigDecimal tasaDesagio;
	private BigDecimal pDescuento;
	private BigDecimal totalSaldo;
	private BigDecimal totalInteresesDescuento;
	private BigDecimal totalTotal;
	private BigDecimal totalVencido;
	private BigDecimal totalXVencer;
	private BigDecimal totalInteresMora;
	private BigDecimal totalDescuento;
	private BigDecimal totalSaldoRefinanciar;

	private Date fechaSistema;
	private Date fechaVenceOriginal;
	private Date fecha;

	private InvControl control;
	private Cliente cliente;
	private Arccrefi arccrefi;

	private List<String[]> agencias;
	private List<Object[]> listaFacturas;
	private List<Arccmd> detalle;
	private List<Object[]> listaEntidadesFinancieras;
	private List<Cliente> listaClientes;
	private List<ArccrefiDet> detalleGuardar;

	private boolean readonly;
	private boolean edit;

	private boolean activaPantalla = true;
	private boolean activaDetalle;

	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		agencias = service.getAgencias(noCia);
		usuarioSesion = getUsuario().getUsuario();
		noFisico = null;
		serieFisico = null;
		cantProrrogas = null;
		cliente = null;
		clasePrestamo = null;
		fecha = null;
		fechaVenceOriginal = null;
		tasaFinanciacion = null;
		mOriginal = null;
		saldo = null;
		detalle = null;
		totalSaldo = null;
		totalInteresesDescuento = null;
		totalTotal = null;
		totalVencido = null;
		totalInteresMora = null;
		totalDescuento = null;
		totalXVencer = null;
		totalSaldoRefinanciar = null;

		if (agencias.isEmpty()) {
			super.error("Error al cargar agencias");
			logger.error("Error al cargar agencias");
			activaPantalla = false;
		}
		centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		control = service.getControl(noCia, centro);
		if (control != null)
			fechaSistema = control.getDiaProcesoCxc();
		else {
			super.error("No se pudo determinar la fecha del proceso");
			logger.error("No se pudo determinar la fecha del proceso");
			activaPantalla = false;
		}

		try {
			BigDecimal[] tasas = service.buscarTasas(noCia, "O");
			moraDol1 = tasas[0];
			moraSuc1 = tasas[1];
			moraDol2 = tasas[2];
			moraSuc2 = tasas[3];
			tasaDesagio = tasas[4];
			pDescuento = tasas[5];
		} catch (FindException e) {
			super.error("Error al buscar tasas");
			logger.error("Error al buscar tasas", e);
			activaPantalla = false;
		}

		try {
			tipoCambio = service.llamarFuncionTipoCambio("01", fechaSistema, fechaSistema, "C");
		} catch (FindException e) {
			super.error("Error al buscar tipo cambio");
			logger.error("Error al buscar tipo cambio", e);
			activaPantalla = false;
		}

		tabActiveIndex = 0;
		activaDetalle = false;
	}

	public void changeNoFisico() {
		if (noFisico == null) {
			super.error("Debe ingresar un valor valido de factura");
			logger.error("Debe ingresar un valor valido de factura");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:noFisico\").select()");
			return;
		}
		filtroNoFisico = noFisico;
		cargarFacturas();
		if (listaFacturas.size() == 1)
			selectNoFisico(listaFacturas.get(0));
		else if (listaFacturas.size() == 0) {
			super.error("Numero de Factura no existe, por favor revise");
			logger.error("Numero de Factura no existe, por favor revise");
			selectNoFisico(null);
		} else
			super.accionesDialog("dlgFacturasW", true);
	}

	public void cargarFacturas() {
		try {
			listaFacturas = service.getFacturas(noCia, centro, filtroNoFisico);
		} catch (FindException e) {
			super.error("Error al cargar factura");
			logger.error("Error al cargar factura", e);
		}
	}

	public void selectNoFisico(Object[] arccmd) {
		if (arccmd != null) {
			noFisico = arccmd[0] != null ? arccmd[0].toString() : null;
			serieFisico = arccmd[1] != null ? arccmd[1].toString() : null;
			cantProrrogas = arccmd[2] != null ? new BigDecimal(arccmd[2].toString()) : null;
			changeCantProrrogas();
		} else {
			noFisico = null;
			serieFisico = null;
			cantProrrogas = null;
			mOriginal = null;
			saldo = null;
			fechaVenceOriginal = null;
			fecha = null;
			detalle = new ArrayList<Arccmd>();
			cliente = null;
			tasaFinanciacion = null;
			clasePrestamo = null;
			calcularTotales();
			activaDetalle = false;
			tabActiveIndex = 0;
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:noFisico\").select()");
		}
	}

	public void changeSerieFisico() {
		if (!CommonUtils.isValid(serieFisico)) {
			super.error("Debe ingresar un valor valido de linea");
			logger.error("Debe ingresar un valor valido de linea");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:serieFisico\").select()");
		}
	}

	public void changeCantProrrogas() {
		Object[] doc;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			doc = service.getDocumentoRefinanciamientoCredito();
			if (doc == null) {
				super.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito");
				logger.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito");
				super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:cantProrrogas_input\").select()");
				return;
			}
		} catch (FindException e) {
			super.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito");
			logger.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito", e);
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:cantProrrogas_input\").select()");
			return;
		}

		doc = null;
		try {
			doc = service.getDocumentoRefinanciamientoDebito();
			if (doc == null) {
				super.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito");
				logger.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito");
				super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:cantProrrogas_input\").select()");
				return;
			}
		} catch (FindException e) {
			super.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito");
			logger.error("No ha definido un documento de refinanciamiento o desagio para la nota de credito", e);
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:cantProrrogas_input\").select()");
			return;
		}

		if (cantProrrogas == null) {
			super.error("Debe ingresar un valor valido de secuencia");
			logger.error("Debe ingresar un valor valido de secuencia");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:cantProrrogas_input\").select()");
			return;
		}

		try {
			Object[] montoOriginal_Saldo = service.getMontoOriginal_Saldo(noCia, centro, noFisico, serieFisico,
					cantProrrogas);
			if (montoOriginal_Saldo != null) {
				mOriginal = montoOriginal_Saldo[0] != null ? new BigDecimal(montoOriginal_Saldo[0].toString()) : null;
				saldo = montoOriginal_Saldo[1] != null ? new BigDecimal(montoOriginal_Saldo[1].toString()) : null;
				fechaVenceOriginal = montoOriginal_Saldo[2] != null ? (Date) montoOriginal_Saldo[2] : null;
				fecha = montoOriginal_Saldo[3] != null ? (Date) montoOriginal_Saldo[3] : null;
			}
		} catch (FindException e) {
			e.printStackTrace();
		}

		try {
			detalle = service.getDetalleFacturaRefinanciamiento(noCia, centro, noFisico, serieFisico, cantProrrogas,
					(fecha != null ? sdf.parse(sdf.format(fecha)) : null));
			detalle.forEach(d -> {
				BigDecimal vInteresRefi;
				BigDecimal vAbono;
				d.setIntereses(NumericUtils.nvl(d.getIntereses()));

				d.setxVencer(NumericUtils.nvl(d.getxVencer()));
				d.setVencido(NumericUtils.nvl(d.getVencido()));
				d.setSaldo(NumericUtils.nvl(d.getSaldo()));
				d.setInteresesDescuento(NumericUtils.nvl(d.getInteresesDescuento()));
				d.setMora(NumericUtils.nvl(d.getMora()));
				d.setDescuentoTmp(NumericUtils.nvl(d.getDescuentoTmp()));
				d.setSaldoParcialTab(NumericUtils.nvl(d.getSaldoParcialTab()));
				d.setTotal(NumericUtils.nvl(d.getTotal()));

				BigDecimal valorPagado = BigDecimal.ZERO;
				d.setDias(FechaUtils.restarFechasAdvanced(fechaSistema, fechaVenceOriginal));
				if ("CI".equals(d.getClaseDocumento())) {
					d.setInteresesTab(d.getSaldo());
					d.setSaldoParcialTab(BigDecimal.ZERO);
					d.setCuotasInteres(d.getSaldo());
				} else if ("CA".equals(d.getClaseDocumento())) {
					d.setInteresesTab(BigDecimal.ZERO);
					d.setSaldoParcialTab(d.getSaldo());
				} else {
					if (d.getIntereses().compareTo(BigDecimal.ZERO) > 0) {
						valorPagado = NumericUtils.nvl(mOriginal).subtract(d.getSaldo());
						if (valorPagado.compareTo(d.getIntereses()) > 0)
							d.setSaldoParcialTab(d.getSaldo());
						else
							d.setSaldoParcialTab(d.getSaldoParcial());
					} else
						d.setSaldoParcialTab(d.getSaldo());
				}

				d.setInteresesTmp(d.getInteresesTab());
				d.setSaldoParcialTmp(d.getSaldoParcialTab());
				d.setInteres2(d.getIntereses());

				if (d.getFechaVenceOriginal().after(fechaSistema)) {
					d.setxVencer(d.getSaldo());
					if (!Arrays.asList("CI", "CA").contains(d.getClaseDocumento())) {

						try {
							d.setDescuentoTmp(service.calcularDescuento(d.getFechaVenceOriginal(), fechaSistema,
									d.getMOriginal(), d.getSaldo(), (d.getTasaFinanciacion().subtract(pDescuento)),
									d.getDescuentoTmp()));
						} catch (FindException e) {
							super.error("Error al calcular descuento");
							logger.error("Error al calcular descuento");
							return;
						}

						d.setInteresesDescuento(d.getDescuentoTmp().multiply(new BigDecimal(-1)));
						d.setInteresesDescuento(NumericUtils.nvl(d.getInteresesDescuento()));
						d.setCssInteresDescuento("visualDescuento");
					} else {
						d.setInteresesDescuento(BigDecimal.ZERO);
						d.setCssInteresDescuento("visualCampos");
					}
					d.setTotal(d.getSaldo().add(d.getInteresesDescuento()));
					d.setTotalNc(d.getTotal());
				} else {
					d.setVencido(d.getSaldo());
					if (!Arrays.asList("CI").contains(d.getClaseDocumento())) {
						vInteresRefi = d.getSaldo().multiply(d.getIntereses()).divide(d.getMOriginal());
						vAbono = d.getSaldo().subtract(vInteresRefi);
						try {
							d.setMora(service.calcularMora(d.getFechaVence(), fechaSistema, vAbono, moraDol1, moraDol2,
									d.getMora()));
						} catch (FindException e) {
							super.error("Error al calcular mora");
							logger.error("Error al calcular mora");
							return;
						}
						d.setInteresesDescuento(d.getMora());
						d.setCssInteresDescuento("visualMora");
					} else {
						d.setInteresesDescuento(BigDecimal.ZERO);
						d.setCssInteresDescuento("visualCampos");
					}
					d.setTotal(d.getSaldo().add(d.getInteresesDescuento()));
					d.setTotalNc(d.getTotal());
				}
			});
		} catch (FindException | ParseException e) {
			super.error("Error al cargar detalle");
			logger.error("Error al cargar detalle", e);
			return;
		}

		if (!detalle.isEmpty()) {
			try {
				cliente = service.getCliente(noCia, detalle.get(0).getGrupo(),
						Long.valueOf(detalle.get(0).getNoCliente()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (FindException e) {
				super.error("Error, no se encontró cliente");
				logger.error("Error, no se encontró cliente", e);
			}

			clasePrestamo = null;

			if (cantProrrogas.equals(BigDecimal.ONE))
				clasePrestamo = "ORIGINAL";
			else if (cantProrrogas.compareTo(BigDecimal.ONE) > 0) {
				if ("P".equals(detalle.get(0).getEstadoNegocio()))
					clasePrestamo = "DESAGIO";
				else
					clasePrestamo = "REFINANCIACION";
			}

			fecha = detalle.get(0).getFecha();
			tasaFinanciacion = detalle.get(0).getTasaFinanciacion();

			try {
				FacVentasCabCp factura = service.getFactura(noCia, centro, noFisico, serieFisico, cantProrrogas);
				if (factura != null) {
					arccrefi.setCedulaGarante(factura.getCedGarante());
					if (arccrefi.getCedulaGarante() != null) {
						Cliente cliente = service.getCliente(noCia, arccrefi.getCedulaGarante());
						if (cliente != null)
							arccrefi.setNomClienteGarante(cliente.getNombre());
					}
				}
			} catch (FindException e) {

			}

		}
		calcularTotales();
	}

	public void calcularTotales() {
		totalSaldo = BigDecimal.ZERO;
		totalInteresesDescuento = BigDecimal.ZERO;
		totalTotal = BigDecimal.ZERO;
		totalVencido = BigDecimal.ZERO;
		totalXVencer = BigDecimal.ZERO;
		totalDescuento = BigDecimal.ZERO;
		totalInteresMora = BigDecimal.ZERO;
		totalSaldoRefinanciar = BigDecimal.ZERO;
		detalle.forEach(d -> {
			totalSaldo = totalSaldo.add(d.getSaldo());
			totalInteresesDescuento = totalInteresesDescuento.add(d.getInteresesDescuento());
			totalTotal = totalTotal.add(d.getTotal());
			totalVencido = totalVencido.add(d.getVencido());
			totalXVencer = totalXVencer.add(d.getxVencer());
			totalInteresMora = totalInteresMora.add(d.getMora());
			totalDescuento = totalDescuento.add(d.getDescuentoTmp());
			totalSaldoRefinanciar = totalSaldoRefinanciar.add(d.getSaldoParcialTab());
		});
	}

	public void refinanciar() {

		if (detalle == null || detalle.isEmpty()) {
			super.error("Debe tener por lo menos un documento a considerarse en la refinanciacion");
			return;
		}

		arccrefi = new Arccrefi(new ArccrefiPK(noCia, centro, null));
		arccrefi.setCedulaDeudor(cliente.getCedula());
		arccrefi.setNomClienteDeudor(cliente.getNombre());
		arccrefi.setCedDeudorNuevo(cliente.getCedula());
		arccrefi.setNoClienteNuevo(cliente.getClientePK().getNoCliente());
		arccrefi.setNomClienteNuevo(cliente.getNombre());
		arccrefi.setNoCliente(cliente.getClientePK().getNoCliente());
		arccrefi.setGrupoNuevo(cliente.getClientePK().getGrupo());
		arccrefi.setIntMora(totalInteresMora);
		arccrefi.setIntDiferido(BigDecimal.ZERO);
		arccrefi.setOtrosCargos(BigDecimal.ZERO);
		arccrefi.setFechaVencimiento(FechaUtils.sumarNMeses(fechaSistema, 1));
		arccrefi.setSubtotal(totalSaldoRefinanciar);
		arccrefi.setPlazoMora(BigDecimal.ZERO);
		try {
			listaEntidadesFinancieras = service.getTiposEntidadFinanciera(noCia, serieFisico);
		} catch (FindException e) {
			super.error("Error al cargar tipos de entidades financieras");
			logger.error("Error al cargar tipos de entidades financieras", e);
			activaDetalle = false;
			tabActiveIndex = 0;
			return;
		}

		activaDetalle = true;
		tabActiveIndex = 1;
	}

	public void changeTab(TabChangeEvent event) {
		Tab tab = event.getTab();
		if (TAB_CABECERA.equals(tab.getId())) {
			activaDetalle = false;
		}
	}

	public void changePlazoMora() {
		BigDecimal dias = arccrefi.getPagosPorAno() != null
				? (new BigDecimal(12).divide(arccrefi.getPagosPorAno(), 0).multiply(new BigDecimal(30)))
				: BigDecimal.ZERO;
		arccrefi.setFechaPrimerVenc(FechaUtils.sumarNDias(arccrefi.getFechaVencimiento(), dias.intValue()));

		if (arccrefi.getFechaPrimerVenc().before(fechaSistema)) {
			arccrefi.setPlazoMora(null);
			super.error("La fecha del primer pago no puede ser menor a la fecha del dia");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoMora_input\").select()");
			return;
		}

		if (arccrefi.getTotalIntereses().compareTo(BigDecimal.ZERO) > 0) {
			if (arccrefi.getPlazoMora() == null || arccrefi.getPlazoMora().equals(BigDecimal.ZERO)) {
				super.error("El plazo debe ser mayor a 0, por favor verifique los datos");
				super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoMora_input\").select()");
				return;
			}
		}

	}

	public void changeTipoFinanciacion() {
		if (arccrefi.getTipoFinanciacion() != null) {
			Object[] item = listaEntidadesFinancieras.stream().filter(p -> p[0].equals(arccrefi.getTipoFinanciacion()))
					.collect(Collectors.toList()).get(0);
			arccrefi.setLineaFinanciacion(item[2] == null ? null : item[2].toString());
			arccrefi.setPlazoMaximo(item[3] == null ? null : new BigDecimal(item[3].toString()));
		} else {
			arccrefi.setLineaFinanciacion(null);
			arccrefi.setPlazoMaximo(null);
		}
	}

	public void changePagosPorAnio() {
		arccrefi.setPlazoCredito(null);
		arccrefi.setCuotaMensual(null);
	}

	public void changePlazoCredito() {
		if (arccrefi.getPlazoCredito() == null || arccrefi.getPlazoCredito().equals(BigDecimal.ZERO)) {
			super.error("Debe ingresar valor valido para el plazo");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoCredito_input\").select()");
			return;
		} else if (arccrefi.getPlazoCredito().compareTo(arccrefi.getPlazoMaximo()) > 0) {
			arccrefi.setPlazoCredito(null);
			super.error(
					"Plazo excede al valor de plazo maximo definido... consulte con el Gerente de Credito por favor");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoCredito_input\").select()");
			return;
		}
		BigDecimal campo = new BigDecimal(12).divide(arccrefi.getPagosPorAno(), 2);
		if (!arccrefi.getPlazoCredito().remainder(campo).equals(BigDecimal.ZERO)) {
			arccrefi.setPlazoCredito(null);
			super.error("No es compatible el plazo indicado con las cuotas por año indicadas");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoCredito_input\").select()");
			return;
		}

		arccrefi.setCuotaMensual(BigDecimal.ZERO);
		BigDecimal mes = new BigDecimal(12).divide(arccrefi.getPagosPorAno(), 0);
		if (arccrefi.getPlazoMora().compareTo(BigDecimal.ZERO) > 0) {
			mes = mes.add(arccrefi.getPlazoMora()).subtract(BigDecimal.ONE);
			arccrefi.setFechaPrimerVenc(FechaUtils.sumarNMeses(arccrefi.getFechaVencimiento(), mes.intValue()));
		} else
			arccrefi.setFechaPrimerVenc(FechaUtils.sumarNMeses(new Date(), mes.intValue()));

		try {
			arccrefi.setTasaFinancia(service.getTasaFinancia(noCia, arccrefi.getTipoFinanciacion(),
					arccrefi.getLineaFinanciacion(), arccrefi.getPlazoCredito()));
		} catch (FindException e) {
			super.error("Error al buscar tasa financiacion");
			logger.error("Error al buscar tasa financiacion", e);
		}

	}

	public boolean changeFechaVencimiento() {
		if (arccrefi.getFechaVencimiento() == null || arccrefi.getFechaVencimiento().before(fechaSistema)) {
			super.error("Fecha no puede ser menor a fecha de proceso");
			logger.error("Fecha no puede ser menor a fecha de proceso");
			return false;
		}
		return true;
	}

	public boolean changeFechaPrimerVencimiento() {
		if (arccrefi.getFechaPrimerVenc() == null || arccrefi.getFechaPrimerVenc().before(fechaSistema)) {
			super.error("La fecha del primer pago no puede ser menor a la fecha del dia");
			logger.error("La fecha del primer pago no puede ser menor a la fecha del dia");
			return false;
		}
		return true;
	}

	public void calcularAlicuota() {
		if (NumericUtils.nvl(arccrefi.getPagosPorAno()).equals(BigDecimal.ZERO)
				|| NumericUtils.nvl(arccrefi.getTasaFinancia()).compareTo(BigDecimal.ZERO) < 0
				|| NumericUtils.nvl(arccrefi.getPlazoCredito()).equals(BigDecimal.ZERO)) {
			super.error("Debe ingresar toda la informacion requerida para poder calcular la alicuota");
			return;
		}

		try {
			arccrefi.setCuotaMensual(service.ccCalculaAlicuota(arccrefi.getPagosPorAno(), arccrefi.getTasaFinancia(),
					arccrefi.getPlazoCredito(), arccrefi.getTotalRefinanciar()));
			BigDecimal campo = new BigDecimal(12).divide(arccrefi.getPagosPorAno(), 2);
			arccrefi.setPeriodo(arccrefi.getPlazoCredito().divide(campo, 2));
			arccrefi.setIntereses(arccrefi.getCuotaMensual().multiply(arccrefi.getPeriodo())
					.subtract(arccrefi.getTotalRefinanciar()));
			arccrefi.setIntereses(
					arccrefi.getIntereses().compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : arccrefi.getIntereses());

			if (arccrefi.getCuotaMensual().compareTo(BigDecimal.ZERO) <= 0) {
				super.error("El valor de la cuota esta incorrecta, no puede ser 0 o menor a 0, por favor revise");
				logger.error("El valor de la cuota esta incorrecta, no puede ser 0 o menor a 0, por favor revise");
				return;
			}
		} catch (FindException e) {
			super.error("Error al calcular cuota mensual");
			logger.error("Error al calcular cuota mensual", e);
			return;
		}

	}

	public void cargarClientes() {
		try {
			if (CommonUtils.isValid(filtroCliente) && filtroCliente.length() > 2)
				listaClientes = service.getClientes(noCia, filtroCliente);
		} catch (FindException e) {
			super.error("Error al cargar clientes");
			logger.error("Error al cargar clientes", e);
		}
	}

	public void validarCedulaDeudor() {
		Cliente cliente = null;
		if (arccrefi.getCedDeudorNuevo() != null) {
			try {
				cliente = service.getCliente(noCia, arccrefi.getCedulaDeudor());
			} catch (FindException e) {
				super.error("Cliente no existe");
				super.accionesDialog("dlgClientesW", true);
			}
		}

		selectCliente(cliente);
	}

	public void validarCedulaGarante() {
		Cliente cliente = null;
		if (arccrefi.getCedulaGarante() != null) {
			try {
				cliente = service.getCliente(noCia, arccrefi.getCedulaGarante());
			} catch (FindException e) {
				super.error("Cliente no existe");
				super.accionesDialog("dlgGarantesW", true);
			}
		}

		selectGarante(cliente);
	}

	public void selectCliente(Cliente cliente) {
		if (arccrefi != null && cliente != null) {
			arccrefi.setCedDeudorNuevo(cliente.getCedula());
			arccrefi.setNoClienteNuevo(cliente.getClientePK().getNoCliente());
			arccrefi.setNomClienteNuevo(cliente.getNombre());
			arccrefi.setGrupoNuevo(cliente.getClientePK().getGrupo());
		} else if (arccrefi != null) {
			arccrefi.setCedDeudorNuevo(null);
			arccrefi.setNoClienteNuevo(null);
			arccrefi.setNomClienteNuevo(null);
			arccrefi.setGrupoNuevo(null);
		}
	}

	public void selectGarante(Cliente cliente) {
		if (arccrefi != null && cliente != null) {
			arccrefi.setCedulaGarante(cliente.getCedula());
			arccrefi.setNomClienteGarante(cliente.getNombre());
		} else if (arccrefi != null) {
			arccrefi.setCedulaGarante(null);
			arccrefi.setNomClienteGarante(null);
		}
	}

	public void guardar() {

		if (arccrefi.getCedDeudorNuevo() == null || arccrefi.getNomClienteNuevo() == null) {
			super.error("Error, ingrese cliente deudor");
			return;
		}

		if (NumericUtils.nvl(arccrefi.getSubtotal()).compareTo(BigDecimal.ZERO) <= 0) {
			super.error("Valor a Refinanciar no puede ser ceros");
			return;
		}

		if (NumericUtils.nvl(arccrefi.getCuotaMensual()).compareTo(BigDecimal.ZERO) <= 0) {
			super.error("Valor de alicuota no puede ser ceros");
			return;
		}

		if (arccrefi.getFechaPrimerVenc().before(fechaSistema)) {
			arccrefi.setPlazoMora(null);
			super.error("La fecha del primer pago no puede ser menor a la fecha del dia");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoMora_input\").select()");
			return;
		}

		if (arccrefi.getTotalIntereses().compareTo(BigDecimal.ZERO) > 0) {
			if (arccrefi.getPlazoMora() == null || arccrefi.getPlazoMora().equals(BigDecimal.ZERO)) {
				super.error("El plazo debe ser mayor a 0, por favor verifique los datos");
				super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoMora_input\").select()");
				return;
			}
		}

		if (arccrefi.getTipoFinanciacion() == null) {
			super.error("Selecione tipo de Financiación");
			return;
		}

		if (arccrefi.getPlazoCredito() == null || arccrefi.getPlazoCredito().equals(BigDecimal.ZERO)) {
			super.error("Debe ingresar valor valido para el plazo");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoCredito_input\").select()");
			return;
		} else if (arccrefi.getPlazoCredito().compareTo(arccrefi.getPlazoMaximo()) > 0) {
			arccrefi.setPlazoCredito(null);
			super.error(
					"Plazo excede al valor de plazo maximo definido... consulte con el Gerente de Credito por favor");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoCredito_input\").select()");
			return;
		}
		BigDecimal campo = new BigDecimal(12).divide(arccrefi.getPagosPorAno(), 2);
		if (!arccrefi.getPlazoCredito().remainder(campo).equals(BigDecimal.ZERO)) {
			arccrefi.setPlazoCredito(null);
			super.error("No es compatible el plazo indicado con las cuotas por año indicadas");
			super.ejecutarJavascript("document.getElementById(\"idForm:pestanas:plazoCredito_input\").select()");
			return;
		}

		if (!changeFechaVencimiento())
			return;

		if (!changeFechaPrimerVencimiento())
			return;

		if (arccrefi.getCuotaMensual().compareTo(BigDecimal.ZERO) <= 0) {
			super.error("El valor de la cuota esta incorrecta, no puede ser 0 o menor a 0, por favor revise");
			return;
		}

		detalleGuardar = new ArrayList<ArccrefiDet>();
		detalle.forEach(d -> {
			ArccrefiDet det = new ArccrefiDet(new ArccrefiDetPK(noCia, centro, null, d.getId().getNoDocu()));
			det.setSaldo(d.getSaldo());
			det.setSaldoParcial(d.getSaldoParcial());
			det.setEstado(null);
			detalleGuardar.add(det);
		});

		arccrefi.setUsuario(usuarioSesion);
		arccrefi.setSvlicodi(new BigDecimal(serieFisico));
		arccrefi.setCodVendActual("0");
		arccrefi.setFechaElabora(new Date());
		arccrefi.setTotalGeneral(arccrefi.getSubtotal());
		arccrefi.setSaldoFinanciar(arccrefi.getTotalRefinanciar());
		arccrefi.setCentroFactura(centro);
		arccrefi.setNoFisico(noFisico);
		arccrefi.setSerieFisico(serieFisico);
		arccrefi.setCantProrrogas(cantProrrogas);
		arccrefi.setGrupo(detalle.get(0).getGrupo());

		try {
			service.guardar(arccrefi, detalleGuardar);
			super.info("Refinanciamiento generado");
			init();
		} catch (InsertException e) {
			super.error("Error al guardar refinanciamiento");
			logger.error("Error al guardar refinanciamiento", e);
		} catch (FindException e) {
			super.error("No se encuentra secuencia para refinanciamiento");
			logger.error("No se encuentra secuencia para refinanciamiento", e);
		}

	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public int getSize() {
		return SIZE;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public List<String[]> getAgencias() {
		return agencias;
	}

	public void setAgencias(List<String[]> agencias) {
		this.agencias = agencias;
	}

	public Date getFechaSistema() {
		return fechaSistema;
	}

	public void setFechaSistema(Date fechaSistema) {
		this.fechaSistema = fechaSistema;
	}

	public String getNoFisico() {
		return noFisico;
	}

	public void setNoFisico(String noFisico) {
		this.noFisico = noFisico;
	}

	public String getSerieFisico() {
		return serieFisico;
	}

	public void setSerieFisico(String serieFisico) {
		this.serieFisico = serieFisico;
	}

	public BigDecimal getCantProrrogas() {
		return cantProrrogas;
	}

	public void setCantProrrogas(BigDecimal cantProrrogas) {
		this.cantProrrogas = cantProrrogas;
	}

	public String getTipoDoc() {
		return tipoDoc;
	}

	public void setTipoDoc(String tipoDoc) {
		this.tipoDoc = tipoDoc;
	}

	public String getFiltroNoFisico() {
		return filtroNoFisico;
	}

	public void setFiltroNoFisico(String filtroNoFisico) {
		this.filtroNoFisico = filtroNoFisico;
	}

	public List<Object[]> getListaFacturas() {
		return listaFacturas;
	}

	public void setListaFacturas(List<Object[]> listaFacturas) {
		this.listaFacturas = listaFacturas;
	}

	public BigDecimal getmOriginal() {
		return mOriginal;
	}

	public void setmOriginal(BigDecimal mOriginal) {
		this.mOriginal = mOriginal;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public Date getFechaVenceOriginal() {
		return fechaVenceOriginal;
	}

	public void setFechaVenceOriginal(Date fechaVenceOriginal) {
		this.fechaVenceOriginal = fechaVenceOriginal;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public List<Arccmd> getDetalle() {
		return detalle;
	}

	public void setDetalle(List<Arccmd> detalle) {
		this.detalle = detalle;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public String getClasePrestamo() {
		return clasePrestamo;
	}

	public void setClasePrestamo(String clasePrestamo) {
		this.clasePrestamo = clasePrestamo;
	}

	public BigDecimal getTasaFinanciacion() {
		return tasaFinanciacion;
	}

	public void setTasaFinanciacion(BigDecimal tasaFinanciacion) {
		this.tasaFinanciacion = tasaFinanciacion;
	}

	public BigDecimal getTotalSaldo() {
		return totalSaldo;
	}

	public void setTotalSaldo(BigDecimal totalSaldo) {
		this.totalSaldo = totalSaldo;
	}

	public BigDecimal getTotalInteresesDescuento() {
		return totalInteresesDescuento;
	}

	public void setTotalInteresesDescuento(BigDecimal totalInteresesDescuento) {
		this.totalInteresesDescuento = totalInteresesDescuento;
	}

	public BigDecimal getTotalTotal() {
		return totalTotal;
	}

	public void setTotalTotal(BigDecimal totalTotal) {
		this.totalTotal = totalTotal;
	}

	public BigDecimal getTotalVencido() {
		return totalVencido;
	}

	public void setTotalVencido(BigDecimal totalVencido) {
		this.totalVencido = totalVencido;
	}

	public BigDecimal getTotalXVencer() {
		return totalXVencer;
	}

	public void setTotalXVencer(BigDecimal totalXVencer) {
		this.totalXVencer = totalXVencer;
	}

	public BigDecimal getTotalInteresMora() {
		return totalInteresMora;
	}

	public void setTotalInteresMora(BigDecimal totalInteresMora) {
		this.totalInteresMora = totalInteresMora;
	}

	public BigDecimal getTotalDescuento() {
		return totalDescuento;
	}

	public void setTotalDescuento(BigDecimal totalDescuento) {
		this.totalDescuento = totalDescuento;
	}

	public BigDecimal getTotalSaldoRefinanciar() {
		return totalSaldoRefinanciar;
	}

	public void setTotalSaldoRefinanciar(BigDecimal totalSaldoRefinanciar) {
		this.totalSaldoRefinanciar = totalSaldoRefinanciar;
	}

	public int getTabActiveIndex() {
		return tabActiveIndex;
	}

	public void setTabActiveIndex(int tabActiveIndex) {
		this.tabActiveIndex = tabActiveIndex;
	}

	public boolean isActivaPantalla() {
		return activaPantalla;
	}

	public void setActivaPantalla(boolean activaPantalla) {
		this.activaPantalla = activaPantalla;
	}

	public boolean isActivaDetalle() {
		return activaDetalle;
	}

	public void setActivaDetalle(boolean activaDetalle) {
		this.activaDetalle = activaDetalle;
	}

	public List<Object[]> getListaEntidadesFinancieras() {
		return listaEntidadesFinancieras;
	}

	public void setListaEntidadesFinancieras(List<Object[]> listaEntidadesFinancieras) {
		this.listaEntidadesFinancieras = listaEntidadesFinancieras;
	}

	public Arccrefi getArccrefi() {
		return arccrefi;
	}

	public void setArccrefi(Arccrefi arccrefi) {
		this.arccrefi = arccrefi;
	}

	public String getFiltroCliente() {
		return filtroCliente;
	}

	public void setFiltroCliente(String filtroCliente) {
		this.filtroCliente = filtroCliente;
	}

	public List<Cliente> getListaClientes() {
		return listaClientes;
	}

	public void setListaClientes(List<Cliente> listaClientes) {
		this.listaClientes = listaClientes;
	}
}
