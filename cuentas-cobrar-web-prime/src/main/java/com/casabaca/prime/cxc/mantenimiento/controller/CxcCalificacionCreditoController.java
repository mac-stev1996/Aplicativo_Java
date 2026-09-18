package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.ExceptionUtils;
import com.casabaca.common.ejb.model.ParamCab;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.model.ParamDetPK;
import com.casabaca.common.ejb.model.Vendedor;
import com.casabaca.common.ejb.service.ParamCabServiceLocal;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.service.VendedorServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;

@ViewScoped
@ManagedBean(name = "cxcCalificacionCreditoController")
public class CxcCalificacionCreditoController extends CommonController implements Serializable {

	private static final long serialVersionUID = 3364717192699645559L;
	private static final Logger log = Logger.getLogger(CxcCalificacionCreditoController.class);

	@EJB(lookup = NombreJNDI.PARAM_DET_SERVICE)
	private ParamDetServiceLocal paramDetServiceLocal;

	@EJB(lookup = NombreJNDI.PARAM_CAB_SERVICE_BEAN)
	private ParamCabServiceLocal paramCabService;

	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;

	@EJB(lookup = NombreJNDI.VENDEDOR_SERVICIO_BEAN)
	private VendedorServicioLocal vendedorService;

	@EJB(lookup = NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL)
	private UsuarioSisServiceLocal usuarioSisService;

	private static final String CALIFICACION_AGENCIA = "CALAGE";
	private static final String CALIFICACION_ASESOR = "CALASE";
	private static final String CALIFICACION_GENERAL = "CALGEN";
	private static final String CALIFICACION_TIEMPO = "CALTIE";
	private List<ParamDet> listTiempos;
	private List<ParamDet> listProductos;
	private List<ParamDet> productos;
	private List<ParamDet> listAsesoresestrella;
	private List<ParamDet> listAgenciasEstrella;
	private List<Agencia> agencias;
	private List<Vendedor> vendedores;

	private ParamCab cabecera;
	private ParamDet parametro;
	private Vendedor vendedor;
	private UsuarioSis usuarioSis;

	private String noCia;
	private String nombreVendedor;
	private String nombreAgencia;

	private boolean esNuevo;

	@PostConstruct
	public void init() {
		esNuevo = false;
		try {
			agencias = agenciaService.findByCompania(getCompania().getNoCia(), null);
			vendedores = vendedorService.findByVendedorActivo(getCompania().getNoCia(), "");
			productos = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CALIFICACION_GENERAL);
			
		} catch (FindException e) {
			e.printStackTrace();
		}
	}

	public String buscarCorreoAsesor(String usuario) {
		String correo = "";
		try {
			usuarioSis = usuarioSisService.findByPk(usuario);
			correo = usuarioSis.getEmail();
		} catch (FindException e) {
			super.error("Error al buscar cabeceras");
			log.error("Error al buscar cabeceras", e);
		}
		return correo;
	}

	public void buscarVendedores() {
		try {
			setVendedores(
					vendedorService.findByVendedorActivo(getCompania().getNoCia(), this.nombreVendedor.toUpperCase()));
		} catch (FindException e) {
			error("Error al consultar vendedores, favor comuniquese con el Dpto. de Sistemas");
			e.printStackTrace();
		}
	}

	public void seleccionarVendedor(Vendedor vendedor) {
		setNombreVendedor(vendedor.getSvvenomb());
		parametro.getParamDetPK().setCodigoDet(vendedor.getSvfvendPK().getSvvecodi());
		parametro.setDescripcion(vendedor.getSvvenomb());
		parametro.setTexto1(vendedor.getSvveusua());
		parametro.setTexto2(buscarCorreoAsesor(vendedor.getSvveusua()));
	}

	public void buscarAgencias() {
		try {
			setAgencias(agenciaService.findByCompania(getCompania().getNoCia(), null));
		} catch (Exception e) {
			error("Error al consultar agencias, favor comuniquese con el Dpto. de Sistemas");
			e.printStackTrace();
		}
	}

	public void seleccionarAgencia(Agencia agencia) {
		setNombreAgencia(agencia.getNombre());
		parametro.getParamDetPK().setCodigoDet(agencia.getAgenciaPK().getCodigo());
		parametro.setDescripcion(agencia.getNombre());
	}


	public void listar() {
		listTiempos = new ArrayList<ParamDet>();
		listProductos = new ArrayList<ParamDet>();
		listAsesoresestrella = new ArrayList<ParamDet>();
		listAgenciasEstrella = new ArrayList<ParamDet>();
		try {
			listProductos = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CALIFICACION_GENERAL);
			if (listProductos.size() > 0) {
				listProductos = listProductos.stream().sorted(Comparator.comparing(ParamDet::getTexto3))
						.collect(Collectors.toList());
			}
			listAsesoresestrella = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CALIFICACION_ASESOR);
			if (listAsesoresestrella.size() > 0) {
				listAsesoresestrella = listAsesoresestrella.stream().sorted(Comparator.comparing(ParamDet::getTexto3))
						.collect(Collectors.toList());
			}			
			listAgenciasEstrella = paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CALIFICACION_AGENCIA);
			if (listAgenciasEstrella.size() > 0) {
				listAgenciasEstrella = listAgenciasEstrella.stream().sorted(Comparator.comparing(ParamDet::getTexto3))
						.collect(Collectors.toList());
			}
			listTiempos= paramDetServiceLocal.buscarDetalleCodigo(getCompania().getNoCia(), CALIFICACION_TIEMPO);
			if (listAgenciasEstrella.size() > 0) {
				listAgenciasEstrella = listAgenciasEstrella.stream().sorted(Comparator.comparing(ParamDet::getTexto3))
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			setMessageGrowl(SEVERITY_ERROR, "Error!", ExceptionUtils.obtainException(e));
		}
	}

	public void abrirItemAsesor() {
		setEsNuevo(true);
		parametro = new ParamDet(new ParamDetPK());
		accionesDialog("DlgResultAsesor", true);
	}

	public void abrirItemAgencia() {
		setEsNuevo(true);
		parametro = new ParamDet(new ParamDetPK());
		accionesDialog("DlgResultAgencia", true);
	}
	public void guardarAsesor() {
		try {
			ParamDetPK id = new ParamDetPK();

			id.setNoCia(getCompania().getNoCia());
			id.setCodigo(CALIFICACION_ASESOR);
			id.setCodigoDet(parametro.getParamDetPK().getCodigoDet());

			if (Objects.isNull(parametro.getTexto1())) {
				addErrorMessage("", "Debe ingresar la informacio del asesor");
				return;
			}
			parametro.setTexto1(parametro.getTexto1());

			if (Objects.isNull(parametro.getTexto3()) || "".equals(parametro.getTexto3())) {
				addErrorMessage("", "Debe ingresar la priorizacion del asesor");
				return;
			}
			parametro.setTexto3(parametro.getTexto3());

			if (Objects.isNull(parametro.getEstado1())) {
				addErrorMessage("", "Debe ingresar la priorizacion del asesor");
				return;
			}
			parametro.setEstado1(parametro.getEstado1());

			parametro.setParamDetPK(id);
			if (esNuevo) {
				paramDetServiceLocal.crearDetalleParametro(parametro);
			} else {
				paramDetServiceLocal.actualizarParametro(parametro);
			}
			addInfoMessage("", "Se ha guardado la informacion exitosamente");
			accionesDialog("DlgResultAsesor", false);
			listar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void guardarAgencia() {
		try {
			ParamDetPK id = new ParamDetPK();

			id.setNoCia(getCompania().getNoCia());
			id.setCodigo(CALIFICACION_AGENCIA);
			id.setCodigoDet(parametro.getParamDetPK().getCodigoDet());

			if (Objects.isNull(parametro.getTexto3()) || "".equals(parametro.getTexto3())) {
				addErrorMessage("", "Debe ingresar la priorizacion del asesor");
				return;
			}
			parametro.setTexto3(parametro.getTexto3());

			if (Objects.isNull(parametro.getEstado1())) {
				addErrorMessage("", "Debe ingresar la priorizacion del asesor");
				return;
			}
			parametro.setEstado1(parametro.getEstado1());

			parametro.setParamDetPK(id);
			if (esNuevo) {
				paramDetServiceLocal.crearDetalleParametro(parametro);
			} else {
				paramDetServiceLocal.actualizarParametro(parametro);
			}
			addInfoMessage("", "Se ha guardado la informacion exitosamente");
			accionesDialog("DlgResultAgencia", false);
			listar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void guardarProducto() {
		try {
			ParamDetPK id = new ParamDetPK();

			id.setNoCia(getCompania().getNoCia());
			id.setCodigo(CALIFICACION_GENERAL);
			id.setCodigoDet(parametro.getParamDetPK().getCodigoDet());

			if (Objects.isNull(parametro.getTexto3()) || "".equals(parametro.getTexto3())) {
				addErrorMessage("", "Debe ingresar la priorizacion del producto");
				return;
			}
			parametro.setTexto3(parametro.getTexto3());

			parametro.setEstado1("A");

			parametro.setParamDetPK(id);
			if (esNuevo) {
				paramDetServiceLocal.crearDetalleParametro(parametro);
			} else {
				paramDetServiceLocal.actualizarParametro(parametro);
			}
			addInfoMessage("", "Se ha guardado la informacion exitosamente");
			accionesDialog("DlgResultProducto", false);
			listar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void guardarTiempo() {
		try {
			ParamDetPK id = new ParamDetPK();

			id.setNoCia(getCompania().getNoCia());
			id.setCodigo(CALIFICACION_TIEMPO);
			id.setCodigoDet(parametro.getParamDetPK().getCodigoDet());

			if (Objects.isNull(parametro.getTexto1()) || "".equals(parametro.getTexto1())) {
				addErrorMessage("", "Debe ingresar la priorizacion del asesor");
				return;
			}
			parametro.setTexto1(parametro.getTexto1());
			parametro.setTexto3(parametro.getTexto1());


			parametro.setParamDetPK(id);
			if (esNuevo) {
				paramDetServiceLocal.crearDetalleParametro(parametro);
			} else {
				paramDetServiceLocal.actualizarParametro(parametro);
			}
			addInfoMessage("", "Se ha guardado la informacion exitosamente");
			accionesDialog("DlgResultTiempo", false);
			listar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void editarProducto(ParamDet entrada) {
		setEsNuevo(false);
		parametro = entrada;
		accionesDialog("DlgResultProducto", true);
	}

	public void eliminarProducto(ParamDet entrada) {
		try {
			paramDetServiceLocal.destroy(entrada);
			listar();
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void editarAsesor(ParamDet entrada) {
		setEsNuevo(false);
		parametro = entrada;
		accionesDialog("DlgResultAsesor", true);
	}

	public void eliminarAsesor(ParamDet entrada) {
		try {
			paramDetServiceLocal.destroy(entrada);
			addInfoMessage("", "Se ha eliminado el registro exitosamente");
			listar();
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void editarAgencia(ParamDet entrada) {
		setEsNuevo(false);
		parametro = entrada;
		accionesDialog("DlgResultAgencia", true);
	}

	public void eliminarAgencia(ParamDet entrada) {
		try {
			paramDetServiceLocal.destroy(entrada);
			listar();
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}

	public void editarTiempo(ParamDet entrada) {
		setEsNuevo(false);
		parametro = entrada;
		accionesDialog("DlgResultTiempo", true);
	}

	public void eliminarTiempo(ParamDet entrada) {
		try {
			paramDetServiceLocal.destroy(entrada);
			addInfoMessage("", "Se ha eliminado el registro exitosamente");
			listar();
		} catch (DeleteException e) {
			e.printStackTrace();
		}
	}
	
	public List<ParamDet> getListProductos() {
		return listProductos;
	}

	public void setListProductos(List<ParamDet> listProductos) {
		this.listProductos = listProductos;
	}

	
	public List<ParamDet> getListTiempos() {
		return listTiempos;
	}

	public void setListTiempos(List<ParamDet> listTiempos) {
		this.listTiempos = listTiempos;
	}

	public ParamDet getParametro() {
		return parametro;
	}

	public void setParametro(ParamDet parametro) {
		this.parametro = parametro;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public void setAgencias(List<Agencia> agencias) {
		this.agencias = agencias;
	}

	public boolean isEsNuevo() {
		return esNuevo;
	}

	public void setEsNuevo(boolean esNuevo) {
		this.esNuevo = esNuevo;
	}

	public ParamCab getCabecera() {
		return cabecera;
	}

	public void setCabecera(ParamCab cabecera) {
		this.cabecera = cabecera;
	}

	public List<Vendedor> getVendedores() {
		return vendedores;
	}

	public void setVendedores(List<Vendedor> vendedores) {
		this.vendedores = vendedores;
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getNombreVendedor() {
		return nombreVendedor;
	}

	public void setNombreVendedor(String nombreVendedor) {
		this.nombreVendedor = nombreVendedor;
	}

	public String getNombreAgencia() {
		return nombreAgencia;
	}

	public void setNombreAgencia(String nombreAgencia) {
		this.nombreAgencia = nombreAgencia;
	}

	public UsuarioSis getUsuarioSis() {
		return usuarioSis;
	}

	public void setUsuarioSis(UsuarioSis usuarioSis) {
		this.usuarioSis = usuarioSis;
	}

	public List<ParamDet> getListAsesoresestrella() {
		return listAsesoresestrella;
	}

	public void setListAsesoresestrella(List<ParamDet> listAsesoresestrella) {
		this.listAsesoresestrella = listAsesoresestrella;
	}

	public List<ParamDet> getListAgenciasEstrella() {
		return listAgenciasEstrella;
	}

	public void setListAgenciasEstrella(List<ParamDet> listAgenciasEstrella) {
		this.listAgenciasEstrella = listAgenciasEstrella;
	}

	public List<ParamDet> getProductos() {
		return productos;
	}

	public void setProductos(List<ParamDet> productos) {
		this.productos = productos;
	}
}
