import java.io.IOException;

/**
 * Specify the EDN source file as the first command line
 * argument, and the directory you want the resulting CSV
 * files to end up as the second command line argument.
 */
public class Main {
    public static void main(String[] args) throws IOException {
	if (args.length < 2) {
	    System.out.println("Usage: java Main <logfile.edn> <csv_file_directory>");
	    System.exit(1);
	}
        //Make instance of the converter
        Converter Converter = new Converter();
        //To convert edn file call the convert method which takes two arguments two arguments: sourceFile and destination for the output files.
        Converter.convert(args[0], args[1]);
    }
}
