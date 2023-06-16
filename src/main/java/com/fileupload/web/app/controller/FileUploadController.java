package com.fileupload.web.app.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileupload.web.app.model.CuadroClasificacionDataSets;
import com.fileupload.web.app.model.RootObject;
import com.fileupload.web.app.model.TCredentials;
import com.fileupload.web.app.repository.CredentialsRepository;
import com.fileupload.web.app.security.JwtUtils;
import com.fileupload.web.app.validator.RequestValidator;

import io.swagger.annotations.Api;

@Api(description = "Servicio para comunicar aplicaciones externas con el Gestor Documental del IVACE.", tags = "API de comunicacion con Alfresco")
@Controller
public class FileUploadController {

	@Autowired
	CredentialsRepository credRepo;

	Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	@Autowired
	CuadroClasificacionDataSets cuadroClasificacion;

	@Autowired
	RequestValidator validator;

	@Value("${alfresco.user}")
	private String user;
	@Value("${alfresco.pass}")
	private String pass;
	@Value("${alfresco.url}")
	private String url;
	@Value("${alfresco.documentLibrary}")
	private String documentLibrary;

	@PostMapping("/uploadFile/{codArea}/{codAnio}/{codConvocatoria}/{codExpediente}/{codProceso}/{codDocumentacion}")
	@ResponseBody
	public ResponseEntity<String> uploadToAlfresco(@RequestParam("file") MultipartFile file,
			@PathVariable("codArea") String codArea, @PathVariable("codAnio") String codAnio,
			@PathVariable("codConvocatoria") String codConvocatoria,
			@PathVariable("codExpediente") String codExpediente, @PathVariable("codProceso") String codProceso,
			@PathVariable("codDocumentacion") String codDocumentacion,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "gustavoId", required = false) String gustavoId,
			@RequestHeader(value = "ulisesId", required = false) String ulisesId) {

		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		try {
			String[] metadata = new String[6];
			metadata[0] = codArea;
			metadata[1] = codAnio;
			metadata[2] = codConvocatoria;
			metadata[3] = codExpediente;
			metadata[4] = codProceso;
			metadata[5] = codDocumentacion;
			String documentDestination;
			documentDestination = "Sites/ivace/documentLibrary/" + codArea + "/" + codAnio + "/" + codConvocatoria + "/"
					+ codExpediente + "/" + codProceso + "/" + codDocumentacion;

			if (!validator.isValidRequest(metadata)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}

			byte[] fileContent = file.getBytes();
			Boolean fileExists = false;

			// Configuraciones básicas para para conectarse
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();

			// Credenciales del usuario y url de conexión
			parameter.put(SessionParameter.USER, user);
			parameter.put(SessionParameter.PASSWORD, pass);
			parameter.put(SessionParameter.ATOMPUB_URL, url);
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

			// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
			Session session = factory.getRepositories(parameter).get(0).createSession();
			Folder root = session.getRootFolder();

			// Creamos las carpetas, pueden ser una o 50
			Folder parent = root;
			String[] parts = null;
			if (documentDestination.contains("/")) {
				parts = documentDestination.split("/");
			} else {
				parts = new String[1];
				parts[0] = documentDestination;
			}

			for (String folderName : parts) {
				parent = createFolder(folderName, parent, "", "");
			}

			// Creamos el archivo si no existe
			for (CmisObject r : parent.getChildren()) {
				if (r.getName().equals(file.getOriginalFilename())) {
					fileExists = true;
				}
			}

			// Devolvemos false si el archivo ya existe
			if (fileExists) {
				System.out.println("Ya hay un archivo con ese nombre");
				return ResponseEntity.badRequest().body("Ya hay un archivo con ese nombre");
			}

			// Si el archivo no existe en ese directorio lo creamos.
			Map<String, String> properties2 = new HashMap<String, String>();
			properties2.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			properties2.put(PropertyIds.NAME, file.getOriginalFilename());

			InputStream stream = new ByteArrayInputStream(fileContent);
			ContentStream contentStream = new ContentStreamImpl(file.getOriginalFilename(),
					BigInteger.valueOf(fileContent.length), "text/plain", stream);

			// Creamos el documento en el Alfresco
			CmisObject o = parent.createDocument(properties2, contentStream, VersioningState.MAJOR);
			// Check gustavo/ulises UniqueConstraint
			String constraintViolationFound = "";
			try {
				if (gustavoId != "" && gustavoId != null) {
					constraintViolationFound = find((Folder) root, Integer.parseInt(gustavoId), 0);
					if (constraintViolationFound != "") {
						logger.info("Could not set GustavoID - Constraint violation found.");
						return ResponseEntity.status(HttpStatus.OK).build();
					}
				}
				if (ulisesId != "" && ulisesId != null) {
					constraintViolationFound = find((Folder) root, Integer.parseInt(ulisesId), 0);
					if (constraintViolationFound != "") {
						logger.info("Could not set UlisesID - Constraint violation found.");
						return ResponseEntity.status(HttpStatus.OK).build();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (gustavoId != null) {
					properties2.put("ids:gustavoID", gustavoId);
				}
				if (ulisesId != null) {
					properties2.put("ids:ulisesID", ulisesId);
				}
			} catch (Exception e) {
				logger.info("Gustavo/Ulises ID not present.");
			}
			o.updateProperties(properties2, true);

			logger.info("Document uploaded successfully");
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return ResponseEntity.badRequest().build();
		}
	}

	@PostMapping("/login")
	@ResponseBody
	public String login(@RequestHeader("clientID") String clientID, @RequestHeader("clientPass") String clientPass) {
		TCredentials cred = credRepo.checkCredentials(clientID, clientPass);
		logger.info("login attemp");
		if (cred == null) {
			System.out.println("Invalid credentials");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid credentials.");
		} else {
			return JwtUtils.generateToken(clientID);
		}
	}

	@PostMapping("/delete-tags")
	@ResponseBody
	public void deleteAllTags() throws JsonMappingException, JsonProcessingException {

		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		Session session = factory.getRepositories(parameter).get(0).createSession();

		ArrayList<String> listNom = new ArrayList<String>();
		ArrayList<String> listCod = new ArrayList<String>();
		String[] codLiterals = new String[5];
		String[] nomLiterals = new String[5];
		cuadroClasificacion.getMapaAreas().forEach((k, v) -> {
			logger.info(k + " " + v);
			codLiterals[0] = documentLibrary + k;
			nomLiterals[0] = documentLibrary + v;
			listCod.add(documentLibrary + k);
			listNom.add(documentLibrary + v);
			cuadroClasificacion.getMapaAnios().forEach((i, b) -> {
				listCod.add(codLiterals[0] + "/" + i);
				listNom.add(nomLiterals[0] + "/" + b);
				codLiterals[1] = codLiterals[0] + "/" + i;
				nomLiterals[1] = nomLiterals[0] + "/" + b;
				cuadroClasificacion.getMapaConvocatorias().forEach((c, r) -> {
					listCod.add(codLiterals[1] + "/" + c);
					listNom.add(nomLiterals[1] + "/" + r);
					codLiterals[2] = codLiterals[1] + "/" + c;
					nomLiterals[2] = nomLiterals[1] + "/" + r;
					cuadroClasificacion.getMapaExpedientes().forEach((l, m) -> {
						listCod.add(codLiterals[2] + "/" + l);
						listNom.add(nomLiterals[2] + "/" + m);
						codLiterals[3] = codLiterals[2] + "/" + l;
						nomLiterals[3] = nomLiterals[2] + "/" + m;
						cuadroClasificacion.getMapaProcesos().forEach((q, w) -> {
							listCod.add(codLiterals[3] + "/" + q);
							listNom.add(nomLiterals[3] + "/" + w);
							codLiterals[4] = codLiterals[3] + "/" + q;
							nomLiterals[4] = nomLiterals[3] + "/" + w;
							LinkedHashMap<String, String> mapaActual = new LinkedHashMap<>();
							switch (q) {
								case "P01":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoSolicitudes();
									break;
								case "P02":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoPreevaluaciontecnico();
									break;
								case "P03":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoComisionEvaluacionivace();
									break;
								case "P04":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoResolucionconcesion();
									break;
								case "P05":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionconcesionabeneficiario();
									break;
								case "P06":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoAnticipoprestamo();
									break;
								case "P07":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoEjecuciondeproyecto();
									break;
								case "P08":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoJustificacionproyecto();
									break;
								case "P09":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoVerificaciondocumental();
									break;
								case "P10":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionmaterial();
									break;
								case "P11":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionfinal();
									break;
								case "P12":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionserviciopago();
									break;
								case "P13":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoPagosubvencion();
									break;
							}
							mapaActual.forEach((d, s) -> {
								listCod.add(codLiterals[4] + "/" + d);
								listNom.add(nomLiterals[4] + "/" + s);
							});
						});
					});
				});
			});
		});
		logger.info("Hello");

		for (String folderPath : listNom) {
			CmisObject cmisObject = session.getObjectByPath("/" + folderPath);
			String nodeId = cmisObject.getId();
			String url = "https://ivace.notacool.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + nodeId
					+ "/tags";

			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(user, pass);
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> requestEntity = new HttpEntity<>(headers);
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

			if (responseEntity.getBody() != null) {
				String jsonStr = responseEntity.getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				RootObject results = objectMapper.readValue(jsonStr, RootObject.class);

				for (int index = 0; index < results.getList().getEntries().length; index++) {
					url = url + "/" + results.getList().getEntries()[index].getEntry().getID();
					restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
				}
			}
		}
	}

	public void generateFolderTag(String folderPath, String tag) {
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		Session session = factory.getRepositories(parameter).get(0).createSession();
		CmisObject cmisObject = session.getObjectByPath("/" + folderPath);
		String nodeId = cmisObject.getId();
		String url = "https://ivace.notacool.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + nodeId
				+ "/tags";
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(user, pass);
		headers.setContentType(MediaType.APPLICATION_JSON);
		String jsonTag = "{\"tag\":\""
				+ tag
				+ "\"}";
		HttpEntity<String> requestEntity = new HttpEntity<>(jsonTag, headers);
		restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
	}

	@PostMapping("/generateDirStructure")
	@ResponseBody
	public String generateDirStruct(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
		// Only allowed for the JWT associated with the NOTACOOLADMIN user
		if (!JwtUtils.verifyToken(authorizationHeader)
				|| !JwtUtils.extractSubject(authorizationHeader).equals("NOTACOOLADMIN")) {
			System.out.println("Invalid JWT");
			return "";
		}

		ArrayList<String> listNom = new ArrayList<String>();
		ArrayList<String> listCod = new ArrayList<String>();
		String[] codLiterals = new String[5];
		String[] nomLiterals = new String[5];
		cuadroClasificacion.getMapaAreas().forEach((k, v) -> {
			logger.info(k + " " + v);
			codLiterals[0] = documentLibrary + k;
			nomLiterals[0] = documentLibrary + v;
			listCod.add(documentLibrary + k);
			listNom.add(documentLibrary + v);
			cuadroClasificacion.getMapaAnios().forEach((i, b) -> {
				listCod.add(codLiterals[0] + "/" + i);
				listNom.add(nomLiterals[0] + "/" + b);
				codLiterals[1] = codLiterals[0] + "/" + i;
				nomLiterals[1] = nomLiterals[0] + "/" + b;
				cuadroClasificacion.getMapaConvocatorias().forEach((c, r) -> {
					listCod.add(codLiterals[1] + "/" + c);
					listNom.add(nomLiterals[1] + "/" + r);
					codLiterals[2] = codLiterals[1] + "/" + c;
					nomLiterals[2] = nomLiterals[1] + "/" + r;
					cuadroClasificacion.getMapaExpedientes().forEach((l, m) -> {
						listCod.add(codLiterals[2] + "/" + l);
						listNom.add(nomLiterals[2] + "/" + m);
						codLiterals[3] = codLiterals[2] + "/" + l;
						nomLiterals[3] = nomLiterals[2] + "/" + m;
						cuadroClasificacion.getMapaProcesos().forEach((q, w) -> {
							listCod.add(codLiterals[3] + "/" + q);
							listNom.add(nomLiterals[3] + "/" + w);
							codLiterals[4] = codLiterals[3] + "/" + q;
							nomLiterals[4] = nomLiterals[3] + "/" + w;
							LinkedHashMap<String, String> mapaActual = new LinkedHashMap<>();
							switch (q) {
								case "P01":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoSolicitudes();
									break;
								case "P02":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoPreevaluaciontecnico();
									break;
								case "P03":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoComisionEvaluacionivace();
									break;
								case "P04":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoResolucionconcesion();
									break;
								case "P05":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionconcesionabeneficiario();
									break;
								case "P06":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoAnticipoprestamo();
									break;
								case "P07":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoEjecuciondeproyecto();
									break;
								case "P08":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoJustificacionproyecto();
									break;
								case "P09":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoVerificaciondocumental();
									break;
								case "P10":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionmaterial();
									break;
								case "P11":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionfinal();
									break;
								case "P12":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoComunicacionserviciopago();
									break;
								case "P13":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoPagosubvencion();
									break;
							}
							mapaActual.forEach((d, s) -> {
								listCod.add(codLiterals[4] + "/" + d);
								listNom.add(nomLiterals[4] + "/" + s);
							});
						});
					});
				});
			});
		});

		// Configuraciones básicas para para conectarse
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		List<Repository> repositories = factory.getRepositories(parameter);
		Session session = repositories.get(0).createSession();
		Folder root = session.getRootFolder();

		// Creamos las carpetas, pueden ser una o 50
		Folder parent = root;
		String[] parts = null;

		// recorremos la lista de directorios
		for (int i = 0; i < listCod.size(); i++) {
			parent = root;
			if (listNom.get(i).contains("/")) {
				parts = listNom.get(i).split("/");
			} else {
				parts = new String[1];
				parts[0] = listNom.get(i);
			}

			for (String folderName : parts) {
				parent = createFolder(folderName, parent, listCod.get(i), listNom.get(i));
			}
			logger.info("Creando el directorio: " + listNom.get(i));
		}
		logger.info("Terminados de crear los " + listCod.size() + " directorios");

		return "";
	}

	@GetMapping("/getByGustavo")
	@ResponseBody
	public ResponseEntity<byte[]> getByGustavoId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("gustavoID") int gustavoID) {
		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		Session session = factory.getRepositories(parameter).get(0).createSession();
		Folder root = session.getRootFolder();
		String fileId = "";
		for (CmisObject r : root.getChildren()) {
			if (r.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
				fileId = find((Folder) r, gustavoID, 0);
				if (!fileId.equals("")) {
					String url = "https://ivace.notacool.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + fileId
							+ "/content?attachment=true";
					RestTemplate restTemplate = new RestTemplate();
					HttpHeaders headers = new HttpHeaders();
					headers.setBasicAuth(user, pass);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
					HttpEntity<String> entity = new HttpEntity<>(headers);
					ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
					return response;
				}
			}
		}
		if (fileId.equals(""))
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<>(HttpStatus.OK);
	}

	public String find(Folder r, int externalId, int type) {
		Folder folder = (Folder) r;
		String fileId = "";
		String out = "";
		for (CmisObject child : folder.getChildren()) {
			if (child.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
				out = find((Folder) child, externalId, type);
				if (out != "") {
					return out;
				}
			} else if (child.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
				// externalId -> gustavo:0 ulises:1
				if (type == 0) {
					try {
						if (child.getProperty("ids:gustavoID").getFirstValue().equals("" + externalId)) {
							fileId = child.getId();
							return fileId;
						}
					} catch (Exception e) {

					}
				} else {
					try {
						if (child.getProperty("ids:ulisesID").getFirstValue().equals("" + externalId)) {
							fileId = child.getId();
							return fileId;
						}
					} catch (Exception e) {
					}

				}
			}
		}
		return fileId;
	}

	@GetMapping("/getByUlises")
	@ResponseBody
	public ResponseEntity<byte[]> getByUlisesId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("ulisesID") int ulisesID) {
		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		Session session = factory.getRepositories(parameter).get(0).createSession();
		Folder root = session.getRootFolder();
		String fileId = "";
		for (CmisObject r : root.getChildren()) {
			if (r.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
				fileId = find((Folder) r, ulisesID, 1);
				if (!fileId.equals("")) {
					String url = "https://ivace.notacool.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + fileId
							+ "/content?attachment=true";
					RestTemplate restTemplate = new RestTemplate();
					HttpHeaders headers = new HttpHeaders();
					headers.setBasicAuth(user, pass);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
					HttpEntity<String> entity = new HttpEntity<>(headers);
					ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
					return response;
				}
			}
		}

		if (fileId.equals(""))
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<>(HttpStatus.OK);

	}

	public Folder createFolder(String folderName, Folder root, String fullPathCod, String fullPathNom) {
		Boolean folderExists = false;
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, folderName);
		String description = null;
		String title = null;
		String tag = "";
		
		Folder parent = null;
		for (CmisObject r : root.getChildren()) {
			if (r.getName().equals(folderName)) {
				folderExists = true;
				parent = (Folder) r;
			}
		}
		
		// create the folder
		if (!folderExists) {
			LinkedHashMap<String, String> mapaAsociado = new LinkedHashMap<>();
			String[] splittedPathNom = fullPathNom.split("/");
			String[] splittedPathCod = fullPathCod.split("/");
			if (splittedPathNom.length == 4) {
				logger.info("Estamos creando el Area");
				mapaAsociado = cuadroClasificacion.getMapaAreas();
				description = splittedPathNom[3];
				tag = description;
				title = splittedPathCod[3];
			}
			if (splittedPathNom.length == 5) {
				logger.info("Estamos creando el Año");
				mapaAsociado = cuadroClasificacion.getMapaAnios();
				description = splittedPathNom[3] + " " + splittedPathNom[4];
				title = splittedPathCod[3] + " " + splittedPathCod[4];
				tag = description;
			}
			if (splittedPathNom.length == 6) {
				logger.info("Estamos creando la convocatoria");
				mapaAsociado = cuadroClasificacion.getMapaConvocatorias();
				description = splittedPathNom[3] + " " + splittedPathNom[4] + " " + splittedPathNom[5];
				title = splittedPathCod[3] + " " + splittedPathCod[4] + " " + splittedPathCod[5];
				tag = description;
			}
			if (splittedPathNom.length == 7) {
				logger.info("Estamos creando el expediente");
				mapaAsociado = cuadroClasificacion.getMapaExpedientes();
				description = splittedPathNom[3] + " " + splittedPathNom[4] + " " + splittedPathNom[5] + " "
						+ splittedPathNom[6];
				title = splittedPathCod[3] + " " + splittedPathCod[4] + " " + splittedPathCod[5] + " "
						+ splittedPathCod[6];
				tag = description;
			}
			if (splittedPathNom.length == 8) {
				logger.info("Estamos creando el proceso");
				mapaAsociado = cuadroClasificacion.getMapaProcesos();
				description = splittedPathNom[3] + " " + splittedPathNom[4] + " " + splittedPathNom[5] + " "
						+ splittedPathNom[6] + " " + splittedPathNom[7].substring(4, splittedPathNom[7].length());
				title = splittedPathCod[3] + " " + splittedPathCod[4] + " " + splittedPathCod[5] + " "
						+ splittedPathCod[6] + " " + splittedPathNom[7].substring(4, splittedPathNom[7].length());
				tag = description;
			}
			if (splittedPathNom.length == 9) {
				logger.info("Estamos creando el documento");
				switch (splittedPathNom[7]) {
					case "P01":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P01");
						break;
					case "P02":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P02");
						break;
					case "P03":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P03");
						break;
					case "P04":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P04");
						break;
					case "P05":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P05");
						break;
					case "P06":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P06");
						break;
					case "P07":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P07");
						break;
					case "P08":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P08");
						break;
					case "P09":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P09");
						break;
					case "P10":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P10");
						break;
					case "P11":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P11");
						break;
					case "P12":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P12");
						break;
					case "P13":
						mapaAsociado = cuadroClasificacion.getMapaProcesosYDocumentaciones().get("P13");
						break;
				}
				logger.info(mapaAsociado.toString());
				description = splittedPathNom[3] + " " + splittedPathNom[4] + " " + splittedPathNom[5] + " "
						+ splittedPathNom[6] + " " + splittedPathNom[7].substring(4, splittedPathNom[7].length()) + " "
						+ splittedPathNom[8].substring(4, splittedPathNom[8].length());

				title = splittedPathCod[3] + " " + splittedPathCod[4] + " " + splittedPathCod[5] + " "
						+ splittedPathCod[6] + " " + splittedPathCod[7] + " " + splittedPathCod[8];

				tag = description;
			}

			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

			List<String> secondary = new ArrayList<>();
			secondary.add("P:cm:titled");
			properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondary);
			properties.put("cm:title", title);
			properties.put(PropertyIds.DESCRIPTION, description);

			parent = root.createFolder(properties);

			if (tag.length() != 0) {
				generateFolderTag(fullPathNom, tag);
			}

		} 
		return parent;
	}

}
