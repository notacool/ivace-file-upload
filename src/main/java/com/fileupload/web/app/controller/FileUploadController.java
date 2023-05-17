package com.fileupload.web.app.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import com.fileupload.web.app.model.CuadroClasificacionDataSets;
import com.fileupload.web.app.model.TCredentials;
import com.fileupload.web.app.repository.CredentialsRepository;
import com.fileupload.web.app.security.JwtUtils;
import com.fileupload.web.app.validator.RequestValidator;

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
	@Autowired
    private JwtUtils jwtUtil;
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
		
		if (!jwtUtil.verifyToken(authorizationHeader)) {
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
			documentDestination = "Sites/ivace/documentLibrary/"+codArea+"/"+codAnio+"/"+codConvocatoria+"/"+codExpediente+"/"+codProceso+"/"+codDocumentacion;

			if(!validator.isValidRequest(metadata)) {
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
				parent = createFolder(folderName, parent,"");
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
			try {
				if (gustavoId != "") {
					properties2.put("ids:gustavoID", gustavoId);
				} else if (ulisesId != "") {
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
			return jwtUtil.generateToken(clientID);
		}
	}

	public void generateFolderTag(String folderPath,String tag) {
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, user);
		parameter.put(SessionParameter.PASSWORD, pass);
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		Session session = factory.getRepositories(parameter).get(0).createSession();
		CmisObject cmisObject = session.getObjectByPath("/"+folderPath);
		String nodeId = cmisObject.getId();
		RestTemplate restTemplate = new RestTemplate();
        String url = "https://ivace.notacool.com/alfresco/api/-default-/public/alfresco/versions/1/nodes/"+nodeId+"/tags";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, pass);
        headers.setContentType(MediaType.APPLICATION_JSON);
		String jsonTag = "{\"tag\":\""
				+ tag
				+ "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonTag, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

	}
	@PostMapping("/generateDirStructure")
	@ResponseBody
	public String generateDirStruct(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
		//Only allowed for the JWT associated with the NOTACOOLADMIN user
		if (!jwtUtil.verifyToken(authorizationHeader) || jwtUtil.extractSubject(authorizationHeader) != "NOTACOOLADMIN") {
			System.out.println("Invalid JWT");
			return "";
		}
		
		ArrayList<String> list = new ArrayList<String>();
		String[] literals = new String[5];
		cuadroClasificacion.getMapaAreas().forEach((k, v) -> {
			logger.info(k + " " + v);
			literals[0] = documentLibrary + k;
			list.add(documentLibrary + k);
			cuadroClasificacion.getMapaAnios().forEach((i, b) -> {
				list.add(literals[0] + "/" + i);
				literals[1] = literals[0] + "/" + i;
				cuadroClasificacion.getMapaConvocatorias().forEach((c, r) -> {
					list.add(literals[1] + "/" + c);
					literals[2] = literals[1] + "/" + c;
					cuadroClasificacion.getMapaExpedientes().forEach((l, m) -> {
						list.add(literals[2] + "/" + l);
						literals[3] = literals[2] + "/" + l;
						cuadroClasificacion.getMapaProcesos().forEach((q, w) -> {
							list.add(literals[3] + "/" + q);
							literals[4] = literals[3] + "/" + q;
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
								list.add(literals[4] + "/" + d);
							});
						});
					});
				});
			});
		});
		
		
		
		
		
		Map<String, Object> properties = new HashMap<String, Object>();
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
					
		//recorremos la lista de directorios
		for(int i=0;i<list.size();i++) {
			parent = root;
			if (list.get(i).contains("/")) {
				parts = list.get(i).split("/");
			} else {
				parts = new String[1];
				parts[0] = list.get(i);
			}

			for (String folderName : parts) {
				parent = createFolder(folderName, parent,list.get(i));
			}
			logger.info("Creando el directorio: "+list.get(i));
		}
		logger.info("Terminados de crear los " + list.size() + " directorios");
		
		
		return "";
	}
	
	
	@GetMapping("/getByGustavo")
	@ResponseBody
	public ResponseEntity<String> getByGustavoId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("gustavoID") int gustavoID) {
		if (!jwtUtil.verifyToken(authorizationHeader)) {
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
					return new ResponseEntity<>("Alfresco NodeId: " + fileId, HttpStatus.OK);
				}
			}
		}
		if(fileId.equals(""))  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else return new ResponseEntity<>(fileId, HttpStatus.OK);
	}

	public String find(Folder r, int externalId, int type) {
		Folder folder = (Folder) r;
		String fileId = "";
		String out= "";
		for (CmisObject child : folder.getChildren()) {
			if (child.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
				out = find((Folder) child, externalId, type);
				if (out != "") {
					return out;
				}
			} else if (child.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
				//externalId -> gustavo:0 ulises:1
				if(type == 0) {
					if (child.getDescription() != null) {
						if(child.getProperty("ids:gustavoID").getFirstValue().equals(""+externalId)) {
							fileId = child.getId();
							return fileId;
						}
					}
				} else {
					if (child.getDescription() != null) {
						if(child.getProperty("ids:ulisesID").getFirstValue().equals(""+externalId)) {
							fileId = child.getId();
							return fileId;
						}
					}
				}
			}
		}
		return fileId;
	}

	@GetMapping("/getByUlises")
	@ResponseBody
	public ResponseEntity<String> getByUlisesId(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader("ulisesID") int ulisesID) {
		if (!jwtUtil.verifyToken(authorizationHeader)) {
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
					return new ResponseEntity<>("Alfresco NodeId: "+fileId, HttpStatus.OK);
				}
			}
		}
		
		if(fileId.equals(""))  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else return new ResponseEntity<>(fileId, HttpStatus.OK);
		
	}

	
	public Folder createFolder(String folderName, Folder root, String fullPath) {
		Boolean folderExists = false;
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, folderName);
		LinkedHashMap<String, String> mapaAsociado = null;
		String description = null;
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
			String[] splittedPath = fullPath.split("/");
			if(splittedPath.length == 4) {
				logger.info("Estamos creando el Area");
				mapaAsociado = cuadroClasificacion.getMapaAreas();
				description = mapaAsociado.get(splittedPath[3]);
				tag = description;
				}
			if(splittedPath.length == 5) {logger.info("Estamos creando el Año");}
			if(splittedPath.length == 6) {
				logger.info("Estamos creando la convocatoria");
				mapaAsociado = cuadroClasificacion.getMapaConvocatorias();
				description = mapaAsociado.get(splittedPath[5]);
				tag = description;
			}
			if(splittedPath.length == 7) {
				logger.info("Estamos creando el expediente");
				mapaAsociado = cuadroClasificacion.getMapaExpedientes();
				description = mapaAsociado.get(splittedPath[6]);
				tag = description;
				}
			if(splittedPath.length == 8) {
				logger.info("Estamos creando el proceso");
				mapaAsociado = cuadroClasificacion.getMapaProcesos();
				description = mapaAsociado.get(splittedPath[7]);
				tag = description;
			}
			if(splittedPath.length == 9) {
				logger.info("Estamos creando el documento");
				switch(splittedPath[7]) {
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
				description = mapaAsociado.get(splittedPath[8]);
				tag = description;
			}

			
			properties.put(PropertyIds.DESCRIPTION, description);

			parent = root.createFolder(properties);
			if (tag.length() != 0) {
				generateFolderTag(fullPath, tag);
			}
		} else {
		}
		return parent;
	}

}
