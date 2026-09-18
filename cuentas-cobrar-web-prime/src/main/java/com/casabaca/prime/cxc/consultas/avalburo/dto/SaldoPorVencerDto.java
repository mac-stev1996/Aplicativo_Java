package com.casabaca.prime.cxc.consultas.avalburo.dto;

public class SaldoPorVencerDto {
	
	private String formaPagoDescripcion;
	private double capitalxVencer1a30;    
    private double capitalxVencer31a90;
    private double capitalxVencer91a180;
    private double capitalxVencer181a360;
    private double capitalxVencerMas360;
	
    public SaldoPorVencerDto(String formaPagoDescripcion, double capitalxVencer1a30, double capitalxVencer31a90, double capitalxVencer91a180,
			double capitalxVencer181a360, double capitalxVencerMas360) {
		super();
		this.formaPagoDescripcion = formaPagoDescripcion;
		this.capitalxVencer1a30 = capitalxVencer1a30;
		this.capitalxVencer31a90 = capitalxVencer31a90;
		this.capitalxVencer91a180 = capitalxVencer91a180;
		this.capitalxVencer181a360 = capitalxVencer181a360;
		this.capitalxVencerMas360 = capitalxVencerMas360;
	}

	public double getCapitalxVencer1a30() {
		return capitalxVencer1a30;
	}

	public void setCapitalxVencer1a30(double capitalxVencer1a30) {
		this.capitalxVencer1a30 = capitalxVencer1a30;
	}

	public double getCapitalxVencer31a90() {
		return capitalxVencer31a90;
	}

	public void setCapitalxVencer31a90(double capitalxVencer31a90) {
		this.capitalxVencer31a90 = capitalxVencer31a90;
	}

	public double getCapitalxVencer91a180() {
		return capitalxVencer91a180;
	}

	public void setCapitalxVencer91a180(double capitalxVencer91a180) {
		this.capitalxVencer91a180 = capitalxVencer91a180;
	}

	public double getCapitalxVencer181a360() {
		return capitalxVencer181a360;
	}

	public void setCapitalxVencer181a360(double capitalxVencer181a360) {
		this.capitalxVencer181a360 = capitalxVencer181a360;
	}

	public double getCapitalxVencerMas360() {
		return capitalxVencerMas360;
	}

	public void setCapitalxVencerMas360(double capitalxVencerMas360) {
		this.capitalxVencerMas360 = capitalxVencerMas360;
	}

	public String getFormaPagoDescripcion() {
		return formaPagoDescripcion;
	}

	public void setFormaPagoDescripcion(String formaPagoDescripcion) {
		this.formaPagoDescripcion = formaPagoDescripcion;
	}
    
    
}
