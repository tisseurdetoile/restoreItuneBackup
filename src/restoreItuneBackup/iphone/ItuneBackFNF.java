/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package restoreItuneBackup.iphone;

import java.io.File;
import java.io.FilenameFilter;

/**
 * FileNameFilter pour les backups Itunes
 * @author tisseurDeToile
 */
public class ItuneBackFNF implements FilenameFilter {

   private final String[] dataBaseFileNames = { "Manifest.mbdb", "Manifest.mbdx"};

    public boolean accept(File file, String string) {
        for (int ni = 0; ni < dataBaseFileNames.length; ni++) {
            if (dataBaseFileNames[ni].equals(string)) {
                return true;
            }
        }

        return false;
    }
  

  

}
