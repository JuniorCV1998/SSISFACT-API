DROP PROCEDURE IF EXISTS sp_crear_o_actualizar_categoria;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_crear_o_actualizar_categoria`(
    IN p_id_categoria BIGINT,
    IN p_empresa_id   BIGINT,
    IN p_nombre       VARCHAR(150),
    IN p_descripcion  VARCHAR(255)
)
BEGIN

    DECLARE v_categoria_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error en el proceso' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR EMPRESA
    -- ============================================================

    IF p_empresa_id IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_EMPRESA' AS estado, 'Debe indicar una empresa válida' AS mensaje, 0 AS id;

    ELSE

        -- ============================================================
        -- CREAR
        -- ============================================================

        IF p_id_categoria IS NULL THEN

            -- Excluye estado=-1: los nombres de categorías eliminadas quedan libres
            IF EXISTS (
                SELECT 1 FROM categorias
                WHERE empresa_id = p_empresa_id
                AND UPPER(TRIM(nombre)) = UPPER(TRIM(p_nombre))
                AND estado <> -1
            ) THEN
                ROLLBACK;
                SELECT 'ERROR_CATEGORIA_EXISTE' AS estado, 'La categoría ya existe' AS mensaje, 0 AS id;

            ELSE
                INSERT INTO categorias (empresa_id, nombre, descripcion, estado, fecha_creacion)
                VALUES (p_empresa_id, TRIM(p_nombre), p_descripcion, 1, NOW());

                SET v_categoria_id = LAST_INSERT_ID();
                COMMIT;
                SELECT 'OK' AS estado, CONCAT('Categoría ', p_nombre, ' creada correctamente') AS mensaje, v_categoria_id AS id;
            END IF;

        ELSE

            -- ============================================================
            -- ACTUALIZAR
            -- ============================================================

            IF NOT EXISTS (
                SELECT 1 FROM categorias
                WHERE id = p_id_categoria AND empresa_id = p_empresa_id
            ) THEN
                ROLLBACK;
                SELECT 'ERROR_CATEGORIA' AS estado, 'La categoría no existe' AS mensaje, 0 AS id;

            ELSE
                -- Excluye estado=-1 y excluye la propia categoría que se está editando
                IF EXISTS (
                    SELECT 1 FROM categorias
                    WHERE empresa_id = p_empresa_id
                    AND UPPER(TRIM(nombre)) = UPPER(TRIM(p_nombre))
                    AND id <> p_id_categoria
                    AND estado <> -1
                ) THEN
                    ROLLBACK;
                    SELECT 'ERROR_CATEGORIA_EXISTE' AS estado, 'La categoría ya existe' AS mensaje, 0 AS id;

                ELSE
                    UPDATE categorias
                    SET nombre = TRIM(p_nombre),
                        descripcion = p_descripcion,
                        fecha_actualizacion = NOW()
                    WHERE id = p_id_categoria;

                    COMMIT;
                    SELECT 'OK' AS estado, CONCAT('Categoría ', p_nombre, ' actualizada correctamente') AS mensaje, p_id_categoria AS id;
                END IF;

            END IF;

        END IF;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_crear_o_actualizar_categoria
    Autor:
        Jose Cerron
    Descripción:
        Registra o actualiza una categoría.
        La validación de nombre duplicado excluye registros con estado=-1
        (eliminados definitivamente), permitiendo reutilizar nombres liberados.
    Estados posibles:
        OK, ERROR, ERROR_EMPRESA, ERROR_CATEGORIA, ERROR_CATEGORIA_EXISTE
    ===============================================================================
    */

END$$

DELIMITER ;
