package bdd;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import ours.Application;

import static org.testng.AssertJUnit.fail;

/**
 * @author mvolkhart
 */
public class ApplicationFlow {

    Application application;

    @Given("^a command line terminal$")
    public void a_command_line_terminal() {
        assert true;
    }

    @When("^I run the application$")
    public void I_run_the_application() throws Throwable {
        application = new Application();
    }

    @Then("^I will be greeted with the message \"([^\"]*)\"$")
    public void I_will_be_greeted_with_the_message(String expectedMessage) throws Throwable {
//        assertEquals(application.output().next(), expectedMessage);
        fail("unimplemented");
    }

    @And("^a list of menu options.$")
    public void a_list_of_menu_options() throws Throwable {
        fail("unimplemented");
    }
}
