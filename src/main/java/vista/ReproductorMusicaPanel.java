package vista;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableRowSorter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import modelo.Playlist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.FileReader;
import java.io.IOException;

public class ReproductorMusicaPanel extends JPanel {

    private PrincipalFrame principal;
    private JTable tablaMusica;
    private DefaultTableModel modeloTabla;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JSlider barraProgreso;
    private JButton btnPausa, btnSiguiente, btnAnterior;
    private Timer actualizacionProgreso;
    private JPanel panelControl;
    private JLabel espacioTotalLabel;
    private List<Playlist> playlists = new ArrayList<>();
    private JTextArea metadataTextArea;
    private JComboBox<String> playlistComboBox;
    private List<File> archivosMusica = new ArrayList<>();
    private List<File> archivosMostrados = new ArrayList<>();

    public ReproductorMusicaPanel(PrincipalFrame principal) {
        this.principal = principal;
        setLayout(new BorderLayout());
        initUI();
        cargarPlaylists();
        actualizarPlaylistComboBox();
    }

    private void initUI() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nombre del Archivo");
        modeloTabla.addColumn("Ruta");
        modeloTabla.addColumn("Tamaño (MB)");

        tablaMusica = new JTable(modeloTabla);
        tablaMusica.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaMusica.setRowSorter(sorter);

        espacioTotalLabel = new JLabel("Espacio total ocupado por archivos de música: 0 MB");

        tablaMusica.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaMusica.getSelectedRow();
                if (selectedRow != -1) {
                    String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
                    File archivo = new File(rutaArchivo);
                    mostrarMetadatosMusica(archivo);
                    reproducirMusica(archivo);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaMusica);

        panelControl = new JPanel(new BorderLayout());

        btnPausa = new JButton("Pausa");
        btnPausa.addActionListener(e -> pausarReanudar());

        btnSiguiente = new JButton("Siguiente");
        btnSiguiente.addActionListener(e -> reproducirSiguiente());

        btnAnterior = new JButton("Anterior");
        btnAnterior.addActionListener(e -> reproducirAnterior());

        JButton btnGestionPlaylists = new JButton("Gestionar Playlists");
        btnGestionPlaylists.addActionListener(e -> abrirPlaylistPanel());

        barraProgreso = new JSlider();
        barraProgreso.setValue(0);
        barraProgreso.setEnabled(false);
        barraProgreso.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (mediaPlayerComponent.mediaPlayer() != null && barraProgreso.getValueIsAdjusting()) {
                    mediaPlayerComponent.mediaPlayer().controls().setTime(barraProgreso.getValue() * 1000);
                }
            }
        });

        panelControl.add(barraProgreso, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.add(btnAnterior);
        controlsPanel.add(btnPausa);
        controlsPanel.add(btnSiguiente);
        controlsPanel.add(btnGestionPlaylists);

        panelControl.add(controlsPanel, BorderLayout.EAST);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        metadataTextArea = new JTextArea();
        metadataTextArea.setEditable(false);
        JScrollPane metadataScrollPane = new JScrollPane(metadataTextArea);
        metadataScrollPane.setPreferredSize(new Dimension(300, 150));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(metadataScrollPane, BorderLayout.NORTH);
        centerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);

        playlistComboBox = new JComboBox<>();
        playlistComboBox.addItem("Todas las canciones");
        playlistComboBox.addActionListener(e -> actualizarListaCanciones());

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSuperior.add(espacioTotalLabel);
        panelSuperior.add(new JLabel(" | Seleccionar Playlist: "));
        panelSuperior.add(playlistComboBox);

        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
    }

    public void cargarArchivos() {
        modeloTabla.setRowCount(0);
        archivosMusica.clear();

        File rutaSeleccionada = principal.getRutaSeleccionada();
        if (rutaSeleccionada != null && rutaSeleccionada.isDirectory()) {
            archivosMusica = buscarArchivosMusica(rutaSeleccionada);
            archivosMostrados = new ArrayList<>(archivosMusica);
            mostrarArchivosEnTabla(archivosMostrados);
        }
    }

    private void mostrarArchivosEnTabla(List<File> archivos) {
        modeloTabla.setRowCount(0);
        long espacioTotal = 0;

        for (File archivo : archivos) {
            long tamañoEnMB = archivo.length() / (1024 * 1024);
            espacioTotal += archivo.length();
            modeloTabla.addRow(new Object[]{archivo.getName(), archivo.getAbsolutePath(), tamañoEnMB});
        }
        espacioTotalLabel.setText("Espacio total ocupado por archivos de música: " + espacioTotal / (1024 * 1024) + " MB");
    }

    private List<File> buscarArchivosMusica(File directorio) {
        List<File> archivosMusica = new ArrayList<>();
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    archivosMusica.addAll(buscarArchivosMusica(archivo));
                } else if (archivo.getName().toLowerCase().endsWith(".mp3") || archivo.getName().toLowerCase().endsWith(".wma")) {
                    archivosMusica.add(archivo);
                }
            }
        }
        return archivosMusica;
    }

    private void reproducirMusica(File archivo) {
        detenerReproduccion();
        mediaPlayerComponent.mediaPlayer().media().play(archivo.getAbsolutePath());
        actualizarBarraProgreso();
    }

    private void actualizarBarraProgreso() {
        if (mediaPlayerComponent.mediaPlayer() != null) {
            barraProgreso.setEnabled(true);
            actualizacionProgreso = new Timer(1000, e -> {
                long time = mediaPlayerComponent.mediaPlayer().status().time() / 1000;
                barraProgreso.setValue((int) time);
                barraProgreso.setMaximum((int) (mediaPlayerComponent.mediaPlayer().media().info().duration() / 1000));
            });
            actualizacionProgreso.start();
        }
    }

    private void pausarReanudar() {
        if (mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
            mediaPlayerComponent.mediaPlayer().controls().pause();
            btnPausa.setText("Reanudar");
        } else {
            mediaPlayerComponent.mediaPlayer().controls().play();
            btnPausa.setText("Pausa");
        }
    }

    private void reproducirSiguiente() {
    int selectedRow = tablaMusica.getSelectedRow();
    if (selectedRow != -1 && selectedRow < tablaMusica.getRowCount() - 1) {
        int nextRow = selectedRow + 1;
        int modelRow = tablaMusica.convertRowIndexToModel(nextRow);
        String rutaArchivo = (String) modeloTabla.getValueAt(modelRow, 1);
        File archivo = new File(rutaArchivo);
        mostrarMetadatosMusica(archivo);
        reproducirMusica(archivo);
        tablaMusica.setRowSelectionInterval(nextRow, nextRow);
    }
}

private void reproducirAnterior() {
    int selectedRow = tablaMusica.getSelectedRow();
    if (selectedRow > 0) {
        int prevRow = selectedRow - 1;
        int modelRow = tablaMusica.convertRowIndexToModel(prevRow);
        String rutaArchivo = (String) modeloTabla.getValueAt(modelRow, 1);
        File archivo = new File(rutaArchivo);
        mostrarMetadatosMusica(archivo);
        reproducirMusica(archivo);
        tablaMusica.setRowSelectionInterval(prevRow, prevRow);
    }
}

    private void detenerReproduccion() {
        if (mediaPlayerComponent.mediaPlayer() != null) {
            mediaPlayerComponent.mediaPlayer().controls().stop();
            barraProgreso.setEnabled(false);
            if (actualizacionProgreso != null) {
                actualizacionProgreso.stop();
            }
        }
    }

    private void mostrarMetadatosMusica(File archivo) {
        StringBuilder metadatos = new StringBuilder();
        try {
            if (archivo.getName().toLowerCase().endsWith(".mp3")) {
                AudioFile audioFile = AudioFileIO.read(archivo);
                Tag tag = audioFile.getTag();

                String artista = (tag != null && tag.getFirst(FieldKey.ARTIST) != null) ? tag.getFirst(FieldKey.ARTIST) : "Desconocido";
                String album = (tag != null && tag.getFirst(FieldKey.ALBUM) != null) ? tag.getFirst(FieldKey.ALBUM) : "Desconocido";
                String genero = (tag != null && tag.getFirst(FieldKey.GENRE) != null) ? tag.getFirst(FieldKey.GENRE) : "Desconocido";
                String duracion = audioFile.getAudioHeader().getTrackLength() + " segundos";
                String año = (tag != null && tag.getFirst(FieldKey.YEAR) != null) ? tag.getFirst(FieldKey.YEAR) : "Desconocido";

                metadatos.append("Artista: ").append(artista).append("\n")
                        .append("Álbum: ").append(album).append("\n")
                        .append("Género: ").append(genero).append("\n")
                        .append("Duración: ").append(duracion).append("\n")
                        .append("Año: ").append(año);

            } else if (archivo.getName().toLowerCase().endsWith(".wma")) {
                Tika tika = new Tika();
                Metadata metadata = new Metadata();
                tika.parse(archivo, metadata);

                for (String nombre : metadata.names()) {
                    metadatos.append(nombre).append(": ").append(metadata.get(nombre)).append("\n");
                }
            } else {
                metadatos.append("Formato de archivo no soportado para metadatos");
            }
        } catch (Exception e) {
            metadatos.append("No se pueden extraer metadatos: ").append(e.getMessage());
        }
        metadataTextArea.setText(metadatos.toString());
    }

    private void abrirPlaylistPanel() {
        JDialog playlistDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Gestión de Playlists", Dialog.ModalityType.APPLICATION_MODAL);
        playlistDialog.setSize(500, 400);
        playlistDialog.setLocationRelativeTo(this);

        PlaylistPanel playlistPanel = new PlaylistPanel(principal);
        playlistDialog.add(playlistPanel);

        playlistDialog.setVisible(true);
        cargarPlaylists();
        actualizarPlaylistComboBox();
    }

    private void cargarPlaylists() {
        playlists.clear();
        try (FileReader reader = new FileReader("playlists.json")) {
            Gson gson = new Gson();
            Type playlistListType = new TypeToken<List<Playlist>>() {}.getType();
            playlists = gson.fromJson(reader, playlistListType);
        } catch (IOException e) {
            System.out.println("No se pudieron cargar las playlists: " + e.getMessage());
        }

        if (playlists == null) {
            playlists = new ArrayList<>();
        }
    }

    private void actualizarPlaylistComboBox() {
        playlistComboBox.removeAllItems();
        playlistComboBox.addItem("Todas las canciones");
        for (Playlist playlist : playlists) {
            playlistComboBox.addItem(playlist.getNombre());
        }
    }

    private void actualizarListaCanciones() {
        String seleccion = (String) playlistComboBox.getSelectedItem();
        if (seleccion != null) {
            if (seleccion.equals("Todas las canciones")) {
                archivosMostrados = new ArrayList<>(archivosMusica);
                mostrarArchivosEnTabla(archivosMostrados);
            } else {
                Playlist playlistSeleccionada = null;
                for (Playlist p : playlists) {
                    if (p.getNombre().equals(seleccion)) {
                        playlistSeleccionada = p;
                        break;
                    }
                }
                if (playlistSeleccionada != null) {
                    archivosMostrados = new ArrayList<>();
                    for (String ruta : playlistSeleccionada.getCanciones()) {
                        File archivo = new File(ruta);
                        if (archivo.exists()) {
                            archivosMostrados.add(archivo);
                        }
                    }
                    mostrarArchivosEnTabla(archivosMostrados);
                }
            }
        }
    }

    private long calcularEspacioTotalMusica() {
        long totalSize = 0;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            File file = new File((String) modeloTabla.getValueAt(i, 1));
            totalSize += file.length();
        }
        return totalSize;
    }
}