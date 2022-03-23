package org.camunda.complexvariable.c7.data;

import java.util.Arrays;

/**
 * This class update the Customer Variable, to track the modification
 */
public class CustomerUpdate {
    public static Customer update(Customer customer, String stamp, String logUpdate) {
        customer.setFirstName((customer.getFirstName() == null ? "" : customer.getFirstName()) + stamp+", ");
        customer.setLastName(stamp);
        customer.setAge(customer.getAge() + 10);
        customer.setListColors(Arrays.asList("Blue", "Red", "Green"));
        return customer;
    }

}
