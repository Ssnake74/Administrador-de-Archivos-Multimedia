package vista;

import control.ArchivoDuplicadoDetector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class PrincipalFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ReproductorMusicaPanel reproductorMusicaPanel;
    private ReproductorVideoPanel reproductorVideoPanel;
    private ImagenesGaleria imagenesGaleria;
    private ArchivoDuplicadoDetector archivoDuplicadoDetector;
    private JLabel titulo;
    private File rutaSeleccionada;
    private JButton btnEliminarDuplicados;   

    public PrincipalFrame() {
        setTitle("Administrador de Archivos Multimedia");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        reproductorMusicaPanel = new ReproductorMusicaPanel(this);
        archivoDuplicadoDetector = new ArchivoDuplicadoDetector(this);
        mainPanel.add(archivoDuplicadoDetector, "duplicados");
        reproductorVideoPanel = new ReproductorVideoPanel(this);
        imagenesGaleria = new ImagenesGaleria(this);

        mainPanel.add(new JPanel(), "empty");
        mainPanel.add(reproductorMusicaPanel, "musica");
        mainPanel.add(reproductorVideoPanel, "video");
        mainPanel.add(imagenesGaleria, "galeria");

        JPanel menuPanel = crearMenuPanel();
        add(menuPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        btnEliminarDuplicados = archivoDuplicadoDetector.getBtnEliminarDuplicados();
        btnEliminarDuplicados.setVisible(false);  
        add(btnEliminarDuplicados, BorderLayout.SOUTH);  

        setVisible(true);
    }

    private JPanel crearMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());

        titulo = new JLabel("Reproductor de Multimedia", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        menuPanel.add(titulo, BorderLayout.NORTH);

        JPanel botonPanel = new JPanel(new GridLayout(1, 4, 20, 0));  
        botonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton btnMusica = new JButton("Música");
        btnMusica.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                reproductorMusicaPanel.cargarArchivos();
                cardLayout.show(mainPanel, "musica");
                btnEliminarDuplicados.setVisible(false);  
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        JButton btnVideo = new JButton("Video");
        btnVideo.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                reproductorVideoPanel.cargarArchivos();
                cardLayout.show(mainPanel, "video");
                btnEliminarDuplicados.setVisible(false);  
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        JButton btnGaleria = new JButton("Galería");
        btnGaleria.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                imagenesGaleria.cargarArchivos();
                cardLayout.show(mainPanel, "galeria");
                btnEliminarDuplicados.setVisible(false); 
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        JButton btnDuplicados = new JButton("Duplicados");
        btnDuplicados.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                archivoDuplicadoDetector.detectarDuplicados();  
                cardLayout.show(mainPanel, "duplicados");
                btnEliminarDuplicados.setVisible(true);  
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        botonPanel.add(btnMusica);
        botonPanel.add(btnVideo);
        botonPanel.add(btnGaleria);
        botonPanel.add(btnDuplicados);  

        JButton btnSeleccionarCarpeta = new JButton("Seleccionar Carpeta");
        btnSeleccionarCarpeta.addActionListener((ActionEvent e) -> seleccionarCarpeta());
        menuPanel.add(btnSeleccionarCarpeta, BorderLayout.SOUTH);

        menuPanel.add(botonPanel, BorderLayout.CENTER);

        return menuPanel;
    }

    private void seleccionarCarpeta() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            rutaSeleccionada = fileChooser.getSelectedFile();
            JOptionPane.showMessageDialog(this, "Carpeta seleccionada: " + rutaSeleccionada.getAbsolutePath());
        }
    }

    public File getRutaSeleccionada() {
        return rutaSeleccionada;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrincipalFrame().setVisible(true));
    }
}