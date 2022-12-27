package tr.com.postalci;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Handler implements RequestHandler<String, Integer> {
    @Override
    public Integer handleRequest(String bodyHTML, Context context) {
        if (bodyHTML == null) {
            return 1;
        }

        context.getLogger().log("processing body HTML " + bodyHTML);

        Tidy tidy = new Tidy();
        InputStream htmlStream = new ByteArrayInputStream(bodyHTML.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        tidy.parse(htmlStream, outputStream);

        int parseErrors = tidy.getParseErrors();

        context.getLogger().log("parse errors: " + parseErrors);

        return parseErrors;
    }
}
