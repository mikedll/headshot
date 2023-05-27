
package com.mikedll.headshot;

import java.io.IOException;
import java.util.Calendar;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginController extends Controller {

    public void getLoginPage(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        
        templateEngine.process("oauth2_login", defaultCtx(req), res.getWriter());
    }
}
