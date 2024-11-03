package control;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;
import vista.PrincipalFrame;

public class ArchivoDuplicadoDetector extends JPanel {

    private PrincipalFrame principal;
    private JTable tablaDuplicados;
    private DefaultTableModel modeloTabla;
    private JButton btnEliminarDuplicados; 
    private Map<String, List<File>> archivosDuplicados;

    public ArchivoDuplicadoDetector(PrincipalFrame principal) {
        this.principal = principal;
        this.archivosDuplicados = new HashMap<>();
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Nombre del Archivo");
        modeloTabla.addColumn("Ruta");
        modeloTabla.addColumn("Tamaño");

        tablaDuplicados = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaDuplicados);

        btnEliminarDuplicados = new JButton("Eliminar Seleccionados");
        btnEliminarDuplicados.addActionListener(e -> eliminarDuplicados());
        btnEliminarDuplicados.setVisible(false);  

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnEliminarDuplicados);  

        add(scrollPane, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    public void detectarDuplicados() {
        File rutaSeleccionada = principal.getRutaSeleccionada();
        if (rutaSeleccionada == null) {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero.");
            return;
        }

        modeloTabla.setRowCount(0);
        archivosDuplicados.clear();

        try {
            archivosDuplicados = buscarDuplicados(rutaSeleccionada);
            for (List<File> duplicados : archivosDuplicados.values()) {
                for (File archivo : duplicados) {
                    modeloTabla.addRow(new Object[]{
                        archivo.getName(),
                        archivo.getAbsolutePath(),
                        archivo.length() / 1024 + " KB"  
                    });
                }
            }
            JOptionPane.showMessageDialog(this, "Detección de duplicados completada.");
        } catch (IOException | NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(this, "Error al detectar archivos duplicados: " + ex.getMessage());
        }
    }

    private Map<String, List<File>> buscarDuplicados(File directorio) throws IOException, NoSuchAlgorithmException {
        Map<Long, List<File>> archivosPorTamaño = new HashMap<>();
        Map<String, List<File>> duplicados = new HashMap<>();

        Files.walk(Paths.get(directorio.getAbsolutePath()))
            .filter(Files::isRegularFile)
            .forEach(path -> {
                File archivo = path.toFile();
                long tamaño = archivo.length();
                archivosPorTamaño.putIfAbsent(tamaño, new ArrayList<>());
                archivosPorTamaño.get(tamaño).add(archivo);
            });

        for (List<File> archivos : archivosPorTamaño.values()) {
            if (archivos.size() > 1) {
                Map<String, List<File>> archivosPorHash = new HashMap<>();

                for (File archivo : archivos) {
                    String hash = calcularHash(archivo);
                    archivosPorHash.putIfAbsent(hash, new ArrayList<>());
                    archivosPorHash.get(hash).add(archivo);
                }

                archivosPorHash.values().stream()
                    .filter(lista -> lista.size() > 1)
                    .forEach(lista -> duplicados.put(lista.get(0).getName(), lista));
            }
        }
        return duplicados;
    }

    private String calcularHash(File archivo) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        try (FileInputStream fis = new FileInputStream(archivo)) {
            byte[] byteArray = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesRead);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void eliminarDuplicados() {
        int[] filasSeleccionadas = tablaDuplicados.getSelectedRows();
        if (filasSeleccionadas.length == 0) {
            JOptionPane.showMessageDialog(this, "Seleccione los archivos duplicados que desea eliminar.");
            return;
        }

        for (int i = filasSeleccionadas.length - 1; i >= 0; i--) {
            int fila = filasSeleccionadas[i];
            String rutaArchivo = (String) modeloTabla.getValueAt(fila, 1);
            File archivo = new File(rutaArchivo);
            if (archivo.delete()) {
                modeloTabla.removeRow(fila);
                System.out.println("Archivo eliminado: " + rutaArchivo);
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el archivo: " + rutaArchivo);
            }
        }
        JOptionPane.showMessageDialog(this, "Archivos seleccionados eliminados.");
    }

    public JButton getBtnEliminarDuplicados() {
        return btnEliminarDuplicados;
    }
}