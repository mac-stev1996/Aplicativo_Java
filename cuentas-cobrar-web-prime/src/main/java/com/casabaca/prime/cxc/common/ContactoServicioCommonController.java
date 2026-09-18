package com.casabaca.prime.cxc.common;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.SeguimientoVehNuevosDto;
import com.casabaca.cxc.ejb.modelo.CxcContactoServicio;
import com.casabaca.cxc.ejb.servicio.CxcContactoServicioServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.lazy.LDMSeguimientoVehNuevos;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.servicios.ejb.modelo.SeTecniAsesor;
import com.casabaca.servicios.ejb.service.SeTecniAsesorServiceLocal;

/**
 * @author lu_soria
 */
public class ContactoServicioCommonController extends CommonController {

	private static final Logger logger = Logger.getLogger(ContactoServicioCommonController.class);

	@EJB(lookup = NombreJNDI.CXC_CONTACTO_SERVICIO_SERVICE)
	private CxcContactoServicioServiceLocal contactoService;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN)
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaService;

	@EJB(lookup = NombreJNDI.SETECNIASESOR_SERVICE_LOCAL)
	private SeTecniAsesorServiceLocal seTecniAsesorService;

	protected String usuarioSesion;
	protected List<FacesMessage> listaMensajesIniciales;
	protected CxcContactoServicio contactoSer;
	protected CxcContactoServicio contactoSeleccionado;
	protected SeguimientoVehNuevosDto seguimientoVehBusqueda;
	protected SeguimientoVehNuevosDto seguimientoVeh;
	protected List<SeguimientoVehNuevosDto> listaSegVehNuevos;
	protected List<CxcContactoServicio> listaContactos;
	protected List<CxcContactoServicio> listaContactosServicio;
	protected List<CxcContactoServicio> listaContactosVehiculos;
	protected List<LineaNegocio> listaLineasNegocio;
	protected List<UsuarioSis> listaAsesores;
	protected List<SeTecniAsesor> listaAsesoresServicio;
	protected String nombreAsesorABuscar;
	protected LDMSeguimientoVehNuevos model;
	// Datos Generales
	protected String noCia;
	protected String centro;
	protected String pantalla;
	protected String noLinea;

	protected static final List<String> ESTADOS_ENTREGA_VEH = Arrays.asList(EnumEntregaVeh.INGRESADA.getCodigo(),
			EnumEntregaVeh.LLEGADA.getCodigo(), EnumEntregaVeh.CONFIRMADA.getCodigo(),
			EnumEntregaVeh.APROBADA.getCodigo());

	public boolean validarCampos() {
		boolean sonCamposValidos = true;
		if (getContactoSer().getNombreContacto() == null || getContactoSer().getNombreContacto().isEmpty()) {
			error("Por favor debe ingresar el nombre del contacto");
			sonCamposValidos = false;
		}
		if (getContactoSer().getTelefono() == null || getContactoSer().getTelefono().isEmpty()) {
			error("Por favor debe ingresar el telefono del contacto");
			sonCamposValidos = false;
		}
		if (getContactoSer().getEmail() == null || getContactoSer().getEmail().isEmpty()) {
			error("Por favor debe ingresar el email del contacto");
			sonCamposValidos = false;
		}
		if (getContactoSer().getTipoContacto() == null || getContactoSer().getTipoContacto().isEmpty()) {
			error("Por favor debe seleccionar el tipo de contacto");
			sonCamposValidos = false;
		}
		return sonCamposValidos;
	}

	// METODOS PARA GUARDAR UN CONTACTO
	public void guardarNuevoContacto() {
		prepararContactoParaGuardar();
		try {
			contactoService.create(contactoSer);
		} catch (InsertException e) {
			error("Error al guardar el contacto de servicio");
		}
	}

	public void actualizarContacto() {
		prepararContactoParaActualizar();
		try {
			contactoService.update(contactoSer);
		} catch (UpdateException e) {
			error("Error al actualizar el contacto de servicio");
		}
	}

	private void prepararContactoParaGuardar() {
		getContactoSer().setNoCliente(this.seguimientoVeh.getNoCliente());
		if (esContactoVehicular()) {
			getContactoSer().setPlaca(seguimientoVeh.getPlaca());
		}
	}

	private void prepararContactoParaActualizar() {
		if (esContactoVehicular()) {
			getContactoSer().setPlaca(seguimientoVeh.getPlaca());
		}
		getContactoSer().setFechaMod(new Date());
		getContactoSer().setUsuarioMod(usuarioSesion);
	}

	private boolean esContactoVehicular() {
		return EnumEntregaVeh.TIPO_VEH.getCodigo().equals(getContactoSer().getTipoContacto());
	}
	
	protected String remplazarCaracteresEspeciales(String mensajeParam) {
		String mensajeRemplaza = mensajeParam.replaceAll("á", "&aacute;").replaceAll("é", "&eacute;")
				.replaceAll("í", "&iacute;").replaceAll("ó", "&oacute;").replaceAll("ú", "&uacute;")
				.replaceAll("Á", "A").replaceAll("É", "E").replaceAll("Í", "I").replaceAll("Ó", "O")
				.replaceAll("Ú", "U").replaceAll("¿", "&iquest;").replaceAll("¡", "&iexcl;").replaceAll("ñ", "&ntilde;")
				.replaceAll("Ñ", "&Ntilde;");
		return mensajeRemplaza;
	}


	public int getBatchSize() {
		return CommonConstants.BATCH_SIZE;
	}

	public String getUsuarioSesion() {
		return usuarioSesion;
	}

	public void setUsuarioSesion(String usuarioSesion) {
		this.usuarioSesion = usuarioSesion;
	}

	public List<FacesMessage> getListaMensajesIniciales() {
		return listaMensajesIniciales;
	}

	public void setListaMensajesIniciales(List<FacesMessage> listaMensajesIniciales) {
		this.listaMensajesIniciales = listaMensajesIniciales;
	}

	public CxcContactoServicio getContactoSer() {
		return contactoSer;
	}

	public void setContactoSer(CxcContactoServicio contactoSer) {
		this.contactoSer = contactoSer;
	}

	public CxcContactoServicio getContactoSeleccionado() {
		return contactoSeleccionado;
	}

	public void setContactoSeleccionado(CxcContactoServicio contactoSeleccionado) {
		this.contactoSeleccionado = contactoSeleccionado;
	}

	public SeguimientoVehNuevosDto getSeguimientoVehBusqueda() {
		return seguimientoVehBusqueda;
	}

	public void setSeguimientoVehBusqueda(SeguimientoVehNuevosDto seguimientoVehBusqueda) {
		this.seguimientoVehBusqueda = seguimientoVehBusqueda;
	}

	public SeguimientoVehNuevosDto getSeguimientoVeh() {
		return seguimientoVeh;
	}

	public void setSeguimientoVeh(SeguimientoVehNuevosDto seguimientoVeh) {
		this.seguimientoVeh = seguimientoVeh;
	}

	public List<SeguimientoVehNuevosDto> getListaSegVehNuevos() {
		return listaSegVehNuevos;
	}

	public void setListaSegVehNuevos(List<SeguimientoVehNuevosDto> listaSegVehNuevos) {
		this.listaSegVehNuevos = listaSegVehNuevos;
	}

	public List<CxcContactoServicio> getListaContactos() {
		return listaContactos;
	}

	public void setListaContactos(List<CxcContactoServicio> listaContactos) {
		this.listaContactos = listaContactos;
	}

	public List<LineaNegocio> getListaLineasNegocio() {
		return listaLineasNegocio;
	}

	public void setListaLineasNegocio(List<LineaNegocio> listaLineasNegocio) {
		this.listaLineasNegocio = listaLineasNegocio;
	}

	public List<UsuarioSis> getListaAsesores() {
		return listaAsesores;
	}

	public void setListaAsesores(List<UsuarioSis> listaAsesores) {
		this.listaAsesores = listaAsesores;
	}

	public List<SeTecniAsesor> getListaAsesoresServicio() {
		return listaAsesoresServicio;
	}

	public void setListaAsesoresServicio(List<SeTecniAsesor> listaAsesoresServicio) {
		this.listaAsesoresServicio = listaAsesoresServicio;
	}

	public String getNombreAsesorABuscar() {
		return nombreAsesorABuscar;
	}

	public void setNombreAsesorABuscar(String nombreAsesorABuscar) {
		this.nombreAsesorABuscar = nombreAsesorABuscar;
	}

	public LDMSeguimientoVehNuevos getModel() {
		return model;
	}

	public void setModel(LDMSeguimientoVehNuevos model) {
		this.model = model;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getCentro() {
		return centro;
	}

	public void setCentro(String centro) {
		this.centro = centro;
	}

	public String getPantalla() {
		return pantalla;
	}

	public void setPantalla(String pantalla) {
		this.pantalla = pantalla;
	}

	public String getNoLinea() {
		return noLinea;
	}

	public void setNoLinea(String noLinea) {
		this.noLinea = noLinea;
	}

	public void setListaContactosServicio(List<CxcContactoServicio> listaContactosServicio) {
		this.listaContactosServicio = listaContactosServicio;
	}

	public void setListaContactosVehiculos(List<CxcContactoServicio> listaContactosVehiculos) {
		this.listaContactosVehiculos = listaContactosVehiculos;
	}
	

}