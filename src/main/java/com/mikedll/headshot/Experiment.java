package com.mikedll.headshot;

import java.util.List;
import java.util.ArrayList;
import java.lang.InterruptedException;

public class Experiment {

    public void run() {
        int count = 5;
        List<Thread> list = new ArrayList<Thread>(count);
        for(int i = 0; i < count; i++) {
            UserRepository userRepository = Application.appCtx.getBean(UserRepository.class);
            Thread td = new Thread() {
                    public void run() {
                        System.out.println("We have " + userRepository.count() + " users");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            System.out.println("sleep interupted");
                        }
                    }
                };
            list.add(td);
            td.start();
        }

        list.forEach((t) -> {
                try {
                    System.out.println("Calling join on " + t);
                    t.join();
                    System.out.println("Join finished");
                } catch (InterruptedException ex ) {
                    System.out.println("InterruptedException message: " + ex.getMessage());
                }
            });

        if(count == 0) {
            try {
                System.out.println("sleeping because count is 0");
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.out.println("sleept interupted");
            }
        }
    }
}
