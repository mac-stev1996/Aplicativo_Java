/**
 * 
 */
package com.casabaca.prime.cxc.procesos.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.primefaces.model.UploadedFile;

import com.casabaca.caja.ejb.dao.ComprobanteElectronicoDtoDaoLocal;
import com.casabaca.caja.ejb.dto.ComprobanteElectronicoDto;
import com.casabaca.caja.ejb.util.Constantes;
import com.casabaca.common.CommonConstants;
import com.casabaca.common.Duplex;
import com.casabaca.common.ejb.dao.CheConfirmaDepositoDtoDaoLocal;
import com.casabaca.common.ejb.dto.CheConfirmaDepositoDto;
import com.casabaca.common.ejb.dto.DatosBancoDto;
import com.casabaca.common.ejb.model.Arccda;
import com.casabaca.common.ejb.model.Banco;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.BancoServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.TipoLiquidacionEnum;
import com.casabaca.cxc.ejb.dto.CxcLiquidacionTarjetaDto;
import com.casabaca.cxc.ejb.modelo.CxcDetComisionesTc;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiarios;
import com.casabaca.cxc.ejb.modelo.CxcRevisionDiariosPK;
import com.casabaca.cxc.ejb.servicio.ComisionTarjetaCreditoServiceLocal;
import com.casabaca.cxc.ejb.servicio.CxcLiquidacionTarjetasCreditoService;
import com.casabaca.cxc.ejb.servicio.CxcRevisionDiariosServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.GeneralException;
import com.casabaca.prime.cxc.common.CommonController;



@ManagedBean
@ViewScoped
public class CxcLiquidacionTarjetasCreditoController extends CommonController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8135357818132393488L;
	private static final Logger LOG = Logger.getLogger(CxcLiquidacionTarjetasCreditoController.class);

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceDelegate;

	@EJB(lookup = NombreJNDI.CXC_LIQUIDA_TARJETA_CREDITO_BEAN)
	private CxcLiquidacionTarjetasCreditoService liquidacionTarjCreditoSevicio;

	@EJB(lookup = NombreJNDI.BANCO_SERVICE)
	private BancoServiceLocal bancoService;

	@EJB(lookup = Constantes.BEAN_CONFIRMA_DEPOSITO)
	private CheConfirmaDepositoDtoDaoLocal cheConfirmaDepositoDtoDao;

	@EJB(lookup = Constantes.BEAN_COMPROBANTES_ELECTRONICOS)
	private ComprobanteElectronicoDtoDaoLocal comprobanteElectronicoDtoDao;

	@EJB(lookup = com.casabaca.cxc.ejb.util.Constantes.JNDI_SERVICE_COMISIONES_TC)
	private ComisionTarjetaCreditoServiceLocal comisionServiceLocal;
	
	private List<String> listaAdjuntos;

	@EJB(lookup = NombreJNDI.CXC_REVISION_DIARIOS_SERVICE_BEAN)
	private CxcRevisionDiariosServiceLocal revisionServiceLocal;
	
	private TipoLiquidacionEnum[] tiposLiquidacion;
	private TipoLiquidacionEnum tipoLiquidacion;
	private List<Cliente> clientes;
	private List<Banco> bancos;
	private UploadedFile file;
	private Long noCliente;
	private DatosBancoDto datosBanco;
	private List<CxcDetComisionesTc> comisionesTc;
	private List<CheConfirmaDepositoDto> depositos;
	private List<CheConfirmaDepositoDto> filteredDepositos;
	private List<ComprobanteElectronicoDto> comprobantes;
	private List<ComprobanteElectronicoDto> comprobantesSeleccionados;
	private CheConfirmaDepositoDto depositoSeleccionado;
	private CxcDetComisionesTc comisionSeleccionada;
	private List<CxcDetComisionesTc> filteredComisiones;
	private List<ComprobanteElectronicoDto> filteredComprobantes;
	private String nombreDialog;
	private String diarioA;
	private List<String> mensajes;
	private boolean hayDiario;
	private boolean hayMensajes;

	@PostConstruct
	public void init() {
		datosBanco = new DatosBancoDto();
		cargarTarjetasCliente();
		cargarBancosActivos();
	}

	/**
	 * Permite cargar las tarjetas de los clientes por medio del codigo de la
	 * empresa
	 */
	private void cargarTarjetasCliente() {
		if (Objects.isNull(clientes)) {
			System.out.println("es null "+clienteServiceDelegate+"  "+getCompania().getNoCia());
			clientes = clienteServiceDelegate.getClienteTarjetaCredito(getCompania().getNoCia());
		}
	}

	/**
	 * Permite obtener las lista de bancos activos
	 */
	private void cargarBancosActivos() {
		if (Objects.isNull(bancos)) {
			try {
				bancos = bancoService.getListaBanco("A");
			} catch (FindException e) {
				LOG.error("No se pudo obtener la lista de bancos", e);
			}
		}
	}

	public void cargaArchivo() {
		try {
			if (esValidaDatos()) {
				listaAdjuntos = new ArrayList<>();
				List<Object[]> objetos = cargarArchivoExcel(file.getInputstream(), Boolean.TRUE, 0);
				CxcLiquidacionTarjetaDto parametro = new CxcLiquidacionTarjetaDto();
				parametro.setNoCia(getCompania().getNoCia());
				parametro.setUsuarioConectado(getLoggedUsername());
				parametro.setCentroConectado(getUsuarioCentroConectado().getUsuarioCentroPK().getCentro());
				parametro.setDatosExcel(objetos);
				parametro.setTipoLiquidacion(tipoLiquidacion);
				parametro.setDatosBancoDto(datosBanco);
				parametro.setNoCliente(noCliente);
				diarioA = null;
				mensajes = new ArrayList<>();
				Duplex<List<Arccda>,List<String>> resultado= liquidacionTarjCreditoSevicio.liquidarTarjetasCreditoBloque(parametro);
				hayDiario = false;
				hayMensajes = false;
				if(resultado.getPrimerElemento() != null){
					
				for(Arccda darioItem : resultado.getPrimerElemento()){
					procesarArchivo(file.getInputstream(), darioItem.getNoFisico()+"-"+darioItem.getSerieFisico());
					guardarRevision(darioItem);	
					//info("Se ha generado el diario A "+darioItem.getNoFisico());
					diarioA = darioItem.getNoFisico();
					hayDiario=true;
				}}
				if (resultado.getSegundoElemento() == null || resultado.getSegundoElemento().isEmpty()) {
					mensajes = null;
				} else {
					for (String msj : resultado.getSegundoElemento()) {
						hayMensajes = true;
						mensajes.add(msj);
					}
				}
				accionesDialog("DlgResultado", true);
				limpiarDatos();
			}

		} catch (IOException | IllegalArgumentException e) {
			LOG.error("Error al cargar archivo", e);
			error("No se pudo cargar el archivo especificado");
		} catch (GeneralException e) {
			error(e.getDetail());
		} catch (Exception e) {
			LOG.error(e);
			error(super.obtenerCausaException(e));
		}

	}

	
	private void procesarArchivo(InputStream is1, String noFisico) {
		try {
			// Para pruebas en windows
//			String raizDiarios = "C://" + //
//					System.getProperty("file.separator") + "opt" + //
//					System.getProperty("file.separator") + "diariosA" + //
//					System.getProperty("file.separator");

			// Para el servidor Linux
			 String raizDiarios = System.getProperty("file.separator") + "opt"
			 + System.getProperty("file.separator")
			 + "diariosA" + System.getProperty("file.separator");

			File folderGeneral = new File(
					raizDiarios + getCompania().getNoCia() + System.getProperty("file.separator") + "Adjuntos");
			if (!folderGeneral.exists()) {
				folderGeneral.mkdir();
			}
			raizDiarios = raizDiarios + getCompania().getNoCia() + System.getProperty("file.separator") + "Adjuntos"
					+ System.getProperty("file.separator");
			
			File folder = new File(raizDiarios +noFisico );
			if (!folder.exists()) {
				folder.mkdir();
			}
			raizDiarios = raizDiarios + noFisico+ System.getProperty("file.separator");

			File archivo = new File(raizDiarios + "RespaldoCargaExcel.xls");
			copyInputStreamToFile(is1, archivo);

//			archivosAdjuntos.put(uf.getFileName(), raizDiarios + uf.getFileName());
			listaAdjuntos.add(raizDiarios + "RespaldoCargaExcel.xls");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

		try (FileOutputStream outputStream = new FileOutputStream(file)) {

			int read;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			// commons-io
			IOUtils.copy(inputStream, outputStream);
		}
	}
	
	private void guardarRevision(Arccda diario){
		CxcRevisionDiarios rev = new CxcRevisionDiarios();
		rev.setRevisionDiarioPk(new  CxcRevisionDiariosPK(getCompania().getNoCia(), diario.getArccdaPk().getNoDocu()));
		rev.setUsuario(getUsuario().getUsuario());
		rev.setFecha(new Date());
		rev.setRevAprobada("N");
		String adjuntos = new String();
		if (listaAdjuntos != null && !listaAdjuntos.isEmpty()) {
			for (String nombre : listaAdjuntos) {
				
					adjuntos = nombre;
				
			}
		}
		rev.setAdjuntos(adjuntos);
		try {
			
			revisionServiceLocal.crear(rev);
		}catch (Exception e) {
			
			warn("Error al guardar el archivo del diario A");
		}
		
	}

	
	/**
	 * Permite validar los datos de entrada para procesar la peticion
	 * 
	 * @return
	 */
	private boolean esValidaDatos() {
		boolean esValido = Boolean.TRUE;
		if (Objects.isNull(noCliente)) {
			error("Debe seleccionar la tarjeta");
			esValido = Boolean.FALSE;
		} else if (Objects.isNull(tipoLiquidacion)) {
			error("Debe seleccionar el tipo de liquidacion");
			esValido = Boolean.FALSE;
		} else if (Objects.isNull(file) || file.getSize() == 0) {
			error("Debe cargar un archivo para procesar");
			esValido = Boolean.FALSE;
		} else if (Objects.nonNull(tipoLiquidacion)) {
			switch (tipoLiquidacion) {
			case COMSION:
			case DEPOSITO:
				break;
			case RETENCION:
				
				break;
			case DEPOSITO_COMISION:
				
				break;
			case TOTAL:
				
				break;
			default:
				error("No se encuentra implementado este tipo de liquidacion");
				esValido = Boolean.FALSE;
				break;
			}
		}
		return esValido;
	}

	/**
	 * Permite inicializar la variables
	 */
	public void limpiarDatos() {
		datosBanco = new DatosBancoDto();
		depositoSeleccionado = new CheConfirmaDepositoDto();
		comprobantesSeleccionados = new ArrayList<>();
	}

	/**
	 * Permite cargar las comisiones de la tarjeta de credito seleccionada
	 */
	public void cargarComisionesTc() {
		datosBanco = new DatosBancoDto();
		comisionSeleccionada = new CxcDetComisionesTc();
		setNombreDialog("dialogComisionesTc");
		if (Objects.nonNull(noCliente)) {
			comisionesTc = comisionServiceLocal.consultarComisionesTc(getCompania().getNoCia(), noCliente);
			accionesDialog(nombreDialog, true);
		} else {
			error("Debe seleccionar una tarjeta de credito");
			accionesDialog(nombreDialog, false);
		}
	}

	/**
	 * Permite cargar los depositos de la tarjeta seleccionada
	 */
	public void cargarDepositos() {
		datosBanco = new DatosBancoDto();
		depositoSeleccionado = new CheConfirmaDepositoDto();
		setNombreDialog("dialogDeposito");
		if (Objects.nonNull(noCliente)) {
			depositos = cheConfirmaDepositoDtoDao.obtenerDeposito(getCompania().getNoCia(), noCliente);
			accionesDialog(nombreDialog, true);
		} else {
			error("Debe seleccionar una tarjeta de credito");
			accionesDialog(nombreDialog, false);
		}
	}

	/**
	 * Permite cargar los comprobantes de acuerdo a la tarjeta seleccionada
	 */
	public void cargarComprobantes() {
		filteredComprobantes = null;
		setNombreDialog("dialogComprobantes");
		if (Objects.nonNull(noCliente)) {
			String identificacion = clientes.stream()
					.filter(c -> c.getClientePK().getNoCliente().compareTo(noCliente) == 0).findFirst().get()
					.getCedula();
			comprobantes = comprobanteElectronicoDtoDao.obtenerComprobantesPorCliente(getCompania().getNoCia(),
					identificacion);
			accionesDialog(nombreDialog, true);
		} else {
			error("Debe seleccionar una tarjeta de credito");
			accionesDialog(nombreDialog, false);
		}
	}

	public void seleccionarDeposito() {
		datosBanco = new DatosBancoDto();
		datosBanco.setCodBanco(depositoSeleccionado.getPk().getBanco());
		datosBanco.setCuenta(depositoSeleccionado.getPk().getNoCuenta());
		datosBanco.setFecha(depositoSeleccionado.getPk().getFechaDeposito());
		datosBanco.setNumeroDocumento(depositoSeleccionado.getPk().getNoFisico());
		datosBanco.setValor(new BigDecimal(depositoSeleccionado.getValor()));
		accionesDialog(nombreDialog, false);
		filteredDepositos = null;
	}

	public void seleccionarComision() {
		datosBanco = new DatosBancoDto();
		datosBanco.setCodBanco(comisionSeleccionada.getId().getBanco());
		datosBanco.setCuenta(comisionSeleccionada.getCuenta());
		datosBanco.setFecha(comisionSeleccionada.getFecha());
		datosBanco.setNumeroDocumento(comisionSeleccionada.getId().getDocumento());
		datosBanco.setValor(comisionSeleccionada.getValor());
		accionesDialog(nombreDialog, false);
		filteredComisiones = null;
	}

	public BigDecimal getTotalRetencion() {
		datosBanco.setValor(BigDecimal.valueOf(
				comprobantesSeleccionados.stream().mapToDouble(p -> p.getValorRetencion().doubleValue()).sum()));
		return datosBanco.getValor();
	}

	public TipoLiquidacionEnum getTipoLiquidacion() {
		return tipoLiquidacion;
	}

	public void setTipoLiquidacion(TipoLiquidacionEnum tipoLiquidacion) {
		this.tipoLiquidacion = tipoLiquidacion;
	}

	public TipoLiquidacionEnum[] getTiposLiquidacion() {
		if (tiposLiquidacion == null) {
			tiposLiquidacion = new TipoLiquidacionEnum[3];
			if(CommonConstants.MANSUERA.equals(getCompania().getNoCia())){
				tiposLiquidacion = new TipoLiquidacionEnum[6];
			}
			 TipoLiquidacionEnum[] aux = TipoLiquidacionEnum.values();
			 for(int i=0; i<aux.length; i++){
				 if(CommonConstants.MANSUERA.equals(getCompania().getNoCia())
						 || !(TipoLiquidacionEnum.DEPOSITO_COMISION.equals(aux[i]) || TipoLiquidacionEnum.TOTAL.equals(aux[i]))){
					 tiposLiquidacion[i]=aux[i]; 
				 }
			 }
		}
		return tiposLiquidacion;
	}

	public void setTiposLiquidacion(TipoLiquidacionEnum[] tiposLiquidacion) {
		this.tiposLiquidacion = tiposLiquidacion;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	public List<Banco> getBancos() {
		return bancos;
	}

	public void setBancos(List<Banco> bancos) {
		this.bancos = bancos;
	}

	public DatosBancoDto getDatosBanco() {
		return datosBanco;
	}

	public void setDatosBanco(DatosBancoDto datosBanco) {
		this.datosBanco = datosBanco;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}

	public Long getNoCliente() {
		return noCliente;
	}

	public void setNoCliente(Long noCliente) {
		this.noCliente = noCliente;
	}

	public List<CheConfirmaDepositoDto> getDepositos() {
		return depositos;
	}

	public void setDepositos(List<CheConfirmaDepositoDto> depositos) {
		this.depositos = depositos;
	}

	public CheConfirmaDepositoDto getDepositoSeleccionado() {
		return depositoSeleccionado;
	}

	public void setDepositoSeleccionado(CheConfirmaDepositoDto depositoSeleccionado) {
		this.depositoSeleccionado = depositoSeleccionado;
	}

	public String getNombreDialog() {
		return nombreDialog;
	}

	public void setNombreDialog(String nombreDialog) {
		this.nombreDialog = nombreDialog;
	}

	public List<ComprobanteElectronicoDto> getComprobantes() {
		return comprobantes;
	}

	public void setComprobantes(List<ComprobanteElectronicoDto> comprobantes) {
		this.comprobantes = comprobantes;
	}

	public List<ComprobanteElectronicoDto> getComprobantesSeleccionados() {
		return comprobantesSeleccionados;
	}

	public void setComprobantesSeleccionados(List<ComprobanteElectronicoDto> comprobantesSeleccionados) {
		this.comprobantesSeleccionados = comprobantesSeleccionados;
	}

	public List<CheConfirmaDepositoDto> getFilteredDepositos() {
		return filteredDepositos;
	}

	public void setFilteredDepositos(List<CheConfirmaDepositoDto> filteredDepositos) {
		this.filteredDepositos = filteredDepositos;
	}

	public List<CxcDetComisionesTc> getComisionesTc() {
		return comisionesTc;
	}

	public void setComisionesTc(List<CxcDetComisionesTc> comisionesTc) {
		this.comisionesTc = comisionesTc;
	}

	public CxcDetComisionesTc getComisionSeleccionada() {
		return comisionSeleccionada;
	}

	public void setComisionSeleccionada(CxcDetComisionesTc comisionSeleccionada) {
		this.comisionSeleccionada = comisionSeleccionada;
	}

	public List<CxcDetComisionesTc> getFilteredComisiones() {
		return filteredComisiones;
	}

	public void setFilteredComisiones(List<CxcDetComisionesTc> filteredComisiones) {
		this.filteredComisiones = filteredComisiones;
	}

	public List<ComprobanteElectronicoDto> getFilteredComprobantes() {
		return filteredComprobantes;
	}

	public void setFilteredComprobantes(List<ComprobanteElectronicoDto> filteredComprobantes) {
		this.filteredComprobantes = filteredComprobantes;
	}

	public String getDiarioA() {
		return diarioA;
	}

	public void setDiarioA(String diarioA) {
		this.diarioA = diarioA;
	}

	public List<String> getMensajes() {
		return mensajes;
	}

	public void setMensajes(List<String> mensajes) {
		this.mensajes = mensajes;
	}

	public boolean isHayDiario() {
		return hayDiario;
	}

	public void setHayDiario(boolean hayDiario) {
		this.hayDiario = hayDiario;
	}

	public boolean isHayMensajes() {
		return hayMensajes;
	}

	public void setHayMensajes(boolean hayMensajes) {
		this.hayMensajes = hayMensajes;
	}
}
