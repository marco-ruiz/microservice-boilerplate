# Microservice Boilerplate [![Build Status](https://travis-ci.org/marco-ruiz/microservice-boilerplate.svg?branch=master)](https://travis-ci.org/marco-ruiz/microservice-boilerplate)

This is a Spring Boot based project; which wires up several concerns common when building microservices.

## Source Code
- Project structure broken down by functional layers: application -> web -> service -> repository.
- Integration of all layers.

#### Application
- Application main setup.
- Web configuration with CORS enabled and the console servlet mapped.
- Standard application properties setup.
- Configuration bean integration to support custom properties across the application.
- Application listener integration to be optionally enabled through configuration for testing purposes. 

#### Web
- REST Controller boilerplate with all CRUD operations.
- DTOs integration as inputs to REST controller.
- Resource integration as outputs of REST controller.
- Resource Assembler integration as factory for outputs of REST controller.
- HATEOAS support within Resource Assembler.
- Common utilities to generate links to resources (to be used with HATEOAS).
- Authentication placeholders across controller and service layer.

#### Service
- Service skeleton with commonly used Spring configuration.

#### Repository
- Repository boilerplate with support for querying an abstract model by custom filters.
- JPA 2.0 metamodel integration for usage with the Criteria API.
- Sample entity model with minimum necessary JPA annotations to be used with the repository boilerplate. 
- Abstract Lucene custom repository to support text search features. 

## DevOps

- Gradle based build.
- Configuration to automatically generate JPA metamodels during the build.
- Ascii doctor configuration to generate REST API documentation using Spring REST Docs.
- Configuration of all other dependencies needed by this boilerplate project.
- Test boilerplate for controller integrated with Spring REST Docs snippets and templates. 
- Travis configuration for CI/CD support.
