# CTC3-Mediator-DDR


[![Codacy Badge](https://api.codacy.com/project/badge/Grade/eb9fc4b8ee9849d5bd7c6a76bdb33995)](https://app.codacy.com/gh/SoftmedTanzania/ctc3-mediator-ddr?utm_source=github.com&utm_medium=referral&utm_content=SoftmedTanzania/ctc3-mediator-ddr&utm_campaign=Badge_Grade_Settings)
[![Java CI Badge](https://github.com/SoftmedTanzania/ctc3-mediator-ddr/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/SoftmedTanzania/ctc3-mediator-ddr/actions?query=workflow%3A%22Java+CI+with+Maven%22)
[![Coverage Status](https://coveralls.io/repos/github/SoftmedTanzania/ctc3-mediator-ddr/badge.svg?branch=development)](https://coveralls.io/github/SoftmedTanzania/ctc3-mediator-ddr?branch=development)

An [OpenHIM](http://openhim.org/) mediator for handling system integrations between DDR and CTC3.

## 1. Dev Requirements

1. Java 1.8
2. IntelliJ or Visual Studio Code
3. Maven 3.6.3

## 2. Mediator Configuration

This mediator handles capturing of filterable fields for integration between DDR and CTC3 .

## 3. Deployment

To build and run the mediator after performing the above configurations, run the following

```
  mvn clean package -DskipTests=true -e source:jar javadoc:jar
  java -jar target/ctc3-mediator-ddr-<version>-jar-with-dependencies.jar
```
