
package com.mikedll.headshot.experiment;

public class Lion extends Animal {

    public Lion(String name, Integer age) {
        super(name, age);
    }
    
    public String speak() {
        return "Roar, my name is " + this.name + " and my age is " + age;
    }

}
