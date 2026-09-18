package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.dto.DiairosTransferenciaMismaEmpresaDTO;
import com.casabaca.common.ejb.dto.DireccionesMail;
import com.casabaca.common.ejb.model.Arcktd;
import com.casabaca.common.ejb.model.Banco;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ConfirmarDeposito;
import com.casabaca.common.ejb.model.ConfirmarDepositoPK;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ArccdaServiceLocal;
import com.casabaca.common.ejb.service.ArckCeServiceLocalCP;
import com.casabaca.common.ejb.service.ArcktdServiceLocal;
import com.casabaca.common.ejb.service.BancoServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.ConfirmarDepositoServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxCdatosCtasBancariasDto;
import com.casabaca.cxc.ejb.servicio.CxcdatosCtasBancariasServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.lazy.LazyDataModelClientes;
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
 * Controlador para verificar Diarios A generados por transferencias a Misma
 * Empresa
 * 
 * @author fb_fabara
 *
 */
@ViewScoped
@ManagedBean(name = "cxcDiairosTransferenciaMismaEmpresaController")
public class CxcDiairosTransferenciaMismaEmpresaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 538432425301260739L;

	private static final Logger log = Logger.getLogger(CxcDiairosTransferenciaMismaEmpresaController.class);

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.ARCCDA_SERVICE)
	private ArccdaServiceLocal arccdaService;

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuariosServicio;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.ARCK_CE_SERVICE_LOCAL_CP)
	private ArckCeServiceLocalCP arckceService;

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetService;

	@EJB(lookup = NombreJNDI.ARCKTD_SERVICE_BEAN)
	private ArcktdServiceLocal arcktdService;

	@EJB(lookup = NombreJNDI.CXC_DATOS_CTAS_BANCARIAS)
	private CxcdatosCtasBancariasServiceLocal ctasbancariasService;

	@EJB(lookup = NombreJNDI.CONFIRMAR_DEPOSITO_SERVICE)
	private ConfirmarDepositoServiceLocal confirmarDepositoService;

	@EJB(lookup = NombreJNDI.BANCO_SERVICE)
	private BancoServiceLocal bancoService;

	private LazyDataModelClientes clientes;
	private Cliente clienteSeleccionado;
	private String noFisicoInicial;
	private String noFisicoFinal;
	private Date fechaInicial;
	private Date fechaFinal;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private String envioMail;
	private String noCtaBanco;
	private UsuarioSis usuarioSeleccionado;
	private List<DiairosTransferenciaMismaEmpresaDTO> listarDiariosTransferenciaMismaEmpresa;
	private List<DiairosTransferenciaMismaEmpresaDTO> listaDiariosSeleccionados;
	private List<Arcktd> listarTiposDocumentos;
	private List<SelectItem> listaSelectItemsDocumentos;
	private List<CxCdatosCtasBancariasDto> listaCtasBancarias;
	private List<SelectItem> ctasBancariasSelectItems;
	private ConfirmarDeposito confirmacDeposito;

	@PostConstruct
	public void init() {
		this.clienteSeleccionado = new Cliente();
		this.clientes = new LazyDataModelClientes(0, 10, getCompania().getNoCia());
		this.listarDiariosTransferenciaMismaEmpresa = new ArrayList<>();
		this.listaDiariosSeleccionados = new ArrayList<>();
		this.listaCtasBancarias = new ArrayList<>();
		this.listarTiposDocumentos = new ArrayList<>();
		this.listaSelectItemsDocumentos = new ArrayList<>();
		this.listaCtasBancarias = new ArrayList<>();
		this.ctasBancariasSelectItems = new ArrayList<>();
		this.confirmacDeposito = new ConfirmarDeposito();
		cargarTipoDocumentos();
		cargarCtasBancarias();
	}

	public void limpiar() {
		noFisicoInicial = null;
		noFisicoFinal = null;
		fechaInicial = null;
		fechaFinal = null;
		clienteSeleccionado = new Cliente();
		this.listarDiariosTransferenciaMismaEmpresa = new ArrayList<>();
	}

	public void buscarDiarios() {
		String noCliente = "";
		Cliente noClienteEmpresa = new Cliente();

		try {
			noClienteEmpresa = clienteServiceLocal.buscarPorCedulaNoCia(getCompania().getIdTributario(),
					getCompania().getNoCia());
			if (noClienteEmpresa == null) {
				warn("Verifique el cliente con cdeula " + getCompania().getIdTributario() + " no fue encontrado");
			} else {
				if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null
						&& clienteSeleccionado.getClientePK().getNoCliente() != null) {
					noCliente = String.valueOf(clienteSeleccionado.getClientePK().getNoCliente());
				}

				this.listarDiariosTransferenciaMismaEmpresa = arccdaService.consultaDiariosTransferenciasMismaEmpresa(
						getCompania().getNoCia(), fechaInicial, fechaFinal, noCliente,
						String.valueOf(noClienteEmpresa.getClientePK().getNoCliente()), noFisicoInicial, noFisicoFinal,
						envioMail, noCtaBanco);

			}
		} catch (FindException e) {
			log.error(e);
		}

	}

	public void imprimirDiario(DiairosTransferenciaMismaEmpresaDTO diarioA) {
		if (diarioA.getRutaArchivo() == null || diarioA.getRutaArchivo().isEmpty()) {
			generarReporteJasper(diarioA);
			return;
		}

		try {
			String destPath = null;
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			destPath = diarioA.getRutaArchivo();

			File file = new File(destPath);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			long length = file.length();
			if (!ctx.getResponseComplete()) {
				byte[] fileBytes = file.getName().getBytes();

				StringBuffer header = new StringBuffer();
				header.append("filename=\"");
				header.append("DIARIO_A" + diarioA.getNumeroDiario() + "-" + diarioA.getNoDocu() + ".pdf");
				header.append("\"");
				response.setHeader("Content-Disposition", header.toString());
				response.setContentType("application/pdf");
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

	@SuppressWarnings("deprecation")
	public void generarReporteJasper(DiairosTransferenciaMismaEmpresaDTO diarioA) {

		try {

			String reportPath = "/reportes/cxcComprobanteDiariosA.jasper";
			UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
			Connection connection = null;
			JasperPrint jasperPrint = null;

			try {
				Map<String, Object> parameters = new HashMap<String, Object>();
				setParametersDiariosA(parameters, this.getCompania().getNoCia(), diarioA.getNoDocu(),
						diarioA.getUsuario());
				connection = utilServiceDelegate.getDataSource().getConnection();
				String ctxPath = getServletContext().getRealPath("/");
				
				String path = System.getProperty("file.separator")
						+ FacesContext.getCurrentInstance().getExternalContext()
								.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
						+ System.getProperty("file.separator");

				
				JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
				parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

				jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);
				virtualizer.cleanup();
				StringBuffer header = new StringBuffer();
				HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
						.getExternalContext().getResponse();
				header.append("inline; filename=\"");
				header.append("comprobantesDiarioA.pdf");
				header.append("\"");
				JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

				FacesContext.getCurrentInstance().getApplication().getStateManager()
						.saveView(FacesContext.getCurrentInstance());
				FacesContext.getCurrentInstance().responseComplete();
				response.getOutputStream().flush();
				response.getOutputStream().close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (JRException ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (connection != null) {
						connection.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	private void setParametersDiariosA(Map<String, Object> parameters, String noCia, String noDocu, String usuario) {
		parameters.put("P_NO_CIA", noCia);
		parameters.put("P_NO_DOCU", noDocu);
		parameters.put("USUARIO", usuario);
		parameters.put("EMPRESA", getCompania().getNombre());
		parameters.put("SUBREPORT_DIR", getPathReal());

	}

	public void guardarNumerosConciliaion() {
		try {
			for (DiairosTransferenciaMismaEmpresaDTO diarioActualizar : listarDiariosTransferenciaMismaEmpresa) {
				if (diarioActualizar.getNumeroConcilicacion() != null
						&& !diarioActualizar.getNumeroConcilicacion().isEmpty()) {
					if (valicacion(diarioActualizar)) {

						arccdaService.actualizarNumeroConciliacionArccda(getCompania().getNoCia(),
								diarioActualizar.getNoDocu(), diarioActualizar.getNumeroConcilicacion());
						arccdaService.actualizarNumeroConciliacionArccmd(getCompania().getNoCia(),
								diarioActualizar.getNoDocu(), diarioActualizar.getNumeroConcilicacion());

						ConfirmarDepositoPK pk = new ConfirmarDepositoPK();
						pk.setNoCia(getCompania().getNoCia());
						pk.setBanco(diarioActualizar.getBancoSigla());
						pk.setNoCta(diarioActualizar.getNoCuenta());
						pk.setFechaDeposito(diarioActualizar.getFechaDeposito());
						pk.setNoFisico(diarioActualizar.getNumeroConcilicacion());

						this.confirmacDeposito = new ConfirmarDeposito();
						this.confirmacDeposito.setId(pk);
						this.confirmacDeposito.setValor(diarioActualizar.getMonto().doubleValue());
						this.confirmacDeposito.setComentario(diarioActualizar.getComentar());
						this.confirmacDeposito.setGrupo(diarioActualizar.getGrupo());
						this.confirmacDeposito.setNoCliente(Integer.valueOf(diarioActualizar.getClienteOriginal()));
						this.confirmacDeposito.setTipoMovimiento(diarioActualizar.getTipoMovimiento());
						this.confirmacDeposito.setFechaCarga(new Date());

						confirmarDepositoService.create(this.confirmacDeposito);

						info("Datos almacenados con exito para los siguientes Diarios A "
								+ diarioActualizar.getNumeroDiario());
					}
				}
			}
			buscarDiarios();
			enviarNotifiacion();
			
		} catch (Exception e) {
			log.error(e.getCause() + " " + e.getMessage());
			error("Error al guardar numeros de conciliacion");
		}

	}

	public boolean valicacion(DiairosTransferenciaMismaEmpresaDTO datosValidados) {
		boolean validado = false;
		if ((datosValidados.getNumeroConcilicacion() != null && !datosValidados.getNumeroConcilicacion().isEmpty())
				&& datosValidados.getFechaDeposito() != null
				&& (datosValidados.getTipoMovimiento() != null && !datosValidados.getTipoMovimiento().isEmpty())) {
			validado = true;
		}else {
			error("Debe ingresar los siguientes campos No. Conciliacion, Fecha Deposito y Tipo Movimiento");
			validado = false;
		}
		return validado;
	}
	
	public void enviarNotifiacion() {
		UsuarioSis usuario = new UsuarioSis();
		UsuarioSis usuarioSolicita = new UsuarioSis();
		String emailNotificaciones = null;
		String emailAdministrador = null;
		String nombreAdministrador = null;
		StringBuffer mensaje = new StringBuffer();
		List<String> gruposJefes = new ArrayList<String>();
		String codigoBanco = null;
		Banco banco = new Banco();

		mensaje.append("<div style='font-size:12px;'>");
		mensaje.append("Por medio del presente notificamos que se generaron las siguientes transferencias: <br><br>");

		mensaje.append(
				"<table border='1' cellspacing='0' cellpadding='1' style='font-size:11px; border-collapse:collapse;' >");
		mensaje.append("<tr style='background: #A9A9A9' >");
		mensaje.append("<td><b>No. Diario</b></td>");
		mensaje.append("<td><b>No. Cliente Original</b></td>");
		mensaje.append("<td><b>Cedula cliente Original</b></td>");
		mensaje.append("<td><b>Nombre</b></td>");
		mensaje.append("<td><b>No. Cliente</b></td>");
		mensaje.append("<td><b>Cedula</b></td>");
		mensaje.append("<td><b>Beneficiario</b></td>");
		mensaje.append("<td><b>Monto</b></td>");
		mensaje.append("<td><b>Comentario</b></td>");
		mensaje.append("<td><b>Docu.Conciliaicon</b></td>");
		mensaje.append("</tr>");

		try {
			for (DiairosTransferenciaMismaEmpresaDTO diariosSeleccionados : this.listaDiariosSeleccionados) {

				if (!diariosSeleccionados.getDocumentoConcilia().isEmpty()) {
					codigoBanco = diariosSeleccionados.getBancoSigla();
					
					mensaje.append("<tr ");

					if (this.listaDiariosSeleccionados.indexOf(diariosSeleccionados) % 2 == 0) {
						mensaje.append("style='background: #e2e2e2'");
					}

					mensaje.append(">");
					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getNumeroDiario().trim().toUpperCase());
					mensaje.append("</td>");

					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getClienteOriginal().trim().toUpperCase());
					mensaje.append("</td>");

					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getCedulaOriginal().toString().toUpperCase().trim());
					mensaje.append("</td>");

					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getNombreOriginal().toString().toUpperCase().trim());
					mensaje.append("</td>");

					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getNoCliente().trim().toUpperCase());
					mensaje.append("</td>");

					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getCedula().toString().toUpperCase().trim());
					mensaje.append("</td>");

					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getNombreCliente().toString().toUpperCase().trim());
					mensaje.append("</td>");

					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getMonto().toString().toUpperCase().trim());
					mensaje.append("</td>");

					if (diariosSeleccionados.getComentar() != null) {
						mensaje.append("<td> ");
						mensaje.append(diariosSeleccionados.getComentar().toString().toUpperCase().trim());
						mensaje.append("</td>");
					} else {
						mensaje.append("<td> ");
						mensaje.append(" - ");
						mensaje.append("</td>");
					}
					mensaje.append("<td> ");
					mensaje.append(diariosSeleccionados.getDocumentoConcilia() == null ? "SIN CONCILICACION"
							: diariosSeleccionados.getDocumentoConcilia().trim().toUpperCase());
					mensaje.append("</td>");
					mensaje.append("</tr>");

				}
			}

			mensaje.append("</table>");

			mensaje.append("<br><br><br>Saludos Cordiales.");
			mensaje.append("</div>");

		} catch (Exception e1) {
			log.error(e1);
		}
		try {

			banco = bancoService.getBancoDescripcion(codigoBanco);

			usuario = usuariosServicio
					.getUsuarioSisByUsername(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());

			if (usuario != null) {
				emailAdministrador = usuario.getEmail();
				nombreAdministrador = usuario.getNombre();
			}

			List<ParamDet> remitente = paramDetService.consultarPorCodigoCab(this.getCompania().getNoCia(),
					CommonConstants.CODIGO_CATALOGO_MAIL_NOTIFICACIONES);
			emailNotificaciones = remitente != null && !remitente.isEmpty() ? remitente.get(0).getTexto1() : null;
			if (emailNotificaciones == null) {
				warn("No cuenta con um mail de notificaciones parametrizado en ParamDet Codigo "
						+ CommonConstants.CODIGO_CATALOGO_MAIL_NOTIFICACIONES);
			} else {
				// ENVIO MAIL USUARIO QUE GNERO DIARIO
				mailService.sendEmail(emailNotificaciones, emailAdministrador, nombreAdministrador,
						"NOTIFICACION TRANSFERENCIAS RECIBIDAS " + banco.getDescrip(), mensaje, true);
			}
			// GRUPO DE MAIL CREDITO - CONTABILIDAD - COBRANZAS
			gruposJefes.add(getCompania().getNoCia()+"NTB");
			enviarMailGrupos("NOTIFICACION TRANSFERENCIAS RECIBIDAS " + banco.getDescrip(), mensaje, gruposJefes, null);

			for (DiairosTransferenciaMismaEmpresaDTO diariosSeleccionados : this.listaDiariosSeleccionados) {
				if (!diariosSeleccionados.getDocumentoConcilia().isEmpty()) {
					arccdaService.actualizarEnvioNotificacionArccda(getCompania().getNoCia(),
							diariosSeleccionados.getNoDocu());
					arccdaService.actualizarEnvioNotificacionArccmd(getCompania().getNoCia(),
							diariosSeleccionados.getNoDocu());

					info("Notifiaciones enviadas");
				} else {
					error("No se pudo actualizar el estado de envio de mail");
				}

			}
			buscarDiarios();

		} catch (Exception e) {
			log.error(e);
		}

	}

	public void enviarMailGrupos(String subject, StringBuffer mensaje, List<String> gruposJefes,
			List<DireccionesMail> mailsIndividuales) throws Exception, Exception {
		if (gruposJefes != null && gruposJefes.size() > 0) {
			for (int i = 0; i < gruposJefes.size(); i++) {
				mailService.sendMailToGroup(gruposJefes.get(i), getUsuario().getEmail(), subject, mensaje, true);
			}
		}
	}

	public void cargarTipoDocumentos() {
		try {
			this.listarTiposDocumentos = arcktdService.listarTiposDocumentos(this.getCompania().getNoCia());
			for (Arcktd documentoRecuperado : listarTiposDocumentos) {
				this.listaSelectItemsDocumentos.add(
						new SelectItem(documentoRecuperado.getId().getTipoDoc(), documentoRecuperado.getDescrip()));
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void cargarCtasBancarias() {
		try {

			this.listaCtasBancarias = ctasbancariasService.buscarCtasBancariasPorCia(getCompania().getNoCia());
			Optional<List<CxCdatosCtasBancariasDto>> cuentasBancarias = Optional.ofNullable(this.listaCtasBancarias);
			if (cuentasBancarias.isPresent()) {
				for (CxCdatosCtasBancariasDto item : this.listaCtasBancarias) {
					getCtasBancariasSelectItems().add(new SelectItem(item.getNoCuenta().trim().toUpperCase().toString(),
							item.getDescripcion().trim().toUpperCase().toString()));
				}
			}
		} catch (Exception e) {
			warn("No se recuperaron Cuentas Bancarias");
			log.error(e);
		}
	}

	public void seleccionarCliente() {
		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null) {
			accionesDialog("DlgClientes", false);
		} else {
			warn("Debe seleccionar un cliente");
		}
	}

	public LazyDataModelClientes getClientes() {
		return clientes;
	}

	public void setClientes(LazyDataModelClientes clientes) {
		this.clientes = clientes;
	}

	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}

	public void setClienteSeleccionado(Cliente clienteSeleccionado) {
		this.clienteSeleccionado = clienteSeleccionado;
	}

	public String getNoFisicoInicial() {
		return noFisicoInicial;
	}

	public void setNoFisicoInicial(String noFisicoInicial) {
		this.noFisicoInicial = noFisicoInicial;
	}

	public String getNoFisicoFinal() {
		return noFisicoFinal;
	}

	public void setNoFisicoFinal(String noFisicoFinal) {
		this.noFisicoFinal = noFisicoFinal;
	}

	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	public String getCedulaClienteConsulta() {
		return cedulaClienteConsulta;
	}

	public void setCedulaClienteConsulta(String cedulaClienteConsulta) {
		this.cedulaClienteConsulta = cedulaClienteConsulta;
	}

	public String getNombreClienteConsulta() {
		return nombreClienteConsulta;
	}

	public void setNombreClienteConsulta(String nombreClienteConsulta) {
		this.nombreClienteConsulta = nombreClienteConsulta;
	}

	public UsuarioSis getUsuarioSeleccionado() {
		return usuarioSeleccionado;
	}

	public void setUsuarioSeleccionado(UsuarioSis usuarioSeleccionado) {
		this.usuarioSeleccionado = usuarioSeleccionado;
	}

	public List<DiairosTransferenciaMismaEmpresaDTO> getListarDiariosTransferenciaMismaEmpresa() {
		return listarDiariosTransferenciaMismaEmpresa;
	}

	public void setListarDiariosTransferenciaMismaEmpresa(
			List<DiairosTransferenciaMismaEmpresaDTO> listarDiariosTransferenciaMismaEmpresa) {
		this.listarDiariosTransferenciaMismaEmpresa = listarDiariosTransferenciaMismaEmpresa;
	}

	public List<DiairosTransferenciaMismaEmpresaDTO> getListaDiariosSeleccionados() {
		return listaDiariosSeleccionados;
	}

	public void setListaDiariosSeleccionados(List<DiairosTransferenciaMismaEmpresaDTO> listaDiariosSeleccionados) {
		this.listaDiariosSeleccionados = listaDiariosSeleccionados;
	}

	public String getEnvioMail() {
		return envioMail;
	}

	public void setEnvioMail(String envioMail) {
		this.envioMail = envioMail;
	}

	public List<Arcktd> getListarTiposDocumentos() {
		return listarTiposDocumentos;
	}

	public void setListarTiposDocumentos(List<Arcktd> listarTiposDocumentos) {
		this.listarTiposDocumentos = listarTiposDocumentos;
	}

	public List<SelectItem> getListaSelectItemsDocumentos() {
		return listaSelectItemsDocumentos;
	}

	public void setListaSelectItemsDocumentos(List<SelectItem> listaSelectItemsDocumentos) {
		this.listaSelectItemsDocumentos = listaSelectItemsDocumentos;
	}

	public List<CxCdatosCtasBancariasDto> getListaCtasBancarias() {
		return listaCtasBancarias;
	}

	public void setListaCtasBancarias(List<CxCdatosCtasBancariasDto> listaCtasBancarias) {
		this.listaCtasBancarias = listaCtasBancarias;
	}

	public List<SelectItem> getCtasBancariasSelectItems() {
		return ctasBancariasSelectItems;
	}

	public void setCtasBancariasSelectItems(List<SelectItem> ctasBancariasSelectItems) {
		this.ctasBancariasSelectItems = ctasBancariasSelectItems;
	}

	public String getNoCtaBanco() {
		return noCtaBanco;
	}

	public void setNoCtaBanco(String noCtaBanco) {
		this.noCtaBanco = noCtaBanco;
	}

	public ConfirmarDeposito getConfirmacDeposito() {
		return confirmacDeposito;
	}

	public void setConfirmacDeposito(ConfirmarDeposito confirmacDeposito) {
		this.confirmacDeposito = confirmacDeposito;
	}

}