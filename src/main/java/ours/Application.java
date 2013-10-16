package ours;

import au.com.bytecode.opencsv.CSVReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author mvolkhart
 */
public class Application {

    public Status load(File toImport) {
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(toImport));
        } catch (FileNotFoundException e) {
            return new Status(Status.FAIL, "File not found.");
        }
        try {
            List<String[]> rows = reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return new Status(Status.PASS, "File successfully imported.");
    }

    public View getView() {
        return View.MENU;
    }
}
