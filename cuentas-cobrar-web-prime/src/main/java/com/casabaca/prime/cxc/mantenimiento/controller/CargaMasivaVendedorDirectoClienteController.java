package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import com.casabaca.common.ExcelUtils;
import com.casabaca.common.FileUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ClientePK;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.FindException;
import com.casabaca.exception.UpdateException;
import com.casabaca.inventario.ejb.model.InvVendedor;
import com.casabaca.inventario.ejb.service.InvVendedorServiceLocal;
import com.casabaca.inventario.ejb.service.ZonasVendeServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author dv_ramirez
 *
 */
@ManagedBean
@ViewScoped
public class CargaMasivaVendedorDirectoClienteController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final String NO_ENCONTRADO = "no encontrado";

	private static final long serialVersionUID = 9129709938033487818L;

	private static final String XLSX = "xlsx";

	private static final Logger LOG = Logger.getLogger(CargaMasivaVendedorDirectoClienteController.class);



	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;
	
	@EJB(lookup = NombreJNDI.INV_VENDEDOR_SERVICE)
	private InvVendedorServiceLocal invVendedorServiceLocal;
	
	@EJB(lookup = NombreJNDI.ZONAS_VENDE_SERVICE)
	private ZonasVendeServiceLocal zonasVendeServiceLocal;

	private Cliente clienteSeleccionado;
	private List<InvVendedor> vendedoresLst;
	
	private List<Cliente> clienteLst;
	private List<String> advertenciasLst;
	private List<String> zonasLst;
	private Map<String,String> vendedoresEncontradosMap;
	private Map<String,String> vendedoresCodigoEnteroMap;
	
	private Long noClienteFiltro;
	private String cedulaFiltro;

	@PostConstruct
	public void init() {
		clienteLst = new ArrayList<Cliente>();
		vendedoresEncontradosMap=new HashMap<>();
		vendedoresCodigoEnteroMap=new HashMap<>();
		zonasLst=zonasVendeServiceLocal.listarZonas(getCompania().getNoCia());
		vendedoresLst=invVendedorServiceLocal.buscarVendedoresNocia(getCompania().getNoCia());
	}

	public void abrirCliente() {
		setNoClienteFiltro(null);
		setCedulaFiltro(null);
		this.clienteSeleccionado=new Cliente();
		this.clienteSeleccionado.setClientePK(new ClientePK());
		accionesDialog("dlgCliente", Boolean.TRUE);
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
				
				clienteLst = new ArrayList<Cliente>();
				advertenciasLst=new ArrayList<>();
				
				int linea=1;
				for (Object[] detalle : objetos) {
					linea++;
					leerDetalle(linea, detalle);
				}
				if(advertenciasLst.isEmpty() && !clienteLst.isEmpty()) {
					try {
						for (Cliente cliente : clienteLst) {
							clienteServiceLocal.update(cliente);
						}
						info("Proceso Realizado Con Exito");
						accionesDialog("dlgArchivo", Boolean.FALSE);
						updateComponentFromId("idFormDatos");
					} catch (UpdateException e) {
						addErrorMessage("Error al guardar vendedores directos", e.getMessage());
						LOG.error("Error al guardar vendedores directos", e);
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

	private void leerDetalle(int linea, Object[] detalle) {
		try {
			String noCia = getCompania().getNoCia();
			Long noCliente=entradaALong(detalle[0],linea,"Numero de Cliente");
			String codigoVendedor=entradaAString(detalle[1],linea,"Codigo del Vendedor");
			
			String vendedorEncontrado = vendedoresEncontradosMap.get(codigoVendedor);
			if(vendedorEncontrado==null) {
				vendedorEncontrado = vendedoresCodigoEnteroMap.get(codigoVendedor);
				if(vendedorEncontrado!=null) {
					codigoVendedor = String.valueOf(Double.valueOf(codigoVendedor).intValue());
				}else {
					InvVendedor vendedor = invVendedorServiceLocal.buscarVendedorPorNociaCodigo(getCompania().getNoCia(),codigoVendedor);
					if(vendedor.getNombre()!=null) {
						vendedorEncontrado=vendedor.getNombre();
						vendedoresEncontradosMap.put(codigoVendedor, vendedorEncontrado);
					}else {
						try {
							String codigoVendedorInt = String.valueOf(Double.valueOf(codigoVendedor).intValue());
							vendedor = invVendedorServiceLocal.buscarVendedorPorNociaCodigo(getCompania().getNoCia(),codigoVendedorInt);
							vendedorEncontrado=vendedor.getNombre()==null?NO_ENCONTRADO:vendedor.getNombre();
							vendedoresCodigoEnteroMap.put(codigoVendedor, vendedorEncontrado);
							codigoVendedor=codigoVendedorInt;
						}catch (Exception e) {
							vendedorEncontrado=NO_ENCONTRADO;
						}
					}
				}
			}
			if(NO_ENCONTRADO.equals(vendedorEncontrado)) {
				advertenciasLst.add("en la linea"+linea+": No se ha encontrado un vendedor con el codigo "+codigoVendedor);
				return;
			}
			
			try {
				Cliente clienteEncontrado =clienteServiceLocal.findByPk(new ClientePK(noCia,"01",noCliente));
				clienteEncontrado.setVendedorDirecto(codigoVendedor);
				clienteEncontrado.setNombreRelacion(vendedorEncontrado);
				clienteLst.add(clienteEncontrado);
			}catch (FindException e) {
				advertenciasLst.add("en la linea"+linea+": No se ha encontrado un cliente con el numero "+noCliente);
				return;
			}
			
		}catch (Exception e) {
			advertenciasLst.add("en la linea"+linea+": Error al cargar datos "+e.getMessage());
			LOG.error("Error al cargar datos ", e);
		}
	}


	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 22 nov 2024]
	 * </p>
	 *
	 * @param object
	 * @param linea
	 * @param string
	 * @return
	 */
	private Long entradaALong(Object entrada,int linea,String nombreCampo) {
		if(entrada==null ) {
			advertenciasLst.add("en la linea"+linea+": "+nombreCampo+" no puede ser nulo");
			return null;
		}
		try {
			long respuesta = (((Double)entrada).longValue());
			return respuesta;
		}catch(Exception e) {
			advertenciasLst.add("en la linea"+linea+": "+nombreCampo+" no puede ser vacio");
			return null;
		}
		
	}

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

	
	public void buscarCliente() {
		try {
			if( getCedulaFiltro()!=null && !getCedulaFiltro().isEmpty()) {
				clienteSeleccionado=clienteServiceLocal.buscarNombrePorCedulaNoCia(getCedulaFiltro(), getCompania().getNoCia());
			}
			if(getNoClienteFiltro()!=null) {
				clienteSeleccionado=clienteServiceLocal.findByPk(new ClientePK(getCompania().getNoCia(),"01", getNoClienteFiltro()));
			}
		}catch (FindException e) {
			info("No se ha encontrado el cliente ");
		}catch (Exception e) {
			LOG.error("Error al buscar cliente ", e);
			error("Error al buscar cliente, " + e.getMessage());
		}
	}
	
	public void guardarCliente() {
		if(clienteSeleccionado==null) {
			warn("No hay un cliente seleccionado");
			return;
		}
		try {
			clienteServiceLocal.update(clienteSeleccionado);
			info("Cliente actualizado");
			accionesDialog("dlgCliente", Boolean.FALSE);
		} catch ( UpdateException e) {
			LOG.error("Error al actualizar cliente ", e);
			error("Error al actualizar cliente, " + e.getMessage());
		}
	}

	public List<Cliente> getClienteLst() {
		return clienteLst;
	}

	public Cliente getClienteSeleccionado() {
		return clienteSeleccionado;
	}
	
	public Long getNoClienteFiltro() {
		return noClienteFiltro;
	}

	public void setNoClienteFiltro(Long noClienteFiltro) {
		this.noClienteFiltro = noClienteFiltro;
	}

	public String getCedulaFiltro() {
		return cedulaFiltro;
	}

	public void setCedulaFiltro(String cedulaFiltro) {
		this.cedulaFiltro = cedulaFiltro;
	}

	public List<InvVendedor> getVendedoresLst() {
		return vendedoresLst;
	}

	public List<String> getZonasLst() {
		return zonasLst;
	}

}
