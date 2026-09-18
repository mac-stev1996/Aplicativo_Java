/*
 * Copyright 2012 TOYOTA CASABACA - ECUADOR 
 * Todos los derechos reservados
 */
package com.casabaca.compras.web.common.controller;

import javax.faces.bean.ManagedProperty;
import com.casabaca.compras.web.proceso.datamanager.CmpValeDataManager;

/**
 * <b> Clase generica que debe ser implentada en cada una de las clases hijas. </b>
 * 
 * @author dpicuasi
 * @version $Revision: 1.0 $
 *          <p>
 *          [$Author: dpicuasi $, $Date: 2018/12/17 $]
 *          </p>
 */
public abstract class CmpValeGenerico {

	//@ManagedProperty("#{cmpValeDataManager}")
	//protected CmpValeDataManager cmpValeDataManager;

	/**
	 * <b> Metodo abstracto para el modulo de compras que debe ser implementado en cada tipo de mantenimiento </b>
	 * <p>
	 * [Author: db_cumbicus, Date: 06/06/2012]
	 * </p>
	 */
	public abstract void buscar();

	/**
	 * <b> Metodo abstracto para el modulo de compras que debe ser implementado en cada tipo de mantenimiento </b>
	 * <p>
	 * [Author: db_cumbicus, Date: 06/06/2012]
	 * </p>
	 */
	public abstract void guardar();

	/**
	 * <b> Metodo abstracto para el modulo de compras que debe ser implementado en cada tipo de mantenimiento </b>
	 * <p>
	 * [Author: db_cumbicus, Date: 06/06/2012]
	 * </p>
	 */
	public abstract void nuevo();

	/**
	 * <b> Metodo abstracto para el modulo de compras que debe ser implementado en cada tipo de mantenimiento </b>
	 * <p>
	 * [Author: db_cumbicus, Date: 06/06/2012]
	 * </p>
	 */
	public abstract void eliminar();

	/**
	 * <b> Metodo abstracto para el modulo de compras que debe ser implementado en cada tipo de mantenimiento </b>
	 * <p>
	 * [Author: db_cumbicus, Date: 06/06/2012]
	 * </p>
	 */
	public abstract void editar();

	/**
	 * @return the cmpValeDataManager
	 */
	/*public CmpValeDataManager getCmpValeDataManager() {
		return cmpValeDataManager;
	}

	/**
	 * @param cmpValeDataManager the cmpValeDataManager to set
	 */
	/*public void setCmpValeDataManager(CmpValeDataManager cmpValeDataManager) {
		this.cmpValeDataManager = cmpValeDataManager;
	}*/

	/**
	 * @return the perfilesPorUsuarioDataManager
	 */
	/*public PerfilesPorUsuarioDataManager getPerfilesPorUsuarioDataManager() {
		return perfilesPorUsuarioDataManager;
	}*/

	/**
	 * @param perfilesPorUsuarioDataManager the perfilesPorUsuarioDataManager to set
	 */
	/*public void setPerfilesPorUsuarioDataManager(PerfilesPorUsuarioDataManager perfilesPorUsuarioDataManager) {
		this.perfilesPorUsuarioDataManager = perfilesPorUsuarioDataManager;
	}*/

}
