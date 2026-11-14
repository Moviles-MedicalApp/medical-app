package pe.com.smart.presentation.users

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Short-lived navigation handoff; no profile identifier is persisted. */
object UserProvisioningSelection {
    var profileId: Int? = null
    var role: String? = null
    var profileName: String? = null
    var specialityId: Int? = null
    var email: String? = null
    var version by mutableIntStateOf(0)

    fun select(profileId: Int, role: String, profileName: String, specialityId: Int? = null, email: String? = null) {
        this.profileId = profileId
        this.role = role
        this.profileName = profileName
        this.specialityId = specialityId
        this.email = email
        version++
    }

    fun clear() {
        profileId = null
        role = null
        profileName = null
        specialityId = null
        email = null
        version++
    }
}
