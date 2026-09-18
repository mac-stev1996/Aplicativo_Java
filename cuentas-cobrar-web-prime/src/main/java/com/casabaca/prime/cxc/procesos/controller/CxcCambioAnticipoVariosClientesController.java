package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
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

import com.casabaca.common.Duplex;
import com.casabaca.common.ejb.model.Arccda;
import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.model.InvControl;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.InvControlServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcAnticipoLineaDto;
import com.casabaca.cxc.ejb.dto.CxcDocumentosFinanciadosDto;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiarios;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiariosPK;
import com.casabaca.cxc.ejb.servicio.CambioAnticipoVariosClientesServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcRevisionDiariosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * Controlador para realizar el cambio anticipo a varios Clientes
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped	
@ManagedBean(name = "cxcCambioAnticipoVariosClientesController")
public class CxcCambioAnticipoVariosClientesController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaService;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeService;

	@EJB(lookup = NombreJNDI.CAMBIO_ANTICIPO_CLIENTES_SERVICE_BEAN)
	private CambioAnticipoVariosClientesServiceLocal cambioVariosClieService;

	@EJB(lookup = NombreJNDI.INV_CONTROL_SERVICE)
	private InvControlServiceLocal invControlService;
	
	@EJB(lookup = NombreJNDI.CXC_REVISION_DIARIOS_SERVICE_BEAN)
	private CxcRevisionDiariosServiceLocal revisionServiceLocal;
	
	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;
	
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
	private List<CxcAnticipoLineaDto> anticiposCliente;
	private List<LineaNegocio> lineasAnticipos;
	private Map<String, BigDecimal> consumosMap;
	private BigDecimal montoTotal;
	private String comentario;
	private String lineaSeleccionada;
	private boolean activarProceso;
	private List<Arccda> diariosResult;
	
	private Date fechaProceso;
	private List<Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>>> clientesAplica;
	private String comentarioDestino;
	private Cliente clienteDestino;
	List<CxcDocumentosFinanciadosDto> documentosPantalla;
	private BigDecimal totalDocumentos;
	private BigDecimal diferencia;
	private BigDecimal montoTotalClientes;
	
	private Map<String, String> archivosAdjuntos;
	private List<String> listaAdjuntos;
	private BigDecimal totalDisponibleCliente;
	private boolean activarRegresoDocumentos;
	
	//REQ-42379 LM_LLANGARI
	private String cedulaTerceros;
	private String nombreTerceros;
	private String apellidosTerceros;
	private Cliente clienteTerceros;
	private Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> clienteAplicaSeleccionado;
	
	
	
	@PostConstruct
	public void init() {
		
		lineasAnticipos = lineaService.lineasNegocioByNoCia(getCompania().getNoCia());
		//Ordeno por tipo de linea
		lineasAnticipos.sort((p1, p2)->new Integer(p1.getLineaNegocioPK().getNoLinea()).compareTo(new Integer(p2.getLineaNegocioPK().getNoLinea())));
		
		clientes = new LazyDataModelClientes(0, 8, getCompania().getNoCia());
		anticiposCliente = new ArrayList<>();
		clienteSeleccionado = new Cliente(new ClientePK());
		clienteTerceros = new Cliente(new ClientePK());
		montoTotal = BigDecimal.ZERO;
		totalDocumentos= BigDecimal.ZERO;
		montoTotalClientes= BigDecimal.ZERO;
		consumosMap = new HashMap<>();
		lineaSeleccionada = null;
		activarProceso = false;
		InvControl control = invControlService.obtenerPorNociaCentro(getCompania().getNoCia(), getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
		fechaProceso = control.getDiaProcesoCxc();
		clientesAplica = new ArrayList<>();
		documentosPantalla = new ArrayList<>();
		archivosAdjuntos = new HashMap<>();
		listaAdjuntos = new ArrayList<>();
	}

	public void limpiarClientes(){
		clientes = new LazyDataModelClientes(0, 8, getCompania().getNoCia());
	}
	
	
	public void limpiar() {
		init();
		comentario = new String();
		comentarioDestino = new String();
	}
	
	public void consultarDocumentosCliente(Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> item){
		activarRegresoDocumentos=true;
		totalDisponibleCliente = new BigDecimal(String.valueOf(item.getSegundoElemento().getPrimerElemento()));
		documentosPantalla= new ArrayList<>();
		
		if(item.getSegundoElemento().getSegundoElemento() == null ||  item.getSegundoElemento().getSegundoElemento().isEmpty()){
			
		
		
		List<Arccmd> deudasCliente = arccmdService.obtenerArccmdSaldoCliente(getCompania().getNoCia(), String.valueOf(item.getPrimerElemento().getClientePK().getNoCliente()));
		
		if(deudasCliente == null || deudasCliente.isEmpty()){
			warn("El cliente no tiene documentos a credito o a financiar ");
			return;
		}
		 List<CxcDocumentosFinanciadosDto> aux = new ArrayList<>();
		for(Arccmd deuda: deudasCliente){
			aux.add(transformarArccmdADocumento(deuda,fechaProceso));
		}
		
		item.getSegundoElemento().setSegundoElemento(aux);
		}
//		if(item.getSegundoElemento().getSegundoElemento() == null || item.getSegundoElemento().getSegundoElemento().isEmpty()){
//			 List<CxcDocumentosFinanciadosDto> aux = nativeService.getDocumentosPorCliente(getCompania().getNoCia(), String.valueOf(item.getPrimerElemento().getClientePK().getNoCliente()), fechaProceso);
//			if(aux == null || aux.isEmpty()){
//			
//			}
//			item.getSegundoElemento().setSegundoElemento(aux);
//			
//		}
		documentosPantalla = item.getSegundoElemento().getSegundoElemento();
		calcularTotalDocumentos();
		accionesDialog("DlgDocumentos", true);
	}
	
	
	private CxcDocumentosFinanciadosDto transformarArccmdADocumento(Arccmd deuda, Date fechaProceso){
		CxcDocumentosFinanciadosDto documento = new CxcDocumentosFinanciadosDto();
		documento.setNoCia(getCompania().getNoCia());
		documento.setNoCliente(deuda.getNoCliente());
		documento.setNoDocu(deuda.getId().getNoDocu());
		documento.setSecuencia(deuda.getCantProrrogas());
		documento.setTipoDoc(deuda.getTipoDoc());
		documento.setSaldo(deuda.getSaldo());
		documento.setFecha(fechaProceso);
		documento.setAbono(BigDecimal.ZERO);
		documento.setTotalPago(BigDecimal.ZERO);
		documento.setNoFisico(deuda.getNoFisico());
		documento.setNoDocuPago(null);
		documento.setFechaVence(deuda.getFechaVence());
		System.out.println("prinSerie Fisico "+deuda.getSerieFisico());
		documento.setLineaNegocio(deuda.getSerieFisico());
		try {
			nativeService.insertarDocumentoDeuda(documento);
		} catch (InsertException e) {
			
			e.printStackTrace();
		}
		return documento;
	}
	
	public void calcularTotalDocumentos(){
		activarRegresoDocumentos=true;
		totalDocumentos = BigDecimal.ZERO;
		for(CxcDocumentosFinanciadosDto item: documentosPantalla){
			item.setTotalPago(item.getAbono());
			totalDocumentos = totalDocumentos.add(item.getTotalPago());
		}
		if(totalDisponibleCliente.compareTo(totalDocumentos) < 0){
			warn("Error: El total a abonar es mayor al valor dsiponible para el cliente");
			activarRegresoDocumentos=false;
		}
	}
	
	public void seleccionarClienteDestino(){
		Cliente clienteDestinoCopia = copiarCliente(clienteDestino);
			if (clienteDestino != null && clienteDestino.getClientePK() != null) {

	        List<CxcDocumentosFinanciadosDto> docsCliente =
	                nativeService.getDocumentosPorCliente(
	                        getCompania().getNoCia(),
	                        String.valueOf(clienteDestino.getClientePK().getNoCliente()),
	                        fechaProceso);

	        Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> aplicacion =
	                new Duplex<>(clienteDestinoCopia, new Duplex<>(BigDecimal.ZERO, docsCliente));

	        aplicacion.getPrimerElemento().setNombreTerceros(clienteSeleccionado.getNombres());
	        if(clienteSeleccionado.getApellidos() == null ||
	        		clienteSeleccionado.getApellidos().trim().isEmpty() ||
	        	    !clienteSeleccionado.getApellidos().matches(".*[a-zA-ZÁÉÍÓÚáéíóúÑñ].*")) {
	        	 aplicacion.getPrimerElemento().setApellidosTerceros(clienteSeleccionado.getNombres());	       
	        }else {	
	        	 aplicacion.getPrimerElemento().setApellidosTerceros(clienteSeleccionado.getApellidos());
	        }
	        aplicacion.getPrimerElemento().setCedulaTerceros(clienteSeleccionado.getCedula());

	        clientesAplica.add(aplicacion);

	        clienteAplicaSeleccionado = aplicacion;

	        clienteDestino = new Cliente();
	        clientes = new LazyDataModelClientes(0, 8, getCompania().getNoCia());
	        accionesDialog("DlgClientesDest", false);

	    } else {
	        warn("Debe seleccionar un cliente");
	    }
		
	}

	public void elminarCliente(Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> item){
		clientesAplica.remove(item);
		calcularTotalClientes();
	}
	
	public void procesar() {
		
		if (lineaSeleccionada != null && comentario != null && !comentario.isEmpty()
				&& montoTotal.compareTo(BigDecimal.ZERO) > 0) {
			List<CxcAnticipoLineaDto> anticiposUtilizados =  new ArrayList<>();
			for(CxcAnticipoLineaDto  ant :anticiposCliente){
				if(ant.getValorUtilizar() != null && ant.getValorUtilizar().compareTo(BigDecimal.ZERO) > 0){
					anticiposUtilizados.add(ant);
				}
			}
			
			try {
				LineaNegocio linCambio = lineaService.getLineaNegocioxNociaxNoLinea(getCompania().getNoCia(), lineaSeleccionada);
				
				diariosResult = cambioVariosClieService.cambiarlineaAnticipo(anticiposUtilizados, linCambio, comentario, montoTotal, 
						getUsuario().getUsuario(), getCompania().getNoCia(),
						getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), clienteSeleccionado, getCompania().getAplicaOrion(), clientesAplica, comentarioDestino);
				for(Arccda darioItem : diariosResult){
					guardarRevision(darioItem);	
				}
				
				
				if (diariosResult != null && !diariosResult.isEmpty()) {
					accionesDialog("DlgResult", true);
					limpiar();
				}
			} catch (Exception e) {
					e.printStackTrace();
				warn("ERROR AL PROCESAR: " + e.toString());
			}

		} else {
			warn("Debe seleccionar una linea de negocio y poner un comentario");
		}

	}

	public void buscarAnticipos() {

		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null
				&& clienteSeleccionado.getClientePK().getNoCliente() != null && lineaSeleccionada != null) {
			try {
				System.out.println("LINEA "+lineaSeleccionada);
				//Obtengo anticipos por lineas de negocio
				anticiposCliente = nativeService.getAnticiposLineaPorCedula(getCompania().getNoCia(),
						clienteSeleccionado.getCedula(),lineaSeleccionada);
			} catch (FindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			warn("Por favor ingrese Un cliente y una linea de negocio");
		}

	}

	public void seleccionarCliente() {
		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null) {
			accionesDialog("DlgClientes", false);
		} else {
			warn("Debe seleccionar un cliente");
		}
	}

	public void calcularTotal() {
		
		montoTotal = BigDecimal.ZERO;
		for (CxcAnticipoLineaDto ant : anticiposCliente) {
			montoTotal = montoTotal.add(ant.getValorUtilizar());
		}
		diferencia=montoTotalClientes.subtract(montoTotalClientes);
	}
	
	public void calcularTotalClientes() {
		activarProceso = false;
		montoTotalClientes = BigDecimal.ZERO;
		for (Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> ant : clientesAplica) {
			System.out.println("NUMERO "+ ant.getSegundoElemento().getPrimerElemento());
			montoTotalClientes = montoTotalClientes.add(new BigDecimal(String.valueOf(ant.getSegundoElemento().getPrimerElemento())));
		}
		diferencia=montoTotalClientes.subtract(montoTotalClientes);
		if (montoTotalClientes.compareTo(BigDecimal.ZERO) > 0 && montoTotalClientes.compareTo(montoTotal) == 0) {
			activarProceso = true;
		}else{
			warn("Los totales de anticipos entre clientes no son iguales para procesar");
		}
	}
	

	public void actualizarAnticipos() {
		buscarAnticipos();
		accionesDialog("DlgResult", false);
	}

	public void imprimirDiario(Arccda diarioA) {
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
				header.append("DIARIO_A" + diarioA.getNoFisico() + "-" + diarioA.getArccdaPk().getNoDocu() + ".pdf");
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

	private void generarReporteJasper(Arccda diarioA) {
		String reportPath = "/reportes/cxcComprobanteDiariosA.jasper";
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		JasperPrint jasperPrint = null;

		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParametersDiariosA(parameters, getCompania().getNoCia(), diarioA.getArccdaPk().getNoDocu(),
					diarioA.getUsuario());
			connection = utilServiceDelegate.getDataSource().getConnection();
			String ctxPath = getServletContext().getRealPath("/");
			
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);

			StringBuffer header = new StringBuffer();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			header.append("inline; filename=\"");
			header.append("DIARIO_A" + diarioA.getNoFisico() + "-" + diarioA.getArccdaPk().getNoDocu() + ".pdf");
			header.append("\"");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
			virtualizer.cleanup();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void setParametersDiariosA(Map<String, Object> parameters, String noCia, String noDocu, String usuario) {
		parameters.put("P_NO_CIA", noCia);
		parameters.put("P_NO_DOCU", noDocu);
		parameters.put("USUARIO", usuario);
		parameters.put("EMPRESA", getCompania().getNombre());
		parameters.put("SUBREPORT_DIR", getPathReal());

	}
	


	private void guardarRevision(Arccda diario){
		CxcRevisionDiarios rev = new CxcRevisionDiarios();
		rev.setRevisionDiarioPk(new  CxcRevisionDiariosPK(getCompania().getNoCia(), diario.getArccdaPk().getNoDocu()));
		rev.setUsuario(getUsuario().getUsuario());
		rev.setFecha(new Date());
		rev.setRevAprobada("N");
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
		rev.setAdjuntos(adjuntos);
		try {
			
			revisionServiceLocal.crear(rev);
		}catch (Exception e) {
			
			warn("Error al guardar el archivo del diario A");
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
	
	public void quitarArchivo(String nombreArchivo) {
		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos.remove(nombreArchivo);
	}
	
	public void cargarArchivo(FileUploadEvent event) {
		try {
			if(clienteSeleccionado == null ||clienteSeleccionado.getClientePK() == null || clienteSeleccionado.getClientePK().getNoCliente() == null){
				warn("Primero debe seleccionar un cliente");
				return;
			}
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
			SimpleDateFormat sdf = new SimpleDateFormat("HH-ddMMyyyy");
			File folder = new File(raizDiarios + clienteSeleccionado.getClientePK().getNoCliente()+lineaSeleccionada+sdf.format(new Date()));
			if (!folder.exists()) {
				folder.mkdir();
			}
			raizDiarios = raizDiarios + clienteSeleccionado.getClientePK().getNoCliente()+lineaSeleccionada +sdf.format(new Date())+ System.getProperty("file.separator");

			File archivo = new File(raizDiarios + uf.getFileName());
			copyInputStreamToFile(is1, archivo);

			archivosAdjuntos.put(uf.getFileName(), raizDiarios + uf.getFileName());
			listaAdjuntos.add(uf.getFileName());
		} catch (Exception e) {
			e.printStackTrace();
		}

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
	
	public void ingresoTerceros(Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> item) {
		
		accionesDialog("DlgTercerosWV", true);
		
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

	public List<CxcAnticipoLineaDto> getAnticiposCliente() {
		return anticiposCliente;
	}

	public void setAnticiposCliente(List<CxcAnticipoLineaDto> anticiposCliente) {
		this.anticiposCliente = anticiposCliente;
	}

	public List<LineaNegocio> getLineasAnticipos() {
		return lineasAnticipos;
	}

	public void setLineasAnticipos(List<LineaNegocio> lineasAnticipos) {
		this.lineasAnticipos = lineasAnticipos;
	}

	public Map<String, BigDecimal> getConsumosMap() {
		return consumosMap;
	}

	public void setConsumosMap(Map<String, BigDecimal> consumosMap) {
		this.consumosMap = consumosMap;
	}

	public BigDecimal getMontoTotal() {
		return montoTotal;
	}

	public void setMontoTotal(BigDecimal montoTotal) {
		this.montoTotal = montoTotal;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getLineaSeleccionada() {
		return lineaSeleccionada;
	}

	public void setLineaSeleccionada(String lineaSeleccionada) {
		this.lineaSeleccionada = lineaSeleccionada;
	}

	public boolean isActivarProceso() {
		return activarProceso;
	}

	public void setActivarProceso(boolean activarProceso) {
		this.activarProceso = activarProceso;
	}

	public List<Arccda> getDiariosResult() {
		return diariosResult;
	}

	public void setDiariosResult(List<Arccda> diariosResult) {
		this.diariosResult = diariosResult;
	}

	public List<Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>>> getClientesAplica() {
		return clientesAplica;
	}

	public void setClientesAplica(
			List<Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>>> clientesAplica) {
		this.clientesAplica = clientesAplica;
	}

	public String getComentarioDestino() {
		return comentarioDestino;
	}

	public void setComentarioDestino(String comentarioDestino) {
		this.comentarioDestino = comentarioDestino;
	}

	public List<CxcDocumentosFinanciadosDto> getDocumentosPantalla() {
		return documentosPantalla;
	}

	public void setDocumentosPantalla(List<CxcDocumentosFinanciadosDto> documentosPantalla) {
		this.documentosPantalla = documentosPantalla;
	}

	public Cliente getClienteDestino() {
		return clienteDestino;
	}

	public void setClienteDestino(Cliente clienteDestino) {
		this.clienteDestino = clienteDestino;
	}

	public BigDecimal getTotalDocumentos() {
		return totalDocumentos;
	}

	public void setTotalDocumentos(BigDecimal totalDocumentos) {
		this.totalDocumentos = totalDocumentos;
	}

	public BigDecimal getMontoTotalClientes() {
		return montoTotalClientes;
	}

	public void setMontoTotalClientes(BigDecimal montoTotalClientes) {
		this.montoTotalClientes = montoTotalClientes;
	}

	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	public BigDecimal getTotalDisponibleCliente() {
		return totalDisponibleCliente;
	}

	public void setTotalDisponibleCliente(BigDecimal totalDisponibleCliente) {
		this.totalDisponibleCliente = totalDisponibleCliente;
	}

	public boolean isActivarRegresoDocumentos() {
		return activarRegresoDocumentos;
	}

	public void setActivarRegresoDocumentos(boolean activarRegresoDocumentos) {
		this.activarRegresoDocumentos = activarRegresoDocumentos;
	}

	public BigDecimal getDiferencia() {
		return diferencia;
	}

	public void setDiferencia(BigDecimal diferencia) {
		this.diferencia = diferencia;
	}

	/**
	 * @return the cedulaTerceros
	 */
	public String getCedulaTerceros() {
		return cedulaTerceros;
	}

	/**
	 * @return the nombreTerceros
	 */
	public String getNombreTerceros() {
		return nombreTerceros;
	}

	/**
	 * @return the apellidosTerceros
	 */
	public String getApellidosTerceros() {
		return apellidosTerceros;
	}

	/**
	 * @param cedulaTerceros the cedulaTerceros to set
	 */
	public void setCedulaTerceros(String cedulaTerceros) {
		this.cedulaTerceros = cedulaTerceros;
	}

	/**
	 * @param nombreTerceros the nombreTerceros to set
	 */
	public void setNombreTerceros(String nombreTerceros) {
		this.nombreTerceros = nombreTerceros;
	}

	/**
	 * @param apellidosTerceros the apellidosTerceros to set
	 */
	public void setApellidosTerceros(String apellidosTerceros) {
		this.apellidosTerceros = apellidosTerceros;
	}

	/**
	 * @return the clienteTerceros
	 */
	public Cliente getClienteTerceros() {
		return clienteTerceros;
	}

	/**
	 * @param clienteTerceros the clienteTerceros to set
	 */
	public void setClienteTerceros(Cliente clienteTerceros) {
		this.clienteTerceros = clienteTerceros;
	}

	/**
	 * @return the clienteAplicaSeleccionado
	 */
	public Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> getClienteAplicaSeleccionado() {
		if (clienteAplicaSeleccionado == null) {
            clienteAplicaSeleccionado = new Duplex<>(new Cliente(), null);
        }
        return clienteAplicaSeleccionado;
	}

	/**
	 * @param clienteAplicaSeleccionado the clienteAplicaSeleccionado to set
	 */
	public void setClienteAplicaSeleccionado(
			Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> clienteAplicaSeleccionado) {
		this.clienteAplicaSeleccionado = clienteAplicaSeleccionado;
	}

	public void prepararDatosTerceros(
            Duplex<Cliente, Duplex<BigDecimal, List<CxcDocumentosFinanciadosDto>>> fila) { 
		clienteAplicaSeleccionado = fila;
		if (clienteAplicaSeleccionado != null) {
	        System.out.println("Seleccionado (sin parámetro): " +
	                clienteAplicaSeleccionado.getPrimerElemento().getCedulaTerceros());
	    } else {
	        System.out.println("clienteAplicaSeleccionado ES NULL");
	    }
			
    }
	
	private Cliente copiarCliente(Cliente origen) {
	    Cliente copia = new Cliente();

	    copia.setClientePK(origen.getClientePK());
	    copia.setNombre(origen.getNombre());
	    copia.setNombres(origen.getNombres());
	    copia.setApellidos(origen.getApellidos());
	    copia.setCedula(origen.getCedula());

	    copia.setNombreTerceros(origen.getNombreTerceros());
	    copia.setApellidosTerceros(origen.getApellidosTerceros());
	    copia.setCedulaTerceros(origen.getCedulaTerceros());

	    return copia;
	}
	

}