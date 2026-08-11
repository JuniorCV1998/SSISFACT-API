DROP PROCEDURE IF EXISTS sp_eliminar_categoria_definitivamente;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_eliminar_categoria_definitivamente`(
    IN p_categoria_id BIGINT,
    IN p_empresa_id   BIGINT
)
BEGIN

    DECLARE v_estado_actual    TINYINT;
    DECLARE v_productos_vivos  INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error al eliminar la categoría' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    -- ============================================================
    -- VALIDAR QUE EXISTE Y PERTENECE A LA EMPRESA
    -- ============================================================

    SELECT estado
    INTO v_estado_actual
    FROM categorias
    WHERE id = p_categoria_id
    AND empresa_id = p_empresa_id
    LIMIT 1;

    IF v_estado_actual IS NULL THEN
        ROLLBACK;
        SELECT 'ERROR_NO_EXISTE' AS estado, 'La categoría no existe' AS mensaje, 0 AS id;

    ELSEIF v_estado_actual = -1 THEN
        ROLLBACK;
        SELECT 'ERROR_YA_ELIMINADA' AS estado, 'La categoría ya fue eliminada definitivamente' AS mensaje, p_categoria_id AS id;

    ELSE

        -- ============================================================
        -- VALIDAR QUE NO TIENE PRODUCTOS ACTIVOS NI INACTIVOS
        -- Solo se permite eliminar si todos sus productos están en estado=-1
        -- o si no tiene ningún producto asociado
        -- ============================================================

        SELECT COUNT(*)
        INTO v_productos_vivos
        FROM productos
        WHERE categoria_id = p_categoria_id
        AND estado IN (0, 1);

        IF v_productos_vivos > 0 THEN
            ROLLBACK;
            SELECT
                'ERROR_PRODUCTOS_EXISTENTES' AS estado,
                CONCAT('No se puede eliminar. La categoría tiene ', v_productos_vivos,
                       ' producto(s) activo(s) o inactivo(s). Elimínalos definitivamente primero.') AS mensaje,
                p_categoria_id AS id;

        ELSE

            -- Renombra la categoría para liberar el nombre original y evitar
            -- conflictos con el UNIQUE(empresa_id, nombre) al eliminar
            UPDATE categorias
            SET nombre = CONCAT(nombre, '__DEL_', p_categoria_id),
                estado = -1,
                fecha_actualizacion = NOW()
            WHERE id = p_categoria_id AND empresa_id = p_empresa_id;

            COMMIT;

            SELECT
                'OK' AS estado,
                'Categoría eliminada definitivamente' AS mensaje,
                p_categoria_id AS id;

        END IF;

    END IF;

    /*
    ===============================================================================
    Nombre:
        sp_eliminar_categoria_definitivamente
    Autor:
        Jose Cerron
    Descripción:
        Elimina una categoría definitivamente (soft delete, estado = -1).
        Solo se permite si todos sus productos están en estado = -1
        o si no tiene productos asociados.
    Estados posibles:
        OK
        ERROR
        ERROR_NO_EXISTE
        ERROR_YA_ELIMINADA
        ERROR_PRODUCTOS_EXISTENTES
    Estados de categoría:
        1  = Activa
        0  = Inactiva
        -1 = Eliminada definitivamente
    ===============================================================================
    */

END$$

DELIMITER ;
