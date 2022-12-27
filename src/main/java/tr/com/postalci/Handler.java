package tr.com.postalci;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Object, String> {

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String handleRequest(Object input, Context context) {

        HashMap<String, Object> inputMap = getInputMap(input);
        LambdaLogger logger = context.getLogger();

        // raw event logs
        logger.log("EVENT: " + gson.toJson(inputMap));
        logger.log("EVENT TYPE: " + input.getClass());

        // process input
        String body = (String) inputMap.get("body");
        logger.log("BODY: " + body);
        HashMap<String, String> bodyMap = gson.fromJson(body, HashMap.class);

        HashMap<String, String> response = new HashMap<>();

        try {
            validateBody(bodyMap, logger);
            String emailResponse = sendEmail(body, logger);
            response.put("message", emailResponse);
            return gson.toJson(response);

        } catch (Exception e) {

            response.put("errorMessage", "cannot send email error:" + e.getMessage());
            return gson.toJson(response);
        }
        // process input
    }

    private HashMap<String, Object> getInputMap(Object input) {
        if (input instanceof String) {
            return gson.fromJson((String) input, HashMap.class);
        }
        return (HashMap<String, Object>) input;
    }

    private void validateBody(Map<String, String> bodyMap, LambdaLogger logger) {
        if (bodyMap.get("sender") == null) {
            throw new IllegalArgumentException("sender cannot be null");
        }
        if (bodyMap.get("recipients") == null) {
            throw new IllegalArgumentException("recipients cannot be null");
        }
        if (bodyMap.get("subject") == null) {
            throw new IllegalArgumentException("subject cannot be null");
        }

        String bodyHTML = bodyMap.get("bodyHTML");
        if (bodyHTML == null) {
            throw new IllegalArgumentException("bodyHTML cannot be null");
        }

        logger.log("processing body HTML " + bodyHTML);

        Tidy tidy = new Tidy();
        InputStream htmlStream = new ByteArrayInputStream(bodyHTML.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        tidy.parse(htmlStream, outputStream);

        int parseErrors = tidy.getParseErrors();
        if (parseErrors > 0) {
            throw new IllegalArgumentException("there are " + parseErrors + " HTML syntax errors in bodyHTML");
        }
    }

    private String sendEmail(String body, LambdaLogger logger) {
        logger.log("start creating lambda client");
        AWSLambda lambdaClient = AWSLambdaClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
        logger.log("lambda client is created");

        String htmlSyntaxCheckerLambda = "emailsender";

        InvokeRequest invokeRequest = new InvokeRequest();
        invokeRequest.setFunctionName(htmlSyntaxCheckerLambda);
        invokeRequest.setPayload(body);

        logger.log("invoking lambda " + htmlSyntaxCheckerLambda);
        ByteBuffer payload = lambdaClient.invoke(invokeRequest).getPayload();

        String response = new String(payload.array());
        logger.log("received payload " + response);

        return response;
    }
}
