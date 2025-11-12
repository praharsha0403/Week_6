import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class Thirty {

    
    static BlockingQueue<String> wordSpace = new LinkedBlockingQueue<>();
    static BlockingQueue<Map<String, Integer>> freqSpace = new LinkedBlockingQueue<>();

    static Set<String> stopWords = new HashSet<>();

    public static void main(String[] args) throws Exception {
        
        try {
            String text = new String(Files.readAllBytes(Paths.get("stop_words.txt")));
            stopWords.addAll(Arrays.asList(text.split(",")));
        } catch (IOException e) {
            System.out.println("Couldn't load stop_words.txt");
            return;
        }

        
        String content = new String(Files.readAllBytes(Paths.get(args[0])));
        Matcher matcher = Pattern.compile("[a-z]{2,}").matcher(content.toLowerCase());
        while (matcher.find()) {
            wordSpace.put(matcher.group());
        }

        
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            workers.add(new Thread(() -> {
                Map<String, Integer> wordFreqs = new HashMap<>();
                while (true) {
                    String word;
                    try {
                        word = wordSpace.poll(1, TimeUnit.SECONDS);
                        if (word == null) break;
                        if (!stopWords.contains(word)) {
                            wordFreqs.put(word, wordFreqs.getOrDefault(word, 0) + 1);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                freqSpace.add(wordFreqs);
            }));
        }

        
        for (Thread t : workers) t.start();
        for (Thread t : workers) t.join();

        
        Map<String, Integer> allFreqs = new HashMap<>();
        while (!freqSpace.isEmpty()) {
            Map<String, Integer> part = freqSpace.poll();
            for (Map.Entry<String, Integer> entry : part.entrySet()) {
                String word = entry.getKey();
                int count = entry.getValue();
                allFreqs.put(word, allFreqs.getOrDefault(word, 0) + count);
            }
        }

        
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(allFreqs.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        for (int i = 0; i < 25 && i < sorted.size(); i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }
}