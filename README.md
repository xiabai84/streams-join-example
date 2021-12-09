# Project 
Example code for Kafka Streams Joins operation.

## Stream-Stream Joins 
There are multiple kinds of joins. For example stream-table, stream-global-table, table-table, stream-stream joins.

This project only focus on following stream-stream-joins: 
* inner join
* inner join with deduplication
* left join

## Quick Start
Make sure you have already a running Kafka Broker, which listen on port 9092.

Create two topics for storing left and right stream data:

    $ kafka-topics --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 1 --config retention.ms=-1 --config cleanup.policy=compact --topic topic-right
    $ kafka-topics --bootstrap-server localhost:9092 --create --replication-factor 1 --partitions 1 --config retention.ms=-1 --config cleanup.policy=compact --topic topic-left

Start stream application:

    $ ./gradlew run

Produce some data for test:

    $ kafka-console-producer --broker-list localhost:9092 --property "parse.key=true" --property "key.separator=:" --topic topic-right
    > k1: left-value-1
    > k1: left-value-1-new
    > k2: left-value-2
    
    $ kafka-console-producer --broker-list localhost:9092 --property "parse.key=true" --property "key.separator=:" --topic topic-left
    > k1: right-value-1
    > k2: right-value-2
