package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.model.ArccdcScb;
import com.casabaca.common.ejb.model.ArccmdAllView;
import com.casabaca.common.ejb.model.Arccrd;
import com.casabaca.common.ejb.util.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.CxcHistorialDocumentosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;

@ViewScoped
@ManagedBean
public class CxcHistorialDocumentosController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(CxcHistorialDocumentosController.class);
	private static final String REPORTE = "/cxc/cxcRepEntradaMov";

	@EJB(lookup = NombreJNDI.CXC_HISTORIAL_DOCUMENTOS_SERVICE)
	private CxcHistorialDocumentosServiceLocal service;

	private String noCia;
	private String noDocu;
	private String noCliente;
	private String cedula;
	private String noFisico;
	private Date fechaDesde;
	private Date fechaHasta;
	private List<ArccmdAllView> lista;
	private ArccmdAllView seleccionado;
	private List<Arccrd> listaAfectandoA;
	private List<Arccrd> listaAfectadoPor;
	private List<ArccdcScb> listaDetalleContable;
	private boolean afectadoPor;

	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		lista = new ArrayList<ArccmdAllView>();
		seleccionado = new ArccmdAllView();
	}

	public void buscarDocumentos() {
		try {
			this.lista = service.getLista(noCia, noDocu, noCliente, cedula, noFisico, fechaDesde, fechaHasta);
			seleccionado = new ArccmdAllView();
			afectadoPor = false;
		} catch (FindException e) {
			super.error("Error al buscar documentos");
			log.error("Error al buscar documentos", e);
		}
	}

	public void seleccionarDocumento() {
		seleccionado = service.seleccionarDocumento(seleccionado);
		if (seleccionado != null)
			afectadoPor = "D".equals(seleccionado.getTipoMov());
	}

	public void cargarAfectandoA() {
		if (seleccionado == null)
			return;
		try {
			listaAfectandoA = service.getAfectandoA(seleccionado.getNoCia(), seleccionado.getNoDocu());
			super.accionesDialog("dlgAfectandoAw", true);
		} catch (FindException e) {
			super.error("Error al cargar documentos afectados");
			log.error("Error al cargar documentos afectados", e);
		}
	}

	public void cargarAfectadoPor() {
		if (seleccionado == null)
			return;
		try {
			listaAfectadoPor = service.getAfectadoPor(seleccionado.getNoCia(), seleccionado.getNoDocu(),
					seleccionado.getCentro());
			super.accionesDialog("dlgAfectadoPorw", true);
		} catch (FindException e) {
			super.error("Error al cargar documentos que afectan al seleccionado");
			log.error("Error al cargar documentos que afectan al seleccionado", e);
		}
	}

	public void cargarDetalleContable() {
		if (seleccionado == null)
			return;
		try {
			listaDetalleContable = service.getDetalleContable(seleccionado.getNoCia(), seleccionado.getNoDocu());
			super.accionesDialog("dlgDetalleContablew", true);
		} catch (FindException e) {
			super.error("Error al cargar documentos que afectan al seleccionado");
			log.error("Error al cargar documentos que afectan al seleccionado", e);
		}
	}

	public void imprimirDocumento() {
		Map<String, Object> parameters = new HashMap<>();
		setParameters(parameters);
		super.ejecutarJavascript("openDuplicatedTab('"
				+ super.callJasperReport(REPORTE, parameters, CommonConstants.OUTPUT_PDF) + "');");
	}

	private void setParameters(Map<String, Object> parameters) {
		parameters.put("p_centrodoc", seleccionado.getCentro());
		parameters.put("p_nodocu", seleccionado.getNoDocu());
		parameters.put("p_tipodoc", seleccionado.getTipoDoc());
	}

	public List<ArccmdAllView> getLista() {
		return lista;
	}

	public void setLista(List<ArccmdAllView> lista) {
		this.lista = lista;
	}

	public ArccmdAllView getSeleccionado() {
		return seleccionado;
	}

	public void setSeleccionado(ArccmdAllView seleccionado) {
		this.seleccionado = seleccionado;
	}

	public String getNoDocu() {
		return noDocu;
	}

	public void setNoDocu(String noDocu) {
		this.noDocu = noDocu;
	}

	public String getNoCliente() {
		return noCliente;
	}

	public void setNoCliente(String noCliente) {
		this.noCliente = noCliente;
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public String getNoFisico() {
		return noFisico;
	}

	public void setNoFisico(String noFisico) {
		this.noFisico = noFisico;
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

	public List<Arccrd> getListaAfectandoA() {
		return listaAfectandoA;
	}

	public void setListaAfectandoA(List<Arccrd> listaAfectandoA) {
		this.listaAfectandoA = listaAfectandoA;
	}

	public List<Arccrd> getListaAfectadoPor() {
		return listaAfectadoPor;
	}

	public void setListaAfectadoPor(List<Arccrd> listaAfectadoPor) {
		this.listaAfectadoPor = listaAfectadoPor;
	}

	public List<ArccdcScb> getListaDetalleContable() {
		return listaDetalleContable;
	}

	public void setListaDetalleContable(List<ArccdcScb> listaDetalleContable) {
		this.listaDetalleContable = listaDetalleContable;
	}

	public boolean isAfectadoPor() {
		return afectadoPor;
	}

	public void setAfectadoPor(boolean afectadoPor) {
		this.afectadoPor = afectadoPor;
	}

}
