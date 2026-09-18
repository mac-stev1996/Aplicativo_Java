/**
 * 
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.StringUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.InventarioMaster;
import com.casabaca.common.ejb.model.InventarioMasterPK;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.model.LineaNegocioPK;
import com.casabaca.common.ejb.model.MasterVehiculo;
import com.casabaca.common.ejb.model.MasterVehiculoPK;
import com.casabaca.common.ejb.model.ParamCab;
import com.casabaca.common.ejb.model.Vendedor;
import com.casabaca.common.ejb.model.VendedorPK;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.InventarioMasterServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.service.MasterVehiculoServicioLocal;
import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.service.NativeSMSServiceLocal;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.service.VendedorServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.modelo.Cotizacion;
import com.casabaca.cxc.ejb.modelo.CotizacionPK;
import com.casabaca.cxc.ejb.servicio.CotizacionServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;
import com.casabaca.vehiculos.ejb.servicio.TipoPorLineaFinanciamientoServicioLocal;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * @author ce_gomez
 *
 */
@ManagedBean
@ViewScoped
public class CxcProDetSolcreController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;
	
	//para buscador IT
	private String itBuscadorNumCoti;
	private List<Cotizacion> itlistCotizaciones;
	private Cotizacion itCotizacionSeleccionada;
	private List<String> agenciasArrayString;
	
	//para buscador no IT
	private String buscadorNumCoti;
	private List<Cotizacion> listCotizaciones;
	private Cotizacion cotizacionSeleccionada;
	private List<String> agenciasArrayStringNoIT;
	
	private Boolean mostrarTabAprobacion;
	private int tabActiveIndex;
	
	private List<SelectItem> agenciasSelectItems;
	private boolean enviarSms;
	
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.COTIZACION_SERVICE)
	private CotizacionServiceLocal cotizacionService;
	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal cxcNativeService;
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;
	@EJB(lookup = NombreJNDI.VENDEDOR_SERVICIO_BEAN)
	private VendedorServicioLocal vendedorService;
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;
	@EJB(lookup = NombreJNDI.NATIVE_SMS_SERVICE_BEAN)
	private NativeSMSServiceLocal nativeSMSService;
	@EJB(lookup = NombreJNDI.INVENTARIO_MASTER_SERVICE)
	private InventarioMasterServiceLocal inventarioMasterService;
	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioService;
	@EJB(lookup = NombreJNDI.MASTER_VEHICULO_SERVICIO_BEAN)
	private MasterVehiculoServicioLocal masterVehiculoService;
	
	@EJB(lookup = NombreJNDI.TIPO_POR_LINEA_FINANCIAMIENTO_SERVICIO_BEAN)
	private TipoPorLineaFinanciamientoServicioLocal tipoPorLineaFinanciamientoService;
	
	protected NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;
	
	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabServiceLocal;
	
	
	private Map<String, File> archivosAdjuntos;
	private List<String> listaAdjuntos;
	private boolean activarGuardar;
	
	// Constantes para verificar tipo de archivo
	public static final String DOC_FILE = ".doc";
	public static final String PDF_FILE = ".pdf";
	public static final String XLS_FILE = ".xls";
	// Constantes para setear la aplicacion en el header
	public static final String XLS_APPLICATION = "application/vnd.ms-excel";
	public static final String PDF_APPLICATION = "application/pdf";
	public static final String DOC_APPLICATION = "application/msword";
	
	public CxcProDetSolcreController() throws ServiceLocatorException {
		nativeDmlDatabaseService = (NativeDmlDatabaseServiceLocal) ServiceLocator
				.getService(com.casabaca.common.ejb.util.NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN);
	}
	public List<SelectItem> getAgenciasSelectItems() {
		if (agenciasSelectItems == null) {
			List<Agencia> agenciasList = new ArrayList<Agencia>();
			String[] arr= {"a.nombre"};
			agenciasList = agenciaService.buscarAgencias(getCompania().getNoCia(), null, arr);
			agenciasSelectItems = new ArrayList<SelectItem>();
			if (agenciasList != null && agenciasList.size() > 0) {
				//consulto si el usuario conectado es administrador de repuestos
				for (Agencia agencia : agenciasList) {
						agenciasSelectItems.add(new SelectItem(agencia.getAgenciaPK().getCodigo(), agencia.getNombre(), agencia.getNombre(),false));
				}
			}
		}
		return agenciasSelectItems;
	}
	public void setAgenciasSelectItems(List<SelectItem> agenciasSelectItems) {
		this.agenciasSelectItems = agenciasSelectItems;
	}
	
	public void buscarCotizacionesIT(){
		try {
			setItlistCotizaciones(
					cotizacionService.findByNociaAprobadocreditoTipoFinanciacionGreaterThanNosolicitudcredCuotadealcanceNullNofisicoInCentrosLikeNumeroCotizacion(
							getCompania().getNoCia(), 
							CommonConstants.NO_STRING_VALUE, 
							"IT", 
							Integer.valueOf(0), 
							0d, 
							agenciasArrayString, 
							Integer.valueOf((null==itBuscadorNumCoti ||itBuscadorNumCoti.compareTo("")==0 )?"0":itBuscadorNumCoti),
							getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario()
							)
					
					);
			this.itCotizacionSeleccionada = null;
		} catch (NumberFormatException | FindException e) {
			e.printStackTrace();
		}
	}
	public void seleccionarCreditoIT(Cotizacion cre){
		this.itCotizacionSeleccionada = cre;
		listaAdjuntos = new ArrayList<>();
		archivosAdjuntos = new HashMap<>();
	}
	
	public void aprobarCotizacionIT(){
		Cotizacion cre = this.itCotizacionSeleccionada;
		
		try {
			//1. Cambio el estado del credito a aprobado
			if(null!=cre.getComentarioAprobCXC() && cre.getComentarioAprobCXC().length()>200){
				cre.setComentarioAprobCXC(cre.getComentarioAprobCXC().substring(0,200));
			}
			String update = cxcNativeService.updateAprobadocreditoComentarioaprobcxcOfSvfcotiByPK("S", cre.getComentarioAprobCXC(), cre.getCotizacionPK());
			if(update.compareTo("ok")==0){
				//info("Se aprobó el crédito de la cotización No. "+cre.getCotizacionPK().getNumeroCotizacion()+" de Importadora Tomebamba");
				info("Se guardó el comentario en la cotización No. "+cre.getCotizacionPK().getNumeroCotizacion()+" de Importadora Tomebamba");
				cre.setAprobadoCredito("S");
				//2. Envio mails a mafernanda.arellano@tomebamba.com.ec, mortiz@casabaca.com, eviteri@casabaca.com --> grupo CCAIT - Cuentas x Cobrar Aprobacion Importadora Tomebamba
				enviarMailAprobacionIT(cre);
				info("En envió el correo al grupo de Importadora Tomebamba (CCAIT)");
				itCotizacionSeleccionada=null;
				
			}else{
				error("Error al actualizar: " + update);
			}
			
		} catch (UpdateException e) {
			e.printStackTrace();
		}
		
	}

	//2. Envio mails a mafernanda.arellano@tomebamba.com.ec, mortiz@casabaca.com, eviteri@casabaca.com --> grupo CCAIT - Cuentas x Cobrar Aprobacion Importadora Tomebamba
	public void enviarMailAprobacionIT(Cotizacion cre){
		try {
			StringBuffer mensaje = new StringBuffer("Estimados Sres ITSA, <br/>");
			mensaje.append("Les indicamos que la cuota de alcance de la cotización No. " + String.valueOf(cre.getCotizacionPK().getNumeroCotizacion()));
			mensaje.append(" por el cliente " + cre.getNombreDeudor()+" por el valor de US" + StringUtils.formatearNumeroADolares(cre.getCuotaDeAlcance()) );
			mensaje.append(" se encuentra aprobado con las siguientes condiciones:<br/><br/>");
			mensaje.append(cre.getComentarioAprobCXC());
			mensaje.append("<br/><br/><br/>");
			mensaje.append("Saludos cordiales<br/>");
			mensaje.append("Departamento Credito<br/>");
			mensaje.append(getCompania().getNombre());
			
			mailService.sendMailToGroup("CCAIT", getUsuario().getEmail(), "Aprobación de Cotización " + String.valueOf(cre.getCotizacionPK().getNumeroCotizacion()) + " con Cuota de Alcance", mensaje, true);
			
		} catch (GeneralException e) {
			e.printStackTrace();
		} catch (FindException e) {
			e.printStackTrace();
		}
	}
	
	
	public void buscarCotizaciones(){
		try {
			setListCotizaciones(
					cotizacionService.findByNociaAprobadocreditoAplicascGreaterThanNosolicitudcredNullNofisicoDifTipoFinanciacionInCentrosLikeNumeroCotizacion
					(		getCompania().getNoCia(), 
							CommonConstants.NO_STRING_VALUE, 
							CommonConstants.NO_STRING_VALUE, 
							Integer.valueOf(0), 
							"IT", 
							agenciasArrayStringNoIT, 
							Integer.valueOf((null==buscadorNumCoti ||buscadorNumCoti.compareTo("")==0 )?"0":buscadorNumCoti),
							getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario()
					)
					);
		} catch (NumberFormatException | FindException e) {
			e.printStackTrace();
		}
	}
	public void seleccionarCredito(Cotizacion cre){
		try {
			listaAdjuntos = new ArrayList<>();
			archivosAdjuntos = new HashMap<>();
			this.cotizacionSeleccionada = cotizacionService.findByPK(cre.getCotizacionPK());
			activarGuardar=true;
			if(cotizacionSeleccionada !=  null && cotizacionSeleccionada.getAprobadoCredito() != null
					&& cotizacionSeleccionada.getAprobadoCredito().equals("S")){
				activarGuardar=false;
			}
			setMostrarTabAprobacion(true);
			setTabActiveIndex(1);
			RequestContext context = RequestContext.getCurrentInstance();
			context.execute("focoAprobacion();");
		} catch (FindException e) {
			e.printStackTrace();
		}
	}
	public void aprobarCotizacion() throws UpdateException, FindException, GeneralException{
		Cotizacion cot = this.cotizacionSeleccionada;
		//1. Actualizo la cotizacion
		if(null!=cot.getComentarioAprob() && cot.getComentarioAprob().length()>200){
			cot.setComentarioAprob(cot.getComentarioAprob().substring(0,200));
		}
		cot.setComentarioAprobCXC(cot.getComentarioAprob());//Ahora se aprueban directamente los creditos de Importadora Tomebamba
		Date hoy = new Date();
		cot.setFechaAprobCred(new Timestamp(hoy.getTime()));
		boolean control = true;
		//Si esta aprobado el credito y pertenece a la compania 01
		if("S".equals(cot.getAprobadoCredito()) && CommonConstants.CASABACA.equals(getCompania().getNoCia())) {
			//Obtengo el porcentaje minimo de entrada para la aprobacion de IFI parametrizado en las tasas de financiacion
			BigDecimal entradaRequerida= tipoPorLineaFinanciamientoService.obtenerEntradaMinimaAprobacionIFI(getCompania().getNoCia(), cot.getTipoFinanciacion(), String.valueOf(cot.getSvlicodi()));
			if (entradaRequerida != null) {

				if (cot.getSaldoFinanciar() == null) {
					cot.setSaldoFinanciar(0D);
				}
				if (cot.getCuotaDeAlcance() == null) {
					cot.setCuotaDeAlcance(0D);
				}
				if (cot.getCuotaDeEntrada() == null) {
					cot.setCuotaDeEntrada(0D);
				}
				BigDecimal total = new BigDecimal(cot.getSaldoFinanciar().doubleValue())
						.add(new BigDecimal(cot.getCuotaDeAlcance().doubleValue()))
						.add(new BigDecimal(cot.getCuotaDeEntrada().doubleValue()));
				// Calculo el porcentaje de entrada que se encuentra en la cotizacion
				BigDecimal entrada = new BigDecimal(cot.getCuotaDeEntrada()).multiply(new BigDecimal(100)).divide(total,
						2, BigDecimal.ROUND_HALF_UP);
				//Si el porcentaje de entrada es menor al requerido, despliega error
				if(entrada.compareTo(entradaRequerida) < 0){
					control = false;
					addErrorMessage("Error", "El porcentaje de la entrada "+entrada+"% es menor al porcentaje minimo de "+entradaRequerida+"% requerido para la aprobacion ");
				}
			}
			
			//si no tiene adjuntos muestra error
			if((listaAdjuntos == null || listaAdjuntos.isEmpty() ) && !"CB".equals(cot.getTipoFinanciacion())){
				control = false;
				addErrorMessage("Error", "Para poder procesar la aprobacion obligatoriamente debe subir al menos un archivo adjunto ");
			}
		}	
		
			if(cot.getAprobadoCredito().compareTo("N")==0 && cot.getObservacion() == null) {
				control = false;
				addErrorMessage("Error", "Es necesario escoger un motivo de Negación.");
			}
			if (control)
			{
				try {
					StringBuffer sql = new StringBuffer();
					HashMap parametros = new HashMap();
					sql.append("update SVFCOTI ");
					sql.append("set ");
					sql.append("APROBADO_CREDITO = :aprobCred, ");
					sql.append("FECHA_APROB_CRED = :fechaaprobCred, ");
					sql.append("COMENTARIO_APROB = :comentarioAprob, ");
					sql.append("COMENTARIO_APROBCXC = :comentarioAprobcxc, ");
					sql.append("OBSERVACION = :motivoNegacion ");
					sql.append("where NO_CIA = :noCia ");
					sql.append("and CENTRO = :centro ");
					sql.append("and NUMERO_COTIZACION = :numeroCotizacion ");
					
					parametros.put("aprobCred", cot.getAprobadoCredito());
					parametros.put("fechaaprobCred", new Date());
					parametros.put("comentarioAprobcxc", cot.getComentarioAprobCXC());
					parametros.put("comentarioAprob", cot.getComentarioAprob());
					parametros.put("motivoNegacion", cot.getObservacion());
		
		
					parametros.put("noCia", getCompania().getNoCia());
					parametros.put("centro", cot.getCotizacionPK().getCentro());
					parametros.put("numeroCotizacion", cot.getCotizacionPK().getNumeroCotizacion());
					
					nativeDmlDatabaseService.nativeSentenceAdvanced(sql, parametros);
				}	
				catch (Exception e) {
					addErrorMessage("Error", "No se ha actualizado la aprobación correctamente");	
				}
				
				if(cot.getAprobadoCredito().compareTo("S")==0){
					info("La cotización "+cot.getCotizacionPK().getNumeroCotizacion()+" ha sido APROBADA");
					activarGuardar=false;
				}else if(cot.getAprobadoCredito().compareTo("N")==0){
					info("La cotización "+cot.getCotizacionPK().getNumeroCotizacion()+" ha sido NEGADA");
				}else if(cot.getAprobadoCredito().compareTo("B")==0){
					info("La cotización "+cot.getCotizacionPK().getNumeroCotizacion()+" ha sido DADA DE BAJA");
				}
				//2. Envio mail al usuario que registro la cotizacion
				enviarMailAprobacionUsuario(cot);
				//3. Envio mail a jefa de credito
				enviarMailAJefaDeCredito(cot);
				//4. Envio SMS al cliente notificando que el credito fue aprobado
				if(enviarSms){
					Cliente cliente = new Cliente();
					try {
						cliente = clienteService.buscarPorCedulaNoCia(
								cot.getCedulaDueno()
								, cot.getCotizacionPK().getNoCia());
					} catch (FindException e) {
						e.printStackTrace();
					}
					if(cot.getAprobadoCredito().compareTo("S")==0 || cot.getAprobadoCredito().compareTo("N")==0){
						if(null!=cliente.getTelefono() ){
							String mensaje="";
							if(cot.getAprobadoCredito().compareTo("S")==0){
								mensaje="Estimado/a cliente, su solicitud de cuota de alcance/credito con "+getCompania().getNombre()+
										//" Requiere la siguiente documentación:  "+cot.getComentarioAprob() +
										" ha sido aprobada" +
										", favor comuniquese con su asesor comercial";
							}else if(cot.getAprobadoCredito().compareTo("N")==0){
								mensaje="Estimado/a cliente, su solicitud de cuota de alcance/credito con "+getCompania().getNombre()+
										//" Presenta observaciones"+cot.getComentarioAprob()+
										" Presenta observaciones"+
										", favor comuniquese con su asesor comercial";
								
								// Envio de mail a CERTERO
								
								if(cot.getObservacion().equals("CAPACIDAD_DE_PAGO")){
									InventarioMaster svfinven = new InventarioMaster();
									
									String modelo="";
									if(null!=cot.getSvincodi()){
										svfinven = inventarioMasterService.findByPK(new InventarioMasterPK(cot.getCotizacionPK().getNoCia(), cot.getSvincodi()));
										modelo = (null!=svfinven.getSvindesc()?svfinven.getSvindesc():"");
									}else{
										MasterVehiculo master = masterVehiculoService.findByPK(new MasterVehiculoPK(cot.getSfx(), cot.getSvmamaster(), cot.getCotizacionPK().getNoCia()));
										modelo = master.getSvmadescri();
									}
									
									
									StringBuffer mensajeNegacion = new StringBuffer();
									mensajeNegacion.append("Se ha NEGADO la siguiente solicitud de Credito en CB por Capacidad de PAGO. Por favor realizar su respectivo seguimiento<br><br>");
									mensajeNegacion.append("<table border='1'><tr><td><b>Cotizacion No </b></td><td>"
											+ cot.getCotizacionPK().getNumeroCotizacion() + "</td></tr>");
									
									mensajeNegacion.append("<tr><td><b>Modelo</b></td> <td> "
											+ modelo + "</td></tr>");
									mensajeNegacion.append("<tr><td><b>Cliente</b></td> <td> "
											+ cot.getApellidos() +" "+ cot.getNombres()+ "</td></tr>");
									mensajeNegacion.append("<tr><td><b>Valor</b></td> <td> "
											+ cot.getTotalGeneral() + "</td></tr>");
									mensajeNegacion.append("<tr><td><b>Entrada</b></td> <td> "
													+ cot.getCuotaDeEntrada() + "</td></tr>");
									mensajeNegacion.append("<tr><td><b>Cuota de Alcance</b></td> <td> "
															+ cot.getCuotaDeAlcance() + "</td></tr>");
									mensajeNegacion.append("<tr><td><b>Saldo Financiar</b></td> <td> "
																	+ cot.getSaldoFinanciar()+ "</td></tr>");
									mensajeNegacion.append("<tr><td><b>Valor Cuotas</b></td> <td> "
																			+ cot.getPeriodo()+ "</td></tr>");												
									mensajeNegacion.append("<tr><td><b>Número Cuota</b></td> <td> "
											+ cot.getCuotaMensual()+ "</td></tr>");
																	
									mensajeNegacion.append("</table>");
									mailService.sendMailToGroup("CERT", getUsuario()
											.getEmail(), "Solicitud de Crédito NEGADA por Capacidad de Pago", mensajeNegacion, true);
								}
							}
							
							/*String sms = nativeSMSService.llamarSpEnviarSMSWSRestLirela(getCompania().getNoCia(), cliente.getTelefono(), 
									mensaje
									);
							if(null!=sms && sms.compareTo("SMS enviado")==0){
								addInfoMessage("Exito", "Se a enviado un SMS al cliente ("+mensaje+")");
							}else{
								addErrorMessage("Error", "Ocurrio un error al enviar el SMS");
							}*/
							
						}else{
							addErrorMessage("Error", "No se ha enviado SMS al cliente debido a que no tiene registrado numero celular");
						}
				}
			}
				
				//5. Una vez guardada la cotizacion debe esconderse la pestana de aprobacion e ir a la pestano de cotizaciones NO IT
				setMostrarTabAprobacion(false);
				setTabActiveIndex(0);
				
				//6. en la pestana de cotizaciones NO IT debe aparecer deshabilitado el botón de edicion de la cotizacion
				//que se acaba de aprobar
				for(Cotizacion cotList : listCotizaciones){
					if(cotList.equals(cot)){
						cotList.setAprobadoCredito("S");
						break;
					}
				}
		}
		
			
		
	}
	
	//2. Envio mail al usuario que registro la cotizacion
	public void enviarMailAprobacionUsuario(Cotizacion cot){
		try {
			String accion="";
			if(null==cot.getAprobadoCredito()){
				accion="NEGADA";
			}else if(cot.getAprobadoCredito().compareTo("N")==0){
				accion="NEGADA";
			}else if(cot.getAprobadoCredito().compareTo("S")==0){
				accion="APROBADA";
			}else if(cot.getAprobadoCredito().compareTo("B")==0){
				accion="DADA DE BAJA";
			}
				
			StringBuffer mensaje = new StringBuffer("Estimado Usuario su cotizacion "+ String.valueOf(cot.getCotizacionPK().getNumeroCotizacion())+" <br/>");
			mensaje.append(" del Cliente " + cot.getApellidos()+" "+cot.getNombres()+" con Id. Tributario: "+cot.getCedulaDeudor()+"<br/>" );
			mensaje.append(" FUE "+accion);
			
			mensaje.append(" con las siguientes condiciones:<br/><br/>");
			mensaje.append(cot.getComentarioAprob());
			
			if(null!=getCompania() && null!=getCompania().getNoCia() && getCompania().getNoCia().compareTo(CommonConstants.CHANGAN)==0){
				//aqui hay q enviar al grupo de changan que reciben la notificacion de que el credito ha sido aprobado
				mailService.sendMailToGroup("05APC", getUsuario().getEmail(), "Automático - PROCESO APROBACIÓN DE COTIZACIÓN", mensaje, true);
			}else{
				Vendedor vend = vendedorService.getVendedor(cot.getCotizacionPK().getNoCia(), cot.getCodVendActual());
				UsuarioSis usuarioTo = usuarioSisService.findByPk(vend.getSvveusua());
				if(null!=usuarioTo.getEmail()){
					List<File> archivos = new ArrayList<File>();
					if(listaAdjuntos != null && !listaAdjuntos.isEmpty()) {
						//Agrego a la lista los archivos para enviar por mail 
						for (String key : archivosAdjuntos.keySet()) {
							archivos.add(archivosAdjuntos.get(key));
						}
					}
					mailService.sendEmailAttachmentsImgEmbebida(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(), getUsuario().getUsuario(), usuarioTo.getEmail(),
							usuarioTo.getNombre()
							 , "Automático - PROCESO APROBACIÓN DE COTIZACIÓN", mensaje, true, archivos,true, new ArrayList<String>(), null);

					info("Se ha enviado un mail al usuario "+usuarioTo.getNombre()+" ("+usuarioTo.getEmail()+")");
				}else{
					error("No se a podido enviar mail al usuario debido a que no tiene registro un email: "+usuarioTo.getNombre()+" ("+usuarioTo.getEmail()+")");
				}
			}
			
				
		} catch (GeneralException e) {
			e.printStackTrace();
		} catch (FindException e) {
			e.printStackTrace();
		}
	}
	
	public void enviarMailAJefaDeCredito(Cotizacion cot) {
		try {
			String accion="";
			if(null==cot.getAprobadoCredito()){
				accion="NEGADA";
			}else if(cot.getAprobadoCredito().compareTo("N")==0){
				accion="NEGADA";
			}else if(cot.getAprobadoCredito().compareTo("S")==0){
				accion="APROBADA";
			}else if(cot.getAprobadoCredito().compareTo("B")==0){
				accion="DADA DE BAJA";
			}
			if(accion.compareTo("NEGADA")==0 || accion.compareTo("APROBADA")==0){
				InventarioMaster svfinven = new InventarioMaster();
				LineaNegocio lineaNegocio = new LineaNegocio();
				Vendedor vendedor = new Vendedor();
				try {
					lineaNegocio = lineaNegocioService.findByPk(new LineaNegocioPK(String.valueOf(cot.getSvlicodi()), cot.getCotizacionPK().getNoCia()));
					vendedor = vendedorService.findByPK(new VendedorPK(cot.getCotizacionPK().getNoCia(), cot.getCodVendActual()));
				} catch (FindException e1) {
					error("Error al enviar correo a jefa de credito: " + this.obtainException(e1));
					e1.printStackTrace();
				}
				String modelo="";
				if(null!=cot.getSvincodi()){
					svfinven = inventarioMasterService.findByPK(new InventarioMasterPK(cot.getCotizacionPK().getNoCia(), cot.getSvincodi()));
					modelo = (null!=svfinven.getSvindesc()?svfinven.getSvindesc():"");
				}else{
					MasterVehiculo master = masterVehiculoService.findByPK(new MasterVehiculoPK(cot.getSfx(), cot.getSvmamaster(), cot.getCotizacionPK().getNoCia()));
					modelo = master.getSvmadescri();
				}
				
				StringBuffer mensaje = new StringBuffer("");
				mensaje.append("<html>");
				mensaje.append("	Estimad@ <br/> Le informamos que se ha procesado una cotizaci&oacute;n con los siguientes datos:<br/><br/>");
				mensaje.append("	<table bgcolor='#e6e6e6' border='1'>");
				mensaje.append("		<tbody>");
				mensaje.append("			<tr><th>N&uacute;mero</th><td>"+String.valueOf(cot.getCotizacionPK().getNumeroCotizacion())+"</td></tr>");
				mensaje.append("			<tr><th>Estado</th><td>"+accion+"</td></tr>");
				mensaje.append("			<tr><th>Cliente</th><td>"+(null!=cot.getApellidos()?cot.getApellidos():"")+" "+(null!=cot.getNombres()?cot.getNombres():"")+"</td></tr>");
				mensaje.append("			<tr><th>Identificaci&oacute;n</th><td>"+cot.getCedulaDeudor()+"</td></tr>");
				mensaje.append("			<tr><th>Asesor</th><td>"+(null!=vendedor.getSvvenomb()?vendedor.getSvvenomb():"")+"</td></tr>");
				mensaje.append("			<tr><th>Tipo Financiamiento</th><td>"+cot.getTipoFinanciacion() + " - " + cot.getTipoFinanciacionDescripcion()+"</td></tr>");
				mensaje.append("			<tr><th>Valor del auto</th><td>"+cot.getNeto().toString()+"</td></tr>");
				mensaje.append("			<tr><th>Modelo</th><td>"+modelo+"</td></tr>");
				mensaje.append("			<tr><th>Linea negocio</th><td>"+(null!=lineaNegocio.getDescripcion()?lineaNegocio.getDescripcion():"")+"</td></tr>");
				mensaje.append("			<tr><th>Cuota de entrada</th><td>"+cot.getCuotaDeEntrada().toString()+"</td></tr>");
				mensaje.append("			<tr><th>Cuota de alcance</th><td>"+cot.getCuotaDeAlcance().toString()+"</td></tr>");
				mensaje.append("			<tr><th>Comentario</th><td>"+cot.getComentarioAprob()+"</td></tr>");
				mensaje.append("			<tr><th>Realizada por usuario</th><td>"+getUsuario().getUsuario()+"</td></tr>");
				mensaje.append("		</tbody>");
				mensaje.append("	</table>");
				mensaje.append("</html>");
				

				if(null!=getCompania() && null!=getCompania().getNoCia() && getCompania().getNoCia().compareTo(CommonConstants.CHANGAN)==0){
					//aqui hay q enviar al grupo de changan que reciben la notificacion de que el credito ha sido aprobado
				}else{
					ParamCab parametro;
					try {
						parametro = paramCabServiceLocal.consultarParametro(cot.getCotizacionPK().getNoCia(), "JEFCXC");
					} catch (FindException e) {
						
						parametro = null;
					}
					
					UsuarioSis usuarioTo= null;
					
					if (parametro != null) {
						usuarioTo = usuarioSisService.findByPk(parametro.getTexto1());
					}
				
					if(usuarioTo!=null && null!=usuarioTo.getEmail()){
						
						List<File> archivos = new ArrayList<File>();
						if(listaAdjuntos != null && !listaAdjuntos.isEmpty()) {
							//Agrego a la lista los archivos para enviar por mail 
							for (String key : archivosAdjuntos.keySet()) {
								archivos.add(archivosAdjuntos.get(key));
							}
							
							//Borro la carpeta temporal creada
							String raizDiarios = System.getProperty("file.separator") + "opt"
									+ System.getProperty("file.separator") + "tempDocumentosAprCred";
							File folderRaiz = new File(raizDiarios);
							if (!folderRaiz.exists()) {
								folderRaiz.delete();
							}
						}
						mailService.sendEmailAttachmentsImgEmbebida(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(), getUsuario().getUsuario(), usuarioTo.getEmail(),
								usuarioTo.getNombre()
								 , "Jefe Credito - PROCESO APROBACIÓN DE COTIZACIÓN", mensaje, true, archivos,true, new ArrayList<String>(), null);
						
						info("Se ha enviado un mail al jefe de credito: "+usuarioTo.getNombre()+" ("+usuarioTo.getEmail()+")");
					}else{
						error("No se a podido enviar mail al usuario debido a que no tiene registro un email: "+usuarioTo.getNombre()+" ("+usuarioTo.getEmail()+")");
					}
				}

			}
		} catch (GeneralException e) {
			e.printStackTrace();
		} catch (FindException e) {
			e.printStackTrace();
		}
	}
	
	public void imprimeCotizacion(Cotizacion cot, String esIT){
		String format = "pdf";
		String reportPath="";
		reportPath = "cxcProDetSolcre";
		reportPath="/reportes/"+reportPath+".jasper";
		
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;

		try {
			Map<String, Object> parameters = new HashMap<String, Object>();

			String ctxPath = getServletContext().getRealPath("/");

			setParametersImprime(parameters, cot.getCotizacionPK(), esIT);
			connection = utilServiceDelegate.getDataSource()
					.getConnection();

			HttpServletResponse response = (HttpServletResponse) FacesContext
					.getCurrentInstance().getExternalContext()
					.getResponse();
			
			String path = System.getProperty("file.separator")
					+ FacesContext.getCurrentInstance().getExternalContext()
							.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			
			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath
					+ reportPath, parameters, connection); 
			
			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint,	//Este usamos siempre para mostrar en Pantalla
						response.getOutputStream());
				FacesContext.getCurrentInstance().responseComplete();			
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
			
			virtualizer.cleanup();
		} catch (Exception e) {
			e.getStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.getStackTrace();
			}
		}
	}
	
	private void setParametersImprime(Map<String, Object> parameters, CotizacionPK cotPK, String esIT)
			throws FindException {
		parameters.put("P_NO_CIA"				, cotPK.getNoCia());
		parameters.put("P_CENTRO"				, cotPK.getCentro());
		parameters.put("P_NUMERO_COTIZACION"	, cotPK.getNumeroCotizacion());
		parameters.put("P_NOMBRE_EMPRESA"		, getCompania().getNombre());
		parameters.put("P_ES_IT", esIT);
	}
	
	public void onTabChange(TabChangeEvent event) {
		setMostrarTabAprobacion(false);
	}
	
	public void irControlDocumentos (Cotizacion cotizacion) {	
		String pagina = "/cxc-web-prime/jsf/procesos/cxcControlDocumentos.jsf?nocia="+cotizacion.getCotizacionPK().getNoCia()
				+"&centro="+cotizacion.getCotizacionPK().getCentro()
				+"&cotizacion="+cotizacion.getCotizacionPK().getNumeroCotizacion();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(pagina);
		} catch (IOException e) {
			e.printStackTrace();
		}							
	}
	
	private String getTipoAplicacion(String fileName) {
		if (fileName != null) {
			if (fileName.indexOf(DOC_FILE) > 0) {
				return DOC_APPLICATION;
			}
			if (fileName.indexOf(XLS_FILE) > 0) {
				return XLS_APPLICATION;
			}
			if (fileName.indexOf(PDF_FILE) > 0) {
				return PDF_APPLICATION;
			}
		}
		return null;
	}
	
	public void verAdjunto(String nombreArchivo) {
		try {
			String destPath = null;
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			destPath = archivosAdjuntos.get(nombreArchivo).getAbsolutePath();

			File file = new File(destPath);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				byte[] fileBytes = file.getName().getBytes();

				StringBuffer header = new StringBuffer();
				header.append("filename=\"");
				header.append(nombreArchivo);
				header.append("\"");
				response.setHeader("Content-Disposition", header.toString());
				response.setContentType(getTipoAplicacion(nombreArchivo));
				response.setContentLength(fileBytes.length);
				response.setContentLength((int) length);
				while ((in != null) && ((length = in.read(buf)) != -1)) {
					response.getOutputStream().write(buf, 0, (int) length);
				}
				in.close();
				response.getOutputStream().close();

				ctx.responseComplete();
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void cargarArchivo(FileUploadEvent event) {
		try {
			UploadedFile uf = event.getFile();
			byte[] buffer = IOUtils.toByteArray(uf.getInputstream());
			InputStream is1 = new ByteArrayInputStream(buffer);
			procesarArchivo(is1, uf, false);
		} catch (IOException e) {
			e.printStackTrace();
			error(this.obtainException(e));
		}
	}
	private void procesarArchivo(InputStream is1, UploadedFile uf, boolean entrega) {
		try {
			//Si el archivo contiene caracter especial lo reemplaza
			String nombreArchivo = uf.getFileName();
			if(nombreArchivo.contains("Ñ")){
				nombreArchivo=nombreArchivo.replaceAll("Ñ","N");
			}
			if(nombreArchivo.contains("ñ")){
				nombreArchivo=nombreArchivo.replaceAll("ñ","n");
			}
			
			// Para pruebas en windows
			// Para el servidor Linux
			String raizDiarios =System.getProperty("file.separator") + "opt" + 
			System.getProperty("file.separator") + "tempDocumentosAprCred";
			
			File folderRaiz = new File(raizDiarios);
			if (!folderRaiz.exists()) {
				folderRaiz.mkdir();
			}
			File folderGeneral = new File(
					raizDiarios + System.getProperty("file.separator") + getCompania().getNoCia());
			if (!folderGeneral.exists()) {
				folderGeneral.mkdir();
			}
			raizDiarios = raizDiarios + System.getProperty("file.separator") + getCompania().getNoCia()
					+ System.getProperty("file.separator");
			File folder = new File(raizDiarios + cotizacionSeleccionada.getCotizacionPK().getCentro() + "_"
					+ cotizacionSeleccionada.getCotizacionPK().getNumeroCotizacion());
			if (!folder.exists()) {
				folder.mkdir();
			}
			raizDiarios =raizDiarios + cotizacionSeleccionada.getCotizacionPK().getCentro() + "_"
					+ cotizacionSeleccionada.getCotizacionPK().getNumeroCotizacion()
					+ System.getProperty("file.separator");

			File archivo = new File(raizDiarios + nombreArchivo);
			copyInputStreamToFile(is1, archivo);
			
			if(archivosAdjuntos == null){
				archivosAdjuntos = new  HashMap<>();
				listaAdjuntos = new ArrayList<>();
			}
			
			archivosAdjuntos.put(nombreArchivo, archivo);
			listaAdjuntos.add(nombreArchivo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void quitarArchivo(String nombreArchivo) {
		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos.remove(nombreArchivo);
	}


	private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

		try (FileOutputStream outputStream = new FileOutputStream(file)) {

			int read;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			// commons-io
			IOUtils.copy(inputStream, outputStream);
		}
	}

	
	public List<String> getAgenciasArrayString() {
		return agenciasArrayString;
	}
	public void setAgenciasArrayString(List<String> agenciasArrayString) {
		this.agenciasArrayString = agenciasArrayString;
	}
	
	public String getItBuscadorNumCoti() {
		return itBuscadorNumCoti;
	}
	public void setItBuscadorNumCoti(String itBuscadorNumCoti) {
		this.itBuscadorNumCoti = itBuscadorNumCoti;
	}
	
	public List<Cotizacion> getItlistCotizaciones() {
		return itlistCotizaciones;
	}
	public void setItlistCotizaciones(List<Cotizacion> itlistCotizaciones) {
		this.itlistCotizaciones = itlistCotizaciones;
	}
	
	public Cotizacion getItCotizacionSeleccionada() {
		return itCotizacionSeleccionada;
	}
	public void setItCotizacionSeleccionada(Cotizacion itCotizacionSeleccionada) {
		this.itCotizacionSeleccionada = itCotizacionSeleccionada;
	}
	
	public String getBuscadorNumCoti() {
		return buscadorNumCoti;
	}
	public void setBuscadorNumCoti(String buscadorNumCoti) {
		this.buscadorNumCoti = buscadorNumCoti;
	}
	
	public List<Cotizacion> getListCotizaciones() {
		return listCotizaciones;
	}
	public void setListCotizaciones(List<Cotizacion> listCotizaciones) {
		this.listCotizaciones = listCotizaciones;
	}
	
	public Cotizacion getCotizacionSeleccionada() {
		return cotizacionSeleccionada;
	}
	public void setCotizacionSeleccionada(Cotizacion cotizacionSeleccionada) {
		this.cotizacionSeleccionada = cotizacionSeleccionada;
	}

	public Boolean getMostrarTabAprobacion() {
		return mostrarTabAprobacion;
	}
	public void setMostrarTabAprobacion(Boolean mostrarTabAprobacion) {
		this.mostrarTabAprobacion = mostrarTabAprobacion;
	}
	
	public int getTabActiveIndex() {
		return tabActiveIndex;
	}
	public void setTabActiveIndex(int tabActiveIndex) {
		this.tabActiveIndex = tabActiveIndex;
	}

	public List<String> getAgenciasArrayStringNoIT() {
		return agenciasArrayStringNoIT;
	}
	public void setAgenciasArrayStringNoIT(List<String> agenciasArrayStringNoIT) {
		this.agenciasArrayStringNoIT = agenciasArrayStringNoIT;
	}

	public boolean isEnviarSms() {
		return enviarSms;
	}
	public void setEnviarSms(boolean enviarSms) {
		this.enviarSms = enviarSms;
	}
	
	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}
	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}
	public boolean isActivarGuardar() {
		return activarGuardar;
	}
	public void setActivarGuardar(boolean activarGuardar) {
		this.activarGuardar = activarGuardar;
	}
	
}