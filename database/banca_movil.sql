CREATE DATABASE banca_digital CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE banca_digital;

CREATE TABLE configuracion_sistema (
    id_config INT PRIMARY KEY,
    fecha_minima DATE NOT NULL
);

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    dni CHAR(8) NOT NULL UNIQUE,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    celular CHAR(9) NOT NULL,
    username VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    rol ENUM('ADMIN', 'CLIENTE') NOT NULL,
    estado ENUM('ACTIVO', 'BLOQUEADO') DEFAULT 'ACTIVO'
);

CREATE TABLE cuentas (
    id_cuenta INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    numero_cuenta VARCHAR(16) NOT NULL UNIQUE,
    tipo_cuenta ENUM('AHORRO', 'CORRIENTE') DEFAULT 'AHORRO',
    saldo DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    limite_diario DECIMAL(10, 2) NOT NULL DEFAULT 1500.00,
    estado ENUM('ACTIVA', 'BLOQUEADA') DEFAULT 'ACTIVA',
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE creditos (
    id_credito INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    monto_capital DECIMAL(10, 2) NOT NULL,
    tasa_interes DECIMAL(5, 2) NOT NULL,
    total_cuotas INT NOT NULL,
    fecha_desembolso DATE NOT NULL,
    estado ENUM('ACTIVO', 'PAGADO', 'MORA') DEFAULT 'ACTIVO',
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE cuotas_credito (
    id_cuota INT AUTO_INCREMENT PRIMARY KEY,
    id_credito INT NOT NULL,
    numero_cuota INT NOT NULL,
    monto_capital DECIMAL(10, 2) NOT NULL,
    monto_interes DECIMAL(10, 2) NOT NULL,
    monto_multa DECIMAL(10, 2) DEFAULT 0.00,
    fecha_vencimiento DATE NOT NULL,
    fecha_pago DATE NULL,
    dias_atraso INT DEFAULT 0,
    estado_cuota ENUM('PENDIENTE', 'PAGADA_A_TIEMPO', 'ATRASADA') DEFAULT 'PENDIENTE',
    FOREIGN KEY (id_credito) REFERENCES creditos(id_credito)
);

CREATE VIEW cuotas AS
SELECT 
    id_cuota,
    id_credito,
    numero_cuota,
    (monto_capital + monto_interes + monto_multa) AS monto_cuota,
    fecha_vencimiento,
    fecha_pago,
    dias_atraso,
    CASE 
        WHEN estado_cuota = 'PENDIENTE' THEN 'POR_PAGAR'
        ELSE estado_cuota
    END AS estado
FROM cuotas_credito;

CREATE TABLE transacciones (
    id_transaccion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    detalle VARCHAR(200) NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    fecha_transaccion DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE notificaciones (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_origen INT NOT NULL,
    mensaje VARCHAR(300) NOT NULL,
    fecha_notificacion DATE NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_usuario_origen) REFERENCES usuarios(id_usuario)
);

CREATE TABLE ofertas_credito (
    id_oferta INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    titulo_atractivo VARCHAR(120) DEFAULT 'Crédito Preaprobado Especial',
    monto_ofrecido DECIMAL(10, 2) NOT NULL,
    tasa_interes DECIMAL(5, 2) DEFAULT 9.50,
    cuotas_sugeridas INT DEFAULT 6,
    estado VARCHAR(20) DEFAULT 'PENDIENTE',
    fecha_oferta DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE quejas_sugerencias (
    id_queja INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo ENUM('QUEJA', 'SUGERENCIA') NOT NULL,
    mensaje VARCHAR(300) NOT NULL,
    respuesta_admin VARCHAR(300) DEFAULT 'Pendiente de revisión',
    fecha_registro DATE NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

USE banca_digital;

INSERT INTO configuracion_sistema (id_config, fecha_minima) VALUES (1, '2012-12-11');

INSERT INTO usuarios (dni, nombres, apellidos, celular, username, password, rol) VALUES
('10000001', 'Carlos', 'Mendoza', '999111222', 'admin', 'admin123', 'ADMIN'),
('71234561', 'Ana', 'Torres', '958746123', 'ana', '1234', 'CLIENTE'),
('71234562', 'Luis', 'Gomez', '987654321', 'luis', '1234', 'CLIENTE'),
('71234563', 'Maria', 'Rojas', '912345678', 'maria', '1234', 'CLIENTE'),
('71234564', 'Pedro', 'Castillo', '945612378', 'pedro', '1234', 'CLIENTE'),
('71234565', 'Sofia', 'Vargas', '963258741', 'sofia', '1234', 'CLIENTE');

INSERT INTO cuentas (id_usuario, numero_cuenta, tipo_cuenta, saldo, limite_diario) VALUES
(2, '19100000000001', 'AHORRO', 0.00, 1500.00),
(3, '19100000000002', 'AHORRO', 0.00, 2000.00),
(4, '19100000000003', 'AHORRO', 0.00, 1000.00),
(5, '19100000000004', 'AHORRO', 0.00, 1000.00),
(6, '19100000000005', 'CORRIENTE', 0.00, 5000.00);

INSERT INTO creditos (id_credito, id_usuario, monto_capital, tasa_interes, total_cuotas, fecha_desembolso, estado) VALUES
(1, 2, 1200.00, 10.00, 12, '2012-04-12', 'ACTIVO'),
(2, 3, 600.00, 8.00, 6, '2012-08-05', 'ACTIVO');

INSERT INTO cuotas_credito (id_credito, numero_cuota, monto_capital, monto_interes, monto_multa, fecha_vencimiento, fecha_pago, dias_atraso, estado_cuota) VALUES
(1, 1,  100.00, 10.00, 0.00, '2012-05-12', '2012-05-10', 0, 'PAGADA_A_TIEMPO'),
(1, 2,  100.00, 10.00, 0.00, '2012-06-12', '2012-06-12', 0, 'PAGADA_A_TIEMPO'),
(1, 3,  100.00, 10.00, 10.00,'2012-07-12', '2012-07-13', 1, 'ATRASADA'),
(1, 4,  100.00, 10.00, 0.00, '2012-08-12', '2012-08-11', 0, 'PAGADA_A_TIEMPO'),
(1, 5,  100.00, 10.00, 20.00,'2012-09-12', '2012-09-15', 3, 'ATRASADA'),
(1, 6,  100.00, 10.00, 0.00, '2012-10-12', '2012-10-12', 0, 'PAGADA_A_TIEMPO'),
(1, 7,  100.00, 10.00, 35.00,'2012-11-12', '2012-11-19', 7, 'ATRASADA'),
(1, 8,  100.00, 10.00, 0.00, '2012-12-12', NULL,         0, 'PENDIENTE'),
(1, 9,  100.00, 10.00, 0.00, '2013-01-12', NULL,         0, 'PENDIENTE'),
(1, 10, 100.00, 10.00, 0.00, '2013-02-12', NULL,         0, 'PENDIENTE'),
(1, 11, 100.00, 10.00, 0.00, '2013-03-12', NULL,         0, 'PENDIENTE'),
(1, 12, 100.00, 10.00, 0.00, '2013-04-12', NULL,         0, 'PENDIENTE'),
(2, 1, 100.00, 8.00, 0.00, '2012-09-05', '2012-09-04', 0, 'PAGADA_A_TIEMPO'),
(2, 2, 100.00, 8.00, 0.00, '2012-10-05', '2012-10-05', 0, 'PAGADA_A_TIEMPO'),
(2, 3, 100.00, 8.00, 0.00, '2012-11-05', '2012-11-03', 0, 'PAGADA_A_TIEMPO'),
(2, 4, 100.00, 8.00, 0.00, '2012-12-05', '2012-12-05', 0, 'PAGADA_A_TIEMPO'),
(2, 5, 100.00, 8.00, 0.00, '2013-01-05', NULL,         0, 'PENDIENTE'),
(2, 6, 100.00, 8.00, 0.00, '2013-02-05', NULL,         0, 'PENDIENTE');

USE banca_digital;

INSERT INTO transacciones (id_usuario, tipo, detalle, monto, fecha_transaccion) VALUES
(2, 'DESEMBOLSO', 'Desembolso de Crédito Personal #1', 1200.00, '2012-04-12'),
(2, 'RETIRO', 'Retiro en ventanilla - Compra equipo hogar', 950.00, '2012-04-15'),

-- Mayo 2012 (Ahorro mes: +S/. 295.00 -> "Excelente!!")
(2, 'DEPOSITO', 'Abono de sueldo mensual', 1650.00, '2012-05-02'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 260.00, '2012-05-05'),
(2, 'PAGO_CUOTA', 'Pago Cuota #1 - Crédito #1 (Puntual)', 110.00, '2012-05-10'),
(2, 'RETIRO', 'Retiro semanal - Mercado y despensa', 250.00, '2012-05-13'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Luz Enel', 85.00, '2012-05-18'),
(2, 'RETIRO', 'Retiro semanal - Comida y gastos varios', 240.00, '2012-05-21'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Agua Sedapal', 55.00, '2012-05-24'),
(2, 'RETIRO', 'Retiro semanal - Gastos fin de mes', 255.00, '2012-05-28'),
(2, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000003 - Para Maria Rojas', 100.00, '2012-05-29'),
(4, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000001 - De Ana Torres', 100.00, '2012-05-29'),

-- Junio 2012 (Ahorro mes: +S/. 280.00 -> "Excelente!!")
(2, 'DEPOSITO', 'Abono de sueldo mensual', 1650.00, '2012-06-02'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 260.00, '2012-06-06'),
(2, 'PAGO_CUOTA', 'Pago Cuota #2 - Crédito #1 (Puntual)', 110.00, '2012-06-12'),
(2, 'RETIRO', 'Retiro semanal - Mercado y despensa', 265.00, '2012-06-14'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Internet Fibra', 85.00, '2012-06-19'),
(2, 'RETIRO', 'Retiro semanal - Comida y gastos varios', 270.00, '2012-06-22'),
(2, 'RETIRO', 'Retiro semanal - Gastos fin de mes', 280.00, '2012-06-29'),
(2, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000004 - Para Pedro Castillo', 100.00, '2012-06-30'),
(5, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000001 - De Ana Torres', 100.00, '2012-06-30'),

-- Julio 2012 (Ahorro mes: +S/. 340.00 -> "Excelente!!")
(2, 'DEPOSITO', 'Abono de sueldo + Gratificación parcial', 1950.00, '2012-07-02'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 290.00, '2012-07-06'),
(2, 'PAGO_CUOTA', 'Pago Cuota #3 - Crédito #1 (Mora 1 día)', 120.00, '2012-07-13'),
(2, 'RETIRO', 'Retiro semanal - Mercado y despensa', 310.00, '2012-07-15'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Luz Enel', 90.00, '2012-07-20'),
(2, 'RETIRO', 'Retiro semanal - Comida y salidas', 320.00, '2012-07-23'),
(2, 'RETIRO', 'Retiro semanal - Gastos Fiestas Patrias', 380.00, '2012-07-28'),

-- Agosto 2012 (Ahorro mes: +S/. 180.00 -> "Vas por buen camino...")
(2, 'DEPOSITO', 'Abono de sueldo mensual', 1650.00, '2012-08-02'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 270.00, '2012-08-05'),
(2, 'PAGO_CUOTA', 'Pago Cuota #4 - Crédito #1 (Puntual)', 110.00, '2012-08-11'),
(2, 'RETIRO', 'Retiro semanal - Mercado y despensa', 280.00, '2012-08-14'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Agua y Luz', 140.00, '2012-08-20'),
(2, 'RETIRO', 'Retiro semanal - Comida y gastos varios', 340.00, '2012-08-23'),
(2, 'RETIRO', 'Retiro semanal - Gastos fin de mes', 330.00, '2012-08-29'),

-- Septiembre 2012 (EMERGENCIA HOSPITAL -> Déficit -S/. 410.00 -> "Tenga cuidado con sus Gastos...")
(2, 'DEPOSITO', 'Abono de sueldo mensual', 1650.00, '2012-09-02'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 250.00, '2012-09-06'),
(2, 'RETIRO', 'Retiro emergencia - Atención clínica y medicinas', 780.00, '2012-09-11'),
(2, 'PAGO_CUOTA', 'Pago Cuota #5 - Crédito #1 (Mora 3 días)', 130.00, '2012-09-15'),
(2, 'RETIRO', 'Retiro semanal - Mercado y dieta médica', 310.00, '2012-09-18'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Luz e Internet', 160.00, '2012-09-22'),
(2, 'RETIRO', 'Retiro semanal - Farmacia y movilidad', 280.00, '2012-09-27'),
(2, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000002 - Para Luis Gomez', 150.00, '2012-09-28'),
(3, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000001 - De Ana Torres', 150.00, '2012-09-28'),

-- Octubre 2012 (Recuperación: +S/. 245.00 -> "Excelente!!")
(2, 'DEPOSITO', 'Abono de sueldo mensual', 1650.00, '2012-10-02'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 250.00, '2012-10-06'),
(2, 'PAGO_CUOTA', 'Pago Cuota #6 - Crédito #1 (Puntual)', 110.00, '2012-10-12'),
(2, 'RETIRO', 'Retiro semanal - Mercado y despensa', 255.00, '2012-10-15'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Luz Enel', 85.00, '2012-10-20'),
(2, 'RETIRO', 'Retiro semanal - Comida y gastos varios', 260.00, '2012-10-23'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Agua y Telefonía', 95.00, '2012-10-26'),
(2, 'RETIRO', 'Retiro semanal - Gastos fin de mes', 250.00, '2012-10-30'),
(2, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000003 - Para Maria Rojas', 100.00, '2012-10-30'),
(4, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000001 - De Ana Torres', 100.00, '2012-10-30'),

-- Noviembre 2012 (Ahorro mes: +S/. 225.00 -> "Excelente!!")
(2, 'DEPOSITO', 'Abono de sueldo mensual', 1650.00, '2012-11-02'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 260.00, '2012-11-05'),
(2, 'RETIRO', 'Retiro semanal - Mercado y despensa', 265.00, '2012-11-12'),
(2, 'PAGO_CUOTA', 'Pago Cuota #7 - Crédito #1 (Mora 7 días)', 145.00, '2012-11-19'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Internet Fibra', 85.00, '2012-11-21'),
(2, 'RETIRO', 'Retiro semanal - Comida y gastos varios', 270.00, '2012-11-23'),
(2, 'RETIRO', 'Retiro semanal - Gastos fin de mes', 280.00, '2012-11-29'),
(2, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000004 - Para Pedro Castillo', 120.00, '2012-11-30'),
(5, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000001 - De Ana Torres', 120.00, '2012-11-30'),

-- Diciembre 2012 (Ahorro mes: +S/. 310.00 -> "Excelente!!")
(2, 'DEPOSITO', 'Abono de sueldo mensual', 1650.00, '2012-12-03'),
(2, 'RETIRO', 'Retiro semanal - Comida y movilidad', 270.00, '2012-12-05'),
(2, 'PAGO_SERVICIO', 'Pago de servicio - Luz y Agua', 130.00, '2012-12-07'),
(2, 'RETIRO', 'Retiro semanal - Mercado y gastos varios', 290.00, '2012-12-09'),
(2, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000005 - De Sofia Vargas', 150.00, '2012-12-10'),
(6, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000001 - Para Ana Torres', 150.00, '2012-12-10'),
(2, 'RETIRO', 'Retiro semanal - Compras y movilidad', 280.00, '2012-12-11'),
(2, 'RETIRO', 'Retiro en agente - Gastos personales', 220.00, '2012-12-11');

USE banca_digital;

INSERT INTO transacciones (id_usuario, tipo, detalle, monto, fecha_transaccion) VALUES
-- =========================================================================
-- LUIS GOMEZ (id_usuario = 3) - Sueldo S/. 1,800 + Ordenado (Ahorro ~S/. 170 - 390/mes)
-- =========================================================================
(3, 'DESEMBOLSO', 'Desembolso de Crédito Personal #2', 600.00, '2012-08-05'),
(3, 'RETIRO', 'Retiro - Compra herramientas de trabajo', 520.00, '2012-08-10'),

-- Septiembre 2012
(3, 'DEPOSITO', 'Abono de sueldo mensual', 1800.00, '2012-09-01'),
(3, 'PAGO_CUOTA', 'Pago Cuota #1 - Crédito #2 (Puntual)', 108.00, '2012-09-04'),
(3, 'RETIRO', 'Retiro quincenal - Comida y pasajes', 480.00, '2012-09-08'),
(3, 'PAGO_SERVICIO', 'Pago de servicio - Luz e Internet', 152.00, '2012-09-18'),
(3, 'RETIRO', 'Retiro quincenal - Mercado y gastos', 510.00, '2012-09-22'),
(3, 'RETIRO', 'Retiro fin de mes - Gastos personales', 320.00, '2012-09-29'),

-- Octubre 2012
(3, 'DEPOSITO', 'Abono de sueldo mensual', 1800.00, '2012-10-01'),
(3, 'PAGO_CUOTA', 'Pago Cuota #2 - Crédito #2 (Puntual)', 108.00, '2012-10-05'),
(3, 'RETIRO', 'Retiro quincenal - Comida y pasajes', 490.00, '2012-10-09'),
(3, 'PAGO_SERVICIO', 'Pago de servicio - Agua y Telefonía', 112.00, '2012-10-17'),
(3, 'RETIRO', 'Retiro quincenal - Mercado y gastos', 500.00, '2012-10-23'),
(3, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000003 - Para Maria Rojas', 120.00, '2012-10-25'),
(4, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000002 - De Luis Gomez', 120.00, '2012-10-25'),
(3, 'RETIRO', 'Retiro fin de mes - Gastos varios', 190.00, '2012-10-30'),

-- Noviembre 2012
(3, 'DEPOSITO', 'Abono de sueldo mensual', 1800.00, '2012-11-01'),
(3, 'PAGO_CUOTA', 'Pago Cuota #3 - Crédito #2 (Puntual)', 108.00, '2012-11-03'),
(3, 'RETIRO', 'Retiro quincenal - Comida y pasajes', 470.00, '2012-11-08'),
(3, 'PAGO_SERVICIO', 'Pago de servicio - Luz e Internet', 155.00, '2012-11-19'),
(3, 'RETIRO', 'Retiro quincenal - Mercado y gastos', 520.00, '2012-11-24'),
(3, 'RETIRO', 'Retiro fin de mes - Gastos personales', 240.00, '2012-11-29'),

-- Diciembre 2012 (Ganancia moderada: +S/. 172.00 -> "Vas por buen camino...")
(3, 'DEPOSITO', 'Abono de sueldo mensual', 1800.00, '2012-12-01'),
(3, 'PAGO_CUOTA', 'Pago Cuota #4 - Crédito #2 (Puntual)', 108.00, '2012-12-05'),
(3, 'RETIRO', 'Retiro quincenal - Comida y pasajes', 480.00, '2012-12-06'),
(3, 'PAGO_SERVICIO', 'Pago de servicio - Luz, Agua e Internet', 170.00, '2012-12-08'),
(3, 'RETIRO', 'Retiro quincenal - Mercado y gastos', 540.00, '2012-12-10'),
(3, 'TRANSFERENCIA_ENVIADA', 'Transferencia a cuenta 19100000000004 - Para Pedro Castillo', 150.00, '2012-12-11'),
(5, 'TRANSFERENCIA_RECIBIDA', 'Transferencia de cuenta 19100000000002 - De Luis Gomez', 150.00, '2012-12-11'),
(3, 'RETIRO', 'Retiro en agente - Gastos personales', 180.00, '2012-12-11'),

-- =========================================================================
-- MARIA ROJAS (id_usuario = 4) - Sueldo S/. 1,250 + En 12/2012 termina en Déficit (-S/. 140)
-- =========================================================================
(4, 'DEPOSITO', 'Abono de sueldo mensual', 1250.00, '2012-11-02'),
(4, 'RETIRO', 'Retiro semanal - Comida y movilidad', 260.00, '2012-11-06'),
(4, 'RETIRO', 'Retiro semanal - Mercado y hogar', 270.00, '2012-11-14'),
(4, 'PAGO_SERVICIO', 'Pago de servicio - Luz y Telefonía', 110.00, '2012-11-20'),
(4, 'RETIRO', 'Retiro semanal - Comida y gastos', 250.00, '2012-11-24'),
(4, 'RETIRO', 'Retiro semanal - Gastos fin de mes', 210.00, '2012-11-29'),

-- Diciembre 2012 (Gasto médico imprevisto -> Déficit -S/. 140.00)
(4, 'DEPOSITO', 'Abono de sueldo mensual', 1250.00, '2012-12-03'),
(4, 'RETIRO', 'Retiro semanal - Comida y movilidad', 270.00, '2012-12-05'),
(4, 'RETIRO', 'Retiro emergencia - Consulta médica y análisis', 420.00, '2012-12-07'),
(4, 'PAGO_SERVICIO', 'Pago de servicio - Luz, Agua y Móvil', 140.00, '2012-12-08'),
(4, 'RETIRO', 'Retiro semanal - Mercado y despensa', 280.00, '2012-12-10'),
(4, 'RETIRO', 'Retiro semanal - Movilidad y gastos', 280.00, '2012-12-11'),

-- =========================================================================
-- PEDRO CASTILLO (id_usuario = 5) - Sueldo S/. 1,100 (Vive ajustado)
-- =========================================================================
(5, 'DEPOSITO', 'Abono de sueldo mensual', 1100.00, '2012-12-02'),
(5, 'RETIRO', 'Retiro quincenal - Comida y pasajes', 460.00, '2012-12-05'),
(5, 'PAGO_SERVICIO', 'Pago de servicio - Luz y Móvil', 95.00, '2012-12-08'),
(5, 'RETIRO', 'Retiro quincenal - Mercado y gastos', 480.00, '2012-12-11'),

-- =========================================================================
-- SOFIA VARGAS (id_usuario = 6) - PERFIL CEO / EJECUTIVO (Montos altos)
-- =========================================================================
(6, 'DEPOSITO', 'Abono honorarios Dirección Ejecutiva (CEO)', 8500.00, '2012-11-01'),
(6, 'RETIRO', 'Retiro ejecutivo - Gastos operativos y viajes', 2200.00, '2012-11-12'),
(6, 'PAGO_SERVICIO', 'Pago de servicio - Plan Corporativo y Fibra', 450.00, '2012-11-20'),
(6, 'RETIRO', 'Retiro ejecutivo - Gastos personales mes', 1850.00, '2012-11-28'),

(6, 'DEPOSITO', 'Abono honorarios Dirección Ejecutiva (CEO)', 8500.00, '2012-12-01'),
(6, 'RETIRO', 'Retiro ejecutivo - Gastos operativos y representación', 2400.00, '2012-12-06'),
(6, 'PAGO_SERVICIO', 'Pago de servicio - Plan Corporativo y Residencial', 480.00, '2012-12-09'),
(6, 'RETIRO', 'Retiro ejecutivo - Compras fin de año', 1900.00, '2012-12-11');

-- =========================================================================
-- SINCRONIZAR SALDOS ACTUALES AL CENTAVO CON TODO EL HISTORIAL
-- =========================================================================
UPDATE cuentas c
SET saldo = (
    SELECT COALESCE(SUM(
        CASE 
            WHEN t.tipo IN ('DEPOSITO', 'TRANSFERENCIA_RECIBIDA', 'DESEMBOLSO') THEN t.monto
            ELSE -t.monto
        END
    ), 0)
    FROM transacciones t
    WHERE t.id_usuario = c.id_usuario
)
WHERE c.id_cuenta > 0;


USE banca_digital;

INSERT INTO ofertas_credito (id_usuario, titulo_atractivo, monto_ofrecido, tasa_interes, cuotas_sugeridas, estado, fecha_oferta) VALUES
(4, '¡Felicidades! Tienes un Crédito Instantáneo Preaprobado', 900.00, 9.50, 6, 'PENDIENTE', '2012-12-11');

INSERT INTO notificaciones (id_usuario_origen, mensaje, fecha_notificacion, leida) VALUES
(2, '[ALERTA QUEJA] Cliente Ana Torres reportó lentitud en transferencias nocturnas.', '2012-12-10', FALSE),
(4, 'Cliente Maria Rojas consultó simulación de crédito preaprobado.', '2012-12-11', TRUE);

INSERT INTO quejas_sugerencias (id_usuario, tipo, mensaje, respuesta_admin, fecha_registro) VALUES
(2, 'QUEJA', 'El día de ayer hubo una caída del servicio durante 20 minutos al intentar hacer una transferencia.', 'Pendiente de revisión', '2012-12-09'),
(3, 'QUEJA', 'Noto lentitud al cargar el historial de mis cuotas desde el celular.', 'Pendiente de revisión', '2012-12-10'),
(4, 'SUGERENCIA', 'Sería excelente recibir alertas automáticas cuando falte 1 día para el vencimiento de una cuota.', '¡Implementado! Ya activamos las alertas automáticas en tu panel de notificaciones.', '2012-12-08'),
(5, 'QUEJA', 'Intenté pagar mi cuota tarde en la noche y el sistema me arrojó error de conexión temporal.', 'Pendiente de revisión', '2012-12-11'),
(6, 'SUGERENCIA', 'Me gustaría poder personalizar mi límite máximo de transferencia diaria por seguridad.', 'Pendiente de revisión', '2012-12-11');

USE banca_digital;

CREATE TABLE IF NOT EXISTS movimientos_notificaciones (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo ENUM('TRANSFERENCIA_ENVIADA', 'TRANSFERENCIA_RECIBIDA', 'PAGO_SERVICIO', 'CREDITO_SOLICITADO', 'PAGO_CUOTA', 'ALERTA_SEGURIDAD', 'DEPOSITO', 'RETIRO') NOT NULL,
    detalle VARCHAR(200) NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    fecha_operacion DATE NOT NULL,
    visto_admin BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

INSERT INTO movimientos_notificaciones (id_usuario, tipo, detalle, monto, fecha_operacion, visto_admin)
SELECT 
    id_usuario,
    CASE 
        WHEN tipo IN ('TRANSFERENCIA_ENVIADA', 'TRANSFERENCIA_RECIBIDA', 'PAGO_SERVICIO', 'CREDITO_SOLICITADO', 'PAGO_CUOTA', 'DEPOSITO', 'RETIRO') THEN tipo
        WHEN tipo = 'DESEMBOLSO' THEN 'CREDITO_SOLICITADO'
        ELSE 'ALERTA_SEGURIDAD'
    END,
    detalle,
    monto,
    fecha_transaccion,
    TRUE
FROM transacciones;

DELIMITER $$

CREATE TRIGGER trg_sincronizar_a_transacciones
AFTER INSERT ON movimientos_notificaciones
FOR EACH ROW
BEGIN
    INSERT INTO transacciones (id_usuario, tipo, detalle, monto, fecha_transaccion)
    VALUES (NEW.id_usuario, NEW.tipo, NEW.detalle, NEW.monto, NEW.fecha_operacion);
END$$

DELIMITER ;
