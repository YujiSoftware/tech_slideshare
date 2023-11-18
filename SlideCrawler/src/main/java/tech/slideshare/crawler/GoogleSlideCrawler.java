package tech.slideshare.crawler;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GoogleSlideCrawler implements Crawler {

    @Override
    public String getURL() {
        return "https://docs.google.com/";
    }

    @Override
    public List<String> crawl(String url) throws IOException {
        String export = url.replace("/preview", "/export/pdf").replace("/pub", "/export/pdf").replace("/edit", "/export/pdf");
        Optional<Path> pdf = Downloader.download(export);
        if (pdf.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> contents = new ArrayList<>();
        try (InputStream is = Files.newInputStream(pdf.get())) {
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
