package com.casabaca.prime.cxc.procesos.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
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

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.ejb.model.Arccda;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.InvControlServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcCuentaCobrarTarjetaCreditoDto;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.cxc.ejb.servicio.ReasignacionCxcTarjetasServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * Controlador para realizar la reasignacion de emisoras de tarjeta de credito
 * 
 * @author cf_yaselga
 *
 */
@ViewScoped
@ManagedBean(name = "cxcReasignacionEmisorTarjetaController")
public class CxcReasignacionEmisorTarjetaController extends CommonController implements Serializable {

	private static final long serialVersionUID = 15348975328954565L;

	/**
	 * VARIABLES DE SERVICIOS
	 */

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaService;

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeService;

	@EJB(lookup = NombreJNDI.REASIGNACION_CXC_TARJETA_SERVICE_BEAN)
	private ReasignacionCxcTarjetasServiceLocal reasignacionService;

	@EJB(lookup = NombreJNDI.INV_CONTROL_SERVICE)
	private InvControlServiceLocal invControlService;

	private List<Cliente> clientes;
	private Cliente clienteSeleccionado;
	private List<CxcCuentaCobrarTarjetaCreditoDto> deudasTarjeta;
	private String filtroVoucher;

	private List<Arccda> diariosResult;
	private Cliente clienteDestino;

	private boolean activarProceso;

	private String mensajeExitoso;
	private String mensajeFallidos;

	@PostConstruct
	public void init() {
		deudasTarjeta = new ArrayList<>();
		clientes= clienteServiceLocal.getClienteTarjetaCredito(getCompania().getNoCia());
		clienteSeleccionado = new Cliente(new ClientePK());
		clienteDestino = new Cliente(new ClientePK());
		filtroVoucher = null;
	}

	public void limpiarClientes() {
		clientes= clienteServiceLocal.getClienteTarjetaCredito(getCompania().getNoCia());
	}

	public void limpiar() {
		init();
	}

	public void cerrarDialogo(){
		accionesDialog("DlgResult", false);
		init();
	}
	
	public void seleccionarClienteDestino() {
		if (clienteDestino != null && clienteDestino.getClientePK() != null) {
			clientes= clienteServiceLocal.getClienteTarjetaCredito(getCompania().getNoCia());
			accionesDialog("DlgClientesDest", false);
		} else {
			warn("Debe seleccionar un cliente");
		}
	}

	public void procesar() {
		try {
			if (deudasTarjeta != null && !deudasTarjeta.isEmpty() && clienteSeleccionado.getClientePK().getNoCliente() != null
					&& clienteDestino.getClientePK().getNoCliente() != null) {
				diariosResult = reasignacionService.reasignarDeudaTarjeta(getUsuario().getUsuario(),
						getCompania().getNoCia(), getUsuarioCentroConectado().getUsuarioCentroPK().getCentro(),
						getCompania().getAplicaOrion(), clienteSeleccionado, clienteDestino, deudasTarjeta);
				info("Se ha ejecutado el proceso con exito");
				accionesDialog("DlgResult", true);
			}else{
				error("Por favor escoja el cliente origen con las deudas a cruzar y el cliente origen");
			}
		} catch (Exception e) {
			error("Error: " + e);
		}
	}

	
	
	public void seleccionarCliente() {
		if (clienteSeleccionado != null && clienteSeleccionado.getClientePK() != null) {
			activarProceso = true;
			accionesDialog("DlgClientes", false);
		} else {
			warn("Debe seleccionar un cliente");
		}
	}

	public void imprimirDiario(Arccda diarioA) {
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
				header.append("DIARIO_A" + diarioA.getNoFisico() + "-" + diarioA.getArccdaPk().getNoDocu() + ".pdf");
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

	private void generarReporteJasper(Arccda diarioA) {
		String reportPath = "/reportes/cxcComprobanteDiariosA.jasper";
		UtilServiceDelegate utilServiceDelegate = new UtilServiceDelegate();
		Connection connection = null;
		JasperPrint jasperPrint = null;

		try {
			Map<String, Object> parameters = new HashMap<String, Object>();
			setParametersDiariosA(parameters, getCompania().getNoCia(), diarioA.getArccdaPk().getNoDocu(),
					diarioA.getUsuario());
			connection = utilServiceDelegate.getDataSource().getConnection();
			String ctxPath = getServletContext().getRealPath("/");

			String path = System.getProperty("file.separator") + FacesContext.getCurrentInstance().getExternalContext()
					.getInitParameter("com.casabaca.vehiculos.web.TMP_FILE_PATH")
					+ System.getProperty("file.separator");

			JRFileVirtualizer virtualizer = new JRFileVirtualizer(10, path);// 50paginas
			parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			jasperPrint = JasperFillManager.fillReport(ctxPath + reportPath, parameters, connection);

			StringBuffer header = new StringBuffer();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();

			header.append("inline; filename=\"");
			header.append("DIARIO_A" + diarioA.getNoFisico() + "-" + diarioA.getArccdaPk().getNoDocu() + ".pdf");
			header.append("\"");
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());

			FacesContext.getCurrentInstance().getApplication().getStateManager()
					.saveView(FacesContext.getCurrentInstance());
			FacesContext.getCurrentInstance().responseComplete();
			response.getOutputStream().flush();
			response.getOutputStream().close();
			virtualizer.cleanup();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void setParametersDiariosA(Map<String, Object> parameters, String noCia, String noDocu, String usuario) {
		parameters.put("P_NO_CIA", noCia);
		parameters.put("P_NO_DOCU", noDocu);
		parameters.put("USUARIO", usuario);
		parameters.put("EMPRESA", getCompania().getNombre());
		parameters.put("SUBREPORT_DIR", getPathReal());

	}

	@SuppressWarnings({ "rawtypes", "unused" })
	public void listener(FileUploadEvent event) throws FindException {
		String result = "";
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		UploadedFile file = event.getFile();
		mensajeExitoso = new String();
		mensajeFallidos = new String();
		try {
			int registrosProcesados = 0;

			List<Object[]> objetos = cargarArchivoExcel(event.getFile().getInputstream(), Boolean.TRUE, 0);
			int existoso = 0;
			int fallidos = 0;
			int fila = 1;
			for (Object[] row : objetos) {
				fila++;
				if (row[0] != null && !String.valueOf(row[0]).isEmpty()) {
					try {

						CxcCuentaCobrarTarjetaCreditoDto registro = new CxcCuentaCobrarTarjetaCreditoDto();
						registro.setAgencia(obtenerString(row[0]));
						registro.setCedula(obtenerString(row[1]));
						registro.setCliente(obtenerString(row[2]));
						registro.setFecha((Date) row[4]);
						registro.setNumeroCC(obtenerString(row[6]));
						registro.setInternoCC(obtenerString(row[7]));
						registro.setEmisor(obtenerString(row[8]));
						
						registro.setNoLinea(obtenerString(row[10]));
						registro.setRecap(obtenerString(row[13]));
						registro.setMontoTotal(new BigDecimal(obtenerString(row[14])));
						if(String.valueOf(clienteSeleccionado.getClientePK().getNoCliente()).equals(registro.getEmisor().trim())){
							//Valida que el registro tenga el mismo noCliente que el seleccionado en la pantalla
							existoso++;
							deudasTarjeta.add(registro);	
						}else{
							mensajeFallidos = mensajeFallidos + "\n El registro de la fila " + fila+" no pertenece a la entidad emisora de tarjeta seleccionada";
							fallidos++;
						}
						
					} catch (Exception e) {
						mensajeFallidos = mensajeFallidos + "\n El registro de la fila " + fila
								+ " Tuvo un inconveniente al cargar, por favor valide que el formato se encuentre correcto: "
								+ e + "; <br/>";
						fallidos++;
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			int total = existoso + fallidos;
			if (fallidos == 0) {
				mensajeExitoso = "Se proceso con exito " + existoso + " registros de " + total;
				addInfoMessage("EXITO", mensajeExitoso);

			} else {
				mensajeExitoso = "Se proceso con exito " + existoso + " registros de " + total + ", han fallado "
						+ fallidos + " registros";
				addWarnMessage("ATENCION", mensajeExitoso);
			}
		} catch (EncryptedDocumentException e2) {
			mensajeFallidos = "Ocurrio un error en el procesamiento del archivo, por favor valide que los datos se encuentren en el orden correcto: "
					+ e2;
			addErrorMessage("ERROR", mensajeFallidos);
			e2.printStackTrace();
		} catch (InvalidFormatException e2) {
			mensajeFallidos = "Ocurrio un error en el procesamiento del archivo, por favor valide que los datos se encuentren en el orden correcto: "
					+ e2;
			addErrorMessage("ERROR", mensajeFallidos);
			e2.printStackTrace();
		} catch (IOException e2) {
			mensajeFallidos = "Ocurrio un error en el procesamiento del archivo, por favor valide que los datos se encuentren en el orden correcto: "
					+ e2;
			addErrorMessage("ERROR", mensajeFallidos);
			e2.printStackTrace();
		} catch (Exception e2) {
			mensajeFallidos = "Ocurrio un error en el procesamiento del archivo, por favor valide que los datos se encuentren en el orden correcto: "
					+ e2;
			addErrorMessage("ERROR", mensajeFallidos);
			e2.printStackTrace();
		}

	}

	private String obtenerString(Object a) {
		if (a == null) {
			return null;
		} else if ("null".equals(a.toString())) {
			return null;
		}
		return a.toString();

	}

	/**
	 * Metodo para buscar una deuda de la tarjeta por el numero de voucher
	 */
	public void buscarDeudaTarjeta() {
		try {
			deudasTarjeta= reasignacionService.getCuentasCobrarTarjetas(getCompania().getNoCia(),String.valueOf(clienteSeleccionado.getClientePK().getNoCliente()), filtroVoucher);
			if(deudasTarjeta == null || deudasTarjeta.isEmpty()){
				error("No se encontraron resultados con los datos ingresados");	
			}
		} catch (FindException e) {
			error("No se encontraron resultados con los datos ingresados");
		}
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}

	public void setClienteSeleccionado(Cliente clienteSeleccionado) {
		this.clienteSeleccionado = clienteSeleccionado;
	}

	public List<CxcCuentaCobrarTarjetaCreditoDto> getDeudasTarjeta() {
		return deudasTarjeta;
	}

	public void setDeudasTarjeta(List<CxcCuentaCobrarTarjetaCreditoDto> deudasTarjeta) {
		this.deudasTarjeta = deudasTarjeta;
	}

	public List<Arccda> getDiariosResult() {
		return diariosResult;
	}

	public void setDiariosResult(List<Arccda> diariosResult) {
		this.diariosResult = diariosResult;
	}

	public Cliente getClienteDestino() {
		return clienteDestino;
	}

	public void setClienteDestino(Cliente clienteDestino) {
		this.clienteDestino = clienteDestino;
	}

	public boolean isActivarProceso() {
		return activarProceso;
	}

	public void setActivarProceso(boolean activarProceso) {
		this.activarProceso = activarProceso;
	}

	public String getMensajeExitoso() {
		return mensajeExitoso;
	}

	public void setMensajeExitoso(String mensajeExitoso) {
		this.mensajeExitoso = mensajeExitoso;
	}

	public String getMensajeFallidos() {
		return mensajeFallidos;
	}

	public void setMensajeFallidos(String mensajeFallidos) {
		this.mensajeFallidos = mensajeFallidos;
	}

	public String getFiltroVoucher() {
		return filtroVoucher;
	}

	public void setFiltroVoucher(String filtroVoucher) {
		this.filtroVoucher = filtroVoucher;
	}

}