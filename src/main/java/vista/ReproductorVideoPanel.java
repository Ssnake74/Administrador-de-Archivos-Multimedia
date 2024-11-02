package vista;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class ReproductorVideoPanel extends JPanel {

    private PrincipalFrame principal;
    private JTable tablaVideos;
    private DefaultTableModel modeloTabla;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JSlider barraProgreso;
    private JButton btnPausa, btnSiguiente, btnAnterior;
    private Timer actualizacionProgreso;
    private JLabel espacioTotalLabel;

    public ReproductorVideoPanel(PrincipalFrame principal) {
        this.principal = principal;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nombre del Archivo");
        modeloTabla.addColumn("Ruta");
        modeloTabla.addColumn("Tamaño (MB)");

        tablaVideos = new JTable(modeloTabla);
        tablaVideos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tablaVideos.setAutoCreateRowSorter(true);

        tablaVideos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaVideos.getSelectedRow();
                if (selectedRow != -1) {
                    String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
                    File archivo = new File(rutaArchivo);
                    reproducirVideo(archivo);
                    mostrarMetadatosVideo(archivo);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaVideos);

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

        espacioTotalLabel = new JLabel("Espacio total ocupado: Calculando...");
        panelControl.add(espacioTotalLabel);

        add(scrollPane, BorderLayout.WEST);
        add(mediaPlayerComponent, BorderLayout.CENTER);
        add(panelControl, BorderLayout.SOUTH);
    }

    public void cargarArchivos() {
        modeloTabla.setRowCount(0);
        long espacioTotal = 0;

        File rutaSeleccionada = principal.getRutaSeleccionada();
        if (rutaSeleccionada != null && rutaSeleccionada.isDirectory()) {
            List<File> archivosVideo = buscarArchivosVideo(rutaSeleccionada);
            for (File archivo : archivosVideo) {
                long tamañoMB = archivo.length() / (1024 * 1024); 
                espacioTotal += tamañoMB;
                modeloTabla.addRow(new Object[]{archivo.getName(), archivo.getAbsolutePath(), tamañoMB});
            }
            espacioTotalLabel.setText("Espacio total ocupado: " + espacioTotal + " MB");
        }
    }

    private List<File> buscarArchivosVideo(File directorio) {
        List<File> archivosVideo = new ArrayList<>();
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    archivosVideo.addAll(buscarArchivosVideo(archivo));
                } else if (archivo.getName().endsWith(".mp4") || archivo.getName().endsWith(".flv")) {
                    archivosVideo.add(archivo);
                }
            }
        }
        return archivosVideo;
    }

    private void reproducirVideo(File archivo) {
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
        int selectedRow = tablaVideos.getSelectedRow();
        if (selectedRow != -1 && selectedRow < modeloTabla.getRowCount() - 1) {
            tablaVideos.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        }
    }

    private void reproducirAnterior() {
        int selectedRow = tablaVideos.getSelectedRow();
        if (selectedRow > 0) {
            tablaVideos.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
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

    private void mostrarMetadatosVideo(File archivo) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(archivo);

            StringBuilder metadatos = new StringBuilder();

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    metadatos.append(tag.getTagName()).append(": ").append(tag.getDescription()).append("\n");
                }
            }

            JTextArea textArea = new JTextArea(metadatos.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "Metadatos de Video", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pueden extraer metadatos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}