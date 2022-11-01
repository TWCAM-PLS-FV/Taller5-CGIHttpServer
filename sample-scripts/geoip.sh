#!/bin/bash

#Archivo con las IPS
AUTH=/tmp/sample-data/auth.txt
#Bases de datos de GeoLite2 obtenidas de MaxMind
ASNGEODATA=/tmp/geodata/GeoLite2-ASN.mmdb
CITYGEODATA=/tmp/geodata/GeoLite2-City.mmdb
COUNTRYGEODATA=/tmp/geodata/GeoLite2-Country.mmdb

# Proceso a realizar
Proceso=$1
# 1. Generar la URL con el formato de Google
# 2. Ejercicio extra: Generar la URL y crear el mapa de las localizaciones.

#Si no se especifican el proceso a realizar entonces por defecto es 1
if [ -z $P ]
then
let P=1
fi

#Creo el array que con el contenido relevante del archivo AUTH: direcciones IP sin repetir
declare -a Array=($(cat $AUTH | awk '{print $8}' | uniq))

#Función para crear la URL del valor que recibe por parametro
function crearURL(){
    IP=$1
    LAT=$2
    LON=$3
    COUNTRY=$4
    echo "<a href=https://www.google.com/maps/@"$LAT","$LON",16z>"$COUNTRY":" $IP"</a> <br/>"
    echo $'\n'
}

#Funcion para crear los Mapas con OpenLayers y OpenStreeMap
NumeroMapa=0
function crearMapa(){
    IP=$1
    LAT=$2
    LON=$3
    COUNTRY=$4
    echo "<p>"$COUNTRY":"$IP"</p>"
    echo "<div id='map"$NumeroMapa"' class='map'></div>"
    echo "<script>"
    echo "var lyrOSM = new ol.layer.Tile({"
    echo "title: 'OSM',"
    echo "type: 'base',"
    echo "visible: true,"
    echo "source: new ol.source.OSM()})"
    echo "var map = new ol.Map({"
    echo "target: 'map"$NumeroMapa"',"
    echo "layers: [new ol.layer.Group({title: 'Mapa base',layers: [lyrOSM]})],"
    echo "view: new ol.View({center: ol.proj.fromLonLat(["$LON", "$LAT"]),"
    echo "zoom: 16, maxZoom: 20, minZoom: 10})})" 
    echo "</script>"
    echo "<br>"
    let NumeroMapa+=1
}

#Función para obtener la LAT y LONG de una IP recibida por parametro
#Una vez se obtienen los valores de Latitud, Longitud y la Ciudad, y dependiendo de la solicitud
#se crea el enlace con la información para google maps o el layer con el mapa de OpenStreetMaps.
function obtenerLatLon(){
    IP=$1
    LAT="$(mmdblookup --file $CITYGEODATA --ip $IP location latitude | awk '{print $1}')"
    LON="$(mmdblookup --file $CITYGEODATA --ip $IP location longitude | awk '{print $1}')"
    COUNTRY="$(mmdblookup --file $CITYGEODATA --ip $IP country names en | awk '{print $1}')"
    if [ "$Proceso" == "2" ]
    then
    crearMapa $IP $LAT $LON $COUNTRY
    else
    crearURL $IP $LAT $LON $COUNTRY
    fi
}

function start(){
    J=0
    while [ $J -lt "${#Array[@]}"  ]; do
    echo "Ubicación:" $(($J+1))
    obtenerLatLon "${Array[$J]}"
    let J+=1
    done
}


echo "<!DOCTYPE html>"
echo "<html lang='en'>"
echo "<head><title>Taller 5</title>"
echo "<meta charset='UTF-8'>"
echo "<meta http-equiv='X-UA-Compatible' content='IE=edge'>"
echo "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
echo "<script src='https://cdn.jsdelivr.net/npm/ol@v7.1.0/dist/ol.js'></script>"
echo "<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/ol@v7.1.0/ol.css'>"
echo "<style>.map {width: 500px;height: 300px;}</style>"
echo "</head>"
echo "<body>"
start
echo "</body></html>"