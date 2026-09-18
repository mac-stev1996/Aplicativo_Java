package com.casabaca.prime.cxc.consultas.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.EjecutaComandoUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiarios;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcRevisionDiariosServiceLocal;
import com.casabaca.cxc.ejb.servicio.HistoricoDocumentosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.inventario.ejb.model.FacVentasCabPK;
import com.casabaca.inventario.ejb.service.FacVentasServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * @author jreyes
 *
 */
@ViewScoped
@ManagedBean
public class ConsultaCupoPostVentController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -440176611185950870L;

	private static final Logger LOG = Logger.getLogger(ConsultaCupoPostVentController.class);
	private static final String LINEA_REPUESTOS = "REP";
	private static final String PDF_PATH_FC = "gnvoiceCasabaca/descargarDocumento/obtenerFacturaPDF/";

	private Cliente cliente;

	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	@EJB(lookup = NombreJNDI.JNDI_CXC + "HistoricoDocumentosServiceBean")
	private HistoricoDocumentosServiceLocal historicoDocumentosService;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal cxcNativeServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_REVISION_DIARIOS_SERVICE_BEAN)
	private CxcRevisionDiariosServiceLocal revisionServiceLocal;

	@EJB(lookup = NombreJNDI.FAC_VENTAS_SERVICE)
	private FacVentasServiceLocal facVentasServiceLocal;

	private List<Object[]> listadoFacturas;
	private List<Object[]> listadoPagos;
	private List<LineaNegocio> lineasNegocio;
	private String lineaNegocio;
	private Date fechaDesde;
	private Date fechaHasta;
	private BigDecimal cupoRepuestos;
	private Boolean activarCupoRepuestos;

	@PostConstruct
	public void init() {
		listadoFacturas = new ArrayList<Object[]>();
		listadoPagos = new ArrayList<Object[]>();
		lineasNegocio = new ArrayList<LineaNegocio>();
		lineaNegocio = null;
		cupoRepuestos = BigDecimal.ZERO;
		activarCupoRepuestos = Boolean.FALSE;
		try {
			lineasNegocio = lineaNegocioService.buscarLineaNegocioLista(getCompania().getNoCia());
		} catch (FindException e) {

			e.printStackTrace();
		}

	}

	public void buscar() {
		listadoFacturas = new ArrayList<Object[]>();
		listadoPagos = new ArrayList<Object[]>();

		if (fechaDesde != null || fechaHasta != null) {
			List<LineaNegocio> listado = lineaNegocioService.obtenerLineasTipoNegocio(getCompania().getNoCia(),
					LINEA_REPUESTOS);

			if (listado.stream().anyMatch(d -> d.getLineaNegocioPK().getNoLinea().equals(lineaNegocio))) {
				cupoRepuestos = cxcNativeServiceLocal.obtenerValorCupoRepuestos(getCompania().getNoCia(),
						cliente.getClientePK().getNoCliente());
				activarCupoRepuestos = Boolean.TRUE;

			} else
				activarCupoRepuestos = Boolean.FALSE;

			listadoFacturas = historicoDocumentosService.listadoFacturasCliente(getCompania().getNoCia(), lineaNegocio,
					cliente.getClientePK().getNoCliente(), fechaDesde, fechaHasta);
			listadoPagos = historicoDocumentosService.listadoPagosCliente(getCompania().getNoCia(), lineaNegocio,
					cliente.getClientePK().getNoCliente(), fechaDesde, fechaHasta);

		} else {

			error("El campo Fecha desde y hasta es requerido!!!");

		}

	}

	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Cliente getCliente() {
		if (Objects.nonNull(dialogClientesController.getCliente())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK().getNoCliente())) {
			cliente = dialogClientesController.getCliente();
			dialogClientesController.setCliente(null);
		}
		return cliente;
	}

	/**
	 * Permite generar el comprobante de Pago
	 * 
	 * @param ingresoCajaDto
	 */
	public void generarComprobantePago(Object[] obj) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("P_NO_CIA", getCompania().getNoCia());
		parameters.put("P_CENTRO", obj[1].toString());
		parameters.put("P_NO_DOCU", obj[13]);
		parameters.put("P_LOGO", obtenerLogoEmpresa());
		parameters.put("IMPRIME_CABECERA", CommonConstants.TRUE_VALUE);
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			String reporte = obtenerRuta("repComprobantePago.jasper");
			parameters.put("SUBREPORT_DIR", reporte.split("repComprobantePago"));
			connection = utilServiceDelegate.getDataSource().getConnection();
			JasperPrint jasperPrint = JasperFillManager.fillReport(reporte, parameters, connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			response.setContentType("application/pdf");
			StringBuffer header = new StringBuffer();
			header.append("attachment; filename=\"");
			header.append("comprobantePago.pdf");
			header.append("\"");
			response.setHeader("Content-Disposition", header.toString());
			response.setContentType("application/pdf");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (SQLException | IOException | JRException e) {
			super.error("Error al ejecutar reporte: " + this.obtainException(e));
		} finally {
			try {
				if (connection != null && !connection.isClosed())
					connection.close();
			} catch (SQLException e) {
				error(this.obtainException(e));
			}
		}
	}

	public void imprimirDiario(Object[] obj) throws IOException, SQLException {
		if (obj[14] == null) {
			generarReporteJasper(obj);
			return;
		}

		try {
			String destPath = null;
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			destPath = obj[14].toString();

			File file = new File(destPath);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				byte[] fileBytes = file.getName().getBytes();

				StringBuffer header = new StringBuffer();
				header.append("filename=\"");
				header.append("DIARIO_A" + obj[4] + "-" + obj[13] + ".pdf");
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
			LOG.error(e);
		}
	}

	/**
	 * Permite obtener el logo de la empresa
	 * 
	 * @return
	 */
	private String obtenerLogoEmpresa() {
		String rutaLogoEmpresa = null;
		if (CommonConstants.CASABACA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoCasabaca.jpg");
		} else if (CommonConstants.TOYOCOSTAS.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoToyocosta.jpg");
		} else if (CommonConstants.NEXUMCORP.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logotipoNexumcorp.jpg");
		} else if (CommonConstants.CARLOS_LARREA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logoCarloslarrea.jpg");
		} else if (CommonConstants.TOYOSERVICIOS.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/toyoser.jpg");
		} else if (CommonConstants.AMBAMAZDA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/ambamazda.jpg");
		} else if (CommonConstants.MANSUERA.equals(getCompania().getNoCia())) {
			rutaLogoEmpresa = getServletContext().getRealPath("/common/imgs/logoMansuera.jpg");
		}
		return rutaLogoEmpresa;
	}

	/**
	 * Permite obtener la ruta del reporte
	 * 
	 * @param nombreReporte
	 * @return
	 */
	private String obtenerRuta(String nombreReporte) {
		EjecutaComandoUtils ejecutar = new EjecutaComandoUtils();
		ejecutar.executeCommand("updatedb");
		StringBuilder comando = new StringBuilder("locate ").append(nombreReporte);
		List<String> listaResultado = ejecutar.executeCommand(comando.toString());
		String ctxPath = null;
		for (String directorio : listaResultado) {
			if (directorio.contains("jboss-EAP-7.4")) {
				ctxPath = directorio;
			}
		}
		return ctxPath;
	}

	private void generarReporteJasper(Object[] obj) throws IOException, SQLException {
		String reportPath = "/reportes/cxcComprobanteDiariosA.jasper";
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		JasperPrint jasperPrint = null;
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParametersDiariosA(parameters, getCompania().getNoCia(), obj[13].toString(), obj[15].toString());
			connection = utilServiceDelegate.getDataSource().getConnection();
			String ctxPath = getServletContext().getRealPath("/");
			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);

			StringBuffer header = new StringBuffer();
			header.append("filename=\"");
			header.append("DIARIO_A" + obj[4] + "-" + obj[13] + ".pdf");
			header.append("\"");
			response.addHeader("Content-Disposition", header.toString());
			response.setContentType("application/pdf");

			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();

		} catch (Exception e) {
		} finally {
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

		CxcRevisionDiarios diarioRev = revisionServiceLocal.obtenerPorPk(getCompania().getNoCia(), noDocu);
		if (diarioRev != null) {
			parameters.put("USUARIO_REVISOR", diarioRev.getUsuario());
		}
	}

	public void imprimirRide(Object[] objeto) {
		String clave;
		try {
			clave = facVentasServiceLocal.getClaveAccesoNc(new FacVentasCabPK(getCompania().getNoCia(),
					objeto[18].toString(), objeto[0].toString(), objeto[17].toString()));
		} catch (FindException e) {
			clave = null;
		}
		if (clave != null && clave.length() >= 49) {
			String ip = getCompania().getIpWebServer();
			if (ip.length() > 0) {
				ip = ip.replace("8080", "18080");
			}
			String pathNC = ip + PDF_PATH_FC + clave;
			RequestContext requestContext = RequestContext.getCurrentInstance();
			requestContext.execute("openDuplicatedTab('" + pathNC + "')");
		} else {
			super.warn("No se ha generado la clave de acceso");
		}
	}

	public String nombreAgencia(String codigo) {

		return agenciaServiceLocal.buscarNombreAgencia(getCompania().getNoCia(), codigo);

	}

	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}

	public void setDialogClientesController(DialogClientesController dialogClientesController) {
		this.dialogClientesController = dialogClientesController;
	}

	public List<Object[]> getListadoFacturas() {
		return listadoFacturas;
	}

	public void setListadoFacturas(List<Object[]> listadoFacturas) {
		this.listadoFacturas = listadoFacturas;
	}

	public List<Object[]> getListadoPagos() {
		return listadoPagos;
	}

	public void setListadoPagos(List<Object[]> listadoPagos) {
		this.listadoPagos = listadoPagos;
	}

	public List<LineaNegocio> getLineasNegocio() {
		return lineasNegocio;
	}

	public void setLineasNegocio(List<LineaNegocio> lineasNegocio) {
		this.lineasNegocio = lineasNegocio;
	}

	public String getLineaNegocio() {
		return lineaNegocio;
	}

	public void setLineaNegocio(String lineaNegocio) {
		this.lineaNegocio = lineaNegocio;
	}

	public Date getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

	public BigDecimal getCupoRepuestos() {
		return cupoRepuestos;
	}

	public void setCupoRepuestos(BigDecimal cupoRepuestos) {
		this.cupoRepuestos = cupoRepuestos;
	}

	public Boolean getActivarCupoRepuestos() {
		return activarCupoRepuestos;
	}

	public void setActivarCupoRepuestos(Boolean activarCupoRepuestos) {
		this.activarCupoRepuestos = activarCupoRepuestos;
	}

}
