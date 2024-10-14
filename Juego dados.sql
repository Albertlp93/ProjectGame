CREATE DATABASE JuegoDados;
USE JuegoDados;

CREATE TABLE Usuarios (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    Nombre VARCHAR(50) NOT NULL,
    Correo VARCHAR(100) NOT NULL UNIQUE,
    Contraseña VARCHAR(255) NOT NULL,
    Fecha_Registro DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Juegos (
    ID_Juego INT PRIMARY KEY AUTO_INCREMENT,
    ID_Usuario INT,
    Fecha_Juego DATETIME DEFAULT CURRENT_TIMESTAMP,
    Numero_Apostado INT,
    Resultado VARCHAR(10),
    Dados VARCHAR(5),
    FOREIGN KEY (ID_Usuario) REFERENCES Usuarios(ID)
);

-- Insertar datos de prueba
INSERT INTO Usuarios (Nombre, Correo, Contraseña) VALUES ('Marc Campuzano', 'mcampuzano@uoc.edu', '12345678');
INSERT INTO Juegos (ID_Usuario, Numero_Apostado, Resultado, Dados) VALUES (1, 7, 'ganado', '3,4');

-- Consultar datos
SELECT * FROM Juegos WHERE ID_Usuario = 1;

-- Crear índice
CREATE INDEX idx_usuario ON Juegos(ID_Usuario);

-- Ejemplos de Insertar
INSERT INTO Usuarios (Nombre, Correo, Contraseña) VALUES ('Marc Campuzano', 'mcampuzano@uoc.edu', '12345678');
INSERT INTO Juegos (ID_Usuario, Numero_Apostado, Resultado, Dados) VALUES (2, 5, 'perdido', '2,3');

-- Ejemplos de Actualizar
UPDATE Usuarios SET Correo = 'mcampuzano@uoc.edu' WHERE ID = 2;
UPDATE Juegos SET Resultado = 'ganado', Dados = '5,2' WHERE ID_Juego = 2;

-- Ejemplos de Eliminar
DELETE FROM Usuarios WHERE ID = 2;
DELETE FROM Juegos WHERE ID_Juego = 2;

