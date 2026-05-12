/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package degenmatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Ivan Almada
 */
public class DegenMatrix {
    
    public static String programVersion = "1.0.0";
    
    public static String pathSeparator = File.separator;
    
    public static String elementSeparator = "  ";
    
    public static String newLine = "\n";
    
    protected static String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
    
    protected static int numberOfLines = 102 * 256;
    
    protected static int numberOfColumns = 16;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Running degenmatrix on: " + getCurrentDirectory());
        String prefix = "ESOCEP";
        String extension = ".DAT";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(getCurrentDirectory() + pathSeparator + "EG.TXT"));
            BufferedWriter writer1 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "F1" + extension));
            BufferedWriter writer2 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "F3" + extension));
            BufferedWriter writer3 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "C3" + extension));
            BufferedWriter writer4 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "P3" + extension));
            BufferedWriter writer5 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "O1" + extension));
            BufferedWriter writer6 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "F7" + extension));
            BufferedWriter writer7 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "T3" + extension));
            BufferedWriter writer8 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "T5" + extension));
            BufferedWriter writer9 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "F2" + extension));
            BufferedWriter writer10 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "F4" + extension));
            BufferedWriter writer11 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "C4" + extension));
            BufferedWriter writer12 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "P4" + extension));
            BufferedWriter writer13 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "O2" + extension));
            BufferedWriter writer14 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "F8" + extension));
            BufferedWriter writer15 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "T4" + extension));
            BufferedWriter writer16 = new BufferedWriter(new FileWriter(getCurrentDirectory() + pathSeparator + prefix + "T6" + extension));
            
            for (int i = 0; i < numberOfLines; i++) {
                String line = reader.readLine();
                System.out.println(line);
                
                String[] columns = line.split(elementSeparator);
                
                if (columns.length != numberOfColumns) {
                    System.out.println("Matrix does not have 16 colmuns.");
                }
                
                String value1 = columns[0];
                System.out.println("" + convert(value1));
                String value2 = columns[1];
                System.out.println("" + convert(value2));
                String value3 = columns[2];
                System.out.println("" + convert(value3));
                String value4 = columns[3];
                System.out.println("" + convert(value4));
                String value5 = columns[4];
                System.out.println("" + convert(value5));
                String value6 = columns[5];
                System.out.println("" + convert(value6));
                String value7 = columns[6];
                System.out.println("" + convert(value7));
                String value8 = columns[7];
                System.out.println("" + convert(value8));
                String value9 = columns[8];
                System.out.println("" + convert(value9));
                String value10 = columns[9];
                System.out.println("" + convert(value10));
                String value11 = columns[10];
                System.out.println("" + convert(value11));
                String value12 = columns[11];
                System.out.println("" + convert(value12));
                String value13 = columns[12];
                System.out.println("" + convert(value13));
                String value14 = columns[13];
                System.out.println("" + convert(value14));
                String value15 = columns[14];
                System.out.println("" + convert(value15));
                String value16 = columns[15];
                System.out.println("" + convert(value16));
                
                writer1.write("" + convert(value1) + newLine);
                writer2.write("" + convert(value2) + newLine);
                writer3.write("" + convert(value3) + newLine);
                writer4.write("" + convert(value4) + newLine);
                writer5.write("" + convert(value5) + newLine);
                writer6.write("" + convert(value6) + newLine);
                writer7.write("" + convert(value7) + newLine);
                writer8.write("" + convert(value8) + newLine);
                writer9.write("" + convert(value9) + newLine);
                writer10.write("" + convert(value10) + newLine);
                writer11.write("" + convert(value11) + newLine);
                writer12.write("" + convert(value12) + newLine);
                writer13.write("" + convert(value13) + newLine);
                writer14.write("" + convert(value14) + newLine);
                writer15.write("" + convert(value15) + newLine);
                writer16.write("" + convert(value16) + newLine);
            }
            
            reader.close();
            writer1.close();
            writer2.close();
            writer3.close();
            writer4.close();
            writer5.close();
            writer6.close();
            writer7.close();
            writer8.close();
            writer9.close();
            writer10.close();
            writer11.close();
            writer12.close();
            writer13.close();
            writer14.close();
            writer15.close();
            writer16.close();
            
        } catch (FileNotFoundException fnfe) {
            System.out.println("File not found on DegenMatrix");
        } catch (IOException ioe) {
            System.out.println("I/O Exception on DegenMatrix");
        } finally {
            System.out.println("Process finished!");
        }

    }
    
    private static int convert(String pointString) {
        int error = 0;
        if (pointString == null) {
            System.out.println("Point string was null");
            return error;
        }
        try {
            double point = Double.parseDouble(pointString);
            double converted = point * 10;
            int integer = (int)converted;
            return integer;
        } catch (NumberFormatException nfe) {
            System.out.println("Error reading point: " + pointString);
        }
        return error;
    }

}
