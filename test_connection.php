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
} else {
    echo "Conexión exitosa!";
}

// Cerrar conexión
$conn->close();
?>
