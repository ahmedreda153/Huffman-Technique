import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;

class HuffmanNode {
    int freq;
    char ch;
    HuffmanNode left;
    HuffmanNode right;
}

class ImplementComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode l, HuffmanNode r) {
        return l.freq - r.freq;
    }
}

public class huffman {
    // Function to read the uncompressed file and return its contents as a string
    public static String readUncompressesdFile(String fileName) {
        String txt = "";
        try {
            File file = new File(fileName);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                txt += sc.nextLine();
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        return txt;
    }

    /*
     * description:
     * calculate the code of each character recursively
     * saves the code in huffmanCode map in the base case
     */
    public static void saveCode(HuffmanNode root, String code, Map<Character, String> huffmanCode) {
        if (root.left == null && root.right == null) {
            huffmanCode.put(root.ch, code);
            return;
        }
        saveCode(root.left, code + "0", huffmanCode);
        saveCode(root.right, code + "1", huffmanCode);
    }

    // Function to define the frequency of each character in the string
    public static Map<Character, Integer> getFrequency(String str) {
        Map<Character, Integer> freqArray = new HashMap<Character, Integer>();
        for (int i = 0; i < str.length(); i++) {
            int cnt = 0;
            if (freqArray.get(str.charAt(i)) != null)
                cnt = freqArray.get(str.charAt(i));
            freqArray.put(str.charAt(i), cnt + 1);
        }
        return freqArray;
    }

    // Function to add nodes to the priority queue
    public static void addNodes(PriorityQueue<HuffmanNode> q2, Map<Character, Integer> freqArray) {
        for (Map.Entry<Character, Integer> entry : freqArray.entrySet()) {
            HuffmanNode node = new HuffmanNode();
            node.ch = entry.getKey();
            node.freq = entry.getValue();
            node.left = node.right = null;
            q2.add(node);
        }
    }

    // Function to build the huffman tree
    public static HuffmanNode buildHuffmanTree(PriorityQueue<HuffmanNode> q2) {
        while (q2.size() > 1) {
            HuffmanNode l = q2.peek();
            q2.poll();
            HuffmanNode r = q2.peek();
            q2.poll();

            HuffmanNode newNode = new HuffmanNode();
            newNode.ch = '$';
            newNode.freq = l.freq + r.freq;
            newNode.left = l;
            newNode.right = r;
            q2.add(newNode);
        }
        return q2.poll(); // Return the root of the Huffman tree
    }

    // Function to write the compressed data to a file
    public static void writeCompressedData(String str, Map<Character, Integer> freqArray,
            String compressed, int extraBits) {
        try (FileOutputStream out = new FileOutputStream("compressed.txt")) {
            // write the number of the different characters in the string
            out.write(freqArray.size());

            // write the characters and their frequencies in the file
            for (Map.Entry<Character, Integer> entry : freqArray.entrySet()) {
                out.write(entry.getKey());
                out.write(entry.getValue());
            }

            // write the number of extra bits in the file
            out.write(extraBits);

            // write the compressed string in the file
            for (int i = 0; i < compressed.length(); i += 8) {
                out.write(Integer.parseInt(compressed.substring(i, i + 8), 2));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (Exception e) {
            System.out.println("Error writing compressed data to file");
        }
    }

    public static void compress(String fileName) {
        // this map contains each character and its huffman code
        Map<Character, String> huffmanCode = new HashMap<>();

        // huffman tree root
        HuffmanNode root = null;

        // load the string from file
        String str = readUncompressesdFile(fileName);

        // frequency map that stores each character and its frequency in the string
        Map<Character, Integer> freqArray = new HashMap<Character, Integer>();

        // The priority queue is used to store huffman nodes
        PriorityQueue<HuffmanNode> q2 = new PriorityQueue<HuffmanNode>(new ImplementComparator());

        // defining a frequency map to store the character and its frequency in the
        // string
        freqArray = getFrequency(str);

        // add nodes to the priority queue
        addNodes(q2, freqArray);

        // build the huffman tree
        root = buildHuffmanTree(q2);

        saveCode(root, "", huffmanCode);

        // stream of bits that represents the compressed string
        String compressed = "";
        for (int i = 0; i < str.length(); i++) {
            compressed += huffmanCode.get(str.charAt(i));
        }

        // store number of extra bits
        int extraBits = 8 - compressed.length() % 8;

        // add extra bits to the compressed string
        for (int i = 0; i < extraBits; i++) {
            compressed += "0";
        }

        // write the compressed data to a file
        writeCompressedData(str, freqArray, compressed, extraBits);
        System.out.println("Compression Done");
    }

    // Function to read the compressed data from a file
    public static byte[] readCompressedFile(String fileName) {
        try {
            File file = new File(fileName);
            FileInputStream in = new FileInputStream(file);
            byte[] compressedBytes = new byte[in.available()];
            in.read(compressedBytes);
            in.close();
            return compressedBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Function to reverse the the key and value of a map
    public static Map<String, Character> reverseMap(Map<Character, String> huffmanCode) {
        Map<String, Character> huffmanCodeReverse = new HashMap<>();
        for (Map.Entry<Character, String> entry : huffmanCode.entrySet()) {
            huffmanCodeReverse.put(entry.getValue(), entry.getKey());
        }
        return huffmanCodeReverse;
    }

    // Function to get code of each character from the compressed data and convert it from map to string
    public static String getCompressedString(Map<String, Character> huffmanCodeReverse, String compressed) {
        String decompressed = "";
        String code = "";
        for (int i = 0; i < compressed.length(); i++) {
            code += compressed.charAt(i);
            if (huffmanCodeReverse.get(code) != null) {
                decompressed += huffmanCodeReverse.get(code);
                code = "";
            }
        }
        return decompressed;
    }

    // Function to decompress the compressed data and write the result to a file
    public static void decompress(String fileName) {
        try (FileOutputStream out = new FileOutputStream("decompressed.txt")) {
            // read the compressed data from the file
            byte[] compressedBytes = readCompressedFile(fileName);
    
            // get the number of different characters in the string
            int numOfChars = compressedBytes[0];
    
            // get the characters and their frequencies
            Map<Character, Integer> freqArray = new HashMap<Character, Integer>();
            for (int i = 1; i < 1 + numOfChars * 2; i += 2) {
                freqArray.put((char) compressedBytes[i], (int) compressedBytes[i + 1]);
            }
    
            // get the number of extra bits
            int extraBits = compressedBytes[1 + numOfChars * 2];
    
            // read the compressed string
            String compressed = "";
            for (int i = 2 + numOfChars * 2; i < compressedBytes.length; i++) {
                compressed += String.format("%8s", Integer.toBinaryString(compressedBytes[i] & 0xFF)).replace(' ',
                        '0');
            }
    
            // remove the extra bits
            compressed = compressed.substring(0, compressed.length() - extraBits);
    
            // add nodes to the priority queue
            PriorityQueue<HuffmanNode> q2 = new PriorityQueue<HuffmanNode>(new ImplementComparator());
            addNodes(q2, freqArray);
    
            // build the huffman tree
            HuffmanNode root = buildHuffmanTree(q2);
    
            // this map contains each character and its huffman code
            Map<Character, String> huffmanCode = new HashMap<>();
            saveCode(root, "", huffmanCode);
    
            // this map contains each huffman code and its character
            Map<String, Character> huffmanCodeReverse = reverseMap(huffmanCode);
    
            // decompress the string
            String decompressed = getCompressedString(huffmanCodeReverse, compressed);
    
            // write the decompressed data to a file
            out.write(decompressed.getBytes());

            System.out.println("Decompression Done");
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (Exception e) {
            System.out.println("Error writing decompressed data to file");
        }
    }    
}
