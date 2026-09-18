package com.casabaca.prime.cxc.mantenimiento.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.modelo.CxcCanalContactado;
import com.casabaca.cxc.ejb.modelo.CxcCanalContactadoPK;
import com.casabaca.cxc.ejb.modelo.CxcCanalGestion;
import com.casabaca.cxc.ejb.modelo.CxcCanalGestionPK;
import com.casabaca.cxc.ejb.modelo.CxcResultadoGestion;
import com.casabaca.cxc.ejb.modelo.CxcResultadoGestionPK;
import com.casabaca.cxc.ejb.servicio.CxcCanalContactadoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcCanalGestionServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcResultadoGestionServiceLocal;
import com.casabaca.exception.InsertException;
import com.casabaca.exception.UpdateException;
import com.casabaca.prime.cxc.common.CommonController;

/**
 * Controlador para mantenimiento de parámetros de seguimiento de cobranzas.
 * Gestiona los canales, resultados y contactados relacionados con cobranzas.
 */
@ViewScoped
@ManagedBean
public class CxcSeguimientoCobranzaParamController extends CommonController implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(CxcSeguimientoCobranzaParamController.class);

	@EJB(lookup = NombreJNDI.CXC_CANAL_GESTION_SERVICE)
	private CxcCanalGestionServiceLocal cxcCanalGestionServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_CANAL_CONTACTADO_SERVICE)
	private CxcCanalContactadoServiceLocal cxcCanalContactadoServiceLocal;

	@EJB(lookup = NombreJNDI.CXC_RESULTADO_GESTION_SERVICE)
	private CxcResultadoGestionServiceLocal cxcResultadoGestionServiceLocal;

	private List<CxcCanalGestion> canalesGestion;
	private List<CxcCanalContactado> canalesContactado;
	private List<CxcResultadoGestion> resultadosGestion;

	private CxcCanalGestion canalGestion;
	private CxcCanalContactado canalContactado;
	private CxcResultadoGestion resultadoGestion;

	private boolean editar;
	private Long codigoGenerado;
	private Long codigoGeneradoContactado;
	private Long codigoGeneradoResultado;

	/**
	 * Inicializa el controlador cargando los datos necesarios.
	 */
	@PostConstruct
	public void init() {
		try {
			cargarDatos();
		} catch (Exception e) {
			LOGGER.error("Error al cargar listados: ", e);
		}
	}

	/**
	 * Carga todos los datos necesarios para las tablas.
	 */
	private void cargarDatos() {
		canalesGestion = cxcCanalGestionServiceLocal.obtenerNocia(getCompania().getNoCia());
		canalesContactado = cxcCanalContactadoServiceLocal.obtenerNocia(getCompania().getNoCia());
		resultadosGestion = cxcResultadoGestionServiceLocal.obtenerNocia(getCompania().getNoCia());
	}

	/**
	 * Prepara la creación de un nuevo canal de gestión.
	 */
	public void nuevoCanalGestion() {
		LOGGER.info("Creando nuevo canal gestión");
		editar = false;
		canalGestion = new CxcCanalGestion();
		canalGestion.setId(new CxcCanalGestionPK());
		canalGestion.getId().setNoCia(getCompania().getNoCia());
		canalGestion.setEstado(CommonConstants.ACTIVO);

		codigoGenerado = cxcCanalGestionServiceLocal.generarSecuenciaCxcResultadoGestion(getCompania().getNoCia());
	}

	/**
	 * Configura un objeto existente para su edición.
	 * 
	 * @param item El canal de gestión a editar
	 */
	public void editarCanalGestion(CxcCanalGestion item) {
		LOGGER.info("Editando canal gestión");
		editar = true;
		canalGestion = new CxcCanalGestion();
		canalGestion.setId(new CxcCanalGestionPK(item.getId().getNoCia(), item.getId().getCodigo()));
		canalGestion.setDescripcion(item.getDescripcion());
		canalGestion.setEstado(item.getEstado());
	}

	/**
	 * Guarda o actualiza un canal de gestión.
	 */
	public void guardarCanalGestion() {
		try {
			if (!editar) {
				canalGestion.getId().setCodigo(codigoGenerado);
				cxcCanalGestionServiceLocal.insertar(canalGestion);
				info("Canal de gestión creado con éxito");
			} else {
				cxcCanalGestionServiceLocal.actualizar(canalGestion);
				info("Canal de gestión actualizado con éxito");
			}

			cargarDatos();
		} catch (InsertException | UpdateException e) {
			error("Error: " + e.getMessage());
			LOGGER.error("Error al guardar canal gestión: ", e);
		}
	}

	/**
	 * Prepara la creación de un nuevo canal contactado.
	 */
	public void nuevoCanalContactado() {
		LOGGER.info("Creando nuevo canal contactado");
		editar = false;
		canalContactado = new CxcCanalContactado();
		canalContactado.setId(new CxcCanalContactadoPK());
		canalContactado.getId().setNoCia(getCompania().getNoCia());
		canalContactado.setEstado(CommonConstants.ACTIVO);

		codigoGeneradoContactado = cxcCanalContactadoServiceLocal
				.generarSecuenciaCxcResultadoGestion(getCompania().getNoCia());
	}

	/**
	 * Configura un objeto existente para su edición.
	 * 
	 * @param item El canal contactado a editar
	 */
	public void editarCanalContactado(CxcCanalContactado item) {
		LOGGER.info("Editando canal contactado");
		editar = true;
		canalContactado = new CxcCanalContactado();
		canalContactado.setId(new CxcCanalContactadoPK(item.getId().getNoCia(), item.getId().getCodigo()));
		canalContactado.setDescripcion(item.getDescripcion());
		canalContactado.setEstado(item.getEstado());
	}

	/**
	 * Guarda o actualiza un canal contactado.
	 */
	public void guardarCanalContactado() {
		try {
			if (!editar) {
				canalContactado.getId().setCodigo(codigoGeneradoContactado);
				cxcCanalContactadoServiceLocal.insertar(canalContactado);
				info("Canal contactado creado con éxito");
			} else {
				cxcCanalContactadoServiceLocal.actualizar(canalContactado);
				info("Canal contactado actualizado con éxito");
			}

			cargarDatos();
		} catch (InsertException | UpdateException e) {
			error("Error al guardar: " + e.getMessage());
			LOGGER.error("Error al guardar canal contactado: ", e);
		}
	}

	/**
	 * Prepara la creación de un nuevo resultado de gestión.
	 */
	public void nuevoResultadoGestion() {
		LOGGER.info("Creando nuevo resultado gestión");
		editar = false;
		resultadoGestion = new CxcResultadoGestion();
		resultadoGestion.setId(new CxcResultadoGestionPK());
		resultadoGestion.getId().setNoCia(getCompania().getNoCia());
		resultadoGestion.setEstado(CommonConstants.ACTIVO);

		codigoGeneradoResultado = cxcResultadoGestionServiceLocal
				.generarSecuenciaCxcResultadoGestion(getCompania().getNoCia());
	}

	/**
	 * Configura un objeto existente para su edición.
	 * 
	 * @param item El resultado de gestión a editar
	 */
	public void editarResultadoGestion(CxcResultadoGestion item) {
		LOGGER.info("Editando resultado gestión");
		editar = true;
		resultadoGestion = new CxcResultadoGestion();
		resultadoGestion.setId(new CxcResultadoGestionPK(item.getId().getNoCia(), item.getId().getCodigo()));
		resultadoGestion.setDescripcion(item.getDescripcion());
		resultadoGestion.setEstado(item.getEstado());
	}

	/**
	 * Guarda o actualiza un resultado de gestión.
	 */
	public void guardarResultadoGestion() {
		try {
			if (!editar) {
				resultadoGestion.getId().setCodigo(codigoGeneradoResultado);
				cxcResultadoGestionServiceLocal.insertar(resultadoGestion);
				info("Resultado de gestión creado con éxito");
			} else {
				cxcResultadoGestionServiceLocal.actualizar(resultadoGestion);
				info("Resultado de gestión actualizado con éxito");
			}

			cargarDatos();
		} catch (InsertException | UpdateException e) {
			error("Error al guardar: " + e.getMessage());
			LOGGER.error("Error al guardar resultado gestión: ", e);
		}
	}

	/**
	 * Retorna el tamaño del lote para paginación.
	 * 
	 * @return Tamaño del lote
	 */
	public int getBatchSize() {
		return CommonConstants.BATCH_SIZE;
	}

	// Getters y Setters

	public List<CxcCanalGestion> getCanalesGestion() {
		return canalesGestion;
	}

	public void setCanalesGestion(List<CxcCanalGestion> canalesGestion) {
		this.canalesGestion = canalesGestion;
	}

	public List<CxcCanalContactado> getCanalesContactado() {
		return canalesContactado;
	}

	public void setCanalesContactado(List<CxcCanalContactado> canalesContactado) {
		this.canalesContactado = canalesContactado;
	}

	public List<CxcResultadoGestion> getResultadosGestion() {
		return resultadosGestion;
	}

	public void setResultadosGestion(List<CxcResultadoGestion> resultadosGestion) {
		this.resultadosGestion = resultadosGestion;
	}

	public CxcCanalGestion getCanalGestion() {
		return canalGestion;
	}

	public void setCanalGestion(CxcCanalGestion canalGestion) {
		this.canalGestion = canalGestion;
	}

	public CxcCanalContactado getCanalContactado() {
		return canalContactado;
	}

	public void setCanalContactado(CxcCanalContactado canalContactado) {
		this.canalContactado = canalContactado;
	}

	public CxcResultadoGestion getResultadoGestion() {
		return resultadoGestion;
	}

	public void setResultadoGestion(CxcResultadoGestion resultadoGestion) {
		this.resultadoGestion = resultadoGestion;
	}

	public boolean isEditar() {
		return editar;
	}

	public void setEditar(boolean editar) {
		this.editar = editar;
	}

	public Long getCodigoGenerado() {
		return codigoGenerado;
	}

	public void setCodigoGenerado(Long codigoGenerado) {
		this.codigoGenerado = codigoGenerado;
	}

	public Long getCodigoGeneradoContactado() {
		return codigoGeneradoContactado;
	}

	public Long getCodigoGeneradoResultado() {
		return codigoGeneradoResultado;
	}

	public void setCodigoGeneradoResultado(Long codigoGeneradoResultado) {
		this.codigoGeneradoResultado = codigoGeneradoResultado;
	}

	public void setCodigoGeneradoContactado(Long codigoGeneradoContactado) {
		this.codigoGeneradoContactado = codigoGeneradoContactado;
	}

}