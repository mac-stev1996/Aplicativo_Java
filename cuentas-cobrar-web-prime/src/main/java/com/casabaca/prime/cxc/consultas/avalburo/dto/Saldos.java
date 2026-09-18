package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class Saldos implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private BigDecimal valorPorVencer;
	private BigDecimal valorVencido;
	private BigDecimal valorNoDevengaIntereses;
	private BigDecimal valorDemandaJudicial;
	private BigDecimal valorCarteraCastida;
	private BigDecimal valorTotalDeuda;
	public BigDecimal getValorPorVencer() {
		return valorPorVencer;
	}
	public void setValorPorVencer(BigDecimal valorPorVencer) {
		this.valorPorVencer = valorPorVencer;
	}
	public BigDecimal getValorVencido() {
		return valorVencido;
	}
	public void setValorVencido(BigDecimal valorVencido) {
		this.valorVencido = valorVencido;
	}
	public BigDecimal getValorNoDevengaIntereses() {
		return valorNoDevengaIntereses;
	}
	public void setValorNoDevengaIntereses(BigDecimal valorNoDevengaIntereses) {
		this.valorNoDevengaIntereses = valorNoDevengaIntereses;
	}
	public BigDecimal getValorDemandaJudicial() {
		return valorDemandaJudicial;
	}
	public void setValorDemandaJudicial(BigDecimal valorDemandaJudicial) {
		this.valorDemandaJudicial = valorDemandaJudicial;
	}
	public BigDecimal getValorCarteraCastida() {
		return valorCarteraCastida;
	}
	public void setValorCarteraCastida(BigDecimal valorCarteraCastida) {
		this.valorCarteraCastida = valorCarteraCastida;
	}
	public BigDecimal getValorTotalDeuda() {
		return valorTotalDeuda;
	}
	public void setValorTotalDeuda(BigDecimal valorTotalDeuda) {
		this.valorTotalDeuda = valorTotalDeuda;
	}
	
	

}
