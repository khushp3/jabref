package org.jabref.logic.pdf;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.jabref.model.pdf.FileAnnotation;
import org.jabref.model.pdf.FileAnnotationType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PdfAnnotationImporterTest {

    private final AnnotationImporter importer = new PdfAnnotationImporter();

    @Test
    void invalidPath() {
        assertEquals(List.of(), importer.importAnnotations(Path.of("/asdf/does/not/exist.pdf")));
    }

    @Test
    void invalidDirectory() {
        assertEquals(List.of(), importer.importAnnotations(Path.of("src/test/resources/pdfs")));
    }

    @Test
    void invalidDocumentType() {
        assertEquals(List.of(), importer.importAnnotations(Path.of("src/test/resources/pdfs/write-protected.docx")));
    }

    @Test
    void noAnnotationsWriteProtected() {
        assertEquals(List.of(), importer.importAnnotations(Path.of("src/test/resources/pdfs/write-protected.pdf")));
    }

    @Test
    void noAnnotationsEncrypted() {
        assertEquals(List.of(), importer.importAnnotations(Path.of("src/test/resources/pdfs/encrypted.pdf")));
    }

    @Test
    void twoAnnotationsThesisExample() {
        assertEquals(2, importer.importAnnotations(Path.of("src/test/resources/pdfs/thesis-example.pdf")).size());
    }

    @Test
    void noAnnotationsMinimal() {
        assertEquals(List.of(), importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal.pdf")));
    }

    @Test
    void inlineNoteMinimal() {
        final FileAnnotation expected = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 25), 1,
                "inline note annotation", FileAnnotationType.FREETEXT, Optional.empty());

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-inlinenote.pdf")));
    }

    @Test
    void popupNoteMinimal() {
        final FileAnnotation expected = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 17, 24), 1,
                "A simple pop-up note", FileAnnotationType.TEXT, Optional.empty());

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-popup.pdf")));
    }

    @Test
    void highlightMinimalFoxit() {
        final FileAnnotation expectedLinkedAnnotation = new FileAnnotation("lynyus", LocalDateTime.of(2017, 5, 31, 15, 16, 1), 1,
                "this is a foxit highlight", FileAnnotationType.HIGHLIGHT, Optional.empty());
        final FileAnnotation expected = new FileAnnotation("lynyus", LocalDateTime.of(2017, 5, 31, 15, 16, 1), 1,
                "Hello", FileAnnotationType.HIGHLIGHT, Optional.of(expectedLinkedAnnotation));
        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-foxithighlight.pdf")));
    }

    @Test
    void highlightNoNoteMinimal() {
        final FileAnnotation expectedLinkedAnnotation = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 28, 39), 1,
                "", FileAnnotationType.HIGHLIGHT, Optional.empty());
        final FileAnnotation expected = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 28, 39), 1,
                "World", FileAnnotationType.HIGHLIGHT, Optional.of(expectedLinkedAnnotation));

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-highlight-no-note.pdf")));
    }

    @Test
    void squigglyWithNoteMinimal() {
        final FileAnnotation expectedLinkedAnnotation = new FileAnnotation("lynyus", LocalDateTime.of(2017, 6, 1, 2, 40, 25), 1,
                "Squiggly note", FileAnnotationType.SQUIGGLY, Optional.empty());
        final FileAnnotation expected = new FileAnnotation("lynyus", LocalDateTime.of(2017, 6, 1, 2, 40, 25), 1,
                "ello", FileAnnotationType.SQUIGGLY, Optional.of(expectedLinkedAnnotation));

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-squiggly.pdf")));
    }

    @Test
    void strikeoutWithNoteMinimal() {
        final FileAnnotation expectedLinkedAnnotation = new FileAnnotation("lynyus", LocalDateTime.of(2017, 6, 1, 13, 2, 3), 1,
                "striked out", FileAnnotationType.STRIKEOUT, Optional.empty());
        final FileAnnotation expected = new FileAnnotation("lynyus", LocalDateTime.of(2017, 6, 1, 13, 2, 3), 1,
                "World", FileAnnotationType.STRIKEOUT, Optional.of(expectedLinkedAnnotation));

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-strikeout.pdf")));
    }

    @Test
    void highlightWithNoteMinimal() {
        final FileAnnotation expectedLinkedAnnotation = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 32, 2), 1,
                "linked note to highlight", FileAnnotationType.HIGHLIGHT, Optional.empty());
        final FileAnnotation expected = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 32, 2), 1,
                "World", FileAnnotationType.HIGHLIGHT, Optional.of(expectedLinkedAnnotation));

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-highlight-with-note.pdf")));
    }

    @Test
    void underlineWithNoteMinimal() {
        final FileAnnotation expectedLinkedAnnotation = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 36, 9), 1,
                "underlined", FileAnnotationType.UNDERLINE, Optional.empty());
        final FileAnnotation expected = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 12, 20, 36, 9), 1,
                "Hello", FileAnnotationType.UNDERLINE, Optional.of(expectedLinkedAnnotation));

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-underline.pdf")));
    }

    @Test
    void polygonNoNoteMinimal() {
        final FileAnnotation expected = new FileAnnotation("Linus Dietz", LocalDateTime.of(2017, 3, 16, 9, 21, 1), 1,
                "polygon annotation", FileAnnotationType.POLYGON, Optional.empty());

        assertEquals(List.of(expected),
                importer.importAnnotations(Path.of("src/test/resources/pdfs/minimal-polygon.pdf")));
    }
}

