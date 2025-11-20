package ch.eureka.eurekapp.model.calendar

import android.content.ContentResolver
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MeetingCalendarViewModel(
    private val calendarRepository: CalendarRepository = CalendarRepositoryProvider.repository,
    private val meetingsRepository: MeetingRepository = FirestoreRepositoriesProvider
        .meetingRepository,
    private val usersRepository: UserRepository = FirestoreRepositoriesProvider.userRepository
): ViewModel() {
    private val _registeredMeetings = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val registeredMeetings = _registeredMeetings.asStateFlow()
    fun checkIsMeetingRegisteredInCalendar(contentResolver: ContentResolver, meeting: Meeting){
        viewModelScope.launch {
            require(meeting.status == MeetingStatus.SCHEDULED)
            try {
                val meetingCalendarEvent = parseMeetingIntoCalendarMeetingDataclass(meeting)
                val eventGot = calendarRepository.getCalendarEvent(contentResolver,
                    meeting.meetingID)

                _registeredMeetings.value = _registeredMeetings.value +
                        (meeting.meetingID to
                                (eventGot.isSuccess && meetingCalendarEvent.eventUid ==
                                eventGot.getOrNull()?.eventUid))
            }catch(e: Exception){
                Log.d("MeetingCalendarViewModel", e.message.toString())
            }
        }
    }

    fun addMeetingToCalendar(contentResolver: ContentResolver, meeting: Meeting,
                             onSuccess: () -> Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
                val meetingToCalendarDataclass = parseMeetingIntoCalendarMeetingDataclass(meeting)
                calendarRepository.createCalendarEvent(contentResolver,
                    meetingToCalendarDataclass).onSuccess {
                    _registeredMeetings.value = _registeredMeetings.value +
                            (meeting.meetingID to true)
                    onSuccess()
                }.onFailure { error ->
                    onFailure(error.message.toString())
                }
        }
    }

    private suspend fun parseMeetingIntoCalendarMeetingDataclass(meeting: Meeting): CalendarEventData
        {
            val attendees = meetingsRepository
                .getParticipants(meeting.projectId, meeting.meetingID)
                .flatMapLatest { participants ->
                    val usersFlow = participants
                        .map { participant -> usersRepository.getUserById(participant.userId) }
                    combine(usersFlow) { users ->
                        users.toList()
                    }
                }.first().filterNotNull().map { user ->
                    CalendarAttendee(
                        email = user.email,
                        name = user.displayName,
                    )
                }

            return CalendarEventData(
                title = meeting.title,
                description = meeting.meetingID,
                location = meeting.location?.name,
                startTimeMillis = meeting.datetime?.toDate()?.time ?: 0L,
                endTimeMillis = (meeting.datetime?.toDate()?.time
                    ?: 0L) + (meeting.duration * 60 + 1000),
                eventUid = meeting.meetingID,
                attendees = attendees
            )
        }
}