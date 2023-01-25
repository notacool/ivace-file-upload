package com.fileupload.web.app.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Property;
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
import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.fileupload.web.app.model.TCredentials;
import com.fileupload.web.app.repository.CredentialsRepository;

@Controller
public class FileUploadController {

	@Autowired
	CredentialsRepository credRepo;

	@Value("${alfresco.user}")
	private String user;
	@Value("${alfresco.pass}")
	private String pass;
	@Value("${alfresco.url}")
	private String url;

	@PostMapping("/uploadFile")
	@ResponseBody
	public Boolean uploadToAlfresco(@RequestHeader("clientID") String clientID,
			@RequestHeader("clientPass") String clientPass, @RequestParam("file") MultipartFile file, String folder,
			int gustavoId, int ulisesId) {
		try {
			TCredentials cred = credRepo.checkCredentials(clientID, clientPass);
			if (cred == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid credentials.");
			}
			// if(gustavoId == null) gustavoId = 0;
			// if(ulisesId == null) ulisesId = 0;

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
			if (folder.contains("/")) {
				parts = folder.split("/");
			} else {
				parts = new String[1];
				parts[0] = folder;
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
				return false;
			}

			// Si el archivo no existe en ese directorio lo creamos.
			Map<String, String> properties2 = new HashMap<String, String>();
			properties2.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			properties2.put(PropertyIds.NAME, file.getOriginalFilename());
			properties2.put(PropertyIds.DESCRIPTION, gustavoId + "&&&" + ulisesId);

			InputStream stream = new ByteArrayInputStream(fileContent);
			ContentStream contentStream = new ContentStreamImpl(file.getOriginalFilename(),
					BigInteger.valueOf(fileContent.length), "text/plain", stream);

			// Creamos el documento en el Alfresco

			parent.createDocument(properties2, contentStream, VersioningState.MAJOR);			

			System.out.println("DONE.");
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return false;
		}
	}

	@GetMapping("/getByGustavo")
	@ResponseBody
	public String getByGustavoId(@RequestHeader("clientID") String clientID,
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
			}
		}
		return fileId;
	}

	public String find(Folder r, int gustavoId, int type) {
		Folder folder = (Folder) r;
		for (CmisObject child : folder.getChildren()) {
			if (child.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
				find((Folder) child, gustavoId, type);
			} else if (child.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
				// SEPARAMOS LA DESCRIPCION COGIENDO LA PRIMERA PARTE
				if (child.getDescription() != null) {
					System.out.println(child.getDescription());
					if (child.getDescription().split("&&&")[type].equals(gustavoId + "")) {
						System.out.println("aaaaa");
						return child.getId();
					}
				}
			}
		}
		return "Not found";
	}

	@GetMapping("/getByUlises")
	@ResponseBody
	public String getByUlisesId(@RequestHeader("clientID") String clientID,
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
			}
		}
		return fileId;
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
