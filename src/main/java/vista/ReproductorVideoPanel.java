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
    private JButton btnMostrarMetadatos;
    private JTextArea metadataTextArea;
    private JScrollPane metadataScrollPane;
    private JLayeredPane videoLayeredPane;

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
                    int modelRow = tablaVideos.convertRowIndexToModel(selectedRow);
                    String rutaArchivo = (String) modeloTabla.getValueAt(modelRow, 1);
                    File archivo = new File(rutaArchivo);
                    reproducirVideo(archivo);
                    cargarMetadatosVideo(archivo);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaVideos);

        JPanel panelControl = new JPanel();
        btnPausa = new JButton("Pausa");
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

        espacioTotalLabel = new JLabel("Espacio total ocupado: Calculando...");
        panelControl.add(espacioTotalLabel);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        btnMostrarMetadatos = new JButton("!");
        btnMostrarMetadatos.setMargin(new Insets(2, 2, 2, 2));
        btnMostrarMetadatos.addActionListener(e -> toggleMetadataDisplay());

        metadataTextArea = new JTextArea();
        metadataTextArea.setEditable(false);
        metadataTextArea.setOpaque(false);
        metadataTextArea.setForeground(Color.WHITE);

        metadataScrollPane = new JScrollPane(metadataTextArea);
        metadataScrollPane.setOpaque(false);
        metadataScrollPane.getViewport().setOpaque(false);
        metadataScrollPane.setVisible(false);

        videoLayeredPane = new JLayeredPane();
        videoLayeredPane.setPreferredSize(new Dimension(600, 400));

        mediaPlayerComponent.setBounds(0, 0, 600, 400);
        videoLayeredPane.add(mediaPlayerComponent, Integer.valueOf(1));

        btnMostrarMetadatos.setBounds(10, 10, 30, 30);
        videoLayeredPane.add(btnMostrarMetadatos, Integer.valueOf(3));

        metadataScrollPane.setBounds(50, 50, 500, 300);
        videoLayeredPane.add(metadataScrollPane, Integer.valueOf(2));
        add(scrollPane, BorderLayout.WEST);
        add(videoLayeredPane, BorderLayout.CENTER);
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
                } else if (archivo.getName().toLowerCase().endsWith(".mp4") || archivo.getName().toLowerCase().endsWith(".flv")) {
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
        if (selectedRow != -1 && selectedRow < tablaVideos.getRowCount() - 1) {
            int nextRow = selectedRow + 1;
            int modelRow = tablaVideos.convertRowIndexToModel(nextRow);
            tablaVideos.setRowSelectionInterval(nextRow, nextRow);
            String rutaArchivo = (String) modeloTabla.getValueAt(modelRow, 1);
            File archivo = new File(rutaArchivo);
            cargarMetadatosVideo(archivo);
            reproducirVideo(archivo);
        }
    }

    private void reproducirAnterior() {
        int selectedRow = tablaVideos.getSelectedRow();
        if (selectedRow > 0) {
            int prevRow = selectedRow - 1;
            int modelRow = tablaVideos.convertRowIndexToModel(prevRow);
            tablaVideos.setRowSelectionInterval(prevRow, prevRow);
            String rutaArchivo = (String) modeloTabla.getValueAt(modelRow, 1);
            File archivo = new File(rutaArchivo);
            cargarMetadatosVideo(archivo);
            reproducirVideo(archivo);
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

    private void cargarMetadatosVideo(File archivo) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(archivo);

            StringBuilder metadatos = new StringBuilder();

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    metadatos.append(tag.getTagName()).append(": ").append(tag.getDescription()).append("\n");
                }
            }

            metadataTextArea.setText(metadatos.toString());

        } catch (Exception e) {
            metadataTextArea.setText("No se pueden extraer metadatos: " + e.getMessage());
        }
    }

    private void toggleMetadataDisplay() {
        boolean isVisible = metadataScrollPane.isVisible();
        metadataScrollPane.setVisible(!isVisible);
    }
}