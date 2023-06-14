package com.achugr.voyager

import com.achugr.voyager.entity.*
import com.achugr.voyager.entity.CalendarEvent
import com.achugr.voyager.entity.Participant
import com.achugr.voyager.storage.transaction
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.jetbrains.api.runtime.*
import space.jetbrains.api.runtime.resources.calendars
import space.jetbrains.api.runtime.resources.projects
import space.jetbrains.api.runtime.resources.teamDirectory
import space.jetbrains.api.runtime.types.*
import kotlin.collections.Map
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.math.log

class Grabber(private val spaceClient: SpaceClient) {
    private val log: Logger = LoggerFactory.getLogger("Grabber.kt")

    suspend fun grab() {
        val employees = grabProfiles()
        grabProjects(employees)
        grapMeetings(employees)
    }

    private suspend fun grapMeetings(employees: Map<String, Employee>) {
        grabData({ batchRequest -> spaceClient.calendars.meetings.getAllMeetings(batchInfo = batchRequest) }) { event: DTO_Meeting ->
            transaction { session ->
                val calendarEvent = CalendarEvent(
                    id = event.id,
                    name = event.summary,
                    clientId = clientId()
                )
                session.save(calendarEvent)
                event.profiles.forEach { participant ->
                    employees[participant.id]?.let {
                        session.save(
                            Participant(
                                id = "${event.id}-${participant.id}",
                                calendarEvent = calendarEvent,
                                employee = it,
                                clientId = clientId()
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun grabProjects(employees: Map<String, Employee>) {
        val projects = grabData({ batchRequest ->
            spaceClient.projects.getAllProjects(
                batchInfo = batchRequest
            ) {
                defaultPartial()
                memberProfiles() {
                    id()
                }
            }
        }) { projectDto: PR_Project ->
            transaction { session ->
                val project = Project(
                    id = projectDto.id,
                    name = projectDto.name,
                    clientId = clientId()
                )
                session.save(project)
                projectDto.memberProfiles.forEach { participant ->
                    employees[participant.id]?.let {
                        session.save(
                            ProjectMember(
                                id = "${project.id}-${participant.id}",
                                project = project,
                                employee = it,
                                clientId = clientId()
                            )
                        )
                    }
                }
                project
            }
        }

        projects.forEach { project ->
            grabData({ batchRequest ->
                spaceClient.projects.codeReviews.getAllCodeReviews(
                    batchInfo = batchRequest,
                    project = ProjectIdentifier.Id(
                        project.id
                    ),
                    state = CodeReviewStateFilter.Closed
                ) {
                    defaultPartial()
                    review {
                        id()
                        issueIds()
                        title()
                    }
                    participants {
                        participants {
                        }
                    }
                }
            }) { codeReviewDto ->
                transaction { session ->
                    val review = codeReviewDto.review
                    val name = getReviewName(review)
                    val codeReview = CodeReview(
                        id = review.id,
                        name = name,
                        clientId = clientId()
                    )
                    session.save(codeReview)
                    codeReviewDto.participants.participants?.forEach { participantDto ->
                        session.save(
                            CodeReviewMember(
                                id = "${codeReview.id}-${participantDto.user.id}",
                                codeReview = codeReview,
                                employee = employees[participantDto.user.id],
                                clientId = clientId()
                            )
                        )
                    }
                }
            }
        }
    }

    private fun getReviewName(review: CodeReviewRecord) = when (review) {
        is MergeRequestRecord -> {
            review.title
        }

        is CommitSetReviewRecord -> {
            review.title
        }

        else -> {
            "Code review"
        }
    }

    private suspend fun <T, E> grabData(
        apiCall: suspend (batchInfo: BatchInfo) -> Batch<T>,
        processor: (T) -> E
    ): List<E> {
        val results = mutableListOf<E>()
        var batch = apiCall(BatchInfo(offset = null, batchSize = 20))
        while (batch.data.isNotEmpty()) {
            batch.data.forEach {
                try {
                    val res = processor(it)
                    results.add(res)
                } catch (e: Exception) {
                    log.error(e.message, e)
                }
            }
            batch = apiCall(BatchInfo(batch.next, 20))
        }
        return results
    }

    private suspend fun grabProfiles(): Map<String, Employee> {
        val employeesMap = mutableMapOf<String, Employee>()
        var employeesResponse = spaceClient.teamDirectory.profiles.getAllProfiles(batchInfo = BatchInfo(null, 20))
        while (employeesResponse.data.isNotEmpty()) {
            transaction { session ->
                employeesResponse.data.forEach { profile ->
                    val employee = Employee(
                        id = profile.id,
                        name = profile.username,
                        firstName = profile.name.firstName,
                        lastName = profile.name.lastName,
                        clientId = clientId()
                    )
                    session.save(employee)
                    employeesMap[employee.id] = employee
                }
            }
            employeesResponse =
                spaceClient.teamDirectory.profiles.getAllProfiles(batchInfo = BatchInfo(employeesResponse.next, 20))
        }
        return employeesMap
    }

    private fun clientId() = spaceClient.appInstance.clientId
}

fun main() {
    val spaceClient = SpaceClient(
        SpaceAppInstance(
            config.getString("local.space.clientId"),
            config.getString("local.space.clientSecret"),
            config.getString("local.space.clientUrl")
        ), SpaceAuth.ClientCredentials()
    )
    runBlocking {
        Grabber(spaceClient).grab()
//        transaction { session ->
//            session.query(
//                "Match (n)-[r]-(m)\n" +
//                        "Return n,r,m", mapOf<String, Any>(), true
//            ).forEach {
//                println(it.entries)
//            }
//        }
    }
    println("saved")
}