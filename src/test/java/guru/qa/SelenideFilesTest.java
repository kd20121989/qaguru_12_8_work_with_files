package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import guru.qa.domain.Teacher;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

public class SelenideFilesTest {

    ClassLoader classLoader = getClass().getClassLoader();

    @Test
    void downloadTest() throws Exception {
        Selenide.open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();
        try (InputStream is = new FileInputStream(textFile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            Assertions.assertThat(strContent).contains("JUnit 5");
        }
    }

    @Test
    void uploadTest() {
        Selenide.open("https://demoqa.com/upload-download");
        $("input[type=file]").uploadFromClasspath("pdf/junit-user-guide-5.8.2.pdf");
        $("p#uploadedFilePath").shouldHave(Condition.text("junit-user-guide-5.8.2.pdf"));
    }

    @Test
    void pdfParsingTest() throws Exception {
        try (InputStream stream = classLoader.getResourceAsStream("pdf/junit-user-guide-5.8.2.pdf")) {
            PDF pdf = new PDF(Objects.requireNonNull(stream));
            org.junit.jupiter.api.Assertions.assertEquals(166, pdf.numberOfPages);
            MatcherAssert.assertThat(pdf, new ContainsExactText("123"));
        }
    }

    @Test
    void pdfDownloadParsingTest() throws Exception {
        Selenide.open("https://junit.org/junit5/docs/current/user-guide/");
        sleep(8000);
        File pdfFile = $(byText("PDF download")).download();
        PDF pdf = new PDF(pdfFile);
        Assertions.assertThat(pdf.author).contains("Marc Philipp");
        org.junit.jupiter.api.Assertions.assertEquals(166, pdf.numberOfPages);
        MatcherAssert.assertThat(pdf, new ContainsExactText("123"));

    }

    @Test
    void xlsParsingTest() throws Exception {
        try (InputStream stream = classLoader.getResourceAsStream("xls/sample3.xls")) {
            XLS xls = new XLS(Objects.requireNonNull(stream));
            String stringCellValue = xls.excel
                    .getSheetAt(1).getRow(6).getCell(2).getStringCellValue();
            Assertions.assertThat(stringCellValue)
                    .contains("Select single or multiple items from a listbox, to enter in a single cell");
        }
    }

    @Test
    void xlsDownloadParsingTest() throws Exception {
        Selenide.open("https://chandoo.org/wp/free-invoice-template/");
        sleep(8000);
        File xlsFile = $("a[href*=invoice-template]").download();
        XLS xls = new XLS(xlsFile);
        Assertions.assertThat(xls.excel
                .getSheetAt(0)
                .getRow(19)
                .getCell(3)
                .getStringCellValue()).contains("Earth Quake Pills");
    }

    @Test
    void csvParsingTest() throws Exception {
        try (InputStream stream = classLoader.getResourceAsStream("csv/test.csv");
             CSVReader reader = new CSVReader(new InputStreamReader
                     (Objects.requireNonNull(stream), StandardCharsets.UTF_8))) {
            List<String[]> csvContent = reader.readAll();
            Assertions.assertThat(csvContent).contains(
                    new String[]{"id", "lat", "lon", "beacon_signals"},
                    new String[]{"71", "48.00906208425271", "7.85912499406445", "A8:B1:D4:C4:1F:9C\":-63"},
                    new String[]{"72", "48.005121066193965", "7.862264127079382", "18:EF:63:34:39:A5\":-85"}
            );
        }
    }

    @Test
    void zipParsingTest() throws Exception {

        try (InputStream is = classLoader.getResourceAsStream("zip/gurutest.zip");
             ZipInputStream zis = new ZipInputStream(Objects.requireNonNull(is))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Assertions.assertThat(entry.getName())
                        .isIn("slf4j-api-1.7.25.jar", "plink.exe", "UICONFIG.bin");

            }
        }
    }

    @Test
    void zipFileParsingTest() throws Exception {
        ZipFile zipFile = new ZipFile(new File("src/test/resources/zip/gurutest.zip"));
        ZipInputStream zis = new ZipInputStream(classLoader.getResourceAsStream("zip/gurutest.zip"));
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null) {
            Assertions.assertThat(entry.getName())
                    .isIn("slf4j-api-1.7.25.jar", "plink.exe", "UICONFIG.bin");;
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                // checks
            }
        }
    }

    @Test
    void jsonParsingTest() throws Exception {
        Gson gson = new Gson();

        try (InputStream is = classLoader.getResourceAsStream("json/package.json")) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            Assertions.assertThat(jsonObject.get("author").getAsString())
                    .isEqualTo("The Chromium Authors");
            Assertions.assertThat(jsonObject.get("repository").getAsJsonObject().
                    get("type").getAsString())
                    .isEqualTo("git");
        }
    }

    @Test
    void jsonTypeTest() throws Exception {
        Gson gson = new Gson();

        try (InputStream is = classLoader.getResourceAsStream("json/teacher.json")) {
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Teacher jsonObject = gson.fromJson(json, Teacher.class);
            Assertions.assertThat(jsonObject.name).isEqualTo("Dmitrii");
            Assertions.assertThat(jsonObject.address.street).isEqualTo("Mira");
        }
    }

}
