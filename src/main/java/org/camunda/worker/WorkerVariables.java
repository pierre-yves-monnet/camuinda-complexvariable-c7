package org.camunda.worker;

import camundajar.impl.com.google.gson.Gson;
import camundajar.impl.com.google.gson.JsonObject;
import camundajar.impl.com.google.gson.JsonParser;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.complexvariable.c7.data.Customer;
import org.camunda.complexvariable.c7.data.CustomerUpdate;
import org.camunda.complexvariable.c7.process.complexvariables.ComplexVariableConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorkerVariables {
  private final Logger logger = LoggerFactory.getLogger(WorkerVariables.class.getName());

  public static void main(String[] args) {
    WorkerVariables workerVariable = new WorkerVariables();
    workerVariable.registerWorker();
  }

  // Attention, the topic "work-to-do" must be handled at a time by only one bean.
  // So, when you replace the value here, check that all other Bean does not get it.
  public void registerWorker() {
    String baseUrl = "http://localhost:8080/engine-rest";
    ExternalTaskClient client = ExternalTaskClient.create()
        .baseUrl(baseUrl)
        .workerId("complexVariable_2")
        .maxTasks(10)
        .lockDuration(4000)
        .asyncResponseTimeout(20000)
        .backoffStrategy( new ExponentialBackoffStrategy())
        .build();

    client.subscribe("complex-variables").lockDuration(10000).handler((externalTask, externalTaskService) -> {
      this.handleWorkerVariable(externalTask, externalTaskService);
    }).open();


    client.subscribe("ff").lockDuration(10000).handler((externalTask, externalTaskService) -> {
      this.handleWorkerVariable(externalTask, externalTaskService);
    }).open();

  }

  public void handleWorkerVariable(org.camunda.bpm.client.task.ExternalTask externalTask,
                                   ExternalTaskService externalTaskService) {
    logger.info("WorkerVariables.handleWorkerVariable : >>>>>>>>>>> start [" + externalTask.getId() + "]");
    Map<String, Object> variables = new HashMap<>();
    List<String> logs = new ArrayList<>();
    try {
      logs.add(updateJsonBased(externalTask, variables));
      // Here , we will have an exception. The class loader detect that the Java class was instantiate from a different class loader, so this
      // method does not work
      logs.add(updateJavaBased(externalTask, variables));
      // same here
      logs.add(updateDirectBased(externalTask, variables));

      String logSt = externalTask.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_LOGS);
      logSt = (logSt == null ? "" : logSt) + logs.stream().collect(Collectors.joining(", "));
      variables.put(ComplexVariableConstant.PROCESS_VARIABLE_LOGS, logSt);


      externalTaskService.complete(externalTask, variables);


    } catch (Exception e) {
      logger.info("WorkerVariables.handleWorkerVariable Exception [" + e + "]");

    }
    logger.info("WorkerVariables.handleWorkerVariable : >>>>>>>>>>>> end [" + externalTask.getId() + "]");

  }

  /**
   * Access and modify a JSON variables
   *
   * @param externalTask manipulate task
   * @return a status
   */
  public String updateJsonBased(ExternalTask externalTask, Map<String, Object> variables) {
    try {
      Object currentCustomer = externalTask.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON);
      Customer customer;
      if (currentCustomer != null) {
        ObjectValue customerValue = externalTask.getVariableTyped(
            ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON);
        String customerJson = customerValue.getValueSerialized();
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject) parser.parse(customerJson);// response will be the json String
        customer = gson.fromJson(object, Customer.class);
      } else
        customer = new Customer();
      customer = CustomerUpdate.update(customer, "WorkHood", "Worker");

      // serialize in JAVA
      ObjectValue typedObjectVariable = Variables.objectValue(customer)
          .serializationDataFormat(Variables.SerializationDataFormats.JSON)
          .create();

      variables.put(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JSON, typedObjectVariable);
      return "Json Worker OK";
    } catch (Exception e) {
      logger.info("WorkerVariables.executeInJson Exception [" + e + "]");
      return "Json Worker failed [" + e + "]";
    }
  }

  /**
   * Access and modify a Serialized variables in JAVA
   *
   * @param externalTask manipulate task
   * @return a status
   */
  public String updateJavaBased(ExternalTask externalTask, Map<String, Object> variables) {
    try {
      Object currentCustomer = externalTask.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JAVA);
      Customer customer;

      if (currentCustomer != null) {
        ObjectValue customerObject = externalTask.getVariableTyped(
            ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JAVA);
        customer = (Customer) customerObject.getValue();
        logger.info("-----> Customer Name = {} {}", customer.getFirstName(), customer.getLastName());
      } else {
        customer = new Customer();
      }
      customer = CustomerUpdate.update(customer, "WorkHood", "Worker");

      // serialize in JAVA
      ObjectValue typedObjectVariable = Variables.objectValue(customer)
          .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
          .create();

      // If we set this variable, we will got a
      // ENGINE-17007 Cannot set variable with name customerInJava. Java serialization format is prohibited
      // variables.put(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER_IN_JAVA, typedObjectVariable);
      return "Java Worker SKIP";
    } catch (Exception e) {
      logger.info("WorkerVariables.executeInJava Exception [" + e + "]");
      return "Java Worker failed [" + e + "]";
    }
  }

  /**
   * Access and modify directly a variable
   *
   * @param externalTask manipulate task
   * @return a status
   */
  public String updateDirectBased(ExternalTask externalTask, Map<String, Object> variables) {
    try {
      Object currentCustomer = externalTask.getVariable(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER);
      Customer customer;
      if (currentCustomer instanceof Customer) {
        customer = (Customer) currentCustomer;
      } else {
        customer = new Customer();
      }

      customer = CustomerUpdate.update(customer, "WorkHood", "Worker");

      // a JAVA serialization will be done behind the scene
      variables.put(ComplexVariableConstant.PROCESS_VARIABLE_CUSTOMER, customer);
      return "Direct Worker OK";
    } catch (Exception e) {
      logger.info("WorkerVariables.executeDirect Exception [" + e + "]");
      return "Direct Worker failed [" + e + "]";

    }
  }
}
