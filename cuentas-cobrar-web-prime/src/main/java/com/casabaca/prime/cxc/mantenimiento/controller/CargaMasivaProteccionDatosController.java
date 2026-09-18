package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import com.casabaca.common.ExcelUtils;
import com.casabaca.common.FileUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcProtecciondatosCliente;
import com.casabaca.cxc.ejb.modelo.CxcProtecciondatosClientePK;
import com.casabaca.cxc.ejb.servicio.CxcProtecciondatosClienteServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author dv_ramirez
 *
 */
@ManagedBean
@ViewScoped
public class CargaMasivaProteccionDatosController extends CommonController implements Serializable {

	private static final String CERO_STR = "0";

	private static final int OCHO = 8;

	private static final int NUEVE = 9;

	private static final String XLSX = "xlsx";

	private static final long serialVersionUID = -6654427181882573825L;

	private static final Logger LOG = Logger.getLogger(CargaMasivaProteccionDatosController.class);


	@EJB(lookup = NombreJNDI.CXC_PROTECCIONDATOS_CLIENTE_SERVICE)
	private CxcProtecciondatosClienteServiceLocal cxcProteccionDatosServiceLocal;

	private List<CxcProtecciondatosCliente> proteccionDatosLst;
	private List<String> advertenciasLst;

	@PostConstruct
	public void init() {
		proteccionDatosLst = new ArrayList<CxcProtecciondatosCliente>();
	}



	public void abrirCargaExcel() {
		accionesDialog("dlgArchivo", Boolean.TRUE);
	}

	/**
	 * @param event
	 */
	public void leerArchivo(FileUploadEvent event) {
		if (event == null || event.getFile() == null || event.getFile().getSize() == 0) {
			warn("Debe cargar un archivo para procesar");
		} else if (!XLSX.equals(FileUtils.getFileExtension(event.getFile().getFileName()))) {
			warn("Debe cargar un archivo excel en formato xlsx");
		} else {
			try {
				
				List<Object[]> objetos = ExcelUtils.leerExcel(event.getFile().getInputstream(), Boolean.TRUE,
						Boolean.TRUE, 0);
				
				proteccionDatosLst = new ArrayList<CxcProtecciondatosCliente>();
				advertenciasLst=new ArrayList<>();
				
				int linea=0;
				for (Object[] detalle : objetos) {
					linea++;
					leerDetalle(linea, detalle);
				}
				if(advertenciasLst.isEmpty() && !proteccionDatosLst.isEmpty()) {
					try {
						for (CxcProtecciondatosCliente proteccion : proteccionDatosLst) {
							cxcProteccionDatosServiceLocal.insertar(proteccion);
						}
						info("Proceso Realizado Con Exito");
						accionesDialog("dlgArchivo", Boolean.FALSE);
						updateComponentFromId("idFormDatos");
					} catch (InsertException e) {
						addErrorMessage("Error al guardar protección datos personales", e.getMessage());
						LOG.error("Error al guardar protección datos personales", e);
					}
				}else {
					for (String advertencia : advertenciasLst) {
						addWarnMessage("Advertencia", advertencia);
					}
					addErrorMessage("Advertencia", "No se ha procesado el archivo");
				}
				
			} catch (IOException e) {
				LOG.error("Error al leer el archivo", e);
				error("Error al leer el archivo, " + e.getMessage());
				accionesDialog("dlgArchivo", Boolean.FALSE);
			} catch (NumberFormatException e) {
				LOG.error("Error en formato de cantidad", e);
				error("Error en formato de cantidad, " + e.getMessage());
				accionesDialog("dlgArchivo", Boolean.FALSE);
			}
		}

	}



	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 23 jul. 2024]
	 * </p>
	 *
	 * @param linea
	 * @param detalle
	 * @param advertenciasLst 
	 */
	private void leerDetalle(int linea, Object[] detalle) {
		try {
			String noCia = getCompania().getNoCia();
			String marca = getCompania().getNombre();
			String usuarioStr=getUsuario().getUsuario();
			
			CxcProtecciondatosCliente proteccion = new CxcProtecciondatosCliente();
			CxcProtecciondatosClientePK id= new CxcProtecciondatosClientePK();
			
			String telefono;
			telefono = leerTelefono(linea,detalle[3]);
			
			String cedula;
			cedula = leerCedula(linea,detalle[0]);
			
			id.setNoCia(noCia);
			id.setIdToken(("CM"+System.currentTimeMillis())+linea);
			proteccion.setId(id);
			proteccion.setMarca(marca);
			proteccion.setFechaCreacion(new Date());
			proteccion.setEstado("C");
			proteccion.setUsuarioAgente(usuarioStr);
			
			proteccion.setCedula(cedula);

			proteccion.setNombre(entradaAString(detalle[1],linea,"Nombre"));
			proteccion.setApellidos(entradaAString(detalle[2],linea,"Apellidos"));
			proteccion.setTelefono(telefono);
			proteccion.setEmail(entradaAString(detalle[4],linea,"Email"));
			proteccion.setDireccion(entradaAString(detalle[5],linea,"Direccion"));
			proteccion.setAceptaLopdp(entradaAString(detalle[6],linea,"Acepta LOPDP"));
			proteccion.setActualizacionDatos(entradaAString(detalle[7],linea,"Actualización Datos"));
			proteccion.setFecha(entradaADate(detalle[8],linea,"Fecha Aceptacion"));

			proteccionDatosLst.add(proteccion);
		}catch (Exception e) {
			advertenciasLst.add("en la linea"+linea+": Error al cargar datos "+e.getMessage());
			LOG.error("Error al cargar datos ", e);
		}
	}



	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 23 jul. 2024]
	 * </p>
	 *
	 * @param entrada
	 * @return
	 */
	private String entradaAString(Object entrada,int linea,String nombreCampo) {
		if(entrada==null ) {
			advertenciasLst.add("en la linea"+linea+": "+nombreCampo+" no puede ser nulo");
			return "";
		}
		String respuestaStr = entrada.toString();
		if(respuestaStr.isEmpty()){
			advertenciasLst.add("en la linea"+linea+": "+nombreCampo+" no puede ser vacio");
		}
		return respuestaStr;
	}

	
	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 23 jul. 2024]
	 * </p>
	 *
	 * @param entrada
	 * @return
	 */
	private Date entradaADate(Object entrada,int linea,String nombreCampo) {
		if(entrada==null) {
			advertenciasLst.add("en la linea"+linea+": "+nombreCampo+" no puede ser nulo");
			return null;
		}
		try {
			Date fechaActual=new Date();
			Date fechaAceptacion = (Date) entrada;
			if(fechaActual.before(fechaAceptacion)) {
				advertenciasLst.add("en la linea"+linea+": "+nombreCampo+"La fecha de aceptacion no puede ser posterior a la fecha actual");
			}
			return fechaAceptacion;
		}catch (Exception e) {
			advertenciasLst.add("en la linea"+linea+": "+nombreCampo+" no es una fecha valida");
			return null;
		}
	}


	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 23 jul. 2024]
	 * </p>
	 *
	 * @param detalle
	 * @return
	 */
	private String leerCedula(int linea,Object entrada) {
		if(entrada==null) {
			advertenciasLst.add("en la linea"+linea+": cedula no puede ser nulo");
			return "";
		}
		String cedula;
		try {
			cedula=String.format("%010d",(((Double)entrada).longValue()));
		}catch (Exception e) {
			try {
				cedula=entrada.toString();
			}catch (Exception e1) {cedula="";	}
		}
		return cedula;
	}



	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 23 jul. 2024]
	 * </p>
	 *
	 * @param entrada
	 * @return
	 */
	private String leerTelefono(int linea,Object entrada) {
		if(entrada==null) {
			advertenciasLst.add("en la linea"+linea+": telefono no puede ser nulo");
		}
		StringBuffer telefono;
		try {
			telefono = new StringBuffer(String.valueOf(((Double)entrada).longValue()));
		}catch (Exception e) {
			try {	
				telefono= new StringBuffer(entrada.toString());
			}catch (Exception e1) {
				telefono= new StringBuffer();
			}
		}
		if(telefono.length()==NUEVE || telefono.length()==OCHO) {
			telefono.append(CERO_STR)
			.append(telefono);
		}

		return telefono.toString();
	}


	

	public void descargarPlantillaFormato() {

		try {
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			
	        // Ruta física del archivo en el servidor
	        String filePath = getServletContext().getRealPath("/resources/plantillas/ejemploCargaProteccionDatos.xlsx");
	        File file = new File(filePath);
	
	        if (!file.exists()) {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Archivo no encontrado");
	            return;
	        }
	
	        // Configurar tipo de contenido (MIME)
	        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
	
	        // Configurar encabezado para descarga
	        response.setHeader("Content-Disposition", "attachment; filename=\"ejemploCargaProteccionDatos.xlsx\"");

			try (ServletOutputStream out = response.getOutputStream();
			     FileInputStream in = new FileInputStream(file)) {
			
			    byte[] buffer = new byte[4096];
			    int bytesRead;
			    while ((bytesRead = in.read(buffer)) != -1) {
			        out.write(buffer, 0, bytesRead);
			    }
			    FacesContext.getCurrentInstance().responseComplete();
			    out.flush();
			}

		}catch (Exception e) {
			error("Error al descargar ejemplo de carga masiva");
			LOG.error("Error al descargar ejemplo de carga masiva",e);
		}
	}

	

	public List<CxcProtecciondatosCliente> getProteccionDatosLst() {
		return proteccionDatosLst;
	}

	public void setProteccionDatosLst(List<CxcProtecciondatosCliente> proteccionDatosLst) {
		this.proteccionDatosLst = proteccionDatosLst;
	}

}
