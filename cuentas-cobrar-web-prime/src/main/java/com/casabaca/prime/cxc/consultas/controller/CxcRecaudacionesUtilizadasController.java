package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcRecaudacionUtilizadaDto;
import com.casabaca.cxc.ejb.servicio.CxcConfirmacionRecaudacionServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;
import com.casabaca.s3s.ejb.service.MailServiceLocal;

/**
 * Controlador para consultar las recaudaciones utilizadas
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcRecaudacionesUtilizadasController")
public class CxcRecaudacionesUtilizadasController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.CXC_CONFIRMA_RECAUDACION_SERVICE)
	private CxcConfirmacionRecaudacionServiceLocal recaudacionService;

	
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	
	// Constantes para verificar tipo de archivo
	public static final String DOC_FILE = ".doc";
	public static final String PDF_FILE = ".pdf";
	public static final String XLS_FILE = ".xls";
	// Constantes para setear la aplicacion en el header
	public static final String XLS_APPLICATION = "application/vnd.ms-excel";
	public static final String PDF_APPLICATION = "application/pdf";
	public static final String DOC_APPLICATION = "application/msword";

	private LazyDataModelClientes clientes;
	private Cliente clienteSeleccionado;
	private List<CxcRecaudacionUtilizadaDto> recaudaciones;
	private CxcRecaudacionUtilizadaDto seleccionada;
	private Date fechaInicial;
	private String noCia;
	private String lote;
	private BigDecimal totalLote;
	
	@PostConstruct
	public void init() {
		recaudaciones = new ArrayList<>();
		seleccionada = new CxcRecaudacionUtilizadaDto();
		noCia = getCompania().getNoCia();
	}

	
	protected String getRequestParameter(String name) {
		return (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(name);
	}
	
	public void limpiar() {
		fechaInicial = null;
		recaudaciones = new ArrayList<>();
		seleccionada = new CxcRecaudacionUtilizadaDto();
	}

	public void abrirRecaudacion() {
		lote= new String();
		totalLote= BigDecimal.ZERO;
		accionesDialog("DlgRecaudacion", true);
	}
	
	public void guardarLote() {
		if(lote == null){
			addErrorMessage("ALERTA", "No ha ingresado un lote valido");
			return;
		}
		if (recaudaciones == null || recaudaciones.isEmpty()) {
			addErrorMessage("ALERTA", "No existen registros a actualizar");
			return;
		}		
		
		double total=recaudaciones.stream().mapToDouble(r->r.getValor().doubleValue()).sum();
		BigDecimal totalDetalle = new BigDecimal(total).divide(BigDecimal.ONE,2,BigDecimal.ROUND_HALF_UP);
		if(totalDetalle.compareTo(totalLote) != 0){
			addErrorMessage("ALERTA", "El valor del total de lote: "+totalLote+" no concuerda con el total del detalle: "+totalDetalle);
			return;
		}
		
		for(CxcRecaudacionUtilizadaDto rec:recaudaciones){
			rec.setLote(lote);
			recaudacionService.actualizarLoteRecaudacion(noCia, rec, getUsuario().getUsuario());
		}
	buscarDiariosCriterios();
	accionesDialog("DlgRecaudacion", false);
	addInfoMessage("", "Se ha registrado el Lote exitosamente "+lote);
	}

	public void buscarDiariosCriterios() {
		try {
			String tipoTrans ="R";
//			String tipoTrans =null;
			recaudaciones = new ArrayList<>();
			List<CxcRecaudacionUtilizadaDto> todas = recaudacionService.consultarTodasRecaudaciones(noCia, fechaInicial,tipoTrans , null, null);
			List<CxcRecaudacionUtilizadaDto> utilizadas= recaudacionService.consultarRecuadacionesFecha(noCia, fechaInicial,tipoTrans , null, null);
			Map<String, CxcRecaudacionUtilizadaDto> map = new  HashMap<String, CxcRecaudacionUtilizadaDto>();
			if(utilizadas != null && !utilizadas.isEmpty()){
				for(CxcRecaudacionUtilizadaDto rec: utilizadas)	{
					map.put(rec.getNoFisico(), rec);
				}
				
				for(CxcRecaudacionUtilizadaDto rec: todas){
					if(map.containsKey(rec.getNoFisico())){
						recaudaciones.add(map.get(rec.getNoFisico()));
					}else{
						recaudaciones.add(rec);
					}
				}
			}else if(todas != null && !todas.isEmpty()){
				recaudaciones = todas;
			}
			
		} catch (FindException e) {

			e.printStackTrace();
		}
		
	}

	
	public LazyDataModelClientes getClientes() {
		return clientes;
	}

	public void setClientes(LazyDataModelClientes clientes) {
		this.clientes = clientes;
	}

	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}

	public void setClienteSeleccionado(Cliente clienteSeleccionado) {
		this.clienteSeleccionado = clienteSeleccionado;
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}


	public List<CxcRecaudacionUtilizadaDto> getRecaudaciones() {
		return recaudaciones;
	}


	public void setRecaudaciones(List<CxcRecaudacionUtilizadaDto> recaudaciones) {
		this.recaudaciones = recaudaciones;
	}


	public CxcRecaudacionUtilizadaDto getSeleccionada() {
		return seleccionada;
	}


	public void setSeleccionada(CxcRecaudacionUtilizadaDto seleccionada) {
		this.seleccionada = seleccionada;
	}


	public String getLote() {
		return lote;
	}


	public void setLote(String lote) {
		this.lote = lote;
	}


	public BigDecimal getTotalLote() {
		return totalLote;
	}


	public void setTotalLote(BigDecimal totalLote) {
		this.totalLote = totalLote;
	}

}