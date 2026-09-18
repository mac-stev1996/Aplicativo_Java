/* 
 * CxcDetDevAntCliController.java 
 * 21 may. 2024
 * Copyright 2024 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.dto.Arcjsec;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArcjsecServiceLocal;
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
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * <b> Descripcion de la clase, interface o enumeracion. </b>
 * 
 * @author laura.llangari
 * @version $1.0$
 */
@ManagedBean
@ViewScoped
public class CxcDetDevAntCliController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Logger LOG = Logger.getLogger(CxcDetDevAntCliController.class);
	private String noCia;
	private String agencia;
	private List<Object[]> listaDevSolicitadas;
	private String cedula;
	private String nombres;
	private String estado;
	private String cliente;
	private String origenDialogo;
	private String motDevValidacion;
	private String aplicaCajaChica;
	private String archivoResp;
	private Object[] datosGenerados;
	private List<ParamDet> listaParametros;
	private List<Object[]> listaDetalle;
	private List<AdjSolicitudDev> listaAdjuntos;
	private CxcObsDevAnticipo observacion;
	private List<UsuarioSis> listaUsuarios;
	private List<String> listaAdjuntos1;
	private Map<String, String> archivosAdjuntos;
	private Map<String, List<String>> mapaArchivosCliente;
	private String numeroDocumentoSeleccionado;
	private String usuarioResponsable;
	private Integer secuencial;
	private Boolean mostrarDatos=Boolean.FALSE;
	private static final String GRUPO_CORREO = "GMDAC";

	@EJB(lookup = NombreJNDI.CXC_CAB_DEV_ANTICIPO_SERVICE)
	private CabDevAnticipoClienteServiceLocal devAnticipoService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	@EJB(lookup = NombreJNDI.CXC_DEVOLUCIONES_SOLICITUDES_SERVICE)
	private CxcDevolucionSolAnticiposServiceLocal solicitudesService;
	
	@EJB(lookup = NombreJNDI.ARCJSEC_SERVICE_BEAN)
	private ArcjsecServiceLocal arcjsecService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	/**
	 * 
	 */
	public CxcDetDevAntCliController() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		this.agencia = getUsuarioCentroConectado().getAgencia().getAgenciaPK().getCodigo();
		this.listaParametros = new ArrayList<>();
		this.numeroDocumentoSeleccionado = "";
		this.motDevValidacion = "";
		this.listaAdjuntos1 = new ArrayList<>();
		this.listaUsuarios = new ArrayList<>();
		this.listaDevSolicitadas = new ArrayList<>();
		this.archivosAdjuntos = new HashMap<>();
		this.cliente = "";
		secuencial=0;
		cargarDatosPrincipales();

	}

	public void cargarDatosPrincipales() {

		listaDevSolicitadas = devAnticipoService.cargarDatosSoli(noCia, agencia);
		if (listaDevSolicitadas.isEmpty()) {
			addInfoMessage("Info", "No Existen Solicitudes de Devolución......");
		}

	}

	public void validarInformacion(Object[] itemSeleccionado) {
		numeroDocumentoSeleccionado = "";
		this.origenDialogo = "V";
		this.cedula = itemSeleccionado[1].toString();
		this.nombres = itemSeleccionado[2].toString();
		this.estado = itemSeleccionado[9].toString();
		this.cliente = itemSeleccionado[0].toString();
		this.aplicaCajaChica=itemSeleccionado[13].toString();
		this.datosGenerados = Arrays.copyOf(itemSeleccionado, itemSeleccionado.length);
		listaDetalle = devAnticipoService.detalleSolicitudDevolucion(noCia,
				Long.parseLong(itemSeleccionado[0].toString()), itemSeleccionado[6].toString());
		for (Object[] obj : listaDetalle) {
			this.numeroDocumentoSeleccionado = this.numeroDocumentoSeleccionado.concat(obj[0].toString()).concat("-");
		}
		listaAdjuntos = solicitudesService.getByPk(noCia, itemSeleccionado[6].toString(),
				Long.parseLong(itemSeleccionado[0].toString()), this.estado);

		try {
			this.listaParametros = paramDetService.consultarPorCodigoCab(noCia, "EDSOL");
		} catch (FindException e) {
			LOG.error(e);
		}
		accionesDialog("dlgVerificacion", Boolean.TRUE);

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

	public void actualizarValidado() {
		String mail="";
		String resultado;
		
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
				if (("V".equals(estado))) {
					mail = usuarioSisServiceLocal.mailUsuario(this.usuarioResponsable);
				} else {
					mail = usuarioSisServiceLocal.mailUsuario(observacion.getUsuarioProceso());
				}

				observacion.setUsuarioMail(mail);
				resultado = solicitudesService.actualizarValidacion(observacion, noCia, datosGenerados[6].toString(),
						Long.parseLong(datosGenerados[0].toString()), estado);
				if (resultado.contains("Info")) {
					cargarDatosPrincipales();
					enviarCorreo(datosGenerados);
					addInfoMessage(" ", "Se encuentra validado la solicitud de Anticipo");
					accionesDialog("dlgVerificacion", Boolean.FALSE);

				}

			}

		} catch (NumberFormatException | GeneralException | InsertException | FindException e) {
			addErrorMessage("Error", "Error Al Generar Actualizacion de Solicitud" + e);
			LOG.error(e);
			return;
		}

	}

	public void generarPagoTransferencia(Object[] dato) {

		String url = "";

		this.mostrarDatos = activarSegunSecuencia(getCompania().getNoCia(),
				getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), "M");
		if (!this.mostrarDatos) {
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
					"No se encuentra Parametrizado la secuencia para generar el pago de transferencia. Por favor verificar o cambiar el centro que se encuentra conectado."));
			return;
		} else {

			if (("S").equals(dato[13].toString())) {

				url = "/cajasWebPrime/jsf/procesos/cruceCtasMesAnterior.jsf?S3SnoCia=" + this.noCia + "&cliente="
						+ Long.parseLong(dato[0].toString()) + "&solicitud=" + dato[6].toString() + "&cedula="
						+ dato[1].toString() + "&nombres=" + dato[2].toString() + "&origen=" + 'D';

			} else {
				url = "/cxc-web-prime/jsf/procesos/cxcAutomatizacionDiariosA.jsf?noCia=" + this.noCia + "&cliente="
						+ Long.parseLong(dato[0].toString()) + "&solicitud=" + dato[6].toString() + "&cedula="
						+ dato[1].toString() + "&nombres=" + dato[2].toString() + "&origen=" + 'D';
			}
		}
		try {
			super.redirect(url);
		} catch (IOException e) {
			LOG.error(e);
		}

	}

	public void actualizarDevolucion() {

		if (validarDatos()) {
			int actualizado = devAnticipoService.actualizarDevolucionSolicitud(noCia, datosGenerados[8].toString(),
					this.motDevValidacion);

			if ((actualizado) >= (1)) {
				addInfoMessage(" ", "Se realizo la devolucion de  solicitud de Anticipo");
				cargarDatosPrincipales();
			} else {
				addErrorMessage("", "Error al realizar devolucion de  solicitud de Anticipo");

			}

		}

	}

	public Boolean validarDatos() {

		if (this.motDevValidacion.isEmpty() || Objects.isNull(this.motDevValidacion)) {
			addErrorMessage(" ", "Se debe Ingresar motivo de Devolucion...");
			return false;
		}
		if (observacion!=null && (null==observacion.getUsuarioProceso() || (observacion.getUsuarioProceso().isEmpty())) )
		{
			addErrorMessage(" ", "Se debe Ingresar un Usuario Valido");
			return false;
		}
		
		if (observacion!=null && (null==observacion.getUsuarioMail() || (observacion.getUsuarioMail().isEmpty()))) {
			addErrorMessage(" ", "El usuario no tiene configurado mail.");
			return false;
		}
		
		return true;
	}

	public void validacionCxc() {
		accionesDialog("dlgVerificacion", Boolean.FALSE);
		listaUsuarios = usuarioSisServiceLocal.getUsuarioList();
		accionesDialog("dlgActualizarCxc", Boolean.TRUE);

	}

	public void abriRevisar() {
		accionesDialog("DlgAdjuntos1", Boolean.TRUE);
	}

	public void cargarArchivo(FileUploadEvent event) {
		try {
			UploadedFile uf = event.getFile();
			byte[] buffer = IOUtils.toByteArray(uf.getInputstream());
			InputStream is1 = new ByteArrayInputStream(buffer);
			procesarArchivo(is1, uf);
		} catch (IOException e) {
			LOG.error(e);
			error(this.obtainException(e));
		}
	}

	private void procesarArchivo(InputStream is1, UploadedFile uf) {
		String pathArchivo = null;
		LocalDate fechaActual = LocalDate.now();
		int mes= fechaActual.getMonthValue();
	    int anio = fechaActual.getYear();
		try {
			String extension = "." + FileUpload.getFileExtension(uf.getFileName());
			String fileN = this.cliente.concat("-").concat("ADJ"+secuencial).concat(uf.getFileName().length()>50? uf.getFileName().substring(0, 50): uf.getFileName()).concat(extension);
			String fileName= fileN.length() > 100 ? fileN.substring(0, 100):fileN;
			String pathRaiz = getCompania().getPathFileServer();
			FileUpload fileUpload = new FileUpload();
			pathArchivo = fileUpload.uploadToFileServer(getCompania().getIdTributario(), "COBRANZAS",
					"DEV_ANTICIPOS", Integer.toString(anio).concat(Integer.toString(mes)), pathRaiz, fileName,
					is1);
			if (this.mapaArchivosCliente == null) {
				this.mapaArchivosCliente = new HashMap<>();
			}

			if (this.mapaArchivosCliente.containsKey(this.cliente)) {
				List<String> a = this.mapaArchivosCliente.get(this.cliente);
				a.add(pathArchivo);
				this.mapaArchivosCliente.replace(this.cliente, a);
			} else {
				List<String> a = new ArrayList<>();
				a.add(pathArchivo);
				this.mapaArchivosCliente.put(this.cliente, a);
			}
			this.archivosAdjuntos.put(uf.getFileName(), pathArchivo);
			this.listaAdjuntos1.add(uf.getFileName());
			secuencial=secuencial+1;

		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	
	public void verAdjunto(String nombreArchivo) {
		if (nombreArchivo == null || nombreArchivo.isEmpty()) {
			LOG.error("El nombre del archivo es nulo o vacío");
			return;
		}

		String destPath = archivosAdjuntos.get(nombreArchivo);
		if (destPath == null) {
			LOG.error("No se encontró la ruta para el archivo: " + nombreArchivo);
			return;
		}

		File file = new File(destPath);
		if (!file.exists() || !file.isFile()) {
			LOG.error("El archivo no existe o no es un archivo válido: " + destPath);
			return;
		}

		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();

		try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(file.toURI())))) {
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				String headerValue = String.format("attachment; filename=\"%s\"", nombreArchivo);
				response.setHeader("Content-Disposition", headerValue);
				response.setContentType((nombreArchivo));
				response.setContentLength((int) length);

				try (ServletOutputStream out = response.getOutputStream()) {
					int bytesRead = 0;
					while (bytesRead != (-1)) {
						bytesRead = in.read(buf);
						out.write(buf, 0, bytesRead);
					}
					ctx.responseComplete();
					out.flush();
				}
			}
		} catch (IOException e) {
			LOG.error("Error al leer o escribir el archivo: " + nombreArchivo + e);
		}
	}

	public void quitarArchivo(String nombreArchivo) {

		if (mapaArchivosCliente != null && mapaArchivosCliente.containsKey(datosGenerados[0])) {
			List<String> a = mapaArchivosCliente.get(datosGenerados[0]);
			a.remove(archivosAdjuntos.get(nombreArchivo));
			mapaArchivosCliente.replace(datosGenerados[0].toString(), a);
		}

		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos1.remove(nombreArchivo);
	}

	public void actualizarVerificacionCxc() {

		String resultado;
		try {			
				cargaObservacion();				
				if (validarDatos()) {				
				resultado = solicitudesService.actualizarVerificacionCxc(observacion, noCia,
						datosGenerados[6].toString(), Long.parseLong(datosGenerados[0].toString()), estado,
						archivosAdjuntos);
				if (resultado.contains("Info")) {
					addInfoMessage(" ", "Se encuentra validado la solicitud de Anticipo");
					cargarDatosPrincipales();
					enviarCorreo(datosGenerados);
					accionesDialog("dlgActualizarCxc", Boolean.FALSE);
				} else {
					addErrorMessage("Error", "Error Al Generar Actualizacion de Solicitud");
					return;
				}

			}

		} catch (NumberFormatException | GeneralException | InsertException | FindException e) {
			addErrorMessage("Error", "Error Al Generar Actualizacion de Solicitud" + e);
			LOG.error(e);
			return;
		}

	}
	
	@SuppressWarnings("unused")
	private void enviarCorreo(Object[] itemSeleccionado) {
		
		String nombre= observacion.getUsuarioProceso();
		String informacion="";
		String mensajeInicial="";
		
		if (("V").equals(estado)) {			
			informacion=" SE HA GENERADO LA SOLICITUD DE DEVOLUCI&Oacute;N DE ANTICIPOS PARA VERIFICAR EN CXC ";
			mensajeInicial="VERIFICACIÓN CXC ";
		} else if (("N").equals(estado) || ("I").equals(estado)) {
			informacion="SE HA GENERADO LA SOLICITUD DE ANTICIPOS CON ERRORES ";
			mensajeInicial="DEVOLUCIÓN DE ANTICIPOS CON ERROR";
		}
		else {
			informacion=" SOLICITUD DE DEVOLUCI&Oacute;N DE ANTICIPOS ";	
			mensajeInicial="DEVOLUCIÓN DE ANTICIPOS ";
		}
		try {
			String  emailUsuarioDevolucion= usuarioSisServiceLocal.mailUsuario((String) itemSeleccionado[12]);    
			String  emailUsuarioConectado=getUsuario().getEmail(); 
			StringBuffer mensajeEnvio = new StringBuffer("Estimado(a)")
			.append(nombre).append("  ")
			.append(informacion)
			.append(generarDetalleEnvio(itemSeleccionado));
			
			if (null != emailUsuarioConectado) {				
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()),emailUsuarioConectado ,  emailUsuarioDevolucion, mensajeInicial , mensajeEnvio,  Boolean.FALSE);
			}			
			mailService.sendMailToGroupAttachments(getSisMailServidores(getCompania().getNoCia()), GRUPO_CORREO,
					informacion, mensajeInicial, mensajeEnvio, Boolean.FALSE, null);

		} catch (GeneralException | FindException e) {
			LOG.error(e);
			addErrorMessage("Error", " Ocurrio un error al enviar el correo al grupo" + e.getMessage());
		}

	}

	private StringBuffer generarDetalleEnvio(Object[] itemSeleccionado) {
		StringBuffer cadena = new StringBuffer();
		String informacion="";
		String motivoDev="";
		if (("V").equals(estado)) {			
			informacion=" VERIFICACI&Oacute;N CXC ";
			motivoDev=observacion.getObservacion();
		} else if (("N").equals(estado) || ("I").equals(estado)) {
			informacion="ERRORES EN DEVOLUCI&Oacute;N DE ANTICIPOS ";
			motivoDev=observacion.getObservacion();;
		}
		else {
			informacion=" DEVOLUCI&Oacute;N DE ANTICIPOS ";	
			motivoDev=observacion.getObservacion();;
		}
		cadena.append(
				" <table border='1' width='100%'> <td  style='color: blue;' align=center><b>  informacion  </b> </td>  <table  width='100%'>  <td  style='color: black;' align=center> </td> </table> <table border='1' width='100%'> <tr> <td align=center><b> CI. CLIENTE</b></td> <td align=center><b> CLIENTE</b></td> <td align=center><b>  FECHA DE SOLICITUD  </b> </td>  <td align=center><b> # SOLICITUD</b> </td> <td align=center><b> ASESOR COMERCIAL </b> <td align=center><b> MOTIVO DEVOLUCI&Oacute;N </b> </td> <td align=center> <b> VALOR </b> </td> <tr> <td align=center> "
				+ itemSeleccionado[1] + "</td>  <td align=center> " + itemSeleccionado[2] 
				+ "<td align=center> " + new SimpleDateFormat("dd/MM/yyyy").format(observacion.getFechaProceso()) + "</td> <td td align=center> "
				+ itemSeleccionado[6] + "  </td> <td align=center> " + itemSeleccionado[12] + "  </td> <td align=center> " + motivoDev 
				+ " </td> <td align=right> " + itemSeleccionado[3] + " </td> </tr> </table> "); 
		return cadena;
	}

	

	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author laura.llangari, 12 jul. 2024]
	 * </p>
	 *
	 * @throws FindException
	 */
	private void cargaObservacion() throws FindException {
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
		if (("V".equals(estado))) {
			observacion.setUsuarioProceso(this.usuarioResponsable);
		} else {
			observacion.setUsuarioProceso(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
		}
		String mail = usuarioSisServiceLocal.mailUsuario(observacion.getUsuarioProceso());
		observacion.setUsuarioMail(mail);
	}
	
	private Boolean activarSegunSecuencia(String noCia, String centro, String tipoSec) {

		Arcjsec datos = arcjsecService.obtenerSiguienteSecuencia(noCia, centro, tipoSec);

		if (datos == null || datos.getSecuencia() == null) {
			return false; // botón deshabilitado
		}

		// Si devuelve datos → habilitar botón
		return true;

	}
	
	
		

	// Getter y Setter

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
	 * @return the archivoResp
	 */
	public String getArchivoResp() {
		return archivoResp;
	}

	/**
	 * @param archivoResp the archivoResp to set
	 */
	public void setArchivoResp(String archivoResp) {
		this.archivoResp = archivoResp;
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
	 * @return the listaDetalle
	 */
	public List<Object[]> getListaDetalle() {
		return listaDetalle;
	}

	/**
	 * @param listaDetalle the listaDetalle to set
	 */
	public void setListaDetalle(List<Object[]> listaDetalle) {
		this.listaDetalle = listaDetalle;
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
	 * @return the listaUsuarios
	 */
	public List<UsuarioSis> getListaUsuarios() {
		return listaUsuarios;
	}

	/**
	 * @param listaUsuarios the listaUsuarios to set
	 */
	public void setListaUsuarios(List<UsuarioSis> listaUsuarios) {
		this.listaUsuarios = listaUsuarios;
	}

	/**
	 * @return the usuarioResponsable
	 */
	public String getUsuarioResponsable() {
		return usuarioResponsable;
	}

	/**
	 * @param usuarioResponsable the usuarioResponsable to set
	 */
	public void setUsuarioResponsable(String usuarioResponsable) {
		this.usuarioResponsable = usuarioResponsable;
	}

	public String getAplicaCajaChica() {
		return aplicaCajaChica;
	}

	public void setAplicaCajaChica(String aplicaCajaChica) {
		this.aplicaCajaChica = aplicaCajaChica;
	}
	
	

}
