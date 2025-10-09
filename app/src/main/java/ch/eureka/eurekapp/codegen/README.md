# Firestore Rules Code Generation

This project includes an annotation-based system for automatically generating Firestore security rules from Kotlin data classes.

## Overview

Instead of manually writing and maintaining Firestore security rules, you can annotate your data classes with metadata about access control, validation, and collection paths. The code generator then produces the complete `firestore.rules` file automatically.

## Annotations

All annotations are located in `model/annotations/firestore/`:

### `@CollectionPath`
Defines where the entity lives in Firestore.

```kotlin
@CollectionPath("users")  // Top-level collection
@CollectionPath("workspaces/{workspaceId}/groups")  // Nested collection
```

### `@Rules`
Declares Firestore access conditions using CEL expressions.

```kotlin
@Rules(
    read = "request.auth != null && request.auth.uid == resource.id",
    create = "request.auth != null",
    update = "request.auth != null && request.auth.uid == resource.id",
    delete = "false"  // Prevent deletion
)
```

Common patterns:
- `"request.auth != null"` - User must be authenticated
- `"request.auth.uid == resource.data.uid"` - User owns the resource
- `"request.auth.uid in resource.data.members.keys()"` - User is a member
- `"true"` - Allow all
- `"false"` - Deny all

### `@Required`
Marks a property that must be present on create/update. The generator emits `hasAll()` checks.

```kotlin
@Required
val name: String
```

### `@Length`
Restricts string length in generated rules.

```kotlin
@Length(min = 1, max = 100)
val displayName: String
```

### `@ServerTimestamp`
Marks timestamp fields as server-managed. The generator ensures `request.time` is used.

```kotlin
@ServerTimestamp
val createdAt: Timestamp
```

### `@Immutable`
Marks properties that cannot be changed after creation. The generator validates that the field matches existing data on updates.

```kotlin
@Immutable
val uid: String
```

## Example Data Class

```kotlin
@CollectionPath("users")
@Rules(
    read = "request.auth != null && request.auth.uid == resource.id",
    create = "request.auth != null && request.auth.uid == request.resource.id",
    update = "request.auth != null && request.auth.uid == resource.id",
    delete = "false"
)
data class User(
    @Required
    @Immutable
    val uid: String,

    @Required
    @Length(min = 1, max = 100)
    val displayName: String,

    @Required
    val email: String,

    val photoUrl: String,

    @ServerTimestamp
    val lastActive: Timestamp
)
```

## Generating Rules

### Method 1: Gradle Task (Recommended)

Run the Gradle task:

```bash
./gradlew generateFirestoreRules
```

This will generate `firestore.rules` in the project root directory.

### Method 2: Run Main Function

You can also run the main function directly from your IDE:
- Open `ch.eureka.eurekapp.codegen.RulesGeneratorMain.kt`
- Run the `main()` function

### Method 3: Programmatic Usage

```kotlin
val generator = FirestoreRulesGenerator()
    .addClass(User::class)
    .addClass(Workspace::class)
    .addClasses(Group::class, Project::class)

val rulesContent = generator.generate()
File("firestore.rules").writeText(rulesContent)
```

## Generated Output Example

```
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // User
    match /users/{usersId} {
      allow get: if request.auth != null && request.auth.uid == resource.id;
      allow list: if request.auth != null && request.auth.uid == resource.id;
      allow create: if request.auth != null && request.auth.uid == request.resource.id &&
                       request.resource.data.keys().hasAll(['uid', 'displayName', 'email']) &&
                       request.resource.data.displayName.size() >= 1 &&
                       request.resource.data.displayName.size() <= 100 &&
                       request.resource.data.lastActive == request.time;
      allow update: if request.auth != null && request.auth.uid == resource.id &&
                       request.resource.data.keys().hasAll(['uid', 'displayName', 'email']) &&
                       request.resource.data.displayName.size() >= 1 &&
                       request.resource.data.displayName.size() <= 100 &&
                       request.resource.data.uid == resource.data.uid &&
                       request.resource.data.lastActive == request.time;
    }

  }
}
```

## Adding New Entities

1. Create your data class in `model/data/`
2. Add the appropriate annotations (`@CollectionPath`, `@Rules`, etc.)
3. Register the class in `RulesGeneratorMain.kt`:

```kotlin
val generator = FirestoreRulesGenerator()
    .addClasses(
        User::class,
        Workspace::class,
        // Add your new class here
        YourNewClass::class
    )
```

4. Run the generator

## Benefits

- **Type Safety**: Annotations are checked at compile time
- **Single Source of Truth**: Data model and security rules live together
- **Automatic Validation**: Field constraints (length, required, immutable) are enforced
- **Maintainability**: Changes to data model automatically propagate to rules
- **Documentation**: Annotations serve as inline documentation for access control

## Architecture

```
model/
├── annotations/firestore/   # Annotation definitions
│   ├── CollectionPath.kt
│   ├── Rules.kt
│   ├── Required.kt
│   ├── Length.kt
│   ├── ServerTimestamp.kt
│   └── Immutable.kt
├── data/                    # Annotated data classes
│   ├── User.kt
│   ├── Workspace.kt
│   └── ...
└── codegen/                 # Code generation logic
    ├── FirestoreRulesGenerator.kt
    └── RulesGeneratorMain.kt
```

## Troubleshooting

### Unresolved references (findAnnotation, memberProperties, etc.)

If you see IDE errors about unresolved references in `FirestoreRulesGenerator.kt`:

1. **Sync Gradle**: File → Sync Project with Gradle Files
2. **Rebuild**: Build → Rebuild Project
3. **Invalidate Caches**: File → Invalidate Caches / Restart
4. **Check dependency**: Ensure `kotlin-reflect` is in `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
}
```

5. **Wait for indexing**: IntelliJ/Android Studio needs time to index the reflect library

The code will compile and run correctly even if IDE shows errors temporarily.

### Running the generator

**Option 1: Standalone (Easiest)**
```kotlin
// In your code or a test:
GeneratorStandalone.generateToFile("firestore.rules")
```

**Option 2: Direct main function**
- Open `GeneratorStandalone.kt`
- Right-click on the `main()` function
- Select "Run"

**Option 3: Gradle task**
```bash
./gradlew generateFirestoreRules
```

### Rules not updating

Make sure to re-run the generator after changing annotations or adding new entities.

### Complex validation rules

For complex business logic that can't be expressed with annotations, you can override the generated rules manually or contribute to the `@Rules` annotation with custom CEL expressions.
