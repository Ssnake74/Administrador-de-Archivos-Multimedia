package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;

public class ImagenesGaleria extends JPanel {

    private PrincipalFrame principal;
    private JTable tablaImagenes;
    private DefaultTableModel modeloTabla;
    private JLabel vistaPreviaImagen;
    private JButton btnAnterior;
    private JButton btnSiguiente;
    private JLabel espacioTotalLabel;

    private JButton btnMostrarMetadatos;
    private JTextArea metadataTextArea;
    private JScrollPane metadataScrollPane;
    private JLayeredPane imagenLayeredPane;

    public ImagenesGaleria(PrincipalFrame principal) {
        this.principal = principal;
        setLayout(new BorderLayout());

        initUI();
        cargarArchivos();
    }

    private void initUI() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nombre del Archivo");
        modeloTabla.addColumn("Ruta");
        modeloTabla.addColumn("Tamaño (MB)");

        tablaImagenes = new JTable(modeloTabla);
        tablaImagenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaImagenes.setAutoCreateRowSorter(true);

        tablaImagenes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tablaImagenes.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = tablaImagenes.convertRowIndexToModel(selectedRow);
                    String rutaArchivo = (String) modeloTabla.getValueAt(modelRow, 1);
                    File archivo = new File(rutaArchivo);
                    mostrarVistaPrevia(archivo);
                    cargarMetadatosImagen(archivo);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaImagenes);

        btnMostrarMetadatos = new JButton("i");
        btnMostrarMetadatos.setMargin(new Insets(1, 1, 1, 1));
        btnMostrarMetadatos.setPreferredSize(new Dimension(20, 20));
        btnMostrarMetadatos.addActionListener(e -> toggleMetadataDisplay());

        metadataTextArea = new JTextArea();
        metadataTextArea.setEditable(false);
        metadataTextArea.setOpaque(true); 
        metadataTextArea.setForeground(Color.WHITE);
        metadataTextArea.setBackground(Color.BLACK); 

        metadataScrollPane = new JScrollPane(metadataTextArea);
        metadataScrollPane.setOpaque(false);
        metadataScrollPane.getViewport().setOpaque(false);
        metadataScrollPane.setVisible(false);

        imagenLayeredPane = new JLayeredPane();
        imagenLayeredPane.setPreferredSize(new Dimension(400, 400));

        vistaPreviaImagen = new JLabel();
        vistaPreviaImagen.setHorizontalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setVerticalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setBounds(0, 0, 400, 400);

        imagenLayeredPane.add(vistaPreviaImagen, Integer.valueOf(1));

        btnMostrarMetadatos.setBounds(5, 5, 20, 20);
        imagenLayeredPane.add(btnMostrarMetadatos, Integer.valueOf(3));

        metadataScrollPane.setBounds(50, 50, 300, 300);
        imagenLayeredPane.add(metadataScrollPane, Integer.valueOf(2));

        JPanel panelVistaPrevia = new JPanel(new BorderLayout());
        panelVistaPrevia.add(new JLabel("Vista Previa"), BorderLayout.NORTH);
        panelVistaPrevia.add(imagenLayeredPane, BorderLayout.CENTER);

        JPanel panelNavegacion = new JPanel(new FlowLayout());
        btnAnterior = new JButton("⬅️ Anterior");
        btnSiguiente = new JButton("Siguiente ➡️");

        btnAnterior.addActionListener(e -> seleccionarImagenAnterior());
        btnSiguiente.addActionListener(e -> seleccionarImagenSiguiente());

        panelNavegacion.add(btnAnterior);
        panelNavegacion.add(btnSiguiente);

        espacioTotalLabel = new JLabel("Espacio total ocupado: Calculando...");
        panelNavegacion.add(espacioTotalLabel);

        panelVistaPrevia.add(panelNavegacion, BorderLayout.SOUTH);

        add(scrollPane, BorderLayout.WEST);
        add(panelVistaPrevia, BorderLayout.CENTER);
    }

    public void cargarArchivos() {
        modeloTabla.setRowCount(0);
        long espacioTotal = 0;

        File rutaSeleccionada = principal.getRutaSeleccionada();
        if (rutaSeleccionada != null && rutaSeleccionada.isDirectory()) {
            List<File> archivosImagen = buscarArchivosImagen(rutaSeleccionada);
            for (File archivo : archivosImagen) {
                long tamañoMB = archivo.length() / (1024 * 1024);
                espacioTotal += tamañoMB;
                modeloTabla.addRow(new Object[]{archivo.getName(), archivo.getAbsolutePath(), tamañoMB});
            }
            espacioTotalLabel.setText("Espacio total ocupado: " + espacioTotal + " MB");
        }
    }

    private List<File> buscarArchivosImagen(File directorio) {
        List<File> archivosImagen = new ArrayList<>();
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isDirectory()) {
                    archivosImagen.addAll(buscarArchivosImagen(archivo));
                } else if (archivo.getName().toLowerCase().endsWith(".png") ||
                           archivo.getName().toLowerCase().endsWith(".jpg") ||
                           archivo.getName().toLowerCase().endsWith(".jpeg")) {
                    archivosImagen.add(archivo);
                }
            }
        }
        return archivosImagen;
    }

    private void mostrarVistaPrevia(File archivo) {
        if (archivo.exists() && archivo.isFile()) {
            ImageIcon icono = new ImageIcon(new ImageIcon(archivo.getAbsolutePath()).getImage()
                    .getScaledInstance(400, 400, Image.SCALE_SMOOTH));
            vistaPreviaImagen.setIcon(icono);
        } else {
            vistaPreviaImagen.setIcon(null);
        }
    }

    private void seleccionarImagenAnterior() {
        int selectedRow = tablaImagenes.getSelectedRow();
        if (selectedRow > 0) {
            tablaImagenes.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
        }
    }

    private void seleccionarImagenSiguiente() {
        int selectedRow = tablaImagenes.getSelectedRow();
        if (selectedRow < tablaImagenes.getRowCount() - 1) {
            tablaImagenes.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
        }
    }

    private void cargarMetadatosImagen(File archivo) {
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