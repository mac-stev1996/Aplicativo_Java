package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcSegEntregaClientes;
import com.casabaca.cxc.ejb.modelo.CxcSegEntregaClientesPK;
import com.casabaca.cxc.ejb.servicio.CxcSegEntregaClientesServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * Controlador para revisar las facturas ya canceladas y se debe entregar la
 * documentacion al cliente
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcBandejaDocumentosEntregaController")
public class CxcBandejaDocumentosEntregaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.CXC_SEG_ENTREGA_CLIENTES_SERVICE_BEAN)
	private CxcSegEntregaClientesServiceLocal seguimientoService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuariosServicio;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	
	private UsuarioSis usuarioSeleccionado;
	private List<UsuarioSis> ListaUsuarios;

	private CxcSegEntregaClientes seguimientoEntrega;
	private CxcSegEntregaClientes seguimientoAnterior;
	private List<CxcSegEntregaClientes> entregasEnBandeja;

	private Map<String, String> archivosAdjuntos;
	private List<String> listaAdjuntos;
	private String archivoEntrega;

	// Constantes para verificar tipo de archivo
	public static final String DOC_FILE = ".doc";
	public static final String PDF_FILE = ".pdf";
	public static final String XLS_FILE = ".xls";
	// Constantes para setear la aplicacion en el header
	public static final String XLS_APPLICATION = "application/vnd.ms-excel";
	public static final String PDF_APPLICATION = "application/pdf";
	public static final String DOC_APPLICATION = "application/msword";

	@PostConstruct
	public void init() {
		try {
			ListaUsuarios = usuariosServicio.getUsuarioList(getCompania().getNoCia());
			entregasEnBandeja = seguimientoService.obtenerPorUsuario(getCompania().getNoCia(),
					getUsuario().getUsuario());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void seleccionarUsuario() {
		if (usuarioSeleccionado != null && usuarioSeleccionado.getUsuario() != null) {
			seguimientoEntrega.setUsuarioAsignado(usuarioSeleccionado.getUsuario());
			accionesDialog("DlgUsuarios", false);
		} else {
			warn("Debe seleccionar un cliente");
		}
	}

	public void cargarArchivo(FileUploadEvent event) {
		try {
			UploadedFile uf = event.getFile();
			byte[] buffer = IOUtils.toByteArray(uf.getInputstream());
			InputStream is1 = new ByteArrayInputStream(buffer);
			procesarArchivo(is1, uf);
		} catch (IOException e) {
			e.printStackTrace();
			error(this.obtainException(e));
		}
	}

	private void procesarArchivo(InputStream is1, UploadedFile uf) {
		try {
			// Para pruebas en windows
//			String raizDiarios = "C://" + //
//					System.getProperty("file.separator") + "opt" + 
//					System.getProperty("file.separator") + "entregasClientes";

			// Para el servidor Linux
			 String raizDiarios = System.getProperty("file.separator") + "opt" + 
			System.getProperty("file.separator") + "entregasClientes";

			File folderRaiz = new File(raizDiarios);
			if (!folderRaiz.exists()) {
				folderRaiz.mkdir();
			}
			File folderGeneral = new File(
					raizDiarios + System.getProperty("file.separator") + getCompania().getNoCia());
			if (!folderGeneral.exists()) {
				folderGeneral.mkdir();
			}
			raizDiarios = raizDiarios + System.getProperty("file.separator") + getCompania().getNoCia()
					+ System.getProperty("file.separator");
			File folder = new File(raizDiarios + seguimientoEntrega.getEntregaClientesPk().getTipoDoc() + "_"
					+ seguimientoEntrega.getEntregaClientesPk().getNoFactu() + "_" + seguimientoEntrega.getNoFisico());
			if (!folder.exists()) {
				folder.mkdir();
			}
			raizDiarios = raizDiarios + seguimientoEntrega.getEntregaClientesPk().getTipoDoc() + "_"
					+ seguimientoEntrega.getEntregaClientesPk().getNoFactu() + "_" + seguimientoEntrega.getNoFisico()
					+ System.getProperty("file.separator");

			File archivo = new File(raizDiarios + uf.getFileName());
			copyInputStreamToFile(is1, archivo);
			if (archivosAdjuntos == null) {
				archivosAdjuntos = new HashMap<>();
			}
			archivosAdjuntos.put(uf.getFileName(), raizDiarios + uf.getFileName());
			archivoEntrega = uf.getFileName();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

		try (FileOutputStream outputStream = new FileOutputStream(file)) {

			int read;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			// commons-io
			IOUtils.copy(inputStream, outputStream);
		}
	}

	public void entregarCliente(CxcSegEntregaClientes seguimientoEnt) {
		if ("REC".equals(seguimientoEnt.getEstadoSeguimiento())) {
			seguimientoAnterior = new CxcSegEntregaClientes(); 
			seguimientoAnterior =seguimientoEnt;
			
			seguimientoEntrega = new CxcSegEntregaClientes();
			
			CxcSegEntregaClientesPK primaria = new CxcSegEntregaClientesPK(getCompania().getNoCia(),
					seguimientoEnt.getEntregaClientesPk().getTipoDoc(),
					seguimientoEnt.getEntregaClientesPk().getNoFactu(), getUsuario().getUsuario(),
					new Timestamp(new Date().getTime()));
			seguimientoEntrega.setEntregaClientesPk(primaria);
			seguimientoEntrega.setActivo("S");
			seguimientoEntrega.setEstadoSeguimiento("ENC");
			seguimientoEntrega.setObservacion(null);
			seguimientoEntrega.setUsuarioAsignado(null);
			seguimientoEntrega.setNoCliente(seguimientoEnt.getNoCliente());
			seguimientoEntrega.setNoFisico(seguimientoEnt.getNoFisico());
			seguimientoEntrega.setCedulaCliente(seguimientoEnt.getCedulaCliente());
			seguimientoEntrega.setNombreCliente(seguimientoEnt.getNombreCliente());
			seguimientoEntrega.setLineaNegocio(seguimientoEnt.getLineaNegocio());
			seguimientoEntrega.setAdjuntosAsignacion(seguimientoEnt.getAdjuntosAsignacion());
			archivoEntrega = null;
			accionesDialog("DlgEntrega", true);
		} else {
			warn("Antes de entregar al cliente, debe recibir la documentacion");
		}

	}

	public void reenviarSeguimiento(CxcSegEntregaClientes seguimientoEnt) {
		if ("REC".equals(seguimientoEnt.getEstadoSeguimiento())) {
			seguimientoAnterior = new CxcSegEntregaClientes(); 
			seguimientoAnterior =seguimientoEnt;
			seguimientoEntrega = new CxcSegEntregaClientes();
			
			CxcSegEntregaClientesPK primaria = new CxcSegEntregaClientesPK(getCompania().getNoCia(),
					seguimientoEnt.getEntregaClientesPk().getTipoDoc(),
					seguimientoEnt.getEntregaClientesPk().getNoFactu(), getUsuario().getUsuario(),
					new Timestamp(new Date().getTime()));
			seguimientoEntrega.setEntregaClientesPk(primaria);
			seguimientoEntrega.setActivo("S");
			seguimientoEntrega.setEstadoSeguimiento("ENV");
			seguimientoEntrega.setObservacion(null);
			seguimientoEntrega.setUsuarioAsignado(null);
			seguimientoEntrega.setNoCliente(seguimientoEnt.getNoCliente());
			seguimientoEntrega.setNoFisico(seguimientoEnt.getNoFisico());
			seguimientoEntrega.setCedulaCliente(seguimientoEnt.getCedulaCliente());
			seguimientoEntrega.setNombreCliente(seguimientoEnt.getNombreCliente());
			seguimientoEntrega.setLineaNegocio(seguimientoEnt.getLineaNegocio());
			seguimientoEntrega.setAdjuntosAsignacion(seguimientoEnt.getAdjuntosAsignacion());
			
			accionesDialog("DlgReenvio", true);
		} else {
			warn("Antes de enviar, debe recibir la documentacion");
		}

	}

	public void recibirSeguimiento(CxcSegEntregaClientes seguimientoEnt) {
		try {

			seguimientoEnt.setActivo("N");
			seguimientoService.modificar(seguimientoEnt);

			
			seguimientoEntrega = new CxcSegEntregaClientes();
			seguimientoEntrega = seguimientoEnt;
			CxcSegEntregaClientesPK primaria = new CxcSegEntregaClientesPK(getCompania().getNoCia(),
					seguimientoEnt.getEntregaClientesPk().getTipoDoc(),
					seguimientoEnt.getEntregaClientesPk().getNoFactu(), getUsuario().getUsuario(),
					new Timestamp(new Date().getTime()));
			seguimientoEntrega.setEntregaClientesPk(primaria);
			seguimientoEntrega.setActivo("S");
			seguimientoEntrega.setEstadoSeguimiento("REC");
			seguimientoEntrega.setObservacion("Recepcion de Documentos");
			seguimientoEntrega.setUsuarioAsignado(getUsuario().getUsuario());

			seguimientoService.crear(seguimientoEntrega);


			entregasEnBandeja = seguimientoService.obtenerPorUsuario(getCompania().getNoCia(),
					getUsuario().getUsuario());

		} catch (Exception e) {
			error("Error al receptar documentos " + e);
		}
	}

	public void guardarReenvio() {
		try {

			seguimientoService.crear(seguimientoEntrega);
			seguimientoAnterior.setActivo("N");
			seguimientoService.modificar(seguimientoAnterior);
			entregasEnBandeja = seguimientoService.obtenerPorUsuario(getCompania().getNoCia(),
					getUsuario().getUsuario());

			accionesDialog("DlgReenvio", false);
			info("Se ha enviado la documentacion exitosamente");
			// Enviar mail al usuario para que realice el ajuste
			// Enviar mail al usuario creador del Diario A
			try {

				UsuarioSis userMail = usuariosServicio.getUsuarioSisByUsername(seguimientoEntrega.getUsuarioAsignado());
				StringBuffer mensaje = new StringBuffer("<p> Estimado/a " + userMail.getNombre()
						+ "</p> <p>Se ha enivado a usted la documentacion para su entrega al cliente: "
						+ seguimientoEntrega.getCedulaCliente() + " - " + seguimientoEntrega.getNombreCliente()
						+ " por parte de: " + getUsuario().getNombre() + ".</p> <p> Con el siguiente observacion: "
						+ seguimientoEntrega.getObservacion()
						+ "</p> Favor su amable ayuda atentiendo este requerimiento.");
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(),
						userMail.getEmail(), "Envio de documentacion entrega del cliente: "
								+ seguimientoEntrega.getCedulaCliente() + " - " + seguimientoEntrega.getNombreCliente(),
						mensaje);
			} catch (FindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (InsertException e) {
			error("Error al guardar registro " + e);
			e.printStackTrace();
		} catch (UpdateException e) {
			error("Error al guardar registro " + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void verAdjuntos(CxcSegEntregaClientes seguimientoEnt) {
		archivosAdjuntos = new HashMap<>();
		listaAdjuntos = new ArrayList<>();
		if (seguimientoEnt.getAdjuntosAsignacion() != null && !seguimientoEnt.getAdjuntosAsignacion().isEmpty()) {
			String[] adjuntos = seguimientoEnt.getAdjuntosAsignacion().toString().split(";");

			for (int i = 0; i < adjuntos.length; i++) {
				String nombreArchivo = adjuntos[i]
						.substring(adjuntos[i].lastIndexOf(System.getProperty("file.separator")));
				archivosAdjuntos.put(nombreArchivo, adjuntos[i]);
				listaAdjuntos.add(nombreArchivo);

			}
			
		}

		accionesDialog("DlgAdjuntos",true);
	}

	public void guardarEntregar() {
		try {
			if (archivoEntrega == null || archivoEntrega.isEmpty()) {
				warn("Debe adjuntar el archivo para guardar la entrega al cliente");
				return;
			}
			seguimientoAnterior.setActivo("N");
			seguimientoService.modificar(seguimientoAnterior);

			seguimientoEntrega.setUsuarioAsignado("CLIENTE");
			seguimientoEntrega.setAdjuntoEntrega(archivosAdjuntos.get(archivoEntrega));
			seguimientoService.crear(seguimientoEntrega);
			
			entregasEnBandeja = seguimientoService.obtenerPorUsuario(getCompania().getNoCia(),
					getUsuario().getUsuario());

			accionesDialog("DlgEntrega", false);
		} catch (InsertException e) {
			error("Error al guardar registro " + e);
			e.printStackTrace();
		} catch (UpdateException e) {
			error("Error al guardar registro " + e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void quitarArchivo(String nombreArchivo) {
		archivosAdjuntos.remove(nombreArchivo);
		archivoEntrega = null;
	}

	public void verAdjunto(String nombreArchivo) {
		try {
			String destPath = null;
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			destPath = archivosAdjuntos.get(nombreArchivo);

			File file = new File(destPath);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				byte[] fileBytes = file.getName().getBytes();

				StringBuffer header = new StringBuffer();
				header.append("filename=\"");
				header.append(nombreArchivo);
				header.append("\"");
				response.setHeader("Content-Disposition", header.toString());
				response.setContentType(getTipoAplicacion(nombreArchivo));
				response.setContentLength(fileBytes.length);
				response.setContentLength((int) length);
				while ((in != null) && ((length = in.read(buf)) != -1)) {
					response.getOutputStream().write(buf, 0, (int) length);
				}
				in.close();
				response.getOutputStream().close();

				ctx.responseComplete();
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getTipoAplicacion(String fileName) {
		if (fileName != null) {
			if (fileName.indexOf(DOC_FILE) > 0) {
				return DOC_APPLICATION;
			}
			if (fileName.indexOf(XLS_FILE) > 0) {
				return XLS_APPLICATION;
			}
			if (fileName.indexOf(PDF_FILE) > 0) {
				return PDF_APPLICATION;
			}
		}
		return null;
	}

	public UsuarioSis getUsuarioSeleccionado() {
		return usuarioSeleccionado;
	}

	public void setUsuarioSeleccionado(UsuarioSis usuarioSeleccionado) {
		this.usuarioSeleccionado = usuarioSeleccionado;
	}

	public List<UsuarioSis> getListaUsuarios() {
		return ListaUsuarios;
	}

	public void setListaUsuarios(List<UsuarioSis> listaUsuarios) {
		ListaUsuarios = listaUsuarios;
	}

	public CxcSegEntregaClientes getSeguimientoEntrega() {
		return seguimientoEntrega;
	}

	public void setSeguimientoEntrega(CxcSegEntregaClientes seguimientoEntrega) {
		this.seguimientoEntrega = seguimientoEntrega;
	}

	public List<CxcSegEntregaClientes> getEntregasEnBandeja() {
		return entregasEnBandeja;
	}

	public void setEntregasEnBandeja(List<CxcSegEntregaClientes> entregasEnBandeja) {
		this.entregasEnBandeja = entregasEnBandeja;
	}

	public Map<String, String> getArchivosAdjuntos() {
		return archivosAdjuntos;
	}

	public void setArchivosAdjuntos(Map<String, String> archivosAdjuntos) {
		this.archivosAdjuntos = archivosAdjuntos;
	}

	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	public String getArchivoEntrega() {
		return archivoEntrega;
	}

	public void setArchivoEntrega(String archivoEntrega) {
		this.archivoEntrega = archivoEntrega;
	}

}