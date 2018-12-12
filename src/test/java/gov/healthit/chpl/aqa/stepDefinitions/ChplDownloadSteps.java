package gov.healthit.chpl.aqa.stepDefinitions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gov.healthit.chpl.aqa.pageObjects.ChplDownloadPage;

/**
 * Class ChplDownloadSteps definition.
 */
public class ChplDownloadSteps extends Base {

    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private static final long BYTES_PER_KILOBYTE = 1024;
    private static final double FILE_TOO_LARGE_FACTOR = 1.2;
    /** amount over the minimum that the file shouldn't get.
     * trying to check that no file balloons too quickly.
     * e.g., 1.2 = 20% over the minimum value
     **/

    /**
     * Constructor creates new driver.
     */
    public ChplDownloadSteps() {
        super();
    }

    /**
     * Get user to the Download CHPL page. Chrome options are necessary to get past
     * keep/discard pop ups for successful download of a file to directory.
     * @throws Throwable throws exception if there is an issue with Chrome options.
     * @param tEnv test environment in which tests will be run
     */
    @Given("^I am on download the CHPL resources page on \"([^\"]*)\"$")
    public void iAmOnDownloadTheCHPLResourcesPage(final String tEnv) throws Throwable {
        // Get download page of system URL, then change to page of passed in environment
        getDriver().get(getUrl() + "#/resources/download");
        getWait().until(ExpectedConditions.visibilityOf(ChplDownloadPage.downloadSelectList(getDriver())));
        String url;
        switch (tEnv) {
        case "DEV": url = "https://chpl.ahrqdev.org";
        break;
        case "STG": url = "https://chpl.ahrqstg.org";
        break;
        case "PROD": url = "https://chpl.healthit.gov";
        break;
        default: url = getUrl();
        break;
        }
        getDriver().get(url + "#/resources/download");
        getWait().until(ExpectedConditions.visibilityOf(ChplDownloadPage.downloadSelectList(getDriver())));
    }

    /**
     * Activates an item in the download file box.
     */
    @When("^user selects a file in download file box")
    public void userSelectsAFileInDownloadFileBox() {
        WebElement selectBox = ChplDownloadPage.downloadSelectList(getDriver());
        Select dropdown = new Select(selectBox);
        dropdown.selectByVisibleText("2015 edition products (xml)");
    }

    /**
     * Assert that correct definition file shows.
     */
    @Then("^definition file shows based on download file selection$")
    public void definitionFileShowsBasedOnSelection() {
        String definition = new Select(ChplDownloadPage.definitionSelectList(getDriver())).getFirstSelectedOption().getText();
        assertTrue(definition.contains("2015 edition products (xml) Definition File"));
    }

    /**
     * Assert that correct number of download files exist.
     * @param expectedLength the expected number of files to find
     */
    @Then("^user sees \"([^\"]*)\" download files")
    public void userSeesDownloadFiles(final String expectedLength) {
        WebElement selectElement = ChplDownloadPage.downloadSelectList(getDriver());
        Select listBox = new Select(selectElement);
        assertEquals(listBox.getOptions().size(), Integer.parseInt(expectedLength));
    }

    /**
     * Download one of the five product files. Waits to exit until file has been downloaded.
     * @param edition which edition to download
     * @param type whether to get the xml or csv file
     * @throws InterruptedException if thread.sleep is interrupted
     */
    @When("^I download the \"(.*)\" \"(.*)\" products file$")
    public void downloadProductFile(final String edition, final String type) throws InterruptedException {
        switch (edition) {
        case "2011":
            ChplDownloadPage.downloadoption2011editionProductsFile(getDriver()).click();
            break;
        case "2014":
            switch (type) {
            case "csv":
                ChplDownloadPage.downloadoption2014summaryFile(getDriver()).click();
                break;
            case "xml":
                ChplDownloadPage.downloadoption2014editionProductsFile(getDriver()).click();
                break;
            default:
                break;
            }
            break;
        case "2015":
            switch (type) {
            case "csv":
                ChplDownloadPage.downloadoption2015summaryFile(getDriver()).click();
                break;
            case "xml":
                ChplDownloadPage.downloadoption2015editionProductsFile(getDriver()).click();
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        ChplDownloadPage.downloadFileButton(getDriver()).click();

        String downloadFileName = null;
        boolean fileFound = false;
        final long sleepTime = 5 * 1000;

        while (!fileFound) {
            File[] files = Hooks.getDownloadDirectory().listFiles();

            for (File file : files) {
                downloadFileName = file.getName();
                if (downloadFileName
                        .replaceAll("(^.........).*(....$)", "$1" + "$2")
                        .equalsIgnoreCase("chpl-" + edition + "." + type)) {
                    fileFound = true;
                }
                Thread.sleep(sleepTime);
            }
        }
    }

    /**
     * Assert that file is not older than expected value. Reads file in download directory
     * then parses filename's date field and compares with "today".
     * @param days maximum number of days old the file may be
     */
    @Then("^the download file is no more than \"(.*)\" days old$")
    public void theDownloadFileIsNotOld(final String days) {
        final int startOfDateInFilename = 10;
        final int endOfDateInFilename = 18;
        String downloadFileName = null;

        File[] files = Hooks.getDownloadDirectory().listFiles();

        for (File file : files) {
            downloadFileName = file.getName();
        }
        String downloadFileDate = downloadFileName.substring(startOfDateInFilename, endOfDateInFilename);
        try {
            Date fileDate = new SimpleDateFormat("yyyyMMdd").parse(downloadFileDate);
            Date currentDate = new Date();
            int numDays = Integer.parseInt(days);
            double age = Math.ceil((currentDate.getTime() - fileDate.getTime()) / MILLIS_IN_A_DAY);
            assertTrue(age <= numDays,
                    "File " + downloadFileName + " is " + age + " days old, should be no more than " + numDays);
        } catch (ParseException e) {
            fail("Could not parse filename: " + downloadFileName + "'s date");
        }
    }

    /**
     * Assert that the file is at least the expected size. Will also give an error if the actual size is more than
     * 10% larger than the minimum size, in order to encourage the file size values to get updated as the files grow.
     * @param sizeParam expected size
     * @param units expected file unit scale (MB or KB)
     */
    @And("^the download file is at least \"(.*)\" \"(.*)\" in size$")
    public void theDownloadFileIsNotTooSmall(final String sizeParam, final String units) {
        Long size = Long.valueOf(sizeParam);
        File[] files = Hooks.getDownloadDirectory().listFiles();
        for (File f : files) {
            Long rawSize = f.length();
            Long fileSize;
            switch (units) {
            case "MB": fileSize = rawSize / BYTES_PER_KILOBYTE / BYTES_PER_KILOBYTE;
            break;
            case "KB": fileSize = rawSize / BYTES_PER_KILOBYTE;
            break;
            default:
                fileSize = rawSize;
            }
            String fileName = f.getName();
            assertTrue(fileSize >= size,
                    "File " + fileName + " is " + fileSize + " " + units + " in size; should be at least " + size + " " + units);
            assertTrue(fileSize <= (size * FILE_TOO_LARGE_FACTOR),
                    "File " + fileName + " is " + fileSize + " " + units + " in size; should be no more than "
                            + size * FILE_TOO_LARGE_FACTOR + " " + units + ". Please update the AQA test values.");
        }
    }

    /**
     * Assert that the file has at least the expected number of items. Will also give an error if the actual count is
     * 10% more than the expected count, in order to encourage the counts to get updated as the files grow.
     * @param itemParam expected item count
     */
    @And("^the download file has at least \"(.*)\" items$")
    public void theDownloadFileHasAtLeastItems(final String itemParam) {
        Long items = Long.valueOf(itemParam);
        File[] files = Hooks.getDownloadDirectory().listFiles();
        for (File f : files) {
            String fileName = f.getName();
            if (StringUtils.endsWith(fileName, ".csv")) {
                Long rawItems = getCsvItemCount(f);
                assertTrue(rawItems >= items,
                        "File " + fileName + " has " + rawItems + " items; should have at least " + items);
                assertTrue(rawItems <= (items * FILE_TOO_LARGE_FACTOR),
                        "File " + fileName + " has " + rawItems + " items; should be no more than "
                                + items * FILE_TOO_LARGE_FACTOR + ". Please update the AQA test values.");
            } else if (StringUtils.endsWith(fileName, ".xml")) {
                Long rawItems = getXmlItemCount(f);
                assertTrue(rawItems >= items,
                        "File " + fileName + " has " + rawItems + " items; should have at least " + items);
                assertTrue(rawItems <= (items * FILE_TOO_LARGE_FACTOR),
                        "File " + fileName + " has " + rawItems + " items; should be no more than "
                                + items * FILE_TOO_LARGE_FACTOR + ". Please update the AQA test values.");
            }
        }
    }

    /**
     * Assert that a listing in the file with a given ID has the expected CHPL Product Number.
     * @param databaseId the database id of a listing
     * @param chplProductNumber the expected chplProductNumber to be associated with the database ID
     */
    @And("^the download file has a listing with database id \"(.*)\" and product number \"(.*)\"$")
    public void theDownloadFileHasListingIdWithChplProductNumber(final String id,
            final String chplProductNumber) {
        if(StringUtils.isEmpty(id) || StringUtils.isEmpty(chplProductNumber)) {
            return;
        }

        File[] files = Hooks.getDownloadDirectory().listFiles();
        for (File f : files) {
            String fileName = f.getName();
            //TODO: add csv parsing
            //xml parsing
            if (StringUtils.endsWith(fileName, ".xml")) {
                String chplProductNumberInFile = findXmlChplProductNumber(f, id);
                assertEquals(chplProductNumber, chplProductNumberInFile);
            }
        }
    }

    /**
     * Select Surveillance Activity file from drop down and download .csv file.
     */
    @When("^I download the Surveillance Activity file$")
    public void downloadSurveillanceActivityFile() {
        ChplDownloadPage.downloadoptionSurveillanceFile(getDriver()).click();
        ChplDownloadPage.downloadFileButton(getDriver()).click();
    }

    /**
     * Assert filename of download file.
     */
    @Then("^the downloaded file shows surveillance-all.csv filename$")
    public void verifySurveillanceActivityFilename() {

        File[] filenames = Hooks.getDownloadDirectory().listFiles();

        for (File file : filenames) {
            String dwldFileName = file.getName();
            String currentfile = "surveillance-all.csv";
            assertEquals(dwldFileName, currentfile, "File is not current");
        }
    }

    /**
     * Select surveillance-with-nonconformities file from drop down and download .csv file.
     */
    @When("^I download the Non-Conformities file$")
    public void downloadNonConformitiesFile() {
        ChplDownloadPage.downloadoptionNonconformitiesFile(getDriver()).click();
        ChplDownloadPage.downloadFileButton(getDriver()).click();
    }

    /**
     * Assert filename of download file.
     */
    @Then("^the downloaded file shows surveillance-with-nonconformities.csv filename$")
    public void verifyNonConformitiesFilename() {

        File[] filenames = Hooks.getDownloadDirectory().listFiles();

        for (File file : filenames) {
            String dwldFileName = file.getName();
            String currentfile = "surveillance-with-nonconformities.csv";
            assertEquals(dwldFileName, currentfile, "File is not current");
        }
    }

    /**
     * Print filenames if directory is not empty.
     * Assert that download directory is empty
     * @throws IOException if unable to clean the directory
     */
    @And("^the download directory is empty$")
    public void checkifDownloadDirectoryisEmpty() throws IOException {
        System.out.println("Clearing directory: " + Hooks.getDownloadDirectory().getAbsolutePath());
        for (String fileName : Hooks.getDownloadDirectory().list()) {
            System.out.println("Removing: " + fileName);
        }
        FileUtils.cleanDirectory(Hooks.getDownloadDirectory());
        assertFalse(Hooks.getDownloadDirectory().list().length > 0, "directory is not empty");
    }

    private long getCsvItemCount(final File input) {
        List<String> lines = new ArrayList<String>();
        try {
            lines = Files.readAllLines(input.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return lines.size() - 1;
    }

    private long getXmlItemCount(final File input) {
        long items = 0;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try (InputStream in = new FileInputStream(input)) {
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    if (event.asStartElement().getName().getLocalPart().equalsIgnoreCase("listing")) {
                        items += 1;
                    }
                }
            }
        } catch (XMLStreamException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return items;
    }

    private String findXmlChplProductNumber(final File input, final String databaseId) {
        String chplProductNumber = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            XPath xPath =  XPathFactory.newInstance().newXPath();
            String expression = "/results/listings/listing/id[text()='" + databaseId + "']";
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
               doc, XPathConstants.NODESET);
    
            if(nodeList != null && nodeList.getLength() == 1) {
                //we found one listing with the given id which is expected.
                Node listingIdNode = nodeList.item(0);
                //get the parent <listing> element
                Node listingNode = listingIdNode.getParentNode();
                if(listingNode != null) {
                    //get all the <listing> child elements
                    NodeList listingChildren = listingNode.getChildNodes();
                    if(listingChildren != null && listingChildren.getLength() > 0) {
                        for(int i = 0; i < listingChildren.getLength(); i++) {
                            Node listingChild = listingChildren.item(i);
                            //look for <listing>...<chplProductNumber>...</listing> element
                            if(listingChild.getNodeName().equalsIgnoreCase("chplProductNumber")) {
                                chplProductNumber = listingChild.getTextContent();
                            }
                        }
                    }
                }
            } else if (nodeList == null || nodeList.getLength() == 0) {
                fail("No listing was found with the given ID.");
            } else if (nodeList.getLength() > 1) {
                fail("More than one listing was found with the given ID.");
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return chplProductNumber;
    }
}
