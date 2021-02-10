package com.unionbankph.corporate.common.data.source.local.cache

class CacheManager {

    private val mCache: MutableMap<String, Any?> = mutableMapOf()

    companion object {
        const val ACCESS_TOKEN = "access_token"
        const val DAO_ACCESS_TOKEN = "dao_access_token"
        const val DAO_REFERENCE_NUMBER = "dao_reference_number"
        const val CORPORATE_USER = "corporate_user"
        const val ENABLED_FEATURES = "enabled_features"
        const val DAO_SIGNATORY_DETAILS = "dao_signatory_details"
        const val ROLE = "role"
    }

    /*
     * retrieves a boolean value for a corresponding key
     * if this key is previously not inserted using put(..., ...) above
     * it will return null
     */
    fun getBoolean(key: String): Boolean {
        return get(key) == "1"
    }

    /*
    * Put a key and corresponding value in a map of String, Boolean
    */
    fun putBoolean(key: String, value: Boolean) {
        put(key, if (value) "1" else "0")
    }

    /*
     * Put a key and corresponding value in a map of String, String
     */
    fun put(key: String, value: Any?) {
        when {
            key.isNotEmpty() -> mCache[key] = value
            else -> throw IllegalArgumentException("Key cannot be empty")
        }
    }

    /*
     * retrieves a string value for a corresponding key
     * if this key is previously not inserted using put(..., ...) above
     * it will return null
     */
    fun get(key: String): String {
        return try {
            when {
                mCache.containsKey(key) -> mCache[key]
                else -> ""
            }  as String
        } catch (e: Exception) {
            ""
        }
    }

    fun getObject(key: String): Any? {
        return when {
            mCache.containsKey(key) -> mCache[key]
            else -> null
        }
    }

    /*
       * it will clear specific cache
       */
    fun clear(key: String) {
        mCache.remove(key)
    }

    /*
     * it will clear cache
     */
    fun clear() {
        mCache.clear()
    }

    fun accessToken(): String = "Bearer ${get(ACCESS_TOKEN)}"
}
