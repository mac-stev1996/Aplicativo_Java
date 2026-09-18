package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import com.casabaca.common.FileUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.cml.model.CxcTipoCliente;
import com.casabaca.cxc.cml.model.CxcTipoClientePK;
import com.casabaca.cxc.cml.model.CxcTipoclienteDetalle;
import com.casabaca.cxc.cml.model.CxcTipoclienteDetallePK;
import com.casabaca.cxc.ejb.servicio.CxcTipoClienteDetalleServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcTipoClienteMantenimientoServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogClientesController;

@ManagedBean
@ViewScoped
public class CxcTipoClienteDetalleController extends CommonController implements Serializable {

	private static final long serialVersionUID = 7093894463892749864L;
	static final Logger logger = Logger.getLogger(CxcTipoClienteDetalleController.class);

	@EJB(lookup = NombreJNDI.CXC_MAN_TIPO_CLIENTE_SERVICE)
	private CxcTipoClienteMantenimientoServiceLocal tipoClienteService;

	@EJB(lookup = NombreJNDI.CXC_TIPO_CLIENTE_DETALLE_SERVICE)
	private CxcTipoClienteDetalleServiceLocal tipoClienteDetalleService;

	@EJB
	private CxcTipoClienteDetalleDoc tipoClienteDetalleDoc;

	@ManagedProperty("#{dialogClientesController}")
	private DialogClientesController dialogClientesController;

	private String noCia;
	private Cliente cliente;
	private List<CxcTipoclienteDetalle> listaTipoclienteDetalle;
	private List<CxcTipoCliente> listaTipoCliente;
	private CxcTipoCliente tipoClienteSelected;
	private CxcTipoclienteDetalle cxcTipoclienteDetalle;

	public CxcTipoClienteDetalleController() {
	}

	@PostConstruct
	public void init() {
		this.noCia = getCompania().getNoCia();
		inicializaObjetoCarga();
	}

	private void inicializaObjetoCarga() {
		cliente = new Cliente(new ClientePK());
		tipoClienteSelected = new CxcTipoCliente(new CxcTipoClientePK());
		listaTipoclienteDetalle = new ArrayList<>();
		listaTipoCliente = new ArrayList<>();
		dialogClientesController.setCliente(new Cliente());
		consultarDetalle();
	}

	public void agregar() {
		listaTipoCliente = consultarTipoCliente();
		cxcTipoclienteDetalle = new CxcTipoclienteDetalle(new CxcTipoclienteDetallePK());
		cxcTipoclienteDetalle.getId().setNoCia(this.noCia);
		tipoClienteSelected = new CxcTipoCliente(new CxcTipoClientePK());
		cliente = new Cliente(new ClientePK());
		dialogClientesController.setCliente(new Cliente());
		accionesDialog("dlgNuevo", Boolean.TRUE);
	}

	public void guardar() {
		// OBS 3: validación de cliente seleccionado
		if (Objects.isNull(cliente.getClientePK().getNoCliente())) {
			error("Por favor seleccione un cliente.");
			return;
		}

		// OBS 3: validación de tipo cliente seleccionado
		if (Objects.isNull(tipoClienteSelected.getId().getTipoCliente())) {
			error("Por favor seleccione un Tipo Cliente.");
			return;
		}

		// OBS 3: validación de Tipo Conozca obligatorio
		if (cxcTipoclienteDetalle.getTipoConozca() == null || cxcTipoclienteDetalle.getTipoConozca().trim().isEmpty()) {
			error("El campo Tipo Conozca es obligatorio.");
			return;
		}

		// OBS 4: validación de Estado obligatorio
		if (cxcTipoclienteDetalle.getEstado() == null || cxcTipoclienteDetalle.getEstado().trim().isEmpty()) {
			error("El campo Estado es obligatorio.");
			return;
		}

		// OBS 5: un cliente solo puede pertenecer a un Tipo Cliente
		String identificacion = cliente.getCedula();
		boolean cedulaExiste = listaTipoclienteDetalle.stream()
				.anyMatch(d -> d.getId().getIdentificacion().equals(identificacion));

		if (cedulaExiste) {
			error("El cliente con identificación '" + identificacion + "' ya tiene un Tipo Cliente asignado. "
					+ "Un cliente solo puede pertenecer a un Tipo Cliente.");
			return;
		}

		try {
			cxcTipoclienteDetalle.getId().setNoCia(this.noCia);
			cxcTipoclienteDetalle.getId().setTipoCliente(tipoClienteSelected.getId().getTipoCliente());
			cxcTipoclienteDetalle.getId().setNoLinea(tipoClienteSelected.getId().getNoLinea());
			cxcTipoclienteDetalle.getId().setIdentificacion(identificacion);
			cxcTipoclienteDetalle.setNombre(cliente.getNombre());
			cxcTipoclienteDetalle.setFechaCrea(new Date());
			cxcTipoclienteDetalle.setUsuarioCrea(getUsuario().getUsuario());
			tipoClienteDetalleService.guardar(cxcTipoclienteDetalle);
			consultarDetalle();
			accionesDialog("dlgNuevo", Boolean.FALSE);
			info("Datos guardados con éxito.");
		} catch (InsertException e) {
			error("Ocurrió un problema al grabar: " + e.getCause());
			logger.error("Error al guardar cxcTipoclienteDetalle", e);
		}
	}

	public void eliminar(CxcTipoclienteDetalle item) {
		try {
			tipoClienteDetalleService.eliminar(item);
			consultarDetalle();
			info("Registro eliminado correctamente.");
		} catch (Exception e) {
			error("Ocurrió un problema al eliminar: " + (e.getCause() != null ? e.getCause() : e.getMessage()));
			logger.error("Error al eliminar cxcTipoclienteDetalle", e);
		}
	}

	public void limpiarDatos() {
		inicializaObjetoCarga();
	}

	public void abrirDialogoTipoCliente() {
		listaTipoCliente = consultarTipoCliente();
		accionesDialog("dlgTipoCliente", Boolean.TRUE);
	}

	public void seleccionarTipoCliente(CxcTipoCliente tipoCliente) {
		this.tipoClienteSelected = tipoCliente;
	}

	public void cargarClientes() {
		this.dialogClientesController.setNombreDialog("dialogClientesWV");
		this.dialogClientesController.cargarClientes();
		accionesDialog("dialogClientesWV", Boolean.TRUE);
	}

	public Cliente getCliente() {
		if (Objects.nonNull(dialogClientesController.getCliente())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK())
				&& Objects.nonNull(dialogClientesController.getCliente().getClientePK().getNoCliente())) {
			cliente = dialogClientesController.getCliente();
			dialogClientesController.setCliente(null);
		}
		return cliente;
	}

	public void consultarDetalle() {
		listaTipoclienteDetalle = tipoClienteDetalleService.listarPorNoCia(this.noCia);
	}

	private List<CxcTipoCliente> consultarTipoCliente() {
		return tipoClienteService.listarTipoCli(this.noCia);
	}

	public void abrirCargaExcel() {
		accionesDialog("dlgArchivo", Boolean.TRUE);
	}

	public void descargarFormato() {
		try {
			byte[] archivo = tipoClienteDetalleDoc.generarPlantilla();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setHeader("Content-Disposition", "attachment; filename=\"formatoTipoClienteDetalle.xlsx\"");
			response.getOutputStream().write(archivo);
			response.getOutputStream().flush();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (IOException e) {
			error("Error al generar el formato: " + e.getMessage());
			logger.error("Error al descargar formato Excel", e);
		}
	}

	public void guardarDatosMasivo(FileUploadEvent event) {
		if (!"xlsx".equals(FileUtils.getFileExtension(event.getFile().getFileName()))) {
			warn("Debe cargar un archivo Excel en formato .xlsx");
			return;
		}
		try {
			List<CxcTipoCliente> tiposValidos = consultarTipoCliente();

			CxcTipoClienteDetalleDoc.ResultadoCarga resultado = tipoClienteDetalleDoc.parsearArchivo(
					event.getFile().getInputstream(), this.noCia, getUsuario().getUsuario(), tiposValidos);

			if (resultado.tieneErrores()) {
				resultado.getErrores().forEach(e -> warn(e));
			}

			if (resultado.tieneRegistros()) {
				// OBS 5: filtrar registros que ya tienen cédula asignada
				List<CxcTipoclienteDetalle> registrosFiltrados = new ArrayList<>();
				for (CxcTipoclienteDetalle reg : resultado.getRegistros()) {
					String cedula = reg.getId().getIdentificacion();
					boolean yaExiste = listaTipoclienteDetalle.stream()
							.anyMatch(d -> d.getId().getIdentificacion().equals(cedula));
					if (yaExiste) {
						warn("Fila con identificación '" + cedula
								+ "' omitida: el cliente ya tiene un Tipo Cliente asignado.");
					} else {
						registrosFiltrados.add(reg);
					}
				}

				if (!registrosFiltrados.isEmpty()) {
					tipoClienteDetalleService.guardarOActualizar(registrosFiltrados);
					consultarDetalle();
					info(registrosFiltrados.size() + " registros procesados con éxito.");
				} else {
					warn("No se procesó ningún registro válido.");
				}
			} else {
				warn("No se procesó ningún registro válido.");
			}

		} catch (IOException | FindException | GeneralException e) {
			error("Error al leer el archivo: " + e.getMessage());
			logger.error("Error al procesar Excel", e);
		}
		accionesDialog("dlgArchivo", Boolean.FALSE);
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<CxcTipoclienteDetalle> getListaTipoclienteDetalle() {
		return listaTipoclienteDetalle;
	}

	public void setListaTipoclienteDetalle(List<CxcTipoclienteDetalle> l) {
		this.listaTipoclienteDetalle = l;
	}

	public List<CxcTipoCliente> getListaTipoCliente() {
		return listaTipoCliente;
	}

	public void setListaTipoCliente(List<CxcTipoCliente> l) {
		this.listaTipoCliente = l;
	}

	public CxcTipoclienteDetalle getCxcTipoclienteDetalle() {
		return cxcTipoclienteDetalle;
	}

	public void setCxcTipoclienteDetalle(CxcTipoclienteDetalle d) {
		this.cxcTipoclienteDetalle = d;
	}

	public CxcTipoCliente getTipoClienteSelected() {
		return tipoClienteSelected;
	}

	public void setTipoClienteSelected(CxcTipoCliente t) {
		this.tipoClienteSelected = t;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public DialogClientesController getDialogClientesController() {
		return dialogClientesController;
	}

	public void setDialogClientesController(DialogClientesController d) {
		this.dialogClientesController = d;
	}
}