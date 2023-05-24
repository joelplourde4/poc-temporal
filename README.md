# Temporal Demo

The following demo is supported by the following documentation:

[Getting Started with Temporal](https://cloudops.atlassian.net/wiki/spaces/gokart/pages/2793439247/Getting+Started+with+Temporal)

[Automation Engine: Dynamic Workflow](https://cloudops.atlassian.net/wiki/spaces/gokart/pages/2794389519/Automation+Engine+Dynamic+Workflow)

## Getting Started

1. Install [Temporalite](https://cloudops.atlassian.net/wiki/spaces/gokart/pages/2793439247/Getting+Started+with+Temporal#Install)
2. Run the Temporalite in a command line interface
   1. ``` ./temporalite.exe start --namespace default --ephemeral```
3. Browse to the user interface: http://localhost:8233/namespaces/default/workflows

## Demo
4. Uncomment the @PostConstruct annotation in the WorkflowHandler
5. Uncomment the first scenario
6. Run the com.cloudops.engine.EngineApplication
7. Enjoy!
8. Repeat the 4-5 steps for each scenario and then the DynamicWorkflowHandler

