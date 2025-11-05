// Importamos las clases necesarias para la interfaz gráfica (swing), manejo de tablas, layouts, eventos, fechas y listas.
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/*
 * =================================================================================
 * CLASES DE LÓGICA DEL NEGOCIO
 * Estas clases no tienen nada que ver con la interfaz. Definen los datos y las
 * reglas de la biblioteca.
 * =================================================================================
 */

// --- Clase Libro: Representa un libro con sus propiedades. ---
class Libro {
    // Atributos privados para encapsular los datos del libro.
    private String codigo;       // Identificador único del libro (ej: "LIB001").
    private String titulo;       // Título del libro.
    private String autor;        // Autor del libro.
    private boolean estaPrestado; // Estado que indica si está disponible o no.

    // Constructor: Se ejecuta al crear un nuevo objeto Libro.
    public Libro(String codigo, String titulo, String autor) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.autor = autor;
        this.estaPrestado = false; // Un libro nuevo siempre está disponible por defecto.
    }

    // Métodos "getters" para obtener los valores de los atributos desde fuera de la clase.
    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public boolean isEstaPrestado() { return estaPrestado; }

    // Método "setter" para modificar el estado del préstamo.
    public void setEstaPrestado(boolean estaPrestado) { this.estaPrestado = estaPrestado; }

    // Método para obtener el estado como un texto legible ("Disponible" o "Prestado").
    public String getEstado() {
        return estaPrestado ? "Prestado" : "Disponible"; // Operador ternario: si estaPrestado es true, devuelve "Prestado", si no, "Disponible".
    }
}

// --- Clase Prestamo: Representa la transacción de un préstamo. ---
class Prestamo {
    private String codigoLibro;      // El código del libro que se prestó.
    private String idUsuario;        // La identificación del usuario que lo pidió.
    private LocalDate fechaPrestamo; // La fecha exacta en que se hizo el préstamo.
    private LocalDate fechaDevolucion; // La fecha límite para devolver el libro.

    // Constructor: Crea un nuevo préstamo.
    public Prestamo(String codigoLibro, String idUsuario) {
        this.codigoLibro = codigoLibro;
        this.idUsuario = idUsuario;
        this.fechaPrestamo = LocalDate.now(); // Captura la fecha actual del sistema.
        this.fechaDevolucion = this.fechaPrestamo.plusDays(7); // Calcula la fecha de devolución sumando 7 días a la fecha del préstamo.
    }

    // Getters para acceder a los datos del préstamo.
    public String getCodigoLibro() { return codigoLibro; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
}

// --- Clase Usuario: Modela a un usuario de la biblioteca. ---
class Usuario {
    // Atributos para los datos personales y de la cuenta.
    private String nombreCompleto, numeroIdentificacion, fechaNacimiento, genero, correoElectronico, username, password;
    private int edad, librosPedidosHistorial;

    // Atributos para gestionar el estado actual del usuario.
    private boolean tieneLibroPrestado; // Indica si el usuario ya tiene un libro en su poder.
    private boolean estaVetado;         // Indica si el usuario está castigado por una devolución tardía.
    private LocalDate fechaFinVeto;     // Guarda la fecha en que termina el castigo.
    private Prestamo prestamoActual;    // Guarda el objeto del préstamo activo.

    // Constructor: Inicializa un nuevo usuario con todos sus datos.
    public Usuario(String nombreCompleto, String numeroIdentificacion, String fechaNacimiento, int edad, String genero, String correoElectronico, String username, String password) {
        // Asignación de los parámetros recibidos a los atributos del objeto.
        this.nombreCompleto = nombreCompleto;
        this.numeroIdentificacion = numeroIdentificacion;
        this.fechaNacimiento = fechaNacimiento;
        this.edad = edad;
        this.genero = genero;
        this.correoElectronico = correoElectronico;
        this.username = username;
        this.password = password;

        // Inicialización de los valores de estado por defecto para un usuario nuevo.
        this.librosPedidosHistorial = 0;
        this.tieneLibroPrestado = false;
        this.estaVetado = false;
    }

    // Getters y Setters para interactuar con los atributos del usuario.
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public boolean tieneLibroPrestado() { return tieneLibroPrestado; }
    public void setTieneLibroPrestado(boolean tieneLibroPrestado) { this.tieneLibroPrestado = tieneLibroPrestado; }
    public Prestamo getPrestamoActual() { return prestamoActual; }
    public void setPrestamoActual(Prestamo prestamoActual) { this.prestamoActual = prestamoActual; }
    public boolean isEstaVetado() { return estaVetado; }
    public void setEstaVetado(boolean estaVetado) { this.estaVetado = estaVetado; }
    public LocalDate getFechaFinVeto() { return fechaFinVeto; }
    public void setFechaFinVeto(LocalDate fechaFinVeto) { this.fechaFinVeto = fechaFinVeto; }
    public void incrementarLibrosPedidos() { this.librosPedidosHistorial++; }

    // Método para comprobar si el período de veto ya ha terminado.
    public void verificarVeto() {
        // Si el usuario está vetado Y la fecha actual es posterior a la fecha de fin del veto...
        if (this.estaVetado && LocalDate.now().isAfter(this.fechaFinVeto)) {
            // ...entonces se le quita el veto.
            this.estaVetado = false;
            this.fechaFinVeto = null; // Se limpia la fecha de fin del veto.
        }
    }
}

/*
 * =================================================================================
 * CLASE PRINCIPAL DE LA INTERFAZ GRÁFICA (GUI)
 * Esta clase construye y gestiona toda la parte visual de la aplicación.
 * =================================================================================
 */

// La clase principal hereda de JFrame, lo que significa que es una ventana.
public class BibliotecaGUI extends JFrame {

    // --- Atributos de la aplicación (Datos) ---
    private ArrayList<Libro> inventario = new ArrayList<>();   // Lista para guardar todos los libros.
    private ArrayList<Usuario> usuarios = new ArrayList<>();   // Lista para guardar todos los usuarios registrados.
    private Usuario usuarioLogueado;                           // Objeto para mantener la información del usuario que ha iniciado sesión.

    // --- Atributos de la Interfaz Gráfica (Componentes Swing) ---
    private CardLayout cardLayout;      // Un layout especial que permite mostrar paneles como si fueran una baraja de cartas.
    private JPanel mainPanel;           // El panel principal que contendrá los demás paneles (login, registro, app).
    private JTable tablaInventario;     // La tabla visual donde se mostrará el inventario.
    private DefaultTableModel tableModel; // El "modelo" o "contenido" de la tabla, que gestiona las filas y columnas.
    private JLabel welcomeLabel;        // Etiqueta para mostrar el mensaje de bienvenida.

    // Constructor de la interfaz: Se ejecuta al crear la ventana.
    public BibliotecaGUI() {
        // --- 1. Carga inicial de datos ---
        inicializarInventario(); // Llama al método para cargar los libros y el usuario de prueba.

        // --- 2. Configuración de la ventana principal (el JFrame) ---
        setTitle("Biblioteca del ETITC");             // Pone el título a la ventana.
        setSize(800, 600);                          // Define el tamaño inicial de la ventana en píxeles.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Hace que el programa se cierre al pulsar la 'X' de la ventana.
        setLocationRelativeTo(null);                // Centra la ventana en la pantalla.

        // --- 3. Configuración del layout principal (CardLayout) ---
        cardLayout = new CardLayout();            // Crea una nueva instancia del CardLayout.
        mainPanel = new JPanel(cardLayout);       // Crea el panel principal y le asigna el CardLayout.

        // --- 4. Crear los diferentes paneles (las "cartas" de la baraja) ---
        JPanel loginPanel = createLoginPanel();       // Panel para iniciar sesión.
        JPanel registerPanel = createRegisterPanel(); // Panel para registrarse.
        JPanel appPanel = createAppPanel();           // Panel principal de la aplicación.

        // --- 5. Añadir los paneles al panel principal con un nombre identificador ---
        mainPanel.add(loginPanel, "login");           // Añade el panel de login con el nombre "login".
        mainPanel.add(registerPanel, "register");     // Añade el panel de registro con el nombre "register".
        mainPanel.add(appPanel, "app");               // Añade el panel de la app con el nombre "app".

        // Añade el panel principal a la ventana.
        add(mainPanel);

        // Muestra el panel de login por defecto al iniciar la aplicación.
        cardLayout.show(mainPanel, "login");
    }

    // --- Método para crear el panel de Inicio de Sesión ---
    private JPanel createLoginPanel() {
        // Usamos GridBagLayout para tener un control más preciso sobre la posición de los componentes.
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(173, 216, 230)); // Establece un color de fondo azul claro.
        GridBagConstraints gbc = new GridBagConstraints(); // Objeto para configurar la posición.
        gbc.insets = new Insets(5, 5, 5, 5); // Un pequeño margen entre componentes.

        // Creación de los componentes visuales.
        JLabel userLabel = new JLabel("Usuario:");
        JTextField userText = new JTextField(20); // Campo de texto de 20 columnas de ancho.
        JLabel passLabel = new JLabel("Contraseña:");
        JPasswordField passText = new JPasswordField(20); // Campo especial para contraseñas.
        JButton loginButton = new JButton("Iniciar Sesión");
        JButton registerButton = new JButton("¿No tienes cuenta? Regístrate");

        // Posicionamiento de cada componente en la "parrilla" del GridBagLayout.
        gbc.gridx = 0; gbc.gridy = 0; panel.add(userLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(userText, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(passText, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.CENTER; panel.add(loginButton, gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(registerButton, gbc);

        // --- Lógica de los botones (Event Handling) ---
        // Se añade un "oyente de acción" al botón de login. El código dentro se ejecutará cuando se haga clic.
        loginButton.addActionListener(e -> {
            String username = userText.getText(); // Obtiene el texto del campo de usuario.
            String password = new String(passText.getPassword()); // Obtiene la contraseña del campo de contraseña.

            boolean loggedIn = false; // Variable para verificar si el login fue exitoso.
            // Recorre la lista de usuarios.
            for (Usuario u : usuarios) {
                // Si el usuario y la contraseña coinciden...
                if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                    usuarioLogueado = u; // Guarda el usuario que ha iniciado sesión.
                    loggedIn = true;     // Marca el login como exitoso.
                    break;               // Sale del bucle porque ya encontramos al usuario.
                }
            }

            if (loggedIn) {
                // Si el login fue exitoso:
                welcomeLabel.setText("¡Bienvenido, " + usuarioLogueado.getUsername() + "!"); // Actualiza el mensaje de bienvenida.
                actualizarTablaInventario(); // Carga los datos en la tabla.
                cardLayout.show(mainPanel, "app"); // Cambia al panel principal de la aplicación.
            } else {
                // Si no, muestra una ventana emergente de error.
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Al hacer clic en el botón de registrar, simplemente cambiamos al panel de registro.
        registerButton.addActionListener(e -> cardLayout.show(mainPanel, "register"));

        return panel; // Devuelve el panel ya creado y configurado.
    }

    // --- Método para crear el panel de Registro ---
    private JPanel createRegisterPanel() {
        // Similar al panel de login, usamos GridBagLayout para el formulario.
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST; // Alinea los componentes a la izquierda.

        // Creación de todos los campos de texto y etiquetas para el formulario de registro.
        JTextField nombreText = new JTextField(20);
        JTextField idText = new JTextField(20);
        JTextField fechaText = new JTextField(20);
        JTextField edadText = new JTextField(20);
        JTextField generoText = new JTextField(20);
        JTextField emailText = new JTextField(20);
        JTextField userText = new JTextField(20);
        JPasswordField passText = new JPasswordField(20);

        // Se añaden todos los componentes al panel en orden.
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(nombreText, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Identificación:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; panel.add(idText, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Fecha Nacimiento (DD/MM/AAAA):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(fechaText, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Edad:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(edadText, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Género:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; panel.add(generoText, gbc);
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(emailText, gbc);
        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Nombre de Usuario:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; panel.add(userText, gbc);
        gbc.gridx = 0; gbc.gridy = 7; panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7; panel.add(passText, gbc);

        JButton confirmButton = new JButton("Confirmar Registro");
        JButton backButton = new JButton("Volver al Login");

        JPanel buttonPanel = new JPanel(); // Un panel extra para agrupar los dos botones.
        buttonPanel.add(confirmButton);
        buttonPanel.add(backButton);
        gbc.gridx = 1; gbc.gridy = 8; gbc.anchor = GridBagConstraints.CENTER; panel.add(buttonPanel, gbc);

        // Lógica para el botón de confirmar registro.
        confirmButton.addActionListener(e -> {
            try {
                // Validación simple para asegurarse de que los campos clave no estén vacíos.
                if(userText.getText().isEmpty() || idText.getText().isEmpty()){
                    JOptionPane.showMessageDialog(this, "Usuario e Identificación no pueden estar vacíos.", "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Detiene la ejecución del método si hay un error.
                }

                // Crea un nuevo objeto Usuario con los datos de los campos de texto.
                Usuario newUser = new Usuario(nombreText.getText(), idText.getText(), fechaText.getText(), Integer.parseInt(edadText.getText()), generoText.getText(), emailText.getText(), userText.getText(), new String(passText.getPassword()));
                usuarios.add(newUser); // Añade el nuevo usuario a la lista de usuarios.

                JOptionPane.showMessageDialog(this, "¡Registro exitoso!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(mainPanel, "login"); // Vuelve a la pantalla de login.
            } catch (NumberFormatException ex) {
                // Si el texto en "Edad" no es un número, salta este error.
                JOptionPane.showMessageDialog(this, "La edad debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        // El botón "Volver" simplemente cambia al panel de login.
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        return panel;
    }

    // --- Método para crear el panel principal de la Aplicación ---
    private JPanel createAppPanel() {
        // Usamos BorderLayout para dividir la pantalla en secciones: NORTE, CENTRO, SUR.
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(200, 200, 200)); // Un gris muy suave


        // --- Sección NORTE: Mensaje de Bienvenida ---
        welcomeLabel = new JLabel("", SwingConstants.CENTER); // Etiqueta centrada.
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Cambia la fuente.
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // --- Sección CENTRO: Contiene los botones de acción y la tabla ---
        // Panel para los botones. FlowLayout los pone uno al lado del otro.
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.setBackground(new Color(200, 200, 200));
        JButton pedirButton = new JButton("Pedir Libro Seleccionado");
        JButton devolverButton = new JButton("Devolver mi Libro");
        JButton estadoButton = new JButton("Consultar mi Estado");
        actionPanel.add(pedirButton);
        actionPanel.add(devolverButton);
        actionPanel.add(estadoButton);

        // Configuración de la tabla del inventario.
        String[] columnNames = {"Código", "Título", "Autor", "Estado"}; // Nombres de las columnas.
        tableModel = new DefaultTableModel(columnNames, 0) {
            // Hacemos que las celdas de la tabla no sean editables por el usuario.
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaInventario = new JTable(tableModel); // Creamos la tabla con el modelo definido.
        JScrollPane scrollPane = new JScrollPane(tablaInventario); // Añadimos la tabla a un panel con barras de scroll.

        // Asignar el renderer personalizado a la columna "Estado" (índice 3).
        tablaInventario.getColumnModel().getColumn(3).setCellRenderer(new PrestamoCellRenderer());


        // Creamos un panel central para organizar los botones arriba y la tabla abajo.
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(actionPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        // --- Sección SUR: Botón de Cerrar Sesión ---
        JButton logoutButton = new JButton("Cerrar Sesión");
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(new Color(200, 200, 200));// Alinea el botón a la derecha.
        southPanel.add(logoutButton);
        panel.add(southPanel, BorderLayout.SOUTH);

        // --- Lógica para los botones del panel principal ---
        pedirButton.addActionListener(e -> realizarPrestamo());
        devolverButton.addActionListener(e -> realizarDevolucion());
        estadoButton.addActionListener(e -> consultarEstado());
        logoutButton.addActionListener(e -> {
            usuarioLogueado = null; // Borra la información del usuario logueado.
            cardLayout.show(mainPanel, "login"); // Vuelve a la pantalla de login.
        });

        return panel;
    }

    // --- Métodos que conectan la GUI con la Lógica ---

    // Lógica para realizar un préstamo.
    private void realizarPrestamo() {
        usuarioLogueado.verificarVeto(); // Primero, comprueba si el veto del usuario ha expirado.

        // 1. Validar si el usuario está vetado.
        if (usuarioLogueado.isEstaVetado()) {
            JOptionPane.showMessageDialog(this, "No puedes pedir libros. Estás vetado hasta: " +
                usuarioLogueado.getFechaFinVeto().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "Veto Activo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Validar si el usuario ya tiene un libro.
        if (usuarioLogueado.tieneLibroPrestado()) {
            JOptionPane.showMessageDialog(this, "Ya tienes un libro prestado. Debes devolverlo primero.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Obtener la fila seleccionada por el usuario en la tabla.
        int selectedRow = tablaInventario.getSelectedRow();
        if (selectedRow == -1) { // Si es -1, significa que no hay ninguna fila seleccionada.
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un libro de la tabla para pedirlo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. Obtener el código del libro de la fila seleccionada.
        String codigoLibro = (String) tableModel.getValueAt(selectedRow, 0); // Fila seleccionada, columna 0 (código).
        Libro libroAPrestar = buscarLibroPorCodigo(codigoLibro);

        // 5. Validar si el libro está disponible y proceder con el préstamo.
        if (libroAPrestar != null && !libroAPrestar.isEstaPrestado()) {
            // Actualizar el estado del libro.
            libroAPrestar.setEstaPrestado(true);
            // Crear el objeto préstamo.
            Prestamo nuevoPrestamo = new Prestamo(libroAPrestar.getCodigo(), usuarioLogueado.getNumeroIdentificacion());
            // Actualizar el estado del usuario.
            usuarioLogueado.setTieneLibroPrestado(true);
            usuarioLogueado.setPrestamoActual(nuevoPrestamo);
            usuarioLogueado.incrementarLibrosPedidos();

            // Mostrar mensaje de éxito.
            JOptionPane.showMessageDialog(this, "¡Préstamo exitoso!\nLibro: " + libroAPrestar.getTitulo() + "\nDevolver antes de: " +
                nuevoPrestamo.getFechaDevolucion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "Préstamo Realizado", JOptionPane.INFORMATION_MESSAGE);

            // Actualizar la tabla para que el libro aparezca como "Prestado".
            actualizarTablaInventario();
        } else {
            JOptionPane.showMessageDialog(this, "El libro seleccionado no está disponible.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Lógica para realizar una devolución.
    private void realizarDevolucion() {
        // 1. Validar si el usuario realmente tiene un libro para devolver.
        if (!usuarioLogueado.tieneLibroPrestado()) {
            JOptionPane.showMessageDialog(this, "No tienes ningún libro prestado para devolver.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Prestamo prestamo = usuarioLogueado.getPrestamoActual();
        Libro libroADevolver = buscarLibroPorCodigo(prestamo.getCodigoLibro());

        if (libroADevolver != null) {
            // 2. Comprobar si la fecha actual es posterior a la fecha de devolución.
            if (LocalDate.now().isAfter(prestamo.getFechaDevolucion())) {
                // Si la devolución es tardía, se aplica el veto.
                usuarioLogueado.setEstaVetado(true);
                usuarioLogueado.setFechaFinVeto(LocalDate.now().plusDays(3)); // El veto dura 3 días desde hoy.
                JOptionPane.showMessageDialog(this, "¡Devolución TARDÍA!\nComo castigo, estarás vetado por 3 días.", "Devolución", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Libro devuelto a tiempo. ¡Gracias!", "Devolución", JOptionPane.INFORMATION_MESSAGE);
            }

            // 3. Actualizar los estados del libro y del usuario.
            libroADevolver.setEstaPrestado(false); // El libro vuelve a estar disponible.
            usuarioLogueado.setTieneLibroPrestado(false); // El usuario ya no tiene un libro.
            usuarioLogueado.setPrestamoActual(null); // Se elimina el préstamo actual.
            actualizarTablaInventario(); // Se actualiza la tabla para reflejar el cambio.
        }
    }

    // Lógica para mostrar el estado del usuario.
    private void consultarEstado() {
        usuarioLogueado.verificarVeto(); // Asegurarse de que el estado de veto esté actualizado.
        String estado = "Estado: ACTIVO ✅\nNo tienes ninguna multa o veto."; // Mensaje por defecto.

        // Si está vetado, se cambia el mensaje.
        if (usuarioLogueado.isEstaVetado()) {
            estado = "Estado: VETADO ❌\nNo podrás pedir libros hasta el: " +
                usuarioLogueado.getFechaFinVeto().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        // Si tiene un libro prestado, se añade esa información al mensaje.
        if (usuarioLogueado.tieneLibroPrestado()) {
            Libro libroActual = buscarLibroPorCodigo(usuarioLogueado.getPrestamoActual().getCodigoLibro());
            estado += "\n\n--- Libro en Préstamo ---\nTítulo: " + libroActual.getTitulo() +
                      "\nFecha límite: " + usuarioLogueado.getPrestamoActual().getFechaDevolucion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            estado += "\n\nNo tienes libros en préstamo actualmente.";
        }

        // Se muestra toda la información en una ventana emergente.
        JOptionPane.showMessageDialog(this, estado, "Estado de Usuario", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- Métodos Utilitarios ---

    // Actualiza el contenido de la tabla del inventario.
    private void actualizarTablaInventario() {
        tableModel.setRowCount(0); // Borra todas las filas existentes en la tabla.

        // Recorre la lista de libros del inventario.
        for (Libro libro : inventario) {
            // Crea un array de objetos con los datos de cada libro.
            Object[] row = {
                libro.getCodigo(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getEstado()
            };
            tableModel.addRow(row); // Añade la nueva fila al modelo de la tabla.
        }
    }

    // Busca un libro en el inventario por su código.
    private Libro buscarLibroPorCodigo(String codigo) {
        for (Libro libro : inventario) {
            if (libro.getCodigo().equals(codigo)) {
                return libro; // Devuelve el objeto libro si lo encuentra.
            }
        }
        return null; // Devuelve null si no encuentra ningún libro con ese código.
    }

    // Carga los datos iniciales de la aplicación.
    private void inicializarInventario() {
        // Añade los 10 libros al inventario.
        inventario.add(new Libro("LIB001", "Satanás", "Mario Mendoza"));
        inventario.add(new Libro("LIB002", "Cosas que piensas...", "Amalia Andrade"));
        inventario.add(new Libro("LIB003", "Los siete maridos de Evelyn Hugo", "Taylor Jenkins Reid"));
        inventario.add(new Libro("LIB004", "Blue sisters", "Coco Mellors"));
        inventario.add(new Libro("LIB005", "Cadáver exquisito", "Agustina Bazterrica"));
        inventario.add(new Libro("LIB006", "Lo que la nieve susurra...", "María Martinez"));
        inventario.add(new Libro("LIB007", "Lady masacre", "Mario Mendoza"));
        inventario.add(new Libro("LIB008", "Amarilla", "R. F. Kuang"));
        inventario.add(new Libro("LIB009", "La cúpula", "Stephen King"));
        inventario.add(new Libro("LIB010", "Relato de un asesino", "Mario Mendoza"));

        // Añade un usuario de prueba para facilitar el acceso.
        Usuario usuarioDePrueba = new Usuario("Usuario Admin", "0000", "01/01/2000", 25, "N/A", "admin@test.com", "admin", "admin");
        usuarios.add(usuarioDePrueba);
    }

    // --- Clase interna para personalizar el renderizado de la celda de Estado ---
    class PrestamoCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // 1. Llama al método de la superclase para obtener el componente de celda por defecto.
            Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 2. Obtiene el código del libro de la fila actual (convertida a la vista por si se ordena la tabla).
            int modelRow = table.convertRowIndexToModel(row);
            String codigoLibro = (String) table.getModel().getValueAt(modelRow, 0);

            // 3. Comprueba si el usuario actual tiene este libro prestado.
            boolean esLibroDelUsuario = false;
            if (usuarioLogueado != null && usuarioLogueado.tieneLibroPrestado()) {
                if (usuarioLogueado.getPrestamoActual().getCodigoLibro().equals(codigoLibro)) {
                    esLibroDelUsuario = true;
                }
            }

            // 4. Establece el color de fondo basado en el estado y la selección.
            if (esLibroDelUsuario) {
                // Si es el libro del usuario, píntalo de verde claro, incluso si está seleccionado.
                cellComponent.setBackground(new Color(144, 238, 144)); // Verde claro
                cellComponent.setForeground(Color.BLACK); // Texto en negro para legibilidad.
            } else {
                // Si no es el libro del usuario, usa los colores por defecto.
                if (isSelected) {
                    cellComponent.setBackground(table.getSelectionBackground());
                    cellComponent.setForeground(table.getSelectionForeground());
                } else {
                    cellComponent.setBackground(table.getBackground());
                    cellComponent.setForeground(table.getForeground());
                }
            }
            
            return cellComponent;
        }
    }

    // --- Método Main: El punto de entrada de la aplicación ---
    public static void main(String[] args) {
        // SwingUtilities.invokeLater es una forma segura de iniciar aplicaciones Swing.
        // Asegura que todo el código de la interfaz gráfica se ejecute en el hilo correcto (Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> {
            BibliotecaGUI ex = new BibliotecaGUI(); // Crea una instancia de nuestra ventana.
            ex.setVisible(true);                  // La hace visible.
        });
    }
}