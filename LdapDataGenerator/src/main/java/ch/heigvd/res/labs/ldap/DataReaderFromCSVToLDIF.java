package ch.heigvd.res.labs.ldap;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This program is used to parse a CSV file and output a LDIF file for LDAP import.
 * 
 * @author ValentinMinder (and paranoodle)
 */
public class DataReaderFromCSVToLDIF {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PrintWriter bw = null;
        BufferedReader in;
        BufferedReader base;
        try {
            bw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("users.ldif"), "UTF-8"));
            in = new BufferedReader(new FileReader("users.csv"));
            base = new BufferedReader(new FileReader("base.ldif"));
            
            // copy the base file (headers) in the output file.
            String line;
            while(null != (line = base.readLine())) {
                bw.println(line);
            }
            
            // prepared set of department (for dynamic groups)
            Set<String> departements = new TreeSet<>();
            
            // read the CSV
            final int nbColumn = 8;
            String[] token;
            while(null != (line = in.readLine())) {
                // CSV = comma-separated values = split by commas
                token = line.split(",");
                
                // we ignore a row without all field
                if (token.length != nbColumn) {
                    continue; 
                }
                // trimming all fields
                for(int i = 0; i < nbColumn; ++i) {
                    token[i] = token[i].trim();
                }
                
                // fields (0-7)
                // EID_100001, De Villiers, Fabienne, (024) 777 486 820, fabienne.devilliers@heig-vd.ch, FEMALE, HEG, Professeur
                // UID, surname(sn), givenName, telephone, mail, sex, departement (number), fonction (employeeType)
                bw.println();
                bw.print("dn: uid=" + token[0]);
                bw.println(",ou=People,dc=contacts,dc=heigvd,dc=ch");
                bw.println("objectClass: top");
                bw.println("objectClass: person");
                bw.println("objectClass: organizationalPerson");
                bw.println("objectClass: inetOrgPerson");
                bw.println("uid: " + token[0]);
                bw.println("sn: " + token[1]); // surname
                bw.println("givenName: " + token[2] ); // given name
                bw.println("cn: "  + token[2] + " " + token[1]); // commun name
                bw.println("telephoneNumber: " + token[3]);
                bw.println("mail: " + token[4]);
                bw.println("departmentNumber: " + token[6] );
                // adding departement to set of department
                departements.add(token[6]);
                bw.println("employeeType: " + token[7]);
                
                // TODO: sex better than this description
                //bw.println("description: SEX=" + token[5]);
            }

            // handling departments
            for(String dpt : departements) {
                bw.println();
                dpt = dpt.trim();
                bw.print("dn: cn=Dpt" + dpt);
                bw.println(",ou=Departments,dc=contacts,dc=heigvd,dc=ch");
                bw.println("cn: Dpt" + dpt);
                bw.println("objectClass: top");
                bw.println("objectClass: groupOfURLs");
                bw.println("ou: Departments");
                bw.print("memberURL: ldap:///ou=People,dc=contacts,dc=heigvd,dc=ch??sub?");
                bw.println("departmentNumber=" + dpt);
            }
            bw.println();
            
        } catch (IOException ex) {
            Logger.getLogger(DataGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            bw.close();
        }
    }
}
