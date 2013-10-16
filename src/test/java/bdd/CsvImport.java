package bdd;

import au.com.bytecode.opencsv.CSVReader;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import ours.Application;
import ours.Status;
import ours.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author mvolkhart
 */
public class CsvImport {

    protected File csv;
    protected Status status;
    protected Application application;

    @Before
    public void beforeScenario() {
        csv = null;
        status = null;
        application = new Application();
    }

    @Given("^A file \"([^\"]*)\"$")
    public void A_file(String fileName) {
        csv = new File("src/test/resources", fileName);
        System.out.println(csv.getAbsolutePath());
    }

    @When("^I load the file$")
    public void I_load_the_file() {
        status = application.load(csv);
    }

    @And("^the file exists$")
    public void the_file_exists() throws Throwable {
        assertTrue(csv.exists());
    }

    @And("^the file is a valid CSV$")
    public void the_file_is_a_valid_CSV() throws IOException {
        CSVReader reader = new CSVReader(new FileReader(csv));
        reader.readAll();
    }

    @And("^the application returns me to the main menu.$")
    public void the_application_returns_me_to_the_main_menu() throws Throwable {
        assertEquals(application.getView(), View.MENU);
    }

    @And("^the file does not exist$")
    public void the_file_does_not_exist() throws Throwable {
        assertFalse(csv.exists());
    }

    @Then("^I get an error message$")
    public void I_get_an_error_message() {
        assertEquals(status.code(), Status.FAIL);
    }

    @And("^the error is \"([^\"]*)\"$")
    public void the_error_is(String expectedMessage) {
        assertEquals(status.message(), expectedMessage);
    }

    @And("^the file is an invalid csv$")
    public void the_file_is_an_invalid_csv() throws FileNotFoundException {
        CSVReader reader = new CSVReader(new FileReader(csv));
        try {
            reader.readAll();
            fail("Able to read entire file. Should have been invalid CSV");
        } catch (IOException e) {
            assert true;
        }
    }

    @And("^I get a success message, \"([^\"]*)\"$")
    public void I_get_a_success_message(String expectedMessage) throws Throwable {
        assertEquals(status.message(), expectedMessage);
    }

    @Then("^the application populates the database$")
    public void the_application_populates_the_database() throws Throwable {
        status = application.load(csv);

        // TODO make sure db has content
    }
}
