package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.component.tabview.Tab;
import org.primefaces.event.TabChangeEvent;

import com.casabaca.caja.ejb.modelo.Arcctd;
import com.casabaca.caja.ejb.service.ArcctdServiceLocal;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.NumericUtils;
import com.casabaca.common.ejb.model.Arccctd;
import com.casabaca.common.ejb.model.ArccdcScb;
import com.casabaca.common.ejb.model.ArccdcScbPK;
import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.model.ArccmdPK;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.model.ConPlantas;
import com.casabaca.common.ejb.model.Concepto;
import com.casabaca.common.ejb.model.GUniadmin;
import com.casabaca.common.ejb.model.InvControl;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcEntradaMovimientosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.ServiceException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.lazy.LDMEntradasMovimientos;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

@ViewScoped
@ManagedBean
public class CxcEntradaMovimientosController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CxcEntradaMovimientosController.class);
	private static final String TAB_CABECERA = "tabCabecera";
	private static final String TAB_DETALLE = "tabDetalle";
	private static final String REPORTE = "/cxc/cxcRepEntradaMov";
	private static final int SIZE = 10;

	@EJB(lookup = NombreJNDI.CXC_ENTRADA_MOVIMIENTOS_SERVICE)
	private CxcEntradaMovimientosServiceLocal service;

	@EJB(lookup = NombreJNDI.ARCCTD_SERVICE)
	private ArcctdServiceLocal arcctdService;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	private String noCia;
	private String centro;
	private String ruta;
	private String usuarioSesion;
	private String comportamiento;
	private String nombreMoneda;
	private String tipoMovimiento;
	private String filtroCliente;
	private String filtroVendedor;
	private String filtroCobrador;
	private String filtroUsuario;
	private String filtroPlanCta;
	private String filtroNombrePlanCta;
	private String filtroUniAdmin;

	private int tabActiveIndex;
	private boolean activaPantalla = true;
	private boolean activaDetalle;
	private boolean renderNegocio;
	private boolean renderMoneda;
	private boolean readonly = false;

	private InvControl control;
	private Arccmd arccmd;
	private ArccdcScb detalleSeleccionado;
	private Arccmd filtro;

	private List<Arcctd> tiposDocumento;
	private List<Cliente> clientes;
	private List<LineaNegocio> lineasNegocio;
	private List<String[]> vendedores;
	private List<String[]> cobradores;
	private List<Concepto> conceptos;
	private List<UsuarioSis> usuarios;
	private List<ArccdcScb> detalle;
	private List<ConPlantas> planCtas;
	private List<GUniadmin> uniAdmin;

	private LDMEntradasMovimientos model;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		ruta = "0000";
		usuarioSesion = getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario();
		control = service.getControl(noCia, centro);
		if (control.getId() == null) {
			activaPantalla = activaPantalla && false;
			super.error("El centro no se encuentra definido");
			return;
		}

		if (control.getDiaProcesoCxc() == null) {
			activaPantalla = activaPantalla && false;
			super.error("El centro no tiene definido un dia en proceso");
			return;
		}

		tabActiveIndex = 0;
		activaDetalle = false;
		validarInicio();

	}

	private void validarInicio() {
		if (super.getRequestParameter("comportamiento") != null)
			this.comportamiento = super.getRequestParameter("comportamiento");
		else
			this.comportamiento = "N";

		String noDocu = super.getRequestParameter("noDocu");
		if (noDocu != null) {
			if ("0".equals(noDocu))
				nuevaEntrada();
			else
				consultarEntrada(super.getRequestParameter("noDocu"));
			changeMoneda();
			cargarTiposDocumento();
			cargarLineasNegocio();
			cargarConceptos();
		} else {
			filtro = new Arccmd(new ArccmdPK(noCia, null));
			filtro.setArccmc(new Cliente());
			filtro.setNoLiq(comportamiento);
			filtro.setCentro(centro);
		}
	}

	public void buscarEntradas() {
		model = new LDMEntradasMovimientos(0, SIZE, filtro);
	}

	public void getUrlConsulta(Arccmd item) {
		String url = "/cxc-web-prime/jsf/procesos/cxcEntradaMovimientos/form.jsf?noDocu=" + item.getId().getNoDocu();
		ejecutarJavascript("openDuplicatedTab('" + url + "');");
	}

	public void getUrlNuevo() {
		String url = "/cxc-web-prime/jsf/procesos/cxcEntradaMovimientos/form.jsf?noDocu=0";
		ejecutarJavascript("openDuplicatedTab('" + url + "');");
	}

	private void consultarEntrada(String noDocu) {
		readonly = true;
		arccmd = service.getEntrada(new ArccmdPK(noCia, noDocu));
		try {
			detalle = service.getDetalle(arccmd.getId().getNoCia(), arccmd.getId().getNoDocu());
			calcularTotalDetalle();
		} catch (FindException e) {
			super.error("Error al obtener detalle");
			logger.error("Error al obtener detalle", e);
		}
	}

	private void nuevaEntrada() {
		readonly = false;
		arccmd = new Arccmd(new ArccmdPK(noCia, null));
		arccmd.setArccmc(new Cliente(new ClientePK()));
		arccmd.setCentro(centro);
		arccmd.setFecha(control.getDiaProcesoCxc());
		arccmd.setFechaDocumento(arccmd.getFecha());
		arccmd.setNegocioId("B");
		arccmd.setUsuarioCrea(usuarioSesion);
		arccmd.setSubtotal(BigDecimal.ZERO);
		arccmd.setExento(BigDecimal.ZERO);
		arccmd.setPeriodo(control.getAnoProceCxc().toString());
		arccmd.setGrupo("01");
		arccmd.setEstado("D");
		arccmd.setFechaDigitacion(new Date());
		arccmd.setOrigen("CC");
		arccmd.setRuta("0000");
		arccmd.setPeriLiq(arccmd.getPeriodo());

		renderNegocio = CommonConstants.CASABACA.equals(noCia);

		String[] monedas = service.getMoneda(noCia);
		monedas = monedas != null ? monedas : new String[] { "00", "00" };
		renderMoneda = !monedas[0].equals(monedas[1]);
		arccmd.setMonedaId(monedas[0]);
	}

	public void changeMoneda() {
		nombreMoneda = service.getNombreMoneda(noCia, arccmd.getMonedaId());
	}

	private void cargarTiposDocumento() {
		try {
			tiposDocumento = arcctdService.getTiposEntradaMovimientos(noCia);
		} catch (FindException e) {
			super.error("Error al cargar tipos de documento");
			logger.error("Error al cargar tipos de documento", e);
		}
	}

	public void selectTipoDoc() {
		tiposDocumento.forEach(td -> {
			if (arccmd.getTipoDoc().equals(td.getPk().getTipo())) {
				tipoMovimiento = td.getTipoMov();
				arccmd.setDescTipoDoc(td.getDescripcion());
			}
		});
	}

	public void buscarClientes() {
		if (null == this.filtroCliente || this.filtroCliente.length() < 4) {
			error("Por favor ingrese mínimo 4 caracteres");
			return;
		}

		try {
			this.clientes = service.getClientes(noCia, filtroCliente);
		} catch (FindException e) {
			super.error("Error al consultar clientes");
			logger.error("Error al consultar clientes", e);
		}
	}

	public void selectCliente(Cliente cliente) {
		arccmd.setNoCliente(cliente.getClientePK().getNoCliente().toString());
		arccmd.setArccmc(cliente);
		arccmd.setTipoCambio(new BigDecimal(25000));
		arccmd.setMoneda("D");

		if (CommonConstants.MANSUERA != noCia) {
			if (arccmd.getArccmc().getFCierre() != null) {
				super.error("El cliente fue inactivado el " + sdf.format(arccmd.getArccmc().getFCierre())
						+ ". Por favor dirigirse al departamento de Cobranzas");
				arccmd.setArccmc(new Cliente(new ClientePK()));
				return;
			}

		}

		try {
			if (service.isClienteVendedorInactivo(noCia, arccmd.getGrupo(),
					arccmd.getArccmc().getClientePK().getNoCliente().toString())) {
				super.error("El cliente corresponde a un vendedor que se encuentra inactivo");
				return;
			}
		} catch (FindException e) {
			super.error("Error al validar si el cliente es un vendedor inactivo");
			logger.error("Error al validar si el cliente es un vendedor inactivo", e);
			arccmd.setArccmc(new Cliente(new ClientePK()));
		}

		try {
			Double saldo = service.getSaldoChequesProtestados(noCia, arccmd.getGrupo(), arccmd.getNoCliente());
			if (saldo > 0) {
				super.error("El cliente tiene cheques protestados");
				return;
			}
		} catch (FindException e) {
			super.error("Error al consultar saldo de cliente");
			logger.error("Error al consultar saldo de cliente", e);
			arccmd.setArccmc(new Cliente(new ClientePK()));
			arccmd.setNoCliente(null);
		}

	}

	public void validarClientePorCodigo() {
		try {
			Cliente cliente = service.getClienteCodigo(noCia, Long.parseLong(arccmd.getNoCliente()));
			selectCliente(cliente);
		} catch (FindException e) {
			arccmd.setArccmc(new Cliente(new ClientePK()));
			arccmd.setNoCliente(null);
			super.error("Cliente no existe");
		}
	}

	public void validarClientePorCedula() {
		try {
			Cliente cliente = service.getClienteCedula(noCia, arccmd.getArccmc().getCedula());
			selectCliente(cliente);
		} catch (FindException e) {
			arccmd.setArccmc(new Cliente(new ClientePK()));
			arccmd.setNoCliente(null);
			super.error("Cliente no existe");
		}
	}

	public boolean changeFechaDocumento() {
		if (arccmd.getFechaDocumento() == null) {
			super.error("Ingrese fecha documento");
			return false;
		}
		if (arccmd.getFechaDocumento().after(arccmd.getFecha())) {
			super.error("La fecha del documento no debe ser mayor a la fecha de registro CXC");
			arccmd.setFechaDocumento(null);
			return false;
		}
		return true;
	}

	public boolean changeFechaVence() {
		if (arccmd.getFechaVence() == null) {
			return true;
		}
		if (arccmd.getFechaVence().before(arccmd.getFechaDocumento())) {
			super.error("La fecha de vencimiento debe ser mayor o igual a la del documento");
			arccmd.setFechaVence(null);
			return false;
		}
		arccmd.setFechaVenceOriginal(arccmd.getFechaVence());
		return true;
	}

	public void cargarLineasNegocio() {
		try {
			lineasNegocio = service.getLineas(noCia);
		} catch (FindException e) {
			super.error("Error al cargar líneas de negocio");
			logger.error("Error al cargar líneas de negocio", e);
		}
	}

	public void selectLineaNegocio() {
		lineasNegocio.forEach(l -> {
			if (arccmd.getSerieFisico().equals(l.getLineaNegocioPK().getNoLinea())) {
				arccmd.setDescLinea(l.getDescripcion());
			}
		});
	}

	public void buscarVendedores() {
		try {
			vendedores = service.getVendedoresEntradaMovimientos(noCia, ruta, filtroVendedor);
		} catch (FindException e) {
			super.error("Error al buscar vendedores");
			logger.error("Error al buscar vendedores", e);
		}
	}

	public void selectVendedor(Object[] vendedor) {
		arccmd.setNoAgente(vendedor[0].toString());
		arccmd.setNomVendedor(vendedor[1].toString());
	}

	public void validarVendedor() {
		if (!CommonUtils.isValid(arccmd.getNoAgente())) {
			arccmd.setNoAgente(null);
			arccmd.setNomVendedor(null);
			return;
		}
		filtroVendedor = arccmd.getNoAgente();
		buscarVendedores();
		if (vendedores.isEmpty()) {
			arccmd.setNoAgente(null);
			super.error("Vendedor no existe");
			return;
		} else if (vendedores.size() == 1) {
			selectVendedor(vendedores.get(0));
			return;
		} else if (vendedores.size() > 1) {
			super.accionesDialog("dlgVendedores", true);
		}
	}

	public void buscarCobradores() {
		try {
			cobradores = service.getCobradoresEntradaMovimientos(noCia, filtroCobrador);
		} catch (FindException e) {
			super.error("Error al buscar cobradores");
			logger.error("Error al buscar cobradores", e);
		}
	}

	public void selectCobrador(Object[] cobrador) {
		arccmd.setCobrador(cobrador[0].toString());
		arccmd.setNomCobrador(cobrador[1].toString());
	}

	public void validarCobrador() {
		if (!CommonUtils.isValid(arccmd.getCobrador())) {
			arccmd.setCobrador(null);
			arccmd.setNomCobrador(null);
			return;
		}
		filtroCobrador = arccmd.getCobrador();
		buscarCobradores();
		if (cobradores.isEmpty()) {
			arccmd.setCobrador(null);
			super.error("Cobrador no existe");
			return;
		} else if (cobradores.size() == 1) {
			selectCobrador(cobradores.get(0));
			return;
		} else if (cobradores.size() > 1) {
			super.accionesDialog("dlgCobradores", true);
		}
	}

	public void cargarConceptos() {
		try {
			conceptos = service.getConceptos(noCia);
		} catch (FindException e) {
			super.error("Error al buscar conceptos");
			logger.error("Error al buscar conceptos", e);
		}
	}

	public void buscarUsuarios() {
		try {
			usuarios = service.getUsuarios(filtroUsuario);
		} catch (FindException e) {
			super.error("Error al buscar cobradores");
			logger.error("Error al buscar cobradores", e);
		}
	}

	public void selectUsuario(UsuarioSis usuario) {
		arccmd.setUsuarioSolicita(usuario.getUsuario());
	}

	public void validarUsuario() {
		if (!CommonUtils.isValid(arccmd.getUsuarioSolicita())) {
			arccmd.setUsuarioSolicita(null);
			return;
		}
		filtroUsuario = arccmd.getUsuarioSolicita();
		buscarUsuarios();
		if (usuarios.isEmpty()) {
			arccmd.setUsuarioSolicita(null);
			super.error("Usuario no existe");
			return;
		} else if (usuarios.size() == 1) {
			selectUsuario(usuarios.get(0));
			return;
		} else if (usuarios.size() > 1) {
			super.accionesDialog("dlgUsuarios", true);
		}
	}

	public void changeSubtotal() {
		calculaTotalDoc();
		arccmd.setSubtotalOld(arccmd.getSubtotal());
	}

	public void changeExento() {
		Double subtotal = NumericUtils.nvl(arccmd.getSubtotal()).doubleValue();
		Double exento = NumericUtils.nvl(arccmd.getExento()).doubleValue();
		if (subtotal + exento <= 0.0) {
			super.error("El monto del documento debe ser mayor a cero");
			return;
		}
		calculaTotalDoc();
		arccmd.setExentoOld(arccmd.getExento());
	}

	public void calculaTotalDoc() {
		Double subtotal = arccmd.getSubtotal().doubleValue();
		Double exento = arccmd.getExento().doubleValue();
		Double saldo = NumericUtils.nvl(subtotal) + NumericUtils.nvl(exento);
		arccmd.setSaldo(new BigDecimal(saldo));
	}

	public void cargarMovimientos() {

	}

	public void contabilizar() {

		if (!CommonUtils.isValid(arccmd.getTipoDoc())) {
			super.error("Seleccione Tipo Doc");
			return;
		}

		if (!CommonUtils.isValid(arccmd.getNoCliente())) {
			super.error("Ingrese cliente");
			return;
		}

		if (!changeFechaDocumento() || !changeFechaVence()) {
			return;
		}

		if (!CommonUtils.isValid(arccmd.getSerieFisico())) {
			super.error("Seleccione Línea de negocio");
			return;
		}

		if (!CommonUtils.isValid(arccmd.getNoAgente())) {
			super.error("Ingrese vendedor");
			return;
		}

		if (!CommonUtils.isValid(arccmd.getConcepto())) {
			super.error("Seleccione concepto");
			return;
		}

		if (!CommonUtils.isValid(arccmd.getCobrador())) {
			super.error("Ingrese cobrador");
			return;
		}

		if (!CommonUtils.isValid(arccmd.getUsuarioSolicita())) {
			super.error("Ingrese Usuario Solicita");
			return;
		}

		Double saldo = NumericUtils.nvl(arccmd.getSaldo()).doubleValue();
		if (saldo <= 0.0) {
			super.error("El monto no puede ser menor ni igual que cero");
			return;
		}

		arccmd.setTotalDebitos(BigDecimal.ZERO);
		arccmd.setTotalCreditos(BigDecimal.ZERO);

		cargarDetalle();

		activaDetalle = true;
		tabActiveIndex = 1;
	}

	public void changeTab(TabChangeEvent event) {
		Tab tab = event.getTab();
		if (TAB_CABECERA.equals(tab.getId())) {
			activaDetalle = false;
		}
	}

	public ArccdcScb nuevoDetalle() {
		ArccdcScbPK pk = new ArccdcScbPK();
		pk.setNoCia(noCia);
		pk.setTipo("D");
		pk.setTipoDoc(arccmd.getTipoDoc());

		ArccdcScb det = new ArccdcScb();
		det.setId(pk);
		det.setTipoCambio(arccmd.getTipoCambio());
		det.setIndCon("P");
		det.setCentro(arccmd.getCentro());
		det.setMontoDol(BigDecimal.ZERO);
		det.setRuta(arccmd.getRuta());
		det.setPeriodo(arccmd.getPeriodo());
		det.setNoCliente(Long.parseLong(arccmd.getNoCliente()));
		det.setGrupo(arccmd.getGrupo());

		return det;
	}

	public void cargarDetalle() {
		detalle = new ArrayList<ArccdcScb>();
		Arccctd arccctd = null;
		try {
			arccctd = service.cctraeCuentasContablesScb(noCia, arccmd.getGrupo(), arccmd.getTipoDoc());
			if (arccctd == null) {
				super.error("No existe la cuenta de clientes para el documento");
				return;
			}
		} catch (FindException e) {
			super.error("Error al cargar cuentas contables");
		}

		if (arccctd.getCtaClienteScb() != null)
			detalle.add(getDetalle(arccctd, false));

		if (arccctd.getCtaContrapartidaScb() != null) {
			detalle.add(getDetalle(arccctd, true));
		}

		calcularTotalDetalle();

	}

	private ArccdcScb getDetalle(Arccctd arccctd, boolean contrapartida) {
		String centroCostos = null;
		ArccdcScb det = nuevoDetalle();
		ArccdcScbPK pk = det.getId();

		pk.setCtaconId(contrapartida ? arccctd.getCtaContrapartidaScb() : arccctd.getCtaClienteScb());
		if (contrapartida)
			pk.setTipo("C".equals(tipoMovimiento) ? "D" : "C");
		else
			pk.setTipo(tipoMovimiento);

		try {
			centroCostos = service.generaCcScb(noCia,
					(contrapartida ? arccctd.getCtaContrapartidaScb() : arccctd.getCtaClienteScb()),
					arccmd.getNegocioId(), "CXC", centro);
			if (centroCostos != null && centroCostos.contains("ERROR")) {
				super.error(centroCostos);
				return null;
			}
		} catch (FindException e) {
			super.error("Error al cargar centro de costos");
			logger.error("Error al cargar centro de costos", e);
		}

		det.setId(pk);
		det.setMontoDol(arccmd.getSaldo());
		det.setTipoCambio(arccmd.getTipoCambio());
		det.setMonto(new BigDecimal(det.getMontoDol().doubleValue() * det.getTipoCambio().doubleValue()));
		det.setMontoOld(det.getMontoDol());

		List<ConPlantas> planCtas = service.getPlanCuentas(noCia, pk.getCtaconId(), null);
		selectPlanCta(planCtas.stream().filter(p -> p.getConPlatasPk().getCtaconId().equals(pk.getCtaconId()))
				.collect(Collectors.toList()).get(0), det);

		det.setUniadminId(centroCostos);

		List<GUniadmin> uniAdmin = service.getUniAdmin(noCia, pk.getCtaconId(), det.getUniadminId());

		selectUniAdmin(uniAdmin.stream().filter(g -> g.getPk().getUniAdminId().equals(det.getUniadminId()))
				.collect(Collectors.toList()).get(0), det);

		return det;
	}

	public void calcularTotalDetalle() {
		arccmd.setTotalDebitos(BigDecimal.ZERO);
		arccmd.setTotalCreditos(BigDecimal.ZERO);

		for (ArccdcScb det : detalle) {
			Double totDeb = arccmd.getTotalDebitos().doubleValue();
			Double totCre = arccmd.getTotalCreditos().doubleValue();
			Double montoDol = det.getMontoDol().doubleValue();

			if ("D".equals(det.getId().getTipo()))
				arccmd.setTotalDebitos(new BigDecimal(totDeb + montoDol));
			else
				arccmd.setTotalCreditos(new BigDecimal(totCre + montoDol));

			det.setMontoOld(det.getMontoDol());
		}

	}

	public void selectDetalleCtaContable(ArccdcScb detalle) {
		detalleSeleccionado = detalle;
		cargarPlanCtas();
		super.accionesDialog("dlgCtaContables", true);
	}

	public void selectDetalleUniAdmin(ArccdcScb detalle) {
		detalleSeleccionado = detalle;

		if (detalleSeleccionado.getId().getCtaconId() == null) {
			super.error("Ingrese cuenta contable");
			return;
		}

		cargarUniAdmin();
		super.accionesDialog("dlgUniAdmin", true);
	}

	public void cargarPlanCtas() {
		planCtas = service.getPlanCuentas(noCia, filtroPlanCta, filtroNombrePlanCta);
	}

	public void selectPlanCta(ConPlantas planCta, ArccdcScb detalle) {
		if (detalle == null)
			detalle = detalleSeleccionado;
		detalle.getId().setCtaconId(planCta.getConPlatasPk().getCtaconId());
		detalle.setNombreC(planCta.getNombre());
		detalle.setAceptaCc(planCta.getCencosn());
		detalle.setUniadminId(null);
		detalle.setDescripcionCentroCosto(null);
	}

	public void cargarUniAdmin() {
		uniAdmin = service.getUniAdmin(noCia, detalleSeleccionado.getId().getCtaconId(), filtroUniAdmin);
	}

	public void selectUniAdmin(GUniadmin uniAdmin, ArccdcScb detalle) {
		if (detalle == null)
			detalle = detalleSeleccionado;
		detalle.setUniadminId(uniAdmin.getPk().getUniAdminId());
		detalle.setDescripcionCentroCosto(uniAdmin.getNombre());
	}

	public void validarCtaCon(ArccdcScb detalle) {
		detalleSeleccionado = detalle;
		filtroPlanCta = detalle.getId().getCtaconId();
		cargarPlanCtas();
		if (planCtas.size() == 1)
			selectPlanCta(planCtas.get(0), detalle);
		else
			super.accionesDialog("dlgCtaContables", true);
	}

	public void validarUniAdmin(ArccdcScb detalle) {
		detalleSeleccionado = detalle;
		filtroUniAdmin = detalle.getUniadminId();
		cargarUniAdmin();
		if (uniAdmin.size() == 1)
			selectUniAdmin(uniAdmin.get(0), detalle);
		else
			super.accionesDialog("dlgUniAdmin", true);
	}

	public void changeMonto(ArccdcScb detalle) {
		detalleSeleccionado = detalle;
		if (detalle.getMontoDol() == null)
			detalle.setMontoDol(BigDecimal.ZERO);
		if (detalle.getMontoDol().doubleValue() <= 0.0) {
			detalle.setMontoDol(detalle.getMontoOld());
			super.error("El monto debe ser mayor que cero");
			return;
		}
		detalle.setMonto(new BigDecimal(detalle.getMontoDol().doubleValue() * detalle.getTipoCambio().doubleValue()));
		calcularTotalDetalle();
	}

	public void addDetalle() {
		detalle.add(nuevoDetalle());
	}

	public void guardar() {
		arccmd.setTotalDb(arccmd.getTotalDebitos());
		arccmd.setTotalCr(arccmd.getTotalCreditos());

		if (arccmd.getTotalDebitos() != BigDecimal.ZERO) {
			if (verificaDebitoCredito()) {
				if (validarDetalle()) {
					try {
						service.guardar(arccmd, detalle, tipoMovimiento);
						super.info("Documento procesado satisfactoriamente numero: " + arccmd.getNoFisico()
								+ " Para la linea: " + arccmd.getDescLinea());
						postInsert();
						envioMail();
					} catch (ServiceException e) {
						super.error("Error al guardar movimiento");
						super.error(e.getMessage());
						logger.error("Error al guardar movimiento", e);
					}
				}
			}
		}
	}

	private boolean verificaDebitoCredito() {
		Double saldo = CommonUtils.redondear(arccmd.getSaldo().doubleValue(), 2);
		Double totalDebito = CommonUtils.redondear(arccmd.getTotalDebitos().doubleValue(), 2);
		Double totalCredito = CommonUtils.redondear(arccmd.getTotalCreditos().doubleValue(), 2);
		if (totalDebito < saldo) {
			super.error("El total del detalle contable es distinto al monto del documento");
			return false;
		} else if (!totalDebito.equals(totalCredito)) {
			super.error("Debitos y Creditos no cuadran");
			return false;
		}
		return true;
	}

	private boolean validarDetalle() {
		if (detalle == null || detalle.isEmpty()) {
			super.error("No existe ningún registro en el detalle");
			return false;
		}

		for (ArccdcScb det : detalle) {
			if (det.getId().getCtaconId() == null || det.getNombreC() == null) {
				super.error("No se puede realizar el ingreso ya que no ha definido la contabilización");
				return false;
			}

			if (det.getUniadminId() == null || det.getDescripcionCentroCosto() == null) {
				super.error("No se puede realizar el ingreso ya que no ha definido centro de costos");
				return false;
			}
		}

		return true;
	}

	private void postInsert() {
		super.accionesDialog("dlgImpresion", true);
	}

	public void imprimirSi() {
		imprimirDocumento();
	}

	public void imprimirNo() {
		super.ejecutarJavascript("close();");
	}

	public void imprimirDocumento() {
		Map<String, Object> parameters = new HashMap<>();
		setParameters(parameters);
		super.ejecutarJavascript("openDuplicatedTab('"
				+ super.callJasperReport(REPORTE, parameters, CommonConstants.OUTPUT_PDF) + "');");
		init();
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("p_centrodoc", arccmd.getCentro());
		parameters.put("p_nodocu", arccmd.getId().getNoDocu());
		parameters.put("p_tipodoc", arccmd.getTipoDoc());
	}

	private void envioMail() {
		try {

			String subject = "Solicitud Creación de documento: " + arccmd.getNoFisico();

			StringBuffer body = new StringBuffer();
			body.append("Solicitud Creación de documento: " + arccmd.getNoFisico() + " esta realizada Fecha: "
					+ sdf.format(arccmd.getFecha()));
			body.append("\n\n");
			body.append("Se creó el documento " + arccmd.getDescTipoDoc());
			body.append("\n");
			body.append("Cliente: " + arccmd.getArccmc().getCedula() + " Nombre: " + arccmd.getArccmc().getNombre());
			body.append("\n");
			body.append("Línea de Negocio: " + arccmd.getDescLinea() + " Valor: " + arccmd.getSaldo());
			body.append("\n");
			body.append("Observación: " + arccmd.getObservacion());
			String mailOrigen = obtenerMailUsuario(usuarioSesion);
			String mailDestino = obtenerMailUsuario(arccmd.getUsuarioSolicita());

			Runnable task = new Runnable() {
				public void run() {
					try {
						mailService.sendEmail(mailOrigen, mailDestino, subject, body.toString());
						logger.info(
								"Correo enviado, entrada movimiento: " + arccmd.getNoFisico() + " a: " + mailDestino);
					} catch (GeneralException e) {
						logger.error("Error al enviar correo de entrada movimiento: " + arccmd.getNoFisico() + " a: "
								+ mailDestino, e);
					}
				}
			};
			new Thread(task, this.getClass().getName()).start();

		} catch (Exception e) {
			super.error("Error al enviar correo");
			logger.error(e.getMessage(), e);
		}
	}

	private String obtenerMailUsuario(String usuario) {
		String mail = null;
		try {
			mail = usuarioSisService.mailUsuario(usuario);
		} catch (FindException e) {
			e.printStackTrace();
		}
		return mail;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public InvControl getControl() {
		return control;
	}

	public void setControl(InvControl control) {
		this.control = control;
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

	public String getNombreMoneda() {
		return nombreMoneda;
	}

	public void setNombreMoneda(String nombreMoneda) {
		this.nombreMoneda = nombreMoneda;
	}

	public boolean isRenderNegocio() {
		return renderNegocio;
	}

	public void setRenderNegocio(boolean renderNegocio) {
		this.renderNegocio = renderNegocio;
	}

	public boolean isRenderMoneda() {
		return renderMoneda;
	}

	public void setRenderMoneda(boolean renderMoneda) {
		this.renderMoneda = renderMoneda;
	}

	public Arccmd getArccmd() {
		return arccmd;
	}

	public void setArccmd(Arccmd arccmd) {
		this.arccmd = arccmd;
	}

	public List<Arcctd> getTiposDocumento() {
		return tiposDocumento;
	}

	public void setTiposDocumento(List<Arcctd> tiposDocumento) {
		this.tiposDocumento = tiposDocumento;
	}

	public String getFiltroCliente() {
		return filtroCliente;
	}

	public void setFiltroCliente(String filtroCliente) {
		this.filtroCliente = filtroCliente;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	public List<LineaNegocio> getLineasNegocio() {
		return lineasNegocio;
	}

	public void setLineasNegocio(List<LineaNegocio> lineasNegocio) {
		this.lineasNegocio = lineasNegocio;
	}

	public String getFiltroVendedor() {
		return filtroVendedor;
	}

	public void setFiltroVendedor(String filtroVendedor) {
		this.filtroVendedor = filtroVendedor;
	}

	public String getFiltroCobrador() {
		return filtroCobrador;
	}

	public void setFiltroCobrador(String filtroCobrador) {
		this.filtroCobrador = filtroCobrador;
	}

	public List<String[]> getVendedores() {
		return vendedores;
	}

	public void setVendedores(List<String[]> vendedores) {
		this.vendedores = vendedores;
	}

	public List<String[]> getCobradores() {
		return cobradores;
	}

	public void setCobradores(List<String[]> cobradores) {
		this.cobradores = cobradores;
	}

	public List<Concepto> getConceptos() {
		return conceptos;
	}

	public void setConceptos(List<Concepto> conceptos) {
		this.conceptos = conceptos;
	}

	public String getFiltroUsuario() {
		return filtroUsuario;
	}

	public void setFiltroUsuario(String filtroUsuario) {
		this.filtroUsuario = filtroUsuario;
	}

	public List<UsuarioSis> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<UsuarioSis> usuarios) {
		this.usuarios = usuarios;
	}

	public String getUsuarioSesion() {
		return usuarioSesion;
	}

	public void setUsuarioSesion(String usuarioSesion) {
		this.usuarioSesion = usuarioSesion;
	}

	public List<ArccdcScb> getDetalle() {
		return detalle;
	}

	public void setDetalle(List<ArccdcScb> detalle) {
		this.detalle = detalle;
	}

	public String getFiltroPlanCta() {
		return filtroPlanCta;
	}

	public void setFiltroPlanCta(String filtroPlanCta) {
		this.filtroPlanCta = filtroPlanCta;
	}

	public String getFiltroNombrePlanCta() {
		return filtroNombrePlanCta;
	}

	public void setFiltroNombrePlanCta(String filtroNombrePlanCta) {
		this.filtroNombrePlanCta = filtroNombrePlanCta;
	}

	public List<ConPlantas> getPlanCtas() {
		return planCtas;
	}

	public void setPlanCtas(List<ConPlantas> planCtas) {
		this.planCtas = planCtas;
	}

	public String getFiltroUniAdmin() {
		return filtroUniAdmin;
	}

	public void setFiltroUniAdmin(String filtroUniAdmin) {
		this.filtroUniAdmin = filtroUniAdmin;
	}

	public List<GUniadmin> getUniAdmin() {
		return uniAdmin;
	}

	public void setUniAdmin(List<GUniadmin> uniAdmin) {
		this.uniAdmin = uniAdmin;
	}

	public Arccmd getFiltro() {
		return filtro;
	}

	public void setFiltro(Arccmd filtro) {
		this.filtro = filtro;
	}

	public LDMEntradasMovimientos getModel() {
		return model;
	}

	public void setModel(LDMEntradasMovimientos model) {
		this.model = model;
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
}
