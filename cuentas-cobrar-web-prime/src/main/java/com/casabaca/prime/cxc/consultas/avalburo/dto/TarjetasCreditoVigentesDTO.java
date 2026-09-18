package com.casabaca.prime.cxc.consultas.avalburo.dto;

public class TarjetasCreditoVigentesDTO {

	private String formaPagoDescripcion;
    private String fechaEmisión;
    private String fechaVencimiento;
    private String fechaCancelacion;		    
    private String estadoOperacionDescripcion;		    
    private double saldoTotal;
    private double capitalxVencerTotal;
    private double saldoVencido;
    private double valorNoDevengaInteresTotal;
    private double valorDemandaJudicial;
    private double carteraCastigada;
    private int diasMorosidad;
    private double valorPagado;
    private double valorMinimoPagar;	    	    
    
	public TarjetasCreditoVigentesDTO(String formaPagoDescripcion, String fechaEmisión, String fechaVencimiento,
			String fechaCancelacion, String estadoOperacionDescripcion, double saldoTotal,
			double capitalxVencerTotal, double saldoVencido, double valorNoDevengaInteresTotal,
			double valorDemandaJudicial, double carteraCastigada, int diasMorosidad, double valorPagado,
			double valorMinimoPagar) {
		super();
		this.formaPagoDescripcion = formaPagoDescripcion;
		this.fechaEmisión = fechaEmisión;
		this.fechaVencimiento = fechaVencimiento;
		this.fechaCancelacion = fechaCancelacion;
		this.estadoOperacionDescripcion = estadoOperacionDescripcion;
		this.saldoTotal = saldoTotal;
		this.capitalxVencerTotal = capitalxVencerTotal;
		this.saldoVencido = saldoVencido;
		this.valorNoDevengaInteresTotal = valorNoDevengaInteresTotal;
		this.valorDemandaJudicial = valorDemandaJudicial;
		this.carteraCastigada = carteraCastigada;
		this.diasMorosidad = diasMorosidad;
		this.valorPagado = valorPagado;
		this.valorMinimoPagar = valorMinimoPagar;
	}
	public String getFormaPagoDescripcion() {
		return formaPagoDescripcion;
	}
	public void setFormaPagoDescripcion(String formaPagoDescripcion) {
		this.formaPagoDescripcion = formaPagoDescripcion;
	}
	public String getFechaEmisión() {
		return fechaEmisión;
	}
	public void setFechaEmisión(String fechaEmisión) {
		this.fechaEmisión = fechaEmisión;
	}
	public String getFechaVencimiento() {
		return fechaVencimiento;
	}
	public void setFechaVencimiento(String fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}
	public String getFechaCancelacion() {
		return fechaCancelacion;
	}
	public void setFechaCancelacion(String fechaCancelacion) {
		this.fechaCancelacion = fechaCancelacion;
	}
	public String getEstadoOperacionDescripcion() {
		return estadoOperacionDescripcion;
	}
	public void setEstadoOperacionDescripcion(String estadoOperacionDescripcion) {
		this.estadoOperacionDescripcion = estadoOperacionDescripcion;
	}
	public double getSaldoTotal() {
		return saldoTotal;
	}
	public void setSaldoTotal(double saldoTotal) {
		this.saldoTotal = saldoTotal;
	}
	public double getCapitalxVencerTotal() {
		return capitalxVencerTotal;
	}
	public void setCapitalxVencerTotal(double capitalxVencerTotal) {
		this.capitalxVencerTotal = capitalxVencerTotal;
	}
	public double getSaldoVencido() {
		return saldoVencido;
	}
	public void setSaldoVencido(double saldoVencido) {
		this.saldoVencido = saldoVencido;
	}
	public double getValorNoDevengaInteresTotal() {
		return valorNoDevengaInteresTotal;
	}
	public void setValorNoDevengaInteresTotal(double valorNoDevengaInteresTotal) {
		this.valorNoDevengaInteresTotal = valorNoDevengaInteresTotal;
	}
	public double getValorDemandaJudicial() {
		return valorDemandaJudicial;
	}
	public void setValorDemandaJudicial(double valorDemandaJudicial) {
		this.valorDemandaJudicial = valorDemandaJudicial;
	}
	public double getCarteraCastigada() {
		return carteraCastigada;
	}
	public void setCarteraCastigada(double carteraCastigada) {
		this.carteraCastigada = carteraCastigada;
	}
	public int getDiasMorosidad() {
		return diasMorosidad;
	}
	public void setDiasMorosidad(int diasMorosidad) {
		this.diasMorosidad = diasMorosidad;
	}
	public double getValorPagado() {
		return valorPagado;
	}
	public void setValorPagado(double valorPagado) {
		this.valorPagado = valorPagado;
	}
	public double getValorMinimoPagar() {
		return valorMinimoPagar;
	}
	public void setValorMinimoPagar(double valorMinimoPagar) {
		this.valorMinimoPagar = valorMinimoPagar;
	}
}
