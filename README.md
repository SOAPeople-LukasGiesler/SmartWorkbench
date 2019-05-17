# SmartWorkbench
Die Smart Workbench soll als visuelle Untersetzung in einer Montagetätigkeit fungieren. An einer Werkbank, auf welcher Werkstücke montiert werden müssen, wird das System zukünftig eingesetzt. Der Mitarbeiter wird über ein Display informiert, welcher Montageschritt zu bearbeiten ist und welche Teile hierfür notwendig sind. Die notwendigen Teile werden in speziellen Boxen gelagert, in welchen die Sensorik direkt verbaut ist. Zum einen ist es eine LED, welche die für den jeweiligen Schritt passende Box bzw. das passende Teil anzeigt. Zum anderen ist ein Infrarotsensor verbaut, welcher Bewegungen erkennt. Greift der Mitarbeiter nun in die richtige Box, wird dies vom System erkannt und der nächste Prozessschritt kann initiiert werden. Dies ist die Planung für den ersten Prototyp. Es bestehen weitere Ideen (z.B. Gewichtsmessung der Teile, um deren Anzahl zu bestimmen), welche jedoch erst bei ausreichender Zeit näher verfolgt werden.

Aus diesem Grund muss zunächst ein Web Frontend für das Display sowie die Steuerung der Sensorik entwickelt werden. Ebenso ist die Kommunikation zwischen Web Frontend, Server und Raspberry Pi ein entscheidender Aspekt, welcher direkt bearbeitet und getestet wird. Hierfür wurden bereits einige Aufgaben erstellt und in die Planung aufgenommen.

## Details
Das Frontend und damit das Dashboard des Projekts wird mit dem React Framework realisiert. 

Zur Kommunikation wird ein MQTT Server genutzt. 

Die Boxen werden mit einem 3D-Drucker erstellt und mit Sensoren bestückt, die über ein Raspberry Pi genutzt werden können. 

## Contributing
Teilnahme am Projekt ist derzeit noch nicht möglich.

## Installation
Details zur Installation folgen.