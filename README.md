# camuinda-complexvariable-c7

Demonstrate the different possiblities to handle a complex variable in Camunda 7
Data are defined under org.camunda.complexvariable.c7.data, class Customer
Process (ComplexVariables) defined a simple process which call a JavaDelegate, a Worker, a JavaDelegate and a Worker.

Each JavaDelegate and Worker manipulate 3 types of variables:
* a Customer variable, saved in JSON. All operations are corrects
* a Customer variable, saved in JAVA SERIALIZATION. The save is NOT POSSIBLE from the worker (Exception occure)
* A Customer variable, direct access. All operations are corrects.


# JavaDelegate
org.camunda.complexvariable.c7.delegate.DelegateVariables
is a Delegate component. This component manipulate 3 differents Customer variables, with 3 differents ways:
* Java
* JSON
* Direct access (Serialization behind the scene)

To start the Delegate, the Camunda Engine is under org.camunda.complexvariable.c7.engine. 
Start the static main under CamundaEngine

# External Worker
org.camunda.complexvariable.c7.workerVariable
Start the main in the worker.
