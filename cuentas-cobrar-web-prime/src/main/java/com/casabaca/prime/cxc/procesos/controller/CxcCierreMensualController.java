package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.administration.ejb.enums.CompaniaBitacoraEnum;
import com.casabaca.administration.ejb.model.PlantillaMail;
import com.casabaca.administration.ejb.service.PlantillaMailServiceLocal;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.Canton;
import com.casabaca.common.ejb.model.CantonPK;
import com.casabaca.common.ejb.model.NomEmpleados;
import com.casabaca.common.ejb.model.NomEmpleadosPK;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.CantonServiceLocal;
import com.casabaca.common.ejb.service.NominaEmpleadosServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.service.S3SPropertiesServiceBean;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcCierreMensualCompania;
import com.casabaca.cxc.ejb.servicio.CxcCierreMensualCompaniaServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.WServiceException;
import com.casabaca.nomina.ejb.modelo.NomAreaPK;
import com.casabaca.nomina.ejb.modelo.NomDepartamentoPK;
import com.casabaca.nomina.ejb.modelo.NomDivisionPK;
import com.casabaca.nomina.ejb.modelo.NomSeccion;
import com.casabaca.nomina.ejb.modelo.NomSeccionPK;
import com.casabaca.nomina.ejb.servicio.NomAreaServicioLocal;
import com.casabaca.nomina.ejb.servicio.NomDepartamentoServicioLocal;
import com.casabaca.nomina.ejb.servicio.NomDivisionServicioLocal;
import com.casabaca.nomina.ejb.servicio.NomSeccionServicioLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.rest.wsNpsIntegrado.entidad.BodyInDatosNps;
import com.casabaca.rest.wsNpsIntegrado.entidad.OutputDatosNps;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.AgenciaPK;
import com.casabaca.s3s.ejb.model.SisUsuariosBitacora;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.SisUsuariosBitacoraServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.webservice.wsTareasNps.NpsIntegradoRest;
import com.casabaca.webservice.wsTareasNps.impl.NpsIntegradoImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@ViewScoped
@ManagedBean(name = "cxcCierreMensualController")
public class CxcCierreMensualController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CxcCierreMensualController.class.getName());
	
	/**
	 * VARIABLES DE SERVICIOS
	 */
	@EJB(lookup = NombreJNDI.CXC_CIERRE_MENSUAL_COMPANIA_SERVICE_LOCAL)
	private CxcCierreMensualCompaniaServiceLocal cierreMensualCompaniaService;
	
	//IMPLEMENTACION NPS
	@EJB(lookup = NombreJNDI.NOMINA_EMPLEADOS_SERVICE)
	private NominaEmpleadosServiceLocal nominaEmpleadosService;
	@EJB(lookup = NombreJNDI.PLANTILLA_MAIL_BEAN)
	private PlantillaMailServiceLocal plantillaMailServiceLocal;
	@EJB(lookup = NombreJNDI.S3S_PROPERTIES_SERVICE)
	private S3SPropertiesServiceBean s3sPropertiesService;
	@EJB(lookup = NombreJNDI.NOM_DEPARTAMENTO_SERVICIO_LOCAL)
	private NomDepartamentoServicioLocal nomDepartamentoServicioLocal;
	@EJB(lookup = NombreJNDI.NOM_DIVISION_SERVICIO_LOCAL)
	private NomDivisionServicioLocal nomDivisionServicioLocal;
	@EJB(lookup = NombreJNDI.NOM_SECCION_SERVICIO_LOCAL)
	private NomSeccionServicioLocal nomSeccionServicioLocal;
	@EJB(lookup = NombreJNDI.NOM_AREA_SERVICIO_LOCAL)
	private NomAreaServicioLocal nomAreaServicioLocal;
	@EJB(lookup = NombreJNDI.SIS_USUSARIOS_BITACORA_SERVICE)
	private SisUsuariosBitacoraServiceLocal sisUsuariosBitacoraService;
	@EJB(lookup = NombreJNDI.CANTON_SERVICE)
	private CantonServiceLocal cantonService;
	@EJB(lookup=NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;
	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	@EJB(lookup=NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;
	
	private List<CxcCierreMensualCompania> listaCierres;
	private List<CxcCierreMensualCompania> listaSeleccionada;
	
	// IMPLEMENTACION NPS
	public static final String NPS_MAIL_URL = "COBRANZAS_NPS";
	public static final String NPS_MAIL = "NOTIFICA_NPS";
	private static final String APP_NPS_BACKEND_API = "appnpsbackend.API_NPS";
	private static final String APP_NPS_FRONTEND_URL = "appnpsfrontend.URL_NPS";
	private static final int MAX_LENGTH = 600;
	public static final String RUTA_IMAGEN_C = "/resources/images/nps/centric_cab";
	public static final String RUTA_IMAGEN_D = "/resources/images/nps/centric_det";
	public static final String EXT_IMAGEN = ".png";
	
	private String urlCobranzas;
	private Boolean isHabilitaNPS;
	private ParamDet urlEmpresa;
	private String codigoParamDet;
	
	private List<BodyInDatosNps> npsPendienteLst;

	@PostConstruct
	public void init(){
		listaCierres = cierreMensualCompaniaService.obtenerUltimosCierresCompania();
		listaSeleccionada = new ArrayList<CxcCierreMensualCompania>();
		this.isHabilitaNPS= Boolean.FALSE;
		this.codigoParamDet = "NPSCOB";
		this.urlCobranzas = null;
//		for(CxcCierreMensualCompania cierre: listaCierres){
//			if(cierre.getContadorRespaldo().intValue() > 0){
//				listaSeleccionada.add(cierre);
//			}
//		}
		consultarNpsPendientes();
	}
	
	
	public void realizarCierreEmpresas(){
		for(CxcCierreMensualCompania cierre: listaSeleccionada){
			try{
				cierre.setContadorRespaldo(cierreMensualCompaniaService.obtenerTotalRegistrosSaldosMensuales(cierre.getCierreMensualCompaniaPk().getNoCia(), FechaUtils.getAno(cierre.getCierreMensualCompaniaPk().getFechaCierre()), FechaUtils.getMes(cierre.getCierreMensualCompaniaPk().getFechaCierre())));
				if(cierre.getContadorRespaldo() == null || cierre.getContadorRespaldo().intValue() == 0 ){
				cierre = cierreMensualCompaniaService.realizarCierreCompania(cierre);
				cierre.setUsuarioCierre(getUsuario().getUsuario());
				cierre.setFechaProceso(new Date());
				if("Exitoso".equals(cierre.getEstadoCierre())){
					cierre.setContadorRespaldo(cierreMensualCompaniaService.obtenerTotalRegistrosSaldosMensuales(cierre.getCierreMensualCompaniaPk().getNoCia(), FechaUtils.getAno(cierre.getCierreMensualCompaniaPk().getFechaCierre()), FechaUtils.getMes(cierre.getCierreMensualCompaniaPk().getFechaCierre())));
					isHabilitaNPS= Boolean.TRUE;
					addInfoMessage("INFO", "Se ha realizado el cierre de la empresa de forma exitosa "+cierre.getNombreCompania());
				}else{
					addInfoMessage("INFO", "Ocurrio un error al realizar el cierre de la empresa  "+cierre.getNombreCompania()+" -Novedad: "+cierre.getNovedadCierre());
				}
				}else{
					cierre.setNovedadCierre("El cierre de esta compania ya ha sido realizado");
					addInfoMessage("INFO", "El cierre de la empresa " + cierre.getNombreCompania()+ " ya ha sido realizado");
				}
				cierreMensualCompaniaService.actualizarCierreCompania(cierre);
				
			// IMPLEMENTACION NPS
				if (isHabilitaNPS) {
					if(cierre.getCierreMensualCompaniaPk().getNoCia().equals(CompaniaBitacoraEnum.CASABACA.getCodigo())) {
						List<Object[]> listaUsuariosMail = mailUsuarioService.findByEmailGrupo("NPS01");
						for (Object[] aux : listaUsuariosMail) {
							String usuario = (String) aux[0];
							validarUrlEmpresa(CompaniaBitacoraEnum.CASABACA.getCodigo(), this.codigoParamDet, CompaniaBitacoraEnum.CASABACA.getDescipcion());
							procesamientoNPS(cierre, usuario, CompaniaBitacoraEnum.CASABACA.getCodigo() );
							}
						}
					else if(cierre.getCierreMensualCompaniaPk().getNoCia().equals(CompaniaBitacoraEnum.M1001_TALLERES.getCodigo())) {
						List<Object[]> listaUsuariosMail = mailUsuarioService.findByEmailGrupo("NPS06");
						for (Object[] aux : listaUsuariosMail) {
							String usuario = (String) aux[0];
							validarUrlEmpresa(CompaniaBitacoraEnum.M1001_TALLERES.getCodigo(), this.codigoParamDet, CompaniaBitacoraEnum.M1001_TALLERES.getDescipcion());
							procesamientoNPS(cierre, usuario, CompaniaBitacoraEnum.M1001_TALLERES.getCodigo() );
							}
						} 
					else if(cierre.getCierreMensualCompaniaPk().getNoCia().equals(CompaniaBitacoraEnum.SUZUKI.getCodigo())) {
						List<Object[]> listaUsuariosMail = mailUsuarioService.findByEmailGrupo("NPS08");
						for (Object[] aux : listaUsuariosMail) {
							String usuario = (String) aux[0];
							validarUrlEmpresa(CompaniaBitacoraEnum.SUZUKI.getCodigo(), this.codigoParamDet, CompaniaBitacoraEnum.SUZUKI.getDescipcion());
							procesamientoNPS(cierre, usuario, CompaniaBitacoraEnum.SUZUKI.getCodigo() );
							}
						} 
					else if(cierre.getCierreMensualCompaniaPk().getNoCia().equals(CompaniaBitacoraEnum.MANSUERA.getCodigo())) {
						List<Object[]> listaUsuariosMail = mailUsuarioService.findByEmailGrupo("NPS10");
						for (Object[] aux : listaUsuariosMail) {
							String usuario = (String) aux[0];
							validarUrlEmpresa(CompaniaBitacoraEnum.MANSUERA.getCodigo(), this.codigoParamDet, CompaniaBitacoraEnum.MANSUERA.getDescipcion());
							procesamientoNPS(cierre, usuario, CompaniaBitacoraEnum.MANSUERA.getCodigo() );
							}
						} 
					else if(cierre.getCierreMensualCompaniaPk().getNoCia().equals(CompaniaBitacoraEnum.NEXUMCORP.getCodigo())) {
						List<Object[]> listaUsuariosMail = mailUsuarioService.findByEmailGrupo("NPST1");
						for (Object[] aux : listaUsuariosMail) {
							String usuario = (String) aux[0];
							validarUrlEmpresa(CompaniaBitacoraEnum.NEXUMCORP.getCodigo(), this.codigoParamDet, CompaniaBitacoraEnum.NEXUMCORP.getDescipcion());
							procesamientoNPS(cierre, usuario, CompaniaBitacoraEnum.NEXUMCORP.getCodigo() );
							}
						}  
					else if(cierre.getCierreMensualCompaniaPk().getNoCia().equals(CompaniaBitacoraEnum.TOYOCOSTA.getCodigo())) {
						List<Object[]> listaUsuariosMail = mailUsuarioService.findByEmailGrupo("NPSTY");
						for (Object[] aux : listaUsuariosMail) {
							String usuario = (String) aux[0];
							validarUrlEmpresa(CompaniaBitacoraEnum.TOYOCOSTA.getCodigo(), this.codigoParamDet, CompaniaBitacoraEnum.TOYOCOSTA.getDescipcion());
							procesamientoNPS(cierre, usuario, CompaniaBitacoraEnum.TOYOCOSTA.getCodigo() );
							}
						} 
					else if(cierre.getCierreMensualCompaniaPk().getNoCia().equals(CompaniaBitacoraEnum.EPICENTRO.getCodigo())) {
						List<Object[]> listaUsuariosMail = mailUsuarioService.findByEmailGrupo("NPS09");
						for (Object[] aux : listaUsuariosMail) {
							String usuario = (String) aux[0];
							validarUrlEmpresa(CompaniaBitacoraEnum.EPICENTRO.getCodigo(), this.codigoParamDet, CompaniaBitacoraEnum.EPICENTRO	.getDescipcion());
							procesamientoNPS(cierre, usuario, CompaniaBitacoraEnum.EPICENTRO.getCodigo() );
							}
						} 
					else {
						addWarnMessage("Advertencia!", "Por favor debe parametrizar Grupo Mail NPS para la empresa "+cierre.getNombreCompania());
					}
				}
			this.isHabilitaNPS= Boolean.FALSE;
			}catch(Exception ue){
				logger.info("Error al generar Cartera General Año, Mes");
			}
		}
		
	}

	//IMPLEMENTACION NPS
	// REGISTRO DE PROCESO NPS, LLAMADO AL MICROSERVICIO Y ENVIO DE ENCUESTA NPS
	public void procesamientoNPS(CxcCierreMensualCompania cierre, String usuarioDb, String noCia) {
		String url = s3sPropertiesService.getProperty(APP_NPS_BACKEND_API) + "createTareaNps";
		String token = "pendiente";
		try {
		NpsIntegradoRest proceso = new NpsIntegradoImpl();
		BodyInDatosNps item = parcearDatos(cierre, usuarioDb, noCia);
		OutputDatosNps resp;
			resp = proceso.enviarTareaReq(item, url, token);
		Long idNps = 0l;
		String emailCliente = "";
		String nombreCliente1 = "";
		String emailAsesor = "";
		String nombreAsesor = "";
		String departamento = "";
		String usuarioAsesor = "";
		String usuarioCliente = "";
		String tituloTarea = "";
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(resp.getCode());
			logger.info(jsonNode);
			idNps = jsonNode.get("idNps").longValue();
			emailCliente = jsonNode.get("emailCliente").textValue();
			nombreCliente1 = jsonNode.get("nombresCliente").textValue();
			emailAsesor = jsonNode.get("emailAsesor").textValue();
			nombreAsesor = jsonNode.get("nombreAsesor").textValue();
			departamento = jsonNode.get("departamento").textValue();
			usuarioAsesor = jsonNode.get("usuarioAsesor").textValue();
			usuarioCliente = jsonNode.get("usuarioCliente").textValue();
			tituloTarea = jsonNode.get("tituloTarea").textValue();
			logger.info("idNps: " + idNps);
		} catch (Exception err) {
			logger.error("Exception : " + err.toString());
		}
		if (!"".equals(emailAsesor) && !"".equals(emailCliente)) {
			this.enviaMailNps(emailAsesor, emailCliente, nombreAsesor, nombreCliente1,
					"Califique nuestro servicio finalizado por: " + nombreAsesor, idNps.toString(),
					tituloTarea);
		}
		logger.info(resp.getMessage());
		} catch (WServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void consultarNpsPendientes() {
		String url = s3sPropertiesService.getProperty(APP_NPS_BACKEND_API) + "listarCenIntegradoNps";

		try {
			NpsIntegradoRest proceso = new NpsIntegradoImpl();
			BodyInDatosNps datosEntrada = new BodyInDatosNps();
			datosEntrada.setEstado("PENDIENTE");
			datosEntrada.setTipoProceso("CXC");
			OutputDatosNps resp;
			resp = proceso.obtenerNPS(datosEntrada, url);

			try {
				ObjectMapper objectMapper = new ObjectMapper();
				npsPendienteLst = objectMapper.readValue(resp.getCode(), new TypeReference<List<BodyInDatosNps>>(){});
			} catch (Exception err) {
				logger.error("Exception : " + err.toString(),err);
				npsPendienteLst=new ArrayList<BodyInDatosNps>();
			}
		
			logger.info(resp.getMessage());
		} catch (WServiceException e) {
			logger.error("Exception : " + e.toString(),e);
			npsPendienteLst=new ArrayList<BodyInDatosNps>();
		}
	}
	
	public boolean presentarEnvioNpsPendientes() {
		return !npsPendienteLst.isEmpty();
	}

	
	public void envioNpsPendientes() {
			for (BodyInDatosNps nps : npsPendienteLst) {
				try {
					if (!"".equals(nps.getEmailAsesor()) && !"".equals(nps.getEmailCliente())) {
						logger.info("Asesor:"+nps.getNombreAsesor()+"-"+nps.getEmailAsesor()+
								", cliente=" +nps.getNombresCliente()+"-"+nps.getEmailCliente()+
								" tarea= "+nps.getIdNps().toString()+"-"+nps.getTituloTarea());
						this.enviaMailNps(nps.getEmailAsesor(), nps.getEmailCliente(), nps.getNombreAsesor(), nps.getNombresCliente(),
								"Califique nuestro servicio finalizado por: " + nps.getNombreAsesor(), nps.getIdNps().toString(),
								nps.getTituloTarea());
					}
				} catch (Exception err) {
					logger.error("Exception : " + err.toString());
				}
			}
			consultarNpsPendientes();

	}
	
	//IMPLEMENTACION NPS
	private BodyInDatosNps parcearDatos(CxcCierreMensualCompania cierre, String usuarioCliente, String noCiaCliente) {
		if (cierre != null) {
			BodyInDatosNps datosEntrada = new BodyInDatosNps();
			// Datos Tarea
			datosEntrada.setCodigoTareq(String.valueOf(cierre.getCierreMensualCompaniaPk().getNoCia() + "-" + cierre.getContadorRespaldo()));
			datosEntrada.setTituloTarea("Procesamiento Cierre Mensual de Cobranzas para la Empresa: " + 
			String.valueOf(cierre.getNombreCompania()) + " con fecha: " + 
			FechaUtils.formatearFecha(cierre.getCierreMensualCompaniaPk().getFechaCierre(), "dd MMMMM yyyy"));
			if (cierre.getNovedadCierre().length() > MAX_LENGTH ) {
				datosEntrada.setDescripcionTarea(cierre.getNovedadCierre().substring(0, MAX_LENGTH));
			} else {
				datosEntrada.setDescripcionTarea(cierre.getNovedadCierre());
			}
			datosEntrada.setTipoProceso("COB"); // Tarea Modulo de COBRANZAS
//			datosEntrada.setFechaRespuesta(FechaUtils.formatearFecha(new Date(), "dd/MM/yyyy HH:mm"));
			datosEntrada.setFechaAtencion(cierre.getFechaProceso() != null
					? FechaUtils.formatearFecha(cierre.getFechaProceso(), "dd/MM/yyyy HH:mm")
					: FechaUtils.formatearFecha(new Date(), "dd/MM/yyyy HH:mm"));

			// Datos Cliente / Solicita
			datosEntrada.setUsuarioCliente(usuarioCliente);
			NomEmpleados empleadoSol = new NomEmpleados();
			UsuarioSis client = null;
			NomEmpleados empleadosl = null;
			try {
				Optional<SisUsuariosBitacora> usuarioBitacora = sisUsuariosBitacoraService
						.findUserByCiaUser(noCiaCliente, usuarioCliente);
				client = usuarioSisService.getUsuarioByNociaUser(noCiaCliente, usuarioCliente);
				
				if (usuarioBitacora.isPresent()) {
					empleadoSol.setNomEmpleadosPK(new NomEmpleadosPK());
					empleadoSol.getNomEmpleadosPK().setNoCia(usuarioBitacora.get().getId().getNoCia());
					empleadoSol.setNombre(usuarioBitacora.get().getNombre());
					empleadoSol.setMail(usuarioBitacora.get().getEmail());
					empleadoSol.setUsuarioDb(usuarioBitacora.get().getId().getUsuario());
					datosEntrada.setNoCia(empleadoSol.getNomEmpleadosPK().getNoCia());
					datosEntrada.setNombresCliente(empleadoSol.getNombre());
					datosEntrada.setEmailCliente(empleadoSol.getMail());
					if (client != null && client.getNoEmple() != null) {
						empleadosl = nominaEmpleadosService.getEmpleado(noCiaCliente, client.getNoEmple());
					}
				} else {
					if (client != null && client.getNoEmple() != null) {
						empleadosl = nominaEmpleadosService.getEmpleado(noCiaCliente, client.getNoEmple());
						empleadoSol.setNomEmpleadosPK(new NomEmpleadosPK());
						empleadoSol.getNomEmpleadosPK().setNoCia(noCiaCliente);
						empleadoSol.setNombre(client.getNombre());
						empleadoSol.setMail(client.getEmail() );
						empleadoSol.setUsuarioDb(client.getUsuario());
						datosEntrada.setNoCia(empleadoSol.getNomEmpleadosPK().getNoCia());
						datosEntrada.setNombresCliente(empleadoSol.getNombre());
						datosEntrada.setEmailCliente(empleadoSol.getMail());
					}
				}
				if (empleadosl != null) {
					//Validacion si solo se encuentra info en tabla Nom_Empleados 
					if(!usuarioBitacora.isPresent() && client == null) {
						datosEntrada.setNombresCliente(empleadosl.getNombre());
						datosEntrada.setEmailCliente(empleadosl.getMail());
						datosEntrada.setNoCia(empleadosl.getNomEmpleadosPK().getNoCia());
					}
					datosEntrada.setCedulaCliente(empleadosl.getCedula());
					datosEntrada.setCelularCliente(empleadosl.getCelular());
					datosEntrada.setTelefonoCliente(empleadosl.getTelefono());
					NomAreaPK nomEmpPk = new NomAreaPK();
					nomEmpPk.setArea(empleadosl.getArea());
					nomEmpPk.setNoCia(empleadosl.getNomEmpleadosPK().getNoCia());
					String empresa = nomAreaServicioLocal.findByPK(nomEmpPk).getDescri();
					NomDepartamentoPK depPk = new NomDepartamentoPK(empleadosl.getNomEmpleadosPK().getNoCia(),
							empleadosl.getArea(), empleadosl.getDepto());
					datosEntrada.setEmpresa(empresa);
					datosEntrada.setArea(nomDepartamentoServicioLocal.findByPK(depPk).getDescri());
					NomSeccionPK secPk = new NomSeccionPK();
					secPk.setArea(empleadosl.getArea());
					secPk.setDepa(empleadosl.getDepto());
					secPk.setDivision(empleadosl.getDivision());
					secPk.setNoCia(empleadosl.getNomEmpleadosPK().getNoCia());
					secPk.setSeccion(empleadosl.getSeccion());
					datosEntrada.setAgencia(nomSeccionServicioLocal.findByPK(secPk).getDescri());
					NomDivisionPK divPk = new NomDivisionPK(empleadosl.getNomEmpleadosPK().getNoCia(),
							empleadosl.getArea(), empleadosl.getDepto(), empleadosl.getDivision());
					datosEntrada.setDepartamento(nomDivisionServicioLocal.findByPK(divPk).getDescri());

					// Agregar correctamente la ciudad o canton
					try {
						NomSeccion nomSecc = nomSeccionServicioLocal.findByPK(secPk);
						AgenciaPK pkagencia = new AgenciaPK(nomSecc.getCodigoAgencia(),
								empleadosl.getNomEmpleadosPK().getNoCia());
						Agencia agencia = agenciaService.getAgenciaByPK(pkagencia);
						CantonPK cantpk = new CantonPK(agencia.getCiudad(), agencia.getProvincia(), "01");
						Canton cant = cantonService.getCantonByPK(cantpk);
						datosEntrada.setCiudad(cant.getDescripcion());
					} catch (Exception ex) {
						logger.error("Error ciudad: " + ex.getMessage());
					}
				}
			} catch (FindException e) {
				logger.error("Error cliente" + e.getMessage());
			}

			// Datos Asesor / Atiende
			datosEntrada.setUsuarioAsesor(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
			NomEmpleados asesor = new NomEmpleados();
			UsuarioSis usuario = null;
			List<NomEmpleados> usSol = null;
			try {
				usSol = nominaEmpleadosService
						.findByEmpleadoActivo(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
			} catch (FindException e) {
				logger.error("Error asesor activo: " + e.getMessage());
			}
			usuario = usuarioSisService.getUsuarioByNociaUser(getCompania().getNoCia(),
					getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
			if (usSol != null && !usSol.isEmpty()) {
				asesor = usSol.get(0);
				Optional<SisUsuariosBitacora> usuarioBitacora = sisUsuariosBitacoraService.findUserByCiaUser(
						asesor.getNomEmpleadosPK().getNoCia(),
						getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				if (usuarioBitacora.isPresent()) {
					asesor.setNomEmpleadosPK(new NomEmpleadosPK());
					asesor.getNomEmpleadosPK().setNoCia(usuarioBitacora.get().getId().getNoCia());
					asesor.setNombre(usuarioBitacora.get().getNombre());
					asesor.setMail(usuarioBitacora.get().getEmail());
					asesor.setUsuarioDb(usuarioBitacora.get().getId().getUsuario());
					datosEntrada.setEmailAsesor(asesor.getMail());
					datosEntrada.setNombreAsesor(asesor.getNombre());
					datosEntrada.setCedulaAsesor(asesor.getCedula());
					// Departamento y Nocia del Asesor
					NomDepartamentoPK depPk = new NomDepartamentoPK(asesor.getNomEmpleadosPK().getNoCia(),
							asesor.getArea(), asesor.getDepto());
					NomSeccionPK secPk = new NomSeccionPK();
					secPk.setArea(asesor.getArea());
					secPk.setDepa(asesor.getDepto());
					secPk.setDivision(asesor.getDivision());
					secPk.setNoCia(asesor.getNomEmpleadosPK().getNoCia());
					secPk.setSeccion(asesor.getSeccion());
					// Nuevos campos
					NomDivisionPK divPk = new NomDivisionPK(asesor.getNomEmpleadosPK().getNoCia(), asesor.getArea(),
							asesor.getDepto(), asesor.getDivision());
					//Envio directo de parametros de departamento y CIA Asesor pedido Jefe TI
					datosEntrada.setDepartamentoAsesor("CREDITO Y COBRANZAS");
					datosEntrada.setNociaAsesor("04");
				}
			} else {
				Optional<SisUsuariosBitacora> usuarioBitacora = sisUsuariosBitacoraService
						.findByUserName(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				
				if (usuarioBitacora.isPresent()) {
					asesor.setNomEmpleadosPK(new NomEmpleadosPK());
					asesor.getNomEmpleadosPK().setNoCia(usuarioBitacora.get().getId().getNoCia());
					asesor.setNombre(usuarioBitacora.get().getNombre());
					asesor.setMail(usuarioBitacora.get().getEmail());
					asesor.setUsuarioDb(usuarioBitacora.get().getId().getUsuario());
					datosEntrada.setEmailAsesor(asesor.getMail());
					datosEntrada.setNombreAsesor(asesor.getNombre());
					//Envio directo de parametros de departamento y CIA Asesor pedido Jefe TI
					datosEntrada.setDepartamentoAsesor("CREDITO Y COBRANZAS");
					datosEntrada.setNociaAsesor("04");
				}
			}
			if (usuario != null) {
				asesor.setNomEmpleadosPK(new NomEmpleadosPK());
				asesor.getNomEmpleadosPK().setNoCia(usuario.getNoCia());
				asesor.setNombre(usuario.getNombre());
				asesor.setMail(usuario.getEmail());
				asesor.setUsuarioDb(usuario.getUsuario());
				datosEntrada.setEmailAsesor(asesor.getMail());
				datosEntrada.setNombreAsesor(asesor.getNombre());
				datosEntrada.setCedulaAsesor(asesor.getCedula());
				//Envio directo de parametros de departamento y CIA Asesor pedido Jefe TI
				datosEntrada.setDepartamentoAsesor("CREDITO Y COBRANZAS");
				datosEntrada.setNociaAsesor("04");
				
			}
			datosEntrada.setEstado("ENVIADO");
			return datosEntrada;
		}
		return null;
	}

	/***
	 * Metodo para envio de correos NPS
	 * 
	 * @param from
	 * @param to
	 */
	//IMPLEMENTACION NPS
	private void enviaMailNps(String from, String to, String fromName, String toName, String subject, String encuesta,
			String tituloTarea) {
		StringBuffer message = new StringBuffer();
		PlantillaMail plantilla = new PlantillaMail();
		try {
			//Metodo para validar si una empresa tiene URL de Cobranzas
			if(this.urlCobranzas != null) {
				plantilla = plantillaMailServiceLocal.obtenerPlantilla(NPS_MAIL_URL);
				message.append(plantilla.getValor().replace("xxx", toName).replace("ooo", tituloTarea)
						.replace("eee", this.urlCobranzas)
						.replace("yyy", fromName)
						.replace("zzz", s3sPropertiesService.getProperty(APP_NPS_FRONTEND_URL) + encuesta.toString()));				
			} else {
				plantilla = plantillaMailServiceLocal.obtenerPlantilla(NPS_MAIL);
				String mensaje = plantilla.getValor().replace("xxx", toName).replace("ooo", tituloTarea)
						.replace("yyy", fromName)
						.replace("zzz", s3sPropertiesService.getProperty(APP_NPS_FRONTEND_URL) + encuesta.toString())
						.replace("Nota de solucion:", "").replace("No_Aplica", "");
				message.append(mensaje);
			}

			List<String> pathImgs = new ArrayList<String>();
			String imgCab=RUTA_IMAGEN_C+EXT_IMAGEN; 
			String imgDet=RUTA_IMAGEN_D+EXT_IMAGEN;
			if(CommonConstants.TOYOCOSTAS.equals(getCompania().getNoCia())){
				imgCab=RUTA_IMAGEN_C+getCompania().getNoCia()+EXT_IMAGEN; 
				imgDet=RUTA_IMAGEN_D+getCompania().getNoCia()+EXT_IMAGEN;
			}
			pathImgs.add(getServletContext().getRealPath(imgCab));
			pathImgs.add(getServletContext().getRealPath(imgDet));

			mailService.sendEmailPlantillaHTML(getSisMailServidores(getCompania().getNoCia()), from, fromName, to,
					toName, subject, message, null, false, pathImgs, null);

		} catch (GeneralException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public Boolean validarUrlEmpresa(String codCia, String codigo, String empresa) {
		try {
			this.urlCobranzas = null;
			urlEmpresa = paramDetService.findbyNoCiabyCodigobyCodigoDet(codCia, this.codigoParamDet, empresa);
			if (this.urlEmpresa != null && this.urlEmpresa.getEstado1().equals("A")) {
				this.urlCobranzas = urlEmpresa.getTexto1();
				}
			else {
				return false;
				}
			} catch (FindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		return true;
	}
	
	public List<CxcCierreMensualCompania> getListaCierres() {
		return listaCierres;
	}

	public void setListaCierres(List<CxcCierreMensualCompania> listaCierres) {
		this.listaCierres = listaCierres;
	}


	public List<CxcCierreMensualCompania> getListaSeleccionada() {
		return listaSeleccionada;
	}


	public void setListaSeleccionada(
			List<CxcCierreMensualCompania> listaSeleccionada) {
		this.listaSeleccionada = listaSeleccionada;
	}
	
}