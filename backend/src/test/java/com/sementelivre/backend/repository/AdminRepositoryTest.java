package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Admin;
import com.sementelivre.backend.entity.enums.NivelAcesso;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    @Test
    public void deveSalvarEBuscarAdmin() {
        Admin admin = new Admin();
        admin.setNome("Admin Rep Test");
        admin.setDocumento("99988877766");
        admin.setTipoDocumento(TipoDocumento.CPF);
        admin.setEmail("adminrep@teste.com");
        admin.setSenhaHash("hash123");
        admin.setNivelAcesso(NivelAcesso.SUPER_ADMIN);

        Admin salvo = adminRepository.save(admin);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNivelAcesso()).isEqualTo(NivelAcesso.SUPER_ADMIN);

        var doBanco = adminRepository.findById(salvo.getId());
        assertThat(doBanco).isPresent();
        assertThat(doBanco.get().getNivelAcesso()).isEqualTo(NivelAcesso.SUPER_ADMIN);
        assertThat(doBanco.get().getEmail()).isEqualTo("adminrep@teste.com");
    }
}
