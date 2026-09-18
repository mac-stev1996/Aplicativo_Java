package com.casabaca.prime.cxc.procesos.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
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

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.StringUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CmlResumen;
import com.casabaca.cxc.ejb.servicio.CmlResumenServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.EnumReporteRESU;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRXlsAbstractExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;

@ManagedBean
@ViewScoped
public class CmlResumenController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8352830682208068439L;
	private static final Logger LOGGER = Logger.getLogger(CmlResumenController.class);
	private static final String PATH_REPORTE_CML_CLIENTES = "/reportes/repCmlClientes.jasper";
	private static final String PATH_REPORTE_CML_OPERACIONES = "/reportes/repCmlOperaciones.jasper";
	private static final String PATH_REPORTE_CML_TRANSACCIONES = "/reportes/repCmlTransacciones.jasper";
	private static final String PATH_REPORTE_CML_RESUMEN = "/reportes/repCmlResumen.jasper";

	@EJB(lookup = NombreJNDI.JNDI_CXC + "CmlResumenServiceBean")
	private CmlResumenServiceLocal cmlResumenService;

	private String noCia;
	private Integer anio;
	private List<CmlResumen> resumens;

	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		this.anio = FechaUtils.getAno(new Date());
		consultar();
	}

	public void consultar() {
		resumens = new ArrayList<>();
		if (anio == null) {
			error("Es obligatorio ingresar el año");
			return;
		}
		resumens = cmlResumenService.consultarResumenPorAnio(this.noCia, this.anio.toString());
		if (resumens.isEmpty()) {
			info("No existen datos con el año ingresado");
		}
	}

	public void generar(CmlResumen itemProcesa) {
		try {
			cmlResumenService.generarCalculosResumenMesCaido(itemProcesa, getUsuario().getUsuario());
			info("RESU generado con exito");
			consultar();
		} catch (InsertException e) {
			error(e.getDetail());
		} catch (FindException e) {
			error(e.getDetail());
		} catch (UpdateException e) {
			error(String.format("Se tupo problemas al actualizar el Resumen RESU. Error: %s", e.getMessage()));
		}
	}

	public void enviar(CmlResumen itemEnvia) {
		int mesActual = FechaUtils.getMes(new Date());
		int anioActual = FechaUtils.getAno(new Date());
		if (mesActual <= Integer.parseInt(itemEnvia.getId().getPeriodo())
				&& anioActual == Integer.parseInt(itemEnvia.getId().getAnio())) {
			error("No puede realizar cierres  RESU del mes actual o posterior");
			return;
		}
		try {
			cmlResumenService.enviarResumen(itemEnvia.getId(), getUsuario().getUsuario());
			info("RESU cerrado con exito");
			consultar();
		} catch (FindException e) {
			error(e.getDetail());
		} catch (UpdateException e) {
			error(e.getDetail());
		} catch (InsertException e) {
			error("Error al crear copia de siguiente resumen. Error: " + obtainException(e));
		}
	}

	public void generarReporte(CmlResumen itemProcesa, String idReporte) {
		Connection connection = prepararConexion();
		Map<String, Object> jasperParams = new HashMap<>();
		String ctxPath = getServletContext().getRealPath("/");
		try {
			EnumReporteRESU enumReporteRESU = EnumReporteRESU.obtenerInformacionReporteRESU(idReporte);
			jasperParams.put("p_no_cia", this.noCia);
			jasperParams.put("p_periodo", itemProcesa.getId().getPeriodo());
			jasperParams.put("p_anio", itemProcesa.getId().getAnio());
			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + getPathReporte(idReporte), jasperParams,
					connection);
			byte[] excelBytes = buildExcelByteArray(jasperPrint);
			String nombreReporte = enumReporteRESU.getNombreArchivo() + getCompania().getCodigoUaf()
					+ itemProcesa.getId().getAnio()
					+ StringUtils.rellenarConCaracteresALaIzquierda(itemProcesa.getId().getPeriodo(), "0", 2);
			sendExcelResponse(excelBytes, nombreReporte);
		} catch (JRException e) {
			error("Problemas al intentar llenar el reporte.");
			LOGGER.error(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				error("Problemas al intentar llenar el reporte.");
			}
		}
	}

	private String getPathReporte(String idReporte) {
		if (EnumReporteRESU.CLIENTES.getId().equals(idReporte)) {
			return PATH_REPORTE_CML_CLIENTES;
		}
		if (EnumReporteRESU.OPERACIONES.getId().equals(idReporte)) {
			return PATH_REPORTE_CML_OPERACIONES;
		}
		if (EnumReporteRESU.TRANSACCIONES.getId().equals(idReporte)) {
			return PATH_REPORTE_CML_TRANSACCIONES;
		}
		return PATH_REPORTE_CML_RESUMEN;
	}

	private byte[] buildExcelByteArray(JasperPrint jasperPrint) {
		JRXlsxExporter exporter = new JRXlsxExporter();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArrayOutputStream);
		exporter.setParameter(JRXlsAbstractExporterParameter.IS_DETECT_CELL_TYPE, CommonConstants.TRUE_VALUE);
		exporter.setParameter(JRXlsAbstractExporterParameter.IS_WHITE_PAGE_BACKGROUND, CommonConstants.FALSE_VALUE);
		exporter.setParameter(JRXlsAbstractExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS,
				CommonConstants.TRUE_VALUE);
		exporter.setParameter(JRXlsAbstractExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,
				CommonConstants.TRUE_VALUE);
		try {
			exporter.exportReport();
			return byteArrayOutputStream.toByteArray();
		} catch (JRException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException("No se pudo exportar el reporte con Jasper");
		}
	}

	private void sendExcelResponse(byte[] content, String nombreReporte) {
		StringBuilder header = new StringBuilder();
		header.append("attachment; filename=\"");
		header.append(nombreReporte);
		header.append(".xlsx");
		header.append("\"");

		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
		response.setHeader("Content-Disposition", header.toString());
		response.setContentType("application/vnd.ms-excel");
		response.setContentLength(content.length);
		try {
			response.getOutputStream().write(content, 0, content.length);
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException("No se puede generar el response del Excel.");
		}
	}

	private Connection prepararConexion() {
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		try {
			return utilServiceDelegate.getDataSource().getConnection();
		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
			throw new IllegalStateException("No se puede conectar a la base para crear el reporte.");
		}
	}

	public String getDescripcionMes(String mes) {
		return FechaUtils.getMesNombre(Integer.parseInt(mes));
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public List<CmlResumen> getResumens() {
		return resumens;
	}

	public void setResumens(List<CmlResumen> resumens) {
		this.resumens = resumens;
	}

}
