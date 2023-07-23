package ir.baho.framework.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExportType {

    pdf("pdf", "application/pdf"),
    csv("csv", "text/csv"),
    xlsx("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    docx("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    pptx("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    odt("odt", "application/vnd.oasis.opendocument.text"),
    ods("ods", "application/vnd.oasis.opendocument.spreadsheet"),
    rtf("rtf", "text/rtf"),
    html("html", "text/html");

    private final String extension;
    private final String mimetype;

    public String fullName(String name) {
        return name + "." + extension;
    }

}
