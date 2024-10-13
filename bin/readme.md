# API de comunicacion con Alfresco

Esta API es la encargada de realizar la comunicación entre el Gestor Documental del IVACE (Alfresco) y las aplicaciones pre-existentes y futuras del flujo de trabajo del IVACE.

## Instalacion

Desplegar el docker-compose (Docker y Docker-compose requeridos) de la carpeta raiz del proyecto.

```bash
docker-compose up
```

## Uso, parametros y ejemplos.


# Autenticar un usuario
POST:/login
* Este es el endpoint inicial de la API mediante el cual, en caso de adjuntar las credenciales correctas, se nos devolvera un JWT (Una cadena de texto) mediante la cual seremos capaces de autenticarnos para poder usar el resto de funcionalidades de la API.
Se le adjuntaran las siguientes cabeceras a la peticion: 
  * ClientID: El usuario que queremos autenticar
  * ClientPass: La contraseña del usuario que queremos autenticar

* Ejemplo de llamada: http://localhost:8080/login


# Realizar la subida de un documento al Gestor Documental
POST:/uploadFile/{codArea}/{codAnio}/{codConvocatoria}/{codExpediente}/{codProceso}/{codDocumentacion}

* Este endpoint es el encargado de subir un documento a la ruta especificada, que debe ser contemplada dentro del cuadro de clasificacion que previamente se elaboró.
* La autenticacion se realiza adjuntando a la cabecera Authorization el token obtenido en /login.
* Opcionalmente, se puede añadir una cabecera GustavoId o UlisesId para que etiquete el documento dentro del propio gestor documental.

* El documento a subir se adjunta en el cuerpo de la peticion.

* Ejemplo de llamada: http://localhost:8080/uploadFile/A01/2023/0001.23/999/P02/D01

# Descargar un documento con el id de Gustavo especificado
GET:/getByGustavo

* Descarga un documento que este etiquetado con el identificador de gustavo adjunto.
* La autenticacion se realiza adjuntando a la cabecera Authorization el token obtenido en /login.
* El identificador del documento se establece como una cabecera llamada gustavoID adjunta a la peticion.

* Ejemplo de llamada: http://localhost:8080/getByGustavo

# Descargar un documento con el id de Ulises especificado
GET:/getByUlises

* Descarga un documento que este etiquetado con el identificador de Ulises adjunto.
* La autenticacion se realiza adjuntando a la cabecera Authorization el token obtenido en /login.
* El identificador del documento se establece como una cabecera llamada UlisesID adjunta a la peticion.

* Ejemplo de llamada: http://localhost:8080/getByUlises

# Inicializacion de los directorios del cuadro de clasificacion.

* POST:/generateDirStructure

* Función de inicialización encargada de generar toda la estructura de directorios especificada por el cuadro de clasificación elaborado previamente.
* La autenticación se realiza adjuntando a la cabecera Authorization el token obtenido en /login.
* Esta funcionalidad solo es accesible mediante el token generado en /login correspondiente al usuario NOTACOOLADMIN.

* Ejemplo de llamada: http://localhost:8080/generateDirStructure


