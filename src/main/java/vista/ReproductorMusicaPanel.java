package vista;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class ReproductorMusicaPanel extends JPanel {

    private PrincipalFrame principal;
    private JTable tablaMusica;
    private DefaultTableModel modeloTabla;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JSlider barraProgreso;
    private JButton btnPausa, btnSiguiente, btnAnterior;
    private Timer actualizacionProgreso;

    public ReproductorMusicaPanel(PrincipalFrame principal) {
        this.principal = principal;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nombre del Archivo");
        modeloTabla.addColumn("Ruta");

        tablaMusica = new JTable(modeloTabla);
        tablaMusica.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Agregar el listener para mostrar metadatos al seleccionar un archivo
        tablaMusica.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaMusica.getSelectedRow();
                if (selectedRow != -1) {
                    String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
                    File archivo = new File(rutaArchivo);
                    mostrarMetadatosMusica(archivo);  // Mostrar metadatos del archivo seleccionado
                    reproducirMusica(archivo);       // Reproducir el archivo seleccionado
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaMusica);

        JPanel panelControl = new JPanel();
        btnPausa = new JButton("Pausa/Reanudar");
        btnPausa.addActionListener(e -> pausarReanudar());

        btnSiguiente = new JButton("Siguiente");
        btnSiguiente.addActionListener(e -> reproducirSiguiente());

        btnAnterior = new JButton("Anterior");
        btnAnterior.addActionListener(e -> reproducirAnterior());

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

        panelControl.add(btnAnterior);
        panelControl.add(btnPausa);
        panelControl.add(btnSiguiente);
        panelControl.add(barraProgreso);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        add(scrollPane, BorderLayout.WEST);
        add(mediaPlayerComponent, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
    }

    public void cargarArchivos() {
        modeloTabla.setRowCount(0);

        File rutaSeleccionada = principal.getRutaSeleccionada();
        if (rutaSeleccionada != null && rutaSeleccionada.isDirectory()) {
            List<File> archivosMusica = buscarArchivosMusica(rutaSeleccionada);
            for (File archivo : archivosMusica) {
                modeloTabla.addRow(new Object[]{archivo.getName(), archivo.getAbsolutePath()});
            }
        }
    }

    private List<File> buscarArchivosMusica(File directorio) {
        List<File> archivosMusica = new ArrayList<>();
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    archivosMusica.addAll(buscarArchivosMusica(archivo));
                } else if (archivo.getName().endsWith(".mp3") || archivo.getName().endsWith(".wma")) {
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
        if (selectedRow != -1 && selectedRow < modeloTabla.getRowCount() - 1) {
            tablaMusica.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        }
    }

    private void reproducirAnterior() {
        int selectedRow = tablaMusica.getSelectedRow();
        if (selectedRow > 0) {
            tablaMusica.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
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
    try {
        if (archivo.getName().toLowerCase().endsWith(".mp3")) {
            // Usar jaudiotagger para archivos MP3
            AudioFile audioFile = AudioFileIO.read(archivo);
            Tag tag = audioFile.getTag();

            String artista = (tag != null && tag.getFirst(FieldKey.ARTIST) != null) ? tag.getFirst(FieldKey.ARTIST) : "Desconocido";
            String album = (tag != null && tag.getFirst(FieldKey.ALBUM) != null) ? tag.getFirst(FieldKey.ALBUM) : "Desconocido";
            String genero = (tag != null && tag.getFirst(FieldKey.GENRE) != null) ? tag.getFirst(FieldKey.GENRE) : "Desconocido";
            String duracion = audioFile.getAudioHeader().getTrackLength() + " segundos";
            String año = (tag != null && tag.getFirst(FieldKey.YEAR) != null) ? tag.getFirst(FieldKey.YEAR) : "Desconocido";

            // Mostrar los metadatos para MP3
            JOptionPane.showMessageDialog(this,
                    "Artista: " + artista + "\n" +
                    "Álbum: " + album + "\n" +
                    "Género: " + genero + "\n" +
                    "Duración: " + duracion + "\n" +
                    "Año: " + año,
                    "Metadatos de Música (MP3)",
                    JOptionPane.INFORMATION_MESSAGE);

        } else if (archivo.getName().toLowerCase().endsWith(".wma")) {
            // Usar Apache Tika para extraer metadatos de WMA
            Tika tika = new Tika();
            Metadata metadata = new Metadata();
            tika.parse(archivo, metadata);

            StringBuilder metadatos = new StringBuilder();
            for (String nombre : metadata.names()) {
                metadatos.append(nombre).append(": ").append(metadata.get(nombre)).append("\n");
            }

            // Mostrar todos los metadatos extraídos por Tika
            JOptionPane.showMessageDialog(this,
                    metadatos.toString(),
                    "Metadatos de Música (WMA)",
                    JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(this, "Formato de archivo no soportado para metadatos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "No se pueden extraer metadatos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
}
