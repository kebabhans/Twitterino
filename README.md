# Twitterino

This project is an exercise from Udemy Course "[Learn Apache Kafka for Beginners v2](https://www.udemy.com/course/apache-kafka/)"



This project contains

- [TODO] A Twitter-client that receives a stream of tweets from subscribed Twitter-channels (such as "cnn") and twitter-handles, such as "#omikron", and hands these tweets off to...
- [TODO] ...a TweetProducer, that pushes these tweets onto a Kafka-topic, "tweets"
- [TODO] A TweetConsumer, that consumes tweets off of topic "tweets" and pushes these into ElasticSearch
- [TODO] docker-compose file for "up'ing" a Kibana+Elasticsearch stack
- [TODO] docker-compose file for "up'ing" a Kafka stack





## Run ElasticSearch and Kibana stack

From `docker-compose` folder, run

```
docker-compose -f docker-compose.yaml up -d
```

Now run ...

```
docker ps
```

...to check that two containers are now running

You should also be able to navigate to http://localhost:5601/ to get the Kibana frontend. 



