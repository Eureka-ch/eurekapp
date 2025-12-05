# Spécification du ViewModel pour Ideas Screen

## 📋 Vue d'ensemble

**Important :** Une "Idea" est une **conversation distincte avec l'IA**. Il peut y avoir **plusieurs Ideas par projet**. Chaque Idea a :
- Sa propre conversation (messages)
- Un titre/description optionnel
- Des participants (pour partager)
- Peut être supprimée

Le ViewModel (`IdeasViewModel`) doit gérer **uniquement la logique de présentation et la coordination** entre l'UI et les repositories. Il ne doit **PAS** implémenter la logique métier (celle-ci sera dans les repositories et services MCP).

## 🎯 Deux Modes d'Affichage

### Mode 1 : Liste des Ideas
- Affiche toutes les Ideas du projet sélectionné
- Permet de créer une nouvelle Idea
- Permet de sélectionner une Idea pour voir sa conversation
- Permet de supprimer une Idea

### Mode 2 : Conversation d'une Idea
- Affiche les messages de l'Idea sélectionnée
- Permet d'envoyer des messages
- Permet d'ajouter des participants (partager)
- Permet de supprimer l'Idea

## 🎯 Responsabilités du ViewModel

### 1. **Gestion de l'État UI (StateFlow)**

Le ViewModel doit exposer un `StateFlow<IdeasUIState>` qui combine toutes les données nécessaires à l'UI :

```kotlin
val uiState: StateFlow<IdeasUIState>
```

**Ce que ça doit contenir :**
- `selectedProject: Project?` - Projet actuellement sélectionné
- `availableProjects: List<Project>` - Liste des projets disponibles pour l'utilisateur
- `ideas: List<Idea>` - Liste des Ideas du projet sélectionné (mode liste)
- `selectedIdea: Idea?` - Idea actuellement sélectionnée (mode conversation)
- `messages: List<Message>` - Messages de l'Idea sélectionnée (mode conversation)
- `currentMessage: String` - Texte du message en cours de saisie
- `isSending: Boolean` - Indique si un message est en cours d'envoi
- `isLoading: Boolean` - Indique si des données sont en cours de chargement
- `errorMsg: String?` - Message d'erreur à afficher (ou null)
- `viewMode: IdeasViewMode` - Mode d'affichage (LIST ou CONVERSATION)

**Pattern à suivre :** Comme `SelfNotesViewModel` ou `ConversationDetailViewModel`
- Utiliser `combine()` pour combiner plusieurs flows
- Utiliser `stateIn()` avec `SharingStarted.WhileSubscribed(5000)`
- Gérer les erreurs avec `.catch { }`

---

### 2. **Chargement des Projets**

**Méthode :** `init { }` ou dans le constructeur

**Ce qu'elle fait :**
- Charge la liste des projets de l'utilisateur depuis `ProjectRepository`
- Utilise `projectRepository.getProjectsForCurrentUser()`
- Met à jour `availableProjects` dans le StateFlow

**Exemple :**
```kotlin
private val projectsFlow = projectRepository.getProjectsForCurrentUser()
```

---

### 3. **Sélection d'un Projet**

**Méthode :** `fun selectProject(project: Project)`

**Ce qu'elle fait :**
- Met à jour `selectedProject` dans le StateFlow
- Charge la liste des Ideas pour ce projet
- Passe en mode LISTE
- Réinitialise `selectedIdea` et `messages`

**Exemple :**
```kotlin
fun selectProject(project: Project) {
    viewModelScope.launch {
        _selectedProject.value = project
        _viewMode.value = IdeasViewMode.LIST
        _selectedIdea.value = null
        // Charger les Ideas via ideasRepository.getIdeasForProject(project.projectId)
    }
}
```

---

### 3b. **Sélection d'une Idea (Passer en mode Conversation)**

**Méthode :** `fun selectIdea(idea: Idea)`

**Ce qu'elle fait :**
- Met à jour `selectedIdea` dans le StateFlow
- Charge les messages de cette Idea
- Passe en mode CONVERSATION

**Exemple :**
```kotlin
fun selectIdea(idea: Idea) {
    viewModelScope.launch {
        _selectedIdea.value = idea
        _viewMode.value = IdeasViewMode.CONVERSATION
        // Charger les messages via ideasRepository.getMessagesForIdea(idea.ideaId)
    }
}
```

---

### 3c. **Création d'une Nouvelle Idea**

**Méthode :** `fun createNewIdea(title: String? = null)`

**Ce qu'elle fait :**
1. Vérifie qu'un projet est sélectionné
2. Crée une nouvelle Idea via le repository
3. Sélectionne automatiquement cette nouvelle Idea
4. Passe en mode CONVERSATION

**Exemple :**
```kotlin
fun createNewIdea(title: String? = null) {
    val projectId = _selectedProject.value?.projectId ?: return
    val currentUserId = getCurrentUserId() ?: return
    
    viewModelScope.launch {
        val idea = Idea(
            ideaId = IdGenerator.generateIdeaId(),
            projectId = projectId,
            title = title,
            createdBy = currentUserId,
            participantIds = listOf(currentUserId) // Créateur est participant
        )
        
        ideasRepository.createIdea(idea)
            .fold(
                onSuccess = { ideaId ->
                    _selectedIdea.value = idea
                    _viewMode.value = IdeasViewMode.CONVERSATION
                },
                onFailure = { error ->
                    _errorMsg.value = error.message
                }
            )
    }
}
```

---

### 3d. **Suppression d'une Idea**

**Méthode :** `fun deleteIdea(ideaId: String)`

**Ce qu'elle fait :**
1. Supprime l'Idea via le repository
2. Si c'était l'Idea sélectionnée, retourne en mode LISTE
3. Met à jour la liste des Ideas

**Exemple :**
```kotlin
fun deleteIdea(ideaId: String) {
    val projectId = _selectedProject.value?.projectId ?: return
    
    viewModelScope.launch {
        ideasRepository.deleteIdea(projectId, ideaId)
            .fold(
                onSuccess = {
                    if (_selectedIdea.value?.ideaId == ideaId) {
                        _selectedIdea.value = null
                        _viewMode.value = IdeasViewMode.LIST
                    }
                },
                onFailure = { error ->
                    _errorMsg.value = error.message
                }
            )
    }
}
```

---

### 3e. **Partager une Idea (Ajouter un Participant)**

**Méthode :** `fun addParticipantToIdea(ideaId: String, userId: String)`

**Ce qu'elle fait :**
1. Ajoute l'utilisateur comme participant de l'Idea
2. Met à jour l'Idea dans le StateFlow
3. L'Idea devient visible pour cet utilisateur

**Exemple :**
```kotlin
fun addParticipantToIdea(ideaId: String, userId: String) {
    val projectId = _selectedProject.value?.projectId ?: return
    
    viewModelScope.launch {
        ideasRepository.addParticipant(projectId, ideaId, userId)
            .fold(
                onSuccess = {
                    // Mettre à jour l'Idea dans la liste
                },
                onFailure = { error ->
                    _errorMsg.value = error.message
                }
            )
    }
}
```

---

### 4. **Mise à jour du Message en Cours**

**Méthode :** `fun updateMessage(message: String)`

**Ce qu'elle fait :**
- Met simplement à jour `currentMessage` dans le StateFlow
- Validation basique (longueur max, etc.)

**Exemple :**
```kotlin
fun updateMessage(message: String) {
    if (message.length <= MAX_MESSAGE_LENGTH) {
        _currentMessage.value = message
    }
}
```

---

### 5. **Envoi d'un Message**

**Méthode :** `fun sendMessage()`

**Ce qu'elle fait :**
1. **Validation :**
   - Vérifie que `selectedIdea` n'est pas null (doit être en mode CONVERSATION)
   - Vérifie que `currentMessage` n'est pas vide
   - Vérifie que `isSending` est false

2. **Préparation :**
   - Met `isSending = true`
   - Récupère le `currentUserId`

3. **Création du Message :**
   - Crée un objet `Message` avec :
     - `messageID` (généré)
     - `text` = `currentMessage`
     - `senderId` = `currentUserId`
     - `createdAt` = `Timestamp.now()`

4. **Envoi via Repository :**
   - Appelle `ideasRepository.sendMessage(ideaId, message)`
   - **OU** appelle le service MCP si c'est le premier message de l'Idea

5. **Nettoyage :**
   - Met `currentMessage = ""`
   - Met `isSending = false`
   - Gère les erreurs si l'envoi échoue

**Exemple :**
```kotlin
fun sendMessage() {
    val ideaId = _selectedIdea.value?.ideaId ?: return
    val messageText = _currentMessage.value.trim()
    if (messageText.isEmpty() || _isSending.value) return
    
    _isSending.value = true
    val currentUserId = getCurrentUserId() ?: return
    
    viewModelScope.launch {
        val message = Message(
            messageID = IdGenerator.generateMessageId(),
            text = messageText,
            senderId = currentUserId,
            createdAt = Timestamp.now()
        )
        
        ideasRepository.sendMessage(ideaId, message)
            .fold(
                onSuccess = {
                    _currentMessage.value = ""
                    _isSending.value = false
                },
                onFailure = { error ->
                    _errorMsg.value = error.message
                    _isSending.value = false
                }
            )
    }
}
```

---

### 6. **Chargement des Ideas**

**Méthode :** Dans `init { }` ou via un flow

**Ce qu'elle fait :**
- Observe les Ideas pour le projet sélectionné
- Utilise `ideasRepository.getIdeasForProject(projectId)`
- Met à jour `ideas` dans le StateFlow automatiquement

**Exemple :**
```kotlin
private val ideasFlow = _selectedProject
    .flatMapLatest { project ->
        if (project != null) {
            ideasRepository.getIdeasForProject(project.projectId)
        } else {
            flowOf(emptyList())
        }
    }
```

---

### 6b. **Chargement des Messages d'une Idea**

**Méthode :** Via un flow qui dépend de `selectedIdea`

**Ce qu'elle fait :**
- Observe les messages pour l'Idea sélectionnée
- Utilise `ideasRepository.getMessagesForIdea(ideaId)`
- Met à jour `messages` dans le StateFlow automatiquement

**Exemple :**
```kotlin
private val messagesFlow = _selectedIdea
    .flatMapLatest { idea ->
        if (idea != null) {
            ideasRepository.getMessagesForIdea(idea.ideaId)
        } else {
            flowOf(emptyList())
        }
    }
```

---

### 7. **Gestion des Erreurs**

**Méthode :** `fun clearError()`

**Ce qu'elle fait :**
- Remet `errorMsg` à `null` dans le StateFlow

**Gestion automatique :**
- Les flows doivent utiliser `.catch { }` pour capturer les erreurs
- Les erreurs doivent être mises dans `_errorMsg`

---

### 8. **Récupération de l'ID Utilisateur**

**Méthode :** `fun getCurrentUserId(): String?`

**Ce qu'elle fait :**
- Retourne l'ID de l'utilisateur actuellement connecté
- Utilise `FirebaseAuth.getInstance().currentUser?.uid`

---

## 🔄 Flows à Combiner dans uiState

Le `uiState` doit combiner :

1. **Projects Flow :** `projectRepository.getProjectsForCurrentUser()`
2. **Selected Project :** `MutableStateFlow<Project?>`
3. **Ideas Flow :** Dépend du projet sélectionné
4. **Selected Idea :** `MutableStateFlow<Idea?>`
5. **Messages Flow :** Dépend de l'Idea sélectionnée
6. **View Mode :** `MutableStateFlow<IdeasViewMode>`
7. **Input State :** `_currentMessage`, `_isSending`, `_errorMsg`

**Pattern :**
```kotlin
enum class IdeasViewMode {
    LIST,           // Affiche la liste des Ideas
    CONVERSATION    // Affiche la conversation d'une Idea
}

val uiState: StateFlow<IdeasUIState> = combine(
    projectsFlow,
    _selectedProject,
    ideasFlow,
    _selectedIdea,
    messagesFlow,
    _viewMode,
    _currentMessage,
    _isSending,
    _errorMsg
) { projects, selectedProject, ideas, selectedIdea, messages, viewMode, currentMessage, isSending, errorMsg ->
    IdeasUIState(
        selectedProject = selectedProject,
        availableProjects = projects,
        ideas = ideas,
        selectedIdea = selectedIdea,
        messages = messages,
        viewMode = viewMode,
        currentMessage = currentMessage,
        isSending = isSending,
        isLoading = false,
        errorMsg = errorMsg
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = IdeasUIState(isLoading = true)
)
```

---

## 📦 Dépendances du ViewModel

Le ViewModel aura besoin de :

1. **ProjectRepository** - Pour charger les projets
2. **IdeasRepository** (à créer) - Pour gérer les Ideas et messages
   - `getIdeasForProject(projectId): Flow<List<Idea>>`
   - `createIdea(idea: Idea): Result<String>`
   - `deleteIdea(projectId: String, ideaId: String): Result<Unit>`
   - `getMessagesForIdea(ideaId: String): Flow<List<Message>>`
   - `sendMessage(ideaId: String, message: Message): Result<Unit>`
   - `addParticipant(projectId: String, ideaId: String, userId: String): Result<Unit>`
3. **UserRepository** (optionnel) - Pour résoudre les noms des participants
4. **getCurrentUserId()** - Fonction pour obtenir l'ID utilisateur
5. **ConnectivityObserver** (optionnel) - Pour vérifier la connexion

**Exemple de constructeur :**
```kotlin
class IdeasViewModel(
    private val projectRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val ideasRepository: IdeasRepository = RepositoriesProvider.ideasRepository,
    private val userRepository: UserRepository = RepositoriesProvider.userRepository,
    private val getCurrentUserId: () -> String? = { 
        FirebaseAuth.getInstance().currentUser?.uid 
    },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel(), IdeasViewModelInterface
```

---

## ⚠️ Ce que le ViewModel NE DOIT PAS faire

1. **❌ Logique métier complexe** - C'est pour les repositories/services
2. **❌ Communication directe avec Firestore** - Via repositories uniquement
3. **❌ Logique MCP** - C'est pour un service MCP séparé
4. **❌ Validation complexe** - Juste validation basique (longueur, etc.)
5. **❌ Gestion de fichiers** - Si nécessaire, via FileStorageRepository

---

## ✅ Checklist d'Implémentation

- [ ] Créer `IdeasViewModel` qui implémente `IdeasViewModelInterface`
- [ ] Implémenter `uiState: StateFlow<IdeasUIState>` avec tous les champs
- [ ] Créer enum `IdeasViewMode` (LIST, CONVERSATION)
- [ ] Charger les projets dans `init { }`
- [ ] Implémenter `selectProject(project: Project)` - charge les Ideas, passe en mode LIST
- [ ] Implémenter `selectIdea(idea: Idea)` - charge les messages, passe en mode CONVERSATION
- [ ] Implémenter `createNewIdea(title: String?)` - crée et sélectionne une nouvelle Idea
- [ ] Implémenter `deleteIdea(ideaId: String)` - supprime une Idea
- [ ] Implémenter `addParticipantToIdea(ideaId: String, userId: String)` - partage une Idea
- [ ] Implémenter `updateMessage(message: String)`
- [ ] Implémenter `sendMessage()` - envoie dans l'Idea sélectionnée
- [ ] Implémenter `clearError()`
- [ ] Implémenter `getCurrentUserId(): String?`
- [ ] Gérer les flows avec `combine()` et `stateIn()`
- [ ] Gérer les erreurs avec `.catch { }`
- [ ] Utiliser `viewModelScope.launch` pour les opérations async

---

## 📝 Notes Importantes

1. **Pattern MVVM :** Le ViewModel est une couche mince qui coordonne les données
2. **StateFlow :** Source unique de vérité pour l'UI
3. **Flows :** Utiliser des flows pour les données réactives (projets, ideas, messages)
4. **Deux Modes :** Le screen peut afficher soit la liste des Ideas, soit la conversation d'une Idea
5. **Navigation :** Quand on sélectionne une Idea, on passe en mode CONVERSATION
6. **Error Handling :** Toujours gérer les erreurs et les exposer dans l'UI state
7. **Lifecycle :** Utiliser `SharingStarted.WhileSubscribed(5000)` pour optimiser les ressources

## 🎨 Structure de l'Idea (Data Model)

```kotlin
data class Idea(
    val ideaId: String = "",
    val projectId: String = "",
    val title: String? = null,  // Titre optionnel
    val content: String? = null,  // Description optionnelle
    val createdBy: String = "",
    val participantIds: List<String> = emptyList(),  // Pour partager
    val createdAt: Timestamp? = null,
    val lastUpdated: Timestamp? = null
)
```

---

**Date de création :** [Date]
**Dernière mise à jour :** [Date]
