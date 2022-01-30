# Twitterino

This project is an exercise from Udemy Course "[Learn Apache Kafka for Beginners v2](https://www.udemy.com/course/apache-kafka/)"



This project contains

- A `TwitterClient` that receives a stream of tweets from subscribed Twitter-channels (such as "cnn") and twitter-handles, such as "#omikron", and hands these tweets off to...
- ...a `KafkaPublisher`, that pushes these tweets onto a Kafka-topic, "tweets"
- A `TweetConsumer`, that consumes tweets off of topic `tweets` and pushes these into an ElasticSearch index named `twitterdata`
- docker-compose file for "up'ing" a Kibana + Elasticsearch stack
- docker-compose file for "up'ing" a Kafka stack





## Run ElasticSearch and Kibana stack

From `docker-compose` folder, run

```
docker-compose -f elastic.yaml up -d
```

Now run ...

```
docker ps
```

...to check that two containers are now running

You should also be able to navigate to http://localhost:5601/ to get the Kibana frontend. 



## Run Kafka (and zookeeper)

From `docker-compose` folder, run

```
docker-compose -f kafka.yaml up -d
```

Now run ...

```
docker ps
```

...to check that a `zookeeper` and a `kafka` container are now running



### One-time setup of `Tweets` kafka-topic (commandline)

This assumes you have the `kafka-topics` command-line utility installed. Check [this quick-start guide](https://kafka.apache.org/quickstart) to get set up.

Assuming `kafka` and `zookeeper` are already running, run

```
kafka-topics.sh --bootstrap-server 127.0.0.1:29092 --create --topic tweets --partitions 1 --replication-factor 1
```



### One-time setup of `Tweets` kafka-topic (GUI)

Look into [Conduktor](https://www.conduktor.io/) (requires Java 11), and create a topic named `tweets` with a replication-factor of 1 and with a single partition. 