# Order Matching Transport Planning

The project computes transport options for parcel deliveries. Given a start and destination, possible logistics service
provider
or crowdworker to transport the parcel, proposals for the parcel transport are computed. A proposal could split the
transport
into several steps assigning a carrier, which could be either a crowdworker or a logistics service provide, to each
step.

## Getting started

To build the project: `./gradlew build`

To publish the built jar to the local maven repository `./gradlew publishToMavenLocal`

## Description of the Model

### Input

- Order with start and end geolocation
- transfer points with opening times and owner (potentially neutral owner)
- logistic service provider with delivery region
- crowdworker routes with timeslot and weekday

### Output

- transport proposal consisting of several order steps
    - each order step starts and end at a transfer point
    - the first step starts at a transfer point near the start location
    - the last step ends at the recipient location or a transfer point nearby

### Constraints

1. consideration of opening times when planning the delivery or pickup at a transfer point
2. crowdworkers can only transport within their given route timeslots
3. in case that a transfer point, owned by a logistics service provider, is used this lsp has to be involved either in
   the transport to or from the transfer point

## Algorithm

The algorithm is based on the classic shortest path algorithm of Dijkstra. The graph we compute a shortest path on
consists of the transfer points as vertices and possible transports by logistics service providers and crowdworkers as
edges. However, the edges are not explicitly represented, as the graph could come near to a complete graph and
additionally
could have multi edges. This could lead to high memory consumption. Besides that the graph could be very time-dependent.

### Initialization

1. Transfer points with an lsp owner are split into two nodes: An IN-node and an OUT-node. Transports to an IN-node can
   only be made by the lsp owning the node and vice versa are transports from OUT-nodes only allowed for the owner of
   the node. As it is to allow two consecutive transports made by the same carrier, the transport from IN-nodes is never
   done by the owner. The same holds for the transport to OUT-nodes.
2. The few nearest transfer points to the start position are initialized with the "distance" 0. Possible distance
   metrics
   are time, price or emissions. All other nodes are initialized with infinite distance.
3. All nodes are added to a priority queue.

### Processing

Like in Dijkstras shortest path algorithm the first node of the priority queue is taken and processed until the
destination is reached. The distance of all reachable nodes is then updated. How a node is processed in detail depends
on the type of node.

#### IN-node

IN-nodes are owned by a logistics service provider. Only this lsp is allowed to transport packages to this node.
With an explicit edge representation all edges ending at an IN-node would be edges of the lsp owning the node.
When processing an IN-node, the owning lsp is not considered.

For all other lsps which have this node in their delivery region we need to relax the implicit edges to other nodes.
We do that by updating all IN-nodes (of the lsp) and NEUTRAL nodes in the delivery region of the lsp.
Crowdworkers can also transport from IN-nodes. If a crowdworker route runs near this node, we update
all OUT and NEUTRAL nodes that lie near that route further on the way.

#### OUT-node

OUT-nodes are the opposite of IN-nodes. All transports leaving an OUT-node have to be done by the owning lsp.
Ingoing transports must not be done by the owning lsp.

When relaxing implicit edges starting at this node we update all IN-nodes of the owning lsp and NEUTRAL-nodes
in the delivery region

#### NEUTRAL-node

For NEUTRAL-nodes in and outgoing transports can be done by all lsps and crowdworkers and the processing
works like for OUT-nodes without an owning lsp.

#### Updating nodes

Updating nodes works as in the classic shortest path algorithm of Dijkstra with a few constraints:

1. Opening times: If an updating time is not during the opening times of the node to be updated, the next
   open hour is taken as updating time.
2. For a crowdworker transport the updating time is always the end time of the corresponding route timeslot

### End

A node is not explored further either when it is the recipient node or a node that is very close to the recipient (self
pickup)

## License

This project is licensed under the terms of the MIT license.

