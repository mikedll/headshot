
package com.mikedll.headshot.experiment;

public class Dog extends Animal {

    public Dog(String name, Integer age) {
        super(name, age);
    }
    
    public String speak() {
        return "Woof woof, my name is " + this.name + " and my age is " + age;
    }

    @Tacky(path="/bark")    
    public String bark() {
        return "ARF! my name is " + this.name;
    }
}
