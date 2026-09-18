package com.casabaca.prime.cxc.consultas.avalburo.dto;

public class SaldoNoDevengaInteresDto {
	
	private String formaPagoDescripcion;
	private double valorNoDevengaInteres1a30;
    private double valorNoDevengaInteres31a90;
    private double valorNoDevengaInteres91a180;
    private double valorNoDevengaInteres181a360;
    private double valorNoDevengaInteresMas360;
	
    public SaldoNoDevengaInteresDto(String formaPagoDescripcion, double valorNoDevengaInteres1a30,
			double valorNoDevengaInteres31a90, double valorNoDevengaInteres91a180, double valorNoDevengaInteres181a360,
			double valorNoDevengaInteresMas360) {
		super();
		this.formaPagoDescripcion = formaPagoDescripcion;
		this.valorNoDevengaInteres1a30 = valorNoDevengaInteres1a30;
		this.valorNoDevengaInteres31a90 = valorNoDevengaInteres31a90;
		this.valorNoDevengaInteres91a180 = valorNoDevengaInteres91a180;
		this.valorNoDevengaInteres181a360 = valorNoDevengaInteres181a360;
		this.valorNoDevengaInteresMas360 = valorNoDevengaInteresMas360;
	}

	public String getFormaPagoDescripcion() {
		return formaPagoDescripcion;
	}

	public void setFormaPagoDescripcion(String formaPagoDescripcion) {
		this.formaPagoDescripcion = formaPagoDescripcion;
	}

	public double getValorNoDevengaInteres1a30() {
		return valorNoDevengaInteres1a30;
	}

	public void setValorNoDevengaInteres1a30(double valorNoDevengaInteres1a30) {
		this.valorNoDevengaInteres1a30 = valorNoDevengaInteres1a30;
	}

	public double getValorNoDevengaInteres31a90() {
		return valorNoDevengaInteres31a90;
	}

	public void setValorNoDevengaInteres31a90(double valorNoDevengaInteres31a90) {
		this.valorNoDevengaInteres31a90 = valorNoDevengaInteres31a90;
	}

	public double getValorNoDevengaInteres91a180() {
		return valorNoDevengaInteres91a180;
	}

	public void setValorNoDevengaInteres91a180(double valorNoDevengaInteres91a180) {
		this.valorNoDevengaInteres91a180 = valorNoDevengaInteres91a180;
	}

	public double getValorNoDevengaInteres181a360() {
		return valorNoDevengaInteres181a360;
	}

	public void setValorNoDevengaInteres181a360(double valorNoDevengaInteres181a360) {
		this.valorNoDevengaInteres181a360 = valorNoDevengaInteres181a360;
	}

	public double getValorNoDevengaInteresMas360() {
		return valorNoDevengaInteresMas360;
	}

	public void setValorNoDevengaInteresMas360(double valorNoDevengaInteresMas360) {
		this.valorNoDevengaInteresMas360 = valorNoDevengaInteresMas360;
	}
        

}
