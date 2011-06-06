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
    private boolean isExit = false;
    private boolean onlyList = true;
    private long nbfile = 0;
    public BooleanOption viewHelp = new BooleanOption("-h");
    public BooleanOption viewVersion = new BooleanOption("-v");
    public BooleanOption updateOnly = new BooleanOption("--update");
    public StringOption str = new StringOption("--itunebackdir");
    public StringOption destDir = new StringOption("--destdir");
    public StringOption fileStartPattern = new StringOption("--startwith", DEFAULT_FILE_PREFIX);
    CmdLineParser parser = new CmdLineParser();

    public static void main(String[] args) throws IOException {
        try {
            new Main().doMain(args);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showHelp() {
        System.out.println("showHelp");
    }

    private void showVersion() {
        System.out.println("showVersion");
    }

    private void showError() {
        System.out.println ("une erreur est survenue");

    }

    private void showErrorParamRepBack() {
        System.out.println ("le repertoire de destination n'est pas définie --itunebackdir");
    }
    /**
     * 
     * @param args
     * @return 
     */
    private int commonParseDispatch(String[] args) {
        parser.addOptionClass(this);

        try {
            parser.parse(args);

            if (viewHelp.value) {
                return PARAM_SHOW_HELP;
            }

            if (viewVersion.value) {
                return PARAM_SHOW_VERSION;
            }

            if (str.value == null || str.value.isEmpty()) {
                return ERR_PARAM_REPBACK;
            }

            if (destDir.value == null || destDir.value.isEmpty()) {
                this.onlyList = true;
            } else {
                this.onlyList = false;
            }

        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get an exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            return ERR_PARAM;
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }


        return PARAM_OK;
    }

    /**
     * @param args the command line arguments
     */
    public void doMain(String[] args) throws Exception {
        // TODO code application logic here


        int result = commonParseDispatch(args);

        switch (result) {
            case PARAM_SHOW_HELP:
                showHelp();
                isExit = true;
                break;

            case PARAM_SHOW_VERSION:
                showVersion();
                isExit = true;
                break;

            case ERR_PARAM:
                showError();
                isExit = true;

            case ERR_PARAM_REPBACK:
                showErrorParamRepBack();
                isExit = true;
        }

        if (isExit) {
             System.exit(10);
        }


        FileLister.OffsetFilename readFileInfo;

        File dirItunesBackup = new File(str.value);


        if (!dirItunesBackup.isDirectory() || !dirItunesBackup.canRead()) {
            System.out.println("Le repertoire n'est pas valide ou inaccessible");
            isError = true;
        }

        FilenameFilter fnf = new ItuneBackFNF();

        File[] backDBFiles = dirItunesBackup.listFiles(fnf);

        if (backDBFiles == null || backDBFiles.length != 2) {
            System.out.println("Le repertoire ne contient pas les fichiers necessaire à la recuperation des données");
            isError = true;

        }

        if (isError) {
            System.exit(10);
        }

        System.out.println("Repertoire de la sauvegarde : " + dirItunesBackup.getAbsolutePath());



        FileLister f = new FileLister(dirItunesBackup);

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
                System.out.println("Le repertoire de destination n'est pas accessible/en ecriture : " + destDir.value);
                System.exit(15);
            }

            FileLister.FileIterator fileIterator = f.getIterator();
            while (fileIterator.hasNext()) {

                readFileInfo = fileIterator.next();

                if (readFileInfo.getString().startsWith(fileStartPattern.value)) {

                    File readFile = f.getCurrentFile(readFileInfo.getOffset());

                    if (readFile != null && readFile.exists() && readFile.canRead()) {
                        File tmpFile = new File(destDir.value + "/" + readFileInfo.getString().replace("/", "_"));
                        if (tmpFile.createNewFile()) {
                            try {
                                System.out.println("+>" + readFileInfo.getString() + "-->" + tmpFile.getName());
                                copy(readFile, tmpFile);
                                this.nbfile++;
                            } catch (IOException e) {
                                Logger.getLogger(Main.class.getName()).log(Level.INFO, null, e);
                            }
                        }
                    }
                }
            }

            System.out.println("nombre de fichier copié :" + this.nbfile);

        }
        System.exit(1);

    }

    /**
     * 
     * @param src
     * @param dst
     * @throws IOException
     */
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
    static final String VERSION = "0.2";
    static final String DEFAULT_FILE_PREFIX = "Media/DCIM/";
    static final int ERR_PARAM = 101;
    static final int ERR_PARAM_REPBACK = 102;
    static final int PARAM_OK = 10;
    static final int PARAM_SHOW_HELP = 11;
    static final int PARAM_SHOW_VERSION = 12;
}
