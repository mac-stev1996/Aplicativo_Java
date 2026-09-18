/**
 * 
 */
package com.casabaca.prime.cxc.lazy;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.dto.RetencionDto;
import com.casabaca.common.ejb.service.RetencionDtoServiceLocal;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;

/**
 * @author Roberto Guizado
 *
 */
public class RetencionDtoLazyDataModel extends LazyDataModel<RetencionDto> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6112146915666379794L;
	private static final Logger LOG = Logger.getLogger(RetencionDtoLazyDataModel.class);

	private RetencionDtoServiceLocal retencionDtoService;
	private String noCia;
	private Map<String, Object> filtros;
	private List<RetencionDto> retencionDtos;
	private Boolean inicializaPaginado;

	public RetencionDtoLazyDataModel(String noCia, Map<String, Object> filtros, Boolean inicializaPaginado) {
		this.noCia = noCia;
		this.filtros = filtros;
		this.inicializaPaginado = inicializaPaginado;
		try {
			retencionDtoService = (RetencionDtoServiceLocal) ServiceLocator
					.getService("java:global/commons-ejb/RetencionDtoServiceBean");
		} catch (ServiceLocatorException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public List<RetencionDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		first = inicializaPaginado ? 0 : first;
		filters = filtros;
		retencionDtos = cargarRetencionesCaja(first, pageSize, filters);
		if (retencionDtos.size() > 0) {
			this.setRowCount(retencionDtos.get(0).getTotal().intValue());
		} else {
			this.setRowCount(0);
		}
		inicializaPaginado = Boolean.FALSE;
		return retencionDtos;
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public RetencionDto getRowData(String rowKey) {
		for (RetencionDto retencionDto : retencionDtos) {
			if (retencionDto.getPk().equals(rowKey)) {
				return retencionDto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(RetencionDto retencionDto) {
		return retencionDto.getPk();
	}

	private List<RetencionDto> cargarRetencionesCaja(int first, int size, Map<String, Object> filters) {
		return retencionDtoService.consultarRetencionesCajas(noCia, filters, first, size);

	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<RetencionDto> getRetencionDtos() {
		return retencionDtos;
	}

	public void setRetencionDtos(List<RetencionDto> retencionDtos) {
		this.retencionDtos = retencionDtos;
	}

	public Map<String, Object> getFiltros() {
		return filtros;
	}

	public void setFiltros(Map<String, Object> filtros) {
		this.filtros = filtros;
	}

	public Boolean getInicializaPaginado() {
		return inicializaPaginado;
	}

	public void setInicializaPaginado(Boolean inicializaPaginado) {
		this.inicializaPaginado = inicializaPaginado;
	}

}
