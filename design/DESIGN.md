# Designing Backend like Google Analytics

This is an agnostic solution. I talk about the logic of the system and how it will be constructed. I think that all concepts in here are true whatever tools we will take.

The concrete proposition is at the end of this document.

## System volumetry

- Billions of requests from clients per day.
- Millions of merchants will ask analytics results.

## Solution requirements
- We need to provide results before 1 hour for each merchant.
- We are looking for minimal downtime.
- We need to be able to track a request and reproduce it easily.

## Client API

I'm assuming we providing a backend and every merchant got his own frontend on his side.

Since we could have a huge variety of websites and apps from Android to iOS and so on, we need to expose a simple REST API since every language and framework will always provide a REST HTTP API. We'll need to provide good documentation and prepare to assume support if clients don't understand the API.

To limit this problem, we can provide libraries in a wide range of languages.

|Pros | Cons |
|-----|------|
|No risk of bad analytics since clients have less chance to mess up the integration.| Will cost a lot to our company to create and maintain each library.| We could provide those libraries under open-source licenses to help support more languages without too many costs.

## Database capacity

| | Billions | Result|
|----- |------|-------|
| Low | 1 | `1024 * 1 000 000 000 = 1 024 000 000 000 bytes`
| Medium | 3 | `4096 * 3 000 000 000 = 12 288 000 000 000 bytes`
| High | 6 | `8192 * 6 000 000 000 = 49 152 000 000 000 bytes`
- Max request size was arbitrarily determined.
- [Max billions were determined by Google searches by day.](https://searchengineland.com/google-now-handles-2-999-trillion-searches-per-year-250247)
It's probably one of the most used services in the World, so it's a good reference.

1.024 terabyte to more than 49.15 terabytes.

## Database

- A raw database with all data inside.
- A middleware to analyze data and put them into specials analytic database.
- There is no need to query multiples merchants at the same time.
    - Better query performance.
    - Better security, no data can mix up together: __One merchant should not see other merchants data.__
- We need to remove data periodically.
- If we don't, amounts of data will be unbearable.
- It will limit analytics results possibilities.

## Back API to get queries from clients

We need to use Microservices technology for scalability of traffic. API Gateway to route traffic.

## Concept
- Control coherence of data.
- Put every data into a database for a specific merchant.

### Structure
To get the best precision of our storage cost, we need to:
- API will provide a final list of what we can track or not, no free area.
- Let merchants send sublist of what can be tracked.
- We need to put a quota on each IP Address and merchants to avoid DDOS and jamming.
    - [Limits from Google Analytics](https://developers.google.com/analytics/devguides/config/mgmt/v3/limits-quotas)

### Traceability
Every call of the API will get a special tracking id. This id will be the same in every step of his life. It's like a tracking number. It will be useful to help reproduce a call.

This tracking id will be generated from `IP + merchant id + timestamp` and send in HTTP request in a special header. We need to use that ID from API Gateway to the end. __The load balancer can generate it.__

## Backend to get queries from merchants

Microservices technology for scalability of traffic. API Gateway to route traffic.

### Considered solution
This backend provides a graphic interface where merchants can display results and filter them.

### Needs
This service needs to provide to each merchants data before 1 hour.


# And now, what are we using?

This proposition is based on feedbacks read online. That kind of task should be teamwork to exchange ideas and to confront thoughts.

## Concrete proposition

![](design.png)

| Type | Solution | Why|
|----- |------|-------|
| Load balancer | HAProxy| Open-source, popular and often used in many high-profile environments like GitHub, Imgur, Instagram, and Twitter. __Can generate a unique tracking id.__
| Gateway to API | Zuul | Developed by Netflix, the last release from a few weeks ago. Largely used.
| Pairing between Gateway and API | Netflix OSS Eureka | Eureka server acts as a registry and allows all clients to register themselves and used for Service Discovery to be able to find an IP address and port of other services if they want to talk to. Pairing very well with Zuul and Sprint Boot.
| API for clients| Spring Boot | Spring Boot is very famous and regularly updated. Works well with Eureka. Using the JVM ecosystem is a mature environment and provides a huge range of libraries.
| Tracking System | Elasticsearch and Fluentd and Kibana | HAProxy, Zuul and Spring Boot will write logs with the unique tracking id. Fluentd will parse them and expose it to Elasticsearch. We will be able to reproduce requests with this. We can use Kibana to read easily results.
| Backend for merchants | Grafana | Wide ranges of graphics, Good pairing with Elasticsearch but can be used with others inputs too. Designed for Graphics and analysis. We can manage users on it and forbidden other merchants data.
| Database for raw data | Apache Cassandra | Fault-tolerant, Free, Scalable, Distributed Processing. Can manage petabytes of data.
| Analytics provider | Elassandra (Elasticsearch fork) | We can easily extract data we need to analyze from Cassandra with it. Grafana can read it easily. We can use an index per merchant to improve performance and security.

Sources (some are in French) :
- [Google Analytics (GA) like Backend System Architecture](https://medium.com/@abhilashkrishnan_64923/google-analytics-ga-like-backend-system-architecture-7a7826d56af7)
- [(fr) Microservices for big e-commerce website](https://blog.ippon.fr/2016/10/20/industrialisation-dune-architecture-de-microservices/)
- [An Example of Load Balancing with Zuul and Eureka](https://www.baeldung.com/zuul-load-balancing)
- [Migrating Messenger storage to optimize performance](https://engineering.fb.com/core-data/migrating-messenger-storage-to-optimize-performance/)
- [Cassandra Vs MongoDB In 2018](https://blog.panoply.io/cassandra-vs-mongodb)
- [Cassandra vs. HBase](https://www.scnsoft.com/blog/cassandra-vs-hbase)
- [Apple has the biggest Cassandra instance](https://www.datastax.com/2019/03/apache-cassandra-five-interesting-facts)
- [Elasticsearch for Hadoop](https://www.elastic.co/what-is/elasticsearch-hadoop)
- [Complementing Cassandra with Elasticsearch](https://blog.devartis.com/complementing-cassandra-with-elasticsearch-121c70ef7f4)
- [Elassandra : Elasticsearch+Cassandra framework](https://www.elassandra.io/)
- [HAProxy can add headers](https://cbonte.github.io/haproxy-dconv/1.7/configuration.html#7.3.1)
- [Monitoring HAProxy Real-time with Elasticsearch and Fluentd](https://www.fluentd.org/guides/recipes/haproxy-elasticsearch)
- [Fluentd vs. LogStash: A Feature Comparison](https://www.loomsystems.com/blog/single-post/2017/01/30/a-comparison-of-fluentd-vs-logstash-log-collector)
- [Zuul Filters, log customization](https://github.com/Netflix/zuul/wiki/Filters)
- [Elasticsearch cluster](https://logz.io/blog/elasticsearch-cluster-tutorial/)
- [Scaling Elasticsearch](https://medium.com/hipages-engineering/scaling-elasticsearch-b63fa400ee9e)
- [Grafana vs Kibana](https://stackshare.io/stackups/grafana-vs-kibana)
- [Grafana vs. Kibana: The Key Differences to Know](https://logz.io/blog/grafana-vs-kibana/)
