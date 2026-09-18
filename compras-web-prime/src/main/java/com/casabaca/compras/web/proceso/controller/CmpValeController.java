/*
 * Copyright 2012 TOYOTA CASABACA - ECUADOR 
 * Todos los derechos reservados
 */
package com.casabaca.compras.web.proceso.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;

import com.casabaca.common.ejb.service.NativeDmlDatabaseServiceLocal;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.compras.ejb.modelo.CmpVale;
import com.casabaca.compras.ejb.servicio.CmpValeServiceLocal;
import com.casabaca.compras.web.common.controller.CommonController;
import com.casabaca.compras.web.proceso.datamanager.CmpValeDataManager;
import com.casabaca.exception.ServiceException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.s3s.ejb.model.UsuarioSis;
import com.casabaca.s3s.ejb.service.UsuarioSisServiceLocal;
import com.casabaca.s3s.ejb.service.delegate.UsuarioSisServiceDelegate;

/**
 * <b> Permisos Por CmpValeController. </b>
 * 
 * @author dpicuasi
 * @version $Revision: 1.0 $
 * <p>
 *   [$Author: dpicuasi $, $Date: 2018/12/20 $]
 * </p>
 */
@ManagedBean(name = "cmpValeController")
@RequestScoped
public class CmpValeController extends CommonController {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(CmpValeController.class);
	
	@ManagedProperty(value = "#{cmpValeDataManager}")
	private CmpValeDataManager cmpValeDataManager;
	
	private CmpValeServiceLocal cmpValeServiceLocal;
	private NativeDmlDatabaseServiceLocal nativeDmlDatabaseService;
	private UsuarioSisServiceLocal usuarioSisServiceLocal; 
	private DataTable tableUsuarioSisModal;
		
	
	public void init() {
		cmpValeDataManager.getCmpVale().setNoCia(getCompania().getNoCia());
		cmpValeDataManager.getCmpVale().setAgencia(getUsuario().getCentro());
		cmpValeDataManager.setNoCia(getCompania().getNoCia());
		cmpValeDataManager.setAgencia(getUsuario().getCentro());
		buscar();
		cargarAgencias();
		cargarCompanias();
		cargarProveedores();
		cargarArea();
	}

	public CmpValeController() {
		super();
		try {
			cmpValeServiceLocal = (CmpValeServiceLocal) ServiceLocator.getService(com.casabaca.common.ejb.util.NombreJNDI.CMP_VALE_VOUCHER);
			usuarioSisServiceLocal = (UsuarioSisServiceLocal) ServiceLocator.getService(com.casabaca.common.ejb.util.NombreJNDI.USUARIO_SIS_SERVICE_BEAN_LOCAL);
			nativeDmlDatabaseService =(NativeDmlDatabaseServiceLocal) ServiceLocator.getService(com.casabaca.common.ejb.util.NombreJNDI.NATIVE_DML_DATABASE_SERVICE_BEAN);
		} catch (ServiceLocatorException e) {
			e.printStackTrace();
		}
	}

	public void buscar() {
		
		
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();

		sql.append("SELECT o");
		sql.append(" FROM CmpVale as o");
		sql.append(" WHERE 1=1 ");
		
		if(cmpValeDataManager.getNoCia()!=null) {
			sql.append(" AND o.noCia =:paramNoCia ");
		}
		if(getUsuario().getCentro()!=null) {
			sql.append(" AND o.agencia =:paramAgencia ");
		}
		if(cmpValeDataManager.getTipoVale()!=null) {
			sql.append(" AND o.tipoVale =:paramTipoVale ");
		}
		if(cmpValeDataManager.getNoProve()!=null) {
			sql.append(" AND o.noProve =:paramNoProve ");
		}
		if(cmpValeDataManager.getFechaDesde()!=null && cmpValeDataManager.getFechaHasta()!=null) {
			sql.append(" AND o.fecha between :paramFechaD and :paramFechaH ");
		}

		
		if(cmpValeDataManager.getNoCia()!=null) {
			parametros.put("paramNoCia", cmpValeDataManager.getNoCia());
		}
		if(getUsuario().getCentro()!=null) {
			parametros.put("paramAgencia", getUsuario().getCentro());
		}
		if(cmpValeDataManager.getTipoVale()!=null) {
			parametros.put("paramTipoVale", cmpValeDataManager.getTipoVale());
		}
		if(cmpValeDataManager.getNoProve()!=null) {
			parametros.put("paramNoProve", cmpValeDataManager.getNoProve());
		}
		if(cmpValeDataManager.getFechaDesde()!=null && cmpValeDataManager.getFechaHasta()!=null) {
			parametros.put("paramFechaD", cmpValeDataManager.getFechaDesde());
			parametros.put("paramFechaH", cmpValeDataManager.getFechaHasta());
		}
		
		sql.append(" order by o.code desc ");

		List<CmpVale> listaCmpVale = cmpValeServiceLocal.findObjectsAdvanced(sql,parametros, 0, 0);
		
		
	
		/*List<CmpVale> listaCmpVale=null;
		try {
			listaCmpVale = cmpValeServiceLocal.findAll();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//cmpValeDataManager.setListaCmpVale(listaCmpVale);
		cmpValeDataManager.setListaCmpVale(listaCmpVale);
	}

	public void guardar() {
	 try {
			int dias=(int) (((new Date()).getTime()-cmpValeDataManager.getCmpVale().getFecha().getTime())/86400000);
		    if(dias<=3) {
		    	getUsuario().getCentro();
		    	
		    	cmpValeDataManager.getCmpVale().setFechaRegistro(new Date());
			    cmpValeDataManager.getCmpVale().setHora(cmpValeDataManager.getHoraTmp()+":"+cmpValeDataManager.getMinutoTmp());
				if(cmpValeDataManager.getCmpVale().getCode()!=null) {
					cmpValeServiceLocal.update(cmpValeDataManager.getCmpVale());
				}else {
					cmpValeServiceLocal.create(cmpValeDataManager.getCmpVale());
				}
				this.buscar();
				addInfoMessage("Exito:", "Información guardada con éxito..");
				
		    }else {
		    	addErrorMessage("Atención:","Lo siento, solo es posible registrar información de hasta 3 días atrás.");
		    }
	  } catch (ServiceException e) {
			addErrorMessage("Error:","Error, no pudo guardar la información");
			e.printStackTrace();
	  }
	}
	
	public void limpiar() {
		cmpValeDataManager.setTipoVale(null);
		cmpValeDataManager.setNoProve(null);
		cmpValeDataManager.setFechaDesde(null);
		cmpValeDataManager.setFechaHasta(null);
		cmpValeDataManager.setCmpVale(new CmpVale());
		cmpValeDataManager.setHoraTmp(null);
		cmpValeDataManager.setMinutoTmp(null);
		cmpValeDataManager.setNoProve(null);
		addInfoMessage("Exito:", "Formulario sin datos..");
	}
	
	/***
	 * Metodo para cargar la información de las agencias
	 */
    public void cargarAgencias() {
    	//if(cmpValeDataManager.getAgencias()!=null) {
	    	//if(cmpValeDataManager.getAgencias().isEmpty()) {
				List<SelectItem> resp = new ArrayList<SelectItem>();
				HashMap parametros = new HashMap();
				StringBuffer sql = new StringBuffer();
				sql.append(" SELECT CODIGO,NOMBRE FROM AGENCIAS WHERE NO_CIA=:noCia AND APLICA_SEMINUEVOS='S' ");
				parametros.put("noCia",getCompania().getNoCia());
				List resultadoInfoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null,0,0);
				if (resultadoInfoList.size() > 0) {
						for (int i = 0; i < resultadoInfoList.size(); i++) {
							resp.add(new SelectItem(((Object[]) resultadoInfoList.get(i))[0].toString(),((Object[]) resultadoInfoList.get(i))[1].toString()));
						}
				}
				cmpValeDataManager.setAgencias(resp);
	    	//}
    	//}
	}
    
    
    /**
     * Metodo que carga la información de la compañia
     */
    public void cargarCompanias() {
    	//if(cmpValeDataManager.getCompanias()!=null) {
	    	//if(cmpValeDataManager.getCompanias().isEmpty()) {
		    	List<SelectItem> items = new ArrayList<SelectItem>();
				HashMap parametros = new HashMap();
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT F.CEDULA,F.NOMBRE FROM ARCCMC F");
				sql.append(" WHERE F.no_cia =:compania");
				sql.append(" AND F.CEDULA =:cedula");
			    parametros.put("cedula", getUsuario().getCedulaConcesionario());
				sql.append(" ORDER BY 2");
				parametros.put("compania", getCompania().getNoCia());
				List resultadoInfoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null,0,0);
				if (resultadoInfoList.size() > 0) {
					for (int i = 0; i < resultadoInfoList.size(); i++) {
							items.add(new SelectItem(((Object[]) resultadoInfoList
								.get(i))[0].toString(),
								((Object[]) resultadoInfoList.get(i))[1].toString()));
					}
				}
				cmpValeDataManager.setCompanias(items);
	    	//} 
    	//}
	}
    
    
    public void cargarArea() {
    	List<SelectItem> resp = new ArrayList<SelectItem>();
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		if(getUsuario().getUsuario().compareTo("MD_ALMEIDA")==0) {
			resp.add(new SelectItem("APOYO","APOYO"));
		}else {
		    if(getUsuario().getUsuario().compareTo("RP_NUNOZ")==0) {
			   resp.add(new SelectItem("APOYO","APOYO"));
			   resp.add(new SelectItem("COMERCIAL","COMERCIAL"));
		    }else {
		    	resp.add(new SelectItem("COMERCIAL","COMERCIAL"));
		    }
		}
		cmpValeDataManager.setArea(resp);
    }
    
    public void cargarSubArea() {
    	List<SelectItem> resp = new ArrayList<SelectItem>();
    	if(cmpValeDataManager.getCmpVale().getArea().compareTo("COMERCIAL")==0) {
			resp.add(new SelectItem("VEHICULOS NUEVOS","VEHICULOS NUEVOS"));
			resp.add(new SelectItem("VEHICULOS SEMINUEVOS","VEHICULOS SEMINUEVOS"));
			resp.add(new SelectItem("SERVICIO","SERVICIO"));
			resp.add(new SelectItem("REPUESTOS","REPUESTOS"));
    	}else {
			resp.add(new SelectItem("ADMINISTRACION","ADMINISTRACION"));
			resp.add(new SelectItem("IT","IT"));
			resp.add(new SelectItem("CONTABILIDAD","CONTABILIDAD"));
			resp.add(new SelectItem("SIG","SIG"));
			resp.add(new SelectItem("NOMINA","NOMINA"));
			resp.add(new SelectItem("CREDITO","CREDITO"));
			resp.add(new SelectItem("COBRANZAS","COBRANZAS"));
    	}
		cmpValeDataManager.setSubArea(resp);
    }
    

    public void cargarProveedor() {
    	List<SelectItem> resp = new ArrayList<SelectItem>();
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		if(cmpValeDataManager.getTipoVale().compareTo("T")==0) {
			sql.append(" SELECT NO_PROVE,NOMBRE FROM ARCPMP WHERE NO_CIA='01' AND NOMBRE LIKE '%CIVICA%' AND CLASE='OTROS' ");
		}
		
		if(cmpValeDataManager.getTipoVale().compareTo("C")==0) {
			sql.append(" SELECT NO_PROVE,NOMBRE FROM ARCPMP WHERE NO_CIA='01' AND NOMBRE LIKE '%ATIMASA S.A%' AND CLASE='OTROS' ");
		}
		List resultadoInfoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, null, null,0,0);
		if (resultadoInfoList.size() > 0) {
				for (int i = 0; i < resultadoInfoList.size(); i++) {
					resp.add(new SelectItem(((Object[]) resultadoInfoList.get(i))[0].toString(),((Object[]) resultadoInfoList.get(i))[1].toString()));
				}
		}
		cmpValeDataManager.setProveedor(resp);
    }
    
    
    public void cargarProveedores() {
    	List<SelectItem> resp = new ArrayList<SelectItem>();
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT NO_PROVE,NOMBRE FROM ARCPMP WHERE NO_CIA='01' AND NOMBRE LIKE '%CIVICA%' AND CLASE='OTROS' ");
		sql.append(" UNION ");
		sql.append(" SELECT NO_PROVE,NOMBRE FROM ARCPMP WHERE NO_CIA='01' AND NOMBRE LIKE '%ATIMASA S.A%' AND CLASE='OTROS' ");
		List resultadoInfoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, null, null,0,0);
		if (resultadoInfoList.size() > 0) {
				for (int i = 0; i < resultadoInfoList.size(); i++) {
					cmpValeDataManager.getHashProveedores().put(((Object[]) resultadoInfoList.get(i))[0].toString(), ((Object[]) resultadoInfoList.get(i))[1].toString());					
				}
		}
    }
    
    public String buscarNombreProveedor(String noProve) {
    	return cmpValeDataManager.getHashProveedores().get(noProve);
    }
    
    
    public void cargarProveedorNuevo() {
    	List<SelectItem> resp = new ArrayList<SelectItem>();
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		if(cmpValeDataManager.getCmpVale().getTipoVale().compareTo("T")==0) {
			sql.append(" SELECT NO_PROVE,NOMBRE FROM ARCPMP WHERE NO_CIA='01' AND NOMBRE LIKE '%CIVICA%' AND CLASE='OTROS' ");
		}
		
		if(cmpValeDataManager.getCmpVale().getTipoVale().compareTo("C")==0) {
			sql.append(" SELECT NO_PROVE,NOMBRE FROM ARCPMP WHERE NO_CIA='01' AND NOMBRE LIKE '%ATIMASA S.A%' AND CLASE='OTROS' ");
		}
		List resultadoInfoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, null, null,0,0);
		if (resultadoInfoList.size() > 0) {
				for (int i = 0; i < resultadoInfoList.size(); i++) {
					resp.add(new SelectItem(((Object[]) resultadoInfoList.get(i))[0].toString(),((Object[]) resultadoInfoList.get(i))[1].toString()));
				}
		}
		cmpValeDataManager.setProveedor(resp);
    }
    
    public void editarPrevio() {
    	if(cmpValeDataManager.getCmpVale().getTipoVale().compareTo("T")==0) {
	    	cmpValeDataManager.setHoraTmp(null);
	    	cmpValeDataManager.setHoraTmp(null);
	    	String[] datos=cmpValeDataManager.getCmpVale().getHora().split(":");
	    	cmpValeDataManager.setHoraTmp(datos[0]);
	    	cmpValeDataManager.setMinutoTmp(datos[1]);
    	}
    }
    
    
	
    /***
     * Nuevo CmpValeController
     */
	public void nuevo() {
		this.limpiar();
		cmpValeDataManager.getCmpVale().setFecha(new Date());
		cmpValeDataManager.getCmpVale().setValor(0.0);
		cmpValeDataManager.getCmpVale().setTipoVale("T");
		this.cargarProveedorNuevo();
	}
	
	
	public void showModalResponsable(ActionEvent event) {
		UsuarioSisServiceDelegate usuarioSis = new UsuarioSisServiceDelegate();
		cmpValeDataManager.setVarSeleccionado("R");
		if(cmpValeDataManager.getUsuariosModal()!=null) {
			if(cmpValeDataManager.getUsuariosModal().isEmpty()) {
				cmpValeDataManager.setUsuariosModal(usuarioSis.getUsuariosSistemas());
			}
		}else {
			cmpValeDataManager.setUsuariosModal(usuarioSis.getUsuariosSistemas());
		}
	}
	
	
	public void showModalResponsableTjta(ActionEvent event) {
		UsuarioSisServiceDelegate usuarioSis = new UsuarioSisServiceDelegate();
		cmpValeDataManager.setVarSeleccionado("T");
		if(cmpValeDataManager.getUsuariosModal()!=null) {
			if(cmpValeDataManager.getUsuariosModal().isEmpty()) {
				cmpValeDataManager.setUsuariosModal(usuarioSis.getUsuariosSistemas());
			}
		}else {
			cmpValeDataManager.setUsuariosModal(usuarioSis.getUsuariosSistemas());
		}
	}
	
	public void showModalConductor(ActionEvent event) {
		UsuarioSisServiceDelegate usuarioSis = new UsuarioSisServiceDelegate();
		cmpValeDataManager.setVarSeleccionado("C");
		if(cmpValeDataManager.getUsuariosModal()!=null) {
			if(cmpValeDataManager.getUsuariosModal().isEmpty()) {
				cmpValeDataManager.setUsuariosModal(usuarioSis.getUsuariosSistemas());
			}
		}else {
			cmpValeDataManager.setUsuariosModal(usuarioSis.getUsuariosSistemas());
		}
		
	}
	
	public void selectUserSistemasModal(ActionEvent event) {
		UsuarioSis usuario = (UsuarioSis) tableUsuarioSisModal.getRowData();
		
		if(usuario!=null) {
			String [] datos=buscarCentroCosto(usuario.getNoEmple(), usuario.getNoCia());
			cmpValeDataManager.getCmpVale().setCentroCosto(datos[0]);
			cmpValeDataManager.getCmpVale().setCentronNombre(datos[1]);
		}
		
		if(cmpValeDataManager.getVarSeleccionado().compareTo("R")==0) {
			cmpValeDataManager.getCmpVale().setResponsable(usuario.getUsuario());
			
		}
		
		if(cmpValeDataManager.getVarSeleccionado().compareTo("T")==0) {
			cmpValeDataManager.getCmpVale().setResponsableTarjeta(usuario.getUsuario());
		}
		
		if(cmpValeDataManager.getVarSeleccionado().compareTo("C")==0) {
			cmpValeDataManager.getCmpVale().setConductor(usuario.getUsuario());
		}
		
	}
	
	public String obtenerNombreEmpleado(String username) {
		if(username.compareTo("")!=0) {
			List<UsuarioSis> usuario = usuarioSisServiceLocal.buscarPorUsuarioLike(username);
			if(usuario!=null) {
				if(!usuario.isEmpty()) {
				  return usuario.get(0).getNombre();
				}
			}
		}
		return "";
	}
	
	
	public String obtenerNombreEmple(String username) {
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		sql.append(" select nombre from  usuarios_sis ");
		sql.append(" where usuario =:username ");
		parametros.put("username", username);
		List resultadoInfoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null, 0, 0);

		if (resultadoInfoList != null && resultadoInfoList.size() > 0) {
			return String.valueOf(resultadoInfoList.get(0));
		}
		return "";
	}
	
	
	public String[] buscarCentroCosto(String noEmple, String noCia) {
		HashMap parametros = new HashMap();
		StringBuffer sql = new StringBuffer();
		//sql.append(" select cencosto_id,nombre from CENTROS_NOMINA WHERE CENCOSTO_ID IN ");
		//sql.append(" (select CENTRO_COSTO from NOM_EMPLEADOS WHERE NO_CIA=:paramNocia AND NO_EMPLE=:paramNoemple) ");
		sql.append(" SELECT NEM.UNIADMIN_ID,CN.NOMBRE  FROM NOM_EMPLEADOS NEM,CENTROS_NOMINA CN WHERE NEM.NO_CIA=:paramNocia AND NEM.NO_EMPLE=:paramNoemple ");
		sql.append(" AND CN.CENCOSTO_ID=NEM.CENTRO_COSTO ");
		parametros.put("paramNocia", noCia );
		parametros.put("paramNoemple", noEmple);
		String codigo="";
		String nombre="";
		String [] datos=new String[2];
		List resultadoInfoList = nativeDmlDatabaseService.nativeQueryAdvanced(sql, parametros, null, 0, 0);

		if (resultadoInfoList != null && resultadoInfoList.size() > 0) {
			codigo=((Object[]) resultadoInfoList.get(0))[0].toString();
			nombre=((Object[]) resultadoInfoList.get(0))[1].toString();
		}
		datos[0]=codigo;
		datos[1]=nombre;
		return datos;
	}
	
	

	public void eliminar() {
		 try {
				if(cmpValeDataManager.getCmpVale().getCode()!=null) {
					cmpValeServiceLocal.delete(cmpValeDataManager.getCmpVale().getCode());
				}
				this.buscar();
				addInfoMessage("Exito:", "Información eliminada con éxito..");
		  } catch (ServiceException e) {
				addErrorMessage("Error:","Error, no pudo eliminar la información");
				e.printStackTrace();
		  }
	}

	public void editar() {
		// TODO Auto-generated method stub
	}

	public CmpValeDataManager getCmpValeDataManager() {
		return cmpValeDataManager;
	}

	public void setCmpValeDataManager(CmpValeDataManager cmpValeDataManager) {
		this.cmpValeDataManager = cmpValeDataManager;
	}

	public CmpValeServiceLocal getCmpValeServiceLocal() {
		return cmpValeServiceLocal;
	}

	public void setCmpValeServiceLocal(CmpValeServiceLocal cmpValeServiceLocal) {
		this.cmpValeServiceLocal = cmpValeServiceLocal;
	}

	public DataTable getTableUsuarioSisModal() {
		return tableUsuarioSisModal;
	}

	public void setTableUsuarioSisModal(DataTable tableUsuarioSisModal) {
		this.tableUsuarioSisModal = tableUsuarioSisModal;
	}

	public UsuarioSisServiceLocal getUsuarioSisServiceLocal() {
		return usuarioSisServiceLocal;
	}

	public void setUsuarioSisServiceLocal(UsuarioSisServiceLocal usuarioSisServiceLocal) {
		this.usuarioSisServiceLocal = usuarioSisServiceLocal;
	}
	
	
	
}
