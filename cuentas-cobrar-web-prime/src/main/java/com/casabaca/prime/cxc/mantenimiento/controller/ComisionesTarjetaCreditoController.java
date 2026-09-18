/**
 * 
 */
package com.casabaca.prime.cxc.mantenimiento.controller;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import com.casabaca.common.ejb.model.Banco;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.BancoServiceLocal;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.ejb.dto.CxcDetComisionTarjetaDto;
import com.casabaca.cxc.ejb.modelo.CxcDetComisionesTc;
import com.casabaca.cxc.ejb.modelo.CxcDetComisionesTcPk;
import com.casabaca.cxc.ejb.servicio.ComisionTarjetaCreditoServiceLocal;
import com.casabaca.cxc.ejb.util.Constantes;
import com.casabaca.exception.DeleteException;
import com.casabaca.exception.FindException;
import com.casabaca.exception.InsertException;
import com.casabaca.prime.cxc.common.CommonController;
import com.casabaca.prime.cxc.dialog.controller.DialogDetComisionesTarjetasController;

/**
 * @author Roberto Guizado
 *
 */
@ManagedBean
@ViewScoped
public class ComisionesTarjetaCreditoController extends CommonController {

	private static final Logger LOG = Logger.getLogger(ComisionesTarjetaCreditoController.class);

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteServiceDelegate;

	@EJB(lookup = NombreJNDI.BANCO_SERVICE)
	private BancoServiceLocal bancoService;

	@EJB(lookup = Constantes.JNDI_SERVICE_COMISIONES_TC)
	private ComisionTarjetaCreditoServiceLocal comisionServiceLocal;

	@ManagedProperty(value = "#{dialogDetComisionesTarjetasController}")
	private DialogDetComisionesTarjetasController dgComisionesTarjetasController;

	private List<Cliente> clientes;
	private List<Banco> bancos;
	private CxcDetComisionesTc comisionTarjeta;
	private List<SelectItem> estadosSelectItems;

	public void crearComision() {
		comisionTarjeta = new CxcDetComisionesTc();
		CxcDetComisionesTcPk pk = new CxcDetComisionesTcPk();
		comisionTarjeta.setId(pk);
		if (Objects.isNull(clientes) || clientes.isEmpty()) {
			clientes = clienteServiceDelegate.getClienteTarjetaCredito(getCompania().getNoCia());
		}
		if (Objects.isNull(bancos) || bancos.isEmpty()) {
			try {
				bancos = bancoService.getListaBanco("A");
			} catch (FindException e) {
				LOG.error("No se pudo obtener la lista de bancos", e);
			}
		}
	}

	public void guardarComision() {
		Optional<CxcDetComisionTarjetaDto> usu = dgComisionesTarjetasController.getLazyDataModelComisionesTc()
				.getComisionesTcDto().stream().filter(c -> c.getBanco().equals(comisionTarjeta.getId().getBanco())
						&& c.getDocumento().equals(comisionTarjeta.getId().getDocumento()))
				.findFirst();
		if (usu.isPresent()) {
			error("Ya se encuentra agregado una comision con el mismo numero de documento");
		} else if (esValidoDatos()) {
			comisionTarjeta.getId().setNoCia(getCompania().getNoCia());
			comisionTarjeta.setEstado("I");
			comisionTarjeta.setUsuarioRegistra(getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
			try {

				comisionServiceLocal.guardarComisionTc(comisionTarjeta);
				info("Comision guardado con exito");
			} catch (InsertException e) {
				LOG.error(e);
				error(e.getDetail());
			}
		}
	}

	public void eliminarComision(CxcDetComisionTarjetaDto comisionTarjetaDto) {
		try {
			comisionServiceLocal.eliminarComisionTc(getCompania().getNoCia(), comisionTarjetaDto);
		} catch (DeleteException e) {
			LOG.error(e);
			error("Existio un error al eliminar el registro");
		} catch (FindException e) {
			error(e.getDetail());
		}
	}

	/**
	 * Permite validar los datos de entrada
	 * 
	 * @return
	 */
	private boolean esValidoDatos() {
		boolean esValido = Boolean.TRUE;
		if (Objects.isNull(comisionTarjeta.getNoCliente()) || comisionTarjeta.getId().getBanco().isEmpty()
				|| comisionTarjeta.getId().getDocumento().isEmpty() || comisionTarjeta.getCuenta().isEmpty()
				|| Objects.isNull(comisionTarjeta.getFecha()) || Objects.isNull(comisionTarjeta.getValor())) {
			esValido = Boolean.FALSE;
			error("Debe ingresar todos los datos de la comison para guardar");
		}
		return esValido;
	}

	public List<Banco> getBancos() {
		return bancos;
	}

	public void setBancos(List<Banco> bancos) {
		this.bancos = bancos;
	}

	public List<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<Cliente> clientes) {
		this.clientes = clientes;
	}

	public CxcDetComisionesTc getComisionTarjeta() {
		return comisionTarjeta;
	}

	public void setComisionTarjeta(CxcDetComisionesTc comisionTarjeta) {
		this.comisionTarjeta = comisionTarjeta;
	}

	public List<SelectItem> getEstadosSelectItems() {
		return estadosSelectItems;
	}

	public void setEstadosSelectItems(List<SelectItem> estadosSelectItems) {
		this.estadosSelectItems = estadosSelectItems;
	}

	public DialogDetComisionesTarjetasController getDgComisionesTarjetasController() {
		return dgComisionesTarjetasController;
	}

	public void setDgComisionesTarjetasController(DialogDetComisionesTarjetasController dgComisionesTarjetasController) {
		this.dgComisionesTarjetasController = dgComisionesTarjetasController;
	}
	
	
}
