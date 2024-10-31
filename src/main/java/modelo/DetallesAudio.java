package modelo;

public class DetallesAudio {
    private String nombre;
    private String artista;
    private String album;
    private String genero;
    private int duracionSegundos;
    private int anio;

    // Constructor y getters/setters
    public DetallesAudio(String nombre, String artista, String album, String genero, int duracionSegundos, int anio) {
        this.nombre = nombre;
        this.artista = artista;
        this.album = album;
        this.genero = genero;
        this.duracionSegundos = duracionSegundos;
        this.anio = anio;
    }

    public String getNombre() { return nombre; }
    public String getArtista() { return artista; }
    public String getAlbum() { return album; }
    public String getGenero() { return genero; }
    public int getDuracionSegundos() { return duracionSegundos; }
    public int getAnio() { return anio; }
}
