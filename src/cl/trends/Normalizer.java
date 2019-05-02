
package cl.trends;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by des01c7 on 30-04-19.
 */
public class Normalizer {

    static private final Logger logger = Logger.getLogger(Normalizer.class.getName());
    
    static final List<Integer> PERIODS = new ArrayList<Integer>(20);
    static final String ROOT = "DATOS/AreaTC-Presentaciones";
    static final String SEPARATOR = "/";
    
    public static void main(String... args) {
        for (int i = 2000; i < 2020; i++) {
            PERIODS.add(i);
        }
        createFirstLevel();
        List<File> files = getFirstLevelComplement();
        traverseFiles(files);
        traverseAndCleanFiles(files);
    }

    public static void traverseFiles(List<File> files) {
        for (File file : files) {
            File firstLevel = assertFirstLevel(file.getName());

            if (file.isDirectory()) {
                //System.out.println("Directory: " + file.getName());
                if(firstLevel != null) {
                    logger.log(Level.INFO, "Moviendo archivo " + file + " a: " + firstLevel);
                    moveDirectoryToFirstLevel(firstLevel, file);
                }
                else {
                    traverseFiles(Arrays.asList(file.listFiles())); // Calls same method again.
                }
            } else {
                if(firstLevel != null) {
                    logger.log(Level.INFO, "Moviendo archivo " + file + " a: " + firstLevel);
                    moveDirectoryToFirstLevel(firstLevel, file);
                }
                //System.out.println("File: " + file.getName());
            }
        }
    }

    public static void traverseAndCleanFiles(List<File> files) {
        for (File file : files) {

            if (file.isDirectory()) {
                if(file.list().length == 0) {
                    boolean deleted = file.delete();
                    if(deleted) {
                        logger.log(Level.INFO, "Directorio vacío eliminado");
                    }
                    else {
                        logger.log(Level.INFO, "Directorio vacío NO eliminado");
                    }
                }
                else {
                    traverseAndCleanFiles(Arrays.asList(file.listFiles()));
                }
            }
        }
    }
    
    static void createFirstLevel() {
        for (int period : PERIODS) {
            new File(ROOT + SEPARATOR + period).mkdirs();
        }
    }

    static List<File> getFirstLevel() {
        List<File> firstLevel = new ArrayList<>();

        for (File file : new File(ROOT).listFiles()) {

            try {
                String[] tokens = file.getName().split(SEPARATOR);

                if (PERIODS.contains(Integer.valueOf(tokens[tokens.length-1]))) {
                    firstLevel.add(file);
                }
            }
            catch (NumberFormatException e) {
                //logger.log(Level.INFO, "Archivo " + file.getName() + " no elegible para 1er nivel");
            }
        }

        return firstLevel;
    }

    static List<File> getFirstLevelComplement() {
        List<File> firstLevel = new ArrayList<>();

        for (File file : new File(ROOT).listFiles()) {

            try {
                String[] tokens = file.getName().split(SEPARATOR);

                if (PERIODS.contains(Integer.valueOf(tokens[tokens.length-1]))) {
                }
            }
            catch (NumberFormatException e) {
                firstLevel.add(file);
                //logger.log(Level.INFO, "Archivo " + file.getName() + " no elegible para 1er nivel");
            }
        }

        return firstLevel;
    }

    static File assertFirstLevel(String name) {

        for (File file : getFirstLevel()) {
            if(name.contains(file.getName())) {
                return file;
            }
        }
        return null;
    }

    static void moveDirectoryToFirstLevel(File path, File file) {
        String _path = path.getPath() + SEPARATOR + StringUtils.difference(path.getPath(), file.getPath());
        String _destination = "";
        String[] tokens = _path.split(SEPARATOR);

        for (int i = 0; i < tokens.length-1; ++i) {
            _destination = _destination + tokens[i] + SEPARATOR;
        }

        Path to = Paths.get(_destination);

        try {
            FileUtils.moveDirectoryToDirectory(file, new File(String.valueOf(Paths.get(String.valueOf(to)))), true);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                FileUtils.moveFileToDirectory(file, new File(String.valueOf(Paths.get(String.valueOf(to)))), true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        //file.renameTo(new File(path + SEPARATOR + StringUtils.difference(path.getPath(), file.getPath())));
        //file.renameTo(new File(path + SEPARATOR + file.getName()));
    }

}
