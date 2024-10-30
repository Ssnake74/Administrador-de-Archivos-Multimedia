package vista;

import control.BuscadorArchivos;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class PrincipalFrame extends JFrame {

    private JButton btnSeleccionarCarpeta;
    private JLabel lblRuta;
    private JTable tablaArchivos;
    private DefaultTableModel modeloTabla;
    private JLabel vistaPreviaImagen;  // Label para mostrar la vista previa

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
        lblRuta = new JLabel("Ruta seleccionada: Ninguna");

        btnSeleccionarCarpeta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seleccionarCarpeta();
            }
        });

        // Inicializar la tabla y el modelo de tabla
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nombre del Archivo");
        modeloTabla.addColumn("Ruta");

        tablaArchivos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaArchivos);

        // Panel para vista previa de imagen
        vistaPreviaImagen = new JLabel();
        vistaPreviaImagen.setHorizontalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setVerticalAlignment(JLabel.CENTER);
        vistaPreviaImagen.setPreferredSize(new Dimension(200, 200));
        
        JPanel panelVistaPrevia = new JPanel(new BorderLayout());
        panelVistaPrevia.add(new JLabel("Vista Previa:"), BorderLayout.NORTH);
        panelVistaPrevia.add(vistaPreviaImagen, BorderLayout.CENTER);

        // Escuchar cambios de selección en la tabla
        tablaArchivos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                mostrarVistaPrevia();
            }
        });

        // Agregar componentes al panel principal
        panel.add(btnSeleccionarCarpeta, BorderLayout.NORTH);
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

            // Limpiar la tabla antes de realizar la nueva búsqueda
            modeloTabla.setRowCount(0);

            // Realizar búsqueda recursiva
            BuscadorArchivos buscador = new BuscadorArchivos();
            List<File> archivosEncontrados = buscador.buscarArchivosMultimedia(selectedDirectory);

            // Añadir los archivos encontrados a la tabla
            for (File archivo : archivosEncontrados) {
                modeloTabla.addRow(new Object[]{archivo.getName(), archivo.getAbsolutePath()});
            }
        }
    }

    // Método para mostrar la vista previa de la imagen seleccionada
    private void mostrarVistaPrevia() {
        int selectedRow = tablaArchivos.getSelectedRow();
        if (selectedRow != -1) {
            String rutaArchivo = (String) modeloTabla.getValueAt(selectedRow, 1);
            File archivo = new File(rutaArchivo);
            if (esImagen(archivo)) {
                ImageIcon icono = new ImageIcon(new ImageIcon(rutaArchivo).getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH));
                vistaPreviaImagen.setIcon(icono);
            } else {
                vistaPreviaImagen.setIcon(null);  // Si no es imagen, limpiar vista previa
            }
        }
    }

    private boolean esImagen(File archivo) {
        String nombreArchivo = archivo.getName().toLowerCase();
        return nombreArchivo.endsWith(".jpg") || nombreArchivo.endsWith(".png") || nombreArchivo.endsWith(".gif");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PrincipalFrame().setVisible(true);
            }
        });
    }
}