package com.bugsee.kmp

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri

// public actual typealias Context = Context

internal var applicationContext: Context? = null
    private set

/**
 * A ContentProvider that does NOT store or provide any data for read or write operations.
 *
 * It's only purpose is to retrieve and store the application context in an internal top-level
 * variable [applicationContext]. The context is used for [Bugsee.launch].
 */
internal class BugseeContextProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val context = context
        if (context != null) {
            applicationContext = context.applicationContext
        } else {
            error("Context cannot be null")
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        error("Not allowed.")
    }

    override fun getType(uri: Uri): String? {
        error("Not allowed.")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        error("Not allowed.")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        error("Not allowed.")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        error("Not allowed.")
    }
}