DROP PROCEDURE IF EXISTS sp_upsert_sunat_mensaje;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_upsert_sunat_mensaje`(
    IN p_empresa_id BIGINT,
    IN p_ruc VARCHAR(20),
    IN p_sunat_id VARCHAR(50),
    IN p_asunto VARCHAR(1000),
    IN p_mensaje VARCHAR(4000),
    IN p_remitente VARCHAR(500),
    IN p_fecha_publicacion VARCHAR(100),
    IN p_leido TINYINT(1),
    IN p_tiene_adjunto TINYINT(1)
)
BEGIN

    DECLARE v_id BIGINT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'ERROR' AS estado, 'Error en el proceso' AS mensaje, 0 AS id;
    END;

    START TRANSACTION;

    INSERT INTO sunat_mensajes (
        empresa_id, ruc, sunat_id, asunto, mensaje, remitente, fecha_publicacion,
        leido, tiene_adjunto, fecha_ultima_sincronizacion
    ) VALUES (
        p_empresa_id, p_ruc, p_sunat_id, p_asunto, p_mensaje, p_remitente, p_fecha_publicacion,
        p_leido, p_tiene_adjunto, NOW()
    )
    ON DUPLICATE KEY UPDATE
        asunto = p_asunto,
        mensaje = p_mensaje,
        remitente = p_remitente,
        fecha_publicacion = p_fecha_publicacion,
        leido = p_leido,
        tiene_adjunto = p_tiene_adjunto,
        fecha_ultima_sincronizacion = NOW(),
        id = LAST_INSERT_ID(id);

    SET v_id = LAST_INSERT_ID();
    COMMIT;
    SELECT 'OK' AS estado, 'Mensaje sincronizado' AS mensaje, v_id AS id;

    /*
    ===============================================================================
    Nombre:
        sp_upsert_sunat_mensaje
    Descripción:
        Inserta o actualiza (empresa_id + sunat_id) un mensaje SUNAT.
        No borra histórico ante una respuesta vacía del bot.
    ===============================================================================
    */

END$$

DELIMITER ;
