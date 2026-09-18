package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonUtils;
import com.casabaca.common.ejb.model.Arccck;
import com.casabaca.common.ejb.model.ArccckDet;
import com.casabaca.common.ejb.model.ArccckDetPK;
import com.casabaca.common.ejb.model.ArccckPK;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcEntradaChequesDevueltosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.ServiceException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LDMEntradaChequesDevueltos;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.UsuarioSis;

@ViewScoped
@ManagedBean
public class CxcEntradaChequesDevueltosController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CxcEntradaChequesDevueltosController.class);
	private static final int SIZE = 10;

	@EJB(lookup = NombreJNDI.CXC_ENTRADA_CHEQUES_DEVUELTOS_SERVICE)
	private CxcEntradaChequesDevueltosServiceLocal service;

	private String noCia;
	private String centro;
	private String usuarioSesion;
	private String filtroCliente;
	private String filtroUsuario;
	private String filtroVendedor;

	private Arccck arccck;
	private Arccck filtro;
	private ArccckDet detalle;

	private List<Cliente> clientes;
	private List<String[]> bancosNd;
	private List<String[]> bancosCh;
	private List<String> motivos;
	private List<String[]> lineas;
	private List<UsuarioSis> usuarios;
	private List<String[]> vendedores;
	private List<ArccckDet> listaDetalle;
	private List<Agencia> agencias;

	private boolean readonly;
	private boolean edit;

	private LDMEntradaChequesDevueltos model;

	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		centro = getUsuarioCentroConectado().getUsuarioCentroPK().getCentro();
		usuarioSesion = getUsuario().getUsuario();

		validarInicio();

	}

	private void validarInicio() {

		String noDocu = super.getRequestParameter("noDocu");
		if (noDocu != null) {
			if ("0".equals(noDocu))
				nuevaEntrada();
			else
				consultarEntrada(super.getRequestParameter("noDocu"));
			cargarBancoNd();
			cargarBancoCh();
			cargarMotivos();
			cargarLineas();
		} else {
			filtro = new Arccck(new ArccckPK(noCia, null));
			filtro.setCentro(centro);
		}

		readonly = !service.canEdit(noCia, usuarioSesion);
	}

	public void buscarEntradas() {
		model = new LDMEntradaChequesDevueltos(0, SIZE, filtro);
	}

	public void getUrlConsulta(Arccck item) {
		String url = "/cxc-web-prime/jsf/procesos/cxcEntradaChequesDevueltos/form.jsf?noDocu="
				+ item.getId().getNoDocu();
		ejecutarJavascript("openDuplicatedTab('" + url + "');");
	}

	public void getUrlNuevo() {
		String url = "/cxc-web-prime/jsf/procesos/cxcEntradaChequesDevueltos/form.jsf?noDocu=0";
		ejecutarJavascript("openDuplicatedTab('" + url + "');");
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

	private void cargarBancoNd() {
		try {
			bancosNd = service.getBancosNotaDebito(noCia);
		} catch (FindException e) {
			super.error("Error al cargar bancos Nd");
			logger.error("Error al cargar bancos Nd", e);
		}
	}

	private void cargarBancoCh() {
		try {
			bancosCh = service.getBancosCheque();
		} catch (FindException e) {
			super.error("Error al cargar bancos Ch");
			logger.error("Error al cargar bancos Ch", e);
		}
	}

	private void cargarMotivos() {
		try {
			motivos = service.getMotivos(noCia);
		} catch (FindException e) {
			super.error("Error al cargar motivos");
			logger.error("Error al cargar motivos", e);
		}
	}

	private void cargarLineas() {
		try {
			lineas = service.getLineas(noCia);
		} catch (FindException e) {
			super.error("Error al cargar líneas");
			logger.error("Error al cargar líneas", e);
		}
	}

	public void selectCliente(Cliente cliente) {
		arccck.setNoCliente(cliente.getClientePK().getNoCliente());
		arccck.setCedula(cliente.getCedula());
		arccck.setNomCliente(cliente.getNombre());
	}

	public void validarClientePorCodigo() {
		try {
			Cliente cliente = service.getClienteCodigo(noCia, arccck.getNoCliente());
			selectCliente(cliente);
		} catch (FindException e) {
			arccck.setNoCliente(null);
			arccck.setCedula(null);
			arccck.setNomCliente(null);
			super.error("Cliente no existe");
		}
	}

	public void validarClientePorCedula() {
		try {
			Cliente cliente = service.getClienteCedula(noCia, arccck.getCedula());
			selectCliente(cliente);
		} catch (FindException e) {
			arccck.setNoCliente(null);
			arccck.setCedula(null);
			arccck.setNomCliente(null);
			super.error("Cliente no existe");
		}
	}

	public void changeBancoEnvia() {
		if (arccck.getCuentaNd() == null)
			arccck.setDepositoBanco(null);
		else {
			bancosNd.forEach(b -> {
				if (arccck.getCuentaNd().equals(b[2]))
					arccck.setDepositoBanco(b[0]);
			});
		}
	}

	public void changeCheque() {
		if (arccck.getNoCliente() == null) {
			super.error("Ingrese cliente");
			arccck.setNoCheque(null);
			return;
		}
		if (arccck.getBanco() == null) {
			super.error("Seleccione banco");
			arccck.setNoCheque(null);
			return;
		}
		if (service.existeCheque(noCia, arccck.getGrupo(), arccck.getNoCliente(), arccck.getBanco(),
				arccck.getNoCheque())) {
			super.error("El cheque de ese cliente ya ha sido digitado");
			arccck.setNoCheque(null);
		}
	}

	public void changeLinea() {
		if (arccck.getPerteneceA() == null)
			arccck.setTipoDoc(null);
		else {
			lineas.forEach(l -> {
				if (arccck.getPerteneceA().equals(l[0]))
					arccck.setTipoDoc(l[2]);
			});
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
		arccck.setUsuarioCajero(usuario.getUsuario());
		arccck.setNomUserCaja(usuario.getNombre());
	}

	public void validarUsuario() {
		if (!CommonUtils.isValid(arccck.getUsuarioCajero())) {
			arccck.setUsuarioCajero(null);
			arccck.setNomUserCaja(null);
			return;
		}
		filtroUsuario = arccck.getUsuarioCajero();
		buscarUsuarios();
		if (usuarios.isEmpty()) {
			arccck.setUsuarioCajero(null);
			arccck.setNomUserCaja(null);
			super.error("Usuario no existe");
			return;
		} else if (usuarios.size() == 1) {
			selectUsuario(usuarios.get(0));
			return;
		} else if (usuarios.size() > 1) {
			super.accionesDialog("dlgUsuarios", true);
		}
	}

	public void buscarVendedores() {
		try {
			vendedores = service.getVendedores(noCia, filtroVendedor);
		} catch (FindException e) {
			super.error("Error al buscar vendedores");
			logger.error("Error al buscar vendedores", e);
		}
	}

	public void selectVendedor(String[] vendedor) {
		arccck.setVendedor(vendedor[0]);
		arccck.setNomVendedor(vendedor[1]);
	}

	public void validarVendedor() {
		if (!CommonUtils.isValid(arccck.getVendedor())) {
			arccck.setVendedor(null);
			arccck.setNomVendedor(null);
			return;
		}
		filtroVendedor = arccck.getVendedor();
		buscarVendedores();
		if (vendedores.isEmpty()) {
			arccck.setVendedor(null);
			arccck.setNomVendedor(null);
			super.error("Vendedor no existe");
			return;
		} else if (vendedores.size() == 1) {
			selectVendedor(vendedores.get(0));
			return;
		} else if (vendedores.size() > 1) {
			super.accionesDialog("dlgVendedores", true);
		}
	}

	public void guardar() {

		if (arccck.getNoCliente() == null) {
			super.error("Ingrese cliente");
			return;
		}

		if (!CommonUtils.isValid(arccck.getDepositoBanco())) {
			super.error("Seleccione banco que envía");
			return;
		}

		if (arccck.getFechaNd() == null) {
			super.error("Ingrese fecha de Nota de Débito");
			return;
		}

		if (!CommonUtils.isValid(arccck.getNotaDebito())) {
			super.error("Ingrese Número de Nota Débito");
			return;
		}

		if (arccck.getValorNd() == null) {
			super.error("Ingrese Valor de Nota de Débito");
			return;
		}

		if (!CommonUtils.isValid(arccck.getBanco())) {
			super.error("Seleccione Banco de Cheque");
			return;
		}

		if (arccck.getFecha() == null) {
			super.error("Ingrese fecha de Cheque");
			return;
		}

		if (!CommonUtils.isValid(arccck.getNoCheque())) {
			super.error("Ingrese número de Cheque");
			return;
		}

		if (!CommonUtils.isValid(arccck.getNoMemo())) {
			super.error("Ingrese memo");
			return;
		}

		if (arccck.getMonto() == null) {
			super.error("Ingrese monto de Cheque");
			return;
		}

		if (!CommonUtils.isValid(arccck.getUsuarioCajero())) {
			super.error("Ingrese Cajero");
			return;
		}

//		if (!CommonUtils.isValid(arccck.getVendedor())) {
//			super.error("Ingrese Vendedor");
//			return;
//		}

		try {
			if (edit) {
				service.actualizar(arccck);
				super.info("Documento actualizado correctamente");
			} else {
				arccck.setMoneda("D");
				service.guardar(arccck, usuarioSesion);
				super.info("Documento ingresado correctamente");
				nuevaEntrada();
			}
		} catch (ServiceException e) {
			super.error("Error al guardar documento");
			super.error(e.getMessage());
			logger.error("Error al guardar documento", e);
		}
	}

	public void nuevoDetalle() {
		Long secuencia = 0L;
		if(listaDetalle != null && !listaDetalle.isEmpty()){
			OptionalLong maximo= listaDetalle.stream().mapToLong(d -> d.getId().getNoLinea()).max();
			if(maximo.isPresent())
				secuencia = maximo.getAsLong();
		}
		secuencia++;
		detalle = new ArccckDet(new ArccckDetPK(noCia, arccck.getId().getNoDocu(), secuencia));
		detalle.setUsuarioEnvia(usuarioSesion);
		cargarAgencias();
		super.accionesDialog("dlgNuevoCustodia", true);
	}

	public void guardarDetalle() {
		if (!CommonUtils.isValid(detalle.getUsuarioRecibe())) {
			super.error("Ingrese Usuario");
			return;
		}
		if (!CommonUtils.isValid(detalle.getObservacion())) {
			super.error("Ingrese observación");
			return;
		}
		try {
			detalle.setAgenciaEnvia(centro);
			detalle.setFechaEnvia(new Date());
			detalle.setFechaRecibe(new Date());
			service.guardarDetalle(arccck, detalle);
			cargarDetalle();
			super.accionesDialog("dlgNuevoCustodia", false);
			super.info("Detalle ingresado correctamente");
		} catch (InsertException e) {
			super.error("Error al guardar detalle");
			logger.error("Error al guardar detalle", e);
		}
	}
	
	public void eliminarDetalle(ArccckDet item) {
		try{
		service.eliminarDetalle(item);
		cargarDetalle();
		super.info("Detalle eliminado con exito");
		}catch (Exception e) {
			super.error("Error al eliminar Detalle "+e);	
		}
	}

	public void selectUsuarioRecibe(UsuarioSis usuario) {
		detalle.setUsuarioRecibe(usuario.getUsuario());
	}

	public void validarUsuarioRecibe() {
		if (!CommonUtils.isValid(detalle.getUsuarioRecibe())) {
			detalle.setUsuarioRecibe(null);
			return;
		}
		filtroUsuario = detalle.getUsuarioRecibe();
		buscarUsuarios();
		if (usuarios.isEmpty()) {
			detalle.setUsuarioRecibe(null);
			super.error("Usuario no existe");
			return;
		} else if (usuarios.size() == 1) {
			selectUsuarioRecibe(usuarios.get(0));
			return;
		} else if (usuarios.size() > 1) {
			super.accionesDialog("dlgUsuariosRecibe", true);
		}
	}

	public Arccck getArccck() {
		return arccck;
	}

	public void setArccck(Arccck arccck) {
		this.arccck = arccck;
	}

	public Arccck getFiltro() {
		return filtro;
	}

	public void setFiltro(Arccck filtro) {
		this.filtro = filtro;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public LDMEntradaChequesDevueltos getModel() {
		return model;
	}

	public void setModel(LDMEntradaChequesDevueltos model) {
		this.model = model;
	}

	public int getSize() {
		return SIZE;
	}

	private void consultarEntrada(String noDocu) {
		edit = true;
		arccck = service.getEntrada(new ArccckPK(noCia, noDocu));
		cargarDetalle();
	}

	private void nuevaEntrada() {
		arccck = new Arccck(new ArccckPK(noCia, null));
		arccck.setGrupo("01");
		arccck.setCentro(centro);
		edit = false;
	}

	public void cargarDetalle() {
		try {
			listaDetalle = service.getDetalle(arccck.getId().getNoCia(), arccck.getId().getNoDocu());
		} catch (FindException e) {
			super.error("Error al cargar detalle");
			logger.error("Error al cargar detalle", e);
		}
	}

	private void cargarAgencias() {
		agencias = service.getAgencias(noCia);
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

	public List<String[]> getBancosNd() {
		return bancosNd;
	}

	public void setBancosNd(List<String[]> bancosNd) {
		this.bancosNd = bancosNd;
	}

	public List<String[]> getBancosCh() {
		return bancosCh;
	}

	public void setBancosCh(List<String[]> bancosCh) {
		this.bancosCh = bancosCh;
	}

	public List<String> getMotivos() {
		return motivos;
	}

	public void setMotivos(List<String> motivos) {
		this.motivos = motivos;
	}

	public List<String[]> getLineas() {
		return lineas;
	}

	public void setLineas(List<String[]> lineas) {
		this.lineas = lineas;
	}

	public List<UsuarioSis> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<UsuarioSis> usuarios) {
		this.usuarios = usuarios;
	}

	public String getFiltroUsuario() {
		return filtroUsuario;
	}

	public void setFiltroUsuario(String filtroUsuario) {
		this.filtroUsuario = filtroUsuario;
	}

	public String getFiltroVendedor() {
		return filtroVendedor;
	}

	public void setFiltroVendedor(String filtroVendedor) {
		this.filtroVendedor = filtroVendedor;
	}

	public List<String[]> getVendedores() {
		return vendedores;
	}

	public void setVendedores(List<String[]> vendedores) {
		this.vendedores = vendedores;
	}

	public ArccckDet getDetalle() {
		return detalle;
	}

	public void setDetalle(ArccckDet detalle) {
		this.detalle = detalle;
	}

	public List<ArccckDet> getListaDetalle() {
		return listaDetalle;
	}

	public void setListaDetalle(List<ArccckDet> listaDetalle) {
		this.listaDetalle = listaDetalle;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public void setAgencias(List<Agencia> agencias) {
		this.agencias = agencias;
	}
}
