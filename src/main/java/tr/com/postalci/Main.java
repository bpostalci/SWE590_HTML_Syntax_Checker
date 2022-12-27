package tr.com.postalci;

import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Tidy tidy = new Tidy();
        String html = "<aʬ></aʬ>";
        InputStream htmlStream = new ByteArrayInputStream(html.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        tidy.parse(htmlStream, outputStream);
        System.out.println(tidy.getParseErrors());
    }
}