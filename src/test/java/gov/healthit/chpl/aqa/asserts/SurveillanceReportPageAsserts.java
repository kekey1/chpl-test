package gov.healthit.chpl.aqa.asserts;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import cucumber.api.java.en.Then;
import gov.healthit.chpl.aqa.pageObjects.SurveillanceReportPage;
import gov.healthit.chpl.aqa.stepDefinitions.Base;

/**
 * Class SurveillanceReportPageAsserts definition.
 */
public class SurveillanceReportPageAsserts extends Base {

    /**
     * Assert that CHPL Surveillance is the expected text.
     * @param expectedPageTitle is CHPL Surveillance
     */
    @Then("^I see \"([^\"]*)\" as the page title for the surveillance report page$")
    public void iSeePageTitle(final String expectedPageTitle) {
        WebElement link = SurveillanceReportPage.surveillancePageSubtitle(getDriver());
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView();", link);
        String actualPageTitle = SurveillanceReportPage.surveillancePageTitle(getDriver()).getText();
        assertEquals(expectedPageTitle, actualPageTitle);
    }

    /**
     * Assert that ACBs are expected to be displayed under available reports.
     * @param expectedACBs are Drummond Group, ICSA Labs, SLI Compliance & UL LLC
     */
    @Then("^I see available reports for \"([^\"]*)\"$")
    public void iSeeReportsAvailableFor(final String expectedACBs) {
        String actualACBs = SurveillanceReportPage.availableYearQuarterForAcbs(getDriver(), expectedACBs).getText();
        String[] acbTempArray = actualACBs.split("\n");
        StringBuffer buff = new StringBuffer();
        for (String string : acbTempArray) {
            buff.append(string.trim() + " ");
        }
        assertTrue(buff.toString().contains(expectedACBs));
    }

    /**
     * Assert that Year-Quarters are displayed for each ACBs.
     * @param expectedYearQuarter - Year Q1 Q2 Q3 Q4
     */
    @Then("^I see \"([^\"]*)\" listed for each ACB$")
    public void iSeeYearQuarterForEachACB(final String expectedYearQuarter) {
        String actualYearQuarter = SurveillanceReportPage.availableYearQuarterForAcbs(getDriver(), expectedYearQuarter).getText();
        assertTrue(actualYearQuarter.contains(expectedYearQuarter));
    }

    /**
     * Assert that page title is correct is for the Quarterly Reports page.
     * @param expectedPageTitle is Quarterly Report
     */
    @Then("^I see \"([^\"]*)\" for the quarterly surveillance report page$")
    public void iSeePageTitleForQuarterlySurveillanceReport(final String expectedPageTitle) {
        String actualPageTitle = SurveillanceReportPage.quarterlySurveillanceReportingTitle(getDriver(), expectedPageTitle).getText();
        assertTrue(actualPageTitle.contains(expectedPageTitle));
    }

    /**
     * Assert that subtitle is correct is for the Quarterly Reports page.
     * @param expectedSubtitle - UL LLC/Drummond Group/SLI Compliance/ICSA Labs Quarterly Surveillance Reporting
     */
    @Then("^\"([^\"]*)\" for the quarterly surveillance report$")
    public void iSeeSubtitle(final String expectedSubtitle) {
        String actualSubtitle = SurveillanceReportPage.quarterlySurveillanceReportingSubtitle(getDriver(), expectedSubtitle).getText();
        assertTrue(actualSubtitle.contains(expectedSubtitle));
    }

    /**
     * Assert that confirmation message is correct on initiating a surveillance report.
     * @param expectedConfirmationMessage - Are you sure you wish to initiate quarterly surveillance reporting for quarter Q1/Q2/Q3/Q4 of year 2019/2020?
     */
    @Then("^I see \"([^\"]*)\" on clicking initiate$")
    public void iSeeConfirmationMessageOnClickingInitiate(final String expectedConfirmationMessage) {
        String actualConfirmationMessage = SurveillanceReportPage.confirmMessage(getDriver()).getText();
        assertTrue(actualConfirmationMessage.contains(expectedConfirmationMessage));
    }

    /**
     * Assert that confirmation message is correct on deleting a surveillance report.
     * @param expectedConfirmationMessage - Are you sure you wish to delete this Quarterly Surveillance Report?
     */
    @Then("^I see \"([^\"]*)\" on clicking delete$")
    public void iSeeConfirmationMessageOnClickingDelete(final String expectedConfirmationMessage) {
        String actualConfirmationMessage = SurveillanceReportPage.confirmMessage(getDriver()).getText();
        assertTrue(actualConfirmationMessage.contains(expectedConfirmationMessage));
    }

    /**
     * Assert that page title is not found for the surveillance report page.
     */
    @Then("^I do not see the page title for the surveillance report page$")
    public void iDoNotSeePageTitleForSurveillanceRport() {
            boolean chplSurveillancePageFound = false;
            try {
                chplSurveillancePageFound = SurveillanceReportPage.surveillancePageSubtitle(getDriver()).isDisplayed();
                fail("Navigated to CHPL Surveillance Reports page when shouldn't have");
            } catch (NoSuchElementException e) {
                assertFalse(chplSurveillancePageFound, "Navigation to CHPL Surveillance Reports page is successful");
            }
        }
}

