import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Project 3: Huffman Encoding / Decoding.
 * <p>
 * Implement the methods below to compress and decompress text files using
 * Huffman coding. See the assignment write-up for the full specification.
 * <p>
 *  - Treat characters as 8-bit values in the range [0, 255].
 *  - When writing output files, write the newline character explicitly as
 *    '\n' (do not rely on System.lineSeparator() / println()), so your
 *    output is identical across Windows/Mac/Linux. A BufferedWriter over
 *    a FileWriter, writing "\n" yourself, works well for this.
 *  - Encoded files store the code as literal '0'/'1' characters (one byte
 *    each), not as packed bits, so the "encoded" file will actually be
 *    larger than the original -- that's expected for this assignment.
 *  - createHuffTree() is built on top of addNode()/removeMin(), which
 *    operate generically over a java.util.Collection<HuffmanNode>.
 *  - HuffmanNode is a record, so its components are read with
 *    node.charAscii(), node.freq(), node.left(), and node.right() -- no
 *    "get" prefix.
 */
public class Huffman {

    /** Number of possible 8-bit character values. */
    public static final int NUM_CHARS = 256;

    /**
     * Opens the text file named filename and counts the frequency of
     * occurrence of every character in it.
     * Returns an int array of size NUM_CHARS, indexed by ASCII value, with
     * the count of each character (0 for characters that do not occur).
     */

    // parameter: filename path to an 8-bits input file
    // return value: a 256-entry frequency array indexed by byte/ASCII
    // example: cntFreq("letter.txt"[97]) = 'a'
    public static int[] cntFreq(String filename) throws IOException {
        int[] frequencies = new int[NUM_CHARS];
        for (byte value : Files.readAllBytes(Path.of(filename))) frequencies[Byte.toUnsignedInt(value)]++;
        return frequencies;
    }

    /**
     * Adds node to collection, preserving whatever ordering your chosen
     * implementation of collection relies on to make the minimum element
     * easy to find.
     * <p>
     * You choose what kind of Collection to pass in when you call this
     * from createHuffTree(). For example, an ArrayList kept sorted by
     * HuffmanNode.comesBefore() as you insert (an "ordered list"), or a
     * PriorityQueue. How you implement this method depends on which you pick.
     */

    // parameter: collection destination collection, node to insert
    // return value: nothing
    // example: adding 'a':1 then 'b':2 to an ArrayList puts 'a' first.

    public static void addNode(Collection<HuffmanNode> collection, HuffmanNode node) {
        if (collection instanceof ArrayList<?>) {
            ArrayList<HuffmanNode> ordered = (ArrayList<HuffmanNode>) collection;
            int index = 0;
            while (index < ordered.size() && !HuffmanNode.comesBefore(node, ordered.get(index))) index++;
            ordered.add(index, node);
        }

        else collection.add(node);
    }

    /**
     * Removes and returns the smallest HuffmanNode in collection, using
     * HuffmanNode.comesBefore() (or compareTo(), which relies on it) to
     * determine "smallest". Returns null if collection is empty.
     *
     * This must work for ANY Collection passed in -- not just the one you
     * use inside createHuffTree(). For example, if you back createHuffTree()
     * with a PriorityQueue, poll() it; for any other collection, find the
     * minimum node and remove it.
     */

    // parameter: collection nodes to search
    // return value: the smallest node, or null when collection is empty
    // example: removing from {'b':2, 'a':1} return 'a':1
    public static HuffmanNode removeMin(Collection<HuffmanNode> collection) {
        HuffmanNode smallest = null;
        for (HuffmanNode node: collection)
            if (smallest == null || HuffmanNode.comesBefore(node, smallest)) smallest = node;
        if (smallest != null) collection.remove(smallest);
        return smallest;
    }

    /**
     * Builds a Huffman tree from an array of character frequencies (as
     * produced by cntFreq()) and returns the root HuffmanNode of the tree.
     *
     * Use addNode()/removeMin() above to manage the collection of
     * not-yet-combined nodes as you build the tree -- pick whichever
     * concrete Collection type you want to back them with (e.g. a sorted
     * ArrayList, or a PriorityQueue). The rest of this method, and the
     * resulting tree, should not depend on which one you chose.
     *
     * Special cases:
     *  - If freq contains no non-zero counts (empty input file), return null
     *  - If freq contains exactly one non-zero count (input file has only
     *    one unique character), return a single leaf HuffmanNode for that
     *    character (no combine() call needed).
     */

    // parameter: freq 256-entry freq array
    // return: root of the tree, or null for an all-zero array
    // example: a table containing {'a':1, 'b':1} produces a root of freq. 2
    public static HuffmanNode createHuffTree(int[] freq) {
        Collection<HuffmanNode> nodes = new ArrayList<>();
        for (int ascii = 0; ascii < NUM_CHARS; ascii++)
            if (freq[ascii] > 0) addNode(nodes, new HuffmanNode(ascii, (int) freq[ascii]));
        while (nodes.size() > 1) addNode(nodes, HuffmanNode.combine(removeMin((nodes)), removeMin(nodes)));
        return removeMin(nodes);
    }

    /**
     * Traverses the Huffman tree rooted at root and builds the Huffman
     * code for every character that appears in it. Going left appends
     * '0' to the code, going right appends '1'.
     *
     * Returns a String array of size NUM_CHARS, indexed by ASCII value.
     * Characters that do not appear in the tree have an empty string ""
     * at their index. The same is true of the lone character in a
     * single-node tree (the only-one-unique-character case) - it has no
     * left/right branch to build a code out of; huffmanEncode/huffmanDecode
     * handle that case entirely through the header.
     */

    // parameter: root of the Huffman tree, possibly null
    // return: 256-entry code table; absent characters and a long root use ""
    // example: a tree with 'a' on the left stores "0" at index 97 (ASCII value)
    public static String[] createCode(HuffmanNode root) {
        String[] codes = new String[NUM_CHARS];
        Arrays.fill(codes, "");
        fillCodes(root, "", codes);
        return codes;
    }

    /**
     * Given a frequency array (as produced by cntFreq()), returns a
     * header string listing the ASCII values and their associated
     * frequencies, separated by single spaces, in ascending order of
     * ASCII value. Only characters with a non-zero count are included.
     *
     * Example: for the text "aaabbbbcc" this returns "97 3 98 4 99 2".
     */

    // parameter: freq 256-entry freq. array
    // return value: space-separated ASCII/count pairs
    // example: frequencies for aaab return "97 3 98 1"
    public static String createHeader(int[] freq) {
        StringBuilder header = new StringBuilder();
        for (int ascii = 0; ascii < NUM_CHARS; ascii++)
            if (freq[ascii] > 0) {
                if (!header.isEmpty()) header.append(' ');
                header.append(ascii).append(' ').append(freq[ascii]);
            }
        return header.toString();
    }

    /**
     * Reads the input text file inFile and writes to outFile:
     *   1. A header line (see createHeader), followed by a newline
     *   2. The Huffman-encoded contents of inFile, using the codes from
     *      createCode() -- UNLESS inFile has only one unique character,
     *      see below.
     * If outFile already exists, its old contents are overwritten.
     *
     * Special cases:
     *  - Empty input file: outFile should end up empty too
     *  - Input file with only one unique character: should contain only the
     *    header line (e.g. "97 5") and nothing else - no second line of code bits.
     */

    // parameter: inFile source file path, outFile destination file path, overwritten if it exists
    // return: nothing
    // example: encoding a file containing only "aaa" writes the bytes for "97"
    public static void huffmanEncode(String inFile, String outFile) throws IOException {
        byte[] input = Files.readAllBytes(Path.of(inFile));
        int[] freq = cntFreq(inFile);
        if (input.length == 0) { Files.write(Path.of(outFile), new byte[0]); return; }
        String[] codes = createCode(createHuffTree(freq));
        StringBuilder encoded = new StringBuilder(createHeader(freq)).append('\n');
        for (byte value : input) encoded.append(codes[Byte.toUnsignedInt(value)]);
        Files.writeString(Path.of(outFile), encoded.toString(), java.nio.charset.StandardCharsets.US_ASCII);
    }

    /**
     * Parses a header string (in the format produced by createHeader) and
     * returns the corresponding frequency array of size NUM_CHARS, indexed
     * by ASCII value, i.e. the same format returned by cntFreq().
     *
     * Example: for the header "97 3 98 4 99 2", freq[97, 98, 99] == {3, 4, 2}.
     */
    // parameter: headerString space-separted ASCII/count pairs, or empty
    // return: corresponding freq. table
    // example: parseHeader("97 2")[97] -> return 2
    public static int[] parseHeader(String headerString) {
        int[] freq = new int[NUM_CHARS];
        if (headerString == null || headerString.isBlank())
            return freq;
        String[] values = headerString.trim().split("\\s+");
        if (values.length % 2 != 0)
            throw new IllegalArgumentException("Header needs ASCII pairs");
        for (int i = 0; i < values.length; i += 2) {
            int ascii = Integer.parseInt(values[i]);
            if (ascii < 0 || ascii >= NUM_CHARS)
                throw new IllegalArgumentException("ASCII value out of range");
            freq[ascii] = Integer.parseInt(values[i + 1]);
        }
        return freq;
    }

    /**
     * Reads an encoded file (in the format written by huffmanEncode) and
     * writes the decoded text to decodeFile.
     *
     * Recreate the frequency array from the header with parseHeader(),
     * rebuild the Huffman tree with createHuffTree(), then walk the tree
     * for each bit of the second line: '0' -> left child, '1' -> right
     * child. When a leaf is reached, emit its character and restart at
     * the root.
     *
     * Special case: if the recreated tree is a single leaf (the
     * only-one-unique-character case), there is no second line to read
     * The header's count alone tells you how many times that character occurred,
     * so write it out that many times directly.
     */
    // parameter: encodedFile encoded input path, decodeFile destination path, overwritten if it exists
    // return value: nothing
    // example: decoding "97 2\\n" -> aa

    public static void huffmanDecode(String encodedFile, String decodeFile) throws IOException {
        byte[] encoded = Files.readAllBytes(Path.of(encodedFile));
        int newline = indexOfNewline(encoded);
        if (newline < 0) { Files.write(Path.of(decodeFile), new byte[0]); return; }
        int[] freq = parseHeader(new String(encoded, 0, newline, java.nio.charset.StandardCharsets.US_ASCII));
        HuffmanNode root = createHuffTree(freq);
        if (root == null) { Files.write(Path.of(decodeFile), new byte[0]); return; }
        if (root.isLeaf()) {
            byte[] repeated = new byte[root.freq()];
            Arrays.fill(repeated, (byte) root.charAscii());
            Files.write(Path.of(decodeFile), repeated);
            return;
        }
        ArrayList<Byte> output = new ArrayList<>(); HuffmanNode current = root;
        for (int i = newline + 1; i < encoded.length; i++) { current = encoded[i] == '0' ? current.left() : current.right();
            if (current.isLeaf()) { output.add((byte) current.charAscii()); current = root; } }
        byte[] decoded = new byte[output.size()]; for (int i = 0; i < decoded.length; i++) decoded[i] = output.get(i);
        Files.write(Path.of(decodeFile), decoded);
    }


    private static void fillCodes(HuffmanNode node, String prefix, String[] codes) {
        if (node == null) return;
        if (node.isLeaf()) { codes[node.charAscii()] = prefix; return; }
        fillCodes(node.left(), prefix + "0", codes);
        fillCodes(node.right(), prefix + "1", codes);
    }

    private static int indexOfNewline(byte[] data) {
        for (int i = 0; i < data.length; i++)
            if (data[i] == '\n')
                return i;
        return -1; }
}
