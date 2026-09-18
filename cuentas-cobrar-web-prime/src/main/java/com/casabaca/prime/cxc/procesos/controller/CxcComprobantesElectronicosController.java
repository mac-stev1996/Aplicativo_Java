package com.casabaca.prime.cxc.procesos.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.casabaca.auditoria.ejb.modelo.ComprobanteElectronico;
import com.casabaca.auditoria.ejb.servicio.ComprobanteElectronicoServiceLocal;
import com.casabaca.auditoria.ejb.util.dto.CierreCaja;
import com.casabaca.auditoria.ejb.util.dto.retencion.Impuesto;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.ExceptionUtils;
import com.casabaca.common.ejb.model.ParamCab;
import com.casabaca.common.ejb.service.NominaEmpleadosServiceLocal;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

@ManagedBean
@ViewScoped
public class CxcComprobantesElectronicosController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String TODOS = "T";
	private static final String AGENCIA = "A";
	private static final String USUARIO = "U";
//	private static final String URL_RETENCIONES = "https://robotcb.casabaca.com/retenciones/RETENCIONES";
//	private static final String URL_RETENCIONES_BANCOS = "https://robotcb.casabaca.com/retenciones/RUC_EXCLUIDO";
	private static final String PARAM_URL_RETENCIONES = "RRCR";
	private static final String PARAM_URL_RETENCIONES_BANCOS = "RRCRE";
	private String URL_RETENCIONES;
	private String URL_RETENCIONES_BANCOS;
	
	
	@EJB(lookup = NombreJNDI.COMPROBANTE_ELECTRONICO_SERVICE_BEAN)
	private ComprobanteElectronicoServiceLocal comprobanteElectronicoServiceLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisServiceLocal;

	@EJB(lookup = NombreJNDI.NOMINA_EMPLEADOS_SERVICE)
	private NominaEmpleadosServiceLocal nominaEmpleadosServiceLocal;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetServiceLocal;

	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabServiceLocal;
	
	private List<ComprobanteElectronico> lcomprobantes;
	private List<ComprobanteElectronico> lcomprobantesError;
	private List<CierreCaja> lcierresCaja;

	private List<Impuesto> ltotalImpuestos;
	private List<SelectItem> lagencias;
	private List<SelectItem> lusuarios;

	private String agenciaSelect;
	private String usuarioSelect;
	private String tabSelect;
	private Date fechaCierre;
	private String url;
	private boolean selectComprobantes;

	@PostConstruct
	public void init() {
		this.tabSelect = "TAB1";
		
		try {
			
			ParamCab paramUrlRetenciones = this.paramCabServiceLocal.consultarParametro(getCompania().getNoCia(), PARAM_URL_RETENCIONES);
			ParamCab paramUrlRetencionesBancos = this.paramCabServiceLocal.consultarParametro(getCompania().getNoCia(), PARAM_URL_RETENCIONES_BANCOS);
			
			if(paramUrlRetenciones != null) {
				this.URL_RETENCIONES = paramUrlRetenciones.getDescripcion(); 
			}else {
				super.setMessageGrowl(SEVERITY_WARN, "Atencion", "Se necesita configurar la url del robot para la empresa");
			}
			
			if(paramUrlRetencionesBancos != null) {
				this.URL_RETENCIONES_BANCOS = paramUrlRetencionesBancos.getDescripcion(); 
			}else {
				super.setMessageGrowl(SEVERITY_ERROR, "Atencion", "Se necesita configurar la url de bancos para la empresa");
			}
			
		} catch (Exception e) {
			super.setMessageGrowl(SEVERITY_ERROR, "Error", ExceptionUtils.obtainException(e));
		}
	}

	public void listarAgencias() {
		List<ComprobanteElectronico> lcomprobantes = this.comprobanteElectronicoServiceLocal
				.getComprobantesBy(getCompania().getNoCia(), agenciaSelect, fechaCierre, null, AGENCIA);

		if (lcomprobantes != null) {
			this.lagencias = new ArrayList<>();
			for (ComprobanteElectronico comp : lcomprobantes) {
				String[] agencia = comp.getAgencia().split("-");
				this.lagencias.add(new SelectItem(agencia[0], agencia[1]));
			}

		}
	}

	public void listarUsuarios() {
		List<ComprobanteElectronico> lcomprobantes = this.comprobanteElectronicoServiceLocal
				.getComprobantesBy(getCompania().getNoCia(), agenciaSelect, fechaCierre, null, USUARIO);

		if (lcomprobantes != null) {
			this.lusuarios = new ArrayList<>();
			for (ComprobanteElectronico comp : lcomprobantes) {

				this.lusuarios.add(new SelectItem(comp.getUsuario(), comp.getUsuario()));

			}

		}
	}

	private boolean esCajaCerrada() {
		List<ComprobanteElectronico> lcomprobantes = this.comprobanteElectronicoServiceLocal
				.getComprobantesBy(getCompania().getNoCia(), agenciaSelect, fechaCierre, usuarioSelect, USUARIO, "VCC");
		return !lcomprobantes.isEmpty();
	}

	public void limpiar() {

		this.lcomprobantes = new ArrayList<>();

		this.lcierresCaja = new ArrayList<>();
		this.lagencias = new ArrayList<>();
		this.lusuarios = new ArrayList<>();

		this.fechaCierre = null;
		this.agenciaSelect = null;
		this.usuarioSelect = null;

	}

	public void consultar() {

		switch (tabSelect) {
		case "TAB1":

			if (esCajaCerrada()) {

				this.lcomprobantes = this.comprobanteElectronicoServiceLocal.getComprobantesBy(getCompania().getNoCia(),
						agenciaSelect, fechaCierre, usuarioSelect, TODOS);
//						agencia, fecha, usuario, TODOS);
			} else {
				this.lcomprobantes = new ArrayList<>();
				addInfoMessage("Esta caja", "Ya se cerro anteriormente.");
			}

			break;
		case "TAB2":

			this.lcierresCaja = this.comprobanteElectronicoServiceLocal.consultaCierresCaja(getCompania().getNoCia(),
					agenciaSelect, fechaCierre, usuarioSelect);
			break;

		case "TAB3":

			this.lcomprobantes = this.comprobanteElectronicoServiceLocal.getComprobantesBy(getCompania().getNoCia(),
					agenciaSelect, fechaCierre, null, TODOS, "NU");

			break;
		default:
			break;
		}

	}

	public void savePreReview() {

		List<ComprobanteElectronico> lcomprobantes = this.lcomprobantes.stream().filter(imp -> imp.isSeleccionado())
				.collect(Collectors.toList());

		this.comprobanteElectronicoServiceLocal.savePreReview(lcomprobantes);
		super.setMessageGrowl(SEVERITY_INFO, "Avance de Revision", "Guardado con exito!");

	}

	public void seeDocument(ComprobanteElectronico comprobante) {

		try {
			this.url = null;
			Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext()
					.getRequestParameterMap();
			String action = params.get("TIPO_DOC");
			String preUrl = buildUrl(comprobante.getComprobanteElectronicoPK().getClaveAcceso(), action); 
			
			if(existDocumentInServer(preUrl)) {
				this.url = preUrl;
			}else {
				this.url = buildUrlBancos(comprobante, action);
			
			}
			
			
			super.accionesDialog("wvDlgDocumentos", Boolean.TRUE);
		} catch (Exception e) {
			super.setMessageGrowl(SEVERITY_ERROR, "Ocurrio un problema en la vizualizacion del documento.",
					e.getMessage());
		}

	}

	private boolean existDocumentInServer(String path) {
		try {

			HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
			return conn.getResponseCode() != 404;

		} catch (Exception e) {
			return false;
		}

	}

	@SuppressWarnings("static-access")
	private String buildUrl(String claveAcceso, String tipo) {
		
		String preUrl = null;
		String fecha = claveAcceso.substring(0, 8);
		String dia = fecha.substring(0, 2);
		String mes = fecha.substring(2, 4);
		String anio = fecha.substring(4, 8);
		
		preUrl = this.URL_RETENCIONES;

		switch (tipo) {
		case "X":
			preUrl = preUrl.concat("/").concat(claveAcceso).concat(".xml");
			break;
		case "P":
			preUrl = preUrl.concat("/").concat(claveAcceso).concat(".pdf");
			break;
		default:
			break;
		}

		return preUrl;
	}

	@SuppressWarnings("static-access")
	private String buildUrlBancos(ComprobanteElectronico comprobante, String tipo) {

		String preUrl = this.URL_RETENCIONES_BANCOS.concat("/")
				.concat(comprobante.getComprobanteElectronicoPK().getClaveAcceso());

		switch (tipo) {
		case "X":
			preUrl = preUrl.concat(".xml");
			break;
		case "P":
			preUrl = preUrl.concat(".pdf");
			break;
		default:
			break;
		}

		return preUrl;
	}

	public void viewCloseComprobants(SelectEvent event) {
		CierreCaja cc = (CierreCaja) event.getObject();
		this.lcomprobantes = this.comprobanteElectronicoServiceLocal.getComprobantesBy(getCompania().getNoCia(),
				cc.getCodAgencia(), fechaCierre, cc.getUsuarioConsulta(), TODOS, "CC");
	}

	public void consolidarPreCierre() {

		List<Impuesto> ltodosImpuestos = new ArrayList<>();
		this.lcomprobantesError = new ArrayList<>();

		List<ComprobanteElectronico> lcomprobantes = this.lcomprobantes.stream().filter(imp -> imp.isSeleccionado())
				.collect(Collectors.toList());

		lcomprobantes.forEach(item -> {

			List<Impuesto> limpuestos = null;
			try {

				String preUrl = buildUrl(item.getComprobanteElectronicoPK().getClaveAcceso(), "X");
				if (!existDocumentInServer(preUrl)) {
					preUrl = buildUrlBancos(item, "X");
					if(!existDocumentInServer(preUrl)) {
						setMessageGrowl(SEVERITY_WARN, "Comprobante no descargado Serie:"+ item.getEstablecimiento() + "-" +item.getPuntoEmision(), "Numero: "+ item.getSecuencial());
					}
				}

				limpuestos = verifyImpuestos(preUrl);
				ltodosImpuestos.addAll(limpuestos);

			} catch (IOException e) {
				this.lcomprobantesError.add(item);
			} catch (XPathExpressionException | SAXException | ParserConfigurationException e) {
				e.printStackTrace();
			}

		});

		Map<Double, Double> totForPercent = ltodosImpuestos.stream().collect(Collectors
				.groupingBy(imp -> imp.getPorcentajeRetener(), Collectors.summingDouble(Impuesto::getValorRetenido)));

		this.ltotalImpuestos = totForPercent.entrySet().stream().map(map -> new Impuesto(map.getKey(), map.getValue()))
				.collect(Collectors.toList());

		if (this.ltotalImpuestos.isEmpty()) {
			super.setMessageGrowl(SEVERITY_WARN, "Atencion!", "No se pudo calcular los impuestos.");
		} else {

			Collections.sort(this.ltotalImpuestos, new Comparator<Impuesto>() {
				public int compare(Impuesto p1, Impuesto p2) {
					return p1.getPorcentajeRetener().compareTo(p2.getPorcentajeRetener());
				}
			});

			super.accionesDialog("wvConfirmarCierre", Boolean.TRUE);
		}

	}

	private List<Impuesto> verifyImpuestos(String urlString)
			throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		final String PATH_PRINCIPAL = "/autorizacion/comprobante";
		List<Impuesto> limpuestos = null;
//		String urlString = "http://192.168.150.26/Retenciones/18-07-2019%20XML/1807201907179228701400120015010000862785658032313.xml";
		URL url = new URL(urlString);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(url.openStream());
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList lnodesVersion = null; 
		Document subdocument = null;
		boolean sinTagData = false; 
		try {
			
			NodeList nodes = (NodeList) xpath.evaluate(PATH_PRINCIPAL, document, XPathConstants.NODESET);
			Element node = (Element) nodes.item(0);
			
			Node nodoCData = null;
			NodeList lnodes = node.getChildNodes();
			for (int i = 0; i < lnodes.getLength(); i++) {
				nodoCData = lnodes.item(i);
				if(nodoCData.getTextContent().length()>1)
					break;
			}
			
			CharacterData cd = (CharacterData) nodoCData;
			subdocument = db.parse(new ByteArrayInputStream(cd.getData().getBytes()));
			lnodesVersion = (NodeList) xpath.evaluate("/comprobanteRetencion", subdocument,
					XPathConstants.NODESET);
		} catch (ClassCastException e) {
			sinTagData = true;
			lnodesVersion = (NodeList) xpath.evaluate(PATH_PRINCIPAL+"/comprobanteRetencion", document, XPathConstants.NODESET);
			subdocument = document;
		}
		
		Element nodeVersion = (Element) lnodesVersion.item(0);
		String versionComprobanteXMl = nodeVersion.getAttribute("version");

		NodeList lnodesImpuestos = null;
		
		switch (versionComprobanteXMl) {
		case "1.0.0":
			lnodesImpuestos = (NodeList) xpath.evaluate((sinTagData?PATH_PRINCIPAL:CommonConstants.VACIO)
					+"/comprobanteRetencion/impuestos/impuesto", subdocument,
					XPathConstants.NODESET);
			break;
		case "2.0.0":
			lnodesImpuestos = (NodeList) xpath.evaluate((sinTagData?PATH_PRINCIPAL:CommonConstants.VACIO)
					+"/comprobanteRetencion/docsSustento/docSustento/retenciones/retencion", subdocument,
					XPathConstants.NODESET);
			break;
		default:
			break;
		}

		limpuestos = new ArrayList<>();

		if (lnodesImpuestos.getLength() == 0) {
			System.out.println("[SIN IMPUESTOS]" + urlString);
		}

		for (int i = 0; i < lnodesImpuestos.getLength(); i++) {

			Element element = (Element) lnodesImpuestos.item(i);
			Impuesto impuesto = new Impuesto();
			impuesto.setCodigo(getValueFromSimpleTag(element, "codigo"));
			impuesto.setCodigoRetencion(getValueFromSimpleTag(element, "codigoRetencion"));
			impuesto.setBaseImponible(new Double(getValueFromSimpleTag(element, "baseImponible")));
			impuesto.setPorcentajeRetener(new Double(getValueFromSimpleTag(element, "porcentajeRetener")));
			impuesto.setValorRetenido(new Double(getValueFromSimpleTag(element, "valorRetenido")));
//			impuesto.setCodDocSustento(getValueFromSimpleTag(element, "codDocSustento"));
			limpuestos.add(impuesto);

		}

		return limpuestos;
	}

	public Double getSumaTotalImpuestos() {

		return this.ltotalImpuestos != null
				? (this.ltotalImpuestos.stream().filter(imp -> imp.getValorRetenido() != null)
						.collect(Collectors.summingDouble(imp -> imp.getValorRetenido())))
				: 0D;

	}

	public void ejecutarCierre() {

		try {

			List<ComprobanteElectronico> lcomprobantes = this.lcomprobantes.stream().filter(imp -> imp.isSeleccionado())
					.collect(Collectors.toList());

			for (ComprobanteElectronico comprobante : lcomprobantes) {
				comprobante.setFechaCierreCaja(new Date());
				comprobante.setUsuarioCierreCaja(getUsuario().getUsuario());
			}

			this.comprobanteElectronicoServiceLocal.cerrarCaja(lcomprobantes);

			super.setMessageGrowl(SEVERITY_INFO, "Cierre de Caja", "Exitoso");
			super.accionesDialog("wvConfirmarCierre", Boolean.FALSE);
			this.lcomprobantes = new ArrayList<>();
			enviarMail();
		} catch (Exception e) {
			super.setMessageGrowl(SEVERITY_ERROR, "Ocurrio un error", e.getMessage());
			e.printStackTrace();
		}

	}

	@Asynchronous
	private void enviarMail() throws GeneralException, FindException {
		DecimalFormat formatDecimal = new DecimalFormat("#.##");
		String to = this.usuarioSisServiceLocal.mailUsuario(this.nominaEmpleadosServiceLocal
				.getEmpleado(getCompania().getNoCia(), getUsuario().getNoEmple()).getJefe());

		String fechaCierreCajaString = new SimpleDateFormat("dd/MM/yyyy").format(this.fechaCierre);

		StringBuffer html = new StringBuffer();
		html.append("El usuario: <b>");
		html.append(getUsuario().getNombre());
		html.append("</b> realizo el cierre de caja de la fecha <b>");
		html.append(fechaCierreCajaString);
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

		for (Impuesto impuesto : this.ltotalImpuestos) {
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
		this.mailServiceLocal.sendEmailInHtmlNoCia(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(), to,
				"Cierre de Caja [".concat(fechaCierreCajaString).concat("]"), html);
	}

	private String getValueFromSimpleTag(Element element, String nameTag) {
		return element.getElementsByTagName(nameTag).item(0).getTextContent();
	}

	public void selectAll() {

		this.lcomprobantes.forEach(item -> {
			item.setSeleccionado(selectComprobantes);
		});

	}

	public void onTabChange(TabChangeEvent event) {
		this.tabSelect = event.getTab().getId().toUpperCase();
		this.lcomprobantes = new ArrayList<>();

	}
	
	public void actualizarObservacion(ComprobanteElectronico comp) {
		
		try {
			this.comprobanteElectronicoServiceLocal.actualizarObservacion(comp);
		} catch (Exception e) {
			addInfoMessage("Error en Actualizacion", e.getMessage());
		}
	
	}

	public void verifyDateClose() {

	}

	public Date getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(Date fechaCierre) {
		this.fechaCierre = fechaCierre;
	}

	public List<ComprobanteElectronico> getLcomprobantes() {
		return lcomprobantes;
	}

	public void setLcomprobantes(List<ComprobanteElectronico> lcomprobantes) {
		this.lcomprobantes = lcomprobantes;
	}

	public List<SelectItem> getLagencias() {
		return lagencias;
	}

	public void setLagencias(List<SelectItem> lagencias) {
		this.lagencias = lagencias;
	}

	public String getAgenciaSelect() {
		return agenciaSelect;
	}

	public void setAgenciaSelect(String agenciaSelect) {
		this.agenciaSelect = agenciaSelect;
	}

	public String getUsuarioSelect() {
		return usuarioSelect;
	}

	public void setUsuarioSelect(String usuarioSelect) {
		this.usuarioSelect = usuarioSelect;
	}

	public List<SelectItem> getLusuarios() {
		return lusuarios;
	}

	public void setLusuarios(List<SelectItem> lusuarios) {
		this.lusuarios = lusuarios;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Impuesto> getLtotalImpuestos() {
		return ltotalImpuestos;
	}

	public void setLtotalImpuestos(List<Impuesto> ltotalImpuestos) {
		this.ltotalImpuestos = ltotalImpuestos;
	}

	public boolean isSelectComprobantes() {
		return selectComprobantes;
	}

	public void setSelectComprobantes(boolean selectComprobantes) {
		this.selectComprobantes = selectComprobantes;
	}

	public List<ComprobanteElectronico> getLcomprobantesError() {
		return lcomprobantesError;
	}

	public void setLcomprobantesError(List<ComprobanteElectronico> lcomprobantesError) {
		this.lcomprobantesError = lcomprobantesError;
	}

	public String getTabSelect() {
		return tabSelect;
	}

	public void setTabSelect(String tabSelect) {
		this.tabSelect = tabSelect;
	}

	public List<CierreCaja> getLcierresCaja() {
		return lcierresCaja;
	}

	public void setLcierresCaja(List<CierreCaja> lcierresCaja) {
		this.lcierresCaja = lcierresCaja;
	}

}