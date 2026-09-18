package com.casabaca.prime.cxc.consultas.avalburo.dto;

public class SaldoVencidoDto {
	
	private String formaPagoDescripcion;
	private double capitalVencido1a30;
    private double capitalVencido31a90;
    private double capitalVencido91a180;
    private double capitalVencido181a360;
    private double capitalVencidoMas360;
    private double capitalVencido181a270;
    private double capitalVencidoMas270;
    private double interesVencido1a30;
    private double interesVencido31a60;
    private double interesVencido61a90;
    private double interesVencido91a180;
    private double interesVencido181a270;
    private double interesVencidoMas270;
    private double interesSobreMora;
    private double totalCostoOperativoVencido;
    
	public SaldoVencidoDto(String formaPagoDescripcion, double capitalVencido1a30, double capitalVencido31a90,
			double capitalVencido91a180, double capitalVencido181a360, double capitalVencidoMas360,
			double capitalVencido181a270, double capitalVencidoMas270, double interesVencido1a30,
			double interesVencido31a60, double interesVencido61a90, double interesVencido91a180,
			double interesVencido181a270, double interesVencidoMas270, double interesSobreMora,
			double totalCostoOperativoVencido) {
		super();
		this.formaPagoDescripcion = formaPagoDescripcion;
		this.capitalVencido1a30 = capitalVencido1a30;
		this.capitalVencido31a90 = capitalVencido31a90;
		this.capitalVencido91a180 = capitalVencido91a180;
		this.capitalVencido181a360 = capitalVencido181a360;
		this.capitalVencidoMas360 = capitalVencidoMas360;
		this.capitalVencido181a270 = capitalVencido181a270;
		this.capitalVencidoMas270 = capitalVencidoMas270;
		this.interesVencido1a30 = interesVencido1a30;
		this.interesVencido31a60 = interesVencido31a60;
		this.interesVencido61a90 = interesVencido61a90;
		this.interesVencido91a180 = interesVencido91a180;
		this.interesVencido181a270 = interesVencido181a270;
		this.interesVencidoMas270 = interesVencidoMas270;
		this.interesSobreMora = interesSobreMora;
		this.totalCostoOperativoVencido = totalCostoOperativoVencido;
	}

	public String getFormaPagoDescripcion() {
		return formaPagoDescripcion;
	}

	public void setFormaPagoDescripcion(String formaPagoDescripcion) {
		this.formaPagoDescripcion = formaPagoDescripcion;
	}

	public double getCapitalVencido1a30() {
		return capitalVencido1a30;
	}

	public void setCapitalVencido1a30(double capitalVencido1a30) {
		this.capitalVencido1a30 = capitalVencido1a30;
	}

	public double getCapitalVencido31a90() {
		return capitalVencido31a90;
	}

	public void setCapitalVencido31a90(double capitalVencido31a90) {
		this.capitalVencido31a90 = capitalVencido31a90;
	}

	public double getCapitalVencido91a180() {
		return capitalVencido91a180;
	}

	public void setCapitalVencido91a180(double capitalVencido91a180) {
		this.capitalVencido91a180 = capitalVencido91a180;
	}

	public double getCapitalVencido181a360() {
		return capitalVencido181a360;
	}

	public void setCapitalVencido181a360(double capitalVencido181a360) {
		this.capitalVencido181a360 = capitalVencido181a360;
	}

	public double getCapitalVencidoMas360() {
		return capitalVencidoMas360;
	}

	public void setCapitalVencidoMas360(double capitalVencidoMas360) {
		this.capitalVencidoMas360 = capitalVencidoMas360;
	}

	public double getCapitalVencido181a270() {
		return capitalVencido181a270;
	}

	public void setCapitalVencido181a270(double capitalVencido181a270) {
		this.capitalVencido181a270 = capitalVencido181a270;
	}

	public double getCapitalVencidoMas270() {
		return capitalVencidoMas270;
	}

	public void setCapitalVencidoMas270(double capitalVencidoMas270) {
		this.capitalVencidoMas270 = capitalVencidoMas270;
	}

	public double getInteresVencido1a30() {
		return interesVencido1a30;
	}

	public void setInteresVencido1a30(double interesVencido1a30) {
		this.interesVencido1a30 = interesVencido1a30;
	}

	public double getInteresVencido31a60() {
		return interesVencido31a60;
	}

	public void setInteresVencido31a60(double interesVencido31a60) {
		this.interesVencido31a60 = interesVencido31a60;
	}

	public double getInteresVencido61a90() {
		return interesVencido61a90;
	}

	public void setInteresVencido61a90(double interesVencido61a90) {
		this.interesVencido61a90 = interesVencido61a90;
	}

	public double getInteresVencido91a180() {
		return interesVencido91a180;
	}

	public void setInteresVencido91a180(double interesVencido91a180) {
		this.interesVencido91a180 = interesVencido91a180;
	}

	public double getInteresVencido181a270() {
		return interesVencido181a270;
	}

	public void setInteresVencido181a270(double interesVencido181a270) {
		this.interesVencido181a270 = interesVencido181a270;
	}

	public double getInteresVencidoMas270() {
		return interesVencidoMas270;
	}

	public void setInteresVencidoMas270(double interesVencidoMas270) {
		this.interesVencidoMas270 = interesVencidoMas270;
	}

	public double getInteresSobreMora() {
		return interesSobreMora;
	}

	public void setInteresSobreMora(double interesSobreMora) {
		this.interesSobreMora = interesSobreMora;
	}

	public double getTotalCostoOperativoVencido() {
		return totalCostoOperativoVencido;
	}

	public void setTotalCostoOperativoVencido(double totalCostoOperativoVencido) {
		this.totalCostoOperativoVencido = totalCostoOperativoVencido;
	}
    

}
