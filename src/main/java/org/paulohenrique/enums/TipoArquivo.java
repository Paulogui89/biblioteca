package org.paulohenrique.enums;

public enum TipoArquivo {
    EXCEL("excel"),
    TXT("txt"),
    CSV("csv");

    private final String tipo;

    TipoArquivo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public static String getTiposDisponiveis() {
        StringBuilder sb = new StringBuilder();
        for (TipoArquivo tipo : values()) {
            sb.append(tipo.tipo).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    public static TipoArquivo fromString(String tipo) {
        for (TipoArquivo t : TipoArquivo.values()) {
            if (t.getTipo().equalsIgnoreCase(tipo)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Tipo de arquivo inválido: " + tipo + ". Tipos disponíveis: " + getTiposDisponiveis());
    }
}
