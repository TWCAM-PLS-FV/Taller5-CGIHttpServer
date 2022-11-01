# Servidor HTTP que delega en un proceso la generación de la respuesta

Código de un servidor HTTP desarrollado en Java que delega en un proceso la generación de la respuesta (tipo CGI).

Usa un fichero de propiedades config.ini que permite configurar:
 - El número de hilos máximos a usar
 - El puerto en el que escucha el servidor
 - El path de la petición HTTP que provoca la ejecución del proceso
 - El proceso a ejecutar en el servidor
 - El orden de los paramámetros para pasarlos al proceso
 - El tipo MIME de respuesta generada
 - El directorio temporal donde se generan artefactos intermedios
 - Información sobre el servidor para entregarla en el campo de cabecera de respuesta
Estos valores también se pueden pasar como variables de entornos (pensando en encapsular esto en un contenedor)

```
NTHREADS=50
PORT=8080
PATH=/ls
PROCESS=/bin/ls
PARAMS=options dir
CONTENT_TYPE=text/plain
TMP_DIR=/tmp
SERVER_INFO=Fantastic dynamic HTTP server (version 1.0)
```

El proyecto tiene una estructura Maven. Para ejecutar el servidor:
```
mvn package
java -jar target/cgi-server-1.0.jar
```

Sample calls:

* Ejemplo de petición GET
```
curl -v "http://localhost:8080/ls?dir=/tmp&options=-latorh"
```

* Petición de petición POST
```
curl -v -X POST -d "dir=/tmp&options=-latorh" http://localhost:8080/ls
```
