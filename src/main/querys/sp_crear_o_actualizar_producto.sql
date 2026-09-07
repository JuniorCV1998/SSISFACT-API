DROP PROCEDURE IF EXISTS sp_crear_o_actualizar_producto;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_o_actualizar_producto`(
    IN p_id_producto BIGINT,
    IN p_empresa_id BIGINT,
    IN p_categoria_nombre VARCHAR(150),
    IN p_codigo VARCHAR(50),
    IN p_codigo_barras VARCHAR(100),
    IN p_nombre VARCHAR(150),
    IN p_descripcion TEXT,
    IN p_precio DECIMAL(10,2),
    IN p_costo DECIMAL(10,2),
    IN p_stock_minimo INT,
    IN p_afecto_impuesto TINYINT,
    IN p_imagen_url VARCHAR(255)
)
sp_crear_o_actualizar_producto: BEGIN

DECLARE v_producto_id BIGINT;
DECLARE v_categoria_id BIGINT;
DECLARE v_prefijo VARCHAR(4);
DECLARE v_siguiente INT;

-- ============================================================
-- HANDLERS - Mapear errores SQL a mensajes de negocio
-- ============================================================

DECLARE EXIT HANDLER FOR 1062
BEGIN
    ROLLBACK;
    SELECT
        'ERROR_DUPLICADO' AS estado,
        'Ya existe un producto con esos datos en esta empresa' AS mensaje,
        0 AS id;
END;

DECLARE EXIT HANDLER FOR 1452
BEGIN
    ROLLBACK;
    SELECT
        'ERROR_REFERENCIA' AS estado,
        'La categoría o empresa especificada no existe' AS mensaje,
        0 AS id;
END;

DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
    ROLLBACK;
    SELECT
        'ERROR' AS estado,
        'Error al procesar la solicitud. Intenta nuevamente' AS mensaje,
        0 AS id;
END;

START TRANSACTION;

-- ============================================================
-- VALIDAR EMPRESA
-- ============================================================

IF p_empresa_id IS NULL THEN
    ROLLBACK;
    SELECT
        'ERROR_EMPRESA' AS estado,
        'Empresa inválida' AS mensaje,
        0 AS id;
    LEAVE sp_crear_o_actualizar_producto;
END IF;

-- Validar que la empresa existe
IF NOT EXISTS (
    SELECT 1
    FROM empresas
    WHERE id = p_empresa_id
) THEN
    ROLLBACK;
    SELECT
        'ERROR_EMPRESA_NO_EXISTE' AS estado,
        'La empresa especificada no existe' AS mensaje,
        0 AS id;
    LEAVE sp_crear_o_actualizar_producto;
END IF;

-- ============================================================
-- BUSCAR O CREAR CATEGORIA
-- ============================================================

SELECT id
INTO v_categoria_id
FROM categorias
WHERE empresa_id = p_empresa_id
AND UPPER(TRIM(nombre)) = UPPER(TRIM(p_categoria_nombre))
LIMIT 1;

IF v_categoria_id IS NULL THEN
    INSERT INTO categorias(
        empresa_id,
        nombre,
        descripcion,
        estado,
        fecha_creacion
    )
    VALUES(
        p_empresa_id,
        TRIM(p_categoria_nombre),
        CONCAT('Categoría ', TRIM(p_categoria_nombre)),
        1,
        NOW()
    );

    SET v_categoria_id = LAST_INSERT_ID();
END IF;

-- Prefijo de 4 letras derivado del nombre de categoría (solo A-Z, sin
-- tildes/espacios/números), relleno con 'X' si la categoría es muy corta.
-- Ej: "Clavos" -> CLAV, "TV" -> TVXX.
SET v_prefijo = RPAD(
    UPPER(LEFT(REGEXP_REPLACE(p_categoria_nombre, '[^A-Za-z]', ''), 4)),
    4, 'X'
);

-- ============================================================
-- CREAR PRODUCTO
-- ============================================================

IF p_id_producto IS NULL THEN

    -- Si no hay código interno pero sí código de barras, se deriva de ahí.
    IF (p_codigo IS NULL OR TRIM(p_codigo) = '')
    AND p_codigo_barras IS NOT NULL AND TRIM(p_codigo_barras) <> '' THEN
        SET p_codigo = CONCAT('AUTO-', p_codigo_barras);
    END IF;

    -- Sin código interno ni de barras: se genera uno propio, tipo
    -- 'CLAV0000001' — prefijo de la categoría + secuencia. La secuencia se
    -- calcula sobre códigos que ya usan ese mismo prefijo en la empresa
    -- (no solo dentro de la categoría), para garantizar que nunca choque
    -- con otro código si dos categorías distintas comparten prefijo.
    IF p_codigo IS NULL OR TRIM(p_codigo) = '' THEN
        SELECT COALESCE(MAX(CAST(SUBSTRING(codigo, 5) AS UNSIGNED)), 0) + 1
        INTO v_siguiente
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND codigo REGEXP CONCAT('^', v_prefijo, '[0-9]{7}$');

        SET p_codigo = CONCAT(v_prefijo, LPAD(v_siguiente, 7, '0'));
    END IF;

    -- Validar nombre no vacío
    IF p_nombre IS NULL OR TRIM(p_nombre) = '' THEN
        ROLLBACK;
        SELECT
            'ERROR_NOMBRE_VACIO' AS estado,
            'El nombre del producto es requerido' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar categoría no vacía
    IF p_categoria_nombre IS NULL OR TRIM(p_categoria_nombre) = '' THEN
        ROLLBACK;
        SELECT
            'ERROR_CATEGORIA_VACIA' AS estado,
            'La categoría es requerida' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar código duplicado (EXCLUIR ELIMINADOS). A esta altura p_codigo
    -- ya tiene un valor siempre (explícito, derivado o autogenerado).
    IF EXISTS (
        SELECT 1
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND codigo = p_codigo
        AND estado <> -1  -- No contar eliminados
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_CODIGO_EXISTE' AS estado,
            'El código interno ya existe en esta empresa' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar código de barras duplicado (EXCLUIR ELIMINADOS)
    IF p_codigo_barras IS NOT NULL
    AND TRIM(p_codigo_barras) <> ''
    AND EXISTS (
        SELECT 1
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND codigo_barras = p_codigo_barras
        AND estado <> -1  -- No contar eliminados
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_BARRAS_EXISTE' AS estado,
            'El código de barras ya existe en esta empresa' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar nombre duplicado (EXCLUIR ELIMINADOS)
    IF EXISTS (
        SELECT 1
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND UPPER(TRIM(nombre)) = UPPER(TRIM(p_nombre))
        AND estado <> -1  -- No contar eliminados
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_NOMBRE_EXISTE' AS estado,
            'Ya existe un producto con ese nombre en esta empresa' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar precio positivo
    IF p_precio IS NOT NULL AND p_precio <= 0 THEN
        ROLLBACK;
        SELECT
            'ERROR_PRECIO' AS estado,
            'El precio debe ser mayor a 0' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar costo positivo
    IF p_costo IS NOT NULL AND p_costo <= 0 THEN
        ROLLBACK;
        SELECT
            'ERROR_COSTO' AS estado,
            'El costo debe ser mayor a 0' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar stock_minimo no negativo
    IF p_stock_minimo IS NOT NULL AND p_stock_minimo < 0 THEN
        ROLLBACK;
        SELECT
            'ERROR_STOCK' AS estado,
            'El stock mínimo no puede ser negativo' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar afecto_impuesto (0 o 1)
    IF p_afecto_impuesto IS NOT NULL
    AND p_afecto_impuesto NOT IN (0, 1) THEN
        ROLLBACK;
        SELECT
            'ERROR_IMPUESTO' AS estado,
            'Afecto impuesto debe ser 0 (no afecto) o 1 (afecto IGV)' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Insertar nuevo producto (codigo puede quedar NULL por ahora)
    INSERT INTO productos(
        empresa_id,
        categoria_id,
        codigo,
        codigo_barras,
        nombre,
        descripcion,
        precio,
        costo,
        stock_minimo,
        afecto_impuesto,
        imagen_url,
        estado,
        fecha_creacion
    )
    VALUES(
        p_empresa_id,
        v_categoria_id,
        p_codigo,
        IF(p_codigo_barras = '', NULL, p_codigo_barras),
        p_nombre,
        p_descripcion,
        p_precio,
        p_costo,
        p_stock_minimo,
        p_afecto_impuesto,
        p_imagen_url,
        1,
        NOW()
    );

    SET v_producto_id = LAST_INSERT_ID();

    COMMIT;

    SELECT
        'OK' AS estado,
        CONCAT('Producto registrado correctamente') AS mensaje,
        v_producto_id AS id;

ELSE

    -- ============================================================
    -- ACTUALIZAR PRODUCTO
    -- ============================================================

    -- Validar que el producto existe
    IF NOT EXISTS (
        SELECT 1
        FROM productos
        WHERE id = p_id_producto
        AND empresa_id = p_empresa_id
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_PRODUCTO_NO_EXISTE' AS estado,
            'Producto no encontrado' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Mismo criterio que al crear: código explícito > derivado del código de
    -- barras > autogenerado con el prefijo de la categoría (esta vez la que
    -- se está asignando en esta edición, no la que tenía antes).
    IF (p_codigo IS NULL OR TRIM(p_codigo) = '')
    AND p_codigo_barras IS NOT NULL AND TRIM(p_codigo_barras) <> '' THEN
        SET p_codigo = CONCAT('AUTO-', p_codigo_barras);
    END IF;

    IF p_codigo IS NULL OR TRIM(p_codigo) = '' THEN
        SELECT COALESCE(MAX(CAST(SUBSTRING(codigo, 5) AS UNSIGNED)), 0) + 1
        INTO v_siguiente
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND codigo REGEXP CONCAT('^', v_prefijo, '[0-9]{7}$')
        AND id <> p_id_producto;

        SET p_codigo = CONCAT(v_prefijo, LPAD(v_siguiente, 7, '0'));
    END IF;

    -- Validar código duplicado (diferente al actual, EXCLUIR ELIMINADOS)
    IF EXISTS (
        SELECT 1
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND codigo = p_codigo
        AND id <> p_id_producto
        AND estado <> -1  -- No contar eliminados
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_CODIGO_EXISTE' AS estado,
            'El código interno ya existe en esta empresa' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar código de barras duplicado (diferente al actual, EXCLUIR ELIMINADOS)
    IF p_codigo_barras IS NOT NULL
    AND TRIM(p_codigo_barras) <> ''
    AND EXISTS (
        SELECT 1
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND codigo_barras = p_codigo_barras
        AND id <> p_id_producto
        AND estado <> -1  -- No contar eliminados
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_BARRAS_EXISTE' AS estado,
            'El código de barras ya existe en esta empresa' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Validar nombre duplicado (diferente al actual, EXCLUIR ELIMINADOS)
    IF EXISTS (
        SELECT 1
        FROM productos
        WHERE empresa_id = p_empresa_id
        AND UPPER(TRIM(nombre)) = UPPER(TRIM(p_nombre))
        AND id <> p_id_producto
        AND estado <> -1  -- No contar eliminados
    ) THEN
        ROLLBACK;
        SELECT
            'ERROR_NOMBRE_EXISTE' AS estado,
            'Ya existe un producto con ese nombre en esta empresa' AS mensaje,
            0 AS id;
        LEAVE sp_crear_o_actualizar_producto;
    END IF;

    -- Actualizar producto
    UPDATE productos
    SET
        categoria_id = v_categoria_id,
        codigo = p_codigo,
        codigo_barras = IF(p_codigo_barras = '', NULL, p_codigo_barras),
        nombre = p_nombre,
        descripcion = p_descripcion,
        precio = p_precio,
        costo = p_costo,
        stock_minimo = p_stock_minimo,
        afecto_impuesto = p_afecto_impuesto,
        imagen_url = p_imagen_url,
        fecha_actualizacion = NOW()
    WHERE id = p_id_producto;

    COMMIT;

    SELECT
        'OK' AS estado,
        CONCAT('Producto actualizado correctamente') AS mensaje,
        p_id_producto AS id;

END IF;

/*
===============================================================================
Nombre:
    sp_crear_o_actualizar_producto
Descripción:
    Crea o actualiza un producto. La categoría se busca por nombre dentro de
    la empresa y se crea automáticamente si no existe.

    Código interno (p_codigo):
        - Si se manda, se usa tal cual (validado como único por empresa).
        - Si no se manda pero hay código de barras, se deriva como
          'AUTO-<codigo_barras>'.
        - Si no se manda ninguno de los dos, el backend genera uno propio:
          4 letras del nombre de categoría + secuencia de 7 dígitos, ej.
          'CLAV0000001' para la categoría "Clavos" (relleno con 'X' si el
          nombre de categoría tiene menos de 4 letras, ej. "TV" -> TVXX).
          La secuencia se calcula por prefijo dentro de la empresa (si dos
          categorías distintas comparten prefijo, comparten también la
          numeración, para que nunca choquen entre sí). El front ya no
          necesita enviar código interno en absoluto.
Estados posibles:
    OK, ERROR, ERROR_EMPRESA, ERROR_EMPRESA_NO_EXISTE, ERROR_NOMBRE_VACIO,
    ERROR_CATEGORIA_VACIA, ERROR_CODIGO_EXISTE, ERROR_BARRAS_EXISTE,
    ERROR_NOMBRE_EXISTE, ERROR_PRECIO, ERROR_COSTO, ERROR_STOCK,
    ERROR_IMPUESTO, ERROR_PRODUCTO_NO_EXISTE, ERROR_DUPLICADO, ERROR_REFERENCIA
===============================================================================
*/

END sp_crear_o_actualizar_producto$$

DELIMITER ;
