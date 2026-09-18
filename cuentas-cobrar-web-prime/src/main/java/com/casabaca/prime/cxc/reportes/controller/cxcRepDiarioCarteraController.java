package com.casabaca.prime.cxc.reportes.controller;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.annotation.*;
import javax.persistence.PostPersist;
import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.Message;

import com.casabaca.common.CommonConstants;
import com.casabaca.common.CommonUtils;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.ejb.modelo.ChequeProtestado;
import com.casabaca.cxc.ejb.servicio.ChequeProtestadoServiceLocal;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.prime.cxc.common.ReportCommonController;
import com.casabaca.s3s.ejb.service.delegate.UtilServiceDelegate;

@ManagedBean(name = "cxcRepDiarioCarteraController")
@RequestScoped
public class cxcRepDiarioCarteraController extends ReportCommonController {

	private static final Logger LOG = Logger.getLogger(cxcRepDiarioCarteraController.class);
	public static final String REPORTE = "/cxc/CxcCarteraDiaria";
	private String resultado;
	@SuppressWarnings("unused")
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	private ChequeProtestadoServiceLocal chequeProtestadoServiceLocal;
	

	
	
	public cxcRepDiarioCarteraController() throws ServiceLocatorException {
		super();
		chequeProtestadoServiceLocal = (ChequeProtestadoServiceLocal) ServiceLocator.getService(com.casabaca.common.ejb.util.NombreJNDI.CHEQUE_PROTESTADO_SERVICE);
	}


	public void init() throws ServiceLocatorException {
	
		
	}
	
	
	public void generarProceso() {
		
		try {
			
			resultado= chequeProtestadoServiceLocal.generarReporteDiario(getCompania().getNoCia(),getCompania().getNombre(),getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
			info(resultado);
		} catch (Exception e) {
			// TODO: handle exception
			LOG.error(e.getMessage(), e);
			error("Se presento un error al generar el reporte, comuníquese con sistemas");
		}
		
}
	
	@SuppressWarnings("deprecation")
	public void generarReporte(String tipoReporte) {
		try {
			//chequeProtestadoServiceLocal.
		
			if ("S".equals(getCompania().getServidorJasper())) {
				Map<String, Object> parameters = new HashMap<>();
				Thread.sleep(100L);
				parameters.put("P_NOCIA", getCompania().getNoCia());
				parameters.put("P_USUARIO", getUsuarioCentroConectado().getUsuarioCentroPK().getUsuario());
				FacesContext.getCurrentInstance().getExternalContext().redirect(super.callJasperReport(
						REPORTE+CommonUtils.getCiaReportUnit(getCompania().getNoCia())
								,
						parameters,
						"pdf".equals(tipoReporte) ? CommonConstants.OUTPUT_PDF : CommonConstants.OUTPUT_XLSX_NO_PAG));
			}

			else {
				warn("Debe seleccionar los datos para generar el reporte");
			}
			 
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			error("Se presento un error al generar el reporte, comuníquese con sistemas");
		}
	}

}
