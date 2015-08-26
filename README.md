# Content-based-Pub-Sub-model

Content - based Pub/Sub model.


Original Contribution:

We have not used any standard libraries or frameworks for our project. The concept of Java RMI was used for communication between all the entities ( i.e. Publisher, Subscriber, Event Service and Gateway) of our project.

The lazy update model was used to update the data of subscribers at the event service. The updates were sent to Event service only when it was needed to avoid event service network flooding. Piggyback concept is used wherein the gateway acts as a loadbalancer i.e. it selects a specific event service to do its task and also sends the updated information about the subscribers to that event service.
