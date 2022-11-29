package com.fileupload.web.app.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {
	
	
	@PostMapping("/uploadFile")
	@ResponseBody
	public Boolean uploadToAlfresco (@RequestParam("file") MultipartFile file) {
		try {	
			byte[] fileContent = file.getBytes();
			System.out.println(file.getOriginalFilename());
			
			
			String folderName = "Nueva Carpeta";
			Boolean folderExists = false, fileExists = false;

			// default factory implementation
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();
			
			// user credentials
			parameter.put(SessionParameter.USER, "admin");
			parameter.put(SessionParameter.PASSWORD, "admin");
			// connection settings
			parameter.put(SessionParameter.ATOMPUB_URL,
					"https://ivace.notacool.com/alfresco/api/-default-/public/cmis/versions/1.1/atom");
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			
			// create session
			Session session = factory.getRepositories(parameter).get(0).createSession();
			Folder root = session.getRootFolder();
			// properties
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

			for (CmisObject r : parent.getChildren()) {
				if (r.getName().equals(file.getOriginalFilename())) {
					fileExists = true;
				}
			}

			if (fileExists) {
				System.out.println("Ya hay un archivo con ese nombre");
				return false;
			}
			// properties
			Map<String, Object> properties2 = new HashMap<String, Object>();
			properties2.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			properties2.put(PropertyIds.NAME, file.getOriginalFilename());
			// content

			InputStream stream = new ByteArrayInputStream(fileContent);
			ContentStream contentStream = new ContentStreamImpl(file.getOriginalFilename(), BigInteger.valueOf(fileContent.length),
					"text/plain", stream);
			// create a major version
			Document newDoc = parent.createDocument(properties2, contentStream, VersioningState.MAJOR);
			
			System.out.println("DONE.");
			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace());
			return false;
		}
	}
}
