/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package restoreItuneBackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collections;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.opts.BooleanOption;
import org.kohsuke.args4j.opts.StringOption;
import restoreItuneBackup.iphone.FileLister;
import restoreItuneBackup.iphone.ItuneBackFNF;

/**
 *
 * @author tisseurDeToile
 */
public class Main {

    private boolean isError = false;
    private boolean onlyList = true;
    public BooleanOption viewHelp = new BooleanOption("-h");
    public BooleanOption viewVersion = new BooleanOption("-v");
    public StringOption str = new StringOption("--itunebackdir");
    public StringOption destDir = new StringOption("--destdir");
    public StringOption fileStartPattern = new StringOption("--startwith", DEFAULT_FILE_PREFIX);

    public static void main(String[] args) throws IOException {
        new Main().doMain(args);
    }

    /**
     * @param args the command line arguments
     */
    public void doMain(String[] args) {
        // TODO code application logic here

        CmdLineParser parser = new CmdLineParser();
        parser.addOptionClass(this);
        String readFileName;

        try {
            parser.parse(args);

            if (str.value == null || str.value.isEmpty()) {
                System.out.println("Le repertoire n'est pas defini");
                System.exit(10);
            }

            if (destDir.value == null || destDir.value.isEmpty()) {
                onlyList = true;
            } else {
                onlyList = false;
            }

            File directory = new File(str.value);


            if (!directory.isDirectory() || !directory.canRead()) {
                System.out.println("Le repertoire n'est pas valide ou inaccessible");
                isError = true;
            }

            FilenameFilter fnf = new ItuneBackFNF();

            File[] backDBFiles = directory.listFiles(fnf);

            if (backDBFiles == null || backDBFiles.length != 2) {
                System.out.println("Le repertoire ne contient pas les fichiers necessaire à la recuperation des données");
                isError = true;

            }

            if (isError) {
                System.exit(10);
            }

            System.out.println("Repertoire de la sauvegarde : " + directory.getAbsolutePath());

            FileLister f = new FileLister(directory);

            if (onlyList) {
                System.out.println("affiche");
                List filesList = f.getFilesNames();
                if (filesList != null) {
                    Collections.sort(filesList);

                    for (int ni = 0; ni < filesList.size(); ni++) {
                        System.out.println(filesList.get(ni));
                    }
                }

            } else {
                File directoryBackup = new File(destDir.value);

                if (!directoryBackup.isDirectory() || !directoryBackup.canWrite()) {
                    System.out.println("Le repertoire de destination n'est pas accessible/en ecriture");
                    System.exit(15);
                }

                FileLister.FileIterator fileIterator = f.getIterator();
                while (fileIterator.hasNext()) {

                    readFileName = fileIterator.next();

                    if (readFileName.startsWith(fileStartPattern.value)) {

                        File readFile = fileIterator.getCurrentFile();

                        if (readFile != null && readFile.exists() && readFile.canRead()) {
                            File tmpFile = new File(destDir.value + "/" + readFileName.replace("/", "_"));
                            if (tmpFile.createNewFile()) {
                                System.out.print("+");
                                copy(readFile, tmpFile);
                            }

                        }

                    }
                }

            }
            System.exit(1);

        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get an exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            return;
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    static final String VERSION = "0.1";
    static final String DEFAULT_FILE_PREFIX = "Media/DCIM/";
}
