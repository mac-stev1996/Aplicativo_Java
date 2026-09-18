/**
 * 
 */
package com.casabaca.prime.cxc.common.lazy;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.administration.ejb.dao.UsuarioDtoDaoLocal;
import com.casabaca.administration.ejb.dto.UsuarioDto;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;

/**
 * 
 * @author Roberto Guizado
 *
 */
public class LazyDataModelUsuarios extends LazyDataModel<UsuarioDto> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5801987836847230970L;
	private static final Logger LOG = Logger.getLogger(LazyDataModelUsuarios.class);
	private String noCia;
	private UsuarioDtoDaoLocal usuarioDtoDao;
	private List<UsuarioDto> usuariosDtos;

	public LazyDataModelUsuarios(int first, int pageSize, String noCia) {
		try {
			this.noCia = noCia;
			usuarioDtoDao = (UsuarioDtoDaoLocal) ServiceLocator.getService("java:global/administracion-ejb/UsuarioDtoDaoBean");
		} catch (ServiceLocatorException e) {
			LOG.error("Error al inicializar el EJB: ", e);
		}
	}

	public List<UsuarioDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		setUsuariosDtos(cargarUsuarios(first, pageSize, filters));
		if (getUsuariosDtos().size() > 0) {
			this.setRowCount(getUsuariosDtos().get(0).getTotal().intValue());
		} else {
			this.setRowCount(0);
		}
		return usuariosDtos;
	}

	@Override
	public UsuarioDto getRowData(String rowKey) {
		for (UsuarioDto clienteDto : usuariosDtos) {
			if (clienteDto.getUsuario().toString().equals(rowKey)) {
				return clienteDto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(UsuarioDto clienteDto) {
		return clienteDto.getUsuario();
	}

	private List<UsuarioDto> cargarUsuarios(int first, int size, Map<String, Object> filters) {
		List<UsuarioDto> clientesDto = usuarioDtoDao.consultarUsuarioNoCiaFiltros(this.noCia, filters, first, size);
		return clientesDto;

	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<UsuarioDto> getUsuariosDtos() {
		return usuariosDtos;
	}

	public void setUsuariosDtos(List<UsuarioDto> usuariosDtos) {
		this.usuariosDtos = usuariosDtos;
	}

}
