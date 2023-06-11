
package com.mikedll.headshot.experiment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Tacky {
    
    String path();

    String method() default "GET";
}
  
