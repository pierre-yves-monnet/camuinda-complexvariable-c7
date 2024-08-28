package org.camunda.worker;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SimpleWorker {
  private final Logger logger = LoggerFactory.getLogger(SimpleWorker.class.getName());

  public static void main(String[] args) {
    SimpleWorker simpleWorker = new SimpleWorker();
    simpleWorker.registerWorker();
  }

  // Attention, the topic "work-to-do" must be handled at a time by only one bean.
  // So, when you replace the value here, check that all other Bean does not get it.
  public void registerWorker() {
    String baseUrl = "http://localhost:8080/engine-rest";
    ExternalTaskClient client = ExternalTaskClient.create()
        .baseUrl(baseUrl)
        .workerId("simpleWorker-22")
        .maxTasks(10)
        .lockDuration(4000)
        .asyncResponseTimeout(20000)
        .backoffStrategy( new ExponentialBackoffStrategy())
        .build();

    client.subscribe("simple-worker").lockDuration(10000).handler((externalTask, externalTaskService) -> {
      this.handleWorkerVariable(externalTask, externalTaskService);
    }).open();
logger.info("Start subscription to [simple-worker]");
    /**
     * create a process instance via the modeler
     {
          "customer": "{\"id\": \"665\",\"lastName\": \"Paul\",\"address\": \"389 meditteraneen av\",*\"company\": {\"name\": \"Camunda\",\"country\": \"germany\"}}"
     }

     {
     "customer": "{\"id\": \"665\"}"
     }
     {
     "customer": {
     "id": "665",
     "lastName": "Paul",
     "address": "389 meditteraneen av",
     "company": {
     "name": "Camunda",
     "country": "germany"
     }
     }
     }
          */




  }

  public void handleWorkerVariable(org.camunda.bpm.client.task.ExternalTask externalTask,
                                   ExternalTaskService externalTaskService) {
    logger.info("SimpleWorker.handleWorkerVariable : >>>>>>>>>>> start [" + externalTask.getId() + "]");
    Map<String, Object> variables = new HashMap<>();
    try {

      String id = externalTask.getVariable("id");
      logger.info("Variable ID [{}]", id);
      variables.put("userName", "Jordan-"+id);
      variables.put("lastName", "Hollande-"+id);
      variables.put("risk", "blue");

      externalTaskService.complete(externalTask, variables);


    } catch (Exception e) {
      logger.info("SimpleWorker.handleWorkerVariable Exception [" + e + "]");

    }
    logger.info("SimpleWorker.handleWorkerVariable : >>>>>>>>>>>> end [" + externalTask.getId() + "]");

  }
}
