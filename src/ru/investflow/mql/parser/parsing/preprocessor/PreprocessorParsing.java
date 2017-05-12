package ru.investflow.mql.parser.parsing.preprocessor;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mql.parser.parsing.util.ParsingErrors;
import ru.investflow.mql.parser.parsing.util.ParsingUtils;
import ru.investflow.mql.psi.MQL4Elements;

import static ru.investflow.mql.parser.parsing.preprocessor.PreprocessorIfDefParsing.parseUndef;
import static ru.investflow.mql.parser.parsing.preprocessor.PreprocessorIncludeParsing.parseInclude;
import static ru.investflow.mql.parser.parsing.preprocessor.PreprocessorPropertyParsing.parseProperty;
import static ru.investflow.mql.parser.parsing.util.ParsingErrors.error;
import static ru.investflow.mql.parser.parsing.util.ParsingUtils.advanceLexerUntil;
import static ru.investflow.mql.parser.parsing.util.ParsingErrors.advanceWithError;
import static ru.investflow.mql.parser.parsing.util.ParsingUtils.containsEndOfLineOrFile;
import static ru.investflow.mql.parser.parsing.util.TokenAdvanceMode.ADVANCE;

public class PreprocessorParsing implements MQL4Elements {

    public static final TokenSet NEW_LINE_OR_SEMICOLON = TokenSet.create(LINE_TERMINATOR, SEMICOLON);

    public static boolean parsePreprocessorBlock(PsiBuilder b) {
        //parseDefine(b, l + 1)
        //parseIfDef(b, l + 1)
        //parseImport(b, l + 1)

        return parseInclude(b)
                || parseProperty(b)
                || parseUndef(b);
    }


    /**
     * Checks that there is no line break between start & current offsets.
     *
     * @return false if there are line breaks in the given range.
     */
    public static boolean assertNoLineBreaksInRange(PsiBuilder b, int startOffset, @NotNull String errorMessage) {
        return assertNoLineBreaksInRange(b, startOffset, b.getCurrentOffset(), errorMessage);
    }

    /**
     * Checks that there is no line break between start & end offsets.
     *
     * @return false if there are line breaks in the given range.
     */
    public static boolean assertNoLineBreaksInRange(PsiBuilder b, int startOffset, int endOffset, @NotNull String errorMessage) {
        boolean hasEol = hasLineBreaks(b, startOffset, endOffset);
        if (hasEol) {
            error(b, errorMessage);
            return false;
        }
        return true;
    }

    public static boolean hasLineBreaks(PsiBuilder b, int startOffset, int endOffset) {
        String text = b.getOriginalText().subSequence(startOffset, endOffset).toString();
        return ParsingUtils.containsEndOfLine(text);
    }

    public static void completePPStatement(@NotNull PsiBuilder b, int offset, @NotNull String errorMessage) {
        advanceWithError(b, errorMessage);
        if (!containsEndOfLineOrFile(b, offset)) { // line ends after identifier -> end of block
            advanceLexerUntil(b, NEW_LINE_OR_SEMICOLON, ADVANCE);
        }
    }


    /**
     * Adds error if (current token is not semicolon or not end of file or there is no line break between current token and statementEnd position).
     */
    public static void completePPStatement(PsiBuilder b, int statementEnd) {
        if (b.getOriginalText().length() == b.getCurrentOffset()) { // end of file
            return;
        }
        if (b.getTokenType() == SEMICOLON) { // statement terminator
            return;
        }
        boolean hasEol = hasLineBreaks(b, statementEnd, b.getCurrentOffset());
        if (!hasEol) {
            error(b, ParsingErrors.UNEXPECTED_TOKEN);
        }
    }
}
