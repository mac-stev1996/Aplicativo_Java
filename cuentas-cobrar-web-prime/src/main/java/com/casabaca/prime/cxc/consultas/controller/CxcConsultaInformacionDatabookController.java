package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.model.DtbDatosEmpleador;
import com.casabaca.common.ejb.model.DtbDatosRelacionDependencia;
import com.casabaca.common.ejb.model.DtbMediosContacto;
import com.casabaca.common.ejb.model.DtbRelacionIndependencia;
import com.casabaca.common.ejb.model.DtbSocioDemografico;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.WServiceException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.webservice.wsDatabook.InformacionDatabookServiceLocal;

/**
 * Controlador para consultar la informacion de databook
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcConsultaInformacionDatabookController")
public class CxcConsultaInformacionDatabookController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.INFORMACION_DATABOOK_SERVICE)
	private InformacionDatabookServiceLocal databookService;

	private String cedulaConsulta;
	private String nombres;
	private String apellidos;

	private DtbSocioDemografico informacionDatabook;
	private DtbMediosContacto mediosContactoDatabook;
	private DtbDatosEmpleador datosEmpleadorDatabook;
	private DtbRelacionIndependencia datosIndependencia;
	private DtbDatosRelacionDependencia datosDependencia;
	private DtbDatosEmpleador empleador1;
	private DtbDatosEmpleador empleador2;
	
	private boolean consultaDirecta;
	@PostConstruct
	public void init() {
		limpiar();
	}

	public void limpiar() {
		cedulaConsulta = null;
		informacionDatabook = new DtbSocioDemografico();
		mediosContactoDatabook= new DtbMediosContacto();
		datosEmpleadorDatabook = new DtbDatosEmpleador();
		datosIndependencia = new DtbRelacionIndependencia();
		nombres= null;
		apellidos=null;
		consultaDirecta=false;
	}

	public void consultarDatabook() {
		if (cedulaConsulta == null && cedulaConsulta.isEmpty()) {
			error("Ingrese una cedula para la consulta");
			return;
		}
		int diasVigencia = 180; // 6 meses
		if(consultaDirecta){
			diasVigencia =1;
		}

		// Consultar Datos
		try {
			informacionDatabook= databookService.obtenerInformacion(getCompania().getNoCia(), cedulaConsulta, CommonConstants.TITULAR_KRIPTO, diasVigencia);
			if(Objects.isNull(informacionDatabook)){
				warn("No existe la informacion de la cedula ingresada");
				return;
			}
			apellidos = getInformacionDatabook().getApellidoPaterno()+" "+getInformacionDatabook().getApellidoMaterno();
			nombres=getInformacionDatabook().getNombre1()+" "+getInformacionDatabook().getNombre2();
			if(Objects.nonNull(informacionDatabook.getDtbMediosContactos()) && !informacionDatabook.getDtbMediosContactos().isEmpty()){
				mediosContactoDatabook =informacionDatabook.getDtbMediosContactos().get(0);
			}
			if(Objects.nonNull(informacionDatabook.getDtbDatosEmpleadors()) && !informacionDatabook.getDtbDatosEmpleadors().isEmpty()){
				datosEmpleadorDatabook =informacionDatabook.getDtbDatosEmpleadors().get(0);
			}
			if(Objects.nonNull(informacionDatabook.getDtbRelacionIndependencias()) && !informacionDatabook.getDtbRelacionIndependencias().isEmpty()){
				datosIndependencia =informacionDatabook.getDtbRelacionIndependencias().get(0);
			}
			if(Objects.nonNull(informacionDatabook.getDtbDatosRelacionDependencias()) && !informacionDatabook.getDtbDatosRelacionDependencias().isEmpty()){
				datosDependencia = informacionDatabook.getDtbDatosRelacionDependencias().get(0);
			}
			if(Objects.nonNull(informacionDatabook.getDtbDatosEmpleadors()) && !informacionDatabook.getDtbDatosEmpleadors().isEmpty()){
				empleador1 = informacionDatabook.getDtbDatosEmpleadors().get(0);
				if(informacionDatabook.getDtbDatosEmpleadors().size() > 1) {
					empleador2 = informacionDatabook.getDtbDatosEmpleadors().get(1);
				}
			}
			
		} catch (WServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getCedulaConsulta() {
		return cedulaConsulta;
	}

	public void setCedulaConsulta(String cedulaConsulta) {
		this.cedulaConsulta = cedulaConsulta;
	}

	public DtbSocioDemografico getInformacionDatabook() {
		return informacionDatabook;
	}

	public void setInformacionDatabook(DtbSocioDemografico informacionDatabook) {
		this.informacionDatabook = informacionDatabook;
	}

	public DtbMediosContacto getMediosContactoDatabook() {
		return mediosContactoDatabook;
	}

	public void setMediosContactoDatabook(DtbMediosContacto mediosContactoDatabook) {
		this.mediosContactoDatabook = mediosContactoDatabook;
	}

	public DtbDatosEmpleador getDatosEmpleadorDatabook() {
		return datosEmpleadorDatabook;
	}

	public void setDatosEmpleadorDatabook(DtbDatosEmpleador datosEmpleadorDatabook) {
		this.datosEmpleadorDatabook = datosEmpleadorDatabook;
	}

	public DtbRelacionIndependencia getDatosIndependencia() {
		return datosIndependencia;
	}

	public void setDatosIndependencia(DtbRelacionIndependencia datosIndependencia) {
		this.datosIndependencia = datosIndependencia;
	}
	
	public DtbDatosRelacionDependencia getDatosDependencia() {
		return datosDependencia;
	}

	public void setDatosDependencia(DtbDatosRelacionDependencia datosDependencia) {
		this.datosDependencia = datosDependencia;
	}
	
	public DtbDatosEmpleador getEmpleador1() {
		return empleador1;
	}

	public void setEmpleador1(DtbDatosEmpleador empleador1) {
		this.empleador1 = empleador1;
	}

	public DtbDatosEmpleador getEmpleador2() {
		return empleador2;
	}

	public void setEmpleador2(DtbDatosEmpleador empleador2) {
		this.empleador2 = empleador2;
	}

	public String getNombres() {
		return nombres;
	}

	public void setNombres(String nombres) {
		this.nombres = nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public boolean isConsultaDirecta() {
		return consultaDirecta;
	}

	public void setConsultaDirecta(boolean consultaDirecta) {
		this.consultaDirecta = consultaDirecta;
	}	
	
}