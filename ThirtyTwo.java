import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class ThirtyTwo {

    
    public static String readFile(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            System.out.println("Can't read file: " + path);
            return "";
        }
    }

    
    public static List<String> partition(String text, int nlines) {
        List<String> chunks = new ArrayList<>();
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i += nlines) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + nlines && j < lines.length; j++) {
                sb.append(lines[j]).append("\n");
            }
            chunks.add(sb.toString());
        }
        return chunks;
    }

    
    public static List<Pair> splitWords(String chunk) {
        List<Pair> pairs = new ArrayList<>();

        String[] words = chunk.replaceAll("[\\W_]+", " ").toLowerCase().split("\\s+");
        Set<String> stopWords = new HashSet<>();
        try {
            String stops = Files.readString(Paths.get("stop_words.txt"));
            stopWords.addAll(Arrays.asList(stops.split(",")));
        } catch (IOException e) {
            System.out.println("Could not load stop words.");
        }
        for (char ch = 'a'; ch <= 'z'; ch++) {
            stopWords.add(String.valueOf(ch));
        }

        for (String w : words) {
            if (!stopWords.contains(w) && !w.isBlank()) {
                pairs.add(new Pair(w, 1));
            }
        }
        return pairs;
    }

    
    public static Map<String, List<Pair>> regroup(List<List<Pair>> allPairs) {
        Map<String, List<Pair>> map = new HashMap<>();
        for (List<Pair> list : allPairs) {
            for (Pair p : list) {
                map.computeIfAbsent(p.word, k -> new ArrayList<>()).add(p);
            }
        }
        return map;
    }

    
    public static Pair countWords(String word, List<Pair> group) {
        int total = 0;
        for (Pair p : group) {
            total += p.count;
        }
        return new Pair(word, total);
    }

    
    public static List<Pair> sort(List<Pair> freqs) {
        freqs.sort((a, b) -> b.count - a.count);
        return freqs;
    }

    public static void main(String[] args) {
        String content = readFile(args[0]);
        List<String> chunks = partition(content, 200);

        
        List<List<Pair>> mapped = new ArrayList<>();
        for (String chunk : chunks) {
            mapped.add(splitWords(chunk));
        }

        
        Map<String, List<Pair>> grouped = regroup(mapped);

        
        List<Pair> freqs = new ArrayList<>();
        for (Map.Entry<String, List<Pair>> entry : grouped.entrySet()) {
            freqs.add(countWords(entry.getKey(), entry.getValue()));
        }

        
        sort(freqs);
        for (int i = 0; i < 25 && i < freqs.size(); i++) {
            System.out.println(freqs.get(i).word + " - " + freqs.get(i).count);
        }
    }

    
    static class Pair {
        String word;
        int count;
        Pair(String w, int c) {
            word = w;
            count = c;
        }
    }
}