#Administrador de Archivos Multimedia

#Requisitos del Sistema

- Sistema Operativo: Windows, macOS o Linux.

- Java Development Kit (JDK): Versión 8 o superior.

- NetBeans IDE: Preferiblemente la versión utilizada en el desarrollo (NetBeans 12 o superior).

-Memoria RAM: Mínimo 2 GB.

- Espacio en Disco: Al menos 200 MB para la instalación, más espacio adicional para los archivos multimedia.

#Dependencias

- La aplicación utiliza las siguientes librerías externas:

```
Gson: Para la serialización y deserialización de objetos JSON.
Jaudiotagger: Para extraer metadatos de archivos de audio MP3.
Apache Tika: Para extraer metadatos de archivos WMA.
Metadata Extractor: Para extraer metadatos de imágenes y videos.
VLCJ: Para la reproducción de archivos multimedia (audio y video).
Nota: Las dependencias están gestionadas a través de Maven y están incluidas en el archivo pom.xml.
```
#Instrucciones de Instalación

- Paso 1: Clonar el Repositorio

Abra una terminal y ejecute el siguiente comando para clonar el repositorio:

```
git clone https://github.com/tu_usuario/administrador-archivos-multimedia.git
```
- Paso 2: Importar el Proyecto en NetBeans

Abra NetBeans IDE.

Vaya a File -> Open Project.

Navegue hasta la carpeta donde clonó el repositorio.

Seleccione la carpeta del proyecto administrador-archivos-multimedia y haga clic en Open Project.

- Paso 3: Construir el Proyecto con Maven

El proyecto utiliza Maven para la gestión de dependencias.

En NetBeans, haga clic derecho sobre el proyecto en el panel de proyectos.

Seleccione Build with Dependencies.

Maven descargará todas las dependencias necesarias y compilará el proyecto.

- Paso 4: Ejecutar la Aplicación

Haga clic derecho sobre el proyecto en NetBeans.

Seleccione Run.

La aplicación se iniciará y mostrará la ventana principal.

#Uso de la Aplicación

- Seleccionar Carpeta de Archivos Multimedia

Antes de utilizar las funcionalidades, debe seleccionar la carpeta que contiene sus archivos multimedia:

En la ventana principal, haga clic en el botón "Seleccionar Carpeta".

Navegue hasta la carpeta deseada y selecciónela.

Confirme su selección.

- Navegar entre las Funcionalidades

Música: Reproduzca y gestione archivos de música.

Video: Reproduzca archivos de video.

Galería: Navegue y visualice imágenes.

Duplicados: Detecte y elimine archivos duplicados.

#Notas Importantes

- Compatibilidad: La aplicación fue desarrollada y probada en NetBeans. No se ha probado en otros entornos de desarrollo.

- Dependencias Externas: Asegúrese de que su sistema tiene acceso a internet durante la compilación para que Maven pueda descargar las dependencias necesarias.

- VLCJ y VLC Media Player:

VLCJ requiere que VLC Media Player esté instalado en su sistema.

Descargue e instale VLC Media Player.

Asegúrese de que las librerías nativas de VLC están en el PATH de su sistema.

#Estructura del Proyecto

```
administrador-archivos-multimedia/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── control/
│   │   │   ├── modelo/
│   │   │   └── vista/
│   │   └── resources/
├── pom.xml
└── README.md
```
