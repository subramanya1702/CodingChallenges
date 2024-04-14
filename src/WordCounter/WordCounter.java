package WordCounter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * <p>
 * A word counter tool that is modeled after the unix utility, `wc`. Ref: <a href="https://www.unix.com/man-page/hpux/1/wc/">https://www.unix.com/man-page/hpux/1/wc/</a>
 * The tool accepts the following command line arguments
 * </p>
 * <p>
 * Arguments:
 * c = bytes in input
 * l = lines in input
 * w = words in input
 * m = characters in input
 * <p>
 * Note: The arguments are case sensitive
 * </p>
 */
public class WordCounter {
    private final boolean printBytes;
    private final boolean printLines;
    private final boolean printWords;
    private final boolean printCharacters;
    private long bytes = 0;
    private long lines = 0;
    private long words = 0;
    private long totalBytes = 0;
    private long totalLines = 0;
    private long totalWords = 0;
    private final List<String> fileNames;

    public WordCounter(final String args[]) {
        this.fileNames = new ArrayList<>();
        boolean c = false, l = false, w = false, m = false;

        for (String arg : args) {
            if (arg.charAt(0) == '-') {
                for (int index = 1; index < arg.length(); index++) {
                    char option = arg.charAt(index);

                    switch (option) {
                        case 'c' -> c = true;
                        case 'l' -> l = true;
                        case 'w' -> w = true;
                        case 'm' -> m = true;
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

        if (c && m) {
            System.out.println("Invalid options: You can only use either `c` or `m` and not both");
            System.exit(0);
        }

        // If no options are passed, count all of them - c, l and w
        if (!(c || l || w || m)) {
            this.printBytes = true;
            this.printLines = true;
            this.printWords = true;
            this.printCharacters = false;
        } else {
            this.printBytes = c;
            this.printLines = l;
            this.printWords = w;
            this.printCharacters = m;
        }
    }

    private void resetCounters() {
        bytes = 0;
        lines = 0;
        words = 0;
    }

    private void incrementTotalCounters() {
        totalBytes += bytes;
        totalLines += lines;
        totalWords += words;
    }

    private void printToStdOut(String fileName) {
        StringBuilder builder = new StringBuilder();

        if (printLines) {
            builder.append(lines).append(" ");
        }

        if (printWords) {
            builder.append(words).append(" ");
        }

        if (printBytes || printCharacters) {
            builder.append(bytes).append(" ");
        }

        builder.append(fileName);

        System.out.println(builder);
    }

    private void countFileSize(String filePath) {
        File file = new File(filePath);
        bytes = file.length();
    }

    private void countCharacters(BufferedReader br) throws IOException {
        int character = br.read();

        while (character != -1) {
            bytes++;
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
                    lines++;
                }

                bytes++;
                charInt = br.read();
                previous = character;
                character = (char) charInt;
            }

            if (charInt != -1) {
                words++;

                while (charInt != -1 && !Character.isWhitespace(character)) {
                    bytes++;
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
            lines++;
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
                    words++;

                    while (index < lineLength && !Character.isWhitespace(line.charAt(index))) {
                        index++;
                    }
                }
            }

            lines++;
            line = br.readLine();
        }
    }

    private void process(String fileName, BufferedReader br) throws IOException {
        if (printLines || printWords || printCharacters) {
            if (printCharacters) {
                if (printLines || printWords) {
                    countCharactersAndOthers(br);
                } else {
                    countCharacters(br);
                }
            } else if (printLines && !printWords) {
                countLines(br);
            } else {
                countWordsWithLines(br);
            }
        }

        incrementTotalCounters();
        printToStdOut(fileName);
    }

    private void processFile(String fileName) {
        String pwd = Path.of("").toAbsolutePath().toString();
        String filePath = pwd + "/" + fileName;

        resetCounters();

        if (printBytes) {
            countFileSize(filePath);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            process(fileName, br);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processStdInput(String input) {
        resetCounters();

        if (printBytes) {
            bytes = input.getBytes().length;
        }

        try (Reader str = new StringReader(input);
             BufferedReader br = new BufferedReader(str)) {
            process("", br);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void start() {
        if (!fileNames.isEmpty()) {
            for (String fileName : fileNames) {
                processFile(fileName);
            }

            // Print the total count only when there are more than 1 files
            if (fileNames.size() > 1) {
                System.out.println(totalLines + " " + totalWords + " " + totalBytes);
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
        WordCounter wordCounter = new WordCounter(args);
        wordCounter.start();
    }
}
