package com.casabaca.prime.cxc.common.lazy;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.dto.ZonasDto;
import com.casabaca.cxc.ejb.servicio.CxcNativeServiceLocal;
import com.casabaca.exception.ServiceLocatorException;

/**
 * @author jreyes
 *
 */
public class LazyZonasDataModel extends LazyDataModel<ZonasDto> {

	private static final long serialVersionUID = 5677382137689231876L;
	private static final Logger LOG = Logger.getLogger(LazyZonasDataModel.class);
	private String noCia;
	private CxcNativeServiceLocal cxcNativeServiceLocal;
	private List<ZonasDto> zonasDto;

	public LazyZonasDataModel(int first, int pageSize, String noCia) {
		try {
			this.noCia = noCia;
			cxcNativeServiceLocal = (CxcNativeServiceLocal) ServiceLocator
					.getService(NombreJNDI.CXC_NATIVE_SERVICE_BEAN);
		} catch (ServiceLocatorException e) {
			LOG.error("Error al inicializar el EJB: ", e);
		}
	}

	@Override
	public List<ZonasDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		setZonasDto(cxcNativeServiceLocal.obtenerZonaNocia(this.noCia, filters, first, pageSize));
		if (getZonasDto().size() > 0) {
			this.setRowCount(getZonasDto().get(0).getTotal().intValue());
		} else {
			this.setRowCount(0);
		}
		return zonasDto;
	}

	@Override
	public ZonasDto getRowData(String rowKey) {
		for (ZonasDto zonasDto : zonasDto) {
			if (zonasDto.getId().toString().equals(rowKey)) {
				return zonasDto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(ZonasDto zonasDto) {
		return zonasDto.getId();
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<ZonasDto> getZonasDto() {
		return zonasDto;
	}

	public void setZonasDto(List<ZonasDto> zonasDto) {
		this.zonasDto = zonasDto;
	}

}
