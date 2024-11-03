package modelo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String nombre;
    private List<String> canciones;

    public Playlist(String nombre) {
        this.nombre = nombre;
        this.canciones = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public List<String> getCanciones() {
        return canciones;
    }

    public void agregarCancion(String rutaArchivo) {
        if (!canciones.contains(rutaArchivo)) {
            canciones.add(rutaArchivo);
        }
    }

    public void eliminarCancion(String rutaArchivo) {
        canciones.remove(rutaArchivo);
    }

    public void guardarPlaylist(String rutaArchivo) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(rutaArchivo)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Playlist cargarPlaylist(String rutaArchivo) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(rutaArchivo)) {
            return gson.fromJson(reader, Playlist.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Playlist> cargarTodasLasPlaylists(String carpetaPlaylists) {
        List<Playlist> playlists = new ArrayList<>();
        File carpeta = new File(carpetaPlaylists);
        if (carpeta.exists() && carpeta.isDirectory()) {
            File[] archivos = carpeta.listFiles((dir, name) -> name.endsWith(".json"));
            if (archivos != null) {
                for (File archivo : archivos) {
                    Playlist playlist = cargarPlaylist(archivo.getAbsolutePath());
                    if (playlist != null) {
                        playlists.add(playlist);
                    }
                }
            }
        }
        return playlists;
    }

    public static void guardarTodasLasPlaylists(List<Playlist> playlists, String carpetaPlaylists) {
        File carpeta = new File(carpetaPlaylists);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        for (Playlist playlist : playlists) {
            String rutaArchivo = carpetaPlaylists + File.separator + playlist.getNombre() + ".json";
            playlist.guardarPlaylist(rutaArchivo);
        }
    }
}