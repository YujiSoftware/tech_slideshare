package tech.slideshare.crawler;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class SpeakerDeckCrawler implements Crawler {

    private static final Logger logger = LoggerFactory.getLogger(SpeakerDeckCrawler.class);

    @Override
    public String getURL() {
        return "https://speakerdeck.com/";
    }

    public List<String> crawl(String url) throws IOException {
        List<String> contents = new ArrayList<>();

        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select("a[title='Download PDF']");
        if (elements.isEmpty()) {
            return contents;
        }

        String link = elements.get(0).attr("href");
        Path pdf = download(link);

        Path fixed = Files.createTempFile(null, null);
        Process process = new ProcessBuilder("pdf-fix-tuc", pdf.toString(), fixed.toString()).start();
        try {
            process.waitFor();
            if (process.exitValue() != 0) {
                logger.warn("pdf-fix-tuc failed. ExitValue={}", process.exitValue());
                fixed = pdf;
            }
        } catch (InterruptedException ignored) {
        }

        try (InputStream is = Files.newInputStream(fixed)) {
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
