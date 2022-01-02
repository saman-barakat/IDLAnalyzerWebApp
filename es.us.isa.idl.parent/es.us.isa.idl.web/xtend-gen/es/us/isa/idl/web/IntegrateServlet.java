package es.us.isa.idl.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;


import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;


import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;




/**
 * Servlet implementation class IntegrateServlet
 */
@WebServlet("/IntegrateServlet")
public class IntegrateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private OpenAPI openApiSpecification = null;

	/**
	 * Operation object.
	 */
	private Operation operation;


	private static final String X_DEPENDENCIES = "x-dependencies";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public IntegrateServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		

	}

	private HttpResponse<?> callAnalyzer(String analysisURI, String operationPath, String operationType,
			String parameter, String jsonOutput) throws IOException, InterruptedException, URISyntaxException {
	 

		HttpClient client = HttpClient.newBuilder().build();
		URI uri;
		if (parameter == "") {
			uri = new URIBuilder(analysisURI).addParameter("operationPath", operationPath)
					.addParameter("operationType", operationType).build();
		} else {
			uri = new URIBuilder(analysisURI).addParameter("operationPath", operationPath)
					.addParameter("operationType", operationType).addParameter("parameter", parameter).build();
		}

		HttpRequest request = HttpRequest.newBuilder().uri(uri)

				.POST(BodyPublishers.ofString(jsonOutput)).build();

		HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		return response;

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		 
		response.setContentType("text/plan");

		PrintWriter writer = response.getWriter();
		try {

			String analysisURI = request.getParameter("uri");
			String operationType = request.getParameter("operationType");
			String operationPath = request.getParameter("operationPath");
			String apiSpecification = request.getParameter("apiSpecification");
			String parameter = request.getParameter("parameter");
			String[] idl = request.getParameterValues("idl[]");

			List<String> newIDL = Arrays.asList(idl);

			ParseOptions parseOptions = new ParseOptions();
			parseOptions.setFlatten(true);

			openApiSpecification = new OpenAPIV3Parser().read(apiSpecification, null, parseOptions);

			if (openApiSpecification == null)
				throw new Exception("API cannot be loaded from path ");

			operation = getOasOperation(operationPath, operationType);
			if (operation == null)
				throw new Exception("Operation not found");

			if (operation.getExtensions() == null)
				operation.setExtensions(new HashMap<>());

			operation.getExtensions().put(X_DEPENDENCIES, newIDL);

			String jsonOutput = Yaml.pretty().writeValueAsString(openApiSpecification);
			// System.out.println(jsonOutput);
			HttpResponse<?> respons = callAnalyzer(analysisURI, operationPath, operationType, parameter, jsonOutput);

			writer.print(respons.body());

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new ServletException("An error has been occurred!");

		}

		writer.close();
	}

	private Operation getOasOperation(String operationPath, String operationType) throws Exception {
		PathItem item = this.openApiSpecification.getPaths().get(operationPath);
		if (item != null) {
			try {
				switch (operationType) {
				case "GET":
					return item.getGet();
				case "DELETE":
					return item.getDelete();
				case "HEAD":
					return item.getHead();
				case "OPTIONS":
					return item.getOptions();
				case "PATCH":
					return item.getPatch();
				case "POST":
					return item.getPost();
				case "PUT":
					return item.getPut();
				default:
					return null;
				}
			} catch (Exception e) {

				return null;
			}
		} else {

			return null;
		}
	}

}
