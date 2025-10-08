# EurekaStyles - Reusable Components

## Where to find the styles
**File**: `app/src/main/java/ch/eureka/eurekapp/ui/designsystem/tokens/EurekaStyles.kt`

## Where to find the components
**Folder**: `app/src/main/java/ch/eureka/eurekapp/ui/components/`

## Available Styles

### Buttons

#### Primary Button
```kotlin
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

Button(
    onClick = { /* action */ },
    colors = EurekaStyles.PrimaryButtonColors()
) {
    Text("My Button")
}
```

#### Outlined Button
```kotlin
OutlinedButton(
    onClick = { /* action */ },
    colors = EurekaStyles.OutlinedButtonColors()
) {
    Text("My Button")
}
```

### Text Fields

#### TextField with Eureka Style
```kotlin
OutlinedTextField(
    value = text,
    onValueChange = { text = it },
    colors = EurekaStyles.TextFieldColors(),
    label = { Text("My Label") }
)
```

### Cards

#### Card with Eureka Style
```kotlin
Card(
    shape = EurekaStyles.CardShape,
    elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)
) {
    Text("My card content")
}
```

## Reusable Components Present on All Pages

### 1. Top Header Bar
**File**: `EurekaTopBar.kt`
**Appears on**: All screens
**Purpose**: Brand identity and navigation

```kotlin
import ch.eureka.eurekapp.ui.components.EurekaTopBar

@Composable
fun MyScreen() {
    EurekaTopBar(title = "EUREKA")
    // ... rest of your content
}
```

### 2. Bottom Navigation Bar
**File**: `EurekaBottomNav.kt`
**Appears on**: All main screens
**Purpose**: Primary navigation between app sections

```kotlin
import ch.eureka.eurekapp.ui.components.EurekaBottomNav

@Composable
fun MyScreen() {
    Column {
        // Your content here
        
        EurekaBottomNav(
            currentRoute = "Tasks",
            onNavigate = { route -> 
                // Handle navigation
            }
        )
    }
}
```

### 3. Information Cards
**File**: `EurekaInfoCard.kt`
**Appears on**: Dashboard, Summary screens
**Purpose**: Display key information in consistent format

```kotlin
import ch.eureka.eurekapp.ui.components.EurekaInfoCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist

@Composable
fun DashboardScreen() {
    EurekaInfoCard(
        icon = Icons.Default.Checklist,
        title = "Tasks in Progress",
        primaryValue = "3 open",
        secondaryValue = "1 due today"
    )
}
```

### 4. Task Cards
**File**: `EurekaTaskCard.kt`
**Appears on**: Tasks screen, Project screens
**Purpose**: Display task information consistently

```kotlin
import ch.eureka.eurekapp.ui.components.EurekaTaskCard

@Composable
fun TasksScreen() {
    EurekaTaskCard(
        title = "Implement Overview Screen",
        dueDate = "Due: Today - 23:59",
        assignee = "Assigned: Ismail",
        priority = "High",
        category = "UI",
        progress = 0.65f,
        onToggleComplete = { /* handle completion */ }
    )
}
```

### 5. Filter/Segmented Control
**File**: `EurekaFilterBar.kt`
**Appears on**: Tasks screen, Ideas screen
**Purpose**: Filter content by different criteria

```kotlin
import ch.eureka.eurekapp.ui.components.EurekaFilterBar

@Composable
fun TasksScreen() {
    val filterOptions = listOf("Me", "Team", "This week", "All")
    
    EurekaFilterBar(
        options = filterOptions,
        selectedOption = "Me",
        onOptionSelected = { option -> 
            // Handle filter selection
        }
    )
}
```

### 6. Status Tags
**File**: `EurekaStatusTag.kt`
**Appears on**: All screens with status indicators
**Purpose**: Show status, priority, or category information

```kotlin
import ch.eureka.eurekapp.ui.components.EurekaStatusTag
import ch.eureka.eurekapp.ui.components.StatusType

@Composable
fun TaskItem() {
    Row {
        EurekaStatusTag(
            text = "High Priority",
            type = StatusType.ERROR
        )
        
        EurekaStatusTag(
            text = "UI",
            type = StatusType.INFO
        )
    }
}
```

## How to use

### 1. Import the components
```kotlin
// Import individual components
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.EurekaBottomNav
import ch.eureka.eurekapp.ui.components.EurekaTaskCard

// Or import all at once
import ch.eureka.eurekapp.ui.components.*
```

### 2. Use EurekaTheme
```kotlin
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme

@Composable
fun MyScreen() {
    EurekaTheme(darkTheme = false) {
        // Your content with components
    }
}
```

### Complete example
```kotlin
@Composable
fun MyScreen() {
    EurekaTheme(darkTheme = false) {
        Column {
            // Top bar
            EurekaTopBar()
            
            // Content
            LazyColumn {
                item {
                    EurekaInfoCard(
                        icon = Icons.Default.Checklist,
                        title = "Tasks in Progress",
                        primaryValue = "3 open",
                        secondaryValue = "1 due today"
                    )
                }
                
                item {
                    EurekaTaskCard(
                        title = "Implement Overview Screen",
                        dueDate = "Due: Today - 23:59",
                        assignee = "Assigned: Ismail",
                        priority = "High",
                        category = "UI",
                        progress = 0.65f
                    )
                }
            }
            
            // Bottom navigation
            EurekaBottomNav(
                currentRoute = "Tasks",
                onNavigate = { /* handle navigation */ }
            )
        }
    }
}
```

## Tips
- Always use `EurekaTheme()` around your content
- Styles automatically adapt to dark/light mode
- Components are ready to use - just import and call them
- If you want to create a new component, add it to the `components/` folder
- Use the reusable components for consistency across all screens