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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        Optional<Path> pdf = Downloader.download(link);
        if (pdf.isEmpty()) {
            return Collections.emptyList();
        }

        Path fixed = Files.createTempFile(null, null);
        fixed.toFile().deleteOnExit();
        Process process = new ProcessBuilder("pdf-fix-tuc", pdf.get().toString(), fixed.toString()).start();
        try {
            process.waitFor();
            if (process.exitValue() != 0) {
                logger.warn("pdf-fix-tuc failed. ExitValue={}", process.exitValue());
                fixed = pdf.get();
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
}
