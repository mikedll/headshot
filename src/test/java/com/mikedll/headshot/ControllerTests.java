
package com.mikedll.headshot;

import java.io.IOException;

import java.io.StringWriter;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.controller.Controller;

public class ControllerTests {

    @BeforeAll
    public static void setUp() throws IOException {
        MySuite.setUp();
    }
    
    @Test
    public void testRootLoggedIn() throws IOException, ServletException {
        Servlet servlet = new Servlet();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        when(req.getRequestURI()).thenReturn("/");
        when(req.getMethod()).thenReturn("GET");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(res.getWriter()).thenReturn(printWriter);
        
        servlet.doGet(req, res);

        printWriter.flush();
        Assertions.assertTrue(stringWriter.toString().contains("This is the app"));
    }
}
