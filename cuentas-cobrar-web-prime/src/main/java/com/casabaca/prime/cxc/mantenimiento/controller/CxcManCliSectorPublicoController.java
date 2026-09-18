/**
 * 
 */
package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.IOException;
import java.io.Serializable;
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

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.ExcelUtils;
import com.casabaca.common.FileUtils;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcClientePublico;
import com.casabaca.cxc.ejb.modelo.CxcClientePublicoPK;
import com.casabaca.cxc.ejb.servicio.CxcClientePublicoServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author laura.llangari
 *
 */
@ManagedBean
@ViewScoped
public class CxcManCliSectorPublicoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final Logger LOG = Logger.getLogger(CxcManCliSectorPublicoController.class);

	@EJB(lookup = NombreJNDI.CLIENTEPUBLICO_SERVICE)
	private CxcClientePublicoServiceLocal clientePublicoService;

	private String noCia;
	private int contador;
	private Boolean permiteGrabar;
	private CxcClientePublico clientePublico;
	private List<CxcClientePublico> listadoCargarExcel;
	private String reporte;
	private String formato;

	private static final String REPORTE = "/cxc/CxcManClientePublico";
	// private CxcClientePublicoPK clientePublicoPk;

	/**
	 * 
	 */
	public CxcManCliSectorPublicoController() {
		this.noCia = getCompania().getNoCia();
	}

	@PostConstruct
	public void init() {
		listadoCargarExcel = new ArrayList<CxcClientePublico>();
		contador = 0;
		clientePublico = new CxcClientePublico(new CxcClientePublico().getId());
		permiteGrabar = Boolean.TRUE;
	}

	public void abrirCargaExcel() {

		accionesDialog("dlgArchivo", Boolean.TRUE);

	}

	public void leerArchivo(FileUploadEvent event) {
		if (event == null || event.getFile() == null || event.getFile().getSize() == 0) {
			warn("Debe cargar un archivo para procesar");
		} else if (!"xlsx".equals(FileUtils.getFileExtension(event.getFile().getFileName()))) {
			warn("Debe cargar un archivo excel en formato xlsx");
		} else {
			try {
				int datos= clientePublicoService.consultaDatos(noCia);
				int error = clientePublicoService.deleteDatosCliente(noCia);
				
				if (datos==0 || error > 0) {
					List<Object[]> objetos = ExcelUtils.leerExcel(event.getFile().getInputstream(), Boolean.TRUE,
							Boolean.TRUE, 0);
					for (Object[] detalle : objetos) {
						contador++;
						validarArchivo(detalle);
					}							
				} 					
					info("Proceso Realizado Con Exito");
					accionesDialog("dlgArchivo", Boolean.FALSE);
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

	public void validarArchivo(Object[] archivo) {
		permiteGrabar = Boolean.FALSE;
		clientePublico.setId(new CxcClientePublicoPK());
		clientePublico.getId().setNoCia(this.noCia);
		clientePublico.getId().setCedula(archivo[0].toString());
		clientePublico.setNombreCli(archivo[1].toString());
		clientePublico.setUsuarioCrea(getUsuario().getUsuario());
		clientePublico.setFechaCrea(new Date());
		listadoCargarExcel.add(clientePublico);
		try {
			clientePublicoService.insertar(clientePublico);
		} catch (InsertException e) {
			addErrorMessage("Error", e.getMessage());
			return;
		}

	}

	public void excel() {
		if (!listadoCargarExcel.isEmpty()) {
			this.formato = CommonConstants.OUTPUT_XLSX_NO_PAG;
			reporte = REPORTE + CommonUtils.getCiaReportUnit(getCompania().getNoCia());
			reporte();
		} else {

			addErrorMessage("Error", "No a PROCESADO los Datos....");
		}
	}

	public void reporte() {
		Map<String, Object> parameters = new HashMap<>();
		setParameters(parameters);
		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(super.callJasperReport(this.reporte, parameters, this.formato));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setParameters(Map<String, Object> parameters) {

	}



	// Getter y Setter

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public CxcClientePublico getClientePublico() {
		return clientePublico;
	}

	public void setClientePublico(CxcClientePublico clientePublico) {
		this.clientePublico = clientePublico;
	}

	public List<CxcClientePublico> getListadoCargarExcel() {
		return listadoCargarExcel;
	}

	public void setListadoCargarExcel(List<CxcClientePublico> listadoCargarExcel) {
		this.listadoCargarExcel = listadoCargarExcel;
	}

	public String getReporte() {
		return reporte;
	}

	public void setReporte(String reporte) {
		this.reporte = reporte;
	}

	public String getFormato() {
		return formato;
	}

	public void setFormato(String formato) {
		this.formato = formato;
	}

}
