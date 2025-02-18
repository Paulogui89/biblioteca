package org.paulohenrique.controller;

import org.paulohenrique.enums.TipoArquivo;
import org.paulohenrique.model.Livro;
import org.paulohenrique.service.LivroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/livros")
public class LivroController {

    @Autowired
    private LivroService livroService;

    @PostMapping
    public ResponseEntity<Livro> adicionarLivro(@RequestBody Livro livro) {
        Livro novoLivro = livroService.adicionarLivro(livro);
        return ResponseEntity.ok(novoLivro);
    }

    @PostMapping("/lista")
    public ResponseEntity<List<Livro>> adicionarLivros(@RequestBody List<Livro> livros) {
        List<Livro> novosLivros = livroService.adicionarLivros(livros);
        return ResponseEntity.ok(novosLivros);
    }

    @GetMapping
    public ResponseEntity<List<Livro>> listarLivros() {
        List<Livro> livros = livroService.listarLivros();
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> consultarLivro(@PathVariable Long id) {
        Optional<Livro> livro = livroService.consultarLivro(id);
        if (livro.isPresent()) {
            return ResponseEntity.ok(livro.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(@PathVariable Long id, @RequestBody Livro livro) {
        Livro livroAtualizado = livroService.atualizarLivro(id, livro);
        if (livroAtualizado != null) {
            return ResponseEntity.ok(livroAtualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerLivro(@PathVariable Long id) {
        livroService.removerLivro(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportarLivros(
            @RequestParam String tipo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Boolean disponivel) throws IOException {

        TipoArquivo tipoArquivo;
        try {
            tipoArquivo = TipoArquivo.fromString(tipo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .body(e.getMessage().getBytes());
        }

        ByteArrayInputStream byteArrayInputStream;
        String contentType;
        String fileExtension;

        switch (tipoArquivo) {
            case EXCEL:
                byteArrayInputStream = livroService.exportarLivrosParaExcel(autor, ano, disponivel);
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                fileExtension = ".xlsx";
                break;
            case TXT:
                byteArrayInputStream = livroService.exportarLivrosParaTxt(autor, ano, disponivel);
                contentType = "text/plain";
                fileExtension = ".txt";
                break;
            case CSV:
                byteArrayInputStream = livroService.exportarLivrosParaCsv(autor, ano, disponivel);
                contentType = "text/csv";
                fileExtension = ".csv";
                break;
            default:
                return ResponseEntity.badRequest()
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                        .body(("Tipo de arquivo inválido: " + tipo + ". Tipos disponíveis: " + TipoArquivo.getTiposDisponiveis()).getBytes());
        }

        byte[] bytes = byteArrayInputStream.readAllBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=livros" + fileExtension);
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @PostMapping("/importar/adicionar")
    public ResponseEntity<?> importarLivros(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            List<Livro> livros = livroService.importarLivrosDeArquivo(file);
            return ResponseEntity.ok(livros);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/importar/atualizar")
    public ResponseEntity<?> atualizarLivros(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            List<Livro> livrosAtualizados = livroService.atualizarLivrosDeArquivo(file);
            return ResponseEntity.ok(livrosAtualizados);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
