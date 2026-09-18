package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;

public class DeudaVigenteReporteDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sistemaCrediticio;
	private String valorPorVencer;
	private String valorVencido;
	private String carteraCastigada;
	private String totalDeuda;

	public DeudaVigenteReporteDTO() {
	}

	public DeudaVigenteReporteDTO(String sistemaCrediticio, String valorPorVencer, String valorVencido,
			String carteraCastigada, String totalDeuda) {
		this.sistemaCrediticio = sistemaCrediticio;
		this.valorPorVencer = valorPorVencer;
		this.valorVencido = valorVencido;
		this.carteraCastigada = carteraCastigada;
		this.totalDeuda = totalDeuda;
	}

	public String getSistemaCrediticio() {
		return sistemaCrediticio;
	}

	public void setSistemaCrediticio(String sistemaCrediticio) {
		this.sistemaCrediticio = sistemaCrediticio;
	}

	public String getValorPorVencer() {
		return valorPorVencer;
	}

	public void setValorPorVencer(String valorPorVencer) {
		this.valorPorVencer = valorPorVencer;
	}

	public String getValorVencido() {
		return valorVencido;
	}

	public void setValorVencido(String valorVencido) {
		this.valorVencido = valorVencido;
	}

	public String getCarteraCastigada() {
		return carteraCastigada;
	}

	public void setCarteraCastigada(String carteraCastigada) {
		this.carteraCastigada = carteraCastigada;
	}

	public String getTotalDeuda() {
		return totalDeuda;
	}

	public void setTotalDeuda(String totalDeuda) {
		this.totalDeuda = totalDeuda;
	}
}
