package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.administration.ejb.model.SisClientes;
import com.casabaca.administration.ejb.service.SisClientesServiceLocal;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCreditoGlobal;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCreditoGlobalMant;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCreditoGlobalMantPK;
import com.casabaca.cxc.ejb.modelo.CxcSolicitudCreditoGlobalPK;
import com.casabaca.cxc.ejb.servicio.CxcCreditoGlobalMantServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcSolicitudCreditoGlobalServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.lazy.LazyDataModelSolCreditoGeneralDTO;
import com.casabaca.rest.wsCredito.entidad.SolicitudCreditoGlobal;
import com.casabaca.seminuevos.ejb.modelo.UsdCatalogueDetail;
import com.casabaca.seminuevos.ejb.servicio.UsdCatalogueDetailServiceLocal;
import com.casabaca.webservice.wsCreditoGlobal.ConsultaBandejaCreditoGlobal;
import com.casabaca.webservice.wsCreditoGlobal.impl.MainEjecutaServicioRestSolCreditoGlobal;

@ViewScoped
@ManagedBean(name = "cxcSolicitudCreditoGlobalController")
public class CxcSolicitudCreditoGlobalController extends CommonController implements Serializable {

	private static final String ESTADO_EN_REVISION = "L";
	private static final String ESTADO_ENVIADO = "E";
	private static final String ESTADO_EN_CONFIRMACION = "P";

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(CxcSolicitudCreditoGlobalController.class);
	private static final String VAL_COD_CATALOGO_NOM_EMPRESAS="NEBCG";

	@EJB(lookup = NombreJNDI.CXC_CREDITO_GLOBAL_SERVICE)
	private CxcSolicitudCreditoGlobalServiceLocal solicitudCreditoGlobalServicio;

	@EJB(lookup = NombreJNDI.CXC_CREDITO_GLOBAL_MANT_SERVICE)
	private CxcCreditoGlobalMantServiceLocal mantCreditoGlobalServicio;

	@EJB(lookup = NombreJNDI.SIS_CLIENTES_SERVICE)
	private SisClientesServiceLocal sisClientesServicio;

	private CxcSolicitudCreditoGlobal creditoBusqueda;
	private LazyDataModelSolCreditoGeneralDTO creditos;
	private UsdCatalogueDetailServiceLocal usdCatalogoService;
	private CxcSolicitudCreditoGlobal creditoSeleccionado;

	private List<CxcSolicitudCreditoGlobalMant> analistas;
	private Boolean jefeModulo;
	private Boolean aplicaAnalista;
	private String compania;
	
	private Map<String, Object> filtros = new HashMap<>();
	private List<UsdCatalogueDetail> listCatalogoNomEmpresas;

	@PostConstruct
	public void init() {
		try {
			usdCatalogoService=(UsdCatalogueDetailServiceLocal) ServiceLocator.getService(NombreJNDI.USD_CATALOGUE_DETAIL_SERVICE);
			creditoBusqueda = new CxcSolicitudCreditoGlobal(new CxcSolicitudCreditoGlobalPK());
			creditoBusqueda.setEstado(ESTADO_ENVIADO);
			creditos = new LazyDataModelSolCreditoGeneralDTO(creditoBusqueda, filtros);
			analistas = new ArrayList<>();
			validarPerfil();
			obtenerCia();
			cargarCatalogoNombreEmpresas();
			logger.info("Iniciando Servicio");
		} catch (ServiceLocatorException e) {
			logger.error("Error al inicializar cxcSolicitudCreditoGlobalController: "+e);
		}
	
	}

	private void obtenerCia() {
		compania = getCompania().getNoCia();
		if (null != getRequestParameter("noCia")) {
			compania = getRequestParameter("noCia");
		}
	}

	public void limpiarFiltros() {
		init();
	}

	private void validarPerfil() {
		jefeModulo = false;
		aplicaAnalista = false;
		List<CxcSolicitudCreditoGlobalMant> analistasPorEmpresa = mantCreditoGlobalServicio
				.buscarPorCiaUsuarioEstado(compania, getUsuario().getUsuario(), CommonConstants.ACTIVO);
		for (CxcSolicitudCreditoGlobalMant usuario : analistasPorEmpresa) {
			if ("J".equalsIgnoreCase(usuario.getRol())) {
				jefeModulo = true;
				aplicaAnalista = true;
			} else if ("A".equalsIgnoreCase(usuario.getRol())) {
				aplicaAnalista = true;
			}
		}
	}
	
	public void cargarCatalogoNombreEmpresas() {
		try {
			listCatalogoNomEmpresas = new ArrayList<UsdCatalogueDetail>();
			listCatalogoNomEmpresas = usdCatalogoService.consultarCatalogoActivoPorNoCiaAndSiglaCab(CommonConstants.CENTRIC,
					VAL_COD_CATALOGO_NOM_EMPRESAS);
			if (listCatalogoNomEmpresas == null) {
				addErrorMessage("Recuerde:","Debe cargar el catalogo de nombre empresas");
			}
		} catch (Exception e) {
			addErrorMessage("Error:","No se ha encontrado el catalogo para el nombre de las empresas");
		}
	}

	public void buscarSolicitudes() {
		this.filtros.put("solicitud", creditoBusqueda.getPk().getSolicitud());
		this.filtros.put("numeroCotizacion", creditoBusqueda.getNumeroCotizacion());
		this.filtros.put("fechaEnviadaDesde", creditoBusqueda.getFechaEnviadaDesde());
		this.filtros.put("fechaEnviadaHasta", creditoBusqueda.getFechaEnviadaHasta());
		this.filtros.put("cedula", creditoBusqueda.getCedula());
		creditos.setFiltros(filtros);
		this.creditos = new LazyDataModelSolCreditoGeneralDTO(creditoBusqueda, filtros);
	}

	public void cargarAnalistas(CxcSolicitudCreditoGlobal item) {
		if(controlarSelecPrimerCredito(item)) {
			creditoSeleccionado = new CxcSolicitudCreditoGlobal();
			analistas = new ArrayList<>();
			if (Boolean.TRUE.equals(jefeModulo)) {
				analistas = mantCreditoGlobalServicio.buscarPorCiaUsuarioEstado(compania, null, CommonConstants.ACTIVO);
			} else {
				analistas = mantCreditoGlobalServicio.buscarPorCiaUsuarioEstado(compania, getUsuario().getUsuario(),
						CommonConstants.ACTIVO);
			}
			creditoSeleccionado = item;
		}		
	}
	
	private boolean controlarSelecPrimerCredito(CxcSolicitudCreditoGlobal creditoParam) {
		boolean esFactibleSeleccionarCredito = false;
		if (this.creditos != null && creditoParam != null) {			
			if (creditos.getRowIndex() == 0) {
				esFactibleSeleccionarCredito = true;
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dialogAnalistas').show();");
			} else {
				addErrorMessage("Recuerde:",
						"Debe seleccionar una solicitud anterior, el orden de asignación es de acuerdo al orden de llegada de las solicitudes.");
			}
		} else {
			addErrorMessage("Recuerde:", "Debe seleccionar una solicitud");
		}
		return esFactibleSeleccionarCredito;
	}

	public void seleccionarAnalista(CxcSolicitudCreditoGlobalMant analista) {
		if (validarCantidadAsignadaAnalista(analista)) {
			addInfoMessage("Importante: "+analista.getNombre(), "Ya tiene asignado una solicitud.");
		} else {
			if(ejecutaActualizacionBandejaPorEmpresa(analista)) {
				asignarRevisionEnBandejaGlobal(analista);
				cambiarPuestosEnCola(ESTADO_ENVIADO, creditoSeleccionado.getPuesto());
			}	
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("PF('dialogAnalistas').hide();");
		}
	}

	private boolean ejecutaActualizacionBandejaPorEmpresa(CxcSolicitudCreditoGlobalMant analista) {
		boolean actualizaBandeja = false;
		SolicitudCreditoGlobal creditoXML = setearDatos(creditoSeleccionado, analista);
		ConsultaBandejaCreditoGlobal creditoGlobal = new MainEjecutaServicioRestSolCreditoGlobal();
		try {
			SisClientes objConex = sisClientesServicio
					.obtenerCodigoClienteByCIA(creditoSeleccionado.getPk().getNoCia());
			String resultado = creditoGlobal.actualizarSolCredito(creditoXML, objConex);
			if (resultado.contains("REGISTRO_ACTUALIZADO")) {
				actualizaBandeja = true;
				addInfoMessage("Exito!!", "Se ha actualizado la solicitud de crédiro");
			} else {
				addErrorMessage("Error:", "No es posible actualizar la solicitud de crédito");
			}
		} catch (Exception e) {
			actualizaBandeja = false;
			addErrorMessage("Error:", "Ocurrio un problema con el servicio de actualización de crédito por empresa");
		}
		return actualizaBandeja;
	}
	
	private void asignarRevisionEnBandejaGlobal(CxcSolicitudCreditoGlobalMant analista) {
		try {
			creditoSeleccionado.setUsuarioRevision(obtenerUsuarioPorEmpresa(analista));
			creditoSeleccionado.setFechaUsuarioRevision(new Date());
			creditoSeleccionado.setUsuarioUltimaRev(obtenerUsuarioPorEmpresa(analista));
			creditoSeleccionado.setFechaUltimaRev(new Date());
			creditoSeleccionado.setEstado(ESTADO_EN_REVISION);
			creditoSeleccionado.setPuesto(obtenerPuestoPorEstado(ESTADO_EN_REVISION));
			solicitudCreditoGlobalServicio.editar(creditoSeleccionado);
		} catch (UpdateException e) {
			addErrorMessage("Ocurrio un error al actualizar la bandeja general", e.getMessage());
		}
	}

	private long obtenerPuestoPorEstado(String estado) {
		this.filtros = new HashMap<>();
		this.creditoBusqueda = new CxcSolicitudCreditoGlobal();
		creditoBusqueda.setEstado(estado);
		return solicitudCreditoGlobalServicio.obtenerTotalSolicitudes(creditoBusqueda, this.filtros) + 1L;
	}

	private void cambiarPuestosEnCola(String estado, Long puesto) {
		this.filtros = new HashMap<>();
	    this.creditoBusqueda = new CxcSolicitudCreditoGlobal();
	    creditoBusqueda.setEstado(estado);	    
	    List<CxcSolicitudCreditoGlobal> creditosList = solicitudCreditoGlobalServicio.obtenerListaSolicitudes(creditoBusqueda,0,100, this.filtros);
	    creditosList.stream()
	        //.filter(credito -> credito.getPuesto() > puesto)
	        .forEach(credito -> {
	            try {
	                credito.setPuesto(credito.getPuesto() - 1);
	                solicitudCreditoGlobalServicio.editar(credito);
	                logger.info("Puesto actualizado: " + credito.getPuesto() + " Solicitud: " + credito.getPk().getSolicitud());
	            } catch (UpdateException e) {
	               addErrorMessage("Error al actualizar puesto de la solicitud: ", "Solicitud: "+credito.getPk().getSolicitud() + e.getMessage());
	            }
	        });
	}
	
	private boolean validarCantidadAsignadaAnalista(CxcSolicitudCreditoGlobalMant analista) {
		CxcSolicitudCreditoGlobal busqueda = new CxcSolicitudCreditoGlobal();
		busqueda.setUsuarioRevision(analista.getPk().getUsuario());
		busqueda.setEstado(ESTADO_EN_REVISION);
		creditos.setFiltros(filtros);
		List<CxcSolicitudCreditoGlobal> lista = solicitudCreditoGlobalServicio
				.obtenerListaSolicitudes(busqueda, 0,100, this.filtros);
		return !lista.isEmpty();
	}

	

	private SolicitudCreditoGlobal setearDatos(CxcSolicitudCreditoGlobal credito, CxcSolicitudCreditoGlobalMant analista) {
		SolicitudCreditoGlobal creditoXML = new SolicitudCreditoGlobal();
		try {
			creditoXML.setCompania(credito.getPk().getNoCia());
			creditoXML.setNoSolicitud(credito.getPk().getSolicitud());
			creditoXML.setUsuarioRevision(obtenerUsuarioPorEmpresa(analista));
			creditoXML.setFechaUsuarioRevision(FechaUtils.formatearFecha(new Date(), "dd/MM/yyyy HH:mm"));
			creditoXML.setCorreoAnalista(analista.getCorreo());
			creditoXML.setCelularAnalista(analista.getTelefono());

		} catch (Exception e) {
			logger.error("ERROR AL SETEAR DATOS PARA ENVIO A WS ", e);
		}
		return creditoXML;
	}
	
	public void cambiarEstadoConfirmar(CxcSolicitudCreditoGlobal credito) {
		if (credito.getUsuarioRevision().equals(getUsuario().getUsuario())) {
			CxcSolicitudCreditoGlobalMant mant;
			try {
				mant = mantCreditoGlobalServicio.buscarPorPk(new CxcSolicitudCreditoGlobalMantPK(CommonConstants.CENTRIC, credito.getUsuarioRevision()));
				if (null != mant && mant.getNumeroRevision() > obtenerPorConfirmar(credito.getUsuarioRevision())) {
					accionesDialog("dialogObservacion", true);
					creditoSeleccionado = credito;
				} else {
					addWarnMessage("El analista no puede cambiar de estado", " Máximo permitido: " + obtenerPorConfirmar(credito.getUsuarioRevision()));
				}
			} catch (FindException e) {
				addErrorMessage("Error", "No se encuentra al analista asignado.");
			}
		} else {
			addWarnMessage("No tiene permisos sobre esta solicitud", "Solo el usuario" + credito.getUsuarioRevision() + " podrá cambiar de estado la solicitud.");
		}
	}
	
	private Long obtenerPorConfirmar(String usuarioRevision) {
		return (long) solicitudCreditoGlobalServicio.buscarSolicitudesPorCiaUsuarioEstado(CommonConstants.CENTRIC,usuarioRevision,ESTADO_EN_CONFIRMACION).size();
	}

	public void guardarObservacion(){
		accionesDialog("dialogObservacion", false);
		try {
			creditoSeleccionado.setEstado("P");
			solicitudCreditoGlobalServicio.editar(creditoSeleccionado);
		} catch (UpdateException e) {
			addErrorMessage("Error al pausar solicitud", e.getMessage());
		}
	}
	
	private String obtenerUsuarioPorEmpresa(CxcSolicitudCreditoGlobalMant analista) {
		switch (creditoSeleccionado.getPk().getNoCia()) {
		case CommonConstants.CASABACA:
			return analista.getUsuarioCb();
		case CommonConstants.CARLOS_LARREA:
			return analista.getUsuarioCl();
		case CommonConstants.COSTA1001:
			return analista.getUsuarioGbv();
		case CommonConstants.NEXUMCORP:
			return analista.getUsuarioNx();
		case CommonConstants.SUZUKI:
			return analista.getUsuarioSzk();
		case CommonConstants.TOYOCOSTAS:
			return analista.getUsuarioTy();
		default:
			return analista.getPk().getUsuario();
		}
	}
	
	public CxcSolicitudCreditoGlobal getCreditoBusqueda() {
		return creditoBusqueda;
	}

	public void setCreditoBusqueda(CxcSolicitudCreditoGlobal creditoBusqueda) {
		this.creditoBusqueda = creditoBusqueda;
	}

	public LazyDataModelSolCreditoGeneralDTO getCreditos() {
		return creditos;
	}

	public void setCreditos(LazyDataModelSolCreditoGeneralDTO creditos) {
		this.creditos = creditos;
	}

	public List<CxcSolicitudCreditoGlobalMant> getAnalistas() {
		return analistas;
	}

	public void setAnalistas(List<CxcSolicitudCreditoGlobalMant> analistas) {
		this.analistas = analistas;
	}

	public Boolean getJefeModulo() {
		return jefeModulo;
	}

	public void setJefeModulo(Boolean jefeModulo) {
		this.jefeModulo = jefeModulo;
	}

	public CxcSolicitudCreditoGlobal getCreditoSeleccionado() {
		return creditoSeleccionado;
	}

	public void setCreditoSeleccionado(CxcSolicitudCreditoGlobal creditoSeleccionado) {
		this.creditoSeleccionado = creditoSeleccionado;
	}

	public Boolean getAplicaAnalista() {
		return aplicaAnalista;
	}

	public void setAplicaAnalista(Boolean aplicaAnalista) {
		this.aplicaAnalista = aplicaAnalista;
	}

	public List<UsdCatalogueDetail> getListCatalogoNomEmpresas() {
		return listCatalogoNomEmpresas;
	}

	public void setListCatalogoNomEmpresas(List<UsdCatalogueDetail> listCatalogoNomEmpresas) {
		this.listCatalogoNomEmpresas = listCatalogoNomEmpresas;
	}
	
	
}