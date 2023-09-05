package com.fileupload.web.app.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class FolderController {

    Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Value("${alfresco.user}")
    private String user;

    @Value("${alfresco.pass}")
    private String pass;

    @Value("${alfresco.url}")
    private String url;

    @Value("${alfresco.documentLibrary}")
    private String documentLibrary;

    public Folder createFolder(String fullPathCod, String fullPathNom) {
        String[] splittedPathNom = fullPathNom.split("/");
        String[] splittedPathCod = fullPathCod.split("/");
        Folder createdFolder = null;

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

        for(int i = 0; i < splittedPathCod.length; i++){
            String description = "";
            String title = "";
            String path = "/";
            String name = splittedPathNom[i];

            Map<String, Object> properties = new HashMap<>();
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            properties.put(PropertyIds.NAME, name);

            for(int j = 0; j <= i; j++){
                path += splittedPathNom[j] + "/";
            }

            for (int index = 3; index <= i; index++) {
                description += splittedPathNom[index] + " ";
                title += splittedPathCod[index] + " ";
            }

            int indexOfSeparator = path.lastIndexOf("/");
            path = path.substring(0, indexOfSeparator);

            //Buscamos la carpeta por path
            try {
                //Si ya existe, pasamos a la siguiente
                session.getObjectByPath(path);
            } catch (Exception ex) {

                //Si no existe, cogemos el path del parent y la creamos tanto en alfresco como en BD
                indexOfSeparator = path.lastIndexOf("/");
                path = path.substring(0, path.lastIndexOf("/"));
                Folder folderToCreate = (Folder)session.getObjectByPath(path);
                List<String> secondary = Collections.singletonList("P:cm:titled");
                properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondary);
                properties.put("cm:title", title);
                properties.put(PropertyIds.DESCRIPTION, description);

                createdFolder = folderToCreate.createFolder(properties);
                logger.info("Creada la carpeta: " + folderToCreate.getName());
            }
        }
        return createdFolder;
    }
}
