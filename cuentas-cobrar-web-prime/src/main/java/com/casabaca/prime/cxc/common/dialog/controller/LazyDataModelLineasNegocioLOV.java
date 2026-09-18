package com.casabaca.prime.cxc.common.dialog.controller;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;


import com.casabaca.common.ejb.dao.LineaNegocioLOVDtoDaoLocal;
import com.casabaca.common.ejb.dto.LineaNegocioLOVDto;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.exception.ServiceLocatorException;

public class LazyDataModelLineasNegocioLOV extends LazyDataModel<LineaNegocioLOVDto>{
	
	private static final long serialVersionUID = 1564897233546879232L;
	private static final Logger LOG = Logger.getLogger(LazyDataModelLineasNegocioLOV.class);
	private String noCia;
	private String aplicaDocTributario;
	private String noLineaAdicional;
	private LineaNegocioLOVDtoDaoLocal lineaNegocioLOVDtoDao;
	private List<LineaNegocioLOVDto> lineasNegocioLOVDtos;

	public LazyDataModelLineasNegocioLOV(String noCia, String aplicaDocTributario, String noLineaAdicional) {
		try {
			this.noCia = noCia;
			this.aplicaDocTributario = aplicaDocTributario;
			this.noLineaAdicional = noLineaAdicional;
			lineaNegocioLOVDtoDao = (LineaNegocioLOVDtoDaoLocal) ServiceLocator
					.getService("java:global/commons-ejb/LineaNegocioLOVDtoDaoBean");
		} catch (ServiceLocatorException e) {
			LOG.error("Error al inicializar el EJB: ", e);
		}
	}

	@Override
	public List<LineaNegocioLOVDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		setLineasNegocioLOVDtos(cargarLineasNegocioLOV(first, pageSize, filters));
		if (getLineasNegocioLOVDtos().size() > 0) {
			this.setRowCount(getLineasNegocioLOVDtos().get(0).getTotal().intValue());
		} else {
			this.setRowCount(0);
		}
		return lineasNegocioLOVDtos;
	}

	@Override
	public LineaNegocioLOVDto getRowData(String rowKey) {
		for (LineaNegocioLOVDto lineaNegocioLOVDto : lineasNegocioLOVDtos) {
			if (lineaNegocioLOVDto.getPk().toString().equals(rowKey)) {
				return lineaNegocioLOVDto;
			}
		}
		return null;
	}

	@Override
	public Object getRowKey(LineaNegocioLOVDto lineaNegocioLOVDto) {
		return lineaNegocioLOVDto.getPk();
	}

	private List<LineaNegocioLOVDto> cargarLineasNegocioLOV(int first, int size, Map<String, Object> filters) {
		List<LineaNegocioLOVDto> lineasNegocioLOVDto = lineaNegocioLOVDtoDao
				.consultarLineanegocioByNoCiaAplicadoctributarioNolineaadicionalFiltros(this.noCia,
						this.aplicaDocTributario, this.noLineaAdicional, filters, first, size);
		return lineasNegocioLOVDto;
	}

	public String getNoCia() {
		return noCia;
	}

	public void setNoCia(String noCia) {
		this.noCia = noCia;
	}

	public String getAplicaDocTributario() {
		return aplicaDocTributario;
	}

	public void setAplicaDocTributario(String aplicaDocTributario) {
		this.aplicaDocTributario = aplicaDocTributario;
	}

	public String getNoLineaAdicional() {
		return noLineaAdicional;
	}

	public void setNoLineaAdicional(String noLineaAdicional) {
		this.noLineaAdicional = noLineaAdicional;
	}

	public List<LineaNegocioLOVDto> getLineasNegocioLOVDtos() {
		return lineasNegocioLOVDtos;
	}

	public void setLineasNegocioLOVDtos(List<LineaNegocioLOVDto> lineasNegocioLOVDtos) {
		this.lineasNegocioLOVDtos = lineasNegocioLOVDtos;
	}


}
