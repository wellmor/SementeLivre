package com.sementelivre.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sementelivre.backend.exception.UploadFotoException;

@Service
public class FotoProdutoService {

    private static final String PASTA_UPLOAD = "uploads/produtos";

    public String salvar(MultipartFile arquivo) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new UploadFotoException("Arquivo de foto é obrigatório");
        }

        String contentType = arquivo.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
           throw new UploadFotoException("O arquivo enviado deve ser uma imagem");
        }

        try {
            Path pasta = Paths.get(PASTA_UPLOAD);

            Files.createDirectories(pasta);

            String nomeOriginal = arquivo.getOriginalFilename();

            String extensao = "";

            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }

            String nomeArquivo = UUID.randomUUID() + extensao;

            Path destino = pasta.resolve(nomeArquivo);

            arquivo.transferTo(destino.toFile());

            return "/uploads/produtos/" + nomeArquivo;

        } catch (IOException e) {
           throw new UploadFotoException("Não foi possível salvar a foto do produto", e);
        }
    }
}