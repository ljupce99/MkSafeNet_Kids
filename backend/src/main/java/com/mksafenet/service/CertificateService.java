package com.mksafenet.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final SpringTemplateEngine templateEngine;

    public byte[] generateCertificatePdf(String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name == null ? "" : name);
        String html = templateEngine.process("certificate", ctx);

        // Parse HTML5 with Jsoup, then convert to W3C DOM that openhtmltopdf can consume
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        jsoupDoc.outputSettings()
                .syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        org.w3c.dom.Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            var templatesUrl = getClass().getResource("/templates/");
            String baseUri = templatesUrl != null ? templatesUrl.toExternalForm() : "";
            builder.withW3cDocument(w3cDoc, baseUri);

            if (getClass().getResource("/fonts/DejaVuSans.ttf") != null) {
                builder.useFont(
                        () -> getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"),
                        "DejaVu Sans",
                        400,
                        PdfRendererBuilder.FontStyle.NORMAL,
                        true
                );
            }

            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}