/* 
 * CxcSolicitudDevAnticipoController.java 
 * 3 may. 2024
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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dao.AdjSolicitudDev;
import com.casabaca.cxc.ejb.modelo.CxcCabDevAnticipo;
import com.casabaca.cxc.ejb.modelo.CxcCabDevAnticipoPK;
import com.casabaca.cxc.ejb.servicio.CabDevAnticipoClienteServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcDevolucionSolAnticiposServiceLocal;
import com.casabaca.cxc.ejb.servicio.DetDevAnticipoServiceLocal;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
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
public class CxcSolicitudDevAnticipoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8926344520437685350L;
	private static Logger LOG = Logger.getLogger(CxcSolicitudDevAnticipoController.class.getName());
	private String noCia;
	private List<Cliente> clienteAnticipoList;
	private Long nbCliente;
	private String pCedula;
	private String pNombres;
	private String pMotDev;
	private String usuarioConectado;
	private Cliente clienteSelected;
	private List<Object[]> listaAanticipoCliente;
	private List<Object[]> listaAanticipoClientes;
	private List<Object[]> listaSolicitudesIngresadas;
	private String origenDialogo;
	private InputStream fileVal;
	private String nombreFileVal;
	private String porigen;
	private String estado;
	private CxcCabDevAnticipo cabAnticipo;
	private List<String> listaAdjuntos;
	private Map<String, String> archivosAdjuntos;
	private Map<String, List<String>> mapaArchivosCliente;
	private List<AdjSolicitudDev> verificarAdjuntos;
	private AdjSolicitudDev adjVerif;
	private Integer secuencial;
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

	@EJB(lookup = NombreJNDI.CXC_DET_DEV_ANTICIPO_SERVICE)
	private DetDevAnticipoServiceLocal detDevolucionService;

	/**
	 * 
	 */

	@PostConstruct
	public void init() {
		clienteAnticipoList = new ArrayList<Cliente>();
		this.noCia = getCompania().getNoCia();
		this.usuarioConectado = getUsuario().getUsuario();
		listaAanticipoCliente = new ArrayList<Object[]>();
		this.archivosAdjuntos = new HashMap<>();
		this.listaAdjuntos = new ArrayList<>();
		this.adjVerif = new AdjSolicitudDev();
		this.cabAnticipo = new CxcCabDevAnticipo(new CxcCabDevAnticipoPK());
		pCedula = "";
		pNombres = "";
		nbCliente = Long.valueOf(0);
		listaSolicitudesIngresadas = new ArrayList<Object[]>();
		parametros();
		this.estado = "I";
		this.secuencial = 0;
	}

	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author laura.llangari, 1 jul. 2024]
	 * </p>
	 */
	private void parametros() {
		if (!"".equals(getRequestParameter("cedula")) && getRequestParameter("cedula") != null) {
			this.pCedula = getRequestParameter("cedula");
		}
		if (!"".equals(getRequestParameter("nombres")) && getRequestParameter("nombres") != null) {
			this.pNombres = getRequestParameter("nombres");
		}
		if (!"".equals(getRequestParameter("noCia")) && getRequestParameter("noCia") != null) {
			this.noCia = getRequestParameter("noCia");
		}
		comparacionParametros();
	}

	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author laura.llangari, 1 jul. 2024]
	 * </p>
	 */
	private void comparacionParametros() {
		if (!"".equals(getRequestParameter("codCliente")) && getRequestParameter("codCliente") != null) {
			this.nbCliente = Long.parseLong(getRequestParameter("codCliente"));
		}
		if (!"".equals(getRequestParameter("origen")) && getRequestParameter("origen") != null) {
			this.porigen = getRequestParameter("origen");
			if ("R".equals(this.porigen)) {
				consultarSolicitudesIngresadas();
			}
		}
	}

	@SuppressWarnings("unused")
	public void limpiarDatos() {
		init();

	}

	public void searchClientes() {
		if ((getpCedula() == null || getpCedula().equals("")) && (getpNombres() == null || getpNombres().equals(""))) {
			error("Por favor ingrese cedula o nombre del cliente a buscar.");
			return;
		}
		String noCia = getCompania() != null ? getCompania().getNoCia() : getCompania().getNoCia();
		try {

			List<Cliente> clientesFacturar = clienteService.findByNoCiabyCedulabyNombre(noCia, getpCedula(),
					getpNombres().toUpperCase());
			setClienteAnticipoList(clientesFacturar);

		} catch (FindException e1) {
			error("Ocurrio un error inesperado, favor comuniquese con el Dpto. de Sistemas");
		}
	}

	public void seleccionarClienteFacturar(Cliente clienteFacturar) {
		clienteSelected = clienteFacturar;
		pCedula = clienteSelected.getCedula();
		pNombres = clienteSelected.getNombre();
		nbCliente = clienteSelected.getClientePK().getNoCliente();
	}

	public void consultarAnticipo() {

		try {
			int index = 0;
			listaAanticipoClientes = new ArrayList<Object[]>();
			listaAanticipoCliente = new ArrayList<Object[]>();
			listaAanticipoClientes = arccmdServiceLocal.obtenerAnticiporCliente(noCia, this.nbCliente.toString());

			for (Object object : listaAanticipoClientes) {
				if (!("I").equals(((Object[]) object)[9].toString())) {
					listaAanticipoCliente.add(listaAanticipoClientes.get(index));
				}
				index++;
			}
		} catch (Exception e) {
			LOG.error(e);
		}

	}

	public void modificarAnticiposSeleccionados(Object[] itemSeleccionado) {
		cabAnticipo.setId(new CxcCabDevAnticipoPK());
		cabAnticipo.getId().setNoCliente(Long.parseLong(itemSeleccionado[0].toString()));
		cabAnticipo.getId().setNoCia(this.noCia);
		cabAnticipo.getId().setNoSolicitud(itemSeleccionado[1].toString());
		pNombres = itemSeleccionado[11].toString();
		cabAnticipo.setMotivoDevolucion(itemSeleccionado[6].toString());
		cabAnticipo.setValorTotal((BigDecimal) itemSeleccionado[3]);
		cabAnticipo.setEstado(itemSeleccionado[4].toString());
		cabAnticipo.setUsuarioProceso(getUsuarioConectado());
		cabAnticipo.setAgencia(itemSeleccionado[5].toString());
		cabAnticipo.setAsesor(itemSeleccionado[12].toString());
		cabAnticipo.setFechaProceso(new Date());
		accionesDialog("dlgNuevo", Boolean.TRUE);
	}

	public void agregar(Object[] itemSeleccionado) {

		this.origenDialogo = "I";
		accionesDialog("dlgDevolucion", Boolean.TRUE);
	}

	@SuppressWarnings("unused")
	private void enviarCorreo(Object[] itemSeleccionado) {

		try {
			StringBuffer mensajeEnvio = new StringBuffer("SE HA GENERADO LA SOLICITUD DE DEVOLUCI&Oacute;N DE ANTICIPOS:  ");
			String email = getUsuario().getEmail();
			mensajeEnvio.append(generarDetalleEnvio(itemSeleccionado));	
			if (null != email) {				
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), email, email, "SOLICITUD DE DEVOLUCIÓN ANTICIPOS ", mensajeEnvio,  Boolean.FALSE);
			}			
			mailService.sendMailToGroupAttachments(getSisMailServidores(getCompania().getNoCia()), GRUPO_CORREO,
					"SOLICITUD DE DEVOLUCIÓN ANTICIPOS ", "INGRESO SOLICITUD DEVOLUCIÓN ANTICIPO ", mensajeEnvio, Boolean.FALSE, null);
			
		} catch (GeneralException | FindException e) {
			LOG.error(e);
			addErrorMessage("Error", " Ocurrio un error al enviar el correo al grupo" + e.getMessage());
			return;
		}

	}

	private StringBuffer generarDetalleEnvio(Object[] itemSeleccionado) {
		StringBuffer cadena = new StringBuffer();
		System.out.println(FechaUtils.formatearFecha((Date)itemSeleccionado[7], "DD/MM/YYYY"));
		cadena.append(
				" <table border='1' width='100%'> <td  style='color: blue;' align=center><b> SOLICITUD DEVOLUCI&Oacute;N ANTICIPOS CLIENTE  </b> </td>  <table  width='100%'>  <td  style='color: black;' align=center> </td> </table> <table border='1' width='100%'> <tr> <td align=center><b> CI. CLIENTE</b></td> <td align=center><b> CLIENTE</b></td> <td align=center><b>  FECHA DE SOLICITUD  </b> </td>  <td align=center><b> # SOLICITUD</b> </td> <td align=center><b> ASESOR COMERCIAL </b> <td align=center><b> MOTIVO DEVOLUCI&Oacute;N </b> </td> <td align=center> <b> VALOR </b> </td> <tr> <td align=center>"
						+ itemSeleccionado[10] + "</td>  <td align=center> " + itemSeleccionado[11]
						+ "<td align=center> " +  new SimpleDateFormat("dd/MM/yyyy").format((Date)itemSeleccionado[7])  + "</td> <td td align=center> "
						+ itemSeleccionado[1] + "  </td> <td align=center> " + itemSeleccionado[12] + "  </td> <td align=center> " + itemSeleccionado[6]
						+ " </td> <td align=right> " + itemSeleccionado[3] + " </td> </tr> </table> ");
		return cadena;
	}

	@SuppressWarnings("unused")
	public void cambiarEstadoSolicitud(Object[] itemSeleccionado) {
		this.estado = "E";
		int resultado = devAnticipoService.actualizarEstadoSolicitud(noCia, itemSeleccionado[1].toString(),
				Long.parseLong(itemSeleccionado[0].toString()));
		int actAdjuntos = solicitudesService.actualizarEnvioAdjuntos(noCia, itemSeleccionado[1].toString(),
				Long.parseLong(itemSeleccionado[0].toString()), this.estado,"");
		if (resultado > 0 && actAdjuntos > 0) {
			addInfoMessage("Se envio Solicitud de Devoluci&oacute;n", "");
			consultarSolicitudesIngresadas();
			enviarCorreo(itemSeleccionado);
		} else {
			addErrorMessage("", "Ocurrio un error al momento de enviar Solicitud de Devolucion");
			return;
		}

	}

	public void abrirModalAdjunto() {
		accionesDialog("dlgArchivo", Boolean.TRUE);
	}

	public void consultarSolicitudesIngresadas() {
		try {
			this.listaSolicitudesIngresadas = new ArrayList<Object[]>();
			if ("R".equals(this.porigen)) {
				this.listaSolicitudesIngresadas = devAnticipoService.cargarSolicitudesActivas(this.noCia,
						this.usuarioConectado, this.nbCliente);
			} else {
				this.listaSolicitudesIngresadas = devAnticipoService.cargarSolicitudesActivas(this.noCia,
						this.usuarioConectado, this.nbCliente);

			}
			if (listaSolicitudesIngresadas.isEmpty()) {
				addInfoMessage("", "No Existe Solicitudes Ingresadas para el Cliente Asignado.......");
			}

		} catch (Exception e) {
			LOG.error(e);
		}
	}

	public void generarnuevaSolicitud() {

		if (!this.pCedula.isEmpty() && !this.pNombres.isEmpty()) {
			String url = "/cxc-web-prime/jsf/procesos/cxcBusquedaAnticiposPorCliente.jsf?noCia=" + this.noCia
					+ "&cedula=" + pCedula + "&nombre=" + pNombres + "&codCliente=" + nbCliente + "&origen=" + 'I';
			try {
				super.redirect(url);
			} catch (IOException e) {
				LOG.error(e);
			}

		} else {

			addErrorMessage("Error", "Debe seleccionar el Cliente a Procesar......");

		}
	}

	public void abriRevisar() {
		verificarAdjuntos = solicitudesService.getByPk(cabAnticipo.getId().getNoCia(),
				cabAnticipo.getId().getNoSolicitud(), cabAnticipo.getId().getNoCliente(), cabAnticipo.getEstado());
		accionesDialog("DlgVisualizarAdj", true);
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

	public void deleteArchivo(AdjSolicitudDev adjunto) throws DeleteException {
		Path path = Paths.get(adjunto.getPath());
		try {
			Files.deleteIfExists(path);
			solicitudesService.deletearchivoBase(adjunto);
			abriRevisar();
		} catch (IOException e) {
			LOG.error(e);
			error("No se pudo ELIMINAR el adjunto, intentar nuevamente");
		}

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
		int mes = fechaActual.getMonthValue();
		int anio = fechaActual.getYear();
		try {
			String extension = "." + FileUpload.getFileExtension(uf.getFileName());
			String fileN = this.nbCliente.toString().concat("-").concat("ADJ" + secuencial)
					.concat(uf.getFileName().length() > 50 ? uf.getFileName().substring(0, 50) : uf.getFileName())
					.concat(extension);
			String fileName = fileN.length() > 100 ? fileN.substring(0, 100) : fileN;
			String pathRaiz = getCompania().getPathFileServer();
			FileUpload fileUpload = new FileUpload();
			pathArchivo = fileUpload.uploadToFileServer(getCompania().getIdTributario(), "COBRANZAS", "DEV_ANTICIPOS",
					Integer.toString(anio).concat(Integer.toString(mes)), pathRaiz, fileName, is1);
			if (this.mapaArchivosCliente == null) {
				this.mapaArchivosCliente = new HashMap<>();
			}

			if (this.mapaArchivosCliente.containsKey(this.nbCliente.toString())) {
				List<String> a = this.mapaArchivosCliente.get(this.nbCliente.toString());
				a.add(pathArchivo);
				this.mapaArchivosCliente.replace(this.nbCliente.toString(), a);
			} else {
				List<String> a = new ArrayList<>();
				a.add(pathArchivo);
				this.mapaArchivosCliente.put(this.nbCliente.toString(), a);
			}
			this.archivosAdjuntos.put(uf.getFileName(), pathArchivo);
			this.listaAdjuntos.add(uf.getFileName());
			secuencial = secuencial + 1;

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

	public void verifDatosDevTrasfer() throws UpdateException {
		solicitudesService.actualizarNuevosAdjuntos(adjVerif);

	}

	public void actualizarSolicitud() {
		try {
			if (archivosAdjuntos.size() >= (1)) {
				String resultado = solicitudesService.actualizarSolicitud(cabAnticipo, archivosAdjuntos);
				if (resultado.contains("Info")) {
					addInfoMessage("Info", "Actualizacion se Genero con éxito..");
					accionesDialog("dlgNuevo", Boolean.FALSE);
					archivosAdjuntos.clear();
					listaAdjuntos.clear();
				} else {
					addErrorMessage("Error", resultado);
					return;
				}
			} else {
				addErrorMessage("Error", "Para actualizar debe adjuntar archivos....");
				return;
			}
		} catch (GeneralException | UpdateException | InsertException e) {
			LOG.error(e);
			addErrorMessage("Error", "Revisar actualizacion de datos" + e.getMessage());
			accionesDialog("dlgNuevo", Boolean.FALSE);
			archivosAdjuntos.clear();
			listaAdjuntos.clear();
		}
	}

	public void quitarArchivo(String nombreArchivo) {

		if (mapaArchivosCliente != null
				&& mapaArchivosCliente.containsKey(Long.toString(cabAnticipo.getId().getNoCliente()))) {
			List<String> a = mapaArchivosCliente.get(Long.toString(cabAnticipo.getId().getNoCliente()));
			a.remove(archivosAdjuntos.get(nombreArchivo));
			mapaArchivosCliente.replace(Long.toString(cabAnticipo.getId().getNoCliente()), a);
		}
		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos.remove(nombreArchivo);
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
	 * @return the clienteAnticipoList
	 */
	public List<Cliente> getClienteAnticipoList() {
		return clienteAnticipoList;
	}

	/**
	 * @param clienteAnticipoList the clienteAnticipoList to set
	 */
	public void setClienteAnticipoList(List<Cliente> clienteAnticipoList) {
		this.clienteAnticipoList = clienteAnticipoList;
	}

	/**
	 * @return the nbCliente
	 */
	public Long getNbCliente() {
		return nbCliente;
	}

	/**
	 * @param nbCliente the nbCliente to set
	 */
	public void setNbCliente(Long nbCliente) {
		this.nbCliente = nbCliente;
	}

	/**
	 * @return the pCedula
	 */
	public String getpCedula() {
		return pCedula;
	}

	/**
	 * @param pCedula the pCedula to set
	 */
	public void setpCedula(String pCedula) {
		this.pCedula = pCedula;
	}

	/**
	 * @return the pNombres
	 */
	public String getpNombres() {
		return pNombres;
	}

	/**
	 * @param pNombres the pNombres to set
	 */
	public void setpNombres(String pNombres) {
		this.pNombres = pNombres;
	}

	/**
	 * @return the clienteSelected
	 */
	public Cliente getClienteSelected() {
		return clienteSelected;
	}

	/**
	 * @param clienteSelected the clienteSelected to set
	 */
	public void setClienteSelected(Cliente clienteSelected) {
		this.clienteSelected = clienteSelected;
	}

	/**
	 * @return the listaAanticipoCliente
	 */
	public List<Object[]> getListaAanticipoCliente() {
		return listaAanticipoCliente;
	}

	/**
	 * @param listaAanticipoCliente the listaAanticipoCliente to set
	 */
	public void setListaAanticipoCliente(List<Object[]> listaAanticipoCliente) {
		this.listaAanticipoCliente = listaAanticipoCliente;
	}

	/**
	 * @return the listaAanticipoClientes
	 */
	public List<Object[]> getListaAanticipoClientes() {
		return listaAanticipoClientes;
	}

	/**
	 * @param listaAanticipoClientes the listaAanticipoClientes to set
	 */
	public void setListaAanticipoClientes(List<Object[]> listaAanticipoClientes) {
		this.listaAanticipoClientes = listaAanticipoClientes;
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
	 * @return the pMotDev
	 */
	public String getpMotDev() {
		return pMotDev;
	}

	/**
	 * @param pMotDev the pMotDev to set
	 */
	public void setpMotDev(String pMotDev) {
		this.pMotDev = pMotDev;
	}

	/**
	 * @return the fileVal
	 */
	public InputStream getFileVal() {
		return fileVal;
	}

	/**
	 * @param fileVal the fileVal to set
	 */
	public void setFileVal(InputStream fileVal) {
		this.fileVal = fileVal;
	}

	/**
	 * @return the nombreFileVal
	 */
	public String getNombreFileVal() {
		return nombreFileVal;
	}

	/**
	 * @param nombreFileVal the nombreFileVal to set
	 */
	public void setNombreFileVal(String nombreFileVal) {
		this.nombreFileVal = nombreFileVal;
	}

	/**
	 * @return the listaSolicitudesIngresadas
	 */
	public List<Object[]> getListaSolicitudesIngresadas() {
		return listaSolicitudesIngresadas;
	}

	/**
	 * @param listaSolicitudesIngresadas the listaSolicitudesIngresadas to set
	 */
	public void setListaSolicitudesIngresadas(List<Object[]> listaSolicitudesIngresadas) {
		this.listaSolicitudesIngresadas = listaSolicitudesIngresadas;
	}

	/**
	 * @return the usuarioConectado
	 */
	public String getUsuarioConectado() {
		return usuarioConectado;
	}

	/**
	 * @param usuarioConectado the usuarioConectado to set
	 */
	public void setUsuarioConectado(String usuarioConectado) {
		this.usuarioConectado = usuarioConectado;
	}

	/**
	 * @return the cabAnticipo
	 */
	public CxcCabDevAnticipo getCabAnticipo() {
		return cabAnticipo;
	}

	/**
	 * @param cabAnticipo the cabAnticipo to set
	 */
	public void setCabAnticipo(CxcCabDevAnticipo cabAnticipo) {
		this.cabAnticipo = cabAnticipo;
	}

	/**
	 * @return the verificarAdjuntos
	 */
	public List<AdjSolicitudDev> getVerificarAdjuntos() {
		return verificarAdjuntos;
	}

	/**
	 * @param verificarAdjuntos the verificarAdjuntos to set
	 */
	public void setVerificarAdjuntos(List<AdjSolicitudDev> verificarAdjuntos) {
		this.verificarAdjuntos = verificarAdjuntos;
	}

	/**
	 * s
	 * 
	 * @return the listaAdjuntos
	 */
	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	/**
	 * @param listaAdjuntos the listaAdjuntos to set
	 */
	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

}
