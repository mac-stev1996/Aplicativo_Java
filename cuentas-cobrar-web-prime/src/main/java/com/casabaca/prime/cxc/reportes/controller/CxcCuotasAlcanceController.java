package com.casabaca.prime.cxc.reportes.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.ExceptionUtils;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.FindException;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.prime.cxc.common.lazy.LazyClientesDataModel;
import com.casabaca.s3s.ejb.model.Agencia;
import com.casabaca.s3s.ejb.service.AgenciaServiceLocal;
import com.casabaca.vehiculos.ejb.modelo.TipoFinanciamiento;
import com.casabaca.vehiculos.ejb.servicio.TipoFinanciamientoServicioLocal;

@ViewScoped
@ManagedBean
public class CxcCuotasAlcanceController extends ReportCommonController {
	
	static Logger logger = Logger.getLogger(CxcCuotasAlcanceController.class);
	private static final String REPORTE_COUTA_ALCANCE = "/cxc/cxcCuotasAlcance";
	
	@EJB(lookup = NombreJNDI.AGENCIA_SERVICE_BEAN)
	private AgenciaServiceLocal agenciaService;
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceLocal;
	@EJB(lookup = NombreJNDI.TIPO_FINANCIAMIENTO_SERVICIO_BEAN)
	private TipoFinanciamientoServicioLocal tipoFinanciamientoService;

	private List<Agencia> agencias;
	private String chasis;
	private LazyDataModel<Cliente> lazyModel;
	private String cedulaClienteConsulta;
	private String nombreClienteConsulta;
	private List<TipoFinanciamiento> tipoFinanciamientoList;
	private Cliente cliente;
	private List<String> tiposFinanciamientoB;
	private boolean sinDetalle;

	@PostConstruct
	public void init() {
		logger.info("init...");
		cargaAgencias();
		cargaTipoFinanciamiento();
		cliente = new Cliente();
		sinDetalle = true;
	}

	private void cargaTipoFinanciamiento() {
		try {
			tipoFinanciamientoList = tipoFinanciamientoService.findByTipoFinanciaXnoCia(getCompania().getNoCia());
		} catch (FindException e) {
			logger.error("cargaTipoFinanciamiento:", e);
		}		
	}

	private void cargaAgencias() {
		String[] order = { "nombre" };
		agencias = agenciaService.findByCompania(getCompania().getNoCia(), order);
	}

	public void report() {
		logger.info("report..."+getFechaDesde());
		try {
			if(getFechaDesde() == null || getFechaHasta() == null) {
				addErrorMessage("","Ingrese la fecha desde y fecha hasta ");
				return;
			}
			if(getFechaHasta() != null && getFechaHasta().before(getFechaDesde())) {
				addErrorMessage("", "La fecha Hasta no puede ser menor a la fecha Desde, favor revise.");
				return;
			}
			Date fechaActual = FechaUtils.calendarioAnoMesDia(new Date()).getTime();//Sin horas, minutos y segundos
			if(getFechaDesde() != null && getFechaDesde().after(fechaActual)) {
				addErrorMessage("", "La fecha Desde no puede ser mayor a la actual, favor revise");
				return;
			}
			if(getFechaHasta() != null && getFechaHasta().after(fechaActual)) {
				addErrorMessage("", "La fecha Hasta no puede ser mayor a la actual, favor revise");
				return;
			}
				
			String format = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
					.get("format");
			Map<String, Object> parameters = new HashMap<>();
			setParameters(parameters, "excel".equals(format)?"N":"S");
			FacesContext.getCurrentInstance().getExternalContext()
				.redirect(super.callJasperReport(REPORTE_COUTA_ALCANCE+getCompania().getNoCia(), parameters, 
						"excel".equals(format)?CommonConstants.OUTPUT_XLSX:CommonConstants.OUTPUT_PDF));
		}catch (Exception e) {
			logger.error("Error reporte: "+ ExceptionUtils.obtainException(e));
		}
	}

	private void setParameters(Map<String, Object> parameters, String cabecera) {

		parameters.put("NO_CIA", getCompania().getNoCia());
		parameters.put("NOMBRE_CIA", getCompania().getNombre());
		parameters.put("P_IMPRIMIR_CABECERA", cabecera);
		if(StringUtils.isNotBlank(getAgencia())) {
			parameters.put("COD_AGENCIA", getAgencia());
		}
		if(StringUtils.isNotBlank(getChasis())) {
			parameters.put("P_CHASIS", getChasis());
		}else {
			parameters.put("P_CHASIS","%");
		}
		if(getTiposFinanciamientoB() != null 
			&& !getTiposFinanciamientoB().isEmpty()) {
			StringBuilder condicionLinea = formarCondicionReporte(getTiposFinanciamientoB(), "and", "f.linea", true);
			parameters.put("P_TIPO_FINANCIAMIENTO", condicionLinea);
		}
		if(getFechaDesde() !=null && getFechaHasta() != null) {
			parameters.put("P_FECHA_INICIO", getFechaDesde());
			parameters.put("P_FECHA_FIN", getFechaHasta());
		}
		if (getCliente() != null && getCliente().getClientePK() != null && cliente.getClientePK().getNoCliente() != null) {
			parameters.put("COD_CLIENTE", cliente.getClientePK().getNoCliente());
		}
		parameters.put("CON_DETALLE", isSinDetalle()?"N":"S");
	}
	
	public void ejecutarConsultaClientes() {
		logger.info("ejecutarConsultaClientes...");
		lazyModel = new LazyClientesDataModel(clienteServiceLocal, getCompania().getNoCia(),
											  cedulaClienteConsulta,
											  nombreClienteConsulta);
	}
	
	public void seleccionarCliente(Cliente cliente) {
		logger.info("seleccionarCliente...");
		this.cliente = cliente;
	}
	
	public void consultaCliente() {
		logger.info("consultaCliente...");
		try {
			if(StringUtils.isNotBlank(cliente.getCedula())) {
				cliente = clienteServiceLocal.findByCedulaNoCia(cliente.getCedula(), getCompania().getNoCia());
			}else {
				cliente = new Cliente();
			}
		}catch (Exception e) {
			logger.error("consultaCliente",e);
			cliente = new Cliente();
		}
	}
	public StringBuilder formarCondicionReporte(List<String> valores, String operador, String propiedad, boolean incluye) {
		StringBuilder condicion = null;
		if(valores.size() == 1) {
			condicion = new StringBuilder(operador+" "+propiedad+(incluye?" =":" <>")+" '"
							+valores.get(0)+"'");
		}else{
			int i = 0;
			condicion = new StringBuilder(operador+" "+propiedad+(incluye?" in":" not in")+" (");
			for (String valor : valores) {
				if(i > 0) {
					condicion.append(',');
				}
				condicion.append("'"+valor+"'");
				i++;					
			}
			condicion.append(") ");
		}
		return condicion;
	}

	public List<Agencia> getAgencias() {
		return agencias;
	}

	public void setAgencias(List<Agencia> agencias) {
		this.agencias = agencias;
	}

	public String getChasis() {
		return chasis;
	}

	public void setChasis(String chasis) {
		this.chasis = chasis;
	}

	public LazyDataModel<Cliente> getLazyModel() {
		return lazyModel;
	}

	public void setLazyModel(LazyDataModel<Cliente> lazyModel) {
		this.lazyModel = lazyModel;
	}

	public String getCedulaClienteConsulta() {
		return cedulaClienteConsulta;
	}

	public void setCedulaClienteConsulta(String cedulaClienteConsulta) {
		this.cedulaClienteConsulta = cedulaClienteConsulta;
	}

	public String getNombreClienteConsulta() {
		return nombreClienteConsulta;
	}

	public void setNombreClienteConsulta(String nombreClienteConsulta) {
		this.nombreClienteConsulta = nombreClienteConsulta;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public boolean isSinDetalle() {
		return sinDetalle;
	}

	public void setSinDetalle(boolean sinDetalle) {
		this.sinDetalle = sinDetalle;
	}

	public List<TipoFinanciamiento> getTipoFinanciamientoList() {
		return tipoFinanciamientoList;
	}

	public void setTipoFinanciamientoList(List<TipoFinanciamiento> tipoFinanciamientoList) {
		this.tipoFinanciamientoList = tipoFinanciamientoList;
	}

	public List<String> getTiposFinanciamientoB() {
		return tiposFinanciamientoB;
	}

	public void setTiposFinanciamientoB(List<String> tiposFinanciamientoB) {
		this.tiposFinanciamientoB = tiposFinanciamientoB;
	}

}
