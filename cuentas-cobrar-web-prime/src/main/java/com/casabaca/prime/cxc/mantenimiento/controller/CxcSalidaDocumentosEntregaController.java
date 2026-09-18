package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
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
import javax.servlet.http.HttpServletResponse;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcSegEntregaClientes;
import com.casabaca.cxc.ejb.servicio.CxcSegEntregaClientesServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * Controlador para revisar el esta de la documentacion entregada
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcSalidaDocumentosEntregaController")
public class CxcSalidaDocumentosEntregaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.CXC_SEG_ENTREGA_CLIENTES_SERVICE_BEAN)
	private CxcSegEntregaClientesServiceLocal seguimientoService;

	
	private CxcSegEntregaClientes seguimientoEntrega;
	private List<CxcSegEntregaClientes> entregasEnviadas;

	private Map<String, String> archivosAdjuntos;
	private List<String> listaAdjuntos;
	private String archivoEntrega;

	private boolean enviadosUsuario;
	
	private Date fechaInicio;
	
	private Date fechaFin;
	
	private String estado;
	
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
			List<String> estados= Arrays.asList("ENV","REC");
			enviadosUsuario = true;
			entregasEnviadas = seguimientoService.getByUsuarioEnvia(getCompania().getNoCia(), getUsuario().getUsuario(), null, null, estados);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void buscar(){
		List<String> estados= Arrays.asList("ENV","REC");
		if(estado != null){
			estados= Arrays.asList(estado);
		}
		String user = null;
		if(enviadosUsuario){
			user = getUsuario().getUsuario();
		}
		
		entregasEnviadas = seguimientoService.getByUsuarioEnvia(getCompania().getNoCia(), user, fechaInicio, fechaFin, estados);

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

	
	
	public CxcSegEntregaClientes getSeguimientoEntrega() {
		return seguimientoEntrega;
	}

	public void setSeguimientoEntrega(CxcSegEntregaClientes seguimientoEntrega) {
		this.seguimientoEntrega = seguimientoEntrega;
	}

	

	public List<CxcSegEntregaClientes> getEntregasEnviadas() {
		return entregasEnviadas;
	}

	public void setEntregasEnviadas(List<CxcSegEntregaClientes> entregasEnviadas) {
		this.entregasEnviadas = entregasEnviadas;
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

	public boolean isEnviadosUsuario() {
		return enviadosUsuario;
	}

	public void setEnviadosUsuario(boolean enviadosUsuario) {
		this.enviadosUsuario = enviadosUsuario;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	
}