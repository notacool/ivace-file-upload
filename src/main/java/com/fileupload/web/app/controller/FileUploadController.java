package com.fileupload.web.app.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fileupload.web.app.model.TCredentials;
import com.fileupload.web.app.repository.CredentialsRepository;
import com.fileupload.web.app.security.JwtUtils;
import com.fileupload.web.app.validator.RequestValidator;

@Controller
public class FileUploadController {

	@Autowired
	CredentialsRepository credRepo;

	@Autowired
	RequestValidator validator;
	@Value("${alfresco.user}")
	private String user;
	@Value("${alfresco.pass}")
	private String pass;
	@Value("${alfresco.url}")
	private String url;
	@Autowired
    private JwtUtils jwtUtil;
	@PostMapping("/uploadFile/{codArea}/{codAnio}/{codConvocatoria}/{codExpediente}/{codProceso}/{codDocumentacion}")
	@ResponseBody
	public ResponseEntity<String> uploadToAlfresco(@RequestHeader("clientID") String clientID,
			@RequestHeader("clientPass") String clientPass, @RequestParam("file") MultipartFile file,
			@PathVariable("codArea") String codArea, @PathVariable("codAnio") String codAnio,
			@PathVariable("codConvocatoria") String codConvocatoria,
			@PathVariable("codExpediente") String codExpediente, @PathVariable("codProceso") String codProceso,
			@PathVariable("codDocumentacion") String codDocumentacion,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			int gustavoId, int ulisesId) {
		//validate JWT
		
		if(!jwtUtil.verifyToken(authorizationHeader)) {
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
			
			
			TCredentials cred = credRepo.checkCredentials(clientID, clientPass);
			if (cred == null) {
				System.out.println("Invalid credentials");
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid credentials.");
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
				parent = createFolder(folderName, parent);
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
			parent.createDocument(properties2, contentStream, VersioningState.MAJOR);		
			

			System.out.println("DONE.");
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
		if (cred == null) {
			System.out.println("Invalid credentials");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid credentials.");
		} else {
			return jwtUtil.generateToken(clientID);
		}
	}
	
	@GetMapping("/getByGustavo")
	@ResponseBody
	public ResponseEntity<String> getByGustavoId(@RequestHeader("clientID") String clientID,
			@RequestHeader("clientPass") String clientPass, int gustavoId) {
		TCredentials cred = credRepo.checkCredentials(clientID, clientPass);
		if (cred == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid credentials.");
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
				fileId = find((Folder) r, gustavoId, 0);
				if(!fileId.equals("")) return new ResponseEntity<>(fileId, HttpStatus.OK);
			}
		}
		if(fileId.equals(""))  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else return new ResponseEntity<>(fileId, HttpStatus.OK);
	}

	public String find(Folder r, int gustavoId, int type) {
		Folder folder = (Folder) r;
		for (CmisObject child : folder.getChildren()) {
			if (child.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
				find((Folder) child, gustavoId, type);
			} else if (child.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
				// SEPARAMOS LA DESCRIPCION COGIENDO LA PRIMERA PARTE
				if (child.getDescription() != null) {
					if (child.getDescription().split("&&&")[type].equals(gustavoId + "")) {
						return child.getId();
					}
				}
			}
		}
		return "";
	}

	@GetMapping("/getByUlises")
	@ResponseBody
	public ResponseEntity<String> getByUlisesId(@RequestHeader("clientID") String clientID,
			@RequestHeader("clientPass") String clientPass, int ulisesID) {
		TCredentials cred = credRepo.checkCredentials(clientID, clientPass);
		if (cred == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid credentials.");
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
				if(!fileId.equals("")) return new ResponseEntity<>(fileId, HttpStatus.OK);
			}
		}
		
		if(fileId.equals(""))  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		else return new ResponseEntity<>(fileId, HttpStatus.OK);
		
	}

	public Folder createFolder(String folderName, Folder root) {
		Boolean folderExists = false;
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, folderName);

		Folder parent = null;
		for (CmisObject r : root.getChildren()) {
			if (r.getName().equals(folderName)) {
				folderExists = true;
				parent = (Folder) r;
			}
		}
		// create the folder
		if (!folderExists)
			parent = root.createFolder(properties);

		return parent;
	}

}
