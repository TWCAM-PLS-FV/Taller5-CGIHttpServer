#!/bin/bash
number=$1
# Se pone el valor 10 por defecto si no pasan argumentos
test -z $number && number=10
# Aumentamos en uno ya que la primera línea es la cabecera:
procs=$(echo "$number + 1" | bc)
# Obtención de los procesos
cat head.txt
ps -eo pmem,pcpu,comm --sort=-pmem | head -n $procs | sed 's|$|<br/><br/>|g'
cat headclose.txt