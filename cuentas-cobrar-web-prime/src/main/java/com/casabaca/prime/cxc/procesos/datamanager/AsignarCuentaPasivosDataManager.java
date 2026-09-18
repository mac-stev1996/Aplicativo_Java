/* 
 * AsignarCuentaPasivosDataManager.java 
 * 30 mar. 2020
 * Copyright 2020 CASABACA.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.datamanager;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.dto.DeudaClientesDto;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ConfirmarDeposito;
import com.casabaca.common.ejb.model.LineaNegocio;

/**
 * <b> Clase con los atributos necesario para realizar el proceso de 
 * <br> creacion de diarios A con depositos de meses anteriores
 * . </b>
 * 
 * @author Jorge Lucas
 * @version $1.0$
 */
@ViewScoped
@ManagedBean(name = "asignarCuentaPasivosDM")
public class AsignarCuentaPasivosDataManager {
	
	private List<Cliente> listaClientes;
	
	private Cliente clienteSeleccionado;
	
	private Long codigoCliente;
	
	private String cedulaCliente;
	
	private String nombreCliente;
	
	private String nombreComercial;
	
	private Boolean esEmisorTarjeta = Boolean.FALSE;
	
	private List<ConfirmarDeposito> listaDepositosCliente;
	
	private List<ConfirmarDeposito> depositosSelecionados;
	
	private List<DeudaClientesDto> listaDeudasCliente;
	
	private List<DeudaClientesDto> deudasSeleccionadas;
	
	private List<LineaNegocio> lineaNegocioList;
	
	private String lineaSeleccionada;
	
	private Double saldoDisponible = 0D;
	
	private Double saldoDeudas = 0D;
	
	private Double valorAnticipo = 0D;
	
	private Boolean habilitarBoton = Boolean.TRUE;
	
	private Boolean habilitarComboLinea = Boolean.TRUE;
	
	/**
	 * @return the listaClientes
	 */
	public List<Cliente> getListaClientes() {
		return listaClientes;
	}

	/**
	 * @param listaClientes the listaClientes to set
	 */
	public void setListaClientes(List<Cliente> listaClientes) {
		this.listaClientes = listaClientes;
	}

	/**
	 * @return the clienteSeleccionado
	 */
	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}

	/**
	 * @param clienteSeleccionado the clienteSeleccionado to set
	 */
	public void setClienteSeleccionado(Cliente clienteSeleccionado) {
		this.clienteSeleccionado = clienteSeleccionado;
	}

	/**
	 * @return the codigoCliente
	 */
	public Long getCodigoCliente() {
		return codigoCliente;
	}

	/**
	 * @param codigoCliente the codigoCliente to set
	 */
	public void setCodigoCliente(Long codigoCliente) {
		this.codigoCliente = codigoCliente;
	}

	/**
	 * @return the cedulaCliente
	 */
	public String getCedulaCliente() {
		return cedulaCliente;
	}

	/**
	 * @param cedulaCliente the cedulaCliente to set
	 */
	public void setCedulaCliente(String cedulaCliente) {
		this.cedulaCliente = cedulaCliente;
	}

	/**
	 * @return the nombreCliente
	 */
	public String getNombreCliente() {
		return nombreCliente;
	}

	/**
	 * @param nombreCliente the nombreCliente to set
	 */
	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	/**
	 * @return the nombreComercial
	 */
	public String getNombreComercial() {
		return nombreComercial;
	}

	/**
	 * @param nombreComercial the nombreComercial to set
	 */
	public void setNombreComercial(String nombreComercial) {
		this.nombreComercial = nombreComercial;
	}
	
	/**
	 * @return the esEmisorTarjeta
	 */
	public Boolean getEsEmisorTarjeta() {
		return esEmisorTarjeta;
	}

	/**
	 * @param esEmisorTarjeta the esEmisorTarjeta to set
	 */
	public void setEsEmisorTarjeta(Boolean esEmisorTarjeta) {
		this.esEmisorTarjeta = esEmisorTarjeta;
	}

	/**
	 * @return the listaDepositosCliente
	 */
	public List<ConfirmarDeposito> getListaDepositosCliente() {
		return listaDepositosCliente;
	}

	/**
	 * @param listaDepositosCliente the listaDepositosCliente to set
	 */
	public void setListaDepositosCliente(List<ConfirmarDeposito> listaDepositosCliente) {
		this.listaDepositosCliente = listaDepositosCliente;
	}

	/**
	 * @return the depositosSelecionados
	 */
	public List<ConfirmarDeposito> getDepositosSelecionados() {
		return depositosSelecionados;
	}

	/**
	 * @param depositosSelecionados the depositosSelecionados to set
	 */
	public void setDepositosSelecionados(List<ConfirmarDeposito> depositosSelecionados) {
		this.depositosSelecionados = depositosSelecionados;
	}

	/**
	 * @return the listaDeudasCliente
	 */
	public List<DeudaClientesDto> getListaDeudasCliente() {
		return listaDeudasCliente;
	}

	/**
	 * @param listaDeudasCliente the listaDeudasCliente to set
	 */
	public void setListaDeudasCliente(List<DeudaClientesDto> listaDeudasCliente) {
		this.listaDeudasCliente = listaDeudasCliente;
	}

	/**
	 * @return the deudasSeleccionadas
	 */
	public List<DeudaClientesDto> getDeudasSeleccionadas() {
		return deudasSeleccionadas;
	}

	/**
	 * @param deudasSeleccionadas the deudasSeleccionadas to set
	 */
	public void setDeudasSeleccionadas(List<DeudaClientesDto> deudasSeleccionadas) {
		this.deudasSeleccionadas = deudasSeleccionadas;
	}
	
	/**
	 * @return the lineaNegocioList
	 */
	public List<LineaNegocio> getLineaNegocioList() {
		return lineaNegocioList;
	}

	/**
	 * @param lineaNegocioList the lineaNegocioList to set
	 */
	public void setLineaNegocioList(List<LineaNegocio> lineaNegocioList) {
		this.lineaNegocioList = lineaNegocioList;
	}
	
	/**
	 * @return the lineaSeleccionada
	 */
	public String getLineaSeleccionada() {
		return lineaSeleccionada;
	}

	/**
	 * @param lineaSeleccionada the lineaSeleccionada to set
	 */
	public void setLineaSeleccionada(String lineaSeleccionada) {
		this.lineaSeleccionada = lineaSeleccionada;
	}

	/**
	 * @return the saldoDisponible
	 */
	public Double getSaldoDisponible() {
		return saldoDisponible;
	}

	/**
	 * @param saldoDisponible the saldoDisponible to set
	 */
	public void setSaldoDisponible(Double saldoDisponible) {
		this.saldoDisponible = saldoDisponible;
	}

	/**
	 * @return the saldoDeudas
	 */
	public Double getSaldoDeudas() {
		return saldoDeudas;
	}

	/**
	 * @param saldoDeudas the saldoDeudas to set
	 */
	public void setSaldoDeudas(Double saldoDeudas) {
		this.saldoDeudas = saldoDeudas;
	}

	/**
	 * @return the valorAnticipo
	 */
	public Double getValorAnticipo() {
		return valorAnticipo;
	}

	/**
	 * @param valorAnticipo the valorAnticipo to set
	 */
	public void setValorAnticipo(Double valorAnticipo) {
		this.valorAnticipo = valorAnticipo;
	}

	/**
	 * @return the habilitarBoton
	 */
	public Boolean getHabilitarBoton() {
		return habilitarBoton;
	}

	/**
	 * @param habilitarBoton the habilitarBoton to set
	 */
	public void setHabilitarBoton(Boolean habilitarBoton) {
		this.habilitarBoton = habilitarBoton;
	}

	/**
	 * @return the habilitarComboLinea
	 */
	public Boolean getHabilitarComboLinea() {
		return habilitarComboLinea;
	}

	/**
	 * @param habilitarComboLinea the habilitarComboLinea to set
	 */
	public void setHabilitarComboLinea(Boolean habilitarComboLinea) {
		this.habilitarComboLinea = habilitarComboLinea;
	}
}
