package com.javamain.akka.cluster;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Router;

import java.util.ArrayList;

public class LeaderSubscriber02 extends AbstractActor {
    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    // 这里有个问题，是定死的
    private Router router = new Router(new RoundRobinRoutingLogic(), new ArrayList<>());
//    private ActorRef routerActorRef;
    private final Cluster cluster = Cluster.get(getContext().getSystem());
    private static final String SLAVE_PATH = "/user/workerActor";
    boolean isReady = false;

    @Override
    public void preStart() {
        // 订阅集群事件，以便及时获知领导者变更
        cluster.subscribe(getSelf(),
                ClusterEvent.LeaderChanged.class,
                ClusterEvent.ClusterDomainEvent.class,
                ClusterEvent.ReachabilityEvent.class,
                ClusterEvent.MemberEvent.class
        );
    }

    @Override
    public void postStop() {
        // 取消订阅集群事件
        cluster.unsubscribe(getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClusterEvent.LeaderChanged.class, leaderChanged -> {
                    System.out.println("Leader changed to: " + leaderChanged.getLeader());
                    //eventBus.tell(new EventMessage("New leader is " + leaderChanged.leader()), self());
                    //handleLeaderChanged(leaderChanged);
                })
//                .match(EventMessage.class, eventMessage -> {
//                    // 处理事件消息
//                    System.out.println("Received event: " + eventMessage.getContent());
//                })
                .match(ClusterEvent.MemberUp.class, mUp -> {
                    if (mUp.member().hasRole("worker")) {
                        Address address = mUp.member().address();
                        String path = address + SLAVE_PATH;
                        ActorSelection selection = getContext().actorSelection(path);
                        router = router.addRoutee(selection);
                        isReady = true;
                        log.info("New routee is added!");
                    }
                })
                .match(ClusterEvent.MemberRemoved.class, mRemoved -> {
                    router = router.removeRoutee(getContext().actorSelection(mRemoved.member().address() + SLAVE_PATH));
                    log.info("Routee is removed");
                })
                .match(ClusterEvent.UnreachableMember.class, mRemoved -> {
                    router = router.removeRoutee(getContext().actorSelection(mRemoved.member().address() + SLAVE_PATH));
                    log.info("Routee is removed");
                })
                .match(String.class, msg->{
                    log.info("Master got: {}", msg);
                    if(!isReady)
                        log.warning("Is not ready yet!");
                    else {
                        log.info("Routee size: {}", router.routees().length());
                        router.route(msg, getSender());
                    }
                })
                .match(String.class, word -> {
                    log.info("Node {} receives: {}", getSelf().path().toSerializationFormat(), word);
                    //routerActorRef.tell(word,getSender());
                    router.route(word,getSender());
                })
                .build();
    }


//    private void handleLeaderChanged(ClusterEvent.LeaderChanged leaderChanged) {
//        if (leaderChanged.getLeader().equals(cluster.selfAddress())) {
//            int totalInstances = 100;
//            int maxInstancesPerNode = 10;
//            boolean allowLocalRoutees = false;
//            String useRole = "worker";
//
//            Props workerProps = Props.create(WorkerActor.class); // 假设 WorkerActor 是您的工作 Actor
//            ClusterRouterPoolSettings settings = new ClusterRouterPoolSettings(
//                    totalInstances, maxInstancesPerNode, allowLocalRoutees, useRole);
//            ClusterRouterPool pool = new ClusterRouterPool(new RoundRobinPool(5), settings);
//
//            routerActorRef = getContext().actorOf(pool.props(workerProps), "clusterRouter");
//            System.out.println("Created a new ClusterRouterPool as a leader.");
//        }
//    }

}
