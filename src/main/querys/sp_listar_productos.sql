DROP PROCEDURE IF EXISTS sp_listar_productos;

DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_listar_productos`(
    IN p_empresa_id BIGINT,
    IN p_busqueda VARCHAR(150),
    IN p_page INT,
    IN p_size INT,
    IN p_estado TINYINT,
    IN p_mostrar_costo TINYINT,
    IN p_sucursal_id BIGINT
)
sp_listar_productos: BEGIN

    DECLARE v_offset INT;

    -- ============================================================
    -- VALIDAR PAGINACION
    -- ============================================================

    IF p_page IS NULL OR p_page < 1 THEN
        SET p_page = 1;
    END IF;

    IF p_size IS NULL OR p_size < 1 THEN
        SET p_size = 20;
    END IF;

    SET v_offset = (p_page - 1) * p_size;

    -- ============================================================
    -- LISTAR PRODUCTOS (Excluye eliminados estado = -1)
    -- ============================================================

    SELECT
        p.id,
        p.empresa_id,
        p.categoria_id,
        c.nombre AS categoria_nombre,
        p.codigo,
        p.codigo_barras,
        p.nombre,
        p.descripcion,
        p.precio,
        CASE WHEN p_mostrar_costo = 1 THEN p.costo ELSE NULL END AS costo,
        p.stock_minimo,
        p.afecto_impuesto,
        -- Siempre la suma de TODAS las sucursales, sin importar el filtro.
        COALESCE((SELECT SUM(i.stock) FROM inventarios i WHERE i.producto_id = p.id), 0) AS stock_total,
        -- Solo se calcula si se manda p_sucursal_id; NULL si no se filtra.
        CASE WHEN p_sucursal_id IS NULL THEN NULL
             ELSE COALESCE((SELECT SUM(i2.stock) FROM inventarios i2
                             WHERE i2.producto_id = p.id AND i2.sucursal_id = p_sucursal_id), 0)
        END AS stock_sucursal,
        p.imagen_url,
        p.estado,
        p.fecha_creacion,
        p.fecha_actualizacion,
        COUNT(*) OVER() AS total_registros
    FROM productos p
    INNER JOIN categorias c
        ON c.id = p.categoria_id
    WHERE p.empresa_id = p_empresa_id
    AND p.estado <> -1  -- ← EXCLUIR ELIMINADOS
    AND (
        p_estado = -1
        OR p.estado = p_estado
    )
    AND (
        p_busqueda IS NULL
        OR TRIM(p_busqueda) = ''
        OR p.nombre LIKE CONCAT('%', p_busqueda, '%')
        OR p.codigo LIKE CONCAT('%', p_busqueda, '%')
        OR p.codigo_barras LIKE CONCAT('%', p_busqueda, '%')
        OR c.nombre LIKE CONCAT('%', p_busqueda, '%')
    )
    ORDER BY p.nombre ASC
    LIMIT p_size OFFSET v_offset;

    /*
    ===============================================================================
    Nombre:
        sp_listar_productos
    Autor:
        Jose Cerron
    Descripción:
        Lista productos de una empresa con búsqueda, paginación,
        filtro por estado y control de visibilidad del costo.

        IMPORTANTE: Excluye productos eliminados (estado = -1)
    Parámetros:
        p_empresa_id
            Empresa propietaria.
        p_busqueda
            Busca por:
                - Nombre
                - Código interno
                - Código de barras
                - Categoría
        p_page
            Número de página (default: 1).
        p_size
            Cantidad de registros por página (default: 20).
        p_estado
            1  = Solo activos
            0  = Solo inactivos
           -1  = Activos + inactivos (pero NO eliminados)
        p_mostrar_costo
            1  = Incluye el costo real (admin/almacén)
            0  = Costo es NULL (vendedores/clientes)
        p_sucursal_id
            NULL  = stock_sucursal viene NULL, no se filtra nada.
            valor = stock_sucursal trae el stock de ESA sucursal (0 si nunca
                   tuvo movimiento ahí), usado en el Punto de Venta y en el
                   selector de sucursal, sin afectar stock_total.
    Retorna:
        - Datos del producto
        - Categoría
        - stock_total: SIEMPRE la suma de todas las sucursales (no cambia con p_sucursal_id)
        - stock_sucursal: stock de la sucursal filtrada, o NULL si no se filtró
        - Total de registros
    Estados de producto:
        1 = Activo (disponible)
        0 = Inactivo (desactivado)
        -1 = Eliminado (NO aparece en listado)
    Tablas involucradas:
        - productos
        - categorias
        - inventarios
    ===============================================================================
    */

END sp_listar_productos$$

DELIMITER ;
