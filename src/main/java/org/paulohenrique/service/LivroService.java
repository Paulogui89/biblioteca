package org.paulohenrique.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.paulohenrique.model.Livro;
import org.paulohenrique.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LivroService {

    @Autowired
    private LivroRepository livroRepository;

    public Livro adicionarLivro(Livro livro) {
        livro.setDisponivel(true);
        return livroRepository.save(livro);
    }

    public List<Livro> adicionarLivros(List<Livro> livros) {
        livros.forEach(livro -> livro.setDisponivel(true));
        return livroRepository.saveAll(livros);
    }

    public List<Livro> listarLivros() {
        return livroRepository.findAll();
    }

    public Optional<Livro> consultarLivro(Long id) {
        return livroRepository.findById(id);
    }

    public Livro atualizarLivro(Long id, Livro livro) {
        Optional<Livro> livroExistente = livroRepository.findById(id);
        if (livroExistente.isPresent()) {
            Livro livroAtualizado = livroExistente.get();
            livroAtualizado.setTitulo(livro.getTitulo());
            livroAtualizado.setAutor(livro.getAutor());
            livroAtualizado.setAno(livro.getAno());
            livroAtualizado.setDisponivel(livro.isDisponivel());
            return livroRepository.save(livroAtualizado);
        } else {
            return null;
        }
    }

    public void removerLivro(Long id) {
        livroRepository.deleteById(id);
    }



    public List<Livro> filtrarLivros(String autor, Integer ano, Boolean disponivel) {
        if (autor == null && ano == null && disponivel == null) {
            return listarLivros();
        } else {
            return livroRepository.findLivrosByAutorAndAnoAndDisponivel(autor, ano, disponivel);
        }
    }

    public ByteArrayInputStream exportarLivrosParaExcel(String autor, Integer ano, Boolean disponivel) throws IOException {
        List<Livro> livros = filtrarLivros(autor, ano, disponivel);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Livros");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Título");
        headerRow.createCell(2).setCellValue("Autor");
        headerRow.createCell(3).setCellValue("Ano");
        headerRow.createCell(4).setCellValue("Disponível");

        int rowNum = 1;
        for (Livro livro : livros) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(livro.getId());
            row.createCell(1).setCellValue(livro.getTitulo());
            row.createCell(2).setCellValue(livro.getAutor());
            row.createCell(3).setCellValue(livro.getAno());
            row.createCell(4).setCellValue(livro.isDisponivel());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream exportarLivrosParaTxt(String autor, Integer ano, Boolean disponivel) {
        List<Livro> livros = filtrarLivros(autor, ano, disponivel);
        StringBuilder sb = new StringBuilder();

        sb.append("ID\tTítulo\tAutor\tAno\tDisponível\n");
        for (Livro livro : livros) {
            sb.append(livro.getId()).append("\t")
                    .append(livro.getTitulo()).append("\t")
                    .append(livro.getAutor()).append("\t")
                    .append(livro.getAno()).append("\t")
                    .append(livro.isDisponivel()).append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    public ByteArrayInputStream exportarLivrosParaCsv(String autor, Integer ano, Boolean disponivel) {
        List<Livro> livros = filtrarLivros(autor, ano, disponivel);
        StringBuilder sb = new StringBuilder();

        sb.append("ID,Título,Autor,Ano,Disponível\n");
        for (Livro livro : livros) {
            sb.append(livro.getId()).append(",")
                    .append(livro.getTitulo()).append(",")
                    .append(livro.getAutor()).append(",")
                    .append(livro.getAno()).append(",")
                    .append(livro.isDisponivel()).append("\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }



    public List<Livro> importarLivrosDeArquivo(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename.endsWith(".csv")) {
            return importarLivrosDeCsv(file);
        } else if (filename.endsWith(".txt")) {
            return importarLivrosDeTxt(file);
        } else if (filename.endsWith(".xlsx")) {
            return importarLivrosDeExcel(file);
        }
        throw new IllegalArgumentException("Tipo de arquivo não suportado: " + filename);
    }

    public List<Livro> atualizarLivrosDeArquivo(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename.endsWith(".csv")) {
            return atualizarLivrosDeCsv(file);
        } else if (filename.endsWith(".txt")) {
            return atualizarLivrosDeTxt(file);
        } else if (filename.endsWith(".xlsx")) {
            return atualizarLivrosDeExcel(file);
        }
        throw new IllegalArgumentException("Tipo de arquivo não suportado: " + filename);
    }


    private void validarCabecalho(String[] cabecalho, String[] esperado) {
        if (cabecalho.length < esperado.length) {
            throw new IllegalArgumentException("Cabeçalho inválido. Esperado: " + String.join(", ", esperado));
        }
        for (int i = 0; i < esperado.length; i++) {
            if (!cabecalho[i].trim().equalsIgnoreCase(esperado[i])) {
                throw new IllegalArgumentException("Cabeçalho inválido. Esperado: " + String.join(", ", esperado));
            }
        }
    }

    private List<Livro> importarLivrosDeCsv(MultipartFile file) throws IOException {
        List<Livro> livros = new ArrayList<>();
        String[] cabecalhoEsperado = {"Título", "Autor", "Ano", "Disponível"};
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line != null) {
                validarCabecalho(line.split(","), cabecalhoEsperado);
            }
            while ((line = br.readLine()) != null) {
                String[] valores = line.split(",");
                Livro livro = new Livro();
                livro.setTitulo(valores[0]);
                livro.setAutor(valores[1]);
                livro.setAno(Integer.parseInt(valores[2]));
                livro.setDisponivel(Boolean.parseBoolean(valores[3]));
                livros.add(livro);
            }
        }
        return livroRepository.saveAll(livros);
    }

    private List<Livro> atualizarLivrosDeCsv(MultipartFile file) throws IOException {
        List<Livro> livrosAtualizados = new ArrayList<>();
        String[] cabecalhoEsperado = {"ID", "Título", "Autor", "Ano", "Disponível"};
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line != null) {
                validarCabecalho(line.split(","), cabecalhoEsperado);
            }
            while ((line = br.readLine()) != null) {
                String[] valores = line.split(",");
                Long id = Long.parseLong(valores[0]);
                Optional<Livro> livroExistente = livroRepository.findById(id);
                if (livroExistente.isPresent()) {
                    Livro livro = livroExistente.get();
                    livro.setTitulo(valores[1]);
                    livro.setAutor(valores[2]);
                    livro.setAno(Integer.parseInt(valores[3]));
                    livro.setDisponivel(Boolean.parseBoolean(valores[4]));
                    livrosAtualizados.add(livroRepository.save(livro));
                }
            }
        }
        return livrosAtualizados;
    }

    private List<Livro> importarLivrosDeTxt(MultipartFile file) throws IOException {
        List<Livro> livros = new ArrayList<>();
        String[] cabecalhoEsperado = {"Título", "Autor", "Ano", "Disponível"};
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line != null) {
                validarCabecalho(line.split("\t"), cabecalhoEsperado);
            }
            while ((line = br.readLine()) != null) {
                String[] valores = line.split("\t");
                Livro livro = new Livro();
                livro.setTitulo(valores[0]);
                livro.setAutor(valores[1]);
                livro.setAno(Integer.parseInt(valores[2]));
                livro.setDisponivel(Boolean.parseBoolean(valores[3]));
                livros.add(livro);
            }
        }
        return livroRepository.saveAll(livros);
    }

    private List<Livro> atualizarLivrosDeTxt(MultipartFile file) throws IOException {
        List<Livro> livrosAtualizados = new ArrayList<>();
        String[] cabecalhoEsperado = {"ID", "Título", "Autor", "Ano", "Disponível"};
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line != null) {
                validarCabecalho(line.split("\t"), cabecalhoEsperado);
            }
            while ((line = br.readLine()) != null) {
                String[] valores = line.split("\t");
                Long id = Long.parseLong(valores[0]);
                Optional<Livro> livroExistente = livroRepository.findById(id);
                if (livroExistente.isPresent()) {
                    Livro livro = livroExistente.get();
                    livro.setTitulo(valores[1]);
                    livro.setAutor(valores[2]);
                    livro.setAno(Integer.parseInt(valores[3]));
                    livro.setDisponivel(Boolean.parseBoolean(valores[4]));
                    livrosAtualizados.add(livroRepository.save(livro));
                }
            }
        }
        return livrosAtualizados;
    }

    private List<Livro> importarLivrosDeExcel(MultipartFile file) throws IOException {
        List<Livro> livros = new ArrayList<>();
        String[] cabecalhoEsperado = {"Título", "Autor", "Ano", "Disponível"};
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(0);
        validarCabecalho(new String[]{
                getCellValue(headerRow, 0),
                getCellValue(headerRow, 1),
                getCellValue(headerRow, 2),
                getCellValue(headerRow, 3)
        }, cabecalhoEsperado);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) { // Skip header row
                continue;
            }
            Livro livro = new Livro();
            livro.setTitulo(getCellValue(row, 0));
            livro.setAutor(getCellValue(row, 1));
            livro.setAno((int) row.getCell(2).getNumericCellValue());
            livro.setDisponivel(row.getCell(3).getBooleanCellValue());
            livros.add(livro);
        }
        workbook.close();
        return livroRepository.saveAll(livros);
    }

    private List<Livro> atualizarLivrosDeExcel(MultipartFile file) throws IOException {
        List<Livro> livrosAtualizados = new ArrayList<>();
        String[] cabecalhoEsperado = {"ID", "Título", "Autor", "Ano", "Disponível"};
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(0);
        validarCabecalho(new String[]{
                getCellValue(headerRow, 0),
                getCellValue(headerRow, 1),
                getCellValue(headerRow, 2),
                getCellValue(headerRow, 3),
                getCellValue(headerRow, 4)
        }, cabecalhoEsperado);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) { // Skip header row
                continue;
            }
            Long id = (long) row.getCell(0).getNumericCellValue();
            Optional<Livro> livroExistente = livroRepository.findById(id);
            if (livroExistente.isPresent()) {
                Livro livro = livroExistente.get();
                livro.setTitulo(getCellValue(row, 1));
                livro.setAutor(getCellValue(row, 2));
                livro.setAno((int) row.getCell(3).getNumericCellValue());
                livro.setDisponivel(row.getCell(4).getBooleanCellValue());
                livrosAtualizados.add(livroRepository.save(livro));
            }
        }
        workbook.close();
        return livrosAtualizados;
    }

    private String getCellValue(Row row, int cellIndex) {
        return row.getCell(cellIndex) != null ? row.getCell(cellIndex).getStringCellValue() : "";
    }
}
