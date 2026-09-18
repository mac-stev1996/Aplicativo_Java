package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.administration.ejb.model.ControlFormu;
import com.casabaca.administration.ejb.service.ControlFormuServiceLocal;
import com.casabaca.caja.ejb.modelo.Arcctd;
import com.casabaca.caja.ejb.modelo.ArcctdPK;
import com.casabaca.caja.ejb.service.ArcctdServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.common.dialog.controller.DialogPlanCuentasController;

/**
 * @author jl_reyes
 *
 */
@ManagedBean
@ViewScoped
public class CxcTiposDocController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2345629973827717625L;

	static final Logger log = Logger.getLogger(CxcTiposDocController.class);

	static final String MODULO = "CC";

	@EJB(lookup = NombreJNDI.ARCCTD_SERVICE)
	private ArcctdServiceLocal arcctdServiceLocal;

	@EJB(lookup = NombreJNDI.CONTROL_FORMU_SERVICE_BEAN)
	private ControlFormuServiceLocal controlFormuServiceLocal;

	private List<Arcctd> listadoArcctd;
	private List<ControlFormu> listadoControlFormu;
	private Arcctd arcctdNuevo;
	private Arcctd arcctdSelected;

	@ManagedProperty("#{dialogPlanCuentasController}")
	private DialogPlanCuentasController dialogPlanCuentasController;

	private String origenPlanCta;

	@PostConstruct
	public void init() {

		listadoArcctd = new ArrayList<Arcctd>();
		listadoControlFormu = new ArrayList<ControlFormu>();
		arcctdNuevo = new Arcctd(new ArcctdPK(getCompania().getNoCia(), null));
		arcctdSelected = new Arcctd();

		listadoArcctd = arcctdServiceLocal.obtenerPorNocia(getCompania().getNoCia());

	}

	public void abrirEditar(Arcctd arcctd) {
		arcctdSelected = new Arcctd();

		arcctdSelected = arcctd;

		if ("S".equalsIgnoreCase(arcctdSelected.getFactura())) {

			arcctdSelected.setValorRadio("FA");

		} else if ("S".equalsIgnoreCase(arcctdSelected.getAjuste())) {
			arcctdSelected.setValorRadio("AJ");

		} else if ("S".equalsIgnoreCase(arcctdSelected.getConjuntar())) {
			arcctdSelected.setValorRadio("UC");

		} else if ("S".equalsIgnoreCase(arcctdSelected.getCksDev())) {
			arcctdSelected.setValorRadio("CD");

		} else if ("S".equalsIgnoreCase(arcctdSelected.getRefinancia())) {
			arcctdSelected.setValorRadio("RD");

		} else {
			arcctdSelected.setValorRadio("GR");

		}

		listadoControlFormu = controlFormuServiceLocal.obtenerListadoNociaModulo(getCompania().getNoCia(), MODULO);

	}

	public void setarCamposAntesGuardar(Arcctd arcctd) {
		if ("FA".equalsIgnoreCase(arcctd.getValorRadio())) {

			arcctd.setFactura("S");

		} else if ("AJ".equalsIgnoreCase(arcctd.getValorRadio())) {

			arcctd.setAjuste("S");

		} else if ("UC".equalsIgnoreCase(arcctd.getValorRadio())) {

			arcctd.setConjuntar("S");

		} else if ("CD".equalsIgnoreCase(arcctd.getValorRadio())) {

			arcctd.setCksDev("S");
		} else if ("RD".equalsIgnoreCase(arcctd.getValorRadio())) {

			arcctd.setRefinancia("S");
		}

	}

	public void cargarValoresNuevo() {
		arcctdNuevo = new Arcctd();
		arcctdNuevo = new Arcctd(new ArcctdPK(getCompania().getNoCia(), null));
		arcctdNuevo.setAjuste("N");
		arcctdNuevo.setConjuntar("N");
		arcctdNuevo.setCksDev("N");
		arcctdNuevo.setCobroVendedor("N");
		arcctdNuevo.setRefinancia("N");
		arcctdNuevo.setFactura("N");

		listadoControlFormu = controlFormuServiceLocal.obtenerListadoNociaModulo(getCompania().getNoCia(), MODULO);

	}

	public void grabar() {
		try {

			if (validarCamposRequeridos(arcctdNuevo)) {

				if (existe(arcctdNuevo)) {

					validaDocumentoUnico(arcctdNuevo);

					setarCamposAntesGuardar(arcctdNuevo);
					arcctdServiceLocal.insertar(arcctdNuevo);

					info("Registro Almacenado con Exito");

				}
			}

		} catch (Exception e) {
			error("Ocurrio un error al Grabar el Registro");
			log.error(e);
		}

	}

	public void actualizar() {

		try {

			arcctdServiceLocal.actualizar(arcctdSelected);
			info("Registro Actualizado Con Exito");

		} catch (Exception e) {
			error("Ocurrio un error al Actualizar el Registro");
			log.error(e);
		}

	}

	public Boolean existe(Arcctd arcctd) {
		try {

			Long valor = arcctdServiceLocal
					.obtenerPorPK(new ArcctdPK(arcctd.getPk().getNoCia(), arcctd.getPk().getTipo()));
			if (valor > 0) {

				error("Registro Ingresado ya Existe,  Para la Compania que esta conectado!!!");
				return false;

			}

			return true;

		} catch (Exception e) {
			log.error(e);
			return false;
		}

	}

	public void asignarValores() {

		if ("FA".equalsIgnoreCase(arcctdNuevo.getValorRadio()) || "FA".equalsIgnoreCase(arcctdNuevo.getValorRadio())
				|| "FA".equalsIgnoreCase(arcctdNuevo.getValorRadio())) {
			arcctdNuevo.setAfectaSaldo("S");

		} else {

			arcctdNuevo.setAfectaSaldo("N");

		}

		validaDocumentoUnico(arcctdNuevo);

	}

	public void validaDocumentoUnico(Arcctd arcctd) {

		try {
			if ("GR".equalsIgnoreCase(arcctd.getValorRadio())) {

				Long valor = arcctdServiceLocal.obtenerPorTipoMotivo(arcctd);

				if (valor > 0) {

					error("No pueden existir 2 tipos de documento con esta misma definicion, ver tipo de doc.: "
							+ arcctd.getPk().getTipo());

				}

			}

		} catch (Exception e) {
			log.error(e);
		}

	}

	public void abrirPlanCuentas(String origen) {
		dialogPlanCuentasController.setNombreDialog("dlgCuentas");
		origenPlanCta = origen;
		accionesDialog("dlgCuentas", Boolean.TRUE);

	}

	public Boolean validarCamposRequeridos(Arcctd arcctd) {

		if (arcctd.getPk().getTipo().isEmpty() && arcctd.getDescripcion().isEmpty() && arcctd.getTipoMov() == null
				&& arcctd.getValorRadio() == null && arcctd.getFormulario() == null) {

			error("Campos Requeridos: Tipo, Descripcion, Tipo Movimiento, Utilizado Como, Formulario");

			return false;

		} else {

			return true;
		}

	}

	public List<Arcctd> getListadoArcctd() {
		return listadoArcctd;
	}

	public void setListadoArcctd(List<Arcctd> listadoArcctd) {
		this.listadoArcctd = listadoArcctd;
	}

	public List<ControlFormu> getListadoControlFormu() {
		return listadoControlFormu;
	}

	public void setListadoControlFormu(List<ControlFormu> listadoControlFormu) {
		this.listadoControlFormu = listadoControlFormu;
	}

	public Arcctd getArcctdNuevo() {
		if ("N".equals(origenPlanCta) && dialogPlanCuentasController != null
				&& dialogPlanCuentasController.getConPlantas() != null) {
			arcctdNuevo
					.setCtaContrapartidaScb(dialogPlanCuentasController.getConPlantas().getConPlatasPk().getCtaconId());
			arcctdNuevo.setNombreCuenta(dialogPlanCuentasController.getConPlantas().getNombre());
			dialogPlanCuentasController.setConPlantas(null);
		}
		return arcctdNuevo;
	}

	public void setArcctdNuevo(Arcctd arcctdNuevo) {
		this.arcctdNuevo = arcctdNuevo;
	}

	public Arcctd getArcctdSelected() {

		if ("E".equals(origenPlanCta) && dialogPlanCuentasController != null
				&& dialogPlanCuentasController.getConPlantas() != null) {
			arcctdSelected
					.setCtaContrapartidaScb(dialogPlanCuentasController.getConPlantas().getConPlatasPk().getCtaconId());
			arcctdSelected.setNombreCuenta(dialogPlanCuentasController.getConPlantas().getNombre());
			dialogPlanCuentasController.setConPlantas(null);
		}

		return arcctdSelected;
	}

	public void setArcctdSelected(Arcctd arcctdSelected) {
		this.arcctdSelected = arcctdSelected;
	}

	public DialogPlanCuentasController getDialogPlanCuentasController() {
		return dialogPlanCuentasController;
	}

	public void setDialogPlanCuentasController(DialogPlanCuentasController dialogPlanCuentasController) {
		this.dialogPlanCuentasController = dialogPlanCuentasController;
	}

	public String getOrigenPlanCta() {
		return origenPlanCta;
	}

	public void setOrigenPlanCta(String origenPlanCta) {
		this.origenPlanCta = origenPlanCta;
	}

}
