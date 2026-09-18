/* 
 * CxcBusquedaAnticipoClienteController.java 
 * 31 may. 2024
 * Copyright 2024 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.FileUpload;
import com.casabaca.common.NumericUtils;
import com.casabaca.common.ejb.model.Vendedor;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.VendedorServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcCabDevAnticipo;
import com.casabaca.cxc.ejb.modelo.CxcCabDevAnticipoPK;
import com.casabaca.cxc.ejb.modelo.CxcDetDevAnticipo;
import com.casabaca.cxc.ejb.modelo.CxcDetDevAnticipoPK;
import com.casabaca.cxc.ejb.servicio.CabDevAnticipoClienteServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcDevolucionSolAnticiposServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.Compania;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.CompaniaServiceLocal;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * <b> Descripcion de la clase, interface o enumeracion. </b>
 * 
 * @author laura.llangari
 * @version $1.0$
 */

@ManagedBean
@ViewScoped
public class CxcBusquedaAnticipoClienteController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Logger LOG = Logger.getLogger(CxcBusquedaAnticipoClienteController.class);

	/**
	 * 
	 */

	private List<Object[]> listaAnticiposPorCliente;
	private List<Object[]> listaAnticiposPorClientes;
	private String noCia;
	private String nbCliente;
	private String pCedula;
	private String pNombres;
	private String origen;
	private String asesor;
	private Integer secuencial;
	private BigDecimal valor;
	private List<Object[]> listaAnticiposSeleccionados;
	private List<Object[]> lista;
	private String aplicacajaChica;
	private Boolean verificacajaChica=Boolean.FALSE;
	private Double valorAutorizaCajaChica;
	private String necesitaAprobacion;
	private List<UsuarioSis> listaUsuarios;
	private UploadedFile file;
	private List<String> listaAdjuntos;
	private Map<String, String> archivosAdjuntos;
	private Map<String, List<String>> mapaArchivosCliente;
	private CxcCabDevAnticipo cabAnticipoDev;
	private List<CxcDetDevAnticipo> listaDetAnticipo;
	private List<Vendedor> listaVendedores;
	private BigDecimal valorTomar;
	private List<String> a;
	
	private static final String GRUPO_CORREO = "ADEVC";

	
	// EJB
	@EJB(lookup = NombreJNDI.CXC_CAB_DEV_ANTICIPO_SERVICE)
	private CabDevAnticipoClienteServiceLocal devAnticipoService;
	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_DEVOLUCIONES_SOLICITUDES_SERVICE)
	private CxcDevolucionSolAnticiposServiceLocal solicitudesService;

	@EJB(lookup = NombreJNDI.VENDEDOR_SERVICIO_BEAN)
	private VendedorServicioLocal vendedorService;
	
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;
	
	@EJB(lookup = NombreJNDI.COMPANIA_SERVICE_BEAN)
	private CompaniaServiceLocal   companiaServiceLocal;
	
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;


	public CxcBusquedaAnticipoClienteController() {

	}

	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author laura.llangari, 31 may. 2024]
	 * </p>
	 */
	@PostConstruct
	public void init() {
		
		this.noCia = getRequestParameter("noCia");
		this.pCedula = getRequestParameter("cedula");
		pNombres = getRequestParameter("nombre");
		nbCliente = getRequestParameter("codCliente");		
		limpiarDatos();
		consultarAnticipo();
		consultarValorCaja();
	}
	
	public void limpiarDatos() {
		this.listaAdjuntos = new ArrayList<>();
		this.archivosAdjuntos = new HashMap<>();
		this.asesor="";
		listaVendedores = new ArrayList<>();
		listaAnticiposSeleccionados = new ArrayList<>();
		this.necesitaAprobacion="N";
		this. a= new ArrayList<>();
		lista = new ArrayList<>();
		cabAnticipoDev = new CxcCabDevAnticipo(new CxcCabDevAnticipoPK());
		listaDetAnticipo = new ArrayList<>();
		secuencial=0;		
		
	}

	public void consultarAnticipo() {

		try {
			int index = 0;
			listaAnticiposPorClientes = new ArrayList<>();
			listaAnticiposPorCliente = new ArrayList<>();
			this.listaUsuarios=usuarioSisServiceLocal.getUsuarioList();
			listaAnticiposPorClientes = arccmdServiceLocal.obtenerAnticiporCliente(this.noCia, this.nbCliente);
			for (Object[] object : listaAnticiposPorClientes) {
				if (!("E").equals(object[10].toString())) {
					listaAnticiposPorCliente.add(listaAnticiposPorClientes.get(index));
				}
				index++;
			}
		} catch (Exception e) {
			LOG.error(e);
		}

	}

	public void agregarAnticiposSeleccionados() {
		listaDetAnticipo.clear();
		BigDecimal sumaValorAnticpos = BigDecimal.valueOf(0D);
		listaVendedores = vendedorService.getVendedores(this.noCia);
		if (!(this.listaAnticiposSeleccionados).isEmpty() && (valorTomar.compareTo(BigDecimal.ZERO) > 0)) {
			CxcCabDevAnticipoPK pk = new CxcCabDevAnticipoPK();
			pk.setNoCia(this.noCia);
			pk.setNoCliente(Long.parseLong(this.nbCliente));
			cabAnticipoDev.setId(pk);
			cabAnticipoDev.getId().setNoCliente(Long.parseLong(this.nbCliente));
			cabAnticipoDev.setValorTotal(this.valorTomar);
			cabAnticipoDev.setAgencia(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
			cabAnticipoDev.setEstado("I");
			cabAnticipoDev.setFechaProceso(new Date());
			cabAnticipoDev.setUsuarioProceso(getUsuario().getUsuario());
			cabAnticipoDev.setAsesor(this.asesor);
			cabAnticipoDev.setAprobacion(this.necesitaAprobacion);
			if (valorAutorizaCajaChica== 0) {
				verificacajaChica=Boolean.FALSE;
				cabAnticipoDev.setAplicaCajaChica("N");	
			}else {
				this.aplicacajaChica="S";
				verificacajaChica=Boolean.TRUE;
			}
			BigDecimal numeromMultiplicar= new BigDecimal(-1);
			for (Object[] object : listaAnticiposSeleccionados) {	
				String noDocuAnticipo = (String) object[7];
				BigDecimal montoAnticipo = NumericUtils.toBigDecimal(object[6]);
				BigDecimal anticipoTomar = NumericUtils.toBigDecimal(object[11]);			
				if (anticipoTomar.compareTo(montoAnticipo.multiply(numeromMultiplicar)) > 0) {
					addErrorMessage("Error", "El Valor a Tomar" + " " + anticipoTomar + " NO puede ser mayor al valor del anticipo"+ " " + montoAnticipo.multiply(numeromMultiplicar) );
					listaAnticiposSeleccionados.clear();
					accionesDialog("dlgNuevo", Boolean.FALSE);
					return;
				}
				
				
				listaDetAnticipo.add(construirDetalleAnticipo(noDocuAnticipo, montoAnticipo,anticipoTomar));
				sumaValorAnticpos = sumaValorAnticpos.add(montoAnticipo);
			}
			
			if (sumaValorAnticpos.compareTo(new BigDecimal(valorAutorizaCajaChica)) == 1) {				
				this.necesitaAprobacion="S";
			}
			accionesDialog("dlgNuevo", Boolean.TRUE);
		} else {
			error("Seleccionar anticipo a usar e ingresar el valor a tomar....");
			accionesDialog("dlgNuevo", Boolean.FALSE);
		}
	}

	private CxcDetDevAnticipo construirDetalleAnticipo(String documento, BigDecimal montoAnticipo, BigDecimal anticipoTomar) {
		CxcDetDevAnticipo detAnticipoDev = new CxcDetDevAnticipo(new CxcDetDevAnticipoPK());
		detAnticipoDev.setCxcCabDevAnticipo(cabAnticipoDev);
		CxcDetDevAnticipoPK pkdet = new CxcDetDevAnticipoPK();
		pkdet.setNoCia(this.noCia);
		pkdet.setNoCliente(Long.parseLong((this.nbCliente)));
		pkdet.setNoDocu(documento);
		detAnticipoDev.setId(pkdet);
		detAnticipoDev.setValor(montoAnticipo);
		detAnticipoDev.setValorTomar(anticipoTomar);
		return detAnticipoDev;
	}

	public BigDecimal obtenerSumaTotalAnticiposSeleccionados() {
		BigDecimal sumaValorAnticiposSeleccionados = BigDecimal.valueOf(0D);
		BigDecimal sumaValorAnticiposTomados = BigDecimal.valueOf(0D);
		BigDecimal valorAnticipos= BigDecimal.valueOf(0D);

		for (Object item : listaAnticiposSeleccionados) {
			if (((Object[]) item)[6] == null || ((Object[]) item)[11] ==null) {
				sumaValorAnticiposSeleccionados = sumaValorAnticiposSeleccionados.add(BigDecimal.valueOf(0D));
				sumaValorAnticiposTomados=sumaValorAnticiposTomados.add(BigDecimal.valueOf(0D));
			} else {
				sumaValorAnticiposSeleccionados = sumaValorAnticiposSeleccionados
						.add(((BigDecimal) ((Object[]) item)[6]).setScale(2, RoundingMode.HALF_UP));
				String valorAnti= (String) ((Object[]) item)[11];
				Double  valorAntici=Double.valueOf(valorAnti);  //(BigDecimal) ((Object[]) item)[11];
				valorAnticipos = BigDecimal.valueOf(valorAntici);
				sumaValorAnticiposTomados=sumaValorAnticiposTomados.add(valorAnticipos).setScale(2, RoundingMode.HALF_UP);
		} 
			sumaValorAnticiposSeleccionados = sumaValorAnticiposSeleccionados.setScale(2, RoundingMode.HALF_UP);
			sumaValorAnticiposTomados=sumaValorAnticiposTomados.setScale(2, RoundingMode.HALF_UP);
		}
		this.setValor(sumaValorAnticiposSeleccionados.setScale(2, RoundingMode.HALF_UP));
		this.setValorTomar(sumaValorAnticiposTomados.setScale(2, RoundingMode.HALF_UP));
		return valor;
	}
	public void selectAllCheckboxes(ToggleSelectEvent event) {
		if (lista != null) {		
			lista.clear();
		}
		lista.addAll(listaAnticiposSeleccionados);
		if (listaAnticiposSeleccionados != null) {
			listaAnticiposSeleccionados.clear();
			if (event.isSelected()) {
				listaAnticiposSeleccionados.addAll(lista); // Add all the elements from getSomeList()
				valor = BigDecimal.valueOf(0D);
				obtenerSumaTotalAnticiposSeleccionados();
			}
		}
	}

	public void rowSelectCheckbox(SelectEvent se) {
		lista.add((Object[]) se.getObject());
		listaAnticiposSeleccionados.clear();
		listaAnticiposSeleccionados.addAll(lista);
		valor = BigDecimal.valueOf(0D);
		obtenerSumaTotalAnticiposSeleccionados();

	}

	public void rowUnselectCheckbox(UnselectEvent use) {
		lista.remove((Object[]) use.getObject());
		listaAnticiposSeleccionados.clear();
		listaAnticiposSeleccionados.addAll(lista);
		valor = BigDecimal.valueOf(0D);
		obtenerSumaTotalAnticiposSeleccionados();

	}

	public void cargarArchivo(FileUploadEvent event) {
		try {
			UploadedFile uf = event.getFile();
			byte[] buffer = IOUtils.toByteArray(uf.getInputstream());
			InputStream is1 = new ByteArrayInputStream(buffer);
			procesarArchivo(is1, uf);
		} catch (IOException e) {
			LOG.error(e);
			error(this.obtainException(e));
		}
	}

	public void actualizarDatosTrasfer(Object[] use) {
		if (listaAnticiposSeleccionados != null) {
			listaAnticiposSeleccionados.clear();
			BigDecimal valorprincipal=(BigDecimal) use[6];
			BigDecimal valoraTomar=new BigDecimal(use[11].toString());
			if (use.length > 0) {				
				if (valoraTomar.compareTo(valorprincipal.multiply(new BigDecimal(-1))) > 0) {
					addErrorMessage("Error", "El Valor a Tomar" + " " + valoraTomar + " NO puede ser mayor al valor del anticipo"+ " " + valorprincipal.multiply(new BigDecimal(-1)) );
					listaAnticiposSeleccionados.clear();
					return;
				}
				listaAnticiposSeleccionados.addAll(lista); // Add all the elements from getSomeList()
				valor = BigDecimal.valueOf(0D);
				valorprincipal=BigDecimal.ZERO;
				valoraTomar=BigDecimal.ZERO;
				obtenerSumaTotalAnticiposSeleccionados();
			}
		}
		

	}
	
	private void procesarArchivo(InputStream is1, UploadedFile uf) {
		String pathArchivo = null;
		LocalDate fechaActual = LocalDate.now();
		int mes = fechaActual.getMonthValue();
		int anio = fechaActual.getYear();
		if (a.size() <= 4) {
			try {
				String extension = "." + FileUpload.getFileExtension(uf.getFileName());
				String fileN = this.nbCliente.concat("-").concat("ADJ" + secuencial)
						.concat((uf.getFileName().length()) > 15 ? uf.getFileName().substring(0, 15) : uf.getFileName())
						.concat(extension);
				String fileName = fileN.length() > 100 ? fileN.substring(0, 100) : fileN;
				String pathRaiz = getCompania().getPathFileServer();
				FileUpload fileUpload = new FileUpload();
				pathArchivo = fileUpload.uploadToFileServer(getCompania().getIdTributario(), "COBRANZAS",
						"DEV_ANTICIPOS", Integer.toString(anio).concat(Integer.toString(mes)), pathRaiz, fileName, is1);
				if (this.mapaArchivosCliente == null) {
					this.mapaArchivosCliente = new HashMap<>();
				}

				if (this.mapaArchivosCliente.containsKey(this.nbCliente)) {
					a = this.mapaArchivosCliente.get(this.nbCliente);
					a.add(pathArchivo);
					this.mapaArchivosCliente.replace(this.nbCliente, a);
				} else {
					a = new ArrayList<>();
					a.add(pathArchivo);
					this.mapaArchivosCliente.put(this.nbCliente, a);
				}
				this.archivosAdjuntos.put(uf.getFileName(), pathArchivo);
				this.listaAdjuntos.add(uf.getFileName());
				secuencial = secuencial + 1;

			} catch (Exception e) {
				LOG.error(e);
			}
		} else {
			addErrorMessage("Error", "No se puede cargar mas de 5 documentos");
		}
	}

	
	public void abriRevisar(String noCliente) {

		accionesDialog("DlgAdjuntos", true);
	}
	public void verAdjunto(String nombreArchivo) {
		if (nombreArchivo == null || nombreArchivo.isEmpty()) {
			LOG.error("El nombre del archivo es nulo o vacío");
			return;
		}

		String destPath = archivosAdjuntos.get(nombreArchivo);
		if (destPath == null) {
			LOG.error("No se encontró la ruta para el archivo: " + nombreArchivo);
			return;
		}

		File file = new File(destPath);
		if (!file.exists() || !file.isFile()) {
			LOG.error("El archivo no existe o no es un archivo válido: " + destPath);
			return;
		}

		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) ctx.getExternalContext().getResponse();

		try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(Paths.get(file.toURI())))) {
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				String headerValue = String.format("attachment; filename=\"%s\"", nombreArchivo);
				response.setHeader("Content-Disposition", headerValue);
				response.setContentType((nombreArchivo));
				response.setContentLength((int) length);

				try (ServletOutputStream out = response.getOutputStream()) {
					int bytesRead = 0;
					while (bytesRead != (-1)) {
						bytesRead = in.read(buf);
						out.write(buf, 0, bytesRead);
					}
					ctx.responseComplete();
					out.flush();
				}
			}
		} catch (IOException e) {
			LOG.error("Error al leer o escribir el archivo: " + nombreArchivo + e);
		}
	}

	public void quitarArchivo(String nombreArchivo) {

		if (mapaArchivosCliente != null && mapaArchivosCliente.containsKey(nbCliente)) {
			List<String> a = mapaArchivosCliente.get(nbCliente);
			a.remove(archivosAdjuntos.get(nombreArchivo));
			mapaArchivosCliente.replace(nbCliente, a);
		}

		archivosAdjuntos.remove(nombreArchivo);
		listaAdjuntos.remove(nombreArchivo);
	}

	@SuppressWarnings("null")
	public void verifDatosDevTrasfer() {
		try {
			BigDecimal valorComparacion = BigDecimal.valueOf(valorAutorizaCajaChica);
			cabAnticipoDev.setAsesor(this.asesor);
			cabAnticipoDev.setAplicaCajaChica(this.aplicacajaChica);	
			
			if (!validaObligatorios()) {
				
				if (valorTomar.compareTo(valorComparacion) > 0 && ("S").equals(this.aplicacajaChica)) {
					cabAnticipoDev.setEstado("C");
					cabAnticipoDev.setAprobacion("S");
				}

			if (archivosAdjuntos.isEmpty()) {
				addErrorMessage("Error", "Debe Cargar Adjuntos para proceder a Guardar la Solicitud......");
				return;
			} else {
				String errorP = solicitudesService.guardarSolicitud(cabAnticipoDev, listaDetAnticipo, archivosAdjuntos);

				if (errorP.contains("Info")) {
					try {
						info("Solicitud de Devolución Generada con exito");
						accionesDialog("dlgNuevo", Boolean.FALSE);
						consultarAnticipo();
						if (cabAnticipoDev.getEstado().equals("C")) {
							enviarCorreo(cabAnticipoDev);
						}
						limpiarDatos();
					} catch (Exception e) {
						limpiarDatos();
						addErrorMessage("Error", e.getCause().getMessage());
					}
				} else {
					accionesDialog("dlgNuevo", Boolean.TRUE);
					error("Error de Solicitud de Devolución" + errorP);
				}
			}
			
		}
		} catch (Exception e2) {
			addErrorMessage("Error", e2.getMessage());
			return;
		}
	}

	public void generarnuevaSolicitud() {
		String url = "/cxc-web-prime/jsf/procesos/CxcSolicitudDevAnticipo.jsf?noCia=" + this.noCia + "&cedula="
				+ pCedula + "&nombres=" + pNombres + "&codCliente=" + nbCliente + "&origen=" + 'R';
		try {
			super.redirect(url);
		} catch (IOException e) {
			LOG.error(e);
		}

	}
	
	public void consultarValorCaja() {

		Compania empresa = new Compania();
		try {
			empresa = companiaServiceLocal.getCompaniaCollections(noCia);
			if (empresa.getBaseDevAnticipos() == null) {
				valorAutorizaCajaChica=0D;
			} else {
				valorAutorizaCajaChica = empresa.getBaseDevAnticipos();
			}
		} catch (FindException e) {
			LOG.error(e);
		}
	}
	
public Boolean validaObligatorios() {
		
		if (verificacajaChica) {
			
			if (this.aplicacajaChica == null || this.aplicacajaChica.isEmpty()) {	
				addErrorMessage("Error", "Es obligatorio seleccionar Aplica Caja Chica");
				return true;
			}			
			if (this.necesitaAprobacion == null || this.necesitaAprobacion.isEmpty()) {	
				addErrorMessage("Error", "Es obligatorio seleccionar Aprobación");
				return true;
			}
		} 
		
		if (cabAnticipoDev.getAsesor()== null || cabAnticipoDev.getAsesor().isEmpty()) {
			addErrorMessage("Error", "Es obligatorio seleccionar Asesor Comercial");
			return true;
		}
		if (cabAnticipoDev.getMotivoDevolucion()== null || cabAnticipoDev.getMotivoDevolucion().isEmpty()) {
			addErrorMessage("Error", "Es obligatorio Ingresar un Motivo de Devolución");
			return true;
		}
		return false;
				
	}


	private void enviarCorreo(CxcCabDevAnticipo itemSeleccionado) {
	
		try {
			StringBuffer mensajeEnvio = new StringBuffer("Se Genero Devoluci&oacute;n de Anticipo para Autorizaci&oacute;n en Caja Chica ");
			String email =usuarioSisServiceLocal.mailUsuario(cabAnticipoDev.getAsesor()) ;
			mensajeEnvio.append(generarDetalleEnvio(itemSeleccionado));
			if (null != email) {
				mailService.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), email, email,
						"Devolución de Anticipo para Autorización en Caja Chica ", mensajeEnvio, Boolean.FALSE);
			}
			mailService.sendMailToGroupAttachments(getSisMailServidores(getCompania().getNoCia()), GRUPO_CORREO,
					 "Anticipo para Autorizaci&oacute;n en Caja Chica",  "Anticipo para Autorización en Caja Chica", mensajeEnvio, Boolean.FALSE, null);

		} catch (GeneralException | FindException e) {
			LOG.error(e);
			addErrorMessage("Error", " Ocurrio un error al enviar el correo al grupo" + e.getMessage());
			return;
		}

	}

	private String generarDetalleEnvio(CxcCabDevAnticipo itemSeleccionado) {
		String  cadena = "";
		String mensaje = "Se Genero Devoluci&oacute;n de Anticipo para Autorizaci&oacute;n en Caja Chica ";
	cadena = (" <table border='1' width='100%'> <td  style='color: blue;' align=center><b>" + " " + mensaje + " "
				+ "</b> </td>  <table  width='100%'>  <td  style='color: black;' align=center> </td> </table> <table border='1' width='100%'> <tr> <td align=center><b> CI. CLIENTE</b></td> <td align=center><b> CLIENTE</b></td> <td align=center><b>  FECHA DE SOLICITUD  </b> </td>  <td align=center><b> # SOLICITUD</b> </td> <td align=center><b> ASESOR COMERCIAL </b> <td align=center><b> MOTIVO DEVOLUCI&Oacute;N </b> </td> <td align=center> <b> VALOR </b> </td> <tr> <td align=center>"
				+ this.pCedula + "</td>  <td align=center> "
				+ this.pNombres + "<td align=center> " + new SimpleDateFormat("dd/MM/yyyy").format(itemSeleccionado.getFechaProceso())     
				+ "</td> <td td align=center> " + itemSeleccionado.getId().getNoSolicitud() + "  </td> <td align=center> "
				+ itemSeleccionado.getAsesor() + "  </td> <td align=center> " + itemSeleccionado.getMotivoDevolucion()
				+ " </td> <td align=right> " + itemSeleccionado.getValorTotal() + " </td> </tr> </table> ");
		return cadena ;
	}

	


	// Getter y Setter

	/**
	 * @return the listaAnticiposPorCliente
	 */
	public List<Object[]> getListaAnticiposPorCliente() {
		return listaAnticiposPorCliente;
	}

	/**
	 * @param listaAnticiposPorCliente the listaAnticiposPorCliente to set
	 */
	public void setListaAnticiposPorCliente(List<Object[]> listaAnticiposPorCliente) {
		this.listaAnticiposPorCliente = listaAnticiposPorCliente;

	}

	/**
	 * @return the listaAnticiposPorClientes
	 */
	public List<Object[]> getListaAnticiposPorClientes() {
		return listaAnticiposPorClientes;
	}

	/**
	 * @param listaAnticiposPorClientes the listaAnticiposPorClientes to set
	 */
	public void setListaAnticiposPorClientes(List<Object[]> listaAnticiposPorClientes) {
		this.listaAnticiposPorClientes = listaAnticiposPorClientes;
	}

	/**
	 * @return the nbCliente
	 */
	public String getNbCliente() {
		return nbCliente;
	}

	/**
	 * @param nbCliente the nbCliente to set
	 */
	public void setNbCliente(String nbCliente) {
		this.nbCliente = nbCliente;
	}

	/**
	 * @return the pCedula
	 */
	public String getpCedula() {
		return pCedula;
	}

	/**
	 * @param pCedula the pCedula to set
	 */
	public void setpCedula(String pCedula) {
		this.pCedula = pCedula;
	}

	/**
	 * @return the pNombres
	 */
	public String getpNombres() {
		return pNombres;
	}

	/**
	 * @param pNombres the pNombres to set
	 */
	public void setpNombres(String pNombres) {
		this.pNombres = pNombres;
	}

	/**
	 * @return the origen
	 */
	public String getOrigen() {
		return origen;
	}

	/**
	 * @param origen the origen to set
	 */
	public void setOrigen(String origen) {
		this.origen = origen;
	}

	/**
	 * @return the listaAnticiposSeleccionados
	 */
	public List<Object[]> getListaAnticiposSeleccionados() {
		return listaAnticiposSeleccionados;
	}

	/**
	 * @param listaAnticiposSeleccionados the listaAnticiposSeleccionados to set
	 */
	public void setListaAnticiposSeleccionados(List<Object[]> listaAnticiposSeleccionados) {
		this.listaAnticiposSeleccionados = listaAnticiposSeleccionados;
	}

	/**
	 * @return the valor
	 */
	public BigDecimal getValor() {
		return valor;
	}

	/**
	 * @param valor the valor to set
	 */
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	/**
	 * @return the file
	 */
	public UploadedFile getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(UploadedFile file) {
		this.file = file;
	}

	/**
	 * @return the listaAdjuntos
	 */
	public List<String> getListaAdjuntos() {
		return listaAdjuntos;
	}

	/**
	 * @param listaAdjuntos the listaAdjuntos to set
	 */
	public void setListaAdjuntos(List<String> listaAdjuntos) {
		this.listaAdjuntos = listaAdjuntos;
	}

	/**
	 * @return the cabAnticipoDev
	 */
	public CxcCabDevAnticipo getCabAnticipoDev() {
		return cabAnticipoDev;
	}

	/**
	 * @param cabAnticipoDev the cabAnticipoDev to set
	 */
	public void setCabAnticipoDev(CxcCabDevAnticipo cabAnticipoDev) {
		this.cabAnticipoDev = cabAnticipoDev;
	}

	/**
	 * @return the listaVendedores
	 */
	public List<Vendedor> getListaVendedores() {
		return listaVendedores;
	}

	/**
	 * @param listaVendedores the listaVendedores to set
	 */
	public void setListaVendedores(List<Vendedor> listaVendedores) {
		this.listaVendedores = listaVendedores;
	}

	/**
	 * @return the asesor
	 */
	public String getAsesor() {
		return asesor;
	}

	/**
	 * @param asesor the asesor to set
	 */
	public void setAsesor(String asesor) {
		this.asesor = asesor;
	}

	/**
	 * @return the listaUsuarios
	 */
	public List<UsuarioSis> getListaUsuarios() {
		return listaUsuarios;
	}

	/**
	 * @param listaUsuarios the listaUsuarios to set
	 */
	public void setListaUsuarios(List<UsuarioSis> listaUsuarios) {
		this.listaUsuarios = listaUsuarios;
	}

	/**
	 * @return the valorTomar
	 */
	public BigDecimal getValorTomar() {
		return valorTomar;
	}

	/**
	 * @param valorTomar the valorTomar to set
	 */
	public void setValorTomar(BigDecimal valorTomar) {
		this.valorTomar = valorTomar;
	}

	/**
	 * @return the aplicacajaChica
	 */
	public String getAplicacajaChica() {
		return aplicacajaChica;
	}

	/**
	 * @param aplicacajaChica the aplicacajaChica to set
	 */
	public void setAplicacajaChica(String aplicacajaChica) {
		this.aplicacajaChica = aplicacajaChica;
	}

	/**
	 * @return the valorAutorizaCajaChica
	 */
	public Double getValorAutorizaCajaChica() {
		return valorAutorizaCajaChica;
	}

	/**
	 * @param valorAutorizaCajaChica the valorAutorizaCajaChica to set
	 */
	public void setValorAutorizaCajaChica(Double valorAutorizaCajaChica) {
		this.valorAutorizaCajaChica = valorAutorizaCajaChica;
	}

	/**
	 * @return the verificacajaChica
	 */
	public Boolean getVerificacajaChica() {
		return verificacajaChica;
	}

	/**
	 * @param verificacajaChica the verificacajaChica to set
	 */
	public void setVerificacajaChica(Boolean verificacajaChica) {
		this.verificacajaChica = verificacajaChica;
	}

	/**
	 * @return the necesitaAprobacion
	 */
	public String getNecesitaAprobacion() {
		return necesitaAprobacion;
	}

	/**
	 * @param necesitaAprobacion the necesitaAprobacion to set
	 */
	public void setNecesitaAprobacion(String necesitaAprobacion) {
		this.necesitaAprobacion = necesitaAprobacion;
	}

	
	
	
}
