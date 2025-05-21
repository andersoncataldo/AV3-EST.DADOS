import java.io.*;
import java.util.*;

public class Main {
    private static ListaEncadeada lista = new ListaEncadeada();
    private static TabelaHash hash = new TabelaHash();

    public static void main(String[] args) {
        try {
            System.out.println("Diretório atual: " + new File(".").getAbsolutePath());
            System.out.println("Lendo arquivo de palavras-chave...");
            List<String> palavrasChave = lerPalavrasChave("C:\\Users\\ander\\Downloads\\projetoAV3\\palavras-chave.txt");
            System.out.println("Arquivo de palavras-chave com sucesso!");

            System.out.println("Processando texto...");
            processarTexto("C:\\Users\\ander\\Downloads\\projetoAV3\\texto.txt");
            System.out.println("Texto processado com sucesso!");

            System.out.println("Gerando arquivo de índice remissivo...");
            gerarArquivoIndice("C:\\Users\\ander\\Downloads\\projetoAV3\\indice-remissivo.txt", palavrasChave);

            System.out.println("Índice remissivo gerado com sucesso em 'indice-remissivo.txt'.");
        } catch (FileNotFoundException e) {
            System.err.println("Erro: Arquivo não encontrado. " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Erro ao ler ou escrever arquivo: " + e.getMessage());
        }
    }

    public static List<String> lerPalavrasChave(String arquivo) throws IOException {
        List<String> palavras = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(arquivo));
        String linha;
        while ((linha = reader.readLine()) != null) {
            String[] tokens = linha.toLowerCase().replaceAll("[^a-z0-9, \\-]", "").split("[,\\s]+");
            for (String palavra : tokens) {
                if (!palavra.isEmpty() && !palavras.contains(palavra)) {
                    palavras.add(palavra);
                }
            }
        }
        reader.close();
        return palavras;
    }

    public static void processarTexto(String arquivoTexto) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(arquivoTexto));
        String linhaTexto;
        int linha = 1;

        while ((linhaTexto = reader.readLine()) != null) {
            String[] palavras = linhaTexto.toLowerCase().replaceAll("[^a-z0-9 \\-]", "").split("\\s+");
            for (String palavraStr : palavras) {
                if (palavraStr.isEmpty()) continue;
                Palavra palavra = hash.buscar(palavraStr);
                if (palavra == null) {
                    palavra = new Palavra(palavraStr);
                    palavra.adicionarOcorrencia(linha);
                    lista.inserir(palavra);
                    hash.inserir(palavra);
                } else {
                    palavra.adicionarOcorrencia(linha);
                }
            }
            linha++;
        }
        reader.close();
    }

    public static void gerarArquivoIndice(String nomeArquivo, List<String> palavrasChave) throws IOException {
        Collections.sort(palavrasChave);
        BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo));

        for (String chave : palavrasChave) {
            String chaveLimpa = chave.toLowerCase().replaceAll("[^a-z0-9\\-]", "");
            Palavra p = hash.buscar(chaveLimpa);
            if (p != null) {
                writer.write(chave);
                System.out.print(chave);
                for (int linha : p.getOcorrenciasLista()) {
                    writer.write(" " + linha);
                    System.out.print(" " + linha);
                }
                writer.newLine();
                System.out.println();
            } else {
                writer.write(chave + ": [não encontrada]\n");
                System.out.println(chave + ": [não encontrada]");
            }
        }
        writer.close();
    }
}

class Palavra {
    private String texto;
    private ListaOcorrencias ocorrencias;

    public Palavra(String texto) {
        this.texto = texto;
        this.ocorrencias = new ListaOcorrencias();
    }

    public void adicionarOcorrencia(int linha) {
        ocorrencias.adicionar(linha);
    }

    public String getTexto() {
        return texto;
    }

    public List<Integer> getOcorrenciasLista() {
        return ocorrencias.toList();
    }
}

class ListaOcorrencias {
    private Nodo inicio;

    private class Nodo {
        int linha;
        Nodo proximo;

        Nodo(int linha) {
            this.linha = linha;
            this.proximo = null;
        }
    }

    public void adicionar(int linha) {
        if (!contem(linha)) {
            Nodo novo = new Nodo(linha);
            novo.proximo = inicio;
            inicio = novo;
        }
    }

    private boolean contem(int linha) {
        Nodo atual = inicio;
        while (atual != null) {
            if (atual.linha == linha) return true;
            atual = atual.proximo;
        }
        return false;
    }

    public List<Integer> toList() {
        List<Integer> lista = new ArrayList<>();
        Nodo atual = inicio;
        while (atual != null) {
            lista.add(atual.linha);
            atual = atual.proximo;
        }
        Collections.sort(lista);
        return lista;
    }
}

class ListaEncadeada {
    private No head;

    private class No {
        Palavra palavra;
        No proximo;

        No(Palavra palavra) {
            this.palavra = palavra;
        }
    }

    public void inserir(Palavra palavra) {
        No novo = new No(palavra);
        novo.proximo = head;
        head = novo;
    }
}

class TabelaHash {
    private Map<Character, ArvoreBinaria> tabela;

    public TabelaHash() {
        tabela = new HashMap<>();
    }

    public void inserir(Palavra palavra) {
        char chave = palavra.getTexto().charAt(0);
        tabela.putIfAbsent(chave, new ArvoreBinaria());
        tabela.get(chave).inserir(palavra);
    }

    public Palavra buscar(String palavra) {
        if (palavra.isEmpty()) return null;
        char chave = palavra.charAt(0);
        ArvoreBinaria abb = tabela.get(chave);
        if (abb == null) return null;
        return abb.buscar(palavra);
    }
}

class ArvoreBinaria {
    private No raiz;

    private class No {
        Palavra palavra;
        No esquerda, direita;

        No(Palavra palavra) {
            this.palavra = palavra;
        }
    }

    public void inserir(Palavra palavra) {
        raiz = inserirRec(raiz, palavra);
    }

    private No inserirRec(No atual, Palavra palavra) {
        if (atual == null) return new No(palavra);
        int cmp = palavra.getTexto().compareTo(atual.palavra.getTexto());
        if (cmp < 0) atual.esquerda = inserirRec(atual.esquerda, palavra);
        else if (cmp > 0) atual.direita = inserirRec(atual.direita, palavra);
        return atual;
    }

    public Palavra buscar(String texto) {
        return buscarRec(raiz, texto);
    }

    private Palavra buscarRec(No atual, String texto) {
        if (atual == null) return null;
        int cmp = texto.compareTo(atual.palavra.getTexto());
        if (cmp == 0) return atual.palavra;
        else if (cmp < 0) return buscarRec(atual.esquerda, texto);
        else return buscarRec(atual.direita, texto);
    }
}