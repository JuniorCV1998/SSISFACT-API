DROP PROCEDURE IF EXISTS sp_crear_o_actualizar_sucursal;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_o_actualizar_sucursal`(
    IN p_id_sucursal BIGINT,
    IN p_empresa_id  BIGINT,
    IN p_nombre      VARCHAR(150),
    IN p_direccion   VARCHAR(150),
    IN p_telefono    VARCHAR(50),
    IN p_estado      TINYINT
)
sp_crear_o_actualizar_sucursal: BEGIN

    DECLARE v_sucursal_id     BIGINT;
    DECLARE v_total_vivas     INT;
    DECLARE v_max_sucursal    INT;
    DECLARE v_siguiente_serie INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al procesar la sucursal' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR EMPRESA
    -- ============================================================

    IF p_empresa_id IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_EMPRESA' AS estado, 'Empresa inválida' AS mensaje, 0 AS id;
        LEAVE sp_crear_o_actualizar_sucursal;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM empresas WHERE id = p_empresa_id) THEN
        ROLLBACK;
        SELECT 'ERROR_EMPRESA_NO_EXISTE' AS estado, 'La empresa especificada no existe' AS mensaje, 0 AS id;
        LEAVE sp_crear_o_actualizar_sucursal;
    END IF;

    -- ============================================================
    -- VALIDAR PARÁMETROS
    -- ============================================================

    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        ROLLBACK;
        SELECT 'ERROR_NOMBRE_VACIO' AS estado, 'El nombre de la sucursal es requerido' AS mensaje, 0 AS id;
        LEAVE sp_crear_o_actualizar_sucursal;
    END IF;

    IF p_direccion IS NULL OR TRIM(p_direccion) = '' THEN
        ROLLBACK;
        SELECT 'ERROR_DIRECCION_VACIA' AS estado, 'La dirección de la sucursal es requerida' AS mensaje, 0 AS id;
        LEAVE sp_crear_o_actualizar_sucursal;
    END IF;

    IF p_estado IS NOT NULL AND p_estado NOT IN (0, 1) THEN
        ROLLBACK;
        SELECT 'ERROR_ESTADO' AS estado, 'El estado debe ser 0 (inactiva) o 1 (activa)' AS mensaje, 0 AS id;
        LEAVE sp_crear_o_actualizar_sucursal;
    END IF;

    -- ============================================================
    -- CREAR SUCURSAL
    -- ============================================================

    IF p_id_sucursal IS NULL THEN

        -- Obtener límite desde configuración de la empresa (fallback: 10)
        SELECT COALESCE(max_sucursal, 10) INTO v_max_sucursal
        FROM configuracion_empresa
        WHERE empresa_id = p_empresa_id
        LIMIT 1;

        IF v_max_sucursal IS NULL THEN SET v_max_sucursal = 10; END IF;

        -- Validar límite de sucursales (activas + inactivas)
        SELECT COUNT(*) INTO v_total_vivas
        FROM sucursales
        WHERE empresa_id = p_empresa_id AND estado IN (0, 1);

        IF v_total_vivas >= v_max_sucursal THEN
            ROLLBACK;
            SELECT
                'ERROR_LIMITE_SUCURSALES' AS estado,
                CONCAT('La empresa ya tiene el máximo de ', v_max_sucursal, ' sucursales permitidas') AS mensaje,
                0 AS id;
            LEAVE sp_crear_o_actualizar_sucursal;
        END IF;

        -- Validar nombre duplicado (solo entre vivas)
        IF EXISTS (
            SELECT 1 FROM sucursales
            WHERE empresa_id = p_empresa_id
            AND UPPER(TRIM(nombre)) = UPPER(TRIM(p_nombre))
            AND estado IN (0, 1)
        ) THEN
            ROLLBACK;
            SELECT 'ERROR_NOMBRE_EXISTE' AS estado, 'Ya existe una sucursal con ese nombre en esta empresa' AS mensaje, 0 AS id;
            LEAVE sp_crear_o_actualizar_sucursal;
        END IF;

        INSERT INTO sucursales (empresa_id, nombre, direccion, telefono, estado, fecha_creacion)
        VALUES (p_empresa_id, TRIM(p_nombre), TRIM(p_direccion), TRIM(p_telefono), COALESCE(p_estado, 1), NOW());

        SET v_sucursal_id = LAST_INSERT_ID();

        -- Serie NOTA_PEDIDO propia para esta sucursal (NP01, NP02... según
        -- cuántas series de este tipo ya tiene la empresa).
        SELECT COALESCE(MAX(CAST(SUBSTRING(serie, 3) AS UNSIGNED)), 0) + 1
        INTO v_siguiente_serie
        FROM series
        WHERE empresa_id = p_empresa_id AND tipo = 'NOTA_PEDIDO' AND serie REGEXP '^NP[0-9]+$';

        INSERT INTO series (empresa_id, sucursal_id, tipo, serie, correlativo_actual, es_principal, estado)
        VALUES (p_empresa_id, v_sucursal_id, 'NOTA_PEDIDO', CONCAT('NP', LPAD(v_siguiente_serie, 2, '0')), 0, 1, 1);

        COMMIT;

        SELECT 'OK' AS estado, CONCAT('Sucursal ', p_nombre, ' registrada correctamente') AS mensaje, v_sucursal_id AS id;

    ELSE

        -- ============================================================
        -- ACTUALIZAR SUCURSAL
        -- ============================================================

        IF NOT EXISTS (SELECT 1 FROM sucursales WHERE id = p_id_sucursal AND empresa_id = p_empresa_id) THEN
            ROLLBACK;
            SELECT 'ERROR_SUCURSAL_NO_EXISTE' AS estado, 'La sucursal no existe' AS mensaje, 0 AS id;
            LEAVE sp_crear_o_actualizar_sucursal;
        END IF;

        IF EXISTS (
            SELECT 1 FROM sucursales
            WHERE empresa_id = p_empresa_id
            AND UPPER(TRIM(nombre)) = UPPER(TRIM(p_nombre))
            AND id <> p_id_sucursal
        ) THEN
            ROLLBACK;
            SELECT 'ERROR_NOMBRE_EXISTE' AS estado, 'Ya existe una sucursal con ese nombre en esta empresa' AS mensaje, 0 AS id;
            LEAVE sp_crear_o_actualizar_sucursal;
        END IF;

        UPDATE sucursales
        SET nombre = TRIM(p_nombre),
            direccion = TRIM(p_direccion),
            telefono  = TRIM(p_telefono),
            estado    = COALESCE(p_estado, estado),
            fecha_actualizacion = NOW()
        WHERE id = p_id_sucursal;

        COMMIT;

        SELECT 'OK' AS estado, CONCAT('Sucursal ', p_nombre, ' actualizada correctamente') AS mensaje, p_id_sucursal AS id;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_crear_o_actualizar_sucursal
    Autor:
        Jose Cerron
    Límite:
        Máximo 10 sucursales por empresa (activas + inactivas).
    Estados posibles:
        OK, ERROR, ERROR_EMPRESA, ERROR_EMPRESA_NO_EXISTE, ERROR_NOMBRE_VACIO,
        ERROR_DIRECCION_VACIA, ERROR_ESTADO, ERROR_NOMBRE_EXISTE,
        ERROR_SUCURSAL_NO_EXISTE, ERROR_LIMITE_SUCURSALES
    ===============================================================================
    */

END$$

DELIMITER ;
