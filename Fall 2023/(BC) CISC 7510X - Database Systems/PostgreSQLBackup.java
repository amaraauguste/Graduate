/** Program to perform a database backup.
 *
 * Prompts user for PostgreSQL database info:
 *  - database name
 *  - user
 *  - password
 *  - table to backup
 * 
 * Backs up a table from a database into a .csv.gz file
 * (comma delimited, gzip compressed [do not use database specific binary formats])
 * 
 */
package pkg7810x;

import org.bouncycastle.openpgp.*;
import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public class PostgreSQLBackup {

    public static String[] getInfo() {
        String[] info = new String[4];
        Scanner input = new Scanner(System.in);
        System.out.print("Enter database name: ");
        String databaseName = input.next(); //database, e.g. "cisc7510x"
        info[0] = databaseName;
        System.out.print("Enter user: ");
        String username = input.next(); //user, e.g. "amara"
        info[1] = username;
        System.out.print("Enter password: ");
        String password = input.next(); //password, e.g. "12345"
        info[2] = password;
        System.out.print("Enter table for backup: ");
        String tableName = input.next(); //table, e.g. "store.customer"
        info[3] = tableName;
        return info;
    }

    public static void main(String[] args) throws IOException, PGPException, SQLException {

        String[] info = getInfo();

        // Connection details 
        String jdbcUrl = "jdbc:postgresql://localhost:5432/" + info[0]; 
        String username = info[1];
        String password = info[2];
        String tableName = info[3];

        // Establish the database connection 
        try (
                 Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            // Export the table to a CSV file
            File outputFile = new File(tableName + ".csv");
            FileWriter fileWriter = null;
            PrintWriter printWriter = null;
            CopyManager copyManager = null;
            try {
                fileWriter = new FileWriter(outputFile);
                printWriter = new PrintWriter(fileWriter);
                copyManager = new CopyManager((BaseConnection) conn);
                copyManager.copyOut("COPY " + tableName + " TO STDOUT WITH CSV HEADER", printWriter);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (printWriter != null) {
                        printWriter.close();
                    }
                    if (fileWriter != null) {
                        fileWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Compress the CSV file using GZIP
            try (
                     FileInputStream in = new FileInputStream(outputFile);  FileOutputStream out = new FileOutputStream(tableName + ".csv.gz");  GZIPOutputStream gzipOut = new GZIPOutputStream(out)) {

                byte[] buffer = new byte[1024];
                int len;

                while ((len = in.read(buffer)) > 0) {
                    gzipOut.write(buffer, 0, len);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
