package viacep;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.util.logging.Logger;


public class StepDefinitions {
    private static final Logger logger = Logger.getLogger(StepDefinitions.class.getName());
    private int responseCode;
    private String zipCode;
    private String uf;
    private String city;
    private String streetAddress;
    private JSONObject JSONResponse;
    private JSONArray JSONArrayResponse;

    @Given("^the user insert a valid zip code like (.*)$")
    @Given("^the user insert a zip code with invalid format like (.*)$")
    @Given("^the user insert a zip code that does not exist at the Post Office database like (.*)$")
    public void theUserInsertsAValidZipCodeLikeX(String zipCode) {
        this.zipCode = zipCode;
    }

    @Given("^the user insert the UF as (.*), the city as (.*), and the street address as (.*)$")
    public void theUserInsertTheUFAsRSTheCityAsGravataiAndTheStreetAddressAsBarroso(String uf, String city, String streetAddress) {
        this.uf = uf;
        this.city = city;
        this.streetAddress = streetAddress;
    }

    @When("^the service is consulted using the (.*)$")
    public void theServiceIsConsultedUsingTheX(String method) throws IOException, ParseException {
        URL url;
        switch (method){
            case "zip code":
                logger.info(String.format("Consulting zip code %s using https://viacep.com.br/ws/%s/json/", zipCode, zipCode));
                url = new URL(String.format("https://viacep.com.br/ws/%s/json/", zipCode));
                break;
            case "address":
                logger.info(String.format("Consulting zip code using address https://viacep.com.br/ws/%s/%s/%s/json/", uf, city, streetAddress));
                url = new URL(String.format("https://viacep.com.br/ws/%s/%s/%s/json/", uf, city, streetAddress));
                break;
            default:
                throw new IllegalStateException("Unexpected method type: " + method);
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONParser parser = new JSONParser();
            if (method.equals("zip code")) JSONResponse = (JSONObject) parser.parse(content.toString());
            else JSONArrayResponse = (JSONArray) parser.parse(content.toString());

            logger.info("API response: \n" + content);
        }
    }

    @Then("the zip code \\(CEP), street address, complement, neighborhood, locality, UF and IBGE is returned")
    public void the_zip_code_cep_street_address_complement_neighborhood_locality_uf_and_ibge_is_returned() {
        Assert.assertTrue("Zip code is not present", JSONResponse.get("cep") != null);
        Assert.assertTrue("Street address is not present", JSONResponse.get("logradouro") != null);
        Assert.assertTrue("Complement is not present", JSONResponse.get("complemento") != null);
        Assert.assertTrue("Neighborhood is not present", JSONResponse.get("bairro") != null);
        Assert.assertTrue("Locality is not present", JSONResponse.get("localidade") != null);
        Assert.assertTrue("UF is not present", JSONResponse.get("uf") != null);
        Assert.assertTrue("IBGE is not present", JSONResponse.get("ibge") != null);
        Assert.assertTrue("GIA is not present", JSONResponse.get("gia") != null);
        Assert.assertTrue("DDD is not present", JSONResponse.get("ddd") != null);
        Assert.assertTrue("SIAFI is not present", JSONResponse.get("siafi") != null);
    }

    @Then("a list of zip codes that matches the parameters are returned")
    public void aListOfZipCodesThatMatchesTheParametersAreReturned() {
        Assert.assertTrue("The response doesn't contain any results", !JSONArrayResponse.isEmpty());
        for (Object address : JSONArrayResponse){
            Assert.assertTrue(String.format("Expected UF: %s. Received: %s", uf, ((JSONObject) address).get("uf")),
                    ((JSONObject) address).get("uf").equals(uf));

            Assert.assertTrue(String.format("Expected city: %s. Received: %s", city, ((JSONObject) address).get("localidade")),
                    Normalizer.normalize((CharSequence) ((JSONObject) address).get("localidade"),
                    Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").equals(city));

            Assert.assertTrue(String.format("Expected street address: %s. Received: %s", streetAddress, ((JSONObject) address).get("logradouro")),
                    String.valueOf(((JSONObject) address).get("logradouro")).contains(streetAddress));
        }
    }

    @Then("the correct error message is raised")
    public void theCorrectErrorMessageIsRaised() {
        Assert.assertTrue("The API found a nonexistent zip code", JSONResponse.get("erro").equals(true));
    }

    @Then("the API raises an HTTP error code 400")
    public void theAPIRaisesAnErrorMessage() {
        Assert.assertTrue(String.format("The API raised the incorrect HTTP code. Expected: %s. Received: %s",
                HttpURLConnection.HTTP_BAD_REQUEST, responseCode), responseCode == HttpURLConnection.HTTP_BAD_REQUEST);
        logger.info("HTTP error code 400 raised as expected");
    }
}
