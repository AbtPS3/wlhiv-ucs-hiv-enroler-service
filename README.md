# WLHIV-UCS-HIV-ENROLLMENT-SERVICE

A service used to register and enroll Women Living With HIV as UCS HIV Clients.

## 1. Dev Requirements

1. Java 11
2. IntelliJ or Visual Studio Code
3. Gradle

## 2. Deployment

To build and run the service after performing the above configurations, run the following

```
  ./gradlew clean shadowJar
  java -jar build/libs/wlhiv-ucs-hiv-enrollment-service-<version>.jar
```


## 3. Deployment via Docker

First Install docker in your PC by following [this guide](https://docs.docker.com/engine/install/). Secondly, clone this repo to your computer by using git clone and the repo's address:

`git clone https://github.com/AbtPS3/wlhiv-ucs-hiv-enroler-service.git`

Once you have completed cloning the repo, go inside the repo in your computer: `cd wlhiv-ucs-hiv-enroler-service` 

Update `mediator.properties` found in `src/main/resources/` with the correct configs and use the following Docker commands for various uses:

### Run/start
`docker build -t ucs-import-service .`

`run -d -p 127.0.0.1:9001:9001 ucs-import-service`


### Interact With Shell

`docker exec -it ucs-import-service sh`

### Stop Services

`docker stop ucs-import-service`

## License

ISC

## Author

MOH, USAID PS3+, Abt Associates

## Version

1.0.0