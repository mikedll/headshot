
package com.mikedll.headshot;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.mikedll.headshot.controller.Scanner;
import com.mikedll.headshot.controller.RequestHandler;

import org.javatuples.Pair;

public class RequestAnnotationTests {
    
    @Test
    public void searchParamsCorrect() {
        Pair<List<RequestHandler>,String> result = new Scanner().scan();
        Assertions.assertNull(result.getValue1());

        List<RequestHandler> requestHandlers = result.getValue0();
        Assertions.assertTrue(requestHandlers.size() > 0);

        RequestHandler found = requestHandlers.stream()
            .filter(rh -> rh.path.equals("/")).findAny().orElse(null);
        Assertions.assertNotNull(found);
    }

    @Test
    public void requestOnNonController() {
        Scanner scanner = new Scanner();
        scanner.setScanPath("classpath*:com/mikedll/headshot/notcontroller/*.class");

        Pair<List<RequestHandler>,String> result = scanner.scan();
        String expected = "Error when building handler for method com.mikedll.headshot.notcontroller.NotController.index(): "
            + "Class com.mikedll.headshot.notcontroller.NotController of method 'index' is not a subclass of Controller";
            
        Assertions.assertEquals(expected, result.getValue1());
    }
}
