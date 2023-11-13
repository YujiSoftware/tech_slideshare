package tech.slideshare.crawler;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleSlideCrawler implements Crawler {

    private static final Logger logger = LoggerFactory.getLogger(SpeakerDeckCrawler.class);

    @Override
    public String getURL() {
        return "https://docs.google.com/";
    }

    @Override
    public List<String> crawl(String url) throws IOException {
        String export = url.replace("/preview", "/export/pdf");
        Path pdf;
        try {
            pdf = download(export);
            pdf.toFile().deleteOnExit();
        } catch (FileNotFoundException e) {
            logger.warn("FileNotFound: " + export);
            // 404 NotFound, 410 Gone の場合、FileNotFoundException
            return Collections.emptyList();
        } catch (IOException e) {
            // 401
            if (e.getMessage().contains("Server returned HTTP response code: 401")) {
                logger.warn(e.getMessage());
                return Collections.emptyList();
            }
            throw e;
        }

        List<String> contents = new ArrayList<>();
        try (InputStream is = Files.newInputStream(pdf)) {
            try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(is))) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                int page = document.getNumberOfPages();
                for (int i = 1; i <= page; i++) {
                    pdfStripper.setStartPage(i);
                    pdfStripper.setEndPage(i);
                    String text = pdfStripper.getText(document);

                    contents.add(text.trim());
                }
            }
        }

        return contents;
    }

    private static Path download(String link) throws IOException {
        Path temp = Files.createTempFile(null, null);
        try (InputStream stream = URI.create(link).toURL().openStream()) {
            Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
        }

        return temp;
    }
}
