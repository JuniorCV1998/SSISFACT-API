DROP PROCEDURE IF EXISTS sp_listar_sunat_mensajes;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_sunat_mensajes`(
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
        mensaje,
        remitente,
        fecha_publicacion,
        leido,
        tiene_adjunto,
        COUNT(*) OVER() AS total_count
    FROM sunat_mensajes
    WHERE empresa_id = p_empresa_id
    ORDER BY CAST(sunat_id AS UNSIGNED) DESC
    LIMIT p_tamanio OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_sunat_mensajes
    Descripción:
        Pagina el histórico de mensajes SUNAT ya sincronizado en BD (no llama
        al bot). Misma estrategia de total_count que sp_listar_sunat_notificaciones.
    ===============================================================================
    */

END$$

DELIMITER ;
