package control;

import modelo.DetallesAudio;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuscadorArchivos {

    // Lista de extensiones multimedia
    private static final String[] EXTENSIONES_MUSICA = {"mp3", "wma"};
    private static final String[] EXTENSIONES_VIDEO = {"mp4", "flv"};
    private static final String[] EXTENSIONES_IMAGEN = {"jpg", "png", "gif"};

    // Método para buscar archivos multimedia en una carpeta de forma recursiva
    public List<File> buscarArchivosMultimedia(File directorio) {
        List<File> archivosMultimedia = new ArrayList<>();
        buscarRecursivo(directorio, archivosMultimedia);
        return archivosMultimedia;
    }

    // Método recursivo para explorar las subcarpetas y agregar archivos multimedia
    private void buscarRecursivo(File directorio, List<File> archivosMultimedia) {
        if (directorio.isDirectory()) {
            for (File archivo : directorio.listFiles()) {
                if (archivo.isDirectory()) {
                    buscarRecursivo(archivo, archivosMultimedia);
                } else if (esArchivoMultimedia(archivo)) {
                    archivosMultimedia.add(archivo);
                }
            }
        }
    }

    // Método auxiliar para verificar si un archivo es multimedia según su extensión
    private boolean esArchivoMultimedia(File archivo) {
        String extension = obtenerExtension(archivo.getName());
        for (String ext : EXTENSIONES_MUSICA) {
            if (extension.equalsIgnoreCase(ext)) return true;
        }
        for (String ext : EXTENSIONES_VIDEO) {
            if (extension.equalsIgnoreCase(ext)) return true;
        }
        for (String ext : EXTENSIONES_IMAGEN) {
            if (extension.equalsIgnoreCase(ext)) return true;
        }
        return false;
    }

    // Método para obtener la extensión de un archivo
    private String obtenerExtension(String nombreArchivo) {
        int i = nombreArchivo.lastIndexOf('.');
        return (i > 0) ? nombreArchivo.substring(i + 1) : "";
    }

    // Método para obtener detalles de audio usando MP3agic
    public DetallesAudio obtenerDetallesAudio(File archivo) {
    try {
        Mp3File mp3File = new Mp3File(archivo);
        if (mp3File.hasId3v2Tag()) {
            ID3v2 id3v2Tag = mp3File.getId3v2Tag();
            String nombre = archivo.getName();
            String artista = id3v2Tag.getArtist() != null ? id3v2Tag.getArtist() : "Desconocido";
            String album = id3v2Tag.getAlbum() != null ? id3v2Tag.getAlbum() : "Desconocido";
            String genero = id3v2Tag.getGenreDescription() != null ? id3v2Tag.getGenreDescription() : "Desconocido";
            int duracionSegundos = (int) mp3File.getLengthInSeconds();
            int anio;
            try {
                anio = Integer.parseInt(id3v2Tag.getYear());
            } catch (NumberFormatException e) {
                anio = 0; // Si el año no es válido o está ausente, usa 0 o cualquier valor predeterminado.
            }
            return new DetallesAudio(nombre, artista, album, genero, duracionSegundos, anio);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
}