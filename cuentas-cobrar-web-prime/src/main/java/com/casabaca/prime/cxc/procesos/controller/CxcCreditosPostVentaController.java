package com.casabaca.prime.cxc.procesos.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.CotizacionRepuesto;
import com.casabaca.common.ejb.model.CxcClaseCliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CotizacionRepuestoServicesLocal;
import com.casabaca.common.ejb.service.CxcClaseClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.service.ValidacionCedulaServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.UpdateException;
import com.casabaca.inventario.ejb.model.InvVendedor;
import com.casabaca.inventario.ejb.model.ProformasCab;
import com.casabaca.inventario.ejb.service.InvVendedorServiceLocal;
import com.casabaca.inventario.ejb.service.ProformasCabServiceLocal;
import com.casabaca.inventario.ejb.service.ProformasDetServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

@ViewScoped
@ManagedBean
public class CxcCreditosPostVentaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 195488519038413871L;

	private static final Logger logger = Logger.getLogger(CxcCreditosPostVentaController.class);

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_CLASE_CLIENTE_SERVICE)
	private CxcClaseClienteServiceLocal servicioClaseCliente;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaSerice;

	@EJB(lookup = NombreJNDI.COTIZACION_REPUESTOS_SERVICES_BEAN)
	private CotizacionRepuestoServicesLocal cotizacionRepuestosServices;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@EJB(lookup = NombreJNDI.PROFORMAS_CAB_SERVICE)
	private ProformasCabServiceLocal proformasCabService;

	@EJB(lookup = NombreJNDI.PROFORMAS_DET_SERVICE)
	private ProformasDetServiceLocal proformasDetService;

	@EJB(lookup = NombreJNDI.INV_VENDEDOR_SERVICE)
	private InvVendedorServiceLocal invVendedorServiceLocal;

	@EJB(lookup = NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN)
	private NativeDmlDatabaseServiceLocal nativeDmlDBServiceLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioServiceLocal;
	
	@EJB(lookup = NombreJNDI.VALIDACION_CEDULA_SERVICE_LOCAL)
	private ValidacionCedulaServiceLocal validacionCedulaServiceLocal;

	private static final String VEH = "VEH";
	private static final String SER = "SER";
	private static final String REP = "REP";
	private static final String COD_BLOQUEO = "BLOQUE";
	private static final String COD_CANAL_REP = "CANALR";

	private String cedula;
	private String codAgencia;
	private String codLinea;
	private String nombre;
	private String descrClaseVeh;
	private String descrClaseSer;
	private String descrClaseRep;
	private String descrBloqueoCli;
	private String descrCanalRep;
	private String noCliente;
	private String tipoReporte;
	private String nombreFileComprobante;
	private BigDecimal monto;
	private BigDecimal cupoDisponible;
	private BigDecimal creditoSolicitado;
	private BigDecimal nuevoCupoDisponible;
	private Double vencido30;
	private Double vencido60;
	private Double vencido90;
	private Double vencidoMas90;
	private Double totalCartera;
	private boolean tienePagare;
	private boolean revisionComite;
	private int tabActiveIndex;
	private Cliente cliente;
	private Agencia agencia;
	private LineaNegocio lineaDeNegocio;
	private CotizacionRepuesto cotizacionRepuesto;
	private ProformasCab proformaCab;
	private InputStream fileComprobante;
	private List<Cliente> lstClientes;
	private List<ParamDet> lstBloqueosCli;
	private List<ParamDet> lstCanalesRepuestos;
	private List<CxcClaseCliente> lstClaseVeh;
	private List<CxcClaseCliente> lstClaseRep;
	private List<CxcClaseCliente> lstClaseSer;
	private List<Agencia> lstAgenciasSer;
	private List<Agencia> lstAgencias;
	private List<LineaNegocio> lstLineasNeg;
	private List<LineaNegocio> lstLineasNegocio;
	private List<CotizacionRepuesto> listCotizacionRep;
	private List<CotizacionRepuesto> listCotiByCliente;

	@PostConstruct
	public void init() {
		setTabActiveIndex(0);
		listCotizacionRep = cotizacionRepuestosServices.findByClienteAndEstadoPediente(getCompania().getNoCia(), null,
				null);
		listCotizacionRep.forEach( item -> {
			Cliente cliente;
			try {
				cliente = clienteServiceLocal.buscarPorCedulaNoCia(item.getCedulaDeudor(), item.getId().getNoCia());
				item.setZonaCliente(cliente.getZona());
			} catch (FindException e) {
				addErrorMessage("ERROR", e.getMessage());
				logger.error(e.getMessage(), e.getCause());
			}
		});
		Collections.sort(this.listCotizacionRep, (item1, item2) -> item2.getFechaElabora().compareTo(item1.getFechaElabora()));
		lstBloqueosCli = paramDetService.buscarDetalleCodigo(getCompania().getNoCia(), COD_BLOQUEO);
		lstCanalesRepuestos = paramDetService.buscarDetalleCodigo(getCompania().getNoCia(), COD_CANAL_REP);
		lstClaseVeh = servicioClaseCliente.findByNoCiaNegocio(getCompania().getNoCia(), VEH);
		lstClaseSer = servicioClaseCliente.findByNoCiaNegocio(getCompania().getNoCia(), SER);
		lstClaseRep = servicioClaseCliente.findByNoCiaNegocio(getCompania().getNoCia(), REP);
		lstAgenciasSer = agenciaService.getAgenciasServicios(getCompania().getNoCia());
		lstAgencias = agenciaService.findByCompania(getCompania().getNoCia(), null);
		try {
			lstLineasNeg = lineaSerice.getLineaNegociobyAplicaServicio(getCompania().getNoCia());
			lstLineasNegocio =  lineaSerice.buscarLineaNegocioLista(getCompania().getNoCia());
		} catch (FindException e) {
			logger.error("No se encontro ninguna linea que aplique servicio con no_cia " + getCompania().getNoCia());
			logger.error(e.getMessage(), e.getCause());
		}

		listCotizacionRep.stream().forEach(item -> {
			item.setNombreCliente(item.getNombres() != null
					? item.getNombres().concat(" ").concat(item.getApellidos() != null ? item.getApellidos() : "")
					: "".concat(item.getApellidos() != null ? item.getApellidos() : ""));
			InvVendedor vendedor = invVendedorServiceLocal.buscarVendedorPorNociaCodigo(getCompania().getNoCia(),
					item.getCodVendActual());
			item.setNombreVendedor(vendedor.getNombre());
			Optional<Agencia> agencia = lstAgencias.stream().filter(codAge -> codAge.getAgenciaPK() != null && codAge.getAgenciaPK().getCodigo().equals(item.getId().getCentro())).findFirst();
			item.getId().setCentro(agencia.isPresent() ? agencia.get().getNombre() : item.getId().getCentro() );
		});

		
		this.lstClientes = new ArrayList<Cliente>();
		cliente = new Cliente();
		noCliente = getRequestParameter("noCliente") != null ? getRequestParameter("noCliente") : null;
		if (noCliente != null && !noCliente.isEmpty()) {
			cliente.setCedula(clienteServiceLocal.buscarCedulaCliente(getCompania().getNoCia(), Long.valueOf(noCliente)));
			loadCliente();
		}
	}

	public void cargarProformaCliente(CotizacionRepuesto item) {
		cliente = new Cliente();
		cotizacionRepuesto = null;
		cliente.setCedula(item.getCedulaDeudor());
		setTabActiveIndex(1);
		loadCliente();
	}

	public void loadCliente() {
		try {
			if (cliente.getCedula() != null && !cliente.getCedula().isEmpty()) {
				if(cliente.getCedula().length() == 10 || cliente.getCedula().length() == 13) {
					boolean ok = false;
					String tipoDoc = cliente.getCedula().length() == 10 ? "C" : cliente.getCedula().length() == 13 ? "R" : null;				
					Object[] results = validacionCedulaServiceLocal.validarCedulaOPasaporte(cliente.getCedula(), tipoDoc);
					ok = (Boolean) results[0];
					if (!ok) {
						if (results[1] != null) {
							error(results[1].toString());
						} else {
							error("El valor ingresado para la cedula no esta correcto");
						}
					}
				} else {
					error("Ingrese una cedula valida");
					return;
				}
				cliente = clienteServiceLocal.findByCedulaNoCia(cliente.getCedula(), getCompania().getNoCia());
				fillFields();
				setTabActiveIndex(1);
			} else {
				lstClientes = clienteServiceLocal.findByNoCiabyCedulabyNombre(getCompania().getNoCia(), getCedula(),
						getNombre().toUpperCase());
			}

		} catch (FindException e) {
			addErrorMessage("ERROR", "El cliente no se ecuentra en la base de datos");
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void fillFields() {
		Optional<ParamDet> bloqueo = lstBloqueosCli.stream()
				.filter(a -> cliente.getSegCensal() != null && a.getParamDetPK().getCodigoDet().equals(cliente.getSegCensal().toString()))
				.findFirst();
		
		setDescrBloqueoCli(bloqueo.isPresent() ? bloqueo.get().getDescripcion() : "");

		Optional<CxcClaseCliente> veh = lstClaseVeh.stream()
				.filter(a -> cliente.getClaseVehiculos() != null && a.getCxcClaseClientePK().getClase().equals(cliente.getClaseVehiculos()))
				.findFirst();
		
		setDescrClaseVeh(veh.isPresent() ? veh.get().getDescripcion() : "");

		Optional<CxcClaseCliente> ser = lstClaseSer.stream()
				.filter(a -> cliente.getClaseServicio() != null && a.getCxcClaseClientePK().getClase().equals(cliente.getClaseServicio()))
				.findFirst();
		
		setDescrClaseSer(ser.isPresent() ? ser.get().getDescripcion() : "");

		Optional<CxcClaseCliente> rep = lstClaseRep.stream()
				.filter(a -> cliente.getClaseRepuestos() != null && a.getCxcClaseClientePK().getClase().equals(cliente.getClaseRepuestos()))
				.findFirst();
		
		setDescrClaseRep(rep.isPresent() ? rep.get().getDescripcion() : "");

		Optional<ParamDet> desCanalRep = lstCanalesRepuestos.stream()
				.filter(a -> cliente.getCanalRepuestos() != null && a.getParamDetPK().getCodigoDet().equals(cliente.getCanalRepuestos()))
				.findFirst();
	
		setDescrCanalRep(desCanalRep.isPresent() ? desCanalRep.get().getDescripcion() : "");

		setTienePagare(cliente.getPagare() != null && "S".equals(cliente.getPagare()) || false );

		HashMap<String, Object> params = new HashMap<>();
		params.put("noCia", getCompania().getNoCia());
		params.put("noCliente", cliente.getClientePK().getNoCliente());
		try {
			StringBuffer sqlVencido30 = new StringBuffer(" select ");
			buildGenericVencidosSql(sqlVencido30);
			sqlVencido30.append(" and trunc(fecha_vence) between trunc(sysdate-30) ");
			sqlVencido30.append(" and trunc(sysdate) ) vencido30 ");
			buildSql(sqlVencido30);
			setVencido30(nativeDmlDBServiceLocal.consultaNativaDouble(sqlVencido30.toString(), params));

			StringBuffer sqlVencido60 = new StringBuffer(" select ");
			buildGenericVencidosSql(sqlVencido60);
			sqlVencido60.append(" and trunc(fecha_vence) between trunc(sysdate-60) ");
			sqlVencido60.append(" and trunc(sysdate-31) ) vencido60 ");
			buildSql(sqlVencido60);
			setVencido60(nativeDmlDBServiceLocal.consultaNativaDouble(sqlVencido60.toString(), params));

			StringBuffer sqlVencido90 = new StringBuffer(" select ");
			buildGenericVencidosSql(sqlVencido90);
			sqlVencido90.append(" and trunc(fecha_vence) between trunc(sysdate-90) ");
			sqlVencido90.append(" and trunc(sysdate-61) ) vencido90 ");
			buildSql(sqlVencido90);
			setVencido90(nativeDmlDBServiceLocal.consultaNativaDouble(sqlVencido90.toString(), params));

			StringBuffer sqlVencidoMas90 = new StringBuffer(" select ");
			buildGenericVencidosSql(sqlVencidoMas90);
			sqlVencidoMas90.append(" and trunc(fecha_vence) < trunc(sysdate-91) ) vencidomas90 ");
			buildSql(sqlVencidoMas90);
			setVencidoMas90(nativeDmlDBServiceLocal.consultaNativaDouble(sqlVencidoMas90.toString(), params));

			StringBuffer sqlTotalCartera = new StringBuffer(" select ");
			buildGenericVencidosSql(sqlTotalCartera);
			sqlTotalCartera.append(" and trunc(fecha_vence) <= trunc(sysdate) ) totalcartera ");
			buildSql(sqlTotalCartera);
			setTotalCartera(nativeDmlDBServiceLocal.consultaNativaDouble(sqlTotalCartera.toString(), params));

		} catch (FindException e) {
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void buildGenericVencidosSql(StringBuffer sql) {
		sql.append(" ( select nvl(sum(saldo),0) valor from arccmd_all_view ");
		sql.append(" where no_cia = :noCia ");
		sql.append(" and no_cliente = :noCliente ");
		sql.append(" and nvl(a.serie_fisico,'X') = nvl(null,nvl(a.serie_fisico ,'X')) ");
		sql.append(" and saldo > 0 ");
		sql.append(" and nvl(anulado,'N') = 'N' ");
	}

	public void buildSql(StringBuffer sql) {
		sql.append(" from arccmd a ");
		sql.append(" where  a.no_cia = :noCia ");
		sql.append(" and nvl(a.serie_fisico  ,'X') = nvl( null ,nvl(a.serie_fisico  ,'X')) ");
		sql.append(" and nvl(a.anulado,'N') = 'N' ");
		sql.append(" and a.saldo <> 0 ");
		sql.append(" and a.no_cliente = :noCliente ");
	}

	public void loadAgencias() {
		Optional<Agencia> agencia = lstAgenciasSer.stream().filter(a -> a.getAgenciaPK().getCodigo().equals(codAgencia))
				.findFirst();
		setCodAgencia(agencia.isPresent() ? agencia.get().getAgenciaPK().getCodigo() : "");
		setAgencia(agencia.isPresent() ? agencia.get() : new Agencia());
	}

	public void loadLineas() {
		Optional<LineaNegocio> linea = lstLineasNeg.stream()
				.filter(a -> a.getLineaNegocioPK().getNoLinea().equals(codLinea)).findFirst();
		setCodLinea(linea.isPresent() ? linea.get().getLineaNegocioPK().getNoLinea() : "");
		setLineaDeNegocio(linea.isPresent() ? linea.get() : new LineaNegocio());
	}

	public void guardarCliente() {
		try {
			cliente.setRelacion(cliente.getClaseVehiculos());
			clienteServiceLocal.update(cliente);
		} catch (UpdateException e) {
			addErrorMessage("ERROR",
					new StringBuilder("No se logro guardar los datos para el cliente ")
							.append(cliente.getClientePK().getNoCliente())
							.append(" con nombre ").append(cliente.getNombre()).toString());
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void loadListCoti() {

		listCotiByCliente = cotizacionRepuestosServices.findByClienteAndEstadoPediente(getCompania().getNoCia(), cliente.getCedula(), null);

		listCotiByCliente.stream().forEach(item -> {
			InvVendedor vendedor = invVendedorServiceLocal.buscarVendedorPorNociaCodigo(getCompania().getNoCia(),
					item.getCodVendActual());
			item.setNombreVendedor(vendedor.getNombre());
		});
		monto = arccmdService.obtenerMonto(cliente.getClientePK().getNoCia(),
				cliente.getClientePK().getNoCliente().toString());
	}

	public void revisionProforma(CotizacionRepuesto item) {
		cotizacionRepuesto = item;
		cotizacionRepuesto.setUsuarioAprob(getUsuario().getUsuario());
		setRevisionComite(
				cotizacionRepuesto.getRevisionComite() != null && cotizacionRepuesto.getRevisionComite().equals("S") || false);
		proformaCab = proformasCabService.findByNoCiaCentroNoFisicoNoCotizacion(
				cotizacionRepuesto.getId().getNoCia(),
				cotizacionRepuesto.getId().getCentro(), 
				cotizacionRepuesto.getNoFisico(),
				cotizacionRepuesto.getId().getNumeroCotizacion());
		
		if(proformaCab != null && proformaCab.getPk() != null) {
			if (cotizacionRepuesto.getAprobadoCredito().equals("P")) {

				if(proformaCab.getDiasCredTemporal() != null 
					&& proformaCab.getDiasCredTemporal() > 0f ) {
					cliente.setLimiteCredi(0f);
				}
				
				cotizacionRepuesto.setCupoAsignadoNuevo(cliente.getLimiteCredi() != null  ? new BigDecimal(cliente.getLimiteCredi().toString()) : BigDecimal.ZERO);
				
				if(monto != null) {
					cupoDisponible = cotizacionRepuesto.getCupoAsignadoNuevo().subtract(monto);
					creditoSolicitado = cotizacionRepuesto.getCuotaMensual().multiply(cotizacionRepuesto.getPeriodo());
					nuevoCupoDisponible = cupoDisponible.subtract(creditoSolicitado);
				}
			} 
		} else {
			String error = "No se encontro la proforma de la cotización, ( ".concat("no_cia:")
					.concat(cotizacionRepuesto.getId().getNoCia())
					.concat(", agencia: ").concat(cotizacionRepuesto.getId().getCentro())
					.concat(", no_fisico: ").concat(cotizacionRepuesto.getNoFisico())
					.concat(", cotizacion:").concat(cotizacionRepuesto.getId().getNumeroCotizacion().toString());
			addErrorMessage("Error", error);
		}
	}

	public void guardarCotizacion() {
		try {
			if(proformaCab.getDiasCredTemporal() == null 
				|| proformaCab.getDiasCredTemporal() == 0f) {
				
				if (cotizacionRepuesto.getAprobadoCredito().equals("S") 
						&& cotizacionRepuesto.getCupoAsignadoNuevo() != null 
						&& cotizacionRepuesto.getCupoAsignadoNuevo().compareTo(BigDecimal.ZERO) == 1  // NUEVO CUPO DISPONIBLE
						&& cotizacionRepuesto.getCuotaDeAlcance() != null 
						&& cotizacionRepuesto.getCuotaDeAlcance().compareTo(BigDecimal.ZERO) == 0 // DIAS CREDITO TEMPORAL
						) {
					addErrorMessage("ERROR!",
							"No se puede aprobar el credito, el NUEVO CUPO DISPONIBLE es menor o igual a cero");
					return;
				}			
			}
	
			// Si las empresa es 10 y los dias son mayores a 10
			if (getCompania().getNoCia().equals(CommonConstants.MANSUERA)) {
				if (proformaCab.getDiasCredTemporal() > 10) {
					cotizacionRepuesto.setGarancheck(BigDecimal.ZERO);
				}
				if (proformaCab.getDiasCredTemporal() > 10 
						&& cotizacionRepuesto.getGarancheck() == null
						&& cotizacionRepuesto.getAprobadoCredito().equals("S")) {
					addErrorMessage("ERROR!", "Ingrese el número de autorización.");
					return;
				} else if (cotizacionRepuesto.getGarancheck().intValue() < 8
						&& cotizacionRepuesto.getAprobadoCredito().equals("S")) {
					addErrorMessage("ERROR!", "El número de autorización debe tener por lo menos 8 dígitos");
					return;
				}
			}
	
			if (nuevoCupoDisponible != null && nuevoCupoDisponible.intValue() <= 0 && proformaCab.getDiasCredTemporal() == 0) {
				if (cotizacionRepuesto.getAprobadoCredito().equals("S")) {
					cotizacionRepuesto.setAprobadoCredito("P");
					addErrorMessage("ERROR!","No puede aprobar el credito  !!NUEVO CUPO DISPONIBLE!! es menor o igual a cero");
				}
			}
	
			if (cotizacionRepuesto.getAprobadoCredito().equals("S")) {
				cotizacionRepuesto.setFechaAprobCred(new Date());
				cotizacionRepuesto.setComentarioAprob(cotizacionRepuesto.getComentarioAprob().concat(", USUARIO: ").concat(getUsuario().getUsuario()).concat(", ").concat(cotizacionRepuesto.getFechaAprobCred().toString()));
	
				if(cotizacionRepuesto.getRevisionComite() != null && cotizacionRepuesto.getRevisionComite().equals("S") && fileComprobante == null) {
					error("El comprobante no esta cargado, por favor cargarlo.");
				} else if(cotizacionRepuesto.getRevisionComite() != null && cotizacionRepuesto.getRevisionComite().equals("S") && fileComprobante != null) {
					cotizacionRepuesto.setPathComprobante(subirArchivoServidor(fileComprobante, nombreFileComprobante));			
				}
			} else if (cotizacionRepuesto.getAprobadoCredito().equals("N")) {
				proformaCab.setDiasCredTemporal(0d);
				cotizacionRepuesto.setGarancheck(null);
			}
		
			if (!cotizacionRepuesto.getAprobadoCredito().equals("P")) {
				proformasCabService.actualizar(proformaCab);
				cotizacionRepuestosServices.update(cotizacionRepuesto);
				envioMail();
				init();
			}
		} catch (UpdateException e) {
			String error = "No se actualizo la cotizacion "
			.concat(" nocia: ").concat(cotizacionRepuesto.getId().getNoCia())
			.concat(" no_cotizacion: ").concat(cotizacionRepuesto.getId().getNumeroCotizacion().toString())
			.concat(" centro: ").concat(cotizacionRepuesto.getId().getCentro());
			addErrorMessage("Error", error);
			logger.error(e.getMessage(), e.getCause());
		} catch (Exception e) {
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void ortogarCredito() {
		if(cliente == null || cliente.getClientePK() == null) {
			addErrorMessage("ERROR", "Seleccione un cliente, por favor.");
			return;
		} else if (agencia == null || agencia.getAgenciaPK() == null) {
			addErrorMessage("ERROR", "Seleccione una agencia, por favor.");
			return;
		}

		try {
			redirect("/serviciosWebPrime/jsf/procesos/serProFacturasCredito.jsf?"
					.concat("noCliente=").concat(cliente.getClientePK().getNoCliente().toString())
					.concat("&codAgencia=").concat(agencia.getAgenciaPK().getCodigo()));
		} catch (IOException e) {
			logger.error("No se logro realizar la redireccion.");
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void autORSaldosVencidos() {
		if(cliente == null || cliente.getClientePK() == null) {
			addErrorMessage("ERROR", "Seleccione un cliente, por favor.");
			return;
		} else if (lineaDeNegocio == null || lineaDeNegocio.getLineaNegocioPK() == null) {
			addErrorMessage("ERROR", "Seleccione una linea de negocio, por favor.");
			return;
		}

		try {
			redirect("/serviciosWebPrime/jsf/procesos/serproAutorizacionFacturacion.jsf?"
					.concat("noCliente=").concat(cliente.getClientePK().getNoCliente().toString())
					.concat("&noLinea=").concat(lineaDeNegocio.getLineaNegocioPK().getNoLinea()));
		} catch (IOException e) {
			logger.error("No se logro realizar la redireccion.");
			logger.error(e.getMessage(), e.getCause());
		}
	}

	public void selectCliente(Cliente cli) {
		cliente = cli;
		fillFields();
	}

	public void selectAgencia(Agencia age) {
		agencia = age;
	}

	public void selectLinea(LineaNegocio linea) {
		lineaDeNegocio = linea;
	}

	public void selectBloCliente(ParamDet item) {
		cliente.setSegCensal(Integer.valueOf(item.getParamDetPK().getCodigoDet()));
		setDescrBloqueoCli(item.getDescripcion());
		
		if ("0".equals(item.getParamDetPK().getCodigoDet())) {
			cliente.setFCierre(null);
			cliente.setCreditoSn("N");
		}else {
			cliente.setFCierre(new Date());
			cliente.setCreditoSn("S");
		}
		
		
	}

	public void checkPagare() {
		cliente.setPagare(isTienePagare() ? "S" : "N");
	}

	public void checkRevision() {
		cotizacionRepuesto.setRevisionComite(isRevisionComite() ? "S" : "N");
	}

	public void castDescrClase(String tipo, CxcClaseCliente clase) {
		if (tipo.equals(VEH)) {
			setDescrClaseVeh(clase.getDescripcion());
			cliente.setClaseVehiculos(clase.getCxcClaseClientePK().getClase());
		} else if (tipo.equals(SER)) {
			setDescrClaseSer(clase.getDescripcion());
			cliente.setClaseServicio(clase.getCxcClaseClientePK().getClase());
		} else if (tipo.equals(REP)) {
			setDescrClaseRep(clase.getDescripcion());
			cliente.setClaseRepuestos(clase.getCxcClaseClientePK().getClase());
		}
	}

	public void castCanalRep(ParamDet item) {
		cliente.setCanalRepuestos(item.getParamDetPK().getCodigoDet());
		setDescrCanalRep(item.getDescripcion());
	}

	public void clearFilters() {
		cliente = new Cliente();
		cedula = null;
		nombre = null;
	}

	public String estadoCuentaReport() {

		if(tipoReporte == null || tipoReporte.isEmpty()) {
			error("No olvide seleccionar el tipo de reporte que quiere descargar.");
			return null;
		}
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("NO_CIA", getCompania().getNoCia());
			parameters.put("CIA", getCompania().getNombre());
			parameters.put("NO_CLIENTE", cliente.getClientePK().getNoCliente());
			parameters.put("SERIE_FISICO", codLinea);

			String ctxPath = getServletContext().getRealPath("/");

			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			File[] drives = File.listRoots();
			for (File fileDrives : drives) {
				// Si es windows
				if (fileDrives.getPath().length() > 1 && fileDrives.getPath().substring(0, 1).equals("C")) {
					path = fileDrives.getPath() + path;
				}
			}

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			parameters.put("IMPRIME_CABECERA",
					"excel".equals(format) ? CommonConstants.FALSE_VALUE : CommonConstants.TRUE_VALUE);
			connection = utilServiceDelegate.getDataSource().getConnection();

			switch (tipoReporte) {
			case "R":
				getReportPath().setValue("/reportes/EstadoCuentaClienteResumido.jasper");
				break;
			case "D":
				getReportPath().setValue("/reportes/EstadoCuentaClienteDetallado.jasper");
				break;

			default:
				break;
			}

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + getStrReportPath(), parameters,
					connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			if ("excel".equals(format)) {
				exportXls(jasperPrint, response);
			}

			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint,
						response.getOutputStream());
			}

			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();

			virtualizer.cleanup();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (IOException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			logger.error(e.getMessage(), e.getCause());
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e.getCause());
			}
		}

		return null;
	}

	public void envioMail() {
		try {
			String estadoProforma = cotizacionRepuesto.getAprobadoCredito().equals("S") ? "APROBADA"
					: cotizacionRepuesto.getAprobadoCredito().equals("P") ? "PENDIENTE" : "NEGADA";
			String subject = "Proforma Generada - ".concat(estadoProforma);
			String message = "Estimados(a) todos ".concat("<br><br>")
					.concat(" Proforma generada ").concat(estadoProforma).concat(" No: ")
					.concat(cotizacionRepuesto.getNoFisico()).concat("<br><br>")
					.concat("LA PROFORMA ES POR EL VALOR DE: $").concat(cotizacionRepuesto.getCuotaMensual().multiply(cotizacionRepuesto.getPeriodo()).toString()).concat("<br><br>")
					.concat("Cliente: ").concat(cotizacionRepuesto.getCedulaDeudor()).concat(" - ").concat(cotizacionRepuesto.getNombres()).concat(" ").concat(cotizacionRepuesto.getApellidos()).concat("<br><br>")
					.concat("OBSERVACIÓN: ").concat(cotizacionRepuesto.getComentarioAprob().toUpperCase());
			
			UsuarioSis vendedor = usuarioServiceLocal.consultarDatosUsuario(cotizacionRepuesto.getUsuario());

			mailService.sendEmail(getUsuario().getEmail(), vendedor.getEmail(), vendedor.getNombre(), subject, new StringBuffer(message), true);
			mailService.sendEmail(getUsuario().getEmail(), getUsuario().getEmail(), vendedor.getNombre(), subject, new StringBuffer(message), true);
		} catch (GeneralException e) {
			addErrorMessage("ERROR", "No se logro hacer el envio del correo.");
		} catch (Exception e) {
			logger.error(e);
			addErrorMessage("ERROR", e.getMessage());
		}
	}
	
	public void anularProforma(CotizacionRepuesto item) {
		cotizacionRepuesto = item;
		try {

			Optional<Agencia> agencia = lstAgencias.stream().filter(codAge -> cotizacionRepuesto.getId().getCentro().equals(codAge.getNombre())).findFirst();
			
			cotizacionRepuesto.getId().setCentro(agencia.get().getAgenciaPK().getCodigo());
	
			proformaCab = proformasCabService.findByNoCiaCentroNoFisicoNoCotizacion(
								cotizacionRepuesto.getId().getNoCia(),
								cotizacionRepuesto.getId().getCentro(), 
								cotizacionRepuesto.getNoFisico(),
								cotizacionRepuesto.getId().getNumeroCotizacion());
			
			cotizacionRepuesto.setAprobadoCredito("N");
			proformaCab.setDiasCredTemporal(0d);
			cotizacionRepuesto.setGarancheck(null);
			cotizacionRepuesto.setUsuarioAprob(getUsuario().getUsuario());

			cotizacionRepuestosServices.update(cotizacionRepuesto);
			proformasCabService.actualizar(proformaCab);
			
			envioMail();
		} catch (UpdateException e) {
			logger.error(new StringBuilder("No se logro realizar la actualización para la cotizacion: ")
					.append(cotizacionRepuesto.getId().getNumeroCotizacion()));
		} catch (Exception e) {
			logger.error(e);
			addErrorMessage("ERROR", e.getMessage());
		}
		
		init();
	}
	
	public void adjuntarArchivo(FileUploadEvent event) throws IOException {
		UploadedFile uploadedFile = event.getFile();
		nombreFileComprobante = uploadedFile.getFileName();
		fileComprobante = uploadedFile.getInputstream();
	}

	
	private String subirArchivoServidor(InputStream file, String fileName) {
		String pathArchivo = null;
		String extension = "." + FileUpload.getFileExtension(fileName);
		fileName = fileName.length() > 50 ? fileName.substring(0, 50).concat(extension) : fileName;
		try {
			String pathRaiz = getCompania().getPathFileServer();
			FileUpload fileUpload = new FileUpload();
			pathArchivo = fileUpload.uploadToFileServer(getCompania().getIdTributario(), "COBRANZAS", "COMPROBANTES_PROFORMAS", cotizacionRepuesto.getNoCliente().toString(), pathRaiz, fileName, file);
		} catch (IOException e) {
			error("Se presento un error al cargar el archivo, intente nuevamente");
		}
		return pathArchivo;
	}
	
	public String downloadPagare() {
		if(cliente == null || cliente.getClientePK() == null || cliente.getClientePK().getNoCliente() == null) {
			addErrorMessage("ERROR", "Por favor pre cargar un cliente.");
			return null;
		}
		if(cliente.getLimiteCredi() == null ) {
			addErrorMessage("ERROR", "Por favor actualice el cupo del cliente y guarde el nuevo cupo.");
			return null;
		}
		
		Connection connection = null;
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("pNoCia", getCompania().getNoCia());
			parameters.put("pNoCliente", cliente.getClientePK().getNoCliente());

			getReportPath().setValue("/reportes/repPagareALaOrden.jasper");
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
			String ctxPath = getServletContext().getRealPath("/").concat((String) getReportPath().getValue());

			String path = System.getProperty("file.separator")
					.concat(FacesContext.getCurrentInstance().getExternalContext().getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH"))
					.concat(System.getProperty("file.separator"));
			

			connection = utilServiceDelegate.getDataSource().getConnection();
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath, parameters, connection);

			response.setContentType("application/pdf");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();

		} catch (SQLException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (IOException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			logger.error(e.getMessage(), e.getCause());
		} finally {
			try {
				if(connection != null && !connection.isClosed())
					connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage(), e.getCause());
			}
		}
		
		return null;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public String getCodAgencia() {
		return codAgencia;
	}

	public void setCodAgencia(String codAgencia) {
		this.codAgencia = codAgencia;
	}

	public String getCodLinea() {
		return codLinea;
	}

	public void setCodLinea(String codLinea) {
		this.codLinea = codLinea;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<ParamDet> getLstBloqueosCli() {
		return lstBloqueosCli;
	}

	public void setLstBloqueosCli(List<ParamDet> lstBloqueosCli) {
		this.lstBloqueosCli = lstBloqueosCli;
	}

	public List<CxcClaseCliente> getLstClaseVeh() {
		return lstClaseVeh;
	}

	public void setLstClaseVeh(List<CxcClaseCliente> lstClaseVeh) {
		this.lstClaseVeh = lstClaseVeh;
	}

	public List<CxcClaseCliente> getLstClaseRep() {
		return lstClaseRep;
	}

	public void setLstClaseRep(List<CxcClaseCliente> lstClaseRep) {
		this.lstClaseRep = lstClaseRep;
	}

	public List<CxcClaseCliente> getLstClaseSer() {
		return lstClaseSer;
	}

	public void setLstClaseSer(List<CxcClaseCliente> lstClaseSer) {
		this.lstClaseSer = lstClaseSer;
	}

	public boolean isTienePagare() {
		return tienePagare;
	}

	public void setTienePagare(boolean tienePagare) {
		this.tienePagare = tienePagare;
	}

	public String getDescrBloqueoCli() {
		return descrBloqueoCli;
	}

	public void setDescrBloqueoCli(String descrBloqueoCli) {
		this.descrBloqueoCli = descrBloqueoCli;
	}

	public String getDescrClaseVeh() {
		return descrClaseVeh;
	}

	public void setDescrClaseVeh(String descrClaseVeh) {
		this.descrClaseVeh = descrClaseVeh;
	}

	public String getDescrClaseSer() {
		return descrClaseSer;
	}

	public void setDescrClaseSer(String descrClaseSer) {
		this.descrClaseSer = descrClaseSer;
	}

	public String getDescrClaseRep() {
		return descrClaseRep;
	}

	public void setDescrClaseRep(String descrClaseRep) {
		this.descrClaseRep = descrClaseRep;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Cliente> getLstClientes() {
		return lstClientes;
	}

	public void setLstClientes(List<Cliente> lstClientes) {
		this.lstClientes = lstClientes;
	}

	public List<Agencia> getLstAgenciasSer() {
		return lstAgenciasSer;
	}

	public void setLstAgenciasSer(List<Agencia> lstAgenciasSer) {
		this.lstAgenciasSer = lstAgenciasSer;
	}

	public List<LineaNegocio> getLstLineasNeg() {
		return lstLineasNeg;
	}

	public void setLstLineasNeg(List<LineaNegocio> lstLineasNeg) {
		this.lstLineasNeg = lstLineasNeg;
	}

	public Agencia getAgencia() {
		return agencia;
	}

	public void setAgencia(Agencia agencia) {
		this.agencia = agencia;
	}

	public LineaNegocio getLineaDeNegocio() {
		return lineaDeNegocio;
	}

	public void setLineaDeNegocio(LineaNegocio lineaDeNegocio) {
		this.lineaDeNegocio = lineaDeNegocio;
	}

	public List<CotizacionRepuesto> getListCotizacionRep() {
		return listCotizacionRep;
	}

	public void setListCotizacionRep(List<CotizacionRepuesto> listCotizacionRep) {
		this.listCotizacionRep = listCotizacionRep;
	}

	public CotizacionRepuesto getCotizacionRepuesto() {
		return cotizacionRepuesto;
	}

	public void setCotizacionRepuesto(CotizacionRepuesto cotizacionRepuesto) {
		this.cotizacionRepuesto = cotizacionRepuesto;
	}

	public String getNoCliente() {
		return noCliente;
	}

	public void setNoCliente(String noCliente) {
		this.noCliente = noCliente;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public ProformasCab getProformaCab() {
		return proformaCab;
	}

	public void setProformaCab(ProformasCab proformaCab) {
		this.proformaCab = proformaCab;
	}

	public BigDecimal getCupoDisponible() {
		return cupoDisponible;
	}

	public void setCupoDisponible(BigDecimal cupoDisponible) {
		this.cupoDisponible = cupoDisponible;
	}

	public BigDecimal getCreditoSolicitado() {
		return creditoSolicitado;
	}

	public void setCreditoSolicitado(BigDecimal creditoSolicitado) {
		this.creditoSolicitado = creditoSolicitado;
	}

	public BigDecimal getNuevoCupoDisponible() {
		return nuevoCupoDisponible;
	}

	public void setNuevoCupoDisponible(BigDecimal nuevoCupoDisponible) {
		this.nuevoCupoDisponible = nuevoCupoDisponible;
	}

	public boolean isRevisionComite() {
		return revisionComite;
	}

	public void setRevisionComite(boolean revisionComite) {
		this.revisionComite = revisionComite;
	}

	public List<ParamDet> getLstCanalesRepuestos() {
		return lstCanalesRepuestos;
	}

	public void setLstCanalesRepuestos(List<ParamDet> lstCanalesRepuestos) {
		this.lstCanalesRepuestos = lstCanalesRepuestos;
	}

	public String getDescrCanalRep() {
		return descrCanalRep;
	}

	public void setDescrCanalRep(String descrCanalRep) {
		this.descrCanalRep = descrCanalRep;
	}

	public Double getVencido30() {
		return vencido30;
	}

	public void setVencido30(Double vencido30) {
		this.vencido30 = vencido30;
	}

	public Double getVencido60() {
		return vencido60;
	}

	public void setVencido60(Double vencido60) {
		this.vencido60 = vencido60;
	}

	public Double getVencido90() {
		return vencido90;
	}

	public void setVencido90(Double vencido90) {
		this.vencido90 = vencido90;
	}

	public Double getVencidoMas90() {
		return vencidoMas90;
	}

	public void setVencidoMas90(Double vencidoMas90) {
		this.vencidoMas90 = vencidoMas90;
	}

	public Double getTotalCartera() {
		return totalCartera;
	}

	public void setTotalCartera(Double totalCartera) {
		this.totalCartera = totalCartera;
	}

	public int getTabActiveIndex() {
		return tabActiveIndex;
	}

	public void setTabActiveIndex(int tabActiveIndex) {
		this.tabActiveIndex = tabActiveIndex;
	}

	public List<CotizacionRepuesto> getListCotiByCliente() {
		return listCotiByCliente;
	}

	public void setListCotiByCliente(List<CotizacionRepuesto> listCotiByCliente) {
		this.listCotiByCliente = listCotiByCliente;
	}

	public String getTipoReporte() {
		return tipoReporte;
	}

	public void setTipoReporte(String tipoReporte) {
		this.tipoReporte = tipoReporte;
	}

	public List<LineaNegocio> getLstLineasNegocio() {
		return lstLineasNegocio;
	}

	public void setLstLineasNegocio(List<LineaNegocio> lstLineasNegocio) {
		this.lstLineasNegocio = lstLineasNegocio;
	}

	public InputStream getFileComprobante() {
		return fileComprobante;
	}

	public void setFileComprobante(InputStream fileComprobante) {
		this.fileComprobante = fileComprobante;
	}

	public String getNombreFileComprobante() {
		return nombreFileComprobante;
	}

	public void setNombreFileComprobante(String nombreFileComprobante) {
		this.nombreFileComprobante = nombreFileComprobante;
	}
	
}
