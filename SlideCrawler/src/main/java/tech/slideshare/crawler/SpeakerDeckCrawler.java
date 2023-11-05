package tech.slideshare.crawler;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpeakerDeckCrawler implements Crawler {

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

                    String s =
                            Arrays.stream(text.trim().split("\n"))
                            .map(String::trim)
                            .collect(Collectors.joining("\n"));
                    contents.add(s);
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
