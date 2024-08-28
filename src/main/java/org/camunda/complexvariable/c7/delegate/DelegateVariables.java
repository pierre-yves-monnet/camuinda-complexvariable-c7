package org.camunda.complexvariable.c7.delegate;

import camundajar.impl.com.google.gson.Gson;
import camundajar.impl.com.google.gson.JsonObject;
import camundajar.impl.com.google.gson.JsonParser;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.complexvariable.c7.data.Customer;
import org.camunda.complexvariable.c7.data.CustomerUpdate;
import org.camunda.complexvariable.c7.process.complexvariables.ComplexVariableConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class DelegateVariables implements JavaDelegate {

    @Autowired
    ProcessEngine processEngine;

    private static final List<String> riskColors = Arrays.asList("Red", "Blue", "Green", "Orange");
    private final Logger logger = LoggerFactory.getLogger(DelegateVariables.class.getName());

    /**
     * JavaDelegate: populate variables
     *
     * @param delegateExecution expression
     * @throws Exception in case of errors
     */
    public void execute(DelegateExecution delegateExecution) throws Exception {
        List<String> logs = new ArrayList<>();
        logs.add(updateJsonBased(delegateExecution));
        logs.add(updateJavaBased(delegateExecution));
        logs.add(updateDirectBased(delegateExecution));

        if (processEngine!=null) {
            HistoryService historyService = processEngine.getHistoryService();

            HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
            query.executionIdIn("a", "b", "c");
        }

        Random r = new Random(System.currentTimeMillis());

        delegateExecution.setVariable("riskLevel", riskColors.get(r.nextInt(riskColors.size())));

        String logSt = (String) delegateExecution.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_LOGS);
        logSt = (logSt == null ? "" : logSt+",") + logs.stream().collect(Collectors.joining(", "));
        delegateExecution.setVariable(ComplexVariableConstant.PROCESS_VARIABLE_LOGS, logSt);

    }

    /**
     * Access and modify a JSON variables
     *
     * @param delegateExecution manipulate task
     * @return a status
     */
    public String updateJsonBased(DelegateExecution delegateExecution) {
        try {
            Object currentCustomer = delegateExecution.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON);
            Customer customer;
            if (currentCustomer != null) {
                ObjectValue customerValue = delegateExecution.getVariableTyped(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON);
                String customerJson = customerValue.getValueSerialized();
                Gson gson = new Gson();
                JsonParser parser = new JsonParser();
                JsonObject object = (JsonObject) parser.parse(customerJson);// response will be the json String
                customer = gson.fromJson(object, Customer.class);
            } else {
                customer = new Customer();
            }
            customer = CustomerUpdate.update(customer, "DeleRobin", "Delegate");
            // serialize in JAVA
            ObjectValue typedObjectVariable = Variables.objectValue(customer).serializationDataFormat(Variables.SerializationDataFormats.JSON).create();

            delegateExecution.setVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON, typedObjectVariable);
            return "Json Delegate OK";
        } catch (Exception e) {
            logger.info("WorkerVariables.executeInJson Exception [" + e + "]");
            return "Json Delegate failed [" + e + "]";

        }
    }

    /**
     * Access and modify a Serialized variables in JAVA
     *
     * @param delegateExecution manipulate task
     * @return a status
     */
    public String updateJavaBased(DelegateExecution delegateExecution) {
        try {
            Object currentCustomer = delegateExecution.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JAVA);
            Customer customer;
            if (currentCustomer != null) {
                ObjectValue customerObject = delegateExecution.getVariableTyped(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JAVA);
                customer = (Customer) customerObject.getValue();
                logger.info("-----> Customer Name = {} {}", customer.getFirstName(), customer.getLastName());
            } else {
                customer = new Customer();
            }

            customer = CustomerUpdate.update(customer, "DeleRobin", "Delegate");

            // serialize in JAVA
            ObjectValue typedObjectVariable = Variables.objectValue(customer).serializationDataFormat(Variables.SerializationDataFormats.JAVA).create();

            delegateExecution.setVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JAVA, typedObjectVariable);
            return "Java Delegate OK";
        } catch (Exception e) {
            logger.info("WorkerVariables.executeInJava Exception [" + e + "]");
            return "Java Delegate failed [" + e + "]";
        }

    }

    /**
     * Access and modify directly a variable
     *
     * @param delegateExecution manipulate task
     * @return a status
     */
    public String updateDirectBased(DelegateExecution delegateExecution) {
        try {
            Object currentCustomer = delegateExecution.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER);
            Customer customer;
            if (currentCustomer instanceof Customer) {
                customer = (Customer) currentCustomer;
            } else {
                customer = new Customer();
            }

            customer = CustomerUpdate.update(customer, "DeleRobin", "Delegate");

            // a JAVA serialization will be done behind the scene
            delegateExecution.setVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER, customer);
            return "Direct Delegate OK";
        } catch (Exception e) {
            logger.info("WorkerVariables.executeDirect Exception [" + e + "]");
            return "Direct Delegate failed [" + e + "]";
        }
    }

}

