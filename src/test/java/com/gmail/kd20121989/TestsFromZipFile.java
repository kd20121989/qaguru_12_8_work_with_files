package com.gmail.kd20121989;

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
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

public class TestsFromZipFile {

    ClassLoader classLoader = getClass().getClassLoader();
    ZipFile resourcesZipFile = new ZipFile(new File("src/test/resources/zip/resources.zip"));

    String fileNamePDF = "junit-user-guide-5.8.2.pdf";
    String fileNameXLS = "sample3.xls";
    String fileNameCSV = "test.csv";
    String fileNameJson = "package.json";

    public TestsFromZipFile() throws IOException {
    }

    @Test
    void checkAllFromZip() throws Exception {
        try (InputStream inputStreamCL = classLoader.getResourceAsStream("zip/resources.zip");
             ZipInputStream zis = new ZipInputStream(Objects.requireNonNull(inputStreamCL))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                try (InputStream inputStream = resourcesZipFile.getInputStream(entry)) {
                    if (entry.getName().equals(fileNamePDF)) {
                        System.out.println("Checking PDF file");
                        PDF pdf = new PDF(inputStream);
                        org.junit.jupiter.api.Assertions.assertEquals(166, pdf.numberOfPages);
                        MatcherAssert.assertThat(pdf, new ContainsExactText("123"));
                    }
                    if (entry.getName().equals(fileNameXLS)) {
                        System.out.println("Checking XLS file");
                        XLS xls = new XLS(Objects.requireNonNull(inputStream));
                        String stringCellValue = xls.excel
                                .getSheetAt(1)
                                .getRow(6)
                                .getCell(2).getStringCellValue();
                        Assertions.assertThat(stringCellValue)
                                .contains("Select single or multiple items from a listbox, to enter in a single cell");
                    }
                    if (entry.getName().equals(fileNameCSV)) {
                        System.out.println("Checking CSV file");
                        CSVReader reader = new CSVReader(new InputStreamReader
                                (Objects.requireNonNull(inputStream), StandardCharsets.UTF_8));
                        List<String[]> csvContent = reader.readAll();
                        Assertions.assertThat(csvContent).contains(
                                new String[]{"id", "lat", "lon", "beacon_signals"},
                                new String[]{"71", "48.00906208425271",
                                        "7.85912499406445",
                                        "A8:B1:D4:C4:1F:9C\":-63"},
                                new String[]{"72", "48.005121066193965",
                                        "7.862264127079382",
                                        "18:EF:63:34:39:A5\":-85"}
                        );
                    }
                    if (entry.getName().equals(fileNameJson)) {
                        System.out.println("Checking json file");
                        Gson gson = new Gson();

                        String json = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                        Assertions.assertThat(jsonObject.get("author").getAsString())
                                .isEqualTo("The Chromium Authors");
                        Assertions.assertThat(jsonObject.get("repository").getAsJsonObject().
                                        get("type").getAsString())
                                .isEqualTo("git");
                    }
                }
            }
        }
    }
}
