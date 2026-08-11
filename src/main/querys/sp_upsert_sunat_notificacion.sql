DROP PROCEDURE IF EXISTS sp_upsert_sunat_notificacion;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_upsert_sunat_notificacion`(
    IN p_empresa_id BIGINT,
    IN p_ruc VARCHAR(20),
    IN p_sunat_id VARCHAR(50),
    IN p_asunto VARCHAR(1000),
    IN p_fecha_publicacion VARCHAR(100),
    IN p_categoria_codigo VARCHAR(50),
    IN p_leido TINYINT(1),
    IN p_destacado TINYINT(1),
    IN p_urgente TINYINT(1),
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

    INSERT INTO sunat_notificaciones (
        empresa_id, ruc, sunat_id, asunto, fecha_publicacion, categoria_codigo,
        leido, destacado, urgente, tiene_adjunto, fecha_ultima_sincronizacion
    ) VALUES (
        p_empresa_id, p_ruc, p_sunat_id, p_asunto, p_fecha_publicacion, p_categoria_codigo,
        p_leido, p_destacado, p_urgente, p_tiene_adjunto, NOW()
    )
    ON DUPLICATE KEY UPDATE
        asunto = p_asunto,
        fecha_publicacion = p_fecha_publicacion,
        categoria_codigo = p_categoria_codigo,
        leido = p_leido,
        destacado = p_destacado,
        urgente = p_urgente,
        tiene_adjunto = p_tiene_adjunto,
        fecha_ultima_sincronizacion = NOW(),
        id = LAST_INSERT_ID(id);

    SET v_id = LAST_INSERT_ID();
    COMMIT;
    SELECT 'OK' AS estado, 'Notificación sincronizada' AS mensaje, v_id AS id;

    /*
    ===============================================================================
    Nombre:
        sp_upsert_sunat_notificacion
    Descripción:
        Inserta o actualiza (empresa_id + sunat_id) una notificación SUNAT.
        No borra histórico: una respuesta vacía del bot simplemente no invoca
        este SP para esa página, las notificaciones previas permanecen.
    ===============================================================================
    */

END$$

DELIMITER ;
