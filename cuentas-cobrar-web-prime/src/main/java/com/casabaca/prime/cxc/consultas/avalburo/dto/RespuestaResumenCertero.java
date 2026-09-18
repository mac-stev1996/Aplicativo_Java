package com.casabaca.prime.cxc.consultas.avalburo.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.casabaca.rest.wsAvalBuro.entidad.DatosGeneralesEmpresa;
import com.casabaca.rest.wsAvalBuro.entidad.FactoresScore;
import com.casabaca.rest.wsAvalBuro.entidad.IdentificacionTitular;
import com.casabaca.rest.wsAvalBuro.entidad.ScoreEmpresa;
import com.casabaca.rest.wsAvalBuro.entidad.ScoreFinanciero;

public class RespuestaResumenCertero implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private IdentificacionTitular identificacionTitular;
	private DatosGeneralesEmpresa datosGeneralesEmpresa;
	private ScoreFinanciero scoreFinanciero;
	private ScoreEmpresa scoreEmpresa;
	private List<FactoresScore> lstFactoresScore;
	private Saldos saldosCliente;
	
	public IdentificacionTitular getIdentificacionTitular() {
		return identificacionTitular;
	}
	public void setIdentificacionTitular(IdentificacionTitular identificacionTitular) {
		this.identificacionTitular = identificacionTitular;
	}
	public DatosGeneralesEmpresa getDatosGeneralesEmpresa() {
		return datosGeneralesEmpresa;
	}
	public void setDatosGeneralesEmpresa(DatosGeneralesEmpresa datosGeneralesEmpresa) {
		this.datosGeneralesEmpresa = datosGeneralesEmpresa;
	}
	public ScoreFinanciero getScoreFinanciero() {
		return scoreFinanciero;
	}
	public void setScoreFinanciero(ScoreFinanciero scoreFinanciero) {
		this.scoreFinanciero = scoreFinanciero;
	}
	public ScoreEmpresa getScoreEmpresa() {
		return scoreEmpresa;
	}
	public void setScoreEmpresa(ScoreEmpresa scoreEmpresa) {
		this.scoreEmpresa = scoreEmpresa;
	}
	public List<FactoresScore> getLstFactoresScore() {
		return lstFactoresScore;
	}
	public void setLstFactoresScore(List<FactoresScore> lstFactoresScore) {
		this.lstFactoresScore = lstFactoresScore;
	}
	public Saldos getSaldosCliente() {
		return saldosCliente;
	}
	public void setSaldosCliente(Saldos saldosCliente) {
		this.saldosCliente = saldosCliente;
	}
	
}
