# Voyager app for Jetbrains Space ![My Image](static/voyager.svg)

## About

The Voyager app grabs your organization's data and lets you discover it. The organization's data is a graph, where
vertices like people and calendar events are connected via edges. Graph analysis could help to understand how the
business operates and what are strengths and weaknesses. This could help make business more effective, for example, too
many connections going through one person could be a sign that there are some communication issues, and losing this
person can be very expensive.

You could find more information by searching for "organizational network analysis"
, [for example](https://www2.deloitte.com/us/en/pages/human-capital/articles/organizational-network-analysis.html).

## Jetbrains Space

A very good thing about the platform is the fact that all data can be accessed through one API and data is very well
connected, this makes it possible to have full data at a relatively low cost.

## Technical details

### Technology stack

* Backend - Kotlin (Ktor).
* Frontend - React.
* Storage - neo4j (community edition).

The code has not yet been published, I'm thinking to publish it later.

### [Neo4j](https://neo4j.com/)

Neo4j is a graph database and so suits the goal pretty well. The main advantage of the graph database is the ability
to effectively run [complex graph queries](https://neo4j.com/developer/cypher/)
and [graph algorithms](https://neo4j.com/developer/graph-data-science/graph-algorithms/), which is mission critical in
organizational network analysis.

## Future work

* Data ingestion
    * Add more node/edge types.
    * Grab data with offset.
* Data discovery
    * Ability to run queries/graph algorithms.
        * There are technical challenges to making this secure because of multi-tenancy.
    * Add some default analysis that works out of the box.
* Permissions
    * Manage permissions better, ideally employees should be able to see just their own scope of data.
* Data visualization
    * Better styles for graph rendering.
