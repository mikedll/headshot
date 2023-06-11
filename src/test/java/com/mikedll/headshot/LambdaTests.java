package com.mikedll.headshot;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import com.mikedll.headshot.experiment.*;

public class LambdaTests {
    
    @State(Scope.Benchmark)
    public static class LambaHandlerPlan {
        public List<RequestHandler> requestHandlers;

        public Experiment2 exp = new Experiment2();

        private String[] names = new String[] { "Minny", "Mickey", "Tom" };
        
        private Integer[] ages = new Integer[] { 21, 25, 39 };
        
        @Setup(Level.Invocation)
        public void setUp() {
            this.requestHandlers = exp.findHandlers();
            if(this.requestHandlers == null) {
                throw new RuntimeException("unable to find handlers");
            }
        }
    }    

    @Benchmark
    @Fork(value = 0, warmups = 0)
    @Warmup(iterations = 1)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @BenchmarkMode(Mode.AverageTime)    
    public void runTests(LambaHandlerPlan plan) {
        if(plan.requestHandlers.size() == 0) {
            System.out.println("Request specs size was 0, returning early");
            return;
        }

        // System.out.println("Request handlers:");
        // plan.requestHandlers.forEach(rh -> System.out.println(rh));

        String name = plan.names[(int)(Math.random() * 3.0)];
        Integer age = plan.ages[(int)(Math.random() * 3.0)];
        RequestHandler requestHandler = plan.requestHandlers.get((int)(Math.random() * plan.requestHandlers.size()));

        Request request = new Request(requestHandler.path, requestHandler.method, name, age);

        plan.exp.dispatch(plan.requestHandlers, request);
    }  
}
