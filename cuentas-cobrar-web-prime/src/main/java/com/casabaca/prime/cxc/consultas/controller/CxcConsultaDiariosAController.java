package com.casabaca.prime.cxc.consultas.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.ExceptionUtils;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.dto.DiarioAClienteDTO;
import com.casabaca.common.ejb.model.Banco;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ConfirmarDeposito;
import com.casabaca.common.ejb.service.ArccdaServiceLocal;
import com.casabaca.common.ejb.service.BancoServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.ConfirmarDepositoServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dao.AdjSolicitudDev;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiarios;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiariosPK;
import com.casabaca.cxc.ejb.servicio.CxcDevolucionSolAnticiposServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcRevisionDiariosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * Controlador para consultar los diarios A
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcConsultaDiariosAController")
public class CxcConsultaDiariosAController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.ARCCDA_SERVICE)
	private ArccdaServiceLocal arccdaService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_REVISION_DIARIOS_SERVICE_BEAN)
	private CxcRevisionDiariosServiceLocal revisionServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuariosServicio;
	
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	
	@EJB(lookup = NombreJNDI.CONFIRMAR_DEPOSITO_SERVICE)
	private ConfirmarDepositoServiceLocal confirmarDepositoService;
	
	@EJB(lookup = NombreJNDI.BANCO_SERVICE)
	private BancoServiceLocal bancoService;
	
	@EJB(lookup = NombreJNDI.CXC_DEVOLUCIONES_SOLICITUDES_SERVICE)
	private CxcDevolucionSolAnticiposServiceLocal solicitudesService;
	
	
	// Constantes para verificar tipo de archivo
	public static final String DOC_FILE = ".doc";
	public static final String PDF_FILE = ".pdf";
	public static final String XLS_FILE = ".xls";
	// Constantes para setear la aplicacion en el header
	public static final String XLS_APPLICATION = "application/vnd.ms-excel";
	public static final String PDF_APPLICATION = "application/pdf";
	public static final String DOC_APPLICATION = "application/msword";

	private LazyDataModelClientes clientes;
	private Cliente clienteSeleccionado;
	private List<DiarioAClienteDTO> diariosA;
	private String noFisicoInicial;
	private String noFisicoFinal;
	private Date fechaInicial;
	private Date fechaFinal;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private CxcRevisionDiarios revision;
	private List<AdjSolicitudDev>   adjSolicitud;
	private Map<String, String> archivosAdjuntos;
	private List<String> listaAdjuntos;
	private boolean aprobadoRevision;
	private boolean devolver;
	private boolean ajustar;
	private DiarioAClienteDTO diarioSeleccionado;
	private UploadedFile file;
	private UsuarioSis usuarioSeleccionado;
	private List<UsuarioSis> usuariosContabilidad;
	private boolean esRevision;
	private List<ConfirmarDeposito> depositosConfirmados;
	
	@PostConstruct
	public void init() {
		esRevision = "S".equals(getRequestParameter("revisar"));
		diariosA = new ArrayList<>();
		clientes = new LazyDataModelClientes(0, 10, getCompania().getNoCia());
		clienteSeleccionado = new Cliente();
		revision = new CxcRevisionDiarios();
		adjSolicitud= new ArrayList<>();
		usuariosContabilidad = usuariosServicio.obtenerPorDepartamento("CON");
	}

	
	protected String getRequestParameter(String name) {
		return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
	}
	
	public void limpiar() {
		noFisicoInicial = null;
		noFisicoFinal = null;
		fechaInicial = null;
		fechaFinal = null;
		diariosA = new ArrayList<>();
		clienteSeleccionado = new Cliente();
	}

	public void abrirRevisar(DiarioAClienteDTO diario, boolean abrirAdjunto) {
		diarioSeleccionado = diario;
		aprobadoRevision = false;
		devolver = false;
		ajustar = false;
		try {

			archivosAdjuntos = new HashMap<>();
			listaAdjuntos = new ArrayList<>();
			revision = revisionServiceLocal.obtenerPorPk(getCompania().getNoCia(),
					diario.getNoDocu());			
			adjSolicitud=solicitudesService.visualizarTodosAdj(getCompania().getNoCia(), diario.getNoDocu(), diario.getNoCliente());
			
			if (revision != null ) {
				aprobadoRevision = "A".equals(revision.getRevAprobada());
				devolver = "S".equals(revision.getDevolver());
				ajustar = "S".equals(revision.getAjuste());
				if (revision.getAdjuntos() != null && !revision.getAdjuntos().isEmpty()) {
					String[] adjuntos = revision.getAdjuntos().toString().split(";");
					for (int i = 0; i < adjuntos.length; i++) {
						String nombreArchivo = adjuntos[i]
								.substring(adjuntos[i].lastIndexOf(System.getProperty("file.separator")));
						archivosAdjuntos.put(nombreArchivo, adjuntos[i]);
						listaAdjuntos.add(nombreArchivo);

					}
				}else if (!adjSolicitud.isEmpty()) {
					
					for (AdjSolicitudDev adj: adjSolicitud ) {	
							String nombreArchivo = adj.getPath()
									.substring(adj.getPath().lastIndexOf(System.getProperty("file.separator")));
							archivosAdjuntos.put(nombreArchivo, adj.getPath());
							listaAdjuntos.add(nombreArchivo);
						}						
					}
					
				
				if(abrirAdjunto){
					accionesDialog("DlgAdjuntos", true);
				}else{
					accionesDialog("DlgRevision", true);
				}
			
	      } else {
				revision = new CxcRevisionDiarios();
				CxcRevisionDiariosPK primaria = new CxcRevisionDiariosPK(getCompania().getNoCia(),
						diario.getNoDocu());
				revision.setRevisionDiarioPk(primaria);
				if(abrirAdjunto){
					accionesDialog("DlgAdjuntos", true);
				}else{
					accionesDialog("DlgRevision", true);
				}archivosAdjuntos = new HashMap<>();

			
	      }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	
	public void guardarRevision(boolean abrirAdjunto) throws GeneralException {
		revision.setRevAprobada("R");
		if (aprobadoRevision) {
			revision.setRevAprobada("A");
		}
		if(devolver){
			revision.setDevolver("S");
		}
		if(ajustar){
			revision.setAjuste("S");
			if(revision.getUsuarioAjuste() == null ){
				warn("Seleccione un usuario para realizar el ajuste");
				return;
			}
		}
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
		try {
			SisMailServidores sisMailServidores = getSisMailServidores(getCompania().getNoCia());
			revision.setAdjuntos(adjuntos);

			if (revision.getUsuario() == null) {
				revision.setUsuario(getUsuario().getUsuario());
				revision.setFecha(new Date());
				revisionServiceLocal.crear(revision);

			} else {
				revision.setUsuario(getUsuario().getUsuario());
				revision.setFecha(new Date());
				revisionServiceLocal.modificar(revision);

			}

			if(devolver && !abrirAdjunto){
				//Enviar mail al usuario creador del Diario A
				try {
					
					UsuarioSis userDiario = usuariosServicio.getUsuarioSisByUsername(diarioSeleccionado.getUsuario());
					StringBuffer mensaje = new StringBuffer("<p> Estimado/a "+userDiario.getNombre() +"</p><p>Se ha realizado la devolucion del diario A No: "+diarioSeleccionado.getNumero()+" de la Empresa: "+getCompania().getNombre()+"</p> <p> Con el siguiente observacion: "+revision.getObservacion()+"</p> Favor su amable ayuda atentiendo este requerimiento.");
				
					mailService.sendEmailInHtmlNoCia(sisMailServidores, userDiario.getEmail(), getUsuario().getEmail(),
							"Devolucion de diario A No: "+diarioSeleccionado.getNumero()+" de "+getCompania().getNombre(), mensaje);
				} catch (FindException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GeneralException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(ajustar && !abrirAdjunto){
				//Enviar mail al usuario para que realice el ajuste
				//Enviar mail al usuario creador del Diario A
				try {
					
					UsuarioSis userDiario = usuariosServicio.getUsuarioSisByUsername(revision.getUsuarioAjuste());
					StringBuffer mensaje = new StringBuffer("<p> Estimado/a "+userDiario.getNombre() +"</p> <p>Se ha solicitado el ajuste contable del diario A No: "+diarioSeleccionado.getNumero()+" de la Empresa: "+getCompania().getNombre()+"</p> <p> Con el siguiente observacion: "+revision.getObservacion()+"</p> Favor su amable ayuda atentiendo este requerimiento.");
					mailService.sendEmailInHtmlNoCia(sisMailServidores, userDiario.getEmail(), getUsuario().getEmail(),
							"Ajuste contable del diario A No: "+diarioSeleccionado.getNumero()+" de "+getCompania().getNombre(), mensaje);
				} catch (FindException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (GeneralException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(abrirAdjunto){
				accionesDialog("DlgAdjuntos", false);
			}else{
				accionesDialog("DlgRevision", false);
			}
			info("Se ha guardado la revision exitosamente ");
		} catch (InsertException | UpdateException e) {
			e.printStackTrace();
			error("Error al guardar la revision");
		}

	}

	public void buscarDiariosCriterios() {
		String noCliente = "";
		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null && clienteSeleccionado.getClientePK().getNoCliente() != null) {
			noCliente = String.valueOf(clienteSeleccionado.getClientePK().getNoCliente());
		}

		diariosA = arccdaService.consultarDiariosAParametros(getCompania().getNoCia(), fechaInicial, fechaFinal,
				noCliente, noFisicoInicial, noFisicoFinal);
		
	}

	public void quitarArchivo(String nombreArchivo) {
		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos.remove(nombreArchivo);
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

	public void imprimirDiario(DiarioAClienteDTO diarioA) throws IOException, SQLException {
		if (diarioA.getRutaArchivo() == null || diarioA.getRutaArchivo().isEmpty()) {
			generarReporteJasper(diarioA);
			return;
		}

		try {
			String destPath = null;
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			destPath = diarioA.getRutaArchivo();

			File file = new File(destPath);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				byte[] fileBytes = file.getName().getBytes();

				StringBuffer header = new StringBuffer();
				header.append("filename=\"");
				header.append("DIARIO_A" + diarioA.getNumero() + "-" + diarioA.getNoDocu() + ".pdf");
				header.append("\"");
				response.setHeader("Content-Disposition", header.toString());
				response.setContentType("application/pdf");
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

	private void generarReporteJasper(DiarioAClienteDTO diarioA) throws IOException, SQLException {
		String reportPath = "/reportes/cxcComprobanteDiariosA.jasper";
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		JasperPrint jasperPrint = null;
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParametersDiariosA(parameters, getCompania().getNoCia(), diarioA.getNoDocu(),
					diarioA.getUsuario());
			connection = utilServiceDelegate.getDataSource().getConnection();
			String ctxPath = getServletContext().getRealPath("/");			
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
						
			
			jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);

			StringBuffer header = new StringBuffer();
			header.append("filename=\"");
			header.append("DIARIO_A" + diarioA.getNumero() + "-" + diarioA.getNoDocu() + ".pdf");
			header.append("\"");			
			response.addHeader("Content-Disposition", header.toString());
			response.setContentType("application/pdf");
			
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally {
			response.getOutputStream().close();
			connection.close();
		}

	}

	private void setParametersDiariosA(Map<String, Object> parameters, String noCia, String noDocu, String usuario) {
		parameters.put("P_NO_CIA", noCia);
		parameters.put("P_NO_DOCU", noDocu);
		parameters.put("USUARIO", usuario);
		parameters.put("EMPRESA", getCompania().getNombre());
		parameters.put("SUBREPORT_DIR", getPathReal());
		
		CxcRevisionDiarios diarioRev = revisionServiceLocal.obtenerPorPk(getCompania().getNoCia(),
				noDocu);
		if (diarioRev != null) {
			parameters.put("USUARIO_REVISOR", diarioRev.getUsuario());
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		FacesMessage msg = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
		FacesContext.getCurrentInstance().addMessage(null, msg);
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

	public void procesar() {
		try {
			InputStream is1;

			is1 = file.getInputstream();
			procesarArchivo(is1, file);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void procesarArchivo(InputStream is1, UploadedFile uf) {
		try {
			// Para pruebas en windows
//			String raizDiarios = "C://" + //
//					System.getProperty("file.separator") + "opt" + //
//					System.getProperty("file.separator") + "diariosA" + //
//					System.getProperty("file.separator");

			// Para el servidor Linux
			 String raizDiarios = System.getProperty("file.separator") + "opt"
			 + System.getProperty("file.separator")
			 + "diariosA" + System.getProperty("file.separator");

			File folderGeneral = new File(
					raizDiarios + getCompania().getNoCia() + System.getProperty("file.separator") + "Adjuntos");
			if (!folderGeneral.exists()) {
				folderGeneral.mkdir();
			}
			raizDiarios = raizDiarios + getCompania().getNoCia() + System.getProperty("file.separator") + "Adjuntos"
					+ System.getProperty("file.separator");
			File folder = new File(raizDiarios + diarioSeleccionado.getNumero());
			if (!folder.exists()) {
				folder.mkdir();
			}
			raizDiarios = raizDiarios + diarioSeleccionado.getNumero() + System.getProperty("file.separator");

			File archivo = new File(raizDiarios + uf.getFileName());
			copyInputStreamToFile(is1, archivo);

			archivosAdjuntos.put(uf.getFileName(), raizDiarios + uf.getFileName());
			listaAdjuntos.add(uf.getFileName());
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

	public void seleccionarUsuario() {
		if(usuarioSeleccionado != null){
			revision.setUsuarioAjuste(usuarioSeleccionado.getUsuario());
		accionesDialog("DlgUsuarios", false);
		}else{
			warn("Debe seleccionar un cliente");
		}
	}

	
	public void seleccionarCliente() {
		if(clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null){
		accionesDialog("DlgClientes", false);
		}else{
			warn("Debe seleccionar un cliente");
		}
	}
	
	/**
	 * Permite cargar los datos de los depositos confirmados
	 * @param diarioA
	 */
	public void cargarDepositos(DiarioAClienteDTO diarioA) {
		depositosConfirmados = confirmarDepositoService.consultarDepositosDiariosA(getCompania().getNoCia(), diarioA);
		if (depositosConfirmados.isEmpty()) {
			error("No existen depositos con el ingreso seleccionado");
		} else {
			List<Banco> bancos = consultarBancos(depositosConfirmados.stream().map(p -> p.getId().getBanco()).distinct()
					.collect(Collectors.toList()));
			depositosConfirmados.forEach(d -> {
				Optional<Banco> bancoTemp = bancos.stream().filter(b -> d.getId().getBanco().equals(b.getBanco()))
						.findFirst();
				d.setNombreBanco(bancoTemp.isPresent() ? bancoTemp.get().getDescrip() : "***");
			});
			accionesDialog("dlgDepositos", Boolean.TRUE);
		}
	}
	
	/**
	 * Permite descargar el archivo
	 * 
	 */
	public void descargarArchivo(String path) {
		try {
			FileUpload fileUpload = new FileUpload();
			fileUpload.seeFile(FacesContext.getCurrentInstance(), path);
		} catch (IOException e) {
			error("Error al descargar el archivo adjunto "+ExceptionUtils.obtainException(e));
			e.printStackTrace();
		}
	}
	
	/**
	 * Permite consultar el nombre del banco
	 * 
	 * @param codBanco
	 * @return
	 */
	private List<Banco> consultarBancos(List<String> codBancos) {
		List<Banco> bancos = new ArrayList<>();
		try {
			for (String codBanco : codBancos) {
				bancos.add(bancoService.getBancoDescripcion(codBanco));
			}
		} catch (FindException e) {
			e.printStackTrace();
		}
		return bancos;
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

	public List<DiarioAClienteDTO> getDiariosA() {
		return diariosA;
	}

	public void setDiariosA(List<DiarioAClienteDTO> diariosA) {
		this.diariosA = diariosA;
	}

	public String getNoFisicoInicial() {
		return noFisicoInicial;
	}

	public void setNoFisicoInicial(String noFisicoInicial) {
		this.noFisicoInicial = noFisicoInicial;
	}

	public String getNoFisicoFinal() {
		return noFisicoFinal;
	}

	public void setNoFisicoFinal(String noFisicoFinal) {
		this.noFisicoFinal = noFisicoFinal;
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

	public String getCedulaClienteConsulta() {
		return cedulaClienteConsulta;
	}

	public void setCedulaClienteConsulta(String cedulaClienteConsulta) {
		this.cedulaClienteConsulta = cedulaClienteConsulta;
	}

	public String getNombreClienteConsulta() {
		return nombreClienteConsulta;
	}

	public void setNombreClienteConsulta(String nombreClienteConsulta) {
		this.nombreClienteConsulta = nombreClienteConsulta;
	}

	public CxcRevisionDiarios getRevision() {
		return revision;
	}

	public void setRevision(CxcRevisionDiarios revision) {
		this.revision = revision;
	}

	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	public boolean isAprobadoRevision() {
		return aprobadoRevision;
	}

	public void setAprobadoRevision(boolean aprobadoRevision) {
		this.aprobadoRevision = aprobadoRevision;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public boolean isDevolver() {
		return devolver;
	}

	public void setDevolver(boolean devolver) {
		this.devolver = devolver;
	}

	public boolean isAjustar() {
		return ajustar;
	}

	public void setAjustar(boolean ajustar) {
		this.ajustar = ajustar;
	}

	public UsuarioSis getUsuarioSeleccionado() {
		return usuarioSeleccionado;
	}

	public void setUsuarioSeleccionado(UsuarioSis usuarioSeleccionado) {
		this.usuarioSeleccionado = usuarioSeleccionado;
	}

	public List<UsuarioSis> getUsuariosContabilidad() {
		return usuariosContabilidad;
	}

	public void setUsuariosContabilidad(List<UsuarioSis> usuariosContabilidad) {
		this.usuariosContabilidad = usuariosContabilidad;
	}


	public boolean isEsRevision() {
		return esRevision;
	}


	public void setEsRevision(boolean esRevision) {
		this.esRevision = esRevision;
	}


	public List<ConfirmarDeposito> getDepositosConfirmados() {
		return depositosConfirmados;
	}


	public void setDepositosConfirmados(List<ConfirmarDeposito> depositosConfirmados) {
		this.depositosConfirmados = depositosConfirmados;
	}

}