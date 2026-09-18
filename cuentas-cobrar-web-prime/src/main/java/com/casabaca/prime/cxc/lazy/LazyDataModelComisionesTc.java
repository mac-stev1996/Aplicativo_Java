/**
 * 
 */
package com.casabaca.prime.cxc.lazy;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.dao.DetComisionTcDtoDaoLocal;
import com.casabaca.cxc.ejb.dto.CxcDetComisionTarjetaDto;
import com.casabaca.cxc.ejb.util.Constantes;
import com.casabaca.exception.ServiceLocatorException;

/**
 * 
 * @author Roberto Guizado
 *
 */
public class LazyDataModelComisionesTc extends LazyDataModel<CxcDetComisionTarjetaDto> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5801987836847230970L;
	private static final Logger LOG = Logger.getLogger(LazyDataModelComisionesTc.class);
	private String noCia;
	private DetComisionTcDtoDaoLocal detComisionTcDtoDao;
	private List<CxcDetComisionTarjetaDto> comisionesTcDto;
	private CxcDetComisionTarjetaDto comisionTc;

	public LazyDataModelComisionesTc(int first, int pageSize, String noCia) {
		try {
			this.noCia = noCia;
			detComisionTcDtoDao = (DetComisionTcDtoDaoLocal) ServiceLocator
					.getService(Constantes.JNDI_DAO_DTO_COMISIONES_TC);
		} catch (ServiceLocatorException e) {
			LOG.error("Error al inicializar el EJB: ", e);
		}
	}

	@Override
	public List<CxcDetComisionTarjetaDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		setComisionesTcDto(cargarComisionesTc(first, pageSize, filters));
		if (getComisionesTcDto().size() > 0) {
			this.setRowCount(getComisionesTcDto().get(0).getTotal().intValue());
		} else {
			this.setRowCount(0);
		}
		return comisionesTcDto;
	}

	@Override
	public CxcDetComisionTarjetaDto getRowData(String rowKey) {
		for (CxcDetComisionTarjetaDto comisionTarjetaDto : comisionesTcDto) {
			if (comisionTarjetaDto.getDocumento().equals(rowKey)) {
				return comisionTarjetaDto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(CxcDetComisionTarjetaDto comisionTarjetaDto) {
		return comisionTarjetaDto.getDocumento();
	}

	private List<CxcDetComisionTarjetaDto> cargarComisionesTc(int first, int size, Map<String, Object> filters) {
		return detComisionTcDtoDao.consultarComisionesNoCiaFiltros(this.noCia, filters, first, size);

	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public List<CxcDetComisionTarjetaDto> getComisionesTcDto() {
		return comisionesTcDto;
	}

	public void setComisionesTcDto(List<CxcDetComisionTarjetaDto> comisionesTcDto) {
		this.comisionesTcDto = comisionesTcDto;
	}

	public CxcDetComisionTarjetaDto getComisionTc() {
		return comisionTc;
	}

	public void setComisionTc(CxcDetComisionTarjetaDto comisionTc) {
		this.comisionTc = comisionTc;
	}

}
