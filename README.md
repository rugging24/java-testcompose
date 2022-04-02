# Testcompose
A clean way to run your containerised tests

## Installation

### Maven
```
<dependency>
  <groupId>de.theitshop</groupId>
  <artifactId>testcompose</artifactId>
  <version>${version}</version>
</dependency>
```

### Gradle
```
implementation 'de.theitshop:testcompose:${version}'
```
## Configuration
`testcompose` accepts a `yaml` config file ideally placed in the resource folder and named `testcompose-bootstrap` witih the 
appropriate `yaml` or `yml` extension. One can also use a differently named config file, and provide it to the constructor call 
of the `RunContainers` class.

```
# With the testcompose-bootstrap.yaml file 

RunContainers runContainers = new RunContainers();

# With a differently named file

String configFileName = "some-config-file-name.yml";
RunContainers runContainers = new RunContainers(configFileName);
```

The config could contain any number of services with the opening tag `services`. E.g

```
services:
  - name: database
    image: "postgres:13"
    command: ""
    environment:
      POSTGRES_USER: postgres
      POSTGRES_DB: postgres_db
      POSTGRES_PASSWORD: password
    exposed_ports:
      - 5432
    log_wait_parameters:
      log_line_regex: ".*database system is ready to accept connections.*"
      log_line_regex_occurrence: 1
  - name: application
    image: "python:3.9"
    command: "/bin/bash -x /run_app.sh"
    environment:
      DB_URL: "${{database.postgres_user}}:${{database.postgres_password}}@${{database.container_hostname}}:5432/${{database.postgres_db}}"
      KAFKA_BOOTSTRAP_SERVERS: "${{kafka.container_hostname}}:9092"
      KAFKA_OFFSET_RESET: "earliest"
      KAFKA_TOPIC: "test_kafka_topic"
    exposed_ports:
      - 8000
    volumes:
      - host: "docker-test-files/run_app.sh"
        container: "/run_app.sh"
        mode: "ro"
        source: "resources"
      - host: "docker-test-files/app.py"
        container: "/app.py"
        mode: "ro"
        source: "resources"
    log_wait_parameters:
      log_line_regex: ".*Application startup complete.*"
      log_line_regex_occurrence: 1
    http_wait_parameters:
      http_port: "8000"
      response_status_code: 200
      end_point: "/ping"
    depends_on:
      - database
      - kafka
```

### Allowed Service Config Parameters
- `name`: the name of the service. This is alos the `hostname` of the service
within the current test network.
- `image`: The docker image full url as specified in its registry - docker hub or private registry. 
Note that when using a private docker registry, TestContainer expects you to already login into the 
registry before attempting to start the container as it does not ask the user to provide registry
login details.
- `command`: This sets the command that should be run in the container on Container startup. To run a command
after a container is verified to be running, use the `exec_command_after_container_startup` option instead.
- `environment`: A set of environment variables for the container. A special usage of the environment variables
in the config file is to be able to reference the properties of other
services in the environment variables of other services. As an example,
the `application` service above references properties from the `database` service
specified in the `DB_URL` environment variable. i.e.
```
DB_URL: "${{database.postgres_user}}:${{database.postgres_password}}@${{database.container_hostname}}:5432/${{database.postgres_db}}"

The placeholder is off two sections => serviceName.environmentVariable
i.e. ${{database.postgres_user}}
Means:
The environment variable `postgres_user` could be optained from the service `database`
```

- `exposed_ports`: Ports to be exposed to the host. Note that the exposed ports to 
be specified are the container ports to be exposed to the host and not the host ports.
TestContainer maps random host ports to the exposed container ports.
- `volumes`: A list of files/directories to be mounted in the container. A typical 
representation is as below:
```
volumes:
  - host: "some-directory-name/some-file.txt"
    container: "/some-file.txt"
    mode: "ro"
    source: "resources"

Where:
    host -> holds the path to the file/directory on the host system 
    container -> path in the container the file/directory should be mounted
    mode: access mode of the file/dorectory 
    source: hint on where the mounted files/direcotries could be found
```
The source can be either of `resources` or `filesystem`. For volume sources specified as
`resources`, the full path of the host file need not be specified. The file is expected to be found in the 
application resource directory. A `FileNotFoundException` is raised if specified files 
are not found. Volume sources specified as `filesystem` requires that the full path of 
the host file/directory need to be provided.

The volume access mode could be either of `ro` or `rw`.

- `log_wait_parameters`: Allows users to specify regexes that could be found in the container log 
during startup. The container will be exited if the provided log line regex is not found in the 
container log. The `log_line_regex_occurrence` specifies the number of times the provided regex should 
appear in the container log.
- `http_wait_parameters`: Similar to the `log_wait_parameters` but allows the users to specify and http
endpoint rather than checking container logs for regex entries. This allows the users to inspect
the response of specified http endpoints to determine if containers were properly started or not.
- `depends_on`: Specifies a list of services that must be started before starting the current
service.
- `exec_command_after_container_startup`: Sometimes it might be necessary to run a set of commands 
having ensured the container had been correctly started. This entry allows users to run command after
a successful startup of containers. 
E.g.
```
exec_command_after_container_startup:
  - name: change_kafka_advertised_listener_config
    command: >-
      kafka-configs --alter
      --bootstrap-server=PLAINTEXT://${{self.container_host_alias}}:9092
      --entity-type=brokers
      --entity-name=${{self.KAFKA_BROKER_ID}}
      --add-config advertised.listeners=["BROKER://${{self.container_host_alias}}:9092","PLAINTEXT://${{self.container_host_address}}:${{self.external_port_9093}}"]

```
This accepts a list of commands identified by a unique name and command. It's worthy of note that the commands
are passed to the docker container as an array split by spaces. If a command contains words that should not be split,
such words should be quoted either by single or double quotes.
Placeholder variables could also be used in `exec_command_after_container_startup` as shown above.

- `test_containers_module`: This is a special entry in the config that allows the usage of a ready-made 
TestContainer modules. The Philosophy of the `testcompose` library is to allow users bring their own 
containers and allow for very low level tuning of their integration test services using mainly the 
`GenericContainer` implementation of the `TestContainers` library. Sometimes it might be a lot easier 
to leverage the work already done in the `TestContainers` library, hence the need for this in the config. 
E.g.
```
test_containers_module:
  module_name: KafkaContainer
  module_parameters:
    zookeeper:
      external: true
      connection_string: "${{zookeeper.container_hostname}}:${{zookeeper.zookeeper_client_port}}"
```

At the moment, only the Kafka module of the `TestContainers` is supported in this section. Also; note that
placeholder variables could be used like in `environment` in the entries.

### Placeholder variables:

The following placeholder variables could be used when specifying any given service in the config file.
- `self` : refers to the current service name.
- `container_hostname`: refers to the hostname of the target container. e.g ${{zookeeper.container_hostname}}
  would yield the zookeeper service/application container hostname.
- `external_port`: This refers to the port exposed to the host of the container. TestContainers usually exposes random ports
  to the host. i.e. if for a postgres service the port 5432 is to be exposed to the host, TestContainers will use a completely
  random port on the host. This placeholder variable will allow the user make use of this port without knowing it upfront.
  For the described example of the postgres service exposing the port 5432 this will be specified in the config file as
  `external_port_5432`
- `container_host_alias`: This is used to obtain a container network alias as derived from `container.getNetworkAliases().get(0)`
  using the TestContainer API.
- `container_host_address`: This provides the external host address of the container as seen from the host the container
  runs on. This is obtained from `container.getHost() `. To obtain the appropriate host of a service, it is advisable
  to use the result of this in together with the result obtained from the `external_port` placeholder variable to construct the
  full container external url.
