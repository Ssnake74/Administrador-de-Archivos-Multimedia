package control;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BuscadorArchivos {

    private static final String[] EXTENSIONES_MUSICA = {"mp3", "wma"};
    private static final String[] EXTENSIONES_VIDEO = {"mp4", "flv"};
    private static final String[] EXTENSIONES_IMAGEN = {"jpg", "png", "gif"};

    public List<File> buscarArchivosMultimedia(File directorio) {
        List<File> archivosMultimedia = new ArrayList<>();
        buscarRecursivo(directorio, archivosMultimedia);
        return archivosMultimedia;
    }

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

    private String obtenerExtension(String nombreArchivo) {
        int i = nombreArchivo.lastIndexOf('.');
        return (i > 0) ? nombreArchivo.substring(i + 1) : "";
    }
}