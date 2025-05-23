package org.jabref.model.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Other parsing tests are available in {@link org.jabref.logic.importer.AuthorListParserTest}
 */
public class AuthorListTest {

    /*
    Examples are similar to page 4 in
    [BibTeXing by Oren Patashnik](https://ctan.org/tex-archive/biblio/bibtex/contrib/doc/)
    */
    private static final Author MUHAMMAD_ALKHWARIZMI =
            new Author("Mu{\\d{h}}ammad", "M.", null, "al-Khw{\\={a}}rizm{\\={i}}", null);
    private static final Author CORRADO_BOHM =
            new Author("Corrado", "C.", null, "B{\\\"o}hm", null);
    private static final Author KURT_GODEL =
            new Author("Kurt", "K.", null, "G{\\\"{o}}del", null);
    private static final Author BANU_MOSA =
            new Author(null, null, null, "{The Ban\\={u} M\\={u}s\\={a} brothers}", null);
    private static final AuthorList EMPTY_AUTHOR = AuthorList.of(List.of());
    private static final AuthorList ONE_AUTHOR_WITH_LATEX = AuthorList.of(MUHAMMAD_ALKHWARIZMI);
    private static final AuthorList TWO_AUTHORS_WITH_LATEX = AuthorList.of(MUHAMMAD_ALKHWARIZMI, CORRADO_BOHM);
    private static final AuthorList THREE_AUTHORS_WITH_LATEX = AuthorList.of(MUHAMMAD_ALKHWARIZMI, CORRADO_BOHM, KURT_GODEL);
    private static final AuthorList ONE_INSTITUTION_WITH_LATEX = AuthorList.of(BANU_MOSA);
    private static final AuthorList ONE_INSTITUTION_WITH_STARTING_PARANTHESIS = AuthorList.of(new Author(
            null, null, null, "{{\\L{}}ukasz Micha\\l{}}", null));
    private static final AuthorList TWO_INSTITUTIONS_WITH_LATEX = AuthorList.of(BANU_MOSA, BANU_MOSA);
    private static final AuthorList MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX = AuthorList.of(BANU_MOSA, CORRADO_BOHM);

    public static int size(String bibtex) {
        return AuthorList.parse(bibtex).getNumberOfAuthors();
    }

    @Test
    void fixAuthorNatbib() {
        assertEquals("", AuthorList.fixAuthorNatbib(""));
        assertEquals("Smith", AuthorList.fixAuthorNatbib("John Smith"));
        assertEquals("Smith and Black Brown", AuthorList
                .fixAuthorNatbib("John Smith and Black Brown, Peter"));
        assertEquals("von Neumann et al.", AuthorList
                .fixAuthorNatbib("John von Neumann and John Smith and Black Brown, Peter"));
    }

    @Test
    void getAsNatbibLatexFreeEmptyAuthorStringForEmptyInput() {
        assertEquals("", EMPTY_AUTHOR.latexFree().getAsNatbib());
    }

    @Test
    void getAsNatbibLatexFreeUnicodeOneAuthorNameFromLatex() {
        assertEquals("al-Khwārizmī",
                ONE_AUTHOR_WITH_LATEX.latexFree().getAsNatbib());
    }

    @Test
    void getAsNatbibLatexFreeUnicodeTwoAuthorNamesFromLatex() {
        assertEquals("al-Khwārizmī and Böhm",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsNatbib());
    }

    @Test
    void getAsNatbibLatexFreeUnicodeAuthorEtAlFromLatex() {
        assertEquals("al-Khwārizmī et al.",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsNatbib());
    }

    @Test
    void getAsNatbibLatexFreeUnicodeOneInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers",
                ONE_INSTITUTION_WITH_LATEX.latexFree().getAsNatbib());
    }

    @Test
    void getAsNatbibLatexFreeUnicodeTwoInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsNatbib());
    }

    @Test
    void getAsNatbibLatexFreeUnicodeMixedAuthorsFromLatex() {
        assertEquals("The Banū Mūsā brothers and Böhm",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsNatbib());
    }

    @Test
    void getAsNatbibLatexFreeOneInstitutionWithParanthesisAtStart() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsNatbib());
    }

    @Test
    void parseCachesOneAuthor() {
        // Test caching in authorCache.
        AuthorList authorList = AuthorList.parse("John Smith");
        assertSame(authorList, AuthorList.parse("John Smith"));
        assertNotSame(authorList, AuthorList.parse("Smith"));
    }

    @Test
    void parseCachesOneLatexFreeAuthor() {
        // Test caching in authorCache.
        AuthorList authorList = AuthorList.parse("John Smith").latexFree();
        assertSame(authorList, AuthorList.parse("John Smith").latexFree());
        assertNotSame(authorList, AuthorList.parse("Smith").latexFree());
    }

    @Test
    void fixAuthorFirstNameFirstCommas() {
        // No Commas
        assertEquals("", AuthorList.fixAuthorFirstNameFirstCommas("", true, false));
        assertEquals("", AuthorList.fixAuthorFirstNameFirstCommas("", false, false));

        assertEquals("John Smith", AuthorList.fixAuthorFirstNameFirstCommas("John Smith",
                false, false));
        assertEquals("J. Smith", AuthorList.fixAuthorFirstNameFirstCommas("John Smith", true,
                false));

        // Check caching
        assertEquals(AuthorList.fixAuthorFirstNameFirstCommas(
                "John von Neumann and John Smith and Black Brown, Peter", true, false), AuthorList
                .fixAuthorFirstNameFirstCommas("John von Neumann and John Smith and Black Brown, Peter", true, false));

        assertEquals("John Smith and Peter Black Brown", AuthorList
                .fixAuthorFirstNameFirstCommas("John Smith and Black Brown, Peter", false, false));
        assertEquals("J. Smith and P. Black Brown", AuthorList.fixAuthorFirstNameFirstCommas(
                "John Smith and Black Brown, Peter", true, false));

        // Method description is different from code -> additional comma
        // there
        assertEquals("John von Neumann, John Smith and Peter Black Brown", AuthorList
                .fixAuthorFirstNameFirstCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", false, false));
        assertEquals("J. von Neumann, J. Smith and P. Black Brown", AuthorList
                .fixAuthorFirstNameFirstCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", true, false));

        assertEquals("J. P. von Neumann", AuthorList.fixAuthorFirstNameFirstCommas(
                "John Peter von Neumann", true, false));
        // Oxford Commas
        assertEquals("", AuthorList.fixAuthorFirstNameFirstCommas("", true, true));
        assertEquals("", AuthorList.fixAuthorFirstNameFirstCommas("", false, true));

        assertEquals("John Smith", AuthorList.fixAuthorFirstNameFirstCommas("John Smith",
                false, true));
        assertEquals("J. Smith", AuthorList.fixAuthorFirstNameFirstCommas("John Smith", true,
                true));

        // Check caching
        assertEquals(AuthorList.fixAuthorFirstNameFirstCommas(
                "John von Neumann and John Smith and Black Brown, Peter", true, true), AuthorList
                .fixAuthorFirstNameFirstCommas("John von Neumann and John Smith and Black Brown, Peter", true, true));

        assertEquals("John Smith and Peter Black Brown", AuthorList
                .fixAuthorFirstNameFirstCommas("John Smith and Black Brown, Peter", false, true));
        assertEquals("J. Smith and P. Black Brown", AuthorList.fixAuthorFirstNameFirstCommas(
                "John Smith and Black Brown, Peter", true, true));

        // Method description is different than code -> additional comma
        // there
        assertEquals("John von Neumann, John Smith, and Peter Black Brown", AuthorList
                .fixAuthorFirstNameFirstCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", false, true));
        assertEquals("J. von Neumann, J. Smith, and P. Black Brown", AuthorList
                .fixAuthorFirstNameFirstCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", true, true));

        assertEquals("J. P. von Neumann", AuthorList.fixAuthorFirstNameFirstCommas(
                "John Peter von Neumann", true, true));
    }

    @Test
    void getAsFirstLastNamesLatexFreeEmptyAuthorStringForEmptyInputAbbreviate() {
        assertEquals("", EMPTY_AUTHOR.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeOneAuthorNameFromLatexAbbreviate() {
        assertEquals("M. al-Khwārizmī",
                ONE_AUTHOR_WITH_LATEX.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeTwoAuthorNamesFromLatexAbbreviate() {
        assertEquals("M. al-Khwārizmī and C. Böhm",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeTwoAuthorNamesFromLatexAbbreviateAndOxfordComma() {
        assertEquals("M. al-Khwārizmī and C. Böhm",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsFirstLastNames(true, true));
    }

    @Test
    void getAsFirstLastNamesLatexFreeThreeUnicodeAuthorsFromLatexAbbreviate() {
        assertEquals("M. al-Khwārizmī, C. Böhm and K. Gödel",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeThreeUnicodeAuthorsFromLatexAbbreviateAndOxfordComma() {
        assertEquals("M. al-Khwārizmī, C. Böhm, and K. Gödel",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsFirstLastNames(true, true));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeOneInsitutionNameFromLatexAbbreviate() {
        assertEquals("The Banū Mūsā brothers", ONE_INSTITUTION_WITH_LATEX.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeTwoInsitutionNameFromLatexAbbreviate() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeMixedAuthorsFromLatexAbbreviate() {
        assertEquals("The Banū Mūsā brothers and C. Böhm",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeOneInstitutionWithParanthesisAtStartAbbreviate() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsFirstLastNames(true, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeEmptyAuthorStringForEmptyInput() {
        assertEquals("", EMPTY_AUTHOR.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeOneAuthorNameFromLatex() {
        assertEquals("Muḥammad al-Khwārizmī",
                ONE_AUTHOR_WITH_LATEX.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeTwoAuthorNamesFromLatex() {
        assertEquals("Muḥammad al-Khwārizmī and Corrado Böhm",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeThreeUnicodeAuthorsFromLatex() {
        assertEquals("Muḥammad al-Khwārizmī, Corrado Böhm and Kurt Gödel",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeOneInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers",
                ONE_INSTITUTION_WITH_LATEX.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeTwoInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeUnicodeMixedAuthorsFromLatex() {
        assertEquals("The Banū Mūsā brothers and Corrado Böhm",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void getAsFirstLastNamesLatexFreeOneInstitutionWithParanthesisAtStart() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsFirstLastNames(false, false));
    }

    @Test
    void fixAuthorFirstNameFirst() {
        assertEquals("John Smith", AuthorList.fixAuthorFirstNameFirst("John Smith"));

        assertEquals("John Smith and Peter Black Brown", AuthorList
                .fixAuthorFirstNameFirst("John Smith and Black Brown, Peter"));

        assertEquals("John von Neumann and John Smith and Peter Black Brown", AuthorList
                .fixAuthorFirstNameFirst("John von Neumann and John Smith and Black Brown, Peter"));

        assertEquals("First von Last, Jr. III", AuthorList
                .fixAuthorFirstNameFirst("von Last, Jr. III, First"));

        // Check caching
        assertEquals(AuthorList
                .fixAuthorFirstNameFirst("John von Neumann and John Smith and Black Brown, Peter"), AuthorList
                .fixAuthorFirstNameFirst("John von Neumann and John Smith and Black Brown, Peter"));
    }

    @Test
    void fixAuthorLastNameFirstCommasNoComma() {
        // No commas before and
        assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", true, false));
        assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", false, false));

        assertEquals("Smith, John", AuthorList.fixAuthorLastNameFirstCommas("John Smith", false, false));
        assertEquals("Smith, J.", AuthorList.fixAuthorLastNameFirstCommas("John Smith", true, false));

        String a = AuthorList.fixAuthorLastNameFirstCommas("John von Neumann and John Smith and Black Brown, Peter",
                true, false);
        String b = AuthorList.fixAuthorLastNameFirstCommas(
                "John von Neumann and John Smith and Black Brown, Peter", true, false);

        // Check caching
        assertEquals(a, b);

        assertEquals("Smith, John and Black Brown, Peter",
                AuthorList.fixAuthorLastNameFirstCommas("John Smith and Black Brown, Peter", false, false));
        assertEquals("Smith, J. and Black Brown, P.",
                AuthorList.fixAuthorLastNameFirstCommas("John Smith and Black Brown, Peter", true, false));

        assertEquals("von Neumann, John, Smith, John and Black Brown, Peter", AuthorList
                .fixAuthorLastNameFirstCommas("John von Neumann and John Smith and Black Brown, Peter", false, false));
        assertEquals("von Neumann, J., Smith, J. and Black Brown, P.", AuthorList
                .fixAuthorLastNameFirstCommas("John von Neumann and John Smith and Black Brown, Peter", true, false));

        assertEquals("von Neumann, J. P.",
                AuthorList.fixAuthorLastNameFirstCommas("John Peter von Neumann", true, false));
    }

    @Test
    void fixAuthorLastNameFirstCommasOxfordComma() {
        // Oxford Commas
        assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", true, true));
        assertEquals("", AuthorList.fixAuthorLastNameFirstCommas("", false, true));

        assertEquals("Smith, John", AuthorList.fixAuthorLastNameFirstCommas("John Smith",
                false, true));
        assertEquals("Smith, J.", AuthorList.fixAuthorLastNameFirstCommas("John Smith", true,
                true));

        String a = AuthorList.fixAuthorLastNameFirstCommas(
                "John von Neumann and John Smith and Black Brown, Peter", true, true);
        String b = AuthorList.fixAuthorLastNameFirstCommas("John von Neumann and John Smith and Black Brown, Peter", true, true);

        // Check caching
        assertEquals(a, b);
        // assertSame(a, b);

        assertEquals("Smith, John and Black Brown, Peter", AuthorList
                .fixAuthorLastNameFirstCommas("John Smith and Black Brown, Peter", false, true));
        assertEquals("Smith, J. and Black Brown, P.", AuthorList.fixAuthorLastNameFirstCommas(
                "John Smith and Black Brown, Peter", true, true));

        assertEquals("von Neumann, John, Smith, John, and Black Brown, Peter", AuthorList
                .fixAuthorLastNameFirstCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", false, true));
        assertEquals("von Neumann, J., Smith, J., and Black Brown, P.", AuthorList
                .fixAuthorLastNameFirstCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", true, true));

        assertEquals("von Neumann, J. P.", AuthorList.fixAuthorLastNameFirstCommas(
                "John Peter von Neumann", true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInputAbbr() {
        assertEquals("", EMPTY_AUTHOR.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatexAbbr() {
        assertEquals("al-Khwārizmī, M.",
                ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatexAbbr() {
        assertEquals("al-Khwārizmī, M. and Böhm, C.",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatexAbbr() {
        assertEquals("al-Khwārizmī, M., Böhm, C. and Gödel, K.",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatexAbbr() {
        assertEquals("The Banū Mūsā brothers",
                ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatexAbbr() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatexAbbr() {
        assertEquals("The Banū Mūsā brothers and Böhm, C.",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStartAbbr() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsLastFirstNames(true, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInput() {
        assertEquals("", EMPTY_AUTHOR.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatex() {
        assertEquals("al-Khwārizmī, Muḥammad",
                ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatex() {
        assertEquals("al-Khwārizmī, Muḥammad and Böhm, Corrado",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatex() {
        assertEquals("al-Khwārizmī, Muḥammad, Böhm, Corrado and Gödel, Kurt",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers",
                ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatex() {
        assertEquals("The Banū Mūsā brothers and Böhm, Corrado",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStart() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsLastFirstNames(false, false));
    }

    @Test
    void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInputAbbrOxfordComma() {
        assertEquals("", EMPTY_AUTHOR.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatexAbbrOxfordComma() {
        assertEquals("al-Khwārizmī, M.",
                ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatexAbbrOxfordComma() {
        assertEquals("al-Khwārizmī, M. and Böhm, C.",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatexAbbrOxfordComma() {
        assertEquals("al-Khwārizmī, M., Böhm, C., and Gödel, K.",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatexAbbrOxfordComma() {
        assertEquals("The Banū Mūsā brothers",
                ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatexAbbrOxfordComma() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatexAbbrOxfordComma() {
        assertEquals("The Banū Mūsā brothers and Böhm, C.",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStartAbbrOxfordComma() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsLastFirstNames(true, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeEmptyAuthorStringForEmptyInputOxfordComma() {
        assertEquals("", EMPTY_AUTHOR.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneAuthorNameFromLatexOxfordComma() {
        assertEquals("al-Khwārizmī, Muḥammad",
                ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoAuthorNamesFromLatexOxfordComma() {
        assertEquals("al-Khwārizmī, Muḥammad and Böhm, Corrado",
                TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeThreeUnicodeAuthorsFromLatexOxfordComma() {
        assertEquals("al-Khwārizmī, Muḥammad, Böhm, Corrado, and Gödel, Kurt",
                THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeOneInsitutionNameFromLatexOxfordComma() {
        assertEquals("The Banū Mūsā brothers",
                ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeTwoInsitutionNameFromLatexOxfordComma() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeUnicodeMixedAuthorsFromLatexOxfordComma() {
        assertEquals("The Banū Mūsā brothers and Böhm, Corrado",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void getAsLastFirstNamesLatexFreeOneInstitutionWithParanthesisAtStartOxfordComma() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsLastFirstNames(false, true));
    }

    @Test
    void fixAuthorLastNameFirst() {
        // Test helper method

        assertEquals("Smith, John", AuthorList.fixAuthorLastNameFirst("John Smith"));

        assertEquals("Smith, John and Black Brown, Peter", AuthorList
                .fixAuthorLastNameFirst("John Smith and Black Brown, Peter"));

        assertEquals("von Neumann, John and Smith, John and Black Brown, Peter", AuthorList
                .fixAuthorLastNameFirst("John von Neumann and John Smith and Black Brown, Peter"));

        assertEquals("von Last, Jr, First", AuthorList
                .fixAuthorLastNameFirst("von Last, Jr ,First"));

        assertEquals(AuthorList
                .fixAuthorLastNameFirst("John von Neumann and John Smith and Black Brown, Peter"), AuthorList
                .fixAuthorLastNameFirst("John von Neumann and John Smith and Black Brown, Peter"));

        // Test Abbreviation == false
        assertEquals("Smith, John", AuthorList.fixAuthorLastNameFirst("John Smith", false));

        assertEquals("Smith, John and Black Brown, Peter", AuthorList.fixAuthorLastNameFirst(
                "John Smith and Black Brown, Peter", false));

        assertEquals("von Neumann, John and Smith, John and Black Brown, Peter", AuthorList
                .fixAuthorLastNameFirst("John von Neumann and John Smith and Black Brown, Peter",
                        false));

        assertEquals("von Last, Jr, First", AuthorList.fixAuthorLastNameFirst(
                "von Last, Jr ,First", false));

        assertEquals(AuthorList.fixAuthorLastNameFirst(
                "John von Neumann and John Smith and Black Brown, Peter", false), AuthorList
                .fixAuthorLastNameFirst("John von Neumann and John Smith and Black Brown, Peter", false));

        // Test Abbreviate == true
        assertEquals("Smith, J.", AuthorList.fixAuthorLastNameFirst("John Smith", true));

        assertEquals("Smith, J. and Black Brown, P.", AuthorList.fixAuthorLastNameFirst(
                "John Smith and Black Brown, Peter", true));

        assertEquals("von Neumann, J. and Smith, J. and Black Brown, P.",
                AuthorList.fixAuthorLastNameFirst(
                        "John von Neumann and John Smith and Black Brown, Peter", true));

        assertEquals("von Last, Jr, F.", AuthorList.fixAuthorLastNameFirst("von Last, Jr ,First",
                true));

        assertEquals(AuthorList.fixAuthorLastNameFirst(
                "John von Neumann and John Smith and Black Brown, Peter", true), AuthorList
                .fixAuthorLastNameFirst("John von Neumann and John Smith and Black Brown, Peter", true));
    }

    @Test
    void fixAuthorLastNameOnlyCommas() {
        // No comma before and
        assertEquals("", AuthorList.fixAuthorLastNameOnlyCommas("", false));
        assertEquals("Smith", AuthorList.fixAuthorLastNameOnlyCommas("John Smith", false));
        assertEquals("Smith", AuthorList.fixAuthorLastNameOnlyCommas("Smith, Jr, John", false));

        assertEquals(AuthorList.fixAuthorLastNameOnlyCommas(
                "John von Neumann and John Smith and Black Brown, Peter", false), AuthorList
                .fixAuthorLastNameOnlyCommas("John von Neumann and John Smith and Black Brown, Peter", false));

        assertEquals("von Neumann, Smith and Black Brown", AuthorList
                .fixAuthorLastNameOnlyCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", false));
        // Oxford Comma
        assertEquals("", AuthorList.fixAuthorLastNameOnlyCommas("", true));
        assertEquals("Smith", AuthorList.fixAuthorLastNameOnlyCommas("John Smith", true));
        assertEquals("Smith", AuthorList.fixAuthorLastNameOnlyCommas("Smith, Jr, John", true));

        assertEquals(AuthorList.fixAuthorLastNameOnlyCommas(
                "John von Neumann and John Smith and Black Brown, Peter", true), AuthorList
                .fixAuthorLastNameOnlyCommas("John von Neumann and John Smith and Black Brown, Peter", true));

        assertEquals("von Neumann, Smith, and Black Brown", AuthorList
                .fixAuthorLastNameOnlyCommas(
                        "John von Neumann and John Smith and Black Brown, Peter", true));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeOneAuthorNameFromLatex() {
        assertEquals("al-Khwārizmī", ONE_AUTHOR_WITH_LATEX.latexFree().getAsLastNames(false));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeTwoAuthorNamesFromLatex() {
        assertEquals("al-Khwārizmī and Böhm", TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(false));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeTwoAuthorNamesFromLatexUsingOxfordComma() {
        assertEquals("al-Khwārizmī and Böhm", TWO_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(true));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeThreeAuthorsFromLatex() {
        assertEquals("al-Khwārizmī, Böhm and Gödel", THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(false));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeThreeAuthorsFromLatexUsingOxfordComma() {
        assertEquals("al-Khwārizmī, Böhm, and Gödel", THREE_AUTHORS_WITH_LATEX.latexFree().getAsLastNames(true));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeOneInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers", ONE_INSTITUTION_WITH_LATEX.latexFree().getAsLastNames(false));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeTwoInsitutionNameFromLatex() {
        assertEquals("The Banū Mūsā brothers and The Banū Mūsā brothers",
                TWO_INSTITUTIONS_WITH_LATEX.latexFree().getAsLastNames(false));
    }

    @Test
    void getAsLastNamesLatexFreeUnicodeMixedAuthorsFromLatex() {
        assertEquals("The Banū Mūsā brothers and Böhm",
                MIXED_AUTHOR_AND_INSTITUTION_WITH_LATEX.latexFree().getAsLastNames(false));
    }

    @Test
    void getAsLastNamesLatexFreeOneInstitutionWithParanthesisAtStart() {
        assertEquals("Łukasz Michał",
                ONE_INSTITUTION_WITH_STARTING_PARANTHESIS.latexFree().getAsLastNames(false));
    }

    @Test
    void fixAuthorForAlphabetization() {
        assertEquals("Smith, J.", AuthorList.fixAuthorForAlphabetization("John Smith"));
        assertEquals("Neumann, J.", AuthorList.fixAuthorForAlphabetization("John von Neumann"));
        assertEquals("Neumann, J.", AuthorList.fixAuthorForAlphabetization("J. von Neumann"));
        assertEquals(
                "Neumann, J. and Smith, J. and Black Brown, Jr., P.",
                AuthorList
                        .fixAuthorForAlphabetization("John von Neumann and John Smith and de Black Brown, Jr., Peter"));
    }

    @Test
    void size() {
        assertEquals(0, AuthorListTest.size(""));
        assertEquals(1, AuthorListTest.size("Bar"));
        assertEquals(1, AuthorListTest.size("Foo Bar"));
        assertEquals(1, AuthorListTest.size("Foo von Bar"));
        assertEquals(1, AuthorListTest.size("von Bar, Foo"));
        assertEquals(1, AuthorListTest.size("Bar, Foo"));
        assertEquals(1, AuthorListTest.size("Bar, Jr., Foo"));
        assertEquals(1, AuthorListTest.size("Bar, Foo"));
        assertEquals(2, AuthorListTest.size("John Neumann and Foo Bar"));
        assertEquals(2, AuthorListTest.size("John von Neumann and Bar, Jr, Foo"));

        assertEquals(3, AuthorListTest.size("John von Neumann and John Smith and Black Brown, Peter"));

        StringBuilder s = new StringBuilder("John von Neumann");
        for (int i = 0; i < 25; i++) {
            assertEquals(i + 1, AuthorListTest.size(s.toString()));
            s.append(" and Albert Einstein");
        }
    }

    @Test
    void isEmpty() {
        assertTrue(AuthorList.parse("").isEmpty());
        assertFalse(AuthorList.parse("Bar").isEmpty());
    }

    @Test
    void getEmptyAuthor() {
        assertThrows(Exception.class, () -> AuthorList.parse("").getAuthor(0));
    }

    @Test
    void getAuthor() {
        Author author = AuthorList.parse("John Smith and von Neumann, Jr, John").getAuthor(0);
        assertEquals(Optional.of("John"), author.getGivenName());
        assertEquals(Optional.of("J."), author.getGivenNameAbbreviated());
        assertEquals("John Smith", author.getGivenFamily(false));
        assertEquals("J. Smith", author.getGivenFamily(true));
        assertEquals(Optional.empty(), author.getNameSuffix());
        assertEquals(Optional.of("Smith"), author.getFamilyName());
        assertEquals("Smith, John", author.getFamilyGiven(false));
        assertEquals("Smith, J.", author.getFamilyGiven(true));
        assertEquals("Smith", author.getNamePrefixAndFamilyName());
        assertEquals("Smith, J.", author.getNameForAlphabetization());
        assertEquals(Optional.empty(), author.getNamePrefix());

        author = AuthorList.parse("Peter Black Brown").getAuthor(0);
        assertEquals(Optional.of("Peter Black"), author.getGivenName());
        assertEquals(Optional.of("P. B."), author.getGivenNameAbbreviated());
        assertEquals("Peter Black Brown", author.getGivenFamily(false));
        assertEquals("P. B. Brown", author.getGivenFamily(true));
        assertEquals(Optional.empty(), author.getNameSuffix());
        assertEquals(Optional.empty(), author.getNamePrefix());

        author = AuthorList.parse("John Smith and von Neumann, Jr, John").getAuthor(1);
        assertEquals(Optional.of("John"), author.getGivenName());
        assertEquals(Optional.of("J."), author.getGivenNameAbbreviated());
        assertEquals("John von Neumann, Jr", author.getGivenFamily(false));
        assertEquals("J. von Neumann, Jr", author.getGivenFamily(true));
        assertEquals(Optional.of("Jr"), author.getNameSuffix());
        assertEquals(Optional.of("Neumann"), author.getFamilyName());
        assertEquals("von Neumann, Jr, John", author.getFamilyGiven(false));
        assertEquals("von Neumann, Jr, J.", author.getFamilyGiven(true));
        assertEquals("von Neumann", author.getNamePrefixAndFamilyName());
        assertEquals("Neumann, Jr, J.", author.getNameForAlphabetization());
        assertEquals(Optional.of("von"), author.getNamePrefix());
    }

    @Test
    void companyAuthor() {
        Author author = AuthorList.parse("{JabRef Developers}").getAuthor(0);
        Author expected = new Author(null, null, null, "{JabRef Developers}", null);
        assertEquals(expected, author);
    }

    @Test
    void companyAuthorAndPerson() {
        Author company = new Author(null, null, null, "{JabRef Developers}", null);
        Author person = new Author("Stefan", "S.", null, "Kolb", null);
        assertEquals(Arrays.asList(company, person), AuthorList.parse("{JabRef Developers} and Stefan Kolb").getAuthors());
    }

    @Test
    void companyAuthorWithLowerCaseWord() {
        Author author = AuthorList.parse("{JabRef Developers on Fire}").getAuthor(0);
        Author expected = new Author(null, null, null, "{JabRef Developers on Fire}", null);
        assertEquals(expected, author);
    }

    @Test
    void abbreviationWithRelax() {
        Author author = AuthorList.parse("{\\relax Ch}ristoph Cholera").getAuthor(0);
        Author expected = new Author("{\\relax Ch}ristoph", "{\\relax Ch}.", null, "Cholera", null);
        assertEquals(expected, author);
    }

    @Test
    void getAuthorsNatbib() {
        assertEquals("", AuthorList.parse("").getAsNatbib());
        assertEquals("Smith", AuthorList.parse("John Smith").getAsNatbib());
        assertEquals("Smith and Black Brown", AuthorList.parse(
                "John Smith and Black Brown, Peter").getAsNatbib());
        assertEquals("von Neumann et al.", AuthorList.parse(
                "John von Neumann and John Smith and Black Brown, Peter").getAsNatbib());

        /*
         * [ 1465610 ] (Double-)Names containing hyphen (-) not handled correctly
         */
        assertEquals("Last-Name et al.", AuthorList.parse(
                "First Second Last-Name" + " and John Smith and Black Brown, Peter").getAsNatbib());

        // Test caching
        AuthorList al = AuthorList
                .parse("John von Neumann and John Smith and Black Brown, Peter");
        assertEquals(al.getAsNatbib(), al.getAsNatbib());
    }

    @Test
    void getAuthorsLastOnly() {
        // No comma before and
        assertEquals("", AuthorList.parse("").getAsLastNames(false));
        assertEquals("Smith", AuthorList.parse("John Smith").getAsLastNames(false));
        assertEquals("Smith", AuthorList.parse("Smith, Jr, John").getAsLastNames(
                false));

        assertEquals("von Neumann, Smith and Black Brown", AuthorList.parse(
                "John von Neumann and John Smith and Black Brown, Peter").getAsLastNames(false));
        // Oxford comma
        assertEquals("", AuthorList.parse("").getAsLastNames(true));
        assertEquals("Smith", AuthorList.parse("John Smith").getAsLastNames(true));
        assertEquals("Smith", AuthorList.parse("Smith, Jr, John").getAsLastNames(
                true));

        assertEquals("von Neumann, Smith, and Black Brown", AuthorList.parse(
                "John von Neumann and John Smith and Black Brown, Peter").getAsLastNames(true));

        assertEquals("von Neumann and Smith",
                AuthorList.parse("John von Neumann and John Smith").getAsLastNames(false));
    }

    @Test
    void getAuthorsLastFirstNoComma() {
        // No commas before and
        AuthorList al;

        al = AuthorList.parse("");
        assertEquals("", al.getAsLastFirstNames(true, false));
        assertEquals("", al.getAsLastFirstNames(false, false));

        al = AuthorList.parse("John Smith");
        assertEquals("Smith, John", al.getAsLastFirstNames(false, false));
        assertEquals("Smith, J.", al.getAsLastFirstNames(true, false));

        al = AuthorList.parse("John Smith and Black Brown, Peter");
        assertEquals("Smith, John and Black Brown, Peter", al.getAsLastFirstNames(false, false));
        assertEquals("Smith, J. and Black Brown, P.", al.getAsLastFirstNames(true, false));

        al = AuthorList.parse("John von Neumann and John Smith and Black Brown, Peter");
        // Method description is different than code -> additional comma
        // there
        assertEquals("von Neumann, John, Smith, John and Black Brown, Peter",
                al.getAsLastFirstNames(false, false));
        assertEquals("von Neumann, J., Smith, J. and Black Brown, P.", al.getAsLastFirstNames(true, false));

        al = AuthorList.parse("John Peter von Neumann");
        assertEquals("von Neumann, J. P.", al.getAsLastFirstNames(true, false));
    }

    @Test
    void getAuthorsLastFirstOxfordComma() {
        // Oxford comma
        AuthorList al;

        al = AuthorList.parse("");
        assertEquals("", al.getAsLastFirstNames(true, true));
        assertEquals("", al.getAsLastFirstNames(false, true));

        al = AuthorList.parse("John Smith");
        assertEquals("Smith, John", al.getAsLastFirstNames(false, true));
        assertEquals("Smith, J.", al.getAsLastFirstNames(true, true));

        al = AuthorList.parse("John Smith and Black Brown, Peter");
        assertEquals("Smith, John and Black Brown, Peter", al.getAsLastFirstNames(false, true));
        assertEquals("Smith, J. and Black Brown, P.", al.getAsLastFirstNames(true, true));

        al = AuthorList.parse("John von Neumann and John Smith and Black Brown, Peter");
        assertEquals("von Neumann, John, Smith, John, and Black Brown, Peter", al
                .getAsLastFirstNames(false, true));
        assertEquals("von Neumann, J., Smith, J., and Black Brown, P.", al.getAsLastFirstNames(
                true, true));

        al = AuthorList.parse("John Peter von Neumann");
        assertEquals("von Neumann, J. P.", al.getAsLastFirstNames(true, true));
    }

    @Test
    void getAuthorsLastFirstAnds() {
        assertEquals("Smith, John", AuthorList.parse("John Smith").getAsLastFirstNamesWithAnd(
                false));
        assertEquals("Smith, John and Black Brown, Peter", AuthorList.parse(
                "John Smith and Black Brown, Peter").getAsLastFirstNamesWithAnd(false));
        assertEquals("von Neumann, John and Smith, John and Black Brown, Peter", AuthorList
                .parse("John von Neumann and John Smith and Black Brown, Peter")
                .getAsLastFirstNamesWithAnd(false));
        assertEquals("von Last, Jr, First", AuthorList.parse("von Last, Jr ,First")
                                                      .getAsLastFirstNamesWithAnd(false));

        assertEquals("Smith, J.", AuthorList.parse("John Smith").getAsLastFirstNamesWithAnd(
                true));
        assertEquals("Smith, J. and Black Brown, P.", AuthorList.parse(
                "John Smith and Black Brown, Peter").getAsLastFirstNamesWithAnd(true));
        assertEquals("von Neumann, J. and Smith, J. and Black Brown, P.", AuthorList.parse(
                "John von Neumann and John Smith and Black Brown, Peter").getAsLastFirstNamesWithAnd(true));
        assertEquals("von Last, Jr, F.", AuthorList.parse("von Last, Jr ,First")
                                                   .getAsLastFirstNamesWithAnd(true));
    }

    @Test
    void getAuthorsFirstFirst() {
        AuthorList al;

        al = AuthorList.parse("");
        assertEquals("", al.getAsFirstLastNames(true, false));
        assertEquals("", al.getAsFirstLastNames(false, false));
        assertEquals("", al.getAsFirstLastNames(true, true));
        assertEquals("", al.getAsFirstLastNames(false, true));

        al = AuthorList.parse("John Smith");
        assertEquals("John Smith", al.getAsFirstLastNames(false, false));
        assertEquals("J. Smith", al.getAsFirstLastNames(true, false));
        assertEquals("John Smith", al.getAsFirstLastNames(false, true));
        assertEquals("J. Smith", al.getAsFirstLastNames(true, true));

        al = AuthorList.parse("John Smith and Black Brown, Peter");
        assertEquals("John Smith and Peter Black Brown", al.getAsFirstLastNames(false, false));
        assertEquals("J. Smith and P. Black Brown", al.getAsFirstLastNames(true, false));
        assertEquals("John Smith and Peter Black Brown", al.getAsFirstLastNames(false, true));
        assertEquals("J. Smith and P. Black Brown", al.getAsFirstLastNames(true, true));

        al = AuthorList.parse("John von Neumann and John Smith and Black Brown, Peter");
        assertEquals("John von Neumann, John Smith and Peter Black Brown", al.getAsFirstLastNames(
                false, false));
        assertEquals("J. von Neumann, J. Smith and P. Black Brown", al.getAsFirstLastNames(true,
                false));
        assertEquals("John von Neumann, John Smith, and Peter Black Brown", al
                .getAsFirstLastNames(false, true));
        assertEquals("J. von Neumann, J. Smith, and P. Black Brown", al.getAsFirstLastNames(true,
                true));

        al = AuthorList.parse("John Peter von Neumann");
        assertEquals("John Peter von Neumann", al.getAsFirstLastNames(false, false));
        assertEquals("John Peter von Neumann", al.getAsFirstLastNames(false, true));
        assertEquals("J. P. von Neumann", al.getAsFirstLastNames(true, false));
        assertEquals("J. P. von Neumann", al.getAsFirstLastNames(true, true));
    }

    @Test
    void getAuthorsFirstFirstAnds() {
        assertEquals("John Smith", AuthorList.parse("John Smith")
                                             .getAsFirstLastNamesWithAnd());
        assertEquals("John Smith and Peter Black Brown", AuthorList.parse(
                "John Smith and Black Brown, Peter").getAsFirstLastNamesWithAnd());
        assertEquals("John von Neumann and John Smith and Peter Black Brown", AuthorList
                .parse("John von Neumann and John Smith and Black Brown, Peter")
                .getAsFirstLastNamesWithAnd());
        assertEquals("First von Last, Jr. III", AuthorList
                .parse("von Last, Jr. III, First").getAsFirstLastNamesWithAnd());
    }

    @Test
    void getAuthorsForAlphabetization() {
        assertEquals("Smith, J.", AuthorList.parse("John Smith")
                                            .getForAlphabetization());
        assertEquals("Neumann, J.", AuthorList.parse("John von Neumann")
                                              .getForAlphabetization());
        assertEquals("Neumann, J.", AuthorList.parse("J. von Neumann")
                                              .getForAlphabetization());
        assertEquals("Neumann, J. and Smith, J. and Black Brown, Jr., P.", AuthorList
                .parse("John von Neumann and John Smith and de Black Brown, Jr., Peter")
                .getForAlphabetization());
    }

    @Test
    void removeStartAndEndBraces() {
        assertEquals("{A}bbb{c}", AuthorList.parse("{A}bbb{c}").getAsLastNames(false));
        assertEquals("{Vall{\\'e}e Poussin}", AuthorList.parse("{Vall{\\'e}e Poussin}").getAsLastNames(false));
        assertEquals("Poussin", AuthorList.parse("{Vall{\\'e}e} {Poussin}").getAsLastNames(false));
        assertEquals("Poussin", AuthorList.parse("Vall{\\'e}e Poussin").getAsLastNames(false));
        assertEquals("Lastname", AuthorList.parse("Firstname {Lastname}").getAsLastNames(false));
        assertEquals("{Firstname Lastname}", AuthorList.parse("{Firstname Lastname}").getAsLastNames(false));
    }

    @Test
    void createCorrectInitials() {
        assertEquals(Optional.of("J. G."),
                AuthorList.parse("Hornberg, Johann Gottfried").getAuthor(0).getGivenNameAbbreviated());
    }

    @Test
    void parseNameWithBracesAroundFirstName() {
        // TODO: Be more intelligent and abbreviate the first name correctly
        Author expected = new Author("Tse-tung", "{Tse-tung}.", null, "Mao", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("{Tse-tung} Mao"));
    }

    @Test
    void parseNameWithBracesAroundLastName() {
        Author expected = new Author("Hans", "H.", null, "van den Bergen", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("{van den Bergen}, Hans"));
    }

    @Test
    void parseNameWithHyphenInFirstName() {
        Author expected = new Author("Tse-tung", "T.-t.", null, "Mao", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("Tse-tung Mao"));
    }

    @Test
    void parseNameWithHyphenInLastName() {
        Author expected = new Author("Firstname", "F.", null, "Bailey-Jones", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("Firstname Bailey-Jones"));
    }

    @Test
    void parseNameWithHyphenInLastNameWithInitials() {
        Author expected = new Author("E. S.", "E. S.", null, "El-{M}allah", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("E. S. El-{M}allah"));
    }

    @Test
    void parseNameWithHyphenInLastNameWithEscaped() {
        Author expected = new Author("E. S.", "E. S.", null, "{K}ent-{B}oswell", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("E. S. {K}ent-{B}oswell"));
    }

    @Test
    void parseNameWithHyphenInLastNameWhenLastNameGivenFirst() {
        // TODO: Fix abbreviation to be "A."
        Author expected = new Author("ʿAbdallāh", "ʿ.", null, "al-Ṣāliḥ", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("al-Ṣāliḥ, ʿAbdallāh"));
    }

    @Test
    @Disabled("Has issues with space character in W-P.")
    void parseWithDash() {
        assertEquals(
                AuthorList.of(
                        new Author("Z.", "Z.", null, "Yao", null),
                        new Author("D. S.", "D. S.", null, "Weld", null),
                        new Author("W-P.", "W-P.", null, "Chen", null),
                        new Author("H.", "H.", null, "Sun", null)
                ),
                AuthorList.parse("Z. Yao, D. S. Weld, W.-P. Chen, and H. Sun"));
    }

    @Test
    void parseNameWithBraces() {
        Author expected = new Author("H{e}lene", "H.", null, "Fiaux", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("H{e}lene Fiaux"));
    }

    @Test
    void parseFirstNameFromFirstAuthorMultipleAuthorsWithLatexNames() {
        assertEquals("Mu{\\d{h}}ammad",
                AuthorList.parse("Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm")
                          .getAuthor(0).getGivenName().orElse(null));
    }

    @Test
    void parseFirstNameFromSecondAuthorMultipleAuthorsWithLatexNames() {
        assertEquals("Corrado",
                AuthorList.parse("Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm")
                          .getAuthor(1).getGivenName().orElse(null));
    }

    @Test
    void parseLastNameFromFirstAuthorMultipleAuthorsWithLatexNames() {
        assertEquals("al-Khw{\\={a}}rizm{\\={i}}",
                AuthorList.parse("Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm")
                          .getAuthor(0).getFamilyName().orElse(null));
    }

    @Test
    void parseLastNameFromSecondAuthorMultipleAuthorsWithLatexNames() {
        assertEquals("B{\\\"o}hm",
                AuthorList.parse("Mu{\\d{h}}ammad al-Khw{\\={a}}rizm{\\={i}} and Corrado B{\\\"o}hm")
                          .getAuthor(1).getFamilyName().orElse(null));
    }

    @Test
    void parseInstitutionAuthorWithLatexNames() {
        assertEquals("{The Ban\\={u} M\\={u}s\\={a} brothers}",
                AuthorList.parse("{The Ban\\={u} M\\={u}s\\={a} brothers}").getAuthor(0).getFamilyName().orElse(null));
    }

    @Test
    void parseRetrieveCachedAuthorListAfterGarbageCollection() {
        final String uniqueAuthorName = "Osvaldo Iongi";
        AuthorList author = AuthorList.parse(uniqueAuthorName);
        System.gc();
        assertSame(author, AuthorList.parse(uniqueAuthorName));
    }

    @Test
    void parseGarbageCollectAuthorListForUnreachableKey() {
        final String uniqueAuthorName = "Fleur Hornbach";
        // Note that "new String()" is needed, uniqueAuthorName is a reference to a String literal
        AuthorList uniqueAuthor = AuthorList.parse(new String(uniqueAuthorName));
        System.gc();
        assertNotSame(uniqueAuthor, AuthorList.parse(uniqueAuthorName));
    }

    @Test
    void parseGarbageCollectUnreachableInstitution() {
        final String uniqueInstitutionName = "{Unique LLC}";
        // Note that "new String()" is needed, uniqueInstitutionName is a reference to a String literal
        AuthorList uniqueInstitution = AuthorList.parse(new String(uniqueInstitutionName));
        System.gc();
        assertNotSame(uniqueInstitution, AuthorList.parse(uniqueInstitutionName));
    }

    /**
     * This tests an unreachable key issue addressed in [#6552](https://github.com/JabRef/jabref/pull/6552). The test is incorrect BibTeX but is handled by the parser and common in practice.
     */
    @Test
    void parseCacheAuthorsWithTwoOrMoreCommasAndWithSpaceInAllParts() {
        final String uniqueAuthorsNames = "Basil Dankworth, Gianna Birdwhistle, Cosmo Berrycloth";
        AuthorList uniqueAuthors = AuthorList.parse(uniqueAuthorsNames);
        System.gc();
        assertSame(uniqueAuthors, AuthorList.parse(uniqueAuthorsNames));
    }

    /**
     * This tests an unreachable key issue addressed in [#6552](https://github.com/JabRef/jabref/pull/6552).
     */
    @Test
    void parseCacheAuthorsWithTwoOrMoreCommasAndWithoutSpaceInAllParts() {
        final String uniqueAuthorsNames = "Dankworth, Jr., Braelynn";
        AuthorList uniqueAuthors = AuthorList.parse(uniqueAuthorsNames);
        System.gc();
        assertSame(uniqueAuthors, AuthorList.parse(uniqueAuthorsNames));
    }

    /**
     * This tests the issue described at https://github.com/JabRef/jabref/pull/2669#issuecomment-288519458
     */
    @Test
    void correctNamesWithOneComma() {
        Author expected = new Author("Alexander der Große", "A. d. G.", null, "Canon der Barbar", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("Canon der Barbar, Alexander der Große"));

        expected = new Author("Alexander H. G.", "A. H. G.", null, "Rinnooy Kan", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("Rinnooy Kan, Alexander H. G."));

        expected = new Author("Alexander Hendrik George", "A. H. G.", null, "Rinnooy Kan", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("Rinnooy Kan, Alexander Hendrik George"));

        expected = new Author("José María", "J. M.", null, "Rodriguez Fernandez", null);
        assertEquals(AuthorList.of(expected), AuthorList.parse("Rodriguez Fernandez, José María"));
    }

    @Test
    void equalsFalseDifferentOrder() {
        Author firstAuthor = new Author("A", null, null, null, null);
        Author secondAuthor = new Author("B", null, null, null, null);
        AuthorList firstAuthorList = AuthorList.of(firstAuthor, secondAuthor);
        AuthorList secondAuthorList = AuthorList.of(secondAuthor, firstAuthor);
        assertNotEquals(firstAuthorList, secondAuthorList);
    }

    @Test
    void equalsFalseWhenNotAuthorList() {
        assertNotEquals(AuthorList.of(new Author(null, null, null, null, null)),
                new Author(null, null, null, null, null));
    }

    @Test
    void equalsTrueReflexive() {
        AuthorList authorList = AuthorList.of(new Author(null, null, null, null, null));
        assertEquals(authorList, authorList);
    }

    @Test
    void equalsTrueSymmetric() {
        AuthorList firstAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        AuthorList secondAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        assertEquals(firstAuthorList, secondAuthorList);
        assertEquals(secondAuthorList, firstAuthorList);
    }

    @Test
    void equalsTrueTransitive() {
        AuthorList firstAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        AuthorList secondAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        AuthorList thirdAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        assertEquals(firstAuthorList, secondAuthorList);
        assertEquals(secondAuthorList, thirdAuthorList);
        assertEquals(firstAuthorList, thirdAuthorList);
    }

    @Test
    void equalsTrueConsistent() {
        AuthorList firstAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        AuthorList secondAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        assertEquals(firstAuthorList, secondAuthorList);
        assertEquals(firstAuthorList, secondAuthorList);
        assertEquals(firstAuthorList, secondAuthorList);
    }

    @Test
    void equalsFalseForNull() {
        assertNotEquals(null, AuthorList.of(new Author(null, null, null, null, null)));
    }

    @Test
    void hashCodeConsistent() {
        AuthorList authorList = AuthorList.of(new Author(null, null, null, null, null));
        assertEquals(authorList.hashCode(), authorList.hashCode());
    }

    @Test
    void hashCodeNotConstant() {
        AuthorList firstAuthorList = AuthorList.of(new Author("A", null, null, null, null));
        AuthorList secondAuthorList = AuthorList.of(new Author("B", null, null, null, null));
        assertNotEquals(firstAuthorList.hashCode(), secondAuthorList.hashCode());
    }

    @Test
    void getAsLastFirstFirstLastNamesWithAndEmptyAuthor() {
        assertEquals("",
                EMPTY_AUTHOR.getAsLastFirstFirstLastNamesWithAnd(true));
    }

    @Test
    void getAsLastFirstFirstLastNamesWithAndMultipleAuthors() {
        assertEquals("al-Khw{\\={a}}rizm{\\={i}}, M. and C. B{\\\"o}hm and K. G{\\\"{o}}del",
                THREE_AUTHORS_WITH_LATEX.getAsLastFirstFirstLastNamesWithAnd(true));
    }
}
