package com.example.demo.repository;

import com.example.demo.model.Proprietario;
import com.example.demo.model.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProprietarioRepositoryTest {

    @Autowired
    private ProprietarioRepository proprietarioRepository;

    @Test
    public void deveSalvarEBuscarProprietario() {
        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento("12345678901");
        proprietario.setNome("Proprietario Rep Test");
        proprietario.setEmail("proprep@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-999999");
        proprietario.setExibirNoSitePublico(true);

        Proprietario salvo = proprietarioRepository.save(proprietario);
        
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getRg()).isEqualTo("MG-999999");

        var doBanco = proprietarioRepository.findById(salvo.getId());
        assertThat(doBanco).isPresent();
        assertThat(doBanco.get().getRg()).isEqualTo("MG-999999");
        assertThat(doBanco.get().getEmail()).isEqualTo("proprep@teste.com");
    }
}
