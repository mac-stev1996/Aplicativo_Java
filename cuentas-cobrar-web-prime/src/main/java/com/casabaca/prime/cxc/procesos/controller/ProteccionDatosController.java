package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.Encripta;
import com.casabaca.common.ejb.model.CotizacionVehiculo;
import com.casabaca.common.ejb.model.InventarioMaster;
import com.casabaca.common.ejb.model.MasterVehiculo;
import com.casabaca.common.ejb.service.CotizacionVehiculoServicioLocal;
import com.casabaca.common.ejb.service.InventarioMasterServiceLocal;
import com.casabaca.common.ejb.service.MasterVehiculoServicioLocal;
import com.casabaca.common.ejb.util.ConstantesVehiculos;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcProtecciondatosCliente;
import com.casabaca.cxc.ejb.modelo.CxcProtecciondatosClientePK;
import com.casabaca.cxc.ejb.servicio.CxcProtecciondatosClienteServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;

/**
 * <b> Clase controlador para obtener politica de proteccion de datos de cotizacion de clientes. </b>
 * 
 * @author luis.soria
 * @version $1.0$
 */
@ManagedBean
@ViewScoped
public class ProteccionDatosController extends CommonController implements Serializable {

	private static final long serialVersionUID = -6654427181882573825L;

	private static final Logger logger = Logger.getLogger(ProteccionDatosController.class);

	@EJB(lookup = NombreJNDI.CXC_PROTECCIONDATOS_CLIENTE_SERVICE)
	private CxcProtecciondatosClienteServiceLocal cxcProteccionDatosServiceLocal;

	@EJB(lookup = NombreJNDI.COTIZACION_VEHICULO_SERVICIO_BEAN)
	private CotizacionVehiculoServicioLocal cotizacionVehiculoService;

	@EJB(lookup = NombreJNDI.MASTER_VEHICULO_SERVICIO_BEAN)
	private MasterVehiculoServicioLocal masterVehiculoService;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;

	@EJB(lookup = NombreJNDI.INVENTARIO_MASTER_SERVICE)
	private InventarioMasterServiceLocal inventarioMasterServiceLocal;

	private CxcProtecciondatosCliente proteccionDatos;
	private String parametro;
	private Integer numeroCotizacion;
	private String noCia;
	private String centro;
	private CotizacionVehiculo cotizacionVeh;
	private MasterVehiculo masterVehiculo;
	private InventarioMaster invVehUsado;
	private CxcProtecciondatosCliente proteccionCliente;
	private boolean muestraMensaje;
	// Variables para identificar GRUPO_MAIl
	private static final String GRUPO_MAIL_CASABACA = "APD01";
	private static final String GRUPO_MAIL_SUZUKI = "APD08";
	private static final String GRUPO_MAIL_TOYOCOSTA = "APDTY";
	private static final String GRUPO_MAIL_NEXUMCORP = "APDT1";

	@PostConstruct
	public void init() {
		logger.info("init...");
		this.muestraMensaje = false;
		cotizacionVeh = new CotizacionVehiculo();
		if (getRequestParameter("token") != null) {
			HttpServletRequest origRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
					.getRequest();
			setParametro(Encripta.desencriptarRecorreHex(getRequestParameter("token")));

			if (!parametro.isEmpty()) {
				int numero = this.parametro.length();
				int posCotizacion = this.parametro.lastIndexOf("-");
				this.noCia = this.parametro.substring(0, 2).trim();
				this.centro = this.parametro.substring(3, 5).trim();
				this.numeroCotizacion = Integer.parseInt(this.parametro.substring(posCotizacion + 1, numero));
			}
			buscarCotizacion();
		}
	}

	public void buscarCotizacion() {

		try {
			cotizacionVeh = cotizacionVehiculoService.getDescripcion(noCia, numeroCotizacion, centro);

			if ((ConstantesVehiculos.VEHICULOS_USADOS_OK.equals(cotizacionVeh.getSvlicodi().toString())
					|| ConstantesVehiculos.VEHICULOS_USADOS.equals(cotizacionVeh.getSvlicodi().toString()))
					&& cotizacionVeh.getFechaProteccionDatos() == null) {
				invVehUsado = inventarioMasterServiceLocal
						.buscarInventarioMaster(cotizacionVeh.getSvfcotiPK().getNoCia(), cotizacionVeh.getSvincodi());
				this.muestraMensaje = true;
			} else if (
			// ConstantesVehiculos.VEHICULOS_NUEVOS.equals(cotizacionVeh.getSvlicodi().toString())&&
			cotizacionVeh.getFechaProteccionDatos() == null) {
				masterVehiculo = masterVehiculoService.buscarMasterVehiculoPorPK(
						cotizacionVeh.getSvfcotiPK().getNoCia(), cotizacionVeh.getSvmamaster(), cotizacionVeh.getSfx());
				this.muestraMensaje = true;
			} else {
				this.muestraMensaje = false;
			}
		} catch (FindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void aceptarProteccionDatos() {
		try {
			if (cotizacionVeh != null) {
				cotizacionVeh.setAceptaProteccionDatos("S");
				cotizacionVeh.setFechaProteccionDatos(new Date());
				cotizacionVehiculoService.update(cotizacionVeh);
				insertaRegistroProteccionDatos();
			}
		} catch (UpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void negarProteccionDatos() {
		try {
			if (cotizacionVeh != null) {
				cotizacionVeh.setAceptaProteccionDatos("N");
				cotizacionVeh.setFechaProteccionDatos(new Date());
				;
				cotizacionVehiculoService.update(cotizacionVeh);
				insertaRegistroProteccionDatos();
			}
		} catch (UpdateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void insertaRegistroProteccionDatos() {
		int sec = 0;
		try {
			if (cotizacionVeh != null && "S".equals(cotizacionVeh.getAceptaProteccionDatos())) {
				CxcProtecciondatosCliente proteccion = new CxcProtecciondatosCliente();
				CxcProtecciondatosClientePK id = new CxcProtecciondatosClientePK();

				id.setNoCia(noCia);
				id.setIdToken(("CO" + System.currentTimeMillis()) + sec);
				proteccion.setId(id);
				if (ConstantesVehiculos.VEHICULOS_USADOS_OK.equals(cotizacionVeh.getSvlicodi().toString())
						|| ConstantesVehiculos.VEHICULOS_USADOS.equals(cotizacionVeh.getSvlicodi().toString())) {
					proteccion.setMarca(invVehUsado.getSvmrcodi());
				} else {
					proteccion.setMarca(masterVehiculo.getSvmrcodi());
				}
				proteccion.setFechaCreacion(new Date());
				proteccion.setEstado("I");
				proteccion.setUsuarioAgente(cotizacionVeh.getUsuario());
				proteccion.setCedula(cotizacionVeh.getCedulaDueno());
				proteccion.setNombre(cotizacionVeh.getNombres());
				proteccion.setApellidos(cotizacionVeh.getApellidos());
				proteccion.setTelefono(cotizacionVeh.getTelefono1());
				proteccion.setEmail(cotizacionVeh.getMail());
				proteccion.setDireccion(cotizacionVeh.getDireccion1());
				proteccion.setAceptaLopdp("S");
				cxcProteccionDatosServiceLocal.insertar(proteccion);
				enviaNotificacionMail();

			} else {
				CxcProtecciondatosCliente proteccion = new CxcProtecciondatosCliente();
				CxcProtecciondatosClientePK id = new CxcProtecciondatosClientePK();

				id.setNoCia(noCia);
				id.setIdToken(("CO" + System.currentTimeMillis()) + sec);
				proteccion.setId(id);
				if (ConstantesVehiculos.VEHICULOS_USADOS_OK.equals(cotizacionVeh.getSvlicodi().toString())
						|| ConstantesVehiculos.VEHICULOS_USADOS.equals(cotizacionVeh.getSvlicodi().toString())) {
					proteccion.setMarca(invVehUsado.getSvmrcodi());
				} else {
					proteccion.setMarca(masterVehiculo.getSvmrcodi());
				}
				proteccion.setFechaCreacion(new Date());
				proteccion.setEstado("I");
				proteccion.setUsuarioAgente(cotizacionVeh.getUsuario());
				proteccion.setCedula(cotizacionVeh.getCedulaDueno());
				proteccion.setNombre(cotizacionVeh.getNombres());
				proteccion.setApellidos(cotizacionVeh.getApellidos());
				proteccion.setTelefono(cotizacionVeh.getTelefono1());
				proteccion.setEmail(cotizacionVeh.getMail());
				proteccion.setDireccion(cotizacionVeh.getDireccion1());
				proteccion.setAceptaLopdp("N");
				cxcProteccionDatosServiceLocal.insertar(proteccion);
			}
		} catch (InsertException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void enviaNotificacionMail() {
		if ("S".equals(cotizacionVeh.getAceptaProteccionDatos())) {

			StringBuffer mensaje = new StringBuffer();

			mensaje.append("Estimado(a), <br><br>");
			mensaje.append("Se informa que el cliente " + cotizacionVeh.getNombres() + " "
					+ cotizacionVeh.getApellidos()
					+ ", autorizo el tratamiento de uso de datos personales con los siguientes datos: <br><br>");
			mensaje.append("<table border='1'><tr><td><b>Numero Cotizacion</b></td><td align=right>"
					+ cotizacionVeh.getSvfcotiPK().getNumeroCotizacion() + "</td></tr>");
			mensaje.append("<tr><td><b>Fecha Aceptación</b></td> <td align=right> "
					+ new SimpleDateFormat("dd/MM/yyyy").format(cotizacionVeh.getFechaProteccionDatos())
					+ "</td></tr>");
			mensaje.append(
					"<tr><td><b>Cédula</b></td> <td align=right> " + cotizacionVeh.getCedulaDueno() + "</td></tr>");
			mensaje.append(
					"<tr><td><b>Teléfono</b></td> <td align=right> " + cotizacionVeh.getTelefono1() + "</td></tr>");
			mensaje.append("<tr><td><b>Email</b></td> <td align=right> " + cotizacionVeh.getMail() + "</td></tr>");
			if (ConstantesVehiculos.VEHICULOS_USADOS_OK.equals(cotizacionVeh.getSvlicodi().toString())
					|| ConstantesVehiculos.VEHICULOS_USADOS.equals(cotizacionVeh.getSvlicodi().toString())) {
				mensaje.append(
						"<tr><td><b>Marca</b></td> <td align=right> " + invVehUsado.getSvmrcodi() + "</td></tr>");
			} else {
				mensaje.append(
						"<tr><td><b>Marca</b></td> <td align=right> " + masterVehiculo.getSvmrcodi() + "</td></tr>");
			}
			mensaje.append("</table>");
			mensaje.append(
					"<br><br><b>El cliente si Acepto la Politica de Proteccion de Datos Personales. </b><br><br>");
			mensaje.append("Gracias por su atención. <br><br>");

			try {
				SisMailServidores sisMailServidores;
				sisMailServidores = getSisMailServidores(cotizacionVeh.getSvfcotiPK().getNoCia());

				if (cotizacionVeh.getSvfcotiPK().getNoCia().equals(CommonConstants.CASABACA)) {

					List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo(GRUPO_MAIL_CASABACA);
					if (usuariosMail != null && !usuariosMail.isEmpty()) {
						for (Object[] objects : usuariosMail) {
							mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
									(String) objects[1],
									"NOTIFICACION DE ACEPTACION POLITICA DE PROTECCION DE DATOS - COTIZACION #: "
											+ cotizacionVeh.getSvfcotiPK().getNumeroCotizacion(),
									mensaje);
						}
					}
				} else if (cotizacionVeh.getSvfcotiPK().getNoCia().equals(CommonConstants.SUZUKI)) {

					List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo(GRUPO_MAIL_SUZUKI);
					if (usuariosMail != null && !usuariosMail.isEmpty()) {
						for (Object[] objects : usuariosMail) {
							mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
									(String) objects[1],
									"NOTIFICACION DE ACEPTACION POLITICA DE PROTECCION DE DATOS - COTIZACION #: "
											+ cotizacionVeh.getSvfcotiPK().getNumeroCotizacion(),
									mensaje);
						}
					}
				} else if (cotizacionVeh.getSvfcotiPK().getNoCia().equals(CommonConstants.TOYOCOSTAS)) {

					List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo(GRUPO_MAIL_TOYOCOSTA);
					if (usuariosMail != null && !usuariosMail.isEmpty()) {
						for (Object[] objects : usuariosMail) {
							mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
									(String) objects[1],
									"NOTIFICACION DE ACEPTACION POLITICA DE PROTECCION DE DATOS - COTIZACION #: "
											+ cotizacionVeh.getSvfcotiPK().getNumeroCotizacion(),
									mensaje);
						}
					}
				} else if (cotizacionVeh.getSvfcotiPK().getNoCia().equals(CommonConstants.NEXUMCORP)) {

					List<Object[]> usuariosMail = mailUsuarioService.findByEmailGrupo(GRUPO_MAIL_NEXUMCORP);
					if (usuariosMail != null && !usuariosMail.isEmpty()) {
						for (Object[] objects : usuariosMail) {
							mailService.sendEmailInHtmlNoCia(sisMailServidores, sisMailServidores.getCuenta(),
									(String) objects[1],
									"NOTIFICACION DE ACEPTACION POLITICA DE PROTECCION DE DATOS - COTIZACION #: "
											+ cotizacionVeh.getSvfcotiPK().getNumeroCotizacion(),
									mensaje);
						}
					}
				}
			} catch (GeneralException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public CxcProtecciondatosCliente getProteccionDatos() {
		return proteccionDatos;
	}

	public void setProteccionDatos(CxcProtecciondatosCliente proteccionDatos) {
		this.proteccionDatos = proteccionDatos;
	}

	public String getParametro() {
		return parametro;
	}

	public void setParametro(String parametro) {
		this.parametro = parametro;
	}

	public void setNumeroCotizacion(Integer numeroCotizacion) {
		this.numeroCotizacion = numeroCotizacion;
	}

	public Integer getNumeroCotizacion() {
		return numeroCotizacion;
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

	public CotizacionVehiculo getCotizacionVeh() {
		return cotizacionVeh;
	}

	public void setCotizacionVeh(CotizacionVehiculo cotizacionVeh) {
		this.cotizacionVeh = cotizacionVeh;
	}

	public boolean isMuestraMensaje() {
		return muestraMensaje;
	}

	public void setMuestraMensaje(boolean muestraMensaje) {
		this.muestraMensaje = muestraMensaje;
	}

}