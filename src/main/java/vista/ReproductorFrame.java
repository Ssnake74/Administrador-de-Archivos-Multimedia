package vista;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ReproductorFrame extends JFrame {

    private final File archivoActual;
    private final PrincipalFrame explorador;
    private MediaPlayer mediaPlayer;
    private JFXPanel videoPanel;
    private JSlider barraProgreso;
    private JButton btnPausa, btnSiguiente, btnAnterior;
    private Timer actualizacionProgreso;

    public ReproductorFrame(File archivo, PrincipalFrame explorador) {
        this.archivoActual = archivo;
        this.explorador = explorador;
        setTitle("Reproductor Multimedia");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        reproducirArchivo();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Panel de control
        JPanel panelControl = new JPanel();
        btnPausa = new JButton("Pausa");
        btnSiguiente = new JButton("Siguiente");
        btnAnterior = new JButton("Anterior");
        barraProgreso = new JSlider();
        barraProgreso.setValue(0);

        panelControl.add(btnAnterior);
        panelControl.add(btnPausa);
        panelControl.add(btnSiguiente);
        panelControl.add(barraProgreso);

        // Listeners para los botones
        btnPausa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pausarReanudar();
            }
        });

        btnSiguiente.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                irAlSiguienteArchivo();
            }
        });

        btnAnterior.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                irAlAnteriorArchivo();
            }
        });

        // Configuración de la barra de progreso para que sea interactiva
        barraProgreso.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (mediaPlayer != null && barraProgreso.getValueIsAdjusting()) {
                    mediaPlayer.seek(Duration.millis(barraProgreso.getValue()));
                }
            }
        });

        videoPanel = new JFXPanel();
        videoPanel.setPreferredSize(new Dimension(800, 450));

        panel.add(videoPanel, BorderLayout.CENTER);
        panel.add(panelControl, BorderLayout.SOUTH);

        add(panel);
    }

    private void reproducirArchivo() {
        if (archivoActual.getName().endsWith(".mp4") || archivoActual.getName().endsWith(".m4v")) {
            javafx.application.Platform.runLater(() -> {
                Media media = new Media(archivoActual.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);
                mediaView.setFitWidth(videoPanel.getWidth());
                mediaView.setFitHeight(videoPanel.getHeight());

                javafx.scene.Scene escena = new javafx.scene.Scene(new javafx.scene.Group(mediaView));
                videoPanel.setScene(escena);

                mediaPlayer.play();
                actualizarBarraProgreso();
            });
        } else if (archivoActual.getName().endsWith(".mp3") || archivoActual.getName().endsWith(".wav")) {
            // Implementación de audio
            // Podrías usar `JLayer` para mp3, aunque aquí se asume que MediaPlayer está manejando audio y video.
            javafx.application.Platform.runLater(() -> {
                Media media = new Media(archivoActual.toURI().toString());
                mediaPlayer = new MediaPlayer(media);

                mediaPlayer.play();
                actualizarBarraProgreso();
            });
        } else {
            JOptionPane.showMessageDialog(this, "Formato de archivo no compatible");
        }
    }

    private void actualizarBarraProgreso() {
        mediaPlayer.setOnReady(() -> barraProgreso.setMaximum((int) mediaPlayer.getTotalDuration().toMillis()));

        actualizacionProgreso = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mediaPlayer != null) {
                    barraProgreso.setValue((int) mediaPlayer.getCurrentTime().toMillis());
                }
            }
        });
        actualizacionProgreso.start();

        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            barraProgreso.setValue((int) newTime.toMillis());
        });
    }

    private void pausarReanudar() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                btnPausa.setText("Reanudar");
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                mediaPlayer.play();
                btnPausa.setText("Pausa");
            }
        }
    }

    private void irAlSiguienteArchivo() {
        File siguienteArchivo = explorador.obtenerSiguienteArchivo(archivoActual);
        if (siguienteArchivo != null) {
            detenerReproduccion();
            new ReproductorFrame(siguienteArchivo, explorador).setVisible(true);
            dispose();
        }
    }

    private void irAlAnteriorArchivo() {
        File anteriorArchivo = explorador.obtenerAnteriorArchivo(archivoActual);
        if (anteriorArchivo != null) {
            detenerReproduccion();
            new ReproductorFrame(anteriorArchivo, explorador).setVisible(true);
            dispose();
        }
    }

    private void detenerReproduccion() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            actualizacionProgreso.stop();
        }
    }
}