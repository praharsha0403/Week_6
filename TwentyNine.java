import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class TwentyNine {
    public static void main(String[] args) throws Exception {
        WordFreqManager wfManager = new WordFreqManager();
        StopWordManager stopManager = new StopWordManager(wfManager);
        DataStorageManager dataStorage = new DataStorageManager(stopManager);
        WordFreqController controller = new WordFreqController(dataStorage);

        dataStorage.send(new Message("init", args[0]));
        stopManager.send(new Message("init", "stop_words.txt"));
        controller.send(new Message("run", null));

        controller.join();
        dataStorage.join();
        stopManager.join();
        wfManager.join();
    }
}

class Message {
    String type;
    Object data;
    Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }
}

abstract class ActiveWFObject extends Thread {
    BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    boolean stopMe = false;

    public void send(Message msg) {
        queue.offer(msg);
    }

    public void run() {
        while (!stopMe) {
            try {
                Message msg = queue.take();
                dispatch(msg);
                if (msg.type.equals("die")) stopMe = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void dispatch(Message msg) throws Exception;
}

class DataStorageManager extends ActiveWFObject {
    String data = "";
    StopWordManager stopWordManager;

    DataStorageManager(StopWordManager swm) {
        stopWordManager = swm;
        this.start();
    }

    protected void dispatch(Message msg) throws Exception {
        if (msg.type.equals("init")) {
            String path = (String) msg.data;
            data = Files.readString(Paths.get(path)).replaceAll("[^a-zA-Z0-9]+", " ").toLowerCase();
        } else if (msg.type.equals("send_word_freqs")) {
            String[] words = data.split("\\s+");
            for (String word : words) {
                stopWordManager.send(new Message("filter", word));
            }
            stopWordManager.send(new Message("top25", msg.data));
        } else {
            stopWordManager.send(msg);
        }
    }
}

class StopWordManager extends ActiveWFObject {
    Set<String> stopWords = new HashSet<>();
    WordFreqManager wordFreqManager;

    StopWordManager(WordFreqManager wfm) {
        wordFreqManager = wfm;
        this.start();
    }

    protected void dispatch(Message msg) throws Exception {
        if (msg.type.equals("init")) {
            String content = Files.readString(Paths.get((String) msg.data));
            stopWords.addAll(Arrays.asList(content.split(",")));
            for (char c = 'a'; c <= 'z'; c++) stopWords.add(String.valueOf(c));
        } else if (msg.type.equals("filter")) {
            String word = (String) msg.data;
            if (!stopWords.contains(word)) {
                wordFreqManager.send(new Message("word", word));
            }
        } else {
            wordFreqManager.send(msg);
        }
    }
}

class WordFreqManager extends ActiveWFObject {
    Map<String, Integer> freqs = new HashMap<>();

    WordFreqManager() {
        this.start();
    }

    protected void dispatch(Message msg) {
        if (msg.type.equals("word")) {
            String word = (String) msg.data;
            freqs.put(word, freqs.getOrDefault(word, 0) + 1);
        } else if (msg.type.equals("top25")) {
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(freqs.entrySet());
            sorted.sort((a, b) -> b.getValue() - a.getValue());
            WordFreqController ctrl = (WordFreqController) msg.data;
            ctrl.send(new Message("top25", sorted));
        }
    }
}

class WordFreqController extends ActiveWFObject {
    DataStorageManager storage;

    WordFreqController(DataStorageManager dsm) {
        storage = dsm;
        this.start();
    }

    protected void dispatch(Message msg) {
        if (msg.type.equals("run")) {
            storage.send(new Message("send_word_freqs", this));
        } else if (msg.type.equals("top25")) {
            List<Map.Entry<String, Integer>> list = (List<Map.Entry<String, Integer>>) msg.data;
            for (int i = 0; i < 25 && i < list.size(); i++) {
                System.out.println(list.get(i).getKey() + " - " + list.get(i).getValue());
            }
            storage.send(new Message("die", null));
            stopMe = true;
        }
    }
}