package com.mikedll.headshot.util;

import java.nio.charset.StandardCharsets;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.nio.charset.CharacterCodingException;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import org.javatuples.Pair;

public class Decoding {

    public static Pair<String,DecodingError> decode(byte[] bytes) {
        CharBuffer result = null;
        try {
            result = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
        } catch (MalformedInputException ex) {
            return Pair.with(null, DecodingError.MALFORMED_INPUT);
        } catch (UnmappableCharacterException ex) {
            return Pair.with(null, DecodingError.UNMAPPABLE_CHARACTER);
        } catch (CharacterCodingException ex) {
            return Pair.with(null, DecodingError.EXCEPTION);
        }

        return Pair.with(result.toString(), null);
    }
    
}
