/*
 * Copyright 2012 TOYOTA CASABACA - ECUADOR 
 * Todos los derechos reservados
 */
package com.casabaca.compras.web.proceso.datamanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import com.casabaca.compras.ejb.modelo.CmpVale;
import com.casabaca.s3s.ejb.model.UsuarioSis;


/**
 * <b> Clase para manejar un CmpValeDataManager. </b>
 * 
 * @author dpicuasi
 * @version $Revision: 1.0 $
 * <p>
 *    [$Author: dpicuasi $, $Date: 2018/12/20 $]
 * </p>
 */

@SessionScoped
@ManagedBean(name = "cmpValeDataManager")
public class CmpValeDataManager{

	private CmpVale cmpVale;
	private CmpVale cmpValeEditar;
	private List<CmpVale> listaCmpVale;
	private List<SelectItem> agencias;
	private List<SelectItem> companias;
	private List<SelectItem> proveedor;
	private List<SelectItem> proveedoresCom;
	private List<SelectItem> area;
	private List<SelectItem> subArea;
	private List<UsuarioSis> usuariosModal;
	private String varSeleccionado;
	
	/**Variables***/
	private Date fechaDesde;	
	private Date fechaHasta;
	private String noCia;
	private String agencia;
	private String tipoVale;
	private String noProve;
	private String horaTmp;
	private String minutoTmp;
	private HashMap<String, String> hashProveedores = new HashMap<String, String>();
	
	public CmpValeDataManager() {
		cmpVale=new CmpVale();
		cmpValeEditar=new CmpVale();
		listaCmpVale=new ArrayList<CmpVale>();
	}

	public CmpValeDataManager(CmpVale cmpVale, CmpVale cmpValeEditar, List<CmpVale> listaCmpVale) {
		super();
		this.cmpVale = cmpVale;
		this.cmpValeEditar = cmpValeEditar;
		this.listaCmpVale = listaCmpVale;
	}
	
	public CmpVale getCmpVale() {
		return cmpVale;
	}
	public void setCmpVale(CmpVale cmpVale) {
		this.cmpVale = cmpVale;
	}
	public CmpVale getCmpValeEditar() {
		return cmpValeEditar;
	}
	public void setCmpValeEditar(CmpVale cmpValeEditar) {
		this.cmpValeEditar = cmpValeEditar;
	}
	public List<CmpVale> getListaCmpVale() {
		return listaCmpVale;
	}
	public void setListaCmpVale(List<CmpVale> listaCmpVale) {
		this.listaCmpVale = listaCmpVale;
	}
	public List<SelectItem> getAgencias() {
		return agencias;
	}
	public void setAgencias(List<SelectItem> agencias) {
		this.agencias = agencias;
	}

	public List<SelectItem> getCompanias() {
		return companias;
	}

	public void setCompanias(List<SelectItem> companias) {
		this.companias = companias;
	}

	public List<SelectItem> getProveedor() {
		return proveedor;
	}

	public void setProveedor(List<SelectItem> proveedor) {
		this.proveedor = proveedor;
	}

	public List<UsuarioSis> getUsuariosModal() {
		return usuariosModal;
	}

	public void setUsuariosModal(List<UsuarioSis> usuariosModal) {
		this.usuariosModal = usuariosModal;
	}

	public String getVarSeleccionado() {
		return varSeleccionado;
	}

	public void setVarSeleccionado(String varSeleccionado) {
		this.varSeleccionado = varSeleccionado;
	}

	public Date getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getAgencia() {
		return agencia;
	}

	public void setAgencia(String agencia) {
		this.agencia = agencia;
	}

	public String getTipoVale() {
		return tipoVale;
	}

	public void setTipoVale(String tipoVale) {
		this.tipoVale = tipoVale;
	}

	public String getNoProve() {
		return noProve;
	}

	public void setNoProve(String noProve) {
		this.noProve = noProve;
	}

	public List<SelectItem> getProveedoresCom() {
		return proveedoresCom;
	}

	public void setProveedoresCom(List<SelectItem> proveedoresCom) {
		this.proveedoresCom = proveedoresCom;
	}

	public String getHoraTmp() {
		return horaTmp;
	}

	public void setHoraTmp(String horaTmp) {
		this.horaTmp = horaTmp;
	}

	public String getMinutoTmp() {
		return minutoTmp;
	}

	public void setMinutoTmp(String minutoTmp) {
		this.minutoTmp = minutoTmp;
	}

	public HashMap<String, String> getHashProveedores() {
		return hashProveedores;
	}

	public void setHashProveedores(HashMap<String, String> hashProveedores) {
		this.hashProveedores = hashProveedores;
	}

	public List<SelectItem> getArea() {
		return area;
	}

	public void setArea(List<SelectItem> area) {
		this.area = area;
	}

	public List<SelectItem> getSubArea() {
		return subArea;
	}

	public void setSubArea(List<SelectItem> subArea) {
		this.subArea = subArea;
	}
		
}
