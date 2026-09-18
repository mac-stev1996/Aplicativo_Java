package com.casabaca.prime.cxc.mantenimiento.controller;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.servicio.RecaudacionDiariaServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

@ManagedBean
@ViewScoped
public class EnvioRecaudacionDiariaCtrl extends CommonController {

	@EJB(lookup = NombreJNDI.RECAUDACION_DIARIA_SERVICE_BEAN)
	private RecaudacionDiariaServiceLocal recaudacionDiariaService;
	
	public void enviarRecaudacionDiaria () {
		recaudacionDiariaService.enviarCorreoRecaudacionDiaria(getCompania().getNoCia());
	}
}
