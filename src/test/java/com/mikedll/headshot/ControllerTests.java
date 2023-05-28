
package com.mikedll.headshot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class ControllerTests {

    public class MyController extends Controller {
    }
    
    @Test
    public void testCookies() {
        Env.cookieSigningKey = "eVKgwkis9APaD2o2/suPAv9sgs156+fMTBDDbM1vgwU=";
        MyController controller = new MyController();
        // controller.beforeFilters();
        // controller.session.get("
    }
}
