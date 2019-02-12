import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;

public class TikaSkeleton {

    List<String> keywords;
    PrintWriter logfile;
    int num_keywords, num_files, num_fileswithkeywords;
    Map<String, Integer> keyword_counts;
    Date timestamp;

    /**
     * constructor
     * DO NOT MODIFY
     */
    public TikaSkeleton() {
        keywords = new ArrayList<String>();
        num_keywords = 0;
        num_files = 0;
        num_fileswithkeywords = 0;
        keyword_counts = new HashMap<String, Integer>();
        timestamp = new Date();
        try {
            logfile = new PrintWriter("log.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * destructor
     * DO NOT MODIFY
     */
    protected void finalize() throws Throwable {
        try {
            logfile.close();
        } finally {
            super.finalize();
        }
    }

    /**
     * main() function
     * instantiate class and execute
     * DO NOT MODIFY
     */
    public static void main(String[] args) {
        TikaSkeleton instance = new TikaSkeleton();
        instance.run();
    }

    /**
     * execute the program
     * DO NOT MODIFY
     */
    private void run() {

        // Open input file and read keywords
        try {
            BufferedReader keyword_reader = new BufferedReader(new FileReader("keywords.txt"));
            String str;
            while ((str = keyword_reader.readLine()) != null) {
                keywords.add(str);
                num_keywords++;
                keyword_counts.put(str, 0);
            }
            keyword_reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Open all pdf files, process each one
        File pdfdir = new File("./tikadataset");
        File[] pdfs = pdfdir.listFiles(new PDFFilenameFilter());
        for (File pdf : pdfs) {
            num_files++;
            processfile(pdf);
        }

        // Print output file
        try {
            PrintWriter outfile = new PrintWriter("output.txt");
            outfile.print("Keyword(s) used: ");
            if (num_keywords > 0) outfile.print(keywords.get(0));
            for (int i = 1; i < num_keywords; i++) outfile.print(", " + keywords.get(i));
            outfile.println();
            outfile.println("No of files processed: " + num_files);
            outfile.println("No of files containing keyword(s): " + num_fileswithkeywords);
            outfile.println();
            outfile.println("No of occurrences of each keyword:");
            outfile.println("----------------------------------");
            for (int i = 0; i < num_keywords; i++) {
                String keyword = keywords.get(i);
                outfile.println("\t" + keyword + ": " + keyword_counts.get(keyword));
            }
            outfile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process a single file
     * <p>
     * Here, you need to:
     * - use Tika to extract text contents from the file
     * - search the extracted text for the given keywords
     * - update num_fileswithkeywords and keyword_counts as needed
     * - update log file as needed
     *
     * @param f File to be processed
     */
    private void processfile(File f) {

        /***** YOUR CODE GOES HERE *****/

        // to update the log file with information on the language, author, type, and last modification date implement
        updatelogMetaData(f.getName());

        Tika tika = new Tika();

        String text = "";

        try {
            text = tika.parseToString(f);
        } catch (IOException | TikaException e) {
            e.printStackTrace();
        }

        boolean keywordFoundInFile = false;
        for (String keyword : keywords) {
            Pattern p = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);

            int keyword_count = 0;
            while (m.find()) {
                keyword_count++;
            }

            int new_value = keyword_counts.get(keyword) + keyword_count;
            keyword_counts.put(keyword, new_value);
            if (keyword_count > 0) {
                // to update the log file with a search hit, use:
                updatelogHit(keyword, f.getName());
                keywordFoundInFile = true;
            }
        }
        if (keywordFoundInFile) {
            num_fileswithkeywords++;
        }


    }

    private void updatelogMetaData(String filename) {
        logfile.println("\n\n -- " + " data on file \"" + filename + "\"");

        /***** YOUR CODE GOES HERE *****/

        File file = new File("./tikadataset/" + filename);
        Tika tika = new Tika();

        String text = null;
        try {
            text = tika.parseToString(file);
        } catch (IOException | TikaException e) {
            e.printStackTrace();
        }

        LanguageDetector textLangDetector = new OptimaizeLangDetector().loadModels();
        LanguageResult languageResult = textLangDetector.detect(text);
        String language = languageResult.getLanguage();
        logfile.println("Language: " + language);


        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Metadata metadata = new Metadata();

        try {
            tika.parse(inputStream, metadata);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String author = "";
        String[] authors = metadata.getValues("Author");
        for (String a : authors) {
            author += a;
        }
        logfile.println("Author: " + author);


        String fileType = "";
        try {
            fileType = tika.detect(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logfile.println("File type: " + fileType);

        String lastModified = "";
        String[] lastModifiedArray = metadata.getValues("Last-Modified");
        for (String lastMod : lastModifiedArray) {
            lastModified += lastMod;
        }
        logfile.println("Last modified: " + lastModified);

        logfile.println();
        logfile.flush();
    }

    /**
     * Update the log file with search hit
     * Appends a log entry with the system timestamp, keyword found, and filename of PDF file containing the keyword
     * DO NOT MODIFY
     */
    private void updatelogHit(String keyword, String filename) {
        timestamp.setTime(System.currentTimeMillis());
        logfile.println(timestamp + " -- \"" + keyword + "\" found in file \"" + filename + "\"");
        logfile.flush();
    }

    /**
     * Filename filter that accepts only *.pdf
     * DO NOT MODIFY
     */
    static class PDFFilenameFilter implements FilenameFilter {
        private Pattern p = Pattern.compile(".*\\.pdf", Pattern.CASE_INSENSITIVE);

        public boolean accept(File dir, String name) {
            Matcher m = p.matcher(name);
            return m.matches();
        }
    }
}
