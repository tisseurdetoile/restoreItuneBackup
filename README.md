restoreItuneBackup
==================

Restore Itunes permet de restaurer les fichiers de vos backup Itunes.


Installation
-----------

### Depuis GIT


### En recuperant le fichier zip
A venir

Usage
-----

Afficher la liste des fichier contenu dans le backup

    java -jar "restoreItuneBackup.jar"  --itunebackdir [REP DU BACKUP ITUNES]

recupere les images contenu dans Media/DCIM/

    java -jar "restoreItuneBackup.jar"  --itunebackdir [REP DU BACKUP ITUNES] --destdir [REP OU RESTAURER]

recupere les fichier ayant pour prefix Mon/Prefix

    java -jar "restoreItuneBackup.jar"  --itunebackdir [REP DU BACKUP ITUNES] --destdir [REP OU RESTAURER] --startwith Mon/Prefix

affiche l'aide

        java -jar "restoreItuneBackup.jar" -h

affiche la version 

        java -jar "restoreItuneBackup.jar" -v