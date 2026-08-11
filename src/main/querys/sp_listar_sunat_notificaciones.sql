DROP PROCEDURE IF EXISTS sp_listar_sunat_notificaciones;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_sunat_notificaciones`(
    IN p_empresa_id BIGINT,
    IN p_pagina INT,
    IN p_tamanio INT
)
BEGIN

    DECLARE v_offset INT;
    SET v_offset = GREATEST(p_pagina, 0) * p_tamanio;

    SELECT
        sunat_id,
        asunto,
        fecha_publicacion,
        categoria_codigo,
        categoria_descripcion,
        leido,
        destacado,
        urgente,
        tiene_adjunto,
        COUNT(*) OVER() AS total_count
    FROM sunat_notificaciones
    WHERE empresa_id = p_empresa_id
    ORDER BY CAST(sunat_id AS UNSIGNED) DESC
    LIMIT p_tamanio OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_sunat_notificaciones
    Descripción:
        Pagina el histórico de notificaciones SUNAT ya sincronizado en BD
        (no llama al bot). total_count viene vía window function; si la página
        pedida no tiene filas, el backend hace un COUNT aparte para saber el total.
    ===============================================================================
    */

END$$

DELIMITER ;
