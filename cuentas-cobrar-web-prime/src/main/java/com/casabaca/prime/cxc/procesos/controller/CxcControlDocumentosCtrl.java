package com.casabaca.prime.cxc.procesos.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.Cotizacion;
import com.casabaca.cxc.ejb.modelo.CotizacionPK;
import com.casabaca.cxc.ejb.modelo.DocumentoCotizacion;
import com.casabaca.cxc.ejb.servicio.CotizacionServiceLocal;
import com.casabaca.cxc.ejb.servicio.CreditoServiceLocal;
import com.casabaca.cxc.ejb.servicio.DocumentoCotizacionServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;

@ManagedBean
@ViewScoped
public class CxcControlDocumentosCtrl extends CommonController {

	@EJB(lookup = NombreJNDI.DOCUMENTOS_COTIZACION_SERVICE_LOCAL)
	private DocumentoCotizacionServiceLocal documentoCotizacionService;
	@EJB(lookup = NombreJNDI.COTIZACION_SERVICE)
	private CotizacionServiceLocal cotizacionService;
	@EJB(lookup = NombreJNDI.CREDITO_SERVICE)
	private CreditoServiceLocal creditoService;
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;
	
	private List<DocumentoCotizacion> documentos;
	
	private Cotizacion cotizacion;
	
	private String noCia;
	private String usuarioConectado;
	private boolean esUsuarioCobranza = false;
	
	private DocumentoCotizacion  documentoSeleccionado;
	
	
	@PostConstruct
	public void init() {
		noCia = getCompania().getNoCia();
		usuarioConectado = getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario();
		
//		try {
//			paramDetService.usuarioCreditoControlDocumento(noCia, usuarioConectado);
//			esUsuarioCobranza = false;
//		} catch (FindException e1) {
//			esUsuarioCobranza = true;
//		}
		
		String pNoCia = getRequestParameter("nocia");
		String pCentro = getRequestParameter("centro");
		Integer pNumeroCotizacion = Integer.valueOf(getRequestParameter("cotizacion"));
		
		
		
		if (pNoCia == null || pCentro == null || pNumeroCotizacion == null) {
			
		} else {
			if (pNoCia.equals(noCia)) {
				try {
					cotizacion = cotizacionService.findByPK(new CotizacionPK(pNoCia, pCentro, pNumeroCotizacion));
					cotizacion.setObservacionCredito(creditoService.consultarObservacionCreditoSolicitudAgencia(pNoCia,
							cotizacion.getCotizacionPK().getNumeroCotizacion(), cotizacion.getCotizacionPK().getCentro(), cotizacion.getNoSolicitudCred()));
					documentos = documentoCotizacionService.obtenerDocumentos(cotizacion.getCotizacionPK());
					
					if (documentos.isEmpty()) {
						documentos = documentoCotizacionService.crearDocumentosCotizacion(cotizacion.getCotizacionPK(), usuarioConectado);
					} 
				} catch (FindException e) {
					e.printStackTrace();
					addErrorMessage("La cotización que está buscando no existe", null);
				}				
			} else {
				addInfoMessage("Sólo puede ver las cotizaciones de la empresa a la que pertenece", null);
			}
		}
	}
	
	public void recibirDocumentos() {

		for (DocumentoCotizacion documento : documentos) {
			if (documento.isEnviadoBoolean()) {
				if (documento.getFechaEnvio() == null && documento.getUsuarioEnvio() == null) {
					documento.setUsuarioEnvio(usuarioConectado);
					documento.setFechaEnvio(new Date());
				}
			} else {
				documento.setUsuarioEnvio(null);
				documento.setFechaEnvio(null);
			}
			documento.setUsuarioRecibeFisico(documento.isRecibidoFisicoVenta()
					? documento.getUsuarioRecibeFisico() == null ? usuarioConectado : documento.getUsuarioRecibeFisico()
					: null);
			documento.setFechaRecibeFisico(documento.isRecibidoFisicoVenta()
					? documento.getFechaRecibeFisico() == null ? new Date() : documento.getFechaRecibeFisico()
					: null);
			documento.setUsuarioModi(usuarioConectado);
			try {
				documentoCotizacionService.actualizar(documento);
			} catch (UpdateException e) {
				e.printStackTrace();
			}
		}
		info("Se han enviado los documentos.");
	}
	
	
	public void entregarDocumentos() {
		List<DocumentoCotizacion> documentosEntregados = new ArrayList<DocumentoCotizacion>();
		for (DocumentoCotizacion documento: documentos) {
			if (documento.isEntregadoBoolean() && documento.isEnviadoBoolean() && documento.getUsuarioEntrega() == null && documento.getFechaEntrega() == null) {
				documento.setUsuarioEntrega(usuarioConectado);
				documento.setFechaEntrega(new Date());
				documento.setUsuarioModi(usuarioConectado);
				try {
					documentoCotizacionService.actualizar(documento);
					documentosEntregados.add(documento);
				} catch (UpdateException e) {
					e.printStackTrace();
				}
			}
		}
		info("Se han entregado los documentos.");		
	}
	
	public void recibirDocumentosEnCobranzas () {
		List<DocumentoCotizacion> documentosEntregados = new ArrayList<DocumentoCotizacion>();
		for (DocumentoCotizacion documento: documentos) {
			if (documento.isRecibeCobranzasBoolean() && documento.isEntregadoBoolean() && documento.getUsuarioRecibeCobranzas() == null && documento.getFechaRecibeCobranzas() == null) {
				documento.setUsuarioRecibeCobranzas(usuarioConectado);
				documento.setFechaRecibeCobranzas(new Date());
				documento.setUsuarioModi(usuarioConectado);
				try {
					documentoCotizacionService.actualizar(documento);
					documentosEntregados.add(documento);
				} catch (UpdateException e) {
					e.printStackTrace();
				}
			}
		}
		info("Se han recibido los documentos en cobranzas.");		
	}
	
	public void seleccionarDocumento(DocumentoCotizacion documento) {
		documentoSeleccionado = documento;
	}
	
	// GETTERS Y SETTERS
	public List<DocumentoCotizacion> getDocumentos() {
		return documentos;
	}


	public void setDocumentos(List<DocumentoCotizacion> documentos) {
		this.documentos = documentos;
	}


	public Cotizacion getCotizacion() {
		return cotizacion;
	}


	public void setCotizacion(Cotizacion cotizacion) {
		this.cotizacion = cotizacion;
	}

	public boolean isEsUsuarioCobranza() {
		return esUsuarioCobranza;
	}

	public void setEsUsuarioCobranza(boolean esUsuarioCobranza) {
		this.esUsuarioCobranza = esUsuarioCobranza;
	}

	public DocumentoCotizacion getDocumentoSeleccionado() {
		return documentoSeleccionado;
	}

	public void setDocumentoSeleccionado(DocumentoCotizacion documentoSeleccionado) {
		this.documentoSeleccionado = documentoSeleccionado;
	}


	
}
