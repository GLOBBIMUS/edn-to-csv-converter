import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        //Make instance of the converter
        Converter Converter = new Converter();

        //To convert edn file call the convert method which takes two arguments two arguments: sourceFile and destination for the output files.
        Converter.convert("your_edn_file", "target");
    }
}
