# Week_6
# 🔍 Word Frequency Counter – Java Editions

## 📁 Files Overview

| Filename       | Style                 | Summary                                                              |
|----------------|-----------------------|----------------------------------------------------------------------|
| `TwentyNine.java` | Style #29 (Actors)     | Message-passing objects in their own threads ("free agents")         |
| `Thirty.java`     | Style #30 (Dataspaces) | Uses shared queues for communication between concurrent workers      |
| `ThirtyTwo.java`  | Style #32 (Map-Reduce) | Breaks data into chunks, applies map, regroup, and reduce logic      |

---

## ⚙️ How to Compile & Run

```bash
javac TwentyNine.java
javac Thirty.java
javac ThirtyTwo.java

java TwentyNine pride-and-prejudice.txt
java Thirty pride-and-prejudice.txt
java ThirtyTwo pride-and-prejudice.txt

```

# Program Descriptions

## 1. TwentyNine.java – Style #29: Actor Model

- Each "thing" (data loader, stop word filter, frequency counter) is a thread with a message queue.

- Threads only communicate by sending messages (Message objects) to each other’s queues.

- Fully decoupled, real actor-style simulation.

Concept: "Each actor does one thing. Everything is done by passing messages."

## 2. Thirty.java – Style #30: Dataspaces (Linda)

- A shared BlockingQueue (wordSpace) holds all words to be processed.

- Multiple worker threads consume words and produce local frequency maps.

- After all workers are done, maps are merged and printed.

Concept: "Workers dump to and pull from common data spaces."

## 3. ThirtyTwo.java – Style #32: Map-Reduce

- The input text is broken into chunks of ~200 lines.

- A map step splits each chunk into words and filters stop words.

- A group step regroups all words.

- A reduce step counts and sums frequencies.

- Finally, the result is sorted and printed.

Concept: "Chunk → Map → Regroup → Reduce → Print"
