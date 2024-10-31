// PrincipalFrame.java

package vista;

import control.BuscadorArchivos;
import modelo.DetallesAudio;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class PrincipalFrame extends JFrame {

    private JButton btnSeleccionarCarpeta;
    private JButton btnAbrirReproductor;
    private JLabel lblRuta;
    private JTable tablaArchivos;
    private DefaultTableModel modeloTabla;
    private JLabel vistaPreviaImagen;

    public PrincipalFrame() {
        setTitle("Administrador de Archivos Multimedia");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        btnSeleccionarCarpeta = new JButton("Seleccionar Carpeta de Búsqueda");
        btnAbrirReproductor = new JButton("Abrir Reproductor");

        lblRuta = new JLabel("Ruta seleccionada: Ninguna");

        // Action listener para seleccionar la carpeta
        btnSeleccionarCarpeta.addActionListener(e -> seleccionarCarpeta());

        // Action listener para abrir el reproductor con el archivo seleccionado
        btnAbrirReproductor.addActionListener(e -> abrirReproductor());

        // Configuración de la tabla y otros elementos de la interfaz
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nombre del Archivo");
        modeloTabla.addColumn("Ruta");

        tablaArchivos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaArchivos);

        vistaPreviaImagen = new JLabel();
        vistaPreviaImagen.setHorizontalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setVerticalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setPreferredSize(new Dimension(200, 200));

        JPanel panelVistaPrevia = new JPanel(new BorderLayout());
        panelVistaPrevia.add(new JLabel("Vista Previa:"), BorderLayout.NORTH);
        panelVistaPrevia.add(vistaPreviaImagen, BorderLayout.CENTER);

        tablaArchivos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                mostrarVistaPrevia();
            }
        });

        // Panel de botones en la parte superior
        JPanel panelBotones = new JPanel();
        panelBotones.add(btnSeleccionarCarpeta);
        panelBotones.add(btnAbrirReproductor);

        panel.add(panelBotones, BorderLayout.NORTH);
        panel.add(lblRuta, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.WEST);
        panel.add(panelVistaPrevia, BorderLayout.EAST);

        add(panel);
    }

    private void seleccionarCarpeta() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            lblRuta.setText("Ruta seleccionada: " + selectedDirectory.getAbsolutePath());

            modeloTabla.setRowCount(0);

            BuscadorArchivos buscador = new BuscadorArchivos();
            List<File> archivosEncontrados = buscador.buscarArchivosMultimedia(selectedDirectory);

            for (File archivo : archivosEncontrados) {
                String nombreArchivo = archivo.getName().toLowerCase();
                if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".png") || nombreArchivo.endsWith(".gif") ||
                    nombreArchivo.endsWith(".mp3") || nombreArchivo.endsWith(".mp4")) {
                    modeloTabla.addRow(new Object[]{archivo.getName(), archivo.getAbsolutePath()});
                }
            }
        }
    }

    private void mostrarVistaPrevia() {
        int selectedRow = tablaArchivos.getSelectedRow();
        if (selectedRow != -1) {
            String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
            File archivo = new File(rutaArchivo);
            String nombreArchivo = archivo.getName().toLowerCase();
            if (nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".png") || nombreArchivo.endsWith(".gif")) {
                ImageIcon icono = new ImageIcon(new ImageIcon(rutaArchivo).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
                vistaPreviaImagen.setIcon(icono);
            } else {
                vistaPreviaImagen.setIcon(null);
            }
        }
    }

    private void abrirReproductor() {
        int selectedRow = tablaArchivos.getSelectedRow();
        if (selectedRow != -1) {
            String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
            File archivo = new File(rutaArchivo);
            String nombreArchivo = archivo.getName().toLowerCase();

            if (nombreArchivo.endsWith(".mp3") || nombreArchivo.endsWith(".mp4")) {
                new ReproductorFrame(archivo, this).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Seleccione un archivo de audio o video (MP3 o MP4).");
            }
        }
    }

    // Métodos en PrincipalFrame para obtener el siguiente y anterior archivo
    public File obtenerSiguienteArchivo(File archivoActual) {
        int currentRow = getRowByFile(archivoActual);
        if (currentRow != -1 && currentRow < modeloTabla.getRowCount() - 1) {
            String rutaArchivo = (String) modeloTabla.getValueAt(currentRow + 1, 1);
            return new File(rutaArchivo);
        }
        return null;
    }

    public File obtenerAnteriorArchivo(File archivoActual) {
        int currentRow = getRowByFile(archivoActual);
        if (currentRow > 0) {
            String rutaArchivo = (String) modeloTabla.getValueAt(currentRow - 1, 1);
            return new File(rutaArchivo);
        }
        return null;
    }

    // Método auxiliar para encontrar el índice del archivo en la tabla
    private int getRowByFile(File archivo) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (((String) modeloTabla.getValueAt(i, 1)).equals(archivo.getAbsolutePath())) {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrincipalFrame().setVisible(true));
    }
}