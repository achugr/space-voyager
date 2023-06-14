package com.achugr.voyager.entity

import org.neo4j.ogm.annotation.*

interface ClientAware {
    val clientId: String
}

interface Vertex : ClientAware {
    val id: String
    val label: String
    val name: String
}

interface Edge : ClientAware {
    val id: String
    val relation: String
    val startId: String
    val endId: String
}

@NodeEntity
data class Employee(
    /**
     * Generated value should be explicitly null, otherwise neo4j does not fill it.
     */
    @Id
    override val id: String,
    override val name: String,
    override val label: String = "Employee",
    val firstName: String? = null,
    val lastName: String? = null,
    @Index
    override val clientId: String,
) : Vertex

@NodeEntity
data class Project(
    /**
     * Generated value should be explicitly null, otherwise neo4j does not fill it.
     */
    @Id
    override val id: String,
    override val name: String,
    override val label: String = "Project",
    @Index
    override val clientId: String,
) : Vertex

@NodeEntity
data class CodeReview(
    /**
     * Generated value should be explicitly null, otherwise neo4j does not fill it.
     */
    @Id
    override val id: String,
    override val name: String,
    override val label: String = "CodeReview",
    @Index
    override val clientId: String,
) : Vertex

@NodeEntity
data class CalendarEvent(
    @Id
    override val id: String,
    @Property
    override val name: String,
    override val label: String = "CalendarEvent",
    @Index
    override val clientId: String,
) : Vertex

@RelationshipEntity(type = "PARTICIPATES_IN_MEETING")
data class Participant(
    @Id
    override val id: String,
    override val relation: String = "PARTICIPATES_IN_MEETING",
    @StartNode
    private val calendarEvent: CalendarEvent?,
    @EndNode
    private val employee: Employee?,
    @Index
    override val clientId: String,
) : Edge {
    override val startId: String
        get() = calendarEvent!!.id
    override val endId: String
        get() = employee!!.id
}

@RelationshipEntity(type = "MEMBER_OF_PROJECT")
data class ProjectMember(
    @Id
    override val id: String,
    override val relation: String = "MEMBER_OF_PROJECT",
    @StartNode
    private val project: Project?,
    @EndNode
    private val employee: Employee?,
    @Index
    override val clientId: String,
) : Edge {
    override val startId: String
        get() = project!!.id
    override val endId: String
        get() = employee!!.id
}

@RelationshipEntity(type = "CODE_REVIEW_MEMBER")
data class CodeReviewMember(
    @Id
    override val id: String,
    override val relation: String = "CODE_REVIEW_MEMBER",
    @StartNode
    private val codeReview: CodeReview?,
    @EndNode
    private val employee: Employee?,
    @Index
    override val clientId: String,
) : Edge {
    override val startId: String
        get() = codeReview!!.id
    override val endId: String
        get() = employee!!.id
}