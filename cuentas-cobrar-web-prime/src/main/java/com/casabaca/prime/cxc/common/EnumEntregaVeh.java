package com.casabaca.prime.cxc.common;

public enum EnumEntregaVeh {

	INGRESADA("I"), LLEGADA("L"), CONFIRMADA("C"), APROBADA("A"), TIPO_ENTREGA("E"), TIPO_SER("S"), TIPO_VEH("V"), 
	OPCION_SI("S"), OPCION_NO("N"), OPCION_NA("O"), LINEA_VEH_NUEVOS("3"), GRUPO_MAIL("GM"), LINEA_EXONERADOS("70"),
	ESTADO_ASIGNADO("ASIGNADO"), ESTADO_ARRIBO("ARRIBO"), ESTADO_EN_PROCESO("EN PROCESO"), ESTADO_CONFIRMADO("CONFIRMADO");

	private final String codigo;

	EnumEntregaVeh(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}

}