# Imagen base de Java
FROM openjdk:17-jdk-slim

# Copiar el proyecto a la imagen
COPY . /app

# Establecer la carpeta de trabajo
WORKDIR /app

# Dar permisos de ejecución al script mvnw
RUN chmod +x mvnw

# Instalar dos2unix
RUN apt-get update && apt-get install dos2unix

# Convertir el script mvnw a formato UNIX
RUN dos2unix mvnw

# Ejecutar el build del proyecto
RUN ./mvnw clean package

# Establecer el comando para arrancar la aplicación
CMD ["java", "-jar", "target/AlfrescoFileUpload-0.0.1-SNAPSHOT.jar"]
