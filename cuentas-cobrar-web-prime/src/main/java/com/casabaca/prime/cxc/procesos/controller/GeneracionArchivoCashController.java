/**
 * 
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.casabaca.common.ejb.model.Arckmc;
import com.casabaca.common.ejb.service.ArckmcServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxCdatosCtasBancariasDto;
import com.casabaca.cxc.ejb.servicio.CxcdatosCtasBancariasServiceLocal;
import com.casabaca.cxc.ejb.servicio.GeneracionArchivoCashServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class GeneracionArchivoCashController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(GeneracionArchivoCashController.class);

	@EJB(lookup = NombreJNDI.RECAUDACION_CASH_SERVICE_BEAN)
	private GeneracionArchivoCashServiceLocal generacionArchivoCashService;
	@EJB(lookup = NombreJNDI.CXC_DATOS_CTAS_BANCARIAS)
	private CxcdatosCtasBancariasServiceLocal ctasbancariasService;
	@EJB(lookup = NombreJNDI.ARCKMC_SERVICE)
	private ArckmcServiceLocal arckmcServiceLocal;
	private static final String NOMBRE_ARCHIVO = "archivoCash.txt";
	private String ctaBancaria;
	private List<SelectItem> ctasBancariasSelectItems;
	private List<CxCdatosCtasBancariasDto> listaCtasBancarias;
	private Arckmc cuentaBancaria;

	@PostConstruct
	public void init() {
		try {
			this.ctasBancariasSelectItems = new ArrayList<SelectItem>();
			this.listaCtasBancarias = new ArrayList<>();
			this.cuentaBancaria = new Arckmc();
			cargarCtasBancarias();
		} catch (Exception e) {
			LOG.error(e);
		}
	}
	
	public void generarArchivo() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
				.getResponse();
		StringBuffer datosRec = generacionArchivoCashService.obtenerDatosRecaudacionCash(getCompania().getNoCia());
		try {
			response.setHeader("Content-Disposition", "attachment; filename=" + NOMBRE_ARCHIVO);
			response.setContentType("text/plain");
			response.setContentLength(datosRec.toString().getBytes().length);
			OutputStream ops = response.getOutputStream();
			ops.write(datosRec.toString().getBytes());
			ctx.responseComplete();
			ops.flush();
			ops.close();
		} catch (FileNotFoundException e) {
			LOG.error(e);
		} catch (IOException e) {
			LOG.error(e);
		}

	}
	
	
	public void generarArchivoCashPagoProveedores() {

		if (this.ctaBancaria != null) {
			try {
				this.cuentaBancaria = arckmcServiceLocal.obtenerCuentaBancariaXnoCiaXnoCta(getCompania().getNoCia(),
						this.ctaBancaria);
			} catch (Exception e) {
				Log.error(e);
			}
			FacesContext ctx = FacesContext.getCurrentInstance();
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext()
					.getResponse();
			StringBuffer datosPagoProveedores = generacionArchivoCashService.obtenerDatosCashPagoProveedoresPichincha(
					getCompania().getNoCia(), this.ctaBancaria, this.cuentaBancaria.getBanco());
			try {
				response.setHeader("Content-Disposition", "attachment; filename=" + NOMBRE_ARCHIVO);
				response.setContentType("text/plain");
				response.setContentLength(datosPagoProveedores.toString().getBytes().length);
				OutputStream ops = response.getOutputStream();
				ops.write(datosPagoProveedores.toString().getBytes());
				ctx.responseComplete();
				ops.flush();
				ops.close();
			} catch (FileNotFoundException e) {
				LOG.error(e);
			} catch (IOException e) {
				LOG.error(e);
			}
			info("Archivo Generado con Exito");
		} else {
			warn("Debe Seleccionar la cuenta bancaria");
		}
	}

	public void cargarCtasBancarias() {
		try {
			this.setCtasBancariasSelectItems(new ArrayList<>());
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
			LOG.error(e);
		}
	}

	public String getCtaBancaria() {
		return ctaBancaria;
	}

	public void setCtaBancaria(String ctaBancaria) {
		this.ctaBancaria = ctaBancaria;
	}

	public List<SelectItem> getCtasBancariasSelectItems() {
		return ctasBancariasSelectItems;
	}

	public void setCtasBancariasSelectItems(List<SelectItem> ctasBancariasSelectItems) {
		this.ctasBancariasSelectItems = ctasBancariasSelectItems;
	}

	public List<CxCdatosCtasBancariasDto> getListaCtasBancarias() {
		return listaCtasBancarias;
	}

	public void setListaCtasBancarias(List<CxCdatosCtasBancariasDto> listaCtasBancarias) {
		this.listaCtasBancarias = listaCtasBancarias;
	}

	public Arckmc getCuentaBancaria() {
		return cuentaBancaria;
	}

	public void setCuentaBancaria(Arckmc cuentaBancaria) {
		this.cuentaBancaria = cuentaBancaria;
	}


}
