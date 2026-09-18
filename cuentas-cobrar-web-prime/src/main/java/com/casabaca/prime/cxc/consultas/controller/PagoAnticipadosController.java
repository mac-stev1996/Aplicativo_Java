/**
 * 
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.HistoricoDocEntradaDto;
import com.casabaca.cxc.ejb.dto.HistoricoDocumentoDto;
import com.casabaca.cxc.ejb.dto.PagoAnticipadoDto;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.servicio.HistoricoDocumentosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class PagoAnticipadosController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2709776566887030528L;
	private static final Logger LOG = Logger.getLogger(PagoAnticipadosController.class);
	private static final String REPORTE_PAGOS_ANTICIPADOS = "/reportes/pagosAnticipados.jasper";
	private static final String NOMBRE_REPORTE_PAGOS_ANTICIPADOS = "pagosAnticipados.pdf";

	@EJB(lookup = NombreJNDI.JNDI_CXC + "HistoricoDocumentosServiceBean")
	private HistoricoDocumentosServiceLocal historicoDocumentosService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeService;

	private HistoricoDocumentoDto historicoDocDto;
	private String noCia;
	private String cedulaClienteFac;
	private String nombreClienteFac;
	private HistoricoDocEntradaDto historicoDocEntradaDto;
	private List<PagoAnticipadoDto> pagos;
	private Double totalSaldo;
	private Double totalInteresDesc;
	private Double total;
	private List<Object[]> tiposOper;
	private String tipoOper;

	public PagoAnticipadosController() {
		this.noCia = getCompania().getNoCia();
	}

	@PostConstruct
	public void init() {
		historicoDocEntradaDto = new HistoricoDocEntradaDto();
		historicoDocEntradaDto.setNoCia(this.noCia);
		historicoDocEntradaDto.setNoCliente(Long.valueOf(getRequestParameter("noCliente")));
		historicoDocEntradaDto.setFechaInicio(getRequestParameter("fechaIni") != null
				? FechaUtils.convertirStrFecha(getRequestParameter("fechaIni"), FechaUtils.patronDiaMesAnio)
				: null);
		historicoDocEntradaDto.setFechaFin(getRequestParameter("fechaFin") != null
				? FechaUtils.convertirStrFecha(getRequestParameter("fechaFin"), FechaUtils.patronDiaMesAnio)
				: null);
		historicoDocEntradaDto.setLineaNegocio(getRequestParameter("lineaNeg"));
		historicoDocEntradaDto.setTipoDoc(getRequestParameter("tipoDoc"));
		historicoDocEntradaDto.setAnulado(getRequestParameter("anulado"));
		historicoDocEntradaDto.setDocumento(getRequestParameter("documento"));
		historicoDocEntradaDto.setAplicaDesbloqueo("S".equals(getRequestParameter("APLDESB")));

		historicoDocDto = new HistoricoDocumentoDto();
		historicoDocDto.setNoCia(this.noCia);
		historicoDocDto.setSerieFisico(getRequestParameter("serieFisico"));
		historicoDocDto.setNoFisico(getRequestParameter("noFisico"));
		historicoDocDto.setCantProrroga(
				getRequestParameter("cantProrrogas") != null ? Integer.parseInt(getRequestParameter("cantProrrogas"))
						: 0);
		historicoDocDto.setAgencia(getRequestParameter("agencia"));
		historicoDocDto.setNoCliente(historicoDocEntradaDto.getNoCliente());
		historicoDocDto
				.setFecha(FechaUtils.convertirStrFecha(getRequestParameter("fecha"), FechaUtils.patronDiaMesAnio));
		tiposOper = nativeService.consultarTipoOper(this.noCia);
		consultarDatosCliente();
	}

	public void consultar() {
		totalSaldo = 0D;
		totalInteresDesc = 0D;
		total = 0D;
		pagos = historicoDocumentosService.consultarPagosAnticipados(
				getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(), tipoOper, historicoDocDto);
		if (pagos != null && !pagos.isEmpty()) {
			totalSaldo = pagos.stream().mapToDouble(p -> p.getSaldo() != null ? p.getSaldo() : 0D).sum();
			totalInteresDesc = pagos.stream().mapToDouble(p -> p.getInteresDesc() != null ? p.getInteresDesc() : 0D)
					.sum();
			total = pagos.stream().mapToDouble(p -> p.getTotal() != null ? p.getTotal() : 0D).sum();
		}
	}

	public String regresar() {
		StringBuilder parametros = new StringBuilder();
		parametros.append("&lineaNeg=").append(historicoDocEntradaDto.getLineaNegocio());
		parametros.append("&tipoDoc=").append(historicoDocEntradaDto.getTipoDoc());
		parametros.append("&anulado=").append(historicoDocEntradaDto.getAnulado());
		parametros.append("&documento=").append(historicoDocEntradaDto.getDocumento());
		parametros.append("&fechaIni=").append(historicoDocEntradaDto.getFechaInicio() == null ? ""
				: FechaUtils.formatearFecha(historicoDocEntradaDto.getFechaInicio(), FechaUtils.patronDiaMesAnio));
		parametros.append("&fechaFin=").append(historicoDocEntradaDto.getFechaFin() == null ? ""
				: FechaUtils.formatearFecha(historicoDocEntradaDto.getFechaFin(), FechaUtils.patronDiaMesAnio));
		parametros.append("&noCliente=").append(historicoDocDto.getNoCliente());
		parametros.append(historicoDocEntradaDto.isAplicaDesbloqueo() ? "&APLDESB=S":"");
		return "consultaHistDocumentoClientes?faces-redirect=true" + parametros.toString();
	}

	/**
	 * Permite generar el reporte de historico de pagos
	 */
	public void generarReporte() {

		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			connection = utilServiceDelegate.getDataSource().getConnection();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			String ctxPath = getServletContext().getRealPath("/") + REPORTE_PAGOS_ANTICIPADOS;
			
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters = obtenerParametros() ;
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath, obtenerParametros(), connection);
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition",
					"attachment;filename=\"" + NOMBRE_REPORTE_PAGOS_ANTICIPADOS + "\"");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException | JRException e) {
			LOG.error(e.getMessage() + ".EE. ", e);
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			cerrarConexion(connection);
		}

	}

	/**
	 * Permite asignar los parametros del comprobante
	 * 
	 * @return
	 */
	private Map<String, Object> obtenerParametros() {
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("p_no_cia", this.noCia);
		parametros.put("p_agencia", historicoDocDto.getAgencia());
		parametros.put("p_nombre_empresa", getCompania().getNombre().toUpperCase());
		parametros.put("p_usuario", getLoggedUsername());
		parametros.put("p_no_fisico", historicoDocDto.getNoFisico());
		parametros.put("p_serie_fisico", historicoDocDto.getSerieFisico());
		parametros.put("p_cant_prorrogas", historicoDocDto.getCantProrroga());
		parametros.put("p_no_cliente", historicoDocDto.getNoCliente());
		return parametros;
	}

	/**
	 * Permite cerrar la conexion a la base de datos
	 * 
	 * @param con
	 */
	private void cerrarConexion(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	private void consultarDatosCliente() {
		try {
			Cliente datosCliente = clienteService
					.findByPk(new ClientePK(this.noCia, "01", historicoDocDto.getNoCliente()));
			cedulaClienteFac = datosCliente.getCedula();
			nombreClienteFac = datosCliente.getNombre();
		} catch (FindException e) {
			cedulaClienteFac = "0";
			cedulaClienteFac = "aa";
		}
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public HistoricoDocumentoDto getHistoricoDocDto() {
		return historicoDocDto;
	}

	public void setHistoricoDocDto(HistoricoDocumentoDto historicoDocDto) {
		this.historicoDocDto = historicoDocDto;
	}

	public String getCedulaClienteFac() {
		return cedulaClienteFac;
	}

	public void setCedulaClienteFac(String cedulaClienteFac) {
		this.cedulaClienteFac = cedulaClienteFac;
	}

	public String getNombreClienteFac() {
		return nombreClienteFac;
	}

	public void setNombreClienteFac(String nombreClienteFac) {
		this.nombreClienteFac = nombreClienteFac;
	}

	public HistoricoDocEntradaDto getHistoricoDocEntradaDto() {
		return historicoDocEntradaDto;
	}

	public void setHistoricoDocEntradaDto(HistoricoDocEntradaDto historicoDocEntradaDto) {
		this.historicoDocEntradaDto = historicoDocEntradaDto;
	}

	public List<PagoAnticipadoDto> getPagos() {
		return pagos;
	}

	public void setPagos(List<PagoAnticipadoDto> pagos) {
		this.pagos = pagos;
	}

	public Double getTotalSaldo() {
		return totalSaldo;
	}

	public void setTotalSaldo(Double totalSaldo) {
		this.totalSaldo = totalSaldo;
	}

	public Double getTotalInteresDesc() {
		return totalInteresDesc;
	}

	public void setTotalInteresDesc(Double totalInteresDesc) {
		this.totalInteresDesc = totalInteresDesc;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public List<Object[]> getTiposOper() {
		return tiposOper;
	}

	public void setTiposOper(List<Object[]> tiposOper) {
		this.tiposOper = tiposOper;
	}

	public String getTipoOper() {
		return tipoOper;
	}

	public void setTipoOper(String tipoOper) {
		this.tipoOper = tipoOper;
	}

}
