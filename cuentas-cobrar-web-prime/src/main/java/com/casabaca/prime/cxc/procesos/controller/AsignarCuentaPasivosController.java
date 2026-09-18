/* 
 * asignarCuentaPasivosController.java 
 * 30 mar. 2020
 * Copyright 2020 CASABACA.
 * Todos los derechos reservados.
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.model.UploadedFile;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.dto.DatosInputDiarioA;
import com.casabaca.common.ejb.dto.DeudaClientesDto;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.model.ConfirmarDeposito;
import com.casabaca.common.ejb.model.LineaNegocio;
import com.casabaca.common.ejb.service.ArccmdServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.service.CommonNativeServiceLocal;
import com.casabaca.common.ejb.service.ConfirmarDepositoServiceLocal;
import com.casabaca.common.ejb.service.LineaNegocioServicioLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.procesos.datamanager.AsignarCuentaPasivosDataManager;

/**
 * <b> Clase controladora que administra todo el proceso de <br>
 * creacion de diarios A con depositos de meses anteriores . </b>
 * 
 * @author Jorge Lucas
 * @version $1.0$
 */
@ViewScoped
@ManagedBean(name = "asignarCuentaPasivosController")
public class AsignarCuentaPasivosController extends CommonController implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7987805207613296209L;
	private static final String TIPO_DOC = "V1";
	private static final String CONCEPTO = "01";
	private static final String COMENTARIO = "DIARIO GENERADO POR CRUCE DE DEPOSITOS MES ANTERIORES";

	/**
	 * Log de sistema
	 */
	static Logger logger = Logger.getLogger(AsignarCuentaPasivosController.class);

	/**
	 * Variables para servicios
	 */
	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;

	@EJB(lookup = NombreJNDI.CONFIRMAR_DEPOSITO_SERVICE)
	private ConfirmarDepositoServiceLocal confirmarDepositoService;

	@EJB(lookup = NombreJNDI.ARCCMD_SERVICE)
	private ArccmdServiceLocal arccmdService;

	@EJB(lookup = NombreJNDI.LINEA_NEGOCIO_SERVICIO)
	private LineaNegocioServicioLocal lineaNegocioServicioLocal;

	@EJB(lookup = NombreJNDI.COMMON_NATIVE_SERVICE_LOCAL)
	CommonNativeServiceLocal commonNativeService;

	/**
	 * Data manager
	 */
	@ManagedProperty("#{asignarCuentaPasivosDM}")
	private AsignarCuentaPasivosDataManager asignarCuentaPasivosDM;

	private List<DeudaClientesDto> listaDeudasCliente;

	private UploadedFile file;

	private String noDocu;
	
	private Integer contadorSaldo = 0;

	@PostConstruct
	public void init() {
		listaDeudasCliente = new ArrayList<>();
	}

	/**
	 * <b> Metodo para llenar el listado de lineas de negocio que aplican anticipo, <br>
	 * retorna el lleno el select item para el combo . </b>
	 * <p>
	 * [Author Jorge Lucas, 9 abr. 2020]
	 * </p>
	 *
	 * @return
	 */
	public List<SelectItem> getLineaNegocio() {
		List<LineaNegocio> lineaNegocioList = lineaNegocioServicioLocal
				.obtenerLineasAplicaAnticipos(getCompania().getNoCia());
		getAsignarCuentaPasivosDM().setLineaNegocioList(lineaNegocioList);

		List<SelectItem> items = new ArrayList<SelectItem>();
		lineaNegocioList.forEach((lineas) -> {
			items.add(new SelectItem(lineas.getLineaNegocioPK().getNoLinea(), lineas.getDescripcion()));
		});
		return items;
	}

	/**
	 * <b> Metodo que se ejecuta para cargar el cliente que es buscado <br>
	 * con criterio de busqueda de cedula o nombre. </b>
	 * <p>
	 * [Author Jorge Lucas, 9 abr. 2020]
	 * </p>
	 */
	public void cargarClientes() {
		try {
			List<Cliente> listaClientes = clienteService.buscarClientesPorParametros(0, 25, getCompania().getNoCia(),
					getAsignarCuentaPasivosDM().getCedulaCliente(), getAsignarCuentaPasivosDM().getNombreCliente());
			getAsignarCuentaPasivosDM().setListaClientes(listaClientes);
			limpiar();
		} catch (Exception e) {
			limpiar();
			super.error("No existen registros con los datos consultados ");
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * <b> Metodo que se ejecuta cunado se selecciona un cliente del panel de busqueda de cleintes. </b>
	 * <p>
	 * [Author Jorge Lucas, 9 abr. 2020]
	 * </p>
	 *
	 * @param event
	 */
	public void seleccionarFilaCliente(SelectEvent event) {
		Cliente cliente = (Cliente) event.getObject();
		getAsignarCuentaPasivosDM().setClienteSeleccionado(cliente);
		getAsignarCuentaPasivosDM().setCodigoCliente(cliente.getClientePK().getNoCliente());
		getAsignarCuentaPasivosDM().setCedulaCliente(cliente.getCedula());
		getAsignarCuentaPasivosDM().setNombreCliente(cliente.getNombre());
		getAsignarCuentaPasivosDM().setNombreComercial(cliente.getNombreComercial());
		if (cliente.getFacturar() != null && cliente.getFacturar().equals("S")) {
			getAsignarCuentaPasivosDM().setEsEmisorTarjeta(Boolean.TRUE);
		} else {
			getAsignarCuentaPasivosDM().setEsEmisorTarjeta(Boolean.FALSE);
			cargarListas(cliente);
		}
	}

	/**
	 * <b> Metodo invoca los metodos de consulta de depositos y deudas que tenga el cliente. </b>
	 * <p>
	 * [Author Jorge Lucas, 30 mar. 2020]
	 * </p>
	 */
	public void cargarListas(Cliente cliente) {
		List<ConfirmarDeposito> listaDepositos = cargarListaDepositosPendientes(cliente);
		getAsignarCuentaPasivosDM().setListaDepositosCliente(listaDepositos);

		List<DeudaClientesDto> listaDeudas = cargarListaDeudasPendientes(cliente, null);
		getAsignarCuentaPasivosDM().setListaDeudasCliente(listaDeudas);
	}

	/**
	 * 
	 * <b> Metodo que ejecuta la carga del archivo excel. </b>
	 * <p>
	 * [Author Jorge Lucas, 26 abr. 2020]
	 * </p>
	 *
	 */
	public void cargaArchivo() {
		if (file != null && file.getSize() > 0L) {
			try {
				List<Object[]> dataExcel = super.leerExcel(this.file.getInputstream(), Boolean.TRUE);
				Map<String, String> mapaValores = new HashMap<>();
				StringBuilder sql = new StringBuilder();
				sql.append(" and a.no_docu IN ('");
				for (Object[] objects : dataExcel) {
					mapaValores.put(String.valueOf(objects[7]), String.valueOf(objects[24]));
					sql.append(objects[7]).append("','");
				}
				sql.delete(sql.length() - 2, sql.length());
				sql.append(") ");

				List<ConfirmarDeposito> listaDepositos = cargarListaDepositosPendientes(
						getAsignarCuentaPasivosDM().getClienteSeleccionado());
				getAsignarCuentaPasivosDM().setListaDepositosCliente(listaDepositos);

				List<DeudaClientesDto> listaDeudas = cargarListaDeudasPendientes(
						getAsignarCuentaPasivosDM().getClienteSeleccionado(), sql.toString());
				listaDeudas.stream().forEach(deuda -> {
					String valor = deuda.getNoDocu() == null ? "0.00" : mapaValores.get(deuda.getNoDocu());
					deuda.setValorTarjetas(new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).doubleValue());
				});
				getAsignarCuentaPasivosDM().setListaDeudasCliente(listaDeudas);
				getAsignarCuentaPasivosDM().setDeudasSeleccionadas(listaDeudas);
				sumarSaldoDeudas();
			} catch (IOException e) {
				super.error("Existe inconvenientes en la carga del archivo.");
				logger.error(e.getMessage(), e);
			}
		} else {
			getAsignarCuentaPasivosDM().setListaDepositosCliente(null);
			getAsignarCuentaPasivosDM().setDepositosSelecionados(null);
			getAsignarCuentaPasivosDM().setListaDeudasCliente(null);
			getAsignarCuentaPasivosDM().setDeudasSeleccionadas(null);
			super.error("Favor seleccione un archivo...");
		}
	}
	
	public String colorCelda(DeudaClientesDto deuda) {
		if (deuda != null) {
			if (getAsignarCuentaPasivosDM().getEsEmisorTarjeta()) {
				if (new BigDecimal(deuda.getValorTarjetas()).setScale(2, RoundingMode.HALF_UP)
						.doubleValue() <= new BigDecimal(deuda.getSaldo()).setScale(2, RoundingMode.HALF_UP)
								.doubleValue()) {
					return "#97F663";
				} else {
					return "#ff99cc";
				}
			} else {
				return "#ffffff";
			}
		} else {
			return "#ffffff";
		}
	}

	/**
	 * <b> Metodo que ejecuta la consulta de los depositos del cliente <br>
	 * que esten pendientes o q no son usados. </b>
	 * <p>
	 * [Author Jorge Lucas, 30 mar. 2020]
	 * </p>
	 *
	 * @param cliente
	 * @return
	 */
	private List<ConfirmarDeposito> cargarListaDepositosPendientes(Cliente cliente) {
		List<ConfirmarDeposito> confirmarDepLista = new ArrayList<>();
		try {
			return confirmarDepLista = confirmarDepositoService.getListaDepositoNoConfirmados(getCompania().getNoCia(),
					cliente.getClientePK().getNoCliente().intValue());
		} catch (FindException e) {
			logger.error(e.getMessage(), e);
		}
		return confirmarDepLista;
	}

	/**
	 * <b> Metodo que ejecuta la consulta de deudas que tenga el cliente. </b>
	 * <p>
	 * [Author Jorge Lucas, 31 mar. 2020]
	 * </p>
	 *
	 * @param clienteSeleccionado
	 * @return
	 */
	private List<DeudaClientesDto> cargarListaDeudasPendientes(Cliente clienteSeleccionado, String filtroNodocu) {
		List<DeudaClientesDto> deudasLista = new ArrayList<>();
		try {
			return deudasLista = arccmdService.consultarDeudasCliente(getCompania().getNoCia(),
					clienteSeleccionado.getClientePK().getNoCliente().intValue(), filtroNodocu);
		} catch (FindException e) {
			logger.error(e.getMessage(), e);
		}
		return deudasLista;
	}

	/**
	 * <b> Metodo que se ejecuta cuando seleccion un item de la lista de depositos. </b>
	 * <p>
	 * [Author Jorge Lucas, 1 abr. 2020]
	 * </p>
	 */
	public void rowSelectDeposito() {
		sumarSaldoDisponible();
	}

	/**
	 * <b> Metodo que se ejecuta cuando se quita el check de un item de la lista de depositos. </b>
	 * <p>
	 * [Author Jorge Lucas, 1 abr. 2020]
	 * </p>
	 */
	public void rowUnSelectDeposito() {
		sumarSaldoDisponible();
	}

	/**
	 * <b> Metodo que se ejecuta cuando seleccion todos los item de la lista de depositos. </b>
	 * <p>
	 * [Author Jorge Lucas, 1 abr. 2020]
	 * </p>
	 */
	public void toggleSelectedDeposito(ToggleSelectEvent tse) {
		if (tse.isSelected()) {
			sumarSaldoDisponible();
		} else {
			getAsignarCuentaPasivosDM().setSaldoDisponible(0D);
			getAsignarCuentaPasivosDM().setValorAnticipo(0D);
			getAsignarCuentaPasivosDM().setHabilitarBoton(Boolean.TRUE);
		}
	}

	/**
	 * <b> Metodo que se ejecuta al momento de digitar un valor en las celdas del listado de deudas. </b>
	 * <p>
	 * [Author Jorge Lucas, 12 abr. 2020]
	 * </p>
	 *
	 * @param objetoFila
	 */
	public void agregarDeuda(DeudaClientesDto objetoFila) {
		if (getAsignarCuentaPasivosDM().getDepositosSelecionados() == null) {
			super.error("Seleccione al menos un deposito...");
			objetoFila.setValor(0D);
			getAsignarCuentaPasivosDM().setDeudasSeleccionadas(listaDeudasCliente);
			sumarSaldoDeudas();
			return;
		}

		if (objetoFila.getValor() > objetoFila.getSaldo()) {
			super.error("EL valor digitado : " + objetoFila.getValor() + " es mayor al valor a cancelar : "
					+ objetoFila.getSaldo());
			objetoFila.setValor(0D);
			getAsignarCuentaPasivosDM().setDeudasSeleccionadas(listaDeudasCliente);
			sumarSaldoDeudas();
			return;
		}

		if (objetoFila.getValor() > 0D) {
			if (listaDeudasCliente.isEmpty()) {
				listaDeudasCliente.add(objetoFila);
			} else {
				listaDeudasCliente.removeIf(p -> p.getNoFisico() == objetoFila.getNoFisico());
				listaDeudasCliente.add(objetoFila);
			}
		} else {
			listaDeudasCliente.removeIf(p -> p.getNoFisico() == objetoFila.getNoFisico());
		}
		getAsignarCuentaPasivosDM().setDeudasSeleccionadas(listaDeudasCliente);
		sumarSaldoDeudas();

		if (getAsignarCuentaPasivosDM().getSaldoDisponible() < getAsignarCuentaPasivosDM().getSaldoDeudas()) {
			super.error("El valor de la deuda sobrepasa al saldo disponible ");
			objetoFila.setValor(0D);
			listaDeudasCliente.removeIf(p -> p.getNoFisico() == objetoFila.getNoFisico());
			getAsignarCuentaPasivosDM().setDeudasSeleccionadas(listaDeudasCliente);
			sumarSaldoDeudas();
		}
	}
	
	public void sumarDeudas() {
		List<DeudaClientesDto> listaDeudas = getAsignarCuentaPasivosDM().getListaDeudasCliente();
		getAsignarCuentaPasivosDM().setListaDeudasCliente(listaDeudas);
		getAsignarCuentaPasivosDM().setDeudasSeleccionadas(listaDeudas);
		sumarSaldoDeudas();
	}

	/**
	 * <b> Metodo para sumar el valor total de todos los depositos. </b>
	 * <p>
	 * [Author Jorge Lucas, 1 abr. 2020]
	 * </p>
	 */
	private void sumarSaldoDisponible() {
		List<ConfirmarDeposito> depositosSeleccionados = getAsignarCuentaPasivosDM().getDepositosSelecionados();
		getAsignarCuentaPasivosDM().setSaldoDisponible(0D);
		getAsignarCuentaPasivosDM().setValorAnticipo(0D);
		Double sumaDepositos = new Double(0D);
		for (ConfirmarDeposito confirmarDeposito : depositosSeleccionados) {
			sumaDepositos += new BigDecimal(confirmarDeposito.getValor()).setScale(2, RoundingMode.HALF_UP).doubleValue();
		}
		getAsignarCuentaPasivosDM().setSaldoDisponible(new BigDecimal(sumaDepositos).setScale(2, RoundingMode.HALF_UP).doubleValue());
		getAsignarCuentaPasivosDM().setHabilitarBoton(Boolean.FALSE);
		if (sumaDepositos == 0D) {
			getAsignarCuentaPasivosDM().setHabilitarBoton(Boolean.TRUE);
		}
		obtenerValorAnticipo();
	}
	
	public void consultarCliente() {
		Cliente cliente;
		try {
			cliente = clienteService.buscarPorCedulaNoCia(getAsignarCuentaPasivosDM().getCedulaCliente(),
					getCompania().getNoCia());
			System.out.println(">>>>>>>Cliente "+cliente.getNombre());
			getAsignarCuentaPasivosDM().setClienteSeleccionado(cliente);
			getAsignarCuentaPasivosDM().setCodigoCliente(cliente.getClientePK().getNoCliente());
			getAsignarCuentaPasivosDM().setCedulaCliente(cliente.getCedula());
			getAsignarCuentaPasivosDM().setNombreCliente(cliente.getNombre());
			getAsignarCuentaPasivosDM().setNombreComercial(cliente.getNombreComercial());
			if (cliente.getFacturar() != null && cliente.getFacturar().equals("S")) {
				getAsignarCuentaPasivosDM().setEsEmisorTarjeta(Boolean.TRUE);
			} else {
				getAsignarCuentaPasivosDM().setEsEmisorTarjeta(Boolean.FALSE);
				cargarListas(cliente);
			}
			System.out.println(">>>>>>>Cliente "+cliente.getNombre());
		} catch (FindException e) {
			error("No existe un cliente con la cedula ingresada");
		}
	}

	/**
	 * <b> Metodo para sumar el valor total de todos las deudas. </b>
	 * <p>
	 * [Author Jorge Lucas, 1 abr. 2020]
	 * </p>
	 */
	private void sumarSaldoDeudas() {
		getAsignarCuentaPasivosDM().setSaldoDeudas(0D);
		getAsignarCuentaPasivosDM().setValorAnticipo(0D);
		Double sumaDeudas = new Double(0D);
		for (DeudaClientesDto deudaClientesDto : getAsignarCuentaPasivosDM().getDeudasSeleccionadas()) {
			if(getAsignarCuentaPasivosDM().getEsEmisorTarjeta()) {
				sumaDeudas += new BigDecimal(deudaClientesDto.getValorTarjetas()).setScale(2, RoundingMode.HALF_UP).doubleValue();
			}else {
				sumaDeudas += new BigDecimal(deudaClientesDto.getValor()).setScale(2, RoundingMode.HALF_UP).doubleValue();
			}
		}
		getAsignarCuentaPasivosDM().setSaldoDeudas(new BigDecimal(sumaDeudas).setScale(2, RoundingMode.HALF_UP).doubleValue());
		getAsignarCuentaPasivosDM().setHabilitarBoton(Boolean.FALSE);
		if (getAsignarCuentaPasivosDM().getSaldoDisponible() == 0D && sumaDeudas > 0) {
			getAsignarCuentaPasivosDM().setHabilitarBoton(Boolean.TRUE);
		}
		obtenerValorAnticipo();
	}

	/**
	 * <b> Metodo que suma los totates de depositos, deudas para obtener el valor total que sera el valor del anticipo.
	 * </b>
	 * <p>
	 * [Author Jorge Lucas, 1 abr. 2020]
	 * </p>
	 */
	private void obtenerValorAnticipo() {
		Double valorAnticipo = 0D;
		valorAnticipo = getAsignarCuentaPasivosDM().getSaldoDisponible() - getAsignarCuentaPasivosDM().getSaldoDeudas();
		getAsignarCuentaPasivosDM().setValorAnticipo(valorAnticipo);
		getAsignarCuentaPasivosDM().setHabilitarBoton(Boolean.FALSE);
		getAsignarCuentaPasivosDM().setHabilitarComboLinea(Boolean.FALSE);
		if (valorAnticipo <= 0D) {
			getAsignarCuentaPasivosDM().setLineaSeleccionada(CommonConstants.VACIO);
			getAsignarCuentaPasivosDM().setHabilitarComboLinea(Boolean.TRUE);
		}
	}

	/**
	 * <b>Metodo que se ejecuta desde el boton procesar. </b>
	 * <p>
	 * [Author Jorge Lucas, 9 abr. 2020]
	 * </p>
	 */
	public void procesar() {
		if (getAsignarCuentaPasivosDM().getValorAnticipo() < 0D) {
			super.error("El valor del anticipo debe ser mayor a Cero");
			return;
		}
		
		if ((null == getAsignarCuentaPasivosDM().getLineaSeleccionada()
				|| CommonConstants.VACIO.equals(getAsignarCuentaPasivosDM().getLineaSeleccionada()))
				&& getAsignarCuentaPasivosDM().getValorAnticipo() > 0D) {
			super.error("Por favor seleccione una linea de Negocio");
			return;
		}
		
		if (getAsignarCuentaPasivosDM().getSaldoDisponible() < getAsignarCuentaPasivosDM().getSaldoDeudas()) {
			super.error("El valor de la deuda sobrepasa al saldo disponible ");
			return;
		}
		
		StringBuilder mensajeSaldo = new StringBuilder();
		getAsignarCuentaPasivosDM().getDeudasSeleccionadas().forEach(deuda -> {
			if(getAsignarCuentaPasivosDM().getEsEmisorTarjeta()) {
				if(deuda.getValorTarjetas() > deuda.getSaldo()) {
					mensajeSaldo.append("El valor [");
					mensajeSaldo.append(deuda.getValorTarjetas());
					mensajeSaldo.append("], es mayor al saldo [");
					mensajeSaldo.append(deuda.getSaldo());
					mensajeSaldo.append("]");
					mensajeSaldo.append("\n");
					contadorSaldo++;
				}
			}else {
				if(deuda.getValor() > deuda.getSaldo()) {
					mensajeSaldo.append("El valor [");
					mensajeSaldo.append(deuda.getValor());
					mensajeSaldo.append("], es mayor al saldo [");
					mensajeSaldo.append(deuda.getSaldo());
					mensajeSaldo.append("]");
					mensajeSaldo.append("\n");
					contadorSaldo++;
				}
			}
		});
		
		if (contadorSaldo > 0) {
			super.error(mensajeSaldo.toString());
			return;
		}
		

		String cuentaLinea = "";
		if (getAsignarCuentaPasivosDM().getValorAnticipo() > 0D) {
			for (LineaNegocio item : getAsignarCuentaPasivosDM().getLineaNegocioList()) {
				if (getAsignarCuentaPasivosDM().getLineaSeleccionada().equals(item.getLineaNegocioPK().getNoLinea())) {
					cuentaLinea = item.getCuentaAnticiposScb();
					break;
				}
			}
		}

		try {
			String xmlDepositos = xmlListaDepositos();
			String xmlDeudas = getAsignarCuentaPasivosDM().getSaldoDeudas() > 0D ? xmlListaDeudas()
					: CommonConstants.VACIO;
			logger.error("xmlDepositos : " + xmlDepositos);
			logger.error("xmlDeudas : " + xmlDeudas);
			
			String resultado = commonNativeService.generarDiariosADepositos(construirInputDiarioA(cuentaLinea, xmlDepositos, xmlDeudas));
			super.info("El proceso fue exitoso, numero de documento generado : " + resultado);
			limpiar();
			getAsignarCuentaPasivosDM().setListaClientes(null);
		} catch (InsertException e) {
			limpiar();
			super.error(e.getDetail());
			logger.error(e.getDetail());
		} catch (Exception e) {
			limpiar();
			super.error("El proceso no pudo ejecutarse. Por favor intente mas tarde.");
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * Construye el input para procesar el diario A
	 * @param cuentaLinea
	 * @param xmlDepositos
	 * @param xmlDeudas
	 * @return
	 */
	private DatosInputDiarioA construirInputDiarioA(String cuentaLinea, String xmlDepositos, String xmlDeudas) {
		DatosInputDiarioA inputDiarioA = new DatosInputDiarioA();
		inputDiarioA.setNoCia(getCompania().getNoCia());
		inputDiarioA.setAgencia(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
		inputDiarioA.setUsuario(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
		inputDiarioA.setNoCliente(getAsignarCuentaPasivosDM().getCodigoCliente().intValue());
		inputDiarioA.setSaldoDisponible(getAsignarCuentaPasivosDM().getSaldoDisponible());
		inputDiarioA.setSumaDeuda(getAsignarCuentaPasivosDM().getSaldoDeudas());
		inputDiarioA.setSumaAnticipo(getAsignarCuentaPasivosDM().getValorAnticipo());
		inputDiarioA.setLineaNegocio(getAsignarCuentaPasivosDM().getLineaSeleccionada());
		inputDiarioA.setCuentaLinea(cuentaLinea);
		inputDiarioA.setListaDeposito(xmlDepositos);
		inputDiarioA.setListaDeuda(xmlDeudas);
		inputDiarioA.setListaCuentasAnt(CommonConstants.VACIO);
		inputDiarioA.setTipoCruce("D");
		inputDiarioA.setListaAnticipos(CommonConstants.VACIO);
		inputDiarioA.setTipoDoc(TIPO_DOC);
		inputDiarioA.setConcepto(CONCEPTO);
		inputDiarioA.setComentario(COMENTARIO);
		inputDiarioA.setEsAntConCuentas(Boolean.FALSE);
		return inputDiarioA;
	}

	/**
	 * <b> Metodo que forma la estructura xml de listado de depositos seleccionada en la lista. </b>
	 * <p>
	 * [Author Jorge Lucas, 9 abr. 2020]
	 * </p>
	 *
	 * @return
	 */
	private String xmlListaDepositos() {
		StringBuilder depositoSeleccionado = new StringBuilder();
		depositoSeleccionado.append("<depositos>");
		getAsignarCuentaPasivosDM().getDepositosSelecionados().forEach(deposito -> {
			depositoSeleccionado.append("<deposito>");
			depositoSeleccionado.append("<noCia>");
			depositoSeleccionado.append(deposito.getId().getNoCia());
			depositoSeleccionado.append("</noCia>");
			depositoSeleccionado.append("<banco>");
			depositoSeleccionado.append(deposito.getId().getBanco());
			depositoSeleccionado.append("</banco>");
			depositoSeleccionado.append("<noCta>");
			depositoSeleccionado.append(deposito.getId().getNoCta());
			depositoSeleccionado.append("</noCta>");
			depositoSeleccionado.append("<fechaDeposito>");
			depositoSeleccionado.append(FechaUtils.formatearFecha(deposito.getId().getFechaDeposito(), "yyyy-MM-dd"));
			depositoSeleccionado.append("</fechaDeposito>");
			depositoSeleccionado.append("<noFisico>");
			depositoSeleccionado.append(deposito.getId().getNoFisico());
			depositoSeleccionado.append("</noFisico>");
			depositoSeleccionado.append("<valor>");
			depositoSeleccionado.append(deposito.getValor());
			depositoSeleccionado.append("</valor>");
			depositoSeleccionado.append("</deposito>");
		});
		depositoSeleccionado.append("</depositos>");
		return depositoSeleccionado.toString();
	}

	/**
	 * <b> Metodo que forma la estructura xml de listado de deudas selecionadas en la lista. </b>
	 * <p>
	 * [Author Jorge Lucas, 9 abr. 2020]
	 * </p>
	 *
	 * @return
	 */
	private String xmlListaDeudas() {
		StringBuilder deudaSeleccionada = new StringBuilder();
		deudaSeleccionada.append("<deudas>");
		getAsignarCuentaPasivosDM().getDeudasSeleccionadas().forEach(deuda -> {
			deudaSeleccionada.append("<deuda>");
			deudaSeleccionada.append("<noCia>");
			deudaSeleccionada.append(getCompania().getNoCia());
			deudaSeleccionada.append("</noCia>");
			deudaSeleccionada.append("<noDocu>");
			deudaSeleccionada.append(deuda.getNoDocu());
			deudaSeleccionada.append("</noDocu>");
			deudaSeleccionada.append("<valor>");
			if(getAsignarCuentaPasivosDM().getEsEmisorTarjeta()) {
				deudaSeleccionada.append(deuda.getValorTarjetas());
			}else {
				deudaSeleccionada.append(deuda.getValor());
			}
			deudaSeleccionada.append("</valor>");
			deudaSeleccionada.append("<cuentaAnticipo>");
			deudaSeleccionada.append(deuda.getCuentaClientesScb());
			deudaSeleccionada.append("</cuentaAnticipo>");
			deudaSeleccionada.append("<letra>");
			deudaSeleccionada.append(deuda.getLetra());
			deudaSeleccionada.append("</letra>");
			deudaSeleccionada.append("<descuento>");
			deudaSeleccionada.append(0);
			deudaSeleccionada.append("</descuento>");
			deudaSeleccionada.append("<interes>");
			deudaSeleccionada.append(0);
			deudaSeleccionada.append("</interes>");
			deudaSeleccionada.append("<comisionMora>");
			deudaSeleccionada.append(0);
			deudaSeleccionada.append("</comisionMora>");
			deudaSeleccionada.append("</deuda>");
		});
		deudaSeleccionada.append("</deudas>");
		return deudaSeleccionada.toString();
	}

	/**
	 * <b> Metodo para limpiar las cajas de texto cedula y nombre. </b>
	 * <p>
	 * [Author Jorge Lucas, 30 mar. 2020]
	 * </p>
	 */
	public void limpiar() {
		getAsignarCuentaPasivosDM().setCodigoCliente(null);
		getAsignarCuentaPasivosDM().setCedulaCliente(CommonConstants.VACIO);
		getAsignarCuentaPasivosDM().setNombreCliente(CommonConstants.VACIO);
		getAsignarCuentaPasivosDM().setNombreComercial(CommonConstants.VACIO);
		getAsignarCuentaPasivosDM().setListaDepositosCliente(null);
		getAsignarCuentaPasivosDM().setDepositosSelecionados(null);
		getAsignarCuentaPasivosDM().setListaDeudasCliente(null);
		getAsignarCuentaPasivosDM().setDeudasSeleccionadas(null);
		getAsignarCuentaPasivosDM().setSaldoDisponible(0D);
		getAsignarCuentaPasivosDM().setSaldoDeudas(0D);
		getAsignarCuentaPasivosDM().setValorAnticipo(0D);
		getAsignarCuentaPasivosDM().setLineaSeleccionada(CommonConstants.VACIO);
		getAsignarCuentaPasivosDM().setHabilitarBoton(Boolean.TRUE);
		getAsignarCuentaPasivosDM().setHabilitarComboLinea(Boolean.TRUE);
		listaDeudasCliente = new ArrayList<>();
	}

	/**
	 * @return the asignarCuentaPasivosDM
	 */
	public AsignarCuentaPasivosDataManager getAsignarCuentaPasivosDM() {
		return asignarCuentaPasivosDM;
	}

	/**
	 * @param asignarCuentaPasivosDM the asignarCuentaPasivosDM to set
	 */
	public void setAsignarCuentaPasivosDM(AsignarCuentaPasivosDataManager asignarCuentaPasivosDM) {
		this.asignarCuentaPasivosDM = asignarCuentaPasivosDM;
	}

	/**
	 * @return the file
	 */
	public UploadedFile getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(UploadedFile file) {
		this.file = file;
	}

	/**
	 * @return the noDocu
	 */
	public String getNoDocu() {
		return noDocu;
	}

	/**
	 * @param noDocu the noDocu to set
	 */
	public void setNoDocu(String noDocu) {
		this.noDocu = noDocu;
	}
}
