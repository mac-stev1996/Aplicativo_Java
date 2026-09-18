package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
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

import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.model.Arcjdep;
import com.casabaca.common.ejb.model.Banco;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.model.Vendedor;
import com.casabaca.common.ejb.model.VendedorPK;
import com.casabaca.common.ejb.service.ArcjdepServiceLocal;
import com.casabaca.common.ejb.service.BancoServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.NominaEmpleadosServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.service.delegate.VendedorServicioDelegate;
import com.casabaca.common.ejb.util.ConstantesVehiculos;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.ConDepositos;
import com.casabaca.cxc.ejb.servicio.ConDepositosServiceLocal;
import com.casabaca.cxc.ejb.util.TipoTransaccion;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.SisMailServidores;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.vehiculos.ejb.modelo.VehHistorialReservacion;
import com.casabaca.vehiculos.ejb.servicio.VehHistorialReservacionServiceLocal;
/**
 * 
 * @author df_cadena
 *
 */

@ViewScoped
@ManagedBean(name = "procesaAnticiposController")
public class ProcesaAnticiposController extends CommonController implements Serializable{

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ProcesaAnticiposController.class);
	
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioServiceLocal;	
	@EJB(lookup = NombreJNDI.BANCO_SERVICE)
	private BancoServiceLocal bancoServiceLocal;	
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;
	@EJB(lookup = NombreJNDI.CON_DEPOSITOS_SERVICE_BEAN)	
	private  ConDepositosServiceLocal conDepositosService;
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.NOMINA_EMPLEADOS_SERVICE)
	private NominaEmpleadosServiceLocal nominaEmpleadosServiceLocal;
	@EJB(lookup = NombreJNDI.VEH_RESERVACION_HISTORIAL_VEHICULO)
	private VehHistorialReservacionServiceLocal vehHistorialReservacionServiceLocal;
	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	protected ParamDetServiceLocal paramDetService;
	//Req-35779 CON AUTOMAT DEPOSITO Y TRANSFER
	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaService;
	@EJB(lookup = NombreJNDI.ARCJDEP_SERVICE)
	private ArcjdepServiceLocal arcjdepService;

	private List<ConDepositos> listConDepositos;
	private Map<Long,String> documentoConDepositosMap;
	private List<Banco> listBancos;
	private String urlImagen;
	private String urlPathArchivoTercero;
	
	private boolean empresaApro;
		
	
	@PostConstruct
	public void init() throws FindException, ServiceLocatorException {
		this.listConDepositos =new ArrayList<>();
		this.documentoConDepositosMap= new HashMap<Long, String>();
		this.listBancos = new ArrayList<>();
		
		this.obtieneDepositos();
		
		List<ParamDet> paramDet = new ArrayList<ParamDet>();
		paramDet = paramDetService.buscarDetalleCodigo(getCompania().getNoCia(), "EMPAPR");
		
		if (paramDet!=null && paramDet.size()>0) {
			this.empresaApro = true;
		}else {
			this.empresaApro = false;
		}
		
	}
	
	public void obtieneDepositos(){
		logger.info("obtieneDepositos..."+getCompania().getNoCia());
		try {			
			HashMap<String, String> bancos = new HashMap<>();
			HashMap<String, String> tiposTransaccion = new HashMap<>();
			this.documentoConDepositosMap= new HashMap<Long, String>();
			
			tiposTransaccion.put(TipoTransaccion.DEPOSITO_EFECTIVO.getTipo(), TipoTransaccion.DEPOSITO_EFECTIVO.getDescripcion());
			tiposTransaccion.put(TipoTransaccion.DEPOSITO_CHEQUE.getTipo(), TipoTransaccion.DEPOSITO_CHEQUE.getDescripcion());
			tiposTransaccion.put(TipoTransaccion.TRANSF_MISMO_BANCO.getTipo(), TipoTransaccion.TRANSF_MISMO_BANCO.getDescripcion());
			tiposTransaccion.put(TipoTransaccion.TRANSF_OTROS_BANCOS.getTipo(), TipoTransaccion.TRANSF_OTROS_BANCOS.getDescripcion());
			tiposTransaccion.put(TipoTransaccion.TRANSF_APP.getTipo(), TipoTransaccion.TRANSF_APP.getDescripcion());
			tiposTransaccion.put(TipoTransaccion.DEPOSITO_AGENCIA.getTipo(), TipoTransaccion.DEPOSITO_AGENCIA.getDescripcion());
			tiposTransaccion.put(TipoTransaccion.RECAUDACION.getTipo(), TipoTransaccion.RECAUDACION.getDescripcion());
			
			if(this.listBancos == null || this.listBancos.isEmpty()) {
				this.listBancos = this.bancoServiceLocal.getListaBanco();
			}
			for (Banco banco : listBancos) {
				bancos.put(banco.getBanco(), banco.getDescrip());
			}
			
			this.listConDepositos = conDepositosService.consultarConDepositos(getCompania().getNoCia(), "S");
			for (ConDepositos conDepositos : listConDepositos) {
				conDepositos.setNombreBanco(bancos.get(conDepositos.getBanco()));
				conDepositos.setNombreTipoTransaccion(tiposTransaccion.get(conDepositos.getTipoTransaccion()));
				if(conDepositos.getCedula() != null){
					String nombre = null;
					try {
						nombre = clienteServiceLocal.buscarNombrePorCedulaNoCia(conDepositos.getCedula(), getCompania().getNoCia()).getNombre();
						conDepositos.setNombreCliente(nombre);
					} catch (FindException e) {
						addErrorMessage("Error al consultar cliente", e.getMessage());				
					}
				
				}
				if(conDepositos.getAgenciaCajero() != null) {
					String nombreAgencia = agenciaService.buscarNombreAgencia(getCompania().getNoCia(), conDepositos.getAgenciaCajero());
					conDepositos.setNombreAgenciaCajero(nombreAgencia);
				}
				//Req-35779 CON AUTOMAT DEPOSITO Y TRANSFER
				conDepositos.setNombreLinea("".equals(conDepositos.getNoLinea()) || conDepositos.getNoLinea() == null ? "N/A" : 
					lineaService.getNombreLineaNegocio(getCompania().getNoCia(),conDepositos.getNoLinea()));
				documentoConDepositosMap.put(conDepositos.getId(), conDepositos.getNumeroDocumento());
			}
			
		} catch (FindException e) {
			logger.error(e);
		}
		
	}
//
//	public void onRowEdit(RowEditEvent event) {
//		try {
//			ConDepositos conDeposito = (ConDepositos) event.getObject();
//			if (conDeposito.getCondicion() == null || conDeposito.getCondicion().isEmpty()) {
//				conDeposito.setCondicion("Documento "+conDeposito.getNumeroDocumento()+", Valor "+ conDeposito.getValorDocumento() + ", fecha "+ conDeposito.getFechaSolicitud());
//			}
//			conDepositosService.actualizarConDepositos(conDeposito);
//			addInfoMessage("Deposito Editado, Solicitud ", conDeposito.getId()+".");
//		} catch (UpdateException e) {
//			addErrorMessage("Error al guardar", e.getMessage());
//		}
//    }
//     
//    public void onRowCancel(RowEditEvent event) {
//    	addInfoMessage("Edición cancelada", ((ConDepositos) event.getObject()).getId()+".");
//    }
//     
    public void actualizar(ConDepositos conDeposito) {
    	logger.info("actualizar...");
    	try {
    		List<ConDepositos> lsolDeposito = conDepositosService.validaConDepositos(getCompania().getNoCia(),conDeposito.getNumeroDocumento(), conDeposito.getBanco());		    
		    
			if (lsolDeposito.size() >0 ){ 
				//SI el codigo de la solicitud es diferente
				if(conDeposito.getId() != lsolDeposito.get(0).getId()) {
					String estado = "SOLICITADO";
					if(lsolDeposito.get(0).getEstado().equals("R")) {
						estado = "RECHAZADO. No. Solicitud: " + lsolDeposito.get(0).getId();
					}else if(lsolDeposito.get(0).getEstado().equals("P")) {
						estado = "CONFIRMADO";
					}	
					addErrorMessage("","Ya existe ese número de documento para este banco en estado " + estado);
					return;
				}
			}
			conDepositosService.actualizarConDepositos(conDeposito);
			actualizarRevisionArcjDep(conDeposito);
		} catch (UpdateException e) {
			addErrorMessage("Error al guardar", e.getMessage());
		}
    }

private void actualizarRevisionArcjDep(ConDepositos conDeposito) {
	try {
		String noFisicoMap=documentoConDepositosMap.get(conDeposito.getId());
		String noFisico=noFisicoMap==null?conDeposito.getNumeroDocumento():noFisicoMap;
		if(conDeposito.getNumeroDocumento()!=null && !conDeposito.getNumeroDocumento().equals(noFisicoMap)) {
			List<Arcjdep> arcjdepLst = arcjdepService.consultarPorDocumento(conDeposito.getNoCia(), conDeposito.getBanco(), noFisico);
			if(!arcjdepLst.isEmpty()) {
				Arcjdep arcjdep = arcjdepLst.get(0);
				arcjdep.setUsuarioRevision(getUsuario().getUsuario());
				arcjdep.setFechaRevision(new Date());
				arcjdepService.actualizarPorPk(arcjdep);
			}
		}
	} catch (Exception e) {
		addErrorMessage("Error al actualizar Revision Deposito", e.getMessage());
		logger.error("Error al actualizar Revision Deposito"+ e.getMessage(), e);
	}
}
 
    public void anularSolicitud(ConDepositos conDepositos){
    	logger.info("anularSolicitud...");
    	try {
    		boolean anular = true;
    		if (CommonConstants.CASABACA.equals(getCompania().getNoCia()) 
    			|| CommonConstants.SUZUKI.equals(getCompania().getNoCia())
    			|| CommonConstants.COSTA1001.equals(getCompania().getNoCia())
    			|| CommonConstants.EPICENTRO.equals(getCompania().getNoCia())) {
    			if(conDepositos.getCondicion() == null || conDepositos.getCondicion().trim().isEmpty()) {
        			addErrorMessage("", "Ingrese una observación o motivo de anulación.");
        			anular = false;
    			}
    		}
    		if(anular) {
	    		conDepositos.setEstado("A");
	    		conDepositos.setFechaAnulado(new Date());;
				conDepositosService.actualizarConDepositos(conDepositos);
		
			    addInfoMessage("Solicitud Anulada", "Solicitud "+ conDepositos.getId());
			    
			    this.envioMail(conDepositos);
			    
			    this.obtieneDepositos();
    		}
			
		} catch (UpdateException e) {
			addErrorMessage("Error al guardar", e.getMessage());
		}
    }
    public void procesarRechazo(ConDepositos solDeposito) {
		logger.info("procesarRechazo...");
		boolean rechazar = true;
		//if (CommonConstants.CASABACA.equals(getCompania().getNoCia()) || CommonConstants.SUZUKI.equals(getCompania().getNoCia())) {
		if (this.empresaApro) {
			if(solDeposito.getCondicion() == null || solDeposito.getCondicion().trim().isEmpty()) {
    			addErrorMessage("", "Ingrese una observación o motivo de rechazo.");
    			rechazar = false;
			}
		}
		try {

			if(rechazar) {
				solDeposito.setEstado("R");
				solDeposito.setFechaRechazado(new Date());
				conDepositosService.actualizarConDepositos(solDeposito);
				
				//Enviar un email al JEFE DE PRODUCTO VEHICULOS, en caso de tener una reserva de un veh con esa solicitud
				VehHistorialReservacion histReservacion = consultarReservacionPorSolicitud(solDeposito.getId());				
					
				try {
					// enviar mail al asesor, usuario confirma 
					StringBuffer mensaje = new StringBuffer();
					SisMailServidores sisMailServidores = getSisMailServidores(solDeposito.getNoCia());
					this.preparaMensajeRechazo(solDeposito, mensaje, histReservacion);
					
					String nombreBanco = solDeposito.getNombreBanco();
					// MAIL al usuario que solicita(asesor)
					UsuarioSis usuarioSolicita = usuarioServiceLocal.findByPk(solDeposito.getUsuario());
					mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(), usuarioSolicita.getEmail(),
							"Se ha rechazado el depósito o transferencia con el banco "+ nombreBanco, mensaje);
					addInfoMessage("ATENCION : Se envio el mail de rechazo al solicitante con exito", "");
					// MAIL al usuario que rechaza la solicitud de confirmacion
					mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(), getUsuario().getEmail(),
							"Se ha rechazado el depósito o transferencia con el banco "+ nombreBanco, mensaje);
					//Mail a jefe de vehiculos y al jefe inmediato del Asesor que registró la solicitud
					if(histReservacion != null) {
						logger.info("La solicitud fue usada en una reserva...");
						List<Object[]> usuariosMail = null;
						//Grupo RSNxx Nuevos, RSSxx Seminuevos, Usuario Jefe de Producto Vehiculos
						if(histReservacion.getLineaNegocio() != null && histReservacion.getLineaNegocio().equals(ConstantesVehiculos.VEHICULOS_NUEVOS)) {
							usuariosMail = mailUsuarioService.findByEmailGrupo("RSN"+getCompania().getNoCia());
						}else {
							usuariosMail = mailUsuarioService.findByEmailGrupo("RSS"+getCompania().getNoCia());
						}
						if(usuariosMail != null && !usuariosMail.isEmpty()) {
							for (Object[] objects : usuariosMail) {				    
								mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),(String)objects[1],
										"Se ha rechazado el depósito o transferencia con el banco "+ nombreBanco, mensaje);
							}
						}
						String mailJefeDeAsesor = usuarioServiceLocal.mailUsuario(this.nominaEmpleadosServiceLocal
														.getEmpleado(getCompania().getNoCia(), usuarioSolicita.getNoEmple()).getJefe());
						if(mailJefeDeAsesor != null) {
							mailService.sendEmailInHtmlNoCia(sisMailServidores, getUsuario().getEmail(),mailJefeDeAsesor,
									"Se ha rechazado el depósito o transferencia con el banco "+ nombreBanco, mensaje);
						}
					}
										
				} catch (Exception e) {
					Log.error(e);
				}
	
				this.obtieneDepositos();
			}
		} catch (UpdateException e) {
			addErrorMessage("ERROR AL RECHAZAR ", e.getDetail());
			e.printStackTrace();
		}catch (Exception e) {
			addErrorMessage("ERROR AL CONFIRMAR ", e.getMessage());
			e.printStackTrace();
		} 	
	}
    private VehHistorialReservacion consultarReservacionPorSolicitud(Long idSolicitud) {
		List<VehHistorialReservacion> historialReservaList = vehHistorialReservacionServiceLocal.findReservacionesBySolicitud(
				getCompania().getNoCia(), idSolicitud);
		VehHistorialReservacion histReservacion = null;
		if(historialReservaList != null && !historialReservaList.isEmpty()) {
			histReservacion = historialReservaList.get(0);
		}
		return histReservacion;
	}
    private void preparaMensajeRechazo(ConDepositos solDeposito, StringBuffer mensaje, VehHistorialReservacion histReservacion) {
		mensaje.append("Se ha Rechazado la solicitud de depósito o transferencia.<br><br>");
		mensaje.append("<table border='1'><tr><td><b>Solicitud No </b></td><td>"
				+  solDeposito.getId() + "</td></tr>");
		mensaje.append("<tr><td><b>Fecha de depósito</b></td> <td> "				
				+ new SimpleDateFormat("dd/MM/yyyy").format(solDeposito.getFechaDocumento()) 
				+ "</td></tr>");
		mensaje.append("<tr><td><b>Fecha de Rechazo</b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(solDeposito.getFechaRechazado()) + "</td></tr>");
		mensaje.append("<tr><td><b>Documento</b></td> <td> "
				+ solDeposito.getNumeroDocumento() + "</td></tr>");
		mensaje.append("<tr><td><b>Valor</b></td> <td> "
				+ solDeposito.getValorDocumento() + "</td></tr>");		
		mensaje.append("<tr><td><b>Cédula</b></td> <td> "
				+ solDeposito.getCedula() + "</td></tr>");
		mensaje.append("<tr><td><b>Nombre</b></td> <td> "
				+ solDeposito.getNombreCliente() + "</td></tr>");
		mensaje.append("<tr><td><b>Observacion</b></td> <td> "
				+ solDeposito.getCondicion() + "</td></tr>");
		if(histReservacion != null) {
			VendedorServicioDelegate vendedorServiceDelegate = new VendedorServicioDelegate();
			Vendedor vendedor = null;
			try {
				vendedor = vendedorServiceDelegate.findByPK(new VendedorPK(histReservacion.getNoCia(), histReservacion.getReservadoPor()));
			} catch (FindException e) {
				logger.error(e);
			}
			String placa = histReservacion.getPlaca() != null?", placa:"+histReservacion.getPlaca():"";
			String nombreVendedor = ", reservado por " + (vendedor != null?vendedor.getSvvenomb():histReservacion.getReservadoPor());
			mensaje.append("<tr><td><b>Reserva vehículo</b></td> <td> "
					+ histReservacion.getChasis() + placa + nombreVendedor + "</td></tr>");
		}
		mensaje.append("</table>");
		mensaje.append("<br><br>POR FAVOR ACTUALIZAR LA SOLICITUD.<br><br>");
		
	}
    
    public void verImagenes(ConDepositos conDepositos) {
    	this.urlImagen = //"/images/dynamic?file="+
    			conDepositos.getPathArchivo();		
	}    

    public void verImagenesAutorizacion(ConDepositos conDepositos) {
    	this.urlPathArchivoTercero = //"/images/dynamic?file="+
    			conDepositos.getPathArchivoTercero();		
	}   
    
	 public void descargarImagen(String imgUrl){
    	String path = System.getProperty("file.separator")+"TEMP"
				+System.getProperty("file.separator")
				+imgUrl; 
    	try{
            URL url = new URL(path);
            ByteArrayOutputStream out;
            try (InputStream in = new BufferedInputStream(url.openStream())) {
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1!=(n=in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            }           

        } catch(IOException ex){
            ex.printStackTrace(System.out);
        }
	 } 

	 public void seeFile() throws IOException {
		FileUpload fileUpload = new FileUpload();
		if (null != urlImagen) {
			fileUpload.seeFile(FacesContext.getCurrentInstance(),
					urlImagen);
		} else {
			addErrorMessage("No se ha subido ningún archivo al servidor", "");
		}
	 }	 

		public void seeFileAutorizacionTerceros() throws IOException {
			FileUpload fileUpload = new FileUpload();
			if (null != urlPathArchivoTercero) {
				fileUpload.seeFile(FacesContext.getCurrentInstance(), urlPathArchivoTercero);
			} else {
				addErrorMessage("No se ha subido ningún archivo al servidor", "");
			}
		}
	 
	 private void envioMail(ConDepositos conDepositos){	 
		 // enviar mail al asesor que solicitó la confirmación
		StringBuffer mensaje = new StringBuffer();
		mensaje.append("Se ha Anulado la solicitud de depósito o transferencia.<br><br>");
		mensaje.append("<table border='1'><tr><td><b>Solicitud No </b></td><td>"
				+  conDepositos.getId() + "</td></tr>");
		mensaje.append("<tr><td><b>Fecha de depósito</b></td> <td> "				
				+ new SimpleDateFormat("dd/MM/yyyy").format(conDepositos.getFechaDocumento()) 
				+ "</td></tr>");
		mensaje.append("<tr><td><b>Fecha de Anulación</b></td> <td> "
				+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(conDepositos.getFechaAnulado()) + "</td></tr>");
		mensaje.append("<tr><td><b>Documento</b></td> <td> "
				+ conDepositos.getNumeroDocumento() + "</td></tr>");
		mensaje.append("<tr><td><b>Valor</b></td> <td> "
				+ conDepositos.getValorDocumento() + "</td></tr>");		
		mensaje.append("<tr><td><b>Cédula</b></td> <td> "
				+ conDepositos.getCedula() + "</td></tr>");
		mensaje.append("<tr><td><b>Nombre</b></td> <td> "
				+ conDepositos.getNombreCliente() + "</td></tr>");
		mensaje.append("<tr><td><b>Observación</b></td> <td> "
				+ conDepositos.getCondicion() + "</td></tr>");
		mensaje.append("</table>");
		mensaje.append("<br><br>POR FAVOR INGRESAR OTRA SOLICITUD.<br><br>");
		
		try {
			// MAIL al usuario que solicita
			UsuarioSis usuarioSolicita = null;			
			try {
				usuarioSolicita = usuarioServiceLocal.findByPk(conDepositos.getUsuario());
			} catch (FindException e) {
				e.printStackTrace();
			}
			if(usuarioSolicita != null) {
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(conDepositos.getNoCia()), getUsuario().getEmail(), usuarioSolicita.getEmail(), 
						"Se ha anulado el deposito o transferencia con el banco", mensaje, true);
			}
		} catch (GeneralException e) {
			e.printStackTrace();
		}
		
	}
	 public void procesarEsperaConfirmacionBanco(ConDepositos solDeposito) {
		logger.info("procesarEsperaConfirmacion...");
		boolean esperaConfirma = true;
		if (this.empresaApro) {
			if(solDeposito.getCondicion() == null || solDeposito.getCondicion().trim().isEmpty()) {
	   			addErrorMessage("", "Ingrese una observación o motivo de CONFIRMACION PENDIENTE BANCO.");
	   			esperaConfirma = false;
			}
		}
		try {
			if(esperaConfirma) {
				solDeposito.setEstado("B");
				solDeposito.setFechaNoIdentificada(new Date());
				conDepositosService.actualizarConDepositos(solDeposito);
				this.obtieneDepositos();
				}
			} catch (UpdateException e) {
				addErrorMessage("ERROR CONFIRMACION BANCO ", e.getDetail());
				e.printStackTrace();
			}catch (Exception e) {
				addErrorMessage("ERROR CONFIRMACION BANCO ", e.getMessage());
				e.printStackTrace();
			} 	
		}
		
	public List<ConDepositos> getListConDepositos() {
		return listConDepositos;
	}
	public void setListConDepositos(List<ConDepositos> listConDepositos) {
		this.listConDepositos = listConDepositos;
	}
	public String getUrlImagen() {		
		return urlImagen;
	}

	public void setUrlImagen(String urlImagen) {
		this.urlImagen = urlImagen;
	}

	public boolean isEmpresaApro() {
		return empresaApro;
	}

	public void setEmpresaApro(boolean empresaApro) {
		this.empresaApro = empresaApro;
	}

	public String getUrlPathArchivoTercero() {
		return urlPathArchivoTercero;
	}

	public void setUrlPathArchivoTercero(String urlPathArchivoTercero) {
		this.urlPathArchivoTercero = urlPathArchivoTercero;
	}
	
	
}
