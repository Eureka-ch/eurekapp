package ch.eureka.eurekapp.ui.components.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.user.UserNotificationSettingsKeys
import ch.eureka.eurekapp.model.data.user.defaultValuesNotificationSettingsKeys
import ch.eureka.eurekapp.model.notifications.NotificationSettingsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Partially written using AI.
enum class HelpContext {
  HOME_OVERVIEW,
  TASKS,
  MEETINGS,
  PROJECTS,
  CREATE_TASK
}

@Composable
fun InteractiveHelpEntryPoint(
    helpContext: HelpContext,
    modifier: Modifier = Modifier,
    userProvidedName: String? = null,
    chipShape: Shape = MaterialTheme.shapes.large,
    notificationSettingsViewModel: NotificationSettingsViewModel = viewModel()
) {
  val helpEnabledDefault =
      defaultValuesNotificationSettingsKeys.getOrDefault(
          UserNotificationSettingsKeys.SHOW_INTERACTIVE_HELP.name, true)
  val isHelpEnabled by
      notificationSettingsViewModel
          .getUserSetting(UserNotificationSettingsKeys.SHOW_INTERACTIVE_HELP)
          .collectAsState(helpEnabledDefault)

  if (!isHelpEnabled) return

  val resolvedName =
      remember(userProvidedName) {
            when {
              !userProvidedName.isNullOrBlank() -> userProvidedName
              else -> Firebase.auth.currentUser?.displayName.orEmpty()
            }
          }
          .ifBlank { "there" }

  var isDialogOpen by rememberSaveable { mutableStateOf(false) }

  val helpContent = remember(resolvedName, helpContext) { helpContext.toHelpContent(resolvedName) }

  AssistChip(
      onClick = { isDialogOpen = true },
      label = { Text("Guide") },
      leadingIcon = {
        Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = null)
      },
      modifier = modifier,
      shape = chipShape)

  if (isDialogOpen) {
    AlertDialog(
        onDismissRequest = { isDialogOpen = false },
        confirmButton = { TextButton(onClick = { isDialogOpen = false }) { Text("Got it!") } },
        title = { Text(helpContent.title) },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(helpContent.intro)
            helpContent.steps.forEach { step ->
              Surface(
                  tonalElevation = 1.dp,
                  shape = MaterialTheme.shapes.medium,
                  color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                          Text(step.highlight, style = MaterialTheme.typography.titleSmall)
                          Text(step.detail, style = MaterialTheme.typography.bodyMedium)
                        }
                  }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tu peux désactiver cette aide depuis Préférences > Notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        })
  }
}

private data class HelpContent(val title: String, val intro: String, val steps: List<HelpStep>)

private data class HelpStep(val highlight: String, val detail: String)

private fun HelpContext.toHelpContent(userName: String): HelpContent {
  return when (this) {
    HelpContext.HOME_OVERVIEW ->
        HelpContent(
            title = "Bienvenue $userName 👋",
            intro = "Hey $userName, faisons un tour rapide du tableau important.",
            steps =
                listOf(
                    HelpStep(
                        "Cartes récap'",
                        "Les trois cartes du haut te donnent en un clin d'œil tes tâches, réunions et projets actifs."),
                    HelpStep(
                        "Actions rapides",
                        "Utilise les boutons 'View all' pour ouvrir les sections complètes (Tasks, Meetings, Projects)."),
                    HelpStep(
                        "Sections interactives",
                        "Tap sur une carte de tâche, réunion ou projet pour ouvrir directement la vue détaillée.")))
    HelpContext.TASKS ->
        HelpContent(
            title = "Gestion des tâches",
            intro = "Hello $userName ! Voici comment dompter tes tâches rapidement.",
            steps =
                listOf(
                    HelpStep(
                        "Barre de filtres",
                        "Les chips en haut permettent de passer de 'My tasks' à 'Team', 'Today', etc."),
                    HelpStep(
                        "Boutons d'action",
                        "Les boutons 'Create task' et 'Auto assign' t'aident à lancer ou répartir le travail."),
                    HelpStep(
                        "Cartes interactives",
                        "Appuie sur une carte pour ouvrir la tâche; utilise l'icône dossier pour gérer les fichiers.")))
    HelpContext.MEETINGS ->
        HelpContent(
            title = "Réunions maîtrisées",
            intro = "$userName, passons en revue ce que tu peux faire ici.",
            steps =
                listOf(
                    HelpStep(
                        "Onglets Upcoming/Past",
                        "Navigue entre tes réunions futures et passées pour garder le rythme."),
                    HelpStep(
                        "Carte réunion",
                        "Chaque carte offre l'accès au vote, aux directions et aux actions de suivi."),
                    HelpStep(
                        "Bouton +",
                        "Le bouton flottant 'Add' crée instantanément une nouvelle réunion (si tu es en ligne).")))
    HelpContext.PROJECTS ->
        HelpContent(
            title = "Vue projet",
            intro = "Hello $userName, voici comment profiter de l’aperçu projet.",
            steps =
                listOf(
                    HelpStep(
                        "Contexte du projet",
                        "La vue affiche les infos clefs du projet sélectionné pour t’aider à rester concentré."),
                    HelpStep(
                        "Navigation rapide",
                        "Tu peux lancer la caméra ou d’autres actions spécifiques au projet depuis cette page."),
                    HelpStep(
                        "Revenir à l’accueil",
                        "Utilise la barre du bas pour retourner rapidement aux tâches ou aux réunions associées.")))
    HelpContext.CREATE_TASK ->
        HelpContent(
            title = "Création guidée",
            intro = "$userName, passons étape par étape pour créer ta tâche.",
            steps =
                listOf(
                    HelpStep(
                        "Champs essentiels",
                        "Commence par le titre, la description et la date limite pour donner le contexte."),
                    HelpStep(
                        "Projet & équipe",
                        "Choisis un projet, assigne des membres et ajoute des dépendances si besoin."),
                    HelpStep(
                        "Pièces jointes",
                        "Ajoute des pièces jointes ou des photos depuis le bas de l’écran avant d’enregistrer.")))
  }
}
