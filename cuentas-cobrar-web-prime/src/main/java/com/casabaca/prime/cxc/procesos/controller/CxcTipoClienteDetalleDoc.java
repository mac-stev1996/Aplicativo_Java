package com.casabaca.prime.cxc.procesos.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.casabaca.common.ExcelUtils;
import com.casabaca.common.ejb.model.Cliente;
import com.casabaca.common.ejb.service.ClienteServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.cxc.cml.model.CxcTipoCliente;
import com.casabaca.cxc.cml.model.CxcTipoclienteDetalle;
import com.casabaca.cxc.cml.model.CxcTipoclienteDetallePK;
import com.casabaca.exception.FindException;

@Stateless
public class CxcTipoClienteDetalleDoc {

	private static final Logger LOG = Logger.getLogger(CxcTipoClienteDetalleDoc.class);

	@EJB(lookup = NombreJNDI.CLIENTE_SERVICE)
	private ClienteServiceLocal clienteService;

	private static final int COL_TIPO_CLIENTE = 0;
	private static final int COL_NO_LINEA = 1;
	private static final int COL_IDENTIFICACION = 2;
	private static final int COL_ESTADO = 3;
	private static final int COL_TIPO_CONOZCA = 4;
	private static final int TOTAL_COLUMNAS = 5;

	private static final String[] ENCABEZADOS = { "TIPO_CLIENTE", "NO_LINEA", "IDENTIFICACION", "ESTADO",
			"TIPO_CONOZCA" };

	private static final List<String> ESTADOS_VALIDOS = java.util.Arrays.asList("A", "I");
	private static final List<String> TIPO_CONOZCA_VALIDOS = java.util.Arrays.asList("F", "N", "J");

	public static class ResultadoCarga {
		private final List<CxcTipoclienteDetalle> registros = new ArrayList<>();
		private final List<String> errores = new ArrayList<>();

		public boolean tieneErrores() {
			return !errores.isEmpty();
		}

		public boolean tieneRegistros() {
			return !registros.isEmpty();
		}

		public List<CxcTipoclienteDetalle> getRegistros() {
			return registros;
		}

		public List<String> getErrores() {
			return errores;
		}
	}

	/**
	 * Genera el archivo .xlsx con solo los encabezados.
	 */
	public byte[] generarPlantilla() throws IOException {
		XSSFWorkbook workbook = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("TipoClienteDetalle");

			CellStyle estiloEncabezado = workbook.createCellStyle();
			estiloEncabezado.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
			estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);

			Font fuente = workbook.createFont();
			fuente.setBoldweight(Font.BOLDWEIGHT_BOLD);
			fuente.setColor(IndexedColors.WHITE.getIndex());
			estiloEncabezado.setFont(fuente);

			XSSFRow fila = sheet.createRow(0);
			for (int i = 0; i < ENCABEZADOS.length; i++) {
				XSSFCell celda = fila.createCell(i);
				celda.setCellValue(ENCABEZADOS[i]);
				celda.setCellStyle(estiloEncabezado);
				sheet.setColumnWidth(i, 5000);
			}

			workbook.write(out);
			return out.toByteArray();

		} finally {
			out.close();
		}
	}

	/**
	 * Lee el archivo subido, valida cada fila y retorna el resultado. Continúa
	 * aunque haya errores — los reporta al final.
	 *
	 * @param inputStream  stream del archivo subido
	 * @param noCia        compañía del usuario logueado
	 * @param usuarioCrea  usuario logueado
	 * @param tiposValidos lista cargada una vez en el controller — validación en
	 *                     memoria
	 * @throws FindException
	 */
	public ResultadoCarga parsearArchivo(InputStream inputStream, String noCia, String usuarioCrea,
			List<CxcTipoCliente> tiposValidos) throws IOException, FindException {

		ResultadoCarga resultado = new ResultadoCarga();
		List<Object[]> filas = ExcelUtils.leerExcel(inputStream, true, true, 0);

		if (filas == null || filas.isEmpty()) {
			resultado.getErrores().add("El archivo no contiene datos.");
			return resultado;
		}

		int numFila = 2;
		for (Object[] fila : filas) {

			if (esFilaVacia(fila)) {
				numFila++;
				continue;
			}

			if (fila.length < TOTAL_COLUMNAS) {
				resultado.getErrores().add("Fila " + numFila + ": se esperaban " + TOTAL_COLUMNAS
						+ " columnas, se encontraron " + fila.length + ".");
				numFila++;
				continue;
			}

			List<String> erroresFila = validarFila(fila, noCia, tiposValidos);
			if (!erroresFila.isEmpty()) {
				resultado.getErrores().add("Fila " + numFila + ": " + String.join(", ", erroresFila));
				numFila++;
				continue;
			}

			resultado.getRegistros().add(construirDetalle(fila, noCia, usuarioCrea, tiposValidos));
			numFila++;
		}

		LOG.info("Excel parseado: " + resultado.getRegistros().size() + " registros válidos, "
				+ resultado.getErrores().size() + " errores.");
		return resultado;
	}

	private List<String> validarFila(Object[] fila, String noCia, List<CxcTipoCliente> tiposValidos)
			throws FindException {

		List<String> errores = new ArrayList<>();

		String tipoCliente = obtenerTexto(fila[COL_TIPO_CLIENTE]);
		String noLineaStr = obtenerTexto(fila[COL_NO_LINEA]);
		String identificacion = obtenerTexto(fila[COL_IDENTIFICACION]);
		String estado = obtenerTexto(fila[COL_ESTADO]);
		String tipoConozca = obtenerTexto(fila[COL_TIPO_CONOZCA]);

		if (tipoCliente == null || tipoCliente.isEmpty())
			errores.add("TIPO_CLIENTE es requerido");
		if (noLineaStr == null || noLineaStr.isEmpty())
			errores.add("NO_LINEA es requerido");
		if (identificacion == null || identificacion.isEmpty())
			errores.add("IDENTIFICACION es requerida");
		if (estado == null || estado.isEmpty())
			errores.add("ESTADO es requerido");
		if (tipoConozca == null || tipoConozca.isEmpty())
			errores.add("TIPO_CONOZCA es requerido");

		if (!errores.isEmpty())
			return errores;

		if (!ESTADOS_VALIDOS.contains(estado.toUpperCase()))
			errores.add("ESTADO '" + estado + "' no válido, debe ser A o I");

		if (!TIPO_CONOZCA_VALIDOS.contains(tipoConozca.toUpperCase()))
			errores.add("TIPO_CONOZCA '" + tipoConozca + "' no válido, debe ser F, N o J");

		Long noLinea = null;
		try {
			noLinea = Long.valueOf(noLineaStr.replace(".0", "").trim());
		} catch (NumberFormatException e) {
			errores.add("NO_LINEA '" + noLineaStr + "' no es un número válido");
		}

		if (tiposValidos == null || tiposValidos.isEmpty()) {
			errores.add("No hay tipos de cliente disponibles para validar");
		} else {
			final String tipoBuscar = tipoCliente.toUpperCase();
			final Long lineaBuscar = noLinea;
			boolean tipoExiste = tiposValidos.stream()
					.anyMatch(tc -> tc.getId().getTipoCliente().equalsIgnoreCase(tipoBuscar)
							&& tc.getId().getNoLinea().equals(lineaBuscar));
			if (!tipoExiste) {
				errores.add("TIPO_CLIENTE '" + tipoCliente + "' con NO_LINEA '" + noLineaStr + "' no existe");
			}
		}

		Cliente cliente = clienteService.findByCedulaNoCia(identificacion, noCia);
		if (cliente == null) {
			errores.add("IDENTIFICACION '" + identificacion + "' no existe como cliente registrado");
		}

		return errores;
	}

	/**
	 * Solo se llama cuando la fila ya fue validada exitosamente. El nombre se toma
	 * del cliente encontrado en ARCCM — no viene del Excel.
	 * 
	 * @throws FindException
	 */
	private CxcTipoclienteDetalle construirDetalle(Object[] fila, String noCia, String usuarioCrea,
			List<CxcTipoCliente> tiposValidos) throws FindException {

		String tipoClienteStr = obtenerTexto(fila[COL_TIPO_CLIENTE]).toUpperCase();
		Long noLinea = Long.valueOf(obtenerTexto(fila[COL_NO_LINEA]).replace(".0", "").trim());
		String identificacion = obtenerTexto(fila[COL_IDENTIFICACION]);
		String estado = obtenerTexto(fila[COL_ESTADO]).toUpperCase();
		String tipoConozca = obtenerTexto(fila[COL_TIPO_CONOZCA]).toUpperCase();

		Cliente cliente = clienteService.findByCedulaNoCia(identificacion, noCia);

		CxcTipoclienteDetalle detalle = new CxcTipoclienteDetalle(new CxcTipoclienteDetallePK());
		detalle.getId().setNoCia(noCia);
		detalle.getId().setTipoCliente(tipoClienteStr);
		detalle.getId().setNoLinea(noLinea);
		detalle.getId().setIdentificacion(identificacion);
		detalle.setNombre(cliente.getNombre());
		detalle.setEstado(estado);
		detalle.setTipoConozca(tipoConozca);
		detalle.setFechaCrea(new Date());
		detalle.setUsuarioCrea(usuarioCrea);

		return detalle;
	}

	private static boolean esFilaVacia(Object[] fila) {
		if (fila == null)
			return true;
		for (Object celda : fila) {
			if (celda != null && !celda.toString().trim().isEmpty())
				return false;
		}
		return true;
	}

	private static String obtenerTexto(Object valor) {
		if (valor == null)
			return null;

		String texto;

		if (valor instanceof Double) {
			texto = new java.math.BigDecimal(valor.toString()).toPlainString();
			if (texto.contains(".")) {
				texto = texto.replaceAll("\\.0*$", "");
			}
		} else {
			texto = valor.toString().trim();
			if (texto.endsWith(".0")) {
				texto = texto.substring(0, texto.length() - 2);
			}
		}

		return texto.isEmpty() ? null : texto;
	}

}