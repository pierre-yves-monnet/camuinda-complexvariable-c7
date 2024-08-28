package org.camunda.complexvariable.c7.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.camunda.spin.json.SpinJsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;

import static org.camunda.spin.Spin.JSON;

@Component
public class CamundaPostConstruct implements ApplicationRunner {

  private final Logger logger = LoggerFactory.getLogger(CamundaPostConstruct.class.getName());

  @Override
  public void run(ApplicationArguments args) throws Exception {
    try {
      discoverEngine();

      ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
      RuntimeService runtimeService = processEngine.getRuntimeService();

      // Create a Map of values.
      Map<String, Object> customerMap = new HashMap<>();
      customerMap.put("id", "665");
      customerMap.put("lastName", "Paul");
      customerMap.put("address", "389 mediteraneen av");
      // Sub map are allwed in Json
      customerMap.put("company", Map.of("name", "Camunda", "country", "germany"));
      JacksonJsonNode jsonValueCustomer = (JacksonJsonNode) JSON(customerMap);

      // create the process instance now
      runtimeService.createProcessInstanceByKey("ProcessSimpleWorker")
          .setVariable("customer", jsonValueCustomer)
          .execute();

    } catch (Exception e) {
      logger.error("Can't create process instance in process [ProcessSimpleWorker]: {} ", e);
    }
    // https://docs.camunda.org/manual/7.21/user-guide/data-formats/json/
    logger.info(">>>>>>>>>>>>>>>>> Process instance created with success in process [ProcessSimpleWorker]");
  }

  private void registerGroovy() {
    discoverEngine();
    GroovyScriptEngineFactory factoryGroovy = new GroovyScriptEngineFactory();
    ScriptEngine engine = factoryGroovy.getScriptEngine();

    ScriptEngineManager manager = new ScriptEngineManager();
    manager.registerEngineName("groovy", factoryGroovy);
    discoverEngine();
  }
  private void discoverEngine() {
    ScriptEngineManager manager = new ScriptEngineManager();
    List<ScriptEngineFactory> factories = manager.getEngineFactories();
  logger.info("Discover Script engine: {}", factories.size());
    for (ScriptEngineFactory factory : factories) {
      logger.info("  Engine Name:{} Engine Version:{} Language Name:{}", factory.getEngineName(),
          factory.getEngineVersion(), factory.getLanguageName());
    }
  }
}
