package pe.ssimple.ssisfact_api.repository.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyConfirmRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterRequest;
import pe.ssimple.ssisfact_api.dto.RegisterCompany.CompanyRegisterResponse;
import pe.ssimple.ssisfact_api.repository.CompanyRegisterRepository;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CompanyRegisterRepositoryImpl implements CompanyRegisterRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public CompanyRegisterResponse registrarEmpresa(CompanyRegisterRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_crear_o_actualizar_registro_proceso_empresa(?,?,?,?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> new CompanyRegisterResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getInt("id")),
                request.getIdRegisterProcess(),
                request.getRuc(),
                request.getRazonSocial(),
                request.getDireccion(),
                request.getTelefono(),
                request.getEmailEmpresa(),
                request.getDocumento(),
                request.getNombreAdmin(),
                request.getEmailAdmin(),
                request.getContrasena(),
                0);
    }

    public CompanyRegisterResponse confirmarEmpresa(CompanyConfirmRequest request) {

        return jdbcTemplate.queryForObject(
                "CALL sp_crear_o_actualizar_registro_proceso_empresa(?,?,?,?,?,?,?,?,?,?,?)",
                (rs, rowNum) -> new CompanyRegisterResponse(
                        rs.getString("estado"),
                        rs.getString("mensaje"),
                        rs.getInt("id")),
                request.getIdRegisterProcess(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1);
    }
}