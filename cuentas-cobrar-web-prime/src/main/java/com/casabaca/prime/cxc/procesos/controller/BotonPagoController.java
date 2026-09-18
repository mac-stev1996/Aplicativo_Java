/**
 * 
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcBotonPago;
import com.casabaca.cxc.ejb.servicio.CxcBotonPagoServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.WServiceException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.rest.wsBotonPago.BotonPagoInput;
import com.casabaca.rest.wsBotonPago.BotonPagoOuput;
import com.casabaca.webservice.ws.IBotonPago;
import com.casabaca.webservice.ws.impl.BotonPagoImpl;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class BotonPagoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8386822278247185320L;

	@EJB(lookup = NombreJNDI.JNDI_CXC + "CxcBotonPagoServiceBean")
	private CxcBotonPagoServiceLocal cxcBotonPagoService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	private String noCia;
	private List<CxcBotonPago> pagos;
	private ParamDet paramDetServidor;

	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		try {
			paramDetServidor = paramDetService.findbyNoCiabyCodigobyCodigoDet(this.noCia, "BOTONP", "RESTBP");
		} catch (FindException e) {
			paramDetServidor = null;
		}
		consultar();
	}

	public void consultar() {
		pagos = cxcBotonPagoService.consultarPagosPendientes(this.noCia);
	}

	public void procesarPendiente(CxcBotonPago cxcBotonPago) {
		if (paramDetServidor == null) {
			error("No existe configurador el servicio rest. Comuniquese con sistemas");
			return;
		}
		IBotonPago iBotonPago = new BotonPagoImpl();
		BotonPagoInput botonPagoInput = new BotonPagoInput();
		botonPagoInput.setCodigoCompania(cxcBotonPago.getId().getNoCia());
		botonPagoInput.setSecPagoPendiente(cxcBotonPago.getId().getSecuencia());
		botonPagoInput.setMotivoFacturacion(cxcBotonPago.getMotivoFacturacion());
		botonPagoInput.setCodigoRazon(cxcBotonPago.getCodigoRazon());
		botonPagoInput.setUsuarioReproceso(getUsuario().getUsuario());
		botonPagoInput.setApellidos(cxcBotonPago.getApellidos());
		botonPagoInput.setNombres(cxcBotonPago.getNombres());
		botonPagoInput.setTipoIdentificacion(cxcBotonPago.getTipoIdentificacion());
		botonPagoInput.setCedula(cxcBotonPago.getIdentificacion());
		botonPagoInput.setDireccion(cxcBotonPago.getDireccion());
		botonPagoInput.setMail(cxcBotonPago.getEmail());
		botonPagoInput.setTelefono(cxcBotonPago.getTelefono());
		botonPagoInput.setCodigoAutorizacion(cxcBotonPago.getAutorizacion());
		botonPagoInput.setNumeroRecap(cxcBotonPago.getRecap());
		botonPagoInput.setNumeroReferencia(cxcBotonPago.getIdDatafast());
		botonPagoInput.setNumeroTransaccion(cxcBotonPago.getTransaccion());
		botonPagoInput.setValor(cxcBotonPago.getMonto());
		try {
			BotonPagoOuput ouput = iBotonPago.procesarFacturaBotonPago(botonPagoInput, paramDetServidor);
			if ("200".equals(ouput.getCodigo())) {
				info(String.format("Factura %s generada con exito", ouput.getValorAdicional()));
			} else {
				error(ouput.getMensaje());
			}
			consultar();
		} catch (WServiceException e) {
			error(e.getMensaje());
		}
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<CxcBotonPago> getPagos() {
		return pagos;
	}

	public void setPagos(List<CxcBotonPago> pagos) {
		this.pagos = pagos;
	}

	public ParamDet getParamDetServidor() {
		return paramDetServidor;
	}

	public void setParamDetServidor(ParamDet paramDetServidor) {
		this.paramDetServidor = paramDetServidor;
	}

}
