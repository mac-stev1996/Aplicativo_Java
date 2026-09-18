package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.FacVentasCabCpServicioLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;

@ManagedBean
@ViewScoped
public class CxcBuscadorControlDocumentos extends CommonController{
	
	private static final Map<String, String> ACCESO_CONTROL_DOCUMENTOS = new HashMap<String, String>() {{
		put("CREDITO", "/vehiculosWebPrime/jsf/proceso/control-documentos/credito.jsf");
		put("COBRANZA", "/vehiculosWebPrime/jsf/proceso/control-documentos/cobranzas.jsf");
		put("COMERCIAL", "/vehiculosWebPrime/jsf/proceso/control-documentos/comercial.jsf");
		put("OTRO", "/vehiculosWebPrime/jsf/proceso/control-documentos/consultas.jsf");
	}};

	@EJB(lookup = NombreJNDI.FAC_VENTAS_CAB_CP_SERVICIO)
	private FacVentasCabCpServicioLocal facVentasCabCpService;
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;
	
	private String cedula;
	private String noCia;
	private List<Object[]> facturasEncontradas;
	private String usuarioConectado;
	
	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		usuarioConectado = getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario();
	}
	
	public void buscarFacturas () {
		facturasEncontradas = facVentasCabCpService.buscarFacturasVeh(noCia, cedula);
	}
	
	
	public void irControlDocumentos (Object[] factura) throws IOException {
		try {
			ParamDet paramUsuario = paramDetService.usuarioControlDocumento(noCia, usuarioConectado);
			String urlDocumentos = ACCESO_CONTROL_DOCUMENTOS.get(paramUsuario.getTexto1());
			if (urlDocumentos == null) {
				String urlConsulta = ACCESO_CONTROL_DOCUMENTOS.get("OTRO");
				FacesContext.getCurrentInstance().getExternalContext().redirect(urlConsulta + "?nocia="+factura[1]
						+"&centro="+factura[2]
								+"&cotizacion="+factura[0]);
			} else {
				FacesContext.getCurrentInstance().getExternalContext().redirect(urlDocumentos + "?nocia="+factura[1]
						+"&centro="+factura[2]
								+"&cotizacion="+factura[0]);
			}
		} catch (FindException e) {
			String urlConsulta = ACCESO_CONTROL_DOCUMENTOS.get("OTRO");
			FacesContext.getCurrentInstance().getExternalContext().redirect(urlConsulta + "?nocia="+factura[1]
					+"&centro="+factura[2]
							+"&cotizacion="+factura[0]);
		}
	}

	public String getCedula() {
		return cedula;
	}

	public void setCedula(String cedula) {
		this.cedula = cedula;
	}

	public List<Object[]> getFacturasEncontradas() {
		return facturasEncontradas;
	}

	public void setFacturasEncontradas(List<Object[]> facturasEncontradas) {
		this.facturasEncontradas = facturasEncontradas;
	}
	
}
