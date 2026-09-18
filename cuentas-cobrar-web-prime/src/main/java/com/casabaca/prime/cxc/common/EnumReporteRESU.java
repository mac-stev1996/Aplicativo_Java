package com.casabaca.prime.cxc.common;

public enum EnumReporteRESU {

	CLIENTES("C", "DETALLECLIENTE"), OPERACIONES("O", "DETALLEOPERACION"), TRANSACCIONES("T", "DETALLETRANSACCION"),
	RESUMEN("R", "CABECERA");

	private String id;
	private String nombreArchivo;

	private EnumReporteRESU(String id, String nombreArchivo) {
		this.id = id;
		this.nombreArchivo = nombreArchivo;
	}

	public static EnumReporteRESU obtenerInformacionReporteRESU(String codigo) {
		EnumReporteRESU enumResultado = null;
		for (EnumReporteRESU d : EnumReporteRESU.values()) {
			if (d.getId().equals(codigo)) {
				enumResultado = d;
				break;
			}
		}
		return enumResultado;
	}

	public String getId() {
		return id;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

}
