package com.casabaca.cxc.aval.buro.web.rest.api.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.casabaca.prime.cxc.consultas.avalburo.dto.CxcAvalBuroJson;
import com.casabaca.prime.cxc.consultas.avalburo.dto.RequestConsultaExterna;
import com.casabaca.prime.cxc.consultas.avalburo.dto.RespuestaWsAbalBuro;

@Path("restAvalBuro")
public interface ServiciosRestCxc{

	@GET
	@Path("/ping")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response ping();
	
	@GET
	@Path("/html")
    @Produces(MediaType.TEXT_HTML)
    public String getHtml();
	
	@GET
	@Path("/consultaravalburo")	
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response consultarAvalBuro(@QueryParam(value = "tipoIdentificacion") final String tipoIdentificacion,
									@QueryParam(value = "cedula") final String cedula);
	
	@POST
	@Path("/guardarclienteavalburo")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public Response guardarClienteAvalBuro(CxcAvalBuroJson cxcAvalBuro);
	
	@GET
	@Path("/consultaAvalBuroCentric")	
	@Produces(MediaType.APPLICATION_JSON)
	public RespuestaWsAbalBuro consultarAvalBuroExterno(
			RequestConsultaExterna request,
			@HeaderParam("authorization") String authString);
			
	
	
}
