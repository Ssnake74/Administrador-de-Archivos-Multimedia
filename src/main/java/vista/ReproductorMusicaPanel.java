package vista;

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

        tablaMusica.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaMusica.getSelectedRow();
                if (selectedRow != -1) {
                    String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
                    File archivo = new File(rutaArchivo);
                    reproducirMusica(archivo);
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
}