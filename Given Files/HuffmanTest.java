import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests every required Project 3 method and its stated edge cases. */
public class HuffmanTest {
    @TempDir
    Path tempDir;

    /** Verifies cntFreq counts bytes and leaves absent entries zero. */
    @Test void testCntFreq() throws IOException {
        Path input = write(); int[] f = Huffman.cntFreq(input.toString());
        assertEquals(2, f['a']); assertEquals(2, f['b']); assertEquals(1, f['c']); assertEquals(0, f['d']);
    }

    /** Verifies comesBefore uses frequency then ASCII tie-breaking. */
    @Test void testComesBefore() {
        assertTrue(HuffmanNode.comesBefore(new HuffmanNode('a', 2), new HuffmanNode('b', 2)));
        assertTrue(HuffmanNode.comesBefore(new HuffmanNode('z', 1), new HuffmanNode('a', 2)));
        assertFalse(HuffmanNode.comesBefore(new HuffmanNode('b', 2), new HuffmanNode('a', 2)));
    }

    /** Verifies combine places the earlier child left and totals frequencies. */
    @Test void testCombine() {
        HuffmanNode a = new HuffmanNode('a', 2), b = new HuffmanNode('b', 1); HuffmanNode parent = HuffmanNode.combine(a, b);
        assertSame(b, parent.left()); assertSame(a, parent.right()); assertEquals(3, parent.freq()); assertEquals('a', parent.charAscii());
    }

    /** Verifies addNode maintains an ArrayList's ascending order. */
    @Test void testAddNode() {
        Collection<HuffmanNode> nodes = new ArrayList<>(); Huffman.addNode(nodes, new HuffmanNode('c', 2));
        Huffman.addNode(nodes, new HuffmanNode('a', 1)); Huffman.addNode(nodes, new HuffmanNode('b', 2));
        assertEquals('a', ((ArrayList<HuffmanNode>) nodes).get(0).charAscii()); assertEquals('b', ((ArrayList<HuffmanNode>) nodes).get(1).charAscii());
    }

    /** Verifies removeMin works on a collection that is not the implementation's ordered list. */
    @Test void testRemoveMin() {
        Collection<HuffmanNode> nodes = new LinkedList<>(); nodes.add(new HuffmanNode('b', 2)); nodes.add(new HuffmanNode('a', 2));
        assertEquals('a', Huffman.removeMin(nodes).charAscii()); assertEquals(1, nodes.size()); assertNull(Huffman.removeMin(new ArrayList<>()));
    }

    /** Verifies createHuffTree handles normal, empty, and single-character frequency tables. */
    @Test void testCreateHuffTree() {
        int[] two = new int[256]; two['a'] = 1; two['b'] = 1; HuffmanNode root = Huffman.createHuffTree(two);
        assertEquals(2, root.freq()); assertEquals('a', root.left().charAscii()); assertNull(Huffman.createHuffTree(new int[256]));
        int[] lone = new int[256]; lone['x'] = 4; assertTrue(Huffman.createHuffTree(lone).isLeaf());
    }

    /** Verifies createCode records paths and keeps a singleton code empty. */
    @Test void testCreateCode() {
        HuffmanNode root = HuffmanNode.combine(new HuffmanNode('a', 1), new HuffmanNode('b', 1)); String[] codes = Huffman.createCode(root);
        assertEquals("0", codes['a']); assertEquals("1", codes['b']); assertEquals("", codes['c']);
        assertEquals("", Huffman.createCode(new HuffmanNode('x', 2))['x']);
    }

    /** Verifies createHeader only includes nonzero entries in ASCII order. */
    @Test void testCreateHeader() { int[] f = new int[256]; f['b'] = 4; f['a'] = 3; assertEquals("97 3 98 4", Huffman.createHeader(f)); }

    /** Verifies parseHeader rebuilds a frequency table and accepts an empty header. */
    @Test void testParseHeader() { int[] f = Huffman.parseHeader("97 3 98 4"); assertEquals(3, f['a']); assertEquals(4, f['b']); assertEquals(0, Huffman.parseHeader("")['a']); }

    /** Writes an ASCII test file in the JUnit temporary directory. */
    private Path write() throws IOException { Path file = tempDir.resolve("counts.txt"); Files.writeString(file, "aabbc"); return file; }
}
