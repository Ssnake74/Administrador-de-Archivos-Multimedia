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
    private JButton btnEliminarDuplicados;  // Referencia al botón "Eliminar Seleccionados"

    public PrincipalFrame() {
        setTitle("Administrador de Archivos Multimedia");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuración de CardLayout para cambiar entre paneles
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Inicializar paneles de cada funcionalidad
        reproductorMusicaPanel = new ReproductorMusicaPanel(this);
        archivoDuplicadoDetector = new ArchivoDuplicadoDetector(this);
        mainPanel.add(archivoDuplicadoDetector, "duplicados");
        reproductorVideoPanel = new ReproductorVideoPanel(this);
        imagenesGaleria = new ImagenesGaleria(this);

        // Añadir paneles de música, video y galería
        mainPanel.add(new JPanel(), "empty"); // Panel vacío inicial
        mainPanel.add(reproductorMusicaPanel, "musica");
        mainPanel.add(reproductorVideoPanel, "video");
        mainPanel.add(imagenesGaleria, "galeria");

        // Crear menú de botones y añadir al frame
        JPanel menuPanel = crearMenuPanel();
        add(menuPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        // Inicializar el botón "Eliminar Seleccionados" y ocultarlo por defecto
        btnEliminarDuplicados = archivoDuplicadoDetector.getBtnEliminarDuplicados();
        btnEliminarDuplicados.setVisible(false);  // Oculto al inicio
        add(btnEliminarDuplicados, BorderLayout.SOUTH);  // Añadirlo al frame en la posición sur

        setVisible(true);
    }

    private JPanel crearMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());

        // Título
        titulo = new JLabel("Reproductor de Multimedia", JLabel.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        menuPanel.add(titulo, BorderLayout.NORTH);

        // Botones
        JPanel botonPanel = new JPanel(new GridLayout(1, 4, 20, 0));  // Cambiar a 4 columnas
        botonPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Botón Música
        JButton btnMusica = new JButton("Música");
        btnMusica.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                reproductorMusicaPanel.cargarArchivos();
                cardLayout.show(mainPanel, "musica");
                btnEliminarDuplicados.setVisible(false);  // Ocultar el botón de eliminación
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        // Botón Video
        JButton btnVideo = new JButton("Video");
        btnVideo.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                reproductorVideoPanel.cargarArchivos();
                cardLayout.show(mainPanel, "video");
                btnEliminarDuplicados.setVisible(false);  // Ocultar el botón de eliminación
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        // Botón Galería
        JButton btnGaleria = new JButton("Galería");
        btnGaleria.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                imagenesGaleria.cargarArchivos();
                cardLayout.show(mainPanel, "galeria");
                btnEliminarDuplicados.setVisible(false);  // Ocultar el botón de eliminación
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        // Crear el botón para acceder al panel de duplicados
        JButton btnDuplicados = new JButton("Duplicados");
        btnDuplicados.addActionListener(e -> {
            if (rutaSeleccionada != null) {
                archivoDuplicadoDetector.detectarDuplicados();  // Llama al método de detección de duplicados
                cardLayout.show(mainPanel, "duplicados");
                btnEliminarDuplicados.setVisible(true);  // Mostrar el botón de eliminación
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            }
        });

        // Añadir botones al panel de botones
        botonPanel.add(btnMusica);
        botonPanel.add(btnVideo);
        botonPanel.add(btnGaleria);
        botonPanel.add(btnDuplicados);  // Añadir el botón de duplicados al panel

        // Botón para seleccionar carpeta
        JButton btnSeleccionarCarpeta = new JButton("Seleccionar Carpeta");
        btnSeleccionarCarpeta.addActionListener((ActionEvent e) -> seleccionarCarpeta());
        menuPanel.add(btnSeleccionarCarpeta, BorderLayout.SOUTH);

        // Añadir panel de botones al menú principal
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