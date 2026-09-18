package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class TarjetaReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String fechaCorte;
	private String institucion;
	private String marca;
	private String cupo;
	private String saldo;
	private String vencido;
	private String diasMora;
	private String cuota;

	public TarjetaReporteDTO(String fechaCorte, String institucion, String marca, String cupo, String saldo,
			String vencido, String diasMora, String cuota) {
		this.fechaCorte = fechaCorte;
		this.institucion = institucion;
		this.marca = marca;
		this.cupo = cupo;
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

	public String getMarca() {
		return marca;
	}

	public String getCupo() {
		return cupo;
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
