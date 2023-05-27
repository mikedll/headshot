
package com.mikedll.headshot;

import java.io.IOException;
import java.util.Calendar;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;

public class LoginController extends AbstractController {

    public void getLoginPage(HttpServletRequest req, HttpServletResponse res)
        throws IOException {

        Context ctx = new Context(req.getLocale());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        ctx.setVariable("today", dateFormat.format(Calendar.getInstance().getTime()));
        
        templateEngine.process("oauth2_login", ctx, res.getWriter());
    }
}
