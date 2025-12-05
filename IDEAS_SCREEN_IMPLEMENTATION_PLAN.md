# Plan d'Implémentation - Ideas Screen avec MCP Integration

## 📋 Vue d'ensemble

Ce document décrit le plan d'implémentation pour un **Ideas Screen** qui permettra aux utilisateurs de:
- Discuter avec un assistant MCP (Model Context Protocol) connecté au contexte du projet
- Poser des questions sur le projet, les tâches, les meetings, les discussions
- Créer des tâches depuis les conversations
- Obtenir des informations sur le projet et les réunions
- Sauvegarder des idées depuis meetings, chats, et tasks
- Afficher une liste d'idées avec création manuelle

## 🎯 Objectifs

1. **UI/UX**: Interface chat similaire à SelfNotesScreen avec sélection de projet
2. **Architecture**: MVVM avec Flow/StateFlow (pattern existant)
3. **Réutilisation**: Maximiser la réutilisation des composants existants
4. **Qualité**: Code flowless, clean, prêt pour review

---

## 🏗️ Architecture

### Structure MVVM

```
IdeasScreen (Composable)
    ↓
IdeasViewModel (StateFlow<IdeasUIState>)
    ↓
IdeasRepository (Interface)
    ↓
FirestoreIdeasRepository (Implémentation)
```

### Pattern de State Management

Suivre le pattern de `SelfNotesViewModel`:
- `StateFlow<IdeasUIState>` comme source unique de vérité
- Utilisation de `combine()` pour combiner plusieurs flows
- `SharingStarted.WhileSubscribed(5000)` pour lifecycle management
- Gestion d'erreurs avec sealed classes

---

## 📁 Structure des Fichiers

### Nouveaux Fichiers à Créer

```
app/src/main/java/ch/eureka/eurekapp/
├── ui/
│   └── ideas/
│       ├── IdeasScreen.kt                    # Screen principal
│       ├── IdeasViewModel.kt                 # ViewModel avec StateFlow
│       ├── IdeasUIState.kt                   # Data class pour UI state
│       ├── IdeaMessageBubble.kt              # Composant message (réutilise MessageBubble)
│       ├── QuickQuestionButtons.kt           # Boutons pour questions rapides
│       └── ProjectSelector.kt                # Sélecteur de projet (réutilise pattern existant)
│
├── model/
│   └── data/
│       └── idea/
│           ├── Idea.kt                       # Data class pour une idée
│           ├── IdeaRepository.kt             # Interface repository
│           ├── FirestoreIdeasRepository.kt    # Implémentation Firestore
│           └── IdeaSource.kt                 # Enum pour source linking (MEETING, CHAT, TASK, MANUAL)
│
└── model/
    └── mcp/
        └── MCPContextBuilder.kt               # Builder pour construire le contexte MCP
        └── MCPService.kt                     # Service pour communiquer avec MCP
```

### Fichiers à Modifier

```
app/src/main/java/ch/eureka/eurekapp/
├── model/data/
│   └── FirestorePaths.kt                     # Ajouter IDEAS constant
│
├── navigation/
│   └── Navigation.kt                         # Ajouter route IdeasSection
│
└── model/data/
    └── RepositoriesProvider.kt               # Ajouter ideasRepository
```

---

## 🗄️ Structure Firestore

### Collection Ideas

```
projects/{projectId}/
  └── ideas/{ideaId}
      ├── ideaId: String
      ├── projectId: String
      ├── title: String? (optional, pour idées manuelles)
      ├── content: String
      ├── sourceType: String (MEETING, CHAT, TASK, MANUAL)
      ├── sourceId: String? (ID de la source: meetingId, chatId, taskId)
      ├── sourceMetadata: Map<String, Any>? (infos supplémentaires)
      ├── createdBy: String
      ├── createdAt: Timestamp
      └── lastUpdated: Timestamp
```

### Collection Ideas Chat Messages

```
projects/{projectId}/
  └── ideas/{ideaId}/
      └── messages/{messageId}
          ├── messageId: String
          ├── text: String
          ├── isFromUser: Boolean
          ├── createdAt: Timestamp
          └── metadata: Map<String, Any>? (pour contexte MCP)
```

---

## 🎨 Composants UI à Réutiliser

### Composants Existants (Réutilisation Directe)

1. **MessageInputField** (`ui/components/MessageInputField.kt`)
   - ✅ Réutiliser tel quel pour l'input de chat
   - Modifier placeholder: "Ask about the project..."

2. **MessageBubble** (`ui/components/MessageBubble.kt`)
   - ✅ Réutiliser pour afficher les messages
   - Créer wrapper `IdeaMessageBubble` similaire à `SelfNoteMessageBubble`

3. **EurekaFilterBar** (`ui/components/EurekaFilterBar.kt`)
   - ✅ Réutiliser pour filtrer les idées par source (si nécessaire)

4. **BackButton** (`ui/components/BackButton.kt`)
   - ✅ Réutiliser dans la TopBar

### Composants à Adapter

1. **ProjectSelector**
   - S'inspirer de `CreateConversationScreen.kt` (lignes 180-212)
   - Utiliser `ExposedDropdownMenuBox` avec liste de projets
   - Afficher le projet sélectionné dans la TopBar

2. **QuickQuestionButtons**
   - Nouveau composant inspiré des patterns existants
   - Afficher seulement quand `messages.isEmpty()`
   - Boutons: "What are the project goals?", "Show me pending tasks", etc.

---

## 📊 Data Models

### Idea.kt

```kotlin
data class Idea(
    val ideaId: String = "",
    val projectId: String = "",
    val title: String? = null,  // Pour idées manuelles
    val content: String = "",
    val sourceType: IdeaSource = IdeaSource.MANUAL,
    val sourceId: String? = null,
    val sourceMetadata: Map<String, String>? = null,
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val lastUpdated: Timestamp? = null
)
```

### IdeaSource.kt

```kotlin
enum class IdeaSource {
    MEETING,
    CHAT,
    TASK,
    MANUAL
}
```

### IdeasUIState.kt

```kotlin
data class IdeasUIState(
    val selectedProjectId: String? = null,
    val selectedProject: Project? = null,
    val availableProjects: List<Project> = emptyList(),
    val messages: List<Message> = emptyList(),
    val ideas: List<Idea> = emptyList(),  // Pour la liste d'idées
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingProjects: Boolean = false,
    val errorMsg: String? = null,
    val isMCPConnected: Boolean = false
)
```

---

## 🔄 Flows et State Management

### IdeasViewModel Pattern

Suivre exactement le pattern de `SelfNotesViewModel`:

```kotlin
class IdeasViewModel(
    private val ideasRepository: IdeaRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val meetingRepository: MeetingRepository,
    private val chatRepository: ChatRepository,
    private val mcpService: MCPService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    
    private val _currentMessage = MutableStateFlow("")
    private val _isSending = MutableStateFlow(false)
    private val _selectedProjectId = MutableStateFlow<String?>(null)
    private val _errorMsg = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<IdeasUIState> = combine(
        _selectedProjectId.flatMapLatest { projectId ->
            projectId?.let { 
                combine(
                    ideasRepository.getIdeasForProject(it),
                    ideasRepository.getMessagesForIdea(it, "current"), // Chat actuel
                    projectRepository.getProjectById(it)
                ) { ideas, messages, project ->
                    Triple(ideas, messages, project)
                }
            } ?: flowOf(Triple(emptyList(), emptyList(), null))
        },
        _currentMessage,
        _isSending,
        _errorMsg,
        projectRepository.getProjectsForCurrentUser()
    ) { combined, currentMsg, isSending, error, projects ->
        // Build UI state
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IdeasUIState(isLoading = true)
    )
}
```

---

## 🚀 Milestones d'Implémentation

### Milestone 1: Foundation & Data Layer
**Objectif**: Créer la structure de base et le repository

#### Tâches:
- [ ] Créer `Idea.kt` data class
- [ ] Créer `IdeaSource.kt` enum
- [ ] Créer `IdeaRepository.kt` interface
- [ ] Créer `FirestoreIdeasRepository.kt` avec méthodes:
  - `getIdeasForProject(projectId: String): Flow<List<Idea>>`
  - `createIdea(idea: Idea): Result<String>`
  - `getMessagesForIdea(projectId: String, ideaId: String): Flow<List<Message>>`
  - `sendMessage(projectId: String, ideaId: String, message: Message): Result<Unit>`
- [ ] Ajouter `IDEAS` constant dans `FirestorePaths.kt`
- [ ] Ajouter `ideasRepository` dans `RepositoriesProvider.kt`
- [ ] Tests unitaires pour repository

**Critères de complétion**:
- ✅ Repository fonctionne avec Firestore
- ✅ Flows émettent correctement
- ✅ Tests passent

---

### Milestone 2: ViewModel & State Management
**Objectif**: Implémenter le ViewModel avec StateFlow

#### Tâches:
- [ ] Créer `IdeasUIState.kt`
- [ ] Créer `IdeasViewModel.kt` avec:
  - StateFlow combinant projets, idées, messages
  - Méthodes: `selectProject()`, `updateMessage()`, `sendMessage()`
  - Gestion d'erreurs avec sealed classes
- [ ] Implémenter la logique de sélection de projet
- [ ] Implémenter la logique d'envoi de message (sans MCP pour l'instant)
- [ ] Gestion du loading state
- [ ] Tests unitaires pour ViewModel

**Critères de complétion**:
- ✅ ViewModel suit le pattern MVVM existant
- ✅ StateFlow émet correctement
- ✅ Gestion d'erreurs fonctionnelle
- ✅ Tests passent

---

### Milestone 3: UI Components - Base Screen
**Objectif**: Créer le screen principal avec sélection de projet

#### Tâches:
- [ ] Créer `IdeasScreen.kt` avec structure de base:
  - Scaffold avec TopBar et BottomBar
  - ProjectSelector dans TopBar
  - MessageInputField dans BottomBar
  - LazyColumn pour messages (vide pour l'instant)
- [ ] Créer `ProjectSelector.kt` (réutilise pattern de `CreateConversationScreen`)
- [ ] Ajouter route dans `Navigation.kt`:
  ```kotlin
  sealed interface IdeasSection : Route {
      @Serializable data class Ideas(val projectId: String?) : IdeasSection
  }
  ```
- [ ] Intégrer dans navigation avec BottomBar si nécessaire
- [ ] Gérer les états: loading, empty, error

**Critères de complétion**:
- ✅ Screen s'affiche correctement
- ✅ Sélection de projet fonctionne
- ✅ Navigation fonctionne
- ✅ États gérés correctement

---

### Milestone 4: Chat Interface
**Objectif**: Afficher les messages et permettre l'envoi

#### Tâches:
- [ ] Créer `IdeaMessageBubble.kt` (wrapper autour de `MessageBubble`)
- [ ] Intégrer `IdeaMessageBubble` dans `IdeasScreen`
- [ ] Implémenter l'affichage des messages dans LazyColumn
- [ ] Connecter `MessageInputField` au ViewModel
- [ ] Implémenter `sendMessage()` dans ViewModel (sans MCP)
- [ ] Auto-scroll vers le bas quand nouveau message
- [ ] Gérer l'état "sending..."

**Critères de complétion**:
- ✅ Messages s'affichent correctement
- ✅ Envoi de message fonctionne (sans réponse MCP)
- ✅ UI responsive et fluide
- ✅ Auto-scroll fonctionne

---

### Milestone 5: Quick Question Buttons
**Objectif**: Afficher les boutons de questions rapides

#### Tâches:
- [ ] Créer `QuickQuestionButtons.kt`
- [ ] Afficher seulement quand `messages.isEmpty()`
- [ ] Boutons suggérés:
  - "What are the project goals?"
  - "Show me pending tasks"
  - "What was discussed in recent meetings?"
  - "Summarize project status"
- [ ] Au clic, pré-remplir le message et envoyer
- [ ] Design cohérent avec le design system

**Critères de complétion**:
- ✅ Boutons s'affichent au bon moment
- ✅ Clic pré-remplit et envoie le message
- ✅ Design cohérent

---

### Milestone 6: MCP Integration (Backend - Phase 1)
**Objectif**: Préparer l'intégration MCP (structure, pas l'implémentation complète)

#### Tâches:
- [ ] Créer `MCPService.kt` interface
- [ ] Créer `MCPContextBuilder.kt` pour construire le contexte:
  - Infos du projet
  - Liste des tâches
  - Messages de chat du projet
  - Transcripts des meetings
  - Participants
- [ ] Créer structure de réponse MCP
- [ ] Préparer l'intégration dans ViewModel (méthode stub)

**Critères de complétion**:
- ✅ Structure MCP prête
- ✅ ContextBuilder peut collecter les données
- ✅ Interface définie (implémentation MCP réelle à faire plus tard)

---

### Milestone 7: Ideas List View
**Objectif**: Afficher la liste des idées sauvegardées

#### Tâches:
- [ ] Créer composant `IdeasList.kt` (similaire à notes list)
- [ ] Afficher les idées avec source linking:
  - Icône selon source (MEETING, CHAT, TASK, MANUAL)
  - Titre ou extrait du contenu
  - Date de création
  - Lien vers la source (si applicable)
- [ ] Implémenter navigation vers le chat de l'idée
- [ ] Filtrage par source (optionnel, réutilise `EurekaFilterBar`)

**Critères de complétion**:
- ✅ Liste d'idées s'affiche
- ✅ Navigation vers chat fonctionne
- ✅ Source linking visible

---

### Milestone 8: Manual Idea Creation
**Objectif**: Permettre la création manuelle d'idées

#### Tâches:
- [ ] Ajouter bouton "New Idea" dans TopBar
- [ ] Créer dialog/bottom sheet pour création:
  - Champ titre (optionnel)
  - Champ contenu
  - Sélection de projet
- [ ] Implémenter `createIdea()` dans ViewModel
- [ ] Sauvegarder dans Firestore
- [ ] Naviguer vers le chat de l'idée créée

**Critères de complétion**:
- ✅ Création manuelle fonctionne
- ✅ Idée sauvegardée dans Firestore
- ✅ Navigation vers chat fonctionne

---

### Milestone 9: "Save as Idea" Actions
**Objectif**: Permettre de sauvegarder depuis meetings, chats, tasks

#### Tâches:
- [ ] Ajouter action "Save as Idea" dans:
  - `MeetingDetailScreen.kt` (menu contextuel)
  - `ConversationDetailScreen.kt` (menu contextuel)
  - `ViewTaskScreen.kt` (menu contextuel)
- [ ] Créer dialog de confirmation avec prévisualisation
- [ ] Implémenter `saveAsIdea()` dans ViewModel
- [ ] Sauvegarder avec source linking:
  - `sourceType`: MEETING/CHAT/TASK
  - `sourceId`: ID de la source
  - `sourceMetadata`: Infos supplémentaires (titre, extrait, etc.)
- [ ] Naviguer vers Ideas screen après sauvegarde

**Critères de complétion**:
- ✅ Actions disponibles dans les screens
- ✅ Sauvegarde avec source linking fonctionne
- ✅ Navigation fonctionne

---

### Milestone 10: Polish & Testing
**Objectif**: Finaliser et tester

#### Tâches:
- [ ] Tests UI (compose testing)
- [ ] Tests ViewModel
- [ ] Tests Repository
- [ ] Vérifier tous les edge cases:
  - Projet non sélectionné
  - Pas de projets disponibles
  - Erreurs réseau
  - Messages vides
- [ ] Optimisations:
  - LazyColumn performance
  - Image loading si nécessaire
  - Caching si nécessaire
- [ ] Documentation code
- [ ] Review du code pour flowless implementation

**Critères de complétion**:
- ✅ Tous les tests passent
- ✅ Code review ready
- ✅ Documentation complète
- ✅ Performance optimale

---

## 🔌 Intégration MCP (Future - Phase 2)

**Note**: L'intégration MCP complète sera faite dans une phase séparée. Pour l'instant, on prépare la structure.

### Structure MCP Context

```kotlin
data class MCPContext(
    val project: Project,
    val tasks: List<Task>,
    val meetings: List<Meeting>,
    val chatMessages: List<Message>,
    val transcripts: List<Transcript>,
    val participants: List<User>
)
```

### MCP Service Interface

```kotlin
interface MCPService {
    suspend fun sendMessage(
        projectId: String,
        message: String,
        context: MCPContext
    ): Result<String> // Réponse du MCP
}
```

---

## 🎨 Design Guidelines

### Réutilisation de Composants

1. **MessageInputField**: Réutiliser tel quel
2. **MessageBubble**: Réutiliser via wrapper
3. **EurekaFilterBar**: Réutiliser pour filtres
4. **ProjectSelector**: Adapter depuis CreateConversationScreen
5. **TopBar**: Pattern similaire à SelfNotesScreen

### Design System

- Utiliser `MaterialTheme.colorScheme`
- Utiliser `EurekaStyles` pour les shapes et elevations
- Utiliser `Spacing` tokens pour les espacements
- Suivre les patterns de `SelfNotesScreen` pour la cohérence

---

## 📝 Notes d'Implémentation

### Patterns à Suivre

1. **ViewModel**: Exactement comme `SelfNotesViewModel`
   - StateFlow avec combine
   - SharingStarted.WhileSubscribed(5000)
   - Gestion d'erreurs avec sealed classes

2. **Repository**: Exactement comme `FirestoreProjectRepository`
   - callbackFlow pour real-time updates
   - awaitClose pour cleanup
   - Result<T> pour error handling

3. **UI State**: Data class immuable
   - Toutes les propriétés dans un seul data class
   - Computed properties avec `get()` si nécessaire

4. **Navigation**: Sealed interface dans Route
   - `IdeasSection` comme sous-interface de `Route`
   - Type-safe navigation

### Points d'Attention

1. **Performance**: 
   - LazyColumn avec keys appropriées
   - Éviter les recompositions inutiles
   - Caching des projets si nécessaire

2. **Error Handling**:
   - Toujours afficher les erreurs à l'utilisateur
   - Snackbar pour erreurs temporaires
   - État d'erreur dans UIState

3. **Lifecycle**:
   - Nettoyer les listeners Firestore
   - Gérer les coroutines correctement
   - Éviter les memory leaks

---

## ✅ Checklist Finale

Avant de considérer l'implémentation complète:

- [ ] Tous les milestones complétés
- [ ] Tests passent (unitaires + UI)
- [ ] Code review effectué
- [ ] Documentation à jour
- [ ] Performance vérifiée
- [ ] Edge cases gérés
- [ ] Design cohérent avec l'app
- [ ] Navigation fonctionne
- [ ] Firestore rules mises à jour (si nécessaire)

---

## 📚 Références

### Fichiers de Référence

1. **SelfNotesScreen.kt**: Pattern de screen avec chat
2. **SelfNotesViewModel.kt**: Pattern de ViewModel avec StateFlow
3. **ConversationDetailScreen.kt**: Pattern de chat avec messages
4. **CreateConversationScreen.kt**: Pattern de sélection de projet
5. **FirestoreProjectRepository.kt**: Pattern de repository Firestore

### Patterns Clés

- MVVM avec StateFlow
- Flow-based data layer
- Real-time Firestore updates
- Type-safe navigation
- Composants réutilisables

---

**Date de création**: [Date]
**Dernière mise à jour**: [Date]
**Auteur**: [Nom]
