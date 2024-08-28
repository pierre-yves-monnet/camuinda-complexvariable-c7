package org.camunda.complexvariable.c7.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Complex Data Customer, with firstName, lastName, age, listOfPreferateColors
 * ATTENTION: the serializer use get() method do serialize in JSON
 */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String firstName;
    private String lastName;
    private int age;
    private List<String> listColors;

    public Customer() {
    }


    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getListColors() {
        return listColors;
    }

    public void setListColors(List<String> listColors) {
        this.listColors = listColors;
    }


}
