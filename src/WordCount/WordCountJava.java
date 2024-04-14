package WordCount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class WordCountJava {
    /**
     * Options
     * c = bytes in input
     * l = lines in input
     * w = words in input
     * m = characters in input
     */
    private boolean c = false, l = false, w = false, m = false;
    private long lineCount = 0, wordCount = 0, byteCount = 0;
    private final List<String> fileNames;

    public WordCountJava() {
        fileNames = new ArrayList<>();
    }

    private void processArguments(String[] args) {
        for (String arg : args) {
            arg = arg.toLowerCase(Locale.ROOT);

            if (arg.charAt(0) == '-') {
                for (int index = 1; index < arg.length(); index++) {
                    char option = arg.charAt(index);

                    switch (option) {
                        case 'c' -> {
                            c = true;
                            m = false; // Cancel out prior usage of m
                        }
                        case 'l' -> l = true;
                        case 'w' -> w = true;
                        case 'm' -> {
                            m = true;
                            c = false; // Cancel out prior usage of c
                        }
                        default -> {
                            System.out.println("Invalid option: \"" + option + "\"");
                            System.exit(0);
                        }
                    }
                }
            } else {
                fileNames.add(arg);
            }
        }
    }

    private void printToStdOut(String fileName) {
        StringBuilder builder = new StringBuilder();

        if (l) {
            builder.append(lineCount).append(" ");
        }

        if (w) {
            builder.append(wordCount).append(" ");
        }

        if (c || m) {
            builder.append(byteCount).append(" ");
        }

        builder.append(fileName);

        System.out.println(builder);
    }

    private void countFileSize(String filePath) {
        File file = new File(filePath);
        byteCount = file.length();
    }

    private void countCharacters(BufferedReader br) throws IOException {
        int character = br.read();

        while (character != -1) {
            byteCount++;
            character = br.read();
        }
    }

    /**
     * Called when option `m` is requested along with other options
     */
    private void countCharactersAndOthers(BufferedReader br) throws IOException {
        int charInt = br.read();
        char character = (char) charInt;
        char previous = '0';

        while (charInt != -1) {
            while (charInt != -1 && Character.isWhitespace(character)) {
                if (previous != '\r' && (character == '\n' || character == '\r')) {
                    lineCount++;
                }

                byteCount++;
                charInt = br.read();
                previous = character;
                character = (char) charInt;
            }

            if (charInt != -1) {
                wordCount++;

                while (charInt != -1 && !Character.isWhitespace(character)) {
                    byteCount++;
                    charInt = br.read();
                    previous = character;
                    character = (char) charInt;
                }
            }
        }
    }

    /**
     * Called when only `l` is passed
     */
    private void countLines(BufferedReader br) throws IOException {
        String line = br.readLine();

        while (line != null) {
            lineCount++;
            line = br.readLine();
        }
    }

    /**
     * Called when only `lw` is passed
     */
    private void countWordsWithLines(BufferedReader br) throws IOException {
        String line = br.readLine();

        while (line != null) {
            int index = 0;
            int lineLength = line.length();

            while (index < lineLength) {
                while (index < lineLength && Character.isWhitespace(line.charAt(index))) {
                    index++;
                }

                if (index < lineLength) {
                    wordCount++;

                    while (index < lineLength && !Character.isWhitespace(line.charAt(index))) {
                        index++;
                    }
                }
            }

            lineCount++;
            line = br.readLine();
        }
    }

    private void process(String fileName, BufferedReader br) throws IOException {
        if (l || w || m) {
            if (m) {
                if (l || w) {
                    countCharactersAndOthers(br);
                } else {
                    countCharacters(br);
                }
            } else if (l && !w) {
                countLines(br);
            } else {
                countWordsWithLines(br);
            }
        }

        printToStdOut(fileName);
    }

    private void processFile(String fileName) {
        String pwd = Path.of("").toAbsolutePath().toString();
        String filePath = pwd + "/" + fileName;

        if (c) {
            countFileSize(filePath);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            process(fileName, br);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processStdInput(String input) {
        if (c) {
            byteCount = input.getBytes().length;
        }

        try (Reader str = new StringReader(input);
             BufferedReader br = new BufferedReader(str)) {
            process("", br);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void start(String[] args) {
        processArguments(args);

        // If no options are passed, count all of them - c, l and w
        if (!(c || l || w || m)) {
            c = l = w = true;
        }

        if (!fileNames.isEmpty()) {
            for (String fileName : fileNames) {
                processFile(fileName);
            }
        } else {
            Scanner scanner = new Scanner(System.in);
            StringBuilder input = new StringBuilder();
            String line = "";

            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                input.append(line).append('\n');
            }

            processStdInput(input.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        WordCountJava wordCountJava = new WordCountJava();
        wordCountJava.start(args);
    }
}
