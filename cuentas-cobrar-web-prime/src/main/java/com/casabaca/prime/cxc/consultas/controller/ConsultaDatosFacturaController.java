/**
 * 
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.CommonUtils;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.DatosFacturaDto;
import com.casabaca.cxc.ejb.dto.HistoricoDocEntradaDto;
import com.casabaca.cxc.ejb.dto.HistoricoDocumentoDto;
import com.casabaca.cxc.ejb.servicio.HistoricoDocumentosServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class ConsultaDatosFacturaController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1285044667250562651L;

	@EJB(lookup = NombreJNDI.JNDI_CXC + "HistoricoDocumentosServiceBean")
	private HistoricoDocumentosServiceLocal historicoDocumentosService;

	private DatosFacturaDto datosFacturaDto;
	private String noCia;
	private HistoricoDocEntradaDto historicoDocEntradaDto;

	public ConsultaDatosFacturaController() {
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
		cargarDatosFac();
	}

	private void cargarDatosFac() {
		HistoricoDocumentoDto documentoDto = new HistoricoDocumentoDto();
		documentoDto.setNoCia(this.noCia);
		documentoDto.setAgencia(getRequestParameter("agencia"));
		documentoDto.setSerieFisico(getRequestParameter("serieFisico"));
		documentoDto.setNoFisico(getRequestParameter("noFisico"));
		documentoDto.setCantProrroga((getRequestParameter("cantProrrogas") != null ?Integer.parseInt(getRequestParameter("cantProrrogas")):1));
		documentoDto.setNoCliente(historicoDocEntradaDto.getNoCliente());
		documentoDto.setFecha(FechaUtils.convertirStrFecha(getRequestParameter("fecha"), FechaUtils.patronDiaMesAnio));
		datosFacturaDto = historicoDocumentosService.consultarDatosFactura(documentoDto);
	}

	public Double getTotalMontoOriginal() {
		Double totalMontoOriginal = 0D;
		if (datosFacturaDto != null && !datosFacturaDto.getDetalleFacturaDtos().isEmpty()) {
			totalMontoOriginal = datosFacturaDto.getDetalleFacturaDtos().stream()
					.mapToDouble(p -> p.getMontoOriginal() != null ? p.getMontoOriginal() : 0D).sum();
		}
		return CommonUtils.redondear(totalMontoOriginal, 2);
	}

	public Double getTotalSaldo() {
		Double totalMontoOriginal = 0D;
		if (datosFacturaDto != null && !datosFacturaDto.getDetalleFacturaDtos().isEmpty()) {
			totalMontoOriginal = datosFacturaDto.getDetalleFacturaDtos().stream()
					.mapToDouble(p -> p.getSaldo() != null ? p.getSaldo() : 0D).sum();
		}
		return CommonUtils.redondear(totalMontoOriginal, 2);
	}

	public Double getTotalFinanciamiento() {
		Double saldoAFacturar = datosFacturaDto != null && datosFacturaDto.getSaldoAFacturar() != null
				? datosFacturaDto.getSaldoAFacturar()
				: 0D;
		Double saldoAFacturarAcc = datosFacturaDto != null && datosFacturaDto.getSaldoFacturaAcc() != null
				? datosFacturaDto.getSaldoFacturaAcc()
				: 0D;
		return CommonUtils.redondear(saldoAFacturar + saldoAFacturarAcc, 2);
	}

	public Double getTotalIngresos() {
		Double valorIngresos = datosFacturaDto != null && datosFacturaDto.getIngresos() != null
				? datosFacturaDto.getIngresos()
				: 0D;
		Double valorIngresosAcc = datosFacturaDto != null && datosFacturaDto.getIngresosAcc() != null
				? datosFacturaDto.getIngresosAcc()
				: 0D;
		return CommonUtils.redondear(valorIngresos + valorIngresosAcc, 2);
	}

	public String regresar() {
		StringBuilder parametros = new StringBuilder();
		parametros.append("&lineaNeg=").append(historicoDocEntradaDto.getLineaNegocio());
		parametros.append("&tipoDoc=").append(historicoDocEntradaDto.getTipoDoc());
		parametros.append("&anulado=").append(historicoDocEntradaDto.getAnulado());
		parametros.append("&documento=").append(historicoDocEntradaDto.getDocumento());
		parametros.append("&fechaIni=").append(historicoDocEntradaDto.getFechaInicio() != null
				? FechaUtils.formatearFecha(historicoDocEntradaDto.getFechaInicio(), FechaUtils.patronDiaMesAnio)
				: "");
		parametros.append("&fechaFin=")
				.append(historicoDocEntradaDto.getFechaFin() != null
						? FechaUtils.formatearFecha(historicoDocEntradaDto.getFechaFin(), FechaUtils.patronDiaMesAnio)
						: "");
		parametros.append("&noCliente=").append(historicoDocEntradaDto.getNoCliente());
		parametros.append(historicoDocEntradaDto.isAplicaDesbloqueo() ? "&APLDESB=S":"");
		return "consultaHistDocumentoClientes?faces-redirect=true" + parametros.toString();
	}

	public DatosFacturaDto getDatosFacturaDto() {
		return datosFacturaDto;
	}

	public void setDatosFacturaDto(DatosFacturaDto datosFacturaDto) {
		this.datosFacturaDto = datosFacturaDto;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public HistoricoDocEntradaDto getHistoricoDocEntradaDto() {
		return historicoDocEntradaDto;
	}

	public void setHistoricoDocEntradaDto(HistoricoDocEntradaDto historicoDocEntradaDto) {
		this.historicoDocEntradaDto = historicoDocEntradaDto;
	}

}
