<?php
// Datos de conexión a la base de datos
$servername = "localhost";
$username = "root";
$password = "b8K43423";
$dbname = "JuegoDados";

// Crear conexión
$conn = new mysqli($servername, $username, $password, $dbname);

// Verificar conexión
if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

// Obtener datos del formulario
$nombre = $_POST['nombre'];
$correo = $_POST['correo'];
$contraseña = password_hash($_POST['contraseña'], PASSWORD_BCRYPT); // Cifrar contraseña

// Insertar datos en la tabla Usuarios
$sql = "INSERT INTO Usuarios (Nombre, Correo, Contraseña) VALUES ('$nombre', '$correo', '$contraseña')";

if ($conn->query($sql) === TRUE) {
    echo "Datos guardados correctamente";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

// Cerrar conexión
$conn->close();
?>
