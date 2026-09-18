/* 
 * CxcCotizacionRepuestosController.java 
 * Aug 10, 2023
 * Copyright 2023 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
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
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.NumericUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.model.CotizacionRepuesto;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CotizacionRepuestoServicesLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.UpdateException;
import com.casabaca.inventario.ejb.model.InvVendedor;
import com.casabaca.inventario.ejb.model.ProformasCab;
import com.casabaca.inventario.ejb.model.WsEcomComprasCab;
import com.casabaca.inventario.ejb.service.InvVendedorServiceLocal;
import com.casabaca.inventario.ejb.service.ProformasCabServiceLocal;
import com.casabaca.inventario.ejb.service.WSEcomComprasCabServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * <b> Clase para migracion de forma CXCPRO_SOLCRE_DET </b>
 * 
 * @author jorge.reyes
 * @version $1.0$
 */
@ManagedBean
@ViewScoped
public class CxcCotizacionRepuestosController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4980787371687800803L;

	private static final Logger logger = Logger.getLogger(CxcCotizacionRepuestosController.class);

	private static final String MODALIDAD = "B2B";
	
	public static final String CENTRO_CPD = "02";

	private static final String APROBADO = "APROBADO";

	private static final String CANCELADO = "CANCELADO";

	private static final String NEGADA = "NEGADA";

	private static final String PENDIENTE = "PENDIENTE";

	private static final String ESTADOPENDIENTE = "P";

	private static final String RESUMIDO = "/reportes/EstadoCuentaClienteResumido.jasper";

	private static final String DETALLADO = "/reportes/EstadoCuentaClienteDetallado.jasper";

	@EJB(lookup = NombreJNDI.COTIZACION_REPUESTOS_SERVICES_BEAN)
	private CotizacionRepuestoServicesLocal cotizacionRepuestosService;

	@EJB(lookup = NombreJNDI.INV_VENDEDOR_SERVICE)
	private InvVendedorServiceLocal invVendedorServiceLocal;

	@EJB(lookup = NombreJNDI.PROFORMAS_CAB_SERVICE)
	private ProformasCabServiceLocal proformasCabServiceLocal;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@EJB(lookup = NombreJNDI.W_SE_COM_COMPRAS_CAB_SERVICE_BEAN)
	private WSEcomComprasCabServiceLocal wsEcomComprasCabServiceLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaSerice;

	private List<CotizacionRepuesto> listdoCotizacionRepuestos;
	private List<ProformasCab> listdoProformasColaborador;
	private List<Object[]> listadoSaldo;
	private List<LineaNegocio> listLineasNegocio;

	private CotizacionRepuesto cotizacionRepuestoEdited;

	private CotizacionRepuesto cotizacionRepuestoSelected;

	private ProformasCab proformasCabConsultada;

	private WsEcomComprasCab wsEcomComprasCabConsultada;

	private BigDecimal totalSaldo;

	private String codLinea;

	private String tipoReporte;

	private String rutaReporte;

	@PostConstruct
	public void init() {

		listdoProformasColaborador = new ArrayList<ProformasCab>();
		listdoCotizacionRepuestos = new ArrayList<CotizacionRepuesto>();
		listLineasNegocio = new ArrayList<LineaNegocio>();
		listadoSaldo = new ArrayList<Object[]>();
		cotizacionRepuestoEdited = new CotizacionRepuesto();
		proformasCabConsultada = new ProformasCab();
		totalSaldo = BigDecimal.ZERO;
		codLinea = null;
		tipoReporte = null;

		try {

			listdoProformasColaborador = proformasCabServiceLocal.consultaProformasColaborador(getCompania().getNoCia(),
					CommonConstants.YES_STRING_VALUE);

			listdoCotizacionRepuestos = cotizacionRepuestosService.obtenerPorNoCia(getCompania().getNoCia());

			listdoCotizacionRepuestos.stream().forEach(item -> {
				InvVendedor vendedor = invVendedorServiceLocal.buscarVendedorPorNociaCodigo(getCompania().getNoCia(),
						item.getCodVendActual());
				item.setNombreVendedor(vendedor.getNombre() != null ? vendedor.getNombre() : "Sin Nombre");
			});

			listLineasNegocio = lineaSerice.buscarLineaNegocioLista(getCompania().getNoCia());
		} catch (FindException e) {
			logger.error(e);
		}
		
	}

	public void abrirDialogo(CotizacionRepuesto cotizacionRepuesto) {

		cotizacionRepuestoEdited = cotizacionRepuesto;

		accionesDialog("proformas", Boolean.TRUE);

	}

	public void actualizar() {

		try {

			if (cotizacionRepuestoSelected.getDias().compareTo(BigDecimal.TEN) < 0
					&& CommonConstants.MANSUERA.equals(getCompania().getNoCia())) {

				 //Se elimina validacion por Pedido de Ricardo Orellana

			}
			
			if ((cotizacionRepuestoSelected.getNuevoCupoDisponible()).doubleValue()<=0 && cotizacionRepuestoSelected.getDias().doubleValue()==0 ) {
				if (cotizacionRepuestoSelected.getAprobadoCredito().equalsIgnoreCase("S")) {
					cotizacionRepuestoSelected.setAprobadoCredito("P");
				  warn("No puede aprobar el credito  !!NUEVO CUPO DISPONIBLE!! es menor o igual a cero");
				  return;
				}
			}
			

			cotizacionRepuestoSelected.setFechaAprobCred(new Date());
			cotizacionRepuestoSelected.setUsuarioAprob(getUsuario().getUsuario());
			cotizacionRepuestoSelected.setComentarioAprob(
					cotizacionRepuestoSelected.getComentarioAprob().concat(getUsuario().getUsuario()));
			cotizacionRepuestoSelected.setCupoAsignado(cotizacionRepuestoSelected.getLimiteCupo());
			cotizacionRepuestoSelected.setCupoUtilizado(cotizacionRepuestoSelected.getCupoAsignadoNuevo()
					.subtract(cotizacionRepuestoSelected.getCupoDisponbible()));
			proformasCabConsultada.setDiasCredTemporal(cotizacionRepuestoSelected.getDias().doubleValue());
			if (proformasCabConsultada.getPk() != null) {
				proformasCabServiceLocal.actualizar(proformasCabConsultada);

			}

			cotizacionRepuestosService.update(cotizacionRepuestoSelected);
			if (CommonConstants.YES_STRING_VALUE.equals(cotizacionRepuestoSelected.getAprobadoCredito())) {
				wsEcomComprasCabServiceLocal.enviarEstado(proformasCabConsultada.getPk().getNoCia(), APROBADO);
				envioMail2(cotizacionRepuestoSelected);
				if (!CENTRO_CPD.equals(proformasCabConsultada.getCentrod()) && !CommonConstants.YES_STRING_VALUE.equals(proformasCabConsultada.getPedidoCpd()) && proformasCabConsultada.getPedidoEcom() != null) {
					wsEcomComprasCabServiceLocal.grabarFacturaEcommers(proformasCabConsultada.getPk().getNoCia(),wsEcomComprasCabConsultada.getIdCabecera().toString());
				}
			} else if (CommonConstants.NO_STRING_VALUE.equals(cotizacionRepuestoSelected.getAprobadoCredito())) {
				wsEcomComprasCabServiceLocal.enviarEstado(proformasCabConsultada.getPk().getNoCia(), CANCELADO);
				envioMail2(cotizacionRepuestoSelected);
			}
			
			info("Registro Actualizado Con exito !!!");
			envioMail(cotizacionRepuestoSelected);
			init();
			accionesDialog("proformas", Boolean.FALSE);
		} catch (UpdateException e) {
			error("Ocurrio un error al realizar la actualizacion!!!");
			logger.error(e);
		}
	}

	public void envioMail(CotizacionRepuesto cotizacionRepuesto) {
		try {
			String estadoProforma = CommonConstants.YES_STRING_VALUE.equals(cotizacionRepuesto.getAprobadoCredito())
					? APROBADO
					: ESTADOPENDIENTE.equals(cotizacionRepuesto.getAprobadoCredito()) ? PENDIENTE : NEGADA;
			String subject = ("Proforma Generada - ").concat(estadoProforma);
			StringBuffer mensaje = new StringBuffer();
			mensaje.append("Proforma Generada ").append(estadoProforma).append(" No: ")
					.append(cotizacionRepuesto.getNoFisico()).append("<br><br>");
			mensaje.append("LA PROFORMA ES POR EL VALOR DE: $")
					.append(cotizacionRepuesto.getCuotaMensual().multiply(cotizacionRepuesto.getPeriodo()))
					.append("<br><br>");
			mensaje.append("Cliente: ").append(cotizacionRepuesto.getCedulaDeudor()).append(" ")
					.append(cotizacionRepuesto.getNombres()).append(" ").append(cotizacionRepuesto.getApellidos())
					.append("<br><br>");
			mensaje.append("OBSERVACIÓN: ").append(cotizacionRepuesto.getComentarioAprob().toUpperCase());
			UsuarioSis vendedor = new UsuarioSis();
			if (cotizacionRepuesto.getUsuario() != null) {

				vendedor = usuarioServiceLocal.consultarDatosUsuario(cotizacionRepuesto.getUsuario());
				mailService.sendEmail(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(),
						getUsuario().getEmail(), subject, mensaje.toString());
			} else {
				mailService.sendEmailCCNocia(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(),
						vendedor.getEmail(), getUsuario().getEmail(), vendedor.getNombre(), subject.toString(), mensaje,
						Boolean.TRUE);

			}

		} catch (GeneralException e) {
			error("Ocurrio un problema al enviar correo electronico!!!.");
			logger.error(e);
		}
	}
	

	public void envioMail2(CotizacionRepuesto cotizacionRepuesto) {
		try {
			String estadoProforma = CommonConstants.YES_STRING_VALUE.equals(cotizacionRepuesto.getAprobadoCredito())
					? APROBADO
					: ESTADOPENDIENTE.equals(cotizacionRepuesto.getAprobadoCredito()) ? PENDIENTE : NEGADA;
			String subject = ("Proforma Generada - ").concat(estadoProforma);
			StringBuffer mensaje = new StringBuffer();
			mensaje.append("Proforma Generada ").append(estadoProforma).append(" No: ")
					.append(cotizacionRepuesto.getNoFisico()).append("<br><br>");
			mensaje.append("LA PROFORMA ES POR EL VALOR DE: $")
					.append(cotizacionRepuesto.getCuotaMensual().multiply(cotizacionRepuesto.getPeriodo()))
					.append("<br><br>");
			mensaje.append("Cliente: ").append(cotizacionRepuesto.getCedulaDeudor()).append(" ")
					.append(cotizacionRepuesto.getNombres()).append(" ").append(cotizacionRepuesto.getApellidos())
					.append("<br><br>");
			mensaje.append("OBSERVACIÓN: ").append(cotizacionRepuesto.getComentarioAprob().toUpperCase());
			UsuarioSis vendedor = new UsuarioSis();
			vendedor = usuarioServiceLocal.consultarDatosUsuario(invVendedorServiceLocal.buscarVendedorPorNociaCodigo(getCompania().getNoCia(),cotizacionRepuesto.getCodVendActual()).getUsuarioRela());
			
			if (vendedor.getUsuario() != null) {

				mailService.sendEmail(getSisMailServidores(getCompania().getNoCia()), vendedor.getEmail(),
						vendedor.getEmail(), subject, mensaje.toString());
			}else {
				mailService.sendEmailCCNocia(getSisMailServidores(getCompania().getNoCia()), getUsuario().getEmail(),
						vendedor.getEmail(), getUsuario().getEmail(), vendedor.getNombre(), subject.toString(), mensaje,
						Boolean.TRUE);

			}

		} catch (GeneralException e) {
			error("Ocurrio un problema al enviar correo electronico!!!..");
			logger.error(e);
		}
	}

	public void autorizar(ProformasCab proformasCab) {
		try {

			proformasCab.setFacEmpleAutorizado(getUsuario().getUsuario());
			proformasCab.setFacEmpleFechaAut(new Date());

			proformasCabServiceLocal.actualizar(proformasCab);

			info("Registro autorizado Con exito !!!");
			init();
		} catch (UpdateException e) {
			error("Ocurrio un error al realizar la autorizacion!!!");
			logger.error(e);
		}

	}

	public void negar(ProformasCab proformasCab) {
		try {

			proformasCab.setFacEmpleAutoriza(CommonConstants.NO_STRING_VALUE);
			proformasCab.setFacEmpleAutorizado(getUsuario().getUsuario());
			proformasCab.setFacEmpleFechaAut(new Date());

			proformasCabServiceLocal.actualizar(proformasCab);
			info("Registro negado Con exito !!!");
			init();
		} catch (UpdateException e) {
			error("Ocurrio un error al realizar la negar registro!!!");
			logger.error(e);
		}

	}

	public void detalleCotizacion(CotizacionRepuesto cotizacionRepuesto) {
		try {
			cotizacionRepuestoSelected = cotizacionRepuesto;

			Cliente cliente = clienteServiceLocal.findByPk(new ClientePK(cotizacionRepuestoSelected.getId().getNoCia(),
					cotizacionRepuestoSelected.getGrupo(), cotizacionRepuestoSelected.getNoCliente().longValue()));

			if ((cliente.getSegCensal()==null?0:cliente.getSegCensal())!=0) {
				warn("Cliente Bloqueado, no puede Aprobar proforma");
				return;
			}
			
			cotizacionRepuestoSelected
					.setLimiteCupo(BigDecimal.valueOf(cliente.getLimiteCredi() == null ? 0 : cliente.getLimiteCredi()));

			cotizacionRepuestoSelected.setCupoSolicitado(NumericUtils.nvl(cotizacionRepuestoSelected.getCuotaMensual())
					.multiply(cotizacionRepuestoSelected.getPeriodo()));

			cotizacionRepuestoSelected
					.setMonto(arccmdService.obtenerMonto(cotizacionRepuestoSelected.getId().getNoCia(),
							cotizacionRepuestoSelected.getNoCliente().toString()));

			if (ESTADOPENDIENTE.equalsIgnoreCase(cotizacionRepuestoSelected.getAprobadoCredito())) {

				cotizacionRepuestoSelected
						.setCupoAsignadoNuevo(NumericUtils.nvl(cotizacionRepuestoSelected.getLimiteCupo()));

			}
			cotizacionRepuestoSelected
					.setCupoDisponbible(NumericUtils.nvl(cotizacionRepuestoSelected.getCupoAsignadoNuevo())
							.subtract(NumericUtils.nvl(cotizacionRepuestoSelected.getMonto())));
			cotizacionRepuestoSelected
					.setNuevoCupoDisponible(NumericUtils.nvl(cotizacionRepuestoSelected.getCupoDisponbible())
							.subtract(NumericUtils.nvl(cotizacionRepuestoSelected.getCupoSolicitado())));

			proformasCabConsultada = proformasCabServiceLocal.findByNoCiaCentroNoFisicoNoCotizacion(
					cotizacionRepuestoSelected.getId().getNoCia(), cotizacionRepuestoSelected.getCentro(),
					cotizacionRepuestoSelected.getNoFisico(), cotizacionRepuestoSelected.getId().getNumeroCotizacion());
			if (proformasCabConsultada != null) {
				wsEcomComprasCabConsultada = wsEcomComprasCabServiceLocal.consultarWsEcom(
						proformasCabConsultada.getPk().getNoCia(), proformasCabConsultada.getPedidoEcom(), MODALIDAD);
			}
			cotizacionRepuestoSelected
					.setDias(BigDecimal.valueOf(NumericUtils.nvl(proformasCabConsultada.getDiasCredTemporal())));

		} catch (FindException e) {
			cotizacionRepuestoSelected.setLimiteCupo(BigDecimal.ZERO);
		}

		accionesDialog("proformas", Boolean.TRUE);

	}

	public void abrirDialogoSaldo() {

		listadoSaldo = cotizacionRepuestosService.obtenerListadoSaldoCliente(getCompania().getNoCia(),
				cotizacionRepuestoSelected.getCedulaDeudor());
		totalSaldo = BigDecimal.ZERO;
		listadoSaldo.forEach(item -> {

			totalSaldo = totalSaldo.add(new BigDecimal(item[2].toString()));

		});

		accionesDialog("idDlgSaldos", Boolean.TRUE);

	}

	public void onRowSelect(SelectEvent event) {
		cotizacionRepuestoEdited = (((CotizacionRepuesto) event.getObject()));
	}

	public void calcularSobreCupo() {
		if (cotizacionRepuestoSelected.getCupoAsignadoNuevo()
				.compareTo(cotizacionRepuestoSelected.getLimiteCupo()) > 0) {

			BigDecimal valorPorcentaje = BigDecimal.ZERO;

			valorPorcentaje = cotizacionRepuestoSelected.getLimiteCupo().multiply(new BigDecimal("0.10"));

			BigDecimal valorCreditoNuevo = cotizacionRepuestoSelected.getLimiteCupo().add(valorPorcentaje);

			if (valorCreditoNuevo.compareTo(cotizacionRepuestoSelected.getCupoAsignadoNuevo()) != 0) {
				cotizacionRepuestoSelected
						.setCupoAsignadoNuevo(NumericUtils.nvl(cotizacionRepuestoSelected.getLimiteCupo()));

				error("Favor validar que el nuevo cupo no exceda el 10% del cupo asigando al cliente");
			}

		}

		cotizacionRepuestoSelected
				.setCupoDisponbible(NumericUtils.nvl(cotizacionRepuestoSelected.getCupoAsignadoNuevo())
						.subtract(NumericUtils.nvl(cotizacionRepuestoSelected.getMonto())));
		cotizacionRepuestoSelected
				.setNuevoCupoDisponible(NumericUtils.nvl(cotizacionRepuestoSelected.getCupoDisponbible())
						.subtract(NumericUtils.nvl(cotizacionRepuestoSelected.getCupoSolicitado())));

	}

	public void abrirDialogReport() {

		codLinea = null;
		tipoReporte = null;
		accionesDialog("idDlgEstadoCuenta", Boolean.TRUE);

	}

	public void estadoCuentaReport() {

		if (tipoReporte == null || tipoReporte.isEmpty()) {
			error("No olvide seleccionar el tipo de reporte que quiere descargar.");
			return;
		}
		String format = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
				.get("format");
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("NO_CIA", getCompania().getNoCia());
			parameters.put("CIA", getCompania().getNombre());
			parameters.put("NO_CLIENTE", cotizacionRepuestoSelected.getNoCliente());
			parameters.put("SERIE_FISICO", codLinea);

			String ctxPath = getServletContext().getRealPath("/");

			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");
			File[] drives = File.listRoots();
			for (File fileDrives : drives) {
				// Si es windows
				if (fileDrives.getPath().length() > 1 && fileDrives.getPath().substring(0, 1).equals("C")) {
					path = fileDrives.getPath() + path;
				}
			}

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			parameters.put("IMPRIME_CABECERA",
					"excel".equals(format) ? CommonConstants.FALSE_VALUE : CommonConstants.TRUE_VALUE);
			connection = utilServiceDelegate.getDataSource().getConnection();

			switch (tipoReporte) {
			case "R":
				rutaReporte = RESUMIDO;
				break;
			case "D":
				rutaReporte = DETALLADO;
				break;

			default:
				break;
			}

			JasperPrint jasperPrint = JasperFillManager.fillReport(ctxPath + rutaReporte, parameters, connection);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			if ("excel".equals(format)) {
				exportXls(jasperPrint, response);
			}

			if ("pdf".equals(format)) {
				response.setContentType("application/pdf");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			}

			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();

			virtualizer.cleanup();
			accionesDialog("idDlgEstadoCuenta", Boolean.FALSE);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (IOException e) {
			logger.error(e.getMessage(), e.getCause());
		} catch (JRException e) {
			logger.error(e.getMessage(), e.getCause());
		} finally {
			try {
				connection.close();
				accionesDialog("idDlgEstadoCuenta", Boolean.FALSE);
			} catch (SQLException e) {
				logger.error(e.getMessage(), e.getCause());
			}
		}
	}

	public List<CotizacionRepuesto> getListdoCotizacionRepuestos() {
		return listdoCotizacionRepuestos;
	}

	public void setListdoCotizacionRepuestos(List<CotizacionRepuesto> listdoCotizacionRepuestos) {
		this.listdoCotizacionRepuestos = listdoCotizacionRepuestos;
	}

	public CotizacionRepuesto getCotizacionRepuestoEdited() {
		return cotizacionRepuestoEdited;
	}

	public void setCotizacionRepuestoEdited(CotizacionRepuesto cotizacionRepuestoEdited) {
		this.cotizacionRepuestoEdited = cotizacionRepuestoEdited;
	}

	public List<ProformasCab> getListdoProformasColaborador() {
		return listdoProformasColaborador;
	}

	public void setListdoProformasColaborador(List<ProformasCab> listdoProformasColaborador) {
		this.listdoProformasColaborador = listdoProformasColaborador;
	}

	public CotizacionRepuesto getCotizacionRepuestoSelected() {
		return cotizacionRepuestoSelected;
	}

	public void setCotizacionRepuestoSelected(CotizacionRepuesto cotizacionRepuestoSelected) {
		this.cotizacionRepuestoSelected = cotizacionRepuestoSelected;
	}

	public ProformasCab getProformasCabConsultada() {
		return proformasCabConsultada;
	}

	public void setProformasCabConsultada(ProformasCab proformasCabConsultada) {
		this.proformasCabConsultada = proformasCabConsultada;
	}

	public List<Object[]> getListadoSaldo() {
		return listadoSaldo;
	}

	public void setListadoSaldo(List<Object[]> listadoSaldo) {
		this.listadoSaldo = listadoSaldo;
	}

	public BigDecimal getTotalSaldo() {
		return totalSaldo;
	}

	public void setTotalSaldo(BigDecimal totalSaldo) {
		this.totalSaldo = totalSaldo;
	}

	public String getCodLinea() {
		return codLinea;
	}

	public void setCodLinea(String codLinea) {
		this.codLinea = codLinea;
	}

	public String getTipoReporte() {
		return tipoReporte;
	}

	public void setTipoReporte(String tipoReporte) {
		this.tipoReporte = tipoReporte;
	}

	public List<LineaNegocio> getListLineasNegocio() {
		return listLineasNegocio;
	}

	public void setListLineasNegocio(List<LineaNegocio> listLineasNegocio) {
		this.listLineasNegocio = listLineasNegocio;
	}

}
