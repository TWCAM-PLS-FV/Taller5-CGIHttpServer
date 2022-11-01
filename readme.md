# CGI HTTP SERVER

## Autor 📋
**Felipe Valencia** - fevabac@alumni.uv.es

## Prerequisitos 🔧

### 1. Instalar paquetes para obtención de datos

Para la obtención de los datos de las direcciones IP, en el ejemplo de la transparencia, se utiliza la base de datos *GeoLiteCity.dat* pero dicha base de datos, en los repositorios oficiales la encontré en estado *"Deprecated"*. Por lo tanto, realicé la actualización de las bases de datos a las versiones más recientes de [MaxMind Developers](https://dev.maxmind.com/geoip/docs/web-services?lang=en).

El proceso para actualizar las bases de datos es el siguiente:

* Agregar el repositorio MAXMIND
```sh
add-apt-repository ppa:maxmind/ppa
```

* Instalar el paquete GeoIPUpdate
```sh
apt-get update
apt-get install geoipupdate
```

* Editar el archivo de configuración GeoIPUpdate de la ruta /etc/GeoIP.conf, añadiendo el ID de la cuenta y licencia generados en [MaxMind Developers](https://www.maxmind.com/en/accounts/785057/license-key)
```sh
AccountID YOUR_ACCOUNT_ID_HERE
LicenseKey YOUR_LICENSE_KEY_HERE
```

* Actualizar la base de datos (Esto no sobreescribe las bases de datos anteriores, de igual manera, en unas secciones más adelante se deja la URL de un repositorio privado que contiene las bases de datos para su descarga directa)
```sh
geoipupdate -v
```

* Debido a que las bases de datos están en formato *"mmdb"* se debe de instalar un nuevo paquete
```sh
apt-get update
apt-get install mmdb-bin
```

### 2. Ubicar los archivos en las rutas correspondientes
* Archivo con las direcciones IP
```sh
/tmp/sample-data/auth.txt
```

* Base de datos GeoLiteCity. Para la descarga de las bases de datos: [Clic aquí](https://universitatdevalencia-my.sharepoint.com/:u:/g/personal/fevabac_alumni_uv_es/EaVgf5woD3FPqwa5oTyURjcBdxr5w4WIHPunfOu4XkUFxA?e=aMy4es)
```sh
/tmp/geodata/GeoLite2-City.mmdb
```

* Archivo .sh
```sh
/tmp/sample-scripts/geoip.sh
```

## Ejecución 🚀

Se encuentran implementados dos métodos:

1. Obtener un enlace a google maps con todas las direcciones IP (Por defecto realiza esta opción si no se pasa el parámetro number)
```sh
http://localhost:8080/geoip?number=1
```
![](/src/main/resources/1.PNG)

2. Generar un mapa en el que se muestra la localización (Realizado con OpenLayer y OpenStreetMap)
```sh
http://localhost:8080/geoip?number=2
```
![](/src/main/resources/2.PNG)