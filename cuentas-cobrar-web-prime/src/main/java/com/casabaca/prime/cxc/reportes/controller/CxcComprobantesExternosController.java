package com.casabaca.prime.cxc.reportes.controller;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.UploadedFile;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.casabaca.auditoria.ejb.util.dto.retencion.Impuesto;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.service.ArcjcaServiceLocal;
import com.casabaca.common.ejb.service.NominaEmpleadosServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcComprobanteExterno;
import com.casabaca.cxc.ejb.servicio.CxcComprobanteExternoServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.nomina.ejb.servicio.NominaPagoEmpleadoServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelCxcComprobanteExterno;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

@ManagedBean
@ViewScoped
public class CxcComprobantesExternosController extends CommonController implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int FIRST_PAGE = 0;
	private int PAGE_SIZE = 20;
	private static final String PATH_OMIT = "C:\\Users\\cb_automatizacion\\Documents";
	private static final String URL_DOCUMENTOS = "http://192.168.150.26";
	
	@EJB(lookup = NombreJNDI.CXC_COMPROBANTE_EXTERNO_SERVICE_BEAN)
	private CxcComprobanteExternoServiceLocal comprobanteExternoServiceLocal;
	
	@EJB(lookup = NombreJNDI.ARCJCA_SERVICE_BEAN)
	private ArcjcaServiceLocal arcjcaServiceLocal;
	
	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailServiceLocal;
	
	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;
	
	@EJB(lookup = NombreJNDI.NOMINA_EMPLEADOS_SERVICE)
	private NominaEmpleadosServiceLocal nominaEmpleadosServiceLocal;
	
	private List<Impuesto> ltotalImpuestos;
	private LazyDataModelCxcComprobanteExterno lazyDataModelCxcComprobanteExterno;
	private List<String> listUsuariosRegistrados;
	private CxcComprobanteExterno comprobanteExterno;
	private CxcComprobanteExterno comprobanteExternoSelected;
	private UploadedFile file;
	private String url;
	private boolean esCierreCaja;

	@PostConstruct
	public void init() {
		
		this.comprobanteExterno = new CxcComprobanteExterno();
		this.esCierreCaja = false;
		try {
			
//			this.lazyDataModelCxcComprobanteExterno = new LazyDataModelCxcComprobanteExterno(FIRST_PAGE, PAGE_SIZE, getCompania().getNoCia(), comprobanteExterno);
			buscarUsuariosDeregistro();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void buscarUsuariosDeregistro(){
		this.listUsuariosRegistrados = comprobanteExternoServiceLocal.listUsuariosRegistrados(getCompania().getNoCia());
	}
	
	public void consolidarPreCierre() {
		
		List<Impuesto> ltodosImpuestos = new ArrayList<>();
		for (CxcComprobanteExterno comprobante : lazyDataModelCxcComprobanteExterno.getLcomprobantesExternosList()) {
			
			if (comprobante.isSeleccionado()) {
				System.out.println(comprobante.getObservacionCierre());
				List<Impuesto> limpuestos =
						verifyImpuestos(buildUrl(comprobante.getRutaXml()).replace(CommonConstants.FILL_CHARACTER, SPACE_IN_URL));
				ltodosImpuestos.addAll(limpuestos);
			}

		}
		
		Map<Double, Double> totForPercent = ltodosImpuestos.stream()
																.collect(Collectors.groupingBy(imp -> imp.getPorcentajeRetener(), 
																							   Collectors.summingDouble(Impuesto::getValorRetenido)
																							  )
																		);
		
		this.ltotalImpuestos = 
						totForPercent.entrySet().stream().map(map -> new Impuesto(map.getKey(), map.getValue()))
														 .collect(Collectors.toList());
		
		if(this.ltotalImpuestos.isEmpty()) {
			super.setMessageGrowl(SEVERITY_WARN, "Atencion!", "No ha seleccionado ningun comprobante.");
		}else {
			super.accionesDialog("wvConfirmarCierre", Boolean.TRUE);
		}
		
		
	}
	
	public Date truncarFecha(Date fecha) {
		return FechaUtils.truncarFecha(fecha);
	}
	
	public void actualizarcomprobante(CxcComprobanteExterno comprobanteExterno) {
		this.comprobanteExternoServiceLocal.merge(comprobanteExterno);
		super.setMessageGrowl(SEVERITY_INFO, "Observacion Posterior", "Actualizada!!");
	}
	
	public void ejecutarCierre() {
		
		try {
			
			List<CxcComprobanteExterno> lcomprobantes = lazyDataModelCxcComprobanteExterno.getLcomprobantesExternosList().stream().filter(imp -> imp.isSeleccionado()).collect(Collectors.toList());
			
			for (CxcComprobanteExterno comprobante : lcomprobantes) {
				comprobante.setFechaRevision(new Date());
				comprobante.setUsuarioRevision(getUsuario().getUsuario());
			}
			
			this.comprobanteExternoServiceLocal.mergeList(lcomprobantes);
			this.lazyDataModelCxcComprobanteExterno = new LazyDataModelCxcComprobanteExterno();
			super.setMessageGrowl(SEVERITY_INFO, "Cierre de Caja", "Exitoso");
			super.accionesDialog("wvConfirmarCierre", Boolean.FALSE);
			
			enviarMail();
		} catch (Exception e) {
			super.setMessageGrowl(SEVERITY_ERROR, "Ocurrio un error", e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	@Asynchronous
	private void enviarMail() throws GeneralException, FindException {
		DecimalFormat formatDecimal = new DecimalFormat("#.##");
		String to = this.usuarioSisServiceLocal.mailUsuario(this.nominaEmpleadosServiceLocal.getEmpleado(getCompania().getNoCia(), getUsuario().getNoEmple()).getJefe());
		
		StringBuffer html = new StringBuffer();
		html.append("El usuario: <b>");
		html.append(getUsuario().getNombre());
		html.append("</b> realizo el cierre de caja de la fecha <b>");
		html.append(new SimpleDateFormat("dd/MM/yyyy").format(this.comprobanteExterno.getFechaCaja()));
		html.append("</b>. Con el siguiente detalle. <br>");
		html.append("<table>");
		
		html.append("<tr>");
		html.append("<td>");
		html.append("<b>PORCENTAJE</b>");
		html.append("</td>");
		html.append("<td>");
		html.append("<b>VALOR</b>");
		html.append("</td>");
		html.append("</tr>");
		
		for(Impuesto impuesto : this.ltotalImpuestos) {
			html.append("<tr>");
			html.append("<td>");
			html.append(formatDecimal.format(impuesto.getPorcentajeRetener()));
			html.append("%</td>");
			html.append("<td>");
			html.append(formatDecimal.format(impuesto.getValorRetenido()));
			html.append("</td>");
			html.append("</tr>");
		}
		
		html.append("<tr>");
		html.append("<td>");
		html.append("<b>TOTAL</b>");
		html.append("</td>");
		html.append("<td><b>");
		html.append(formatDecimal.format(getSumaTotalImpuestos()));
		html.append("</b></td>");
		html.append("</tr>");
		
		html.append("</table>");
		html.append("<br>");
		html.append("Saludos cordiales.");
		this.mailServiceLocal.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(), to, "Cierre de Caja", html);
	}
	
	public Double getSumaTotalImpuestos() {
		
		return this.ltotalImpuestos != null ? (this.ltotalImpuestos.stream().filter(imp -> imp.getValorRetenido() != null ).collect(Collectors.summingDouble(imp -> imp.getValorRetenido()))): 0D;
		
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
	private List<Impuesto> verifyImpuestos(String urlString)  {
		List<Impuesto> limpuestos = null;
		try {
//			String urlString = "http://192.168.150.26/Retenciones/18-07-2019%20XML/1807201907179228701400120015010000862785658032313.xml";
			URL url = new URL(urlString);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(url.openStream());
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList) xpath.evaluate("/autorizacion/comprobante", document, XPathConstants.NODESET);
			Element node = (Element) nodes.item(0);
			CharacterData cd = (CharacterData) node.getFirstChild();
			Document subdocument = db.parse(new ByteArrayInputStream(cd.getData().getBytes()));
			NodeList lnodesImpuestos = (NodeList) xpath.evaluate("/comprobanteRetencion/impuestos/impuesto", subdocument, XPathConstants.NODESET);
			
			limpuestos = new ArrayList<>();
			
			for (int i = 0; i < lnodesImpuestos.getLength(); i++) {
				
				Element element = (Element) lnodesImpuestos.item(i);
				Impuesto impuesto = new Impuesto();
				impuesto.setCodigo(getValueFromSimpleTag(element, "codigo"));
				impuesto.setCodigoRetencion(getValueFromSimpleTag(element, "codigoRetencion"));
				impuesto.setBaseImponible(new Double(getValueFromSimpleTag(element, "baseImponible")));
				impuesto.setPorcentajeRetener(new Double(getValueFromSimpleTag(element, "porcentajeRetener")));
				impuesto.setValorRetenido(new Double(getValueFromSimpleTag(element, "valorRetenido")));
				impuesto.setCodDocSustento(getValueFromSimpleTag(element, "codDocSustento"));
				limpuestos.add(impuesto);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return limpuestos;
	}
	
	
	private String getValueFromSimpleTag(Element element, String nameTag) {
		return element.getElementsByTagName(nameTag).item(0).getTextContent();
	}
	
	public void procesarArchivo() {
		String[] acceptedFormats = {"dd/MM/yyyy","dd/MM/yyyy HH:mm:ss"};
		
		List<CxcComprobanteExterno> lcomprobantesExternos = new ArrayList<CxcComprobanteExterno>();
		
		try {
			List<Object[]> dataExcel = super.leerExcel(this.file.getInputstream(), Boolean.TRUE);
			
			CxcComprobanteExterno comprobante;
			for (Object[] obj : dataExcel) {
				comprobante = new CxcComprobanteExterno();
				comprobante.setNoCia(getCompania().getNoCia());
				comprobante.setComprobante((String) obj[0]);
				comprobante.setSerieComprobante((String) obj[1]);
				comprobante.setSecuencial1((String) obj[2]);
				comprobante.setSecuencial2((String) obj[3]);
				comprobante.setRucEmisor((String) obj[4]);
				comprobante.setRazonSocialEmisor((String) obj[5]);
				
				if(obj[6] instanceof Date) {
					comprobante.setFechaEmision((Date) obj[6]);
				}else{
					comprobante.setFechaEmision(DateUtils.parseDate((String) obj[6], acceptedFormats));
				}
				
				if(obj[7] instanceof Date) {
					comprobante.setFechaAutorizacion((Date) obj[7]);
				}else{
					comprobante.setFechaAutorizacion(DateUtils.parseDate((String) obj[7], acceptedFormats));
				}
				
				comprobante.setTipoEmision((String) obj[8]);
				comprobante.setIdentificacionReceptor((String) obj[9]);
				comprobante.setClaveAcceso((String) obj[10]);
				comprobante.setNumeroAutorizacion((String) obj[11]);
				comprobante.setXml((String) obj[12]);
				comprobante.setPdf((String) obj[13]);;
				comprobante.setEstadoComprobante((String) obj[14]);
				comprobante.setNumeroIngreso((String) obj[15]);
				comprobante.setTipoDoc((String) obj[16]);
				comprobante.setCentro((String) obj[17]);
				comprobante.setSerieFactura((String) obj[18]);
				comprobante.setNoReferencia((String) obj[19]);
				comprobante.setRutaXml((String) obj[20]);
				comprobante.setRutaPdf((String) obj[21]);
				if(obj.length>22) {
					comprobante.setImpreso((String) obj[22]);
				}
				
				lcomprobantesExternos.add(comprobante);
			}
			
			//Consolidacion de Numeros de ingreso con centros para una busqueda mediante un in
			HashMap<String, String> numerosIngreso = new HashMap<>();
			for (CxcComprobanteExterno comprobanteExt : lcomprobantesExternos) {
				numerosIngreso.put(comprobanteExt.getNumeroIngreso() +"|"+ comprobanteExt.getCentro(), 
								   comprobanteExt.getNumeroIngreso() +"|"+ comprobanteExt.getCentro());
			}
			
			//Ejecucion de busqueda para obtener la fecha de caja por numeros de ingreso
			HashMap<String, Object[]> numeroYFechaIngreso = new HashMap<>();
			if(numerosIngreso.keySet().size()>0) {
				numeroYFechaIngreso = this.arcjcaServiceLocal.obtenerFechaCajaPor(getCompania().getNoCia(), new ArrayList<String>(numerosIngreso.keySet()));
			}
			
			//Seteo de fecha de caja obtenida
			for (CxcComprobanteExterno compr : lcomprobantesExternos) {
				
				String key = compr.getNumeroIngreso()+"|"+compr.getCentro();
				Object[] data = numeroYFechaIngreso.get(key);
				Date fechaCaja = (Date) data[0];
				String usuario = (String) data[1];
				if(fechaCaja!=null) {
					compr.setFechaCaja(fechaCaja);
				}
				if(usuario!=null) {
					compr.setUsuarioRegistra(usuario);
				}
				
			}
			
			//Grabando registros
			this.comprobanteExternoServiceLocal.grabarComprobantes(lcomprobantesExternos);
			buscarUsuariosDeregistro();
			super.setMessageGrowl(SEVERITY_INFO, "Excel Procesado Correctamente","");
		} catch (Exception e) {
			e.printStackTrace();
			super.setMessageGrowl(SEVERITY_ERROR, "Error al procesar el Excel", e.getMessage());
		}
				
	}

	public void onTabConsultaChange(TabChangeEvent event) {
        
		this.esCierreCaja = false;
		this.comprobanteExterno.setFechaCaja(null);
		this.comprobanteExterno.setFechaDesde(null);
		this.comprobanteExterno.setFechaHasta(null);
		this.comprobanteExterno.setFechaRevision(null);
		
		this.lazyDataModelCxcComprobanteExterno = new LazyDataModelCxcComprobanteExterno();
    }
	
	public void consultar() {
		this.esCierreCaja = false;
		String parameter = super.getRequestParameter("CIERRE_CAJA");
		
		if (parameter!=null && parameter.equals("C")) {
			
			this.esCierreCaja = true;
			if(this.comprobanteExterno.getFechaCaja() != null) {
				
				if(this.comprobanteExternoServiceLocal.existeFechaRevision(this.comprobanteExterno.getFechaCaja(), getCompania().getNoCia())) {
					
					this.lazyDataModelCxcComprobanteExterno = new LazyDataModelCxcComprobanteExterno();
					super.updateComponentFromId("idFormContent:idDtComprobantes");
					super.setMessageGrowl(SEVERITY_WARN, "Atencion!", "La fecha ya ha sido cerrada anteriormente.");
					
				}else {
					
					this.lazyDataModelCxcComprobanteExterno = new LazyDataModelCxcComprobanteExterno(FIRST_PAGE, PAGE_SIZE, getCompania().getNoCia(), comprobanteExterno);
					this.PAGE_SIZE = this.lazyDataModelCxcComprobanteExterno.getRowCount();
					super.updateComponentFromId("idFormContent:idDtComprobantes");
					
				}
				
			}else {
				
				super.setMessageGrowl(SEVERITY_WARN, "Atencion!!", "Ingrese una fecha de Caja.");
				
			}
			
		} else if (parameter!=null && parameter.equals("P")){
			
			this.PAGE_SIZE = 20;
			this.comprobanteExterno.setFechaRevision(new Date());
			this.lazyDataModelCxcComprobanteExterno = new LazyDataModelCxcComprobanteExterno(FIRST_PAGE, PAGE_SIZE, getCompania().getNoCia(), comprobanteExterno);
			
		}else{
			
			this.PAGE_SIZE = 20;
			this.lazyDataModelCxcComprobanteExterno = new LazyDataModelCxcComprobanteExterno(FIRST_PAGE, PAGE_SIZE, getCompania().getNoCia(), comprobanteExterno);
			
		}

	}
	

	public void seeDocument(CxcComprobanteExterno comprobanteExterno) {

		Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String action = params.get("TIPO_DOC");
		
		switch (action) {
		case "X":
			this.url = buildUrl(comprobanteExterno.getRutaXml());
			break;
		case "P":
			this.url = buildUrl(comprobanteExterno.getRutaPdf());
			break;
		default:
			break;
		}
		
	}

	@SuppressWarnings("static-access")	
	private String buildUrl(String url) {
		return this.URL_DOCUMENTOS+url.replace(this.PATH_OMIT, "").replace("\\" ,"/");
	}
	
	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public CxcComprobanteExterno getComprobanteExterno() {
		return comprobanteExterno;
	}

	public void setComprobanteExterno(CxcComprobanteExterno comprobanteExterno) {
		this.comprobanteExterno = comprobanteExterno;
	}

	public int getPageSize() {
		return this.PAGE_SIZE;
	}
	
	public LazyDataModelCxcComprobanteExterno getLazyDataModelCxcComprobanteExterno() {
		return lazyDataModelCxcComprobanteExterno;
	}

	public void setLazyDataModelCxcComprobanteExterno(
			LazyDataModelCxcComprobanteExterno lazyDataModelCxcComprobanteExterno) {
		this.lazyDataModelCxcComprobanteExterno = lazyDataModelCxcComprobanteExterno;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public CxcComprobanteExterno getComprobanteExternoSelected() {
		return comprobanteExternoSelected;
	}

	public void setComprobanteExternoSelected(CxcComprobanteExterno comprobanteExternoSelected) {
		this.comprobanteExternoSelected = comprobanteExternoSelected;
	}

	public List<String> getListUsuariosRegistrados() {
		return listUsuariosRegistrados;
	}

	public void setListUsuariosRegistrados(List<String> listUsuariosRegistrados) {
		this.listUsuariosRegistrados = listUsuariosRegistrados;
	}

	public List<Impuesto> getLtotalImpuestos() {
		return ltotalImpuestos;
	}

	public void setLtotalImpuestos(List<Impuesto> ltotalImpuestos) {
		this.ltotalImpuestos = ltotalImpuestos;
	}

	public boolean isEsCierreCaja() {
		return esCierreCaja;
	}

	public void setEsCierreCaja(boolean esCierreCaja) {
		this.esCierreCaja = esCierreCaja;
	}
	
}