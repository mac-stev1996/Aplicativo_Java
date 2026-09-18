package com.casabaca.cxc.aval.buro.web.rest.api.client.impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import com.casabaca.common.FechaUtils;
import com.casabaca.common.ejb.model.ParamDet;
import com.casabaca.common.ejb.service.ParamDetServiceLocal;
import com.casabaca.common.ejb.util.NombreJNDI;
import com.casabaca.common.ejb.util.ServiceLocator;
import com.casabaca.cxc.aval.buro.web.rest.api.client.ServiciosRestCxc;
import com.casabaca.cxc.ejb.modelo.CxcAvalBuro;
import com.casabaca.cxc.ejb.servicio.CxcAvalBuroServiceLocal;
import com.casabaca.exception.FindException;
import com.casabaca.exception.ServiceLocatorException;
import com.casabaca.prime.cxc.consultas.avalburo.dto.CxcAvalBuroJson;
import com.casabaca.prime.cxc.consultas.avalburo.dto.RequestConsultaExterna;
import com.casabaca.prime.cxc.consultas.avalburo.dto.RespuestaResumenCertero;
import com.casabaca.prime.cxc.consultas.avalburo.dto.RespuestaWsAbalBuro;
import com.casabaca.prime.cxc.consultas.avalburo.dto.Saldos;
import com.casabaca.rest.wsAvalBuro.entidad.ConsultaResponse;
import com.casabaca.rest.wsAvalBuro.entidad.DeudaVigenteTotal;
import com.casabaca.rest.wsAvalBuro.entidad.FactoresScore;
import com.casabaca.webservice.wsAvalBuroImpl.MainEjecutarServicioConsultaAvalBuro;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Path("restAvalBuro")
public class ServiciosRestCxcImpl implements ServiciosRestCxc, Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ServiciosRestCxcImpl.class);
	private static final String COMPRASIGMA = "07";
	private static final String USUARIO = "LEAD_SOLUTIONS";
	
	ConsultaResponse consultaResponse;
	Gson gson = new Gson();
	private CxcAvalBuro existeCedula;
	private String descErrorWebServiceGlobal="SERVICIO INACTIVO";
	private CxcAvalBuroServiceLocal cxcAvalBuroServiceLocal;
	
	private ParamDetServiceLocal paramDetService;
		
	public ServiciosRestCxcImpl() {			
				
		try {
			cxcAvalBuroServiceLocal = (CxcAvalBuroServiceLocal) ServiceLocator
					.getService(com.casabaca.common.ejb.util.NombreJNDI.CXC_AVAL_BURO_SERVICE);
			paramDetService = (ParamDetServiceLocal) ServiceLocator
					.getService(NombreJNDI.PARAM_DET_SERVICE);	
		} catch (ServiceLocatorException e) {
			logger.error(e.getCause() + " - " + e.getMessage());
		}
	}

	@Override
	public Response ping() {		
		
		return Response.status(200).entity(gson.toJson("pong angelo *************")).build();
	}
	
	@Override
	public String getHtml() {
		
		String htmlContent = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<title>DATOS DEL VEHICULO</title>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }"
                + "h1 { color: #333366; }"
                + "p { color: #666666; }"
                + ".container { max-width: 100%; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
                + "@media (min-width: 768px) {"
                + "    .container { max-width: 800px; }"
                + "}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<h1>PLACA</h1>"
                + "<p>ABCD-123.</p>"
                + "<h1>MODELO</h1>"
                + "<p>TOYOTA YARIS 1.5 TM</p>"
                + "<h1>AÑO</h1>"
                + "<p>2023</p>"
                + "<h1>PRECIO</h1>"
                + "<p>$ 15600</p>"
                + "</div>"
                + "</body>"
                + "</html>";
		
		return htmlContent;
	}
	
	@Override
	public Response consultarAvalBuro(String tipoIdentificacion, String cedula) {
		if(consultarAvalBuroLocal(cedula)) {
			try {				
				CxcAvalBuroJson envioJson = new CxcAvalBuroJson(existeCedula.getCedula(), 
						existeCedula.getNombre(), 
						FechaUtils.formatearFecha(existeCedula.getFechaInicio(), "yyyy-MM-dd"), 
						FechaUtils.formatearFecha(existeCedula.getFechaFin(), "yyyy-MM-dd"),
						existeCedula.getUsuario(), 
						existeCedula.getJson(),
						existeCedula.getJsonV2());	
				
				
				Date fechaActual = new Date();
				LocalDateTime fecActual = fechaActual.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();								
				LocalDateTime fecFinalDatos = existeCedula.getFechaFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().plusDays(60);							
						
				if(fecActual.isAfter(fecFinalDatos) || fecActual.isEqual(fecFinalDatos)) {
					return Response.status(404).entity("Aval no encontrado").build();
				} else {
					// Si los datos son validos
					return Response.status(200).entity(envioJson).build();
				}
					
			} catch (JsonSyntaxException e) {
	            // Maneja la excepción si la cadena JSON no es válida
	            logger.error("Error al convertir la cadena JSON: " + e.getMessage());
	            return Response.status(500).entity("Error interno del servidor").build();
	        }
	    } else {
	        // Devuelve un código 404 si el aval no existe localmente
	        return Response.status(404).entity("Aval no encontrado").build();
	    }
	
	}

	
	public Boolean consultarAvalBuroLocal(String cedula) {
		Boolean existe = Boolean.FALSE;
		try {
			existeCedula = cxcAvalBuroServiceLocal.consultarJsonAvalBuro(cedula);	
			if (existeCedula != null) {				
				existe = Boolean.TRUE;
			} else {
				existe = Boolean.FALSE;
			}			
		} catch (FindException e) {			
			logger.error(e.getCause() + " - " + e.getMessage());
		}		
		return existe;
	}

	@Override
	public Response guardarClienteAvalBuro(CxcAvalBuroJson cxcAvalBuroJson) {
		try {
			logger.info(" Entro al servicio guardarClienteAvalBuro: " + cxcAvalBuroJson.getNombre());
			CxcAvalBuro cxcAvalBuro = cxcAvalBuroServiceLocal.consultarJsonAvalBuro(cxcAvalBuroJson.getCedula());
			if (cxcAvalBuro == null) {
				cxcAvalBuro = new CxcAvalBuro();
			}
			cxcAvalBuro.setCedula(cxcAvalBuroJson.getCedula());
			cxcAvalBuro.setFechaFin(FechaUtils.convertirStrFecha(cxcAvalBuroJson.getFechaFin(), "yyyy-MM-dd"));
			cxcAvalBuro.setFechaInicio(FechaUtils.convertirStrFecha(cxcAvalBuroJson.getFechaInicio(), "yyyy-MM-dd"));
			cxcAvalBuro.setNombre(cxcAvalBuroJson.getNombre());
			cxcAvalBuro.setUsuario(cxcAvalBuroJson.getUsuario());
			if (cxcAvalBuroJson.getJson() != null && !cxcAvalBuroJson.getJson().trim().isEmpty()) {
				cxcAvalBuro.setJson(cxcAvalBuroJson.getJson());
			}
			if (cxcAvalBuroJson.getJsonV2() != null && !cxcAvalBuroJson.getJsonV2().trim().isEmpty()) {
				cxcAvalBuro.setJsonV2(cxcAvalBuroJson.getJsonV2());
			}
			cxcAvalBuroServiceLocal.grabarJsonAvalBuro(cxcAvalBuro);
            return Response.status(Response.Status.CREATED).entity(cxcAvalBuro).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al guardar la información").build();
        }
						
	}

	@Override
	public RespuestaWsAbalBuro consultarAvalBuroExterno(RequestConsultaExterna request, String authString) {
		RespuestaWsAbalBuro respuestaConsulta = new RespuestaWsAbalBuro();
		respuestaConsulta.setCodigoMensaje("1");
		respuestaConsulta.setNomMensaje("ERROR");
		respuestaConsulta.setDescripcionMensaje(descErrorWebServiceGlobal);
		respuestaConsulta.setCedula(null);
		respuestaConsulta.setNombre(null);
		respuestaConsulta.setFechaInicio(null);
		respuestaConsulta.setFechaFin(null);
		respuestaConsulta.setJson(null);
		respuestaConsulta.setUsuario(null);
		logger.info(" Autorizacion: " + authString);
		if(authString!=null && !authString.equals("")) {
			if(esUsuarioAutenticado(authString,COMPRASIGMA)){						
				return datosClienteAvalBuro(COMPRASIGMA, request, respuestaConsulta);
	        } 
		}		
		return respuestaConsulta;
	
	}

	private RespuestaWsAbalBuro datosClienteAvalBuro(String noCia, RequestConsultaExterna request, RespuestaWsAbalBuro respuestaConsulta) {
		Date fechaActual = new Date();
		LocalDateTime fecActual = fechaActual.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime fecFinalDatos = fecActual.plusDays(cargarDiasValidosAvalBuro());			
		Date fechaFinDatosValidos = Date.from(fecFinalDatos.atZone(ZoneId.systemDefault()).toInstant());
		
		String errorNoExistenDatos = "";
		
		if(consultarAvalBuroLocal(request.getIdentificacion())) {
			
			LocalDateTime fecFinDb = existeCedula.getFechaFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			
			if(fecActual.isAfter(fecFinDb) || fecActual.isEqual(fecFinDb)) {
				
				consultaAvalBuro(noCia, request, respuestaConsulta, fechaActual, fechaFinDatosValidos);
								
			} else {
				
				consultaResponse = transformarResponse(existeCedula.getJson());
				
				Object[] validacionLocal = obtenerValidacionConsultaClienteLocal(consultaResponse, request);
							
				errorNoExistenDatos = validacionLocal[1].toString();			
				
				respuestaConsulta.setCodigoMensaje(errorNoExistenDatos);
				respuestaConsulta.setNomMensaje("1".equals(errorNoExistenDatos) ? "SERVICIOS ACTIVO":"ERROR EN CONSULTA DE DATOS");
				respuestaConsulta.setDescripcionMensaje("1".equals(errorNoExistenDatos) ? "Consulta Exitosa" : "NO EXISTEN DATOS CREDITICIOS");
				respuestaConsulta.setCedula(request.getIdentificacion());
				respuestaConsulta.setNombre(existeCedula.getNombre());
				respuestaConsulta.setFechaInicio(FechaUtils.formatearFecha(existeCedula.getFechaInicio(), "yyyy-MM-dd"));
				respuestaConsulta.setFechaFin(FechaUtils.formatearFecha(existeCedula.getFechaFin(), "yyyy-MM-dd"));
				respuestaConsulta.setUsuario(USUARIO);
				respuestaConsulta.setJson("1".equals(errorNoExistenDatos) ? existeCedula.getJson() : "");
				generarResumenCertero(errorNoExistenDatos, respuestaConsulta);
				
			}						
						
		} else {
			
			consultaAvalBuro(noCia, request, respuestaConsulta, fechaActual, fechaFinDatosValidos);
						
		}
		return respuestaConsulta;
	}
	
	private void consultaAvalBuro(String noCia, RequestConsultaExterna request, RespuestaWsAbalBuro respuestaConsulta,
			Date fechaActual, Date fechaFinDatosValidos) {
		LocalDateTime fecActual;
		String errorNoExistenDatos;
		//Se consulta directamente en Aval Buro
		MainEjecutarServicioConsultaAvalBuro consultaAvalBuro = new MainEjecutarServicioConsultaAvalBuro();		
		consultaResponse = consultaAvalBuro.consultarAvalBuro(noCia, request.getTipoCliente(), request.getIdentificacion());
		
		if("A200".equals(consultaResponse.getResponseCode())) {
			
			Object[] validacion = obtenerValidacionConsultaCliente(consultaResponse, request, fechaActual, fechaFinDatosValidos);
			
			String nombreSujeto = validacion[0].toString();
			errorNoExistenDatos = validacion[1].toString();												
			
			fecActual = fechaActual.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();	        			        		
			Date fechaInicioDatosValidos = Date.from(fecActual.atZone(ZoneId.systemDefault()).toInstant());		
			String formatoJson = "yyyy-MM-dd";
			SimpleDateFormat sdfJson = new SimpleDateFormat(formatoJson);
			String fechaJsonIni = sdfJson.format(fechaInicioDatosValidos);
			String fechaJsonFin = sdfJson.format(fechaFinDatosValidos);
		
			respuestaConsulta.setCodigoMensaje(errorNoExistenDatos);
			respuestaConsulta.setNomMensaje("1".equals(errorNoExistenDatos) ? "SERVICIOS ACTIVO":"ERROR EN CONSULTA DE DATOS");
			respuestaConsulta.setDescripcionMensaje("1".equals(errorNoExistenDatos) ? "Consulta Exitosa" : "NO EXISTEN DATOS CREDITICIOS");
			respuestaConsulta.setCedula(request.getIdentificacion());
			respuestaConsulta.setNombre(nombreSujeto);
			respuestaConsulta.setFechaInicio(fechaJsonIni);
			respuestaConsulta.setFechaFin(fechaJsonFin);
			respuestaConsulta.setUsuario(USUARIO);
			respuestaConsulta.setJson("1".equals(errorNoExistenDatos) ? gson.toJson(consultaResponse) : "");
			generarResumenCertero(errorNoExistenDatos, respuestaConsulta);
			
			guardarClienteAvalBuro(new CxcAvalBuroJson(request.getIdentificacion(), nombreSujeto, fechaJsonIni,
					fechaJsonFin, USUARIO, gson.toJson(consultaResponse)));
			
		}
	}

	private ConsultaResponse transformarResponse(String json) {				 
		Gson gsonParse = new Gson();
		// Verificamos si jsonString está entre comillas, lo que indicaría que es un string serializado
		if (json.startsWith("\"") && json.endsWith("\"")) {
		    // Elimina las comillas iniciales y finales, y desescapa el contenido
			json = json.substring(1, json.length() - 1).replace("\\\"", "\"");
		}
		JsonObject jsonObject = gsonParse.fromJson(json, JsonObject.class);
		return gsonParse.fromJson(jsonObject, ConsultaResponse.class);
	}

	private void llenarDatosBuro(String identificacion, String nombreSujeto, Date fechaActual, Date fechaFinDatos, String usuario, String json) {
		try {
			CxcAvalBuro cxcAvalBuro = cxcAvalBuroServiceLocal.consultarJsonAvalBuro(identificacion);
			if (cxcAvalBuro == null) {
				cxcAvalBuro = new CxcAvalBuro();
				cxcAvalBuro.setCedula(identificacion);
			}

			cxcAvalBuro.setNombre(nombreSujeto);
			cxcAvalBuro.setFechaInicio(fechaActual);
			cxcAvalBuro.setFechaFin(fechaFinDatos);
			cxcAvalBuro.setUsuario(usuario);
			cxcAvalBuro.setJson(gson.toJson(json));

			cxcAvalBuroServiceLocal.grabarJsonAvalBuro(cxcAvalBuro);
		} catch (Exception e) {
			logger.error("Error al guardar datos de aval buro preservando JSON_V2", e);
		}
	}
	
	private Object[] obtenerValidacionConsultaCliente(ConsultaResponse response, RequestConsultaExterna request, Date fechaActual, Date fechaFinDatosValidos) {
		Object[] respuesta = new Object[2];
		if ("C".equals(request.getTipoCliente())){
			if(!response.getResult().getIdentificacionTitular().isEmpty()) {
				respuesta[0] = response.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
				respuesta[1] = "1";
				llenarDatosBuro(request.getIdentificacion(), respuesta[0].toString(), fechaActual, fechaFinDatosValidos, USUARIO, gson.toJson(response));
			} else {
				respuesta[0] = "";
				respuesta[1] = "2";
			}																	
		} else if("R".equals(request.getTipoCliente())) {
			if(response.getResult().getDatosGeneralesEmpresa().isEmpty() && response.getResult().getIdentificacionTitular().isEmpty()) {
				respuesta[0] = "";
				respuesta[1] = "2";
			} else {
				if(response.getResult().getDatosGeneralesEmpresa() == null || response.getResult().getDatosGeneralesEmpresa().isEmpty()) {
					respuesta[0] = response.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
					respuesta[1] = "1";
				} else {
					respuesta[0] = response.getResult().getDatosGeneralesEmpresa().get(0).getNombreRazonSocial();
					respuesta[1] = "1";
				}
				llenarDatosBuro(request.getIdentificacion(), respuesta[0].toString(), fechaActual, fechaFinDatosValidos, USUARIO, gson.toJson(response));
			}													
		} 	
		return respuesta;
	}
	
	private Object[] obtenerValidacionConsultaClienteLocal(ConsultaResponse response, RequestConsultaExterna request) {
		Object[] respuesta = new Object[2];
		if ("C".equals(request.getTipoCliente())){
			if(!response.getResult().getIdentificacionTitular().isEmpty()) {
				respuesta[0] = response.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
				respuesta[1] = "1";				
			} else {
				respuesta[0] = "";
				respuesta[1] = "2";
			}																	
		} else if("R".equals(request.getTipoCliente())) {
			if(response.getResult().getDatosGeneralesEmpresa().isEmpty() && response.getResult().getIdentificacionTitular().isEmpty()) {
				respuesta[0] = "";
				respuesta[1] = "2";
			} else {
				if(response.getResult().getDatosGeneralesEmpresa() == null || response.getResult().getDatosGeneralesEmpresa().isEmpty()) {
					respuesta[0] = response.getResult().getIdentificacionTitular().get(0).getNombreRazonSocial();
					respuesta[1] = "1";
				} else {
					respuesta[0] = response.getResult().getDatosGeneralesEmpresa().get(0).getNombreRazonSocial();
					respuesta[1] = "1";
				}				
			}													
		} 	
		return respuesta;
	}
	
	private void generarResumenCertero(String errorNoExistenDatos, RespuestaWsAbalBuro respuestaConsulta) {
		ConsultaResponse consultaResponse;
		if("2".equals(errorNoExistenDatos)) {
			respuestaConsulta.setResumen("");
		}  else {

			consultaResponse = validarJson(respuestaConsulta);
			
			RespuestaResumenCertero respuestaCertero = new RespuestaResumenCertero();
			if(!isNullOrEmpty(consultaResponse.getResult().getIdentificacionTitular())) {
				respuestaCertero.setIdentificacionTitular(consultaResponse.getResult().getIdentificacionTitular().get(0));
			}	
			
			if(!isNullOrEmpty(consultaResponse.getResult().getDatosGeneralesEmpresa())) {
				respuestaCertero.setDatosGeneralesEmpresa(consultaResponse.getResult().getDatosGeneralesEmpresa().get(0));
			}
			
			if(!isNullOrEmpty(consultaResponse.getResult().getScoreFinanciero())) {
				respuestaCertero.setScoreFinanciero(consultaResponse.getResult().getScoreFinanciero().get(0));		
			}
			
			if(!isNullOrEmpty(consultaResponse.getResult().getScoreEmpresa() )) {
				respuestaCertero.setScoreEmpresa(consultaResponse.getResult().getScoreEmpresa().get(0));	
			}
			
			if(!isNullOrEmpty(consultaResponse.getResult().getFactoresScore())) {
				
				List<FactoresScore> lstFactoresScore = consultaResponse.getResult().getFactoresScore();
				List<FactoresScore> lstFactoresScoreFiltro = new ArrayList<FactoresScore>();
				for (FactoresScore factoresScore : lstFactoresScore) {
					if (factoresScore.getFactor().contains("operaciones actuales") || factoresScore.getFactor().contains("operaciones 36 meses") || factoresScore.getFactor().contains("operaciones que registra vencidos")) {
						lstFactoresScoreFiltro.add(factoresScore);
					}
				}			
				respuestaCertero.setLstFactoresScore(lstFactoresScore);
			}
			
			seteoDatosResumenCertero(consultaResponse, respuestaCertero);	
					
			respuestaConsulta.setResumen(gson.toJson(respuestaCertero));
		}
	}

	private ConsultaResponse validarJson(RespuestaWsAbalBuro respuestaConsulta) {
		ConsultaResponse consultaResponse;
		String jsonString = respuestaConsulta.getJson();		 
		// Verificamos si jsonString está entre comillas, lo que indicaría que es un string serializado
		if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
		    // Elimina las comillas iniciales y finales, y desescapa el contenido
		    jsonString = jsonString.substring(1, jsonString.length() - 1).replace("\\\"", "\"");
		}
		JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);				
		consultaResponse = gson.fromJson(jsonObject, ConsultaResponse.class);
		return consultaResponse;
	}
	
	private void seteoDatosResumenCertero(ConsultaResponse consultaResponse, RespuestaResumenCertero respuestaCertero) {
		if(!isNullOrEmpty(consultaResponse.getResult().getIndicadoresDeuda())) {
			List<DeudaVigenteTotal> lstDeudaVigenteTotal = consultaResponse.getResult().getDeudaVigenteTotal();
			Saldos saldosCliente = new Saldos();
			BigDecimal totalValorPorVencer = lstDeudaVigenteTotal.stream().map(DeudaVigenteTotal::getValorPorVencer).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal totalValorNoDevengaIntereses = lstDeudaVigenteTotal.stream().map(DeudaVigenteTotal::getNoDevengaIntereses).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal totalValorVencido = lstDeudaVigenteTotal.stream().map(DeudaVigenteTotal::getValorVencido).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal totalValorDemandaJudicial = lstDeudaVigenteTotal.stream().map(DeudaVigenteTotal::getValorDemandaJudicial).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal totalValorCarteraCastigada = lstDeudaVigenteTotal.stream().map(DeudaVigenteTotal::getCarteraCastigada).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal totalTotalDecuda = lstDeudaVigenteTotal.stream().map(DeudaVigenteTotal::getTotalDeuda).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
			
			saldosCliente.setValorPorVencer(totalValorPorVencer.setScale(2, RoundingMode.HALF_UP));
			saldosCliente.setValorNoDevengaIntereses(totalValorNoDevengaIntereses.setScale(2, RoundingMode.HALF_UP));			
			saldosCliente.setValorVencido(totalValorVencido.setScale(2, RoundingMode.HALF_UP));			
			saldosCliente.setValorDemandaJudicial(totalValorDemandaJudicial.setScale(2, RoundingMode.HALF_UP));			
			saldosCliente.setValorCarteraCastida(totalValorCarteraCastigada.setScale(2, RoundingMode.HALF_UP));			
			saldosCliente.setValorTotalDeuda(totalTotalDecuda.setScale(2, RoundingMode.HALF_UP));
		
			respuestaCertero.setSaldosCliente(saldosCliente);
		}
	}
	
	//Se incluye autenticaci�n decodificada en Base64.
	private boolean esUsuarioAutenticado(String authString,String noCia) {
		String user = "";
		String passwd = "";
		String decodedAuth = "";
		String[] authParts = authString.split("\\s+");
		String authInfo = authParts[1];
		byte[] bytes = null;		 
		ParamDet objValidaWs = validaAccesoWs(noCia);		
		bytes = Base64.decodeBase64(authInfo.toString().getBytes());
		decodedAuth = new String(bytes);
		//Valido existencia de credenciales en param det
		if(objValidaWs!=null) {
			user=objValidaWs.getTexto1();
			passwd=objValidaWs.getTexto2();
			if (decodedAuth.equals(user+":"+passwd)) {
				return true;
			} else {
				descErrorWebServiceGlobal = "AUTENTICACIÓN FALLIDA";
				return false;
			}
		}else {
			return false;
		}	
	}
	
	
	private ParamDet validaAccesoWs(String nocia) {
		String codParamDet = "AVBEXT";
		String codParamDetCodigoDet = "UP";
		 ParamDet objParamDet = null;	 
		try {
			objParamDet = paramDetService.findbyNoCiabyCodigobyCodigoDet(nocia, codParamDet, codParamDetCodigoDet);
			if (objParamDet == null) {			 
				descErrorWebServiceGlobal = "SERVICIO INACTIVO";
			}
		} catch (FindException e) {
			descErrorWebServiceGlobal = "SERVICIO INACTIVO";
			logger.error(e);
		}
		return objParamDet;
	}
	
	public int cargarDiasValidosAvalBuro() {
		List<ParamDet> diasValidos = paramDetService.buscarTiposIdentificacion(COMPRASIGMA, "DIASAB");
		int numeroDiasValidoAvalBuro = Integer.parseInt(diasValidos.get(0).getTexto1() != null ? diasValidos.get(0).getTexto1() : "0");
		return numeroDiasValidoAvalBuro;
	}

	public static <T> boolean isNullOrEmpty(List<T> lista) {
        return lista == null || lista.isEmpty();
    }



	
}
