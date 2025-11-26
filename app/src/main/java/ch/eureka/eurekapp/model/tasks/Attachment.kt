// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.tasks

import android.net.Uri

sealed interface Attachment {
  data class Remote(val url: String) : Attachment

  data class Local(val uri: Uri) : Attachment
}
