package vista;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagenesGaleria extends JPanel {

    private PrincipalFrame principal;
    private JTable tablaImagenes;
    private DefaultTableModel modeloTabla;
    private JLabel vistaPreviaImagen;
    private JButton btnAnterior;
    private JButton btnSiguiente;

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

        tablaImagenes = new JTable(modeloTabla);
        tablaImagenes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tablaImagenes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = tablaImagenes.getSelectedRow();
                    if (selectedRow != -1) {
                        String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
                        mostrarVistaPrevia(new File(rutaArchivo));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaImagenes);

        vistaPreviaImagen = new JLabel();
        vistaPreviaImagen.setHorizontalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setVerticalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setPreferredSize(new Dimension(400, 400));

        // Panel de vista previa
        JPanel panelVistaPrevia = new JPanel(new BorderLayout());
        panelVistaPrevia.add(new JLabel("Vista Previa"), BorderLayout.NORTH);
        panelVistaPrevia.add(vistaPreviaImagen, BorderLayout.CENTER);

        // Panel de navegación
        JPanel panelNavegacion = new JPanel(new FlowLayout());
        btnAnterior = new JButton("⬅️ Anterior");
        btnSiguiente = new JButton("Siguiente ➡️");

        btnAnterior.addActionListener(e -> seleccionarImagenAnterior());
        btnSiguiente.addActionListener(e -> seleccionarImagenSiguiente());

        panelNavegacion.add(btnAnterior);
        panelNavegacion.add(btnSiguiente);

        // Agregar paneles
        panelVistaPrevia.add(panelNavegacion, BorderLayout.SOUTH);

        add(scrollPane, BorderLayout.WEST);
        add(panelVistaPrevia, BorderLayout.CENTER);
    }

    public void cargarArchivos() {
        modeloTabla.setRowCount(0);

        File rutaSeleccionada = principal.getRutaSeleccionada();
        if (rutaSeleccionada != null && rutaSeleccionada.isDirectory()) {
            List<File> archivosImagen = buscarArchivosImagen(rutaSeleccionada);
            for (File archivo : archivosImagen) {
                modeloTabla.addRow(new Object[]{archivo.getName(), archivo.getAbsolutePath()});
            }
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
}