package tn.esprit.dam.features.vault.domain.password

import tn.esprit.dam.features.vault.data.model.PasswordEntry

object PasswordMatcher {
    private val domainIndex = mutableMapOf<String, MutableList<PasswordEntry>>()

    fun buildIndex(passwords: List<PasswordEntry>) {
        domainIndex.clear()
        passwords.forEach { entry ->
            val domain = extractDomain(entry.site)
            domainIndex.getOrPut(domain) { mutableListOf() }.add(entry)

            entry.url?.let { url ->
                val urlDomain = extractDomainFromUrl(url)
                if (urlDomain != domain) {
                    domainIndex.getOrPut(urlDomain) { mutableListOf() }.add(entry)
                }
            }
        }
    }

    fun findMatchingPasswords(url: String): List<PasswordEntry> {
        val domain = extractDomainFromUrl(url)
        return domainIndex[domain] ?: emptyList()
    }

    fun findSimilarPasswords(siteName: String): List<PasswordEntry> {
        val searchTerm = siteName.lowercase().trim()
        return domainIndex.entries
            .filter { it.key.contains(searchTerm) || searchTerm.contains(it.key) }
            .flatMap { it.value }
            .distinct()
    }

    private fun extractDomain(site: String): String {
        return site.lowercase()
            .replace(Regex("^(www\\.)"), "")
            .split(".")[0]
    }

    private fun extractDomainFromUrl(url: String): String {
        val cleaned = url.lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .split("/")[0]
            .split(":")[0]

        return cleaned.split(".").let {
            when {
                it.size >= 2 -> it[it.size - 2]
                else -> it[0]
            }
        }
    }
}
