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

import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcFacturaCanceladaClienteDto;
import com.casabaca.cxc.ejb.modelo.CxcSegEntregaClientes;
import com.casabaca.cxc.ejb.modelo.CxcSegEntregaClientesPK;
import com.casabaca.cxc.ejb.servicio.CxcSegEntregaClientesServiceLocal;
import com.casabaca.cxc.ejb.servicio.FacVentasCabCpServicioLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;
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
@ManagedBean(name = "cxcSeguimientoEntregaClientesController")
public class CxcSeguimientoEntregaClientesController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabServiceLocal;

	@EJB(lookup = NombreJNDI.FAC_VENTAS_CAB_CP_SERVICIO)
	private FacVentasCabCpServicioLocal facVentasServicio;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@EJB(lookup = NombreJNDI.CXC_SEG_ENTREGA_CLIENTES_SERVICE_BEAN)
	private CxcSegEntregaClientesServiceLocal seguimientoService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuariosServicio;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	private UsuarioSis usuarioSeleccionado;
	private List<UsuarioSis> ListaUsuarios;

	private CxcSegEntregaClientes seguimientoEntrega;
	private List<CxcSegEntregaClientes> historialSeguimiento;

	private LazyDataModelClientes clientes;
	private Cliente clienteSeleccionado;
	private List<Arccmd> cuotasAAfectar;
	private List<CxcFacturaCanceladaClienteDto> facturasDeudas;
	private List<LineaNegocio> lineasDeNegocio;
	private String lineaSeleccionada;
	private List<Arccmd> letrasDeuda;
	private Integer mesesGracia;
	private Map<String, String> archivosAdjuntos;
	private List<String> listaAdjuntos;

	// Constantes para verificar tipo de archivo
	public static final String DOC_FILE = ".doc";
	public static final String PDF_FILE = ".pdf";
	public static final String XLS_FILE = ".xls";
	// Constantes para setear la aplicacion en el header
	public static final String XLS_APPLICATION = "application/vnd.ms-excel";
	public static final String PDF_APPLICATION = "application/pdf";
	public static final String DOC_APPLICATION = "application/msword";

	public Date fechaInicial;
	public Date fechaFinal;
	private String archivoEntrega;

	@PostConstruct
	public void init() {
		facturasDeudas = new ArrayList<>();
		cuotasAAfectar = new ArrayList<>();
		letrasDeuda = new ArrayList<>();
		clienteSeleccionado = new Cliente();
		lineaSeleccionada = null;
		ListaUsuarios = usuariosServicio.getUsuarioList(getCompania().getNoCia());

		clientes = new LazyDataModelClientes(0, 10, getCompania().getNoCia());
		clienteSeleccionado = new Cliente();
		try {
			lineasDeNegocio = lineaNegocioService.buscarLineaNegocioLista(getCompania().getNoCia());
		} catch (FindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void limpiar() {
		lineaSeleccionada = null;
		clienteSeleccionado = new Cliente();
		facturasDeudas = new ArrayList<>();
		cuotasAAfectar = new ArrayList<>();
		letrasDeuda = new ArrayList<>();

	}

	public void buscarDeudasCanceladas() {
		Long noCliente = null;
		if (clienteSeleccionado.getClientePK() != null && clienteSeleccionado.getClientePK().getNoCliente() != null) {
			noCliente = clienteSeleccionado.getClientePK().getNoCliente();
		}
		facturasDeudas = facVentasServicio.getFacturasCanceladasClientes(getCompania().getNoCia(), noCliente,
				lineaSeleccionada, fechaInicial, fechaFinal);

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
			procesarArchivo(is1, uf, false);
		} catch (IOException e) {
			e.printStackTrace();
			error(this.obtainException(e));
		}
	}

	public void cargarArchivoEntrega(FileUploadEvent event) {
		try {
			UploadedFile uf = event.getFile();
			byte[] buffer = IOUtils.toByteArray(uf.getInputstream());
			InputStream is1 = new ByteArrayInputStream(buffer);
			procesarArchivo(is1, uf, true);
		} catch (IOException e) {
			e.printStackTrace();
			error(this.obtainException(e));
		}
	}

	private void procesarArchivo(InputStream is1, UploadedFile uf, boolean entrega) {
		try {
			// Para pruebas en windows
//			String raizDiarios = "C://" + //
//					System.getProperty("file.separator") + "opt" + //
//					System.getProperty("file.separator") + "entregasClientes";

			// Para el servidor Linux
			String raizDiarios =System.getProperty("file.separator") + "opt" + 
			System.getProperty("file.separator") + "entregasClientes";
			String nombreArchivo = uf.getFileName();
			if(nombreArchivo.contains("Ñ")){
				nombreArchivo=nombreArchivo.replaceAll("Ñ","N");
			}
			if(nombreArchivo.contains("ñ")){
				nombreArchivo=nombreArchivo.replaceAll("ñ","n");
			}
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

			File archivo = new File(raizDiarios + nombreArchivo);
			copyInputStreamToFile(is1, archivo);
			if (entrega) {
				if (archivosAdjuntos == null) {
					archivosAdjuntos = new HashMap<>();
				}
				archivosAdjuntos.put(nombreArchivo, raizDiarios + nombreArchivo);
				archivoEntrega = nombreArchivo;
			} else {
				archivosAdjuntos.put(nombreArchivo, raizDiarios + nombreArchivo);
				listaAdjuntos.add(nombreArchivo);
			}
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

	public void realizarSeguimiento(CxcFacturaCanceladaClienteDto factura) {
		historialSeguimiento = seguimientoService.obtenerPorFactura(getCompania().getNoCia(), factura.getTipoDoc(),
				factura.getNoFactura(), String.valueOf(factura.getNoCliente()), factura.getNoFisico());
		if (historialSeguimiento != null && !historialSeguimiento.isEmpty()) {
			accionesDialog("DlgHistorial", true);
		} else {
			seguimientoEntrega = new CxcSegEntregaClientes();
			CxcSegEntregaClientesPK primaria = new CxcSegEntregaClientesPK(getCompania().getNoCia(),
					factura.getTipoDoc(), factura.getNoFactura(), getUsuario().getUsuario(),
					new Timestamp(new Date().getTime()));
			seguimientoEntrega.setEntregaClientesPk(primaria);
			seguimientoEntrega.setNoCliente(factura.getNoCliente());
			seguimientoEntrega.setCedulaCliente(factura.getCedula());
			seguimientoEntrega.setNombreCliente(factura.getNombre());
			seguimientoEntrega.setLineaNegocio(factura.getLineaNegocio());
			seguimientoEntrega.setNoFisico(factura.getNoFisico());
			seguimientoEntrega.setEstadoSeguimiento("ENV");
			seguimientoEntrega.setActivo("S");
			archivosAdjuntos = new HashMap<>();
			listaAdjuntos = new ArrayList<>();
			accionesDialog("DlgSeguimiento", true);

		}
	}

	public void entregarSeguimiento(CxcFacturaCanceladaClienteDto factura) {
		historialSeguimiento = seguimientoService.obtenerPorFactura(getCompania().getNoCia(), factura.getTipoDoc(),
				factura.getNoFactura(), String.valueOf(factura.getNoCliente()), factura.getNoFisico());
		if (historialSeguimiento != null && !historialSeguimiento.isEmpty()) {
			accionesDialog("DlgHistorial", true);
		} else {
			seguimientoEntrega = new CxcSegEntregaClientes();
			CxcSegEntregaClientesPK primaria = new CxcSegEntregaClientesPK(getCompania().getNoCia(),
					factura.getTipoDoc(), factura.getNoFactura(), getUsuario().getUsuario(),
					new Timestamp(new Date().getTime()));
			seguimientoEntrega.setEntregaClientesPk(primaria);
			seguimientoEntrega.setNoCliente(factura.getNoCliente());
			seguimientoEntrega.setCedulaCliente(factura.getCedula());
			seguimientoEntrega.setNombreCliente(factura.getNombre());
			seguimientoEntrega.setLineaNegocio(factura.getLineaNegocio());
			seguimientoEntrega.setNoFisico(factura.getNoFisico());
			seguimientoEntrega.setEstadoSeguimiento("ENC");
			seguimientoEntrega.setActivo("S");
			archivosAdjuntos = new HashMap<>();
			listaAdjuntos = new ArrayList<>();
			accionesDialog("DlgEntrega", true);

		}
	}

	public void guardarEntregar() {
		try {
			if (archivoEntrega == null || archivoEntrega.isEmpty()) {
				warn("Debe adjuntar el archivo para guardar la entrega al cliente");
				return;
			}

			seguimientoEntrega.setUsuarioAsignado("CLIENTE");
			seguimientoEntrega.setAdjuntoEntrega(archivosAdjuntos.get(archivoEntrega));
			seguimientoService.crear(seguimientoEntrega);

			accionesDialog("DlgEntrega", false);
			info("Se ha entregado la documentacion de exitosamente");
		} catch (InsertException e) {
			error("Error al guardar registro " + e);
			e.printStackTrace();
		}
	}

	public void guardarSeguimiento() {
		try {
			String adjuntos = new String();
			if (listaAdjuntos != null && !listaAdjuntos.isEmpty()) {
				for (String nombre : listaAdjuntos) {
					if (adjuntos.isEmpty()) {
						adjuntos = archivosAdjuntos.get(nombre);
					} else {
						adjuntos = adjuntos + ";" + archivosAdjuntos.get(nombre);
					}
				}
			}
			seguimientoEntrega.setAdjuntosAsignacion(adjuntos);

			seguimientoService.crear(seguimientoEntrega);
			accionesDialog("DlgSeguimiento", false);
			info("Se ha enviado los documentos exitosamente");

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

		accionesDialog("DlgAdjuntos", true);
	}

	public void seleccionarCliente() {
		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null) {
			accionesDialog("DlgClientes", false);
		} else {
			warn("Debe seleccionar un cliente");
		}
	}

	public void quitarArchivo(String nombreArchivo) {
		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos.remove(nombreArchivo);
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

	public void verArchivoEntrega(String nombreArchivo) {
		try {
			String destPath = null;
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			destPath = nombreArchivo;

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

	public LazyDataModelClientes getClientes() {
		return clientes;
	}

	public void setClientes(LazyDataModelClientes clientes) {
		this.clientes = clientes;
	}

	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}

	public void setClienteSeleccionado(Cliente clienteSeleccionado) {
		this.clienteSeleccionado = clienteSeleccionado;
	}

	public List<Arccmd> getCuotasAAfectar() {
		return cuotasAAfectar;
	}

	public void setCuotasAAfectar(List<Arccmd> cuotasAAfectar) {
		this.cuotasAAfectar = cuotasAAfectar;
	}

	public List<CxcFacturaCanceladaClienteDto> getFacturasDeudas() {
		return facturasDeudas;
	}

	public void setFacturasDeudas(List<CxcFacturaCanceladaClienteDto> facturasDeudas) {
		this.facturasDeudas = facturasDeudas;
	}

	public List<LineaNegocio> getLineasDeNegocio() {
		return lineasDeNegocio;
	}

	public void setLineasDeNegocio(List<LineaNegocio> lineasDeNegocio) {
		this.lineasDeNegocio = lineasDeNegocio;
	}

	public String getLineaSeleccionada() {
		return lineaSeleccionada;
	}

	public void setLineaSeleccionada(String lineaSeleccionada) {
		this.lineaSeleccionada = lineaSeleccionada;
	}

	public List<Arccmd> getLetrasDeuda() {
		return letrasDeuda;
	}

	public void setLetrasDeuda(List<Arccmd> letrasDeuda) {
		this.letrasDeuda = letrasDeuda;
	}

	public Integer getMesesGracia() {
		return mesesGracia;
	}

	public void setMesesGracia(Integer mesesGracia) {
		this.mesesGracia = mesesGracia;
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

	public List<CxcSegEntregaClientes> getHistorialSeguimiento() {
		return historialSeguimiento;
	}

	public void setHistorialSeguimiento(List<CxcSegEntregaClientes> historialSeguimiento) {
		this.historialSeguimiento = historialSeguimiento;
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

	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	public String getArchivoEntrega() {
		return archivoEntrega;
	}

	public void setArchivoEntrega(String archivoEntrega) {
		this.archivoEntrega = archivoEntrega;
	}

}