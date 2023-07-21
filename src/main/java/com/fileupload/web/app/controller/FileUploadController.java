package com.fileupload.web.app.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileupload.web.app.model.CuadroClasificacionDataSets;
import com.fileupload.web.app.model.Path;
import com.fileupload.web.app.model.RootObject;
import com.fileupload.web.app.model.TCredentials;
import com.fileupload.web.app.repository.CredentialsRepository;
import com.fileupload.web.app.repository.PathRepository;
import com.fileupload.web.app.security.JwtUtils;
import com.fileupload.web.app.validator.RequestValidator;

import es.gob.aapp.libreriaENI.exception.document.DocumentENIValidationException;
import es.gob.aapp.libreriaENI.model.documento.ObjetoDocumentoENI;
import es.gob.aapp.libreriaENI.model.documento.contenido.ObjetoDocumentoContenido;
import es.gob.aapp.libreriaENI.model.documento.firma.FirmaENI;
import es.gob.aapp.libreriaENI.model.documento.metadatos.ObjetoDocumentoMetadatos;
import es.gob.aapp.libreriaENI.model.documento.metadatos.ObjetoDocumentoMetadatosEstadoElaboracion;
import es.gob.aapp.libreriaENI.service.impl.GenerateDocumentENIImpl;
import es.gob.aapp.libreriaENI.util.EnumeracionDocumentoEstadoElaboracion;
import es.gob.aapp.libreriaENI.util.EnumeracionDocumentoTipoDocumental;
import es.gob.aapp.libreriaENI.util.EnumeracionDocumentoTipoFirma;
import es.gob.aapp.libreriaENI.valide.ValideDocumentENI;
import io.swagger.annotations.Api;

@Api(description = "Servicio para comunicar aplicaciones externas con el Gestor Documental del IVACE.", tags = "API de comunicacion con Alfresco")
@Controller
public class FileUploadController {

	Logger logger = LoggerFactory.getLogger(FileUploadController.class);

	@Autowired
	CredentialsRepository credRepo;

	@Autowired
	PathRepository pathRepository;

	@Autowired
	CuadroClasificacionDataSets cuadroClasificacion;

	@Autowired
	RequestValidator validator;

	@Value("${alfresco.url}")
	private String url;

	@Value("${alfresco.documentLibrary}")
	private String documentLibrary;

	@Value("${alfresco.excelPath}")
	private String excelPath;

	@PostMapping("/upload/{nomArea}/{nomAnio}/{nomConvocatoria}/{nomX}/{nomExpediente}/{nomProceso}/{nomDocumentacion}/{codArea}/{codAnio}/{codConvocatoria}/{codX}/{codExpediente}/{codProceso}/{codDocumentacion}")
	@ResponseBody
	public ResponseEntity<String> uploadToAlfresco(
			@RequestPart("file") MultipartFile file,
			@PathVariable("nomArea") String nomArea,
			@PathVariable("nomAnio") String nomAnio,
			@PathVariable("nomConvocatoria") String nomConvocatoria,
			@PathVariable("nomX") String nomX,
			@PathVariable("nomExpediente") String nomExpediente,
			@PathVariable("nomProceso") String nomProceso,
			@PathVariable("nomDocumentacion") String nomDocumentacion,
			@PathVariable("codArea") String codArea,
			@PathVariable("codAnio") String codAnio,
			@PathVariable("codConvocatoria") String codConvocatoria,
			@PathVariable("codX") String codX,
			@PathVariable("codExpediente") String codExpediente,
			@PathVariable("codProceso") String codProceso,
			@PathVariable("codDocumentacion") String codDocumentacion,
			@RequestHeader(value = "user", required = true) String user,
			@RequestHeader(value = "password", required = true) String password,
			@RequestHeader(value = "Authorization", required = true) String authorizationHeader,
			@RequestHeader(value = "gustavoId", required = true) String gustavoId,
			@RequestHeader(value = "ulisesId", required = true) String ulisesId) {

		if (!JwtUtils.verifyToken(authorizationHeader)) {
			System.out.println("Invalid JWT");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		try {

			byte[] fileContent = file.getBytes();
			Boolean fileExists = false;

			// Configuraciones básicas para para conectarse
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();

			// Credenciales del usuario y url de conexión
			parameter.put(SessionParameter.USER, user);
			parameter.put(SessionParameter.PASSWORD, password);
			parameter.put(SessionParameter.ATOMPUB_URL, url);
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

			// Creamos la sesión y cogemos la carpeta raíz del árbol de directorios
			Session session = factory.getRepositories(parameter).get(0).createSession();
			Folder root = session.getRootFolder();

			Path newPath = new Path(codArea, nomArea, codAnio, nomAnio, codConvocatoria, nomConvocatoria, codX, nomX,
					codExpediente, nomExpediente,
					codProceso, nomProceso, codDocumentacion, nomDocumentacion);

			// Creamos las carpetas, pueden ser una o 50
			Folder parent = root;

			String[] aux = newPath.getNomPath().split("/");
			for (int index = 0; index < aux.length; index++) {
				parent = createFolder(parent, newPath, index, user, password);
			}

			// Creamos el archivo si no existe
			for (CmisObject r : parent.getChildren()) {
				if (r.getName().equals(file.getOriginalFilename())) {
					fileExists = true;
				}
			}

			// Devolvemos false si el archivo ya existe
			if (fileExists) {
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
			CmisObject o = parent.createDocument(properties2, contentStream,
					VersioningState.MAJOR);
			// Check gustavo/ulises UniqueConstraint
			String constraintViolationFound = "";
			try {
				if (gustavoId != "" && gustavoId != null) {
					constraintViolationFound = find((Folder) root, Integer.parseInt(gustavoId),
							0);
					if (constraintViolationFound != "") {
						logger.info("Could not set GustavoID - Constraint violation found.");
						return ResponseEntity.status(HttpStatus.OK).build();
					}
				}
				if (ulisesId != "" && ulisesId != null) {
					constraintViolationFound = find((Folder) root, Integer.parseInt(ulisesId),
							0);
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
	public void deleteAllTags(String user, String pass)
			throws JsonMappingException, JsonProcessingException {

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
									mapaActual = cuadroClasificacion
											.getMapaDocumentacionesProcesoPreevaluaciontecnico();
									break;
								case "P03":
									mapaActual = cuadroClasificacion
											.getMapaDocumentacionesProcesoComisionEvaluacionivace();
									break;
								case "P04":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoResolucionconcesion();
									break;
								case "P05":
									mapaActual = cuadroClasificacion
											.getMapaDocumentacionesProcesoComunicacionconcesionabeneficiario();
									break;
								case "P06":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoAnticipoprestamo();
									break;
								case "P07":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoEjecuciondeproyecto();
									break;
								case "P08":
									mapaActual = cuadroClasificacion
											.getMapaDocumentacionesProcesoJustificacionproyecto();
									break;
								case "P09":
									mapaActual = cuadroClasificacion
											.getMapaDocumentacionesProcesoVerificaciondocumental();
									break;
								case "P10":
									mapaActual = cuadroClasificacion
											.getMapaDocumentacionesProcesoVerificacionmaterial();
									break;
								case "P11":
									mapaActual = cuadroClasificacion.getMapaDocumentacionesProcesoVerificacionfinal();
									break;
								case "P12":
									mapaActual = cuadroClasificacion
											.getMapaDocumentacionesProcesoComunicacionserviciopago();
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
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
					String.class);

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

	public void generateFolderTag(String folderPath, String tag, String user, String pass) {
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

	@GetMapping("/getByGustavo")
	@ResponseBody
	public ResponseEntity<byte[]> getByGustavoId(
			@RequestHeader(value = "user", required = true) String user,
			@RequestHeader(value = "password", required = true) String pass,
			@RequestHeader(value = "Authorization", required = true) String authorizationHeader,
			@RequestHeader(value = "gustavoID", required = true) int gustavoID) {

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
					String url = "https://ivace.notacool.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
							+ fileId
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
			@RequestHeader(value = "user", required = true) String user,
			@RequestHeader(value = "password", required = true) String pass,
			@RequestHeader(value = "Authorization", required = true) String authorizationHeader,
			@RequestHeader(value = "ulisesID", required = true) int ulisesID) {
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
					String url = "https://ivace.notacool.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/"
							+ fileId
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

	public Folder createFolder(Folder root, Path path, Integer index, String user, String pass) {
		Boolean folderExists = false;
		Map<String, Object> properties = new HashMap<String, Object>();

		String[] fullNomPathSplitted = path.getNomPath().split("/");

		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, fullNomPathSplitted[index].trim());
		String description = null;
		String title = null;

		Folder parent = null;
		for (CmisObject r : root.getChildren()) {
			if (r.getName().equals(fullNomPathSplitted[index])) {
				folderExists = true;
				parent = (Folder) r;
			}
		}
		// create the folder
		if (!folderExists && fullNomPathSplitted[index].length() > 0) {
			if(path.getCodX().equals("Normativa")  || path.getCodX().equals("CO Evaluación")){
				if (index == 3) {
					logger.info("Estamos creando el Area");
					description = path.getDescripcionArea();
					title = path.getTituloArea();
				}
				if (index == 4) {
					logger.info("Estamos creando el Año");
					description = path.getDescripcionAnho();
					title = path.getTituloAnho();
				}
				if (index == 5) {
					logger.info("Estamos creando la convocatoria");
					description = path.getDescripcionConvocatoria();
					title = path.getTituloConvocatoria();
				}
				if (index == 6) {
					logger.info("Estamos creando X");
					description = path.getDescripcionX();
					title = path.getTituloX();
				}
				if (index == 7) {
					logger.info("Estamos creando el documento");
					description = path.getDescripcionDocumentacion();
					title = path.getTituloDocumentacion();
				}
			}else{
				if (index == 3) {
					logger.info("Estamos creando el Area");
					description = path.getDescripcionArea();
					title = path.getTituloArea();
				}
				if (index == 4) {
					logger.info("Estamos creando el Año");
					description = path.getDescripcionAnho();
					title = path.getTituloAnho();
				}
				if (index == 5) {
					logger.info("Estamos creando la convocatoria");
					description = path.getDescripcionConvocatoria();
					title = path.getTituloConvocatoria();
				}
				if (index == 6) {
					logger.info("Estamos creando X");
					description = path.getDescripcionX();
					title = path.getTituloX();
				}
				if (index == 7) {
					logger.info("Estamos creando el expediente");
					description = path.getDescripcionExpediente();
					title = path.getTituloExpediente();
				}
				if (index == 8) {
					logger.info("Estamos creando el proceso");
					description = path.getDescripcionProceso();
					title = path.getTituloProceso();
				}
				if (index == 9) {
					logger.info("Estamos creando el documento");
					description = path.getDescripcionDocumentacion();
					title = path.getTituloDocumentacion();
				}
			}

			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

			List<String> secondary = new ArrayList<>();
			secondary.add("P:cm:titled");
			properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondary);
			properties.put("cm:title", title);
			properties.put(PropertyIds.DESCRIPTION, description);

			parent = root.createFolder(properties);
		}
		return parent;
	}

	@PostMapping("/generate-eni")
	@ResponseBody
	public ResponseEntity<String> GenerateDocumentENI(
			@RequestPart("file") MultipartFile file,
			String pathDestinoLocal,
			String MetadatoEstadoElaboracion,
			String MetadatoOrganos,
			String MetadatoIdDocumento,
			String MetadatoVersionNTI) throws IOException, Exception {

		GenerateDocumentENIImpl gdENI = new GenerateDocumentENIImpl();
		ObjetoDocumentoENI eni = new ObjetoDocumentoENI();
		ObjetoDocumentoMetadatos obMetadatos = new ObjetoDocumentoMetadatos();
		ObjetoDocumentoMetadatosEstadoElaboracion estEl = new ObjetoDocumentoMetadatosEstadoElaboracion();
		GregorianCalendar gCalendar = new GregorianCalendar();
		ArrayList<String> organos = new ArrayList<String>();

		if (!validator.isValidDocumentId(MetadatoIdDocumento)) {
			return new ResponseEntity<>(
					"Identificador introducido no válido. Debe ser de la forma ES_LLNNNNNNN_NNNN_XXXXXXX con un máximo de 52 caracteres (L = Letra, N = Numeros).",
					HttpStatus.BAD_REQUEST);
		}

		organos.add(MetadatoOrganos);

		switch (MetadatoEstadoElaboracion) {
			default:
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_01);
				break;
			case "ORIGINAL":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_01);
				break;
			case "COPIA ELECTRONICA AUTENTICA CON CAMBIO DE FORMATO":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_02);
				break;
			case "COPIA ELECTRONICA AUTENTICA DE DOCUMENTO PAPEL":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_03);
				break;
			case "COPIA ELECTRONICA PARCIAL AUTENTICA":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_04);
				break;
			case "OTROS":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_99);
				break;
		}

		obMetadatos.setEstadoElaboracion(estEl);
		obMetadatos.setVersionNTI(MetadatoVersionNTI);
		obMetadatos.setIdentificadorDocumento(MetadatoIdDocumento);
		obMetadatos.setOrgano(organos);
		obMetadatos.setFechaCaptura(gCalendar);
		obMetadatos.setTipoDocumental(EnumeracionDocumentoTipoDocumental.TD_99);

		eni.setMetadatos(obMetadatos);

		ArrayList<FirmaENI> listFirmas = new ArrayList<FirmaENI>();
		listFirmas.add(new FirmaENI());
		listFirmas.get(0).setEnumeracionDocumentoTipoFirma(EnumeracionDocumentoTipoFirma.TF_01);
		listFirmas.get(0).setCsv("sample csv code");
		listFirmas.get(0).setRegulacionCsv("sample regulation csv code");

		eni.setFirmas(listFirmas);

		ObjetoDocumentoContenido obDocContenido = new ObjetoDocumentoContenido();
		InputStream fileStream = file.getInputStream();
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		obDocContenido.setContenido(fileStream);
		obDocContenido.setNombreFormato(extension);
		eni.setContenidoDocumento(obDocContenido);

		if (ValidateENI(eni)) {
			InputStream fis = gdENI.generateENI(eni);
			File f = new File(pathDestinoLocal + "\\" + MetadatoIdDocumento + ".xml");
			FileUtils.copyInputStreamToFile(fis, f);
			return new ResponseEntity<>(null, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/generate-eni-and-upload-to-alfresco")
	@ResponseBody
	public ResponseEntity<String> GenerateDocumentENIAndUploadToAlfresco(
			@RequestPart("file") MultipartFile file,
			@RequestHeader("user") String user,
			@RequestHeader("password") String password,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "gustavoId", required = false) String gustavoId,
			@RequestHeader(value = "ulisesId", required = false) String ulisesId,
			String MetadatoEstadoElaboracion,
			String MetadatoOrganos,
			String MetadatoIdDocumento,
			String MetadatoVersionNTI,
			String pathDestino,
			String codArea,
			String codAnio,
			String codConvocatoria,
			String codX,
			String codExpediente,
			String codProceso,
			String codDocumentacion,
			String nomArea,
			String nomAnio,
			String nomConvocatoria,
			String nomX,
			String nomExpediente,
			String nomProceso,
			String nomDocumentacion) throws IOException, Exception {

		GenerateDocumentENIImpl gdENI = new GenerateDocumentENIImpl();
		ObjetoDocumentoENI eni = new ObjetoDocumentoENI();
		ObjetoDocumentoMetadatos obMetadatos = new ObjetoDocumentoMetadatos();
		ObjetoDocumentoMetadatosEstadoElaboracion estEl = new ObjetoDocumentoMetadatosEstadoElaboracion();
		GregorianCalendar gCalendar = new GregorianCalendar();
		ArrayList<String> organos = new ArrayList<String>();

		organos.add(MetadatoOrganos);

		switch (MetadatoEstadoElaboracion) {
			default:
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_01);
				break;
			case "ORIGINAL":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_01);
				break;
			case "COPIA ELECTRONICA AUTENTICA CON CAMBIO DE FORMATO":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_02);
				break;
			case "COPIA ELECTRONICA AUTENTICA DE DOCUMENTO PAPEL":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_03);
				break;
			case "COPIA ELECTRONICA PARCIAL AUTENTICA":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_04);
				break;
			case "OTROS":
				estEl.setValorEstadoElaboracion(EnumeracionDocumentoEstadoElaboracion.EE_99);
				break;
		}

		obMetadatos.setEstadoElaboracion(estEl);
		obMetadatos.setVersionNTI(MetadatoVersionNTI);
		obMetadatos.setIdentificadorDocumento(MetadatoIdDocumento);
		obMetadatos.setOrgano(organos);
		obMetadatos.setFechaCaptura(gCalendar);
		obMetadatos.setTipoDocumental(EnumeracionDocumentoTipoDocumental.TD_99);

		eni.setMetadatos(obMetadatos);

		ArrayList<FirmaENI> listFirmas = new ArrayList<FirmaENI>();
		listFirmas.add(new FirmaENI());
		listFirmas.get(0).setEnumeracionDocumentoTipoFirma(EnumeracionDocumentoTipoFirma.TF_01);
		listFirmas.get(0).setCsv("sample csv code");
		listFirmas.get(0).setRegulacionCsv("sample regulation csv code");

		eni.setFirmas(listFirmas);

		ObjetoDocumentoContenido obDocContenido = new ObjetoDocumentoContenido();
		InputStream fileStream = file.getInputStream();
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		obDocContenido.setContenido(fileStream);
		obDocContenido.setNombreFormato(extension);
		eni.setContenidoDocumento(obDocContenido);

		if (ValidateENI(eni)) {
			File f = gdENI.generateENIToFile(eni);
			FileInputStream input = new FileInputStream(f);
			MultipartFile multipartFile = new MockMultipartFile(
					f.getName().substring(0, f.getName().indexOf('-')) + ".xml",
					f.getName().substring(0, f.getName().indexOf('-')) + ".xml", null,
					IOUtils.toByteArray(input));
			uploadToAlfresco(multipartFile, nomArea, nomAnio, nomConvocatoria, nomX, nomExpediente, nomProceso,
					nomDocumentacion, codArea, codAnio,
					codConvocatoria, codX, codExpediente, codProceso, codDocumentacion, user, password,
					authorizationHeader, gustavoId, ulisesId);

			return new ResponseEntity<>(null, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	public static boolean ValidateENI(ObjetoDocumentoENI file) {
		try {
			ValideDocumentENI.validaDocumentENI(file);
		} catch (DocumentENIValidationException e) {
			return false;
		}
		return true;
	}

	@PostMapping("/createPathsFromExcel")
	@ResponseBody
	@Transactional(readOnly = false)
	public ResponseEntity<String> CreatePaths(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader)
			throws EncryptedDocumentException, IOException {

		if (!JwtUtils.verifyToken(authorizationHeader)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		DataFormatter dataFormatter = new DataFormatter();

		int iRow = 1;

		File f = new File(excelPath);
		InputStream inp = new FileInputStream(f);
		Workbook wb = WorkbookFactory.create(inp);
		Sheet sheet = wb.getSheetAt(0);

		Row row = sheet.getRow(iRow);
		while (row != null) {
			if (pathRepository.findByCodigos(dataFormatter.formatCellValue(row.getCell(1)).trim(),
					dataFormatter.formatCellValue(row.getCell(5)).trim(),
					dataFormatter.formatCellValue(row.getCell(9)).trim(),
					dataFormatter.formatCellValue(row.getCell(13)).trim(),
					dataFormatter.formatCellValue(row.getCell(17)).trim(),
					dataFormatter.formatCellValue(row.getCell(21)).trim()) == null) {

				Path newPath = new Path();
				int i = 0;

				// AREA
				newPath.setNomArea(dataFormatter.formatCellValue(row.getCell(i)).trim());
				i++;
				newPath.setCodArea(row.getCell(i).getRichStringCellValue().toString().trim());
				i++;
				newPath.setTituloArea(row.getCell(i).getRichStringCellValue().toString());
				i++;
				newPath.setDescripcionArea(row.getCell(i).getRichStringCellValue().toString());
				// AÑO
				i++;
				newPath.setNomAnho(dataFormatter.formatCellValue(row.getCell(i)).trim());
				i++; 
				double aux = row.getCell(i).getNumericCellValue();
				newPath.setCodAnho(Double.toString(aux));
				i++;
				newPath.setTituloAnho(row.getCell(i).getRichStringCellValue().toString());
				i++;
				newPath.setDescripcionAnho(row.getCell(i).getRichStringCellValue().toString());
				// CONVOCATORIA
				i++;
				newPath.setNomConvocatoria(dataFormatter.formatCellValue(row.getCell(i)).trim());
				i++;
				newPath.setCodConvocatoria(row.getCell(i).getRichStringCellValue().toString().trim());
				i++;
				newPath.setTituloConvocatoria(row.getCell(i).getRichStringCellValue().toString());
				i++;
				newPath.setDescripcionConvocatoria(row.getCell(i).getRichStringCellValue().toString());
				// X
				i++;
				newPath.setNomX(dataFormatter.formatCellValue(row.getCell(i)).trim());
				i++;
				newPath.setCodX(row.getCell(i).getRichStringCellValue().toString().trim().trim());
				i++;
				newPath.setTituloX(row.getCell(i).getRichStringCellValue().toString());
				i++;
				newPath.setDescripcionX(row.getCell(i).getRichStringCellValue().toString());

				if (!newPath.getCodX().equals("Normativa") && !newPath.getCodX().equals("CO Evaluación")) {
					// EXPEDIENTE
					i++;
					newPath.setNomExpediente(dataFormatter.formatCellValue(row.getCell(i)).trim());
					i++;
					newPath.setCodExpediente(row.getCell(i).getRichStringCellValue().toString().trim());
					i++;
					newPath.setTituloExpediente(row.getCell(i).getRichStringCellValue().toString());
					i++;
					newPath.setDescripcionExpediente(row.getCell(i).getRichStringCellValue().toString());
					// PROCESO
					i++;
					newPath.setNomProceso(dataFormatter.formatCellValue(row.getCell(i)).trim());
					i++;
					newPath.setCodProceso(row.getCell(i).getRichStringCellValue().toString().trim());
					i++;
					newPath.setTituloProceso(row.getCell(i).getRichStringCellValue().toString());
					i++;
					newPath.setDescripcionProceso(row.getCell(i).getRichStringCellValue().toString());
					// DOCUMENTACION
					i++;
					newPath.setNomDocumentacion(dataFormatter.formatCellValue(row.getCell(i)).trim());
					i++;
					newPath.setCodDocumentacion(row.getCell(i).getRichStringCellValue().toString().trim());
					i++;
					newPath.setTituloDocumentacion(row.getCell(i).getRichStringCellValue().toString());
					i++;
					newPath.setDescripcionDocumentacion(row.getCell(i).getRichStringCellValue().toString());
					// PATHS
					newPath.setNomPath(
							documentLibrary + newPath.getNomArea() + "/" + newPath.getNomAnho() + "/"
									+ newPath.getNomConvocatoria() + "/" + newPath.getNomX() + "/" +
									newPath.getNomExpediente() + "/" + newPath.getNomProceso() + "/"
									+ newPath.getNomDocumentacion());

					newPath.setCodPath(
							documentLibrary + newPath.getCodArea() + "/" + newPath.getCodAnho() + "/"
									+ newPath.getCodConvocatoria() + "/" + newPath.getCodX() + "/" +
									newPath.getCodExpediente() + "/" + newPath.getCodProceso() + "/"
									+ newPath.getCodDocumentacion());
				} else {
					i = i + 9;
					// DOCUMENTACION
					newPath.setNomDocumentacion(dataFormatter.formatCellValue(row.getCell(i)).trim());
					i++;
					newPath.setCodDocumentacion(row.getCell(i).getRichStringCellValue().toString().trim());
					i++;
					newPath.setTituloDocumentacion(row.getCell(i).getRichStringCellValue().toString());
					i++;
					newPath.setDescripcionDocumentacion(row.getCell(i).getRichStringCellValue().toString());
					// PATHS
					newPath.setNomPath(
							documentLibrary + newPath.getNomArea() + "/" + newPath.getNomAnho() + "/"
									+ newPath.getNomConvocatoria() + "/" + newPath.getNomX() + "/"
									+ newPath.getNomDocumentacion());

					newPath.setCodPath(
							documentLibrary + newPath.getCodArea() + "/" + newPath.getCodAnho() + "/"
									+ newPath.getCodConvocatoria() + "/" + newPath.getCodX() + "/"
									+ newPath.getCodDocumentacion());
				}

				pathRepository.create(newPath);

				iRow++;
				row = sheet.getRow(iRow);
			}
		}

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping("/GenerateDirectoryStructure")
	@ResponseBody
	public String GenerateDirectoryStructure(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader, String user,
			String pass) {
		if (!JwtUtils.verifyToken(authorizationHeader)
				|| !JwtUtils.extractSubject(authorizationHeader).equals("NOTACOOLADMIN")) {
			System.out.println("Invalid JWT");
			return "";
		}

		List<Path> allPaths = pathRepository.findAll();

		if (allPaths.size() == 0) {
			return "No existen paths";
		} else {
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

			for (int i = 0; i < allPaths.size(); i++) {

				// Creamos las carpetas, pueden ser una o 50
				Folder parent = root;

				String[] aux = allPaths.get(i).getNomPath().split("/");
				for (int index = 0; index < aux.length; index++) {
					parent = createFolder(parent, allPaths.get(i), index, user, pass);
				}
			}
		}

		logger.info("Terminados de crear los " + allPaths.size() + " directorios");

		return "";
	}

	// @RequestMapping(path = "/generate-expediente", method = RequestMethod.POST,
	// consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	// public ResponseEntity<String> GenerateExpedienteENI(
	// @RequestPart("file") MultipartFile file,
	// @RequestHeader("clientID") String clientID,
	// @RequestHeader("clientPass") String clientPass,
	// String MetadatoEstadoElaboracion,
	// String MetadatoOrganos,
	// String MetadatoClasificacion,
	// String MetadatoVersionNTI,
	// String MetadatoInteresados,
	// String MetadatoIdentificador)
	// throws IOException, JAXBException, ConverterException,
	// ExpedientENIValidationException {

	// GregorianCalendar gc = GregorianCalendar.from(ZonedDateTime.now());
	// GenerateExpedientENIImpl geENI = new GenerateExpedientENIImpl();
	// ObjetoExpedienteENI objetoExpedienteENI = new ObjetoExpedienteENI();
	// ObjetoExpedienteIndice objetoExpedienteIndice = new ObjetoExpedienteIndice();
	// ObjetoExpedienteMetadatos objetoExpedienteMetadatos = new
	// ObjetoExpedienteMetadatos();
	// ObjetoExpedienteMetadatosEnumeracionEstados estado =
	// ObjetoExpedienteMetadatosEnumeracionEstados.E_01;
	// ObjetoExpedienteVersion objetoExpedienteVersion = new
	// ObjetoExpedienteVersion(1, gc);
	// Calendar fechaAperturaExpediente = Calendar.getInstance();
	// ObjetoExpedienteIndiceContenido objetoExpedienteIndiceContenido = new
	// ObjetoExpedienteIndiceContenido();
	// List<String> listInteresados = new ArrayList<>();
	// listInteresados.add(MetadatoInteresados);
	// List<String> listOrganos = new ArrayList<>();
	// listOrganos.add(MetadatoOrganos);

	// // METADATOS
	// switch (MetadatoEstadoElaboracion) {
	// default:
	// estado = ObjetoExpedienteMetadatosEnumeracionEstados.E_01;
	// break;
	// case "ABIERTO":
	// estado = ObjetoExpedienteMetadatosEnumeracionEstados.E_01;
	// break;
	// case "CERRADO":
	// estado = ObjetoExpedienteMetadatosEnumeracionEstados.E_02;
	// break;
	// case "INDICE PARA REMISION CERRADO":
	// estado = ObjetoExpedienteMetadatosEnumeracionEstados.E_03;
	// break;
	// }

	// objetoExpedienteMetadatos.setIdentificadorExpediente(MetadatoIdentificador);
	// objetoExpedienteMetadatos.setOrgano(listOrganos);
	// objetoExpedienteMetadatos.setClasificacion(MetadatoClasificacion);
	// objetoExpedienteMetadatos.setEstado(estado);
	// objetoExpedienteMetadatos.setFechaAperturaExpediente(fechaAperturaExpediente);
	// objetoExpedienteMetadatos.setInteresado(listInteresados);
	// objetoExpedienteMetadatos.setVersionNTI(MetadatoVersionNTI);

	// objetoExpedienteENI.setMetadatos(objetoExpedienteMetadatos);

	// // INDICE
	// ArrayList<FirmaENI> listFirmas = new ArrayList<FirmaENI>();
	// ContenidoFirmaCertificado contenidoFirmaCertificado = new
	// ContenidoFirmaCertificadoReferencia();
	// listFirmas.add(new FirmaENI());
	// listFirmas.get(0).setEnumeracionDocumentoTipoFirma(EnumeracionDocumentoTipoFirma.TF_03);
	// listFirmas.get(0).setContenidoFirmaDocument(contenidoFirmaCertificado);

	// List<ObjetoExpedienteIndiceContenidoElementoIndizado>
	// listObjetoExpedienteIndiceContenidoElementoIndizados = new
	// ArrayList<ObjetoExpedienteIndiceContenidoElementoIndizado>();
	// ObjetoExpedienteIndiceContenidoElementoIndizado
	// objetoExpedienteIndiceContenidoElementoIndizado = new
	// ObjetoExpedienteIndiceContenidoElementoIndizado() {
	// };
	// objetoExpedienteIndiceContenidoElementoIndizado.setOrden(1);

	// listObjetoExpedienteIndiceContenidoElementoIndizados.add(objetoExpedienteIndiceContenidoElementoIndizado);

	// objetoExpedienteIndiceContenido
	// .setIdentificadorExpedienteAsociado("EXP_INDICE_CONTENIDO" +
	// MetadatoIdentificador);
	// objetoExpedienteIndiceContenido.setFechaIndiceElectronico(Calendar.getInstance());
	// objetoExpedienteIndiceContenido.setOrden(1);
	// objetoExpedienteIndiceContenido.setElementosIndizados(listObjetoExpedienteIndiceContenidoElementoIndizados);

	// objetoExpedienteIndice.setFirmas(listFirmas);
	// objetoExpedienteIndice.setIndiceContenido(objetoExpedienteIndiceContenido);

	// objetoExpedienteENI.setIndice(objetoExpedienteIndice);

	// // CONTENIDO
	// ObjetoDocumentoContenido obDocContenido = new ObjetoDocumentoContenido();
	// InputStream fileStream = file.getInputStream();
	// String extension = FilenameUtils.getExtension(file.getOriginalFilename());
	// obDocContenido.setContenido(fileStream);
	// obDocContenido.setNombreFormato(extension);
	// objetoExpedienteENI.setVisualizacionIndice(obDocContenido);

	// objetoExpedienteENI.setVersion(objetoExpedienteVersion);

	// geENI.generateENIToFile(objetoExpedienteENI);

	// return new ResponseEntity<>(null, HttpStatus.OK);
	// }
}
