package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import com.casabaca.common.ejb.model.Arccda;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcAnticipoLineaDto;
import com.casabaca.cxc.ejb.servicio.CambioLineaAnticipoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * Controlador para realizar el cambio de linea de anticipo
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcCambioLineaAnticipoController")
public class CxcCambioLineaAnticipoController extends CommonController implements Serializable {

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

	@EJB(lookup = NombreJNDI.CAMBIO_LINEA_ANTICIPO_SERVICE_BEAN)
	private CambioLineaAnticipoServiceLocal cambioLineaService;

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

	@PostConstruct
	public void init() {
		lineasAnticipos = lineaService.lineasNegocioByNoCia(getCompania().getNoCia());

		clientes = new LazyDataModelClientes(0, 8, getCompania().getNoCia());
		anticiposCliente = new ArrayList<>();
		clienteSeleccionado = new Cliente(new ClientePK());
		montoTotal = BigDecimal.ZERO;
		consumosMap = new HashMap<>();
		lineaSeleccionada = null;
		activarProceso = false;
	}

	public void limpiar() {
		init();
		comentario = new String();
		lineaSeleccionada = null;

	}

	public void procesar() {
		
		System.out.println("Entra a procesar");
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
				diariosResult = cambioLineaService.cambiarlineaAnticipo(anticiposUtilizados, linCambio,
						comentario, montoTotal, getUsuario().getUsuario(), getCompania().getNoCia(),
						getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), clienteSeleccionado,
						getCompania().getAplicaOrion());

				if (diariosResult != null && !diariosResult.isEmpty()) {
					accionesDialog("DlgResult", true);
				}
			} catch (Exception e) {

				warn("ERROR AL PROCESAR: " + e.toString());
			}

		} else {
			warn("Debe seleccionar una linea de negocio y poner un comentario");
		}

	}

	public void buscarAnticipos() {

		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null
				&& clienteSeleccionado.getClientePK().getNoCliente() != null) {
			try {
				anticiposCliente = nativeService.getAnticiposLineaPorCedula(getCompania().getNoCia(),
						clienteSeleccionado.getCedula(),null);
			} catch (FindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
		activarProceso = false;
		montoTotal = BigDecimal.ZERO;
		for (CxcAnticipoLineaDto ant : anticiposCliente) {
			montoTotal = montoTotal.add(ant.getValorUtilizar());
		}

		if (montoTotal.compareTo(BigDecimal.ZERO) > 0) {
			activarProceso = true;
		}
	}

	public void actualizarAnticipos() {
		buscarAnticipos();
		accionesDialog("DlgResult", false);
	}

	public void imprimirDiario(Arccda diarioA) {
		if (diarioA.getRutaArchivo() == null || diarioA.getRutaArchivo().isEmpty()) {
			generarReporteJasper(diarioA);
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
			
			jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);

			StringBuffer header = new StringBuffer();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			header.append("filename=\"");
			header.append("DIARIO_A" + diarioA.getNoFisico() + "-" + diarioA.getArccdaPk().getNoDocu() + ".pdf");
			header.append("\"");
			response.addHeader("Content-Disposition", header.toString());
			response.setContentType("application/pdf");
			
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
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

}