/** Amara Auguste
 * Fall 2023 
 * CISC 7510X HW #4
 * 
 * HW 4: Write a command line program to "join" .csv files.
 * Use any programming language you're comfortable with (Python suggested). 
 * Your program should work similarly to the unix "join" utility 
 * (google for it). Unlike the unix join, your program will not 
 * require files to be sorted on the key. 
 * Your program must also accept the "type" of join to use --- 
 * merge join, inner loop join, or hash join, etc. 
 * Assume that first column is the join key --- 
 * or you can accept the column number as parameter (like unix join command).
 * 
 * order to remember to run: 
 * java CSVJoin <joinType> <file1> <file2> > <outputFileName> --key <keyColumn>
 * if key not specified then default key is first column of both files
 * if output file not specified then print to screen
 */

package pkg7810x;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CSVJoin {

    static PrintStream output = new PrintStream(System.out);

    public static void main(String[] args) throws FileNotFoundException, IOException {

        String joinType = args[0]; //inner loop, hash, sort merge, etc.
        String file1 = args[1]; //first CSV file
        String file2 = args[2]; //second CSV file
        int key = 0;
        if (args.length > 3) { //if more than: <joinType> <file1> <file2>
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(">")) { //if user wants output to new file
                    output = new PrintStream(args[i + 1]); //new file name
                }
                if (args[i].equals("--key")) { //if user wants to specify key
                    key = Integer.parseInt(args[i + 1]); //key column
                }
            }

        }

        switch (joinType) {
            case "inner_loop":
                innerLoopJoin(file1, file2, key);
                break;
            case "hash":
                hashJoin(file1, file2, key);
                break;
            case "merge":
                sortMergeJoin(file1, file2, key);
                break;

            default:
                System.out.println("Invalid type! Supported types: merge, inner_loop, hash");
                break;
        }

        output.close();
    }

    /*A simple inner-loop join algorithm reads rows from the first table in a 
    loop one at a time, passing each row to a nested loop that processes 
    the next table in the join. 
    This process is repeated as many times as 
    there remain tables to be joined.*/
    
    public static void innerLoopJoin(String file1, String file2, int key) throws IOException {
        List<String> f2Lines = new ArrayList<>();

        Scanner f1 = new Scanner(new File(file1));
        Scanner f2 = new Scanner(new File(file2));

        // Skip header lines (if any)
        f1.nextLine();
        f2.nextLine();

        while (f2.hasNextLine()) {
            f2Lines.add(f2.nextLine()); //load all data from file2 into list for access
        }

        while (f1.hasNextLine()) { //for (a <-- values in table1){
            String line1 = f1.nextLine();
            String[] row1 = line1.split(",");

            for (int j = 0; j < f2Lines.size(); j++) {//for (b <-- values in table2)
                String line2 = f2Lines.get(j);

                String[] row2 = line2.split(",");

                if (row1[key].equals(row2[key])) { //if (a.key == b.key)
                    //output(a,b)
                    for (int i = 0; i < row1.length; i++) {
                        output.print(row1[i]);
                        output.print(", ");
                    }

                    for (int k = 0; k < row2.length; k++) {
                        if (k == key) {
                        } else {
                            output.print(row2[k]);
                            if (k < row2.length - 1) {
                                output.print(", ");
                            }
                        }
                    }

                    output.println();
                }

            }

        }

    }

    /*A hash join can also be used when there are one or more indexes 
    that can be used for single-table predicates.
    A hash join is usually faster than and is intended to be used 
    in such cases instead of the block nested loop algorithm*/
    
    public static void hashJoin(String file1, String file2, int key) throws IOException {
        Map<String, List<String[]>> file2Map = new HashMap<>();

        Scanner f1 = new Scanner(new File(file1));
        Scanner f2 = new Scanner(new File(file2));

        // Skip header lines (if any)
        f1.nextLine();
        f2.nextLine();

        String line2;
        while ((f2.hasNextLine())) { //for (b <-- values in table2)
            line2 = f2.nextLine();
            String[] row2 = line2.split(",");
            String keyValue = row2[key];
            if (!file2Map.containsKey(keyValue)) {
                file2Map.put(keyValue, new ArrayList<>());
            }
            file2Map.get(keyValue).add(row2); //hash(b.key).add(key)
        }

        String line1;
        while ((f1.hasNextLine())) { //for(a <-- values in table1)
            line1 = f1.nextLine();
            String[] row1 = line1.split(",");
            String keyValue = row1[key];

            if (file2Map.containsKey(keyValue)) { //for (b <-- values in hash[a.key])
                //output(a,b)
                for (int j = 0; j < row1.length; j++) {
                    output.print(row1[j]);
                    output.print(", ");
                }
                List<String[]> joinRows = file2Map.get(keyValue);
                for (int i = 0; i < joinRows.size(); i++) {
                    for (String[] joinRow : joinRows) {
                        for (int k = 0; k < joinRow.length; k++) {
                            if (k == key) {
                            } else {
                                output.print(joinRow[k]);
                                if (k < row1.length - 1) {
                                    output.print(", ");
                                }

                            }
                        }
                    }
                    output.println();
                }
            }
        }

    }

    /*The sort-merge join combines two sorted lists like a zipper. 
    Both sides of the join must be sorted by the join predicates and then 
    joined when a match is found*/
    
    public static void sortMergeJoin(String file1, String file2, int key) throws IOException {
        Scanner f1 = new Scanner(new File(file1));
        Scanner f2 = new Scanner(new File(file2));

        // Skip header lines (if any)
        f1.nextLine();
        f2.nextLine();

        List<String> f1Lines = new ArrayList<>();
        while (f1.hasNextLine()) {
            f1Lines.add(f1.nextLine()); //load all data from file1 into list for access
        }

        List<String> f2Lines = new ArrayList<>();
        while (f2.hasNextLine()) {
            f2Lines.add(f2.nextLine()); //load all data from file2 into list for access
        }

        //sort lists (modify for key) 
        Collections.sort(f1Lines);
        Collections.sort(f2Lines);

        //beginning of list(s)
        int idx = 0;
        String line1 = f1Lines.get(idx);
        String line2 = f2Lines.get(idx);

        while (!f1Lines.isEmpty() && !f2Lines.isEmpty()) { //while(a has records, b has records)

            String[] row1 = line1.split(",");
            String[] row2 = line2.split(",");

            String key1 = row1[key].trim();
            String key2 = row2[key].trim();

            //remove processed lines
            f1Lines.remove(line1);
            f2Lines.remove(line2);

            int compare = key1.compareTo(key2);

            if (compare == 0) { //if (a.key == b.key)
                //output(a,b)
                for (int i = 0; i < row1.length; i++) {
                    output.print(row1[i]);
                    output.print(", ");
                }
                for (int j = 0; j < row2.length; j++) {
                    if (j == key) {
                    } else {
                        output.print(row2[j]);
                        if (j < row2.length - 1) {
                            output.print(", ");
                        }
                    }
                }

                output.println();
                if (!f1Lines.isEmpty()) {
                    line1 = f1Lines.get(idx);
                }
                if (!f2Lines.isEmpty()) {
                    line2 = f2Lines.get(idx);
                }
            }
            if (compare < 0) { //else if (a.key < b.key)
                if (f1Lines.size() > 0) {
                    line1 = f1Lines.get(idx);
                }
            } else if (compare > 0) {
                if (f2Lines.size() > 0) {
                    line2 = f2Lines.get(idx);
                }
            }

        } 
    }

}
