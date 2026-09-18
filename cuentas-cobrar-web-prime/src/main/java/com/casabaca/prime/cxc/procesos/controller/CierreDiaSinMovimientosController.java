package com.casabaca.prime.cxc.procesos.controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.jboss.logging.Logger;

import com.casabaca.caja.ejb.service.CierreDiarioServiceLocal;
import com.casabaca.common.ejb.dto.InvControlDto;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcCierreDiaSinMovimientosDTO;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.exception.GeneralException;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * Control de cierres diarios sin movimientos
 * 
 * @author mm_rivera
 *
 */
@ViewScoped
@ManagedBean
public class CierreDiaSinMovimientosController extends CommonController implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(CierreDiaSinMovimientosController.class);

	@EJB(lookup = NombreJNDI.CXC_NATIVE_SERVICE_BEAN)
	private CxcNativeServiceLocal nativeService;

	@EJB(lookup = NombreJNDI.CIERRE_DIARIO_CAJAS_SERVICE)
	private CierreDiarioServiceLocal cierreDiarioService;

	private List<CxcCierreDiaSinMovimientosDTO> cierreDiaSinMovimientosDTOList;
	private List<CxcCierreDiaSinMovimientosDTO> cierreDiaSinMovimientosSelectDTOList;

	@PostConstruct
	public void init() {
		logger.info("init...");
		cierreDiaSinMovimientosSelectDTOList = new ArrayList<>();

		consultaCierreDiaSinMovimientos();
	}

	/**
	 * Carga la lista de OrdenRepPenFacturaDTO
	 */
	public void consultaCierreDiaSinMovimientos() {
		logger.info("inicio consultaCierreDiaSinMovimientos...");
		LocalDate fechaActual = LocalDate.now();
		LocalDate fechaDiaAnterior = fechaActual.minusDays(1);
		LocalDate primerDiaMes = fechaActual.withDayOfMonth(1);

		cierreDiaSinMovimientosDTOList = nativeService.consultarCierreDiaSinMovimientos(getCompania().getNoCia(),
				Date.from(fechaActual.atStartOfDay(ZoneId.systemDefault()).toInstant()), 
				Date.from(fechaDiaAnterior.atStartOfDay(ZoneId.systemDefault()).toInstant()), 
				Date.from(primerDiaMes.atStartOfDay(ZoneId.systemDefault()).toInstant()));

		logger.info("fin consultaCierreDiaSinMovimientos...");
	}

	public void procesarCierreDia() {
		logger.info("procesarCierreDia...");
		try {

			if (cierreDiaSinMovimientosSelectDTOList == null || cierreDiaSinMovimientosSelectDTOList.isEmpty()) {
				addErrorMessage("", "No hay registros seleccionados para procesar el cierre del día.");
				return;
			}

			for (CxcCierreDiaSinMovimientosDTO cxcCierreDiaSinMovimientosDTO : cierreDiaSinMovimientosSelectDTOList) {

				InvControlDto invControlDto = new InvControlDto();
				invControlDto.setNoCia(cxcCierreDiaSinMovimientosDTO.getNoCia());
				invControlDto.setCentroConectado(cxcCierreDiaSinMovimientosDTO.getCentro());
				invControlDto.setFechaProceso(cxcCierreDiaSinMovimientosDTO.getDiaProcesoCaja());

//				cierreDiarioService.procesarCierre(invControlDto);
				cierreDiarioService.procesarCierreBDD(invControlDto);
			}

			cierreDiaSinMovimientosSelectDTOList = new ArrayList<>();
			addInfoMessage("", "Procesados con exito.");
			consultaCierreDiaSinMovimientos();

		} catch (GeneralException e) {
			logger.error("Error al procesar el cierre del día sin movimientos.", e);
			addErrorMessage("", "Error al procesar el cierre del día sin movimientos: " + e.getDetail());
		} catch (Exception e) {
			logger.error("Error al procesar el cierre del día sin movimientos.", e);
			addErrorMessage("", "Error al procesar el cierre del día sin movimientos: " + e.getMessage());
		}

	}

	public List<CxcCierreDiaSinMovimientosDTO> getCierreDiaSinMovimientosDTOList() {
		return cierreDiaSinMovimientosDTOList;
	}

	public void setCierreDiaSinMovimientosDTOList(List<CxcCierreDiaSinMovimientosDTO> cierreDiaSinMovimientosDTOList) {
		this.cierreDiaSinMovimientosDTOList = cierreDiaSinMovimientosDTOList;
	}

	public List<CxcCierreDiaSinMovimientosDTO> getCierreDiaSinMovimientosSelectDTOList() {
		return cierreDiaSinMovimientosSelectDTOList;
	}

	public void setCierreDiaSinMovimientosSelectDTOList(
			List<CxcCierreDiaSinMovimientosDTO> cierreDiaSinMovimientosSelectDTOList) {
		this.cierreDiaSinMovimientosSelectDTOList = cierreDiaSinMovimientosSelectDTOList;
	}

}