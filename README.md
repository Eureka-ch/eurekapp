# EurekApp
EurekApp is an application for teams to collaborate on projects. It allows for easy assignment and tracking of task status, including reminders via notifications, recording of meetings and their AI transcription or summary, and sharing of hand-written notes using photos from the camera.

## Main features
- Task management: The application includes conversations for team collaboration and sharing ideas. Users can create tasks with custom templates, set deadlines, notifications, assign team members, and view the status of work on individual tasks as well as the overall project progress (similar to Jira). Tasks can also have dependencies on other tasks. The app includes an automatic assignment algorithm that distributes unassigned tasks to team members based on their current workload and task dependencies.
- Activity feed: The application provides a centralized activity feed that tracks all important actions within a project, such as task updates, meeting changes, and new messages, helping team members stay informed about what's happening.
- Meeting scheduling: EurekApp simplifies meeting planning with a proposal voting system. Users can create meeting proposals with multiple time slots, and team members vote on their preferred times. The app integrates with Google Calendar to create calendar events with reminders. Once scheduled, users can share the online meeting link with participants or use turn-by-turn navigation to the meeting location.
- Meeting recordings and transcription: EurekApp makes it easy to record every meeting with automatic transcription using Google Cloud Speech-to-Text. The app can generate AI-powered summaries of transcripts. This reduces the need to switch between applications, and makes it very easy to share the recording in the chat, for example with team members who were unable to attend the meeting for whatever reason.
- File management: Tasks and meetings can have file attachments, including photos from the camera, documents, and other files, making it easy to keep all project-related materials organized in one place.
- AI-powered idea discussions: Users can have conversations with AI to brainstorm and develop ideas for their projects. Ideas can be shared with team members and serve as a foundation for creating tasks.
- Self-notes: The application includes a personal note-taking feature with an offline-first architecture. Notes are stored locally on the device for instant access and can optionally be synced to the cloud. Users have full control over their privacy with the ability to choose between local-only storage or cloud synchronization.

## Technology
- Artificial intelligence (LLM): EurekApp uses LLMs to generate summaries of meeting transcripts and to facilitate AI-powered brainstorming conversations, helping teams understand key points from meetings and develop new ideas efficiently.
- Google Sign-In: EurekApp uses Google Sign-In because it is a very simple way to authenticate users and invite members to the team. It also allows users to start working with the application almost immediately without having to register, which could discourage some users.

## Advantages
- Overview of tasks: As soon as users open the app, they can see the current status and progress of their tasks and those of their team. The activity feed keeps users informed about all important changes, so there's no need to scroll through chaotic chats to find out what's going on.
- Centralized coordination: Thanks to the integrated conversations and task management, there is no risk of the team forgetting an idea discussed in chat. This means there will be no need to spend a long time searching WhatsApp, Telegram, or Google Chat to find out what the idea actually was.
- AI-assisted brainstorming: The AI-powered idea discussions help users develop and refine concepts through interactive conversations, making it easier to generate creative solutions and structure ideas before turning them into actionable tasks.
- Automatic task distribution: The intelligent assignment algorithm ensures that tasks are fairly distributed among team members based on their current workload, preventing bottlenecks and ensuring efficient project progress.
- Quick meeting scheduling: EurekApp allows users to find a suitable meeting time through a voting system without lengthy conversations. It also integrates with Google Calendar, allows sharing of online meeting links with participants, and provides turn-by-turn navigation to the meeting location, further reducing the overhead of team collaboration.
- Preserving meeting content: The application allows users to record meetings with automatic transcription and AI-generated summaries. This ensures that absent members are always up to date and can quickly catch up on what was discussed.
- Offline accessibility: The self-notes feature works completely offline with local storage, ensuring users can always access their personal notes even without an internet connection. This makes the app reliable in any situation.

## Figma Project link
View the complete design on [Figma](https://www.figma.com/design/bKRZnuU5m3tkV8UKezHdAy/Eureka-figma?node-id=0-1).

---

*Portions of this file were co-authored with Claude 4.5 Sonnet.*

