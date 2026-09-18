/* 
 * ConsultaContratosCertController.java 
 * May 8, 2023
 * Copyright 2023 Centric.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.consultas.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.model.Arccmd;
import com.casabaca.common.ejb.model.Arcjca;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ArcjcaServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcArccmdDetCambios;
import com.casabaca.cxc.ejb.servicio.CxcArccmdDetCambiosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.inventario.ejb.model.FacVentasCab;
import com.casabaca.inventario.ejb.service.FacVentasServiceLocal;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.service.MailServiceLocal;
import com.casabaca.s3s.ejb.service.SisMailUsuarioServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

/**
 * <b> Controlador para consultar contratos Certero </b>
 * 
 * @author jreyes
 * @version $1.0$
 */
@ViewScoped
@ManagedBean
public class ConsultaContratosCertController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6823665700871965287L;

	private static final Logger LOG = Logger.getLogger(ConsultaContratosCertController.class);

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdServiceLocal;

	@EJB(lookup = NombreJNDI.ARCJCA_SERVICE_BEAN)
	private ArcjcaServiceLocal arcjcaServiceLocal;

	@EJB(lookup = NombreJNDI.FAC_VENTAS_SERVICE)
	private FacVentasServiceLocal facVentasService;

	@EJB(lookup = NombreJNDI.CXC_ARCCMD_DET_CAMBIOS_SERVICE)
	private CxcArccmdDetCambiosServiceLocal arccmdDetCambiosServiceLocal;

	@EJB(lookup = NombreJNDI.MAIL_SERVICE)
	private MailServiceLocal mailService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	@EJB(lookup = NombreJNDI.SIS_MAIL_USUARIO_SERVICE)
	private SisMailUsuarioServiceLocal mailUsuarioService;

	private static final String RUTA = "0000";

	private List<Arccmd> listadoLetrasContrato;
	private List<Arcjca> listadoCabeceraPagos;
	private List<FacVentasCab> listadoCabeceraFacturas;
	private List<CxcArccmdDetCambios> listadoCambios;
	private List<Object[]> listadoCabeceraDiariosA;
	private List<Object[]> listadoPagos;
	private List<Object[]> listadoDiariosA;
	private List<Object[]> listadoFacturas;
	private List<Object[]> listadoCuotasPagadas;
	private Arccmd cuotaEditada;
	private Arccmd datosCuota;
	private String noContrato;
	private String estado;
	private Boolean activarPagos;
	private String aplicaSaldo;
	private BigDecimal valorAnterior;
	private String comentarioCambio;
	private String cuotaModificada;

	@PostConstruct
	public void init() {
		cuotaEditada = new Arccmd();
		datosCuota = new Arccmd();
		noContrato = null;
		aplicaSaldo = null;
		listadoCabeceraPagos = new ArrayList<Arcjca>();
		listadoLetrasContrato = new ArrayList<Arccmd>();
		listadoCabeceraDiariosA = new ArrayList<Object[]>();
		listadoPagos = new ArrayList<Object[]>();
		listadoDiariosA = new ArrayList<Object[]>();
		listadoFacturas = new ArrayList<Object[]>();
		listadoCuotasPagadas = new ArrayList<Object[]>();
		activarPagos = Boolean.FALSE;
		listadoCabeceraFacturas = new ArrayList<FacVentasCab>();
		comentarioCambio = null;
		listadoCambios = new ArrayList<CxcArccmdDetCambios>();

	}

	public void buscarContrato() {

		try {
			if (noContrato != null && aplicaSaldo != null) {

				listadoLetrasContrato = arccmdServiceLocal.obtenerLetrasContrato(getCompania().getNoCia(), noContrato,
						aplicaSaldo);

				if (listadoLetrasContrato.size() == 0) {

					error("Contrato no encontrato favor validar datos!!!");

				} else {

					datosCuota = new Arccmd();
					datosCuota = listadoLetrasContrato.get(0);
					FacVentasCab factura = facVentasService.getFacturaPorRutaClienteFisico(CommonConstants.COMPRASIGMA,
							String.valueOf(datosCuota.getNoCliente()), noContrato, RUTA);
					if(factura!=null) {
						datosCuota.setNombreGrupo(factura.getGrupoPlan());
					}
					obtenerCabecera();
				}
			} else {

				error("El campo contrato y cuotas con saldo son requeridos!!!");

			}
		} catch (NoResultException e) {
			error("Favor Validar que el numero de contrato sea correcto!!!");
		}

	}

	public void editarCouta(Arccmd arccmdEditada) {
		comentarioCambio = null;
		valorAnterior = BigDecimal.ZERO;
		cuotaEditada = arccmdEditada;
		valorAnterior = cuotaEditada.getSaldo();
		accionesDialog("editarCuota", Boolean.TRUE);

	}

	public void actualizarCouta() {

		try {

			if (comentarioCambio != null && !comentarioCambio.isEmpty()) {

				CxcArccmdDetCambios nuevo = new CxcArccmdDetCambios();
				nuevo.setFechaCreacion(new Date());
				nuevo.setNoCia(cuotaEditada.getId().getNoCia());
				nuevo.setNoDocu(cuotaEditada.getId().getNoDocu());
				nuevo.setUsuarioCreacion(getUsuario().getUsuario());
				nuevo.setMotivo(comentarioCambio);
				nuevo.setSaldoNuevo(cuotaEditada.getSaldo());
				nuevo.setSaldoAnterior(valorAnterior);
				arccmdDetCambiosServiceLocal.insertar(nuevo);
				arccmdServiceLocal.updateArccmd(cuotaEditada);
				info("Cuota Actualizada con Exito");
				accionesDialog("editarCuota", Boolean.FALSE);
				buscarContrato();
				enviarMail(cuotaEditada, nuevo);
			} else {

				error("Campo motivo es requerido para actualizar saldos");
			}
		} catch (UpdateException e) {
			error("No se pudo actualizar registro: " + e.getDetail());
			LOG.error(e);
		} catch (InsertException e) {
			error("No se pudo Insertar registro: " + e.getDetail());
			LOG.error(e);
		}
	}

	public void enviarMail(Arccmd arccmd, CxcArccmdDetCambios cxcArccmdDetCambios) {

		try {
			String mailFrom = usuarioSisService.mailUsuario(getUsuario().getUsuario());

			String subject = "Cambio Cuota : ".concat(arccmd.getNoDocuRefe()).concat(", del contrato ")
					.concat(arccmd.getNoFisico());
			StringBuffer body = new StringBuffer();
			body = new StringBuffer();
			body.append("El usuario " + getUsuario().getNombre());
			body.append(" Modifico la cuota: ").append(arccmd.getNoDocuRefe()).append(", del contrato :")
					.append(arccmd.getNoFisico()).append("<br><br>");
			body.append(" Saldo anterior : ").append(valorAnterior).append("<br><br>");
			body.append(" Saldo actual : ").append(arccmd.getSaldo()).append("<br><br>");
			body.append("Saludos Cordiales." + "<br><br>");

			List<Object[]> listaDestinatarios = mailUsuarioService.findByEmailGrupoYExterno("CERTE");

			mailService.sendEmailVarios(getSisMailServidores(getCompania().getNoCia()), mailFrom,
					listaDestinatarios.stream().map(d -> String.valueOf(d[1])).collect(Collectors.toList()), null, null,
					subject, body, Boolean.FALSE);
		} catch (FindException | GeneralException e) {
			error("Ocurrio un error al enviar correo!!!");
			LOG.error(e);
		}
	}

	public void obtenerCabecera() {

		if (noContrato != null) {

			listadoCabeceraPagos = arcjcaServiceLocal.consultarPagosContrato(getCompania().getNoCia(), noContrato);

			listadoCabeceraDiariosA = arcjcaServiceLocal.consultarCabeceraDiarioAcertero(getCompania().getNoCia(),
					noContrato, datosCuota.getNoCliente());

			listadoCabeceraFacturas = facVentasService.obtenerFacturasContrato(getCompania().getNoCia(), noContrato);

		} else {

			error("Para obtener el detalle de pagos debe ingresar un Contrato y realizar la busqueda!!!");

		}

	}

	public void consultaDetPago(Arcjca arcjca) {

		listadoPagos = arcjcaServiceLocal.consultarIngresoCajaCertero(arcjca.getId().getNoCia(), noContrato,
				arcjca.getNoCliente().toString(), arcjca.getId().getNoDocu());

		accionesDialog("detPago", Boolean.TRUE);

	}

	public void consultaDetCuotas(Arcjca arcjca) {

		listadoCuotasPagadas = arcjcaServiceLocal.consultarDetallePagCertero(arcjca.getId().getNoCia(),
				arcjca.getId().getNoDocu());

		accionesDialog("detCuotas", Boolean.TRUE);

	}

	public void consultaDetDiario(Object[] diario) {

		listadoDiariosA = arcjcaServiceLocal.consultarDiarioAcertero(getCompania().getNoCia(), diario[1].toString(),
				diario[0].toString());
		accionesDialog("detDiario", Boolean.TRUE);

	}

	public void consultaDetFact(FacVentasCab fact) {

		listadoFacturas = arcjcaServiceLocal.consultarFacturacionCertero(fact.getFacVentasCabPK().getNoCia(),
				noContrato, fact.getNoCliente().toString(), fact.getFacVentasCabPK().getNoFactu());
		accionesDialog("detFact", Boolean.TRUE);
	}

	public void consultarCambiosLetra(Arccmd arccmdSelected) {

		cuotaModificada = arccmdSelected.getNoDocuRefe();

		listadoCambios = new ArrayList<CxcArccmdDetCambios>();
		listadoCambios = arccmdDetCambiosServiceLocal.obtenerListado(arccmdSelected.getId().getNoCia(),
				arccmdSelected.getId().getNoDocu());

		accionesDialog("detCambios", Boolean.TRUE);

	}

	public List<Arccmd> getListadoLetrasContrato() {
		return listadoLetrasContrato;
	}

	public void setListadoLetrasContrato(List<Arccmd> listadoLetrasContrato) {
		this.listadoLetrasContrato = listadoLetrasContrato;
	}

	public String getNoContrato() {
		return noContrato;
	}

	public void setNoContrato(String noContrato) {
		this.noContrato = noContrato;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Arccmd getCuotaEditada() {
		return cuotaEditada;
	}

	public void setCuotaEditada(Arccmd cuotaEditada) {
		this.cuotaEditada = cuotaEditada;
	}

	public Arccmd getDatosCuota() {
		return datosCuota;
	}

	public void setDatosCuota(Arccmd datosCuota) {
		this.datosCuota = datosCuota;
	}

	public Boolean getActivarPagos() {
		return activarPagos;
	}

	public void setActivarPagos(Boolean activarPagos) {
		this.activarPagos = activarPagos;
	}

	public List<Object[]> getListadoPagos() {
		return listadoPagos;
	}

	public void setListadoPagos(List<Object[]> listadoPagos) {
		this.listadoPagos = listadoPagos;
	}

	public List<Object[]> getListadoDiariosA() {
		return listadoDiariosA;
	}

	public void setListadoDiariosA(List<Object[]> listadoDiariosA) {
		this.listadoDiariosA = listadoDiariosA;
	}

	public List<Object[]> getListadoFacturas() {
		return listadoFacturas;
	}

	public void setListadoFacturas(List<Object[]> listadoFacturas) {
		this.listadoFacturas = listadoFacturas;
	}

	public List<Arcjca> getListadoCabeceraPagos() {
		return listadoCabeceraPagos;
	}

	public void setListadoCabeceraPagos(List<Arcjca> listadoCabeceraPagos) {
		this.listadoCabeceraPagos = listadoCabeceraPagos;
	}

	public List<FacVentasCab> getListadoCabeceraFacturas() {
		return listadoCabeceraFacturas;
	}

	public void setListadoCabeceraFacturas(List<FacVentasCab> listadoCabeceraFacturas) {
		this.listadoCabeceraFacturas = listadoCabeceraFacturas;
	}

	public List<Object[]> getListadoCabeceraDiariosA() {
		return listadoCabeceraDiariosA;
	}

	public void setListadoCabeceraDiariosA(List<Object[]> listadoCabeceraDiariosA) {
		this.listadoCabeceraDiariosA = listadoCabeceraDiariosA;
	}

	public String getAplicaSaldo() {
		return aplicaSaldo;
	}

	public void setAplicaSaldo(String aplicaSaldo) {
		this.aplicaSaldo = aplicaSaldo;
	}

	public List<Object[]> getListadoCuotasPagadas() {
		return listadoCuotasPagadas;
	}

	public void setListadoCuotasPagadas(List<Object[]> listadoCuotasPagadas) {
		this.listadoCuotasPagadas = listadoCuotasPagadas;
	}

	public BigDecimal getValorAnterior() {
		return valorAnterior;
	}

	public void setValorAnterior(BigDecimal valorAnterior) {
		this.valorAnterior = valorAnterior;
	}

	public String getComentarioCambio() {
		return comentarioCambio;
	}

	public void setComentarioCambio(String comentarioCambio) {
		this.comentarioCambio = comentarioCambio;
	}

	public List<CxcArccmdDetCambios> getListadoCambios() {
		return listadoCambios;
	}

	public void setListadoCambios(List<CxcArccmdDetCambios> listadoCambios) {
		this.listadoCambios = listadoCambios;
	}

	public String getCuotaModificada() {
		return cuotaModificada;
	}

	public void setCuotaModificada(String cuotaModificada) {
		this.cuotaModificada = cuotaModificada;
	}

}
