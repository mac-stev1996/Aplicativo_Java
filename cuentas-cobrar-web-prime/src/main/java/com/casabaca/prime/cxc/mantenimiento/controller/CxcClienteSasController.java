package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.casabaca.common.FileUpload;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcClientesSas;
import com.casabaca.cxc.ejb.servicio.CxcClientesSasService;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 * Controlador para la tabla CxcClientesSas Crud que valida si exite presenta
 * mensaje de error y almacena la Ruta de la justificacion
 * 
 */
@ViewScoped
@ManagedBean
public class CxcClienteSasController extends CommonController implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	static final Logger LOG = Logger.getLogger(CxcClienteSasController.class);

	@EJB(lookup = NombreJNDI.CXC_CLIENTES_SAS_SERVICE_BEAN)
	private CxcClientesSasService cxcClientesSasService;

	private List<CxcClientesSas> listadoClientes;
	private List<CxcClientesSas> listadoClientesSelected;
	private CxcClientesSas cxcClienteSasNuevo;
	private CxcClientesSas cxcClienteSasEditado;

	@PostConstruct
	public void init() {
		listadoClientes = new ArrayList<CxcClientesSas>();
		listadoClientesSelected = new ArrayList<CxcClientesSas>();
		cxcClienteSasNuevo = new CxcClientesSas();
		cxcClienteSasEditado = new CxcClientesSas();

		listadoClientes = cxcClientesSasService.obtenerTodos();

	}

	public void guardar() {

		try {

			if (cxcClienteSasNuevo.getRutaImagen() != null && !cxcClienteSasNuevo.getNombre().isEmpty()
					&& cxcClienteSasNuevo.getEstado() != null && !cxcClienteSasNuevo.getRuc().isEmpty()) {

				if (cxcClientesSasService.validarExite(cxcClienteSasNuevo.getRuc()).intValue() == 0) {
					cxcClienteSasNuevo.setUsuario(getUsuario().getUsuario());
					cxcClienteSasNuevo.setFechaControl(new Date());
					cxcClientesSasService.insertar(cxcClienteSasNuevo);
					addInfoMessage("Info", "Registro Almacenado con exito!!!");
					accionesDialog("dlgNuevo", Boolean.FALSE);
					init();
				} else
					error("Numero de Ruc ingresado ya existe, Favor validar!!!");
			} else {

				error("Todos los campos son Obligatorios!!!, Favor ingresarlos!!!");
			}

		} catch (InsertException e) {
			error("Ocurrio un problema al Grabar Registro " + e.getCause());
			LOG.error(e.getCause());
		}

	}

	public void abrirDialogoEditar(CxcClientesSas cxcClientesSas) {

		cxcClienteSasEditado = cxcClientesSas;

		accionesDialog("dlgEditar", Boolean.TRUE);

	}

	public void nuevo() {

		cxcClienteSasNuevo = new CxcClientesSas();
		accionesDialog("dlgNuevo", Boolean.TRUE);

	}

	public void actualizar() {
		try {

			if (cxcClienteSasEditado.getRutaImagen() != null && !cxcClienteSasEditado.getNombre().isEmpty()) {
				cxcClienteSasEditado.setUsuario(getUsuario().getUsuario());
				cxcClienteSasEditado.setFechaControl(new Date());
				cxcClientesSasService.actualizar(cxcClienteSasEditado);
				info("Registro Actualizado con exito!!!");
				accionesDialog("dlgEditar", Boolean.FALSE);
				init();
			} else {

				error("Para Realizar la Actualizacion debe contener un archivo de respaldo y Debe tener una Razon Social");

			}

		} catch (UpdateException e) {
			addErrorMessage("Error", "Ocurrio un problema al Actualizar Registro");
			LOG.error(e.getCause());
		}

	}

	/**
	 * Permite cargar un Archivo de Embarque
	 * 
	 * @param event
	 */
	public void cargarArchivo(FileUploadEvent event) {
		File dirPath = null;
		try {

			UploadedFile uf = event.getFile();
			String path = System.getProperty("file.separator") + "opt" + System.getProperty("file.separator")
					+ "archivosClientesSas" + System.getProperty("file.separator");
			String fileName = uf.getFileName().length() > 100 ? uf.getFileName().substring(0, 100) : uf.getFileName();
			File[] drives = File.listRoots();
			for (File fileDrives : drives) {
				// Si es windows
				if (fileDrives.getPath().length() > 1 && fileDrives.getPath().substring(0, 1).equals("C")) {
					path = fileDrives.getPath() + path;
				}
			}
			// Create the directory if it doesn't exist
			dirPath = new File(path);
			if (!dirPath.exists()) {
				dirPath.mkdirs();
			}
			path = path + System.getProperty("file.separator") + FileUpload.cambioNombre(fileName).toLowerCase().trim();
			InputStream in = uf.getInputstream();
			OutputStream out = new FileOutputStream(path);

			int read = 0;
			byte[] bytes = new byte[10240];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			in.close();
			out.flush();
			out.close();
			if (cxcClienteSasNuevo.getRuc() != null) {
				cxcClienteSasNuevo.setRutaImagen(path);
			} else {

				cxcClienteSasEditado.setRutaImagen(path);
			}

			accionesDialog("dlgArchivo", Boolean.FALSE);
			info("Archivo subido de forma exitosa");
		} catch (IOException e) {
			LOG.error("Error al Cargar archivo", e);
			error("Error al Cargar archivo");
		}
	}

	public void eliminarDocumento() {
		try {

			String pathArchivo = cxcClienteSasNuevo.getRutaImagen();
			cxcClienteSasNuevo.setRutaImagen(null);
			File archivo = new File(pathArchivo);
			archivo.delete();
			info("Documento eliminado con exito");
		} catch (Exception e) {
			LOG.error("Error al actualizar Ciente", e);
			error("Se presento un error al eliminar el archivo, recargar la página nuevamente");
		}

	}

	public void eliminarDocumentoEditado() {
		try {

			String pathArchivo = cxcClienteSasEditado.getRutaImagen();
			cxcClienteSasEditado.setRutaImagen(null);
			File archivo = new File(pathArchivo);
			archivo.delete();
			info("Documento eliminado con exito");
		} catch (Exception e) {
			LOG.error("Error al actualizar Ciente", e);
			error("Se presento un error al eliminar el archivo, recargar la página nuevamente");
		}

	}

	public List<CxcClientesSas> getListadoClientes() {
		return listadoClientes;
	}

	public void setListadoClientes(List<CxcClientesSas> listadoClientes) {
		this.listadoClientes = listadoClientes;
	}

	public CxcClientesSas getCxcClienteSasNuevo() {
		return cxcClienteSasNuevo;
	}

	public void setCxcClienteSasNuevo(CxcClientesSas cxcClienteSasNuevo) {
		this.cxcClienteSasNuevo = cxcClienteSasNuevo;
	}

	public CxcClientesSas getCxcClienteSasEditado() {
		return cxcClienteSasEditado;
	}

	public void setCxcClienteSasEditado(CxcClientesSas cxcClienteSasEditado) {
		this.cxcClienteSasEditado = cxcClienteSasEditado;
	}

	public List<CxcClientesSas> getListadoClientesSelected() {
		return listadoClientesSelected;
	}

	public void setListadoClientesSelected(List<CxcClientesSas> listadoClientesSelected) {
		this.listadoClientesSelected = listadoClientesSelected;
	}

}
