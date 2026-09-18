package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import com.casabaca.common.ExcelUtils;
import com.casabaca.common.FileUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcRentingCarga;
import com.casabaca.cxc.ejb.modelo.CxcRentingCargaPK;
import com.casabaca.cxc.ejb.modelo.CxcRentingDetalle;
import com.casabaca.cxc.ejb.modelo.CxcRentingDetallePK;
import com.casabaca.cxc.ejb.servicio.CxcRentingCargaServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author dv_ramirez
 *
 */
@ManagedBean
@ViewScoped
public class CxcRentingCargaController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6654427181882573825L;

	private static final Logger LOG = Logger.getLogger(CxcRentingCargaController.class);


	@EJB(lookup = NombreJNDI.CXC_RENTING_CARGA_SERVICE)
	private CxcRentingCargaServiceLocal cxcRentingCargaServiceLocal;

	private List<CxcRentingCarga> cxcRentingCargaLst;
	
	private List<CxcRentingDetalle> detalleLst;

	private Date fechaDesde;
	
	private Date fechaHasta;
	
	private String noCia ;

	@PostConstruct
	public void init() {
		cxcRentingCargaLst = new ArrayList<CxcRentingCarga>();
		detalleLst = new ArrayList<CxcRentingDetalle>();
		fechaDesde=new Date();
		fechaHasta=new Date();
		noCia = getCompania().getNoCia();
	}



	public void abrirCargaExcel() {
		accionesDialog("dlgArchivo", Boolean.TRUE);
	}
	
	
	public void abrirBusqueda() {
		accionesDialog("dlgBuscarCarga", Boolean.TRUE);
	}
	
	public void buscarCargas() {
		cxcRentingCargaLst=cxcRentingCargaServiceLocal.listarPorFechas(noCia ,fechaDesde,fechaHasta);
	}
	
	public void seleccionarCarga(CxcRentingCargaPK id) {
		detalleLst = cxcRentingCargaServiceLocal.listarDetallePorPK(id);
		
	}

	/**
	 * @param event
	 */
	public void leerArchivo(FileUploadEvent event) {
		if (event == null || event.getFile() == null || event.getFile().getSize() == 0) {
			warn("Debe cargar un archivo para procesar");
		} else if (!"xlsx".equals(FileUtils.getFileExtension(event.getFile().getFileName()))) {
			warn("Debe cargar un archivo excel en formato xlsx");
		} else {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
				
				String usuarioStr=getUsuario().getUsuario();
				
				
				List<Object[]> objetos = ExcelUtils.leerExcel(event.getFile().getInputstream(), Boolean.TRUE,
						Boolean.TRUE, 0);
				
				cxcRentingCargaLst = new ArrayList<CxcRentingCarga>();
				
				detalleLst=new ArrayList<>();
				
				long sec=0;
				for (Object[] detalle : objetos) {
					sec++;
					try {
						CxcRentingDetalle rentDetalle = new CxcRentingDetalle();
						CxcRentingDetallePK rentDetalleId= new CxcRentingDetallePK();

						rentDetalleId.setNoCia(noCia);
						rentDetalleId.setCxcRentingDetalleId(sec);
						rentDetalle.setId(rentDetalleId);
					
						rentDetalle.setCodigoEmpresa(convertirSting(detalle[0]));
						rentDetalle.setNombreEmpresa(convertirSting(detalle[1]));
						rentDetalle.setCodigoAgencia(convertirSting(detalle[2]));
						rentDetalle.setNombreAgencia(convertirSting(detalle[3]));
						rentDetalle.setLineaNegocioAsignado(convertirSting(detalle[4]));
						rentDetalle.setFechaCierre(convertirDate(detalle[5]));
						rentDetalle.setRelacionado(convertirSting(detalle[6]));
						rentDetalle.setTotalDocumentos(convertirBigDecimal(detalle[7]));
						rentDetalle.setSaldoTotalCxc(convertirBigDecimal(detalle[8]));
						rentDetalle.setVencido1a30(convertirBigDecimal(detalle[9]));
						rentDetalle.setVencido31a60(convertirBigDecimal(detalle[10]));
						rentDetalle.setVencido61a90(convertirBigDecimal(detalle[11]));
						rentDetalle.setVencido91a120(convertirBigDecimal(detalle[12]));
						rentDetalle.setVencido121a150(convertirBigDecimal(detalle[13]));
						rentDetalle.setVencido151a180(convertirBigDecimal(detalle[14]));
						rentDetalle.setVencidomas180(convertirBigDecimal(detalle[15]));
                                       
						rentDetalle.setPorvencer1a30(convertirBigDecimal(detalle[16]));
						rentDetalle.setPorvencer31a60(convertirBigDecimal(detalle[17]));
						rentDetalle.setPorvencer61a90(convertirBigDecimal(detalle[18]));
						rentDetalle.setPorvencer91a120(convertirBigDecimal(detalle[19]));
						rentDetalle.setPorvencer121a150(convertirBigDecimal(detalle[20]));
						rentDetalle.setPorvencer151a180(convertirBigDecimal(detalle[21]));
						rentDetalle.setPorvencermas180(convertirBigDecimal(detalle[22]));
	
						rentDetalle.setAnioCierre(convertirBigDecimal(detalle[23]));
						rentDetalle.setMesCierre(convertirBigDecimal(detalle[24]));
						
						detalleLst.add(rentDetalle);
						
					}catch (Exception e) {
						addErrorMessage("Error al cargar fila "+sec, e.getMessage());
						LOG.error("Error al cargar fila "+sec, e);
					}

				}
				
				try {
					CxcRentingCarga carga = cxcRentingCargaServiceLocal.crearCarga(noCia,usuarioStr,detalleLst);
					cxcRentingCargaLst.add(carga);
				} catch (InsertException e) {
					addErrorMessage("Error al crear cargar", e.getMessage());
					LOG.error("Error al crear cargar", e);
					return;
				}

				info("Proceso Realizado Con Exito");
				accionesDialog("dlgArchivo", Boolean.FALSE);
				updateComponentFromId("idFormDatos");
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
	 * [Author diego.ramirez, 15 nov. 2023]
	 * </p>
	 *
	 * @param objeto
	 * @return
	 */
	private BigDecimal convertirBigDecimal(Object obj) {
		if(obj!=null) {
			try {
				return new BigDecimal(obj.toString());
			}catch (Exception e) {
				return null;
			}
		}else {
			return null;
		}
	}

	
	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 15 nov. 2023]
	 * </p>
	 *
	 * @param objeto
	 * @return
	 */
	private Date convertirDate(Object obj) {
		if(obj!=null) {
			try {
				return (Date) obj;
			}catch (Exception e) {
				return null;
			}
		}else {
			return null;
		}
	}
	
	/**
	 * <b> Incluir aqui la descripcion del metodo. </b>
	 * <p>
	 * [Author diego.ramirez, 15 nov. 2023]
	 * </p>
	 *
	 * @param objeto
	 * @return
	 */
	private String convertirSting(Object obj) {
		if(obj!=null) {
			return obj.toString();
		}else {
			return null;
		}
	}


	public List<CxcRentingCarga> getProteccionDatosLst() {
		return cxcRentingCargaLst;
	}

	public void setProteccionDatosLst(List<CxcRentingCarga> proteccionDatosLst) {
		this.cxcRentingCargaLst = proteccionDatosLst;
	}



	public List<CxcRentingCarga> getCxcRentingCargaLst() {
		return cxcRentingCargaLst;
	}



	public void setCxcRentingCargaLst(List<CxcRentingCarga> cxcRentingCargaLst) {
		this.cxcRentingCargaLst = cxcRentingCargaLst;
	}



	public List<CxcRentingDetalle> getDetalleLst() {
		return detalleLst;
	}



	public void setDetalleLst(List<CxcRentingDetalle> detalleLst) {
		this.detalleLst = detalleLst;
	}



	public Date getFechaDesde() {
		return fechaDesde;
	}



	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}



	public Date getFechaHasta() {
		return fechaHasta;
	}



	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}
	

	

}
