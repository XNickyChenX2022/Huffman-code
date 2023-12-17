package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.transform.Source;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        this.sortedCharFreqList = new ArrayList<CharFreq>();
        StdIn.setFile(fileName);
        int [] arr = new int [128];
        int length = 0;
        while(StdIn.hasNextChar()){
            char newChar = StdIn.readChar();
            arr[newChar] ++;
            length++;
        }
        for(int i = 0; i < arr.length;i ++){
            if(arr[i] != 0){
                double p = (double)arr[i]/length; 
                CharFreq newElt = new CharFreq((char)i, p);
                sortedCharFreqList.add(newElt);
            }
        }
        if(sortedCharFreqList.size() == 1){
            int only = sortedCharFreqList.get(0).getCharacter();
            if(only == 127) only = -1;
            CharFreq newElt = new CharFreq((char)(only+1),0);
            sortedCharFreqList.add(newElt);
        }
        Collections.sort(sortedCharFreqList);
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    private TreeNode findSmallestNode(Queue Source, Queue Target){
        if(Source.size() == 0) return (TreeNode)Target.dequeue();
        if(Target.size() == 0) return (TreeNode)Source.dequeue();
        TreeNode SourceNode =  (TreeNode)Source.peek();
        TreeNode TargetNode = (TreeNode)Target.peek();
        double SourceProb = SourceNode.getData().getProbOcc();
        double TargetProb = TargetNode.getData().getProbOcc();
        TreeNode newNode = null;
        if(SourceProb <= TargetProb){
            newNode = (TreeNode)Source.dequeue();
        } else{
            newNode = (TreeNode)Target.dequeue();
        }
        return newNode;
    }
    public void makeTree() {
        Queue <TreeNode> Source = new Queue<>();
        Queue <TreeNode> Target = new Queue<>();
        TreeNode newNode = null;
        for(int i = 0; i < sortedCharFreqList.size();i++){
            CharFreq newChar = sortedCharFreqList.get(i);
            newNode = new TreeNode(newChar, null, null);
            Source.enqueue(newNode);
        }
        while(!Source.isEmpty() || Target.size() != 1){
            if(Target.size() == 0 && !Source.isEmpty()){
                TreeNode Node1 = Source.dequeue(); //smaller node
                TreeNode Node2 = Source.dequeue();
                CharFreq newChar = new CharFreq(null, Node1.getData().getProbOcc() + Node2.getData().getProbOcc());
                newNode = new TreeNode(newChar, Node1, Node2);}
            // } else if(Source.isEmpty() && Target.size() > 1){
            //     TreeNode Node1 = Target.dequeue(); //smaller node
            //     TreeNode Node2 = Target.dequeue();
            //     CharFreq newChar = new CharFreq(null, Node1.getData().getProbOcc() + Node2.getData().getProbOcc());
            //     newNode = new TreeNode(newChar, Node1, Node2);
            // }
            else{
                TreeNode Node1 = findSmallestNode(Source, Target); //smaller node
                TreeNode Node2 = findSmallestNode(Source, Target);
                CharFreq newChar = new CharFreq(null, Node1.getData().getProbOcc() + Node2.getData().getProbOcc());
                newNode = new TreeNode(newChar, Node1, Node2);
            }
            huffmanRoot = newNode;
            Target.enqueue(newNode);
        }
    }

    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    private void mCode(TreeNode newNode, String[]arr, String newStr){
    
        if(newNode.getData().getCharacter()  != null){
            char newChar = newNode.getData().getCharacter();
            arr[newChar] = newStr;
            return;
        }
        mCode(newNode.getLeft(), arr, newStr + "0");
        mCode(newNode.getRight(),arr, newStr + "1");

    }
    public void makeEncodings() {
        encodings = new String [128];
        mCode(huffmanRoot, encodings, "");
    }

    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String newfile = "";
        while(StdIn.hasNextChar()){
            char newChar = StdIn.readChar();
            newfile += encodings[newChar];
        }
        
        writeBitString(encodedFile,newfile);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    // private String dcode(String encodedFile, String decodedFile, TreeNode Position){
    //     if(encodedFile.length() == 0) return decodedFile;
    //     if(Position.getData().getCharacter() != null){
    //         char newChar = Position.getData().getCharacter();
    //         decodedFile += newChar;
    //         dcode(encodedFile, decodedFile, huffmanRoot);
    //     }
    //     int newCode = (int)encodedFile.charAt(0);
    //     if(newCode == 1) dcode(encodedFile.substring(1),decodedFile, Position.getRight());
    //     else dcode(encodedFile.substring(1), decodedFile, Position.getLeft());
    //     return decodedFile;
    // }
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);
        String newS= readBitString(encodedFile);
        TreeNode ptr = huffmanRoot;
        for(int i = 0; i < newS.length();i++){
            String s = "" + newS.charAt(i);
            int newCode = Integer.parseInt(s);
            if(newCode == 1) {ptr = ptr.getRight();}
            else {ptr = ptr.getLeft();}
            if(ptr.getData().getCharacter() != null){
                char newChar = ptr.getData().getCharacter();
                StdOut.print(newChar);
                ptr = huffmanRoot;            
        }
    }
	/* Your code goes here */
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
