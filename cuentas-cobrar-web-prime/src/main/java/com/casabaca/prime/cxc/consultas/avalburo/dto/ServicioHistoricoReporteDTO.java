package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class ServicioHistoricoReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fechaCorte;
	private String institucion;
	private String tipoServicio;
	private String contrato;
	private String estado;
	private String cuota;
	private String deuda;
	private String vencido;
	private String diasVencido;

	public ServicioHistoricoReporteDTO(String fechaCorte, String institucion, String tipoServicio, String contrato,
			String estado, String cuota, String deuda, String vencido, String diasVencido) {
		this.fechaCorte = fechaCorte;
		this.institucion = institucion;
		this.tipoServicio = tipoServicio;
		this.contrato = contrato;
		this.estado = estado;
		this.cuota = cuota;
		this.deuda = deuda;
		this.vencido = vencido;
		this.diasVencido = diasVencido;
	}

	public String getFechaCorte() {
		return fechaCorte;
	}

	public String getInstitucion() {
		return institucion;
	}

	public String getTipoServicio() {
		return tipoServicio;
	}

	public String getContrato() {
		return contrato;
	}

	public String getEstado() {
		return estado;
	}

	public String getCuota() {
		return cuota;
	}

	public String getDeuda() {
		return deuda;
	}

	public String getVencido() {
		return vencido;
	}

	public String getDiasVencido() {
		return diasVencido;
	}
}
