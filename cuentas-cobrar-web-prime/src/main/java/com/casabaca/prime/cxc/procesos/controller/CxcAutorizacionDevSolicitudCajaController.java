/* 
 * CxcAutorizacionDevSolicitudCaja.java 
 * 14 nov. 2024
 * Copyright 2024 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.casabaca.common.ejb.dto.DiarioAClienteDTO;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dao.AdjSolicitudDev;
import com.casabaca.cxc.ejb.dao.CxcObsDevAnticipo;
import com.casabaca.cxc.ejb.dto.CxcCabDevAnticipoDto;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiarios;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiariosPK;
import com.casabaca.cxc.ejb.servicio.CabDevAnticipoClienteServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcDevolucionSolAnticiposServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
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
public class CxcAutorizacionDevSolicitudCajaController extends CommonController implements Serializable {

	/**
	 * 
	 */

	private static final long serialVersionUID = 15348975328782565L;
	private static Logger LOG = Logger.getLogger(CxcAutorizacionDevSolicitudCajaController.class.getName());
	/**
	 * 
	 */
	private String noCia;
	private List<CxcCabDevAnticipoDto> listarAnticipo;
	private CxcObsDevAnticipo observacion;
	private static final String GRUPO_CORREO = "GMDAC";
	private List<AdjSolicitudDev>   adjSolicitud;
	private Map<String, String> archivosAdjuntos;
	private List<String> listaAdjuntos;

	// EJB
	@EJB(lookup = NombreJNDI.CXC_CAB_DEV_ANTICIPO_SERVICE)
	private CabDevAnticipoClienteServiceLocal cabAnticipoService;

	@EJB(lookup = NombreJNDI.CXC_DEVOLUCIONES_SOLICITUDES_SERVICE)
	private CxcDevolucionSolAnticiposServiceLocal solicitudesService;

	@EJB(lookup = NombreJNDI.CXC_CAB_DEV_ANTICIPO_SERVICE)
	private CabDevAnticipoClienteServiceLocal devAnticipoService;
	
	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal sisUsuario;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	public CxcAutorizacionDevSolicitudCajaController() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		this.listarAnticipo = new ArrayList<>();
		listarSolicitudesAprobar();

	}

	public void listarSolicitudesAprobar() {

		listarAnticipo = cabAnticipoService.listarSolicitudesAprobacion(noCia);

	}

	public void actualizarAprobacion(CxcCabDevAnticipoDto itemSeleccionado, String opcion) {
		String estado = " ";
		if (("A").equals(opcion)) {
			estado="E";
		} else {
			estado="N";
		}		
		int resultado = devAnticipoService.actualizarEstadoSolicitudAprobacion(noCia, itemSeleccionado.getNoSolicitud(),
				itemSeleccionado.getNoCliente(), estado, getUsuario().getUsuario());
		int actAdjuntos = solicitudesService.actualizarEnvioAdjuntos(noCia, itemSeleccionado.getNoSolicitud(),
				itemSeleccionado.getNoCliente(), estado,"C");
		if (resultado > 0 && actAdjuntos > 0) {
			addInfoMessage(" Se Actualizo los Datos con exito.. ", "");
			listarSolicitudesAprobar();
			enviarCorreo(itemSeleccionado,opcion);
		} else {
			addErrorMessage("", "Ocurrio un error al momento de enviar Solicitud de Devolucion");
			return;
		}

	}

	@SuppressWarnings("unused")
	private void enviarCorreo(CxcCabDevAnticipoDto itemSeleccionado,String opcion) {
		String mensaje="";
		String subject="";
		try {
			if (("A").equals(opcion)) {
				mensaje="SE HA GENERADO APROBACI&Oacute;N DE DEVOLUCI&Oacute;N DE ANTICIPOS CON AUTORIZACI&Oacute;N POR CAJA CHICA: ";
				subject="APROBACIÓN DE DEVOLUCIÓN DE ANTICIPOS CON AUTORIZACIÓN POR CAJA CHICA ";
			} else {
				mensaje="SE NEGO LA DEVOLUCI&Oacute;N DE ANTICIPOS CON AUTORIZACI&Oacute;N POR CAJA CHICA : ";
				subject="NO SE APROBO LA DEVOLUCIÓN DE ANTICIPOS CON AUTORIZACIÓN POR CAJA CHICA ";
			}
			
			StringBuffer mensajeEnvio = new StringBuffer(mensaje);
			String email = getUsuario().getEmail();
			mensajeEnvio.append(generarDetalleEnvio(itemSeleccionado,opcion));	
			if (null != email) {				
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), email, email, "SOLICITUD DE DEVOLUCIÓN ANTICIPOS ", mensajeEnvio,  Boolean.FALSE);
			}			
			mailService.sendMailToGroupAttachments(getSisMailServidores(getCompania().getNoCia()), GRUPO_CORREO,
					subject, subject, mensajeEnvio, Boolean.FALSE, null);
			
		} catch (GeneralException | FindException e) {
			LOG.error(e);
			addErrorMessage("Error", " Ocurrio un error al enviar el correo al grupo" + e.getMessage());
			return;
		}

	}

	private String generarDetalleEnvio(CxcCabDevAnticipoDto itemSeleccionado,String opcion) {
		String  cadena = "";
		String mensaje="";
		if (("A").equals(opcion)) {
			mensaje="SE HA GENERADO APROBACI&Oacute;N DE DEVOLUCI&Oacute;N DE ANTICIPOS CON AUTORIZACI&Oacute;N POR CAJA CHICA : ";
		} else {
			mensaje="SE NEGO LA DEVOLUCI&Oacute;N DE ANTICIPOS CON AUTORIZACI&Oacute;N POR CAJA CHICA: ";
		}	
		cadena= (" <table border='1' width='100%'> <td  style='color: blue;' align=center><b>"  + " " + mensaje + " " + "</b> </td>  <table  width='100%'>  <td  style='color: black;' align=center> </td> </table> <table border='1' width='100%'> <tr> <td align=center><b> CI. CLIENTE</b></td> <td align=center><b> CLIENTE</b></td> <td align=center><b>  FECHA DE SOLICITUD  </b> </td>  <td align=center><b> # SOLICITUD</b> </td> <td align=center><b> ASESOR COMERCIAL </b> <td align=center><b> MOTIVO DEVOLUCI&Oacute;N </b> </td> <td align=center> <b> VALOR </b> </td> <tr> <td align=center>"
						+ itemSeleccionado.getCedulaCliente() + "</td>  <td align=center> " + itemSeleccionado.getNombreCliente()
						+ "<td align=center> " + new SimpleDateFormat("dd/MM/yyyy").format(itemSeleccionado.getFechaProceso())  + "</td> <td td align=center> "
						+ itemSeleccionado.getNoSolicitud() + "  </td> <td align=center> " + itemSeleccionado.getAsesor() + "  </td> <td align=center> " + itemSeleccionado.getMotivoDevolucion()
						+ " </td> <td align=right> " + itemSeleccionado.getValorTotal()+ " </td> </tr> </table> ");
		return cadena;
	}
	
	public void abrirRevisar(CxcCabDevAnticipoDto anticipo, boolean abrirAdjunto) {
		try {

			archivosAdjuntos = new HashMap<>();
			listaAdjuntos = new ArrayList<>();
			adjSolicitud = solicitudesService.visualizarAdjuntosCajaChica(getCompania().getNoCia(), anticipo.getNoSolicitud(),
					anticipo.getNoCliente());

			if (!adjSolicitud.isEmpty()) {

				for (AdjSolicitudDev adj : adjSolicitud) {
					String nombreArchivo = adj.getPath()
							.substring(adj.getPath().lastIndexOf(System.getProperty("file.separator")));
					archivosAdjuntos.put(nombreArchivo, adj.getPath());
					listaAdjuntos.add(nombreArchivo);
				}
			}

			if (abrirAdjunto) {
				accionesDialog("DlgAdjuntos", true);
			} else {
				accionesDialog("DlgRevision", true);
			}

		} catch (Exception e) {
			LOG.error(e);
		}

	}
	
	public void verAdjunto(String nombreArchivo) {
		String destPath = null;
		try {
			destPath = archivosAdjuntos.get(nombreArchivo);
			FileUpload fileUpload = new FileUpload();
			fileUpload.seeFile(FacesContext.getCurrentInstance(), destPath);
		} catch (Exception e) {
			e.printStackTrace();
			error("No se pudo descargar el archivo de la ruta "+destPath);
		}
	}
		
	
	/**
	 * @return the listarAnticipo
	 */
	public List<CxcCabDevAnticipoDto> getListarAnticipo() {
		return listarAnticipo;
	}

	/**
	 * @param listarAnticipo the listarAnticipo to set
	 */
	public void setListarAnticipo(List<CxcCabDevAnticipoDto> listarAnticipo) {
		this.listarAnticipo = listarAnticipo;
	}

	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	
}
