
package com.mikedll.headshot.experiment;

public class Cat extends Animal {

    public Cat(String name, Integer age) {
        super(name, age);
    }
    
    public String speak() {
        return "Meow meow, my name is " + this.name + " and my age is " + age;
    }

    @Tacky(path="/purr")
    public String purr() {
        return "purr";
    }

}
