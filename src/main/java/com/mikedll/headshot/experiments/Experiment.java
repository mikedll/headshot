package com.mikedll.headshot.experiments;

import java.lang.InterruptedException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.lang.Runnable;

import org.javatuples.Pair;

import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.User;

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
    
    public void run(UserRepository userRepository) {
        int count = 2;
        List<Thread> list = new ArrayList<Thread>(count);
        Runnable sleeper = buildSleep();
        for(int i = 0; i < count; i++) {
            // System.out.println("userRepository from getBean() is " + userRepository);
            final int iCopy = i;
            Thread td = new Thread() {
                    public void run() {
                        if(iCopy == 0) {
                            // sleeper.run();
                        }
                        System.out.println("We have " + userRepository.count().getValue0() + " users");
                        Pair<Optional<User>,String> userFetch = userRepository.findById(1L);
                        User user = userFetch.getValue0().orElse(null);
                        if(user != null) {
                            System.out.println("User name: " + user.getName());
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
            // sleeper.run();
        }
    }
}
