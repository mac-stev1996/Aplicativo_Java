/* 
 * CxcVerificacionDevSolicitudesController.java 
 * 14 jun. 2024
 * Copyright 2024 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dao.AdjSolicitudDev;
import com.casabaca.cxc.ejb.dao.CxcObsDevAnticipo;
import com.casabaca.cxc.ejb.dao.CxcObsDevAnticipoPK;
import com.casabaca.cxc.ejb.servicio.CabDevAnticipoClienteServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcDevolucionSolAnticiposServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * <b> Descripcion de la clase, interface o enumeracion. </b>
 * 
 * @author laura.llangari
 * @version $1.0$
 */
@ManagedBean
@ViewScoped
public class CxcVerificacionDevSolicitudesController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOG = Logger.getLogger(CxcVerificacionDevSolicitudesController.class.getName());

	/**
	 * 
	 */

	// Variables
	private String noCia;
	private String agencia;
	private List<Object[]> listaDevSolicitadas;
	private Object[] datosGenerados;
	private CxcObsDevAnticipo observacion;
	private String estado;
	private String motDevValidacion;
	private String origenDialogo;
	private String cedula;
	private String nombres;
	private List<ParamDet> listaParametros;
	private List<String> listaAdjuntos1;
	private List<AdjSolicitudDev> listaAdjuntos;
	private Map<String, String> archivosAdjuntos;
	private static final String GRUPO_CORREO = "GMDAC";

	// EJB
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	@EJB(lookup = NombreJNDI.CXC_CAB_DEV_ANTICIPO_SERVICE)
	private CabDevAnticipoClienteServiceLocal devAnticipoService;

	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal sisUsuario;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.CXC_DEVOLUCIONES_SOLICITUDES_SERVICE)
	private CxcDevolucionSolAnticiposServiceLocal solicitudesService;
	
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	public CxcVerificacionDevSolicitudesController() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		this.agencia = getUsuarioCentroConectado().getAgencia().getAgenciaPK().getCodigo();
		this.origenDialogo = "V";
		this.listaParametros = new ArrayList<>();
		this.listaAdjuntos1=new ArrayList<>();
		this.listaAdjuntos=new ArrayList<AdjSolicitudDev>();
		this.archivosAdjuntos= new HashMap<String, String>();
		cargarDatosPrincipales();

	}

	public void cargarDatosPrincipales() {
		try {
			this.listaParametros = paramDetService.consultarPorCodigoCab(noCia, "EDSOL");
			listaDevSolicitadas = devAnticipoService.cargarDatosVerificacionCxC(noCia);
			if (listaDevSolicitadas.isEmpty()) {
				addInfoMessage("Info", "No Existen Solicitudes de Devolución......");
			}
		} catch (FindException e) {
			LOG.error(e);
		}
	}

	public void validarInformacion(Object[] itemSeleccionado) {
		this.datosGenerados = Arrays.copyOf(itemSeleccionado, itemSeleccionado.length);
		this.cedula= datosGenerados[1].toString();
		this.nombres=datosGenerados[2].toString();
		this.estado=datosGenerados[9].toString();
		listaAdjuntos = solicitudesService.getByPk(noCia, itemSeleccionado[6].toString(),
		Long.parseLong(itemSeleccionado[0].toString()), this.estado);
		accionesDialog("dlgVerificacion", Boolean.TRUE);
	}

	public void actualizarValidado() {
		String resultado;
		String mail;
		try {
			if (validarDatos()) {
				observacion = new CxcObsDevAnticipo();
				observacion.setId(new CxcObsDevAnticipoPK());
				observacion.setEstado(estado);
				observacion.getId().setNoCia(noCia);
				observacion.getId().setNoCliente(Long.parseLong(datosGenerados[0].toString()));
				observacion.getId().setNoSolicitud(datosGenerados[6].toString());
				Long secuencia = (long) solicitudesService.generarSecuenciaObservacion(observacion);
				observacion.getId().setSecuencia(secuencia + 1);
				observacion.setObservacion(motDevValidacion);
				observacion.setFechaProceso(new Date());
				observacion.setUsuarioProceso(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				mail = usuarioSisLocal.mailUsuario(observacion.getUsuarioProceso());
				observacion.setUsuarioMail(mail);
				resultado = solicitudesService.actualizarValidacion(observacion, noCia, datosGenerados[6].toString(),
						Long.parseLong(datosGenerados[0].toString()), estado);				
				if (resultado.contains("Info")) {
					addInfoMessage(" ", "Se encuentra validado la solicitud de Anticipo");
					cargarDatosPrincipales();
					accionesDialog("dlgVerificacion", Boolean.FALSE);
					enviarCorreo(datosGenerados);
				}
			}

		} catch (NumberFormatException | GeneralException | InsertException | FindException e) {
			addErrorMessage("Error", "Error Al Generar Actualizacion de Solicitud" + e);
			LOG.error(e);
			return;
		}

	}

	public Boolean validarDatos() {

		if (this.motDevValidacion.isEmpty() && (null) == this.motDevValidacion) {
			addErrorMessage(" ", "Se debe Ingresar motivo de Devolucion...");
			return false;

		}
		return true;
	}
	
	public void abriRevisar() {
		archivosAdjuntos = new HashMap<>();
		listaAdjuntos1 = new ArrayList<>();
		archivosAdjuntos.clear();
		listaAdjuntos1.clear();
		accionesDialog("DlgAdjuntos1", Boolean.TRUE);
	}
	
	@Override
	public void descargarArchivo(String path) {
		try {
			FileUpload fileUpload = new FileUpload();
			fileUpload.seeFile(FacesContext.getCurrentInstance(), path);
		} catch (Exception e) {
			LOG.error("Error en la descarga de archivo", e);
			error("No se pudo descargar el adjunto, intentar nuevamente");
		}
	}
	
	@SuppressWarnings("unused")
	private void enviarCorreo(Object[] itemSeleccionado) {
		
		String desEstado=paramDetService.obtenerDescripcionGrupo(noCia, "EDSOL", observacion.getEstado());

		try {
			String email = getUsuario().getNombre();
			StringBuffer mensajeEnvio = new StringBuffer("SE HA GENERADO LA VERIFICACI&Oacute;N DE SOLICITUD EN CXC: ")
			.append(generarDetalleEnvio(itemSeleccionado))
			.append(" <td align=center> <b>  La Solicitud: </b>   </td>" + itemSeleccionado[6] )
			.append(" <td align=center> <b>  SE ENCUENTRA EN ESTADO : </b>  </td> " +    desEstado.toUpperCase())
			.append(" <td align=center> <b>  Por Favor Verificar  </b>   </td> ");
			mailService.sendMailToGroupAttachments(getSisMailServidores(getCompania().getNoCia()), GRUPO_CORREO,
					"VERIFICACI&Oacute;N DE SOLICITUD EN CXC", "VERIFICACI&Oacute;N DE SOLICITUD EN CXC", mensajeEnvio, Boolean.FALSE, null);
		} catch (GeneralException | FindException e) {
			LOG.error(e);
			addErrorMessage("Error", " Ocurrio un error al enviar el correo al grupo" + e.getMessage());
			return;
		}

	}

	private StringBuffer generarDetalleEnvio(Object[] itemSeleccionado) {
		StringBuffer cadena = new StringBuffer();
		cadena.append(
				" <table border='1' width='100%'> <td  style='color: blue;' align=center><b> VERIFICACI&Oacute;N CXC </b> </td>  <table  width='100%'>  <td  style='color: black;' align=center> </td> </table> <table border='1' width='100%'> <tr> <td align=center><b> CI. CLIENTE</b></td> <td align=center><b> CLIENTE</b></td> <td align=center><b>  FECHA DE SOLICITUD  </b> </td>  <td align=center><b> # SOLICITUD</b> </td> <td align=center><b> ASESOR COMERCIAL </b> <td align=center><b> MOTIVO DEVOLUCI&Oacute;N </b> </td> <td align=center> <b> VALOR </b> </td> <tr> <td align=center>"
						+ itemSeleccionado[1] + "</td>  <td align=center> " + itemSeleccionado[2]
						+ "<td align=center> " + new SimpleDateFormat("dd/MM/yyyy").format(observacion.getFechaProceso())  + "</td> <td td align=center> "
						+ itemSeleccionado[6] + "  </td> <td align=center> " + itemSeleccionado[5] + "  </td> <td align=center> " + observacion.getObservacion()
						+ " </td> <td align=right> " + itemSeleccionado[3] + " </td> </tr> </table> ");
		return cadena;
	}

	

	// GETTER Y SETTER

	/**
	 * @return the lOG
	 */
	public static Logger getLOG() {
		return LOG;
	}

	/**
	 * @param lOG the lOG to set
	 */
	public static void setLOG(Logger lOG) {
		LOG = lOG;
	}

	/**
	 * @return the noCia
	 */
	public String getNoCia() {
		return noCia;
	}

	/**
	 * @param noCia the noCia to set
	 */
	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	/**
	 * @return the agencia
	 */
	public String getAgencia() {
		return agencia;
	}

	/**
	 * @param agencia the agencia to set
	 */
	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	/**
	 * @return the listaDevSolicitadas
	 */
	public List<Object[]> getListaDevSolicitadas() {
		return listaDevSolicitadas;
	}

	/**
	 * @param listaDevSolicitadas the listaDevSolicitadas to set
	 */
	public void setListaDevSolicitadas(List<Object[]> listaDevSolicitadas) {
		this.listaDevSolicitadas = listaDevSolicitadas;
	}

	/**
	 * @return the origenDialogo
	 */
	public String getOrigenDialogo() {
		return origenDialogo;
	}

	/**
	 * @param origenDialogo the origenDialogo to set
	 */
	public void setOrigenDialogo(String origenDialogo) {
		this.origenDialogo = origenDialogo;
	}

	/**
	 * @return the listaParametros
	 */
	public List<ParamDet> getListaParametros() {
		return listaParametros;
	}

	/**
	 * @param listaParametros the listaParametros to set
	 */
	public void setListaParametros(List<ParamDet> listaParametros) {
		this.listaParametros = listaParametros;
	}

	/**
	 * @return the observacion
	 */
	public CxcObsDevAnticipo getObservacion() {
		return observacion;
	}

	/**
	 * @param observacion the observacion to set
	 */
	public void setObservacion(CxcObsDevAnticipo observacion) {
		this.observacion = observacion;
	}

	/**
	 * @return the estado
	 */
	public String getEstado() {
		return estado;
	}

	/**
	 * @param estado the estado to set
	 */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/**
	 * @return the motDevValidacion
	 */
	public String getMotDevValidacion() {
		return motDevValidacion;
	}

	/**
	 * @param motDevValidacion the motDevValidacion to set
	 */
	public void setMotDevValidacion(String motDevValidacion) {
		this.motDevValidacion = motDevValidacion;
	}

	/**
	 * @return the archivosAdjuntos
	 */
	public Map<String, String> getArchivosAdjuntos() {
		return archivosAdjuntos;
	}

	/**
	 * @param archivosAdjuntos the archivosAdjuntos to set
	 */
	public void setArchivosAdjuntos(Map<String, String> archivosAdjuntos) {
		this.archivosAdjuntos = archivosAdjuntos;
	}

	/**
	 * @return the listaAdjuntos1
	 */
	public List<String> getListaAdjuntos1() {
		return listaAdjuntos1;
	}

	/**
	 * @param listaAdjuntos1 the listaAdjuntos1 to set
	 */
	public void setListaAdjuntos1(List<String> listaAdjuntos1) {
		this.listaAdjuntos1 = listaAdjuntos1;
	}

	/**
	 * @return the cedula
	 */
	public String getCedula() {
		return cedula;
	}

	/**
	 * @param cedula the cedula to set
	 */
	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	/**
	 * @return the nombres
	 */
	public String getNombres() {
		return nombres;
	}

	/**
	 * @param nombres the nombres to set
	 */
	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	/**
	 * @return the listaAdjuntos
	 */
	public List<AdjSolicitudDev> getListaAdjuntos() {
		return listaAdjuntos;
	}

	/**
	 * @param listaAdjuntos the listaAdjuntos to set
	 */
	public void setListaAdjuntos(List<AdjSolicitudDev> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}
	
	

}
