package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class IndicadoresDeudaReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String saldoPromedio;
	private String saldoPromedioTarjetas;
	private String maximoMontoDeuda;
	private String peorEdadVencido;
	private String mayorSaldoVencido;
	private String fechaUltimoVencido;

	public IndicadoresDeudaReporteDTO(String saldoPromedio, String saldoPromedioTarjetas, String maximoMontoDeuda,
			String peorEdadVencido, String mayorSaldoVencido, String fechaUltimoVencido) {
		this.saldoPromedio = saldoPromedio;
		this.saldoPromedioTarjetas = saldoPromedioTarjetas;
		this.maximoMontoDeuda = maximoMontoDeuda;
		this.peorEdadVencido = peorEdadVencido;
		this.mayorSaldoVencido = mayorSaldoVencido;
		this.fechaUltimoVencido = fechaUltimoVencido;
	}

	public String getSaldoPromedio() {
		return saldoPromedio;
	}

	public String getSaldoPromedioTarjetas() {
		return saldoPromedioTarjetas;
	}

	public String getMaximoMontoDeuda() {
		return maximoMontoDeuda;
	}

	public String getPeorEdadVencido() {
		return peorEdadVencido;
	}

	public String getMayorSaldoVencido() {
		return mayorSaldoVencido;
	}

	public String getFechaUltimoVencido() {
		return fechaUltimoVencido;
	}
}
