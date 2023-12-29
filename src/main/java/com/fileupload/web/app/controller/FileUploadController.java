package com.fileupload.web.app.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
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
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fileupload.web.app.model.Document;
import com.fileupload.web.app.model.TCredentials;
import com.fileupload.web.app.repository.CredentialsRepository;
import com.fileupload.web.app.repository.DocumentRepository;
import com.fileupload.web.app.security.JwtUtils;
import com.fileupload.web.app.validator.RequestValidator;

import io.swagger.annotations.Api;

@Api(description = "Servicio para comunicar aplicaciones externas con el Gestor Documental del IVACE.", tags = "API de comunicacion con Alfresco")
@Controller
public class FileUploadController {

	Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	@Autowired
	CredentialsRepository credRepo;

	@Autowired
	FolderController folderController;

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	RequestValidator validator;

	@Value("${alfresco.user}")
	private String user;

	@Value("${alfresco.pass}")
	private String pass;

	@Value("${alfresco.url}")
	private String url;

	private String getCmisUrl() {
		return url + "/alfresco/api/-default-/public/cmis/versions/1.1/atom";
	}

	private String getRestUrl() {
		return url + "/alfresco/api/-default-/public/alfresco/versions/1/nodes";
	}

	@Value("${alfresco.documentLibrary}")
	private String documentLibrary;

	@PostMapping("/login")
	@ResponseBody
	public String login(@RequestHeader("clientID") String clientID, @RequestHeader("clientPass") String clientPass) {
		TCredentials cred = credRepo.checkCredentials(clientID, clientPass);
		if (cred == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid credentials.");
		} else {
			return JwtUtils.generateToken(clientID);
		}
	}

	@PostMapping("/uploadFile")
	@ResponseBody
	@Transactional(readOnly = false)
	public ResponseEntity<String> uploadToAlfresco(
			@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "codArea") String codArea,
			@RequestHeader(value = "codAnio") String codAnio,
			@RequestHeader(value = "codConvocatoria") String codConvocatoria,
			@RequestHeader(value = "codX") String codX,
			@RequestHeader(value = "codExpediente", required = false) String codExpediente,
			@RequestHeader(value = "codProceso", required = false) String codProceso,
			@RequestHeader(value = "codDocumentacion") String codDocumentacion,
			@RequestHeader(value = "nomArea") String nomArea,
			@RequestHeader(value = "nomAnio") String nomAnio,
			@RequestHeader(value = "nomConvocatoria") String nomConvocatoria,
			@RequestHeader(value = "nomX") String nomX,
			@RequestHeader(value = "nomExpediente", required = false) String nomExpediente,
			@RequestHeader(value = "nomProceso", required = false) String nomProceso,
			@RequestHeader(value = "nomDocumentacion") String nomDocumentacion,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "gustavoId", required = false) String gustavoId,
			@RequestHeader(value = "ulisesId", required = false) String ulisesId) {

		try {
			if (!JwtUtils.verifyToken(authorizationHeader)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			if (!validator.validateIDS(gustavoId, ulisesId)) {
				return ResponseEntity.badRequest().body("Bad request. Introduce valid metadata");
			}

			String documentDestinationCod = buildDocumentDestination(codArea, codAnio, codConvocatoria, codX,
					codExpediente, codProceso, codDocumentacion);
			String documentDestinationNom = buildDocumentDestination(nomArea, nomAnio, nomConvocatoria, nomX,
					nomExpediente, nomProceso, nomDocumentacion);

			byte[] fileContent = file.getBytes();
			Boolean fileExists = false;
			String path = "/" + documentDestinationNom;

			// Configuraciones básicas para para conectarse
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();

			// Credenciales del usuario y url de conexión
			parameter.put(SessionParameter.USER, user);
			parameter.put(SessionParameter.PASSWORD, pass);
			parameter.put(SessionParameter.ATOMPUB_URL, this.getCmisUrl());
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

			// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
			Session session = factory.getRepositories(parameter).get(0).createSession();
			Folder folder = null;

			try {
				folder = (Folder) session.getObjectByPath(path);
			} catch (Exception e) {
				folder = folderController.createFolder(documentDestinationCod, documentDestinationNom);
			}

			// Creamos el archivo si no existe
			// ¿SE PUEDE OPTIMIZAR?
			for (CmisObject r : folder.getChildren()) {
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
					BigInteger.valueOf(fileContent.length), file.getContentType(), stream);

			if (gustavoId != null) {
				Document doc = documentRepository.findByGustavoID(gustavoId);

				if (doc != null) {
					String url = this.getRestUrl() + "/"
							+ folder.getId() + "/secondary-children";
					HttpClient client = HttpClient.newHttpClient();
					HttpHeaders headers = new HttpHeaders();
					headers.setBasicAuth(user, pass);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
					String body = "{\"childId\": " + "\"" + doc.getAlfrescoId() + "\""
							+ ", \"assocType\":\"cm:contains\"}";
					HttpRequest request = HttpRequest.newBuilder()
							.uri(URI.create(url))
							.POST(HttpRequest.BodyPublishers.ofString(body))
							.header("Authorization", headers.get("Authorization").get(0))
							.header("Content-Type", "application/json")
							.build();

					client.send(request,
							HttpResponse.BodyHandlers.ofString());
				} else {
					// Creamos el documento en el Alfresco
					CmisObject o = folder.createDocument(properties2, contentStream, VersioningState.MAJOR);
					properties2.put("ids:gustavoID", gustavoId);
					o.updateProperties(properties2, true);

					Document newDocument = new Document();
					newDocument.setAlfrescoId(o.getId());
					newDocument.setGustavoId(gustavoId);
					documentRepository.create(newDocument);
				}
			} else if (ulisesId != "" && ulisesId != null) {
				Document doc = documentRepository.findByUlisesId(ulisesId);
				if (doc != null) {
					String url = this.getRestUrl() + "/"
							+ folder.getId() + "/targets";
					HttpClient client = HttpClient.newHttpClient();
					HttpHeaders headers = new HttpHeaders();
					headers.setBasicAuth(user, pass);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
					String body = "{\"childId\": " + "\"" + doc.getAlfrescoId() + "\""
							+ ", \"assocType\":\"cm:contains\"}";
					HttpRequest request = HttpRequest.newBuilder()
							.uri(URI.create(url))
							.POST(HttpRequest.BodyPublishers.ofString(body))
							.header("Authorization", headers.get("Authorization").get(0))
							.header("Content-Type", "application/json")
							.build();

					client.send(request,
							HttpResponse.BodyHandlers.ofString());
				} else {
					// Creamos el documento en el Alfresco
					CmisObject o = folder.createDocument(properties2, contentStream, VersioningState.MAJOR);
					properties2.put("ids:ulisesID", ulisesId);
					o.updateProperties(properties2, true);

					Document newDocument = new Document();
					newDocument.setAlfrescoId(o.getId());
					newDocument.setUlisesId(ulisesId);
					documentRepository.create(newDocument);
				}
			}
			return ResponseEntity.ok().body("Document uploaded successfully");
		} catch (Exception e) {
			logger.info(e.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/getByGustavo")
	@ResponseBody
	public ResponseEntity<byte[]> getByGustavoId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("gustavoID") String gustavoID) {
		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, this.getCmisUrl());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión
		Session session = factory.getRepositories(parameter).get(0).createSession();

		Document doc = documentRepository.findByGustavoID(gustavoID);

		if (doc == null)
			return ResponseEntity.notFound().build();
		else {
			String url = this.getRestUrl() + "/"
					+ doc.getAlfrescoId() + "/content?attachment=true";
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(user, pass);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
			return response;
		}
	}

	@GetMapping("/getByUlises")
	@ResponseBody
	public ResponseEntity<byte[]> getByUlisesId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("ulisesID") String ulisesID) {
		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, this.getCmisUrl());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		Session session = factory.getRepositories(parameter).get(0).createSession();

		Document doc = documentRepository.findByUlisesId(ulisesID);

		if (doc == null)
			return ResponseEntity.notFound().build();
		else {

			String url = this.getRestUrl() + "/" + doc.getAlfrescoId() + "/content?attachment=true";
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(user, pass);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
			return response;
		}
	}

	@DeleteMapping("/deleteFileByGustavoId")
	@Transactional(readOnly = false)
	public ResponseEntity<String> deleteByGustavoID(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("gustavoID") String gustavoID) {

		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, this.getCmisUrl());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		Session session = factory.getRepositories(parameter).get(0).createSession();

		Document doc = documentRepository.findByGustavoID(gustavoID);

		if (doc == null)
			return ResponseEntity.notFound().build();
		else {
			String requestUrl = this.getRestUrl() + "/"
					+ doc.getAlfrescoId();
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(user, pass);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.DELETE, entity,
					String.class);
			if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
				documentRepository.deleteById(doc.getId());
			}
			return response;
		}
	}

	@DeleteMapping("/deleteFileByUlisesId")
	@Transactional(readOnly = false)
	public ResponseEntity<String> deleteByUlisesID(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("ulisesID") String ulisesID) {

		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, this.getCmisUrl());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		Session session = factory.getRepositories(parameter).get(0).createSession();

		Document doc = documentRepository.findByUlisesId(ulisesID);

		if (doc == null)
			return ResponseEntity.notFound().build();
		else {
			String requestUrl = this.getRestUrl() + "/"
					+ doc.getAlfrescoId();
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(user, pass);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.DELETE, entity,
					String.class);
			if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
				documentRepository.deleteById(doc.getId());
			}
			return response;
		}
	}

	@PutMapping("/updateByGustavo")
	public ResponseEntity<byte[]> updateGustavoFile(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("gustavoID") String gustavoID, @RequestHeader("name") String name,
			@RequestHeader("title") String title, @RequestHeader("description") String description)
			throws IOException, InterruptedException {

		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, this.getCmisUrl());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		Session session = factory.getRepositories(parameter).get(0).createSession();

		Document doc = documentRepository.findByGustavoID(gustavoID);

		if (doc == null)
			return ResponseEntity.notFound().build();
		else {
			String url = this.getRestUrl() + "/"
					+ doc.getAlfrescoId() + "/";
			HttpClient client = HttpClient.newHttpClient();
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(user, pass);
			JSONObject properties = new JSONObject();
			properties.put("cm:title", title);
			properties.put("cm:description", description);
			JSONObject jsonNode = new JSONObject();
			jsonNode.put("name", name);
			jsonNode.put("properties", properties);
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.PUT(HttpRequest.BodyPublishers.ofString(jsonNode.toString()))
					.header("Authorization", headers.get("Authorization").get(0))
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			return ResponseEntity.status(response.statusCode()).build();
		}

	}

	@PutMapping("/updateByUlises")
	public ResponseEntity<byte[]> updateUlisesFile(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("ulisesID") String ulisesID, @RequestHeader("name") String name,
			@RequestHeader("title") String title, @RequestHeader("description") String description)
			throws IOException, InterruptedException {

		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// Credenciales del usuario y url de conexión
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, this.getCmisUrl());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
		Session session = factory.getRepositories(parameter).get(0).createSession();

		Document doc = documentRepository.findByUlisesId(ulisesID);

		if (doc == null)
			return ResponseEntity.notFound().build();
		else {
			String url = this.getRestUrl() + "/"
					+ doc.getAlfrescoId() + "/";
			HttpClient client = HttpClient.newHttpClient();
			HttpHeaders headers = new HttpHeaders();
			headers.setBasicAuth(user, pass);
			JSONObject properties = new JSONObject();
			properties.put("cm:title", title);
			properties.put("cm:description", description);
			JSONObject jsonNode = new JSONObject();
			jsonNode.put("name", name);
			jsonNode.put("properties", properties);
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(url))
					.PUT(HttpRequest.BodyPublishers.ofString(jsonNode.toString()))
					.header("Authorization", headers.get("Authorization").get(0))
					.header("Content-Type", "application/json")
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			return ResponseEntity.status(response.statusCode()).build();
		}

	}

	public String find(Folder folder, String externalId, int type) {
		for (CmisObject child : folder.getChildren()) {
			if (child.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
				String result = find((Folder) child, externalId, type);
				if (!result.isEmpty()) {
					return result;
				}
			} else if (child.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
				String propertyKey = (type == 0) ? "ids:gustavoID" : "ids:ulisesID";
				try {
					Object propertyValue = child.getProperty(propertyKey).getFirstValue();
					if (String.valueOf(externalId).equals(propertyValue.toString())) {
						return child.getId();
					}
				} catch (Exception e) {
					// Manejar la excepción o ignorarla según sea necesario
				}
			}
		}
		return "";
	}

	private String buildDocumentDestination(String... parts) {
		StringJoiner joiner = new StringJoiner("/");
		for (String part : parts) {
			if (part == null || part.isEmpty() || part.equals(" "))
				continue;
			joiner.add(part);
		}
		return documentLibrary + joiner.toString();
	}
}
