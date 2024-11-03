package vista;

import modelo.Playlist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlaylistPanel extends JPanel {

    private List<Playlist> playlists = new ArrayList<>();
    private JComboBox<String> comboPlaylists;
    private JTable tablaCanciones;
    private DefaultTableModel modeloTabla;
    private PrincipalFrame principal;

    public PlaylistPanel(PrincipalFrame principal) {
        this.principal = principal;
        setLayout(new BorderLayout());
        initUI();
        cargarPlaylists();
    }

    PlaylistPanel() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void initUI() {
        JPanel panelSuperior = new JPanel(new FlowLayout());
        comboPlaylists = new JComboBox<>();
        comboPlaylists.addActionListener(e -> cargarCancionesPlaylist());

        JButton btnCrearPlaylist = new JButton("Crear Playlist");
        btnCrearPlaylist.addActionListener(e -> crearPlaylist());

        JButton btnEliminarPlaylist = new JButton("Eliminar Playlist");
        btnEliminarPlaylist.addActionListener(e -> eliminarPlaylist());

        JButton btnAgregarCancion = new JButton("Agregar Canción");
        btnAgregarCancion.addActionListener(e -> agregarCancion());

        JButton btnEliminarCancion = new JButton("Eliminar Canción");
        btnEliminarCancion.addActionListener(e -> eliminarCancion());

        JButton btnGuardarPlaylist = new JButton("Guardar Playlist");
        btnGuardarPlaylist.addActionListener(e -> guardarPlaylists());

        JButton btnRegresar = new JButton("Regresar");
        btnRegresar.addActionListener(e -> regresar());

        panelSuperior.add(comboPlaylists);
        panelSuperior.add(btnCrearPlaylist);
        panelSuperior.add(btnEliminarPlaylist);
        panelSuperior.add(btnAgregarCancion);
        panelSuperior.add(btnEliminarCancion);
        panelSuperior.add(btnGuardarPlaylist);
        panelSuperior.add(btnRegresar);

        add(panelSuperior, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Ruta de la Canción");

        tablaCanciones = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaCanciones);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void crearPlaylist() {
        String nombrePlaylist = JOptionPane.showInputDialog(this, "Ingrese el nombre de la playlist:");
        if (nombrePlaylist != null && !nombrePlaylist.isEmpty()) {
            playlists.add(new Playlist(nombrePlaylist));
            comboPlaylists.addItem(nombrePlaylist);
            JOptionPane.showMessageDialog(this, "Playlist creada con éxito.");
        }
    }

    private void eliminarPlaylist() {
        int index = comboPlaylists.getSelectedIndex();
        if (index != -1) {
            playlists.remove(index);
            comboPlaylists.removeItemAt(index);
            modeloTabla.setRowCount(0);
            guardarPlaylists();
            JOptionPane.showMessageDialog(this, "Playlist eliminada.");
        }
    }

    private void cargarCancionesPlaylist() {
        int index = comboPlaylists.getSelectedIndex();
        if (index != -1) {
            Playlist playlist = playlists.get(index);
            modeloTabla.setRowCount(0);
            for (String ruta : playlist.getCanciones()) {
                modeloTabla.addRow(new Object[]{ruta});
            }
        }
    }

    private void agregarCancion() {
        File carpetaSeleccionada = principal.getRutaSeleccionada();

        if (carpetaSeleccionada == null || !carpetaSeleccionada.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Seleccione una carpeta primero desde la interfaz principal.");
            return;
        }

        List<File> archivosMusica = buscarArchivosMusica(carpetaSeleccionada);
        if (archivosMusica.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron archivos de música (mp3, wma) en la carpeta seleccionada.");
            return;
        }

        Object[] opciones = archivosMusica.stream().map(File::getName).toArray();
        List<String> seleccionados = new ArrayList<>();
        JList<Object> listaArchivos = new JList<>(opciones);
        listaArchivos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        int resultado = JOptionPane.showConfirmDialog(this, new JScrollPane(listaArchivos), "Seleccione las canciones a agregar", JOptionPane.OK_CANCEL_OPTION);

        if (resultado == JOptionPane.OK_OPTION) {
            int[] indicesSeleccionados = listaArchivos.getSelectedIndices();
            for (int i : indicesSeleccionados) {
                seleccionados.add(archivosMusica.get(i).getAbsolutePath());
            }
        }

        int index = comboPlaylists.getSelectedIndex();
        if (index != -1 && !seleccionados.isEmpty()) {
            Playlist playlist = playlists.get(index);
            for (String ruta : seleccionados) {
                playlist.agregarCancion(ruta);
                modeloTabla.addRow(new Object[]{ruta});
            }
            guardarPlaylists();
        }
    }

    private List<File> buscarArchivosMusica(File directorio) {
        List<File> archivosMusica = new ArrayList<>();
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File archivo : archivos) {
                if (archivo.isFile() && (archivo.getName().toLowerCase().endsWith(".mp3") || archivo.getName().toLowerCase().endsWith(".wma"))) {
                    archivosMusica.add(archivo);
                }
            }
        }
        return archivosMusica;
    }

    private void eliminarCancion() {
        int selectedRow = tablaCanciones.getSelectedRow();
        int playlistIndex = comboPlaylists.getSelectedIndex();
        if (selectedRow != -1 && playlistIndex != -1) {
            Playlist playlist = playlists.get(playlistIndex);
            playlist.getCanciones().remove(selectedRow);
            modeloTabla.removeRow(selectedRow);
            guardarPlaylists();
        }
    }

    private void guardarPlaylists() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter("playlists.json")) {
            gson.toJson(playlists, writer);
            JOptionPane.showMessageDialog(this, "Playlists guardadas exitosamente.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar playlists: " + e.getMessage());
        }
    }

    private void cargarPlaylists() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("playlists.json")) {
            Type playlistListType = new TypeToken<List<Playlist>>(){}.getType();
            playlists = gson.fromJson(reader, playlistListType);

            for (Playlist playlist : playlists) {
                comboPlaylists.addItem(playlist.getNombre());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se encontraron playlists guardadas.");
        }
    }

    public Playlist obtenerPlaylistSeleccionada() {
        int index = comboPlaylists.getSelectedIndex();
        if (index != -1) {
            return playlists.get(index);
        }
        return null;
    }

    private void regresar() {
        if (principal.getMainPanel() instanceof JPanel) {
            JPanel mainPanel = (JPanel) principal.getMainPanel();
            if (mainPanel.getLayout() instanceof CardLayout) {
                CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                cardLayout.show(mainPanel, "musica");
            }
        }
    }
}