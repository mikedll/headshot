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

    public static Pair<String,String> decode(byte[] bytes) {
        CharBuffer result = null;
        try {
            result = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
        } catch (MalformedInputException ex) {
            return Pair.with(null, "MalformedInputException: " + ex.getMessage());
        } catch (UnmappableCharacterException ex) {
            return Pair.with(null, "UnmappableCharacterException: " + ex.getMessage());
        } catch (CharacterCodingException ex) {
            throw new RuntimeException("CharacterCodingException while decoding byte array", ex);
        }

        return Pair.with(result.toString(), null);
    }
    
}
