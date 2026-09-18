package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class OperacionCreditoReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fechaCorte;
	private String institucion;
	private String tipoCredito;
	private String numeroOperacion;
	private String saldo;
	private String vencido;
	private String diasMora;
	private String cuota;

	public OperacionCreditoReporteDTO(String fechaCorte, String institucion, String tipoCredito, String numeroOperacion,
			String saldo, String vencido, String diasMora, String cuota) {
		this.fechaCorte = fechaCorte;
		this.institucion = institucion;
		this.tipoCredito = tipoCredito;
		this.numeroOperacion = numeroOperacion;
		this.saldo = saldo;
		this.vencido = vencido;
		this.diasMora = diasMora;
		this.cuota = cuota;
	}

	public String getFechaCorte() {
		return fechaCorte;
	}

	public String getInstitucion() {
		return institucion;
	}

	public String getTipoCredito() {
		return tipoCredito;
	}

	public String getNumeroOperacion() {
		return numeroOperacion;
	}

	public String getSaldo() {
		return saldo;
	}

	public String getVencido() {
		return vencido;
	}

	public String getDiasMora() {
		return diasMora;
	}

	public String getCuota() {
		return cuota;
	}
}
