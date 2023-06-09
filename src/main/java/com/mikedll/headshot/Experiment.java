package com.mikedll.headshot;

import java.lang.InterruptedException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import java.lang.Runnable;

public class Experiment {

    public Runnable buildSleep() {
        return () -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                System.out.println("sleep encountered InterruptedException");
            }
        };
    }        
    
    public void run() {
        int count = 2;
        List<Thread> list = new ArrayList<Thread>(count);
        Runnable sleeper = buildSleep();
        for(int i = 0; i < count; i++) {
            UserRepository userRepository = Application.appCtx.getBean(UserRepository.class);
            final int iCopy = i;
            Thread td = new Thread() {
                    public void run() {
                        if(iCopy == 0) {
                            // sleeper.run();
                        }
                        System.out.println("We have " + userRepository.count() + " users");
                        Optional<User> user = userRepository.findById(new Long(1));
                        if(user.isPresent()) {
                            System.out.println("User name: " + user.get().getName());
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
            sleeper.run();
        } else {
            sleeper.run();
        }
    }
}
